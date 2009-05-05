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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
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
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
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
name|ASTNode
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
name|lang
operator|.
name|StringUtils
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
name|MetaStoreUtils
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
name|api
operator|.
name|FieldSchema
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
name|ParseDriver
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
name|SemanticException
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
name|ParseException
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
name|BaseSemanticAnalyzer
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
name|SemanticAnalyzerFactory
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
name|exec
operator|.
name|FetchTask
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
name|TaskFactory
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
name|Utilities
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
name|hooks
operator|.
name|PreExecute
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
name|HiveHistory
operator|.
name|Keys
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
name|plan
operator|.
name|tableDesc
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
name|serde2
operator|.
name|ByteStream
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|security
operator|.
name|UserGroupInformation
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

begin_class
specifier|public
class|class
name|Driver
implements|implements
name|CommandProcessor
block|{
specifier|static
specifier|final
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"hive.ql.Driver"
argument_list|)
decl_stmt|;
specifier|private
name|int
name|maxRows
init|=
literal|100
decl_stmt|;
name|ByteStream
operator|.
name|Output
name|bos
init|=
operator|new
name|ByteStream
operator|.
name|Output
argument_list|()
decl_stmt|;
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|DataInput
name|resStream
decl_stmt|;
specifier|private
name|LogHelper
name|console
decl_stmt|;
specifier|private
name|Context
name|ctx
decl_stmt|;
specifier|private
name|QueryPlan
name|plan
decl_stmt|;
specifier|public
name|int
name|countJobs
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|tasks
parameter_list|)
block|{
return|return
name|countJobs
argument_list|(
name|tasks
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|int
name|countJobs
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|tasks
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|seenTasks
parameter_list|)
block|{
if|if
condition|(
name|tasks
operator|==
literal|null
condition|)
return|return
literal|0
return|;
name|int
name|jobs
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
range|:
name|tasks
control|)
block|{
if|if
condition|(
operator|!
name|seenTasks
operator|.
name|contains
argument_list|(
name|task
argument_list|)
condition|)
block|{
name|seenTasks
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
if|if
condition|(
name|task
operator|.
name|isMapRedTask
argument_list|()
condition|)
block|{
name|jobs
operator|++
expr_stmt|;
block|}
name|jobs
operator|+=
name|countJobs
argument_list|(
name|task
operator|.
name|getChildTasks
argument_list|()
argument_list|,
name|seenTasks
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|jobs
return|;
block|}
comment|/**    * Return the Thrift DDL string of the result    */
specifier|public
name|String
name|getSchema
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|plan
operator|!=
literal|null
operator|&&
name|plan
operator|.
name|getPlan
argument_list|()
operator|.
name|getFetchTask
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|BaseSemanticAnalyzer
name|sem
init|=
name|plan
operator|.
name|getPlan
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|sem
operator|.
name|getFetchTaskInit
argument_list|()
condition|)
block|{
name|sem
operator|.
name|setFetchTaskInit
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|sem
operator|.
name|getFetchTask
argument_list|()
operator|.
name|initialize
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|FetchTask
name|ft
init|=
operator|(
name|FetchTask
operator|)
name|sem
operator|.
name|getFetchTask
argument_list|()
decl_stmt|;
name|tableDesc
name|td
init|=
name|ft
operator|.
name|getTblDesc
argument_list|()
decl_stmt|;
name|String
name|tableName
init|=
literal|"result"
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|lst
init|=
name|MetaStoreUtils
operator|.
name|getFieldsFromDeserializer
argument_list|(
name|tableName
argument_list|,
name|td
operator|.
name|getDeserializer
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|schema
init|=
name|MetaStoreUtils
operator|.
name|getDDLFromFieldSchema
argument_list|(
name|tableName
argument_list|,
name|lst
argument_list|)
decl_stmt|;
return|return
name|schema
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Return the maximum number of rows returned by getResults    */
specifier|public
name|int
name|getMaxRows
parameter_list|()
block|{
return|return
name|maxRows
return|;
block|}
comment|/**    * Set the maximum number of rows returned by getResults    */
specifier|public
name|void
name|setMaxRows
parameter_list|(
name|int
name|maxRows
parameter_list|)
block|{
name|this
operator|.
name|maxRows
operator|=
name|maxRows
expr_stmt|;
block|}
specifier|public
name|boolean
name|hasReduceTasks
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|tasks
parameter_list|)
block|{
if|if
condition|(
name|tasks
operator|==
literal|null
condition|)
return|return
literal|false
return|;
name|boolean
name|hasReduce
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
range|:
name|tasks
control|)
block|{
if|if
condition|(
name|task
operator|.
name|hasReduce
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
name|hasReduce
operator|=
operator|(
name|hasReduce
operator|||
name|hasReduceTasks
argument_list|(
name|task
operator|.
name|getChildTasks
argument_list|()
argument_list|)
operator|)
expr_stmt|;
block|}
return|return
name|hasReduce
return|;
block|}
comment|/**    * for backwards compatibility with current tests    */
specifier|public
name|Driver
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|console
operator|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|ctx
operator|=
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Driver
parameter_list|()
block|{
name|console
operator|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|)
expr_stmt|;
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|conf
operator|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
expr_stmt|;
name|ctx
operator|=
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Compile a new query. Any currently-planned query associated with this Driver is discarded.    *    * @param command The SQL query to compile.    */
specifier|public
name|int
name|compile
parameter_list|(
name|String
name|command
parameter_list|)
block|{
if|if
condition|(
name|plan
operator|!=
literal|null
condition|)
block|{
name|close
argument_list|()
expr_stmt|;
name|plan
operator|=
literal|null
expr_stmt|;
block|}
name|TaskFactory
operator|.
name|resetId
argument_list|()
expr_stmt|;
try|try
block|{
name|ctx
operator|.
name|clear
argument_list|()
expr_stmt|;
name|ctx
operator|.
name|makeScratchDir
argument_list|()
expr_stmt|;
name|ParseDriver
name|pd
init|=
operator|new
name|ParseDriver
argument_list|()
decl_stmt|;
name|ASTNode
name|tree
init|=
name|pd
operator|.
name|parse
argument_list|(
name|command
argument_list|)
decl_stmt|;
while|while
condition|(
operator|(
name|tree
operator|.
name|getToken
argument_list|()
operator|==
literal|null
operator|)
operator|&&
operator|(
name|tree
operator|.
name|getChildCount
argument_list|()
operator|>
literal|0
operator|)
condition|)
block|{
name|tree
operator|=
operator|(
name|ASTNode
operator|)
name|tree
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|BaseSemanticAnalyzer
name|sem
init|=
name|SemanticAnalyzerFactory
operator|.
name|get
argument_list|(
name|conf
argument_list|,
name|tree
argument_list|)
decl_stmt|;
comment|// Do semantic analysis and plan generation
name|sem
operator|.
name|analyze
argument_list|(
name|tree
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Semantic Analysis Completed"
argument_list|)
expr_stmt|;
name|plan
operator|=
operator|new
name|QueryPlan
argument_list|(
name|command
argument_list|,
name|sem
argument_list|)
expr_stmt|;
return|return
operator|(
literal|0
operator|)
return|;
block|}
catch|catch
parameter_list|(
name|SemanticException
name|e
parameter_list|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"FAILED: Error in semantic analysis: "
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
return|return
operator|(
literal|10
operator|)
return|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"FAILED: Parse Error: "
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
return|return
operator|(
literal|11
operator|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"FAILED: Unknown exception : "
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
return|return
operator|(
literal|12
operator|)
return|;
block|}
block|}
comment|/**    * @return The current query plan associated with this Driver, if any.    */
specifier|public
name|QueryPlan
name|getPlan
parameter_list|()
block|{
return|return
name|plan
return|;
block|}
specifier|public
name|int
name|run
parameter_list|(
name|String
name|command
parameter_list|)
block|{
name|int
name|ret
init|=
name|compile
argument_list|(
name|command
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|!=
literal|0
condition|)
return|return
operator|(
name|ret
operator|)
return|;
return|return
name|execute
argument_list|()
return|;
block|}
specifier|private
name|List
argument_list|<
name|PreExecute
argument_list|>
name|getPreExecHooks
parameter_list|()
throws|throws
name|Exception
block|{
name|ArrayList
argument_list|<
name|PreExecute
argument_list|>
name|pehooks
init|=
operator|new
name|ArrayList
argument_list|<
name|PreExecute
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|pestr
init|=
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
argument_list|)
decl_stmt|;
name|pestr
operator|=
name|pestr
operator|.
name|trim
argument_list|()
expr_stmt|;
if|if
condition|(
name|pestr
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
return|return
name|pehooks
return|;
name|String
index|[]
name|peClasses
init|=
name|pestr
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|peClass
range|:
name|peClasses
control|)
block|{
try|try
block|{
name|pehooks
operator|.
name|add
argument_list|(
operator|(
name|PreExecute
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|peClass
operator|.
name|trim
argument_list|()
argument_list|)
operator|.
name|newInstance
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Pre Exec Hook Class not found:"
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
return|return
name|pehooks
return|;
block|}
specifier|public
name|int
name|execute
parameter_list|()
block|{
name|boolean
name|noName
init|=
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPJOBNAME
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|maxlen
init|=
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEJOBNAMELENGTH
argument_list|)
decl_stmt|;
name|String
name|queryId
init|=
name|plan
operator|.
name|getQueryId
argument_list|()
decl_stmt|;
name|String
name|queryStr
init|=
name|plan
operator|.
name|getQueryStr
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|,
name|queryId
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYSTRING
argument_list|,
name|queryStr
argument_list|)
expr_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting command: "
operator|+
name|queryStr
argument_list|)
expr_stmt|;
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHiveHistory
argument_list|()
operator|.
name|startQuery
argument_list|(
name|queryStr
argument_list|,
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|)
argument_list|)
expr_stmt|;
name|resStream
operator|=
literal|null
expr_stmt|;
name|BaseSemanticAnalyzer
name|sem
init|=
name|plan
operator|.
name|getPlan
argument_list|()
decl_stmt|;
comment|// Get all the pre execution hooks and execute them.
for|for
control|(
name|PreExecute
name|peh
range|:
name|getPreExecHooks
argument_list|()
control|)
block|{
name|peh
operator|.
name|run
argument_list|(
name|SessionState
operator|.
name|get
argument_list|()
argument_list|,
name|sem
operator|.
name|getInputs
argument_list|()
argument_list|,
name|sem
operator|.
name|getOutputs
argument_list|()
argument_list|,
name|UserGroupInformation
operator|.
name|getCurrentUGI
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|int
name|jobs
init|=
name|countJobs
argument_list|(
name|sem
operator|.
name|getRootTasks
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|jobs
operator|>
literal|0
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Total MapReduce jobs = "
operator|+
name|jobs
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHiveHistory
argument_list|()
operator|.
name|setQueryProperty
argument_list|(
name|queryId
argument_list|,
name|Keys
operator|.
name|QUERY_NUM_TASKS
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|jobs
argument_list|)
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHiveHistory
argument_list|()
operator|.
name|setIdToTableMap
argument_list|(
name|sem
operator|.
name|getIdToTableNameMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|jobname
init|=
name|Utilities
operator|.
name|abbreviate
argument_list|(
name|queryStr
argument_list|,
name|maxlen
operator|-
literal|6
argument_list|)
decl_stmt|;
name|int
name|curJobNo
init|=
literal|0
decl_stmt|;
comment|// A very simple runtime that keeps putting runnable tasks on a list and
comment|// when a job completes, it puts the children at the back of the list
comment|// while taking the job to run from the front of the list
name|Queue
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|runnable
init|=
operator|new
name|LinkedList
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|rootTask
range|:
name|sem
operator|.
name|getRootTasks
argument_list|()
control|)
block|{
if|if
condition|(
name|runnable
operator|.
name|offer
argument_list|(
name|rootTask
argument_list|)
operator|==
literal|false
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Could not insert the first task into the queue"
argument_list|)
expr_stmt|;
return|return
operator|(
literal|1
operator|)
return|;
block|}
block|}
while|while
condition|(
name|runnable
operator|.
name|peek
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|tsk
init|=
name|runnable
operator|.
name|remove
argument_list|()
decl_stmt|;
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHiveHistory
argument_list|()
operator|.
name|startTask
argument_list|(
name|queryId
argument_list|,
name|tsk
argument_list|,
name|tsk
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|tsk
operator|.
name|isMapRedTask
argument_list|()
condition|)
block|{
name|curJobNo
operator|++
expr_stmt|;
if|if
condition|(
name|noName
condition|)
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPJOBNAME
argument_list|,
name|jobname
operator|+
literal|"("
operator|+
name|curJobNo
operator|+
literal|"/"
operator|+
name|jobs
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
block|}
name|tsk
operator|.
name|initialize
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|exitVal
init|=
name|tsk
operator|.
name|execute
argument_list|()
decl_stmt|;
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHiveHistory
argument_list|()
operator|.
name|setTaskProperty
argument_list|(
name|queryId
argument_list|,
name|tsk
operator|.
name|getId
argument_list|()
argument_list|,
name|Keys
operator|.
name|TASK_RET_CODE
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|exitVal
argument_list|)
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHiveHistory
argument_list|()
operator|.
name|endTask
argument_list|(
name|queryId
argument_list|,
name|tsk
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|exitVal
operator|!=
literal|0
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"FAILED: Execution Error, return code "
operator|+
name|exitVal
operator|+
literal|" from "
operator|+
name|tsk
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|9
return|;
block|}
name|tsk
operator|.
name|setDone
argument_list|()
expr_stmt|;
if|if
condition|(
name|tsk
operator|.
name|getChildTasks
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|child
range|:
name|tsk
operator|.
name|getChildTasks
argument_list|()
control|)
block|{
comment|// Check if the child is runnable
if|if
condition|(
operator|!
name|child
operator|.
name|isRunnable
argument_list|()
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|runnable
operator|.
name|offer
argument_list|(
name|child
argument_list|)
operator|==
literal|false
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Could not add child task to queue"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHiveHistory
argument_list|()
operator|.
name|setQueryProperty
argument_list|(
name|queryId
argument_list|,
name|Keys
operator|.
name|QUERY_RET_CODE
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHiveHistory
argument_list|()
operator|.
name|printRowCount
argument_list|(
name|queryId
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHiveHistory
argument_list|()
operator|.
name|setQueryProperty
argument_list|(
name|queryId
argument_list|,
name|Keys
operator|.
name|QUERY_RET_CODE
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
literal|12
argument_list|)
argument_list|)
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
literal|"FAILED: Unknown exception : "
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
return|return
operator|(
literal|12
operator|)
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getHiveHistory
argument_list|()
operator|.
name|endQuery
argument_list|(
name|queryId
argument_list|)
expr_stmt|;
if|if
condition|(
name|noName
condition|)
block|{
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPJOBNAME
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
block|}
name|console
operator|.
name|printInfo
argument_list|(
literal|"OK"
argument_list|)
expr_stmt|;
return|return
operator|(
literal|0
operator|)
return|;
block|}
specifier|public
name|boolean
name|getResults
parameter_list|(
name|Vector
argument_list|<
name|String
argument_list|>
name|res
parameter_list|)
block|{
if|if
condition|(
name|plan
operator|!=
literal|null
operator|&&
name|plan
operator|.
name|getPlan
argument_list|()
operator|.
name|getFetchTask
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|BaseSemanticAnalyzer
name|sem
init|=
name|plan
operator|.
name|getPlan
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|sem
operator|.
name|getFetchTaskInit
argument_list|()
condition|)
block|{
name|sem
operator|.
name|setFetchTaskInit
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|sem
operator|.
name|getFetchTask
argument_list|()
operator|.
name|initialize
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
name|FetchTask
name|ft
init|=
operator|(
name|FetchTask
operator|)
name|sem
operator|.
name|getFetchTask
argument_list|()
decl_stmt|;
name|ft
operator|.
name|setMaxRows
argument_list|(
name|maxRows
argument_list|)
expr_stmt|;
return|return
name|ft
operator|.
name|fetch
argument_list|(
name|res
argument_list|)
return|;
block|}
if|if
condition|(
name|resStream
operator|==
literal|null
condition|)
name|resStream
operator|=
name|ctx
operator|.
name|getStream
argument_list|()
expr_stmt|;
if|if
condition|(
name|resStream
operator|==
literal|null
condition|)
return|return
literal|false
return|;
name|int
name|numRows
init|=
literal|0
decl_stmt|;
name|String
name|row
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|numRows
operator|<
name|maxRows
condition|)
block|{
if|if
condition|(
name|resStream
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|numRows
operator|>
literal|0
condition|)
return|return
literal|true
return|;
else|else
return|return
literal|false
return|;
block|}
name|bos
operator|.
name|reset
argument_list|()
expr_stmt|;
name|Utilities
operator|.
name|streamStatus
name|ss
decl_stmt|;
try|try
block|{
name|ss
operator|=
name|Utilities
operator|.
name|readColumn
argument_list|(
name|resStream
argument_list|,
name|bos
argument_list|)
expr_stmt|;
if|if
condition|(
name|bos
operator|.
name|getCount
argument_list|()
operator|>
literal|0
condition|)
name|row
operator|=
operator|new
name|String
argument_list|(
name|bos
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bos
operator|.
name|getCount
argument_list|()
argument_list|,
literal|"UTF-8"
argument_list|)
expr_stmt|;
elseif|else
if|if
condition|(
name|ss
operator|==
name|Utilities
operator|.
name|streamStatus
operator|.
name|TERMINATED
condition|)
name|row
operator|=
operator|new
name|String
argument_list|()
expr_stmt|;
if|if
condition|(
name|row
operator|!=
literal|null
condition|)
block|{
name|numRows
operator|++
expr_stmt|;
name|res
operator|.
name|add
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
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
literal|"FAILED: Unexpected IO exception : "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|res
operator|=
literal|null
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|ss
operator|==
name|Utilities
operator|.
name|streamStatus
operator|.
name|EOF
condition|)
name|resStream
operator|=
name|ctx
operator|.
name|getStream
argument_list|()
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
name|int
name|close
parameter_list|()
block|{
try|try
block|{
comment|// Delete the scratch directory from the context
name|ctx
operator|.
name|removeScratchDir
argument_list|()
expr_stmt|;
name|ctx
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"FAILED: Unknown exception : "
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
return|return
operator|(
literal|13
operator|)
return|;
block|}
return|return
operator|(
literal|0
operator|)
return|;
block|}
block|}
end_class

end_unit

