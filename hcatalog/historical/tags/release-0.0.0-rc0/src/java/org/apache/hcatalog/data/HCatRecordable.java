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
name|hcatalog
operator|.
name|data
package|;
end_package

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
name|io
operator|.
name|WritableComparable
import|;
end_import

begin_comment
comment|/**  * Interface that determines whether we can implement a HowlRecord on top of it  */
end_comment

begin_interface
specifier|public
interface|interface
name|HCatRecordable
extends|extends
name|WritableComparable
argument_list|<
name|Object
argument_list|>
block|{
comment|/**    * Gets the field at the specified index.    * @param fieldNum the field number    * @return the object at the specified index    */
name|Object
name|get
parameter_list|(
name|int
name|fieldNum
parameter_list|)
function_decl|;
comment|/**    * Gets all the fields of the howl record.    * @return the list of fields    */
name|List
argument_list|<
name|Object
argument_list|>
name|getAll
parameter_list|()
function_decl|;
comment|/**    * Sets the field at the specified index.    * @param fieldNum the field number    * @param value the value to set    */
name|void
name|set
parameter_list|(
name|int
name|fieldNum
parameter_list|,
name|Object
name|value
parameter_list|)
function_decl|;
comment|/**    * Gets the size of the howl record.    * @return the size    */
name|int
name|size
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

