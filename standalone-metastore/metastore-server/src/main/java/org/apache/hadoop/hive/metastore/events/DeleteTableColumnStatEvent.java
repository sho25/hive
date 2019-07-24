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
name|IHMSHandler
import|;
end_import

begin_comment
comment|/**  * DeleteTableColumnStatEvent  * Event generated for table column stat delete event.  */
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
class|class
name|DeleteTableColumnStatEvent
extends|extends
name|ListenerEvent
block|{
specifier|private
name|String
name|catName
decl_stmt|,
name|dbName
decl_stmt|,
name|tableName
decl_stmt|,
name|colName
decl_stmt|,
name|engine
decl_stmt|;
comment|/**    * @param catName catalog name    * @param dbName database name    * @param tableName table name    * @param colName column name    * @param engine engine    * @param handler handler that is firing the event    */
specifier|public
name|DeleteTableColumnStatEvent
parameter_list|(
name|String
name|catName
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|colName
parameter_list|,
name|String
name|engine
parameter_list|,
name|IHMSHandler
name|handler
parameter_list|)
block|{
name|super
argument_list|(
literal|true
argument_list|,
name|handler
argument_list|)
expr_stmt|;
name|this
operator|.
name|catName
operator|=
name|catName
expr_stmt|;
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|colName
operator|=
name|colName
expr_stmt|;
name|this
operator|.
name|engine
operator|=
name|engine
expr_stmt|;
block|}
specifier|public
name|String
name|getCatName
parameter_list|()
block|{
return|return
name|catName
return|;
block|}
specifier|public
name|String
name|getDBName
parameter_list|()
block|{
return|return
name|dbName
return|;
block|}
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
specifier|public
name|String
name|getColName
parameter_list|()
block|{
return|return
name|colName
return|;
block|}
specifier|public
name|String
name|getEngine
parameter_list|()
block|{
return|return
name|engine
return|;
block|}
block|}
end_class

end_unit

