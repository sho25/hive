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
name|StatsCollectionTaskIndependent
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
implements|,
name|StatsCollectionTaskIndependent
block|{
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
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
name|Configuration
name|hconf
parameter_list|)
block|{
name|Path
name|statsDir
init|=
operator|new
name|Path
argument_list|(
name|hconf
operator|.
name|get
argument_list|(
name|StatsSetupConst
operator|.
name|STATS_TMP_LOC
argument_list|)
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
try|try
block|{
name|statsDir
operator|.
name|getFileSystem
argument_list|(
name|hconf
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
name|Configuration
name|hconf
parameter_list|)
block|{
name|conf
operator|=
name|hconf
expr_stmt|;
name|Path
name|statsDir
init|=
operator|new
name|Path
argument_list|(
name|hconf
operator|.
name|get
argument_list|(
name|StatsSetupConst
operator|.
name|STATS_TMP_LOC
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"Putting in map : "
operator|+
name|partKV
operator|+
literal|"\t"
operator|+
name|stats
argument_list|)
expr_stmt|;
comment|// we need to do new hashmap, since stats object is reused across calls.
name|statsMap
operator|.
name|put
argument_list|(
name|partKV
argument_list|,
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
parameter_list|()
block|{
name|Path
name|statsDir
init|=
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|StatsSetupConst
operator|.
name|STATS_TMP_LOC
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"About to create stats file for this task : "
operator|+
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
name|info
argument_list|(
literal|"Created file : "
operator|+
name|statsFile
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Writing stats in it : "
operator|+
name|statsMap
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|runtimeSerializationKryo
operator|.
name|get
argument_list|()
operator|.
name|writeObject
argument_list|(
name|output
argument_list|,
name|statsMap
argument_list|)
expr_stmt|;
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
name|LOG
operator|.
name|error
argument_list|(
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

