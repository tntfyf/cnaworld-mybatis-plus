package cn.cnaworld.framework.infrastructure.component.mybatisplus.baseclass.service;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.baseclass.mapper.CnaworldBaseMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * 基础service
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 */
public interface CnaworldBaseService<T> extends IService<T> {

	/**
	 * 根据 entity 条件，恢复记录
	 * @author Lucifer
	 * @date 2023/3/5
	 * @since 1.0.0
	 * @param queryWrapper 实体对象封装操作类（可以为 null,里面的 entity 用于生成 where 语句）
	 * @return boolean
	 */
	default boolean restore(Wrapper<T> queryWrapper) {
		return SqlHelper.retBool(((CnaworldBaseMapper<T>)getBaseMapper()).recover(queryWrapper));
	}

	/**
	 * 根据 ID 恢复
	 * @author Lucifer
	 * @date 2023/3/5
	 * @since 1.0.0
	 * @param id 主键
	 * @return boolean
	 */
	default boolean restoreById(Serializable id) {
		return SqlHelper.retBool(((CnaworldBaseMapper<T>) getBaseMapper()).recoverById(id));
	}

	/**
	 * 根据 columnMap 条件，恢复记录
	 * @author Lucifer
	 * @date 2023/3/5
	 * @since 1.0.0
	 * @param columnMap 表字段 map 对象
	 * @return boolean
	 */
	default boolean restoreByMap(Map<String, Object> columnMap) {
		 Assert.notEmpty(columnMap, "error: columnMap must not be empty");
	     return SqlHelper.retBool(((CnaworldBaseMapper<T>) getBaseMapper()).recoverByMap(columnMap));
	}
	
	/**
	 * 恢复（根据ID 批量恢复）
	 * @author Lucifer
	 * @date 2023/3/5
	 * @since 1.0.0
	 * @param idList idList 主键ID列表(不能为 null 以及 empty)
	 * @return boolean
	 */
	default boolean restoreByIds(Collection<? extends Serializable> idList) {
		if (CollectionUtils.isEmpty(idList)) {
            return false;
        }
        return SqlHelper.retBool(((CnaworldBaseMapper<T>)getBaseMapper()).recoverBatchIds(idList));
	}

	/**
	 * 根据 entity 条件，直接删除记录
	 * @author Lucifer
	 * @date 2023/3/5
	 * @since 1.0.0
	 * @param queryWrapper 实体对象封装操作类（可以为 null,里面的 entity 用于生成 where 语句）
	 * @return boolean
	 */
	default boolean directRemove(Wrapper<T> queryWrapper) {
		return SqlHelper.retBool(((CnaworldBaseMapper<T>)getBaseMapper()).directDelete(queryWrapper));
	}

	/**
	 * 根据 ID 直接删除
	 * @author Lucifer
	 * @date 2023/3/5
	 * @since 1.0.0
	 * @param id 主键ID
	 * @return boolean
	 */
	default boolean directRemoveById(Serializable id) {
		return SqlHelper.retBool(((CnaworldBaseMapper<T>) getBaseMapper()).directDeleteById(id));
	}

	/**
	 * 根据 columnMap 条件，直接删除记录
	 * @author Lucifer
	 * @date 2023/3/5
	 * @since 1.0.0
	 * @param columnMap 表字段 map 对象
	 * @return boolean
	 */
	default boolean directRemoveByMap(Map<String, Object> columnMap) {
		Assert.notEmpty(columnMap, "error: columnMap must not be empty");
		return SqlHelper.retBool(((CnaworldBaseMapper<T>) getBaseMapper()).directDeleteByMap(columnMap));
	}

	/**
	 * 根据IDS 批量直接删除
	 * @author Lucifer
	 * @date 2023/3/5
	 * @since 1.0.0
	 * @param idList 主键ID列表(不能为 null 以及 empty)
	 * @return boolean
	 */
	default boolean directRemoveByIds(Collection<? extends Serializable> idList) {
		if (CollectionUtils.isEmpty(idList)) {
			return false;
		}
		return SqlHelper.retBool(((CnaworldBaseMapper<T>)getBaseMapper()).directDeleteBatchIds(idList));
	}

}
