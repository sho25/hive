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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|hadoop
operator|.
name|io
operator|.
name|LongWritable
import|;
end_import

begin_class
specifier|public
class|class
name|Stat
block|{
comment|// stored stats
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|LongWritable
argument_list|>
name|statsMap
decl_stmt|;
comment|// additional bookkeeping info for the stored stats
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|bookkeepingInfo
decl_stmt|;
specifier|public
name|Stat
parameter_list|()
block|{
name|statsMap
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|LongWritable
argument_list|>
argument_list|()
expr_stmt|;
name|bookkeepingInfo
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|addToStat
parameter_list|(
name|String
name|statType
parameter_list|,
name|long
name|amount
parameter_list|)
block|{
name|LongWritable
name|currentValue
init|=
name|statsMap
operator|.
name|get
argument_list|(
name|statType
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentValue
operator|==
literal|null
condition|)
block|{
name|statsMap
operator|.
name|put
argument_list|(
name|statType
argument_list|,
operator|new
name|LongWritable
argument_list|(
name|amount
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|currentValue
operator|.
name|set
argument_list|(
name|currentValue
operator|.
name|get
argument_list|()
operator|+
name|amount
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|long
name|getStat
parameter_list|(
name|String
name|statType
parameter_list|)
block|{
name|LongWritable
name|currValue
init|=
name|statsMap
operator|.
name|get
argument_list|(
name|statType
argument_list|)
decl_stmt|;
if|if
condition|(
name|currValue
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|currValue
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|getStoredStats
parameter_list|()
block|{
return|return
name|statsMap
operator|.
name|keySet
argument_list|()
return|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|statsMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|// additional information about stats (e.g., virtual column number
specifier|public
name|void
name|setBookkeepingInfo
parameter_list|(
name|String
name|statType
parameter_list|,
name|int
name|info
parameter_list|)
block|{
name|bookkeepingInfo
operator|.
name|put
argument_list|(
name|statType
argument_list|,
name|info
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getBookkeepingInfo
parameter_list|(
name|String
name|statType
parameter_list|)
block|{
name|Integer
name|info
init|=
name|bookkeepingInfo
operator|.
name|get
argument_list|(
name|statType
argument_list|)
decl_stmt|;
if|if
condition|(
name|info
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
name|info
operator|.
name|intValue
argument_list|()
return|;
block|}
block|}
end_class

end_unit

