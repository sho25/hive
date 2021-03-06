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
name|udf
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
name|Description
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
name|UDF
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
name|IntWritable
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

begin_comment
comment|/**  * UDFConv.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"conv"
argument_list|,
name|value
operator|=
literal|"_FUNC_(num, from_base, to_base) - convert num from from_base to"
operator|+
literal|" to_base"
argument_list|,
name|extended
operator|=
literal|"If to_base is negative, treat num as a signed integer,"
operator|+
literal|"otherwise, treat it as an unsigned integer.\n"
operator|+
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_('100', 2, 10) FROM src LIMIT 1;\n"
operator|+
literal|"  '4'\n"
operator|+
literal|"> SELECT _FUNC_(-10, 16, -10) FROM src LIMIT 1;\n"
operator|+
literal|"  '16'"
argument_list|)
specifier|public
class|class
name|UDFConv
extends|extends
name|UDF
block|{
specifier|private
specifier|final
name|Text
name|result
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
literal|64
index|]
decl_stmt|;
comment|/**    * Divide x by m as if x is an unsigned 64-bit integer. Examples:    * unsignedLongDiv(-1, 2) == Long.MAX_VALUE unsignedLongDiv(6, 3) == 2    * unsignedLongDiv(0, 5) == 0    *    * @param x    *          is treated as unsigned    * @param m    *          is treated as signed    */
specifier|private
name|long
name|unsignedLongDiv
parameter_list|(
name|long
name|x
parameter_list|,
name|int
name|m
parameter_list|)
block|{
if|if
condition|(
name|x
operator|>=
literal|0
condition|)
block|{
return|return
name|x
operator|/
name|m
return|;
block|}
comment|// Let uval be the value of the unsigned long with the same bits as x
comment|// Two's complement => x = uval - 2*MAX - 2
comment|// => uval = x + 2*MAX + 2
comment|// Now, use the fact: (a+b)/c = a/c + b/c + (a%c+b%c)/c
return|return
name|x
operator|/
name|m
operator|+
literal|2
operator|*
operator|(
name|Long
operator|.
name|MAX_VALUE
operator|/
name|m
operator|)
operator|+
literal|2
operator|/
name|m
operator|+
operator|(
name|x
operator|%
name|m
operator|+
literal|2
operator|*
operator|(
name|Long
operator|.
name|MAX_VALUE
operator|%
name|m
operator|)
operator|+
literal|2
operator|%
name|m
operator|)
operator|/
name|m
return|;
block|}
comment|/**    * Decode val into value[].    *    * @param val    *          is treated as an unsigned 64-bit integer    * @param radix    *          must be between MIN_RADIX and MAX_RADIX    */
specifier|private
name|void
name|decode
parameter_list|(
name|long
name|val
parameter_list|,
name|int
name|radix
parameter_list|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|value
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|value
operator|.
name|length
operator|-
literal|1
init|;
name|val
operator|!=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|long
name|q
init|=
name|unsignedLongDiv
argument_list|(
name|val
argument_list|,
name|radix
argument_list|)
decl_stmt|;
name|value
index|[
name|i
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|val
operator|-
name|q
operator|*
name|radix
argument_list|)
expr_stmt|;
name|val
operator|=
name|q
expr_stmt|;
block|}
block|}
comment|/**    * Convert value[] into a long. On overflow, return -1 (as mySQL does). If a    * negative digit is found, ignore the suffix starting there.    *    * @param radix    *          must be between MIN_RADIX and MAX_RADIX    * @param fromPos    *          is the first element that should be conisdered    * @return the result should be treated as an unsigned 64-bit integer.    */
specifier|private
name|long
name|encode
parameter_list|(
name|int
name|radix
parameter_list|,
name|int
name|fromPos
parameter_list|)
block|{
name|long
name|val
init|=
literal|0
decl_stmt|;
name|long
name|bound
init|=
name|unsignedLongDiv
argument_list|(
operator|-
literal|1
operator|-
name|radix
argument_list|,
name|radix
argument_list|)
decl_stmt|;
comment|// Possible overflow once
comment|// val
comment|// exceeds this value
for|for
control|(
name|int
name|i
init|=
name|fromPos
init|;
name|i
operator|<
name|value
operator|.
name|length
operator|&&
name|value
index|[
name|i
index|]
operator|>=
literal|0
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|val
operator|>=
name|bound
condition|)
block|{
comment|// Check for overflow
if|if
condition|(
name|unsignedLongDiv
argument_list|(
operator|-
literal|1
operator|-
name|value
index|[
name|i
index|]
argument_list|,
name|radix
argument_list|)
operator|<
name|val
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
block|}
name|val
operator|=
name|val
operator|*
name|radix
operator|+
name|value
index|[
name|i
index|]
expr_stmt|;
block|}
return|return
name|val
return|;
block|}
comment|/**    * Convert the bytes in value[] to the corresponding chars.    *    * @param radix    *          must be between MIN_RADIX and MAX_RADIX    * @param fromPos    *          is the first nonzero element    */
specifier|private
name|void
name|byte2char
parameter_list|(
name|int
name|radix
parameter_list|,
name|int
name|fromPos
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
name|fromPos
init|;
name|i
operator|<
name|value
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|value
index|[
name|i
index|]
operator|=
operator|(
name|byte
operator|)
name|Character
operator|.
name|toUpperCase
argument_list|(
name|Character
operator|.
name|forDigit
argument_list|(
name|value
index|[
name|i
index|]
argument_list|,
name|radix
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Convert the chars in value[] to the corresponding integers. Convert invalid    * characters to -1.    *    * @param radix    *          must be between MIN_RADIX and MAX_RADIX    * @param fromPos    *          is the first nonzero element    */
specifier|private
name|void
name|char2byte
parameter_list|(
name|int
name|radix
parameter_list|,
name|int
name|fromPos
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
name|fromPos
init|;
name|i
operator|<
name|value
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|value
index|[
name|i
index|]
operator|=
operator|(
name|byte
operator|)
name|Character
operator|.
name|digit
argument_list|(
name|value
index|[
name|i
index|]
argument_list|,
name|radix
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Convert numbers between different number bases. If toBase&gt;0 the result is    * unsigned, otherwise it is signed.    *    */
specifier|public
name|Text
name|evaluate
parameter_list|(
name|Text
name|n
parameter_list|,
name|IntWritable
name|fromBase
parameter_list|,
name|IntWritable
name|toBase
parameter_list|)
block|{
if|if
condition|(
name|n
operator|==
literal|null
operator|||
name|fromBase
operator|==
literal|null
operator|||
name|toBase
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|fromBs
init|=
name|fromBase
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
name|toBs
init|=
name|toBase
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|fromBs
argument_list|<
name|Character
operator|.
name|MIN_RADIX
operator|||
name|fromBs
argument_list|>
name|Character
operator|.
name|MAX_RADIX
operator|||
name|Math
operator|.
name|abs
argument_list|(
name|toBs
argument_list|)
operator|<
name|Character
operator|.
name|MIN_RADIX
operator|||
name|Math
operator|.
name|abs
argument_list|(
name|toBs
argument_list|)
operator|>
name|Character
operator|.
name|MAX_RADIX
condition|)
block|{
return|return
literal|null
return|;
block|}
name|byte
index|[]
name|num
init|=
name|n
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|boolean
name|negative
init|=
operator|(
name|num
index|[
literal|0
index|]
operator|==
literal|'-'
operator|)
decl_stmt|;
name|int
name|first
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|negative
condition|)
block|{
name|first
operator|=
literal|1
expr_stmt|;
block|}
comment|// Copy the digits in the right side of the array
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|n
operator|.
name|getLength
argument_list|()
operator|-
name|first
condition|;
name|i
operator|++
control|)
block|{
name|value
index|[
name|value
operator|.
name|length
operator|-
name|i
index|]
operator|=
name|num
index|[
name|n
operator|.
name|getLength
argument_list|()
operator|-
name|i
index|]
expr_stmt|;
block|}
name|char2byte
argument_list|(
name|fromBs
argument_list|,
name|value
operator|.
name|length
operator|-
name|n
operator|.
name|getLength
argument_list|()
operator|+
name|first
argument_list|)
expr_stmt|;
comment|// Do the conversion by going through a 64 bit integer
name|long
name|val
init|=
name|encode
argument_list|(
name|fromBs
argument_list|,
name|value
operator|.
name|length
operator|-
name|n
operator|.
name|getLength
argument_list|()
operator|+
name|first
argument_list|)
decl_stmt|;
if|if
condition|(
name|negative
operator|&&
name|toBs
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|val
operator|<
literal|0
condition|)
block|{
name|val
operator|=
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
name|val
operator|=
operator|-
name|val
expr_stmt|;
block|}
block|}
if|if
condition|(
name|toBs
operator|<
literal|0
operator|&&
name|val
operator|<
literal|0
condition|)
block|{
name|val
operator|=
operator|-
name|val
expr_stmt|;
name|negative
operator|=
literal|true
expr_stmt|;
block|}
name|decode
argument_list|(
name|val
argument_list|,
name|Math
operator|.
name|abs
argument_list|(
name|toBs
argument_list|)
argument_list|)
expr_stmt|;
comment|// Find the first non-zero digit or the last digits if all are zero.
for|for
control|(
name|first
operator|=
literal|0
init|;
name|first
operator|<
name|value
operator|.
name|length
operator|-
literal|1
operator|&&
name|value
index|[
name|first
index|]
operator|==
literal|0
condition|;
name|first
operator|++
control|)
block|{
empty_stmt|;
block|}
name|byte2char
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
name|toBs
argument_list|)
argument_list|,
name|first
argument_list|)
expr_stmt|;
if|if
condition|(
name|negative
operator|&&
name|toBs
operator|<
literal|0
condition|)
block|{
name|value
index|[
operator|--
name|first
index|]
operator|=
literal|'-'
expr_stmt|;
block|}
name|result
operator|.
name|set
argument_list|(
name|value
argument_list|,
name|first
argument_list|,
name|value
operator|.
name|length
operator|-
name|first
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

