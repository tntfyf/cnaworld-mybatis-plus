package cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.booleanprocessor.impl;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.booleanprocessor.BooleanEncryptAlgorithmProcessor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

/**
 * 自动加密算法处理器
 * @author Lucifer
 * @date 2023/3/7
 * @since 1.0.0
 */
@Slf4j
public class BooleanEncryptProcessor extends BooleanEncryptAlgorithmProcessor {

    /**
     * Date加密处理器
     *
     * @param data Date 明文
     * @param keys String 密钥
     * @return Date 密文
     * @author Lucifer
     * @date 2023/7/10
     * @since 1.1.3
     */
    @Override
    public Boolean encrypt(Boolean data, String...keys) {
        return getaBoolean(data, keys);
    }

    /**
     * Date解密处理器
     *
     * @param data  Date 密文
     * @param keys  String 密钥
     * @return Date 泛型
     * @author Lucifer
     * @date 2023/7/10
     * @since 1.1.3
     */
    @Override
    public Boolean decrypt(Boolean data,String...keys) {
        return getaBoolean(data, keys);
    }

    @Nullable
    private Boolean getaBoolean(Boolean data, String[] keys) {
        if(!check(data,keys)){
            return null;
        }
        int myInt = data ? 0 : 1;
        int i = myInt ^ getHashKey(keys);
        return i % 2 == 0;
    }

}
