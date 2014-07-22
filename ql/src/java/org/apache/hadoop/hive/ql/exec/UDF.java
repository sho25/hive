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
comment|/**  * A User-defined function (UDF) for use with Hive.  *<p>  * New UDF classes need to inherit from this UDF class (or from {@link  * org.apache.hadoop.hive.ql.udf.generic.GenericUDF GenericUDF} which provides more flexibility at  * the cost of more complexity).  *<p>  * Requirements for all classes extending this UDF are:  *<ul>  *<li>Implement one or more methods named {@code evaluate} which will be called by Hive (the exact  * way in which Hive resolves the method to call can be configured by setting a custom {@link  * UDFMethodResolver}). The following are some examples:  *<ul>  *<li>{@code public int evaluate();}</li>  *<li>{@code public int evaluate(int a);}</li>  *<li>{@code public double evaluate(int a, double b);}</li>  *<li>{@code public String evaluate(String a, int b, Text c);}</li>  *<li>{@code public Text evaluate(String a);}</li>  *<li>{@code public String evaluate(List<Integer> a);} (Note that Hive Arrays are represented as  * {@link java.util.List Lists} in Hive.  * So an {@code ARRAY<int>} column would be passed in as a {@code List<Integer>}.)</li>  *</ul>  *</li>  *<li>{@code evaluate} should never be a void method. However it can return {@code null} if  * needed.  *<li>Return types as well as method arguments can be either Java primitives or the corresponding  * {@link org.apache.hadoop.io.Writable Writable} class.</li>  *</ul>  * One instance of this class will be instantiated per JVM and it will not be called concurrently.  *  * @see Description  * @see UDFType  */
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
comment|/**    * The constructor with user-provided {@link UDFMethodResolver}.    */
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
comment|/**    * Sets the resolver.    *    * @param rslv The method resolver to use for method resolution.    */
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
comment|/**    * This can be overridden to include JARs required by this UDF.    *    * @see org.apache.hadoop.hive.ql.udf.generic.GenericUDF#getRequiredJars()    *      GenericUDF.getRequiredJars()    *    * @return an array of paths to files to include, {@code null} by default.    */
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
comment|/**    * This can be overridden to include files required by this UDF.    *    * @see org.apache.hadoop.hive.ql.udf.generic.GenericUDF#getRequiredFiles()    *      GenericUDF.getRequiredFiles()    *    * @return an array of paths to files to include, {@code null} by default.    */
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

