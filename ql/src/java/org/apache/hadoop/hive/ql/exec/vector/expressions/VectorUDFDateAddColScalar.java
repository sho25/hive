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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Text
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

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_class
specifier|public
class|class
name|VectorUDFDateAddColScalar
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
name|int
name|outputColumn
decl_stmt|;
specifier|private
name|int
name|numDays
decl_stmt|;
specifier|protected
name|boolean
name|isPositive
init|=
literal|true
decl_stmt|;
specifier|private
specifier|transient
specifier|final
name|Text
name|text
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
specifier|final
name|DateParser
name|dateParser
init|=
operator|new
name|DateParser
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
specifier|final
name|Date
name|date
init|=
operator|new
name|Date
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|public
name|VectorUDFDateAddColScalar
parameter_list|(
name|int
name|colNum
parameter_list|,
name|long
name|numDays
parameter_list|,
name|int
name|outputColumn
parameter_list|)
block|{
name|super
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
name|numDays
operator|=
operator|(
name|int
operator|)
name|numDays
expr_stmt|;
name|this
operator|.
name|outputColumn
operator|=
name|outputColumn
expr_stmt|;
block|}
specifier|public
name|VectorUDFDateAddColScalar
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
name|inputCol
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
name|inputCol
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
name|inputCol
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
name|inputCol
operator|.
name|isRepeating
expr_stmt|;
switch|switch
condition|(
name|inputTypes
index|[
literal|0
index|]
condition|)
block|{
case|case
name|DATE
case|:
if|if
condition|(
name|inputCol
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
name|evaluateDate
argument_list|(
name|inputCol
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
name|evaluateDate
argument_list|(
name|inputCol
argument_list|,
name|i
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
name|inputCol
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|inputCol
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
name|evaluateDate
argument_list|(
name|inputCol
argument_list|,
name|i
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
name|inputCol
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|inputCol
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
name|evaluateDate
argument_list|(
name|inputCol
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
break|break;
case|case
name|TIMESTAMP
case|:
if|if
condition|(
name|inputCol
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
name|evaluateTimestamp
argument_list|(
name|inputCol
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
name|evaluateTimestamp
argument_list|(
name|inputCol
argument_list|,
name|i
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
name|inputCol
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|inputCol
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
name|evaluateTimestamp
argument_list|(
name|inputCol
argument_list|,
name|i
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
name|inputCol
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|inputCol
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
name|evaluateTimestamp
argument_list|(
name|inputCol
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
break|break;
case|case
name|STRING
case|:
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
if|if
condition|(
name|inputCol
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
name|evaluateString
argument_list|(
name|inputCol
argument_list|,
name|outV
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
name|evaluateString
argument_list|(
name|inputCol
argument_list|,
name|outV
argument_list|,
name|i
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
name|inputCol
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|inputCol
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|evaluateString
argument_list|(
name|inputCol
argument_list|,
name|outV
argument_list|,
name|i
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
name|inputCol
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|inputCol
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|evaluateString
argument_list|(
name|inputCol
argument_list|,
name|outV
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
break|break;
default|default:
throw|throw
operator|new
name|Error
argument_list|(
literal|"Unsupported input type "
operator|+
name|inputTypes
index|[
literal|0
index|]
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|long
name|evaluateTimestamp
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|index
parameter_list|)
block|{
name|TimestampColumnVector
name|tcv
init|=
operator|(
name|TimestampColumnVector
operator|)
name|columnVector
decl_stmt|;
comment|// Convert to date value (in days)
name|long
name|days
init|=
name|DateWritable
operator|.
name|millisToDays
argument_list|(
name|tcv
operator|.
name|getTime
argument_list|(
name|index
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|isPositive
condition|)
block|{
name|days
operator|+=
name|numDays
expr_stmt|;
block|}
else|else
block|{
name|days
operator|-=
name|numDays
expr_stmt|;
block|}
return|return
name|days
return|;
block|}
specifier|protected
name|long
name|evaluateDate
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|index
parameter_list|)
block|{
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|columnVector
decl_stmt|;
name|long
name|days
init|=
name|lcv
operator|.
name|vector
index|[
name|index
index|]
decl_stmt|;
if|if
condition|(
name|isPositive
condition|)
block|{
name|days
operator|+=
name|numDays
expr_stmt|;
block|}
else|else
block|{
name|days
operator|-=
name|numDays
expr_stmt|;
block|}
return|return
name|days
return|;
block|}
specifier|protected
name|void
name|evaluateString
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|LongColumnVector
name|outputVector
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|BytesColumnVector
name|bcv
init|=
operator|(
name|BytesColumnVector
operator|)
name|columnVector
decl_stmt|;
name|text
operator|.
name|set
argument_list|(
name|bcv
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|bcv
operator|.
name|start
index|[
name|i
index|]
argument_list|,
name|bcv
operator|.
name|length
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|boolean
name|parsed
init|=
name|dateParser
operator|.
name|parseDate
argument_list|(
name|text
operator|.
name|toString
argument_list|()
argument_list|,
name|date
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|parsed
condition|)
block|{
name|outputVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
return|return;
block|}
name|long
name|days
init|=
name|DateWritable
operator|.
name|millisToDays
argument_list|(
name|date
operator|.
name|getTime
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|isPositive
condition|)
block|{
name|days
operator|+=
name|numDays
expr_stmt|;
block|}
else|else
block|{
name|days
operator|-=
name|numDays
expr_stmt|;
block|}
name|outputVector
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|days
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
literal|"date"
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
specifier|public
name|int
name|getNumDays
parameter_list|()
block|{
return|return
name|numDays
return|;
block|}
specifier|public
name|void
name|setNumDay
parameter_list|(
name|int
name|numDays
parameter_list|)
block|{
name|this
operator|.
name|numDays
operator|=
name|numDays
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
literal|", val "
operator|+
name|numDays
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
name|STRING_DATETIME_FAMILY
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
block|}
end_class

end_unit

