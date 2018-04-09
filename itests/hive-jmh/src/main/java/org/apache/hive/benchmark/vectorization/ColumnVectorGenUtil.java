begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|benchmark
operator|.
name|vectorization
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|common
operator|.
name|type
operator|.
name|Timestamp
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
name|DecimalColumnVector
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
name|DoubleColumnVector
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
name|RandomTypeUtil
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
name|HiveDecimalWritable
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
name|ObjectInspector
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
name|DecimalTypeInfo
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

begin_class
specifier|public
class|class
name|ColumnVectorGenUtil
block|{
specifier|private
specifier|static
specifier|final
name|long
name|LONG_VECTOR_NULL_VALUE
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|double
name|DOUBLE_VECTOR_NULL_VALUE
init|=
name|Double
operator|.
name|NaN
decl_stmt|;
specifier|public
specifier|static
name|VectorizedRowBatch
name|getVectorizedRowBatch
parameter_list|(
name|int
name|size
parameter_list|,
name|int
name|numCol
parameter_list|,
name|int
name|seed
parameter_list|)
block|{
name|VectorizedRowBatch
name|vrg
init|=
operator|new
name|VectorizedRowBatch
argument_list|(
name|numCol
argument_list|,
name|size
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numCol
condition|;
name|j
operator|++
control|)
block|{
name|LongColumnVector
name|lcv
init|=
operator|new
name|LongColumnVector
argument_list|(
name|size
argument_list|)
decl_stmt|;
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
name|lcv
operator|.
name|vector
index|[
name|i
index|]
operator|=
operator|(
name|i
operator|+
literal|1
operator|)
operator|*
name|seed
operator|*
operator|(
name|j
operator|+
literal|1
operator|)
expr_stmt|;
block|}
name|vrg
operator|.
name|cols
index|[
name|j
index|]
operator|=
name|lcv
expr_stmt|;
block|}
name|vrg
operator|.
name|size
operator|=
name|size
expr_stmt|;
return|return
name|vrg
return|;
block|}
specifier|public
specifier|static
name|ColumnVector
name|generateColumnVector
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|boolean
name|nulls
parameter_list|,
name|boolean
name|repeating
parameter_list|,
name|int
name|size
parameter_list|,
name|Random
name|rand
parameter_list|)
block|{
if|if
condition|(
name|typeInfo
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
argument_list|)
condition|)
block|{
switch|switch
condition|(
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
case|case
name|DATE
case|:
return|return
name|generateLongColumnVector
argument_list|(
name|nulls
argument_list|,
name|repeating
argument_list|,
name|size
argument_list|,
name|rand
argument_list|)
return|;
case|case
name|FLOAT
case|:
case|case
name|DOUBLE
case|:
return|return
name|generateDoubleColumnVector
argument_list|(
name|nulls
argument_list|,
name|repeating
argument_list|,
name|size
argument_list|,
name|rand
argument_list|)
return|;
case|case
name|DECIMAL
case|:
return|return
name|generateDecimalColumnVector
argument_list|(
operator|(
operator|(
name|DecimalTypeInfo
operator|)
name|typeInfo
operator|)
argument_list|,
name|nulls
argument_list|,
name|repeating
argument_list|,
name|size
argument_list|,
name|rand
argument_list|)
return|;
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
case|case
name|STRING
case|:
case|case
name|BINARY
case|:
return|return
name|generateBytesColumnVector
argument_list|(
name|nulls
argument_list|,
name|repeating
argument_list|,
name|size
argument_list|,
name|rand
argument_list|)
return|;
case|case
name|TIMESTAMP
case|:
return|return
name|generateTimestampColumnVector
argument_list|(
name|nulls
argument_list|,
name|repeating
argument_list|,
name|size
argument_list|,
name|rand
argument_list|)
return|;
comment|// TODO: add interval and complex types
block|}
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unsupported type info category: "
operator|+
name|typeInfo
operator|.
name|getCategory
argument_list|()
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|BytesColumnVector
name|generateBytesColumnVector
parameter_list|(
name|boolean
name|nulls
parameter_list|,
name|boolean
name|repeating
parameter_list|,
name|int
name|size
parameter_list|,
name|Random
name|rand
parameter_list|)
block|{
name|BytesColumnVector
name|bcv
init|=
operator|new
name|BytesColumnVector
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|bcv
operator|.
name|initBuffer
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|bcv
operator|.
name|noNulls
operator|=
operator|!
name|nulls
expr_stmt|;
name|bcv
operator|.
name|isRepeating
operator|=
name|repeating
expr_stmt|;
name|byte
index|[]
name|repeatingValue
init|=
operator|new
name|byte
index|[
literal|10
index|]
decl_stmt|;
name|rand
operator|.
name|nextBytes
argument_list|(
name|repeatingValue
argument_list|)
expr_stmt|;
name|int
name|nullFrequency
init|=
name|generateNullFrequency
argument_list|(
name|rand
argument_list|)
decl_stmt|;
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
name|nulls
operator|&&
operator|(
name|repeating
operator|||
name|i
operator|%
name|nullFrequency
operator|==
literal|0
operator|)
condition|)
block|{
name|bcv
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|bcv
operator|.
name|setVal
argument_list|(
literal|0
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|0
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|bcv
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|repeating
condition|)
block|{
name|bcv
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|repeatingValue
argument_list|,
literal|0
argument_list|,
name|repeatingValue
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|val
init|=
name|String
operator|.
name|valueOf
argument_list|(
literal|"value_"
operator|+
name|i
argument_list|)
decl_stmt|;
name|bcv
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|val
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|val
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|bcv
return|;
block|}
specifier|public
specifier|static
name|LongColumnVector
name|generateLongColumnVector
parameter_list|(
name|boolean
name|nulls
parameter_list|,
name|boolean
name|repeating
parameter_list|,
name|int
name|size
parameter_list|,
name|Random
name|rand
parameter_list|)
block|{
name|LongColumnVector
name|lcv
init|=
operator|new
name|LongColumnVector
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|lcv
operator|.
name|noNulls
operator|=
operator|!
name|nulls
expr_stmt|;
name|lcv
operator|.
name|isRepeating
operator|=
name|repeating
expr_stmt|;
name|long
name|repeatingValue
decl_stmt|;
do|do
block|{
name|repeatingValue
operator|=
name|rand
operator|.
name|nextLong
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|repeatingValue
operator|==
literal|0
condition|)
do|;
name|int
name|nullFrequency
init|=
name|generateNullFrequency
argument_list|(
name|rand
argument_list|)
decl_stmt|;
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
name|nulls
operator|&&
operator|(
name|repeating
operator|||
name|i
operator|%
name|nullFrequency
operator|==
literal|0
operator|)
condition|)
block|{
name|lcv
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|lcv
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|LONG_VECTOR_NULL_VALUE
expr_stmt|;
block|}
else|else
block|{
name|lcv
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|lcv
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|repeating
condition|?
name|repeatingValue
else|:
name|rand
operator|.
name|nextLong
argument_list|()
expr_stmt|;
if|if
condition|(
name|lcv
operator|.
name|vector
index|[
name|i
index|]
operator|==
literal|0
condition|)
block|{
name|i
operator|--
expr_stmt|;
block|}
block|}
block|}
return|return
name|lcv
return|;
block|}
specifier|private
specifier|static
name|ColumnVector
name|generateTimestampColumnVector
parameter_list|(
specifier|final
name|boolean
name|nulls
parameter_list|,
specifier|final
name|boolean
name|repeating
parameter_list|,
specifier|final
name|int
name|size
parameter_list|,
specifier|final
name|Random
name|rand
parameter_list|)
block|{
name|Timestamp
index|[]
name|timestamps
init|=
operator|new
name|Timestamp
index|[
name|size
index|]
decl_stmt|;
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
name|timestamps
index|[
name|i
index|]
operator|=
name|Timestamp
operator|.
name|ofEpochMilli
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|generateTimestampColumnVector
argument_list|(
name|nulls
argument_list|,
name|repeating
argument_list|,
name|size
argument_list|,
name|rand
argument_list|,
name|timestamps
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TimestampColumnVector
name|generateTimestampColumnVector
parameter_list|(
name|boolean
name|nulls
parameter_list|,
name|boolean
name|repeating
parameter_list|,
name|int
name|size
parameter_list|,
name|Random
name|rand
parameter_list|,
name|Timestamp
index|[]
name|timestampValues
parameter_list|)
block|{
name|TimestampColumnVector
name|tcv
init|=
operator|new
name|TimestampColumnVector
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|tcv
operator|.
name|noNulls
operator|=
operator|!
name|nulls
expr_stmt|;
name|tcv
operator|.
name|isRepeating
operator|=
name|repeating
expr_stmt|;
name|Timestamp
name|repeatingTimestamp
init|=
name|RandomTypeUtil
operator|.
name|getRandTimestamp
argument_list|(
name|rand
argument_list|)
decl_stmt|;
name|int
name|nullFrequency
init|=
name|generateNullFrequency
argument_list|(
name|rand
argument_list|)
decl_stmt|;
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
name|nulls
operator|&&
operator|(
name|repeating
operator|||
name|i
operator|%
name|nullFrequency
operator|==
literal|0
operator|)
condition|)
block|{
name|tcv
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|tcv
operator|.
name|setNullValue
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|timestampValues
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|tcv
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
if|if
condition|(
operator|!
name|repeating
condition|)
block|{
name|Timestamp
name|randomTimestamp
init|=
name|RandomTypeUtil
operator|.
name|getRandTimestamp
argument_list|(
name|rand
argument_list|)
decl_stmt|;
name|tcv
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|randomTimestamp
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|timestampValues
index|[
name|i
index|]
operator|=
name|randomTimestamp
expr_stmt|;
block|}
else|else
block|{
name|tcv
operator|.
name|set
argument_list|(
name|i
argument_list|,
name|repeatingTimestamp
operator|.
name|toSqlTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|timestampValues
index|[
name|i
index|]
operator|=
name|repeatingTimestamp
expr_stmt|;
block|}
block|}
block|}
return|return
name|tcv
return|;
block|}
specifier|public
specifier|static
name|DoubleColumnVector
name|generateDoubleColumnVector
parameter_list|(
name|boolean
name|nulls
parameter_list|,
name|boolean
name|repeating
parameter_list|,
name|int
name|size
parameter_list|,
name|Random
name|rand
parameter_list|)
block|{
name|DoubleColumnVector
name|dcv
init|=
operator|new
name|DoubleColumnVector
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|dcv
operator|.
name|noNulls
operator|=
operator|!
name|nulls
expr_stmt|;
name|dcv
operator|.
name|isRepeating
operator|=
name|repeating
expr_stmt|;
name|double
name|repeatingValue
decl_stmt|;
do|do
block|{
name|repeatingValue
operator|=
name|rand
operator|.
name|nextDouble
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|repeatingValue
operator|==
literal|0
condition|)
do|;
name|int
name|nullFrequency
init|=
name|generateNullFrequency
argument_list|(
name|rand
argument_list|)
decl_stmt|;
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
name|nulls
operator|&&
operator|(
name|repeating
operator|||
name|i
operator|%
name|nullFrequency
operator|==
literal|0
operator|)
condition|)
block|{
name|dcv
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|dcv
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|DOUBLE_VECTOR_NULL_VALUE
expr_stmt|;
block|}
else|else
block|{
name|dcv
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|dcv
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|repeating
condition|?
name|repeatingValue
else|:
name|rand
operator|.
name|nextDouble
argument_list|()
expr_stmt|;
if|if
condition|(
name|dcv
operator|.
name|vector
index|[
name|i
index|]
operator|==
literal|0
condition|)
block|{
name|i
operator|--
expr_stmt|;
block|}
block|}
block|}
return|return
name|dcv
return|;
block|}
specifier|public
specifier|static
name|DecimalColumnVector
name|generateDecimalColumnVector
parameter_list|(
name|DecimalTypeInfo
name|typeInfo
parameter_list|,
name|boolean
name|nulls
parameter_list|,
name|boolean
name|repeating
parameter_list|,
name|int
name|size
parameter_list|,
name|Random
name|rand
parameter_list|)
block|{
name|DecimalColumnVector
name|dcv
init|=
operator|new
name|DecimalColumnVector
argument_list|(
name|size
argument_list|,
name|typeInfo
operator|.
name|precision
argument_list|()
argument_list|,
name|typeInfo
operator|.
name|scale
argument_list|()
argument_list|)
decl_stmt|;
name|dcv
operator|.
name|noNulls
operator|=
operator|!
name|nulls
expr_stmt|;
name|dcv
operator|.
name|isRepeating
operator|=
name|repeating
expr_stmt|;
name|HiveDecimalWritable
name|repeatingValue
init|=
operator|new
name|HiveDecimalWritable
argument_list|()
decl_stmt|;
do|do
block|{
name|repeatingValue
operator|.
name|set
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|(
operator|(
name|Double
operator|)
name|rand
operator|.
name|nextDouble
argument_list|()
operator|)
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|setScale
argument_list|(
operator|(
name|short
operator|)
name|typeInfo
operator|.
name|scale
argument_list|()
argument_list|,
name|HiveDecimal
operator|.
name|ROUND_HALF_UP
argument_list|)
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|repeatingValue
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|doubleValue
argument_list|()
operator|==
literal|0
condition|)
do|;
name|int
name|nullFrequency
init|=
name|generateNullFrequency
argument_list|(
name|rand
argument_list|)
decl_stmt|;
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
name|nulls
operator|&&
operator|(
name|repeating
operator|||
name|i
operator|%
name|nullFrequency
operator|==
literal|0
operator|)
condition|)
block|{
name|dcv
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|dcv
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|dcv
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|repeating
condition|)
block|{
name|dcv
operator|.
name|vector
index|[
name|i
index|]
operator|.
name|set
argument_list|(
name|repeatingValue
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|dcv
operator|.
name|vector
index|[
name|i
index|]
operator|.
name|set
argument_list|(
name|HiveDecimal
operator|.
name|create
argument_list|(
operator|(
operator|(
name|Double
operator|)
name|rand
operator|.
name|nextDouble
argument_list|()
operator|)
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|setScale
argument_list|(
operator|(
name|short
operator|)
name|typeInfo
operator|.
name|scale
argument_list|()
argument_list|,
name|HiveDecimal
operator|.
name|ROUND_HALF_UP
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|dcv
operator|.
name|vector
index|[
name|i
index|]
operator|.
name|getHiveDecimal
argument_list|()
operator|.
name|doubleValue
argument_list|()
operator|==
literal|0
condition|)
block|{
name|i
operator|--
expr_stmt|;
block|}
block|}
block|}
return|return
name|dcv
return|;
block|}
specifier|private
specifier|static
name|int
name|generateNullFrequency
parameter_list|(
name|Random
name|rand
parameter_list|)
block|{
return|return
literal|60
operator|+
name|rand
operator|.
name|nextInt
argument_list|(
literal|20
argument_list|)
return|;
block|}
block|}
end_class

end_unit

