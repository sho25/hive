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
name|lib
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Stack
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|ql
operator|.
name|parse
operator|.
name|SemanticException
import|;
end_import

begin_comment
comment|/**  * Rule interface for Nodes Used in Node dispatching to dispatch process/visitor  * functions for Nodes.  */
end_comment

begin_class
specifier|public
class|class
name|RuleRegExp
implements|implements
name|Rule
block|{
specifier|private
specifier|final
name|String
name|ruleName
decl_stmt|;
specifier|private
specifier|final
name|Pattern
name|patternWithWildCardChar
decl_stmt|;
specifier|private
specifier|final
name|String
name|patternWithoutWildCardChar
decl_stmt|;
specifier|private
name|String
index|[]
name|patternORWildChar
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|Character
argument_list|>
name|wildCards
init|=
operator|new
name|HashSet
argument_list|<
name|Character
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|'['
argument_list|,
literal|'^'
argument_list|,
literal|'$'
argument_list|,
literal|'*'
argument_list|,
literal|']'
argument_list|,
literal|'+'
argument_list|,
literal|'|'
argument_list|,
literal|'('
argument_list|,
literal|'\\'
argument_list|,
literal|'.'
argument_list|,
literal|'?'
argument_list|,
literal|')'
argument_list|,
literal|'&'
argument_list|)
argument_list|)
decl_stmt|;
comment|/**    * The function iterates through the list of wild card characters and sees if    * this regular expression contains a wild card character.    *    * @param pattern    *          pattern expressed as a regular Expression    */
specifier|private
specifier|static
name|boolean
name|patternHasWildCardChar
parameter_list|(
name|String
name|pattern
parameter_list|)
block|{
if|if
condition|(
name|pattern
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|char
name|pc
range|:
name|pattern
operator|.
name|toCharArray
argument_list|()
control|)
block|{
if|if
condition|(
name|wildCards
operator|.
name|contains
argument_list|(
name|pc
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * The function iterates through the list of wild card characters and sees if    * this regular expression contains  only the given char as wild card character.    *    * @param pattern    *          pattern expressed as a regular Expression    * @param wcc    *          wild card character    */
specifier|private
specifier|static
name|boolean
name|patternHasOnlyWildCardChar
parameter_list|(
name|String
name|pattern
parameter_list|,
name|char
name|wcc
parameter_list|)
block|{
if|if
condition|(
name|pattern
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|boolean
name|ret
init|=
literal|true
decl_stmt|;
name|boolean
name|hasWildCard
init|=
literal|false
decl_stmt|;
for|for
control|(
name|char
name|pc
range|:
name|pattern
operator|.
name|toCharArray
argument_list|()
control|)
block|{
if|if
condition|(
name|wildCards
operator|.
name|contains
argument_list|(
name|pc
argument_list|)
condition|)
block|{
name|hasWildCard
operator|=
literal|true
expr_stmt|;
name|ret
operator|=
name|ret
operator|&&
operator|(
name|pc
operator|==
name|wcc
operator|)
expr_stmt|;
if|if
condition|(
operator|!
name|ret
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
return|return
name|ret
operator|&&
name|hasWildCard
return|;
block|}
comment|/**    * The rule specified by the regular expression. Note that, the regular    * expression is specified in terms of Node name. For eg: TS.*RS -> means    * TableScan Node followed by anything any number of times followed by    * ReduceSink    *     * @param ruleName    *          name of the rule    * @param regExp    *          regular expression for the rule    **/
specifier|public
name|RuleRegExp
parameter_list|(
name|String
name|ruleName
parameter_list|,
name|String
name|regExp
parameter_list|)
block|{
name|this
operator|.
name|ruleName
operator|=
name|ruleName
expr_stmt|;
if|if
condition|(
name|patternHasWildCardChar
argument_list|(
name|regExp
argument_list|)
condition|)
block|{
if|if
condition|(
name|patternHasOnlyWildCardChar
argument_list|(
name|regExp
argument_list|,
literal|'|'
argument_list|)
condition|)
block|{
name|this
operator|.
name|patternWithWildCardChar
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|patternWithoutWildCardChar
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|patternORWildChar
operator|=
name|regExp
operator|.
name|split
argument_list|(
literal|"\\|"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|patternWithWildCardChar
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|regExp
argument_list|)
expr_stmt|;
name|this
operator|.
name|patternWithoutWildCardChar
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|patternORWildChar
operator|=
literal|null
expr_stmt|;
block|}
block|}
else|else
block|{
name|this
operator|.
name|patternWithWildCardChar
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|patternWithoutWildCardChar
operator|=
name|regExp
expr_stmt|;
name|this
operator|.
name|patternORWildChar
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**    * This function returns the cost of the rule for the specified stack when the pattern    * matched for has no wildcard character in it. The function expects patternWithoutWildCardChar    * to be not null.    * @param stack    *          Node stack encountered so far    * @return cost of the function    * @throws SemanticException    */
specifier|private
name|int
name|costPatternWithoutWildCardChar
parameter_list|(
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|)
throws|throws
name|SemanticException
block|{
name|int
name|numElems
init|=
operator|(
name|stack
operator|!=
literal|null
condition|?
name|stack
operator|.
name|size
argument_list|()
else|:
literal|0
operator|)
decl_stmt|;
comment|// No elements
if|if
condition|(
name|numElems
operator|==
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|int
name|patLen
init|=
name|patternWithoutWildCardChar
operator|.
name|length
argument_list|()
decl_stmt|;
name|StringBuilder
name|name
init|=
operator|new
name|StringBuilder
argument_list|(
name|patLen
operator|+
name|numElems
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|pos
init|=
name|numElems
operator|-
literal|1
init|;
name|pos
operator|>=
literal|0
condition|;
name|pos
operator|--
control|)
block|{
name|String
name|nodeName
init|=
name|stack
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getName
argument_list|()
operator|+
literal|"%"
decl_stmt|;
name|name
operator|.
name|insert
argument_list|(
literal|0
argument_list|,
name|nodeName
argument_list|)
expr_stmt|;
if|if
condition|(
name|name
operator|.
name|length
argument_list|()
operator|>=
name|patLen
condition|)
block|{
if|if
condition|(
name|patternWithoutWildCardChar
operator|.
name|contentEquals
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|patLen
return|;
block|}
break|break;
block|}
block|}
return|return
operator|-
literal|1
return|;
block|}
comment|/**    * This function returns the cost of the rule for the specified stack when the pattern    * matched for has only OR wildcard character in it. The function expects patternORWildChar    * to be not null.    * @param stack    *          Node stack encountered so far    * @return cost of the function    * @throws SemanticException    */
specifier|private
name|int
name|costPatternWithORWildCardChar
parameter_list|(
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|)
throws|throws
name|SemanticException
block|{
name|int
name|numElems
init|=
operator|(
name|stack
operator|!=
literal|null
condition|?
name|stack
operator|.
name|size
argument_list|()
else|:
literal|0
operator|)
decl_stmt|;
comment|// No elements
if|if
condition|(
name|numElems
operator|==
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
comment|// These DS are used to cache previously created String
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|cachedNames
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|maxDepth
init|=
name|numElems
decl_stmt|;
name|int
name|maxLength
init|=
literal|0
decl_stmt|;
comment|// For every pattern
for|for
control|(
name|String
name|pattern
range|:
name|patternORWildChar
control|)
block|{
name|int
name|patLen
init|=
name|pattern
operator|.
name|length
argument_list|()
decl_stmt|;
comment|// If the stack has been explored already till that level,
comment|// obtained cached String
if|if
condition|(
name|cachedNames
operator|.
name|containsKey
argument_list|(
name|patLen
argument_list|)
condition|)
block|{
if|if
condition|(
name|pattern
operator|.
name|contentEquals
argument_list|(
name|cachedNames
operator|.
name|get
argument_list|(
name|patLen
argument_list|)
argument_list|)
condition|)
block|{
return|return
name|patLen
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|maxLength
operator|>=
name|patLen
condition|)
block|{
comment|// We have already explored the stack deep enough, but
comment|// we do not have a matching
continue|continue;
block|}
else|else
block|{
comment|// We are going to build the name
name|StringBuilder
name|name
init|=
operator|new
name|StringBuilder
argument_list|(
name|patLen
operator|+
name|numElems
argument_list|)
decl_stmt|;
if|if
condition|(
name|maxLength
operator|!=
literal|0
condition|)
block|{
name|name
operator|.
name|append
argument_list|(
name|cachedNames
operator|.
name|get
argument_list|(
name|maxLength
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|pos
init|=
name|maxDepth
operator|-
literal|1
init|;
name|pos
operator|>=
literal|0
condition|;
name|pos
operator|--
control|)
block|{
name|String
name|nodeName
init|=
name|stack
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getName
argument_list|()
operator|+
literal|"%"
decl_stmt|;
name|name
operator|.
name|insert
argument_list|(
literal|0
argument_list|,
name|nodeName
argument_list|)
expr_stmt|;
comment|// We cache the values
name|cachedNames
operator|.
name|put
argument_list|(
name|name
operator|.
name|length
argument_list|()
argument_list|,
name|name
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|maxLength
operator|=
name|name
operator|.
name|length
argument_list|()
expr_stmt|;
name|maxDepth
operator|--
expr_stmt|;
if|if
condition|(
name|name
operator|.
name|length
argument_list|()
operator|>=
name|patLen
condition|)
block|{
if|if
condition|(
name|pattern
operator|.
name|contentEquals
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|patLen
return|;
block|}
break|break;
block|}
block|}
block|}
block|}
return|return
operator|-
literal|1
return|;
block|}
comment|/**    * This function returns the cost of the rule for the specified stack when the pattern    * matched for has wildcard character in it. The function expects patternWithWildCardChar    * to be not null.    *    * @param stack    *          Node stack encountered so far    * @return cost of the function    * @throws SemanticException    */
specifier|private
name|int
name|costPatternWithWildCardChar
parameter_list|(
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|)
throws|throws
name|SemanticException
block|{
name|int
name|numElems
init|=
operator|(
name|stack
operator|!=
literal|null
condition|?
name|stack
operator|.
name|size
argument_list|()
else|:
literal|0
operator|)
decl_stmt|;
name|StringBuilder
name|name
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|Matcher
name|m
init|=
name|patternWithWildCardChar
operator|.
name|matcher
argument_list|(
literal|""
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|pos
init|=
name|numElems
operator|-
literal|1
init|;
name|pos
operator|>=
literal|0
condition|;
name|pos
operator|--
control|)
block|{
name|String
name|nodeName
init|=
name|stack
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getName
argument_list|()
operator|+
literal|"%"
decl_stmt|;
name|name
operator|.
name|insert
argument_list|(
literal|0
argument_list|,
name|nodeName
argument_list|)
expr_stmt|;
name|m
operator|.
name|reset
argument_list|(
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
name|name
operator|.
name|length
argument_list|()
return|;
block|}
block|}
return|return
operator|-
literal|1
return|;
block|}
comment|/**    * Returns true if the rule pattern is valid and has wild character in it.    */
name|boolean
name|rulePatternIsValidWithWildCardChar
parameter_list|()
block|{
return|return
name|patternWithoutWildCardChar
operator|==
literal|null
operator|&&
name|patternWithWildCardChar
operator|!=
literal|null
operator|&&
name|this
operator|.
name|patternORWildChar
operator|==
literal|null
return|;
block|}
comment|/**    * Returns true if the rule pattern is valid and has wild character in it.    */
name|boolean
name|rulePatternIsValidWithoutWildCardChar
parameter_list|()
block|{
return|return
name|patternWithWildCardChar
operator|==
literal|null
operator|&&
name|patternWithoutWildCardChar
operator|!=
literal|null
operator|&&
name|this
operator|.
name|patternORWildChar
operator|==
literal|null
return|;
block|}
comment|/**    * Returns true if the rule pattern is valid and has wild character in it.    */
name|boolean
name|rulePatternIsValidWithORWildCardChar
parameter_list|()
block|{
return|return
name|patternWithoutWildCardChar
operator|==
literal|null
operator|&&
name|patternWithWildCardChar
operator|==
literal|null
operator|&&
name|this
operator|.
name|patternORWildChar
operator|!=
literal|null
return|;
block|}
comment|/**    * This function returns the cost of the rule for the specified stack. Lower    * the cost, the better the rule is matched    *    * @param stack    *          Node stack encountered so far    * @return cost of the function    * @throws SemanticException    */
annotation|@
name|Override
specifier|public
name|int
name|cost
parameter_list|(
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|rulePatternIsValidWithoutWildCardChar
argument_list|()
condition|)
block|{
return|return
name|costPatternWithoutWildCardChar
argument_list|(
name|stack
argument_list|)
return|;
block|}
if|if
condition|(
name|rulePatternIsValidWithWildCardChar
argument_list|()
condition|)
block|{
return|return
name|costPatternWithWildCardChar
argument_list|(
name|stack
argument_list|)
return|;
block|}
if|if
condition|(
name|rulePatternIsValidWithORWildCardChar
argument_list|()
condition|)
block|{
return|return
name|costPatternWithORWildCardChar
argument_list|(
name|stack
argument_list|)
return|;
block|}
comment|// If we reached here, either :
comment|// 1. patternWithWildCardChar and patternWithoutWildCardChar are both nulls.
comment|// 2. patternWithWildCardChar and patternWithoutWildCardChar are both not nulls.
comment|// This is an internal error and we should not let this happen, so throw an exception.
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Rule pattern is invalid for "
operator|+
name|getName
argument_list|()
operator|+
literal|" : patternWithWildCardChar = "
operator|+
name|patternWithWildCardChar
operator|+
literal|" patternWithoutWildCardChar = "
operator|+
name|patternWithoutWildCardChar
argument_list|)
throw|;
block|}
comment|/**    * @return the name of the Node    **/
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|ruleName
return|;
block|}
block|}
end_class

end_unit

