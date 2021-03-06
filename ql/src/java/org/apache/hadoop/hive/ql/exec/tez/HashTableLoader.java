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
name|tez
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
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
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
name|HashMap
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|llap
operator|.
name|LlapDaemonInfo
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
name|MemoryMonitorInfo
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
name|mapjoin
operator|.
name|MapJoinMemoryExhaustionError
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
name|persistence
operator|.
name|HashMapWrapper
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
name|HybridHashTableConf
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
name|HybridHashTableContainer
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
operator|.
name|Category
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
name|PrimitiveObjectInspector
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
name|StructField
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
name|StructObjectInspector
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
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|Input
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|api
operator|.
name|LogicalInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|runtime
operator|.
name|library
operator|.
name|api
operator|.
name|KeyValueReader
import|;
end_import

begin_comment
comment|/**  * HashTableLoader for Tez constructs the hashtable from records read from  * a broadcast edge.  */
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
name|Configuration
name|hconf
decl_stmt|;
specifier|private
name|MapJoinDesc
name|desc
decl_stmt|;
specifier|private
name|TezContext
name|tezContext
decl_stmt|;
specifier|private
name|String
name|cacheKey
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
name|tezContext
operator|=
operator|(
name|TezContext
operator|)
name|mrContext
expr_stmt|;
name|this
operator|.
name|hconf
operator|=
name|hconf
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
name|this
operator|.
name|cacheKey
operator|=
name|joinOp
operator|.
name|getCacheKey
argument_list|()
expr_stmt|;
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
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|parentToInput
init|=
name|desc
operator|.
name|getParentToInput
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|Long
argument_list|>
name|parentKeyCounts
init|=
name|desc
operator|.
name|getParentKeyCounts
argument_list|()
decl_stmt|;
name|boolean
name|isCrossProduct
init|=
literal|false
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|joinExprs
init|=
name|desc
operator|.
name|getKeys
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|joinExprs
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|isCrossProduct
operator|=
literal|true
expr_stmt|;
block|}
name|boolean
name|useOptimizedTables
init|=
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
name|boolean
name|useHybridGraceHashJoin
init|=
name|desc
operator|.
name|isHybridHashJoin
argument_list|()
decl_stmt|;
name|boolean
name|isFirstKey
init|=
literal|true
decl_stmt|;
comment|// Get the total available memory from memory manager
name|long
name|totalMapJoinMemory
init|=
name|desc
operator|.
name|getMemoryNeeded
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Memory manager allocates "
operator|+
name|totalMapJoinMemory
operator|+
literal|" bytes for the loading hashtable."
argument_list|)
expr_stmt|;
if|if
condition|(
name|totalMapJoinMemory
operator|<=
literal|0
condition|)
block|{
name|totalMapJoinMemory
operator|=
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVECONVERTJOINNOCONDITIONALTASKTHRESHOLD
argument_list|)
expr_stmt|;
block|}
name|long
name|processMaxMemory
init|=
name|ManagementFactory
operator|.
name|getMemoryMXBean
argument_list|()
operator|.
name|getHeapMemoryUsage
argument_list|()
operator|.
name|getMax
argument_list|()
decl_stmt|;
if|if
condition|(
name|totalMapJoinMemory
operator|>
name|processMaxMemory
condition|)
block|{
name|float
name|hashtableMemoryUsage
init|=
name|HiveConf
operator|.
name|getFloatVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHASHTABLEFOLLOWBYGBYMAXMEMORYUSAGE
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"totalMapJoinMemory value of "
operator|+
name|totalMapJoinMemory
operator|+
literal|" is greater than the max memory size of "
operator|+
name|processMaxMemory
argument_list|)
expr_stmt|;
comment|// Don't want to attempt to grab more memory than we have available .. percentage is a bit arbitrary
name|totalMapJoinMemory
operator|=
call|(
name|long
call|)
argument_list|(
name|processMaxMemory
operator|*
name|hashtableMemoryUsage
argument_list|)
expr_stmt|;
block|}
comment|// Only applicable to n-way Hybrid Grace Hash Join
name|HybridHashTableConf
name|nwayConf
init|=
literal|null
decl_stmt|;
name|long
name|totalSize
init|=
literal|0
decl_stmt|;
name|int
name|biggest
init|=
literal|0
decl_stmt|;
comment|// position of the biggest small table
name|Map
argument_list|<
name|Integer
argument_list|,
name|Long
argument_list|>
name|tableMemorySizes
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|useHybridGraceHashJoin
operator|&&
name|mapJoinTables
operator|.
name|length
operator|>
literal|2
condition|)
block|{
comment|// Create a Conf for n-way HybridHashTableContainers
name|nwayConf
operator|=
operator|new
name|HybridHashTableConf
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"N-way join: "
operator|+
operator|(
name|mapJoinTables
operator|.
name|length
operator|-
literal|1
operator|)
operator|+
literal|" small tables."
argument_list|)
expr_stmt|;
comment|// Find the biggest small table; also calculate total data size of all small tables
name|long
name|maxSize
init|=
name|Long
operator|.
name|MIN_VALUE
decl_stmt|;
comment|// the size of the biggest small table
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
condition|)
block|{
continue|continue;
block|}
name|long
name|smallTableSize
init|=
name|desc
operator|.
name|getParentDataSizes
argument_list|()
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|totalSize
operator|+=
name|smallTableSize
expr_stmt|;
if|if
condition|(
name|maxSize
operator|<
name|smallTableSize
condition|)
block|{
name|maxSize
operator|=
name|smallTableSize
expr_stmt|;
name|biggest
operator|=
name|pos
expr_stmt|;
block|}
block|}
name|tableMemorySizes
operator|=
name|divideHybridHashTableMemory
argument_list|(
name|mapJoinTables
argument_list|,
name|desc
argument_list|,
name|totalSize
argument_list|,
name|totalMapJoinMemory
argument_list|)
expr_stmt|;
comment|// Using biggest small table, calculate number of partitions to create for each small table
name|long
name|memory
init|=
name|tableMemorySizes
operator|.
name|get
argument_list|(
name|biggest
argument_list|)
decl_stmt|;
name|int
name|numPartitions
init|=
literal|0
decl_stmt|;
try|try
block|{
name|numPartitions
operator|=
name|HybridHashTableContainer
operator|.
name|calcNumPartitions
argument_list|(
name|memory
argument_list|,
name|maxSize
argument_list|,
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHYBRIDGRACEHASHJOINMINNUMPARTITIONS
argument_list|)
argument_list|,
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHYBRIDGRACEHASHJOINMINWBSIZE
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
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|nwayConf
operator|.
name|setNumberOfPartitions
argument_list|(
name|numPartitions
argument_list|)
expr_stmt|;
block|}
name|MemoryMonitorInfo
name|memoryMonitorInfo
init|=
name|desc
operator|.
name|getMemoryMonitorInfo
argument_list|()
decl_stmt|;
name|boolean
name|doMemCheck
init|=
literal|false
decl_stmt|;
name|long
name|effectiveThreshold
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|memoryMonitorInfo
operator|!=
literal|null
condition|)
block|{
name|effectiveThreshold
operator|=
name|memoryMonitorInfo
operator|.
name|getEffectiveThreshold
argument_list|(
name|desc
operator|.
name|getMaxMemoryAvailable
argument_list|()
argument_list|)
expr_stmt|;
comment|// hash table loading happens in server side, LlapDecider could kick out some fragments to run outside of LLAP.
comment|// Flip the flag at runtime in case if we are running outside of LLAP
if|if
condition|(
operator|!
name|LlapDaemonInfo
operator|.
name|INSTANCE
operator|.
name|isLlap
argument_list|()
condition|)
block|{
name|memoryMonitorInfo
operator|.
name|setLlap
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|memoryMonitorInfo
operator|.
name|doMemoryMonitoring
argument_list|()
condition|)
block|{
name|doMemCheck
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Memory monitoring for hash table loader enabled. {}"
argument_list|,
name|memoryMonitorInfo
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|doMemCheck
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Not doing hash table memory monitoring. {}"
argument_list|,
name|memoryMonitorInfo
argument_list|)
expr_stmt|;
block|}
block|}
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
condition|)
block|{
continue|continue;
block|}
name|long
name|numEntries
init|=
literal|0
decl_stmt|;
name|String
name|inputName
init|=
name|parentToInput
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|LogicalInput
name|input
init|=
name|tezContext
operator|.
name|getInput
argument_list|(
name|inputName
argument_list|)
decl_stmt|;
try|try
block|{
name|input
operator|.
name|start
argument_list|()
expr_stmt|;
name|tezContext
operator|.
name|getTezProcessorContext
argument_list|()
operator|.
name|waitForAnyInputReady
argument_list|(
name|Collections
operator|.
expr|<
name|Input
operator|>
name|singletonList
argument_list|(
name|input
argument_list|)
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
try|try
block|{
name|KeyValueReader
name|kvReader
init|=
operator|(
name|KeyValueReader
operator|)
name|input
operator|.
name|getReader
argument_list|()
decl_stmt|;
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
decl_stmt|,
name|valCtx
init|=
name|mapJoinTableSerdes
index|[
name|pos
index|]
operator|.
name|getValueContext
argument_list|()
decl_stmt|;
if|if
condition|(
name|useOptimizedTables
condition|)
block|{
name|ObjectInspector
name|keyOi
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
name|keyOi
argument_list|)
condition|)
block|{
if|if
condition|(
name|isFirstKey
condition|)
block|{
name|useOptimizedTables
operator|=
literal|false
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|describeOi
argument_list|(
literal|"Not using optimized hash table. "
operator|+
literal|"Only a subset of mapjoin keys is supported. Unsupported key: "
argument_list|,
name|keyOi
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|describeOi
argument_list|(
literal|"Only a subset of mapjoin keys is supported. Unsupported key: "
argument_list|,
name|keyOi
argument_list|)
argument_list|)
throw|;
block|}
block|}
block|}
name|isFirstKey
operator|=
literal|false
expr_stmt|;
name|Long
name|keyCountObj
init|=
name|parentKeyCounts
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|long
name|keyCount
init|=
operator|(
name|keyCountObj
operator|==
literal|null
operator|)
condition|?
operator|-
literal|1
else|:
name|keyCountObj
operator|.
name|longValue
argument_list|()
decl_stmt|;
name|long
name|memory
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|useHybridGraceHashJoin
condition|)
block|{
if|if
condition|(
name|mapJoinTables
operator|.
name|length
operator|>
literal|2
condition|)
block|{
name|memory
operator|=
name|tableMemorySizes
operator|.
name|get
argument_list|(
name|pos
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// binary join
name|memory
operator|=
name|totalMapJoinMemory
expr_stmt|;
block|}
block|}
name|MapJoinTableContainer
name|tableContainer
decl_stmt|;
if|if
condition|(
name|useOptimizedTables
condition|)
block|{
if|if
condition|(
operator|!
name|useHybridGraceHashJoin
operator|||
name|isCrossProduct
condition|)
block|{
name|tableContainer
operator|=
operator|new
name|MapJoinBytesTableContainer
argument_list|(
name|hconf
argument_list|,
name|valCtx
argument_list|,
name|keyCount
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tableContainer
operator|=
operator|new
name|HybridHashTableContainer
argument_list|(
name|hconf
argument_list|,
name|keyCount
argument_list|,
name|memory
argument_list|,
name|desc
operator|.
name|getParentDataSizes
argument_list|()
operator|.
name|get
argument_list|(
name|pos
argument_list|)
argument_list|,
name|nwayConf
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|tableContainer
operator|=
operator|new
name|HashMapWrapper
argument_list|(
name|hconf
argument_list|,
name|keyCount
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Loading hash table for input: {} cacheKey: {} tableContainer: {} smallTablePos: {}"
argument_list|,
name|inputName
argument_list|,
name|cacheKey
argument_list|,
name|tableContainer
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|pos
argument_list|)
expr_stmt|;
name|tableContainer
operator|.
name|setSerde
argument_list|(
name|keyCtx
argument_list|,
name|valCtx
argument_list|)
expr_stmt|;
while|while
condition|(
name|kvReader
operator|.
name|next
argument_list|()
condition|)
block|{
name|tableContainer
operator|.
name|putRow
argument_list|(
operator|(
name|Writable
operator|)
name|kvReader
operator|.
name|getCurrentKey
argument_list|()
argument_list|,
operator|(
name|Writable
operator|)
name|kvReader
operator|.
name|getCurrentValue
argument_list|()
argument_list|)
expr_stmt|;
name|numEntries
operator|++
expr_stmt|;
if|if
condition|(
name|doMemCheck
operator|&&
operator|(
name|numEntries
operator|%
name|memoryMonitorInfo
operator|.
name|getMemoryCheckInterval
argument_list|()
operator|==
literal|0
operator|)
condition|)
block|{
specifier|final
name|long
name|estMemUsage
init|=
name|tableContainer
operator|.
name|getEstimatedMemorySize
argument_list|()
decl_stmt|;
if|if
condition|(
name|estMemUsage
operator|>
name|effectiveThreshold
condition|)
block|{
name|String
name|msg
init|=
literal|"Hash table loading exceeded memory limits for input: "
operator|+
name|inputName
operator|+
literal|" numEntries: "
operator|+
name|numEntries
operator|+
literal|" estimatedMemoryUsage: "
operator|+
name|estMemUsage
operator|+
literal|" effectiveThreshold: "
operator|+
name|effectiveThreshold
operator|+
literal|" memoryMonitorInfo: "
operator|+
name|memoryMonitorInfo
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|MapJoinMemoryExhaustionError
argument_list|(
name|msg
argument_list|)
throw|;
block|}
else|else
block|{
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Checking hash table loader memory usage for input: {} numEntries: {} "
operator|+
literal|"estimatedMemoryUsage: {} effectiveThreshold: {}"
argument_list|,
name|inputName
argument_list|,
name|numEntries
argument_list|,
name|estMemUsage
argument_list|,
name|effectiveThreshold
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|tableContainer
operator|.
name|seal
argument_list|()
expr_stmt|;
name|mapJoinTables
index|[
name|pos
index|]
operator|=
name|tableContainer
expr_stmt|;
if|if
condition|(
name|doMemCheck
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished loading hash table for input: {} cacheKey: {} numEntries: {} estimatedMemoryUsage: {}"
argument_list|,
name|inputName
argument_list|,
name|cacheKey
argument_list|,
name|numEntries
argument_list|,
name|tableContainer
operator|.
name|getEstimatedMemorySize
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished loading hash table for input: {} cacheKey: {} numEntries: {}"
argument_list|,
name|inputName
argument_list|,
name|cacheKey
argument_list|,
name|numEntries
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
block|}
specifier|private
specifier|static
name|Map
argument_list|<
name|Integer
argument_list|,
name|Long
argument_list|>
name|divideHybridHashTableMemory
parameter_list|(
name|MapJoinTableContainer
index|[]
name|mapJoinTables
parameter_list|,
name|MapJoinDesc
name|desc
parameter_list|,
name|long
name|totalSize
parameter_list|,
name|long
name|totalHashTableMemory
parameter_list|)
block|{
name|int
name|smallTableCount
init|=
name|Math
operator|.
name|max
argument_list|(
name|mapJoinTables
operator|.
name|length
operator|-
literal|1
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|Long
argument_list|>
name|tableMemorySizes
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|Long
argument_list|>
argument_list|()
decl_stmt|;
comment|// If any table has bad size estimate, we need to fall back to sizing each table equally
name|boolean
name|fallbackToEqualProportions
init|=
name|totalSize
operator|<=
literal|0
decl_stmt|;
if|if
condition|(
operator|!
name|fallbackToEqualProportions
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Integer
argument_list|,
name|Long
argument_list|>
name|tableSizeEntry
range|:
name|desc
operator|.
name|getParentDataSizes
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|tableSizeEntry
operator|.
name|getKey
argument_list|()
operator|==
name|desc
operator|.
name|getPosBigTable
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|long
name|tableSize
init|=
name|tableSizeEntry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableSize
operator|<=
literal|0
condition|)
block|{
name|fallbackToEqualProportions
operator|=
literal|true
expr_stmt|;
break|break;
block|}
name|float
name|percentage
init|=
operator|(
name|float
operator|)
name|tableSize
operator|/
name|totalSize
decl_stmt|;
name|long
name|tableMemory
init|=
call|(
name|long
call|)
argument_list|(
name|totalHashTableMemory
operator|*
name|percentage
argument_list|)
decl_stmt|;
name|tableMemorySizes
operator|.
name|put
argument_list|(
name|tableSizeEntry
operator|.
name|getKey
argument_list|()
argument_list|,
name|tableMemory
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|fallbackToEqualProportions
condition|)
block|{
comment|// Just give each table the same amount of memory.
name|long
name|equalPortion
init|=
name|totalHashTableMemory
operator|/
name|smallTableCount
decl_stmt|;
for|for
control|(
name|Integer
name|pos
range|:
name|desc
operator|.
name|getParentDataSizes
argument_list|()
operator|.
name|keySet
argument_list|()
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
condition|)
block|{
break|break;
block|}
name|tableMemorySizes
operator|.
name|put
argument_list|(
name|pos
argument_list|,
name|equalPortion
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|tableMemorySizes
return|;
block|}
specifier|private
name|String
name|describeOi
parameter_list|(
name|String
name|desc
parameter_list|,
name|ObjectInspector
name|keyOi
parameter_list|)
block|{
for|for
control|(
name|StructField
name|field
range|:
operator|(
operator|(
name|StructObjectInspector
operator|)
name|keyOi
operator|)
operator|.
name|getAllStructFieldRefs
argument_list|()
control|)
block|{
name|ObjectInspector
name|oi
init|=
name|field
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
name|String
name|cat
init|=
name|oi
operator|.
name|getCategory
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|oi
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
name|cat
operator|=
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|oi
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|desc
operator|+=
name|field
operator|.
name|getFieldName
argument_list|()
operator|+
literal|":"
operator|+
name|cat
operator|+
literal|", "
expr_stmt|;
block|}
return|return
name|desc
return|;
block|}
block|}
end_class

end_unit

