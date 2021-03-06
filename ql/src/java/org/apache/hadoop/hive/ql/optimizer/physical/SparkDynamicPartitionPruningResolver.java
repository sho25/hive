begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|SemanticDispatcher
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
name|spark
operator|.
name|SparkPartitionPruningSinkDesc
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
name|spark
operator|.
name|SparkPartitionPruningSinkDesc
operator|.
name|DPPTargetInfo
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

begin_comment
comment|/**  * A physical optimization that disables DPP if the source {@link MapWork} and target {@link MapWork} aren't in  * dependent {@link SparkTask}s.  *  *<p>  *   When DPP is run, the source {@link MapWork} produces a temp file that is read by the target {@link MapWork}. The  *   source {@link MapWork} must be run before the target {@link MapWork} is run, otherwise the target {@link MapWork}  *   will throw a {@link java.io.FileNotFoundException}. In order to guarantee this, the source {@link MapWork} must be  *   inside a {@link SparkTask} that runs before the {@link SparkTask} containing the target {@link MapWork}.  *</p>  *  *<p>  *   This {@link PhysicalPlanResolver} works by walking through the {@link Task} DAG and iterating over all the  *   {@link SparkPartitionPruningSinkOperator}s inside the {@link SparkTask}. For each sink operator, it takes the  *   target {@link MapWork} and checks if it exists in any of the child {@link SparkTask}s. If the target {@link MapWork}  *   is not in any child {@link SparkTask} then it removes the operator subtree that contains the  *   {@link SparkPartitionPruningSinkOperator}.  *</p>  */
end_comment

begin_class
specifier|public
class|class
name|SparkDynamicPartitionPruningResolver
implements|implements
name|PhysicalPlanResolver
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SparkDynamicPartitionPruningResolver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
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
comment|// Walk through the Task Graph and invoke SparkDynamicPartitionPruningDispatcher
name|TaskGraphWalker
name|graphWalker
init|=
operator|new
name|TaskGraphWalker
argument_list|(
operator|new
name|SparkDynamicPartitionPruningDispatcher
argument_list|()
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Node
argument_list|>
name|rootTasks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|rootTasks
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
name|rootTasks
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|pctx
return|;
block|}
specifier|private
class|class
name|SparkDynamicPartitionPruningDispatcher
implements|implements
name|SemanticDispatcher
block|{
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
name|Task
argument_list|<
name|?
argument_list|>
name|task
init|=
operator|(
name|Task
argument_list|<
name|?
argument_list|>
operator|)
name|nd
decl_stmt|;
comment|// If the given Task is a SparkTask then search its Work DAG for SparkPartitionPruningSinkOperator
if|if
condition|(
name|task
operator|instanceof
name|SparkTask
condition|)
block|{
comment|// Search for any SparkPartitionPruningSinkOperator in the SparkTask
for|for
control|(
name|BaseWork
name|baseWork
range|:
operator|(
operator|(
name|SparkTask
operator|)
name|task
operator|)
operator|.
name|getWork
argument_list|()
operator|.
name|getAllWork
argument_list|()
control|)
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|pruningSinkOps
init|=
name|OperatorUtils
operator|.
name|getOp
argument_list|(
name|baseWork
argument_list|,
name|SparkPartitionPruningSinkOperator
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// For each SparkPartitionPruningSinkOperator, take the target MapWork and see if it is in a dependent SparkTask
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|pruningSinkOps
control|)
block|{
name|SparkPartitionPruningSinkOperator
name|pruningSinkOp
init|=
operator|(
name|SparkPartitionPruningSinkOperator
operator|)
name|op
decl_stmt|;
name|SparkPartitionPruningSinkDesc
name|desc
init|=
name|pruningSinkOp
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|DPPTargetInfo
argument_list|>
name|toRemove
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|DPPTargetInfo
name|targetInfo
range|:
name|desc
operator|.
name|getTargetInfos
argument_list|()
control|)
block|{
name|MapWork
name|targetMapWork
init|=
name|targetInfo
operator|.
name|work
decl_stmt|;
comment|// Check if the given SparkTask has a child SparkTask that contains the target MapWork
comment|// If it does not, then remove the target from DPP op
if|if
condition|(
operator|!
name|taskContainsDependentMapWork
argument_list|(
name|task
argument_list|,
name|targetMapWork
argument_list|)
condition|)
block|{
name|toRemove
operator|.
name|add
argument_list|(
name|targetInfo
argument_list|)
expr_stmt|;
name|pruningSinkOp
operator|.
name|removeFromSourceEvent
argument_list|(
name|targetMapWork
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Removing target map work "
operator|+
name|targetMapWork
operator|.
name|getName
argument_list|()
operator|+
literal|" from "
operator|+
name|baseWork
operator|.
name|getName
argument_list|()
operator|+
literal|" as no dependency exists between the two works."
argument_list|)
expr_stmt|;
block|}
block|}
name|desc
operator|.
name|getTargetInfos
argument_list|()
operator|.
name|removeAll
argument_list|(
name|toRemove
argument_list|)
expr_stmt|;
if|if
condition|(
name|desc
operator|.
name|getTargetInfos
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// The DPP sink has no target, remove the subtree.
name|OperatorUtils
operator|.
name|removeBranch
argument_list|(
name|pruningSinkOp
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Recursively go through the children of the given {@link Task} and check if any child {@link SparkTask} contains    * the specified {@link MapWork} object.    */
specifier|private
name|boolean
name|taskContainsDependentMapWork
parameter_list|(
name|Task
argument_list|<
name|?
argument_list|>
name|task
parameter_list|,
name|MapWork
name|work
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|task
operator|==
literal|null
operator|||
name|task
operator|.
name|getChildTasks
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|Task
argument_list|<
name|?
argument_list|>
name|childTask
range|:
name|task
operator|.
name|getChildTasks
argument_list|()
control|)
block|{
if|if
condition|(
name|childTask
operator|!=
literal|null
operator|&&
name|childTask
operator|instanceof
name|SparkTask
operator|&&
name|childTask
operator|.
name|getMapWork
argument_list|()
operator|.
name|contains
argument_list|(
name|work
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|taskContainsDependentMapWork
argument_list|(
name|childTask
argument_list|,
name|work
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

