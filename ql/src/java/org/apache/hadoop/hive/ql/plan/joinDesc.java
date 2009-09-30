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
name|io
operator|.
name|Serializable
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
name|Iterator
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

begin_comment
comment|/**  * Join operator Descriptor implementation.  *   */
end_comment

begin_class
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"Join Operator"
argument_list|)
specifier|public
class|class
name|joinDesc
implements|implements
name|Serializable
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
comment|// alias to key mapping
specifier|private
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|exprNodeDesc
argument_list|>
argument_list|>
name|exprs
decl_stmt|;
comment|//used for create joinOutputObjectInspector
specifier|protected
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
name|outputColumnNames
decl_stmt|;
comment|// key:column output name, value:tag
specifier|transient
specifier|private
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
name|joinCond
index|[]
name|conds
decl_stmt|;
specifier|protected
name|Byte
index|[]
name|tagOrder
decl_stmt|;
specifier|public
name|joinDesc
parameter_list|()
block|{ }
specifier|public
name|joinDesc
parameter_list|(
specifier|final
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|exprNodeDesc
argument_list|>
argument_list|>
name|exprs
parameter_list|,
name|ArrayList
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
name|joinCond
index|[]
name|conds
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
specifier|public
name|joinDesc
parameter_list|(
specifier|final
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|exprNodeDesc
argument_list|>
argument_list|>
name|exprs
parameter_list|,
name|ArrayList
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
name|joinDesc
parameter_list|(
specifier|final
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|exprNodeDesc
argument_list|>
argument_list|>
name|exprs
parameter_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
name|outputColumnNames
parameter_list|,
specifier|final
name|joinCond
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
literal|false
argument_list|,
name|conds
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|exprNodeDesc
argument_list|>
argument_list|>
name|getExprs
parameter_list|()
block|{
return|return
name|this
operator|.
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
name|reversed_Exprs
parameter_list|)
block|{
name|this
operator|.
name|reversedExprs
operator|=
name|reversed_Exprs
expr_stmt|;
block|}
annotation|@
name|explain
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
name|exprNodeDesc
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
name|exprNodeDesc
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
name|exprNodeDesc
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
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"outputColumnNames"
argument_list|)
specifier|public
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
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
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|java
operator|.
name|lang
operator|.
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
name|this
operator|.
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
name|explain
argument_list|(
name|displayName
operator|=
literal|"condition map"
argument_list|)
specifier|public
name|List
argument_list|<
name|joinCond
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
name|joinCond
argument_list|>
name|l
init|=
operator|new
name|ArrayList
argument_list|<
name|joinCond
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|joinCond
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
name|joinCond
index|[]
name|getConds
parameter_list|()
block|{
return|return
name|this
operator|.
name|conds
return|;
block|}
specifier|public
name|void
name|setConds
parameter_list|(
specifier|final
name|joinCond
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
comment|/**    * The order in which tables should be processed when joining    *     * @return Array of tags    */
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
comment|/**    * The order in which tables should be processed when joining    *     * @param tagOrder Array of tags    */
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
block|}
end_class

end_unit

