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
name|exec
operator|.
name|repl
operator|.
name|bootstrap
operator|.
name|load
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|Function
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
name|NoSuchObjectException
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
name|ErrorMsg
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
name|exec
operator|.
name|TaskFactory
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
name|exec
operator|.
name|repl
operator|.
name|ReplStateLogWork
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
name|exec
operator|.
name|repl
operator|.
name|util
operator|.
name|AddDependencyToLeaves
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
name|exec
operator|.
name|repl
operator|.
name|bootstrap
operator|.
name|events
operator|.
name|FunctionEvent
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
name|exec
operator|.
name|repl
operator|.
name|bootstrap
operator|.
name|load
operator|.
name|util
operator|.
name|Context
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
name|exec
operator|.
name|repl
operator|.
name|util
operator|.
name|TaskTracker
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
name|exec
operator|.
name|util
operator|.
name|DAGTraversal
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
name|metadata
operator|.
name|HiveException
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
name|SemanticException
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
name|repl
operator|.
name|ReplLogger
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
name|repl
operator|.
name|load
operator|.
name|MetaData
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
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|CreateFunctionHandler
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
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|MessageHandler
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
name|io
operator|.
name|IOException
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
name|net
operator|.
name|URI
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

begin_import
import|import static
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
operator|.
name|stripQuotes
import|;
end_import

begin_class
specifier|public
class|class
name|LoadFunction
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|LoadFunction
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Context
name|context
decl_stmt|;
specifier|private
name|ReplLogger
name|replLogger
decl_stmt|;
specifier|private
specifier|final
name|FunctionEvent
name|event
decl_stmt|;
specifier|private
specifier|final
name|String
name|dbNameToLoadIn
decl_stmt|;
specifier|private
specifier|final
name|TaskTracker
name|tracker
decl_stmt|;
specifier|public
name|LoadFunction
parameter_list|(
name|Context
name|context
parameter_list|,
name|ReplLogger
name|replLogger
parameter_list|,
name|FunctionEvent
name|event
parameter_list|,
name|String
name|dbNameToLoadIn
parameter_list|,
name|TaskTracker
name|existingTracker
parameter_list|)
block|{
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|replLogger
operator|=
name|replLogger
expr_stmt|;
name|this
operator|.
name|event
operator|=
name|event
expr_stmt|;
name|this
operator|.
name|dbNameToLoadIn
operator|=
name|dbNameToLoadIn
expr_stmt|;
name|this
operator|.
name|tracker
operator|=
operator|new
name|TaskTracker
argument_list|(
name|existingTracker
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|createFunctionReplLogTask
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|functionTasks
parameter_list|,
name|String
name|functionName
parameter_list|)
block|{
name|ReplStateLogWork
name|replLogWork
init|=
operator|new
name|ReplStateLogWork
argument_list|(
name|replLogger
argument_list|,
name|functionName
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|ReplStateLogWork
argument_list|>
name|replLogTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|replLogWork
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|DAGTraversal
operator|.
name|traverse
argument_list|(
name|functionTasks
argument_list|,
operator|new
name|AddDependencyToLeaves
argument_list|(
name|replLogTask
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TaskTracker
name|tasks
parameter_list|()
throws|throws
name|IOException
throws|,
name|SemanticException
block|{
name|URI
name|fromURI
init|=
name|EximUtil
operator|.
name|getValidatedURI
argument_list|(
name|context
operator|.
name|hiveConf
argument_list|,
name|stripQuotes
argument_list|(
name|event
operator|.
name|rootDir
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|fromPath
init|=
operator|new
name|Path
argument_list|(
name|fromURI
operator|.
name|getScheme
argument_list|()
argument_list|,
name|fromURI
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|fromURI
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|isFunctionAlreadyLoaded
argument_list|(
name|fromPath
argument_list|)
condition|)
block|{
return|return
name|tracker
return|;
block|}
name|CreateFunctionHandler
name|handler
init|=
operator|new
name|CreateFunctionHandler
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|tasks
init|=
name|handler
operator|.
name|handle
argument_list|(
operator|new
name|MessageHandler
operator|.
name|Context
argument_list|(
name|dbNameToLoadIn
argument_list|,
name|fromPath
operator|.
name|toString
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|,
name|context
operator|.
name|hiveDb
argument_list|,
name|context
operator|.
name|nestedContext
argument_list|,
name|LOG
argument_list|)
argument_list|)
decl_stmt|;
name|createFunctionReplLogTask
argument_list|(
name|tasks
argument_list|,
name|handler
operator|.
name|getFunctionName
argument_list|()
argument_list|)
expr_stmt|;
name|tasks
operator|.
name|forEach
argument_list|(
name|tracker
operator|::
name|addTask
argument_list|)
expr_stmt|;
return|return
name|tracker
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
name|ErrorMsg
operator|.
name|INVALID_PATH
operator|.
name|getMsg
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|boolean
name|isFunctionAlreadyLoaded
parameter_list|(
name|Path
name|funcDumpRoot
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|Path
name|metadataPath
init|=
operator|new
name|Path
argument_list|(
name|funcDumpRoot
argument_list|,
name|EximUtil
operator|.
name|METADATA_NAME
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|metadataPath
operator|.
name|toUri
argument_list|()
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|MetaData
name|metadata
init|=
name|EximUtil
operator|.
name|readMetaData
argument_list|(
name|fs
argument_list|,
name|metadataPath
argument_list|)
decl_stmt|;
name|Function
name|function
decl_stmt|;
try|try
block|{
name|String
name|dbName
init|=
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|dbNameToLoadIn
argument_list|)
condition|?
name|metadata
operator|.
name|function
operator|.
name|getDbName
argument_list|()
else|:
name|dbNameToLoadIn
decl_stmt|;
name|function
operator|=
name|context
operator|.
name|hiveDb
operator|.
name|getFunction
argument_list|(
name|dbName
argument_list|,
name|metadata
operator|.
name|function
operator|.
name|getFunctionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|NoSuchObjectException
condition|)
block|{
return|return
literal|false
return|;
block|}
throw|throw
name|e
throw|;
block|}
return|return
operator|(
name|function
operator|!=
literal|null
operator|)
return|;
block|}
block|}
end_class

end_unit

