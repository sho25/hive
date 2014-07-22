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

begin_comment
comment|/**  *<strong>Please see the deprecation notice</strong>  *<p/>  * Base class for all User-defined Aggregation Function (UDAF) classes.  *<p/>  * Requirements for a UDAF class:  *<ol>  *<li>Implement the {@code init()} method, which resets the status of the aggregation function.</li>  *<li>Implement a single method called {@code aggregate} that returns {@code boolean}.  *     The method should always return {@code true} on valid inputs, or the framework will throw an Exception.  *     Following are some examples:  *<ul>  *<li>{@code public boolean aggregate(double a);}</li>  *<li>{@code public boolean aggregate(int b);}</li>  *<li>{@code public boolean aggregate(double c, double d);}</li>  *</ul>  *</li>  *<li>Implement a single method called {@code evaluate} that returns the FINAL aggregation result.  *     {@code evaluate} should never return {@code null} or an Exception will be thrown.  *     Following are some examples:  *<ul>  *<li>{@code public int evaluate();}</li>  *<li>{@code public long evaluate();}</li>  *<li>{@code public double evaluate();}</li>  *<li>{@code public Double evaluate();}</li>  *<li>{@code public String evaluate();}</li>  *</ul>  *</li>  *</ol>  *  * Optional for a UDAF class (by implementing these two methods, the user declares  * that the UDAF supports partial aggregations):  *<ol>  *<li>Implement a single method called {@code evaluatePartial} that returns the PARTIAL aggregation result.  *     {@code evaluatePartial} should never return {@code null} or an Exception will be thrown.</li>  *<li>Implement a single method called {@code aggregatePartial} that takes a PARTIAL  *     aggregation result and returns a boolean. The method should always return  *     {@code true} on valid inputs, or the framework will throw an Exception.</li>  *</ol>  *<p/>  * Following are some examples:  *<ul>  *<li>public int evaluatePartial();</li>  *<li>public boolean aggregatePartial(int partial);</li>  *<li>public String evaluatePartial();</li>  *<li>public boolean aggregatePartial(String partial);</li>  *</ul>  *<p/>  *  * @deprecated Either implement {@link org.apache.hadoop.hive.ql.udf.generic.GenericUDAFResolver2} or extend  * {@link org.apache.hadoop.hive.ql.udf.generic.AbstractGenericUDAFResolver} instead.  */
end_comment

begin_class
annotation|@
name|Deprecated
specifier|public
class|class
name|UDAF
block|{
comment|/**    * The resolver used for method resolution.    */
name|UDAFEvaluatorResolver
name|rslv
decl_stmt|;
comment|/**    * The default constructor.    */
specifier|public
name|UDAF
parameter_list|()
block|{
name|rslv
operator|=
operator|new
name|DefaultUDAFEvaluatorResolver
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * The constructor with a particular type of resolver.    */
specifier|public
name|UDAF
parameter_list|(
name|UDAFEvaluatorResolver
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
name|UDAFEvaluatorResolver
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
comment|/**    * Gets the resolver.    */
specifier|public
name|UDAFEvaluatorResolver
name|getResolver
parameter_list|()
block|{
return|return
name|rslv
return|;
block|}
block|}
end_class

end_unit

