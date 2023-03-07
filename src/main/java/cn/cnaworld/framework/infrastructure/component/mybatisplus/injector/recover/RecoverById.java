package cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.recover;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.AbstractCustMethod;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.statics.enums.CustomizedSqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 逻辑删除恢复
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 */
public class RecoverById extends AbstractCustMethod {

	private static final long serialVersionUID = 1L;

	public RecoverById(String methodName) {
		super(methodName);
	}

	@Override
	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {

		CustomizedSqlMethod mySqlMethod = CustomizedSqlMethod.RECOVER_BY_ID;
		if (!tableInfo.isWithLogicDelete()) {
			SqlSource sqlSource = languageDriver.createSqlSource(configuration, "cnaworld mybatis-plus entity : " + tableInfo.getTableName() +" , missing logic fields , restoreById or recoverById method cannot be used", modelClass);
			return addUpdateMappedStatement(mapperClass, modelClass, mySqlMethod.getMethod(), sqlSource);
		}
		String sql;
		List<TableFieldInfo> fieldInfos = (List)tableInfo.getFieldList().stream().filter(TableFieldInfo::isWithUpdateFill).filter((f) -> !f.isLogicDelete()).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(fieldInfos)) {
			String sqlSet = "SET " + SqlScriptUtils.convertIf(fieldInfos.stream().map((i) -> i.getSqlSet("")).collect(Collectors.joining("")), "!@org.apache.ibatis.type.SimpleTypeRegistry@isSimpleType(_parameter.getClass())", true) + getReLogicDeleteSql(false, false,tableInfo);
			sql = String.format(mySqlMethod.getSql(),
					tableInfo.getTableName(),
					sqlSet,
					tableInfo.getKeyColumn(),
					tableInfo.getKeyProperty(),
					getReLogicDeleteSql(true, true,tableInfo));
		} else {
			sql = String.format(mySqlMethod.getSql(),
					tableInfo.getTableName(),
					sqlReLogicSet(tableInfo),
					tableInfo.getKeyColumn(),
					tableInfo.getKeyProperty(),
					getReLogicDeleteSql(true, true,tableInfo));
		}
		SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, Object.class);

        return addUpdateMappedStatement(mapperClass, modelClass, mySqlMethod.getMethod(), sqlSource);
	}

}
