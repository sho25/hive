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
name|templeton
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|Configuration
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
name|fs
operator|.
name|Path
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
name|util
operator|.
name|StringUtils
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
name|util
operator|.
name|VersionInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
operator|.
name|JobState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
operator|.
name|TempletonUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
operator|.
name|ZooKeeperCleanup
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|tool
operator|.
name|ZooKeeperStorage
import|;
end_import

begin_comment
comment|/**  * The configuration for Templeton.  This merges the normal Hadoop  * configuration with the Templeton specific variables.  *  * The Templeton configuration variables are described in  * templeton-default.xml  *  * The Templeton specific configuration is split into two layers  *  * 1. webhcat-default.xml - All the configuration variables that  *    Templeton needs.  These are the defaults that ship with the app  *    and should only be changed be the app developers.  *  * 2. webhcat-site.xml - The (possibly empty) configuration that the  *    system administrator can set variables for their Hadoop cluster.  *  * The configuration files are loaded in this order with later files  * overriding earlier ones.  *  * To find the configuration files, we first attempt to load a file  * from the CLASSPATH and then look in the directory specified in the  * TEMPLETON_HOME environment variable.  *  * In addition the configuration files may access the special env  * variable env for all environment variables.  For example, the  * hadoop executable could be specified using:  *<pre>  *      ${env.HADOOP_PREFIX}/bin/hadoop  *</pre>  */
end_comment

