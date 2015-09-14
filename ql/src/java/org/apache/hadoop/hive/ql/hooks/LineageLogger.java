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
name|hooks
package|;
end_package

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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|LinkedHashMap
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
name|Map
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
name|collections
operator|.
name|SetUtils
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
name|io
operator|.
name|output
operator|.
name|StringBuilderWriter
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
name|hive
operator|.
name|common
operator|.
name|ObjectPair
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
name|metastore
operator|.
name|api
operator|.
name|Table
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
name|exec
operator|.
name|ColumnInfo
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
name|SelectOperator
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
name|TaskRunner
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
name|hooks
operator|.
name|HookContext
operator|.
name|HookType
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
name|LineageInfo
operator|.
name|BaseColumnInfo
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
name|LineageInfo
operator|.
name|Dependency
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
name|LineageInfo
operator|.
name|Predicate
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
name|lineage
operator|.
name|LineageCtx
operator|.
name|Index
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
name|HiveOperation
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
name|session
operator|.
name|SessionState
operator|.
name|LogHelper
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|hash
operator|.
name|Hasher
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
name|hash
operator|.
name|Hashing
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|gson
operator|.
name|stream
operator|.
name|JsonWriter
import|;
end_import

begin_comment
comment|/**  * Implementation of a post execute hook that logs lineage info to a log file.  */
end_comment

