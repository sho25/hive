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
name|view
operator|.
name|materialized
operator|.
name|alter
operator|.
name|rewrite
package|;
end_package

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
name|DDLWork
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
name|DDLDesc
operator|.
name|DDLDescWithWriteId
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
name|view
operator|.
name|materialized
operator|.
name|update
operator|.
name|MaterializedViewUpdateDesc
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
name|hooks
operator|.
name|ReadEntity
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
name|WriteEntity
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

begin_comment
comment|/**  * Analyzer for alter materialized view rewrite commands.  */
end_comment

begin_class
annotation|@
name|DDLType
argument_list|(
name|type
operator|=
name|HiveParser
operator|.
name|TOK_ALTER_MATERIALIZED_VIEW_REWRITE
argument_list|)
specifier|public
class|class
name|AlterMaterializedViewRewriteAnalyzer
extends|extends
name|BaseSemanticAnalyzer
block|{
specifier|public
name|AlterMaterializedViewRewriteAnalyzer
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
specifier|private
name|DDLDescWithWriteId
name|ddlDescWithWriteId
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|analyzeInternal
parameter_list|(
name|ASTNode
name|root
parameter_list|)
throws|throws
name|SemanticException
block|{
name|TableName
name|tableName
init|=
name|getQualifiedTableName
argument_list|(
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
comment|// Value for the flag
name|boolean
name|rewriteEnable
decl_stmt|;
switch|switch
condition|(
name|root
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|HiveParser
operator|.
name|TOK_REWRITE_ENABLED
case|:
name|rewriteEnable
operator|=
literal|true
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_REWRITE_DISABLED
case|:
name|rewriteEnable
operator|=
literal|false
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Invalid alter materialized view expression"
argument_list|)
throw|;
block|}
comment|// It can be fully qualified name or use default database
name|Table
name|materializedViewTable
init|=
name|getTable
argument_list|(
name|tableName
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// One last test: if we are enabling the rewrite, we need to check that query
comment|// only uses transactional (MM and ACID) tables
if|if
condition|(
name|rewriteEnable
condition|)
block|{
for|for
control|(
name|String
name|tName
range|:
name|materializedViewTable
operator|.
name|getCreationMetadata
argument_list|()
operator|.
name|getTablesUsed
argument_list|()
control|)
block|{
name|Table
name|table
init|=
name|getTable
argument_list|(
name|tName
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|AcidUtils
operator|.
name|isTransactionalTable
argument_list|(
name|table
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Automatic rewriting for materialized view cannot be enabled if the "
operator|+
literal|"materialized view uses non-transactional tables"
argument_list|)
throw|;
block|}
block|}
block|}
name|AlterMaterializedViewRewriteDesc
name|desc
init|=
operator|new
name|AlterMaterializedViewRewriteDesc
argument_list|(
name|tableName
operator|.
name|getNotEmptyDbTable
argument_list|()
argument_list|,
name|rewriteEnable
argument_list|)
decl_stmt|;
if|if
condition|(
name|AcidUtils
operator|.
name|isTransactionalTable
argument_list|(
name|materializedViewTable
argument_list|)
condition|)
block|{
name|ddlDescWithWriteId
operator|=
name|desc
expr_stmt|;
block|}
name|inputs
operator|.
name|add
argument_list|(
operator|new
name|ReadEntity
argument_list|(
name|materializedViewTable
argument_list|)
argument_list|)
expr_stmt|;
name|outputs
operator|.
name|add
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|materializedViewTable
argument_list|,
name|WriteEntity
operator|.
name|WriteType
operator|.
name|DDL_EXCLUSIVE
argument_list|)
argument_list|)
expr_stmt|;
comment|// Create task for alterMVRewriteDesc
name|DDLWork
name|work
init|=
operator|new
name|DDLWork
argument_list|(
name|getInputs
argument_list|()
argument_list|,
name|getOutputs
argument_list|()
argument_list|,
name|desc
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|targetTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|work
argument_list|)
decl_stmt|;
comment|// Create task to update rewrite flag as dependant of previous one
name|MaterializedViewUpdateDesc
name|materializedViewUpdateDesc
init|=
operator|new
name|MaterializedViewUpdateDesc
argument_list|(
name|tableName
operator|.
name|getNotEmptyDbTable
argument_list|()
argument_list|,
name|rewriteEnable
argument_list|,
operator|!
name|rewriteEnable
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|DDLWork
name|updateDdlWork
init|=
operator|new
name|DDLWork
argument_list|(
name|getInputs
argument_list|()
argument_list|,
name|getOutputs
argument_list|()
argument_list|,
name|materializedViewUpdateDesc
argument_list|)
decl_stmt|;
name|targetTask
operator|.
name|addDependentTask
argument_list|(
name|TaskFactory
operator|.
name|get
argument_list|(
name|updateDdlWork
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
comment|// Add root task
name|rootTasks
operator|.
name|add
argument_list|(
name|targetTask
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|DDLDescWithWriteId
name|getAcidDdlDesc
parameter_list|()
block|{
return|return
name|ddlDescWithWriteId
return|;
block|}
block|}
end_class

end_unit

