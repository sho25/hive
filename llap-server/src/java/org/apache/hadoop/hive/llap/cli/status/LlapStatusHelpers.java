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
name|llap
operator|.
name|cli
operator|.
name|status
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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
name|Map
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
name|llap
operator|.
name|cli
operator|.
name|LlapStatusServiceDriver
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|annotate
operator|.
name|JsonIgnore
import|;
end_import

begin_class
specifier|public
class|class
name|LlapStatusHelpers
block|{
specifier|public
enum|enum
name|State
block|{
name|APP_NOT_FOUND
block|,
name|LAUNCHING
block|,
name|RUNNING_PARTIAL
block|,
name|RUNNING_ALL
block|,
name|COMPLETE
block|,
name|UNKNOWN
block|}
specifier|public
specifier|static
class|class
name|AmInfo
block|{
specifier|private
name|String
name|appName
decl_stmt|;
specifier|private
name|String
name|appType
decl_stmt|;
specifier|private
name|String
name|appId
decl_stmt|;
specifier|private
name|String
name|containerId
decl_stmt|;
specifier|private
name|String
name|hostname
decl_stmt|;
specifier|private
name|String
name|amWebUrl
decl_stmt|;
specifier|public
name|AmInfo
name|setAppName
parameter_list|(
name|String
name|appName
parameter_list|)
block|{
name|this
operator|.
name|appName
operator|=
name|appName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AmInfo
name|setAppType
parameter_list|(
name|String
name|appType
parameter_list|)
block|{
name|this
operator|.
name|appType
operator|=
name|appType
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AmInfo
name|setAppId
parameter_list|(
name|String
name|appId
parameter_list|)
block|{
name|this
operator|.
name|appId
operator|=
name|appId
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AmInfo
name|setContainerId
parameter_list|(
name|String
name|containerId
parameter_list|)
block|{
name|this
operator|.
name|containerId
operator|=
name|containerId
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AmInfo
name|setHostname
parameter_list|(
name|String
name|hostname
parameter_list|)
block|{
name|this
operator|.
name|hostname
operator|=
name|hostname
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AmInfo
name|setAmWebUrl
parameter_list|(
name|String
name|amWebUrl
parameter_list|)
block|{
name|this
operator|.
name|amWebUrl
operator|=
name|amWebUrl
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|String
name|getAppName
parameter_list|()
block|{
return|return
name|appName
return|;
block|}
specifier|public
name|String
name|getAppType
parameter_list|()
block|{
return|return
name|appType
return|;
block|}
specifier|public
name|String
name|getAppId
parameter_list|()
block|{
return|return
name|appId
return|;
block|}
specifier|public
name|String
name|getContainerId
parameter_list|()
block|{
return|return
name|containerId
return|;
block|}
specifier|public
name|String
name|getHostname
parameter_list|()
block|{
return|return
name|hostname
return|;
block|}
specifier|public
name|String
name|getAmWebUrl
parameter_list|()
block|{
return|return
name|amWebUrl
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"AmInfo{"
operator|+
literal|"appName='"
operator|+
name|appName
operator|+
literal|'\''
operator|+
literal|", appType='"
operator|+
name|appType
operator|+
literal|'\''
operator|+
literal|", appId='"
operator|+
name|appId
operator|+
literal|'\''
operator|+
literal|", containerId='"
operator|+
name|containerId
operator|+
literal|'\''
operator|+
literal|", hostname='"
operator|+
name|hostname
operator|+
literal|'\''
operator|+
literal|", amWebUrl='"
operator|+
name|amWebUrl
operator|+
literal|'\''
operator|+
literal|'}'
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|LlapInstance
block|{
specifier|private
specifier|final
name|String
name|hostname
decl_stmt|;
specifier|private
specifier|final
name|String
name|containerId
decl_stmt|;
specifier|private
name|String
name|logUrl
decl_stmt|;
comment|// Only for live instances.
specifier|private
name|String
name|statusUrl
decl_stmt|;
specifier|private
name|String
name|webUrl
decl_stmt|;
specifier|private
name|Integer
name|rpcPort
decl_stmt|;
specifier|private
name|Integer
name|mgmtPort
decl_stmt|;
specifier|private
name|Integer
name|shufflePort
decl_stmt|;
comment|// For completed instances
specifier|private
name|String
name|diagnostics
decl_stmt|;
specifier|private
name|int
name|yarnContainerExitStatus
decl_stmt|;
comment|// TODO HIVE-13454 Add additional information such as #executors, container size, etc
specifier|public
name|LlapInstance
parameter_list|(
name|String
name|hostname
parameter_list|,
name|String
name|containerId
parameter_list|)
block|{
name|this
operator|.
name|hostname
operator|=
name|hostname
expr_stmt|;
name|this
operator|.
name|containerId
operator|=
name|containerId
expr_stmt|;
block|}
specifier|public
name|LlapInstance
name|setLogUrl
parameter_list|(
name|String
name|logUrl
parameter_list|)
block|{
name|this
operator|.
name|logUrl
operator|=
name|logUrl
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|LlapInstance
name|setWebUrl
parameter_list|(
name|String
name|webUrl
parameter_list|)
block|{
name|this
operator|.
name|webUrl
operator|=
name|webUrl
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|LlapInstance
name|setStatusUrl
parameter_list|(
name|String
name|statusUrl
parameter_list|)
block|{
name|this
operator|.
name|statusUrl
operator|=
name|statusUrl
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|LlapInstance
name|setRpcPort
parameter_list|(
name|int
name|rpcPort
parameter_list|)
block|{
name|this
operator|.
name|rpcPort
operator|=
name|rpcPort
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|LlapInstance
name|setMgmtPort
parameter_list|(
name|int
name|mgmtPort
parameter_list|)
block|{
name|this
operator|.
name|mgmtPort
operator|=
name|mgmtPort
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|LlapInstance
name|setShufflePort
parameter_list|(
name|int
name|shufflePort
parameter_list|)
block|{
name|this
operator|.
name|shufflePort
operator|=
name|shufflePort
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|LlapInstance
name|setDiagnostics
parameter_list|(
name|String
name|diagnostics
parameter_list|)
block|{
name|this
operator|.
name|diagnostics
operator|=
name|diagnostics
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|LlapInstance
name|setYarnContainerExitStatus
parameter_list|(
name|int
name|yarnContainerExitStatus
parameter_list|)
block|{
name|this
operator|.
name|yarnContainerExitStatus
operator|=
name|yarnContainerExitStatus
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|String
name|getHostname
parameter_list|()
block|{
return|return
name|hostname
return|;
block|}
specifier|public
name|String
name|getLogUrl
parameter_list|()
block|{
return|return
name|logUrl
return|;
block|}
specifier|public
name|String
name|getStatusUrl
parameter_list|()
block|{
return|return
name|statusUrl
return|;
block|}
specifier|public
name|String
name|getContainerId
parameter_list|()
block|{
return|return
name|containerId
return|;
block|}
specifier|public
name|String
name|getWebUrl
parameter_list|()
block|{
return|return
name|webUrl
return|;
block|}
specifier|public
name|Integer
name|getRpcPort
parameter_list|()
block|{
return|return
name|rpcPort
return|;
block|}
specifier|public
name|Integer
name|getMgmtPort
parameter_list|()
block|{
return|return
name|mgmtPort
return|;
block|}
specifier|public
name|Integer
name|getShufflePort
parameter_list|()
block|{
return|return
name|shufflePort
return|;
block|}
specifier|public
name|String
name|getDiagnostics
parameter_list|()
block|{
return|return
name|diagnostics
return|;
block|}
specifier|public
name|int
name|getYarnContainerExitStatus
parameter_list|()
block|{
return|return
name|yarnContainerExitStatus
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"LlapInstance{"
operator|+
literal|"hostname='"
operator|+
name|hostname
operator|+
literal|'\''
operator|+
literal|"logUrl="
operator|+
name|logUrl
operator|+
literal|'\''
operator|+
literal|", containerId='"
operator|+
name|containerId
operator|+
literal|'\''
operator|+
literal|", statusUrl='"
operator|+
name|statusUrl
operator|+
literal|'\''
operator|+
literal|", webUrl='"
operator|+
name|webUrl
operator|+
literal|'\''
operator|+
literal|", rpcPort="
operator|+
name|rpcPort
operator|+
literal|", mgmtPort="
operator|+
name|mgmtPort
operator|+
literal|", shufflePort="
operator|+
name|shufflePort
operator|+
literal|", diagnostics="
operator|+
name|diagnostics
operator|+
literal|", yarnContainerExitStatus="
operator|+
name|yarnContainerExitStatus
operator|+
literal|'}'
return|;
block|}
block|}
specifier|public
specifier|static
specifier|final
class|class
name|AppStatusBuilder
block|{
specifier|private
name|AmInfo
name|amInfo
decl_stmt|;
specifier|private
name|State
name|state
init|=
name|State
operator|.
name|UNKNOWN
decl_stmt|;
specifier|private
name|String
name|diagnostics
decl_stmt|;
specifier|private
name|String
name|originalConfigurationPath
decl_stmt|;
specifier|private
name|String
name|generatedConfigurationPath
decl_stmt|;
specifier|private
name|Integer
name|desiredInstances
init|=
literal|null
decl_stmt|;
specifier|private
name|Integer
name|liveInstances
init|=
literal|null
decl_stmt|;
specifier|private
name|Integer
name|launchingInstances
init|=
literal|null
decl_stmt|;
specifier|private
name|Long
name|appStartTime
decl_stmt|;
specifier|private
name|Long
name|appFinishTime
decl_stmt|;
specifier|private
name|boolean
name|runningThresholdAchieved
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|LlapInstance
argument_list|>
name|runningInstances
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|LlapInstance
argument_list|>
name|completedInstances
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|LlapInstance
argument_list|>
name|containerToRunningInstanceMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|LlapInstance
argument_list|>
name|containerToCompletedInstanceMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|void
name|setAmInfo
parameter_list|(
name|AmInfo
name|amInfo
parameter_list|)
block|{
name|this
operator|.
name|amInfo
operator|=
name|amInfo
expr_stmt|;
block|}
specifier|public
name|AppStatusBuilder
name|setState
parameter_list|(
name|State
name|state
parameter_list|)
block|{
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AppStatusBuilder
name|setDiagnostics
parameter_list|(
name|String
name|diagnostics
parameter_list|)
block|{
name|this
operator|.
name|diagnostics
operator|=
name|diagnostics
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AppStatusBuilder
name|setOriginalConfigurationPath
parameter_list|(
name|String
name|originalConfigurationPath
parameter_list|)
block|{
name|this
operator|.
name|originalConfigurationPath
operator|=
name|originalConfigurationPath
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AppStatusBuilder
name|setGeneratedConfigurationPath
parameter_list|(
name|String
name|generatedConfigurationPath
parameter_list|)
block|{
name|this
operator|.
name|generatedConfigurationPath
operator|=
name|generatedConfigurationPath
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AppStatusBuilder
name|setAppStartTime
parameter_list|(
name|long
name|appStartTime
parameter_list|)
block|{
name|this
operator|.
name|appStartTime
operator|=
name|appStartTime
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AppStatusBuilder
name|setAppFinishTime
parameter_list|(
name|long
name|finishTime
parameter_list|)
block|{
name|this
operator|.
name|appFinishTime
operator|=
name|finishTime
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|void
name|setRunningThresholdAchieved
parameter_list|(
name|boolean
name|runningThresholdAchieved
parameter_list|)
block|{
name|this
operator|.
name|runningThresholdAchieved
operator|=
name|runningThresholdAchieved
expr_stmt|;
block|}
specifier|public
name|AppStatusBuilder
name|setDesiredInstances
parameter_list|(
name|int
name|desiredInstances
parameter_list|)
block|{
name|this
operator|.
name|desiredInstances
operator|=
name|desiredInstances
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AppStatusBuilder
name|setLiveInstances
parameter_list|(
name|int
name|liveInstances
parameter_list|)
block|{
name|this
operator|.
name|liveInstances
operator|=
name|liveInstances
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AppStatusBuilder
name|setLaunchingInstances
parameter_list|(
name|int
name|launchingInstances
parameter_list|)
block|{
name|this
operator|.
name|launchingInstances
operator|=
name|launchingInstances
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|AppStatusBuilder
name|addNewRunningLlapInstance
parameter_list|(
name|LlapInstance
name|llapInstance
parameter_list|)
block|{
name|this
operator|.
name|runningInstances
operator|.
name|add
argument_list|(
name|llapInstance
argument_list|)
expr_stmt|;
name|this
operator|.
name|containerToRunningInstanceMap
operator|.
name|put
argument_list|(
name|llapInstance
operator|.
name|getContainerId
argument_list|()
argument_list|,
name|llapInstance
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|LlapInstance
name|removeAndGetRunningLlapInstanceForContainer
parameter_list|(
name|String
name|containerIdString
parameter_list|)
block|{
return|return
name|containerToRunningInstanceMap
operator|.
name|remove
argument_list|(
name|containerIdString
argument_list|)
return|;
block|}
specifier|public
name|void
name|clearRunningLlapInstances
parameter_list|()
block|{
name|this
operator|.
name|runningInstances
operator|.
name|clear
argument_list|()
expr_stmt|;
name|this
operator|.
name|containerToRunningInstanceMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|public
name|AppStatusBuilder
name|clearAndAddPreviouslyKnownRunningInstances
parameter_list|(
name|List
argument_list|<
name|LlapInstance
argument_list|>
name|llapInstances
parameter_list|)
block|{
name|clearRunningLlapInstances
argument_list|()
expr_stmt|;
for|for
control|(
name|LlapInstance
name|llapInstance
range|:
name|llapInstances
control|)
block|{
name|addNewRunningLlapInstance
argument_list|(
name|llapInstance
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
annotation|@
name|JsonIgnore
specifier|public
name|List
argument_list|<
name|LlapInstance
argument_list|>
name|allRunningInstances
parameter_list|()
block|{
return|return
name|this
operator|.
name|runningInstances
return|;
block|}
specifier|public
name|AppStatusBuilder
name|addNewCompleteLlapInstance
parameter_list|(
name|LlapInstance
name|llapInstance
parameter_list|)
block|{
name|this
operator|.
name|completedInstances
operator|.
name|add
argument_list|(
name|llapInstance
argument_list|)
expr_stmt|;
name|this
operator|.
name|containerToCompletedInstanceMap
operator|.
name|put
argument_list|(
name|llapInstance
operator|.
name|getContainerId
argument_list|()
argument_list|,
name|llapInstance
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|LlapInstance
name|removeAndGetCompletedLlapInstanceForContainer
parameter_list|(
name|String
name|containerIdString
parameter_list|)
block|{
return|return
name|containerToCompletedInstanceMap
operator|.
name|remove
argument_list|(
name|containerIdString
argument_list|)
return|;
block|}
specifier|public
name|void
name|clearCompletedLlapInstances
parameter_list|()
block|{
name|this
operator|.
name|completedInstances
operator|.
name|clear
argument_list|()
expr_stmt|;
name|this
operator|.
name|containerToCompletedInstanceMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|public
name|AppStatusBuilder
name|clearAndAddPreviouslyKnownCompletedInstances
parameter_list|(
name|List
argument_list|<
name|LlapInstance
argument_list|>
name|llapInstances
parameter_list|)
block|{
name|clearCompletedLlapInstances
argument_list|()
expr_stmt|;
for|for
control|(
name|LlapInstance
name|llapInstance
range|:
name|llapInstances
control|)
block|{
name|addNewCompleteLlapInstance
argument_list|(
name|llapInstance
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
annotation|@
name|JsonIgnore
specifier|public
name|List
argument_list|<
name|LlapInstance
argument_list|>
name|allCompletedInstances
parameter_list|()
block|{
return|return
name|this
operator|.
name|completedInstances
return|;
block|}
specifier|public
name|AmInfo
name|getAmInfo
parameter_list|()
block|{
return|return
name|amInfo
return|;
block|}
specifier|public
name|State
name|getState
parameter_list|()
block|{
return|return
name|state
return|;
block|}
specifier|public
name|String
name|getDiagnostics
parameter_list|()
block|{
return|return
name|diagnostics
return|;
block|}
specifier|public
name|String
name|getOriginalConfigurationPath
parameter_list|()
block|{
return|return
name|originalConfigurationPath
return|;
block|}
specifier|public
name|String
name|getGeneratedConfigurationPath
parameter_list|()
block|{
return|return
name|generatedConfigurationPath
return|;
block|}
specifier|public
name|Integer
name|getDesiredInstances
parameter_list|()
block|{
return|return
name|desiredInstances
return|;
block|}
specifier|public
name|Integer
name|getLiveInstances
parameter_list|()
block|{
return|return
name|liveInstances
return|;
block|}
specifier|public
name|Integer
name|getLaunchingInstances
parameter_list|()
block|{
return|return
name|launchingInstances
return|;
block|}
specifier|public
name|Long
name|getAppStartTime
parameter_list|()
block|{
return|return
name|appStartTime
return|;
block|}
specifier|public
name|Long
name|getAppFinishTime
parameter_list|()
block|{
return|return
name|appFinishTime
return|;
block|}
specifier|public
name|boolean
name|isRunningThresholdAchieved
parameter_list|()
block|{
return|return
name|runningThresholdAchieved
return|;
block|}
specifier|public
name|List
argument_list|<
name|LlapInstance
argument_list|>
name|getRunningInstances
parameter_list|()
block|{
return|return
name|runningInstances
return|;
block|}
specifier|public
name|List
argument_list|<
name|LlapInstance
argument_list|>
name|getCompletedInstances
parameter_list|()
block|{
return|return
name|completedInstances
return|;
block|}
annotation|@
name|JsonIgnore
specifier|public
name|AmInfo
name|maybeCreateAndGetAmInfo
parameter_list|()
block|{
if|if
condition|(
name|amInfo
operator|==
literal|null
condition|)
block|{
name|amInfo
operator|=
operator|new
name|AmInfo
argument_list|()
expr_stmt|;
block|}
return|return
name|amInfo
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"AppStatusBuilder{"
operator|+
literal|"amInfo="
operator|+
name|amInfo
operator|+
literal|", state="
operator|+
name|state
operator|+
literal|", diagnostics="
operator|+
name|diagnostics
operator|+
literal|", originalConfigurationPath='"
operator|+
name|originalConfigurationPath
operator|+
literal|'\''
operator|+
literal|", generatedConfigurationPath='"
operator|+
name|generatedConfigurationPath
operator|+
literal|'\''
operator|+
literal|", desiredInstances="
operator|+
name|desiredInstances
operator|+
literal|", liveInstances="
operator|+
name|liveInstances
operator|+
literal|", launchingInstances="
operator|+
name|launchingInstances
operator|+
literal|", appStartTime="
operator|+
name|appStartTime
operator|+
literal|", appFinishTime="
operator|+
name|appFinishTime
operator|+
literal|", runningThresholdAchieved="
operator|+
name|runningThresholdAchieved
operator|+
literal|", runningInstances="
operator|+
name|runningInstances
operator|+
literal|", completedInstances="
operator|+
name|completedInstances
operator|+
literal|", containerToRunningInstanceMap="
operator|+
name|containerToRunningInstanceMap
operator|+
literal|'}'
return|;
block|}
block|}
block|}
end_class

end_unit

