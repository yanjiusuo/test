package com.jd.workflow.impl;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/23
 */

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.util.JdbcConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jd.matrix.sdk.annotation.Extension;
import com.jd.workflow.DataTransformApp;
import com.jd.workflow.common.JsonType;
import com.jd.workflow.common.ObjectJsonType;
import com.jd.workflow.domain.ModelContentParseModel;
import com.jd.workflow.matrix.ext.matrix1.ModelContentParseExt;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/23 
 */
@Slf4j
@Extension(code = DataTransformApp.CODE)
public class SqlModelContentParseExtImpl implements ModelContentParseExt {
    /**
     * 解析字符串
     *
     * @param modelContentParseModel
     * @return
     */
    @Override
    public ObjectJsonType parseContent(ModelContentParseModel modelContentParseModel) {
        log.info("ModelContentParseSPI  parseContent :{}", modelContentParseModel.getContent());
        List<JsonType> jsonTypeList = Lists.newArrayList();
        Set<String> intSet = Sets.newHashSet("INT", "TINYINT");
        Set<String> longSet = Sets.newHashSet("BIGINT");
        Set<String> doubleSet = Sets.newHashSet("DECIMAL");
        ObjectJsonType tableObj = new ObjectJsonType();
        String dbType = JdbcConstants.MYSQL;
        try {
            List<SQLStatement> statementList = SQLUtils.parseStatements(modelContentParseModel.getContent(), dbType);
            for (SQLStatement sqlStatement : statementList) {
                if (sqlStatement instanceof MySqlCreateTableStatement) {
                    MySqlCreateTableStatement mySqlCreateTableStatement = (MySqlCreateTableStatement) sqlStatement;
                    if (mySqlCreateTableStatement.getTableSource().getExpr() instanceof SQLPropertyExpr) {
                        tableObj.setName(((SQLPropertyExpr) mySqlCreateTableStatement.getTableSource().getExpr()).getName().replaceAll("`", ""));
                        if (Objects.nonNull(mySqlCreateTableStatement.getComment())) {
                            if (mySqlCreateTableStatement.getComment() instanceof SQLCharExpr) {
                                tableObj.setDesc(((SQLCharExpr) mySqlCreateTableStatement.getComment()).getText().replaceAll("`", ""));
                            }
                        }
                    } else if (mySqlCreateTableStatement.getTableSource().getExpr() instanceof SQLIdentifierExpr) {
                        tableObj.setName(((SQLIdentifierExpr) mySqlCreateTableStatement.getTableSource().getExpr()).getName());
                        if (Objects.nonNull(mySqlCreateTableStatement.getComment())) {
                            if (mySqlCreateTableStatement.getComment() instanceof SQLCharExpr) {
                                tableObj.setDesc(((SQLCharExpr) mySqlCreateTableStatement.getComment()).getText().replaceAll("`", ""));
                            }
                        }
                    }

                    List<SQLColumnDefinition> sqlColumnDefinitionList = parseSQLColumnDefs(mySqlCreateTableStatement);
                    for (SQLColumnDefinition sqlColumnDefinition : sqlColumnDefinitionList) {
                        log.info("dataType:{}", sqlColumnDefinition.getDataType().getName().toUpperCase());
                        if (intSet.contains(sqlColumnDefinition.getDataType().getName().toUpperCase())) {
                            jsonTypeList.add(createJsonType(sqlColumnDefinition.getName().getSimpleName().replaceAll("`", ""), "integer", Objects.nonNull(sqlColumnDefinition.getComment()) ? sqlColumnDefinition.getComment().toString().replaceAll("'", "") : ""));
                        } else if (longSet.contains(sqlColumnDefinition.getDataType().getName().toUpperCase())) {
                            jsonTypeList.add(createJsonType(sqlColumnDefinition.getName().getSimpleName().replaceAll("`", ""), "long", Objects.nonNull(sqlColumnDefinition.getComment()) ? sqlColumnDefinition.getComment().toString().replaceAll("'", "") : ""));
                        } else if (doubleSet.contains(sqlColumnDefinition.getDataType().getName().toUpperCase())) {
                            jsonTypeList.add(createJsonType(sqlColumnDefinition.getName().getSimpleName().replaceAll("`", ""), "double", Objects.nonNull(sqlColumnDefinition.getComment()) ? sqlColumnDefinition.getComment().toString().replaceAll("'", "") : ""));
                        } else {
                            jsonTypeList.add(createJsonType(sqlColumnDefinition.getName().getSimpleName().replaceAll("`", ""), "string", Objects.nonNull(sqlColumnDefinition.getComment()) ? sqlColumnDefinition.getComment().toString().replaceAll("'", "") : ""));
                        }
                    }
                    tableObj.setChildren(jsonTypeList);

                    return tableObj;
                }
            }
        } catch (Exception ex) {
            log.error("parseSql error", ex);
        }
        tableObj.setChildren(jsonTypeList);
        return tableObj;
    }


    private JsonType createJsonType(String name, String type, String comment) {
        JsonType simpleJsonType = new JsonType();
        simpleJsonType.setType(type);
        simpleJsonType.setName(name);
        simpleJsonType.setDesc(comment);
        simpleJsonType.setClassName(type);
        return simpleJsonType;
    }

    private List<SQLColumnDefinition> parseSQLColumnDefs(MySqlCreateTableStatement createTable) {
        return createTable.getTableElementList().stream()
                .filter(i -> i instanceof SQLColumnDefinition)
                .map(i -> (SQLColumnDefinition) i)
                .collect(Collectors.toList());
    }
}
