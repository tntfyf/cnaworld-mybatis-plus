package cn.cnaworld.framework.infrastructure.component.mybatisplus.interceptor;

import com.baomidou.mybatisplus.core.mapper.Mapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 解决逻辑删除时，不自动修改updateTime以及version问题
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 */
@Intercepts({
		//type指定代理的是那个对象，method指定代理Executor中的那个方法,args指定Executor中的query方法都有哪些参数对象
		//由于Executor中有两个query，因此需要两个@Signature
		@Signature(type = Executor.class,
				method = "update",
				args = { MappedStatement.class, Object.class })//需要代理的对象和方法
})
public class CnaWorldInnerInterceptor implements Interceptor{

	private static final Map<String, Class<?>> ENTITY_CLASS_CACHE = new ConcurrentHashMap<>();

	@Override
	public Object intercept(Invocation invocation) throws Throwable {

		final Object[] args = invocation.getArgs();
		MappedStatement ms = (MappedStatement) args[0];
		Object parameterObject = args[1];
		if (SqlCommandType.UPDATE == ms.getSqlCommandType()) {
			String msId = ms.getId();
			Class<?> entityClass = ENTITY_CLASS_CACHE.get(msId);
			if (null == entityClass) {
				try {
					String className = msId.substring(0, msId.lastIndexOf(46));
					entityClass  = ReflectionKit.getSuperClassGenericType(Class.forName(className), Mapper.class, 0);
					ENTITY_CLASS_CACHE.put(msId, entityClass);
				} catch (ClassNotFoundException var11) {
					throw ExceptionUtils.mpe(var11);
				}
			}
			TableFieldInfo versionField = this.getVersionFieldInfo(entityClass);
			if (null == versionField) {
				return invocation.proceed();
			}

			String versionColumn = versionField.getColumn();
			Class<?> propertyType = versionField.getPropertyType();

			BoundSql boundSql = ms.getBoundSql(parameterObject);
			String sql = boundSql.getSql();
			if (sql.contains(versionColumn + "=?")){
				return invocation.proceed();
			}else {
				String replaceStr = getUpdatedVersionVal(propertyType, versionColumn);
				sql=sql.replace("SET", replaceStr);
				// 包装sql后，重置到invocation中
				resetSql2Invocation(invocation, sql);
				// 返回，继续执行
				return invocation.proceed();
			}
		}

		// 返回，继续执行
		return invocation.proceed();
	}

	/**
	 * 包装sql后，重置到invocation中
	 * @author Lucifer
	 * @date 2023/3/6
	 * @since 1.0.0
	 */
	private void resetSql2Invocation(Invocation invocation, String sql){
		final Object[] args = invocation.getArgs();
		MappedStatement statement = (MappedStatement) args[0];
		Object parameterObject = args[1];
		BoundSql boundSql = statement.getBoundSql(parameterObject);
		MappedStatement newStatement = newMappedStatement(statement, new BoundSqlSqlSource(boundSql));
		MetaObject msObject =  MetaObject.forObject(newStatement, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(),new DefaultReflectorFactory());
		msObject.setValue("sqlSource.boundSql.sql", sql);
		args[0] = newStatement;
	}
	
	private MappedStatement newMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
		MappedStatement.Builder builder =
				new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
		builder.resource(ms.getResource());
		builder.fetchSize(ms.getFetchSize());
		builder.statementType(ms.getStatementType());
		builder.keyGenerator(ms.getKeyGenerator());
		if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
			StringBuilder keyProperties = new StringBuilder();
			for (String keyProperty : ms.getKeyProperties()) {
				keyProperties.append(keyProperty).append(",");
			}
			keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
			builder.keyProperty(keyProperties.toString());
		}
		builder.timeout(ms.getTimeout());
		builder.parameterMap(ms.getParameterMap());
		builder.resultMaps(ms.getResultMaps());
		builder.resultSetType(ms.getResultSetType());
		builder.cache(ms.getCache());
		builder.flushCacheRequired(ms.isFlushCacheRequired());
		builder.useCache(ms.isUseCache());
		
		return builder.build();
	}
	
	/**
	 * 定义一个内部辅助类，作用是包装sq
	 * @author Lucifer
	 * @date 2023/3/6
	 * @since 1.0.0
	 */
	static class BoundSqlSqlSource implements SqlSource {
		private final BoundSql boundSql;
		public BoundSqlSqlSource(BoundSql boundSql) {
			this.boundSql = boundSql;
		}
		@Override
		public BoundSql getBoundSql(Object parameterObject) {
			return boundSql;
		}
	}

	protected TableFieldInfo getVersionFieldInfo(Class<?> entityClazz) {
		TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClazz);
		return null != tableInfo && tableInfo.isWithVersion() ? tableInfo.getVersionFieldInfo() : null;
	}

	protected String getUpdatedVersionVal(Class<?> clazz,String versionColumn) {

		StringBuffer replaceStrBuf= new StringBuffer("SET ");
		replaceStrBuf.append(versionColumn)
				.append("=");
		if (!Long.TYPE.equals(clazz) && !Long.class.equals(clazz)) {
			if (!Integer.TYPE.equals(clazz) && !Integer.class.equals(clazz)) {
				replaceStrBuf.append("'");
				if (Date.class.equals(clazz)) {
					replaceStrBuf.append(new Date());
				} else if (Timestamp.class.equals(clazz)) {
					replaceStrBuf.append(new Timestamp(System.currentTimeMillis()));
				} else {
					replaceStrBuf.append(LocalDateTime.now());
				}
				replaceStrBuf.append("',");
			} else {
				replaceStrBuf.append(versionColumn).append("+1").append(",");
			}
		} else {
			replaceStrBuf.append(versionColumn).append("+1").append(",");
		}

		return replaceStrBuf.toString();
	}

}