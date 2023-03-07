package cn.cnaworld.framework.infrastructure.component.mybatisplus.baseclass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基本实体类
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
public class CnaWorldBaseEntity implements Serializable {

    @ApiModelProperty(value = "逻辑删除")
    @TableField(value = "deleted_db",fill = FieldFill.INSERT)
    @TableLogic
    private Boolean deletedDb;

    @ApiModelProperty(value = "创建者")
    @TableField("create_by_db")
    private String createByDb;

    @ApiModelProperty(value = "更新者")
    @TableField("update_by_db")
    private String updateByDb;

    @ApiModelProperty(value = "创建日期")
    @TableField(value = "create_time_db",fill = FieldFill.INSERT)
    private LocalDateTime createTimeDb;

    @ApiModelProperty(value = "更新日期")
    @TableField(value = "update_time_db",fill = FieldFill.INSERT)
    @Version
    private LocalDateTime updateTimeDb;

}
