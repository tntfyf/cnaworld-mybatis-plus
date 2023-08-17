package cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.numberprocessor.impl;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.numberprocessor.NumberEncryptAlgorithmProcessor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Cipher;
import java.math.BigDecimal;

/**
 * 自动加密算法处理器
 * @author Lucifer
 * @date 2023/3/7
 * @since 1.0.0
 */
@Slf4j
public class NumberEncryptProcessor extends NumberEncryptAlgorithmProcessor {


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
    public Number encrypt(Number data,String...keys) {
        return getNumber(data, keys,Cipher.ENCRYPT_MODE);
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
    public Number decrypt(Number data,String...keys) {
        return getNumber(data, keys,Cipher.DECRYPT_MODE);
    }

    @Nullable
    private Number getNumber(Number data, String[] keys,int encryptType) {
        if(!check(data,keys)){
            return null;
        }
        if(data instanceof Integer){
            return (Integer) data ^ getHashKey(keys);
        } else if (data instanceof Long) {
            return (Long) data ^ getHashKey(keys);
        }else if (data instanceof Short){
            return Short.parseShort(String.valueOf((Short) data ^ Short.parseShort(String.valueOf(getHashKey(keys)).substring(0, 4))));
        }else if (data instanceof Byte){
            return Byte.parseByte(String.valueOf((Byte) data ^ Byte.parseByte(String.valueOf(getHashKey(keys)).substring(0, 2))));
        } else if (data instanceof Float){
            return Float.intBitsToFloat(Float.floatToIntBits((Float) data) ^ Integer.parseInt(String.valueOf(getHashKey(keys)).substring(0, 3)));
        } else if (data instanceof Double){
            return Double.longBitsToDouble(Double.doubleToLongBits((Double) data) ^ getHashKey(keys));
        }else if (data instanceof BigDecimal){
            if(Cipher.ENCRYPT_MODE == encryptType){
                return ((BigDecimal) data).add(new BigDecimal(String.valueOf(getHashKey(keys))));
            }else{
                return ((BigDecimal) data).subtract(new BigDecimal(String.valueOf(getHashKey(keys))));
            }
        }
        return null;
    }


}
