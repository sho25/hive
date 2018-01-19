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
name|HashMap
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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|AbstractMapJoinOperator
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
name|SMBMapJoinOperator
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
name|TableScanOperator
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
name|NodeProcessor
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
name|NodeProcessorCtx
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
name|GenMRProcContext
operator|.
name|GenMapRedCtx
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
name|MapredWork
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

begin_comment
comment|/**  * Operator factory for MapJoin processing.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|MapJoinFactory
block|{
specifier|public
specifier|static
name|int
name|getPositionParent
parameter_list|(
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
name|op
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|)
block|{
name|int
name|pos
init|=
literal|0
decl_stmt|;
name|int
name|size
init|=
name|stack
operator|.
name|size
argument_list|()
decl_stmt|;
assert|assert
name|size
operator|>=
literal|2
operator|&&
name|stack
operator|.
name|get
argument_list|(
name|size
operator|-
literal|1
argument_list|)
operator|==
name|op
assert|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parent
init|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|)
name|stack
operator|.
name|get
argument_list|(
name|size
operator|-
literal|2
argument_list|)
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
name|parOp
init|=
name|op
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
name|pos
operator|=
name|parOp
operator|.
name|indexOf
argument_list|(
name|parent
argument_list|)
expr_stmt|;
assert|assert
name|pos
operator|<
name|parOp
operator|.
name|size
argument_list|()
assert|;
return|return
name|pos
return|;
block|}
comment|/**    * MapJoin processor.    * The user can specify a mapjoin hint to specify that the input should be processed as a    * mapjoin instead of map-reduce join. If hive.auto.convert.join is set to true, the    * user need not specify the hint explicitly, but hive will automatically process the joins    * as a mapjoin whenever possible. However, a join can only be processed as a bucketized    * map-side join or a sort merge join, if the user has provided the hint explicitly. This    * will be fixed as part of HIVE-3433, and eventually, we should remove support for mapjoin    * hint.    * However, currently, the mapjoin hint is processed as follows:    * A mapjoin will have 'n' parents for a n-way mapjoin, and therefore the mapjoin operator    * will be encountered 'n' times (one for each parent). Since a reduceSink operator is not    * allowed before a mapjoin, the task for the mapjoin will always be a root task. The task    * corresponding to the mapjoin is converted to a root task when the operator is encountered    * for the first time. When the operator is encountered subsequently, the current task is    * merged with the root task for the mapjoin. Note that, it is possible that the map-join task    * may be performed as a bucketized map-side join (or sort-merge join), the map join operator    * is enhanced to contain the bucketing info. when it is encountered.    */
specifier|private
specifier|static
class|class
name|TableScanMapJoinProcessor
implements|implements
name|NodeProcessor
block|{
specifier|public
specifier|static
name|void
name|setupBucketMapJoinInfo
parameter_list|(
name|MapWork
name|plan
parameter_list|,
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
name|currMapJoinOp
parameter_list|)
block|{
if|if
condition|(
name|currMapJoinOp
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
name|aliasBucketFileNameMapping
init|=
name|currMapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getAliasBucketFileNameMapping
argument_list|()
decl_stmt|;
if|if
condition|(
name|aliasBucketFileNameMapping
operator|!=
literal|null
condition|)
block|{
name|MapredLocalWork
name|localPlan
init|=
name|plan
operator|.
name|getMapRedLocalWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|localPlan
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|currMapJoinOp
operator|instanceof
name|SMBMapJoinOperator
condition|)
block|{
name|localPlan
operator|=
operator|(
operator|(
name|SMBMapJoinOperator
operator|)
name|currMapJoinOp
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|getLocalWork
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// local plan is not null, we want to merge it into SMBMapJoinOperator's local work
if|if
condition|(
name|currMapJoinOp
operator|instanceof
name|SMBMapJoinOperator
condition|)
block|{
name|MapredLocalWork
name|smbLocalWork
init|=
operator|(
operator|(
name|SMBMapJoinOperator
operator|)
name|currMapJoinOp
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|getLocalWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|smbLocalWork
operator|!=
literal|null
condition|)
block|{
name|localPlan
operator|.
name|getAliasToFetchWork
argument_list|()
operator|.
name|putAll
argument_list|(
name|smbLocalWork
operator|.
name|getAliasToFetchWork
argument_list|()
argument_list|)
expr_stmt|;
name|localPlan
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|putAll
argument_list|(
name|smbLocalWork
operator|.
name|getAliasToWork
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|localPlan
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|currMapJoinOp
operator|instanceof
name|SMBMapJoinOperator
condition|)
block|{
name|plan
operator|.
name|setMapRedLocalWork
argument_list|(
literal|null
argument_list|)
expr_stmt|;
operator|(
operator|(
name|SMBMapJoinOperator
operator|)
name|currMapJoinOp
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|setLocalWork
argument_list|(
name|localPlan
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|plan
operator|.
name|setMapRedLocalWork
argument_list|(
name|localPlan
argument_list|)
expr_stmt|;
block|}
name|BucketMapJoinContext
name|bucketMJCxt
init|=
operator|new
name|BucketMapJoinContext
argument_list|()
decl_stmt|;
name|localPlan
operator|.
name|setBucketMapjoinContext
argument_list|(
name|bucketMJCxt
argument_list|)
expr_stmt|;
name|bucketMJCxt
operator|.
name|setAliasBucketFileNameMapping
argument_list|(
name|aliasBucketFileNameMapping
argument_list|)
expr_stmt|;
name|bucketMJCxt
operator|.
name|setBucketFileNameMapping
argument_list|(
name|currMapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getBigTableBucketNumMapping
argument_list|()
argument_list|)
expr_stmt|;
name|localPlan
operator|.
name|setInputFileChangeSensitive
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|bucketMJCxt
operator|.
name|setMapJoinBigTableAlias
argument_list|(
name|currMapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getBigTableAlias
argument_list|()
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
name|setBigTablePartSpecToFileMapping
argument_list|(
name|currMapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getBigTablePartSpecToFileMapping
argument_list|()
argument_list|)
expr_stmt|;
comment|// BucketizedHiveInputFormat should be used for either sort merge join or bucket map join
if|if
condition|(
operator|(
name|currMapJoinOp
operator|instanceof
name|SMBMapJoinOperator
operator|)
operator|||
operator|(
name|currMapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|isBucketMapJoin
argument_list|()
operator|)
condition|)
block|{
name|plan
operator|.
name|setUseBucketizedHiveInputFormat
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**      * Initialize the current plan by adding it to root tasks. Since a reduce sink      * cannot be present before a mapjoin, and the mapjoin operator is encountered      * for the first time, the task corresposding to the mapjoin is added to the      * root tasks.      *      * @param op      *          the map join operator encountered      * @param opProcCtx      *          processing context      * @param pos      *          position of the parent      */
specifier|private
specifier|static
name|void
name|initMapJoinPlan
parameter_list|(
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
name|op
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
parameter_list|,
name|GenMRProcContext
name|opProcCtx
parameter_list|,
name|boolean
name|local
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// The map is overloaded to keep track of mapjoins also
name|opProcCtx
operator|.
name|getOpTaskMap
argument_list|()
operator|.
name|put
argument_list|(
name|op
argument_list|,
name|currTask
argument_list|)
expr_stmt|;
name|TableScanOperator
name|currTopOp
init|=
name|opProcCtx
operator|.
name|getCurrTopOp
argument_list|()
decl_stmt|;
name|String
name|currAliasId
init|=
name|opProcCtx
operator|.
name|getCurrAliasId
argument_list|()
decl_stmt|;
name|GenMapRedUtils
operator|.
name|setTaskPlan
argument_list|(
name|currAliasId
argument_list|,
name|currTopOp
argument_list|,
name|currTask
argument_list|,
name|local
argument_list|,
name|opProcCtx
argument_list|)
expr_stmt|;
block|}
comment|/**      * Merge the current task with the task for the current mapjoin. The mapjoin operator      * has already been encountered.      *      * @param op      *          operator being processed      * @param oldTask      *          the old task for the current mapjoin      * @param opProcCtx      *          processing context      * @param pos      *          position of the parent in the stack      */
specifier|private
specifier|static
name|void
name|joinMapJoinPlan
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|oldTask
parameter_list|,
name|GenMRProcContext
name|opProcCtx
parameter_list|,
name|boolean
name|local
parameter_list|)
throws|throws
name|SemanticException
block|{
name|TableScanOperator
name|currTopOp
init|=
name|opProcCtx
operator|.
name|getCurrTopOp
argument_list|()
decl_stmt|;
name|GenMapRedUtils
operator|.
name|mergeInput
argument_list|(
name|currTopOp
argument_list|,
name|opProcCtx
argument_list|,
name|oldTask
argument_list|,
name|local
argument_list|)
expr_stmt|;
block|}
comment|/*      * The mapjoin operator will be encountered many times (n times for a n-way join). Since a      * reduceSink operator is not allowed before a mapjoin, the task for the mapjoin will always      * be a root task. The task corresponding to the mapjoin is converted to a root task when the      * operator is encountered for the first time. When the operator is encountered subsequently,      * the current task is merged with the root task for the mapjoin. Note that, it is possible      * that the map-join task may be performed as a bucketized map-side join (or sort-merge join),      * the map join operator is enhanced to contain the bucketing info. when it is encountered.      */
annotation|@
name|Override
specifier|public
name|Object
name|process
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
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|AbstractMapJoinOperator
argument_list|<
name|MapJoinDesc
argument_list|>
name|mapJoin
init|=
operator|(
name|AbstractMapJoinOperator
argument_list|<
name|MapJoinDesc
argument_list|>
operator|)
name|nd
decl_stmt|;
name|GenMRProcContext
name|ctx
init|=
operator|(
name|GenMRProcContext
operator|)
name|procCtx
decl_stmt|;
comment|// find the branch on which this processor was invoked
name|int
name|pos
init|=
name|getPositionParent
argument_list|(
name|mapJoin
argument_list|,
name|stack
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|,
name|GenMapRedCtx
argument_list|>
name|mapCurrCtx
init|=
name|ctx
operator|.
name|getMapCurrCtx
argument_list|()
decl_stmt|;
name|GenMapRedCtx
name|mapredCtx
init|=
name|mapCurrCtx
operator|.
name|get
argument_list|(
name|mapJoin
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|pos
argument_list|)
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
name|mapredCtx
operator|.
name|getCurrTask
argument_list|()
decl_stmt|;
name|MapredWork
name|currPlan
init|=
operator|(
name|MapredWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|String
name|currAliasId
init|=
name|mapredCtx
operator|.
name|getCurrAliasId
argument_list|()
decl_stmt|;
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|opTaskMap
init|=
name|ctx
operator|.
name|getOpTaskMap
argument_list|()
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|oldTask
init|=
name|opTaskMap
operator|.
name|get
argument_list|(
name|mapJoin
argument_list|)
decl_stmt|;
name|ctx
operator|.
name|setCurrAliasId
argument_list|(
name|currAliasId
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCurrTask
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
comment|// If we are seeing this mapjoin for the first time, initialize the plan.
comment|// If we are seeing this mapjoin for the second or later time then atleast one of the
comment|// branches for this mapjoin have been encounered. Join the plan with the plan created
comment|// the first time.
name|boolean
name|local
init|=
name|pos
operator|!=
name|mapJoin
operator|.
name|getConf
argument_list|()
operator|.
name|getPosBigTable
argument_list|()
decl_stmt|;
if|if
condition|(
name|oldTask
operator|==
literal|null
condition|)
block|{
assert|assert
name|currPlan
operator|.
name|getReduceWork
argument_list|()
operator|==
literal|null
assert|;
name|initMapJoinPlan
argument_list|(
name|mapJoin
argument_list|,
name|currTask
argument_list|,
name|ctx
argument_list|,
name|local
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// The current plan can be thrown away after being merged with the
comment|// original plan
name|joinMapJoinPlan
argument_list|(
name|oldTask
argument_list|,
name|ctx
argument_list|,
name|local
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCurrTask
argument_list|(
name|currTask
operator|=
name|oldTask
argument_list|)
expr_stmt|;
block|}
name|MapredWork
name|plan
init|=
operator|(
name|MapredWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|setupBucketMapJoinInfo
argument_list|(
name|plan
operator|.
name|getMapWork
argument_list|()
argument_list|,
name|mapJoin
argument_list|)
expr_stmt|;
name|mapCurrCtx
operator|.
name|put
argument_list|(
name|mapJoin
argument_list|,
operator|new
name|GenMapRedCtx
argument_list|(
name|ctx
operator|.
name|getCurrTask
argument_list|()
argument_list|,
name|ctx
operator|.
name|getCurrAliasId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// local aliases need not to hand over context further
return|return
operator|!
name|local
return|;
block|}
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getTableScanMapJoin
parameter_list|()
block|{
return|return
operator|new
name|TableScanMapJoinProcessor
argument_list|()
return|;
block|}
specifier|private
name|MapJoinFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

