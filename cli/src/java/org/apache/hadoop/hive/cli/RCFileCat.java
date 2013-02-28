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
import|import
name|java
operator|.
name|io
operator|.
name|BufferedOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileDescriptor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
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
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|Charset
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CharsetDecoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CodingErrorAction
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
operator|.
name|KeyBuffer
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
name|RCFileRecordReader
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
name|mapred
operator|.
name|FileSplit
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
name|mapred
operator|.
name|JobConf
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
name|util
operator|.
name|Tool
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
name|util
operator|.
name|ToolRunner
import|;
end_import

begin_class
specifier|public
class|class
name|RCFileCat
implements|implements
name|Tool
block|{
comment|// Size of string buffer in bytes
specifier|private
specifier|static
specifier|final
name|int
name|STRING_BUFFER_SIZE
init|=
literal|16
operator|*
literal|1024
decl_stmt|;
comment|// The size to flush the string buffer at
specifier|private
specifier|static
specifier|final
name|int
name|STRING_BUFFER_FLUSH_SIZE
init|=
literal|14
operator|*
literal|1024
decl_stmt|;
comment|// Size of stdout buffer in bytes
specifier|private
specifier|static
specifier|final
name|int
name|STDOUT_BUFFER_SIZE
init|=
literal|128
operator|*
literal|1024
decl_stmt|;
comment|// In verbose mode, print an update per RECORD_PRINT_INTERVAL records
specifier|private
specifier|static
specifier|final
name|int
name|RECORD_PRINT_INTERVAL
init|=
operator|(
literal|1024
operator|*
literal|1024
operator|)
decl_stmt|;
specifier|public
name|RCFileCat
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|decoder
operator|=
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
operator|.
name|newDecoder
argument_list|()
operator|.
name|onMalformedInput
argument_list|(
name|CodingErrorAction
operator|.
name|REPLACE
argument_list|)
operator|.
name|onUnmappableCharacter
argument_list|(
name|CodingErrorAction
operator|.
name|REPLACE
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|CharsetDecoder
name|decoder
decl_stmt|;
name|Configuration
name|conf
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|String
name|TAB
init|=
literal|"\t"
decl_stmt|;
specifier|private
specifier|static
name|String
name|NEWLINE
init|=
literal|"\r\n"
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|long
name|start
init|=
literal|0l
decl_stmt|;
name|long
name|length
init|=
operator|-
literal|1l
decl_stmt|;
name|int
name|recordCount
init|=
literal|0
decl_stmt|;
name|long
name|startT
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|boolean
name|verbose
init|=
literal|false
decl_stmt|;
name|boolean
name|columnSizes
init|=
literal|false
decl_stmt|;
name|boolean
name|pretty
init|=
literal|false
decl_stmt|;
name|boolean
name|fileSizes
init|=
literal|false
decl_stmt|;
comment|//get options from arguments
if|if
condition|(
name|args
operator|.
name|length
argument_list|<
literal|1
operator|||
name|args
operator|.
name|length
argument_list|>
literal|3
condition|)
block|{
name|printUsage
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|Path
name|fileName
init|=
literal|null
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
name|args
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|arg
init|=
name|args
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|arg
operator|.
name|startsWith
argument_list|(
literal|"--start="
argument_list|)
condition|)
block|{
name|start
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|arg
operator|.
name|substring
argument_list|(
literal|"--start="
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|arg
operator|.
name|startsWith
argument_list|(
literal|"--length="
argument_list|)
condition|)
block|{
name|length
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|arg
operator|.
name|substring
argument_list|(
literal|"--length="
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|arg
operator|.
name|equals
argument_list|(
literal|"--verbose"
argument_list|)
condition|)
block|{
name|verbose
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|arg
operator|.
name|equals
argument_list|(
literal|"--column-sizes"
argument_list|)
condition|)
block|{
name|columnSizes
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|arg
operator|.
name|equals
argument_list|(
literal|"--column-sizes-pretty"
argument_list|)
condition|)
block|{
name|columnSizes
operator|=
literal|true
expr_stmt|;
name|pretty
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|arg
operator|.
name|equals
argument_list|(
literal|"--file-sizes"
argument_list|)
condition|)
block|{
name|fileSizes
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fileName
operator|==
literal|null
condition|)
block|{
name|fileName
operator|=
operator|new
name|Path
argument_list|(
name|arg
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|printUsage
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
name|setupBufferedOutput
argument_list|()
expr_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|fileName
operator|.
name|toUri
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|long
name|fileLen
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|fileName
argument_list|)
operator|.
name|getLen
argument_list|()
decl_stmt|;
if|if
condition|(
name|start
operator|<
literal|0
condition|)
block|{
name|start
operator|=
literal|0
expr_stmt|;
block|}
if|if
condition|(
name|start
operator|>
name|fileLen
condition|)
block|{
return|return
literal|0
return|;
block|}
if|if
condition|(
name|length
argument_list|<
literal|0
operator|||
operator|(
name|start
operator|+
name|length
operator|)
argument_list|>
name|fileLen
condition|)
block|{
name|length
operator|=
name|fileLen
operator|-
name|start
expr_stmt|;
block|}
comment|//share the code with RecordReader.
name|FileSplit
name|split
init|=
operator|new
name|FileSplit
argument_list|(
name|fileName
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|RCFileRecordReader
name|recordReader
init|=
operator|new
name|RCFileRecordReader
argument_list|(
name|conf
argument_list|,
name|split
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnSizes
operator|||
name|fileSizes
condition|)
block|{
comment|// Print out the un/compressed sizes of each column
name|long
index|[]
name|compressedColumnSizes
init|=
literal|null
decl_stmt|;
name|long
index|[]
name|uncompressedColumnSizes
init|=
literal|null
decl_stmt|;
comment|// un/compressed sizes of file and no. of rows
name|long
name|rowNo
init|=
literal|0
decl_stmt|;
name|long
name|uncompressedFileSize
init|=
literal|0
decl_stmt|;
name|long
name|compressedFileSize
init|=
literal|0
decl_stmt|;
comment|// Skip from block to block since we only need the header
while|while
condition|(
name|recordReader
operator|.
name|nextBlock
argument_list|()
condition|)
block|{
comment|// Get the sizes from the key buffer and aggregate
name|KeyBuffer
name|keyBuffer
init|=
name|recordReader
operator|.
name|getKeyBuffer
argument_list|()
decl_stmt|;
if|if
condition|(
name|uncompressedColumnSizes
operator|==
literal|null
condition|)
block|{
name|uncompressedColumnSizes
operator|=
operator|new
name|long
index|[
name|keyBuffer
operator|.
name|getColumnNumber
argument_list|()
index|]
expr_stmt|;
block|}
if|if
condition|(
name|compressedColumnSizes
operator|==
literal|null
condition|)
block|{
name|compressedColumnSizes
operator|=
operator|new
name|long
index|[
name|keyBuffer
operator|.
name|getColumnNumber
argument_list|()
index|]
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
name|keyBuffer
operator|.
name|getColumnNumber
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|uncompressedColumnSizes
index|[
name|i
index|]
operator|+=
name|keyBuffer
operator|.
name|getEachColumnUncompressedValueLen
argument_list|()
index|[
name|i
index|]
expr_stmt|;
name|compressedColumnSizes
index|[
name|i
index|]
operator|+=
name|keyBuffer
operator|.
name|getEachColumnValueLen
argument_list|()
index|[
name|i
index|]
expr_stmt|;
block|}
name|rowNo
operator|+=
name|keyBuffer
operator|.
name|getNumberRows
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|columnSizes
operator|&&
name|uncompressedColumnSizes
operator|!=
literal|null
operator|&&
name|compressedColumnSizes
operator|!=
literal|null
condition|)
block|{
comment|// Print out the sizes, if pretty is set, print it out in a human friendly format,
comment|// otherwise print it out as if it were a row
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|uncompressedColumnSizes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|pretty
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Column "
operator|+
name|i
operator|+
literal|": Uncompressed size: "
operator|+
name|uncompressedColumnSizes
index|[
name|i
index|]
operator|+
literal|" Compressed size: "
operator|+
name|compressedColumnSizes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
name|i
operator|+
name|TAB
operator|+
name|uncompressedColumnSizes
index|[
name|i
index|]
operator|+
name|TAB
operator|+
name|compressedColumnSizes
index|[
name|i
index|]
operator|+
name|NEWLINE
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|fileSizes
condition|)
block|{
if|if
condition|(
name|uncompressedColumnSizes
operator|!=
literal|null
operator|&&
name|compressedColumnSizes
operator|!=
literal|null
condition|)
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
name|uncompressedColumnSizes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|uncompressedFileSize
operator|+=
name|uncompressedColumnSizes
index|[
name|i
index|]
expr_stmt|;
name|compressedFileSize
operator|+=
name|compressedColumnSizes
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"File size (uncompressed): "
operator|+
name|uncompressedFileSize
operator|+
literal|". File size (compressed): "
operator|+
name|compressedFileSize
operator|+
literal|". Number of rows: "
operator|+
name|rowNo
operator|+
literal|"."
operator|+
name|NEWLINE
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
return|return
literal|0
return|;
block|}
name|LongWritable
name|key
init|=
operator|new
name|LongWritable
argument_list|()
decl_stmt|;
name|BytesRefArrayWritable
name|value
init|=
operator|new
name|BytesRefArrayWritable
argument_list|()
decl_stmt|;
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|(
name|STRING_BUFFER_SIZE
argument_list|)
decl_stmt|;
comment|// extra capacity in case we overrun, to avoid resizing
while|while
condition|(
name|recordReader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
name|printRecord
argument_list|(
name|value
argument_list|,
name|buf
argument_list|)
expr_stmt|;
name|recordCount
operator|++
expr_stmt|;
if|if
condition|(
name|verbose
operator|&&
operator|(
name|recordCount
operator|%
name|RECORD_PRINT_INTERVAL
operator|)
operator|==
literal|0
condition|)
block|{
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Read "
operator|+
name|recordCount
operator|/
literal|1024
operator|+
literal|"k records"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Read "
operator|+
operator|(
operator|(
name|recordReader
operator|.
name|getPos
argument_list|()
operator|/
operator|(
literal|1024L
operator|*
literal|1024L
operator|)
operator|)
operator|)
operator|+
literal|"MB"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|printf
argument_list|(
literal|"Input scan rate %.2f MB/s\n"
argument_list|,
operator|(
name|recordReader
operator|.
name|getPos
argument_list|()
operator|*
literal|1.0
operator|/
operator|(
name|now
operator|-
name|startT
operator|)
operator|)
operator|/
literal|1024.0
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|buf
operator|.
name|length
argument_list|()
operator|>
name|STRING_BUFFER_FLUSH_SIZE
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
name|buf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|buf
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
comment|// print out last part of buffer
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
name|buf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
return|return
literal|0
return|;
block|}
comment|/**    * Print record to string builder    * @param value    * @param buf    * @throws IOException    */
specifier|private
name|void
name|printRecord
parameter_list|(
name|BytesRefArrayWritable
name|value
parameter_list|,
name|StringBuilder
name|buf
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|n
init|=
name|value
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|n
operator|>
literal|0
condition|)
block|{
name|BytesRefWritable
name|v
init|=
name|value
operator|.
name|unCheckedGet
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ByteBuffer
name|bb
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|v
operator|.
name|getData
argument_list|()
argument_list|,
name|v
operator|.
name|getStart
argument_list|()
argument_list|,
name|v
operator|.
name|getLength
argument_list|()
argument_list|)
decl_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|decoder
operator|.
name|decode
argument_list|(
name|bb
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
comment|// do not put the TAB for the last column
name|buf
operator|.
name|append
argument_list|(
name|RCFileCat
operator|.
name|TAB
argument_list|)
expr_stmt|;
name|v
operator|=
name|value
operator|.
name|unCheckedGet
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|bb
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|v
operator|.
name|getData
argument_list|()
argument_list|,
name|v
operator|.
name|getStart
argument_list|()
argument_list|,
name|v
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|buf
operator|.
name|append
argument_list|(
name|decoder
operator|.
name|decode
argument_list|(
name|bb
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|append
argument_list|(
name|RCFileCat
operator|.
name|NEWLINE
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
specifier|private
specifier|static
name|String
name|Usage
init|=
literal|"RCFileCat [--start=start_offet] [--length=len] [--verbose] "
operator|+
literal|"[--column-sizes | --column-sizes-pretty] [--file-sizes] fileName"
decl_stmt|;
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
try|try
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|RCFileCat
name|instance
init|=
operator|new
name|RCFileCat
argument_list|()
decl_stmt|;
name|instance
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|ToolRunner
operator|.
name|run
argument_list|(
name|instance
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"\n\n\n"
argument_list|)
expr_stmt|;
name|printUsage
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|setupBufferedOutput
parameter_list|()
block|{
name|FileOutputStream
name|fdout
init|=
operator|new
name|FileOutputStream
argument_list|(
name|FileDescriptor
operator|.
name|out
argument_list|)
decl_stmt|;
name|BufferedOutputStream
name|bos
init|=
operator|new
name|BufferedOutputStream
argument_list|(
name|fdout
argument_list|,
name|STDOUT_BUFFER_SIZE
argument_list|)
decl_stmt|;
name|PrintStream
name|ps
init|=
operator|new
name|PrintStream
argument_list|(
name|bos
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|System
operator|.
name|setOut
argument_list|(
name|ps
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|printUsage
parameter_list|(
name|String
name|errorMsg
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|Usage
argument_list|)
expr_stmt|;
if|if
condition|(
name|errorMsg
operator|!=
literal|null
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|errorMsg
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

