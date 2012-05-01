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
name|metastore
operator|.
name|hooks
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|AbstractMap
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
name|hive
operator|.
name|metastore
operator|.
name|MetaStoreEndFunctionContext
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
name|MetaStoreEndFunctionListener
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
name|hooks
operator|.
name|HookUtils
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
name|hooks
operator|.
name|conf
operator|.
name|FBHiveConf
import|;
end_import

begin_comment
comment|/*  * MetaStoreEndFunctionListener that uses the StatsManager to collect fb303 counters for  * the number of successes and failures for each metastore thrift function, bucketed by time.  */
end_comment

begin_class
specifier|public
class|class
name|CounterMetaStoreEndFunctionListener
extends|extends
name|MetaStoreEndFunctionListener
block|{
name|StatsManager
name|stats
init|=
literal|null
decl_stmt|;
specifier|public
name|CounterMetaStoreEndFunctionListener
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
name|super
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|String
name|statsMgr
init|=
name|config
operator|.
name|get
argument_list|(
name|FBHiveConf
operator|.
name|METASTORE_LISTENER_STATS_MANAGER
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|statsMgr
operator|==
literal|null
operator|)
operator|||
operator|(
name|statsMgr
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
return|return;
block|}
name|stats
operator|=
name|HookUtils
operator|.
name|getObject
argument_list|(
name|config
argument_list|,
name|statsMgr
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onEndFunction
parameter_list|(
name|String
name|functionName
parameter_list|,
name|MetaStoreEndFunctionContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|stats
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// Construct the counter name, as<functionName> for success
comment|// and<functionName.failure> for failure
name|String
name|statName
init|=
name|functionName
operator|+
operator|(
name|context
operator|.
name|isSuccess
argument_list|()
condition|?
literal|""
else|:
literal|".failure"
operator|)
decl_stmt|;
comment|// If this is the first time this counter name has been seen, initialize it
if|if
condition|(
operator|!
name|stats
operator|.
name|containsKey
argument_list|(
name|statName
argument_list|)
condition|)
block|{
name|stats
operator|.
name|addCountStatType
argument_list|(
name|statName
argument_list|)
expr_stmt|;
block|}
name|stats
operator|.
name|addStatValue
argument_list|(
name|statName
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|exportCounters
parameter_list|(
name|AbstractMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|counters
parameter_list|)
block|{
if|if
condition|(
name|stats
operator|==
literal|null
condition|)
block|{
return|return;
block|}
comment|// For each counter the StatsManager has collected, add it to the map of fb303 counters
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|stats
operator|.
name|getCounters
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|counters
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

