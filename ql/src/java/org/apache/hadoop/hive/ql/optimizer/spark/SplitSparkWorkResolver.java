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
name|optimizer
operator|.
name|spark
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
name|HashSet
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
name|LinkedList
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
name|Queue
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
name|exec
operator|.
name|SerializationUtilities
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
name|Task
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
name|spark
operator|.
name|SparkTask
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
name|optimizer
operator|.
name|physical
operator|.
name|PhysicalContext
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
name|optimizer
operator|.
name|physical
operator|.
name|PhysicalPlanResolver
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
name|SemanticException
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
name|spark
operator|.
name|GenSparkUtils
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
name|BaseWork
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
name|MapWork
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
name|OperatorDesc
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
name|ReduceWork
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
name|SparkEdgeProperty
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
name|SparkWork
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Do a BFS on the sparkWork graph, and look for any work that has more than one child.  * If we found such a work, we split it into multiple ones, one for each of its child.  */
end_comment

begin_class
specifier|public
class|class
name|SplitSparkWorkResolver
implements|implements
name|PhysicalPlanResolver
block|{
annotation|@
name|Override
specifier|public
name|PhysicalContext
name|resolve
parameter_list|(
name|PhysicalContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
for|for
control|(
name|Task
argument_list|<
name|?
argument_list|>
name|task
range|:
name|pctx
operator|.
name|getRootTasks
argument_list|()
control|)
block|{
if|if
condition|(
name|task
operator|instanceof
name|SparkTask
condition|)
block|{
name|splitSparkWork
argument_list|(
operator|(
operator|(
name|SparkTask
operator|)
name|task
operator|)
operator|.
name|getWork
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|pctx
return|;
block|}
specifier|private
name|void
name|splitSparkWork
parameter_list|(
name|SparkWork
name|sparkWork
parameter_list|)
block|{
name|Queue
argument_list|<
name|BaseWork
argument_list|>
name|queue
init|=
operator|new
name|LinkedList
argument_list|<
name|BaseWork
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|visited
init|=
operator|new
name|HashSet
argument_list|<
name|BaseWork
argument_list|>
argument_list|()
decl_stmt|;
name|queue
operator|.
name|addAll
argument_list|(
name|sparkWork
operator|.
name|getRoots
argument_list|()
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|queue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|BaseWork
name|work
init|=
name|queue
operator|.
name|poll
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|visited
operator|.
name|add
argument_list|(
name|work
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|List
argument_list|<
name|BaseWork
argument_list|>
name|childWorks
init|=
name|sparkWork
operator|.
name|getChildren
argument_list|(
name|work
argument_list|)
decl_stmt|;
comment|// First, add all children of this work into queue, to be processed later.
for|for
control|(
name|BaseWork
name|w
range|:
name|childWorks
control|)
block|{
name|queue
operator|.
name|add
argument_list|(
name|w
argument_list|)
expr_stmt|;
block|}
comment|// Second, check if this work has multiple reduceSinks. If so, do split.
name|splitBaseWork
argument_list|(
name|sparkWork
argument_list|,
name|work
argument_list|,
name|childWorks
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Split work into multiple branches, one for each childWork in childWorks.
comment|// It also set up the connection between each parent work and child work.
specifier|private
name|void
name|splitBaseWork
parameter_list|(
name|SparkWork
name|sparkWork
parameter_list|,
name|BaseWork
name|parentWork
parameter_list|,
name|List
argument_list|<
name|BaseWork
argument_list|>
name|childWorks
parameter_list|)
block|{
if|if
condition|(
name|getAllReduceSinks
argument_list|(
name|parentWork
argument_list|)
operator|.
name|size
argument_list|()
operator|<=
literal|1
condition|)
block|{
return|return;
block|}
comment|// Grand-parent works - we need to set these to be the parents of the cloned works.
name|List
argument_list|<
name|BaseWork
argument_list|>
name|grandParentWorks
init|=
name|sparkWork
operator|.
name|getParents
argument_list|(
name|parentWork
argument_list|)
decl_stmt|;
name|boolean
name|isFirst
init|=
literal|true
decl_stmt|;
for|for
control|(
name|BaseWork
name|childWork
range|:
name|childWorks
control|)
block|{
name|BaseWork
name|clonedParentWork
init|=
name|SerializationUtilities
operator|.
name|cloneBaseWork
argument_list|(
name|parentWork
argument_list|)
decl_stmt|;
comment|// give the cloned work a different name
name|clonedParentWork
operator|.
name|setName
argument_list|(
name|clonedParentWork
operator|.
name|getName
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"^([a-zA-Z]+)(\\s+)(\\d+)"
argument_list|,
literal|"$1$2"
operator|+
name|GenSparkUtils
operator|.
name|getUtils
argument_list|()
operator|.
name|getNextSeqNumber
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|setStatistics
argument_list|(
name|parentWork
argument_list|,
name|clonedParentWork
argument_list|)
expr_stmt|;
name|String
name|childReducerName
init|=
name|childWork
operator|.
name|getName
argument_list|()
decl_stmt|;
name|SparkEdgeProperty
name|clonedEdgeProperty
init|=
name|sparkWork
operator|.
name|getEdgeProperty
argument_list|(
name|parentWork
argument_list|,
name|childWork
argument_list|)
decl_stmt|;
comment|// We need to remove those branches that
comment|// 1, ended with a ReduceSinkOperator, and
comment|// 2, the ReduceSinkOperator's name is not the same as childReducerName.
comment|// Also, if the cloned work is not the first, we remove ALL leaf operators except
comment|// the corresponding ReduceSinkOperator.
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|clonedParentWork
operator|.
name|getAllLeafOperators
argument_list|()
control|)
block|{
if|if
condition|(
name|op
operator|instanceof
name|ReduceSinkOperator
condition|)
block|{
if|if
condition|(
operator|!
operator|(
operator|(
name|ReduceSinkOperator
operator|)
name|op
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|getOutputName
argument_list|()
operator|.
name|equals
argument_list|(
name|childReducerName
argument_list|)
condition|)
block|{
name|removeOpRecursive
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|isFirst
condition|)
block|{
name|removeOpRecursive
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
block|}
name|isFirst
operator|=
literal|false
expr_stmt|;
comment|// Then, we need to set up the graph connection. Especially:
comment|// 1, we need to connect this cloned parent work with all the grand-parent works.
comment|// 2, we need to connect this cloned parent work with the corresponding child work.
name|sparkWork
operator|.
name|add
argument_list|(
name|clonedParentWork
argument_list|)
expr_stmt|;
for|for
control|(
name|BaseWork
name|gpw
range|:
name|grandParentWorks
control|)
block|{
name|sparkWork
operator|.
name|connect
argument_list|(
name|gpw
argument_list|,
name|clonedParentWork
argument_list|,
name|sparkWork
operator|.
name|getEdgeProperty
argument_list|(
name|gpw
argument_list|,
name|parentWork
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|sparkWork
operator|.
name|connect
argument_list|(
name|clonedParentWork
argument_list|,
name|childWork
argument_list|,
name|clonedEdgeProperty
argument_list|)
expr_stmt|;
name|sparkWork
operator|.
name|getCloneToWork
argument_list|()
operator|.
name|put
argument_list|(
name|clonedParentWork
argument_list|,
name|parentWork
argument_list|)
expr_stmt|;
block|}
name|sparkWork
operator|.
name|remove
argument_list|(
name|parentWork
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|getAllReduceSinks
parameter_list|(
name|BaseWork
name|work
parameter_list|)
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|resultSet
init|=
name|work
operator|.
name|getAllLeafOperators
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|it
init|=
name|resultSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|it
operator|.
name|next
argument_list|()
operator|instanceof
name|ReduceSinkOperator
operator|)
condition|)
block|{
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|resultSet
return|;
block|}
comment|// Remove op from all its parents' child list.
comment|// Recursively remove any of its parent who only have this op as child.
specifier|private
name|void
name|removeOpRecursive
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|operator
parameter_list|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|parentOperators
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|operator
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
name|parentOperators
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|parentOperator
range|:
name|parentOperators
control|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|parentOperator
operator|.
name|getChildOperators
argument_list|()
operator|.
name|contains
argument_list|(
name|operator
argument_list|)
argument_list|,
literal|"AssertionError: parent of "
operator|+
name|operator
operator|.
name|getName
argument_list|()
operator|+
literal|" doesn't have it as child."
argument_list|)
expr_stmt|;
name|parentOperator
operator|.
name|removeChild
argument_list|(
name|operator
argument_list|)
expr_stmt|;
if|if
condition|(
name|parentOperator
operator|.
name|getNumChild
argument_list|()
operator|==
literal|0
condition|)
block|{
name|removeOpRecursive
argument_list|(
name|parentOperator
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// we lost statistics& opTraits through cloning, try to get them back
specifier|private
name|void
name|setStatistics
parameter_list|(
name|BaseWork
name|origin
parameter_list|,
name|BaseWork
name|clone
parameter_list|)
block|{
if|if
condition|(
name|origin
operator|instanceof
name|MapWork
operator|&&
name|clone
operator|instanceof
name|MapWork
condition|)
block|{
name|MapWork
name|originMW
init|=
operator|(
name|MapWork
operator|)
name|origin
decl_stmt|;
name|MapWork
name|cloneMW
init|=
operator|(
name|MapWork
operator|)
name|clone
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|entry
range|:
name|originMW
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|alias
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|cloneOP
init|=
name|cloneMW
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|cloneOP
operator|!=
literal|null
condition|)
block|{
name|setStatistics
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|cloneOP
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|origin
operator|instanceof
name|ReduceWork
operator|&&
name|clone
operator|instanceof
name|ReduceWork
condition|)
block|{
name|setStatistics
argument_list|(
operator|(
operator|(
name|ReduceWork
operator|)
name|origin
operator|)
operator|.
name|getReducer
argument_list|()
argument_list|,
operator|(
operator|(
name|ReduceWork
operator|)
name|clone
operator|)
operator|.
name|getReducer
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|setStatistics
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|origin
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|clone
parameter_list|)
block|{
name|clone
operator|.
name|getConf
argument_list|()
operator|.
name|setStatistics
argument_list|(
name|origin
operator|.
name|getConf
argument_list|()
operator|.
name|getStatistics
argument_list|()
argument_list|)
expr_stmt|;
name|clone
operator|.
name|getConf
argument_list|()
operator|.
name|setTraits
argument_list|(
name|origin
operator|.
name|getConf
argument_list|()
operator|.
name|getTraits
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|origin
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
name|clone
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
condition|)
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
name|clone
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|setStatistics
argument_list|(
name|origin
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|clone
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

