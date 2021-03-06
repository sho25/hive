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
name|hive
operator|.
name|http
package|;
end_package

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
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ThreadInfo
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ThreadMXBean
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|ServletException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServlet
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletRequest
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletResponse
import|;
end_import

begin_comment
comment|/**  * A servlet to print out the current stack traces.  */
end_comment

begin_class
specifier|public
class|class
name|StackServlet
extends|extends
name|HttpServlet
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
name|ThreadMXBean
name|threadBean
init|=
name|ManagementFactory
operator|.
name|getThreadMXBean
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|doGet
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|)
throws|throws
name|ServletException
throws|,
name|IOException
block|{
if|if
condition|(
operator|!
name|HttpServer
operator|.
name|isInstrumentationAccessAllowed
argument_list|(
name|getServletContext
argument_list|()
argument_list|,
name|request
argument_list|,
name|response
argument_list|)
condition|)
block|{
return|return;
block|}
name|response
operator|.
name|setContentType
argument_list|(
literal|"text/plain; charset=UTF-8"
argument_list|)
expr_stmt|;
try|try
init|(
name|PrintStream
name|out
init|=
operator|new
name|PrintStream
argument_list|(
name|response
operator|.
name|getOutputStream
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|"UTF-8"
argument_list|)
init|)
block|{
name|printThreadInfo
argument_list|(
name|out
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Print all of the thread's information and stack traces.    *    * @param stream the stream to    * @param title a string title for the stack trace    */
specifier|private
specifier|synchronized
name|void
name|printThreadInfo
parameter_list|(
name|PrintStream
name|stream
parameter_list|,
name|String
name|title
parameter_list|)
block|{
specifier|final
name|int
name|STACK_DEPTH
init|=
literal|20
decl_stmt|;
name|boolean
name|contention
init|=
name|threadBean
operator|.
name|isThreadContentionMonitoringEnabled
argument_list|()
decl_stmt|;
name|long
index|[]
name|threadIds
init|=
name|threadBean
operator|.
name|getAllThreadIds
argument_list|()
decl_stmt|;
name|stream
operator|.
name|println
argument_list|(
literal|"Process Thread Dump: "
operator|+
name|title
argument_list|)
expr_stmt|;
name|stream
operator|.
name|println
argument_list|(
name|threadIds
operator|.
name|length
operator|+
literal|" active threads"
argument_list|)
expr_stmt|;
for|for
control|(
name|long
name|tid
range|:
name|threadIds
control|)
block|{
name|ThreadInfo
name|info
init|=
name|threadBean
operator|.
name|getThreadInfo
argument_list|(
name|tid
argument_list|,
name|STACK_DEPTH
argument_list|)
decl_stmt|;
if|if
condition|(
name|info
operator|==
literal|null
condition|)
block|{
name|stream
operator|.
name|println
argument_list|(
literal|"  Inactive"
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|stream
operator|.
name|println
argument_list|(
literal|"Thread "
operator|+
name|getTaskName
argument_list|(
name|info
operator|.
name|getThreadId
argument_list|()
argument_list|,
name|info
operator|.
name|getThreadName
argument_list|()
argument_list|)
operator|+
literal|":"
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|State
name|state
init|=
name|info
operator|.
name|getThreadState
argument_list|()
decl_stmt|;
name|stream
operator|.
name|println
argument_list|(
literal|"  State: "
operator|+
name|state
argument_list|)
expr_stmt|;
name|stream
operator|.
name|println
argument_list|(
literal|"  Blocked count: "
operator|+
name|info
operator|.
name|getBlockedCount
argument_list|()
argument_list|)
expr_stmt|;
name|stream
operator|.
name|println
argument_list|(
literal|"  Waited count: "
operator|+
name|info
operator|.
name|getWaitedCount
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|contention
condition|)
block|{
name|stream
operator|.
name|println
argument_list|(
literal|"  Blocked time: "
operator|+
name|info
operator|.
name|getBlockedTime
argument_list|()
argument_list|)
expr_stmt|;
name|stream
operator|.
name|println
argument_list|(
literal|"  Waited time: "
operator|+
name|info
operator|.
name|getWaitedTime
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|state
operator|==
name|Thread
operator|.
name|State
operator|.
name|WAITING
condition|)
block|{
name|stream
operator|.
name|println
argument_list|(
literal|"  Waiting on "
operator|+
name|info
operator|.
name|getLockName
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|state
operator|==
name|Thread
operator|.
name|State
operator|.
name|BLOCKED
condition|)
block|{
name|stream
operator|.
name|println
argument_list|(
literal|"  Blocked on "
operator|+
name|info
operator|.
name|getLockName
argument_list|()
argument_list|)
expr_stmt|;
name|stream
operator|.
name|println
argument_list|(
literal|"  Blocked by "
operator|+
name|getTaskName
argument_list|(
name|info
operator|.
name|getLockOwnerId
argument_list|()
argument_list|,
name|info
operator|.
name|getLockOwnerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|stream
operator|.
name|println
argument_list|(
literal|"  Stack:"
argument_list|)
expr_stmt|;
for|for
control|(
name|StackTraceElement
name|frame
range|:
name|info
operator|.
name|getStackTrace
argument_list|()
control|)
block|{
name|stream
operator|.
name|println
argument_list|(
literal|"    "
operator|+
name|frame
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|stream
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
specifier|private
name|String
name|getTaskName
parameter_list|(
name|long
name|id
parameter_list|,
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|name
operator|==
literal|null
condition|)
block|{
return|return
name|Long
operator|.
name|toString
argument_list|(
name|id
argument_list|)
return|;
block|}
return|return
name|id
operator|+
literal|" ("
operator|+
name|name
operator|+
literal|")"
return|;
block|}
block|}
end_class

end_unit

