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
name|hooks
package|;
end_package

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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|SemanticGraphWalker
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
name|SemanticNodeProcessor
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
name|TezWork
import|;
end_import

begin_comment
comment|/**  * Checks whenever operator ids are not reused.  */
end_comment

begin_class
specifier|public
class|class
name|NoOperatorReuseCheckerHook
implements|implements
name|ExecuteWithHookContext
block|{
specifier|static
class|class
name|UniqueOpIdChecker
implements|implements
name|SemanticNodeProcessor
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|opMap
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
name|Operator
name|op
init|=
operator|(
name|Operator
operator|)
name|nd
decl_stmt|;
name|String
name|opKey
init|=
name|op
operator|.
name|getOperatorId
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
argument_list|>
name|found
init|=
name|opMap
operator|.
name|get
argument_list|(
name|opKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|found
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"operator id reuse found: "
operator|+
name|opKey
argument_list|)
throw|;
block|}
name|opMap
operator|.
name|put
argument_list|(
name|opKey
argument_list|,
name|op
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|(
name|HookContext
name|hookContext
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Node
argument_list|>
name|rootOps
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|roots
init|=
name|hookContext
operator|.
name|getQueryPlan
argument_list|()
operator|.
name|getRootTasks
argument_list|()
decl_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
argument_list|>
name|task
range|:
name|roots
control|)
block|{
name|Object
name|work
init|=
name|task
operator|.
name|getWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|work
operator|instanceof
name|MapredWork
condition|)
block|{
name|MapredWork
name|mapredWork
init|=
operator|(
name|MapredWork
operator|)
name|work
decl_stmt|;
name|MapWork
name|mapWork
init|=
name|mapredWork
operator|.
name|getMapWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|mapWork
operator|!=
literal|null
condition|)
block|{
name|rootOps
operator|.
name|addAll
argument_list|(
name|mapWork
operator|.
name|getAllRootOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ReduceWork
name|reduceWork
init|=
name|mapredWork
operator|.
name|getReduceWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|reduceWork
operator|!=
literal|null
condition|)
block|{
name|rootOps
operator|.
name|addAll
argument_list|(
name|reduceWork
operator|.
name|getAllRootOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|work
operator|instanceof
name|TezWork
condition|)
block|{
for|for
control|(
name|BaseWork
name|bw
range|:
operator|(
operator|(
name|TezWork
operator|)
name|work
operator|)
operator|.
name|getAllWorkUnsorted
argument_list|()
control|)
block|{
name|rootOps
operator|.
name|addAll
argument_list|(
name|bw
operator|.
name|getAllRootOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|rootOps
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
name|SemanticDispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
operator|new
name|UniqueOpIdChecker
argument_list|()
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|SemanticGraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
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
operator|new
name|HashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|rootOps
argument_list|,
name|nodeOutput
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

