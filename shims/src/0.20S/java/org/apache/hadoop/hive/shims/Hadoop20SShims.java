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
name|hive
operator|.
name|shims
package|;
end_package

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|MalformedURLException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
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
name|hive
operator|.
name|shims
operator|.
name|HadoopShimsSecure
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
name|ClusterStatus
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
name|TaskLogServlet
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
name|TaskAttemptID
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

begin_comment
comment|/**  * Implemention of shims against Hadoop 0.20 with Security.  */
end_comment

begin_class
specifier|public
class|class
name|Hadoop20SShims
extends|extends
name|HadoopShimsSecure
block|{
annotation|@
name|Override
specifier|public
name|String
name|getTaskAttemptLogUrl
parameter_list|(
name|JobConf
name|conf
parameter_list|,
name|String
name|taskTrackerHttpAddress
parameter_list|,
name|String
name|taskAttemptId
parameter_list|)
throws|throws
name|MalformedURLException
block|{
name|URL
name|taskTrackerHttpURL
init|=
operator|new
name|URL
argument_list|(
name|taskTrackerHttpAddress
argument_list|)
decl_stmt|;
return|return
name|TaskLogServlet
operator|.
name|getTaskLogUrl
argument_list|(
name|taskTrackerHttpURL
operator|.
name|getHost
argument_list|()
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|taskTrackerHttpURL
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|,
name|taskAttemptId
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|JobTrackerState
name|getJobTrackerState
parameter_list|(
name|ClusterStatus
name|clusterStatus
parameter_list|)
throws|throws
name|Exception
block|{
name|JobTrackerState
name|state
decl_stmt|;
switch|switch
condition|(
name|clusterStatus
operator|.
name|getJobTrackerState
argument_list|()
condition|)
block|{
case|case
name|INITIALIZING
case|:
return|return
name|JobTrackerState
operator|.
name|INITIALIZING
return|;
case|case
name|RUNNING
case|:
return|return
name|JobTrackerState
operator|.
name|RUNNING
return|;
default|default:
name|String
name|errorMsg
init|=
literal|"Unrecognized JobTracker state: "
operator|+
name|clusterStatus
operator|.
name|getJobTrackerState
argument_list|()
decl_stmt|;
throw|throw
operator|new
name|Exception
argument_list|(
name|errorMsg
argument_list|)
throw|;
block|}
block|}
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
name|TaskAttemptContext
name|newTaskAttemptContext
parameter_list|(
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Progressable
name|progressable
parameter_list|)
block|{
return|return
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|TaskAttemptContext
argument_list|(
name|conf
argument_list|,
operator|new
name|TaskAttemptID
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|progress
parameter_list|()
block|{
name|progressable
operator|.
name|progress
argument_list|()
expr_stmt|;
block|}
block|}
return|;
block|}
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
name|JobContext
name|newJobContext
parameter_list|(
name|Job
name|job
parameter_list|)
block|{
return|return
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|mapreduce
operator|.
name|JobContext
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|job
operator|.
name|getJobID
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

