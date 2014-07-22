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
name|filecache
operator|.
name|DistributedCache
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
name|ipc
operator|.
name|RPC
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
name|net
operator|.
name|NetUtils
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
name|InetSocketAddress
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

begin_comment
comment|/**  * This is in org.apache.hadoop.mapred package because it relies on  * JobSubmissionProtocol which is package private  */
end_comment

begin_class
specifier|public
class|class
name|WebHCatJTShim20S
implements|implements
name|WebHCatJTShim
block|{
specifier|private
name|JobSubmissionProtocol
name|cnx
decl_stmt|;
comment|/**    * Create a connection to the Job Tracker.    */
specifier|public
name|WebHCatJTShim20S
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|UserGroupInformation
name|ugi
parameter_list|)
throws|throws
name|IOException
block|{
name|cnx
operator|=
operator|(
name|JobSubmissionProtocol
operator|)
name|RPC
operator|.
name|getProxy
argument_list|(
name|JobSubmissionProtocol
operator|.
name|class
argument_list|,
name|JobSubmissionProtocol
operator|.
name|versionID
argument_list|,
name|getAddress
argument_list|(
name|conf
argument_list|)
argument_list|,
name|ugi
argument_list|,
name|conf
argument_list|,
name|NetUtils
operator|.
name|getSocketFactory
argument_list|(
name|conf
argument_list|,
name|JobSubmissionProtocol
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Grab a handle to a job that is already known to the JobTracker.    *    * @return Profile of the job, or null if not found.    */
specifier|public
name|JobProfile
name|getJobProfile
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobID
name|jobid
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|cnx
operator|.
name|getJobProfile
argument_list|(
name|jobid
argument_list|)
return|;
block|}
comment|/**    * Grab a handle to a job that is already known to the JobTracker.    *    * @return Status of the job, or null if not found.    */
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobStatus
name|getJobStatus
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobID
name|jobid
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|cnx
operator|.
name|getJobStatus
argument_list|(
name|jobid
argument_list|)
return|;
block|}
comment|/**    * Kill a job.    */
specifier|public
name|void
name|killJob
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobID
name|jobid
parameter_list|)
throws|throws
name|IOException
block|{
name|cnx
operator|.
name|killJob
argument_list|(
name|jobid
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get all the jobs submitted.    */
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobStatus
index|[]
name|getAllJobs
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|cnx
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
name|RPC
operator|.
name|stopProxy
argument_list|(
name|cnx
argument_list|)
expr_stmt|;
block|}
specifier|private
name|InetSocketAddress
name|getAddress
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|jobTrackerStr
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"mapred.job.tracker"
argument_list|,
literal|"localhost:8012"
argument_list|)
decl_stmt|;
return|return
name|NetUtils
operator|.
name|createSocketAddr
argument_list|(
name|jobTrackerStr
argument_list|)
return|;
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
name|DistributedCache
operator|.
name|addCacheFile
argument_list|(
name|uri
argument_list|,
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Kill jobs is only supported on hadoop 2.0+.    */
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
return|return;
block|}
block|}
end_class

end_unit

