begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *	Source code for the "strtod" library procedure.  *  * Copyright 1988-1992 Regents of the University of California  * Permission to use, copy, modify, and distribute this  * software and its documentation for any purpose and without  * fee is hereby granted, provided that the above copyright  * notice appear in all copies.  The University of California  * makes no representations about the suitability of this  * software for any purpose.  It is provided "as is" without  * express or implied warranty.  */
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
operator|.
name|fast
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_class
specifier|public
class|class
name|StringToDouble
block|{
specifier|static
specifier|final
name|int
name|maxExponent
init|=
literal|511
decl_stmt|;
comment|/* Largest possible base 10 exponent.  Any 				 * exponent larger than this will already 				 * produce underflow or overflow, so there's 				 * no need to worry about additional digits. 				 */
specifier|static
specifier|final
name|double
name|powersOf10
index|[]
init|=
block|{
comment|/* Table giving binary powers of 10.  Entry */
literal|10.
block|,
comment|/* is 10^2^i.  Used to convert decimal */
literal|100.
block|,
comment|/* exponents into floating-point numbers. */
literal|1.0e4
block|,
literal|1.0e8
block|,
literal|1.0e16
block|,
literal|1.0e32
block|,
literal|1.0e64
block|,
literal|1.0e128
block|,
literal|1.0e256
block|}
decl_stmt|;
comment|/*    * Only for testing    */
specifier|static
name|double
name|strtod
parameter_list|(
name|String
name|s
parameter_list|)
block|{
specifier|final
name|byte
index|[]
name|utf8
init|=
name|s
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
return|return
name|strtod
argument_list|(
name|utf8
argument_list|,
literal|0
argument_list|,
name|utf8
operator|.
name|length
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|double
name|strtod
parameter_list|(
name|byte
index|[]
name|utf8
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
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
argument_list|()
throw|;
block|}
name|boolean
name|signIsNegative
init|=
literal|true
decl_stmt|;
name|boolean
name|expSignIsNegative
init|=
literal|true
decl_stmt|;
name|double
name|fraction
decl_stmt|;
name|int
name|d
decl_stmt|;
name|int
name|p
init|=
name|offset
decl_stmt|;
name|int
name|end
init|=
name|offset
operator|+
name|length
decl_stmt|;
name|int
name|c
decl_stmt|;
name|int
name|exp
init|=
literal|0
decl_stmt|;
comment|/* Exponent read from "EX" field. */
name|int
name|fracExp
init|=
literal|0
decl_stmt|;
comment|/* Exponent that derives from the fractional 				 * part.  Under normal circumstatnces, it is 				 * the negative of the number of digits in F. 				 * However, if I is very long, the last digits 				 * of I get dropped (otherwise a long I with a 				 * large negative exponent could cause an 				 * unnecessary overflow on I alone).  In this 				 * case, fracExp is incremented one for each 				 * dropped digit. */
name|int
name|mantSize
decl_stmt|;
comment|/* Number of digits in mantissa. */
name|int
name|decPt
decl_stmt|;
comment|/* Number of mantissa digits BEFORE decimal 				 * point. */
name|int
name|pExp
decl_stmt|;
comment|/* Temporarily holds location of exponent 				 * in string. */
comment|/*      * Strip off leading blanks and check for a sign.      */
while|while
condition|(
name|p
operator|<
name|end
operator|&&
name|Character
operator|.
name|isWhitespace
argument_list|(
name|utf8
index|[
name|p
index|]
argument_list|)
condition|)
block|{
name|p
operator|++
expr_stmt|;
block|}
while|while
condition|(
name|end
operator|>
name|p
operator|&&
name|Character
operator|.
name|isWhitespace
argument_list|(
name|utf8
index|[
name|end
operator|-
literal|1
index|]
argument_list|)
condition|)
block|{
name|end
operator|--
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|testSimpleDecimal
argument_list|(
name|utf8
argument_list|,
name|p
argument_list|,
name|end
operator|-
name|p
argument_list|)
condition|)
block|{
return|return
name|Double
operator|.
name|parseDouble
argument_list|(
operator|new
name|String
argument_list|(
name|utf8
argument_list|,
name|p
argument_list|,
name|end
operator|-
name|p
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|utf8
index|[
name|p
index|]
operator|==
literal|'-'
condition|)
block|{
name|signIsNegative
operator|=
literal|true
expr_stmt|;
name|p
operator|+=
literal|1
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|utf8
index|[
name|p
index|]
operator|==
literal|'+'
condition|)
block|{
name|p
operator|+=
literal|1
expr_stmt|;
block|}
name|signIsNegative
operator|=
literal|false
expr_stmt|;
block|}
comment|/*      * Count the number of digits in the mantissa (including the decimal      * point), and also locate the decimal point.      */
name|decPt
operator|=
operator|-
literal|1
expr_stmt|;
name|int
name|mantEnd
init|=
name|end
operator|-
name|p
decl_stmt|;
for|for
control|(
name|mantSize
operator|=
literal|0
init|;
name|mantSize
operator|<
name|mantEnd
condition|;
name|mantSize
operator|+=
literal|1
control|)
block|{
name|c
operator|=
name|utf8
index|[
name|p
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|isdigit
argument_list|(
name|c
argument_list|)
condition|)
block|{
if|if
condition|(
operator|(
name|c
operator|!=
literal|'.'
operator|)
operator|||
operator|(
name|decPt
operator|>=
literal|0
operator|)
condition|)
block|{
break|break;
block|}
name|decPt
operator|=
name|mantSize
expr_stmt|;
block|}
name|p
operator|+=
literal|1
expr_stmt|;
block|}
comment|/*      * Now suck up the digits in the mantissa.  Use two integers to      * collect 9 digits each (this is faster than using floating-point).      * If the mantissa has more than 18 digits, ignore the extras, since      * they can't affect the value anyway.      */
name|pExp
operator|=
name|p
expr_stmt|;
name|p
operator|-=
name|mantSize
expr_stmt|;
if|if
condition|(
name|decPt
operator|<
literal|0
condition|)
block|{
name|decPt
operator|=
name|mantSize
expr_stmt|;
block|}
else|else
block|{
name|mantSize
operator|-=
literal|1
expr_stmt|;
comment|/* One of the digits was the point. */
block|}
if|if
condition|(
name|mantSize
operator|>
literal|18
condition|)
block|{
name|fracExp
operator|=
name|decPt
operator|-
literal|18
expr_stmt|;
name|mantSize
operator|=
literal|18
expr_stmt|;
block|}
else|else
block|{
name|fracExp
operator|=
name|decPt
operator|-
name|mantSize
expr_stmt|;
block|}
if|if
condition|(
name|mantSize
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|signIsNegative
condition|)
block|{
return|return
operator|-
literal|0.0d
return|;
block|}
return|return
literal|0.0d
return|;
block|}
else|else
block|{
name|double
name|frac1
decl_stmt|,
name|frac2
decl_stmt|;
name|frac1
operator|=
literal|0
expr_stmt|;
for|for
control|(
init|;
name|mantSize
operator|>
literal|9
condition|;
name|mantSize
operator|-=
literal|1
control|)
block|{
name|c
operator|=
name|utf8
index|[
name|p
index|]
expr_stmt|;
name|p
operator|+=
literal|1
expr_stmt|;
if|if
condition|(
name|c
operator|==
literal|'.'
condition|)
block|{
name|c
operator|=
name|utf8
index|[
name|p
index|]
expr_stmt|;
name|p
operator|+=
literal|1
expr_stmt|;
block|}
name|frac1
operator|=
literal|10
operator|*
name|frac1
operator|+
operator|(
name|c
operator|-
literal|'0'
operator|)
expr_stmt|;
block|}
name|frac2
operator|=
literal|0
expr_stmt|;
for|for
control|(
init|;
name|mantSize
operator|>
literal|0
condition|;
name|mantSize
operator|-=
literal|1
control|)
block|{
name|c
operator|=
name|utf8
index|[
name|p
index|]
expr_stmt|;
name|p
operator|+=
literal|1
expr_stmt|;
if|if
condition|(
name|c
operator|==
literal|'.'
condition|)
block|{
name|c
operator|=
name|utf8
index|[
name|p
index|]
expr_stmt|;
name|p
operator|+=
literal|1
expr_stmt|;
block|}
name|frac2
operator|=
literal|10
operator|*
name|frac2
operator|+
operator|(
name|c
operator|-
literal|'0'
operator|)
expr_stmt|;
block|}
name|fraction
operator|=
operator|(
literal|1e9d
operator|*
name|frac1
operator|)
operator|+
name|frac2
expr_stmt|;
block|}
comment|/*      * Skim off the exponent.      */
name|p
operator|=
name|pExp
expr_stmt|;
if|if
condition|(
name|p
operator|<
name|end
condition|)
block|{
if|if
condition|(
operator|(
name|utf8
index|[
name|p
index|]
operator|==
literal|'E'
operator|)
operator|||
operator|(
name|utf8
index|[
name|p
index|]
operator|==
literal|'e'
operator|)
condition|)
block|{
name|p
operator|+=
literal|1
expr_stmt|;
if|if
condition|(
name|p
operator|<
name|end
condition|)
block|{
if|if
condition|(
name|utf8
index|[
name|p
index|]
operator|==
literal|'-'
condition|)
block|{
name|expSignIsNegative
operator|=
literal|true
expr_stmt|;
name|p
operator|+=
literal|1
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|utf8
index|[
name|p
index|]
operator|==
literal|'+'
condition|)
block|{
name|p
operator|+=
literal|1
expr_stmt|;
block|}
name|expSignIsNegative
operator|=
literal|false
expr_stmt|;
block|}
while|while
condition|(
name|p
operator|<
name|end
operator|&&
name|isdigit
argument_list|(
name|utf8
index|[
name|p
index|]
argument_list|)
condition|)
block|{
name|exp
operator|=
name|exp
operator|*
literal|10
operator|+
operator|(
name|utf8
index|[
name|p
index|]
operator|-
literal|'0'
operator|)
expr_stmt|;
name|p
operator|+=
literal|1
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|expSignIsNegative
condition|)
block|{
name|exp
operator|=
name|fracExp
operator|-
name|exp
expr_stmt|;
block|}
else|else
block|{
name|exp
operator|=
name|fracExp
operator|+
name|exp
expr_stmt|;
block|}
comment|/*      * Generate a floating-point number that represents the exponent.      * Do this by processing the exponent one bit at a time to combine      * many powers of 2 of 10. Then combine the exponent with the      * fraction.      */
if|if
condition|(
name|exp
operator|<
literal|0
condition|)
block|{
name|expSignIsNegative
operator|=
literal|true
expr_stmt|;
name|exp
operator|=
operator|-
name|exp
expr_stmt|;
block|}
else|else
block|{
name|expSignIsNegative
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|exp
operator|>
name|maxExponent
condition|)
block|{
name|exp
operator|=
name|maxExponent
expr_stmt|;
block|}
name|double
name|dblExp
init|=
literal|1.0
decl_stmt|;
for|for
control|(
name|d
operator|=
literal|0
init|;
name|exp
operator|!=
literal|0
condition|;
name|exp
operator|>>=
literal|1
operator|,
name|d
operator|+=
literal|1
control|)
block|{
if|if
condition|(
operator|(
name|exp
operator|&
literal|1
operator|)
operator|==
literal|1
condition|)
block|{
name|dblExp
operator|*=
name|powersOf10
index|[
name|d
index|]
expr_stmt|;
block|}
block|}
if|if
condition|(
name|expSignIsNegative
condition|)
block|{
name|fraction
operator|/=
name|dblExp
expr_stmt|;
block|}
else|else
block|{
name|fraction
operator|*=
name|dblExp
expr_stmt|;
block|}
if|if
condition|(
name|signIsNegative
condition|)
block|{
return|return
operator|-
name|fraction
return|;
block|}
return|return
name|fraction
return|;
block|}
specifier|private
specifier|static
name|boolean
name|testSimpleDecimal
parameter_list|(
name|byte
index|[]
name|utf8
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
block|{
if|if
condition|(
name|len
operator|>
literal|18
condition|)
block|{
return|return
literal|false
return|;
block|}
name|int
name|decimalPts
init|=
literal|0
decl_stmt|;
name|int
name|signs
init|=
literal|0
decl_stmt|;
name|int
name|nondigits
init|=
literal|0
decl_stmt|;
name|int
name|digits
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|off
init|;
name|i
operator|<
name|len
operator|+
name|off
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|c
init|=
name|utf8
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|c
operator|==
literal|'.'
condition|)
block|{
name|decimalPts
operator|++
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|c
operator|==
literal|'-'
operator|||
name|c
operator|==
literal|'+'
condition|)
block|{
name|signs
operator|++
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|isdigit
argument_list|(
name|c
argument_list|)
condition|)
block|{
comment|// could be exponential notations
name|nondigits
operator|++
expr_stmt|;
block|}
else|else
block|{
name|digits
operator|++
expr_stmt|;
block|}
block|}
comment|// There can be up to 5e-16 error
return|return
operator|(
name|decimalPts
operator|<=
literal|1
operator|&&
name|signs
operator|<=
literal|1
operator|&&
name|nondigits
operator|==
literal|0
operator|&&
name|digits
operator|<
literal|16
operator|)
return|;
block|}
specifier|private
specifier|static
name|boolean
name|isdigit
parameter_list|(
name|int
name|c
parameter_list|)
block|{
return|return
literal|'0'
operator|<=
name|c
operator|&&
name|c
operator|<=
literal|'9'
return|;
block|}
block|}
end_class

end_unit

