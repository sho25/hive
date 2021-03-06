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
name|spark
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
name|assertTrue
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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|concurrent
operator|.
name|LinkedBlockingQueue
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
name|io
operator|.
name|HiveKey
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
name|io
operator|.
name|BytesWritable
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
name|scala
operator|.
name|Tuple2
import|;
end_import

begin_import
import|import
name|com
operator|.
name|clearspring
operator|.
name|analytics
operator|.
name|util
operator|.
name|Preconditions
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|,
literal|"rawtypes"
block|}
argument_list|)
specifier|public
class|class
name|TestHiveKVResultCache
block|{
annotation|@
name|Test
specifier|public
name|void
name|testSimple
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Create KV result cache object, add one (k,v) pair and retrieve them.
name|HiveKVResultCache
name|cache
init|=
operator|new
name|HiveKVResultCache
argument_list|()
decl_stmt|;
name|HiveKey
name|key
init|=
operator|new
name|HiveKey
argument_list|(
literal|"key"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"key"
operator|.
name|hashCode
argument_list|()
argument_list|)
decl_stmt|;
name|BytesWritable
name|value
init|=
operator|new
name|BytesWritable
argument_list|(
literal|"value"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|cache
operator|.
name|add
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"KV result cache should have at least one element"
argument_list|,
name|cache
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|row
init|=
name|cache
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Incorrect key"
argument_list|,
name|row
operator|.
name|_1
argument_list|()
operator|.
name|equals
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Incorrect value"
argument_list|,
name|row
operator|.
name|_2
argument_list|()
operator|.
name|equals
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Cache shouldn't have more records"
argument_list|,
operator|!
name|cache
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSpilling
parameter_list|()
throws|throws
name|Exception
block|{
name|HiveKVResultCache
name|cache
init|=
operator|new
name|HiveKVResultCache
argument_list|()
decl_stmt|;
specifier|final
name|int
name|recordCount
init|=
name|HiveKVResultCache
operator|.
name|IN_MEMORY_NUM_ROWS
operator|*
literal|3
decl_stmt|;
comment|// Test using the same cache where first n rows are inserted then cache is cleared.
comment|// Next reuse the same cache and insert another m rows and verify the cache stores correctly.
comment|// This simulates reusing the same cache over and over again.
name|testSpillingHelper
argument_list|(
name|cache
argument_list|,
name|recordCount
argument_list|)
expr_stmt|;
name|testSpillingHelper
argument_list|(
name|cache
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|testSpillingHelper
argument_list|(
name|cache
argument_list|,
name|recordCount
argument_list|)
expr_stmt|;
block|}
comment|/** Helper method which inserts numRecords and retrieves them from cache and verifies */
specifier|private
name|void
name|testSpillingHelper
parameter_list|(
name|HiveKVResultCache
name|cache
parameter_list|,
name|int
name|numRecords
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
name|numRecords
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
literal|"key_"
operator|+
name|i
decl_stmt|;
name|String
name|value
init|=
literal|"value_"
operator|+
name|i
decl_stmt|;
name|cache
operator|.
name|add
argument_list|(
operator|new
name|HiveKey
argument_list|(
name|key
operator|.
name|getBytes
argument_list|()
argument_list|,
name|key
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|,
operator|new
name|BytesWritable
argument_list|(
name|value
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|int
name|recordsSeen
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|cache
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|key
init|=
literal|"key_"
operator|+
name|recordsSeen
decl_stmt|;
name|String
name|value
init|=
literal|"value_"
operator|+
name|recordsSeen
decl_stmt|;
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|row
init|=
name|cache
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Unexpected key at position: "
operator|+
name|recordsSeen
argument_list|,
operator|new
name|String
argument_list|(
name|row
operator|.
name|_1
argument_list|()
operator|.
name|getBytes
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Unexpected value at position: "
operator|+
name|recordsSeen
argument_list|,
operator|new
name|String
argument_list|(
name|row
operator|.
name|_2
argument_list|()
operator|.
name|getBytes
argument_list|()
argument_list|)
operator|.
name|equals
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|recordsSeen
operator|++
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"Retrieved record count doesn't match inserted record count"
argument_list|,
name|numRecords
operator|==
name|recordsSeen
argument_list|)
expr_stmt|;
name|cache
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testResultList
parameter_list|()
throws|throws
name|Exception
block|{
name|scanAndVerify
argument_list|(
literal|10000
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
name|scanAndVerify
argument_list|(
literal|10000
argument_list|,
literal|511
argument_list|,
literal|0
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
name|scanAndVerify
argument_list|(
literal|10000
argument_list|,
literal|511
operator|*
literal|2
argument_list|,
literal|0
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
name|scanAndVerify
argument_list|(
literal|10000
argument_list|,
literal|511
argument_list|,
literal|10
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
name|scanAndVerify
argument_list|(
literal|10000
argument_list|,
literal|511
operator|*
literal|2
argument_list|,
literal|10
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
name|scanAndVerify
argument_list|(
literal|10000
argument_list|,
literal|512
argument_list|,
literal|0
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
name|scanAndVerify
argument_list|(
literal|10000
argument_list|,
literal|512
operator|*
literal|2
argument_list|,
literal|0
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
name|scanAndVerify
argument_list|(
literal|10000
argument_list|,
literal|512
argument_list|,
literal|3
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
name|scanAndVerify
argument_list|(
literal|10000
argument_list|,
literal|512
operator|*
literal|6
argument_list|,
literal|10
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
name|scanAndVerify
argument_list|(
literal|10000
argument_list|,
literal|512
operator|*
literal|7
argument_list|,
literal|5
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
name|scanAndVerify
argument_list|(
literal|10000
argument_list|,
literal|512
operator|*
literal|9
argument_list|,
literal|19
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
name|scanAndVerify
argument_list|(
literal|10000
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
name|scanAndVerify
argument_list|(
literal|10000
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|scanAndVerify
parameter_list|(
name|long
name|rows
parameter_list|,
name|int
name|threshold
parameter_list|,
name|int
name|separate
parameter_list|,
name|String
name|prefix1
parameter_list|,
name|String
name|prefix2
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
name|output
init|=
operator|new
name|ArrayList
argument_list|<
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
argument_list|(
operator|(
name|int
operator|)
name|rows
argument_list|)
decl_stmt|;
name|scanResultList
argument_list|(
name|rows
argument_list|,
name|threshold
argument_list|,
name|separate
argument_list|,
name|output
argument_list|,
name|prefix1
argument_list|,
name|prefix2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|rows
argument_list|,
name|output
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|primaryRows
init|=
name|rows
operator|*
operator|(
literal|100
operator|-
name|separate
operator|)
operator|/
literal|100
decl_stmt|;
name|long
name|separateRows
init|=
name|rows
operator|-
name|primaryRows
decl_stmt|;
name|HashSet
argument_list|<
name|Long
argument_list|>
name|primaryRowKeys
init|=
operator|new
name|HashSet
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|HashSet
argument_list|<
name|Long
argument_list|>
name|separateRowKeys
init|=
operator|new
name|HashSet
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|item
range|:
name|output
control|)
block|{
name|String
name|key
init|=
name|bytesWritableToString
argument_list|(
name|item
operator|.
name|_1
argument_list|)
decl_stmt|;
name|String
name|value
init|=
name|bytesWritableToString
argument_list|(
name|item
operator|.
name|_2
argument_list|)
decl_stmt|;
name|String
name|prefix
init|=
name|key
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|key
operator|.
name|indexOf
argument_list|(
literal|'_'
argument_list|)
argument_list|)
decl_stmt|;
name|Long
name|id
init|=
name|Long
operator|.
name|valueOf
argument_list|(
name|key
operator|.
name|substring
argument_list|(
literal|5
operator|+
name|prefix
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|prefix
operator|.
name|equals
argument_list|(
name|prefix1
argument_list|)
condition|)
block|{
name|assertTrue
argument_list|(
name|id
operator|>=
literal|0
operator|&&
name|id
operator|<
name|primaryRows
argument_list|)
expr_stmt|;
name|primaryRowKeys
operator|.
name|add
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|prefix2
argument_list|,
name|prefix
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|id
operator|>=
literal|0
operator|&&
name|id
operator|<
name|separateRows
argument_list|)
expr_stmt|;
name|separateRowKeys
operator|.
name|add
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|prefix
operator|+
literal|"_value_"
operator|+
name|id
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|separateRows
argument_list|,
name|separateRowKeys
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|primaryRows
argument_list|,
name|primaryRowKeys
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Convert a BytesWritable to a string.    * Don't use {@link BytesWritable#copyBytes()}    * so as to be compatible with hadoop 1    */
specifier|private
specifier|static
name|String
name|bytesWritableToString
parameter_list|(
name|BytesWritable
name|bw
parameter_list|)
block|{
name|int
name|size
init|=
name|bw
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|size
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|bw
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bytes
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
return|return
operator|new
name|String
argument_list|(
name|bytes
argument_list|)
return|;
block|}
specifier|private
specifier|static
class|class
name|MyHiveFunctionResultList
extends|extends
name|HiveBaseFunctionResultList
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|1L
decl_stmt|;
comment|// Total rows to emit during the whole iteration,
comment|// excluding the rows emitted by the separate thread.
specifier|private
name|long
name|primaryRows
decl_stmt|;
comment|// Batch of rows to emit per processNextRecord() call.
specifier|private
name|int
name|thresholdRows
decl_stmt|;
comment|// Rows to be emitted with a separate thread per processNextRecord() call.
specifier|private
name|long
name|separateRows
decl_stmt|;
comment|// Thread to generate the separate rows beside the normal thread.
specifier|private
name|Thread
name|separateRowGenerator
decl_stmt|;
comment|// Counter for rows emitted
specifier|private
name|long
name|rowsEmitted
decl_stmt|;
specifier|private
name|long
name|separateRowsEmitted
decl_stmt|;
comment|// Prefix for primary row keys
specifier|private
name|String
name|prefix1
decl_stmt|;
comment|// Prefix for separate row keys
specifier|private
name|String
name|prefix2
decl_stmt|;
comment|// A queue to notify separateRowGenerator to generate the next batch of rows.
specifier|private
name|LinkedBlockingQueue
argument_list|<
name|Boolean
argument_list|>
name|queue
decl_stmt|;
name|MyHiveFunctionResultList
parameter_list|(
name|Iterator
name|inputIterator
parameter_list|)
block|{
name|super
argument_list|(
name|inputIterator
argument_list|)
expr_stmt|;
block|}
name|void
name|init
parameter_list|(
name|long
name|rows
parameter_list|,
name|int
name|threshold
parameter_list|,
name|int
name|separate
parameter_list|,
name|String
name|p1
parameter_list|,
name|String
name|p2
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
operator|(
name|threshold
operator|>
literal|0
operator|||
name|separate
operator|==
literal|0
operator|)
operator|&&
name|separate
argument_list|<
literal|100
operator|&&
name|separate
operator|>=
literal|0
operator|&&
name|rows
argument_list|>
literal|0
argument_list|)
expr_stmt|;
name|primaryRows
operator|=
name|rows
operator|*
operator|(
literal|100
operator|-
name|separate
operator|)
operator|/
literal|100
expr_stmt|;
name|separateRows
operator|=
name|rows
operator|-
name|primaryRows
expr_stmt|;
name|thresholdRows
operator|=
name|threshold
expr_stmt|;
name|prefix1
operator|=
name|p1
expr_stmt|;
name|prefix2
operator|=
name|p2
expr_stmt|;
if|if
condition|(
name|separateRows
operator|>
literal|0
condition|)
block|{
name|separateRowGenerator
operator|=
operator|new
name|Thread
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|long
name|separateBatchSize
init|=
name|thresholdRows
operator|*
name|separateRows
operator|/
name|primaryRows
decl_stmt|;
while|while
condition|(
operator|!
name|queue
operator|.
name|take
argument_list|()
operator|.
name|booleanValue
argument_list|()
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
name|separateBatchSize
condition|;
name|i
operator|++
control|)
block|{
name|collect
argument_list|(
name|prefix2
argument_list|,
name|separateRowsEmitted
operator|++
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
for|for
control|(
init|;
name|separateRowsEmitted
operator|<
name|separateRows
condition|;
control|)
block|{
name|collect
argument_list|(
name|prefix2
argument_list|,
name|separateRowsEmitted
operator|++
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|queue
operator|=
operator|new
name|LinkedBlockingQueue
argument_list|<
name|Boolean
argument_list|>
argument_list|()
expr_stmt|;
name|separateRowGenerator
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|collect
parameter_list|(
name|String
name|prefix
parameter_list|,
name|long
name|id
parameter_list|)
block|{
name|String
name|k
init|=
name|prefix
operator|+
literal|"_key_"
operator|+
name|id
decl_stmt|;
name|String
name|v
init|=
name|prefix
operator|+
literal|"_value_"
operator|+
name|id
decl_stmt|;
name|HiveKey
name|key
init|=
operator|new
name|HiveKey
argument_list|(
name|k
operator|.
name|getBytes
argument_list|()
argument_list|,
name|k
operator|.
name|hashCode
argument_list|()
argument_list|)
decl_stmt|;
name|BytesWritable
name|value
init|=
operator|new
name|BytesWritable
argument_list|(
name|v
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|collect
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processNextRecord
parameter_list|(
name|Object
name|inputRecord
parameter_list|)
throws|throws
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
name|thresholdRows
condition|;
name|i
operator|++
control|)
block|{
name|collect
argument_list|(
name|prefix1
argument_list|,
name|rowsEmitted
operator|++
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|separateRowGenerator
operator|!=
literal|null
condition|)
block|{
name|queue
operator|.
name|add
argument_list|(
name|Boolean
operator|.
name|FALSE
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|processingDone
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|closeRecordProcessor
parameter_list|()
block|{
for|for
control|(
init|;
name|rowsEmitted
operator|<
name|primaryRows
condition|;
control|)
block|{
name|collect
argument_list|(
name|prefix1
argument_list|,
name|rowsEmitted
operator|++
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|separateRowGenerator
operator|!=
literal|null
condition|)
block|{
name|queue
operator|.
name|add
argument_list|(
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
try|try
block|{
name|separateRowGenerator
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
specifier|static
name|long
name|scanResultList
parameter_list|(
name|long
name|rows
parameter_list|,
name|int
name|threshold
parameter_list|,
name|int
name|separate
parameter_list|,
name|List
argument_list|<
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
name|output
parameter_list|,
name|String
name|prefix1
parameter_list|,
name|String
name|prefix2
parameter_list|)
block|{
specifier|final
name|long
name|iteratorCount
init|=
name|threshold
operator|==
literal|0
condition|?
literal|1
else|:
name|rows
operator|*
operator|(
literal|100
operator|-
name|separate
operator|)
operator|/
literal|100
operator|/
name|threshold
decl_stmt|;
name|MyHiveFunctionResultList
name|resultList
init|=
operator|new
name|MyHiveFunctionResultList
argument_list|(
operator|new
name|Iterator
argument_list|()
block|{
comment|// Input record iterator, not used
specifier|private
name|int
name|i
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|i
operator|++
operator|<
name|iteratorCount
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|next
parameter_list|()
block|{
return|return
name|Integer
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|remove
parameter_list|()
block|{       }
block|}
argument_list|)
decl_stmt|;
name|resultList
operator|.
name|init
argument_list|(
name|rows
argument_list|,
name|threshold
argument_list|,
name|separate
argument_list|,
name|prefix1
argument_list|,
name|prefix2
argument_list|)
expr_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
name|resultList
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Object
name|item
init|=
name|resultList
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|output
operator|!=
literal|null
condition|)
block|{
name|output
operator|.
name|add
argument_list|(
operator|(
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
operator|)
name|item
argument_list|)
expr_stmt|;
block|}
block|}
name|long
name|endTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
return|return
name|endTime
operator|-
name|startTime
return|;
block|}
specifier|private
specifier|static
name|long
index|[]
name|scanResultList
parameter_list|(
name|long
name|rows
parameter_list|,
name|int
name|threshold
parameter_list|,
name|int
name|extra
parameter_list|)
block|{
comment|// 1. Simulate emitting all records in closeRecordProcessor().
name|long
name|t1
init|=
name|scanResultList
argument_list|(
name|rows
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|,
literal|"a"
argument_list|,
literal|"b"
argument_list|)
decl_stmt|;
comment|// 2. Simulate emitting records in processNextRecord() with small memory usage limit.
name|long
name|t2
init|=
name|scanResultList
argument_list|(
name|rows
argument_list|,
name|threshold
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|,
literal|"c"
argument_list|,
literal|"d"
argument_list|)
decl_stmt|;
comment|// 3. Simulate emitting records in processNextRecord() with large memory usage limit.
name|long
name|t3
init|=
name|scanResultList
argument_list|(
name|rows
argument_list|,
name|threshold
operator|*
literal|10
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|,
literal|"e"
argument_list|,
literal|"f"
argument_list|)
decl_stmt|;
comment|// 4. Same as 2. Also emit extra records from a separate thread.
name|long
name|t4
init|=
name|scanResultList
argument_list|(
name|rows
argument_list|,
name|threshold
argument_list|,
name|extra
argument_list|,
literal|null
argument_list|,
literal|"g"
argument_list|,
literal|"h"
argument_list|)
decl_stmt|;
comment|// 5. Same as 3. Also emit extra records from a separate thread.
name|long
name|t5
init|=
name|scanResultList
argument_list|(
name|rows
argument_list|,
name|threshold
operator|*
literal|10
argument_list|,
name|extra
argument_list|,
literal|null
argument_list|,
literal|"i"
argument_list|,
literal|"j"
argument_list|)
decl_stmt|;
return|return
operator|new
name|long
index|[]
block|{
name|t1
block|,
name|t2
block|,
name|t3
block|,
name|t4
block|,
name|t5
block|}
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|long
name|rows
init|=
literal|1000000
decl_stmt|;
comment|// total rows to generate
name|int
name|threshold
init|=
literal|512
decl_stmt|;
comment|// # of rows to cache at most
name|int
name|extra
init|=
literal|5
decl_stmt|;
comment|// percentile of extra rows to generate by a different thread
if|if
condition|(
name|args
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|rows
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|args
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|threshold
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|args
operator|.
name|length
operator|>
literal|2
condition|)
block|{
name|extra
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
block|}
comment|// Warm up couple times
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|scanResultList
argument_list|(
name|rows
argument_list|,
name|threshold
argument_list|,
name|extra
argument_list|)
expr_stmt|;
block|}
name|int
name|count
init|=
literal|5
decl_stmt|;
name|long
index|[]
name|t
init|=
operator|new
name|long
index|[
name|count
index|]
decl_stmt|;
comment|// Run count times and get average
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
name|long
index|[]
name|tmp
init|=
name|scanResultList
argument_list|(
name|rows
argument_list|,
name|threshold
argument_list|,
name|extra
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|count
condition|;
name|k
operator|++
control|)
block|{
name|t
index|[
name|k
index|]
operator|+=
name|tmp
index|[
name|k
index|]
expr_stmt|;
block|}
block|}
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
name|t
index|[
name|i
index|]
operator|/=
name|count
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|t
index|[
literal|0
index|]
operator|+
literal|"\t"
operator|+
name|t
index|[
literal|1
index|]
operator|+
literal|"\t"
operator|+
name|t
index|[
literal|2
index|]
operator|+
literal|"\t"
operator|+
name|t
index|[
literal|3
index|]
operator|+
literal|"\t"
operator|+
name|t
index|[
literal|4
index|]
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

