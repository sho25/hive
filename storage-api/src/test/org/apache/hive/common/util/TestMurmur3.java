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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
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
name|Murmur3
operator|.
name|IncrementalHash32
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|hash
operator|.
name|HashFunction
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|hash
operator|.
name|Hashing
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteOrder
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_comment
comment|/**  * Tests for Murmur3 variants.  */
end_comment

begin_class
specifier|public
class|class
name|TestMurmur3
block|{
annotation|@
name|Test
specifier|public
name|void
name|testHashCodesM3_32_string
parameter_list|()
block|{
name|String
name|key
init|=
literal|"test"
decl_stmt|;
name|int
name|seed
init|=
literal|123
decl_stmt|;
name|HashFunction
name|hf
init|=
name|Hashing
operator|.
name|murmur3_32
argument_list|(
name|seed
argument_list|)
decl_stmt|;
name|int
name|hc1
init|=
name|hf
operator|.
name|hashBytes
argument_list|(
name|key
operator|.
name|getBytes
argument_list|()
argument_list|)
operator|.
name|asInt
argument_list|()
decl_stmt|;
name|int
name|hc2
init|=
name|Murmur3
operator|.
name|hash32
argument_list|(
name|key
operator|.
name|getBytes
argument_list|()
argument_list|,
name|key
operator|.
name|getBytes
argument_list|()
operator|.
name|length
argument_list|,
name|seed
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hc1
argument_list|,
name|hc2
argument_list|)
expr_stmt|;
name|key
operator|=
literal|"testkey"
expr_stmt|;
name|hc1
operator|=
name|hf
operator|.
name|hashBytes
argument_list|(
name|key
operator|.
name|getBytes
argument_list|()
argument_list|)
operator|.
name|asInt
argument_list|()
expr_stmt|;
name|hc2
operator|=
name|Murmur3
operator|.
name|hash32
argument_list|(
name|key
operator|.
name|getBytes
argument_list|()
argument_list|,
name|key
operator|.
name|getBytes
argument_list|()
operator|.
name|length
argument_list|,
name|seed
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|hc1
argument_list|,
name|hc2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHashCodesM3_32_ints
parameter_list|()
block|{
name|int
name|seed
init|=
literal|123
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
decl_stmt|;
name|HashFunction
name|hf
init|=
name|Hashing
operator|.
name|murmur3_32
argument_list|(
name|seed
argument_list|)
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
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|int
name|val
init|=
name|rand
operator|.
name|nextInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|data
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|4
argument_list|)
operator|.
name|putInt
argument_list|(
name|val
argument_list|)
operator|.
name|array
argument_list|()
decl_stmt|;
name|int
name|hc1
init|=
name|hf
operator|.
name|hashBytes
argument_list|(
name|data
argument_list|)
operator|.
name|asInt
argument_list|()
decl_stmt|;
name|int
name|hc2
init|=
name|Murmur3
operator|.
name|hash32
argument_list|(
name|data
argument_list|,
name|data
operator|.
name|length
argument_list|,
name|seed
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hc1
argument_list|,
name|hc2
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHashCodesM3_32_longs
parameter_list|()
block|{
name|int
name|seed
init|=
literal|123
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
decl_stmt|;
name|HashFunction
name|hf
init|=
name|Hashing
operator|.
name|murmur3_32
argument_list|(
name|seed
argument_list|)
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
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|long
name|val
init|=
name|rand
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|byte
index|[]
name|data
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|8
argument_list|)
operator|.
name|putLong
argument_list|(
name|val
argument_list|)
operator|.
name|array
argument_list|()
decl_stmt|;
name|int
name|hc1
init|=
name|hf
operator|.
name|hashBytes
argument_list|(
name|data
argument_list|)
operator|.
name|asInt
argument_list|()
decl_stmt|;
name|int
name|hc2
init|=
name|Murmur3
operator|.
name|hash32
argument_list|(
name|data
argument_list|,
name|data
operator|.
name|length
argument_list|,
name|seed
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hc1
argument_list|,
name|hc2
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHashCodesM3_32_double
parameter_list|()
block|{
name|int
name|seed
init|=
literal|123
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
decl_stmt|;
name|HashFunction
name|hf
init|=
name|Hashing
operator|.
name|murmur3_32
argument_list|(
name|seed
argument_list|)
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
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|double
name|val
init|=
name|rand
operator|.
name|nextDouble
argument_list|()
decl_stmt|;
name|byte
index|[]
name|data
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|8
argument_list|)
operator|.
name|putDouble
argument_list|(
name|val
argument_list|)
operator|.
name|array
argument_list|()
decl_stmt|;
name|int
name|hc1
init|=
name|hf
operator|.
name|hashBytes
argument_list|(
name|data
argument_list|)
operator|.
name|asInt
argument_list|()
decl_stmt|;
name|int
name|hc2
init|=
name|Murmur3
operator|.
name|hash32
argument_list|(
name|data
argument_list|,
name|data
operator|.
name|length
argument_list|,
name|seed
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hc1
argument_list|,
name|hc2
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHashCodesM3_128_string
parameter_list|()
block|{
name|String
name|key
init|=
literal|"test"
decl_stmt|;
name|int
name|seed
init|=
literal|123
decl_stmt|;
name|HashFunction
name|hf
init|=
name|Hashing
operator|.
name|murmur3_128
argument_list|(
name|seed
argument_list|)
decl_stmt|;
comment|// guava stores the hashcodes in little endian order
name|ByteBuffer
name|buf
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|16
argument_list|)
operator|.
name|order
argument_list|(
name|ByteOrder
operator|.
name|LITTLE_ENDIAN
argument_list|)
decl_stmt|;
name|buf
operator|.
name|put
argument_list|(
name|hf
operator|.
name|hashBytes
argument_list|(
name|key
operator|.
name|getBytes
argument_list|()
argument_list|)
operator|.
name|asBytes
argument_list|()
argument_list|)
expr_stmt|;
name|buf
operator|.
name|flip
argument_list|()
expr_stmt|;
name|long
name|gl1
init|=
name|buf
operator|.
name|getLong
argument_list|()
decl_stmt|;
name|long
name|gl2
init|=
name|buf
operator|.
name|getLong
argument_list|(
literal|8
argument_list|)
decl_stmt|;
name|long
index|[]
name|hc
init|=
name|Murmur3
operator|.
name|hash128
argument_list|(
name|key
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|key
operator|.
name|getBytes
argument_list|()
operator|.
name|length
argument_list|,
name|seed
argument_list|)
decl_stmt|;
name|long
name|m1
init|=
name|hc
index|[
literal|0
index|]
decl_stmt|;
name|long
name|m2
init|=
name|hc
index|[
literal|1
index|]
decl_stmt|;
name|assertEquals
argument_list|(
name|gl1
argument_list|,
name|m1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|gl2
argument_list|,
name|m2
argument_list|)
expr_stmt|;
name|key
operator|=
literal|"testkey128_testkey128"
expr_stmt|;
name|buf
operator|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|16
argument_list|)
operator|.
name|order
argument_list|(
name|ByteOrder
operator|.
name|LITTLE_ENDIAN
argument_list|)
expr_stmt|;
name|buf
operator|.
name|put
argument_list|(
name|hf
operator|.
name|hashBytes
argument_list|(
name|key
operator|.
name|getBytes
argument_list|()
argument_list|)
operator|.
name|asBytes
argument_list|()
argument_list|)
expr_stmt|;
name|buf
operator|.
name|flip
argument_list|()
expr_stmt|;
name|gl1
operator|=
name|buf
operator|.
name|getLong
argument_list|()
expr_stmt|;
name|gl2
operator|=
name|buf
operator|.
name|getLong
argument_list|(
literal|8
argument_list|)
expr_stmt|;
name|byte
index|[]
name|keyBytes
init|=
name|key
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|hc
operator|=
name|Murmur3
operator|.
name|hash128
argument_list|(
name|keyBytes
argument_list|,
literal|0
argument_list|,
name|keyBytes
operator|.
name|length
argument_list|,
name|seed
argument_list|)
expr_stmt|;
name|m1
operator|=
name|hc
index|[
literal|0
index|]
expr_stmt|;
name|m2
operator|=
name|hc
index|[
literal|1
index|]
expr_stmt|;
name|assertEquals
argument_list|(
name|gl1
argument_list|,
name|m1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|gl2
argument_list|,
name|m2
argument_list|)
expr_stmt|;
name|byte
index|[]
name|offsetKeyBytes
init|=
operator|new
name|byte
index|[
name|keyBytes
operator|.
name|length
operator|+
literal|35
index|]
decl_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|offsetKeyBytes
argument_list|,
operator|(
name|byte
operator|)
operator|-
literal|1
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|keyBytes
argument_list|,
literal|0
argument_list|,
name|offsetKeyBytes
argument_list|,
literal|35
argument_list|,
name|keyBytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|hc
operator|=
name|Murmur3
operator|.
name|hash128
argument_list|(
name|offsetKeyBytes
argument_list|,
literal|35
argument_list|,
name|keyBytes
operator|.
name|length
argument_list|,
name|seed
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|gl1
argument_list|,
name|hc
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|gl2
argument_list|,
name|hc
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHashCodeM3_64
parameter_list|()
block|{
name|byte
index|[]
name|origin
init|=
operator|(
literal|"It was the best of times, it was the worst of times,"
operator|+
literal|" it was the age of wisdom, it was the age of foolishness,"
operator|+
literal|" it was the epoch of belief, it was the epoch of incredulity,"
operator|+
literal|" it was the season of Light, it was the season of Darkness,"
operator|+
literal|" it was the spring of hope, it was the winter of despair,"
operator|+
literal|" we had everything before us, we had nothing before us,"
operator|+
literal|" we were all going direct to Heaven,"
operator|+
literal|" we were all going direct the other way."
operator|)
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|long
name|hash
init|=
name|Murmur3
operator|.
name|hash64
argument_list|(
name|origin
argument_list|,
literal|0
argument_list|,
name|origin
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|305830725663368540L
argument_list|,
name|hash
argument_list|)
expr_stmt|;
name|byte
index|[]
name|originOffset
init|=
operator|new
name|byte
index|[
name|origin
operator|.
name|length
operator|+
literal|150
index|]
decl_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|originOffset
argument_list|,
operator|(
name|byte
operator|)
literal|123
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|origin
argument_list|,
literal|0
argument_list|,
name|originOffset
argument_list|,
literal|150
argument_list|,
name|origin
operator|.
name|length
argument_list|)
expr_stmt|;
name|hash
operator|=
name|Murmur3
operator|.
name|hash64
argument_list|(
name|originOffset
argument_list|,
literal|150
argument_list|,
name|origin
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|305830725663368540L
argument_list|,
name|hash
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHashCodesM3_128_ints
parameter_list|()
block|{
name|int
name|seed
init|=
literal|123
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
decl_stmt|;
name|HashFunction
name|hf
init|=
name|Hashing
operator|.
name|murmur3_128
argument_list|(
name|seed
argument_list|)
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
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|int
name|val
init|=
name|rand
operator|.
name|nextInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|data
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|4
argument_list|)
operator|.
name|putInt
argument_list|(
name|val
argument_list|)
operator|.
name|array
argument_list|()
decl_stmt|;
comment|// guava stores the hashcodes in little endian order
name|ByteBuffer
name|buf
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|16
argument_list|)
operator|.
name|order
argument_list|(
name|ByteOrder
operator|.
name|LITTLE_ENDIAN
argument_list|)
decl_stmt|;
name|buf
operator|.
name|put
argument_list|(
name|hf
operator|.
name|hashBytes
argument_list|(
name|data
argument_list|)
operator|.
name|asBytes
argument_list|()
argument_list|)
expr_stmt|;
name|buf
operator|.
name|flip
argument_list|()
expr_stmt|;
name|long
name|gl1
init|=
name|buf
operator|.
name|getLong
argument_list|()
decl_stmt|;
name|long
name|gl2
init|=
name|buf
operator|.
name|getLong
argument_list|(
literal|8
argument_list|)
decl_stmt|;
name|long
index|[]
name|hc
init|=
name|Murmur3
operator|.
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
name|seed
argument_list|)
decl_stmt|;
name|long
name|m1
init|=
name|hc
index|[
literal|0
index|]
decl_stmt|;
name|long
name|m2
init|=
name|hc
index|[
literal|1
index|]
decl_stmt|;
name|assertEquals
argument_list|(
name|gl1
argument_list|,
name|m1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|gl2
argument_list|,
name|m2
argument_list|)
expr_stmt|;
name|byte
index|[]
name|offsetData
init|=
operator|new
name|byte
index|[
name|data
operator|.
name|length
operator|+
literal|50
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|offsetData
argument_list|,
literal|50
argument_list|,
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
name|hc
operator|=
name|Murmur3
operator|.
name|hash128
argument_list|(
name|offsetData
argument_list|,
literal|50
argument_list|,
name|data
operator|.
name|length
argument_list|,
name|seed
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|gl1
argument_list|,
name|hc
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|gl2
argument_list|,
name|hc
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHashCodesM3_128_longs
parameter_list|()
block|{
name|int
name|seed
init|=
literal|123
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
decl_stmt|;
name|HashFunction
name|hf
init|=
name|Hashing
operator|.
name|murmur3_128
argument_list|(
name|seed
argument_list|)
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
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|long
name|val
init|=
name|rand
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|byte
index|[]
name|data
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|8
argument_list|)
operator|.
name|putLong
argument_list|(
name|val
argument_list|)
operator|.
name|array
argument_list|()
decl_stmt|;
comment|// guava stores the hashcodes in little endian order
name|ByteBuffer
name|buf
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|16
argument_list|)
operator|.
name|order
argument_list|(
name|ByteOrder
operator|.
name|LITTLE_ENDIAN
argument_list|)
decl_stmt|;
name|buf
operator|.
name|put
argument_list|(
name|hf
operator|.
name|hashBytes
argument_list|(
name|data
argument_list|)
operator|.
name|asBytes
argument_list|()
argument_list|)
expr_stmt|;
name|buf
operator|.
name|flip
argument_list|()
expr_stmt|;
name|long
name|gl1
init|=
name|buf
operator|.
name|getLong
argument_list|()
decl_stmt|;
name|long
name|gl2
init|=
name|buf
operator|.
name|getLong
argument_list|(
literal|8
argument_list|)
decl_stmt|;
name|long
index|[]
name|hc
init|=
name|Murmur3
operator|.
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
name|seed
argument_list|)
decl_stmt|;
name|long
name|m1
init|=
name|hc
index|[
literal|0
index|]
decl_stmt|;
name|long
name|m2
init|=
name|hc
index|[
literal|1
index|]
decl_stmt|;
name|assertEquals
argument_list|(
name|gl1
argument_list|,
name|m1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|gl2
argument_list|,
name|m2
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHashCodesM3_128_double
parameter_list|()
block|{
name|int
name|seed
init|=
literal|123
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
decl_stmt|;
name|HashFunction
name|hf
init|=
name|Hashing
operator|.
name|murmur3_128
argument_list|(
name|seed
argument_list|)
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
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|double
name|val
init|=
name|rand
operator|.
name|nextDouble
argument_list|()
decl_stmt|;
name|byte
index|[]
name|data
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|8
argument_list|)
operator|.
name|putDouble
argument_list|(
name|val
argument_list|)
operator|.
name|array
argument_list|()
decl_stmt|;
comment|// guava stores the hashcodes in little endian order
name|ByteBuffer
name|buf
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|16
argument_list|)
operator|.
name|order
argument_list|(
name|ByteOrder
operator|.
name|LITTLE_ENDIAN
argument_list|)
decl_stmt|;
name|buf
operator|.
name|put
argument_list|(
name|hf
operator|.
name|hashBytes
argument_list|(
name|data
argument_list|)
operator|.
name|asBytes
argument_list|()
argument_list|)
expr_stmt|;
name|buf
operator|.
name|flip
argument_list|()
expr_stmt|;
name|long
name|gl1
init|=
name|buf
operator|.
name|getLong
argument_list|()
decl_stmt|;
name|long
name|gl2
init|=
name|buf
operator|.
name|getLong
argument_list|(
literal|8
argument_list|)
decl_stmt|;
name|long
index|[]
name|hc
init|=
name|Murmur3
operator|.
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
name|seed
argument_list|)
decl_stmt|;
name|long
name|m1
init|=
name|hc
index|[
literal|0
index|]
decl_stmt|;
name|long
name|m2
init|=
name|hc
index|[
literal|1
index|]
decl_stmt|;
name|assertEquals
argument_list|(
name|gl1
argument_list|,
name|m1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|gl2
argument_list|,
name|m2
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|test64
parameter_list|()
block|{
specifier|final
name|int
name|seed
init|=
literal|123
decl_stmt|,
name|iters
init|=
literal|1000000
decl_stmt|;
name|ByteBuffer
name|SHORT_BUFFER
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|Short
operator|.
name|BYTES
argument_list|)
decl_stmt|;
name|ByteBuffer
name|INT_BUFFER
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|Integer
operator|.
name|BYTES
argument_list|)
decl_stmt|;
name|ByteBuffer
name|LONG_BUFFER
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|Long
operator|.
name|BYTES
argument_list|)
decl_stmt|;
name|Random
name|rdm
init|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
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
name|iters
condition|;
operator|++
name|i
control|)
block|{
name|long
name|ln
init|=
name|rdm
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|int
name|in
init|=
name|rdm
operator|.
name|nextInt
argument_list|()
decl_stmt|;
name|short
name|sn
init|=
call|(
name|short
call|)
argument_list|(
name|rdm
operator|.
name|nextInt
argument_list|(
literal|2
operator|*
name|Short
operator|.
name|MAX_VALUE
operator|-
literal|1
argument_list|)
operator|-
name|Short
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|float
name|fn
init|=
name|rdm
operator|.
name|nextFloat
argument_list|()
decl_stmt|;
name|double
name|dn
init|=
name|rdm
operator|.
name|nextDouble
argument_list|()
decl_stmt|;
name|SHORT_BUFFER
operator|.
name|putShort
argument_list|(
literal|0
argument_list|,
name|sn
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Murmur3
operator|.
name|hash64
argument_list|(
name|SHORT_BUFFER
operator|.
name|array
argument_list|()
argument_list|)
argument_list|,
name|Murmur3
operator|.
name|hash64
argument_list|(
name|sn
argument_list|)
argument_list|)
expr_stmt|;
name|INT_BUFFER
operator|.
name|putInt
argument_list|(
literal|0
argument_list|,
name|in
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Murmur3
operator|.
name|hash64
argument_list|(
name|INT_BUFFER
operator|.
name|array
argument_list|()
argument_list|)
argument_list|,
name|Murmur3
operator|.
name|hash64
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
name|LONG_BUFFER
operator|.
name|putLong
argument_list|(
literal|0
argument_list|,
name|ln
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Murmur3
operator|.
name|hash64
argument_list|(
name|LONG_BUFFER
operator|.
name|array
argument_list|()
argument_list|)
argument_list|,
name|Murmur3
operator|.
name|hash64
argument_list|(
name|ln
argument_list|)
argument_list|)
expr_stmt|;
name|INT_BUFFER
operator|.
name|putFloat
argument_list|(
literal|0
argument_list|,
name|fn
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Murmur3
operator|.
name|hash64
argument_list|(
name|INT_BUFFER
operator|.
name|array
argument_list|()
argument_list|)
argument_list|,
name|Murmur3
operator|.
name|hash64
argument_list|(
name|Float
operator|.
name|floatToIntBits
argument_list|(
name|fn
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|LONG_BUFFER
operator|.
name|putDouble
argument_list|(
literal|0
argument_list|,
name|dn
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Murmur3
operator|.
name|hash64
argument_list|(
name|LONG_BUFFER
operator|.
name|array
argument_list|()
argument_list|)
argument_list|,
name|Murmur3
operator|.
name|hash64
argument_list|(
name|Double
operator|.
name|doubleToLongBits
argument_list|(
name|dn
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIncremental
parameter_list|()
block|{
specifier|final
name|int
name|seed
init|=
literal|123
decl_stmt|,
name|arraySize
init|=
literal|1023
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|arraySize
index|]
decl_stmt|;
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
operator|.
name|nextBytes
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|int
name|expected
init|=
name|Murmur3
operator|.
name|hash32
argument_list|(
name|bytes
argument_list|,
name|arraySize
argument_list|)
decl_stmt|;
name|Murmur3
operator|.
name|IncrementalHash32
name|same
init|=
operator|new
name|IncrementalHash32
argument_list|()
decl_stmt|,
name|diff
init|=
operator|new
name|IncrementalHash32
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|blockSize
init|=
literal|1
init|;
name|blockSize
operator|<=
name|arraySize
condition|;
operator|++
name|blockSize
control|)
block|{
name|byte
index|[]
name|block
init|=
operator|new
name|byte
index|[
name|blockSize
index|]
decl_stmt|;
name|same
operator|.
name|start
argument_list|(
name|Murmur3
operator|.
name|DEFAULT_SEED
argument_list|)
expr_stmt|;
name|diff
operator|.
name|start
argument_list|(
name|Murmur3
operator|.
name|DEFAULT_SEED
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|offset
init|=
literal|0
init|;
name|offset
operator|<
name|arraySize
condition|;
name|offset
operator|+=
name|blockSize
control|)
block|{
name|int
name|length
init|=
name|Math
operator|.
name|min
argument_list|(
name|arraySize
operator|-
name|offset
argument_list|,
name|blockSize
argument_list|)
decl_stmt|;
name|same
operator|.
name|add
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|block
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|diff
operator|.
name|add
argument_list|(
name|block
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"Block size "
operator|+
name|blockSize
argument_list|,
name|expected
argument_list|,
name|same
operator|.
name|end
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Block size "
operator|+
name|blockSize
argument_list|,
name|expected
argument_list|,
name|diff
operator|.
name|end
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

