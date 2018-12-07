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
name|spark
package|;
end_package

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
name|Arrays
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
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
name|HashTableSinkOperator
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
name|MapredContext
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
name|TemporaryHashSinkOperator
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
name|ExecMapperContext
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
name|MapredLocalTask
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
name|persistence
operator|.
name|MapJoinBytesTableContainer
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
name|persistence
operator|.
name|MapJoinObjectSerDeContext
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
name|persistence
operator|.
name|MapJoinTableContainer
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
name|persistence
operator|.
name|MapJoinTableContainerSerDe
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
name|vector
operator|.
name|VectorizationOperator
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
name|BucketMapJoinContext
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
name|MapJoinDesc
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
name|MapredLocalWork
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
name|OperatorDesc
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
name|SparkBucketMapJoinContext
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
name|VectorMapJoinDesc
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_comment
comment|/**  * HashTableLoader for Spark to load the hashtable for MapJoins.  */
end_comment

begin_class
specifier|public
class|class
name|HashTableLoader
implements|implements
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
name|HashTableLoader
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
name|HashTableLoader
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|ExecMapperContext
name|context
decl_stmt|;
specifier|private
name|Configuration
name|hconf
decl_stmt|;
specifier|private
name|MapJoinOperator
name|joinOp
decl_stmt|;
specifier|private
name|MapJoinDesc
name|desc
decl_stmt|;
specifier|private
name|boolean
name|useFastContainer
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|ExecMapperContext
name|context
parameter_list|,
name|MapredContext
name|mrContext
parameter_list|,
name|Configuration
name|hconf
parameter_list|,
name|MapJoinOperator
name|joinOp
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
name|hconf
operator|=
name|hconf
expr_stmt|;
name|this
operator|.
name|joinOp
operator|=
name|joinOp
expr_stmt|;
name|this
operator|.
name|desc
operator|=
name|joinOp
operator|.
name|getConf
argument_list|()
expr_stmt|;
if|if
condition|(
name|desc
operator|.
name|getVectorMode
argument_list|()
operator|&&
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_VECTORIZATION_MAPJOIN_NATIVE_FAST_HASHTABLE_ENABLED
argument_list|)
condition|)
block|{
if|if
condition|(
name|joinOp
operator|instanceof
name|VectorizationOperator
condition|)
block|{
name|VectorMapJoinDesc
name|vectorDesc
init|=
call|(
name|VectorMapJoinDesc
call|)
argument_list|(
operator|(
name|VectorizationOperator
operator|)
name|joinOp
argument_list|)
operator|.
name|getVectorDesc
argument_list|()
decl_stmt|;
name|useFastContainer
operator|=
name|vectorDesc
operator|!=
literal|null
operator|&&
name|vectorDesc
operator|.
name|getHashTableImplementationType
argument_list|()
operator|==
name|VectorMapJoinDesc
operator|.
name|HashTableImplementationType
operator|.
name|FAST
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|load
parameter_list|(
name|MapJoinTableContainer
index|[]
name|mapJoinTables
parameter_list|,
name|MapJoinTableContainerSerDe
index|[]
name|mapJoinTableSerdes
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Note: it's possible that a MJ operator is in a ReduceWork, in which case the
comment|// currentInputPath will be null. But, since currentInputPath is only interesting
comment|// for bucket join case, and for bucket join the MJ operator will always be in
comment|// a MapWork, this should be OK.
name|String
name|currentInputPath
init|=
name|context
operator|.
name|getCurrentInputPath
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|context
operator|.
name|getCurrentInputPath
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"******* Load from HashTable for input file: "
operator|+
name|currentInputPath
argument_list|)
expr_stmt|;
name|MapredLocalWork
name|localWork
init|=
name|context
operator|.
name|getLocalWork
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
name|localWork
operator|.
name|getDirectFetchOp
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|loadDirectly
argument_list|(
name|mapJoinTables
argument_list|,
name|currentInputPath
argument_list|)
expr_stmt|;
block|}
comment|// All HashTables share the same base dir,
comment|// which is passed in as the tmp path
name|Path
name|baseDir
init|=
name|localWork
operator|.
name|getTmpPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|baseDir
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|baseDir
operator|.
name|toUri
argument_list|()
argument_list|,
name|hconf
argument_list|)
decl_stmt|;
name|BucketMapJoinContext
name|mapJoinCtx
init|=
name|localWork
operator|.
name|getBucketMapjoinContext
argument_list|()
decl_stmt|;
name|boolean
name|firstContainer
init|=
literal|true
decl_stmt|;
name|boolean
name|useOptimizedContainer
init|=
operator|!
name|useFastContainer
operator|&&
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMAPJOINUSEOPTIMIZEDTABLE
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|mapJoinTables
operator|.
name|length
condition|;
name|pos
operator|++
control|)
block|{
if|if
condition|(
name|pos
operator|==
name|desc
operator|.
name|getPosBigTable
argument_list|()
operator|||
name|mapJoinTables
index|[
name|pos
index|]
operator|!=
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|useOptimizedContainer
condition|)
block|{
name|MapJoinObjectSerDeContext
name|keyCtx
init|=
name|mapJoinTableSerdes
index|[
name|pos
index|]
operator|.
name|getKeyContext
argument_list|()
decl_stmt|;
name|ObjectInspector
name|keyOI
init|=
name|keyCtx
operator|.
name|getSerDe
argument_list|()
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|MapJoinBytesTableContainer
operator|.
name|isSupportedKey
argument_list|(
name|keyOI
argument_list|)
condition|)
block|{
if|if
condition|(
name|firstContainer
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Not using optimized table container."
operator|+
literal|"Only a subset of mapjoin keys is supported."
argument_list|)
expr_stmt|;
name|useOptimizedContainer
operator|=
literal|false
expr_stmt|;
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMAPJOINUSEOPTIMIZEDTABLE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Only a subset of mapjoin keys is supported."
argument_list|)
throw|;
block|}
block|}
block|}
name|firstContainer
operator|=
literal|false
expr_stmt|;
name|String
name|bigInputPath
init|=
name|currentInputPath
decl_stmt|;
if|if
condition|(
name|currentInputPath
operator|!=
literal|null
operator|&&
name|mapJoinCtx
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|desc
operator|.
name|isBucketMapJoin
argument_list|()
condition|)
block|{
name|bigInputPath
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|aliases
init|=
operator|(
operator|(
name|SparkBucketMapJoinContext
operator|)
name|mapJoinCtx
operator|)
operator|.
name|getPosToAliasMap
argument_list|()
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|String
name|alias
init|=
name|aliases
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
comment|// Any one small table input path
name|String
name|smallInputPath
init|=
name|mapJoinCtx
operator|.
name|getAliasBucketFileNameMapping
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
operator|.
name|get
argument_list|(
name|bigInputPath
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|bigInputPath
operator|=
name|mapJoinCtx
operator|.
name|getMappingBigFile
argument_list|(
name|alias
argument_list|,
name|smallInputPath
argument_list|)
expr_stmt|;
block|}
block|}
name|String
name|fileName
init|=
name|localWork
operator|.
name|getBucketFileName
argument_list|(
name|bigInputPath
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
name|Utilities
operator|.
name|generatePath
argument_list|(
name|baseDir
argument_list|,
name|desc
operator|.
name|getDumpFilePrefix
argument_list|()
argument_list|,
operator|(
name|byte
operator|)
name|pos
argument_list|,
name|fileName
argument_list|)
decl_stmt|;
name|mapJoinTables
index|[
name|pos
index|]
operator|=
name|load
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|mapJoinTableSerdes
index|[
name|pos
index|]
argument_list|)
expr_stmt|;
block|}
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
specifier|private
name|MapJoinTableContainer
name|load
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|MapJoinTableContainerSerDe
name|mapJoinTableSerde
parameter_list|)
throws|throws
name|HiveException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"\tLoad back all hashtable files from tmp folder uri:"
operator|+
name|path
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|SparkUtilities
operator|.
name|isDedicatedCluster
argument_list|(
name|hconf
argument_list|)
condition|)
block|{
return|return
name|loadMapJoinTableContainer
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|mapJoinTableSerde
argument_list|)
return|;
block|}
try|try
block|{
return|return
name|SmallTableCache
operator|.
name|get
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|loadMapJoinTableContainer
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|mapJoinTableSerde
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ExecutionException
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
specifier|private
name|MapJoinTableContainer
name|loadMapJoinTableContainer
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|MapJoinTableContainerSerDe
name|mapJoinTableSerde
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
name|useFastContainer
condition|?
name|mapJoinTableSerde
operator|.
name|loadFastContainer
argument_list|(
name|desc
argument_list|,
name|fs
argument_list|,
name|path
argument_list|,
name|hconf
argument_list|)
else|:
name|mapJoinTableSerde
operator|.
name|load
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|hconf
argument_list|)
return|;
block|}
specifier|private
name|void
name|loadDirectly
parameter_list|(
name|MapJoinTableContainer
index|[]
name|mapJoinTables
parameter_list|,
name|String
name|inputFileName
parameter_list|)
throws|throws
name|Exception
block|{
name|MapredLocalWork
name|localWork
init|=
name|context
operator|.
name|getLocalWork
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|directWorks
init|=
name|localWork
operator|.
name|getDirectFetchOp
argument_list|()
operator|.
name|get
argument_list|(
name|joinOp
argument_list|)
decl_stmt|;
if|if
condition|(
name|directWorks
operator|==
literal|null
operator|||
name|directWorks
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|JobConf
name|job
init|=
operator|new
name|JobConf
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
name|MapredLocalTask
name|localTask
init|=
operator|new
name|MapredLocalTask
argument_list|(
name|localWork
argument_list|,
name|job
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HashTableSinkOperator
name|sink
init|=
operator|new
name|TemporaryHashSinkOperator
argument_list|(
operator|new
name|CompilationOpContext
argument_list|()
argument_list|,
name|desc
argument_list|)
decl_stmt|;
name|sink
operator|.
name|setParentOperators
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|(
name|directWorks
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|operator
range|:
name|directWorks
control|)
block|{
if|if
condition|(
name|operator
operator|!=
literal|null
condition|)
block|{
name|operator
operator|.
name|setChildOperators
argument_list|(
name|Arrays
operator|.
expr|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|>
name|asList
argument_list|(
name|sink
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|localTask
operator|.
name|setExecContext
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|localTask
operator|.
name|startForward
argument_list|(
name|inputFileName
argument_list|)
expr_stmt|;
name|MapJoinTableContainer
index|[]
name|tables
init|=
name|sink
operator|.
name|getMapJoinTables
argument_list|()
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
name|sink
operator|.
name|getNumParent
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|sink
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|mapJoinTables
index|[
name|i
index|]
operator|=
name|tables
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
name|Arrays
operator|.
name|fill
argument_list|(
name|tables
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

