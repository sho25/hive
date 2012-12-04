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
name|session
operator|.
name|SessionState
import|;
end_import

begin_comment
comment|/**  * An test implementation for StatsAggregator.  * aggregateStats prints the length of the keyPrefix to SessionState's out stream  * All other methods are no-ops.  */
end_comment

begin_class
specifier|public
class|class
name|KeyVerifyingStatsAggregator
implements|implements
name|StatsAggregator
block|{
specifier|public
name|boolean
name|connect
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
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
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
comment|// Have to use the length instead of the actual prefix because the prefix is location dependent
comment|// 17 is 16 (16 byte MD5 hash) + 1 for the path separator
name|ss
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Stats prefix is hashed: "
operator|+
operator|new
name|Boolean
argument_list|(
name|keyPrefix
operator|.
name|length
argument_list|()
operator|==
literal|17
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|public
name|boolean
name|closeConnection
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
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

