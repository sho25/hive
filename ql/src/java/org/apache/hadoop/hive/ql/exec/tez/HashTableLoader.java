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
name|MapJoinKeyObject
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
name|LazyFlatRowContainer
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
name|MapJoinKey
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
name|ByteStream
operator|.
name|Output
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
name|lazybinary
operator|.
name|LazyBinarySerDe
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
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MapJoinOperator
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
name|MapJoinDesc
name|desc
decl_stmt|;
specifier|private
name|MapJoinKey
name|lastKey
init|=
literal|null
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
name|desc
operator|=
name|joinOp
operator|.
name|getConf
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
name|TezContext
name|tezContext
init|=
operator|(
name|TezContext
operator|)
name|MapredContext
operator|.
name|get
argument_list|()
decl_stmt|;
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
name|int
name|hashTableThreshold
init|=
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
name|HIVEHASHTABLETHRESHOLD
argument_list|)
decl_stmt|;
name|float
name|hashTableLoadFactor
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
name|HIVEHASHTABLELOADFACTOR
argument_list|)
decl_stmt|;
name|boolean
name|useLazyRows
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
name|HIVEMAPJOINLAZYHASHTABLE
argument_list|)
decl_stmt|;
name|TezCacheAccess
name|tezCacheAccess
init|=
name|TezCacheAccess
operator|.
name|createInstance
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
comment|// We only check if we can use optimized keys here; that is ok because we don't
comment|// create optimized keys in MapJoin if hash map doesn't have optimized keys.
if|if
condition|(
operator|!
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
name|HIVEMAPJOINUSEOPTIMIZEDKEYS
argument_list|)
condition|)
block|{
name|lastKey
operator|=
operator|new
name|MapJoinKeyObject
argument_list|()
expr_stmt|;
block|}
name|Output
name|output
init|=
operator|new
name|Output
argument_list|()
decl_stmt|;
comment|// Reusable output for serialization.
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
name|MapJoinTableContainer
name|tableContainer
init|=
operator|new
name|HashMapWrapper
argument_list|(
name|hashTableThreshold
argument_list|,
name|hashTableLoadFactor
argument_list|)
decl_stmt|;
comment|// simply read all the kv pairs into the hashtable.
while|while
condition|(
name|kvReader
operator|.
name|next
argument_list|()
condition|)
block|{
comment|// We pass key in as reference, to find out quickly if optimized keys can be used.
comment|// However, we do not reuse the object since we are putting them into the hashmap.
name|lastKey
operator|=
name|MapJoinKey
operator|.
name|read
argument_list|(
name|output
argument_list|,
name|lastKey
argument_list|,
name|mapJoinTableSerdes
index|[
name|pos
index|]
operator|.
name|getKeyContext
argument_list|()
argument_list|,
operator|(
name|Writable
operator|)
name|kvReader
operator|.
name|getCurrentKey
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|LazyFlatRowContainer
name|values
init|=
operator|(
name|LazyFlatRowContainer
operator|)
name|tableContainer
operator|.
name|get
argument_list|(
name|lastKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|values
operator|==
literal|null
condition|)
block|{
name|values
operator|=
operator|new
name|LazyFlatRowContainer
argument_list|()
expr_stmt|;
name|tableContainer
operator|.
name|put
argument_list|(
name|lastKey
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
name|values
operator|.
name|add
argument_list|(
name|mapJoinTableSerdes
index|[
name|pos
index|]
operator|.
name|getValueContext
argument_list|()
argument_list|,
operator|(
name|BytesWritable
operator|)
name|kvReader
operator|.
name|getCurrentValue
argument_list|()
argument_list|,
name|useLazyRows
argument_list|)
expr_stmt|;
block|}
name|mapJoinTables
index|[
name|pos
index|]
operator|=
name|tableContainer
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
comment|// Register that the Input has been cached.
name|tezCacheAccess
operator|.
name|registerCachedInput
argument_list|(
name|inputName
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting Input: "
operator|+
name|inputName
operator|+
literal|" as cached"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lastKey
operator|==
literal|null
condition|)
block|{
name|lastKey
operator|=
operator|new
name|MapJoinKeyObject
argument_list|()
expr_stmt|;
comment|// No rows in tables, the key type doesn't matter.
block|}
block|}
block|}
end_class

end_unit

