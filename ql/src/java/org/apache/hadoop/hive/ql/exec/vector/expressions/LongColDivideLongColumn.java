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
name|DoubleColumnVector
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
comment|/**  * This operation is handled as a special case because Hive  * long/long division returns double. This file is thus not generated  * from a template like the other arithmetic operations are.  */
end_comment

begin_class
specifier|public
class|class
name|LongColDivideLongColumn
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
name|colNum1
decl_stmt|;
specifier|private
specifier|final
name|int
name|colNum2
decl_stmt|;
specifier|public
name|LongColDivideLongColumn
parameter_list|(
name|int
name|colNum1
parameter_list|,
name|int
name|colNum2
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
block|}
specifier|public
name|LongColDivideLongColumn
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// Dummy final assignments.
name|colNum1
operator|=
operator|-
literal|1
expr_stmt|;
name|colNum2
operator|=
operator|-
literal|1
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
throws|throws
name|HiveException
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
name|DoubleColumnVector
name|outputColVector
init|=
operator|(
name|DoubleColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|outputColumnNum
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
name|double
index|[]
name|outputVector
init|=
name|outputColVector
operator|.
name|vector
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
comment|/*      * Propagate null values for a two-input operator and set isRepeating and noNulls appropriately.      */
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
comment|/* Disregard nulls for processing. In other words,      * the arithmetic operation is performed even if one or      * more inputs are null. This is to improve speed by avoiding      * conditional checks in the inner loop.      */
name|boolean
name|hasDivBy0
init|=
literal|false
decl_stmt|;
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
name|long
name|denom
init|=
name|vector2
index|[
literal|0
index|]
decl_stmt|;
name|outputVector
index|[
literal|0
index|]
operator|=
name|vector1
index|[
literal|0
index|]
operator|/
operator|(
name|double
operator|)
name|denom
expr_stmt|;
name|hasDivBy0
operator|=
name|hasDivBy0
operator|||
operator|(
name|denom
operator|==
literal|0
operator|)
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
name|long
name|denom
init|=
name|vector2
index|[
name|i
index|]
decl_stmt|;
name|outputVector
index|[
name|i
index|]
operator|=
name|vector1
index|[
literal|0
index|]
operator|/
operator|(
name|double
operator|)
name|denom
expr_stmt|;
name|hasDivBy0
operator|=
name|hasDivBy0
operator|||
operator|(
name|denom
operator|==
literal|0
operator|)
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
name|long
name|denom
init|=
name|vector2
index|[
name|i
index|]
decl_stmt|;
name|outputVector
index|[
name|i
index|]
operator|=
name|vector1
index|[
literal|0
index|]
operator|/
operator|(
name|double
operator|)
name|denom
expr_stmt|;
name|hasDivBy0
operator|=
name|hasDivBy0
operator|||
operator|(
name|denom
operator|==
literal|0
operator|)
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
name|vector2
index|[
literal|0
index|]
operator|==
literal|0
condition|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
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
operator|/
operator|(
name|double
operator|)
name|vector2
index|[
literal|0
index|]
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
name|vector1
index|[
name|i
index|]
operator|/
operator|(
name|double
operator|)
name|vector2
index|[
literal|0
index|]
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
name|long
name|denom
init|=
name|vector2
index|[
name|i
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
operator|/
operator|(
name|double
operator|)
name|denom
expr_stmt|;
name|hasDivBy0
operator|=
name|hasDivBy0
operator|||
operator|(
name|denom
operator|==
literal|0
operator|)
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
name|long
name|denom
init|=
name|vector2
index|[
name|i
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
operator|/
operator|(
name|double
operator|)
name|denom
expr_stmt|;
name|hasDivBy0
operator|=
name|hasDivBy0
operator|||
operator|(
name|denom
operator|==
literal|0
operator|)
expr_stmt|;
block|}
block|}
block|}
comment|/* For the case when the output can have null values, follow      * the convention that the data values must be 1 for long and      * NaN for double. This is to prevent possible later zero-divide errors      * in complex arithmetic expressions like col2 / (col1 - 1)      * in the case when some col1 entries are null.      */
if|if
condition|(
operator|!
name|hasDivBy0
condition|)
block|{
name|NullUtil
operator|.
name|setNullDataEntriesDouble
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
else|else
block|{
name|NullUtil
operator|.
name|setNullAndDivBy0DataEntriesDouble
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
argument_list|,
name|inputColVector2
argument_list|)
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
name|getColumnParamString
argument_list|(
literal|0
argument_list|,
name|colNum1
argument_list|)
operator|+
literal|", "
operator|+
name|getColumnParamString
argument_list|(
literal|1
argument_list|,
name|colNum2
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

