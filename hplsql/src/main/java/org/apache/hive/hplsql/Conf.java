begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hplsql
package|;
end_package

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|hive
operator|.
name|hplsql
operator|.
name|Exec
operator|.
name|OnError
import|;
end_import

begin_comment
comment|/**  * HPL/SQL run-time configuration  */
end_comment

begin_class
specifier|public
class|class
name|Conf
extends|extends
name|Configuration
block|{
specifier|public
specifier|static
specifier|final
name|String
name|SITE_XML
init|=
literal|"hplsql-site.xml"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DOT_HPLSQLRC
init|=
literal|".hplsqlrc"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HPLSQLRC
init|=
literal|"hplsqlrc"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HPLSQL_LOCALS_SQL
init|=
literal|"hplsql_locals.sql"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CONN_CONVERT
init|=
literal|"hplsql.conn.convert."
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CONN_DEFAULT
init|=
literal|"hplsql.conn.default"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DUAL_TABLE
init|=
literal|"hplsql.dual.table"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INSERT_VALUES
init|=
literal|"hplsql.insert.values"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ONERROR
init|=
literal|"hplsql.onerror"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEMP_TABLES
init|=
literal|"hplsql.temp.tables"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEMP_TABLES_SCHEMA
init|=
literal|"hplsql.temp.tables.schema"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEMP_TABLES_LOCATION
init|=
literal|"hplsql.temp.tables.location"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TRUE
init|=
literal|"true"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FALSE
init|=
literal|"false"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|YES
init|=
literal|"yes"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|NO
init|=
literal|"no"
decl_stmt|;
specifier|public
enum|enum
name|InsertValues
block|{
name|NATIVE
block|,
name|SELECT
block|}
empty_stmt|;
specifier|public
enum|enum
name|TempTables
block|{
name|NATIVE
block|,
name|MANAGED
block|}
empty_stmt|;
specifier|public
name|String
name|defaultConnection
decl_stmt|;
name|OnError
name|onError
init|=
name|OnError
operator|.
name|EXCEPTION
decl_stmt|;
name|InsertValues
name|insertValues
init|=
name|InsertValues
operator|.
name|NATIVE
decl_stmt|;
name|TempTables
name|tempTables
init|=
name|TempTables
operator|.
name|NATIVE
decl_stmt|;
name|String
name|dualTable
init|=
literal|null
decl_stmt|;
name|String
name|tempTablesSchema
init|=
literal|""
decl_stmt|;
name|String
name|tempTablesLocation
init|=
literal|"/tmp/hplsql"
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
name|connConvert
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Set an option    */
specifier|public
name|void
name|setOption
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|key
operator|.
name|startsWith
argument_list|(
name|CONN_CONVERT
argument_list|)
condition|)
block|{
name|setConnectionConvert
argument_list|(
name|key
operator|.
name|substring
argument_list|(
literal|19
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|compareToIgnoreCase
argument_list|(
name|CONN_DEFAULT
argument_list|)
operator|==
literal|0
condition|)
block|{
name|defaultConnection
operator|=
name|value
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|compareToIgnoreCase
argument_list|(
name|DUAL_TABLE
argument_list|)
operator|==
literal|0
condition|)
block|{
name|dualTable
operator|=
name|value
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|compareToIgnoreCase
argument_list|(
name|INSERT_VALUES
argument_list|)
operator|==
literal|0
condition|)
block|{
name|setInsertValues
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|compareToIgnoreCase
argument_list|(
name|ONERROR
argument_list|)
operator|==
literal|0
condition|)
block|{
name|setOnError
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|compareToIgnoreCase
argument_list|(
name|TEMP_TABLES
argument_list|)
operator|==
literal|0
condition|)
block|{
name|setTempTables
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|compareToIgnoreCase
argument_list|(
name|TEMP_TABLES_SCHEMA
argument_list|)
operator|==
literal|0
condition|)
block|{
name|tempTablesSchema
operator|=
name|value
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|compareToIgnoreCase
argument_list|(
name|TEMP_TABLES_LOCATION
argument_list|)
operator|==
literal|0
condition|)
block|{
name|tempTablesLocation
operator|=
name|value
expr_stmt|;
block|}
block|}
comment|/**    * Set hplsql.insert.values option    */
specifier|private
name|void
name|setInsertValues
parameter_list|(
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|.
name|compareToIgnoreCase
argument_list|(
literal|"NATIVE"
argument_list|)
operator|==
literal|0
condition|)
block|{
name|insertValues
operator|=
name|InsertValues
operator|.
name|NATIVE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|.
name|compareToIgnoreCase
argument_list|(
literal|"SELECT"
argument_list|)
operator|==
literal|0
condition|)
block|{
name|insertValues
operator|=
name|InsertValues
operator|.
name|SELECT
expr_stmt|;
block|}
block|}
comment|/**    * Set hplsql.temp.tables option    */
specifier|private
name|void
name|setTempTables
parameter_list|(
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|.
name|compareToIgnoreCase
argument_list|(
literal|"NATIVE"
argument_list|)
operator|==
literal|0
condition|)
block|{
name|tempTables
operator|=
name|TempTables
operator|.
name|NATIVE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|.
name|compareToIgnoreCase
argument_list|(
literal|"MANAGED"
argument_list|)
operator|==
literal|0
condition|)
block|{
name|tempTables
operator|=
name|TempTables
operator|.
name|MANAGED
expr_stmt|;
block|}
block|}
comment|/**    * Set error handling approach    */
specifier|private
name|void
name|setOnError
parameter_list|(
name|String
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|.
name|compareToIgnoreCase
argument_list|(
literal|"EXCEPTION"
argument_list|)
operator|==
literal|0
condition|)
block|{
name|onError
operator|=
name|OnError
operator|.
name|EXCEPTION
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|.
name|compareToIgnoreCase
argument_list|(
literal|"SETERROR"
argument_list|)
operator|==
literal|0
condition|)
block|{
name|onError
operator|=
name|OnError
operator|.
name|SETERROR
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|.
name|compareToIgnoreCase
argument_list|(
literal|"STOP"
argument_list|)
operator|==
literal|0
condition|)
block|{
name|onError
operator|=
name|OnError
operator|.
name|STOP
expr_stmt|;
block|}
block|}
comment|/**    * Set whether convert or not SQL for the specified connection profile    */
name|void
name|setConnectionConvert
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|boolean
name|convert
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|value
operator|.
name|compareToIgnoreCase
argument_list|(
name|TRUE
argument_list|)
operator|==
literal|0
operator|||
name|value
operator|.
name|compareToIgnoreCase
argument_list|(
name|YES
argument_list|)
operator|==
literal|0
condition|)
block|{
name|convert
operator|=
literal|true
expr_stmt|;
block|}
name|connConvert
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|convert
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get whether convert or not SQL for the specified connection profile    */
name|boolean
name|getConnectionConvert
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|Boolean
name|convert
init|=
name|connConvert
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|convert
operator|!=
literal|null
condition|)
block|{
return|return
name|convert
operator|.
name|booleanValue
argument_list|()
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Load parameters    */
specifier|public
name|void
name|init
parameter_list|()
block|{
name|addResource
argument_list|(
name|SITE_XML
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the location of the configuration file    */
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
name|URL
name|url
init|=
name|getResource
argument_list|(
name|SITE_XML
argument_list|)
decl_stmt|;
if|if
condition|(
name|url
operator|!=
literal|null
condition|)
block|{
return|return
name|url
operator|.
name|toString
argument_list|()
return|;
block|}
return|return
literal|""
return|;
block|}
block|}
end_class

end_unit

