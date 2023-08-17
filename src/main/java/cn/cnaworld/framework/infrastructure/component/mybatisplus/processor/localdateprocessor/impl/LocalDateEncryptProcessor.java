package cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.localdateprocessor.impl;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.processor.localdateprocessor.LocalDateEncryptAlgorithmProcessor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;

/**
 * 自动加密算法处理器
 * @author Lucifer
 * @date 2023/3/7
 * @since 1.0.0
 */
@Slf4j
public class LocalDateEncryptProcessor extends LocalDateEncryptAlgorithmProcessor {

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
    public Temporal encrypt(Temporal data, String...keys) {
        return getTemporal(data, keys);
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
    public Temporal decrypt(Temporal data,String...keys) {
        return getTemporal(data, keys);
    }

    @Nullable
    private Temporal getTemporal(Temporal data, String[] keys) {
        if(!check(data,keys)){
            return null;
        }
        if(data instanceof LocalDate){
            long timestamp = ((LocalDate) data).atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli() ^ getHashKey(keys);
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.of("+8")).toLocalDate();
        } else if (data instanceof LocalDateTime) {
            long timestamp = ((LocalDateTime) data).toInstant(ZoneOffset.of("+8")).toEpochMilli() ^ getHashKey(keys);
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.of("+8"));
        }
        return null;
    }

}
