package cn.cnaworld.framework.infrastructure.component.mybatisplus.statics.enums;

import lombok.Getter;
/**
 *
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 */
@Getter
public enum CustomizedSqlMethod {

	/**
     * 插入
     */
    RECOVER("recover", "根据 entity 条件恢复记录", "<script>\nUPDATE %s %s %s %s\n</script>"),
    RECOVER_BY_ID("recoverById", "根据ID 恢复一条数据", "<script>\nUPDATE %s %s WHERE %s=#{%s} %s\n</script>"),
	RECOVER_BATCH_BY_IDS("recoverBatchIds", "根据ID集合，批量恢复数据", "<script>\nUPDATE %s %s WHERE %s IN (%s) %s\n</script>"),
    RECOVER_BY_MAP("recoverByMap", "根据columnMap 条件恢复记录", "<script>\nUPDATE %s %s %s\n</script>"),

    DIRECT_DELETE("directDelete", "根据 entity 条件删除记录", "<script>\nDELETE FROM %s %s %s\n</script>"),
    DIRECT_DELETE_BY_ID("directDeleteById", "根据ID 删除一条数据", "<script>\nDELETE FROM %s WHERE %s=#{%s} \n</script>"),
    DIRECT_DELETE_BATCH_BY_IDS("directDeleteBatchIds", "根据ID集合，批量删除数据", "<script>\nDELETE FROM %s WHERE %s IN (%s)\n</script>"),
    DIRECT_DELETE_BY_MAP("directDeleteByMap", "根据columnMap 条件删除记录", "<script>\nDELETE FROM %s %s\n</script>");

    private final String method;
    private final String desc;
    private final String sql;
    
    CustomizedSqlMethod(String method, String desc, String sql) {
        this.method = method;
        this.desc = desc;
        this.sql = sql;
    }
   
}
