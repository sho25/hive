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
name|outputVector
init|=
name|batch
operator|.
name|cols
index|[
name|outputColumnNum
index|]
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
name|outputVector
operator|.
name|init
argument_list|()
expr_stmt|;
name|boolean
name|noNulls
init|=
literal|false
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
comment|// non-nulls in any column qualifies coalesce having no nulls
comment|// common case: last column is a constant& non-null
name|noNulls
operator|=
name|noNulls
operator|||
name|cv
operator|.
name|noNulls
expr_stmt|;
block|}
name|outputVector
operator|.
name|noNulls
operator|=
name|noNulls
expr_stmt|;
name|outputVector
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|ColumnVector
name|first
init|=
name|batch
operator|.
name|cols
index|[
name|inputColumns
index|[
literal|0
index|]
index|]
decl_stmt|;
if|if
condition|(
name|first
operator|.
name|noNulls
operator|&&
name|first
operator|.
name|isRepeating
condition|)
block|{
name|outputVector
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|outputVector
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|outputVector
operator|.
name|setElement
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
name|first
argument_list|)
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
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
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
operator|(
name|cv
operator|.
name|isRepeating
operator|)
operator|&&
operator|(
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
operator|)
condition|)
block|{
name|outputVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|outputVector
operator|.
name|setElement
argument_list|(
name|i
argument_list|,
literal|0
argument_list|,
name|cv
argument_list|)
expr_stmt|;
break|break;
block|}
elseif|else
if|if
condition|(
operator|(
operator|!
name|cv
operator|.
name|isRepeating
operator|)
operator|&&
operator|(
name|cv
operator|.
name|noNulls
operator|||
operator|!
name|cv
operator|.
name|isNull
index|[
name|i
index|]
operator|)
condition|)
block|{
name|outputVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|outputVector
operator|.
name|setElement
argument_list|(
name|i
argument_list|,
name|i
argument_list|,
name|cv
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
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
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
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
operator|(
name|cv
operator|.
name|isRepeating
operator|)
operator|&&
operator|(
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
operator|)
condition|)
block|{
name|outputVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|outputVector
operator|.
name|setElement
argument_list|(
name|i
argument_list|,
literal|0
argument_list|,
name|cv
argument_list|)
expr_stmt|;
break|break;
block|}
elseif|else
if|if
condition|(
operator|(
operator|!
name|cv
operator|.
name|isRepeating
operator|)
operator|&&
operator|(
name|cv
operator|.
name|noNulls
operator|||
operator|!
name|cv
operator|.
name|isNull
index|[
name|i
index|]
operator|)
condition|)
block|{
name|outputVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|outputVector
operator|.
name|setElement
argument_list|(
name|i
argument_list|,
name|i
argument_list|,
name|cv
argument_list|)
expr_stmt|;
break|break;
block|}
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

