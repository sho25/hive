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
name|optimizer
operator|.
name|calcite
operator|.
name|druid
package|;
end_package

begin_comment
comment|/**  * Type of Druid query.  *  * TODO: to be removed when Calcite is upgraded to 1.9  */
end_comment

begin_enum
specifier|public
enum|enum
name|DruidQueryType
block|{
name|SELECT
argument_list|(
literal|"select"
argument_list|)
block|,
name|TOP_N
argument_list|(
literal|"topN"
argument_list|)
block|,
name|GROUP_BY
argument_list|(
literal|"groupBy"
argument_list|)
block|,
name|TIMESERIES
argument_list|(
literal|"timeseries"
argument_list|)
block|;
specifier|private
specifier|final
name|String
name|queryName
decl_stmt|;
specifier|private
name|DruidQueryType
parameter_list|(
name|String
name|queryName
parameter_list|)
block|{
name|this
operator|.
name|queryName
operator|=
name|queryName
expr_stmt|;
block|}
specifier|public
name|String
name|getQueryName
parameter_list|()
block|{
return|return
name|this
operator|.
name|queryName
return|;
block|}
block|}
end_enum

begin_comment
comment|// End QueryType.java
end_comment

end_unit

