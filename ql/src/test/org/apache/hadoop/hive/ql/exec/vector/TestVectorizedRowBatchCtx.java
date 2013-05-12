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
name|vector
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

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
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|RCFile
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
name|RCFileOutputFormat
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
name|FloatWritable
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
name|ObjectWritable
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|compress
operator|.
name|DefaultCodec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * Class that tests the functionality of VectorizedRowBatchCtx  */
end_comment

begin_class
specifier|public
class|class
name|TestVectorizedRowBatchCtx
block|{
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Path
name|testFilePath
decl_stmt|;
specifier|private
name|int
name|colCount
decl_stmt|;
specifier|private
name|ColumnarSerDe
name|serDe
decl_stmt|;
specifier|private
name|Properties
name|tbl
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|openFileSystem
parameter_list|()
throws|throws
name|Exception
block|{
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Path
name|workDir
init|=
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|,
literal|"target"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"test"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|fs
operator|.
name|setWorkingDirectory
argument_list|(
name|workDir
argument_list|)
expr_stmt|;
name|testFilePath
operator|=
operator|new
name|Path
argument_list|(
literal|"TestVectorizedRowBatchCtx.testDump.rc"
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|testFilePath
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|InitSerde
parameter_list|()
block|{
name|tbl
operator|=
operator|new
name|Properties
argument_list|()
expr_stmt|;
comment|// Set the configuration parameters
name|tbl
operator|.
name|setProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_FORMAT
argument_list|,
literal|"6"
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setProperty
argument_list|(
literal|"columns"
argument_list|,
literal|"ashort,aint,along,adouble,afloat,astring"
argument_list|)
expr_stmt|;
name|tbl
operator|.
name|setProperty
argument_list|(
literal|"columns.types"
argument_list|,
literal|"smallint:int:bigint:double:float:string"
argument_list|)
expr_stmt|;
name|colCount
operator|=
literal|6
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
try|try
block|{
name|serDe
operator|=
operator|new
name|ColumnarSerDe
argument_list|()
expr_stmt|;
name|serDe
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|WriteRCFile
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|file
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|SerDeException
block|{
name|fs
operator|.
name|delete
argument_list|(
name|file
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|RCFileOutputFormat
operator|.
name|setColumnNumber
argument_list|(
name|conf
argument_list|,
name|colCount
argument_list|)
expr_stmt|;
name|RCFile
operator|.
name|Writer
name|writer
init|=
operator|new
name|RCFile
operator|.
name|Writer
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|file
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|DefaultCodec
argument_list|()
argument_list|)
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
operator|++
name|i
control|)
block|{
name|BytesRefArrayWritable
name|bytes
init|=
operator|new
name|BytesRefArrayWritable
argument_list|(
name|colCount
argument_list|)
decl_stmt|;
name|BytesRefWritable
name|cu
decl_stmt|;
if|if
condition|(
name|i
operator|%
literal|3
operator|!=
literal|0
condition|)
block|{
name|cu
operator|=
operator|new
name|BytesRefWritable
argument_list|(
operator|(
name|i
operator|+
literal|""
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|,
literal|0
argument_list|,
operator|(
name|i
operator|+
literal|""
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
name|bytes
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|cu
argument_list|)
expr_stmt|;
name|cu
operator|=
operator|new
name|BytesRefWritable
argument_list|(
operator|(
name|i
operator|+
literal|100
operator|+
literal|""
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|,
literal|0
argument_list|,
operator|(
name|i
operator|+
literal|100
operator|+
literal|""
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
name|bytes
operator|.
name|set
argument_list|(
literal|1
argument_list|,
name|cu
argument_list|)
expr_stmt|;
name|cu
operator|=
operator|new
name|BytesRefWritable
argument_list|(
operator|(
name|i
operator|+
literal|200
operator|+
literal|""
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|,
literal|0
argument_list|,
operator|(
name|i
operator|+
literal|200
operator|+
literal|""
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
name|bytes
operator|.
name|set
argument_list|(
literal|2
argument_list|,
name|cu
argument_list|)
expr_stmt|;
name|cu
operator|=
operator|new
name|BytesRefWritable
argument_list|(
operator|(
name|i
operator|+
literal|1.23
operator|+
literal|""
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|,
literal|0
argument_list|,
operator|(
name|i
operator|+
literal|1.23
operator|+
literal|""
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
name|bytes
operator|.
name|set
argument_list|(
literal|3
argument_list|,
name|cu
argument_list|)
expr_stmt|;
name|cu
operator|=
operator|new
name|BytesRefWritable
argument_list|(
operator|(
name|i
operator|+
literal|2.23
operator|+
literal|""
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|,
literal|0
argument_list|,
operator|(
name|i
operator|+
literal|2.23
operator|+
literal|""
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
name|bytes
operator|.
name|set
argument_list|(
literal|4
argument_list|,
name|cu
argument_list|)
expr_stmt|;
name|cu
operator|=
operator|new
name|BytesRefWritable
argument_list|(
operator|(
literal|"Test string"
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|,
literal|0
argument_list|,
operator|(
literal|"Test string"
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
name|bytes
operator|.
name|set
argument_list|(
literal|5
argument_list|,
name|cu
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cu
operator|=
operator|new
name|BytesRefWritable
argument_list|(
operator|(
name|i
operator|+
literal|""
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|,
literal|0
argument_list|,
operator|(
name|i
operator|+
literal|""
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
name|bytes
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|cu
argument_list|)
expr_stmt|;
name|cu
operator|=
operator|new
name|BytesRefWritable
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|bytes
operator|.
name|set
argument_list|(
literal|1
argument_list|,
name|cu
argument_list|)
expr_stmt|;
name|cu
operator|=
operator|new
name|BytesRefWritable
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|bytes
operator|.
name|set
argument_list|(
literal|2
argument_list|,
name|cu
argument_list|)
expr_stmt|;
name|cu
operator|=
operator|new
name|BytesRefWritable
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|bytes
operator|.
name|set
argument_list|(
literal|3
argument_list|,
name|cu
argument_list|)
expr_stmt|;
name|cu
operator|=
operator|new
name|BytesRefWritable
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|bytes
operator|.
name|set
argument_list|(
literal|4
argument_list|,
name|cu
argument_list|)
expr_stmt|;
name|cu
operator|=
operator|new
name|BytesRefWritable
argument_list|(
operator|(
literal|"Test string"
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|,
literal|0
argument_list|,
operator|(
literal|"Test string"
operator|)
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
name|bytes
operator|.
name|set
argument_list|(
literal|5
argument_list|,
name|cu
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|append
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|VectorizedRowBatch
name|GetRowBatch
parameter_list|()
throws|throws
name|SerDeException
throws|,
name|HiveException
throws|,
name|IOException
block|{
name|RCFile
operator|.
name|Reader
name|reader
init|=
operator|new
name|RCFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|this
operator|.
name|testFilePath
argument_list|,
name|conf
argument_list|)
decl_stmt|;
comment|// Get object inspector
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
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fieldRefs
init|=
name|oi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Field size should be 6"
argument_list|,
name|colCount
argument_list|,
name|fieldRefs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Create the context
name|VectorizedRowBatchCtx
name|ctx
init|=
operator|new
name|VectorizedRowBatchCtx
argument_list|(
name|oi
argument_list|,
name|oi
argument_list|,
name|serDe
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|VectorizedRowBatch
name|batch
init|=
name|ctx
operator|.
name|CreateVectorizedRowBatch
argument_list|()
decl_stmt|;
name|VectorizedBatchUtil
operator|.
name|SetNoNullFields
argument_list|(
literal|true
argument_list|,
name|batch
argument_list|)
expr_stmt|;
comment|// Iterate thru the rows and populate the batch
name|LongWritable
name|rowID
init|=
operator|new
name|LongWritable
argument_list|()
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
name|reader
operator|.
name|next
argument_list|(
name|rowID
argument_list|)
expr_stmt|;
name|BytesRefArrayWritable
name|cols
init|=
operator|new
name|BytesRefArrayWritable
argument_list|()
decl_stmt|;
name|reader
operator|.
name|getCurrentRow
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|cols
operator|.
name|resetValid
argument_list|(
name|colCount
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|AddRowToBatch
argument_list|(
name|i
argument_list|,
name|cols
argument_list|,
name|batch
argument_list|)
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|batch
operator|.
name|size
operator|=
literal|10
expr_stmt|;
return|return
name|batch
return|;
block|}
name|void
name|ValidateRowBatch
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
throws|throws
name|IOException
throws|,
name|SerDeException
block|{
name|LongWritable
name|rowID
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
name|RCFile
operator|.
name|Reader
name|reader
init|=
operator|new
name|RCFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|this
operator|.
name|testFilePath
argument_list|,
name|conf
argument_list|)
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
name|batch
operator|.
name|size
condition|;
name|i
operator|++
control|)
block|{
name|reader
operator|.
name|next
argument_list|(
name|rowID
argument_list|)
expr_stmt|;
name|BytesRefArrayWritable
name|cols
init|=
operator|new
name|BytesRefArrayWritable
argument_list|()
decl_stmt|;
name|reader
operator|.
name|getCurrentRow
argument_list|(
name|cols
argument_list|)
expr_stmt|;
name|cols
operator|.
name|resetValid
argument_list|(
name|colCount
argument_list|)
expr_stmt|;
name|Object
name|row
init|=
name|serDe
operator|.
name|deserialize
argument_list|(
name|cols
argument_list|)
decl_stmt|;
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
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fieldRefs
init|=
name|oi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|fieldRefs
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|Object
name|fieldData
init|=
name|oi
operator|.
name|getStructFieldData
argument_list|(
name|row
argument_list|,
name|fieldRefs
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
decl_stmt|;
name|ObjectInspector
name|foi
init|=
name|fieldRefs
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
decl_stmt|;
comment|// Vectorization only supports PRIMITIVE data types. Assert the same
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|foi
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|PRIMITIVE
argument_list|)
expr_stmt|;
name|PrimitiveObjectInspector
name|poi
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|foi
decl_stmt|;
name|Object
name|writableCol
init|=
name|poi
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|fieldData
argument_list|)
decl_stmt|;
if|if
condition|(
name|writableCol
operator|!=
literal|null
condition|)
block|{
switch|switch
condition|(
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|SHORT
case|:
block|{
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|j
index|]
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|lcv
operator|.
name|vector
index|[
name|i
index|]
operator|==
operator|(
operator|(
name|ShortWritable
operator|)
name|writableCol
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|INT
case|:
block|{
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|j
index|]
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|lcv
operator|.
name|vector
index|[
name|i
index|]
operator|==
operator|(
operator|(
name|IntWritable
operator|)
name|writableCol
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|LONG
case|:
block|{
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|j
index|]
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|lcv
operator|.
name|vector
index|[
name|i
index|]
operator|==
operator|(
operator|(
name|LongWritable
operator|)
name|writableCol
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|FLOAT
case|:
block|{
name|DoubleColumnVector
name|dcv
init|=
operator|(
name|DoubleColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|j
index|]
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|dcv
operator|.
name|vector
index|[
name|i
index|]
operator|==
operator|(
operator|(
name|FloatWritable
operator|)
name|writableCol
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|DOUBLE
case|:
block|{
name|DoubleColumnVector
name|dcv
init|=
operator|(
name|DoubleColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|j
index|]
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|dcv
operator|.
name|vector
index|[
name|i
index|]
operator|==
operator|(
operator|(
name|DoubleWritable
operator|)
name|writableCol
operator|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|STRING
case|:
block|{
name|BytesColumnVector
name|bcv
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|j
index|]
decl_stmt|;
name|Text
name|colText
init|=
operator|(
name|Text
operator|)
name|writableCol
decl_stmt|;
name|Text
name|batchText
init|=
operator|(
name|Text
operator|)
name|bcv
operator|.
name|getWritableObject
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|String
name|a
init|=
name|colText
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|b
init|=
name|batchText
operator|.
name|toString
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|a
operator|.
name|equals
argument_list|(
name|b
argument_list|)
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Unknown type"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|batch
operator|.
name|cols
index|[
name|j
index|]
operator|.
name|isNull
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Check repeating
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|batch
operator|.
name|cols
index|[
literal|1
index|]
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|batch
operator|.
name|cols
index|[
literal|2
index|]
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|batch
operator|.
name|cols
index|[
literal|3
index|]
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|batch
operator|.
name|cols
index|[
literal|4
index|]
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
comment|// Check non null
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|batch
operator|.
name|cols
index|[
literal|1
index|]
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|batch
operator|.
name|cols
index|[
literal|2
index|]
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|batch
operator|.
name|cols
index|[
literal|3
index|]
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|batch
operator|.
name|cols
index|[
literal|4
index|]
operator|.
name|noNulls
argument_list|)
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|TestCtx
parameter_list|()
throws|throws
name|Exception
block|{
name|InitSerde
argument_list|()
expr_stmt|;
name|WriteRCFile
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|this
operator|.
name|testFilePath
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
name|VectorizedRowBatch
name|batch
init|=
name|GetRowBatch
argument_list|()
decl_stmt|;
name|ValidateRowBatch
argument_list|(
name|batch
argument_list|)
expr_stmt|;
comment|// Test VectorizedColumnarSerDe
name|VectorizedColumnarSerDe
name|vcs
init|=
operator|new
name|VectorizedColumnarSerDe
argument_list|()
decl_stmt|;
name|vcs
operator|.
name|initialize
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
name|Writable
name|w
init|=
name|vcs
operator|.
name|serializeVector
argument_list|(
name|batch
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|serDe
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
decl_stmt|;
name|BytesRefArrayWritable
index|[]
name|refArray
init|=
operator|(
name|BytesRefArrayWritable
index|[]
operator|)
operator|(
operator|(
name|ObjectWritable
operator|)
name|w
operator|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|vcs
operator|.
name|deserializeVector
argument_list|(
name|refArray
argument_list|,
literal|10
argument_list|,
name|batch
argument_list|)
expr_stmt|;
name|ValidateRowBatch
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

