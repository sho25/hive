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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|io
operator|.
name|protobuf
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|hive
operator|.
name|serde2
operator|.
name|SerDeException
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
name|BytesWritable
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
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
name|Message
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

begin_comment
comment|/**  * Class to convert bytes writable containing a protobuf message to hive formats.  * @see ProtobufSerDe  */
end_comment

begin_class
specifier|public
class|class
name|ProtobufBytesWritableSerDe
extends|extends
name|ProtobufSerDe
block|{
specifier|private
name|Parser
argument_list|<
name|?
extends|extends
name|Message
argument_list|>
name|parser
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
block|{
name|super
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
try|try
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Parser
argument_list|<
name|?
extends|extends
name|Message
argument_list|>
name|tmpParser
init|=
operator|(
name|Parser
argument_list|<
name|?
extends|extends
name|Message
argument_list|>
operator|)
name|protoMessageClass
operator|.
name|getField
argument_list|(
literal|"PARSER"
argument_list|)
operator|.
name|get
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|this
operator|.
name|parser
operator|=
name|tmpParser
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
decl||
name|IllegalAccessException
decl||
name|NoSuchFieldException
decl||
name|SecurityException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Unable get PARSER from class: "
operator|+
name|protoMessageClass
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|Message
name|toMessage
parameter_list|(
name|Writable
name|writable
parameter_list|)
throws|throws
name|SerDeException
block|{
try|try
block|{
name|BytesWritable
name|bytes
init|=
operator|(
name|BytesWritable
operator|)
name|writable
decl_stmt|;
return|return
name|parser
operator|.
name|parseFrom
argument_list|(
name|bytes
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|getLength
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Unable to parse proto message"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|getSerializedClass
parameter_list|()
block|{
return|return
name|BytesWritable
operator|.
name|class
return|;
block|}
block|}
end_class

end_unit

