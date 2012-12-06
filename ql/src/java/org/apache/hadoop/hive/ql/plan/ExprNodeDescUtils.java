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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
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
comment|// duplicate function with possibily replaced children
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
name|setChildExprs
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
comment|/**    * bind two predicates by AND op     */
specifier|public
specifier|static
name|ExprNodeDesc
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
block|}
end_class

end_unit

