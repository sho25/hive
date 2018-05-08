begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
comment|/**  * Compute IF(expr1, expr2, expr3) for 3 input column expressions.  * The first is always a boolean (LongColumnVector).  * The second and third are long columns or long expression results.  */
end_comment

begin_class
specifier|public
class|class
name|IfExprLongColumnLongColumn
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
name|arg1Column
decl_stmt|;
specifier|private
specifier|final
name|int
name|arg2Column
decl_stmt|;
specifier|private
specifier|final
name|int
name|arg3Column
decl_stmt|;
specifier|public
name|IfExprLongColumnLongColumn
parameter_list|(
name|int
name|arg1Column
parameter_list|,
name|int
name|arg2Column
parameter_list|,
name|int
name|arg3Column
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
name|arg1Column
operator|=
name|arg1Column
expr_stmt|;
name|this
operator|.
name|arg2Column
operator|=
name|arg2Column
expr_stmt|;
name|this
operator|.
name|arg3Column
operator|=
name|arg3Column
expr_stmt|;
block|}
specifier|public
name|IfExprLongColumnLongColumn
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// Dummy final assignments.
name|arg1Column
operator|=
operator|-
literal|1
expr_stmt|;
name|arg2Column
operator|=
operator|-
literal|1
expr_stmt|;
name|arg3Column
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
name|arg1ColVector
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|arg1Column
index|]
decl_stmt|;
name|LongColumnVector
name|arg2ColVector
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|arg2Column
index|]
decl_stmt|;
name|LongColumnVector
name|arg3ColVector
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|arg3Column
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
name|outputIsNull
init|=
name|outputColVector
operator|.
name|isNull
decl_stmt|;
comment|// We do not need to do a column reset since we are carefully changing the output.
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
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
name|arg1ColVector
operator|.
name|vector
decl_stmt|;
name|long
index|[]
name|vector2
init|=
name|arg2ColVector
operator|.
name|vector
decl_stmt|;
name|long
index|[]
name|vector3
init|=
name|arg3ColVector
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
comment|/* All the code paths below propagate nulls even if neither arg2 nor arg3      * have nulls. This is to reduce the number of code paths and shorten the      * code, at the expense of maybe doing unnecessary work if neither input      * has nulls. This could be improved in the future by expanding the number      * of code paths.      */
if|if
condition|(
name|arg1ColVector
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
name|arg2ColVector
operator|.
name|copySelected
argument_list|(
name|batch
operator|.
name|selectedInUse
argument_list|,
name|sel
argument_list|,
name|n
argument_list|,
name|outputColVector
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|arg3ColVector
operator|.
name|copySelected
argument_list|(
name|batch
operator|.
name|selectedInUse
argument_list|,
name|sel
argument_list|,
name|n
argument_list|,
name|outputColVector
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
comment|// extend any repeating values and noNulls indicator in the inputs
name|arg2ColVector
operator|.
name|flatten
argument_list|(
name|batch
operator|.
name|selectedInUse
argument_list|,
name|sel
argument_list|,
name|n
argument_list|)
expr_stmt|;
name|arg3ColVector
operator|.
name|flatten
argument_list|(
name|batch
operator|.
name|selectedInUse
argument_list|,
name|sel
argument_list|,
name|n
argument_list|)
expr_stmt|;
comment|// Carefully handle NULLs...
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|arg1ColVector
operator|.
name|noNulls
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
operator|(
operator|~
operator|(
name|vector1
index|[
name|i
index|]
operator|-
literal|1
operator|)
operator|&
name|vector2
index|[
name|i
index|]
operator|)
operator||
operator|(
operator|(
name|vector1
index|[
name|i
index|]
operator|-
literal|1
operator|)
operator|&
name|vector3
index|[
name|i
index|]
operator|)
expr_stmt|;
name|outputIsNull
index|[
name|i
index|]
operator|=
operator|(
name|vector1
index|[
name|i
index|]
operator|==
literal|1
condition|?
name|arg2ColVector
operator|.
name|isNull
index|[
name|i
index|]
else|:
name|arg3ColVector
operator|.
name|isNull
index|[
name|i
index|]
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
name|outputVector
index|[
name|i
index|]
operator|=
operator|(
operator|~
operator|(
name|vector1
index|[
name|i
index|]
operator|-
literal|1
operator|)
operator|&
name|vector2
index|[
name|i
index|]
operator|)
operator||
operator|(
operator|(
name|vector1
index|[
name|i
index|]
operator|-
literal|1
operator|)
operator|&
name|vector3
index|[
name|i
index|]
operator|)
expr_stmt|;
name|outputIsNull
index|[
name|i
index|]
operator|=
operator|(
name|vector1
index|[
name|i
index|]
operator|==
literal|1
condition|?
name|arg2ColVector
operator|.
name|isNull
index|[
name|i
index|]
else|:
name|arg3ColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|)
expr_stmt|;
block|}
block|}
block|}
else|else
comment|/* there are nulls */
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
operator|(
operator|!
name|arg1ColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|&&
name|vector1
index|[
name|i
index|]
operator|==
literal|1
condition|?
name|vector2
index|[
name|i
index|]
else|:
name|vector3
index|[
name|i
index|]
operator|)
expr_stmt|;
name|outputIsNull
index|[
name|i
index|]
operator|=
operator|(
operator|!
name|arg1ColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|&&
name|vector1
index|[
name|i
index|]
operator|==
literal|1
condition|?
name|arg2ColVector
operator|.
name|isNull
index|[
name|i
index|]
else|:
name|arg3ColVector
operator|.
name|isNull
index|[
name|i
index|]
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
name|outputVector
index|[
name|i
index|]
operator|=
operator|(
operator|!
name|arg1ColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|&&
name|vector1
index|[
name|i
index|]
operator|==
literal|1
condition|?
name|vector2
index|[
name|i
index|]
else|:
name|vector3
index|[
name|i
index|]
operator|)
expr_stmt|;
name|outputIsNull
index|[
name|i
index|]
operator|=
operator|(
operator|!
name|arg1ColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|&&
name|vector1
index|[
name|i
index|]
operator|==
literal|1
condition|?
name|arg2ColVector
operator|.
name|isNull
index|[
name|i
index|]
else|:
name|arg3ColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|)
expr_stmt|;
block|}
block|}
block|}
comment|// restore repeating and no nulls indicators
name|arg2ColVector
operator|.
name|unFlatten
argument_list|()
expr_stmt|;
name|arg3ColVector
operator|.
name|unFlatten
argument_list|()
expr_stmt|;
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
name|arg1Column
argument_list|)
operator|+
literal|", "
operator|+
name|getColumnParamString
argument_list|(
literal|1
argument_list|,
name|arg2Column
argument_list|)
operator|+
literal|", "
operator|+
name|getColumnParamString
argument_list|(
literal|1
argument_list|,
name|arg3Column
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
literal|3
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
literal|"int_date_interval_year_month"
argument_list|)
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|getType
argument_list|(
literal|"int_date_interval_year_month"
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

