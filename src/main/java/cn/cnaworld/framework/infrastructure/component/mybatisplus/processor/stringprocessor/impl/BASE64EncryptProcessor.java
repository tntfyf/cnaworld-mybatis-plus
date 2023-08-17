package cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.stringprocessor.impl;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.stringprocessor.StringEncryptAlgorithmProcessor;
import cn.cnaworld.framework.infrastructure.utils.code.CnaCodeParseUtil;
import cn.cnaworld.framework.infrastructure.utils.log.CnaLogUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 自动加密算法处理器
 * @author Lucifer
 * @date 2023/3/7
 * @since 1.0.0
 */
@Slf4j
public class BASE64EncryptProcessor extends StringEncryptAlgorithmProcessor {

    /**
     * BASE64加密处理器
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
            return CnaCodeParseUtil.base64Encode(CnaCodeParseUtil.toByteArray(data));
        } catch (Exception e) {
            CnaLogUtil.error(log,"BASE64自动加密失败" + e.getMessage() , e);
        }
        return null;
    }

    /**
     * BASE64解密处理器
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
            byte[] bytes = CnaCodeParseUtil.base64Decode(data);
            return data.getClass().cast(bytes);
        } catch (Exception e) {
            CnaLogUtil.error(log,"base64解码异常");
        }
        return null;
    }

}
