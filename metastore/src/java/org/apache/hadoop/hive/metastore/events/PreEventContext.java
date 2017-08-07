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
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|common
operator|.
name|classification
operator|.
name|InterfaceStability
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
name|HiveMetaStore
operator|.
name|HMSHandler
import|;
end_import

begin_comment
comment|/**  * Base class for all the events which are defined for metastore.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
specifier|abstract
class|class
name|PreEventContext
block|{
specifier|public
specifier|static
enum|enum
name|PreEventType
block|{
name|CREATE_TABLE
block|,
name|DROP_TABLE
block|,
name|ALTER_TABLE
block|,
name|ADD_PARTITION
block|,
name|DROP_PARTITION
block|,
name|ALTER_PARTITION
block|,
name|CREATE_DATABASE
block|,
name|DROP_DATABASE
block|,
name|LOAD_PARTITION_DONE
block|,
name|AUTHORIZATION_API_CALL
block|,
name|READ_TABLE
block|,
name|READ_DATABASE
block|,
name|ADD_INDEX
block|,
name|ALTER_INDEX
block|,
name|DROP_INDEX
block|,
name|ALTER_DATABASE
block|}
specifier|private
specifier|final
name|PreEventType
name|eventType
decl_stmt|;
specifier|private
specifier|final
name|HMSHandler
name|handler
decl_stmt|;
specifier|public
name|PreEventContext
parameter_list|(
name|PreEventType
name|eventType
parameter_list|,
name|HMSHandler
name|handler
parameter_list|)
block|{
name|this
operator|.
name|eventType
operator|=
name|eventType
expr_stmt|;
name|this
operator|.
name|handler
operator|=
name|handler
expr_stmt|;
block|}
comment|/**    * @return the event type    */
specifier|public
name|PreEventType
name|getEventType
parameter_list|()
block|{
return|return
name|eventType
return|;
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
block|}
end_class

end_unit

