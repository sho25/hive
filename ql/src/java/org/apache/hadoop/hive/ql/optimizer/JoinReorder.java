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
name|HashSet
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
name|plan
operator|.
name|OperatorDesc
import|;
end_import

begin_comment
comment|/**  * Implementation of rule-based join table reordering optimization. User passes  * hints to specify which tables are to be streamed and they are moved to have  * largest tag so that they are processed last. In future, once statistics are  * implemented, this transformation can also be done based on costs.  */
end_comment

begin_class
specifier|public
class|class
name|JoinReorder
implements|implements
name|Transform
block|{
comment|/**    * Estimate the size of the output based on the STREAMTABLE hints. To do so    * the whole tree is traversed. Possible sizes: 0: the operator and its    * subtree don't contain any big tables 1: the subtree of the operator    * contains a big table 2: the operator is a big table    *    * @param operator    *          The operator which output size is to be estimated    * @param bigTables    *          Set of tables that should be streamed    * @return The estimated size - 0 (no streamed tables), 1 (streamed tables in    *         subtree) or 2 (a streamed table)    */
specifier|private
name|int
name|getOutputSize
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|operator
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|bigTables
parameter_list|)
block|{
comment|// If a join operator contains a big subtree, there is a chance that its
comment|// output is also big, so the output size is 1 (medium)
if|if
condition|(
name|operator
operator|instanceof
name|JoinOperator
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|o
range|:
name|operator
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
if|if
condition|(
name|getOutputSize
argument_list|(
name|o
argument_list|,
name|bigTables
argument_list|)
operator|!=
literal|0
condition|)
block|{
return|return
literal|1
return|;
block|}
block|}
block|}
comment|// If a table is in bigTables then its output is big (2)
if|if
condition|(
name|operator
operator|instanceof
name|TableScanOperator
condition|)
block|{
name|String
name|alias
init|=
operator|(
operator|(
name|TableScanOperator
operator|)
name|operator
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|getAlias
argument_list|()
decl_stmt|;
if|if
condition|(
name|bigTables
operator|.
name|contains
argument_list|(
name|alias
argument_list|)
condition|)
block|{
return|return
literal|2
return|;
block|}
block|}
comment|// For all other kinds of operators, assume the output is as big as the
comment|// the biggest output from a parent
name|int
name|maxSize
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|operator
operator|.
name|getParentOperators
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|o
range|:
name|operator
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
name|int
name|current
init|=
name|getOutputSize
argument_list|(
name|o
argument_list|,
name|bigTables
argument_list|)
decl_stmt|;
if|if
condition|(
name|current
operator|>
name|maxSize
condition|)
block|{
name|maxSize
operator|=
name|current
expr_stmt|;
block|}
block|}
block|}
return|return
name|maxSize
return|;
block|}
comment|/**    * Find all big tables from STREAMTABLE hints.    *    * @param joinCtx    *          The join context    * @return Set of all big tables    */
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|getBigTables
parameter_list|(
name|ParseContext
name|joinCtx
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|bigTables
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|QBJoinTree
name|qbJoin
range|:
name|joinCtx
operator|.
name|getJoinContext
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|qbJoin
operator|.
name|getStreamAliases
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|bigTables
operator|.
name|addAll
argument_list|(
name|qbJoin
operator|.
name|getStreamAliases
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|bigTables
return|;
block|}
comment|/**    * Reorder the tables in a join operator appropriately (by reordering the tags    * of the reduces sinks).    *    * @param joinOp    *          The join operator to be processed    * @param bigTables    *          Set of all big tables    */
specifier|private
name|void
name|reorder
parameter_list|(
name|JoinOperator
name|joinOp
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|bigTables
parameter_list|)
block|{
name|int
name|count
init|=
name|joinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
comment|// Find the biggest reduce sink
name|int
name|biggestPos
init|=
name|count
operator|-
literal|1
decl_stmt|;
name|int
name|biggestSize
init|=
name|getOutputSize
argument_list|(
name|joinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|biggestPos
argument_list|)
argument_list|,
name|bigTables
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|int
name|currSize
init|=
name|getOutputSize
argument_list|(
name|joinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|bigTables
argument_list|)
decl_stmt|;
if|if
condition|(
name|currSize
operator|>
name|biggestSize
condition|)
block|{
name|biggestSize
operator|=
name|currSize
expr_stmt|;
name|biggestPos
operator|=
name|i
expr_stmt|;
block|}
block|}
comment|// Reorder tags if need be
if|if
condition|(
name|biggestPos
operator|!=
operator|(
name|count
operator|-
literal|1
operator|)
condition|)
block|{
name|Byte
index|[]
name|tagOrder
init|=
name|joinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getTagOrder
argument_list|()
decl_stmt|;
name|Byte
name|temp
init|=
name|tagOrder
index|[
name|biggestPos
index|]
decl_stmt|;
name|tagOrder
index|[
name|biggestPos
index|]
operator|=
name|tagOrder
index|[
name|count
operator|-
literal|1
index|]
expr_stmt|;
name|tagOrder
index|[
name|count
operator|-
literal|1
index|]
operator|=
name|temp
expr_stmt|;
comment|// Update tags of reduce sinks
operator|(
operator|(
name|ReduceSinkOperator
operator|)
name|joinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|biggestPos
argument_list|)
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|setTag
argument_list|(
name|count
operator|-
literal|1
argument_list|)
expr_stmt|;
operator|(
operator|(
name|ReduceSinkOperator
operator|)
name|joinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|count
operator|-
literal|1
argument_list|)
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|setTag
argument_list|(
name|biggestPos
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Transform the query tree. For each join, check which reduce sink will    * output the biggest result (based on STREAMTABLE hints) and give it the    * biggest tag so that it gets streamed.    *    * @param pactx    *          current parse context    */
specifier|public
name|ParseContext
name|transform
parameter_list|(
name|ParseContext
name|pactx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|bigTables
init|=
name|getBigTables
argument_list|(
name|pactx
argument_list|)
decl_stmt|;
for|for
control|(
name|JoinOperator
name|joinOp
range|:
name|pactx
operator|.
name|getJoinContext
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|reorder
argument_list|(
name|joinOp
argument_list|,
name|bigTables
argument_list|)
expr_stmt|;
block|}
return|return
name|pactx
return|;
block|}
block|}
end_class

end_unit

