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
name|llap
operator|.
name|io
operator|.
name|decode
operator|.
name|orc
operator|.
name|stream
operator|.
name|readers
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
name|hive
operator|.
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|EncodedColumnBatch
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
name|llap
operator|.
name|io
operator|.
name|decode
operator|.
name|orc
operator|.
name|stream
operator|.
name|StreamUtils
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
name|orc
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
name|hive
operator|.
name|ql
operator|.
name|io
operator|.
name|orc
operator|.
name|InStream
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
name|orc
operator|.
name|OrcProto
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
name|orc
operator|.
name|PositionProvider
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
name|orc
operator|.
name|RecordReaderImpl
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
specifier|public
class|class
name|TimestampStreamReader
extends|extends
name|RecordReaderImpl
operator|.
name|TimestampTreeReader
block|{
specifier|private
name|boolean
name|isFileCompressed
decl_stmt|;
specifier|private
name|TimestampStreamReader
parameter_list|(
name|int
name|columnId
parameter_list|,
name|InStream
name|present
parameter_list|,
name|InStream
name|data
parameter_list|,
name|InStream
name|nanos
parameter_list|,
name|boolean
name|isFileCompressed
parameter_list|,
name|OrcProto
operator|.
name|ColumnEncoding
name|encoding
parameter_list|,
name|boolean
name|skipCorrupt
parameter_list|,
name|OrcProto
operator|.
name|RowIndexEntry
name|rowIndex
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|columnId
argument_list|,
name|present
argument_list|,
name|data
argument_list|,
name|nanos
argument_list|,
name|encoding
argument_list|,
name|skipCorrupt
argument_list|)
expr_stmt|;
name|this
operator|.
name|isFileCompressed
operator|=
name|isFileCompressed
expr_stmt|;
comment|// position the readers based on the specified row index
name|seek
argument_list|(
name|StreamUtils
operator|.
name|getPositionProvider
argument_list|(
name|rowIndex
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|seek
parameter_list|(
name|PositionProvider
name|index
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|present
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|isFileCompressed
condition|)
block|{
name|index
operator|.
name|getNext
argument_list|()
expr_stmt|;
block|}
name|present
operator|.
name|seek
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isFileCompressed
condition|)
block|{
name|index
operator|.
name|getNext
argument_list|()
expr_stmt|;
block|}
name|data
operator|.
name|seek
argument_list|(
name|index
argument_list|)
expr_stmt|;
if|if
condition|(
name|isFileCompressed
condition|)
block|{
name|index
operator|.
name|getNext
argument_list|()
expr_stmt|;
block|}
name|nanos
operator|.
name|seek
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|StreamReaderBuilder
block|{
specifier|private
name|String
name|fileName
decl_stmt|;
specifier|private
name|int
name|columnIndex
decl_stmt|;
specifier|private
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|presentStream
decl_stmt|;
specifier|private
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|dataStream
decl_stmt|;
specifier|private
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|nanosStream
decl_stmt|;
specifier|private
name|CompressionCodec
name|compressionCodec
decl_stmt|;
specifier|private
name|int
name|bufferSize
decl_stmt|;
specifier|private
name|OrcProto
operator|.
name|RowIndexEntry
name|rowIndex
decl_stmt|;
specifier|private
name|OrcProto
operator|.
name|ColumnEncoding
name|columnEncoding
decl_stmt|;
specifier|private
name|boolean
name|skipCorrupt
decl_stmt|;
specifier|private
name|int
name|presentCBIdx
decl_stmt|;
specifier|private
name|int
name|secondsCBIdx
decl_stmt|;
specifier|private
name|int
name|nanosCBIdx
decl_stmt|;
specifier|public
name|StreamReaderBuilder
name|setFileName
parameter_list|(
name|String
name|fileName
parameter_list|)
block|{
name|this
operator|.
name|fileName
operator|=
name|fileName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|StreamReaderBuilder
name|setColumnIndex
parameter_list|(
name|int
name|columnIndex
parameter_list|)
block|{
name|this
operator|.
name|columnIndex
operator|=
name|columnIndex
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|StreamReaderBuilder
name|setPresentStream
parameter_list|(
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|presentStream
parameter_list|)
block|{
name|this
operator|.
name|presentStream
operator|=
name|presentStream
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|StreamReaderBuilder
name|setSecondsStream
parameter_list|(
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|dataStream
parameter_list|)
block|{
name|this
operator|.
name|dataStream
operator|=
name|dataStream
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|StreamReaderBuilder
name|setNanosStream
parameter_list|(
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|secondaryStream
parameter_list|)
block|{
name|this
operator|.
name|nanosStream
operator|=
name|secondaryStream
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|StreamReaderBuilder
name|setCompressionCodec
parameter_list|(
name|CompressionCodec
name|compressionCodec
parameter_list|)
block|{
name|this
operator|.
name|compressionCodec
operator|=
name|compressionCodec
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|StreamReaderBuilder
name|setBufferSize
parameter_list|(
name|int
name|bufferSize
parameter_list|)
block|{
name|this
operator|.
name|bufferSize
operator|=
name|bufferSize
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|StreamReaderBuilder
name|setRowIndex
parameter_list|(
name|OrcProto
operator|.
name|RowIndexEntry
name|rowIndex
parameter_list|)
block|{
name|this
operator|.
name|rowIndex
operator|=
name|rowIndex
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|StreamReaderBuilder
name|setColumnEncoding
parameter_list|(
name|OrcProto
operator|.
name|ColumnEncoding
name|encoding
parameter_list|)
block|{
name|this
operator|.
name|columnEncoding
operator|=
name|encoding
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|StreamReaderBuilder
name|skipCorrupt
parameter_list|(
name|boolean
name|skipCorrupt
parameter_list|)
block|{
name|this
operator|.
name|skipCorrupt
operator|=
name|skipCorrupt
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|StreamReaderBuilder
name|setPresentCompressionBufferIndex
parameter_list|(
name|int
name|presentCBIdx
parameter_list|)
block|{
name|this
operator|.
name|presentCBIdx
operator|=
name|presentCBIdx
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|StreamReaderBuilder
name|setSecondsCompressionBufferIndex
parameter_list|(
name|int
name|secondsCBIdx
parameter_list|)
block|{
name|this
operator|.
name|secondsCBIdx
operator|=
name|secondsCBIdx
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|StreamReaderBuilder
name|setNanosCompressionBufferIndex
parameter_list|(
name|int
name|nanosCBIdx
parameter_list|)
block|{
name|this
operator|.
name|nanosCBIdx
operator|=
name|nanosCBIdx
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|TimestampStreamReader
name|build
parameter_list|()
throws|throws
name|IOException
block|{
name|InStream
name|present
init|=
name|StreamUtils
operator|.
name|createInStream
argument_list|(
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|PRESENT
operator|.
name|name
argument_list|()
argument_list|,
name|fileName
argument_list|,
literal|null
argument_list|,
name|bufferSize
argument_list|,
name|presentStream
argument_list|,
name|presentCBIdx
argument_list|)
decl_stmt|;
name|InStream
name|data
init|=
name|StreamUtils
operator|.
name|createInStream
argument_list|(
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|DATA
operator|.
name|name
argument_list|()
argument_list|,
name|fileName
argument_list|,
literal|null
argument_list|,
name|bufferSize
argument_list|,
name|dataStream
argument_list|,
name|secondsCBIdx
argument_list|)
decl_stmt|;
name|InStream
name|nanos
init|=
name|StreamUtils
operator|.
name|createInStream
argument_list|(
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|SECONDARY
operator|.
name|name
argument_list|()
argument_list|,
name|fileName
argument_list|,
literal|null
argument_list|,
name|bufferSize
argument_list|,
name|nanosStream
argument_list|,
name|nanosCBIdx
argument_list|)
decl_stmt|;
name|boolean
name|isFileCompressed
init|=
name|compressionCodec
operator|!=
literal|null
decl_stmt|;
return|return
operator|new
name|TimestampStreamReader
argument_list|(
name|columnIndex
argument_list|,
name|present
argument_list|,
name|data
argument_list|,
name|nanos
argument_list|,
name|isFileCompressed
argument_list|,
name|columnEncoding
argument_list|,
name|skipCorrupt
argument_list|,
name|rowIndex
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
name|StreamReaderBuilder
name|builder
parameter_list|()
block|{
return|return
operator|new
name|StreamReaderBuilder
argument_list|()
return|;
block|}
block|}
end_class

end_unit

