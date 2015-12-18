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
name|orc
operator|.
name|encoded
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
name|nio
operator|.
name|ByteBuffer
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
name|common
operator|.
name|DiskRangeInfo
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
name|common
operator|.
name|io
operator|.
name|encoded
operator|.
name|EncodedColumnBatch
operator|.
name|ColumnStreamData
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
name|common
operator|.
name|io
operator|.
name|encoded
operator|.
name|MemoryBuffer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|impl
operator|.
name|SettableUncompressedStream
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|impl
operator|.
name|BufferChunk
import|;
end_import

begin_comment
comment|/**  * Stream utility.  */
end_comment

begin_class
specifier|public
class|class
name|StreamUtils
block|{
comment|/**    * Create SettableUncompressedStream from stream buffer.    *    * @param streamName - stream name    * @param fileId - file id    * @param streamBuffer - stream buffer    * @return - SettableUncompressedStream    * @throws IOException    */
specifier|public
specifier|static
name|SettableUncompressedStream
name|createSettableUncompressedStream
parameter_list|(
name|String
name|streamName
parameter_list|,
name|ColumnStreamData
name|streamBuffer
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|streamBuffer
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|DiskRangeInfo
name|diskRangeInfo
init|=
name|createDiskRangeInfo
argument_list|(
name|streamBuffer
argument_list|)
decl_stmt|;
return|return
operator|new
name|SettableUncompressedStream
argument_list|(
name|streamName
argument_list|,
name|diskRangeInfo
operator|.
name|getDiskRanges
argument_list|()
argument_list|,
name|diskRangeInfo
operator|.
name|getTotalLength
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Converts stream buffers to disk ranges.    * @param streamBuffer - stream buffer    * @return - total length of disk ranges    */
specifier|public
specifier|static
name|DiskRangeInfo
name|createDiskRangeInfo
parameter_list|(
name|ColumnStreamData
name|streamBuffer
parameter_list|)
block|{
name|DiskRangeInfo
name|diskRangeInfo
init|=
operator|new
name|DiskRangeInfo
argument_list|(
name|streamBuffer
operator|.
name|getIndexBaseOffset
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|offset
init|=
name|streamBuffer
operator|.
name|getIndexBaseOffset
argument_list|()
decl_stmt|;
comment|// See ctor comment.
comment|// TODO: we should get rid of this
for|for
control|(
name|MemoryBuffer
name|memoryBuffer
range|:
name|streamBuffer
operator|.
name|getCacheBuffers
argument_list|()
control|)
block|{
name|ByteBuffer
name|buffer
init|=
name|memoryBuffer
operator|.
name|getByteBufferDup
argument_list|()
decl_stmt|;
name|diskRangeInfo
operator|.
name|addDiskRange
argument_list|(
operator|new
name|BufferChunk
argument_list|(
name|buffer
argument_list|,
name|offset
argument_list|)
argument_list|)
expr_stmt|;
name|offset
operator|+=
name|buffer
operator|.
name|remaining
argument_list|()
expr_stmt|;
block|}
return|return
name|diskRangeInfo
return|;
block|}
block|}
end_class

end_unit

