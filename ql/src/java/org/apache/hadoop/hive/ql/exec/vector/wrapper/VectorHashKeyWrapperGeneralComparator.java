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
name|wrapper
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|expressions
operator|.
name|StringExpr
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
name|util
operator|.
name|NullOrdering
import|;
end_import

begin_comment
comment|/**  * An implementation of {@link Comparator} to compare {@link VectorHashKeyWrapperBase} instances.  */
end_comment

begin_class
specifier|public
class|class
name|VectorHashKeyWrapperGeneralComparator
implements|implements
name|Comparator
argument_list|<
name|VectorHashKeyWrapperBase
argument_list|>
implements|,
name|Serializable
block|{
comment|/**    * Compare {@link VectorHashKeyWrapperBase} instances only by one column.    */
specifier|private
specifier|static
class|class
name|VectorHashKeyWrapperBaseComparator
implements|implements
name|Comparator
argument_list|<
name|VectorHashKeyWrapperBase
argument_list|>
implements|,
name|Serializable
block|{
specifier|private
specifier|final
name|int
name|keyIndex
decl_stmt|;
specifier|private
specifier|final
name|Comparator
argument_list|<
name|VectorHashKeyWrapperBase
argument_list|>
name|comparator
decl_stmt|;
specifier|private
specifier|final
name|int
name|nullResult
decl_stmt|;
name|VectorHashKeyWrapperBaseComparator
parameter_list|(
name|int
name|keyIndex
parameter_list|,
name|Comparator
argument_list|<
name|VectorHashKeyWrapperBase
argument_list|>
name|comparator
parameter_list|,
name|char
name|nullOrder
parameter_list|)
block|{
name|this
operator|.
name|keyIndex
operator|=
name|keyIndex
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
switch|switch
condition|(
name|NullOrdering
operator|.
name|fromSign
argument_list|(
name|nullOrder
argument_list|)
condition|)
block|{
case|case
name|NULLS_FIRST
case|:
name|this
operator|.
name|nullResult
operator|=
literal|1
expr_stmt|;
break|break;
default|default:
name|this
operator|.
name|nullResult
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|VectorHashKeyWrapperBase
name|o1
parameter_list|,
name|VectorHashKeyWrapperBase
name|o2
parameter_list|)
block|{
name|boolean
name|isNull1
init|=
name|o1
operator|.
name|isNull
argument_list|(
name|keyIndex
argument_list|)
decl_stmt|;
name|boolean
name|isNull2
init|=
name|o2
operator|.
name|isNull
argument_list|(
name|keyIndex
argument_list|)
decl_stmt|;
if|if
condition|(
name|isNull1
operator|&&
name|isNull2
condition|)
block|{
return|return
literal|0
return|;
block|}
if|if
condition|(
name|isNull1
condition|)
block|{
return|return
operator|-
name|nullResult
return|;
block|}
if|if
condition|(
name|isNull2
condition|)
block|{
return|return
name|nullResult
return|;
block|}
return|return
name|comparator
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|final
name|List
argument_list|<
name|VectorHashKeyWrapperBaseComparator
argument_list|>
name|comparators
decl_stmt|;
specifier|public
name|VectorHashKeyWrapperGeneralComparator
parameter_list|(
name|int
name|numberOfColumns
parameter_list|)
block|{
name|this
operator|.
name|comparators
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numberOfColumns
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addColumnComparator
parameter_list|(
name|int
name|keyIndex
parameter_list|,
name|int
name|columnTypeSpecificIndex
parameter_list|,
name|ColumnVector
operator|.
name|Type
name|columnVectorType
parameter_list|,
name|char
name|sortOrder
parameter_list|,
name|char
name|nullOrder
parameter_list|)
block|{
name|Comparator
argument_list|<
name|VectorHashKeyWrapperBase
argument_list|>
name|comparator
decl_stmt|;
switch|switch
condition|(
name|columnVectorType
condition|)
block|{
case|case
name|LONG
case|:
case|case
name|DECIMAL_64
case|:
name|comparator
operator|=
parameter_list|(
name|o1
parameter_list|,
name|o2
parameter_list|)
lambda|->
name|Long
operator|.
name|compare
argument_list|(
name|o1
operator|.
name|getLongValue
argument_list|(
name|columnTypeSpecificIndex
argument_list|)
argument_list|,
name|o2
operator|.
name|getLongValue
argument_list|(
name|columnTypeSpecificIndex
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|comparator
operator|=
parameter_list|(
name|o1
parameter_list|,
name|o2
parameter_list|)
lambda|->
name|Double
operator|.
name|compare
argument_list|(
name|o1
operator|.
name|getDoubleValue
argument_list|(
name|columnTypeSpecificIndex
argument_list|)
argument_list|,
name|o2
operator|.
name|getDoubleValue
argument_list|(
name|columnTypeSpecificIndex
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTES
case|:
name|comparator
operator|=
parameter_list|(
name|o1
parameter_list|,
name|o2
parameter_list|)
lambda|->
name|StringExpr
operator|.
name|compare
argument_list|(
name|o1
operator|.
name|getBytes
argument_list|(
name|columnTypeSpecificIndex
argument_list|)
argument_list|,
name|o1
operator|.
name|getByteStart
argument_list|(
name|columnTypeSpecificIndex
argument_list|)
argument_list|,
name|o1
operator|.
name|getByteLength
argument_list|(
name|columnTypeSpecificIndex
argument_list|)
argument_list|,
name|o2
operator|.
name|getBytes
argument_list|(
name|columnTypeSpecificIndex
argument_list|)
argument_list|,
name|o2
operator|.
name|getByteStart
argument_list|(
name|columnTypeSpecificIndex
argument_list|)
argument_list|,
name|o2
operator|.
name|getByteLength
argument_list|(
name|columnTypeSpecificIndex
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|comparator
operator|=
parameter_list|(
name|o1
parameter_list|,
name|o2
parameter_list|)
lambda|->
name|o1
operator|.
name|getDecimal
argument_list|(
name|columnTypeSpecificIndex
argument_list|)
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getDecimal
argument_list|(
name|columnTypeSpecificIndex
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|comparator
operator|=
parameter_list|(
name|o1
parameter_list|,
name|o2
parameter_list|)
lambda|->
name|o1
operator|.
name|getTimestamp
argument_list|(
name|columnTypeSpecificIndex
argument_list|)
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getTimestamp
argument_list|(
name|columnTypeSpecificIndex
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|INTERVAL_DAY_TIME
case|:
name|comparator
operator|=
parameter_list|(
name|o1
parameter_list|,
name|o2
parameter_list|)
lambda|->
name|o1
operator|.
name|getIntervalDayTime
argument_list|(
name|columnTypeSpecificIndex
argument_list|)
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|getIntervalDayTime
argument_list|(
name|columnTypeSpecificIndex
argument_list|)
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected column vector columnVectorType "
operator|+
name|columnVectorType
argument_list|)
throw|;
block|}
name|comparators
operator|.
name|add
argument_list|(
operator|new
name|VectorHashKeyWrapperBaseComparator
argument_list|(
name|keyIndex
argument_list|,
name|sortOrder
operator|==
literal|'-'
condition|?
name|comparator
operator|.
name|reversed
argument_list|()
else|:
name|comparator
argument_list|,
name|nullOrder
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|VectorHashKeyWrapperBase
name|o1
parameter_list|,
name|VectorHashKeyWrapperBase
name|o2
parameter_list|)
block|{
for|for
control|(
name|Comparator
argument_list|<
name|VectorHashKeyWrapperBase
argument_list|>
name|comparator
range|:
name|comparators
control|)
block|{
name|int
name|c
init|=
name|comparator
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
block|{
return|return
name|c
return|;
block|}
block|}
return|return
literal|0
return|;
block|}
block|}
end_class

end_unit

