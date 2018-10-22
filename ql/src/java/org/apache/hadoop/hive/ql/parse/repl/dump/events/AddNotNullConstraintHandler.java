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
name|AddNotNullConstraintMessage
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
name|DumpType
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
name|load
operator|.
name|DumpMetaData
import|;
end_import

begin_class
specifier|public
class|class
name|AddNotNullConstraintHandler
extends|extends
name|AbstractConstraintEventHandler
argument_list|<
name|AddNotNullConstraintMessage
argument_list|>
block|{
name|AddNotNullConstraintHandler
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
annotation|@
name|Override
name|AddNotNullConstraintMessage
name|eventMessage
parameter_list|(
name|String
name|stringRepresentation
parameter_list|)
block|{
return|return
name|deserializer
operator|.
name|getAddNotNullConstraintMessage
argument_list|(
name|stringRepresentation
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handle
parameter_list|(
name|Context
name|withinContext
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Processing#{} ADD_NOTNULLCONSTRAINT_MESSAGE message : {}"
argument_list|,
name|fromEventId
argument_list|()
argument_list|,
name|eventMessageAsJSON
argument_list|)
expr_stmt|;
if|if
condition|(
name|shouldReplicate
argument_list|(
name|withinContext
argument_list|)
condition|)
block|{
name|DumpMetaData
name|dmd
init|=
name|withinContext
operator|.
name|createDmd
argument_list|(
name|this
argument_list|)
decl_stmt|;
name|dmd
operator|.
name|setPayload
argument_list|(
name|eventMessageAsJSON
argument_list|)
expr_stmt|;
name|dmd
operator|.
name|write
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|DumpType
name|dumpType
parameter_list|()
block|{
return|return
name|DumpType
operator|.
name|EVENT_ADD_NOTNULLCONSTRAINT
return|;
block|}
block|}
end_class

end_unit

