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
name|service
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
name|service
operator|.
name|ThriftHive
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
name|service
operator|.
name|HiveServerException
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
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TProcessorFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|TServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|TThreadPoolServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TServerSocket
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TServerTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransportFactory
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
name|api
operator|.
name|Query
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
name|api
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
name|metastore
operator|.
name|api
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
name|metastore
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
name|mapred
operator|.
name|ClusterStatus
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
name|JobTracker
import|;
end_import

begin_comment
comment|/**  * Thrift Hive Server Implementation  */
end_comment

begin_class
specifier|public
class|class
name|HiveServer
extends|extends
name|ThriftHive
block|{
specifier|private
specifier|final
specifier|static
name|String
name|VERSION
init|=
literal|"0"
decl_stmt|;
comment|/**    * Handler which implements the Hive Interface    * This class can be used in lieu of the HiveClient class    * to get an embedded server    */
specifier|public
specifier|static
class|class
name|HiveServerHandler
extends|extends
name|HiveMetaStore
operator|.
name|HMSHandler
implements|implements
name|HiveInterface
block|{
comment|/**      * Hive server uses org.apache.hadoop.hive.ql.Driver for run() and       * getResults() methods.      */
specifier|private
name|Driver
name|driver
decl_stmt|;
comment|/**      * Stores state per connection      */
specifier|private
name|SessionState
name|session
decl_stmt|;
comment|/**      * Flag that indicates whether the last executed command was a Hive query      */
specifier|private
name|boolean
name|isHiveQuery
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HiveServer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**      * A constructor.      */
specifier|public
name|HiveServerHandler
parameter_list|()
throws|throws
name|MetaException
block|{
name|super
argument_list|(
name|HiveServer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|isHiveQuery
operator|=
literal|false
expr_stmt|;
name|SessionState
name|session
init|=
operator|new
name|SessionState
argument_list|(
operator|new
name|HiveConf
argument_list|(
name|SessionState
operator|.
name|class
argument_list|)
argument_list|)
decl_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|session
argument_list|)
expr_stmt|;
name|session
operator|.
name|in
operator|=
literal|null
expr_stmt|;
name|session
operator|.
name|out
operator|=
literal|null
expr_stmt|;
name|session
operator|.
name|err
operator|=
literal|null
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|()
expr_stmt|;
block|}
comment|/**      * Executes a query.      *      * @param cmd HiveQL query to execute      */
specifier|public
name|void
name|execute
parameter_list|(
name|String
name|cmd
parameter_list|)
throws|throws
name|HiveServerException
throws|,
name|TException
block|{
name|HiveServerHandler
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Running the query: "
operator|+
name|cmd
argument_list|)
expr_stmt|;
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
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
literal|"\\s"
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
name|int
name|ret
init|=
literal|0
decl_stmt|;
try|try
block|{
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
name|isHiveQuery
operator|=
literal|true
expr_stmt|;
name|ret
operator|=
name|driver
operator|.
name|run
argument_list|(
name|cmd
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|isHiveQuery
operator|=
literal|false
expr_stmt|;
name|ret
operator|=
name|proc
operator|.
name|run
argument_list|(
name|cmd_1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveServerException
argument_list|(
literal|"Error running query: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|ret
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|HiveServerException
argument_list|(
literal|"Query returned non-zero code: "
operator|+
name|ret
argument_list|)
throw|;
block|}
block|}
comment|/**      * Return the status information about the Map-Reduce cluster      */
specifier|public
name|HiveClusterStatus
name|getClusterStatus
parameter_list|()
throws|throws
name|HiveServerException
throws|,
name|TException
block|{
name|HiveClusterStatus
name|hcs
decl_stmt|;
try|try
block|{
name|ClusterStatus
name|cs
init|=
name|driver
operator|.
name|getClusterStatus
argument_list|()
decl_stmt|;
name|JobTracker
operator|.
name|State
name|jbs
init|=
name|cs
operator|.
name|getJobTrackerState
argument_list|()
decl_stmt|;
comment|// Convert the ClusterStatus to its Thrift equivalent: HiveClusterStatus
name|int
name|state
decl_stmt|;
switch|switch
condition|(
name|jbs
condition|)
block|{
case|case
name|INITIALIZING
case|:
name|state
operator|=
name|JobTrackerState
operator|.
name|INITIALIZING
expr_stmt|;
break|break;
case|case
name|RUNNING
case|:
name|state
operator|=
name|JobTrackerState
operator|.
name|RUNNING
expr_stmt|;
break|break;
default|default:
name|String
name|errorMsg
init|=
literal|"Unrecognized JobTracker state: "
operator|+
name|jbs
operator|.
name|toString
argument_list|()
decl_stmt|;
throw|throw
operator|new
name|Exception
argument_list|(
name|errorMsg
argument_list|)
throw|;
block|}
name|hcs
operator|=
operator|new
name|HiveClusterStatus
argument_list|(
name|cs
operator|.
name|getTaskTrackers
argument_list|()
argument_list|,
name|cs
operator|.
name|getMapTasks
argument_list|()
argument_list|,
name|cs
operator|.
name|getReduceTasks
argument_list|()
argument_list|,
name|cs
operator|.
name|getMaxMapTasks
argument_list|()
argument_list|,
name|cs
operator|.
name|getMaxReduceTasks
argument_list|()
argument_list|,
name|state
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|HiveServerException
argument_list|(
literal|"Unable to get cluster status: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|hcs
return|;
block|}
comment|/**      * Return the Hive schema of the query result      */
specifier|public
name|Schema
name|getSchema
parameter_list|()
throws|throws
name|HiveServerException
throws|,
name|TException
block|{
if|if
condition|(
operator|!
name|isHiveQuery
condition|)
comment|// Return empty schema if the last command was not a Hive query
return|return
operator|new
name|Schema
argument_list|()
return|;
try|try
block|{
name|Schema
name|schema
init|=
name|driver
operator|.
name|getSchema
argument_list|()
decl_stmt|;
if|if
condition|(
name|schema
operator|==
literal|null
condition|)
block|{
name|schema
operator|=
operator|new
name|Schema
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Returning schema: "
operator|+
name|schema
argument_list|)
expr_stmt|;
return|return
name|schema
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|HiveServerException
argument_list|(
literal|"Unable to get schema: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**      * Return the Thrift schema of the query result      */
specifier|public
name|Schema
name|getThriftSchema
parameter_list|()
throws|throws
name|HiveServerException
throws|,
name|TException
block|{
if|if
condition|(
operator|!
name|isHiveQuery
condition|)
comment|// Return empty schema if the last command was not a Hive query
return|return
operator|new
name|Schema
argument_list|()
return|;
try|try
block|{
name|Schema
name|schema
init|=
name|driver
operator|.
name|getThriftSchema
argument_list|()
decl_stmt|;
if|if
condition|(
name|schema
operator|==
literal|null
condition|)
block|{
name|schema
operator|=
operator|new
name|Schema
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Returning schema: "
operator|+
name|schema
argument_list|)
expr_stmt|;
return|return
name|schema
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|HiveServerException
argument_list|(
literal|"Unable to get schema: "
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**       * Fetches the next row in a query result set.      *       * @return the next row in a query result set. null if there is no more row to fetch.      */
specifier|public
name|String
name|fetchOne
parameter_list|()
throws|throws
name|HiveServerException
throws|,
name|TException
block|{
if|if
condition|(
operator|!
name|isHiveQuery
condition|)
comment|// Return no results if the last command was not a Hive query
return|return
literal|""
return|;
name|Vector
argument_list|<
name|String
argument_list|>
name|result
init|=
operator|new
name|Vector
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|driver
operator|.
name|setMaxRows
argument_list|(
literal|1
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|driver
operator|.
name|getResults
argument_list|(
name|result
argument_list|)
condition|)
block|{
return|return
name|result
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
comment|// TODO: Cannot return null here because thrift cannot handle nulls
comment|// TODO: Returning empty string for now. Need to figure out how to
comment|// TODO: return null in some other way
return|return
literal|""
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveServerException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**      * Fetches numRows rows.      *      * @param numRows Number of rows to fetch.      * @return A list of rows. The size of the list is numRows if there are at least       *         numRows rows available to return. The size is smaller than numRows if      *         there aren't enough rows. The list will be empty if there is no more       *         row to fetch or numRows == 0.       * @throws HiveServerException Invalid value for numRows (numRows< 0)      */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|fetchN
parameter_list|(
name|int
name|numRows
parameter_list|)
throws|throws
name|HiveServerException
throws|,
name|TException
block|{
if|if
condition|(
name|numRows
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|HiveServerException
argument_list|(
literal|"Invalid argument for number of rows: "
operator|+
name|numRows
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|isHiveQuery
condition|)
comment|// Return no results if the last command was not a Hive query
return|return
operator|new
name|Vector
argument_list|<
name|String
argument_list|>
argument_list|()
return|;
name|Vector
argument_list|<
name|String
argument_list|>
name|result
init|=
operator|new
name|Vector
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|driver
operator|.
name|setMaxRows
argument_list|(
name|numRows
argument_list|)
expr_stmt|;
try|try
block|{
name|driver
operator|.
name|getResults
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveServerException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
comment|/**      * Fetches all the rows in a result set.      *      * @return All the rows in a result set of a query executed using execute method.      *      * TODO: Currently the server buffers all the rows before returning them       * to the client. Decide whether the buffering should be done in the client.      */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|fetchAll
parameter_list|()
throws|throws
name|HiveServerException
throws|,
name|TException
block|{
if|if
condition|(
operator|!
name|isHiveQuery
condition|)
comment|// Return no results if the last command was not a Hive query
return|return
operator|new
name|Vector
argument_list|<
name|String
argument_list|>
argument_list|()
return|;
name|Vector
argument_list|<
name|String
argument_list|>
name|rows
init|=
operator|new
name|Vector
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Vector
argument_list|<
name|String
argument_list|>
name|result
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
name|driver
operator|.
name|getResults
argument_list|(
name|result
argument_list|)
condition|)
block|{
name|rows
operator|.
name|addAll
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|result
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveServerException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|rows
return|;
block|}
comment|/**      * Return the status of the server      */
annotation|@
name|Override
specifier|public
name|int
name|getStatus
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
comment|/**      * Return the version of the server software      */
annotation|@
name|Override
specifier|public
name|String
name|getVersion
parameter_list|()
block|{
return|return
name|VERSION
return|;
block|}
annotation|@
name|Override
specifier|public
name|QueryPlan
name|getQueryPlan
parameter_list|()
throws|throws
name|HiveServerException
throws|,
name|TException
block|{
name|QueryPlan
name|qp
init|=
operator|new
name|QueryPlan
argument_list|()
decl_stmt|;
comment|// TODO for now only return one query at a time
comment|// going forward, all queries associated with a single statement
comment|// will be returned in a single QueryPlan
try|try
block|{
name|qp
operator|.
name|addToQueries
argument_list|(
name|driver
operator|.
name|getQueryPlan
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveServerException
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|qp
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|ThriftHiveProcessorFactory
extends|extends
name|TProcessorFactory
block|{
specifier|public
name|ThriftHiveProcessorFactory
parameter_list|(
name|TProcessor
name|processor
parameter_list|)
block|{
name|super
argument_list|(
name|processor
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TProcessor
name|getProcessor
parameter_list|(
name|TTransport
name|trans
parameter_list|)
block|{
try|try
block|{
name|Iface
name|handler
init|=
operator|new
name|HiveServerHandler
argument_list|()
decl_stmt|;
return|return
operator|new
name|ThriftHive
operator|.
name|Processor
argument_list|(
name|handler
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
try|try
block|{
name|int
name|port
init|=
literal|10000
decl_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|>=
literal|1
condition|)
block|{
name|port
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
name|TServerTransport
name|serverTransport
init|=
operator|new
name|TServerSocket
argument_list|(
name|port
argument_list|)
decl_stmt|;
name|ThriftHiveProcessorFactory
name|hfactory
init|=
operator|new
name|ThriftHiveProcessorFactory
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|TThreadPoolServer
operator|.
name|Options
name|options
init|=
operator|new
name|TThreadPoolServer
operator|.
name|Options
argument_list|()
decl_stmt|;
name|TServer
name|server
init|=
operator|new
name|TThreadPoolServer
argument_list|(
name|hfactory
argument_list|,
name|serverTransport
argument_list|,
operator|new
name|TTransportFactory
argument_list|()
argument_list|,
operator|new
name|TTransportFactory
argument_list|()
argument_list|,
operator|new
name|TBinaryProtocol
operator|.
name|Factory
argument_list|()
argument_list|,
operator|new
name|TBinaryProtocol
operator|.
name|Factory
argument_list|()
argument_list|,
name|options
argument_list|)
decl_stmt|;
name|HiveServerHandler
operator|.
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting hive server on port "
operator|+
name|port
argument_list|)
expr_stmt|;
name|server
operator|.
name|serve
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|x
parameter_list|)
block|{
name|x
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

