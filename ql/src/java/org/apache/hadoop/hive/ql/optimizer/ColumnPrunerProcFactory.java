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
name|ql
operator|.
name|exec
operator|.
name|ColumnInfo
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
name|exec
operator|.
name|Utilities
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
name|aggregationDesc
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
name|exprNodeDesc
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
name|groupByDesc
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
name|reduceSinkDesc
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
name|selectDesc
import|;
end_import

begin_comment
comment|/**  * Factory for generating the different node processors used by ColumnPruner.  */
end_comment

begin_class
specifier|public
class|class
name|ColumnPrunerProcFactory
block|{
comment|/**    * Node Processor for Column Pruning on Filter Operators.    */
specifier|public
specifier|static
class|class
name|ColumnPrunerFilterProc
implements|implements
name|NodeProcessor
block|{
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|NodeProcessorCtx
name|ctx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|FilterOperator
name|op
init|=
operator|(
name|FilterOperator
operator|)
name|nd
decl_stmt|;
name|ColumnPrunerProcCtx
name|cppCtx
init|=
operator|(
name|ColumnPrunerProcCtx
operator|)
name|ctx
decl_stmt|;
name|exprNodeDesc
name|condn
init|=
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getPredicate
argument_list|()
decl_stmt|;
comment|// get list of columns used in the filter
name|List
argument_list|<
name|String
argument_list|>
name|cl
init|=
name|condn
operator|.
name|getCols
argument_list|()
decl_stmt|;
comment|// merge it with the downstream col list
name|cppCtx
operator|.
name|getPrunedColLists
argument_list|()
operator|.
name|put
argument_list|(
name|op
argument_list|,
name|Utilities
operator|.
name|mergeUniqElems
argument_list|(
name|cppCtx
operator|.
name|genColLists
argument_list|(
name|op
argument_list|)
argument_list|,
name|cl
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Factory method to get the ColumnPrunerFilterProc class.    * @return ColumnPrunerFilterProc    */
specifier|public
specifier|static
name|ColumnPrunerFilterProc
name|getFilterProc
parameter_list|()
block|{
return|return
operator|new
name|ColumnPrunerFilterProc
argument_list|()
return|;
block|}
comment|/**    * Node Processor for Column Pruning on Group By Operators.    */
specifier|public
specifier|static
class|class
name|ColumnPrunerGroupByProc
implements|implements
name|NodeProcessor
block|{
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|NodeProcessorCtx
name|ctx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|GroupByOperator
name|op
init|=
operator|(
name|GroupByOperator
operator|)
name|nd
decl_stmt|;
name|ColumnPrunerProcCtx
name|cppCtx
init|=
operator|(
name|ColumnPrunerProcCtx
operator|)
name|ctx
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colLists
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|groupByDesc
name|conf
init|=
name|op
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|keys
init|=
name|conf
operator|.
name|getKeys
argument_list|()
decl_stmt|;
for|for
control|(
name|exprNodeDesc
name|key
range|:
name|keys
control|)
name|colLists
operator|=
name|Utilities
operator|.
name|mergeUniqElems
argument_list|(
name|colLists
argument_list|,
name|key
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|aggregationDesc
argument_list|>
name|aggrs
init|=
name|conf
operator|.
name|getAggregators
argument_list|()
decl_stmt|;
for|for
control|(
name|aggregationDesc
name|aggr
range|:
name|aggrs
control|)
block|{
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|params
init|=
name|aggr
operator|.
name|getParameters
argument_list|()
decl_stmt|;
for|for
control|(
name|exprNodeDesc
name|param
range|:
name|params
control|)
name|colLists
operator|=
name|Utilities
operator|.
name|mergeUniqElems
argument_list|(
name|colLists
argument_list|,
name|param
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|cppCtx
operator|.
name|getPrunedColLists
argument_list|()
operator|.
name|put
argument_list|(
name|op
argument_list|,
name|colLists
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Factory method to get the ColumnPrunerGroupByProc class.    * @return ColumnPrunerGroupByProc    */
specifier|public
specifier|static
name|ColumnPrunerGroupByProc
name|getGroupByProc
parameter_list|()
block|{
return|return
operator|new
name|ColumnPrunerGroupByProc
argument_list|()
return|;
block|}
comment|/**    * The Default Node Processor for Column Pruning.    */
specifier|public
specifier|static
class|class
name|ColumnPrunerDefaultProc
implements|implements
name|NodeProcessor
block|{
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|NodeProcessorCtx
name|ctx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ColumnPrunerProcCtx
name|cppCtx
init|=
operator|(
name|ColumnPrunerProcCtx
operator|)
name|ctx
decl_stmt|;
name|cppCtx
operator|.
name|getPrunedColLists
argument_list|()
operator|.
name|put
argument_list|(
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|nd
argument_list|,
name|cppCtx
operator|.
name|genColLists
argument_list|(
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|nd
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Factory method to get the ColumnPrunerDefaultProc class.    * @return ColumnPrunerDefaultProc    */
specifier|public
specifier|static
name|ColumnPrunerDefaultProc
name|getDefaultProc
parameter_list|()
block|{
return|return
operator|new
name|ColumnPrunerDefaultProc
argument_list|()
return|;
block|}
comment|/**    * The Node Processor for Column Pruning on Reduce Sink Operators.    */
specifier|public
specifier|static
class|class
name|ColumnPrunerReduceSinkProc
implements|implements
name|NodeProcessor
block|{
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|NodeProcessorCtx
name|ctx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ReduceSinkOperator
name|op
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|nd
decl_stmt|;
name|ColumnPrunerProcCtx
name|cppCtx
init|=
operator|(
name|ColumnPrunerProcCtx
operator|)
name|ctx
decl_stmt|;
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
init|=
name|cppCtx
operator|.
name|getOpToParseCtxMap
argument_list|()
decl_stmt|;
name|RowResolver
name|redSinkRR
init|=
name|opToParseCtxMap
operator|.
name|get
argument_list|(
name|op
argument_list|)
operator|.
name|getRR
argument_list|()
decl_stmt|;
name|reduceSinkDesc
name|conf
init|=
name|op
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|childOperators
init|=
name|op
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|parentOperators
init|=
name|op
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|childColLists
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
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|child
range|:
name|childOperators
control|)
name|childColLists
operator|=
name|Utilities
operator|.
name|mergeUniqElems
argument_list|(
name|childColLists
argument_list|,
name|cppCtx
operator|.
name|getPrunedColLists
argument_list|()
operator|.
name|get
argument_list|(
name|child
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colLists
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|keys
init|=
name|conf
operator|.
name|getKeyCols
argument_list|()
decl_stmt|;
for|for
control|(
name|exprNodeDesc
name|key
range|:
name|keys
control|)
name|colLists
operator|=
name|Utilities
operator|.
name|mergeUniqElems
argument_list|(
name|colLists
argument_list|,
name|key
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|childOperators
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|)
operator|&&
operator|(
name|childOperators
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|JoinOperator
operator|)
condition|)
block|{
assert|assert
name|parentOperators
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|par
init|=
name|parentOperators
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|RowResolver
name|parRR
init|=
name|opToParseCtxMap
operator|.
name|get
argument_list|(
name|par
argument_list|)
operator|.
name|getRR
argument_list|()
decl_stmt|;
name|RowResolver
name|childRR
init|=
name|opToParseCtxMap
operator|.
name|get
argument_list|(
name|childOperators
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|getRR
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|childCol
range|:
name|childColLists
control|)
block|{
name|String
index|[]
name|nm
init|=
name|childRR
operator|.
name|reverseLookup
argument_list|(
name|childCol
argument_list|)
decl_stmt|;
name|ColumnInfo
name|cInfo
init|=
name|redSinkRR
operator|.
name|get
argument_list|(
name|nm
index|[
literal|0
index|]
argument_list|,
name|nm
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|cInfo
operator|!=
literal|null
condition|)
block|{
name|cInfo
operator|=
name|parRR
operator|.
name|get
argument_list|(
name|nm
index|[
literal|0
index|]
argument_list|,
name|nm
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|colLists
operator|.
name|contains
argument_list|(
name|cInfo
operator|.
name|getInternalName
argument_list|()
argument_list|)
condition|)
name|colLists
operator|.
name|add
argument_list|(
name|cInfo
operator|.
name|getInternalName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// Reduce Sink contains the columns needed - no need to aggregate from children
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|vals
init|=
name|conf
operator|.
name|getValueCols
argument_list|()
decl_stmt|;
for|for
control|(
name|exprNodeDesc
name|val
range|:
name|vals
control|)
name|colLists
operator|=
name|Utilities
operator|.
name|mergeUniqElems
argument_list|(
name|colLists
argument_list|,
name|val
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|cppCtx
operator|.
name|getPrunedColLists
argument_list|()
operator|.
name|put
argument_list|(
name|op
argument_list|,
name|colLists
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * The Factory method to get ColumnPrunerReduceSinkProc class.    * @return ColumnPrunerReduceSinkProc    */
specifier|public
specifier|static
name|ColumnPrunerReduceSinkProc
name|getReduceSinkProc
parameter_list|()
block|{
return|return
operator|new
name|ColumnPrunerReduceSinkProc
argument_list|()
return|;
block|}
comment|/**    * The Node Processor for Column Pruning on Select Operators.    */
specifier|public
specifier|static
class|class
name|ColumnPrunerSelectProc
implements|implements
name|NodeProcessor
block|{
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|NodeProcessorCtx
name|ctx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|SelectOperator
name|op
init|=
operator|(
name|SelectOperator
operator|)
name|nd
decl_stmt|;
name|ColumnPrunerProcCtx
name|cppCtx
init|=
operator|(
name|ColumnPrunerProcCtx
operator|)
name|ctx
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|cols
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|op
operator|.
name|getChildOperators
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|child
range|:
name|op
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
comment|// If one of my children is a FileSink or Script, return all columns.
comment|// Without this break, a bug in ReduceSink to Extract edge column pruning will manifest
comment|// which should be fixed before remove this
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
block|{
name|cppCtx
operator|.
name|getPrunedColLists
argument_list|()
operator|.
name|put
argument_list|(
name|op
argument_list|,
name|cppCtx
operator|.
name|getColsFromSelectExpr
argument_list|(
name|op
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|cols
operator|=
name|Utilities
operator|.
name|mergeUniqElems
argument_list|(
name|cols
argument_list|,
name|cppCtx
operator|.
name|getPrunedColLists
argument_list|()
operator|.
name|get
argument_list|(
name|child
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|selectDesc
name|conf
init|=
name|op
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|conf
operator|.
name|isSelectStar
argument_list|()
operator|&&
operator|!
name|cols
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// The input to the select does not matter. Go over the expressions
comment|// and return the ones which have a marked column
name|cppCtx
operator|.
name|getPrunedColLists
argument_list|()
operator|.
name|put
argument_list|(
name|op
argument_list|,
name|cppCtx
operator|.
name|getSelectColsFromChildren
argument_list|(
name|op
argument_list|,
name|cols
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|cppCtx
operator|.
name|getPrunedColLists
argument_list|()
operator|.
name|put
argument_list|(
name|op
argument_list|,
name|cppCtx
operator|.
name|getColsFromSelectExpr
argument_list|(
name|op
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * The Factory method to get the ColumnPrunerSelectProc class.    * @return ColumnPrunerSelectProc    */
specifier|public
specifier|static
name|ColumnPrunerSelectProc
name|getSelectProc
parameter_list|()
block|{
return|return
operator|new
name|ColumnPrunerSelectProc
argument_list|()
return|;
block|}
block|}
end_class

end_unit

