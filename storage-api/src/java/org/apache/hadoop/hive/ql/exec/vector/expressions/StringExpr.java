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

begin_comment
comment|/**  * String expression evaluation helper functions.  */
end_comment

begin_class
specifier|public
class|class
name|StringExpr
block|{
comment|/* Compare two strings from two byte arrays each    * with their own start position and length.    * Use lexicographic unsigned byte value order.    * This is what's used for UTF-8 sort order.    * Return negative value if arg1< arg2, 0 if arg1 = arg2,    * positive if arg1> arg2.    */
specifier|public
specifier|static
name|int
name|compare
parameter_list|(
name|byte
index|[]
name|arg1
parameter_list|,
name|int
name|start1
parameter_list|,
name|int
name|len1
parameter_list|,
name|byte
index|[]
name|arg2
parameter_list|,
name|int
name|start2
parameter_list|,
name|int
name|len2
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
name|len1
operator|&&
name|i
operator|<
name|len2
condition|;
name|i
operator|++
control|)
block|{
comment|// Note the "& 0xff" is just a way to convert unsigned bytes to signed integer.
name|int
name|b1
init|=
name|arg1
index|[
name|i
operator|+
name|start1
index|]
operator|&
literal|0xff
decl_stmt|;
name|int
name|b2
init|=
name|arg2
index|[
name|i
operator|+
name|start2
index|]
operator|&
literal|0xff
decl_stmt|;
if|if
condition|(
name|b1
operator|!=
name|b2
condition|)
block|{
return|return
name|b1
operator|-
name|b2
return|;
block|}
block|}
return|return
name|len1
operator|-
name|len2
return|;
block|}
comment|/* Determine if two strings are equal from two byte arrays each    * with their own start position and length.    * Use lexicographic unsigned byte value order.    * This is what's used for UTF-8 sort order.    */
specifier|public
specifier|static
name|boolean
name|equal
parameter_list|(
name|byte
index|[]
name|arg1
parameter_list|,
specifier|final
name|int
name|start1
parameter_list|,
specifier|final
name|int
name|len1
parameter_list|,
name|byte
index|[]
name|arg2
parameter_list|,
specifier|final
name|int
name|start2
parameter_list|,
specifier|final
name|int
name|len2
parameter_list|)
block|{
if|if
condition|(
name|len1
operator|!=
name|len2
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|len1
operator|==
literal|0
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// do bounds check for OOB exception
if|if
condition|(
name|arg1
index|[
name|start1
index|]
operator|!=
name|arg2
index|[
name|start2
index|]
operator|||
name|arg1
index|[
name|start1
operator|+
name|len1
operator|-
literal|1
index|]
operator|!=
name|arg2
index|[
name|start2
operator|+
name|len2
operator|-
literal|1
index|]
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|len1
operator|==
name|len2
condition|)
block|{
comment|// prove invariant to the compiler: len1 = len2
comment|// all array access between (start1, start1+len1)
comment|// and (start2, start2+len2) are valid
comment|// no more OOB exceptions are possible
specifier|final
name|int
name|step
init|=
literal|8
decl_stmt|;
specifier|final
name|int
name|remainder
init|=
name|len1
operator|%
name|step
decl_stmt|;
specifier|final
name|int
name|wlen
init|=
name|len1
operator|-
name|remainder
decl_stmt|;
comment|// suffix first
for|for
control|(
name|int
name|i
init|=
name|wlen
init|;
name|i
operator|<
name|len1
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|arg1
index|[
name|start1
operator|+
name|i
index|]
operator|!=
name|arg2
index|[
name|start2
operator|+
name|i
index|]
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
comment|// SIMD loop
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|wlen
condition|;
name|i
operator|+=
name|step
control|)
block|{
specifier|final
name|int
name|s1
init|=
name|start1
operator|+
name|i
decl_stmt|;
specifier|final
name|int
name|s2
init|=
name|start2
operator|+
name|i
decl_stmt|;
name|boolean
name|neq
init|=
literal|false
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
name|step
condition|;
name|j
operator|++
control|)
block|{
name|neq
operator|=
operator|(
name|arg1
index|[
name|s1
operator|+
name|j
index|]
operator|!=
name|arg2
index|[
name|s2
operator|+
name|j
index|]
operator|)
operator|||
name|neq
expr_stmt|;
block|}
if|if
condition|(
name|neq
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
specifier|public
specifier|static
name|int
name|characterCount
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
block|{
name|int
name|end
init|=
name|bytes
operator|.
name|length
decl_stmt|;
comment|// count characters
name|int
name|j
init|=
literal|0
decl_stmt|;
name|int
name|charCount
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|j
operator|<
name|end
condition|)
block|{
comment|// UTF-8 continuation bytes have 2 high bits equal to 0x80.
if|if
condition|(
operator|(
name|bytes
index|[
name|j
index|]
operator|&
literal|0xc0
operator|)
operator|!=
literal|0x80
condition|)
block|{
operator|++
name|charCount
expr_stmt|;
block|}
name|j
operator|++
expr_stmt|;
block|}
return|return
name|charCount
return|;
block|}
specifier|public
specifier|static
name|int
name|characterCount
parameter_list|(
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
name|int
name|end
init|=
name|start
operator|+
name|length
decl_stmt|;
comment|// count characters
name|int
name|j
init|=
name|start
decl_stmt|;
name|int
name|charCount
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|j
operator|<
name|end
condition|)
block|{
comment|// UTF-8 continuation bytes have 2 high bits equal to 0x80.
if|if
condition|(
operator|(
name|bytes
index|[
name|j
index|]
operator|&
literal|0xc0
operator|)
operator|!=
literal|0x80
condition|)
block|{
operator|++
name|charCount
expr_stmt|;
block|}
name|j
operator|++
expr_stmt|;
block|}
return|return
name|charCount
return|;
block|}
specifier|public
specifier|static
name|void
name|padRight
parameter_list|(
name|BytesColumnVector
name|outV
parameter_list|,
name|int
name|i
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
parameter_list|,
name|int
name|maxCharacterLength
parameter_list|)
block|{
specifier|final
name|int
name|characterLength
init|=
name|StringExpr
operator|.
name|characterCount
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
decl_stmt|;
specifier|final
name|int
name|blankPadLength
init|=
name|Math
operator|.
name|max
argument_list|(
name|maxCharacterLength
operator|-
name|characterLength
argument_list|,
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|int
name|resultLength
init|=
name|length
operator|+
name|blankPadLength
decl_stmt|;
name|outV
operator|.
name|ensureValPreallocated
argument_list|(
name|resultLength
argument_list|)
expr_stmt|;
name|byte
index|[]
name|resultBytes
init|=
name|outV
operator|.
name|getValPreallocatedBytes
argument_list|()
decl_stmt|;
specifier|final
name|int
name|resultStart
init|=
name|outV
operator|.
name|getValPreallocatedStart
argument_list|()
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|resultBytes
argument_list|,
name|resultStart
argument_list|,
name|length
argument_list|)
expr_stmt|;
specifier|final
name|int
name|padEnd
init|=
name|resultStart
operator|+
name|resultLength
decl_stmt|;
for|for
control|(
name|int
name|p
init|=
name|resultStart
operator|+
name|length
init|;
name|p
operator|<
name|padEnd
condition|;
name|p
operator|++
control|)
block|{
name|resultBytes
index|[
name|p
index|]
operator|=
literal|' '
expr_stmt|;
block|}
name|outV
operator|.
name|setValPreallocated
argument_list|(
name|i
argument_list|,
name|resultLength
argument_list|)
expr_stmt|;
block|}
comment|// A setVal with the same function signature as rightTrim, leftTrim, truncate, etc, below.
comment|// Useful for class generation via templates.
specifier|public
specifier|static
name|void
name|assign
parameter_list|(
name|BytesColumnVector
name|outV
parameter_list|,
name|int
name|i
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
comment|// set output vector
name|outV
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
comment|/*    * Right trim a slice of a byte array and return the new byte length.    */
specifier|public
specifier|static
name|int
name|rightTrim
parameter_list|(
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
comment|// skip trailing blank characters
name|int
name|j
init|=
name|start
operator|+
name|length
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|j
operator|>=
name|start
operator|&&
name|bytes
index|[
name|j
index|]
operator|==
literal|0x20
condition|)
block|{
name|j
operator|--
expr_stmt|;
block|}
return|return
operator|(
name|j
operator|-
name|start
operator|)
operator|+
literal|1
return|;
block|}
comment|/*    * Right trim a slice of a byte array and place the result into element i of a vector.    */
specifier|public
specifier|static
name|void
name|rightTrim
parameter_list|(
name|BytesColumnVector
name|outV
parameter_list|,
name|int
name|i
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
comment|// skip trailing blank characters
name|int
name|j
init|=
name|start
operator|+
name|length
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|j
operator|>=
name|start
operator|&&
name|bytes
index|[
name|j
index|]
operator|==
literal|0x20
condition|)
block|{
name|j
operator|--
expr_stmt|;
block|}
comment|// set output vector
name|outV
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|bytes
argument_list|,
name|start
argument_list|,
operator|(
name|j
operator|-
name|start
operator|)
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/*    * Truncate a slice of a byte array to a maximum number of characters and    * return the new byte length.    */
specifier|public
specifier|static
name|int
name|truncate
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|,
name|int
name|maxLength
parameter_list|)
block|{
name|int
name|end
init|=
name|start
operator|+
name|length
decl_stmt|;
comment|// count characters forward
name|int
name|j
init|=
name|start
decl_stmt|;
name|int
name|charCount
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|j
operator|<
name|end
condition|)
block|{
comment|// UTF-8 continuation bytes have 2 high bits equal to 0x80.
if|if
condition|(
operator|(
name|bytes
index|[
name|j
index|]
operator|&
literal|0xc0
operator|)
operator|!=
literal|0x80
condition|)
block|{
if|if
condition|(
name|charCount
operator|==
name|maxLength
condition|)
block|{
break|break;
block|}
operator|++
name|charCount
expr_stmt|;
block|}
name|j
operator|++
expr_stmt|;
block|}
return|return
operator|(
name|j
operator|-
name|start
operator|)
return|;
block|}
comment|/*    * Truncate a slice of a byte array to a maximum number of characters and    * place the result into element i of a vector.    */
specifier|public
specifier|static
name|void
name|truncate
parameter_list|(
name|BytesColumnVector
name|outV
parameter_list|,
name|int
name|i
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
parameter_list|,
name|int
name|maxLength
parameter_list|)
block|{
name|int
name|end
init|=
name|start
operator|+
name|length
decl_stmt|;
comment|// count characters forward
name|int
name|j
init|=
name|start
decl_stmt|;
name|int
name|charCount
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|j
operator|<
name|end
condition|)
block|{
comment|// UTF-8 continuation bytes have 2 high bits equal to 0x80.
if|if
condition|(
operator|(
name|bytes
index|[
name|j
index|]
operator|&
literal|0xc0
operator|)
operator|!=
literal|0x80
condition|)
block|{
if|if
condition|(
name|charCount
operator|==
name|maxLength
condition|)
block|{
break|break;
block|}
operator|++
name|charCount
expr_stmt|;
block|}
name|j
operator|++
expr_stmt|;
block|}
comment|// set output vector
name|outV
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|bytes
argument_list|,
name|start
argument_list|,
operator|(
name|j
operator|-
name|start
operator|)
argument_list|)
expr_stmt|;
block|}
comment|/*    * Truncate a byte array to a maximum number of characters and    * return a byte array with only truncated bytes.    */
specifier|public
specifier|static
name|byte
index|[]
name|truncateScalar
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|maxLength
parameter_list|)
block|{
name|int
name|end
init|=
name|bytes
operator|.
name|length
decl_stmt|;
comment|// count characters forward
name|int
name|j
init|=
literal|0
decl_stmt|;
name|int
name|charCount
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|j
operator|<
name|end
condition|)
block|{
comment|// UTF-8 continuation bytes have 2 high bits equal to 0x80.
if|if
condition|(
operator|(
name|bytes
index|[
name|j
index|]
operator|&
literal|0xc0
operator|)
operator|!=
literal|0x80
condition|)
block|{
if|if
condition|(
name|charCount
operator|==
name|maxLength
condition|)
block|{
break|break;
block|}
operator|++
name|charCount
expr_stmt|;
block|}
name|j
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|j
operator|==
name|end
condition|)
block|{
return|return
name|bytes
return|;
block|}
else|else
block|{
return|return
name|Arrays
operator|.
name|copyOf
argument_list|(
name|bytes
argument_list|,
name|j
argument_list|)
return|;
block|}
block|}
comment|/*    * Right trim and truncate a slice of a byte array to a maximum number of characters and    * return the new byte length.    */
specifier|public
specifier|static
name|int
name|rightTrimAndTruncate
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|,
name|int
name|maxLength
parameter_list|)
block|{
name|int
name|end
init|=
name|start
operator|+
name|length
decl_stmt|;
comment|// count characters forward and watch for final run of pads
name|int
name|j
init|=
name|start
decl_stmt|;
name|int
name|charCount
init|=
literal|0
decl_stmt|;
name|int
name|padRunStart
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|j
operator|<
name|end
condition|)
block|{
comment|// UTF-8 continuation bytes have 2 high bits equal to 0x80.
if|if
condition|(
operator|(
name|bytes
index|[
name|j
index|]
operator|&
literal|0xc0
operator|)
operator|!=
literal|0x80
condition|)
block|{
if|if
condition|(
name|charCount
operator|==
name|maxLength
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|bytes
index|[
name|j
index|]
operator|==
literal|0x20
condition|)
block|{
if|if
condition|(
name|padRunStart
operator|==
operator|-
literal|1
condition|)
block|{
name|padRunStart
operator|=
name|j
expr_stmt|;
block|}
block|}
else|else
block|{
name|padRunStart
operator|=
operator|-
literal|1
expr_stmt|;
block|}
operator|++
name|charCount
expr_stmt|;
block|}
else|else
block|{
name|padRunStart
operator|=
operator|-
literal|1
expr_stmt|;
block|}
name|j
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|padRunStart
operator|!=
operator|-
literal|1
condition|)
block|{
return|return
operator|(
name|padRunStart
operator|-
name|start
operator|)
return|;
block|}
else|else
block|{
return|return
operator|(
name|j
operator|-
name|start
operator|)
return|;
block|}
block|}
comment|/*    * Right trim and truncate a slice of a byte array to a maximum number of characters and    * place the result into element i of a vector.    */
specifier|public
specifier|static
name|void
name|rightTrimAndTruncate
parameter_list|(
name|BytesColumnVector
name|outV
parameter_list|,
name|int
name|i
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
parameter_list|,
name|int
name|maxLength
parameter_list|)
block|{
name|int
name|end
init|=
name|start
operator|+
name|length
decl_stmt|;
comment|// count characters forward and watch for final run of pads
name|int
name|j
init|=
name|start
decl_stmt|;
name|int
name|charCount
init|=
literal|0
decl_stmt|;
name|int
name|padRunStart
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|j
operator|<
name|end
condition|)
block|{
comment|// UTF-8 continuation bytes have 2 high bits equal to 0x80.
if|if
condition|(
operator|(
name|bytes
index|[
name|j
index|]
operator|&
literal|0xc0
operator|)
operator|!=
literal|0x80
condition|)
block|{
if|if
condition|(
name|charCount
operator|==
name|maxLength
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|bytes
index|[
name|j
index|]
operator|==
literal|0x20
condition|)
block|{
if|if
condition|(
name|padRunStart
operator|==
operator|-
literal|1
condition|)
block|{
name|padRunStart
operator|=
name|j
expr_stmt|;
block|}
block|}
else|else
block|{
name|padRunStart
operator|=
operator|-
literal|1
expr_stmt|;
block|}
operator|++
name|charCount
expr_stmt|;
block|}
else|else
block|{
name|padRunStart
operator|=
operator|-
literal|1
expr_stmt|;
block|}
name|j
operator|++
expr_stmt|;
block|}
comment|// set output vector
if|if
condition|(
name|padRunStart
operator|!=
operator|-
literal|1
condition|)
block|{
name|outV
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|bytes
argument_list|,
name|start
argument_list|,
operator|(
name|padRunStart
operator|-
name|start
operator|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outV
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|bytes
argument_list|,
name|start
argument_list|,
operator|(
name|j
operator|-
name|start
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*    * Right trim and truncate a byte array to a maximum number of characters and    * return a byte array with only the trimmed and truncated bytes.    */
specifier|public
specifier|static
name|byte
index|[]
name|rightTrimAndTruncateScalar
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|maxLength
parameter_list|)
block|{
name|int
name|end
init|=
name|bytes
operator|.
name|length
decl_stmt|;
comment|// count characters forward and watch for final run of pads
name|int
name|j
init|=
literal|0
decl_stmt|;
name|int
name|charCount
init|=
literal|0
decl_stmt|;
name|int
name|padRunStart
init|=
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|j
operator|<
name|end
condition|)
block|{
comment|// UTF-8 continuation bytes have 2 high bits equal to 0x80.
if|if
condition|(
operator|(
name|bytes
index|[
name|j
index|]
operator|&
literal|0xc0
operator|)
operator|!=
literal|0x80
condition|)
block|{
if|if
condition|(
name|charCount
operator|==
name|maxLength
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|bytes
index|[
name|j
index|]
operator|==
literal|0x20
condition|)
block|{
if|if
condition|(
name|padRunStart
operator|==
operator|-
literal|1
condition|)
block|{
name|padRunStart
operator|=
name|j
expr_stmt|;
block|}
block|}
else|else
block|{
name|padRunStart
operator|=
operator|-
literal|1
expr_stmt|;
block|}
operator|++
name|charCount
expr_stmt|;
block|}
else|else
block|{
name|padRunStart
operator|=
operator|-
literal|1
expr_stmt|;
block|}
name|j
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|padRunStart
operator|!=
operator|-
literal|1
condition|)
block|{
return|return
name|Arrays
operator|.
name|copyOf
argument_list|(
name|bytes
argument_list|,
name|padRunStart
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|j
operator|==
name|end
condition|)
block|{
return|return
name|bytes
return|;
block|}
else|else
block|{
return|return
name|Arrays
operator|.
name|copyOf
argument_list|(
name|bytes
argument_list|,
name|j
argument_list|)
return|;
block|}
block|}
comment|/*    * Compiles the given pattern with a proper algorithm.    */
specifier|public
specifier|static
name|Finder
name|compile
parameter_list|(
name|byte
index|[]
name|pattern
parameter_list|)
block|{
return|return
operator|new
name|BoyerMooreHorspool
argument_list|(
name|pattern
argument_list|)
return|;
block|}
comment|/*    * A finder finds the first index of its pattern in a given byte array.    * Its thread-safety depends on its implementation.    */
specifier|public
interface|interface
name|Finder
block|{
name|int
name|find
parameter_list|(
name|byte
index|[]
name|input
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|len
parameter_list|)
function_decl|;
block|}
comment|/*    * StringExpr uses Boyer Moore Horspool algorithm to find faster.    * It is thread-safe, because it holds final member instances only.    * See https://en.wikipedia.org/wiki/Boyer–Moore–Horspool_algorithm .    */
specifier|private
specifier|static
class|class
name|BoyerMooreHorspool
implements|implements
name|Finder
block|{
specifier|private
specifier|static
specifier|final
name|int
name|MAX_BYTE
init|=
literal|0xff
decl_stmt|;
specifier|private
specifier|final
name|long
index|[]
name|shift
init|=
operator|new
name|long
index|[
name|MAX_BYTE
index|]
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|pattern
decl_stmt|;
specifier|private
specifier|final
name|int
name|plen
decl_stmt|;
specifier|public
name|BoyerMooreHorspool
parameter_list|(
name|byte
index|[]
name|pattern
parameter_list|)
block|{
name|this
operator|.
name|pattern
operator|=
name|pattern
expr_stmt|;
name|this
operator|.
name|plen
operator|=
name|pattern
operator|.
name|length
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|shift
argument_list|,
name|plen
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
name|plen
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|shift
index|[
name|pattern
index|[
name|i
index|]
operator|&
name|MAX_BYTE
index|]
operator|=
name|plen
operator|-
name|i
operator|-
literal|1
expr_stmt|;
block|}
block|}
specifier|public
name|int
name|find
parameter_list|(
name|byte
index|[]
name|input
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|len
parameter_list|)
block|{
if|if
condition|(
name|pattern
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
specifier|final
name|int
name|end
init|=
name|start
operator|+
name|len
decl_stmt|;
name|int
name|next
init|=
name|start
operator|+
name|plen
operator|-
literal|1
decl_stmt|;
specifier|final
name|int
name|plen
init|=
name|this
operator|.
name|plen
decl_stmt|;
specifier|final
name|byte
index|[]
name|pattern
init|=
name|this
operator|.
name|pattern
decl_stmt|;
while|while
condition|(
name|next
operator|<
name|end
condition|)
block|{
name|int
name|s_tmp
init|=
name|next
decl_stmt|;
name|int
name|p_tmp
init|=
name|plen
operator|-
literal|1
decl_stmt|;
while|while
condition|(
name|input
index|[
name|s_tmp
index|]
operator|==
name|pattern
index|[
name|p_tmp
index|]
condition|)
block|{
name|p_tmp
operator|--
expr_stmt|;
if|if
condition|(
name|p_tmp
operator|<
literal|0
condition|)
block|{
return|return
name|s_tmp
return|;
block|}
name|s_tmp
operator|--
expr_stmt|;
block|}
name|next
operator|+=
name|shift
index|[
name|input
index|[
name|next
index|]
operator|&
name|MAX_BYTE
index|]
expr_stmt|;
block|}
return|return
operator|-
literal|1
return|;
block|}
block|}
block|}
end_class

end_unit

