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
name|parse
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|GroupByOperator
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
name|JoinOperator
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
name|MapJoinOperator
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
name|ReduceSinkOperator
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
name|metadata
operator|.
name|Table
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
name|ExprNodeConstantDesc
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
name|OperatorDesc
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
name|SelectDesc
import|;
end_import

begin_comment
comment|/**  * TableAccessAnalyzer walks the operator graph from joins and group bys  * to the table scan operator backing it. It checks whether the operators  * in the path are pass-through of the base table (no aggregations/joins),  * and if the keys are mapped by expressions that do not modify the bucket  * for the key. If all the keys for a join/group by are clean pass-through  * of the base table columns, we can consider this operator as a candidate  * for improvement through bucketing.  */
end_comment

begin_class
specifier|public
class|class
name|TableAccessAnalyzer
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TableAccessAnalyzer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ParseContext
name|pGraphContext
decl_stmt|;
specifier|public
name|TableAccessAnalyzer
parameter_list|()
block|{
name|pGraphContext
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|TableAccessAnalyzer
parameter_list|(
name|ParseContext
name|pactx
parameter_list|)
block|{
name|pGraphContext
operator|=
name|pactx
expr_stmt|;
block|}
specifier|public
name|TableAccessInfo
name|analyzeTableAccess
parameter_list|()
throws|throws
name|SemanticException
block|{
comment|// Set up the rules for the graph walker for group by and join operators
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
name|GroupByOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|GroupByProcessor
argument_list|(
name|pGraphContext
argument_list|)
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
name|JoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|JoinProcessor
argument_list|(
name|pGraphContext
argument_list|)
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
name|MapJoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|JoinProcessor
argument_list|(
name|pGraphContext
argument_list|)
argument_list|)
expr_stmt|;
name|TableAccessCtx
name|tableAccessCtx
init|=
operator|new
name|TableAccessCtx
argument_list|()
decl_stmt|;
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|getDefaultProc
argument_list|()
argument_list|,
name|opRules
argument_list|,
name|tableAccessCtx
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
comment|// Create a list of topop nodes and walk!
name|List
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
name|tableAccessCtx
operator|.
name|getTableAccessInfo
argument_list|()
return|;
block|}
specifier|private
name|NodeProcessor
name|getDefaultProc
parameter_list|()
block|{
return|return
operator|new
name|NodeProcessor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
literal|null
return|;
block|}
block|}
return|;
block|}
comment|/**    * Processor for GroupBy operator    */
specifier|public
class|class
name|GroupByProcessor
implements|implements
name|NodeProcessor
block|{
specifier|protected
name|ParseContext
name|pGraphContext
decl_stmt|;
specifier|public
name|GroupByProcessor
parameter_list|(
name|ParseContext
name|pGraphContext
parameter_list|)
block|{
name|this
operator|.
name|pGraphContext
operator|=
name|pGraphContext
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
block|{
name|GroupByOperator
name|op
init|=
operator|(
name|GroupByOperator
operator|)
name|nd
decl_stmt|;
name|TableAccessCtx
name|tableAccessCtx
init|=
operator|(
name|TableAccessCtx
operator|)
name|procCtx
decl_stmt|;
comment|// Get the key column names, and check if the keys are all constants
comment|// or columns (not expressions). If yes, proceed.
name|List
argument_list|<
name|String
argument_list|>
name|keyColNames
init|=
name|TableAccessAnalyzer
operator|.
name|getKeyColNames
argument_list|(
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getKeys
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyColNames
operator|==
literal|null
condition|)
block|{
comment|// we are done, since there are no keys to check for
return|return
literal|null
return|;
block|}
comment|// Walk the operator tree to the TableScan and build the mapping
comment|// along the way for the columns that the group by uses as keys
name|TableScanOperator
name|tso
init|=
name|TableAccessAnalyzer
operator|.
name|genRootTableScan
argument_list|(
name|op
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|keyColNames
argument_list|)
decl_stmt|;
if|if
condition|(
name|tso
operator|==
literal|null
condition|)
block|{
comment|// Could not find an allowed path to a table scan operator,
comment|// hence we are done
return|return
literal|null
return|;
block|}
comment|// Must be deterministic order map for consistent q-test output across Java versions
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableToKeysMap
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|Table
name|tbl
init|=
name|tso
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
decl_stmt|;
name|tableToKeysMap
operator|.
name|put
argument_list|(
name|tbl
operator|.
name|getCompleteName
argument_list|()
argument_list|,
name|keyColNames
argument_list|)
expr_stmt|;
name|tableAccessCtx
operator|.
name|addOperatorTableAccess
argument_list|(
name|op
argument_list|,
name|tableToKeysMap
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Processor for Join operator.    */
specifier|public
class|class
name|JoinProcessor
implements|implements
name|NodeProcessor
block|{
specifier|protected
name|ParseContext
name|pGraphContext
decl_stmt|;
specifier|public
name|JoinProcessor
parameter_list|(
name|ParseContext
name|pGraphContext
parameter_list|)
block|{
name|this
operator|.
name|pGraphContext
operator|=
name|pGraphContext
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
block|{
name|JoinOperator
name|op
init|=
operator|(
name|JoinOperator
operator|)
name|nd
decl_stmt|;
name|TableAccessCtx
name|tableAccessCtx
init|=
operator|(
name|TableAccessCtx
operator|)
name|procCtx
decl_stmt|;
comment|// Must be deterministic order map for consistent q-test output across Java versions
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|tableToKeysMap
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|parentOps
init|=
name|op
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
comment|// Get the key column names for each side of the join,
comment|// and check if the keys are all constants
comment|// or columns (not expressions). If yes, proceed.
assert|assert
operator|(
name|parentOps
operator|.
name|size
argument_list|()
operator|==
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getBaseSrc
argument_list|()
operator|.
name|length
operator|)
assert|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|src
range|:
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getBaseSrc
argument_list|()
control|)
block|{
if|if
condition|(
name|src
operator|!=
literal|null
condition|)
block|{
assert|assert
operator|(
name|parentOps
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|instanceof
name|ReduceSinkOperator
operator|)
assert|;
name|ReduceSinkOperator
name|reduceSinkOp
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|parentOps
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
comment|// Get the key column names, and check if the keys are all constants
comment|// or columns (not expressions). If yes, proceed.
name|List
argument_list|<
name|String
argument_list|>
name|keyColNames
init|=
name|TableAccessAnalyzer
operator|.
name|getKeyColNames
argument_list|(
name|reduceSinkOp
operator|.
name|getConf
argument_list|()
operator|.
name|getKeyCols
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyColNames
operator|==
literal|null
condition|)
block|{
comment|// we are done, since there are no keys to check for
return|return
literal|null
return|;
block|}
comment|// Walk the operator tree to the TableScan and build the mapping
comment|// along the way for the columns that the group by uses as keys
name|TableScanOperator
name|tso
init|=
name|TableAccessAnalyzer
operator|.
name|genRootTableScan
argument_list|(
name|reduceSinkOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|keyColNames
argument_list|)
decl_stmt|;
if|if
condition|(
name|tso
operator|==
literal|null
condition|)
block|{
comment|// Could not find an allowed path to a table scan operator,
comment|// hence we are done
return|return
literal|null
return|;
block|}
name|Table
name|tbl
init|=
name|tso
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
decl_stmt|;
name|tableToKeysMap
operator|.
name|put
argument_list|(
name|tbl
operator|.
name|getCompleteName
argument_list|()
argument_list|,
name|keyColNames
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
name|pos
operator|++
expr_stmt|;
block|}
comment|// We only get here if we could map all join keys to source table columns
name|tableAccessCtx
operator|.
name|addOperatorTableAccess
argument_list|(
name|op
argument_list|,
name|tableToKeysMap
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * This method traces up from the given operator to the root    * of the operator graph until a TableScanOperator is reached.    * Along the way, if any operators are present that do not    * provide a direct mapping from columns of the base table to    * the keys on the input operator, the trace-back is stopped at that    * point. If the trace back can be done successfully, the method    * returns the root TableScanOperator as well as the list of column    * names on that table that map to the keys used for the input    * operator (which is currently only a join or group by).    */
specifier|public
specifier|static
name|TableScanOperator
name|genRootTableScan
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|keyNames
parameter_list|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|currOp
init|=
name|op
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|currColNames
init|=
name|keyNames
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|parentOps
init|=
literal|null
decl_stmt|;
comment|// Track as you walk up the tree if there is an operator
comment|// along the way that changes the rows from the table through
comment|// joins or aggregations. Only allowed operators are selects
comment|// and filters.
while|while
condition|(
literal|true
condition|)
block|{
name|parentOps
operator|=
name|currOp
operator|.
name|getParentOperators
argument_list|()
expr_stmt|;
if|if
condition|(
operator|(
name|parentOps
operator|==
literal|null
operator|)
operator|||
operator|(
name|parentOps
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
return|return
operator|(
name|TableScanOperator
operator|)
name|currOp
return|;
block|}
if|if
condition|(
name|parentOps
operator|.
name|size
argument_list|()
operator|>
literal|1
operator|||
operator|!
operator|(
name|currOp
operator|.
name|columnNamesRowResolvedCanBeObtained
argument_list|()
operator|)
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
comment|// Generate the map of the input->output column name for the keys
comment|// we are about
if|if
condition|(
operator|!
name|TableAccessAnalyzer
operator|.
name|genColNameMap
argument_list|(
name|currOp
argument_list|,
name|currColNames
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|currOp
operator|=
name|parentOps
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/*    * This method takes in an input operator and a subset of its output    * column names, and generates the input column names for the operator    * corresponding to those outputs. If the mapping from the input column    * name to the output column name is not simple, the method returns    * false, else it returns true. The list of output column names is    * modified by this method to be the list of corresponding input column    * names.    */
specifier|private
specifier|static
name|boolean
name|genColNameMap
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|currColNames
parameter_list|)
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|colList
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|outputColNames
init|=
literal|null
decl_stmt|;
assert|assert
operator|(
name|op
operator|.
name|columnNamesRowResolvedCanBeObtained
argument_list|()
operator|)
assert|;
comment|// Only select operators among the allowed operators can cause changes in the
comment|// column names
if|if
condition|(
name|op
operator|instanceof
name|SelectOperator
condition|)
block|{
name|SelectDesc
name|selectDesc
init|=
operator|(
operator|(
name|SelectOperator
operator|)
name|op
operator|)
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|selectDesc
operator|.
name|isSelStarNoCompute
argument_list|()
condition|)
block|{
name|colList
operator|=
name|selectDesc
operator|.
name|getColList
argument_list|()
expr_stmt|;
name|outputColNames
operator|=
name|selectDesc
operator|.
name|getOutputColumnNames
argument_list|()
expr_stmt|;
comment|// Only columns and constants can be selected
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|colList
operator|.
name|size
argument_list|()
condition|;
name|pos
operator|++
control|)
block|{
name|ExprNodeDesc
name|colExpr
init|=
name|colList
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|String
name|outputColName
init|=
name|outputColNames
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
comment|// If it is not a column we need for the keys, move on
if|if
condition|(
operator|!
name|currColNames
operator|.
name|contains
argument_list|(
name|outputColName
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|colExpr
operator|instanceof
name|ExprNodeConstantDesc
condition|)
block|{
name|currColNames
operator|.
name|remove
argument_list|(
name|outputColName
argument_list|)
expr_stmt|;
continue|continue;
block|}
elseif|else
if|if
condition|(
name|colExpr
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
name|String
name|inputColName
init|=
operator|(
operator|(
name|ExprNodeColumnDesc
operator|)
name|colExpr
operator|)
operator|.
name|getColumn
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|outputColName
operator|.
name|equals
argument_list|(
name|inputColName
argument_list|)
condition|)
block|{
name|currColNames
operator|.
name|set
argument_list|(
name|currColNames
operator|.
name|indexOf
argument_list|(
name|outputColName
argument_list|)
argument_list|,
name|inputColName
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// the column map can not be generated
return|return
literal|false
return|;
block|}
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|getKeyColNames
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keys
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|colList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|expr
range|:
name|keys
control|)
block|{
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
name|ExprNodeColumnDesc
name|colExpr
init|=
operator|(
name|ExprNodeColumnDesc
operator|)
name|expr
decl_stmt|;
name|colList
operator|.
name|add
argument_list|(
name|colExpr
operator|.
name|getColumn
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeConstantDesc
condition|)
block|{
continue|continue;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
return|return
name|colList
return|;
block|}
block|}
end_class

end_unit

