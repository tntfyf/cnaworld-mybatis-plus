package cn.cnaworld.framework.infrastructure.component.mybatisplus.snowflake;

import cn.cnaworld.framework.infrastructure.utils.code.CnaCodeUtil;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

/**
 * 重写默认雪花ID实现
 * @author Lucifer
 * @date 2023/3/6
 * @since 1.0.0
 */
public class CustomerIdGenerator implements IdentifierGenerator {

    @Override
    public Long nextId(Object entity) {
        // 填充自己的Id生成器，
        return CnaCodeUtil.getSnowflakeId();
    }

}
