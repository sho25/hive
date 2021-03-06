begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|llap
operator|.
name|shufflehandler
package|;
end_package

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
name|IOException
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
name|channels
operator|.
name|FileChannel
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|channels
operator|.
name|WritableByteChannel
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
name|hadoop
operator|.
name|io
operator|.
name|ReadaheadPool
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
name|ReadaheadPool
operator|.
name|ReadaheadRequest
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
name|nativeio
operator|.
name|NativeIO
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|DefaultFileRegion
import|;
end_import

begin_class
specifier|public
class|class
name|FadvisedFileRegion
extends|extends
name|DefaultFileRegion
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
name|FadvisedFileRegion
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|manageOsCache
decl_stmt|;
specifier|private
specifier|final
name|int
name|readaheadLength
decl_stmt|;
specifier|private
specifier|final
name|ReadaheadPool
name|readaheadPool
decl_stmt|;
specifier|private
specifier|final
name|FileDescriptor
name|fd
decl_stmt|;
specifier|private
specifier|final
name|String
name|identifier
decl_stmt|;
specifier|private
specifier|final
name|long
name|count
decl_stmt|;
specifier|private
specifier|final
name|long
name|position
decl_stmt|;
specifier|private
specifier|final
name|int
name|shuffleBufferSize
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|shuffleTransferToAllowed
decl_stmt|;
specifier|private
specifier|final
name|FileChannel
name|fileChannel
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|canEvictAfterTransfer
decl_stmt|;
specifier|private
name|ReadaheadRequest
name|readaheadRequest
decl_stmt|;
specifier|public
name|FadvisedFileRegion
parameter_list|(
name|RandomAccessFile
name|file
parameter_list|,
name|long
name|position
parameter_list|,
name|long
name|count
parameter_list|,
name|boolean
name|manageOsCache
parameter_list|,
name|int
name|readaheadLength
parameter_list|,
name|ReadaheadPool
name|readaheadPool
parameter_list|,
name|String
name|identifier
parameter_list|,
name|int
name|shuffleBufferSize
parameter_list|,
name|boolean
name|shuffleTransferToAllowed
parameter_list|,
name|boolean
name|canEvictAfterTransfer
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|file
operator|.
name|getChannel
argument_list|()
argument_list|,
name|position
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|this
operator|.
name|manageOsCache
operator|=
name|manageOsCache
expr_stmt|;
name|this
operator|.
name|readaheadLength
operator|=
name|readaheadLength
expr_stmt|;
name|this
operator|.
name|readaheadPool
operator|=
name|readaheadPool
expr_stmt|;
name|this
operator|.
name|fd
operator|=
name|file
operator|.
name|getFD
argument_list|()
expr_stmt|;
name|this
operator|.
name|identifier
operator|=
name|identifier
expr_stmt|;
name|this
operator|.
name|fileChannel
operator|=
name|file
operator|.
name|getChannel
argument_list|()
expr_stmt|;
name|this
operator|.
name|count
operator|=
name|count
expr_stmt|;
name|this
operator|.
name|position
operator|=
name|position
expr_stmt|;
name|this
operator|.
name|shuffleBufferSize
operator|=
name|shuffleBufferSize
expr_stmt|;
name|this
operator|.
name|shuffleTransferToAllowed
operator|=
name|shuffleTransferToAllowed
expr_stmt|;
comment|// To indicate whether the pages should be thrown away or not.
name|this
operator|.
name|canEvictAfterTransfer
operator|=
name|canEvictAfterTransfer
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|transferTo
parameter_list|(
name|WritableByteChannel
name|target
parameter_list|,
name|long
name|position
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|manageOsCache
operator|&&
name|readaheadPool
operator|!=
literal|null
condition|)
block|{
name|readaheadRequest
operator|=
name|readaheadPool
operator|.
name|readaheadStream
argument_list|(
name|identifier
argument_list|,
name|fd
argument_list|,
name|getPosition
argument_list|()
operator|+
name|position
argument_list|,
name|readaheadLength
argument_list|,
name|getPosition
argument_list|()
operator|+
name|getCount
argument_list|()
argument_list|,
name|readaheadRequest
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|shuffleTransferToAllowed
condition|)
block|{
return|return
name|super
operator|.
name|transferTo
argument_list|(
name|target
argument_list|,
name|position
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|customShuffleTransfer
argument_list|(
name|target
argument_list|,
name|position
argument_list|)
return|;
block|}
block|}
comment|/**    * This method transfers data using local buffer. It transfers data from     * a disk to a local buffer in memory, and then it transfers data from the     * buffer to the target. This is used only if transferTo is disallowed in    * the configuration file. super.TransferTo does not perform well on Windows     * due to a small IO request generated. customShuffleTransfer can control     * the size of the IO requests by changing the size of the intermediate     * buffer.    */
annotation|@
name|VisibleForTesting
name|long
name|customShuffleTransfer
parameter_list|(
name|WritableByteChannel
name|target
parameter_list|,
name|long
name|position
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|actualCount
init|=
name|this
operator|.
name|count
operator|-
name|position
decl_stmt|;
if|if
condition|(
name|actualCount
operator|<
literal|0
operator|||
name|position
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"position out of range: "
operator|+
name|position
operator|+
literal|" (expected: 0 - "
operator|+
operator|(
name|this
operator|.
name|count
operator|-
literal|1
operator|)
operator|+
literal|')'
argument_list|)
throw|;
block|}
if|if
condition|(
name|actualCount
operator|==
literal|0
condition|)
block|{
return|return
literal|0L
return|;
block|}
name|long
name|trans
init|=
name|actualCount
decl_stmt|;
name|int
name|readSize
decl_stmt|;
name|ByteBuffer
name|byteBuffer
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|this
operator|.
name|shuffleBufferSize
argument_list|)
decl_stmt|;
while|while
condition|(
name|trans
operator|>
literal|0L
operator|&&
operator|(
name|readSize
operator|=
name|fileChannel
operator|.
name|read
argument_list|(
name|byteBuffer
argument_list|,
name|this
operator|.
name|position
operator|+
name|position
argument_list|)
operator|)
operator|>
literal|0
condition|)
block|{
comment|//adjust counters and buffer limit
if|if
condition|(
name|readSize
operator|<
name|trans
condition|)
block|{
name|trans
operator|-=
name|readSize
expr_stmt|;
name|position
operator|+=
name|readSize
expr_stmt|;
name|byteBuffer
operator|.
name|flip
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|//We can read more than we need if the actualCount is not multiple
comment|//of the byteBuffer size and file is big enough. In that case we cannot
comment|//use flip method but we need to set buffer limit manually to trans.
name|byteBuffer
operator|.
name|limit
argument_list|(
operator|(
name|int
operator|)
name|trans
argument_list|)
expr_stmt|;
name|byteBuffer
operator|.
name|position
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|position
operator|+=
name|trans
expr_stmt|;
name|trans
operator|=
literal|0
expr_stmt|;
block|}
comment|//write data to the target
while|while
condition|(
name|byteBuffer
operator|.
name|hasRemaining
argument_list|()
condition|)
block|{
name|target
operator|.
name|write
argument_list|(
name|byteBuffer
argument_list|)
expr_stmt|;
block|}
name|byteBuffer
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
return|return
name|actualCount
operator|-
name|trans
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|releaseExternalResources
parameter_list|()
block|{
if|if
condition|(
name|readaheadRequest
operator|!=
literal|null
condition|)
block|{
name|readaheadRequest
operator|.
name|cancel
argument_list|()
expr_stmt|;
block|}
name|super
operator|.
name|releaseExternalResources
argument_list|()
expr_stmt|;
block|}
comment|/**    * Call when the transfer completes successfully so we can advise the OS that    * we don't need the region to be cached anymore.    */
specifier|public
name|void
name|transferSuccessful
parameter_list|()
block|{
if|if
condition|(
name|manageOsCache
operator|&&
name|getCount
argument_list|()
operator|>
literal|0
condition|)
block|{
try|try
block|{
if|if
condition|(
name|canEvictAfterTransfer
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"shuffleBufferSize: {}, path: {}"
argument_list|,
name|shuffleBufferSize
argument_list|,
name|identifier
argument_list|)
expr_stmt|;
name|NativeIO
operator|.
name|POSIX
operator|.
name|getCacheManipulator
argument_list|()
operator|.
name|posixFadviseIfPossible
argument_list|(
name|identifier
argument_list|,
name|fd
argument_list|,
name|getPosition
argument_list|()
argument_list|,
name|getCount
argument_list|()
argument_list|,
name|NativeIO
operator|.
name|POSIX
operator|.
name|POSIX_FADV_DONTNEED
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to manage OS cache for "
operator|+
name|identifier
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

