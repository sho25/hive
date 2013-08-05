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
name|cli
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

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
name|io
operator|.
name|PrintStream
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
name|hbase
operator|.
name|util
operator|.
name|Bytes
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
name|Test
import|;
end_import

begin_comment
comment|/**  * test RCFileCat  *  */
end_comment

begin_class
specifier|public
class|class
name|TestRCFileCat
block|{
comment|/**    * test parse file    */
annotation|@
name|Test
specifier|public
name|void
name|testRCFileCat
parameter_list|()
throws|throws
name|Exception
block|{
name|File
name|template
init|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"hive"
argument_list|,
literal|"tmpTest"
argument_list|)
decl_stmt|;
name|Configuration
name|configuration
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|byte
index|[]
index|[]
name|record_1
init|=
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"123"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"456"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"789"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1000"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"5.3"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"hive and hadoop"
argument_list|)
block|,
operator|new
name|byte
index|[
literal|0
index|]
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"NULL"
argument_list|)
block|}
decl_stmt|;
name|byte
index|[]
index|[]
name|record_2
init|=
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"100"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"200"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"123"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1000"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"5.3"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"hive and hadoop"
argument_list|)
block|,
operator|new
name|byte
index|[
literal|0
index|]
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"NULL"
argument_list|)
block|}
decl_stmt|;
name|byte
index|[]
index|[]
name|record_3
init|=
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"200"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"400"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"678"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1000"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"4.8"
argument_list|)
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"hive and hadoop"
argument_list|)
block|,
operator|new
name|byte
index|[
literal|0
index|]
block|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TEST"
argument_list|)
block|}
decl_stmt|;
name|RCFileOutputFormat
operator|.
name|setColumnNumber
argument_list|(
name|configuration
argument_list|,
literal|8
argument_list|)
expr_stmt|;
name|Path
name|file
init|=
operator|new
name|Path
argument_list|(
name|template
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
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
name|configuration
argument_list|,
name|file
argument_list|,
literal|null
argument_list|,
name|RCFile
operator|.
name|createMetadata
argument_list|(
operator|new
name|Text
argument_list|(
literal|"apple"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"block"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"cat"
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"dog"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|DefaultCodec
argument_list|()
argument_list|)
decl_stmt|;
name|write
argument_list|(
name|writer
argument_list|,
name|record_1
argument_list|)
expr_stmt|;
name|write
argument_list|(
name|writer
argument_list|,
name|record_2
argument_list|)
expr_stmt|;
name|write
argument_list|(
name|writer
argument_list|,
name|record_3
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|RCFileCat
name|fileCat
init|=
operator|new
name|RCFileCat
argument_list|()
decl_stmt|;
name|RCFileCat
operator|.
name|test
operator|=
literal|true
expr_stmt|;
name|fileCat
operator|.
name|setConf
argument_list|(
operator|new
name|Configuration
argument_list|()
argument_list|)
expr_stmt|;
comment|// set fake input and output streams
name|PrintStream
name|oldOutPrintStream
init|=
name|System
operator|.
name|out
decl_stmt|;
name|PrintStream
name|oldErrPrintStream
init|=
name|System
operator|.
name|err
decl_stmt|;
name|ByteArrayOutputStream
name|dataOut
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|ByteArrayOutputStream
name|dataErr
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|System
operator|.
name|setOut
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|dataOut
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|setErr
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|dataErr
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|String
index|[]
name|params
init|=
block|{
literal|"--verbose"
block|,
literal|"file://"
operator|+
name|template
operator|.
name|getAbsolutePath
argument_list|()
block|}
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|fileCat
operator|.
name|run
argument_list|(
name|params
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"123\t456\t789\t1000\t5.3\thive and hadoop\t\tNULL"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"100\t200\t123\t1000\t5.3\thive and hadoop\t\tNULL"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"200\t400\t678\t1000\t4.8\thive and hadoop\t\tTEST"
argument_list|)
argument_list|)
expr_stmt|;
name|dataOut
operator|.
name|reset
argument_list|()
expr_stmt|;
name|params
operator|=
operator|new
name|String
index|[]
block|{
literal|"--start=-10"
block|,
literal|"--file-sizes"
block|,
literal|"file://"
operator|+
name|template
operator|.
name|getAbsolutePath
argument_list|()
block|}
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|fileCat
operator|.
name|run
argument_list|(
name|params
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"File size (uncompressed): 105. File size (compressed): 134. Number of rows: 3."
argument_list|)
argument_list|)
expr_stmt|;
name|dataOut
operator|.
name|reset
argument_list|()
expr_stmt|;
name|params
operator|=
operator|new
name|String
index|[]
block|{
literal|"--start=0"
block|,
literal|"--column-sizes"
block|,
literal|"file://"
operator|+
name|template
operator|.
name|getAbsolutePath
argument_list|()
block|}
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|fileCat
operator|.
name|run
argument_list|(
name|params
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"0\t9\t17"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"1\t9\t17"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"2\t9\t17"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"3\t12\t14"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"4\t9\t17"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"5\t45\t26"
argument_list|)
argument_list|)
expr_stmt|;
name|dataOut
operator|.
name|reset
argument_list|()
expr_stmt|;
name|params
operator|=
operator|new
name|String
index|[]
block|{
literal|"--start=0"
block|,
literal|"--column-sizes-pretty"
block|,
literal|"file://"
operator|+
name|template
operator|.
name|getAbsolutePath
argument_list|()
block|}
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|fileCat
operator|.
name|run
argument_list|(
name|params
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Column 0: Uncompressed size: 9 Compressed size: 17"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Column 1: Uncompressed size: 9 Compressed size: 17"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Column 2: Uncompressed size: 9 Compressed size: 17"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Column 3: Uncompressed size: 12 Compressed size: 14"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Column 4: Uncompressed size: 9 Compressed size: 17"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataOut
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Column 5: Uncompressed size: 45 Compressed size: 26"
argument_list|)
argument_list|)
expr_stmt|;
name|params
operator|=
operator|new
name|String
index|[]
block|{ }
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|fileCat
operator|.
name|run
argument_list|(
name|params
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataErr
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"RCFileCat [--start=start_offet] [--length=len] [--verbose] "
operator|+
literal|"[--column-sizes | --column-sizes-pretty] [--file-sizes] fileName"
argument_list|)
argument_list|)
expr_stmt|;
name|dataErr
operator|.
name|reset
argument_list|()
expr_stmt|;
name|params
operator|=
operator|new
name|String
index|[]
block|{
literal|"--fakeParameter"
block|,
literal|"file://"
operator|+
name|template
operator|.
name|getAbsolutePath
argument_list|()
block|}
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|fileCat
operator|.
name|run
argument_list|(
name|params
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dataErr
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"RCFileCat [--start=start_offet] [--length=len] [--verbose] "
operator|+
literal|"[--column-sizes | --column-sizes-pretty] [--file-sizes] fileName"
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// restore  input and output streams
name|System
operator|.
name|setOut
argument_list|(
name|oldOutPrintStream
argument_list|)
expr_stmt|;
name|System
operator|.
name|setErr
argument_list|(
name|oldErrPrintStream
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|write
parameter_list|(
name|RCFile
operator|.
name|Writer
name|writer
parameter_list|,
name|byte
index|[]
index|[]
name|record
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
block|}
block|}
end_class

end_unit

