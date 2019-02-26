begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|write
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
name|Properties
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
name|fs
operator|.
name|Path
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
name|StatsProvidingRecordWriter
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
name|serde
operator|.
name|ParquetTableUtils
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
name|mapreduce
operator|.
name|JobContext
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
name|RecordWriter
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
name|hadoop
operator|.
name|mapreduce
operator|.
name|OutputFormat
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
name|mapreduce
operator|.
name|TaskAttemptContext
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
name|mapreduce
operator|.
name|TaskAttemptID
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
name|SerDeStats
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
name|io
operator|.
name|ParquetHiveRecord
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
name|util
operator|.
name|Progressable
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
name|ParquetFileReader
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
name|ParquetOutputFormat
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
name|metadata
operator|.
name|BlockMetaData
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
name|metadata
operator|.
name|CompressionCodecName
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
name|util
operator|.
name|ContextUtil
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
name|util
operator|.
name|HadoopInputFile
import|;
end_import

begin_class
specifier|public
class|class
name|ParquetRecordWriterWrapper
implements|implements
name|RecordWriter
argument_list|<
name|NullWritable
argument_list|,
name|ParquetHiveRecord
argument_list|>
implements|,
name|StatsProvidingRecordWriter
implements|,
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
name|FileSinkOperator
operator|.
name|RecordWriter
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ParquetRecordWriterWrapper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|RecordWriter
argument_list|<
name|NullWritable
argument_list|,
name|ParquetHiveRecord
argument_list|>
name|realWriter
decl_stmt|;
specifier|private
specifier|final
name|TaskAttemptContext
name|taskContext
decl_stmt|;
specifier|private
specifier|final
name|JobConf
name|jobConf
decl_stmt|;
specifier|private
specifier|final
name|Path
name|file
decl_stmt|;
specifier|private
name|SerDeStats
name|stats
decl_stmt|;
specifier|public
name|ParquetRecordWriterWrapper
parameter_list|(
specifier|final
name|OutputFormat
argument_list|<
name|Void
argument_list|,
name|ParquetHiveRecord
argument_list|>
name|realOutputFormat
parameter_list|,
specifier|final
name|JobConf
name|jobConf
parameter_list|,
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|Progressable
name|progress
parameter_list|,
name|Properties
name|tableProperties
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
comment|// create a TaskInputOutputContext
name|TaskAttemptID
name|taskAttemptID
init|=
name|TaskAttemptID
operator|.
name|forName
argument_list|(
name|jobConf
operator|.
name|get
argument_list|(
literal|"mapred.task.id"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|taskAttemptID
operator|==
literal|null
condition|)
block|{
name|taskAttemptID
operator|=
operator|new
name|TaskAttemptID
argument_list|()
expr_stmt|;
block|}
name|taskContext
operator|=
name|ContextUtil
operator|.
name|newTaskAttemptContext
argument_list|(
name|jobConf
argument_list|,
name|taskAttemptID
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"initialize serde with table properties."
argument_list|)
expr_stmt|;
name|initializeSerProperties
argument_list|(
name|taskContext
argument_list|,
name|tableProperties
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"creating real writer to write at "
operator|+
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|jobConf
operator|=
name|jobConf
expr_stmt|;
name|this
operator|.
name|file
operator|=
operator|new
name|Path
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|realWriter
operator|=
operator|(
operator|(
name|ParquetOutputFormat
operator|)
name|realOutputFormat
operator|)
operator|.
name|getRecordWriter
argument_list|(
name|taskContext
argument_list|,
name|this
operator|.
name|file
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"real writer: "
operator|+
name|realWriter
argument_list|)
expr_stmt|;
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
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|ParquetRecordWriterWrapper
parameter_list|(
specifier|final
name|ParquetOutputFormat
argument_list|<
name|ParquetHiveRecord
argument_list|>
name|realOutputFormat
parameter_list|,
specifier|final
name|JobConf
name|jobConf
parameter_list|,
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|Progressable
name|progress
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|realOutputFormat
argument_list|,
name|jobConf
argument_list|,
name|name
argument_list|,
name|progress
argument_list|,
name|getParquetProperties
argument_list|(
name|jobConf
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|Properties
name|getParquetProperties
parameter_list|(
name|JobConf
name|jobConf
parameter_list|)
block|{
name|Properties
name|tblProperties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|it
init|=
name|jobConf
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|ParquetTableUtils
operator|.
name|isParquetProperty
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|tblProperties
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|tblProperties
return|;
block|}
specifier|private
name|void
name|initializeSerProperties
parameter_list|(
name|JobContext
name|job
parameter_list|,
name|Properties
name|tableProperties
parameter_list|)
block|{
name|String
name|blockSize
init|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|ParquetOutputFormat
operator|.
name|BLOCK_SIZE
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|ContextUtil
operator|.
name|getConfiguration
argument_list|(
name|job
argument_list|)
decl_stmt|;
if|if
condition|(
name|blockSize
operator|!=
literal|null
operator|&&
operator|!
name|blockSize
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"get override parquet.block.size property via tblproperties"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|ParquetOutputFormat
operator|.
name|BLOCK_SIZE
argument_list|,
name|Integer
operator|.
name|parseInt
argument_list|(
name|blockSize
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|enableDictionaryPage
init|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|ParquetOutputFormat
operator|.
name|ENABLE_DICTIONARY
argument_list|)
decl_stmt|;
if|if
condition|(
name|enableDictionaryPage
operator|!=
literal|null
operator|&&
operator|!
name|enableDictionaryPage
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"get override parquet.enable.dictionary property via tblproperties"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|ParquetOutputFormat
operator|.
name|ENABLE_DICTIONARY
argument_list|,
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|enableDictionaryPage
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|compressionName
init|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|ParquetOutputFormat
operator|.
name|COMPRESSION
argument_list|)
decl_stmt|;
if|if
condition|(
name|compressionName
operator|!=
literal|null
operator|&&
operator|!
name|compressionName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|//get override compression properties via "tblproperties" clause if it is set
name|LOG
operator|.
name|debug
argument_list|(
literal|"get override compression properties via tblproperties"
argument_list|)
expr_stmt|;
name|CompressionCodecName
name|codecName
init|=
name|CompressionCodecName
operator|.
name|fromConf
argument_list|(
name|compressionName
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|ParquetOutputFormat
operator|.
name|COMPRESSION
argument_list|,
name|codecName
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
specifier|final
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|realWriter
operator|.
name|close
argument_list|(
name|taskContext
argument_list|)
expr_stmt|;
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
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// Collect file stats
try|try
block|{
name|ParquetFileReader
name|reader
init|=
name|ParquetFileReader
operator|.
name|open
argument_list|(
name|HadoopInputFile
operator|.
name|fromPath
argument_list|(
name|this
operator|.
name|file
argument_list|,
name|this
operator|.
name|jobConf
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|totalSize
init|=
literal|0
decl_stmt|;
for|for
control|(
name|BlockMetaData
name|block
range|:
name|reader
operator|.
name|getFooter
argument_list|()
operator|.
name|getBlocks
argument_list|()
control|)
block|{
name|totalSize
operator|+=
name|block
operator|.
name|getTotalByteSize
argument_list|()
expr_stmt|;
block|}
name|stats
operator|=
operator|new
name|SerDeStats
argument_list|()
expr_stmt|;
name|stats
operator|.
name|setRowCount
argument_list|(
name|reader
operator|.
name|getRecordCount
argument_list|()
argument_list|)
expr_stmt|;
name|stats
operator|.
name|setRawDataSize
argument_list|(
name|totalSize
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Ignore
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
specifier|final
name|NullWritable
name|key
parameter_list|,
specifier|final
name|ParquetHiveRecord
name|value
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|realWriter
operator|.
name|write
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
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
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
specifier|final
name|boolean
name|abort
parameter_list|)
throws|throws
name|IOException
block|{
name|close
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
specifier|final
name|Writable
name|w
parameter_list|)
throws|throws
name|IOException
block|{
name|write
argument_list|(
literal|null
argument_list|,
operator|(
name|ParquetHiveRecord
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getStats
parameter_list|()
block|{
return|return
name|stats
return|;
block|}
block|}
end_class

end_unit

