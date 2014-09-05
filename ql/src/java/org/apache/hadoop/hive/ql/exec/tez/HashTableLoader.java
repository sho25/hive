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
name|MapJoinDesc
name|desc
decl_stmt|;
specifier|private
name|MapJoinKey
name|lastKey
init|=
literal|null
decl_stmt|;
specifier|private
name|int
name|rowCount
init|=
literal|0
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
name|isFirstKey
init|=
literal|true
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
name|MapJoinTableContainer
name|tableContainer
init|=
name|useOptimizedTables
condition|?
operator|new
name|MapJoinBytesTableContainer
argument_list|(
name|hconf
argument_list|,
name|valCtx
argument_list|,
name|keyCount
argument_list|)
else|:
operator|new
name|HashMapWrapper
argument_list|(
name|hconf
argument_list|,
name|keyCount
argument_list|)
decl_stmt|;
while|while
condition|(
name|kvReader
operator|.
name|next
argument_list|()
condition|)
block|{
name|rowCount
operator|++
expr_stmt|;
name|lastKey
operator|=
name|tableContainer
operator|.
name|putRow
argument_list|(
name|keyCtx
argument_list|,
operator|(
name|Writable
operator|)
name|kvReader
operator|.
name|getCurrentKey
argument_list|()
argument_list|,
name|valCtx
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Is this a bucket map join: "
operator|+
name|desc
operator|.
name|isBucketMapJoin
argument_list|()
argument_list|)
expr_stmt|;
comment|// cache is disabled for bucket map join because of the same reason
comment|// given in loadHashTable in MapJoinOperator.
if|if
condition|(
operator|!
name|desc
operator|.
name|isBucketMapJoin
argument_list|()
condition|)
block|{
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
block|}
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

