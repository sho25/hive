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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

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
comment|/**  * The UDF Method resolver interface. A user can plugin a resolver to their UDF by implementing the  * functions in this interface. Note that the resolver is stored in the UDF class as an instance  * variable. We did not use a static variable because many resolvers maintain the class of the   * enclosing UDF as state and are called from a base class e.g. UDFBaseCompare. This makes it very  * easy to write UDFs that want to do resolution similar to the comparison operators. Such UDFs  * just need to extend UDFBaseCompare and do not have to care about the UDFMethodResolver interface.  * Same is true for UDFs that want to do resolution similar to that done by the numeric operators.  * Such UDFs simply have to extend UDFBaseNumericOp class. For the default resolution the UDF  * implementation simply needs to extend the UDF class.  */
end_comment

begin_interface
specifier|public
interface|interface
name|UDFMethodResolver
block|{
comment|/**    * Gets the evaluate method for the UDF given the parameter types.    *     * @param argClasses The list of the argument types that need to matched with the evaluate    *                   function signature.    */
specifier|public
name|Method
name|getEvalMethod
parameter_list|(
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|argClasses
parameter_list|)
throws|throws
name|AmbiguousMethodException
throws|,
name|UDFArgumentException
function_decl|;
block|}
end_interface

end_unit

