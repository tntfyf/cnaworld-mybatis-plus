package cn.cnaworld.framework.infrastructure.component.mybatisplus.baseclass.mapper;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * 基础Mapper
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 * 根Mapper，给表Mapper继承用的，可以自定义通用方法
 * {@link com.baomidou.mybatisplus.core.mapper.BaseMapper}
 * {@link com.baomidou.mybatisplus.extension.service.IService}
 * {@link com.baomidou.mybatisplus.extension.service.impl.ServiceImpl}
 */
public interface CnaWorldBaseMapper<T> extends BaseMapper<T> {

    /**
     * 根据 entity 条件，恢复记录
     * @author Lucifer
     * @date 2023/3/6
     * @since 1.0.0
     * @param queryWrapper 实体对象封装操作类（可以为 null,里面的 entity 用于生成 where 语句）
     * @return 操作数
     */
    int recover(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 根据 ID 恢复
     * @author Lucifer
     * @date 2023/3/6
     * @since 1.0.0
     * @param id 主键ID
     * @return 操作数
     */
    int recoverById(Serializable id);

    /**
     * 根据 columnMap 条件，恢复记录
     * @author Lucifer
     * @date 2023/3/6
     * @since 1.0.0
     * @param columnMap 表字段 map 对象
     * @return 操作数
     */
    int recoverByMap(@Param(Constants.COLUMN_MAP) Map<String, Object> columnMap);

    /**
     * 根据ID列表 批量恢复
     * @author Lucifer
     * @date 2023/3/6
     * @since 1.0.0
     * @param idList 主键ID列表(不能为 null 以及 empty)
     * @return 操作数
     */
    int recoverBatchIds(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList);

    /**
     * 根据 entity 条件，删除记录
     * @author Lucifer
     * @date 2023/3/6
     * @since 1.0.0
     * @param queryWrapper 实体对象封装操作类（可以为 null,里面的 entity 用于生成 where 语句）
     * @return 操作数
     */
    int directDelete(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 根据 ID 直接删除
     * @author Lucifer
     * @date 2023/3/6
     * @since 1.0.0
     * @param id 主键ID
     * @return 操作数
     */
    int directDeleteById(Serializable id);

    /**
     * 根据 columnMap 条件，删除记录
     * @author Lucifer
     * @date 2023/3/6
     * @since 1.0.0
     * @param columnMap 表字段 map 对象
     * @return 操作数
     */
    int directDeleteByMap(@Param(Constants.COLUMN_MAP) Map<String, Object> columnMap);

    /**
     * 根据ID列表 批量直接删除
     * @author Lucifer
     * @date 2023/3/6
     * @since 1.0.0
     * @param idList 主键ID列表(不能为 null 以及 empty)
     * @return 操作数
     */
    int directDeleteBatchIds(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList);

}
