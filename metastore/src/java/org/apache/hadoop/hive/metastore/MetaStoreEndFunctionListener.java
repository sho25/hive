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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configurable
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

begin_comment
comment|/**  * This abstract class needs to be extended to  provide implementation of actions that need  * to be performed when a function ends. These methods are called whenever a function ends.  *  * It also provides a way to add fb303 counters through the exportCounters method.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|MetaStoreEndFunctionListener
implements|implements
name|Configurable
block|{
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|MetaStoreEndFunctionListener
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|config
expr_stmt|;
block|}
specifier|public
specifier|abstract
name|void
name|onEndFunction
parameter_list|(
name|String
name|functionName
parameter_list|,
name|MetaStoreEndFunctionContext
name|context
parameter_list|)
function_decl|;
comment|// Unless this is overridden, it does nothing
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
block|{   }
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|this
operator|.
name|conf
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|config
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|config
expr_stmt|;
block|}
block|}
end_class

end_unit

