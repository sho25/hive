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
name|serde2
operator|.
name|lazy
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
name|serde2
operator|.
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyIntObjectInspector
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
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|HiveStringUtils
import|;
end_import

begin_comment
comment|/**  * LazyObject for storing a value of Integer.  *   *<p>  * Part of the code is adapted from Apache Harmony Project.  *   * As with the specification, this implementation relied on code laid out in<a  * href="http://www.hackersdelight.org/">Henry S. Warren, Jr.'s Hacker's  * Delight, (Addison Wesley, 2002)</a> as well as<a  * href="http://aggregate.org/MAGIC/">The Aggregate's Magic Algorithms</a>.  *</p>  *   */
end_comment

begin_class
specifier|public
class|class
name|LazyInteger
extends|extends
name|LazyPrimitive
argument_list|<
name|LazyIntObjectInspector
argument_list|,
name|IntWritable
argument_list|>
block|{
specifier|public
name|LazyInteger
parameter_list|(
name|LazyIntObjectInspector
name|oi
parameter_list|)
block|{
name|super
argument_list|(
name|oi
argument_list|)
expr_stmt|;
name|data
operator|=
operator|new
name|IntWritable
argument_list|()
expr_stmt|;
block|}
specifier|public
name|LazyInteger
parameter_list|(
name|LazyInteger
name|copy
parameter_list|)
block|{
name|super
argument_list|(
name|copy
argument_list|)
expr_stmt|;
name|data
operator|=
operator|new
name|IntWritable
argument_list|(
name|copy
operator|.
name|data
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|ByteArrayRef
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
block|{
if|if
condition|(
operator|!
name|LazyUtils
operator|.
name|isNumberMaybe
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
condition|)
block|{
name|isNull
operator|=
literal|true
expr_stmt|;
return|return;
block|}
try|try
block|{
name|data
operator|.
name|set
argument_list|(
name|parseInt
argument_list|(
name|bytes
operator|.
name|getData
argument_list|()
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|isNull
operator|=
literal|false
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
name|isNull
operator|=
literal|true
expr_stmt|;
name|logExceptionMessage
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
literal|"INT"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Parses the string argument as if it was an int value and returns the    * result. Throws NumberFormatException if the string does not represent an    * int quantity.    *     * @param bytes    * @param start    * @param length    *          a UTF-8 encoded string representation of an int quantity.    * @return int the value represented by the argument    * @exception NumberFormatException    *              if the argument could not be parsed as an int quantity.    */
specifier|public
specifier|static
name|int
name|parseInt
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
return|return
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
return|;
block|}
comment|/**    * Parses the string argument as if it was an int value and returns the    * result. Throws NumberFormatException if the string does not represent an    * int quantity. The second argument specifies the radix to use when parsing    * the value.    *     * @param bytes    * @param start    * @param length    *          a UTF-8 encoded string representation of an int quantity.    * @param radix    *          the base to use for conversion.    * @return the value represented by the argument    * @exception NumberFormatException    *              if the argument could not be parsed as an int quantity.    */
specifier|public
specifier|static
name|int
name|parseInt
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
name|radix
parameter_list|)
block|{
return|return
name|parseInt
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
name|radix
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Parses the string argument as if it was an int value and returns the    * result. Throws NumberFormatException if the string does not represent an    * int quantity. The second argument specifies the radix to use when parsing    * the value.    *    * @param bytes    * @param start    * @param length    *          a UTF-8 encoded string representation of an int quantity.    * @param radix    *          the base to use for conversion.    * @param trim    *          whether to trim leading/trailing whitespace    * @return the value represented by the argument    * @exception NumberFormatException    *              if the argument could not be parsed as an int quantity.    */
specifier|public
specifier|static
name|int
name|parseInt
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
name|radix
parameter_list|,
name|boolean
name|trim
parameter_list|)
block|{
if|if
condition|(
name|bytes
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NumberFormatException
argument_list|(
literal|"String is null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|radix
argument_list|<
name|Character
operator|.
name|MIN_RADIX
operator|||
name|radix
argument_list|>
name|Character
operator|.
name|MAX_RADIX
condition|)
block|{
throw|throw
operator|new
name|NumberFormatException
argument_list|(
literal|"Invalid radix: "
operator|+
name|radix
argument_list|)
throw|;
block|}
if|if
condition|(
name|trim
condition|)
block|{
comment|// Handle leading/trailing whitespace
name|int
name|leadingSpaces
init|=
name|HiveStringUtils
operator|.
name|findLeadingSpaces
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
decl_stmt|;
name|int
name|trailingSpaces
init|=
name|HiveStringUtils
operator|.
name|findTrailingSpaces
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
decl_stmt|;
name|start
operator|=
name|start
operator|+
name|leadingSpaces
expr_stmt|;
comment|// min() needed in the case that entire string is whitespace
name|length
operator|=
name|length
operator|-
name|Math
operator|.
name|min
argument_list|(
name|length
argument_list|,
name|leadingSpaces
operator|+
name|trailingSpaces
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|NumberFormatException
argument_list|(
literal|"Empty string!"
argument_list|)
throw|;
block|}
name|int
name|offset
init|=
name|start
decl_stmt|;
name|boolean
name|negative
init|=
name|bytes
index|[
name|start
index|]
operator|==
literal|'-'
decl_stmt|;
if|if
condition|(
name|negative
operator|||
name|bytes
index|[
name|start
index|]
operator|==
literal|'+'
condition|)
block|{
name|offset
operator|++
expr_stmt|;
if|if
condition|(
name|length
operator|==
literal|1
condition|)
block|{
throw|throw
operator|new
name|NumberFormatException
argument_list|(
name|LazyUtils
operator|.
name|convertToString
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
argument_list|)
throw|;
block|}
block|}
return|return
name|parse
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|,
name|offset
argument_list|,
name|radix
argument_list|,
name|negative
argument_list|)
return|;
block|}
comment|/**    *     * @param bytes    * @param start    * @param length    *          a UTF-8 encoded string representation of an int quantity.    * @param radix    *          the base to use for conversion.    * @param offset    *          the starting position after the sign (if exists)    * @param radix    *          the base to use for conversion.    * @param negative    *          whether the number is negative.    * @return the value represented by the argument    * @exception NumberFormatException    *              if the argument could not be parsed as an int quantity.    */
specifier|private
specifier|static
name|int
name|parse
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
name|offset
parameter_list|,
name|int
name|radix
parameter_list|,
name|boolean
name|negative
parameter_list|)
block|{
name|byte
name|separator
init|=
literal|'.'
decl_stmt|;
name|int
name|max
init|=
name|Integer
operator|.
name|MIN_VALUE
operator|/
name|radix
decl_stmt|;
name|int
name|result
init|=
literal|0
decl_stmt|,
name|end
init|=
name|start
operator|+
name|length
decl_stmt|;
while|while
condition|(
name|offset
operator|<
name|end
condition|)
block|{
name|int
name|digit
init|=
name|LazyUtils
operator|.
name|digit
argument_list|(
name|bytes
index|[
name|offset
operator|++
index|]
argument_list|,
name|radix
argument_list|)
decl_stmt|;
if|if
condition|(
name|digit
operator|==
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|bytes
index|[
name|offset
operator|-
literal|1
index|]
operator|==
name|separator
condition|)
block|{
comment|// We allow decimals and will return a truncated integer in that case.
comment|// Therefore we won't throw an exception here (checking the fractional
comment|// part happens below.)
break|break;
block|}
throw|throw
operator|new
name|NumberFormatException
argument_list|(
name|LazyUtils
operator|.
name|convertToString
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|max
operator|>
name|result
condition|)
block|{
throw|throw
operator|new
name|NumberFormatException
argument_list|(
name|LazyUtils
operator|.
name|convertToString
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
argument_list|)
throw|;
block|}
name|int
name|next
init|=
name|result
operator|*
name|radix
operator|-
name|digit
decl_stmt|;
if|if
condition|(
name|next
operator|>
name|result
condition|)
block|{
throw|throw
operator|new
name|NumberFormatException
argument_list|(
name|LazyUtils
operator|.
name|convertToString
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
argument_list|)
throw|;
block|}
name|result
operator|=
name|next
expr_stmt|;
block|}
comment|// This is the case when we've encountered a decimal separator. The fractional
comment|// part will not change the number, but we will verify that the fractional part
comment|// is well formed.
while|while
condition|(
name|offset
operator|<
name|end
condition|)
block|{
name|int
name|digit
init|=
name|LazyUtils
operator|.
name|digit
argument_list|(
name|bytes
index|[
name|offset
operator|++
index|]
argument_list|,
name|radix
argument_list|)
decl_stmt|;
if|if
condition|(
name|digit
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|NumberFormatException
argument_list|(
name|LazyUtils
operator|.
name|convertToString
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
operator|!
name|negative
condition|)
block|{
name|result
operator|=
operator|-
name|result
expr_stmt|;
if|if
condition|(
name|result
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|NumberFormatException
argument_list|(
name|LazyUtils
operator|.
name|convertToString
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
argument_list|)
throw|;
block|}
block|}
return|return
name|result
return|;
block|}
comment|/**    * Writes out the text representation of an integer using base 10 to an    * OutputStream in UTF-8 encoding.    *     * Note: division by a constant (like 10) is much faster than division by a    * variable. That's one of the reasons that we don't make radix a parameter    * here.    *     * @param out    *          the outputstream to write to    * @param i    *          an int to write out    * @throws IOException    */
specifier|public
specifier|static
name|void
name|writeUTF8
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|int
name|i
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
literal|'0'
argument_list|)
expr_stmt|;
return|return;
block|}
name|boolean
name|negative
init|=
name|i
operator|<
literal|0
decl_stmt|;
if|if
condition|(
name|negative
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
literal|'-'
argument_list|)
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
name|int
name|start
init|=
literal|1000000000
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
name|out
operator|.
name|write
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
block|}
specifier|public
specifier|static
name|void
name|writeUTF8NoException
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|int
name|i
parameter_list|)
block|{
try|try
block|{
name|writeUTF8
argument_list|(
name|out
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

