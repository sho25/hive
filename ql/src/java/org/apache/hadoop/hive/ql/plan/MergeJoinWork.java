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
name|Map
operator|.
name|Entry
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|CommonMergeJoinOperator
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
name|HashTableDummyOperator
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
name|ReduceSinkOperator
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
name|Explain
operator|.
name|Level
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_class
specifier|public
class|class
name|MergeJoinWork
extends|extends
name|BaseWork
block|{
specifier|private
name|CommonMergeJoinOperator
name|mergeJoinOp
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|BaseWork
argument_list|>
name|mergeWorkList
init|=
operator|new
name|ArrayList
argument_list|<
name|BaseWork
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|BaseWork
name|bigTableWork
decl_stmt|;
specifier|public
name|MergeJoinWork
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|replaceRoots
parameter_list|(
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|replacementMap
parameter_list|)
block|{
name|getMainWork
argument_list|()
operator|.
name|replaceRoots
argument_list|(
name|replacementMap
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|getAllRootOperators
parameter_list|()
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|set
init|=
operator|new
name|LinkedHashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|set
operator|.
name|addAll
argument_list|(
name|getMainWork
argument_list|()
operator|.
name|getAllRootOperators
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|mergeWorkList
control|)
block|{
name|set
operator|.
name|addAll
argument_list|(
name|w
operator|.
name|getAllRootOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|set
return|;
block|}
annotation|@
name|Override
specifier|public
name|Operator
argument_list|<
name|?
argument_list|>
name|getAnyRootOperator
parameter_list|()
block|{
return|return
name|getMainWork
argument_list|()
operator|.
name|getAnyRootOperator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|configureJobConf
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{   }
specifier|public
name|CommonMergeJoinOperator
name|getMergeJoinOperator
parameter_list|()
block|{
return|return
name|this
operator|.
name|mergeJoinOp
return|;
block|}
specifier|public
name|void
name|setMergeJoinOperator
parameter_list|(
name|CommonMergeJoinOperator
name|mergeJoinOp
parameter_list|)
block|{
name|this
operator|.
name|mergeJoinOp
operator|=
name|mergeJoinOp
expr_stmt|;
block|}
specifier|public
name|void
name|addMergedWork
parameter_list|(
name|BaseWork
name|work
parameter_list|,
name|BaseWork
name|connectWork
parameter_list|,
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|BaseWork
argument_list|>
name|leafOperatorToFollowingWork
parameter_list|)
block|{
if|if
condition|(
name|work
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|(
name|bigTableWork
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|bigTableWork
operator|!=
name|work
operator|)
condition|)
block|{
assert|assert
literal|false
assert|;
block|}
name|this
operator|.
name|bigTableWork
operator|=
name|work
expr_stmt|;
name|setName
argument_list|(
name|work
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|connectWork
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|mergeWorkList
operator|.
name|add
argument_list|(
name|connectWork
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|connectWork
operator|instanceof
name|ReduceWork
operator|)
operator|&&
operator|(
name|bigTableWork
operator|!=
literal|null
operator|)
condition|)
block|{
comment|/*          * For tez to route data from an up-stream vertex correctly to the following vertex, the          * output name in the reduce sink needs to be setup appropriately. In the case of reduce          * side merge work, we need to ensure that the parent work that provides data to this merge          * work is setup to point to the right vertex name - the main work name.          *          * In this case, if the big table work has already been created, we can hook up the merge          * work items for the small table correctly.          */
name|setReduceSinkOutputName
argument_list|(
name|connectWork
argument_list|,
name|leafOperatorToFollowingWork
argument_list|,
name|bigTableWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|work
operator|!=
literal|null
condition|)
block|{
comment|/*        * Same reason as above. This is the case when we have the main work item after the merge work        * has been created for the small table side.        */
for|for
control|(
name|BaseWork
name|mergeWork
range|:
name|mergeWorkList
control|)
block|{
if|if
condition|(
name|mergeWork
operator|instanceof
name|ReduceWork
condition|)
block|{
name|setReduceSinkOutputName
argument_list|(
name|mergeWork
argument_list|,
name|leafOperatorToFollowingWork
argument_list|,
name|work
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|setReduceSinkOutputName
parameter_list|(
name|BaseWork
name|mergeWork
parameter_list|,
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|BaseWork
argument_list|>
name|leafOperatorToFollowingWork
parameter_list|,
name|String
name|name
parameter_list|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|BaseWork
argument_list|>
name|entry
range|:
name|leafOperatorToFollowingWork
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|==
name|mergeWork
condition|)
block|{
operator|(
operator|(
name|ReduceSinkOperator
operator|)
name|entry
operator|.
name|getKey
argument_list|()
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|setOutputName
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Explain
argument_list|(
name|skipHeader
operator|=
literal|true
argument_list|,
name|displayName
operator|=
literal|"Join"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|List
argument_list|<
name|BaseWork
argument_list|>
name|getBaseWorkList
parameter_list|()
block|{
return|return
name|mergeWorkList
return|;
block|}
specifier|public
name|String
name|getBigTableAlias
parameter_list|()
block|{
return|return
operator|(
operator|(
name|MapWork
operator|)
name|bigTableWork
operator|)
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|skipHeader
operator|=
literal|true
argument_list|,
name|displayName
operator|=
literal|"Main"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|BaseWork
name|getMainWork
parameter_list|()
block|{
return|return
name|bigTableWork
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setDummyOps
parameter_list|(
name|List
argument_list|<
name|HashTableDummyOperator
argument_list|>
name|dummyOps
parameter_list|)
block|{
name|getMainWork
argument_list|()
operator|.
name|setDummyOps
argument_list|(
name|dummyOps
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|HashTableDummyOperator
argument_list|>
name|getDummyOps
parameter_list|()
block|{
return|return
name|getMainWork
argument_list|()
operator|.
name|getDummyOps
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setVectorMode
parameter_list|(
name|boolean
name|vectorMode
parameter_list|)
block|{
name|getMainWork
argument_list|()
operator|.
name|setVectorMode
argument_list|(
name|vectorMode
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|getVectorMode
parameter_list|()
block|{
return|return
name|getMainWork
argument_list|()
operator|.
name|getVectorMode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setUberMode
parameter_list|(
name|boolean
name|uberMode
parameter_list|)
block|{
name|getMainWork
argument_list|()
operator|.
name|setUberMode
argument_list|(
name|uberMode
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|getUberMode
parameter_list|()
block|{
return|return
name|getMainWork
argument_list|()
operator|.
name|getUberMode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setLlapMode
parameter_list|(
name|boolean
name|llapMode
parameter_list|)
block|{
name|getMainWork
argument_list|()
operator|.
name|setLlapMode
argument_list|(
name|llapMode
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|getLlapMode
parameter_list|()
block|{
return|return
name|getMainWork
argument_list|()
operator|.
name|getLlapMode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addDummyOp
parameter_list|(
name|HashTableDummyOperator
name|dummyOp
parameter_list|)
block|{
name|getMainWork
argument_list|()
operator|.
name|addDummyOp
argument_list|(
name|dummyOp
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

