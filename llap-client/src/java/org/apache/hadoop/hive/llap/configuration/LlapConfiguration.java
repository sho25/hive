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
name|configuration
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
name|LlapConfiguration
extends|extends
name|Configuration
block|{
specifier|public
name|LlapConfiguration
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
name|LlapConfiguration
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
name|LLAP_PREFIX
init|=
literal|"llap."
decl_stmt|;
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
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_SHUFFLE_DIR_WATCHER_ENABLED
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"shuffle.dir-watcher.enabled"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|LLAP_DAEMON_SHUFFLE_DIR_WATCHER_ENABLED_DEFAULT
init|=
literal|false
decl_stmt|;
comment|// This needs to be kept below the task timeout interval, but otherwise as high as possible to avoid unnecessary traffic.
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_LIVENESS_HEARTBEAT_INTERVAL_MS
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"liveness.heartbeat.interval-ms"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|LLAP_DAEMON_LIVENESS_HEARTBEAT_INTERVAL_MS_DEFAULT
init|=
literal|10000l
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
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_NUM_FILE_CLEANER_THREADS
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"num.file.cleaner.threads"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|LLAP_DAEMON_NUM_FILE_CLEANER_THREADS_DEFAULT
init|=
literal|1
decl_stmt|;
comment|// Section for configs used in the AM //
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_FILE_CLEANUP_DELAY_SECONDS
init|=
name|LLAP_PREFIX
operator|+
literal|"file.cleanup.delay-seconds"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|LLAP_FILE_CLEANUP_DELAY_SECONDS_DEFAULT
init|=
literal|300
decl_stmt|;
comment|// 5 minutes by default
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
name|LLAP_DAEMON_SERVICE_REFRESH_INTERVAL
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"service.refresh.interval"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|LLAP_DAEMON_SERVICE_REFRESH_INTERVAL_DEFAULT
init|=
literal|60
decl_stmt|;
comment|// seconds
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
literal|10
decl_stmt|;
comment|/**    * Minimum time after which a previously disabled node will be re-enabled for scheduling. This may    * be modified by an exponential back-off if failures persist    */
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_TASK_SCHEDULER_NODE_REENABLE_MIN_TIMEOUT_MILLIS
init|=
name|LLAP_PREFIX
operator|+
literal|"task.scheduler.node.re-enable.min.timeout.ms"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|LLAP_TASK_SCHEDULER_NODE_REENABLE_MIN_TIMEOUT_MILLIS_DEFAULT
init|=
literal|200l
decl_stmt|;
comment|/**    * Maximum time after which a previously disabled node will be re-enabled for scheduling. This may    * be modified by an exponential back-off if failures persist    */
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_TASK_SCHEDULER_NODE_REENABLE_MAX_TIMEOUT_MILLIS
init|=
name|LLAP_PREFIX
operator|+
literal|"task.scheduler.node.re-enable.max.timeout.ms"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|LLAP_TASK_SCHEDULER_NODE_REENABLE_MAX_TIMEOUT_MILLIS_DEFAULT
init|=
literal|10000l
decl_stmt|;
comment|/**    * Backoff factor on successive blacklists of a node. Blacklists timeouts start at the min timeout    * and go up to the max timeout based on this backoff factor    */
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_TASK_SCHEDULER_NODE_DISABLE_BACK_OFF_FACTOR
init|=
name|LLAP_PREFIX
operator|+
literal|"task.scheduler.node.disable.backoff.factor"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|float
name|LLAP_TASK_SCHEDULER_NODE_DISABLE_BACK_OFF_FACTOR_DEFAULT
init|=
literal|1.5f
decl_stmt|;
comment|/**    * The number of tasks the AM TaskScheduler will try allocating per node.    * 0 indicates that this should be picked up from the Registry.    * -1 indicates unlimited capacity    *>0 indicates a specific bound    */
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_TASK_SCHEDULER_NUM_SCHEDULABLE_TASKS_PER_NODE
init|=
name|LLAP_PREFIX
operator|+
literal|"task.scheduler.num.schedulable.tasks.per.node"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|LLAP_TASK_SCHEDULER_NUM_SCHEDULABLE_TASKS_PER_NODE_DEFAULT
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_TASK_SCHEDULER_WAIT_QUEUE_SIZE
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"task.scheduler.wait.queue.size"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|LLAP_DAEMON_TASK_SCHEDULER_WAIT_QUEUE_SIZE_DEFAULT
init|=
literal|10
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_TASK_SCHEDULER_ENABLE_PREEMPTION
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"task.scheduler.enable.preemption"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|LLAP_DAEMON_TASK_SCHEDULER_ENABLE_PREEMPTION_DEFAULT
init|=
literal|true
decl_stmt|;
comment|/** Amount of time to wait on a connection failure to an LLAP daemon */
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_TASK_COMMUNICATOR_CONNECTION_TIMEOUT_MILLIS
init|=
name|LLAP_PREFIX
operator|+
literal|"task.communicator.connection.timeout-millis"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|LLAP_TASK_COMMUNICATOR_CONNECTION_TIMEOUT_MILLIS_DEFAULT
init|=
literal|16000
decl_stmt|;
comment|/** Sleep duration while waiting for a connection failure */
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_TASK_COMMUNICATOR_CONNECTION_SLEEP_BETWEEN_RETRIES_MILLIS
init|=
name|LLAP_PREFIX
operator|+
literal|"task.communicator.connection.sleep-between-retries-millis"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|LLAP_TASK_COMMUNICATOR_CONNECTION_SLEEP_BETWEEN_RETRIES_MILLIS_DEFAULT
init|=
literal|2000l
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_SERVICE_PORT
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"service.port"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|LLAP_DAEMON_SERVICE_PORT_DEFAULT
init|=
literal|15002
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_DAEMON_SERVICE_SSL
init|=
name|LLAP_DAEMON_PREFIX
operator|+
literal|"service.ssl"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|LLAP_DAEMON_SERVICE_SSL_DEFAULT
init|=
literal|false
decl_stmt|;
block|}
end_class

end_unit

