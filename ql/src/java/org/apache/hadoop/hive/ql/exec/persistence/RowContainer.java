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
name|RandomAccessFile
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
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
name|ObjectInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectOutputStream
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|hive
operator|.
name|serde2
operator|.
name|SerDe
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
name|Writable
import|;
end_import

begin_comment
comment|/**  * Simple persistent container for rows.  *  * This container interface only accepts adding or appending new rows and  * iterating through the rows in the order of their insertions.  *  * The iterator interface is a lightweight first()/next() API rather than  * the Java Iterator interface. This way we do not need to create   * an Iterator object every time we want to start a new iteration. Below is   * simple example of how to convert a typical Java's Iterator code to the LW  * iterator iterface.  *   * Itereator itr = rowContainer.iterator();  * while (itr.hasNext()) {  *   v = itr.next();  *   // do anything with v  * }  *   * can be rewritten to:  *   * for ( v =  rowContainer.first();   *       v != null;   *       v =  rowContainer.next()) {  *   // do anything with v  * }  *  * The adding and iterating operations can be interleaving.   *  */
end_comment

begin_class
specifier|public
class|class
name|RowContainer
parameter_list|<
name|Row
extends|extends
name|List
parameter_list|>
block|{
specifier|protected
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// max # of rows can be put into one block
specifier|private
specifier|static
specifier|final
name|int
name|BLOCKSIZE
init|=
literal|25000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|BLKMETA_LEN
init|=
literal|100
decl_stmt|;
comment|// default # of block metadata: (offset,length) pair
specifier|private
name|Row
index|[]
name|lastBlock
decl_stmt|;
comment|// the last block that add() should append to
specifier|private
name|Row
index|[]
name|currBlock
decl_stmt|;
comment|// the current block where the cursor is in
specifier|private
name|int
name|blockSize
decl_stmt|;
comment|// number of objects in the block before it is spilled to disk
specifier|private
name|int
name|numBlocks
decl_stmt|;
comment|// total # of blocks
specifier|private
name|int
name|size
decl_stmt|;
comment|// total # of elements in the RowContainer
specifier|private
name|File
name|tmpFile
decl_stmt|;
comment|// temporary file holding the spilled blocks
specifier|private
name|RandomAccessFile
name|rFile
decl_stmt|;
comment|// random access file holding the data
specifier|private
name|long
index|[]
name|off_len
decl_stmt|;
comment|// offset length pair: i-th position is offset, (i+1)-th position is length
specifier|private
name|int
name|itrCursor
decl_stmt|;
comment|// iterator cursor in the currBlock
specifier|private
name|int
name|addCursor
decl_stmt|;
comment|// append cursor in the lastBlock
specifier|private
name|int
name|pBlock
decl_stmt|;
comment|// pointer to the iterator block
specifier|private
name|SerDe
name|serde
decl_stmt|;
comment|// serialization/deserialization for the row
specifier|private
name|ObjectInspector
name|standardOI
decl_stmt|;
comment|// object inspector for the row
specifier|public
name|RowContainer
parameter_list|()
block|{
name|this
argument_list|(
name|BLOCKSIZE
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RowContainer
parameter_list|(
name|int
name|blockSize
parameter_list|)
block|{
comment|// no 0-sized block
name|this
operator|.
name|blockSize
operator|=
name|blockSize
operator|==
literal|0
condition|?
name|BLOCKSIZE
else|:
name|blockSize
expr_stmt|;
name|this
operator|.
name|size
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|itrCursor
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|addCursor
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|numBlocks
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|pBlock
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|tmpFile
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|lastBlock
operator|=
operator|(
name|Row
index|[]
operator|)
operator|new
name|ArrayList
index|[
name|blockSize
index|]
expr_stmt|;
name|this
operator|.
name|currBlock
operator|=
name|this
operator|.
name|lastBlock
expr_stmt|;
name|this
operator|.
name|off_len
operator|=
operator|new
name|long
index|[
name|BLKMETA_LEN
operator|*
literal|2
index|]
expr_stmt|;
name|this
operator|.
name|serde
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|standardOI
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|RowContainer
parameter_list|(
name|int
name|blockSize
parameter_list|,
name|SerDe
name|sd
parameter_list|,
name|ObjectInspector
name|oi
parameter_list|)
block|{
name|this
argument_list|(
name|blockSize
argument_list|)
expr_stmt|;
name|setSerDe
argument_list|(
name|sd
argument_list|,
name|oi
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setSerDe
parameter_list|(
name|SerDe
name|sd
parameter_list|,
name|ObjectInspector
name|oi
parameter_list|)
block|{
assert|assert
name|serde
operator|!=
literal|null
operator|:
literal|"serde is null"
assert|;
assert|assert
name|oi
operator|!=
literal|null
operator|:
literal|"oi is null"
assert|;
name|this
operator|.
name|serde
operator|=
name|sd
expr_stmt|;
name|this
operator|.
name|standardOI
operator|=
name|oi
expr_stmt|;
block|}
specifier|public
name|void
name|add
parameter_list|(
name|Row
name|t
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|addCursor
operator|>=
name|blockSize
condition|)
block|{
comment|// spill the current block to tmp file
name|spillBlock
argument_list|(
name|lastBlock
argument_list|)
expr_stmt|;
name|addCursor
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|numBlocks
operator|==
literal|1
condition|)
name|lastBlock
operator|=
operator|(
name|Row
index|[]
operator|)
operator|new
name|ArrayList
index|[
name|blockSize
index|]
expr_stmt|;
block|}
name|lastBlock
index|[
name|addCursor
operator|++
index|]
operator|=
name|t
expr_stmt|;
operator|++
name|size
expr_stmt|;
block|}
specifier|public
name|Row
name|first
parameter_list|()
block|{
if|if
condition|(
name|size
operator|==
literal|0
condition|)
return|return
literal|null
return|;
if|if
condition|(
name|pBlock
operator|>
literal|0
condition|)
block|{
name|pBlock
operator|=
literal|0
expr_stmt|;
name|currBlock
operator|=
name|getBlock
argument_list|(
literal|0
argument_list|)
expr_stmt|;
assert|assert
name|currBlock
operator|!=
literal|null
operator|:
literal|"currBlock == null"
assert|;
block|}
if|if
condition|(
name|currBlock
operator|==
literal|null
operator|&&
name|lastBlock
operator|!=
literal|null
condition|)
block|{
name|currBlock
operator|=
name|lastBlock
expr_stmt|;
block|}
assert|assert
name|pBlock
operator|==
literal|0
operator|:
literal|"pBlock != 0 "
assert|;
name|itrCursor
operator|=
literal|1
expr_stmt|;
return|return
name|currBlock
index|[
literal|0
index|]
return|;
block|}
specifier|public
name|Row
name|next
parameter_list|()
block|{
assert|assert
name|pBlock
operator|<=
name|numBlocks
operator|:
literal|"pBlock "
operator|+
name|pBlock
operator|+
literal|"> numBlocks"
operator|+
name|numBlocks
assert|;
comment|// pBlock should not be greater than numBlocks;
if|if
condition|(
name|pBlock
operator|<
name|numBlocks
condition|)
block|{
if|if
condition|(
name|itrCursor
operator|<
name|blockSize
condition|)
block|{
return|return
name|currBlock
index|[
name|itrCursor
operator|++
index|]
return|;
block|}
elseif|else
if|if
condition|(
operator|++
name|pBlock
operator|<
name|numBlocks
condition|)
block|{
name|currBlock
operator|=
name|getBlock
argument_list|(
name|pBlock
argument_list|)
expr_stmt|;
assert|assert
name|currBlock
operator|!=
literal|null
operator|:
literal|"currBlock == null"
assert|;
name|itrCursor
operator|=
literal|1
expr_stmt|;
return|return
name|currBlock
index|[
literal|0
index|]
return|;
block|}
else|else
block|{
name|itrCursor
operator|=
literal|0
expr_stmt|;
name|currBlock
operator|=
name|lastBlock
expr_stmt|;
block|}
block|}
comment|// last block (pBlock == numBlocks)
if|if
condition|(
name|itrCursor
operator|<
name|addCursor
condition|)
return|return
name|currBlock
index|[
name|itrCursor
operator|++
index|]
return|;
else|else
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|spillBlock
parameter_list|(
name|Row
index|[]
name|block
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
if|if
condition|(
name|tmpFile
operator|==
literal|null
condition|)
block|{
name|tmpFile
operator|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"RowContainer"
argument_list|,
literal|".tmp"
argument_list|,
operator|new
name|File
argument_list|(
literal|"/tmp"
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"RowContainer created temp file "
operator|+
name|tmpFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
comment|// Delete the temp file if the JVM terminate normally through Hadoop job kill command.
comment|// Caveat: it won't be deleted if JVM is killed by 'kill -9'.
name|tmpFile
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|rFile
operator|=
operator|new
name|RandomAccessFile
argument_list|(
name|tmpFile
argument_list|,
literal|"rw"
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|buf
init|=
name|serialize
argument_list|(
name|block
argument_list|)
decl_stmt|;
name|long
name|offset
init|=
name|rFile
operator|.
name|getFilePointer
argument_list|()
decl_stmt|;
name|long
name|len
init|=
name|buf
operator|.
name|length
decl_stmt|;
comment|// append the block at the end
name|rFile
operator|.
name|seek
argument_list|(
name|offset
argument_list|)
expr_stmt|;
name|rFile
operator|.
name|write
argument_list|(
name|buf
argument_list|)
expr_stmt|;
comment|// maintain block metadata
name|addBlockMetadata
argument_list|(
name|offset
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Maintain the blocks meta data: number of blocks, and the block (offset, length)    * pair.     * @param offset offset of the tmp file where the block was serialized.    * @param len the length of the serialized block in the temp file.    */
specifier|private
name|void
name|addBlockMetadata
parameter_list|(
name|long
name|offset
parameter_list|,
name|long
name|len
parameter_list|)
block|{
if|if
condition|(
operator|(
name|numBlocks
operator|+
literal|1
operator|)
operator|*
literal|2
operator|>=
name|off_len
operator|.
name|length
condition|)
block|{
comment|// expand (offset, len) array
name|off_len
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|off_len
argument_list|,
name|off_len
operator|.
name|length
operator|*
literal|2
argument_list|)
expr_stmt|;
block|}
name|off_len
index|[
name|numBlocks
operator|*
literal|2
index|]
operator|=
name|offset
expr_stmt|;
name|off_len
index|[
name|numBlocks
operator|*
literal|2
operator|+
literal|1
index|]
operator|=
name|len
expr_stmt|;
operator|++
name|numBlocks
expr_stmt|;
block|}
comment|/**    * Serialize the object into a byte array.    * @param obj object needed to be serialized    * @return the byte array that contains the serialized array.    * @throws IOException    */
specifier|private
name|byte
index|[]
name|serialize
parameter_list|(
name|Row
index|[]
name|obj
parameter_list|)
throws|throws
name|HiveException
block|{
assert|assert
operator|(
name|serde
operator|!=
literal|null
operator|&&
name|standardOI
operator|!=
literal|null
operator|)
assert|;
name|ByteArrayOutputStream
name|baos
decl_stmt|;
name|ObjectOutputStream
name|oos
decl_stmt|;
try|try
block|{
name|baos
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|oos
operator|=
operator|new
name|ObjectOutputStream
argument_list|(
name|baos
argument_list|)
expr_stmt|;
comment|// # of rows
name|oos
operator|.
name|writeInt
argument_list|(
name|obj
operator|.
name|length
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
name|obj
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|Writable
name|outVal
init|=
name|serde
operator|.
name|serialize
argument_list|(
name|obj
index|[
name|i
index|]
argument_list|,
name|standardOI
argument_list|)
decl_stmt|;
name|outVal
operator|.
name|write
argument_list|(
name|oos
argument_list|)
expr_stmt|;
block|}
name|oos
operator|.
name|close
argument_list|()
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
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|baos
operator|.
name|toByteArray
argument_list|()
return|;
block|}
comment|/**    * Deserialize an object from a byte array    * @param buf the byte array containing the serialized object.    * @return the serialized object.    */
specifier|private
name|Row
index|[]
name|deserialize
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|)
throws|throws
name|HiveException
block|{
name|ByteArrayInputStream
name|bais
decl_stmt|;
name|ObjectInputStream
name|ois
decl_stmt|;
try|try
block|{
name|bais
operator|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|buf
argument_list|)
expr_stmt|;
name|ois
operator|=
operator|new
name|ObjectInputStream
argument_list|(
name|bais
argument_list|)
expr_stmt|;
name|int
name|sz
init|=
name|ois
operator|.
name|readInt
argument_list|()
decl_stmt|;
assert|assert
name|sz
operator|==
name|blockSize
operator|:
literal|"deserialized size "
operator|+
name|sz
operator|+
literal|" is not the same as block size "
operator|+
name|blockSize
assert|;
name|Row
index|[]
name|ret
init|=
operator|(
name|Row
index|[]
operator|)
operator|new
name|ArrayList
index|[
name|sz
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
name|sz
condition|;
operator|++
name|i
control|)
block|{
name|Writable
name|val
init|=
name|serde
operator|.
name|getSerializedClass
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|val
operator|.
name|readFields
argument_list|(
name|ois
argument_list|)
expr_stmt|;
name|ret
index|[
name|i
index|]
operator|=
operator|(
name|Row
operator|)
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|serde
operator|.
name|deserialize
argument_list|(
name|val
argument_list|)
argument_list|,
name|serde
operator|.
name|getObjectInspector
argument_list|()
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
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
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Get the number of elements in the RowContainer.    * @return number of elements in the RowContainer    */
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|size
return|;
block|}
comment|/**    * Remove all elements in the RowContainer.    */
specifier|public
name|void
name|clear
parameter_list|()
throws|throws
name|HiveException
block|{
name|itrCursor
operator|=
literal|0
expr_stmt|;
name|addCursor
operator|=
literal|0
expr_stmt|;
name|numBlocks
operator|=
literal|0
expr_stmt|;
name|size
operator|=
literal|0
expr_stmt|;
try|try
block|{
if|if
condition|(
name|rFile
operator|!=
literal|null
condition|)
name|rFile
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|tmpFile
operator|!=
literal|null
condition|)
name|tmpFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|tmpFile
operator|=
literal|null
expr_stmt|;
block|}
specifier|private
name|Row
index|[]
name|getBlock
parameter_list|(
name|int
name|block
parameter_list|)
block|{
name|long
name|offset
init|=
name|off_len
index|[
name|block
operator|*
literal|2
index|]
decl_stmt|;
name|long
name|len
init|=
name|off_len
index|[
name|block
operator|*
literal|2
operator|+
literal|1
index|]
decl_stmt|;
name|byte
index|[]
name|buf
init|=
operator|new
name|byte
index|[
operator|(
name|int
operator|)
name|len
index|]
decl_stmt|;
try|try
block|{
name|rFile
operator|.
name|seek
argument_list|(
name|offset
argument_list|)
expr_stmt|;
name|rFile
operator|.
name|readFully
argument_list|(
name|buf
argument_list|)
expr_stmt|;
name|currBlock
operator|=
name|deserialize
argument_list|(
name|buf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|currBlock
return|;
block|}
block|}
end_class

end_unit

