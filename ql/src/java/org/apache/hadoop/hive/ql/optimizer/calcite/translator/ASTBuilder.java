begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|optimizer
operator|.
name|calcite
operator|.
name|translator
package|;
end_package

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|adapter
operator|.
name|druid
operator|.
name|DruidQuery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|RelNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|core
operator|.
name|JoinRelType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rex
operator|.
name|RexLiteral
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|type
operator|.
name|SqlTypeName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|util
operator|.
name|DateString
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|util
operator|.
name|TimeString
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|util
operator|.
name|TimestampString
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|type
operator|.
name|HiveIntervalDayTime
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|type
operator|.
name|HiveIntervalYearMonth
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|conf
operator|.
name|Constants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|optimizer
operator|.
name|calcite
operator|.
name|RelOptHiveTable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveTableScan
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|jdbc
operator|.
name|HiveJdbcConverter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|parse
operator|.
name|ASTNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|parse
operator|.
name|BaseSemanticAnalyzer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|parse
operator|.
name|HiveParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|parse
operator|.
name|ParseDriver
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|parse
operator|.
name|SemanticAnalyzer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
specifier|public
class|class
name|ASTBuilder
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOGGER
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ASTBuilder
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|ASTBuilder
name|construct
parameter_list|(
name|int
name|tokenType
parameter_list|,
name|String
name|text
parameter_list|)
block|{
name|ASTBuilder
name|b
init|=
operator|new
name|ASTBuilder
argument_list|()
decl_stmt|;
name|b
operator|.
name|curr
operator|=
name|createAST
argument_list|(
name|tokenType
argument_list|,
name|text
argument_list|)
expr_stmt|;
return|return
name|b
return|;
block|}
specifier|public
specifier|static
name|ASTNode
name|createAST
parameter_list|(
name|int
name|tokenType
parameter_list|,
name|String
name|text
parameter_list|)
block|{
return|return
operator|(
name|ASTNode
operator|)
name|ParseDriver
operator|.
name|adaptor
operator|.
name|create
argument_list|(
name|tokenType
argument_list|,
name|text
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ASTNode
name|destNode
parameter_list|()
block|{
return|return
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_DESTINATION
argument_list|,
literal|"TOK_DESTINATION"
argument_list|)
operator|.
name|add
argument_list|(
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_DIR
argument_list|,
literal|"TOK_DIR"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|TOK_TMP_FILE
argument_list|,
literal|"TOK_TMP_FILE"
argument_list|)
argument_list|)
operator|.
name|node
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|ASTNode
name|table
parameter_list|(
specifier|final
name|RelNode
name|scan
parameter_list|)
block|{
name|HiveTableScan
name|hts
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|scan
operator|instanceof
name|HiveJdbcConverter
condition|)
block|{
name|hts
operator|=
operator|(
operator|(
name|HiveJdbcConverter
operator|)
name|scan
operator|)
operator|.
name|getTableScan
argument_list|()
operator|.
name|getHiveTableScan
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|scan
operator|instanceof
name|DruidQuery
condition|)
block|{
name|hts
operator|=
call|(
name|HiveTableScan
call|)
argument_list|(
operator|(
name|DruidQuery
operator|)
name|scan
argument_list|)
operator|.
name|getTableScan
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|hts
operator|=
operator|(
name|HiveTableScan
operator|)
name|scan
expr_stmt|;
block|}
assert|assert
name|hts
operator|!=
literal|null
assert|;
name|RelOptHiveTable
name|hTbl
init|=
operator|(
name|RelOptHiveTable
operator|)
name|hts
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|ASTBuilder
name|b
init|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_TABREF
argument_list|,
literal|"TOK_TABREF"
argument_list|)
operator|.
name|add
argument_list|(
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_TABNAME
argument_list|,
literal|"TOK_TABNAME"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|hTbl
operator|.
name|getHiveTableMD
argument_list|()
operator|.
name|getDbName
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|hTbl
operator|.
name|getHiveTableMD
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|ASTBuilder
name|propList
init|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_TABLEPROPLIST
argument_list|,
literal|"TOK_TABLEPROPLIST"
argument_list|)
decl_stmt|;
if|if
condition|(
name|scan
operator|instanceof
name|DruidQuery
condition|)
block|{
comment|//Passing query spec, column names and column types to be used as part of Hive Physical execution
name|DruidQuery
name|dq
init|=
operator|(
name|DruidQuery
operator|)
name|scan
decl_stmt|;
comment|//Adding Query specs to be used by org.apache.hadoop.hive.druid.io.DruidQueryBasedInputFormat
name|propList
operator|.
name|add
argument_list|(
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_TABLEPROPERTY
argument_list|,
literal|"TOK_TABLEPROPERTY"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\""
operator|+
name|Constants
operator|.
name|DRUID_QUERY_JSON
operator|+
literal|"\""
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\""
operator|+
name|SemanticAnalyzer
operator|.
name|escapeSQLString
argument_list|(
name|dq
operator|.
name|getQueryString
argument_list|()
argument_list|)
operator|+
literal|"\""
argument_list|)
argument_list|)
expr_stmt|;
comment|// Adding column names used later by org.apache.hadoop.hive.druid.serde.DruidSerDe
name|propList
operator|.
name|add
argument_list|(
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_TABLEPROPERTY
argument_list|,
literal|"TOK_TABLEPROPERTY"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\""
operator|+
name|Constants
operator|.
name|DRUID_QUERY_FIELD_NAMES
operator|+
literal|"\""
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\""
operator|+
name|dq
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldNames
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|Object
operator|::
name|toString
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|","
argument_list|)
argument_list|)
operator|+
literal|"\""
argument_list|)
argument_list|)
expr_stmt|;
comment|// Adding column types used later by org.apache.hadoop.hive.druid.serde.DruidSerDe
name|propList
operator|.
name|add
argument_list|(
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_TABLEPROPERTY
argument_list|,
literal|"TOK_TABLEPROPERTY"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\""
operator|+
name|Constants
operator|.
name|DRUID_QUERY_FIELD_TYPES
operator|+
literal|"\""
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\""
operator|+
name|dq
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|e
lambda|->
name|TypeConverter
operator|.
name|convert
argument_list|(
name|e
operator|.
name|getType
argument_list|()
argument_list|)
operator|.
name|getTypeName
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|","
argument_list|)
argument_list|)
operator|+
literal|"\""
argument_list|)
argument_list|)
expr_stmt|;
name|propList
operator|.
name|add
argument_list|(
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_TABLEPROPERTY
argument_list|,
literal|"TOK_TABLEPROPERTY"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\""
operator|+
name|Constants
operator|.
name|DRUID_QUERY_TYPE
operator|+
literal|"\""
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\""
operator|+
name|dq
operator|.
name|getQueryType
argument_list|()
operator|.
name|getQueryName
argument_list|()
operator|+
literal|"\""
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|scan
operator|instanceof
name|HiveJdbcConverter
condition|)
block|{
name|HiveJdbcConverter
name|jdbcConverter
init|=
operator|(
name|HiveJdbcConverter
operator|)
name|scan
decl_stmt|;
specifier|final
name|String
name|query
init|=
name|jdbcConverter
operator|.
name|generateSql
argument_list|()
decl_stmt|;
name|LOGGER
operator|.
name|debug
argument_list|(
literal|"Generated SQL query: "
operator|+
name|System
operator|.
name|lineSeparator
argument_list|()
operator|+
name|query
argument_list|)
expr_stmt|;
name|propList
operator|.
name|add
argument_list|(
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_TABLEPROPERTY
argument_list|,
literal|"TOK_TABLEPROPERTY"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\""
operator|+
name|Constants
operator|.
name|JDBC_QUERY
operator|+
literal|"\""
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\""
operator|+
name|SemanticAnalyzer
operator|.
name|escapeSQLString
argument_list|(
name|query
argument_list|)
operator|+
literal|"\""
argument_list|)
argument_list|)
expr_stmt|;
comment|// Whether we can split the query
name|propList
operator|.
name|add
argument_list|(
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_TABLEPROPERTY
argument_list|,
literal|"TOK_TABLEPROPERTY"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\""
operator|+
name|Constants
operator|.
name|JDBC_SPLIT_QUERY
operator|+
literal|"\""
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\""
operator|+
name|jdbcConverter
operator|.
name|splittingAllowed
argument_list|()
operator|+
literal|"\""
argument_list|)
argument_list|)
expr_stmt|;
comment|// Adding column names used later by org.apache.hadoop.hive.druid.serde.DruidSerDe
name|propList
operator|.
name|add
argument_list|(
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_TABLEPROPERTY
argument_list|,
literal|"TOK_TABLEPROPERTY"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\""
operator|+
name|Constants
operator|.
name|JDBC_QUERY_FIELD_NAMES
operator|+
literal|"\""
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\""
operator|+
name|scan
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldNames
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|Object
operator|::
name|toString
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|","
argument_list|)
argument_list|)
operator|+
literal|"\""
argument_list|)
argument_list|)
expr_stmt|;
comment|// Adding column types used later by org.apache.hadoop.hive.druid.serde.DruidSerDe
name|propList
operator|.
name|add
argument_list|(
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_TABLEPROPERTY
argument_list|,
literal|"TOK_TABLEPROPERTY"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\""
operator|+
name|Constants
operator|.
name|JDBC_QUERY_FIELD_TYPES
operator|+
literal|"\""
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\""
operator|+
name|scan
operator|.
name|getRowType
argument_list|()
operator|.
name|getFieldList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|e
lambda|->
name|TypeConverter
operator|.
name|convert
argument_list|(
name|e
operator|.
name|getType
argument_list|()
argument_list|)
operator|.
name|getTypeName
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|","
argument_list|)
argument_list|)
operator|+
literal|"\""
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hts
operator|.
name|isInsideView
argument_list|()
condition|)
block|{
comment|// We need to carry the insideView information from calcite into the ast.
name|propList
operator|.
name|add
argument_list|(
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_TABLEPROPERTY
argument_list|,
literal|"TOK_TABLEPROPERTY"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\"insideView\""
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|StringLiteral
argument_list|,
literal|"\"TRUE\""
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|b
operator|.
name|add
argument_list|(
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_TABLEPROPERTIES
argument_list|,
literal|"TOK_TABLEPROPERTIES"
argument_list|)
operator|.
name|add
argument_list|(
name|propList
argument_list|)
argument_list|)
expr_stmt|;
comment|// NOTE: Calcite considers tbls to be equal if their names are the same. Hence
comment|// we need to provide Calcite the fully qualified table name (dbname.tblname)
comment|// and not the user provided aliases.
comment|// However in HIVE DB name can not appear in select list; in case of join
comment|// where table names differ only in DB name, Hive would require user
comment|// introducing explicit aliases for tbl.
name|b
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|hts
operator|.
name|getTableAlias
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|b
operator|.
name|node
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|ASTNode
name|join
parameter_list|(
name|ASTNode
name|left
parameter_list|,
name|ASTNode
name|right
parameter_list|,
name|JoinRelType
name|joinType
parameter_list|,
name|ASTNode
name|cond
parameter_list|)
block|{
name|ASTBuilder
name|b
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|joinType
condition|)
block|{
case|case
name|SEMI
case|:
name|b
operator|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_LEFTSEMIJOIN
argument_list|,
literal|"TOK_LEFTSEMIJOIN"
argument_list|)
expr_stmt|;
break|break;
case|case
name|INNER
case|:
name|b
operator|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_JOIN
argument_list|,
literal|"TOK_JOIN"
argument_list|)
expr_stmt|;
break|break;
case|case
name|LEFT
case|:
name|b
operator|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_LEFTOUTERJOIN
argument_list|,
literal|"TOK_LEFTOUTERJOIN"
argument_list|)
expr_stmt|;
break|break;
case|case
name|RIGHT
case|:
name|b
operator|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_RIGHTOUTERJOIN
argument_list|,
literal|"TOK_RIGHTOUTERJOIN"
argument_list|)
expr_stmt|;
break|break;
case|case
name|FULL
case|:
name|b
operator|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_FULLOUTERJOIN
argument_list|,
literal|"TOK_FULLOUTERJOIN"
argument_list|)
expr_stmt|;
break|break;
block|}
name|b
operator|.
name|add
argument_list|(
name|left
argument_list|)
operator|.
name|add
argument_list|(
name|right
argument_list|)
operator|.
name|add
argument_list|(
name|cond
argument_list|)
expr_stmt|;
return|return
name|b
operator|.
name|node
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|ASTNode
name|subQuery
parameter_list|(
name|ASTNode
name|qry
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
return|return
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_SUBQUERY
argument_list|,
literal|"TOK_SUBQUERY"
argument_list|)
operator|.
name|add
argument_list|(
name|qry
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|alias
argument_list|)
operator|.
name|node
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|ASTNode
name|qualifiedName
parameter_list|(
name|String
name|tableName
parameter_list|,
name|String
name|colName
parameter_list|)
block|{
name|ASTBuilder
name|b
init|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|DOT
argument_list|,
literal|"."
argument_list|)
operator|.
name|add
argument_list|(
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_TABLE_OR_COL
argument_list|,
literal|"TOK_TABLE_OR_COL"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|tableName
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|colName
argument_list|)
decl_stmt|;
return|return
name|b
operator|.
name|node
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|ASTNode
name|unqualifiedName
parameter_list|(
name|String
name|colName
parameter_list|)
block|{
name|ASTBuilder
name|b
init|=
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_TABLE_OR_COL
argument_list|,
literal|"TOK_TABLE_OR_COL"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|colName
argument_list|)
decl_stmt|;
return|return
name|b
operator|.
name|node
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|ASTNode
name|where
parameter_list|(
name|ASTNode
name|cond
parameter_list|)
block|{
return|return
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_WHERE
argument_list|,
literal|"TOK_WHERE"
argument_list|)
operator|.
name|add
argument_list|(
name|cond
argument_list|)
operator|.
name|node
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|ASTNode
name|having
parameter_list|(
name|ASTNode
name|cond
parameter_list|)
block|{
return|return
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_HAVING
argument_list|,
literal|"TOK_HAVING"
argument_list|)
operator|.
name|add
argument_list|(
name|cond
argument_list|)
operator|.
name|node
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|ASTNode
name|limit
parameter_list|(
name|Object
name|offset
parameter_list|,
name|Object
name|limit
parameter_list|)
block|{
return|return
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_LIMIT
argument_list|,
literal|"TOK_LIMIT"
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Number
argument_list|,
name|offset
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Number
argument_list|,
name|limit
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|node
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|ASTNode
name|selectExpr
parameter_list|(
name|ASTNode
name|expr
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
return|return
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_SELEXPR
argument_list|,
literal|"TOK_SELEXPR"
argument_list|)
operator|.
name|add
argument_list|(
name|expr
argument_list|)
operator|.
name|add
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|alias
argument_list|)
operator|.
name|node
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|ASTNode
name|literal
parameter_list|(
name|RexLiteral
name|literal
parameter_list|)
block|{
name|Object
name|val
init|=
literal|null
decl_stmt|;
name|int
name|type
init|=
literal|0
decl_stmt|;
name|SqlTypeName
name|sqlType
init|=
name|literal
operator|.
name|getType
argument_list|()
operator|.
name|getSqlTypeName
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|sqlType
condition|)
block|{
case|case
name|BINARY
case|:
case|case
name|DATE
case|:
case|case
name|TIME
case|:
case|case
name|TIMESTAMP
case|:
case|case
name|TIMESTAMP_WITH_LOCAL_TIME_ZONE
case|:
case|case
name|INTERVAL_DAY
case|:
case|case
name|INTERVAL_DAY_HOUR
case|:
case|case
name|INTERVAL_DAY_MINUTE
case|:
case|case
name|INTERVAL_DAY_SECOND
case|:
case|case
name|INTERVAL_HOUR
case|:
case|case
name|INTERVAL_HOUR_MINUTE
case|:
case|case
name|INTERVAL_HOUR_SECOND
case|:
case|case
name|INTERVAL_MINUTE
case|:
case|case
name|INTERVAL_MINUTE_SECOND
case|:
case|case
name|INTERVAL_MONTH
case|:
case|case
name|INTERVAL_SECOND
case|:
case|case
name|INTERVAL_YEAR
case|:
case|case
name|INTERVAL_YEAR_MONTH
case|:
case|case
name|ROW
case|:
if|if
condition|(
name|literal
operator|.
name|getValue
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_NULL
argument_list|,
literal|"TOK_NULL"
argument_list|)
operator|.
name|node
argument_list|()
return|;
block|}
break|break;
case|case
name|TINYINT
case|:
case|case
name|SMALLINT
case|:
case|case
name|INTEGER
case|:
case|case
name|BIGINT
case|:
case|case
name|DOUBLE
case|:
case|case
name|DECIMAL
case|:
case|case
name|FLOAT
case|:
case|case
name|REAL
case|:
case|case
name|VARCHAR
case|:
case|case
name|CHAR
case|:
case|case
name|BOOLEAN
case|:
if|if
condition|(
name|literal
operator|.
name|getValue3
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
name|ASTBuilder
operator|.
name|construct
argument_list|(
name|HiveParser
operator|.
name|TOK_NULL
argument_list|,
literal|"TOK_NULL"
argument_list|)
operator|.
name|node
argument_list|()
return|;
block|}
block|}
switch|switch
condition|(
name|sqlType
condition|)
block|{
case|case
name|TINYINT
case|:
case|case
name|SMALLINT
case|:
case|case
name|INTEGER
case|:
case|case
name|BIGINT
case|:
name|val
operator|=
name|literal
operator|.
name|getValue3
argument_list|()
expr_stmt|;
comment|// Calcite considers all numeric literals as bigdecimal values
comment|// Hive makes a distinction between them most importantly IntegralLiteral
if|if
condition|(
name|val
operator|instanceof
name|BigDecimal
condition|)
block|{
name|val
operator|=
operator|(
operator|(
name|BigDecimal
operator|)
name|val
operator|)
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
switch|switch
condition|(
name|sqlType
condition|)
block|{
case|case
name|TINYINT
case|:
name|val
operator|+=
literal|"Y"
expr_stmt|;
break|break;
case|case
name|SMALLINT
case|:
name|val
operator|+=
literal|"S"
expr_stmt|;
break|break;
case|case
name|INTEGER
case|:
name|val
operator|+=
literal|""
expr_stmt|;
break|break;
case|case
name|BIGINT
case|:
name|val
operator|+=
literal|"L"
expr_stmt|;
break|break;
block|}
name|type
operator|=
name|HiveParser
operator|.
name|IntegralLiteral
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|val
operator|=
name|literal
operator|.
name|getValue3
argument_list|()
operator|+
literal|"D"
expr_stmt|;
name|type
operator|=
name|HiveParser
operator|.
name|NumberLiteral
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|val
operator|=
name|literal
operator|.
name|getValue3
argument_list|()
operator|+
literal|"BD"
expr_stmt|;
name|type
operator|=
name|HiveParser
operator|.
name|NumberLiteral
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
case|case
name|REAL
case|:
name|val
operator|=
name|literal
operator|.
name|getValue3
argument_list|()
operator|+
literal|"F"
expr_stmt|;
name|type
operator|=
name|HiveParser
operator|.
name|Number
expr_stmt|;
break|break;
case|case
name|VARCHAR
case|:
case|case
name|CHAR
case|:
name|val
operator|=
name|literal
operator|.
name|getValue3
argument_list|()
expr_stmt|;
name|String
name|escapedVal
init|=
name|BaseSemanticAnalyzer
operator|.
name|escapeSQLString
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|val
argument_list|)
argument_list|)
decl_stmt|;
name|type
operator|=
name|HiveParser
operator|.
name|StringLiteral
expr_stmt|;
name|val
operator|=
literal|"'"
operator|+
name|escapedVal
operator|+
literal|"'"
expr_stmt|;
break|break;
case|case
name|BOOLEAN
case|:
name|val
operator|=
name|literal
operator|.
name|getValue3
argument_list|()
expr_stmt|;
name|type
operator|=
operator|(
operator|(
name|Boolean
operator|)
name|val
operator|)
operator|.
name|booleanValue
argument_list|()
condition|?
name|HiveParser
operator|.
name|KW_TRUE
else|:
name|HiveParser
operator|.
name|KW_FALSE
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|val
operator|=
literal|"'"
operator|+
name|literal
operator|.
name|getValueAs
argument_list|(
name|DateString
operator|.
name|class
argument_list|)
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
expr_stmt|;
name|type
operator|=
name|HiveParser
operator|.
name|TOK_DATELITERAL
expr_stmt|;
break|break;
case|case
name|TIME
case|:
name|val
operator|=
literal|"'"
operator|+
name|literal
operator|.
name|getValueAs
argument_list|(
name|TimeString
operator|.
name|class
argument_list|)
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
expr_stmt|;
name|type
operator|=
name|HiveParser
operator|.
name|TOK_TIMESTAMPLITERAL
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|val
operator|=
literal|"'"
operator|+
name|literal
operator|.
name|getValueAs
argument_list|(
name|TimestampString
operator|.
name|class
argument_list|)
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
expr_stmt|;
name|type
operator|=
name|HiveParser
operator|.
name|TOK_TIMESTAMPLITERAL
expr_stmt|;
break|break;
case|case
name|TIMESTAMP_WITH_LOCAL_TIME_ZONE
case|:
comment|// Calcite stores timestamp with local time-zone in UTC internally, thus
comment|// when we bring it back, we need to add the UTC suffix.
name|val
operator|=
literal|"'"
operator|+
name|literal
operator|.
name|getValueAs
argument_list|(
name|TimestampString
operator|.
name|class
argument_list|)
operator|.
name|toString
argument_list|()
operator|+
literal|" UTC'"
expr_stmt|;
name|type
operator|=
name|HiveParser
operator|.
name|TOK_TIMESTAMPLOCALTZLITERAL
expr_stmt|;
break|break;
case|case
name|INTERVAL_YEAR
case|:
case|case
name|INTERVAL_MONTH
case|:
case|case
name|INTERVAL_YEAR_MONTH
case|:
block|{
name|type
operator|=
name|HiveParser
operator|.
name|TOK_INTERVAL_YEAR_MONTH_LITERAL
expr_stmt|;
name|BigDecimal
name|monthsBd
init|=
operator|(
name|BigDecimal
operator|)
name|literal
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|HiveIntervalYearMonth
name|intervalYearMonth
init|=
operator|new
name|HiveIntervalYearMonth
argument_list|(
name|monthsBd
operator|.
name|intValue
argument_list|()
argument_list|)
decl_stmt|;
name|val
operator|=
literal|"'"
operator|+
name|intervalYearMonth
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
expr_stmt|;
block|}
break|break;
case|case
name|INTERVAL_DAY
case|:
case|case
name|INTERVAL_DAY_HOUR
case|:
case|case
name|INTERVAL_DAY_MINUTE
case|:
case|case
name|INTERVAL_DAY_SECOND
case|:
case|case
name|INTERVAL_HOUR
case|:
case|case
name|INTERVAL_HOUR_MINUTE
case|:
case|case
name|INTERVAL_HOUR_SECOND
case|:
case|case
name|INTERVAL_MINUTE
case|:
case|case
name|INTERVAL_MINUTE_SECOND
case|:
case|case
name|INTERVAL_SECOND
case|:
block|{
name|type
operator|=
name|HiveParser
operator|.
name|TOK_INTERVAL_DAY_TIME_LITERAL
expr_stmt|;
name|BigDecimal
name|millisBd
init|=
operator|(
name|BigDecimal
operator|)
name|literal
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|// Calcite literal is in millis, convert to seconds
name|BigDecimal
name|secsBd
init|=
name|millisBd
operator|.
name|divide
argument_list|(
name|BigDecimal
operator|.
name|valueOf
argument_list|(
literal|1000
argument_list|)
argument_list|)
decl_stmt|;
name|HiveIntervalDayTime
name|intervalDayTime
init|=
operator|new
name|HiveIntervalDayTime
argument_list|(
name|secsBd
argument_list|)
decl_stmt|;
name|val
operator|=
literal|"'"
operator|+
name|intervalDayTime
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
expr_stmt|;
block|}
break|break;
case|case
name|NULL
case|:
name|type
operator|=
name|HiveParser
operator|.
name|TOK_NULL
expr_stmt|;
break|break;
comment|//binary, ROW type should not be seen.
case|case
name|BINARY
case|:
case|case
name|ROW
case|:
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unsupported Type: "
operator|+
name|sqlType
argument_list|)
throw|;
block|}
return|return
operator|(
name|ASTNode
operator|)
name|ParseDriver
operator|.
name|adaptor
operator|.
name|create
argument_list|(
name|type
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|val
argument_list|)
argument_list|)
return|;
block|}
name|ASTNode
name|curr
decl_stmt|;
specifier|public
name|ASTNode
name|node
parameter_list|()
block|{
return|return
name|curr
return|;
block|}
specifier|public
name|ASTBuilder
name|add
parameter_list|(
name|int
name|tokenType
parameter_list|,
name|String
name|text
parameter_list|)
block|{
name|ParseDriver
operator|.
name|adaptor
operator|.
name|addChild
argument_list|(
name|curr
argument_list|,
name|createAST
argument_list|(
name|tokenType
argument_list|,
name|text
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ASTBuilder
name|add
parameter_list|(
name|ASTBuilder
name|b
parameter_list|)
block|{
name|ParseDriver
operator|.
name|adaptor
operator|.
name|addChild
argument_list|(
name|curr
argument_list|,
name|b
operator|.
name|curr
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ASTBuilder
name|add
parameter_list|(
name|ASTNode
name|n
parameter_list|)
block|{
if|if
condition|(
name|n
operator|!=
literal|null
condition|)
block|{
name|ParseDriver
operator|.
name|adaptor
operator|.
name|addChild
argument_list|(
name|curr
argument_list|,
name|n
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

