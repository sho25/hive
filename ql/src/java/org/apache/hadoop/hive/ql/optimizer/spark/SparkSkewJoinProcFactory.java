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
name|spark
package|;
end_package

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
name|JoinOperator
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
name|GenMapRedUtils
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
name|GenSparkSkewJoinProcessor
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
name|SkewJoinProcFactory
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
name|SparkMapJoinResolver
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
name|ParseContext
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
name|QBJoinTree
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
name|PlanUtils
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
name|TableDesc
import|;
end_import

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
name|List
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

begin_comment
comment|/**  * Spark-version of SkewJoinProcFactory  */
end_comment

begin_class
specifier|public
class|class
name|SparkSkewJoinProcFactory
block|{
specifier|private
name|SparkSkewJoinProcFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getDefaultProc
parameter_list|()
block|{
return|return
name|SkewJoinProcFactory
operator|.
name|getDefaultProc
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getJoinProc
parameter_list|()
block|{
return|return
operator|new
name|SparkSkewJoinJoinProcessor
argument_list|()
return|;
block|}
specifier|public
specifier|static
class|class
name|SparkSkewJoinJoinProcessor
implements|implements
name|NodeProcessor
block|{
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
name|SparkSkewJoinResolver
operator|.
name|SparkSkewJoinProcCtx
name|context
init|=
operator|(
name|SparkSkewJoinResolver
operator|.
name|SparkSkewJoinProcCtx
operator|)
name|procCtx
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currentTsk
init|=
name|context
operator|.
name|getCurrentTask
argument_list|()
decl_stmt|;
name|JoinOperator
name|op
init|=
operator|(
name|JoinOperator
operator|)
name|nd
decl_stmt|;
name|ReduceWork
name|reduceWork
init|=
name|context
operator|.
name|getReducerToReduceWork
argument_list|()
operator|.
name|get
argument_list|(
name|op
argument_list|)
decl_stmt|;
name|ParseContext
name|parseContext
init|=
name|context
operator|.
name|getParseCtx
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|isFixedAsSorted
argument_list|()
operator|&&
name|currentTsk
operator|instanceof
name|SparkTask
operator|&&
name|reduceWork
operator|!=
literal|null
operator|&&
operator|(
operator|(
name|SparkTask
operator|)
name|currentTsk
operator|)
operator|.
name|getWork
argument_list|()
operator|.
name|contains
argument_list|(
name|reduceWork
argument_list|)
operator|&&
name|GenSparkSkewJoinProcessor
operator|.
name|supportRuntimeSkewJoin
argument_list|(
name|op
argument_list|,
name|currentTsk
argument_list|,
name|parseContext
operator|.
name|getConf
argument_list|()
argument_list|)
condition|)
block|{
comment|// first we try to split the task
name|splitTask
argument_list|(
operator|(
name|SparkTask
operator|)
name|currentTsk
argument_list|,
name|reduceWork
argument_list|,
name|parseContext
argument_list|)
expr_stmt|;
name|GenSparkSkewJoinProcessor
operator|.
name|processSkewJoin
argument_list|(
name|op
argument_list|,
name|currentTsk
argument_list|,
name|reduceWork
argument_list|,
name|parseContext
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
comment|/**    * If the join is not in a leaf ReduceWork, the spark task has to be split into 2 tasks.    */
specifier|private
specifier|static
name|void
name|splitTask
parameter_list|(
name|SparkTask
name|currentTask
parameter_list|,
name|ReduceWork
name|reduceWork
parameter_list|,
name|ParseContext
name|parseContext
parameter_list|)
throws|throws
name|SemanticException
block|{
name|SparkWork
name|currentWork
init|=
name|currentTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|reduceSinkSet
init|=
name|SparkMapJoinResolver
operator|.
name|getOp
argument_list|(
name|reduceWork
argument_list|,
name|ReduceSinkOperator
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentWork
operator|.
name|getChildren
argument_list|(
name|reduceWork
argument_list|)
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|canSplit
argument_list|(
name|currentWork
argument_list|)
operator|&&
name|reduceSinkSet
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|ReduceSinkOperator
name|reduceSink
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|reduceSinkSet
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|BaseWork
name|childWork
init|=
name|currentWork
operator|.
name|getChildren
argument_list|(
name|reduceWork
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|SparkEdgeProperty
name|originEdge
init|=
name|currentWork
operator|.
name|getEdgeProperty
argument_list|(
name|reduceWork
argument_list|,
name|childWork
argument_list|)
decl_stmt|;
comment|// disconnect the reduce work from its child. this should produce two isolated sub graphs
name|currentWork
operator|.
name|disconnect
argument_list|(
name|reduceWork
argument_list|,
name|childWork
argument_list|)
expr_stmt|;
comment|// move works following the current reduce work into a new spark work
name|SparkWork
name|newWork
init|=
operator|new
name|SparkWork
argument_list|(
name|parseContext
operator|.
name|getConf
argument_list|()
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
name|newWork
operator|.
name|add
argument_list|(
name|childWork
argument_list|)
expr_stmt|;
name|copyWorkGraph
argument_list|(
name|currentWork
argument_list|,
name|newWork
argument_list|,
name|childWork
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|copyWorkGraph
argument_list|(
name|currentWork
argument_list|,
name|newWork
argument_list|,
name|childWork
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// remove them from current spark work
for|for
control|(
name|BaseWork
name|baseWork
range|:
name|newWork
operator|.
name|getAllWorkUnsorted
argument_list|()
control|)
block|{
name|currentWork
operator|.
name|remove
argument_list|(
name|baseWork
argument_list|)
expr_stmt|;
comment|// TODO: take care of cloneToWork?
name|currentWork
operator|.
name|getCloneToWork
argument_list|()
operator|.
name|remove
argument_list|(
name|baseWork
argument_list|)
expr_stmt|;
block|}
comment|// create TS to read intermediate data
name|Context
name|baseCtx
init|=
name|parseContext
operator|.
name|getContext
argument_list|()
decl_stmt|;
name|Path
name|taskTmpDir
init|=
name|baseCtx
operator|.
name|getMRTmpPath
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|rsParent
init|=
name|reduceSink
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|TableDesc
name|tableDesc
init|=
name|PlanUtils
operator|.
name|getIntermediateFileTableDesc
argument_list|(
name|PlanUtils
operator|.
name|getFieldSchemasFromRowSchema
argument_list|(
name|rsParent
operator|.
name|getSchema
argument_list|()
argument_list|,
literal|"temporarycol"
argument_list|)
argument_list|)
decl_stmt|;
comment|// this will insert FS and TS between the RS and its parent
name|TableScanOperator
name|tableScanOp
init|=
name|GenMapRedUtils
operator|.
name|createTemporaryFile
argument_list|(
name|rsParent
argument_list|,
name|reduceSink
argument_list|,
name|taskTmpDir
argument_list|,
name|tableDesc
argument_list|,
name|parseContext
argument_list|)
decl_stmt|;
comment|// create new MapWork
name|MapWork
name|mapWork
init|=
name|PlanUtils
operator|.
name|getMapRedWork
argument_list|()
operator|.
name|getMapWork
argument_list|()
decl_stmt|;
name|mapWork
operator|.
name|setName
argument_list|(
literal|"Map "
operator|+
name|GenSparkUtils
operator|.
name|getUtils
argument_list|()
operator|.
name|getNextSeqNumber
argument_list|()
argument_list|)
expr_stmt|;
name|newWork
operator|.
name|add
argument_list|(
name|mapWork
argument_list|)
expr_stmt|;
name|newWork
operator|.
name|connect
argument_list|(
name|mapWork
argument_list|,
name|childWork
argument_list|,
name|originEdge
argument_list|)
expr_stmt|;
comment|// setup the new map work
name|String
name|streamDesc
init|=
name|taskTmpDir
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|GenMapRedUtils
operator|.
name|needsTagging
argument_list|(
operator|(
name|ReduceWork
operator|)
name|childWork
argument_list|)
condition|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|childReducer
init|=
operator|(
operator|(
name|ReduceWork
operator|)
name|childWork
operator|)
operator|.
name|getReducer
argument_list|()
decl_stmt|;
name|QBJoinTree
name|joinTree
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|childReducer
operator|instanceof
name|JoinOperator
condition|)
block|{
name|joinTree
operator|=
name|parseContext
operator|.
name|getJoinContext
argument_list|()
operator|.
name|get
argument_list|(
name|childReducer
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|childReducer
operator|instanceof
name|MapJoinOperator
condition|)
block|{
name|joinTree
operator|=
name|parseContext
operator|.
name|getMapJoinContext
argument_list|()
operator|.
name|get
argument_list|(
name|childReducer
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|childReducer
operator|instanceof
name|SMBMapJoinOperator
condition|)
block|{
name|joinTree
operator|=
name|parseContext
operator|.
name|getSmbMapJoinContext
argument_list|()
operator|.
name|get
argument_list|(
name|childReducer
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|joinTree
operator|!=
literal|null
operator|&&
name|joinTree
operator|.
name|getId
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|streamDesc
operator|=
name|joinTree
operator|.
name|getId
argument_list|()
operator|+
literal|":$INTNAME"
expr_stmt|;
block|}
else|else
block|{
name|streamDesc
operator|=
literal|"$INTNAME"
expr_stmt|;
block|}
comment|// TODO: remove this?
name|String
name|origStreamDesc
init|=
name|streamDesc
decl_stmt|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|mapWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|get
argument_list|(
name|streamDesc
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|streamDesc
operator|=
name|origStreamDesc
operator|.
name|concat
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
operator|++
name|pos
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|GenMapRedUtils
operator|.
name|setTaskPlan
argument_list|(
name|taskTmpDir
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|streamDesc
argument_list|,
name|tableScanOp
argument_list|,
name|mapWork
argument_list|,
literal|false
argument_list|,
name|tableDesc
argument_list|)
expr_stmt|;
comment|// insert the new task between current task and its child
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|newTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|newWork
argument_list|,
name|parseContext
operator|.
name|getConf
argument_list|()
argument_list|)
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
name|childTasks
init|=
name|currentTask
operator|.
name|getChildTasks
argument_list|()
decl_stmt|;
comment|// must have at most one child
if|if
condition|(
name|childTasks
operator|!=
literal|null
operator|&&
name|childTasks
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|childTask
init|=
name|childTasks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|currentTask
operator|.
name|removeDependentTask
argument_list|(
name|childTask
argument_list|)
expr_stmt|;
name|newTask
operator|.
name|addDependentTask
argument_list|(
name|childTask
argument_list|)
expr_stmt|;
block|}
name|currentTask
operator|.
name|addDependentTask
argument_list|(
name|newTask
argument_list|)
expr_stmt|;
name|newTask
operator|.
name|setFetchSource
argument_list|(
name|currentTask
operator|.
name|isFetchSource
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Whether we can split at reduceWork. For simplicity, let's require each work can    * have at most one child work. This may be relaxed by checking connectivity of the    * work graph after disconnect the current reduce work from its child    */
specifier|private
specifier|static
name|boolean
name|canSplit
parameter_list|(
name|SparkWork
name|sparkWork
parameter_list|)
block|{
for|for
control|(
name|BaseWork
name|baseWork
range|:
name|sparkWork
operator|.
name|getAllWorkUnsorted
argument_list|()
control|)
block|{
if|if
condition|(
name|sparkWork
operator|.
name|getChildren
argument_list|(
name|baseWork
argument_list|)
operator|.
name|size
argument_list|()
operator|>
literal|1
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
comment|/**    * Copy a sub-graph from originWork to newWork    */
specifier|private
specifier|static
name|void
name|copyWorkGraph
parameter_list|(
name|SparkWork
name|originWork
parameter_list|,
name|SparkWork
name|newWork
parameter_list|,
name|BaseWork
name|baseWork
parameter_list|,
name|boolean
name|upWards
parameter_list|)
block|{
if|if
condition|(
name|upWards
condition|)
block|{
for|for
control|(
name|BaseWork
name|parent
range|:
name|originWork
operator|.
name|getParents
argument_list|(
name|baseWork
argument_list|)
control|)
block|{
name|newWork
operator|.
name|add
argument_list|(
name|parent
argument_list|)
expr_stmt|;
name|SparkEdgeProperty
name|edgeProperty
init|=
name|originWork
operator|.
name|getEdgeProperty
argument_list|(
name|parent
argument_list|,
name|baseWork
argument_list|)
decl_stmt|;
name|newWork
operator|.
name|connect
argument_list|(
name|parent
argument_list|,
name|baseWork
argument_list|,
name|edgeProperty
argument_list|)
expr_stmt|;
name|copyWorkGraph
argument_list|(
name|originWork
argument_list|,
name|newWork
argument_list|,
name|parent
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|BaseWork
name|child
range|:
name|originWork
operator|.
name|getChildren
argument_list|(
name|baseWork
argument_list|)
control|)
block|{
name|newWork
operator|.
name|add
argument_list|(
name|child
argument_list|)
expr_stmt|;
name|SparkEdgeProperty
name|edgeProperty
init|=
name|originWork
operator|.
name|getEdgeProperty
argument_list|(
name|baseWork
argument_list|,
name|child
argument_list|)
decl_stmt|;
name|newWork
operator|.
name|connect
argument_list|(
name|baseWork
argument_list|,
name|child
argument_list|,
name|edgeProperty
argument_list|)
expr_stmt|;
name|copyWorkGraph
argument_list|(
name|originWork
argument_list|,
name|newWork
argument_list|,
name|child
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

