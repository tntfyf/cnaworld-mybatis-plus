package cn.cnaworld.framework.infrastructure.component.mybatisplus.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;

/**
 * @author Lucifer
 * @date 2023/3/5
 * @since 1.0.0
 */
public abstract class AbstractCustMethod extends AbstractMethod {

    private static final long serialVersionUID = 1L;

    public AbstractCustMethod(String methodName) {
        super(methodName);
    }

    protected String sqlReLogicSet(TableInfo table) {
        return "SET " + getReLogicDeleteSql(false, false,table);
    }

    protected String getReLogicDeleteSql(boolean startWithAnd, boolean isWhere,TableInfo table) {
        if (table.isWithLogicDelete()) {
            String reLogicDeleteSql = this.formatReLogicDeleteSql(isWhere,table);
            if (startWithAnd) {
                reLogicDeleteSql = " AND " + reLogicDeleteSql;
            }

            return reLogicDeleteSql;
        } else {
            return "";
        }
    }

    protected String formatReLogicDeleteSql(boolean isWhere,TableInfo table) {
        String value = isWhere ? table.getLogicDeleteFieldInfo().getLogicDeleteValue() : table.getLogicDeleteFieldInfo().getLogicNotDeleteValue();
        if (isWhere) {
            return "null".equalsIgnoreCase(value) ? table.getLogicDeleteFieldInfo().getColumn() + " IS NULL" : table.getLogicDeleteFieldInfo().getColumn() + "=" + String.format(table.getLogicDeleteFieldInfo().isCharSequence() ? "'%s'" : "%s", value);
        } else {
            String targetStr = table.getLogicDeleteFieldInfo().getColumn() + "=";
            return "null".equalsIgnoreCase(value) ? targetStr + "null" : targetStr + String.format(table.getLogicDeleteFieldInfo().isCharSequence() ? "'%s'" : "%s", value);
        }
    }

    protected String sqlReWhereByMap(TableInfo table,boolean directDelete) {
        String sqlScript;
        if (table.isWithLogicDelete() && !directDelete) {
            sqlScript = SqlScriptUtils.convertChoose("v == null", " ${k} IS NULL ", " ${k} = #{v} ");
            sqlScript = SqlScriptUtils.convertForeach(sqlScript, "cm", "k", "v", "AND");
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null and !%s.isEmpty", "cm", "cm"), true);
            sqlScript = sqlScript + "\n" + getReLogicDeleteSql(true, true,table);
            sqlScript = SqlScriptUtils.convertWhere(sqlScript);
            return sqlScript;
        } else {
            sqlScript = SqlScriptUtils.convertChoose("v == null", " ${k} IS NULL ", " ${k} = #{v} ");
            sqlScript = SqlScriptUtils.convertForeach(sqlScript, "cm", "k", "v", "AND");
            sqlScript = SqlScriptUtils.convertWhere(sqlScript);
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null and !%s", "cm", "cm.isEmpty"), true);
            return sqlScript;
        }
    }

    protected String sqlReWhereEntityWrapper(boolean newLine, TableInfo table,boolean directDelete) {
        String sqlScript;
        if (table.isWithLogicDelete() && !directDelete) {
            sqlScript = table.getAllSqlWhere(true, true, "ew.entity.");
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", "ew.entity"), true);
            sqlScript = sqlScript + "\n" + getReLogicDeleteSql(true, true,table) + "\n";
            String normalSqlScript = SqlScriptUtils.convertIf(String.format("AND ${%s}", "ew.sqlSegment"), String.format("%s != null and %s != '' and %s", "ew.sqlSegment", "ew.sqlSegment", "ew.nonEmptyOfNormal"), true);
            normalSqlScript = normalSqlScript + "\n";
            normalSqlScript = normalSqlScript + SqlScriptUtils.convertIf(String.format(" ${%s}", "ew.sqlSegment"), String.format("%s != null and %s != '' and %s", "ew.sqlSegment", "ew.sqlSegment", "ew.emptyOfNormal"), true);
            sqlScript = sqlScript + normalSqlScript;
            sqlScript = SqlScriptUtils.convertChoose(String.format("%s != null", "ew"), sqlScript, getReLogicDeleteSql(false, true,table));
            sqlScript = SqlScriptUtils.convertWhere(sqlScript);
        } else {
            sqlScript = table.getAllSqlWhere(false, true, "ew.entity.");
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", "ew.entity"), true);
            sqlScript = sqlScript + "\n";
            sqlScript = sqlScript + SqlScriptUtils.convertIf(String.format(SqlScriptUtils.convertIf(" AND", String.format("%s and %s", "ew.nonEmptyOfEntity", "ew.nonEmptyOfNormal"), false) + " ${%s}", "ew.sqlSegment"), String.format("%s != null and %s != '' and %s", "ew.sqlSegment", "ew.sqlSegment", "ew.nonEmptyOfWhere"), true);
            sqlScript = SqlScriptUtils.convertWhere(sqlScript) + "\n";
            sqlScript = sqlScript + SqlScriptUtils.convertIf(String.format(" ${%s}", "ew.sqlSegment"), String.format("%s != null and %s != '' and %s", "ew.sqlSegment", "ew.sqlSegment", "ew.emptyOfWhere"), true);
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", "ew"), true);
        }
        return newLine ? "\n" + sqlScript : sqlScript;
    }

}
