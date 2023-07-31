package cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.impl;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.EncryptAlgorithmProcessor;
import cn.cnaworld.framework.infrastructure.utils.encryption.CnaAesUtil;

/**
 * 填充值处理器
 * @author Lucifer
 * @date 2023/3/7
 * @since 1.0.0
 */
public class AESEncryptProcessor implements EncryptAlgorithmProcessor {


    /**
     * 加密处理器
     *
     * @param keys  String 密钥
     * @param data Object 明文
     * @return String 密文
     * @author Lucifer
     * @date 2023/7/10
     * @since 1.1.3
     */
    @Override
    public String encrypt(Object data,String...keys) {
        return CnaAesUtil.encrypt(keys[0],String.valueOf(data));
    }

    /**
     * 解密处理器
     *
     * @param keys  String 密钥
     * @param data Object 明文
     * @return String 密文
     * @author Lucifer
     * @date 2023/7/10
     * @since 1.1.3
     */
    @Override
    public String decrypt(Object data,String...keys) {
        return CnaAesUtil.decrypt(keys[0],String.valueOf(data));
    }
}
