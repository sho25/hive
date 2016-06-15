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
name|llap
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|EOFException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
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
name|LinkedBlockingQueue
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
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
name|hadoop
operator|.
name|hive
operator|.
name|conf
operator|.
name|HiveConf
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
name|llap
operator|.
name|Schema
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
name|io
operator|.
name|Writable
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
name|io
operator|.
name|WritableComparable
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
name|io
operator|.
name|NullWritable
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
name|util
operator|.
name|ReflectionUtils
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
name|mapred
operator|.
name|RecordReader
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
name|mapred
operator|.
name|Reporter
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Base LLAP RecordReader to handle receiving of the data from the LLAP daemon.  */
end_comment

begin_class
specifier|public
class|class
name|LlapBaseRecordReader
parameter_list|<
name|V
extends|extends
name|WritableComparable
parameter_list|>
implements|implements
name|RecordReader
argument_list|<
name|NullWritable
argument_list|,
name|V
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|LlapBaseRecordReader
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|DataInputStream
name|din
decl_stmt|;
specifier|protected
specifier|final
name|Schema
name|schema
decl_stmt|;
specifier|protected
specifier|final
name|Class
argument_list|<
name|V
argument_list|>
name|clazz
decl_stmt|;
specifier|protected
name|Thread
name|readerThread
init|=
literal|null
decl_stmt|;
specifier|protected
specifier|final
name|LinkedBlockingQueue
argument_list|<
name|ReaderEvent
argument_list|>
name|readerEvents
init|=
operator|new
name|LinkedBlockingQueue
argument_list|<
name|ReaderEvent
argument_list|>
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|long
name|timeout
decl_stmt|;
specifier|protected
specifier|final
name|Closeable
name|client
decl_stmt|;
specifier|public
name|LlapBaseRecordReader
parameter_list|(
name|InputStream
name|in
parameter_list|,
name|Schema
name|schema
parameter_list|,
name|Class
argument_list|<
name|V
argument_list|>
name|clazz
parameter_list|,
name|JobConf
name|job
parameter_list|,
name|Closeable
name|client
parameter_list|)
block|{
name|din
operator|=
operator|new
name|DataInputStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|schema
operator|=
name|schema
expr_stmt|;
name|this
operator|.
name|clazz
operator|=
name|clazz
expr_stmt|;
name|this
operator|.
name|readerThread
operator|=
name|Thread
operator|.
name|currentThread
argument_list|()
expr_stmt|;
name|this
operator|.
name|timeout
operator|=
literal|3
operator|*
name|HiveConf
operator|.
name|getTimeVar
argument_list|(
name|job
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LLAP_DAEMON_AM_LIVENESS_CONNECTION_TIMEOUT_MS
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
block|}
specifier|public
name|Schema
name|getSchema
parameter_list|()
block|{
return|return
name|schema
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|Exception
name|caughtException
init|=
literal|null
decl_stmt|;
try|try
block|{
name|din
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error closing input stream:"
operator|+
name|err
operator|.
name|getMessage
argument_list|()
argument_list|,
name|err
argument_list|)
expr_stmt|;
name|caughtException
operator|=
name|err
expr_stmt|;
block|}
if|if
condition|(
name|client
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error closing client:"
operator|+
name|err
operator|.
name|getMessage
argument_list|()
argument_list|,
name|err
argument_list|)
expr_stmt|;
name|caughtException
operator|=
operator|(
name|caughtException
operator|==
literal|null
condition|?
name|err
else|:
name|caughtException
operator|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|caughtException
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Exception during close: "
operator|+
name|caughtException
operator|.
name|getMessage
argument_list|()
argument_list|,
name|caughtException
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getPos
parameter_list|()
block|{
comment|// dummy impl
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|getProgress
parameter_list|()
block|{
comment|// dummy impl
return|return
literal|0f
return|;
block|}
annotation|@
name|Override
specifier|public
name|NullWritable
name|createKey
parameter_list|()
block|{
return|return
name|NullWritable
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|V
name|createValue
parameter_list|()
block|{
try|try
block|{
return|return
name|clazz
operator|.
name|newInstance
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|NullWritable
name|key
parameter_list|,
name|V
name|value
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
comment|// Need a way to know what thread to interrupt, since this is a blocking thread.
name|setReaderThread
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
argument_list|)
expr_stmt|;
name|value
operator|.
name|readFields
argument_list|(
name|din
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|EOFException
name|eof
parameter_list|)
block|{
comment|// End of input. There should be a reader event available, or coming soon, so okay to be blocking call.
name|ReaderEvent
name|event
init|=
name|getReaderEvent
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|event
operator|.
name|getEventType
argument_list|()
condition|)
block|{
case|case
name|DONE
case|:
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Expected reader event with done status, but got "
operator|+
name|event
operator|.
name|getEventType
argument_list|()
operator|+
literal|" with message "
operator|+
name|event
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
return|return
literal|false
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|io
parameter_list|)
block|{
if|if
condition|(
name|Thread
operator|.
name|interrupted
argument_list|()
condition|)
block|{
comment|// Either we were interrupted by one of:
comment|// 1. handleEvent(), in which case there is a reader (error) event waiting for us in the queue
comment|// 2. Some other unrelated cause which interrupted us, in which case there may not be a reader event coming.
comment|// Either way we should not try to block trying to read the reader events queue.
if|if
condition|(
name|readerEvents
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// Case 2.
throw|throw
name|io
throw|;
block|}
else|else
block|{
comment|// Case 1. Fail the reader, sending back the error we received from the reader event.
name|ReaderEvent
name|event
init|=
name|getReaderEvent
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|event
operator|.
name|getEventType
argument_list|()
condition|)
block|{
case|case
name|ERROR
case|:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Received reader event error: "
operator|+
name|event
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Got reader event type "
operator|+
name|event
operator|.
name|getEventType
argument_list|()
operator|+
literal|", expected error event"
argument_list|)
throw|;
block|}
block|}
block|}
else|else
block|{
comment|// If we weren't interrupted, just propagate the error
throw|throw
name|io
throw|;
block|}
block|}
block|}
comment|/**    * Define success/error events which are passed to the reader from a different thread.    * The reader will check for these events on end of input and interruption of the reader thread.    */
specifier|public
specifier|static
class|class
name|ReaderEvent
block|{
specifier|public
enum|enum
name|EventType
block|{
name|DONE
block|,
name|ERROR
block|}
specifier|protected
specifier|final
name|EventType
name|eventType
decl_stmt|;
specifier|protected
specifier|final
name|String
name|message
decl_stmt|;
specifier|protected
name|ReaderEvent
parameter_list|(
name|EventType
name|type
parameter_list|,
name|String
name|message
parameter_list|)
block|{
name|this
operator|.
name|eventType
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|message
operator|=
name|message
expr_stmt|;
block|}
specifier|public
specifier|static
name|ReaderEvent
name|doneEvent
parameter_list|()
block|{
return|return
operator|new
name|ReaderEvent
argument_list|(
name|EventType
operator|.
name|DONE
argument_list|,
literal|""
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ReaderEvent
name|errorEvent
parameter_list|(
name|String
name|message
parameter_list|)
block|{
return|return
operator|new
name|ReaderEvent
argument_list|(
name|EventType
operator|.
name|ERROR
argument_list|,
name|message
argument_list|)
return|;
block|}
specifier|public
name|EventType
name|getEventType
parameter_list|()
block|{
return|return
name|eventType
return|;
block|}
specifier|public
name|String
name|getMessage
parameter_list|()
block|{
return|return
name|message
return|;
block|}
block|}
specifier|public
name|void
name|handleEvent
parameter_list|(
name|ReaderEvent
name|event
parameter_list|)
block|{
switch|switch
condition|(
name|event
operator|.
name|getEventType
argument_list|()
condition|)
block|{
case|case
name|DONE
case|:
comment|// Reader will check for the event queue upon the end of the input stream - no need to interrupt.
name|readerEvents
operator|.
name|add
argument_list|(
name|event
argument_list|)
expr_stmt|;
break|break;
case|case
name|ERROR
case|:
name|readerEvents
operator|.
name|add
argument_list|(
name|event
argument_list|)
expr_stmt|;
if|if
condition|(
name|readerThread
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Reader thread is unexpectedly null, during ReaderEvent error "
operator|+
name|event
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
comment|// Reader is using a blocking socket .. interrupt it.
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Interrupting reader thread due to reader event with error "
operator|+
name|event
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|getReaderThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unhandled ReaderEvent type "
operator|+
name|event
operator|.
name|getEventType
argument_list|()
operator|+
literal|" with message "
operator|+
name|event
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|ReaderEvent
name|getReaderEvent
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|ReaderEvent
name|event
init|=
name|readerEvents
operator|.
name|poll
argument_list|(
name|timeout
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
if|if
condition|(
name|event
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Timed out getting readerEvents"
argument_list|)
throw|;
block|}
return|return
name|event
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Interrupted while getting readerEvents, not expected: "
operator|+
name|ie
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ie
argument_list|)
throw|;
block|}
block|}
specifier|protected
specifier|synchronized
name|void
name|setReaderThread
parameter_list|(
name|Thread
name|readerThread
parameter_list|)
block|{
name|this
operator|.
name|readerThread
operator|=
name|readerThread
expr_stmt|;
block|}
specifier|protected
specifier|synchronized
name|Thread
name|getReaderThread
parameter_list|()
block|{
return|return
name|readerThread
return|;
block|}
block|}
end_class

end_unit

