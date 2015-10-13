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

begin_class
specifier|public
class|class
name|LongColLessEqualLongColumn
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
name|int
name|colNum1
decl_stmt|;
specifier|private
name|int
name|colNum2
decl_stmt|;
specifier|private
name|int
name|outputColumn
decl_stmt|;
specifier|public
name|LongColLessEqualLongColumn
parameter_list|(
name|int
name|colNum1
parameter_list|,
name|int
name|colNum2
parameter_list|,
name|int
name|outputColumn
parameter_list|)
block|{
name|this
operator|.
name|colNum1
operator|=
name|colNum1
expr_stmt|;
name|this
operator|.
name|colNum2
operator|=
name|colNum2
expr_stmt|;
name|this
operator|.
name|outputColumn
operator|=
name|outputColumn
expr_stmt|;
block|}
specifier|public
name|LongColLessEqualLongColumn
parameter_list|()
block|{   }
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
name|LongColumnVector
name|inputColVector1
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colNum1
index|]
decl_stmt|;
name|LongColumnVector
name|inputColVector2
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colNum2
index|]
decl_stmt|;
name|LongColumnVector
name|outputColVector
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|outputColumn
index|]
decl_stmt|;
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
name|long
index|[]
name|vector1
init|=
name|inputColVector1
operator|.
name|vector
decl_stmt|;
name|long
index|[]
name|vector2
init|=
name|inputColVector2
operator|.
name|vector
decl_stmt|;
name|long
index|[]
name|outputVector
init|=
name|outputColVector
operator|.
name|vector
decl_stmt|;
name|long
name|vector1Value
init|=
name|vector1
index|[
literal|0
index|]
decl_stmt|;
name|long
name|vector2Value
init|=
name|vector2
index|[
literal|0
index|]
decl_stmt|;
comment|// return immediately if batch is empty
if|if
condition|(
name|n
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|outputColVector
operator|.
name|isRepeating
operator|=
name|inputColVector1
operator|.
name|isRepeating
operator|&&
name|inputColVector2
operator|.
name|isRepeating
operator|||
name|inputColVector1
operator|.
name|isRepeating
operator|&&
operator|!
name|inputColVector1
operator|.
name|noNulls
operator|&&
name|inputColVector1
operator|.
name|isNull
index|[
literal|0
index|]
operator|||
name|inputColVector2
operator|.
name|isRepeating
operator|&&
operator|!
name|inputColVector2
operator|.
name|noNulls
operator|&&
name|inputColVector2
operator|.
name|isNull
index|[
literal|0
index|]
expr_stmt|;
comment|// Handle nulls first
name|NullUtil
operator|.
name|propagateNullsColCol
argument_list|(
name|inputColVector1
argument_list|,
name|inputColVector2
argument_list|,
name|outputColVector
argument_list|,
name|sel
argument_list|,
name|n
argument_list|,
name|batch
operator|.
name|selectedInUse
argument_list|)
expr_stmt|;
comment|/* Disregard nulls for processing. In other words,      * the arithmetic operation is performed even if one or       * more inputs are null. This is to improve speed by avoiding      * conditional checks in the inner loop.      */
if|if
condition|(
name|inputColVector1
operator|.
name|isRepeating
operator|&&
name|inputColVector2
operator|.
name|isRepeating
condition|)
block|{
name|outputVector
index|[
literal|0
index|]
operator|=
name|vector1Value
operator|<=
name|vector2Value
condition|?
literal|1
else|:
literal|0
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|inputColVector1
operator|.
name|isRepeating
condition|)
block|{
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
name|j
init|=
literal|0
init|;
name|j
operator|!=
name|n
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
name|outputVector
index|[
name|i
index|]
operator|=
name|vector1Value
operator|<=
name|vector2
index|[
name|i
index|]
condition|?
literal|1
else|:
literal|0
expr_stmt|;
block|}
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
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
comment|// The SIMD optimized form of "a<= b" is "((b - a)>>> 63) ^ 1"
name|outputVector
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|vector2
index|[
name|i
index|]
operator|-
name|vector1Value
operator|)
operator|>>>
literal|63
operator|)
operator|^
literal|1
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|inputColVector2
operator|.
name|isRepeating
condition|)
block|{
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
name|j
init|=
literal|0
init|;
name|j
operator|!=
name|n
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
name|outputVector
index|[
name|i
index|]
operator|=
name|vector1
index|[
name|i
index|]
operator|<=
name|vector2Value
condition|?
literal|1
else|:
literal|0
expr_stmt|;
block|}
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
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
name|outputVector
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|vector2Value
operator|-
name|vector1
index|[
name|i
index|]
operator|)
operator|>>>
literal|63
operator|)
operator|^
literal|1
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
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
name|j
init|=
literal|0
init|;
name|j
operator|!=
name|n
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
name|outputVector
index|[
name|i
index|]
operator|=
name|vector1
index|[
name|i
index|]
operator|<=
name|vector2
index|[
name|i
index|]
condition|?
literal|1
else|:
literal|0
expr_stmt|;
block|}
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
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
name|outputVector
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|vector2
index|[
name|i
index|]
operator|-
name|vector1
index|[
name|i
index|]
operator|)
operator|>>>
literal|63
operator|)
operator|^
literal|1
expr_stmt|;
block|}
block|}
block|}
comment|/* For the case when the output can have null values, follow       * the convention that the data values must be 1 for long and       * NaN for double. This is to prevent possible later zero-divide errors      * in complex arithmetic expressions like col2 / (col1 - 1)      * in the case when some col1 entries are null.      */
name|NullUtil
operator|.
name|setNullDataEntriesLong
argument_list|(
name|outputColVector
argument_list|,
name|batch
operator|.
name|selectedInUse
argument_list|,
name|sel
argument_list|,
name|n
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getOutputColumn
parameter_list|()
block|{
return|return
name|outputColumn
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getOutputType
parameter_list|()
block|{
return|return
literal|"long"
return|;
block|}
specifier|public
name|int
name|getColNum1
parameter_list|()
block|{
return|return
name|colNum1
return|;
block|}
specifier|public
name|void
name|setColNum1
parameter_list|(
name|int
name|colNum1
parameter_list|)
block|{
name|this
operator|.
name|colNum1
operator|=
name|colNum1
expr_stmt|;
block|}
specifier|public
name|int
name|getColNum2
parameter_list|()
block|{
return|return
name|colNum2
return|;
block|}
specifier|public
name|void
name|setColNum2
parameter_list|(
name|int
name|colNum2
parameter_list|)
block|{
name|this
operator|.
name|colNum2
operator|=
name|colNum2
expr_stmt|;
block|}
specifier|public
name|void
name|setOutputColumn
parameter_list|(
name|int
name|outputColumn
parameter_list|)
block|{
name|this
operator|.
name|outputColumn
operator|=
name|outputColumn
expr_stmt|;
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
name|PROJECTION
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
name|getType
argument_list|(
literal|"int_datetime_interval_family"
argument_list|)
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|getType
argument_list|(
literal|"int_datetime_interval_family"
argument_list|)
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

