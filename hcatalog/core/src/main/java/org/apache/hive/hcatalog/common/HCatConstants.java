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
name|common
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
name|mapred
operator|.
name|SequenceFileInputFormat
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
name|SequenceFileOutputFormat
import|;
end_import

begin_class
specifier|public
specifier|final
class|class
name|HCatConstants
block|{
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_RCFILE_IF_CLASS
init|=
literal|"org.apache.hadoop.hive.ql.io.RCFileInputFormat"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_RCFILE_OF_CLASS
init|=
literal|"org.apache.hadoop.hive.ql.io.RCFileOutputFormat"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SEQUENCEFILE_INPUT
init|=
name|SequenceFileInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SEQUENCEFILE_OUTPUT
init|=
name|SequenceFileOutputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_PIG_STORAGE_CLASS
init|=
literal|"org.apache.pig.builtin.PigStorage"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_PIG_LOADER
init|=
literal|"hcat.pig.loader"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_PIG_LOADER_LOCATION_SET
init|=
name|HCAT_PIG_LOADER
operator|+
literal|".location.set"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_PIG_LOADER_ARGS
init|=
literal|"hcat.pig.loader.args"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_PIG_STORER
init|=
literal|"hcat.pig.storer"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_PIG_STORER_ARGS
init|=
literal|"hcat.pig.storer.args"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_PIG_ARGS_DELIMIT
init|=
literal|"hcat.pig.args.delimiter"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_PIG_ARGS_DELIMIT_DEFAULT
init|=
literal|","
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_PIG_STORER_LOCATION_SET
init|=
name|HCAT_PIG_STORER
operator|+
literal|".location.set"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_PIG_INNER_TUPLE_NAME
init|=
literal|"hcat.pig.inner.tuple.name"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_PIG_INNER_TUPLE_NAME_DEFAULT
init|=
literal|"innertuple"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_PIG_INNER_FIELD_NAME
init|=
literal|"hcat.pig.inner.field.name"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_PIG_INNER_FIELD_NAME_DEFAULT
init|=
literal|"innerfield"
decl_stmt|;
comment|/**      * {@value} (default: null)      * When the property is set in the UDFContext of the org.apache.hive.hcatalog.pig.HCatStorer, HCatStorer writes      * to the location it specifies instead of the default HCatalog location format. An example can be found      * in org.apache.hive.hcatalog.pig.HCatStorerWrapper.      */
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_PIG_STORER_EXTERNAL_LOCATION
init|=
name|HCAT_PIG_STORER
operator|+
literal|".external.location"
decl_stmt|;
comment|//The keys used to store info into the job Configuration
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_KEY_BASE
init|=
literal|"mapreduce.lib.hcat"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_KEY_OUTPUT_SCHEMA
init|=
name|HCAT_KEY_BASE
operator|+
literal|".output.schema"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_KEY_JOB_INFO
init|=
name|HCAT_KEY_BASE
operator|+
literal|".job.info"
decl_stmt|;
comment|// hcatalog specific configurations, that can be put in hive-site.xml
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_HIVE_CLIENT_EXPIRY_TIME
init|=
literal|"hcatalog.hive.client.cache.expiry.time"
decl_stmt|;
specifier|private
name|HCatConstants
parameter_list|()
block|{
comment|// restrict instantiation
block|}
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_TABLE_SCHEMA
init|=
literal|"hcat.table.schema"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_METASTORE_URI
init|=
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
operator|.
name|varname
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_PERMS
init|=
literal|"hcat.perms"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_GROUP
init|=
literal|"hcat.group"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_CREATE_TBL_NAME
init|=
literal|"hcat.create.tbl.name"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_CREATE_DB_NAME
init|=
literal|"hcat.create.db.name"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_METASTORE_PRINCIPAL
init|=
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_KERBEROS_PRINCIPAL
operator|.
name|varname
decl_stmt|;
comment|/**      * The desired number of input splits produced for each partition. When the      * input files are large and few, we want to split them into many splits,      * so as to increase the parallelizm of loading the splits. Try also two      * other parameters, mapred.min.split.size and mapred.max.split.size, to      * control the number of input splits.      */
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_DESIRED_PARTITION_NUM_SPLITS
init|=
literal|"hcat.desired.partition.num.splits"
decl_stmt|;
comment|// IMPORTANT IMPORTANT IMPORTANT!!!!!
comment|//The keys used to store info into the job Configuration.
comment|//If any new keys are added, the HCatStorer needs to be updated. The HCatStorer
comment|//updates the job configuration in the backend to insert these keys to avoid
comment|//having to call setOutput from the backend (which would cause a metastore call
comment|//from the map jobs)
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_KEY_OUTPUT_BASE
init|=
literal|"mapreduce.lib.hcatoutput"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_KEY_OUTPUT_INFO
init|=
name|HCAT_KEY_OUTPUT_BASE
operator|+
literal|".info"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_KEY_HIVE_CONF
init|=
name|HCAT_KEY_OUTPUT_BASE
operator|+
literal|".hive.conf"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_KEY_TOKEN_SIGNATURE
init|=
name|HCAT_KEY_OUTPUT_BASE
operator|+
literal|".token.sig"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
index|[]
name|OUTPUT_CONFS_TO_SAVE
init|=
block|{
name|HCAT_KEY_OUTPUT_INFO
block|,
name|HCAT_KEY_HIVE_CONF
block|,
name|HCAT_KEY_TOKEN_SIGNATURE
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_MSG_CLEAN_FREQ
init|=
literal|"hcat.msg.clean.freq"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_MSG_EXPIRY_DURATION
init|=
literal|"hcat.msg.expiry.duration"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_MSGBUS_TOPIC_NAME
init|=
literal|"hcat.msgbus.topic.name"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_MSGBUS_TOPIC_NAMING_POLICY
init|=
literal|"hcat.msgbus.topic.naming.policy"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_MSGBUS_TOPIC_PREFIX
init|=
literal|"hcat.msgbus.topic.prefix"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_DYNAMIC_PTN_JOBID
init|=
name|HCAT_KEY_OUTPUT_BASE
operator|+
literal|"dynamic.jobid"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|HCAT_IS_DYNAMIC_MAX_PTN_CHECK_ENABLED
init|=
literal|false
decl_stmt|;
comment|// Message Bus related properties.
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_DEFAULT_TOPIC_PREFIX
init|=
literal|"hcat"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_EVENT
init|=
literal|"HCAT_EVENT"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_ADD_PARTITION_EVENT
init|=
literal|"ADD_PARTITION"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_DROP_PARTITION_EVENT
init|=
literal|"DROP_PARTITION"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_PARTITION_DONE_EVENT
init|=
literal|"PARTITION_DONE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_CREATE_TABLE_EVENT
init|=
literal|"CREATE_TABLE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_DROP_TABLE_EVENT
init|=
literal|"DROP_TABLE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_CREATE_DATABASE_EVENT
init|=
literal|"CREATE_DATABASE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_DROP_DATABASE_EVENT
init|=
literal|"DROP_DATABASE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_MESSAGE_VERSION
init|=
literal|"HCAT_MESSAGE_VERSION"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_MESSAGE_FORMAT
init|=
literal|"HCAT_MESSAGE_FORMAT"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CONF_LABEL_HCAT_MESSAGE_FACTORY_IMPL_PREFIX
init|=
literal|"hcatalog.message.factory.impl."
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CONF_LABEL_HCAT_MESSAGE_FORMAT
init|=
literal|"hcatalog.message.format"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_MESSAGE_FACTORY_IMPL
init|=
literal|"org.apache.hive.hcatalog.messaging.json.JSONMessageFactory"
decl_stmt|;
comment|// System environment variables
specifier|public
specifier|static
specifier|final
name|String
name|SYSENV_HADOOP_TOKEN_FILE_LOCATION
init|=
literal|"HADOOP_TOKEN_FILE_LOCATION"
decl_stmt|;
comment|// Hadoop Conf Var Names
specifier|public
specifier|static
specifier|final
name|String
name|CONF_MAPREDUCE_JOB_CREDENTIALS_BINARY
init|=
literal|"mapreduce.job.credentials.binary"
decl_stmt|;
comment|//***************************************************************************
comment|// Data-related configuration properties.
comment|//***************************************************************************
comment|/**      * {@value} (default: {@value #HCAT_DATA_CONVERT_BOOLEAN_TO_INTEGER_DEFAULT}).      * Pig< 0.10.0 does not have boolean support, and scripts written for pre-boolean Pig versions      * will not expect boolean values when upgrading Pig. For integration the option is offered to      * convert boolean fields to integers by setting this Hadoop configuration key.      */
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_DATA_CONVERT_BOOLEAN_TO_INTEGER
init|=
literal|"hcat.data.convert.boolean.to.integer"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|HCAT_DATA_CONVERT_BOOLEAN_TO_INTEGER_DEFAULT
init|=
literal|false
decl_stmt|;
comment|/**      * {@value} (default: {@value #HCAT_DATA_TINY_SMALL_INT_PROMOTION_DEFAULT}).      * Hive tables support tinyint and smallint columns, while not all processing frameworks support      * these types (Pig only has integer for example). Enable this property to promote tinyint and      * smallint columns to integer at runtime. Note that writes to tinyint and smallint columns      * enforce bounds checking and jobs will fail if attempting to write values outside the column      * bounds.      */
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_DATA_TINY_SMALL_INT_PROMOTION
init|=
literal|"hcat.data.tiny.small.int.promotion"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|boolean
name|HCAT_DATA_TINY_SMALL_INT_PROMOTION_DEFAULT
init|=
literal|false
decl_stmt|;
comment|/**      * {@value} (default: {@value #HCAT_INPUT_BAD_RECORD_THRESHOLD_DEFAULT}).      * Threshold for the ratio of bad records that will be silently skipped without causing a task      * failure. This is useful when processing large data sets with corrupt records, when its      * acceptable to skip some bad records.      */
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_INPUT_BAD_RECORD_THRESHOLD_KEY
init|=
literal|"hcat.input.bad.record.threshold"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|float
name|HCAT_INPUT_BAD_RECORD_THRESHOLD_DEFAULT
init|=
literal|0.0001f
decl_stmt|;
comment|/**      * {@value} (default: {@value #HCAT_INPUT_BAD_RECORD_MIN_DEFAULT}).      * Number of bad records that will be accepted before applying      * {@value #HCAT_INPUT_BAD_RECORD_THRESHOLD_KEY}. This is necessary to prevent an initial bad      * record from causing a task failure.      */
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_INPUT_BAD_RECORD_MIN_KEY
init|=
literal|"hcat.input.bad.record.min"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|HCAT_INPUT_BAD_RECORD_MIN_DEFAULT
init|=
literal|2
decl_stmt|;
block|}
end_class

end_unit

