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
name|serde2
operator|.
name|lazy
package|;
end_package

begin_comment
comment|/**  * ByteArrayRef stores a reference to a byte array.  *   * The LazyObject hierarchy uses a reference to a single ByteArrayRef, so that  * it's much faster to switch to the next row and release the reference to the  * old row (so that the system can do garbage collection if needed).  */
end_comment

begin_class
specifier|public
class|class
name|ByteArrayRef
block|{
comment|/**    * Stores the actual data.    */
name|byte
index|[]
name|data
decl_stmt|;
specifier|public
name|byte
index|[]
name|getData
parameter_list|()
block|{
return|return
name|data
return|;
block|}
specifier|public
name|void
name|setData
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
block|{
name|this
operator|.
name|data
operator|=
name|data
expr_stmt|;
block|}
block|}
end_class

end_unit

