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
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigInteger
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
name|RandomTypeUtil
import|;
end_import

begin_class
specifier|public
class|class
name|HiveDecimalTestBase
block|{
specifier|public
specifier|static
name|int
name|POUND_FACTOR
init|=
literal|1000
decl_stmt|;
specifier|public
specifier|static
enum|enum
name|BigDecimalFlavor
block|{
name|NORMAL_RANGE
block|,
name|FRACTIONS_ONLY
block|,
name|NEGATIVE_SCALE
block|,
name|LONG_TAIL
block|}
specifier|public
specifier|static
enum|enum
name|BigDecimalPairFlavor
block|{
name|RANDOM
block|,
name|NEAR
block|,
name|INVERSE
block|}
specifier|public
name|BigDecimal
name|randHiveBigDecimal
parameter_list|(
name|Random
name|r
parameter_list|,
name|String
name|digitAlphabet
parameter_list|,
name|BigDecimalFlavor
name|bigDecimalFlavor
parameter_list|)
block|{
switch|switch
condition|(
name|bigDecimalFlavor
condition|)
block|{
case|case
name|NORMAL_RANGE
case|:
return|return
name|randHiveBigDecimalNormalRange
argument_list|(
name|r
argument_list|,
name|digitAlphabet
argument_list|)
return|;
case|case
name|FRACTIONS_ONLY
case|:
return|return
name|randHiveBigDecimalFractionsOnly
argument_list|(
name|r
argument_list|,
name|digitAlphabet
argument_list|)
return|;
case|case
name|NEGATIVE_SCALE
case|:
return|return
name|randHiveBigDecimalNegativeScale
argument_list|(
name|r
argument_list|,
name|digitAlphabet
argument_list|)
return|;
case|case
name|LONG_TAIL
case|:
return|return
name|randHiveBigDecimalLongTail
argument_list|(
name|r
argument_list|,
name|digitAlphabet
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected big decimal flavor "
operator|+
name|bigDecimalFlavor
argument_list|)
throw|;
block|}
block|}
specifier|public
name|BigDecimal
index|[]
name|randHiveBigDecimalPair
parameter_list|(
name|Random
name|r
parameter_list|,
name|String
name|digitAlphabet
parameter_list|,
name|BigDecimalFlavor
name|bigDecimalFlavor
parameter_list|,
name|BigDecimalPairFlavor
name|bigDecimalPairFlavor
parameter_list|)
block|{
name|BigDecimal
index|[]
name|pair
init|=
operator|new
name|BigDecimal
index|[
literal|2
index|]
decl_stmt|;
name|BigDecimal
name|bigDecimal1
init|=
name|randHiveBigDecimal
argument_list|(
name|r
argument_list|,
name|digitAlphabet
argument_list|,
name|bigDecimalFlavor
argument_list|)
decl_stmt|;
name|pair
index|[
literal|0
index|]
operator|=
name|bigDecimal1
expr_stmt|;
name|BigDecimal
name|bigDecimal2
decl_stmt|;
switch|switch
condition|(
name|bigDecimalPairFlavor
condition|)
block|{
case|case
name|RANDOM
case|:
name|bigDecimal2
operator|=
name|randHiveBigDecimal
argument_list|(
name|r
argument_list|,
name|digitAlphabet
argument_list|,
name|bigDecimalFlavor
argument_list|)
expr_stmt|;
break|break;
case|case
name|NEAR
case|:
name|bigDecimal2
operator|=
name|randHiveBigDecimalNear
argument_list|(
name|r
argument_list|,
name|bigDecimal1
argument_list|)
expr_stmt|;
break|break;
case|case
name|INVERSE
case|:
name|bigDecimal2
operator|=
name|randHiveBigDecimalNear
argument_list|(
name|r
argument_list|,
name|bigDecimal1
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected big decimal pair flavor "
operator|+
name|bigDecimalPairFlavor
argument_list|)
throw|;
block|}
name|pair
index|[
literal|1
index|]
operator|=
name|bigDecimal2
expr_stmt|;
return|return
name|pair
return|;
block|}
specifier|public
name|BigDecimal
name|randHiveBigDecimalNormalRange
parameter_list|(
name|Random
name|r
parameter_list|,
name|String
name|digitAlphabet
parameter_list|)
block|{
name|String
name|digits
init|=
name|RandomTypeUtil
operator|.
name|getRandString
argument_list|(
name|r
argument_list|,
name|digitAlphabet
argument_list|,
literal|1
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|38
argument_list|)
argument_list|)
decl_stmt|;
name|BigInteger
name|bigInteger
init|=
operator|new
name|BigInteger
argument_list|(
name|digits
argument_list|)
decl_stmt|;
name|boolean
name|negated
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|r
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|bigInteger
operator|=
name|bigInteger
operator|.
name|negate
argument_list|()
expr_stmt|;
name|negated
operator|=
literal|true
expr_stmt|;
block|}
name|int
name|scale
init|=
literal|0
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|38
operator|+
literal|1
argument_list|)
decl_stmt|;
return|return
operator|new
name|BigDecimal
argument_list|(
name|bigInteger
argument_list|,
name|scale
argument_list|)
return|;
block|}
specifier|public
name|BigDecimal
name|randHiveBigDecimalNegativeScale
parameter_list|(
name|Random
name|r
parameter_list|,
name|String
name|digitAlphabet
parameter_list|)
block|{
name|String
name|digits
init|=
name|RandomTypeUtil
operator|.
name|getRandString
argument_list|(
name|r
argument_list|,
name|digitAlphabet
argument_list|,
literal|1
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|38
argument_list|)
argument_list|)
decl_stmt|;
name|BigInteger
name|bigInteger
init|=
operator|new
name|BigInteger
argument_list|(
name|digits
argument_list|)
decl_stmt|;
name|boolean
name|negated
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|r
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|bigInteger
operator|=
name|bigInteger
operator|.
name|negate
argument_list|()
expr_stmt|;
name|negated
operator|=
literal|true
expr_stmt|;
block|}
name|int
name|scale
init|=
literal|0
operator|+
operator|(
name|r
operator|.
name|nextBoolean
argument_list|()
condition|?
literal|0
else|:
name|r
operator|.
name|nextInt
argument_list|(
literal|38
operator|+
literal|1
argument_list|)
operator|)
decl_stmt|;
if|if
condition|(
name|r
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|scale
operator|=
operator|-
name|scale
expr_stmt|;
block|}
return|return
operator|new
name|BigDecimal
argument_list|(
name|bigInteger
argument_list|,
name|scale
argument_list|)
return|;
block|}
specifier|public
name|BigDecimal
name|randHiveBigDecimalLongTail
parameter_list|(
name|Random
name|r
parameter_list|,
name|String
name|digitAlphabet
parameter_list|)
block|{
name|int
name|scale
init|=
literal|0
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|38
operator|+
literal|20
argument_list|)
decl_stmt|;
specifier|final
name|int
name|maxDigits
init|=
literal|38
operator|+
operator|(
name|scale
operator|==
literal|0
condition|?
literal|0
else|:
literal|20
operator|)
decl_stmt|;
name|String
name|digits
init|=
name|RandomTypeUtil
operator|.
name|getRandString
argument_list|(
name|r
argument_list|,
name|digitAlphabet
argument_list|,
literal|1
operator|+
name|r
operator|.
name|nextInt
argument_list|(
name|maxDigits
argument_list|)
argument_list|)
decl_stmt|;
name|BigInteger
name|bigInteger
init|=
operator|new
name|BigInteger
argument_list|(
name|digits
argument_list|)
decl_stmt|;
name|boolean
name|negated
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|r
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|bigInteger
operator|=
name|bigInteger
operator|.
name|negate
argument_list|()
expr_stmt|;
name|negated
operator|=
literal|true
expr_stmt|;
block|}
return|return
operator|new
name|BigDecimal
argument_list|(
name|bigInteger
argument_list|,
name|scale
argument_list|)
return|;
block|}
specifier|public
name|BigDecimal
name|randHiveBigDecimalFractionsOnly
parameter_list|(
name|Random
name|r
parameter_list|,
name|String
name|digitAlphabet
parameter_list|)
block|{
name|int
name|scale
init|=
literal|1
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|38
operator|+
literal|1
argument_list|)
decl_stmt|;
name|String
name|digits
init|=
name|RandomTypeUtil
operator|.
name|getRandString
argument_list|(
name|r
argument_list|,
name|digitAlphabet
argument_list|,
literal|1
operator|+
name|r
operator|.
name|nextInt
argument_list|(
name|scale
argument_list|)
argument_list|)
decl_stmt|;
name|BigInteger
name|bigInteger
init|=
operator|new
name|BigInteger
argument_list|(
name|digits
argument_list|)
decl_stmt|;
name|boolean
name|negated
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|r
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|bigInteger
operator|=
name|bigInteger
operator|.
name|negate
argument_list|()
expr_stmt|;
name|negated
operator|=
literal|true
expr_stmt|;
block|}
return|return
operator|new
name|BigDecimal
argument_list|(
name|bigInteger
argument_list|,
name|scale
argument_list|)
return|;
block|}
specifier|public
name|BigDecimal
name|randHiveBigDecimalNear
parameter_list|(
name|Random
name|r
parameter_list|,
name|BigDecimal
name|bigDecimal
parameter_list|)
block|{
name|int
name|scale
init|=
name|bigDecimal
operator|.
name|scale
argument_list|()
decl_stmt|;
name|int
name|delta
init|=
name|r
operator|.
name|nextInt
argument_list|(
literal|10
argument_list|)
decl_stmt|;
if|if
condition|(
name|r
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
return|return
name|bigDecimal
operator|.
name|add
argument_list|(
operator|new
name|BigDecimal
argument_list|(
name|BigInteger
operator|.
name|valueOf
argument_list|(
name|delta
argument_list|)
argument_list|,
name|scale
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|bigDecimal
operator|.
name|subtract
argument_list|(
operator|new
name|BigDecimal
argument_list|(
name|BigInteger
operator|.
name|valueOf
argument_list|(
name|delta
argument_list|)
argument_list|,
name|scale
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|public
name|BigDecimal
name|randHiveBigDecimalInverse
parameter_list|(
name|Random
name|r
parameter_list|,
name|BigDecimal
name|bigDecimal
parameter_list|)
block|{
if|if
condition|(
name|bigDecimal
operator|.
name|signum
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|bigDecimal
return|;
block|}
return|return
name|BigDecimal
operator|.
name|ONE
operator|.
name|divide
argument_list|(
name|bigDecimal
argument_list|)
return|;
block|}
specifier|public
name|BigInteger
name|randHiveBigInteger
parameter_list|(
name|Random
name|r
parameter_list|,
name|String
name|digitAlphabet
parameter_list|)
block|{
name|String
name|digits
init|=
name|RandomTypeUtil
operator|.
name|getRandString
argument_list|(
name|r
argument_list|,
name|digitAlphabet
argument_list|,
literal|1
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|38
argument_list|)
argument_list|)
decl_stmt|;
name|BigInteger
name|bigInteger
init|=
operator|new
name|BigInteger
argument_list|(
name|digits
argument_list|)
decl_stmt|;
name|boolean
name|negated
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|r
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|bigInteger
operator|=
name|bigInteger
operator|.
name|negate
argument_list|()
expr_stmt|;
name|negated
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|bigInteger
return|;
block|}
specifier|public
name|boolean
name|isTenPowerBug
parameter_list|(
name|String
name|string
parameter_list|)
block|{
comment|// // System.out.println("TEST_IS_TEN_TO_38_STRING isTenPowerBug " + string);
if|if
condition|(
name|string
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'-'
condition|)
block|{
name|string
operator|=
name|string
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|int
name|index
init|=
name|string
operator|.
name|indexOf
argument_list|(
literal|'.'
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|!=
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|index
operator|==
literal|0
condition|)
block|{
name|string
operator|=
name|string
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|string
operator|=
name|string
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
operator|+
name|string
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
comment|// // System.out.println("TEST_IS_TEN_TO_38_STRING isTenPowerBug " + string);
return|return
name|string
operator|.
name|equals
argument_list|(
literal|"100000000000000000000000000000000000000"
argument_list|)
return|;
block|}
comment|//------------------------------------------------------------------------------------------------
specifier|public
specifier|static
name|String
index|[]
name|specialDecimalStrings
init|=
operator|new
name|String
index|[]
block|{
literal|"0"
block|,
literal|"1"
block|,
literal|"-1"
block|,
literal|"10"
block|,
literal|"-10"
block|,
literal|"100"
block|,
literal|"-100"
block|,
literal|"127"
block|,
comment|// Byte.MAX_VALUE
literal|"127.1"
block|,
literal|"127.0008"
block|,
literal|"127.49"
block|,
literal|"127.5"
block|,
literal|"127.9999999999999999999"
block|,
literal|"-127"
block|,
literal|"-127.1"
block|,
literal|"-127.0008"
block|,
literal|"-127.49"
block|,
literal|"-127.5"
block|,
literal|"-127.999999"
block|,
literal|"128"
block|,
literal|"128.1"
block|,
literal|"128.0008"
block|,
literal|"128.49"
block|,
literal|"128.5"
block|,
literal|"128.9999999999999999999"
block|,
literal|"-128"
block|,
comment|// Byte.MIN_VALUE
literal|"-128.1"
block|,
literal|"-128.0008"
block|,
literal|"-128.49"
block|,
literal|"-128.5"
block|,
literal|"-128.999"
block|,
literal|"129"
block|,
literal|"129.1"
block|,
literal|"-129"
block|,
literal|"-129.1"
block|,
literal|"1000"
block|,
literal|"-1000"
block|,
literal|"10000"
block|,
literal|"-10000"
block|,
literal|"32767"
block|,
comment|// Short.MAX_VALUE
literal|"32767.1"
block|,
literal|"32767.0008"
block|,
literal|"32767.49"
block|,
literal|"32767.5"
block|,
literal|"32767.99999999999"
block|,
literal|"-32767"
block|,
literal|"-32767.1"
block|,
literal|"-32767.0008"
block|,
literal|"-32767.49"
block|,
literal|"-32767.5"
block|,
literal|"-32767.9"
block|,
literal|"32768"
block|,
literal|"32768.1"
block|,
literal|"32768.0008"
block|,
literal|"32768.49"
block|,
literal|"32768.5"
block|,
literal|"32768.9999999999"
block|,
literal|"-32768"
block|,
comment|// Short.MIN_VALUE
literal|"-32768.1"
block|,
literal|"-32768.0008"
block|,
literal|"-32768.49"
block|,
literal|"-32768.5"
block|,
literal|"-32768.9999999"
block|,
literal|"32769"
block|,
literal|"32769.1"
block|,
literal|"-32769"
block|,
literal|"-32769.1"
block|,
literal|"100000"
block|,
literal|"-100000"
block|,
literal|"1000000"
block|,
literal|"-1000000"
block|,
literal|"10000000"
block|,
literal|"-10000000"
block|,
literal|"100000000"
block|,
literal|"99999999"
block|,
comment|// 10^8 - 1
literal|"-99999999"
block|,
literal|"-100000000"
block|,
literal|"1000000000"
block|,
literal|"-1000000000"
block|,
literal|"2147483647"
block|,
comment|// Integer.MAX_VALUE
literal|"2147483647.1"
block|,
literal|"2147483647.0008"
block|,
literal|"2147483647.49"
block|,
literal|"2147483647.5"
block|,
literal|"2147483647.9999999999"
block|,
literal|"-2147483647"
block|,
literal|"-2147483647.1"
block|,
literal|"-2147483647.0008"
block|,
literal|"-2147483647.49"
block|,
literal|"-2147483647.5"
block|,
literal|"-2147483647.9999999999999999999"
block|,
literal|"2147483648"
block|,
literal|"2147483648.1"
block|,
literal|"2147483648.0008"
block|,
literal|"2147483648.49"
block|,
literal|"2147483648.5"
block|,
literal|"2147483648.9"
block|,
literal|"-2147483648"
block|,
comment|// Integer.MIN_VALUE
literal|"-2147483648.1"
block|,
literal|"-2147483648.0008"
block|,
literal|"-2147483648.49"
block|,
literal|"-2147483648.5"
block|,
literal|"-2147483648.999"
block|,
literal|"2147483649"
block|,
literal|"2147483649.1"
block|,
literal|"-2147483649"
block|,
literal|"-2147483649.1"
block|,
literal|"10000000000"
block|,
literal|"-10000000000"
block|,
literal|"100000000000"
block|,
literal|"-100000000000"
block|,
literal|"1000000000000"
block|,
literal|"-1000000000000"
block|,
literal|"10000000000000"
block|,
literal|"-10000000000000"
block|,
literal|"100000000000000"
block|,
literal|"-100000000000000"
block|,
literal|"999999999999999"
block|,
literal|"-999999999999999"
block|,
literal|"1000000000000000"
block|,
comment|// 10^15
literal|"-1000000000000000"
block|,
literal|"9999999999999999"
block|,
comment|// 10^16 - 1
literal|"-9999999999999999"
block|,
literal|"10000000000000000"
block|,
comment|// 10^16
literal|"-10000000000000000"
block|,
literal|"99999999999999999"
block|,
comment|// 10^17 - 1
literal|"-99999999999999999"
block|,
literal|"100000000000000000"
block|,
literal|"-100000000000000000"
block|,
literal|"999999999999999999"
block|,
comment|// 10^18 - 1
literal|"-999999999999999999"
block|,
literal|"123456789012345678"
block|,
literal|"-123456789012345678"
block|,
literal|"1000000000000000000"
block|,
literal|"-1000000000000000000"
block|,
literal|"9223372036854775807"
block|,
comment|// Long.MAX_VALUE
literal|"9223372036854775807.1"
block|,
literal|"9223372036854775807.0008"
block|,
literal|"9223372036854775807.49"
block|,
literal|"9223372036854775807.5"
block|,
literal|"9223372036854775807.9"
block|,
literal|"-9223372036854775807"
block|,
literal|"-9223372036854775807.1"
block|,
literal|"-9223372036854775807.0008"
block|,
literal|"-9223372036854775807.49"
block|,
literal|"-9223372036854775807.5"
block|,
literal|"-9223372036854775807.9999999999999999999"
block|,
literal|"-9223372036854775808"
block|,
literal|"-9223372036854775808.1"
block|,
literal|"9223372036854775808"
block|,
literal|"9223372036854775808.1"
block|,
literal|"9223372036854775808.0008"
block|,
literal|"9223372036854775808.49"
block|,
literal|"9223372036854775808.5"
block|,
literal|"9223372036854775808.9"
block|,
literal|"9223372036854775809"
block|,
literal|"9223372036854775809.1"
block|,
literal|"-9223372036854775808"
block|,
comment|// Long.MIN_VALUE
literal|"-9223372036854775808.1"
block|,
literal|"-9223372036854775808.0008"
block|,
literal|"-9223372036854775808.49"
block|,
literal|"-9223372036854775808.5"
block|,
literal|"-9223372036854775808.9999999"
block|,
literal|"9223372036854775809"
block|,
literal|"9223372036854775809.1"
block|,
literal|"-9223372036854775809"
block|,
literal|"-9223372036854775809.1"
block|,
literal|"10000000000000000000000000000000"
block|,
comment|// 10^31
literal|"-10000000000000000000000000000000"
block|,
literal|"99999999999999999999999999999999"
block|,
comment|// 10^32 - 1
literal|"-99999999999999999999999999999999"
block|,
literal|"100000000000000000000000000000000"
block|,
comment|// 10^32
literal|"-100000000000000000000000000000000"
block|,
literal|"10000000000000000000000000000000000000"
block|,
comment|// 10^37
literal|"-10000000000000000000000000000000000000"
block|,
literal|"99999999999999999999999999999999999999"
block|,
comment|// 10^38 - 1
literal|"-99999999999999999999999999999999999999"
block|,
literal|"100000000000000000000000000000000000000"
block|,
comment|// 10^38
literal|"-100000000000000000000000000000000000000"
block|,
literal|"1000000000000000000000000000000000000000"
block|,
comment|// 10^39
literal|"-1000000000000000000000000000000000000000"
block|,
literal|"18446744073709551616"
block|,
comment|// Unsigned 64 max.
literal|"-18446744073709551616"
block|,
literal|"340282366920938463463374607431768211455"
block|,
comment|// 2^128 - 1
literal|"-340282366920938463463374607431768211455"
block|,
literal|"0.999999999999999"
block|,
literal|"-0.999999999999999"
block|,
literal|"0.0000000000000001"
block|,
comment|// 10^-15
literal|"-0.0000000000000001"
block|,
literal|"0.9999999999999999"
block|,
literal|"-0.9999999999999999"
block|,
literal|"0.00000000000000001"
block|,
comment|// 10^-16
literal|"-0.00000000000000001"
block|,
literal|"0.99999999999999999"
block|,
literal|"-0.99999999999999999"
block|,
literal|"0.999999999999999999"
block|,
comment|// 10^-18
literal|"-0.999999999999999999"
block|,
literal|"0.00000000000000000000000000000001"
block|,
comment|// 10^-31
literal|"-0.00000000000000000000000000000001"
block|,
literal|"0.99999999999999999999999999999999"
block|,
comment|// 10^-32 + 1
literal|"-0.99999999999999999999999999999999"
block|,
literal|"0.000000000000000000000000000000001"
block|,
comment|// 10^-32
literal|"-0.000000000000000000000000000000001"
block|,
literal|"0.00000000000000000000000000000000000001"
block|,
comment|// 10^-37
literal|"-0.00000000000000000000000000000000000001"
block|,
literal|"0.99999999999999999999999999999999999999"
block|,
comment|// 10^-38 + 1
literal|"-0.99999999999999999999999999999999999999"
block|,
literal|"0.000000000000000000000000000000000000001"
block|,
comment|// 10^-38
literal|"-0.000000000000000000000000000000000000001"
block|,
literal|"0.0000000000000000000000000000000000000001"
block|,
comment|// 10^-39
literal|"-0.0000000000000000000000000000000000000001"
block|,
literal|"0.0000000000000000000000000000000000000005"
block|,
comment|// 10^-39  (rounds)
literal|"-0.0000000000000000000000000000000000000005"
block|,
literal|"0.340282366920938463463374607431768211455"
block|,
comment|// (2^128 - 1) * 10^-39
literal|"-0.340282366920938463463374607431768211455"
block|,
literal|"0.000000000000000000000000000000000000001"
block|,
comment|// 10^-38
literal|"-0.000000000000000000000000000000000000001"
block|,
literal|"0.000000000000000000000000000000000000005"
block|,
comment|// 10^-38
literal|"-0.000000000000000000000000000000000000005"
block|,
literal|"234.79"
block|,
literal|"342348.343"
block|,
literal|"12.25"
block|,
literal|"-12.25"
block|,
literal|"72057594037927935"
block|,
comment|// 2^56 - 1
literal|"-72057594037927935"
block|,
literal|"72057594037927936"
block|,
comment|// 2^56
literal|"-72057594037927936"
block|,
literal|"5192296858534827628530496329220095"
block|,
comment|// 2^56 * 2^56 - 1
literal|"-5192296858534827628530496329220095"
block|,
literal|"5192296858534827628530496329220096"
block|,
comment|// 2^56 * 2^56
literal|"-5192296858534827628530496329220096"
block|,
literal|"54216721532321902598.70"
block|,
literal|"-906.62545207002374150309544832320"
block|,
literal|"-0.0709351061072"
block|,
literal|"1460849063411925.53"
block|,
literal|"8.809130E-33"
block|,
literal|"-4.0786300706013636202E-20"
block|,
literal|"-3.8823936518E-1"
block|,
literal|"-3.8823936518E-28"
block|,
literal|"-3.8823936518E-29"
block|,
literal|"598575157855521918987423259.94094"
block|,
literal|"299999448432.001342152474197"
block|,
literal|"1786135888657847525803324040144343378.09799306448796128931113691624"
block|,
comment|// More than 38 digits.
literal|"-1786135888657847525803324040144343378.09799306448796128931113691624"
block|,
literal|"57847525803324040144343378.09799306448796128931113691624"
block|,
literal|"0.999999999999999999990000"
block|,
literal|"005.34000"
block|,
literal|"1E-90"
block|,
literal|"0.4"
block|,
literal|"-0.4"
block|,
literal|"0.5"
block|,
literal|"-0.5"
block|,
literal|"0.6"
block|,
literal|"-0.6"
block|,
literal|"1.4"
block|,
literal|"-1.4"
block|,
literal|"1.5"
block|,
literal|"-1.5"
block|,
literal|"1.6"
block|,
literal|"-1.6"
block|,
literal|"2.4"
block|,
literal|"-2.4"
block|,
literal|"2.49"
block|,
literal|"-2.49"
block|,
literal|"2.5"
block|,
literal|"-2.5"
block|,
literal|"2.51"
block|,
literal|"-2.51"
block|,
literal|"-2.5"
block|,
literal|"2.6"
block|,
literal|"-2.6"
block|,
literal|"3.00001415926"
block|,
literal|"0.00"
block|,
literal|"-12.25"
block|,
literal|"234.79"
block|}
decl_stmt|;
specifier|public
specifier|static
name|BigDecimal
index|[]
name|specialBigDecimals
init|=
name|stringArrayToBigDecimals
argument_list|(
name|specialDecimalStrings
argument_list|)
decl_stmt|;
comment|// decimal_1_1.txt
specifier|public
specifier|static
name|String
index|[]
name|decimal_1_1_txt
init|=
block|{
literal|"0.0"
block|,
literal|"0.0000"
block|,
literal|".0"
block|,
literal|"0.1"
block|,
literal|"0.15"
block|,
literal|"0.9"
block|,
literal|"0.94"
block|,
literal|"0.99"
block|,
literal|"0.345"
block|,
literal|"1.0"
block|,
literal|"1"
block|,
literal|"0"
block|,
literal|"00"
block|,
literal|"22"
block|,
literal|"1E-9"
block|,
literal|"-0.0"
block|,
literal|"-0.0000"
block|,
literal|"-.0"
block|,
literal|"-0.1"
block|,
literal|"-0.15"
block|,
literal|"-0.9"
block|,
literal|"-0.94"
block|,
literal|"-0.99"
block|,
literal|"-0.345"
block|,
literal|"-1.0"
block|,
literal|"-1"
block|,
literal|"-0"
block|,
literal|"-00"
block|,
literal|"-22"
block|,
literal|"-1E-9"
block|}
decl_stmt|;
comment|// kv7.txt KEYS
specifier|public
specifier|static
name|String
index|[]
name|kv7_txt_keys
init|=
block|{
literal|"-4400"
block|,
literal|"1E+99"
block|,
literal|"1E-99"
block|,
literal|"0"
block|,
literal|"100"
block|,
literal|"10"
block|,
literal|"1"
block|,
literal|"0.1"
block|,
literal|"0.01"
block|,
literal|"200"
block|,
literal|"20"
block|,
literal|"2"
block|,
literal|"0"
block|,
literal|"0.2"
block|,
literal|"0.02"
block|,
literal|"0.3"
block|,
literal|"0.33"
block|,
literal|"0.333"
block|,
literal|"-0.3"
block|,
literal|"-0.33"
block|,
literal|"-0.333"
block|,
literal|"1.0"
block|,
literal|"2"
block|,
literal|"3.14"
block|,
literal|"-1.12"
block|,
literal|"-1.12"
block|,
literal|"-1.122"
block|,
literal|"1.12"
block|,
literal|"1.122"
block|,
literal|"124.00"
block|,
literal|"125.2"
block|,
literal|"-1255.49"
block|,
literal|"3.14"
block|,
literal|"3.14"
block|,
literal|"3.140"
block|,
literal|"0.9999999999999999999999999"
block|,
literal|"-1234567890.1234567890"
block|,
literal|"1234567890.1234567800"
block|}
decl_stmt|;
specifier|public
specifier|static
name|String
name|standardAlphabet
init|=
literal|"0123456789"
decl_stmt|;
specifier|public
specifier|static
name|String
index|[]
name|sparseAlphabets
init|=
operator|new
name|String
index|[]
block|{
literal|"0000000000000000000000000000000000000003"
block|,
literal|"0000000000000000000000000000000000000009"
block|,
literal|"0000000000000000000000000000000000000001"
block|,
literal|"0000000000000000000003"
block|,
literal|"0000000000000000000009"
block|,
literal|"0000000000000000000001"
block|,
literal|"0000000000091"
block|,
literal|"000000000005"
block|,
literal|"9"
block|,
literal|"5555555555999999999000000000000001111111"
block|,
literal|"24680"
block|,
literal|"1"
block|}
decl_stmt|;
specifier|public
specifier|static
name|BigDecimal
index|[]
name|stringArrayToBigDecimals
parameter_list|(
name|String
index|[]
name|strings
parameter_list|)
block|{
name|BigDecimal
index|[]
name|result
init|=
operator|new
name|BigDecimal
index|[
name|strings
operator|.
name|length
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
name|strings
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|result
index|[
name|i
index|]
operator|=
operator|new
name|BigDecimal
argument_list|(
name|strings
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

