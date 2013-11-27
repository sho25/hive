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
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|HashMap
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|VectorExpressionWriter
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
name|io
operator|.
name|NullWritable
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
name|Writable
import|;
end_import

begin_comment
comment|/**  * A VectorizedRowBatch is a set of rows, organized with each column  * as a vector. It is the unit of query execution, organized to minimize  * the cost per row and achieve high cycles-per-instruction.  * The major fields are public by design to allow fast and convenient  * access by the vectorized query execution code.  */
end_comment

begin_class
specifier|public
class|class
name|VectorizedRowBatch
implements|implements
name|Writable
block|{
specifier|public
name|int
name|numCols
decl_stmt|;
comment|// number of columns
specifier|public
name|ColumnVector
index|[]
name|cols
decl_stmt|;
comment|// a vector for each column
specifier|public
name|int
name|size
decl_stmt|;
comment|// number of rows that qualify (i.e. haven't been filtered out)
specifier|public
name|int
index|[]
name|selected
decl_stmt|;
comment|// array of positions of selected values
specifier|public
name|int
index|[]
name|projectedColumns
decl_stmt|;
specifier|public
name|int
name|projectionSize
decl_stmt|;
comment|/*    * If no filtering has been applied yet, selectedInUse is false,    * meaning that all rows qualify. If it is true, then the selected[] array    * records the offsets of qualifying rows.    */
specifier|public
name|boolean
name|selectedInUse
decl_stmt|;
comment|// If this is true, then there is no data in the batch -- we have hit the end of input.
specifier|public
name|boolean
name|endOfFile
decl_stmt|;
comment|/*    * This number is carefully chosen to minimize overhead and typically allows    * one VectorizedRowBatch to fit in cache.    */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_SIZE
init|=
literal|1024
decl_stmt|;
specifier|public
name|VectorExpressionWriter
index|[]
name|valueWriters
init|=
literal|null
decl_stmt|;
comment|/**    * Return a batch with the specified number of columns.    * This is the standard constructor -- all batches should be the same size    *    * @param numCols the number of columns to include in the batch    */
specifier|public
name|VectorizedRowBatch
parameter_list|(
name|int
name|numCols
parameter_list|)
block|{
name|this
argument_list|(
name|numCols
argument_list|,
name|DEFAULT_SIZE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Return a batch with the specified number of columns and rows.    * Only call this constructor directly for testing purposes.    * Batch size should normally always be defaultSize.    *    * @param numCols the number of columns to include in the batch    * @param size  the number of rows to include in the batch    */
specifier|public
name|VectorizedRowBatch
parameter_list|(
name|int
name|numCols
parameter_list|,
name|int
name|size
parameter_list|)
block|{
name|this
operator|.
name|numCols
operator|=
name|numCols
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|selected
operator|=
operator|new
name|int
index|[
name|size
index|]
expr_stmt|;
name|selectedInUse
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|cols
operator|=
operator|new
name|ColumnVector
index|[
name|numCols
index|]
expr_stmt|;
name|projectedColumns
operator|=
operator|new
name|int
index|[
name|numCols
index|]
expr_stmt|;
comment|// Initially all columns are projected and in the same order
name|projectionSize
operator|=
name|numCols
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
name|numCols
condition|;
name|i
operator|++
control|)
block|{
name|projectedColumns
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
comment|/**    * Return count of qualifying rows.    *    * @return number of rows that have not been filtered out    */
specifier|public
name|long
name|count
parameter_list|()
block|{
return|return
name|size
return|;
block|}
specifier|private
name|String
name|toUTF8
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|o
operator|instanceof
name|NullWritable
condition|)
block|{
return|return
literal|"\\N"
return|;
comment|/* as found in LazySimpleSerDe's nullSequence */
block|}
return|return
name|o
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
return|return
literal|""
return|;
block|}
name|StringBuilder
name|b
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
name|this
operator|.
name|selectedInUse
condition|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|size
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|selected
index|[
name|j
index|]
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
name|projectionSize
condition|;
name|k
operator|++
control|)
block|{
name|int
name|projIndex
init|=
name|projectedColumns
index|[
name|k
index|]
decl_stmt|;
name|ColumnVector
name|cv
init|=
name|cols
index|[
name|projIndex
index|]
decl_stmt|;
if|if
condition|(
name|k
operator|>
literal|0
condition|)
block|{
name|b
operator|.
name|append
argument_list|(
literal|'\u0001'
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cv
operator|.
name|isRepeating
condition|)
block|{
name|b
operator|.
name|append
argument_list|(
name|toUTF8
argument_list|(
name|valueWriters
index|[
name|k
index|]
operator|.
name|writeValue
argument_list|(
name|cv
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|b
operator|.
name|append
argument_list|(
name|toUTF8
argument_list|(
name|valueWriters
index|[
name|k
index|]
operator|.
name|writeValue
argument_list|(
name|cv
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|j
operator|<
name|size
operator|-
literal|1
condition|)
block|{
name|b
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
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
name|size
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|projectionSize
condition|;
name|k
operator|++
control|)
block|{
name|int
name|projIndex
init|=
name|projectedColumns
index|[
name|k
index|]
decl_stmt|;
name|ColumnVector
name|cv
init|=
name|cols
index|[
name|projIndex
index|]
decl_stmt|;
if|if
condition|(
name|k
operator|>
literal|0
condition|)
block|{
name|b
operator|.
name|append
argument_list|(
literal|'\u0001'
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cv
operator|.
name|isRepeating
condition|)
block|{
name|b
operator|.
name|append
argument_list|(
name|toUTF8
argument_list|(
name|valueWriters
index|[
name|k
index|]
operator|.
name|writeValue
argument_list|(
name|cv
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|b
operator|.
name|append
argument_list|(
name|toUTF8
argument_list|(
name|valueWriters
index|[
name|k
index|]
operator|.
name|writeValue
argument_list|(
name|cv
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|i
operator|<
name|size
operator|-
literal|1
condition|)
block|{
name|b
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
return|return
name|b
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|arg0
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Do you really need me?"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|arg0
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Don't call me"
argument_list|)
throw|;
block|}
specifier|public
name|void
name|setValueWriters
parameter_list|(
name|VectorExpressionWriter
index|[]
name|valueWriters
parameter_list|)
block|{
name|this
operator|.
name|valueWriters
operator|=
name|valueWriters
expr_stmt|;
block|}
comment|/**    * Resets the row batch to default state    *  - sets selectedInUse to false    *  - sets size to 0    *  - sets endOfFile to false    *  - resets each column    */
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|selectedInUse
operator|=
literal|false
expr_stmt|;
name|size
operator|=
literal|0
expr_stmt|;
name|endOfFile
operator|=
literal|false
expr_stmt|;
for|for
control|(
name|ColumnVector
name|vc
range|:
name|cols
control|)
block|{
if|if
condition|(
name|vc
operator|!=
literal|null
condition|)
block|{
name|vc
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

