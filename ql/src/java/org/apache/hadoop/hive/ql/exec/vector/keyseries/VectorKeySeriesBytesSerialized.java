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
name|keyseries
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
name|Arrays
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
name|BytesColumnVector
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
name|VectorizedRowBatch
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
name|expressions
operator|.
name|StringExpr
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
name|serde2
operator|.
name|fast
operator|.
name|SerializeWrite
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
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * A key series of a single column of byte array keys where the keys get serialized.  */
end_comment

begin_class
specifier|public
class|class
name|VectorKeySeriesBytesSerialized
parameter_list|<
name|T
extends|extends
name|SerializeWrite
parameter_list|>
extends|extends
name|VectorKeySeriesSerializedImpl
argument_list|<
name|T
argument_list|>
implements|implements
name|VectorKeySeriesSerialized
block|{
specifier|private
specifier|final
name|int
name|columnNum
decl_stmt|;
specifier|private
name|int
name|outputStartPosition
decl_stmt|;
specifier|public
name|VectorKeySeriesBytesSerialized
parameter_list|(
name|int
name|columnNum
parameter_list|,
name|T
name|serializeWrite
parameter_list|)
block|{
name|super
argument_list|(
name|serializeWrite
argument_list|)
expr_stmt|;
name|this
operator|.
name|columnNum
operator|=
name|columnNum
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|processBatch
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
throws|throws
name|IOException
block|{
name|currentBatchSize
operator|=
name|batch
operator|.
name|size
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|currentBatchSize
operator|>
literal|0
argument_list|)
expr_stmt|;
name|BytesColumnVector
name|bytesColVector
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|columnNum
index|]
decl_stmt|;
name|byte
index|[]
index|[]
name|vectorBytesArrays
init|=
name|bytesColVector
operator|.
name|vector
decl_stmt|;
name|int
index|[]
name|vectorStarts
init|=
name|bytesColVector
operator|.
name|start
decl_stmt|;
name|int
index|[]
name|vectorLengths
init|=
name|bytesColVector
operator|.
name|length
decl_stmt|;
comment|// The serialize routine uses this to build serializedKeyLengths.
name|outputStartPosition
operator|=
literal|0
expr_stmt|;
name|output
operator|.
name|reset
argument_list|()
expr_stmt|;
if|if
condition|(
name|bytesColVector
operator|.
name|isRepeating
condition|)
block|{
name|duplicateCounts
index|[
literal|0
index|]
operator|=
name|currentBatchSize
expr_stmt|;
if|if
condition|(
name|bytesColVector
operator|.
name|noNulls
operator|||
operator|!
name|bytesColVector
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|seriesIsAllNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|serialize
argument_list|(
literal|0
argument_list|,
name|vectorBytesArrays
index|[
literal|0
index|]
argument_list|,
name|vectorStarts
index|[
literal|0
index|]
argument_list|,
name|vectorLengths
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|nonNullKeyCount
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
name|seriesIsAllNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|nonNullKeyCount
operator|=
literal|0
expr_stmt|;
block|}
name|seriesCount
operator|=
literal|1
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|seriesCount
operator|<=
name|currentBatchSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|seriesCount
operator|=
literal|0
expr_stmt|;
name|nonNullKeyCount
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|int
index|[]
name|selected
init|=
name|batch
operator|.
name|selected
decl_stmt|;
if|if
condition|(
name|bytesColVector
operator|.
name|noNulls
condition|)
block|{
name|duplicateCounts
index|[
literal|0
index|]
operator|=
literal|1
expr_stmt|;
name|int
name|index
decl_stmt|;
name|index
operator|=
name|selected
index|[
literal|0
index|]
expr_stmt|;
name|byte
index|[]
name|prevKeyBytes
init|=
name|vectorBytesArrays
index|[
name|index
index|]
decl_stmt|;
name|int
name|prevKeyStart
init|=
name|vectorStarts
index|[
name|index
index|]
decl_stmt|;
name|int
name|prevKeyLength
init|=
name|vectorLengths
index|[
name|index
index|]
decl_stmt|;
name|serialize
argument_list|(
literal|0
argument_list|,
name|prevKeyBytes
argument_list|,
name|prevKeyStart
argument_list|,
name|prevKeyLength
argument_list|)
expr_stmt|;
name|int
name|currentKeyStart
decl_stmt|;
name|int
name|currentKeyLength
decl_stmt|;
name|byte
index|[]
name|currentKeyBytes
decl_stmt|;
for|for
control|(
name|int
name|logical
init|=
literal|1
init|;
name|logical
operator|<
name|currentBatchSize
condition|;
name|logical
operator|++
control|)
block|{
name|index
operator|=
name|selected
index|[
name|logical
index|]
expr_stmt|;
name|currentKeyBytes
operator|=
name|vectorBytesArrays
index|[
name|index
index|]
expr_stmt|;
name|currentKeyStart
operator|=
name|vectorStarts
index|[
name|index
index|]
expr_stmt|;
name|currentKeyLength
operator|=
name|vectorLengths
index|[
name|index
index|]
expr_stmt|;
if|if
condition|(
name|StringExpr
operator|.
name|equal
argument_list|(
name|prevKeyBytes
argument_list|,
name|prevKeyStart
argument_list|,
name|prevKeyLength
argument_list|,
name|currentKeyBytes
argument_list|,
name|currentKeyStart
argument_list|,
name|currentKeyLength
argument_list|)
condition|)
block|{
name|duplicateCounts
index|[
name|seriesCount
index|]
operator|++
expr_stmt|;
block|}
else|else
block|{
name|duplicateCounts
index|[
operator|++
name|seriesCount
index|]
operator|=
literal|1
expr_stmt|;
name|serialize
argument_list|(
name|seriesCount
argument_list|,
name|currentKeyBytes
argument_list|,
name|currentKeyStart
argument_list|,
name|currentKeyLength
argument_list|)
expr_stmt|;
name|prevKeyBytes
operator|=
name|currentKeyBytes
expr_stmt|;
name|prevKeyStart
operator|=
name|currentKeyStart
expr_stmt|;
name|prevKeyLength
operator|=
name|currentKeyLength
expr_stmt|;
block|}
block|}
name|Arrays
operator|.
name|fill
argument_list|(
name|seriesIsAllNull
argument_list|,
literal|0
argument_list|,
operator|++
name|seriesCount
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|nonNullKeyCount
operator|=
name|seriesCount
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|seriesCount
operator|<=
name|currentBatchSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|boolean
index|[]
name|isNull
init|=
name|bytesColVector
operator|.
name|isNull
decl_stmt|;
name|boolean
name|prevKeyIsNull
decl_stmt|;
name|byte
index|[]
name|prevKeyBytes
init|=
literal|null
decl_stmt|;
name|int
name|prevKeyStart
init|=
literal|0
decl_stmt|;
name|int
name|prevKeyLength
init|=
literal|0
decl_stmt|;
name|duplicateCounts
index|[
literal|0
index|]
operator|=
literal|1
expr_stmt|;
name|int
name|index
init|=
name|selected
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|isNull
index|[
name|index
index|]
condition|)
block|{
name|seriesIsAllNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|prevKeyIsNull
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|seriesIsAllNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|prevKeyIsNull
operator|=
literal|false
expr_stmt|;
name|prevKeyBytes
operator|=
name|vectorBytesArrays
index|[
name|index
index|]
expr_stmt|;
name|prevKeyStart
operator|=
name|vectorStarts
index|[
name|index
index|]
expr_stmt|;
name|prevKeyLength
operator|=
name|vectorLengths
index|[
name|index
index|]
expr_stmt|;
name|serialize
argument_list|(
literal|0
argument_list|,
name|prevKeyBytes
argument_list|,
name|prevKeyStart
argument_list|,
name|prevKeyLength
argument_list|)
expr_stmt|;
name|nonNullKeyCount
operator|=
literal|1
expr_stmt|;
block|}
name|int
name|currentKeyStart
decl_stmt|;
name|int
name|currentKeyLength
decl_stmt|;
name|byte
index|[]
name|currentKeyBytes
decl_stmt|;
for|for
control|(
name|int
name|logical
init|=
literal|1
init|;
name|logical
operator|<
name|currentBatchSize
condition|;
name|logical
operator|++
control|)
block|{
name|index
operator|=
name|selected
index|[
name|logical
index|]
expr_stmt|;
if|if
condition|(
name|isNull
index|[
name|index
index|]
condition|)
block|{
if|if
condition|(
name|prevKeyIsNull
condition|)
block|{
name|duplicateCounts
index|[
name|seriesCount
index|]
operator|++
expr_stmt|;
block|}
else|else
block|{
name|duplicateCounts
index|[
operator|++
name|seriesCount
index|]
operator|=
literal|1
expr_stmt|;
name|seriesIsAllNull
index|[
name|seriesCount
index|]
operator|=
literal|true
expr_stmt|;
name|prevKeyIsNull
operator|=
literal|true
expr_stmt|;
block|}
block|}
else|else
block|{
name|currentKeyBytes
operator|=
name|vectorBytesArrays
index|[
name|index
index|]
expr_stmt|;
name|currentKeyStart
operator|=
name|vectorStarts
index|[
name|index
index|]
expr_stmt|;
name|currentKeyLength
operator|=
name|vectorLengths
index|[
name|index
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|prevKeyIsNull
operator|&&
name|StringExpr
operator|.
name|equal
argument_list|(
name|prevKeyBytes
argument_list|,
name|prevKeyStart
argument_list|,
name|prevKeyLength
argument_list|,
name|currentKeyBytes
argument_list|,
name|currentKeyStart
argument_list|,
name|currentKeyLength
argument_list|)
condition|)
block|{
name|duplicateCounts
index|[
name|seriesCount
index|]
operator|++
expr_stmt|;
block|}
else|else
block|{
name|duplicateCounts
index|[
operator|++
name|seriesCount
index|]
operator|=
literal|1
expr_stmt|;
name|seriesIsAllNull
index|[
name|seriesCount
index|]
operator|=
literal|false
expr_stmt|;
name|serialize
argument_list|(
name|nonNullKeyCount
operator|++
argument_list|,
name|currentKeyBytes
argument_list|,
name|currentKeyStart
argument_list|,
name|currentKeyLength
argument_list|)
expr_stmt|;
name|prevKeyIsNull
operator|=
literal|false
expr_stmt|;
name|prevKeyBytes
operator|=
name|currentKeyBytes
expr_stmt|;
name|prevKeyStart
operator|=
name|currentKeyStart
expr_stmt|;
name|prevKeyLength
operator|=
name|currentKeyLength
expr_stmt|;
block|}
block|}
block|}
name|seriesCount
operator|++
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|seriesCount
operator|<=
name|currentBatchSize
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// NOT selectedInUse
if|if
condition|(
name|bytesColVector
operator|.
name|noNulls
condition|)
block|{
name|duplicateCounts
index|[
literal|0
index|]
operator|=
literal|1
expr_stmt|;
name|byte
index|[]
name|prevKeyBytes
init|=
name|vectorBytesArrays
index|[
literal|0
index|]
decl_stmt|;
name|int
name|prevKeyStart
init|=
name|vectorStarts
index|[
literal|0
index|]
decl_stmt|;
name|int
name|prevKeyLength
init|=
name|vectorLengths
index|[
literal|0
index|]
decl_stmt|;
name|serialize
argument_list|(
literal|0
argument_list|,
name|prevKeyBytes
argument_list|,
name|prevKeyStart
argument_list|,
name|prevKeyLength
argument_list|)
expr_stmt|;
name|int
name|currentKeyStart
decl_stmt|;
name|int
name|currentKeyLength
decl_stmt|;
name|byte
index|[]
name|currentKeyBytes
decl_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|1
init|;
name|index
operator|<
name|currentBatchSize
condition|;
name|index
operator|++
control|)
block|{
name|currentKeyBytes
operator|=
name|vectorBytesArrays
index|[
name|index
index|]
expr_stmt|;
name|currentKeyStart
operator|=
name|vectorStarts
index|[
name|index
index|]
expr_stmt|;
name|currentKeyLength
operator|=
name|vectorLengths
index|[
name|index
index|]
expr_stmt|;
if|if
condition|(
name|StringExpr
operator|.
name|equal
argument_list|(
name|prevKeyBytes
argument_list|,
name|prevKeyStart
argument_list|,
name|prevKeyLength
argument_list|,
name|currentKeyBytes
argument_list|,
name|currentKeyStart
argument_list|,
name|currentKeyLength
argument_list|)
condition|)
block|{
name|duplicateCounts
index|[
name|seriesCount
index|]
operator|++
expr_stmt|;
block|}
else|else
block|{
name|duplicateCounts
index|[
operator|++
name|seriesCount
index|]
operator|=
literal|1
expr_stmt|;
name|serialize
argument_list|(
name|seriesCount
argument_list|,
name|currentKeyBytes
argument_list|,
name|currentKeyStart
argument_list|,
name|currentKeyLength
argument_list|)
expr_stmt|;
name|prevKeyBytes
operator|=
name|currentKeyBytes
expr_stmt|;
name|prevKeyStart
operator|=
name|currentKeyStart
expr_stmt|;
name|prevKeyLength
operator|=
name|currentKeyLength
expr_stmt|;
block|}
block|}
name|Arrays
operator|.
name|fill
argument_list|(
name|seriesIsAllNull
argument_list|,
literal|0
argument_list|,
operator|++
name|seriesCount
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|nonNullKeyCount
operator|=
name|seriesCount
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|seriesCount
operator|<=
name|currentBatchSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|boolean
index|[]
name|isNull
init|=
name|bytesColVector
operator|.
name|isNull
decl_stmt|;
name|boolean
name|prevKeyIsNull
decl_stmt|;
name|byte
index|[]
name|prevKeyBytes
init|=
literal|null
decl_stmt|;
name|int
name|prevKeyStart
init|=
literal|0
decl_stmt|;
name|int
name|prevKeyLength
init|=
literal|0
decl_stmt|;
name|duplicateCounts
index|[
literal|0
index|]
operator|=
literal|1
expr_stmt|;
if|if
condition|(
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|seriesIsAllNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|prevKeyIsNull
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|seriesIsAllNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|prevKeyIsNull
operator|=
literal|false
expr_stmt|;
name|prevKeyBytes
operator|=
name|vectorBytesArrays
index|[
literal|0
index|]
expr_stmt|;
name|prevKeyStart
operator|=
name|vectorStarts
index|[
literal|0
index|]
expr_stmt|;
name|prevKeyLength
operator|=
name|vectorLengths
index|[
literal|0
index|]
expr_stmt|;
name|serialize
argument_list|(
literal|0
argument_list|,
name|prevKeyBytes
argument_list|,
name|prevKeyStart
argument_list|,
name|prevKeyLength
argument_list|)
expr_stmt|;
name|nonNullKeyCount
operator|=
literal|1
expr_stmt|;
block|}
name|byte
index|[]
name|currentKeyBytes
decl_stmt|;
name|int
name|currentKeyStart
decl_stmt|;
name|int
name|currentKeyLength
decl_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|1
init|;
name|index
operator|<
name|currentBatchSize
condition|;
name|index
operator|++
control|)
block|{
if|if
condition|(
name|isNull
index|[
name|index
index|]
condition|)
block|{
if|if
condition|(
name|prevKeyIsNull
condition|)
block|{
name|duplicateCounts
index|[
name|seriesCount
index|]
operator|++
expr_stmt|;
block|}
else|else
block|{
name|duplicateCounts
index|[
operator|++
name|seriesCount
index|]
operator|=
literal|1
expr_stmt|;
name|seriesIsAllNull
index|[
name|seriesCount
index|]
operator|=
literal|true
expr_stmt|;
name|prevKeyIsNull
operator|=
literal|true
expr_stmt|;
block|}
block|}
else|else
block|{
name|currentKeyBytes
operator|=
name|vectorBytesArrays
index|[
name|index
index|]
expr_stmt|;
name|currentKeyStart
operator|=
name|vectorStarts
index|[
name|index
index|]
expr_stmt|;
name|currentKeyLength
operator|=
name|vectorLengths
index|[
name|index
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|prevKeyIsNull
operator|&&
name|StringExpr
operator|.
name|equal
argument_list|(
name|prevKeyBytes
argument_list|,
name|prevKeyStart
argument_list|,
name|prevKeyLength
argument_list|,
name|currentKeyBytes
argument_list|,
name|currentKeyStart
argument_list|,
name|currentKeyLength
argument_list|)
condition|)
block|{
name|duplicateCounts
index|[
name|seriesCount
index|]
operator|++
expr_stmt|;
block|}
else|else
block|{
name|duplicateCounts
index|[
operator|++
name|seriesCount
index|]
operator|=
literal|1
expr_stmt|;
name|seriesIsAllNull
index|[
name|seriesCount
index|]
operator|=
literal|false
expr_stmt|;
name|serialize
argument_list|(
name|nonNullKeyCount
operator|++
argument_list|,
name|currentKeyBytes
argument_list|,
name|currentKeyStart
argument_list|,
name|currentKeyLength
argument_list|)
expr_stmt|;
name|prevKeyIsNull
operator|=
literal|false
expr_stmt|;
name|prevKeyBytes
operator|=
name|currentKeyBytes
expr_stmt|;
name|prevKeyStart
operator|=
name|currentKeyStart
expr_stmt|;
name|prevKeyLength
operator|=
name|currentKeyLength
expr_stmt|;
block|}
block|}
block|}
name|seriesCount
operator|++
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|seriesCount
operator|<=
name|currentBatchSize
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Finally.
name|computeSerializedHashCodes
argument_list|()
expr_stmt|;
name|positionToFirst
argument_list|()
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|validate
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|serialize
parameter_list|(
name|int
name|pos
parameter_list|,
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|serializeWrite
operator|.
name|setAppend
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|serializeWrite
operator|.
name|writeString
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|int
name|outputNewPosition
init|=
name|output
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|serializedKeyLengths
index|[
name|pos
index|]
operator|=
name|outputNewPosition
operator|-
name|outputStartPosition
expr_stmt|;
name|outputStartPosition
operator|=
name|outputNewPosition
expr_stmt|;
block|}
block|}
end_class

end_unit

