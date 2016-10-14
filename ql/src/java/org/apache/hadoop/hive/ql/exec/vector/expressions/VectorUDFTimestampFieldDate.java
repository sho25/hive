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
name|java
operator|.
name|util
operator|.
name|Calendar
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|io
operator|.
name|DateWritable
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

begin_comment
comment|/**  * Abstract class to return various fields from a Timestamp or Date.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VectorUDFTimestampFieldDate
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
specifier|protected
name|int
name|colNum
decl_stmt|;
specifier|protected
name|int
name|outputColumn
decl_stmt|;
specifier|protected
name|int
name|field
decl_stmt|;
specifier|protected
specifier|transient
specifier|final
name|Calendar
name|calendar
init|=
name|Calendar
operator|.
name|getInstance
argument_list|()
decl_stmt|;
specifier|public
name|VectorUDFTimestampFieldDate
parameter_list|(
name|int
name|field
parameter_list|,
name|int
name|colNum
parameter_list|,
name|int
name|outputColumn
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|this
operator|.
name|colNum
operator|=
name|colNum
expr_stmt|;
name|this
operator|.
name|outputColumn
operator|=
name|outputColumn
expr_stmt|;
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
block|}
specifier|public
name|VectorUDFTimestampFieldDate
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|long
name|getDateField
parameter_list|(
name|long
name|days
parameter_list|)
block|{
name|calendar
operator|.
name|setTimeInMillis
argument_list|(
name|DateWritable
operator|.
name|daysToMillis
argument_list|(
operator|(
name|int
operator|)
name|days
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|calendar
operator|.
name|get
argument_list|(
name|field
argument_list|)
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
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|inputTypes
index|[
literal|0
index|]
operator|==
name|VectorExpression
operator|.
name|Type
operator|.
name|DATE
argument_list|)
expr_stmt|;
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
name|outV
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
name|ColumnVector
name|inputColVec
init|=
name|batch
operator|.
name|cols
index|[
name|this
operator|.
name|colNum
index|]
decl_stmt|;
comment|/* every line below this is identical for evaluateLong& evaluateString */
specifier|final
name|int
name|n
init|=
name|inputColVec
operator|.
name|isRepeating
condition|?
literal|1
else|:
name|batch
operator|.
name|size
decl_stmt|;
name|int
index|[]
name|sel
init|=
name|batch
operator|.
name|selected
decl_stmt|;
specifier|final
name|boolean
name|selectedInUse
init|=
operator|(
name|inputColVec
operator|.
name|isRepeating
operator|==
literal|false
operator|)
operator|&&
name|batch
operator|.
name|selectedInUse
decl_stmt|;
if|if
condition|(
name|batch
operator|.
name|size
operator|==
literal|0
condition|)
block|{
comment|/* n != batch.size when isRepeating */
return|return;
block|}
comment|/* true for all algebraic UDFs with no state */
name|outV
operator|.
name|isRepeating
operator|=
name|inputColVec
operator|.
name|isRepeating
expr_stmt|;
name|LongColumnVector
name|longColVector
init|=
operator|(
name|LongColumnVector
operator|)
name|inputColVec
decl_stmt|;
if|if
condition|(
name|inputColVec
operator|.
name|noNulls
condition|)
block|{
name|outV
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
if|if
condition|(
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
name|sel
index|[
name|j
index|]
decl_stmt|;
name|outV
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|getDateField
argument_list|(
name|longColVector
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
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
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|outV
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|getDateField
argument_list|(
name|longColVector
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// Handle case with nulls. Don't do function if the value is null, to save time,
comment|// because calling the function can be expensive.
name|outV
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
if|if
condition|(
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
name|sel
index|[
name|j
index|]
decl_stmt|;
name|outV
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputColVec
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|inputColVec
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|outV
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|getDateField
argument_list|(
name|longColVector
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
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
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|outV
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputColVec
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|inputColVec
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|outV
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|getDateField
argument_list|(
name|longColVector
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
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
name|this
operator|.
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
name|getColNum
parameter_list|()
block|{
return|return
name|colNum
return|;
block|}
specifier|public
name|void
name|setColNum
parameter_list|(
name|int
name|colNum
parameter_list|)
block|{
name|this
operator|.
name|colNum
operator|=
name|colNum
expr_stmt|;
block|}
specifier|public
name|int
name|getField
parameter_list|()
block|{
return|return
name|field
return|;
block|}
specifier|public
name|void
name|setField
parameter_list|(
name|int
name|field
parameter_list|)
block|{
name|this
operator|.
name|field
operator|=
name|field
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
name|PROJECTION
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
name|DATE
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

