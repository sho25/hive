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
name|constraint
operator|.
name|add
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|SQLCheckConstraint
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
name|SQLForeignKey
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
name|SQLPrimaryKey
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
name|SQLUniqueConstraint
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
name|constraint
operator|.
name|Constraints
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
name|constraint
operator|.
name|ConstraintsUtils
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
comment|/**  * Analyzer for add constraint commands.  */
end_comment

begin_class
annotation|@
name|DDLType
argument_list|(
name|types
operator|=
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDCONSTRAINT
argument_list|)
specifier|public
class|class
name|AlterTableAddConstraintAnalyzer
extends|extends
name|AbstractAlterTableAnalyzer
block|{
specifier|public
name|AlterTableAddConstraintAnalyzer
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
comment|// TODO CAT - for now always use the default catalog.  Eventually will want to see if
comment|// the user specified a catalog
name|List
argument_list|<
name|SQLPrimaryKey
argument_list|>
name|primaryKeys
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|SQLForeignKey
argument_list|>
name|foreignKeys
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|SQLUniqueConstraint
argument_list|>
name|uniqueConstraints
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|SQLCheckConstraint
argument_list|>
name|checkConstraints
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ASTNode
name|constraintNode
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
switch|switch
condition|(
name|constraintNode
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|HiveParser
operator|.
name|TOK_UNIQUE
case|:
name|ConstraintsUtils
operator|.
name|processUniqueConstraints
argument_list|(
name|tableName
argument_list|,
name|constraintNode
argument_list|,
name|uniqueConstraints
argument_list|)
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_PRIMARY_KEY
case|:
name|ConstraintsUtils
operator|.
name|processPrimaryKeys
argument_list|(
name|tableName
argument_list|,
name|constraintNode
argument_list|,
name|primaryKeys
argument_list|)
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_FOREIGN_KEY
case|:
name|ConstraintsUtils
operator|.
name|processForeignKeys
argument_list|(
name|tableName
argument_list|,
name|constraintNode
argument_list|,
name|foreignKeys
argument_list|)
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_CHECK_CONSTRAINT
case|:
name|ConstraintsUtils
operator|.
name|processCheckConstraints
argument_list|(
name|tableName
argument_list|,
name|constraintNode
argument_list|,
literal|null
argument_list|,
name|checkConstraints
argument_list|,
name|command
argument_list|,
name|ctx
operator|.
name|getTokenRewriteStream
argument_list|()
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|NOT_RECOGNIZED_CONSTRAINT
operator|.
name|getMsg
argument_list|(
name|constraintNode
operator|.
name|getToken
argument_list|()
operator|.
name|getText
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
name|Constraints
name|constraints
init|=
operator|new
name|Constraints
argument_list|(
name|primaryKeys
argument_list|,
name|foreignKeys
argument_list|,
literal|null
argument_list|,
name|uniqueConstraints
argument_list|,
literal|null
argument_list|,
name|checkConstraints
argument_list|)
decl_stmt|;
name|AlterTableAddConstraintDesc
name|desc
init|=
operator|new
name|AlterTableAddConstraintDesc
argument_list|(
name|tableName
argument_list|,
literal|null
argument_list|,
name|constraints
argument_list|)
decl_stmt|;
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

