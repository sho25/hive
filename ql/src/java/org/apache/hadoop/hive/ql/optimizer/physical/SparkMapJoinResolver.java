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
name|optimizer
operator|.
name|physical
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
name|Collection
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
name|java
operator|.
name|util
operator|.
name|Stack
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
name|conf
operator|.
name|HiveConf
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
name|Context
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
name|ConditionalTask
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
name|SparkHashTableSinkOperator
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
name|TaskFactory
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
name|Utilities
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
name|ConditionalResolver
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
name|ConditionalResolverSkewJoin
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
name|ConditionalWork
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
name|BucketMapJoinContext
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
name|MapredLocalWork
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
name|SparkBucketMapJoinContext
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
name|SparkHashTableSinkDesc
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

begin_class
specifier|public
class|class
name|SparkMapJoinResolver
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
name|Dispatcher
name|dispatcher
init|=
operator|new
name|SparkMapJoinTaskDispatcher
argument_list|(
name|pctx
argument_list|)
decl_stmt|;
name|TaskGraphWalker
name|graphWalker
init|=
operator|new
name|TaskGraphWalker
argument_list|(
name|dispatcher
argument_list|)
decl_stmt|;
name|ArrayList
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
name|graphWalker
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|pctx
return|;
block|}
comment|// Check whether the specified BaseWork's operator tree contains a operator
comment|// of the specified operator class
specifier|private
name|boolean
name|containsOp
parameter_list|(
name|BaseWork
name|work
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|matchingOps
init|=
name|getOp
argument_list|(
name|work
argument_list|,
name|clazz
argument_list|)
decl_stmt|;
return|return
name|matchingOps
operator|!=
literal|null
operator|&&
operator|!
name|matchingOps
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|getOp
parameter_list|(
name|BaseWork
name|work
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|ops
init|=
operator|new
name|HashSet
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|work
operator|instanceof
name|MapWork
condition|)
block|{
name|Collection
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|opSet
init|=
operator|(
operator|(
name|MapWork
operator|)
name|work
operator|)
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|values
argument_list|()
decl_stmt|;
name|Stack
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|opStack
init|=
operator|new
name|Stack
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|opStack
operator|.
name|addAll
argument_list|(
name|opSet
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|opStack
operator|.
name|empty
argument_list|()
condition|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|op
init|=
name|opStack
operator|.
name|pop
argument_list|()
decl_stmt|;
name|ops
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
if|if
condition|(
name|op
operator|.
name|getChildOperators
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|opStack
operator|.
name|addAll
argument_list|(
name|op
operator|.
name|getChildOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|ops
operator|.
name|addAll
argument_list|(
name|work
operator|.
name|getAllOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|matchingOps
init|=
operator|new
name|HashSet
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|ops
control|)
block|{
if|if
condition|(
name|clazz
operator|.
name|isInstance
argument_list|(
name|op
argument_list|)
condition|)
block|{
name|matchingOps
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|matchingOps
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
class|class
name|SparkMapJoinTaskDispatcher
implements|implements
name|Dispatcher
block|{
specifier|private
specifier|final
name|PhysicalContext
name|physicalContext
decl_stmt|;
comment|// For each BaseWork with MJ operator, we build a SparkWork for its small table BaseWorks
comment|// This map records such information
specifier|private
specifier|final
name|Map
argument_list|<
name|BaseWork
argument_list|,
name|SparkWork
argument_list|>
name|sparkWorkMap
decl_stmt|;
comment|// SparkWork dependency graph - from a SparkWork with MJ operators to all
comment|// of its parent SparkWorks for the small tables
specifier|private
specifier|final
name|Map
argument_list|<
name|SparkWork
argument_list|,
name|List
argument_list|<
name|SparkWork
argument_list|>
argument_list|>
name|dependencyGraph
decl_stmt|;
specifier|public
name|SparkMapJoinTaskDispatcher
parameter_list|(
name|PhysicalContext
name|pc
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|physicalContext
operator|=
name|pc
expr_stmt|;
name|sparkWorkMap
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|BaseWork
argument_list|,
name|SparkWork
argument_list|>
argument_list|()
expr_stmt|;
name|dependencyGraph
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|SparkWork
argument_list|,
name|List
argument_list|<
name|SparkWork
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
block|}
comment|// Move the specified work from the sparkWork to the targetWork
comment|// Note that, in order not to break the graph (since we need it for the edges),
comment|// we don't remove the work from the sparkWork here. The removal is done later.
specifier|private
name|void
name|moveWork
parameter_list|(
name|SparkWork
name|sparkWork
parameter_list|,
name|BaseWork
name|work
parameter_list|,
name|SparkWork
name|targetWork
parameter_list|)
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|parentWorks
init|=
name|sparkWork
operator|.
name|getParents
argument_list|(
name|work
argument_list|)
decl_stmt|;
if|if
condition|(
name|sparkWork
operator|!=
name|targetWork
condition|)
block|{
name|targetWork
operator|.
name|add
argument_list|(
name|work
argument_list|)
expr_stmt|;
comment|// If any child work for this work is already added to the targetWork earlier,
comment|// we should connect this work with it
for|for
control|(
name|BaseWork
name|childWork
range|:
name|sparkWork
operator|.
name|getChildren
argument_list|(
name|work
argument_list|)
control|)
block|{
if|if
condition|(
name|targetWork
operator|.
name|contains
argument_list|(
name|childWork
argument_list|)
condition|)
block|{
name|targetWork
operator|.
name|connect
argument_list|(
name|work
argument_list|,
name|childWork
argument_list|,
name|sparkWork
operator|.
name|getEdgeProperty
argument_list|(
name|work
argument_list|,
name|childWork
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|containsOp
argument_list|(
name|work
argument_list|,
name|MapJoinOperator
operator|.
name|class
argument_list|)
condition|)
block|{
for|for
control|(
name|BaseWork
name|parent
range|:
name|parentWorks
control|)
block|{
name|moveWork
argument_list|(
name|sparkWork
argument_list|,
name|parent
argument_list|,
name|targetWork
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Create a new SparkWork for all the small tables of this work
name|SparkWork
name|parentWork
init|=
operator|new
name|SparkWork
argument_list|(
name|physicalContext
operator|.
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|)
argument_list|)
decl_stmt|;
comment|// copy cloneToWork to ensure RDD cache still works
name|parentWork
operator|.
name|setCloneToWork
argument_list|(
name|sparkWork
operator|.
name|getCloneToWork
argument_list|()
argument_list|)
expr_stmt|;
name|dependencyGraph
operator|.
name|get
argument_list|(
name|targetWork
argument_list|)
operator|.
name|add
argument_list|(
name|parentWork
argument_list|)
expr_stmt|;
name|dependencyGraph
operator|.
name|put
argument_list|(
name|parentWork
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|SparkWork
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
comment|// this work is now moved to the parentWork, thus we should
comment|// update this information in sparkWorkMap
name|sparkWorkMap
operator|.
name|put
argument_list|(
name|work
argument_list|,
name|parentWork
argument_list|)
expr_stmt|;
for|for
control|(
name|BaseWork
name|parent
range|:
name|parentWorks
control|)
block|{
if|if
condition|(
name|containsOp
argument_list|(
name|parent
argument_list|,
name|SparkHashTableSinkOperator
operator|.
name|class
argument_list|)
condition|)
block|{
name|moveWork
argument_list|(
name|sparkWork
argument_list|,
name|parent
argument_list|,
name|parentWork
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|moveWork
argument_list|(
name|sparkWork
argument_list|,
name|parent
argument_list|,
name|targetWork
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|generateLocalWork
parameter_list|(
name|SparkTask
name|originalTask
parameter_list|)
block|{
name|SparkWork
name|originalWork
init|=
name|originalTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|BaseWork
argument_list|>
name|allBaseWorks
init|=
name|originalWork
operator|.
name|getAllWorkUnsorted
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|work
range|:
name|allBaseWorks
control|)
block|{
if|if
condition|(
name|containsOp
argument_list|(
name|work
argument_list|,
name|SparkHashTableSinkOperator
operator|.
name|class
argument_list|)
operator|||
name|containsOp
argument_list|(
name|work
argument_list|,
name|MapJoinOperator
operator|.
name|class
argument_list|)
condition|)
block|{
name|work
operator|.
name|setMapRedLocalWork
argument_list|(
operator|new
name|MapredLocalWork
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|Context
name|ctx
init|=
name|physicalContext
operator|.
name|getContext
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|work
range|:
name|allBaseWorks
control|)
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|ops
init|=
name|getOp
argument_list|(
name|work
argument_list|,
name|MapJoinOperator
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|ops
operator|==
literal|null
operator|||
name|ops
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|Path
name|tmpPath
init|=
name|Utilities
operator|.
name|generateTmpPath
argument_list|(
name|ctx
operator|.
name|getMRTmpPath
argument_list|()
argument_list|,
name|originalTask
operator|.
name|getId
argument_list|()
argument_list|)
decl_stmt|;
name|MapredLocalWork
name|bigTableLocalWork
init|=
name|work
operator|.
name|getMapRedLocalWork
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
name|dummyOps
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|(
name|work
operator|.
name|getDummyOps
argument_list|()
argument_list|)
decl_stmt|;
name|bigTableLocalWork
operator|.
name|setDummyParentOp
argument_list|(
name|dummyOps
argument_list|)
expr_stmt|;
name|bigTableLocalWork
operator|.
name|setTmpPath
argument_list|(
name|tmpPath
argument_list|)
expr_stmt|;
comment|// In one work, only one map join operator can be bucketed
name|SparkBucketMapJoinContext
name|bucketMJCxt
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|ops
control|)
block|{
name|MapJoinOperator
name|mapJoinOp
init|=
operator|(
name|MapJoinOperator
operator|)
name|op
decl_stmt|;
name|MapJoinDesc
name|mapJoinDesc
init|=
name|mapJoinOp
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|mapJoinDesc
operator|.
name|isBucketMapJoin
argument_list|()
condition|)
block|{
name|bucketMJCxt
operator|=
operator|new
name|SparkBucketMapJoinContext
argument_list|(
name|mapJoinDesc
argument_list|)
expr_stmt|;
name|bucketMJCxt
operator|.
name|setBucketMatcherClass
argument_list|(
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
name|DefaultBucketMatcher
operator|.
name|class
argument_list|)
expr_stmt|;
name|bucketMJCxt
operator|.
name|setPosToAliasMap
argument_list|(
name|mapJoinOp
operator|.
name|getPosToAliasMap
argument_list|()
argument_list|)
expr_stmt|;
operator|(
operator|(
name|MapWork
operator|)
name|work
operator|)
operator|.
name|setUseBucketizedHiveInputFormat
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|bigTableLocalWork
operator|.
name|setBucketMapjoinContext
argument_list|(
name|bucketMJCxt
argument_list|)
expr_stmt|;
name|bigTableLocalWork
operator|.
name|setInputFileChangeSensitive
argument_list|(
literal|true
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
for|for
control|(
name|BaseWork
name|parentWork
range|:
name|originalWork
operator|.
name|getParents
argument_list|(
name|work
argument_list|)
control|)
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|hashTableSinkOps
init|=
name|getOp
argument_list|(
name|parentWork
argument_list|,
name|SparkHashTableSinkOperator
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|hashTableSinkOps
operator|==
literal|null
operator|||
name|hashTableSinkOps
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|MapredLocalWork
name|parentLocalWork
init|=
name|parentWork
operator|.
name|getMapRedLocalWork
argument_list|()
decl_stmt|;
name|parentLocalWork
operator|.
name|setTmpHDFSPath
argument_list|(
name|tmpPath
argument_list|)
expr_stmt|;
if|if
condition|(
name|bucketMJCxt
operator|!=
literal|null
condition|)
block|{
comment|// We only need to update the work with the hashtable
comment|// sink operator with the same mapjoin desc. We can tell
comment|// that by comparing the bucket file name mapping map
comment|// instance. They should be exactly the same one due to
comment|// the way how the bucket mapjoin context is constructed.
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|hashTableSinkOps
control|)
block|{
name|SparkHashTableSinkOperator
name|hashTableSinkOp
init|=
operator|(
name|SparkHashTableSinkOperator
operator|)
name|op
decl_stmt|;
name|SparkHashTableSinkDesc
name|hashTableSinkDesc
init|=
name|hashTableSinkOp
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|BucketMapJoinContext
name|original
init|=
name|hashTableSinkDesc
operator|.
name|getBucketMapjoinContext
argument_list|()
decl_stmt|;
if|if
condition|(
name|original
operator|!=
literal|null
operator|&&
name|original
operator|.
name|getBucketFileNameMapping
argument_list|()
operator|==
name|bucketMJCxt
operator|.
name|getBucketFileNameMapping
argument_list|()
condition|)
block|{
operator|(
operator|(
name|MapWork
operator|)
name|parentWork
operator|)
operator|.
name|setUseBucketizedHiveInputFormat
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|parentLocalWork
operator|.
name|setBucketMapjoinContext
argument_list|(
name|bucketMJCxt
argument_list|)
expr_stmt|;
name|parentLocalWork
operator|.
name|setInputFileChangeSensitive
argument_list|(
literal|true
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
block|}
block|}
comment|// Create a new SparkTask for the specified SparkWork, recursively compute
comment|// all the parent SparkTasks that this new task is depend on, if they don't already exists.
specifier|private
name|SparkTask
name|createSparkTask
parameter_list|(
name|SparkTask
name|originalTask
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|,
name|Map
argument_list|<
name|SparkWork
argument_list|,
name|SparkTask
argument_list|>
name|createdTaskMap
parameter_list|,
name|ConditionalTask
name|conditionalTask
parameter_list|)
block|{
if|if
condition|(
name|createdTaskMap
operator|.
name|containsKey
argument_list|(
name|sparkWork
argument_list|)
condition|)
block|{
return|return
name|createdTaskMap
operator|.
name|get
argument_list|(
name|sparkWork
argument_list|)
return|;
block|}
name|SparkTask
name|resultTask
init|=
name|originalTask
operator|.
name|getWork
argument_list|()
operator|==
name|sparkWork
condition|?
name|originalTask
else|:
operator|(
name|SparkTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
name|sparkWork
argument_list|,
name|physicalContext
operator|.
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|dependencyGraph
operator|.
name|get
argument_list|(
name|sparkWork
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|SparkWork
name|parentWork
range|:
name|dependencyGraph
operator|.
name|get
argument_list|(
name|sparkWork
argument_list|)
control|)
block|{
name|SparkTask
name|parentTask
init|=
name|createSparkTask
argument_list|(
name|originalTask
argument_list|,
name|parentWork
argument_list|,
name|createdTaskMap
argument_list|,
name|conditionalTask
argument_list|)
decl_stmt|;
name|parentTask
operator|.
name|addDependentTask
argument_list|(
name|resultTask
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|originalTask
operator|!=
name|resultTask
condition|)
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|parentTasks
init|=
name|originalTask
operator|.
name|getParentTasks
argument_list|()
decl_stmt|;
if|if
condition|(
name|parentTasks
operator|!=
literal|null
operator|&&
name|parentTasks
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// avoid concurrent modification
name|originalTask
operator|.
name|setParentTasks
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|parentTask
range|:
name|parentTasks
control|)
block|{
name|parentTask
operator|.
name|addDependentTask
argument_list|(
name|resultTask
argument_list|)
expr_stmt|;
name|parentTask
operator|.
name|removeDependentTask
argument_list|(
name|originalTask
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|conditionalTask
operator|==
literal|null
condition|)
block|{
name|physicalContext
operator|.
name|addToRootTask
argument_list|(
name|resultTask
argument_list|)
expr_stmt|;
name|physicalContext
operator|.
name|removeFromRootTask
argument_list|(
name|originalTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|updateConditionalTask
argument_list|(
name|conditionalTask
argument_list|,
name|originalTask
argument_list|,
name|resultTask
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|createdTaskMap
operator|.
name|put
argument_list|(
name|sparkWork
argument_list|,
name|resultTask
argument_list|)
expr_stmt|;
return|return
name|resultTask
return|;
block|}
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
name|nos
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currentTask
init|=
operator|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|nd
decl_stmt|;
if|if
condition|(
name|currentTask
operator|.
name|isMapRedTask
argument_list|()
condition|)
block|{
if|if
condition|(
name|currentTask
operator|instanceof
name|ConditionalTask
condition|)
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|taskList
init|=
operator|(
operator|(
name|ConditionalTask
operator|)
name|currentTask
operator|)
operator|.
name|getListTasks
argument_list|()
decl_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tsk
range|:
name|taskList
control|)
block|{
if|if
condition|(
name|tsk
operator|instanceof
name|SparkTask
condition|)
block|{
name|processCurrentTask
argument_list|(
operator|(
name|SparkTask
operator|)
name|tsk
argument_list|,
operator|(
name|ConditionalTask
operator|)
name|currentTask
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|currentTask
operator|instanceof
name|SparkTask
condition|)
block|{
name|processCurrentTask
argument_list|(
operator|(
name|SparkTask
operator|)
name|currentTask
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**      * @param sparkTask The current spark task we're processing.      * @param conditionalTask If conditional task is not null, it means the current task is      *                        wrapped in its task list.      */
specifier|private
name|void
name|processCurrentTask
parameter_list|(
name|SparkTask
name|sparkTask
parameter_list|,
name|ConditionalTask
name|conditionalTask
parameter_list|)
block|{
name|dependencyGraph
operator|.
name|clear
argument_list|()
expr_stmt|;
name|sparkWorkMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|SparkWork
name|sparkWork
init|=
name|sparkTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
comment|// Generate MapredLocalWorks for MJ and HTS
name|generateLocalWork
argument_list|(
name|sparkTask
argument_list|)
expr_stmt|;
name|dependencyGraph
operator|.
name|put
argument_list|(
name|sparkWork
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|SparkWork
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|BaseWork
argument_list|>
name|leaves
init|=
name|sparkWork
operator|.
name|getLeaves
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|leaf
range|:
name|leaves
control|)
block|{
name|moveWork
argument_list|(
name|sparkWork
argument_list|,
name|leaf
argument_list|,
name|sparkWork
argument_list|)
expr_stmt|;
block|}
comment|// Now remove all BaseWorks in all the childSparkWorks that we created
comment|// from the original SparkWork
for|for
control|(
name|SparkWork
name|newSparkWork
range|:
name|sparkWorkMap
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|BaseWork
name|work
range|:
name|newSparkWork
operator|.
name|getAllWorkUnsorted
argument_list|()
control|)
block|{
name|sparkWork
operator|.
name|remove
argument_list|(
name|work
argument_list|)
expr_stmt|;
block|}
block|}
name|Map
argument_list|<
name|SparkWork
argument_list|,
name|SparkTask
argument_list|>
name|createdTaskMap
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|SparkWork
argument_list|,
name|SparkTask
argument_list|>
argument_list|()
decl_stmt|;
comment|// Now create SparkTasks from the SparkWorks, also set up dependency
for|for
control|(
name|SparkWork
name|work
range|:
name|dependencyGraph
operator|.
name|keySet
argument_list|()
control|)
block|{
name|createSparkTask
argument_list|(
name|sparkTask
argument_list|,
name|work
argument_list|,
name|createdTaskMap
argument_list|,
name|conditionalTask
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Update the task/work list of this conditional task to replace originalTask with newTask.      * For runtime skew join, also update dirToTaskMap for the conditional resolver      */
specifier|private
name|void
name|updateConditionalTask
parameter_list|(
name|ConditionalTask
name|conditionalTask
parameter_list|,
name|SparkTask
name|originalTask
parameter_list|,
name|SparkTask
name|newTask
parameter_list|)
block|{
name|ConditionalWork
name|conditionalWork
init|=
name|conditionalTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|SparkWork
name|originWork
init|=
name|originalTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|SparkWork
name|newWork
init|=
name|newTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|listTask
init|=
name|conditionalTask
operator|.
name|getListTasks
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Serializable
argument_list|>
name|listWork
init|=
operator|(
name|List
argument_list|<
name|Serializable
argument_list|>
operator|)
name|conditionalWork
operator|.
name|getListWorks
argument_list|()
decl_stmt|;
name|int
name|taskIndex
init|=
name|listTask
operator|.
name|indexOf
argument_list|(
name|originalTask
argument_list|)
decl_stmt|;
name|int
name|workIndex
init|=
name|listWork
operator|.
name|indexOf
argument_list|(
name|originWork
argument_list|)
decl_stmt|;
if|if
condition|(
name|taskIndex
operator|<
literal|0
operator|||
name|workIndex
operator|<
literal|0
condition|)
block|{
return|return;
block|}
name|listTask
operator|.
name|set
argument_list|(
name|taskIndex
argument_list|,
name|newTask
argument_list|)
expr_stmt|;
name|listWork
operator|.
name|set
argument_list|(
name|workIndex
argument_list|,
name|newWork
argument_list|)
expr_stmt|;
name|ConditionalResolver
name|resolver
init|=
name|conditionalTask
operator|.
name|getResolver
argument_list|()
decl_stmt|;
if|if
condition|(
name|resolver
operator|instanceof
name|ConditionalResolverSkewJoin
condition|)
block|{
comment|// get bigKeysDirToTaskMap
name|ConditionalResolverSkewJoin
operator|.
name|ConditionalResolverSkewJoinCtx
name|context
init|=
operator|(
name|ConditionalResolverSkewJoin
operator|.
name|ConditionalResolverSkewJoinCtx
operator|)
name|conditionalTask
operator|.
name|getResolverCtx
argument_list|()
decl_stmt|;
name|HashMap
argument_list|<
name|Path
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|bigKeysDirToTaskMap
init|=
name|context
operator|.
name|getDirToTaskMap
argument_list|()
decl_stmt|;
comment|// to avoid concurrent modify the hashmap
name|HashMap
argument_list|<
name|Path
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|newbigKeysDirToTaskMap
init|=
operator|new
name|HashMap
argument_list|<
name|Path
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// reset the resolver
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Path
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|entry
range|:
name|bigKeysDirToTaskMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Path
name|bigKeyDir
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|task
operator|.
name|equals
argument_list|(
name|originalTask
argument_list|)
condition|)
block|{
name|newbigKeysDirToTaskMap
operator|.
name|put
argument_list|(
name|bigKeyDir
argument_list|,
name|newTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newbigKeysDirToTaskMap
operator|.
name|put
argument_list|(
name|bigKeyDir
argument_list|,
name|task
argument_list|)
expr_stmt|;
block|}
block|}
name|context
operator|.
name|setDirToTaskMap
argument_list|(
name|newbigKeysDirToTaskMap
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

