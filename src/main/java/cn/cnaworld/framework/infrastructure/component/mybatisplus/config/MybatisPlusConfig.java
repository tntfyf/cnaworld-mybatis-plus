package cn.cnaworld.framework.infrastructure.component.mybatisplus.config;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.handler.CustomizedMetaObjectHandler;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.CustomizedSqlInjector;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.interceptor.CnaWorldInnerInterceptor;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.snowflake.CustomerIdGenerator;
import cn.cnaworld.framework.infrastructure.utils.CnaLogUtil;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;

/**
 * cnaworld-mybatis-plus 自动装配
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 */
@Slf4j
@ConditionalOnExpression("#{environment['cnaworld.mybatis-plus.enabled'] ==null || !environment['cnaworld.mybatis-plus.enabled'].contains('false')}")
public class MybatisPlusConfig {

    @Bean
    @ConditionalOnExpression("#{environment['cnaworld.mybatis-plus.function-extension'] ==null || !environment['cnaworld.mybatis-plus.function-extension'].contains('false')}")
    public CustomizedSqlInjector customizedSqlInjector() {
        CnaLogUtil.info(log,"cnaworld mybatis-plus extend method initialized ！");
        return new CustomizedSqlInjector();
    }

    @Bean
    @ConditionalOnExpression("#{environment['cnaworld.mybatis-plus.optimistic-locker'] ==null || !environment['cnaworld.mybatis-plus.optimistic-locker'].contains('false')}")
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor(true));
        CnaLogUtil.info(log,"cnaworld mybatis-plus optimistic-locker initialized ！");
        return mybatisPlusInterceptor;
    }

    @Bean
    @ConditionalOnExpression("#{environment['cnaworld.mybatis-plus.snow-flake'] ==null || !environment['cnaworld.mybatis-plus.snow-flake'].contains('false')}")
    public CustomerIdGenerator customerIdGenerator() {
        CnaLogUtil.info(log,"cnaworld mybatis-plus 16-snowflake initialized ！");
        return new CustomerIdGenerator();
    }

    @Bean
    @ConditionalOnExpression("#{environment['cnaworld.mybatis-plus.auto-insert-fill'] == null || !environment['cnaworld.mybatis-plus.auto-insert-fill'].contains('false')}")
    public CustomizedMetaObjectHandler customizedMetaObjectHandler() {
        CnaLogUtil.info(log,"cnaworld mybatis-plus auto-insert-fill initialized ！");
        return new CustomizedMetaObjectHandler();
    }

    @Bean
    @ConditionalOnExpression("#{environment['cnaworld.mybatis-plus.update-optimistic-locker-field'] ==null || !environment['cnaworld.mybatis-plus.update-optimistic-locker-field'].contains('false')}")
    public CnaWorldInnerInterceptor cnaWorldInnerInterceptor() {
        CnaLogUtil.info(log,"cnaworld mybatis-plus update-optimistic-locker-field initialized ！");
        return new CnaWorldInnerInterceptor();
    }

}
