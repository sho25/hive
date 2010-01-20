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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|parse
operator|.
name|QBParseInfo
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
name|QBMetaData
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
name|createTableDesc
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

begin_comment
comment|/**  * Implementation of the query block  *  **/
end_comment

begin_class
specifier|public
class|class
name|QB
block|{
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
literal|"hive.ql.parse.QB"
argument_list|)
decl_stmt|;
specifier|private
name|int
name|numJoins
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|numGbys
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|numSels
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|numSelDi
init|=
literal|0
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|aliasToTabs
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|QBExpr
argument_list|>
name|aliasToSubq
decl_stmt|;
specifier|private
name|QBParseInfo
name|qbp
decl_stmt|;
specifier|private
name|QBMetaData
name|qbm
decl_stmt|;
specifier|private
name|QBJoinTree
name|qbjoin
decl_stmt|;
specifier|private
name|String
name|id
decl_stmt|;
specifier|private
name|boolean
name|isQuery
decl_stmt|;
specifier|private
name|createTableDesc
name|tblDesc
init|=
literal|null
decl_stmt|;
comment|// table descriptor of the final results
specifier|public
name|void
name|print
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|msg
operator|+
literal|"alias="
operator|+
name|qbp
operator|.
name|getAlias
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|alias
range|:
name|getSubqAliases
argument_list|()
control|)
block|{
name|QBExpr
name|qbexpr
init|=
name|getSubqForAlias
argument_list|(
name|alias
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|msg
operator|+
literal|"start subquery "
operator|+
name|alias
argument_list|)
expr_stmt|;
name|qbexpr
operator|.
name|print
argument_list|(
name|msg
operator|+
literal|" "
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|msg
operator|+
literal|"end subquery "
operator|+
name|alias
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|QB
parameter_list|()
block|{     }
specifier|public
name|QB
parameter_list|(
name|String
name|outer_id
parameter_list|,
name|String
name|alias
parameter_list|,
name|boolean
name|isSubQ
parameter_list|)
block|{
name|aliasToTabs
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|aliasToSubq
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|QBExpr
argument_list|>
argument_list|()
expr_stmt|;
if|if
condition|(
name|alias
operator|!=
literal|null
condition|)
block|{
name|alias
operator|=
name|alias
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
block|}
name|qbp
operator|=
operator|new
name|QBParseInfo
argument_list|(
name|alias
argument_list|,
name|isSubQ
argument_list|)
expr_stmt|;
name|qbm
operator|=
operator|new
name|QBMetaData
argument_list|()
expr_stmt|;
name|this
operator|.
name|id
operator|=
operator|(
name|outer_id
operator|==
literal|null
condition|?
name|alias
else|:
name|outer_id
operator|+
literal|":"
operator|+
name|alias
operator|)
expr_stmt|;
block|}
specifier|public
name|QBParseInfo
name|getParseInfo
parameter_list|()
block|{
return|return
name|qbp
return|;
block|}
specifier|public
name|QBMetaData
name|getMetaData
parameter_list|()
block|{
return|return
name|qbm
return|;
block|}
specifier|public
name|void
name|setQBParseInfo
parameter_list|(
name|QBParseInfo
name|qbp
parameter_list|)
block|{
name|this
operator|.
name|qbp
operator|=
name|qbp
expr_stmt|;
block|}
specifier|public
name|void
name|countSelDi
parameter_list|()
block|{
name|numSelDi
operator|++
expr_stmt|;
block|}
specifier|public
name|void
name|countSel
parameter_list|()
block|{
name|numSels
operator|++
expr_stmt|;
block|}
specifier|public
name|boolean
name|exists
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
name|alias
operator|=
name|alias
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
if|if
condition|(
name|aliasToTabs
operator|.
name|get
argument_list|(
name|alias
argument_list|)
operator|!=
literal|null
operator|||
name|aliasToSubq
operator|.
name|get
argument_list|(
name|alias
argument_list|)
operator|!=
literal|null
condition|)
return|return
literal|true
return|;
return|return
literal|false
return|;
block|}
specifier|public
name|void
name|setTabAlias
parameter_list|(
name|String
name|alias
parameter_list|,
name|String
name|tabName
parameter_list|)
block|{
name|aliasToTabs
operator|.
name|put
argument_list|(
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|tabName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setSubqAlias
parameter_list|(
name|String
name|alias
parameter_list|,
name|QBExpr
name|qbexpr
parameter_list|)
block|{
name|aliasToSubq
operator|.
name|put
argument_list|(
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|qbexpr
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
specifier|public
name|int
name|getNumGbys
parameter_list|()
block|{
return|return
name|numGbys
return|;
block|}
specifier|public
name|int
name|getNumSelDi
parameter_list|()
block|{
return|return
name|numSelDi
return|;
block|}
specifier|public
name|int
name|getNumSels
parameter_list|()
block|{
return|return
name|numSels
return|;
block|}
specifier|public
name|int
name|getNumJoins
parameter_list|()
block|{
return|return
name|numJoins
return|;
block|}
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getSubqAliases
parameter_list|()
block|{
return|return
name|aliasToSubq
operator|.
name|keySet
argument_list|()
return|;
block|}
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getTabAliases
parameter_list|()
block|{
return|return
name|aliasToTabs
operator|.
name|keySet
argument_list|()
return|;
block|}
specifier|public
name|QBExpr
name|getSubqForAlias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|aliasToSubq
operator|.
name|get
argument_list|(
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|String
name|getTabNameForAlias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|aliasToTabs
operator|.
name|get
argument_list|(
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|void
name|rewriteViewToSubq
parameter_list|(
name|String
name|alias
parameter_list|,
name|String
name|viewName
parameter_list|,
name|QBExpr
name|qbexpr
parameter_list|)
block|{
name|alias
operator|=
name|alias
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
name|String
name|tableName
init|=
name|aliasToTabs
operator|.
name|remove
argument_list|(
name|alias
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|viewName
operator|.
name|equals
argument_list|(
name|tableName
argument_list|)
operator|)
assert|;
name|aliasToSubq
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|qbexpr
argument_list|)
expr_stmt|;
block|}
specifier|public
name|QBJoinTree
name|getQbJoinTree
parameter_list|()
block|{
return|return
name|qbjoin
return|;
block|}
specifier|public
name|void
name|setQbJoinTree
parameter_list|(
name|QBJoinTree
name|qbjoin
parameter_list|)
block|{
name|this
operator|.
name|qbjoin
operator|=
name|qbjoin
expr_stmt|;
block|}
specifier|public
name|void
name|setIsQuery
parameter_list|(
name|boolean
name|isQuery
parameter_list|)
block|{
name|this
operator|.
name|isQuery
operator|=
name|isQuery
expr_stmt|;
block|}
specifier|public
name|boolean
name|getIsQuery
parameter_list|()
block|{
return|return
name|isQuery
return|;
block|}
specifier|public
name|boolean
name|isSelectStarQuery
parameter_list|()
block|{
return|return
name|qbp
operator|.
name|isSelectStarQuery
argument_list|()
operator|&&
name|aliasToSubq
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|!
name|isCTAS
argument_list|()
return|;
block|}
specifier|public
name|createTableDesc
name|getTableDesc
parameter_list|()
block|{
return|return
name|tblDesc
return|;
block|}
specifier|public
name|void
name|setTableDesc
parameter_list|(
name|createTableDesc
name|desc
parameter_list|)
block|{
name|tblDesc
operator|=
name|desc
expr_stmt|;
block|}
comment|/**    * Whether this QB is for a CREATE-TABLE-AS-SELECT.    */
specifier|public
name|boolean
name|isCTAS
parameter_list|()
block|{
return|return
name|tblDesc
operator|!=
literal|null
return|;
block|}
block|}
end_class

end_unit

