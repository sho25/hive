begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|druid
package|;
end_package

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
name|Throwables
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
name|collect
operator|.
name|ImmutableMap
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
name|io
operator|.
name|FileUtils
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
name|service
operator|.
name|AbstractService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

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

begin_comment
comment|/**  * This class has the hooks to start and stop the external Druid Nodes  */
end_comment

begin_class
specifier|public
class|class
name|MiniDruidCluster
extends|extends
name|AbstractService
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|log
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MiniDruidCluster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COMMON_DRUID_JVM_PROPPERTIES
init|=
literal|"-Duser.timezone=UTC -Dfile.encoding=UTF-8 -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager -Ddruid.emitter=logging -Ddruid.emitter.logging.logLevel=info"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|HISTORICAL_JVM_CONF
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"-server"
argument_list|,
literal|"-XX:MaxDirectMemorySize=10g"
argument_list|,
literal|"-Xmx512m"
argument_list|,
literal|"-Xmx512m"
argument_list|,
name|COMMON_DRUID_JVM_PROPPERTIES
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|COORDINATOR_JVM_CONF
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"-server"
argument_list|,
literal|"-XX:MaxDirectMemorySize=2g"
argument_list|,
literal|"-Xmx512m"
argument_list|,
literal|"-Xms512m"
argument_list|,
name|COMMON_DRUID_JVM_PROPPERTIES
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|COMMON_DRUID_CONF
init|=
name|ImmutableMap
operator|.
name|of
argument_list|(
literal|"druid.metadata.storage.type"
argument_list|,
literal|"derby"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|COMMON_DRUID_HISTORICAL
init|=
name|ImmutableMap
operator|.
name|of
argument_list|(
literal|"druid.processing.buffer.sizeBytes"
argument_list|,
literal|"213870912"
argument_list|,
literal|"druid.processing.numThreads"
argument_list|,
literal|"2"
argument_list|,
literal|"druid.server.maxSize"
argument_list|,
literal|"130000000000"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|COMMON_COORDINATOR_INDEXER
init|=
name|ImmutableMap
operator|.
name|of
argument_list|(
literal|"druid.indexer.logs.type"
argument_list|,
literal|"file"
argument_list|,
literal|"druid.coordinator.asOverlord.enabled"
argument_list|,
literal|"true"
argument_list|,
literal|"druid.coordinator.asOverlord.overlordService"
argument_list|,
literal|"druid/overlord"
argument_list|,
literal|"druid.coordinator.period"
argument_list|,
literal|"PT10S"
argument_list|,
literal|"druid.manager.segments.pollDuration"
argument_list|,
literal|"PT10S"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|DruidNode
name|historical
decl_stmt|;
specifier|private
specifier|final
name|DruidNode
name|broker
decl_stmt|;
comment|// Coordinator is running as Overlord as well.
specifier|private
specifier|final
name|DruidNode
name|coordinator
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|DruidNode
argument_list|>
name|druidNodes
decl_stmt|;
specifier|private
specifier|final
name|File
name|dataDirectory
decl_stmt|;
specifier|private
specifier|final
name|File
name|logDirectory
decl_stmt|;
specifier|public
name|MiniDruidCluster
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
literal|"/tmp/miniDruid/log"
argument_list|,
literal|"/tmp/miniDruid/data"
argument_list|,
literal|2181
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MiniDruidCluster
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|logDir
parameter_list|,
name|String
name|dataDir
parameter_list|,
name|Integer
name|zookeeperPort
parameter_list|,
name|String
name|classpath
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|dataDirectory
operator|=
operator|new
name|File
argument_list|(
name|dataDir
argument_list|,
literal|"druid-data"
argument_list|)
expr_stmt|;
name|this
operator|.
name|logDirectory
operator|=
operator|new
name|File
argument_list|(
name|logDir
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|dataDirectory
operator|.
name|exists
argument_list|()
condition|)
block|{
comment|// need to clean data directory to ensure that there is no interference from old runs
comment|// Cleaning is happening here to allow debugging in case of tests fail
comment|// we don;t have to clean logs since it is an append mode
name|log
operator|.
name|info
argument_list|(
literal|"Cleaning the druid-data directory [{}]"
argument_list|,
name|dataDirectory
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|FileUtils
operator|.
name|deleteDirectory
argument_list|(
name|dataDirectory
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|log
operator|.
name|info
argument_list|(
literal|"Creating the druid-data directory [{}]"
argument_list|,
name|dataDirectory
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|dataDirectory
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|log
operator|.
name|error
argument_list|(
literal|"Failed to clean data directory"
argument_list|)
expr_stmt|;
name|Throwables
operator|.
name|propagate
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
name|String
name|derbyURI
init|=
name|String
operator|.
name|format
argument_list|(
literal|"jdbc:derby://localhost:1527/%s/druid_derby/metadata.db;create=true"
argument_list|,
name|dataDirectory
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|segmentsCache
init|=
name|String
operator|.
name|format
argument_list|(
literal|"[{\"path\":\"%s/druid/segment-cache\",\"maxSize\":130000000000}]"
argument_list|,
name|dataDirectory
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|indexingLogDir
init|=
operator|new
name|File
argument_list|(
name|logDirectory
argument_list|,
literal|"indexer-log"
argument_list|)
operator|.
name|getAbsolutePath
argument_list|()
decl_stmt|;
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|coordinatorMapBuilder
init|=
operator|new
name|ImmutableMap
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|historicalMapBuilder
init|=
operator|new
name|ImmutableMap
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|coordinatorProperties
init|=
name|coordinatorMapBuilder
operator|.
name|putAll
argument_list|(
name|COMMON_DRUID_CONF
argument_list|)
operator|.
name|putAll
argument_list|(
name|COMMON_COORDINATOR_INDEXER
argument_list|)
operator|.
name|put
argument_list|(
literal|"druid.metadata.storage.connector.connectURI"
argument_list|,
name|derbyURI
argument_list|)
operator|.
name|put
argument_list|(
literal|"druid.indexer.logs.directory"
argument_list|,
name|indexingLogDir
argument_list|)
operator|.
name|put
argument_list|(
literal|"druid.zk.service.host"
argument_list|,
literal|"localhost:"
operator|+
name|zookeeperPort
argument_list|)
operator|.
name|put
argument_list|(
literal|"druid.coordinator.startDelay"
argument_list|,
literal|"PT1S"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|historicalProperties
init|=
name|historicalMapBuilder
operator|.
name|putAll
argument_list|(
name|COMMON_DRUID_CONF
argument_list|)
operator|.
name|putAll
argument_list|(
name|COMMON_DRUID_HISTORICAL
argument_list|)
operator|.
name|put
argument_list|(
literal|"druid.zk.service.host"
argument_list|,
literal|"localhost:"
operator|+
name|zookeeperPort
argument_list|)
operator|.
name|put
argument_list|(
literal|"druid.segmentCache.locations"
argument_list|,
name|segmentsCache
argument_list|)
operator|.
name|put
argument_list|(
literal|"druid.storage.storageDirectory"
argument_list|,
name|getDeepStorageDir
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"druid.storage.type"
argument_list|,
literal|"hdfs"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|coordinator
operator|=
operator|new
name|ForkingDruidNode
argument_list|(
literal|"coordinator"
argument_list|,
name|classpath
argument_list|,
name|coordinatorProperties
argument_list|,
name|COORDINATOR_JVM_CONF
argument_list|,
name|logDirectory
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|historical
operator|=
operator|new
name|ForkingDruidNode
argument_list|(
literal|"historical"
argument_list|,
name|classpath
argument_list|,
name|historicalProperties
argument_list|,
name|HISTORICAL_JVM_CONF
argument_list|,
name|logDirectory
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|broker
operator|=
operator|new
name|ForkingDruidNode
argument_list|(
literal|"broker"
argument_list|,
name|classpath
argument_list|,
name|historicalProperties
argument_list|,
name|HISTORICAL_JVM_CONF
argument_list|,
name|logDirectory
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|druidNodes
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|coordinator
argument_list|,
name|historical
argument_list|,
name|broker
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|serviceStart
parameter_list|()
throws|throws
name|Exception
block|{
name|druidNodes
operator|.
name|stream
argument_list|()
operator|.
name|forEach
argument_list|(
name|node
lambda|->
block|{
try|try
block|{
name|node
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|log
operator|.
name|error
argument_list|(
literal|"Failed to start node "
operator|+
name|node
operator|.
name|getNodeType
argument_list|()
operator|+
literal|" Consequently will destroy the cluster"
argument_list|)
expr_stmt|;
name|druidNodes
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|node1
lambda|->
name|node1
operator|.
name|isAlive
argument_list|()
argument_list|)
operator|.
name|forEach
argument_list|(
name|nodeToStop
lambda|->
block|{
lambda|try
block|{
name|log
operator|.
name|info
argument_list|(
literal|"Stopping Node "
operator|+
name|nodeToStop
operator|.
name|getNodeType
argument_list|()
argument_list|)
expr_stmt|;
name|nodeToStop
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|log
operator|.
name|error
argument_list|(
literal|"Error while stopping "
operator|+
name|nodeToStop
operator|.
name|getNodeType
argument_list|()
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|Throwables
operator|.
name|propagate
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
end_class

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_function
unit|}    @
name|Override
specifier|protected
name|void
name|serviceStop
parameter_list|()
throws|throws
name|Exception
block|{
name|druidNodes
operator|.
name|stream
argument_list|()
operator|.
name|forEach
argument_list|(
name|node
lambda|->
block|{
try|try
block|{
name|node
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// nothing that we can really do about it
name|log
operator|.
name|error
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Failed to stop druid node [%s]"
argument_list|,
name|node
operator|.
name|getNodeType
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
specifier|public
name|String
name|getMetadataURI
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"jdbc:derby://localhost:1527/%s/druid_derby/metadata.db"
argument_list|,
name|dataDirectory
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
return|;
block|}
end_function

begin_function
specifier|public
name|String
name|getDeepStorageDir
parameter_list|()
block|{
return|return
name|dataDirectory
operator|.
name|getAbsolutePath
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
literal|"deep-storage"
return|;
block|}
end_function

unit|}
end_unit

