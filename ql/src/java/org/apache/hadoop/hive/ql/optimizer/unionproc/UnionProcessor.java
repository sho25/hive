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
name|optimizer
operator|.
name|unionproc
package|;
end_package

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
name|LinkedHashMap
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
operator|.
name|DefaultRuleDispatcher
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
name|lib
operator|.
name|Dispatcher
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
name|lib
operator|.
name|GraphWalker
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
name|lib
operator|.
name|Node
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
name|lib
operator|.
name|NodeProcessor
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
name|lib
operator|.
name|PreOrderWalker
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
name|lib
operator|.
name|Rule
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
name|lib
operator|.
name|RuleRegExp
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
name|optimizer
operator|.
name|Transform
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
name|ParseContext
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
comment|/**  * Implementation of the union processor. This can be enhanced later on.  * Currently, it does the following: Identify if both the subqueries of UNION  * are map-only. Store that fact in the unionDesc/UnionOperator. If either of  * the sub-query involves a map-reduce job, a FS is introduced on top of the  * UNION. This can be later optimized to clone all the operators above the  * UNION.  *   * The parse Context is not changed.  */
end_comment

begin_class
specifier|public
class|class
name|UnionProcessor
implements|implements
name|Transform
block|{
comment|/**    * empty constructor    */
specifier|public
name|UnionProcessor
parameter_list|()
block|{   }
comment|/**    * Transform the query tree. For each union, store the fact whether both the    * sub-queries are map-only    *     * @param pCtx    *          the current parse context    */
specifier|public
name|ParseContext
name|transform
parameter_list|(
name|ParseContext
name|pCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// create a walker which walks the tree in a DFS manner while maintaining
comment|// the operator stack.
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|opRules
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
argument_list|()
decl_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
operator|new
name|String
argument_list|(
literal|"R1"
argument_list|)
argument_list|,
literal|"RS%.*UNION%"
argument_list|)
argument_list|,
name|UnionProcFactory
operator|.
name|getMapRedUnion
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
operator|new
name|String
argument_list|(
literal|"R2"
argument_list|)
argument_list|,
literal|"UNION%.*UNION%"
argument_list|)
argument_list|,
name|UnionProcFactory
operator|.
name|getUnknownUnion
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
operator|new
name|String
argument_list|(
literal|"R3"
argument_list|)
argument_list|,
literal|"TS%.*UNION%"
argument_list|)
argument_list|,
name|UnionProcFactory
operator|.
name|getMapUnion
argument_list|()
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
operator|new
name|String
argument_list|(
literal|"R3"
argument_list|)
argument_list|,
literal|"MAPJOIN%.*UNION%"
argument_list|)
argument_list|,
name|UnionProcFactory
operator|.
name|getMapJoinUnion
argument_list|()
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor for the matching rule and passes the
comment|// context along
name|UnionProcContext
name|uCtx
init|=
operator|new
name|UnionProcContext
argument_list|()
decl_stmt|;
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|UnionProcFactory
operator|.
name|getNoUnion
argument_list|()
argument_list|,
name|opRules
argument_list|,
name|uCtx
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|PreOrderWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
comment|// Create a list of topop nodes
name|ArrayList
argument_list|<
name|Node
argument_list|>
name|topNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|topNodes
operator|.
name|addAll
argument_list|(
name|pCtx
operator|.
name|getTopOps
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|pCtx
operator|.
name|setUCtx
argument_list|(
name|uCtx
argument_list|)
expr_stmt|;
return|return
name|pCtx
return|;
block|}
block|}
end_class

end_unit

