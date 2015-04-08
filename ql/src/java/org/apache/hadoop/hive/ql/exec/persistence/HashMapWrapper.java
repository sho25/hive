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
name|persistence
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
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|ExprNodeEvaluator
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
name|JoinUtil
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
name|VectorHashKeyWrapper
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
name|VectorHashKeyWrapperBatch
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
name|expressions
operator|.
name|VectorExpressionWriter
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

begin_comment
comment|/**  * Simple wrapper for persistent Hashmap implementing only the put/get/remove/clear interface. The  * main memory hash table acts as a cache and all put/get will operate on it first. If the size of  * the main memory hash table exceeds a certain threshold, new elements will go into the persistent  * hash table.  */
end_comment

begin_class
specifier|public
class|class
name|HashMapWrapper
extends|extends
name|AbstractMapJoinTableContainer
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HashMapWrapper
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// default threshold for using main memory based HashMap
specifier|private
specifier|static
specifier|final
name|int
name|THRESHOLD
init|=
literal|1000000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|float
name|LOADFACTOR
init|=
literal|0.75f
decl_stmt|;
specifier|private
specifier|final
name|HashMap
argument_list|<
name|MapJoinKey
argument_list|,
name|MapJoinRowContainer
argument_list|>
name|mHash
decl_stmt|;
comment|// main memory HashMap
specifier|private
name|MapJoinKey
name|lastKey
init|=
literal|null
decl_stmt|;
specifier|private
name|Output
name|output
init|=
operator|new
name|Output
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Reusable output for serialization
specifier|public
name|HashMapWrapper
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|metaData
parameter_list|)
block|{
name|super
argument_list|(
name|metaData
argument_list|)
expr_stmt|;
name|int
name|threshold
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|metaData
operator|.
name|get
argument_list|(
name|THESHOLD_NAME
argument_list|)
argument_list|)
decl_stmt|;
name|float
name|loadFactor
init|=
name|Float
operator|.
name|parseFloat
argument_list|(
name|metaData
operator|.
name|get
argument_list|(
name|LOAD_NAME
argument_list|)
argument_list|)
decl_stmt|;
name|mHash
operator|=
operator|new
name|HashMap
argument_list|<
name|MapJoinKey
argument_list|,
name|MapJoinRowContainer
argument_list|>
argument_list|(
name|threshold
argument_list|,
name|loadFactor
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HashMapWrapper
parameter_list|()
block|{
name|this
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHASHTABLEKEYCOUNTADJUSTMENT
operator|.
name|defaultFloatVal
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHASHTABLETHRESHOLD
operator|.
name|defaultIntVal
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHASHTABLELOADFACTOR
operator|.
name|defaultFloatVal
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HashMapWrapper
parameter_list|(
name|Configuration
name|hconf
parameter_list|,
name|long
name|keyCount
parameter_list|)
block|{
name|this
argument_list|(
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
name|HIVEHASHTABLEKEYCOUNTADJUSTMENT
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
name|HIVEHASHTABLETHRESHOLD
argument_list|)
argument_list|,
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
argument_list|,
name|keyCount
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HashMapWrapper
parameter_list|(
name|float
name|keyCountAdj
parameter_list|,
name|int
name|threshold
parameter_list|,
name|float
name|loadFactor
parameter_list|,
name|long
name|keyCount
parameter_list|)
block|{
name|super
argument_list|(
name|createConstructorMetaData
argument_list|(
name|threshold
argument_list|,
name|loadFactor
argument_list|)
argument_list|)
expr_stmt|;
name|threshold
operator|=
name|calculateTableSize
argument_list|(
name|keyCountAdj
argument_list|,
name|threshold
argument_list|,
name|loadFactor
argument_list|,
name|keyCount
argument_list|)
expr_stmt|;
name|mHash
operator|=
operator|new
name|HashMap
argument_list|<
name|MapJoinKey
argument_list|,
name|MapJoinRowContainer
argument_list|>
argument_list|(
name|threshold
argument_list|,
name|loadFactor
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|int
name|calculateTableSize
parameter_list|(
name|float
name|keyCountAdj
parameter_list|,
name|int
name|threshold
parameter_list|,
name|float
name|loadFactor
parameter_list|,
name|long
name|keyCount
parameter_list|)
block|{
if|if
condition|(
name|keyCount
operator|>=
literal|0
operator|&&
name|keyCountAdj
operator|!=
literal|0
condition|)
block|{
comment|// We have statistics for the table. Size appropriately.
name|threshold
operator|=
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
name|keyCount
operator|/
operator|(
name|keyCountAdj
operator|*
name|loadFactor
operator|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Key count from statistics is "
operator|+
name|keyCount
operator|+
literal|"; setting map size to "
operator|+
name|threshold
argument_list|)
expr_stmt|;
return|return
name|threshold
return|;
block|}
annotation|@
name|Override
specifier|public
name|MapJoinRowContainer
name|get
parameter_list|(
name|MapJoinKey
name|key
parameter_list|)
block|{
return|return
name|mHash
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|put
parameter_list|(
name|MapJoinKey
name|key
parameter_list|,
name|MapJoinRowContainer
name|value
parameter_list|)
block|{
name|mHash
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|mHash
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|Entry
argument_list|<
name|MapJoinKey
argument_list|,
name|MapJoinRowContainer
argument_list|>
argument_list|>
name|entrySet
parameter_list|()
block|{
return|return
name|mHash
operator|.
name|entrySet
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|mHash
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|MapJoinKey
name|putRow
parameter_list|(
name|MapJoinObjectSerDeContext
name|keyContext
parameter_list|,
name|Writable
name|currentKey
parameter_list|,
name|MapJoinObjectSerDeContext
name|valueContext
parameter_list|,
name|Writable
name|currentValue
parameter_list|)
throws|throws
name|SerDeException
throws|,
name|HiveException
block|{
name|MapJoinKey
name|key
init|=
name|MapJoinKey
operator|.
name|read
argument_list|(
name|output
argument_list|,
name|keyContext
argument_list|,
name|currentKey
argument_list|)
decl_stmt|;
name|FlatRowContainer
name|values
init|=
operator|(
name|FlatRowContainer
operator|)
name|get
argument_list|(
name|key
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
name|FlatRowContainer
argument_list|()
expr_stmt|;
name|put
argument_list|(
name|key
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
name|values
operator|.
name|add
argument_list|(
name|valueContext
argument_list|,
operator|(
name|BytesWritable
operator|)
name|currentValue
argument_list|)
expr_stmt|;
return|return
name|key
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReusableGetAdaptor
name|createGetter
parameter_list|(
name|MapJoinKey
name|keyTypeFromLoader
parameter_list|)
block|{
return|return
operator|new
name|GetAdaptor
argument_list|(
name|keyTypeFromLoader
argument_list|)
return|;
block|}
specifier|private
class|class
name|GetAdaptor
implements|implements
name|ReusableGetAdaptor
block|{
specifier|private
name|Object
index|[]
name|currentKey
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|vectorKeyOIs
decl_stmt|;
specifier|private
name|MapJoinKey
name|key
decl_stmt|;
specifier|private
name|MapJoinRowContainer
name|currentValue
decl_stmt|;
specifier|private
specifier|final
name|Output
name|output
init|=
operator|new
name|Output
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|isFirstKey
init|=
literal|true
decl_stmt|;
specifier|public
name|GetAdaptor
parameter_list|(
name|MapJoinKey
name|key
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|JoinUtil
operator|.
name|JoinResult
name|setFromVector
parameter_list|(
name|VectorHashKeyWrapper
name|kw
parameter_list|,
name|VectorExpressionWriter
index|[]
name|keyOutputWriters
parameter_list|,
name|VectorHashKeyWrapperBatch
name|keyWrapperBatch
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|currentKey
operator|==
literal|null
condition|)
block|{
name|currentKey
operator|=
operator|new
name|Object
index|[
name|keyOutputWriters
operator|.
name|length
index|]
expr_stmt|;
name|vectorKeyOIs
operator|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keyOutputWriters
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|vectorKeyOIs
operator|.
name|add
argument_list|(
name|keyOutputWriters
index|[
name|i
index|]
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keyOutputWriters
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|currentKey
index|[
name|i
index|]
operator|=
name|keyWrapperBatch
operator|.
name|getWritableKeyValue
argument_list|(
name|kw
argument_list|,
name|i
argument_list|,
name|keyOutputWriters
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|key
operator|=
name|MapJoinKey
operator|.
name|readFromVector
argument_list|(
name|output
argument_list|,
name|key
argument_list|,
name|currentKey
argument_list|,
name|vectorKeyOIs
argument_list|,
operator|!
name|isFirstKey
argument_list|)
expr_stmt|;
name|isFirstKey
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|currentValue
operator|=
name|mHash
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|currentValue
operator|==
literal|null
condition|)
block|{
return|return
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|NOMATCH
return|;
block|}
else|else
block|{
return|return
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|MATCH
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|JoinUtil
operator|.
name|JoinResult
name|setFromRow
parameter_list|(
name|Object
name|row
parameter_list|,
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
name|fields
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|ois
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|currentKey
operator|==
literal|null
condition|)
block|{
name|currentKey
operator|=
operator|new
name|Object
index|[
name|fields
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
block|}
for|for
control|(
name|int
name|keyIndex
init|=
literal|0
init|;
name|keyIndex
operator|<
name|fields
operator|.
name|size
argument_list|()
condition|;
operator|++
name|keyIndex
control|)
block|{
name|currentKey
index|[
name|keyIndex
index|]
operator|=
name|fields
operator|.
name|get
argument_list|(
name|keyIndex
argument_list|)
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
name|key
operator|=
name|MapJoinKey
operator|.
name|readFromRow
argument_list|(
name|output
argument_list|,
name|key
argument_list|,
name|currentKey
argument_list|,
name|ois
argument_list|,
operator|!
name|isFirstKey
argument_list|)
expr_stmt|;
name|isFirstKey
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|currentValue
operator|=
name|mHash
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|currentValue
operator|==
literal|null
condition|)
block|{
return|return
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|NOMATCH
return|;
block|}
else|else
block|{
return|return
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|MATCH
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|JoinUtil
operator|.
name|JoinResult
name|setFromOther
parameter_list|(
name|ReusableGetAdaptor
name|other
parameter_list|)
block|{
assert|assert
name|other
operator|instanceof
name|GetAdaptor
assert|;
name|GetAdaptor
name|other2
init|=
operator|(
name|GetAdaptor
operator|)
name|other
decl_stmt|;
name|this
operator|.
name|key
operator|=
name|other2
operator|.
name|key
expr_stmt|;
name|this
operator|.
name|isFirstKey
operator|=
name|other2
operator|.
name|isFirstKey
expr_stmt|;
name|this
operator|.
name|currentValue
operator|=
name|mHash
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|currentValue
operator|==
literal|null
condition|)
block|{
return|return
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|NOMATCH
return|;
block|}
else|else
block|{
return|return
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|MATCH
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasAnyNulls
parameter_list|(
name|int
name|fieldCount
parameter_list|,
name|boolean
index|[]
name|nullsafes
parameter_list|)
block|{
return|return
name|key
operator|.
name|hasAnyNulls
argument_list|(
name|fieldCount
argument_list|,
name|nullsafes
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|MapJoinRowContainer
name|getCurrentRows
parameter_list|()
block|{
return|return
name|currentValue
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
index|[]
name|getCurrentKey
parameter_list|()
block|{
return|return
name|currentKey
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|seal
parameter_list|()
block|{
comment|// Nothing to do.
block|}
annotation|@
name|Override
specifier|public
name|MapJoinKey
name|getAnyKey
parameter_list|()
block|{
return|return
name|mHash
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
name|mHash
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|dumpMetrics
parameter_list|()
block|{
comment|// Nothing to do.
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasSpill
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

