package cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.dateprocessor.impl;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.dateprocessor.DateEncryptAlgorithmProcessor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * 自动加密算法处理器
 * @author Lucifer
 * @date 2023/3/7
 * @since 1.0.0
 */
@Slf4j
public class DateEncryptProcessor extends DateEncryptAlgorithmProcessor {

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
    public Date encrypt(Date data, String...keys) {
        return getDate(data, keys);
    }

    @Nullable
    private Date getDate(Date data, String[] keys) {
        if(!check(data, keys)){
            return null;
        }
        return new Date(data.getTime() ^ getHashKey(keys));
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
    public Date decrypt(Date data,String...keys) {
        return getDate(data, keys);
    }

}
