package cn.cnaworld.framework.infrastructure.component.mybatisplus.handler.impl;

import org.apache.ibatis.reflection.MetaObject;

/**
 * 填充值处理器
 * @author Lucifer
 * @date 2023/3/7
 * @since 1.0.0
 */
public interface FieldProcessor {

    /**
     * 填充值处理器
     * @author Lucifer
     * @date 2023/3/7
     * @since 1.0.0
     * @param fieldName String
     * @param metaObject MetaObject
     * @return Object
     */
    Object getFieldValue(String fieldName,MetaObject metaObject);

}
