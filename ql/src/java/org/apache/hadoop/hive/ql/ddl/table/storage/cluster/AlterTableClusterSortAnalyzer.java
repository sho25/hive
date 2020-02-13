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
name|storage
operator|.
name|cluster
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
name|Order
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
name|AbstractAlterTableDesc
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
comment|/**  * Analyzer for table cluster sort commands.  */
end_comment

begin_class
annotation|@
name|DDLType
argument_list|(
name|types
operator|=
name|HiveParser
operator|.
name|TOK_ALTERTABLE_CLUSTER_SORT
argument_list|)
specifier|public
class|class
name|AlterTableClusterSortAnalyzer
extends|extends
name|AbstractAlterTableAnalyzer
block|{
specifier|public
name|AlterTableClusterSortAnalyzer
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
name|AbstractAlterTableDesc
name|desc
decl_stmt|;
switch|switch
condition|(
name|command
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|HiveParser
operator|.
name|TOK_NOT_CLUSTERED
case|:
name|desc
operator|=
operator|new
name|AlterTableNotClusteredDesc
argument_list|(
name|tableName
argument_list|,
name|partitionSpec
argument_list|)
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_NOT_SORTED
case|:
name|desc
operator|=
operator|new
name|AlterTableNotSortedDesc
argument_list|(
name|tableName
argument_list|,
name|partitionSpec
argument_list|)
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_BUCKETS
case|:
name|ASTNode
name|buckets
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
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
init|=
name|getColumnNames
argument_list|(
operator|(
name|ASTNode
operator|)
name|buckets
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Order
argument_list|>
name|sortCols
init|=
operator|new
name|ArrayList
argument_list|<
name|Order
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|numBuckets
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|buckets
operator|.
name|getChildCount
argument_list|()
operator|==
literal|2
condition|)
block|{
name|numBuckets
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|buckets
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sortCols
operator|=
name|getColumnNamesOrder
argument_list|(
operator|(
name|ASTNode
operator|)
name|buckets
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|numBuckets
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|buckets
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|numBuckets
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_BUCKET_NUMBER
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
name|desc
operator|=
operator|new
name|AlterTableClusteredByDesc
argument_list|(
name|tableName
argument_list|,
name|partitionSpec
argument_list|,
name|numBuckets
argument_list|,
name|bucketCols
argument_list|,
name|sortCols
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Invalid operation "
operator|+
name|command
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|)
throw|;
block|}
name|addInputsOutputsAlterTable
argument_list|(
name|tableName
argument_list|,
name|partitionSpec
argument_list|,
name|desc
argument_list|,
name|desc
operator|.
name|getType
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
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

