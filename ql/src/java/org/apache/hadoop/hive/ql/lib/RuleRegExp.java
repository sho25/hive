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
name|lib
package|;
end_package

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
name|pattern
decl_stmt|;
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
name|pattern
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|regExp
argument_list|)
expr_stmt|;
block|}
comment|/**    * This function returns the cost of the rule for the specified stack. Lower    * the cost, the better the rule is matched    *     * @param stack    *          Node stack encountered so far    * @return cost of the function    * @throws SemanticException    */
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
name|String
name|name
init|=
literal|""
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
name|name
operator|=
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
operator|+
name|name
expr_stmt|;
name|Matcher
name|m
init|=
name|pattern
operator|.
name|matcher
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
name|m
operator|.
name|group
argument_list|()
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

