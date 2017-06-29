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
operator|.
name|repl
operator|.
name|load
operator|.
name|message
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
name|ql
operator|.
name|exec
operator|.
name|Task
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
name|EximUtil
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
name|ImportSemanticAnalyzer
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
name|SemanticException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|List
import|;
end_import

begin_class
specifier|public
class|class
name|TableHandler
extends|extends
name|AbstractMessageHandler
block|{
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|handle
parameter_list|(
name|Context
name|context
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// Path being passed to us is a table dump location. We go ahead and load it in as needed.
comment|// If tblName is null, then we default to the table name specified in _metadata, which is good.
comment|// or are both specified, in which case, that's what we are intended to create the new table as.
if|if
condition|(
name|context
operator|.
name|isDbNameEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Database name cannot be null for a table load"
argument_list|)
throw|;
block|}
try|try
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|importTasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|EximUtil
operator|.
name|SemanticAnalyzerWrapperContext
name|x
init|=
operator|new
name|EximUtil
operator|.
name|SemanticAnalyzerWrapperContext
argument_list|(
name|context
operator|.
name|hiveConf
argument_list|,
name|context
operator|.
name|db
argument_list|,
name|readEntitySet
argument_list|,
name|writeEntitySet
argument_list|,
name|importTasks
argument_list|,
name|context
operator|.
name|log
argument_list|,
name|context
operator|.
name|nestedContext
argument_list|)
decl_stmt|;
comment|// REPL LOAD is not partition level. It is always DB or table level. So, passing null for partition specs.
comment|// Also, REPL LOAD doesn't support external table and hence no location set as well.
name|ImportSemanticAnalyzer
operator|.
name|prepareImport
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
operator|(
name|context
operator|.
name|precursor
operator|!=
literal|null
operator|)
argument_list|,
literal|null
argument_list|,
name|context
operator|.
name|tableName
argument_list|,
name|context
operator|.
name|dbName
argument_list|,
literal|null
argument_list|,
name|context
operator|.
name|location
argument_list|,
name|x
argument_list|,
name|databasesUpdated
argument_list|,
name|tablesUpdated
argument_list|)
expr_stmt|;
return|return
name|importTasks
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

