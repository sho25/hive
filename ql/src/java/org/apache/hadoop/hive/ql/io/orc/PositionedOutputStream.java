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
name|ql
operator|.
name|io
operator|.
name|orc
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
name|io
operator|.
name|OutputStream
import|;
end_import

begin_class
specifier|abstract
class|class
name|PositionedOutputStream
extends|extends
name|OutputStream
block|{
comment|/**    * Record the current position to the recorder.    * @param recorder the object that receives the position    * @throws IOException    */
specifier|abstract
name|void
name|getPosition
parameter_list|(
name|PositionRecorder
name|recorder
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the memory size currently allocated as buffer associated with this    * stream.    * @return the number of bytes used by buffers.    */
specifier|abstract
name|long
name|getBufferSize
parameter_list|()
function_decl|;
block|}
end_class

end_unit

