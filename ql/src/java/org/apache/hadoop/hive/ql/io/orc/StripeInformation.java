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

begin_comment
comment|/**  * Information about the stripes in an ORC file that is provided by the Reader.  */
end_comment

begin_interface
specifier|public
interface|interface
name|StripeInformation
block|{
comment|/**    * Get the byte offset of the start of the stripe.    * @return the bytes from the start of the file    */
name|long
name|getOffset
parameter_list|()
function_decl|;
comment|/**    * Get the total length of the stripe in bytes.    * @return the number of bytes in the stripe    */
name|long
name|getLength
parameter_list|()
function_decl|;
comment|/**    * Get the length of the stripe's indexes.    * @return the number of bytes in the index    */
name|long
name|getIndexLength
parameter_list|()
function_decl|;
comment|/**    * Get the length of the stripe's data.    * @return the number of bytes in the stripe    */
name|long
name|getDataLength
parameter_list|()
function_decl|;
comment|/**    * Get the length of the stripe's tail section, which contains its index.    * @return the number of bytes in the tail    */
name|long
name|getFooterLength
parameter_list|()
function_decl|;
comment|/**    * Get the number of rows in the stripe.    * @return a count of the number of rows    */
name|long
name|getNumberOfRows
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

