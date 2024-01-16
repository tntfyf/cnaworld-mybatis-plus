package cn.cnaworld.framework.infrastructure.component.mybatisplus.handler.vo;

import cn.cnaworld.framework.infrastructure.properties.CnaworldMybatisPlusProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 获取值Vo
 * @author Lucifer
 * @date 2024/1/16
 * @since 1.1.9
 */
@Getter
@Setter
@ToString
public class FillValueVo {

    private CnaworldMybatisPlusProperties.FillStrategyField fillStrategyField;

    private String classFullName;

    private String name;


}
