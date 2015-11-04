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
name|SerDe
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
name|MyTestPrimitiveClass
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
name|MyTestPrimitiveClass
operator|.
name|ExtraTypeInfo
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
name|ObjectInspectorFactory
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
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
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
operator|.
name|PrimitiveCategory
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
name|StandardStructObjectInspector
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
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|TypeInfoUtils
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
name|MyTestPrimitiveClass
index|[]
name|myTestPrimitiveClasses
parameter_list|,
name|SerDe
index|[]
name|serdes
parameter_list|,
name|StructObjectInspector
index|[]
name|rowOIs
parameter_list|,
name|PrimitiveTypeInfo
index|[]
index|[]
name|primitiveTypeInfosArray
parameter_list|)
throws|throws
name|Throwable
block|{
name|LazyBinarySerializeWrite
name|lazyBinarySerializeWrite
init|=
operator|new
name|LazyBinarySerializeWrite
argument_list|(
name|MyTestPrimitiveClass
operator|.
name|primitiveCount
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
name|myTestPrimitiveClasses
operator|.
name|length
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
name|myTestPrimitiveClasses
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|MyTestPrimitiveClass
name|t
init|=
name|myTestPrimitiveClasses
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
name|MyTestPrimitiveClass
operator|.
name|primitiveCount
condition|;
name|index
operator|++
control|)
block|{
name|Object
name|object
init|=
name|t
operator|.
name|getPrimitiveObject
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|VerifyFast
operator|.
name|serializeWrite
argument_list|(
name|lazyBinarySerializeWrite
argument_list|,
name|primitiveTypeInfosArray
index|[
name|i
index|]
index|[
name|index
index|]
argument_list|,
name|object
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
name|myTestPrimitiveClasses
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|MyTestPrimitiveClass
name|t
init|=
name|myTestPrimitiveClasses
index|[
name|i
index|]
decl_stmt|;
name|PrimitiveTypeInfo
index|[]
name|primitiveTypeInfos
init|=
name|primitiveTypeInfosArray
index|[
name|i
index|]
decl_stmt|;
name|LazyBinaryDeserializeRead
name|lazyBinaryDeserializeRead
init|=
operator|new
name|LazyBinaryDeserializeRead
argument_list|(
name|primitiveTypeInfos
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
name|MyTestPrimitiveClass
operator|.
name|primitiveCount
condition|;
name|index
operator|++
control|)
block|{
name|Object
name|object
init|=
name|t
operator|.
name|getPrimitiveObject
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|PrimitiveCategory
name|primitiveCategory
init|=
name|t
operator|.
name|getPrimitiveCategory
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|VerifyFast
operator|.
name|verifyDeserializeRead
argument_list|(
name|lazyBinaryDeserializeRead
argument_list|,
name|primitiveTypeInfos
index|[
name|index
index|]
argument_list|,
name|object
argument_list|)
expr_stmt|;
block|}
name|lazyBinaryDeserializeRead
operator|.
name|extraFieldsCheck
argument_list|()
expr_stmt|;
name|TestCase
operator|.
name|assertTrue
argument_list|(
operator|!
name|lazyBinaryDeserializeRead
operator|.
name|readBeyondConfiguredFieldsWarned
argument_list|()
argument_list|)
expr_stmt|;
name|TestCase
operator|.
name|assertTrue
argument_list|(
operator|!
name|lazyBinaryDeserializeRead
operator|.
name|readBeyondBufferRangeWarned
argument_list|()
argument_list|)
expr_stmt|;
name|TestCase
operator|.
name|assertTrue
argument_list|(
operator|!
name|lazyBinaryDeserializeRead
operator|.
name|bufferRangeHasExtraDataWarned
argument_list|()
argument_list|)
expr_stmt|;
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
name|myTestPrimitiveClasses
operator|.
name|length
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
init|=
operator|(
name|LazyBinaryStruct
operator|)
name|serdes
index|[
name|i
index|]
operator|.
name|deserialize
argument_list|(
name|bytesWritable
argument_list|)
decl_stmt|;
name|MyTestPrimitiveClass
name|t
init|=
name|myTestPrimitiveClasses
index|[
name|i
index|]
decl_stmt|;
name|PrimitiveTypeInfo
index|[]
name|primitiveTypeInfos
init|=
name|primitiveTypeInfosArray
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
name|MyTestPrimitiveClass
operator|.
name|primitiveCount
condition|;
name|index
operator|++
control|)
block|{
name|PrimitiveTypeInfo
name|primitiveTypeInfo
init|=
name|primitiveTypeInfos
index|[
name|index
index|]
decl_stmt|;
name|Object
name|expected
init|=
name|t
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|index
argument_list|,
name|primitiveTypeInfo
argument_list|)
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
name|expected
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
name|expected
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
name|object
operator|.
name|equals
argument_list|(
name|expected
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
name|myTestPrimitiveClasses
operator|.
name|length
index|]
decl_stmt|;
comment|// Serialize using the SerDe, then below deserialize using DeserializeRead.
name|Object
index|[]
name|row
init|=
operator|new
name|Object
index|[
name|MyTestPrimitiveClass
operator|.
name|primitiveCount
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
name|myTestPrimitiveClasses
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|MyTestPrimitiveClass
name|t
init|=
name|myTestPrimitiveClasses
index|[
name|i
index|]
decl_stmt|;
name|PrimitiveTypeInfo
index|[]
name|primitiveTypeInfos
init|=
name|primitiveTypeInfosArray
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
name|MyTestPrimitiveClass
operator|.
name|primitiveCount
condition|;
name|index
operator|++
control|)
block|{
name|Object
name|object
init|=
name|t
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|index
argument_list|,
name|primitiveTypeInfos
index|[
name|index
index|]
argument_list|)
decl_stmt|;
name|row
index|[
name|index
index|]
operator|=
name|object
expr_stmt|;
block|}
name|BytesWritable
name|serialized
init|=
operator|(
name|BytesWritable
operator|)
name|serdes
index|[
name|i
index|]
operator|.
name|serialize
argument_list|(
name|row
argument_list|,
name|rowOIs
index|[
name|i
index|]
argument_list|)
decl_stmt|;
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
name|serialized
argument_list|)
expr_stmt|;
name|byte
index|[]
name|bytes1
init|=
name|Arrays
operator|.
name|copyOfRange
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
decl_stmt|;
name|byte
index|[]
name|bytes2
init|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|serializeWriteBytes
index|[
name|i
index|]
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|serializeWriteBytes
index|[
name|i
index|]
operator|.
name|getLength
argument_list|()
argument_list|)
decl_stmt|;
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
literal|"SerializeWrite and SerDe serialization does not match"
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
name|myTestPrimitiveClasses
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|MyTestPrimitiveClass
name|t
init|=
name|myTestPrimitiveClasses
index|[
name|i
index|]
decl_stmt|;
name|PrimitiveTypeInfo
index|[]
name|primitiveTypeInfos
init|=
name|primitiveTypeInfosArray
index|[
name|i
index|]
decl_stmt|;
name|LazyBinaryDeserializeRead
name|lazyBinaryDeserializeRead
init|=
operator|new
name|LazyBinaryDeserializeRead
argument_list|(
name|primitiveTypeInfos
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
name|MyTestPrimitiveClass
operator|.
name|primitiveCount
condition|;
name|index
operator|++
control|)
block|{
name|Object
name|object
init|=
name|t
operator|.
name|getPrimitiveObject
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|VerifyFast
operator|.
name|verifyDeserializeRead
argument_list|(
name|lazyBinaryDeserializeRead
argument_list|,
name|primitiveTypeInfos
index|[
name|index
index|]
argument_list|,
name|object
argument_list|)
expr_stmt|;
block|}
name|lazyBinaryDeserializeRead
operator|.
name|extraFieldsCheck
argument_list|()
expr_stmt|;
name|TestCase
operator|.
name|assertTrue
argument_list|(
operator|!
name|lazyBinaryDeserializeRead
operator|.
name|readBeyondConfiguredFieldsWarned
argument_list|()
argument_list|)
expr_stmt|;
name|TestCase
operator|.
name|assertTrue
argument_list|(
operator|!
name|lazyBinaryDeserializeRead
operator|.
name|readBeyondBufferRangeWarned
argument_list|()
argument_list|)
expr_stmt|;
name|TestCase
operator|.
name|assertTrue
argument_list|(
operator|!
name|lazyBinaryDeserializeRead
operator|.
name|bufferRangeHasExtraDataWarned
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testLazyBinaryFast
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|int
name|num
init|=
literal|1000
decl_stmt|;
name|Random
name|r
init|=
operator|new
name|Random
argument_list|(
literal|1234
argument_list|)
decl_stmt|;
name|MyTestPrimitiveClass
index|[]
name|rows
init|=
operator|new
name|MyTestPrimitiveClass
index|[
name|num
index|]
decl_stmt|;
name|PrimitiveTypeInfo
index|[]
index|[]
name|primitiveTypeInfosArray
init|=
operator|new
name|PrimitiveTypeInfo
index|[
name|num
index|]
index|[]
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
name|num
condition|;
name|i
operator|++
control|)
block|{
name|int
name|randField
init|=
name|r
operator|.
name|nextInt
argument_list|(
name|MyTestPrimitiveClass
operator|.
name|primitiveCount
argument_list|)
decl_stmt|;
name|MyTestPrimitiveClass
name|t
init|=
operator|new
name|MyTestPrimitiveClass
argument_list|()
decl_stmt|;
name|int
name|field
init|=
literal|0
decl_stmt|;
name|ExtraTypeInfo
name|extraTypeInfo
init|=
operator|new
name|ExtraTypeInfo
argument_list|()
decl_stmt|;
name|t
operator|.
name|randomFill
argument_list|(
name|r
argument_list|,
name|randField
argument_list|,
name|field
argument_list|,
name|extraTypeInfo
argument_list|)
expr_stmt|;
name|PrimitiveTypeInfo
index|[]
name|primitiveTypeInfos
init|=
name|MyTestPrimitiveClass
operator|.
name|getPrimitiveTypeInfos
argument_list|(
name|extraTypeInfo
argument_list|)
decl_stmt|;
name|rows
index|[
name|i
index|]
operator|=
name|t
expr_stmt|;
name|primitiveTypeInfosArray
index|[
name|i
index|]
operator|=
name|primitiveTypeInfos
expr_stmt|;
block|}
comment|// To get the specific type information for CHAR and VARCHAR, seems like we need an
comment|// inspector and SerDe per row...
name|StructObjectInspector
index|[]
name|rowOIs
init|=
operator|new
name|StructObjectInspector
index|[
name|num
index|]
decl_stmt|;
name|SerDe
index|[]
name|serdes
init|=
operator|new
name|SerDe
index|[
name|num
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
name|num
condition|;
name|i
operator|++
control|)
block|{
name|MyTestPrimitiveClass
name|t
init|=
name|rows
index|[
name|i
index|]
decl_stmt|;
name|StructObjectInspector
name|rowOI
init|=
name|t
operator|.
name|getRowInspector
argument_list|(
name|primitiveTypeInfosArray
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|String
name|fieldNames
init|=
name|ObjectInspectorUtils
operator|.
name|getFieldNames
argument_list|(
name|rowOI
argument_list|)
decl_stmt|;
name|String
name|fieldTypes
init|=
name|ObjectInspectorUtils
operator|.
name|getFieldTypes
argument_list|(
name|rowOI
argument_list|)
decl_stmt|;
name|rowOIs
index|[
name|i
index|]
operator|=
name|rowOI
expr_stmt|;
name|serdes
index|[
name|i
index|]
operator|=
name|TestLazyBinarySerDe
operator|.
name|getSerDe
argument_list|(
name|fieldNames
argument_list|,
name|fieldTypes
argument_list|)
expr_stmt|;
block|}
name|testLazyBinaryFast
argument_list|(
name|rows
argument_list|,
name|serdes
argument_list|,
name|rowOIs
argument_list|,
name|primitiveTypeInfosArray
argument_list|)
expr_stmt|;
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
block|}
end_class

end_unit

