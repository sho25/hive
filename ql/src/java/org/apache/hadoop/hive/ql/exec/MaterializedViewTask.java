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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableSet
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
name|common
operator|.
name|ValidTxnList
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
name|CreationMetadata
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
name|metadata
operator|.
name|Hive
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
name|HiveMaterializedViewsRegistry
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
name|Table
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
name|ExplainConfiguration
operator|.
name|AnalyzeState
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
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_comment
comment|/**  * This task does some work related to materialized views. In particular, it adds  * or removes the materialized view from the registry if needed, or registers new  * creation metadata.  */
end_comment

begin_class
specifier|public
class|class
name|MaterializedViewTask
extends|extends
name|Task
argument_list|<
name|MaterializedViewDesc
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
specifier|public
name|MaterializedViewTask
parameter_list|()
block|{
name|super
argument_list|()
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
if|if
condition|(
name|driverContext
operator|.
name|getCtx
argument_list|()
operator|.
name|getExplainAnalyze
argument_list|()
operator|==
name|AnalyzeState
operator|.
name|RUNNING
condition|)
block|{
return|return
literal|0
return|;
block|}
try|try
block|{
if|if
condition|(
name|getWork
argument_list|()
operator|.
name|isRetrieveAndInclude
argument_list|()
condition|)
block|{
name|Hive
name|db
init|=
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Table
name|mvTable
init|=
name|db
operator|.
name|getTable
argument_list|(
name|getWork
argument_list|()
operator|.
name|getViewName
argument_list|()
argument_list|)
decl_stmt|;
name|HiveMaterializedViewsRegistry
operator|.
name|get
argument_list|()
operator|.
name|createMaterializedView
argument_list|(
name|db
operator|.
name|getConf
argument_list|()
argument_list|,
name|mvTable
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|getWork
argument_list|()
operator|.
name|isDisableRewrite
argument_list|()
condition|)
block|{
comment|// Disabling rewriting, removing from cache
name|String
index|[]
name|names
init|=
name|getWork
argument_list|()
operator|.
name|getViewName
argument_list|()
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
decl_stmt|;
name|HiveMaterializedViewsRegistry
operator|.
name|get
argument_list|()
operator|.
name|dropMaterializedView
argument_list|(
name|names
index|[
literal|0
index|]
argument_list|,
name|names
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|getWork
argument_list|()
operator|.
name|isUpdateCreationMetadata
argument_list|()
condition|)
block|{
comment|// We need to update the status of the creation signature
name|Hive
name|db
init|=
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Table
name|mvTable
init|=
name|db
operator|.
name|getTable
argument_list|(
name|getWork
argument_list|()
operator|.
name|getViewName
argument_list|()
argument_list|)
decl_stmt|;
name|CreationMetadata
name|cm
init|=
operator|new
name|CreationMetadata
argument_list|(
name|mvTable
operator|.
name|getDbName
argument_list|()
argument_list|,
name|mvTable
operator|.
name|getTableName
argument_list|()
argument_list|,
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
name|mvTable
operator|.
name|getCreationMetadata
argument_list|()
operator|.
name|getTablesUsed
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|cm
operator|.
name|setValidTxnList
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|ValidTxnList
operator|.
name|VALID_TXNS_KEY
argument_list|)
argument_list|)
expr_stmt|;
name|db
operator|.
name|updateCreationMetadata
argument_list|(
name|mvTable
operator|.
name|getDbName
argument_list|()
argument_list|,
name|mvTable
operator|.
name|getTableName
argument_list|()
argument_list|,
name|cm
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Exception during materialized view cache update"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|0
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
name|DDL
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
name|MaterializedViewTask
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
return|;
block|}
block|}
end_class

end_unit

