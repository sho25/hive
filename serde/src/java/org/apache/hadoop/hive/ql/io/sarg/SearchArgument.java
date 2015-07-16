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
operator|.
name|sarg
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

begin_comment
comment|/**  * Primary interface for<a href="http://en.wikipedia.org/wiki/Sargable">  *   SearchArgument</a>, which are the subset of predicates  * that can be pushed down to the RecordReader. Each SearchArgument consists  * of a series of SearchClauses that must each be true for the row to be  * accepted by the filter.  *  * This requires that the filter be normalized into conjunctive normal form  * (<a href="http://en.wikipedia.org/wiki/Conjunctive_normal_form">CNF</a>).  */
end_comment

begin_interface
specifier|public
interface|interface
name|SearchArgument
block|{
comment|/**    * The potential result sets of logical operations.    */
specifier|public
specifier|static
enum|enum
name|TruthValue
block|{
name|YES
block|,
name|NO
block|,
name|NULL
block|,
name|YES_NULL
block|,
name|NO_NULL
block|,
name|YES_NO
block|,
name|YES_NO_NULL
block|;
comment|/**      * Compute logical or between the two values.      * @param right the other argument or null      * @return the result      */
specifier|public
name|TruthValue
name|or
parameter_list|(
name|TruthValue
name|right
parameter_list|)
block|{
if|if
condition|(
name|right
operator|==
literal|null
operator|||
name|right
operator|==
name|this
condition|)
block|{
return|return
name|this
return|;
block|}
if|if
condition|(
name|right
operator|==
name|YES
operator|||
name|this
operator|==
name|YES
condition|)
block|{
return|return
name|YES
return|;
block|}
if|if
condition|(
name|right
operator|==
name|YES_NULL
operator|||
name|this
operator|==
name|YES_NULL
condition|)
block|{
return|return
name|YES_NULL
return|;
block|}
if|if
condition|(
name|right
operator|==
name|NO
condition|)
block|{
return|return
name|this
return|;
block|}
if|if
condition|(
name|this
operator|==
name|NO
condition|)
block|{
return|return
name|right
return|;
block|}
if|if
condition|(
name|this
operator|==
name|NULL
condition|)
block|{
if|if
condition|(
name|right
operator|==
name|NO_NULL
condition|)
block|{
return|return
name|NULL
return|;
block|}
else|else
block|{
return|return
name|YES_NULL
return|;
block|}
block|}
if|if
condition|(
name|right
operator|==
name|NULL
condition|)
block|{
if|if
condition|(
name|this
operator|==
name|NO_NULL
condition|)
block|{
return|return
name|NULL
return|;
block|}
else|else
block|{
return|return
name|YES_NULL
return|;
block|}
block|}
return|return
name|YES_NO_NULL
return|;
block|}
comment|/**      * Compute logical AND between the two values.      * @param right the other argument or null      * @return the result      */
specifier|public
name|TruthValue
name|and
parameter_list|(
name|TruthValue
name|right
parameter_list|)
block|{
if|if
condition|(
name|right
operator|==
literal|null
operator|||
name|right
operator|==
name|this
condition|)
block|{
return|return
name|this
return|;
block|}
if|if
condition|(
name|right
operator|==
name|NO
operator|||
name|this
operator|==
name|NO
condition|)
block|{
return|return
name|NO
return|;
block|}
if|if
condition|(
name|right
operator|==
name|NO_NULL
operator|||
name|this
operator|==
name|NO_NULL
condition|)
block|{
return|return
name|NO_NULL
return|;
block|}
if|if
condition|(
name|right
operator|==
name|YES
condition|)
block|{
return|return
name|this
return|;
block|}
if|if
condition|(
name|this
operator|==
name|YES
condition|)
block|{
return|return
name|right
return|;
block|}
if|if
condition|(
name|this
operator|==
name|NULL
condition|)
block|{
if|if
condition|(
name|right
operator|==
name|YES_NULL
condition|)
block|{
return|return
name|NULL
return|;
block|}
else|else
block|{
return|return
name|NO_NULL
return|;
block|}
block|}
if|if
condition|(
name|right
operator|==
name|NULL
condition|)
block|{
if|if
condition|(
name|this
operator|==
name|YES_NULL
condition|)
block|{
return|return
name|NULL
return|;
block|}
else|else
block|{
return|return
name|NO_NULL
return|;
block|}
block|}
return|return
name|YES_NO_NULL
return|;
block|}
specifier|public
name|TruthValue
name|not
parameter_list|()
block|{
switch|switch
condition|(
name|this
condition|)
block|{
case|case
name|NO
case|:
return|return
name|YES
return|;
case|case
name|YES
case|:
return|return
name|NO
return|;
case|case
name|NULL
case|:
case|case
name|YES_NO
case|:
case|case
name|YES_NO_NULL
case|:
return|return
name|this
return|;
case|case
name|NO_NULL
case|:
return|return
name|YES_NULL
return|;
case|case
name|YES_NULL
case|:
return|return
name|NO_NULL
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown value: "
operator|+
name|this
argument_list|)
throw|;
block|}
block|}
comment|/**      * Does the RecordReader need to include this set of records?      * @return true unless none of the rows qualify      */
specifier|public
name|boolean
name|isNeeded
parameter_list|()
block|{
switch|switch
condition|(
name|this
condition|)
block|{
case|case
name|NO
case|:
case|case
name|NULL
case|:
case|case
name|NO_NULL
case|:
return|return
literal|false
return|;
default|default:
return|return
literal|true
return|;
block|}
block|}
block|}
comment|/**    * Get the leaf predicates that are required to evaluate the predicate. The    * list will have the duplicates removed.    * @return the list of leaf predicates    */
specifier|public
name|List
argument_list|<
name|PredicateLeaf
argument_list|>
name|getLeaves
parameter_list|()
function_decl|;
comment|/**    * Get the expression tree. This should only needed for file formats that    * need to translate the expression to an internal form.    */
specifier|public
name|ExpressionTree
name|getExpression
parameter_list|()
function_decl|;
comment|/**    * Evaluate the entire predicate based on the values for the leaf predicates.    * @param leaves the value of each leaf predicate    * @return the value of hte entire predicate    */
specifier|public
name|TruthValue
name|evaluate
parameter_list|(
name|TruthValue
index|[]
name|leaves
parameter_list|)
function_decl|;
comment|/**    * Serialize the SARG as a kyro object and return the base64 string.    *    * Hive should replace the current XML-based AST serialization for predicate pushdown    * with the Kryo serialization of the SARG because the representation is much more    * compact and focused on what is needed for predicate pushdown.    *    * @return the serialized SARG    */
specifier|public
name|String
name|toKryo
parameter_list|()
function_decl|;
comment|/**    * A builder object for contexts outside of Hive where it isn't easy to    * get a ExprNodeDesc. The user must call startOr, startAnd, or startNot    * before adding any leaves.    */
specifier|public
interface|interface
name|Builder
block|{
comment|/**      * Start building an or operation and push it on the stack.      * @return this      */
specifier|public
name|Builder
name|startOr
parameter_list|()
function_decl|;
comment|/**      * Start building an and operation and push it on the stack.      * @return this      */
specifier|public
name|Builder
name|startAnd
parameter_list|()
function_decl|;
comment|/**      * Start building a not operation and push it on the stack.      * @return this      */
specifier|public
name|Builder
name|startNot
parameter_list|()
function_decl|;
comment|/**      * Finish the current operation and pop it off of the stack. Each start      * call must have a matching end.      * @return this      */
specifier|public
name|Builder
name|end
parameter_list|()
function_decl|;
comment|/**      * Add a less than leaf to the current item on the stack.      * @param column the name of the column      * @param literal the literal      * @return this      */
specifier|public
name|Builder
name|lessThan
parameter_list|(
name|String
name|column
parameter_list|,
name|Object
name|literal
parameter_list|)
function_decl|;
comment|/**      * Add a less than equals leaf to the current item on the stack.      * @param column the name of the column      * @param literal the literal      * @return this      */
specifier|public
name|Builder
name|lessThanEquals
parameter_list|(
name|String
name|column
parameter_list|,
name|Object
name|literal
parameter_list|)
function_decl|;
comment|/**      * Add an equals leaf to the current item on the stack.      * @param column the name of the column      * @param literal the literal      * @return this      */
specifier|public
name|Builder
name|equals
parameter_list|(
name|String
name|column
parameter_list|,
name|Object
name|literal
parameter_list|)
function_decl|;
comment|/**      * Add a null safe equals leaf to the current item on the stack.      * @param column the name of the column      * @param literal the literal      * @return this      */
specifier|public
name|Builder
name|nullSafeEquals
parameter_list|(
name|String
name|column
parameter_list|,
name|Object
name|literal
parameter_list|)
function_decl|;
comment|/**      * Add an in leaf to the current item on the stack.      * @param column the name of the column      * @param literal the literal      * @return this      */
specifier|public
name|Builder
name|in
parameter_list|(
name|String
name|column
parameter_list|,
name|Object
modifier|...
name|literal
parameter_list|)
function_decl|;
comment|/**      * Add an is null leaf to the current item on the stack.      * @param column the name of the column      * @return this      */
specifier|public
name|Builder
name|isNull
parameter_list|(
name|String
name|column
parameter_list|)
function_decl|;
comment|/**      * Add a between leaf to the current item on the stack.      * @param column the name of the column      * @param lower the literal      * @param upper the literal      * @return this      */
specifier|public
name|Builder
name|between
parameter_list|(
name|String
name|column
parameter_list|,
name|Object
name|lower
parameter_list|,
name|Object
name|upper
parameter_list|)
function_decl|;
comment|/**      * Build and return the SearchArgument that has been defined. All of the      * starts must have been ended before this call.      * @return the new SearchArgument      */
specifier|public
name|SearchArgument
name|build
parameter_list|()
function_decl|;
block|}
block|}
end_interface

end_unit

