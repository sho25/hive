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
name|lang
operator|.
name|reflect
operator|.
name|Array
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
name|Arrays
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

begin_comment
comment|/**  * Join operator Descriptor implementation.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Join Operator"
argument_list|)
specifier|public
class|class
name|JoinDesc
extends|extends
name|AbstractOperatorDesc
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|INNER_JOIN
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|LEFT_OUTER_JOIN
init|=
literal|1
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|RIGHT_OUTER_JOIN
init|=
literal|2
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|FULL_OUTER_JOIN
init|=
literal|3
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|UNIQUE_JOIN
init|=
literal|4
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|LEFT_SEMI_JOIN
init|=
literal|5
decl_stmt|;
comment|// used to handle skew join
specifier|private
name|boolean
name|handleSkewJoin
init|=
literal|false
decl_stmt|;
specifier|private
name|int
name|skewKeyDefinition
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|bigKeysDirMap
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|>
name|smallKeysDirMap
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|TableDesc
argument_list|>
name|skewKeysValuesTables
decl_stmt|;
comment|// alias to key mapping
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|exprs
decl_stmt|;
comment|// alias to filter mapping
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|filters
decl_stmt|;
comment|// pos of outer join alias=<pos of other alias:num of filters on outer join alias>xn
comment|// for example,
comment|// a left outer join b on a.k=b.k AND a.k>5 full outer join c on a.k=c.k AND a.k>10 AND c.k>20
comment|//
comment|// That means on a(pos=0), there are overlapped filters associated with b(pos=1) and c(pos=2).
comment|// (a)b has one filter on a (a.k>5) and (a)c also has one filter on a (a.k>10),
comment|// making filter map for a as 0=1:1:2:1.
comment|// C also has one outer join filter associated with A(c.k>20), which is making 2=0:1
specifier|private
name|int
index|[]
index|[]
name|filterMap
decl_stmt|;
comment|// key index to nullsafe join flag
specifier|private
name|boolean
index|[]
name|nullsafes
decl_stmt|;
comment|// used for create joinOutputObjectInspector
specifier|protected
name|List
argument_list|<
name|String
argument_list|>
name|outputColumnNames
decl_stmt|;
comment|// key:column output name, value:tag
specifier|private
specifier|transient
name|Map
argument_list|<
name|String
argument_list|,
name|Byte
argument_list|>
name|reversedExprs
decl_stmt|;
comment|// No outer join involved
specifier|protected
name|boolean
name|noOuterJoin
decl_stmt|;
specifier|protected
name|JoinCondDesc
index|[]
name|conds
decl_stmt|;
specifier|protected
name|Byte
index|[]
name|tagOrder
decl_stmt|;
specifier|private
name|TableDesc
name|keyTableDesc
decl_stmt|;
specifier|public
name|JoinDesc
parameter_list|()
block|{   }
specifier|public
name|JoinDesc
parameter_list|(
specifier|final
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|exprs
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|outputColumnNames
parameter_list|,
specifier|final
name|boolean
name|noOuterJoin
parameter_list|,
specifier|final
name|JoinCondDesc
index|[]
name|conds
parameter_list|,
specifier|final
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|filters
parameter_list|)
block|{
name|this
operator|.
name|exprs
operator|=
name|exprs
expr_stmt|;
name|this
operator|.
name|outputColumnNames
operator|=
name|outputColumnNames
expr_stmt|;
name|this
operator|.
name|noOuterJoin
operator|=
name|noOuterJoin
expr_stmt|;
name|this
operator|.
name|conds
operator|=
name|conds
expr_stmt|;
name|this
operator|.
name|filters
operator|=
name|filters
expr_stmt|;
name|resetOrder
argument_list|()
expr_stmt|;
block|}
comment|// called by late-MapJoin processor (hive.auto.convert.join=true for example)
specifier|public
name|void
name|resetOrder
parameter_list|()
block|{
name|tagOrder
operator|=
operator|new
name|Byte
index|[
name|exprs
operator|.
name|size
argument_list|()
index|]
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
name|tagOrder
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|tagOrder
index|[
name|i
index|]
operator|=
operator|(
name|byte
operator|)
name|i
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|clone
parameter_list|()
block|{
name|JoinDesc
name|ret
init|=
operator|new
name|JoinDesc
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|cloneExprs
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|cloneExprs
operator|.
name|putAll
argument_list|(
name|getExprs
argument_list|()
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setExprs
argument_list|(
name|cloneExprs
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|cloneFilters
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|cloneFilters
operator|.
name|putAll
argument_list|(
name|getFilters
argument_list|()
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setFilters
argument_list|(
name|cloneFilters
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setConds
argument_list|(
name|getConds
argument_list|()
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setNoOuterJoin
argument_list|(
name|getNoOuterJoin
argument_list|()
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setNullSafes
argument_list|(
name|getNullSafes
argument_list|()
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setHandleSkewJoin
argument_list|(
name|handleSkewJoin
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setSkewKeyDefinition
argument_list|(
name|getSkewKeyDefinition
argument_list|()
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setTagOrder
argument_list|(
name|getTagOrder
argument_list|()
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|getKeyTableDesc
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|ret
operator|.
name|setKeyTableDesc
argument_list|(
operator|(
name|TableDesc
operator|)
name|getKeyTableDesc
argument_list|()
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getBigKeysDirMap
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|cloneBigKeysDirMap
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|cloneBigKeysDirMap
operator|.
name|putAll
argument_list|(
name|getBigKeysDirMap
argument_list|()
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setBigKeysDirMap
argument_list|(
name|cloneBigKeysDirMap
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getSmallKeysDirMap
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|Byte
argument_list|,
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|>
name|cloneSmallKeysDirMap
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|cloneSmallKeysDirMap
operator|.
name|putAll
argument_list|(
name|getSmallKeysDirMap
argument_list|()
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setSmallKeysDirMap
argument_list|(
name|cloneSmallKeysDirMap
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getSkewKeysValuesTables
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|Byte
argument_list|,
name|TableDesc
argument_list|>
name|cloneSkewKeysValuesTables
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|TableDesc
argument_list|>
argument_list|()
decl_stmt|;
name|cloneSkewKeysValuesTables
operator|.
name|putAll
argument_list|(
name|getSkewKeysValuesTables
argument_list|()
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setSkewKeysValuesTables
argument_list|(
name|cloneSkewKeysValuesTables
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getOutputColumnNames
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|cloneOutputColumnNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|cloneOutputColumnNames
operator|.
name|addAll
argument_list|(
name|getOutputColumnNames
argument_list|()
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setOutputColumnNames
argument_list|(
name|cloneOutputColumnNames
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getReversedExprs
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Byte
argument_list|>
name|cloneReversedExprs
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Byte
argument_list|>
argument_list|()
decl_stmt|;
name|cloneReversedExprs
operator|.
name|putAll
argument_list|(
name|getReversedExprs
argument_list|()
argument_list|)
expr_stmt|;
name|ret
operator|.
name|setReversedExprs
argument_list|(
name|cloneReversedExprs
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|public
name|JoinDesc
parameter_list|(
specifier|final
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|exprs
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|outputColumnNames
parameter_list|,
specifier|final
name|boolean
name|noOuterJoin
parameter_list|,
specifier|final
name|JoinCondDesc
index|[]
name|conds
parameter_list|)
block|{
name|this
argument_list|(
name|exprs
argument_list|,
name|outputColumnNames
argument_list|,
name|noOuterJoin
argument_list|,
name|conds
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|JoinDesc
parameter_list|(
specifier|final
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|exprs
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|outputColumnNames
parameter_list|)
block|{
name|this
argument_list|(
name|exprs
argument_list|,
name|outputColumnNames
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|JoinDesc
parameter_list|(
specifier|final
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|exprs
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|outputColumnNames
parameter_list|,
specifier|final
name|JoinCondDesc
index|[]
name|conds
parameter_list|)
block|{
name|this
argument_list|(
name|exprs
argument_list|,
name|outputColumnNames
argument_list|,
literal|true
argument_list|,
name|conds
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|JoinDesc
parameter_list|(
name|JoinDesc
name|clone
parameter_list|)
block|{
name|this
operator|.
name|bigKeysDirMap
operator|=
name|clone
operator|.
name|bigKeysDirMap
expr_stmt|;
name|this
operator|.
name|conds
operator|=
name|clone
operator|.
name|conds
expr_stmt|;
name|this
operator|.
name|exprs
operator|=
name|clone
operator|.
name|exprs
expr_stmt|;
name|this
operator|.
name|nullsafes
operator|=
name|clone
operator|.
name|nullsafes
expr_stmt|;
name|this
operator|.
name|handleSkewJoin
operator|=
name|clone
operator|.
name|handleSkewJoin
expr_stmt|;
name|this
operator|.
name|keyTableDesc
operator|=
name|clone
operator|.
name|keyTableDesc
expr_stmt|;
name|this
operator|.
name|noOuterJoin
operator|=
name|clone
operator|.
name|noOuterJoin
expr_stmt|;
name|this
operator|.
name|outputColumnNames
operator|=
name|clone
operator|.
name|outputColumnNames
expr_stmt|;
name|this
operator|.
name|reversedExprs
operator|=
name|clone
operator|.
name|reversedExprs
expr_stmt|;
name|this
operator|.
name|skewKeyDefinition
operator|=
name|clone
operator|.
name|skewKeyDefinition
expr_stmt|;
name|this
operator|.
name|skewKeysValuesTables
operator|=
name|clone
operator|.
name|skewKeysValuesTables
expr_stmt|;
name|this
operator|.
name|smallKeysDirMap
operator|=
name|clone
operator|.
name|smallKeysDirMap
expr_stmt|;
name|this
operator|.
name|tagOrder
operator|=
name|clone
operator|.
name|tagOrder
expr_stmt|;
name|this
operator|.
name|filters
operator|=
name|clone
operator|.
name|filters
expr_stmt|;
name|this
operator|.
name|filterMap
operator|=
name|clone
operator|.
name|filterMap
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|getExprs
parameter_list|()
block|{
return|return
name|exprs
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Byte
argument_list|>
name|getReversedExprs
parameter_list|()
block|{
return|return
name|reversedExprs
return|;
block|}
specifier|public
name|void
name|setReversedExprs
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Byte
argument_list|>
name|reversedExprs
parameter_list|)
block|{
name|this
operator|.
name|reversedExprs
operator|=
name|reversedExprs
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"condition expressions"
argument_list|)
specifier|public
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|getExprsStringMap
parameter_list|()
block|{
if|if
condition|(
name|getExprs
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|LinkedHashMap
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|ret
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|ent
range|:
name|getExprs
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|ent
operator|.
name|getValue
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ExprNodeDesc
name|expr
range|:
name|ent
operator|.
name|getValue
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|expr
operator|.
name|getExprString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
block|}
block|}
name|ret
operator|.
name|put
argument_list|(
name|ent
operator|.
name|getKey
argument_list|()
argument_list|,
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
specifier|public
name|void
name|setExprs
parameter_list|(
specifier|final
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|exprs
parameter_list|)
block|{
name|this
operator|.
name|exprs
operator|=
name|exprs
expr_stmt|;
block|}
comment|/**    * Get the string representation of filters.    *    * Returns null if they are no filters.    *    * @return Map from alias to filters on the alias.    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"filter predicates"
argument_list|)
specifier|public
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|getFiltersStringMap
parameter_list|()
block|{
if|if
condition|(
name|getFilters
argument_list|()
operator|==
literal|null
operator|||
name|getFilters
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|LinkedHashMap
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|ret
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|filtersPresent
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|ent
range|:
name|getFilters
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|ent
operator|.
name|getValue
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|ent
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|filtersPresent
operator|=
literal|true
expr_stmt|;
block|}
for|for
control|(
name|ExprNodeDesc
name|expr
range|:
name|ent
operator|.
name|getValue
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|first
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
name|first
operator|=
literal|false
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"{"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|expr
operator|.
name|getExprString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
block|}
block|}
name|ret
operator|.
name|put
argument_list|(
name|ent
operator|.
name|getKey
argument_list|()
argument_list|,
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|filtersPresent
condition|)
block|{
return|return
name|ret
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
specifier|public
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|getFilters
parameter_list|()
block|{
return|return
name|filters
return|;
block|}
specifier|public
name|void
name|setFilters
parameter_list|(
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|filters
parameter_list|)
block|{
name|this
operator|.
name|filters
operator|=
name|filters
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"outputColumnNames"
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getOutputColumnNames
parameter_list|()
block|{
return|return
name|outputColumnNames
return|;
block|}
specifier|public
name|void
name|setOutputColumnNames
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|outputColumnNames
parameter_list|)
block|{
name|this
operator|.
name|outputColumnNames
operator|=
name|outputColumnNames
expr_stmt|;
block|}
specifier|public
name|boolean
name|getNoOuterJoin
parameter_list|()
block|{
return|return
name|noOuterJoin
return|;
block|}
specifier|public
name|void
name|setNoOuterJoin
parameter_list|(
specifier|final
name|boolean
name|noOuterJoin
parameter_list|)
block|{
name|this
operator|.
name|noOuterJoin
operator|=
name|noOuterJoin
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"condition map"
argument_list|)
specifier|public
name|List
argument_list|<
name|JoinCondDesc
argument_list|>
name|getCondsList
parameter_list|()
block|{
if|if
condition|(
name|conds
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|ArrayList
argument_list|<
name|JoinCondDesc
argument_list|>
name|l
init|=
operator|new
name|ArrayList
argument_list|<
name|JoinCondDesc
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|JoinCondDesc
name|cond
range|:
name|conds
control|)
block|{
name|l
operator|.
name|add
argument_list|(
name|cond
argument_list|)
expr_stmt|;
block|}
return|return
name|l
return|;
block|}
specifier|public
name|JoinCondDesc
index|[]
name|getConds
parameter_list|()
block|{
return|return
name|conds
return|;
block|}
specifier|public
name|void
name|setConds
parameter_list|(
specifier|final
name|JoinCondDesc
index|[]
name|conds
parameter_list|)
block|{
name|this
operator|.
name|conds
operator|=
name|conds
expr_stmt|;
block|}
comment|/**    * The order in which tables should be processed when joining.    *    * @return Array of tags    */
specifier|public
name|Byte
index|[]
name|getTagOrder
parameter_list|()
block|{
return|return
name|tagOrder
return|;
block|}
comment|/**    * The order in which tables should be processed when joining.    *    * @param tagOrder    *          Array of tags    */
specifier|public
name|void
name|setTagOrder
parameter_list|(
name|Byte
index|[]
name|tagOrder
parameter_list|)
block|{
name|this
operator|.
name|tagOrder
operator|=
name|tagOrder
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"handleSkewJoin"
argument_list|)
specifier|public
name|boolean
name|getHandleSkewJoin
parameter_list|()
block|{
return|return
name|handleSkewJoin
return|;
block|}
comment|/**    * set to handle skew join in this join op.    *    * @param handleSkewJoin    */
specifier|public
name|void
name|setHandleSkewJoin
parameter_list|(
name|boolean
name|handleSkewJoin
parameter_list|)
block|{
name|this
operator|.
name|handleSkewJoin
operator|=
name|handleSkewJoin
expr_stmt|;
block|}
comment|/**    * @return mapping from tbl to dir for big keys.    */
specifier|public
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|getBigKeysDirMap
parameter_list|()
block|{
return|return
name|bigKeysDirMap
return|;
block|}
comment|/**    * set the mapping from tbl to dir for big keys.    *    * @param bigKeysDirMap    */
specifier|public
name|void
name|setBigKeysDirMap
parameter_list|(
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
name|bigKeysDirMap
parameter_list|)
block|{
name|this
operator|.
name|bigKeysDirMap
operator|=
name|bigKeysDirMap
expr_stmt|;
block|}
comment|/**    * @return mapping from tbl to dir for small keys    */
specifier|public
name|Map
argument_list|<
name|Byte
argument_list|,
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|>
name|getSmallKeysDirMap
parameter_list|()
block|{
return|return
name|smallKeysDirMap
return|;
block|}
comment|/**    * set the mapping from tbl to dir for small keys.    *    * @param smallKeysDirMap    */
specifier|public
name|void
name|setSmallKeysDirMap
parameter_list|(
name|Map
argument_list|<
name|Byte
argument_list|,
name|Map
argument_list|<
name|Byte
argument_list|,
name|String
argument_list|>
argument_list|>
name|smallKeysDirMap
parameter_list|)
block|{
name|this
operator|.
name|smallKeysDirMap
operator|=
name|smallKeysDirMap
expr_stmt|;
block|}
comment|/**    * @return skew key definition. If we see a key's associated entries' number    *         is bigger than this, we will define this key as a skew key.    */
specifier|public
name|int
name|getSkewKeyDefinition
parameter_list|()
block|{
return|return
name|skewKeyDefinition
return|;
block|}
comment|/**    * set skew key definition.    *    * @param skewKeyDefinition    */
specifier|public
name|void
name|setSkewKeyDefinition
parameter_list|(
name|int
name|skewKeyDefinition
parameter_list|)
block|{
name|this
operator|.
name|skewKeyDefinition
operator|=
name|skewKeyDefinition
expr_stmt|;
block|}
comment|/**    * @return the table desc for storing skew keys and their corresponding value;    */
specifier|public
name|Map
argument_list|<
name|Byte
argument_list|,
name|TableDesc
argument_list|>
name|getSkewKeysValuesTables
parameter_list|()
block|{
return|return
name|skewKeysValuesTables
return|;
block|}
comment|/**    * @param skewKeysValuesTables    *          set the table desc for storing skew keys and their corresponding    *          value;    */
specifier|public
name|void
name|setSkewKeysValuesTables
parameter_list|(
name|Map
argument_list|<
name|Byte
argument_list|,
name|TableDesc
argument_list|>
name|skewKeysValuesTables
parameter_list|)
block|{
name|this
operator|.
name|skewKeysValuesTables
operator|=
name|skewKeysValuesTables
expr_stmt|;
block|}
specifier|public
name|boolean
name|isNoOuterJoin
parameter_list|()
block|{
return|return
name|noOuterJoin
return|;
block|}
specifier|public
name|void
name|setKeyTableDesc
parameter_list|(
name|TableDesc
name|keyTblDesc
parameter_list|)
block|{
name|keyTableDesc
operator|=
name|keyTblDesc
expr_stmt|;
block|}
specifier|public
name|TableDesc
name|getKeyTableDesc
parameter_list|()
block|{
return|return
name|keyTableDesc
return|;
block|}
specifier|public
name|boolean
index|[]
name|getNullSafes
parameter_list|()
block|{
return|return
name|nullsafes
return|;
block|}
specifier|public
name|void
name|setNullSafes
parameter_list|(
name|boolean
index|[]
name|nullSafes
parameter_list|)
block|{
name|this
operator|.
name|nullsafes
operator|=
name|nullSafes
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"nullSafes"
argument_list|)
specifier|public
name|String
name|getNullSafeString
parameter_list|()
block|{
if|if
condition|(
name|nullsafes
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|boolean
name|hasNS
init|=
literal|false
decl_stmt|;
for|for
control|(
name|boolean
name|ns
range|:
name|nullsafes
control|)
block|{
name|hasNS
operator||=
name|ns
expr_stmt|;
block|}
return|return
name|hasNS
condition|?
name|Arrays
operator|.
name|toString
argument_list|(
name|nullsafes
argument_list|)
else|:
literal|null
return|;
block|}
specifier|public
name|int
index|[]
index|[]
name|getFilterMap
parameter_list|()
block|{
return|return
name|filterMap
return|;
block|}
specifier|public
name|void
name|setFilterMap
parameter_list|(
name|int
index|[]
index|[]
name|filterMap
parameter_list|)
block|{
name|this
operator|.
name|filterMap
operator|=
name|filterMap
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"filter mappings"
argument_list|,
name|normalExplain
operator|=
literal|false
argument_list|)
specifier|public
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|getFilterMapString
parameter_list|()
block|{
return|return
name|toCompactString
argument_list|(
name|filterMap
argument_list|)
return|;
block|}
specifier|protected
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|toCompactString
parameter_list|(
name|int
index|[]
index|[]
name|filterMap
parameter_list|)
block|{
if|if
condition|(
name|filterMap
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|filterMap
operator|=
name|compactFilter
argument_list|(
name|filterMap
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|result
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Integer
argument_list|,
name|String
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
name|filterMap
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|filterMap
index|[
name|i
index|]
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|result
operator|.
name|put
argument_list|(
name|i
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|filterMap
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
name|result
return|;
block|}
comment|// remove filterMap for outer alias if filter is not exist on that
specifier|private
name|int
index|[]
index|[]
name|compactFilter
parameter_list|(
name|int
index|[]
index|[]
name|filterMap
parameter_list|)
block|{
if|if
condition|(
name|filterMap
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|filterMap
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|filterMap
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
name|boolean
name|noFilter
init|=
literal|true
decl_stmt|;
comment|// join positions for even index, filter lengths for odd index
for|for
control|(
name|int
name|j
init|=
literal|1
init|;
name|j
operator|<
name|filterMap
index|[
name|i
index|]
operator|.
name|length
condition|;
name|j
operator|+=
literal|2
control|)
block|{
if|if
condition|(
name|filterMap
index|[
name|i
index|]
index|[
name|j
index|]
operator|>
literal|0
condition|)
block|{
name|noFilter
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|noFilter
condition|)
block|{
name|filterMap
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|int
index|[]
name|mapping
range|:
name|filterMap
control|)
block|{
if|if
condition|(
name|mapping
operator|!=
literal|null
condition|)
block|{
return|return
name|filterMap
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|int
name|getTagLength
parameter_list|()
block|{
name|int
name|tagLength
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|byte
name|tag
range|:
name|getExprs
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|tagLength
operator|=
name|Math
operator|.
name|max
argument_list|(
name|tagLength
argument_list|,
name|tag
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|tagLength
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
index|[]
name|convertToArray
parameter_list|(
name|Map
argument_list|<
name|Byte
argument_list|,
name|T
argument_list|>
name|source
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|compType
parameter_list|)
block|{
name|T
index|[]
name|result
init|=
operator|(
name|T
index|[]
operator|)
name|Array
operator|.
name|newInstance
argument_list|(
name|compType
argument_list|,
name|getTagLength
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Byte
argument_list|,
name|T
argument_list|>
name|entry
range|:
name|source
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|result
index|[
name|entry
operator|.
name|getKey
argument_list|()
index|]
operator|=
name|entry
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

