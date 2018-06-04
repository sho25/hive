begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|Statistic
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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

begin_class
specifier|public
class|class
name|SparkStatisticGroup
block|{
specifier|private
specifier|final
name|String
name|groupName
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|SparkStatistic
argument_list|>
name|statistics
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|SparkStatisticGroup
parameter_list|(
name|String
name|groupName
parameter_list|,
name|List
argument_list|<
name|SparkStatistic
argument_list|>
name|statisticList
parameter_list|)
block|{
name|this
operator|.
name|groupName
operator|=
name|groupName
expr_stmt|;
for|for
control|(
name|SparkStatistic
name|sparkStatistic
range|:
name|statisticList
control|)
block|{
name|this
operator|.
name|statistics
operator|.
name|put
argument_list|(
name|sparkStatistic
operator|.
name|getName
argument_list|()
argument_list|,
name|sparkStatistic
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|String
name|getGroupName
parameter_list|()
block|{
return|return
name|groupName
return|;
block|}
specifier|public
name|Iterator
argument_list|<
name|SparkStatistic
argument_list|>
name|getStatistics
parameter_list|()
block|{
return|return
name|this
operator|.
name|statistics
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
comment|/**    * Get a {@link SparkStatistic} by its given name    */
specifier|public
name|SparkStatistic
name|getSparkStatistic
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|this
operator|.
name|statistics
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|containsSparkStatistic
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|this
operator|.
name|statistics
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
return|;
block|}
block|}
end_class

end_unit

