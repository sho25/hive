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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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

begin_comment
comment|/**  * Math expression evaluation helper functions.  * Some of these are referenced from ColumnUnaryFunc.txt.  */
end_comment

begin_class
specifier|public
class|class
name|MathExpr
block|{
comment|// Round using the "half-up" method used in Hive.
specifier|public
specifier|static
name|double
name|round
parameter_list|(
name|double
name|d
parameter_list|)
block|{
if|if
condition|(
name|d
operator|>
literal|0.0
condition|)
block|{
return|return
call|(
name|double
call|)
argument_list|(
call|(
name|long
call|)
argument_list|(
name|d
operator|+
literal|0.5d
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
call|(
name|double
call|)
argument_list|(
call|(
name|long
call|)
argument_list|(
name|d
operator|-
literal|0.5d
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
name|double
name|log2
parameter_list|(
name|double
name|d
parameter_list|)
block|{
return|return
name|Math
operator|.
name|log
argument_list|(
name|d
argument_list|)
operator|/
name|Math
operator|.
name|log
argument_list|(
literal|2
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|long
name|abs
parameter_list|(
name|long
name|v
parameter_list|)
block|{
return|return
name|v
operator|>=
literal|0
condition|?
name|v
else|:
operator|-
name|v
return|;
block|}
specifier|public
specifier|static
name|double
name|sign
parameter_list|(
name|double
name|v
parameter_list|)
block|{
if|if
condition|(
name|v
operator|==
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
elseif|else
if|if
condition|(
name|v
operator|>
literal|0
condition|)
block|{
return|return
literal|1.0
return|;
block|}
else|else
block|{
return|return
operator|-
literal|1.0
return|;
block|}
block|}
specifier|public
specifier|static
name|double
name|sign
parameter_list|(
name|long
name|v
parameter_list|)
block|{
if|if
condition|(
name|v
operator|==
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
elseif|else
if|if
condition|(
name|v
operator|>
literal|0
condition|)
block|{
return|return
literal|1.0
return|;
block|}
else|else
block|{
return|return
operator|-
literal|1.0
return|;
block|}
block|}
comment|// for casting integral types to boolean
specifier|public
specifier|static
name|long
name|toBool
parameter_list|(
name|long
name|v
parameter_list|)
block|{
return|return
name|v
operator|==
literal|0
condition|?
literal|0
else|:
literal|1
return|;
block|}
comment|// for casting floating point types to boolean
specifier|public
specifier|static
name|long
name|toBool
parameter_list|(
name|double
name|v
parameter_list|)
block|{
return|return
name|v
operator|==
literal|0.0D
condition|?
literal|0L
else|:
literal|1L
return|;
block|}
comment|/* Convert an integer value in miliseconds since the epoch to a timestamp value    * for use in a long column vector, which is represented in nanoseconds since the epoch.    */
specifier|public
specifier|static
name|long
name|longToTimestamp
parameter_list|(
name|long
name|v
parameter_list|)
block|{
return|return
name|v
operator|*
literal|1000000
return|;
block|}
comment|// Convert seconds since the epoch (with fraction) to nanoseconds, as a long integer.
specifier|public
specifier|static
name|long
name|doubleToTimestamp
parameter_list|(
name|double
name|v
parameter_list|)
block|{
return|return
call|(
name|long
call|)
argument_list|(
name|v
operator|*
literal|1000000000.0
argument_list|)
return|;
block|}
comment|/* Convert an integer value representing a timestamp in nanoseconds to one    * that represents a timestamp in seconds (since the epoch).    */
specifier|public
specifier|static
name|long
name|fromTimestamp
parameter_list|(
name|long
name|v
parameter_list|)
block|{
return|return
name|v
operator|/
literal|1000000000
return|;
block|}
comment|/* Convert an integer value representing a timestamp in nanoseconds to one    * that represents a timestamp in seconds, with fraction, since the epoch.    */
specifier|public
specifier|static
name|double
name|fromTimestampToDouble
parameter_list|(
name|long
name|v
parameter_list|)
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|v
operator|)
operator|/
literal|1000000000.0
return|;
block|}
comment|/* Convert a long to a string. The string is output into the argument    * byte array, beginning at character 0. The length is returned.    */
specifier|public
specifier|static
name|int
name|writeLongToUTF8
parameter_list|(
name|byte
index|[]
name|result
parameter_list|,
name|long
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|result
index|[
literal|0
index|]
operator|=
literal|'0'
expr_stmt|;
return|return
literal|1
return|;
block|}
name|int
name|current
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|i
operator|<
literal|0
condition|)
block|{
name|result
index|[
name|current
operator|++
index|]
operator|=
literal|'-'
expr_stmt|;
block|}
else|else
block|{
comment|// negative range is bigger than positive range, so there is no risk
comment|// of overflow here.
name|i
operator|=
operator|-
name|i
expr_stmt|;
block|}
name|long
name|start
init|=
literal|1000000000000000000L
decl_stmt|;
while|while
condition|(
name|i
operator|/
name|start
operator|==
literal|0
condition|)
block|{
name|start
operator|/=
literal|10
expr_stmt|;
block|}
while|while
condition|(
name|start
operator|>
literal|0
condition|)
block|{
name|result
index|[
name|current
operator|++
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
literal|'0'
operator|-
operator|(
name|i
operator|/
name|start
operator|%
literal|10
operator|)
argument_list|)
expr_stmt|;
name|start
operator|/=
literal|10
expr_stmt|;
block|}
return|return
name|current
return|;
block|}
comment|// Convert all NaN values in vector v to NULL. Should only be used if n> 0.
specifier|public
specifier|static
name|void
name|NaNToNull
parameter_list|(
name|DoubleColumnVector
name|v
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
name|n
parameter_list|)
block|{
name|NaNToNull
argument_list|(
name|v
argument_list|,
name|sel
argument_list|,
name|selectedInUse
argument_list|,
name|n
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|// Convert all NaN, and optionally infinity values in vector v to NULL.
comment|// Should only be used if n> 0.
specifier|public
specifier|static
name|void
name|NaNToNull
parameter_list|(
name|DoubleColumnVector
name|v
parameter_list|,
name|int
index|[]
name|sel
parameter_list|,
name|boolean
name|selectedInUse
parameter_list|,
name|int
name|n
parameter_list|,
name|boolean
name|convertInfinity
parameter_list|)
block|{
comment|// handle repeating case
if|if
condition|(
name|v
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
operator|(
name|convertInfinity
operator|&&
name|Double
operator|.
name|isInfinite
argument_list|(
name|v
operator|.
name|vector
index|[
literal|0
index|]
argument_list|)
operator|)
operator|||
name|Double
operator|.
name|isNaN
argument_list|(
name|v
operator|.
name|vector
index|[
literal|0
index|]
argument_list|)
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
name|v
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|v
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
return|return;
block|}
if|if
condition|(
name|v
operator|.
name|noNulls
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
if|if
condition|(
operator|(
name|convertInfinity
operator|&&
name|Double
operator|.
name|isInfinite
argument_list|(
name|v
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
operator|)
operator|||
name|Double
operator|.
name|isNaN
argument_list|(
name|v
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|v
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
comment|// Must set isNull[i] to false to make sure
comment|// it gets initialized, in case we set noNulls to true.
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
operator|(
name|convertInfinity
operator|&&
name|Double
operator|.
name|isInfinite
argument_list|(
name|v
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
operator|)
operator|||
name|Double
operator|.
name|isNaN
argument_list|(
name|v
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
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
name|v
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|v
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
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
block|}
block|}
else|else
block|{
comment|// there are nulls, so null array entries are already initialized
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
operator|(
name|convertInfinity
operator|&&
name|Double
operator|.
name|isInfinite
argument_list|(
name|v
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
operator|)
operator|||
name|Double
operator|.
name|isNaN
argument_list|(
name|v
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
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
name|v
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
operator|(
name|convertInfinity
operator|&&
name|Double
operator|.
name|isInfinite
argument_list|(
name|v
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
operator|)
operator|||
name|Double
operator|.
name|isNaN
argument_list|(
name|v
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
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
name|v
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
block|}
block|}
block|}
block|}
end_class

end_unit

