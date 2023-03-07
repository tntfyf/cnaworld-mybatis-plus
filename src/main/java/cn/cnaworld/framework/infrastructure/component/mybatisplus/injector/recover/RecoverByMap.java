package cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.recover;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.AbstractCustMethod;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.statics.enums.CustomizedSqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.Map;
/**
 * 逻辑删除恢复
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 */
public class RecoverByMap extends AbstractCustMethod {

	private static final long serialVersionUID = 1L;

	public RecoverByMap(String methodName) {
		super(methodName);
	}

	@Override
	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {


		CustomizedSqlMethod mySqlMethod = CustomizedSqlMethod.RECOVER_BY_MAP;
		if (!tableInfo.isWithLogicDelete()) {
			SqlSource sqlSource = languageDriver.createSqlSource(configuration, "cnaworld mybatis-plus entity : " + tableInfo.getTableName() +" , missing logic fields , restoreByMap or recoverByMap method cannot be used", modelClass);
			return addUpdateMappedStatement(mapperClass, modelClass, mySqlMethod.getMethod(), sqlSource);
		}
		String sql;
		sql = String.format(mySqlMethod.getSql(),
				tableInfo.getTableName(), 
				sqlReLogicSet(tableInfo),
				sqlReWhereByMap(tableInfo,false));
		SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, Map.class);

		return addUpdateMappedStatement(mapperClass, Map.class, mySqlMethod.getMethod(), sqlSource);
	}

}