begin_class
specifier|public
class|class
name|AppConfig
extends|extends
name|Configuration
block|{
specifier|public
specifier|static
specifier|final
name|String
index|[]
name|HADOOP_CONF_FILENAMES
init|=
block|{
literal|"core-default.xml"
block|,
literal|"core-site.xml"
block|,
literal|"mapred-default.xml"
block|,
literal|"mapred-site.xml"
block|,
literal|"hdfs-site.xml"
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
index|[]
name|HADOOP_PREFIX_VARS
init|=
block|{
literal|"HADOOP_PREFIX"
block|,
literal|"HADOOP_HOME"
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEMPLETON_HOME_VAR
init|=
literal|"TEMPLETON_HOME"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|WEBHCAT_CONF_DIR
init|=
literal|"WEBHCAT_CONF_DIR"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
index|[]
name|TEMPLETON_CONF_FILENAMES
init|=
block|{
literal|"webhcat-default.xml"
block|,
literal|"webhcat-site.xml"
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PORT
init|=
literal|"templeton.port"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXEC_ENCODING_NAME
init|=
literal|"templeton.exec.encoding"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXEC_ENVS_NAME
init|=
literal|"templeton.exec.envs"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXEC_MAX_BYTES_NAME
init|=
literal|"templeton.exec.max-output-bytes"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXEC_MAX_PROCS_NAME
init|=
literal|"templeton.exec.max-procs"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXEC_TIMEOUT_NAME
init|=
literal|"templeton.exec.timeout"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_QUEUE_NAME
init|=
literal|"templeton.hadoop.queue.name"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_NAME
init|=
literal|"templeton.hadoop"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_CONF_DIR
init|=
literal|"templeton.hadoop.conf.dir"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_NAME
init|=
literal|"templeton.hcat"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PYTHON_NAME
init|=
literal|"templeton.python"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_ARCHIVE_NAME
init|=
literal|"templeton.hive.archive"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_PATH_NAME
init|=
literal|"templeton.hive.path"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MAPPER_MEMORY_MB
init|=
literal|"templeton.mapper.memory.mb"
decl_stmt|;
comment|/**    * see webhcat-default.xml    */
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_HOME_PATH
init|=
literal|"templeton.hive.home"
decl_stmt|;
comment|/**    * see webhcat-default.xml    */
specifier|public
specifier|static
specifier|final
name|String
name|HCAT_HOME_PATH
init|=
literal|"templeton.hcat.home"
decl_stmt|;
comment|/**    * is a comma separated list of name=value pairs;    * In case some value is itself a comma-separated list, the comma needs to    * be escaped with {@link org.apache.hadoop.util.StringUtils#ESCAPE_CHAR}.  See other usage    * of escape/unescape methods in {@link org.apache.hadoop.util.StringUtils} in webhcat.    */
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_PROPS_NAME
init|=
literal|"templeton.hive.properties"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SQOOP_ARCHIVE_NAME
init|=
literal|"templeton.sqoop.archive"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SQOOP_PATH_NAME
init|=
literal|"templeton.sqoop.path"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SQOOP_HOME_PATH
init|=
literal|"templeton.sqoop.home"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LIB_JARS_NAME
init|=
literal|"templeton.libjars"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PIG_ARCHIVE_NAME
init|=
literal|"templeton.pig.archive"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PIG_PATH_NAME
init|=
literal|"templeton.pig.path"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STREAMING_JAR_NAME
init|=
literal|"templeton.streaming.jar"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OVERRIDE_JARS_NAME
init|=
literal|"templeton.override.jars"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|OVERRIDE_JARS_ENABLED
init|=
literal|"templeton.override.enabled"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEMPLETON_CONTROLLER_MR_CHILD_OPTS
init|=
literal|"templeton.controller.mr.child.opts"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|KERBEROS_SECRET
init|=
literal|"templeton.kerberos.secret"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|KERBEROS_PRINCIPAL
init|=
literal|"templeton.kerberos.principal"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|KERBEROS_KEYTAB
init|=
literal|"templeton.kerberos.keytab"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CALLBACK_INTERVAL_NAME
init|=
literal|"templeton.callback.retry.interval"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CALLBACK_RETRY_NAME
init|=
literal|"templeton.callback.retry.attempts"
decl_stmt|;
comment|//Hadoop property names (set by templeton logic)
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_END_INTERVAL_NAME
init|=
literal|"job.end.retry.interval"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_END_RETRY_NAME
init|=
literal|"job.end.retry.attempts"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_END_URL_NAME
init|=
literal|"job.end.notification.url"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_SPECULATIVE_NAME
init|=
literal|"mapred.map.tasks.speculative.execution"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_CHILD_JAVA_OPTS
init|=
literal|"mapred.child.java.opts"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HADOOP_MAP_MEMORY_MB
init|=
literal|"mapreduce.map.memory.mb"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|UNIT_TEST_MODE
init|=
literal|"templeton.unit.test.mode"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|AppConfig
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|AppConfig
parameter_list|()
block|{
name|init
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Using Hadoop version "
operator|+
name|VersionInfo
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|init
parameter_list|()
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
range|:
name|System
operator|.
name|getenv
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
name|set
argument_list|(
literal|"env."
operator|+
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|templetonDir
init|=
name|getTempletonDir
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|fname
range|:
name|TEMPLETON_CONF_FILENAMES
control|)
block|{
name|logConfigLoadAttempt
argument_list|(
name|templetonDir
operator|+
name|File
operator|.
name|separator
operator|+
name|fname
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|loadOneClasspathConfig
argument_list|(
name|fname
argument_list|)
condition|)
name|loadOneFileConfig
argument_list|(
name|templetonDir
argument_list|,
name|fname
argument_list|)
expr_stmt|;
block|}
name|String
name|hadoopConfDir
init|=
name|getHadoopConfDir
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|fname
range|:
name|HADOOP_CONF_FILENAMES
control|)
block|{
name|logConfigLoadAttempt
argument_list|(
name|hadoopConfDir
operator|+
name|File
operator|.
name|separator
operator|+
name|fname
argument_list|)
expr_stmt|;
name|loadOneFileConfig
argument_list|(
name|hadoopConfDir
argument_list|,
name|fname
argument_list|)
expr_stmt|;
block|}
name|ProxyUserSupport
operator|.
name|processProxyuserConfig
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|handleHiveProperties
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|dumpEnvironent
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * When auto-shipping hive tar (for example when hive query or pig script    * is submitted via webhcat), Hive client is launched on some remote node where Hive has not    * been installed.  We need pass some properties to that client to make sure it connects to the    * right Metastore, configures Tez, etc.  Here we look for such properties in hive config,    * and set a comma-separated list of key values in {@link #HIVE_PROPS_NAME}.    * Note that the user may choose to set the same keys in HIVE_PROPS_NAME directly, in which case    * those values should take precedence.    */
specifier|private
name|void
name|handleHiveProperties
parameter_list|()
block|{
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
comment|//load hive-site.xml from classpath
name|List
argument_list|<
name|String
argument_list|>
name|interestingPropNames
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"hive.metastore.uris"
argument_list|,
literal|"hive.metastore.sasl.enabled"
argument_list|,
literal|"hive.metastore.execute.setugi"
argument_list|,
literal|"hive.execution.engine"
argument_list|)
decl_stmt|;
comment|//each items is a "key=value" format
name|List
argument_list|<
name|String
argument_list|>
name|webhcatHiveProps
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|hiveProps
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|interestingPropName
range|:
name|interestingPropNames
control|)
block|{
name|String
name|value
init|=
name|hiveConf
operator|.
name|get
argument_list|(
name|interestingPropName
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|whProp
range|:
name|webhcatHiveProps
control|)
block|{
if|if
condition|(
name|whProp
operator|.
name|startsWith
argument_list|(
name|interestingPropName
operator|+
literal|"="
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|found
condition|)
block|{
name|webhcatHiveProps
operator|.
name|add
argument_list|(
name|interestingPropName
operator|+
literal|"="
operator|+
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|StringBuilder
name|hiveProps
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|whProp
range|:
name|webhcatHiveProps
control|)
block|{
comment|//make sure to escape separator char in prop values
name|hiveProps
operator|.
name|append
argument_list|(
name|hiveProps
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|?
literal|","
else|:
literal|""
argument_list|)
operator|.
name|append
argument_list|(
name|StringUtils
operator|.
name|escapeString
argument_list|(
name|whProp
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|set
argument_list|(
name|HIVE_PROPS_NAME
argument_list|,
name|hiveProps
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|logConfigLoadAttempt
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Attempting to load config file: "
operator|+
name|path
argument_list|)
expr_stmt|;
block|}
comment|/**    * Dumps all env and config state.  Should be called once on WebHCat start up to facilitate     * support/debugging.  Later it may be worth adding a REST call which will return this data.    */
specifier|private
name|String
name|dumpEnvironent
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"WebHCat environment:\n"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
init|=
name|System
operator|.
name|getenv
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|propKeys
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|env
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|propKeys
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|propKey
range|:
name|propKeys
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|propKey
argument_list|)
operator|.
name|append
argument_list|(
literal|'='
argument_list|)
operator|.
name|append
argument_list|(
name|env
operator|.
name|get
argument_list|(
name|propKey
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"Configration properties: \n"
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|configIter
init|=
name|this
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|configVals
init|=
operator|new
name|ArrayList
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|configIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|configVals
operator|.
name|add
argument_list|(
name|configIter
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|configVals
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ent
parameter_list|,
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ent2
parameter_list|)
block|{
return|return
name|ent
operator|.
name|getKey
argument_list|()
operator|.
name|compareTo
argument_list|(
name|ent2
operator|.
name|getKey
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|configVals
control|)
block|{
comment|//use get() to make sure variable substitution works
name|sb
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|'='
argument_list|)
operator|.
name|append
argument_list|(
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|'\n'
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|void
name|startCleanup
parameter_list|()
block|{
name|JobState
operator|.
name|getStorageInstance
argument_list|(
name|this
argument_list|)
operator|.
name|startCleanup
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getHadoopConfDir
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|HADOOP_CONF_DIR
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getTempletonDir
parameter_list|()
block|{
return|return
name|System
operator|.
name|getenv
argument_list|(
name|TEMPLETON_HOME_VAR
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getWebhcatConfDir
parameter_list|()
block|{
return|return
name|System
operator|.
name|getenv
argument_list|(
name|WEBHCAT_CONF_DIR
argument_list|)
return|;
block|}
specifier|private
name|boolean
name|loadOneFileConfig
parameter_list|(
name|String
name|dir
parameter_list|,
name|String
name|fname
parameter_list|)
block|{
if|if
condition|(
name|dir
operator|!=
literal|null
condition|)
block|{
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|dir
argument_list|,
name|fname
argument_list|)
decl_stmt|;
if|if
condition|(
name|f
operator|.
name|exists
argument_list|()
condition|)
block|{
name|addResource
argument_list|(
operator|new
name|Path
argument_list|(
name|f
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"loaded config file "
operator|+
name|f
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|loadOneClasspathConfig
parameter_list|(
name|String
name|fname
parameter_list|)
block|{
name|URL
name|x
init|=
name|getResource
argument_list|(
name|fname
argument_list|)
decl_stmt|;
if|if
condition|(
name|x
operator|!=
literal|null
condition|)
block|{
name|addResource
argument_list|(
name|x
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"loaded config from classpath "
operator|+
name|x
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|public
name|String
name|libJars
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|LIB_JARS_NAME
argument_list|)
return|;
block|}
specifier|public
name|String
name|hadoopQueueName
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|HADOOP_QUEUE_NAME
argument_list|)
return|;
block|}
specifier|public
name|String
name|clusterHadoop
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|HADOOP_NAME
argument_list|)
return|;
block|}
specifier|public
name|String
name|clusterHcat
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|HCAT_NAME
argument_list|)
return|;
block|}
specifier|public
name|String
name|clusterPython
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|PYTHON_NAME
argument_list|)
return|;
block|}
specifier|public
name|String
name|pigPath
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|PIG_PATH_NAME
argument_list|)
return|;
block|}
specifier|public
name|String
name|pigArchive
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|PIG_ARCHIVE_NAME
argument_list|)
return|;
block|}
specifier|public
name|String
name|hivePath
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|HIVE_PATH_NAME
argument_list|)
return|;
block|}
specifier|public
name|String
name|hiveArchive
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|HIVE_ARCHIVE_NAME
argument_list|)
return|;
block|}
specifier|public
name|String
name|sqoopPath
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|SQOOP_PATH_NAME
argument_list|)
return|;
block|}
specifier|public
name|String
name|sqoopArchive
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|SQOOP_ARCHIVE_NAME
argument_list|)
return|;
block|}
specifier|public
name|String
name|sqoopHome
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|SQOOP_HOME_PATH
argument_list|)
return|;
block|}
specifier|public
name|String
name|streamingJar
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|STREAMING_JAR_NAME
argument_list|)
return|;
block|}
specifier|public
name|String
name|kerberosSecret
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|KERBEROS_SECRET
argument_list|)
return|;
block|}
specifier|public
name|String
name|kerberosPrincipal
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|KERBEROS_PRINCIPAL
argument_list|)
return|;
block|}
specifier|public
name|String
name|kerberosKeytab
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|KERBEROS_KEYTAB
argument_list|)
return|;
block|}
specifier|public
name|String
name|controllerMRChildOpts
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|TEMPLETON_CONTROLLER_MR_CHILD_OPTS
argument_list|)
return|;
block|}
specifier|public
name|String
name|mapperMemoryMb
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|MAPPER_MEMORY_MB
argument_list|)
return|;
block|}
comment|/**    * @see  #HIVE_PROPS_NAME    */
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|hiveProps
parameter_list|()
block|{
name|String
index|[]
name|props
init|=
name|StringUtils
operator|.
name|split
argument_list|(
name|get
argument_list|(
name|HIVE_PROPS_NAME
argument_list|)
argument_list|)
decl_stmt|;
comment|//since raw data was (possibly) escaped to make split work,
comment|//now need to remove escape chars so they don't interfere with downstream processing
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|props
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|props
index|[
name|i
index|]
operator|=
name|TempletonUtils
operator|.
name|unEscapeString
argument_list|(
name|props
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|props
argument_list|)
return|;
block|}
specifier|public
name|String
index|[]
name|overrideJars
parameter_list|()
block|{
if|if
condition|(
name|getBoolean
argument_list|(
name|OVERRIDE_JARS_ENABLED
argument_list|,
literal|true
argument_list|)
condition|)
return|return
name|getStrings
argument_list|(
name|OVERRIDE_JARS_NAME
argument_list|)
return|;
else|else
return|return
literal|null
return|;
block|}
specifier|public
name|String
name|overrideJarsString
parameter_list|()
block|{
if|if
condition|(
name|getBoolean
argument_list|(
name|OVERRIDE_JARS_ENABLED
argument_list|,
literal|true
argument_list|)
condition|)
return|return
name|get
argument_list|(
name|OVERRIDE_JARS_NAME
argument_list|)
return|;
else|else
return|return
literal|null
return|;
block|}
specifier|public
name|long
name|zkCleanupInterval
parameter_list|()
block|{
return|return
name|getLong
argument_list|(
name|ZooKeeperCleanup
operator|.
name|ZK_CLEANUP_INTERVAL
argument_list|,
operator|(
literal|1000L
operator|*
literal|60L
operator|*
literal|60L
operator|*
literal|12L
operator|)
argument_list|)
return|;
block|}
specifier|public
name|long
name|zkMaxAge
parameter_list|()
block|{
return|return
name|getLong
argument_list|(
name|ZooKeeperCleanup
operator|.
name|ZK_CLEANUP_MAX_AGE
argument_list|,
operator|(
literal|1000L
operator|*
literal|60L
operator|*
literal|60L
operator|*
literal|24L
operator|*
literal|7L
operator|)
argument_list|)
return|;
block|}
specifier|public
name|String
name|zkHosts
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|ZooKeeperStorage
operator|.
name|ZK_HOSTS
argument_list|)
return|;
block|}
specifier|public
name|int
name|zkSessionTimeout
parameter_list|()
block|{
return|return
name|getInt
argument_list|(
name|ZooKeeperStorage
operator|.
name|ZK_SESSION_TIMEOUT
argument_list|,
literal|30000
argument_list|)
return|;
block|}
block|}
end_class

end_unit

