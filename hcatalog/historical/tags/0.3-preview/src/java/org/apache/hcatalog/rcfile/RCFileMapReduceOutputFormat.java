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
name|hcatalog
operator|.
name|rcfile
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
name|FileSystem
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
name|RCFile
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
name|columnar
operator|.
name|BytesRefArrayWritable
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
name|WritableComparable
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
name|compress
operator|.
name|CompressionCodec
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
name|compress
operator|.
name|DefaultCodec
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
name|lib
operator|.
name|output
operator|.
name|FileOutputCommitter
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
name|lib
operator|.
name|output
operator|.
name|FileOutputFormat
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
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * The RC file input format using new Hadoop mapreduce APIs.  */
end_comment

begin_class
specifier|public
class|class
name|RCFileMapReduceOutputFormat
extends|extends
name|FileOutputFormat
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|BytesRefArrayWritable
argument_list|>
block|{
comment|/**    * Set number of columns into the given configuration.    * @param conf    *          configuration instance which need to set the column number    * @param columnNum    *          column number for RCFile's Writer    *    */
specifier|public
specifier|static
name|void
name|setColumnNumber
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|columnNum
parameter_list|)
block|{
assert|assert
name|columnNum
operator|>
literal|0
assert|;
name|conf
operator|.
name|setInt
argument_list|(
name|RCFile
operator|.
name|COLUMN_NUMBER_CONF_STR
argument_list|,
name|columnNum
argument_list|)
expr_stmt|;
block|}
comment|/* (non-Javadoc)    * @see org.apache.hadoop.mapreduce.lib.output.FileOutputFormat#getRecordWriter(org.apache.hadoop.mapreduce.TaskAttemptContext)    */
annotation|@
name|Override
specifier|public
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
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|BytesRefArrayWritable
argument_list|>
name|getRecordWriter
parameter_list|(
name|TaskAttemptContext
name|task
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|//FileOutputFormat.getWorkOutputPath takes TaskInputOutputContext instead of
comment|//TaskAttemptContext, so can't use that here
name|FileOutputCommitter
name|committer
init|=
operator|(
name|FileOutputCommitter
operator|)
name|getOutputCommitter
argument_list|(
name|task
argument_list|)
decl_stmt|;
name|Path
name|outputPath
init|=
name|committer
operator|.
name|getWorkPath
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|outputPath
operator|.
name|getFileSystem
argument_list|(
name|task
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|outputPath
argument_list|)
condition|)
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|outputPath
argument_list|)
expr_stmt|;
block|}
name|Path
name|file
init|=
name|getDefaultWorkFile
argument_list|(
name|task
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|CompressionCodec
name|codec
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|getCompressOutput
argument_list|(
name|task
argument_list|)
condition|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|codecClass
init|=
name|getOutputCompressorClass
argument_list|(
name|task
argument_list|,
name|DefaultCodec
operator|.
name|class
argument_list|)
decl_stmt|;
name|codec
operator|=
operator|(
name|CompressionCodec
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|codecClass
argument_list|,
name|task
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|final
name|RCFile
operator|.
name|Writer
name|out
init|=
operator|new
name|RCFile
operator|.
name|Writer
argument_list|(
name|fs
argument_list|,
name|task
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|file
argument_list|,
name|task
argument_list|,
name|codec
argument_list|)
decl_stmt|;
return|return
operator|new
name|RecordWriter
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|BytesRefArrayWritable
argument_list|>
argument_list|()
block|{
comment|/* (non-Javadoc)        * @see org.apache.hadoop.mapreduce.RecordWriter#write(java.lang.Object, java.lang.Object)        */
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|WritableComparable
argument_list|<
name|?
argument_list|>
name|key
parameter_list|,
name|BytesRefArrayWritable
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|append
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
comment|/* (non-Javadoc)        * @see org.apache.hadoop.mapreduce.RecordWriter#close(org.apache.hadoop.mapreduce.TaskAttemptContext)        */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|TaskAttemptContext
name|task
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
empty_stmt|;
block|}
block|}
end_class

end_unit

