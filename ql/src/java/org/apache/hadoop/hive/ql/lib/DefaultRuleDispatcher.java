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
name|Map
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
comment|/**  * Dispatches calls to relevant method in processor. The user registers various  * rules with the dispatcher, and the processor corresponding to closest  * matching rule is fired.  */
end_comment

begin_class
specifier|public
class|class
name|DefaultRuleDispatcher
implements|implements
name|SemanticDispatcher
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|SemanticRule
argument_list|,
name|SemanticNodeProcessor
argument_list|>
name|procRules
decl_stmt|;
specifier|private
specifier|final
name|NodeProcessorCtx
name|procCtx
decl_stmt|;
specifier|private
specifier|final
name|SemanticNodeProcessor
name|defaultProc
decl_stmt|;
comment|/**    * Constructor.    *     * @param defaultProc    *          default processor to be fired if no rule matches    * @param rules    *          operator processor that handles actual processing of the node    * @param procCtx    *          operator processor context, which is opaque to the dispatcher    */
specifier|public
name|DefaultRuleDispatcher
parameter_list|(
name|SemanticNodeProcessor
name|defaultProc
parameter_list|,
name|Map
argument_list|<
name|SemanticRule
argument_list|,
name|SemanticNodeProcessor
argument_list|>
name|rules
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|)
block|{
name|this
operator|.
name|defaultProc
operator|=
name|defaultProc
expr_stmt|;
name|procRules
operator|=
name|rules
expr_stmt|;
name|this
operator|.
name|procCtx
operator|=
name|procCtx
expr_stmt|;
block|}
comment|/**    * Dispatcher function.    *     * @param nd    *          operator to process    * @param ndStack    *          the operators encountered so far    * @throws SemanticException    */
annotation|@
name|Override
specifier|public
name|Object
name|dispatch
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|ndStack
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// find the firing rule
comment|// find the rule from the stack specified
name|SemanticRule
name|rule
init|=
literal|null
decl_stmt|;
name|int
name|minCost
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
for|for
control|(
name|SemanticRule
name|r
range|:
name|procRules
operator|.
name|keySet
argument_list|()
control|)
block|{
name|int
name|cost
init|=
name|r
operator|.
name|cost
argument_list|(
name|ndStack
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|cost
operator|>=
literal|0
operator|)
operator|&&
operator|(
name|cost
operator|<=
name|minCost
operator|)
condition|)
block|{
name|minCost
operator|=
name|cost
expr_stmt|;
name|rule
operator|=
name|r
expr_stmt|;
block|}
block|}
name|SemanticNodeProcessor
name|proc
decl_stmt|;
if|if
condition|(
name|rule
operator|==
literal|null
condition|)
block|{
name|proc
operator|=
name|defaultProc
expr_stmt|;
block|}
else|else
block|{
name|proc
operator|=
name|procRules
operator|.
name|get
argument_list|(
name|rule
argument_list|)
expr_stmt|;
block|}
comment|// Do nothing in case proc is null
if|if
condition|(
name|proc
operator|!=
literal|null
condition|)
block|{
comment|// Call the process function
return|return
name|proc
operator|.
name|process
argument_list|(
name|nd
argument_list|,
name|ndStack
argument_list|,
name|procCtx
argument_list|,
name|nodeOutputs
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

