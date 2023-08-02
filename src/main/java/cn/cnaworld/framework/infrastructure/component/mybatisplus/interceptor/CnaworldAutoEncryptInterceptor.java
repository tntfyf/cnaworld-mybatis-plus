package cn.cnaworld.framework.infrastructure.component.mybatisplus.interceptor;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.annotation.CnaFieldEncrypt;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.EncryptAlgorithmProcessor;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.impl.AESEncryptProcessor;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
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

	@Override
	public Object intercept(Invocation invocation) throws Throwable {

		final Object[] args = invocation.getArgs();
		MappedStatement ms = (MappedStatement) args[0];
		if (SqlCommandType.SELECT == ms.getSqlCommandType()) {
			//执行并获取返回结果
			Object proceed = invocation.proceed();
			if(!CnaObjectUtil.isObject(proceed)){
				return proceed;
			}
			Map<Field, CnaFieldEncrypt> fieldMap = getEntityFieldCache(proceed);
			if(proceed instanceof List){
				//讲对象转换成list
				List<?> resultList = (List<?>) proceed;
				if(ObjectUtils.isNotEmpty(resultList)){
					//将结果集中所有需要自动解密的字段进行遍历解密
					resultList.forEach(result-> {
						if (fieldMap != null) {
							fieldMap.forEach((field, annotation) -> encrypt(result, field, annotation, "2"));
						}
					});
				}
			}else {
				if (fieldMap != null) {
					fieldMap.forEach((field,annotation) -> encrypt(proceed, field, annotation,"2"));
				}
			}
			return proceed;
		} else if (SqlCommandType.UPDATE == ms.getSqlCommandType() || SqlCommandType.INSERT == ms.getSqlCommandType()) {
			Object parameterObject = args[1];
			if(!CnaObjectUtil.isObject(parameterObject)){
				return invocation.proceed();
			}
			Map<Field, CnaFieldEncrypt> fieldMap = getEntityFieldCache(parameterObject);
			if(parameterObject instanceof List){
				List<?> parameterList = (List<?>) parameterObject;
				if(ObjectUtils.isNotEmpty(parameterList)){
					parameterList.forEach(parameter-> {
						if (fieldMap != null) {
							fieldMap.forEach((field, annotation) -> encrypt(parameter, field, annotation, "1"));
						}
					});
				}
			}
		}
		//判断是select语句
		// 返回，继续执行
		return invocation.proceed();
	}

	private void encrypt(Object entity, Field field, CnaFieldEncrypt annotation,String encryptType) {
		try {
			field.setAccessible(true);
			Object value = field.get(entity);
			String content = null;
			if (ObjectUtils.isNotEmpty(value)){
				String key = null;
				EncryptAlgorithmProcessor encryptAlgorithmProcessor=null;
				EncryptAlgorithm algorithm = annotation.encryptAlgorithm();
				//如果没有定义过算法，则采用默认的AES算法实现
				if(algorithm.equals(EncryptAlgorithm.NONE)){
					if(cnaworldMybatisPlusProperties.getFieldEncrypt().getAlgorithm().equals(EncryptAlgorithm.AES)){
						encryptAlgorithmProcessor= new AESEncryptProcessor();
					}
				}
				//判断是使用全局密钥还是字段密钥
				if(StringUtils.isNotBlank(annotation.key())){
					key = annotation.key();
				}else if (StringUtils.isNotBlank(cnaworldMybatisPlusProperties.getFieldEncrypt().getKey())){
					key = cnaworldMybatisPlusProperties.getFieldEncrypt().getKey();
				}else {
					content="加密解密失败，密钥未配置";
					CnaLogUtil.error(log,"field:{},加密解密失败，密钥未配置", field.getDeclaringClass()+"."+ field.getName());
				}
				//算法和密钥都没问题，则进行解密，并将数据回写到原始结果集中
				if(StringUtils.isNotBlank(key)){
					if (encryptAlgorithmProcessor != null) {
						if ("1".equals(encryptType)){
							//加密
							content = encryptAlgorithmProcessor.encrypt(String.valueOf(value),key);
						}else {
							//解密
							content = encryptAlgorithmProcessor.decrypt(String.valueOf(value),key);
						}
					}
				}
				if (!StringUtils.isBlank(content)){
					field.set(entity, content);
				}
			}
			field.setAccessible(false);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
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
				objClass = objList.get(0).getClass();
			}
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
				if(annotation!=null){
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