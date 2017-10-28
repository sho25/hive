begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
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
name|conf
operator|.
name|HiveConf
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
name|shims
operator|.
name|ShimLoader
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
name|SequenceFile
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
name|CompressionCodec
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
name|Job
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
comment|/**  * TestRCFile.  *  */
end_comment

begin_class
specifier|public
class|class
name|TestRCFileMapReduceInputFormat
extends|extends
name|TestCase
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
name|TestRCFileMapReduceInputFormat
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
literal|"test.tmp.dir"
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
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{     }
block|}
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
name|LOG
operator|.
name|error
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
name|LOG
operator|.
name|error
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
literal|"count = {}"
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"create = {}"
argument_list|,
name|create
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"file = {}"
argument_list|,
name|file
argument_list|)
expr_stmt|;
comment|// test.performanceTest();
name|LOG
operator|.
name|info
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
specifier|public
name|void
name|testSynAndSplit
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|splitBeforeSync
argument_list|()
expr_stmt|;
name|splitRightBeforeSync
argument_list|()
expr_stmt|;
name|splitInMiddleOfSync
argument_list|()
expr_stmt|;
name|splitRightAfterSync
argument_list|()
expr_stmt|;
name|splitAfterSync
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|splitBeforeSync
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|writeThenReadByRecordReader
argument_list|(
literal|600
argument_list|,
literal|10000
argument_list|,
literal|2
argument_list|,
literal|176840
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|splitRightBeforeSync
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|writeThenReadByRecordReader
argument_list|(
literal|500
argument_list|,
literal|10000
argument_list|,
literal|2
argument_list|,
literal|177500
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|splitInMiddleOfSync
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|writeThenReadByRecordReader
argument_list|(
literal|500
argument_list|,
literal|10000
argument_list|,
literal|2
argument_list|,
literal|177600
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|splitRightAfterSync
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|writeThenReadByRecordReader
argument_list|(
literal|500
argument_list|,
literal|10000
argument_list|,
literal|2
argument_list|,
literal|177700
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|splitAfterSync
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|writeThenReadByRecordReader
argument_list|(
literal|500
argument_list|,
literal|10000
argument_list|,
literal|2
argument_list|,
literal|199500
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|writeThenReadByRecordReader
parameter_list|(
name|int
name|intervalRecordCount
parameter_list|,
name|int
name|writeCount
parameter_list|,
name|int
name|splitNumber
parameter_list|,
name|long
name|maxSplitSize
parameter_list|,
name|CompressionCodec
name|codec
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Path
name|testDir
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
literal|"."
argument_list|)
operator|+
literal|"/mapred/testsmallfirstsplit"
argument_list|)
decl_stmt|;
name|Path
name|testFile
init|=
operator|new
name|Path
argument_list|(
name|testDir
argument_list|,
literal|"test_rcfile"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|testFile
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|Configuration
name|cloneConf
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|RCFileOutputFormat
operator|.
name|setColumnNumber
argument_list|(
name|cloneConf
argument_list|,
name|bytesArray
operator|.
name|length
argument_list|)
expr_stmt|;
name|cloneConf
operator|.
name|setInt
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_RCFILE_RECORD_INTERVAL
operator|.
name|varname
argument_list|,
name|intervalRecordCount
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
name|cloneConf
argument_list|,
name|testFile
argument_list|,
literal|null
argument_list|,
name|codec
argument_list|)
decl_stmt|;
name|BytesRefArrayWritable
name|bytes
init|=
operator|new
name|BytesRefArrayWritable
argument_list|(
name|bytesArray
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
name|bytesArray
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
name|bytesArray
index|[
name|i
index|]
argument_list|,
literal|0
argument_list|,
name|bytesArray
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
name|writeCount
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
name|RCFileMapReduceInputFormat
argument_list|<
name|LongWritable
argument_list|,
name|BytesRefArrayWritable
argument_list|>
name|inputFormat
init|=
operator|new
name|RCFileMapReduceInputFormat
argument_list|<
name|LongWritable
argument_list|,
name|BytesRefArrayWritable
argument_list|>
argument_list|()
decl_stmt|;
name|Configuration
name|jonconf
init|=
operator|new
name|Configuration
argument_list|(
name|cloneConf
argument_list|)
decl_stmt|;
name|jonconf
operator|.
name|set
argument_list|(
literal|"mapred.input.dir"
argument_list|,
name|testDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|JobContext
name|context
init|=
operator|new
name|Job
argument_list|(
name|jonconf
argument_list|)
decl_stmt|;
name|HiveConf
operator|.
name|setLongVar
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAPREDMAXSPLITSIZE
argument_list|,
name|maxSplitSize
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|InputSplit
argument_list|>
name|splits
init|=
name|inputFormat
operator|.
name|getSplits
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"splits length should be "
operator|+
name|splitNumber
argument_list|,
name|splitNumber
argument_list|,
name|splits
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|readCount
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
name|splits
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|TaskAttemptContext
name|tac
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createTaskAttemptContext
argument_list|(
name|jonconf
argument_list|,
operator|new
name|TaskAttemptID
argument_list|()
argument_list|)
decl_stmt|;
name|RecordReader
argument_list|<
name|LongWritable
argument_list|,
name|BytesRefArrayWritable
argument_list|>
name|rr
init|=
name|inputFormat
operator|.
name|createRecordReader
argument_list|(
name|splits
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|tac
argument_list|)
decl_stmt|;
name|rr
operator|.
name|initialize
argument_list|(
name|splits
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|tac
argument_list|)
expr_stmt|;
while|while
condition|(
name|rr
operator|.
name|nextKeyValue
argument_list|()
condition|)
block|{
name|readCount
operator|++
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|"readCount should be equal to writeCount"
argument_list|,
name|readCount
argument_list|,
name|writeCount
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

