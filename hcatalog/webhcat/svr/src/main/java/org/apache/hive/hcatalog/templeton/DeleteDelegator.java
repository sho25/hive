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
comment|/**  * Delete a job  */
end_comment

begin_class
specifier|public
class|class
name|DeleteDelegator
extends|extends
name|TempletonDelegator
block|{
specifier|public
name|DeleteDelegator
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
name|tracker
operator|.
name|killJob
argument_list|(
name|jobid
argument_list|)
expr_stmt|;
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
name|String
name|childid
init|=
name|state
operator|.
name|getChildId
argument_list|()
decl_stmt|;
if|if
condition|(
name|childid
operator|!=
literal|null
condition|)
name|tracker
operator|.
name|killJob
argument_list|(
name|StatusDelegator
operator|.
name|StringToJobID
argument_list|(
name|childid
argument_list|)
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
block|}
end_class

end_unit

