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
name|history
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
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
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|QueryPlan
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
name|exec
operator|.
name|Task
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
operator|.
name|LogHelper
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
name|IOUtils
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
name|Counters
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
name|Counters
operator|.
name|Counter
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
name|Counters
operator|.
name|Group
import|;
end_import

begin_comment
comment|/**  * HiveHistory. Logs information such as query, query plan, runtime statistics  * into a file.  * Each session uses a new object, which creates a new file.  */
end_comment

begin_class
specifier|public
class|class
name|HiveHistoryImpl
implements|implements
name|HiveHistory
block|{
name|PrintWriter
name|histStream
decl_stmt|;
comment|// History File stream
name|String
name|histFileName
decl_stmt|;
comment|// History file name
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
literal|"hive.ql.exec.HiveHistoryImpl"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Random
name|randGen
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|private
name|LogHelper
name|console
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|idToTableMap
init|=
literal|null
decl_stmt|;
comment|// Job Hash Map
specifier|private
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|QueryInfo
argument_list|>
name|queryInfoMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|QueryInfo
argument_list|>
argument_list|()
decl_stmt|;
comment|// Task Hash Map
specifier|private
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|TaskInfo
argument_list|>
name|taskInfoMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|TaskInfo
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DELIMITER
init|=
literal|" "
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ROW_COUNT_PATTERN
init|=
literal|"RECORDS_OUT_(\\d+)(_)*(\\S+)*"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Pattern
name|rowCountPattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|ROW_COUNT_PATTERN
argument_list|)
decl_stmt|;
comment|/**    * Construct HiveHistoryImpl object and open history log file.    *    * @param ss    */
specifier|public
name|HiveHistoryImpl
parameter_list|(
name|SessionState
name|ss
parameter_list|)
block|{
try|try
block|{
name|console
operator|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|)
expr_stmt|;
name|String
name|conf_file_loc
init|=
name|ss
operator|.
name|getConf
argument_list|()
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHISTORYFILELOC
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|conf_file_loc
operator|==
literal|null
operator|)
operator|||
name|conf_file_loc
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"No history file location given"
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Create directory
name|File
name|histDir
init|=
operator|new
name|File
argument_list|(
name|conf_file_loc
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|histDir
operator|.
name|exists
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|histDir
operator|.
name|mkdirs
argument_list|()
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Unable to create log directory "
operator|+
name|conf_file_loc
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
do|do
block|{
name|histFileName
operator|=
name|conf_file_loc
operator|+
name|File
operator|.
name|separator
operator|+
literal|"hive_job_log_"
operator|+
name|ss
operator|.
name|getSessionId
argument_list|()
operator|+
literal|"_"
operator|+
name|Math
operator|.
name|abs
argument_list|(
name|randGen
operator|.
name|nextInt
argument_list|()
argument_list|)
operator|+
literal|".txt"
expr_stmt|;
block|}
do|while
condition|(
operator|!
operator|new
name|File
argument_list|(
name|histFileName
argument_list|)
operator|.
name|createNewFile
argument_list|()
condition|)
do|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Hive history file="
operator|+
name|histFileName
argument_list|)
expr_stmt|;
name|histStream
operator|=
operator|new
name|PrintWriter
argument_list|(
name|histFileName
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hm
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|hm
operator|.
name|put
argument_list|(
name|Keys
operator|.
name|SESSION_ID
operator|.
name|name
argument_list|()
argument_list|,
name|ss
operator|.
name|getSessionId
argument_list|()
argument_list|)
expr_stmt|;
name|log
argument_list|(
name|RecordTypes
operator|.
name|SessionStart
argument_list|,
name|hm
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"FAILED: Failed to open Query Log : "
operator|+
name|histFileName
operator|+
literal|" "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"\n"
operator|+
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getHistFileName
parameter_list|()
block|{
return|return
name|histFileName
return|;
block|}
comment|/**    * Write the a history record to history file.    *    * @param rt    * @param keyValMap    */
name|void
name|log
parameter_list|(
name|RecordTypes
name|rt
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|keyValMap
parameter_list|)
block|{
if|if
condition|(
name|histStream
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|rt
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ent
range|:
name|keyValMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|DELIMITER
argument_list|)
expr_stmt|;
name|String
name|key
init|=
name|ent
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|String
name|val
init|=
name|ent
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|val
operator|!=
literal|null
condition|)
block|{
name|val
operator|=
name|val
operator|.
name|replace
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"line.separator"
argument_list|)
argument_list|,
literal|" "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|key
operator|+
literal|"=\""
operator|+
name|val
operator|+
literal|"\""
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|DELIMITER
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Keys
operator|.
name|TIME
operator|.
name|name
argument_list|()
operator|+
literal|"=\""
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
literal|"\""
argument_list|)
expr_stmt|;
name|histStream
operator|.
name|println
argument_list|(
name|sb
argument_list|)
expr_stmt|;
name|histStream
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|startQuery
parameter_list|(
name|String
name|cmd
parameter_list|,
name|String
name|id
parameter_list|)
block|{
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|ss
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|QueryInfo
name|ji
init|=
operator|new
name|QueryInfo
argument_list|()
decl_stmt|;
name|ji
operator|.
name|hm
operator|.
name|put
argument_list|(
name|Keys
operator|.
name|QUERY_ID
operator|.
name|name
argument_list|()
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|ji
operator|.
name|hm
operator|.
name|put
argument_list|(
name|Keys
operator|.
name|QUERY_STRING
operator|.
name|name
argument_list|()
argument_list|,
name|cmd
argument_list|)
expr_stmt|;
name|queryInfoMap
operator|.
name|put
argument_list|(
name|id
argument_list|,
name|ji
argument_list|)
expr_stmt|;
name|log
argument_list|(
name|RecordTypes
operator|.
name|QueryStart
argument_list|,
name|ji
operator|.
name|hm
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setQueryProperty
parameter_list|(
name|String
name|queryId
parameter_list|,
name|Keys
name|propName
parameter_list|,
name|String
name|propValue
parameter_list|)
block|{
name|QueryInfo
name|ji
init|=
name|queryInfoMap
operator|.
name|get
argument_list|(
name|queryId
argument_list|)
decl_stmt|;
if|if
condition|(
name|ji
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|ji
operator|.
name|hm
operator|.
name|put
argument_list|(
name|propName
operator|.
name|name
argument_list|()
argument_list|,
name|propValue
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setTaskProperty
parameter_list|(
name|String
name|queryId
parameter_list|,
name|String
name|taskId
parameter_list|,
name|Keys
name|propName
parameter_list|,
name|String
name|propValue
parameter_list|)
block|{
name|String
name|id
init|=
name|queryId
operator|+
literal|":"
operator|+
name|taskId
decl_stmt|;
name|TaskInfo
name|ti
init|=
name|taskInfoMap
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|ti
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|ti
operator|.
name|hm
operator|.
name|put
argument_list|(
name|propName
operator|.
name|name
argument_list|()
argument_list|,
name|propValue
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setTaskCounters
parameter_list|(
name|String
name|queryId
parameter_list|,
name|String
name|taskId
parameter_list|,
name|Counters
name|ctrs
parameter_list|)
block|{
name|String
name|id
init|=
name|queryId
operator|+
literal|":"
operator|+
name|taskId
decl_stmt|;
name|QueryInfo
name|ji
init|=
name|queryInfoMap
operator|.
name|get
argument_list|(
name|queryId
argument_list|)
decl_stmt|;
name|StringBuilder
name|sb1
init|=
operator|new
name|StringBuilder
argument_list|(
literal|""
argument_list|)
decl_stmt|;
name|TaskInfo
name|ti
init|=
name|taskInfoMap
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|ti
operator|==
literal|null
operator|)
operator|||
operator|(
name|ctrs
operator|==
literal|null
operator|)
condition|)
block|{
return|return;
block|}
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|""
argument_list|)
decl_stmt|;
try|try
block|{
name|boolean
name|first
init|=
literal|true
decl_stmt|;
for|for
control|(
name|Group
name|group
range|:
name|ctrs
control|)
block|{
for|for
control|(
name|Counter
name|counter
range|:
name|group
control|)
block|{
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|group
operator|.
name|getDisplayName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'.'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|counter
operator|.
name|getDisplayName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|counter
operator|.
name|getCounter
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|tab
init|=
name|getRowCountTableName
argument_list|(
name|counter
operator|.
name|getDisplayName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|tab
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|sb1
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|sb1
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|sb1
operator|.
name|append
argument_list|(
name|tab
argument_list|)
expr_stmt|;
name|sb1
operator|.
name|append
argument_list|(
literal|'~'
argument_list|)
expr_stmt|;
name|sb1
operator|.
name|append
argument_list|(
name|counter
operator|.
name|getCounter
argument_list|()
argument_list|)
expr_stmt|;
name|ji
operator|.
name|rowCountMap
operator|.
name|put
argument_list|(
name|tab
argument_list|,
name|counter
operator|.
name|getCounter
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sb1
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|taskInfoMap
operator|.
name|get
argument_list|(
name|id
argument_list|)
operator|.
name|hm
operator|.
name|put
argument_list|(
name|Keys
operator|.
name|ROWS_INSERTED
operator|.
name|name
argument_list|()
argument_list|,
name|sb1
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|queryInfoMap
operator|.
name|get
argument_list|(
name|queryId
argument_list|)
operator|.
name|hm
operator|.
name|put
argument_list|(
name|Keys
operator|.
name|ROWS_INSERTED
operator|.
name|name
argument_list|()
argument_list|,
name|sb1
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|taskInfoMap
operator|.
name|get
argument_list|(
name|id
argument_list|)
operator|.
name|hm
operator|.
name|put
argument_list|(
name|Keys
operator|.
name|TASK_COUNTERS
operator|.
name|name
argument_list|()
argument_list|,
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|printRowCount
parameter_list|(
name|String
name|queryId
parameter_list|)
block|{
name|QueryInfo
name|ji
init|=
name|queryInfoMap
operator|.
name|get
argument_list|(
name|queryId
argument_list|)
decl_stmt|;
if|if
condition|(
name|ji
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|String
name|tab
range|:
name|ji
operator|.
name|rowCountMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
name|ji
operator|.
name|rowCountMap
operator|.
name|get
argument_list|(
name|tab
argument_list|)
operator|+
literal|" Rows loaded to "
operator|+
name|tab
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|endQuery
parameter_list|(
name|String
name|queryId
parameter_list|)
block|{
name|QueryInfo
name|ji
init|=
name|queryInfoMap
operator|.
name|get
argument_list|(
name|queryId
argument_list|)
decl_stmt|;
if|if
condition|(
name|ji
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|log
argument_list|(
name|RecordTypes
operator|.
name|QueryEnd
argument_list|,
name|ji
operator|.
name|hm
argument_list|)
expr_stmt|;
name|queryInfoMap
operator|.
name|remove
argument_list|(
name|queryId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|startTask
parameter_list|(
name|String
name|queryId
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|,
name|String
name|taskName
parameter_list|)
block|{
name|TaskInfo
name|ti
init|=
operator|new
name|TaskInfo
argument_list|()
decl_stmt|;
name|ti
operator|.
name|hm
operator|.
name|put
argument_list|(
name|Keys
operator|.
name|QUERY_ID
operator|.
name|name
argument_list|()
argument_list|,
name|queryId
argument_list|)
expr_stmt|;
name|ti
operator|.
name|hm
operator|.
name|put
argument_list|(
name|Keys
operator|.
name|TASK_ID
operator|.
name|name
argument_list|()
argument_list|,
name|task
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|ti
operator|.
name|hm
operator|.
name|put
argument_list|(
name|Keys
operator|.
name|TASK_NAME
operator|.
name|name
argument_list|()
argument_list|,
name|taskName
argument_list|)
expr_stmt|;
name|String
name|id
init|=
name|queryId
operator|+
literal|":"
operator|+
name|task
operator|.
name|getId
argument_list|()
decl_stmt|;
name|taskInfoMap
operator|.
name|put
argument_list|(
name|id
argument_list|,
name|ti
argument_list|)
expr_stmt|;
name|log
argument_list|(
name|RecordTypes
operator|.
name|TaskStart
argument_list|,
name|ti
operator|.
name|hm
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|endTask
parameter_list|(
name|String
name|queryId
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|)
block|{
name|String
name|id
init|=
name|queryId
operator|+
literal|":"
operator|+
name|task
operator|.
name|getId
argument_list|()
decl_stmt|;
name|TaskInfo
name|ti
init|=
name|taskInfoMap
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|ti
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|log
argument_list|(
name|RecordTypes
operator|.
name|TaskEnd
argument_list|,
name|ti
operator|.
name|hm
argument_list|)
expr_stmt|;
name|taskInfoMap
operator|.
name|remove
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|progressTask
parameter_list|(
name|String
name|queryId
parameter_list|,
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
parameter_list|)
block|{
name|String
name|id
init|=
name|queryId
operator|+
literal|":"
operator|+
name|task
operator|.
name|getId
argument_list|()
decl_stmt|;
name|TaskInfo
name|ti
init|=
name|taskInfoMap
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|ti
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|log
argument_list|(
name|RecordTypes
operator|.
name|TaskProgress
argument_list|,
name|ti
operator|.
name|hm
argument_list|)
expr_stmt|;
block|}
comment|/**    * write out counters.    */
specifier|static
name|ThreadLocal
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|ctrMapFactory
init|=
operator|new
name|ThreadLocal
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
return|;
block|}
block|}
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|logPlanProgress
parameter_list|(
name|QueryPlan
name|plan
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|plan
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ctrmap
init|=
name|ctrMapFactory
operator|.
name|get
argument_list|()
decl_stmt|;
name|ctrmap
operator|.
name|put
argument_list|(
literal|"plan"
argument_list|,
name|plan
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|log
argument_list|(
name|RecordTypes
operator|.
name|Counters
argument_list|,
name|ctrmap
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setIdToTableMap
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
parameter_list|)
block|{
name|idToTableMap
operator|=
name|map
expr_stmt|;
block|}
comment|/**    * Returns table name for the counter name.    *    * @param name    * @return tableName    */
name|String
name|getRowCountTableName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|idToTableMap
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Matcher
name|m
init|=
name|rowCountPattern
operator|.
name|matcher
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|.
name|find
argument_list|()
condition|)
block|{
name|String
name|tuple
init|=
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|String
name|tableName
init|=
name|m
operator|.
name|group
argument_list|(
literal|3
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableName
operator|!=
literal|null
condition|)
return|return
name|tableName
return|;
return|return
name|idToTableMap
operator|.
name|get
argument_list|(
name|tuple
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|closeStream
parameter_list|()
block|{
name|IOUtils
operator|.
name|closeStream
argument_list|(
name|histStream
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|finalize
parameter_list|()
throws|throws
name|Throwable
block|{
name|closeStream
argument_list|()
expr_stmt|;
name|super
operator|.
name|finalize
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

