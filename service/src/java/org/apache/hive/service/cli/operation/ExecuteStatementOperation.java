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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|operation
package|;
end_package

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
name|Map
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
name|processors
operator|.
name|CommandProcessor
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
name|processors
operator|.
name|CommandProcessorFactory
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
name|common
operator|.
name|util
operator|.
name|HiveStringUtils
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
name|service
operator|.
name|cli
operator|.
name|HiveSQLException
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
name|service
operator|.
name|cli
operator|.
name|OperationType
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
name|service
operator|.
name|cli
operator|.
name|session
operator|.
name|HiveSession
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|ExecuteStatementOperation
extends|extends
name|Operation
block|{
specifier|protected
name|String
name|statement
init|=
literal|null
decl_stmt|;
specifier|public
name|ExecuteStatementOperation
parameter_list|(
name|HiveSession
name|parentSession
parameter_list|,
name|String
name|statement
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
parameter_list|,
name|boolean
name|runInBackground
parameter_list|)
block|{
name|super
argument_list|(
name|parentSession
argument_list|,
name|confOverlay
argument_list|,
name|OperationType
operator|.
name|EXECUTE_STATEMENT
argument_list|)
expr_stmt|;
name|this
operator|.
name|statement
operator|=
name|statement
expr_stmt|;
block|}
specifier|public
name|String
name|getStatement
parameter_list|()
block|{
return|return
name|statement
return|;
block|}
specifier|public
specifier|static
name|ExecuteStatementOperation
name|newExecuteStatementOperation
parameter_list|(
name|HiveSession
name|parentSession
parameter_list|,
name|String
name|statement
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
parameter_list|,
name|boolean
name|runAsync
parameter_list|,
name|long
name|queryTimeout
parameter_list|)
throws|throws
name|HiveSQLException
block|{
name|String
name|cleanStatement
init|=
name|HiveStringUtils
operator|.
name|removeComments
argument_list|(
name|statement
argument_list|)
decl_stmt|;
name|String
index|[]
name|tokens
init|=
name|cleanStatement
operator|.
name|trim
argument_list|()
operator|.
name|split
argument_list|(
literal|"\\s+"
argument_list|)
decl_stmt|;
name|CommandProcessor
name|processor
init|=
literal|null
decl_stmt|;
try|try
block|{
name|processor
operator|=
name|CommandProcessorFactory
operator|.
name|getForHiveCommand
argument_list|(
name|tokens
argument_list|,
name|parentSession
operator|.
name|getHiveConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getSQLState
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|processor
operator|==
literal|null
condition|)
block|{
comment|// runAsync, queryTimeout makes sense only for a SQLOperation
comment|// Pass the original statement to SQLOperation as sql parser can remove comments by itself
return|return
operator|new
name|SQLOperation
argument_list|(
name|parentSession
argument_list|,
name|statement
argument_list|,
name|confOverlay
argument_list|,
name|runAsync
argument_list|,
name|queryTimeout
argument_list|)
return|;
block|}
return|return
operator|new
name|HiveCommandOperation
argument_list|(
name|parentSession
argument_list|,
name|cleanStatement
argument_list|,
name|processor
argument_list|,
name|confOverlay
argument_list|)
return|;
block|}
block|}
end_class

end_unit

