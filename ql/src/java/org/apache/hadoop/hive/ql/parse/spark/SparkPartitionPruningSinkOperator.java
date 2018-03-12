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
name|spark
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedOutputStream
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
name|ObjectOutputStream
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|fs
operator|.
name|FSDataOutputStream
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
name|exec
operator|.
name|MapJoinOperator
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
name|Operator
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
name|ReduceSinkOperator
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
name|spark
operator|.
name|SparkPartitionPruningSinkDesc
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
name|ExprNodeDesc
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
name|MapWork
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
name|plan
operator|.
name|api
operator|.
name|OperatorType
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
name|serde2
operator|.
name|Serializer
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|io
operator|.
name|DataOutputBuffer
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
name|io
operator|.
name|Writable
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
name|ReflectionUtils
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

begin_comment
comment|/**  * This operator gets partition info from the upstream operators, and write them  * to HDFS. This will later be read at the driver, and used for pruning the partitions  * for the big table side.  */
end_comment

begin_class
specifier|public
class|class
name|SparkPartitionPruningSinkOperator
extends|extends
name|Operator
argument_list|<
name|SparkPartitionPruningSinkDesc
argument_list|>
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|protected
specifier|transient
name|Serializer
name|serializer
decl_stmt|;
specifier|protected
specifier|transient
name|DataOutputBuffer
name|buffer
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SparkPartitionPruningSinkOperator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|AtomicLong
name|SEQUENCE_NUM
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|transient
name|String
name|uniqueId
init|=
literal|null
decl_stmt|;
comment|/** Kryo ctor. */
annotation|@
name|VisibleForTesting
specifier|public
name|SparkPartitionPruningSinkOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|SparkPartitionPruningSinkOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|serializer
operator|=
operator|(
name|Serializer
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|conf
operator|.
name|getTable
argument_list|()
operator|.
name|getDeserializerClass
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|buffer
operator|=
operator|new
name|DataOutputBuffer
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
name|ObjectInspector
name|rowInspector
init|=
name|inputObjInspectors
index|[
literal|0
index|]
decl_stmt|;
try|try
block|{
name|Writable
name|writableRow
init|=
name|serializer
operator|.
name|serialize
argument_list|(
name|row
argument_list|,
name|rowInspector
argument_list|)
decl_stmt|;
name|writableRow
operator|.
name|write
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|closeOp
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|!
name|abort
condition|)
block|{
try|try
block|{
name|flushToFile
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
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
comment|/* This function determines whether sparkpruningsink is with mapjoin.  This will be called      to check whether the tree should be split for dpp.  For mapjoin it won't be.  Also called      to determine whether dpp should be enabled for anything other than mapjoin.    */
specifier|public
name|boolean
name|isWithMapjoin
parameter_list|()
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|branchingOp
init|=
name|this
operator|.
name|getBranchingOp
argument_list|()
decl_stmt|;
comment|// Check if this is a MapJoin. If so, do not split.
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|childOp
range|:
name|branchingOp
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
if|if
condition|(
name|childOp
operator|instanceof
name|ReduceSinkOperator
operator|&&
name|childOp
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|MapJoinOperator
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/* Locate the op where the branch starts.  This function works only for the following pattern.    *     TS1       TS2    *      |         |    *     FIL       FIL    *      |         |    *      |     ---------    *      RS    |   |   |    *      |    RS  SEL SEL    *      |    /    |   |    *      |   /    GBY GBY    *      JOIN       |  |    *                 |  SPARKPRUNINGSINK    *                 |    *              SPARKPRUNINGSINK    */
specifier|public
name|Operator
argument_list|<
name|?
argument_list|>
name|getBranchingOp
parameter_list|()
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|branchingOp
init|=
name|this
decl_stmt|;
while|while
condition|(
name|branchingOp
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|branchingOp
operator|.
name|getNumChild
argument_list|()
operator|>
literal|1
condition|)
block|{
break|break;
block|}
else|else
block|{
name|branchingOp
operator|=
name|branchingOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|branchingOp
return|;
block|}
specifier|private
name|void
name|flushToFile
parameter_list|()
throws|throws
name|IOException
block|{
comment|// write an intermediate file to the specified path
comment|// the format of the path is: tmpPath/targetWorkId/sourceWorkId/randInt
name|Path
name|path
init|=
name|conf
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|this
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|path
argument_list|)
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|path
operator|=
operator|new
name|Path
argument_list|(
name|path
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|Utilities
operator|.
name|randGen
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
break|break;
block|}
block|}
name|short
name|numOfRepl
init|=
name|fs
operator|.
name|getDefaultReplication
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|ObjectOutputStream
name|out
init|=
literal|null
decl_stmt|;
name|FSDataOutputStream
name|fsout
init|=
literal|null
decl_stmt|;
try|try
block|{
name|fsout
operator|=
name|fs
operator|.
name|create
argument_list|(
name|path
argument_list|,
name|numOfRepl
argument_list|)
expr_stmt|;
name|out
operator|=
operator|new
name|ObjectOutputStream
argument_list|(
operator|new
name|BufferedOutputStream
argument_list|(
name|fsout
argument_list|)
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|conf
operator|.
name|getTargetInfos
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|SparkPartitionPruningSinkDesc
operator|.
name|DPPTargetInfo
name|info
range|:
name|conf
operator|.
name|getTargetInfos
argument_list|()
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|info
operator|.
name|columnName
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|fs
operator|.
name|delete
argument_list|(
name|path
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception happened while trying to clean partial file."
argument_list|)
expr_stmt|;
block|}
throw|throw
name|e
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|out
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Flushed to file: "
operator|+
name|path
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fsout
operator|!=
literal|null
condition|)
block|{
name|fsout
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|OperatorType
name|getType
parameter_list|()
block|{
return|return
name|OperatorType
operator|.
name|SPARKPRUNINGSINK
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
name|SparkPartitionPruningSinkOperator
operator|.
name|getOperatorName
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|String
name|getOperatorName
parameter_list|()
block|{
return|return
literal|"SPARKPRUNINGSINK"
return|;
block|}
specifier|public
specifier|synchronized
name|String
name|getUniqueId
parameter_list|()
block|{
if|if
condition|(
name|uniqueId
operator|==
literal|null
condition|)
block|{
name|uniqueId
operator|=
name|getOperatorId
argument_list|()
operator|+
literal|"_"
operator|+
name|SEQUENCE_NUM
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
block|}
return|return
name|uniqueId
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|setUniqueId
parameter_list|(
name|String
name|uniqueId
parameter_list|)
block|{
name|this
operator|.
name|uniqueId
operator|=
name|uniqueId
expr_stmt|;
block|}
comment|/**    * Add this DPP sink as a pruning source for the target MapWork. It means the DPP sink's output    * will be used to prune a certain partition in the MapWork. The MapWork's event source maps will    * be updated to remember the DPP sink's unique ID and corresponding target columns.    */
specifier|public
name|void
name|addAsSourceEvent
parameter_list|(
name|MapWork
name|mapWork
parameter_list|,
name|ExprNodeDesc
name|partKey
parameter_list|,
name|String
name|columnName
parameter_list|,
name|String
name|columnType
parameter_list|)
block|{
name|String
name|sourceId
init|=
name|getUniqueId
argument_list|()
decl_stmt|;
name|SparkPartitionPruningSinkDesc
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
comment|// store table descriptor in map-targetWork
name|List
argument_list|<
name|TableDesc
argument_list|>
name|tableDescs
init|=
name|mapWork
operator|.
name|getEventSourceTableDescMap
argument_list|()
operator|.
name|computeIfAbsent
argument_list|(
name|sourceId
argument_list|,
name|v
lambda|->
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|tableDescs
operator|.
name|add
argument_list|(
name|conf
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
comment|// store partition key expr in map-targetWork
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|partKeys
init|=
name|mapWork
operator|.
name|getEventSourcePartKeyExprMap
argument_list|()
operator|.
name|computeIfAbsent
argument_list|(
name|sourceId
argument_list|,
name|v
lambda|->
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|partKeys
operator|.
name|add
argument_list|(
name|partKey
argument_list|)
expr_stmt|;
comment|// store column name in map-targetWork
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
name|mapWork
operator|.
name|getEventSourceColumnNameMap
argument_list|()
operator|.
name|computeIfAbsent
argument_list|(
name|sourceId
argument_list|,
name|v
lambda|->
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|columnNames
operator|.
name|add
argument_list|(
name|columnName
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columnTypes
init|=
name|mapWork
operator|.
name|getEventSourceColumnTypeMap
argument_list|()
operator|.
name|computeIfAbsent
argument_list|(
name|sourceId
argument_list|,
name|v
lambda|->
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|columnTypes
operator|.
name|add
argument_list|(
name|columnType
argument_list|)
expr_stmt|;
block|}
comment|/**    * Remove this DPP sink from the target MapWork's pruning source. The MapWork's event source maps    * will be updated to remove the association between the target column and the DPP sink's unique    * ID. If the DPP sink has no target columns after the removal, its unique ID is removed from the    * event source maps.    */
specifier|public
name|void
name|removeFromSourceEvent
parameter_list|(
name|MapWork
name|mapWork
parameter_list|,
name|ExprNodeDesc
name|partKey
parameter_list|,
name|String
name|columnName
parameter_list|,
name|String
name|columnType
parameter_list|)
block|{
name|String
name|sourceId
init|=
name|getUniqueId
argument_list|()
decl_stmt|;
name|SparkPartitionPruningSinkDesc
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|TableDesc
argument_list|>
name|tableDescs
init|=
name|mapWork
operator|.
name|getEventSourceTableDescMap
argument_list|()
operator|.
name|get
argument_list|(
name|sourceId
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableDescs
operator|!=
literal|null
condition|)
block|{
name|tableDescs
operator|.
name|remove
argument_list|(
name|conf
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|tableDescs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|mapWork
operator|.
name|getEventSourceTableDescMap
argument_list|()
operator|.
name|remove
argument_list|(
name|sourceId
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|partKeys
init|=
name|mapWork
operator|.
name|getEventSourcePartKeyExprMap
argument_list|()
operator|.
name|get
argument_list|(
name|sourceId
argument_list|)
decl_stmt|;
if|if
condition|(
name|partKeys
operator|!=
literal|null
condition|)
block|{
name|partKeys
operator|.
name|remove
argument_list|(
name|partKey
argument_list|)
expr_stmt|;
if|if
condition|(
name|partKeys
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|mapWork
operator|.
name|getEventSourcePartKeyExprMap
argument_list|()
operator|.
name|remove
argument_list|(
name|sourceId
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
name|mapWork
operator|.
name|getEventSourceColumnNameMap
argument_list|()
operator|.
name|get
argument_list|(
name|sourceId
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnNames
operator|!=
literal|null
condition|)
block|{
name|columnNames
operator|.
name|remove
argument_list|(
name|columnName
argument_list|)
expr_stmt|;
if|if
condition|(
name|columnNames
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|mapWork
operator|.
name|getEventSourceColumnNameMap
argument_list|()
operator|.
name|remove
argument_list|(
name|sourceId
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|String
argument_list|>
name|columnTypes
init|=
name|mapWork
operator|.
name|getEventSourceColumnTypeMap
argument_list|()
operator|.
name|get
argument_list|(
name|sourceId
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnTypes
operator|!=
literal|null
condition|)
block|{
name|columnTypes
operator|.
name|remove
argument_list|(
name|columnType
argument_list|)
expr_stmt|;
if|if
condition|(
name|columnTypes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|mapWork
operator|.
name|getEventSourceColumnTypeMap
argument_list|()
operator|.
name|remove
argument_list|(
name|sourceId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

