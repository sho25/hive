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
name|vector
operator|.
name|mapjoin
operator|.
name|fast
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
name|Collections
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
name|tez
operator|.
name|TezContext
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
name|SerDeException
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
name|BytesWritable
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
name|VectorMapJoinFastHashTableLoader
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
name|VectorMapJoinFastHashTableLoader
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
specifier|protected
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
name|VectorMapJoinFastTableContainer
name|vectorMapJoinFastTableContainer
init|=
operator|new
name|VectorMapJoinFastTableContainer
argument_list|(
name|desc
argument_list|,
name|hconf
argument_list|,
name|keyCount
argument_list|)
decl_stmt|;
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
name|vectorMapJoinFastTableContainer
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
name|vectorMapJoinFastTableContainer
operator|.
name|setSerde
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// No SerDes here.
while|while
condition|(
name|kvReader
operator|.
name|next
argument_list|()
condition|)
block|{
name|vectorMapJoinFastTableContainer
operator|.
name|putRow
argument_list|(
operator|(
name|BytesWritable
operator|)
name|kvReader
operator|.
name|getCurrentKey
argument_list|()
argument_list|,
operator|(
name|BytesWritable
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
name|vectorMapJoinFastTableContainer
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
name|vectorMapJoinFastTableContainer
operator|.
name|seal
argument_list|()
expr_stmt|;
name|mapJoinTables
index|[
name|pos
index|]
operator|=
name|vectorMapJoinFastTableContainer
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
literal|"Finished loading hash table for input: {} cacheKey: {} numEntries: {} "
operator|+
literal|"estimatedMemoryUsage: {}"
argument_list|,
name|inputName
argument_list|,
name|cacheKey
argument_list|,
name|numEntries
argument_list|,
name|vectorMapJoinFastTableContainer
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
catch|catch
parameter_list|(
name|SerDeException
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
block|}
end_class

end_unit

