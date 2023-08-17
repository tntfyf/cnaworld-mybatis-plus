package cn.cnaworld.framework.infrastructure.component.mybatisplus.processor;

import org.apache.commons.lang3.ObjectUtils;

/**
 * 自动加密算法处理器
 * @author Lucifer
 * @date 2023/3/7
 * @since 1.0.0
 */
public interface EncryptAlgorithmProcessor {

    /**
     * 加密处理器
     *
     * @param keys String 密钥
     * @param data T 明文
     * @return String 密文
     * @author Lucifer
     * @date 2023/7/10
     * @since 1.1.3
     */
    <T> T encrypt(T data, String...keys);

    /**
     * 解密处理器
     * @author Lucifer
     * @date 2023/7/10
     * @since 1.1.3
     * @param data String 密文
     * @param keys String 密钥
     * @return T 泛型
     */
    <T> T decrypt(T data,String...keys);

    default <T> boolean check(T data,String...keys) {
        return  ObjectUtils.isNotEmpty(keys) && ObjectUtils.isNotEmpty(data) ;
    }
    default int getHashKey(String[] keys) {
        return  keys[0].hashCode() & Integer.MAX_VALUE;
    }

}
