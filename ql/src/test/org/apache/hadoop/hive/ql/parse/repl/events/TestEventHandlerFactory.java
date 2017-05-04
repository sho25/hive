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
name|ql
operator|.
name|parse
operator|.
name|repl
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
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_class
specifier|public
class|class
name|TestEventHandlerFactory
block|{
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|shouldNotAllowRegisteringEventsWhichCannotBeInstantiated
parameter_list|()
block|{
class|class
name|NonCompatibleEventHandler
implements|implements
name|EventHandler
block|{
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
block|{        }
annotation|@
name|Override
specifier|public
name|long
name|fromEventId
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|toEventId
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|DumpType
name|dumpType
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
name|EventHandlerFactory
operator|.
name|register
argument_list|(
literal|"anyEvent"
argument_list|,
name|NonCompatibleEventHandler
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|shouldProvideDefaultHandlerWhenNothingRegisteredForThatEvent
parameter_list|()
block|{
name|EventHandler
name|eventHandler
init|=
name|EventHandlerFactory
operator|.
name|handlerFor
argument_list|(
operator|new
name|NotificationEvent
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
literal|"shouldGiveDefaultHandler"
argument_list|,
literal|"s"
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|eventHandler
operator|instanceof
name|DefaultHandler
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

