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
name|api
operator|.
name|cache
operator|.
name|LlapMemoryBuffer
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
name|StreamUtils
block|{
specifier|public
specifier|static
name|InStream
name|createInStream
parameter_list|(
name|String
name|streamName
parameter_list|,
name|String
name|fileName
parameter_list|,
name|CompressionCodec
name|codec
parameter_list|,
name|int
name|bufferSize
parameter_list|,
name|EncodedColumnBatch
operator|.
name|StreamBuffer
name|streamBuffer
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|numBuffers
init|=
name|streamBuffer
operator|.
name|cacheBuffers
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|DiskRange
argument_list|>
name|input
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numBuffers
argument_list|)
decl_stmt|;
name|int
name|totalLength
init|=
literal|0
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
name|numBuffers
condition|;
name|i
operator|++
control|)
block|{
name|LlapMemoryBuffer
name|lmb
init|=
name|streamBuffer
operator|.
name|cacheBuffers
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|input
operator|.
name|add
argument_list|(
operator|new
name|RecordReaderImpl
operator|.
name|CacheChunk
argument_list|(
name|lmb
argument_list|,
literal|0
argument_list|,
name|lmb
operator|.
name|byteBuffer
operator|.
name|limit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|totalLength
operator|+=
name|lmb
operator|.
name|byteBuffer
operator|.
name|limit
argument_list|()
expr_stmt|;
block|}
return|return
name|InStream
operator|.
name|create
argument_list|(
name|fileName
argument_list|,
name|streamName
argument_list|,
name|input
argument_list|,
name|totalLength
argument_list|,
name|codec
argument_list|,
name|bufferSize
argument_list|,
literal|null
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|PositionProvider
name|getPositionProvider
parameter_list|(
name|OrcProto
operator|.
name|RowIndexEntry
name|rowIndex
parameter_list|)
block|{
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
return|return
name|positionProvider
return|;
block|}
block|}
end_class

end_unit

