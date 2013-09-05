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
name|HIVE_PROPS_NAME
init|=
literal|"templeton.hive.properties"
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
name|TEMPLETON_JAR_NAME
init|=
literal|"templeton.jar"
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
name|loadOneFileConfig
argument_list|(
name|hadoopConfDir
argument_list|,
name|fname
argument_list|)
expr_stmt|;
name|ProxyUserSupport
operator|.
name|processProxyuserConfig
argument_list|(
name|this
argument_list|)
expr_stmt|;
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
name|debug
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
name|debug
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
name|templetonJar
parameter_list|()
block|{
return|return
name|get
argument_list|(
name|TEMPLETON_JAR_NAME
argument_list|)
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

