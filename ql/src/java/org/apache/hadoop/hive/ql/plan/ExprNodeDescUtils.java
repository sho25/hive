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
name|plan
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
name|ExprNodeEvaluator
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
name|ExprNodeEvaluatorFactory
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
name|FunctionRegistry
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
name|UDF
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
name|GenericUDFBridge
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_class
specifier|public
class|class
name|ExprNodeDescUtils
block|{
specifier|public
specifier|static
name|int
name|indexOf
parameter_list|(
name|ExprNodeDesc
name|origin
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|sources
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|sources
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|origin
operator|.
name|isSame
argument_list|(
name|sources
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
return|return
name|i
return|;
block|}
block|}
return|return
operator|-
literal|1
return|;
block|}
comment|// traversing origin, find ExprNodeDesc in sources and replaces it with ExprNodeDesc
comment|// in targets having same index.
comment|// return null if failed to find
specifier|public
specifier|static
name|ExprNodeDesc
name|replace
parameter_list|(
name|ExprNodeDesc
name|origin
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|sources
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|targets
parameter_list|)
block|{
name|int
name|index
init|=
name|indexOf
argument_list|(
name|origin
argument_list|,
name|sources
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|>=
literal|0
condition|)
block|{
return|return
name|targets
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
comment|// encountered column or field which cannot be found in sources
if|if
condition|(
name|origin
operator|instanceof
name|ExprNodeColumnDesc
operator|||
name|origin
operator|instanceof
name|ExprNodeFieldDesc
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// for ExprNodeGenericFuncDesc, it should be deterministic and stateless
if|if
condition|(
name|origin
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
name|origin
decl_stmt|;
if|if
condition|(
operator|!
name|FunctionRegistry
operator|.
name|isDeterministic
argument_list|(
name|func
operator|.
name|getGenericUDF
argument_list|()
argument_list|)
operator|||
name|FunctionRegistry
operator|.
name|isStateful
argument_list|(
name|func
operator|.
name|getGenericUDF
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
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
name|origin
operator|.
name|getChildren
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ExprNodeDesc
name|child
init|=
name|replace
argument_list|(
name|origin
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|sources
argument_list|,
name|targets
argument_list|)
decl_stmt|;
if|if
condition|(
name|child
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|children
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
block|}
comment|// duplicate function with possibly replaced children
name|ExprNodeGenericFuncDesc
name|clone
init|=
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|func
operator|.
name|clone
argument_list|()
decl_stmt|;
name|clone
operator|.
name|setChildren
argument_list|(
name|children
argument_list|)
expr_stmt|;
return|return
name|clone
return|;
block|}
comment|// constant or null, just return it
return|return
name|origin
return|;
block|}
comment|/**    * return true if predicate is already included in source     */
specifier|public
specifier|static
name|boolean
name|containsPredicate
parameter_list|(
name|ExprNodeDesc
name|source
parameter_list|,
name|ExprNodeDesc
name|predicate
parameter_list|)
block|{
if|if
condition|(
name|source
operator|.
name|isSame
argument_list|(
name|predicate
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|FunctionRegistry
operator|.
name|isOpAnd
argument_list|(
name|source
argument_list|)
condition|)
block|{
if|if
condition|(
name|containsPredicate
argument_list|(
name|source
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|predicate
argument_list|)
operator|||
name|containsPredicate
argument_list|(
name|source
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|predicate
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * bind two predicates by AND op    */
specifier|public
specifier|static
name|ExprNodeGenericFuncDesc
name|mergePredicates
parameter_list|(
name|ExprNodeDesc
name|prev
parameter_list|,
name|ExprNodeDesc
name|next
parameter_list|)
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|children
operator|.
name|add
argument_list|(
name|prev
argument_list|)
expr_stmt|;
name|children
operator|.
name|add
argument_list|(
name|next
argument_list|)
expr_stmt|;
return|return
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|,
name|FunctionRegistry
operator|.
name|getGenericUDFForAnd
argument_list|()
argument_list|,
name|children
argument_list|)
return|;
block|}
comment|/**    * bind n predicates by AND op    */
specifier|public
specifier|static
name|ExprNodeDesc
name|mergePredicates
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|exprs
parameter_list|)
block|{
name|ExprNodeDesc
name|prev
init|=
literal|null
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|expr
range|:
name|exprs
control|)
block|{
if|if
condition|(
name|prev
operator|==
literal|null
condition|)
block|{
name|prev
operator|=
name|expr
expr_stmt|;
continue|continue;
block|}
name|prev
operator|=
name|mergePredicates
argument_list|(
name|prev
argument_list|,
name|expr
argument_list|)
expr_stmt|;
block|}
return|return
name|prev
return|;
block|}
comment|/**    * split predicates by AND op    */
specifier|public
specifier|static
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|split
parameter_list|(
name|ExprNodeDesc
name|current
parameter_list|)
block|{
return|return
name|split
argument_list|(
name|current
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * split predicates by AND op    */
specifier|public
specifier|static
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|split
parameter_list|(
name|ExprNodeDesc
name|current
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|splitted
parameter_list|)
block|{
if|if
condition|(
name|FunctionRegistry
operator|.
name|isOpAnd
argument_list|(
name|current
argument_list|)
condition|)
block|{
for|for
control|(
name|ExprNodeDesc
name|child
range|:
name|current
operator|.
name|getChildren
argument_list|()
control|)
block|{
name|split
argument_list|(
name|child
argument_list|,
name|splitted
argument_list|)
expr_stmt|;
block|}
return|return
name|splitted
return|;
block|}
if|if
condition|(
name|indexOf
argument_list|(
name|current
argument_list|,
name|splitted
argument_list|)
operator|<
literal|0
condition|)
block|{
name|splitted
operator|.
name|add
argument_list|(
name|current
argument_list|)
expr_stmt|;
block|}
return|return
name|splitted
return|;
block|}
comment|/**    * Recommend name for the expression    */
specifier|public
specifier|static
name|String
name|recommendInputName
parameter_list|(
name|ExprNodeDesc
name|desc
parameter_list|)
block|{
if|if
condition|(
name|desc
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
return|return
operator|(
operator|(
name|ExprNodeColumnDesc
operator|)
name|desc
operator|)
operator|.
name|getColumn
argument_list|()
return|;
block|}
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
name|desc
operator|.
name|getChildren
argument_list|()
decl_stmt|;
if|if
condition|(
name|FunctionRegistry
operator|.
name|isOpPreserveInputName
argument_list|(
name|desc
argument_list|)
operator|&&
operator|!
name|children
operator|.
name|isEmpty
argument_list|()
operator|&&
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
return|return
operator|(
operator|(
name|ExprNodeColumnDesc
operator|)
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|getColumn
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Return false if the expression has any non deterministic function    */
specifier|public
specifier|static
name|boolean
name|isDeterministic
parameter_list|(
name|ExprNodeDesc
name|desc
parameter_list|)
block|{
if|if
condition|(
name|desc
operator|instanceof
name|ExprNodeGenericFuncDesc
condition|)
block|{
if|if
condition|(
operator|!
name|FunctionRegistry
operator|.
name|isDeterministic
argument_list|(
operator|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|desc
operator|)
operator|.
name|getGenericUDF
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
if|if
condition|(
name|desc
operator|.
name|getChildren
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ExprNodeDesc
name|child
range|:
name|desc
operator|.
name|getChildren
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|isDeterministic
argument_list|(
name|child
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|public
specifier|static
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|clone
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|sources
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|expr
range|:
name|sources
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|expr
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Convert expressions in current operator to those in terminal operator, which    * is an ancestor of current or null (back to top operator).    */
specifier|public
specifier|static
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|backtrack
parameter_list|(
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|sources
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|current
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|terminal
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|expr
range|:
name|sources
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|backtrack
argument_list|(
name|expr
argument_list|,
name|current
argument_list|,
name|terminal
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|ExprNodeDesc
name|backtrack
parameter_list|(
name|ExprNodeDesc
name|source
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|current
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|terminal
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|parent
init|=
name|getSingleParent
argument_list|(
name|current
argument_list|,
name|terminal
argument_list|)
decl_stmt|;
if|if
condition|(
name|parent
operator|==
literal|null
condition|)
block|{
return|return
name|source
return|;
block|}
if|if
condition|(
name|source
operator|instanceof
name|ExprNodeGenericFuncDesc
condition|)
block|{
comment|// all children expression should be resolved
name|ExprNodeGenericFuncDesc
name|function
init|=
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|source
operator|.
name|clone
argument_list|()
decl_stmt|;
name|function
operator|.
name|setChildren
argument_list|(
name|backtrack
argument_list|(
name|function
operator|.
name|getChildren
argument_list|()
argument_list|,
name|current
argument_list|,
name|terminal
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|function
return|;
block|}
if|if
condition|(
name|source
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
name|ExprNodeColumnDesc
name|column
init|=
operator|(
name|ExprNodeColumnDesc
operator|)
name|source
decl_stmt|;
return|return
name|backtrack
argument_list|(
name|column
argument_list|,
name|parent
argument_list|,
name|terminal
argument_list|)
return|;
block|}
if|if
condition|(
name|source
operator|instanceof
name|ExprNodeFieldDesc
condition|)
block|{
comment|// field expression should be resolved
name|ExprNodeFieldDesc
name|field
init|=
operator|(
name|ExprNodeFieldDesc
operator|)
name|source
operator|.
name|clone
argument_list|()
decl_stmt|;
name|field
operator|.
name|setDesc
argument_list|(
name|backtrack
argument_list|(
name|field
operator|.
name|getDesc
argument_list|()
argument_list|,
name|current
argument_list|,
name|terminal
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|field
return|;
block|}
comment|// constant or null expr, just return
return|return
name|source
return|;
block|}
comment|// Resolve column expression to input expression by using expression mapping in current operator
specifier|private
specifier|static
name|ExprNodeDesc
name|backtrack
parameter_list|(
name|ExprNodeColumnDesc
name|column
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|current
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|terminal
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|mapping
init|=
name|current
operator|.
name|getColumnExprMap
argument_list|()
decl_stmt|;
if|if
condition|(
name|mapping
operator|==
literal|null
condition|)
block|{
return|return
name|backtrack
argument_list|(
operator|(
name|ExprNodeDesc
operator|)
name|column
argument_list|,
name|current
argument_list|,
name|terminal
argument_list|)
return|;
block|}
name|ExprNodeDesc
name|mapped
init|=
name|mapping
operator|.
name|get
argument_list|(
name|column
operator|.
name|getColumn
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|mapped
operator|==
literal|null
condition|?
literal|null
else|:
name|backtrack
argument_list|(
name|mapped
argument_list|,
name|current
argument_list|,
name|terminal
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Operator
argument_list|<
name|?
argument_list|>
name|getSingleParent
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|current
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|terminal
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|current
operator|==
name|terminal
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|parents
init|=
name|current
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
if|if
condition|(
name|parents
operator|==
literal|null
operator|||
name|parents
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|terminal
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Failed to meet terminal operator"
argument_list|)
throw|;
block|}
return|return
literal|null
return|;
block|}
if|if
condition|(
name|parents
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
name|parents
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
if|if
condition|(
name|terminal
operator|!=
literal|null
operator|&&
name|parents
operator|.
name|contains
argument_list|(
name|terminal
argument_list|)
condition|)
block|{
return|return
name|terminal
return|;
block|}
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Met multiple parent operators"
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|ExprNodeDesc
index|[]
name|extractComparePair
parameter_list|(
name|ExprNodeDesc
name|expr1
parameter_list|,
name|ExprNodeDesc
name|expr2
parameter_list|)
block|{
name|expr1
operator|=
name|extractConstant
argument_list|(
name|expr1
argument_list|)
expr_stmt|;
name|expr2
operator|=
name|extractConstant
argument_list|(
name|expr2
argument_list|)
expr_stmt|;
if|if
condition|(
name|expr1
operator|instanceof
name|ExprNodeColumnDesc
operator|&&
name|expr2
operator|instanceof
name|ExprNodeConstantDesc
condition|)
block|{
return|return
operator|new
name|ExprNodeDesc
index|[]
block|{
name|expr1
block|,
name|expr2
block|}
return|;
block|}
if|if
condition|(
name|expr1
operator|instanceof
name|ExprNodeConstantDesc
operator|&&
name|expr2
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
return|return
operator|new
name|ExprNodeDesc
index|[]
block|{
name|expr1
block|,
name|expr2
block|}
return|;
block|}
comment|// handles cases where the query has a predicate "column-name=constant"
if|if
condition|(
name|expr1
operator|instanceof
name|ExprNodeFieldDesc
operator|&&
name|expr2
operator|instanceof
name|ExprNodeConstantDesc
condition|)
block|{
name|ExprNodeColumnDesc
name|columnDesc
init|=
name|extractColumn
argument_list|(
name|expr1
argument_list|)
decl_stmt|;
return|return
name|columnDesc
operator|!=
literal|null
condition|?
operator|new
name|ExprNodeDesc
index|[]
block|{
name|columnDesc
block|,
name|expr2
block|,
name|expr1
block|}
else|:
literal|null
return|;
block|}
comment|// handles cases where the query has a predicate "constant=column-name"
if|if
condition|(
name|expr1
operator|instanceof
name|ExprNodeConstantDesc
operator|&&
name|expr2
operator|instanceof
name|ExprNodeFieldDesc
condition|)
block|{
name|ExprNodeColumnDesc
name|columnDesc
init|=
name|extractColumn
argument_list|(
name|expr2
argument_list|)
decl_stmt|;
return|return
name|columnDesc
operator|!=
literal|null
condition|?
operator|new
name|ExprNodeDesc
index|[]
block|{
name|expr1
block|,
name|columnDesc
block|,
name|expr2
block|}
else|:
literal|null
return|;
block|}
comment|// todo: constant op constant
return|return
literal|null
return|;
block|}
comment|/**    * Extract fields from the given {@link ExprNodeFieldDesc node descriptor}    * */
specifier|public
specifier|static
name|String
index|[]
name|extractFields
parameter_list|(
name|ExprNodeFieldDesc
name|expr
parameter_list|)
block|{
return|return
name|extractFields
argument_list|(
name|expr
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
return|;
block|}
comment|/*    * Recursively extract fields from ExprNodeDesc. Deeply nested structs can have multiple levels of    * fields in them    */
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|extractFields
parameter_list|(
name|ExprNodeDesc
name|expr
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|fields
parameter_list|)
block|{
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeFieldDesc
condition|)
block|{
name|ExprNodeFieldDesc
name|field
init|=
operator|(
name|ExprNodeFieldDesc
operator|)
name|expr
decl_stmt|;
name|fields
operator|.
name|add
argument_list|(
name|field
operator|.
name|getFieldName
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|extractFields
argument_list|(
name|field
operator|.
name|getDesc
argument_list|()
argument_list|,
name|fields
argument_list|)
return|;
block|}
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
return|return
name|fields
return|;
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unexpected exception while extracting fields from ExprNodeDesc"
argument_list|)
throw|;
block|}
comment|/*    * Extract column from the given ExprNodeDesc    */
specifier|private
specifier|static
name|ExprNodeColumnDesc
name|extractColumn
parameter_list|(
name|ExprNodeDesc
name|expr
parameter_list|)
block|{
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
return|return
operator|(
name|ExprNodeColumnDesc
operator|)
name|expr
return|;
block|}
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeFieldDesc
condition|)
block|{
return|return
name|extractColumn
argument_list|(
operator|(
operator|(
name|ExprNodeFieldDesc
operator|)
name|expr
operator|)
operator|.
name|getDesc
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
comment|// from IndexPredicateAnalyzer
specifier|private
specifier|static
name|ExprNodeDesc
name|extractConstant
parameter_list|(
name|ExprNodeDesc
name|expr
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|expr
operator|instanceof
name|ExprNodeGenericFuncDesc
operator|)
condition|)
block|{
return|return
name|expr
return|;
block|}
name|ExprNodeConstantDesc
name|folded
init|=
name|foldConstant
argument_list|(
operator|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|expr
operator|)
argument_list|)
decl_stmt|;
return|return
name|folded
operator|==
literal|null
condition|?
name|expr
else|:
name|folded
return|;
block|}
specifier|private
specifier|static
name|ExprNodeConstantDesc
name|foldConstant
parameter_list|(
name|ExprNodeGenericFuncDesc
name|func
parameter_list|)
block|{
name|GenericUDF
name|udf
init|=
name|func
operator|.
name|getGenericUDF
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|FunctionRegistry
operator|.
name|isDeterministic
argument_list|(
name|udf
argument_list|)
operator|||
name|FunctionRegistry
operator|.
name|isStateful
argument_list|(
name|udf
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
try|try
block|{
comment|// If the UDF depends on any external resources, we can't fold because the
comment|// resources may not be available at compile time.
if|if
condition|(
name|udf
operator|instanceof
name|GenericUDFBridge
condition|)
block|{
name|UDF
name|internal
init|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
operator|(
operator|(
name|GenericUDFBridge
operator|)
name|udf
operator|)
operator|.
name|getUdfClass
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|internal
operator|.
name|getRequiredFiles
argument_list|()
operator|!=
literal|null
operator|||
name|internal
operator|.
name|getRequiredJars
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|udf
operator|.
name|getRequiredFiles
argument_list|()
operator|!=
literal|null
operator|||
name|udf
operator|.
name|getRequiredJars
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
if|if
condition|(
name|func
operator|.
name|getChildren
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ExprNodeDesc
name|child
range|:
name|func
operator|.
name|getChildren
argument_list|()
control|)
block|{
if|if
condition|(
name|child
operator|instanceof
name|ExprNodeConstantDesc
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|child
operator|instanceof
name|ExprNodeGenericFuncDesc
condition|)
block|{
if|if
condition|(
name|foldConstant
argument_list|(
operator|(
name|ExprNodeGenericFuncDesc
operator|)
name|child
argument_list|)
operator|!=
literal|null
condition|)
block|{
continue|continue;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
name|ExprNodeEvaluator
name|evaluator
init|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|func
argument_list|)
decl_stmt|;
name|ObjectInspector
name|output
init|=
name|evaluator
operator|.
name|initialize
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|Object
name|constant
init|=
name|evaluator
operator|.
name|evaluate
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|Object
name|java
init|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardJavaObject
argument_list|(
name|constant
argument_list|,
name|output
argument_list|)
decl_stmt|;
return|return
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|java
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

