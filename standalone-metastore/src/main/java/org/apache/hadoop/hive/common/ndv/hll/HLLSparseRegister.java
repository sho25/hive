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
name|ndv
operator|.
name|hll
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_class
specifier|public
class|class
name|HLLSparseRegister
implements|implements
name|HLLRegister
block|{
specifier|private
name|TreeMap
argument_list|<
name|Integer
argument_list|,
name|Byte
argument_list|>
name|sparseMap
decl_stmt|;
comment|// for a better insertion performance values are added to temporary unsorted
comment|// list which will be merged to sparse map after a threshold
specifier|private
name|int
index|[]
name|tempList
decl_stmt|;
specifier|private
name|int
name|tempListIdx
decl_stmt|;
comment|// number of register bits
specifier|private
specifier|final
name|int
name|p
decl_stmt|;
comment|// new number of register bits for higher accuracy
specifier|private
specifier|final
name|int
name|pPrime
decl_stmt|;
comment|// number of bits to store the number of zero runs
specifier|private
specifier|final
name|int
name|qPrime
decl_stmt|;
comment|// masks for quicker extraction of p, pPrime, qPrime values
specifier|private
specifier|final
name|int
name|mask
decl_stmt|;
specifier|private
specifier|final
name|int
name|pPrimeMask
decl_stmt|;
specifier|private
specifier|final
name|int
name|qPrimeMask
decl_stmt|;
specifier|public
name|HLLSparseRegister
parameter_list|(
name|int
name|p
parameter_list|,
name|int
name|pp
parameter_list|,
name|int
name|qp
parameter_list|)
block|{
name|this
operator|.
name|p
operator|=
name|p
expr_stmt|;
name|this
operator|.
name|sparseMap
operator|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|tempList
operator|=
operator|new
name|int
index|[
name|HLLConstants
operator|.
name|TEMP_LIST_DEFAULT_SIZE
index|]
expr_stmt|;
name|this
operator|.
name|tempListIdx
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|pPrime
operator|=
name|pp
expr_stmt|;
name|this
operator|.
name|qPrime
operator|=
name|qp
expr_stmt|;
name|this
operator|.
name|mask
operator|=
operator|(
operator|(
literal|1
operator|<<
name|pPrime
operator|)
operator|-
literal|1
operator|)
operator|^
operator|(
operator|(
literal|1
operator|<<
name|p
operator|)
operator|-
literal|1
operator|)
expr_stmt|;
name|this
operator|.
name|pPrimeMask
operator|=
operator|(
operator|(
literal|1
operator|<<
name|pPrime
operator|)
operator|-
literal|1
operator|)
expr_stmt|;
name|this
operator|.
name|qPrimeMask
operator|=
operator|(
literal|1
operator|<<
name|qPrime
operator|)
operator|-
literal|1
expr_stmt|;
block|}
specifier|public
name|boolean
name|add
parameter_list|(
name|long
name|hashcode
parameter_list|)
block|{
name|boolean
name|updated
init|=
literal|false
decl_stmt|;
comment|// fill the temp list before merging to sparse map
if|if
condition|(
name|tempListIdx
operator|<
name|tempList
operator|.
name|length
condition|)
block|{
name|int
name|encodedHash
init|=
name|encodeHash
argument_list|(
name|hashcode
argument_list|)
decl_stmt|;
name|tempList
index|[
name|tempListIdx
operator|++
index|]
operator|=
name|encodedHash
expr_stmt|;
name|updated
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|updated
operator|=
name|mergeTempListToSparseMap
argument_list|()
expr_stmt|;
block|}
return|return
name|updated
return|;
block|}
comment|/**    * Adds temp list to sparse map. The key for sparse map entry is the register    * index determined by pPrime and value is the number of trailing zeroes.    * @return    */
specifier|private
name|boolean
name|mergeTempListToSparseMap
parameter_list|()
block|{
name|boolean
name|updated
init|=
literal|false
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
name|tempListIdx
condition|;
name|i
operator|++
control|)
block|{
name|int
name|encodedHash
init|=
name|tempList
index|[
name|i
index|]
decl_stmt|;
name|int
name|key
init|=
name|encodedHash
operator|&
name|pPrimeMask
decl_stmt|;
name|byte
name|value
init|=
call|(
name|byte
call|)
argument_list|(
name|encodedHash
operator|>>>
name|pPrime
argument_list|)
decl_stmt|;
name|byte
name|nr
init|=
literal|0
decl_stmt|;
comment|// if MSB is set to 1 then next qPrime MSB bits contains the value of
comment|// number of zeroes.
comment|// if MSB is set to 0 then number of zeroes is contained within pPrime - p
comment|// bits.
if|if
condition|(
name|encodedHash
operator|<
literal|0
condition|)
block|{
name|nr
operator|=
call|(
name|byte
call|)
argument_list|(
name|value
operator|&
name|qPrimeMask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|nr
operator|=
call|(
name|byte
call|)
argument_list|(
name|Integer
operator|.
name|numberOfTrailingZeros
argument_list|(
name|encodedHash
operator|>>>
name|p
argument_list|)
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|updated
operator|=
name|set
argument_list|(
name|key
argument_list|,
name|nr
argument_list|)
expr_stmt|;
block|}
comment|// reset temp list index
name|tempListIdx
operator|=
literal|0
expr_stmt|;
return|return
name|updated
return|;
block|}
comment|/**    *<pre>    *<b>Input:</b> 64 bit hashcode    *     * |---------w-------------| |------------p'----------|    * 10101101.......1010101010 10101010101 01010101010101    *                                       |------p-----|    *                                           *<b>Output:</b> 32 bit int    *     * |b| |-q'-|  |------------p'----------|    *  1  010101  01010101010 10101010101010    *                         |------p-----|    *                        *     * The default values of p', q' and b are 25, 6, 1 (total 32 bits) respectively.    * This function will return an int encoded in the following format    *     * p  - LSB p bits represent the register index    * p' - LSB p' bits are used for increased accuracy in estimation    * q' - q' bits after p' are left as such from the hashcode if b = 0 else    *      q' bits encodes the longest trailing zero runs from in (w-p) input bits    * b  - 0 if longest trailing zero run is contained within (p'-p) bits    *      1 if longest trailing zero run is computeed from (w-p) input bits and    *      its value is stored in q' bits    *</pre>    * @param hashcode    * @return    */
specifier|public
name|int
name|encodeHash
parameter_list|(
name|long
name|hashcode
parameter_list|)
block|{
comment|// x = p' - p
name|int
name|x
init|=
call|(
name|int
call|)
argument_list|(
name|hashcode
operator|&
name|mask
argument_list|)
decl_stmt|;
if|if
condition|(
name|x
operator|==
literal|0
condition|)
block|{
comment|// more bits should be considered for finding q (longest zero runs)
comment|// set MSB to 1
name|int
name|ntr
init|=
name|Long
operator|.
name|numberOfTrailingZeros
argument_list|(
name|hashcode
operator|>>
name|p
argument_list|)
operator|+
literal|1
decl_stmt|;
name|long
name|newHashCode
init|=
name|hashcode
operator|&
name|pPrimeMask
decl_stmt|;
name|newHashCode
operator||=
name|ntr
operator|<<
name|pPrime
expr_stmt|;
name|newHashCode
operator||=
literal|0x80000000
expr_stmt|;
return|return
operator|(
name|int
operator|)
name|newHashCode
return|;
block|}
else|else
block|{
comment|// q is contained within p' - p
comment|// set MSB to 0
return|return
call|(
name|int
call|)
argument_list|(
name|hashcode
operator|&
literal|0x7FFFFFFF
argument_list|)
return|;
block|}
block|}
specifier|public
name|int
name|getSize
parameter_list|()
block|{
return|return
name|sparseMap
operator|.
name|size
argument_list|()
operator|+
name|tempListIdx
return|;
block|}
specifier|public
name|void
name|merge
parameter_list|(
name|HLLRegister
name|hllRegister
parameter_list|)
block|{
if|if
condition|(
name|hllRegister
operator|instanceof
name|HLLSparseRegister
condition|)
block|{
name|HLLSparseRegister
name|hsr
init|=
operator|(
name|HLLSparseRegister
operator|)
name|hllRegister
decl_stmt|;
comment|// retain only the largest value for a register index
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Integer
argument_list|,
name|Byte
argument_list|>
name|entry
range|:
name|hsr
operator|.
name|getSparseMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|int
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|byte
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|set
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Specified register not instance of HLLSparseRegister"
argument_list|)
throw|;
block|}
block|}
specifier|public
name|boolean
name|set
parameter_list|(
name|int
name|key
parameter_list|,
name|byte
name|value
parameter_list|)
block|{
comment|// retain only the largest value for a register index
name|Byte
name|containedValue
init|=
name|sparseMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|containedValue
operator|==
literal|null
operator|||
name|value
operator|>
name|containedValue
condition|)
block|{
name|sparseMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|TreeMap
argument_list|<
name|Integer
argument_list|,
name|Byte
argument_list|>
name|getSparseMap
parameter_list|()
block|{
return|return
name|getMergedSparseMap
argument_list|()
return|;
block|}
specifier|private
name|TreeMap
argument_list|<
name|Integer
argument_list|,
name|Byte
argument_list|>
name|getMergedSparseMap
parameter_list|()
block|{
if|if
condition|(
name|tempListIdx
operator|!=
literal|0
condition|)
block|{
name|mergeTempListToSparseMap
argument_list|()
expr_stmt|;
block|}
return|return
name|sparseMap
return|;
block|}
specifier|public
name|int
name|getP
parameter_list|()
block|{
return|return
name|p
return|;
block|}
specifier|public
name|int
name|getPPrime
parameter_list|()
block|{
return|return
name|pPrime
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"HLLSparseRegister - "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"p: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" pPrime: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|pPrime
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" qPrime: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|qPrime
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|String
name|toExtendedString
parameter_list|()
block|{
return|return
name|toString
argument_list|()
operator|+
literal|" register: "
operator|+
name|sparseMap
operator|.
name|toString
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
operator|!
operator|(
name|obj
operator|instanceof
name|HLLSparseRegister
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|HLLSparseRegister
name|other
init|=
operator|(
name|HLLSparseRegister
operator|)
name|obj
decl_stmt|;
name|boolean
name|result
init|=
name|p
operator|==
name|other
operator|.
name|p
operator|&&
name|pPrime
operator|==
name|other
operator|.
name|pPrime
operator|&&
name|qPrime
operator|==
name|other
operator|.
name|qPrime
operator|&&
name|tempListIdx
operator|==
name|other
operator|.
name|tempListIdx
decl_stmt|;
if|if
condition|(
name|result
condition|)
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
name|tempListIdx
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|tempList
index|[
name|i
index|]
operator|!=
name|other
operator|.
name|tempList
index|[
name|i
index|]
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
name|result
operator|=
name|result
operator|&&
name|sparseMap
operator|.
name|equals
argument_list|(
name|other
operator|.
name|sparseMap
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|hashcode
init|=
literal|0
decl_stmt|;
name|hashcode
operator|+=
literal|31
operator|*
name|p
expr_stmt|;
name|hashcode
operator|+=
literal|31
operator|*
name|pPrime
expr_stmt|;
name|hashcode
operator|+=
literal|31
operator|*
name|qPrime
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
name|tempListIdx
condition|;
name|i
operator|++
control|)
block|{
name|hashcode
operator|+=
literal|31
operator|*
name|tempList
index|[
name|tempListIdx
index|]
expr_stmt|;
block|}
name|hashcode
operator|+=
name|sparseMap
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|hashcode
return|;
block|}
block|}
end_class

end_unit

