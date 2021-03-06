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
name|ql
operator|.
name|parse
operator|.
name|repl
operator|.
name|dump
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
name|api
operator|.
name|NotificationEvent
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
name|messaging
operator|.
name|EventMessage
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
name|parse
operator|.
name|repl
operator|.
name|dump
operator|.
name|Utils
import|;
end_import

begin_class
specifier|abstract
class|class
name|AbstractConstraintEventHandler
parameter_list|<
name|T
extends|extends
name|EventMessage
parameter_list|>
extends|extends
name|AbstractEventHandler
argument_list|<
name|T
argument_list|>
block|{
name|AbstractConstraintEventHandler
parameter_list|(
name|NotificationEvent
name|event
parameter_list|)
block|{
name|super
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
name|boolean
name|shouldReplicate
parameter_list|(
name|Context
name|withinContext
parameter_list|)
block|{
return|return
name|Utils
operator|.
name|shouldReplicate
argument_list|(
name|event
argument_list|,
name|withinContext
operator|.
name|replicationSpec
argument_list|,
name|withinContext
operator|.
name|db
argument_list|,
literal|true
argument_list|,
name|withinContext
operator|.
name|getTablesForBootstrap
argument_list|()
argument_list|,
name|withinContext
operator|.
name|oldReplScope
argument_list|,
name|withinContext
operator|.
name|hiveConf
argument_list|)
return|;
block|}
block|}
end_class

end_unit

