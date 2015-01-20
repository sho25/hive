begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ql
operator|.
name|exec
operator|.
name|spark
package|;
end_package

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
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|MalformedURLException
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Future
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|ql
operator|.
name|Context
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
name|ql
operator|.
name|DriverContext
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
name|ql
operator|.
name|exec
operator|.
name|Utilities
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
name|ql
operator|.
name|exec
operator|.
name|spark
operator|.
name|status
operator|.
name|SparkJobRef
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
name|ql
operator|.
name|exec
operator|.
name|spark
operator|.
name|status
operator|.
name|impl
operator|.
name|RemoteSparkJobRef
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
name|ql
operator|.
name|exec
operator|.
name|spark
operator|.
name|status
operator|.
name|impl
operator|.
name|RemoteSparkJobStatus
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
name|ql
operator|.
name|io
operator|.
name|HiveKey
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
name|ql
operator|.
name|plan
operator|.
name|BaseWork
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
name|ql
operator|.
name|plan
operator|.
name|SparkWork
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
name|ql
operator|.
name|session
operator|.
name|SessionState
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
name|BytesWritable
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
name|hive
operator|.
name|spark
operator|.
name|client
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
name|hive
operator|.
name|spark
operator|.
name|client
operator|.
name|JobContext
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
name|spark
operator|.
name|client
operator|.
name|JobHandle
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
name|spark
operator|.
name|client
operator|.
name|SparkClient
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
name|spark
operator|.
name|client
operator|.
name|SparkClientFactory
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
name|spark
operator|.
name|counter
operator|.
name|SparkCounters
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|SparkConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|SparkException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaFutureAction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaPairRDD
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Splitter
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Strings
import|;
end_import

begin_comment
comment|/**  * RemoteSparkClient is a wrapper of {@link org.apache.hive.spark.client.SparkClient}, which  * wrap a spark job request and send to an remote SparkContext.  */
end_comment

