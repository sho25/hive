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
name|Serializable
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
name|CommandNeedRetryException
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
name|CompilationOpContext
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
name|QueryState
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
name|mr
operator|.
name|ExecMapper
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
name|io
operator|.
name|AcidUtils
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
name|io
operator|.
name|HiveInputFormat
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
name|metadata
operator|.
name|VirtualColumn
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
name|FetchWork
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
name|FileSinkDesc
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
name|TableDesc
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
name|serde2
operator|.
name|ColumnProjectionUtils
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
name|objectinspector
operator|.
name|ObjectInspector
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

begin_comment
comment|/**  * FetchTask implementation.  **/
end_comment

begin_class
specifier|public
class|class
name|FetchTask
extends|extends
name|Task
argument_list|<
name|FetchWork
argument_list|>
implements|implements
name|Serializable
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
name|int
name|maxRows
init|=
literal|100
decl_stmt|;
specifier|private
name|FetchOperator
name|fetch
decl_stmt|;
specifier|private
name|ListSinkOperator
name|sink
decl_stmt|;
specifier|private
name|int
name|totalRows
decl_stmt|;
specifier|private
specifier|static
specifier|transient
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|FetchTask
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|FetchTask
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|QueryState
name|queryState
parameter_list|,
name|QueryPlan
name|queryPlan
parameter_list|,
name|DriverContext
name|ctx
parameter_list|,
name|CompilationOpContext
name|opContext
parameter_list|)
block|{
name|super
operator|.
name|initialize
argument_list|(
name|queryState
argument_list|,
name|queryPlan
argument_list|,
name|ctx
argument_list|,
name|opContext
argument_list|)
expr_stmt|;
name|work
operator|.
name|initializeForFetch
argument_list|(
name|opContext
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Create a file system handle
name|JobConf
name|job
init|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|?
argument_list|>
name|source
init|=
name|work
operator|.
name|getSource
argument_list|()
decl_stmt|;
if|if
condition|(
name|source
operator|instanceof
name|TableScanOperator
condition|)
block|{
name|TableScanOperator
name|ts
init|=
operator|(
name|TableScanOperator
operator|)
name|source
decl_stmt|;
comment|// push down projections
name|ColumnProjectionUtils
operator|.
name|appendReadColumns
argument_list|(
name|job
argument_list|,
name|ts
operator|.
name|getNeededColumnIDs
argument_list|()
argument_list|,
name|ts
operator|.
name|getNeededColumns
argument_list|()
argument_list|)
expr_stmt|;
comment|// push down filters
name|HiveInputFormat
operator|.
name|pushFilters
argument_list|(
name|job
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|AcidUtils
operator|.
name|setTransactionalTableScan
argument_list|(
name|job
argument_list|,
name|ts
operator|.
name|getConf
argument_list|()
operator|.
name|isAcidTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sink
operator|=
name|work
operator|.
name|getSink
argument_list|()
expr_stmt|;
name|fetch
operator|=
operator|new
name|FetchOperator
argument_list|(
name|work
argument_list|,
name|job
argument_list|,
name|source
argument_list|,
name|getVirtualColumns
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
name|source
operator|.
name|initialize
argument_list|(
name|conf
argument_list|,
operator|new
name|ObjectInspector
index|[]
block|{
name|fetch
operator|.
name|getOutputObjectInspector
argument_list|()
block|}
argument_list|)
expr_stmt|;
name|totalRows
operator|=
literal|0
expr_stmt|;
name|ExecMapper
operator|.
name|setDone
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Bail out ungracefully - we should never hit
comment|// this here - but would have hit it in SemanticAnalyzer
name|LOG
operator|.
name|error
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|VirtualColumn
argument_list|>
name|getVirtualColumns
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|ts
parameter_list|)
block|{
if|if
condition|(
name|ts
operator|instanceof
name|TableScanOperator
operator|&&
name|ts
operator|.
name|getConf
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
operator|(
operator|(
name|TableScanOperator
operator|)
name|ts
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|getVirtualCols
argument_list|()
return|;
block|}
return|return
literal|null
return|;
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
assert|assert
literal|false
assert|;
return|return
literal|0
return|;
block|}
comment|/**    * Return the tableDesc of the fetchWork.    */
specifier|public
name|TableDesc
name|getTblDesc
parameter_list|()
block|{
return|return
name|work
operator|.
name|getTblDesc
argument_list|()
return|;
block|}
comment|/**    * Return the maximum number of rows returned by fetch.    */
specifier|public
name|int
name|getMaxRows
parameter_list|()
block|{
return|return
name|maxRows
return|;
block|}
comment|/**    * Set the maximum number of rows returned by fetch.    */
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
name|fetch
parameter_list|(
name|List
name|res
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|sink
operator|.
name|reset
argument_list|(
name|res
argument_list|)
expr_stmt|;
name|int
name|rowsRet
init|=
name|work
operator|.
name|getLeastNumRows
argument_list|()
decl_stmt|;
if|if
condition|(
name|rowsRet
operator|<=
literal|0
condition|)
block|{
name|rowsRet
operator|=
name|work
operator|.
name|getLimit
argument_list|()
operator|>=
literal|0
condition|?
name|Math
operator|.
name|min
argument_list|(
name|work
operator|.
name|getLimit
argument_list|()
operator|-
name|totalRows
argument_list|,
name|maxRows
argument_list|)
else|:
name|maxRows
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|rowsRet
operator|<=
literal|0
operator|||
name|work
operator|.
name|getLimit
argument_list|()
operator|==
name|totalRows
condition|)
block|{
name|fetch
operator|.
name|clearFetchContext
argument_list|()
expr_stmt|;
return|return
literal|false
return|;
block|}
name|boolean
name|fetched
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|sink
operator|.
name|getNumRows
argument_list|()
operator|<
name|rowsRet
condition|)
block|{
if|if
condition|(
operator|!
name|fetch
operator|.
name|pushRow
argument_list|()
condition|)
block|{
if|if
condition|(
name|work
operator|.
name|getLeastNumRows
argument_list|()
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|CommandNeedRetryException
argument_list|()
throw|;
block|}
return|return
name|fetched
return|;
block|}
name|fetched
operator|=
literal|true
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|CommandNeedRetryException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|totalRows
operator|+=
name|sink
operator|.
name|getNumRows
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|isFetchFrom
parameter_list|(
name|FileSinkDesc
name|fs
parameter_list|)
block|{
return|return
name|fs
operator|.
name|getFinalDirName
argument_list|()
operator|.
name|equals
argument_list|(
name|work
operator|.
name|getTblDir
argument_list|()
argument_list|)
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
name|FETCH
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
literal|"FETCH"
return|;
block|}
comment|/**    * Clear the Fetch Operator.    *    * @throws HiveException    */
specifier|public
name|void
name|clearFetch
parameter_list|()
throws|throws
name|HiveException
block|{
if|if
condition|(
name|fetch
operator|!=
literal|null
condition|)
block|{
name|fetch
operator|.
name|clearFetchContext
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

