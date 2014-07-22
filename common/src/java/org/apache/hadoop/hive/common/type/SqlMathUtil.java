begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (c) Microsoft Corporation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|common
operator|.
name|type
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

begin_comment
comment|/**  * This code was originally written for Microsoft PolyBase.  *  * Misc utilities used in this package.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|SqlMathUtil
block|{
comment|/** Mask to convert a long to a negative long. */
specifier|public
specifier|static
specifier|final
name|long
name|NEGATIVE_LONG_MASK
init|=
literal|0x8000000000000000L
decl_stmt|;
comment|/** Mask to convert a long to an unsigned long. */
specifier|public
specifier|static
specifier|final
name|long
name|FULLBITS_63
init|=
literal|0x7FFFFFFFFFFFFFFFL
decl_stmt|;
comment|/** Mask to convert an int to a negative int. */
specifier|public
specifier|static
specifier|final
name|int
name|NEGATIVE_INT_MASK
init|=
literal|0x80000000
decl_stmt|;
comment|/** Mask to convert signed integer to unsigned long. */
specifier|public
specifier|static
specifier|final
name|long
name|LONG_MASK
init|=
literal|0xFFFFFFFFL
decl_stmt|;
comment|/** Mask to convert an int to an unsigned int. */
specifier|public
specifier|static
specifier|final
name|int
name|FULLBITS_31
init|=
literal|0x7FFFFFFF
decl_stmt|;
comment|/** Max unsigned integer. */
specifier|public
specifier|static
specifier|final
name|int
name|FULLBITS_32
init|=
literal|0xFFFFFFFF
decl_stmt|;
comment|/** 5^13 fits in 2^31. */
specifier|public
specifier|static
specifier|final
name|int
name|MAX_POWER_FIVE_INT31
init|=
literal|13
decl_stmt|;
comment|/** 5^x. All unsigned values. */
specifier|public
specifier|static
specifier|final
name|int
index|[]
name|POWER_FIVES_INT31
init|=
operator|new
name|int
index|[
name|MAX_POWER_FIVE_INT31
operator|+
literal|1
index|]
decl_stmt|;
comment|/** 5^27 fits in 2^63. */
specifier|public
specifier|static
specifier|final
name|int
name|MAX_POWER_FIVE_INT63
init|=
literal|27
decl_stmt|;
comment|/** 5^x. All unsigned values. */
specifier|public
specifier|static
specifier|final
name|long
index|[]
name|POWER_FIVES_INT63
init|=
operator|new
name|long
index|[
name|MAX_POWER_FIVE_INT63
operator|+
literal|1
index|]
decl_stmt|;
comment|/** 5^55 fits in 2^128. */
specifier|public
specifier|static
specifier|final
name|int
name|MAX_POWER_FIVE_INT128
init|=
literal|55
decl_stmt|;
comment|/** 5^x. */
specifier|public
specifier|static
specifier|final
name|UnsignedInt128
index|[]
name|POWER_FIVES_INT128
init|=
operator|new
name|UnsignedInt128
index|[
name|MAX_POWER_FIVE_INT128
operator|+
literal|1
index|]
decl_stmt|;
comment|/**    * 1/5^x, scaled to 128bits (in other words, 2^128/5^x). Because of flooring,    * this is same or smaller than real value.    */
specifier|public
specifier|static
specifier|final
name|UnsignedInt128
index|[]
name|INVERSE_POWER_FIVES_INT128
init|=
operator|new
name|UnsignedInt128
index|[
name|MAX_POWER_FIVE_INT128
operator|+
literal|1
index|]
decl_stmt|;
comment|/** 10^9 fits in 2^31. */
specifier|public
specifier|static
specifier|final
name|int
name|MAX_POWER_TEN_INT31
init|=
literal|9
decl_stmt|;
comment|/** 10^x. All unsigned values. */
specifier|public
specifier|static
specifier|final
name|int
index|[]
name|POWER_TENS_INT31
init|=
operator|new
name|int
index|[
name|MAX_POWER_TEN_INT31
operator|+
literal|1
index|]
decl_stmt|;
comment|/** 5 * 10^(x-1). */
specifier|public
specifier|static
specifier|final
name|int
index|[]
name|ROUND_POWER_TENS_INT31
init|=
operator|new
name|int
index|[
name|MAX_POWER_TEN_INT31
operator|+
literal|1
index|]
decl_stmt|;
comment|/** 10^38 fits in UnsignedInt128. */
specifier|public
specifier|static
specifier|final
name|int
name|MAX_POWER_TEN_INT128
init|=
literal|38
decl_stmt|;
comment|/** 10^x. */
specifier|public
specifier|static
specifier|final
name|UnsignedInt128
index|[]
name|POWER_TENS_INT128
init|=
operator|new
name|UnsignedInt128
index|[
name|MAX_POWER_TEN_INT128
operator|+
literal|1
index|]
decl_stmt|;
comment|/** 5 * 10^(x-1). */
specifier|public
specifier|static
specifier|final
name|UnsignedInt128
index|[]
name|ROUND_POWER_TENS_INT128
init|=
operator|new
name|UnsignedInt128
index|[
name|MAX_POWER_TEN_INT128
operator|+
literal|1
index|]
decl_stmt|;
comment|/**    * 1/10^x, scaled to 128bits, also word-shifted for better accuracy. Because    * of flooring, this is same or smaller than real value.    */
specifier|public
specifier|static
specifier|final
name|UnsignedInt128
index|[]
name|INVERSE_POWER_TENS_INT128
init|=
operator|new
name|UnsignedInt128
index|[
name|MAX_POWER_TEN_INT128
operator|+
literal|1
index|]
decl_stmt|;
comment|/** number of words shifted up in each INVERSE_POWER_TENS_INT128. */
specifier|public
specifier|static
specifier|final
name|int
index|[]
name|INVERSE_POWER_TENS_INT128_WORD_SHIFTS
init|=
operator|new
name|int
index|[
name|MAX_POWER_TEN_INT128
operator|+
literal|1
index|]
decl_stmt|;
comment|/** To quickly calculate bit length for up to 256. */
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|BIT_LENGTH
decl_stmt|;
comment|/** Used in division. */
specifier|private
specifier|static
specifier|final
name|long
name|BASE
init|=
operator|(
literal|1L
operator|<<
literal|32
operator|)
decl_stmt|;
comment|/**    * Turn on or off the highest bit of an int value.    *    * @param val    *          the value to modify    * @param positive    *          whether to turn off (positive) or on (negative).    * @return unsigned int value    */
specifier|public
specifier|static
name|int
name|setSignBitInt
parameter_list|(
name|int
name|val
parameter_list|,
name|boolean
name|positive
parameter_list|)
block|{
if|if
condition|(
name|positive
condition|)
block|{
return|return
name|val
operator|&
name|FULLBITS_31
return|;
block|}
return|return
name|val
operator||
name|NEGATIVE_INT_MASK
return|;
block|}
comment|/**    * Turn on or off the highest bit of a long value.    *    * @param val    *          the value to modify    * @param positive    *          whether to turn off (positive) or on (negative).    * @return unsigned long value    */
specifier|public
specifier|static
name|long
name|setSignBitLong
parameter_list|(
name|long
name|val
parameter_list|,
name|boolean
name|positive
parameter_list|)
block|{
if|if
condition|(
name|positive
condition|)
block|{
return|return
name|val
operator|&
name|FULLBITS_63
return|;
block|}
return|return
name|val
operator||
name|NEGATIVE_LONG_MASK
return|;
block|}
comment|/**    * Returns the minimal number of bits to represent the given integer value.    *    * @param word    *          int32 value    * @return the minimal number of bits to represent the given integer value    */
specifier|public
specifier|static
name|short
name|bitLengthInWord
parameter_list|(
name|int
name|word
parameter_list|)
block|{
if|if
condition|(
name|word
operator|<
literal|0
condition|)
block|{
return|return
literal|32
return|;
block|}
if|if
condition|(
name|word
operator|<
operator|(
literal|1
operator|<<
literal|16
operator|)
condition|)
block|{
if|if
condition|(
name|word
operator|<
literal|1
operator|<<
literal|8
condition|)
block|{
return|return
name|BIT_LENGTH
index|[
name|word
index|]
return|;
block|}
else|else
block|{
return|return
call|(
name|short
call|)
argument_list|(
name|BIT_LENGTH
index|[
name|word
operator|>>>
literal|8
index|]
operator|+
literal|8
argument_list|)
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|word
operator|<
operator|(
literal|1
operator|<<
literal|24
operator|)
condition|)
block|{
return|return
call|(
name|short
call|)
argument_list|(
name|BIT_LENGTH
index|[
name|word
operator|>>>
literal|16
index|]
operator|+
literal|16
argument_list|)
return|;
block|}
else|else
block|{
return|return
call|(
name|short
call|)
argument_list|(
name|BIT_LENGTH
index|[
name|word
operator|>>>
literal|24
index|]
operator|+
literal|24
argument_list|)
return|;
block|}
block|}
block|}
comment|/**    * Returns the minimal number of bits to represent the words.    *    * @param v0    *          v0    * @param v1    *          v1    * @param v2    *          v2    * @param v3    *          v3    * @return the minimal number of bits to represent the words    */
specifier|public
specifier|static
name|short
name|bitLength
parameter_list|(
name|int
name|v0
parameter_list|,
name|int
name|v1
parameter_list|,
name|int
name|v2
parameter_list|,
name|int
name|v3
parameter_list|)
block|{
if|if
condition|(
name|v3
operator|!=
literal|0
condition|)
block|{
return|return
call|(
name|short
call|)
argument_list|(
name|bitLengthInWord
argument_list|(
name|v3
argument_list|)
operator|+
literal|96
argument_list|)
return|;
block|}
if|if
condition|(
name|v2
operator|!=
literal|0
condition|)
block|{
return|return
call|(
name|short
call|)
argument_list|(
name|bitLengthInWord
argument_list|(
name|v2
argument_list|)
operator|+
literal|64
argument_list|)
return|;
block|}
if|if
condition|(
name|v1
operator|!=
literal|0
condition|)
block|{
return|return
call|(
name|short
call|)
argument_list|(
name|bitLengthInWord
argument_list|(
name|v1
argument_list|)
operator|+
literal|32
argument_list|)
return|;
block|}
return|return
name|bitLengthInWord
argument_list|(
name|v0
argument_list|)
return|;
block|}
comment|/**    * If we can assume JDK 1.8, this should use    * java.lang.Integer.compareUnsigned(), which will be replaced with intrinsics    * in JVM.    *    * @param x    *          the first {@code int} to compare    * @param y    *          the second {@code int} to compare    * @return the value {@code 0} if {@code x == y}; a value less than {@code 0}    *         if {@code x< y} as unsigned values; and a value greater than    *         {@code 0} if {@code x> y} as unsigned values    * @see "http://hg.openjdk.java.net/jdk8/tl/jdk/rev/71200c517524"    */
specifier|public
specifier|static
name|int
name|compareUnsignedInt
parameter_list|(
name|int
name|x
parameter_list|,
name|int
name|y
parameter_list|)
block|{
comment|// Can't assume JDK 1.8, so implementing this explicitly.
comment|// return Integer.compare(x + Integer.MIN_VALUE, y + Integer.MIN_VALUE);
if|if
condition|(
name|x
operator|==
name|y
condition|)
block|{
return|return
literal|0
return|;
block|}
if|if
condition|(
name|x
operator|+
name|Integer
operator|.
name|MIN_VALUE
operator|<
name|y
operator|+
name|Integer
operator|.
name|MIN_VALUE
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
else|else
block|{
return|return
literal|1
return|;
block|}
block|}
comment|/**    * If we can assume JDK 1.8, this should use java.lang.Long.compareUnsigned(),    * which will be replaced with intrinsics in JVM.    *    * @param x    *          the first {@code int} to compare    * @param y    *          the second {@code int} to compare    * @return the value {@code 0} if {@code x == y}; a value less than {@code 0}    *         if {@code x< y} as unsigned values; and a value greater than    *         {@code 0} if {@code x> y} as unsigned values    * @see "http://hg.openjdk.java.net/jdk8/tl/jdk/rev/71200c517524"    */
specifier|public
specifier|static
name|int
name|compareUnsignedLong
parameter_list|(
name|long
name|x
parameter_list|,
name|long
name|y
parameter_list|)
block|{
comment|// Can't assume JDK 1.8, so implementing this explicitly.
comment|// return Long.compare(x + Long.MIN_VALUE, y + Long.MIN_VALUE);
if|if
condition|(
name|x
operator|==
name|y
condition|)
block|{
return|return
literal|0
return|;
block|}
if|if
condition|(
name|x
operator|+
name|Long
operator|.
name|MIN_VALUE
operator|<
name|y
operator|+
name|Long
operator|.
name|MIN_VALUE
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
else|else
block|{
return|return
literal|1
return|;
block|}
block|}
comment|/**    * If we can assume JDK 1.8, this should use java.lang.Long.divideUnsigned(),    * which will be replaced with intrinsics in JVM.    *    * @param dividend    *          the value to be divided    * @param divisor    *          the value doing the dividing    * @return the unsigned quotient of the first argument divided by the second    *         argument    * @see "http://hg.openjdk.java.net/jdk8/tl/jdk/rev/71200c517524"    */
specifier|public
specifier|static
name|long
name|divideUnsignedLong
parameter_list|(
name|long
name|dividend
parameter_list|,
name|long
name|divisor
parameter_list|)
block|{
if|if
condition|(
name|divisor
operator|<
literal|0L
condition|)
block|{
comment|// Answer must be 0 or 1 depending on relative magnitude
comment|// of dividend and divisor.
return|return
operator|(
name|compareUnsignedLong
argument_list|(
name|dividend
argument_list|,
name|divisor
argument_list|)
operator|)
operator|<
literal|0
condition|?
literal|0L
else|:
literal|1L
return|;
block|}
if|if
condition|(
name|dividend
operator|>=
literal|0
condition|)
block|{
comment|// Both inputs non-negative
return|return
name|dividend
operator|/
name|divisor
return|;
block|}
else|else
block|{
comment|// simple division.
comment|// Yes, we should do something like this:
comment|// http://www.hackersdelight.org/divcMore.pdf
comment|// but later... (anyway this will be eventually replaced by
comment|// intrinsics in Java 8)
comment|// an equivalent algorithm exists in
comment|// com.google.common.primitives.UnsingedLongs
name|long
name|quotient
init|=
operator|(
operator|(
name|dividend
operator|>>>
literal|1L
operator|)
operator|/
name|divisor
operator|)
operator|<<
literal|1L
decl_stmt|;
name|long
name|remainder
init|=
name|dividend
operator|-
name|quotient
operator|*
name|divisor
decl_stmt|;
if|if
condition|(
name|compareUnsignedLong
argument_list|(
name|remainder
argument_list|,
name|divisor
argument_list|)
operator|>=
literal|0
condition|)
block|{
return|return
name|quotient
operator|+
literal|1
return|;
block|}
return|return
name|quotient
return|;
block|}
block|}
comment|/**    * If we can assume JDK 1.8, this should use    * java.lang.Long.remainderUnsigned(), which will be replaced with intrinsics    * in JVM.    *    * @param dividend    *          the value to be divided    * @param divisor    *          the value doing the dividing    * @return the unsigned remainder of the first argument divided by the second    *         argument    * @see "http://hg.openjdk.java.net/jdk8/tl/jdk/rev/71200c517524"    */
specifier|public
specifier|static
name|long
name|remainderUnsignedLong
parameter_list|(
name|long
name|dividend
parameter_list|,
name|long
name|divisor
parameter_list|)
block|{
if|if
condition|(
name|divisor
operator|<
literal|0L
condition|)
block|{
comment|// because divisor is negative, quotient is at most 1.
comment|// remainder must be dividend itself (quotient=0), or dividend -
comment|// divisor
return|return
operator|(
name|compareUnsignedLong
argument_list|(
name|dividend
argument_list|,
name|divisor
argument_list|)
operator|)
operator|<
literal|0
condition|?
name|dividend
else|:
name|dividend
operator|-
name|divisor
return|;
block|}
if|if
condition|(
name|dividend
operator|>=
literal|0L
condition|)
block|{
comment|// signed comparisons
return|return
name|dividend
operator|%
name|divisor
return|;
block|}
else|else
block|{
comment|// same above
name|long
name|quotient
init|=
operator|(
operator|(
name|dividend
operator|>>>
literal|1L
operator|)
operator|/
name|divisor
operator|)
operator|<<
literal|1L
decl_stmt|;
name|long
name|remainder
init|=
name|dividend
operator|-
name|quotient
operator|*
name|divisor
decl_stmt|;
if|if
condition|(
name|compareUnsignedLong
argument_list|(
name|remainder
argument_list|,
name|divisor
argument_list|)
operator|>=
literal|0
condition|)
block|{
return|return
name|remainder
operator|-
name|divisor
return|;
block|}
return|return
name|remainder
return|;
block|}
block|}
comment|/**    * @param lo    *          low 32bit    * @param hi    *          high 32bit    * @return long value that combines the two integers    */
specifier|public
specifier|static
name|long
name|combineInts
parameter_list|(
name|int
name|lo
parameter_list|,
name|int
name|hi
parameter_list|)
block|{
return|return
operator|(
operator|(
name|hi
operator|&
name|LONG_MASK
operator|)
operator|<<
literal|32L
operator|)
operator||
operator|(
name|lo
operator|&
name|LONG_MASK
operator|)
return|;
block|}
comment|/**    * @param val    *          long value    * @return high 32bit of the given value    */
specifier|public
specifier|static
name|int
name|extractHiInt
parameter_list|(
name|long
name|val
parameter_list|)
block|{
return|return
call|(
name|int
call|)
argument_list|(
name|val
operator|>>
literal|32
argument_list|)
return|;
block|}
comment|/**    * @param val    *          long value    * @return low 32bit of the given value    */
specifier|public
specifier|static
name|int
name|extractLowInt
parameter_list|(
name|long
name|val
parameter_list|)
block|{
return|return
operator|(
name|int
operator|)
name|val
return|;
block|}
comment|/** Throws an overflow exception. */
specifier|static
name|void
name|throwOverflowException
parameter_list|()
block|{
throw|throw
operator|new
name|ArithmeticException
argument_list|(
literal|"Overflow"
argument_list|)
throw|;
block|}
comment|/** Throws a divide-by-zero exception. */
specifier|static
name|void
name|throwZeroDivisionException
parameter_list|()
block|{
throw|throw
operator|new
name|ArithmeticException
argument_list|(
literal|"Divide by zero"
argument_list|)
throw|;
block|}
comment|/**    * Multi-precision one super-digit multiply in place.    *    * @param inOut    * @param multiplier    */
specifier|private
specifier|static
name|void
name|multiplyMultiPrecision
parameter_list|(
name|int
index|[]
name|inOut
parameter_list|,
name|int
name|multiplier
parameter_list|)
block|{
name|long
name|multiplierUnsigned
init|=
name|multiplier
operator|&
name|SqlMathUtil
operator|.
name|LONG_MASK
decl_stmt|;
name|long
name|product
init|=
literal|0L
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
name|inOut
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|product
operator|=
operator|(
name|inOut
index|[
name|i
index|]
operator|&
name|SqlMathUtil
operator|.
name|LONG_MASK
operator|)
operator|*
name|multiplierUnsigned
operator|+
operator|(
name|product
operator|>>>
literal|32
operator|)
expr_stmt|;
name|inOut
index|[
name|i
index|]
operator|=
operator|(
name|int
operator|)
name|product
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|product
operator|>>
literal|32
operator|)
operator|!=
literal|0
condition|)
block|{
name|SqlMathUtil
operator|.
name|throwOverflowException
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Multi-precision one super-digit divide in place.    *    * @param inOut    * @param divisor    * @return    */
specifier|private
specifier|static
name|int
name|divideMultiPrecision
parameter_list|(
name|int
index|[]
name|inOut
parameter_list|,
name|int
name|divisor
parameter_list|)
block|{
name|long
name|divisorUnsigned
init|=
name|divisor
operator|&
name|SqlMathUtil
operator|.
name|LONG_MASK
decl_stmt|;
name|long
name|quotient
decl_stmt|;
name|long
name|remainder
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|inOut
operator|.
name|length
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
name|remainder
operator|=
operator|(
name|inOut
index|[
name|i
index|]
operator|&
name|SqlMathUtil
operator|.
name|LONG_MASK
operator|)
operator|+
operator|(
name|remainder
operator|<<
literal|32
operator|)
expr_stmt|;
name|quotient
operator|=
name|remainder
operator|/
name|divisorUnsigned
expr_stmt|;
name|inOut
index|[
name|i
index|]
operator|=
operator|(
name|int
operator|)
name|quotient
expr_stmt|;
name|remainder
operator|%=
name|divisorUnsigned
expr_stmt|;
block|}
return|return
operator|(
name|int
operator|)
name|remainder
return|;
block|}
comment|/**    * Returns length of the array discounting the trailing elements with zero value.    */
specifier|private
specifier|static
name|int
name|arrayValidLength
parameter_list|(
name|int
index|[]
name|array
parameter_list|)
block|{
name|int
name|len
init|=
name|array
operator|.
name|length
decl_stmt|;
while|while
condition|(
name|len
operator|>
literal|0
operator|&&
name|array
index|[
name|len
operator|-
literal|1
index|]
operator|==
literal|0
condition|)
block|{
operator|--
name|len
expr_stmt|;
block|}
return|return
name|len
operator|<=
literal|0
condition|?
literal|0
else|:
name|len
return|;
block|}
comment|/**    * Multi-precision divide. dividend and divisor not changed. Assumes that    * there is enough room in quotient for results. Drawbacks of this    * implementation: 1) Need one extra super-digit in R 2) As it modifies D    * during work, then it restores it back (this is necessary because the caller    * doesn't expect D to change) 3) Always get Q and R - if R is unnecessary,    * can be slightly faster.    *    * @param dividend    *          dividend. in.    * @param divisor    *          divisor. in.    * @param quotient    *          quotient. out.    * @return remainder    */
specifier|public
specifier|static
name|int
index|[]
name|divideMultiPrecision
parameter_list|(
name|int
index|[]
name|dividend
parameter_list|,
name|int
index|[]
name|divisor
parameter_list|,
name|int
index|[]
name|quotient
parameter_list|)
block|{
specifier|final
name|int
name|dividendLength
init|=
name|arrayValidLength
argument_list|(
name|dividend
argument_list|)
decl_stmt|;
specifier|final
name|int
name|divisorLength
init|=
name|arrayValidLength
argument_list|(
name|divisor
argument_list|)
decl_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|quotient
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// Remainder := Dividend
name|int
index|[]
name|remainder
init|=
operator|new
name|int
index|[
name|dividend
operator|.
name|length
operator|+
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|dividend
argument_list|,
literal|0
argument_list|,
name|remainder
argument_list|,
literal|0
argument_list|,
name|dividend
operator|.
name|length
argument_list|)
expr_stmt|;
name|remainder
index|[
name|remainder
operator|.
name|length
operator|-
literal|1
index|]
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|divisorLength
operator|==
literal|0
condition|)
block|{
name|throwZeroDivisionException
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|dividendLength
operator|<
name|divisorLength
condition|)
block|{
return|return
name|remainder
return|;
block|}
if|if
condition|(
name|divisorLength
operator|==
literal|1
condition|)
block|{
name|int
name|rem
init|=
name|divideMultiPrecision
argument_list|(
name|remainder
argument_list|,
name|divisor
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|remainder
argument_list|,
literal|0
argument_list|,
name|quotient
argument_list|,
literal|0
argument_list|,
name|quotient
operator|.
name|length
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|remainder
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|remainder
index|[
literal|0
index|]
operator|=
name|rem
expr_stmt|;
return|return
name|remainder
return|;
block|}
comment|// Knuth, "The Art of Computer Programming", 3rd edition, vol.II, Alg.D,
comment|// pg 272
comment|// D1. Normalize so high digit of D>= BASE/2 - that guarantee
comment|// that QH will not be too far from the correct digit later in D3
name|int
name|d1
init|=
call|(
name|int
call|)
argument_list|(
name|BASE
operator|/
operator|(
operator|(
name|divisor
index|[
name|divisorLength
operator|-
literal|1
index|]
operator|&
name|LONG_MASK
operator|)
operator|+
literal|1L
operator|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|d1
operator|>
literal|1
condition|)
block|{
comment|// We are modifying divisor here, so make a local copy.
name|int
index|[]
name|newDivisor
init|=
operator|new
name|int
index|[
name|divisorLength
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|divisor
argument_list|,
literal|0
argument_list|,
name|newDivisor
argument_list|,
literal|0
argument_list|,
name|divisorLength
argument_list|)
expr_stmt|;
name|multiplyMultiPrecision
argument_list|(
name|newDivisor
argument_list|,
name|d1
argument_list|)
expr_stmt|;
name|divisor
operator|=
name|newDivisor
expr_stmt|;
name|multiplyMultiPrecision
argument_list|(
name|remainder
argument_list|,
name|d1
argument_list|)
expr_stmt|;
block|}
comment|// only 32bits, but long to behave as unsigned
name|long
name|dHigh
init|=
operator|(
name|divisor
index|[
name|divisorLength
operator|-
literal|1
index|]
operator|&
name|LONG_MASK
operator|)
decl_stmt|;
name|long
name|dLow
init|=
operator|(
name|divisor
index|[
name|divisorLength
operator|-
literal|2
index|]
operator|&
name|LONG_MASK
operator|)
decl_stmt|;
comment|// D2 already done - iulRindex initialized before normalization of R.
comment|// D3-D7. Loop on iulRindex - obtaining digits one-by-one, as "in paper"
for|for
control|(
name|int
name|rIndex
init|=
name|remainder
operator|.
name|length
operator|-
literal|1
init|;
name|rIndex
operator|>=
name|divisorLength
condition|;
operator|--
name|rIndex
control|)
block|{
comment|// D3. Calculate Q hat - estimation of the next digit
name|long
name|accum
init|=
name|combineInts
argument_list|(
name|remainder
index|[
name|rIndex
operator|-
literal|1
index|]
argument_list|,
name|remainder
index|[
name|rIndex
index|]
argument_list|)
decl_stmt|;
name|int
name|qhat
decl_stmt|;
if|if
condition|(
name|dHigh
operator|==
operator|(
name|remainder
index|[
name|rIndex
index|]
operator|&
name|LONG_MASK
operator|)
condition|)
block|{
name|qhat
operator|=
call|(
name|int
call|)
argument_list|(
name|BASE
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|qhat
operator|=
operator|(
name|int
operator|)
name|divideUnsignedLong
argument_list|(
name|accum
argument_list|,
name|dHigh
argument_list|)
expr_stmt|;
block|}
name|int
name|rhat
init|=
call|(
name|int
call|)
argument_list|(
name|accum
operator|-
operator|(
name|qhat
operator|&
name|LONG_MASK
operator|)
operator|*
name|dHigh
argument_list|)
decl_stmt|;
while|while
condition|(
name|compareUnsignedLong
argument_list|(
name|dLow
operator|*
operator|(
name|qhat
operator|&
name|LONG_MASK
operator|)
argument_list|,
name|combineInts
argument_list|(
name|remainder
index|[
name|rIndex
operator|-
literal|2
index|]
argument_list|,
name|rhat
argument_list|)
argument_list|)
operator|>
literal|0
condition|)
block|{
name|qhat
operator|--
expr_stmt|;
if|if
condition|(
operator|(
name|rhat
operator|&
name|LONG_MASK
operator|)
operator|>=
operator|-
operator|(
operator|(
name|int
operator|)
name|dHigh
operator|)
condition|)
block|{
break|break;
block|}
name|rhat
operator|+=
name|dHigh
expr_stmt|;
block|}
comment|// D4. Multiply and subtract: (some digits of) R -= D * QH
name|long
name|dwlMulAccum
init|=
literal|0
decl_stmt|;
name|accum
operator|=
name|BASE
expr_stmt|;
name|int
name|iulRwork
init|=
name|rIndex
operator|-
name|divisorLength
decl_stmt|;
for|for
control|(
name|int
name|dIndex
init|=
literal|0
init|;
name|dIndex
operator|<
name|divisorLength
condition|;
name|dIndex
operator|++
operator|,
name|iulRwork
operator|++
control|)
block|{
name|dwlMulAccum
operator|+=
operator|(
name|qhat
operator|&
name|LONG_MASK
operator|)
operator|*
operator|(
name|divisor
index|[
name|dIndex
index|]
operator|&
name|LONG_MASK
operator|)
expr_stmt|;
name|accum
operator|+=
operator|(
name|remainder
index|[
name|iulRwork
index|]
operator|&
name|LONG_MASK
operator|)
operator|-
operator|(
name|extractLowInt
argument_list|(
name|dwlMulAccum
argument_list|)
operator|&
name|LONG_MASK
operator|)
expr_stmt|;
name|dwlMulAccum
operator|=
operator|(
name|extractHiInt
argument_list|(
name|dwlMulAccum
argument_list|)
operator|&
name|LONG_MASK
operator|)
expr_stmt|;
name|remainder
index|[
name|iulRwork
index|]
operator|=
name|extractLowInt
argument_list|(
name|accum
argument_list|)
expr_stmt|;
name|accum
operator|=
operator|(
name|extractHiInt
argument_list|(
name|accum
argument_list|)
operator|&
name|LONG_MASK
operator|)
operator|+
name|BASE
operator|-
literal|1
expr_stmt|;
block|}
name|accum
operator|+=
operator|(
name|remainder
index|[
name|iulRwork
index|]
operator|&
name|LONG_MASK
operator|)
operator|-
name|dwlMulAccum
expr_stmt|;
name|remainder
index|[
name|iulRwork
index|]
operator|=
name|extractLowInt
argument_list|(
name|accum
argument_list|)
expr_stmt|;
name|quotient
index|[
name|rIndex
operator|-
name|divisorLength
index|]
operator|=
name|qhat
expr_stmt|;
comment|// D5. Test remainder. Carry indicates result<0, therefore QH 1 too
comment|// large
if|if
condition|(
name|extractHiInt
argument_list|(
name|accum
argument_list|)
operator|==
literal|0
condition|)
block|{
comment|// D6. Add back - probability is 2**(-31). R += D. Q[digit] -= 1
name|quotient
index|[
name|rIndex
operator|-
name|divisorLength
index|]
operator|=
name|qhat
operator|-
literal|1
expr_stmt|;
name|int
name|carry
init|=
literal|0
decl_stmt|;
name|int
name|dIndex
init|=
literal|0
decl_stmt|;
for|for
control|(
name|iulRwork
operator|=
name|rIndex
operator|-
name|divisorLength
init|;
name|dIndex
operator|<
name|divisorLength
condition|;
name|dIndex
operator|++
operator|,
name|iulRwork
operator|++
control|)
block|{
name|long
name|accum2
init|=
operator|(
name|divisor
index|[
name|dIndex
index|]
operator|&
name|LONG_MASK
operator|)
operator|+
operator|(
name|remainder
index|[
name|iulRwork
index|]
operator|&
name|LONG_MASK
operator|)
operator|+
operator|(
name|carry
operator|&
name|LONG_MASK
operator|)
decl_stmt|;
name|carry
operator|=
name|extractHiInt
argument_list|(
name|accum2
argument_list|)
expr_stmt|;
name|remainder
index|[
name|iulRwork
index|]
operator|=
name|extractLowInt
argument_list|(
name|accum2
argument_list|)
expr_stmt|;
block|}
name|remainder
index|[
name|iulRwork
index|]
operator|+=
name|carry
expr_stmt|;
block|}
block|}
comment|// D8. Unnormalize: Divide R to get result
if|if
condition|(
name|d1
operator|>
literal|1
condition|)
block|{
name|divideMultiPrecision
argument_list|(
name|remainder
argument_list|,
name|d1
argument_list|)
expr_stmt|;
block|}
return|return
name|remainder
return|;
block|}
static|static
block|{
name|BIT_LENGTH
operator|=
operator|new
name|byte
index|[
literal|256
index|]
expr_stmt|;
name|BIT_LENGTH
index|[
literal|0
index|]
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|8
condition|;
operator|++
name|i
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|1
operator|<<
operator|(
name|i
operator|-
literal|1
operator|)
init|;
name|j
operator|<
literal|1
operator|<<
name|i
condition|;
operator|++
name|j
control|)
block|{
name|BIT_LENGTH
index|[
name|j
index|]
operator|=
operator|(
name|byte
operator|)
name|i
expr_stmt|;
block|}
block|}
name|POWER_FIVES_INT31
index|[
literal|0
index|]
operator|=
literal|1
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|POWER_FIVES_INT31
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|POWER_FIVES_INT31
index|[
name|i
index|]
operator|=
name|POWER_FIVES_INT31
index|[
name|i
operator|-
literal|1
index|]
operator|*
literal|5
expr_stmt|;
assert|assert
operator|(
name|POWER_FIVES_INT31
index|[
name|i
index|]
operator|>
literal|0
operator|)
assert|;
block|}
name|POWER_FIVES_INT63
index|[
literal|0
index|]
operator|=
literal|1L
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|POWER_FIVES_INT63
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|POWER_FIVES_INT63
index|[
name|i
index|]
operator|=
name|POWER_FIVES_INT63
index|[
name|i
operator|-
literal|1
index|]
operator|*
literal|5L
expr_stmt|;
assert|assert
operator|(
name|POWER_FIVES_INT63
index|[
name|i
index|]
operator|>
literal|0L
operator|)
assert|;
block|}
name|POWER_TENS_INT31
index|[
literal|0
index|]
operator|=
literal|1
expr_stmt|;
name|ROUND_POWER_TENS_INT31
index|[
literal|0
index|]
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|POWER_TENS_INT31
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|POWER_TENS_INT31
index|[
name|i
index|]
operator|=
name|POWER_TENS_INT31
index|[
name|i
operator|-
literal|1
index|]
operator|*
literal|10
expr_stmt|;
assert|assert
operator|(
name|POWER_TENS_INT31
index|[
name|i
index|]
operator|>
literal|0
operator|)
assert|;
name|ROUND_POWER_TENS_INT31
index|[
name|i
index|]
operator|=
name|POWER_TENS_INT31
index|[
name|i
index|]
operator|>>
literal|1
expr_stmt|;
block|}
name|POWER_FIVES_INT128
index|[
literal|0
index|]
operator|=
operator|new
name|UnsignedInt128
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|INVERSE_POWER_FIVES_INT128
index|[
literal|0
index|]
operator|=
operator|new
name|UnsignedInt128
argument_list|(
literal|0xFFFFFFFF
argument_list|,
literal|0xFFFFFFFF
argument_list|,
literal|0xFFFFFFFF
argument_list|,
literal|0xFFFFFFFF
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|POWER_FIVES_INT128
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|POWER_FIVES_INT128
index|[
name|i
index|]
operator|=
operator|new
name|UnsignedInt128
argument_list|(
name|POWER_FIVES_INT128
index|[
name|i
operator|-
literal|1
index|]
argument_list|)
expr_stmt|;
name|POWER_FIVES_INT128
index|[
name|i
index|]
operator|.
name|multiplyDestructive
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|INVERSE_POWER_FIVES_INT128
index|[
name|i
index|]
operator|=
operator|new
name|UnsignedInt128
argument_list|(
name|INVERSE_POWER_FIVES_INT128
index|[
name|i
operator|-
literal|1
index|]
argument_list|)
expr_stmt|;
name|INVERSE_POWER_FIVES_INT128
index|[
name|i
index|]
operator|.
name|divideDestructive
argument_list|(
literal|5
argument_list|)
expr_stmt|;
block|}
name|POWER_TENS_INT128
index|[
literal|0
index|]
operator|=
operator|new
name|UnsignedInt128
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|ROUND_POWER_TENS_INT128
index|[
literal|0
index|]
operator|=
operator|new
name|UnsignedInt128
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|INVERSE_POWER_TENS_INT128
index|[
literal|0
index|]
operator|=
operator|new
name|UnsignedInt128
argument_list|(
literal|0xFFFFFFFF
argument_list|,
literal|0xFFFFFFFF
argument_list|,
literal|0xFFFFFFFF
argument_list|,
literal|0xFFFFFFFF
argument_list|)
expr_stmt|;
name|INVERSE_POWER_TENS_INT128_WORD_SHIFTS
index|[
literal|0
index|]
operator|=
literal|0
expr_stmt|;
name|int
index|[]
name|inverseTens
init|=
operator|new
name|int
index|[
literal|8
index|]
decl_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|inverseTens
argument_list|,
literal|0xFFFFFFFF
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|POWER_TENS_INT128
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|int
name|divisor
init|=
literal|10
decl_stmt|;
name|POWER_TENS_INT128
index|[
name|i
index|]
operator|=
operator|new
name|UnsignedInt128
argument_list|(
name|POWER_TENS_INT128
index|[
name|i
operator|-
literal|1
index|]
argument_list|)
expr_stmt|;
name|POWER_TENS_INT128
index|[
name|i
index|]
operator|.
name|multiplyDestructive
argument_list|(
name|divisor
argument_list|)
expr_stmt|;
name|ROUND_POWER_TENS_INT128
index|[
name|i
index|]
operator|=
name|POWER_TENS_INT128
index|[
name|i
index|]
operator|.
name|shiftRightConstructive
argument_list|(
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|long
name|quotient
decl_stmt|;
name|long
name|remainder
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
name|inverseTens
operator|.
name|length
operator|-
literal|1
init|;
name|j
operator|>=
literal|0
condition|;
operator|--
name|j
control|)
block|{
name|quotient
operator|=
operator|(
operator|(
name|inverseTens
index|[
name|j
index|]
operator|&
name|SqlMathUtil
operator|.
name|LONG_MASK
operator|)
operator|+
operator|(
name|remainder
operator|<<
literal|32
operator|)
operator|)
operator|/
name|divisor
expr_stmt|;
name|remainder
operator|=
operator|(
operator|(
name|inverseTens
index|[
name|j
index|]
operator|&
name|SqlMathUtil
operator|.
name|LONG_MASK
operator|)
operator|+
operator|(
name|remainder
operator|<<
literal|32
operator|)
operator|)
operator|%
name|divisor
expr_stmt|;
name|inverseTens
index|[
name|j
index|]
operator|=
operator|(
name|int
operator|)
name|quotient
expr_stmt|;
block|}
name|int
name|wordShifts
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
name|inverseTens
operator|.
name|length
operator|-
literal|1
init|;
name|j
operator|>=
literal|4
operator|&&
name|inverseTens
index|[
name|j
index|]
operator|==
literal|0
condition|;
operator|--
name|j
control|)
block|{
operator|++
name|wordShifts
expr_stmt|;
block|}
name|INVERSE_POWER_TENS_INT128_WORD_SHIFTS
index|[
name|i
index|]
operator|=
name|wordShifts
expr_stmt|;
name|INVERSE_POWER_TENS_INT128
index|[
name|i
index|]
operator|=
operator|new
name|UnsignedInt128
argument_list|(
name|inverseTens
index|[
name|inverseTens
operator|.
name|length
operator|-
literal|4
operator|-
name|wordShifts
index|]
argument_list|,
name|inverseTens
index|[
name|inverseTens
operator|.
name|length
operator|-
literal|3
operator|-
name|wordShifts
index|]
argument_list|,
name|inverseTens
index|[
name|inverseTens
operator|.
name|length
operator|-
literal|2
operator|-
name|wordShifts
index|]
argument_list|,
name|inverseTens
index|[
name|inverseTens
operator|.
name|length
operator|-
literal|1
operator|-
name|wordShifts
index|]
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|SqlMathUtil
parameter_list|()
block|{   }
block|}
end_class

end_unit

