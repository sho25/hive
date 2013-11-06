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
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|expressions
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

begin_class
specifier|public
class|class
name|TestCuckooSet
block|{
comment|// maximum table size
specifier|private
specifier|static
name|int
name|MAX_SIZE
init|=
literal|65437
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testSetLong
parameter_list|()
block|{
comment|// Set of values to look for. Include the original blank value Long.MIN_VALUE to make sure
comment|// the process of choosing a new blank works.
name|Long
index|[]
name|values
init|=
block|{
literal|1L
block|,
literal|2L
block|,
literal|3L
block|,
literal|1000L
block|,
literal|2000L
block|,
literal|3000L
block|,
literal|8L
block|,
literal|8L
block|,
literal|9L
block|,
literal|13L
block|,
literal|17L
block|,
literal|22L
block|,
literal|23L
block|,
literal|24L
block|,
literal|25L
block|,
operator|-
literal|26L
block|,
literal|27L
block|,
literal|28L
block|,
literal|29L
block|,
literal|30L
block|,
literal|111111111111111L
block|,
operator|-
literal|444444444444444L
block|,
name|Long
operator|.
name|MIN_VALUE
block|}
decl_stmt|;
name|Long
index|[]
name|negatives
init|=
block|{
literal|0L
block|,
literal|4L
block|,
literal|4000L
block|,
operator|-
literal|2L
block|,
literal|19L
block|,
literal|222222222222222L
block|,
operator|-
literal|333333333333333L
block|}
decl_stmt|;
name|CuckooSetLong
name|s
init|=
operator|new
name|CuckooSetLong
argument_list|(
name|values
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|Long
name|v
range|:
name|values
control|)
block|{
name|s
operator|.
name|insert
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
comment|// test that the values we added are there
for|for
control|(
name|Long
name|v
range|:
name|values
control|)
block|{
name|assertTrue
argument_list|(
name|s
operator|.
name|lookup
argument_list|(
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// test that values that we know are missing are shown to be absent
for|for
control|(
name|Long
name|v
range|:
name|negatives
control|)
block|{
name|assertFalse
argument_list|(
name|s
operator|.
name|lookup
argument_list|(
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Set of values to look for.
name|Long
index|[]
name|values2
init|=
block|{
literal|1L
block|,
literal|2L
block|,
literal|3L
block|,
literal|1000L
block|,
literal|2000L
block|,
literal|3000L
block|,
literal|8L
block|,
literal|8L
block|,
literal|9L
block|,
literal|13L
block|,
literal|17L
block|,
literal|22L
block|,
literal|23L
block|,
literal|24L
block|,
literal|25L
block|,
operator|-
literal|26L
block|,
literal|27L
block|,
literal|28L
block|,
literal|29L
block|,
literal|30L
block|,
literal|111111111111111L
block|,
operator|-
literal|444444444444444L
block|}
decl_stmt|;
comment|// Include the original blank value Long.MIN_VALUE in the negatives to make sure we get
comment|// the correct result that the blank value is not there.
name|Long
index|[]
name|negatives2
init|=
block|{
literal|0L
block|,
literal|4L
block|,
literal|4000L
block|,
operator|-
literal|2L
block|,
literal|19L
block|,
literal|222222222222222L
block|,
operator|-
literal|333333333333333L
block|,
name|Long
operator|.
name|MIN_VALUE
block|}
decl_stmt|;
name|s
operator|=
operator|new
name|CuckooSetLong
argument_list|(
name|values2
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|Long
name|v
range|:
name|values2
control|)
block|{
name|s
operator|.
name|insert
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
comment|// test that the values we added are there
for|for
control|(
name|Long
name|v
range|:
name|values2
control|)
block|{
name|assertTrue
argument_list|(
name|s
operator|.
name|lookup
argument_list|(
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// test that values that we know are missing are shown to be absent
for|for
control|(
name|Long
name|v
range|:
name|negatives2
control|)
block|{
name|assertFalse
argument_list|(
name|s
operator|.
name|lookup
argument_list|(
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// load multiple random sets of Long values
annotation|@
name|Test
specifier|public
name|void
name|testSetLongRandom
parameter_list|()
block|{
name|long
index|[]
name|values
decl_stmt|;
name|Random
name|gen
init|=
operator|new
name|Random
argument_list|(
literal|98763537
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
literal|200
condition|;
control|)
block|{
comment|// Make a random array of longs
name|int
name|size
init|=
name|gen
operator|.
name|nextInt
argument_list|()
operator|%
name|MAX_SIZE
decl_stmt|;
if|if
condition|(
name|size
operator|<=
literal|0
condition|)
block|{
comment|// ensure size is>= 1, otherwise try again
continue|continue;
block|}
name|i
operator|++
expr_stmt|;
name|values
operator|=
operator|new
name|long
index|[
name|size
index|]
expr_stmt|;
name|loadRandom
argument_list|(
name|values
argument_list|,
name|gen
argument_list|)
expr_stmt|;
comment|// load them into a SetLong
name|CuckooSetLong
name|s
init|=
operator|new
name|CuckooSetLong
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|loadSet
argument_list|(
name|s
argument_list|,
name|values
argument_list|)
expr_stmt|;
comment|// look them up to make sure they are all there
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|!=
name|size
condition|;
name|j
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|s
operator|.
name|lookup
argument_list|(
name|values
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetDouble
parameter_list|()
block|{
comment|// Set of values to look for.
name|Double
index|[]
name|values
init|=
block|{
literal|7021.0D
block|,
literal|5780.0D
block|,
literal|0D
block|,
operator|-
literal|1D
block|,
literal|1.999e50D
block|}
decl_stmt|;
name|Double
index|[]
name|negatives
init|=
block|{
literal|7000.0D
block|,
operator|-
literal|2D
block|,
literal|1.9999e50D
block|}
decl_stmt|;
name|CuckooSetDouble
name|s
init|=
operator|new
name|CuckooSetDouble
argument_list|(
name|values
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|Double
name|v
range|:
name|values
control|)
block|{
name|s
operator|.
name|insert
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
comment|// test that the values we added are there
for|for
control|(
name|Double
name|v
range|:
name|values
control|)
block|{
name|assertTrue
argument_list|(
name|s
operator|.
name|lookup
argument_list|(
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// test that values that we know are missing are shown to be absent
for|for
control|(
name|Double
name|v
range|:
name|negatives
control|)
block|{
name|assertFalse
argument_list|(
name|s
operator|.
name|lookup
argument_list|(
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetBytes
parameter_list|()
block|{
name|String
index|[]
name|strings
init|=
block|{
literal|"foo"
block|,
literal|"bar"
block|,
literal|"baz"
block|,
literal|"a"
block|,
literal|""
block|,
literal|"x1341"
block|,
literal|"Z"
block|}
decl_stmt|;
name|String
index|[]
name|negativeStrings
init|=
block|{
literal|"not"
block|,
literal|"in"
block|,
literal|"the"
block|,
literal|"set"
block|,
literal|"foobar"
block|}
decl_stmt|;
name|byte
index|[]
index|[]
name|values
init|=
name|getByteArrays
argument_list|(
name|strings
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|negatives
init|=
name|getByteArrays
argument_list|(
name|negativeStrings
argument_list|)
decl_stmt|;
comment|// load set
name|CuckooSetBytes
name|s
init|=
operator|new
name|CuckooSetBytes
argument_list|(
name|strings
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|v
range|:
name|values
control|)
block|{
name|s
operator|.
name|insert
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
comment|// test that the values we added are there
for|for
control|(
name|byte
index|[]
name|v
range|:
name|values
control|)
block|{
name|assertTrue
argument_list|(
name|s
operator|.
name|lookup
argument_list|(
name|v
argument_list|,
literal|0
argument_list|,
name|v
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// test that values that we know are missing are shown to be absent
for|for
control|(
name|byte
index|[]
name|v
range|:
name|negatives
control|)
block|{
name|assertFalse
argument_list|(
name|s
operator|.
name|lookup
argument_list|(
name|v
argument_list|,
literal|0
argument_list|,
name|v
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Test that we can search correctly using a buffer and pulling
comment|// a sequence of bytes out of the middle of it. In this case it
comment|// is the 3 letter sequence "foo".
name|byte
index|[]
name|buf
init|=
name|getUTF8Bytes
argument_list|(
literal|"thewordfooisinhere"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|s
operator|.
name|lookup
argument_list|(
name|buf
argument_list|,
literal|7
argument_list|,
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetBytesLargeRandom
parameter_list|()
block|{
name|byte
index|[]
index|[]
name|values
decl_stmt|;
name|Random
name|gen
init|=
operator|new
name|Random
argument_list|(
literal|98763537
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
literal|200
condition|;
control|)
block|{
comment|// Make a random array of byte arrays
name|int
name|size
init|=
name|gen
operator|.
name|nextInt
argument_list|()
operator|%
name|MAX_SIZE
decl_stmt|;
if|if
condition|(
name|size
operator|<=
literal|0
condition|)
block|{
comment|// ensure size is>= 1, otherwise try again
continue|continue;
block|}
name|i
operator|++
expr_stmt|;
name|values
operator|=
operator|new
name|byte
index|[
name|size
index|]
index|[]
expr_stmt|;
name|loadRandomBytes
argument_list|(
name|values
argument_list|,
name|gen
argument_list|)
expr_stmt|;
comment|// load them into a set
name|CuckooSetBytes
name|s
init|=
operator|new
name|CuckooSetBytes
argument_list|(
name|size
argument_list|)
decl_stmt|;
name|loadSet
argument_list|(
name|s
argument_list|,
name|values
argument_list|)
expr_stmt|;
comment|// look them up to make sure they are all there
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|!=
name|size
condition|;
name|j
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|s
operator|.
name|lookup
argument_list|(
name|values
index|[
name|j
index|]
argument_list|,
literal|0
argument_list|,
name|values
index|[
name|j
index|]
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|loadRandomBytes
parameter_list|(
name|byte
index|[]
index|[]
name|values
parameter_list|,
name|Random
name|gen
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|values
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|values
index|[
name|i
index|]
operator|=
name|getUTF8Bytes
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|gen
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|byte
index|[]
name|getUTF8Bytes
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|byte
index|[]
name|v
init|=
literal|null
decl_stmt|;
try|try
block|{
name|v
operator|=
name|s
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
empty_stmt|;
comment|// won't happen
block|}
return|return
name|v
return|;
block|}
comment|// Get an array of UTF-8 byte arrays from an array of strings
specifier|private
name|byte
index|[]
index|[]
name|getByteArrays
parameter_list|(
name|String
index|[]
name|strings
parameter_list|)
block|{
name|byte
index|[]
index|[]
name|values
init|=
operator|new
name|byte
index|[
name|strings
operator|.
name|length
index|]
index|[]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|strings
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|values
index|[
name|i
index|]
operator|=
name|strings
index|[
name|i
index|]
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
empty_stmt|;
comment|// can't happen
block|}
block|}
return|return
name|values
return|;
block|}
specifier|private
name|void
name|loadSet
parameter_list|(
name|CuckooSetLong
name|s
parameter_list|,
name|long
index|[]
name|values
parameter_list|)
block|{
for|for
control|(
name|Long
name|v
range|:
name|values
control|)
block|{
name|s
operator|.
name|insert
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|loadSet
parameter_list|(
name|CuckooSetBytes
name|s
parameter_list|,
name|byte
index|[]
index|[]
name|values
parameter_list|)
block|{
for|for
control|(
name|byte
index|[]
name|v
range|:
name|values
control|)
block|{
name|s
operator|.
name|insert
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|loadRandom
parameter_list|(
name|long
index|[]
name|a
parameter_list|,
name|Random
name|gen
parameter_list|)
block|{
name|int
name|size
init|=
name|a
operator|.
name|length
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|size
condition|;
name|i
operator|++
control|)
block|{
name|a
index|[
name|i
index|]
operator|=
name|gen
operator|.
name|nextLong
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

