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
name|hbase
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
name|List
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
name|hbase
operator|.
name|Cell
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
name|hbase
operator|.
name|KeyValue
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
name|hbase
operator|.
name|client
operator|.
name|Put
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
name|hbase
operator|.
name|io
operator|.
name|ImmutableBytesWritable
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
name|hbase
operator|.
name|security
operator|.
name|User
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
name|Text
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
name|mapred
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
name|mapred
operator|.
name|JobClient
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
name|mapred
operator|.
name|SequenceFileOutputFormat
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
name|hcatalog
operator|.
name|hbase
operator|.
name|snapshot
operator|.
name|RevisionManager
import|;
end_import

begin_comment
comment|/**  * Class which imports data into HBase via it's "bulk load" feature. Wherein  * regions are created by the MR job using HFileOutputFormat and then later  * "moved" into the appropriate region server.  */
end_comment

begin_class
class|class
name|HBaseBulkOutputFormat
extends|extends
name|HBaseBaseOutputFormat
block|{
specifier|private
specifier|final
specifier|static
name|ImmutableBytesWritable
name|EMPTY_LIST
init|=
operator|new
name|ImmutableBytesWritable
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
specifier|private
name|SequenceFileOutputFormat
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|Object
argument_list|>
name|baseOutputFormat
decl_stmt|;
specifier|public
name|HBaseBulkOutputFormat
parameter_list|()
block|{
name|baseOutputFormat
operator|=
operator|new
name|SequenceFileOutputFormat
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|Object
argument_list|>
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkOutputSpecs
parameter_list|(
name|FileSystem
name|ignored
parameter_list|,
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
name|baseOutputFormat
operator|.
name|checkOutputSpecs
argument_list|(
name|ignored
argument_list|,
name|job
argument_list|)
expr_stmt|;
name|HBaseUtil
operator|.
name|addHBaseDelegationToken
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|addJTDelegationToken
argument_list|(
name|job
argument_list|)
expr_stmt|;
block|}
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
name|Object
argument_list|>
name|getRecordWriter
parameter_list|(
name|FileSystem
name|ignored
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|String
name|name
parameter_list|,
name|Progressable
name|progress
parameter_list|)
throws|throws
name|IOException
block|{
name|HBaseHCatStorageHandler
operator|.
name|setHBaseSerializers
argument_list|(
name|job
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputKeyClass
argument_list|(
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputValueClass
argument_list|(
name|Put
operator|.
name|class
argument_list|)
expr_stmt|;
name|long
name|version
init|=
name|HBaseRevisionManagerUtil
operator|.
name|getOutputRevision
argument_list|(
name|job
argument_list|)
decl_stmt|;
return|return
operator|new
name|HBaseBulkRecordWriter
argument_list|(
name|baseOutputFormat
operator|.
name|getRecordWriter
argument_list|(
name|ignored
argument_list|,
name|job
argument_list|,
name|name
argument_list|,
name|progress
argument_list|)
argument_list|,
name|version
argument_list|)
return|;
block|}
specifier|private
name|void
name|addJTDelegationToken
parameter_list|(
name|JobConf
name|job
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Get jobTracker delegation token if security is enabled
comment|// we need to launch the ImportSequenceFile job
if|if
condition|(
name|User
operator|.
name|isSecurityEnabled
argument_list|()
condition|)
block|{
name|JobClient
name|jobClient
init|=
operator|new
name|JobClient
argument_list|(
operator|new
name|JobConf
argument_list|(
name|job
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|job
operator|.
name|getCredentials
argument_list|()
operator|.
name|addToken
argument_list|(
operator|new
name|Text
argument_list|(
literal|"my mr token"
argument_list|)
argument_list|,
name|jobClient
operator|.
name|getDelegationToken
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Error while getting JT delegation token"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|HBaseBulkRecordWriter
implements|implements
name|RecordWriter
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|Object
argument_list|>
block|{
specifier|private
name|RecordWriter
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|Object
argument_list|>
name|baseWriter
decl_stmt|;
specifier|private
specifier|final
name|Long
name|outputVersion
decl_stmt|;
specifier|public
name|HBaseBulkRecordWriter
parameter_list|(
name|RecordWriter
argument_list|<
name|WritableComparable
argument_list|<
name|?
argument_list|>
argument_list|,
name|Object
argument_list|>
name|baseWriter
parameter_list|,
name|Long
name|outputVersion
parameter_list|)
block|{
name|this
operator|.
name|baseWriter
operator|=
name|baseWriter
expr_stmt|;
name|this
operator|.
name|outputVersion
operator|=
name|outputVersion
expr_stmt|;
block|}
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
name|Object
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|original
init|=
name|toPut
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
name|original
decl_stmt|;
if|if
condition|(
name|outputVersion
operator|!=
literal|null
condition|)
block|{
name|put
operator|=
operator|new
name|Put
argument_list|(
name|original
operator|.
name|getRow
argument_list|()
argument_list|,
name|outputVersion
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|List
argument_list|<
name|?
extends|extends
name|Cell
argument_list|>
name|row
range|:
name|original
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|row
control|)
block|{
name|KeyValue
name|el
init|=
operator|(
name|KeyValue
operator|)
name|cell
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|el
operator|.
name|getFamily
argument_list|()
argument_list|,
name|el
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|el
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// we ignore the key
name|baseWriter
operator|.
name|write
argument_list|(
name|EMPTY_LIST
argument_list|,
name|put
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|(
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|baseWriter
operator|.
name|close
argument_list|(
name|reporter
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|HBaseBulkOutputCommitter
extends|extends
name|OutputCommitter
block|{
specifier|private
specifier|final
name|OutputCommitter
name|baseOutputCommitter
decl_stmt|;
specifier|public
name|HBaseBulkOutputCommitter
parameter_list|()
block|{
name|baseOutputCommitter
operator|=
operator|new
name|FileOutputCommitter
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abortTask
parameter_list|(
name|TaskAttemptContext
name|taskContext
parameter_list|)
throws|throws
name|IOException
block|{
name|baseOutputCommitter
operator|.
name|abortTask
argument_list|(
name|taskContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitTask
parameter_list|(
name|TaskAttemptContext
name|taskContext
parameter_list|)
throws|throws
name|IOException
block|{
comment|// baseOutputCommitter.commitTask(taskContext);
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|needsTaskCommit
parameter_list|(
name|TaskAttemptContext
name|taskContext
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|baseOutputCommitter
operator|.
name|needsTaskCommit
argument_list|(
name|taskContext
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setupJob
parameter_list|(
name|JobContext
name|jobContext
parameter_list|)
throws|throws
name|IOException
block|{
name|baseOutputCommitter
operator|.
name|setupJob
argument_list|(
name|jobContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setupTask
parameter_list|(
name|TaskAttemptContext
name|taskContext
parameter_list|)
throws|throws
name|IOException
block|{
name|baseOutputCommitter
operator|.
name|setupTask
argument_list|(
name|taskContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abortJob
parameter_list|(
name|JobContext
name|jobContext
parameter_list|,
name|int
name|status
parameter_list|)
throws|throws
name|IOException
block|{
name|baseOutputCommitter
operator|.
name|abortJob
argument_list|(
name|jobContext
argument_list|,
name|status
argument_list|)
expr_stmt|;
name|RevisionManager
name|rm
init|=
literal|null
decl_stmt|;
try|try
block|{
name|rm
operator|=
name|HBaseRevisionManagerUtil
operator|.
name|getOpenedRevisionManager
argument_list|(
name|jobContext
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|rm
operator|.
name|abortWriteTransaction
argument_list|(
name|HBaseRevisionManagerUtil
operator|.
name|getWriteTransaction
argument_list|(
name|jobContext
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|cleanIntermediate
argument_list|(
name|jobContext
argument_list|)
expr_stmt|;
if|if
condition|(
name|rm
operator|!=
literal|null
condition|)
name|rm
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|commitJob
parameter_list|(
name|JobContext
name|jobContext
parameter_list|)
throws|throws
name|IOException
block|{
name|baseOutputCommitter
operator|.
name|commitJob
argument_list|(
name|jobContext
argument_list|)
expr_stmt|;
name|RevisionManager
name|rm
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Configuration
name|conf
init|=
name|jobContext
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|Path
name|srcPath
init|=
name|FileOutputFormat
operator|.
name|getOutputPath
argument_list|(
name|jobContext
operator|.
name|getJobConf
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
operator|.
name|exists
argument_list|(
name|srcPath
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to bulk import hfiles. "
operator|+
literal|"Intermediate data directory is cleaned up or missing. "
operator|+
literal|"Please look at the bulk import job if it exists for failure reason"
argument_list|)
throw|;
block|}
name|Path
name|destPath
init|=
operator|new
name|Path
argument_list|(
name|srcPath
operator|.
name|getParent
argument_list|()
argument_list|,
name|srcPath
operator|.
name|getName
argument_list|()
operator|+
literal|"_hfiles"
argument_list|)
decl_stmt|;
name|boolean
name|success
init|=
name|ImportSequenceFile
operator|.
name|runJob
argument_list|(
name|jobContext
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|HBaseConstants
operator|.
name|PROPERTY_OUTPUT_TABLE_NAME_KEY
argument_list|)
argument_list|,
name|srcPath
argument_list|,
name|destPath
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|cleanIntermediate
argument_list|(
name|jobContext
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to bulk import hfiles."
operator|+
literal|" Please look at the bulk import job for failure reason"
argument_list|)
throw|;
block|}
name|rm
operator|=
name|HBaseRevisionManagerUtil
operator|.
name|getOpenedRevisionManager
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|rm
operator|.
name|commitWriteTransaction
argument_list|(
name|HBaseRevisionManagerUtil
operator|.
name|getWriteTransaction
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|cleanIntermediate
argument_list|(
name|jobContext
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|rm
operator|!=
literal|null
condition|)
name|rm
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|cleanIntermediate
parameter_list|(
name|JobContext
name|jobContext
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|jobContext
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|FileOutputFormat
operator|.
name|getOutputPath
argument_list|(
name|jobContext
operator|.
name|getJobConf
argument_list|()
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

