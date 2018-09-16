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
name|binarysortable
operator|.
name|fast
operator|.
name|BinarySortableDeserializeRead
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
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|typeinfo
operator|.
name|TypeInfoFactory
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

begin_comment
comment|/*  * An single byte array value hash map optimized for vector map join.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinFastStringCommon
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|VectorMapJoinFastStringCommon
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|BinarySortableDeserializeRead
name|keyBinarySortableDeserializeRead
decl_stmt|;
specifier|public
name|boolean
name|adaptPutRow
parameter_list|(
name|VectorMapJoinFastBytesHashTable
name|hashTable
parameter_list|,
name|BytesWritable
name|currentKey
parameter_list|,
name|BytesWritable
name|currentValue
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|byte
index|[]
name|keyBytes
init|=
name|currentKey
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|int
name|keyLength
init|=
name|currentKey
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|keyBinarySortableDeserializeRead
operator|.
name|set
argument_list|(
name|keyBytes
argument_list|,
literal|0
argument_list|,
name|keyLength
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|keyBinarySortableDeserializeRead
operator|.
name|readNextField
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
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
literal|"\nDeserializeRead details: "
operator|+
name|keyBinarySortableDeserializeRead
operator|.
name|getDetailedReadPositionString
argument_list|()
operator|+
literal|"\nException: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
name|hashTable
operator|.
name|add
argument_list|(
name|keyBinarySortableDeserializeRead
operator|.
name|currentBytes
argument_list|,
name|keyBinarySortableDeserializeRead
operator|.
name|currentBytesStart
argument_list|,
name|keyBinarySortableDeserializeRead
operator|.
name|currentBytesLength
argument_list|,
name|currentValue
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|public
name|VectorMapJoinFastStringCommon
parameter_list|()
block|{
name|PrimitiveTypeInfo
index|[]
name|primitiveTypeInfos
init|=
block|{
name|TypeInfoFactory
operator|.
name|stringTypeInfo
block|}
decl_stmt|;
name|keyBinarySortableDeserializeRead
operator|=
operator|new
name|BinarySortableDeserializeRead
argument_list|(
name|primitiveTypeInfos
argument_list|,
comment|/* useExternalBuffer */
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

