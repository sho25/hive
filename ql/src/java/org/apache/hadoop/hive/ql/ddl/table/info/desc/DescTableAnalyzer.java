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
name|info
operator|.
name|desc
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
name|table
operator|.
name|partition
operator|.
name|PartitionUtils
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
name|DDLUtils
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
name|InvalidTableException
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
name|Partition
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
comment|/**  * Analyzer for table describing commands.  *  * A query like this will generate a tree as follows  *   "describe formatted default.maptable partition (b=100) id;"  * TOK_TABTYPE  *   TOK_TABNAME --> root for tablename, 2 child nodes mean DB specified  *     default  *     maptable  *   TOK_PARTSPEC  --> root node for partition spec. else columnName  *     TOK_PARTVAL  *       b  *       100  *   id           --> root node for columnName  * formatted  */
end_comment

begin_class
annotation|@
name|DDLType
argument_list|(
name|types
operator|=
name|HiveParser
operator|.
name|TOK_DESCTABLE
argument_list|)
specifier|public
class|class
name|DescTableAnalyzer
extends|extends
name|BaseSemanticAnalyzer
block|{
specifier|public
name|DescTableAnalyzer
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
name|ctx
operator|.
name|setResFile
argument_list|(
name|ctx
operator|.
name|getLocalTmpPath
argument_list|()
argument_list|)
expr_stmt|;
name|ASTNode
name|tableTypeExpr
init|=
operator|(
name|ASTNode
operator|)
name|root
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|TableName
name|tableName
init|=
name|getQualifiedTableName
argument_list|(
operator|(
name|ASTNode
operator|)
name|tableTypeExpr
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
comment|// if database is not the one currently using validate database
if|if
condition|(
name|tableName
operator|.
name|getDb
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|db
operator|.
name|validateDatabaseExists
argument_list|(
name|tableName
operator|.
name|getDb
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Table
name|table
init|=
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// process the second child, if exists, node to get partition spec(s)
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
init|=
name|getPartitionSpec
argument_list|(
name|db
argument_list|,
name|tableTypeExpr
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|partitionSpec
operator|!=
literal|null
condition|)
block|{
comment|// validate that partition exists
name|PartitionUtils
operator|.
name|getPartition
argument_list|(
name|db
argument_list|,
name|table
argument_list|,
name|partitionSpec
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// process the third child node,if exists, to get partition spec(s)
name|String
name|columnPath
init|=
name|getColumnPath
argument_list|(
name|db
argument_list|,
name|tableTypeExpr
argument_list|,
name|tableName
argument_list|,
name|partitionSpec
argument_list|)
decl_stmt|;
name|boolean
name|showColStats
init|=
literal|false
decl_stmt|;
name|boolean
name|isFormatted
init|=
literal|false
decl_stmt|;
name|boolean
name|isExt
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|root
operator|.
name|getChildCount
argument_list|()
operator|==
literal|2
condition|)
block|{
name|int
name|descOptions
init|=
name|root
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getType
argument_list|()
decl_stmt|;
name|isFormatted
operator|=
name|descOptions
operator|==
name|HiveParser
operator|.
name|KW_FORMATTED
expr_stmt|;
name|isExt
operator|=
name|descOptions
operator|==
name|HiveParser
operator|.
name|KW_EXTENDED
expr_stmt|;
comment|// in case of "DESCRIBE FORMATTED tablename column_name" statement, colPath will contain tablename.column_name.
comment|// If column_name is not specified colPath will be equal to tableName.
comment|// This is how we can differentiate if we are describing a table or column.
if|if
condition|(
name|columnPath
operator|!=
literal|null
operator|&&
name|isFormatted
condition|)
block|{
name|showColStats
operator|=
literal|true
expr_stmt|;
block|}
block|}
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
name|DescTableDesc
name|desc
init|=
operator|new
name|DescTableDesc
argument_list|(
name|ctx
operator|.
name|getResFile
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|partitionSpec
argument_list|,
name|columnPath
argument_list|,
name|isExt
argument_list|,
name|isFormatted
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|task
init|=
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
decl_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
name|task
operator|.
name|setFetchSource
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|String
name|schema
init|=
name|showColStats
condition|?
name|DescTableDesc
operator|.
name|COLUMN_STATISTICS_SCHEMA
else|:
name|DescTableDesc
operator|.
name|SCHEMA
decl_stmt|;
name|setFetchTask
argument_list|(
name|createFetchTask
argument_list|(
name|schema
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get the column path.    * Return column name if exists, column could be DOT separated.    * Example: lintString.$elem$.myint.    * Return table name for column name if no column has been specified.    */
specifier|private
name|String
name|getColumnPath
parameter_list|(
name|Hive
name|db
parameter_list|,
name|ASTNode
name|node
parameter_list|,
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
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// if this ast has only one child, then no column name specified.
if|if
condition|(
name|node
operator|.
name|getChildCount
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Second child node could be partitionSpec or column
if|if
condition|(
name|node
operator|.
name|getChildCount
argument_list|()
operator|>
literal|1
condition|)
block|{
name|ASTNode
name|columnNode
init|=
operator|(
name|partitionSpec
operator|==
literal|null
operator|)
condition|?
operator|(
name|ASTNode
operator|)
name|node
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
else|:
operator|(
name|ASTNode
operator|)
name|node
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
decl_stmt|;
if|if
condition|(
name|columnNode
operator|!=
literal|null
condition|)
block|{
return|return
name|String
operator|.
name|join
argument_list|(
literal|"."
argument_list|,
name|tableName
operator|.
name|getNotEmptyDbTable
argument_list|()
argument_list|,
name|DDLUtils
operator|.
name|getFQName
argument_list|(
name|columnNode
argument_list|)
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartitionSpec
parameter_list|(
name|Hive
name|db
parameter_list|,
name|ASTNode
name|node
parameter_list|,
name|TableName
name|tableName
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// if this node has only one child, then no partition spec specified.
if|if
condition|(
name|node
operator|.
name|getChildCount
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// if ast has two children the 2nd child could be partition spec or columnName
comment|// if the ast has 3 children, the second *has to* be partition spec
if|if
condition|(
name|node
operator|.
name|getChildCount
argument_list|()
operator|>
literal|2
operator|&&
operator|(
operator|(
operator|(
name|ASTNode
operator|)
name|node
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|)
operator|.
name|getType
argument_list|()
operator|!=
name|HiveParser
operator|.
name|TOK_PARTSPEC
operator|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
operator|(
operator|(
name|ASTNode
operator|)
name|node
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|)
operator|.
name|getType
argument_list|()
operator|+
literal|" is not a partition specification"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|(
operator|(
name|ASTNode
operator|)
name|node
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|)
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_PARTSPEC
condition|)
block|{
name|ASTNode
name|partNode
init|=
operator|(
name|ASTNode
operator|)
name|node
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Table
name|tab
init|=
literal|null
decl_stmt|;
try|try
block|{
name|tab
operator|=
name|db
operator|.
name|getTable
argument_list|(
name|tableName
operator|.
name|getNotEmptyDbTable
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidTableException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_TABLE
operator|.
name|getMsg
argument_list|(
name|tableName
operator|.
name|getNotEmptyDbTable
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
init|=
literal|null
decl_stmt|;
try|try
block|{
name|partitionSpec
operator|=
name|getValidatedPartSpec
argument_list|(
name|tab
argument_list|,
name|partNode
argument_list|,
name|db
operator|.
name|getConf
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SemanticException
name|e
parameter_list|)
block|{
comment|// get exception in resolving partition it could be DESCRIBE table key
comment|// return null, continue processing for DESCRIBE table key
return|return
literal|null
return|;
block|}
if|if
condition|(
name|partitionSpec
operator|!=
literal|null
condition|)
block|{
name|Partition
name|part
init|=
literal|null
decl_stmt|;
try|try
block|{
name|part
operator|=
name|db
operator|.
name|getPartition
argument_list|(
name|tab
argument_list|,
name|partitionSpec
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
comment|// if get exception in finding partition it could be DESCRIBE table key
comment|// return null, continue processing for DESCRIBE table key
return|return
literal|null
return|;
block|}
if|if
condition|(
name|part
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_PARTITION
operator|.
name|getMsg
argument_list|(
name|partitionSpec
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|partitionSpec
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

