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
name|stats
package|;
end_package

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
name|spark
operator|.
name|SparkTask
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

begin_class
specifier|public
class|class
name|CounterStatsAggregatorSpark
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
name|CounterStatsAggregatorSpark
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|SparkCounters
name|sparkCounters
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
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
name|SparkTask
name|task
init|=
operator|(
name|SparkTask
operator|)
name|scc
operator|.
name|getTask
argument_list|()
decl_stmt|;
name|sparkCounters
operator|=
name|task
operator|.
name|getSparkCounters
argument_list|()
expr_stmt|;
if|if
condition|(
name|sparkCounters
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
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
name|long
name|value
init|=
name|sparkCounters
operator|.
name|getValue
argument_list|(
name|keyPrefix
argument_list|,
name|statType
argument_list|)
decl_stmt|;
name|String
name|result
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Counter based stats for (%s, %s) are: %s"
argument_list|,
name|keyPrefix
argument_list|,
name|statType
argument_list|,
name|result
argument_list|)
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

