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
name|metastore
operator|.
name|tools
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|conf
operator|.
name|Configuration
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
name|metastore
operator|.
name|DatabaseProduct
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|metastore
operator|.
name|conf
operator|.
name|MetastoreConf
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
name|metastore
operator|.
name|conf
operator|.
name|MetastoreConf
operator|.
name|ConfVars
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

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Connection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|PreparedStatement
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * Helper class that generates SQL queries with syntax specific to target DB  * todo: why throw MetaException?  */
end_comment

begin_class
annotation|@
name|VisibleForTesting
specifier|public
specifier|final
class|class
name|SQLGenerator
block|{
specifier|static
specifier|final
specifier|private
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SQLGenerator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|DatabaseProduct
name|dbProduct
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|SQLGenerator
parameter_list|(
name|DatabaseProduct
name|dbProduct
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|dbProduct
operator|=
name|dbProduct
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
comment|/**    * Generates "Insert into T(a,b,c) values(1,2,'f'),(3,4,'c')" for appropriate DB    *    * @param tblColumns   e.g. "T(a,b,c)"    * @param rows         e.g. list of Strings like 3,4,'d'    * @param paramsList   List of parameters which in turn is list of Strings to be set in PreparedStatement object    * @return List PreparedStatement objects for fully formed INSERT INTO ... statements    */
specifier|public
name|List
argument_list|<
name|PreparedStatement
argument_list|>
name|createInsertValuesPreparedStmt
parameter_list|(
name|Connection
name|dbConn
parameter_list|,
name|String
name|tblColumns
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|rows
parameter_list|,
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|paramsList
parameter_list|)
throws|throws
name|SQLException
block|{
if|if
condition|(
name|rows
operator|==
literal|null
operator|||
name|rows
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
assert|assert
operator|(
operator|(
name|paramsList
operator|==
literal|null
operator|)
operator|||
operator|(
name|rows
operator|.
name|size
argument_list|()
operator|==
name|paramsList
operator|.
name|size
argument_list|()
operator|)
operator|)
assert|;
name|List
argument_list|<
name|Integer
argument_list|>
name|rowsCountInStmts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|insertStmts
init|=
name|createInsertValuesStmt
argument_list|(
name|tblColumns
argument_list|,
name|rows
argument_list|,
name|rowsCountInStmts
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|insertStmts
operator|.
name|size
argument_list|()
operator|==
name|rowsCountInStmts
operator|.
name|size
argument_list|()
operator|)
assert|;
name|List
argument_list|<
name|PreparedStatement
argument_list|>
name|preparedStmts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|paramsListFromIdx
init|=
literal|0
decl_stmt|;
try|try
block|{
for|for
control|(
name|int
name|stmtIdx
init|=
literal|0
init|;
name|stmtIdx
operator|<
name|insertStmts
operator|.
name|size
argument_list|()
condition|;
name|stmtIdx
operator|++
control|)
block|{
name|String
name|sql
init|=
name|insertStmts
operator|.
name|get
argument_list|(
name|stmtIdx
argument_list|)
decl_stmt|;
name|PreparedStatement
name|pStmt
init|=
name|prepareStmtWithParameters
argument_list|(
name|dbConn
argument_list|,
name|sql
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|paramsList
operator|!=
literal|null
condition|)
block|{
name|int
name|paramIdx
init|=
literal|1
decl_stmt|;
name|int
name|paramsListToIdx
init|=
name|paramsListFromIdx
operator|+
name|rowsCountInStmts
operator|.
name|get
argument_list|(
name|stmtIdx
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|paramsListIdx
init|=
name|paramsListFromIdx
init|;
name|paramsListIdx
operator|<
name|paramsListToIdx
condition|;
name|paramsListIdx
operator|++
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|params
init|=
name|paramsList
operator|.
name|get
argument_list|(
name|paramsListIdx
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|params
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
operator|,
name|paramIdx
operator|++
control|)
block|{
name|pStmt
operator|.
name|setString
argument_list|(
name|paramIdx
argument_list|,
name|params
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|paramsListFromIdx
operator|=
name|paramsListToIdx
expr_stmt|;
block|}
name|preparedStmts
operator|.
name|add
argument_list|(
name|pStmt
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
for|for
control|(
name|PreparedStatement
name|pst
range|:
name|preparedStmts
control|)
block|{
name|pst
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
throw|throw
name|e
throw|;
block|}
return|return
name|preparedStmts
return|;
block|}
comment|/**    * Generates "Insert into T(a,b,c) values(1,2,'f'),(3,4,'c')" for appropriate DB    *    * @param tblColumns e.g. "T(a,b,c)"    * @param rows       e.g. list of Strings like 3,4,'d'    * @return fully formed INSERT INTO ... statements    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|createInsertValuesStmt
parameter_list|(
name|String
name|tblColumns
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|rows
parameter_list|)
block|{
return|return
name|createInsertValuesStmt
argument_list|(
name|tblColumns
argument_list|,
name|rows
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Generates "Insert into T(a,b,c) values(1,2,'f'),(3,4,'c')" for appropriate DB    *    * @param tblColumns e.g. "T(a,b,c)"    * @param rows       e.g. list of Strings like 3,4,'d'    * @param rowsCountInStmts Output the number of rows in each insert statement returned.    * @return fully formed INSERT INTO ... statements    */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|createInsertValuesStmt
parameter_list|(
name|String
name|tblColumns
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|rows
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|rowsCountInStmts
parameter_list|)
block|{
if|if
condition|(
name|rows
operator|==
literal|null
operator|||
name|rows
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|insertStmts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|numRowsInCurrentStmt
init|=
literal|0
decl_stmt|;
switch|switch
condition|(
name|dbProduct
condition|)
block|{
case|case
name|ORACLE
case|:
if|if
condition|(
name|rows
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
comment|//http://www.oratable.com/oracle-insert-all/
comment|//https://livesql.oracle.com/apex/livesql/file/content_BM1LJQ87M5CNIOKPOWPV6ZGR3.html
for|for
control|(
name|int
name|numRows
init|=
literal|0
init|;
name|numRows
operator|<
name|rows
operator|.
name|size
argument_list|()
condition|;
name|numRows
operator|++
control|)
block|{
if|if
condition|(
name|numRows
operator|%
name|MetastoreConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|DIRECT_SQL_MAX_ELEMENTS_VALUES_CLAUSE
argument_list|)
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|numRows
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" select * from dual"
argument_list|)
expr_stmt|;
name|insertStmts
operator|.
name|add
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|rowsCountInStmts
operator|!=
literal|null
condition|)
block|{
name|rowsCountInStmts
operator|.
name|add
argument_list|(
name|numRowsInCurrentStmt
argument_list|)
expr_stmt|;
block|}
name|numRowsInCurrentStmt
operator|=
literal|0
expr_stmt|;
block|}
name|sb
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"insert all "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"into "
argument_list|)
operator|.
name|append
argument_list|(
name|tblColumns
argument_list|)
operator|.
name|append
argument_list|(
literal|" values("
argument_list|)
operator|.
name|append
argument_list|(
name|rows
operator|.
name|get
argument_list|(
name|numRows
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|") "
argument_list|)
expr_stmt|;
name|numRowsInCurrentStmt
operator|++
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"select * from dual"
argument_list|)
expr_stmt|;
name|insertStmts
operator|.
name|add
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|rowsCountInStmts
operator|!=
literal|null
condition|)
block|{
name|rowsCountInStmts
operator|.
name|add
argument_list|(
name|numRowsInCurrentStmt
argument_list|)
expr_stmt|;
block|}
return|return
name|insertStmts
return|;
block|}
comment|//fall through
case|case
name|DERBY
case|:
case|case
name|MYSQL
case|:
case|case
name|POSTGRES
case|:
case|case
name|SQLSERVER
case|:
for|for
control|(
name|int
name|numRows
init|=
literal|0
init|;
name|numRows
operator|<
name|rows
operator|.
name|size
argument_list|()
condition|;
name|numRows
operator|++
control|)
block|{
if|if
condition|(
name|numRows
operator|%
name|MetastoreConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|DIRECT_SQL_MAX_ELEMENTS_VALUES_CLAUSE
argument_list|)
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|numRows
operator|>
literal|0
condition|)
block|{
name|insertStmts
operator|.
name|add
argument_list|(
name|sb
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sb
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|//exclude trailing comma
if|if
condition|(
name|rowsCountInStmts
operator|!=
literal|null
condition|)
block|{
name|rowsCountInStmts
operator|.
name|add
argument_list|(
name|numRowsInCurrentStmt
argument_list|)
expr_stmt|;
block|}
name|numRowsInCurrentStmt
operator|=
literal|0
expr_stmt|;
block|}
name|sb
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"insert into "
argument_list|)
operator|.
name|append
argument_list|(
name|tblColumns
argument_list|)
operator|.
name|append
argument_list|(
literal|" values"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|'('
argument_list|)
operator|.
name|append
argument_list|(
name|rows
operator|.
name|get
argument_list|(
name|numRows
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"),"
argument_list|)
expr_stmt|;
name|numRowsInCurrentStmt
operator|++
expr_stmt|;
block|}
name|insertStmts
operator|.
name|add
argument_list|(
name|sb
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sb
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|//exclude trailing comma
if|if
condition|(
name|rowsCountInStmts
operator|!=
literal|null
condition|)
block|{
name|rowsCountInStmts
operator|.
name|add
argument_list|(
name|numRowsInCurrentStmt
argument_list|)
expr_stmt|;
block|}
return|return
name|insertStmts
return|;
default|default:
name|String
name|msg
init|=
literal|"Unrecognized database product name<"
operator|+
name|dbProduct
operator|+
literal|">"
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
block|}
comment|/**    * Given a {@code selectStatement}, decorated it with FOR UPDATE or semantically equivalent    * construct.  If the DB doesn't support, return original select.    */
specifier|public
name|String
name|addForUpdateClause
parameter_list|(
name|String
name|selectStatement
parameter_list|)
throws|throws
name|MetaException
block|{
switch|switch
condition|(
name|dbProduct
condition|)
block|{
case|case
name|DERBY
case|:
comment|//https://db.apache.org/derby/docs/10.1/ref/rrefsqlj31783.html
comment|//sadly in Derby, FOR UPDATE doesn't meant what it should
return|return
name|selectStatement
return|;
case|case
name|MYSQL
case|:
comment|//http://dev.mysql.com/doc/refman/5.7/en/select.html
case|case
name|ORACLE
case|:
comment|//https://docs.oracle.com/cd/E17952_01/refman-5.6-en/select.html
case|case
name|POSTGRES
case|:
comment|//http://www.postgresql.org/docs/9.0/static/sql-select.html
return|return
name|selectStatement
operator|+
literal|" for update"
return|;
case|case
name|SQLSERVER
case|:
comment|//https://msdn.microsoft.com/en-us/library/ms189499.aspx
comment|//https://msdn.microsoft.com/en-us/library/ms187373.aspx
name|String
name|modifier
init|=
literal|" with (updlock)"
decl_stmt|;
name|int
name|wherePos
init|=
name|selectStatement
operator|.
name|toUpperCase
argument_list|()
operator|.
name|indexOf
argument_list|(
literal|" WHERE "
argument_list|)
decl_stmt|;
if|if
condition|(
name|wherePos
operator|<
literal|0
condition|)
block|{
return|return
name|selectStatement
operator|+
name|modifier
return|;
block|}
return|return
name|selectStatement
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|wherePos
argument_list|)
operator|+
name|modifier
operator|+
name|selectStatement
operator|.
name|substring
argument_list|(
name|wherePos
argument_list|,
name|selectStatement
operator|.
name|length
argument_list|()
argument_list|)
return|;
default|default:
name|String
name|msg
init|=
literal|"Unrecognized database product name<"
operator|+
name|dbProduct
operator|+
literal|">"
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|MetaException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
block|}
comment|/**    * Suppose you have a query "select a,b from T" and you want to limit the result set    * to the first 5 rows.  The mechanism to do that differs in different DBs.    * Make {@code noSelectsqlQuery} to be "a,b from T" and this method will return the    * appropriately modified row limiting query.    *<p>    * Note that if {@code noSelectsqlQuery} contains a join, you must make sure that    * all columns are unique for Oracle.    */
specifier|public
name|String
name|addLimitClause
parameter_list|(
name|int
name|numRows
parameter_list|,
name|String
name|noSelectsqlQuery
parameter_list|)
throws|throws
name|MetaException
block|{
switch|switch
condition|(
name|dbProduct
condition|)
block|{
case|case
name|DERBY
case|:
comment|//http://db.apache.org/derby/docs/10.7/ref/rrefsqljoffsetfetch.html
return|return
literal|"select "
operator|+
name|noSelectsqlQuery
operator|+
literal|" fetch first "
operator|+
name|numRows
operator|+
literal|" rows only"
return|;
case|case
name|MYSQL
case|:
comment|//http://www.postgresql.org/docs/7.3/static/queries-limit.html
case|case
name|POSTGRES
case|:
comment|//https://dev.mysql.com/doc/refman/5.0/en/select.html
return|return
literal|"select "
operator|+
name|noSelectsqlQuery
operator|+
literal|" limit "
operator|+
name|numRows
return|;
case|case
name|ORACLE
case|:
comment|//newer versions (12c and later) support OFFSET/FETCH
return|return
literal|"select * from (select "
operator|+
name|noSelectsqlQuery
operator|+
literal|") where rownum<= "
operator|+
name|numRows
return|;
case|case
name|SQLSERVER
case|:
comment|//newer versions (2012 and later) support OFFSET/FETCH
comment|//https://msdn.microsoft.com/en-us/library/ms189463.aspx
return|return
literal|"select TOP("
operator|+
name|numRows
operator|+
literal|") "
operator|+
name|noSelectsqlQuery
return|;
default|default:
name|String
name|msg
init|=
literal|"Unrecognized database product name<"
operator|+
name|dbProduct
operator|+
literal|">"
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|MetaException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
block|}
comment|/**    * Make PreparedStatement object with list of String type parameters to be set.    * It is assumed the input sql string have the number of "?" equal to number of parameters    * passed as input.    * @param dbConn - Connection object    * @param sql - SQL statement with "?" for input parameters.    * @param parameters - List of String type parameters to be set in PreparedStatement object    * @return PreparedStatement type object    * @throws SQLException    */
specifier|public
name|PreparedStatement
name|prepareStmtWithParameters
parameter_list|(
name|Connection
name|dbConn
parameter_list|,
name|String
name|sql
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|parameters
parameter_list|)
throws|throws
name|SQLException
block|{
name|PreparedStatement
name|pst
init|=
name|dbConn
operator|.
name|prepareStatement
argument_list|(
name|addEscapeCharacters
argument_list|(
name|sql
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|parameters
operator|==
literal|null
operator|)
operator|||
name|parameters
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|pst
return|;
block|}
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|parameters
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|pst
operator|.
name|setString
argument_list|(
name|i
argument_list|,
name|parameters
operator|.
name|get
argument_list|(
name|i
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
name|pst
operator|.
name|close
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
return|return
name|pst
return|;
block|}
specifier|public
name|DatabaseProduct
name|getDbProduct
parameter_list|()
block|{
return|return
name|dbProduct
return|;
block|}
comment|// This is required for SQL executed directly. If the SQL has double quotes then some dbs tend to
comment|// remove the escape characters and store the variable without double quote.
specifier|public
name|String
name|addEscapeCharacters
parameter_list|(
name|String
name|s
parameter_list|)
block|{
if|if
condition|(
name|dbProduct
operator|==
name|DatabaseProduct
operator|.
name|MYSQL
condition|)
block|{
return|return
name|s
operator|.
name|replaceAll
argument_list|(
literal|"\\\\"
argument_list|,
literal|"\\\\\\\\"
argument_list|)
return|;
block|}
return|return
name|s
return|;
block|}
block|}
end_class

end_unit

