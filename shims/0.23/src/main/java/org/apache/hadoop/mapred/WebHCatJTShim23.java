begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
package|;
end_package

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
name|TypeConverter
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
name|yarn
operator|.
name|api
operator|.
name|ApplicationClientProtocol
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
name|yarn
operator|.
name|api
operator|.
name|protocolrecords
operator|.
name|ApplicationsRequestScope
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
name|yarn
operator|.
name|api
operator|.
name|protocolrecords
operator|.
name|GetApplicationsRequest
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
name|yarn
operator|.
name|api
operator|.
name|protocolrecords
operator|.
name|GetApplicationsResponse
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|ApplicationId
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|ApplicationReport
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
name|yarn
operator|.
name|client
operator|.
name|ClientRMProxy
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
name|yarn
operator|.
name|client
operator|.
name|api
operator|.
name|YarnClient
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
name|yarn
operator|.
name|exceptions
operator|.
name|YarnException
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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_class
specifier|public
class|class
name|WebHCatJTShim23
implements|implements
name|WebHCatJTShim
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
name|WebHCatJTShim23
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|JobClient
name|jc
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
comment|/**    * Create a connection to the Job Tracker.    */
specifier|public
name|WebHCatJTShim23
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|UserGroupInformation
name|ugi
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|jc
operator|=
name|ugi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|JobClient
argument_list|>
argument_list|()
block|{
specifier|public
name|JobClient
name|run
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|//create this in doAs() so that it gets a security context based passed in 'ugi'
return|return
operator|new
name|JobClient
argument_list|(
name|conf
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to create JobClient"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
comment|/**    * Grab a handle to a job that is already known to the JobTracker.    *    * @return Profile of the job, or null if not found.    */
specifier|public
name|JobProfile
name|getJobProfile
parameter_list|(
name|JobID
name|jobid
parameter_list|)
throws|throws
name|IOException
block|{
name|RunningJob
name|rj
init|=
name|getJob
argument_list|(
name|jobid
argument_list|)
decl_stmt|;
if|if
condition|(
name|rj
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|JobStatus
name|jobStatus
init|=
name|rj
operator|.
name|getJobStatus
argument_list|()
decl_stmt|;
return|return
operator|new
name|JobProfile
argument_list|(
name|jobStatus
operator|.
name|getUsername
argument_list|()
argument_list|,
name|jobStatus
operator|.
name|getJobID
argument_list|()
argument_list|,
name|jobStatus
operator|.
name|getJobFile
argument_list|()
argument_list|,
name|jobStatus
operator|.
name|getTrackingUrl
argument_list|()
argument_list|,
name|jobStatus
operator|.
name|getJobName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Grab a handle to a job that is already known to the JobTracker.    *    * @return Status of the job, or null if not found.    */
specifier|public
name|JobStatus
name|getJobStatus
parameter_list|(
name|JobID
name|jobid
parameter_list|)
throws|throws
name|IOException
block|{
name|RunningJob
name|rj
init|=
name|getJob
argument_list|(
name|jobid
argument_list|)
decl_stmt|;
if|if
condition|(
name|rj
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|rj
operator|.
name|getJobStatus
argument_list|()
return|;
block|}
comment|/**    * Kill a job.    */
specifier|public
name|void
name|killJob
parameter_list|(
name|JobID
name|jobid
parameter_list|)
throws|throws
name|IOException
block|{
name|RunningJob
name|rj
init|=
name|getJob
argument_list|(
name|jobid
argument_list|)
decl_stmt|;
if|if
condition|(
name|rj
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|rj
operator|.
name|killJob
argument_list|()
expr_stmt|;
block|}
comment|/**    * Get all the jobs submitted.    */
specifier|public
name|JobStatus
index|[]
name|getAllJobs
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|jc
operator|.
name|getAllJobs
argument_list|()
return|;
block|}
comment|/**    * Close the connection to the Job Tracker.    */
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
name|jc
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{     }
block|}
annotation|@
name|Override
specifier|public
name|void
name|addCacheFile
parameter_list|(
name|URI
name|uri
parameter_list|,
name|Job
name|job
parameter_list|)
block|{
name|job
operator|.
name|addCacheFile
argument_list|(
name|uri
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return {@code null} if no such application exists    */
specifier|private
name|RunningJob
name|getJob
parameter_list|(
name|JobID
name|jobid
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|jc
operator|.
name|getJob
argument_list|(
name|jobid
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|String
name|msg
init|=
name|ex
operator|.
name|getMessage
argument_list|()
decl_stmt|;
if|if
condition|(
name|msg
operator|!=
literal|null
operator|&&
name|msg
operator|.
name|contains
argument_list|(
literal|"ApplicationNotFoundException"
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Job("
operator|+
name|jobid
operator|+
literal|") not found: "
operator|+
name|msg
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
throw|throw
name|ex
throw|;
block|}
block|}
comment|/**    * Kills all jobs tagged with the given tag that have been started after the    * given timestamp.    */
annotation|@
name|Override
specifier|public
name|void
name|killJobs
parameter_list|(
name|String
name|tag
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Looking for jobs to kill..."
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|ApplicationId
argument_list|>
name|childJobs
init|=
name|getYarnChildJobs
argument_list|(
name|tag
argument_list|,
name|timestamp
argument_list|)
decl_stmt|;
if|if
condition|(
name|childJobs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No jobs found from"
argument_list|)
expr_stmt|;
return|return;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Found MR jobs count: %d"
argument_list|,
name|childJobs
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Killing all found jobs"
argument_list|)
expr_stmt|;
name|YarnClient
name|yarnClient
init|=
name|YarnClient
operator|.
name|createYarnClient
argument_list|()
decl_stmt|;
name|yarnClient
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|yarnClient
operator|.
name|start
argument_list|()
expr_stmt|;
for|for
control|(
name|ApplicationId
name|app
range|:
name|childJobs
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Killing job: %s ..."
argument_list|,
name|app
argument_list|)
argument_list|)
expr_stmt|;
name|yarnClient
operator|.
name|killApplication
argument_list|(
name|app
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Job %s killed"
argument_list|,
name|app
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|YarnException
name|ye
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Exception occurred while killing child job(s)"
argument_list|,
name|ye
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Exception occurred while killing child job(s)"
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
block|}
comment|/**    * Returns all jobs tagged with the given tag that have been started after the    * given timestamp. Returned jobIds are MapReduce JobIds.    */
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getJobs
parameter_list|(
name|String
name|tag
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|Set
argument_list|<
name|ApplicationId
argument_list|>
name|childYarnJobs
init|=
name|getYarnChildJobs
argument_list|(
name|tag
argument_list|,
name|timestamp
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|childJobs
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ApplicationId
name|id
range|:
name|childYarnJobs
control|)
block|{
comment|// Convert to a MapReduce job id
name|String
name|childJobId
init|=
name|TypeConverter
operator|.
name|fromYarn
argument_list|(
name|id
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|childJobs
operator|.
name|add
argument_list|(
name|childJobId
argument_list|)
expr_stmt|;
block|}
return|return
name|childJobs
return|;
block|}
comment|/**    * Queries RM for the list of applications with the given tag that have started    * after the given timestamp.    */
specifier|private
name|Set
argument_list|<
name|ApplicationId
argument_list|>
name|getYarnChildJobs
parameter_list|(
name|String
name|tag
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|Set
argument_list|<
name|ApplicationId
argument_list|>
name|childYarnJobs
init|=
operator|new
name|HashSet
argument_list|<
name|ApplicationId
argument_list|>
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Querying RM for tag = %s, starting with ts = %s"
argument_list|,
name|tag
argument_list|,
name|timestamp
argument_list|)
argument_list|)
expr_stmt|;
name|GetApplicationsRequest
name|gar
init|=
name|GetApplicationsRequest
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|gar
operator|.
name|setScope
argument_list|(
name|ApplicationsRequestScope
operator|.
name|OWN
argument_list|)
expr_stmt|;
name|gar
operator|.
name|setStartRange
argument_list|(
name|timestamp
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|gar
operator|.
name|setApplicationTags
argument_list|(
name|Collections
operator|.
name|singleton
argument_list|(
name|tag
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|ApplicationClientProtocol
name|proxy
init|=
name|ClientRMProxy
operator|.
name|createRMProxy
argument_list|(
name|conf
argument_list|,
name|ApplicationClientProtocol
operator|.
name|class
argument_list|)
decl_stmt|;
name|GetApplicationsResponse
name|apps
init|=
name|proxy
operator|.
name|getApplications
argument_list|(
name|gar
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ApplicationReport
argument_list|>
name|appsList
init|=
name|apps
operator|.
name|getApplicationList
argument_list|()
decl_stmt|;
for|for
control|(
name|ApplicationReport
name|appReport
range|:
name|appsList
control|)
block|{
name|childYarnJobs
operator|.
name|add
argument_list|(
name|appReport
operator|.
name|getApplicationId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Exception occurred while finding child jobs"
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|YarnException
name|ye
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Exception occurred while finding child jobs"
argument_list|,
name|ye
argument_list|)
throw|;
block|}
return|return
name|childYarnJobs
return|;
block|}
block|}
end_class

end_unit

