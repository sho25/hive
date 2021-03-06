begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|history
operator|.
name|logging
operator|.
name|proto
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|io
operator|.
name|NullWritable
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
name|SequenceFile
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
name|SequenceFile
operator|.
name|CompressionType
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
name|SequenceFile
operator|.
name|Writer
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|MessageLite
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Parser
import|;
end_import

begin_class
specifier|public
class|class
name|ProtoMessageWriter
parameter_list|<
name|T
extends|extends
name|MessageLite
parameter_list|>
implements|implements
name|Closeable
block|{
specifier|private
specifier|final
name|Path
name|filePath
decl_stmt|;
specifier|private
specifier|final
name|Writer
name|writer
decl_stmt|;
specifier|private
specifier|final
name|ProtoMessageWritable
argument_list|<
name|T
argument_list|>
name|writable
decl_stmt|;
name|ProtoMessageWriter
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Path
name|filePath
parameter_list|,
name|Parser
argument_list|<
name|T
argument_list|>
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|filePath
operator|=
name|filePath
expr_stmt|;
name|this
operator|.
name|writer
operator|=
name|SequenceFile
operator|.
name|createWriter
argument_list|(
name|conf
argument_list|,
name|Writer
operator|.
name|file
argument_list|(
name|filePath
argument_list|)
argument_list|,
name|Writer
operator|.
name|keyClass
argument_list|(
name|NullWritable
operator|.
name|class
argument_list|)
argument_list|,
name|Writer
operator|.
name|valueClass
argument_list|(
name|ProtoMessageWritable
operator|.
name|class
argument_list|)
argument_list|,
name|Writer
operator|.
name|compression
argument_list|(
name|CompressionType
operator|.
name|RECORD
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|writable
operator|=
operator|new
name|ProtoMessageWritable
argument_list|<>
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Path
name|getPath
parameter_list|()
block|{
return|return
name|filePath
return|;
block|}
specifier|public
name|long
name|getOffset
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|writer
operator|.
name|getLength
argument_list|()
return|;
block|}
specifier|public
name|void
name|writeProto
parameter_list|(
name|T
name|message
parameter_list|)
throws|throws
name|IOException
block|{
name|writable
operator|.
name|setMessage
argument_list|(
name|message
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|NullWritable
operator|.
name|get
argument_list|()
argument_list|,
name|writable
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|hflush
parameter_list|()
throws|throws
name|IOException
block|{
name|writer
operator|.
name|hflush
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

