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
name|api
operator|.
name|cache
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|LlapMemoryBuffer
block|{
specifier|protected
name|LlapMemoryBuffer
parameter_list|()
block|{   }
specifier|protected
name|void
name|initialize
parameter_list|(
name|ByteBuffer
name|byteBuffer
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|byteBuffer
operator|=
name|byteBuffer
operator|.
name|slice
argument_list|()
expr_stmt|;
name|this
operator|.
name|byteBuffer
operator|.
name|position
argument_list|(
name|offset
argument_list|)
expr_stmt|;
name|this
operator|.
name|byteBuffer
operator|.
name|limit
argument_list|(
name|offset
operator|+
name|length
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ByteBuffer
name|byteBuffer
decl_stmt|;
block|}
end_class

end_unit

