package cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.delete;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.AbstractCustMethod;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.statics.enums.CustomizedSqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
/**
 * 直接删除
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 */
public class DirectDeleteBatchByIds extends AbstractCustMethod {

	private static final long serialVersionUID = 1L;

	public DirectDeleteBatchByIds(String methodName) {
		super(methodName);
	}

	@Override
	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {

		String sql;
		CustomizedSqlMethod mySqlMethod = CustomizedSqlMethod.DIRECT_DELETE_BATCH_BY_IDS;
		sql = String.format(mySqlMethod.getSql(),
				tableInfo.getTableName(),
				tableInfo.getKeyColumn(),
				SqlScriptUtils.convertForeach(
						SqlScriptUtils.convertChoose(
								"@org.apache.ibatis.type.SimpleTypeRegistry@isSimpleType(item.getClass())",
								"#{item}",
								"#{item." + tableInfo.getKeyProperty() + "}"),
								"coll", null,
								"item", ","));
		SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, Object.class);

		return addDeleteMappedStatement(mapperClass, mySqlMethod.getMethod(), sqlSource);
	}

}
