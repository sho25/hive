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
name|VectorExpressionDescriptor
import|;
end_import

begin_class
specifier|public
class|class
name|LongScalarGreaterLongColumn
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
name|colNum
decl_stmt|;
specifier|private
name|long
name|value
decl_stmt|;
specifier|public
name|LongScalarGreaterLongColumn
parameter_list|(
name|long
name|value
parameter_list|,
name|int
name|colNum
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
name|colNum
operator|=
name|colNum
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|LongScalarGreaterLongColumn
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// Dummy final assignments.
name|colNum
operator|=
operator|-
literal|1
expr_stmt|;
name|value
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
name|inputColVector
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colNum
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
name|boolean
index|[]
name|nullPos
init|=
name|inputColVector
operator|.
name|isNull
decl_stmt|;
name|boolean
index|[]
name|outNulls
init|=
name|outputColVector
operator|.
name|isNull
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
name|vector
init|=
name|inputColVector
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
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|noNulls
operator|=
name|inputColVector
operator|.
name|noNulls
expr_stmt|;
if|if
condition|(
name|inputColVector
operator|.
name|noNulls
condition|)
block|{
if|if
condition|(
name|inputColVector
operator|.
name|isRepeating
condition|)
block|{
comment|//All must be selected otherwise size would be zero
comment|//Repeating property will not change.
name|outputVector
index|[
literal|0
index|]
operator|=
name|value
operator|>
name|vector
index|[
literal|0
index|]
condition|?
literal|1
else|:
literal|0
expr_stmt|;
name|outputColVector
operator|.
name|isRepeating
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
name|value
operator|>
name|vector
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
comment|// The SIMD optimized form of "a> b" is "(b - a)>>> 63"
name|outputVector
index|[
name|i
index|]
operator|=
operator|(
name|vector
index|[
name|i
index|]
operator|-
name|value
operator|)
operator|>>>
literal|63
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|inputColVector
operator|.
name|isRepeating
condition|)
block|{
comment|//All must be selected otherwise size would be zero
comment|//Repeating property will not change.
if|if
condition|(
operator|!
name|nullPos
index|[
literal|0
index|]
condition|)
block|{
name|outputVector
index|[
literal|0
index|]
operator|=
name|value
operator|>
name|vector
index|[
literal|0
index|]
condition|?
literal|1
else|:
literal|0
expr_stmt|;
name|outNulls
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|outNulls
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
block|}
name|outputColVector
operator|.
name|isRepeating
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
name|value
operator|>
name|vector
index|[
name|i
index|]
condition|?
literal|1
else|:
literal|0
expr_stmt|;
name|outNulls
index|[
name|i
index|]
operator|=
name|nullPos
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
else|else
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|nullPos
argument_list|,
literal|0
argument_list|,
name|outNulls
argument_list|,
literal|0
argument_list|,
name|n
argument_list|)
expr_stmt|;
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
name|vector
index|[
name|i
index|]
operator|-
name|value
operator|)
operator|>>>
literal|63
expr_stmt|;
block|}
block|}
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
literal|"val "
operator|+
name|value
operator|+
literal|", "
operator|+
name|getColumnParamString
argument_list|(
literal|0
argument_list|,
name|colNum
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
name|getType
argument_list|(
literal|"long"
argument_list|)
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|getType
argument_list|(
literal|"long"
argument_list|)
argument_list|)
operator|.
name|setInputExpressionTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|SCALAR
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

