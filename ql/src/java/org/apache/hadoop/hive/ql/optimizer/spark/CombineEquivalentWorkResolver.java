begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Comparator
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
name|Stack
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
name|collect
operator|.
name|Maps
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
name|collect
operator|.
name|Sets
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
name|exec
operator|.
name|OperatorUtils
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
name|SparkUtilities
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
name|SparkPartitionPruningSinkOperator
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
name|PartitionDesc
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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|MapJoinOperator
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
name|lib
operator|.
name|Dispatcher
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
name|lib
operator|.
name|Node
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
name|lib
operator|.
name|TaskGraphWalker
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
name|MapJoinDesc
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

begin_comment
comment|/**  * CombineEquivalentWorkResolver would search inside SparkWork, find and combine equivalent  * works.  */
end_comment

begin_class
specifier|public
class|class
name|CombineEquivalentWorkResolver
implements|implements
name|PhysicalPlanResolver
block|{
specifier|protected
specifier|static
specifier|transient
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|CombineEquivalentWorkResolver
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|removedMapWorkNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|PhysicalContext
name|pctx
decl_stmt|;
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
name|this
operator|.
name|pctx
operator|=
name|pctx
expr_stmt|;
name|List
argument_list|<
name|Node
argument_list|>
name|topNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|topNodes
operator|.
name|addAll
argument_list|(
name|pctx
operator|.
name|getRootTasks
argument_list|()
argument_list|)
expr_stmt|;
name|TaskGraphWalker
name|taskWalker
init|=
operator|new
name|TaskGraphWalker
argument_list|(
operator|new
name|EquivalentWorkMatcher
argument_list|()
argument_list|)
decl_stmt|;
name|HashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
name|nodeOutput
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|taskWalker
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
name|nodeOutput
argument_list|)
expr_stmt|;
return|return
name|pctx
return|;
block|}
class|class
name|EquivalentWorkMatcher
implements|implements
name|Dispatcher
block|{
specifier|private
name|Comparator
argument_list|<
name|BaseWork
argument_list|>
name|baseWorkComparator
init|=
operator|new
name|Comparator
argument_list|<
name|BaseWork
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|BaseWork
name|o1
parameter_list|,
name|BaseWork
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|getName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
block|}
decl_stmt|;
comment|// maps from a work to the DPPs it contains
specifier|private
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|List
argument_list|<
name|SparkPartitionPruningSinkOperator
argument_list|>
argument_list|>
name|workToDpps
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Object
name|dispatch
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|nd
operator|instanceof
name|SparkTask
condition|)
block|{
name|SparkTask
name|sparkTask
init|=
operator|(
name|SparkTask
operator|)
name|nd
decl_stmt|;
name|SparkWork
name|sparkWork
init|=
name|sparkTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
comment|// For dpp case, dpp sink will appear in Task1 and the target work of dpp sink will appear in Task2.
comment|// Task2 is the child task of Task1. Task2 will be traversed before task1 because TaskGraphWalker will first
comment|// put children task in the front of task queue.
comment|// If a spark work which is equal to other is found and removed in Task2, the dpp sink can be removed when Task1
comment|// is traversed(More detailed see HIVE-16948)
if|if
condition|(
name|removedMapWorkNames
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|removeDynamicPartitionPruningSink
argument_list|(
name|removedMapWorkNames
argument_list|,
name|sparkWork
argument_list|)
expr_stmt|;
if|if
condition|(
name|sparkWork
operator|.
name|getAllWork
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|removeEmptySparkTask
argument_list|(
name|sparkTask
argument_list|)
expr_stmt|;
block|}
block|}
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|roots
init|=
name|sparkWork
operator|.
name|getRoots
argument_list|()
decl_stmt|;
name|compareWorksRecursively
argument_list|(
name|roots
argument_list|,
name|sparkWork
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|compareWorksRecursively
parameter_list|(
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|works
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
block|{
name|workToDpps
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// find out all equivalent works in the Set.
name|Set
argument_list|<
name|Set
argument_list|<
name|BaseWork
argument_list|>
argument_list|>
name|equivalentWorks
init|=
name|compareChildWorks
argument_list|(
name|works
argument_list|,
name|sparkWork
argument_list|)
decl_stmt|;
comment|// combine equivalent work into single one in SparkWork's work graph.
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|removedWorks
init|=
name|combineEquivalentWorks
argument_list|(
name|equivalentWorks
argument_list|,
name|sparkWork
argument_list|)
decl_stmt|;
comment|// try to combine next level works recursively.
for|for
control|(
name|BaseWork
name|work
range|:
name|works
control|)
block|{
if|if
condition|(
operator|!
name|removedWorks
operator|.
name|contains
argument_list|(
name|work
argument_list|)
condition|)
block|{
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|children
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|children
operator|.
name|addAll
argument_list|(
name|sparkWork
operator|.
name|getChildren
argument_list|(
name|work
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|children
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|compareWorksRecursively
argument_list|(
name|children
argument_list|,
name|sparkWork
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|Set
argument_list|<
name|Set
argument_list|<
name|BaseWork
argument_list|>
argument_list|>
name|compareChildWorks
parameter_list|(
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|children
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
block|{
name|Set
argument_list|<
name|Set
argument_list|<
name|BaseWork
argument_list|>
argument_list|>
name|equivalentChildren
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
if|if
condition|(
name|children
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
for|for
control|(
name|BaseWork
name|work
range|:
name|children
control|)
block|{
name|boolean
name|assigned
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|set
range|:
name|equivalentChildren
control|)
block|{
if|if
condition|(
name|belongToSet
argument_list|(
name|set
argument_list|,
name|work
argument_list|,
name|sparkWork
argument_list|)
condition|)
block|{
name|set
operator|.
name|add
argument_list|(
name|work
argument_list|)
expr_stmt|;
name|assigned
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|assigned
condition|)
block|{
comment|// sort the works so that we get consistent query plan for multi executions(for test verification).
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|newSet
init|=
name|Sets
operator|.
name|newTreeSet
argument_list|(
name|baseWorkComparator
argument_list|)
decl_stmt|;
name|newSet
operator|.
name|add
argument_list|(
name|work
argument_list|)
expr_stmt|;
name|equivalentChildren
operator|.
name|add
argument_list|(
name|newSet
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|equivalentChildren
return|;
block|}
specifier|private
name|boolean
name|belongToSet
parameter_list|(
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|set
parameter_list|,
name|BaseWork
name|work
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
block|{
if|if
condition|(
name|set
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|compareWork
argument_list|(
name|set
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
argument_list|,
name|work
argument_list|,
name|sparkWork
argument_list|)
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
specifier|private
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|combineEquivalentWorks
parameter_list|(
name|Set
argument_list|<
name|Set
argument_list|<
name|BaseWork
argument_list|>
argument_list|>
name|equivalentWorks
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
block|{
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|removedWorks
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|workSet
range|:
name|equivalentWorks
control|)
block|{
if|if
condition|(
name|workSet
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
name|Iterator
argument_list|<
name|BaseWork
argument_list|>
name|iterator
init|=
name|workSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|BaseWork
name|first
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|SparkPartitionPruningSinkOperator
argument_list|>
name|dppList1
init|=
name|workToDpps
operator|.
name|get
argument_list|(
name|first
argument_list|)
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|BaseWork
name|next
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|dppList1
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|SparkPartitionPruningSinkOperator
argument_list|>
name|dppList2
init|=
name|workToDpps
operator|.
name|get
argument_list|(
name|next
argument_list|)
decl_stmt|;
comment|// equivalent works must have dpp lists of same size
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|dppList1
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|combineEquivalentDPPSinks
argument_list|(
name|dppList1
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|dppList2
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|replaceWork
argument_list|(
name|next
argument_list|,
name|first
argument_list|,
name|sparkWork
argument_list|)
expr_stmt|;
name|removedWorks
operator|.
name|add
argument_list|(
name|next
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|removedWorks
return|;
block|}
specifier|private
name|void
name|replaceWork
parameter_list|(
name|BaseWork
name|previous
parameter_list|,
name|BaseWork
name|current
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
block|{
name|updateReference
argument_list|(
name|previous
argument_list|,
name|current
argument_list|,
name|sparkWork
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|BaseWork
argument_list|>
name|parents
init|=
name|sparkWork
operator|.
name|getParents
argument_list|(
name|previous
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|BaseWork
argument_list|>
name|children
init|=
name|sparkWork
operator|.
name|getChildren
argument_list|(
name|previous
argument_list|)
decl_stmt|;
if|if
condition|(
name|parents
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|BaseWork
name|parent
range|:
name|parents
control|)
block|{
comment|// we do not need to connect its parent to its counterpart, as they have the same parents.
name|sparkWork
operator|.
name|disconnect
argument_list|(
name|parent
argument_list|,
name|previous
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|children
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|BaseWork
name|child
range|:
name|children
control|)
block|{
name|SparkEdgeProperty
name|edgeProperty
init|=
name|sparkWork
operator|.
name|getEdgeProperty
argument_list|(
name|previous
argument_list|,
name|child
argument_list|)
decl_stmt|;
name|sparkWork
operator|.
name|disconnect
argument_list|(
name|previous
argument_list|,
name|child
argument_list|)
expr_stmt|;
name|sparkWork
operator|.
name|connect
argument_list|(
name|current
argument_list|,
name|child
argument_list|,
name|edgeProperty
argument_list|)
expr_stmt|;
block|}
block|}
name|sparkWork
operator|.
name|remove
argument_list|(
name|previous
argument_list|)
expr_stmt|;
comment|// In order to fix HIVE-16948
if|if
condition|(
name|previous
operator|instanceof
name|MapWork
condition|)
block|{
name|removedMapWorkNames
operator|.
name|add
argument_list|(
name|previous
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*     * update the Work name which referred by Operators in following Works.     */
specifier|private
name|void
name|updateReference
parameter_list|(
name|BaseWork
name|previous
parameter_list|,
name|BaseWork
name|current
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
block|{
name|String
name|previousName
init|=
name|previous
operator|.
name|getName
argument_list|()
decl_stmt|;
name|String
name|currentName
init|=
name|current
operator|.
name|getName
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|BaseWork
argument_list|>
name|children
init|=
name|sparkWork
operator|.
name|getAllWork
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|child
range|:
name|children
control|)
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|allOperators
init|=
name|child
operator|.
name|getAllOperators
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|operator
range|:
name|allOperators
control|)
block|{
if|if
condition|(
name|operator
operator|instanceof
name|MapJoinOperator
condition|)
block|{
name|MapJoinDesc
name|mapJoinDesc
init|=
operator|(
operator|(
name|MapJoinOperator
operator|)
name|operator
operator|)
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|parentToInput
init|=
name|mapJoinDesc
operator|.
name|getParentToInput
argument_list|()
decl_stmt|;
for|for
control|(
name|Integer
name|id
range|:
name|parentToInput
operator|.
name|keySet
argument_list|()
control|)
block|{
name|String
name|parent
init|=
name|parentToInput
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|parent
operator|.
name|equals
argument_list|(
name|previousName
argument_list|)
condition|)
block|{
name|parentToInput
operator|.
name|put
argument_list|(
name|id
argument_list|,
name|currentName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
specifier|private
name|boolean
name|compareWork
parameter_list|(
name|BaseWork
name|first
parameter_list|,
name|BaseWork
name|second
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
block|{
if|if
condition|(
operator|!
name|first
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|second
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|hasSameParent
argument_list|(
name|first
argument_list|,
name|second
argument_list|,
name|sparkWork
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// leave work's output may be read in further SparkWork/FetchWork, we should not combine
comment|// leave works without notifying further SparkWork/FetchWork.
if|if
condition|(
name|sparkWork
operator|.
name|getLeaves
argument_list|()
operator|.
name|contains
argument_list|(
name|first
argument_list|)
operator|&&
name|sparkWork
operator|.
name|getLeaves
argument_list|()
operator|.
name|contains
argument_list|(
name|second
argument_list|)
condition|)
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|leafOps
init|=
name|first
operator|.
name|getAllLeafOperators
argument_list|()
decl_stmt|;
name|leafOps
operator|.
name|addAll
argument_list|(
name|second
operator|.
name|getAllLeafOperators
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Operator
name|operator
range|:
name|leafOps
control|)
block|{
comment|// we know how to handle DPP sinks
if|if
condition|(
operator|!
operator|(
name|operator
operator|instanceof
name|SparkPartitionPruningSinkOperator
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
comment|// need to check paths and partition desc for MapWorks
if|if
condition|(
name|first
operator|instanceof
name|MapWork
operator|&&
operator|!
name|compareMapWork
argument_list|(
operator|(
name|MapWork
operator|)
name|first
argument_list|,
operator|(
name|MapWork
operator|)
name|second
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|firstRootOperators
init|=
name|first
operator|.
name|getAllRootOperators
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|secondRootOperators
init|=
name|second
operator|.
name|getAllRootOperators
argument_list|()
decl_stmt|;
if|if
condition|(
name|firstRootOperators
operator|.
name|size
argument_list|()
operator|!=
name|secondRootOperators
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|Iterator
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|firstIterator
init|=
name|firstRootOperators
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|secondIterator
init|=
name|secondRootOperators
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|firstIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|boolean
name|result
init|=
name|compareOperatorChain
argument_list|(
name|firstIterator
operator|.
name|next
argument_list|()
argument_list|,
name|secondIterator
operator|.
name|next
argument_list|()
argument_list|,
name|first
argument_list|,
name|second
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|result
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
specifier|private
name|boolean
name|compareMapWork
parameter_list|(
name|MapWork
name|first
parameter_list|,
name|MapWork
name|second
parameter_list|)
block|{
name|Map
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartition1
init|=
name|first
operator|.
name|getPathToPartitionInfo
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartition2
init|=
name|second
operator|.
name|getPathToPartitionInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|pathToPartition1
operator|.
name|size
argument_list|()
operator|==
name|pathToPartition2
operator|.
name|size
argument_list|()
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
name|entry
range|:
name|pathToPartition1
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Path
name|path1
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|PartitionDesc
name|partitionDesc1
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|PartitionDesc
name|partitionDesc2
init|=
name|pathToPartition2
operator|.
name|get
argument_list|(
name|path1
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|partitionDesc1
operator|.
name|equals
argument_list|(
name|partitionDesc2
argument_list|)
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
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|hasSameParent
parameter_list|(
name|BaseWork
name|first
parameter_list|,
name|BaseWork
name|second
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
block|{
name|boolean
name|result
init|=
literal|true
decl_stmt|;
name|List
argument_list|<
name|BaseWork
argument_list|>
name|firstParents
init|=
name|sparkWork
operator|.
name|getParents
argument_list|(
name|first
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|BaseWork
argument_list|>
name|secondParents
init|=
name|sparkWork
operator|.
name|getParents
argument_list|(
name|second
argument_list|)
decl_stmt|;
if|if
condition|(
name|firstParents
operator|.
name|size
argument_list|()
operator|!=
name|secondParents
operator|.
name|size
argument_list|()
condition|)
block|{
name|result
operator|=
literal|false
expr_stmt|;
block|}
for|for
control|(
name|BaseWork
name|parent
range|:
name|firstParents
control|)
block|{
if|if
condition|(
operator|!
name|secondParents
operator|.
name|contains
argument_list|(
name|parent
argument_list|)
condition|)
block|{
name|result
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|private
name|boolean
name|compareOperatorChain
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|firstOperator
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|secondOperator
parameter_list|,
name|BaseWork
name|first
parameter_list|,
name|BaseWork
name|second
parameter_list|)
block|{
name|boolean
name|result
init|=
name|compareCurrentOperator
argument_list|(
name|firstOperator
argument_list|,
name|secondOperator
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|result
condition|)
block|{
return|return
literal|false
return|;
block|}
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|firstOperatorChildOperators
init|=
name|firstOperator
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|secondOperatorChildOperators
init|=
name|secondOperator
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
if|if
condition|(
name|firstOperatorChildOperators
operator|==
literal|null
operator|&&
name|secondOperatorChildOperators
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
name|firstOperatorChildOperators
operator|!=
literal|null
operator|&&
name|secondOperatorChildOperators
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
name|firstOperatorChildOperators
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|firstOperatorChildOperators
operator|.
name|size
argument_list|()
operator|!=
name|secondOperatorChildOperators
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|int
name|size
init|=
name|firstOperatorChildOperators
operator|.
name|size
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|=
name|compareOperatorChain
argument_list|(
name|firstOperatorChildOperators
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|secondOperatorChildOperators
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|first
argument_list|,
name|second
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|result
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
if|if
condition|(
name|firstOperator
operator|instanceof
name|SparkPartitionPruningSinkOperator
condition|)
block|{
name|List
argument_list|<
name|SparkPartitionPruningSinkOperator
argument_list|>
name|dpps
init|=
name|workToDpps
operator|.
name|computeIfAbsent
argument_list|(
name|first
argument_list|,
name|k
lambda|->
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|dpps
operator|.
name|add
argument_list|(
operator|(
operator|(
name|SparkPartitionPruningSinkOperator
operator|)
name|firstOperator
operator|)
argument_list|)
expr_stmt|;
name|dpps
operator|=
name|workToDpps
operator|.
name|computeIfAbsent
argument_list|(
name|second
argument_list|,
name|k
lambda|->
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
name|dpps
operator|.
name|add
argument_list|(
operator|(
operator|(
name|SparkPartitionPruningSinkOperator
operator|)
name|secondOperator
operator|)
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
comment|/**      * Compare Operators through their Explain output string.      *      * @param firstOperator      * @param secondOperator      * @return      */
specifier|private
name|boolean
name|compareCurrentOperator
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|firstOperator
parameter_list|,
name|Operator
argument_list|<
name|?
argument_list|>
name|secondOperator
parameter_list|)
block|{
return|return
name|firstOperator
operator|.
name|logicalEquals
argument_list|(
name|secondOperator
argument_list|)
return|;
block|}
comment|/**      * traverse the children in sparkWork to find the dpp sink operator which target work is included in      * removedMapWorkList      * If there is branch, remove prune sink operator branch in the BaseWork      * If there is no branch, remove the whole BaseWork      *      * @param removedMapWorkList: the name of the map work has been deleted because they are equals to other works.      * @param sparkWork:          current spark work      */
specifier|private
name|void
name|removeDynamicPartitionPruningSink
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|removedMapWorkList
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|allWorks
init|=
name|sparkWork
operator|.
name|getAllWork
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|baseWork
range|:
name|allWorks
control|)
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|rootOperators
init|=
name|baseWork
operator|.
name|getAllRootOperators
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
name|root
range|:
name|rootOperators
control|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|pruningList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|SparkUtilities
operator|.
name|collectOp
argument_list|(
name|pruningList
argument_list|,
name|root
argument_list|,
name|SparkPartitionPruningSinkOperator
operator|.
name|class
argument_list|)
expr_stmt|;
for|for
control|(
name|Operator
name|pruneSinkOp
range|:
name|pruningList
control|)
block|{
name|SparkPartitionPruningSinkOperator
name|sparkPruneSinkOp
init|=
operator|(
name|SparkPartitionPruningSinkOperator
operator|)
name|pruneSinkOp
decl_stmt|;
for|for
control|(
name|String
name|removedName
range|:
name|removedMapWorkList
control|)
block|{
name|sparkPruneSinkOp
operator|.
name|getConf
argument_list|()
operator|.
name|removeTarget
argument_list|(
name|removedName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sparkPruneSinkOp
operator|.
name|getConf
argument_list|()
operator|.
name|getTargetInfos
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"ready to remove the sparkPruneSinkOp which target work is "
operator|+
name|sparkPruneSinkOp
operator|.
name|getConf
argument_list|()
operator|.
name|getTargetWorks
argument_list|()
operator|+
literal|" because the MapWork is equals to other map work and "
operator|+
literal|"has been deleted!"
argument_list|)
expr_stmt|;
comment|// If there is branch, remove prune sink operator branch in the baseWork
comment|// If there is no branch, remove the whole baseWork
if|if
condition|(
name|OperatorUtils
operator|.
name|isInBranch
argument_list|(
name|sparkPruneSinkOp
argument_list|)
condition|)
block|{
name|OperatorUtils
operator|.
name|removeBranch
argument_list|(
name|sparkPruneSinkOp
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sparkWork
operator|.
name|remove
argument_list|(
name|baseWork
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
specifier|private
name|void
name|removeEmptySparkTask
parameter_list|(
name|SparkTask
name|currTask
parameter_list|)
block|{
comment|// If currTask is rootTasks, remove it and add its children to the rootTasks which currTask is its only parent
comment|// task
if|if
condition|(
name|pctx
operator|.
name|getRootTasks
argument_list|()
operator|.
name|contains
argument_list|(
name|currTask
argument_list|)
condition|)
block|{
name|pctx
operator|.
name|removeFromRootTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|newRoots
init|=
name|currTask
operator|.
name|getChildTasks
argument_list|()
decl_stmt|;
for|for
control|(
name|Task
name|newRoot
range|:
name|newRoots
control|)
block|{
if|if
condition|(
name|newRoot
operator|.
name|getParentTasks
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|pctx
operator|.
name|addToRootTask
argument_list|(
name|newRoot
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|SparkUtilities
operator|.
name|removeEmptySparkTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Merge the target works of the second DPP sink into the first DPP sink.
specifier|public
specifier|static
name|void
name|combineEquivalentDPPSinks
parameter_list|(
name|SparkPartitionPruningSinkOperator
name|first
parameter_list|,
name|SparkPartitionPruningSinkOperator
name|second
parameter_list|)
block|{
name|SparkPartitionPruningSinkDesc
name|firstConf
init|=
name|first
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|SparkPartitionPruningSinkDesc
name|secondConf
init|=
name|second
operator|.
name|getConf
argument_list|()
decl_stmt|;
for|for
control|(
name|SparkPartitionPruningSinkDesc
operator|.
name|DPPTargetInfo
name|targetInfo
range|:
name|secondConf
operator|.
name|getTargetInfos
argument_list|()
control|)
block|{
name|MapWork
name|target
init|=
name|targetInfo
operator|.
name|work
decl_stmt|;
name|firstConf
operator|.
name|addTarget
argument_list|(
name|targetInfo
operator|.
name|columnName
argument_list|,
name|targetInfo
operator|.
name|columnType
argument_list|,
name|targetInfo
operator|.
name|partKey
argument_list|,
name|target
argument_list|,
name|targetInfo
operator|.
name|tableScan
argument_list|)
expr_stmt|;
if|if
condition|(
name|target
operator|!=
literal|null
condition|)
block|{
comment|// update the target map work of the second
name|first
operator|.
name|addAsSourceEvent
argument_list|(
name|target
argument_list|,
name|targetInfo
operator|.
name|partKey
argument_list|,
name|targetInfo
operator|.
name|columnName
argument_list|,
name|targetInfo
operator|.
name|columnType
argument_list|)
expr_stmt|;
name|second
operator|.
name|removeFromSourceEvent
argument_list|(
name|target
argument_list|,
name|targetInfo
operator|.
name|partKey
argument_list|,
name|targetInfo
operator|.
name|columnName
argument_list|,
name|targetInfo
operator|.
name|columnType
argument_list|)
expr_stmt|;
name|target
operator|.
name|setTmpPathForPartitionPruning
argument_list|(
name|firstConf
operator|.
name|getTmpPathOfTargetWork
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

