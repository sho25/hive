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
name|java
operator|.
name|util
operator|.
name|Arrays
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

begin_comment
comment|/**  * Output a boolean value indicating if a column is IN a list of constants.  */
end_comment

begin_class
specifier|public
class|class
name|DoubleColumnInList
extends|extends
name|VectorExpression
implements|implements
name|IDoubleInExpr
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
name|double
index|[]
name|inListValues
decl_stmt|;
comment|// The set object containing the IN list. This is optimized for lookup
comment|// of the data type of the column.
specifier|private
specifier|transient
name|CuckooSetDouble
name|inSet
decl_stmt|;
specifier|public
name|DoubleColumnInList
parameter_list|(
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
block|}
specifier|public
name|DoubleColumnInList
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|inSet
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
if|if
condition|(
name|inSet
operator|==
literal|null
condition|)
block|{
name|inSet
operator|=
operator|new
name|CuckooSetDouble
argument_list|(
name|inListValues
operator|.
name|length
argument_list|)
expr_stmt|;
name|inSet
operator|.
name|load
argument_list|(
name|inListValues
argument_list|)
expr_stmt|;
block|}
name|DoubleColumnVector
name|inputColVector
init|=
operator|(
name|DoubleColumnVector
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
name|inputIsNull
init|=
name|inputColVector
operator|.
name|isNull
decl_stmt|;
name|boolean
index|[]
name|outputIsNull
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
name|double
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
comment|// We do not need to do a column reset since we are carefully changing the output.
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|inputColVector
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|inputColVector
operator|.
name|noNulls
operator|||
operator|!
name|inputIsNull
index|[
literal|0
index|]
condition|)
block|{
comment|// Set isNull before call in case it changes it mind.
name|outputIsNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|outputVector
index|[
literal|0
index|]
operator|=
name|inSet
operator|.
name|lookup
argument_list|(
name|vector
index|[
literal|0
index|]
argument_list|)
condition|?
literal|1
else|:
literal|0
expr_stmt|;
block|}
else|else
block|{
name|outputIsNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|inputColVector
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
comment|// CONSIDER: For large n, fill n or all of isNull array and use the tighter ELSE loop.
if|if
condition|(
operator|!
name|outputColVector
operator|.
name|noNulls
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
specifier|final
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
comment|// Set isNull before call in case it changes it mind.
name|outputIsNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|outputVector
index|[
name|i
index|]
operator|=
name|inSet
operator|.
name|lookup
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|)
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
specifier|final
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
name|inSet
operator|.
name|lookup
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|)
condition|?
literal|1
else|:
literal|0
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|outputColVector
operator|.
name|noNulls
condition|)
block|{
comment|// Assume it is almost always a performance win to fill all of isNull so we can
comment|// safely reset noNulls.
name|Arrays
operator|.
name|fill
argument_list|(
name|outputIsNull
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|outputColVector
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
block|}
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
name|inSet
operator|.
name|lookup
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|)
condition|?
literal|1
else|:
literal|0
expr_stmt|;
block|}
block|}
block|}
else|else
comment|/* there are NULLs in the inputColVector */
block|{
comment|// Carefully handle NULLs...
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
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
name|outputIsNull
index|[
name|i
index|]
operator|=
name|inputIsNull
index|[
name|i
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|inputIsNull
index|[
name|i
index|]
condition|)
block|{
name|outputVector
index|[
name|i
index|]
operator|=
name|inSet
operator|.
name|lookup
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|)
condition|?
literal|1
else|:
literal|0
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|inputIsNull
argument_list|,
literal|0
argument_list|,
name|outputIsNull
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
if|if
condition|(
operator|!
name|inputIsNull
index|[
name|i
index|]
condition|)
block|{
name|outputVector
index|[
name|i
index|]
operator|=
name|inSet
operator|.
name|lookup
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|)
condition|?
literal|1
else|:
literal|0
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
specifier|public
name|void
name|setInListValues
parameter_list|(
name|double
index|[]
name|a
parameter_list|)
block|{
name|this
operator|.
name|inListValues
operator|=
name|a
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
name|colNum
argument_list|)
operator|+
literal|", values "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|inListValues
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
comment|// return null since this will be handled as a special case in VectorizationContext
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

