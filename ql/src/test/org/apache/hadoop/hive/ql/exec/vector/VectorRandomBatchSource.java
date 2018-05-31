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
package|;
end_package

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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|BitSet
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
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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

begin_comment
comment|/**  * Generate random batch source from a random Object[] row source (VectorRandomRowSource).  */
end_comment

begin_class
specifier|public
class|class
name|VectorRandomBatchSource
block|{
comment|// Divide up rows array into different sized batches.
comment|// Modify the rows array for isRepeating / NULL patterns.
comment|// Provide iterator that will fill up a VRB with the divided up rows.
specifier|private
specifier|final
name|VectorRandomRowSource
name|vectorRandomRowSource
decl_stmt|;
specifier|private
specifier|final
name|Object
index|[]
index|[]
name|randomRows
decl_stmt|;
specifier|private
specifier|final
name|int
name|rowCount
decl_stmt|;
specifier|private
specifier|final
name|int
name|columnCount
decl_stmt|;
specifier|private
specifier|final
name|VectorBatchPatterns
name|vectorBatchPatterns
decl_stmt|;
specifier|private
name|VectorAssignRow
name|vectorAssignRow
decl_stmt|;
specifier|private
name|int
name|nextRowIndex
decl_stmt|;
specifier|private
name|int
name|batchCount
decl_stmt|;
specifier|private
name|VectorRandomBatchSource
parameter_list|(
name|VectorRandomRowSource
name|vectorRandomRowSource
parameter_list|,
name|Object
index|[]
index|[]
name|randomRows
parameter_list|,
name|VectorBatchPatterns
name|vectorBatchPatterns
parameter_list|,
name|VectorAssignRow
name|vectorAssignRow
parameter_list|)
block|{
name|this
operator|.
name|vectorRandomRowSource
operator|=
name|vectorRandomRowSource
expr_stmt|;
name|this
operator|.
name|randomRows
operator|=
name|randomRows
expr_stmt|;
name|rowCount
operator|=
name|randomRows
operator|.
name|length
expr_stmt|;
name|Object
index|[]
name|firstRow
init|=
name|randomRows
index|[
literal|0
index|]
decl_stmt|;
name|columnCount
operator|=
name|firstRow
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|vectorBatchPatterns
operator|=
name|vectorBatchPatterns
expr_stmt|;
name|this
operator|.
name|vectorAssignRow
operator|=
name|vectorAssignRow
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|VectorRandomBatchParameters
block|{   }
specifier|private
specifier|static
class|class
name|VectorBatchPatterns
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|VectorBatchPattern
argument_list|>
name|vectorBatchPatternList
decl_stmt|;
name|VectorBatchPatterns
parameter_list|(
name|List
argument_list|<
name|VectorBatchPattern
argument_list|>
name|vectorBatchPatternList
parameter_list|)
block|{
name|this
operator|.
name|vectorBatchPatternList
operator|=
name|vectorBatchPatternList
expr_stmt|;
block|}
name|List
argument_list|<
name|VectorBatchPattern
argument_list|>
name|getTectorBatchPatternList
parameter_list|()
block|{
return|return
name|vectorBatchPatternList
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|VectorBatchPattern
block|{
specifier|final
name|int
name|batchSize
decl_stmt|;
specifier|final
name|BitSet
name|bitSet
decl_stmt|;
specifier|final
name|int
index|[]
name|selected
decl_stmt|;
specifier|private
name|VectorBatchPattern
parameter_list|(
name|Random
name|random
parameter_list|,
name|int
name|batchSize
parameter_list|,
name|BitSet
name|bitSet
parameter_list|,
name|boolean
name|asSelected
parameter_list|)
block|{
name|this
operator|.
name|batchSize
operator|=
name|batchSize
expr_stmt|;
name|this
operator|.
name|bitSet
operator|=
name|bitSet
expr_stmt|;
if|if
condition|(
name|asSelected
condition|)
block|{
name|selected
operator|=
name|randomSelection
argument_list|(
name|random
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|selected
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|private
name|int
index|[]
name|randomSelection
parameter_list|(
name|Random
name|random
parameter_list|,
name|int
name|batchSize
parameter_list|)
block|{
comment|// Random batchSize unique ordered integers of 1024 (VectorizedRowBatch.DEFAULT_SIZE) indices.
comment|// This could be smarter...
name|Set
argument_list|<
name|Integer
argument_list|>
name|selectedSet
init|=
operator|new
name|TreeSet
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|currentCount
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
specifier|final
name|int
name|candidateIndex
init|=
name|random
operator|.
name|nextInt
argument_list|(
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|selectedSet
operator|.
name|contains
argument_list|(
name|candidateIndex
argument_list|)
condition|)
block|{
name|selectedSet
operator|.
name|add
argument_list|(
name|candidateIndex
argument_list|)
expr_stmt|;
if|if
condition|(
operator|++
name|currentCount
operator|==
name|batchSize
condition|)
block|{
name|Integer
index|[]
name|integerArray
init|=
name|selectedSet
operator|.
name|toArray
argument_list|(
operator|new
name|Integer
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|int
index|[]
name|result
init|=
operator|new
name|int
index|[
name|batchSize
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
name|batchSize
condition|;
name|i
operator|++
control|)
block|{
name|result
index|[
name|i
index|]
operator|=
name|integerArray
index|[
name|i
index|]
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
block|}
block|}
specifier|public
specifier|static
name|VectorBatchPattern
name|createRegularBatch
parameter_list|(
name|int
name|batchSize
parameter_list|)
block|{
return|return
operator|new
name|VectorBatchPattern
argument_list|(
literal|null
argument_list|,
name|batchSize
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|VectorBatchPattern
name|createRegularBatch
parameter_list|(
name|Random
name|random
parameter_list|,
name|int
name|batchSize
parameter_list|,
name|boolean
name|asSelected
parameter_list|)
block|{
return|return
operator|new
name|VectorBatchPattern
argument_list|(
name|random
argument_list|,
name|batchSize
argument_list|,
literal|null
argument_list|,
name|asSelected
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|VectorBatchPattern
name|createRepeatedBatch
parameter_list|(
name|Random
name|random
parameter_list|,
name|int
name|batchSize
parameter_list|,
name|BitSet
name|bitSet
parameter_list|,
name|boolean
name|asSelected
parameter_list|)
block|{
return|return
operator|new
name|VectorBatchPattern
argument_list|(
name|random
argument_list|,
name|batchSize
argument_list|,
name|bitSet
argument_list|,
name|asSelected
argument_list|)
return|;
block|}
specifier|public
name|int
name|getBatchSize
parameter_list|()
block|{
return|return
name|batchSize
return|;
block|}
specifier|public
name|BitSet
name|getBitSet
parameter_list|()
block|{
return|return
name|bitSet
return|;
block|}
specifier|public
name|int
index|[]
name|getSelected
parameter_list|()
block|{
return|return
name|selected
return|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|batchSizeString
init|=
literal|"batchSize "
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|batchSize
argument_list|)
decl_stmt|;
if|if
condition|(
name|bitSet
operator|!=
literal|null
condition|)
block|{
name|long
name|bitMask
init|=
name|bitSet
operator|.
name|toLongArray
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|batchSizeString
operator|+=
literal|" repeating 0x"
operator|+
name|Long
operator|.
name|toHexString
argument_list|(
name|bitMask
argument_list|)
expr_stmt|;
block|}
name|boolean
name|selectedInUse
init|=
operator|(
name|selected
operator|!=
literal|null
operator|)
decl_stmt|;
name|batchSizeString
operator|+=
literal|" selectedInUse "
operator|+
name|selectedInUse
expr_stmt|;
if|if
condition|(
name|selectedInUse
condition|)
block|{
name|batchSizeString
operator|+=
literal|" selected "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|selected
argument_list|)
expr_stmt|;
block|}
return|return
name|batchSizeString
return|;
block|}
block|}
specifier|private
specifier|static
name|VectorBatchPatterns
name|chooseBatchPatterns
parameter_list|(
name|Random
name|random
parameter_list|,
name|VectorRandomRowSource
name|vectorRandomRowSource
parameter_list|,
name|Object
index|[]
index|[]
name|randomRows
parameter_list|)
block|{
name|List
argument_list|<
name|VectorBatchPattern
argument_list|>
name|vectorBatchPatternList
init|=
operator|new
name|ArrayList
argument_list|<
name|VectorBatchPattern
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|int
name|rowCount
init|=
name|randomRows
operator|.
name|length
decl_stmt|;
name|int
name|rowIndex
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|rowCount
operator|>
literal|0
condition|)
block|{
specifier|final
name|int
name|columnCount
init|=
name|randomRows
index|[
literal|0
index|]
operator|.
name|length
decl_stmt|;
comment|// Choose first up to a full batch with no selection.
specifier|final
name|int
name|regularBatchSize
init|=
name|Math
operator|.
name|min
argument_list|(
name|rowCount
operator|-
name|rowIndex
argument_list|,
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
decl_stmt|;
name|vectorBatchPatternList
operator|.
name|add
argument_list|(
name|VectorBatchPattern
operator|.
name|createRegularBatch
argument_list|(
name|regularBatchSize
argument_list|)
argument_list|)
expr_stmt|;
name|rowIndex
operator|+=
name|regularBatchSize
expr_stmt|;
comment|// Have a non-NULL value on hand.
name|Object
index|[]
name|nonNullRow
init|=
operator|new
name|Object
index|[
name|columnCount
index|]
decl_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|columnCount
condition|;
name|c
operator|++
control|)
block|{
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|rowCount
condition|;
name|r
operator|++
control|)
block|{
name|Object
name|object
init|=
name|randomRows
index|[
name|r
index|]
index|[
name|c
index|]
decl_stmt|;
if|if
condition|(
name|object
operator|!=
literal|null
condition|)
block|{
name|nonNullRow
index|[
name|c
index|]
operator|=
name|object
expr_stmt|;
break|break;
block|}
block|}
block|}
name|int
name|columnPermutationLimit
init|=
name|Math
operator|.
name|min
argument_list|(
name|columnCount
argument_list|,
name|Long
operator|.
name|SIZE
argument_list|)
decl_stmt|;
name|boolean
name|asSelected
init|=
literal|false
decl_stmt|;
comment|/*        * Do a round each as physical with no row selection and logical with row selection.        */
while|while
condition|(
literal|true
condition|)
block|{
comment|// Repeated NULL permutations.
name|long
name|columnPermutation
init|=
literal|1
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|columnPermutation
operator|>
name|columnPermutationLimit
condition|)
block|{
break|break;
block|}
specifier|final
name|int
name|maximumRowCount
init|=
name|Math
operator|.
name|min
argument_list|(
name|rowCount
operator|-
name|rowIndex
argument_list|,
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
decl_stmt|;
if|if
condition|(
name|maximumRowCount
operator|==
literal|0
condition|)
block|{
break|break;
block|}
name|int
name|randomRowCount
init|=
literal|1
operator|+
name|random
operator|.
name|nextInt
argument_list|(
name|maximumRowCount
argument_list|)
decl_stmt|;
specifier|final
name|int
name|rowLimit
init|=
name|rowIndex
operator|+
name|randomRowCount
decl_stmt|;
name|BitSet
name|bitSet
init|=
name|BitSet
operator|.
name|valueOf
argument_list|(
operator|new
name|long
index|[]
block|{
name|columnPermutation
block|}
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|columnNum
init|=
name|bitSet
operator|.
name|nextSetBit
argument_list|(
literal|0
argument_list|)
init|;
name|columnNum
operator|>=
literal|0
condition|;
name|columnNum
operator|=
name|bitSet
operator|.
name|nextSetBit
argument_list|(
name|columnNum
operator|+
literal|1
argument_list|)
control|)
block|{
comment|// Repeated NULL fill down column.
for|for
control|(
name|int
name|r
init|=
name|rowIndex
init|;
name|r
operator|<
name|rowLimit
condition|;
name|r
operator|++
control|)
block|{
name|randomRows
index|[
name|r
index|]
index|[
name|columnNum
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
name|vectorBatchPatternList
operator|.
name|add
argument_list|(
name|VectorBatchPattern
operator|.
name|createRepeatedBatch
argument_list|(
name|random
argument_list|,
name|randomRowCount
argument_list|,
name|bitSet
argument_list|,
name|asSelected
argument_list|)
argument_list|)
expr_stmt|;
name|columnPermutation
operator|++
expr_stmt|;
name|rowIndex
operator|=
name|rowLimit
expr_stmt|;
block|}
comment|// Repeated non-NULL permutations.
name|columnPermutation
operator|=
literal|1
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|columnPermutation
operator|>
name|columnPermutationLimit
condition|)
block|{
break|break;
block|}
specifier|final
name|int
name|maximumRowCount
init|=
name|Math
operator|.
name|min
argument_list|(
name|rowCount
operator|-
name|rowIndex
argument_list|,
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
decl_stmt|;
if|if
condition|(
name|maximumRowCount
operator|==
literal|0
condition|)
block|{
break|break;
block|}
name|int
name|randomRowCount
init|=
literal|1
operator|+
name|random
operator|.
name|nextInt
argument_list|(
name|maximumRowCount
argument_list|)
decl_stmt|;
specifier|final
name|int
name|rowLimit
init|=
name|rowIndex
operator|+
name|randomRowCount
decl_stmt|;
name|BitSet
name|bitSet
init|=
name|BitSet
operator|.
name|valueOf
argument_list|(
operator|new
name|long
index|[]
block|{
name|columnPermutation
block|}
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|columnNum
init|=
name|bitSet
operator|.
name|nextSetBit
argument_list|(
literal|0
argument_list|)
init|;
name|columnNum
operator|>=
literal|0
condition|;
name|columnNum
operator|=
name|bitSet
operator|.
name|nextSetBit
argument_list|(
name|columnNum
operator|+
literal|1
argument_list|)
control|)
block|{
comment|// Repeated non-NULL fill down column.
name|Object
name|repeatedObject
init|=
name|randomRows
index|[
name|rowIndex
index|]
index|[
name|columnNum
index|]
decl_stmt|;
if|if
condition|(
name|repeatedObject
operator|==
literal|null
condition|)
block|{
name|repeatedObject
operator|=
name|nonNullRow
index|[
name|columnNum
index|]
expr_stmt|;
block|}
for|for
control|(
name|int
name|r
init|=
name|rowIndex
init|;
name|r
operator|<
name|rowLimit
condition|;
name|r
operator|++
control|)
block|{
name|randomRows
index|[
name|r
index|]
index|[
name|columnNum
index|]
operator|=
name|repeatedObject
expr_stmt|;
block|}
block|}
name|vectorBatchPatternList
operator|.
name|add
argument_list|(
name|VectorBatchPattern
operator|.
name|createRepeatedBatch
argument_list|(
name|random
argument_list|,
name|randomRowCount
argument_list|,
name|bitSet
argument_list|,
name|asSelected
argument_list|)
argument_list|)
expr_stmt|;
name|columnPermutation
operator|++
expr_stmt|;
name|rowIndex
operator|=
name|rowLimit
expr_stmt|;
block|}
if|if
condition|(
name|asSelected
condition|)
block|{
break|break;
block|}
name|asSelected
operator|=
literal|true
expr_stmt|;
block|}
comment|// Remaining batches.
while|while
condition|(
literal|true
condition|)
block|{
specifier|final
name|int
name|maximumRowCount
init|=
name|Math
operator|.
name|min
argument_list|(
name|rowCount
operator|-
name|rowIndex
argument_list|,
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
argument_list|)
decl_stmt|;
if|if
condition|(
name|maximumRowCount
operator|==
literal|0
condition|)
block|{
break|break;
block|}
name|int
name|randomRowCount
init|=
literal|1
operator|+
name|random
operator|.
name|nextInt
argument_list|(
name|maximumRowCount
argument_list|)
decl_stmt|;
name|asSelected
operator|=
name|random
operator|.
name|nextBoolean
argument_list|()
expr_stmt|;
name|vectorBatchPatternList
operator|.
name|add
argument_list|(
name|VectorBatchPattern
operator|.
name|createRegularBatch
argument_list|(
name|random
argument_list|,
name|randomRowCount
argument_list|,
name|asSelected
argument_list|)
argument_list|)
expr_stmt|;
name|rowIndex
operator|+=
name|randomRowCount
expr_stmt|;
block|}
block|}
comment|// System.out.println("*DEBUG* vectorBatchPatternList" + vectorBatchPatternList.toString());
return|return
operator|new
name|VectorBatchPatterns
argument_list|(
name|vectorBatchPatternList
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|VectorRandomBatchSource
name|createInterestingBatches
parameter_list|(
name|Random
name|random
parameter_list|,
name|VectorRandomRowSource
name|vectorRandomRowSource
parameter_list|,
name|Object
index|[]
index|[]
name|randomRows
parameter_list|,
name|VectorRandomBatchParameters
name|vectorRandomBatchParameters
parameter_list|)
throws|throws
name|HiveException
block|{
name|VectorAssignRow
name|vectorAssignRow
init|=
operator|new
name|VectorAssignRow
argument_list|()
decl_stmt|;
name|vectorAssignRow
operator|.
name|init
argument_list|(
name|vectorRandomRowSource
operator|.
name|typeNames
argument_list|()
argument_list|)
expr_stmt|;
name|VectorBatchPatterns
name|vectorBatchPatterns
init|=
name|chooseBatchPatterns
argument_list|(
name|random
argument_list|,
name|vectorRandomRowSource
argument_list|,
name|randomRows
argument_list|)
decl_stmt|;
return|return
operator|new
name|VectorRandomBatchSource
argument_list|(
name|vectorRandomRowSource
argument_list|,
name|randomRows
argument_list|,
name|vectorBatchPatterns
argument_list|,
name|vectorAssignRow
argument_list|)
return|;
block|}
specifier|public
name|VectorRandomRowSource
name|getRowSource
parameter_list|()
block|{
return|return
name|vectorRandomRowSource
return|;
block|}
specifier|public
name|Object
index|[]
index|[]
name|getRandomRows
parameter_list|()
block|{
return|return
name|randomRows
return|;
block|}
specifier|public
name|void
name|resetBatchIteration
parameter_list|()
block|{
name|nextRowIndex
operator|=
literal|0
expr_stmt|;
name|batchCount
operator|=
literal|0
expr_stmt|;
block|}
specifier|public
name|int
name|getBatchCount
parameter_list|()
block|{
return|return
name|batchCount
return|;
block|}
specifier|public
name|int
name|getRowCount
parameter_list|()
block|{
return|return
name|rowCount
return|;
block|}
comment|/*    * Patterns of isRepeating columns    * For boolean: tri-state: null, 0, 1    * For others: null, some-value    * noNulls: sometimes false and there are no NULLs.    * Random selectedInUse, too.    */
specifier|public
name|boolean
name|fillNextBatch
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
block|{
if|if
condition|(
name|nextRowIndex
operator|>=
name|rowCount
condition|)
block|{
return|return
literal|false
return|;
block|}
name|VectorBatchPattern
name|vectorBatchPattern
init|=
name|vectorBatchPatterns
operator|.
name|getTectorBatchPatternList
argument_list|()
operator|.
name|get
argument_list|(
name|batchCount
argument_list|)
decl_stmt|;
comment|// System.out.println("*DEBUG* vectorBatchPattern " + vectorBatchPattern.toString());
specifier|final
name|int
name|batchSize
init|=
name|vectorBatchPattern
operator|.
name|getBatchSize
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|columnCount
condition|;
name|c
operator|++
control|)
block|{
name|batch
operator|.
name|cols
index|[
name|c
index|]
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
name|BitSet
name|bitSet
init|=
name|vectorBatchPattern
operator|.
name|getBitSet
argument_list|()
decl_stmt|;
if|if
condition|(
name|bitSet
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|int
name|columnNum
init|=
name|bitSet
operator|.
name|nextSetBit
argument_list|(
literal|0
argument_list|)
init|;
name|columnNum
operator|>=
literal|0
condition|;
name|columnNum
operator|=
name|bitSet
operator|.
name|nextSetBit
argument_list|(
name|columnNum
operator|+
literal|1
argument_list|)
control|)
block|{
name|batch
operator|.
name|cols
index|[
name|columnNum
index|]
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|int
index|[]
name|selected
init|=
name|vectorBatchPattern
operator|.
name|getSelected
argument_list|()
decl_stmt|;
name|boolean
name|selectedInUse
init|=
operator|(
name|selected
operator|!=
literal|null
operator|)
decl_stmt|;
name|batch
operator|.
name|selectedInUse
operator|=
name|selectedInUse
expr_stmt|;
if|if
condition|(
name|selectedInUse
condition|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|selected
argument_list|,
literal|0
argument_list|,
name|batch
operator|.
name|selected
argument_list|,
literal|0
argument_list|,
name|batchSize
argument_list|)
expr_stmt|;
block|}
name|int
name|rowIndex
init|=
name|nextRowIndex
decl_stmt|;
for|for
control|(
name|int
name|logicalIndex
init|=
literal|0
init|;
name|logicalIndex
operator|<
name|batchSize
condition|;
name|logicalIndex
operator|++
control|)
block|{
specifier|final
name|int
name|batchIndex
init|=
operator|(
name|selectedInUse
condition|?
name|selected
index|[
name|logicalIndex
index|]
else|:
name|logicalIndex
operator|)
decl_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|columnCount
condition|;
name|c
operator|++
control|)
block|{
if|if
condition|(
name|batch
operator|.
name|cols
index|[
name|c
index|]
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|logicalIndex
operator|>
literal|0
condition|)
block|{
continue|continue;
block|}
name|vectorAssignRow
operator|.
name|assignRowColumn
argument_list|(
name|batch
argument_list|,
literal|0
argument_list|,
name|c
argument_list|,
name|randomRows
index|[
name|rowIndex
index|]
index|[
name|c
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|vectorAssignRow
operator|.
name|assignRowColumn
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|,
name|c
argument_list|,
name|randomRows
index|[
name|rowIndex
index|]
index|[
name|c
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|rowIndex
operator|++
expr_stmt|;
block|}
name|batch
operator|.
name|size
operator|=
name|batchSize
expr_stmt|;
name|batchCount
operator|++
expr_stmt|;
name|nextRowIndex
operator|+=
name|batchSize
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