begin_class
specifier|public
class|class
name|RemoteHiveSparkClient
implements|implements
name|HiveSparkClient
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MR_JAR_PROPERTY
init|=
literal|"tmpjars"
decl_stmt|;
specifier|protected
specifier|static
specifier|final
specifier|transient
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RemoteHiveSparkClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
specifier|transient
name|Splitter
name|CSV_SPLITTER
init|=
name|Splitter
operator|.
name|on
argument_list|(
literal|","
argument_list|)
operator|.
name|omitEmptyStrings
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
name|SparkClient
name|remoteClient
decl_stmt|;
specifier|private
specifier|transient
name|SparkConf
name|sparkConf
decl_stmt|;
specifier|private
specifier|transient
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|transient
name|List
argument_list|<
name|String
argument_list|>
name|localJars
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
name|List
argument_list|<
name|String
argument_list|>
name|localFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|RemoteHiveSparkClient
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|SparkException
block|{
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|sparkConf
operator|=
name|HiveSparkClientFactory
operator|.
name|generateSparkConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|remoteClient
operator|=
name|SparkClientFactory
operator|.
name|createClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|SparkConf
name|getSparkConf
parameter_list|()
block|{
return|return
name|sparkConf
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getExecutorCount
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|timeout
init|=
name|hiveConf
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_CLIENT_FUTURE_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|Future
argument_list|<
name|Integer
argument_list|>
name|handler
init|=
name|remoteClient
operator|.
name|getExecutorCount
argument_list|()
decl_stmt|;
return|return
name|handler
operator|.
name|get
argument_list|(
name|timeout
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|intValue
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getDefaultParallelism
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|timeout
init|=
name|hiveConf
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_CLIENT_FUTURE_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|Future
argument_list|<
name|Integer
argument_list|>
name|handler
init|=
name|remoteClient
operator|.
name|getDefaultParallelism
argument_list|()
decl_stmt|;
return|return
name|handler
operator|.
name|get
argument_list|(
name|timeout
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SparkJobRef
name|execute
parameter_list|(
specifier|final
name|DriverContext
name|driverContext
parameter_list|,
specifier|final
name|SparkWork
name|sparkWork
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|Context
name|ctx
init|=
name|driverContext
operator|.
name|getCtx
argument_list|()
decl_stmt|;
specifier|final
name|HiveConf
name|hiveConf
init|=
operator|(
name|HiveConf
operator|)
name|ctx
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|refreshLocalResources
argument_list|(
name|sparkWork
argument_list|,
name|hiveConf
argument_list|)
expr_stmt|;
specifier|final
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
comment|// Create temporary scratch dir
specifier|final
name|Path
name|emptyScratchDir
init|=
name|ctx
operator|.
name|getMRTmpPath
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|emptyScratchDir
operator|.
name|getFileSystem
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|emptyScratchDir
argument_list|)
expr_stmt|;
name|byte
index|[]
name|jobConfBytes
init|=
name|KryoSerializer
operator|.
name|serializeJobConf
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|byte
index|[]
name|scratchDirBytes
init|=
name|KryoSerializer
operator|.
name|serialize
argument_list|(
name|emptyScratchDir
argument_list|)
decl_stmt|;
name|byte
index|[]
name|sparkWorkBytes
init|=
name|KryoSerializer
operator|.
name|serialize
argument_list|(
name|sparkWork
argument_list|)
decl_stmt|;
name|long
name|timeout
init|=
name|hiveConf
operator|.
name|getTimeVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPARK_CLIENT_FUTURE_TIMEOUT
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|JobHandle
argument_list|<
name|Serializable
argument_list|>
name|jobHandle
init|=
name|remoteClient
operator|.
name|submit
argument_list|(
operator|new
name|JobStatusJob
argument_list|(
name|jobConfBytes
argument_list|,
name|scratchDirBytes
argument_list|,
name|sparkWorkBytes
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|RemoteSparkJobRef
argument_list|(
name|jobHandle
argument_list|,
operator|new
name|RemoteSparkJobStatus
argument_list|(
name|remoteClient
argument_list|,
name|jobHandle
argument_list|,
name|timeout
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|void
name|refreshLocalResources
parameter_list|(
name|SparkWork
name|sparkWork
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
block|{
comment|// add hive-exec jar
name|addJars
argument_list|(
operator|(
operator|new
name|JobConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|)
operator|.
name|getJar
argument_list|()
argument_list|)
expr_stmt|;
comment|// add aux jars
name|addJars
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEAUXJARS
argument_list|)
argument_list|)
expr_stmt|;
comment|// add added jars
name|String
name|addedJars
init|=
name|Utilities
operator|.
name|getResourceFiles
argument_list|(
name|conf
argument_list|,
name|SessionState
operator|.
name|ResourceType
operator|.
name|JAR
argument_list|)
decl_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEADDEDJARS
argument_list|,
name|addedJars
argument_list|)
expr_stmt|;
name|addJars
argument_list|(
name|addedJars
argument_list|)
expr_stmt|;
comment|// add plugin module jars on demand
comment|// jobConf will hold all the configuration for hadoop, tez, and hive
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
name|MR_JAR_PROPERTY
argument_list|,
literal|""
argument_list|)
expr_stmt|;
for|for
control|(
name|BaseWork
name|work
range|:
name|sparkWork
operator|.
name|getAllWork
argument_list|()
control|)
block|{
name|work
operator|.
name|configureJobConf
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
block|}
name|addJars
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|MR_JAR_PROPERTY
argument_list|)
argument_list|)
expr_stmt|;
comment|// add added files
name|String
name|addedFiles
init|=
name|Utilities
operator|.
name|getResourceFiles
argument_list|(
name|conf
argument_list|,
name|SessionState
operator|.
name|ResourceType
operator|.
name|FILE
argument_list|)
decl_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEADDEDFILES
argument_list|,
name|addedFiles
argument_list|)
expr_stmt|;
name|addResources
argument_list|(
name|addedFiles
argument_list|)
expr_stmt|;
comment|// add added archives
name|String
name|addedArchives
init|=
name|Utilities
operator|.
name|getResourceFiles
argument_list|(
name|conf
argument_list|,
name|SessionState
operator|.
name|ResourceType
operator|.
name|ARCHIVE
argument_list|)
decl_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEADDEDARCHIVES
argument_list|,
name|addedArchives
argument_list|)
expr_stmt|;
name|addResources
argument_list|(
name|addedArchives
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|addResources
parameter_list|(
name|String
name|addedFiles
parameter_list|)
block|{
for|for
control|(
name|String
name|addedFile
range|:
name|CSV_SPLITTER
operator|.
name|split
argument_list|(
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|addedFiles
argument_list|)
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|localFiles
operator|.
name|contains
argument_list|(
name|addedFile
argument_list|)
condition|)
block|{
name|localFiles
operator|.
name|add
argument_list|(
name|addedFile
argument_list|)
expr_stmt|;
try|try
block|{
name|remoteClient
operator|.
name|addFile
argument_list|(
name|SparkUtilities
operator|.
name|getURL
argument_list|(
name|addedFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MalformedURLException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to add file:"
operator|+
name|addedFile
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|void
name|addJars
parameter_list|(
name|String
name|addedJars
parameter_list|)
block|{
for|for
control|(
name|String
name|addedJar
range|:
name|CSV_SPLITTER
operator|.
name|split
argument_list|(
name|Strings
operator|.
name|nullToEmpty
argument_list|(
name|addedJars
argument_list|)
argument_list|)
control|)
block|{
if|if
condition|(
operator|!
name|localJars
operator|.
name|contains
argument_list|(
name|addedJar
argument_list|)
condition|)
block|{
name|localJars
operator|.
name|add
argument_list|(
name|addedJar
argument_list|)
expr_stmt|;
try|try
block|{
name|remoteClient
operator|.
name|addJar
argument_list|(
name|SparkUtilities
operator|.
name|getURL
argument_list|(
name|addedJar
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MalformedURLException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to add jar:"
operator|+
name|addedJar
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|remoteClient
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|JobStatusJob
implements|implements
name|Job
argument_list|<
name|Serializable
argument_list|>
block|{
specifier|private
specifier|final
name|byte
index|[]
name|jobConfBytes
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|scratchDirBytes
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|sparkWorkBytes
decl_stmt|;
specifier|private
name|JobStatusJob
parameter_list|()
block|{
comment|// For deserialization.
name|this
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|JobStatusJob
parameter_list|(
name|byte
index|[]
name|jobConfBytes
parameter_list|,
name|byte
index|[]
name|scratchDirBytes
parameter_list|,
name|byte
index|[]
name|sparkWorkBytes
parameter_list|)
block|{
name|this
operator|.
name|jobConfBytes
operator|=
name|jobConfBytes
expr_stmt|;
name|this
operator|.
name|scratchDirBytes
operator|=
name|scratchDirBytes
expr_stmt|;
name|this
operator|.
name|sparkWorkBytes
operator|=
name|sparkWorkBytes
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Serializable
name|call
parameter_list|(
name|JobContext
name|jc
parameter_list|)
throws|throws
name|Exception
block|{
name|JobConf
name|localJobConf
init|=
name|KryoSerializer
operator|.
name|deserializeJobConf
argument_list|(
name|jobConfBytes
argument_list|)
decl_stmt|;
name|Path
name|localScratchDir
init|=
name|KryoSerializer
operator|.
name|deserialize
argument_list|(
name|scratchDirBytes
argument_list|,
name|Path
operator|.
name|class
argument_list|)
decl_stmt|;
name|SparkWork
name|localSparkWork
init|=
name|KryoSerializer
operator|.
name|deserialize
argument_list|(
name|sparkWorkBytes
argument_list|,
name|SparkWork
operator|.
name|class
argument_list|)
decl_stmt|;
name|SparkCounters
name|sparkCounters
init|=
operator|new
name|SparkCounters
argument_list|(
name|jc
operator|.
name|sc
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|prefixes
init|=
name|localSparkWork
operator|.
name|getRequiredCounterPrefix
argument_list|()
decl_stmt|;
if|if
condition|(
name|prefixes
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|group
range|:
name|prefixes
operator|.
name|keySet
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|counterName
range|:
name|prefixes
operator|.
name|get
argument_list|(
name|group
argument_list|)
control|)
block|{
name|sparkCounters
operator|.
name|createCounter
argument_list|(
name|group
argument_list|,
name|counterName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|SparkReporter
name|sparkReporter
init|=
operator|new
name|SparkReporter
argument_list|(
name|sparkCounters
argument_list|)
decl_stmt|;
comment|// Generate Spark plan
name|SparkPlanGenerator
name|gen
init|=
operator|new
name|SparkPlanGenerator
argument_list|(
name|jc
operator|.
name|sc
argument_list|()
argument_list|,
literal|null
argument_list|,
name|localJobConf
argument_list|,
name|localScratchDir
argument_list|,
name|sparkReporter
argument_list|)
decl_stmt|;
name|SparkPlan
name|plan
init|=
name|gen
operator|.
name|generate
argument_list|(
name|localSparkWork
argument_list|)
decl_stmt|;
comment|// Execute generated plan.
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|finalRDD
init|=
name|plan
operator|.
name|generateGraph
argument_list|()
decl_stmt|;
comment|// We use Spark RDD async action to submit job as it's the only way to get jobId now.
name|JavaFutureAction
argument_list|<
name|Void
argument_list|>
name|future
init|=
name|finalRDD
operator|.
name|foreachAsync
argument_list|(
name|HiveVoidFunction
operator|.
name|getInstance
argument_list|()
argument_list|)
decl_stmt|;
name|jc
operator|.
name|monitor
argument_list|(
name|future
argument_list|,
name|sparkCounters
argument_list|,
name|plan
operator|.
name|getCachedRDDIds
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

