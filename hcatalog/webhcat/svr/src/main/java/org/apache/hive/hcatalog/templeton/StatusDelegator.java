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
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
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
name|shims
operator|.
name|HadoopShims
operator|.
name|WebHCatJTShim
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|security
operator|.
name|UserGroupInformation
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
name|JobID
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
name|JobProfile
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
name|JobStatus
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
name|templeton
operator|.
name|tool
operator|.
name|JobState
import|;
end_import

begin_comment
comment|/**  * Fetch the status of a given job id in the queue. There are three sources of the info  * 1. Query result from JobTracker  * 2. JobState saved by TempletonControllerJob when monitoring the TempletonControllerJob  * 3. TempletonControllerJob put a JobState for every job it launches, so child job can  *    retrieve its parent job by its JobState  *   * Currently there is no permission restriction, any user can query any job  */
end_comment

begin_class
specifier|public
class|class
name|StatusDelegator
extends|extends
name|TempletonDelegator
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
name|StatusDelegator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|StatusDelegator
parameter_list|(
name|AppConfig
name|appConf
parameter_list|)
block|{
name|super
argument_list|(
name|appConf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|QueueStatusBean
name|run
parameter_list|(
name|String
name|user
parameter_list|,
name|String
name|id
parameter_list|)
throws|throws
name|NotAuthorizedException
throws|,
name|BadParam
throws|,
name|IOException
throws|,
name|InterruptedException
block|{
name|WebHCatJTShim
name|tracker
init|=
literal|null
decl_stmt|;
name|JobState
name|state
init|=
literal|null
decl_stmt|;
try|try
block|{
name|UserGroupInformation
name|ugi
init|=
name|UgiFactory
operator|.
name|getUgi
argument_list|(
name|user
argument_list|)
decl_stmt|;
name|tracker
operator|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getWebHCatShim
argument_list|(
name|appConf
argument_list|,
name|ugi
argument_list|)
expr_stmt|;
name|JobID
name|jobid
init|=
name|StatusDelegator
operator|.
name|StringToJobID
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|jobid
operator|==
literal|null
condition|)
throw|throw
operator|new
name|BadParam
argument_list|(
literal|"Invalid jobid: "
operator|+
name|id
argument_list|)
throw|;
name|state
operator|=
operator|new
name|JobState
argument_list|(
name|id
argument_list|,
name|Main
operator|.
name|getAppConfigInstance
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|StatusDelegator
operator|.
name|makeStatus
argument_list|(
name|tracker
argument_list|,
name|jobid
argument_list|,
name|state
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BadParam
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|tracker
operator|!=
literal|null
condition|)
name|tracker
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|state
operator|!=
literal|null
condition|)
name|state
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|static
name|QueueStatusBean
name|makeStatus
parameter_list|(
name|WebHCatJTShim
name|tracker
parameter_list|,
name|JobID
name|jobid
parameter_list|,
name|JobState
name|state
parameter_list|)
throws|throws
name|BadParam
throws|,
name|IOException
block|{
name|JobStatus
name|status
init|=
name|tracker
operator|.
name|getJobStatus
argument_list|(
name|jobid
argument_list|)
decl_stmt|;
name|JobProfile
name|profile
init|=
name|tracker
operator|.
name|getJobProfile
argument_list|(
name|jobid
argument_list|)
decl_stmt|;
if|if
condition|(
name|status
operator|==
literal|null
operator|||
name|profile
operator|==
literal|null
condition|)
comment|// No such job.
throw|throw
operator|new
name|BadParam
argument_list|(
literal|"Could not find job "
operator|+
name|jobid
argument_list|)
throw|;
return|return
operator|new
name|QueueStatusBean
argument_list|(
name|state
argument_list|,
name|status
argument_list|,
name|profile
argument_list|)
return|;
block|}
comment|/**    * A version of JobID.forName with our app specific error handling.    */
specifier|public
specifier|static
name|JobID
name|StringToJobID
parameter_list|(
name|String
name|id
parameter_list|)
throws|throws
name|BadParam
block|{
try|try
block|{
return|return
name|JobID
operator|.
name|forName
argument_list|(
name|id
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BadParam
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

