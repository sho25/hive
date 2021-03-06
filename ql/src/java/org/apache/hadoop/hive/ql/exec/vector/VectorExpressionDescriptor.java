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
package|;
end_package

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|serde
operator|.
name|serdeConstants
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
name|AnnotationUtils
import|;
end_import

begin_comment
comment|/**  * Describes a vector expression and encapsulates the {@link Mode}, number of arguments,  * argument types {@link ArgumentType} and expression types {@link InputExpressionType}.  */
end_comment

begin_class
specifier|public
class|class
name|VectorExpressionDescriptor
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
specifier|static
name|int
name|MAX_NUM_ARGUMENTS
init|=
literal|3
decl_stmt|;
comment|//
comment|// Special handling is needed at times for DATE, TIMESTAMP, (STRING), CHAR, and VARCHAR so they can
comment|// be named specifically as argument types.
comment|//
comment|// LongColumnVector -->
comment|//    INT_FAMILY
comment|//    DATE
comment|//    INTERVAL_FAMILY
comment|//
comment|// DoubleColumnVector -->
comment|//    FLOAT_FAMILY
comment|//
comment|// DecimalColumnVector -->
comment|//    DECIMAL
comment|//
comment|// BytesColumnVector -->
comment|//    STRING
comment|//    CHAR
comment|//    VARCHAR
comment|//
comment|// TimestampColumnVector -->
comment|//    TIMESTAMP
comment|//
comment|// IntervalDayTimeColumnVector -->
comment|//    INTERVAL_DAY_TIME
comment|//
specifier|public
enum|enum
name|ArgumentType
block|{
name|NONE
argument_list|(
literal|0x000000L
argument_list|)
block|,
name|INT_FAMILY
argument_list|(
literal|0x000001L
argument_list|)
block|,
name|FLOAT
argument_list|(
literal|0x000002L
argument_list|)
block|,
name|DOUBLE
argument_list|(
literal|0x000004L
argument_list|)
block|,
name|FLOAT_FAMILY
argument_list|(
name|FLOAT
operator|.
name|value
operator||
name|DOUBLE
operator|.
name|value
argument_list|)
block|,
name|DECIMAL
argument_list|(
literal|0x000008L
argument_list|)
block|,
name|STRING
argument_list|(
literal|0x000010L
argument_list|)
block|,
name|CHAR
argument_list|(
literal|0x000020L
argument_list|)
block|,
name|VARCHAR
argument_list|(
literal|0x000040L
argument_list|)
block|,
name|STRING_FAMILY
argument_list|(
name|STRING
operator|.
name|value
operator||
name|CHAR
operator|.
name|value
operator||
name|VARCHAR
operator|.
name|value
argument_list|)
block|,
name|DATE
argument_list|(
literal|0x000080L
argument_list|)
block|,
name|TIMESTAMP
argument_list|(
literal|0x000100L
argument_list|)
block|,
name|INTERVAL_YEAR_MONTH
argument_list|(
literal|0x000200L
argument_list|)
block|,
name|INTERVAL_DAY_TIME
argument_list|(
literal|0x000400L
argument_list|)
block|,
name|BINARY
argument_list|(
literal|0x000800L
argument_list|)
block|,
name|STRUCT
argument_list|(
literal|0x001000L
argument_list|)
block|,
name|DECIMAL_64
argument_list|(
literal|0x002000L
argument_list|)
block|,
name|LIST
argument_list|(
literal|0x004000L
argument_list|)
block|,
name|MAP
argument_list|(
literal|0x008000L
argument_list|)
block|,
name|VOID
argument_list|(
literal|0x010000L
argument_list|)
block|,
name|INT_DECIMAL_64_FAMILY
argument_list|(
name|INT_FAMILY
operator|.
name|value
operator||
name|DECIMAL_64
operator|.
name|value
argument_list|)
block|,
name|DATETIME_FAMILY
argument_list|(
name|DATE
operator|.
name|value
operator||
name|TIMESTAMP
operator|.
name|value
argument_list|)
block|,
name|INTERVAL_FAMILY
argument_list|(
name|INTERVAL_YEAR_MONTH
operator|.
name|value
operator||
name|INTERVAL_DAY_TIME
operator|.
name|value
argument_list|)
block|,
name|INT_INTERVAL_YEAR_MONTH
argument_list|(
name|INT_FAMILY
operator|.
name|value
operator||
name|INTERVAL_YEAR_MONTH
operator|.
name|value
argument_list|)
block|,
name|INT_DATE_INTERVAL_YEAR_MONTH
argument_list|(
name|INT_FAMILY
operator|.
name|value
operator||
name|DATE
operator|.
name|value
operator||
name|INTERVAL_YEAR_MONTH
operator|.
name|value
argument_list|)
block|,
name|STRING_DATETIME_FAMILY
argument_list|(
name|STRING_FAMILY
operator|.
name|value
operator||
name|DATETIME_FAMILY
operator|.
name|value
argument_list|)
block|,
name|STRING_FAMILY_BINARY
argument_list|(
name|STRING_FAMILY
operator|.
name|value
operator||
name|BINARY
operator|.
name|value
argument_list|)
block|,
name|STRING_BINARY
argument_list|(
name|STRING
operator|.
name|value
operator||
name|BINARY
operator|.
name|value
argument_list|)
block|,
name|ALL_FAMILY
argument_list|(
literal|0xFFFFFFL
argument_list|)
block|;
specifier|private
specifier|final
name|long
name|value
decl_stmt|;
name|ArgumentType
parameter_list|(
name|long
name|val
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|val
expr_stmt|;
block|}
specifier|public
name|long
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
specifier|public
specifier|static
name|ArgumentType
name|fromHiveTypeName
parameter_list|(
name|String
name|hiveTypeName
parameter_list|)
block|{
name|String
name|lower
init|=
name|hiveTypeName
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
if|if
condition|(
name|lower
operator|.
name|equals
argument_list|(
literal|"tinyint"
argument_list|)
operator|||
name|lower
operator|.
name|equals
argument_list|(
literal|"smallint"
argument_list|)
operator|||
name|lower
operator|.
name|equals
argument_list|(
literal|"int"
argument_list|)
operator|||
name|lower
operator|.
name|equals
argument_list|(
literal|"bigint"
argument_list|)
operator|||
name|lower
operator|.
name|equals
argument_list|(
literal|"boolean"
argument_list|)
operator|||
name|lower
operator|.
name|equals
argument_list|(
literal|"long"
argument_list|)
condition|)
block|{
return|return
name|INT_FAMILY
return|;
block|}
elseif|else
if|if
condition|(
name|lower
operator|.
name|equals
argument_list|(
literal|"double"
argument_list|)
operator|||
name|lower
operator|.
name|equals
argument_list|(
literal|"float"
argument_list|)
condition|)
block|{
return|return
name|FLOAT_FAMILY
return|;
block|}
elseif|else
if|if
condition|(
name|lower
operator|.
name|equals
argument_list|(
literal|"string"
argument_list|)
condition|)
block|{
return|return
name|STRING
return|;
block|}
elseif|else
if|if
condition|(
name|VectorizationContext
operator|.
name|charTypePattern
operator|.
name|matcher
argument_list|(
name|lower
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
name|CHAR
return|;
block|}
elseif|else
if|if
condition|(
name|VectorizationContext
operator|.
name|varcharTypePattern
operator|.
name|matcher
argument_list|(
name|lower
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
name|VARCHAR
return|;
block|}
elseif|else
if|if
condition|(
name|lower
operator|.
name|equals
argument_list|(
literal|"binary"
argument_list|)
condition|)
block|{
return|return
name|BINARY
return|;
block|}
elseif|else
if|if
condition|(
name|VectorizationContext
operator|.
name|decimalTypePattern
operator|.
name|matcher
argument_list|(
name|lower
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
name|DECIMAL
return|;
block|}
elseif|else
if|if
condition|(
name|lower
operator|.
name|equals
argument_list|(
literal|"timestamp"
argument_list|)
condition|)
block|{
return|return
name|TIMESTAMP
return|;
block|}
elseif|else
if|if
condition|(
name|lower
operator|.
name|equals
argument_list|(
literal|"date"
argument_list|)
condition|)
block|{
return|return
name|DATE
return|;
block|}
elseif|else
if|if
condition|(
name|lower
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|INTERVAL_YEAR_MONTH_TYPE_NAME
argument_list|)
condition|)
block|{
return|return
name|INTERVAL_YEAR_MONTH
return|;
block|}
elseif|else
if|if
condition|(
name|lower
operator|.
name|equals
argument_list|(
name|serdeConstants
operator|.
name|INTERVAL_DAY_TIME_TYPE_NAME
argument_list|)
condition|)
block|{
return|return
name|INTERVAL_DAY_TIME
return|;
block|}
elseif|else
if|if
condition|(
name|VectorizationContext
operator|.
name|structTypePattern
operator|.
name|matcher
argument_list|(
name|lower
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
name|STRUCT
return|;
block|}
elseif|else
if|if
condition|(
name|VectorizationContext
operator|.
name|listTypePattern
operator|.
name|matcher
argument_list|(
name|lower
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
name|LIST
return|;
block|}
elseif|else
if|if
condition|(
name|VectorizationContext
operator|.
name|mapTypePattern
operator|.
name|matcher
argument_list|(
name|lower
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
name|MAP
return|;
block|}
elseif|else
if|if
condition|(
name|lower
operator|.
name|equals
argument_list|(
literal|"void"
argument_list|)
condition|)
block|{
return|return
name|VOID
return|;
block|}
else|else
block|{
return|return
name|NONE
return|;
block|}
block|}
specifier|public
specifier|static
name|ArgumentType
name|getType
parameter_list|(
name|String
name|inType
parameter_list|)
block|{
if|if
condition|(
name|inType
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"long"
argument_list|)
condition|)
block|{
comment|// A synonym in some places in the code...
return|return
name|INT_FAMILY
return|;
block|}
elseif|else
if|if
condition|(
name|inType
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"double"
argument_list|)
condition|)
block|{
comment|// A synonym in some places in the code...
return|return
name|FLOAT_FAMILY
return|;
block|}
elseif|else
if|if
condition|(
name|VectorizationContext
operator|.
name|decimalTypePattern
operator|.
name|matcher
argument_list|(
name|inType
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
name|DECIMAL
return|;
block|}
elseif|else
if|if
condition|(
name|VectorizationContext
operator|.
name|charTypePattern
operator|.
name|matcher
argument_list|(
name|inType
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
name|CHAR
return|;
block|}
elseif|else
if|if
condition|(
name|VectorizationContext
operator|.
name|varcharTypePattern
operator|.
name|matcher
argument_list|(
name|inType
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
return|return
name|VARCHAR
return|;
block|}
return|return
name|valueOf
argument_list|(
name|inType
operator|.
name|toUpperCase
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|isSameTypeOrFamily
parameter_list|(
name|ArgumentType
name|other
parameter_list|)
block|{
return|return
operator|(
operator|(
name|value
operator|&
name|other
operator|.
name|value
operator|)
operator|!=
literal|0
operator|)
return|;
block|}
block|}
specifier|public
enum|enum
name|InputExpressionType
block|{
name|NONE
argument_list|(
literal|0
argument_list|)
block|,
name|COLUMN
argument_list|(
literal|1
argument_list|)
block|,
name|SCALAR
argument_list|(
literal|2
argument_list|)
block|,
name|DYNAMICVALUE
argument_list|(
literal|3
argument_list|)
block|,
name|NULLSCALAR
argument_list|(
literal|4
argument_list|)
block|;
specifier|private
specifier|final
name|int
name|value
decl_stmt|;
name|InputExpressionType
parameter_list|(
name|int
name|val
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|val
expr_stmt|;
block|}
specifier|public
name|int
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
block|}
specifier|public
enum|enum
name|Mode
block|{
name|PROJECTION
argument_list|(
literal|0
argument_list|)
block|,
name|FILTER
argument_list|(
literal|1
argument_list|)
block|;
specifier|private
specifier|final
name|int
name|value
decl_stmt|;
name|Mode
parameter_list|(
name|int
name|val
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|val
expr_stmt|;
block|}
specifier|public
name|int
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
block|}
comment|/**    * Builder builds a {@link Descriptor} object. Setter methods are provided to set the {@link Mode}, number    * of arguments, argument types and expression types for each argument.    */
specifier|public
specifier|static
class|class
name|Builder
block|{
specifier|private
name|Mode
name|mode
init|=
name|Mode
operator|.
name|PROJECTION
decl_stmt|;
name|ArgumentType
index|[]
name|argTypes
init|=
operator|new
name|ArgumentType
index|[
name|MAX_NUM_ARGUMENTS
index|]
decl_stmt|;
name|InputExpressionType
index|[]
name|exprTypes
init|=
operator|new
name|InputExpressionType
index|[
name|MAX_NUM_ARGUMENTS
index|]
decl_stmt|;
specifier|private
name|boolean
name|unscaled
decl_stmt|;
specifier|private
name|int
name|argCount
init|=
literal|0
decl_stmt|;
specifier|public
name|Builder
parameter_list|()
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
name|MAX_NUM_ARGUMENTS
condition|;
name|i
operator|++
control|)
block|{
name|argTypes
index|[
name|i
index|]
operator|=
name|ArgumentType
operator|.
name|NONE
expr_stmt|;
name|exprTypes
index|[
name|i
index|]
operator|=
name|InputExpressionType
operator|.
name|NONE
expr_stmt|;
block|}
block|}
specifier|public
name|Builder
name|setMode
parameter_list|(
name|Mode
name|m
parameter_list|)
block|{
name|this
operator|.
name|mode
operator|=
name|m
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setNumArguments
parameter_list|(
name|int
name|argCount
parameter_list|)
block|{
name|this
operator|.
name|argCount
operator|=
name|argCount
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setArgumentTypes
parameter_list|(
name|ArgumentType
modifier|...
name|types
parameter_list|)
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
name|types
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|argTypes
index|[
name|i
index|]
operator|=
name|types
index|[
name|i
index|]
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setArgumentTypes
parameter_list|(
name|String
modifier|...
name|types
parameter_list|)
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
name|types
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|argTypes
index|[
name|i
index|]
operator|=
name|ArgumentType
operator|.
name|getType
argument_list|(
name|types
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setArgumentType
parameter_list|(
name|int
name|index
parameter_list|,
name|ArgumentType
name|type
parameter_list|)
block|{
name|argTypes
index|[
name|index
index|]
operator|=
name|type
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setArgumentType
parameter_list|(
name|int
name|index
parameter_list|,
name|String
name|type
parameter_list|)
block|{
name|argTypes
index|[
name|index
index|]
operator|=
name|ArgumentType
operator|.
name|getType
argument_list|(
name|type
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setInputExpressionTypes
parameter_list|(
name|InputExpressionType
modifier|...
name|types
parameter_list|)
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
name|types
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|exprTypes
index|[
name|i
index|]
operator|=
name|types
index|[
name|i
index|]
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setInputExpressionType
parameter_list|(
name|int
name|index
parameter_list|,
name|InputExpressionType
name|type
parameter_list|)
block|{
name|exprTypes
index|[
name|index
index|]
operator|=
name|type
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Builder
name|setUnscaled
parameter_list|(
name|boolean
name|unscaled
parameter_list|)
block|{
name|this
operator|.
name|unscaled
operator|=
name|unscaled
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Descriptor
name|build
parameter_list|()
block|{
return|return
operator|new
name|Descriptor
argument_list|(
name|mode
argument_list|,
name|argCount
argument_list|,
name|argTypes
argument_list|,
name|exprTypes
argument_list|,
name|unscaled
argument_list|)
return|;
block|}
block|}
comment|/**    * Descriptor is immutable and is constructed by the {@link Builder} only. {@link #equals(Object)} is the only    * publicly exposed member which can be used to compare two descriptors.    */
specifier|public
specifier|static
specifier|final
class|class
name|Descriptor
block|{
specifier|public
name|boolean
name|matches
parameter_list|(
name|Descriptor
name|other
parameter_list|)
block|{
if|if
condition|(
operator|!
name|mode
operator|.
name|equals
argument_list|(
name|other
operator|.
name|mode
argument_list|)
operator|||
operator|(
name|argCount
operator|!=
name|other
operator|.
name|argCount
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|unscaled
operator|!=
name|other
operator|.
name|unscaled
condition|)
block|{
return|return
literal|false
return|;
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
name|argCount
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|argTypes
index|[
name|i
index|]
operator|.
name|isSameTypeOrFamily
argument_list|(
name|other
operator|.
name|argTypes
index|[
name|i
index|]
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|exprTypes
index|[
name|i
index|]
operator|.
name|equals
argument_list|(
name|other
operator|.
name|exprTypes
index|[
name|i
index|]
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
specifier|private
specifier|final
name|Mode
name|mode
decl_stmt|;
specifier|private
specifier|final
name|ArgumentType
index|[]
name|argTypes
decl_stmt|;
specifier|private
specifier|final
name|InputExpressionType
index|[]
name|exprTypes
decl_stmt|;
specifier|private
specifier|final
name|int
name|argCount
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|unscaled
decl_stmt|;
specifier|private
name|Descriptor
parameter_list|(
name|Mode
name|mode
parameter_list|,
name|int
name|argCount
parameter_list|,
name|ArgumentType
index|[]
name|argTypes
parameter_list|,
name|InputExpressionType
index|[]
name|exprTypes
parameter_list|,
name|boolean
name|unscaled
parameter_list|)
block|{
name|this
operator|.
name|mode
operator|=
name|mode
expr_stmt|;
name|this
operator|.
name|argTypes
operator|=
name|argTypes
operator|.
name|clone
argument_list|()
expr_stmt|;
name|this
operator|.
name|exprTypes
operator|=
name|exprTypes
operator|.
name|clone
argument_list|()
expr_stmt|;
name|this
operator|.
name|argCount
operator|=
name|argCount
expr_stmt|;
name|this
operator|.
name|unscaled
operator|=
name|unscaled
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|b
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"Argument Count = "
argument_list|)
decl_stmt|;
name|b
operator|.
name|append
argument_list|(
name|argCount
argument_list|)
expr_stmt|;
name|b
operator|.
name|append
argument_list|(
literal|", mode = "
argument_list|)
expr_stmt|;
name|b
operator|.
name|append
argument_list|(
name|mode
argument_list|)
expr_stmt|;
name|b
operator|.
name|append
argument_list|(
literal|", Argument Types = {"
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
operator|<
name|argCount
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|!=
literal|0
condition|)
block|{
name|b
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|b
operator|.
name|append
argument_list|(
name|argTypes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|b
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
name|b
operator|.
name|append
argument_list|(
literal|", Input Expression Types = {"
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
operator|<
name|argCount
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|!=
literal|0
condition|)
block|{
name|b
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|b
operator|.
name|append
argument_list|(
name|exprTypes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|b
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
return|return
name|b
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getVectorExpressionClass
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|udf
parameter_list|,
name|Descriptor
name|descriptor
parameter_list|,
name|boolean
name|useCheckedExpressionIfAvailable
parameter_list|)
throws|throws
name|HiveException
block|{
name|VectorizedExpressions
name|annotation
init|=
name|AnnotationUtils
operator|.
name|getAnnotation
argument_list|(
name|udf
argument_list|,
name|VectorizedExpressions
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|annotation
operator|==
literal|null
operator|||
name|annotation
operator|.
name|value
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Class
argument_list|<
name|?
extends|extends
name|VectorExpression
argument_list|>
index|[]
name|list
init|=
name|annotation
operator|.
name|value
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|VectorExpression
argument_list|>
name|matchedVe
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|VectorExpression
argument_list|>
name|ve
range|:
name|list
control|)
block|{
try|try
block|{
name|VectorExpression
name|candidateVe
init|=
name|ve
operator|.
name|newInstance
argument_list|()
decl_stmt|;
if|if
condition|(
name|candidateVe
operator|.
name|getDescriptor
argument_list|()
operator|.
name|matches
argument_list|(
name|descriptor
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|useCheckedExpressionIfAvailable
condition|)
block|{
comment|// no need to look further for a checked variant of this expression
return|return
name|ve
return|;
block|}
elseif|else
if|if
condition|(
name|candidateVe
operator|.
name|supportsCheckedExecution
argument_list|()
condition|)
block|{
return|return
name|ve
return|;
block|}
else|else
block|{
comment|// vector expression doesn't support checked execution
comment|// hold on to it in case there is no available checked variant
name|matchedVe
operator|=
name|ve
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Could not instantiate VectorExpression class "
operator|+
name|ve
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|matchedVe
operator|!=
literal|null
condition|)
block|{
return|return
name|matchedVe
return|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"getVectorExpressionClass udf "
operator|+
name|udf
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" descriptor: "
operator|+
name|descriptor
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|VectorExpression
argument_list|>
name|ve
range|:
name|list
control|)
block|{
try|try
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"getVectorExpressionClass doesn't match "
operator|+
name|ve
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" "
operator|+
name|ve
operator|.
name|newInstance
argument_list|()
operator|.
name|getDescriptor
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

