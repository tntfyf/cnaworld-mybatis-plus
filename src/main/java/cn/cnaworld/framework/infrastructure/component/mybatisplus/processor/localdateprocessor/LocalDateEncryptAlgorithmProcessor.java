package cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.localdateprocessor;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.EncryptAlgorithmProcessor;

import java.time.temporal.Temporal;

/**
 * 自动加密算法处理器
 * @author Lucifer
 * @date 2023/3/7
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public abstract class LocalDateEncryptAlgorithmProcessor implements EncryptAlgorithmProcessor {

    /**
     * String加密处理器
     *
     * @param data T 明文
     * @param keys String 密钥
     * @return String 密文
     * @author Lucifer
     * @date 2023/7/10
     * @since 1.1.3
     */
    public abstract Temporal encrypt(Temporal data, String... keys);

    /**
     * String解密处理器
     *
     * @param data String 密文
     * @param keys String 密钥
     * @return T 泛型
     * @author Lucifer
     * @date 2023/7/10
     * @since 1.1.3
     */
    public abstract Temporal decrypt(Temporal data, String... keys);

    /**
     * 加密处理器
     *
     * @param data T 明文
     * @param keys String 密钥
     * @return String 密文
     * @author Lucifer
     * @date 2023/7/10
     * @since 1.1.3
     */
    @Override
    public <T> T encrypt(T data, String... keys) {
        return (T) encrypt((Temporal)data,keys);
    }

    /**
     * 解密处理器
     *
     * @param data String 密文
     * @param keys String 密钥
     * @return T 泛型
     * @author Lucifer
     * @date 2023/7/10
     * @since 1.1.3
     */
    @Override
    public <T> T decrypt(T data, String... keys) {
        return (T) decrypt((Temporal)data,keys);
    }

}
