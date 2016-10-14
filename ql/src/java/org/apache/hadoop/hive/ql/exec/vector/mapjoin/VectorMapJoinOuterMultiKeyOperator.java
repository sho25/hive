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
name|mapjoin
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
name|CompilationOpContext
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
name|vector
operator|.
name|ColumnVector
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
name|VectorizationContext
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
name|VectorExpression
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
name|OperatorDesc
import|;
end_import

begin_comment
comment|// Multi-Key hash table import.
end_comment

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
name|VectorMapJoinBytesHashMap
import|;
end_import

begin_comment
comment|// Multi-Key specific imports.
end_comment

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
name|serde2
operator|.
name|ByteStream
operator|.
name|Output
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
name|binarysortable
operator|.
name|fast
operator|.
name|BinarySortableSerializeWrite
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
comment|/*  * Specialized class for doing a vectorized map join that is an outer join on Multi-Key  * using a hash map.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinOuterMultiKeyOperator
extends|extends
name|VectorMapJoinOuterGenerateResultOperator
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|//------------------------------------------------------------------------------------------------
specifier|private
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|VectorMapJoinOuterMultiKeyOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
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
name|CLASS_NAME
argument_list|)
decl_stmt|;
specifier|protected
name|String
name|getLoggingPrefix
parameter_list|()
block|{
return|return
name|super
operator|.
name|getLoggingPrefix
argument_list|(
name|CLASS_NAME
argument_list|)
return|;
block|}
comment|//------------------------------------------------------------------------------------------------
comment|// (none)
comment|// The above members are initialized by the constructor and must not be
comment|// transient.
comment|//---------------------------------------------------------------------------
comment|// The hash map for this specialized class.
specifier|private
specifier|transient
name|VectorMapJoinBytesHashMap
name|hashMap
decl_stmt|;
comment|//---------------------------------------------------------------------------
comment|// Multi-Key specific members.
comment|//
comment|// Object that can take a set of columns in row in a vectorized row batch and serialized it.
specifier|private
specifier|transient
name|VectorSerializeRow
name|keyVectorSerializeWrite
decl_stmt|;
comment|// The BinarySortable serialization of the current key.
specifier|private
specifier|transient
name|Output
name|currentKeyOutput
decl_stmt|;
comment|// The BinarySortable serialization of the saved key for a possible series of equal keys.
specifier|private
specifier|transient
name|Output
name|saveKeyOutput
decl_stmt|;
comment|//---------------------------------------------------------------------------
comment|// Pass-thru constructors.
comment|//
comment|/** Kryo ctor. */
specifier|protected
name|VectorMapJoinOuterMultiKeyOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorMapJoinOuterMultiKeyOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
specifier|public
name|VectorMapJoinOuterMultiKeyOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|,
name|VectorizationContext
name|vContext
parameter_list|,
name|OperatorDesc
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
argument_list|(
name|ctx
argument_list|,
name|vContext
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
comment|//---------------------------------------------------------------------------
comment|// Process Multi-Key Outer Join on a vectorized row batch.
comment|//
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|VectorizedRowBatch
name|batch
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|row
decl_stmt|;
name|alias
operator|=
operator|(
name|byte
operator|)
name|tag
expr_stmt|;
if|if
condition|(
name|needCommonSetup
condition|)
block|{
comment|// Our one time process method initialization.
name|commonSetup
argument_list|(
name|batch
argument_list|)
expr_stmt|;
comment|/*          * Initialize Multi-Key members for this specialized class.          */
name|keyVectorSerializeWrite
operator|=
operator|new
name|VectorSerializeRow
argument_list|(
operator|new
name|BinarySortableSerializeWrite
argument_list|(
name|bigTableKeyColumnMap
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|keyVectorSerializeWrite
operator|.
name|init
argument_list|(
name|bigTableKeyTypeInfos
argument_list|,
name|bigTableKeyColumnMap
argument_list|)
expr_stmt|;
name|currentKeyOutput
operator|=
operator|new
name|Output
argument_list|()
expr_stmt|;
name|saveKeyOutput
operator|=
operator|new
name|Output
argument_list|()
expr_stmt|;
name|needCommonSetup
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|needHashTableSetup
condition|)
block|{
comment|// Setup our hash table specialization.  It will be the first time the process
comment|// method is called, or after a Hybrid Grace reload.
comment|/*          * Get our Multi-Key hash map information for this specialized class.          */
name|hashMap
operator|=
operator|(
name|VectorMapJoinBytesHashMap
operator|)
name|vectorMapJoinHashTable
expr_stmt|;
name|needHashTableSetup
operator|=
literal|false
expr_stmt|;
block|}
name|batchCounter
operator|++
expr_stmt|;
specifier|final
name|int
name|inputLogicalSize
init|=
name|batch
operator|.
name|size
decl_stmt|;
if|if
condition|(
name|inputLogicalSize
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|isLogDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|CLASS_NAME
operator|+
literal|" batch #"
operator|+
name|batchCounter
operator|+
literal|" empty"
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
comment|// Do the per-batch setup for an outer join.
name|outerPerBatchSetup
argument_list|(
name|batch
argument_list|)
expr_stmt|;
comment|// For outer join, remember our input rows before ON expression filtering or before
comment|// hash table matching so we can generate results for all rows (matching and non matching)
comment|// later.
name|boolean
name|inputSelectedInUse
init|=
name|batch
operator|.
name|selectedInUse
decl_stmt|;
if|if
condition|(
name|inputSelectedInUse
condition|)
block|{
comment|// if (!verifyMonotonicallyIncreasing(batch.selected, batch.size)) {
comment|//   throw new HiveException("batch.selected is not in sort order and unique");
comment|// }
name|System
operator|.
name|arraycopy
argument_list|(
name|batch
operator|.
name|selected
argument_list|,
literal|0
argument_list|,
name|inputSelected
argument_list|,
literal|0
argument_list|,
name|inputLogicalSize
argument_list|)
expr_stmt|;
block|}
comment|// Filtering for outer join just removes rows available for hash table matching.
name|boolean
name|someRowsFilteredOut
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|bigTableFilterExpressions
operator|.
name|length
operator|>
literal|0
condition|)
block|{
comment|// Since the input
for|for
control|(
name|VectorExpression
name|ve
range|:
name|bigTableFilterExpressions
control|)
block|{
name|ve
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
name|someRowsFilteredOut
operator|=
operator|(
name|batch
operator|.
name|size
operator|!=
name|inputLogicalSize
operator|)
expr_stmt|;
if|if
condition|(
name|isLogDebugEnabled
condition|)
block|{
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
if|if
condition|(
name|inputSelectedInUse
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|CLASS_NAME
operator|+
literal|" inputSelected "
operator|+
name|intArrayToRangesString
argument_list|(
name|inputSelected
argument_list|,
name|inputLogicalSize
argument_list|)
operator|+
literal|" filtered batch.selected "
operator|+
name|intArrayToRangesString
argument_list|(
name|batch
operator|.
name|selected
argument_list|,
name|batch
operator|.
name|size
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|CLASS_NAME
operator|+
literal|" inputLogicalSize "
operator|+
name|inputLogicalSize
operator|+
literal|" filtered batch.selected "
operator|+
name|intArrayToRangesString
argument_list|(
name|batch
operator|.
name|selected
argument_list|,
name|batch
operator|.
name|size
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// Perform any key expressions.  Results will go into scratch columns.
if|if
condition|(
name|bigTableKeyExpressions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|VectorExpression
name|ve
range|:
name|bigTableKeyExpressions
control|)
block|{
name|ve
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*        * Multi-Key specific declarations.        */
comment|// None.
comment|/*        * Multi-Key Long check for repeating.        */
comment|// If all BigTable input columns to key expressions are isRepeating, then
comment|// calculate key once; lookup once.
comment|// Also determine if any nulls are present since for a join that means no match.
name|boolean
name|allKeyInputColumnsRepeating
decl_stmt|;
name|boolean
name|someKeyInputColumnIsNull
init|=
literal|false
decl_stmt|;
comment|// Only valid if allKeyInputColumnsRepeating is true.
if|if
condition|(
name|bigTableKeyColumnMap
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|allKeyInputColumnsRepeating
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|allKeyInputColumnsRepeating
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|bigTableKeyColumnMap
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ColumnVector
name|colVector
init|=
name|batch
operator|.
name|cols
index|[
name|bigTableKeyColumnMap
index|[
name|i
index|]
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|colVector
operator|.
name|isRepeating
condition|)
block|{
name|allKeyInputColumnsRepeating
operator|=
literal|false
expr_stmt|;
break|break;
block|}
if|if
condition|(
operator|!
name|colVector
operator|.
name|noNulls
operator|&&
name|colVector
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|someKeyInputColumnIsNull
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|allKeyInputColumnsRepeating
condition|)
block|{
comment|/*          * Repeating.          */
comment|// All key input columns are repeating.  Generate key once.  Lookup once.
comment|// Since the key is repeated, we must use entry 0 regardless of selectedInUse.
comment|/*          * Multi-Key specific repeated lookup.          */
name|JoinUtil
operator|.
name|JoinResult
name|joinResult
decl_stmt|;
if|if
condition|(
name|batch
operator|.
name|size
operator|==
literal|0
condition|)
block|{
comment|// Whole repeated key batch was filtered out.
name|joinResult
operator|=
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|NOMATCH
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|someKeyInputColumnIsNull
condition|)
block|{
comment|// Any (repeated) null key column is no match for whole batch.
name|joinResult
operator|=
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|NOMATCH
expr_stmt|;
block|}
else|else
block|{
comment|// All key input columns are repeating.  Generate key once.  Lookup once.
name|keyVectorSerializeWrite
operator|.
name|setOutput
argument_list|(
name|currentKeyOutput
argument_list|)
expr_stmt|;
name|keyVectorSerializeWrite
operator|.
name|serializeWrite
argument_list|(
name|batch
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|byte
index|[]
name|keyBytes
init|=
name|currentKeyOutput
operator|.
name|getData
argument_list|()
decl_stmt|;
name|int
name|keyLength
init|=
name|currentKeyOutput
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|joinResult
operator|=
name|hashMap
operator|.
name|lookup
argument_list|(
name|keyBytes
argument_list|,
literal|0
argument_list|,
name|keyLength
argument_list|,
name|hashMapResults
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*          * Common repeated join result processing.          */
if|if
condition|(
name|isLogDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|CLASS_NAME
operator|+
literal|" batch #"
operator|+
name|batchCounter
operator|+
literal|" repeated joinResult "
operator|+
name|joinResult
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|finishOuterRepeated
argument_list|(
name|batch
argument_list|,
name|joinResult
argument_list|,
name|hashMapResults
index|[
literal|0
index|]
argument_list|,
name|someRowsFilteredOut
argument_list|,
name|inputSelectedInUse
argument_list|,
name|inputLogicalSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|/*          * NOT Repeating.          */
if|if
condition|(
name|isLogDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|CLASS_NAME
operator|+
literal|" batch #"
operator|+
name|batchCounter
operator|+
literal|" non-repeated"
argument_list|)
expr_stmt|;
block|}
name|int
name|selected
index|[]
init|=
name|batch
operator|.
name|selected
decl_stmt|;
name|boolean
name|selectedInUse
init|=
name|batch
operator|.
name|selectedInUse
decl_stmt|;
name|int
name|hashMapResultCount
init|=
literal|0
decl_stmt|;
name|int
name|allMatchCount
init|=
literal|0
decl_stmt|;
name|int
name|equalKeySeriesCount
init|=
literal|0
decl_stmt|;
name|int
name|spillCount
init|=
literal|0
decl_stmt|;
name|boolean
name|atLeastOneNonMatch
init|=
name|someRowsFilteredOut
decl_stmt|;
comment|/*          * Multi-Key specific variables.          */
name|Output
name|temp
decl_stmt|;
comment|// We optimize performance by only looking up the first key in a series of equal keys.
name|boolean
name|haveSaveKey
init|=
literal|false
decl_stmt|;
name|JoinUtil
operator|.
name|JoinResult
name|saveJoinResult
init|=
name|JoinUtil
operator|.
name|JoinResult
operator|.
name|NOMATCH
decl_stmt|;
comment|// Logical loop over the rows in the batch since the batch may have selected in use.
for|for
control|(
name|int
name|logical
init|=
literal|0
init|;
name|logical
operator|<
name|batch
operator|.
name|size
condition|;
name|logical
operator|++
control|)
block|{
name|int
name|batchIndex
init|=
operator|(
name|selectedInUse
condition|?
name|selected
index|[
name|logical
index|]
else|:
name|logical
operator|)
decl_stmt|;
comment|// VectorizedBatchUtil.debugDisplayOneRow(batch, batchIndex, taskName + ", " + getOperatorId() + " candidate " + CLASS_NAME + " batch");
comment|/*            * Multi-Key outer null detection.            */
comment|// Generate binary sortable key for current row in vectorized row batch.
name|keyVectorSerializeWrite
operator|.
name|setOutput
argument_list|(
name|currentKeyOutput
argument_list|)
expr_stmt|;
name|keyVectorSerializeWrite
operator|.
name|serializeWrite
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|)
expr_stmt|;
if|if
condition|(
name|keyVectorSerializeWrite
operator|.
name|getHasAnyNulls
argument_list|()
condition|)
block|{
comment|// Have that the NULL does not interfere with the current equal key series, if there
comment|// is one. We do not set saveJoinResult.
comment|//
comment|//    Let a current MATCH equal key series keep going, or
comment|//    Let a current SPILL equal key series keep going, or
comment|//    Let a current NOMATCH keep not matching.
name|atLeastOneNonMatch
operator|=
literal|true
expr_stmt|;
comment|// LOG.debug(CLASS_NAME + " logical " + logical + " batchIndex " + batchIndex + " NULL");
block|}
else|else
block|{
comment|/*              * Multi-Key outer get key.              */
comment|// Generated earlier to get possible null(s).
comment|/*              * Equal key series checking.              */
if|if
condition|(
operator|!
name|haveSaveKey
operator|||
operator|!
name|saveKeyOutput
operator|.
name|arraysEquals
argument_list|(
name|currentKeyOutput
argument_list|)
condition|)
block|{
comment|// New key.
if|if
condition|(
name|haveSaveKey
condition|)
block|{
comment|// Move on with our counts.
switch|switch
condition|(
name|saveJoinResult
condition|)
block|{
case|case
name|MATCH
case|:
name|hashMapResultCount
operator|++
expr_stmt|;
name|equalKeySeriesCount
operator|++
expr_stmt|;
break|break;
case|case
name|SPILL
case|:
name|hashMapResultCount
operator|++
expr_stmt|;
break|break;
case|case
name|NOMATCH
case|:
break|break;
block|}
block|}
comment|// Regardless of our matching result, we keep that information to make multiple use
comment|// of it for a possible series of equal keys.
name|haveSaveKey
operator|=
literal|true
expr_stmt|;
comment|/*                * Multi-Key specific save key.                */
name|temp
operator|=
name|saveKeyOutput
expr_stmt|;
name|saveKeyOutput
operator|=
name|currentKeyOutput
expr_stmt|;
name|currentKeyOutput
operator|=
name|temp
expr_stmt|;
comment|/*                * Multi-Key specific lookup key.                */
name|byte
index|[]
name|keyBytes
init|=
name|saveKeyOutput
operator|.
name|getData
argument_list|()
decl_stmt|;
name|int
name|keyLength
init|=
name|saveKeyOutput
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|saveJoinResult
operator|=
name|hashMap
operator|.
name|lookup
argument_list|(
name|keyBytes
argument_list|,
literal|0
argument_list|,
name|keyLength
argument_list|,
name|hashMapResults
index|[
name|hashMapResultCount
index|]
argument_list|)
expr_stmt|;
comment|/*                * Common outer join result processing.                */
switch|switch
condition|(
name|saveJoinResult
condition|)
block|{
case|case
name|MATCH
case|:
name|equalKeySeriesHashMapResultIndices
index|[
name|equalKeySeriesCount
index|]
operator|=
name|hashMapResultCount
expr_stmt|;
name|equalKeySeriesAllMatchIndices
index|[
name|equalKeySeriesCount
index|]
operator|=
name|allMatchCount
expr_stmt|;
name|equalKeySeriesIsSingleValue
index|[
name|equalKeySeriesCount
index|]
operator|=
name|hashMapResults
index|[
name|hashMapResultCount
index|]
operator|.
name|isSingleRow
argument_list|()
expr_stmt|;
name|equalKeySeriesDuplicateCounts
index|[
name|equalKeySeriesCount
index|]
operator|=
literal|1
expr_stmt|;
name|allMatchs
index|[
name|allMatchCount
operator|++
index|]
operator|=
name|batchIndex
expr_stmt|;
comment|// VectorizedBatchUtil.debugDisplayOneRow(batch, batchIndex, CLASS_NAME + " MATCH isSingleValue " + equalKeySeriesIsSingleValue[equalKeySeriesCount] + " currentKey " + currentKey);
break|break;
case|case
name|SPILL
case|:
name|spills
index|[
name|spillCount
index|]
operator|=
name|batchIndex
expr_stmt|;
name|spillHashMapResultIndices
index|[
name|spillCount
index|]
operator|=
name|hashMapResultCount
expr_stmt|;
name|spillCount
operator|++
expr_stmt|;
break|break;
case|case
name|NOMATCH
case|:
name|atLeastOneNonMatch
operator|=
literal|true
expr_stmt|;
comment|// VectorizedBatchUtil.debugDisplayOneRow(batch, batchIndex, CLASS_NAME + " NOMATCH" + " currentKey " + currentKey);
break|break;
block|}
block|}
else|else
block|{
comment|// LOG.debug(CLASS_NAME + " logical " + logical + " batchIndex " + batchIndex + " Key Continues " + saveKey + " " + saveJoinResult.name());
comment|// Series of equal keys.
switch|switch
condition|(
name|saveJoinResult
condition|)
block|{
case|case
name|MATCH
case|:
name|equalKeySeriesDuplicateCounts
index|[
name|equalKeySeriesCount
index|]
operator|++
expr_stmt|;
name|allMatchs
index|[
name|allMatchCount
operator|++
index|]
operator|=
name|batchIndex
expr_stmt|;
comment|// VectorizedBatchUtil.debugDisplayOneRow(batch, batchIndex, CLASS_NAME + " MATCH duplicate");
break|break;
case|case
name|SPILL
case|:
name|spills
index|[
name|spillCount
index|]
operator|=
name|batchIndex
expr_stmt|;
name|spillHashMapResultIndices
index|[
name|spillCount
index|]
operator|=
name|hashMapResultCount
expr_stmt|;
name|spillCount
operator|++
expr_stmt|;
break|break;
case|case
name|NOMATCH
case|:
comment|// VectorizedBatchUtil.debugDisplayOneRow(batch, batchIndex, CLASS_NAME + " NOMATCH duplicate");
break|break;
block|}
block|}
comment|// if (!verifyMonotonicallyIncreasing(allMatchs, allMatchCount)) {
comment|//   throw new HiveException("allMatchs is not in sort order and unique");
comment|// }
block|}
block|}
if|if
condition|(
name|haveSaveKey
condition|)
block|{
comment|// Update our counts for the last key.
switch|switch
condition|(
name|saveJoinResult
condition|)
block|{
case|case
name|MATCH
case|:
name|hashMapResultCount
operator|++
expr_stmt|;
name|equalKeySeriesCount
operator|++
expr_stmt|;
break|break;
case|case
name|SPILL
case|:
name|hashMapResultCount
operator|++
expr_stmt|;
break|break;
case|case
name|NOMATCH
case|:
break|break;
block|}
block|}
if|if
condition|(
name|isLogDebugEnabled
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|CLASS_NAME
operator|+
literal|" batch #"
operator|+
name|batchCounter
operator|+
literal|" allMatchs "
operator|+
name|intArrayToRangesString
argument_list|(
name|allMatchs
argument_list|,
name|allMatchCount
argument_list|)
operator|+
literal|" equalKeySeriesHashMapResultIndices "
operator|+
name|intArrayToRangesString
argument_list|(
name|equalKeySeriesHashMapResultIndices
argument_list|,
name|equalKeySeriesCount
argument_list|)
operator|+
literal|" equalKeySeriesAllMatchIndices "
operator|+
name|intArrayToRangesString
argument_list|(
name|equalKeySeriesAllMatchIndices
argument_list|,
name|equalKeySeriesCount
argument_list|)
operator|+
literal|" equalKeySeriesIsSingleValue "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|equalKeySeriesIsSingleValue
argument_list|,
literal|0
argument_list|,
name|equalKeySeriesCount
argument_list|)
argument_list|)
operator|+
literal|" equalKeySeriesDuplicateCounts "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|equalKeySeriesDuplicateCounts
argument_list|,
literal|0
argument_list|,
name|equalKeySeriesCount
argument_list|)
argument_list|)
operator|+
literal|" atLeastOneNonMatch "
operator|+
name|atLeastOneNonMatch
operator|+
literal|" inputSelectedInUse "
operator|+
name|inputSelectedInUse
operator|+
literal|" inputLogicalSize "
operator|+
name|inputLogicalSize
operator|+
literal|" spills "
operator|+
name|intArrayToRangesString
argument_list|(
name|spills
argument_list|,
name|spillCount
argument_list|)
operator|+
literal|" spillHashMapResultIndices "
operator|+
name|intArrayToRangesString
argument_list|(
name|spillHashMapResultIndices
argument_list|,
name|spillCount
argument_list|)
operator|+
literal|" hashMapResults "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|hashMapResults
argument_list|,
literal|0
argument_list|,
name|hashMapResultCount
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// We will generate results for all matching and non-matching rows.
name|finishOuter
argument_list|(
name|batch
argument_list|,
name|allMatchCount
argument_list|,
name|equalKeySeriesCount
argument_list|,
name|atLeastOneNonMatch
argument_list|,
name|inputSelectedInUse
argument_list|,
name|inputLogicalSize
argument_list|,
name|spillCount
argument_list|,
name|hashMapResultCount
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|batch
operator|.
name|size
operator|>
literal|0
condition|)
block|{
comment|// Forward any remaining selected rows.
name|forwardBigTableBatch
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

