begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|parquet
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
name|vector
operator|.
name|VectorColumnAssign
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
name|VectorColumnAssignFactory
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
name|VectorizedInputFormatInterface
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
name|parquet
operator|.
name|read
operator|.
name|ParquetRecordReaderWrapper
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
name|ArrayWritable
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
name|FileInputFormat
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
name|FileSplit
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
name|InputSplit
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
name|JobConf
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
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|Reporter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|hadoop
operator|.
name|ParquetInputFormat
import|;
end_import

begin_comment
comment|/**  * Vectorized input format for Parquet files  */
end_comment

begin_class
specifier|public
class|class
name|VectorizedParquetInputFormat
extends|extends
name|FileInputFormat
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
implements|implements
name|VectorizedInputFormatInterface
block|{
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
name|VectorizedParquetInputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Vectorized record reader for vectorized Parquet input format    */
specifier|private
specifier|static
class|class
name|VectorizedParquetRecordReader
implements|implements
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
block|{
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
name|VectorizedParquetRecordReader
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ParquetRecordReaderWrapper
name|internalReader
decl_stmt|;
specifier|private
name|VectorizedRowBatchCtx
name|rbCtx
decl_stmt|;
specifier|private
name|ArrayWritable
name|internalValues
decl_stmt|;
specifier|private
name|NullWritable
name|internalKey
decl_stmt|;
specifier|private
name|VectorColumnAssign
index|[]
name|assigners
decl_stmt|;
specifier|public
name|VectorizedParquetRecordReader
parameter_list|(
name|ParquetInputFormat
argument_list|<
name|ArrayWritable
argument_list|>
name|realInput
parameter_list|,
name|FileSplit
name|split
parameter_list|,
name|JobConf
name|conf
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|internalReader
operator|=
operator|new
name|ParquetRecordReaderWrapper
argument_list|(
name|realInput
argument_list|,
name|split
argument_list|,
name|conf
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
try|try
block|{
name|rbCtx
operator|=
operator|new
name|VectorizedRowBatchCtx
argument_list|()
expr_stmt|;
name|rbCtx
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
name|Exception
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
block|}
annotation|@
name|Override
specifier|public
name|NullWritable
name|createKey
parameter_list|()
block|{
name|internalKey
operator|=
name|internalReader
operator|.
name|createKey
argument_list|()
expr_stmt|;
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
name|VectorizedRowBatch
name|outputBatch
init|=
literal|null
decl_stmt|;
try|try
block|{
name|outputBatch
operator|=
name|rbCtx
operator|.
name|createVectorizedRowBatch
argument_list|()
expr_stmt|;
name|internalValues
operator|=
name|internalReader
operator|.
name|createValue
argument_list|()
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
name|RuntimeException
argument_list|(
literal|"Error creating a batch"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|outputBatch
return|;
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
name|internalReader
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
name|internalReader
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
name|internalReader
operator|.
name|getProgress
argument_list|()
return|;
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
name|VectorizedRowBatch
name|outputBatch
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|assigners
operator|!=
literal|null
condition|)
block|{
assert|assert
operator|(
name|outputBatch
operator|.
name|numCols
operator|==
name|assigners
operator|.
name|length
operator|)
assert|;
block|}
name|outputBatch
operator|.
name|reset
argument_list|()
expr_stmt|;
name|int
name|maxSize
init|=
name|outputBatch
operator|.
name|getMaxSize
argument_list|()
decl_stmt|;
try|try
block|{
while|while
condition|(
name|outputBatch
operator|.
name|size
operator|<
name|maxSize
condition|)
block|{
if|if
condition|(
literal|false
operator|==
name|internalReader
operator|.
name|next
argument_list|(
name|internalKey
argument_list|,
name|internalValues
argument_list|)
condition|)
block|{
name|outputBatch
operator|.
name|endOfFile
operator|=
literal|true
expr_stmt|;
break|break;
block|}
name|Writable
index|[]
name|writables
init|=
name|internalValues
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|assigners
condition|)
block|{
comment|// Normally we'd build the assigners from the rbCtx.rowOI, but with Parquet
comment|// we have a discrepancy between the metadata type (Eg. tinyint -> BYTE) and
comment|// the writable value (IntWritable). see Parquet's ETypeConverter class.
name|assigners
operator|=
name|VectorColumnAssignFactory
operator|.
name|buildAssigners
argument_list|(
name|outputBatch
argument_list|,
name|writables
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|writables
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|assigners
index|[
name|i
index|]
operator|.
name|assignObjectValue
argument_list|(
name|writables
index|[
name|i
index|]
argument_list|,
name|outputBatch
operator|.
name|size
argument_list|)
expr_stmt|;
block|}
operator|++
name|outputBatch
operator|.
name|size
expr_stmt|;
block|}
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
name|e
argument_list|)
throw|;
block|}
return|return
name|outputBatch
operator|.
name|size
operator|>
literal|0
return|;
block|}
block|}
specifier|private
specifier|final
name|ParquetInputFormat
argument_list|<
name|ArrayWritable
argument_list|>
name|realInput
decl_stmt|;
specifier|public
name|VectorizedParquetInputFormat
parameter_list|(
name|ParquetInputFormat
argument_list|<
name|ArrayWritable
argument_list|>
name|realInput
parameter_list|)
block|{
name|this
operator|.
name|realInput
operator|=
name|realInput
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
name|getRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|JobConf
name|conf
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
return|return
operator|(
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|VectorizedRowBatch
argument_list|>
operator|)
operator|new
name|VectorizedParquetRecordReader
argument_list|(
name|realInput
argument_list|,
operator|(
name|FileSplit
operator|)
name|split
argument_list|,
name|conf
argument_list|,
name|reporter
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
specifier|final
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot create a VectorizedParquetRecordReader"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

