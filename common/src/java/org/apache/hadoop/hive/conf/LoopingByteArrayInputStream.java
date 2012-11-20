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
name|conf
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
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

begin_comment
comment|/**  * LoopingByteArrayInputStream.  *  * This was designed specifically to handle the problem in Hadoop's Configuration object that it  * tries to read the entire contents of the same InputStream repeatedly without resetting it.  *  * The Configuration object does attempt to close the InputStream though, so, since close does  * nothing for the ByteArrayInputStream object, override it to reset it.  */
end_comment

begin_class
specifier|public
class|class
name|LoopingByteArrayInputStream
extends|extends
name|ByteArrayInputStream
block|{
specifier|public
name|LoopingByteArrayInputStream
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|)
block|{
name|super
argument_list|(
name|buf
argument_list|)
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
name|this
operator|.
name|reset
argument_list|()
expr_stmt|;
comment|// According to the Java documentation this does nothing, but just in case
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

