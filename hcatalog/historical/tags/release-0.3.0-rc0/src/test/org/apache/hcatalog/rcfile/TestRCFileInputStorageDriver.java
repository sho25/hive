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
name|hcatalog
operator|.
name|rcfile
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
name|*
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|InputFormat
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
name|mapreduce
operator|.
name|InputSplit
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
name|mapreduce
operator|.
name|JobContext
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
name|mapreduce
operator|.
name|JobID
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
name|mapreduce
operator|.
name|RecordReader
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
name|mapreduce
operator|.
name|TaskAttemptContext
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
name|mapreduce
operator|.
name|TaskAttemptID
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|DefaultHCatRecord
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|HCatRecord
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
operator|.
name|HCatSchema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|rcfile
operator|.
name|RCFileInputDriver
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|shims
operator|.
name|HCatHadoopShims
import|;
end_import

begin_class
specifier|public
class|class
name|TestRCFileInputStorageDriver
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
specifier|final
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
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
specifier|private
specifier|static
specifier|final
name|Path
name|file
init|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
literal|"test_rcfile"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HCatHadoopShims
name|shim
init|=
name|HCatHadoopShims
operator|.
name|Instance
operator|.
name|get
argument_list|()
decl_stmt|;
comment|// Generate sample records to compare against
specifier|private
name|byte
index|[]
index|[]
index|[]
name|getRecords
parameter_list|()
throws|throws
name|UnsupportedEncodingException
block|{
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
literal|"hcatalog and hadoop"
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
literal|"\\N"
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
literal|"hcatalog and hadoop"
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
literal|"\\N"
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
block|}
decl_stmt|;
return|return
operator|new
name|byte
index|[]
index|[]
index|[]
block|{
name|record_1
block|,
name|record_2
block|}
return|;
block|}
comment|// Write sample records to file for individual tests
specifier|private
name|BytesRefArrayWritable
index|[]
name|initTestEnvironment
parameter_list|()
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
decl_stmt|;
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
index|[]
name|records
init|=
name|getRecords
argument_list|()
decl_stmt|;
name|RCFileOutputFormat
operator|.
name|setColumnNumber
argument_list|(
name|conf
argument_list|,
literal|8
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
name|writeBytesToFile
argument_list|(
name|records
index|[
literal|0
index|]
argument_list|,
name|writer
argument_list|)
decl_stmt|;
name|BytesRefArrayWritable
name|bytes2
init|=
name|writeBytesToFile
argument_list|(
name|records
index|[
literal|1
index|]
argument_list|,
name|writer
argument_list|)
decl_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
operator|new
name|BytesRefArrayWritable
index|[]
block|{
name|bytes
block|,
name|bytes2
block|}
return|;
block|}
specifier|private
name|BytesRefArrayWritable
name|writeBytesToFile
parameter_list|(
name|byte
index|[]
index|[]
name|record
parameter_list|,
name|RCFile
operator|.
name|Writer
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
name|BytesRefArrayWritable
name|bytes
init|=
operator|new
name|BytesRefArrayWritable
argument_list|(
name|record
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
name|record
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
name|record
index|[
name|i
index|]
argument_list|,
literal|0
argument_list|,
name|record
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
return|return
name|bytes
return|;
block|}
specifier|public
name|void
name|testConvertValueToTuple
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|BytesRefArrayWritable
index|[]
name|bytesArr
init|=
name|initTestEnvironment
argument_list|()
decl_stmt|;
name|HCatSchema
name|schema
init|=
name|buildHiveSchema
argument_list|()
decl_stmt|;
name|RCFileInputDriver
name|sd
init|=
operator|new
name|RCFileInputDriver
argument_list|()
decl_stmt|;
name|JobContext
name|jc
init|=
name|shim
operator|.
name|createJobContext
argument_list|(
name|conf
argument_list|,
operator|new
name|JobID
argument_list|()
argument_list|)
decl_stmt|;
name|sd
operator|.
name|setInputPath
argument_list|(
name|jc
argument_list|,
name|file
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|InputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|iF
init|=
name|sd
operator|.
name|getInputFormat
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|InputSplit
name|split
init|=
name|iF
operator|.
name|getSplits
argument_list|(
name|jc
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|sd
operator|.
name|setOriginalSchema
argument_list|(
name|jc
argument_list|,
name|schema
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setOutputSchema
argument_list|(
name|jc
argument_list|,
name|schema
argument_list|)
expr_stmt|;
name|sd
operator|.
name|initialize
argument_list|(
name|jc
argument_list|,
name|getProps
argument_list|()
argument_list|)
expr_stmt|;
name|TaskAttemptContext
name|tac
init|=
name|shim
operator|.
name|createTaskAttemptContext
argument_list|(
name|conf
argument_list|,
operator|new
name|TaskAttemptID
argument_list|()
argument_list|)
decl_stmt|;
name|RecordReader
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|rr
init|=
name|iF
operator|.
name|createRecordReader
argument_list|(
name|split
argument_list|,
name|tac
argument_list|)
decl_stmt|;
name|rr
operator|.
name|initialize
argument_list|(
name|split
argument_list|,
name|tac
argument_list|)
expr_stmt|;
name|HCatRecord
index|[]
name|tuples
init|=
name|getExpectedRecords
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
literal|2
condition|;
name|j
operator|++
control|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|rr
operator|.
name|nextKeyValue
argument_list|()
argument_list|)
expr_stmt|;
name|BytesRefArrayWritable
name|w
init|=
operator|(
name|BytesRefArrayWritable
operator|)
name|rr
operator|.
name|getCurrentValue
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|bytesArr
index|[
name|j
index|]
argument_list|,
name|w
argument_list|)
expr_stmt|;
name|HCatRecord
name|t
init|=
name|sd
operator|.
name|convertToHCatRecord
argument_list|(
literal|null
argument_list|,
name|w
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|8
argument_list|,
name|t
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|t
argument_list|,
name|tuples
index|[
name|j
index|]
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testPruning
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|BytesRefArrayWritable
index|[]
name|bytesArr
init|=
name|initTestEnvironment
argument_list|()
decl_stmt|;
name|RCFileInputDriver
name|sd
init|=
operator|new
name|RCFileInputDriver
argument_list|()
decl_stmt|;
name|JobContext
name|jc
init|=
name|shim
operator|.
name|createJobContext
argument_list|(
name|conf
argument_list|,
operator|new
name|JobID
argument_list|()
argument_list|)
decl_stmt|;
name|sd
operator|.
name|setInputPath
argument_list|(
name|jc
argument_list|,
name|file
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|InputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|iF
init|=
name|sd
operator|.
name|getInputFormat
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|InputSplit
name|split
init|=
name|iF
operator|.
name|getSplits
argument_list|(
name|jc
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|sd
operator|.
name|setOriginalSchema
argument_list|(
name|jc
argument_list|,
name|buildHiveSchema
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setOutputSchema
argument_list|(
name|jc
argument_list|,
name|buildPrunedSchema
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|initialize
argument_list|(
name|jc
argument_list|,
name|getProps
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|ColumnProjectionUtils
operator|.
name|READ_COLUMN_IDS_CONF_STR
argument_list|,
name|jc
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|ColumnProjectionUtils
operator|.
name|READ_COLUMN_IDS_CONF_STR
argument_list|)
argument_list|)
expr_stmt|;
name|TaskAttemptContext
name|tac
init|=
name|shim
operator|.
name|createTaskAttemptContext
argument_list|(
name|conf
argument_list|,
operator|new
name|TaskAttemptID
argument_list|()
argument_list|)
decl_stmt|;
name|RecordReader
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|rr
init|=
name|iF
operator|.
name|createRecordReader
argument_list|(
name|split
argument_list|,
name|tac
argument_list|)
decl_stmt|;
name|rr
operator|.
name|initialize
argument_list|(
name|split
argument_list|,
name|tac
argument_list|)
expr_stmt|;
name|HCatRecord
index|[]
name|tuples
init|=
name|getPrunedRecords
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
literal|2
condition|;
name|j
operator|++
control|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|rr
operator|.
name|nextKeyValue
argument_list|()
argument_list|)
expr_stmt|;
name|BytesRefArrayWritable
name|w
init|=
operator|(
name|BytesRefArrayWritable
operator|)
name|rr
operator|.
name|getCurrentValue
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|bytesArr
index|[
name|j
index|]
operator|.
name|equals
argument_list|(
name|w
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|w
operator|.
name|size
argument_list|()
argument_list|,
literal|8
argument_list|)
expr_stmt|;
name|HCatRecord
name|t
init|=
name|sd
operator|.
name|convertToHCatRecord
argument_list|(
literal|null
argument_list|,
name|w
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|t
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|t
argument_list|,
name|tuples
index|[
name|j
index|]
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|rr
operator|.
name|nextKeyValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testReorderdCols
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|BytesRefArrayWritable
index|[]
name|bytesArr
init|=
name|initTestEnvironment
argument_list|()
decl_stmt|;
name|RCFileInputDriver
name|sd
init|=
operator|new
name|RCFileInputDriver
argument_list|()
decl_stmt|;
name|JobContext
name|jc
init|=
name|shim
operator|.
name|createJobContext
argument_list|(
name|conf
argument_list|,
operator|new
name|JobID
argument_list|()
argument_list|)
decl_stmt|;
name|sd
operator|.
name|setInputPath
argument_list|(
name|jc
argument_list|,
name|file
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|InputFormat
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|iF
init|=
name|sd
operator|.
name|getInputFormat
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|InputSplit
name|split
init|=
name|iF
operator|.
name|getSplits
argument_list|(
name|jc
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|sd
operator|.
name|setOriginalSchema
argument_list|(
name|jc
argument_list|,
name|buildHiveSchema
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setOutputSchema
argument_list|(
name|jc
argument_list|,
name|buildReorderedSchema
argument_list|()
argument_list|)
expr_stmt|;
name|sd
operator|.
name|initialize
argument_list|(
name|jc
argument_list|,
name|getProps
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"part1"
argument_list|,
literal|"first-part"
argument_list|)
expr_stmt|;
name|sd
operator|.
name|setPartitionValues
argument_list|(
name|jc
argument_list|,
name|map
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|ColumnProjectionUtils
operator|.
name|READ_COLUMN_IDS_CONF_STR
argument_list|,
name|jc
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|ColumnProjectionUtils
operator|.
name|READ_COLUMN_IDS_CONF_STR
argument_list|)
argument_list|)
expr_stmt|;
name|TaskAttemptContext
name|tac
init|=
name|shim
operator|.
name|createTaskAttemptContext
argument_list|(
name|conf
argument_list|,
operator|new
name|TaskAttemptID
argument_list|()
argument_list|)
decl_stmt|;
name|RecordReader
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|rr
init|=
name|iF
operator|.
name|createRecordReader
argument_list|(
name|split
argument_list|,
name|tac
argument_list|)
decl_stmt|;
name|rr
operator|.
name|initialize
argument_list|(
name|split
argument_list|,
name|tac
argument_list|)
expr_stmt|;
name|HCatRecord
index|[]
name|tuples
init|=
name|getReorderedCols
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
literal|2
condition|;
name|j
operator|++
control|)
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
name|rr
operator|.
name|nextKeyValue
argument_list|()
argument_list|)
expr_stmt|;
name|BytesRefArrayWritable
name|w
init|=
operator|(
name|BytesRefArrayWritable
operator|)
name|rr
operator|.
name|getCurrentValue
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|bytesArr
index|[
name|j
index|]
operator|.
name|equals
argument_list|(
name|w
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|w
operator|.
name|size
argument_list|()
argument_list|,
literal|8
argument_list|)
expr_stmt|;
name|HCatRecord
name|t
init|=
name|sd
operator|.
name|convertToHCatRecord
argument_list|(
literal|null
argument_list|,
name|w
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|7
argument_list|,
name|t
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|t
argument_list|,
name|tuples
index|[
name|j
index|]
argument_list|)
expr_stmt|;
block|}
name|assertFalse
argument_list|(
name|rr
operator|.
name|nextKeyValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HCatRecord
index|[]
name|getExpectedRecords
parameter_list|()
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|rec_1
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|8
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|rec_1
argument_list|,
operator|new
name|Byte
argument_list|(
literal|"123"
argument_list|)
argument_list|,
operator|new
name|Short
argument_list|(
literal|"456"
argument_list|)
argument_list|,
operator|new
name|Integer
argument_list|(
literal|789
argument_list|)
argument_list|,
operator|new
name|Long
argument_list|(
literal|1000L
argument_list|)
argument_list|,
operator|new
name|Double
argument_list|(
literal|5.3D
argument_list|)
argument_list|,
operator|new
name|String
argument_list|(
literal|"hcatalog and hadoop"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|HCatRecord
name|tup_1
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
name|rec_1
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|rec_2
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|8
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|rec_2
argument_list|,
operator|new
name|Byte
argument_list|(
literal|"100"
argument_list|)
argument_list|,
operator|new
name|Short
argument_list|(
literal|"200"
argument_list|)
argument_list|,
operator|new
name|Integer
argument_list|(
literal|123
argument_list|)
argument_list|,
operator|new
name|Long
argument_list|(
literal|1000L
argument_list|)
argument_list|,
operator|new
name|Double
argument_list|(
literal|5.3D
argument_list|)
argument_list|,
operator|new
name|String
argument_list|(
literal|"hcatalog and hadoop"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|HCatRecord
name|tup_2
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
name|rec_2
argument_list|)
decl_stmt|;
return|return
operator|new
name|HCatRecord
index|[]
block|{
name|tup_1
block|,
name|tup_2
block|}
return|;
block|}
specifier|private
name|HCatRecord
index|[]
name|getPrunedRecords
parameter_list|()
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|rec_1
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|8
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|rec_1
argument_list|,
operator|new
name|Byte
argument_list|(
literal|"123"
argument_list|)
argument_list|,
operator|new
name|Integer
argument_list|(
literal|789
argument_list|)
argument_list|,
operator|new
name|Double
argument_list|(
literal|5.3D
argument_list|)
argument_list|,
operator|new
name|String
argument_list|(
literal|"hcatalog and hadoop"
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|HCatRecord
name|tup_1
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
name|rec_1
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|rec_2
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|8
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|rec_2
argument_list|,
operator|new
name|Byte
argument_list|(
literal|"100"
argument_list|)
argument_list|,
operator|new
name|Integer
argument_list|(
literal|123
argument_list|)
argument_list|,
operator|new
name|Double
argument_list|(
literal|5.3D
argument_list|)
argument_list|,
operator|new
name|String
argument_list|(
literal|"hcatalog and hadoop"
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|HCatRecord
name|tup_2
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
name|rec_2
argument_list|)
decl_stmt|;
return|return
operator|new
name|HCatRecord
index|[]
block|{
name|tup_1
block|,
name|tup_2
block|}
return|;
block|}
specifier|private
name|HCatSchema
name|buildHiveSchema
parameter_list|()
throws|throws
name|HCatException
block|{
return|return
operator|new
name|HCatSchema
argument_list|(
name|HCatUtil
operator|.
name|getHCatFieldSchemaList
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"atinyint"
argument_list|,
literal|"tinyint"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"asmallint"
argument_list|,
literal|"smallint"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"aint"
argument_list|,
literal|"int"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"along"
argument_list|,
literal|"bigint"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"adouble"
argument_list|,
literal|"double"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"astring"
argument_list|,
literal|"string"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"anullint"
argument_list|,
literal|"int"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"anullstring"
argument_list|,
literal|"string"
argument_list|,
literal|""
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|HCatSchema
name|buildPrunedSchema
parameter_list|()
throws|throws
name|HCatException
block|{
return|return
operator|new
name|HCatSchema
argument_list|(
name|HCatUtil
operator|.
name|getHCatFieldSchemaList
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"atinyint"
argument_list|,
literal|"tinyint"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"aint"
argument_list|,
literal|"int"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"adouble"
argument_list|,
literal|"double"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"astring"
argument_list|,
literal|"string"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"anullint"
argument_list|,
literal|"int"
argument_list|,
literal|""
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|HCatSchema
name|buildReorderedSchema
parameter_list|()
throws|throws
name|HCatException
block|{
return|return
operator|new
name|HCatSchema
argument_list|(
name|HCatUtil
operator|.
name|getHCatFieldSchemaList
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"aint"
argument_list|,
literal|"int"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"part1"
argument_list|,
literal|"string"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"adouble"
argument_list|,
literal|"double"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"newCol"
argument_list|,
literal|"tinyint"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"astring"
argument_list|,
literal|"string"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"atinyint"
argument_list|,
literal|"tinyint"
argument_list|,
literal|""
argument_list|)
argument_list|,
operator|new
name|FieldSchema
argument_list|(
literal|"anullint"
argument_list|,
literal|"int"
argument_list|,
literal|""
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|HCatRecord
index|[]
name|getReorderedCols
parameter_list|()
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|rec_1
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|7
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|rec_1
argument_list|,
operator|new
name|Integer
argument_list|(
literal|789
argument_list|)
argument_list|,
operator|new
name|String
argument_list|(
literal|"first-part"
argument_list|)
argument_list|,
operator|new
name|Double
argument_list|(
literal|5.3D
argument_list|)
argument_list|,
literal|null
argument_list|,
comment|// new column
operator|new
name|String
argument_list|(
literal|"hcatalog and hadoop"
argument_list|)
argument_list|,
operator|new
name|Byte
argument_list|(
literal|"123"
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|HCatRecord
name|tup_1
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
name|rec_1
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|rec_2
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
literal|7
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|addAll
argument_list|(
name|rec_2
argument_list|,
operator|new
name|Integer
argument_list|(
literal|123
argument_list|)
argument_list|,
operator|new
name|String
argument_list|(
literal|"first-part"
argument_list|)
argument_list|,
operator|new
name|Double
argument_list|(
literal|5.3D
argument_list|)
argument_list|,
literal|null
argument_list|,
operator|new
name|String
argument_list|(
literal|"hcatalog and hadoop"
argument_list|)
argument_list|,
operator|new
name|Byte
argument_list|(
literal|"100"
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|HCatRecord
name|tup_2
init|=
operator|new
name|DefaultHCatRecord
argument_list|(
name|rec_2
argument_list|)
decl_stmt|;
return|return
operator|new
name|HCatRecord
index|[]
block|{
name|tup_1
block|,
name|tup_2
block|}
return|;
block|}
specifier|private
name|Properties
name|getProps
parameter_list|()
block|{
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|props
operator|.
name|setProperty
argument_list|(
name|Constants
operator|.
name|SERIALIZATION_NULL_FORMAT
argument_list|,
literal|"\\N"
argument_list|)
expr_stmt|;
name|props
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
return|return
name|props
return|;
block|}
block|}
end_class

end_unit

