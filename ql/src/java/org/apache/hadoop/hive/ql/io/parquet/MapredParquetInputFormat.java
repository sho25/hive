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
name|exec
operator|.
name|Utilities
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
name|DataWritableReadSupport
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
name|RecordReader
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
comment|/**  *  * A Parquet InputFormat for Hive (with the deprecated package mapred)  *  * NOTE: With HIVE-9235 we removed "implements VectorizedParquetInputFormat" since all data types  *       are not currently supported.  Removing the interface turns off vectorization.  */
end_comment

begin_class
specifier|public
class|class
name|MapredParquetInputFormat
extends|extends
name|FileInputFormat
argument_list|<
name|NullWritable
argument_list|,
name|ArrayWritable
argument_list|>
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
name|MapredParquetInputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ParquetInputFormat
argument_list|<
name|ArrayWritable
argument_list|>
name|realInput
decl_stmt|;
specifier|private
specifier|final
specifier|transient
name|VectorizedParquetInputFormat
name|vectorizedSelf
decl_stmt|;
specifier|public
name|MapredParquetInputFormat
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|ParquetInputFormat
argument_list|<
name|ArrayWritable
argument_list|>
argument_list|(
name|DataWritableReadSupport
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|MapredParquetInputFormat
parameter_list|(
specifier|final
name|ParquetInputFormat
argument_list|<
name|ArrayWritable
argument_list|>
name|inputFormat
parameter_list|)
block|{
name|this
operator|.
name|realInput
operator|=
name|inputFormat
expr_stmt|;
name|vectorizedSelf
operator|=
operator|new
name|VectorizedParquetInputFormat
argument_list|(
name|inputFormat
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|,
literal|"rawtypes"
block|}
argument_list|)
annotation|@
name|Override
specifier|public
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
name|ArrayWritable
argument_list|>
name|getRecordReader
parameter_list|(
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|InputSplit
name|split
parameter_list|,
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobConf
name|job
parameter_list|,
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
if|if
condition|(
name|Utilities
operator|.
name|getUseVectorizedInputFileFormat
argument_list|(
name|job
argument_list|)
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
literal|"Using vectorized record reader"
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|RecordReader
operator|)
name|vectorizedSelf
operator|.
name|getRecordReader
argument_list|(
name|split
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|)
return|;
block|}
else|else
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
literal|"Using row-mode record reader"
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|ArrayWritable
argument_list|>
operator|)
operator|new
name|ParquetRecordReaderWrapper
argument_list|(
name|realInput
argument_list|,
name|split
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|)
return|;
block|}
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
literal|"Cannot create a RecordReaderWrapper"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

