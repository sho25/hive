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
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|mapjoin
operator|.
name|fast
package|;
end_package

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
name|util
operator|.
name|Properties
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
name|JoinUtil
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
name|ql
operator|.
name|exec
operator|.
name|mapjoin
operator|.
name|MapJoinMemoryExhaustionError
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
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|mapjoin
operator|.
name|hashtable
operator|.
name|VectorMapJoinHashMapResult
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
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|mapjoin
operator|.
name|fast
operator|.
name|CheckFastHashTable
operator|.
name|VerifyFastLongHashMap
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|ql
operator|.
name|plan
operator|.
name|TableDesc
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
name|ql
operator|.
name|plan
operator|.
name|VectorMapJoinDesc
operator|.
name|HashTableKeyType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
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

begin_class
specifier|public
class|class
name|TestVectorMapJoinFastLongHashMap
extends|extends
name|CommonFastHashTable
block|{
specifier|private
specifier|static
name|TableDesc
name|tableDesc
init|=
operator|new
name|TableDesc
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|tableDesc
operator|.
name|setProperties
argument_list|(
operator|new
name|Properties
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOneKey
parameter_list|()
throws|throws
name|Exception
block|{
name|random
operator|=
operator|new
name|Random
argument_list|(
literal|33221
argument_list|)
expr_stmt|;
name|VectorMapJoinFastLongHashMap
name|map
init|=
operator|new
name|VectorMapJoinFastLongHashMap
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
name|HashTableKeyType
operator|.
name|LONG
argument_list|,
name|CAPACITY
argument_list|,
name|LOAD_FACTOR
argument_list|,
name|WB_SIZE
argument_list|,
operator|-
literal|1
argument_list|,
name|tableDesc
argument_list|)
decl_stmt|;
name|VerifyFastLongHashMap
name|verifyTable
init|=
operator|new
name|VerifyFastLongHashMap
argument_list|()
decl_stmt|;
name|long
name|key
init|=
name|random
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
name|random
operator|.
name|nextInt
argument_list|(
name|MAX_VALUE_LENGTH
argument_list|)
index|]
decl_stmt|;
name|random
operator|.
name|nextBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|map
operator|.
name|testPutRow
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|verifyTable
operator|.
name|add
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|verifyTable
operator|.
name|verify
argument_list|(
name|map
argument_list|)
expr_stmt|;
comment|// Second value.
name|value
operator|=
operator|new
name|byte
index|[
name|random
operator|.
name|nextInt
argument_list|(
name|MAX_VALUE_LENGTH
argument_list|)
index|]
expr_stmt|;
name|random
operator|.
name|nextBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|map
operator|.
name|testPutRow
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|verifyTable
operator|.
name|add
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|verifyTable
operator|.
name|verify
argument_list|(
name|map
argument_list|)
expr_stmt|;
comment|// Third value.
name|value
operator|=
operator|new
name|byte
index|[
name|random
operator|.
name|nextInt
argument_list|(
name|MAX_VALUE_LENGTH
argument_list|)
index|]
expr_stmt|;
name|random
operator|.
name|nextBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|map
operator|.
name|testPutRow
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|verifyTable
operator|.
name|add
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|verifyTable
operator|.
name|verify
argument_list|(
name|map
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultipleKeysSingleValue
parameter_list|()
throws|throws
name|Exception
block|{
name|random
operator|=
operator|new
name|Random
argument_list|(
literal|900
argument_list|)
expr_stmt|;
name|VectorMapJoinFastLongHashMap
name|map
init|=
operator|new
name|VectorMapJoinFastLongHashMap
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
name|HashTableKeyType
operator|.
name|LONG
argument_list|,
name|CAPACITY
argument_list|,
name|LOAD_FACTOR
argument_list|,
name|WB_SIZE
argument_list|,
operator|-
literal|1
argument_list|,
name|tableDesc
argument_list|)
decl_stmt|;
name|VerifyFastLongHashMap
name|verifyTable
init|=
operator|new
name|VerifyFastLongHashMap
argument_list|()
decl_stmt|;
name|int
name|keyCount
init|=
literal|100
operator|+
name|random
operator|.
name|nextInt
argument_list|(
literal|1000
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
name|keyCount
condition|;
name|i
operator|++
control|)
block|{
name|long
name|key
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|key
operator|=
name|random
operator|.
name|nextLong
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|verifyTable
operator|.
name|contains
argument_list|(
name|key
argument_list|)
condition|)
block|{
comment|// Unique keys for this test.
break|break;
block|}
block|}
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
name|random
operator|.
name|nextInt
argument_list|(
name|MAX_VALUE_LENGTH
argument_list|)
index|]
decl_stmt|;
name|random
operator|.
name|nextBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|map
operator|.
name|testPutRow
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|verifyTable
operator|.
name|add
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
comment|// verifyTable.verify(map);
block|}
name|verifyTable
operator|.
name|verify
argument_list|(
name|map
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetNonExistent
parameter_list|()
throws|throws
name|Exception
block|{
name|random
operator|=
operator|new
name|Random
argument_list|(
literal|450
argument_list|)
expr_stmt|;
name|VectorMapJoinFastLongHashMap
name|map
init|=
operator|new
name|VectorMapJoinFastLongHashMap
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
name|HashTableKeyType
operator|.
name|LONG
argument_list|,
name|CAPACITY
argument_list|,
name|LOAD_FACTOR
argument_list|,
name|WB_SIZE
argument_list|,
operator|-
literal|1
argument_list|,
name|tableDesc
argument_list|)
decl_stmt|;
name|VerifyFastLongHashMap
name|verifyTable
init|=
operator|new
name|VerifyFastLongHashMap
argument_list|()
decl_stmt|;
name|long
name|key1
init|=
name|random
operator|.
name|nextLong
argument_list|()
decl_stmt|;
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
name|random
operator|.
name|nextInt
argument_list|(
name|MAX_VALUE_LENGTH
argument_list|)
index|]
decl_stmt|;
name|random
operator|.
name|nextBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|map
operator|.
name|testPutRow
argument_list|(
name|key1
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|verifyTable
operator|.
name|add
argument_list|(
name|key1
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|verifyTable
operator|.
name|verify
argument_list|(
name|map
argument_list|)
expr_stmt|;
name|long
name|key2
init|=
name|key1
operator|+=
literal|1
decl_stmt|;
name|VectorMapJoinHashMapResult
name|hashMapResult
init|=
name|map
operator|.
name|createHashMapResult
argument_list|()
decl_stmt|;
name|JoinUtil
operator|.
name|JoinResult
name|joinResult
init|=
name|map
operator|.
name|lookup
argument_list|(
name|key2
argument_list|,
name|hashMapResult
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|joinResult
operator|==
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|NOMATCH
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|!
name|hashMapResult
operator|.
name|hasRows
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|testPutRow
argument_list|(
name|key2
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|verifyTable
operator|.
name|add
argument_list|(
name|key2
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|verifyTable
operator|.
name|verify
argument_list|(
name|map
argument_list|)
expr_stmt|;
name|long
name|key3
init|=
name|key2
operator|+=
literal|1
decl_stmt|;
name|hashMapResult
operator|=
name|map
operator|.
name|createHashMapResult
argument_list|()
expr_stmt|;
name|joinResult
operator|=
name|map
operator|.
name|lookup
argument_list|(
name|key3
argument_list|,
name|hashMapResult
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|joinResult
operator|==
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|NOMATCH
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|!
name|hashMapResult
operator|.
name|hasRows
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFullMap
parameter_list|()
throws|throws
name|Exception
block|{
name|random
operator|=
operator|new
name|Random
argument_list|(
literal|93440
argument_list|)
expr_stmt|;
comment|// Make sure the map does not expand; should be able to find space.
name|VectorMapJoinFastLongHashMap
name|map
init|=
operator|new
name|VectorMapJoinFastLongHashMap
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
name|HashTableKeyType
operator|.
name|LONG
argument_list|,
name|CAPACITY
argument_list|,
literal|1f
argument_list|,
name|WB_SIZE
argument_list|,
operator|-
literal|1
argument_list|,
name|tableDesc
argument_list|)
decl_stmt|;
name|VerifyFastLongHashMap
name|verifyTable
init|=
operator|new
name|VerifyFastLongHashMap
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
name|CAPACITY
condition|;
name|i
operator|++
control|)
block|{
name|long
name|key
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|key
operator|=
name|random
operator|.
name|nextLong
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|verifyTable
operator|.
name|contains
argument_list|(
name|key
argument_list|)
condition|)
block|{
comment|// Unique keys for this test.
break|break;
block|}
block|}
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
name|random
operator|.
name|nextInt
argument_list|(
name|MAX_VALUE_LENGTH
argument_list|)
index|]
decl_stmt|;
name|random
operator|.
name|nextBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|map
operator|.
name|testPutRow
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|verifyTable
operator|.
name|add
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
comment|// verifyTable.verify(map);
block|}
name|verifyTable
operator|.
name|verify
argument_list|(
name|map
argument_list|)
expr_stmt|;
name|long
name|anotherKey
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|anotherKey
operator|=
name|random
operator|.
name|nextLong
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|verifyTable
operator|.
name|contains
argument_list|(
name|anotherKey
argument_list|)
condition|)
block|{
comment|// Unique keys for this test.
break|break;
block|}
block|}
name|VectorMapJoinHashMapResult
name|hashMapResult
init|=
name|map
operator|.
name|createHashMapResult
argument_list|()
decl_stmt|;
name|JoinUtil
operator|.
name|JoinResult
name|joinResult
init|=
name|map
operator|.
name|lookup
argument_list|(
name|anotherKey
argument_list|,
name|hashMapResult
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|joinResult
operator|==
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|NOMATCH
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExpand
parameter_list|()
throws|throws
name|Exception
block|{
name|random
operator|=
operator|new
name|Random
argument_list|(
literal|5227
argument_list|)
expr_stmt|;
comment|// Start with capacity 1; make sure we expand on every put.
name|VectorMapJoinFastLongHashMap
name|map
init|=
operator|new
name|VectorMapJoinFastLongHashMap
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
name|HashTableKeyType
operator|.
name|LONG
argument_list|,
literal|1
argument_list|,
literal|0.0000001f
argument_list|,
name|WB_SIZE
argument_list|,
operator|-
literal|1
argument_list|,
name|tableDesc
argument_list|)
decl_stmt|;
name|VerifyFastLongHashMap
name|verifyTable
init|=
operator|new
name|VerifyFastLongHashMap
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
literal|6
condition|;
operator|++
name|i
control|)
block|{
name|long
name|key
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|key
operator|=
name|random
operator|.
name|nextLong
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|verifyTable
operator|.
name|contains
argument_list|(
name|key
argument_list|)
condition|)
block|{
comment|// Unique keys for this test.
break|break;
block|}
block|}
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
name|random
operator|.
name|nextInt
argument_list|(
name|MAX_VALUE_LENGTH
argument_list|)
index|]
decl_stmt|;
name|random
operator|.
name|nextBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|map
operator|.
name|testPutRow
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|verifyTable
operator|.
name|add
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
comment|// verifyTable.verify(map);
block|}
name|verifyTable
operator|.
name|verify
argument_list|(
name|map
argument_list|)
expr_stmt|;
comment|// assertEquals(1<< 18, map.getCapacity());
block|}
specifier|public
name|void
name|addAndVerifyMultipleKeyMultipleValue
parameter_list|(
name|int
name|keyCount
parameter_list|,
name|VectorMapJoinFastLongHashMap
name|map
parameter_list|,
name|VerifyFastLongHashMap
name|verifyTable
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
name|addAndVerifyMultipleKeyMultipleValue
argument_list|(
name|keyCount
argument_list|,
name|map
argument_list|,
name|verifyTable
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addAndVerifyMultipleKeyMultipleValue
parameter_list|(
name|int
name|keyCount
parameter_list|,
name|VectorMapJoinFastLongHashMap
name|map
parameter_list|,
name|VerifyFastLongHashMap
name|verifyTable
parameter_list|,
name|int
name|fixedValueLength
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
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
name|keyCount
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|value
decl_stmt|;
if|if
condition|(
name|fixedValueLength
operator|==
operator|-
literal|1
condition|)
block|{
name|value
operator|=
operator|new
name|byte
index|[
name|generateLargeCount
argument_list|()
operator|-
literal|1
index|]
expr_stmt|;
block|}
else|else
block|{
name|value
operator|=
operator|new
name|byte
index|[
name|fixedValueLength
index|]
expr_stmt|;
block|}
name|random
operator|.
name|nextBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
comment|// Add a new key or add a value to an existing key?
if|if
condition|(
name|random
operator|.
name|nextBoolean
argument_list|()
operator|||
name|verifyTable
operator|.
name|getCount
argument_list|()
operator|==
literal|0
condition|)
block|{
name|long
name|key
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|key
operator|=
name|random
operator|.
name|nextLong
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|verifyTable
operator|.
name|contains
argument_list|(
name|key
argument_list|)
condition|)
block|{
comment|// Unique keys for this test.
break|break;
block|}
block|}
name|map
operator|.
name|testPutRow
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|verifyTable
operator|.
name|add
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|verifyTable
operator|.
name|verify
argument_list|(
name|map
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|long
name|randomExistingKey
init|=
name|verifyTable
operator|.
name|addRandomExisting
argument_list|(
name|value
argument_list|,
name|random
argument_list|)
decl_stmt|;
name|map
operator|.
name|testPutRow
argument_list|(
name|randomExistingKey
argument_list|,
name|value
argument_list|)
expr_stmt|;
comment|// verifyTable.verify(map);
block|}
name|verifyTable
operator|.
name|verify
argument_list|(
name|map
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultipleKeysMultipleValue
parameter_list|()
throws|throws
name|Exception
block|{
name|random
operator|=
operator|new
name|Random
argument_list|(
literal|8
argument_list|)
expr_stmt|;
comment|// Use a large capacity that doesn't require expansion, yet.
name|VectorMapJoinFastLongHashMap
name|map
init|=
operator|new
name|VectorMapJoinFastLongHashMap
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
name|HashTableKeyType
operator|.
name|LONG
argument_list|,
name|LARGE_CAPACITY
argument_list|,
name|LOAD_FACTOR
argument_list|,
name|LARGE_WB_SIZE
argument_list|,
operator|-
literal|1
argument_list|,
name|tableDesc
argument_list|)
decl_stmt|;
name|VerifyFastLongHashMap
name|verifyTable
init|=
operator|new
name|VerifyFastLongHashMap
argument_list|()
decl_stmt|;
name|int
name|keyCount
init|=
literal|1000
decl_stmt|;
name|addAndVerifyMultipleKeyMultipleValue
argument_list|(
name|keyCount
argument_list|,
name|map
argument_list|,
name|verifyTable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLargeAndExpand
parameter_list|()
throws|throws
name|Exception
block|{
name|random
operator|=
operator|new
name|Random
argument_list|(
literal|20
argument_list|)
expr_stmt|;
comment|// Use a large capacity that doesn't require expansion, yet.
name|VectorMapJoinFastLongHashMap
name|map
init|=
operator|new
name|VectorMapJoinFastLongHashMap
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|,
name|HashTableKeyType
operator|.
name|LONG
argument_list|,
name|MODERATE_CAPACITY
argument_list|,
name|LOAD_FACTOR
argument_list|,
name|MODERATE_WB_SIZE
argument_list|,
operator|-
literal|1
argument_list|,
name|tableDesc
argument_list|)
decl_stmt|;
name|VerifyFastLongHashMap
name|verifyTable
init|=
operator|new
name|VerifyFastLongHashMap
argument_list|()
decl_stmt|;
name|int
name|keyCount
init|=
literal|1000
decl_stmt|;
name|addAndVerifyMultipleKeyMultipleValue
argument_list|(
name|keyCount
argument_list|,
name|map
argument_list|,
name|verifyTable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
annotation|@
name|Test
specifier|public
name|void
name|testOutOfBounds
parameter_list|()
throws|throws
name|Exception
block|{
name|random
operator|=
operator|new
name|Random
argument_list|(
literal|42662
argument_list|)
expr_stmt|;
name|int
name|HIGHEST_INT_POWER_OF_2
init|=
literal|1073741824
decl_stmt|;
name|boolean
name|error
init|=
literal|false
decl_stmt|;
try|try
block|{
comment|// The c'tor should throw the error
name|VectorMapJoinFastMultiKeyHashMap
name|map
init|=
operator|new
name|VectorMapJoinFastMultiKeyHashMap
argument_list|(
literal|false
argument_list|,
name|HIGHEST_INT_POWER_OF_2
argument_list|,
name|LOAD_FACTOR
argument_list|,
name|MODERATE_WB_SIZE
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
block|}
catch|catch
parameter_list|(
name|MapJoinMemoryExhaustionError
name|e
parameter_list|)
block|{
name|error
operator|=
literal|true
expr_stmt|;
block|}
assert|assert
name|error
assert|;
block|}
comment|/*   // Doesn't finish in a reasonable amount of time....   @Test   public void testKeyCountLimit() throws Exception {     random = new Random(28400);      // Use a large capacity that doesn't require expansion, yet.     VectorMapJoinFastLongHashMap map =         new VectorMapJoinFastLongHashMap(             false, false, HashTableKeyType.LONG, MODERATE_CAPACITY, LOAD_FACTOR, MODERATE_WB_SIZE, 10000000);      VerifyFastLongHashMap verifyTable = new VerifyFastLongHashMap();      int keyCount = Integer.MAX_VALUE;     try {       addAndVerifyMultipleKeyMultipleValue(keyCount, map, verifyTable, 1);     } catch (RuntimeException re) {       System.out.println(re.toString());       assertTrue(re.toString().startsWith("Vector MapJoin Long Hash Table cannot grow any more"));     }   }   */
block|}
end_class

end_unit

