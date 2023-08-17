package cn.cnaworld.framework.infrastructure.component.mybatisplus.statics.enums;

import lombok.Getter;

/**
 *
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 */
@Getter
public enum EncryptAlgorithm {
    NONE,
    MD5_32,
    MD5_16,
    BASE64,
    AES,
    RSA,
    SM2,
    SM3,
    SM4,
    PBEWithMD5AndDES,
    PBEWithMD5AndTripleDES,
    PBEWithHMACSHA512AndAES_256,
    PBEWithSHA1AndDESede,
    PBEWithSHA1AndRC2_40
}
