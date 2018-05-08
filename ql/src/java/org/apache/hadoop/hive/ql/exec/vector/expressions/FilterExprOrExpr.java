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
name|exec
operator|.
name|vector
operator|.
name|expressions
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|VectorExpressionDescriptor
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
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_comment
comment|/**  * This class represents an Or expression. This applies short circuit optimization.  */
end_comment

begin_class
specifier|public
class|class
name|FilterExprOrExpr
extends|extends
name|VectorExpression
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
specifier|transient
specifier|final
name|int
index|[]
name|initialSelected
init|=
operator|new
name|int
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
decl_stmt|;
specifier|private
specifier|transient
name|int
index|[]
name|unselected
init|=
operator|new
name|int
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
decl_stmt|;
specifier|private
specifier|transient
name|int
index|[]
name|unselectedCopy
init|=
operator|new
name|int
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
decl_stmt|;
specifier|private
specifier|transient
name|int
index|[]
name|difference
init|=
operator|new
name|int
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
decl_stmt|;
specifier|private
specifier|transient
specifier|final
name|int
index|[]
name|tmp
init|=
operator|new
name|int
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
decl_stmt|;
specifier|public
name|FilterExprOrExpr
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Remove (subtract) members from an array and produce the results into    * a difference array.     * @param all    *          The selected array containing all members.    * @param allSize    *          The size of all.    * @param remove    *          The indices to remove.  They must all be present in input selected array.    * @param removeSize    *          The size of remove.    * @param difference    *          The resulting difference -- the all array indices not in the    *          remove array.    * @return    *          The resulting size of the difference array.    */
specifier|private
name|int
name|subtract
parameter_list|(
name|int
index|[]
name|all
parameter_list|,
name|int
name|allSize
parameter_list|,
name|int
index|[]
name|remove
parameter_list|,
name|int
name|removeSize
parameter_list|,
name|int
index|[]
name|difference
parameter_list|)
block|{
comment|// UNDONE: Copied from VectorMapJoinOuterGenerateResultOperator.
name|Preconditions
operator|.
name|checkState
argument_list|(
operator|(
name|all
operator|!=
name|remove
operator|)
operator|&&
operator|(
name|remove
operator|!=
name|difference
operator|)
operator|&&
operator|(
name|difference
operator|!=
name|all
operator|)
argument_list|)
expr_stmt|;
comment|// Comment out these checks when we are happy..
if|if
condition|(
operator|!
name|verifyMonotonicallyIncreasing
argument_list|(
name|all
argument_list|,
name|allSize
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"all is not in sort order and unique"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|verifyMonotonicallyIncreasing
argument_list|(
name|remove
argument_list|,
name|removeSize
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"remove is not in sort order and unique"
argument_list|)
throw|;
block|}
name|int
name|differenceCount
init|=
literal|0
decl_stmt|;
comment|// Determine which rows are left.
name|int
name|removeIndex
init|=
literal|0
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
name|allSize
condition|;
name|i
operator|++
control|)
block|{
name|int
name|candidateIndex
init|=
name|all
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|removeIndex
operator|<
name|removeSize
operator|&&
name|candidateIndex
operator|==
name|remove
index|[
name|removeIndex
index|]
condition|)
block|{
name|removeIndex
operator|++
expr_stmt|;
block|}
else|else
block|{
name|difference
index|[
name|differenceCount
operator|++
index|]
operator|=
name|candidateIndex
expr_stmt|;
block|}
block|}
if|if
condition|(
name|removeIndex
operator|!=
name|removeSize
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not all batch indices removed"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|verifyMonotonicallyIncreasing
argument_list|(
name|difference
argument_list|,
name|differenceCount
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"difference is not in sort order and unique"
argument_list|)
throw|;
block|}
return|return
name|differenceCount
return|;
block|}
specifier|public
name|boolean
name|verifyMonotonicallyIncreasing
parameter_list|(
name|int
index|[]
name|selected
parameter_list|,
name|int
name|size
parameter_list|)
block|{
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
name|int
name|prevBatchIndex
init|=
name|selected
index|[
literal|0
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|int
name|batchIndex
init|=
name|selected
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|batchIndex
operator|<=
name|prevBatchIndex
condition|)
block|{
return|return
literal|false
return|;
block|}
name|prevBatchIndex
operator|=
name|batchIndex
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|evaluate
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
throws|throws
name|HiveException
block|{
name|int
name|n
init|=
name|batch
operator|.
name|size
decl_stmt|;
if|if
condition|(
name|n
operator|<=
literal|0
condition|)
block|{
return|return;
block|}
name|VectorExpression
name|childExpr1
init|=
name|this
operator|.
name|childExpressions
index|[
literal|0
index|]
decl_stmt|;
name|boolean
name|prevSelectInUse
init|=
name|batch
operator|.
name|selectedInUse
decl_stmt|;
comment|// Save the original selected vector
name|int
index|[]
name|sel
init|=
name|batch
operator|.
name|selected
decl_stmt|;
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|sel
argument_list|,
literal|0
argument_list|,
name|initialSelected
argument_list|,
literal|0
argument_list|,
name|n
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|initialSelected
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
name|sel
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
name|childExpr1
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
comment|// Preserve the selected reference and size values generated
comment|// after the first child is evaluated.
name|int
name|sizeAfterFirstChild
init|=
name|batch
operator|.
name|size
decl_stmt|;
name|int
index|[]
name|selectedAfterFirstChild
init|=
name|batch
operator|.
name|selected
decl_stmt|;
comment|// Calculate unselected ones in last evaluate.
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|n
condition|;
name|j
operator|++
control|)
block|{
name|tmp
index|[
name|initialSelected
index|[
name|j
index|]
index|]
operator|=
literal|0
expr_stmt|;
block|}
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|batch
operator|.
name|size
condition|;
name|j
operator|++
control|)
block|{
name|tmp
index|[
name|selectedAfterFirstChild
index|[
name|j
index|]
index|]
operator|=
literal|1
expr_stmt|;
block|}
name|int
name|unselectedSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|n
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|initialSelected
index|[
name|j
index|]
decl_stmt|;
if|if
condition|(
name|tmp
index|[
name|i
index|]
operator|==
literal|0
condition|)
block|{
name|unselected
index|[
name|unselectedSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
name|int
name|newSize
init|=
name|sizeAfterFirstChild
decl_stmt|;
name|batch
operator|.
name|selected
operator|=
name|unselected
expr_stmt|;
name|batch
operator|.
name|size
operator|=
name|unselectedSize
expr_stmt|;
if|if
condition|(
name|unselectedSize
operator|>
literal|0
condition|)
block|{
comment|// Evaluate subsequent child expression over unselected ones only.
specifier|final
name|int
name|childrenCount
init|=
name|this
operator|.
name|childExpressions
operator|.
name|length
decl_stmt|;
name|int
name|childIndex
init|=
literal|1
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|boolean
name|isLastChild
init|=
operator|(
name|childIndex
operator|+
literal|1
operator|>=
name|childrenCount
operator|)
decl_stmt|;
comment|// When we have yet another child beyond the current one... save unselected.
if|if
condition|(
operator|!
name|isLastChild
condition|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|batch
operator|.
name|selected
argument_list|,
literal|0
argument_list|,
name|unselectedCopy
argument_list|,
literal|0
argument_list|,
name|unselectedSize
argument_list|)
expr_stmt|;
block|}
name|VectorExpression
name|childExpr
init|=
name|this
operator|.
name|childExpressions
index|[
name|childIndex
index|]
decl_stmt|;
name|childExpr
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
comment|// Merge the result of last evaluate to previous evaluate.
name|newSize
operator|+=
name|batch
operator|.
name|size
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
name|batch
operator|.
name|size
condition|;
name|i
operator|++
control|)
block|{
name|tmp
index|[
name|batch
operator|.
name|selected
index|[
name|i
index|]
index|]
operator|=
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|isLastChild
condition|)
block|{
break|break;
block|}
name|unselectedSize
operator|=
name|subtract
argument_list|(
name|unselectedCopy
argument_list|,
name|unselectedSize
argument_list|,
name|batch
operator|.
name|selected
argument_list|,
name|batch
operator|.
name|size
argument_list|,
name|difference
argument_list|)
expr_stmt|;
if|if
condition|(
name|unselectedSize
operator|==
literal|0
condition|)
block|{
break|break;
block|}
name|System
operator|.
name|arraycopy
argument_list|(
name|difference
argument_list|,
literal|0
argument_list|,
name|batch
operator|.
name|selected
argument_list|,
literal|0
argument_list|,
name|unselectedSize
argument_list|)
expr_stmt|;
name|batch
operator|.
name|size
operator|=
name|unselectedSize
expr_stmt|;
name|childIndex
operator|++
expr_stmt|;
block|}
block|}
comment|// Important: Restore the batch's selected array.
name|batch
operator|.
name|selected
operator|=
name|selectedAfterFirstChild
expr_stmt|;
name|int
name|k
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|n
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|initialSelected
index|[
name|j
index|]
decl_stmt|;
if|if
condition|(
name|tmp
index|[
name|i
index|]
operator|==
literal|1
condition|)
block|{
name|batch
operator|.
name|selected
index|[
name|k
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
name|batch
operator|.
name|size
operator|=
name|newSize
expr_stmt|;
if|if
condition|(
name|newSize
operator|==
name|n
condition|)
block|{
comment|// Filter didn't do anything
name|batch
operator|.
name|selectedInUse
operator|=
name|prevSelectInUse
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|vectorExpressionParameters
parameter_list|()
block|{
comment|// The children are input.
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|VectorExpressionDescriptor
operator|.
name|Descriptor
name|getDescriptor
parameter_list|()
block|{
comment|// IMPORTANT NOTE: For Multi-OR, the VectorizationContext class will catch cases with 3 or
comment|//                 more parameters...
return|return
operator|(
operator|new
name|VectorExpressionDescriptor
operator|.
name|Builder
argument_list|()
operator|)
operator|.
name|setMode
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|Mode
operator|.
name|FILTER
argument_list|)
operator|.
name|setNumArguments
argument_list|(
literal|2
argument_list|)
operator|.
name|setArgumentTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|INT_FAMILY
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|INT_FAMILY
argument_list|)
operator|.
name|setInputExpressionTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|COLUMN
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|COLUMN
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

