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
name|parse
package|;
end_package

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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashSet
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
name|Set
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|Warehouse
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
name|ql
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
name|ColumnStatsTask
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
name|FetchTask
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
name|StatsTask
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
name|Utilities
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
name|mr
operator|.
name|ExecDriver
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
name|hooks
operator|.
name|ReadEntity
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
name|hooks
operator|.
name|WriteEntity
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
name|optimizer
operator|.
name|GenMapRedUtils
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
name|ColumnStatsDesc
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
name|ColumnStatsWork
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
name|plan
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
name|plan
operator|.
name|FetchWork
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
name|LoadFileDesc
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
name|LoadTableDesc
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
name|MoveWork
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
name|PlanUtils
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
name|TableDesc
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
operator|.
name|LogHelper
import|;
end_import

begin_comment
comment|/**  * TaskCompiler is a the base class for classes that compile  * operator pipelines into tasks.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|TaskCompiler
block|{
specifier|protected
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TaskCompiler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|Hive
name|db
decl_stmt|;
specifier|protected
name|LogHelper
name|console
decl_stmt|;
specifier|protected
name|HiveConf
name|conf
decl_stmt|;
specifier|public
name|void
name|init
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|LogHelper
name|console
parameter_list|,
name|Hive
name|db
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|db
operator|=
name|db
expr_stmt|;
name|this
operator|.
name|console
operator|=
name|console
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"nls"
block|,
literal|"unchecked"
block|}
argument_list|)
specifier|public
name|void
name|compile
parameter_list|(
specifier|final
name|ParseContext
name|pCtx
parameter_list|,
specifier|final
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|,
specifier|final
name|HashSet
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
specifier|final
name|HashSet
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Context
name|ctx
init|=
name|pCtx
operator|.
name|getContext
argument_list|()
decl_stmt|;
name|GlobalLimitCtx
name|globalLimitCtx
init|=
name|pCtx
operator|.
name|getGlobalLimitCtx
argument_list|()
decl_stmt|;
name|QB
name|qb
init|=
name|pCtx
operator|.
name|getQB
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|MoveWork
argument_list|>
argument_list|>
name|mvTask
init|=
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|MoveWork
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|LoadTableDesc
argument_list|>
name|loadTableWork
init|=
name|pCtx
operator|.
name|getLoadTableWork
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|LoadFileDesc
argument_list|>
name|loadFileWork
init|=
name|pCtx
operator|.
name|getLoadFileWork
argument_list|()
decl_stmt|;
name|boolean
name|isCStats
init|=
name|qb
operator|.
name|isAnalyzeRewrite
argument_list|()
decl_stmt|;
if|if
condition|(
name|pCtx
operator|.
name|getFetchTask
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return;
block|}
name|optimizeOperatorPlan
argument_list|(
name|pCtx
argument_list|,
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
comment|/*      * In case of a select, use a fetch task instead of a move task.      * If the select is from analyze table column rewrite, don't create a fetch task. Instead create      * a column stats task later.      */
if|if
condition|(
name|pCtx
operator|.
name|getQB
argument_list|()
operator|.
name|getIsQuery
argument_list|()
operator|&&
operator|!
name|isCStats
condition|)
block|{
if|if
condition|(
operator|(
operator|!
name|loadTableWork
operator|.
name|isEmpty
argument_list|()
operator|)
operator|||
operator|(
name|loadFileWork
operator|.
name|size
argument_list|()
operator|!=
literal|1
operator|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|GENERIC_ERROR
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
name|LoadFileDesc
name|loadFileDesc
init|=
name|loadFileWork
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|cols
init|=
name|loadFileDesc
operator|.
name|getColumns
argument_list|()
decl_stmt|;
name|String
name|colTypes
init|=
name|loadFileDesc
operator|.
name|getColumnTypes
argument_list|()
decl_stmt|;
name|TableDesc
name|resultTab
init|=
name|pCtx
operator|.
name|getFetchTabledesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|resultTab
operator|==
literal|null
condition|)
block|{
name|String
name|resFileFormat
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYRESULTFILEFORMAT
argument_list|)
decl_stmt|;
name|resultTab
operator|=
name|PlanUtils
operator|.
name|getDefaultQueryOutputTableDesc
argument_list|(
name|cols
argument_list|,
name|colTypes
argument_list|,
name|resFileFormat
argument_list|)
expr_stmt|;
block|}
name|FetchWork
name|fetch
init|=
operator|new
name|FetchWork
argument_list|(
name|loadFileDesc
operator|.
name|getSourcePath
argument_list|()
argument_list|,
name|resultTab
argument_list|,
name|qb
operator|.
name|getParseInfo
argument_list|()
operator|.
name|getOuterQueryLimit
argument_list|()
argument_list|)
decl_stmt|;
name|fetch
operator|.
name|setSource
argument_list|(
name|pCtx
operator|.
name|getFetchSource
argument_list|()
argument_list|)
expr_stmt|;
name|fetch
operator|.
name|setSink
argument_list|(
name|pCtx
operator|.
name|getFetchSink
argument_list|()
argument_list|)
expr_stmt|;
name|pCtx
operator|.
name|setFetchTask
argument_list|(
operator|(
name|FetchTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
name|fetch
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
comment|// For the FetchTask, the limit optimization requires we fetch all the rows
comment|// in memory and count how many rows we get. It's not practical if the
comment|// limit factor is too big
name|int
name|fetchLimit
init|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVELIMITOPTMAXFETCH
argument_list|)
decl_stmt|;
if|if
condition|(
name|globalLimitCtx
operator|.
name|isEnable
argument_list|()
operator|&&
name|globalLimitCtx
operator|.
name|getGlobalLimit
argument_list|()
operator|>
name|fetchLimit
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"For FetchTask, LIMIT "
operator|+
name|globalLimitCtx
operator|.
name|getGlobalLimit
argument_list|()
operator|+
literal|"> "
operator|+
name|fetchLimit
operator|+
literal|". Doesn't qualify limit optimiztion."
argument_list|)
expr_stmt|;
name|globalLimitCtx
operator|.
name|disableOpt
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|qb
operator|.
name|getParseInfo
argument_list|()
operator|.
name|getOuterQueryLimit
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// Believe it or not, some tools do generate queries with limit 0 and than expect
comment|// query to run quickly. Lets meet their requirement.
name|LOG
operator|.
name|info
argument_list|(
literal|"Limit 0. No query execution needed."
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|isCStats
condition|)
block|{
for|for
control|(
name|LoadTableDesc
name|ltd
range|:
name|loadTableWork
control|)
block|{
name|Task
argument_list|<
name|MoveWork
argument_list|>
name|tsk
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|MoveWork
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|ltd
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|mvTask
operator|.
name|add
argument_list|(
name|tsk
argument_list|)
expr_stmt|;
comment|// Check to see if we are stale'ing any indexes and auto-update them if we want
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEINDEXAUTOUPDATE
argument_list|)
condition|)
block|{
name|IndexUpdater
name|indexUpdater
init|=
operator|new
name|IndexUpdater
argument_list|(
name|loadTableWork
argument_list|,
name|inputs
argument_list|,
name|conf
argument_list|)
decl_stmt|;
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
name|indexUpdateTasks
init|=
name|indexUpdater
operator|.
name|generateUpdateTasks
argument_list|()
decl_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|updateTask
range|:
name|indexUpdateTasks
control|)
block|{
name|tsk
operator|.
name|addDependentTask
argument_list|(
name|updateTask
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"WARNING: could not auto-update stale indexes, which are not in sync"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|boolean
name|oneLoadFile
init|=
literal|true
decl_stmt|;
for|for
control|(
name|LoadFileDesc
name|lfd
range|:
name|loadFileWork
control|)
block|{
if|if
condition|(
name|qb
operator|.
name|isCTAS
argument_list|()
condition|)
block|{
assert|assert
operator|(
name|oneLoadFile
operator|)
assert|;
comment|// should not have more than 1 load file for
comment|// CTAS
comment|// make the movetask's destination directory the table's destination.
name|Path
name|location
decl_stmt|;
name|String
name|loc
init|=
name|qb
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getLocation
argument_list|()
decl_stmt|;
if|if
condition|(
name|loc
operator|==
literal|null
condition|)
block|{
comment|// get the table's default location
name|Path
name|targetPath
decl_stmt|;
try|try
block|{
name|String
index|[]
name|names
init|=
name|Utilities
operator|.
name|getDbTableName
argument_list|(
name|qb
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|db
operator|.
name|databaseExists
argument_list|(
name|names
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"ERROR: The database "
operator|+
name|names
index|[
literal|0
index|]
operator|+
literal|" does not exist."
argument_list|)
throw|;
block|}
name|Warehouse
name|wh
init|=
operator|new
name|Warehouse
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|targetPath
operator|=
name|wh
operator|.
name|getTablePath
argument_list|(
name|db
operator|.
name|getDatabase
argument_list|(
name|names
index|[
literal|0
index|]
argument_list|)
argument_list|,
name|names
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
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
catch|catch
parameter_list|(
name|MetaException
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
name|location
operator|=
name|targetPath
expr_stmt|;
block|}
else|else
block|{
name|location
operator|=
operator|new
name|Path
argument_list|(
name|loc
argument_list|)
expr_stmt|;
block|}
name|lfd
operator|.
name|setTargetDir
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|oneLoadFile
operator|=
literal|false
expr_stmt|;
block|}
name|mvTask
operator|.
name|add
argument_list|(
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|MoveWork
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|lfd
argument_list|,
literal|false
argument_list|)
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|generateTaskTree
argument_list|(
name|rootTasks
argument_list|,
name|pCtx
argument_list|,
name|mvTask
argument_list|,
name|inputs
argument_list|,
name|outputs
argument_list|)
expr_stmt|;
comment|/*      * If the query was the result of analyze table column compute statistics rewrite, create      * a column stats task instead of a fetch task to persist stats to the metastore.      */
if|if
condition|(
name|isCStats
condition|)
block|{
name|genColumnStatsTask
argument_list|(
name|qb
argument_list|,
name|loadTableWork
argument_list|,
name|loadFileWork
argument_list|,
name|rootTasks
argument_list|)
expr_stmt|;
block|}
comment|// For each task, set the key descriptor for the reducer
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|rootTask
range|:
name|rootTasks
control|)
block|{
name|GenMapRedUtils
operator|.
name|setKeyAndValueDescForTaskTree
argument_list|(
name|rootTask
argument_list|)
expr_stmt|;
block|}
comment|// If a task contains an operator which instructs bucketizedhiveinputformat
comment|// to be used, please do so
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|rootTask
range|:
name|rootTasks
control|)
block|{
name|setInputFormat
argument_list|(
name|rootTask
argument_list|)
expr_stmt|;
block|}
name|optimizeTaskPlan
argument_list|(
name|rootTasks
argument_list|,
name|pCtx
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|decideExecMode
argument_list|(
name|rootTasks
argument_list|,
name|ctx
argument_list|,
name|globalLimitCtx
argument_list|)
expr_stmt|;
if|if
condition|(
name|qb
operator|.
name|isCTAS
argument_list|()
condition|)
block|{
comment|// generate a DDL task and make it a dependent task of the leaf
name|CreateTableDesc
name|crtTblDesc
init|=
name|qb
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
name|crtTblDesc
operator|.
name|validate
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// clear the mapredWork output file from outputs for CTAS
comment|// DDLWork at the tail of the chain will have the output
name|Iterator
argument_list|<
name|WriteEntity
argument_list|>
name|outIter
init|=
name|outputs
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|outIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
switch|switch
condition|(
name|outIter
operator|.
name|next
argument_list|()
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|DFS_DIR
case|:
case|case
name|LOCAL_DIR
case|:
name|outIter
operator|.
name|remove
argument_list|()
expr_stmt|;
break|break;
default|default:
break|break;
block|}
block|}
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|crtTblTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DDLWork
argument_list|(
name|inputs
argument_list|,
name|outputs
argument_list|,
name|crtTblDesc
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
comment|// find all leaf tasks and make the DDLTask as a dependent task of all of
comment|// them
name|HashSet
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|leaves
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|getLeafTasks
argument_list|(
name|rootTasks
argument_list|,
name|leaves
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|leaves
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|)
assert|;
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
range|:
name|leaves
control|)
block|{
if|if
condition|(
name|task
operator|instanceof
name|StatsTask
condition|)
block|{
comment|// StatsTask require table to already exist
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|parentOfStatsTask
range|:
name|task
operator|.
name|getParentTasks
argument_list|()
control|)
block|{
name|parentOfStatsTask
operator|.
name|addDependentTask
argument_list|(
name|crtTblTask
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|parentOfCrtTblTask
range|:
name|crtTblTask
operator|.
name|getParentTasks
argument_list|()
control|)
block|{
name|parentOfCrtTblTask
operator|.
name|removeDependentTask
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
name|crtTblTask
operator|.
name|addDependentTask
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|task
operator|.
name|addDependentTask
argument_list|(
name|crtTblTask
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|globalLimitCtx
operator|.
name|isEnable
argument_list|()
operator|&&
name|pCtx
operator|.
name|getFetchTask
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"set least row check for FetchTask: "
operator|+
name|globalLimitCtx
operator|.
name|getGlobalLimit
argument_list|()
argument_list|)
expr_stmt|;
name|pCtx
operator|.
name|getFetchTask
argument_list|()
operator|.
name|getWork
argument_list|()
operator|.
name|setLeastNumRows
argument_list|(
name|globalLimitCtx
operator|.
name|getGlobalLimit
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|globalLimitCtx
operator|.
name|isEnable
argument_list|()
operator|&&
name|globalLimitCtx
operator|.
name|getLastReduceLimitDesc
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"set least row check for LimitDesc: "
operator|+
name|globalLimitCtx
operator|.
name|getGlobalLimit
argument_list|()
argument_list|)
expr_stmt|;
name|globalLimitCtx
operator|.
name|getLastReduceLimitDesc
argument_list|()
operator|.
name|setLeastRows
argument_list|(
name|globalLimitCtx
operator|.
name|getGlobalLimit
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ExecDriver
argument_list|>
name|mrTasks
init|=
name|Utilities
operator|.
name|getMRTasks
argument_list|(
name|rootTasks
argument_list|)
decl_stmt|;
for|for
control|(
name|ExecDriver
name|tsk
range|:
name|mrTasks
control|)
block|{
name|tsk
operator|.
name|setRetryCmdWhenFail
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * A helper function to generate a column stats task on top of map-red task. The column stats    * task fetches from the output of the map-red task, constructs the column stats object and    * persists it to the metastore.    *    * This method generates a plan with a column stats task on top of map-red task and sets up the    * appropriate metadata to be used during execution.    *    * @param qb    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|protected
name|void
name|genColumnStatsTask
parameter_list|(
name|QB
name|qb
parameter_list|,
name|List
argument_list|<
name|LoadTableDesc
argument_list|>
name|loadTableWork
parameter_list|,
name|List
argument_list|<
name|LoadFileDesc
argument_list|>
name|loadFileWork
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|)
block|{
name|QBParseInfo
name|qbParseInfo
init|=
name|qb
operator|.
name|getParseInfo
argument_list|()
decl_stmt|;
name|ColumnStatsTask
name|cStatsTask
init|=
literal|null
decl_stmt|;
name|ColumnStatsWork
name|cStatsWork
init|=
literal|null
decl_stmt|;
name|FetchWork
name|fetch
init|=
literal|null
decl_stmt|;
name|String
name|tableName
init|=
name|qbParseInfo
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colName
init|=
name|qbParseInfo
operator|.
name|getColName
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colType
init|=
name|qbParseInfo
operator|.
name|getColType
argument_list|()
decl_stmt|;
name|boolean
name|isTblLevel
init|=
name|qbParseInfo
operator|.
name|isTblLvl
argument_list|()
decl_stmt|;
name|String
name|cols
init|=
name|loadFileWork
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getColumns
argument_list|()
decl_stmt|;
name|String
name|colTypes
init|=
name|loadFileWork
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getColumnTypes
argument_list|()
decl_stmt|;
name|String
name|resFileFormat
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYRESULTFILEFORMAT
argument_list|)
decl_stmt|;
name|TableDesc
name|resultTab
init|=
name|PlanUtils
operator|.
name|getDefaultQueryOutputTableDesc
argument_list|(
name|cols
argument_list|,
name|colTypes
argument_list|,
name|resFileFormat
argument_list|)
decl_stmt|;
name|fetch
operator|=
operator|new
name|FetchWork
argument_list|(
name|loadFileWork
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getSourcePath
argument_list|()
argument_list|,
name|resultTab
argument_list|,
name|qb
operator|.
name|getParseInfo
argument_list|()
operator|.
name|getOuterQueryLimit
argument_list|()
argument_list|)
expr_stmt|;
name|ColumnStatsDesc
name|cStatsDesc
init|=
operator|new
name|ColumnStatsDesc
argument_list|(
name|tableName
argument_list|,
name|colName
argument_list|,
name|colType
argument_list|,
name|isTblLevel
argument_list|)
decl_stmt|;
name|cStatsWork
operator|=
operator|new
name|ColumnStatsWork
argument_list|(
name|fetch
argument_list|,
name|cStatsDesc
argument_list|)
expr_stmt|;
name|cStatsTask
operator|=
operator|(
name|ColumnStatsTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
name|cStatsWork
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|cStatsTask
argument_list|)
expr_stmt|;
block|}
comment|/**    * Find all leaf tasks of the list of root tasks.    */
specifier|protected
name|void
name|getLeafTasks
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|,
name|HashSet
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|leaves
parameter_list|)
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|root
range|:
name|rootTasks
control|)
block|{
name|getLeafTasks
argument_list|(
name|root
argument_list|,
name|leaves
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|getLeafTasks
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|,
name|HashSet
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|leaves
parameter_list|)
block|{
if|if
condition|(
name|task
operator|.
name|getDependentTasks
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|leaves
operator|.
name|contains
argument_list|(
name|task
argument_list|)
condition|)
block|{
name|leaves
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|getLeafTasks
argument_list|(
name|task
operator|.
name|getDependentTasks
argument_list|()
argument_list|,
name|leaves
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*    * Called to transform tasks into local tasks where possible/desirable    */
specifier|protected
specifier|abstract
name|void
name|decideExecMode
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|,
name|Context
name|ctx
parameter_list|,
name|GlobalLimitCtx
name|globalLimitCtx
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
comment|/*    * Called at the beginning of the compile phase to have another chance to optimize the operator plan    */
specifier|protected
name|void
name|optimizeOperatorPlan
parameter_list|(
name|ParseContext
name|pCtxSet
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
block|{   }
comment|/*    * Called after the tasks have been generated to run another round of optimization    */
specifier|protected
specifier|abstract
name|void
name|optimizeTaskPlan
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|,
name|ParseContext
name|pCtx
parameter_list|,
name|Context
name|ctx
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
comment|/*    * Called to set the appropriate input format for tasks    */
specifier|protected
specifier|abstract
name|void
name|setInputFormat
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|rootTask
parameter_list|)
function_decl|;
comment|/*    * Called to generate the taks tree from the parse context/operator tree    */
specifier|protected
specifier|abstract
name|void
name|generateTaskTree
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|,
name|ParseContext
name|pCtx
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|MoveWork
argument_list|>
argument_list|>
name|mvTask
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
comment|/**    * Create a clone of the parse context    */
specifier|public
name|ParseContext
name|getParseContext
parameter_list|(
name|ParseContext
name|pCtx
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|)
block|{
name|ParseContext
name|clone
init|=
operator|new
name|ParseContext
argument_list|(
name|conf
argument_list|,
name|pCtx
operator|.
name|getQB
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getParseTree
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getOpToPartPruner
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getOpToPartList
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getTopOps
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getTopSelOps
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getOpParseCtx
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getJoinContext
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getSmbMapJoinContext
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getTopToTable
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getTopToProps
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getFsopToTable
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getLoadTableWork
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getLoadFileWork
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getContext
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getIdToTableNameMap
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getDestTableId
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getUCtx
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getListMapJoinOpsNoReducer
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getGroupOpToInputTables
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getPrunedPartitions
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getOpToSamplePruner
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getGlobalLimitCtx
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getNameToSplitSample
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getSemanticInputs
argument_list|()
argument_list|,
name|rootTasks
argument_list|,
name|pCtx
operator|.
name|getOpToPartToSkewedPruner
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getViewAliasToInput
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getReduceSinkOperatorsAddedByEnforceBucketingSorting
argument_list|()
argument_list|,
name|pCtx
operator|.
name|getQueryProperties
argument_list|()
argument_list|)
decl_stmt|;
name|clone
operator|.
name|setFetchTask
argument_list|(
name|pCtx
operator|.
name|getFetchTask
argument_list|()
argument_list|)
expr_stmt|;
name|clone
operator|.
name|setLineageInfo
argument_list|(
name|pCtx
operator|.
name|getLineageInfo
argument_list|()
argument_list|)
expr_stmt|;
name|clone
operator|.
name|setMapJoinContext
argument_list|(
name|pCtx
operator|.
name|getMapJoinContext
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|clone
return|;
block|}
block|}
end_class

end_unit

