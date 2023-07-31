package cn.cnaworld.framework.infrastructure.component.mybatisplus.processor;

/**
 * 填充值处理器
 * @author Lucifer
 * @date 2023/3/7
 * @since 1.0.0
 */
public interface EncryptAlgorithmProcessor {

    /**
     * 加密处理器
     * @author Lucifer
     * @date 2023/7/10
     * @since 1.1.3
     * @param keys String 密钥
     * @param data Object 明文
     * @return String 密文
     */
    String encrypt(Object data,String...keys);

    /**
     * 解密处理器
     * @author Lucifer
     * @date 2023/7/10
     * @since 1.1.3
     * @param keys String 密钥
     * @param data Object 明文
     * @return String 密文
     */
    String decrypt(Object data,String...keys);

}
