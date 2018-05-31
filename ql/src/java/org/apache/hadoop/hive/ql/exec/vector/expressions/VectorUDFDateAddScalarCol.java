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
name|hive
operator|.
name|serde2
operator|.
name|objectinspector
operator|.
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
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
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
specifier|final
name|int
name|colNum
decl_stmt|;
specifier|private
name|Object
name|object
decl_stmt|;
specifier|private
name|long
name|longValue
init|=
literal|0
decl_stmt|;
specifier|private
name|Timestamp
name|timestampValue
init|=
literal|null
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
name|baseDate
init|=
operator|new
name|Date
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Transient members initialized by transientInit method.
specifier|private
specifier|transient
name|PrimitiveCategory
name|primitiveCategory
decl_stmt|;
specifier|public
name|VectorUDFDateAddScalarCol
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// Dummy final assignments.
name|colNum
operator|=
operator|-
literal|1
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
name|object
operator|=
name|object
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
name|Timestamp
condition|)
block|{
name|this
operator|.
name|timestampValue
operator|=
operator|(
name|Timestamp
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
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected scalar object "
operator|+
name|object
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" "
operator|+
name|object
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|transientInit
parameter_list|()
throws|throws
name|HiveException
block|{
name|super
operator|.
name|transientInit
argument_list|()
expr_stmt|;
name|primitiveCategory
operator|=
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|inputTypeInfos
index|[
literal|0
index|]
operator|)
operator|.
name|getPrimitiveCategory
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
name|boolean
index|[]
name|outputIsNull
init|=
name|outputColVector
operator|.
name|isNull
decl_stmt|;
switch|switch
condition|(
name|primitiveCategory
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
name|timestampValue
operator|.
name|getTime
argument_list|()
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
name|boolean
name|parsed
init|=
name|dateParser
operator|.
name|parseDate
argument_list|(
operator|new
name|String
argument_list|(
name|stringValue
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|,
name|baseDate
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|parsed
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
name|outputColVector
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
name|outputColVector
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
break|break;
default|default:
throw|throw
operator|new
name|Error
argument_list|(
literal|"Unsupported input type "
operator|+
name|primitiveCategory
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
comment|// We do not need to do a column reset since we are carefully changing the output.
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
name|long
name|baseDateDays
init|=
name|DateWritable
operator|.
name|millisToDays
argument_list|(
name|baseDate
operator|.
name|getTime
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|inputCol
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|inputCol
operator|.
name|noNulls
operator|||
operator|!
name|inputCol
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|outputColVector
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|evaluate
argument_list|(
name|baseDateDays
argument_list|,
name|inputCol
operator|.
name|vector
index|[
literal|0
index|]
argument_list|,
name|outputColVector
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
name|inputCol
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
name|evaluate
argument_list|(
name|baseDateDays
argument_list|,
name|inputCol
operator|.
name|vector
index|[
name|i
index|]
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
name|evaluate
argument_list|(
name|baseDateDays
argument_list|,
name|inputCol
operator|.
name|vector
index|[
name|i
index|]
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
name|evaluate
argument_list|(
name|baseDateDays
argument_list|,
name|inputCol
operator|.
name|vector
index|[
name|i
index|]
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
comment|// Carefully handle NULLs..
comment|// Handle case with nulls. Don't do function if the value is null, to save time,
comment|// because calling the function can be expensive.
name|outputColVector
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
name|outputColVector
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
name|baseDateDays
argument_list|,
name|inputCol
operator|.
name|vector
index|[
name|i
index|]
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
name|outputColVector
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
name|baseDateDays
argument_list|,
name|inputCol
operator|.
name|vector
index|[
name|i
index|]
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
specifier|private
name|void
name|evaluate
parameter_list|(
name|long
name|baseDateDays
parameter_list|,
name|long
name|numDays
parameter_list|,
name|LongColumnVector
name|output
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|long
name|result
init|=
name|baseDateDays
decl_stmt|;
if|if
condition|(
name|isPositive
condition|)
block|{
name|result
operator|+=
name|numDays
expr_stmt|;
block|}
else|else
block|{
name|result
operator|-=
name|numDays
expr_stmt|;
block|}
name|output
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|result
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
annotation|@
name|Override
specifier|public
name|String
name|vectorExpressionParameters
parameter_list|()
block|{
name|String
name|value
decl_stmt|;
if|if
condition|(
name|object
operator|instanceof
name|Long
condition|)
block|{
name|Date
name|tempDate
init|=
operator|new
name|Date
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|tempDate
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
name|value
operator|=
name|tempDate
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|object
operator|instanceof
name|Timestamp
condition|)
block|{
name|value
operator|=
name|this
operator|.
name|timestampValue
operator|.
name|toString
argument_list|()
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
name|value
operator|=
operator|new
name|String
argument_list|(
name|this
operator|.
name|stringValue
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|value
operator|=
literal|"unknown"
expr_stmt|;
block|}
return|return
literal|"val "
operator|+
name|value
operator|+
literal|", "
operator|+
name|getColumnParamString
argument_list|(
literal|0
argument_list|,
name|colNum
argument_list|)
return|;
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

