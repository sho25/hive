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
name|lazy
package|;
end_package

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
name|Properties
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
name|serde
operator|.
name|serdeConstants
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
name|SerDeUtils
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
name|fast
operator|.
name|LazySimpleDeserializeRead
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
name|fast
operator|.
name|LazySimpleSerializeWrite
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
name|Text
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
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_class
specifier|public
class|class
name|TestLazySimpleFast
extends|extends
name|TestCase
block|{
specifier|private
name|void
name|testLazySimpleFast
parameter_list|(
name|SerdeRandomRowSource
name|source
parameter_list|,
name|Object
index|[]
index|[]
name|rows
parameter_list|,
name|LazySimpleSerDe
name|serde
parameter_list|,
name|StructObjectInspector
name|rowOI
parameter_list|,
name|LazySimpleSerDe
name|serde_fewer
parameter_list|,
name|StructObjectInspector
name|writeRowOI
parameter_list|,
name|byte
name|separator
parameter_list|,
name|LazySerDeParameters
name|serdeParams
parameter_list|,
name|LazySerDeParameters
name|serdeParams_fewer
parameter_list|,
name|PrimitiveTypeInfo
index|[]
name|primitiveTypeInfos
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
name|primitiveTypeInfos
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
name|PrimitiveTypeInfo
index|[]
name|writePrimitiveTypeInfos
init|=
name|primitiveTypeInfos
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
name|writePrimitiveTypeInfos
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|primitiveTypeInfos
argument_list|,
name|writeColumnCount
argument_list|)
expr_stmt|;
block|}
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
name|LazySimpleSerializeWrite
name|lazySimpleSerializeWrite
init|=
operator|new
name|LazySimpleSerializeWrite
argument_list|(
name|columnCount
argument_list|,
name|separator
argument_list|,
name|serdeParams
argument_list|)
decl_stmt|;
name|lazySimpleSerializeWrite
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
name|columnCount
condition|;
name|index
operator|++
control|)
block|{
name|Writable
name|writable
init|=
operator|(
name|Writable
operator|)
name|row
index|[
name|index
index|]
decl_stmt|;
name|VerifyFast
operator|.
name|serializeWrite
argument_list|(
name|lazySimpleSerializeWrite
argument_list|,
name|primitiveTypeInfos
index|[
name|index
index|]
argument_list|,
name|writable
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
name|LazySimpleDeserializeRead
name|lazySimpleDeserializeRead
init|=
operator|new
name|LazySimpleDeserializeRead
argument_list|(
name|writePrimitiveTypeInfos
argument_list|,
name|separator
argument_list|,
name|serdeParams
argument_list|)
decl_stmt|;
if|if
condition|(
name|useIncludeColumns
condition|)
block|{
name|lazySimpleDeserializeRead
operator|.
name|setColumnsToInclude
argument_list|(
name|columnsToInclude
argument_list|)
expr_stmt|;
block|}
name|BytesWritable
name|bytesWritable
init|=
name|serializeWriteBytes
index|[
name|i
index|]
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|bytesWritable
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|int
name|length
init|=
name|bytesWritable
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|lazySimpleDeserializeRead
operator|.
name|set
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|char
index|[]
name|chars
init|=
operator|new
name|char
index|[
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|chars
operator|.
name|length
condition|;
name|c
operator|++
control|)
block|{
name|chars
index|[
name|c
index|]
operator|=
call|(
name|char
call|)
argument_list|(
name|bytes
index|[
name|c
index|]
operator|&
literal|0xFF
argument_list|)
expr_stmt|;
block|}
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
name|index
operator|>=
name|writeColumnCount
operator|||
operator|(
name|useIncludeColumns
operator|&&
operator|!
name|columnsToInclude
index|[
name|index
index|]
operator|)
condition|)
block|{
comment|// Should come back a null.
name|VerifyFast
operator|.
name|verifyDeserializeRead
argument_list|(
name|lazySimpleDeserializeRead
argument_list|,
name|primitiveTypeInfos
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
name|Writable
name|writable
init|=
operator|(
name|Writable
operator|)
name|row
index|[
name|index
index|]
decl_stmt|;
name|VerifyFast
operator|.
name|verifyDeserializeRead
argument_list|(
name|lazySimpleDeserializeRead
argument_list|,
name|primitiveTypeInfos
index|[
name|index
index|]
argument_list|,
name|writable
argument_list|)
expr_stmt|;
block|}
block|}
name|lazySimpleDeserializeRead
operator|.
name|extraFieldsCheck
argument_list|()
expr_stmt|;
name|TestCase
operator|.
name|assertTrue
argument_list|(
operator|!
name|lazySimpleDeserializeRead
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
name|lazySimpleDeserializeRead
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
name|LazyStruct
name|lazySimpleStruct
init|=
operator|(
name|LazyStruct
operator|)
name|serde
operator|.
name|deserialize
argument_list|(
name|bytesWritable
argument_list|)
decl_stmt|;
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
name|columnCount
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
name|Writable
name|writable
init|=
operator|(
name|Writable
operator|)
name|row
index|[
name|index
index|]
decl_stmt|;
name|LazyPrimitive
name|lazyPrimitive
init|=
operator|(
name|LazyPrimitive
operator|)
name|lazySimpleStruct
operator|.
name|getField
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|Object
name|object
decl_stmt|;
if|if
condition|(
name|lazyPrimitive
operator|!=
literal|null
condition|)
block|{
name|object
operator|=
name|lazyPrimitive
operator|.
name|getWritableObject
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|object
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|writable
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
name|writable
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
name|writable
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
name|byte
index|[]
index|[]
name|serdeBytes
init|=
operator|new
name|byte
index|[
name|rowCount
index|]
index|[]
decl_stmt|;
comment|// Serialize using the SerDe, then below deserialize using DeserializeRead.
name|Object
index|[]
name|serdeRow
init|=
operator|new
name|Object
index|[
name|columnCount
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
comment|// LazySimple seems to work better with an row object array instead of a Java object...
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
name|Text
name|serialized
init|=
operator|(
name|Text
operator|)
name|serde
operator|.
name|serialize
argument_list|(
name|serdeRow
argument_list|,
name|rowOI
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes1
init|=
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
name|copyBytes
argument_list|(
name|serialized
argument_list|)
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
name|LazySimpleDeserializeRead
name|lazySimpleDeserializeRead
init|=
operator|new
name|LazySimpleDeserializeRead
argument_list|(
name|writePrimitiveTypeInfos
argument_list|,
name|separator
argument_list|,
name|serdeParams
argument_list|)
decl_stmt|;
if|if
condition|(
name|useIncludeColumns
condition|)
block|{
name|lazySimpleDeserializeRead
operator|.
name|setColumnsToInclude
argument_list|(
name|columnsToInclude
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|bytes
init|=
name|serdeBytes
index|[
name|i
index|]
decl_stmt|;
name|lazySimpleDeserializeRead
operator|.
name|set
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
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
name|index
operator|>=
name|writeColumnCount
operator|||
operator|(
name|useIncludeColumns
operator|&&
operator|!
name|columnsToInclude
index|[
name|index
index|]
operator|)
condition|)
block|{
comment|// Should come back a null.
name|VerifyFast
operator|.
name|verifyDeserializeRead
argument_list|(
name|lazySimpleDeserializeRead
argument_list|,
name|primitiveTypeInfos
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
name|Writable
name|writable
init|=
operator|(
name|Writable
operator|)
name|row
index|[
name|index
index|]
decl_stmt|;
name|VerifyFast
operator|.
name|verifyDeserializeRead
argument_list|(
name|lazySimpleDeserializeRead
argument_list|,
name|primitiveTypeInfos
index|[
name|index
index|]
argument_list|,
name|writable
argument_list|)
expr_stmt|;
block|}
block|}
name|lazySimpleDeserializeRead
operator|.
name|extraFieldsCheck
argument_list|()
expr_stmt|;
name|TestCase
operator|.
name|assertTrue
argument_list|(
operator|!
name|lazySimpleDeserializeRead
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
name|lazySimpleDeserializeRead
operator|.
name|bufferRangeHasExtraDataWarned
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|byte
index|[]
name|copyBytes
parameter_list|(
name|Text
name|serialized
parameter_list|)
block|{
name|byte
index|[]
name|result
init|=
operator|new
name|byte
index|[
name|serialized
operator|.
name|getLength
argument_list|()
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|serialized
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|result
argument_list|,
literal|0
argument_list|,
name|serialized
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|private
name|Properties
name|createProperties
parameter_list|(
name|String
name|fieldNames
parameter_list|,
name|String
name|fieldTypes
parameter_list|)
block|{
name|Properties
name|tbl
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
comment|// Set the configuration parameters
name|tbl
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
literal|"9"
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setProperty
argument_list|(
literal|"columns"
argument_list|,
name|fieldNames
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setProperty
argument_list|(
literal|"columns.types"
argument_list|,
name|fieldTypes
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_NULL_FORMAT
argument_list|,
literal|"NULL"
argument_list|)
expr_stmt|;
return|return
name|tbl
return|;
block|}
specifier|private
name|LazySimpleSerDe
name|getSerDe
parameter_list|(
name|String
name|fieldNames
parameter_list|,
name|String
name|fieldTypes
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Create the SerDe
name|LazySimpleSerDe
name|serDe
init|=
operator|new
name|LazySimpleSerDe
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Properties
name|tbl
init|=
name|createProperties
argument_list|(
name|fieldNames
argument_list|,
name|fieldTypes
argument_list|)
decl_stmt|;
name|SerDeUtils
operator|.
name|initializeSerDe
argument_list|(
name|serDe
argument_list|,
name|conf
argument_list|,
name|tbl
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|serDe
return|;
block|}
specifier|private
name|LazySerDeParameters
name|getSerDeParams
parameter_list|(
name|String
name|fieldNames
parameter_list|,
name|String
name|fieldTypes
parameter_list|)
throws|throws
name|SerDeException
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|Properties
name|tbl
init|=
name|createProperties
argument_list|(
name|fieldNames
argument_list|,
name|fieldTypes
argument_list|)
decl_stmt|;
return|return
operator|new
name|LazySerDeParameters
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|,
name|LazySimpleSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|void
name|testLazySimpleFastCase
parameter_list|(
name|int
name|caseNum
parameter_list|,
name|boolean
name|doNonRandomFill
parameter_list|,
name|Random
name|r
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
argument_list|)
expr_stmt|;
name|int
name|rowCount
init|=
literal|1000
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
name|PrimitiveTypeInfo
index|[]
name|primitiveTypeInfos
init|=
name|source
operator|.
name|primitiveTypeInfos
argument_list|()
decl_stmt|;
name|int
name|columnCount
init|=
name|primitiveTypeInfos
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
name|LazySimpleSerDe
name|serde
init|=
name|getSerDe
argument_list|(
name|fieldNames
argument_list|,
name|fieldTypes
argument_list|)
decl_stmt|;
name|LazySerDeParameters
name|serdeParams
init|=
name|getSerDeParams
argument_list|(
name|fieldNames
argument_list|,
name|fieldTypes
argument_list|)
decl_stmt|;
name|LazySimpleSerDe
name|serde_fewer
init|=
literal|null
decl_stmt|;
name|LazySerDeParameters
name|serdeParams_fewer
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
name|getSerDe
argument_list|(
name|fieldNames
argument_list|,
name|fieldTypes
argument_list|)
expr_stmt|;
name|serdeParams_fewer
operator|=
name|getSerDeParams
argument_list|(
name|partialFieldNames
argument_list|,
name|partialFieldTypes
argument_list|)
expr_stmt|;
block|}
name|byte
name|separator
init|=
operator|(
name|byte
operator|)
literal|'\t'
decl_stmt|;
name|testLazySimpleFast
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
name|separator
argument_list|,
name|serdeParams
argument_list|,
name|serdeParams_fewer
argument_list|,
name|primitiveTypeInfos
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
name|testLazySimpleFast
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
name|separator
argument_list|,
name|serdeParams
argument_list|,
name|serdeParams_fewer
argument_list|,
name|primitiveTypeInfos
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
if|if
condition|(
name|doWriteFewerColumns
condition|)
block|{
name|testLazySimpleFast
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
name|separator
argument_list|,
name|serdeParams
argument_list|,
name|serdeParams_fewer
argument_list|,
name|primitiveTypeInfos
argument_list|,
comment|/* useIncludeColumns */
literal|false
argument_list|,
comment|/* doWriteFewerColumns */
literal|true
argument_list|,
name|r
argument_list|)
expr_stmt|;
name|testLazySimpleFast
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
name|separator
argument_list|,
name|serdeParams
argument_list|,
name|serdeParams_fewer
argument_list|,
name|primitiveTypeInfos
argument_list|,
comment|/* useIncludeColumns */
literal|true
argument_list|,
comment|/* doWriteFewerColumns */
literal|true
argument_list|,
name|r
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testLazySimpleFast
parameter_list|()
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
literal|35790
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
name|testLazySimpleFastCase
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
block|}
end_class

end_unit

