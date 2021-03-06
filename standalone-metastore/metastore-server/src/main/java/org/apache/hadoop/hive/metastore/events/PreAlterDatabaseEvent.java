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
name|hive
operator|.
name|metastore
operator|.
name|IHMSHandler
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
name|Database
import|;
end_import

begin_class
specifier|public
class|class
name|PreAlterDatabaseEvent
extends|extends
name|PreEventContext
block|{
specifier|private
specifier|final
name|Database
name|oldDB
decl_stmt|,
name|newDB
decl_stmt|;
specifier|public
name|PreAlterDatabaseEvent
parameter_list|(
name|Database
name|oldDB
parameter_list|,
name|Database
name|newDB
parameter_list|,
name|IHMSHandler
name|handler
parameter_list|)
block|{
name|super
argument_list|(
name|PreEventType
operator|.
name|ALTER_DATABASE
argument_list|,
name|handler
argument_list|)
expr_stmt|;
name|this
operator|.
name|oldDB
operator|=
name|oldDB
expr_stmt|;
name|this
operator|.
name|newDB
operator|=
name|newDB
expr_stmt|;
block|}
comment|/**    * @return the old db    */
specifier|public
name|Database
name|getOldDatabase
parameter_list|()
block|{
return|return
name|oldDB
return|;
block|}
comment|/**    * @return the new db    */
specifier|public
name|Database
name|getNewDatabase
parameter_list|()
block|{
return|return
name|newDB
return|;
block|}
block|}
end_class

end_unit

