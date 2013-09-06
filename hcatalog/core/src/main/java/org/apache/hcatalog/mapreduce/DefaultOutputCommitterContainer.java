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
name|mapreduce
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
name|metastore
operator|.
name|HiveMetaStoreClient
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
name|mapreduce
operator|.
name|HCatMapRedUtil
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
name|TaskAttemptContext
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
name|HCatConstants
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
name|HCatUtil
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

begin_comment
comment|/**  * Part of the DefaultOutput*Container classes  * See {@link DefaultOutputFormatContainer} for more information  * @deprecated Use/modify {@link org.apache.hive.hcatalog.mapreduce.DefaultOutputCommitterContainer} instead  */
end_comment

begin_class
class|class
name|DefaultOutputCommitterContainer
extends|extends
name|OutputCommitterContainer
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
name|DefaultOutputCommitterContainer
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**      * @param context current JobContext      * @param baseCommitter OutputCommitter to contain      * @throws IOException      */
specifier|public
name|DefaultOutputCommitterContainer
parameter_list|(
name|JobContext
name|context
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|OutputCommitter
name|baseCommitter
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|context
argument_list|,
name|baseCommitter
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abortTask
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|getBaseOutputCommitter
argument_list|()
operator|.
name|abortTask
argument_list|(
name|HCatMapRedUtil
operator|.
name|createTaskAttemptContext
argument_list|(
name|context
argument_list|)
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
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|getBaseOutputCommitter
argument_list|()
operator|.
name|commitTask
argument_list|(
name|HCatMapRedUtil
operator|.
name|createTaskAttemptContext
argument_list|(
name|context
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|needsTaskCommit
parameter_list|(
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getBaseOutputCommitter
argument_list|()
operator|.
name|needsTaskCommit
argument_list|(
name|HCatMapRedUtil
operator|.
name|createTaskAttemptContext
argument_list|(
name|context
argument_list|)
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
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|getBaseOutputCommitter
argument_list|()
operator|.
name|setupJob
argument_list|(
name|HCatMapRedUtil
operator|.
name|createJobContext
argument_list|(
name|context
argument_list|)
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
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|getBaseOutputCommitter
argument_list|()
operator|.
name|setupTask
argument_list|(
name|HCatMapRedUtil
operator|.
name|createTaskAttemptContext
argument_list|(
name|context
argument_list|)
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
name|State
name|state
parameter_list|)
throws|throws
name|IOException
block|{
name|getBaseOutputCommitter
argument_list|()
operator|.
name|abortJob
argument_list|(
name|HCatMapRedUtil
operator|.
name|createJobContext
argument_list|(
name|jobContext
argument_list|)
argument_list|,
name|state
argument_list|)
expr_stmt|;
name|cleanupJob
argument_list|(
name|jobContext
argument_list|)
expr_stmt|;
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
name|getBaseOutputCommitter
argument_list|()
operator|.
name|commitJob
argument_list|(
name|HCatMapRedUtil
operator|.
name|createJobContext
argument_list|(
name|jobContext
argument_list|)
argument_list|)
expr_stmt|;
name|cleanupJob
argument_list|(
name|jobContext
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cleanupJob
parameter_list|(
name|JobContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|getBaseOutputCommitter
argument_list|()
operator|.
name|cleanupJob
argument_list|(
name|HCatMapRedUtil
operator|.
name|createJobContext
argument_list|(
name|context
argument_list|)
argument_list|)
expr_stmt|;
comment|//Cancel HCat and JobTracker tokens
name|HiveMetaStoreClient
name|client
init|=
literal|null
decl_stmt|;
try|try
block|{
name|HiveConf
name|hiveConf
init|=
name|HCatUtil
operator|.
name|getHiveConf
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|client
operator|=
name|HCatUtil
operator|.
name|getHiveClient
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|String
name|tokenStrForm
init|=
name|client
operator|.
name|getTokenStrForm
argument_list|()
decl_stmt|;
if|if
condition|(
name|tokenStrForm
operator|!=
literal|null
operator|&&
name|context
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|HCatConstants
operator|.
name|HCAT_KEY_TOKEN_SIGNATURE
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|client
operator|.
name|cancelDelegationToken
argument_list|(
name|tokenStrForm
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to cancel delegation token"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HCatUtil
operator|.
name|closeHiveClientQuietly
argument_list|(
name|client
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

