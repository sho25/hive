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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_comment
comment|/**  * Subclasses represent a storage format for the  * CREATE TABLE ... STORED AS ... command. Subclasses are  * found via the ServiceLoader facility.  */
end_comment

begin_interface
specifier|public
interface|interface
name|StorageFormatDescriptor
block|{
comment|/**    * Return the set of names this storage format is known as.    */
name|Set
argument_list|<
name|String
argument_list|>
name|getNames
parameter_list|()
function_decl|;
comment|/**    * Return the name of the input format as a string    */
name|String
name|getInputFormat
parameter_list|()
function_decl|;
comment|/**    * Return the name of the output format as a string    */
name|String
name|getOutputFormat
parameter_list|()
function_decl|;
comment|/**    * Return the name of the serde as a string or null    */
annotation|@
name|Nullable
name|String
name|getSerde
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

