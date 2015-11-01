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
name|exec
operator|.
name|vector
operator|.
name|keyseries
package|;
end_package

begin_comment
comment|/**  * An abstract adding serialization to key series.  *  * A key with no or some nulls has serialized bytes, offset, and length.  */
end_comment

begin_interface
specifier|public
interface|interface
name|VectorKeySeriesSerialized
extends|extends
name|VectorKeySeries
block|{
comment|/**    * @return the serialized bytes (including optional key tag), start offset of the key in the    *         bytes, and key byte length.    */
name|byte
index|[]
name|getSerializedBytes
parameter_list|()
function_decl|;
name|int
name|getSerializedStart
parameter_list|()
function_decl|;
name|int
name|getSerializedLength
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

