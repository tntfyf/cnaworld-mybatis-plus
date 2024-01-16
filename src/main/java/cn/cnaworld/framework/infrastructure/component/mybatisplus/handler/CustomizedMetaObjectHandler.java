package cn.cnaworld.framework.infrastructure.component.mybatisplus.handler;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.handler.impl.FieldProcessor;
import cn.cnaworld.framework.infrastructure.properties.CnaworldMybatisPlusProperties;
import cn.cnaworld.framework.infrastructure.utils.log.CnaLogUtil;
import cn.cnaworld.framework.infrastructure.utils.object.CnaCheckUtil;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自动填充处理类
 * @author Lucifer
 * @version 1.0
 **/
@Slf4j
public class CustomizedMetaObjectHandler implements MetaObjectHandler {

    @Autowired
    private CnaworldMybatisPlusProperties cnaworldMybatisPlusProperties;

    private static final Map<String,CnaworldMybatisPlusProperties.FillStrategyField> FIELD_CACHE = new HashMap<>();

    private static final Map<String, Map<String,String>> CLASS_NAME_CACHE = new HashMap<>();

    private static final Map<String, Map<String,String>> EXCLUDE_CLASS_NAME_CACHE = new HashMap<>();

    private static final Map<Class<?>, FieldProcessor> PROCESSOR_OBJECT_CACHE = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        List<CnaworldMybatisPlusProperties.FillStrategyField> fillStrategyField = cnaworldMybatisPlusProperties.getFillStrategyField();
        if (ObjectUtils.isNotEmpty(fillStrategyField)){
            fillStrategyField.forEach(t->{
                if(CnaCheckUtil.isNotNull(t.getFieldFullName())){
                    FIELD_CACHE.put(t.getFieldFullName(),t);
                }else if(CnaCheckUtil.isNotNull(t.getRangeFill()) && CnaCheckUtil.isNotNull(t.getRangeFill().getFieldName())) {
                    FIELD_CACHE.put(t.getRangeFill().getFieldName(),t);
                    if(CnaCheckUtil.isNotNull(t.getRangeFill().getIncludeClass())){
                        Map<String,String> includeClass = new HashMap<>();
                        t.getRangeFill().getIncludeClass().forEach(s-> includeClass.put(s,s));
                        CLASS_NAME_CACHE.put(t.getRangeFill().getFieldName(),includeClass);
                    }
                    if(CnaCheckUtil.isNotNull(t.getRangeFill().getExcludeClass())){
                        Map<String,String> excludeClass = new HashMap<>();
                        t.getRangeFill().getExcludeClass().forEach(s-> excludeClass.put(s,s));
                        EXCLUDE_CLASS_NAME_CACHE.put(t.getRangeFill().getFieldName(),excludeClass);
                    }
                }
            });
        }
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        try {
            //在执行mybatisPlus的insert()时，为我们自动给某些字段填充值，这样的话，我们就不需要手动给insert()里的实体类赋值了
            //插入是表级别的，一个字段配置了插入的注解，整个拦截器就会生效，所以其实无需给每个字段都配置注解。
            List<Field> fieldList = getFieldList(metaObject);
            String className = metaObject.getOriginalObject().getClass().getName();
            if(CnaCheckUtil.isNotNull(fieldList)){
                for(Field field: fieldList){
                    if(CnaCheckUtil.isNotObjectClass(field.getType())){
                        String name = field.getName();
                        String classAllName = className +"."+ name;
                        //先匹配全限定名称
                        if(FIELD_CACHE.containsKey(classAllName)){
                            fillValueByName(metaObject, name, classAllName);
                        } else if (FIELD_CACHE.containsKey(name)) {
                            //若匹配不到则再匹配名称
                            CnaworldMybatisPlusProperties.FillStrategyField fillStrategyField = getFillStrategyField(className, name);
                            if (fillStrategyField == null) {
                                continue;
                            }
                            if (CnaCheckUtil.isNotNull(fillStrategyField.getFillType())
                                    && (fillStrategyField.getFillType().equals(FieldFill.INSERT)
                                    || fillStrategyField.getFillType().equals(FieldFill.INSERT_UPDATE))) {
                                Object value = getValue(metaObject, fillStrategyField);
                                if (ObjectUtils.isNotEmpty(value)) {
                                    this.fillStrategy(metaObject,name,value);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            CnaLogUtil.error(log,"cnaworld mybatis-plus auto-insert-fill error : {}",e.getMessage(),e);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            //在执行mybatisPlus的insert()时，为我们自动给某些字段填充值，这样的话，我们就不需要手动给insert()里的实体类赋值了
            //插入是表级别的，一个字段配置了插入的注解，整个拦截器就会生效，所以其实无需给每个字段都配置注解。
            List<Field> fieldList = getFieldList(metaObject);
            String className = metaObject.getOriginalObject().getClass().getName();
            if(CnaCheckUtil.isNotNull(fieldList)){
                for(Field field:fieldList){
                    if(CnaCheckUtil.isNotObjectClass(field.getType())){
                        String name = field.getName();
                        String classAllName = className + name;
                        //先匹配全限定名称
                        if(FIELD_CACHE.containsKey(classAllName)){
                            fillValueByName(metaObject, name, classAllName);
                        } else if (FIELD_CACHE.containsKey(name)) {
                            //若匹配不到则再匹配名称
                            CnaworldMybatisPlusProperties.FillStrategyField fillStrategyField = getFillStrategyField(className, name);
                            if (fillStrategyField == null) {
                                continue;
                            }
                            if (CnaCheckUtil.isNotNull(fillStrategyField.getFillType())
                                    && (fillStrategyField.getFillType().equals(FieldFill.UPDATE)
                                    || fillStrategyField.getFillType().equals(FieldFill.INSERT_UPDATE))) {
                                Object value = getValue(metaObject, fillStrategyField);
                                if (ObjectUtils.isNotEmpty(value)) {
                                    this.fillStrategy(metaObject,name,value);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            CnaLogUtil.error(log,"cnaworld mybatis-plus auto-insert-fill error : {}",e.getMessage(),e);
        }
    }

    @Nullable
    private static CnaworldMybatisPlusProperties.FillStrategyField getFillStrategyField(String className, String name) {
        CnaworldMybatisPlusProperties.FillStrategyField fillStrategyField = FIELD_CACHE.get(name);
        if (EXCLUDE_CLASS_NAME_CACHE.containsKey(name)) {
            //若拒绝中包含则不填充
            Map<String,String> excludeClassAllName = EXCLUDE_CLASS_NAME_CACHE.get(name);
            if(excludeClassAllName.containsKey(className)){
                return null;
            }
        }
        if (CLASS_NAME_CACHE.containsKey(name)) {
            //若拒绝中包含则不填充
            Map<String,String> classAllNameTmp = CLASS_NAME_CACHE.get(name);
            if(!classAllNameTmp.containsKey(className)){
                return null;
            }
        }
        return fillStrategyField;
    }

    private void fillValueByName(MetaObject metaObject, String name, String classAllName) {
        CnaworldMybatisPlusProperties.FillStrategyField fillStrategyField = FIELD_CACHE.get(classAllName);
        if (CnaCheckUtil.isNotNull(fillStrategyField.getFillType())
                && (fillStrategyField.getFillType().equals(FieldFill.INSERT)
                || fillStrategyField.getFillType().equals(FieldFill.INSERT_UPDATE))) {
            Object value = getValue(metaObject, fillStrategyField);
            if (ObjectUtils.isNotEmpty(value)) {
                this.fillStrategy(metaObject,name,value);
            }
        }
    }

    @NotNull
    private static List<Field> getFieldList(MetaObject metaObject) {
        List<Field> fieldList = new ArrayList<>();
        Class<?> tempClass = metaObject.getOriginalObject().getClass();
        while (tempClass != null) {//当父类为null的时候说明到达了最上层的父类(Object类).
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass(); //得到父类,然后赋给自己
        }
        return fieldList;
    }

    @Nullable
    private Object getValue(MetaObject metaObject, CnaworldMybatisPlusProperties.FillStrategyField t) {
        Object value = null;
        if (ObjectUtils.isNotEmpty(t.getFieldValue())) {
            value = t.getFieldValue();
        }else if (t.getFieldProcessorClass()!=null) {
            String fileName = "";
            if(CnaCheckUtil.isNotNull(t.getFieldFullName())){
                fileName=t.getFieldFullName();
            }else if(CnaCheckUtil.isNotNull(t.getRangeFill())
                    && CnaCheckUtil.isNotNull(t.getRangeFill().getFieldName())){
                fileName=t.getRangeFill().getFieldName();
            }
            value = getFieldValueProcessor(fileName, t.getFieldProcessorClass(), metaObject);
        }else if (t.getFieldClass()!=null){
            value = getFieldValue(t.getFieldClass());
        }
        return value;
    }

    protected Object getFieldValue(Class<?> clazz) {
        if (!Long.TYPE.equals(clazz) && !Long.class.equals(clazz)) {
            if (!Integer.TYPE.equals(clazz) && !Integer.class.equals(clazz)) {
                if (Date.class.equals(clazz)) {
                    return new Date();
                } else if (Timestamp.class.equals(clazz)) {
                    return new Timestamp(System.currentTimeMillis());
                } else {
                    return LocalDateTime.class.equals(clazz) ? LocalDateTime.now() : null;
                }
            } else {
                return 0;
            }
        } else {
            return 0L;
        }
    }

    private Object getFieldValueProcessor(String fieldName , Class<?> fieldProcessorClass,MetaObject metaObject) {
        FieldProcessor fieldProcessor;
        if (PROCESSOR_OBJECT_CACHE.containsKey(fieldProcessorClass)){
            fieldProcessor = PROCESSOR_OBJECT_CACHE.get(fieldProcessorClass);
        }else {
            Object obj;
            try {
                obj = fieldProcessorClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                CnaLogUtil.error(log,"cnaworld aop auto-insert-fill-processor-class 解析失败 ：{}" , fieldProcessorClass);
                return null;
            }
            if (!fieldProcessorClass.isInstance(obj)) {
                CnaLogUtil.error(log,"cnaworld aop auto-insert-fill-processor-class 解析失败 ：{}" , fieldProcessorClass);
                return null;
            }
            fieldProcessor = (FieldProcessor) obj;
            PROCESSOR_OBJECT_CACHE.put(fieldProcessorClass,fieldProcessor);
        }
        return fieldProcessor.getFieldValue(fieldName , metaObject);
    }

}