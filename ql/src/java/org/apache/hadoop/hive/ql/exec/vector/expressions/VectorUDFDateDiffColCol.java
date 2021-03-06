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
name|conf
operator|.
name|Configuration
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
name|DateWritableV2
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
name|hadoop
operator|.
name|hive
operator|.
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfo
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

begin_class
specifier|public
class|class
name|VectorUDFDateDiffColCol
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
name|colNum1
decl_stmt|;
specifier|private
specifier|final
name|int
name|colNum2
decl_stmt|;
specifier|private
specifier|transient
specifier|final
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
name|Date
name|date
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
name|LongColumnVector
name|dateVector1
decl_stmt|;
specifier|private
specifier|transient
name|LongColumnVector
name|dateVector2
decl_stmt|;
specifier|public
name|VectorUDFDateDiffColCol
parameter_list|(
name|int
name|colNum1
parameter_list|,
name|int
name|colNum2
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
block|}
specifier|public
name|VectorUDFDateDiffColCol
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// Dummy final assignments.
name|colNum1
operator|=
operator|-
literal|1
expr_stmt|;
name|colNum2
operator|=
operator|-
literal|1
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|transientInit
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|transientInit
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|dateVector1
operator|=
operator|new
name|LongColumnVector
argument_list|()
expr_stmt|;
name|dateVector2
operator|=
operator|new
name|LongColumnVector
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
name|ColumnVector
name|inputColVector2
init|=
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
name|outputColumnNum
index|]
decl_stmt|;
name|long
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
comment|/*      * Propagate null values for a two-input operator and set isRepeating and noNulls appropriately.      */
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
name|LongColumnVector
name|convertedVector1
init|=
name|toDateArray
argument_list|(
name|batch
argument_list|,
name|inputTypeInfos
index|[
literal|0
index|]
argument_list|,
name|inputColVector1
argument_list|,
name|dateVector1
argument_list|)
decl_stmt|;
name|LongColumnVector
name|convertedVector2
init|=
name|toDateArray
argument_list|(
name|batch
argument_list|,
name|inputTypeInfos
index|[
literal|1
index|]
argument_list|,
name|inputColVector2
argument_list|,
name|dateVector2
argument_list|)
decl_stmt|;
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
if|if
condition|(
name|convertedVector1
operator|.
name|isNull
index|[
literal|0
index|]
operator|||
name|convertedVector2
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|outV
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|outputVector
index|[
literal|0
index|]
operator|=
name|convertedVector1
operator|.
name|vector
index|[
literal|0
index|]
operator|-
name|convertedVector2
operator|.
name|vector
index|[
literal|0
index|]
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|inputColVector1
operator|.
name|isRepeating
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
if|if
condition|(
name|convertedVector1
operator|.
name|isNull
index|[
literal|0
index|]
operator|||
name|convertedVector2
operator|.
name|isNull
index|[
name|i
index|]
condition|)
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
else|else
block|{
name|outputVector
index|[
name|i
index|]
operator|=
name|convertedVector1
operator|.
name|vector
index|[
literal|0
index|]
operator|-
name|convertedVector2
operator|.
name|vector
index|[
name|i
index|]
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
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|convertedVector1
operator|.
name|isNull
index|[
literal|0
index|]
operator|||
name|convertedVector2
operator|.
name|isNull
index|[
name|i
index|]
condition|)
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
else|else
block|{
name|outputVector
index|[
name|i
index|]
operator|=
name|convertedVector1
operator|.
name|vector
index|[
literal|0
index|]
operator|-
name|convertedVector2
operator|.
name|vector
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|inputColVector2
operator|.
name|isRepeating
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
if|if
condition|(
name|convertedVector1
operator|.
name|isNull
index|[
name|i
index|]
operator|||
name|convertedVector2
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
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
else|else
block|{
name|outputVector
index|[
name|i
index|]
operator|=
name|convertedVector1
operator|.
name|vector
index|[
name|i
index|]
operator|-
name|convertedVector2
operator|.
name|vector
index|[
literal|0
index|]
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
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|convertedVector1
operator|.
name|isNull
index|[
name|i
index|]
operator|||
name|convertedVector2
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
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
else|else
block|{
name|outputVector
index|[
name|i
index|]
operator|=
name|convertedVector1
operator|.
name|vector
index|[
name|i
index|]
operator|-
name|convertedVector2
operator|.
name|vector
index|[
literal|0
index|]
expr_stmt|;
block|}
block|}
block|}
block|}
else|else
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
if|if
condition|(
name|convertedVector1
operator|.
name|isNull
index|[
name|i
index|]
operator|||
name|convertedVector2
operator|.
name|isNull
index|[
name|i
index|]
condition|)
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
else|else
block|{
name|outputVector
index|[
name|i
index|]
operator|=
name|convertedVector1
operator|.
name|vector
index|[
name|i
index|]
operator|-
name|convertedVector2
operator|.
name|vector
index|[
name|i
index|]
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
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|convertedVector1
operator|.
name|isNull
index|[
name|i
index|]
operator|||
name|convertedVector2
operator|.
name|isNull
index|[
name|i
index|]
condition|)
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
else|else
block|{
name|outputVector
index|[
name|i
index|]
operator|=
name|convertedVector1
operator|.
name|vector
index|[
name|i
index|]
operator|-
name|convertedVector2
operator|.
name|vector
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
specifier|private
name|LongColumnVector
name|toDateArray
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|,
name|TypeInfo
name|typeInfo
parameter_list|,
name|ColumnVector
name|inputColVector
parameter_list|,
name|LongColumnVector
name|dateVector
parameter_list|)
block|{
name|PrimitiveCategory
name|primitiveCategory
init|=
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
name|int
name|size
init|=
name|batch
operator|.
name|size
decl_stmt|;
if|if
condition|(
name|primitiveCategory
operator|==
name|PrimitiveCategory
operator|.
name|DATE
condition|)
block|{
return|return
operator|(
name|LongColumnVector
operator|)
name|inputColVector
return|;
block|}
if|if
condition|(
name|size
operator|>
name|dateVector
operator|.
name|vector
operator|.
name|length
condition|)
block|{
if|if
condition|(
name|dateVector1
operator|==
name|dateVector
condition|)
block|{
name|dateVector1
operator|=
operator|new
name|LongColumnVector
argument_list|(
name|size
operator|*
literal|2
argument_list|)
expr_stmt|;
name|dateVector
operator|=
name|dateVector1
expr_stmt|;
block|}
else|else
block|{
name|dateVector2
operator|=
operator|new
name|LongColumnVector
argument_list|(
name|size
operator|*
literal|2
argument_list|)
expr_stmt|;
name|dateVector
operator|=
name|dateVector2
expr_stmt|;
block|}
block|}
switch|switch
condition|(
name|primitiveCategory
condition|)
block|{
case|case
name|TIMESTAMP
case|:
name|TimestampColumnVector
name|tcv
init|=
operator|(
name|TimestampColumnVector
operator|)
name|inputColVector
decl_stmt|;
name|copySelected
argument_list|(
name|tcv
argument_list|,
name|batch
operator|.
name|selectedInUse
argument_list|,
name|batch
operator|.
name|selected
argument_list|,
name|batch
operator|.
name|size
argument_list|,
name|dateVector
argument_list|)
expr_stmt|;
return|return
name|dateVector
return|;
case|case
name|STRING
case|:
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
name|BytesColumnVector
name|bcv
init|=
operator|(
name|BytesColumnVector
operator|)
name|inputColVector
decl_stmt|;
name|copySelected
argument_list|(
name|bcv
argument_list|,
name|batch
operator|.
name|selectedInUse
argument_list|,
name|batch
operator|.
name|selected
argument_list|,
name|batch
operator|.
name|size
argument_list|,
name|dateVector
argument_list|)
expr_stmt|;
return|return
name|dateVector
return|;
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
block|}
comment|// Copy the current object contents into the output. Only copy selected entries,
comment|// as indicated by selectedInUse and the sel array.
specifier|public
name|void
name|copySelected
parameter_list|(
name|BytesColumnVector
name|input
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|size
parameter_list|,
name|LongColumnVector
name|output
parameter_list|)
block|{
name|output
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
comment|// Handle repeating case
if|if
condition|(
name|input
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|input
operator|.
name|noNulls
operator|||
operator|!
name|input
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|String
name|string
init|=
operator|new
name|String
argument_list|(
name|input
operator|.
name|vector
index|[
literal|0
index|]
argument_list|,
name|input
operator|.
name|start
index|[
literal|0
index|]
argument_list|,
name|input
operator|.
name|length
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
try|try
block|{
name|date
operator|.
name|setTime
argument_list|(
name|formatter
operator|.
name|parse
argument_list|(
name|string
argument_list|)
operator|.
name|getTime
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|DateWritableV2
operator|.
name|dateToDays
argument_list|(
name|date
argument_list|)
expr_stmt|;
name|output
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
name|output
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|output
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
block|}
else|else
block|{
name|output
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|output
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
name|output
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
return|return;
block|}
comment|// Handle normal case
comment|// Copy data values over
if|if
condition|(
name|input
operator|.
name|noNulls
condition|)
block|{
if|if
condition|(
name|selectedInUse
condition|)
block|{
if|if
condition|(
operator|!
name|output
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
operator|<
name|size
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
name|output
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
comment|// setDays resets the isNull[i] to false if there is a parse exception
name|setDays
argument_list|(
name|input
argument_list|,
name|output
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
operator|<
name|size
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
comment|// setDays resets the isNull[i] to false if there is a parse exception
name|setDays
argument_list|(
name|input
argument_list|,
name|output
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
name|output
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
name|output
operator|.
name|isNull
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|output
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
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|setDays
argument_list|(
name|input
argument_list|,
name|output
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
comment|/* there are nulls in our column */
block|{
comment|// handle the isNull array first in tight loops
name|output
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
name|size
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
name|output
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|input
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
else|else
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|input
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|output
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
comment|//now copy over the data where isNull[index] is false
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
name|size
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
operator|!
name|input
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|setDays
argument_list|(
name|input
argument_list|,
name|output
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
name|size
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|input
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|setDays
argument_list|(
name|input
argument_list|,
name|output
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
name|setDays
parameter_list|(
name|BytesColumnVector
name|input
parameter_list|,
name|LongColumnVector
name|output
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|String
name|string
init|=
operator|new
name|String
argument_list|(
name|input
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|input
operator|.
name|start
index|[
name|i
index|]
argument_list|,
name|input
operator|.
name|length
index|[
name|i
index|]
argument_list|)
decl_stmt|;
try|try
block|{
name|date
operator|.
name|setTime
argument_list|(
name|formatter
operator|.
name|parse
argument_list|(
name|string
argument_list|)
operator|.
name|getTime
argument_list|()
argument_list|)
expr_stmt|;
name|output
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|DateWritableV2
operator|.
name|dateToDays
argument_list|(
name|date
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
name|output
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|output
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
block|}
comment|// Copy the current object contents into the output. Only copy selected entries,
comment|// as indicated by selectedInUse and the sel array.
specifier|public
name|void
name|copySelected
parameter_list|(
name|TimestampColumnVector
name|input
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|size
parameter_list|,
name|LongColumnVector
name|output
parameter_list|)
block|{
name|output
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
comment|// Handle repeating case
if|if
condition|(
name|input
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|input
operator|.
name|noNulls
operator|||
operator|!
name|input
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|date
operator|.
name|setTime
argument_list|(
name|input
operator|.
name|getTime
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|output
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|DateWritableV2
operator|.
name|dateToDays
argument_list|(
name|date
argument_list|)
expr_stmt|;
name|output
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|output
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|output
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
name|output
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
return|return;
block|}
comment|// Handle normal case
comment|// Copy data values over
if|if
condition|(
name|input
operator|.
name|noNulls
condition|)
block|{
if|if
condition|(
name|selectedInUse
condition|)
block|{
if|if
condition|(
operator|!
name|output
operator|.
name|noNulls
condition|)
block|{
comment|// output has noNulls set to false so set the isNull[] to false carefully
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|size
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
name|date
operator|.
name|setTime
argument_list|(
name|input
operator|.
name|getTime
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|output
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|DateWritableV2
operator|.
name|dateToDays
argument_list|(
name|date
argument_list|)
expr_stmt|;
name|output
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
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
operator|<
name|size
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
name|date
operator|.
name|setTime
argument_list|(
name|input
operator|.
name|getTime
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|output
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|DateWritableV2
operator|.
name|dateToDays
argument_list|(
name|date
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
name|output
operator|.
name|noNulls
condition|)
block|{
comment|//output noNulls is set to false, we need to reset the isNull array
name|Arrays
operator|.
name|fill
argument_list|(
name|output
operator|.
name|isNull
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|output
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
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|date
operator|.
name|setTime
argument_list|(
name|input
operator|.
name|getTime
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|output
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|DateWritableV2
operator|.
name|dateToDays
argument_list|(
name|date
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
comment|/* there are nulls in our column */
block|{
name|output
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
comment|//handle the isNull array first in tight loops
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
name|size
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
name|output
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|input
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
else|else
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|input
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|output
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
comment|//now copy over the data when isNull[index] is false
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
name|size
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
operator|!
name|input
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|date
operator|.
name|setTime
argument_list|(
name|input
operator|.
name|getTime
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|output
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|DateWritableV2
operator|.
name|dateToDays
argument_list|(
name|date
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
name|size
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|input
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|date
operator|.
name|setTime
argument_list|(
name|input
operator|.
name|getTime
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|output
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|DateWritableV2
operator|.
name|dateToDays
argument_list|(
name|date
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
name|String
name|vectorExpressionParameters
parameter_list|()
block|{
return|return
name|getColumnParamString
argument_list|(
literal|0
argument_list|,
name|colNum1
argument_list|)
operator|+
literal|", "
operator|+
name|getColumnParamString
argument_list|(
literal|1
argument_list|,
name|colNum2
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
name|STRING_DATETIME_FAMILY
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

