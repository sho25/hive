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
name|lineage
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
name|LinkedHashSet
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
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|RowSchema
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
name|hooks
operator|.
name|LineageInfo
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
name|hooks
operator|.
name|LineageInfo
operator|.
name|BaseColumnInfo
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
name|hooks
operator|.
name|LineageInfo
operator|.
name|Dependency
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
name|hooks
operator|.
name|LineageInfo
operator|.
name|DependencyType
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
name|hooks
operator|.
name|LineageInfo
operator|.
name|Predicate
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
name|hooks
operator|.
name|LineageInfo
operator|.
name|TableAliasInfo
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

begin_comment
comment|/**  * Expression processor factory for lineage. Each processor is responsible to  * create the leaf level column info objects that the expression depends upon  * and also generates a string representation of the expression.  */
end_comment

begin_class
specifier|public
class|class
name|ExprProcFactory
block|{
comment|/**    * Processor for column expressions.    */
specifier|public
specifier|static
class|class
name|ColumnExprProcessor
implements|implements
name|NodeProcessor
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
name|ExprNodeColumnDesc
name|cd
init|=
operator|(
name|ExprNodeColumnDesc
operator|)
name|nd
decl_stmt|;
name|ExprProcCtx
name|epc
init|=
operator|(
name|ExprProcCtx
operator|)
name|procCtx
decl_stmt|;
comment|// assert that the input operator is not null as there are no
comment|// exprs associated with table scans.
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|operator
init|=
name|epc
operator|.
name|getInputOperator
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|operator
operator|!=
literal|null
operator|)
assert|;
name|RowSchema
name|schema
init|=
name|epc
operator|.
name|getSchema
argument_list|()
decl_stmt|;
name|ColumnInfo
name|ci
init|=
name|schema
operator|.
name|getColumnInfo
argument_list|(
name|cd
operator|.
name|getColumn
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|ci
operator|==
literal|null
operator|&&
name|operator
operator|instanceof
name|ReduceSinkOperator
condition|)
block|{
name|ci
operator|=
name|schema
operator|.
name|getColumnInfo
argument_list|(
name|Utilities
operator|.
name|removeValueTag
argument_list|(
name|cd
operator|.
name|getColumn
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Insert the dependencies of inp_ci to that of the current operator, ci
name|LineageCtx
name|lc
init|=
name|epc
operator|.
name|getLineageCtx
argument_list|()
decl_stmt|;
name|Dependency
name|dep
init|=
name|lc
operator|.
name|getIndex
argument_list|()
operator|.
name|getDependency
argument_list|(
name|operator
argument_list|,
name|ci
argument_list|)
decl_stmt|;
return|return
name|dep
return|;
block|}
block|}
comment|/**    * Processor for any function or field expression.    */
specifier|public
specifier|static
class|class
name|GenericExprProcessor
implements|implements
name|NodeProcessor
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
assert|assert
operator|(
name|nd
operator|instanceof
name|ExprNodeGenericFuncDesc
operator|||
name|nd
operator|instanceof
name|ExprNodeFieldDesc
operator|)
assert|;
comment|// Concatenate the dependencies of all the children to compute the new
comment|// dependency.
name|Dependency
name|dep
init|=
operator|new
name|Dependency
argument_list|()
decl_stmt|;
name|LinkedHashSet
argument_list|<
name|BaseColumnInfo
argument_list|>
name|bci_set
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|BaseColumnInfo
argument_list|>
argument_list|()
decl_stmt|;
name|LineageInfo
operator|.
name|DependencyType
name|new_type
init|=
name|LineageInfo
operator|.
name|DependencyType
operator|.
name|EXPRESSION
decl_stmt|;
for|for
control|(
name|Object
name|child
range|:
name|nodeOutputs
control|)
block|{
if|if
condition|(
name|child
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|Dependency
name|child_dep
init|=
operator|(
name|Dependency
operator|)
name|child
decl_stmt|;
name|new_type
operator|=
name|LineageCtx
operator|.
name|getNewDependencyType
argument_list|(
name|child_dep
operator|.
name|getType
argument_list|()
argument_list|,
name|new_type
argument_list|)
expr_stmt|;
name|bci_set
operator|.
name|addAll
argument_list|(
name|child_dep
operator|.
name|getBaseCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|dep
operator|.
name|setBaseCols
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|BaseColumnInfo
argument_list|>
argument_list|(
name|bci_set
argument_list|)
argument_list|)
expr_stmt|;
name|dep
operator|.
name|setType
argument_list|(
name|new_type
argument_list|)
expr_stmt|;
return|return
name|dep
return|;
block|}
block|}
comment|/**    * Processor for constants and null expressions. For such expressions the    * processor simply returns a null dependency vector.    */
specifier|public
specifier|static
class|class
name|DefaultExprProcessor
implements|implements
name|NodeProcessor
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
assert|assert
operator|(
name|nd
operator|instanceof
name|ExprNodeConstantDesc
operator|)
assert|;
comment|// Create a dependency that has no basecols
name|Dependency
name|dep
init|=
operator|new
name|Dependency
argument_list|()
decl_stmt|;
name|dep
operator|.
name|setType
argument_list|(
name|LineageInfo
operator|.
name|DependencyType
operator|.
name|SIMPLE
argument_list|)
expr_stmt|;
name|dep
operator|.
name|setBaseCols
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|BaseColumnInfo
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|dep
return|;
block|}
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getDefaultExprProcessor
parameter_list|()
block|{
return|return
operator|new
name|DefaultExprProcessor
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getGenericFuncProcessor
parameter_list|()
block|{
return|return
operator|new
name|GenericExprProcessor
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getFieldProcessor
parameter_list|()
block|{
return|return
operator|new
name|GenericExprProcessor
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getColumnProcessor
parameter_list|()
block|{
return|return
operator|new
name|ColumnExprProcessor
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|boolean
name|findSourceColumn
parameter_list|(
name|LineageCtx
name|lctx
parameter_list|,
name|Predicate
name|cond
parameter_list|,
name|String
name|tabAlias
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|topOpMap
range|:
name|lctx
operator|.
name|getParseCtx
argument_list|()
operator|.
name|getTopOps
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|topOp
init|=
name|topOpMap
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|topOp
operator|instanceof
name|TableScanOperator
condition|)
block|{
name|TableScanOperator
name|tableScanOp
init|=
operator|(
name|TableScanOperator
operator|)
name|topOp
decl_stmt|;
name|Table
name|tbl
init|=
name|tableScanOp
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
decl_stmt|;
if|if
condition|(
name|tbl
operator|.
name|getTableName
argument_list|()
operator|.
name|equals
argument_list|(
name|tabAlias
argument_list|)
operator|||
name|tabAlias
operator|.
name|equals
argument_list|(
name|tableScanOp
operator|.
name|getConf
argument_list|()
operator|.
name|getAlias
argument_list|()
argument_list|)
condition|)
block|{
for|for
control|(
name|FieldSchema
name|column
range|:
name|tbl
operator|.
name|getCols
argument_list|()
control|)
block|{
if|if
condition|(
name|column
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|alias
argument_list|)
condition|)
block|{
name|TableAliasInfo
name|table
init|=
operator|new
name|TableAliasInfo
argument_list|()
decl_stmt|;
name|table
operator|.
name|setTable
argument_list|(
name|tbl
operator|.
name|getTTable
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|setAlias
argument_list|(
name|tabAlias
argument_list|)
expr_stmt|;
name|BaseColumnInfo
name|colInfo
init|=
operator|new
name|BaseColumnInfo
argument_list|()
decl_stmt|;
name|colInfo
operator|.
name|setColumn
argument_list|(
name|column
argument_list|)
expr_stmt|;
name|colInfo
operator|.
name|setTabAlias
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|cond
operator|.
name|getBaseCols
argument_list|()
operator|.
name|add
argument_list|(
name|colInfo
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Get the expression string of an expression node.    */
specifier|public
specifier|static
name|String
name|getExprString
parameter_list|(
name|RowSchema
name|rs
parameter_list|,
name|ExprNodeDesc
name|expr
parameter_list|,
name|LineageCtx
name|lctx
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|inpOp
parameter_list|,
name|Predicate
name|cond
parameter_list|)
block|{
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
name|ExprNodeColumnDesc
name|col
init|=
operator|(
name|ExprNodeColumnDesc
operator|)
name|expr
decl_stmt|;
name|String
name|internalName
init|=
name|col
operator|.
name|getColumn
argument_list|()
decl_stmt|;
name|String
name|alias
init|=
name|internalName
decl_stmt|;
name|String
name|tabAlias
init|=
name|col
operator|.
name|getTabAlias
argument_list|()
decl_stmt|;
name|ColumnInfo
name|ci
init|=
name|rs
operator|.
name|getColumnInfo
argument_list|(
name|internalName
argument_list|)
decl_stmt|;
if|if
condition|(
name|ci
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|ci
operator|.
name|getAlias
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|alias
operator|=
name|ci
operator|.
name|getAlias
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|ci
operator|.
name|getTabAlias
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tabAlias
operator|=
name|ci
operator|.
name|getTabAlias
argument_list|()
expr_stmt|;
block|}
block|}
name|Dependency
name|dep
init|=
name|lctx
operator|.
name|getIndex
argument_list|()
operator|.
name|getDependency
argument_list|(
name|inpOp
argument_list|,
name|internalName
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|tabAlias
operator|==
literal|null
operator|||
name|tabAlias
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
operator|||
name|tabAlias
operator|.
name|startsWith
argument_list|(
literal|"$"
argument_list|)
operator|)
operator|&&
operator|(
name|dep
operator|!=
literal|null
operator|&&
name|dep
operator|.
name|getType
argument_list|()
operator|==
name|DependencyType
operator|.
name|SIMPLE
operator|)
condition|)
block|{
name|List
argument_list|<
name|BaseColumnInfo
argument_list|>
name|baseCols
init|=
name|dep
operator|.
name|getBaseCols
argument_list|()
decl_stmt|;
if|if
condition|(
name|baseCols
operator|!=
literal|null
operator|&&
operator|!
name|baseCols
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|BaseColumnInfo
name|baseCol
init|=
name|baseCols
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|tabAlias
operator|=
name|baseCol
operator|.
name|getTabAlias
argument_list|()
operator|.
name|getAlias
argument_list|()
expr_stmt|;
name|alias
operator|=
name|baseCol
operator|.
name|getColumn
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|tabAlias
operator|!=
literal|null
operator|&&
name|tabAlias
operator|.
name|length
argument_list|()
operator|>
literal|0
operator|&&
operator|!
name|tabAlias
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
operator|&&
operator|!
name|tabAlias
operator|.
name|startsWith
argument_list|(
literal|"$"
argument_list|)
condition|)
block|{
if|if
condition|(
name|cond
operator|!=
literal|null
operator|&&
operator|!
name|findSourceColumn
argument_list|(
name|lctx
argument_list|,
name|cond
argument_list|,
name|tabAlias
argument_list|,
name|alias
argument_list|)
operator|&&
name|dep
operator|!=
literal|null
condition|)
block|{
name|cond
operator|.
name|getBaseCols
argument_list|()
operator|.
name|addAll
argument_list|(
name|dep
operator|.
name|getBaseCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|tabAlias
operator|+
literal|"."
operator|+
name|alias
return|;
block|}
if|if
condition|(
name|dep
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|cond
operator|!=
literal|null
condition|)
block|{
name|cond
operator|.
name|getBaseCols
argument_list|()
operator|.
name|addAll
argument_list|(
name|dep
operator|.
name|getBaseCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|dep
operator|.
name|getExpr
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|dep
operator|.
name|getExpr
argument_list|()
return|;
block|}
block|}
if|if
condition|(
name|alias
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
condition|)
block|{
name|ci
operator|=
name|inpOp
operator|.
name|getSchema
argument_list|()
operator|.
name|getColumnInfo
argument_list|(
name|internalName
argument_list|)
expr_stmt|;
if|if
condition|(
name|ci
operator|!=
literal|null
operator|&&
name|ci
operator|.
name|getAlias
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|alias
operator|=
name|ci
operator|.
name|getAlias
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|alias
return|;
block|}
elseif|else
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeGenericFuncDesc
condition|)
block|{
name|ExprNodeGenericFuncDesc
name|func
init|=
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|expr
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
name|func
operator|.
name|getChildren
argument_list|()
decl_stmt|;
name|String
index|[]
name|childrenExprStrings
init|=
operator|new
name|String
index|[
name|children
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|childrenExprStrings
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|childrenExprStrings
index|[
name|i
index|]
operator|=
name|getExprString
argument_list|(
name|rs
argument_list|,
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|lctx
argument_list|,
name|inpOp
argument_list|,
name|cond
argument_list|)
expr_stmt|;
block|}
return|return
name|func
operator|.
name|getGenericUDF
argument_list|()
operator|.
name|getDisplayString
argument_list|(
name|childrenExprStrings
argument_list|)
return|;
block|}
return|return
name|expr
operator|.
name|getExprString
argument_list|()
return|;
block|}
comment|/**    * Gets the expression dependencies for the expression.    *    * @param lctx    *          The lineage context containing the input operators dependencies.    * @param inpOp    *          The input operator to the current operator.    * @param expr    *          The expression that is being processed.    * @throws SemanticException    */
specifier|public
specifier|static
name|Dependency
name|getExprDependency
parameter_list|(
name|LineageCtx
name|lctx
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|inpOp
parameter_list|,
name|ExprNodeDesc
name|expr
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// Create the walker, the rules dispatcher and the context.
name|ExprProcCtx
name|exprCtx
init|=
operator|new
name|ExprProcCtx
argument_list|(
name|lctx
argument_list|,
name|inpOp
argument_list|)
decl_stmt|;
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
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
name|ExprNodeColumnDesc
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|getColumnProcessor
argument_list|()
argument_list|)
expr_stmt|;
name|exprRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R2"
argument_list|,
name|ExprNodeFieldDesc
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|getFieldProcessor
argument_list|()
argument_list|)
expr_stmt|;
name|exprRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R3"
argument_list|,
name|ExprNodeGenericFuncDesc
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|getGenericFuncProcessor
argument_list|()
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
name|getDefaultExprProcessor
argument_list|()
argument_list|,
name|exprRules
argument_list|,
name|exprCtx
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
name|expr
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
operator|(
name|Dependency
operator|)
name|outputMap
operator|.
name|get
argument_list|(
name|expr
argument_list|)
return|;
block|}
block|}
end_class

end_unit

