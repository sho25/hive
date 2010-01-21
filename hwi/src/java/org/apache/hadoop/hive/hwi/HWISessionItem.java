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
name|hwi
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
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
name|PrintStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Vector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|cli
operator|.
name|CliSessionState
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
name|cli
operator|.
name|OptionsProcessor
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
name|ql
operator|.
name|Driver
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
name|history
operator|.
name|HiveHistoryViewer
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
name|processors
operator|.
name|CommandProcessor
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
name|processors
operator|.
name|CommandProcessorFactory
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_comment
comment|/**  * HWISessionItem can be viewed as a wrapper for a Hive shell. With it the user  * has a session on the web server rather then in a console window.  *   */
end_comment

begin_class
specifier|public
class|class
name|HWISessionItem
implements|implements
name|Runnable
implements|,
name|Comparable
argument_list|<
name|HWISessionItem
argument_list|>
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|l4j
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HWISessionItem
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/** Represents the state a session item can be in. */
specifier|public
enum|enum
name|WebSessionItemStatus
block|{
name|NEW
block|,
name|READY
block|,
name|QUERY_SET
block|,
name|QUERY_RUNNING
block|,
name|DESTROY
block|,
name|KILL_QUERY
block|}
empty_stmt|;
comment|/** The Web Interface sessionName this is used to identify the session */
specifier|private
specifier|final
name|String
name|sessionName
decl_stmt|;
comment|/**    * Respresents the current status of the session. Used by components to    * determine state. Operations will throw exceptions if the item is not in the    * correct state.    */
specifier|private
name|HWISessionItem
operator|.
name|WebSessionItemStatus
name|status
decl_stmt|;
specifier|private
name|CliSessionState
name|ss
decl_stmt|;
comment|/** Standard out from the session will be written to this local file */
specifier|private
name|String
name|resultFile
decl_stmt|;
comment|/** Standard error from the session will be written to this local file */
specifier|private
name|String
name|errorFile
decl_stmt|;
comment|/**    * The results from the Driver. This is used for storing the most result    * results from the driver in memory    */
specifier|private
name|Vector
argument_list|<
name|Vector
argument_list|<
name|String
argument_list|>
argument_list|>
name|resultBucket
decl_stmt|;
comment|/** Limits the resultBucket to be no greater then this size */
specifier|private
name|int
name|resultBucketMaxSize
decl_stmt|;
comment|/** List of queries that this item should/has operated on */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|queries
decl_stmt|;
comment|/** status code results of queries */
specifier|private
name|List
argument_list|<
name|Integer
argument_list|>
name|queryRet
decl_stmt|;
comment|/** Reference to the configuration */
specifier|private
name|HiveConf
name|conf
decl_stmt|;
comment|/** User privileges */
specifier|private
name|HWIAuth
name|auth
decl_stmt|;
specifier|public
name|Thread
name|runnable
decl_stmt|;
comment|/**    * Threading SessionState issues require us to capture a reference to the hive    * history file and store it    */
specifier|private
name|String
name|historyFile
decl_stmt|;
comment|/**    * Creates an instance of WebSessionItem, sets status to NEW.    */
specifier|public
name|HWISessionItem
parameter_list|(
name|HWIAuth
name|auth
parameter_list|,
name|String
name|sessionName
parameter_list|)
block|{
name|this
operator|.
name|auth
operator|=
name|auth
expr_stmt|;
name|this
operator|.
name|sessionName
operator|=
name|sessionName
expr_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
literal|"HWISessionItem created"
argument_list|)
expr_stmt|;
name|status
operator|=
name|WebSessionItemStatus
operator|.
name|NEW
expr_stmt|;
name|queries
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|queryRet
operator|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
name|resultBucket
operator|=
operator|new
name|Vector
argument_list|<
name|Vector
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|resultBucketMaxSize
operator|=
literal|1000
expr_stmt|;
name|runnable
operator|=
operator|new
name|Thread
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|runnable
operator|.
name|start
argument_list|()
expr_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
literal|"Wait for NEW->READY transition"
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|runnable
init|)
block|{
if|if
condition|(
name|status
operator|!=
name|WebSessionItemStatus
operator|.
name|READY
condition|)
block|{
try|try
block|{
name|runnable
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{         }
block|}
block|}
name|l4j
operator|.
name|debug
argument_list|(
literal|"NEW->READY transition complete"
argument_list|)
expr_stmt|;
block|}
comment|/**    * This is the initialization process that is carried out for each    * SessionItem. The goal is to emulate the startup of CLIDriver.    */
specifier|private
name|void
name|itemInit
parameter_list|()
block|{
name|l4j
operator|.
name|debug
argument_list|(
literal|"HWISessionItem itemInit start "
operator|+
name|getSessionName
argument_list|()
argument_list|)
expr_stmt|;
name|OptionsProcessor
name|oproc
init|=
operator|new
name|OptionsProcessor
argument_list|()
decl_stmt|;
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"hwi-args"
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|String
index|[]
name|parts
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"hwi-args"
argument_list|)
operator|.
name|split
argument_list|(
literal|"\\s+"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|oproc
operator|.
name|process_stage1
argument_list|(
name|parts
argument_list|)
condition|)
block|{       }
block|}
name|SessionState
operator|.
name|initHiveLog4j
argument_list|()
expr_stmt|;
name|conf
operator|=
operator|new
name|HiveConf
argument_list|(
name|SessionState
operator|.
name|class
argument_list|)
expr_stmt|;
name|ss
operator|=
operator|new
name|CliSessionState
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|ss
argument_list|)
expr_stmt|;
name|queries
operator|.
name|add
argument_list|(
literal|"set hadoop.job.ugi="
operator|+
name|auth
operator|.
name|getUser
argument_list|()
operator|+
literal|","
operator|+
name|auth
operator|.
name|getGroups
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|queries
operator|.
name|add
argument_list|(
literal|"set user.name="
operator|+
name|auth
operator|.
name|getUser
argument_list|()
argument_list|)
expr_stmt|;
comment|/*      * HiveHistoryFileName will not be accessible outside this thread. We must      * capture this now.      */
name|historyFile
operator|=
name|ss
operator|.
name|get
argument_list|()
operator|.
name|getHiveHistory
argument_list|()
operator|.
name|getHistFileName
argument_list|()
expr_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
literal|"HWISessionItem itemInit Complete "
operator|+
name|getSessionName
argument_list|()
argument_list|)
expr_stmt|;
name|status
operator|=
name|WebSessionItemStatus
operator|.
name|READY
expr_stmt|;
synchronized|synchronized
init|(
name|runnable
init|)
block|{
name|runnable
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * HWISessionItem is a Runnable instance. Calling this method will change the    * status to QUERY_SET and notify(). The run method detects this and then    * continues processing.    */
specifier|public
name|void
name|clientStart
parameter_list|()
throws|throws
name|HWIException
block|{
if|if
condition|(
name|status
operator|==
name|WebSessionItemStatus
operator|.
name|QUERY_RUNNING
condition|)
block|{
throw|throw
operator|new
name|HWIException
argument_list|(
literal|"Query already running"
argument_list|)
throw|;
block|}
name|status
operator|=
name|WebSessionItemStatus
operator|.
name|QUERY_SET
expr_stmt|;
synchronized|synchronized
init|(
name|runnable
init|)
block|{
name|runnable
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
name|l4j
operator|.
name|debug
argument_list|(
name|getSessionName
argument_list|()
operator|+
literal|" Query is set to start"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|clientKill
parameter_list|()
throws|throws
name|HWIException
block|{
if|if
condition|(
name|status
operator|!=
name|WebSessionItemStatus
operator|.
name|QUERY_RUNNING
condition|)
block|{
throw|throw
operator|new
name|HWIException
argument_list|(
literal|"Can not kill that which is not running."
argument_list|)
throw|;
block|}
name|status
operator|=
name|WebSessionItemStatus
operator|.
name|KILL_QUERY
expr_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
name|getSessionName
argument_list|()
operator|+
literal|" Query is set to KILL_QUERY"
argument_list|)
expr_stmt|;
block|}
comment|/** This method clears the private member variables. */
specifier|public
name|void
name|clientRenew
parameter_list|()
throws|throws
name|HWIException
block|{
name|throwIfRunning
argument_list|()
expr_stmt|;
name|queries
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|queryRet
operator|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
name|resultBucket
operator|=
operator|new
name|Vector
argument_list|<
name|Vector
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|resultFile
operator|=
literal|null
expr_stmt|;
name|errorFile
operator|=
literal|null
expr_stmt|;
comment|// this.conf = null;
comment|// this.ss = null;
name|status
operator|=
name|WebSessionItemStatus
operator|.
name|NEW
expr_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
name|getSessionName
argument_list|()
operator|+
literal|" Query is renewed to start"
argument_list|)
expr_stmt|;
block|}
comment|/**    * This is a callback style function used by the HiveSessionManager. The    * HiveSessionManager notices this and attempts to stop the query.    */
specifier|protected
name|void
name|killIt
parameter_list|()
block|{
name|l4j
operator|.
name|debug
argument_list|(
name|getSessionName
argument_list|()
operator|+
literal|" Attempting kill."
argument_list|)
expr_stmt|;
if|if
condition|(
name|runnable
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|runnable
operator|.
name|join
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
name|getSessionName
argument_list|()
operator|+
literal|" Thread join complete"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|l4j
operator|.
name|error
argument_list|(
name|getSessionName
argument_list|()
operator|+
literal|" killing session caused exception "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Helper function to get configuration variables    *     * @param wanted    *          a ConfVar    * @return Value of the configuration variable.    */
specifier|public
name|String
name|getHiveConfVar
parameter_list|(
name|HiveConf
operator|.
name|ConfVars
name|wanted
parameter_list|)
throws|throws
name|HWIException
block|{
name|String
name|result
init|=
literal|null
decl_stmt|;
try|try
block|{
name|result
operator|=
name|ss
operator|.
name|getConf
argument_list|()
operator|.
name|getVar
argument_list|(
name|wanted
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|HWIException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|String
name|getHiveConfVar
parameter_list|(
name|String
name|s
parameter_list|)
throws|throws
name|HWIException
block|{
name|String
name|result
init|=
literal|null
decl_stmt|;
try|try
block|{
name|result
operator|=
name|conf
operator|.
name|get
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|HWIException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
comment|/*    * mapred.job.tracker could be host:port or just local    * mapred.job.tracker.http.address could be host:port or just host In some    * configurations http.address is set to 0.0.0.0 we are combining the two    * variables to provide a url to the job tracker WUI if it exists. If hadoop    * chose the first available port for the JobTracker HTTP port will can not    * determine it.    */
specifier|public
name|String
name|getJobTrackerURL
parameter_list|(
name|String
name|jobid
parameter_list|)
throws|throws
name|HWIException
block|{
name|String
name|jt
init|=
name|this
operator|.
name|getHiveConfVar
argument_list|(
literal|"mapred.job.tracker"
argument_list|)
decl_stmt|;
name|String
name|jth
init|=
name|this
operator|.
name|getHiveConfVar
argument_list|(
literal|"mapred.job.tracker.http.address"
argument_list|)
decl_stmt|;
name|String
index|[]
name|jtparts
init|=
literal|null
decl_stmt|;
name|String
index|[]
name|jthttpParts
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|jt
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"local"
argument_list|)
condition|)
block|{
name|jtparts
operator|=
operator|new
name|String
index|[
literal|2
index|]
expr_stmt|;
name|jtparts
index|[
literal|0
index|]
operator|=
literal|"local"
expr_stmt|;
name|jtparts
index|[
literal|1
index|]
operator|=
literal|""
expr_stmt|;
block|}
else|else
block|{
name|jtparts
operator|=
name|jt
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|jth
operator|.
name|contains
argument_list|(
literal|":"
argument_list|)
condition|)
block|{
name|jthttpParts
operator|=
name|jth
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|jthttpParts
operator|=
operator|new
name|String
index|[
literal|2
index|]
expr_stmt|;
name|jthttpParts
index|[
literal|0
index|]
operator|=
name|jth
expr_stmt|;
name|jthttpParts
index|[
literal|1
index|]
operator|=
literal|""
expr_stmt|;
block|}
return|return
name|jtparts
index|[
literal|0
index|]
operator|+
literal|":"
operator|+
name|jthttpParts
index|[
literal|1
index|]
operator|+
literal|"/jobdetails.jsp?jobid="
operator|+
name|jobid
operator|+
literal|"&refresh=30"
return|;
block|}
annotation|@
name|Override
comment|/*    * HWISessionItem uses a wait() notify() system. If the thread detects conf to    * be null, control is transfered to initItem(). A status of QUERY_SET causes    * control to transfer to the runQuery() method. DESTROY will cause the run    * loop to end permanently.    */
specifier|public
name|void
name|run
parameter_list|()
block|{
synchronized|synchronized
init|(
name|runnable
init|)
block|{
while|while
condition|(
name|status
operator|!=
name|HWISessionItem
operator|.
name|WebSessionItemStatus
operator|.
name|DESTROY
condition|)
block|{
if|if
condition|(
name|status
operator|==
name|WebSessionItemStatus
operator|.
name|NEW
condition|)
block|{
name|itemInit
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|status
operator|==
name|WebSessionItemStatus
operator|.
name|QUERY_SET
condition|)
block|{
name|runQuery
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|runnable
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|l4j
operator|.
name|error
argument_list|(
literal|"in wait() state "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|// end while
block|}
comment|// end sync
block|}
comment|// end run
comment|/**    * runQuery iterates the list of queries executing each query.    */
specifier|public
name|void
name|runQuery
parameter_list|()
block|{
name|FileOutputStream
name|fos
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|getResultFile
argument_list|()
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|fos
operator|=
operator|new
name|FileOutputStream
argument_list|(
operator|new
name|File
argument_list|(
name|resultFile
argument_list|)
argument_list|)
expr_stmt|;
name|ss
operator|.
name|out
operator|=
operator|new
name|PrintStream
argument_list|(
name|fos
argument_list|,
literal|true
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
name|fex
parameter_list|)
block|{
name|l4j
operator|.
name|error
argument_list|(
name|getSessionName
argument_list|()
operator|+
literal|" opening resultfile "
operator|+
name|resultFile
argument_list|,
name|fex
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
name|uex
parameter_list|)
block|{
name|l4j
operator|.
name|error
argument_list|(
name|getSessionName
argument_list|()
operator|+
literal|" opening resultfile "
operator|+
name|resultFile
argument_list|,
name|uex
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|l4j
operator|.
name|debug
argument_list|(
name|getSessionName
argument_list|()
operator|+
literal|" Output file was not specified"
argument_list|)
expr_stmt|;
block|}
name|l4j
operator|.
name|debug
argument_list|(
name|getSessionName
argument_list|()
operator|+
literal|" state is now QUERY_RUNNING."
argument_list|)
expr_stmt|;
name|status
operator|=
name|WebSessionItemStatus
operator|.
name|QUERY_RUNNING
expr_stmt|;
comment|// expect one return per query
name|queryRet
operator|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
name|queries
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|queries
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|cmd
init|=
name|queries
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|String
name|cmd_trimmed
init|=
name|cmd
operator|.
name|trim
argument_list|()
decl_stmt|;
name|String
index|[]
name|tokens
init|=
name|cmd_trimmed
operator|.
name|split
argument_list|(
literal|"\\s+"
argument_list|)
decl_stmt|;
name|String
name|cmd_1
init|=
name|cmd_trimmed
operator|.
name|substring
argument_list|(
name|tokens
index|[
literal|0
index|]
operator|.
name|length
argument_list|()
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
name|CommandProcessor
name|proc
init|=
name|CommandProcessorFactory
operator|.
name|get
argument_list|(
name|tokens
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|proc
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|proc
operator|instanceof
name|Driver
condition|)
block|{
name|Driver
name|qp
init|=
operator|(
name|Driver
operator|)
name|proc
decl_stmt|;
name|queryRet
operator|.
name|add
argument_list|(
operator|new
name|Integer
argument_list|(
name|qp
operator|.
name|run
argument_list|(
name|cmd
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Vector
argument_list|<
name|String
argument_list|>
name|res
init|=
operator|new
name|Vector
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
while|while
condition|(
name|qp
operator|.
name|getResults
argument_list|(
name|res
argument_list|)
condition|)
block|{
name|resultBucket
operator|.
name|add
argument_list|(
name|res
argument_list|)
expr_stmt|;
if|if
condition|(
name|resultBucket
operator|.
name|size
argument_list|()
operator|>
name|resultBucketMaxSize
condition|)
block|{
name|resultBucket
operator|.
name|remove
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|row
range|:
name|res
control|)
block|{
if|if
condition|(
name|ss
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|ss
operator|.
name|out
operator|!=
literal|null
condition|)
block|{
name|ss
operator|.
name|out
operator|.
name|println
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"ss was null"
argument_list|)
throw|;
block|}
block|}
comment|// res.clear();
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|l4j
operator|.
name|error
argument_list|(
name|getSessionName
argument_list|()
operator|+
literal|" getting results "
operator|+
name|getResultFile
argument_list|()
operator|+
literal|" caused exception."
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
name|qp
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|queryRet
operator|.
name|add
argument_list|(
operator|new
name|Integer
argument_list|(
name|proc
operator|.
name|run
argument_list|(
name|cmd_1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// processor was null
name|l4j
operator|.
name|error
argument_list|(
name|getSessionName
argument_list|()
operator|+
literal|" query processor was not found for query "
operator|+
name|cmd
argument_list|)
expr_stmt|;
block|}
block|}
comment|// end for
comment|// cleanup
try|try
block|{
if|if
condition|(
name|fos
operator|!=
literal|null
condition|)
block|{
name|fos
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|l4j
operator|.
name|error
argument_list|(
name|getSessionName
argument_list|()
operator|+
literal|" closing result file "
operator|+
name|getResultFile
argument_list|()
operator|+
literal|" caused exception."
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
name|status
operator|=
name|WebSessionItemStatus
operator|.
name|READY
expr_stmt|;
name|l4j
operator|.
name|debug
argument_list|(
name|getSessionName
argument_list|()
operator|+
literal|" state is now READY"
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|runnable
init|)
block|{
name|runnable
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * This is a chained call to SessionState.setIsSilent(). Use this if you do    * not want the result file to have information status    */
specifier|public
name|void
name|setSSIsSilent
parameter_list|(
name|boolean
name|silent
parameter_list|)
throws|throws
name|HWIException
block|{
if|if
condition|(
name|ss
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HWIException
argument_list|(
literal|"Session State is null"
argument_list|)
throw|;
block|}
name|ss
operator|.
name|setIsSilent
argument_list|(
name|silent
argument_list|)
expr_stmt|;
block|}
comment|/**    * This is a chained call to SessionState.getIsSilent()    */
specifier|public
name|boolean
name|getSSIsSilent
parameter_list|()
throws|throws
name|HWIException
block|{
if|if
condition|(
name|ss
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HWIException
argument_list|(
literal|"Session State is null"
argument_list|)
throw|;
block|}
return|return
name|ss
operator|.
name|getIsSilent
argument_list|()
return|;
block|}
comment|/** to support sorting/Set */
specifier|public
name|int
name|compareTo
parameter_list|(
name|HWISessionItem
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
name|getSessionName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|getSessionName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    *     * @return the HiveHistoryViewer for the session    * @throws HWIException    */
specifier|public
name|HiveHistoryViewer
name|getHistoryViewer
parameter_list|()
throws|throws
name|HWIException
block|{
if|if
condition|(
name|ss
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HWIException
argument_list|(
literal|"Session state was null"
argument_list|)
throw|;
block|}
comment|/*      * we can not call this.ss.get().getHiveHistory().getHistFileName() directly      * as this call is made from a a Jetty thread and will return null      */
name|HiveHistoryViewer
name|hv
init|=
operator|new
name|HiveHistoryViewer
argument_list|(
name|historyFile
argument_list|)
decl_stmt|;
return|return
name|hv
return|;
block|}
comment|/**    * Uses the sessionName property to compare to sessions    *     * @return true if sessionNames are equal false otherwise    */
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|other
operator|instanceof
name|HWISessionItem
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|HWISessionItem
name|o
init|=
operator|(
name|HWISessionItem
operator|)
name|other
decl_stmt|;
if|if
condition|(
name|getSessionName
argument_list|()
operator|.
name|equals
argument_list|(
name|o
operator|.
name|getSessionName
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
specifier|public
name|String
name|getResultFile
parameter_list|()
block|{
return|return
name|resultFile
return|;
block|}
specifier|public
name|void
name|setResultFile
parameter_list|(
name|String
name|resultFile
parameter_list|)
block|{
name|this
operator|.
name|resultFile
operator|=
name|resultFile
expr_stmt|;
block|}
comment|/**    * The session name is an identifier to recognize the session    *     * @return the session's name    */
specifier|public
name|String
name|getSessionName
parameter_list|()
block|{
return|return
name|sessionName
return|;
block|}
comment|/**    * Used to represent to the user and other components what state the    * HWISessionItem is in. Certain commands can only be run when the application    * is in certain states.    *     * @return the current status of the session    */
specifier|public
name|WebSessionItemStatus
name|getStatus
parameter_list|()
block|{
return|return
name|status
return|;
block|}
comment|/**    * Currently unused    *     * @return a String with the full path to the error file.    */
specifier|public
name|String
name|getErrorFile
parameter_list|()
block|{
return|return
name|errorFile
return|;
block|}
comment|/**    * Currently unused    *     * @param errorFile    *          the full path to the file for results.    */
specifier|public
name|void
name|setErrorFile
parameter_list|(
name|String
name|errorFile
parameter_list|)
block|{
name|this
operator|.
name|errorFile
operator|=
name|errorFile
expr_stmt|;
block|}
comment|/**    * @return the auth    */
specifier|public
name|HWIAuth
name|getAuth
parameter_list|()
block|{
return|return
name|auth
return|;
block|}
comment|/**    * @param auth    *          the auth to set    */
specifier|protected
name|void
name|setAuth
parameter_list|(
name|HWIAuth
name|auth
parameter_list|)
block|{
name|this
operator|.
name|auth
operator|=
name|auth
expr_stmt|;
block|}
comment|/** returns an unmodifiable list of queries */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getQueries
parameter_list|()
block|{
return|return
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|queries
argument_list|)
return|;
block|}
comment|/**    * adds a new query to the execution list    *     * @param query    *          query to be added to the list    */
specifier|public
name|void
name|addQuery
parameter_list|(
name|String
name|query
parameter_list|)
throws|throws
name|HWIException
block|{
name|throwIfRunning
argument_list|()
expr_stmt|;
name|queries
operator|.
name|add
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
comment|/**    * removes a query from the execution list    *     * @param item    *          the 0 based index of the item to be removed    */
specifier|public
name|void
name|removeQuery
parameter_list|(
name|int
name|item
parameter_list|)
throws|throws
name|HWIException
block|{
name|throwIfRunning
argument_list|()
expr_stmt|;
name|queries
operator|.
name|remove
argument_list|(
name|item
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|clearQueries
parameter_list|()
throws|throws
name|HWIException
block|{
name|throwIfRunning
argument_list|()
expr_stmt|;
name|queries
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|/** returns the value for resultBucketMaxSize */
specifier|public
name|int
name|getResultBucketMaxSize
parameter_list|()
block|{
return|return
name|resultBucketMaxSize
return|;
block|}
comment|/**    * sets the value for resultBucketMaxSize    *     * @param size    *          the new size    */
specifier|public
name|void
name|setResultBucketMaxSize
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|resultBucketMaxSize
operator|=
name|size
expr_stmt|;
block|}
comment|/** gets the value for resultBucket */
specifier|public
name|Vector
argument_list|<
name|Vector
argument_list|<
name|String
argument_list|>
argument_list|>
name|getResultBucket
parameter_list|()
block|{
return|return
name|resultBucket
return|;
block|}
comment|/**    * The HWISessionItem stores the result of each query in an array    *     * @return unmodifiable list of return codes    */
specifier|public
name|List
argument_list|<
name|Integer
argument_list|>
name|getQueryRet
parameter_list|()
block|{
return|return
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|queryRet
argument_list|)
return|;
block|}
comment|/**    * If the ItemStatus is QueryRunning most of the configuration is in a read    * only state.    */
specifier|private
name|void
name|throwIfRunning
parameter_list|()
throws|throws
name|HWIException
block|{
if|if
condition|(
name|status
operator|==
name|WebSessionItemStatus
operator|.
name|QUERY_RUNNING
condition|)
block|{
throw|throw
operator|new
name|HWIException
argument_list|(
literal|"Query already running"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

