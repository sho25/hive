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
name|mapred
operator|.
name|TableOutputFormat
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
name|Transaction
import|;
end_import

begin_comment
comment|/**  * "Direct" implementation of OutputFormat for HBase. Uses HTable client's put  * API to write each row to HBase one a time. Presently it is just using  * TableOutputFormat as the underlying implementation in the future we can tune  * this to make the writes faster such as permanently disabling WAL, caching,  * etc.  */
end_comment

begin_class
class|class
name|HBaseDirectOutputFormat
extends|extends
name|HBaseBaseOutputFormat
block|{
specifier|private
name|TableOutputFormat
name|outputFormat
decl_stmt|;
specifier|public
name|HBaseDirectOutputFormat
parameter_list|()
block|{
name|this
operator|.
name|outputFormat
operator|=
operator|new
name|TableOutputFormat
argument_list|()
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
name|HBaseDirectRecordWriter
argument_list|(
name|outputFormat
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
name|outputFormat
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
block|}
specifier|private
specifier|static
class|class
name|HBaseDirectRecordWriter
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
name|HBaseDirectRecordWriter
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
name|baseWriter
operator|.
name|write
argument_list|(
name|key
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
name|HBaseDirectOutputCommitter
extends|extends
name|OutputCommitter
block|{
specifier|public
name|HBaseDirectOutputCommitter
parameter_list|()
throws|throws
name|IOException
block|{     }
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
block|{     }
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
block|{     }
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
literal|false
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
block|{     }
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
block|{     }
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
name|super
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
name|Transaction
name|writeTransaction
init|=
name|HBaseRevisionManagerUtil
operator|.
name|getWriteTransaction
argument_list|(
name|jobContext
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|rm
operator|.
name|abortWriteTransaction
argument_list|(
name|writeTransaction
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
name|commitWriteTransaction
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
block|}
block|}
end_class

end_unit

