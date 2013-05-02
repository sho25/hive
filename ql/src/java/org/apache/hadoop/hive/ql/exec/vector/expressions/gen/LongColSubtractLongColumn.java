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
operator|.
name|gen
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
name|*
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
name|LongColSubtractLongColumn
extends|extends
name|VectorExpression
block|{
name|int
name|colNum1
decl_stmt|;
name|int
name|colNum2
decl_stmt|;
name|int
name|outputColumn
decl_stmt|;
specifier|public
name|LongColSubtractLongColumn
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
comment|/* Set repeating property to false (the default).      * It will be set to true later if needed later.      */
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
comment|//Handle nulls first
if|if
condition|(
name|inputColVector1
operator|.
name|noNulls
operator|&&
operator|!
name|inputColVector2
operator|.
name|noNulls
condition|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|inputColVector2
operator|.
name|isRepeating
condition|)
block|{
comment|//Output will also be repeating and null
name|outputColVector
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
comment|//return as no further processing is needed
return|return;
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
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputColVector2
operator|.
name|isNull
index|[
name|i
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
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputColVector2
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|inputColVector1
operator|.
name|noNulls
operator|&&
name|inputColVector2
operator|.
name|noNulls
condition|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|inputColVector1
operator|.
name|isRepeating
condition|)
block|{
comment|//Output will also be repeating and null
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
comment|//return as no further processing is needed
return|return;
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
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputColVector1
operator|.
name|isNull
index|[
name|i
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
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputColVector1
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|inputColVector1
operator|.
name|noNulls
operator|&&
operator|!
name|inputColVector2
operator|.
name|noNulls
condition|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|inputColVector1
operator|.
name|isRepeating
operator|||
name|inputColVector2
operator|.
name|isRepeating
condition|)
block|{
comment|//Output will also be repeating and null
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
comment|//return as no further processing is needed
return|return;
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
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputColVector1
operator|.
name|isNull
index|[
name|i
index|]
operator|||
name|inputColVector2
operator|.
name|isNull
index|[
name|i
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
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputColVector1
operator|.
name|isNull
index|[
name|i
index|]
operator|||
name|inputColVector2
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
block|}
block|}
comment|//Disregard nulls for processing
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
comment|//All must be selected otherwise size would be zero
comment|//Repeating property will not change.
name|outputVector
index|[
literal|0
index|]
operator|=
name|vector1
index|[
literal|0
index|]
operator|-
name|vector2
index|[
literal|0
index|]
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
name|vector1
index|[
literal|0
index|]
operator|-
name|vector2
index|[
name|i
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
literal|0
index|]
operator|-
name|vector2
index|[
name|i
index|]
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
operator|-
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
operator|-
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
name|outputVector
index|[
name|i
index|]
operator|=
name|vector1
index|[
name|i
index|]
operator|-
name|vector2
index|[
name|i
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
operator|-
name|vector2
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
block|}
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
block|}
end_class

end_unit

