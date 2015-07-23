begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  *  you may not use this file except in compliance with the License.  *  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|app
operator|.
name|rm
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|ApplicationAttemptId
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
name|Container
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
name|ContainerId
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
name|NodeId
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
name|Priority
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
name|Resource
import|;
end_import

begin_class
class|class
name|ContainerFactory
block|{
specifier|final
name|ApplicationAttemptId
name|customAppAttemptId
decl_stmt|;
name|AtomicLong
name|nextId
decl_stmt|;
specifier|public
name|ContainerFactory
parameter_list|(
name|ApplicationAttemptId
name|appAttemptId
parameter_list|,
name|long
name|appIdLong
parameter_list|)
block|{
name|this
operator|.
name|nextId
operator|=
operator|new
name|AtomicLong
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|ApplicationId
name|appId
init|=
name|ApplicationId
operator|.
name|newInstance
argument_list|(
name|appIdLong
argument_list|,
name|appAttemptId
operator|.
name|getApplicationId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|customAppAttemptId
operator|=
name|ApplicationAttemptId
operator|.
name|newInstance
argument_list|(
name|appId
argument_list|,
name|appAttemptId
operator|.
name|getAttemptId
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Container
name|createContainer
parameter_list|(
name|Resource
name|capability
parameter_list|,
name|Priority
name|priority
parameter_list|,
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
block|{
name|ContainerId
name|containerId
init|=
name|ContainerId
operator|.
name|newContainerId
argument_list|(
name|customAppAttemptId
argument_list|,
name|nextId
operator|.
name|getAndIncrement
argument_list|()
argument_list|)
decl_stmt|;
name|NodeId
name|nodeId
init|=
name|NodeId
operator|.
name|newInstance
argument_list|(
name|hostname
argument_list|,
name|port
argument_list|)
decl_stmt|;
name|String
name|nodeHttpAddress
init|=
literal|"hostname:0"
decl_stmt|;
comment|// TODO: include UI ports
name|Container
name|container
init|=
name|Container
operator|.
name|newInstance
argument_list|(
name|containerId
argument_list|,
name|nodeId
argument_list|,
name|nodeHttpAddress
argument_list|,
name|capability
argument_list|,
name|priority
argument_list|,
literal|null
argument_list|)
decl_stmt|;
return|return
name|container
return|;
block|}
block|}
end_class

end_unit

