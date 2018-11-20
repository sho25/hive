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
name|druid
operator|.
name|conf
package|;
end_package

begin_comment
comment|/**  * Utility class for Druid Constants.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|DruidConstants
block|{
specifier|private
name|DruidConstants
parameter_list|()
block|{   }
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
comment|//Druid storage timestamp column name
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_TIMESTAMP_COLUMN
init|=
literal|"__time"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_TIMESTAMP_FORMAT
init|=
literal|"druid.timestamp.format"
decl_stmt|;
comment|// Used when the field name in ingested data via streaming ingestion does not match
comment|// druid default timestamp column i.e `__time`
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_TIMESTAMP_COLUMN
init|=
literal|"druid.timestamp.column"
decl_stmt|;
comment|//Druid Json timestamp column name for GroupBy results
specifier|public
specifier|static
specifier|final
name|String
name|EVENT_TIMESTAMP_COLUMN
init|=
literal|"timestamp"
decl_stmt|;
comment|// Druid ParseSpec Type - JSON/CSV/TSV/AVRO
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_PARSE_SPEC_FORMAT
init|=
literal|"druid.parseSpec.format"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|AVRO_SCHEMA_LITERAL
init|=
literal|"avro.schema.literal"
decl_stmt|;
comment|// value delimiter for druid columns
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_PARSE_SPEC_DELIMITER
init|=
literal|"druid.parseSpec.delimiter"
decl_stmt|;
comment|// list demiliter for multi-valued columns
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_PARSE_SPEC_LIST_DELIMITER
init|=
literal|"druid.parseSpec.listDelimiter"
decl_stmt|;
comment|// order of columns for delimiter and csv parse specs.
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_PARSE_SPEC_COLUMNS
init|=
literal|"druid.parseSpec.columns"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_PARSE_SPEC_SKIP_HEADER_ROWS
init|=
literal|"druid.parseSpec.skipHeaderRows"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DRUID_PARSE_SPEC_HAS_HEADER_ROWS
init|=
literal|"druid.parseSpec.hasHeaderRows"
decl_stmt|;
block|}
end_class

end_unit

