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
package|;
end_package

begin_comment
comment|/**  *  * QueryProperties.  *  * A structure to contain features of a query that are determined  * during parsing and may be useful for categorizing a query type  *  * These inlucde whether the query contains:  * a join clause, a group by clause, an order by clause, a sort by  * clause, a group by clause following a join clause, and whether  * the query uses a script for mapping/reducing  */
end_comment

begin_class
specifier|public
class|class
name|QueryProperties
block|{
name|boolean
name|hasJoin
init|=
literal|false
decl_stmt|;
name|boolean
name|hasGroupBy
init|=
literal|false
decl_stmt|;
name|boolean
name|hasOrderBy
init|=
literal|false
decl_stmt|;
name|boolean
name|hasSortBy
init|=
literal|false
decl_stmt|;
name|boolean
name|hasJoinFollowedByGroupBy
init|=
literal|false
decl_stmt|;
name|boolean
name|hasPTF
init|=
literal|false
decl_stmt|;
name|boolean
name|hasWindowing
init|=
literal|false
decl_stmt|;
comment|// does the query have a using clause
name|boolean
name|usesScript
init|=
literal|false
decl_stmt|;
name|boolean
name|hasDistributeBy
init|=
literal|false
decl_stmt|;
name|boolean
name|hasClusterBy
init|=
literal|false
decl_stmt|;
name|boolean
name|mapJoinRemoved
init|=
literal|false
decl_stmt|;
name|boolean
name|hasMapGroupBy
init|=
literal|false
decl_stmt|;
specifier|private
name|int
name|noOfJoins
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|noOfOuterJoins
init|=
literal|0
decl_stmt|;
specifier|private
name|boolean
name|multiDestQuery
decl_stmt|;
specifier|private
name|boolean
name|filterWithSubQuery
decl_stmt|;
specifier|public
name|boolean
name|hasJoin
parameter_list|()
block|{
return|return
operator|(
name|noOfJoins
operator|>
literal|0
operator|)
return|;
block|}
specifier|public
name|void
name|incrementJoinCount
parameter_list|(
name|boolean
name|noOuterJoin
parameter_list|)
block|{
name|noOfJoins
operator|++
expr_stmt|;
if|if
condition|(
operator|!
name|noOuterJoin
condition|)
name|noOfOuterJoins
operator|++
expr_stmt|;
block|}
specifier|public
name|int
name|getJoinCount
parameter_list|()
block|{
return|return
name|noOfJoins
return|;
block|}
specifier|public
name|int
name|getOuterJoinCount
parameter_list|()
block|{
return|return
name|noOfOuterJoins
return|;
block|}
specifier|public
name|boolean
name|hasGroupBy
parameter_list|()
block|{
return|return
name|hasGroupBy
return|;
block|}
specifier|public
name|void
name|setHasGroupBy
parameter_list|(
name|boolean
name|hasGroupBy
parameter_list|)
block|{
name|this
operator|.
name|hasGroupBy
operator|=
name|hasGroupBy
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasOrderBy
parameter_list|()
block|{
return|return
name|hasOrderBy
return|;
block|}
specifier|public
name|void
name|setHasOrderBy
parameter_list|(
name|boolean
name|hasOrderBy
parameter_list|)
block|{
name|this
operator|.
name|hasOrderBy
operator|=
name|hasOrderBy
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasSortBy
parameter_list|()
block|{
return|return
name|hasSortBy
return|;
block|}
specifier|public
name|void
name|setHasSortBy
parameter_list|(
name|boolean
name|hasSortBy
parameter_list|)
block|{
name|this
operator|.
name|hasSortBy
operator|=
name|hasSortBy
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasJoinFollowedByGroupBy
parameter_list|()
block|{
return|return
name|hasJoinFollowedByGroupBy
return|;
block|}
specifier|public
name|void
name|setHasJoinFollowedByGroupBy
parameter_list|(
name|boolean
name|hasJoinFollowedByGroupBy
parameter_list|)
block|{
name|this
operator|.
name|hasJoinFollowedByGroupBy
operator|=
name|hasJoinFollowedByGroupBy
expr_stmt|;
block|}
specifier|public
name|boolean
name|usesScript
parameter_list|()
block|{
return|return
name|usesScript
return|;
block|}
specifier|public
name|void
name|setUsesScript
parameter_list|(
name|boolean
name|usesScript
parameter_list|)
block|{
name|this
operator|.
name|usesScript
operator|=
name|usesScript
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasDistributeBy
parameter_list|()
block|{
return|return
name|hasDistributeBy
return|;
block|}
specifier|public
name|void
name|setHasDistributeBy
parameter_list|(
name|boolean
name|hasDistributeBy
parameter_list|)
block|{
name|this
operator|.
name|hasDistributeBy
operator|=
name|hasDistributeBy
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasClusterBy
parameter_list|()
block|{
return|return
name|hasClusterBy
return|;
block|}
specifier|public
name|void
name|setHasClusterBy
parameter_list|(
name|boolean
name|hasClusterBy
parameter_list|)
block|{
name|this
operator|.
name|hasClusterBy
operator|=
name|hasClusterBy
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasPTF
parameter_list|()
block|{
return|return
name|hasPTF
return|;
block|}
specifier|public
name|void
name|setHasPTF
parameter_list|(
name|boolean
name|hasPTF
parameter_list|)
block|{
name|this
operator|.
name|hasPTF
operator|=
name|hasPTF
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasWindowing
parameter_list|()
block|{
return|return
name|hasWindowing
return|;
block|}
specifier|public
name|void
name|setHasWindowing
parameter_list|(
name|boolean
name|hasWindowing
parameter_list|)
block|{
name|this
operator|.
name|hasWindowing
operator|=
name|hasWindowing
expr_stmt|;
block|}
specifier|public
name|boolean
name|isMapJoinRemoved
parameter_list|()
block|{
return|return
name|mapJoinRemoved
return|;
block|}
specifier|public
name|void
name|setMapJoinRemoved
parameter_list|(
name|boolean
name|mapJoinRemoved
parameter_list|)
block|{
name|this
operator|.
name|mapJoinRemoved
operator|=
name|mapJoinRemoved
expr_stmt|;
block|}
specifier|public
name|boolean
name|isHasMapGroupBy
parameter_list|()
block|{
return|return
name|hasMapGroupBy
return|;
block|}
specifier|public
name|void
name|setHasMapGroupBy
parameter_list|(
name|boolean
name|hasMapGroupBy
parameter_list|)
block|{
name|this
operator|.
name|hasMapGroupBy
operator|=
name|hasMapGroupBy
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasMultiDestQuery
parameter_list|()
block|{
return|return
name|this
operator|.
name|multiDestQuery
return|;
block|}
specifier|public
name|void
name|setMultiDestQuery
parameter_list|(
name|boolean
name|multiDestQuery
parameter_list|)
block|{
name|this
operator|.
name|multiDestQuery
operator|=
name|multiDestQuery
expr_stmt|;
block|}
specifier|public
name|void
name|setFilterWithSubQuery
parameter_list|(
name|boolean
name|filterWithSubQuery
parameter_list|)
block|{
name|this
operator|.
name|filterWithSubQuery
operator|=
name|filterWithSubQuery
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasFilterWithSubQuery
parameter_list|()
block|{
return|return
name|this
operator|.
name|filterWithSubQuery
return|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|hasJoin
operator|=
literal|false
expr_stmt|;
name|hasGroupBy
operator|=
literal|false
expr_stmt|;
name|hasOrderBy
operator|=
literal|false
expr_stmt|;
name|hasSortBy
operator|=
literal|false
expr_stmt|;
name|hasJoinFollowedByGroupBy
operator|=
literal|false
expr_stmt|;
name|hasPTF
operator|=
literal|false
expr_stmt|;
name|hasWindowing
operator|=
literal|false
expr_stmt|;
comment|// does the query have a using clause
name|usesScript
operator|=
literal|false
expr_stmt|;
name|hasDistributeBy
operator|=
literal|false
expr_stmt|;
name|hasClusterBy
operator|=
literal|false
expr_stmt|;
name|mapJoinRemoved
operator|=
literal|false
expr_stmt|;
name|hasMapGroupBy
operator|=
literal|false
expr_stmt|;
name|noOfJoins
operator|=
literal|0
expr_stmt|;
name|noOfOuterJoins
operator|=
literal|0
expr_stmt|;
name|multiDestQuery
operator|=
literal|false
expr_stmt|;
name|filterWithSubQuery
operator|=
literal|false
expr_stmt|;
block|}
block|}
end_class

end_unit

