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
name|HashMap
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|DependencyCollectionTask
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
name|FileSinkOperator
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
name|UnionOperator
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
name|hooks
operator|.
name|ReadEntity
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
name|hooks
operator|.
name|WriteEntity
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
name|plan
operator|.
name|DependencyCollectionWork
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
name|FileSinkDesc
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
name|MoveWork
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
name|TableDesc
import|;
end_import

begin_comment
comment|/**  * Processor Context for creating map reduce task. Walk the tree in a DFS manner  * and process the nodes. Some state is maintained about the current nodes  * visited so far.  */
end_comment

begin_class
specifier|public
class|class
name|GenMRProcContext
implements|implements
name|NodeProcessorCtx
block|{
comment|/**    * GenMapRedCtx is used to keep track of the current state.    */
specifier|public
specifier|static
class|class
name|GenMapRedCtx
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
decl_stmt|;
name|String
name|currAliasId
decl_stmt|;
specifier|public
name|GenMapRedCtx
parameter_list|()
block|{     }
comment|/**      * @param currTask      *          the current task      * @param currAliasId      */
specifier|public
name|GenMapRedCtx
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
parameter_list|,
name|String
name|currAliasId
parameter_list|)
block|{
name|this
operator|.
name|currTask
operator|=
name|currTask
expr_stmt|;
name|this
operator|.
name|currAliasId
operator|=
name|currAliasId
expr_stmt|;
block|}
comment|/**      * @return current task      */
specifier|public
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|getCurrTask
parameter_list|()
block|{
return|return
name|currTask
return|;
block|}
comment|/**      * @return current alias      */
specifier|public
name|String
name|getCurrAliasId
parameter_list|()
block|{
return|return
name|currAliasId
return|;
block|}
block|}
comment|/**    * GenMRUnionCtx.    *    */
specifier|public
specifier|static
class|class
name|GenMRUnionCtx
block|{
specifier|final
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|uTask
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|taskTmpDir
decl_stmt|;
name|List
argument_list|<
name|TableDesc
argument_list|>
name|tt_desc
decl_stmt|;
name|List
argument_list|<
name|TableScanOperator
argument_list|>
name|listTopOperators
decl_stmt|;
specifier|public
name|GenMRUnionCtx
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|uTask
parameter_list|)
block|{
name|this
operator|.
name|uTask
operator|=
name|uTask
expr_stmt|;
name|taskTmpDir
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|tt_desc
operator|=
operator|new
name|ArrayList
argument_list|<
name|TableDesc
argument_list|>
argument_list|()
expr_stmt|;
name|listTopOperators
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|getUTask
parameter_list|()
block|{
return|return
name|uTask
return|;
block|}
specifier|public
name|void
name|addTaskTmpDir
parameter_list|(
name|String
name|taskTmpDir
parameter_list|)
block|{
name|this
operator|.
name|taskTmpDir
operator|.
name|add
argument_list|(
name|taskTmpDir
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getTaskTmpDir
parameter_list|()
block|{
return|return
name|taskTmpDir
return|;
block|}
specifier|public
name|void
name|addTTDesc
parameter_list|(
name|TableDesc
name|tt_desc
parameter_list|)
block|{
name|this
operator|.
name|tt_desc
operator|.
name|add
argument_list|(
name|tt_desc
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|TableDesc
argument_list|>
name|getTTDesc
parameter_list|()
block|{
return|return
name|tt_desc
return|;
block|}
specifier|public
name|List
argument_list|<
name|TableScanOperator
argument_list|>
name|getListTopOperators
parameter_list|()
block|{
return|return
name|listTopOperators
return|;
block|}
specifier|public
name|void
name|addListTopOperators
parameter_list|(
name|TableScanOperator
name|topOperator
parameter_list|)
block|{
name|listTopOperators
operator|.
name|add
argument_list|(
name|topOperator
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
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
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|>
name|taskToSeenOps
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|UnionOperator
argument_list|,
name|GenMRUnionCtx
argument_list|>
name|unionTaskMap
decl_stmt|;
specifier|private
name|List
argument_list|<
name|FileSinkOperator
argument_list|>
name|seenFileSinkOps
decl_stmt|;
specifier|private
name|ParseContext
name|parseCtx
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Task
argument_list|<
name|MoveWork
argument_list|>
argument_list|>
name|mvTask
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
decl_stmt|;
specifier|private
name|LinkedHashMap
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
decl_stmt|;
specifier|private
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
decl_stmt|;
specifier|private
name|TableScanOperator
name|currTopOp
decl_stmt|;
specifier|private
name|UnionOperator
name|currUnionOp
decl_stmt|;
specifier|private
name|String
name|currAliasId
decl_stmt|;
specifier|private
name|DependencyCollectionTask
name|dependencyTaskForMultiInsert
decl_stmt|;
comment|// If many fileSinkDescs are linked to each other, it is a good idea to keep track of
comment|// tasks for first fileSinkDesc. others can use it
specifier|private
name|Map
argument_list|<
name|FileSinkDesc
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|linkedFileDescTasks
decl_stmt|;
comment|/**    * Set of read entities. This list is generated by the walker and is passed to    * the hooks.    */
specifier|private
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
decl_stmt|;
comment|/**    * Set of write entities. This list is generated by the walker and is passed    * to the hooks.    */
specifier|private
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
decl_stmt|;
specifier|public
name|GenMRProcContext
parameter_list|()
block|{   }
comment|/**    * @param conf    *          hive configuration    * @param opTaskMap    *          reducer to task mapping    * @param seenOps    *          operator already visited    * @param parseCtx    *          current parse context    * @param rootTasks    *          root tasks for the plan    * @param mvTask    *          the final move task    * @param mapCurrCtx    *          operator to task mappings    * @param inputs    *          the set of input tables/partitions generated by the walk    * @param outputs    *          the set of destinations generated by the walk    */
specifier|public
name|GenMRProcContext
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
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
parameter_list|,
name|ParseContext
name|parseCtx
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|MoveWork
argument_list|>
argument_list|>
name|mvTask
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|,
name|LinkedHashMap
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
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|opTaskMap
operator|=
name|opTaskMap
expr_stmt|;
name|this
operator|.
name|mvTask
operator|=
name|mvTask
expr_stmt|;
name|this
operator|.
name|parseCtx
operator|=
name|parseCtx
expr_stmt|;
name|this
operator|.
name|rootTasks
operator|=
name|rootTasks
expr_stmt|;
name|this
operator|.
name|mapCurrCtx
operator|=
name|mapCurrCtx
expr_stmt|;
name|this
operator|.
name|inputs
operator|=
name|inputs
expr_stmt|;
name|this
operator|.
name|outputs
operator|=
name|outputs
expr_stmt|;
name|currTask
operator|=
literal|null
expr_stmt|;
name|currTopOp
operator|=
literal|null
expr_stmt|;
name|currUnionOp
operator|=
literal|null
expr_stmt|;
name|currAliasId
operator|=
literal|null
expr_stmt|;
name|unionTaskMap
operator|=
operator|new
name|HashMap
argument_list|<
name|UnionOperator
argument_list|,
name|GenMRUnionCtx
argument_list|>
argument_list|()
expr_stmt|;
name|taskToSeenOps
operator|=
operator|new
name|HashMap
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|dependencyTaskForMultiInsert
operator|=
literal|null
expr_stmt|;
name|linkedFileDescTasks
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * @return reducer to task mapping    */
specifier|public
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
name|getOpTaskMap
parameter_list|()
block|{
return|return
name|opTaskMap
return|;
block|}
comment|/**    * @param opTaskMap    *          reducer to task mapping    */
specifier|public
name|void
name|setOpTaskMap
parameter_list|(
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
parameter_list|)
block|{
name|this
operator|.
name|opTaskMap
operator|=
name|opTaskMap
expr_stmt|;
block|}
specifier|public
name|boolean
name|isSeenOp
parameter_list|(
name|Task
name|task
parameter_list|,
name|Operator
name|operator
parameter_list|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|seenOps
init|=
name|taskToSeenOps
operator|.
name|get
argument_list|(
name|task
argument_list|)
decl_stmt|;
return|return
name|seenOps
operator|!=
literal|null
operator|&&
name|seenOps
operator|.
name|contains
argument_list|(
name|operator
argument_list|)
return|;
block|}
specifier|public
name|void
name|addSeenOp
parameter_list|(
name|Task
name|task
parameter_list|,
name|Operator
name|operator
parameter_list|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|seenOps
init|=
name|taskToSeenOps
operator|.
name|get
argument_list|(
name|task
argument_list|)
decl_stmt|;
if|if
condition|(
name|seenOps
operator|==
literal|null
condition|)
block|{
name|taskToSeenOps
operator|.
name|put
argument_list|(
name|task
argument_list|,
name|seenOps
operator|=
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
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|seenOps
operator|.
name|add
argument_list|(
name|operator
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return file operators already visited    */
specifier|public
name|List
argument_list|<
name|FileSinkOperator
argument_list|>
name|getSeenFileSinkOps
parameter_list|()
block|{
return|return
name|seenFileSinkOps
return|;
block|}
comment|/**    * @param seenFileSinkOps    *          file sink operators already visited    */
specifier|public
name|void
name|setSeenFileSinkOps
parameter_list|(
name|List
argument_list|<
name|FileSinkOperator
argument_list|>
name|seenFileSinkOps
parameter_list|)
block|{
name|this
operator|.
name|seenFileSinkOps
operator|=
name|seenFileSinkOps
expr_stmt|;
block|}
comment|/**    * @return current parse context    */
specifier|public
name|ParseContext
name|getParseCtx
parameter_list|()
block|{
return|return
name|parseCtx
return|;
block|}
comment|/**    * @param parseCtx    *          current parse context    */
specifier|public
name|void
name|setParseCtx
parameter_list|(
name|ParseContext
name|parseCtx
parameter_list|)
block|{
name|this
operator|.
name|parseCtx
operator|=
name|parseCtx
expr_stmt|;
block|}
comment|/**    * @return the final move task    */
specifier|public
name|List
argument_list|<
name|Task
argument_list|<
name|MoveWork
argument_list|>
argument_list|>
name|getMvTask
parameter_list|()
block|{
return|return
name|mvTask
return|;
block|}
comment|/**    * @param mvTask    *          the final move task    */
specifier|public
name|void
name|setMvTask
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|MoveWork
argument_list|>
argument_list|>
name|mvTask
parameter_list|)
block|{
name|this
operator|.
name|mvTask
operator|=
name|mvTask
expr_stmt|;
block|}
comment|/**    * @return root tasks for the plan    */
specifier|public
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|getRootTasks
parameter_list|()
block|{
return|return
name|rootTasks
return|;
block|}
comment|/**    * @param rootTasks    *          root tasks for the plan    */
specifier|public
name|void
name|setRootTasks
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|)
block|{
name|this
operator|.
name|rootTasks
operator|=
name|rootTasks
expr_stmt|;
block|}
specifier|public
name|boolean
name|addRootIfPossible
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|)
block|{
if|if
condition|(
name|task
operator|.
name|getParentTasks
argument_list|()
operator|==
literal|null
operator|||
name|task
operator|.
name|getParentTasks
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|rootTasks
operator|.
name|contains
argument_list|(
name|task
argument_list|)
condition|)
block|{
return|return
name|rootTasks
operator|.
name|add
argument_list|(
name|task
argument_list|)
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * @return operator to task mappings    */
specifier|public
name|LinkedHashMap
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
name|getMapCurrCtx
parameter_list|()
block|{
return|return
name|mapCurrCtx
return|;
block|}
comment|/**    * @param mapCurrCtx    *          operator to task mappings    */
specifier|public
name|void
name|setMapCurrCtx
parameter_list|(
name|LinkedHashMap
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
parameter_list|)
block|{
name|this
operator|.
name|mapCurrCtx
operator|=
name|mapCurrCtx
expr_stmt|;
block|}
comment|/**    * @return current task    */
specifier|public
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|getCurrTask
parameter_list|()
block|{
return|return
name|currTask
return|;
block|}
comment|/**    * @param currTask    *          current task    */
specifier|public
name|void
name|setCurrTask
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
parameter_list|)
block|{
name|this
operator|.
name|currTask
operator|=
name|currTask
expr_stmt|;
block|}
comment|/**    * @return current top operator    */
specifier|public
name|TableScanOperator
name|getCurrTopOp
parameter_list|()
block|{
return|return
name|currTopOp
return|;
block|}
comment|/**    * @param currTopOp    *          current top operator    */
specifier|public
name|void
name|setCurrTopOp
parameter_list|(
name|TableScanOperator
name|currTopOp
parameter_list|)
block|{
name|this
operator|.
name|currTopOp
operator|=
name|currTopOp
expr_stmt|;
block|}
specifier|public
name|UnionOperator
name|getCurrUnionOp
parameter_list|()
block|{
return|return
name|currUnionOp
return|;
block|}
comment|/**    * @param currUnionOp    *          current union operator    */
specifier|public
name|void
name|setCurrUnionOp
parameter_list|(
name|UnionOperator
name|currUnionOp
parameter_list|)
block|{
name|this
operator|.
name|currUnionOp
operator|=
name|currUnionOp
expr_stmt|;
block|}
comment|/**    * @return current top alias    */
specifier|public
name|String
name|getCurrAliasId
parameter_list|()
block|{
return|return
name|currAliasId
return|;
block|}
comment|/**    * @param currAliasId    *          current top alias    */
specifier|public
name|void
name|setCurrAliasId
parameter_list|(
name|String
name|currAliasId
parameter_list|)
block|{
name|this
operator|.
name|currAliasId
operator|=
name|currAliasId
expr_stmt|;
block|}
specifier|public
name|GenMRUnionCtx
name|getUnionTask
parameter_list|(
name|UnionOperator
name|op
parameter_list|)
block|{
return|return
name|unionTaskMap
operator|.
name|get
argument_list|(
name|op
argument_list|)
return|;
block|}
specifier|public
name|void
name|setUnionTask
parameter_list|(
name|UnionOperator
name|op
parameter_list|,
name|GenMRUnionCtx
name|uTask
parameter_list|)
block|{
name|unionTaskMap
operator|.
name|put
argument_list|(
name|op
argument_list|,
name|uTask
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the input set.    */
specifier|public
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|getInputs
parameter_list|()
block|{
return|return
name|inputs
return|;
block|}
comment|/**    * Get the output set.    */
specifier|public
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|getOutputs
parameter_list|()
block|{
return|return
name|outputs
return|;
block|}
comment|/**    * @return the conf    */
specifier|public
name|HiveConf
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
comment|/**    * @param conf    *          the conf to set    */
specifier|public
name|void
name|setConf
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
comment|/**    * Returns dependencyTaskForMultiInsert initializing it if necessary.    *    * dependencyTaskForMultiInsert serves as a mutual dependency for the final move tasks in a    * multi-insert query.    *    * @return    */
specifier|public
name|DependencyCollectionTask
name|getDependencyTaskForMultiInsert
parameter_list|()
block|{
if|if
condition|(
name|dependencyTaskForMultiInsert
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_MULTI_INSERT_MOVE_TASKS_SHARE_DEPENDENCIES
argument_list|)
condition|)
block|{
name|dependencyTaskForMultiInsert
operator|=
operator|(
name|DependencyCollectionTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DependencyCollectionWork
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|dependencyTaskForMultiInsert
return|;
block|}
specifier|public
name|Map
argument_list|<
name|FileSinkDesc
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|getLinkedFileDescTasks
parameter_list|()
block|{
return|return
name|linkedFileDescTasks
return|;
block|}
specifier|public
name|void
name|setLinkedFileDescTasks
parameter_list|(
name|Map
argument_list|<
name|FileSinkDesc
argument_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|linkedFileDescTasks
parameter_list|)
block|{
name|this
operator|.
name|linkedFileDescTasks
operator|=
name|linkedFileDescTasks
expr_stmt|;
block|}
block|}
end_class

end_unit

