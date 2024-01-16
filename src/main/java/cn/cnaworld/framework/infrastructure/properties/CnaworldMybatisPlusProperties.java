package cn.cnaworld.framework.infrastructure.properties;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.EncryptAlgorithmProcessor;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.statics.enums.EncryptAlgorithm;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * cnaworld-mybatis-plus 属性配置类
 * @author Lucifer
 * @date 2023/1/30
 * @since 1.0
 */
@ConfigurationProperties(prefix="cnaworld.mybatis-plus")
@Getter
@Setter
@ToString
public class CnaworldMybatisPlusProperties {

    /**
     * cnaworld-mybatis-plus 总开关
     */
    private boolean enabled = true;

    /**
     * 开启16位雪花ID
     */
    private boolean snowFlake = true;

    /**
     * insert时，自动获取 fillStrategyField 中的属性进行填充 ，填充值采用类型的初始化默认值
     */
    private boolean autoInsertFill = true;

    /**
     * 乐观锁支持 OptimisticLockerInnerInterceptor , wrapperMode 为 true
     */
    private boolean optimisticLocker = true;

    /**
     * 逻辑删除扩展方法注入，directDelete 系列和 recover 系列
     */
    private boolean functionExtension = true;

    /**
     * 对注解字段自动加密解密
     */
    private boolean autoFieldEncrypt = true;

    /**
     * 自动填充 insert时，自动获取遍历属性进行填充 ，填充值采用类型的初始化默认值
     */
    private List<FillStrategyField> fillStrategyField;

    /**
     * 自动加密解密
     */
    private FieldEncrypt fieldEncrypt;


    /**
     * 属性实体
     * @author Lucifer
     * @date 2023/1/30
     * @since 1.0
     */
    @Getter
    @Setter
    @ToString
    public static class FillStrategyField {

        /**
         * 填充字段全限定名称
         */
        private String fieldFullName;

        /**
         * 范围填充
         */
        private RangeFill rangeFill;

        /**
         * 填充值
         */
        private Object fieldValue;

        /**
         * 填充策略
         */
        private FieldFill fillType = FieldFill.DEFAULT;

        /**
         * 填充类型
         */
        private Class<?> fieldClass;

        /**
         * 填充值实现
         */
        private Class<?> fieldProcessorClass;

        /**
         * 范围填充
         * @author Lucifer
         * @date 2023/1/30
         * @since 1.0
         */
        @Getter
        @Setter
        @ToString
        public static class RangeFill {

            /**
             * 填充字段（批量填充）
             * 若fieldAllName不为空，则使用全限定名称，精准填充
             * 并且includeClass、excludeClass失效。
             */
            private List<String> fieldNames;

            /**
             * 包含要填充的类全限定名称
             * 若不配置则默认全局填充
             * 若配置了则仅在这些类中应用
             * 若没配置但是配置了excludeClassName，则除excludeClassName全局填充
             */
            private List<Class<?>> includeClass;

            /**
             * 不包含要填充的类全限定名称
             */
            private List<Class<?>> excludeClass;
        }

    }




    /**
     * 自动加密
     * @author Lucifer
     * @date 2023/1/30
     * @since 1.0
     */
    @Getter
    @Setter
    @ToString
    public static class FieldEncrypt {

        /**
         * 加密密钥
         */
        private String[] keys;

        /**
         * 加密算法
         */
        private EncryptAlgorithm algorithm;

        /**
         * 自定义处理器
         */
        private Class<? extends EncryptAlgorithmProcessor> encryptAlgorithmProcessor;

    }

}
