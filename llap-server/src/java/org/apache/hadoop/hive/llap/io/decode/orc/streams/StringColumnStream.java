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
name|streams
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
name|readers
operator|.
name|DictionaryStringReader
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
name|readers
operator|.
name|DirectStringReader
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
name|readers
operator|.
name|StringReader
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
name|vector
operator|.
name|ColumnVector
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
name|IntegerReader
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
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * String column stream reader.  */
end_comment

begin_class
specifier|public
class|class
name|StringColumnStream
extends|extends
name|BaseColumnStream
block|{
specifier|private
name|IntegerReader
name|lengthReader
decl_stmt|;
specifier|private
name|StringReader
name|stringReader
decl_stmt|;
specifier|private
name|IntegerReader
name|dataReader
decl_stmt|;
specifier|private
name|InStream
name|dictionaryStream
decl_stmt|;
specifier|private
name|InStream
name|lengthStream
decl_stmt|;
specifier|private
name|InStream
name|dataStream
decl_stmt|;
specifier|private
name|OrcProto
operator|.
name|ColumnEncoding
operator|.
name|Kind
name|kind
decl_stmt|;
specifier|public
name|StringColumnStream
parameter_list|(
name|String
name|file
parameter_list|,
name|int
name|colIx
parameter_list|,
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|present
parameter_list|,
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|data
parameter_list|,
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|dictionary
parameter_list|,
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|lengths
parameter_list|,
name|OrcProto
operator|.
name|ColumnEncoding
name|columnEncoding
parameter_list|,
name|CompressionCodec
name|codec
parameter_list|,
name|int
name|bufferSize
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
name|file
argument_list|,
name|colIx
argument_list|,
name|present
argument_list|,
name|codec
argument_list|,
name|bufferSize
argument_list|)
expr_stmt|;
comment|// preconditions check
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|data
argument_list|,
literal|"DATA stream buffer cannot be null"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|columnEncoding
argument_list|,
literal|"ColumnEncoding cannot be null"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|lengths
argument_list|,
literal|"ColumnEncoding is "
operator|+
name|columnEncoding
operator|+
literal|"."
operator|+
literal|" Length stream cannot be null"
argument_list|)
expr_stmt|;
name|this
operator|.
name|dataStream
operator|=
name|StreamUtils
operator|.
name|createInStream
argument_list|(
literal|"DATA"
argument_list|,
name|file
argument_list|,
literal|null
argument_list|,
name|bufferSize
argument_list|,
name|data
argument_list|)
expr_stmt|;
name|this
operator|.
name|dataReader
operator|=
name|StreamUtils
operator|.
name|createIntegerReader
argument_list|(
name|kind
argument_list|,
name|dataStream
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|lengthStream
operator|=
name|StreamUtils
operator|.
name|createInStream
argument_list|(
literal|"LENGTH"
argument_list|,
name|file
argument_list|,
literal|null
argument_list|,
name|bufferSize
argument_list|,
name|lengths
argument_list|)
expr_stmt|;
name|this
operator|.
name|lengthReader
operator|=
name|StreamUtils
operator|.
name|createIntegerReader
argument_list|(
name|kind
argument_list|,
name|lengthStream
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|kind
operator|=
name|columnEncoding
operator|.
name|getKind
argument_list|()
expr_stmt|;
if|if
condition|(
name|kind
operator|.
name|equals
argument_list|(
name|OrcProto
operator|.
name|ColumnEncoding
operator|.
name|Kind
operator|.
name|DICTIONARY
argument_list|)
operator|||
name|kind
operator|.
name|equals
argument_list|(
name|OrcProto
operator|.
name|ColumnEncoding
operator|.
name|Kind
operator|.
name|DICTIONARY_V2
argument_list|)
condition|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|dictionary
argument_list|,
literal|"ColumnEncoding is "
operator|+
name|columnEncoding
operator|+
literal|"."
operator|+
literal|" Dictionary stream cannot be null"
argument_list|)
expr_stmt|;
name|this
operator|.
name|dictionaryStream
operator|=
name|StreamUtils
operator|.
name|createInStream
argument_list|(
name|kind
operator|.
name|toString
argument_list|()
argument_list|,
name|file
argument_list|,
literal|null
argument_list|,
name|bufferSize
argument_list|,
name|dictionary
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|kind
operator|.
name|equals
argument_list|(
name|OrcProto
operator|.
name|ColumnEncoding
operator|.
name|Kind
operator|.
name|DIRECT
argument_list|)
operator|||
name|kind
operator|.
name|equals
argument_list|(
name|OrcProto
operator|.
name|ColumnEncoding
operator|.
name|Kind
operator|.
name|DIRECT_V2
argument_list|)
condition|)
block|{
name|this
operator|.
name|stringReader
operator|=
operator|new
name|DirectStringReader
argument_list|(
name|lengthReader
argument_list|,
name|dataStream
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|stringReader
operator|=
operator|new
name|DictionaryStringReader
argument_list|(
name|lengthReader
argument_list|,
name|dataReader
argument_list|,
name|dictionaryStream
argument_list|,
name|columnEncoding
operator|.
name|getDictionarySize
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|positionReaders
argument_list|(
name|positionProvider
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|positionReaders
parameter_list|(
name|PositionProvider
name|positionProvider
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|positionReaders
argument_list|(
name|positionProvider
argument_list|)
expr_stmt|;
comment|// stream is uncompressed and if file is compressed then skip 1st position in index
if|if
condition|(
name|isFileCompressed
condition|)
block|{
name|positionProvider
operator|.
name|getNext
argument_list|()
expr_stmt|;
block|}
name|dataReader
operator|.
name|seek
argument_list|(
name|positionProvider
argument_list|)
expr_stmt|;
if|if
condition|(
name|kind
operator|.
name|equals
argument_list|(
name|OrcProto
operator|.
name|ColumnEncoding
operator|.
name|Kind
operator|.
name|DIRECT
argument_list|)
operator|||
name|kind
operator|.
name|equals
argument_list|(
name|OrcProto
operator|.
name|ColumnEncoding
operator|.
name|Kind
operator|.
name|DIRECT_V2
argument_list|)
condition|)
block|{
if|if
condition|(
name|isFileCompressed
condition|)
block|{
name|positionProvider
operator|.
name|getNext
argument_list|()
expr_stmt|;
block|}
name|lengthReader
operator|.
name|seek
argument_list|(
name|positionProvider
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|ColumnVector
name|nextVector
parameter_list|(
name|ColumnVector
name|previousVector
parameter_list|,
name|int
name|batchSize
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|nextVector
argument_list|(
name|previousVector
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
return|return
name|stringReader
operator|.
name|nextVector
argument_list|(
name|previousVector
argument_list|,
name|batchSize
argument_list|)
return|;
block|}
block|}
end_class

end_unit

