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
name|parse
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
name|Set
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
name|RowSchema
import|;
end_import

begin_comment
comment|/**  * Implementation of the Row Resolver.  *  */
end_comment

begin_class
specifier|public
class|class
name|RowResolver
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
specifier|private
name|RowSchema
name|rowSchema
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
argument_list|>
name|rslvMap
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|invRslvMap
decl_stmt|;
comment|/*    * now a Column can have an alternate mapping.    * This captures the alternate mapping.    * The primary(first) mapping is still only held in    * invRslvMap.    */
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|altInvRslvMap
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|expressionMap
decl_stmt|;
comment|// TODO: Refactor this and do in a more object oriented manner
specifier|private
name|boolean
name|isExprResolver
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RowResolver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|RowResolver
parameter_list|()
block|{
name|rowSchema
operator|=
operator|new
name|RowSchema
argument_list|()
expr_stmt|;
name|rslvMap
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|invRslvMap
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
argument_list|()
expr_stmt|;
name|altInvRslvMap
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
argument_list|()
expr_stmt|;
name|expressionMap
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
argument_list|()
expr_stmt|;
name|isExprResolver
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * Puts a resolver entry corresponding to a source expression which is to be    * used for identical expression recognition (e.g. for matching expressions    * in the SELECT list with the GROUP BY clause).  The convention for such    * entries is an empty-string ("") as the table alias together with the    * string rendering of the ASTNode as the column alias.    */
specifier|public
name|void
name|putExpression
parameter_list|(
name|ASTNode
name|node
parameter_list|,
name|ColumnInfo
name|colInfo
parameter_list|)
block|{
name|String
name|treeAsString
init|=
name|node
operator|.
name|toStringTree
argument_list|()
decl_stmt|;
name|expressionMap
operator|.
name|put
argument_list|(
name|treeAsString
argument_list|,
name|node
argument_list|)
expr_stmt|;
name|put
argument_list|(
literal|""
argument_list|,
name|treeAsString
argument_list|,
name|colInfo
argument_list|)
expr_stmt|;
block|}
comment|/**    * Retrieves the ColumnInfo corresponding to a source expression which    * exactly matches the string rendering of the given ASTNode.    */
specifier|public
name|ColumnInfo
name|getExpression
parameter_list|(
name|ASTNode
name|node
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
name|get
argument_list|(
literal|""
argument_list|,
name|node
operator|.
name|toStringTree
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Retrieves the source expression matching a given ASTNode's    * string rendering exactly.    */
specifier|public
name|ASTNode
name|getExpressionSource
parameter_list|(
name|ASTNode
name|node
parameter_list|)
block|{
return|return
name|expressionMap
operator|.
name|get
argument_list|(
name|node
operator|.
name|toStringTree
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|void
name|put
parameter_list|(
name|String
name|tab_alias
parameter_list|,
name|String
name|col_alias
parameter_list|,
name|ColumnInfo
name|colInfo
parameter_list|)
block|{
if|if
condition|(
operator|!
name|addMappingOnly
argument_list|(
name|tab_alias
argument_list|,
name|col_alias
argument_list|,
name|colInfo
argument_list|)
condition|)
block|{
name|rowSchema
operator|.
name|getSignature
argument_list|()
operator|.
name|add
argument_list|(
name|colInfo
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|addMappingOnly
parameter_list|(
name|String
name|tab_alias
parameter_list|,
name|String
name|col_alias
parameter_list|,
name|ColumnInfo
name|colInfo
parameter_list|)
block|{
if|if
condition|(
name|tab_alias
operator|!=
literal|null
condition|)
block|{
name|tab_alias
operator|=
name|tab_alias
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
block|}
name|col_alias
operator|=
name|col_alias
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
comment|/*      * allow multiple mappings to the same ColumnInfo.      * When a ColumnInfo is mapped multiple times, only the      * first inverse mapping is captured.      */
name|boolean
name|colPresent
init|=
name|invRslvMap
operator|.
name|containsKey
argument_list|(
name|colInfo
operator|.
name|getInternalName
argument_list|()
argument_list|)
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
name|f_map
init|=
name|rslvMap
operator|.
name|get
argument_list|(
name|tab_alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|f_map
operator|==
literal|null
condition|)
block|{
name|f_map
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
argument_list|()
expr_stmt|;
name|rslvMap
operator|.
name|put
argument_list|(
name|tab_alias
argument_list|,
name|f_map
argument_list|)
expr_stmt|;
block|}
name|f_map
operator|.
name|put
argument_list|(
name|col_alias
argument_list|,
name|colInfo
argument_list|)
expr_stmt|;
name|String
index|[]
name|qualifiedAlias
init|=
operator|new
name|String
index|[
literal|2
index|]
decl_stmt|;
name|qualifiedAlias
index|[
literal|0
index|]
operator|=
name|tab_alias
expr_stmt|;
name|qualifiedAlias
index|[
literal|1
index|]
operator|=
name|col_alias
expr_stmt|;
if|if
condition|(
operator|!
name|colPresent
condition|)
block|{
name|invRslvMap
operator|.
name|put
argument_list|(
name|colInfo
operator|.
name|getInternalName
argument_list|()
argument_list|,
name|qualifiedAlias
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|altInvRslvMap
operator|.
name|put
argument_list|(
name|colInfo
operator|.
name|getInternalName
argument_list|()
argument_list|,
name|qualifiedAlias
argument_list|)
expr_stmt|;
block|}
return|return
name|colPresent
return|;
block|}
specifier|public
name|boolean
name|hasTableAlias
parameter_list|(
name|String
name|tab_alias
parameter_list|)
block|{
return|return
name|rslvMap
operator|.
name|get
argument_list|(
name|tab_alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
operator|!=
literal|null
return|;
block|}
comment|/**    * Gets the column Info to tab_alias.col_alias type of a column reference. I    * the tab_alias is not provided as can be the case with an non aliased    * column, this function looks up the column in all the table aliases in this    * row resolver and returns the match. It also throws an exception if the    * column is found in multiple table aliases. If no match is found a null    * values is returned.    *    * This allows us to interpret both select t.c1 type of references and select    * c1 kind of references. The later kind are what we call non aliased column    * references in the query.    *    * @param tab_alias    *          The table alias to match (this is null if the column reference is    *          non aliased)    * @param col_alias    *          The column name that is being searched for    * @return ColumnInfo    * @throws SemanticException    */
specifier|public
name|ColumnInfo
name|get
parameter_list|(
name|String
name|tab_alias
parameter_list|,
name|String
name|col_alias
parameter_list|)
throws|throws
name|SemanticException
block|{
name|col_alias
operator|=
name|col_alias
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
name|ColumnInfo
name|ret
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|tab_alias
operator|!=
literal|null
condition|)
block|{
name|tab_alias
operator|=
name|tab_alias
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
name|f_map
init|=
name|rslvMap
operator|.
name|get
argument_list|(
name|tab_alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|f_map
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|ret
operator|=
name|f_map
operator|.
name|get
argument_list|(
name|col_alias
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|boolean
name|found
init|=
literal|false
decl_stmt|;
name|String
name|foundTbl
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
argument_list|>
name|rslvEntry
range|:
name|rslvMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|rslvKey
init|=
name|rslvEntry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
name|cmap
init|=
name|rslvEntry
operator|.
name|getValue
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
name|cmapEnt
range|:
name|cmap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|col_alias
operator|.
name|equalsIgnoreCase
argument_list|(
name|cmapEnt
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
comment|/*              * We can have an unaliased and one aliased mapping to a Column.              */
if|if
condition|(
name|found
operator|&&
name|foundTbl
operator|!=
literal|null
operator|&&
name|rslvKey
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Column "
operator|+
name|col_alias
operator|+
literal|" Found in more than One Tables/Subqueries"
argument_list|)
throw|;
block|}
name|found
operator|=
literal|true
expr_stmt|;
name|foundTbl
operator|=
name|rslvKey
operator|==
literal|null
condition|?
name|foundTbl
else|:
name|rslvKey
expr_stmt|;
name|ret
operator|=
name|cmapEnt
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|ret
return|;
block|}
comment|/**    * check if column name is already exist in RR    */
specifier|public
name|void
name|checkColumn
parameter_list|(
name|String
name|tableAlias
parameter_list|,
name|String
name|columnAlias
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ColumnInfo
name|prev
init|=
name|get
argument_list|(
literal|null
argument_list|,
name|columnAlias
argument_list|)
decl_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
operator|&&
operator|(
name|tableAlias
operator|==
literal|null
operator|||
operator|!
name|tableAlias
operator|.
name|equalsIgnoreCase
argument_list|(
name|prev
operator|.
name|getTabAlias
argument_list|()
argument_list|)
operator|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|AMBIGUOUS_COLUMN
operator|.
name|getMsg
argument_list|(
name|columnAlias
argument_list|)
argument_list|)
throw|;
block|}
block|}
specifier|public
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|getColumnInfos
parameter_list|()
block|{
return|return
name|rowSchema
operator|.
name|getSignature
argument_list|()
return|;
block|}
comment|/**    * Get a list of aliases for non-hidden columns    * @param max the maximum number of columns to return    * @return a list of non-hidden column names no greater in size than max    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getReferenceableColumnAliases
parameter_list|(
name|String
name|tableAlias
parameter_list|,
name|int
name|max
parameter_list|)
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|tables
init|=
name|rslvMap
operator|.
name|size
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
name|mapping
init|=
name|rslvMap
operator|.
name|get
argument_list|(
name|tableAlias
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapping
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
name|entry
range|:
name|mapping
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|max
operator|>
literal|0
operator|&&
name|count
operator|>=
name|max
condition|)
block|{
break|break;
block|}
name|ColumnInfo
name|columnInfo
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|columnInfo
operator|.
name|isHiddenVirtualCol
argument_list|()
condition|)
block|{
name|columnNames
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
for|for
control|(
name|ColumnInfo
name|columnInfo
range|:
name|getColumnInfos
argument_list|()
control|)
block|{
if|if
condition|(
name|max
operator|>
literal|0
operator|&&
name|count
operator|>=
name|max
condition|)
block|{
break|break;
block|}
if|if
condition|(
operator|!
name|columnInfo
operator|.
name|isHiddenVirtualCol
argument_list|()
condition|)
block|{
name|String
index|[]
name|inverse
init|=
operator|!
name|isExprResolver
condition|?
name|reverseLookup
argument_list|(
name|columnInfo
operator|.
name|getInternalName
argument_list|()
argument_list|)
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|inverse
operator|!=
literal|null
condition|)
block|{
name|columnNames
operator|.
name|add
argument_list|(
name|inverse
index|[
literal|0
index|]
operator|==
literal|null
operator|||
name|tables
operator|<=
literal|1
condition|?
name|inverse
index|[
literal|1
index|]
else|:
name|inverse
index|[
literal|0
index|]
operator|+
literal|"."
operator|+
name|inverse
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|columnNames
operator|.
name|add
argument_list|(
name|columnInfo
operator|.
name|getAlias
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|columnNames
argument_list|)
return|;
block|}
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
name|getFieldMap
parameter_list|(
name|String
name|tabAlias
parameter_list|)
block|{
if|if
condition|(
name|tabAlias
operator|==
literal|null
condition|)
block|{
return|return
name|rslvMap
operator|.
name|get
argument_list|(
literal|null
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|rslvMap
operator|.
name|get
argument_list|(
name|tabAlias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
return|;
block|}
block|}
specifier|public
name|int
name|getPosition
parameter_list|(
name|String
name|internalName
parameter_list|)
block|{
name|int
name|pos
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|ColumnInfo
name|var
range|:
name|rowSchema
operator|.
name|getSignature
argument_list|()
control|)
block|{
operator|++
name|pos
expr_stmt|;
if|if
condition|(
name|var
operator|.
name|getInternalName
argument_list|()
operator|.
name|equals
argument_list|(
name|internalName
argument_list|)
condition|)
block|{
return|return
name|pos
return|;
block|}
block|}
return|return
operator|-
literal|1
return|;
block|}
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getTableNames
parameter_list|()
block|{
return|return
name|rslvMap
operator|.
name|keySet
argument_list|()
return|;
block|}
specifier|public
name|String
index|[]
name|reverseLookup
parameter_list|(
name|String
name|internalName
parameter_list|)
block|{
return|return
name|invRslvMap
operator|.
name|get
argument_list|(
name|internalName
argument_list|)
return|;
block|}
specifier|public
name|void
name|setIsExprResolver
parameter_list|(
name|boolean
name|isExprResolver
parameter_list|)
block|{
name|this
operator|.
name|isExprResolver
operator|=
name|isExprResolver
expr_stmt|;
block|}
specifier|public
name|boolean
name|getIsExprResolver
parameter_list|()
block|{
return|return
name|isExprResolver
return|;
block|}
specifier|public
name|String
index|[]
name|getAlternateMappings
parameter_list|(
name|String
name|internalName
parameter_list|)
block|{
return|return
name|altInvRslvMap
operator|.
name|get
argument_list|(
name|internalName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
argument_list|>
name|e
range|:
name|rslvMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|tab
init|=
name|e
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|tab
operator|+
literal|"{"
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
name|f_map
init|=
name|e
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|f_map
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
name|entry
range|:
name|f_map
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"("
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|","
operator|+
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"} "
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|RowSchema
name|getRowSchema
parameter_list|()
block|{
return|return
name|rowSchema
return|;
block|}
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
argument_list|>
name|getRslvMap
parameter_list|()
block|{
return|return
name|rslvMap
return|;
block|}
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|getInvRslvMap
parameter_list|()
block|{
return|return
name|invRslvMap
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|getExpressionMap
parameter_list|()
block|{
return|return
name|expressionMap
return|;
block|}
specifier|public
name|void
name|setExprResolver
parameter_list|(
name|boolean
name|isExprResolver
parameter_list|)
block|{
name|this
operator|.
name|isExprResolver
operator|=
name|isExprResolver
expr_stmt|;
block|}
specifier|public
name|void
name|setRowSchema
parameter_list|(
name|RowSchema
name|rowSchema
parameter_list|)
block|{
name|this
operator|.
name|rowSchema
operator|=
name|rowSchema
expr_stmt|;
block|}
specifier|public
name|void
name|setRslvMap
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
argument_list|>
name|rslvMap
parameter_list|)
block|{
name|this
operator|.
name|rslvMap
operator|=
name|rslvMap
expr_stmt|;
block|}
specifier|public
name|void
name|setInvRslvMap
parameter_list|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|invRslvMap
parameter_list|)
block|{
name|this
operator|.
name|invRslvMap
operator|=
name|invRslvMap
expr_stmt|;
block|}
specifier|public
name|void
name|setExpressionMap
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|expressionMap
parameter_list|)
block|{
name|this
operator|.
name|expressionMap
operator|=
name|expressionMap
expr_stmt|;
block|}
comment|// TODO: 1) How to handle collisions? 2) Should we be cloning ColumnInfo or
comment|// not?
specifier|public
specifier|static
name|int
name|add
parameter_list|(
name|RowResolver
name|rrToAddTo
parameter_list|,
name|RowResolver
name|rrToAddFrom
parameter_list|,
name|int
name|outputColPos
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
name|tabAlias
decl_stmt|;
name|String
name|colAlias
decl_stmt|;
name|String
index|[]
name|qualifiedColName
decl_stmt|;
for|for
control|(
name|ColumnInfo
name|cInfoFrmInput
range|:
name|rrToAddFrom
operator|.
name|getRowSchema
argument_list|()
operator|.
name|getSignature
argument_list|()
control|)
block|{
name|ColumnInfo
name|newCI
init|=
literal|null
decl_stmt|;
name|qualifiedColName
operator|=
name|rrToAddFrom
operator|.
name|getInvRslvMap
argument_list|()
operator|.
name|get
argument_list|(
name|cInfoFrmInput
operator|.
name|getInternalName
argument_list|()
argument_list|)
expr_stmt|;
name|tabAlias
operator|=
name|qualifiedColName
index|[
literal|0
index|]
expr_stmt|;
name|colAlias
operator|=
name|qualifiedColName
index|[
literal|1
index|]
expr_stmt|;
name|newCI
operator|=
operator|new
name|ColumnInfo
argument_list|(
name|cInfoFrmInput
argument_list|)
expr_stmt|;
name|newCI
operator|.
name|setInternalName
argument_list|(
name|SemanticAnalyzer
operator|.
name|getColumnInternalName
argument_list|(
name|outputColPos
argument_list|)
argument_list|)
expr_stmt|;
name|outputColPos
operator|++
expr_stmt|;
if|if
condition|(
name|rrToAddTo
operator|.
name|get
argument_list|(
name|tabAlias
argument_list|,
name|colAlias
argument_list|)
operator|!=
literal|null
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Ambigous Column Names"
argument_list|)
throw|;
name|rrToAddTo
operator|.
name|put
argument_list|(
name|tabAlias
argument_list|,
name|colAlias
argument_list|,
name|newCI
argument_list|)
expr_stmt|;
block|}
return|return
name|outputColPos
return|;
block|}
comment|/** 	 * Return a new row resolver that is combination of left RR and right RR. 	 * The schema will be schema of left, schema of right 	 *  	 * @param leftRR 	 * @param rightRR 	 * @return 	 * @throws SemanticException 	 */
specifier|public
specifier|static
name|RowResolver
name|getCombinedRR
parameter_list|(
name|RowResolver
name|leftRR
parameter_list|,
name|RowResolver
name|rightRR
parameter_list|)
throws|throws
name|SemanticException
block|{
name|int
name|outputColPos
init|=
literal|0
decl_stmt|;
name|RowResolver
name|combinedRR
init|=
operator|new
name|RowResolver
argument_list|()
decl_stmt|;
name|outputColPos
operator|=
name|add
argument_list|(
name|combinedRR
argument_list|,
name|leftRR
argument_list|,
name|outputColPos
argument_list|)
expr_stmt|;
name|outputColPos
operator|=
name|add
argument_list|(
name|combinedRR
argument_list|,
name|rightRR
argument_list|,
name|outputColPos
argument_list|)
expr_stmt|;
return|return
name|combinedRR
return|;
block|}
block|}
end_class

end_unit

