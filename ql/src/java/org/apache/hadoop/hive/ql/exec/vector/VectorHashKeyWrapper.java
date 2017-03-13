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
package|;
end_package

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
name|HiveIntervalDayTime
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
name|KeyWrapper
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
name|ql
operator|.
name|util
operator|.
name|JavaDataModel
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

begin_comment
comment|/**  * A hash map key wrapper for vectorized processing.  * It stores the key values as primitives in arrays for each supported primitive type.  * This works in conjunction with  * {@link org.apache.hadoop.hive.ql.exec.VectorHashKeyWrapperBatch VectorHashKeyWrapperBatch}  * to hash vectorized processing units (batches).  */
end_comment

begin_class
specifier|public
class|class
name|VectorHashKeyWrapper
extends|extends
name|KeyWrapper
block|{
specifier|private
specifier|static
specifier|final
name|int
index|[]
name|EMPTY_INT_ARRAY
init|=
operator|new
name|int
index|[
literal|0
index|]
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
index|[]
name|EMPTY_LONG_ARRAY
init|=
operator|new
name|long
index|[
literal|0
index|]
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|double
index|[]
name|EMPTY_DOUBLE_ARRAY
init|=
operator|new
name|double
index|[
literal|0
index|]
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|EMPTY_BYTES_ARRAY
init|=
operator|new
name|byte
index|[
literal|0
index|]
index|[]
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HiveDecimalWritable
index|[]
name|EMPTY_DECIMAL_ARRAY
init|=
operator|new
name|HiveDecimalWritable
index|[
literal|0
index|]
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Timestamp
index|[]
name|EMPTY_TIMESTAMP_ARRAY
init|=
operator|new
name|Timestamp
index|[
literal|0
index|]
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HiveIntervalDayTime
index|[]
name|EMPTY_INTERVAL_DAY_TIME_ARRAY
init|=
operator|new
name|HiveIntervalDayTime
index|[
literal|0
index|]
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|VectorHashKeyWrapper
name|EMPTY_KEY_WRAPPER
init|=
operator|new
name|EmptyVectorHashKeyWrapper
argument_list|()
decl_stmt|;
specifier|private
name|long
index|[]
name|longValues
decl_stmt|;
specifier|private
name|double
index|[]
name|doubleValues
decl_stmt|;
specifier|private
name|byte
index|[]
index|[]
name|byteValues
decl_stmt|;
specifier|private
name|int
index|[]
name|byteStarts
decl_stmt|;
specifier|private
name|int
index|[]
name|byteLengths
decl_stmt|;
specifier|private
name|HiveDecimalWritable
index|[]
name|decimalValues
decl_stmt|;
specifier|private
name|Timestamp
index|[]
name|timestampValues
decl_stmt|;
specifier|private
name|HiveIntervalDayTime
index|[]
name|intervalDayTimeValues
decl_stmt|;
specifier|private
name|boolean
index|[]
name|isNull
decl_stmt|;
specifier|private
name|int
name|hashcode
decl_stmt|;
specifier|private
name|VectorHashKeyWrapper
parameter_list|(
name|int
name|longValuesCount
parameter_list|,
name|int
name|doubleValuesCount
parameter_list|,
name|int
name|byteValuesCount
parameter_list|,
name|int
name|decimalValuesCount
parameter_list|,
name|int
name|timestampValuesCount
parameter_list|,
name|int
name|intervalDayTimeValuesCount
parameter_list|)
block|{
name|longValues
operator|=
name|longValuesCount
operator|>
literal|0
condition|?
operator|new
name|long
index|[
name|longValuesCount
index|]
else|:
name|EMPTY_LONG_ARRAY
expr_stmt|;
name|doubleValues
operator|=
name|doubleValuesCount
operator|>
literal|0
condition|?
operator|new
name|double
index|[
name|doubleValuesCount
index|]
else|:
name|EMPTY_DOUBLE_ARRAY
expr_stmt|;
name|decimalValues
operator|=
name|decimalValuesCount
operator|>
literal|0
condition|?
operator|new
name|HiveDecimalWritable
index|[
name|decimalValuesCount
index|]
else|:
name|EMPTY_DECIMAL_ARRAY
expr_stmt|;
name|timestampValues
operator|=
name|timestampValuesCount
operator|>
literal|0
condition|?
operator|new
name|Timestamp
index|[
name|timestampValuesCount
index|]
else|:
name|EMPTY_TIMESTAMP_ARRAY
expr_stmt|;
name|intervalDayTimeValues
operator|=
name|intervalDayTimeValuesCount
operator|>
literal|0
condition|?
operator|new
name|HiveIntervalDayTime
index|[
name|intervalDayTimeValuesCount
index|]
else|:
name|EMPTY_INTERVAL_DAY_TIME_ARRAY
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
name|decimalValuesCount
condition|;
operator|++
name|i
control|)
block|{
name|decimalValues
index|[
name|i
index|]
operator|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|ZERO
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|byteValuesCount
operator|>
literal|0
condition|)
block|{
name|byteValues
operator|=
operator|new
name|byte
index|[
name|byteValuesCount
index|]
index|[]
expr_stmt|;
name|byteStarts
operator|=
operator|new
name|int
index|[
name|byteValuesCount
index|]
expr_stmt|;
name|byteLengths
operator|=
operator|new
name|int
index|[
name|byteValuesCount
index|]
expr_stmt|;
block|}
else|else
block|{
name|byteValues
operator|=
name|EMPTY_BYTES_ARRAY
expr_stmt|;
name|byteStarts
operator|=
name|EMPTY_INT_ARRAY
expr_stmt|;
name|byteLengths
operator|=
name|EMPTY_INT_ARRAY
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
name|timestampValuesCount
condition|;
operator|++
name|i
control|)
block|{
name|timestampValues
index|[
name|i
index|]
operator|=
operator|new
name|Timestamp
argument_list|(
literal|0
argument_list|)
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
name|intervalDayTimeValuesCount
condition|;
operator|++
name|i
control|)
block|{
name|intervalDayTimeValues
index|[
name|i
index|]
operator|=
operator|new
name|HiveIntervalDayTime
argument_list|()
expr_stmt|;
block|}
name|isNull
operator|=
operator|new
name|boolean
index|[
name|longValuesCount
operator|+
name|doubleValuesCount
operator|+
name|byteValuesCount
operator|+
name|decimalValuesCount
operator|+
name|timestampValuesCount
operator|+
name|intervalDayTimeValuesCount
index|]
expr_stmt|;
name|hashcode
operator|=
literal|0
expr_stmt|;
block|}
specifier|private
name|VectorHashKeyWrapper
parameter_list|()
block|{   }
specifier|public
specifier|static
name|VectorHashKeyWrapper
name|allocate
parameter_list|(
name|int
name|longValuesCount
parameter_list|,
name|int
name|doubleValuesCount
parameter_list|,
name|int
name|byteValuesCount
parameter_list|,
name|int
name|decimalValuesCount
parameter_list|,
name|int
name|timestampValuesCount
parameter_list|,
name|int
name|intervalDayTimeValuesCount
parameter_list|)
block|{
if|if
condition|(
operator|(
name|longValuesCount
operator|+
name|doubleValuesCount
operator|+
name|byteValuesCount
operator|+
name|decimalValuesCount
operator|+
name|timestampValuesCount
operator|+
name|intervalDayTimeValuesCount
operator|)
operator|==
literal|0
condition|)
block|{
return|return
name|EMPTY_KEY_WRAPPER
return|;
block|}
return|return
operator|new
name|VectorHashKeyWrapper
argument_list|(
name|longValuesCount
argument_list|,
name|doubleValuesCount
argument_list|,
name|byteValuesCount
argument_list|,
name|decimalValuesCount
argument_list|,
name|timestampValuesCount
argument_list|,
name|intervalDayTimeValuesCount
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|getNewKey
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Should not be called"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setHashKey
parameter_list|()
block|{
name|hashcode
operator|=
name|Arrays
operator|.
name|hashCode
argument_list|(
name|longValues
argument_list|)
operator|^
name|Arrays
operator|.
name|hashCode
argument_list|(
name|doubleValues
argument_list|)
operator|^
name|Arrays
operator|.
name|hashCode
argument_list|(
name|isNull
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
name|decimalValues
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// Use the new faster hash code since we are hashing memory objects.
name|hashcode
operator|^=
name|decimalValues
index|[
name|i
index|]
operator|.
name|newFasterHashCode
argument_list|()
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
name|timestampValues
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|hashcode
operator|^=
name|timestampValues
index|[
name|i
index|]
operator|.
name|hashCode
argument_list|()
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
name|intervalDayTimeValues
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|hashcode
operator|^=
name|intervalDayTimeValues
index|[
name|i
index|]
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
comment|// This code, with branches and all, is not executed if there are no string keys
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|byteValues
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
comment|/*        *  Hashing the string is potentially expensive so is better to branch.        *  Additionally not looking at values for nulls allows us not reset the values.        */
if|if
condition|(
operator|!
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|i
index|]
condition|)
block|{
name|byte
index|[]
name|bytes
init|=
name|byteValues
index|[
name|i
index|]
decl_stmt|;
name|int
name|start
init|=
name|byteStarts
index|[
name|i
index|]
decl_stmt|;
name|int
name|length
init|=
name|byteLengths
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|length
operator|==
name|bytes
operator|.
name|length
operator|&&
name|start
operator|==
literal|0
condition|)
block|{
name|hashcode
operator|^=
name|Arrays
operator|.
name|hashCode
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Unfortunately there is no Arrays.hashCode(byte[], start, length)
for|for
control|(
name|int
name|j
init|=
name|start
init|;
name|j
operator|<
name|start
operator|+
name|length
condition|;
operator|++
name|j
control|)
block|{
comment|// use 461 as is a (sexy!) prime.
name|hashcode
operator|^=
literal|461
operator|*
name|bytes
index|[
name|j
index|]
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|hashcode
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|that
parameter_list|)
block|{
if|if
condition|(
name|that
operator|instanceof
name|VectorHashKeyWrapper
condition|)
block|{
name|VectorHashKeyWrapper
name|keyThat
init|=
operator|(
name|VectorHashKeyWrapper
operator|)
name|that
decl_stmt|;
return|return
name|hashcode
operator|==
name|keyThat
operator|.
name|hashcode
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|longValues
argument_list|,
name|keyThat
operator|.
name|longValues
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|doubleValues
argument_list|,
name|keyThat
operator|.
name|doubleValues
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|decimalValues
argument_list|,
name|keyThat
operator|.
name|decimalValues
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|timestampValues
argument_list|,
name|keyThat
operator|.
name|timestampValues
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|intervalDayTimeValues
argument_list|,
name|keyThat
operator|.
name|intervalDayTimeValues
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|isNull
argument_list|,
name|keyThat
operator|.
name|isNull
argument_list|)
operator|&&
name|byteValues
operator|.
name|length
operator|==
name|keyThat
operator|.
name|byteValues
operator|.
name|length
operator|&&
operator|(
literal|0
operator|==
name|byteValues
operator|.
name|length
operator|||
name|bytesEquals
argument_list|(
name|keyThat
argument_list|)
operator|)
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|bytesEquals
parameter_list|(
name|VectorHashKeyWrapper
name|keyThat
parameter_list|)
block|{
comment|//By the time we enter here the byteValues.lentgh and isNull must have already been compared
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|byteValues
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
comment|// the byte comparison is potentially expensive so is better to branch on null
if|if
condition|(
operator|!
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|i
index|]
condition|)
block|{
if|if
condition|(
operator|!
name|StringExpr
operator|.
name|equal
argument_list|(
name|byteValues
index|[
name|i
index|]
argument_list|,
name|byteStarts
index|[
name|i
index|]
argument_list|,
name|byteLengths
index|[
name|i
index|]
argument_list|,
name|keyThat
operator|.
name|byteValues
index|[
name|i
index|]
argument_list|,
name|keyThat
operator|.
name|byteStarts
index|[
name|i
index|]
argument_list|,
name|keyThat
operator|.
name|byteLengths
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
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Object
name|clone
parameter_list|()
block|{
name|VectorHashKeyWrapper
name|clone
init|=
operator|new
name|VectorHashKeyWrapper
argument_list|()
decl_stmt|;
name|duplicateTo
argument_list|(
name|clone
argument_list|)
expr_stmt|;
return|return
name|clone
return|;
block|}
specifier|public
name|void
name|duplicateTo
parameter_list|(
name|VectorHashKeyWrapper
name|clone
parameter_list|)
block|{
name|clone
operator|.
name|longValues
operator|=
operator|(
name|longValues
operator|.
name|length
operator|>
literal|0
operator|)
condition|?
name|longValues
operator|.
name|clone
argument_list|()
else|:
name|EMPTY_LONG_ARRAY
expr_stmt|;
name|clone
operator|.
name|doubleValues
operator|=
operator|(
name|doubleValues
operator|.
name|length
operator|>
literal|0
operator|)
condition|?
name|doubleValues
operator|.
name|clone
argument_list|()
else|:
name|EMPTY_DOUBLE_ARRAY
expr_stmt|;
name|clone
operator|.
name|isNull
operator|=
name|isNull
operator|.
name|clone
argument_list|()
expr_stmt|;
if|if
condition|(
name|decimalValues
operator|.
name|length
operator|>
literal|0
condition|)
block|{
comment|// Decimal columns use HiveDecimalWritable.
name|clone
operator|.
name|decimalValues
operator|=
operator|new
name|HiveDecimalWritable
index|[
name|decimalValues
operator|.
name|length
index|]
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
name|decimalValues
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|clone
operator|.
name|decimalValues
index|[
name|i
index|]
operator|=
operator|new
name|HiveDecimalWritable
argument_list|(
name|decimalValues
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|clone
operator|.
name|decimalValues
operator|=
name|EMPTY_DECIMAL_ARRAY
expr_stmt|;
block|}
if|if
condition|(
name|byteLengths
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|clone
operator|.
name|byteValues
operator|=
operator|new
name|byte
index|[
name|byteValues
operator|.
name|length
index|]
index|[]
expr_stmt|;
name|clone
operator|.
name|byteStarts
operator|=
operator|new
name|int
index|[
name|byteValues
operator|.
name|length
index|]
expr_stmt|;
name|clone
operator|.
name|byteLengths
operator|=
name|byteLengths
operator|.
name|clone
argument_list|()
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
name|byteValues
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
comment|// avoid allocation/copy of nulls, because it potentially expensive.
comment|// branch instead.
if|if
condition|(
operator|!
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|i
index|]
condition|)
block|{
name|clone
operator|.
name|byteValues
index|[
name|i
index|]
operator|=
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|byteValues
index|[
name|i
index|]
argument_list|,
name|byteStarts
index|[
name|i
index|]
argument_list|,
name|byteStarts
index|[
name|i
index|]
operator|+
name|byteLengths
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|clone
operator|.
name|byteValues
operator|=
name|EMPTY_BYTES_ARRAY
expr_stmt|;
name|clone
operator|.
name|byteStarts
operator|=
name|EMPTY_INT_ARRAY
expr_stmt|;
name|clone
operator|.
name|byteLengths
operator|=
name|EMPTY_INT_ARRAY
expr_stmt|;
block|}
if|if
condition|(
name|timestampValues
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|clone
operator|.
name|timestampValues
operator|=
operator|new
name|Timestamp
index|[
name|timestampValues
operator|.
name|length
index|]
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
name|timestampValues
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|clone
operator|.
name|timestampValues
index|[
name|i
index|]
operator|=
operator|(
name|Timestamp
operator|)
name|timestampValues
index|[
name|i
index|]
operator|.
name|clone
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|clone
operator|.
name|timestampValues
operator|=
name|EMPTY_TIMESTAMP_ARRAY
expr_stmt|;
block|}
if|if
condition|(
name|intervalDayTimeValues
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|clone
operator|.
name|intervalDayTimeValues
operator|=
operator|new
name|HiveIntervalDayTime
index|[
name|intervalDayTimeValues
operator|.
name|length
index|]
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
name|intervalDayTimeValues
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|clone
operator|.
name|intervalDayTimeValues
index|[
name|i
index|]
operator|=
operator|(
name|HiveIntervalDayTime
operator|)
name|intervalDayTimeValues
index|[
name|i
index|]
operator|.
name|clone
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|clone
operator|.
name|intervalDayTimeValues
operator|=
name|EMPTY_INTERVAL_DAY_TIME_ARRAY
expr_stmt|;
block|}
name|clone
operator|.
name|hashcode
operator|=
name|hashcode
expr_stmt|;
assert|assert
name|clone
operator|.
name|equals
argument_list|(
name|this
argument_list|)
assert|;
block|}
annotation|@
name|Override
specifier|public
name|KeyWrapper
name|copyKey
parameter_list|()
block|{
return|return
operator|(
name|KeyWrapper
operator|)
name|clone
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|copyKey
parameter_list|(
name|KeyWrapper
name|oldWrapper
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Object
index|[]
name|getKeyArray
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|void
name|assignDouble
parameter_list|(
name|int
name|index
parameter_list|,
name|double
name|d
parameter_list|)
block|{
name|doubleValues
index|[
name|index
index|]
operator|=
name|d
expr_stmt|;
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|index
index|]
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|void
name|assignNullDouble
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|doubleValues
index|[
name|index
index|]
operator|=
literal|0
expr_stmt|;
comment|// assign 0 to simplify hashcode
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|index
index|]
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|assignLong
parameter_list|(
name|int
name|index
parameter_list|,
name|long
name|v
parameter_list|)
block|{
name|longValues
index|[
name|index
index|]
operator|=
name|v
expr_stmt|;
name|isNull
index|[
name|index
index|]
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|void
name|assignNullLong
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|longValues
index|[
name|index
index|]
operator|=
literal|0
expr_stmt|;
comment|// assign 0 to simplify hashcode
name|isNull
index|[
name|index
index|]
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|assignString
parameter_list|(
name|int
name|index
parameter_list|,
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|byteValues
index|[
name|index
index|]
operator|=
name|bytes
expr_stmt|;
name|byteStarts
index|[
name|index
index|]
operator|=
name|start
expr_stmt|;
name|byteLengths
index|[
name|index
index|]
operator|=
name|length
expr_stmt|;
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|index
index|]
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|void
name|assignNullString
parameter_list|(
name|int
name|index
parameter_list|)
block|{
comment|// We do not assign the value to byteValues[] because the value is never used on null
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|index
index|]
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|assignDecimal
parameter_list|(
name|int
name|index
parameter_list|,
name|HiveDecimalWritable
name|value
parameter_list|)
block|{
name|decimalValues
index|[
name|index
index|]
operator|.
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|byteValues
operator|.
name|length
operator|+
name|index
index|]
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|void
name|assignNullDecimal
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|byteValues
operator|.
name|length
operator|+
name|index
index|]
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|assignTimestamp
parameter_list|(
name|int
name|index
parameter_list|,
name|Timestamp
name|value
parameter_list|)
block|{
name|timestampValues
index|[
name|index
index|]
operator|=
name|value
expr_stmt|;
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|byteValues
operator|.
name|length
operator|+
name|decimalValues
operator|.
name|length
operator|+
name|index
index|]
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|void
name|assignTimestamp
parameter_list|(
name|int
name|index
parameter_list|,
name|TimestampColumnVector
name|colVector
parameter_list|,
name|int
name|elementNum
parameter_list|)
block|{
name|colVector
operator|.
name|timestampUpdate
argument_list|(
name|timestampValues
index|[
name|index
index|]
argument_list|,
name|elementNum
argument_list|)
expr_stmt|;
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|byteValues
operator|.
name|length
operator|+
name|decimalValues
operator|.
name|length
operator|+
name|index
index|]
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|void
name|assignNullTimestamp
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|byteValues
operator|.
name|length
operator|+
name|decimalValues
operator|.
name|length
operator|+
name|index
index|]
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|assignIntervalDayTime
parameter_list|(
name|int
name|index
parameter_list|,
name|HiveIntervalDayTime
name|value
parameter_list|)
block|{
name|intervalDayTimeValues
index|[
name|index
index|]
operator|.
name|set
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|byteValues
operator|.
name|length
operator|+
name|decimalValues
operator|.
name|length
operator|+
name|timestampValues
operator|.
name|length
operator|+
name|index
index|]
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|void
name|assignIntervalDayTime
parameter_list|(
name|int
name|index
parameter_list|,
name|IntervalDayTimeColumnVector
name|colVector
parameter_list|,
name|int
name|elementNum
parameter_list|)
block|{
name|intervalDayTimeValues
index|[
name|index
index|]
operator|.
name|set
argument_list|(
name|colVector
operator|.
name|asScratchIntervalDayTime
argument_list|(
name|elementNum
argument_list|)
argument_list|)
expr_stmt|;
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|byteValues
operator|.
name|length
operator|+
name|decimalValues
operator|.
name|length
operator|+
name|timestampValues
operator|.
name|length
operator|+
name|index
index|]
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|void
name|assignNullIntervalDayTime
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|byteValues
operator|.
name|length
operator|+
name|decimalValues
operator|.
name|length
operator|+
name|timestampValues
operator|.
name|length
operator|+
name|index
index|]
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%d[%s] %d[%s] %d[%s] %d[%s] %d[%s] %d[%s]"
argument_list|,
name|longValues
operator|.
name|length
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|longValues
argument_list|)
argument_list|,
name|doubleValues
operator|.
name|length
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|doubleValues
argument_list|)
argument_list|,
name|byteValues
operator|.
name|length
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|byteValues
argument_list|)
argument_list|,
name|decimalValues
operator|.
name|length
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|decimalValues
argument_list|)
argument_list|,
name|timestampValues
operator|.
name|length
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|timestampValues
argument_list|)
argument_list|,
name|intervalDayTimeValues
operator|.
name|length
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|intervalDayTimeValues
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|getIsLongNull
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|isNull
index|[
name|i
index|]
return|;
block|}
specifier|public
name|boolean
name|getIsDoubleNull
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|i
index|]
return|;
block|}
specifier|public
name|boolean
name|getIsBytesNull
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|i
index|]
return|;
block|}
specifier|public
name|long
name|getLongValue
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|longValues
index|[
name|i
index|]
return|;
block|}
specifier|public
name|double
name|getDoubleValue
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|doubleValues
index|[
name|i
index|]
return|;
block|}
specifier|public
name|byte
index|[]
name|getBytes
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|byteValues
index|[
name|i
index|]
return|;
block|}
specifier|public
name|int
name|getByteStart
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|byteStarts
index|[
name|i
index|]
return|;
block|}
specifier|public
name|int
name|getByteLength
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|byteLengths
index|[
name|i
index|]
return|;
block|}
specifier|public
name|int
name|getVariableSize
parameter_list|()
block|{
name|int
name|variableSize
init|=
literal|0
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
name|byteLengths
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|JavaDataModel
name|model
init|=
name|JavaDataModel
operator|.
name|get
argument_list|()
decl_stmt|;
name|variableSize
operator|+=
name|model
operator|.
name|lengthForByteArrayOfSize
argument_list|(
name|byteLengths
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|variableSize
return|;
block|}
specifier|public
name|boolean
name|getIsDecimalNull
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|byteValues
operator|.
name|length
operator|+
name|i
index|]
return|;
block|}
specifier|public
name|HiveDecimalWritable
name|getDecimal
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|decimalValues
index|[
name|i
index|]
return|;
block|}
specifier|public
name|boolean
name|getIsTimestampNull
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|byteValues
operator|.
name|length
operator|+
name|decimalValues
operator|.
name|length
operator|+
name|i
index|]
return|;
block|}
specifier|public
name|Timestamp
name|getTimestamp
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|timestampValues
index|[
name|i
index|]
return|;
block|}
specifier|public
name|boolean
name|getIsIntervalDayTimeNull
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|isNull
index|[
name|longValues
operator|.
name|length
operator|+
name|doubleValues
operator|.
name|length
operator|+
name|byteValues
operator|.
name|length
operator|+
name|decimalValues
operator|.
name|length
operator|+
name|timestampValues
operator|.
name|length
operator|+
name|i
index|]
return|;
block|}
specifier|public
name|HiveIntervalDayTime
name|getIntervalDayTime
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|intervalDayTimeValues
index|[
name|i
index|]
return|;
block|}
specifier|public
specifier|static
specifier|final
class|class
name|EmptyVectorHashKeyWrapper
extends|extends
name|VectorHashKeyWrapper
block|{
specifier|private
name|EmptyVectorHashKeyWrapper
parameter_list|()
block|{
name|super
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// no need to override assigns - all assign ops will fail due to 0 size
block|}
annotation|@
name|Override
specifier|protected
name|Object
name|clone
parameter_list|()
block|{
comment|// immutable
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|that
parameter_list|)
block|{
if|if
condition|(
name|that
operator|==
name|this
condition|)
block|{
comment|// should only be one object
return|return
literal|true
return|;
block|}
return|return
name|super
operator|.
name|equals
argument_list|(
name|that
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

