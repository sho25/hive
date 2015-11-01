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
name|VectorSerializeRow
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
name|serde2
operator|.
name|fast
operator|.
name|SerializeWrite
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
name|typeinfo
operator|.
name|TypeInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
comment|/**  * A key series of a multiple columns of keys where the keys get serialized.  * (Or, it can be 1 column).  */
end_comment

begin_class
specifier|public
class|class
name|VectorKeySeriesMultiSerialized
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
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|VectorKeySeriesMultiSerialized
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|VectorSerializeRow
argument_list|<
name|T
argument_list|>
name|keySerializeRow
decl_stmt|;
specifier|private
name|boolean
index|[]
name|hasAnyNulls
decl_stmt|;
specifier|public
name|VectorKeySeriesMultiSerialized
parameter_list|(
name|T
name|serializeWrite
parameter_list|)
block|{
name|super
argument_list|(
name|serializeWrite
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|init
parameter_list|(
name|TypeInfo
index|[]
name|typeInfos
parameter_list|,
name|int
index|[]
name|columnNums
parameter_list|)
throws|throws
name|HiveException
block|{
name|keySerializeRow
operator|=
operator|new
name|VectorSerializeRow
argument_list|<
name|T
argument_list|>
argument_list|(
name|serializeWrite
argument_list|)
expr_stmt|;
name|keySerializeRow
operator|.
name|init
argument_list|(
name|typeInfos
argument_list|,
name|columnNums
argument_list|)
expr_stmt|;
name|hasAnyNulls
operator|=
operator|new
name|boolean
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
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
comment|// LOG.info("VectorKeySeriesMultiSerialized processBatch size " + currentBatchSize + " numCols " + batch.numCols + " selectedInUse " + batch.selectedInUse);
name|int
name|prevKeyStart
init|=
literal|0
decl_stmt|;
name|int
name|prevKeyLength
decl_stmt|;
name|int
name|currentKeyStart
init|=
literal|0
decl_stmt|;
name|output
operator|.
name|reset
argument_list|()
expr_stmt|;
name|seriesCount
operator|=
literal|0
expr_stmt|;
name|boolean
name|prevKeyIsNull
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
name|int
name|index
init|=
name|selected
index|[
literal|0
index|]
decl_stmt|;
name|keySerializeRow
operator|.
name|setOutputAppend
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|keySerializeRow
operator|.
name|serializeWrite
argument_list|(
name|batch
argument_list|,
name|index
argument_list|)
expr_stmt|;
if|if
condition|(
name|keySerializeRow
operator|.
name|getIsAllNulls
argument_list|()
condition|)
block|{
name|seriesIsAllNull
index|[
literal|0
index|]
operator|=
name|prevKeyIsNull
operator|=
literal|true
expr_stmt|;
name|prevKeyLength
operator|=
literal|0
expr_stmt|;
name|output
operator|.
name|setWritePosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|nonNullKeyCount
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
name|seriesIsAllNull
index|[
literal|0
index|]
operator|=
name|prevKeyIsNull
operator|=
literal|false
expr_stmt|;
name|serializedKeyLengths
index|[
literal|0
index|]
operator|=
name|currentKeyStart
operator|=
name|prevKeyLength
operator|=
name|output
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|hasAnyNulls
index|[
literal|0
index|]
operator|=
name|keySerializeRow
operator|.
name|getHasAnyNulls
argument_list|()
expr_stmt|;
name|nonNullKeyCount
operator|=
literal|1
expr_stmt|;
block|}
name|int
name|keyLength
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
name|keySerializeRow
operator|.
name|setOutputAppend
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|keySerializeRow
operator|.
name|serializeWrite
argument_list|(
name|batch
argument_list|,
name|index
argument_list|)
expr_stmt|;
if|if
condition|(
name|keySerializeRow
operator|.
name|getIsAllNulls
argument_list|()
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
name|prevKeyIsNull
operator|=
literal|true
expr_stmt|;
block|}
name|output
operator|.
name|setWritePosition
argument_list|(
name|currentKeyStart
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|keyLength
operator|=
name|output
operator|.
name|getLength
argument_list|()
operator|-
name|currentKeyStart
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
name|output
operator|.
name|getData
argument_list|()
argument_list|,
name|prevKeyStart
argument_list|,
name|prevKeyLength
argument_list|,
name|output
operator|.
name|getData
argument_list|()
argument_list|,
name|currentKeyStart
argument_list|,
name|keyLength
argument_list|)
condition|)
block|{
name|duplicateCounts
index|[
name|seriesCount
index|]
operator|++
expr_stmt|;
name|output
operator|.
name|setWritePosition
argument_list|(
name|currentKeyStart
argument_list|)
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
name|prevKeyIsNull
operator|=
literal|false
expr_stmt|;
name|prevKeyStart
operator|=
name|currentKeyStart
expr_stmt|;
name|serializedKeyLengths
index|[
name|nonNullKeyCount
index|]
operator|=
name|prevKeyLength
operator|=
name|keyLength
expr_stmt|;
name|currentKeyStart
operator|+=
name|keyLength
expr_stmt|;
name|hasAnyNulls
index|[
name|nonNullKeyCount
index|]
operator|=
name|keySerializeRow
operator|.
name|getHasAnyNulls
argument_list|()
expr_stmt|;
name|nonNullKeyCount
operator|++
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
else|else
block|{
name|keySerializeRow
operator|.
name|setOutputAppend
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|keySerializeRow
operator|.
name|serializeWrite
argument_list|(
name|batch
argument_list|,
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|keySerializeRow
operator|.
name|getIsAllNulls
argument_list|()
condition|)
block|{
name|seriesIsAllNull
index|[
literal|0
index|]
operator|=
name|prevKeyIsNull
operator|=
literal|true
expr_stmt|;
name|prevKeyLength
operator|=
literal|0
expr_stmt|;
name|output
operator|.
name|setWritePosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|nonNullKeyCount
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
name|seriesIsAllNull
index|[
literal|0
index|]
operator|=
name|prevKeyIsNull
operator|=
literal|false
expr_stmt|;
name|serializedKeyLengths
index|[
literal|0
index|]
operator|=
name|currentKeyStart
operator|=
name|prevKeyLength
operator|=
name|output
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|hasAnyNulls
index|[
literal|0
index|]
operator|=
name|keySerializeRow
operator|.
name|getHasAnyNulls
argument_list|()
expr_stmt|;
name|nonNullKeyCount
operator|=
literal|1
expr_stmt|;
block|}
name|int
name|keyLength
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
name|keySerializeRow
operator|.
name|setOutputAppend
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|keySerializeRow
operator|.
name|serializeWrite
argument_list|(
name|batch
argument_list|,
name|index
argument_list|)
expr_stmt|;
if|if
condition|(
name|keySerializeRow
operator|.
name|getIsAllNulls
argument_list|()
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
name|prevKeyIsNull
operator|=
literal|true
expr_stmt|;
block|}
name|output
operator|.
name|setWritePosition
argument_list|(
name|currentKeyStart
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|keyLength
operator|=
name|output
operator|.
name|getLength
argument_list|()
operator|-
name|currentKeyStart
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
name|output
operator|.
name|getData
argument_list|()
argument_list|,
name|prevKeyStart
argument_list|,
name|prevKeyLength
argument_list|,
name|output
operator|.
name|getData
argument_list|()
argument_list|,
name|currentKeyStart
argument_list|,
name|keyLength
argument_list|)
condition|)
block|{
name|duplicateCounts
index|[
name|seriesCount
index|]
operator|++
expr_stmt|;
name|output
operator|.
name|setWritePosition
argument_list|(
name|currentKeyStart
argument_list|)
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
name|prevKeyIsNull
operator|=
literal|false
expr_stmt|;
name|prevKeyStart
operator|=
name|currentKeyStart
expr_stmt|;
name|serializedKeyLengths
index|[
name|nonNullKeyCount
index|]
operator|=
name|prevKeyLength
operator|=
name|keyLength
expr_stmt|;
name|currentKeyStart
operator|+=
name|keyLength
expr_stmt|;
name|hasAnyNulls
index|[
name|nonNullKeyCount
index|]
operator|=
name|keySerializeRow
operator|.
name|getHasAnyNulls
argument_list|()
expr_stmt|;
name|nonNullKeyCount
operator|++
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
annotation|@
name|Override
specifier|public
name|void
name|setNextNonNullKey
parameter_list|(
name|int
name|nonNullKeyPosition
parameter_list|)
block|{
name|super
operator|.
name|setNextNonNullKey
argument_list|(
name|nonNullKeyPosition
argument_list|)
expr_stmt|;
name|currentHasAnyNulls
operator|=
name|hasAnyNulls
index|[
name|nonNullKeyPosition
index|]
expr_stmt|;
block|}
block|}
end_class

end_unit

