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

begin_comment
comment|/**  * This expression selects a row if the given boolean column is false.  */
end_comment

begin_class
specifier|public
class|class
name|SelectColumnIsFalse
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
specifier|public
name|SelectColumnIsFalse
parameter_list|(
name|int
name|colNum1
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|colNum1
operator|=
name|colNum1
expr_stmt|;
block|}
specifier|public
name|SelectColumnIsFalse
parameter_list|()
block|{
name|super
argument_list|()
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
name|boolean
index|[]
name|nullVector
init|=
name|inputColVector1
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
name|inputColVector1
operator|.
name|noNulls
condition|)
block|{
if|if
condition|(
name|inputColVector1
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|vector1
index|[
literal|0
index|]
operator|==
literal|1
condition|)
block|{
comment|// All are filtered out
name|batch
operator|.
name|size
operator|=
literal|0
expr_stmt|;
return|return;
block|}
else|else
block|{
comment|// All are selected;
return|return;
block|}
block|}
elseif|else
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|int
name|newSize
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
if|if
condition|(
name|vector1
index|[
name|i
index|]
operator|==
literal|0
condition|)
block|{
name|sel
index|[
name|newSize
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
block|}
else|else
block|{
name|int
name|newSize
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
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|vector1
index|[
name|i
index|]
operator|==
literal|0
condition|)
block|{
name|sel
index|[
name|newSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
if|if
condition|(
name|newSize
operator|<
name|n
condition|)
block|{
name|batch
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
name|batch
operator|.
name|size
operator|=
name|newSize
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|inputColVector1
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|nullVector
index|[
literal|0
index|]
operator|||
operator|(
name|vector1
index|[
literal|0
index|]
operator|==
literal|1
operator|)
condition|)
block|{
comment|// All are filtered out
name|batch
operator|.
name|size
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
comment|// All are selected;
return|return;
block|}
block|}
elseif|else
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|int
name|newSize
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
if|if
condition|(
name|vector1
index|[
name|i
index|]
operator|==
literal|0
operator|&&
operator|!
name|nullVector
index|[
name|i
index|]
condition|)
block|{
name|sel
index|[
name|newSize
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
block|}
else|else
block|{
name|int
name|newSize
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
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|vector1
index|[
name|i
index|]
operator|==
literal|0
operator|&&
operator|!
name|nullVector
index|[
name|i
index|]
condition|)
block|{
name|sel
index|[
name|newSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
if|if
condition|(
name|newSize
operator|<
name|n
condition|)
block|{
name|batch
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
name|batch
operator|.
name|size
operator|=
name|newSize
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
operator|-
literal|1
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
literal|"boolean"
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
name|String
name|vectorExpressionParameters
parameter_list|()
block|{
return|return
literal|"col "
operator|+
name|colNum1
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
name|VectorExpressionDescriptor
operator|.
name|Builder
name|b
init|=
operator|new
name|VectorExpressionDescriptor
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|b
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
literal|1
argument_list|)
operator|.
name|setArgumentTypes
argument_list|(
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
argument_list|)
expr_stmt|;
return|return
name|b
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

