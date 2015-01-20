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
name|JobMetricsListener
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
name|LocalSparkJobRef
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
name|LocalSparkJobStatus
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
name|JavaSparkContext
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
comment|/**  * LocalSparkClient submit Spark job in local driver, it's responsible for build spark client  * environment and execute spark work.  */
end_comment

begin_class
specifier|public
class|class
name|LocalHiveSparkClient
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
name|LocalHiveSparkClient
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
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
specifier|static
name|LocalHiveSparkClient
name|client
decl_stmt|;
specifier|public
specifier|static
specifier|synchronized
name|LocalHiveSparkClient
name|getInstance
parameter_list|(
name|SparkConf
name|sparkConf
parameter_list|)
block|{
if|if
condition|(
name|client
operator|==
literal|null
condition|)
block|{
name|client
operator|=
operator|new
name|LocalHiveSparkClient
argument_list|(
name|sparkConf
argument_list|)
expr_stmt|;
block|}
return|return
name|client
return|;
block|}
specifier|private
name|JavaSparkContext
name|sc
decl_stmt|;
specifier|private
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
specifier|private
name|JobMetricsListener
name|jobMetricsListener
decl_stmt|;
specifier|private
name|LocalHiveSparkClient
parameter_list|(
name|SparkConf
name|sparkConf
parameter_list|)
block|{
name|sc
operator|=
operator|new
name|JavaSparkContext
argument_list|(
name|sparkConf
argument_list|)
expr_stmt|;
name|jobMetricsListener
operator|=
operator|new
name|JobMetricsListener
argument_list|()
expr_stmt|;
name|sc
operator|.
name|sc
argument_list|()
operator|.
name|listenerBus
argument_list|()
operator|.
name|addListener
argument_list|(
name|jobMetricsListener
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
name|sc
operator|.
name|sc
argument_list|()
operator|.
name|conf
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getExecutorCount
parameter_list|()
block|{
return|return
name|sc
operator|.
name|sc
argument_list|()
operator|.
name|getExecutorMemoryStatus
argument_list|()
operator|.
name|size
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
return|return
name|sc
operator|.
name|sc
argument_list|()
operator|.
name|defaultParallelism
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|SparkJobRef
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|,
name|SparkWork
name|sparkWork
parameter_list|)
throws|throws
name|Exception
block|{
name|Context
name|ctx
init|=
name|driverContext
operator|.
name|getCtx
argument_list|()
decl_stmt|;
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
name|Path
name|emptyScratchDir
decl_stmt|;
name|emptyScratchDir
operator|=
name|ctx
operator|.
name|getMRTmpPath
argument_list|()
expr_stmt|;
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
name|SparkCounters
name|sparkCounters
init|=
operator|new
name|SparkCounters
argument_list|(
name|sc
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
name|sparkWork
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
name|sc
argument_list|,
name|ctx
argument_list|,
name|jobConf
argument_list|,
name|emptyScratchDir
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
name|sparkWork
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
comment|// As we always use foreach action to submit RDD graph, it would only trigger one job.
name|int
name|jobId
init|=
name|future
operator|.
name|jobIds
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|LocalSparkJobStatus
name|sparkJobStatus
init|=
operator|new
name|LocalSparkJobStatus
argument_list|(
name|sc
argument_list|,
name|jobId
argument_list|,
name|jobMetricsListener
argument_list|,
name|sparkCounters
argument_list|,
name|plan
operator|.
name|getCachedRDDIds
argument_list|()
argument_list|,
name|future
argument_list|)
decl_stmt|;
return|return
operator|new
name|LocalSparkJobRef
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|jobId
argument_list|)
argument_list|,
name|sparkJobStatus
argument_list|,
name|sc
argument_list|)
return|;
block|}
comment|/**    * At this point single SparkContext is used by more than one thread, so make this    * method synchronized.    *    * This method can't remove a jar/resource from SparkContext. Looks like this is an    * issue we have to live with until multiple SparkContexts are supported in a single JVM.    */
specifier|private
specifier|synchronized
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
name|sc
operator|.
name|addFile
argument_list|(
name|addedFile
argument_list|)
expr_stmt|;
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
name|sc
operator|.
name|addJar
argument_list|(
name|addedJar
argument_list|)
expr_stmt|;
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
name|sc
operator|.
name|stop
argument_list|()
expr_stmt|;
name|client
operator|=
literal|null
expr_stmt|;
block|}
block|}
end_class

end_unit

