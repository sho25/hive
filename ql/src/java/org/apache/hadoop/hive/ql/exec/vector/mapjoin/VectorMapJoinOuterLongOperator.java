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
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
comment|// Single-Column Long hash table import.
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
name|VectorMapJoinLongHashMap
import|;
end_import

begin_comment
comment|// Single-Column Long specific imports.
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
name|LongColumnVector
import|;
end_import

begin_comment
comment|/*  * Specialized class for doing a vectorized map join that is an outer join on a Single-Column Long  * using a hash map.  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinOuterLongOperator
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
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|VectorMapJoinOuterLongOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|VectorMapJoinOuterLongOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|// (none)
comment|// The above members are initialized by the constructor and must not be
comment|// transient.
comment|//---------------------------------------------------------------------------
comment|// The hash map for this specialized class.
specifier|private
specifier|transient
name|VectorMapJoinLongHashMap
name|hashMap
decl_stmt|;
comment|//---------------------------------------------------------------------------
comment|// Single-Column Long specific members.
comment|//
comment|// For integers, we have optional min/max filtering.
specifier|private
specifier|transient
name|boolean
name|useMinMax
decl_stmt|;
specifier|private
specifier|transient
name|long
name|min
decl_stmt|;
specifier|private
specifier|transient
name|long
name|max
decl_stmt|;
comment|// The column number for this one column join specialization.
specifier|private
specifier|transient
name|int
name|singleJoinColumn
decl_stmt|;
comment|//---------------------------------------------------------------------------
comment|// Pass-thru constructors.
comment|//
specifier|public
name|VectorMapJoinOuterLongOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorMapJoinOuterLongOperator
parameter_list|(
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
name|vContext
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
comment|//---------------------------------------------------------------------------
comment|// Process Single-Column Long Outer Join on a vectorized row batch.
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
comment|/*          * Initialize Single-Column Long members for this specialized class.          */
name|singleJoinColumn
operator|=
name|bigTableKeyColumnMap
index|[
literal|0
index|]
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
comment|/*          * Get our Single-Column Long hash map information for this specialized class.          */
name|hashMap
operator|=
operator|(
name|VectorMapJoinLongHashMap
operator|)
name|vectorMapJoinHashTable
expr_stmt|;
name|useMinMax
operator|=
name|hashMap
operator|.
name|useMinMax
argument_list|()
expr_stmt|;
if|if
condition|(
name|useMinMax
condition|)
block|{
name|min
operator|=
name|hashMap
operator|.
name|min
argument_list|()
expr_stmt|;
name|max
operator|=
name|hashMap
operator|.
name|max
argument_list|()
expr_stmt|;
block|}
name|needHashTableSetup
operator|=
literal|false
expr_stmt|;
block|}
name|batchCounter
operator|++
expr_stmt|;
comment|// For outer join, DO NOT apply filters yet.  It is incorrect for outer join to
comment|// apply the filter before hash table matching.
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
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
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
comment|// We rebuild in-place the selected array with rows destine to be forwarded.
name|int
name|numSel
init|=
literal|0
decl_stmt|;
comment|/*        * Single-Column Long specific declarations.        */
comment|// The one join column for this specialized class.
name|LongColumnVector
name|joinColVector
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|singleJoinColumn
index|]
decl_stmt|;
name|long
index|[]
name|vector
init|=
name|joinColVector
operator|.
name|vector
decl_stmt|;
comment|/*        * Single-Column Long check for repeating.        */
comment|// Check single column for repeating.
name|boolean
name|allKeyInputColumnsRepeating
init|=
name|joinColVector
operator|.
name|isRepeating
decl_stmt|;
if|if
condition|(
name|allKeyInputColumnsRepeating
condition|)
block|{
comment|/*          * Repeating.          */
comment|// All key input columns are repeating.  Generate key once.  Lookup once.
comment|// Since the key is repeated, we must use entry 0 regardless of selectedInUse.
comment|/*          * Single-Column Long specific repeated lookup.          */
name|JoinUtil
operator|.
name|JoinResult
name|joinResult
decl_stmt|;
if|if
condition|(
operator|!
name|joinColVector
operator|.
name|noNulls
operator|&&
name|joinColVector
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
comment|// Null key is no match for whole batch.
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
comment|// Handle *repeated* join key, if found.
name|long
name|key
init|=
name|vector
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|useMinMax
operator|&&
operator|(
name|key
argument_list|<
name|min
operator|||
name|key
argument_list|>
name|max
operator|)
condition|)
block|{
comment|// Out of range for whole batch.
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
name|joinResult
operator|=
name|hashMap
operator|.
name|lookup
argument_list|(
name|key
argument_list|,
name|hashMapResults
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*          * Common repeated join result processing.          */
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
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
name|numSel
operator|=
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
name|scratch1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|/*          * NOT Repeating.          */
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
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
comment|// For outer join we must apply the filter after match and cause some matches to become
comment|// non-matches, we do not track non-matches here.  Instead we remember all non spilled rows
comment|// and compute non matches later in finishOuter.
name|int
name|hashMapResultCount
init|=
literal|0
decl_stmt|;
name|int
name|matchCount
init|=
literal|0
decl_stmt|;
name|int
name|nonSpillCount
init|=
literal|0
decl_stmt|;
name|int
name|spillCount
init|=
literal|0
decl_stmt|;
comment|/*          * Single-Column Long specific variables.          */
name|long
name|saveKey
init|=
literal|0
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
name|inputLogicalSize
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
comment|/*            * Single-Column Long outer null detection.            */
name|boolean
name|isNull
init|=
operator|!
name|joinColVector
operator|.
name|noNulls
operator|&&
name|joinColVector
operator|.
name|isNull
index|[
name|batchIndex
index|]
decl_stmt|;
if|if
condition|(
name|isNull
condition|)
block|{
comment|// Have that the NULL does not interfere with the current equal key series, if there
comment|// is one. We do not set saveJoinResult.
comment|//
comment|//    Let a current MATCH equal key series keep going, or
comment|//    Let a current SPILL equal key series keep going, or
comment|//    Let a current NOMATCH keep not matching.
comment|// Remember non-matches for Outer Join.
name|nonSpills
index|[
name|nonSpillCount
operator|++
index|]
operator|=
name|batchIndex
expr_stmt|;
comment|// LOG.debug(CLASS_NAME + " logical " + logical + " batchIndex " + batchIndex + " NULL");
block|}
else|else
block|{
comment|/*              * Single-Column Long outer get key.              */
name|long
name|currentKey
init|=
name|vector
index|[
name|batchIndex
index|]
decl_stmt|;
comment|/*              * Equal key series checking.              */
if|if
condition|(
operator|!
name|haveSaveKey
operator|||
name|currentKey
operator|!=
name|saveKey
condition|)
block|{
comment|// New key.
if|if
condition|(
name|haveSaveKey
condition|)
block|{
comment|// Move on with our count(s).
switch|switch
condition|(
name|saveJoinResult
condition|)
block|{
case|case
name|MATCH
case|:
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
comment|/*                * Single-Column Long specific save key.                */
name|saveKey
operator|=
name|currentKey
expr_stmt|;
comment|/*                * Single-Column Long specific lookup key.                */
if|if
condition|(
name|useMinMax
operator|&&
operator|(
name|currentKey
argument_list|<
name|min
operator|||
name|currentKey
argument_list|>
name|max
operator|)
condition|)
block|{
comment|// Key out of range for whole hash table.
name|saveJoinResult
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
name|saveJoinResult
operator|=
name|hashMap
operator|.
name|lookup
argument_list|(
name|currentKey
argument_list|,
name|hashMapResults
index|[
name|hashMapResultCount
index|]
argument_list|)
expr_stmt|;
block|}
comment|// LOG.debug(CLASS_NAME + " logical " + logical + " batchIndex " + batchIndex + " New Key " + saveJoinResult.name());
block|}
else|else
block|{
comment|// LOG.debug(CLASS_NAME + " logical " + logical + " batchIndex " + batchIndex + " Key Continues " + saveJoinResult.name());
block|}
comment|/*              * Common outer join result processing.              */
switch|switch
condition|(
name|saveJoinResult
condition|)
block|{
case|case
name|MATCH
case|:
name|matchs
index|[
name|matchCount
index|]
operator|=
name|batchIndex
expr_stmt|;
name|matchHashMapResultIndices
index|[
name|matchCount
index|]
operator|=
name|hashMapResultCount
expr_stmt|;
name|matchCount
operator|++
expr_stmt|;
name|nonSpills
index|[
name|nonSpillCount
operator|++
index|]
operator|=
name|batchIndex
expr_stmt|;
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
name|nonSpills
index|[
name|nonSpillCount
operator|++
index|]
operator|=
name|batchIndex
expr_stmt|;
comment|// VectorizedBatchUtil.debugDisplayOneRow(batch, batchIndex, CLASS_NAME + " NOMATCH duplicate");
break|break;
block|}
block|}
block|}
if|if
condition|(
name|haveSaveKey
condition|)
block|{
comment|// Account for last equal key sequence.
switch|switch
condition|(
name|saveJoinResult
condition|)
block|{
case|case
name|MATCH
case|:
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
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
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
literal|" matchs "
operator|+
name|intArrayToRangesString
argument_list|(
name|matchs
argument_list|,
name|matchCount
argument_list|)
operator|+
literal|" matchHashMapResultIndices "
operator|+
name|intArrayToRangesString
argument_list|(
name|matchHashMapResultIndices
argument_list|,
name|matchCount
argument_list|)
operator|+
literal|" nonSpills "
operator|+
name|intArrayToRangesString
argument_list|(
name|nonSpills
argument_list|,
name|nonSpillCount
argument_list|)
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
comment|// Note that scratch1 is undefined at this point -- it's preallocated storage.
name|numSel
operator|=
name|finishOuter
argument_list|(
name|batch
argument_list|,
name|matchs
argument_list|,
name|matchHashMapResultIndices
argument_list|,
name|matchCount
argument_list|,
name|nonSpills
argument_list|,
name|nonSpillCount
argument_list|,
name|spills
argument_list|,
name|spillHashMapResultIndices
argument_list|,
name|spillCount
argument_list|,
name|hashMapResults
argument_list|,
name|hashMapResultCount
argument_list|,
name|scratch1
argument_list|)
expr_stmt|;
block|}
name|batch
operator|.
name|selectedInUse
operator|=
literal|true
expr_stmt|;
name|batch
operator|.
name|size
operator|=
name|numSel
expr_stmt|;
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

