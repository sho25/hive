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
name|ql
operator|.
name|session
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * A front handle for managing job submission to Yarn-FairScheduler.  */
end_comment

begin_class
specifier|public
class|class
name|YarnFairScheduling
block|{
comment|/**    * Determine if jobs can be configured for YARN fair scheduling.    * @param conf - the current HiveConf configuration.    * @return Returns true when impersonation mode is disabled and fair-scheduling is enabled.    */
specifier|public
specifier|static
name|boolean
name|usingNonImpersonationModeWithFairScheduling
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
return|return
operator|(
name|conf
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
argument_list|)
operator|&&
operator|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_MAP_FAIR_SCHEDULER_QUEUE
argument_list|)
operator|)
operator|)
return|;
block|}
comment|/**    * Configure the default YARN queue for the user.    * @param conf - The current HiveConf configuration.    * @param forUser - The user to configure scheduling for.    * @throws IOException    * @throws HiveException    */
specifier|public
specifier|static
name|void
name|setDefaultJobQueue
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|forUser
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveException
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|usingNonImpersonationModeWithFairScheduling
argument_list|(
name|conf
argument_list|)
argument_list|,
literal|"Unable to map job to fair-scheduler because either impersonation is on or fair-scheduling is disabled."
argument_list|)
expr_stmt|;
name|ShimLoader
operator|.
name|getSchedulerShims
argument_list|()
operator|.
name|refreshDefaultQueue
argument_list|(
name|conf
argument_list|,
name|forUser
argument_list|)
expr_stmt|;
block|}
comment|/**    * Validate the current YARN queue for the current user.    * @param conf - The current HiveConf configuration.    * @param forUser - The user to configure scheduling for.    * @throws IOException    * @throws HiveException    */
specifier|public
specifier|static
name|void
name|validateYarnQueue
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|forUser
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveException
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|usingNonImpersonationModeWithFairScheduling
argument_list|(
name|conf
argument_list|)
argument_list|,
literal|"Unable to map job to fair-scheduler because either impersonation is on or fair-scheduling is disabled."
argument_list|)
expr_stmt|;
name|ShimLoader
operator|.
name|getSchedulerShims
argument_list|()
operator|.
name|validateQueueConfiguration
argument_list|(
name|conf
argument_list|,
name|forUser
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

