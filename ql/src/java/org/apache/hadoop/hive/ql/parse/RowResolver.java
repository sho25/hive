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
name|util
operator|.
name|*
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
name|serde
operator|.
name|ExpressionUtils
import|;
end_import

begin_comment
comment|/**  * Implementation of the Row Resolver  *  **/
end_comment

begin_class
specifier|public
class|class
name|RowResolver
block|{
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
name|isExprResolver
operator|=
literal|false
expr_stmt|;
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
name|rowSchema
operator|.
name|getSignature
argument_list|()
operator|==
literal|null
condition|)
block|{
name|rowSchema
operator|.
name|setSignature
argument_list|(
operator|new
name|Vector
argument_list|<
name|ColumnInfo
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
argument_list|)
operator|!=
literal|null
condition|?
literal|true
else|:
literal|false
return|;
block|}
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
block|{
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
name|ColumnInfo
name|resInfo
init|=
name|f_map
operator|.
name|get
argument_list|(
name|col_alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|resInfo
operator|==
literal|null
condition|)
block|{
comment|// case insensitive search on column names but ANTLR Tokens are upppercase
comment|// TODO: need to fix this in a better way
name|resInfo
operator|=
name|f_map
operator|.
name|get
argument_list|(
name|col_alias
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|resInfo
operator|==
literal|null
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|exprs
init|=
name|ExpressionUtils
operator|.
name|decomposeComplexExpression
argument_list|(
name|col_alias
argument_list|)
decl_stmt|;
comment|// Is this a complex field?
if|if
condition|(
name|exprs
operator|.
name|size
argument_list|()
operator|==
literal|2
condition|)
block|{
name|String
name|topLevelField
init|=
name|exprs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|suffix
init|=
name|exprs
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ColumnInfo
name|info
init|=
name|f_map
operator|.
name|get
argument_list|(
name|topLevelField
operator|.
name|toLowerCase
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|info
operator|==
literal|null
condition|)
block|{
name|info
operator|=
name|f_map
operator|.
name|get
argument_list|(
name|topLevelField
argument_list|)
expr_stmt|;
block|}
name|resInfo
operator|=
operator|new
name|ColumnInfo
argument_list|(
name|info
operator|.
name|getInternalName
argument_list|()
operator|+
name|suffix
argument_list|,
name|info
operator|.
name|getType
argument_list|()
operator|.
name|getFieldType
argument_list|(
name|suffix
argument_list|)
argument_list|,
name|info
operator|.
name|getIsVirtual
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|resInfo
return|;
block|}
specifier|public
name|Vector
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
name|tab_alias
parameter_list|)
block|{
return|return
name|rslvMap
operator|.
name|get
argument_list|(
name|tab_alias
argument_list|)
return|;
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
name|toString
parameter_list|()
block|{
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
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
operator|(
name|String
operator|)
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
operator|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|ColumnInfo
argument_list|>
operator|)
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
operator|(
name|String
operator|)
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
block|}
end_class

end_unit

