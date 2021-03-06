begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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

begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache license, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License. You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the license for the specific language governing permissions and  * limitations under the license.  */
end_comment

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
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|cache
operator|.
name|CacheBuilder
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
name|cache
operator|.
name|CacheLoader
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
name|cache
operator|.
name|LoadingCache
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
name|RandomAccessFileManager
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
name|PluginConfiguration
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
name|PluginElement
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
name|net
operator|.
name|Advertiser
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
name|util
operator|.
name|Booleans
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
name|util
operator|.
name|Integers
import|;
end_import

begin_comment
comment|/**  * A File Appender that does not append log records after it has been stopped.  * Based on  * https://github.com/apache/logging-log4j2/blob/c02e7de69e81c174f1ea0be43639d9e231fa5283/log4j-core/src/main/java/org/apache/logging/log4j/core/appender/RandomAccessFileAppender.java  * (which is from log4j 2.6.2)  */
end_comment

begin_class
annotation|@
name|Plugin
argument_list|(
name|name
operator|=
literal|"HushableRandomAccessFile"
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
specifier|final
class|class
name|HushableRandomAccessFileAppender
extends|extends
name|AbstractOutputStreamAppender
argument_list|<
name|RandomAccessFileManager
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|LoadingCache
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|CLOSED_FILES
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|maximumSize
argument_list|(
literal|1000
argument_list|)
operator|.
name|expireAfterWrite
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|build
argument_list|(
operator|new
name|CacheLoader
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|String
name|load
parameter_list|(
name|String
name|key
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|key
return|;
block|}
block|}
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|String
name|fileName
decl_stmt|;
specifier|private
name|Object
name|advertisement
decl_stmt|;
specifier|private
specifier|final
name|Advertiser
name|advertiser
decl_stmt|;
specifier|private
name|HushableRandomAccessFileAppender
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|Layout
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|layout
parameter_list|,
specifier|final
name|Filter
name|filter
parameter_list|,
specifier|final
name|RandomAccessFileManager
name|manager
parameter_list|,
specifier|final
name|String
name|filename
parameter_list|,
specifier|final
name|boolean
name|ignoreExceptions
parameter_list|,
specifier|final
name|boolean
name|immediateFlush
parameter_list|,
specifier|final
name|Advertiser
name|advertiser
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
if|if
condition|(
name|advertiser
operator|!=
literal|null
condition|)
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|configuration
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|layout
operator|.
name|getContentFormat
argument_list|()
argument_list|)
decl_stmt|;
name|configuration
operator|.
name|putAll
argument_list|(
name|manager
operator|.
name|getContentFormat
argument_list|()
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|put
argument_list|(
literal|"contentType"
argument_list|,
name|layout
operator|.
name|getContentType
argument_list|()
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|advertisement
operator|=
name|advertiser
operator|.
name|advertise
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|fileName
operator|=
name|filename
expr_stmt|;
name|this
operator|.
name|advertiser
operator|=
name|advertiser
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|super
operator|.
name|stop
argument_list|()
expr_stmt|;
name|CLOSED_FILES
operator|.
name|put
argument_list|(
name|fileName
argument_list|,
name|fileName
argument_list|)
expr_stmt|;
if|if
condition|(
name|advertiser
operator|!=
literal|null
condition|)
block|{
name|advertiser
operator|.
name|unadvertise
argument_list|(
name|advertisement
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Write the log entry rolling over the file when required.    *    * @param event The LogEvent.    */
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
specifier|final
name|LogEvent
name|event
parameter_list|)
block|{
comment|// Unlike RandomAccessFileAppender, do not append log when stopped.
if|if
condition|(
name|isStopped
argument_list|()
condition|)
block|{
comment|// Don't try to log anything when appender is stopped
return|return;
block|}
comment|// Leverage the nice batching behaviour of async Loggers/Appenders:
comment|// we can signal the file manager that it needs to flush the buffer
comment|// to disk at the end of a batch.
comment|// From a user's point of view, this means that all log events are
comment|// _always_ available in the log file, without incurring the overhead
comment|// of immediateFlush=true.
name|getManager
argument_list|()
operator|.
name|setEndOfBatch
argument_list|(
name|event
operator|.
name|isEndOfBatch
argument_list|()
argument_list|)
expr_stmt|;
comment|// FIXME manager's EndOfBatch threadlocal can be deleted
comment|// LOG4J2-1292 utilize gc-free Layout.encode() method: taken care of in superclass
name|super
operator|.
name|append
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns the file name this appender is associated with.    *    * @return The File name.    */
specifier|public
name|String
name|getFileName
parameter_list|()
block|{
return|return
name|this
operator|.
name|fileName
return|;
block|}
comment|/**    * Returns the size of the file manager's buffer.    * @return the buffer size    */
specifier|public
name|int
name|getBufferSize
parameter_list|()
block|{
return|return
name|getManager
argument_list|()
operator|.
name|getBufferSize
argument_list|()
return|;
block|}
comment|// difference from standard File Appender:
comment|// locking is not supported and buffering cannot be switched off
comment|/**    * Create a File Appender.    *    * @param fileName The name and path of the file.    * @param append "True" if the file should be appended to, "false" if it    *            should be overwritten. The default is "true".    * @param name The name of the Appender.    * @param immediateFlush "true" if the contents should be flushed on every    *            write, "false" otherwise. The default is "true".    * @param bufferSizeStr The buffer size, defaults to {@value RandomAccessFileManager#DEFAULT_BUFFER_SIZE}.    * @param ignore If {@code "true"} (default) exceptions encountered when appending events are logged; otherwise    *               they are propagated to the caller.    * @param layout The layout to use to format the event. If no layout is    *            provided the default PatternLayout will be used.    * @param filter The filter, if any, to use.    * @param advertise "true" if the appender configuration should be    *            advertised, "false" otherwise.    * @param advertiseURI The advertised URI which can be used to retrieve the    *            file contents.    * @param config The Configuration.    * @return The FileAppender.    */
annotation|@
name|PluginFactory
specifier|public
specifier|static
name|HushableRandomAccessFileAppender
name|createAppender
parameter_list|(
annotation|@
name|PluginAttribute
argument_list|(
literal|"fileName"
argument_list|)
specifier|final
name|String
name|fileName
parameter_list|,
annotation|@
name|PluginAttribute
argument_list|(
literal|"append"
argument_list|)
specifier|final
name|String
name|append
parameter_list|,
annotation|@
name|PluginAttribute
argument_list|(
literal|"name"
argument_list|)
specifier|final
name|String
name|name
parameter_list|,
annotation|@
name|PluginAttribute
argument_list|(
literal|"immediateFlush"
argument_list|)
specifier|final
name|String
name|immediateFlush
parameter_list|,
annotation|@
name|PluginAttribute
argument_list|(
literal|"bufferSize"
argument_list|)
specifier|final
name|String
name|bufferSizeStr
parameter_list|,
annotation|@
name|PluginAttribute
argument_list|(
literal|"ignoreExceptions"
argument_list|)
specifier|final
name|String
name|ignore
parameter_list|,
annotation|@
name|PluginElement
argument_list|(
literal|"Layout"
argument_list|)
name|Layout
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|layout
parameter_list|,
annotation|@
name|PluginElement
argument_list|(
literal|"Filter"
argument_list|)
specifier|final
name|Filter
name|filter
parameter_list|,
annotation|@
name|PluginAttribute
argument_list|(
literal|"advertise"
argument_list|)
specifier|final
name|String
name|advertise
parameter_list|,
annotation|@
name|PluginAttribute
argument_list|(
literal|"advertiseURI"
argument_list|)
specifier|final
name|String
name|advertiseURI
parameter_list|,
annotation|@
name|PluginConfiguration
specifier|final
name|Configuration
name|config
parameter_list|)
block|{
specifier|final
name|boolean
name|isAppend
init|=
name|Booleans
operator|.
name|parseBoolean
argument_list|(
name|append
argument_list|,
literal|true
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|isFlush
init|=
name|Booleans
operator|.
name|parseBoolean
argument_list|(
name|immediateFlush
argument_list|,
literal|true
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|ignoreExceptions
init|=
name|Booleans
operator|.
name|parseBoolean
argument_list|(
name|ignore
argument_list|,
literal|true
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|isAdvertise
init|=
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|advertise
argument_list|)
decl_stmt|;
specifier|final
name|int
name|bufferSize
init|=
name|Integers
operator|.
name|parseInt
argument_list|(
name|bufferSizeStr
argument_list|,
literal|256
operator|*
literal|1024
comment|/* RandomAccessFileManager.DEFAULT_BUFFER_SIZE */
argument_list|)
decl_stmt|;
if|if
condition|(
name|name
operator|==
literal|null
condition|)
block|{
name|LOGGER
operator|.
name|error
argument_list|(
literal|"No name provided for FileAppender"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
if|if
condition|(
name|fileName
operator|==
literal|null
condition|)
block|{
name|LOGGER
operator|.
name|error
argument_list|(
literal|"No filename provided for FileAppender with name "
operator|+
name|name
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|/**      * In corner cases (e.g exceptions), there seem to be some race between      * com.lmax.disruptor.BatchEventProcessor and HS2 thread which is actually      * stopping the logs. Because of this, same filename is recreated and      * stop() would never be invoked on that instance, causing a mem leak.      * To prevent same file being recreated within very short time,      * CLOSED_FILES are tracked in cache with TTL of 1 second. This      * also helps in avoiding the stale directories created.      */
if|if
condition|(
name|CLOSED_FILES
operator|.
name|getIfPresent
argument_list|(
name|fileName
argument_list|)
operator|!=
literal|null
condition|)
block|{
comment|// Do not create another file, which got closed in last 5 seconds
name|LOGGER
operator|.
name|error
argument_list|(
name|fileName
operator|+
literal|" was closed recently."
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
if|if
condition|(
name|layout
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
specifier|final
name|RandomAccessFileManager
name|manager
init|=
name|RandomAccessFileManager
operator|.
name|getFileManager
argument_list|(
name|fileName
argument_list|,
name|isAppend
argument_list|,
name|isFlush
argument_list|,
name|bufferSize
argument_list|,
name|advertiseURI
argument_list|,
name|layout
argument_list|,
name|config
argument_list|)
decl_stmt|;
if|if
condition|(
name|manager
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|HushableRandomAccessFileAppender
argument_list|(
name|name
argument_list|,
name|layout
argument_list|,
name|filter
argument_list|,
name|manager
argument_list|,
name|fileName
argument_list|,
name|ignoreExceptions
argument_list|,
name|isFlush
argument_list|,
name|isAdvertise
condition|?
name|config
operator|.
name|getAdvertiser
argument_list|()
else|:
literal|null
argument_list|)
return|;
block|}
block|}
end_class

end_unit

