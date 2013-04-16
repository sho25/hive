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
name|profiler
package|;
end_package

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
name|sql
operator|.
name|ResultSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|PreparedStatement
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
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
name|ql
operator|.
name|exec
operator|.
name|Utilities
import|;
end_import

begin_class
specifier|public
class|class
name|HiveProfilerStatsAggregator
block|{
specifier|final
specifier|private
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|long
name|totalTime
decl_stmt|;
specifier|private
name|HiveProfilePublisherInfo
name|rawProfileConnInfo
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|HiveProfilerAggregateStat
argument_list|>
name|stats
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|HiveProfilerAggregateStat
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|HiveProfilerStatsAggregator
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
try|try
block|{
comment|// initialize the raw data connection
name|rawProfileConnInfo
operator|=
operator|new
name|HiveProfilePublisherInfo
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|populateAggregateStats
argument_list|(
name|conf
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
literal|"Error during initialization"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|long
name|getTotalTime
parameter_list|()
block|{
return|return
name|totalTime
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|HiveProfilerAggregateStat
argument_list|>
name|getAggregateStats
parameter_list|()
block|{
return|return
name|stats
return|;
block|}
specifier|private
name|void
name|populateAggregateStats
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|SQLException
block|{
name|int
name|waitWindow
init|=
name|rawProfileConnInfo
operator|.
name|getWaitWindow
argument_list|()
decl_stmt|;
name|int
name|maxRetries
init|=
name|rawProfileConnInfo
operator|.
name|getMaxRetries
argument_list|()
decl_stmt|;
name|String
name|queryId
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEQUERYID
argument_list|)
decl_stmt|;
name|String
name|profilerStatsTable
init|=
name|rawProfileConnInfo
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|String
name|getProfileStats
init|=
literal|"SELECT * FROM "
operator|+
name|profilerStatsTable
operator|+
literal|" WHERE queryId = ? "
decl_stmt|;
name|Utilities
operator|.
name|SQLCommand
argument_list|<
name|ResultSet
argument_list|>
name|execQuery
init|=
operator|new
name|Utilities
operator|.
name|SQLCommand
argument_list|<
name|ResultSet
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ResultSet
name|run
parameter_list|(
name|PreparedStatement
name|stmt
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
name|stmt
operator|.
name|executeQuery
argument_list|()
return|;
block|}
block|}
decl_stmt|;
try|try
block|{
name|PreparedStatement
name|getProfileStatsStmt
init|=
name|Utilities
operator|.
name|prepareWithRetry
argument_list|(
name|rawProfileConnInfo
operator|.
name|getConnection
argument_list|()
argument_list|,
name|getProfileStats
argument_list|,
name|waitWindow
argument_list|,
name|maxRetries
argument_list|)
decl_stmt|;
name|getProfileStatsStmt
operator|.
name|setString
argument_list|(
literal|1
argument_list|,
name|queryId
argument_list|)
expr_stmt|;
name|ResultSet
name|result
init|=
name|Utilities
operator|.
name|executeWithRetry
argument_list|(
name|execQuery
argument_list|,
name|getProfileStatsStmt
argument_list|,
name|waitWindow
argument_list|,
name|maxRetries
argument_list|)
decl_stmt|;
name|populateAggregateStats
argument_list|(
name|result
argument_list|)
expr_stmt|;
name|getProfileStatsStmt
operator|.
name|close
argument_list|()
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
literal|"executing error: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|HiveProfilerUtils
operator|.
name|closeConnection
argument_list|(
name|rawProfileConnInfo
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|populateAggregateStats
parameter_list|(
name|ResultSet
name|result
parameter_list|)
block|{
try|try
block|{
while|while
condition|(
name|result
operator|.
name|next
argument_list|()
condition|)
block|{
comment|// string denoting parent==>child
comment|// example:SEL_2==>GBY_1
name|String
name|levelAnnoName
init|=
name|result
operator|.
name|getString
argument_list|(
name|HiveProfilerStats
operator|.
name|Columns
operator|.
name|LEVEL_ANNO_NAME
argument_list|)
decl_stmt|;
comment|// Microseconds
name|Long
name|curInclTime
init|=
name|result
operator|.
name|getLong
argument_list|(
name|HiveProfilerStats
operator|.
name|Columns
operator|.
name|INCL_TIME
argument_list|)
operator|/
literal|1000
decl_stmt|;
name|Long
name|curCallCount
init|=
name|result
operator|.
name|getLong
argument_list|(
name|HiveProfilerStats
operator|.
name|Columns
operator|.
name|CALL_COUNT
argument_list|)
decl_stmt|;
name|totalTime
operator|+=
name|curInclTime
expr_stmt|;
if|if
condition|(
name|curInclTime
operator|!=
literal|null
operator|&&
name|curCallCount
operator|!=
literal|null
condition|)
block|{
name|HiveProfilerAggregateStat
name|curStat
decl_stmt|;
if|if
condition|(
name|stats
operator|.
name|containsKey
argument_list|(
name|levelAnnoName
argument_list|)
condition|)
block|{
name|curStat
operator|=
name|stats
operator|.
name|get
argument_list|(
name|levelAnnoName
argument_list|)
expr_stmt|;
name|curStat
operator|.
name|update
argument_list|(
name|curInclTime
argument_list|,
name|curCallCount
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|curStat
operator|=
operator|new
name|HiveProfilerAggregateStat
argument_list|(
name|curInclTime
argument_list|,
name|curCallCount
argument_list|)
expr_stmt|;
block|}
name|stats
operator|.
name|put
argument_list|(
name|levelAnnoName
argument_list|,
name|curStat
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Error Aggregating Stats"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