begin_class
specifier|public
class|class
name|LineageLogger
implements|implements
name|ExecuteWithHookContext
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|LineageLogger
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HashSet
argument_list|<
name|String
argument_list|>
name|OPERATION_NAMES
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|OPERATION_NAMES
operator|.
name|add
argument_list|(
name|HiveOperation
operator|.
name|QUERY
operator|.
name|getOperationName
argument_list|()
argument_list|)
expr_stmt|;
name|OPERATION_NAMES
operator|.
name|add
argument_list|(
name|HiveOperation
operator|.
name|CREATETABLE_AS_SELECT
operator|.
name|getOperationName
argument_list|()
argument_list|)
expr_stmt|;
name|OPERATION_NAMES
operator|.
name|add
argument_list|(
name|HiveOperation
operator|.
name|ALTERVIEW_AS
operator|.
name|getOperationName
argument_list|()
argument_list|)
expr_stmt|;
name|OPERATION_NAMES
operator|.
name|add
argument_list|(
name|HiveOperation
operator|.
name|CREATEVIEW
operator|.
name|getOperationName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|String
name|FORMAT_VERSION
init|=
literal|"1.0"
decl_stmt|;
specifier|final
specifier|static
class|class
name|Edge
block|{
specifier|public
specifier|static
enum|enum
name|Type
block|{
name|PROJECTION
block|,
name|PREDICATE
block|}
specifier|private
name|Set
argument_list|<
name|Vertex
argument_list|>
name|sources
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|Vertex
argument_list|>
name|targets
decl_stmt|;
specifier|private
name|String
name|expr
decl_stmt|;
specifier|private
name|Type
name|type
decl_stmt|;
name|Edge
parameter_list|(
name|Set
argument_list|<
name|Vertex
argument_list|>
name|sources
parameter_list|,
name|Set
argument_list|<
name|Vertex
argument_list|>
name|targets
parameter_list|,
name|String
name|expr
parameter_list|,
name|Type
name|type
parameter_list|)
block|{
name|this
operator|.
name|sources
operator|=
name|sources
expr_stmt|;
name|this
operator|.
name|targets
operator|=
name|targets
expr_stmt|;
name|this
operator|.
name|expr
operator|=
name|expr
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
block|}
specifier|final
specifier|static
class|class
name|Vertex
block|{
specifier|public
specifier|static
enum|enum
name|Type
block|{
name|COLUMN
block|,
name|TABLE
block|}
specifier|private
name|Type
name|type
decl_stmt|;
specifier|private
name|String
name|label
decl_stmt|;
specifier|private
name|int
name|id
decl_stmt|;
name|Vertex
parameter_list|(
name|String
name|label
parameter_list|)
block|{
name|this
argument_list|(
name|label
argument_list|,
name|Type
operator|.
name|COLUMN
argument_list|)
expr_stmt|;
block|}
name|Vertex
parameter_list|(
name|String
name|label
parameter_list|,
name|Type
name|type
parameter_list|)
block|{
name|this
operator|.
name|label
operator|=
name|label
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|label
operator|.
name|hashCode
argument_list|()
operator|+
name|type
operator|.
name|hashCode
argument_list|()
operator|*
literal|3
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|Vertex
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Vertex
name|vertex
init|=
operator|(
name|Vertex
operator|)
name|obj
decl_stmt|;
return|return
name|label
operator|.
name|equals
argument_list|(
name|vertex
operator|.
name|label
argument_list|)
operator|&&
name|type
operator|==
name|vertex
operator|.
name|type
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|HookContext
name|hookContext
parameter_list|)
block|{
assert|assert
operator|(
name|hookContext
operator|.
name|getHookType
argument_list|()
operator|==
name|HookType
operator|.
name|POST_EXEC_HOOK
operator|)
assert|;
name|QueryPlan
name|plan
init|=
name|hookContext
operator|.
name|getQueryPlan
argument_list|()
decl_stmt|;
name|Index
name|index
init|=
name|hookContext
operator|.
name|getIndex
argument_list|()
decl_stmt|;
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|ss
operator|!=
literal|null
operator|&&
name|index
operator|!=
literal|null
operator|&&
name|OPERATION_NAMES
operator|.
name|contains
argument_list|(
name|plan
operator|.
name|getOperationName
argument_list|()
argument_list|)
condition|)
block|{
try|try
block|{
name|StringBuilderWriter
name|out
init|=
operator|new
name|StringBuilderWriter
argument_list|(
literal|1024
argument_list|)
decl_stmt|;
name|JsonWriter
name|writer
init|=
operator|new
name|JsonWriter
argument_list|(
name|out
argument_list|)
decl_stmt|;
name|String
name|queryStr
init|=
name|plan
operator|.
name|getQueryStr
argument_list|()
operator|.
name|trim
argument_list|()
decl_stmt|;
name|writer
operator|.
name|beginObject
argument_list|()
expr_stmt|;
name|writer
operator|.
name|name
argument_list|(
literal|"version"
argument_list|)
operator|.
name|value
argument_list|(
name|FORMAT_VERSION
argument_list|)
expr_stmt|;
name|HiveConf
name|conf
init|=
name|ss
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|boolean
name|testMode
init|=
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_IN_TEST
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|testMode
condition|)
block|{
comment|// Don't emit user/timestamp info in test mode,
comment|// so that the test golden output file is fixed.
name|long
name|queryTime
init|=
name|plan
operator|.
name|getQueryStartTime
argument_list|()
operator|.
name|longValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryTime
operator|==
literal|0
condition|)
name|queryTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|long
name|duration
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|queryTime
decl_stmt|;
name|writer
operator|.
name|name
argument_list|(
literal|"user"
argument_list|)
operator|.
name|value
argument_list|(
name|hookContext
operator|.
name|getUgi
argument_list|()
operator|.
name|getUserName
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|name
argument_list|(
literal|"timestamp"
argument_list|)
operator|.
name|value
argument_list|(
name|queryTime
operator|/
literal|1000
argument_list|)
expr_stmt|;
name|writer
operator|.
name|name
argument_list|(
literal|"duration"
argument_list|)
operator|.
name|value
argument_list|(
name|duration
argument_list|)
expr_stmt|;
name|writer
operator|.
name|name
argument_list|(
literal|"jobIds"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|beginArray
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|TaskRunner
argument_list|>
name|tasks
init|=
name|hookContext
operator|.
name|getCompleteTaskList
argument_list|()
decl_stmt|;
if|if
condition|(
name|tasks
operator|!=
literal|null
operator|&&
operator|!
name|tasks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|TaskRunner
name|task
range|:
name|tasks
control|)
block|{
name|String
name|jobId
init|=
name|task
operator|.
name|getTask
argument_list|()
operator|.
name|getJobID
argument_list|()
decl_stmt|;
if|if
condition|(
name|jobId
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|value
argument_list|(
name|jobId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|writer
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|writer
operator|.
name|name
argument_list|(
literal|"engine"
argument_list|)
operator|.
name|value
argument_list|(
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
name|HIVE_EXECUTION_ENGINE
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|name
argument_list|(
literal|"hash"
argument_list|)
operator|.
name|value
argument_list|(
name|getQueryHash
argument_list|(
name|queryStr
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|name
argument_list|(
literal|"queryText"
argument_list|)
operator|.
name|value
argument_list|(
name|queryStr
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Edge
argument_list|>
name|edges
init|=
name|getEdges
argument_list|(
name|plan
argument_list|,
name|index
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Vertex
argument_list|>
name|vertices
init|=
name|getVertices
argument_list|(
name|edges
argument_list|)
decl_stmt|;
name|writeEdges
argument_list|(
name|writer
argument_list|,
name|edges
argument_list|)
expr_stmt|;
name|writeVertices
argument_list|(
name|writer
argument_list|,
name|vertices
argument_list|)
expr_stmt|;
name|writer
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Log the lineage info
name|String
name|lineage
init|=
name|out
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|testMode
condition|)
block|{
comment|// Log to console
name|log
argument_list|(
name|lineage
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// In non-test mode, emit to a log file,
comment|// which can be different from the normal hive.log.
comment|// For example, using NoDeleteRollingFileAppender to
comment|// log to some file with different rolling policy.
name|LOG
operator|.
name|info
argument_list|(
name|lineage
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// Don't fail the query just because of any lineage issue.
name|log
argument_list|(
literal|"Failed to log lineage graph, query is not affected\n"
operator|+
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
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Log an error to console if available.    */
specifier|private
name|void
name|log
parameter_list|(
name|String
name|error
parameter_list|)
block|{
name|LogHelper
name|console
init|=
name|SessionState
operator|.
name|getConsole
argument_list|()
decl_stmt|;
if|if
condition|(
name|console
operator|!=
literal|null
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
name|error
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Based on the final select operator, find out all the target columns.    * For each target column, find out its sources based on the dependency index.    */
specifier|private
name|List
argument_list|<
name|Edge
argument_list|>
name|getEdges
parameter_list|(
name|QueryPlan
name|plan
parameter_list|,
name|Index
name|index
parameter_list|)
block|{
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ObjectPair
argument_list|<
name|SelectOperator
argument_list|,
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
name|Table
argument_list|>
argument_list|>
name|finalSelOps
init|=
name|index
operator|.
name|getFinalSelectOps
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Vertex
argument_list|>
name|allTargets
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|Vertex
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Vertex
argument_list|>
name|allSources
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Vertex
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Edge
argument_list|>
name|edges
init|=
operator|new
name|ArrayList
argument_list|<
name|Edge
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ObjectPair
argument_list|<
name|SelectOperator
argument_list|,
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
name|Table
argument_list|>
name|pair
range|:
name|finalSelOps
operator|.
name|values
argument_list|()
control|)
block|{
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fieldSchemas
init|=
name|plan
operator|.
name|getResultSchema
argument_list|()
operator|.
name|getFieldSchemas
argument_list|()
decl_stmt|;
name|SelectOperator
name|finalSelOp
init|=
name|pair
operator|.
name|getFirst
argument_list|()
decl_stmt|;
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
name|Table
name|t
init|=
name|pair
operator|.
name|getSecond
argument_list|()
decl_stmt|;
name|String
name|destTableName
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colNames
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|t
operator|!=
literal|null
condition|)
block|{
name|destTableName
operator|=
name|t
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|t
operator|.
name|getTableName
argument_list|()
expr_stmt|;
name|fieldSchemas
operator|=
name|t
operator|.
name|getCols
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// Based on the plan outputs, find out the target table name and column names.
for|for
control|(
name|WriteEntity
name|output
range|:
name|plan
operator|.
name|getOutputs
argument_list|()
control|)
block|{
name|Entity
operator|.
name|Type
name|entityType
init|=
name|output
operator|.
name|getType
argument_list|()
decl_stmt|;
if|if
condition|(
name|entityType
operator|==
name|Entity
operator|.
name|Type
operator|.
name|TABLE
operator|||
name|entityType
operator|==
name|Entity
operator|.
name|Type
operator|.
name|PARTITION
condition|)
block|{
name|t
operator|=
name|output
operator|.
name|getTable
argument_list|()
expr_stmt|;
name|destTableName
operator|=
name|t
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|t
operator|.
name|getTableName
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
init|=
name|t
operator|.
name|getCols
argument_list|()
decl_stmt|;
if|if
condition|(
name|cols
operator|!=
literal|null
operator|&&
operator|!
name|cols
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|colNames
operator|=
name|Utilities
operator|.
name|getColumnNamesFromFieldSchema
argument_list|(
name|cols
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
block|}
block|}
name|int
name|fields
init|=
name|fieldSchemas
operator|.
name|size
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|ColumnInfo
argument_list|,
name|Dependency
argument_list|>
name|colMap
init|=
name|index
operator|.
name|getDependencies
argument_list|(
name|finalSelOp
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Dependency
argument_list|>
name|dependencies
init|=
name|colMap
operator|!=
literal|null
condition|?
name|Lists
operator|.
name|newArrayList
argument_list|(
name|colMap
operator|.
name|values
argument_list|()
argument_list|)
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|dependencies
operator|==
literal|null
operator|||
name|dependencies
operator|.
name|size
argument_list|()
operator|!=
name|fields
condition|)
block|{
name|log
argument_list|(
literal|"Result schema has "
operator|+
name|fields
operator|+
literal|" fields, but we don't get as many dependencies"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Go through each target column, generate the lineage edges.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fields
condition|;
name|i
operator|++
control|)
block|{
name|Vertex
name|target
init|=
operator|new
name|Vertex
argument_list|(
name|getTargetFieldName
argument_list|(
name|i
argument_list|,
name|destTableName
argument_list|,
name|colNames
argument_list|,
name|fieldSchemas
argument_list|)
argument_list|)
decl_stmt|;
name|allTargets
operator|.
name|add
argument_list|(
name|target
argument_list|)
expr_stmt|;
name|Dependency
name|dep
init|=
name|dependencies
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|String
name|expr
init|=
name|dep
operator|.
name|getExpr
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Vertex
argument_list|>
name|sources
init|=
name|createSourceVertices
argument_list|(
name|allSources
argument_list|,
name|dep
operator|.
name|getBaseCols
argument_list|()
argument_list|)
decl_stmt|;
name|Edge
name|edge
init|=
name|findSimilarEdgeBySources
argument_list|(
name|edges
argument_list|,
name|sources
argument_list|,
name|expr
argument_list|,
name|Edge
operator|.
name|Type
operator|.
name|PROJECTION
argument_list|)
decl_stmt|;
if|if
condition|(
name|edge
operator|==
literal|null
condition|)
block|{
name|Set
argument_list|<
name|Vertex
argument_list|>
name|targets
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|Vertex
argument_list|>
argument_list|()
decl_stmt|;
name|targets
operator|.
name|add
argument_list|(
name|target
argument_list|)
expr_stmt|;
name|edges
operator|.
name|add
argument_list|(
operator|new
name|Edge
argument_list|(
name|sources
argument_list|,
name|targets
argument_list|,
name|expr
argument_list|,
name|Edge
operator|.
name|Type
operator|.
name|PROJECTION
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|edge
operator|.
name|targets
operator|.
name|add
argument_list|(
name|target
argument_list|)
expr_stmt|;
block|}
block|}
name|Set
argument_list|<
name|Predicate
argument_list|>
name|conds
init|=
name|index
operator|.
name|getPredicates
argument_list|(
name|finalSelOp
argument_list|)
decl_stmt|;
if|if
condition|(
name|conds
operator|!=
literal|null
operator|&&
operator|!
name|conds
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|Predicate
name|cond
range|:
name|conds
control|)
block|{
name|String
name|expr
init|=
name|cond
operator|.
name|getExpr
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Vertex
argument_list|>
name|sources
init|=
name|createSourceVertices
argument_list|(
name|allSources
argument_list|,
name|cond
operator|.
name|getBaseCols
argument_list|()
argument_list|)
decl_stmt|;
name|Edge
name|edge
init|=
name|findSimilarEdgeByTargets
argument_list|(
name|edges
argument_list|,
name|allTargets
argument_list|,
name|expr
argument_list|,
name|Edge
operator|.
name|Type
operator|.
name|PREDICATE
argument_list|)
decl_stmt|;
if|if
condition|(
name|edge
operator|==
literal|null
condition|)
block|{
name|edges
operator|.
name|add
argument_list|(
operator|new
name|Edge
argument_list|(
name|sources
argument_list|,
name|allTargets
argument_list|,
name|expr
argument_list|,
name|Edge
operator|.
name|Type
operator|.
name|PREDICATE
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|edge
operator|.
name|sources
operator|.
name|addAll
argument_list|(
name|sources
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
return|return
name|edges
return|;
block|}
comment|/**    * Convert a list of columns to a set of vertices.    * Use cached vertices if possible.    */
specifier|private
name|Set
argument_list|<
name|Vertex
argument_list|>
name|createSourceVertices
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Vertex
argument_list|>
name|srcVertexCache
parameter_list|,
name|Collection
argument_list|<
name|BaseColumnInfo
argument_list|>
name|baseCols
parameter_list|)
block|{
name|Set
argument_list|<
name|Vertex
argument_list|>
name|sources
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|Vertex
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|baseCols
operator|!=
literal|null
operator|&&
operator|!
name|baseCols
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|BaseColumnInfo
name|col
range|:
name|baseCols
control|)
block|{
name|Table
name|table
init|=
name|col
operator|.
name|getTabAlias
argument_list|()
operator|.
name|getTable
argument_list|()
decl_stmt|;
if|if
condition|(
name|table
operator|.
name|isTemporary
argument_list|()
condition|)
block|{
comment|// Ignore temporary tables
continue|continue;
block|}
name|Vertex
operator|.
name|Type
name|type
init|=
name|Vertex
operator|.
name|Type
operator|.
name|TABLE
decl_stmt|;
name|String
name|tableName
init|=
name|table
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|table
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|FieldSchema
name|fieldSchema
init|=
name|col
operator|.
name|getColumn
argument_list|()
decl_stmt|;
name|String
name|label
init|=
name|tableName
decl_stmt|;
if|if
condition|(
name|fieldSchema
operator|!=
literal|null
condition|)
block|{
name|type
operator|=
name|Vertex
operator|.
name|Type
operator|.
name|COLUMN
expr_stmt|;
name|label
operator|=
name|tableName
operator|+
literal|"."
operator|+
name|fieldSchema
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
name|sources
operator|.
name|add
argument_list|(
name|getOrCreateVertex
argument_list|(
name|srcVertexCache
argument_list|,
name|label
argument_list|,
name|type
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|sources
return|;
block|}
comment|/**    * Find a vertex from a cache, or create one if not.    */
specifier|private
name|Vertex
name|getOrCreateVertex
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Vertex
argument_list|>
name|vertices
parameter_list|,
name|String
name|label
parameter_list|,
name|Vertex
operator|.
name|Type
name|type
parameter_list|)
block|{
name|Vertex
name|vertex
init|=
name|vertices
operator|.
name|get
argument_list|(
name|label
argument_list|)
decl_stmt|;
if|if
condition|(
name|vertex
operator|==
literal|null
condition|)
block|{
name|vertex
operator|=
operator|new
name|Vertex
argument_list|(
name|label
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|vertices
operator|.
name|put
argument_list|(
name|label
argument_list|,
name|vertex
argument_list|)
expr_stmt|;
block|}
return|return
name|vertex
return|;
block|}
comment|/**    * Find an edge that has the same type, expression, and sources.    */
specifier|private
name|Edge
name|findSimilarEdgeBySources
parameter_list|(
name|List
argument_list|<
name|Edge
argument_list|>
name|edges
parameter_list|,
name|Set
argument_list|<
name|Vertex
argument_list|>
name|sources
parameter_list|,
name|String
name|expr
parameter_list|,
name|Edge
operator|.
name|Type
name|type
parameter_list|)
block|{
for|for
control|(
name|Edge
name|edge
range|:
name|edges
control|)
block|{
if|if
condition|(
name|edge
operator|.
name|type
operator|==
name|type
operator|&&
name|StringUtils
operator|.
name|equals
argument_list|(
name|edge
operator|.
name|expr
argument_list|,
name|expr
argument_list|)
operator|&&
name|SetUtils
operator|.
name|isEqualSet
argument_list|(
name|edge
operator|.
name|sources
argument_list|,
name|sources
argument_list|)
condition|)
block|{
return|return
name|edge
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Find an edge that has the same type, expression, and targets.    */
specifier|private
name|Edge
name|findSimilarEdgeByTargets
parameter_list|(
name|List
argument_list|<
name|Edge
argument_list|>
name|edges
parameter_list|,
name|Set
argument_list|<
name|Vertex
argument_list|>
name|targets
parameter_list|,
name|String
name|expr
parameter_list|,
name|Edge
operator|.
name|Type
name|type
parameter_list|)
block|{
for|for
control|(
name|Edge
name|edge
range|:
name|edges
control|)
block|{
if|if
condition|(
name|edge
operator|.
name|type
operator|==
name|type
operator|&&
name|StringUtils
operator|.
name|equals
argument_list|(
name|edge
operator|.
name|expr
argument_list|,
name|expr
argument_list|)
operator|&&
name|SetUtils
operator|.
name|isEqualSet
argument_list|(
name|edge
operator|.
name|targets
argument_list|,
name|targets
argument_list|)
condition|)
block|{
return|return
name|edge
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Generate normalized name for a given target column.    */
specifier|private
name|String
name|getTargetFieldName
parameter_list|(
name|int
name|fieldIndex
parameter_list|,
name|String
name|destTableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colNames
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fieldSchemas
parameter_list|)
block|{
name|String
name|fieldName
init|=
name|fieldSchemas
operator|.
name|get
argument_list|(
name|fieldIndex
argument_list|)
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
index|[]
name|parts
init|=
name|fieldName
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
decl_stmt|;
if|if
condition|(
name|destTableName
operator|!=
literal|null
condition|)
block|{
name|String
name|colName
init|=
name|parts
index|[
name|parts
operator|.
name|length
operator|-
literal|1
index|]
decl_stmt|;
if|if
condition|(
name|colNames
operator|!=
literal|null
operator|&&
operator|!
name|colNames
operator|.
name|contains
argument_list|(
name|colName
argument_list|)
condition|)
block|{
name|colName
operator|=
name|colNames
operator|.
name|get
argument_list|(
name|fieldIndex
argument_list|)
expr_stmt|;
block|}
return|return
name|destTableName
operator|+
literal|"."
operator|+
name|colName
return|;
block|}
if|if
condition|(
name|parts
operator|.
name|length
operator|==
literal|2
operator|&&
name|parts
index|[
literal|0
index|]
operator|.
name|startsWith
argument_list|(
literal|"_u"
argument_list|)
condition|)
block|{
return|return
name|parts
index|[
literal|1
index|]
return|;
block|}
return|return
name|fieldName
return|;
block|}
comment|/**    * Get all the vertices of all edges. Targets at first,    * then sources. Assign id to each vertex.    */
specifier|private
name|Set
argument_list|<
name|Vertex
argument_list|>
name|getVertices
parameter_list|(
name|List
argument_list|<
name|Edge
argument_list|>
name|edges
parameter_list|)
block|{
name|Set
argument_list|<
name|Vertex
argument_list|>
name|vertices
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|Vertex
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Edge
name|edge
range|:
name|edges
control|)
block|{
name|vertices
operator|.
name|addAll
argument_list|(
name|edge
operator|.
name|targets
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Edge
name|edge
range|:
name|edges
control|)
block|{
name|vertices
operator|.
name|addAll
argument_list|(
name|edge
operator|.
name|sources
argument_list|)
expr_stmt|;
block|}
comment|// Assign ids to all vertices,
comment|// targets at first, then sources.
name|int
name|id
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Vertex
name|vertex
range|:
name|vertices
control|)
block|{
name|vertex
operator|.
name|id
operator|=
name|id
operator|++
expr_stmt|;
block|}
return|return
name|vertices
return|;
block|}
comment|/**    * Write out an JSON array of edges.    */
specifier|private
name|void
name|writeEdges
parameter_list|(
name|JsonWriter
name|writer
parameter_list|,
name|List
argument_list|<
name|Edge
argument_list|>
name|edges
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|.
name|name
argument_list|(
literal|"edges"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|beginArray
argument_list|()
expr_stmt|;
for|for
control|(
name|Edge
name|edge
range|:
name|edges
control|)
block|{
name|writer
operator|.
name|beginObject
argument_list|()
expr_stmt|;
name|writer
operator|.
name|name
argument_list|(
literal|"sources"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|beginArray
argument_list|()
expr_stmt|;
for|for
control|(
name|Vertex
name|vertex
range|:
name|edge
operator|.
name|sources
control|)
block|{
name|writer
operator|.
name|value
argument_list|(
name|vertex
operator|.
name|id
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|writer
operator|.
name|name
argument_list|(
literal|"targets"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|beginArray
argument_list|()
expr_stmt|;
for|for
control|(
name|Vertex
name|vertex
range|:
name|edge
operator|.
name|targets
control|)
block|{
name|writer
operator|.
name|value
argument_list|(
name|vertex
operator|.
name|id
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|endArray
argument_list|()
expr_stmt|;
if|if
condition|(
name|edge
operator|.
name|expr
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|name
argument_list|(
literal|"expression"
argument_list|)
operator|.
name|value
argument_list|(
name|edge
operator|.
name|expr
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|name
argument_list|(
literal|"edgeType"
argument_list|)
operator|.
name|value
argument_list|(
name|edge
operator|.
name|type
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|writer
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
comment|/**    * Write out an JSON array of vertices.    */
specifier|private
name|void
name|writeVertices
parameter_list|(
name|JsonWriter
name|writer
parameter_list|,
name|Set
argument_list|<
name|Vertex
argument_list|>
name|vertices
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|.
name|name
argument_list|(
literal|"vertices"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|beginArray
argument_list|()
expr_stmt|;
for|for
control|(
name|Vertex
name|vertex
range|:
name|vertices
control|)
block|{
name|writer
operator|.
name|beginObject
argument_list|()
expr_stmt|;
name|writer
operator|.
name|name
argument_list|(
literal|"id"
argument_list|)
operator|.
name|value
argument_list|(
name|vertex
operator|.
name|id
argument_list|)
expr_stmt|;
name|writer
operator|.
name|name
argument_list|(
literal|"vertexType"
argument_list|)
operator|.
name|value
argument_list|(
name|vertex
operator|.
name|type
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|name
argument_list|(
literal|"vertexId"
argument_list|)
operator|.
name|value
argument_list|(
name|vertex
operator|.
name|label
argument_list|)
expr_stmt|;
name|writer
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|writer
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
comment|/**    * Generate query string md5 hash.    */
specifier|private
name|String
name|getQueryHash
parameter_list|(
name|String
name|queryStr
parameter_list|)
block|{
name|Hasher
name|hasher
init|=
name|Hashing
operator|.
name|md5
argument_list|()
operator|.
name|newHasher
argument_list|()
decl_stmt|;
name|hasher
operator|.
name|putString
argument_list|(
name|queryStr
argument_list|)
expr_stmt|;
return|return
name|hasher
operator|.
name|hash
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

