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
comment|/**  * Exception thrown by the UDF and UDAF method resolvers in case a unique method is not found.  *  */
end_comment

begin_class
specifier|public
class|class
name|AmbiguousMethodException
extends|extends
name|UDFArgumentException
block|{
comment|/**    *     */
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|/**    * The UDF or UDAF class that has the ambiguity.    */
name|Class
argument_list|<
name|?
argument_list|>
name|funcClass
decl_stmt|;
comment|/**    * The list of parameter types.    */
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|argTypeInfos
decl_stmt|;
comment|/**    * Constructor.    *     * @param funcClass The UDF or UDAF class.    * @param argTypeInfos The list of argument types that lead to an ambiguity.    */
specifier|public
name|AmbiguousMethodException
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|funcClass
parameter_list|,
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|argTypeInfos
parameter_list|)
block|{
name|super
argument_list|(
literal|"Ambiguous method for "
operator|+
name|funcClass
operator|+
literal|" with "
operator|+
name|argTypeInfos
argument_list|)
expr_stmt|;
name|this
operator|.
name|funcClass
operator|=
name|funcClass
expr_stmt|;
name|this
operator|.
name|argTypeInfos
operator|=
name|argTypeInfos
expr_stmt|;
block|}
name|Class
argument_list|<
name|?
argument_list|>
name|getFunctionClass
parameter_list|()
block|{
return|return
name|funcClass
return|;
block|}
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|getArgTypeList
parameter_list|()
block|{
return|return
name|argTypeInfos
return|;
block|}
block|}
end_class

end_unit

