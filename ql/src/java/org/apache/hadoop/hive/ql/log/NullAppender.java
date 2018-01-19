begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|log
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Level
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|LogManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|Filter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|Layout
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|LogEvent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|LoggerContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|appender
operator|.
name|AbstractAppender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|config
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|config
operator|.
name|LoggerConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|config
operator|.
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|config
operator|.
name|plugins
operator|.
name|PluginFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|core
operator|.
name|layout
operator|.
name|PatternLayout
import|;
end_import

begin_comment
comment|/**  * A NullAppender merely exists, it never outputs a message to any device.  */
end_comment

begin_class
annotation|@
name|Plugin
argument_list|(
name|name
operator|=
literal|"NullAppender"
argument_list|,
name|category
operator|=
literal|"Core"
argument_list|,
name|elementType
operator|=
literal|"appender"
argument_list|,
name|printObject
operator|=
literal|false
argument_list|)
specifier|public
class|class
name|NullAppender
extends|extends
name|AbstractAppender
block|{
specifier|private
specifier|static
name|LoggerContext
name|context
init|=
operator|(
name|LoggerContext
operator|)
name|LogManager
operator|.
name|getContext
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Configuration
name|configuration
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|protected
name|NullAppender
parameter_list|(
name|String
name|name
parameter_list|,
name|Filter
name|filter
parameter_list|,
name|Layout
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|layout
parameter_list|,
name|boolean
name|ignoreExceptions
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|filter
argument_list|,
name|layout
argument_list|,
name|ignoreExceptions
argument_list|)
expr_stmt|;
block|}
annotation|@
name|PluginFactory
specifier|public
specifier|static
name|NullAppender
name|createNullAppender
parameter_list|()
block|{
return|return
operator|new
name|NullAppender
argument_list|(
literal|"NullAppender"
argument_list|,
literal|null
argument_list|,
name|PatternLayout
operator|.
name|createDefaultLayout
argument_list|()
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|public
name|void
name|addToLogger
parameter_list|(
name|String
name|loggerName
parameter_list|,
name|Level
name|level
parameter_list|)
block|{
name|LoggerConfig
name|loggerConfig
init|=
name|configuration
operator|.
name|getLoggerConfig
argument_list|(
name|loggerName
argument_list|)
decl_stmt|;
name|loggerConfig
operator|.
name|addAppender
argument_list|(
name|this
argument_list|,
name|level
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|context
operator|.
name|updateLoggers
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|append
parameter_list|(
name|LogEvent
name|event
parameter_list|)
block|{
comment|// no-op
block|}
block|}
end_class

end_unit

