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
operator|.
name|UDFType
import|;
end_import

begin_comment
comment|/**  * A User-defined function (UDF) for the use with Hive.  *  * New UDF classes need to inherit from this UDF class.  *  * Required for all UDF classes: 1. Implement one or more methods named  * "evaluate" which will be called by Hive. The following are some examples:  * public int evaluate(); public int evaluate(int a); public double evaluate(int  * a, double b); public String evaluate(String a, int b, String c);  *  * "evaluate" should never be a void method. However it can return "null" if  * needed.  */
end_comment

begin_class
annotation|@
name|UDFType
argument_list|(
name|deterministic
operator|=
literal|true
argument_list|)
specifier|public
class|class
name|UDF
block|{
comment|/**    * The resolver to use for method resolution.    */
specifier|private
name|UDFMethodResolver
name|rslv
decl_stmt|;
comment|/**    * The constructor.    */
specifier|public
name|UDF
parameter_list|()
block|{
name|rslv
operator|=
operator|new
name|DefaultUDFMethodResolver
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * The constructor with user-provided UDFMethodResolver.    */
specifier|protected
name|UDF
parameter_list|(
name|UDFMethodResolver
name|rslv
parameter_list|)
block|{
name|this
operator|.
name|rslv
operator|=
name|rslv
expr_stmt|;
block|}
comment|/**    * Sets the resolver.    *    * @param rslv    *          The method resolver to use for method resolution.    */
specifier|public
name|void
name|setResolver
parameter_list|(
name|UDFMethodResolver
name|rslv
parameter_list|)
block|{
name|this
operator|.
name|rslv
operator|=
name|rslv
expr_stmt|;
block|}
comment|/**    * Get the method resolver.    */
specifier|public
name|UDFMethodResolver
name|getResolver
parameter_list|()
block|{
return|return
name|rslv
return|;
block|}
comment|/**    * These can be overriden to provide the same functionality as the    * correspondingly named methods in GenericUDF.    */
specifier|public
name|String
index|[]
name|getRequiredJars
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
specifier|public
name|String
index|[]
name|getRequiredFiles
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

