begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|common
operator|.
name|type
operator|.
name|Date
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
name|BytesColumnVector
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
name|TimestampColumnVector
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
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
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
operator|.
name|Descriptor
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|DateParser
import|;
end_import

begin_comment
comment|/**  * Vectorized implementation of trunc(date, fmt) function for timestamp input  */
end_comment

begin_class
specifier|public
class|class
name|TruncDateFromTimestamp
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
name|String
name|fmt
decl_stmt|;
specifier|protected
specifier|transient
specifier|final
name|DateParser
name|dateParser
init|=
operator|new
name|DateParser
argument_list|()
decl_stmt|;
specifier|public
name|TruncDateFromTimestamp
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|colNum
operator|=
operator|-
literal|1
expr_stmt|;
block|}
specifier|public
name|TruncDateFromTimestamp
parameter_list|(
name|int
name|colNum
parameter_list|,
name|byte
index|[]
name|fmt
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
name|fmt
operator|=
operator|new
name|String
argument_list|(
name|fmt
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
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
literal|"col "
operator|+
name|colNum
operator|+
literal|", format "
operator|+
name|fmt
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
name|this
operator|.
name|evaluateChildren
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
name|ColumnVector
name|inputColVector
init|=
name|batch
operator|.
name|cols
index|[
name|colNum
index|]
decl_stmt|;
name|BytesColumnVector
name|outputColVector
init|=
operator|(
name|BytesColumnVector
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
name|outputIsNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|truncDate
argument_list|(
name|inputColVector
argument_list|,
name|outputColVector
argument_list|,
literal|0
argument_list|)
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
name|truncDate
argument_list|(
name|inputColVector
argument_list|,
name|outputColVector
argument_list|,
name|i
argument_list|)
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
name|truncDate
argument_list|(
name|inputColVector
argument_list|,
name|outputColVector
argument_list|,
name|i
argument_list|)
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
name|truncDate
argument_list|(
name|inputColVector
argument_list|,
name|outputColVector
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
comment|/* there are nulls in the inputColVector */
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
name|truncDate
argument_list|(
name|inputColVector
argument_list|,
name|outputColVector
argument_list|,
name|i
argument_list|)
expr_stmt|;
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
name|inputColVector
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|truncDate
argument_list|(
name|inputColVector
argument_list|,
name|outputColVector
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
specifier|protected
name|void
name|truncDate
parameter_list|(
name|ColumnVector
name|inV
parameter_list|,
name|BytesColumnVector
name|outV
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|Date
name|date
init|=
name|Date
operator|.
name|ofEpochMilli
argument_list|(
operator|(
operator|(
name|TimestampColumnVector
operator|)
name|inV
operator|)
operator|.
name|getTime
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|processDate
argument_list|(
name|outV
argument_list|,
name|i
argument_list|,
name|date
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|processDate
parameter_list|(
name|BytesColumnVector
name|outV
parameter_list|,
name|int
name|i
parameter_list|,
name|Date
name|date
parameter_list|)
block|{
if|if
condition|(
literal|"MONTH"
operator|.
name|equals
argument_list|(
name|fmt
argument_list|)
operator|||
literal|"MON"
operator|.
name|equals
argument_list|(
name|fmt
argument_list|)
operator|||
literal|"MM"
operator|.
name|equals
argument_list|(
name|fmt
argument_list|)
condition|)
block|{
name|date
operator|.
name|setDayOfMonth
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"QUARTER"
operator|.
name|equals
argument_list|(
name|fmt
argument_list|)
operator|||
literal|"Q"
operator|.
name|equals
argument_list|(
name|fmt
argument_list|)
condition|)
block|{
name|int
name|month
init|=
name|date
operator|.
name|getMonth
argument_list|()
operator|-
literal|1
decl_stmt|;
name|int
name|quarter
init|=
name|month
operator|/
literal|3
decl_stmt|;
name|int
name|monthToSet
init|=
name|quarter
operator|*
literal|3
operator|+
literal|1
decl_stmt|;
name|date
operator|.
name|setMonth
argument_list|(
name|monthToSet
argument_list|)
expr_stmt|;
name|date
operator|.
name|setDayOfMonth
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"YEAR"
operator|.
name|equals
argument_list|(
name|fmt
argument_list|)
operator|||
literal|"YYYY"
operator|.
name|equals
argument_list|(
name|fmt
argument_list|)
operator|||
literal|"YY"
operator|.
name|equals
argument_list|(
name|fmt
argument_list|)
condition|)
block|{
name|date
operator|.
name|setMonth
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|date
operator|.
name|setDayOfMonth
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|bytes
init|=
name|date
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
name|outV
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
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
literal|2
argument_list|)
operator|.
name|setArgumentTypes
argument_list|(
name|getInputColumnType
argument_list|()
argument_list|,
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|STRING_FAMILY
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
name|SCALAR
argument_list|)
expr_stmt|;
return|return
name|b
operator|.
name|build
argument_list|()
return|;
block|}
specifier|protected
name|ArgumentType
name|getInputColumnType
parameter_list|()
block|{
return|return
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|TIMESTAMP
return|;
block|}
block|}
end_class

end_unit

