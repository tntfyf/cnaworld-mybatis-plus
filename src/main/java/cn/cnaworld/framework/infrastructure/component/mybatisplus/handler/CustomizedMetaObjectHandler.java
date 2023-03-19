package cn.cnaworld.framework.infrastructure.component.mybatisplus.handler;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.handler.impl.FieldProcessor;
import cn.cnaworld.framework.infrastructure.properties.CnaworldMybatisPlusProperties;
import cn.cnaworld.framework.infrastructure.utils.CnaLogUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 自动填充处理类
 * @author Lucifer
 * @version 1.0
 **/
@Slf4j
public class CustomizedMetaObjectHandler implements MetaObjectHandler {

    @Autowired
    private CnaworldMybatisPlusProperties cnaworldMybatisPlusProperties;

    @Override
    public void insertFill(MetaObject metaObject) {

        try {
            //在执行mybatisPlus的insert()时，为我们自动给某些字段填充值，这样的话，我们就不需要手动给insert()里的实体类赋值了
            //需要搭配数据库规范
            if (cnaworldMybatisPlusProperties != null) {
                List<CnaworldMybatisPlusProperties.FillStrategyField> fillStrategyField = cnaworldMybatisPlusProperties.getFillStrategyField();
                if (ObjectUtils.isNotEmpty(fillStrategyField)){
                    fillStrategyField.forEach(t->{
                        if(StringUtils.isNotBlank(t.getFieldName())){
                            Object value = null;
                            if (ObjectUtils.isNotEmpty(t.getFieldValue())) {
                                value = t.getFieldValue();
                            }else if (t.getFieldProcessorClass()!=null) {
                                value = getFieldValueProcessor(t.getFieldName(),t.getFieldProcessorClass(),metaObject);
                            }else if (t.getFieldClass()!=null){
                                value = getFieldValue(t.getFieldClass());
                            }
                            if (ObjectUtils.isNotEmpty(value)) {
                                this.fillStrategy(metaObject,t.getFieldName(),value);
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            CnaLogUtil.error(log,"cnaworld mybatis-plus auto-insert-fill error : {}",e.getMessage(),e);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
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
        fieldProcessor =(FieldProcessor) obj;

        return fieldProcessor.getFieldValue(fieldName , metaObject);
    }

}