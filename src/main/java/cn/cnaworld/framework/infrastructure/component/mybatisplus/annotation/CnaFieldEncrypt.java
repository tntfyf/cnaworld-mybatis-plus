package cn.cnaworld.framework.infrastructure.component.mybatisplus.annotation;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.EncryptAlgorithmProcessor;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.statics.enums.EncryptAlgorithm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 仓储懒加载注解
 * @author Lucifer
 * @date 2023/6/20
 * @since 1.0.5
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CnaFieldEncrypt {

    String key() default "";

    EncryptAlgorithm encryptAlgorithm() default EncryptAlgorithm.NONE;

    /**
     * 自定义加密逻辑处理
     */
    Class<? extends EncryptAlgorithmProcessor> encryptAlgorithmProcessor() default EncryptAlgorithmProcessor.class;

}

