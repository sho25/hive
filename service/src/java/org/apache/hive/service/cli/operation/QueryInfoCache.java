begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|operation
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
name|hive
operator|.
name|ql
operator|.
name|QueryInfo
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
name|Map
import|;
end_import

begin_comment
comment|/**  * Cache some SQLOperation information for WebUI  */
end_comment

begin_class
specifier|public
class|class
name|QueryInfoCache
extends|extends
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|QueryInfo
argument_list|>
block|{
specifier|private
specifier|final
name|int
name|capacity
decl_stmt|;
specifier|public
name|QueryInfoCache
parameter_list|(
name|int
name|capacity
parameter_list|)
block|{
name|super
argument_list|(
name|capacity
operator|+
literal|1
argument_list|,
literal|1.1f
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|capacity
operator|=
name|capacity
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|removeEldestEntry
parameter_list|(
name|Map
operator|.
name|Entry
name|eldest
parameter_list|)
block|{
return|return
name|size
argument_list|()
operator|>
name|capacity
return|;
block|}
block|}
end_class

end_unit

