begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|wm
package|;
end_package

begin_comment
comment|/**  * Expression that is defined in triggers.  * Most expressions will get triggered only after exceeding a limit. As a result, only greater than (&gt;) expression  * is supported.  */
end_comment

begin_interface
specifier|public
interface|interface
name|Expression
block|{
enum|enum
name|Predicate
block|{
name|GREATER_THAN
argument_list|(
literal|">"
argument_list|)
block|;
name|String
name|symbol
decl_stmt|;
name|Predicate
parameter_list|(
specifier|final
name|String
name|symbol
parameter_list|)
block|{
name|this
operator|.
name|symbol
operator|=
name|symbol
expr_stmt|;
block|}
specifier|public
name|String
name|getSymbol
parameter_list|()
block|{
return|return
name|symbol
return|;
block|}
block|}
interface|interface
name|Builder
block|{
name|Builder
name|greaterThan
parameter_list|(
name|CounterLimit
name|counter
parameter_list|)
function_decl|;
name|Expression
name|build
parameter_list|()
function_decl|;
block|}
comment|/**    * Evaluate current value against this expression. Return true if expression evaluates to true (current&gt; limit)    * else false otherwise    *    * @param current - current value against which expression will be evaluated    * @return true if current value exceeds limit    */
name|boolean
name|evaluate
parameter_list|(
specifier|final
name|long
name|current
parameter_list|)
function_decl|;
comment|/**    * Return counter limit    *    * @return counter limit    */
name|CounterLimit
name|getCounterLimit
parameter_list|()
function_decl|;
comment|/**    * Return predicate defined in the expression.    *    * @return predicate    */
name|Predicate
name|getPredicate
parameter_list|()
function_decl|;
comment|/**    * Return cloned copy of this expression.    *    * @return cloned copy    */
name|Expression
name|clone
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

