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
name|exec
operator|.
name|spark
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
name|Collection
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|fs
operator|.
name|ContentSummary
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
name|DriverContext
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
name|Operator
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
name|exec
operator|.
name|spark
operator|.
name|session
operator|.
name|SparkSession
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
name|spark
operator|.
name|session
operator|.
name|SparkSessionManager
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
name|spark
operator|.
name|session
operator|.
name|SparkSessionManagerImpl
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
name|metadata
operator|.
name|HiveException
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
name|BaseWork
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
name|MapWork
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
name|ReduceWork
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
name|SparkWork
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
name|UnionWork
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
name|StageType
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
name|mapred
operator|.
name|JobConf
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
name|StringUtils
import|;
end_import

begin_class
specifier|public
class|class
name|SparkTask
extends|extends
name|Task
argument_list|<
name|SparkWork
argument_list|>
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
specifier|transient
name|JobConf
name|job
decl_stmt|;
specifier|private
specifier|transient
name|ContentSummary
name|inputSummary
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|QueryPlan
name|queryPlan
parameter_list|,
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
name|queryPlan
argument_list|,
name|driverContext
argument_list|)
expr_stmt|;
name|job
operator|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|,
name|SparkTask
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|(
name|DriverContext
name|driverContext
parameter_list|)
block|{
name|int
name|rc
init|=
literal|1
decl_stmt|;
name|SparkSession
name|sparkSession
init|=
literal|null
decl_stmt|;
name|SparkSessionManager
name|sparkSessionManager
init|=
literal|null
decl_stmt|;
try|try
block|{
name|configureNumberOfReducers
argument_list|()
expr_stmt|;
name|sparkSessionManager
operator|=
name|SparkSessionManagerImpl
operator|.
name|getInstance
argument_list|()
expr_stmt|;
name|sparkSession
operator|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getSparkSession
argument_list|()
expr_stmt|;
comment|// Spark configurations are updated close the existing session
if|if
condition|(
name|conf
operator|.
name|getSparkConfigUpdated
argument_list|()
condition|)
block|{
name|sparkSessionManager
operator|.
name|closeSession
argument_list|(
name|sparkSession
argument_list|)
expr_stmt|;
name|sparkSession
operator|=
literal|null
expr_stmt|;
name|conf
operator|.
name|setSparkConfigUpdated
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|sparkSession
operator|=
name|sparkSessionManager
operator|.
name|getSession
argument_list|(
name|sparkSession
argument_list|,
name|conf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|setSparkSession
argument_list|(
name|sparkSession
argument_list|)
expr_stmt|;
name|rc
operator|=
name|sparkSession
operator|.
name|submit
argument_list|(
name|driverContext
argument_list|,
name|getWork
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to execute spark task."
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|sparkSession
operator|!=
literal|null
operator|&&
name|sparkSessionManager
operator|!=
literal|null
condition|)
block|{
name|rc
operator|=
name|close
argument_list|(
name|rc
argument_list|)
expr_stmt|;
try|try
block|{
name|sparkSessionManager
operator|.
name|returnSession
argument_list|(
name|sparkSession
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to return the session to SessionManager"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|rc
return|;
block|}
comment|/**    * close will move the temp files into the right place for the fetch    * task. If the job has failed it will clean up the files.    */
specifier|private
name|int
name|close
parameter_list|(
name|int
name|rc
parameter_list|)
block|{
try|try
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|ws
init|=
name|work
operator|.
name|getAllWork
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|ws
control|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|op
range|:
name|w
operator|.
name|getAllOperators
argument_list|()
control|)
block|{
name|op
operator|.
name|jobClose
argument_list|(
name|conf
argument_list|,
name|rc
operator|==
literal|0
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
comment|// jobClose needs to execute successfully otherwise fail task
if|if
condition|(
name|rc
operator|==
literal|0
condition|)
block|{
name|rc
operator|=
literal|3
expr_stmt|;
name|String
name|mesg
init|=
literal|"Job Commit failed with exception '"
operator|+
name|Utilities
operator|.
name|getNameMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"'"
decl_stmt|;
name|console
operator|.
name|printError
argument_list|(
name|mesg
argument_list|,
literal|"\n"
operator|+
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
return|return
name|rc
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isMapRedTask
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|StageType
name|getType
parameter_list|()
block|{
return|return
name|StageType
operator|.
name|MAPRED
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"SPARK"
return|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|MapWork
argument_list|>
name|getMapWork
parameter_list|()
block|{
name|List
argument_list|<
name|MapWork
argument_list|>
name|result
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|SparkWork
name|work
init|=
name|getWork
argument_list|()
decl_stmt|;
comment|// framework expects MapWork instances that have no physical parents (i.e.: union parent is
comment|// fine, broadcast parent isn't)
for|for
control|(
name|BaseWork
name|w
range|:
name|work
operator|.
name|getAllWorkUnsorted
argument_list|()
control|)
block|{
if|if
condition|(
name|w
operator|instanceof
name|MapWork
condition|)
block|{
name|List
argument_list|<
name|BaseWork
argument_list|>
name|parents
init|=
name|work
operator|.
name|getParents
argument_list|(
name|w
argument_list|)
decl_stmt|;
name|boolean
name|candidate
init|=
literal|true
decl_stmt|;
for|for
control|(
name|BaseWork
name|parent
range|:
name|parents
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|parent
operator|instanceof
name|UnionWork
operator|)
condition|)
block|{
name|candidate
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
name|candidate
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|(
name|MapWork
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
comment|/**    * Set the number of reducers for the spark work.    */
specifier|private
name|void
name|configureNumberOfReducers
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|BaseWork
name|baseWork
range|:
name|work
operator|.
name|getAllWork
argument_list|()
control|)
block|{
if|if
condition|(
name|baseWork
operator|instanceof
name|ReduceWork
condition|)
block|{
name|configureNumberOfReducers
argument_list|(
operator|(
name|ReduceWork
operator|)
name|baseWork
argument_list|)
expr_stmt|;
block|}
block|}
name|console
operator|.
name|printInfo
argument_list|(
literal|"In order to change the average load for a reducer (in bytes):"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"  set "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|BYTESPERREDUCER
operator|.
name|varname
operator|+
literal|"=<number>"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"In order to limit the maximum number of reducers:"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"  set "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|MAXREDUCERS
operator|.
name|varname
operator|+
literal|"=<number>"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"In order to set a constant number of reducers:"
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"  set "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPNUMREDUCERS
operator|+
literal|"=<number>"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|configureNumberOfReducers
parameter_list|(
name|ReduceWork
name|rWork
parameter_list|)
throws|throws
name|IOException
block|{
comment|// this is a temporary hack to fix things that are not fixed in the compiler
name|Integer
name|numReducersFromWork
init|=
name|rWork
operator|==
literal|null
condition|?
literal|0
else|:
name|rWork
operator|.
name|getNumReduceTasks
argument_list|()
decl_stmt|;
if|if
condition|(
name|rWork
operator|==
literal|null
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Number of reduce tasks is set to 0 since there's no reduce operator"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|numReducersFromWork
operator|>=
literal|0
condition|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"Number of reduce tasks determined at compile time: "
operator|+
name|rWork
operator|.
name|getNumReduceTasks
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|job
operator|.
name|getNumReduceTasks
argument_list|()
operator|>
literal|0
condition|)
block|{
name|int
name|reducers
init|=
name|job
operator|.
name|getNumReduceTasks
argument_list|()
decl_stmt|;
name|rWork
operator|.
name|setNumReduceTasks
argument_list|(
name|reducers
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Number of reduce tasks not specified. Defaulting to jobconf value of: "
operator|+
name|reducers
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|inputSummary
operator|==
literal|null
condition|)
block|{
name|inputSummary
operator|=
name|Utilities
operator|.
name|getInputSummary
argument_list|(
name|driverContext
operator|.
name|getCtx
argument_list|()
argument_list|,
name|work
operator|.
name|getMapWork
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|int
name|reducers
init|=
name|Utilities
operator|.
name|estimateNumberOfReducers
argument_list|(
name|conf
argument_list|,
name|inputSummary
argument_list|,
name|work
operator|.
name|getMapWork
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|rWork
operator|.
name|setNumReduceTasks
argument_list|(
name|reducers
argument_list|)
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
literal|"Number of reduce tasks not specified. Estimated from input data size: "
operator|+
name|reducers
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

