begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|spark
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
name|FileInputStream
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|tuple
operator|.
name|MutablePair
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
name|FileUtil
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
name|HiveKey
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
name|scala
operator|.
name|Tuple2
import|;
end_import

begin_import
import|import
name|com
operator|.
name|esotericsoftware
operator|.
name|kryo
operator|.
name|io
operator|.
name|Input
import|;
end_import

begin_import
import|import
name|com
operator|.
name|esotericsoftware
operator|.
name|kryo
operator|.
name|io
operator|.
name|Output
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * A cache with fixed buffer. If the buffer is full, new entries will  * be written to disk. This class is thread safe since multiple threads  * could access it (doesn't have to be concurrently), for example,  * the StreamThread in ScriptOperator.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
class|class
name|HiveKVResultCache
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
name|HiveKVResultCache
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|static
specifier|final
name|int
name|IN_MEMORY_NUM_ROWS
init|=
literal|1024
decl_stmt|;
specifier|private
name|MutablePair
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
index|[]
name|writeBuffer
decl_stmt|;
specifier|private
name|MutablePair
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
index|[]
name|readBuffer
decl_stmt|;
specifier|private
name|File
name|parentFile
decl_stmt|;
specifier|private
name|File
name|tmpFile
decl_stmt|;
specifier|private
name|int
name|readCursor
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|writeCursor
init|=
literal|0
decl_stmt|;
comment|// Indicate if the read buffer has data, for example,
comment|// when in reading, data on disk could be pull in
specifier|private
name|boolean
name|readBufferUsed
init|=
literal|false
decl_stmt|;
specifier|private
name|int
name|rowsInReadBuffer
init|=
literal|0
decl_stmt|;
specifier|private
name|Input
name|input
decl_stmt|;
specifier|private
name|Output
name|output
decl_stmt|;
specifier|public
name|HiveKVResultCache
parameter_list|()
block|{
name|writeBuffer
operator|=
operator|new
name|MutablePair
index|[
name|IN_MEMORY_NUM_ROWS
index|]
expr_stmt|;
name|readBuffer
operator|=
operator|new
name|MutablePair
index|[
name|IN_MEMORY_NUM_ROWS
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
name|IN_MEMORY_NUM_ROWS
condition|;
name|i
operator|++
control|)
block|{
name|writeBuffer
index|[
name|i
index|]
operator|=
operator|new
name|MutablePair
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|()
expr_stmt|;
name|readBuffer
index|[
name|i
index|]
operator|=
operator|new
name|MutablePair
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|switchBufferAndResetCursor
parameter_list|()
block|{
name|MutablePair
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
index|[]
name|tmp
init|=
name|readBuffer
decl_stmt|;
name|rowsInReadBuffer
operator|=
name|writeCursor
expr_stmt|;
name|readBuffer
operator|=
name|writeBuffer
expr_stmt|;
name|readBufferUsed
operator|=
literal|true
expr_stmt|;
name|readCursor
operator|=
literal|0
expr_stmt|;
name|writeBuffer
operator|=
name|tmp
expr_stmt|;
name|writeCursor
operator|=
literal|0
expr_stmt|;
block|}
specifier|private
name|void
name|setupOutput
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|parentFile
operator|==
literal|null
condition|)
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|parentFile
operator|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"hive-resultcache"
argument_list|,
literal|""
argument_list|)
expr_stmt|;
if|if
condition|(
name|parentFile
operator|.
name|delete
argument_list|()
operator|&&
name|parentFile
operator|.
name|mkdir
argument_list|()
condition|)
block|{
name|parentFile
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Retry creating tmp result-cache directory..."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|tmpFile
operator|==
literal|null
operator|||
name|input
operator|!=
literal|null
condition|)
block|{
name|tmpFile
operator|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"ResultCache"
argument_list|,
literal|".tmp"
argument_list|,
name|parentFile
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"ResultCache created temp file "
operator|+
name|tmpFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|tmpFile
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
block|}
name|FileOutputStream
name|fos
init|=
literal|null
decl_stmt|;
try|try
block|{
name|fos
operator|=
operator|new
name|FileOutputStream
argument_list|(
name|tmpFile
argument_list|)
expr_stmt|;
name|output
operator|=
operator|new
name|Output
argument_list|(
name|fos
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|output
operator|==
literal|null
operator|&&
name|fos
operator|!=
literal|null
condition|)
block|{
name|fos
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|BytesWritable
name|readValue
parameter_list|(
name|Input
name|input
parameter_list|)
block|{
return|return
operator|new
name|BytesWritable
argument_list|(
name|input
operator|.
name|readBytes
argument_list|(
name|input
operator|.
name|readInt
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|void
name|writeValue
parameter_list|(
name|Output
name|output
parameter_list|,
name|BytesWritable
name|bytesWritable
parameter_list|)
block|{
name|int
name|size
init|=
name|bytesWritable
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|output
operator|.
name|writeInt
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeBytes
argument_list|(
name|bytesWritable
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HiveKey
name|readHiveKey
parameter_list|(
name|Input
name|input
parameter_list|)
block|{
name|HiveKey
name|hiveKey
init|=
operator|new
name|HiveKey
argument_list|(
name|input
operator|.
name|readBytes
argument_list|(
name|input
operator|.
name|readInt
argument_list|()
argument_list|)
argument_list|,
name|input
operator|.
name|readInt
argument_list|()
argument_list|)
decl_stmt|;
name|hiveKey
operator|.
name|setDistKeyLength
argument_list|(
name|input
operator|.
name|readInt
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|hiveKey
return|;
block|}
specifier|private
name|void
name|writeHiveKey
parameter_list|(
name|Output
name|output
parameter_list|,
name|HiveKey
name|hiveKey
parameter_list|)
block|{
name|int
name|size
init|=
name|hiveKey
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|output
operator|.
name|writeInt
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeBytes
argument_list|(
name|hiveKey
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeInt
argument_list|(
name|hiveKey
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeInt
argument_list|(
name|hiveKey
operator|.
name|getDistKeyLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|add
parameter_list|(
name|HiveKey
name|key
parameter_list|,
name|BytesWritable
name|value
parameter_list|)
block|{
if|if
condition|(
name|writeCursor
operator|>=
name|IN_MEMORY_NUM_ROWS
condition|)
block|{
comment|// Write buffer is full
if|if
condition|(
operator|!
name|readBufferUsed
condition|)
block|{
comment|// Read buffer isn't used, switch buffer
name|switchBufferAndResetCursor
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// Need to spill from write buffer to disk
try|try
block|{
if|if
condition|(
name|output
operator|==
literal|null
condition|)
block|{
name|setupOutput
argument_list|()
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
name|IN_MEMORY_NUM_ROWS
condition|;
name|i
operator|++
control|)
block|{
name|MutablePair
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|pair
init|=
name|writeBuffer
index|[
name|i
index|]
decl_stmt|;
name|writeHiveKey
argument_list|(
name|output
argument_list|,
name|pair
operator|.
name|getLeft
argument_list|()
argument_list|)
expr_stmt|;
name|writeValue
argument_list|(
name|output
argument_list|,
name|pair
operator|.
name|getRight
argument_list|()
argument_list|)
expr_stmt|;
name|pair
operator|.
name|setLeft
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|pair
operator|.
name|setRight
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|writeCursor
operator|=
literal|0
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|clear
argument_list|()
expr_stmt|;
comment|// Clean up the cache
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to spill rows to disk"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
name|MutablePair
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|pair
init|=
name|writeBuffer
index|[
name|writeCursor
operator|++
index|]
decl_stmt|;
name|pair
operator|.
name|setLeft
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|pair
operator|.
name|setRight
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|clear
parameter_list|()
block|{
name|writeCursor
operator|=
name|readCursor
operator|=
name|rowsInReadBuffer
operator|=
literal|0
expr_stmt|;
name|readBufferUsed
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|parentFile
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|input
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|input
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ignored
parameter_list|)
block|{         }
name|input
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|output
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|output
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ignored
parameter_list|)
block|{         }
name|output
operator|=
literal|null
expr_stmt|;
block|}
try|try
block|{
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
name|parentFile
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ignored
parameter_list|)
block|{       }
name|parentFile
operator|=
literal|null
expr_stmt|;
name|tmpFile
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
specifier|synchronized
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|readBufferUsed
operator|||
name|writeCursor
operator|>
literal|0
return|;
block|}
specifier|public
specifier|synchronized
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|next
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|readBufferUsed
condition|)
block|{
try|try
block|{
if|if
condition|(
name|input
operator|==
literal|null
operator|&&
name|output
operator|!=
literal|null
condition|)
block|{
comment|// Close output stream if open
name|output
operator|.
name|close
argument_list|()
expr_stmt|;
name|output
operator|=
literal|null
expr_stmt|;
name|FileInputStream
name|fis
init|=
literal|null
decl_stmt|;
try|try
block|{
name|fis
operator|=
operator|new
name|FileInputStream
argument_list|(
name|tmpFile
argument_list|)
expr_stmt|;
name|input
operator|=
operator|new
name|Input
argument_list|(
name|fis
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|input
operator|==
literal|null
operator|&&
name|fis
operator|!=
literal|null
condition|)
block|{
name|fis
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|input
operator|!=
literal|null
condition|)
block|{
comment|// Load next batch from disk
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|IN_MEMORY_NUM_ROWS
condition|;
name|i
operator|++
control|)
block|{
name|MutablePair
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|pair
init|=
name|readBuffer
index|[
name|i
index|]
decl_stmt|;
name|pair
operator|.
name|setLeft
argument_list|(
name|readHiveKey
argument_list|(
name|input
argument_list|)
argument_list|)
expr_stmt|;
name|pair
operator|.
name|setRight
argument_list|(
name|readValue
argument_list|(
name|input
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|input
operator|.
name|eof
argument_list|()
condition|)
block|{
name|input
operator|.
name|close
argument_list|()
expr_stmt|;
name|input
operator|=
literal|null
expr_stmt|;
block|}
name|rowsInReadBuffer
operator|=
name|IN_MEMORY_NUM_ROWS
expr_stmt|;
name|readBufferUsed
operator|=
literal|true
expr_stmt|;
name|readCursor
operator|=
literal|0
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|writeCursor
operator|==
literal|1
condition|)
block|{
name|MutablePair
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|pair
init|=
name|writeBuffer
index|[
literal|0
index|]
decl_stmt|;
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|row
init|=
operator|new
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|(
name|pair
operator|.
name|getLeft
argument_list|()
argument_list|,
name|pair
operator|.
name|getRight
argument_list|()
argument_list|)
decl_stmt|;
name|pair
operator|.
name|setLeft
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|pair
operator|.
name|setRight
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|writeCursor
operator|=
literal|0
expr_stmt|;
return|return
name|row
return|;
block|}
else|else
block|{
comment|// No record on disk, more data in write buffer
name|switchBufferAndResetCursor
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|clear
argument_list|()
expr_stmt|;
comment|// Clean up the cache
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to load rows from disk"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
name|MutablePair
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|pair
init|=
name|readBuffer
index|[
name|readCursor
index|]
decl_stmt|;
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|row
init|=
operator|new
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|(
name|pair
operator|.
name|getLeft
argument_list|()
argument_list|,
name|pair
operator|.
name|getRight
argument_list|()
argument_list|)
decl_stmt|;
name|pair
operator|.
name|setLeft
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|pair
operator|.
name|setRight
argument_list|(
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
operator|++
name|readCursor
operator|>=
name|rowsInReadBuffer
condition|)
block|{
name|readBufferUsed
operator|=
literal|false
expr_stmt|;
name|rowsInReadBuffer
operator|=
literal|0
expr_stmt|;
name|readCursor
operator|=
literal|0
expr_stmt|;
block|}
return|return
name|row
return|;
block|}
block|}
end_class

end_unit

