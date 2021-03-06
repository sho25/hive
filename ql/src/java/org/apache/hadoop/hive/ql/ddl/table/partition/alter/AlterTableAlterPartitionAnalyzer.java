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
name|partition
operator|.
name|alter
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
name|ErrorMsg
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

begin_comment
comment|/**  * Analyzer for alter partition commands.  */
end_comment

begin_class
annotation|@
name|DDLType
argument_list|(
name|types
operator|=
name|HiveParser
operator|.
name|TOK_ALTERTABLE_PARTCOLTYPE
argument_list|)
specifier|public
class|class
name|AlterTableAlterPartitionAnalyzer
extends|extends
name|AbstractAlterTableAnalyzer
block|{
specifier|public
name|AlterTableAlterPartitionAnalyzer
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
name|validateAlterTableType
argument_list|(
name|table
argument_list|,
name|AlterTableType
operator|.
name|ALTERPARTITION
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|inputs
operator|.
name|add
argument_list|(
operator|new
name|ReadEntity
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
comment|// Alter table ... partition column ( column newtype) only takes one column at a time.
name|ASTNode
name|colAst
init|=
operator|(
name|ASTNode
operator|)
name|command
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|name
init|=
name|colAst
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
name|String
name|type
init|=
name|getTypeStringFromAST
argument_list|(
call|(
name|ASTNode
call|)
argument_list|(
name|colAst
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|comment
init|=
operator|(
name|colAst
operator|.
name|getChildCount
argument_list|()
operator|==
literal|3
operator|)
condition|?
name|unescapeSQLString
argument_list|(
name|colAst
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
else|:
literal|null
decl_stmt|;
name|FieldSchema
name|newCol
init|=
operator|new
name|FieldSchema
argument_list|(
name|unescapeIdentifier
argument_list|(
name|name
argument_list|)
argument_list|,
name|type
argument_list|,
name|comment
argument_list|)
decl_stmt|;
name|boolean
name|isDefined
init|=
literal|false
decl_stmt|;
for|for
control|(
name|FieldSchema
name|col
range|:
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getPartitionKeys
argument_list|()
control|)
block|{
if|if
condition|(
name|col
operator|.
name|getName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|newCol
operator|.
name|getName
argument_list|()
argument_list|)
operator|==
literal|0
condition|)
block|{
name|isDefined
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|isDefined
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_COLUMN
operator|.
name|getMsg
argument_list|(
name|newCol
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
name|AlterTableAlterPartitionDesc
name|desc
init|=
operator|new
name|AlterTableAlterPartitionDesc
argument_list|(
name|tableName
operator|.
name|getNotEmptyDbTable
argument_list|()
argument_list|,
name|newCol
argument_list|)
decl_stmt|;
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
name|desc
argument_list|)
expr_stmt|;
block|}
name|rootTasks
operator|.
name|add
argument_list|(
name|TaskFactory
operator|.
name|get
argument_list|(
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
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

