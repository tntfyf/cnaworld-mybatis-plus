package cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.recover;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.AbstractCustMethod;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.statics.enums.CustomizedSqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
/**
 * 逻辑删除恢复
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 */
public class Recover extends AbstractCustMethod {

	private static final long serialVersionUID = 1L;

	public Recover(String methodName) {
		super(methodName);
	}

	@Override
	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {

		CustomizedSqlMethod mySqlMethod = CustomizedSqlMethod.RECOVER;
		if (!tableInfo.isWithLogicDelete()) {
			SqlSource sqlSource = languageDriver.createSqlSource(configuration, "cnaworld mybatis-plus entity : " + tableInfo.getTableName() +" , missing logic fields , restore or recover method cannot be used", modelClass);
			return addUpdateMappedStatement(mapperClass, modelClass, mySqlMethod.getMethod(), sqlSource);
		}
		String sql;
		sql = String.format(mySqlMethod.getSql(),
				tableInfo.getTableName(),
				sqlReLogicSet(tableInfo),
				sqlReWhereEntityWrapper(true, tableInfo,false),
				sqlComment());
		SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);

		return addUpdateMappedStatement(mapperClass, modelClass, mySqlMethod.getMethod(), sqlSource);
	}

}
