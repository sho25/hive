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
operator|.
name|rcfile
operator|.
name|merge
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
name|RCFile
operator|.
name|Reader
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
name|RecordReader
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|public
class|class
name|RCFileBlockMergeRecordReader
implements|implements
name|RecordReader
argument_list|<
name|RCFileKeyBufferWrapper
argument_list|,
name|RCFileValueBufferWrapper
argument_list|>
block|{
specifier|private
specifier|final
name|Reader
name|in
decl_stmt|;
specifier|private
specifier|final
name|long
name|start
decl_stmt|;
specifier|private
specifier|final
name|long
name|end
decl_stmt|;
specifier|private
name|boolean
name|more
init|=
literal|true
decl_stmt|;
specifier|private
specifier|final
name|Path
name|path
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|RCFileBlockMergeRecordReader
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSplit
name|split
parameter_list|)
throws|throws
name|IOException
block|{
name|path
operator|=
name|split
operator|.
name|getPath
argument_list|()
expr_stmt|;
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|this
operator|.
name|in
operator|=
operator|new
name|RCFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|end
operator|=
name|split
operator|.
name|getStart
argument_list|()
operator|+
name|split
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
if|if
condition|(
name|split
operator|.
name|getStart
argument_list|()
operator|>
name|in
operator|.
name|getPosition
argument_list|()
condition|)
block|{
name|in
operator|.
name|sync
argument_list|(
name|split
operator|.
name|getStart
argument_list|()
argument_list|)
expr_stmt|;
comment|// sync to start
block|}
name|this
operator|.
name|start
operator|=
name|in
operator|.
name|getPosition
argument_list|()
expr_stmt|;
name|more
operator|=
name|start
operator|<
name|end
expr_stmt|;
block|}
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getKeyClass
parameter_list|()
block|{
return|return
name|RCFileKeyBufferWrapper
operator|.
name|class
return|;
block|}
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getValueClass
parameter_list|()
block|{
return|return
name|RCFileValueBufferWrapper
operator|.
name|class
return|;
block|}
specifier|public
name|RCFileKeyBufferWrapper
name|createKey
parameter_list|()
block|{
return|return
operator|new
name|RCFileKeyBufferWrapper
argument_list|()
return|;
block|}
specifier|public
name|RCFileValueBufferWrapper
name|createValue
parameter_list|()
block|{
return|return
operator|new
name|RCFileValueBufferWrapper
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|RCFileKeyBufferWrapper
name|key
parameter_list|,
name|RCFileValueBufferWrapper
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|more
operator|=
name|nextBlock
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|more
return|;
block|}
specifier|protected
name|boolean
name|nextBlock
parameter_list|(
name|RCFileKeyBufferWrapper
name|keyWrapper
parameter_list|,
name|RCFileValueBufferWrapper
name|valueWrapper
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|more
condition|)
block|{
return|return
literal|false
return|;
block|}
name|more
operator|=
name|in
operator|.
name|nextBlock
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|more
condition|)
block|{
return|return
literal|false
return|;
block|}
name|keyWrapper
operator|.
name|keyBuffer
operator|=
name|this
operator|.
name|in
operator|.
name|getCurrentKeyBufferObj
argument_list|()
expr_stmt|;
name|keyWrapper
operator|.
name|recordLength
operator|=
name|this
operator|.
name|in
operator|.
name|getCurrentBlockLength
argument_list|()
expr_stmt|;
name|keyWrapper
operator|.
name|keyLength
operator|=
name|this
operator|.
name|in
operator|.
name|getCurrentKeyLength
argument_list|()
expr_stmt|;
name|keyWrapper
operator|.
name|compressedKeyLength
operator|=
name|this
operator|.
name|in
operator|.
name|getCurrentCompressedKeyLen
argument_list|()
expr_stmt|;
name|keyWrapper
operator|.
name|codec
operator|=
name|this
operator|.
name|in
operator|.
name|getCompressionCodec
argument_list|()
expr_stmt|;
name|keyWrapper
operator|.
name|inputPath
operator|=
name|path
expr_stmt|;
name|valueWrapper
operator|.
name|valueBuffer
operator|=
name|this
operator|.
name|in
operator|.
name|getCurrentValueBufferObj
argument_list|()
expr_stmt|;
name|long
name|lastSeenSyncPos
init|=
name|in
operator|.
name|lastSeenSyncPos
argument_list|()
decl_stmt|;
if|if
condition|(
name|lastSeenSyncPos
operator|>=
name|end
condition|)
block|{
name|more
operator|=
literal|false
expr_stmt|;
return|return
name|more
return|;
block|}
return|return
name|more
return|;
block|}
comment|/**    * Return the progress within the input split.    *    * @return 0.0 to 1.0 of the input byte range    */
specifier|public
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|end
operator|==
name|start
condition|)
block|{
return|return
literal|0.0f
return|;
block|}
else|else
block|{
return|return
name|Math
operator|.
name|min
argument_list|(
literal|1.0f
argument_list|,
operator|(
name|in
operator|.
name|getPosition
argument_list|()
operator|-
name|start
operator|)
operator|/
call|(
name|float
call|)
argument_list|(
name|end
operator|-
name|start
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|public
name|long
name|getPos
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|in
operator|.
name|getPosition
argument_list|()
return|;
block|}
specifier|protected
name|void
name|seek
parameter_list|(
name|long
name|pos
parameter_list|)
throws|throws
name|IOException
block|{
name|in
operator|.
name|seek
argument_list|(
name|pos
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|getStart
parameter_list|()
block|{
return|return
name|start
return|;
block|}
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

