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
operator|.
name|tool
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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
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
name|conf
operator|.
name|Configured
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
name|FileSystem
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
name|fs
operator|.
name|permission
operator|.
name|FsPermission
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
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|hive
operator|.
name|metastore
operator|.
name|HiveMetaStoreClient
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
name|shims
operator|.
name|ShimLoader
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
name|io
operator|.
name|NullWritable
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
name|io
operator|.
name|Text
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
name|JobClient
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
name|JobConf
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
name|mapreduce
operator|.
name|Job
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
name|mapreduce
operator|.
name|JobID
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
name|mapreduce
operator|.
name|lib
operator|.
name|output
operator|.
name|NullOutputFormat
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
name|mapreduce
operator|.
name|security
operator|.
name|token
operator|.
name|delegation
operator|.
name|DelegationTokenIdentifier
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
name|security
operator|.
name|UserGroupInformation
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
name|security
operator|.
name|token
operator|.
name|Token
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
name|Tool
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
name|AppConfig
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
name|Main
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
name|SecureProxySupport
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
name|UgiFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_comment
comment|/**  * A Map Reduce job that will start another job.  *  * We have a single Mapper job that starts a child MR job.  The parent  * monitors the child child job and ends when the child job exits.  In  * addition, we  *  * - write out the parent job id so the caller can record it.  * - run a keep alive thread so the job doesn't end.  * - Optionally, store the stdout, stderr, and exit value of the child  *   in hdfs files.  *     * A note on security.  When jobs are submitted through WebHCat that use HCatalog, it means that  * metastore access is required.  Hive queries, of course, need metastore access.  This in turn  * requires delegation token to be obtained for metastore in a<em>secure cluster</em>.  Since we  * can't usually parse the job to find out if it is using metastore, we require 'usehcatalog'  * parameter supplied in the REST call.  WebHcat takes care of cancelling the token when the job  * is complete.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TempletonControllerJob
extends|extends
name|Configured
implements|implements
name|Tool
implements|,
name|JobSubmissionConstants
block|{
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
name|TempletonControllerJob
operator|.
name|class
argument_list|)
decl_stmt|;
comment|//file to add to DistributedCache
specifier|private
specifier|static
name|URI
name|overrideLog4jURI
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|overrideContainerLog4jProps
decl_stmt|;
comment|//Jar cmd submission likely will be affected, Pig likely not
specifier|private
specifier|static
specifier|final
name|String
name|affectedMsg
init|=
literal|"Monitoring of Hadoop jobs submitted through WebHCat "
operator|+
literal|"may be affected."
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TMP_DIR_PROP
init|=
literal|"hadoop.tmp.dir"
decl_stmt|;
comment|/**    * Copy the file from local file system to tmp dir    */
specifier|private
specifier|static
name|URI
name|copyLog4JtoFileSystem
parameter_list|(
specifier|final
name|String
name|localFile
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|UserGroupInformation
name|ugi
init|=
name|UserGroupInformation
operator|.
name|getLoginUser
argument_list|()
decl_stmt|;
return|return
name|ugi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|URI
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|URI
name|run
parameter_list|()
throws|throws
name|IOException
block|{
name|AppConfig
name|appConfig
init|=
name|Main
operator|.
name|getAppConfigInstance
argument_list|()
decl_stmt|;
name|String
name|fsTmpDir
init|=
name|appConfig
operator|.
name|get
argument_list|(
name|TMP_DIR_PROP
argument_list|)
decl_stmt|;
if|if
condition|(
name|fsTmpDir
operator|==
literal|null
operator|||
name|fsTmpDir
operator|.
name|length
argument_list|()
operator|<=
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Could not find 'hadoop.tmp.dir'; "
operator|+
name|affectedMsg
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|appConfig
argument_list|)
decl_stmt|;
name|Path
name|dirPath
init|=
operator|new
name|Path
argument_list|(
name|fsTmpDir
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|dirPath
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|dirPath
operator|+
literal|" does not exist; "
operator|+
name|affectedMsg
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|Path
name|dst
init|=
name|fs
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|fsTmpDir
argument_list|,
name|CONTAINER_LOG4J_PROPS
argument_list|)
argument_list|)
decl_stmt|;
name|fs
operator|.
name|copyFromLocalFile
argument_list|(
operator|new
name|Path
argument_list|(
name|localFile
argument_list|)
argument_list|,
name|dst
argument_list|)
expr_stmt|;
comment|//make readable by all users since TempletonControllerJob#run() is run as submitting user
name|fs
operator|.
name|setPermission
argument_list|(
name|dst
argument_list|,
operator|new
name|FsPermission
argument_list|(
operator|(
name|short
operator|)
literal|0644
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|dst
operator|.
name|toUri
argument_list|()
return|;
block|}
block|}
argument_list|)
return|;
block|}
comment|/**    * local file system    * @return    */
specifier|private
specifier|static
name|String
name|getLog4JPropsLocal
parameter_list|()
block|{
return|return
name|AppConfig
operator|.
name|getWebhcatConfDir
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
name|CONTAINER_LOG4J_PROPS
return|;
block|}
static|static
block|{
comment|//initialize once-per-JVM (i.e. one running WebHCat server) state and log it once since it's
comment|// the same for every job
try|try
block|{
comment|//safe (thread) publication
comment|// http://docs.oracle.com/javase/specs/jls/se5.0/html/execution.html#12.4.2
name|LOG
operator|.
name|info
argument_list|(
literal|"Using Hadoop Version: "
operator|+
name|ShimLoader
operator|.
name|getMajorVersion
argument_list|()
argument_list|)
expr_stmt|;
name|overrideContainerLog4jProps
operator|=
literal|"0.23"
operator|.
name|equals
argument_list|(
name|ShimLoader
operator|.
name|getMajorVersion
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|overrideContainerLog4jProps
condition|)
block|{
comment|//see detailed note in CONTAINER_LOG4J_PROPS file
name|LOG
operator|.
name|info
argument_list|(
name|AppConfig
operator|.
name|WEBHCAT_CONF_DIR
operator|+
literal|"="
operator|+
name|AppConfig
operator|.
name|getWebhcatConfDir
argument_list|()
argument_list|)
expr_stmt|;
name|File
name|localFile
init|=
operator|new
name|File
argument_list|(
name|getLog4JPropsLocal
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|localFile
operator|.
name|exists
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Found "
operator|+
name|localFile
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|" to use for job submission."
argument_list|)
expr_stmt|;
try|try
block|{
name|overrideLog4jURI
operator|=
name|copyLog4JtoFileSystem
argument_list|(
name|getLog4JPropsLocal
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Job submission will use log4j.properties="
operator|+
name|overrideLog4jURI
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Will not add "
operator|+
name|CONTAINER_LOG4J_PROPS
operator|+
literal|" to Distributed Cache.  "
operator|+
literal|"Some fields in job status may be unavailable"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Could not find "
operator|+
name|localFile
operator|.
name|getAbsolutePath
argument_list|()
operator|+
literal|". "
operator|+
name|affectedMsg
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|//this intentionally doesn't use TempletonControllerJob.class.getName() to be able to
comment|//log errors which may be due to class loading
name|String
name|msg
init|=
literal|"org.apache.hive.hcatalog.templeton.tool.TempletonControllerJob is not "
operator|+
literal|"properly initialized. "
operator|+
name|affectedMsg
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|final
name|boolean
name|secureMetastoreAccess
decl_stmt|;
comment|/**    * @param secureMetastoreAccess - if true, a delegation token will be created    *                              and added to the job    */
specifier|public
name|TempletonControllerJob
parameter_list|(
name|boolean
name|secureMetastoreAccess
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|secureMetastoreAccess
operator|=
name|secureMetastoreAccess
expr_stmt|;
block|}
specifier|private
name|JobID
name|submittedJobId
decl_stmt|;
specifier|public
name|String
name|getSubmittedId
parameter_list|()
block|{
if|if
condition|(
name|submittedJobId
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
name|submittedJobId
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
comment|/**    * Enqueue the job and print out the job id for later collection.    * @see org.apache.hive.hcatalog.templeton.CompleteDelegator    */
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ClassNotFoundException
throws|,
name|TException
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Preparing to submit job: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Configuration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|JAR_ARGS_NAME
argument_list|,
name|TempletonUtils
operator|.
name|encodeArray
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|user
init|=
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
operator|.
name|getShortUserName
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"user.name"
argument_list|,
name|user
argument_list|)
expr_stmt|;
if|if
condition|(
name|overrideContainerLog4jProps
operator|&&
name|overrideLog4jURI
operator|!=
literal|null
condition|)
block|{
comment|//must be done before Job object is created
name|conf
operator|.
name|set
argument_list|(
name|OVERRIDE_CONTAINER_LOG4J_PROPS
argument_list|,
name|Boolean
operator|.
name|TRUE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|LaunchMapper
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setJobName
argument_list|(
name|TempletonControllerJob
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapperClass
argument_list|(
name|LaunchMapper
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputKeyClass
argument_list|(
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setMapOutputValueClass
argument_list|(
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setInputFormatClass
argument_list|(
name|SingleInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
if|if
condition|(
name|overrideContainerLog4jProps
operator|&&
name|overrideLog4jURI
operator|!=
literal|null
condition|)
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
name|overrideLog4jURI
argument_list|)
argument_list|)
condition|)
block|{
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getWebHCatShim
argument_list|(
name|conf
argument_list|,
name|UgiFactory
operator|.
name|getUgi
argument_list|(
name|user
argument_list|)
argument_list|)
operator|.
name|addCacheFile
argument_list|(
name|overrideLog4jURI
argument_list|,
name|job
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"added "
operator|+
name|overrideLog4jURI
operator|+
literal|" to Dist Cache"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|//in case this file was deleted by someone issue a warning but don't try to add to
comment|// DistributedCache as that will throw and fail job submission
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cannot find "
operator|+
name|overrideLog4jURI
operator|+
literal|" which is created on WebHCat startup/job "
operator|+
literal|"submission.  "
operator|+
name|affectedMsg
argument_list|)
expr_stmt|;
block|}
block|}
name|NullOutputFormat
argument_list|<
name|NullWritable
argument_list|,
name|NullWritable
argument_list|>
name|of
init|=
operator|new
name|NullOutputFormat
argument_list|<
name|NullWritable
argument_list|,
name|NullWritable
argument_list|>
argument_list|()
decl_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|of
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|JobClient
name|jc
init|=
operator|new
name|JobClient
argument_list|(
operator|new
name|JobConf
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Token
argument_list|<
name|DelegationTokenIdentifier
argument_list|>
name|mrdt
init|=
name|jc
operator|.
name|getDelegationToken
argument_list|(
operator|new
name|Text
argument_list|(
literal|"mr token"
argument_list|)
argument_list|)
decl_stmt|;
name|job
operator|.
name|getCredentials
argument_list|()
operator|.
name|addToken
argument_list|(
operator|new
name|Text
argument_list|(
literal|"mr token"
argument_list|)
argument_list|,
name|mrdt
argument_list|)
expr_stmt|;
name|String
name|metastoreTokenStrForm
init|=
name|addHMSToken
argument_list|(
name|job
argument_list|,
name|user
argument_list|)
decl_stmt|;
name|job
operator|.
name|submit
argument_list|()
expr_stmt|;
name|submittedJobId
operator|=
name|job
operator|.
name|getJobID
argument_list|()
expr_stmt|;
if|if
condition|(
name|metastoreTokenStrForm
operator|!=
literal|null
condition|)
block|{
comment|//so that it can be cancelled later from CompleteDelegator
name|DelegationTokenCache
operator|.
name|getStringFormTokenCache
argument_list|()
operator|.
name|storeDelegationToken
argument_list|(
name|submittedJobId
operator|.
name|toString
argument_list|()
argument_list|,
name|metastoreTokenStrForm
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Added metastore delegation token for jobId="
operator|+
name|submittedJobId
operator|.
name|toString
argument_list|()
operator|+
literal|" user="
operator|+
name|user
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|overrideContainerLog4jProps
operator|&&
name|overrideLog4jURI
operator|==
literal|null
condition|)
block|{
comment|//do this here so that log msg has JobID
name|LOG
operator|.
name|warn
argument_list|(
literal|"Could not override container log4j properties for "
operator|+
name|submittedJobId
argument_list|)
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|String
name|addHMSToken
parameter_list|(
name|Job
name|job
parameter_list|,
name|String
name|user
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|TException
block|{
if|if
condition|(
operator|!
name|secureMetastoreAccess
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Token
argument_list|<
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|thrift
operator|.
name|DelegationTokenIdentifier
argument_list|>
name|hiveToken
init|=
operator|new
name|Token
argument_list|<
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|thrift
operator|.
name|DelegationTokenIdentifier
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|metastoreTokenStrForm
init|=
name|buildHcatDelegationToken
argument_list|(
name|user
argument_list|)
decl_stmt|;
name|hiveToken
operator|.
name|decodeFromUrlString
argument_list|(
name|metastoreTokenStrForm
argument_list|)
expr_stmt|;
name|job
operator|.
name|getCredentials
argument_list|()
operator|.
name|addToken
argument_list|(
operator|new
name|Text
argument_list|(
name|SecureProxySupport
operator|.
name|HCAT_SERVICE
argument_list|)
argument_list|,
name|hiveToken
argument_list|)
expr_stmt|;
return|return
name|metastoreTokenStrForm
return|;
block|}
specifier|private
name|String
name|buildHcatDelegationToken
parameter_list|(
name|String
name|user
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|TException
block|{
specifier|final
name|HiveConf
name|c
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Creating hive metastore delegation token for user "
operator|+
name|user
argument_list|)
expr_stmt|;
specifier|final
name|UserGroupInformation
name|ugi
init|=
name|UgiFactory
operator|.
name|getUgi
argument_list|(
name|user
argument_list|)
decl_stmt|;
name|UserGroupInformation
name|real
init|=
name|ugi
operator|.
name|getRealUser
argument_list|()
decl_stmt|;
return|return
name|real
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|run
parameter_list|()
throws|throws
name|IOException
throws|,
name|TException
throws|,
name|InterruptedException
block|{
specifier|final
name|HiveMetaStoreClient
name|client
init|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|c
argument_list|)
decl_stmt|;
return|return
name|ugi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
specifier|public
name|String
name|run
parameter_list|()
throws|throws
name|IOException
throws|,
name|TException
throws|,
name|InterruptedException
block|{
name|String
name|u
init|=
name|ugi
operator|.
name|getUserName
argument_list|()
decl_stmt|;
return|return
name|client
operator|.
name|getDelegationToken
argument_list|(
name|u
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
block|}
end_class

end_unit

