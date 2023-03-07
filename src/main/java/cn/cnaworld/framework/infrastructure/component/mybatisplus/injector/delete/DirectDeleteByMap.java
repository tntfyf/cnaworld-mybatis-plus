package cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.delete;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.AbstractCustMethod;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.statics.enums.CustomizedSqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.Map;

/**
 * 直接删除
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 */
public class DirectDeleteByMap extends AbstractCustMethod {

	private static final long serialVersionUID = 1L;

	public DirectDeleteByMap(String methodName) {
		super(methodName);
	}

	@Override
	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {

		String sql;
		CustomizedSqlMethod mySqlMethod = CustomizedSqlMethod.DIRECT_DELETE_BY_MAP;
		//逻辑删除
		sql = String.format(mySqlMethod.getSql(),
				tableInfo.getTableName(),
				sqlReWhereByMap(tableInfo,true));
		SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, Map.class);

		return addDeleteMappedStatement(mapperClass, mySqlMethod.getMethod(), sqlSource);
	}

}
