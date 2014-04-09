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
name|io
operator|.
name|orc
package|;
end_package

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
name|exec
operator|.
name|vector
operator|.
name|VectorizedBatchUtil
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
name|VectorizedRowBatchCtx
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
name|AcidInputFormat
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
name|RecordIdentifier
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
name|ObjectInspector
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
name|DataOutputBuffer
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
name|mapred
operator|.
name|*
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

begin_comment
comment|/**  * Implement a RecordReader that stitches together base and delta files to  * support tables and partitions stored in the ACID format. It works by using  * the non-vectorized ACID reader and moving the data into a vectorized row  * batch.  */
end_comment

begin_class
class|class
name|VectorizedOrcAcidRowReader
implements|implements
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
block|{
specifier|private
specifier|final
name|AcidInputFormat
operator|.
name|RowReader
argument_list|<
name|OrcStruct
argument_list|>
name|innerReader
decl_stmt|;
specifier|private
specifier|final
name|RecordIdentifier
name|key
decl_stmt|;
specifier|private
specifier|final
name|OrcStruct
name|value
decl_stmt|;
specifier|private
specifier|final
name|VectorizedRowBatchCtx
name|rowBatchCtx
decl_stmt|;
specifier|private
specifier|final
name|ObjectInspector
name|objectInspector
decl_stmt|;
specifier|private
name|boolean
name|needToSetPartition
init|=
literal|true
decl_stmt|;
specifier|private
specifier|final
name|DataOutputBuffer
name|buffer
init|=
operator|new
name|DataOutputBuffer
argument_list|()
decl_stmt|;
name|VectorizedOrcAcidRowReader
parameter_list|(
name|AcidInputFormat
operator|.
name|RowReader
argument_list|<
name|OrcStruct
argument_list|>
name|inner
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|FileSplit
name|split
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|innerReader
operator|=
name|inner
expr_stmt|;
name|this
operator|.
name|key
operator|=
name|inner
operator|.
name|createKey
argument_list|()
expr_stmt|;
name|this
operator|.
name|rowBatchCtx
operator|=
operator|new
name|VectorizedRowBatchCtx
argument_list|()
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|inner
operator|.
name|createValue
argument_list|()
expr_stmt|;
name|this
operator|.
name|objectInspector
operator|=
name|inner
operator|.
name|getObjectInspector
argument_list|()
expr_stmt|;
try|try
block|{
name|rowBatchCtx
operator|.
name|init
argument_list|(
name|conf
argument_list|,
name|split
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to initialize context"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to initialize context"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InstantiationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to initialize context"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to initialize context"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to initialize context"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|NullWritable
name|nullWritable
parameter_list|,
name|VectorizedRowBatch
name|vectorizedRowBatch
parameter_list|)
throws|throws
name|IOException
block|{
name|vectorizedRowBatch
operator|.
name|reset
argument_list|()
expr_stmt|;
name|buffer
operator|.
name|reset
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|innerReader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|needToSetPartition
condition|)
block|{
try|try
block|{
name|rowBatchCtx
operator|.
name|addPartitionColsToBatch
argument_list|(
name|vectorizedRowBatch
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Problem adding partition column"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|needToSetPartition
operator|=
literal|false
expr_stmt|;
block|}
try|try
block|{
name|VectorizedBatchUtil
operator|.
name|addRowToBatch
argument_list|(
name|value
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|objectInspector
argument_list|,
name|vectorizedRowBatch
operator|.
name|size
operator|++
argument_list|,
name|vectorizedRowBatch
argument_list|,
name|buffer
argument_list|)
expr_stmt|;
while|while
condition|(
name|vectorizedRowBatch
operator|.
name|size
operator|<
name|vectorizedRowBatch
operator|.
name|selected
operator|.
name|length
operator|&&
name|innerReader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
name|VectorizedBatchUtil
operator|.
name|addRowToBatch
argument_list|(
name|value
argument_list|,
operator|(
name|StructObjectInspector
operator|)
name|objectInspector
argument_list|,
name|vectorizedRowBatch
operator|.
name|size
operator|++
argument_list|,
name|vectorizedRowBatch
argument_list|,
name|buffer
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|he
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"error iterating"
argument_list|,
name|he
argument_list|)
throw|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|NullWritable
name|createKey
parameter_list|()
block|{
return|return
name|NullWritable
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|VectorizedRowBatch
name|createValue
parameter_list|()
block|{
try|try
block|{
return|return
name|rowBatchCtx
operator|.
name|createVectorizedRowBatch
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Error creating a batch"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getPos
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|innerReader
operator|.
name|getPos
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|innerReader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|innerReader
operator|.
name|getProgress
argument_list|()
return|;
block|}
block|}
end_class

end_unit

