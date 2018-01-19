begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|mapreduce
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
name|mapreduce
operator|.
name|OutputCommitter
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
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|HCatRecord
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
name|text
operator|.
name|NumberFormat
import|;
end_import

begin_comment
comment|/**  * Bare bones implementation of OutputFormatContainer. Does only the required  * tasks to work properly with HCatalog. HCatalog features which require a  * storage specific implementation are unsupported (ie partitioning).  */
end_comment

begin_class
class|class
name|DefaultOutputFormatContainer
extends|extends
name|OutputFormatContainer
block|{
specifier|private
specifier|static
specifier|final
name|NumberFormat
name|NUMBER_FORMAT
init|=
name|NumberFormat
operator|.
name|getInstance
argument_list|()
decl_stmt|;
static|static
block|{
name|NUMBER_FORMAT
operator|.
name|setMinimumIntegerDigits
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|NUMBER_FORMAT
operator|.
name|setGroupingUsed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|DefaultOutputFormatContainer
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|OutputFormat
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|Writable
argument_list|>
name|of
parameter_list|)
block|{
name|super
argument_list|(
name|of
argument_list|)
expr_stmt|;
block|}
specifier|static
specifier|synchronized
name|String
name|getOutputName
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
block|{
return|return
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
literal|"mapreduce.output.basename"
argument_list|,
literal|"part"
argument_list|)
operator|+
literal|"-"
operator|+
name|NUMBER_FORMAT
operator|.
name|format
argument_list|(
name|context
operator|.
name|getTaskAttemptID
argument_list|()
operator|.
name|getTaskID
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Get the record writer for the job. Uses the storagehandler's OutputFormat    * to get the record writer.    * @param context the information about the current task.    * @return a RecordWriter to write the output for the job.    * @throws IOException    */
annotation|@
name|Override
specifier|public
name|RecordWriter
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|HCatRecord
argument_list|>
name|getRecordWriter
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|String
name|name
init|=
name|getOutputName
argument_list|(
name|context
argument_list|)
decl_stmt|;
return|return
operator|new
name|DefaultRecordWriterContainer
argument_list|(
name|context
argument_list|,
name|getBaseOutputFormat
argument_list|()
operator|.
name|getRecordWriter
argument_list|(
literal|null
argument_list|,
operator|new
name|JobConf
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|name
argument_list|,
name|InternalUtil
operator|.
name|createReporter
argument_list|(
name|context
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Get the output committer for this output format. This is responsible    * for ensuring the output is committed correctly.    * @param context the task context    * @return an output committer    * @throws IOException    * @throws InterruptedException    */
annotation|@
name|Override
specifier|public
name|OutputCommitter
name|getOutputCommitter
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
operator|new
name|DefaultOutputCommitterContainer
argument_list|(
name|context
argument_list|,
operator|new
name|JobConf
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|getOutputCommitter
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Check for validity of the output-specification for the job.    * @param context information about the job    * @throws IOException when output should not be attempted    */
annotation|@
name|Override
specifier|public
name|void
name|checkOutputSpecs
parameter_list|(
name|JobContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|OutputFormat
argument_list|<
name|?
super|super
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|?
super|super
name|Writable
argument_list|>
name|outputFormat
init|=
name|getBaseOutputFormat
argument_list|()
decl_stmt|;
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|outputFormat
operator|.
name|checkOutputSpecs
argument_list|(
literal|null
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
name|HCatUtil
operator|.
name|copyConf
argument_list|(
name|jobConf
argument_list|,
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

