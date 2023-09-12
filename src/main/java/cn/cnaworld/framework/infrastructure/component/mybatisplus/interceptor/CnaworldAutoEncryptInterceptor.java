package cn.cnaworld.framework.infrastructure.component.mybatisplus.interceptor;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.annotation.CnaFieldEncrypt;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.EncryptAlgorithmProcessor;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.booleanprocessor.impl.BooleanEncryptProcessor;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.dateprocessor.impl.DateEncryptProcessor;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.localdateprocessor.impl.LocalDateEncryptProcessor;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.numberprocessor.impl.NumberEncryptProcessor;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.stringprocessor.impl.AESEncryptProcessor;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.statics.enums.EncryptAlgorithm;
import cn.cnaworld.framework.infrastructure.properties.CnaworldMybatisPlusProperties;
import cn.cnaworld.framework.infrastructure.utils.log.CnaLogUtil;
import cn.cnaworld.framework.infrastructure.utils.object.CnaObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.Cipher;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 解决逻辑删除时，不自动修改updateTime以及version问题
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.10
 */
@Intercepts({
		//type指定代理的是那个对象，method指定代理Executor中的那个方法,args指定Executor中的query方法都有哪些参数对象
		//由于Executor中有两个query，因此需要两个@Signature
		@Signature(type = Executor.class,
				method = "update",
				args = { MappedStatement.class, Object.class }),//需要代理的对象和方法.
		@Signature(type = Executor.class,
		method = "query",
		args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})//需要代理的对象和方法
})
@Slf4j
public class CnaworldAutoEncryptInterceptor implements Interceptor{

	@Autowired
	private CnaworldMybatisPlusProperties cnaworldMybatisPlusProperties;
   //缓存实体类的哪些属性是目标属性，以及属性的注解值
	private static final Map<Class<?>,Map<Field,CnaFieldEncrypt>> ENTITY_FIELD_CACHE = new ConcurrentHashMap<>();

