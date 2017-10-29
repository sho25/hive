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
name|exec
operator|.
name|vector
operator|.
name|mapjoin
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|CompilationOpContext
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
name|JoinUtil
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
name|vector
operator|.
name|VectorizationContext
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
name|vector
operator|.
name|VectorizedRowBatch
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
name|vector
operator|.
name|expressions
operator|.
name|VectorExpression
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
name|vector
operator|.
name|mapjoin
operator|.
name|hashtable
operator|.
name|VectorMapJoinHashSet
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
name|vector
operator|.
name|mapjoin
operator|.
name|hashtable
operator|.
name|VectorMapJoinHashSetResult
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
name|vector
operator|.
name|mapjoin
operator|.
name|hashtable
operator|.
name|VectorMapJoinHashTableResult
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
name|metadata
operator|.
name|HiveException
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
name|VectorDesc
import|;
end_import

begin_comment
comment|/**  * This class has methods for generating vectorized join results for left semi joins.  *  * The big difference between inner joins and left semi joins is existence testing.  *  * Inner joins use a hash map to lookup the 1 or more small table values.  *  * Left semi joins are a specialized join for outputting big table rows whose key exists  * in the small table.  *  * No small table values are needed for left semi join since they would be empty.  So,  * we use a hash set as the hash table.  Hash sets just report whether a key exists.  This  * is a big performance optimization.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VectorMapJoinLeftSemiGenerateResultOperator
extends|extends
name|VectorMapJoinGenerateResultOperator
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
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
name|VectorMapJoinLeftSemiGenerateResultOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|//---------------------------------------------------------------------------
comment|// Semi join specific members.
comment|//
comment|// An array of hash set results so we can do lookups on the whole batch before output result
comment|// generation.
specifier|protected
specifier|transient
name|VectorMapJoinHashSetResult
name|hashSetResults
index|[]
decl_stmt|;
comment|// Pre-allocated member for storing the (physical) batch index of matching row (single- or
comment|// multi-small-table-valued) indexes during a process call.
specifier|protected
specifier|transient
name|int
index|[]
name|allMatchs
decl_stmt|;
comment|// Pre-allocated member for storing the (physical) batch index of rows that need to be spilled.
specifier|protected
specifier|transient
name|int
index|[]
name|spills
decl_stmt|;
comment|// Pre-allocated member for storing index into the hashSetResults for each spilled row.
specifier|protected
specifier|transient
name|int
index|[]
name|spillHashMapResultIndices
decl_stmt|;
comment|/** Kryo ctor. */
specifier|protected
name|VectorMapJoinLeftSemiGenerateResultOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorMapJoinLeftSemiGenerateResultOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
specifier|public
name|VectorMapJoinLeftSemiGenerateResultOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|,
name|OperatorDesc
name|conf
parameter_list|,
name|VectorizationContext
name|vContext
parameter_list|,
name|VectorDesc
name|vectorDesc
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
argument_list|(
name|ctx
argument_list|,
name|conf
argument_list|,
name|vContext
argument_list|,
name|vectorDesc
argument_list|)
expr_stmt|;
block|}
comment|/*    * Setup our left semi join specific members.    */
specifier|protected
name|void
name|commonSetup
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|commonSetup
argument_list|(
name|batch
argument_list|)
expr_stmt|;
comment|// Semi join specific.
name|VectorMapJoinHashSet
name|baseHashSet
init|=
operator|(
name|VectorMapJoinHashSet
operator|)
name|vectorMapJoinHashTable
decl_stmt|;
name|hashSetResults
operator|=
operator|new
name|VectorMapJoinHashSetResult
index|[
name|batch
operator|.
name|DEFAULT_SIZE
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|hashSetResults
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|hashSetResults
index|[
name|i
index|]
operator|=
name|baseHashSet
operator|.
name|createHashSetResult
argument_list|()
expr_stmt|;
block|}
name|allMatchs
operator|=
operator|new
name|int
index|[
name|batch
operator|.
name|DEFAULT_SIZE
index|]
expr_stmt|;
name|spills
operator|=
operator|new
name|int
index|[
name|batch
operator|.
name|DEFAULT_SIZE
index|]
expr_stmt|;
name|spillHashMapResultIndices
operator|=
operator|new
name|int
index|[
name|batch
operator|.
name|DEFAULT_SIZE
index|]
expr_stmt|;
block|}
comment|//-----------------------------------------------------------------------------------------------
comment|/*    * Left semi join (hash set).    */
comment|/**    * Generate the left semi join output results for one vectorized row batch.    *    * @param batch    *          The big table batch with any matching and any non matching rows both as    *          selected in use.    * @param allMatchCount    *          Number of matches in allMatchs.    * @param spillCount    *          Number of spills in spills.    * @param hashTableResults    *          The array of all hash table results for the batch. We need the    *          VectorMapJoinHashTableResult for the spill information.    */
specifier|protected
name|void
name|finishLeftSemi
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|,
name|int
name|allMatchCount
parameter_list|,
name|int
name|spillCount
parameter_list|,
name|VectorMapJoinHashTableResult
index|[]
name|hashTableResults
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
comment|// Get rid of spills before we start modifying the batch.
if|if
condition|(
name|spillCount
operator|>
literal|0
condition|)
block|{
name|spillHashMapBatch
argument_list|(
name|batch
argument_list|,
name|hashTableResults
argument_list|,
name|spills
argument_list|,
name|spillHashMapResultIndices
argument_list|,
name|spillCount
argument_list|)
expr_stmt|;
block|}
comment|/*      * Optimize by running value expressions only over the matched rows.      */
if|if
condition|(
name|allMatchCount
operator|>
literal|0
operator|&&
name|bigTableValueExpressions
operator|!=
literal|null
condition|)
block|{
name|performValueExpressions
argument_list|(
name|batch
argument_list|,
name|allMatchs
argument_list|,
name|allMatchCount
argument_list|)
expr_stmt|;
block|}
name|int
name|numSel
init|=
name|generateHashSetResults
argument_list|(
name|batch
argument_list|,
name|allMatchs
argument_list|,
name|allMatchCount
argument_list|)
decl_stmt|;
name|batch
operator|.
name|size
operator|=
name|numSel
expr_stmt|;
name|batch
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * Generate the matching left semi join output results of a vectorized row batch.    *    * @param batch    *          The big table batch.    * @param allMatchs    *          A subset of the rows of the batch that are matches.    * @param allMatchCount    *          Number of matches in allMatchs.    */
specifier|private
name|int
name|generateHashSetResults
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|,
name|int
index|[]
name|allMatchs
parameter_list|,
name|int
name|allMatchCount
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|int
name|numSel
init|=
literal|0
decl_stmt|;
comment|// Generate result within big table batch itself.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|allMatchCount
condition|;
name|i
operator|++
control|)
block|{
name|int
name|batchIndex
init|=
name|allMatchs
index|[
name|i
index|]
decl_stmt|;
comment|// Use the big table row as output.
name|batch
operator|.
name|selected
index|[
name|numSel
operator|++
index|]
operator|=
name|batchIndex
expr_stmt|;
block|}
return|return
name|numSel
return|;
block|}
comment|/**    * Generate the left semi join output results for one vectorized row batch with a repeated key.    *    * @param batch    *          The big table batch whose repeated key matches.    */
specifier|protected
name|int
name|generateHashSetResultRepeatedAll
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
comment|// The selected array is already filled in as we want it.
block|}
else|else
block|{
name|int
index|[]
name|selected
init|=
name|batch
operator|.
name|selected
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
name|batch
operator|.
name|size
condition|;
name|i
operator|++
control|)
block|{
name|selected
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
block|}
name|batch
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|batch
operator|.
name|size
return|;
block|}
specifier|protected
name|void
name|finishLeftSemiRepeated
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|,
name|JoinUtil
operator|.
name|JoinResult
name|joinResult
parameter_list|,
name|VectorMapJoinHashTableResult
name|hashSetResult
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
switch|switch
condition|(
name|joinResult
condition|)
block|{
case|case
name|MATCH
case|:
if|if
condition|(
name|bigTableValueExpressions
operator|!=
literal|null
condition|)
block|{
comment|// Run our value expressions over whole batch.
for|for
control|(
name|VectorExpression
name|ve
range|:
name|bigTableValueExpressions
control|)
block|{
name|ve
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Generate special repeated case.
name|int
name|numSel
init|=
name|generateHashSetResultRepeatedAll
argument_list|(
name|batch
argument_list|)
decl_stmt|;
name|batch
operator|.
name|size
operator|=
name|numSel
expr_stmt|;
name|batch
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
break|break;
case|case
name|SPILL
case|:
comment|// Whole batch is spilled.
name|spillBatchRepeated
argument_list|(
name|batch
argument_list|,
operator|(
name|VectorMapJoinHashTableResult
operator|)
name|hashSetResult
argument_list|)
expr_stmt|;
name|batch
operator|.
name|size
operator|=
literal|0
expr_stmt|;
break|break;
case|case
name|NOMATCH
case|:
comment|// No match for entire batch.
name|batch
operator|.
name|size
operator|=
literal|0
expr_stmt|;
break|break;
block|}
block|}
block|}
end_class

end_unit

