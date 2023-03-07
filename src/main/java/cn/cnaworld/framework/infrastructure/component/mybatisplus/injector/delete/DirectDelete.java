package cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.delete;

import cn.cnaworld.framework.infrastructure.component.mybatisplus.injector.AbstractCustMethod;
import cn.cnaworld.framework.infrastructure.component.mybatisplus.statics.enums.CustomizedSqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * 直接删除
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 */
public class DirectDelete extends AbstractCustMethod {

	private static final long serialVersionUID = 1L;

	public DirectDelete(String methodName) {
		super(methodName);
	}

	@Override
	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
		
		String sql;
		CustomizedSqlMethod mySqlMethod = CustomizedSqlMethod.DIRECT_DELETE;
		//去掉逻辑删除逻辑
		sql = String.format(mySqlMethod.getSql(), 
				tableInfo.getTableName(),
				sqlReWhereEntityWrapper(true, tableInfo,true),
				sqlComment());
		SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);

		return addDeleteMappedStatement(mapperClass, mySqlMethod.getMethod(), sqlSource);
	}

}
