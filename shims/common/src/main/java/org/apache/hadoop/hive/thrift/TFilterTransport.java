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
name|thrift
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransportException
import|;
end_import

begin_comment
comment|/**   * Transport that simply wraps another transport.   * This is the equivalent of FilterInputStream for Thrift transports.   */
end_comment

begin_class
specifier|public
class|class
name|TFilterTransport
extends|extends
name|TTransport
block|{
specifier|protected
specifier|final
name|TTransport
name|wrapped
decl_stmt|;
specifier|public
name|TFilterTransport
parameter_list|(
name|TTransport
name|wrapped
parameter_list|)
block|{
name|this
operator|.
name|wrapped
operator|=
name|wrapped
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|open
parameter_list|()
throws|throws
name|TTransportException
block|{
name|wrapped
operator|.
name|open
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isOpen
parameter_list|()
block|{
return|return
name|wrapped
operator|.
name|isOpen
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|peek
parameter_list|()
block|{
return|return
name|wrapped
operator|.
name|peek
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|wrapped
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|TTransportException
block|{
return|return
name|wrapped
operator|.
name|read
argument_list|(
name|buf
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|readAll
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|TTransportException
block|{
return|return
name|wrapped
operator|.
name|readAll
argument_list|(
name|buf
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|)
throws|throws
name|TTransportException
block|{
name|wrapped
operator|.
name|write
argument_list|(
name|buf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|TTransportException
block|{
name|wrapped
operator|.
name|write
argument_list|(
name|buf
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|flush
parameter_list|()
throws|throws
name|TTransportException
block|{
name|wrapped
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getBuffer
parameter_list|()
block|{
return|return
name|wrapped
operator|.
name|getBuffer
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getBufferPosition
parameter_list|()
block|{
return|return
name|wrapped
operator|.
name|getBufferPosition
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getBytesRemainingInBuffer
parameter_list|()
block|{
return|return
name|wrapped
operator|.
name|getBytesRemainingInBuffer
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|consumeBuffer
parameter_list|(
name|int
name|len
parameter_list|)
block|{
name|wrapped
operator|.
name|consumeBuffer
argument_list|(
name|len
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

