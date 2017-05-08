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
name|hive
operator|.
name|common
operator|.
name|util
package|;
end_package

begin_comment
comment|/**  * Murmur3 is successor to Murmur2 fast non-crytographic hash algorithms.  *  * Murmur3 32 and 128 bit variants.  * 32-bit Java port of https://code.google.com/p/smhasher/source/browse/trunk/MurmurHash3.cpp#94  * 128-bit Java port of https://code.google.com/p/smhasher/source/browse/trunk/MurmurHash3.cpp#255  *  * This is a public domain code with no copyrights.  * From homepage of MurmurHash (https://code.google.com/p/smhasher/),  * "All MurmurHash versions are public domain software, and the author disclaims all copyright  * to their code."  */
end_comment

begin_class
specifier|public
class|class
name|Murmur3
block|{
comment|// from 64-bit linear congruential generator
specifier|public
specifier|static
specifier|final
name|long
name|NULL_HASHCODE
init|=
literal|2862933555777941757L
decl_stmt|;
comment|// Constants for 32 bit variant
specifier|private
specifier|static
specifier|final
name|int
name|C1_32
init|=
literal|0xcc9e2d51
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|C2_32
init|=
literal|0x1b873593
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|R1_32
init|=
literal|15
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|R2_32
init|=
literal|13
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|M_32
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|N_32
init|=
literal|0xe6546b64
decl_stmt|;
comment|// Constants for 128 bit variant
specifier|private
specifier|static
specifier|final
name|long
name|C1
init|=
literal|0x87c37b91114253d5L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|C2
init|=
literal|0x4cf5ad432745937fL
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|R1
init|=
literal|31
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|R2
init|=
literal|27
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|R3
init|=
literal|33
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|M
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|N1
init|=
literal|0x52dce729
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|N2
init|=
literal|0x38495ab5
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_SEED
init|=
literal|104729
decl_stmt|;
comment|/**    * Murmur3 32-bit variant.    *    * @param data - input byte array    * @return - hashcode    */
specifier|public
specifier|static
name|int
name|hash32
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
block|{
return|return
name|hash32
argument_list|(
name|data
argument_list|,
name|data
operator|.
name|length
argument_list|,
name|DEFAULT_SEED
argument_list|)
return|;
block|}
comment|/**    * Murmur3 32-bit variant.    *    * @param data   - input byte array    * @param length - length of array    * @param seed   - seed. (default 0)    * @return - hashcode    */
specifier|public
specifier|static
name|int
name|hash32
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|length
parameter_list|,
name|int
name|seed
parameter_list|)
block|{
name|int
name|hash
init|=
name|seed
decl_stmt|;
specifier|final
name|int
name|nblocks
init|=
name|length
operator|>>
literal|2
decl_stmt|;
comment|// body
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nblocks
condition|;
name|i
operator|++
control|)
block|{
name|int
name|i_4
init|=
name|i
operator|<<
literal|2
decl_stmt|;
name|int
name|k
init|=
operator|(
name|data
index|[
name|i_4
index|]
operator|&
literal|0xff
operator|)
operator||
operator|(
operator|(
name|data
index|[
name|i_4
operator|+
literal|1
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|8
operator|)
operator||
operator|(
operator|(
name|data
index|[
name|i_4
operator|+
literal|2
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|16
operator|)
operator||
operator|(
operator|(
name|data
index|[
name|i_4
operator|+
literal|3
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|24
operator|)
decl_stmt|;
comment|// mix functions
name|k
operator|*=
name|C1_32
expr_stmt|;
name|k
operator|=
name|Integer
operator|.
name|rotateLeft
argument_list|(
name|k
argument_list|,
name|R1_32
argument_list|)
expr_stmt|;
name|k
operator|*=
name|C2_32
expr_stmt|;
name|hash
operator|^=
name|k
expr_stmt|;
name|hash
operator|=
name|Integer
operator|.
name|rotateLeft
argument_list|(
name|hash
argument_list|,
name|R2_32
argument_list|)
operator|*
name|M_32
operator|+
name|N_32
expr_stmt|;
block|}
comment|// tail
name|int
name|idx
init|=
name|nblocks
operator|<<
literal|2
decl_stmt|;
name|int
name|k1
init|=
literal|0
decl_stmt|;
switch|switch
condition|(
name|length
operator|-
name|idx
condition|)
block|{
case|case
literal|3
case|:
name|k1
operator|^=
name|data
index|[
name|idx
operator|+
literal|2
index|]
operator|<<
literal|16
expr_stmt|;
case|case
literal|2
case|:
name|k1
operator|^=
name|data
index|[
name|idx
operator|+
literal|1
index|]
operator|<<
literal|8
expr_stmt|;
case|case
literal|1
case|:
name|k1
operator|^=
name|data
index|[
name|idx
index|]
expr_stmt|;
comment|// mix functions
name|k1
operator|*=
name|C1_32
expr_stmt|;
name|k1
operator|=
name|Integer
operator|.
name|rotateLeft
argument_list|(
name|k1
argument_list|,
name|R1_32
argument_list|)
expr_stmt|;
name|k1
operator|*=
name|C2_32
expr_stmt|;
name|hash
operator|^=
name|k1
expr_stmt|;
block|}
comment|// finalization
name|hash
operator|^=
name|length
expr_stmt|;
name|hash
operator|^=
operator|(
name|hash
operator|>>>
literal|16
operator|)
expr_stmt|;
name|hash
operator|*=
literal|0x85ebca6b
expr_stmt|;
name|hash
operator|^=
operator|(
name|hash
operator|>>>
literal|13
operator|)
expr_stmt|;
name|hash
operator|*=
literal|0xc2b2ae35
expr_stmt|;
name|hash
operator|^=
operator|(
name|hash
operator|>>>
literal|16
operator|)
expr_stmt|;
return|return
name|hash
return|;
block|}
comment|/**    * Murmur3 64-bit variant. This is essentially MSB 8 bytes of Murmur3 128-bit variant.    *    * @param data - input byte array    * @return - hashcode    */
specifier|public
specifier|static
name|long
name|hash64
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
block|{
return|return
name|hash64
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|,
name|DEFAULT_SEED
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|long
name|hash64
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
name|hash64
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|DEFAULT_SEED
argument_list|)
return|;
block|}
comment|/**    * Murmur3 64-bit variant. This is essentially MSB 8 bytes of Murmur3 128-bit variant.    *    * @param data   - input byte array    * @param length - length of array    * @param seed   - seed. (default is 0)    * @return - hashcode    */
specifier|public
specifier|static
name|long
name|hash64
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|int
name|seed
parameter_list|)
block|{
name|long
name|hash
init|=
name|seed
decl_stmt|;
specifier|final
name|int
name|nblocks
init|=
name|length
operator|>>
literal|3
decl_stmt|;
comment|// body
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nblocks
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|i8
init|=
name|i
operator|<<
literal|3
decl_stmt|;
name|long
name|k
init|=
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i8
index|]
operator|&
literal|0xff
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i8
operator|+
literal|1
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|8
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i8
operator|+
literal|2
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|16
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i8
operator|+
literal|3
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|24
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i8
operator|+
literal|4
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|32
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i8
operator|+
literal|5
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|40
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i8
operator|+
literal|6
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|48
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i8
operator|+
literal|7
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|56
operator|)
decl_stmt|;
comment|// mix functions
name|k
operator|*=
name|C1
expr_stmt|;
name|k
operator|=
name|Long
operator|.
name|rotateLeft
argument_list|(
name|k
argument_list|,
name|R1
argument_list|)
expr_stmt|;
name|k
operator|*=
name|C2
expr_stmt|;
name|hash
operator|^=
name|k
expr_stmt|;
name|hash
operator|=
name|Long
operator|.
name|rotateLeft
argument_list|(
name|hash
argument_list|,
name|R2
argument_list|)
operator|*
name|M
operator|+
name|N1
expr_stmt|;
block|}
comment|// tail
name|long
name|k1
init|=
literal|0
decl_stmt|;
name|int
name|tailStart
init|=
name|nblocks
operator|<<
literal|3
decl_stmt|;
switch|switch
condition|(
name|length
operator|-
name|tailStart
condition|)
block|{
case|case
literal|7
case|:
name|k1
operator|^=
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|6
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|48
expr_stmt|;
case|case
literal|6
case|:
name|k1
operator|^=
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|5
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|40
expr_stmt|;
case|case
literal|5
case|:
name|k1
operator|^=
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|4
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|32
expr_stmt|;
case|case
literal|4
case|:
name|k1
operator|^=
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|3
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|24
expr_stmt|;
case|case
literal|3
case|:
name|k1
operator|^=
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|2
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|16
expr_stmt|;
case|case
literal|2
case|:
name|k1
operator|^=
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|1
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|8
expr_stmt|;
case|case
literal|1
case|:
name|k1
operator|^=
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|tailStart
index|]
operator|&
literal|0xff
operator|)
expr_stmt|;
name|k1
operator|*=
name|C1
expr_stmt|;
name|k1
operator|=
name|Long
operator|.
name|rotateLeft
argument_list|(
name|k1
argument_list|,
name|R1
argument_list|)
expr_stmt|;
name|k1
operator|*=
name|C2
expr_stmt|;
name|hash
operator|^=
name|k1
expr_stmt|;
block|}
comment|// finalization
name|hash
operator|^=
name|length
expr_stmt|;
name|hash
operator|=
name|fmix64
argument_list|(
name|hash
argument_list|)
expr_stmt|;
return|return
name|hash
return|;
block|}
comment|/**    * Murmur3 128-bit variant.    *    * @param data - input byte array    * @return - hashcode (2 longs)    */
specifier|public
specifier|static
name|long
index|[]
name|hash128
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
block|{
return|return
name|hash128
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|,
name|DEFAULT_SEED
argument_list|)
return|;
block|}
comment|/**    * Murmur3 128-bit variant.    *    * @param data   - input byte array    * @param offset - the first element of array    * @param length - length of array    * @param seed   - seed. (default is 0)    * @return - hashcode (2 longs)    */
specifier|public
specifier|static
name|long
index|[]
name|hash128
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|int
name|seed
parameter_list|)
block|{
name|long
name|h1
init|=
name|seed
decl_stmt|;
name|long
name|h2
init|=
name|seed
decl_stmt|;
specifier|final
name|int
name|nblocks
init|=
name|length
operator|>>
literal|4
decl_stmt|;
comment|// body
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nblocks
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|i16
init|=
name|i
operator|<<
literal|4
decl_stmt|;
name|long
name|k1
init|=
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i16
index|]
operator|&
literal|0xff
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i16
operator|+
literal|1
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|8
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i16
operator|+
literal|2
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|16
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i16
operator|+
literal|3
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|24
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i16
operator|+
literal|4
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|32
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i16
operator|+
literal|5
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|40
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i16
operator|+
literal|6
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|48
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i16
operator|+
literal|7
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|56
operator|)
decl_stmt|;
name|long
name|k2
init|=
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i16
operator|+
literal|8
index|]
operator|&
literal|0xff
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i16
operator|+
literal|9
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|8
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i16
operator|+
literal|10
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|16
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i16
operator|+
literal|11
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|24
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i16
operator|+
literal|12
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|32
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i16
operator|+
literal|13
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|40
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i16
operator|+
literal|14
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|48
operator|)
operator||
operator|(
operator|(
operator|(
name|long
operator|)
name|data
index|[
name|offset
operator|+
name|i16
operator|+
literal|15
index|]
operator|&
literal|0xff
operator|)
operator|<<
literal|56
operator|)
decl_stmt|;
comment|// mix functions for k1
name|k1
operator|*=
name|C1
expr_stmt|;
name|k1
operator|=
name|Long
operator|.
name|rotateLeft
argument_list|(
name|k1
argument_list|,
name|R1
argument_list|)
expr_stmt|;
name|k1
operator|*=
name|C2
expr_stmt|;
name|h1
operator|^=
name|k1
expr_stmt|;
name|h1
operator|=
name|Long
operator|.
name|rotateLeft
argument_list|(
name|h1
argument_list|,
name|R2
argument_list|)
expr_stmt|;
name|h1
operator|+=
name|h2
expr_stmt|;
name|h1
operator|=
name|h1
operator|*
name|M
operator|+
name|N1
expr_stmt|;
comment|// mix functions for k2
name|k2
operator|*=
name|C2
expr_stmt|;
name|k2
operator|=
name|Long
operator|.
name|rotateLeft
argument_list|(
name|k2
argument_list|,
name|R3
argument_list|)
expr_stmt|;
name|k2
operator|*=
name|C1
expr_stmt|;
name|h2
operator|^=
name|k2
expr_stmt|;
name|h2
operator|=
name|Long
operator|.
name|rotateLeft
argument_list|(
name|h2
argument_list|,
name|R1
argument_list|)
expr_stmt|;
name|h2
operator|+=
name|h1
expr_stmt|;
name|h2
operator|=
name|h2
operator|*
name|M
operator|+
name|N2
expr_stmt|;
block|}
comment|// tail
name|long
name|k1
init|=
literal|0
decl_stmt|;
name|long
name|k2
init|=
literal|0
decl_stmt|;
name|int
name|tailStart
init|=
name|nblocks
operator|<<
literal|4
decl_stmt|;
switch|switch
condition|(
name|length
operator|-
name|tailStart
condition|)
block|{
case|case
literal|15
case|:
name|k2
operator|^=
call|(
name|long
call|)
argument_list|(
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|14
index|]
operator|&
literal|0xff
argument_list|)
operator|<<
literal|48
expr_stmt|;
case|case
literal|14
case|:
name|k2
operator|^=
call|(
name|long
call|)
argument_list|(
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|13
index|]
operator|&
literal|0xff
argument_list|)
operator|<<
literal|40
expr_stmt|;
case|case
literal|13
case|:
name|k2
operator|^=
call|(
name|long
call|)
argument_list|(
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|12
index|]
operator|&
literal|0xff
argument_list|)
operator|<<
literal|32
expr_stmt|;
case|case
literal|12
case|:
name|k2
operator|^=
call|(
name|long
call|)
argument_list|(
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|11
index|]
operator|&
literal|0xff
argument_list|)
operator|<<
literal|24
expr_stmt|;
case|case
literal|11
case|:
name|k2
operator|^=
call|(
name|long
call|)
argument_list|(
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|10
index|]
operator|&
literal|0xff
argument_list|)
operator|<<
literal|16
expr_stmt|;
case|case
literal|10
case|:
name|k2
operator|^=
call|(
name|long
call|)
argument_list|(
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|9
index|]
operator|&
literal|0xff
argument_list|)
operator|<<
literal|8
expr_stmt|;
case|case
literal|9
case|:
name|k2
operator|^=
call|(
name|long
call|)
argument_list|(
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|8
index|]
operator|&
literal|0xff
argument_list|)
expr_stmt|;
name|k2
operator|*=
name|C2
expr_stmt|;
name|k2
operator|=
name|Long
operator|.
name|rotateLeft
argument_list|(
name|k2
argument_list|,
name|R3
argument_list|)
expr_stmt|;
name|k2
operator|*=
name|C1
expr_stmt|;
name|h2
operator|^=
name|k2
expr_stmt|;
case|case
literal|8
case|:
name|k1
operator|^=
call|(
name|long
call|)
argument_list|(
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|7
index|]
operator|&
literal|0xff
argument_list|)
operator|<<
literal|56
expr_stmt|;
case|case
literal|7
case|:
name|k1
operator|^=
call|(
name|long
call|)
argument_list|(
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|6
index|]
operator|&
literal|0xff
argument_list|)
operator|<<
literal|48
expr_stmt|;
case|case
literal|6
case|:
name|k1
operator|^=
call|(
name|long
call|)
argument_list|(
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|5
index|]
operator|&
literal|0xff
argument_list|)
operator|<<
literal|40
expr_stmt|;
case|case
literal|5
case|:
name|k1
operator|^=
call|(
name|long
call|)
argument_list|(
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|4
index|]
operator|&
literal|0xff
argument_list|)
operator|<<
literal|32
expr_stmt|;
case|case
literal|4
case|:
name|k1
operator|^=
call|(
name|long
call|)
argument_list|(
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|3
index|]
operator|&
literal|0xff
argument_list|)
operator|<<
literal|24
expr_stmt|;
case|case
literal|3
case|:
name|k1
operator|^=
call|(
name|long
call|)
argument_list|(
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|2
index|]
operator|&
literal|0xff
argument_list|)
operator|<<
literal|16
expr_stmt|;
case|case
literal|2
case|:
name|k1
operator|^=
call|(
name|long
call|)
argument_list|(
name|data
index|[
name|offset
operator|+
name|tailStart
operator|+
literal|1
index|]
operator|&
literal|0xff
argument_list|)
operator|<<
literal|8
expr_stmt|;
case|case
literal|1
case|:
name|k1
operator|^=
call|(
name|long
call|)
argument_list|(
name|data
index|[
name|offset
operator|+
name|tailStart
index|]
operator|&
literal|0xff
argument_list|)
expr_stmt|;
name|k1
operator|*=
name|C1
expr_stmt|;
name|k1
operator|=
name|Long
operator|.
name|rotateLeft
argument_list|(
name|k1
argument_list|,
name|R1
argument_list|)
expr_stmt|;
name|k1
operator|*=
name|C2
expr_stmt|;
name|h1
operator|^=
name|k1
expr_stmt|;
block|}
comment|// finalization
name|h1
operator|^=
name|length
expr_stmt|;
name|h2
operator|^=
name|length
expr_stmt|;
name|h1
operator|+=
name|h2
expr_stmt|;
name|h2
operator|+=
name|h1
expr_stmt|;
name|h1
operator|=
name|fmix64
argument_list|(
name|h1
argument_list|)
expr_stmt|;
name|h2
operator|=
name|fmix64
argument_list|(
name|h2
argument_list|)
expr_stmt|;
name|h1
operator|+=
name|h2
expr_stmt|;
name|h2
operator|+=
name|h1
expr_stmt|;
return|return
operator|new
name|long
index|[]
block|{
name|h1
block|,
name|h2
block|}
return|;
block|}
specifier|private
specifier|static
name|long
name|fmix64
parameter_list|(
name|long
name|h
parameter_list|)
block|{
name|h
operator|^=
operator|(
name|h
operator|>>>
literal|33
operator|)
expr_stmt|;
name|h
operator|*=
literal|0xff51afd7ed558ccdL
expr_stmt|;
name|h
operator|^=
operator|(
name|h
operator|>>>
literal|33
operator|)
expr_stmt|;
name|h
operator|*=
literal|0xc4ceb9fe1a85ec53L
expr_stmt|;
name|h
operator|^=
operator|(
name|h
operator|>>>
literal|33
operator|)
expr_stmt|;
return|return
name|h
return|;
block|}
specifier|public
specifier|static
class|class
name|IncrementalHash32
block|{
name|byte
index|[]
name|tail
init|=
operator|new
name|byte
index|[
literal|3
index|]
decl_stmt|;
name|int
name|tailLen
decl_stmt|;
name|int
name|totalLen
decl_stmt|;
name|int
name|hash
decl_stmt|;
specifier|public
specifier|final
name|void
name|start
parameter_list|(
name|int
name|hash
parameter_list|)
block|{
name|tailLen
operator|=
name|totalLen
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|hash
operator|=
name|hash
expr_stmt|;
block|}
specifier|public
specifier|final
name|void
name|add
parameter_list|(
name|byte
index|[]
name|data
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
return|return;
name|totalLen
operator|+=
name|length
expr_stmt|;
if|if
condition|(
name|tailLen
operator|+
name|length
operator|<
literal|4
condition|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|tail
argument_list|,
name|tailLen
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|tailLen
operator|+=
name|length
expr_stmt|;
return|return;
block|}
name|int
name|offset2
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|tailLen
operator|>
literal|0
condition|)
block|{
name|offset2
operator|=
operator|(
literal|4
operator|-
name|tailLen
operator|)
expr_stmt|;
name|int
name|k
init|=
operator|-
literal|1
decl_stmt|;
switch|switch
condition|(
name|tailLen
condition|)
block|{
case|case
literal|1
case|:
name|k
operator|=
name|orBytes
argument_list|(
name|tail
index|[
literal|0
index|]
argument_list|,
name|data
index|[
name|offset
index|]
argument_list|,
name|data
index|[
name|offset
operator|+
literal|1
index|]
argument_list|,
name|data
index|[
name|offset
operator|+
literal|2
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|k
operator|=
name|orBytes
argument_list|(
name|tail
index|[
literal|0
index|]
argument_list|,
name|tail
index|[
literal|1
index|]
argument_list|,
name|data
index|[
name|offset
index|]
argument_list|,
name|data
index|[
name|offset
operator|+
literal|1
index|]
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|k
operator|=
name|orBytes
argument_list|(
name|tail
index|[
literal|0
index|]
argument_list|,
name|tail
index|[
literal|1
index|]
argument_list|,
name|tail
index|[
literal|2
index|]
argument_list|,
name|data
index|[
name|offset
index|]
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|(
name|tailLen
argument_list|)
throw|;
block|}
comment|// mix functions
name|k
operator|*=
name|C1_32
expr_stmt|;
name|k
operator|=
name|Integer
operator|.
name|rotateLeft
argument_list|(
name|k
argument_list|,
name|R1_32
argument_list|)
expr_stmt|;
name|k
operator|*=
name|C2_32
expr_stmt|;
name|hash
operator|^=
name|k
expr_stmt|;
name|hash
operator|=
name|Integer
operator|.
name|rotateLeft
argument_list|(
name|hash
argument_list|,
name|R2_32
argument_list|)
operator|*
name|M_32
operator|+
name|N_32
expr_stmt|;
block|}
name|int
name|length2
init|=
name|length
operator|-
name|offset2
decl_stmt|;
name|offset
operator|+=
name|offset2
expr_stmt|;
specifier|final
name|int
name|nblocks
init|=
name|length2
operator|>>
literal|2
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
name|nblocks
condition|;
name|i
operator|++
control|)
block|{
name|int
name|i_4
init|=
operator|(
name|i
operator|<<
literal|2
operator|)
operator|+
name|offset
decl_stmt|;
name|int
name|k
init|=
name|orBytes
argument_list|(
name|data
index|[
name|i_4
index|]
argument_list|,
name|data
index|[
name|i_4
operator|+
literal|1
index|]
argument_list|,
name|data
index|[
name|i_4
operator|+
literal|2
index|]
argument_list|,
name|data
index|[
name|i_4
operator|+
literal|3
index|]
argument_list|)
decl_stmt|;
comment|// mix functions
name|k
operator|*=
name|C1_32
expr_stmt|;
name|k
operator|=
name|Integer
operator|.
name|rotateLeft
argument_list|(
name|k
argument_list|,
name|R1_32
argument_list|)
expr_stmt|;
name|k
operator|*=
name|C2_32
expr_stmt|;
name|hash
operator|^=
name|k
expr_stmt|;
name|hash
operator|=
name|Integer
operator|.
name|rotateLeft
argument_list|(
name|hash
argument_list|,
name|R2_32
argument_list|)
operator|*
name|M_32
operator|+
name|N_32
expr_stmt|;
block|}
name|int
name|consumed
init|=
operator|(
name|nblocks
operator|<<
literal|2
operator|)
decl_stmt|;
name|tailLen
operator|=
name|length2
operator|-
name|consumed
expr_stmt|;
if|if
condition|(
name|consumed
operator|==
name|length2
condition|)
return|return;
name|System
operator|.
name|arraycopy
argument_list|(
name|data
argument_list|,
name|offset
operator|+
name|consumed
argument_list|,
name|tail
argument_list|,
literal|0
argument_list|,
name|tailLen
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|final
name|int
name|end
parameter_list|()
block|{
name|int
name|k1
init|=
literal|0
decl_stmt|;
switch|switch
condition|(
name|tailLen
condition|)
block|{
case|case
literal|3
case|:
name|k1
operator|^=
name|tail
index|[
literal|2
index|]
operator|<<
literal|16
expr_stmt|;
case|case
literal|2
case|:
name|k1
operator|^=
name|tail
index|[
literal|1
index|]
operator|<<
literal|8
expr_stmt|;
case|case
literal|1
case|:
name|k1
operator|^=
name|tail
index|[
literal|0
index|]
expr_stmt|;
comment|// mix functions
name|k1
operator|*=
name|C1_32
expr_stmt|;
name|k1
operator|=
name|Integer
operator|.
name|rotateLeft
argument_list|(
name|k1
argument_list|,
name|R1_32
argument_list|)
expr_stmt|;
name|k1
operator|*=
name|C2_32
expr_stmt|;
name|hash
operator|^=
name|k1
expr_stmt|;
block|}
comment|// finalization
name|hash
operator|^=
name|totalLen
expr_stmt|;
name|hash
operator|^=
operator|(
name|hash
operator|>>>
literal|16
operator|)
expr_stmt|;
name|hash
operator|*=
literal|0x85ebca6b
expr_stmt|;
name|hash
operator|^=
operator|(
name|hash
operator|>>>
literal|13
operator|)
expr_stmt|;
name|hash
operator|*=
literal|0xc2b2ae35
expr_stmt|;
name|hash
operator|^=
operator|(
name|hash
operator|>>>
literal|16
operator|)
expr_stmt|;
return|return
name|hash
return|;
block|}
block|}
specifier|private
specifier|static
name|int
name|orBytes
parameter_list|(
name|byte
name|b1
parameter_list|,
name|byte
name|b2
parameter_list|,
name|byte
name|b3
parameter_list|,
name|byte
name|b4
parameter_list|)
block|{
return|return
operator|(
name|b1
operator|&
literal|0xff
operator|)
operator||
operator|(
operator|(
name|b2
operator|&
literal|0xff
operator|)
operator|<<
literal|8
operator|)
operator||
operator|(
operator|(
name|b3
operator|&
literal|0xff
operator|)
operator|<<
literal|16
operator|)
operator||
operator|(
operator|(
name|b4
operator|&
literal|0xff
operator|)
operator|<<
literal|24
operator|)
return|;
block|}
block|}
end_class

end_unit

