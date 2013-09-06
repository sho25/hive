begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|impl
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
name|mapreduce
operator|.
name|Job
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
name|JobStatus
operator|.
name|State
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
name|hcatalog
operator|.
name|common
operator|.
name|ErrorType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
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
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|HCatWriter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|WriteEntity
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|WriterContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|transfer
operator|.
name|state
operator|.
name|StateProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|HCatOutputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|OutputJobInfo
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
name|shims
operator|.
name|ShimLoader
import|;
end_import

begin_comment
comment|/**  * This writer writes via {@link HCatOutputFormat}  *  */
end_comment

begin_class
specifier|public
class|class
name|HCatOutputFormatWriter
extends|extends
name|HCatWriter
block|{
specifier|public
name|HCatOutputFormatWriter
parameter_list|(
name|WriteEntity
name|we
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|config
parameter_list|)
block|{
name|super
argument_list|(
name|we
argument_list|,
name|config
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HCatOutputFormatWriter
parameter_list|(
name|Configuration
name|config
parameter_list|,
name|StateProvider
name|sp
parameter_list|)
block|{
name|super
argument_list|(
name|config
argument_list|,
name|sp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|WriterContext
name|prepareWrite
parameter_list|()
throws|throws
name|HCatException
block|{
name|OutputJobInfo
name|jobInfo
init|=
name|OutputJobInfo
operator|.
name|create
argument_list|(
name|we
operator|.
name|getDbName
argument_list|()
argument_list|,
name|we
operator|.
name|getTableName
argument_list|()
argument_list|,
name|we
operator|.
name|getPartitionKVs
argument_list|()
argument_list|)
decl_stmt|;
name|Job
name|job
decl_stmt|;
try|try
block|{
name|job
operator|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|HCatOutputFormat
operator|.
name|setOutput
argument_list|(
name|job
argument_list|,
name|jobInfo
argument_list|)
expr_stmt|;
name|HCatOutputFormat
operator|.
name|setSchema
argument_list|(
name|job
argument_list|,
name|HCatOutputFormat
operator|.
name|getTableSchema
argument_list|(
name|job
argument_list|)
argument_list|)
expr_stmt|;
name|HCatOutputFormat
name|outFormat
init|=
operator|new
name|HCatOutputFormat
argument_list|()
decl_stmt|;
name|outFormat
operator|.
name|checkOutputSpecs
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|outFormat
operator|.
name|getOutputCommitter
argument_list|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createTaskAttemptContext
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createTaskAttemptID
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setupJob
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_NOT_INITIALIZED
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_NOT_INITIALIZED
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|WriterContext
name|cntxt
init|=
operator|new
name|WriterContext
argument_list|()
decl_stmt|;
name|cntxt
operator|.
name|setConf
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|cntxt
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|Iterator
argument_list|<
name|HCatRecord
argument_list|>
name|recordItr
parameter_list|)
throws|throws
name|HCatException
block|{
name|int
name|id
init|=
name|sp
operator|.
name|getId
argument_list|()
decl_stmt|;
name|setVarsInConf
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|HCatOutputFormat
name|outFormat
init|=
operator|new
name|HCatOutputFormat
argument_list|()
decl_stmt|;
name|TaskAttemptContext
name|cntxt
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createTaskAttemptContext
argument_list|(
name|conf
argument_list|,
operator|new
name|TaskAttemptID
argument_list|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createTaskID
argument_list|()
argument_list|,
name|id
argument_list|)
argument_list|)
decl_stmt|;
name|OutputCommitter
name|committer
init|=
literal|null
decl_stmt|;
name|RecordWriter
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|HCatRecord
argument_list|>
name|writer
decl_stmt|;
try|try
block|{
name|committer
operator|=
name|outFormat
operator|.
name|getOutputCommitter
argument_list|(
name|cntxt
argument_list|)
expr_stmt|;
name|committer
operator|.
name|setupTask
argument_list|(
name|cntxt
argument_list|)
expr_stmt|;
name|writer
operator|=
name|outFormat
operator|.
name|getRecordWriter
argument_list|(
name|cntxt
argument_list|)
expr_stmt|;
while|while
condition|(
name|recordItr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|HCatRecord
name|rec
init|=
name|recordItr
operator|.
name|next
argument_list|()
decl_stmt|;
name|writer
operator|.
name|write
argument_list|(
literal|null
argument_list|,
name|rec
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|(
name|cntxt
argument_list|)
expr_stmt|;
if|if
condition|(
name|committer
operator|.
name|needsTaskCommit
argument_list|(
name|cntxt
argument_list|)
condition|)
block|{
name|committer
operator|.
name|commitTask
argument_list|(
name|cntxt
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
literal|null
operator|!=
name|committer
condition|)
block|{
try|try
block|{
name|committer
operator|.
name|abortTask
argument_list|(
name|cntxt
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_INTERNAL_EXCEPTION
argument_list|,
name|e1
argument_list|)
throw|;
block|}
block|}
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Failed while writing"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
if|if
condition|(
literal|null
operator|!=
name|committer
condition|)
block|{
try|try
block|{
name|committer
operator|.
name|abortTask
argument_list|(
name|cntxt
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_INTERNAL_EXCEPTION
argument_list|,
name|e1
argument_list|)
throw|;
block|}
block|}
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Failed while writing"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|commit
parameter_list|(
name|WriterContext
name|context
parameter_list|)
throws|throws
name|HCatException
block|{
try|try
block|{
operator|new
name|HCatOutputFormat
argument_list|()
operator|.
name|getOutputCommitter
argument_list|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createTaskAttemptContext
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createTaskAttemptID
argument_list|()
argument_list|)
argument_list|)
operator|.
name|commitJob
argument_list|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createJobContext
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_NOT_INITIALIZED
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_NOT_INITIALIZED
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|WriterContext
name|context
parameter_list|)
throws|throws
name|HCatException
block|{
try|try
block|{
operator|new
name|HCatOutputFormat
argument_list|()
operator|.
name|getOutputCommitter
argument_list|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createTaskAttemptContext
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createTaskAttemptID
argument_list|()
argument_list|)
argument_list|)
operator|.
name|abortJob
argument_list|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getHCatShim
argument_list|()
operator|.
name|createJobContext
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|,
name|State
operator|.
name|FAILED
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_NOT_INITIALIZED
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
name|ErrorType
operator|.
name|ERROR_NOT_INITIALIZED
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|setVarsInConf
parameter_list|(
name|int
name|id
parameter_list|)
block|{
comment|// Following two config keys are required by FileOutputFormat to work
comment|// correctly.
comment|// In usual case of Hadoop, JobTracker will set these before launching
comment|// tasks.
comment|// Since there is no jobtracker here, we set it ourself.
name|conf
operator|.
name|setInt
argument_list|(
literal|"mapred.task.partition"
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"mapred.task.id"
argument_list|,
literal|"attempt__0000_r_000000_"
operator|+
name|id
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

