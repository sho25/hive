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
name|parse
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
name|Deque
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
name|tez
operator|.
name|TezTask
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
name|TezWork
import|;
end_import

begin_comment
comment|/**  * GenTezProcContext. GenTezProcContext maintains information  * about the tasks and operators as we walk the operator tree  * to break them into TezTasks.  *  */
end_comment

begin_class
specifier|public
class|class
name|GenTezProcContext
implements|implements
name|NodeProcessorCtx
block|{
specifier|public
specifier|final
name|ParseContext
name|parseContext
decl_stmt|;
specifier|public
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|public
specifier|final
name|List
argument_list|<
name|Task
argument_list|<
name|MoveWork
argument_list|>
argument_list|>
name|moveTask
decl_stmt|;
comment|// rootTasks is the entry point for all generated tasks
specifier|public
specifier|final
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
specifier|public
specifier|final
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
decl_stmt|;
specifier|public
specifier|final
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
decl_stmt|;
comment|// rootOperators are all the table scan operators in sequence
comment|// of traversal
specifier|public
specifier|final
name|Deque
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|rootOperators
decl_stmt|;
comment|// holds the root of the operator tree we're currently processing
comment|// this could be a table scan, but also a join, ptf, etc (i.e.:
comment|// first operator of a reduce task.
specifier|public
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|currentRootOperator
decl_stmt|;
comment|// this is the original parent of the currentRootOperator as we scan
comment|// through the graph. A root operator might have multiple parents and
comment|// we just use this one to remember where we came from in the current
comment|// walk.
specifier|public
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentOfRoot
decl_stmt|;
comment|// tez task we're currently processing
specifier|public
name|TezTask
name|currentTask
decl_stmt|;
comment|// last work we've processed (in order to hook it up to the current
comment|// one.
specifier|public
name|BaseWork
name|preceedingWork
decl_stmt|;
comment|// map that keeps track of the last operator of a task to the work
comment|// that follows it. This is used for connecting them later.
specifier|public
specifier|final
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|BaseWork
argument_list|>
name|leafOperatorToFollowingWork
decl_stmt|;
comment|// a map that keeps track of work that need to be linked while
comment|// traversing an operator tree
specifier|public
specifier|final
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|List
argument_list|<
name|BaseWork
argument_list|>
argument_list|>
name|linkOpWithWorkMap
decl_stmt|;
comment|// a map that maintains operator (file-sink or reduce-sink) to work mapping
specifier|public
specifier|final
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|BaseWork
argument_list|>
name|operatorWorkMap
decl_stmt|;
comment|// we need to keep the original list of operators in the map join to know
comment|// what position in the mapjoin the different parent work items will have.
specifier|public
specifier|final
name|Map
argument_list|<
name|MapJoinOperator
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
name|mapJoinParentMap
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|GenTezProcContext
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|ParseContext
name|parseContext
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|MoveWork
argument_list|>
argument_list|>
name|moveTask
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
parameter_list|,
name|Deque
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|rootOperators
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
name|parseContext
operator|=
name|parseContext
expr_stmt|;
name|this
operator|.
name|moveTask
operator|=
name|moveTask
expr_stmt|;
name|this
operator|.
name|rootTasks
operator|=
name|rootTasks
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
name|this
operator|.
name|currentTask
operator|=
operator|(
name|TezTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|TezWork
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|leafOperatorToFollowingWork
operator|=
operator|new
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|BaseWork
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|rootOperators
operator|=
name|rootOperators
expr_stmt|;
name|this
operator|.
name|linkOpWithWorkMap
operator|=
operator|new
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|List
argument_list|<
name|BaseWork
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|operatorWorkMap
operator|=
operator|new
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|BaseWork
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|mapJoinParentMap
operator|=
operator|new
name|HashMap
argument_list|<
name|MapJoinOperator
argument_list|,
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

