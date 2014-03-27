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
name|HashTableDummyOperator
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
name|RowSchema
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
name|parse
operator|.
name|GenTezProcContext
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
name|ExprNodeDesc
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
name|HashTableDummyDesc
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
name|ReduceSinkDesc
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
operator|.
name|EdgeType
import|;
end_import

begin_class
specifier|public
class|class
name|ReduceSinkMapJoinProc
implements|implements
name|NodeProcessor
block|{
specifier|protected
specifier|transient
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/* (non-Javadoc)    * This processor addresses the RS-MJ case that occurs in tez on the small/hash    * table side of things. The work that RS will be a part of must be connected     * to the MJ work via be a broadcast edge.    * We should not walk down the tree when we encounter this pattern because:    * the type of work (map work or reduce work) needs to be determined    * on the basis of the big table side because it may be a mapwork (no need for shuffle)    * or reduce work.    */
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
name|MapJoinOperator
name|mapJoinOp
init|=
operator|(
name|MapJoinOperator
operator|)
name|nd
decl_stmt|;
if|if
condition|(
name|stack
operator|.
name|size
argument_list|()
operator|<
literal|2
operator|||
operator|!
operator|(
name|stack
operator|.
name|get
argument_list|(
name|stack
operator|.
name|size
argument_list|()
operator|-
literal|2
argument_list|)
operator|instanceof
name|ReduceSinkOperator
operator|)
condition|)
block|{
name|context
operator|.
name|currentMapJoinOperators
operator|.
name|add
argument_list|(
name|mapJoinOp
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|context
operator|.
name|preceedingWork
operator|=
literal|null
expr_stmt|;
name|context
operator|.
name|currentRootOperator
operator|=
literal|null
expr_stmt|;
name|ReduceSinkOperator
name|parentRS
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|stack
operator|.
name|get
argument_list|(
name|stack
operator|.
name|size
argument_list|()
operator|-
literal|2
argument_list|)
decl_stmt|;
comment|// remember the original parent list before we start modifying it.
if|if
condition|(
operator|!
name|context
operator|.
name|mapJoinParentMap
operator|.
name|containsKey
argument_list|(
name|mapJoinOp
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|parents
init|=
operator|new
name|ArrayList
argument_list|(
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
argument_list|)
decl_stmt|;
name|context
operator|.
name|mapJoinParentMap
operator|.
name|put
argument_list|(
name|mapJoinOp
argument_list|,
name|parents
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|BaseWork
argument_list|>
name|mapJoinWork
init|=
literal|null
decl_stmt|;
comment|/*      *  if there was a pre-existing work generated for the big-table mapjoin side,      *  we need to hook the work generated for the RS (associated with the RS-MJ pattern)      *  with the pre-existing work.      *      *  Otherwise, we need to associate that the mapjoin op      *  to be linked to the RS work (associated with the RS-MJ pattern).      *      */
name|mapJoinWork
operator|=
name|context
operator|.
name|mapJoinWorkMap
operator|.
name|get
argument_list|(
name|mapJoinOp
argument_list|)
expr_stmt|;
name|BaseWork
name|parentWork
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|unionWorkMap
operator|.
name|containsKey
argument_list|(
name|parentRS
argument_list|)
condition|)
block|{
name|parentWork
operator|=
name|context
operator|.
name|unionWorkMap
operator|.
name|get
argument_list|(
name|parentRS
argument_list|)
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|context
operator|.
name|childToWorkMap
operator|.
name|get
argument_list|(
name|parentRS
argument_list|)
operator|.
name|size
argument_list|()
operator|==
literal|1
assert|;
name|parentWork
operator|=
name|context
operator|.
name|childToWorkMap
operator|.
name|get
argument_list|(
name|parentRS
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
comment|// set the link between mapjoin and parent vertex
name|int
name|pos
init|=
name|context
operator|.
name|mapJoinParentMap
operator|.
name|get
argument_list|(
name|mapJoinOp
argument_list|)
operator|.
name|indexOf
argument_list|(
name|parentRS
argument_list|)
decl_stmt|;
if|if
condition|(
name|pos
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Cannot find position of parent in mapjoin"
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Mapjoin "
operator|+
name|mapJoinOp
operator|+
literal|", pos: "
operator|+
name|pos
operator|+
literal|" --> "
operator|+
name|parentWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getParentToInput
argument_list|()
operator|.
name|put
argument_list|(
name|pos
argument_list|,
name|parentWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|mapJoinWork
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|BaseWork
name|myWork
range|:
name|mapJoinWork
control|)
block|{
comment|// link the work with the work associated with the reduce sink that triggered this rule
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"connecting "
operator|+
name|parentWork
operator|.
name|getName
argument_list|()
operator|+
literal|" with "
operator|+
name|myWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|tezWork
operator|.
name|connect
argument_list|(
name|parentWork
argument_list|,
name|myWork
argument_list|,
name|EdgeType
operator|.
name|BROADCAST_EDGE
argument_list|)
expr_stmt|;
name|ReduceSinkOperator
name|r
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|parentRS
operator|.
name|getConf
argument_list|()
operator|.
name|getOutputName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Cloning reduce sink for multi-child broadcast edge"
argument_list|)
expr_stmt|;
comment|// we've already set this one up. Need to clone for the next work.
name|r
operator|=
operator|(
name|ReduceSinkOperator
operator|)
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
operator|(
name|ReduceSinkDesc
operator|)
name|parentRS
operator|.
name|getConf
argument_list|()
operator|.
name|clone
argument_list|()
argument_list|,
name|parentRS
operator|.
name|getParentOperators
argument_list|()
argument_list|)
expr_stmt|;
name|context
operator|.
name|clonedReduceSinks
operator|.
name|add
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|r
operator|=
name|parentRS
expr_stmt|;
block|}
comment|// remember the output name of the reduce sink
name|r
operator|.
name|getConf
argument_list|()
operator|.
name|setOutputName
argument_list|(
name|myWork
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|context
operator|.
name|connectedReduceSinks
operator|.
name|add
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
block|}
comment|// remember in case we need to connect additional work later
name|List
argument_list|<
name|BaseWork
argument_list|>
name|linkWorkList
init|=
name|context
operator|.
name|linkOpWithWorkMap
operator|.
name|get
argument_list|(
name|mapJoinOp
argument_list|)
decl_stmt|;
if|if
condition|(
name|linkWorkList
operator|==
literal|null
condition|)
block|{
name|linkWorkList
operator|=
operator|new
name|ArrayList
argument_list|<
name|BaseWork
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|linkWorkList
operator|.
name|add
argument_list|(
name|parentWork
argument_list|)
expr_stmt|;
name|context
operator|.
name|linkOpWithWorkMap
operator|.
name|put
argument_list|(
name|mapJoinOp
argument_list|,
name|linkWorkList
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ReduceSinkOperator
argument_list|>
name|reduceSinks
init|=
name|context
operator|.
name|linkWorkWithReduceSinkMap
operator|.
name|get
argument_list|(
name|parentWork
argument_list|)
decl_stmt|;
if|if
condition|(
name|reduceSinks
operator|==
literal|null
condition|)
block|{
name|reduceSinks
operator|=
operator|new
name|ArrayList
argument_list|<
name|ReduceSinkOperator
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|reduceSinks
operator|.
name|add
argument_list|(
name|parentRS
argument_list|)
expr_stmt|;
name|context
operator|.
name|linkWorkWithReduceSinkMap
operator|.
name|put
argument_list|(
name|parentWork
argument_list|,
name|reduceSinks
argument_list|)
expr_stmt|;
comment|// create the dummy operators
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|dummyOperators
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
argument_list|()
decl_stmt|;
comment|// create an new operator: HashTableDummyOperator, which share the table desc
name|HashTableDummyDesc
name|desc
init|=
operator|new
name|HashTableDummyDesc
argument_list|()
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|HashTableDummyOperator
name|dummyOp
init|=
operator|(
name|HashTableDummyOperator
operator|)
name|OperatorFactory
operator|.
name|get
argument_list|(
name|desc
argument_list|)
decl_stmt|;
name|TableDesc
name|tbl
decl_stmt|;
comment|// need to create the correct table descriptor for key/value
name|RowSchema
name|rowSchema
init|=
name|parentRS
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getSchema
argument_list|()
decl_stmt|;
name|tbl
operator|=
name|PlanUtils
operator|.
name|getReduceValueTableDesc
argument_list|(
name|PlanUtils
operator|.
name|getFieldSchemasFromRowSchema
argument_list|(
name|rowSchema
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|dummyOp
operator|.
name|getConf
argument_list|()
operator|.
name|setTbl
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|keyExprMap
init|=
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getKeys
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyCols
init|=
name|keyExprMap
operator|.
name|get
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|StringBuffer
name|keyOrder
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|k
range|:
name|keyCols
control|)
block|{
name|keyOrder
operator|.
name|append
argument_list|(
literal|"+"
argument_list|)
expr_stmt|;
block|}
name|TableDesc
name|keyTableDesc
init|=
name|PlanUtils
operator|.
name|getReduceKeyTableDesc
argument_list|(
name|PlanUtils
operator|.
name|getFieldSchemasFromColumnList
argument_list|(
name|keyCols
argument_list|,
literal|"mapjoinkey"
argument_list|)
argument_list|,
name|keyOrder
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|mapJoinOp
operator|.
name|getConf
argument_list|()
operator|.
name|setKeyTableDesc
argument_list|(
name|keyTableDesc
argument_list|)
expr_stmt|;
comment|// let the dummy op be the parent of mapjoin op
name|mapJoinOp
operator|.
name|replaceParent
argument_list|(
name|parentRS
argument_list|,
name|dummyOp
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|dummyChildren
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
argument_list|()
decl_stmt|;
name|dummyChildren
operator|.
name|add
argument_list|(
name|mapJoinOp
argument_list|)
expr_stmt|;
name|dummyOp
operator|.
name|setChildOperators
argument_list|(
name|dummyChildren
argument_list|)
expr_stmt|;
name|dummyOperators
operator|.
name|add
argument_list|(
name|dummyOp
argument_list|)
expr_stmt|;
comment|// cut the operator tree so as to not retain connections from the parent RS downstream
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|childOperators
init|=
name|parentRS
operator|.
name|getChildOperators
argument_list|()
decl_stmt|;
name|int
name|childIndex
init|=
name|childOperators
operator|.
name|indexOf
argument_list|(
name|mapJoinOp
argument_list|)
decl_stmt|;
name|childOperators
operator|.
name|remove
argument_list|(
name|childIndex
argument_list|)
expr_stmt|;
comment|// the "work" needs to know about the dummy operators. They have to be separately initialized
comment|// at task startup
if|if
condition|(
name|mapJoinWork
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|BaseWork
name|myWork
range|:
name|mapJoinWork
control|)
block|{
name|myWork
operator|.
name|addDummyOp
argument_list|(
name|dummyOp
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|context
operator|.
name|linkChildOpWithDummyOp
operator|.
name|containsKey
argument_list|(
name|mapJoinOp
argument_list|)
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|context
operator|.
name|linkChildOpWithDummyOp
operator|.
name|get
argument_list|(
name|mapJoinOp
argument_list|)
control|)
block|{
name|dummyOperators
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
block|}
name|context
operator|.
name|linkChildOpWithDummyOp
operator|.
name|put
argument_list|(
name|mapJoinOp
argument_list|,
name|dummyOperators
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

