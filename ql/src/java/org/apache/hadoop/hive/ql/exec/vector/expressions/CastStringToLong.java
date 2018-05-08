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
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|lazy
operator|.
name|LazyByte
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
name|lazy
operator|.
name|LazyInteger
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
name|lazy
operator|.
name|LazyLong
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
name|lazy
operator|.
name|LazyShort
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
name|lazy
operator|.
name|LazyUtils
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
name|TypeInfoUtils
import|;
end_import

begin_comment
comment|/**  * Cast a string to a long.  *  * If other functions besides cast need to take a string in and produce a long,  * you can subclass this class or convert it to a superclass, and  * implement different "func()" methods for each operation.  */
end_comment

begin_class
specifier|public
class|class
name|CastStringToLong
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
name|int
name|inputColumn
decl_stmt|;
comment|// Transient members initialized by transientInit method.
specifier|protected
specifier|transient
name|PrimitiveCategory
name|integerPrimitiveCategory
decl_stmt|;
specifier|public
name|CastStringToLong
parameter_list|(
name|int
name|inputColumn
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
name|inputColumn
operator|=
name|inputColumn
expr_stmt|;
block|}
specifier|public
name|CastStringToLong
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// Dummy final assignments.
name|inputColumn
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
parameter_list|()
throws|throws
name|HiveException
block|{
name|super
operator|.
name|transientInit
argument_list|()
expr_stmt|;
name|integerPrimitiveCategory
operator|=
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|outputTypeInfo
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
expr_stmt|;
block|}
comment|/**    * Convert input string to a long, at position i in the respective vectors.    */
specifier|protected
name|void
name|func
parameter_list|(
name|LongColumnVector
name|outputColVector
parameter_list|,
name|BytesColumnVector
name|inputColVector
parameter_list|,
name|int
name|batchIndex
parameter_list|)
block|{
name|byte
index|[]
name|bytes
init|=
name|inputColVector
operator|.
name|vector
index|[
name|batchIndex
index|]
decl_stmt|;
specifier|final
name|int
name|start
init|=
name|inputColVector
operator|.
name|start
index|[
name|batchIndex
index|]
decl_stmt|;
specifier|final
name|int
name|length
init|=
name|inputColVector
operator|.
name|length
index|[
name|batchIndex
index|]
decl_stmt|;
try|try
block|{
switch|switch
condition|(
name|integerPrimitiveCategory
condition|)
block|{
case|case
name|BOOLEAN
case|:
block|{
name|boolean
name|booleanValue
decl_stmt|;
name|int
name|i
init|=
name|start
decl_stmt|;
if|if
condition|(
name|length
operator|==
literal|4
condition|)
block|{
if|if
condition|(
operator|(
name|bytes
index|[
name|i
index|]
operator|==
literal|'T'
operator|||
name|bytes
index|[
name|i
index|]
operator|==
literal|'t'
operator|)
operator|&&
operator|(
name|bytes
index|[
name|i
operator|+
literal|1
index|]
operator|==
literal|'R'
operator|||
name|bytes
index|[
name|i
operator|+
literal|1
index|]
operator|==
literal|'r'
operator|)
operator|&&
operator|(
name|bytes
index|[
name|i
operator|+
literal|2
index|]
operator|==
literal|'U'
operator|||
name|bytes
index|[
name|i
operator|+
literal|2
index|]
operator|==
literal|'u'
operator|)
operator|&&
operator|(
name|bytes
index|[
name|i
operator|+
literal|3
index|]
operator|==
literal|'E'
operator|||
name|bytes
index|[
name|i
operator|+
literal|3
index|]
operator|==
literal|'e'
operator|)
condition|)
block|{
name|booleanValue
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
comment|// No boolean value match for 4 char field.
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|batchIndex
index|]
operator|=
literal|true
expr_stmt|;
return|return;
block|}
block|}
elseif|else
if|if
condition|(
name|length
operator|==
literal|5
condition|)
block|{
if|if
condition|(
operator|(
name|bytes
index|[
name|i
index|]
operator|==
literal|'F'
operator|||
name|bytes
index|[
name|i
index|]
operator|==
literal|'f'
operator|)
operator|&&
operator|(
name|bytes
index|[
name|i
operator|+
literal|1
index|]
operator|==
literal|'A'
operator|||
name|bytes
index|[
name|i
operator|+
literal|1
index|]
operator|==
literal|'a'
operator|)
operator|&&
operator|(
name|bytes
index|[
name|i
operator|+
literal|2
index|]
operator|==
literal|'L'
operator|||
name|bytes
index|[
name|i
operator|+
literal|2
index|]
operator|==
literal|'l'
operator|)
operator|&&
operator|(
name|bytes
index|[
name|i
operator|+
literal|3
index|]
operator|==
literal|'S'
operator|||
name|bytes
index|[
name|i
operator|+
literal|3
index|]
operator|==
literal|'s'
operator|)
operator|&&
operator|(
name|bytes
index|[
name|i
operator|+
literal|4
index|]
operator|==
literal|'E'
operator|||
name|bytes
index|[
name|i
operator|+
literal|4
index|]
operator|==
literal|'e'
operator|)
condition|)
block|{
name|booleanValue
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
comment|// No boolean value match for 5 char field.
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|batchIndex
index|]
operator|=
literal|true
expr_stmt|;
return|return;
block|}
block|}
elseif|else
if|if
condition|(
name|length
operator|==
literal|1
condition|)
block|{
name|byte
name|b
init|=
name|bytes
index|[
name|start
index|]
decl_stmt|;
if|if
condition|(
name|b
operator|==
literal|'1'
operator|||
name|b
operator|==
literal|'t'
operator|||
name|b
operator|==
literal|'T'
condition|)
block|{
name|booleanValue
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|b
operator|==
literal|'0'
operator|||
name|b
operator|==
literal|'f'
operator|||
name|b
operator|==
literal|'F'
condition|)
block|{
name|booleanValue
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
comment|// No boolean value match for extended 1 char field.
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|batchIndex
index|]
operator|=
literal|true
expr_stmt|;
return|return;
block|}
block|}
else|else
block|{
comment|// No boolean value match for other lengths.
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|batchIndex
index|]
operator|=
literal|true
expr_stmt|;
return|return;
block|}
name|outputColVector
operator|.
name|vector
index|[
name|batchIndex
index|]
operator|=
operator|(
name|booleanValue
condition|?
literal|1
else|:
literal|0
operator|)
expr_stmt|;
block|}
break|break;
case|case
name|BYTE
case|:
if|if
condition|(
operator|!
name|LazyUtils
operator|.
name|isNumberMaybe
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
condition|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|batchIndex
index|]
operator|=
literal|true
expr_stmt|;
return|return;
block|}
name|outputColVector
operator|.
name|vector
index|[
name|batchIndex
index|]
operator|=
name|LazyByte
operator|.
name|parseByte
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
literal|10
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
if|if
condition|(
operator|!
name|LazyUtils
operator|.
name|isNumberMaybe
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
condition|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|batchIndex
index|]
operator|=
literal|true
expr_stmt|;
return|return;
block|}
name|outputColVector
operator|.
name|vector
index|[
name|batchIndex
index|]
operator|=
name|LazyShort
operator|.
name|parseShort
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
literal|10
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT
case|:
if|if
condition|(
operator|!
name|LazyUtils
operator|.
name|isNumberMaybe
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
condition|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|batchIndex
index|]
operator|=
literal|true
expr_stmt|;
return|return;
block|}
name|outputColVector
operator|.
name|vector
index|[
name|batchIndex
index|]
operator|=
name|LazyInteger
operator|.
name|parseInt
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
literal|10
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
if|if
condition|(
operator|!
name|LazyUtils
operator|.
name|isNumberMaybe
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
condition|)
block|{
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|batchIndex
index|]
operator|=
literal|true
expr_stmt|;
return|return;
block|}
name|outputColVector
operator|.
name|vector
index|[
name|batchIndex
index|]
operator|=
name|LazyLong
operator|.
name|parseLong
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
literal|10
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|Error
argument_list|(
literal|"Unexpected primitive category "
operator|+
name|integerPrimitiveCategory
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// for any exception in conversion to integer, produce NULL
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isNull
index|[
name|batchIndex
index|]
operator|=
literal|true
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
name|BytesColumnVector
name|inputColVector
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|inputColumn
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
if|if
condition|(
name|n
operator|==
literal|0
condition|)
block|{
comment|// Nothing to do
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
name|func
argument_list|(
name|outputColVector
argument_list|,
name|inputColVector
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
name|func
argument_list|(
name|outputColVector
argument_list|,
name|inputColVector
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
name|func
argument_list|(
name|outputColVector
argument_list|,
name|inputColVector
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
name|func
argument_list|(
name|outputColVector
argument_list|,
name|inputColVector
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
comment|/* there are NULLs in the inputColVector */
block|{
comment|/*        * Do careful maintenance of the outputColVector.noNulls flag.        */
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
operator|!
name|inputColVector
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
comment|// Set isNull before call in case it changes it mind.
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|func
argument_list|(
name|outputColVector
argument_list|,
name|inputColVector
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
else|else
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
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
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
operator|!
name|inputColVector
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
comment|// Set isNull before call in case it changes it mind.
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|func
argument_list|(
name|outputColVector
argument_list|,
name|inputColVector
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
else|else
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
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
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
name|inputColumn
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
literal|1
argument_list|)
operator|.
name|setArgumentTypes
argument_list|(
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

