package cn.cnaworld.framework.infrastructure.config;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.config.MybatisPlusConfig;
import cn.cnaworld.framework.infrastructure.properties.CnaworldMybatisPlusProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 自动装配类
 * @author Lucifer
 * @date 2023/2/10
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties({CnaworldMybatisPlusProperties.class})
@Import(value = {MybatisPlusConfig.class})
public class CnaMybatisPlusAutoWiredConfig {}