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
name|parse
package|;
end_package

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
name|HiveConf
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
name|FieldSchema
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
name|Driver
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
name|QueryPlan
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
name|ddl
operator|.
name|DDLTask
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
name|ddl
operator|.
name|DDLWork
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
name|ddl
operator|.
name|table
operator|.
name|creation
operator|.
name|CreateTableDesc
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestHiveDecimalParse
block|{
annotation|@
name|Test
specifier|public
name|void
name|testDecimalType
parameter_list|()
throws|throws
name|ParseException
block|{
name|String
name|query
init|=
literal|"create table `dec` (d decimal)"
decl_stmt|;
name|String
name|type
init|=
name|getColumnType
argument_list|(
name|query
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"decimal(10,0)"
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalType1
parameter_list|()
throws|throws
name|ParseException
block|{
name|String
name|query
init|=
literal|"create table `dec` (d decimal(5))"
decl_stmt|;
name|String
name|type
init|=
name|getColumnType
argument_list|(
name|query
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"decimal(5,0)"
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalType2
parameter_list|()
throws|throws
name|ParseException
block|{
name|String
name|query
init|=
literal|"create table `dec` (d decimal(9,7))"
decl_stmt|;
name|String
name|type
init|=
name|getColumnType
argument_list|(
name|query
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"decimal(9,7)"
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalType3
parameter_list|()
throws|throws
name|ParseException
block|{
name|String
name|query
init|=
literal|"create table `dec` (d decimal(66,7))"
decl_stmt|;
name|Driver
name|driver
init|=
name|createDriver
argument_list|()
decl_stmt|;
name|int
name|rc
init|=
name|driver
operator|.
name|compile
argument_list|(
name|query
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Got "
operator|+
name|rc
operator|+
literal|", expected not zero"
argument_list|,
name|rc
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|driver
operator|.
name|getErrorMsg
argument_list|()
argument_list|,
name|driver
operator|.
name|getErrorMsg
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Decimal precision out of allowed range [1,38]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalType4
parameter_list|()
throws|throws
name|ParseException
block|{
name|String
name|query
init|=
literal|"create table `dec` (d decimal(0,7))"
decl_stmt|;
name|Driver
name|driver
init|=
name|createDriver
argument_list|()
decl_stmt|;
name|int
name|rc
init|=
name|driver
operator|.
name|compile
argument_list|(
name|query
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Got "
operator|+
name|rc
operator|+
literal|", expected not zero"
argument_list|,
name|rc
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|driver
operator|.
name|getErrorMsg
argument_list|()
argument_list|,
name|driver
operator|.
name|getErrorMsg
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Decimal precision out of allowed range [1,38]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalType5
parameter_list|()
throws|throws
name|ParseException
block|{
name|String
name|query
init|=
literal|"create table `dec` (d decimal(7,33))"
decl_stmt|;
name|Driver
name|driver
init|=
name|createDriver
argument_list|()
decl_stmt|;
name|int
name|rc
init|=
name|driver
operator|.
name|compile
argument_list|(
name|query
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Got "
operator|+
name|rc
operator|+
literal|", expected not zero"
argument_list|,
name|rc
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|driver
operator|.
name|getErrorMsg
argument_list|()
argument_list|,
name|driver
operator|.
name|getErrorMsg
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Decimal scale must be less than or equal to precision"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalType6
parameter_list|()
throws|throws
name|ParseException
block|{
name|String
name|query
init|=
literal|"create table `dec` (d decimal(7,-1))"
decl_stmt|;
name|Driver
name|driver
init|=
name|createDriver
argument_list|()
decl_stmt|;
name|int
name|rc
init|=
name|driver
operator|.
name|compile
argument_list|(
name|query
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Got "
operator|+
name|rc
operator|+
literal|", expected not zero"
argument_list|,
name|rc
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|driver
operator|.
name|getErrorMsg
argument_list|()
argument_list|,
name|driver
operator|.
name|getErrorMsg
argument_list|()
operator|.
name|contains
argument_list|(
literal|"extraneous input '-' expecting Number"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalType7
parameter_list|()
throws|throws
name|ParseException
block|{
name|String
name|query
init|=
literal|"create table `dec` (d decimal(7,33,4))"
decl_stmt|;
name|Driver
name|driver
init|=
name|createDriver
argument_list|()
decl_stmt|;
name|int
name|rc
init|=
name|driver
operator|.
name|compile
argument_list|(
name|query
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Got "
operator|+
name|rc
operator|+
literal|", expected not zero"
argument_list|,
name|rc
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|driver
operator|.
name|getErrorMsg
argument_list|()
argument_list|,
name|driver
operator|.
name|getErrorMsg
argument_list|()
operator|.
name|contains
argument_list|(
literal|"missing ) at ',' near ',' in column name or constraint"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalType8
parameter_list|()
throws|throws
name|ParseException
block|{
name|String
name|query
init|=
literal|"create table `dec` (d decimal(7a))"
decl_stmt|;
name|Driver
name|driver
init|=
name|createDriver
argument_list|()
decl_stmt|;
name|int
name|rc
init|=
name|driver
operator|.
name|compile
argument_list|(
name|query
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Got "
operator|+
name|rc
operator|+
literal|", expected not zero"
argument_list|,
name|rc
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|driver
operator|.
name|getErrorMsg
argument_list|()
argument_list|,
name|driver
operator|.
name|getErrorMsg
argument_list|()
operator|.
name|contains
argument_list|(
literal|"mismatched input '7a' expecting Number near '('"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimalType9
parameter_list|()
throws|throws
name|ParseException
block|{
name|String
name|query
init|=
literal|"create table `dec` (d decimal(20,23))"
decl_stmt|;
name|Driver
name|driver
init|=
name|createDriver
argument_list|()
decl_stmt|;
name|int
name|rc
init|=
name|driver
operator|.
name|compile
argument_list|(
name|query
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Got "
operator|+
name|rc
operator|+
literal|", expected not zero"
argument_list|,
name|rc
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|driver
operator|.
name|getErrorMsg
argument_list|()
argument_list|,
name|driver
operator|.
name|getErrorMsg
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Decimal scale must be less than or equal to precision"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Driver
name|createDriver
parameter_list|()
block|{
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|Driver
operator|.
name|class
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Driver
name|driver
init|=
operator|new
name|Driver
argument_list|(
name|conf
argument_list|)
decl_stmt|;
return|return
name|driver
return|;
block|}
specifier|private
name|String
name|getColumnType
parameter_list|(
name|String
name|query
parameter_list|)
block|{
name|Driver
name|driver
init|=
name|createDriver
argument_list|()
decl_stmt|;
name|int
name|rc
init|=
name|driver
operator|.
name|compile
argument_list|(
name|query
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|rc
operator|!=
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|QueryPlan
name|plan
init|=
name|driver
operator|.
name|getPlan
argument_list|()
decl_stmt|;
name|DDLTask
name|task
init|=
operator|(
name|DDLTask
operator|)
name|plan
operator|.
name|getRootTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|DDLWork
name|work
init|=
name|task
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|CreateTableDesc
name|spec
init|=
operator|(
name|CreateTableDesc
operator|)
name|work
operator|.
name|getDDLDesc
argument_list|()
decl_stmt|;
name|FieldSchema
name|fs
init|=
name|spec
operator|.
name|getCols
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
return|return
name|fs
operator|.
name|getType
argument_list|()
return|;
block|}
block|}
end_class

end_unit

