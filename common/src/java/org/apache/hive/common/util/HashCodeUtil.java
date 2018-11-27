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

begin_comment
comment|/*  * Common hash code routines.  */
end_comment

begin_class
specifier|public
class|class
name|HashCodeUtil
block|{
specifier|public
specifier|static
name|int
name|calculateIntHashCode
parameter_list|(
name|int
name|key
parameter_list|)
block|{
name|key
operator|=
operator|~
name|key
operator|+
operator|(
name|key
operator|<<
literal|15
operator|)
expr_stmt|;
comment|// key = (key<< 15) - key - 1;
name|key
operator|=
name|key
operator|^
operator|(
name|key
operator|>>>
literal|12
operator|)
expr_stmt|;
name|key
operator|=
name|key
operator|+
operator|(
name|key
operator|<<
literal|2
operator|)
expr_stmt|;
name|key
operator|=
name|key
operator|^
operator|(
name|key
operator|>>>
literal|4
operator|)
expr_stmt|;
name|key
operator|=
name|key
operator|*
literal|2057
expr_stmt|;
comment|// key = (key + (key<< 3)) + (key<< 11);
name|key
operator|=
name|key
operator|^
operator|(
name|key
operator|>>>
literal|16
operator|)
expr_stmt|;
return|return
name|key
return|;
block|}
specifier|public
specifier|static
name|int
name|calculateTwoLongHashCode
parameter_list|(
name|long
name|l0
parameter_list|,
name|long
name|l1
parameter_list|)
block|{
return|return
name|Murmur3
operator|.
name|hash32
argument_list|(
name|l0
argument_list|,
name|l1
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|int
name|calculateLongHashCode
parameter_list|(
name|long
name|key
parameter_list|)
block|{
return|return
name|Murmur3
operator|.
name|hash32
argument_list|(
name|key
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|calculateLongArrayHashCodes
parameter_list|(
name|long
index|[]
name|longs
parameter_list|,
name|int
index|[]
name|hashCodes
parameter_list|,
specifier|final
name|int
name|count
parameter_list|)
block|{
for|for
control|(
name|int
name|v
init|=
literal|0
init|;
name|v
operator|<
name|count
condition|;
name|v
operator|++
control|)
block|{
name|hashCodes
index|[
name|v
index|]
operator|=
operator|(
name|int
operator|)
name|calculateLongHashCode
argument_list|(
name|longs
index|[
name|v
index|]
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|int
name|calculateBytesHashCode
parameter_list|(
name|byte
index|[]
name|keyBytes
parameter_list|,
name|int
name|keyStart
parameter_list|,
name|int
name|keyLength
parameter_list|)
block|{
return|return
name|murmurHash
argument_list|(
name|keyBytes
argument_list|,
name|keyStart
argument_list|,
name|keyLength
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|calculateBytesArrayHashCodes
parameter_list|(
name|byte
index|[]
index|[]
name|bytesArrays
parameter_list|,
name|int
index|[]
name|starts
parameter_list|,
name|int
index|[]
name|lengths
parameter_list|,
name|int
index|[]
name|valueSelected
parameter_list|,
name|int
index|[]
name|hashCodes
parameter_list|,
specifier|final
name|int
name|count
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
operator|<
name|count
condition|;
name|i
operator|++
control|)
block|{
name|int
name|batchIndex
init|=
name|valueSelected
index|[
name|i
index|]
decl_stmt|;
name|hashCodes
index|[
name|i
index|]
operator|=
name|murmurHash
argument_list|(
name|bytesArrays
index|[
name|batchIndex
index|]
argument_list|,
name|starts
index|[
name|batchIndex
index|]
argument_list|,
name|lengths
index|[
name|batchIndex
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Lifted from org.apache.hadoop.util.hash.MurmurHash... but supports offset.
comment|// Must produce the same result as MurmurHash.hash with seed = 0.
specifier|public
specifier|static
name|int
name|murmurHash
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
name|int
name|m
init|=
literal|0x5bd1e995
decl_stmt|;
name|int
name|r
init|=
literal|24
decl_stmt|;
name|int
name|h
init|=
name|length
decl_stmt|;
name|int
name|len_4
init|=
name|length
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
name|len_4
condition|;
name|i
operator|++
control|)
block|{
name|int
name|i_4
init|=
name|offset
operator|+
operator|(
name|i
operator|<<
literal|2
operator|)
decl_stmt|;
name|int
name|k
init|=
name|data
index|[
name|i_4
operator|+
literal|3
index|]
decl_stmt|;
name|k
operator|=
name|k
operator|<<
literal|8
expr_stmt|;
name|k
operator|=
name|k
operator||
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
expr_stmt|;
name|k
operator|=
name|k
operator|<<
literal|8
expr_stmt|;
name|k
operator|=
name|k
operator||
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
expr_stmt|;
name|k
operator|=
name|k
operator|<<
literal|8
expr_stmt|;
name|k
operator|=
name|k
operator||
operator|(
name|data
index|[
name|i_4
operator|+
literal|0
index|]
operator|&
literal|0xff
operator|)
expr_stmt|;
name|k
operator|*=
name|m
expr_stmt|;
name|k
operator|^=
name|k
operator|>>>
name|r
expr_stmt|;
name|k
operator|*=
name|m
expr_stmt|;
name|h
operator|*=
name|m
expr_stmt|;
name|h
operator|^=
name|k
expr_stmt|;
block|}
comment|// avoid calculating modulo
name|int
name|len_m
init|=
name|len_4
operator|<<
literal|2
decl_stmt|;
name|int
name|left
init|=
name|length
operator|-
name|len_m
decl_stmt|;
if|if
condition|(
name|left
operator|!=
literal|0
condition|)
block|{
name|length
operator|+=
name|offset
expr_stmt|;
if|if
condition|(
name|left
operator|>=
literal|3
condition|)
block|{
name|h
operator|^=
operator|(
name|int
operator|)
name|data
index|[
name|length
operator|-
literal|3
index|]
operator|<<
literal|16
expr_stmt|;
block|}
if|if
condition|(
name|left
operator|>=
literal|2
condition|)
block|{
name|h
operator|^=
operator|(
name|int
operator|)
name|data
index|[
name|length
operator|-
literal|2
index|]
operator|<<
literal|8
expr_stmt|;
block|}
if|if
condition|(
name|left
operator|>=
literal|1
condition|)
block|{
name|h
operator|^=
operator|(
name|int
operator|)
name|data
index|[
name|length
operator|-
literal|1
index|]
expr_stmt|;
block|}
name|h
operator|*=
name|m
expr_stmt|;
block|}
name|h
operator|^=
name|h
operator|>>>
literal|13
expr_stmt|;
name|h
operator|*=
name|m
expr_stmt|;
name|h
operator|^=
name|h
operator|>>>
literal|15
expr_stmt|;
return|return
name|h
return|;
block|}
block|}
end_class

end_unit

