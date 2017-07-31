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
name|api
operator|.
name|Table
import|;
end_import

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
class|class
name|PreDropTableEvent
extends|extends
name|PreEventContext
block|{
specifier|private
specifier|final
name|Table
name|table
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|deleteData
decl_stmt|;
specifier|public
name|PreDropTableEvent
parameter_list|(
name|Table
name|table
parameter_list|,
name|boolean
name|deleteData
parameter_list|,
name|HMSHandler
name|handler
parameter_list|)
block|{
name|super
argument_list|(
name|PreEventType
operator|.
name|DROP_TABLE
argument_list|,
name|handler
argument_list|)
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
comment|// In HiveMetaStore, the deleteData flag indicates whether DFS data should be
comment|// removed on a drop.
name|this
operator|.
name|deleteData
operator|=
name|deleteData
expr_stmt|;
block|}
comment|/**    * @return the table    */
specifier|public
name|Table
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
comment|/**    * @return the deleteData flag    */
specifier|public
name|boolean
name|getDeleteData
parameter_list|()
block|{
return|return
name|deleteData
return|;
block|}
block|}
end_class

end_unit

