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
name|optimized
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
name|optimized
operator|.
name|VectorMapJoinOptimizedHashTable
operator|.
name|SerializedBytes
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
operator|.
name|HashTableKeyType
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
name|binarysortable
operator|.
name|fast
operator|.
name|BinarySortableSerializeWrite
import|;
end_import

begin_comment
comment|/*  * An single long value hash map based on the BytesBytesMultiHashMap.  *  * We serialize the long key into BinarySortable format into an output buffer accepted by  * BytesBytesMultiHashMap.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinOptimizedLongCommon
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
name|VectorMapJoinOptimizedLongCommon
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isOuterJoin
decl_stmt|;
specifier|private
specifier|final
specifier|transient
name|TableDesc
name|tableDesc
decl_stmt|;
specifier|private
name|HashTableKeyType
name|hashTableKeyType
decl_stmt|;
specifier|private
name|BinarySortableSerializeWrite
name|keyBinarySortableSerializeWrite
decl_stmt|;
specifier|private
specifier|transient
name|Output
name|output
decl_stmt|;
specifier|private
specifier|transient
name|SerializedBytes
name|serializedBytes
decl_stmt|;
comment|// protected boolean useMinMax;
specifier|protected
name|long
name|min
decl_stmt|;
specifier|protected
name|long
name|max
decl_stmt|;
specifier|public
name|boolean
name|useMinMax
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|long
name|min
parameter_list|()
block|{
return|return
name|min
return|;
block|}
specifier|public
name|long
name|max
parameter_list|()
block|{
return|return
name|max
return|;
block|}
specifier|public
name|SerializedBytes
name|serialize
parameter_list|(
name|long
name|key
parameter_list|)
throws|throws
name|IOException
block|{
name|keyBinarySortableSerializeWrite
operator|.
name|reset
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|hashTableKeyType
condition|)
block|{
case|case
name|BOOLEAN
case|:
name|keyBinarySortableSerializeWrite
operator|.
name|writeBoolean
argument_list|(
name|key
operator|==
literal|1
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTE
case|:
name|keyBinarySortableSerializeWrite
operator|.
name|writeByte
argument_list|(
operator|(
name|byte
operator|)
name|key
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|keyBinarySortableSerializeWrite
operator|.
name|writeShort
argument_list|(
operator|(
name|short
operator|)
name|key
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|keyBinarySortableSerializeWrite
operator|.
name|writeInt
argument_list|(
operator|(
name|int
operator|)
name|key
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|keyBinarySortableSerializeWrite
operator|.
name|writeLong
argument_list|(
name|key
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected hash table key type "
operator|+
name|hashTableKeyType
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
comment|// byte[] bytes = Arrays.copyOf(output.getData(), output.getLength());
comment|// LOG.debug("VectorMapJoinOptimizedLongCommon serialize key " + key + " hashTableKeyType " + hashTableKeyType.name() + " hex " + Hex.encodeHexString(bytes));
name|serializedBytes
operator|.
name|bytes
operator|=
name|output
operator|.
name|getData
argument_list|()
expr_stmt|;
name|serializedBytes
operator|.
name|offset
operator|=
literal|0
expr_stmt|;
name|serializedBytes
operator|.
name|length
operator|=
name|output
operator|.
name|getLength
argument_list|()
expr_stmt|;
return|return
name|serializedBytes
return|;
block|}
specifier|public
name|VectorMapJoinOptimizedLongCommon
parameter_list|(
name|boolean
name|minMaxEnabled
parameter_list|,
name|boolean
name|isOuterJoin
parameter_list|,
name|HashTableKeyType
name|hashTableKeyType
parameter_list|,
name|TableDesc
name|tableDesc
parameter_list|)
block|{
name|this
operator|.
name|isOuterJoin
operator|=
name|isOuterJoin
expr_stmt|;
comment|// useMinMax = minMaxEnabled;
name|min
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
name|max
operator|=
name|Long
operator|.
name|MIN_VALUE
expr_stmt|;
name|this
operator|.
name|hashTableKeyType
operator|=
name|hashTableKeyType
expr_stmt|;
name|this
operator|.
name|tableDesc
operator|=
name|tableDesc
expr_stmt|;
name|keyBinarySortableSerializeWrite
operator|=
name|BinarySortableSerializeWrite
operator|.
name|with
argument_list|(
name|tableDesc
operator|.
name|getProperties
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|output
operator|=
operator|new
name|Output
argument_list|()
expr_stmt|;
name|keyBinarySortableSerializeWrite
operator|.
name|set
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|serializedBytes
operator|=
operator|new
name|SerializedBytes
argument_list|()
expr_stmt|;
block|}
specifier|public
name|TableDesc
name|getTableDesc
parameter_list|()
block|{
return|return
name|tableDesc
return|;
block|}
block|}
end_class

end_unit

