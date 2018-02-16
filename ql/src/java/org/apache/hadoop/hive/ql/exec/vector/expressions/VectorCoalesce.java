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
name|ColumnVector
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
name|LongColumnVector
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
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_comment
comment|/**  * This expression returns the value of the first non-null expression  * in the given set of inputs expressions.  */
end_comment

begin_class
specifier|public
class|class
name|VectorCoalesce
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
specifier|final
name|int
index|[]
name|inputColumns
decl_stmt|;
comment|// The unassigned batchIndex for the rows that have not received a non-NULL value yet.
comment|// A temporary work array.
specifier|private
specifier|transient
name|int
index|[]
name|unassignedBatchIndices
decl_stmt|;
specifier|public
name|VectorCoalesce
parameter_list|(
name|int
index|[]
name|inputColumns
parameter_list|,
name|int
name|outputColumnNum
parameter_list|)
block|{
name|super
argument_list|(
name|outputColumnNum
argument_list|)
expr_stmt|;
name|this
operator|.
name|inputColumns
operator|=
name|inputColumns
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|this
operator|.
name|inputColumns
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|public
name|VectorCoalesce
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// Dummy final assignments.
name|inputColumns
operator|=
literal|null
expr_stmt|;
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
block|{
if|if
condition|(
name|childExpressions
operator|!=
literal|null
condition|)
block|{
name|super
operator|.
name|evaluateChildren
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
name|int
index|[]
name|sel
init|=
name|batch
operator|.
name|selected
decl_stmt|;
name|int
name|n
init|=
name|batch
operator|.
name|size
decl_stmt|;
name|ColumnVector
name|outputColVector
init|=
name|batch
operator|.
name|cols
index|[
name|outputColumnNum
index|]
decl_stmt|;
name|boolean
index|[]
name|outputIsNull
init|=
name|outputColVector
operator|.
name|isNull
decl_stmt|;
if|if
condition|(
name|n
operator|<=
literal|0
condition|)
block|{
comment|// Nothing to do
return|return;
block|}
if|if
condition|(
name|unassignedBatchIndices
operator|==
literal|null
operator|||
name|n
operator|>
name|unassignedBatchIndices
operator|.
name|length
condition|)
block|{
comment|// (Re)allocate larger to be a multiple of 1024 (DEFAULT_SIZE).
specifier|final
name|int
name|roundUpSize
init|=
operator|(
operator|(
name|n
operator|+
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
operator|-
literal|1
operator|)
operator|/
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
operator|)
operator|*
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
decl_stmt|;
name|unassignedBatchIndices
operator|=
operator|new
name|int
index|[
name|roundUpSize
index|]
expr_stmt|;
block|}
comment|// We do not need to do a column reset since we are carefully changing the output.
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
comment|// CONSIDER: Should be do this for all vector expressions that can
comment|//           work on BytesColumnVector output columns???
name|outputColVector
operator|.
name|init
argument_list|()
expr_stmt|;
specifier|final
name|int
name|columnCount
init|=
name|inputColumns
operator|.
name|length
decl_stmt|;
comment|/*      * Process the input columns to find a non-NULL value for each row.      *      * We track the unassigned batchIndex of the rows that have not received      * a non-NULL value yet.  Similar to a selected array.      */
name|boolean
name|isAllUnassigned
init|=
literal|true
decl_stmt|;
name|int
name|unassignedColumnCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|inputColumns
operator|.
name|length
condition|;
name|k
operator|++
control|)
block|{
name|ColumnVector
name|cv
init|=
name|batch
operator|.
name|cols
index|[
name|inputColumns
index|[
name|k
index|]
index|]
decl_stmt|;
if|if
condition|(
name|cv
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|cv
operator|.
name|noNulls
operator|||
operator|!
name|cv
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
comment|/*            * With a repeating value we can finish all remaining rows.            */
if|if
condition|(
name|isAllUnassigned
condition|)
block|{
comment|// No other columns provided non-NULL values.  We can return repeated output.
name|outputIsNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|setElement
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
name|cv
argument_list|)
expr_stmt|;
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
return|return;
block|}
else|else
block|{
comment|// Some rows have already been assigned values. Assign the remaining.
comment|// We cannot use copySelected method here.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|unassignedColumnCount
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|batchIndex
init|=
name|unassignedBatchIndices
index|[
name|i
index|]
decl_stmt|;
name|outputIsNull
index|[
name|batchIndex
index|]
operator|=
literal|false
expr_stmt|;
comment|// Our input is repeating (i.e. inputColNumber = 0).
name|outputColVector
operator|.
name|setElement
argument_list|(
name|batchIndex
argument_list|,
literal|0
argument_list|,
name|cv
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
block|}
else|else
block|{
comment|// Repeated NULLs -- skip this input column.
block|}
block|}
else|else
block|{
comment|/*          * Non-repeating input column. Use any non-NULL values for unassigned rows.          */
if|if
condition|(
name|isAllUnassigned
condition|)
block|{
comment|/*            * No other columns provided non-NULL values.  We *may* be able to finish all rows            * with this input column...            */
if|if
condition|(
name|cv
operator|.
name|noNulls
condition|)
block|{
comment|// Since no NULLs, we can provide values for all rows.
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
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
specifier|final
name|int
name|batchIndex
init|=
name|sel
index|[
name|i
index|]
decl_stmt|;
name|outputIsNull
index|[
name|batchIndex
index|]
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|setElement
argument_list|(
name|batchIndex
argument_list|,
name|batchIndex
argument_list|,
name|cv
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|outputIsNull
argument_list|,
literal|0
argument_list|,
name|n
argument_list|,
literal|false
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|batchIndex
init|=
literal|0
init|;
name|batchIndex
operator|<
name|n
condition|;
name|batchIndex
operator|++
control|)
block|{
name|outputColVector
operator|.
name|setElement
argument_list|(
name|batchIndex
argument_list|,
name|batchIndex
argument_list|,
name|cv
argument_list|)
expr_stmt|;
block|}
block|}
return|return;
block|}
else|else
block|{
comment|// We might not be able to assign all rows because of input NULLs.  Start tracking any
comment|// unassigned rows.
name|boolean
index|[]
name|inputIsNull
init|=
name|cv
operator|.
name|isNull
decl_stmt|;
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
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
specifier|final
name|int
name|batchIndex
init|=
name|sel
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|inputIsNull
index|[
name|batchIndex
index|]
condition|)
block|{
name|outputIsNull
index|[
name|batchIndex
index|]
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|setElement
argument_list|(
name|batchIndex
argument_list|,
name|batchIndex
argument_list|,
name|cv
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|unassignedBatchIndices
index|[
name|unassignedColumnCount
operator|++
index|]
operator|=
name|batchIndex
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|batchIndex
init|=
literal|0
init|;
name|batchIndex
operator|<
name|n
condition|;
name|batchIndex
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|inputIsNull
index|[
name|batchIndex
index|]
condition|)
block|{
name|outputIsNull
index|[
name|batchIndex
index|]
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|setElement
argument_list|(
name|batchIndex
argument_list|,
name|batchIndex
argument_list|,
name|cv
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|unassignedBatchIndices
index|[
name|unassignedColumnCount
operator|++
index|]
operator|=
name|batchIndex
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|unassignedColumnCount
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|isAllUnassigned
operator|=
literal|false
expr_stmt|;
block|}
block|}
else|else
block|{
comment|/*            * We previously assigned *some* rows with non-NULL values. The batch indices of            * the unassigned row were tracked.            */
if|if
condition|(
name|cv
operator|.
name|noNulls
condition|)
block|{
comment|// Assign all remaining rows.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|unassignedColumnCount
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|batchIndex
init|=
name|unassignedBatchIndices
index|[
name|i
index|]
decl_stmt|;
name|outputIsNull
index|[
name|batchIndex
index|]
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|setElement
argument_list|(
name|batchIndex
argument_list|,
name|batchIndex
argument_list|,
name|cv
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
else|else
block|{
comment|// Use any non-NULL values found; remember the remaining unassigned.
name|boolean
index|[]
name|inputIsNull
init|=
name|cv
operator|.
name|isNull
decl_stmt|;
name|int
name|newUnassignedColumnCount
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
name|unassignedColumnCount
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|batchIndex
init|=
name|unassignedBatchIndices
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|inputIsNull
index|[
name|batchIndex
index|]
condition|)
block|{
name|outputIsNull
index|[
name|batchIndex
index|]
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|setElement
argument_list|(
name|batchIndex
argument_list|,
name|batchIndex
argument_list|,
name|cv
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|unassignedBatchIndices
index|[
name|newUnassignedColumnCount
operator|++
index|]
operator|=
name|batchIndex
expr_stmt|;
block|}
block|}
if|if
condition|(
name|newUnassignedColumnCount
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|unassignedColumnCount
operator|=
name|newUnassignedColumnCount
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// NULL out the remaining columns.
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
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
name|unassignedColumnCount
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|batchIndex
init|=
name|unassignedBatchIndices
index|[
name|i
index|]
decl_stmt|;
name|outputIsNull
index|[
name|batchIndex
index|]
operator|=
literal|true
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
return|return
literal|"columns "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|inputColumns
argument_list|)
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
comment|// Descriptor is not defined because it takes variable number of arguments with different
comment|// data types.
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Undefined descriptor"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

