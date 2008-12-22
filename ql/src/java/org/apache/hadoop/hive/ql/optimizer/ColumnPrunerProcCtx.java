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
name|selectDesc
import|;
end_import

begin_comment
comment|/**  * This class implements the processor context for Column Pruner.  */
end_comment

begin_class
specifier|public
class|class
name|ColumnPrunerProcCtx
extends|extends
name|NodeProcessorCtx
block|{
specifier|private
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|prunedColLists
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
specifier|public
name|ColumnPrunerProcCtx
parameter_list|(
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
name|opToParseContextMap
parameter_list|)
block|{
name|prunedColLists
operator|=
operator|new
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|opToParseCtxMap
operator|=
name|opToParseContextMap
expr_stmt|;
block|}
comment|/**    * @return the prunedColLists    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getPrunedColList
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
parameter_list|)
block|{
return|return
name|prunedColLists
operator|.
name|get
argument_list|(
name|op
argument_list|)
return|;
block|}
specifier|public
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
name|getOpToParseCtxMap
parameter_list|()
block|{
return|return
name|opToParseCtxMap
return|;
block|}
specifier|public
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|getPrunedColLists
parameter_list|()
block|{
return|return
name|prunedColLists
return|;
block|}
comment|/**    * Creates the list of internal column names(these names are used in the RowResolver and    * are different from the external column names) that are needed in the subtree. These columns     * eventually have to be selected from the table scan.    *     * @param curOp The root of the operator subtree.    * @return List<String> of the internal column names.    * @throws SemanticException    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|genColLists
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|curOp
parameter_list|)
throws|throws
name|SemanticException
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
if|if
condition|(
name|curOp
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
name|curOp
operator|.
name|getChildOperators
argument_list|()
control|)
name|colList
operator|=
name|Utilities
operator|.
name|mergeUniqElems
argument_list|(
name|colList
argument_list|,
name|prunedColLists
operator|.
name|get
argument_list|(
name|child
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|colList
return|;
block|}
comment|/**    * Creates the list of internal column names from select expressions in a select operator.    * This function is used for the select operator instead of the genColLists function (which is    * used by the rest of the operators).    *     * @param op The select operator.    * @return List<String> of the internal column names.    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getColsFromSelectExpr
parameter_list|(
name|SelectOperator
name|op
parameter_list|)
block|{
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
name|selectDesc
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
name|exprList
init|=
name|conf
operator|.
name|getColList
argument_list|()
decl_stmt|;
for|for
control|(
name|exprNodeDesc
name|expr
range|:
name|exprList
control|)
name|cols
operator|=
name|Utilities
operator|.
name|mergeUniqElems
argument_list|(
name|cols
argument_list|,
name|expr
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|cols
return|;
block|}
comment|/**    * Creates the list of internal column names for select * expressions.    *     * @param op The select operator.    * @param colList The list of internal column names returned by the children of the select operator.    * @return List<String> of the internal column names.    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getSelectColsFromChildren
parameter_list|(
name|SelectOperator
name|op
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|colList
parameter_list|)
block|{
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
name|selectDesc
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
name|selectExprs
init|=
name|conf
operator|.
name|getColList
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|col
range|:
name|colList
control|)
block|{
comment|// col is the internal name i.e. position within the expression list
name|exprNodeDesc
name|expr
init|=
name|selectExprs
operator|.
name|get
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|col
argument_list|)
argument_list|)
decl_stmt|;
name|cols
operator|=
name|Utilities
operator|.
name|mergeUniqElems
argument_list|(
name|cols
argument_list|,
name|expr
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|cols
return|;
block|}
block|}
end_class

end_unit

