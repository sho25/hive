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
name|optiq
operator|.
name|translator
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
name|Collection
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
name|ErrorMsg
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
name|FunctionInfo
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
name|ASTNode
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
name|BaseSemanticAnalyzer
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
name|HiveParser
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
name|parse
operator|.
name|TypeCheckCtx
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
name|TypeCheckProcFactory
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
name|ExprNodeDescUtils
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDF
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFBaseCompare
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFOPAnd
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDFOPOr
import|;
end_import

begin_comment
comment|/**  * JoinCondTypeCheckProcFactory is used by Optiq planner(CBO) to generate Join Conditions from Join Condition AST.  * Reasons for sub class:  * 1. Additional restrictions on what is supported in Join Conditions  * 2. Column handling is different  * 3. Join Condn expr has two input RR as opposed to one.  */
end_comment

begin_comment
comment|/**  * TODO:<br>  * 1. Could we use combined RR instead of list of RR ?<br>  * 2. Use Column Processing from TypeCheckProcFactory<br>  * 3. Why not use GB expr ?  */
end_comment

begin_class
specifier|public
class|class
name|JoinCondTypeCheckProcFactory
extends|extends
name|TypeCheckProcFactory
block|{
specifier|public
specifier|static
name|Map
argument_list|<
name|ASTNode
argument_list|,
name|ExprNodeDesc
argument_list|>
name|genExprNode
parameter_list|(
name|ASTNode
name|expr
parameter_list|,
name|TypeCheckCtx
name|tcCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
name|TypeCheckProcFactory
operator|.
name|genExprNode
argument_list|(
name|expr
argument_list|,
name|tcCtx
argument_list|,
operator|new
name|JoinCondTypeCheckProcFactory
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Processor for table columns.    */
specifier|public
specifier|static
class|class
name|JoinCondColumnExprProcessor
extends|extends
name|ColumnExprProcessor
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
name|JoinTypeCheckCtx
name|ctx
init|=
operator|(
name|JoinTypeCheckCtx
operator|)
name|procCtx
decl_stmt|;
if|if
condition|(
name|ctx
operator|.
name|getError
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|ASTNode
name|expr
init|=
operator|(
name|ASTNode
operator|)
name|nd
decl_stmt|;
name|ASTNode
name|parent
init|=
name|stack
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|?
operator|(
name|ASTNode
operator|)
name|stack
operator|.
name|get
argument_list|(
name|stack
operator|.
name|size
argument_list|()
operator|-
literal|2
argument_list|)
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|expr
operator|.
name|getType
argument_list|()
operator|!=
name|HiveParser
operator|.
name|TOK_TABLE_OR_COL
condition|)
block|{
name|ctx
operator|.
name|setError
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_COLUMN
operator|.
name|getMsg
argument_list|(
name|expr
argument_list|)
argument_list|,
name|expr
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
assert|assert
operator|(
name|expr
operator|.
name|getChildCount
argument_list|()
operator|==
literal|1
operator|)
assert|;
name|String
name|tableOrCol
init|=
name|BaseSemanticAnalyzer
operator|.
name|unescapeIdentifier
argument_list|(
name|expr
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|qualifiedAccess
init|=
operator|(
name|parent
operator|!=
literal|null
operator|&&
name|parent
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|DOT
operator|)
decl_stmt|;
name|ColumnInfo
name|colInfo
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|qualifiedAccess
condition|)
block|{
name|colInfo
operator|=
name|getColInfo
argument_list|(
name|ctx
argument_list|,
literal|null
argument_list|,
name|tableOrCol
argument_list|,
name|expr
argument_list|)
expr_stmt|;
comment|// It's a column.
return|return
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|colInfo
operator|.
name|getType
argument_list|()
argument_list|,
name|colInfo
operator|.
name|getInternalName
argument_list|()
argument_list|,
name|colInfo
operator|.
name|getTabAlias
argument_list|()
argument_list|,
name|colInfo
operator|.
name|getIsVirtualCol
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|hasTableAlias
argument_list|(
name|ctx
argument_list|,
name|tableOrCol
argument_list|,
name|expr
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_COLUMN
operator|.
name|getMsg
argument_list|(
name|expr
argument_list|)
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|boolean
name|hasTableAlias
parameter_list|(
name|JoinTypeCheckCtx
name|ctx
parameter_list|,
name|String
name|tabName
parameter_list|,
name|ASTNode
name|expr
parameter_list|)
throws|throws
name|SemanticException
block|{
name|int
name|tblAliasCnt
init|=
literal|0
decl_stmt|;
for|for
control|(
name|RowResolver
name|rr
range|:
name|ctx
operator|.
name|getInputRRList
argument_list|()
control|)
block|{
if|if
condition|(
name|rr
operator|.
name|hasTableAlias
argument_list|(
name|tabName
argument_list|)
condition|)
name|tblAliasCnt
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|tblAliasCnt
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_JOIN_CONDITION_1
operator|.
name|getMsg
argument_list|(
name|expr
argument_list|)
argument_list|)
throw|;
block|}
return|return
operator|(
name|tblAliasCnt
operator|==
literal|1
operator|)
condition|?
literal|true
else|:
literal|false
return|;
block|}
specifier|private
specifier|static
name|ColumnInfo
name|getColInfo
parameter_list|(
name|JoinTypeCheckCtx
name|ctx
parameter_list|,
name|String
name|tabName
parameter_list|,
name|String
name|colAlias
parameter_list|,
name|ASTNode
name|expr
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ColumnInfo
name|tmp
decl_stmt|;
name|ColumnInfo
name|cInfoToRet
init|=
literal|null
decl_stmt|;
for|for
control|(
name|RowResolver
name|rr
range|:
name|ctx
operator|.
name|getInputRRList
argument_list|()
control|)
block|{
name|tmp
operator|=
name|rr
operator|.
name|get
argument_list|(
name|tabName
argument_list|,
name|colAlias
argument_list|)
expr_stmt|;
if|if
condition|(
name|tmp
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|cInfoToRet
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_JOIN_CONDITION_1
operator|.
name|getMsg
argument_list|(
name|expr
argument_list|)
argument_list|)
throw|;
block|}
name|cInfoToRet
operator|=
name|tmp
expr_stmt|;
block|}
block|}
return|return
name|cInfoToRet
return|;
block|}
block|}
comment|/**    * Factory method to get ColumnExprProcessor.    *     * @return ColumnExprProcessor.    */
annotation|@
name|Override
specifier|public
name|ColumnExprProcessor
name|getColumnExprProcessor
parameter_list|()
block|{
return|return
operator|new
name|JoinCondColumnExprProcessor
argument_list|()
return|;
block|}
comment|/**    * The default processor for typechecking.    */
specifier|public
specifier|static
class|class
name|JoinCondDefaultExprProcessor
extends|extends
name|DefaultExprProcessor
block|{
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|String
argument_list|>
name|getReferenceableColumnAliases
parameter_list|(
name|TypeCheckCtx
name|ctx
parameter_list|)
block|{
name|JoinTypeCheckCtx
name|jCtx
init|=
operator|(
name|JoinTypeCheckCtx
operator|)
name|ctx
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|possibleColumnNames
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
name|RowResolver
name|rr
range|:
name|jCtx
operator|.
name|getInputRRList
argument_list|()
control|)
block|{
name|possibleColumnNames
operator|.
name|addAll
argument_list|(
name|rr
operator|.
name|getReferenceableColumnAliases
argument_list|(
literal|null
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|possibleColumnNames
return|;
block|}
annotation|@
name|Override
specifier|protected
name|ExprNodeColumnDesc
name|processQualifiedColRef
parameter_list|(
name|TypeCheckCtx
name|ctx
parameter_list|,
name|ASTNode
name|expr
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
name|tableAlias
init|=
name|BaseSemanticAnalyzer
operator|.
name|unescapeIdentifier
argument_list|(
name|expr
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
decl_stmt|;
comment|// NOTE: tableAlias must be a valid non-ambiguous table alias,
comment|// because we've checked that in TOK_TABLE_OR_COL's process method.
name|ColumnInfo
name|colInfo
init|=
name|getColInfo
argument_list|(
operator|(
name|JoinTypeCheckCtx
operator|)
name|ctx
argument_list|,
name|tableAlias
argument_list|,
operator|(
operator|(
name|ExprNodeConstantDesc
operator|)
name|nodeOutputs
index|[
literal|1
index|]
operator|)
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|expr
argument_list|)
decl_stmt|;
if|if
condition|(
name|colInfo
operator|==
literal|null
condition|)
block|{
name|ctx
operator|.
name|setError
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_COLUMN
operator|.
name|getMsg
argument_list|(
name|expr
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|,
name|expr
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|colInfo
operator|.
name|getType
argument_list|()
argument_list|,
name|colInfo
operator|.
name|getInternalName
argument_list|()
argument_list|,
name|tableAlias
argument_list|,
name|colInfo
operator|.
name|getIsVirtualCol
argument_list|()
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|ColumnInfo
name|getColInfo
parameter_list|(
name|JoinTypeCheckCtx
name|ctx
parameter_list|,
name|String
name|tabName
parameter_list|,
name|String
name|colAlias
parameter_list|,
name|ASTNode
name|expr
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ColumnInfo
name|tmp
decl_stmt|;
name|ColumnInfo
name|cInfoToRet
init|=
literal|null
decl_stmt|;
for|for
control|(
name|RowResolver
name|rr
range|:
name|ctx
operator|.
name|getInputRRList
argument_list|()
control|)
block|{
name|tmp
operator|=
name|rr
operator|.
name|get
argument_list|(
name|tabName
argument_list|,
name|colAlias
argument_list|)
expr_stmt|;
if|if
condition|(
name|tmp
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|cInfoToRet
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_JOIN_CONDITION_1
operator|.
name|getMsg
argument_list|(
name|expr
argument_list|)
argument_list|)
throw|;
block|}
name|cInfoToRet
operator|=
name|tmp
expr_stmt|;
block|}
block|}
return|return
name|cInfoToRet
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|validateUDF
parameter_list|(
name|ASTNode
name|expr
parameter_list|,
name|boolean
name|isFunction
parameter_list|,
name|TypeCheckCtx
name|ctx
parameter_list|,
name|FunctionInfo
name|fi
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
parameter_list|,
name|GenericUDF
name|genericUDF
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
operator|.
name|validateUDF
argument_list|(
name|expr
argument_list|,
name|isFunction
argument_list|,
name|ctx
argument_list|,
name|fi
argument_list|,
name|children
argument_list|,
name|genericUDF
argument_list|)
expr_stmt|;
name|JoinTypeCheckCtx
name|jCtx
init|=
operator|(
name|JoinTypeCheckCtx
operator|)
name|ctx
decl_stmt|;
comment|// Join Condition can not contain disjunctions
if|if
condition|(
name|genericUDF
operator|instanceof
name|GenericUDFOPOr
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_JOIN_CONDITION_3
operator|.
name|getMsg
argument_list|(
name|expr
argument_list|)
argument_list|)
throw|;
block|}
comment|// Non Conjunctive elements have further limitations in Join conditions
if|if
condition|(
operator|!
operator|(
name|genericUDF
operator|instanceof
name|GenericUDFOPAnd
operator|)
condition|)
block|{
comment|// Non Comparison UDF other than 'and' can not use inputs from both side
if|if
condition|(
operator|!
operator|(
name|genericUDF
operator|instanceof
name|GenericUDFBaseCompare
operator|)
condition|)
block|{
if|if
condition|(
name|genericUDFargsRefersToBothInput
argument_list|(
name|genericUDF
argument_list|,
name|children
argument_list|,
name|jCtx
operator|.
name|getInputRRList
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_JOIN_CONDITION_1
operator|.
name|getMsg
argument_list|(
name|expr
argument_list|)
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|genericUDF
operator|instanceof
name|GenericUDFBaseCompare
condition|)
block|{
comment|// Comparisons of non literals LHS/RHS can not refer to inputs from
comment|// both sides
if|if
condition|(
name|children
operator|.
name|size
argument_list|()
operator|==
literal|2
operator|&&
operator|!
operator|(
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|ExprNodeConstantDesc
operator|)
operator|&&
operator|!
operator|(
name|children
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|instanceof
name|ExprNodeConstantDesc
operator|)
condition|)
block|{
if|if
condition|(
name|comparisonUDFargsRefersToBothInput
argument_list|(
operator|(
name|GenericUDFBaseCompare
operator|)
name|genericUDF
argument_list|,
name|children
argument_list|,
name|jCtx
operator|.
name|getInputRRList
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_JOIN_CONDITION_1
operator|.
name|getMsg
argument_list|(
name|expr
argument_list|)
argument_list|)
throw|;
block|}
block|}
block|}
block|}
block|}
specifier|private
specifier|static
name|boolean
name|genericUDFargsRefersToBothInput
parameter_list|(
name|GenericUDF
name|udf
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
parameter_list|,
name|List
argument_list|<
name|RowResolver
argument_list|>
name|inputRRList
parameter_list|)
block|{
name|boolean
name|argsRefersToBothInput
init|=
literal|false
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|ExprNodeDesc
argument_list|>
name|hasCodeToColDescMap
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|child
range|:
name|children
control|)
block|{
name|ExprNodeDescUtils
operator|.
name|getExprNodeColumnDesc
argument_list|(
name|child
argument_list|,
name|hasCodeToColDescMap
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|Integer
argument_list|>
name|inputRef
init|=
name|getInputRef
argument_list|(
name|hasCodeToColDescMap
operator|.
name|values
argument_list|()
argument_list|,
name|inputRRList
argument_list|)
decl_stmt|;
if|if
condition|(
name|inputRef
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
name|argsRefersToBothInput
operator|=
literal|true
expr_stmt|;
return|return
name|argsRefersToBothInput
return|;
block|}
specifier|private
specifier|static
name|boolean
name|comparisonUDFargsRefersToBothInput
parameter_list|(
name|GenericUDFBaseCompare
name|comparisonUDF
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
parameter_list|,
name|List
argument_list|<
name|RowResolver
argument_list|>
name|inputRRList
parameter_list|)
block|{
name|boolean
name|argsRefersToBothInput
init|=
literal|false
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|ExprNodeDesc
argument_list|>
name|lhsHashCodeToColDescMap
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|ExprNodeDesc
argument_list|>
name|rhsHashCodeToColDescMap
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|ExprNodeDescUtils
operator|.
name|getExprNodeColumnDesc
argument_list|(
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|lhsHashCodeToColDescMap
argument_list|)
expr_stmt|;
name|ExprNodeDescUtils
operator|.
name|getExprNodeColumnDesc
argument_list|(
name|children
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|rhsHashCodeToColDescMap
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|Integer
argument_list|>
name|lhsInputRef
init|=
name|getInputRef
argument_list|(
name|lhsHashCodeToColDescMap
operator|.
name|values
argument_list|()
argument_list|,
name|inputRRList
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Integer
argument_list|>
name|rhsInputRef
init|=
name|getInputRef
argument_list|(
name|rhsHashCodeToColDescMap
operator|.
name|values
argument_list|()
argument_list|,
name|inputRRList
argument_list|)
decl_stmt|;
if|if
condition|(
name|lhsInputRef
operator|.
name|size
argument_list|()
operator|>
literal|1
operator|||
name|rhsInputRef
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
name|argsRefersToBothInput
operator|=
literal|true
expr_stmt|;
return|return
name|argsRefersToBothInput
return|;
block|}
specifier|private
specifier|static
name|Set
argument_list|<
name|Integer
argument_list|>
name|getInputRef
parameter_list|(
name|Collection
argument_list|<
name|ExprNodeDesc
argument_list|>
name|colDescSet
parameter_list|,
name|List
argument_list|<
name|RowResolver
argument_list|>
name|inputRRList
parameter_list|)
block|{
name|String
name|tableAlias
decl_stmt|;
name|RowResolver
name|inputRR
decl_stmt|;
name|Set
argument_list|<
name|Integer
argument_list|>
name|inputLineage
init|=
operator|new
name|HashSet
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|col
range|:
name|colDescSet
control|)
block|{
name|ExprNodeColumnDesc
name|colDesc
init|=
operator|(
name|ExprNodeColumnDesc
operator|)
name|col
decl_stmt|;
name|tableAlias
operator|=
name|colDesc
operator|.
name|getTabAlias
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|inputRRList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|inputRR
operator|=
name|inputRRList
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
comment|// If table Alias is present check if InputRR has that table and then
comment|// check for internal name
comment|// else if table alias is null then check with internal name in all
comment|// inputRR.
if|if
condition|(
name|tableAlias
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|inputRR
operator|.
name|hasTableAlias
argument_list|(
name|tableAlias
argument_list|)
condition|)
block|{
if|if
condition|(
name|inputRR
operator|.
name|getInvRslvMap
argument_list|()
operator|.
name|containsKey
argument_list|(
name|colDesc
operator|.
name|getColumn
argument_list|()
argument_list|)
condition|)
block|{
name|inputLineage
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|inputRR
operator|.
name|getInvRslvMap
argument_list|()
operator|.
name|containsKey
argument_list|(
name|colDesc
operator|.
name|getColumn
argument_list|()
argument_list|)
condition|)
block|{
name|inputLineage
operator|.
name|add
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|inputLineage
return|;
block|}
block|}
comment|/**    * Factory method to get DefaultExprProcessor.    *     * @return DefaultExprProcessor.    */
annotation|@
name|Override
specifier|public
name|DefaultExprProcessor
name|getDefaultExprProcessor
parameter_list|()
block|{
return|return
operator|new
name|JoinCondDefaultExprProcessor
argument_list|()
return|;
block|}
block|}
end_class

end_unit

