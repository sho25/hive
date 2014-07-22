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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|Task
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
name|mr
operator|.
name|ExecDriver
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
name|Task
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
name|mr
operator|.
name|MapRedTask
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
name|Counters
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
name|mapred
operator|.
name|RunningJob
import|;
end_import

begin_class
specifier|public
class|class
name|CounterStatsAggregator
implements|implements
name|StatsAggregator
implements|,
name|StatsCollectionTaskIndependent
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
name|CounterStatsAggregator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|Counters
name|counters
decl_stmt|;
specifier|private
name|JobClient
name|jc
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|connect
parameter_list|(
name|Configuration
name|hconf
parameter_list|,
name|Task
name|sourceTask
parameter_list|)
block|{
try|try
block|{
name|jc
operator|=
operator|new
name|JobClient
argument_list|(
name|toJobConf
argument_list|(
name|hconf
argument_list|)
argument_list|)
expr_stmt|;
name|RunningJob
name|job
init|=
name|jc
operator|.
name|getJob
argument_list|(
operator|(
operator|(
name|MapRedTask
operator|)
name|sourceTask
operator|)
operator|.
name|getJobID
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|job
operator|!=
literal|null
condition|)
block|{
name|counters
operator|=
name|job
operator|.
name|getCounters
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to get Job instance for "
operator|+
name|sourceTask
operator|.
name|getJobID
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|counters
operator|!=
literal|null
return|;
block|}
specifier|private
name|JobConf
name|toJobConf
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
block|{
return|return
name|hconf
operator|instanceof
name|JobConf
condition|?
operator|(
name|JobConf
operator|)
name|hconf
else|:
operator|new
name|JobConf
argument_list|(
name|hconf
argument_list|,
name|ExecDriver
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|aggregateStats
parameter_list|(
name|String
name|counterGrpName
parameter_list|,
name|String
name|statType
parameter_list|)
block|{
comment|// In case of counters, aggregation is done by JobTracker / MR AM itself
comment|// so no need to aggregate, simply return the counter value for requested stat.
return|return
name|String
operator|.
name|valueOf
argument_list|(
name|counters
operator|.
name|getGroup
argument_list|(
name|counterGrpName
argument_list|)
operator|.
name|getCounter
argument_list|(
name|statType
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|closeConnection
parameter_list|()
block|{
try|try
block|{
name|jc
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Error closing job client for stats aggregator."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|cleanUp
parameter_list|(
name|String
name|keyPrefix
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

