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
name|serde2
operator|.
name|lazybinary
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|AbstractSerDe
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
name|SerdeRandomRowSource
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
name|VerifyFast
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
name|MyTestClass
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
name|lazy
operator|.
name|VerifyLazy
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
name|fast
operator|.
name|LazyBinaryDeserializeRead
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
name|fast
operator|.
name|LazyBinarySerializeWrite
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
name|ObjectInspectorUtils
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|UnionObject
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
name|TypeInfo
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

begin_class
specifier|public
class|class
name|TestLazyBinaryFast
extends|extends
name|TestCase
block|{
specifier|private
name|void
name|testLazyBinaryFast
parameter_list|(
name|SerdeRandomRowSource
name|source
parameter_list|,
name|Object
index|[]
index|[]
name|rows
parameter_list|,
name|AbstractSerDe
name|serde
parameter_list|,
name|StructObjectInspector
name|rowOI
parameter_list|,
name|AbstractSerDe
name|serde_fewer
parameter_list|,
name|StructObjectInspector
name|writeRowOI
parameter_list|,
name|TypeInfo
index|[]
name|typeInfos
parameter_list|,
name|boolean
name|useIncludeColumns
parameter_list|,
name|boolean
name|doWriteFewerColumns
parameter_list|,
name|Random
name|r
parameter_list|)
throws|throws
name|Throwable
block|{
name|int
name|rowCount
init|=
name|rows
operator|.
name|length
decl_stmt|;
name|int
name|columnCount
init|=
name|typeInfos
operator|.
name|length
decl_stmt|;
name|boolean
index|[]
name|columnsToInclude
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|useIncludeColumns
condition|)
block|{
name|columnsToInclude
operator|=
operator|new
name|boolean
index|[
name|columnCount
index|]
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
name|columnCount
condition|;
name|i
operator|++
control|)
block|{
name|columnsToInclude
index|[
name|i
index|]
operator|=
name|r
operator|.
name|nextBoolean
argument_list|()
expr_stmt|;
block|}
block|}
name|int
name|writeColumnCount
init|=
name|columnCount
decl_stmt|;
name|TypeInfo
index|[]
name|writeTypeInfos
init|=
name|typeInfos
decl_stmt|;
if|if
condition|(
name|doWriteFewerColumns
condition|)
block|{
name|writeColumnCount
operator|=
name|writeRowOI
operator|.
name|getAllStructFieldRefs
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
name|writeTypeInfos
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|typeInfos
argument_list|,
name|writeColumnCount
argument_list|)
expr_stmt|;
block|}
name|LazyBinarySerializeWrite
name|lazyBinarySerializeWrite
init|=
operator|new
name|LazyBinarySerializeWrite
argument_list|(
name|writeColumnCount
argument_list|)
decl_stmt|;
comment|// Try to serialize
name|BytesWritable
name|serializeWriteBytes
index|[]
init|=
operator|new
name|BytesWritable
index|[
name|rowCount
index|]
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
name|rowCount
condition|;
name|i
operator|++
control|)
block|{
name|Object
index|[]
name|row
init|=
name|rows
index|[
name|i
index|]
decl_stmt|;
name|Output
name|output
init|=
operator|new
name|Output
argument_list|()
decl_stmt|;
name|lazyBinarySerializeWrite
operator|.
name|set
argument_list|(
name|output
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|writeColumnCount
condition|;
name|index
operator|++
control|)
block|{
name|VerifyFast
operator|.
name|serializeWrite
argument_list|(
name|lazyBinarySerializeWrite
argument_list|,
name|typeInfos
index|[
name|index
index|]
argument_list|,
name|row
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
name|BytesWritable
name|bytesWritable
init|=
operator|new
name|BytesWritable
argument_list|()
decl_stmt|;
name|bytesWritable
operator|.
name|set
argument_list|(
name|output
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|output
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|serializeWriteBytes
index|[
name|i
index|]
operator|=
name|bytesWritable
expr_stmt|;
block|}
comment|// Try to deserialize
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rowCount
condition|;
name|i
operator|++
control|)
block|{
name|Object
index|[]
name|row
init|=
name|rows
index|[
name|i
index|]
decl_stmt|;
comment|// Specifying the right type info length tells LazyBinaryDeserializeRead which is the last
comment|// column.
name|LazyBinaryDeserializeRead
name|lazyBinaryDeserializeRead
init|=
operator|new
name|LazyBinaryDeserializeRead
argument_list|(
name|writeTypeInfos
argument_list|,
comment|/* useExternalBuffer */
literal|false
argument_list|)
decl_stmt|;
name|BytesWritable
name|bytesWritable
init|=
name|serializeWriteBytes
index|[
name|i
index|]
decl_stmt|;
name|lazyBinaryDeserializeRead
operator|.
name|set
argument_list|(
name|bytesWritable
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bytesWritable
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|columnCount
condition|;
name|index
operator|++
control|)
block|{
if|if
condition|(
name|useIncludeColumns
operator|&&
operator|!
name|columnsToInclude
index|[
name|index
index|]
condition|)
block|{
name|lazyBinaryDeserializeRead
operator|.
name|skipNextField
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|index
operator|>=
name|writeColumnCount
condition|)
block|{
comment|// Should come back a null.
name|VerifyFast
operator|.
name|verifyDeserializeRead
argument_list|(
name|lazyBinaryDeserializeRead
argument_list|,
name|typeInfos
index|[
name|index
index|]
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|verifyRead
argument_list|(
name|lazyBinaryDeserializeRead
argument_list|,
name|typeInfos
index|[
name|index
index|]
argument_list|,
name|row
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|writeColumnCount
operator|==
name|columnCount
condition|)
block|{
name|TestCase
operator|.
name|assertTrue
argument_list|(
name|lazyBinaryDeserializeRead
operator|.
name|isEndOfInputReached
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Try to deserialize using SerDe class our Writable row objects created by SerializeWrite.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rowCount
condition|;
name|i
operator|++
control|)
block|{
name|BytesWritable
name|bytesWritable
init|=
name|serializeWriteBytes
index|[
name|i
index|]
decl_stmt|;
name|LazyBinaryStruct
name|lazyBinaryStruct
decl_stmt|;
if|if
condition|(
name|doWriteFewerColumns
condition|)
block|{
name|lazyBinaryStruct
operator|=
operator|(
name|LazyBinaryStruct
operator|)
name|serde_fewer
operator|.
name|deserialize
argument_list|(
name|bytesWritable
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|lazyBinaryStruct
operator|=
operator|(
name|LazyBinaryStruct
operator|)
name|serde
operator|.
name|deserialize
argument_list|(
name|bytesWritable
argument_list|)
expr_stmt|;
block|}
name|Object
index|[]
name|row
init|=
name|rows
index|[
name|i
index|]
decl_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|writeColumnCount
condition|;
name|index
operator|++
control|)
block|{
name|TypeInfo
name|typeInfo
init|=
name|typeInfos
index|[
name|index
index|]
decl_stmt|;
name|Object
name|object
init|=
name|lazyBinaryStruct
operator|.
name|getField
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|row
index|[
name|index
index|]
operator|==
literal|null
operator|||
name|object
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|row
index|[
name|index
index|]
operator|!=
literal|null
operator|||
name|object
operator|!=
literal|null
condition|)
block|{
name|fail
argument_list|(
literal|"SerDe deserialized NULL column mismatch"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|VerifyLazy
operator|.
name|lazyCompare
argument_list|(
name|typeInfo
argument_list|,
name|object
argument_list|,
name|row
index|[
name|index
index|]
argument_list|)
condition|)
block|{
name|fail
argument_list|(
literal|"SerDe deserialized value does not match"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// One Writable per row.
name|BytesWritable
name|serdeBytes
index|[]
init|=
operator|new
name|BytesWritable
index|[
name|rowCount
index|]
decl_stmt|;
comment|// Serialize using the SerDe, then below deserialize using DeserializeRead.
name|Object
index|[]
name|serdeRow
init|=
operator|new
name|Object
index|[
name|writeColumnCount
index|]
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
name|rowCount
condition|;
name|i
operator|++
control|)
block|{
name|Object
index|[]
name|row
init|=
name|rows
index|[
name|i
index|]
decl_stmt|;
comment|// LazyBinary seems to work better with an row object array instead of a Java object...
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|writeColumnCount
condition|;
name|index
operator|++
control|)
block|{
name|serdeRow
index|[
name|index
index|]
operator|=
name|row
index|[
name|index
index|]
expr_stmt|;
block|}
name|BytesWritable
name|serialized
decl_stmt|;
if|if
condition|(
name|doWriteFewerColumns
condition|)
block|{
name|serialized
operator|=
operator|(
name|BytesWritable
operator|)
name|serde_fewer
operator|.
name|serialize
argument_list|(
name|serdeRow
argument_list|,
name|writeRowOI
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|serialized
operator|=
operator|(
name|BytesWritable
operator|)
name|serde
operator|.
name|serialize
argument_list|(
name|serdeRow
argument_list|,
name|rowOI
argument_list|)
expr_stmt|;
block|}
name|BytesWritable
name|bytesWritable
init|=
operator|new
name|BytesWritable
argument_list|(
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|serialized
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|serialized
operator|.
name|getLength
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes1
init|=
name|bytesWritable
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|BytesWritable
name|lazySerializedWriteBytes
init|=
name|serializeWriteBytes
index|[
name|i
index|]
decl_stmt|;
name|byte
index|[]
name|bytes2
init|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|lazySerializedWriteBytes
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|lazySerializedWriteBytes
operator|.
name|getLength
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|bytes1
operator|.
name|length
operator|!=
name|bytes2
operator|.
name|length
condition|)
block|{
name|fail
argument_list|(
literal|"SerializeWrite length "
operator|+
name|bytes2
operator|.
name|length
operator|+
literal|" and "
operator|+
literal|"SerDe serialization length "
operator|+
name|bytes1
operator|.
name|length
operator|+
literal|" do not match ("
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|typeInfos
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|bytes1
argument_list|,
name|bytes2
argument_list|)
condition|)
block|{
name|fail
argument_list|(
literal|"SerializeWrite and SerDe serialization does not match ("
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|typeInfos
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
name|serdeBytes
index|[
name|i
index|]
operator|=
name|bytesWritable
expr_stmt|;
block|}
comment|// Try to deserialize using DeserializeRead our Writable row objects created by SerDe.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rowCount
condition|;
name|i
operator|++
control|)
block|{
name|Object
index|[]
name|row
init|=
name|rows
index|[
name|i
index|]
decl_stmt|;
comment|// When doWriteFewerColumns, try to read more fields than exist in buffer.
name|LazyBinaryDeserializeRead
name|lazyBinaryDeserializeRead
init|=
operator|new
name|LazyBinaryDeserializeRead
argument_list|(
name|typeInfos
argument_list|,
comment|/* useExternalBuffer */
literal|false
argument_list|)
decl_stmt|;
name|BytesWritable
name|bytesWritable
init|=
name|serdeBytes
index|[
name|i
index|]
decl_stmt|;
name|lazyBinaryDeserializeRead
operator|.
name|set
argument_list|(
name|bytesWritable
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bytesWritable
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|columnCount
condition|;
name|index
operator|++
control|)
block|{
if|if
condition|(
name|useIncludeColumns
operator|&&
operator|!
name|columnsToInclude
index|[
name|index
index|]
condition|)
block|{
name|lazyBinaryDeserializeRead
operator|.
name|skipNextField
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|index
operator|>=
name|writeColumnCount
condition|)
block|{
comment|// Should come back a null.
name|VerifyFast
operator|.
name|verifyDeserializeRead
argument_list|(
name|lazyBinaryDeserializeRead
argument_list|,
name|typeInfos
index|[
name|index
index|]
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|verifyRead
argument_list|(
name|lazyBinaryDeserializeRead
argument_list|,
name|typeInfos
index|[
name|index
index|]
argument_list|,
name|row
index|[
name|index
index|]
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|writeColumnCount
operator|==
name|columnCount
condition|)
block|{
name|TestCase
operator|.
name|assertTrue
argument_list|(
name|lazyBinaryDeserializeRead
operator|.
name|isEndOfInputReached
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|verifyRead
parameter_list|(
name|LazyBinaryDeserializeRead
name|lazyBinaryDeserializeRead
parameter_list|,
name|TypeInfo
name|typeInfo
parameter_list|,
name|Object
name|expectedObject
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|typeInfo
operator|.
name|getCategory
argument_list|()
operator|==
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
name|VerifyFast
operator|.
name|verifyDeserializeRead
argument_list|(
name|lazyBinaryDeserializeRead
argument_list|,
name|typeInfo
argument_list|,
name|expectedObject
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Object
name|complexFieldObj
init|=
name|VerifyFast
operator|.
name|deserializeReadComplexType
argument_list|(
name|lazyBinaryDeserializeRead
argument_list|,
name|typeInfo
argument_list|)
decl_stmt|;
if|if
condition|(
name|expectedObject
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|complexFieldObj
operator|!=
literal|null
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Field reports not null but object is null (class "
operator|+
name|complexFieldObj
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|", "
operator|+
name|complexFieldObj
operator|.
name|toString
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|complexFieldObj
operator|==
literal|null
condition|)
block|{
comment|// It's hard to distinguish a union with null from a null union.
if|if
condition|(
name|expectedObject
operator|instanceof
name|UnionObject
condition|)
block|{
name|UnionObject
name|expectedUnion
init|=
operator|(
name|UnionObject
operator|)
name|expectedObject
decl_stmt|;
if|if
condition|(
name|expectedUnion
operator|.
name|getObject
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return;
block|}
block|}
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Field reports null but object is not null (class "
operator|+
name|expectedObject
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|", "
operator|+
name|expectedObject
operator|.
name|toString
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|VerifyLazy
operator|.
name|lazyCompare
argument_list|(
name|typeInfo
argument_list|,
name|complexFieldObj
argument_list|,
name|expectedObject
argument_list|)
condition|)
block|{
name|TestCase
operator|.
name|fail
argument_list|(
literal|"Comparision failed typeInfo "
operator|+
name|typeInfo
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|testLazyBinaryFastCase
parameter_list|(
name|int
name|caseNum
parameter_list|,
name|boolean
name|doNonRandomFill
parameter_list|,
name|Random
name|r
parameter_list|,
name|SerdeRandomRowSource
operator|.
name|SupportedTypes
name|supportedTypes
parameter_list|,
name|int
name|depth
parameter_list|)
throws|throws
name|Throwable
block|{
name|SerdeRandomRowSource
name|source
init|=
operator|new
name|SerdeRandomRowSource
argument_list|()
decl_stmt|;
name|source
operator|.
name|init
argument_list|(
name|r
argument_list|,
name|supportedTypes
argument_list|,
name|depth
argument_list|)
expr_stmt|;
name|int
name|rowCount
init|=
literal|100
decl_stmt|;
name|Object
index|[]
index|[]
name|rows
init|=
name|source
operator|.
name|randomRows
argument_list|(
name|rowCount
argument_list|)
decl_stmt|;
if|if
condition|(
name|doNonRandomFill
condition|)
block|{
name|MyTestClass
operator|.
name|nonRandomRowFill
argument_list|(
name|rows
argument_list|,
name|source
operator|.
name|primitiveCategories
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|StructObjectInspector
name|rowStructObjectInspector
init|=
name|source
operator|.
name|rowStructObjectInspector
argument_list|()
decl_stmt|;
name|TypeInfo
index|[]
name|typeInfos
init|=
name|source
operator|.
name|typeInfos
argument_list|()
decl_stmt|;
name|int
name|columnCount
init|=
name|typeInfos
operator|.
name|length
decl_stmt|;
name|int
name|writeColumnCount
init|=
name|columnCount
decl_stmt|;
name|StructObjectInspector
name|writeRowStructObjectInspector
init|=
name|rowStructObjectInspector
decl_stmt|;
name|boolean
name|doWriteFewerColumns
init|=
name|r
operator|.
name|nextBoolean
argument_list|()
decl_stmt|;
if|if
condition|(
name|doWriteFewerColumns
condition|)
block|{
name|writeColumnCount
operator|=
literal|1
operator|+
name|r
operator|.
name|nextInt
argument_list|(
name|columnCount
argument_list|)
expr_stmt|;
if|if
condition|(
name|writeColumnCount
operator|==
name|columnCount
condition|)
block|{
name|doWriteFewerColumns
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|writeRowStructObjectInspector
operator|=
name|source
operator|.
name|partialRowStructObjectInspector
argument_list|(
name|writeColumnCount
argument_list|)
expr_stmt|;
block|}
block|}
name|String
name|fieldNames
init|=
name|ObjectInspectorUtils
operator|.
name|getFieldNames
argument_list|(
name|rowStructObjectInspector
argument_list|)
decl_stmt|;
name|String
name|fieldTypes
init|=
name|ObjectInspectorUtils
operator|.
name|getFieldTypes
argument_list|(
name|rowStructObjectInspector
argument_list|)
decl_stmt|;
name|AbstractSerDe
name|serde
init|=
name|TestLazyBinarySerDe
operator|.
name|getSerDe
argument_list|(
name|fieldNames
argument_list|,
name|fieldTypes
argument_list|)
decl_stmt|;
name|AbstractSerDe
name|serde_fewer
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|doWriteFewerColumns
condition|)
block|{
name|String
name|partialFieldNames
init|=
name|ObjectInspectorUtils
operator|.
name|getFieldNames
argument_list|(
name|writeRowStructObjectInspector
argument_list|)
decl_stmt|;
name|String
name|partialFieldTypes
init|=
name|ObjectInspectorUtils
operator|.
name|getFieldTypes
argument_list|(
name|writeRowStructObjectInspector
argument_list|)
decl_stmt|;
name|serde_fewer
operator|=
name|TestLazyBinarySerDe
operator|.
name|getSerDe
argument_list|(
name|partialFieldNames
argument_list|,
name|partialFieldTypes
argument_list|)
expr_stmt|;
empty_stmt|;
block|}
name|testLazyBinaryFast
argument_list|(
name|source
argument_list|,
name|rows
argument_list|,
name|serde
argument_list|,
name|rowStructObjectInspector
argument_list|,
name|serde_fewer
argument_list|,
name|writeRowStructObjectInspector
argument_list|,
name|typeInfos
argument_list|,
comment|/* useIncludeColumns */
literal|false
argument_list|,
comment|/* doWriteFewerColumns */
literal|false
argument_list|,
name|r
argument_list|)
expr_stmt|;
name|testLazyBinaryFast
argument_list|(
name|source
argument_list|,
name|rows
argument_list|,
name|serde
argument_list|,
name|rowStructObjectInspector
argument_list|,
name|serde_fewer
argument_list|,
name|writeRowStructObjectInspector
argument_list|,
name|typeInfos
argument_list|,
comment|/* useIncludeColumns */
literal|true
argument_list|,
comment|/* doWriteFewerColumns */
literal|false
argument_list|,
name|r
argument_list|)
expr_stmt|;
comment|/*      * Can the LazyBinary format really tolerate writing fewer columns?      */
comment|// if (doWriteFewerColumns) {
comment|//   testLazyBinaryFast(
comment|//       source, rows,
comment|//       serde, rowStructObjectInspector,
comment|//       serde_fewer, writeRowStructObjectInspector,
comment|//       primitiveTypeInfos,
comment|//       /* useIncludeColumns */ false, /* doWriteFewerColumns */ true, r);
comment|//   testLazyBinaryFast(
comment|//       source, rows,
comment|//       serde, rowStructObjectInspector,
comment|//       serde_fewer, writeRowStructObjectInspector,
comment|//       primitiveTypeInfos,
comment|//       /* useIncludeColumns */ true, /* doWriteFewerColumns */ true, r);
comment|// }
block|}
specifier|private
name|void
name|testLazyBinaryFast
parameter_list|(
name|SerdeRandomRowSource
operator|.
name|SupportedTypes
name|supportedTypes
parameter_list|,
name|int
name|depth
parameter_list|)
throws|throws
name|Throwable
block|{
try|try
block|{
name|Random
name|r
init|=
operator|new
name|Random
argument_list|(
literal|9983
argument_list|)
decl_stmt|;
name|int
name|caseNum
init|=
literal|0
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|testLazyBinaryFastCase
argument_list|(
name|caseNum
argument_list|,
operator|(
name|i
operator|%
literal|2
operator|==
literal|0
operator|)
argument_list|,
name|r
argument_list|,
name|supportedTypes
argument_list|,
name|depth
argument_list|)
expr_stmt|;
name|caseNum
operator|++
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|public
name|void
name|testLazyBinaryFastPrimitive
parameter_list|()
throws|throws
name|Throwable
block|{
name|testLazyBinaryFast
argument_list|(
name|SerdeRandomRowSource
operator|.
name|SupportedTypes
operator|.
name|PRIMITIVE
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testLazyBinaryFastComplexDepthOne
parameter_list|()
throws|throws
name|Throwable
block|{
name|testLazyBinaryFast
argument_list|(
name|SerdeRandomRowSource
operator|.
name|SupportedTypes
operator|.
name|ALL
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testLazyBinaryFastComplexDepthFour
parameter_list|()
throws|throws
name|Throwable
block|{
name|testLazyBinaryFast
argument_list|(
name|SerdeRandomRowSource
operator|.
name|SupportedTypes
operator|.
name|ALL
argument_list|,
literal|4
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

