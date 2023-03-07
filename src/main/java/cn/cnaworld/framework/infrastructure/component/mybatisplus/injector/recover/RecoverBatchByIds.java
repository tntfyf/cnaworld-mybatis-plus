package cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.recover;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.AbstractCustMethod;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.statics.enums.CustomizedSqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
/**
 * 逻辑删除恢复
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 */
public class RecoverBatchByIds extends AbstractCustMethod {

	private static final long serialVersionUID = 1L;

	public RecoverBatchByIds(String methodName) {
		super(methodName);
	}

	@Override
	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {

		CustomizedSqlMethod mySqlMethod = CustomizedSqlMethod.RECOVER_BATCH_BY_IDS;
		if (!tableInfo.isWithLogicDelete()) {
			SqlSource sqlSource = languageDriver.createSqlSource(configuration, "cnaworld mybatis-plus entity : " + tableInfo.getTableName() +" , missing logic fields , restoreByIds or recoverBatchIds method cannot be used", modelClass);
			return addUpdateMappedStatement(mapperClass, modelClass, mySqlMethod.getMethod(), sqlSource);
		}
		String sql;
		sql = String.format(mySqlMethod.getSql(),
				tableInfo.getTableName(),
				sqlReLogicSet(tableInfo),
				tableInfo.getKeyColumn(),
				SqlScriptUtils.convertForeach(
						SqlScriptUtils.convertChoose(
								"@org.apache.ibatis.type.SimpleTypeRegistry@isSimpleType(item.getClass())",
								"#{item}",
								"#{item." + tableInfo.getKeyProperty() + "}"),
								"coll", null,
								"item", ","),
				getReLogicDeleteSql(true, true,tableInfo));
		SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, Object.class);

		return addUpdateMappedStatement(mapperClass, modelClass, mySqlMethod.getMethod(), sqlSource);
	}

}
