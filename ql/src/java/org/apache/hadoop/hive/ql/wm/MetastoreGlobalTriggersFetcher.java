begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  * http://www.apache.org/licenses/LICENSE-2.0  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|wm
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|metadata
operator|.
name|Hive
import|;
end_import

begin_comment
comment|/**  * Fetch global (non-llap) rules from metastore  */
end_comment

begin_class
specifier|public
class|class
name|MetastoreGlobalTriggersFetcher
block|{
specifier|private
specifier|static
specifier|final
name|String
name|GLOBAL_TRIGGER_NAME
init|=
literal|"global"
decl_stmt|;
specifier|private
name|Hive
name|db
decl_stmt|;
specifier|public
name|MetastoreGlobalTriggersFetcher
parameter_list|(
specifier|final
name|Hive
name|db
parameter_list|)
block|{
name|this
operator|.
name|db
operator|=
name|db
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Trigger
argument_list|>
name|fetch
parameter_list|()
block|{
comment|// TODO: this entire class will go away, DDLTask will push RP to TezSessionPoolManager where triggers are available
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|()
return|;
block|}
block|}
end_class

end_unit

