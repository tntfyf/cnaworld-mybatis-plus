package cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.stringprocessor.impl;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.stringprocessor.StringEncryptAlgorithmProcessor;
import cn.cnaworld.framework.infrastructure.utils.encryption.CnaAesUtil;
import cn.cnaworld.framework.infrastructure.utils.log.CnaLogUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 自动加密算法处理器
 * @author Lucifer
 * @date 2023/3/7
 * @since 1.0.0
 */
@Slf4j
public class AESEncryptProcessor extends StringEncryptAlgorithmProcessor {

    /**
     * AES加密处理器
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
        if(!check(data,keys)){
            return null;
        }
        try {
            return CnaAesUtil.encrypt(keys[0], data);
        } catch (Exception e) {
            CnaLogUtil.error(log,"AES自动加密失败" + e.getMessage() , e);
            return null;
        }
    }

    /**
     * AES解密处理器
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
        if(!check(data,keys)){
            return null;
        }
        try {
            return CnaAesUtil.decrypt(keys[0],data);
        } catch (Exception e) {
            CnaLogUtil.error(log,"AES自动解密失败" + e.getMessage() , e);
            return null;
        }
    }

}
