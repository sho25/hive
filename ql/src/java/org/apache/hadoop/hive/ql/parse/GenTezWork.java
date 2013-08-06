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
name|Stack
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
comment|/**  * GenTezWork separates the operator tree into tez tasks.  * It is called once per leaf operator (operator that forces  * a new execution unit.) and break the operators into work  * and tasks along the way.  */
end_comment

begin_class
specifier|public
class|class
name|GenTezWork
implements|implements
name|NodeProcessor
block|{
specifier|static
specifier|final
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|GenTezWork
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
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
name|procContext
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|GenTezProcContext
name|context
init|=
operator|(
name|GenTezProcContext
operator|)
name|procContext
decl_stmt|;
comment|// Operator is a file sink or reduce sink. Something that forces
comment|// a new vertex.
name|Operator
argument_list|<
name|?
argument_list|>
name|operator
init|=
operator|(
name|Operator
argument_list|<
name|?
argument_list|>
operator|)
name|nd
decl_stmt|;
name|TezWork
name|tezWork
init|=
name|context
operator|.
name|currentTask
operator|.
name|getWork
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|context
operator|.
name|rootTasks
operator|.
name|contains
argument_list|(
name|context
operator|.
name|currentTask
argument_list|)
condition|)
block|{
name|context
operator|.
name|rootTasks
operator|.
name|add
argument_list|(
name|context
operator|.
name|currentTask
argument_list|)
expr_stmt|;
block|}
comment|// root is the start of the operator pipeline we're currently
comment|// packing into a vertex, typically a table scan, union or join
name|Operator
argument_list|<
name|?
argument_list|>
name|root
init|=
name|context
operator|.
name|currentRootOperator
decl_stmt|;
if|if
condition|(
name|root
operator|==
literal|null
condition|)
block|{
comment|// null means that we're starting with a new table scan
comment|// the graph walker walks the rootOperators in the same
comment|// order so we can just take the next
name|context
operator|.
name|preceedingWork
operator|=
literal|null
expr_stmt|;
name|root
operator|=
name|context
operator|.
name|rootOperators
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Root operator: "
operator|+
name|root
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Leaf operator: "
operator|+
name|operator
argument_list|)
expr_stmt|;
comment|// Right now the work graph is pretty simple. If there is no
comment|// Preceding work we have a root and will generate a map
comment|// vertex. If there is a preceding work we will generate
comment|// a reduce vertex
name|BaseWork
name|work
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|preceedingWork
operator|==
literal|null
condition|)
block|{
assert|assert
name|root
operator|.
name|getParentOperators
argument_list|()
operator|.
name|isEmpty
argument_list|()
assert|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding map work for "
operator|+
name|root
argument_list|)
expr_stmt|;
name|MapWork
name|mapWork
init|=
operator|new
name|MapWork
argument_list|()
decl_stmt|;
name|mapWork
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|put
argument_list|(
literal|""
argument_list|,
name|root
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|add
argument_list|(
name|mapWork
argument_list|)
expr_stmt|;
name|work
operator|=
name|mapWork
expr_stmt|;
block|}
else|else
block|{
assert|assert
operator|!
name|root
operator|.
name|getParentOperators
argument_list|()
operator|.
name|isEmpty
argument_list|()
assert|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Adding reduce work for "
operator|+
name|root
argument_list|)
expr_stmt|;
name|ReduceWork
name|reduceWork
init|=
operator|new
name|ReduceWork
argument_list|()
decl_stmt|;
name|reduceWork
operator|.
name|setReducer
argument_list|(
name|root
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|add
argument_list|(
name|reduceWork
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|connect
argument_list|(
name|context
operator|.
name|preceedingWork
argument_list|,
name|reduceWork
argument_list|)
expr_stmt|;
name|work
operator|=
name|reduceWork
expr_stmt|;
block|}
comment|// We're scanning the operator from table scan to final file sink.
comment|// We're scanning a tree from roots to leaf (this is not technically
comment|// correct, demux and mux operators might form a diamond shape, but
comment|// we will only scan one path and ignore the others, because the
comment|// diamond shape is always contained in a single vertex). The scan
comment|// is depth first and because we remove parents when we pack a pipeline
comment|// into a vertex we will never visit any node twice. But because of that
comment|// we might have a situation where we need to connect 'work' that comes after
comment|// the 'work' we're currently looking at.
comment|//
comment|// Also note: the concept of leaf and root is reversed in hive for historical
comment|// reasons. Roots are data sources, leaves are data sinks. I know.
if|if
condition|(
name|context
operator|.
name|leafOperatorToFollowingWork
operator|.
name|containsKey
argument_list|(
name|operator
argument_list|)
condition|)
block|{
name|tezWork
operator|.
name|connect
argument_list|(
name|work
argument_list|,
name|context
operator|.
name|leafOperatorToFollowingWork
operator|.
name|get
argument_list|(
name|operator
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// This is where we cut the tree as described above. We also remember that
comment|// we might have to connect parent work with this work later.
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|parent
range|:
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|(
name|root
operator|.
name|getParentOperators
argument_list|()
argument_list|)
control|)
block|{
assert|assert
operator|!
name|context
operator|.
name|leafOperatorToFollowingWork
operator|.
name|containsKey
argument_list|(
name|parent
argument_list|)
assert|;
name|context
operator|.
name|leafOperatorToFollowingWork
operator|.
name|put
argument_list|(
name|parent
argument_list|,
name|work
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing "
operator|+
name|parent
operator|+
literal|" as parent from "
operator|+
name|root
argument_list|)
expr_stmt|;
name|root
operator|.
name|removeParent
argument_list|(
name|parent
argument_list|)
expr_stmt|;
block|}
comment|// No children means we're at the bottom. If there are more operators to scan
comment|// the next item will be a new root.
if|if
condition|(
operator|!
name|operator
operator|.
name|getChildOperators
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
assert|assert
name|operator
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|context
operator|.
name|currentRootOperator
operator|=
name|operator
operator|.
name|getChildOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|context
operator|.
name|preceedingWork
operator|=
name|work
expr_stmt|;
block|}
else|else
block|{
name|context
operator|.
name|currentRootOperator
operator|=
literal|null
expr_stmt|;
name|context
operator|.
name|preceedingWork
operator|=
literal|null
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