	private static final Map<String,EncryptAlgorithmProcessor> ENCRYPT_PROCESSOR_CACHE = new ConcurrentHashMap<>();

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		final Object[] args = invocation.getArgs();
		MappedStatement ms = (MappedStatement) args[0];
		if (SqlCommandType.SELECT == ms.getSqlCommandType()) {
			return handleRead(invocation);
		} else if (SqlCommandType.UPDATE == ms.getSqlCommandType() || SqlCommandType.INSERT == ms.getSqlCommandType()) {
			handleWrite(args);
		}
		// 返回，继续执行
		return invocation.proceed();
	}

	private void handleWrite(Object[] args) {
		Object parameterObject = args[1];
		if(CnaObjectUtil.notObject(parameterObject)){
			return;
		}
		Map<Field, CnaFieldEncrypt> fieldMap = getEntityFieldCache(parameterObject);
		if(parameterObject instanceof List){
			List<?> parameterList = (List<?>) parameterObject;
			if(ObjectUtils.isNotEmpty(parameterList)){
				parameterListFor(fieldMap, parameterList);
			}
		}else if (parameterObject instanceof Map){
			Map<?,?> objMap = (HashMap<?,?>) parameterObject;
			Object parameter;
			if(objMap.containsKey("et")){
				parameter = objMap.get("et");
				fieldMapFor(fieldMap, parameter, Cipher.ENCRYPT_MODE);
			} else {
				parameter = objMap.getOrDefault("collection", null);
				List<?> parameterList = (List<?>) parameter;
				if(ObjectUtils.isNotEmpty(parameterList)){
					parameterListFor(fieldMap, parameterList);
				}
			}
		} else{
			fieldMapFor(fieldMap, parameterObject, Cipher.ENCRYPT_MODE);
		}
	}

	private void parameterListFor(Map<Field, CnaFieldEncrypt> fieldMap, List<?> parameterList) {
		parameterList.forEach(param-> fieldMapFor(fieldMap, param, Cipher.ENCRYPT_MODE));
	}

	private void fieldMapFor(Map<Field, CnaFieldEncrypt> fieldMap, Object parameterObject, int number) {
		if (fieldMap != null) {
			fieldMap.forEach((field, annotation) -> processor(parameterObject, field, annotation, number));
		}
	}

	@Nullable
	private Object handleRead(Invocation invocation) throws InvocationTargetException, IllegalAccessException {
		//执行并获取返回结果
		Object proceed = invocation.proceed();
		if(CnaObjectUtil.notObject(proceed)){
			return proceed;
		}
		Map<Field, CnaFieldEncrypt> fieldMap = getEntityFieldCache(proceed);
		if(proceed instanceof List){
			//讲对象转换成list
			List<?> resultList = (List<?>) proceed;
			if(ObjectUtils.isNotEmpty(resultList)){
				//将结果集中所有需要自动解密的字段进行遍历解密
				resultList.forEach(result-> fieldMapFor(fieldMap, result, Cipher.DECRYPT_MODE));
			}
		}else {
			fieldMapFor(fieldMap, proceed, Cipher.DECRYPT_MODE);
		}
		return proceed;
	}

	private void processor(Object entity, Field field, CnaFieldEncrypt annotation,int encryptType) {
		if (CnaObjectUtil.isEmpty(entity)){
			return;
		}
		try {
			field.setAccessible(true);
			Object value = field.get(entity);
			if (ObjectUtils.isNotEmpty(value)){
				EncryptAlgorithmProcessor encryptAlgorithmProcessor = encryptAlgorithmProcessorFactory(annotation,value);
				//判断是使用全局密钥还是字段密钥
				String[] keys = getKeys(field, annotation);
				//算法和密钥都没问题，则进行解密，并将数据回写到原始结果集中
				Object content;
				if (Cipher.ENCRYPT_MODE == encryptType){
					content = encryptAlgorithmProcessor.encrypt(value,keys);
				}else {
					content = encryptAlgorithmProcessor.decrypt(value,keys);
				}
				if (!ObjectUtils.isEmpty(content)){
					field.set(entity, content);
				}
			}
			field.setAccessible(false);
		} catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private String[] getKeys(Field field, CnaFieldEncrypt annotation) {
		if(ObjectUtils.isNotEmpty(annotation.keys())){
			return annotation.keys();
		}else if (ObjectUtils.isNotEmpty(cnaworldMybatisPlusProperties.getFieldEncrypt().getKeys())){
			return cnaworldMybatisPlusProperties.getFieldEncrypt().getKeys();
		}else {
			CnaLogUtil.error(log,"field:{},加密解密失败，密钥未配置", field.getDeclaringClass()+"."+ field.getName());
			return null;
		}
	}

	private EncryptAlgorithmProcessor encryptAlgorithmProcessorFactory(CnaFieldEncrypt annotation, Object value) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		EncryptAlgorithmProcessor encryptAlgorithmProcessor;
		EncryptAlgorithm algorithm = annotation.encryptAlgorithm();
		Class<? extends EncryptAlgorithmProcessor> aClass = annotation.encryptAlgorithmProcessor();
		if(!(EncryptAlgorithmProcessor.class == aClass)){
			if(ENCRYPT_PROCESSOR_CACHE.containsKey(aClass.getName())){
				encryptAlgorithmProcessor = ENCRYPT_PROCESSOR_CACHE.get(aClass.getName());
			}else {
				encryptAlgorithmProcessor = aClass.newInstance();
				ENCRYPT_PROCESSOR_CACHE.put(aClass.getName(),encryptAlgorithmProcessor);
			}
		}else {
			if(ObjectUtils.isNotEmpty(cnaworldMybatisPlusProperties.getFieldEncrypt()) && ObjectUtils.isNotEmpty(cnaworldMybatisPlusProperties.getFieldEncrypt().getEncryptAlgorithmProcessor())){
				if(ENCRYPT_PROCESSOR_CACHE.containsKey(cnaworldMybatisPlusProperties.getFieldEncrypt().getEncryptAlgorithmProcessor().getName())){
					encryptAlgorithmProcessor = ENCRYPT_PROCESSOR_CACHE.get(cnaworldMybatisPlusProperties.getFieldEncrypt().getEncryptAlgorithmProcessor().getName());
				}else {
					encryptAlgorithmProcessor = cnaworldMybatisPlusProperties.getFieldEncrypt().getEncryptAlgorithmProcessor().newInstance();
					ENCRYPT_PROCESSOR_CACHE.put(cnaworldMybatisPlusProperties.getFieldEncrypt().getEncryptAlgorithmProcessor().getName(),encryptAlgorithmProcessor);
				}
			}else{
				//如果没有定义过算法，则采用默认的AES算法实现
				if(algorithm.equals(EncryptAlgorithm.NONE)){
					if(ObjectUtils.isEmpty(cnaworldMybatisPlusProperties.getFieldEncrypt()) || ObjectUtils.isEmpty(cnaworldMybatisPlusProperties.getFieldEncrypt().getAlgorithm())){
						encryptAlgorithmProcessor = getEncryptAlgorithmProcessor(null,value);
					}else {
						encryptAlgorithmProcessor = getEncryptAlgorithmProcessor(cnaworldMybatisPlusProperties.getFieldEncrypt().getAlgorithm().name(),value);
					}
				}else {
					encryptAlgorithmProcessor = getEncryptAlgorithmProcessor(algorithm.name(),value);
				}
			}
		}
		return encryptAlgorithmProcessor;
	}

	@NotNull
	private EncryptAlgorithmProcessor getEncryptAlgorithmProcessor(String name , Object value) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		EncryptAlgorithmProcessor encryptAlgorithmProcessor;
		if(value instanceof Number){
			if(ENCRYPT_PROCESSOR_CACHE.containsKey(Number.class.getName())){
				encryptAlgorithmProcessor = ENCRYPT_PROCESSOR_CACHE.get(Number.class.getName());
			}else {
				encryptAlgorithmProcessor = new NumberEncryptProcessor();
				ENCRYPT_PROCESSOR_CACHE.put(Number.class.getName(),encryptAlgorithmProcessor);
			}
		}else if (value instanceof Boolean) {
			if(ENCRYPT_PROCESSOR_CACHE.containsKey(Boolean.class.getName())){
				encryptAlgorithmProcessor = ENCRYPT_PROCESSOR_CACHE.get(Boolean.class.getName());
			}else {
				encryptAlgorithmProcessor = new BooleanEncryptProcessor();
				ENCRYPT_PROCESSOR_CACHE.put(Boolean.class.getName(),encryptAlgorithmProcessor);
			}
		} else if (value instanceof Date) {
			if(ENCRYPT_PROCESSOR_CACHE.containsKey(Date.class.getName())){
				encryptAlgorithmProcessor = ENCRYPT_PROCESSOR_CACHE.get(Date.class.getName());
			}else {
				encryptAlgorithmProcessor = new DateEncryptProcessor();
				ENCRYPT_PROCESSOR_CACHE.put(Date.class.getName(),encryptAlgorithmProcessor);
			}
		}else if (value instanceof Temporal) {
			if(ENCRYPT_PROCESSOR_CACHE.containsKey(Temporal.class.getName())){
				encryptAlgorithmProcessor = ENCRYPT_PROCESSOR_CACHE.get(Temporal.class.getName());
			}else {
				encryptAlgorithmProcessor = new LocalDateEncryptProcessor();
				ENCRYPT_PROCESSOR_CACHE.put(Temporal.class.getName(),encryptAlgorithmProcessor);
			}
		}else if (value instanceof String) {
			if(StringUtils.isNotBlank(name)){
				if(ENCRYPT_PROCESSOR_CACHE.containsKey(Temporal.class.getName()+name)){
					encryptAlgorithmProcessor = ENCRYPT_PROCESSOR_CACHE.get(Temporal.class.getName()+name);
				}else {
					String encryptProcessor = "cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.stringprocessor.impl." + name + "EncryptProcessor";
					Class<?> encryptProcessorClass = Class.forName(encryptProcessor);
					encryptAlgorithmProcessor = (EncryptAlgorithmProcessor) encryptProcessorClass.newInstance();
					ENCRYPT_PROCESSOR_CACHE.put(String.class.getName()+name,encryptAlgorithmProcessor);
				}
			}else {
				if(ENCRYPT_PROCESSOR_CACHE.containsKey(String.class.getName()+"aes")){
					encryptAlgorithmProcessor = ENCRYPT_PROCESSOR_CACHE.get(String.class.getName()+"aes");
				}else {
					encryptAlgorithmProcessor = new AESEncryptProcessor();
					ENCRYPT_PROCESSOR_CACHE.put(String.class.getName()+"aes",encryptAlgorithmProcessor);
				}
			}
		}else {
			if(ENCRYPT_PROCESSOR_CACHE.containsKey(String.class.getName()+"aes")){
				encryptAlgorithmProcessor = ENCRYPT_PROCESSOR_CACHE.get(String.class.getName()+"aes");
			}else {
				encryptAlgorithmProcessor = new AESEncryptProcessor();
				ENCRYPT_PROCESSOR_CACHE.put(String.class.getName()+"aes",encryptAlgorithmProcessor);
			}
		}
		return encryptAlgorithmProcessor;
	}

	private Map<Field,CnaFieldEncrypt> getEntityFieldCache(Object obj){
		if(ObjectUtils.isEmpty(obj)){
			return null;
		}
		Class<?> objClass = null;
		//属性及属性注解缓存
		Map<Field,CnaFieldEncrypt> fieldMap;
		if(obj instanceof List){
			//讲对象转换成list
			List<?> objList = (List<?>) obj;
			if(ObjectUtils.isNotEmpty(objList)){
				//得到list的泛型类型
				Object o = objList.get(0);
				if(!(o instanceof Map)){
					objClass = objList.get(0).getClass();
				}else {
					return null;
				}
			}
		}else if(obj instanceof Map){
			Map<?,?> objMap = (HashMap<?,?>) obj;
			Object o = null;
			if(objMap.containsKey("et")){
				o = objMap.get("et");
			} else if (objMap.containsKey("collection")) {
				o = objMap.get("collection");
				List<?> objList = (List<?>) o ;
				if(ObjectUtils.isNotEmpty(objList)){
					//得到list的泛型类型
					o = objList.get(0);
				}
			}
			if(CnaObjectUtil.isEmpty(o)){
				return null;
			}
			objClass = o.getClass();
		}else{
			objClass = obj.getClass();
		}
		//判断泛型类是否已经被缓存
		if(!ENTITY_FIELD_CACHE.containsKey(objClass)){
			//没有被缓存则遍历类中所有字段，检查是否有需要自动解密的字段并缓存
			Field[] objFields = new Field[0];
			if (objClass != null) {
				objFields = objClass.getDeclaredFields();
			}
			fieldMap= new HashMap<>();
			for (Field field:objFields){
				CnaFieldEncrypt annotation = field.getAnnotation(CnaFieldEncrypt.class);
				if(annotation!=null  && CnaObjectUtil.notObjectClass(field.getType())) {
					fieldMap.put(field,annotation);
				}
			}
			if(ObjectUtils.isNotEmpty(fieldMap)){
				ENTITY_FIELD_CACHE.put(objClass,fieldMap);
			}
		}else{
			fieldMap=ENTITY_FIELD_CACHE.get(objClass);
		}
		return fieldMap;
	}

}