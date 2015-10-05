begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p/>  * http://www.apache.org/licenses/LICENSE-2.0  *<p/>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metadata
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStreamWriter
import|;
end_import

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
name|AbstractOutputStreamAppender
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
name|OutputStreamManager
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
name|PluginAttribute
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Log4j2 appender that writers to in-memory string object.  */
end_comment

begin_class
annotation|@
name|Plugin
argument_list|(
name|name
operator|=
literal|"StringAppender"
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
literal|true
argument_list|)
specifier|public
class|class
name|StringAppender
extends|extends
name|AbstractOutputStreamAppender
argument_list|<
name|StringAppender
operator|.
name|StringOutputStreamManager
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|String
name|APPENDER_NAME
init|=
literal|"StringAppender"
decl_stmt|;
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
specifier|private
name|StringOutputStreamManager
name|manager
decl_stmt|;
comment|/**    * Instantiate a WriterAppender and set the output destination to a    * new {@link OutputStreamWriter} initialized with<code>os</code>    * as its {@link OutputStream}.    *    * @param name             The name of the Appender.    * @param layout           The layout to format the message.    * @param filter    * @param ignoreExceptions    * @param immediateFlush    * @param manager          The OutputStreamManager.    */
specifier|protected
name|StringAppender
parameter_list|(
name|String
name|name
parameter_list|,
name|Layout
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|layout
parameter_list|,
name|Filter
name|filter
parameter_list|,
name|boolean
name|ignoreExceptions
parameter_list|,
name|boolean
name|immediateFlush
parameter_list|,
name|StringOutputStreamManager
name|manager
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|layout
argument_list|,
name|filter
argument_list|,
name|ignoreExceptions
argument_list|,
name|immediateFlush
argument_list|,
name|manager
argument_list|)
expr_stmt|;
name|this
operator|.
name|manager
operator|=
name|manager
expr_stmt|;
block|}
annotation|@
name|PluginFactory
specifier|public
specifier|static
name|StringAppender
name|createStringAppender
parameter_list|(
annotation|@
name|PluginAttribute
argument_list|(
literal|"name"
argument_list|)
name|String
name|nullablePatternString
parameter_list|)
block|{
name|PatternLayout
name|layout
decl_stmt|;
if|if
condition|(
name|nullablePatternString
operator|==
literal|null
condition|)
block|{
name|layout
operator|=
name|PatternLayout
operator|.
name|createDefaultLayout
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|layout
operator|=
name|PatternLayout
operator|.
name|createLayout
argument_list|(
name|nullablePatternString
argument_list|,
name|configuration
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|StringAppender
argument_list|(
name|APPENDER_NAME
argument_list|,
name|layout
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
operator|new
name|StringOutputStreamManager
argument_list|(
operator|new
name|ByteArrayOutputStream
argument_list|()
argument_list|,
literal|"StringStream"
argument_list|,
name|layout
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
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
annotation|@
name|VisibleForTesting
specifier|public
name|void
name|removeFromLogger
parameter_list|(
name|String
name|loggerName
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
name|removeAppender
argument_list|(
name|APPENDER_NAME
argument_list|)
expr_stmt|;
name|context
operator|.
name|updateLoggers
argument_list|()
expr_stmt|;
block|}
specifier|public
name|String
name|getOutput
parameter_list|()
block|{
name|manager
operator|.
name|flush
argument_list|()
expr_stmt|;
return|return
operator|new
name|String
argument_list|(
name|manager
operator|.
name|getStream
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|manager
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
specifier|protected
specifier|static
class|class
name|StringOutputStreamManager
extends|extends
name|OutputStreamManager
block|{
name|ByteArrayOutputStream
name|stream
decl_stmt|;
specifier|protected
name|StringOutputStreamManager
parameter_list|(
name|ByteArrayOutputStream
name|os
parameter_list|,
name|String
name|streamName
parameter_list|,
name|Layout
argument_list|<
name|?
argument_list|>
name|layout
parameter_list|)
block|{
name|super
argument_list|(
name|os
argument_list|,
name|streamName
argument_list|,
name|layout
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|stream
operator|=
name|os
expr_stmt|;
block|}
specifier|public
name|ByteArrayOutputStream
name|getStream
parameter_list|()
block|{
return|return
name|stream
return|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|stream
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

