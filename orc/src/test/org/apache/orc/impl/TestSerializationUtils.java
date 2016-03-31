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
name|orc
operator|.
name|impl
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
name|assertArrayEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|math
operator|.
name|BigInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|math
operator|.
name|LongMath
import|;
end_import

begin_class
specifier|public
class|class
name|TestSerializationUtils
block|{
specifier|private
name|InputStream
name|fromBuffer
parameter_list|(
name|ByteArrayOutputStream
name|buffer
parameter_list|)
block|{
return|return
operator|new
name|ByteArrayInputStream
argument_list|(
name|buffer
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDoubles
parameter_list|()
throws|throws
name|Exception
block|{
name|double
name|tolerance
init|=
literal|0.0000000000000001
decl_stmt|;
name|ByteArrayOutputStream
name|buffer
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|SerializationUtils
name|utils
init|=
operator|new
name|SerializationUtils
argument_list|()
decl_stmt|;
name|utils
operator|.
name|writeDouble
argument_list|(
name|buffer
argument_list|,
literal|1343822337.759
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1343822337.759
argument_list|,
name|utils
operator|.
name|readDouble
argument_list|(
name|fromBuffer
argument_list|(
name|buffer
argument_list|)
argument_list|)
argument_list|,
name|tolerance
argument_list|)
expr_stmt|;
name|buffer
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|utils
operator|.
name|writeDouble
argument_list|(
name|buffer
argument_list|,
literal|0.8
argument_list|)
expr_stmt|;
name|double
name|got
init|=
name|utils
operator|.
name|readDouble
argument_list|(
name|fromBuffer
argument_list|(
name|buffer
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0.8
argument_list|,
name|got
argument_list|,
name|tolerance
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBigIntegers
parameter_list|()
throws|throws
name|Exception
block|{
name|ByteArrayOutputStream
name|buffer
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|SerializationUtils
operator|.
name|writeBigInteger
argument_list|(
name|buffer
argument_list|,
name|BigInteger
operator|.
name|valueOf
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|0
block|}
argument_list|,
name|buffer
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|SerializationUtils
operator|.
name|readBigInteger
argument_list|(
name|fromBuffer
argument_list|(
name|buffer
argument_list|)
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|SerializationUtils
operator|.
name|writeBigInteger
argument_list|(
name|buffer
argument_list|,
name|BigInteger
operator|.
name|valueOf
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|2
block|}
argument_list|,
name|buffer
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|SerializationUtils
operator|.
name|readBigInteger
argument_list|(
name|fromBuffer
argument_list|(
name|buffer
argument_list|)
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|SerializationUtils
operator|.
name|writeBigInteger
argument_list|(
name|buffer
argument_list|,
name|BigInteger
operator|.
name|valueOf
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|,
name|buffer
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1L
argument_list|,
name|SerializationUtils
operator|.
name|readBigInteger
argument_list|(
name|fromBuffer
argument_list|(
name|buffer
argument_list|)
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|SerializationUtils
operator|.
name|writeBigInteger
argument_list|(
name|buffer
argument_list|,
name|BigInteger
operator|.
name|valueOf
argument_list|(
literal|50
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|100
block|}
argument_list|,
name|buffer
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|50L
argument_list|,
name|SerializationUtils
operator|.
name|readBigInteger
argument_list|(
name|fromBuffer
argument_list|(
name|buffer
argument_list|)
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|SerializationUtils
operator|.
name|writeBigInteger
argument_list|(
name|buffer
argument_list|,
name|BigInteger
operator|.
name|valueOf
argument_list|(
operator|-
literal|50
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|99
block|}
argument_list|,
name|buffer
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|50L
argument_list|,
name|SerializationUtils
operator|.
name|readBigInteger
argument_list|(
name|fromBuffer
argument_list|(
name|buffer
argument_list|)
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
operator|-
literal|8192
init|;
name|i
operator|<
literal|8192
condition|;
operator|++
name|i
control|)
block|{
name|buffer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|SerializationUtils
operator|.
name|writeBigInteger
argument_list|(
name|buffer
argument_list|,
name|BigInteger
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"compare length for "
operator|+
name|i
argument_list|,
name|i
operator|>=
operator|-
literal|64
operator|&&
name|i
operator|<
literal|64
condition|?
literal|1
else|:
literal|2
argument_list|,
name|buffer
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"compare result for "
operator|+
name|i
argument_list|,
name|i
argument_list|,
name|SerializationUtils
operator|.
name|readBigInteger
argument_list|(
name|fromBuffer
argument_list|(
name|buffer
argument_list|)
argument_list|)
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|SerializationUtils
operator|.
name|writeBigInteger
argument_list|(
name|buffer
argument_list|,
operator|new
name|BigInteger
argument_list|(
literal|"123456789abcdef0"
argument_list|,
literal|16
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|BigInteger
argument_list|(
literal|"123456789abcdef0"
argument_list|,
literal|16
argument_list|)
argument_list|,
name|SerializationUtils
operator|.
name|readBigInteger
argument_list|(
name|fromBuffer
argument_list|(
name|buffer
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|SerializationUtils
operator|.
name|writeBigInteger
argument_list|(
name|buffer
argument_list|,
operator|new
name|BigInteger
argument_list|(
literal|"-123456789abcdef0"
argument_list|,
literal|16
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|BigInteger
argument_list|(
literal|"-123456789abcdef0"
argument_list|,
literal|16
argument_list|)
argument_list|,
name|SerializationUtils
operator|.
name|readBigInteger
argument_list|(
name|fromBuffer
argument_list|(
name|buffer
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|()
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
literal|256
condition|;
operator|++
name|i
control|)
block|{
name|String
name|num
init|=
name|Integer
operator|.
name|toHexString
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|num
operator|.
name|length
argument_list|()
operator|==
literal|1
condition|)
block|{
name|buf
operator|.
name|append
argument_list|(
literal|'0'
argument_list|)
expr_stmt|;
block|}
name|buf
operator|.
name|append
argument_list|(
name|num
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|SerializationUtils
operator|.
name|writeBigInteger
argument_list|(
name|buffer
argument_list|,
operator|new
name|BigInteger
argument_list|(
name|buf
operator|.
name|toString
argument_list|()
argument_list|,
literal|16
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|BigInteger
argument_list|(
name|buf
operator|.
name|toString
argument_list|()
argument_list|,
literal|16
argument_list|)
argument_list|,
name|SerializationUtils
operator|.
name|readBigInteger
argument_list|(
name|fromBuffer
argument_list|(
name|buffer
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|reset
argument_list|()
expr_stmt|;
name|SerializationUtils
operator|.
name|writeBigInteger
argument_list|(
name|buffer
argument_list|,
operator|new
name|BigInteger
argument_list|(
literal|"ff000000000000000000000000000000000000000000ff"
argument_list|,
literal|16
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|BigInteger
argument_list|(
literal|"ff000000000000000000000000000000000000000000ff"
argument_list|,
literal|16
argument_list|)
argument_list|,
name|SerializationUtils
operator|.
name|readBigInteger
argument_list|(
name|fromBuffer
argument_list|(
name|buffer
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSubtractionOverflow
parameter_list|()
block|{
comment|// cross check results with Guava results below
name|SerializationUtils
name|utils
init|=
operator|new
name|SerializationUtils
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|utils
operator|.
name|isSafeSubtract
argument_list|(
literal|22222222222L
argument_list|,
name|Long
operator|.
name|MIN_VALUE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|utils
operator|.
name|isSafeSubtract
argument_list|(
operator|-
literal|22222222222L
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|utils
operator|.
name|isSafeSubtract
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|utils
operator|.
name|isSafeSubtract
argument_list|(
operator|-
literal|1553103058346370095L
argument_list|,
literal|6553103058346370095L
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|utils
operator|.
name|isSafeSubtract
argument_list|(
literal|0
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|utils
operator|.
name|isSafeSubtract
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSubtractionOverflowGuava
parameter_list|()
block|{
try|try
block|{
name|LongMath
operator|.
name|checkedSubtract
argument_list|(
literal|22222222222L
argument_list|,
name|Long
operator|.
name|MIN_VALUE
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected ArithmeticException for overflow"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArithmeticException
name|ex
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"overflow"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|LongMath
operator|.
name|checkedSubtract
argument_list|(
operator|-
literal|22222222222L
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected ArithmeticException for overflow"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArithmeticException
name|ex
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"overflow"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|LongMath
operator|.
name|checkedSubtract
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected ArithmeticException for overflow"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArithmeticException
name|ex
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"overflow"
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
operator|-
literal|8106206116692740190L
argument_list|,
name|LongMath
operator|.
name|checkedSubtract
argument_list|(
operator|-
literal|1553103058346370095L
argument_list|,
literal|6553103058346370095L
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|LongMath
operator|.
name|checkedSubtract
argument_list|(
literal|0
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|,
name|LongMath
operator|.
name|checkedSubtract
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRandomFloats
parameter_list|()
throws|throws
name|Exception
block|{
name|float
name|tolerance
init|=
literal|0.0000000000000001f
decl_stmt|;
name|ByteArrayOutputStream
name|buffer
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|SerializationUtils
name|utils
init|=
operator|new
name|SerializationUtils
argument_list|()
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|int
name|n
init|=
literal|100_000
decl_stmt|;
name|float
index|[]
name|expected
init|=
operator|new
name|float
index|[
name|n
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
name|n
condition|;
name|i
operator|++
control|)
block|{
name|float
name|f
init|=
name|rand
operator|.
name|nextFloat
argument_list|()
decl_stmt|;
name|expected
index|[
name|i
index|]
operator|=
name|f
expr_stmt|;
name|utils
operator|.
name|writeFloat
argument_list|(
name|buffer
argument_list|,
name|f
argument_list|)
expr_stmt|;
block|}
name|InputStream
name|newBuffer
init|=
name|fromBuffer
argument_list|(
name|buffer
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
name|n
condition|;
name|i
operator|++
control|)
block|{
name|float
name|got
init|=
name|utils
operator|.
name|readFloat
argument_list|(
name|newBuffer
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
index|[
name|i
index|]
argument_list|,
name|got
argument_list|,
name|tolerance
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRandomDoubles
parameter_list|()
throws|throws
name|Exception
block|{
name|double
name|tolerance
init|=
literal|0.0000000000000001
decl_stmt|;
name|ByteArrayOutputStream
name|buffer
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|SerializationUtils
name|utils
init|=
operator|new
name|SerializationUtils
argument_list|()
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|int
name|n
init|=
literal|100_000
decl_stmt|;
name|double
index|[]
name|expected
init|=
operator|new
name|double
index|[
name|n
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
name|n
condition|;
name|i
operator|++
control|)
block|{
name|double
name|d
init|=
name|rand
operator|.
name|nextDouble
argument_list|()
decl_stmt|;
name|expected
index|[
name|i
index|]
operator|=
name|d
expr_stmt|;
name|utils
operator|.
name|writeDouble
argument_list|(
name|buffer
argument_list|,
name|d
argument_list|)
expr_stmt|;
block|}
name|InputStream
name|newBuffer
init|=
name|fromBuffer
argument_list|(
name|buffer
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
name|n
condition|;
name|i
operator|++
control|)
block|{
name|double
name|got
init|=
name|utils
operator|.
name|readDouble
argument_list|(
name|newBuffer
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
index|[
name|i
index|]
argument_list|,
name|got
argument_list|,
name|tolerance
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

