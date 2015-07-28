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
name|common
operator|.
name|type
package|;
end_package

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
name|java
operator|.
name|math
operator|.
name|RoundingMode
import|;
end_import

begin_comment
comment|/**  *  * HiveDecimal. Simple wrapper for BigDecimal. Adds fixed max precision and non scientific string  * representation  *  */
end_comment

begin_class
specifier|public
class|class
name|HiveDecimal
implements|implements
name|Comparable
argument_list|<
name|HiveDecimal
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|int
name|MAX_PRECISION
init|=
literal|38
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|MAX_SCALE
init|=
literal|38
decl_stmt|;
comment|/**    * Default precision/scale when user doesn't specify in the column metadata, such as    * decimal and decimal(8).    */
specifier|public
specifier|static
specifier|final
name|int
name|USER_DEFAULT_PRECISION
init|=
literal|10
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|USER_DEFAULT_SCALE
init|=
literal|0
decl_stmt|;
comment|/**    *  Default precision/scale when system is not able to determine them, such as in case    *  of a non-generic udf.    */
specifier|public
specifier|static
specifier|final
name|int
name|SYSTEM_DEFAULT_PRECISION
init|=
literal|38
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|SYSTEM_DEFAULT_SCALE
init|=
literal|18
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HiveDecimal
name|ZERO
init|=
operator|new
name|HiveDecimal
argument_list|(
name|BigDecimal
operator|.
name|ZERO
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|HiveDecimal
name|ONE
init|=
operator|new
name|HiveDecimal
argument_list|(
name|BigDecimal
operator|.
name|ONE
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|ROUND_FLOOR
init|=
name|BigDecimal
operator|.
name|ROUND_FLOOR
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|ROUND_CEILING
init|=
name|BigDecimal
operator|.
name|ROUND_CEILING
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|ROUND_HALF_UP
init|=
name|BigDecimal
operator|.
name|ROUND_HALF_UP
decl_stmt|;
specifier|private
name|BigDecimal
name|bd
init|=
name|BigDecimal
operator|.
name|ZERO
decl_stmt|;
specifier|private
name|HiveDecimal
parameter_list|(
name|BigDecimal
name|bd
parameter_list|)
block|{
name|this
operator|.
name|bd
operator|=
name|bd
expr_stmt|;
block|}
specifier|public
specifier|static
name|HiveDecimal
name|create
parameter_list|(
name|BigDecimal
name|b
parameter_list|)
block|{
return|return
name|create
argument_list|(
name|b
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HiveDecimal
name|create
parameter_list|(
name|BigDecimal
name|b
parameter_list|,
name|boolean
name|allowRounding
parameter_list|)
block|{
name|BigDecimal
name|bd
init|=
name|normalize
argument_list|(
name|b
argument_list|,
name|allowRounding
argument_list|)
decl_stmt|;
return|return
name|bd
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|HiveDecimal
argument_list|(
name|bd
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HiveDecimal
name|create
parameter_list|(
name|BigInteger
name|unscaled
parameter_list|,
name|int
name|scale
parameter_list|)
block|{
name|BigDecimal
name|bd
init|=
name|normalize
argument_list|(
operator|new
name|BigDecimal
argument_list|(
name|unscaled
argument_list|,
name|scale
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
return|return
name|bd
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|HiveDecimal
argument_list|(
name|bd
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HiveDecimal
name|create
parameter_list|(
name|String
name|dec
parameter_list|)
block|{
name|BigDecimal
name|bd
decl_stmt|;
try|try
block|{
name|bd
operator|=
operator|new
name|BigDecimal
argument_list|(
name|dec
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|ex
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
name|bd
operator|=
name|normalize
argument_list|(
name|bd
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
name|bd
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|HiveDecimal
argument_list|(
name|bd
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HiveDecimal
name|create
parameter_list|(
name|BigInteger
name|bi
parameter_list|)
block|{
name|BigDecimal
name|bd
init|=
name|normalize
argument_list|(
operator|new
name|BigDecimal
argument_list|(
name|bi
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
return|return
name|bd
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|HiveDecimal
argument_list|(
name|bd
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HiveDecimal
name|create
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
operator|new
name|HiveDecimal
argument_list|(
operator|new
name|BigDecimal
argument_list|(
name|i
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HiveDecimal
name|create
parameter_list|(
name|long
name|l
parameter_list|)
block|{
return|return
operator|new
name|HiveDecimal
argument_list|(
operator|new
name|BigDecimal
argument_list|(
name|l
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|bd
operator|.
name|toPlainString
argument_list|()
return|;
block|}
specifier|public
name|HiveDecimal
name|setScale
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
operator|new
name|HiveDecimal
argument_list|(
name|bd
operator|.
name|setScale
argument_list|(
name|i
argument_list|,
name|RoundingMode
operator|.
name|HALF_UP
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|HiveDecimal
name|dec
parameter_list|)
block|{
return|return
name|bd
operator|.
name|compareTo
argument_list|(
name|dec
operator|.
name|bd
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|bd
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
literal|null
operator|||
name|obj
operator|.
name|getClass
argument_list|()
operator|!=
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|bd
operator|.
name|equals
argument_list|(
operator|(
operator|(
name|HiveDecimal
operator|)
name|obj
operator|)
operator|.
name|bd
argument_list|)
return|;
block|}
specifier|public
name|int
name|scale
parameter_list|()
block|{
return|return
name|bd
operator|.
name|scale
argument_list|()
return|;
block|}
comment|/**    * Returns the number of digits (integer and fractional) in the number, which is equivalent    * to SQL decimal precision. Note that this is different from BigDecimal.precision(),    * which returns the precision of the unscaled value (BigDecimal.valueOf(0.01).precision() = 1,    * whereas HiveDecimal.create("0.01").precision() = 2).    * If you want the BigDecimal precision, use HiveDecimal.bigDecimalValue().precision()    * @return    */
specifier|public
name|int
name|precision
parameter_list|()
block|{
name|int
name|bdPrecision
init|=
name|bd
operator|.
name|precision
argument_list|()
decl_stmt|;
name|int
name|bdScale
init|=
name|bd
operator|.
name|scale
argument_list|()
decl_stmt|;
if|if
condition|(
name|bdPrecision
operator|<
name|bdScale
condition|)
block|{
comment|// This can happen for numbers less than 0.1
comment|// For 0.001234: bdPrecision=4, bdScale=6
comment|// In this case, we'll set the type to have the same precision as the scale.
return|return
name|bdScale
return|;
block|}
return|return
name|bdPrecision
return|;
block|}
specifier|public
name|int
name|intValue
parameter_list|()
block|{
return|return
name|bd
operator|.
name|intValue
argument_list|()
return|;
block|}
specifier|public
name|double
name|doubleValue
parameter_list|()
block|{
return|return
name|bd
operator|.
name|doubleValue
argument_list|()
return|;
block|}
specifier|public
name|long
name|longValue
parameter_list|()
block|{
return|return
name|bd
operator|.
name|longValue
argument_list|()
return|;
block|}
specifier|public
name|short
name|shortValue
parameter_list|()
block|{
return|return
name|bd
operator|.
name|shortValue
argument_list|()
return|;
block|}
specifier|public
name|float
name|floatValue
parameter_list|()
block|{
return|return
name|bd
operator|.
name|floatValue
argument_list|()
return|;
block|}
specifier|public
name|BigDecimal
name|bigDecimalValue
parameter_list|()
block|{
return|return
name|bd
return|;
block|}
specifier|public
name|byte
name|byteValue
parameter_list|()
block|{
return|return
name|bd
operator|.
name|byteValue
argument_list|()
return|;
block|}
specifier|public
name|HiveDecimal
name|setScale
parameter_list|(
name|int
name|adjustedScale
parameter_list|,
name|int
name|rm
parameter_list|)
block|{
return|return
name|create
argument_list|(
name|bd
operator|.
name|setScale
argument_list|(
name|adjustedScale
argument_list|,
name|rm
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|HiveDecimal
name|subtract
parameter_list|(
name|HiveDecimal
name|dec
parameter_list|)
block|{
return|return
name|create
argument_list|(
name|bd
operator|.
name|subtract
argument_list|(
name|dec
operator|.
name|bd
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|HiveDecimal
name|multiply
parameter_list|(
name|HiveDecimal
name|dec
parameter_list|)
block|{
return|return
name|create
argument_list|(
name|bd
operator|.
name|multiply
argument_list|(
name|dec
operator|.
name|bd
argument_list|)
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|public
name|BigInteger
name|unscaledValue
parameter_list|()
block|{
return|return
name|bd
operator|.
name|unscaledValue
argument_list|()
return|;
block|}
specifier|public
name|HiveDecimal
name|scaleByPowerOfTen
parameter_list|(
name|int
name|n
parameter_list|)
block|{
return|return
name|create
argument_list|(
name|bd
operator|.
name|scaleByPowerOfTen
argument_list|(
name|n
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|HiveDecimal
name|abs
parameter_list|()
block|{
return|return
name|create
argument_list|(
name|bd
operator|.
name|abs
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|HiveDecimal
name|negate
parameter_list|()
block|{
return|return
name|create
argument_list|(
name|bd
operator|.
name|negate
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|HiveDecimal
name|add
parameter_list|(
name|HiveDecimal
name|dec
parameter_list|)
block|{
return|return
name|create
argument_list|(
name|bd
operator|.
name|add
argument_list|(
name|dec
operator|.
name|bd
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|HiveDecimal
name|pow
parameter_list|(
name|int
name|n
parameter_list|)
block|{
name|BigDecimal
name|result
init|=
name|normalize
argument_list|(
name|bd
operator|.
name|pow
argument_list|(
name|n
argument_list|)
argument_list|,
literal|false
argument_list|)
decl_stmt|;
return|return
name|result
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|HiveDecimal
argument_list|(
name|result
argument_list|)
return|;
block|}
specifier|public
name|HiveDecimal
name|remainder
parameter_list|(
name|HiveDecimal
name|dec
parameter_list|)
block|{
return|return
name|create
argument_list|(
name|bd
operator|.
name|remainder
argument_list|(
name|dec
operator|.
name|bd
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|HiveDecimal
name|divide
parameter_list|(
name|HiveDecimal
name|dec
parameter_list|)
block|{
return|return
name|create
argument_list|(
name|bd
operator|.
name|divide
argument_list|(
name|dec
operator|.
name|bd
argument_list|,
name|MAX_SCALE
argument_list|,
name|RoundingMode
operator|.
name|HALF_UP
argument_list|)
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * Get the sign of the underlying decimal.    * @return 0 if the decimal is equal to 0, -1 if less than zero, and 1 if greater than 0    */
specifier|public
name|int
name|signum
parameter_list|()
block|{
return|return
name|bd
operator|.
name|signum
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|BigDecimal
name|trim
parameter_list|(
name|BigDecimal
name|d
parameter_list|)
block|{
if|if
condition|(
name|d
operator|.
name|compareTo
argument_list|(
name|BigDecimal
operator|.
name|ZERO
argument_list|)
operator|==
literal|0
condition|)
block|{
comment|// Special case for 0, because java doesn't strip zeros correctly on that number.
name|d
operator|=
name|BigDecimal
operator|.
name|ZERO
expr_stmt|;
block|}
else|else
block|{
name|d
operator|=
name|d
operator|.
name|stripTrailingZeros
argument_list|()
expr_stmt|;
if|if
condition|(
name|d
operator|.
name|scale
argument_list|()
operator|<
literal|0
condition|)
block|{
comment|// no negative scale decimals
name|d
operator|=
name|d
operator|.
name|setScale
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|d
return|;
block|}
specifier|private
specifier|static
name|BigDecimal
name|normalize
parameter_list|(
name|BigDecimal
name|bd
parameter_list|,
name|boolean
name|allowRounding
parameter_list|)
block|{
if|if
condition|(
name|bd
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|bd
operator|=
name|trim
argument_list|(
name|bd
argument_list|)
expr_stmt|;
name|int
name|intDigits
init|=
name|bd
operator|.
name|precision
argument_list|()
operator|-
name|bd
operator|.
name|scale
argument_list|()
decl_stmt|;
if|if
condition|(
name|intDigits
operator|>
name|MAX_PRECISION
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|maxScale
init|=
name|Math
operator|.
name|min
argument_list|(
name|MAX_SCALE
argument_list|,
name|Math
operator|.
name|min
argument_list|(
name|MAX_PRECISION
operator|-
name|intDigits
argument_list|,
name|bd
operator|.
name|scale
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|bd
operator|.
name|scale
argument_list|()
operator|>
name|maxScale
condition|)
block|{
if|if
condition|(
name|allowRounding
condition|)
block|{
name|bd
operator|=
name|bd
operator|.
name|setScale
argument_list|(
name|maxScale
argument_list|,
name|RoundingMode
operator|.
name|HALF_UP
argument_list|)
expr_stmt|;
comment|// Trimming is again necessary, because rounding may introduce new trailing 0's.
name|bd
operator|=
name|trim
argument_list|(
name|bd
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|bd
operator|=
literal|null
expr_stmt|;
block|}
block|}
return|return
name|bd
return|;
block|}
specifier|public
specifier|static
name|BigDecimal
name|enforcePrecisionScale
parameter_list|(
name|BigDecimal
name|bd
parameter_list|,
name|int
name|maxPrecision
parameter_list|,
name|int
name|maxScale
parameter_list|)
block|{
if|if
condition|(
name|bd
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|bd
operator|=
name|trim
argument_list|(
name|bd
argument_list|)
expr_stmt|;
if|if
condition|(
name|bd
operator|.
name|scale
argument_list|()
operator|>
name|maxScale
condition|)
block|{
name|bd
operator|=
name|bd
operator|.
name|setScale
argument_list|(
name|maxScale
argument_list|,
name|RoundingMode
operator|.
name|HALF_UP
argument_list|)
expr_stmt|;
block|}
name|int
name|maxIntDigits
init|=
name|maxPrecision
operator|-
name|maxScale
decl_stmt|;
name|int
name|intDigits
init|=
name|bd
operator|.
name|precision
argument_list|()
operator|-
name|bd
operator|.
name|scale
argument_list|()
decl_stmt|;
if|if
condition|(
name|intDigits
operator|>
name|maxIntDigits
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|bd
return|;
block|}
specifier|public
specifier|static
name|HiveDecimal
name|enforcePrecisionScale
parameter_list|(
name|HiveDecimal
name|dec
parameter_list|,
name|int
name|maxPrecision
parameter_list|,
name|int
name|maxScale
parameter_list|)
block|{
if|if
condition|(
name|dec
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Minor optimization, avoiding creating new objects.
if|if
condition|(
name|dec
operator|.
name|precision
argument_list|()
operator|-
name|dec
operator|.
name|scale
argument_list|()
operator|<=
name|maxPrecision
operator|-
name|maxScale
operator|&&
name|dec
operator|.
name|scale
argument_list|()
operator|<=
name|maxScale
condition|)
block|{
return|return
name|dec
return|;
block|}
name|BigDecimal
name|bd
init|=
name|enforcePrecisionScale
argument_list|(
name|dec
operator|.
name|bd
argument_list|,
name|maxPrecision
argument_list|,
name|maxScale
argument_list|)
decl_stmt|;
if|if
condition|(
name|bd
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|HiveDecimal
operator|.
name|create
argument_list|(
name|bd
argument_list|)
return|;
block|}
block|}
end_class

end_unit

