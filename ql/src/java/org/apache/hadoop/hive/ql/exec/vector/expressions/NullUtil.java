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
name|IntervalDayTimeColumnVector
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
name|TimestampColumnVector
import|;
end_import

begin_comment
comment|/**  * Utility functions to handle null propagation.  */
end_comment

begin_class
specifier|public
class|class
name|NullUtil
block|{
comment|/**    * Set the data value for all NULL entries to the designated NULL_VALUE.    */
specifier|public
specifier|static
name|void
name|setNullDataEntriesLong
parameter_list|(
name|LongColumnVector
name|v
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|n
parameter_list|)
block|{
if|if
condition|(
name|v
operator|.
name|noNulls
condition|)
block|{
return|return;
block|}
elseif|else
if|if
condition|(
name|v
operator|.
name|isRepeating
operator|&&
name|v
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|v
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|LongColumnVector
operator|.
name|NULL_VALUE
expr_stmt|;
block|}
elseif|else
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|LongColumnVector
operator|.
name|NULL_VALUE
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|LongColumnVector
operator|.
name|NULL_VALUE
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Set the data value for all NULL entries to the designated NULL_VALUE.    */
specifier|public
specifier|static
name|void
name|setNullDataEntriesBytes
parameter_list|(
name|BytesColumnVector
name|v
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|n
parameter_list|)
block|{
if|if
condition|(
name|v
operator|.
name|noNulls
condition|)
block|{
return|return;
block|}
elseif|else
if|if
condition|(
name|v
operator|.
name|isRepeating
operator|&&
name|v
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|v
operator|.
name|vector
index|[
literal|0
index|]
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|null
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Set the data value for all NULL entries to the designated NULL_VALUE.    */
specifier|public
specifier|static
name|void
name|setNullDataEntriesTimestamp
parameter_list|(
name|TimestampColumnVector
name|v
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|n
parameter_list|)
block|{
if|if
condition|(
name|v
operator|.
name|noNulls
condition|)
block|{
return|return;
block|}
elseif|else
if|if
condition|(
name|v
operator|.
name|isRepeating
operator|&&
name|v
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|v
operator|.
name|setNullValue
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
elseif|else
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|v
operator|.
name|setNullValue
argument_list|(
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
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|v
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|v
operator|.
name|setNullValue
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Set the data value for all NULL entries to the designated NULL_VALUE.    */
specifier|public
specifier|static
name|void
name|setNullDataEntriesIntervalDayTime
parameter_list|(
name|IntervalDayTimeColumnVector
name|v
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|n
parameter_list|)
block|{
if|if
condition|(
name|v
operator|.
name|noNulls
condition|)
block|{
return|return;
block|}
elseif|else
if|if
condition|(
name|v
operator|.
name|isRepeating
operator|&&
name|v
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|v
operator|.
name|setNullValue
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
elseif|else
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|v
operator|.
name|setNullValue
argument_list|(
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
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|v
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|v
operator|.
name|setNullValue
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// for use by Column-Scalar and Scalar-Column arithmetic for null propagation
specifier|public
specifier|static
name|void
name|setNullOutputEntriesColScalar
parameter_list|(
name|ColumnVector
name|v
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|n
parameter_list|)
block|{
if|if
condition|(
name|v
operator|instanceof
name|DoubleColumnVector
condition|)
block|{
comment|// No need to set null data entries because the input NaN values
comment|// will automatically propagate to the output.
return|return;
block|}
elseif|else
if|if
condition|(
name|v
operator|instanceof
name|LongColumnVector
condition|)
block|{
name|setNullDataEntriesLong
argument_list|(
operator|(
name|LongColumnVector
operator|)
name|v
argument_list|,
name|selectedInUse
argument_list|,
name|sel
argument_list|,
name|n
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|v
operator|instanceof
name|TimestampColumnVector
condition|)
block|{
name|setNullDataEntriesTimestamp
argument_list|(
operator|(
name|TimestampColumnVector
operator|)
name|v
argument_list|,
name|selectedInUse
argument_list|,
name|sel
argument_list|,
name|n
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Set the data value for all NULL entries to NaN    */
specifier|public
specifier|static
name|void
name|setNullDataEntriesDouble
parameter_list|(
name|DoubleColumnVector
name|v
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|n
parameter_list|)
block|{
if|if
condition|(
name|v
operator|.
name|noNulls
condition|)
block|{
return|return;
block|}
elseif|else
if|if
condition|(
name|v
operator|.
name|isRepeating
operator|&&
name|v
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|v
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|DoubleColumnVector
operator|.
name|NULL_VALUE
expr_stmt|;
block|}
elseif|else
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|DoubleColumnVector
operator|.
name|NULL_VALUE
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|DoubleColumnVector
operator|.
name|NULL_VALUE
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Set all the entries for which denoms array contains zeroes to NULL; sets all the data    * values for NULL entries for DoubleColumnVector.NULL_VALUE.    */
specifier|public
specifier|static
name|void
name|setNullAndDivBy0DataEntriesDouble
parameter_list|(
name|DoubleColumnVector
name|v
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|n
parameter_list|,
name|LongColumnVector
name|denoms
parameter_list|)
block|{
assert|assert
name|v
operator|.
name|isRepeating
operator|||
operator|!
name|denoms
operator|.
name|isRepeating
assert|;
specifier|final
name|boolean
name|realNulls
init|=
operator|!
name|v
operator|.
name|noNulls
decl_stmt|;
name|v
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|long
index|[]
name|vector
init|=
name|denoms
operator|.
name|vector
decl_stmt|;
if|if
condition|(
name|v
operator|.
name|isRepeating
operator|&&
operator|(
name|v
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
operator|(
operator|(
name|realNulls
operator|&&
name|v
operator|.
name|isNull
index|[
literal|0
index|]
operator|)
operator|||
name|vector
index|[
literal|0
index|]
operator|==
literal|0
operator|)
operator|)
condition|)
block|{
name|v
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|DoubleColumnVector
operator|.
name|NULL_VALUE
expr_stmt|;
block|}
elseif|else
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|realNulls
operator|&&
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|)
operator|||
name|vector
index|[
name|i
index|]
operator|==
literal|0
operator|)
condition|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|DoubleColumnVector
operator|.
name|NULL_VALUE
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|realNulls
operator|&&
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|)
operator|||
name|vector
index|[
name|i
index|]
operator|==
literal|0
operator|)
condition|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|DoubleColumnVector
operator|.
name|NULL_VALUE
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Set all the entries for which denoms array contains zeroes to NULL; sets all the data    * values for NULL entries for DoubleColumnVector.NULL_VALUE.    */
specifier|public
specifier|static
name|void
name|setNullAndDivBy0DataEntriesDouble
parameter_list|(
name|DoubleColumnVector
name|v
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|n
parameter_list|,
name|DoubleColumnVector
name|denoms
parameter_list|)
block|{
assert|assert
name|v
operator|.
name|isRepeating
operator|||
operator|!
name|denoms
operator|.
name|isRepeating
assert|;
specifier|final
name|boolean
name|realNulls
init|=
operator|!
name|v
operator|.
name|noNulls
decl_stmt|;
name|v
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|double
index|[]
name|vector
init|=
name|denoms
operator|.
name|vector
decl_stmt|;
if|if
condition|(
name|v
operator|.
name|isRepeating
operator|&&
operator|(
name|v
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
operator|(
operator|(
name|realNulls
operator|&&
name|v
operator|.
name|isNull
index|[
literal|0
index|]
operator|)
operator|||
name|vector
index|[
literal|0
index|]
operator|==
literal|0
operator|)
operator|)
condition|)
block|{
name|v
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|DoubleColumnVector
operator|.
name|NULL_VALUE
expr_stmt|;
block|}
elseif|else
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|realNulls
operator|&&
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|)
operator|||
name|vector
index|[
name|i
index|]
operator|==
literal|0
operator|)
condition|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|DoubleColumnVector
operator|.
name|NULL_VALUE
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|realNulls
operator|&&
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|)
operator|||
name|vector
index|[
name|i
index|]
operator|==
literal|0
operator|)
condition|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|DoubleColumnVector
operator|.
name|NULL_VALUE
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Set all the entries for which denoms array contains zeroes to NULL; sets all the data    * values for NULL entries for LongColumnVector.NULL_VALUE.    */
specifier|public
specifier|static
name|void
name|setNullAndDivBy0DataEntriesLong
parameter_list|(
name|LongColumnVector
name|v
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|n
parameter_list|,
name|LongColumnVector
name|denoms
parameter_list|)
block|{
assert|assert
name|v
operator|.
name|isRepeating
operator|||
operator|!
name|denoms
operator|.
name|isRepeating
assert|;
name|v
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|long
index|[]
name|vector
init|=
name|denoms
operator|.
name|vector
decl_stmt|;
if|if
condition|(
name|v
operator|.
name|isRepeating
operator|&&
operator|(
name|v
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
operator|(
name|v
operator|.
name|isNull
index|[
literal|0
index|]
operator|||
name|vector
index|[
literal|0
index|]
operator|==
literal|0
operator|)
operator|)
condition|)
block|{
name|v
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|LongColumnVector
operator|.
name|NULL_VALUE
expr_stmt|;
block|}
elseif|else
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|=
operator|(
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|||
name|vector
index|[
name|i
index|]
operator|==
literal|0
operator|)
condition|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|LongColumnVector
operator|.
name|NULL_VALUE
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|=
operator|(
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|||
name|vector
index|[
name|i
index|]
operator|==
literal|0
operator|)
condition|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|LongColumnVector
operator|.
name|NULL_VALUE
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Set all the entries for which denoms array contains zeroes to NULL; sets all the data    * values for NULL entries for LongColumnVector.NULL_VALUE.    */
specifier|public
specifier|static
name|void
name|setNullAndDivBy0DataEntriesLong
parameter_list|(
name|LongColumnVector
name|v
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|n
parameter_list|,
name|DoubleColumnVector
name|denoms
parameter_list|)
block|{
assert|assert
name|v
operator|.
name|isRepeating
operator|||
operator|!
name|denoms
operator|.
name|isRepeating
assert|;
name|v
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|double
index|[]
name|vector
init|=
name|denoms
operator|.
name|vector
decl_stmt|;
if|if
condition|(
name|v
operator|.
name|isRepeating
operator|&&
operator|(
name|v
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
operator|(
name|v
operator|.
name|isNull
index|[
literal|0
index|]
operator|||
name|vector
index|[
literal|0
index|]
operator|==
literal|0
operator|)
operator|)
condition|)
block|{
name|v
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|LongColumnVector
operator|.
name|NULL_VALUE
expr_stmt|;
block|}
elseif|else
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|=
operator|(
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|||
name|vector
index|[
name|i
index|]
operator|==
literal|0
operator|)
condition|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|LongColumnVector
operator|.
name|NULL_VALUE
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|=
operator|(
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|||
name|vector
index|[
name|i
index|]
operator|==
literal|0
operator|)
condition|)
block|{
name|v
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|LongColumnVector
operator|.
name|NULL_VALUE
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/*    * Propagate null values for a two-input operator and set isRepeating and noNulls appropriately.    */
specifier|public
specifier|static
name|void
name|propagateNullsColCol
parameter_list|(
name|ColumnVector
name|inputColVector1
parameter_list|,
name|ColumnVector
name|inputColVector2
parameter_list|,
name|ColumnVector
name|outputColVector
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|n
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|)
block|{
comment|// We do not need to do a column reset since we are carefully changing the output.
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|inputColVector1
operator|.
name|noNulls
operator|&&
name|inputColVector2
operator|.
name|noNulls
condition|)
block|{
if|if
condition|(
name|inputColVector1
operator|.
name|isRepeating
operator|&&
name|inputColVector2
operator|.
name|isRepeating
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
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
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
name|outputColVector
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
name|Arrays
operator|.
name|fill
argument_list|(
name|outputColVector
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|n
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|inputColVector1
operator|.
name|noNulls
operator|&&
operator|!
name|inputColVector2
operator|.
name|noNulls
condition|)
block|{
if|if
condition|(
name|inputColVector1
operator|.
name|isRepeating
operator|&&
name|inputColVector2
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
operator|!
name|inputColVector2
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
operator|!
name|inputColVector2
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
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
name|outputColVector
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
name|Arrays
operator|.
name|fill
argument_list|(
name|outputColVector
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|n
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
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
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
comment|// Because every value will be NULL.
block|}
block|}
else|else
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
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputColVector2
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
name|inputColVector2
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|outputColVector
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|n
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|inputColVector1
operator|.
name|noNulls
operator|&&
name|inputColVector2
operator|.
name|noNulls
condition|)
block|{
if|if
condition|(
name|inputColVector1
operator|.
name|isRepeating
operator|&&
name|inputColVector2
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
operator|!
name|inputColVector1
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
operator|!
name|inputColVector1
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
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
name|outputColVector
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
name|Arrays
operator|.
name|fill
argument_list|(
name|outputColVector
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|n
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
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
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
comment|// Because every value will be NULL.
block|}
block|}
else|else
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
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputColVector1
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
name|inputColVector1
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|outputColVector
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|n
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|inputColVector1
operator|.
name|noNulls
operator|&&
operator|!
name|inputColVector2
operator|.
name|noNulls
condition|)
block|{
if|if
condition|(
name|inputColVector1
operator|.
name|isRepeating
operator|&&
name|inputColVector2
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
operator|!
name|inputColVector1
operator|.
name|isNull
index|[
literal|0
index|]
operator|&&
operator|!
name|inputColVector2
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
block|}
elseif|else
if|if
condition|(
name|inputColVector1
operator|.
name|isRepeating
operator|&&
operator|!
name|inputColVector2
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|inputColVector1
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
literal|true
expr_stmt|;
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
comment|// Because every value will be NULL.
block|}
else|else
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
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputColVector2
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
comment|// copy nulls from the non-repeating side
name|System
operator|.
name|arraycopy
argument_list|(
name|inputColVector2
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|outputColVector
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|n
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|inputColVector1
operator|.
name|isRepeating
operator|&&
name|inputColVector2
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|inputColVector2
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
literal|true
expr_stmt|;
name|outputColVector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|outputColVector
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
comment|// Because every value will be NULL.
block|}
else|else
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
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputColVector1
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
comment|// copy nulls from the non-repeating side
name|System
operator|.
name|arraycopy
argument_list|(
name|inputColVector1
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|outputColVector
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|n
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// neither side is repeating
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
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputColVector1
operator|.
name|isNull
index|[
name|i
index|]
operator|||
name|inputColVector2
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
name|outputColVector
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputColVector1
operator|.
name|isNull
index|[
name|i
index|]
operator|||
name|inputColVector2
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|/**    * Follow the convention that null decimal values are internally set to the smallest    * positive value available. Prevents accidental zero-divide later in expression    * evaluation.    */
specifier|public
specifier|static
name|void
name|setNullDataEntriesDecimal
parameter_list|(
name|DecimalColumnVector
name|v
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|n
parameter_list|)
block|{
if|if
condition|(
name|v
operator|.
name|noNulls
condition|)
block|{
return|return;
block|}
elseif|else
if|if
condition|(
name|v
operator|.
name|isRepeating
operator|&&
name|v
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|v
operator|.
name|setNullDataValue
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
elseif|else
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|v
operator|.
name|setNullDataValue
argument_list|(
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
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|v
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|v
operator|.
name|setNullDataValue
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// Initialize any entries that could be used in an output vector to have false for null value.
specifier|public
specifier|static
name|void
name|initOutputNullsToFalse
parameter_list|(
name|ColumnVector
name|v
parameter_list|,
name|boolean
name|isRepeating
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|n
parameter_list|)
block|{
if|if
condition|(
name|v
operator|.
name|isRepeating
condition|)
block|{
name|v
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
return|return;
block|}
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
name|v
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
name|Arrays
operator|.
name|fill
argument_list|(
name|v
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|n
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Filter out rows with null values. Return the number of rows in the batch.    */
specifier|public
specifier|static
name|int
name|filterNulls
parameter_list|(
name|ColumnVector
name|v
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|int
name|n
parameter_list|)
block|{
name|int
name|newSize
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|v
operator|.
name|noNulls
condition|)
block|{
comment|// no rows will be filtered
return|return
name|n
return|;
block|}
if|if
condition|(
name|v
operator|.
name|isRepeating
condition|)
block|{
comment|// all rows are filtered if repeating null, otherwise no rows are filtered
return|return
name|v
operator|.
name|isNull
index|[
literal|0
index|]
condition|?
literal|0
else|:
name|n
return|;
block|}
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|sel
index|[
name|newSize
operator|++
index|]
operator|=
name|i
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|sel
index|[
name|newSize
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
block|}
return|return
name|newSize
return|;
block|}
block|}
end_class

end_unit

