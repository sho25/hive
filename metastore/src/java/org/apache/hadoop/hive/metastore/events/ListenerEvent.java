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
name|events
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
name|metastore
operator|.
name|HiveMetaStore
operator|.
name|HMSHandler
import|;
end_import

begin_comment
comment|/**  * Base class for all the events which are defined for metastore.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|ListenerEvent
block|{
comment|/**    * status of the event, whether event was successful or not.    */
specifier|private
specifier|final
name|boolean
name|status
decl_stmt|;
specifier|private
specifier|final
name|HMSHandler
name|handler
decl_stmt|;
specifier|public
name|ListenerEvent
parameter_list|(
name|boolean
name|status
parameter_list|,
name|HMSHandler
name|handler
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
name|this
operator|.
name|handler
operator|=
name|handler
expr_stmt|;
block|}
comment|/**    * @return the handler    */
specifier|public
name|HMSHandler
name|getHandler
parameter_list|()
block|{
return|return
name|handler
return|;
block|}
comment|/**    * @return the status of event.    */
specifier|public
name|boolean
name|getStatus
parameter_list|()
block|{
return|return
name|status
return|;
block|}
block|}
end_class

end_unit

