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
name|java
operator|.
name|text
operator|.
name|ParseException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|SimpleDateFormat
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
name|java
operator|.
name|util
operator|.
name|Calendar
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
import|;
end_import

begin_class
specifier|public
class|class
name|VectorUDFDateAddColCol
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
specifier|private
name|int
name|colNum2
decl_stmt|;
specifier|private
name|int
name|outputColumn
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
name|Calendar
name|calendar
init|=
name|Calendar
operator|.
name|getInstance
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
name|SimpleDateFormat
name|formatter
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd"
argument_list|)
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
specifier|public
name|VectorUDFDateAddColCol
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
argument_list|()
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
name|this
operator|.
name|outputColumn
operator|=
name|outputColumn
expr_stmt|;
block|}
specifier|public
name|VectorUDFDateAddColCol
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
name|ColumnVector
name|inputColVector1
init|=
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
name|vector2
init|=
name|inputColVector2
operator|.
name|vector
decl_stmt|;
name|BytesColumnVector
name|outV
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|outputColumn
index|]
decl_stmt|;
name|byte
index|[]
index|[]
name|outputVector
init|=
name|outV
operator|.
name|vector
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
comment|// Handle null
name|NullUtil
operator|.
name|propagateNullsColCol
argument_list|(
name|inputColVector1
argument_list|,
name|inputColVector2
argument_list|,
name|outV
argument_list|,
name|batch
operator|.
name|selected
argument_list|,
name|batch
operator|.
name|size
argument_list|,
name|batch
operator|.
name|selectedInUse
argument_list|)
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
comment|// Now disregard null in second pass.
if|if
condition|(
operator|(
name|inputColVector1
operator|.
name|isRepeating
operator|)
operator|&&
operator|(
name|inputColVector2
operator|.
name|isRepeating
operator|)
condition|)
block|{
comment|// All must be selected otherwise size would be zero
comment|// Repeating property will not change.
name|outV
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|outputVector
index|[
literal|0
index|]
operator|=
name|evaluateDate
argument_list|(
name|inputColVector1
argument_list|,
literal|0
argument_list|,
name|vector2
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|outV
operator|.
name|start
index|[
literal|0
index|]
operator|=
literal|0
expr_stmt|;
name|outV
operator|.
name|length
index|[
literal|0
index|]
operator|=
name|outputVector
index|[
literal|0
index|]
operator|.
name|length
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
name|evaluateDate
argument_list|(
name|inputColVector1
argument_list|,
name|i
argument_list|,
name|vector2
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|outV
operator|.
name|start
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|outV
operator|.
name|length
index|[
name|i
index|]
operator|=
name|outputVector
index|[
literal|0
index|]
operator|.
name|length
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
name|evaluateDate
argument_list|(
name|inputColVector1
argument_list|,
name|i
argument_list|,
name|vector2
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|outV
operator|.
name|start
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|outV
operator|.
name|length
index|[
name|i
index|]
operator|=
name|outputVector
index|[
literal|0
index|]
operator|.
name|length
expr_stmt|;
block|}
block|}
break|break;
case|case
name|TIMESTAMP
case|:
comment|// Now disregard null in second pass.
if|if
condition|(
operator|(
name|inputColVector1
operator|.
name|isRepeating
operator|)
operator|&&
operator|(
name|inputColVector2
operator|.
name|isRepeating
operator|)
condition|)
block|{
comment|// All must be selected otherwise size would be zero
comment|// Repeating property will not change.
name|outV
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|outputVector
index|[
literal|0
index|]
operator|=
name|evaluateTimestamp
argument_list|(
name|inputColVector1
argument_list|,
literal|0
argument_list|,
name|vector2
index|[
literal|0
index|]
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
index|[
name|i
index|]
operator|=
name|evaluateTimestamp
argument_list|(
name|inputColVector1
argument_list|,
name|i
argument_list|,
name|vector2
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|outV
operator|.
name|start
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|outV
operator|.
name|length
index|[
name|i
index|]
operator|=
name|outputVector
index|[
literal|0
index|]
operator|.
name|length
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
name|evaluateTimestamp
argument_list|(
name|inputColVector1
argument_list|,
name|i
argument_list|,
name|vector2
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|outV
operator|.
name|start
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|outV
operator|.
name|length
index|[
name|i
index|]
operator|=
name|outputVector
index|[
literal|0
index|]
operator|.
name|length
expr_stmt|;
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
comment|// Now disregard null in second pass.
if|if
condition|(
operator|(
name|inputColVector1
operator|.
name|isRepeating
operator|)
operator|&&
operator|(
name|inputColVector2
operator|.
name|isRepeating
operator|)
condition|)
block|{
comment|// All must be selected otherwise size would be zero
comment|// Repeating property will not change.
name|outV
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|evaluateString
argument_list|(
operator|(
name|BytesColumnVector
operator|)
name|inputColVector1
argument_list|,
name|inputColVector2
argument_list|,
name|outV
argument_list|,
literal|0
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
name|evaluateString
argument_list|(
operator|(
name|BytesColumnVector
operator|)
name|inputColVector1
argument_list|,
name|inputColVector2
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
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
name|evaluateString
argument_list|(
operator|(
name|BytesColumnVector
operator|)
name|inputColVector1
argument_list|,
name|inputColVector2
argument_list|,
name|outV
argument_list|,
name|i
argument_list|)
expr_stmt|;
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
name|byte
index|[]
name|evaluateDate
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|index
parameter_list|,
name|long
name|numDays
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
if|if
condition|(
name|isPositive
condition|)
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
name|lcv
operator|.
name|vector
index|[
name|index
index|]
operator|+
operator|(
name|int
operator|)
name|numDays
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
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
name|lcv
operator|.
name|vector
index|[
name|index
index|]
operator|-
operator|(
name|int
operator|)
name|numDays
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Date
name|newDate
init|=
name|calendar
operator|.
name|getTime
argument_list|()
decl_stmt|;
name|text
operator|.
name|set
argument_list|(
name|formatter
operator|.
name|format
argument_list|(
name|newDate
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|Arrays
operator|.
name|copyOf
argument_list|(
name|text
operator|.
name|getBytes
argument_list|()
argument_list|,
name|text
operator|.
name|getLength
argument_list|()
argument_list|)
return|;
block|}
specifier|protected
name|byte
index|[]
name|evaluateTimestamp
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|index
parameter_list|,
name|long
name|numDays
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
name|calendar
operator|.
name|setTimeInMillis
argument_list|(
name|lcv
operator|.
name|vector
index|[
name|index
index|]
operator|/
literal|1000000
argument_list|)
expr_stmt|;
if|if
condition|(
name|isPositive
condition|)
block|{
name|calendar
operator|.
name|add
argument_list|(
name|Calendar
operator|.
name|DATE
argument_list|,
operator|(
name|int
operator|)
name|numDays
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|calendar
operator|.
name|add
argument_list|(
name|Calendar
operator|.
name|DATE
argument_list|,
operator|(
name|int
operator|)
operator|-
name|numDays
argument_list|)
expr_stmt|;
block|}
name|Date
name|newDate
init|=
name|calendar
operator|.
name|getTime
argument_list|()
decl_stmt|;
name|text
operator|.
name|set
argument_list|(
name|formatter
operator|.
name|format
argument_list|(
name|newDate
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|Arrays
operator|.
name|copyOf
argument_list|(
name|text
operator|.
name|getBytes
argument_list|()
argument_list|,
name|text
operator|.
name|getLength
argument_list|()
argument_list|)
return|;
block|}
specifier|protected
name|void
name|evaluateString
parameter_list|(
name|BytesColumnVector
name|inputColumnVector1
parameter_list|,
name|LongColumnVector
name|inputColumnVector2
parameter_list|,
name|BytesColumnVector
name|outputVector
parameter_list|,
name|int
name|i
parameter_list|)
block|{
if|if
condition|(
name|inputColumnVector1
operator|.
name|isNull
index|[
name|i
index|]
operator|||
name|inputColumnVector2
operator|.
name|isNull
index|[
name|i
index|]
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
block|}
else|else
block|{
name|text
operator|.
name|set
argument_list|(
name|inputColumnVector1
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|inputColumnVector1
operator|.
name|start
index|[
name|i
index|]
argument_list|,
name|inputColumnVector1
operator|.
name|length
index|[
name|i
index|]
argument_list|)
expr_stmt|;
try|try
block|{
name|calendar
operator|.
name|setTime
argument_list|(
name|formatter
operator|.
name|parse
argument_list|(
name|text
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
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
block|}
if|if
condition|(
name|isPositive
condition|)
block|{
name|calendar
operator|.
name|add
argument_list|(
name|Calendar
operator|.
name|DATE
argument_list|,
operator|(
name|int
operator|)
name|inputColumnVector2
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|calendar
operator|.
name|add
argument_list|(
name|Calendar
operator|.
name|DATE
argument_list|,
operator|-
operator|(
name|int
operator|)
name|inputColumnVector2
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|Date
name|newDate
init|=
name|calendar
operator|.
name|getTime
argument_list|()
decl_stmt|;
name|text
operator|.
name|set
argument_list|(
name|formatter
operator|.
name|format
argument_list|(
name|newDate
argument_list|)
argument_list|)
expr_stmt|;
name|outputVector
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|text
operator|.
name|getBytes
argument_list|()
argument_list|,
name|text
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|outputVector
operator|.
name|start
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|outputVector
operator|.
name|length
index|[
name|i
index|]
operator|=
name|text
operator|.
name|getLength
argument_list|()
expr_stmt|;
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
literal|"string"
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
name|int
name|getColNum2
parameter_list|()
block|{
return|return
name|colNum2
return|;
block|}
specifier|public
name|void
name|setColNum2
parameter_list|(
name|int
name|colNum2
parameter_list|)
block|{
name|this
operator|.
name|colNum2
operator|=
name|colNum2
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

