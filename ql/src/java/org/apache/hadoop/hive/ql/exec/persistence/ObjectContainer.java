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
name|persistence
package|;
end_package

begin_import
import|import
name|com
operator|.
name|esotericsoftware
operator|.
name|kryo
operator|.
name|Kryo
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
name|exec
operator|.
name|Utilities
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

begin_comment
comment|/**  * An eager object container that puts every row directly to output stream.  * The object can be of any type.  * Kryo is used for the serialization/deserialization.  * When reading, we load IN_MEMORY_NUM_ROWS rows from input stream to memory batch by batch.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
class|class
name|ObjectContainer
parameter_list|<
name|ROW
parameter_list|>
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
name|ObjectContainer
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
name|ROW
index|[]
name|readBuffer
decl_stmt|;
specifier|private
name|boolean
name|readBufferUsed
init|=
literal|false
decl_stmt|;
comment|// indicates if read buffer has data
specifier|private
name|int
name|rowsInReadBuffer
init|=
literal|0
decl_stmt|;
comment|// number of rows in the temporary read buffer
specifier|private
name|int
name|readCursor
init|=
literal|0
decl_stmt|;
comment|// cursor during reading
specifier|private
name|int
name|rowsOnDisk
init|=
literal|0
decl_stmt|;
comment|// total number of pairs in output
specifier|private
name|File
name|parentFile
decl_stmt|;
specifier|private
name|File
name|tmpFile
decl_stmt|;
specifier|private
name|Input
name|input
decl_stmt|;
specifier|private
name|Output
name|output
decl_stmt|;
specifier|private
name|Kryo
name|kryo
decl_stmt|;
specifier|public
name|ObjectContainer
parameter_list|()
block|{
name|readBuffer
operator|=
operator|(
name|ROW
index|[]
operator|)
operator|new
name|Object
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
name|readBuffer
index|[
name|i
index|]
operator|=
operator|(
name|ROW
operator|)
operator|new
name|Object
argument_list|()
expr_stmt|;
block|}
name|kryo
operator|=
name|Utilities
operator|.
name|runtimeSerializationKryo
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
block|{
name|setupOutput
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to create temporary output file on disk"
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
name|parentFile
operator|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"object-container"
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
literal|"ObjectContainer"
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
literal|"ObjectContainer created temp file "
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
specifier|public
name|void
name|add
parameter_list|(
name|ROW
name|row
parameter_list|)
block|{
name|kryo
operator|.
name|writeClassAndObject
argument_list|(
name|output
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|rowsOnDisk
operator|++
expr_stmt|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|readCursor
operator|=
name|rowsInReadBuffer
operator|=
name|rowsOnDisk
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
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|readBufferUsed
operator|||
name|rowsOnDisk
operator|>
literal|0
return|;
block|}
specifier|public
name|ROW
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
if|if
condition|(
name|rowsOnDisk
operator|>=
name|IN_MEMORY_NUM_ROWS
condition|)
block|{
name|rowsInReadBuffer
operator|=
name|IN_MEMORY_NUM_ROWS
expr_stmt|;
block|}
else|else
block|{
name|rowsInReadBuffer
operator|=
name|rowsOnDisk
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
name|rowsInReadBuffer
condition|;
name|i
operator|++
control|)
block|{
name|readBuffer
index|[
name|i
index|]
operator|=
operator|(
name|ROW
operator|)
name|kryo
operator|.
name|readClassAndObject
argument_list|(
name|input
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
name|readBufferUsed
operator|=
literal|true
expr_stmt|;
name|readCursor
operator|=
literal|0
expr_stmt|;
name|rowsOnDisk
operator|-=
name|rowsInReadBuffer
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
name|ROW
name|row
init|=
name|readBuffer
index|[
name|readCursor
index|]
decl_stmt|;
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
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|rowsInReadBuffer
operator|+
name|rowsOnDisk
return|;
block|}
block|}
end_class

end_unit

