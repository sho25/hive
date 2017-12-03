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
name|Arrays
import|;
end_import

begin_class
specifier|public
class|class
name|HLLDenseRegister
implements|implements
name|HLLRegister
block|{
comment|// 2^p number of bytes for register
specifier|private
name|byte
index|[]
name|register
decl_stmt|;
comment|// max value stored in registered is cached to determine the bit width for
comment|// bit packing
specifier|private
name|int
name|maxRegisterValue
decl_stmt|;
comment|// number of register bits
specifier|private
name|int
name|p
decl_stmt|;
comment|// m = 2^p
specifier|private
name|int
name|m
decl_stmt|;
specifier|public
name|HLLDenseRegister
parameter_list|(
name|int
name|p
parameter_list|)
block|{
name|this
argument_list|(
name|p
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HLLDenseRegister
parameter_list|(
name|int
name|p
parameter_list|,
name|boolean
name|bitPack
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
name|m
operator|=
literal|1
operator|<<
name|p
expr_stmt|;
name|this
operator|.
name|register
operator|=
operator|new
name|byte
index|[
name|m
index|]
expr_stmt|;
name|this
operator|.
name|maxRegisterValue
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|bitPack
operator|==
literal|false
condition|)
block|{
name|this
operator|.
name|maxRegisterValue
operator|=
literal|0xff
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|add
parameter_list|(
name|long
name|hashcode
parameter_list|)
block|{
comment|// LSB p bits
specifier|final
name|int
name|registerIdx
init|=
call|(
name|int
call|)
argument_list|(
name|hashcode
operator|&
operator|(
name|m
operator|-
literal|1
operator|)
argument_list|)
decl_stmt|;
comment|// MSB 64 - p bits
specifier|final
name|long
name|w
init|=
name|hashcode
operator|>>>
name|p
decl_stmt|;
comment|// longest run of trailing zeroes
specifier|final
name|int
name|lr
init|=
name|Long
operator|.
name|numberOfTrailingZeros
argument_list|(
name|w
argument_list|)
operator|+
literal|1
decl_stmt|;
return|return
name|set
argument_list|(
name|registerIdx
argument_list|,
operator|(
name|byte
operator|)
name|lr
argument_list|)
return|;
block|}
comment|// this is a lossy invert of the function above, which produces a hashcode
comment|// which collides with the current winner of the register (we lose all higher
comment|// bits, but we get all bits useful for lesser p-bit options)
comment|// +-------------|-------------+
comment|// |xxxx100000000|1000000000000|  (lr=9 + idx=1024)
comment|// +-------------|-------------+
comment|//                \
comment|// +---------------|-----------+
comment|// |xxxx10000000010|00000000000|  (lr=2 + idx=0)
comment|// +---------------|-----------+
comment|// This shows the relevant bits of the original hash value
comment|// and how the conversion is moving bits from the index value
comment|// over to the leading zero computation
specifier|public
name|void
name|extractLowBitsTo
parameter_list|(
name|HLLRegister
name|dest
parameter_list|)
block|{
for|for
control|(
name|int
name|idx
init|=
literal|0
init|;
name|idx
operator|<
name|register
operator|.
name|length
condition|;
name|idx
operator|++
control|)
block|{
name|byte
name|lr
init|=
name|register
index|[
name|idx
index|]
decl_stmt|;
comment|// this can be a max of 65, never> 127
if|if
condition|(
name|lr
operator|!=
literal|0
condition|)
block|{
name|dest
operator|.
name|add
argument_list|(
call|(
name|long
call|)
argument_list|(
operator|(
literal|1
operator|<<
operator|(
name|p
operator|+
name|lr
operator|-
literal|1
operator|)
operator|)
operator||
name|idx
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|boolean
name|set
parameter_list|(
name|int
name|idx
parameter_list|,
name|byte
name|value
parameter_list|)
block|{
name|boolean
name|updated
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|idx
argument_list|<
name|register
operator|.
name|length
operator|&&
name|value
argument_list|>
name|register
index|[
name|idx
index|]
condition|)
block|{
comment|// update max register value
if|if
condition|(
name|value
operator|>
name|maxRegisterValue
condition|)
block|{
name|maxRegisterValue
operator|=
name|value
expr_stmt|;
block|}
comment|// set register value and compute inverse pow of 2 for register value
name|register
index|[
name|idx
index|]
operator|=
name|value
expr_stmt|;
name|updated
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|updated
return|;
block|}
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|register
operator|.
name|length
return|;
block|}
specifier|public
name|int
name|getNumZeroes
parameter_list|()
block|{
name|int
name|numZeroes
init|=
literal|0
decl_stmt|;
for|for
control|(
name|byte
name|b
range|:
name|register
control|)
block|{
if|if
condition|(
name|b
operator|==
literal|0
condition|)
block|{
name|numZeroes
operator|++
expr_stmt|;
block|}
block|}
return|return
name|numZeroes
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
name|HLLDenseRegister
condition|)
block|{
name|HLLDenseRegister
name|hdr
init|=
operator|(
name|HLLDenseRegister
operator|)
name|hllRegister
decl_stmt|;
name|byte
index|[]
name|inRegister
init|=
name|hdr
operator|.
name|getRegister
argument_list|()
decl_stmt|;
comment|// merge only if the register length matches
if|if
condition|(
name|register
operator|.
name|length
operator|!=
name|inRegister
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The size of register sets of HyperLogLogs to be merged does not match."
argument_list|)
throw|;
block|}
comment|// compare register values and store the max register value
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|inRegister
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|byte
name|cb
init|=
name|register
index|[
name|i
index|]
decl_stmt|;
specifier|final
name|byte
name|ob
init|=
name|inRegister
index|[
name|i
index|]
decl_stmt|;
name|register
index|[
name|i
index|]
operator|=
name|ob
operator|>
name|cb
condition|?
name|ob
else|:
name|cb
expr_stmt|;
block|}
comment|// update max register value
if|if
condition|(
name|hdr
operator|.
name|getMaxRegisterValue
argument_list|()
operator|>
name|maxRegisterValue
condition|)
block|{
name|maxRegisterValue
operator|=
name|hdr
operator|.
name|getMaxRegisterValue
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Specified register is not instance of HLLDenseRegister"
argument_list|)
throw|;
block|}
block|}
specifier|public
name|byte
index|[]
name|getRegister
parameter_list|()
block|{
return|return
name|register
return|;
block|}
specifier|public
name|void
name|setRegister
parameter_list|(
name|byte
index|[]
name|register
parameter_list|)
block|{
name|this
operator|.
name|register
operator|=
name|register
expr_stmt|;
block|}
specifier|public
name|int
name|getMaxRegisterValue
parameter_list|()
block|{
return|return
name|maxRegisterValue
return|;
block|}
specifier|public
name|double
name|getSumInversePow2
parameter_list|()
block|{
name|double
name|sum
init|=
literal|0
decl_stmt|;
for|for
control|(
name|byte
name|b
range|:
name|register
control|)
block|{
name|sum
operator|+=
name|HLLConstants
operator|.
name|inversePow2Data
index|[
name|b
index|]
expr_stmt|;
block|}
return|return
name|sum
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
literal|"HLLDenseRegister - "
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
literal|" numZeroes: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getNumZeroes
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" maxRegisterValue: "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|maxRegisterValue
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
name|Arrays
operator|.
name|toString
argument_list|(
name|register
argument_list|)
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
name|HLLDenseRegister
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|HLLDenseRegister
name|other
init|=
operator|(
name|HLLDenseRegister
operator|)
name|obj
decl_stmt|;
return|return
name|maxRegisterValue
operator|==
name|other
operator|.
name|maxRegisterValue
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|register
argument_list|,
name|other
operator|.
name|register
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
name|int
name|hashcode
init|=
literal|0
decl_stmt|;
name|hashcode
operator|+=
literal|31
operator|*
name|maxRegisterValue
expr_stmt|;
name|hashcode
operator|+=
name|Arrays
operator|.
name|hashCode
argument_list|(
name|register
argument_list|)
expr_stmt|;
return|return
name|hashcode
return|;
block|}
block|}
end_class

end_unit

