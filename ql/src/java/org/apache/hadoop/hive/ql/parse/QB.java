begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|fs
operator|.
name|Path
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
name|ddl
operator|.
name|table
operator|.
name|creation
operator|.
name|CreateTableDesc
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
name|ddl
operator|.
name|view
operator|.
name|CreateViewDesc
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

begin_comment
comment|/**  * Implementation of the query block.  *  **/
end_comment

begin_class
specifier|public
class|class
name|QB
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"hive.ql.parse.QB"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|int
name|numJoins
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
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
name|HashMap
argument_list|<
name|String
argument_list|,
name|Table
argument_list|>
name|viewAliasToViewSchema
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|aliasToProps
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|aliases
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
name|boolean
name|isAnalyzeRewrite
decl_stmt|;
specifier|private
name|CreateTableDesc
name|tblDesc
init|=
literal|null
decl_stmt|;
comment|// table descriptor of the final
specifier|private
name|CreateTableDesc
name|directoryDesc
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|insideView
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|aliasInsideView
decl_stmt|;
comment|// If this is a materialized view, this stores the view descriptor
specifier|private
name|CreateViewDesc
name|viewDesc
decl_stmt|;
comment|// used by PTFs
comment|/*    * This map maintains the PTFInvocationSpec for each PTF chain invocation in this QB.    */
specifier|private
name|HashMap
argument_list|<
name|ASTNode
argument_list|,
name|PTFInvocationSpec
argument_list|>
name|ptfNodeToSpec
decl_stmt|;
comment|/*    * the WindowingSpec used for windowing clauses in this QB.    */
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|WindowingSpec
argument_list|>
name|destToWindowingSpec
decl_stmt|;
comment|/*    * If this QB represents a  SubQuery predicate then this will point to the SubQuery object.    */
specifier|private
name|QBSubQuery
name|subQueryPredicateDef
decl_stmt|;
comment|/*    * used to give a unique name to each SubQuery QB Currently there can be at    * most 2 SubQueries in a Query: 1 in the Where clause, and 1 in the Having    * clause.    */
specifier|private
name|int
name|numSubQueryPredicates
decl_stmt|;
comment|/*    * for now a top level QB can have 1 where clause SQ predicate.    */
specifier|private
name|QBSubQuery
name|whereClauseSubQueryPredicate
decl_stmt|;
comment|/*    * for now a top level QB can have 1 where clause SQ predicate.    */
specifier|private
name|QBSubQuery
name|havingClauseSubQueryPredicate
decl_stmt|;
comment|// results
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
block|{   }
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
comment|// Must be deterministic order maps - see HIVE-8707
name|aliasToTabs
operator|=
operator|new
name|LinkedHashMap
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
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|QBExpr
argument_list|>
argument_list|()
expr_stmt|;
name|viewAliasToViewSchema
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Table
argument_list|>
argument_list|()
expr_stmt|;
name|aliasToProps
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|aliases
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
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
comment|// Must be deterministic order maps - see HIVE-8707
name|ptfNodeToSpec
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|ASTNode
argument_list|,
name|PTFInvocationSpec
argument_list|>
argument_list|()
expr_stmt|;
name|destToWindowingSpec
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|WindowingSpec
argument_list|>
argument_list|()
expr_stmt|;
name|id
operator|=
name|getAppendedAliasFromId
argument_list|(
name|outer_id
argument_list|,
name|alias
argument_list|)
expr_stmt|;
name|aliasInsideView
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
block|}
comment|// For sub-queries, the id. and alias should be appended since same aliases can be re-used
comment|// within different sub-queries.
comment|// For a query like:
comment|// select ...
comment|//   (select * from T1 a where ...) subq1
comment|//  join
comment|//   (select * from T2 a where ...) subq2
comment|// ..
comment|// the alias is modified to subq1:a and subq2:a from a, to identify the right sub-query.
specifier|public
specifier|static
name|String
name|getAppendedAliasFromId
parameter_list|(
name|String
name|outer_id
parameter_list|,
name|String
name|alias
parameter_list|)
block|{
return|return
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
return|;
block|}
specifier|public
name|String
name|getAlias
parameter_list|()
block|{
return|return
name|qbp
operator|.
name|getAlias
argument_list|()
return|;
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
block|{
return|return
literal|true
return|;
block|}
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
name|void
name|setTabProps
parameter_list|(
name|String
name|alias
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
parameter_list|)
block|{
name|aliasToProps
operator|.
name|put
argument_list|(
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|props
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addAlias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
if|if
condition|(
operator|!
name|aliases
operator|.
name|contains
argument_list|(
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
condition|)
block|{
name|aliases
operator|.
name|add
argument_list|(
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|List
argument_list|<
name|String
argument_list|>
name|getAliases
parameter_list|()
block|{
return|return
name|aliases
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
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getTabPropsForAlias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|aliasToProps
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
parameter_list|,
name|Table
name|tab
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
if|if
condition|(
name|tab
operator|!=
literal|null
condition|)
block|{
name|viewAliasToViewSchema
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|tab
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|rewriteCTEToSubq
parameter_list|(
name|String
name|alias
parameter_list|,
name|String
name|cteName
parameter_list|,
name|QBExpr
name|qbexpr
parameter_list|)
block|{
name|rewriteViewToSubq
argument_list|(
name|alias
argument_list|,
name|cteName
argument_list|,
name|qbexpr
argument_list|,
literal|null
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
comment|/**    * Set to true in SemanticAnalyzer.getMetadataForDestFile,    * if destination is a file and query is not CTAS    * @return    */
specifier|public
name|boolean
name|getIsQuery
parameter_list|()
block|{
return|return
name|isQuery
return|;
block|}
comment|// to decide whether to rewrite RR of subquery
specifier|public
name|boolean
name|isTopLevelSelectStarQuery
parameter_list|()
block|{
return|return
operator|!
name|isCTAS
argument_list|()
operator|&&
name|qbp
operator|.
name|isTopLevelSimpleSelectStarQuery
argument_list|()
return|;
block|}
comment|// to find target for fetch task conversion optimizer (not allows subqueries)
specifier|public
name|boolean
name|isSimpleSelectQuery
parameter_list|()
block|{
if|if
condition|(
operator|!
name|qbp
operator|.
name|isSimpleSelectQuery
argument_list|()
operator|||
name|isCTAS
argument_list|()
operator|||
name|qbp
operator|.
name|isAnalyzeCommand
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|QBExpr
name|qbexpr
range|:
name|aliasToSubq
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|qbexpr
operator|.
name|isSimpleSelectQuery
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|boolean
name|hasTableSample
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|qbp
operator|.
name|getTabSample
argument_list|(
name|alias
argument_list|)
operator|!=
literal|null
return|;
block|}
specifier|public
name|CreateTableDesc
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
name|CreateTableDesc
name|desc
parameter_list|)
block|{
name|tblDesc
operator|=
name|desc
expr_stmt|;
block|}
specifier|public
name|CreateTableDesc
name|getDirectoryDesc
parameter_list|()
block|{
return|return
name|directoryDesc
return|;
block|}
specifier|public
name|void
name|setDirectoryDesc
parameter_list|(
name|CreateTableDesc
name|directoryDesc
parameter_list|)
block|{
name|this
operator|.
name|directoryDesc
operator|=
name|directoryDesc
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
comment|/**    * Retrieve skewed column name for a table.    * @param alias table alias    * @return    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getSkewedColumnNames
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|skewedColNames
init|=
literal|null
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|qbm
operator|&&
literal|null
operator|!=
name|qbm
operator|.
name|getAliasToTable
argument_list|()
operator|&&
name|qbm
operator|.
name|getAliasToTable
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Table
name|tbl
init|=
name|getMetaData
argument_list|()
operator|.
name|getTableForAlias
argument_list|(
name|alias
argument_list|)
decl_stmt|;
name|skewedColNames
operator|=
name|tbl
operator|.
name|getSkewedColNames
argument_list|()
expr_stmt|;
block|}
return|return
name|skewedColNames
return|;
block|}
specifier|public
name|boolean
name|isAnalyzeRewrite
parameter_list|()
block|{
return|return
name|isAnalyzeRewrite
return|;
block|}
specifier|public
name|void
name|setAnalyzeRewrite
parameter_list|(
name|boolean
name|isAnalyzeRewrite
parameter_list|)
block|{
name|this
operator|.
name|isAnalyzeRewrite
operator|=
name|isAnalyzeRewrite
expr_stmt|;
block|}
specifier|public
name|PTFInvocationSpec
name|getPTFInvocationSpec
parameter_list|(
name|ASTNode
name|node
parameter_list|)
block|{
return|return
name|ptfNodeToSpec
operator|==
literal|null
condition|?
literal|null
else|:
name|ptfNodeToSpec
operator|.
name|get
argument_list|(
name|node
argument_list|)
return|;
block|}
specifier|public
name|void
name|addPTFNodeToSpec
parameter_list|(
name|ASTNode
name|node
parameter_list|,
name|PTFInvocationSpec
name|spec
parameter_list|)
block|{
comment|// Must be deterministic order map - see HIVE-8707
name|ptfNodeToSpec
operator|=
name|ptfNodeToSpec
operator|==
literal|null
condition|?
operator|new
name|LinkedHashMap
argument_list|<
name|ASTNode
argument_list|,
name|PTFInvocationSpec
argument_list|>
argument_list|()
else|:
name|ptfNodeToSpec
expr_stmt|;
name|ptfNodeToSpec
operator|.
name|put
argument_list|(
name|node
argument_list|,
name|spec
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HashMap
argument_list|<
name|ASTNode
argument_list|,
name|PTFInvocationSpec
argument_list|>
name|getPTFNodeToSpec
parameter_list|()
block|{
return|return
name|ptfNodeToSpec
return|;
block|}
specifier|public
name|WindowingSpec
name|getWindowingSpec
parameter_list|(
name|String
name|dest
parameter_list|)
block|{
return|return
name|destToWindowingSpec
operator|.
name|get
argument_list|(
name|dest
argument_list|)
return|;
block|}
specifier|public
name|void
name|addDestToWindowingSpec
parameter_list|(
name|String
name|dest
parameter_list|,
name|WindowingSpec
name|windowingSpec
parameter_list|)
block|{
name|destToWindowingSpec
operator|.
name|put
argument_list|(
name|dest
argument_list|,
name|windowingSpec
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasWindowingSpec
parameter_list|(
name|String
name|dest
parameter_list|)
block|{
return|return
name|destToWindowingSpec
operator|.
name|get
argument_list|(
name|dest
argument_list|)
operator|!=
literal|null
return|;
block|}
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|WindowingSpec
argument_list|>
name|getAllWindowingSpecs
parameter_list|()
block|{
return|return
name|destToWindowingSpec
return|;
block|}
specifier|protected
name|void
name|setSubQueryDef
parameter_list|(
name|QBSubQuery
name|subQueryPredicateDef
parameter_list|)
block|{
name|this
operator|.
name|subQueryPredicateDef
operator|=
name|subQueryPredicateDef
expr_stmt|;
block|}
specifier|protected
name|QBSubQuery
name|getSubQueryPredicateDef
parameter_list|()
block|{
return|return
name|subQueryPredicateDef
return|;
block|}
specifier|protected
name|int
name|getNumSubQueryPredicates
parameter_list|()
block|{
return|return
name|numSubQueryPredicates
return|;
block|}
specifier|protected
name|int
name|incrNumSubQueryPredicates
parameter_list|()
block|{
return|return
operator|++
name|numSubQueryPredicates
return|;
block|}
name|void
name|setWhereClauseSubQueryPredicate
parameter_list|(
name|QBSubQuery
name|sq
parameter_list|)
block|{
name|whereClauseSubQueryPredicate
operator|=
name|sq
expr_stmt|;
block|}
specifier|public
name|QBSubQuery
name|getWhereClauseSubQueryPredicate
parameter_list|()
block|{
return|return
name|whereClauseSubQueryPredicate
return|;
block|}
name|void
name|setHavingClauseSubQueryPredicate
parameter_list|(
name|QBSubQuery
name|sq
parameter_list|)
block|{
name|havingClauseSubQueryPredicate
operator|=
name|sq
expr_stmt|;
block|}
specifier|public
name|QBSubQuery
name|getHavingClauseSubQueryPredicate
parameter_list|()
block|{
return|return
name|havingClauseSubQueryPredicate
return|;
block|}
specifier|public
name|CreateViewDesc
name|getViewDesc
parameter_list|()
block|{
return|return
name|viewDesc
return|;
block|}
specifier|public
name|void
name|setViewDesc
parameter_list|(
name|CreateViewDesc
name|viewDesc
parameter_list|)
block|{
name|this
operator|.
name|viewDesc
operator|=
name|viewDesc
expr_stmt|;
block|}
specifier|public
name|boolean
name|isMaterializedView
parameter_list|()
block|{
return|return
name|viewDesc
operator|!=
literal|null
operator|&&
name|viewDesc
operator|.
name|isMaterialized
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isView
parameter_list|()
block|{
return|return
name|viewDesc
operator|!=
literal|null
operator|&&
operator|!
name|viewDesc
operator|.
name|isMaterialized
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isMultiDestQuery
parameter_list|()
block|{
return|return
name|qbp
operator|!=
literal|null
operator|&&
name|qbp
operator|.
name|getClauseNamesForDest
argument_list|()
operator|!=
literal|null
operator|&&
name|qbp
operator|.
name|getClauseNamesForDest
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|1
return|;
block|}
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|Table
argument_list|>
name|getViewToTabSchema
parameter_list|()
block|{
return|return
name|viewAliasToViewSchema
return|;
block|}
specifier|public
name|boolean
name|isInsideView
parameter_list|()
block|{
return|return
name|insideView
return|;
block|}
specifier|public
name|void
name|setInsideView
parameter_list|(
name|boolean
name|insideView
parameter_list|)
block|{
name|this
operator|.
name|insideView
operator|=
name|insideView
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getAliasInsideView
parameter_list|()
block|{
return|return
name|aliasInsideView
return|;
block|}
comment|/**    * returns true, if the query block contains any query, or subquery without a source table    * Like select current_user(), select current_database()    * @return true, if the query block contains any query without a source table    */
specifier|public
name|boolean
name|containsQueryWithoutSourceTable
parameter_list|()
block|{
for|for
control|(
name|QBExpr
name|qbexpr
range|:
name|aliasToSubq
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|qbexpr
operator|.
name|containsQueryWithoutSourceTable
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
name|aliasToTabs
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|&&
name|aliasToSubq
operator|.
name|size
argument_list|()
operator|==
literal|0
return|;
block|}
block|}
end_class

end_unit

