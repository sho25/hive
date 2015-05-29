begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|api
operator|.
name|repl
operator|.
name|exim
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|api
operator|.
name|HCatNotificationEvent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|api
operator|.
name|repl
operator|.
name|NoopReplicationTask
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_class
specifier|public
class|class
name|CreateDatabaseReplicationTask
extends|extends
name|NoopReplicationTask
block|{
comment|// "CREATE DATABASE" is specifically not replicated across, per design, since if a user
comment|// drops a database and recreates another with the same one, we want to distinguish
comment|// between the two. We will replicate the drop across, but after that, the goal is
comment|// that if a new db is created, a new replication definition should be created in
comment|// the replication implementer above this. Thus, we extend NoopReplicationTask and
comment|// the only additional thing we do is validate event type.
specifier|public
name|CreateDatabaseReplicationTask
parameter_list|(
name|HCatNotificationEvent
name|event
parameter_list|)
block|{
name|super
argument_list|(
name|event
argument_list|)
expr_stmt|;
name|validateEventType
argument_list|(
name|event
argument_list|,
name|HCatConstants
operator|.
name|HCAT_CREATE_DATABASE_EVENT
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

