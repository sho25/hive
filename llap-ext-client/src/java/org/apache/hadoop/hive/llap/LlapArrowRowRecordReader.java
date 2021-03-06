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
name|llap
package|;
end_package

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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|arrow
operator|.
name|vector
operator|.
name|FieldVector
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
name|conf
operator|.
name|Configuration
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
name|arrow
operator|.
name|ArrowColumnarBatchSerDe
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
name|arrow
operator|.
name|ArrowWrapperWritable
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
name|AbstractSerDe
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
name|SerDeException
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
name|objectinspector
operator|.
name|StructObjectInspector
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|RecordReader
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
name|List
import|;
end_import

begin_comment
comment|/**  * Buffers a batch for reading one row at a time.  */
end_comment

begin_class
specifier|public
class|class
name|LlapArrowRowRecordReader
extends|extends
name|LlapRowRecordReader
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
name|LlapArrowRowRecordReader
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|int
name|rowIndex
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|batchSize
init|=
literal|0
decl_stmt|;
comment|//Buffer one batch at a time, for row retrieval
specifier|private
name|Object
index|[]
index|[]
name|currentBatch
decl_stmt|;
specifier|public
name|LlapArrowRowRecordReader
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Schema
name|schema
parameter_list|,
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|?
extends|extends
name|Writable
argument_list|>
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|schema
argument_list|,
name|reader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|NullWritable
name|key
parameter_list|,
name|Row
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|value
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|boolean
name|hasNext
init|=
literal|false
decl_stmt|;
name|ArrowWrapperWritable
name|batchData
init|=
operator|(
name|ArrowWrapperWritable
operator|)
name|data
decl_stmt|;
if|if
condition|(
operator|(
name|batchSize
operator|==
literal|0
operator|)
operator|||
operator|(
name|rowIndex
operator|==
name|batchSize
operator|)
condition|)
block|{
comment|//This is either the first batch or we've used up the current batch buffer
name|batchSize
operator|=
literal|0
expr_stmt|;
name|rowIndex
operator|=
literal|0
expr_stmt|;
comment|// since HIVE-22856, a zero length batch doesn't mean that we won't have any more batches
comment|// we can have more batches with data even after after a zero length batch
comment|// we should keep trying until we get a batch with some data or reader.next() returns false
while|while
condition|(
name|batchSize
operator|==
literal|0
operator|&&
operator|(
name|hasNext
operator|=
name|reader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|data
argument_list|)
operator|)
condition|)
block|{
name|List
argument_list|<
name|FieldVector
argument_list|>
name|vectors
init|=
name|batchData
operator|.
name|getVectorSchemaRoot
argument_list|()
operator|.
name|getFieldVectors
argument_list|()
decl_stmt|;
comment|//hasNext implies there is some column in the batch
name|Preconditions
operator|.
name|checkState
argument_list|(
name|vectors
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
comment|//All the vectors have the same length,
comment|//we can get the number of rows from the first vector
name|batchSize
operator|=
name|vectors
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getValueCount
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|hasNext
condition|)
block|{
comment|//There is another batch to buffer
try|try
block|{
name|ArrowWrapperWritable
name|wrapper
init|=
operator|new
name|ArrowWrapperWritable
argument_list|(
name|batchData
operator|.
name|getVectorSchemaRoot
argument_list|()
argument_list|)
decl_stmt|;
name|currentBatch
operator|=
operator|(
name|Object
index|[]
index|[]
operator|)
name|serde
operator|.
name|deserialize
argument_list|(
name|wrapper
argument_list|)
expr_stmt|;
name|StructObjectInspector
name|rowOI
init|=
operator|(
name|StructObjectInspector
operator|)
name|serde
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|setRowFromStruct
argument_list|(
name|value
argument_list|,
name|currentBatch
index|[
name|rowIndex
index|]
argument_list|,
name|rowOI
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to fetch Arrow batch"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|//There were no more batches AND
comment|//this is either the first batch or we've used up the current batch buffer.
comment|//goto return false
block|}
elseif|else
if|if
condition|(
name|rowIndex
operator|<
name|batchSize
condition|)
block|{
comment|//Take a row from the current buffered batch
name|hasNext
operator|=
literal|true
expr_stmt|;
name|StructObjectInspector
name|rowOI
init|=
literal|null
decl_stmt|;
try|try
block|{
name|rowOI
operator|=
operator|(
name|StructObjectInspector
operator|)
name|serde
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|setRowFromStruct
argument_list|(
name|value
argument_list|,
name|currentBatch
index|[
name|rowIndex
index|]
argument_list|,
name|rowOI
argument_list|)
expr_stmt|;
block|}
comment|//Always inc the batch buffer index
comment|//If we return false, it is just a noop
name|rowIndex
operator|++
expr_stmt|;
return|return
name|hasNext
return|;
block|}
specifier|protected
name|AbstractSerDe
name|createSerDe
parameter_list|()
throws|throws
name|SerDeException
block|{
return|return
operator|new
name|ArrowColumnarBatchSerDe
argument_list|()
return|;
block|}
block|}
end_class

end_unit

