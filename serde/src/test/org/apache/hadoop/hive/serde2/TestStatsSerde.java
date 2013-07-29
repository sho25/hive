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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
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
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|binarysortable
operator|.
name|MyTestInnerStruct
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
name|TestBinarySortableSerDe
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
name|columnar
operator|.
name|BytesRefArrayWritable
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
name|columnar
operator|.
name|BytesRefWritable
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
name|columnar
operator|.
name|ColumnarSerDe
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
name|LazySimpleSerDe
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

begin_class
specifier|public
class|class
name|TestStatsSerde
extends|extends
name|TestCase
block|{
specifier|public
name|TestStatsSerde
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test LazySimpleSerDe    */
specifier|public
name|void
name|testLazySimpleSerDe
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
comment|// Create the SerDe
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"test: testLazySimpleSerDe"
argument_list|)
expr_stmt|;
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
argument_list|()
decl_stmt|;
name|serDe
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
comment|// Data
name|Text
name|t
init|=
operator|new
name|Text
argument_list|(
literal|"123\t456\t789\t1000\t5.3\thive and hadoop\t1.\tNULL"
argument_list|)
decl_stmt|;
comment|// Test
name|deserializeAndSerializeLazySimple
argument_list|(
name|serDe
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"test: testLazySimpleSerDe - OK"
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
specifier|private
name|void
name|deserializeAndSerializeLazySimple
parameter_list|(
name|LazySimpleSerDe
name|serDe
parameter_list|,
name|Text
name|t
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Get the row structure
name|StructObjectInspector
name|oi
init|=
operator|(
name|StructObjectInspector
operator|)
name|serDe
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
comment|// Deserialize
name|Object
name|row
init|=
name|serDe
operator|.
name|deserialize
argument_list|(
name|t
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"serialized size correct after deserialization"
argument_list|,
name|serDe
operator|.
name|getSerDeStats
argument_list|()
operator|.
name|getRawDataSize
argument_list|()
argument_list|,
name|t
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
comment|// Serialize
name|Text
name|serializedText
init|=
operator|(
name|Text
operator|)
name|serDe
operator|.
name|serialize
argument_list|(
name|row
argument_list|,
name|oi
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"serialized size correct after serialization"
argument_list|,
name|serDe
operator|.
name|getSerDeStats
argument_list|()
operator|.
name|getRawDataSize
argument_list|()
argument_list|,
name|serializedText
operator|.
name|toString
argument_list|()
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test LazyBinarySerDe    */
specifier|public
name|void
name|testLazyBinarySerDe
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"test: testLazyBinarySerDe"
argument_list|)
expr_stmt|;
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
name|MyTestClass
name|rows
index|[]
init|=
operator|new
name|MyTestClass
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
name|int
name|randField
init|=
name|r
operator|.
name|nextInt
argument_list|(
literal|12
argument_list|)
decl_stmt|;
name|Byte
name|b
init|=
name|randField
operator|>
literal|0
condition|?
literal|null
else|:
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
name|r
operator|.
name|nextInt
argument_list|()
argument_list|)
decl_stmt|;
name|Short
name|s
init|=
name|randField
operator|>
literal|1
condition|?
literal|null
else|:
name|Short
operator|.
name|valueOf
argument_list|(
operator|(
name|short
operator|)
name|r
operator|.
name|nextInt
argument_list|()
argument_list|)
decl_stmt|;
name|Integer
name|n
init|=
name|randField
operator|>
literal|2
condition|?
literal|null
else|:
name|Integer
operator|.
name|valueOf
argument_list|(
name|r
operator|.
name|nextInt
argument_list|()
argument_list|)
decl_stmt|;
name|Long
name|l
init|=
name|randField
operator|>
literal|3
condition|?
literal|null
else|:
name|Long
operator|.
name|valueOf
argument_list|(
name|r
operator|.
name|nextLong
argument_list|()
argument_list|)
decl_stmt|;
name|Float
name|f
init|=
name|randField
operator|>
literal|4
condition|?
literal|null
else|:
name|Float
operator|.
name|valueOf
argument_list|(
name|r
operator|.
name|nextFloat
argument_list|()
argument_list|)
decl_stmt|;
name|Double
name|d
init|=
name|randField
operator|>
literal|5
condition|?
literal|null
else|:
name|Double
operator|.
name|valueOf
argument_list|(
name|r
operator|.
name|nextDouble
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|st
init|=
name|randField
operator|>
literal|6
condition|?
literal|null
else|:
name|TestBinarySortableSerDe
operator|.
name|getRandString
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|HiveDecimal
name|bd
init|=
name|randField
operator|>
literal|7
condition|?
literal|null
else|:
name|TestBinarySortableSerDe
operator|.
name|getRandHiveDecimal
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|Date
name|date
init|=
name|randField
operator|>
literal|8
condition|?
literal|null
else|:
name|TestBinarySortableSerDe
operator|.
name|getRandDate
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|MyTestInnerStruct
name|is
init|=
name|randField
operator|>
literal|9
condition|?
literal|null
else|:
operator|new
name|MyTestInnerStruct
argument_list|(
name|r
operator|.
name|nextInt
argument_list|(
literal|5
argument_list|)
operator|-
literal|2
argument_list|,
name|r
operator|.
name|nextInt
argument_list|(
literal|5
argument_list|)
operator|-
literal|2
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|li
init|=
name|randField
operator|>
literal|10
condition|?
literal|null
else|:
name|TestBinarySortableSerDe
operator|.
name|getRandIntegerArray
argument_list|(
name|r
argument_list|)
decl_stmt|;
name|byte
index|[]
name|ba
init|=
name|TestBinarySortableSerDe
operator|.
name|getRandBA
argument_list|(
name|r
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|MyTestClass
name|t
init|=
operator|new
name|MyTestClass
argument_list|(
name|b
argument_list|,
name|s
argument_list|,
name|n
argument_list|,
name|l
argument_list|,
name|f
argument_list|,
name|d
argument_list|,
name|st
argument_list|,
name|bd
argument_list|,
name|date
argument_list|,
name|is
argument_list|,
name|li
argument_list|,
name|ba
argument_list|)
decl_stmt|;
name|rows
index|[
name|i
index|]
operator|=
name|t
expr_stmt|;
block|}
name|StructObjectInspector
name|rowOI
init|=
operator|(
name|StructObjectInspector
operator|)
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|MyTestClass
operator|.
name|class
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
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
name|Properties
name|schema
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
name|fieldNames
argument_list|)
expr_stmt|;
name|schema
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
name|fieldTypes
argument_list|)
expr_stmt|;
name|LazyBinarySerDe
name|serDe
init|=
operator|new
name|LazyBinarySerDe
argument_list|()
decl_stmt|;
name|serDe
operator|.
name|initialize
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|,
name|schema
argument_list|)
expr_stmt|;
name|deserializeAndSerializeLazyBinary
argument_list|(
name|serDe
argument_list|,
name|rows
argument_list|,
name|rowOI
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"test: testLazyBinarySerDe - OK"
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
specifier|private
name|void
name|deserializeAndSerializeLazyBinary
parameter_list|(
name|SerDe
name|serDe
parameter_list|,
name|Object
index|[]
name|rows
parameter_list|,
name|ObjectInspector
name|rowOI
parameter_list|)
throws|throws
name|Throwable
block|{
name|BytesWritable
name|bytes
index|[]
init|=
operator|new
name|BytesWritable
index|[
name|rows
operator|.
name|length
index|]
decl_stmt|;
name|int
name|lenS
init|=
literal|0
decl_stmt|;
name|int
name|lenD
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
name|rows
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|BytesWritable
name|s
init|=
operator|(
name|BytesWritable
operator|)
name|serDe
operator|.
name|serialize
argument_list|(
name|rows
index|[
name|i
index|]
argument_list|,
name|rowOI
argument_list|)
decl_stmt|;
name|lenS
operator|+=
name|serDe
operator|.
name|getSerDeStats
argument_list|()
operator|.
name|getRawDataSize
argument_list|()
expr_stmt|;
name|bytes
index|[
name|i
index|]
operator|=
operator|new
name|BytesWritable
argument_list|()
expr_stmt|;
name|bytes
index|[
name|i
index|]
operator|.
name|set
argument_list|(
name|s
argument_list|)
expr_stmt|;
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
name|rows
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|serDe
operator|.
name|deserialize
argument_list|(
name|bytes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|lenD
operator|+=
name|serDe
operator|.
name|getSerDeStats
argument_list|()
operator|.
name|getRawDataSize
argument_list|()
expr_stmt|;
block|}
comment|// serialized sizes after serialization and deserialization should be equal
name|assertEquals
argument_list|(
name|lenS
argument_list|,
name|lenD
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
literal|0
argument_list|,
name|lenS
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test ColumnarSerDe    */
specifier|public
name|void
name|testColumnarSerDe
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"test: testColumnarSerde"
argument_list|)
expr_stmt|;
comment|// Create the SerDe
name|ColumnarSerDe
name|serDe
init|=
operator|new
name|ColumnarSerDe
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
argument_list|()
decl_stmt|;
name|serDe
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
comment|// Data
name|BytesRefArrayWritable
name|braw
init|=
operator|new
name|BytesRefArrayWritable
argument_list|(
literal|8
argument_list|)
decl_stmt|;
name|String
index|[]
name|data
init|=
block|{
literal|"123"
block|,
literal|"456"
block|,
literal|"789"
block|,
literal|"1000"
block|,
literal|"5.3"
block|,
literal|"hive and hadoop"
block|,
literal|"1."
block|,
literal|"NULL"
block|}
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
literal|8
condition|;
name|i
operator|++
control|)
block|{
name|braw
operator|.
name|set
argument_list|(
name|i
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
name|data
index|[
name|i
index|]
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Test
name|deserializeAndSerializeColumnar
argument_list|(
name|serDe
argument_list|,
name|braw
argument_list|,
name|data
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"test: testColumnarSerde - OK"
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
specifier|private
name|void
name|deserializeAndSerializeColumnar
parameter_list|(
name|ColumnarSerDe
name|serDe
parameter_list|,
name|BytesRefArrayWritable
name|t
parameter_list|,
name|String
index|[]
name|data
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Get the row structure
name|StructObjectInspector
name|oi
init|=
operator|(
name|StructObjectInspector
operator|)
name|serDe
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
comment|// Deserialize
name|Object
name|row
init|=
name|serDe
operator|.
name|deserialize
argument_list|(
name|t
argument_list|)
decl_stmt|;
name|int
name|size
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
name|data
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|size
operator|+=
name|data
index|[
name|i
index|]
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"serialized size correct after deserialization"
argument_list|,
name|size
argument_list|,
name|serDe
operator|.
name|getSerDeStats
argument_list|()
operator|.
name|getRawDataSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|BytesRefArrayWritable
name|serializedData
init|=
operator|(
name|BytesRefArrayWritable
operator|)
name|serDe
operator|.
name|serialize
argument_list|(
name|row
argument_list|,
name|oi
argument_list|)
decl_stmt|;
name|size
operator|=
literal|0
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
name|serializedData
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|size
operator|+=
name|serializedData
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getLength
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"serialized size correct after serialization"
argument_list|,
name|size
argument_list|,
name|serDe
operator|.
name|getSerDeStats
argument_list|()
operator|.
name|getRawDataSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Properties
name|createProperties
parameter_list|()
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
literal|"abyte,ashort,aint,along,adouble,astring,anullint,anullstring"
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setProperty
argument_list|(
literal|"columns.types"
argument_list|,
literal|"tinyint:smallint:int:bigint:double:string:int:string"
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
block|}
end_class

end_unit

