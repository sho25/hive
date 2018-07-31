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
name|hive
operator|.
name|common
operator|.
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
import|;
end_import

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
name|InputStream
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
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_comment
comment|/**  * BloomKFilter is variation of {@link BloomFilter}. Unlike BloomFilter, BloomKFilter will spread  * 'k' hash bits within same cache line for better L1 cache performance. The way it works is,  * First hash code is computed from key which is used to locate the block offset (n-longs in bitset constitute a block)  * Subsequent 'k' hash codes are used to spread hash bits within the block. By default block size is chosen as 8,  * which is to match cache line size (8 longs = 64 bytes = cache line size).  * Refer {@link BloomKFilter#addBytes(byte[])} for more info.  *  * This implementation has much lesser L1 data cache misses than {@link BloomFilter}.  */
end_comment

begin_class
specifier|public
class|class
name|BloomKFilter
block|{
specifier|public
specifier|static
specifier|final
name|float
name|DEFAULT_FPP
init|=
literal|0.05f
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_BLOCK_SIZE
init|=
literal|8
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_BLOCK_SIZE_BITS
init|=
call|(
name|int
call|)
argument_list|(
name|Math
operator|.
name|log
argument_list|(
name|DEFAULT_BLOCK_SIZE
argument_list|)
operator|/
name|Math
operator|.
name|log
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_BLOCK_OFFSET_MASK
init|=
name|DEFAULT_BLOCK_SIZE
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_BIT_OFFSET_MASK
init|=
name|Long
operator|.
name|SIZE
operator|-
literal|1
decl_stmt|;
specifier|private
specifier|final
name|long
index|[]
name|masks
init|=
operator|new
name|long
index|[
name|DEFAULT_BLOCK_SIZE
index|]
decl_stmt|;
specifier|private
specifier|final
name|BitSet
name|bitSet
decl_stmt|;
specifier|private
specifier|final
name|int
name|m
decl_stmt|;
specifier|private
specifier|final
name|int
name|k
decl_stmt|;
comment|// spread k-1 bits to adjacent longs, default is 8
comment|// spreading hash bits within blockSize * longs will make bloom filter L1 cache friendly
comment|// default block size is set to 8 as most cache line sizes are 64 bytes and also AVX512 friendly
specifier|private
specifier|final
name|int
name|totalBlockCount
decl_stmt|;
specifier|static
name|void
name|checkArgument
parameter_list|(
name|boolean
name|expression
parameter_list|,
name|String
name|message
parameter_list|)
block|{
if|if
condition|(
operator|!
name|expression
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|message
argument_list|)
throw|;
block|}
block|}
specifier|public
name|BloomKFilter
parameter_list|(
name|long
name|maxNumEntries
parameter_list|)
block|{
name|checkArgument
argument_list|(
name|maxNumEntries
operator|>
literal|0
argument_list|,
literal|"expectedEntries should be> 0"
argument_list|)
expr_stmt|;
name|long
name|numBits
init|=
name|optimalNumOfBits
argument_list|(
name|maxNumEntries
argument_list|,
name|DEFAULT_FPP
argument_list|)
decl_stmt|;
name|this
operator|.
name|k
operator|=
name|optimalNumOfHashFunctions
argument_list|(
name|maxNumEntries
argument_list|,
name|numBits
argument_list|)
expr_stmt|;
name|int
name|nLongs
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
operator|(
name|double
operator|)
name|numBits
operator|/
operator|(
name|double
operator|)
name|Long
operator|.
name|SIZE
argument_list|)
decl_stmt|;
comment|// additional bits to pad long array to block size
name|int
name|padLongs
init|=
name|DEFAULT_BLOCK_SIZE
operator|-
name|nLongs
operator|%
name|DEFAULT_BLOCK_SIZE
decl_stmt|;
name|this
operator|.
name|m
operator|=
operator|(
name|nLongs
operator|+
name|padLongs
operator|)
operator|*
name|Long
operator|.
name|SIZE
expr_stmt|;
name|this
operator|.
name|bitSet
operator|=
operator|new
name|BitSet
argument_list|(
name|m
argument_list|)
expr_stmt|;
name|checkArgument
argument_list|(
operator|(
name|bitSet
operator|.
name|data
operator|.
name|length
operator|%
name|DEFAULT_BLOCK_SIZE
operator|)
operator|==
literal|0
argument_list|,
literal|"bitSet has to be block aligned"
argument_list|)
expr_stmt|;
name|this
operator|.
name|totalBlockCount
operator|=
name|bitSet
operator|.
name|data
operator|.
name|length
operator|/
name|DEFAULT_BLOCK_SIZE
expr_stmt|;
block|}
comment|/**    * A constructor to support rebuilding the BloomFilter from a serialized representation.    * @param bits    * @param numFuncs    */
specifier|public
name|BloomKFilter
parameter_list|(
name|long
index|[]
name|bits
parameter_list|,
name|int
name|numFuncs
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|bitSet
operator|=
operator|new
name|BitSet
argument_list|(
name|bits
argument_list|)
expr_stmt|;
name|this
operator|.
name|m
operator|=
name|bits
operator|.
name|length
operator|*
name|Long
operator|.
name|SIZE
expr_stmt|;
name|this
operator|.
name|k
operator|=
name|numFuncs
expr_stmt|;
name|checkArgument
argument_list|(
operator|(
name|bitSet
operator|.
name|data
operator|.
name|length
operator|%
name|DEFAULT_BLOCK_SIZE
operator|)
operator|==
literal|0
argument_list|,
literal|"bitSet has to be block aligned"
argument_list|)
expr_stmt|;
name|this
operator|.
name|totalBlockCount
operator|=
name|bitSet
operator|.
name|data
operator|.
name|length
operator|/
name|DEFAULT_BLOCK_SIZE
expr_stmt|;
block|}
specifier|static
name|int
name|optimalNumOfHashFunctions
parameter_list|(
name|long
name|n
parameter_list|,
name|long
name|m
parameter_list|)
block|{
return|return
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
operator|(
name|int
operator|)
name|Math
operator|.
name|round
argument_list|(
operator|(
name|double
operator|)
name|m
operator|/
name|n
operator|*
name|Math
operator|.
name|log
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
specifier|static
name|long
name|optimalNumOfBits
parameter_list|(
name|long
name|n
parameter_list|,
name|double
name|p
parameter_list|)
block|{
return|return
call|(
name|long
call|)
argument_list|(
operator|-
name|n
operator|*
name|Math
operator|.
name|log
argument_list|(
name|p
argument_list|)
operator|/
operator|(
name|Math
operator|.
name|log
argument_list|(
literal|2
argument_list|)
operator|*
name|Math
operator|.
name|log
argument_list|(
literal|2
argument_list|)
operator|)
argument_list|)
return|;
block|}
specifier|public
name|void
name|add
parameter_list|(
name|byte
index|[]
name|val
parameter_list|)
block|{
name|addBytes
argument_list|(
name|val
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addBytes
parameter_list|(
name|byte
index|[]
name|val
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
comment|// We use the trick mentioned in "Less Hashing, Same Performance: Building a Better Bloom Filter"
comment|// by Kirsch et.al. From abstract 'only two hash functions are necessary to effectively
comment|// implement a Bloom filter without any loss in the asymptotic false positive probability'
comment|// Lets split up 64-bit hashcode into two 32-bit hash codes and employ the technique mentioned
comment|// in the above paper
name|long
name|hash64
init|=
name|val
operator|==
literal|null
condition|?
name|Murmur3
operator|.
name|NULL_HASHCODE
else|:
name|Murmur3
operator|.
name|hash64
argument_list|(
name|val
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
decl_stmt|;
name|addHash
argument_list|(
name|hash64
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addBytes
parameter_list|(
name|byte
index|[]
name|val
parameter_list|)
block|{
name|addBytes
argument_list|(
name|val
argument_list|,
literal|0
argument_list|,
name|val
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addHash
parameter_list|(
name|long
name|hash64
parameter_list|)
block|{
specifier|final
name|int
name|hash1
init|=
operator|(
name|int
operator|)
name|hash64
decl_stmt|;
specifier|final
name|int
name|hash2
init|=
call|(
name|int
call|)
argument_list|(
name|hash64
operator|>>>
literal|32
argument_list|)
decl_stmt|;
name|int
name|firstHash
init|=
name|hash1
operator|+
name|hash2
decl_stmt|;
comment|// hashcode should be positive, flip all the bits if it's negative
if|if
condition|(
name|firstHash
operator|<
literal|0
condition|)
block|{
name|firstHash
operator|=
operator|~
name|firstHash
expr_stmt|;
block|}
comment|// first hash is used to locate start of the block (blockBaseOffset)
comment|// subsequent K hashes are used to generate K bits within a block of words
specifier|final
name|int
name|blockIdx
init|=
name|firstHash
operator|%
name|totalBlockCount
decl_stmt|;
specifier|final
name|int
name|blockBaseOffset
init|=
name|blockIdx
operator|<<
name|DEFAULT_BLOCK_SIZE_BITS
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|k
condition|;
name|i
operator|++
control|)
block|{
name|int
name|combinedHash
init|=
name|hash1
operator|+
operator|(
operator|(
name|i
operator|+
literal|1
operator|)
operator|*
name|hash2
operator|)
decl_stmt|;
comment|// hashcode should be positive, flip all the bits if it's negative
if|if
condition|(
name|combinedHash
operator|<
literal|0
condition|)
block|{
name|combinedHash
operator|=
operator|~
name|combinedHash
expr_stmt|;
block|}
comment|// LSB 3 bits is used to locate offset within the block
specifier|final
name|int
name|absOffset
init|=
name|blockBaseOffset
operator|+
operator|(
name|combinedHash
operator|&
name|DEFAULT_BLOCK_OFFSET_MASK
operator|)
decl_stmt|;
comment|// Next 6 bits are used to locate offset within a long/word
specifier|final
name|int
name|bitPos
init|=
operator|(
name|combinedHash
operator|>>>
name|DEFAULT_BLOCK_SIZE_BITS
operator|)
operator|&
name|DEFAULT_BIT_OFFSET_MASK
decl_stmt|;
name|bitSet
operator|.
name|data
index|[
name|absOffset
index|]
operator||=
operator|(
literal|1L
operator|<<
name|bitPos
operator|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|addString
parameter_list|(
name|String
name|val
parameter_list|)
block|{
name|addBytes
argument_list|(
name|val
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addByte
parameter_list|(
name|byte
name|val
parameter_list|)
block|{
name|addBytes
argument_list|(
operator|new
name|byte
index|[]
block|{
name|val
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addInt
parameter_list|(
name|int
name|val
parameter_list|)
block|{
name|addHash
argument_list|(
name|Murmur3
operator|.
name|hash64
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addLong
parameter_list|(
name|long
name|val
parameter_list|)
block|{
comment|// puts long in little endian order
name|addHash
argument_list|(
name|Murmur3
operator|.
name|hash64
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addFloat
parameter_list|(
name|float
name|val
parameter_list|)
block|{
name|addInt
argument_list|(
name|Float
operator|.
name|floatToIntBits
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addDouble
parameter_list|(
name|double
name|val
parameter_list|)
block|{
name|addLong
argument_list|(
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|test
parameter_list|(
name|byte
index|[]
name|val
parameter_list|)
block|{
return|return
name|testBytes
argument_list|(
name|val
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|testBytes
parameter_list|(
name|byte
index|[]
name|val
parameter_list|)
block|{
return|return
name|testBytes
argument_list|(
name|val
argument_list|,
literal|0
argument_list|,
name|val
operator|.
name|length
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|testBytes
parameter_list|(
name|byte
index|[]
name|val
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|long
name|hash64
init|=
name|val
operator|==
literal|null
condition|?
name|Murmur3
operator|.
name|NULL_HASHCODE
else|:
name|Murmur3
operator|.
name|hash64
argument_list|(
name|val
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
decl_stmt|;
return|return
name|testHash
argument_list|(
name|hash64
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|testHash
parameter_list|(
name|long
name|hash64
parameter_list|)
block|{
specifier|final
name|int
name|hash1
init|=
operator|(
name|int
operator|)
name|hash64
decl_stmt|;
specifier|final
name|int
name|hash2
init|=
call|(
name|int
call|)
argument_list|(
name|hash64
operator|>>>
literal|32
argument_list|)
decl_stmt|;
specifier|final
name|long
index|[]
name|bits
init|=
name|bitSet
operator|.
name|data
decl_stmt|;
name|int
name|firstHash
init|=
name|hash1
operator|+
name|hash2
decl_stmt|;
comment|// hashcode should be positive, flip all the bits if it's negative
if|if
condition|(
name|firstHash
operator|<
literal|0
condition|)
block|{
name|firstHash
operator|=
operator|~
name|firstHash
expr_stmt|;
block|}
comment|// first hash is used to locate start of the block (blockBaseOffset)
comment|// subsequent K hashes are used to generate K bits within a block of words
comment|// To avoid branches during probe, a separate masks array is used for each longs/words within a block.
comment|// data array and masks array are then traversed together and checked for corresponding set bits.
specifier|final
name|int
name|blockIdx
init|=
name|firstHash
operator|%
name|totalBlockCount
decl_stmt|;
specifier|final
name|int
name|blockBaseOffset
init|=
name|blockIdx
operator|<<
name|DEFAULT_BLOCK_SIZE_BITS
decl_stmt|;
comment|// iterate and update masks array
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|k
condition|;
name|i
operator|++
control|)
block|{
name|int
name|combinedHash
init|=
name|hash1
operator|+
operator|(
operator|(
name|i
operator|+
literal|1
operator|)
operator|*
name|hash2
operator|)
decl_stmt|;
comment|// hashcode should be positive, flip all the bits if it's negative
if|if
condition|(
name|combinedHash
operator|<
literal|0
condition|)
block|{
name|combinedHash
operator|=
operator|~
name|combinedHash
expr_stmt|;
block|}
comment|// LSB 3 bits is used to locate offset within the block
specifier|final
name|int
name|wordOffset
init|=
name|combinedHash
operator|&
name|DEFAULT_BLOCK_OFFSET_MASK
decl_stmt|;
comment|// Next 6 bits are used to locate offset within a long/word
specifier|final
name|int
name|bitPos
init|=
operator|(
name|combinedHash
operator|>>>
name|DEFAULT_BLOCK_SIZE_BITS
operator|)
operator|&
name|DEFAULT_BIT_OFFSET_MASK
decl_stmt|;
name|masks
index|[
name|wordOffset
index|]
operator||=
operator|(
literal|1L
operator|<<
name|bitPos
operator|)
expr_stmt|;
block|}
comment|// traverse data and masks array together, check for set bits
name|long
name|expected
init|=
literal|0
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
name|DEFAULT_BLOCK_SIZE
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|long
name|mask
init|=
name|masks
index|[
name|i
index|]
decl_stmt|;
name|expected
operator||=
operator|(
name|bits
index|[
name|blockBaseOffset
operator|+
name|i
index|]
operator|&
name|mask
operator|)
operator|^
name|mask
expr_stmt|;
block|}
comment|// clear the mask for array reuse (this is to avoid masks array allocation in inner loop)
name|Arrays
operator|.
name|fill
argument_list|(
name|masks
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// if all bits are set, expected should be 0
return|return
name|expected
operator|==
literal|0
return|;
block|}
specifier|public
name|boolean
name|testString
parameter_list|(
name|String
name|val
parameter_list|)
block|{
return|return
name|testBytes
argument_list|(
name|val
operator|.
name|getBytes
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|testByte
parameter_list|(
name|byte
name|val
parameter_list|)
block|{
return|return
name|testBytes
argument_list|(
operator|new
name|byte
index|[]
block|{
name|val
block|}
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|testInt
parameter_list|(
name|int
name|val
parameter_list|)
block|{
return|return
name|testHash
argument_list|(
name|Murmur3
operator|.
name|hash64
argument_list|(
name|val
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|testLong
parameter_list|(
name|long
name|val
parameter_list|)
block|{
return|return
name|testHash
argument_list|(
name|Murmur3
operator|.
name|hash64
argument_list|(
name|val
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|testFloat
parameter_list|(
name|float
name|val
parameter_list|)
block|{
return|return
name|testInt
argument_list|(
name|Float
operator|.
name|floatToIntBits
argument_list|(
name|val
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|testDouble
parameter_list|(
name|double
name|val
parameter_list|)
block|{
return|return
name|testLong
argument_list|(
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|val
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|long
name|sizeInBytes
parameter_list|()
block|{
return|return
name|getBitSize
argument_list|()
operator|/
literal|8
return|;
block|}
specifier|public
name|int
name|getBitSize
parameter_list|()
block|{
return|return
name|bitSet
operator|.
name|getData
argument_list|()
operator|.
name|length
operator|*
name|Long
operator|.
name|SIZE
return|;
block|}
specifier|public
name|int
name|getNumHashFunctions
parameter_list|()
block|{
return|return
name|k
return|;
block|}
specifier|public
name|int
name|getNumBits
parameter_list|()
block|{
return|return
name|m
return|;
block|}
specifier|public
name|long
index|[]
name|getBitSet
parameter_list|()
block|{
return|return
name|bitSet
operator|.
name|getData
argument_list|()
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
literal|"m: "
operator|+
name|m
operator|+
literal|" k: "
operator|+
name|k
return|;
block|}
comment|/**    * Merge the specified bloom filter with current bloom filter.    *    * @param that - bloom filter to merge    */
specifier|public
name|void
name|merge
parameter_list|(
name|BloomKFilter
name|that
parameter_list|)
block|{
if|if
condition|(
name|this
operator|!=
name|that
operator|&&
name|this
operator|.
name|m
operator|==
name|that
operator|.
name|m
operator|&&
name|this
operator|.
name|k
operator|==
name|that
operator|.
name|k
condition|)
block|{
name|this
operator|.
name|bitSet
operator|.
name|putAll
argument_list|(
name|that
operator|.
name|bitSet
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"BloomKFilters are not compatible for merging."
operator|+
literal|" this - "
operator|+
name|this
operator|.
name|toString
argument_list|()
operator|+
literal|" that - "
operator|+
name|that
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|this
operator|.
name|bitSet
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|/**    * Serialize a bloom filter    *    * @param out         output stream to write to    * @param bloomFilter BloomKFilter that needs to be seralized    */
specifier|public
specifier|static
name|void
name|serialize
parameter_list|(
name|OutputStream
name|out
parameter_list|,
name|BloomKFilter
name|bloomFilter
parameter_list|)
throws|throws
name|IOException
block|{
comment|/**      * Serialized BloomKFilter format:      * 1 byte for the number of hash functions.      * 1 big endian int(That is how OutputStream works) for the number of longs in the bitset      * big endina longs in the BloomKFilter bitset      */
name|DataOutputStream
name|dataOutputStream
init|=
operator|new
name|DataOutputStream
argument_list|(
name|out
argument_list|)
decl_stmt|;
name|dataOutputStream
operator|.
name|writeByte
argument_list|(
name|bloomFilter
operator|.
name|k
argument_list|)
expr_stmt|;
name|dataOutputStream
operator|.
name|writeInt
argument_list|(
name|bloomFilter
operator|.
name|getBitSet
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|long
name|value
range|:
name|bloomFilter
operator|.
name|getBitSet
argument_list|()
control|)
block|{
name|dataOutputStream
operator|.
name|writeLong
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Deserialize a bloom filter    * Read a byte stream, which was written by {@linkplain #serialize(OutputStream, BloomKFilter)}    * into a {@code BloomKFilter}    *    * @param in input bytestream    * @return deserialized BloomKFilter    */
specifier|public
specifier|static
name|BloomKFilter
name|deserialize
parameter_list|(
name|InputStream
name|in
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|in
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Input stream is null"
argument_list|)
throw|;
block|}
try|try
block|{
name|DataInputStream
name|dataInputStream
init|=
operator|new
name|DataInputStream
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|int
name|numHashFunc
init|=
name|dataInputStream
operator|.
name|readByte
argument_list|()
decl_stmt|;
name|int
name|bitsetArrayLen
init|=
name|dataInputStream
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|long
index|[]
name|data
init|=
operator|new
name|long
index|[
name|bitsetArrayLen
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
name|bitsetArrayLen
condition|;
name|i
operator|++
control|)
block|{
name|data
index|[
name|i
index|]
operator|=
name|dataInputStream
operator|.
name|readLong
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|BloomKFilter
argument_list|(
name|data
argument_list|,
name|numHashFunc
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|IOException
name|io
init|=
operator|new
name|IOException
argument_list|(
literal|"Unable to deserialize BloomKFilter"
argument_list|)
decl_stmt|;
name|io
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|io
throw|;
block|}
block|}
comment|// Given a byte array consisting of a serialized BloomKFilter, gives the offset (from 0)
comment|// for the start of the serialized long values that make up the bitset.
comment|// NumHashFunctions (1 byte) + bitset array length (4 bytes)
specifier|public
specifier|static
specifier|final
name|int
name|START_OF_SERIALIZED_LONGS
init|=
literal|5
decl_stmt|;
comment|/**    * Merges BloomKFilter bf2 into bf1.    * Assumes 2 BloomKFilters with the same size/hash functions are serialized to byte arrays    *    * @param bf1Bytes    * @param bf1Start    * @param bf1Length    * @param bf2Bytes    * @param bf2Start    * @param bf2Length    */
specifier|public
specifier|static
name|void
name|mergeBloomFilterBytes
parameter_list|(
name|byte
index|[]
name|bf1Bytes
parameter_list|,
name|int
name|bf1Start
parameter_list|,
name|int
name|bf1Length
parameter_list|,
name|byte
index|[]
name|bf2Bytes
parameter_list|,
name|int
name|bf2Start
parameter_list|,
name|int
name|bf2Length
parameter_list|)
block|{
if|if
condition|(
name|bf1Length
operator|!=
name|bf2Length
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"bf1Length "
operator|+
name|bf1Length
operator|+
literal|" does not match bf2Length "
operator|+
name|bf2Length
argument_list|)
throw|;
block|}
comment|// Validation on the bitset size/3 hash functions.
for|for
control|(
name|int
name|idx
init|=
literal|0
init|;
name|idx
operator|<
name|START_OF_SERIALIZED_LONGS
condition|;
operator|++
name|idx
control|)
block|{
if|if
condition|(
name|bf1Bytes
index|[
name|bf1Start
operator|+
name|idx
index|]
operator|!=
name|bf2Bytes
index|[
name|bf2Start
operator|+
name|idx
index|]
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"bf1 NumHashFunctions/NumBits does not match bf2"
argument_list|)
throw|;
block|}
block|}
comment|// Just bitwise-OR the bits together - size/# functions should be the same,
comment|// rest of the data is serialized long values for the bitset which are supposed to be bitwise-ORed.
for|for
control|(
name|int
name|idx
init|=
name|START_OF_SERIALIZED_LONGS
init|;
name|idx
operator|<
name|bf1Length
condition|;
operator|++
name|idx
control|)
block|{
name|bf1Bytes
index|[
name|bf1Start
operator|+
name|idx
index|]
operator||=
name|bf2Bytes
index|[
name|bf2Start
operator|+
name|idx
index|]
expr_stmt|;
block|}
block|}
comment|/**    * Bare metal bit set implementation. For performance reasons, this implementation does not check    * for index bounds nor expand the bit set size if the specified index is greater than the size.    */
specifier|public
specifier|static
class|class
name|BitSet
block|{
specifier|private
specifier|final
name|long
index|[]
name|data
decl_stmt|;
specifier|public
name|BitSet
parameter_list|(
name|long
name|bits
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|long
index|[
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
operator|(
name|double
operator|)
name|bits
operator|/
operator|(
name|double
operator|)
name|Long
operator|.
name|SIZE
argument_list|)
index|]
argument_list|)
expr_stmt|;
block|}
comment|/**      * Deserialize long array as bit set.      *      * @param data - bit array      */
specifier|public
name|BitSet
parameter_list|(
name|long
index|[]
name|data
parameter_list|)
block|{
assert|assert
name|data
operator|.
name|length
operator|>
literal|0
operator|:
literal|"data length is zero!"
assert|;
name|this
operator|.
name|data
operator|=
name|data
expr_stmt|;
block|}
comment|/**      * Sets the bit at specified index.      *      * @param index - position      */
specifier|public
name|void
name|set
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|data
index|[
name|index
operator|>>>
literal|6
index|]
operator||=
operator|(
literal|1L
operator|<<
name|index
operator|)
expr_stmt|;
block|}
comment|/**      * Returns true if the bit is set in the specified index.      *      * @param index - position      * @return - value at the bit position      */
specifier|public
name|boolean
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
operator|(
name|data
index|[
name|index
operator|>>>
literal|6
index|]
operator|&
operator|(
literal|1L
operator|<<
name|index
operator|)
operator|)
operator|!=
literal|0
return|;
block|}
comment|/**      * Number of bits      */
specifier|public
name|int
name|bitSize
parameter_list|()
block|{
return|return
name|data
operator|.
name|length
operator|*
name|Long
operator|.
name|SIZE
return|;
block|}
specifier|public
name|long
index|[]
name|getData
parameter_list|()
block|{
return|return
name|data
return|;
block|}
comment|/**      * Combines the two BitArrays using bitwise OR.      */
specifier|public
name|void
name|putAll
parameter_list|(
name|BloomKFilter
operator|.
name|BitSet
name|array
parameter_list|)
block|{
assert|assert
name|data
operator|.
name|length
operator|==
name|array
operator|.
name|data
operator|.
name|length
operator|:
literal|"BitArrays must be of equal length ("
operator|+
name|data
operator|.
name|length
operator|+
literal|"!= "
operator|+
name|array
operator|.
name|data
operator|.
name|length
operator|+
literal|")"
assert|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|data
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|data
index|[
name|i
index|]
operator||=
name|array
operator|.
name|data
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
comment|/**      * Clear the bit set.      */
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|data
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

