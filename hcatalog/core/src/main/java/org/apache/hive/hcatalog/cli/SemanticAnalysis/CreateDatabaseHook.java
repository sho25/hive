begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|cli
operator|.
name|SemanticAnalysis
package|;
end_package

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
name|Database
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
name|HiveSemanticAnalyzerHookContext
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
name|CreateDatabaseDesc
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
name|security
operator|.
name|authorization
operator|.
name|Privilege
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_class
specifier|final
class|class
name|CreateDatabaseHook
extends|extends
name|HCatSemanticAnalyzerBase
block|{
name|String
name|databaseName
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ASTNode
name|preAnalyze
parameter_list|(
name|HiveSemanticAnalyzerHookContext
name|context
parameter_list|,
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Hive
name|db
decl_stmt|;
try|try
block|{
name|db
operator|=
name|context
operator|.
name|getHive
argument_list|()
expr_stmt|;
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
literal|"Couldn't get Hive DB instance in semantic analysis phase."
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// Analyze and create tbl properties object
name|int
name|numCh
init|=
name|ast
operator|.
name|getChildCount
argument_list|()
decl_stmt|;
name|databaseName
operator|=
name|BaseSemanticAnalyzer
operator|.
name|getUnescapedName
argument_list|(
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|num
init|=
literal|1
init|;
name|num
operator|<
name|numCh
condition|;
name|num
operator|++
control|)
block|{
name|ASTNode
name|child
init|=
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
name|num
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|child
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
name|TOK_IFNOTEXISTS
case|:
try|try
block|{
name|List
argument_list|<
name|String
argument_list|>
name|dbs
init|=
name|db
operator|.
name|getDatabasesByPattern
argument_list|(
name|databaseName
argument_list|)
decl_stmt|;
if|if
condition|(
name|dbs
operator|!=
literal|null
operator|&&
name|dbs
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// db exists
return|return
name|ast
return|;
block|}
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
argument_list|)
throw|;
block|}
break|break;
block|}
block|}
return|return
name|ast
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postAnalyze
parameter_list|(
name|HiveSemanticAnalyzerHookContext
name|context
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|)
throws|throws
name|SemanticException
block|{
name|context
operator|.
name|getConf
argument_list|()
operator|.
name|set
argument_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_DB_NAME
argument_list|,
name|databaseName
argument_list|)
expr_stmt|;
name|super
operator|.
name|postAnalyze
argument_list|(
name|context
argument_list|,
name|rootTasks
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|authorizeDDLWork
parameter_list|(
name|HiveSemanticAnalyzerHookContext
name|context
parameter_list|,
name|Hive
name|hive
parameter_list|,
name|DDLWork
name|work
parameter_list|)
throws|throws
name|HiveException
block|{
name|CreateDatabaseDesc
name|createDb
init|=
name|work
operator|.
name|getCreateDatabaseDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|createDb
operator|!=
literal|null
condition|)
block|{
name|Database
name|db
init|=
operator|new
name|Database
argument_list|(
name|createDb
operator|.
name|getName
argument_list|()
argument_list|,
name|createDb
operator|.
name|getComment
argument_list|()
argument_list|,
name|createDb
operator|.
name|getLocationUri
argument_list|()
argument_list|,
name|createDb
operator|.
name|getDatabaseProperties
argument_list|()
argument_list|)
decl_stmt|;
name|authorize
argument_list|(
name|db
argument_list|,
name|Privilege
operator|.
name|CREATE
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

