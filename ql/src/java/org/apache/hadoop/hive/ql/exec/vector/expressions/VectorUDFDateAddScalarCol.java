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
name|io
operator|.
name|UnsupportedEncodingException
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
name|VectorUDFDateAddScalarCol
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
name|long
name|longValue
init|=
literal|0
decl_stmt|;
specifier|private
name|byte
index|[]
name|stringValue
init|=
literal|null
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
specifier|private
specifier|transient
name|Date
name|baseDate
init|=
operator|new
name|Date
argument_list|()
decl_stmt|;
specifier|public
name|VectorUDFDateAddScalarCol
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorUDFDateAddScalarCol
parameter_list|(
name|Object
name|object
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
if|if
condition|(
name|object
operator|instanceof
name|Long
condition|)
block|{
name|this
operator|.
name|longValue
operator|=
operator|(
name|Long
operator|)
name|object
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|object
operator|instanceof
name|byte
index|[]
condition|)
block|{
name|this
operator|.
name|stringValue
operator|=
operator|(
name|byte
index|[]
operator|)
name|object
expr_stmt|;
block|}
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
name|inputCol
init|=
operator|(
name|LongColumnVector
operator|)
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
name|baseDate
operator|.
name|setTime
argument_list|(
name|DateWritable
operator|.
name|daysToMillis
argument_list|(
operator|(
name|int
operator|)
name|longValue
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|baseDate
operator|.
name|setTime
argument_list|(
name|longValue
operator|/
literal|1000000
argument_list|)
expr_stmt|;
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
try|try
block|{
name|baseDate
operator|=
name|formatter
operator|.
name|parse
argument_list|(
operator|new
name|String
argument_list|(
name|stringValue
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
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
literal|true
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
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return;
block|}
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
name|evaluate
argument_list|(
name|baseDate
argument_list|,
name|inputCol
operator|.
name|vector
index|[
name|i
index|]
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
name|evaluate
argument_list|(
name|baseDate
argument_list|,
name|inputCol
operator|.
name|vector
index|[
name|i
index|]
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
name|evaluate
argument_list|(
name|baseDate
argument_list|,
name|inputCol
operator|.
name|vector
index|[
name|i
index|]
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
name|evaluate
argument_list|(
name|baseDate
argument_list|,
name|inputCol
operator|.
name|vector
index|[
name|i
index|]
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
block|}
specifier|private
name|void
name|evaluate
parameter_list|(
name|Date
name|baseDate
parameter_list|,
name|long
name|numDays
parameter_list|,
name|BytesColumnVector
name|output
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|calendar
operator|.
name|setTime
argument_list|(
name|baseDate
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
operator|-
operator|(
name|int
operator|)
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
name|int
name|size
init|=
name|text
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|output
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
name|size
argument_list|)
expr_stmt|;
name|output
operator|.
name|start
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
name|output
operator|.
name|length
index|[
name|i
index|]
operator|=
name|size
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
literal|"string"
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
name|long
name|getLongValue
parameter_list|()
block|{
return|return
name|longValue
return|;
block|}
specifier|public
name|void
name|setLongValue
parameter_list|(
name|long
name|longValue
parameter_list|)
block|{
name|this
operator|.
name|longValue
operator|=
name|longValue
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getStringValue
parameter_list|()
block|{
return|return
name|stringValue
return|;
block|}
specifier|public
name|void
name|setStringValue
parameter_list|(
name|byte
index|[]
name|stringValue
parameter_list|)
block|{
name|this
operator|.
name|stringValue
operator|=
name|stringValue
expr_stmt|;
block|}
specifier|public
name|boolean
name|isPositive
parameter_list|()
block|{
return|return
name|isPositive
return|;
block|}
specifier|public
name|void
name|setPositive
parameter_list|(
name|boolean
name|isPositive
parameter_list|)
block|{
name|this
operator|.
name|isPositive
operator|=
name|isPositive
expr_stmt|;
block|}
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
name|SCALAR
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

