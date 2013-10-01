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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
name|DoubleColumnVector
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
name|LongColumnVector
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
name|TimestampUtils
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
name|metadata
operator|.
name|HiveException
import|;
end_import

begin_comment
comment|/**  * Test helper class that creates vectorized execution batches from arbitrary type iterables.  */
end_comment

begin_class
specifier|public
class|class
name|FakeVectorRowBatchFromObjectIterables
extends|extends
name|FakeVectorRowBatchBase
block|{
specifier|private
specifier|final
name|String
index|[]
name|types
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|Iterator
argument_list|<
name|Object
argument_list|>
argument_list|>
name|iterators
decl_stmt|;
specifier|private
specifier|final
name|VectorizedRowBatch
name|batch
decl_stmt|;
specifier|private
name|boolean
name|eof
decl_stmt|;
specifier|private
specifier|final
name|int
name|batchSize
decl_stmt|;
specifier|public
name|String
index|[]
name|getTypes
parameter_list|()
block|{
return|return
name|this
operator|.
name|types
return|;
block|}
comment|/**    * Helper interface for assigning values to primitive vector column types.    */
specifier|private
specifier|static
interface|interface
name|ColumnVectorAssign
block|{
name|void
name|assign
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|row
parameter_list|,
name|Object
name|value
parameter_list|)
function_decl|;
block|}
specifier|private
specifier|final
name|ColumnVectorAssign
index|[]
name|columnAssign
decl_stmt|;
specifier|public
name|FakeVectorRowBatchFromObjectIterables
parameter_list|(
name|int
name|batchSize
parameter_list|,
name|String
index|[]
name|types
parameter_list|,
name|Iterable
argument_list|<
name|Object
argument_list|>
modifier|...
name|iterables
parameter_list|)
throws|throws
name|HiveException
block|{
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
name|this
operator|.
name|batchSize
operator|=
name|batchSize
expr_stmt|;
name|iterators
operator|=
operator|new
name|ArrayList
argument_list|<
name|Iterator
argument_list|<
name|Object
argument_list|>
argument_list|>
argument_list|(
name|types
operator|.
name|length
argument_list|)
expr_stmt|;
name|columnAssign
operator|=
operator|new
name|ColumnVectorAssign
index|[
name|types
operator|.
name|length
index|]
expr_stmt|;
name|batch
operator|=
operator|new
name|VectorizedRowBatch
argument_list|(
name|types
operator|.
name|length
argument_list|,
name|batchSize
argument_list|)
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
name|types
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|types
index|[
name|i
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"tinyint"
argument_list|)
operator|||
name|types
index|[
name|i
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"smallint"
argument_list|)
operator|||
name|types
index|[
name|i
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"int"
argument_list|)
operator|||
name|types
index|[
name|i
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"bigint"
argument_list|)
operator|||
name|types
index|[
name|i
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"long"
argument_list|)
condition|)
block|{
name|batch
operator|.
name|cols
index|[
name|i
index|]
operator|=
operator|new
name|LongColumnVector
argument_list|(
name|batchSize
argument_list|)
expr_stmt|;
name|columnAssign
index|[
name|i
index|]
operator|=
operator|new
name|ColumnVectorAssign
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|assign
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|row
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|columnVector
decl_stmt|;
name|lcv
operator|.
name|vector
index|[
name|row
index|]
operator|=
name|Long
operator|.
name|valueOf
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|types
index|[
name|i
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"boolean"
argument_list|)
condition|)
block|{
name|batch
operator|.
name|cols
index|[
name|i
index|]
operator|=
operator|new
name|LongColumnVector
argument_list|(
name|batchSize
argument_list|)
expr_stmt|;
name|columnAssign
index|[
name|i
index|]
operator|=
operator|new
name|ColumnVectorAssign
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|assign
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|row
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|columnVector
decl_stmt|;
name|lcv
operator|.
name|vector
index|[
name|row
index|]
operator|=
operator|(
name|Boolean
operator|)
name|value
condition|?
literal|1
else|:
literal|0
expr_stmt|;
block|}
block|}
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|types
index|[
name|i
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"timestamp"
argument_list|)
condition|)
block|{
name|batch
operator|.
name|cols
index|[
name|i
index|]
operator|=
operator|new
name|LongColumnVector
argument_list|(
name|batchSize
argument_list|)
expr_stmt|;
name|columnAssign
index|[
name|i
index|]
operator|=
operator|new
name|ColumnVectorAssign
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|assign
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|row
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|LongColumnVector
name|lcv
init|=
operator|(
name|LongColumnVector
operator|)
name|columnVector
decl_stmt|;
name|Timestamp
name|t
init|=
operator|(
name|Timestamp
operator|)
name|value
decl_stmt|;
name|lcv
operator|.
name|vector
index|[
name|row
index|]
operator|=
name|TimestampUtils
operator|.
name|getTimeNanoSec
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|types
index|[
name|i
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"string"
argument_list|)
condition|)
block|{
name|batch
operator|.
name|cols
index|[
name|i
index|]
operator|=
operator|new
name|BytesColumnVector
argument_list|(
name|batchSize
argument_list|)
expr_stmt|;
name|columnAssign
index|[
name|i
index|]
operator|=
operator|new
name|ColumnVectorAssign
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|assign
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|row
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|BytesColumnVector
name|bcv
init|=
operator|(
name|BytesColumnVector
operator|)
name|columnVector
decl_stmt|;
name|String
name|s
init|=
operator|(
name|String
operator|)
name|value
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|s
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|bcv
operator|.
name|vector
index|[
name|row
index|]
operator|=
name|bytes
expr_stmt|;
name|bcv
operator|.
name|start
index|[
name|row
index|]
operator|=
literal|0
expr_stmt|;
name|bcv
operator|.
name|length
index|[
name|row
index|]
operator|=
name|bytes
operator|.
name|length
expr_stmt|;
block|}
block|}
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|types
index|[
name|i
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"double"
argument_list|)
operator|||
name|types
index|[
name|i
index|]
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"float"
argument_list|)
condition|)
block|{
name|batch
operator|.
name|cols
index|[
name|i
index|]
operator|=
operator|new
name|DoubleColumnVector
argument_list|(
name|batchSize
argument_list|)
expr_stmt|;
name|columnAssign
index|[
name|i
index|]
operator|=
operator|new
name|ColumnVectorAssign
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|assign
parameter_list|(
name|ColumnVector
name|columnVector
parameter_list|,
name|int
name|row
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|DoubleColumnVector
name|dcv
init|=
operator|(
name|DoubleColumnVector
operator|)
name|columnVector
decl_stmt|;
name|dcv
operator|.
name|vector
index|[
name|row
index|]
operator|=
name|Double
operator|.
name|valueOf
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Unimplemented type "
operator|+
name|types
index|[
name|i
index|]
argument_list|)
throw|;
block|}
name|iterators
operator|.
name|add
argument_list|(
name|iterables
index|[
name|i
index|]
operator|.
name|iterator
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|VectorizedRowBatch
name|produceNextBatch
parameter_list|()
block|{
name|batch
operator|.
name|size
operator|=
literal|0
expr_stmt|;
name|batch
operator|.
name|selectedInUse
operator|=
literal|false
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
name|types
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|ColumnVector
name|col
init|=
name|batch
operator|.
name|cols
index|[
name|i
index|]
decl_stmt|;
name|col
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
name|col
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
block|}
while|while
condition|(
operator|!
name|eof
operator|&&
name|batch
operator|.
name|size
operator|<
name|this
operator|.
name|batchSize
condition|)
block|{
name|int
name|r
init|=
name|batch
operator|.
name|size
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
name|types
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|Iterator
argument_list|<
name|Object
argument_list|>
name|it
init|=
name|iterators
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|eof
operator|=
literal|true
expr_stmt|;
break|break;
block|}
name|Object
name|value
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|value
condition|)
block|{
name|batch
operator|.
name|cols
index|[
name|i
index|]
operator|.
name|isNull
index|[
name|batch
operator|.
name|size
index|]
operator|=
literal|true
expr_stmt|;
name|batch
operator|.
name|cols
index|[
name|i
index|]
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
comment|// Must reset the isNull, could be set from prev batch use
name|batch
operator|.
name|cols
index|[
name|i
index|]
operator|.
name|isNull
index|[
name|batch
operator|.
name|size
index|]
operator|=
literal|false
expr_stmt|;
name|columnAssign
index|[
name|i
index|]
operator|.
name|assign
argument_list|(
name|batch
operator|.
name|cols
index|[
name|i
index|]
argument_list|,
name|batch
operator|.
name|size
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|eof
condition|)
block|{
name|batch
operator|.
name|size
operator|+=
literal|1
expr_stmt|;
block|}
block|}
return|return
name|batch
return|;
block|}
block|}
end_class

end_unit

