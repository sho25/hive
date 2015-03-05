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
name|common
operator|.
name|DiskRange
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
name|SettableUncompressedStream
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Stream reader for char and varchar column types.  */
end_comment

begin_class
specifier|public
class|class
name|CharacterStreamReader
extends|extends
name|RecordReaderImpl
operator|.
name|StringTreeReader
block|{
specifier|private
name|boolean
name|_isFileCompressed
decl_stmt|;
specifier|private
name|boolean
name|_isDictionaryEncoding
decl_stmt|;
specifier|private
name|SettableUncompressedStream
name|_presentStream
decl_stmt|;
specifier|private
name|SettableUncompressedStream
name|_dataStream
decl_stmt|;
specifier|private
name|SettableUncompressedStream
name|_lengthStream
decl_stmt|;
specifier|private
name|SettableUncompressedStream
name|_dictionaryStream
decl_stmt|;
specifier|private
name|CharacterStreamReader
parameter_list|(
name|int
name|columnId
parameter_list|,
name|int
name|maxLength
parameter_list|,
name|OrcProto
operator|.
name|Type
name|charType
parameter_list|,
name|SettableUncompressedStream
name|present
parameter_list|,
name|SettableUncompressedStream
name|data
parameter_list|,
name|SettableUncompressedStream
name|length
parameter_list|,
name|SettableUncompressedStream
name|dictionary
parameter_list|,
name|boolean
name|isFileCompressed
parameter_list|,
name|OrcProto
operator|.
name|ColumnEncoding
name|encoding
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|columnId
argument_list|)
expr_stmt|;
name|this
operator|.
name|_isDictionaryEncoding
operator|=
name|dictionary
operator|!=
literal|null
expr_stmt|;
if|if
condition|(
name|charType
operator|.
name|getKind
argument_list|()
operator|==
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|CHAR
condition|)
block|{
name|reader
operator|=
operator|new
name|RecordReaderImpl
operator|.
name|CharTreeReader
argument_list|(
name|columnId
argument_list|,
name|maxLength
argument_list|,
name|present
argument_list|,
name|data
argument_list|,
name|length
argument_list|,
name|dictionary
argument_list|,
name|encoding
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|charType
operator|.
name|getKind
argument_list|()
operator|==
name|OrcProto
operator|.
name|Type
operator|.
name|Kind
operator|.
name|VARCHAR
condition|)
block|{
name|reader
operator|=
operator|new
name|RecordReaderImpl
operator|.
name|VarcharTreeReader
argument_list|(
name|columnId
argument_list|,
name|maxLength
argument_list|,
name|present
argument_list|,
name|data
argument_list|,
name|length
argument_list|,
name|dictionary
argument_list|,
name|encoding
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unknown character type "
operator|+
name|charType
operator|+
literal|". Expected CHAR or VARCHAR."
argument_list|)
throw|;
block|}
name|this
operator|.
name|_isFileCompressed
operator|=
name|isFileCompressed
expr_stmt|;
name|this
operator|.
name|_presentStream
operator|=
name|present
expr_stmt|;
name|this
operator|.
name|_dataStream
operator|=
name|data
expr_stmt|;
name|this
operator|.
name|_lengthStream
operator|=
name|length
expr_stmt|;
name|this
operator|.
name|_dictionaryStream
operator|=
name|dictionary
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
name|_isFileCompressed
condition|)
block|{
name|index
operator|.
name|getNext
argument_list|()
expr_stmt|;
block|}
name|reader
operator|.
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
name|_isDictionaryEncoding
condition|)
block|{
comment|// DICTIONARY encoding
comment|// data stream could be empty stream or already reached end of stream before present stream.
comment|// This can happen if all values in stream are nulls or last row group values are all null.
if|if
condition|(
name|_dataStream
operator|.
name|available
argument_list|()
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|_isFileCompressed
condition|)
block|{
name|index
operator|.
name|getNext
argument_list|()
expr_stmt|;
block|}
operator|(
operator|(
name|RecordReaderImpl
operator|.
name|StringDictionaryTreeReader
operator|)
name|reader
operator|)
operator|.
name|reader
operator|.
name|seek
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// DIRECT encoding
comment|// data stream could be empty stream or already reached end of stream before present stream.
comment|// This can happen if all values in stream are nulls or last row group values are all null.
if|if
condition|(
name|_dataStream
operator|.
name|available
argument_list|()
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|_isFileCompressed
condition|)
block|{
name|index
operator|.
name|getNext
argument_list|()
expr_stmt|;
block|}
operator|(
operator|(
name|RecordReaderImpl
operator|.
name|StringDirectTreeReader
operator|)
name|reader
operator|)
operator|.
name|stream
operator|.
name|seek
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|_lengthStream
operator|.
name|available
argument_list|()
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|_isFileCompressed
condition|)
block|{
name|index
operator|.
name|getNext
argument_list|()
expr_stmt|;
block|}
operator|(
operator|(
name|RecordReaderImpl
operator|.
name|StringDirectTreeReader
operator|)
name|reader
operator|)
operator|.
name|lengths
operator|.
name|seek
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|setBuffers
parameter_list|(
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|presentStreamBuffer
parameter_list|,
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|dataStreamBuffer
parameter_list|,
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|lengthStreamBuffer
parameter_list|,
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|dictionaryStreamBuffer
parameter_list|,
name|boolean
name|sameStripe
parameter_list|)
block|{
name|long
name|length
decl_stmt|;
if|if
condition|(
name|_presentStream
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|DiskRange
argument_list|>
name|presentDiskRanges
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|length
operator|=
name|StreamUtils
operator|.
name|createDiskRanges
argument_list|(
name|presentStreamBuffer
argument_list|,
name|presentDiskRanges
argument_list|)
expr_stmt|;
name|_presentStream
operator|.
name|setBuffers
argument_list|(
name|presentDiskRanges
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|_dataStream
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|DiskRange
argument_list|>
name|dataDiskRanges
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|length
operator|=
name|StreamUtils
operator|.
name|createDiskRanges
argument_list|(
name|dataStreamBuffer
argument_list|,
name|dataDiskRanges
argument_list|)
expr_stmt|;
name|_dataStream
operator|.
name|setBuffers
argument_list|(
name|dataDiskRanges
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|_isDictionaryEncoding
condition|)
block|{
if|if
condition|(
name|_lengthStream
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|DiskRange
argument_list|>
name|lengthDiskRanges
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|length
operator|=
name|StreamUtils
operator|.
name|createDiskRanges
argument_list|(
name|lengthStreamBuffer
argument_list|,
name|lengthDiskRanges
argument_list|)
expr_stmt|;
name|_lengthStream
operator|.
name|setBuffers
argument_list|(
name|lengthDiskRanges
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
block|}
comment|// set these streams only if the stripe is different
if|if
condition|(
operator|!
name|sameStripe
operator|&&
name|_isDictionaryEncoding
condition|)
block|{
if|if
condition|(
name|_lengthStream
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|DiskRange
argument_list|>
name|lengthDiskRanges
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|length
operator|=
name|StreamUtils
operator|.
name|createDiskRanges
argument_list|(
name|lengthStreamBuffer
argument_list|,
name|lengthDiskRanges
argument_list|)
expr_stmt|;
name|_lengthStream
operator|.
name|setBuffers
argument_list|(
name|lengthDiskRanges
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|_dictionaryStream
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|DiskRange
argument_list|>
name|dictionaryDiskRanges
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|length
operator|=
name|StreamUtils
operator|.
name|createDiskRanges
argument_list|(
name|dictionaryStreamBuffer
argument_list|,
name|dictionaryDiskRanges
argument_list|)
expr_stmt|;
name|_dictionaryStream
operator|.
name|setBuffers
argument_list|(
name|dictionaryDiskRanges
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|StreamReaderBuilder
block|{
specifier|private
name|Long
name|fileId
decl_stmt|;
specifier|private
name|int
name|columnIndex
decl_stmt|;
specifier|private
name|int
name|maxLength
decl_stmt|;
specifier|private
name|OrcProto
operator|.
name|Type
name|charType
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
name|dictionaryStream
decl_stmt|;
specifier|private
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|lengthStream
decl_stmt|;
specifier|private
name|CompressionCodec
name|compressionCodec
decl_stmt|;
specifier|private
name|OrcProto
operator|.
name|ColumnEncoding
name|columnEncoding
decl_stmt|;
specifier|public
name|StreamReaderBuilder
name|setFileId
parameter_list|(
name|Long
name|fileId
parameter_list|)
block|{
name|this
operator|.
name|fileId
operator|=
name|fileId
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
name|setMaxLength
parameter_list|(
name|int
name|maxLength
parameter_list|)
block|{
name|this
operator|.
name|maxLength
operator|=
name|maxLength
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|StreamReaderBuilder
name|setCharacterType
parameter_list|(
name|OrcProto
operator|.
name|Type
name|charType
parameter_list|)
block|{
name|this
operator|.
name|charType
operator|=
name|charType
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
name|setDataStream
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
name|setLengthStream
parameter_list|(
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|lengthStream
parameter_list|)
block|{
name|this
operator|.
name|lengthStream
operator|=
name|lengthStream
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|StreamReaderBuilder
name|setDictionaryStream
parameter_list|(
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|dictStream
parameter_list|)
block|{
name|this
operator|.
name|dictionaryStream
operator|=
name|dictStream
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
name|CharacterStreamReader
name|build
parameter_list|()
throws|throws
name|IOException
block|{
name|SettableUncompressedStream
name|present
init|=
name|StreamUtils
operator|.
name|createLlapInStream
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
name|fileId
argument_list|,
name|presentStream
argument_list|)
decl_stmt|;
name|SettableUncompressedStream
name|data
init|=
name|StreamUtils
operator|.
name|createLlapInStream
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
name|fileId
argument_list|,
name|dataStream
argument_list|)
decl_stmt|;
name|SettableUncompressedStream
name|length
init|=
name|StreamUtils
operator|.
name|createLlapInStream
argument_list|(
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|LENGTH
operator|.
name|name
argument_list|()
argument_list|,
name|fileId
argument_list|,
name|lengthStream
argument_list|)
decl_stmt|;
name|SettableUncompressedStream
name|dictionary
init|=
name|StreamUtils
operator|.
name|createLlapInStream
argument_list|(
name|OrcProto
operator|.
name|Stream
operator|.
name|Kind
operator|.
name|DICTIONARY_DATA
operator|.
name|name
argument_list|()
argument_list|,
name|fileId
argument_list|,
name|dictionaryStream
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
name|CharacterStreamReader
argument_list|(
name|columnIndex
argument_list|,
name|maxLength
argument_list|,
name|charType
argument_list|,
name|present
argument_list|,
name|data
argument_list|,
name|length
argument_list|,
name|dictionary
argument_list|,
name|isFileCompressed
argument_list|,
name|columnEncoding
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

