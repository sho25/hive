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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|exec
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
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
name|util
operator|.
name|List
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|HashMultimap
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
name|Multimap
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
name|PrincipalType
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
name|ResourceType
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
name|exec
operator|.
name|FunctionInfo
operator|.
name|FunctionResource
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
name|CompilationOpContext
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
name|DriverContext
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
name|QueryState
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
name|Hive
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
name|plan
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
name|plan
operator|.
name|DropFunctionDesc
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
name|CreateMacroDesc
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
name|DropMacroDesc
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
name|FunctionWork
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
name|api
operator|.
name|StageType
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
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|util
operator|.
name|ResourceDownloader
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * FunctionTask.  *  */
end_comment

begin_class
specifier|public
class|class
name|FunctionTask
extends|extends
name|Task
argument_list|<
name|FunctionWork
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
specifier|transient
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|FunctionTask
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|FunctionTask
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|QueryState
name|queryState
parameter_list|,
name|QueryPlan
name|queryPlan
parameter_list|,
name|DriverContext
name|ctx
parameter_list|,
name|CompilationOpContext
name|opContext
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|queryState
argument_list|,
name|queryPlan
argument_list|,
name|ctx
argument_list|,
name|opContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|CreateFunctionDesc
name|createFunctionDesc
init|=
name|work
operator|.
name|getCreateFunctionDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|createFunctionDesc
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|createFunctionDesc
operator|.
name|isTemp
argument_list|()
condition|)
block|{
return|return
name|createTemporaryFunction
argument_list|(
name|createFunctionDesc
argument_list|)
return|;
block|}
else|else
block|{
try|try
block|{
if|if
condition|(
name|createFunctionDesc
operator|.
name|getReplicationSpec
argument_list|()
operator|.
name|isInReplicationScope
argument_list|()
condition|)
block|{
name|String
index|[]
name|qualifiedNameParts
init|=
name|FunctionUtils
operator|.
name|getQualifiedFunctionNameParts
argument_list|(
name|createFunctionDesc
operator|.
name|getFunctionName
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|dbName
init|=
name|qualifiedNameParts
index|[
literal|0
index|]
decl_stmt|;
name|String
name|funcName
init|=
name|qualifiedNameParts
index|[
literal|1
index|]
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|dbProps
init|=
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|createFunctionDesc
operator|.
name|getReplicationSpec
argument_list|()
operator|.
name|allowEventReplacementInto
argument_list|(
name|dbProps
argument_list|)
condition|)
block|{
comment|// If the database is newer than the create event, then noop it.
name|LOG
operator|.
name|debug
argument_list|(
literal|"FunctionTask: Create Function {} is skipped as database {} "
operator|+
literal|"is newer than update"
argument_list|,
name|funcName
argument_list|,
name|dbName
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
block|}
return|return
name|createPermanentFunction
argument_list|(
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
argument_list|,
name|createFunctionDesc
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|setException
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to create function"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
block|}
block|}
name|DropFunctionDesc
name|dropFunctionDesc
init|=
name|work
operator|.
name|getDropFunctionDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|dropFunctionDesc
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|dropFunctionDesc
operator|.
name|isTemp
argument_list|()
condition|)
block|{
return|return
name|dropTemporaryFunction
argument_list|(
name|dropFunctionDesc
argument_list|)
return|;
block|}
else|else
block|{
try|try
block|{
if|if
condition|(
name|dropFunctionDesc
operator|.
name|getReplicationSpec
argument_list|()
operator|.
name|isInReplicationScope
argument_list|()
condition|)
block|{
name|String
index|[]
name|qualifiedNameParts
init|=
name|FunctionUtils
operator|.
name|getQualifiedFunctionNameParts
argument_list|(
name|dropFunctionDesc
operator|.
name|getFunctionName
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|dbName
init|=
name|qualifiedNameParts
index|[
literal|0
index|]
decl_stmt|;
name|String
name|funcName
init|=
name|qualifiedNameParts
index|[
literal|1
index|]
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|dbProps
init|=
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|dropFunctionDesc
operator|.
name|getReplicationSpec
argument_list|()
operator|.
name|allowEventReplacementInto
argument_list|(
name|dbProps
argument_list|)
condition|)
block|{
comment|// If the database is newer than the drop event, then noop it.
name|LOG
operator|.
name|debug
argument_list|(
literal|"FunctionTask: Drop Function {} is skipped as database {} "
operator|+
literal|"is newer than update"
argument_list|,
name|funcName
argument_list|,
name|dbName
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
block|}
return|return
name|dropPermanentFunction
argument_list|(
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
argument_list|,
name|dropFunctionDesc
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|setException
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to drop function"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
block|}
block|}
if|if
condition|(
name|work
operator|.
name|getReloadFunctionDesc
argument_list|()
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|reloadFunctions
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|setException
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to reload functions"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
block|}
name|CreateMacroDesc
name|createMacroDesc
init|=
name|work
operator|.
name|getCreateMacroDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|createMacroDesc
operator|!=
literal|null
condition|)
block|{
return|return
name|createMacro
argument_list|(
name|createMacroDesc
argument_list|)
return|;
block|}
name|DropMacroDesc
name|dropMacroDesc
init|=
name|work
operator|.
name|getDropMacroDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|dropMacroDesc
operator|!=
literal|null
condition|)
block|{
return|return
name|dropMacro
argument_list|(
name|dropMacroDesc
argument_list|)
return|;
block|}
return|return
literal|0
return|;
block|}
comment|// todo authorization
specifier|private
name|int
name|createPermanentFunction
parameter_list|(
name|Hive
name|db
parameter_list|,
name|CreateFunctionDesc
name|createFunctionDesc
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|String
index|[]
name|qualifiedNameParts
init|=
name|FunctionUtils
operator|.
name|getQualifiedFunctionNameParts
argument_list|(
name|createFunctionDesc
operator|.
name|getFunctionName
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|dbName
init|=
name|qualifiedNameParts
index|[
literal|0
index|]
decl_stmt|;
name|String
name|funcName
init|=
name|qualifiedNameParts
index|[
literal|1
index|]
decl_stmt|;
name|String
name|registeredName
init|=
name|FunctionUtils
operator|.
name|qualifyFunctionName
argument_list|(
name|funcName
argument_list|,
name|dbName
argument_list|)
decl_stmt|;
name|String
name|className
init|=
name|createFunctionDesc
operator|.
name|getClassName
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ResourceUri
argument_list|>
name|resources
init|=
name|createFunctionDesc
operator|.
name|getResources
argument_list|()
decl_stmt|;
comment|// For permanent functions, check for any resources from local filesystem.
name|checkLocalFunctionResources
argument_list|(
name|db
argument_list|,
name|createFunctionDesc
operator|.
name|getResources
argument_list|()
argument_list|)
expr_stmt|;
name|FunctionInfo
name|registered
init|=
literal|null
decl_stmt|;
try|try
block|{
name|registered
operator|=
name|FunctionRegistry
operator|.
name|registerPermanentFunction
argument_list|(
name|registeredName
argument_list|,
name|className
argument_list|,
literal|true
argument_list|,
name|toFunctionResource
argument_list|(
name|resources
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|ex
parameter_list|)
block|{
name|Throwable
name|t
init|=
name|ex
decl_stmt|;
while|while
condition|(
name|t
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|t
operator|=
name|t
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|registered
operator|==
literal|null
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Failed to register "
operator|+
name|registeredName
operator|+
literal|" using class "
operator|+
name|createFunctionDesc
operator|.
name|getClassName
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
comment|// Add to metastore
name|Function
name|func
init|=
operator|new
name|Function
argument_list|(
name|funcName
argument_list|,
name|dbName
argument_list|,
name|className
argument_list|,
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getUserName
argument_list|()
argument_list|,
name|PrincipalType
operator|.
name|USER
argument_list|,
call|(
name|int
call|)
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|/
literal|1000
argument_list|)
argument_list|,
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
name|FunctionType
operator|.
name|JAVA
argument_list|,
name|resources
argument_list|)
decl_stmt|;
name|db
operator|.
name|createFunction
argument_list|(
name|func
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
specifier|private
name|int
name|createTemporaryFunction
parameter_list|(
name|CreateFunctionDesc
name|createFunctionDesc
parameter_list|)
block|{
try|try
block|{
comment|// Add any required resources
name|FunctionResource
index|[]
name|resources
init|=
name|toFunctionResource
argument_list|(
name|createFunctionDesc
operator|.
name|getResources
argument_list|()
argument_list|)
decl_stmt|;
name|addFunctionResources
argument_list|(
name|resources
argument_list|)
expr_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|udfClass
init|=
name|getUdfClass
argument_list|(
name|createFunctionDesc
argument_list|)
decl_stmt|;
name|FunctionInfo
name|registered
init|=
name|FunctionRegistry
operator|.
name|registerTemporaryUDF
argument_list|(
name|createFunctionDesc
operator|.
name|getFunctionName
argument_list|()
argument_list|,
name|udfClass
argument_list|,
name|resources
argument_list|)
decl_stmt|;
if|if
condition|(
name|registered
operator|!=
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
name|console
operator|.
name|printError
argument_list|(
literal|"FAILED: Class "
operator|+
name|createFunctionDesc
operator|.
name|getClassName
argument_list|()
operator|+
literal|" does not implement UDF, GenericUDF, or UDAF"
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"FAILED: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"create function: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"FAILED: Class "
operator|+
name|createFunctionDesc
operator|.
name|getClassName
argument_list|()
operator|+
literal|" not found"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"create function: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
block|}
specifier|private
name|int
name|createMacro
parameter_list|(
name|CreateMacroDesc
name|createMacroDesc
parameter_list|)
block|{
name|FunctionRegistry
operator|.
name|registerTemporaryMacro
argument_list|(
name|createMacroDesc
operator|.
name|getMacroName
argument_list|()
argument_list|,
name|createMacroDesc
operator|.
name|getBody
argument_list|()
argument_list|,
name|createMacroDesc
operator|.
name|getColNames
argument_list|()
argument_list|,
name|createMacroDesc
operator|.
name|getColTypes
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
specifier|private
name|int
name|dropMacro
parameter_list|(
name|DropMacroDesc
name|dropMacroDesc
parameter_list|)
block|{
try|try
block|{
name|FunctionRegistry
operator|.
name|unregisterTemporaryUDF
argument_list|(
name|dropMacroDesc
operator|.
name|getMacroName
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"drop macro: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
block|}
comment|// todo authorization
specifier|private
name|int
name|dropPermanentFunction
parameter_list|(
name|Hive
name|db
parameter_list|,
name|DropFunctionDesc
name|dropFunctionDesc
parameter_list|)
block|{
try|try
block|{
name|String
index|[]
name|qualifiedNameParts
init|=
name|FunctionUtils
operator|.
name|getQualifiedFunctionNameParts
argument_list|(
name|dropFunctionDesc
operator|.
name|getFunctionName
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|dbName
init|=
name|qualifiedNameParts
index|[
literal|0
index|]
decl_stmt|;
name|String
name|funcName
init|=
name|qualifiedNameParts
index|[
literal|1
index|]
decl_stmt|;
name|String
name|registeredName
init|=
name|FunctionUtils
operator|.
name|qualifyFunctionName
argument_list|(
name|funcName
argument_list|,
name|dbName
argument_list|)
decl_stmt|;
name|FunctionRegistry
operator|.
name|unregisterPermanentFunction
argument_list|(
name|registeredName
argument_list|)
expr_stmt|;
name|db
operator|.
name|dropFunction
argument_list|(
name|dbName
argument_list|,
name|funcName
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"drop function: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
literal|"FAILED: error during drop function: "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
block|}
specifier|private
name|int
name|dropTemporaryFunction
parameter_list|(
name|DropFunctionDesc
name|dropFunctionDesc
parameter_list|)
block|{
try|try
block|{
name|FunctionRegistry
operator|.
name|unregisterTemporaryUDF
argument_list|(
name|dropFunctionDesc
operator|.
name|getFunctionName
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"drop function: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
block|}
specifier|private
name|void
name|checkLocalFunctionResources
parameter_list|(
name|Hive
name|db
parameter_list|,
name|List
argument_list|<
name|ResourceUri
argument_list|>
name|resources
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// If this is a non-local warehouse, then adding resources from the local filesystem
comment|// may mean that other clients will not be able to access the resources.
comment|// So disallow resources from local filesystem in this case.
if|if
condition|(
name|resources
operator|!=
literal|null
operator|&&
name|resources
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
try|try
block|{
name|String
name|localFsScheme
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|db
operator|.
name|getConf
argument_list|()
argument_list|)
operator|.
name|getUri
argument_list|()
operator|.
name|getScheme
argument_list|()
decl_stmt|;
name|String
name|configuredFsScheme
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|db
operator|.
name|getConf
argument_list|()
argument_list|)
operator|.
name|getUri
argument_list|()
operator|.
name|getScheme
argument_list|()
decl_stmt|;
if|if
condition|(
name|configuredFsScheme
operator|.
name|equals
argument_list|(
name|localFsScheme
argument_list|)
condition|)
block|{
comment|// Configured warehouse FS is local, don't need to bother checking.
return|return;
block|}
for|for
control|(
name|ResourceUri
name|res
range|:
name|resources
control|)
block|{
name|String
name|resUri
init|=
name|res
operator|.
name|getUri
argument_list|()
decl_stmt|;
if|if
condition|(
name|ResourceDownloader
operator|.
name|isFileUri
argument_list|(
name|resUri
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Hive warehouse is non-local, but "
operator|+
name|res
operator|.
name|getUri
argument_list|()
operator|+
literal|" specifies file on local filesystem. "
operator|+
literal|"Resources on non-local warehouse should specify a non-local scheme/path"
argument_list|)
throw|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Exception caught in checkLocalFunctionResources"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
name|FunctionResource
index|[]
name|toFunctionResource
parameter_list|(
name|List
argument_list|<
name|ResourceUri
argument_list|>
name|resources
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|resources
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|FunctionResource
index|[]
name|converted
init|=
operator|new
name|FunctionResource
index|[
name|resources
operator|.
name|size
argument_list|()
index|]
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
name|converted
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ResourceUri
name|resource
init|=
name|resources
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|SessionState
operator|.
name|ResourceType
name|type
init|=
name|getResourceType
argument_list|(
name|resource
operator|.
name|getResourceType
argument_list|()
argument_list|)
decl_stmt|;
name|converted
index|[
name|i
index|]
operator|=
operator|new
name|FunctionResource
argument_list|(
name|type
argument_list|,
name|resource
operator|.
name|getUri
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|converted
return|;
block|}
specifier|public
specifier|static
name|SessionState
operator|.
name|ResourceType
name|getResourceType
parameter_list|(
name|ResourceType
name|rt
parameter_list|)
block|{
switch|switch
condition|(
name|rt
condition|)
block|{
case|case
name|JAR
case|:
return|return
name|SessionState
operator|.
name|ResourceType
operator|.
name|JAR
return|;
case|case
name|FILE
case|:
return|return
name|SessionState
operator|.
name|ResourceType
operator|.
name|FILE
return|;
case|case
name|ARCHIVE
case|:
return|return
name|SessionState
operator|.
name|ResourceType
operator|.
name|ARCHIVE
return|;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Unexpected resource type "
operator|+
name|rt
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|void
name|addFunctionResources
parameter_list|(
name|FunctionResource
index|[]
name|resources
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|resources
operator|!=
literal|null
condition|)
block|{
name|Multimap
argument_list|<
name|SessionState
operator|.
name|ResourceType
argument_list|,
name|String
argument_list|>
name|mappings
init|=
name|HashMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
for|for
control|(
name|FunctionResource
name|res
range|:
name|resources
control|)
block|{
name|mappings
operator|.
name|put
argument_list|(
name|res
operator|.
name|getResourceType
argument_list|()
argument_list|,
name|res
operator|.
name|getResourceURI
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|SessionState
operator|.
name|ResourceType
name|type
range|:
name|mappings
operator|.
name|keys
argument_list|()
control|)
block|{
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|add_resources
argument_list|(
name|type
argument_list|,
name|mappings
operator|.
name|get
argument_list|(
name|type
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|Class
argument_list|<
name|?
argument_list|>
name|getUdfClass
parameter_list|(
name|CreateFunctionDesc
name|desc
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
comment|// get the session specified class loader from SessionState
name|ClassLoader
name|classLoader
init|=
name|Utilities
operator|.
name|getSessionSpecifiedClassLoader
argument_list|()
decl_stmt|;
return|return
name|Class
operator|.
name|forName
argument_list|(
name|desc
operator|.
name|getClassName
argument_list|()
argument_list|,
literal|true
argument_list|,
name|classLoader
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
return|return
name|StageType
operator|.
name|FUNC
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"FUNCTION"
return|;
block|}
comment|/**    * this needs access to session state resource downloads which in turn uses references to Registry objects.    */
annotation|@
name|Override
specifier|public
name|boolean
name|canExecuteInParallel
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

