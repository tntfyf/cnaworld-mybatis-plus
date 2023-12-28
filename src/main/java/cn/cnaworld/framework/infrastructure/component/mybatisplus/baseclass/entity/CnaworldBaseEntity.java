package cn.cnaworld.framework.infrastructure.component.mybatisplus.baseclass.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "CnaWorldBaseEntity对象")
public class CnaworldBaseEntity implements Serializable {

    /**
     * 逻辑删除
     */
    @Schema(description = "逻辑删除")
    @TableField(value = "deleted_db",fill = FieldFill.INSERT)
    @TableLogic
    private Boolean deletedDb;

    /**
     * 创建者
     */
    @Schema(description = "创建者")
    @TableField(value = "create_by_db",fill = FieldFill.INSERT)
    private String createByDb;

    /**
     * 更新者
     */
    @Schema(description = "更新者")
    @TableField(value = "update_by_db",fill = FieldFill.INSERT_UPDATE)
    private String updateByDb;

    /**
     * 创建日期
     */
    @Schema(description = "创建日期")
    @TableField(value = "create_time_db",fill = FieldFill.INSERT)
    private LocalDateTime createTimeDb;

    /**
     * 更新日期
     */
    @Schema(description = "更新日期")
    @TableField(value = "update_time_db",fill = FieldFill.INSERT_UPDATE)
    @Version
    private LocalDateTime updateTimeDb;

}
