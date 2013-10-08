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
name|udf
package|;
end_package

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
name|ql
operator|.
name|exec
operator|.
name|UDFArgumentException
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
name|typeinfo
operator|.
name|TypeInfo
import|;
end_import

begin_comment
comment|/**  * THIS INTERFACE IS UNSTABLE AND SHOULD NOT BE USED BY 3RD PARTY UDFS.  * Interface to allow passing of parameters to the UDF, before it is initialized.  * For example, to be able to pass the char length parameters to a char type cast.  */
end_comment

begin_interface
specifier|public
interface|interface
name|SettableUDF
block|{
comment|/**    * Add data to UDF prior to initialization.    * An exception may be thrown if the UDF doesn't know what to do with this data.    * @param params UDF-specific data to add to the UDF    */
name|void
name|setTypeInfo
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|)
throws|throws
name|UDFArgumentException
function_decl|;
name|TypeInfo
name|getTypeInfo
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

