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
name|exec
operator|.
name|spark
operator|.
name|counter
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|hive
operator|.
name|shims
operator|.
name|ShimLoader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaSparkContext
import|;
end_import

begin_comment
comment|/**  * We use group to fold all the same kind of counters.  */
end_comment

begin_class
specifier|public
class|class
name|SparkCounterGroup
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
name|String
name|groupName
decl_stmt|;
specifier|private
name|String
name|groupDisplayName
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|SparkCounter
argument_list|>
name|sparkCounters
decl_stmt|;
specifier|private
specifier|transient
name|JavaSparkContext
name|javaSparkContext
decl_stmt|;
specifier|public
name|SparkCounterGroup
parameter_list|(
name|String
name|groupName
parameter_list|,
name|String
name|groupDisplayName
parameter_list|,
name|JavaSparkContext
name|javaSparkContext
parameter_list|)
block|{
name|this
operator|.
name|groupName
operator|=
name|groupName
expr_stmt|;
name|this
operator|.
name|groupDisplayName
operator|=
name|groupDisplayName
expr_stmt|;
name|this
operator|.
name|javaSparkContext
operator|=
name|javaSparkContext
expr_stmt|;
name|sparkCounters
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|SparkCounter
argument_list|>
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|createCounter
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|initValue
parameter_list|)
block|{
name|String
name|displayName
init|=
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|getCounterGroupName
argument_list|(
name|groupName
argument_list|,
name|groupName
argument_list|)
decl_stmt|;
name|SparkCounter
name|counter
init|=
operator|new
name|SparkCounter
argument_list|(
name|name
argument_list|,
name|displayName
argument_list|,
name|groupName
argument_list|,
name|initValue
argument_list|,
name|javaSparkContext
argument_list|)
decl_stmt|;
name|sparkCounters
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|counter
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SparkCounter
name|getCounter
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|sparkCounters
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
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
name|String
name|getGroupDisplayName
parameter_list|()
block|{
return|return
name|groupDisplayName
return|;
block|}
specifier|public
name|void
name|setGroupDisplayName
parameter_list|(
name|String
name|groupDisplayName
parameter_list|)
block|{
name|this
operator|.
name|groupDisplayName
operator|=
name|groupDisplayName
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|SparkCounter
argument_list|>
name|getSparkCounters
parameter_list|()
block|{
return|return
name|sparkCounters
return|;
block|}
block|}
end_class

end_unit

