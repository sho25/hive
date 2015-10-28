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
name|tez
operator|.
name|TezTask
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|common
operator|.
name|counters
operator|.
name|TezCounters
import|;
end_import

begin_comment
comment|/**  * This class aggregates stats via counters and does so for Tez Tasks.  * With dbclass=counters this class will compute table/partition statistics  * using hadoop counters. They will be published using special keys and  * then retrieved on the client after the insert/ctas statement ran.  */
end_comment

begin_class
specifier|public
class|class
name|CounterStatsAggregatorTez
implements|implements
name|StatsAggregator
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|CounterStatsAggregatorTez
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|TezCounters
name|counters
decl_stmt|;
specifier|private
specifier|final
name|CounterStatsAggregator
name|mrAggregator
decl_stmt|;
specifier|private
name|boolean
name|delegate
decl_stmt|;
specifier|public
name|CounterStatsAggregatorTez
parameter_list|()
block|{
name|mrAggregator
operator|=
operator|new
name|CounterStatsAggregator
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|connect
parameter_list|(
name|StatsCollectionContext
name|scc
parameter_list|)
block|{
name|Task
name|sourceTask
init|=
name|scc
operator|.
name|getTask
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|sourceTask
operator|instanceof
name|TezTask
operator|)
condition|)
block|{
name|delegate
operator|=
literal|true
expr_stmt|;
return|return
name|mrAggregator
operator|.
name|connect
argument_list|(
name|scc
argument_list|)
return|;
block|}
name|counters
operator|=
operator|(
operator|(
name|TezTask
operator|)
name|sourceTask
operator|)
operator|.
name|getTezCounters
argument_list|()
expr_stmt|;
return|return
name|counters
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|aggregateStats
parameter_list|(
name|String
name|keyPrefix
parameter_list|,
name|String
name|statType
parameter_list|)
block|{
name|String
name|result
decl_stmt|;
if|if
condition|(
name|delegate
condition|)
block|{
name|result
operator|=
name|mrAggregator
operator|.
name|aggregateStats
argument_list|(
name|keyPrefix
argument_list|,
name|statType
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|long
name|value
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|groupName
range|:
name|counters
operator|.
name|getGroupNames
argument_list|()
control|)
block|{
if|if
condition|(
name|groupName
operator|.
name|startsWith
argument_list|(
name|keyPrefix
argument_list|)
condition|)
block|{
name|value
operator|+=
name|counters
operator|.
name|getGroup
argument_list|(
name|groupName
argument_list|)
operator|.
name|findCounter
argument_list|(
name|statType
argument_list|)
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
block|}
name|result
operator|=
name|String
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Counter based stats for ("
operator|+
name|keyPrefix
operator|+
literal|") are: "
operator|+
name|result
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|closeConnection
parameter_list|(
name|StatsCollectionContext
name|scc
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

