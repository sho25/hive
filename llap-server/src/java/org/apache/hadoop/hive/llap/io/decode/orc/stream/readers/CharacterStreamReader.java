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
name|CharacterStreamReader
extends|extends
name|RecordReaderImpl
operator|.
name|StringTreeReader
block|{
specifier|private
name|boolean
name|isFileCompressed
decl_stmt|;
specifier|private
name|OrcProto
operator|.
name|RowIndexEntry
name|rowIndex
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
name|InStream
name|present
parameter_list|,
name|InStream
name|data
parameter_list|,
name|InStream
name|length
parameter_list|,
name|InStream
name|dictionary
parameter_list|,
name|boolean
name|isFileCompressed
parameter_list|,
name|OrcProto
operator|.
name|ColumnEncoding
name|encoding
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
argument_list|)
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
name|isFileCompressed
operator|=
name|isFileCompressed
expr_stmt|;
comment|// position the readers based on the specified row index
name|PositionProvider
name|positionProvider
init|=
operator|new
name|RecordReaderImpl
operator|.
name|PositionProviderImpl
argument_list|(
name|rowIndex
argument_list|)
decl_stmt|;
name|seek
argument_list|(
name|positionProvider
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|seek
parameter_list|(
name|PositionProvider
name|positionProvider
parameter_list|)
throws|throws
name|IOException
block|{
name|reader
operator|.
name|seek
argument_list|(
name|positionProvider
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
name|CharacterStreamReader
name|build
parameter_list|()
throws|throws
name|IOException
block|{
name|InStream
name|present
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|presentStream
operator|!=
literal|null
condition|)
block|{
name|present
operator|=
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
argument_list|)
expr_stmt|;
block|}
name|InStream
name|data
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|dataStream
operator|!=
literal|null
condition|)
block|{
name|data
operator|=
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
argument_list|)
expr_stmt|;
block|}
name|InStream
name|length
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|lengthStream
operator|!=
literal|null
condition|)
block|{
name|length
operator|=
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
name|LENGTH
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
name|lengthStream
argument_list|)
expr_stmt|;
block|}
name|InStream
name|dictionary
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|dictionaryStream
operator|!=
literal|null
condition|)
block|{
name|dictionary
operator|=
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
name|DICTIONARY_DATA
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
name|dictionaryStream
argument_list|)
expr_stmt|;
block|}
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
name|compressionCodec
operator|!=
literal|null
argument_list|,
name|columnEncoding
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

