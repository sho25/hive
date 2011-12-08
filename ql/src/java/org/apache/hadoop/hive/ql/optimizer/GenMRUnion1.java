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
name|OperatorFactory
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
name|GenMRMapJoinCtx
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
name|GenMRUnionCtx
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
name|optimizer
operator|.
name|unionproc
operator|.
name|UnionProcContext
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
name|unionproc
operator|.
name|UnionProcFactory
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
name|unionproc
operator|.
name|UnionProcContext
operator|.
name|UnionParseContext
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
name|PartitionDesc
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
name|TableDesc
import|;
end_import

begin_comment
comment|/**  * Processor for the rule - TableScan followed by Union.  */
end_comment

begin_class
specifier|public
class|class
name|GenMRUnion1
implements|implements
name|NodeProcessor
block|{
specifier|public
name|GenMRUnion1
parameter_list|()
block|{   }
comment|/**    * Union Operator encountered . Currently, the algorithm is pretty simple: If    * all the sub-queries are map-only, dont do anything. However, if there is a    * mapjoin followed by the union, merge at the union Otherwise, insert a    * FileSink on top of all the sub-queries.    *    * This can be optimized later on.    *    * @param nd    *          the file sink operator encountered    * @param opProcCtx    *          context    */
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
name|opProcCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|UnionOperator
name|union
init|=
operator|(
name|UnionOperator
operator|)
name|nd
decl_stmt|;
name|GenMRProcContext
name|ctx
init|=
operator|(
name|GenMRProcContext
operator|)
name|opProcCtx
decl_stmt|;
name|ParseContext
name|parseCtx
init|=
name|ctx
operator|.
name|getParseCtx
argument_list|()
decl_stmt|;
name|UnionProcContext
name|uCtx
init|=
name|parseCtx
operator|.
name|getUCtx
argument_list|()
decl_stmt|;
comment|// Map-only subqueries can be optimized in future to not write to a file in
comment|// future
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
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
comment|// The plan needs to be broken only if one of the sub-queries involve a
comment|// map-reduce job
if|if
condition|(
name|uCtx
operator|.
name|isMapOnlySubq
argument_list|()
condition|)
block|{
comment|// merge currTask from multiple topOps
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
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
if|if
condition|(
name|opTaskMap
operator|!=
literal|null
operator|&&
name|opTaskMap
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
name|tsk
init|=
name|opTaskMap
operator|.
name|get
argument_list|(
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|tsk
operator|!=
literal|null
condition|)
block|{
name|ctx
operator|.
name|setCurrTask
argument_list|(
name|tsk
argument_list|)
expr_stmt|;
block|}
block|}
name|UnionParseContext
name|uPrsCtx
init|=
name|uCtx
operator|.
name|getUnionParseContext
argument_list|(
name|union
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|uPrsCtx
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|uPrsCtx
operator|.
name|getMapJoinQuery
argument_list|()
operator|)
condition|)
block|{
name|GenMapRedUtils
operator|.
name|mergeMapJoinUnion
argument_list|(
name|union
argument_list|,
name|ctx
argument_list|,
name|UnionProcFactory
operator|.
name|getPositionParent
argument_list|(
name|union
argument_list|,
name|stack
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|mapCurrCtx
operator|.
name|put
argument_list|(
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|nd
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
name|getCurrTopOp
argument_list|()
argument_list|,
name|ctx
operator|.
name|getCurrAliasId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
name|ctx
operator|.
name|setCurrUnionOp
argument_list|(
name|union
argument_list|)
expr_stmt|;
name|UnionParseContext
name|uPrsCtx
init|=
name|uCtx
operator|.
name|getUnionParseContext
argument_list|(
name|union
argument_list|)
decl_stmt|;
assert|assert
name|uPrsCtx
operator|!=
literal|null
assert|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currTask
init|=
name|ctx
operator|.
name|getCurrTask
argument_list|()
decl_stmt|;
name|int
name|pos
init|=
name|UnionProcFactory
operator|.
name|getPositionParent
argument_list|(
name|union
argument_list|,
name|stack
argument_list|)
decl_stmt|;
comment|// is the current task a root task
if|if
condition|(
name|uPrsCtx
operator|.
name|getRootTask
argument_list|(
name|pos
argument_list|)
operator|&&
operator|(
operator|!
name|ctx
operator|.
name|getRootTasks
argument_list|()
operator|.
name|contains
argument_list|(
name|currTask
argument_list|)
operator|)
condition|)
block|{
name|ctx
operator|.
name|getRootTasks
argument_list|()
operator|.
name|add
argument_list|(
name|currTask
argument_list|)
expr_stmt|;
block|}
name|GenMRUnionCtx
name|uCtxTask
init|=
name|ctx
operator|.
name|getUnionTask
argument_list|(
name|union
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|uTask
init|=
literal|null
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|parent
init|=
name|union
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|MapredWork
name|uPlan
init|=
literal|null
decl_stmt|;
comment|// union is encountered for the first time
if|if
condition|(
name|uCtxTask
operator|==
literal|null
condition|)
block|{
name|uCtxTask
operator|=
operator|new
name|GenMRUnionCtx
argument_list|()
expr_stmt|;
name|uPlan
operator|=
name|GenMapRedUtils
operator|.
name|getMapRedWork
argument_list|(
name|parseCtx
argument_list|)
expr_stmt|;
name|uTask
operator|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|uPlan
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|uCtxTask
operator|.
name|setUTask
argument_list|(
name|uTask
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setUnionTask
argument_list|(
name|union
argument_list|,
name|uCtxTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|uTask
operator|=
name|uCtxTask
operator|.
name|getUTask
argument_list|()
expr_stmt|;
block|}
comment|// If there is a mapjoin at position 'pos'
if|if
condition|(
name|uPrsCtx
operator|.
name|getMapJoinSubq
argument_list|(
name|pos
argument_list|)
condition|)
block|{
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
name|mjOp
init|=
name|ctx
operator|.
name|getCurrMapJoinOp
argument_list|()
decl_stmt|;
assert|assert
name|mjOp
operator|!=
literal|null
assert|;
name|GenMRMapJoinCtx
name|mjCtx
init|=
name|ctx
operator|.
name|getMapJoinCtx
argument_list|(
name|mjOp
argument_list|)
decl_stmt|;
assert|assert
name|mjCtx
operator|!=
literal|null
assert|;
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
name|String
name|taskTmpDir
init|=
name|mjCtx
operator|.
name|getTaskTmpDir
argument_list|()
decl_stmt|;
name|TableDesc
name|tt_desc
init|=
name|mjCtx
operator|.
name|getTTDesc
argument_list|()
decl_stmt|;
assert|assert
name|plan
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|get
argument_list|(
name|taskTmpDir
argument_list|)
operator|==
literal|null
assert|;
name|plan
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|put
argument_list|(
name|taskTmpDir
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|plan
operator|.
name|getPathToAliases
argument_list|()
operator|.
name|get
argument_list|(
name|taskTmpDir
argument_list|)
operator|.
name|add
argument_list|(
name|taskTmpDir
argument_list|)
expr_stmt|;
name|plan
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|put
argument_list|(
name|taskTmpDir
argument_list|,
operator|new
name|PartitionDesc
argument_list|(
name|tt_desc
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|plan
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|put
argument_list|(
name|taskTmpDir
argument_list|,
name|mjCtx
operator|.
name|getRootMapJoinOp
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|TableDesc
name|tt_desc
init|=
name|PlanUtils
operator|.
name|getIntermediateFileTableDesc
argument_list|(
name|PlanUtils
operator|.
name|getFieldSchemasFromRowSchema
argument_list|(
name|parent
operator|.
name|getSchema
argument_list|()
argument_list|,
literal|"temporarycol"
argument_list|)
argument_list|)
decl_stmt|;
comment|// generate the temporary file
name|Context
name|baseCtx
init|=
name|parseCtx
operator|.
name|getContext
argument_list|()
decl_stmt|;
name|String
name|taskTmpDir
init|=
name|baseCtx
operator|.
name|getMRTmpFileURI
argument_list|()
decl_stmt|;
comment|// Add the path to alias mapping
name|uCtxTask
operator|.
name|addTaskTmpDir
argument_list|(
name|taskTmpDir
argument_list|)
expr_stmt|;
name|uCtxTask
operator|.
name|addTTDesc
argument_list|(
name|tt_desc
argument_list|)
expr_stmt|;
comment|// The union task is empty. The files created for all the inputs are
comment|// assembled in the
comment|// union context and later used to initialize the union plan
comment|// Create a file sink operator for this file name
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|fs_op
init|=
name|OperatorFactory
operator|.
name|get
argument_list|(
operator|new
name|FileSinkDesc
argument_list|(
name|taskTmpDir
argument_list|,
name|tt_desc
argument_list|,
name|parseCtx
operator|.
name|getConf
argument_list|()
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|COMPRESSINTERMEDIATE
argument_list|)
argument_list|)
argument_list|,
name|parent
operator|.
name|getSchema
argument_list|()
argument_list|)
decl_stmt|;
assert|assert
name|parent
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|parent
operator|.
name|getChildOperators
argument_list|()
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|fs_op
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|parentOpList
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|parentOpList
operator|.
name|add
argument_list|(
name|parent
argument_list|)
expr_stmt|;
name|fs_op
operator|.
name|setParentOperators
argument_list|(
name|parentOpList
argument_list|)
expr_stmt|;
name|currTask
operator|.
name|addDependentTask
argument_list|(
name|uTask
argument_list|)
expr_stmt|;
comment|// If it is map-only task, add the files to be processed
if|if
condition|(
name|uPrsCtx
operator|.
name|getMapOnlySubq
argument_list|(
name|pos
argument_list|)
operator|&&
name|uPrsCtx
operator|.
name|getRootTask
argument_list|(
name|pos
argument_list|)
condition|)
block|{
name|GenMapRedUtils
operator|.
name|setTaskPlan
argument_list|(
name|ctx
operator|.
name|getCurrAliasId
argument_list|()
argument_list|,
name|ctx
operator|.
name|getCurrTopOp
argument_list|()
argument_list|,
operator|(
name|MapredWork
operator|)
name|currTask
operator|.
name|getWork
argument_list|()
argument_list|,
literal|false
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|setCurrTask
argument_list|(
name|uTask
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCurrAliasId
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setCurrTopOp
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|mapCurrCtx
operator|.
name|put
argument_list|(
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|nd
argument_list|,
operator|new
name|GenMapRedCtx
argument_list|(
name|ctx
operator|.
name|getCurrTask
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

