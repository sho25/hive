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
name|DRUID_QUERY_GRANULARITY
init|=
literal|"druid.query.granularity"
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
name|DRUID_QUERY_JSON
init|=
literal|"druid.query.json"
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
block|}
end_class

end_unit

