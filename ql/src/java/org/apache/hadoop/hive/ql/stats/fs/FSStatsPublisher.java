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
name|ql
operator|.
name|stats
operator|.
name|fs
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
name|util
operator|.
name|HashMap
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
name|Map
operator|.
name|Entry
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
name|common
operator|.
name|StatsSetupConst
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
name|SerializationUtilities
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
name|stats
operator|.
name|StatsCollectionContext
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
name|stats
operator|.
name|StatsPublisher
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
name|com
operator|.
name|esotericsoftware
operator|.
name|kryo
operator|.
name|Kryo
import|;
end_import

begin_import
import|import
name|com
operator|.
name|esotericsoftware
operator|.
name|kryo
operator|.
name|io
operator|.
name|Output
import|;
end_import

begin_class
specifier|public
class|class
name|FSStatsPublisher
implements|implements
name|StatsPublisher
block|{
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|statsMap
decl_stmt|;
comment|// map from partID -> (statType->value)
annotation|@
name|Override
specifier|public
name|boolean
name|init
parameter_list|(
name|StatsCollectionContext
name|context
parameter_list|)
block|{
try|try
block|{
for|for
control|(
name|String
name|tmpDir
range|:
name|context
operator|.
name|getStatsTmpDirs
argument_list|()
control|)
block|{
name|Path
name|statsDir
init|=
operator|new
name|Path
argument_list|(
name|tmpDir
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Initing FSStatsPublisher with : "
operator|+
name|statsDir
argument_list|)
expr_stmt|;
name|statsDir
operator|.
name|getFileSystem
argument_list|(
name|context
operator|.
name|getHiveConf
argument_list|()
argument_list|)
operator|.
name|mkdirs
argument_list|(
name|statsDir
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"created : "
operator|+
name|statsDir
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to create dir"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|connect
parameter_list|(
name|StatsCollectionContext
name|context
parameter_list|)
block|{
name|conf
operator|=
name|context
operator|.
name|getHiveConf
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|statsDirs
init|=
name|context
operator|.
name|getStatsTmpDirs
argument_list|()
decl_stmt|;
assert|assert
name|statsDirs
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|:
literal|"Found multiple stats dirs: "
operator|+
name|statsDirs
assert|;
name|Path
name|statsDir
init|=
operator|new
name|Path
argument_list|(
name|statsDirs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Connecting to : "
operator|+
name|statsDir
argument_list|)
expr_stmt|;
name|statsMap
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|statsDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|exists
argument_list|(
name|statsDir
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to check if dir exists"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|publishStat
parameter_list|(
name|String
name|partKV
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|stats
parameter_list|)
block|{
comment|// we need to do new hashmap, since stats object is reused across calls.
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|cpy
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
name|stats
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|statMap
init|=
name|statsMap
operator|.
name|get
argument_list|(
name|partKV
argument_list|)
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|statMap
condition|)
block|{
comment|// In case of LB, we might get called repeatedly.
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|e
range|:
name|statMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|cpy
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
operator|+
name|Long
operator|.
name|parseLong
argument_list|(
name|cpy
operator|.
name|get
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|statsMap
operator|.
name|put
argument_list|(
name|partKV
argument_list|,
name|cpy
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|closeConnection
parameter_list|(
name|StatsCollectionContext
name|context
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|statsDirs
init|=
name|context
operator|.
name|getStatsTmpDirs
argument_list|()
decl_stmt|;
assert|assert
name|statsDirs
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|:
literal|"Found multiple stats dirs: "
operator|+
name|statsDirs
assert|;
if|if
condition|(
name|context
operator|.
name|getContextSuffix
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"ContextSuffix must be set before publishing!"
argument_list|)
throw|;
block|}
name|Path
name|statsDir
init|=
operator|new
name|Path
argument_list|(
name|statsDirs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|String
name|suffix
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|conf
operator|.
name|getInt
argument_list|(
literal|"mapred.task.partition"
argument_list|,
literal|0
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|getContextSuffix
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|suffix
operator|+=
literal|"_"
operator|+
name|context
operator|.
name|getContextSuffix
argument_list|()
expr_stmt|;
block|}
name|Path
name|statsFile
init|=
operator|new
name|Path
argument_list|(
name|statsDir
argument_list|,
name|StatsSetupConst
operator|.
name|STATS_FILE_PREFIX
operator|+
name|suffix
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|trace
argument_list|(
literal|"About to create stats file for this task : {}"
argument_list|,
name|statsFile
argument_list|)
expr_stmt|;
name|Output
name|output
init|=
operator|new
name|Output
argument_list|(
name|statsFile
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|create
argument_list|(
name|statsFile
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Created file : "
operator|+
name|statsFile
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Writing stats in it : "
operator|+
name|statsMap
argument_list|)
expr_stmt|;
name|Kryo
name|kryo
init|=
name|SerializationUtilities
operator|.
name|borrowKryo
argument_list|()
decl_stmt|;
try|try
block|{
name|kryo
operator|.
name|writeObject
argument_list|(
name|output
argument_list|,
name|statsMap
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|SerializationUtilities
operator|.
name|releaseKryo
argument_list|(
name|kryo
argument_list|)
expr_stmt|;
block|}
name|output
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|error
argument_list|(
literal|"Failed to persist stats on filesystem"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

