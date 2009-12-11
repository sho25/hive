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
name|antlr
operator|.
name|runtime
operator|.
name|tree
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

begin_comment
comment|/**  * Implementation of the parse information related to a query block  *  **/
end_comment

begin_class
specifier|public
class|class
name|QBParseInfo
block|{
specifier|private
name|boolean
name|isSubQ
decl_stmt|;
specifier|private
name|String
name|alias
decl_stmt|;
specifier|private
name|ASTNode
name|joinExpr
decl_stmt|;
specifier|private
name|ASTNode
name|hints
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|aliasToSrc
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|nameToDest
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|TableSample
argument_list|>
name|nameToSample
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|destToSelExpr
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|destToWhereExpr
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|destToGroupby
decl_stmt|;
comment|/**    * ClusterBy is a short name for both DistributeBy and SortBy.      */
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|destToClusterby
decl_stmt|;
comment|/**    * DistributeBy controls the hashcode of the row, which determines which reducer    * the rows will go to.     */
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|destToDistributeby
decl_stmt|;
comment|/**    * SortBy controls the reduce keys, which affects the order of rows     * that the reducer receives.     */
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|destToSortby
decl_stmt|;
comment|/**    * Maping from table/subquery aliases to all the associated lateral view    * nodes    */
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|>
name|aliasToLateralViews
decl_stmt|;
comment|/* Order by clause */
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|destToOrderby
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|destToLimit
decl_stmt|;
specifier|private
name|int
name|outerQueryLimit
decl_stmt|;
comment|// used by GroupBy
specifier|private
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
argument_list|>
name|destToAggregationExprs
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|destToDistinctFuncExpr
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
name|QBParseInfo
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
name|QBParseInfo
parameter_list|(
name|String
name|alias
parameter_list|,
name|boolean
name|isSubQ
parameter_list|)
block|{
name|this
operator|.
name|aliasToSrc
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
name|this
operator|.
name|nameToDest
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
name|this
operator|.
name|nameToSample
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|TableSample
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|destToSelExpr
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
name|this
operator|.
name|destToWhereExpr
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
name|this
operator|.
name|destToGroupby
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
name|this
operator|.
name|destToClusterby
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
name|this
operator|.
name|destToDistributeby
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
name|this
operator|.
name|destToSortby
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
name|this
operator|.
name|destToOrderby
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
name|this
operator|.
name|destToLimit
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|destToAggregationExprs
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|destToDistinctFuncExpr
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
name|this
operator|.
name|alias
operator|=
name|alias
expr_stmt|;
name|this
operator|.
name|isSubQ
operator|=
name|isSubQ
expr_stmt|;
name|this
operator|.
name|outerQueryLimit
operator|=
operator|-
literal|1
expr_stmt|;
name|this
operator|.
name|aliasToLateralViews
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|setAggregationExprsForClause
parameter_list|(
name|String
name|clause
parameter_list|,
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|aggregationTrees
parameter_list|)
block|{
name|this
operator|.
name|destToAggregationExprs
operator|.
name|put
argument_list|(
name|clause
argument_list|,
name|aggregationTrees
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|getAggregationExprsForClause
parameter_list|(
name|String
name|clause
parameter_list|)
block|{
return|return
name|this
operator|.
name|destToAggregationExprs
operator|.
name|get
argument_list|(
name|clause
argument_list|)
return|;
block|}
specifier|public
name|void
name|setDistinctFuncExprForClause
parameter_list|(
name|String
name|clause
parameter_list|,
name|ASTNode
name|ast
parameter_list|)
block|{
name|this
operator|.
name|destToDistinctFuncExpr
operator|.
name|put
argument_list|(
name|clause
argument_list|,
name|ast
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ASTNode
name|getDistinctFuncExprForClause
parameter_list|(
name|String
name|clause
parameter_list|)
block|{
return|return
name|this
operator|.
name|destToDistinctFuncExpr
operator|.
name|get
argument_list|(
name|clause
argument_list|)
return|;
block|}
specifier|public
name|void
name|setSelExprForClause
parameter_list|(
name|String
name|clause
parameter_list|,
name|ASTNode
name|ast
parameter_list|)
block|{
name|this
operator|.
name|destToSelExpr
operator|.
name|put
argument_list|(
name|clause
argument_list|,
name|ast
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setWhrExprForClause
parameter_list|(
name|String
name|clause
parameter_list|,
name|ASTNode
name|ast
parameter_list|)
block|{
name|this
operator|.
name|destToWhereExpr
operator|.
name|put
argument_list|(
name|clause
argument_list|,
name|ast
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setGroupByExprForClause
parameter_list|(
name|String
name|clause
parameter_list|,
name|ASTNode
name|ast
parameter_list|)
block|{
name|this
operator|.
name|destToGroupby
operator|.
name|put
argument_list|(
name|clause
argument_list|,
name|ast
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setDestForClause
parameter_list|(
name|String
name|clause
parameter_list|,
name|ASTNode
name|ast
parameter_list|)
block|{
name|this
operator|.
name|nameToDest
operator|.
name|put
argument_list|(
name|clause
argument_list|,
name|ast
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the Cluster By AST for the clause.      * @param clause the name of the clause    * @param ast the abstract syntax tree    */
specifier|public
name|void
name|setClusterByExprForClause
parameter_list|(
name|String
name|clause
parameter_list|,
name|ASTNode
name|ast
parameter_list|)
block|{
name|this
operator|.
name|destToClusterby
operator|.
name|put
argument_list|(
name|clause
argument_list|,
name|ast
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the Distribute By AST for the clause.      * @param clause the name of the clause    * @param ast the abstract syntax tree    */
specifier|public
name|void
name|setDistributeByExprForClause
parameter_list|(
name|String
name|clause
parameter_list|,
name|ASTNode
name|ast
parameter_list|)
block|{
name|this
operator|.
name|destToDistributeby
operator|.
name|put
argument_list|(
name|clause
argument_list|,
name|ast
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the Sort By AST for the clause.      * @param clause the name of the clause    * @param ast the abstract syntax tree    */
specifier|public
name|void
name|setSortByExprForClause
parameter_list|(
name|String
name|clause
parameter_list|,
name|ASTNode
name|ast
parameter_list|)
block|{
name|this
operator|.
name|destToSortby
operator|.
name|put
argument_list|(
name|clause
argument_list|,
name|ast
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setOrderByExprForClause
parameter_list|(
name|String
name|clause
parameter_list|,
name|ASTNode
name|ast
parameter_list|)
block|{
name|this
operator|.
name|destToOrderby
operator|.
name|put
argument_list|(
name|clause
argument_list|,
name|ast
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setSrcForAlias
parameter_list|(
name|String
name|alias
parameter_list|,
name|ASTNode
name|ast
parameter_list|)
block|{
name|this
operator|.
name|aliasToSrc
operator|.
name|put
argument_list|(
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|ast
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getClauseNames
parameter_list|()
block|{
return|return
name|this
operator|.
name|destToSelExpr
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
name|getClauseNamesForDest
parameter_list|()
block|{
return|return
name|this
operator|.
name|nameToDest
operator|.
name|keySet
argument_list|()
return|;
block|}
specifier|public
name|ASTNode
name|getDestForClause
parameter_list|(
name|String
name|clause
parameter_list|)
block|{
return|return
name|this
operator|.
name|nameToDest
operator|.
name|get
argument_list|(
name|clause
argument_list|)
return|;
block|}
specifier|public
name|ASTNode
name|getWhrForClause
parameter_list|(
name|String
name|clause
parameter_list|)
block|{
return|return
name|this
operator|.
name|destToWhereExpr
operator|.
name|get
argument_list|(
name|clause
argument_list|)
return|;
block|}
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|getDestToWhereExpr
parameter_list|()
block|{
return|return
name|destToWhereExpr
return|;
block|}
specifier|public
name|ASTNode
name|getGroupByForClause
parameter_list|(
name|String
name|clause
parameter_list|)
block|{
return|return
name|this
operator|.
name|destToGroupby
operator|.
name|get
argument_list|(
name|clause
argument_list|)
return|;
block|}
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|getDestToGroupBy
parameter_list|()
block|{
return|return
name|this
operator|.
name|destToGroupby
return|;
block|}
specifier|public
name|ASTNode
name|getSelForClause
parameter_list|(
name|String
name|clause
parameter_list|)
block|{
return|return
name|this
operator|.
name|destToSelExpr
operator|.
name|get
argument_list|(
name|clause
argument_list|)
return|;
block|}
comment|/**    * Get the Cluster By AST for the clause.      * @param clause the name of the clause    * @return the abstract syntax tree    */
specifier|public
name|ASTNode
name|getClusterByForClause
parameter_list|(
name|String
name|clause
parameter_list|)
block|{
return|return
name|this
operator|.
name|destToClusterby
operator|.
name|get
argument_list|(
name|clause
argument_list|)
return|;
block|}
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|getDestToClusterBy
parameter_list|()
block|{
return|return
name|destToClusterby
return|;
block|}
comment|/**    * Get the Distribute By AST for the clause.      * @param clause the name of the clause    * @return the abstract syntax tree    */
specifier|public
name|ASTNode
name|getDistributeByForClause
parameter_list|(
name|String
name|clause
parameter_list|)
block|{
return|return
name|this
operator|.
name|destToDistributeby
operator|.
name|get
argument_list|(
name|clause
argument_list|)
return|;
block|}
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|getDestToDistributeBy
parameter_list|()
block|{
return|return
name|destToDistributeby
return|;
block|}
comment|/**    * Get the Sort By AST for the clause.      * @param clause the name of the clause    * @return the abstract syntax tree    */
specifier|public
name|ASTNode
name|getSortByForClause
parameter_list|(
name|String
name|clause
parameter_list|)
block|{
return|return
name|this
operator|.
name|destToSortby
operator|.
name|get
argument_list|(
name|clause
argument_list|)
return|;
block|}
specifier|public
name|ASTNode
name|getOrderByForClause
parameter_list|(
name|String
name|clause
parameter_list|)
block|{
return|return
name|this
operator|.
name|destToOrderby
operator|.
name|get
argument_list|(
name|clause
argument_list|)
return|;
block|}
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|getDestToSortBy
parameter_list|()
block|{
return|return
name|destToSortby
return|;
block|}
specifier|public
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|getDestToOrderBy
parameter_list|()
block|{
return|return
name|destToOrderby
return|;
block|}
specifier|public
name|ASTNode
name|getSrcForAlias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|this
operator|.
name|aliasToSrc
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
name|getAlias
parameter_list|()
block|{
return|return
name|this
operator|.
name|alias
return|;
block|}
specifier|public
name|boolean
name|getIsSubQ
parameter_list|()
block|{
return|return
name|this
operator|.
name|isSubQ
return|;
block|}
specifier|public
name|ASTNode
name|getJoinExpr
parameter_list|()
block|{
return|return
name|this
operator|.
name|joinExpr
return|;
block|}
specifier|public
name|void
name|setJoinExpr
parameter_list|(
name|ASTNode
name|joinExpr
parameter_list|)
block|{
name|this
operator|.
name|joinExpr
operator|=
name|joinExpr
expr_stmt|;
block|}
specifier|public
name|TableSample
name|getTabSample
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|this
operator|.
name|nameToSample
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
name|setTabSample
parameter_list|(
name|String
name|alias
parameter_list|,
name|TableSample
name|tableSample
parameter_list|)
block|{
name|this
operator|.
name|nameToSample
operator|.
name|put
argument_list|(
name|alias
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|tableSample
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setDestLimit
parameter_list|(
name|String
name|dest
parameter_list|,
name|Integer
name|limit
parameter_list|)
block|{
name|this
operator|.
name|destToLimit
operator|.
name|put
argument_list|(
name|dest
argument_list|,
name|limit
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Integer
name|getDestLimit
parameter_list|(
name|String
name|dest
parameter_list|)
block|{
return|return
name|this
operator|.
name|destToLimit
operator|.
name|get
argument_list|(
name|dest
argument_list|)
return|;
block|}
comment|/** 	 * @return the outerQueryLimit 	 */
specifier|public
name|int
name|getOuterQueryLimit
parameter_list|()
block|{
return|return
name|outerQueryLimit
return|;
block|}
comment|/** 	 * @param outerQueryLimit the outerQueryLimit to set 	 */
specifier|public
name|void
name|setOuterQueryLimit
parameter_list|(
name|int
name|outerQueryLimit
parameter_list|)
block|{
name|this
operator|.
name|outerQueryLimit
operator|=
name|outerQueryLimit
expr_stmt|;
block|}
specifier|public
name|boolean
name|isSelectStarQuery
parameter_list|()
block|{
if|if
condition|(
name|isSubQ
operator|||
operator|(
name|joinExpr
operator|!=
literal|null
operator|)
operator|||
operator|(
operator|!
name|nameToSample
operator|.
name|isEmpty
argument_list|()
operator|)
operator|||
operator|(
operator|!
name|destToGroupby
operator|.
name|isEmpty
argument_list|()
operator|)
operator|||
operator|(
operator|!
name|destToClusterby
operator|.
name|isEmpty
argument_list|()
operator|)
operator|||
operator|(
operator|!
name|aliasToLateralViews
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
return|return
literal|false
return|;
name|Iterator
argument_list|<
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
name|ASTNode
argument_list|>
argument_list|>
argument_list|>
name|aggrIter
init|=
name|destToAggregationExprs
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|aggrIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|h
init|=
name|aggrIter
operator|.
name|next
argument_list|()
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|h
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|h
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|destToDistinctFuncExpr
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
argument_list|>
name|distn
init|=
name|destToDistinctFuncExpr
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|distn
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|ASTNode
name|ct
init|=
name|distn
operator|.
name|next
argument_list|()
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|ct
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
block|}
block|}
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
argument_list|>
name|iter
init|=
name|nameToDest
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|entry
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|ASTNode
name|v
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
operator|(
operator|(
name|ASTNode
operator|)
name|v
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_TMP_FILE
operator|)
condition|)
return|return
literal|false
return|;
block|}
name|iter
operator|=
name|destToSelExpr
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
expr_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|entry
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|ASTNode
name|selExprList
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|// Iterate over the selects
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|selExprList
operator|.
name|getChildCount
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
comment|// list of the columns
name|ASTNode
name|selExpr
init|=
operator|(
name|ASTNode
operator|)
name|selExprList
operator|.
name|getChild
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ASTNode
name|sel
init|=
operator|(
name|ASTNode
operator|)
name|selExpr
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|sel
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|!=
name|HiveParser
operator|.
name|TOK_ALLCOLREF
condition|)
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
name|void
name|setHints
parameter_list|(
name|ASTNode
name|hint
parameter_list|)
block|{
name|this
operator|.
name|hints
operator|=
name|hint
expr_stmt|;
block|}
specifier|public
name|ASTNode
name|getHints
parameter_list|()
block|{
return|return
name|hints
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|>
name|getAliasToLateralViews
parameter_list|()
block|{
return|return
name|this
operator|.
name|aliasToLateralViews
return|;
block|}
specifier|public
name|List
argument_list|<
name|ASTNode
argument_list|>
name|getLateralViewsForAlias
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
return|return
name|aliasToLateralViews
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
name|addLateralViewForAlias
parameter_list|(
name|String
name|alias
parameter_list|,
name|ASTNode
name|lateralView
parameter_list|)
block|{
name|String
name|lowerAlias
init|=
name|alias
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
name|lateralViews
init|=
name|aliasToLateralViews
operator|.
name|get
argument_list|(
name|lowerAlias
argument_list|)
decl_stmt|;
if|if
condition|(
name|lateralViews
operator|==
literal|null
condition|)
block|{
name|lateralViews
operator|=
operator|new
name|ArrayList
argument_list|<
name|ASTNode
argument_list|>
argument_list|()
expr_stmt|;
name|aliasToLateralViews
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|lateralViews
argument_list|)
expr_stmt|;
block|}
name|lateralViews
operator|.
name|add
argument_list|(
name|lateralView
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

