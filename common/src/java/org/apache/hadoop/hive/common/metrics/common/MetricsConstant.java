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
name|common
operator|.
name|metrics
operator|.
name|common
package|;
end_package

begin_comment
comment|/**  * This class defines some metrics generated by Hive processes.  */
end_comment

begin_class
specifier|public
class|class
name|MetricsConstant
block|{
specifier|public
specifier|static
specifier|final
name|String
name|JVM_PAUSE_INFO
init|=
literal|"jvm.pause.info-threshold"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JVM_PAUSE_WARN
init|=
literal|"jvm.pause.warn-threshold"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JVM_EXTRA_SLEEP
init|=
literal|"jvm.pause.extraSleepTime"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OPEN_CONNECTIONS
init|=
literal|"open_connections"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OPEN_OPERATIONS
init|=
literal|"open_operations"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CUMULATIVE_CONNECTION_COUNT
init|=
literal|"cumulative_connection_count"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|METASTORE_HIVE_LOCKS
init|=
literal|"metastore_hive_locks"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ZOOKEEPER_HIVE_SHAREDLOCKS
init|=
literal|"zookeeper_hive_sharedlocks"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ZOOKEEPER_HIVE_EXCLUSIVELOCKS
init|=
literal|"zookeeper_hive_exclusivelocks"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ZOOKEEPER_HIVE_SEMISHAREDLOCKS
init|=
literal|"zookeeper_hive_semisharedlocks"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXEC_ASYNC_QUEUE_SIZE
init|=
literal|"exec_async_queue_size"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXEC_ASYNC_POOL_SIZE
init|=
literal|"exec_async_pool_size"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OPERATION_PREFIX
init|=
literal|"hs2_operation_"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|COMPLETED_OPERATION_PREFIX
init|=
literal|"hs2_completed_operation_"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INIT_TOTAL_DATABASES
init|=
literal|"init_total_count_dbs"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INIT_TOTAL_TABLES
init|=
literal|"init_total_count_tables"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INIT_TOTAL_PARTITIONS
init|=
literal|"init_total_count_partitions"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CREATE_TOTAL_DATABASES
init|=
literal|"create_total_count_dbs"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CREATE_TOTAL_TABLES
init|=
literal|"create_total_count_tables"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CREATE_TOTAL_PARTITIONS
init|=
literal|"create_total_count_partitions"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DELETE_TOTAL_DATABASES
init|=
literal|"delete_total_count_dbs"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DELETE_TOTAL_TABLES
init|=
literal|"delete_total_count_tables"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DELETE_TOTAL_PARTITIONS
init|=
literal|"delete_total_count_partitions"
decl_stmt|;
block|}
end_class

end_unit

