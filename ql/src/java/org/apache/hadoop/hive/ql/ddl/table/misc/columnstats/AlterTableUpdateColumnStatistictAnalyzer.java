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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|ddl
operator|.
name|table
operator|.
name|misc
operator|.
name|columnstats
package|;
end_package

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
name|TableName
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
name|Warehouse
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|ddl
operator|.
name|DDLSemanticAnalyzerFactory
operator|.
name|DDLType
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
name|ddl
operator|.
name|table
operator|.
name|AbstractAlterTableAnalyzer
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
name|ddl
operator|.
name|table
operator|.
name|AlterTableType
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
name|ColumnStatsUpdateTask
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
name|ASTNode
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
name|HiveParser
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
name|plan
operator|.
name|ColumnStatsUpdateWork
import|;
end_import

begin_comment
comment|/**  * Analyzer for update column statistics commands.  */
end_comment

begin_class
annotation|@
name|DDLType
argument_list|(
name|types
operator|=
block|{
name|HiveParser
operator|.
name|TOK_ALTERTABLE_UPDATECOLSTATS
block|,
name|HiveParser
operator|.
name|TOK_ALTERPARTITION_UPDATECOLSTATS
block|}
argument_list|)
specifier|public
class|class
name|AlterTableUpdateColumnStatistictAnalyzer
extends|extends
name|AbstractAlterTableAnalyzer
block|{
specifier|public
name|AlterTableUpdateColumnStatistictAnalyzer
parameter_list|(
name|QueryState
name|queryState
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|queryState
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|analyzeCommand
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|,
name|ASTNode
name|command
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Table
name|table
init|=
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|String
name|columnName
init|=
name|getUnescapedName
argument_list|(
operator|(
name|ASTNode
operator|)
name|command
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|properties
init|=
name|getProps
argument_list|(
call|(
name|ASTNode
call|)
argument_list|(
name|command
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|partitionName
init|=
name|getPartitionName
argument_list|(
name|partitionSpec
argument_list|)
decl_stmt|;
name|String
name|columnType
init|=
name|getColumnType
argument_list|(
name|table
argument_list|,
name|columnName
argument_list|)
decl_stmt|;
name|ColumnStatsUpdateWork
name|work
init|=
operator|new
name|ColumnStatsUpdateWork
argument_list|(
name|partitionName
argument_list|,
name|properties
argument_list|,
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|columnName
argument_list|,
name|columnType
argument_list|)
decl_stmt|;
name|ColumnStatsUpdateTask
name|task
init|=
operator|(
name|ColumnStatsUpdateTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
name|work
argument_list|)
decl_stmt|;
comment|// TODO: doesn't look like this path is actually ever exercised. Maybe this needs to be removed.
name|addInputsOutputsAlterTable
argument_list|(
name|tableName
argument_list|,
name|partitionSpec
argument_list|,
literal|null
argument_list|,
name|AlterTableType
operator|.
name|UPDATESTATS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|AcidUtils
operator|.
name|isTransactionalTable
argument_list|(
name|table
argument_list|)
condition|)
block|{
name|setAcidDdlDesc
argument_list|(
name|work
argument_list|)
expr_stmt|;
block|}
name|rootTasks
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|getPartitionName
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
name|partitionName
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|partitionSpec
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|partitionName
operator|=
name|Warehouse
operator|.
name|makePartName
argument_list|(
name|partitionSpec
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"partition "
operator|+
name|partitionSpec
operator|.
name|toString
argument_list|()
operator|+
literal|" not found"
argument_list|)
throw|;
block|}
block|}
return|return
name|partitionName
return|;
block|}
specifier|private
name|String
name|getColumnType
parameter_list|(
name|Table
name|table
parameter_list|,
name|String
name|columnName
parameter_list|)
throws|throws
name|SemanticException
block|{
for|for
control|(
name|FieldSchema
name|column
range|:
name|table
operator|.
name|getCols
argument_list|()
control|)
block|{
if|if
condition|(
name|columnName
operator|.
name|equalsIgnoreCase
argument_list|(
name|column
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|column
operator|.
name|getType
argument_list|()
return|;
block|}
block|}
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"column type not found"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

