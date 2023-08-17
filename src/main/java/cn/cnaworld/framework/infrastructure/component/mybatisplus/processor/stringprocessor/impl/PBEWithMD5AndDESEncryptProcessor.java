package cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.stringprocessor.impl;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.stringprocessor.StringEncryptAlgorithmProcessor;

/**
 * 自动加密算法处理器
 * @author Lucifer
 * @date 2023/3/7
 * @since 1.0.0
 */
public class PBEWithMD5AndDESEncryptProcessor extends StringEncryptAlgorithmProcessor {


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
    public String encrypt(String data,String...keys) {
        return null;
    }

    /**
     * 解密处理器
     *
     * @param data  String 密文
     * @param keys  String 密钥
     * @return T 泛型
     * @author Lucifer
     * @date 2023/7/10
     * @since 1.1.3
     */
    @Override
    public String decrypt(String data,String...keys) {
        return null;
    }
}
