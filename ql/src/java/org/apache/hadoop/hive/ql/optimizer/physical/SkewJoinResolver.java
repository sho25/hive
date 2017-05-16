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
name|LinkedHashMap
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
name|CommonJoinOperator
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
name|DefaultGraphWalker
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
name|DefaultRuleDispatcher
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
name|GraphWalker
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
name|lib
operator|.
name|Rule
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
name|RuleRegExp
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
name|LoadFileDesc
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
name|LoadTableDesc
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_comment
comment|/**  * An implementation of PhysicalPlanResolver. It iterator each task with a rule  * dispatcher for its reducer operator tree, for task with join op in reducer,  * it will try to add a conditional task associated a list of skew join tasks.  */
end_comment

begin_class
specifier|public
class|class
name|SkewJoinResolver
implements|implements
name|PhysicalPlanResolver
block|{
specifier|private
specifier|final
specifier|static
name|Logger
name|LOG
init|=
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SkewJoinResolver
operator|.
name|class
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
name|Dispatcher
name|disp
init|=
operator|new
name|SkewJoinTaskDispatcher
argument_list|(
name|pctx
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
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
name|ogw
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
comment|/**    * Iterator a task with a rule dispatcher for its reducer operator tree.    */
class|class
name|SkewJoinTaskDispatcher
implements|implements
name|Dispatcher
block|{
specifier|private
name|PhysicalContext
name|physicalContext
decl_stmt|;
specifier|public
name|SkewJoinTaskDispatcher
parameter_list|(
name|PhysicalContext
name|context
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|physicalContext
operator|=
name|context
expr_stmt|;
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
name|nodeOutputs
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
name|task
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
operator|!
name|task
operator|.
name|isMapRedTask
argument_list|()
operator|||
name|task
operator|instanceof
name|ConditionalTask
operator|||
operator|(
operator|(
name|MapredWork
operator|)
name|task
operator|.
name|getWork
argument_list|()
operator|)
operator|.
name|getReduceWork
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|ParseContext
name|pc
init|=
name|physicalContext
operator|.
name|getParseContext
argument_list|()
decl_stmt|;
if|if
condition|(
name|pc
operator|.
name|getLoadTableWork
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|LoadTableDesc
name|ltd
range|:
name|pc
operator|.
name|getLoadTableWork
argument_list|()
control|)
block|{
if|if
condition|(
name|ltd
operator|.
name|getTxnId
argument_list|()
operator|==
literal|null
condition|)
continue|continue;
comment|// See the path in FSOP that calls fs.exists on finalPath.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Not using skew join because the destination table "
operator|+
name|ltd
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
operator|+
literal|" is an insert_only table"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
if|if
condition|(
name|pc
operator|.
name|getLoadFileWork
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|LoadFileDesc
name|lfd
range|:
name|pc
operator|.
name|getLoadFileWork
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|lfd
operator|.
name|isMmCtas
argument_list|()
condition|)
continue|continue;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Not using skew join because the destination table "
operator|+
name|lfd
operator|.
name|getDestinationCreateTable
argument_list|()
operator|+
literal|" is an insert_only table"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
name|SkewJoinProcCtx
name|skewJoinProcContext
init|=
operator|new
name|SkewJoinProcCtx
argument_list|(
name|task
argument_list|,
name|pc
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|opRules
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
argument_list|()
decl_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
name|CommonJoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
name|SkewJoinProcFactory
operator|.
name|getJoinProc
argument_list|()
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest
comment|// matching rule and passes the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|SkewJoinProcFactory
operator|.
name|getDefaultProc
argument_list|()
argument_list|,
name|opRules
argument_list|,
name|skewJoinProcContext
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
comment|// iterator the reducer operator tree
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
if|if
condition|(
operator|(
operator|(
name|MapredWork
operator|)
name|task
operator|.
name|getWork
argument_list|()
operator|)
operator|.
name|getReduceWork
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|topNodes
operator|.
name|add
argument_list|(
operator|(
operator|(
name|MapredWork
operator|)
name|task
operator|.
name|getWork
argument_list|()
operator|)
operator|.
name|getReduceWork
argument_list|()
operator|.
name|getReducer
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|public
name|PhysicalContext
name|getPhysicalContext
parameter_list|()
block|{
return|return
name|physicalContext
return|;
block|}
specifier|public
name|void
name|setPhysicalContext
parameter_list|(
name|PhysicalContext
name|physicalContext
parameter_list|)
block|{
name|this
operator|.
name|physicalContext
operator|=
name|physicalContext
expr_stmt|;
block|}
block|}
comment|/**    * A container of current task and parse context.    */
specifier|public
specifier|static
class|class
name|SkewJoinProcCtx
implements|implements
name|NodeProcessorCtx
block|{
specifier|private
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currentTask
decl_stmt|;
specifier|private
name|ParseContext
name|parseCtx
decl_stmt|;
specifier|public
name|SkewJoinProcCtx
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|,
name|ParseContext
name|parseCtx
parameter_list|)
block|{
name|currentTask
operator|=
name|task
expr_stmt|;
name|this
operator|.
name|parseCtx
operator|=
name|parseCtx
expr_stmt|;
block|}
specifier|public
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|getCurrentTask
parameter_list|()
block|{
return|return
name|currentTask
return|;
block|}
specifier|public
name|void
name|setCurrentTask
parameter_list|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|currentTask
parameter_list|)
block|{
name|this
operator|.
name|currentTask
operator|=
name|currentTask
expr_stmt|;
block|}
specifier|public
name|ParseContext
name|getParseCtx
parameter_list|()
block|{
return|return
name|parseCtx
return|;
block|}
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
block|}
block|}
end_class

end_unit

