begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|daemon
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

begin_class
specifier|public
class|class
name|LlapDaemonConfiguration
extends|extends
name|Configuration
block|{
specifier|public
name|LlapDaemonConfiguration
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|addResource
argument_list|(
name|LLAP_DAEMON_SITE
argument_list|)
expr_stmt|;
block|}
specifier|public
name|LlapDaemonConfiguration
parameter_list|()
block|{
name|super
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|addResource
argument_list|(
name|LLAP_DAEMON_SITE
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_PREFIX
init|=
literal|"llap.daemon."
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_SITE
init|=
literal|"llap-daemon-site.xml"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_RPC_NUM_HANDLERS
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"rpc.num.handlers"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|LLAP_DAEMON_RPC_NUM_HANDLERS_DEFAULT
init|=
literal|5
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_WORK_DIRS
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"work.dirs"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_YARN_SHUFFLE_PORT
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"yarn.shuffle.port"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|LLAP_DAEMON_YARN_SHUFFLE_PORT_DEFAULT
init|=
literal|15551
decl_stmt|;
comment|// Section for configs used in AM and executors
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_NUM_EXECUTORS
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"num.executors"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|LLAP_DAEMON_NUM_EXECUTORS_DEFAULT
init|=
literal|4
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_RPC_PORT
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"rpc.port"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|LLAP_DAEMON_RPC_PORT_DEFAULT
init|=
literal|15001
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_MEMORY_PER_INSTANCE_MB
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"memory.per.instance.mb"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|LLAP_DAEMON_MEMORY_PER_INSTANCE_MB_DEFAULT
init|=
literal|4096
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_VCPUS_PER_INSTANCE
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"vcpus.per.instance"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|LLAP_DAEMON_VCPUS_PER_INSTANCE_DEFAULT
init|=
literal|4
decl_stmt|;
comment|// Section for configs used in the AM //
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_SERVICE_HOSTS
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"service.hosts"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_COMMUNICATOR_NUM_THREADS
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"communicator.num.threads"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|LLAP_DAEMON_COMMUNICATOR_NUM_THREADS_DEFAULT
init|=
literal|5
decl_stmt|;
comment|/**    * Time after which a previously disabled node will be re-enabled for scheduling. This may be    * modified by an exponential back-off if failures persist    */
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_TASK_SCHEDULER_NODE_REENABLE_TIMEOUT_MILLIS
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"task.scheduler.node.re-enable.timeout.ms"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|LLAP_DAEMON_TASK_SCHEDULER_NODE_REENABLE_TIMEOUT_MILLIS_DEFAULT
init|=
literal|2000l
decl_stmt|;
block|}
end_class

end_unit

