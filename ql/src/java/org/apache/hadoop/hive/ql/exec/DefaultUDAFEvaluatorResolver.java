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
name|ArrayList
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
comment|/**  * The default UDAF Method resolver. This resolver is used for resolving the  * UDAF methods are used for partial and final evaluation given the list of the  * argument types. The getEvalMethod goes through all the evaluate methods and  * returns the one that matches the argument signature or is the closest match.  * Closest match is defined as the one that requires the least number of  * arguments to be converted. In case more than one matches are found, the  * method throws an ambiguous method exception.  */
end_comment

begin_class
specifier|public
class|class
name|DefaultUDAFEvaluatorResolver
implements|implements
name|UDAFEvaluatorResolver
block|{
comment|/**    * The class of the UDAF.    */
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|UDAF
argument_list|>
name|udafClass
decl_stmt|;
comment|/**    * Constructor. This constructor sets the resolver to be used for comparison    * operators. See {@link UDAFEvaluatorResolver}    */
specifier|public
name|DefaultUDAFEvaluatorResolver
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|UDAF
argument_list|>
name|udafClass
parameter_list|)
block|{
name|this
operator|.
name|udafClass
operator|=
name|udafClass
expr_stmt|;
block|}
comment|/**    * Gets the evaluator class for the UDAF given the parameter types.    *     * @param argClasses    *          The list of the parameter types.    */
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|UDAFEvaluator
argument_list|>
name|getEvaluatorClass
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
name|ArrayList
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|UDAFEvaluator
argument_list|>
argument_list|>
name|classList
init|=
operator|new
name|ArrayList
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|UDAFEvaluator
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// Add all the public member classes that implement an evaluator
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|enclClass
range|:
name|udafClass
operator|.
name|getClasses
argument_list|()
control|)
block|{
if|if
condition|(
name|UDAFEvaluator
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|enclClass
argument_list|)
condition|)
block|{
name|classList
operator|.
name|add
argument_list|(
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|UDAFEvaluator
argument_list|>
operator|)
name|enclClass
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Next we locate all the iterate methods for each of these classes.
name|ArrayList
argument_list|<
name|Method
argument_list|>
name|mList
init|=
operator|new
name|ArrayList
argument_list|<
name|Method
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|UDAFEvaluator
argument_list|>
argument_list|>
name|cList
init|=
operator|new
name|ArrayList
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|UDAFEvaluator
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|UDAFEvaluator
argument_list|>
name|evaluator
range|:
name|classList
control|)
block|{
for|for
control|(
name|Method
name|m
range|:
name|evaluator
operator|.
name|getMethods
argument_list|()
control|)
block|{
if|if
condition|(
name|m
operator|.
name|getName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"iterate"
argument_list|)
condition|)
block|{
name|mList
operator|.
name|add
argument_list|(
name|m
argument_list|)
expr_stmt|;
name|cList
operator|.
name|add
argument_list|(
name|evaluator
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|Method
name|m
init|=
name|FunctionRegistry
operator|.
name|getMethodInternal
argument_list|(
name|udafClass
argument_list|,
name|mList
argument_list|,
literal|false
argument_list|,
name|argClasses
argument_list|)
decl_stmt|;
comment|// Find the class that has this method.
comment|// Note that Method.getDeclaringClass() may not work here because the method
comment|// can be inherited from a base class.
name|int
name|found
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|mList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|mList
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|==
name|m
condition|)
block|{
if|if
condition|(
name|found
operator|==
operator|-
literal|1
condition|)
block|{
name|found
operator|=
name|i
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|AmbiguousMethodException
argument_list|(
name|udafClass
argument_list|,
name|argClasses
argument_list|,
name|mList
argument_list|)
throw|;
block|}
block|}
block|}
assert|assert
operator|(
name|found
operator|!=
operator|-
literal|1
operator|)
assert|;
return|return
name|cList
operator|.
name|get
argument_list|(
name|found
argument_list|)
return|;
block|}
block|}
end_class

end_unit

