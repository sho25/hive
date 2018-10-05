begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|conf
package|;
end_package

begin_class
specifier|public
class|class
name|Constants
block|{
comment|/* Constants for LLAP */
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_LOGGER_NAME_QUERY_ROUTING
init|=
literal|"query-routing"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_LOGGER_NAME_CONSOLE
init|=
literal|"console"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_LOGGER_NAME_RFA
init|=
literal|"RFA"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_NUM_BUCKETS
init|=
literal|"llap.num.buckets"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LLAP_BUCKET_ID
init|=
literal|"llap.bucket.id"
decl_stmt|;
comment|/* Constants for Druid storage handler */
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_HIVE_STORAGE_HANDLER_ID
init|=
literal|"org.apache.hadoop.hive.druid.DruidStorageHandler"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_HIVE_OUTPUT_FORMAT
init|=
literal|"org.apache.hadoop.hive.druid.io.DruidOutputFormat"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_DATA_SOURCE
init|=
literal|"druid.datasource"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_SEGMENT_GRANULARITY
init|=
literal|"druid.segment.granularity"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_ROLLUP
init|=
literal|"druid.rollup"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_QUERY_GRANULARITY
init|=
literal|"druid.query.granularity"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_TARGET_SHARDS_PER_GRANULARITY
init|=
literal|"druid.segment.targetShardsPerGranularity"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_TIMESTAMP_GRANULARITY_COL_NAME
init|=
literal|"__time_granularity"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_SHARD_KEY_COL_NAME
init|=
literal|"__druid_extra_partition_key"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_QUERY_JSON
init|=
literal|"druid.query.json"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_QUERY_FIELD_NAMES
init|=
literal|"druid.fieldNames"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_QUERY_FIELD_TYPES
init|=
literal|"druid.fieldTypes"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_QUERY_TYPE
init|=
literal|"druid.query.type"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_QUERY_FETCH
init|=
literal|"druid.query.fetch"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_SEGMENT_DIRECTORY
init|=
literal|"druid.storage.storageDirectory"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_SEGMENT_INTERMEDIATE_DIRECTORY
init|=
literal|"druid.storage.storageDirectory.intermediate"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_SEGMENT_VERSION
init|=
literal|"druid.segment.version"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_JOB_WORKING_DIRECTORY
init|=
literal|"druid.job.workingDirectory"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|KAFKA_TOPIC
init|=
literal|"kafka.topic"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|KAFKA_BOOTSTRAP_SERVERS
init|=
literal|"kafka.bootstrap.servers"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_KAFKA_INGESTION_PROPERTY_PREFIX
init|=
literal|"druid.kafka.ingestion."
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_KAFKA_CONSUMER_PROPERTY_PREFIX
init|=
name|DRUID_KAFKA_INGESTION_PROPERTY_PREFIX
operator|+
literal|"consumer."
decl_stmt|;
comment|/* Kafka Ingestion state - valid values - START/STOP/RESET */
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_KAFKA_INGESTION
init|=
literal|"druid.kafka.ingestion"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JDBC_HIVE_STORAGE_HANDLER_ID
init|=
literal|"org.apache.hive.storage.jdbc.JdbcStorageHandler"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JDBC_CONFIG_PREFIX
init|=
literal|"hive.sql"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JDBC_TABLE
init|=
name|JDBC_CONFIG_PREFIX
operator|+
literal|".table"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JDBC_DATABASE_TYPE
init|=
name|JDBC_CONFIG_PREFIX
operator|+
literal|".database.type"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JDBC_URL
init|=
name|JDBC_CONFIG_PREFIX
operator|+
literal|".jdbc.url"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JDBC_DRIVER
init|=
name|JDBC_CONFIG_PREFIX
operator|+
literal|".jdbc.driver"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JDBC_USERNAME
init|=
name|JDBC_CONFIG_PREFIX
operator|+
literal|".dbcp.username"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JDBC_PASSWORD
init|=
name|JDBC_CONFIG_PREFIX
operator|+
literal|".dbcp.password"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JDBC_KEYSTORE
init|=
name|JDBC_CONFIG_PREFIX
operator|+
literal|".dbcp.password.keystore"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JDBC_KEY
init|=
name|JDBC_CONFIG_PREFIX
operator|+
literal|".dbcp.password.key"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JDBC_QUERY
init|=
name|JDBC_CONFIG_PREFIX
operator|+
literal|".query"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JDBC_QUERY_FIELD_NAMES
init|=
name|JDBC_CONFIG_PREFIX
operator|+
literal|".query.fieldNames"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JDBC_QUERY_FIELD_TYPES
init|=
name|JDBC_CONFIG_PREFIX
operator|+
literal|".query.fieldTypes"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|JDBC_SPLIT_QUERY
init|=
name|JDBC_CONFIG_PREFIX
operator|+
literal|".query.split"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_SERVER2_JOB_CREDSTORE_PASSWORD_ENVVAR
init|=
literal|"HIVE_JOB_CREDSTORE_PASSWORD"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_CREDENTIAL_PASSWORD_ENVVAR
init|=
literal|"HADOOP_CREDSTORE_PASSWORD"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_CREDENTIAL_PROVIDER_PATH_CONFIG
init|=
literal|"hadoop.security.credential.provider.path"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MATERIALIZED_VIEW_REWRITING_TIME_WINDOW
init|=
literal|"rewriting.time.window"
decl_stmt|;
block|}
end_class

end_unit

