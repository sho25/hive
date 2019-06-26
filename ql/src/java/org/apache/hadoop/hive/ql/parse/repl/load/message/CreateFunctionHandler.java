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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|ReplChangeManager
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
name|ResourceUri
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
name|function
operator|.
name|CreateFunctionDesc
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
name|FunctionUtils
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
name|ReplCopyTask
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
name|ReplicationSpec
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
name|PathBuilder
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
name|plan
operator|.
name|DependencyCollectionWork
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
name|toReadEntity
import|;
end_import

begin_class
specifier|public
class|class
name|CreateFunctionHandler
extends|extends
name|AbstractMessageHandler
block|{
specifier|private
name|String
name|functionName
decl_stmt|;
specifier|public
name|String
name|getFunctionName
parameter_list|()
block|{
return|return
name|functionName
return|;
block|}
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
try|try
block|{
name|FunctionDescBuilder
name|builder
init|=
operator|new
name|FunctionDescBuilder
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|CreateFunctionDesc
name|descToLoad
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|this
operator|.
name|functionName
operator|=
name|builder
operator|.
name|metadata
operator|.
name|function
operator|.
name|getFunctionName
argument_list|()
expr_stmt|;
name|context
operator|.
name|log
operator|.
name|debug
argument_list|(
literal|"Loading function desc : {}"
argument_list|,
name|descToLoad
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|DDLWork
argument_list|>
name|createTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DDLWork
argument_list|(
name|readEntitySet
argument_list|,
name|writeEntitySet
argument_list|,
name|descToLoad
argument_list|)
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|context
operator|.
name|log
operator|.
name|debug
argument_list|(
literal|"Added create function task : {}:{},{}"
argument_list|,
name|createTask
operator|.
name|getId
argument_list|()
argument_list|,
name|descToLoad
operator|.
name|getName
argument_list|()
argument_list|,
name|descToLoad
operator|.
name|getClassName
argument_list|()
argument_list|)
expr_stmt|;
comment|// This null check is specifically done as the same class is used to handle both incremental and
comment|// bootstrap replication scenarios for create function. When doing bootstrap we do not have
comment|// event id for this event but rather when bootstrap started and hence we pass in null dmd for
comment|// bootstrap.There should be a better way to do this but might required a lot of changes across
comment|// different handlers, unless this is a common pattern that is seen, leaving this here.
if|if
condition|(
name|context
operator|.
name|dmd
operator|!=
literal|null
condition|)
block|{
name|updatedMetadata
operator|.
name|set
argument_list|(
name|context
operator|.
name|dmd
operator|.
name|getEventTo
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|builder
operator|.
name|destinationDbName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|readEntitySet
operator|.
name|add
argument_list|(
name|toReadEntity
argument_list|(
operator|new
name|Path
argument_list|(
name|context
operator|.
name|location
argument_list|)
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|builder
operator|.
name|replCopyTasks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// reply copy only happens for jars on hdfs not otherwise.
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|createTask
argument_list|)
return|;
block|}
else|else
block|{
comment|/**          *  This is to understand how task dependencies work.          *  All root tasks are executed in parallel. For bootstrap replication there should be only one root task of creating db. Incremental can be multiple ( have to verify ).          *  Task has children, which are put in queue for execution after the parent has finished execution.          *  One -to- One dependency can be satisfied by adding children to a given task, do this recursively where the relation holds.          *  for many to one , create a barrier task that is the child of every item in 'many' dependencies, make the 'one' dependency as child of barrier task.          *  add the 'many' to parent/root tasks. The execution environment will make sure that the child barrier task will not get executed unless all parents of the barrier task are complete,          *  which should only happen when the last task is finished, at which point the child of the barrier task is picked up.          */
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|barrierTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DependencyCollectionWork
argument_list|()
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|builder
operator|.
name|replCopyTasks
operator|.
name|forEach
argument_list|(
name|t
lambda|->
name|t
operator|.
name|addDependentTask
argument_list|(
name|barrierTask
argument_list|)
argument_list|)
expr_stmt|;
name|barrierTask
operator|.
name|addDependentTask
argument_list|(
name|createTask
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|replCopyTasks
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|e
operator|instanceof
name|SemanticException
operator|)
condition|?
operator|(
name|SemanticException
operator|)
name|e
else|:
operator|new
name|SemanticException
argument_list|(
literal|"Error reading message members"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
class|class
name|FunctionDescBuilder
block|{
specifier|private
specifier|final
name|Context
name|context
decl_stmt|;
specifier|private
specifier|final
name|MetaData
name|metadata
decl_stmt|;
specifier|private
specifier|final
name|String
name|destinationDbName
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|replCopyTasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|FunctionDescBuilder
parameter_list|(
name|Context
name|context
parameter_list|)
throws|throws
name|SemanticException
block|{
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
try|try
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
operator|new
name|Path
argument_list|(
name|context
operator|.
name|location
argument_list|)
operator|.
name|toUri
argument_list|()
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|metadata
operator|=
name|EximUtil
operator|.
name|readMetaData
argument_list|(
name|fs
argument_list|,
operator|new
name|Path
argument_list|(
name|context
operator|.
name|location
argument_list|,
name|EximUtil
operator|.
name|METADATA_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
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
name|destinationDbName
operator|=
name|context
operator|.
name|isDbNameEmpty
argument_list|()
condition|?
name|metadata
operator|.
name|function
operator|.
name|getDbName
argument_list|()
else|:
name|context
operator|.
name|dbName
expr_stmt|;
block|}
specifier|private
name|CreateFunctionDesc
name|build
parameter_list|()
throws|throws
name|SemanticException
block|{
name|replCopyTasks
operator|.
name|clear
argument_list|()
expr_stmt|;
name|PrimaryToReplicaResourceFunction
name|conversionFunction
init|=
operator|new
name|PrimaryToReplicaResourceFunction
argument_list|(
name|context
argument_list|,
name|metadata
argument_list|,
name|destinationDbName
argument_list|)
decl_stmt|;
comment|// We explicitly create immutable lists here as it forces the guava lib to run the transformations
comment|// and not do them lazily. The reason being the function class used for transformations additionally
comment|// also creates the corresponding replCopyTasks, which cannot be evaluated lazily. since the query
comment|// plan needs to be complete before we execute and not modify it while execution in the driver.
name|List
argument_list|<
name|ResourceUri
argument_list|>
name|transformedUris
init|=
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|Lists
operator|.
name|transform
argument_list|(
name|metadata
operator|.
name|function
operator|.
name|getResourceUris
argument_list|()
argument_list|,
name|conversionFunction
argument_list|)
argument_list|)
decl_stmt|;
name|replCopyTasks
operator|.
name|addAll
argument_list|(
name|conversionFunction
operator|.
name|replCopyTasks
argument_list|)
expr_stmt|;
name|String
name|fullQualifiedFunctionName
init|=
name|FunctionUtils
operator|.
name|qualifyFunctionName
argument_list|(
name|metadata
operator|.
name|function
operator|.
name|getFunctionName
argument_list|()
argument_list|,
name|destinationDbName
argument_list|)
decl_stmt|;
comment|// For bootstrap load, the create function should be always performed.
comment|// Only for incremental load, need to validate if event is newer than the database.
name|ReplicationSpec
name|replSpec
init|=
operator|(
name|context
operator|.
name|dmd
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|context
operator|.
name|eventOnlyReplicationSpec
argument_list|()
decl_stmt|;
return|return
operator|new
name|CreateFunctionDesc
argument_list|(
name|fullQualifiedFunctionName
argument_list|,
name|metadata
operator|.
name|function
operator|.
name|getClassName
argument_list|()
argument_list|,
literal|false
argument_list|,
name|transformedUris
argument_list|,
name|replSpec
argument_list|)
return|;
block|}
block|}
specifier|static
class|class
name|PrimaryToReplicaResourceFunction
implements|implements
name|Function
argument_list|<
name|ResourceUri
argument_list|,
name|ResourceUri
argument_list|>
block|{
specifier|private
specifier|final
name|Context
name|context
decl_stmt|;
specifier|private
specifier|final
name|MetaData
name|metadata
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|replCopyTasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|String
name|functionsRootDir
decl_stmt|;
specifier|private
name|String
name|destinationDbName
decl_stmt|;
name|PrimaryToReplicaResourceFunction
parameter_list|(
name|Context
name|context
parameter_list|,
name|MetaData
name|metadata
parameter_list|,
name|String
name|destinationDbName
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
name|metadata
operator|=
name|metadata
expr_stmt|;
name|this
operator|.
name|destinationDbName
operator|=
name|destinationDbName
expr_stmt|;
name|this
operator|.
name|functionsRootDir
operator|=
name|context
operator|.
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPL_FUNCTIONS_ROOT_DIR
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ResourceUri
name|apply
parameter_list|(
name|ResourceUri
name|resourceUri
parameter_list|)
block|{
try|try
block|{
return|return
name|resourceUri
operator|.
name|getUri
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"hdfs:"
argument_list|)
condition|?
name|destinationResourceUri
argument_list|(
name|resourceUri
argument_list|)
else|:
name|resourceUri
return|;
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|SemanticException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * the destination also includes the current timestamp to randomise the placement of the jar at a given location for a function .      * this is done to allow the  CREATE / DROP / CREATE of the same function with same name and jar's but updated      * binaries across the two creates.      */
name|ResourceUri
name|destinationResourceUri
parameter_list|(
name|ResourceUri
name|resourceUri
parameter_list|)
throws|throws
name|IOException
throws|,
name|SemanticException
block|{
name|String
name|sourceUri
init|=
name|resourceUri
operator|.
name|getUri
argument_list|()
decl_stmt|;
name|String
index|[]
name|split
init|=
name|ReplChangeManager
operator|.
name|decodeFileUri
argument_list|(
name|sourceUri
argument_list|)
index|[
literal|0
index|]
operator|.
name|split
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
decl_stmt|;
name|PathBuilder
name|pathBuilder
init|=
operator|new
name|PathBuilder
argument_list|(
name|functionsRootDir
argument_list|)
decl_stmt|;
name|Path
name|qualifiedDestinationPath
init|=
name|PathBuilder
operator|.
name|fullyQualifiedHDFSUri
argument_list|(
name|pathBuilder
operator|.
name|addDescendant
argument_list|(
name|destinationDbName
operator|.
name|toLowerCase
argument_list|()
argument_list|)
operator|.
name|addDescendant
argument_list|(
name|metadata
operator|.
name|function
operator|.
name|getFunctionName
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
operator|.
name|addDescendant
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
argument_list|)
argument_list|)
operator|.
name|addDescendant
argument_list|(
name|split
index|[
name|split
operator|.
name|length
operator|-
literal|1
index|]
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
name|functionsRootDir
argument_list|)
operator|.
name|getFileSystem
argument_list|(
name|context
operator|.
name|hiveConf
argument_list|)
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|copyTask
init|=
name|ReplCopyTask
operator|.
name|getLoadCopyTask
argument_list|(
name|metadata
operator|.
name|getReplicationSpec
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
name|sourceUri
argument_list|)
argument_list|,
name|qualifiedDestinationPath
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|replCopyTasks
operator|.
name|add
argument_list|(
name|copyTask
argument_list|)
expr_stmt|;
name|ResourceUri
name|destinationUri
init|=
operator|new
name|ResourceUri
argument_list|(
name|resourceUri
operator|.
name|getResourceType
argument_list|()
argument_list|,
name|qualifiedDestinationPath
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|context
operator|.
name|log
operator|.
name|debug
argument_list|(
literal|"copy source uri : {} to destination uri: {}"
argument_list|,
name|sourceUri
argument_list|,
name|destinationUri
argument_list|)
expr_stmt|;
return|return
name|destinationUri
return|;
block|}
block|}
block|}
end_class

end_unit

