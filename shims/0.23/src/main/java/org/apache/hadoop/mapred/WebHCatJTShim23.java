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

begin_class
specifier|public
class|class
name|WebHCatJTShim23
implements|implements
name|WebHCatJTShim
block|{
specifier|private
name|JobClient
name|jc
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
return|return
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
name|jc
operator|.
name|getJob
argument_list|(
name|jobid
argument_list|)
decl_stmt|;
name|JobStatus
name|jobStatus
init|=
name|rj
operator|.
name|getJobStatus
argument_list|()
decl_stmt|;
name|JobProfile
name|jobProfile
init|=
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
decl_stmt|;
return|return
name|jobProfile
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
name|jc
operator|.
name|getJob
argument_list|(
name|jobid
argument_list|)
decl_stmt|;
name|JobStatus
name|jobStatus
init|=
name|rj
operator|.
name|getJobStatus
argument_list|()
decl_stmt|;
return|return
name|jobStatus
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
name|jc
operator|.
name|getJob
argument_list|(
name|jobid
argument_list|)
decl_stmt|;
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
block|}
end_class

end_unit

