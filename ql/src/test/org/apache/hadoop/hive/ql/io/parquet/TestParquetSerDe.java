begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|parquet
package|;
end_package

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
name|ql
operator|.
name|io
operator|.
name|parquet
operator|.
name|serde
operator|.
name|ParquetHiveSerDe
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
name|ColumnProjectionUtils
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
name|io
operator|.
name|ByteWritable
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
name|io
operator|.
name|DoubleWritable
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
name|io
operator|.
name|ParquetHiveRecord
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
name|io
operator|.
name|ShortWritable
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
name|ArrayWritable
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
name|IntWritable
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
name|LongWritable
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
name|TestParquetSerDe
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testParquetHiveSerDe
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
literal|"test: testParquetHiveSerDe"
argument_list|)
expr_stmt|;
specifier|final
name|ParquetHiveSerDe
name|serDe
init|=
operator|new
name|ParquetHiveSerDe
argument_list|()
decl_stmt|;
specifier|final
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
specifier|final
name|Properties
name|tbl
init|=
name|createProperties
argument_list|()
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
comment|// Data
specifier|final
name|Writable
index|[]
name|arr
init|=
operator|new
name|Writable
index|[
literal|9
index|]
decl_stmt|;
comment|//primitive types
name|arr
index|[
literal|0
index|]
operator|=
operator|new
name|ByteWritable
argument_list|(
operator|(
name|byte
operator|)
literal|123
argument_list|)
expr_stmt|;
name|arr
index|[
literal|1
index|]
operator|=
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|456
argument_list|)
expr_stmt|;
name|arr
index|[
literal|2
index|]
operator|=
operator|new
name|IntWritable
argument_list|(
literal|789
argument_list|)
expr_stmt|;
name|arr
index|[
literal|3
index|]
operator|=
operator|new
name|LongWritable
argument_list|(
literal|1000l
argument_list|)
expr_stmt|;
name|arr
index|[
literal|4
index|]
operator|=
operator|new
name|DoubleWritable
argument_list|(
operator|(
name|double
operator|)
literal|5.3
argument_list|)
expr_stmt|;
name|arr
index|[
literal|5
index|]
operator|=
operator|new
name|BytesWritable
argument_list|(
literal|"hive and hadoop and parquet. Big family."
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
name|arr
index|[
literal|6
index|]
operator|=
operator|new
name|BytesWritable
argument_list|(
literal|"parquetSerde binary"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|Writable
index|[]
name|map
init|=
operator|new
name|Writable
index|[
literal|3
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
literal|3
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|Writable
index|[]
name|pair
init|=
operator|new
name|Writable
index|[
literal|2
index|]
decl_stmt|;
name|pair
index|[
literal|0
index|]
operator|=
operator|new
name|BytesWritable
argument_list|(
operator|(
literal|"key_"
operator|+
name|i
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
name|pair
index|[
literal|1
index|]
operator|=
operator|new
name|IntWritable
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|map
index|[
name|i
index|]
operator|=
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|pair
argument_list|)
expr_stmt|;
block|}
name|arr
index|[
literal|7
index|]
operator|=
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|map
argument_list|)
expr_stmt|;
specifier|final
name|Writable
index|[]
name|array
init|=
operator|new
name|Writable
index|[
literal|5
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
literal|5
condition|;
operator|++
name|i
control|)
block|{
name|array
index|[
name|i
index|]
operator|=
operator|new
name|BytesWritable
argument_list|(
operator|(
literal|"elem_"
operator|+
name|i
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|arr
index|[
literal|8
index|]
operator|=
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|array
argument_list|)
expr_stmt|;
specifier|final
name|ArrayWritable
name|arrWritable
init|=
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|arr
argument_list|)
decl_stmt|;
comment|// Test
name|deserializeAndSerializeLazySimple
argument_list|(
name|serDe
argument_list|,
name|arrWritable
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"test: testParquetHiveSerDe - OK"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
specifier|final
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
name|testParquetHiveSerDeComplexTypes
parameter_list|()
throws|throws
name|Throwable
block|{
comment|// Initialize
name|ParquetHiveSerDe
name|serDe
init|=
operator|new
name|ParquetHiveSerDe
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
name|tblProperties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|tblProperties
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|,
literal|"a,s"
argument_list|)
expr_stmt|;
name|tblProperties
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|,
literal|"int,struct<a:int,b:string>"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|ColumnProjectionUtils
operator|.
name|READ_NESTED_COLUMN_PATH_CONF_STR
argument_list|,
literal|"s.b"
argument_list|)
expr_stmt|;
name|serDe
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|tblProperties
argument_list|)
expr_stmt|;
comment|// Generate test data
name|Writable
index|[]
name|wb
init|=
operator|new
name|Writable
index|[
literal|1
index|]
decl_stmt|;
name|wb
index|[
literal|0
index|]
operator|=
operator|new
name|BytesWritable
argument_list|(
literal|"foo"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
name|Writable
index|[]
name|ws
init|=
operator|new
name|Writable
index|[
literal|2
index|]
decl_stmt|;
name|ws
index|[
literal|0
index|]
operator|=
literal|null
expr_stmt|;
name|ArrayWritable
name|awb
init|=
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|wb
argument_list|)
decl_stmt|;
name|ws
index|[
literal|1
index|]
operator|=
name|awb
expr_stmt|;
name|ArrayWritable
name|aws
init|=
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
name|ws
argument_list|)
decl_stmt|;
comment|// Inspect the test data
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|serDe
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|StructField
name|s
init|=
name|soi
operator|.
name|getStructFieldRef
argument_list|(
literal|"s"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|awb
argument_list|,
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|aws
argument_list|,
name|s
argument_list|)
argument_list|)
expr_stmt|;
name|StructObjectInspector
name|boi
init|=
operator|(
name|StructObjectInspector
operator|)
name|s
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
name|StructField
name|b
init|=
name|boi
operator|.
name|getStructFieldRef
argument_list|(
literal|"b"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|wb
index|[
literal|0
index|]
argument_list|,
name|boi
operator|.
name|getStructFieldData
argument_list|(
name|awb
argument_list|,
name|b
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|deserializeAndSerializeLazySimple
parameter_list|(
specifier|final
name|ParquetHiveSerDe
name|serDe
parameter_list|,
specifier|final
name|ArrayWritable
name|t
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Get the row structure
specifier|final
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
specifier|final
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
literal|"deserialization gives the wrong object class"
argument_list|,
name|row
operator|.
name|getClass
argument_list|()
argument_list|,
name|ArrayWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"size correct after deserialization"
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
name|get
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"deserialization gives the wrong object"
argument_list|,
name|t
argument_list|,
name|row
argument_list|)
expr_stmt|;
comment|// Serialize
specifier|final
name|ParquetHiveRecord
name|serializedArr
init|=
operator|(
name|ParquetHiveRecord
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
literal|"size correct after serialization"
argument_list|,
name|serDe
operator|.
name|getSerDeStats
argument_list|()
operator|.
name|getRawDataSize
argument_list|()
argument_list|,
operator|(
operator|(
name|ArrayWritable
operator|)
name|serializedArr
operator|.
name|getObject
argument_list|()
operator|)
operator|.
name|get
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"serialized object should be equal to starting object"
argument_list|,
name|arrayWritableEquals
argument_list|(
name|t
argument_list|,
operator|(
name|ArrayWritable
operator|)
name|serializedArr
operator|.
name|getObject
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Properties
name|createProperties
parameter_list|()
block|{
specifier|final
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
literal|"columns"
argument_list|,
literal|"abyte,ashort,aint,along,adouble,astring,abinary,amap,alist"
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setProperty
argument_list|(
literal|"columns.types"
argument_list|,
literal|"tinyint:smallint:int:bigint:double:string:binary:map<string,int>:array<string>"
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setProperty
argument_list|(
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
specifier|public
specifier|static
name|boolean
name|arrayWritableEquals
parameter_list|(
specifier|final
name|ArrayWritable
name|a1
parameter_list|,
specifier|final
name|ArrayWritable
name|a2
parameter_list|)
block|{
specifier|final
name|Writable
index|[]
name|a1Arr
init|=
name|a1
operator|.
name|get
argument_list|()
decl_stmt|;
specifier|final
name|Writable
index|[]
name|a2Arr
init|=
name|a2
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|a1Arr
operator|.
name|length
operator|!=
name|a2Arr
operator|.
name|length
condition|)
block|{
return|return
literal|false
return|;
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
name|a1Arr
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|a1Arr
index|[
name|i
index|]
operator|instanceof
name|ArrayWritable
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|a2Arr
index|[
name|i
index|]
operator|instanceof
name|ArrayWritable
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|arrayWritableEquals
argument_list|(
operator|(
name|ArrayWritable
operator|)
name|a1Arr
index|[
name|i
index|]
argument_list|,
operator|(
name|ArrayWritable
operator|)
name|a2Arr
index|[
name|i
index|]
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|a1Arr
index|[
name|i
index|]
operator|.
name|equals
argument_list|(
name|a2Arr
index|[
name|i
index|]
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

