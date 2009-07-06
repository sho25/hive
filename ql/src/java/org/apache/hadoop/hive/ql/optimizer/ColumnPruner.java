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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|HashMap
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
name|exec
operator|.
name|FileSinkOperator
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
name|exec
operator|.
name|Operator
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
name|exec
operator|.
name|ScriptOperator
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
name|exec
operator|.
name|SelectOperator
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
name|DefaultGraphWalker
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
name|parse
operator|.
name|OpParseContext
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
name|RowResolver
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
comment|/**  * Implementation of one of the rule-based optimization steps. ColumnPruner gets the current operator tree. The \  * tree is traversed to find out the columns used   * for all the base tables. If all the columns for a table are not used, a select is pushed on top of that table   * (to select only those columns). Since this   * changes the row resolver, the tree is built again. This can be optimized later to patch the tree.   */
end_comment

begin_class
specifier|public
class|class
name|ColumnPruner
implements|implements
name|Transform
block|{
specifier|protected
name|ParseContext
name|pGraphContext
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|OpParseContext
argument_list|>
name|opToParseCtxMap
decl_stmt|;
comment|/**    * empty constructor    */
specifier|public
name|ColumnPruner
parameter_list|()
block|{
name|pGraphContext
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * update the map between operator and row resolver    * @param op operator being inserted    * @param rr row resolver of the operator    * @return    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|private
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|putOpInsertMap
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
parameter_list|,
name|RowResolver
name|rr
parameter_list|)
block|{
name|OpParseContext
name|ctx
init|=
operator|new
name|OpParseContext
argument_list|(
name|rr
argument_list|)
decl_stmt|;
name|pGraphContext
operator|.
name|getOpParseCtx
argument_list|()
operator|.
name|put
argument_list|(
name|op
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
return|return
name|op
return|;
block|}
comment|/**    * Transform the query tree. For each table under consideration, check if all columns are needed. If not,     * only select the operators needed at the beginning and proceed     * @param pactx the current parse context    */
specifier|public
name|ParseContext
name|transform
parameter_list|(
name|ParseContext
name|pactx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|this
operator|.
name|pGraphContext
operator|=
name|pactx
expr_stmt|;
name|this
operator|.
name|opToParseCtxMap
operator|=
name|pGraphContext
operator|.
name|getOpParseCtx
argument_list|()
expr_stmt|;
comment|// generate pruned column list for all relevant operators
name|ColumnPrunerProcCtx
name|cppCtx
init|=
operator|new
name|ColumnPrunerProcCtx
argument_list|(
name|opToParseCtxMap
argument_list|)
decl_stmt|;
comment|// create a walker which walks the tree in a DFS manner while maintaining the operator stack. The dispatcher
comment|// generates the plan from the operator tree
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
literal|"R1"
argument_list|,
literal|"FIL%"
argument_list|)
argument_list|,
name|ColumnPrunerProcFactory
operator|.
name|getFilterProc
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
literal|"R2"
argument_list|,
literal|"GBY%"
argument_list|)
argument_list|,
name|ColumnPrunerProcFactory
operator|.
name|getGroupByProc
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
literal|"R3"
argument_list|,
literal|"RS%"
argument_list|)
argument_list|,
name|ColumnPrunerProcFactory
operator|.
name|getReduceSinkProc
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
literal|"R4"
argument_list|,
literal|"SEL%"
argument_list|)
argument_list|,
name|ColumnPrunerProcFactory
operator|.
name|getSelectProc
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
literal|"R5"
argument_list|,
literal|"JOIN%"
argument_list|)
argument_list|,
name|ColumnPrunerProcFactory
operator|.
name|getJoinProc
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
literal|"R6"
argument_list|,
literal|"MAPJOIN%"
argument_list|)
argument_list|,
name|ColumnPrunerProcFactory
operator|.
name|getMapJoinProc
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
literal|"R7"
argument_list|,
literal|"TS%"
argument_list|)
argument_list|,
name|ColumnPrunerProcFactory
operator|.
name|getTableScanProc
argument_list|()
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest matching rule and passes the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|ColumnPrunerProcFactory
operator|.
name|getDefaultProc
argument_list|()
argument_list|,
name|opRules
argument_list|,
name|cppCtx
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|ColumnPrunerWalker
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
name|pGraphContext
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
return|return
name|pGraphContext
return|;
block|}
comment|/**    * Walks the op tree in post order fashion (skips selects with file sink or script op children)    */
specifier|public
specifier|static
class|class
name|ColumnPrunerWalker
extends|extends
name|DefaultGraphWalker
block|{
specifier|public
name|ColumnPrunerWalker
parameter_list|(
name|Dispatcher
name|disp
parameter_list|)
block|{
name|super
argument_list|(
name|disp
argument_list|)
expr_stmt|;
block|}
comment|/**      * Walk the given operator      */
annotation|@
name|Override
specifier|public
name|void
name|walk
parameter_list|(
name|Node
name|nd
parameter_list|)
throws|throws
name|SemanticException
block|{
name|boolean
name|walkChildren
init|=
literal|true
decl_stmt|;
name|opStack
operator|.
name|push
argument_list|(
name|nd
argument_list|)
expr_stmt|;
comment|// no need to go further down for a select op with a file sink or script child
comment|// since all cols are needed for these ops
if|if
condition|(
name|nd
operator|instanceof
name|SelectOperator
condition|)
block|{
for|for
control|(
name|Node
name|child
range|:
name|nd
operator|.
name|getChildren
argument_list|()
control|)
block|{
if|if
condition|(
operator|(
name|child
operator|instanceof
name|FileSinkOperator
operator|)
operator|||
operator|(
name|child
operator|instanceof
name|ScriptOperator
operator|)
condition|)
name|walkChildren
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|(
name|nd
operator|.
name|getChildren
argument_list|()
operator|==
literal|null
operator|)
operator|||
name|getDispatchedList
argument_list|()
operator|.
name|containsAll
argument_list|(
name|nd
operator|.
name|getChildren
argument_list|()
argument_list|)
operator|||
operator|!
name|walkChildren
condition|)
block|{
comment|// all children are done or no need to walk the children
name|dispatch
argument_list|(
name|nd
argument_list|,
name|opStack
argument_list|)
expr_stmt|;
name|opStack
operator|.
name|pop
argument_list|()
expr_stmt|;
return|return;
block|}
comment|// move all the children to the front of queue
name|getToWalk
argument_list|()
operator|.
name|removeAll
argument_list|(
name|nd
operator|.
name|getChildren
argument_list|()
argument_list|)
expr_stmt|;
name|getToWalk
argument_list|()
operator|.
name|addAll
argument_list|(
literal|0
argument_list|,
name|nd
operator|.
name|getChildren
argument_list|()
argument_list|)
expr_stmt|;
comment|// add self to the end of the queue
name|getToWalk
argument_list|()
operator|.
name|add
argument_list|(
name|nd
argument_list|)
expr_stmt|;
name|opStack
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

