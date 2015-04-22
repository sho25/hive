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
package|;
end_package

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

begin_comment
comment|/**  * This class copies specified columns of a row from one VectorizedRowBatch to another.  */
end_comment

begin_class
specifier|public
class|class
name|VectorCopyRow
block|{
specifier|protected
specifier|static
specifier|transient
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|VectorCopyRow
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|abstract
class|class
name|CopyRow
block|{
specifier|protected
name|int
name|inColumnIndex
decl_stmt|;
specifier|protected
name|int
name|outColumnIndex
decl_stmt|;
name|CopyRow
parameter_list|(
name|int
name|inColumnIndex
parameter_list|,
name|int
name|outColumnIndex
parameter_list|)
block|{
name|this
operator|.
name|inColumnIndex
operator|=
name|inColumnIndex
expr_stmt|;
name|this
operator|.
name|outColumnIndex
operator|=
name|outColumnIndex
expr_stmt|;
block|}
specifier|abstract
name|void
name|copy
parameter_list|(
name|VectorizedRowBatch
name|inBatch
parameter_list|,
name|int
name|inBatchIndex
parameter_list|,
name|VectorizedRowBatch
name|outBatch
parameter_list|,
name|int
name|outBatchIndex
parameter_list|)
function_decl|;
block|}
specifier|private
class|class
name|LongCopyRow
extends|extends
name|CopyRow
block|{
name|LongCopyRow
parameter_list|(
name|int
name|inColumnIndex
parameter_list|,
name|int
name|outColumnIndex
parameter_list|)
block|{
name|super
argument_list|(
name|inColumnIndex
argument_list|,
name|outColumnIndex
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|copy
parameter_list|(
name|VectorizedRowBatch
name|inBatch
parameter_list|,
name|int
name|inBatchIndex
parameter_list|,
name|VectorizedRowBatch
name|outBatch
parameter_list|,
name|int
name|outBatchIndex
parameter_list|)
block|{
name|LongColumnVector
name|inColVector
init|=
operator|(
name|LongColumnVector
operator|)
name|inBatch
operator|.
name|cols
index|[
name|inColumnIndex
index|]
decl_stmt|;
name|LongColumnVector
name|outColVector
init|=
operator|(
name|LongColumnVector
operator|)
name|outBatch
operator|.
name|cols
index|[
name|outColumnIndex
index|]
decl_stmt|;
if|if
condition|(
name|inColVector
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|inColVector
operator|.
name|noNulls
operator|||
operator|!
name|inColVector
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|outColVector
operator|.
name|vector
index|[
name|outBatchIndex
index|]
operator|=
name|inColVector
operator|.
name|vector
index|[
literal|0
index|]
expr_stmt|;
block|}
else|else
block|{
name|VectorizedBatchUtil
operator|.
name|setNullColIsNullValue
argument_list|(
name|outColVector
argument_list|,
name|outBatchIndex
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|inColVector
operator|.
name|noNulls
operator|||
operator|!
name|inColVector
operator|.
name|isNull
index|[
name|inBatchIndex
index|]
condition|)
block|{
name|outColVector
operator|.
name|vector
index|[
name|outBatchIndex
index|]
operator|=
name|inColVector
operator|.
name|vector
index|[
name|inBatchIndex
index|]
expr_stmt|;
block|}
else|else
block|{
name|VectorizedBatchUtil
operator|.
name|setNullColIsNullValue
argument_list|(
name|outColVector
argument_list|,
name|outBatchIndex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
class|class
name|DoubleCopyRow
extends|extends
name|CopyRow
block|{
name|DoubleCopyRow
parameter_list|(
name|int
name|inColumnIndex
parameter_list|,
name|int
name|outColumnIndex
parameter_list|)
block|{
name|super
argument_list|(
name|inColumnIndex
argument_list|,
name|outColumnIndex
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|copy
parameter_list|(
name|VectorizedRowBatch
name|inBatch
parameter_list|,
name|int
name|inBatchIndex
parameter_list|,
name|VectorizedRowBatch
name|outBatch
parameter_list|,
name|int
name|outBatchIndex
parameter_list|)
block|{
name|DoubleColumnVector
name|inColVector
init|=
operator|(
name|DoubleColumnVector
operator|)
name|inBatch
operator|.
name|cols
index|[
name|inColumnIndex
index|]
decl_stmt|;
name|DoubleColumnVector
name|outColVector
init|=
operator|(
name|DoubleColumnVector
operator|)
name|outBatch
operator|.
name|cols
index|[
name|outColumnIndex
index|]
decl_stmt|;
if|if
condition|(
name|inColVector
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|inColVector
operator|.
name|noNulls
operator|||
operator|!
name|inColVector
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|outColVector
operator|.
name|vector
index|[
name|outBatchIndex
index|]
operator|=
name|inColVector
operator|.
name|vector
index|[
literal|0
index|]
expr_stmt|;
block|}
else|else
block|{
name|VectorizedBatchUtil
operator|.
name|setNullColIsNullValue
argument_list|(
name|outColVector
argument_list|,
name|outBatchIndex
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|inColVector
operator|.
name|noNulls
operator|||
operator|!
name|inColVector
operator|.
name|isNull
index|[
name|inBatchIndex
index|]
condition|)
block|{
name|outColVector
operator|.
name|vector
index|[
name|outBatchIndex
index|]
operator|=
name|inColVector
operator|.
name|vector
index|[
name|inBatchIndex
index|]
expr_stmt|;
block|}
else|else
block|{
name|VectorizedBatchUtil
operator|.
name|setNullColIsNullValue
argument_list|(
name|outColVector
argument_list|,
name|outBatchIndex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
specifier|abstract
class|class
name|AbstractBytesCopyRow
extends|extends
name|CopyRow
block|{
name|AbstractBytesCopyRow
parameter_list|(
name|int
name|inColumnIndex
parameter_list|,
name|int
name|outColumnIndex
parameter_list|)
block|{
name|super
argument_list|(
name|inColumnIndex
argument_list|,
name|outColumnIndex
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|BytesCopyRowByValue
extends|extends
name|AbstractBytesCopyRow
block|{
name|BytesCopyRowByValue
parameter_list|(
name|int
name|inColumnIndex
parameter_list|,
name|int
name|outColumnIndex
parameter_list|)
block|{
name|super
argument_list|(
name|inColumnIndex
argument_list|,
name|outColumnIndex
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|copy
parameter_list|(
name|VectorizedRowBatch
name|inBatch
parameter_list|,
name|int
name|inBatchIndex
parameter_list|,
name|VectorizedRowBatch
name|outBatch
parameter_list|,
name|int
name|outBatchIndex
parameter_list|)
block|{
name|BytesColumnVector
name|inColVector
init|=
operator|(
name|BytesColumnVector
operator|)
name|inBatch
operator|.
name|cols
index|[
name|inColumnIndex
index|]
decl_stmt|;
name|BytesColumnVector
name|outColVector
init|=
operator|(
name|BytesColumnVector
operator|)
name|outBatch
operator|.
name|cols
index|[
name|outColumnIndex
index|]
decl_stmt|;
if|if
condition|(
name|inColVector
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|inColVector
operator|.
name|noNulls
operator|||
operator|!
name|inColVector
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|outColVector
operator|.
name|setVal
argument_list|(
name|outBatchIndex
argument_list|,
name|inColVector
operator|.
name|vector
index|[
literal|0
index|]
argument_list|,
name|inColVector
operator|.
name|start
index|[
literal|0
index|]
argument_list|,
name|inColVector
operator|.
name|length
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|VectorizedBatchUtil
operator|.
name|setNullColIsNullValue
argument_list|(
name|outColVector
argument_list|,
name|outBatchIndex
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|inColVector
operator|.
name|noNulls
operator|||
operator|!
name|inColVector
operator|.
name|isNull
index|[
name|inBatchIndex
index|]
condition|)
block|{
name|outColVector
operator|.
name|setVal
argument_list|(
name|outBatchIndex
argument_list|,
name|inColVector
operator|.
name|vector
index|[
name|inBatchIndex
index|]
argument_list|,
name|inColVector
operator|.
name|start
index|[
name|inBatchIndex
index|]
argument_list|,
name|inColVector
operator|.
name|length
index|[
name|inBatchIndex
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|VectorizedBatchUtil
operator|.
name|setNullColIsNullValue
argument_list|(
name|outColVector
argument_list|,
name|outBatchIndex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
class|class
name|BytesCopyRowByReference
extends|extends
name|AbstractBytesCopyRow
block|{
name|BytesCopyRowByReference
parameter_list|(
name|int
name|inColumnIndex
parameter_list|,
name|int
name|outColumnIndex
parameter_list|)
block|{
name|super
argument_list|(
name|inColumnIndex
argument_list|,
name|outColumnIndex
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|copy
parameter_list|(
name|VectorizedRowBatch
name|inBatch
parameter_list|,
name|int
name|inBatchIndex
parameter_list|,
name|VectorizedRowBatch
name|outBatch
parameter_list|,
name|int
name|outBatchIndex
parameter_list|)
block|{
name|BytesColumnVector
name|inColVector
init|=
operator|(
name|BytesColumnVector
operator|)
name|inBatch
operator|.
name|cols
index|[
name|inColumnIndex
index|]
decl_stmt|;
name|BytesColumnVector
name|outColVector
init|=
operator|(
name|BytesColumnVector
operator|)
name|outBatch
operator|.
name|cols
index|[
name|outColumnIndex
index|]
decl_stmt|;
if|if
condition|(
name|inColVector
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|inColVector
operator|.
name|noNulls
operator|||
operator|!
name|inColVector
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|outColVector
operator|.
name|setRef
argument_list|(
name|outBatchIndex
argument_list|,
name|inColVector
operator|.
name|vector
index|[
literal|0
index|]
argument_list|,
name|inColVector
operator|.
name|start
index|[
literal|0
index|]
argument_list|,
name|inColVector
operator|.
name|length
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|VectorizedBatchUtil
operator|.
name|setNullColIsNullValue
argument_list|(
name|outColVector
argument_list|,
name|outBatchIndex
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|inColVector
operator|.
name|noNulls
operator|||
operator|!
name|inColVector
operator|.
name|isNull
index|[
name|inBatchIndex
index|]
condition|)
block|{
name|outColVector
operator|.
name|setRef
argument_list|(
name|outBatchIndex
argument_list|,
name|inColVector
operator|.
name|vector
index|[
name|inBatchIndex
index|]
argument_list|,
name|inColVector
operator|.
name|start
index|[
name|inBatchIndex
index|]
argument_list|,
name|inColVector
operator|.
name|length
index|[
name|inBatchIndex
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|VectorizedBatchUtil
operator|.
name|setNullColIsNullValue
argument_list|(
name|outColVector
argument_list|,
name|outBatchIndex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
class|class
name|DecimalCopyRow
extends|extends
name|CopyRow
block|{
name|DecimalCopyRow
parameter_list|(
name|int
name|inColumnIndex
parameter_list|,
name|int
name|outColumnIndex
parameter_list|)
block|{
name|super
argument_list|(
name|inColumnIndex
argument_list|,
name|outColumnIndex
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|void
name|copy
parameter_list|(
name|VectorizedRowBatch
name|inBatch
parameter_list|,
name|int
name|inBatchIndex
parameter_list|,
name|VectorizedRowBatch
name|outBatch
parameter_list|,
name|int
name|outBatchIndex
parameter_list|)
block|{
name|DecimalColumnVector
name|inColVector
init|=
operator|(
name|DecimalColumnVector
operator|)
name|inBatch
operator|.
name|cols
index|[
name|inColumnIndex
index|]
decl_stmt|;
name|DecimalColumnVector
name|outColVector
init|=
operator|(
name|DecimalColumnVector
operator|)
name|outBatch
operator|.
name|cols
index|[
name|outColumnIndex
index|]
decl_stmt|;
if|if
condition|(
name|inColVector
operator|.
name|isRepeating
condition|)
block|{
if|if
condition|(
name|inColVector
operator|.
name|noNulls
operator|||
operator|!
name|inColVector
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|outColVector
operator|.
name|set
argument_list|(
name|outBatchIndex
argument_list|,
name|inColVector
operator|.
name|vector
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|VectorizedBatchUtil
operator|.
name|setNullColIsNullValue
argument_list|(
name|outColVector
argument_list|,
name|outBatchIndex
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|inColVector
operator|.
name|noNulls
operator|||
operator|!
name|inColVector
operator|.
name|isNull
index|[
name|inBatchIndex
index|]
condition|)
block|{
name|outColVector
operator|.
name|set
argument_list|(
name|outBatchIndex
argument_list|,
name|inColVector
operator|.
name|vector
index|[
name|inBatchIndex
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|VectorizedBatchUtil
operator|.
name|setNullColIsNullValue
argument_list|(
name|outColVector
argument_list|,
name|outBatchIndex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|CopyRow
index|[]
name|subRowToBatchCopiersByValue
decl_stmt|;
specifier|private
name|CopyRow
index|[]
name|subRowToBatchCopiersByReference
decl_stmt|;
specifier|public
name|void
name|init
parameter_list|(
name|VectorColumnMapping
name|columnMapping
parameter_list|)
block|{
name|int
name|count
init|=
name|columnMapping
operator|.
name|getCount
argument_list|()
decl_stmt|;
name|subRowToBatchCopiersByValue
operator|=
operator|new
name|CopyRow
index|[
name|count
index|]
expr_stmt|;
name|subRowToBatchCopiersByReference
operator|=
operator|new
name|CopyRow
index|[
name|count
index|]
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|int
name|inputColumn
init|=
name|columnMapping
operator|.
name|getInputColumns
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|int
name|outputColumn
init|=
name|columnMapping
operator|.
name|getOutputColumns
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|String
name|typeName
init|=
name|columnMapping
operator|.
name|getTypeNames
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|CopyRow
name|copyRowByValue
init|=
literal|null
decl_stmt|;
name|CopyRow
name|copyRowByReference
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|VectorizationContext
operator|.
name|isIntFamily
argument_list|(
name|typeName
argument_list|)
operator|||
name|VectorizationContext
operator|.
name|isDatetimeFamily
argument_list|(
name|typeName
argument_list|)
condition|)
block|{
name|copyRowByValue
operator|=
operator|new
name|LongCopyRow
argument_list|(
name|inputColumn
argument_list|,
name|outputColumn
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|VectorizationContext
operator|.
name|isFloatFamily
argument_list|(
name|typeName
argument_list|)
condition|)
block|{
name|copyRowByValue
operator|=
operator|new
name|DoubleCopyRow
argument_list|(
name|inputColumn
argument_list|,
name|outputColumn
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|VectorizationContext
operator|.
name|isStringFamily
argument_list|(
name|typeName
argument_list|)
condition|)
block|{
name|copyRowByValue
operator|=
operator|new
name|BytesCopyRowByValue
argument_list|(
name|inputColumn
argument_list|,
name|outputColumn
argument_list|)
expr_stmt|;
name|copyRowByReference
operator|=
operator|new
name|BytesCopyRowByReference
argument_list|(
name|inputColumn
argument_list|,
name|outputColumn
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|VectorizationContext
operator|.
name|decimalTypePattern
operator|.
name|matcher
argument_list|(
name|typeName
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
name|copyRowByValue
operator|=
operator|new
name|DecimalCopyRow
argument_list|(
name|inputColumn
argument_list|,
name|outputColumn
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot allocate vector copy row for "
operator|+
name|typeName
argument_list|)
throw|;
block|}
name|subRowToBatchCopiersByValue
index|[
name|i
index|]
operator|=
name|copyRowByValue
expr_stmt|;
if|if
condition|(
name|copyRowByReference
operator|==
literal|null
condition|)
block|{
name|subRowToBatchCopiersByReference
index|[
name|i
index|]
operator|=
name|copyRowByValue
expr_stmt|;
block|}
else|else
block|{
name|subRowToBatchCopiersByReference
index|[
name|i
index|]
operator|=
name|copyRowByReference
expr_stmt|;
block|}
block|}
block|}
comment|/*    * Use this copy method when the source batch may get reused before the target batch is finished.    * Any bytes column vector values will be copied to the target by value into the column's    * data buffer.    */
specifier|public
name|void
name|copyByValue
parameter_list|(
name|VectorizedRowBatch
name|inBatch
parameter_list|,
name|int
name|inBatchIndex
parameter_list|,
name|VectorizedRowBatch
name|outBatch
parameter_list|,
name|int
name|outBatchIndex
parameter_list|)
block|{
for|for
control|(
name|CopyRow
name|copyRow
range|:
name|subRowToBatchCopiersByValue
control|)
block|{
name|copyRow
operator|.
name|copy
argument_list|(
name|inBatch
argument_list|,
name|inBatchIndex
argument_list|,
name|outBatch
argument_list|,
name|outBatchIndex
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*    * Use this copy method when the source batch is safe and will remain around until the target    * batch is finished.    *    * Any bytes column vector values will be referenced by the target column instead of copying.    */
specifier|public
name|void
name|copyByReference
parameter_list|(
name|VectorizedRowBatch
name|inBatch
parameter_list|,
name|int
name|inBatchIndex
parameter_list|,
name|VectorizedRowBatch
name|outBatch
parameter_list|,
name|int
name|outBatchIndex
parameter_list|)
block|{
for|for
control|(
name|CopyRow
name|copyRow
range|:
name|subRowToBatchCopiersByReference
control|)
block|{
name|copyRow
operator|.
name|copy
argument_list|(
name|inBatch
argument_list|,
name|inBatchIndex
argument_list|,
name|outBatch
argument_list|,
name|outBatchIndex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

