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
name|io
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
name|io
operator|.
name|UnsupportedEncodingException
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
name|TestCase
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
name|serde
operator|.
name|Constants
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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
operator|.
name|ObjectInspectorCopyOption
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

begin_class
specifier|public
class|class
name|TestRCFile
extends|extends
name|TestCase
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
name|TestRCFile
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|ColumnarSerDe
name|serDe
decl_stmt|;
specifier|private
specifier|static
name|Path
name|file
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
name|Properties
name|tbl
decl_stmt|;
static|static
block|{
try|try
block|{
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
name|dir
init|=
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.data.dir"
argument_list|,
literal|"."
argument_list|)
operator|+
literal|"/mapred"
argument_list|)
decl_stmt|;
name|file
operator|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
literal|"test_rcfile"
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|dir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// the SerDe part is from TestLazySimpleSerDe
name|serDe
operator|=
operator|new
name|ColumnarSerDe
argument_list|()
expr_stmt|;
comment|// Create the SerDe
name|tbl
operator|=
name|createProperties
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
name|Exception
name|e
parameter_list|)
block|{     }
block|}
comment|// Data
specifier|private
specifier|static
name|Writable
index|[]
name|expectedFieldsData
init|=
block|{
operator|new
name|ByteWritable
argument_list|(
operator|(
name|byte
operator|)
literal|123
argument_list|)
block|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|456
argument_list|)
block|,
operator|new
name|IntWritable
argument_list|(
literal|789
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|1000
argument_list|)
block|,
operator|new
name|DoubleWritable
argument_list|(
literal|5.3
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"hive and hadoop"
argument_list|)
block|,
literal|null
block|,
literal|null
block|}
decl_stmt|;
specifier|private
specifier|static
name|Object
index|[]
name|expectedPartitalFieldsData
init|=
block|{
literal|null
block|,
literal|null
block|,
operator|new
name|IntWritable
argument_list|(
literal|789
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|1000
argument_list|)
block|,
literal|null
block|,
literal|null
block|,
literal|null
block|,
literal|null
block|}
decl_stmt|;
specifier|private
specifier|static
name|BytesRefArrayWritable
name|patialS
init|=
operator|new
name|BytesRefArrayWritable
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|bytesArray
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|BytesRefArrayWritable
name|s
init|=
literal|null
decl_stmt|;
static|static
block|{
try|try
block|{
name|bytesArray
operator|=
operator|new
name|byte
index|[]
index|[]
block|{
literal|"123"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
literal|"456"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
literal|"789"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
literal|"1000"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
literal|"5.3"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
literal|"hive and hadoop"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
operator|new
name|byte
index|[
literal|0
index|]
block|,
literal|"NULL"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|}
expr_stmt|;
name|s
operator|=
operator|new
name|BytesRefArrayWritable
argument_list|(
name|bytesArray
operator|.
name|length
argument_list|)
expr_stmt|;
name|s
operator|.
name|set
argument_list|(
literal|0
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
literal|"123"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|s
operator|.
name|set
argument_list|(
literal|1
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
literal|"456"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|s
operator|.
name|set
argument_list|(
literal|2
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
literal|"789"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|s
operator|.
name|set
argument_list|(
literal|3
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
literal|"1000"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|s
operator|.
name|set
argument_list|(
literal|4
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
literal|"5.3"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|s
operator|.
name|set
argument_list|(
literal|5
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
literal|"hive and hadoop"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|s
operator|.
name|set
argument_list|(
literal|6
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
literal|"NULL"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|s
operator|.
name|set
argument_list|(
literal|7
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
literal|"NULL"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// partial test init
name|patialS
operator|.
name|set
argument_list|(
literal|0
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
literal|"NULL"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|patialS
operator|.
name|set
argument_list|(
literal|1
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
literal|"NULL"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|patialS
operator|.
name|set
argument_list|(
literal|2
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
literal|"789"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|patialS
operator|.
name|set
argument_list|(
literal|3
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
literal|"1000"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|patialS
operator|.
name|set
argument_list|(
literal|4
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
literal|"NULL"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|patialS
operator|.
name|set
argument_list|(
literal|5
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
literal|"NULL"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|patialS
operator|.
name|set
argument_list|(
literal|6
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
literal|"NULL"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|patialS
operator|.
name|set
argument_list|(
literal|7
argument_list|,
operator|new
name|BytesRefWritable
argument_list|(
literal|"NULL"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{     }
block|}
specifier|public
name|void
name|testSimpleReadAndWrite
parameter_list|()
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
name|byte
index|[]
index|[]
name|record_1
init|=
block|{
literal|"123"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
literal|"456"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
literal|"789"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
literal|"1000"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
literal|"5.3"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
literal|"hive and hadoop"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
operator|new
name|byte
index|[
literal|0
index|]
block|,
literal|"NULL"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|}
decl_stmt|;
name|byte
index|[]
index|[]
name|record_2
init|=
block|{
literal|"100"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
literal|"200"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
literal|"123"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
literal|"1000"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
literal|"5.3"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
literal|"hive and hadoop"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|,
operator|new
name|byte
index|[
literal|0
index|]
block|,
literal|"NULL"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|}
decl_stmt|;
name|RCFileOutputFormat
operator|.
name|setColumnNumber
argument_list|(
name|conf
argument_list|,
name|expectedFieldsData
operator|.
name|length
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
operator|new
name|DefaultCodec
argument_list|()
argument_list|)
decl_stmt|;
name|BytesRefArrayWritable
name|bytes
init|=
operator|new
name|BytesRefArrayWritable
argument_list|(
name|record_1
operator|.
name|length
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
name|record_1
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|BytesRefWritable
name|cu
init|=
operator|new
name|BytesRefWritable
argument_list|(
name|record_1
index|[
name|i
index|]
argument_list|,
literal|0
argument_list|,
name|record_1
index|[
name|i
index|]
operator|.
name|length
argument_list|)
decl_stmt|;
name|bytes
operator|.
name|set
argument_list|(
name|i
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
name|bytes
operator|.
name|clear
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
name|record_2
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|BytesRefWritable
name|cu
init|=
operator|new
name|BytesRefWritable
argument_list|(
name|record_2
index|[
name|i
index|]
argument_list|,
literal|0
argument_list|,
name|record_2
index|[
name|i
index|]
operator|.
name|length
argument_list|)
decl_stmt|;
name|bytes
operator|.
name|set
argument_list|(
name|i
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
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|Object
index|[]
name|expectedRecord_1
init|=
block|{
operator|new
name|ByteWritable
argument_list|(
operator|(
name|byte
operator|)
literal|123
argument_list|)
block|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|456
argument_list|)
block|,
operator|new
name|IntWritable
argument_list|(
literal|789
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|1000
argument_list|)
block|,
operator|new
name|DoubleWritable
argument_list|(
literal|5.3
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"hive and hadoop"
argument_list|)
block|,
literal|null
block|,
literal|null
block|}
decl_stmt|;
name|Object
index|[]
name|expectedRecord_2
init|=
block|{
operator|new
name|ByteWritable
argument_list|(
operator|(
name|byte
operator|)
literal|100
argument_list|)
block|,
operator|new
name|ShortWritable
argument_list|(
operator|(
name|short
operator|)
literal|200
argument_list|)
block|,
operator|new
name|IntWritable
argument_list|(
literal|123
argument_list|)
block|,
operator|new
name|LongWritable
argument_list|(
literal|1000
argument_list|)
block|,
operator|new
name|DoubleWritable
argument_list|(
literal|5.3
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"hive and hadoop"
argument_list|)
block|,
literal|null
block|,
literal|null
block|}
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
name|file
argument_list|,
name|conf
argument_list|)
decl_stmt|;
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
literal|2
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
literal|8
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
name|assertEquals
argument_list|(
literal|"Field size should be 8"
argument_list|,
literal|8
argument_list|,
name|fieldRefs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
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
name|Object
name|standardWritableData
init|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|fieldData
argument_list|,
name|fieldRefs
operator|.
name|get
argument_list|(
name|j
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
name|assertEquals
argument_list|(
literal|"Field "
operator|+
name|i
argument_list|,
name|standardWritableData
argument_list|,
name|expectedRecord_1
index|[
name|j
index|]
argument_list|)
expr_stmt|;
else|else
name|assertEquals
argument_list|(
literal|"Field "
operator|+
name|i
argument_list|,
name|standardWritableData
argument_list|,
name|expectedRecord_2
index|[
name|j
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testWriteAndFullyRead
parameter_list|()
throws|throws
name|IOException
throws|,
name|SerDeException
block|{
name|writeTest
argument_list|(
name|fs
argument_list|,
literal|10000
argument_list|,
name|file
argument_list|,
name|bytesArray
argument_list|)
expr_stmt|;
name|fullyReadTest
argument_list|(
name|fs
argument_list|,
literal|10000
argument_list|,
name|file
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testWriteAndPartialRead
parameter_list|()
throws|throws
name|IOException
throws|,
name|SerDeException
block|{
name|writeTest
argument_list|(
name|fs
argument_list|,
literal|10000
argument_list|,
name|file
argument_list|,
name|bytesArray
argument_list|)
expr_stmt|;
name|partialReadTest
argument_list|(
name|fs
argument_list|,
literal|10000
argument_list|,
name|file
argument_list|)
expr_stmt|;
block|}
comment|/** For debugging and testing. */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|count
init|=
literal|10000
decl_stmt|;
name|boolean
name|create
init|=
literal|true
decl_stmt|;
name|String
name|usage
init|=
literal|"Usage: RCFile "
operator|+
literal|"[-count N]"
operator|+
literal|" file"
decl_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|usage
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|args
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
comment|// parse command line
if|if
condition|(
name|args
index|[
name|i
index|]
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
elseif|else
if|if
condition|(
name|args
index|[
name|i
index|]
operator|.
name|equals
argument_list|(
literal|"-count"
argument_list|)
condition|)
block|{
name|count
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// file is required parameter
name|file
operator|=
operator|new
name|Path
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|file
operator|==
literal|null
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|usage
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"count = "
operator|+
name|count
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"create = "
operator|+
name|create
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"file = "
operator|+
name|file
argument_list|)
expr_stmt|;
name|TestRCFile
name|test
init|=
operator|new
name|TestRCFile
argument_list|()
decl_stmt|;
comment|// test.performanceTest();
name|test
operator|.
name|testSimpleReadAndWrite
argument_list|()
expr_stmt|;
name|test
operator|.
name|writeTest
argument_list|(
name|fs
argument_list|,
name|count
argument_list|,
name|file
argument_list|,
name|bytesArray
argument_list|)
expr_stmt|;
name|test
operator|.
name|fullyReadTest
argument_list|(
name|fs
argument_list|,
name|count
argument_list|,
name|file
argument_list|)
expr_stmt|;
name|test
operator|.
name|partialReadTest
argument_list|(
name|fs
argument_list|,
name|count
argument_list|,
name|file
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Finished."
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|fs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|writeTest
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|int
name|count
parameter_list|,
name|Path
name|file
parameter_list|,
name|byte
index|[]
index|[]
name|fieldsData
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
name|fieldsData
operator|.
name|length
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
operator|new
name|DefaultCodec
argument_list|()
argument_list|)
decl_stmt|;
name|BytesRefArrayWritable
name|bytes
init|=
operator|new
name|BytesRefArrayWritable
argument_list|(
name|fieldsData
operator|.
name|length
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
name|fieldsData
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|BytesRefWritable
name|cu
init|=
literal|null
decl_stmt|;
name|cu
operator|=
operator|new
name|BytesRefWritable
argument_list|(
name|fieldsData
index|[
name|i
index|]
argument_list|,
literal|0
argument_list|,
name|fieldsData
index|[
name|i
index|]
operator|.
name|length
argument_list|)
expr_stmt|;
name|bytes
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|cu
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
name|count
condition|;
name|i
operator|++
control|)
block|{
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
name|long
name|fileLen
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|file
argument_list|)
operator|.
name|getLen
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"The file size of RCFile with "
operator|+
name|bytes
operator|.
name|size
argument_list|()
operator|+
literal|" number columns and "
operator|+
name|count
operator|+
literal|" number rows is "
operator|+
name|fileLen
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
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
name|Constants
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
name|Constants
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
name|void
name|fullyReadTest
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|int
name|count
parameter_list|,
name|Path
name|file
parameter_list|)
throws|throws
name|IOException
throws|,
name|SerDeException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"reading "
operator|+
name|count
operator|+
literal|" records"
argument_list|)
expr_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|ColumnProjectionUtils
operator|.
name|setFullyReadColumns
argument_list|(
name|conf
argument_list|)
expr_stmt|;
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
name|file
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|LongWritable
name|rowID
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
name|int
name|actualRead
init|=
literal|0
decl_stmt|;
name|BytesRefArrayWritable
name|cols
init|=
operator|new
name|BytesRefArrayWritable
argument_list|()
decl_stmt|;
while|while
condition|(
name|reader
operator|.
name|next
argument_list|(
name|rowID
argument_list|)
condition|)
block|{
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
literal|8
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
name|assertEquals
argument_list|(
literal|"Field size should be 8"
argument_list|,
literal|8
argument_list|,
name|fieldRefs
operator|.
name|size
argument_list|()
argument_list|)
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
name|fieldRefs
operator|.
name|size
argument_list|()
condition|;
name|i
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
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|Object
name|standardWritableData
init|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|fieldData
argument_list|,
name|fieldRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Field "
operator|+
name|i
argument_list|,
name|standardWritableData
argument_list|,
name|expectedFieldsData
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
comment|// Serialize
name|assertEquals
argument_list|(
literal|"Class of the serialized object should be BytesRefArrayWritable"
argument_list|,
name|BytesRefArrayWritable
operator|.
name|class
argument_list|,
name|serDe
operator|.
name|getSerializedClass
argument_list|()
argument_list|)
expr_stmt|;
name|BytesRefArrayWritable
name|serializedText
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
name|assertEquals
argument_list|(
literal|"Serialized data"
argument_list|,
name|s
argument_list|,
name|serializedText
argument_list|)
expr_stmt|;
name|actualRead
operator|++
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Expect "
operator|+
name|count
operator|+
literal|" rows, actual read "
operator|+
name|actualRead
argument_list|,
name|actualRead
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|long
name|cost
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"reading fully costs:"
operator|+
name|cost
operator|+
literal|" milliseconds"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|partialReadTest
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|int
name|count
parameter_list|,
name|Path
name|file
parameter_list|)
throws|throws
name|IOException
throws|,
name|SerDeException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"reading "
operator|+
name|count
operator|+
literal|" records"
argument_list|)
expr_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|readCols
init|=
operator|new
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|readCols
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|readCols
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|ColumnProjectionUtils
operator|.
name|setReadColumnIDs
argument_list|(
name|conf
argument_list|,
name|readCols
argument_list|)
expr_stmt|;
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
name|file
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|LongWritable
name|rowID
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
name|BytesRefArrayWritable
name|cols
init|=
operator|new
name|BytesRefArrayWritable
argument_list|()
decl_stmt|;
while|while
condition|(
name|reader
operator|.
name|next
argument_list|(
name|rowID
argument_list|)
condition|)
block|{
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
literal|8
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
name|assertEquals
argument_list|(
literal|"Field size should be 8"
argument_list|,
literal|8
argument_list|,
name|fieldRefs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
range|:
name|readCols
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
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|Object
name|standardWritableData
init|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|fieldData
argument_list|,
name|fieldRefs
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Field "
operator|+
name|i
argument_list|,
name|standardWritableData
argument_list|,
name|expectedPartitalFieldsData
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Class of the serialized object should be BytesRefArrayWritable"
argument_list|,
name|BytesRefArrayWritable
operator|.
name|class
argument_list|,
name|serDe
operator|.
name|getSerializedClass
argument_list|()
argument_list|)
expr_stmt|;
name|BytesRefArrayWritable
name|serializedBytes
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
name|assertEquals
argument_list|(
literal|"Serialized data"
argument_list|,
name|patialS
argument_list|,
name|serializedBytes
argument_list|)
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|long
name|cost
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"reading fully costs:"
operator|+
name|cost
operator|+
literal|" milliseconds"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

