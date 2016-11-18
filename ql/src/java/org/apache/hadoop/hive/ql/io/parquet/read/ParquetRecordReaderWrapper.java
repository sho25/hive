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
operator|.
name|read
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
name|ParquetRecordReaderBase
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
name|hive
operator|.
name|conf
operator|.
name|HiveConf
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
name|IOConstants
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
name|StatsProvidingRecordReader
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
name|ProjectionPusher
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
name|parquet
operator|.
name|hadoop
operator|.
name|ParquetInputFormat
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
name|ParquetInputSplit
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

begin_class
specifier|public
class|class
name|ParquetRecordReaderWrapper
extends|extends
name|ParquetRecordReaderBase
implements|implements
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|ArrayWritable
argument_list|>
implements|,
name|StatsProvidingRecordReader
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
name|ParquetRecordReaderWrapper
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|long
name|splitLen
decl_stmt|;
comment|// for getPos()
specifier|private
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|RecordReader
argument_list|<
name|Void
argument_list|,
name|ArrayWritable
argument_list|>
name|realReader
decl_stmt|;
comment|// expect readReader return same Key& Value objects (common case)
comment|// this avoids extra serialization& deserialization of these objects
specifier|private
name|ArrayWritable
name|valueObj
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|firstRecord
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|eof
init|=
literal|false
decl_stmt|;
specifier|public
name|ParquetRecordReaderWrapper
parameter_list|(
specifier|final
name|ParquetInputFormat
argument_list|<
name|ArrayWritable
argument_list|>
name|newInputFormat
parameter_list|,
specifier|final
name|InputSplit
name|oldSplit
parameter_list|,
specifier|final
name|JobConf
name|oldJobConf
parameter_list|,
specifier|final
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|this
argument_list|(
name|newInputFormat
argument_list|,
name|oldSplit
argument_list|,
name|oldJobConf
argument_list|,
name|reporter
argument_list|,
operator|new
name|ProjectionPusher
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ParquetRecordReaderWrapper
parameter_list|(
specifier|final
name|ParquetInputFormat
argument_list|<
name|ArrayWritable
argument_list|>
name|newInputFormat
parameter_list|,
specifier|final
name|InputSplit
name|oldSplit
parameter_list|,
specifier|final
name|JobConf
name|oldJobConf
parameter_list|,
specifier|final
name|Reporter
name|reporter
parameter_list|,
specifier|final
name|ProjectionPusher
name|pusher
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|this
operator|.
name|splitLen
operator|=
name|oldSplit
operator|.
name|getLength
argument_list|()
expr_stmt|;
name|this
operator|.
name|projectionPusher
operator|=
name|pusher
expr_stmt|;
name|this
operator|.
name|serDeStats
operator|=
operator|new
name|SerDeStats
argument_list|()
expr_stmt|;
name|jobConf
operator|=
name|oldJobConf
expr_stmt|;
specifier|final
name|ParquetInputSplit
name|split
init|=
name|getSplit
argument_list|(
name|oldSplit
argument_list|,
name|jobConf
argument_list|)
decl_stmt|;
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
name|IOConstants
operator|.
name|MAPRED_TASK_ID
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
comment|// create a TaskInputOutputContext
name|Configuration
name|conf
init|=
name|jobConf
decl_stmt|;
if|if
condition|(
name|skipTimestampConversion
operator|^
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_PARQUET_TIMESTAMP_SKIP_CONVERSION
argument_list|)
condition|)
block|{
name|conf
operator|=
operator|new
name|JobConf
argument_list|(
name|oldJobConf
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_PARQUET_TIMESTAMP_SKIP_CONVERSION
argument_list|,
name|skipTimestampConversion
argument_list|)
expr_stmt|;
block|}
specifier|final
name|TaskAttemptContext
name|taskContext
init|=
name|ContextUtil
operator|.
name|newTaskAttemptContext
argument_list|(
name|conf
argument_list|,
name|taskAttemptID
argument_list|)
decl_stmt|;
if|if
condition|(
name|split
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|realReader
operator|=
name|newInputFormat
operator|.
name|createRecordReader
argument_list|(
name|split
argument_list|,
name|taskContext
argument_list|)
expr_stmt|;
name|realReader
operator|.
name|initialize
argument_list|(
name|split
argument_list|,
name|taskContext
argument_list|)
expr_stmt|;
comment|// read once to gain access to key and value objects
if|if
condition|(
name|realReader
operator|.
name|nextKeyValue
argument_list|()
condition|)
block|{
name|firstRecord
operator|=
literal|true
expr_stmt|;
name|valueObj
operator|=
name|realReader
operator|.
name|getCurrentValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|eof
operator|=
literal|true
expr_stmt|;
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
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|realReader
operator|=
literal|null
expr_stmt|;
name|eof
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|valueObj
operator|==
literal|null
condition|)
block|{
comment|// Should initialize the value for createValue
name|valueObj
operator|=
operator|new
name|ArrayWritable
argument_list|(
name|Writable
operator|.
name|class
argument_list|,
operator|new
name|Writable
index|[
name|schemaSize
index|]
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|realReader
operator|!=
literal|null
condition|)
block|{
name|realReader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|NullWritable
name|createKey
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ArrayWritable
name|createValue
parameter_list|()
block|{
return|return
name|valueObj
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
call|(
name|long
call|)
argument_list|(
name|splitLen
operator|*
name|getProgress
argument_list|()
argument_list|)
return|;
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
if|if
condition|(
name|realReader
operator|==
literal|null
condition|)
block|{
return|return
literal|1f
return|;
block|}
else|else
block|{
try|try
block|{
return|return
name|realReader
operator|.
name|getProgress
argument_list|()
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
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
specifier|final
name|NullWritable
name|key
parameter_list|,
specifier|final
name|ArrayWritable
name|value
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|eof
condition|)
block|{
return|return
literal|false
return|;
block|}
try|try
block|{
if|if
condition|(
name|firstRecord
condition|)
block|{
comment|// key& value are already read.
name|firstRecord
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|realReader
operator|.
name|nextKeyValue
argument_list|()
condition|)
block|{
name|eof
operator|=
literal|true
expr_stmt|;
comment|// strictly not required, just for consistency
return|return
literal|false
return|;
block|}
specifier|final
name|ArrayWritable
name|tmpCurValue
init|=
name|realReader
operator|.
name|getCurrentValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
name|tmpCurValue
condition|)
block|{
specifier|final
name|Writable
index|[]
name|arrValue
init|=
name|value
operator|.
name|get
argument_list|()
decl_stmt|;
specifier|final
name|Writable
index|[]
name|arrCurrent
init|=
name|tmpCurValue
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
operator|&&
name|arrValue
operator|.
name|length
operator|==
name|arrCurrent
operator|.
name|length
condition|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|arrCurrent
argument_list|,
literal|0
argument_list|,
name|arrValue
argument_list|,
literal|0
argument_list|,
name|arrCurrent
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|arrValue
operator|.
name|length
operator|!=
name|arrCurrent
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"DeprecatedParquetHiveInput : size of object differs. Value"
operator|+
literal|" size :  "
operator|+
name|arrValue
operator|.
name|length
operator|+
literal|", Current Object size : "
operator|+
name|arrCurrent
operator|.
name|length
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"DeprecatedParquetHiveInput can not support RecordReaders that"
operator|+
literal|" don't return same key& value& value is null"
argument_list|)
throw|;
block|}
block|}
block|}
return|return
literal|true
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
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

