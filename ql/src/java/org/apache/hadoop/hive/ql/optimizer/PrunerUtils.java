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
name|List
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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|FilterOperator
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
name|TableScanOperator
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
name|NodeProcessorCtx
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
name|RuleExactMatch
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
name|lib
operator|.
name|TypeRule
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
name|plan
operator|.
name|ExprNodeColumnDesc
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
name|plan
operator|.
name|ExprNodeDesc
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
name|plan
operator|.
name|ExprNodeFieldDesc
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
name|plan
operator|.
name|ExprNodeGenericFuncDesc
import|;
end_import

begin_comment
comment|/**  * General utility common functions for the Pruner to do optimization.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|PrunerUtils
block|{
specifier|private
specifier|static
name|Log
name|LOG
decl_stmt|;
static|static
block|{
name|LOG
operator|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"org.apache.hadoop.hive.ql.optimizer.PrunerUtils"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|PrunerUtils
parameter_list|()
block|{
comment|//prevent instantiation
block|}
comment|/**    * Walk operator tree for pruner generation.    *    * @param pctx    * @param opWalkerCtx    * @param filterProc    * @param defaultProc    * @throws SemanticException    */
specifier|public
specifier|static
name|void
name|walkOperatorTree
parameter_list|(
name|ParseContext
name|pctx
parameter_list|,
name|NodeProcessorCtx
name|opWalkerCtx
parameter_list|,
name|NodeProcessor
name|filterProc
parameter_list|,
name|NodeProcessor
name|defaultProc
parameter_list|)
throws|throws
name|SemanticException
block|{
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
comment|// Build regular expression for operator rule.
comment|// "(TS%FIL%)|(TS%FIL%FIL%)"
name|String
name|tsOprName
init|=
name|TableScanOperator
operator|.
name|getOperatorName
argument_list|()
decl_stmt|;
name|String
name|filtOprName
init|=
name|FilterOperator
operator|.
name|getOperatorName
argument_list|()
decl_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleExactMatch
argument_list|(
literal|"R1"
argument_list|,
operator|new
name|String
index|[]
block|{
name|tsOprName
block|,
name|filtOprName
block|,
name|filtOprName
block|}
argument_list|)
argument_list|,
name|filterProc
argument_list|)
expr_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleExactMatch
argument_list|(
literal|"R2"
argument_list|,
operator|new
name|String
index|[]
block|{
name|tsOprName
block|,
name|filtOprName
block|}
argument_list|)
argument_list|,
name|filterProc
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest matching
comment|// rule and passes the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|defaultProc
argument_list|,
name|opRules
argument_list|,
name|opWalkerCtx
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
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
name|pctx
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
block|}
comment|/**    * Walk expression tree for pruner generation.    *    * @param pred    * @param ctx    * @param colProc    * @param fieldProc    * @param genFuncProc    * @param defProc    * @return    * @throws SemanticException    */
specifier|public
specifier|static
name|Map
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
name|walkExprTree
parameter_list|(
name|ExprNodeDesc
name|pred
parameter_list|,
name|NodeProcessorCtx
name|ctx
parameter_list|,
name|NodeProcessor
name|colProc
parameter_list|,
name|NodeProcessor
name|fieldProc
parameter_list|,
name|NodeProcessor
name|genFuncProc
parameter_list|,
name|NodeProcessor
name|defProc
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// create a walker which walks the tree in a DFS manner while maintaining
comment|// the operator stack. The dispatcher
comment|// generates the plan from the operator tree
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|exprRules
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
name|exprRules
operator|.
name|put
argument_list|(
operator|new
name|TypeRule
argument_list|(
name|ExprNodeColumnDesc
operator|.
name|class
argument_list|)
argument_list|,
name|colProc
argument_list|)
expr_stmt|;
name|exprRules
operator|.
name|put
argument_list|(
operator|new
name|TypeRule
argument_list|(
name|ExprNodeFieldDesc
operator|.
name|class
argument_list|)
argument_list|,
name|fieldProc
argument_list|)
expr_stmt|;
name|exprRules
operator|.
name|put
argument_list|(
operator|new
name|TypeRule
argument_list|(
name|ExprNodeGenericFuncDesc
operator|.
name|class
argument_list|)
argument_list|,
name|genFuncProc
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest matching
comment|// rule and passes the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|defProc
argument_list|,
name|exprRules
argument_list|,
name|ctx
argument_list|)
decl_stmt|;
name|GraphWalker
name|egw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Node
argument_list|>
name|startNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|startNodes
operator|.
name|add
argument_list|(
name|pred
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
name|outputMap
init|=
operator|new
name|HashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|egw
operator|.
name|startWalking
argument_list|(
name|startNodes
argument_list|,
name|outputMap
argument_list|)
expr_stmt|;
return|return
name|outputMap
return|;
block|}
block|}
end_class

end_unit

