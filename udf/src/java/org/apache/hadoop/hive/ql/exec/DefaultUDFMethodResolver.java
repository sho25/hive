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
comment|/**  * The default UDF Method resolver. This resolver is used for resolving the UDF  * method that is to be used for evaluation given the list of the argument  * types. The getEvalMethod goes through all the evaluate methods and returns  * the one that matches the argument signature or is the closest match. Closest  * match is defined as the one that requires the least number of arguments to be  * converted. In case more than one matches are found, the method throws an  * ambiguous method exception.  */
end_comment

begin_class
specifier|public
class|class
name|DefaultUDFMethodResolver
implements|implements
name|UDFMethodResolver
block|{
comment|/**    * The class of the UDF.    */
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|UDF
argument_list|>
name|udfClass
decl_stmt|;
comment|/**    * Constructor. This constructor sets the resolver to be used for comparison    * operators. See {@link UDFMethodResolver}    */
specifier|public
name|DefaultUDFMethodResolver
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|UDF
argument_list|>
name|udfClass
parameter_list|)
block|{
name|this
operator|.
name|udfClass
operator|=
name|udfClass
expr_stmt|;
block|}
comment|/**    * Gets the evaluate method for the UDF given the parameter types.    *     * @param argClasses    *          The list of the argument types that need to matched with the    *          evaluate function signature.    */
annotation|@
name|Override
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
name|UDFArgumentException
block|{
return|return
name|MethodUtils
operator|.
name|getMethodInternal
argument_list|(
name|udfClass
argument_list|,
literal|"evaluate"
argument_list|,
literal|false
argument_list|,
name|argClasses
argument_list|)
return|;
block|}
block|}
end_class

end_unit

