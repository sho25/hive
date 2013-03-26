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
name|HashMap
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
name|metastore
operator|.
name|api
operator|.
name|InvalidOperationException
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
name|io
operator|.
name|RCFileInputFormat
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
name|RCFileOutputFormat
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
name|AbstractSemanticAnalyzerHook
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
name|DDLWork
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|rcfile
operator|.
name|RCFileInputDriver
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|rcfile
operator|.
name|RCFileOutputDriver
import|;
end_import

begin_class
specifier|public
class|class
name|AlterTableFileFormatHook
extends|extends
name|AbstractSemanticAnalyzerHook
block|{
specifier|private
name|String
name|inDriver
decl_stmt|,
name|outDriver
decl_stmt|,
name|tableName
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
name|String
name|inputFormat
init|=
literal|null
decl_stmt|,
name|outputFormat
init|=
literal|null
decl_stmt|;
name|tableName
operator|=
name|BaseSemanticAnalyzer
operator|.
name|unescapeIdentifier
argument_list|(
operator|(
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
argument_list|)
expr_stmt|;
name|ASTNode
name|child
init|=
call|(
name|ASTNode
call|)
argument_list|(
operator|(
name|ASTNode
operator|)
name|ast
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
name|TOK_TABLEFILEFORMAT
case|:
name|inputFormat
operator|=
name|BaseSemanticAnalyzer
operator|.
name|unescapeSQLString
argument_list|(
operator|(
operator|(
name|ASTNode
operator|)
name|child
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|getToken
argument_list|()
operator|.
name|getText
argument_list|()
argument_list|)
expr_stmt|;
name|outputFormat
operator|=
name|BaseSemanticAnalyzer
operator|.
name|unescapeSQLString
argument_list|(
operator|(
operator|(
name|ASTNode
operator|)
name|child
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|)
operator|.
name|getToken
argument_list|()
operator|.
name|getText
argument_list|()
argument_list|)
expr_stmt|;
name|inDriver
operator|=
name|BaseSemanticAnalyzer
operator|.
name|unescapeSQLString
argument_list|(
operator|(
operator|(
name|ASTNode
operator|)
name|child
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
operator|)
operator|.
name|getToken
argument_list|()
operator|.
name|getText
argument_list|()
argument_list|)
expr_stmt|;
name|outDriver
operator|=
name|BaseSemanticAnalyzer
operator|.
name|unescapeSQLString
argument_list|(
operator|(
operator|(
name|ASTNode
operator|)
name|child
operator|.
name|getChild
argument_list|(
literal|3
argument_list|)
operator|)
operator|.
name|getToken
argument_list|()
operator|.
name|getText
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_TBLSEQUENCEFILE
case|:
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Operation not supported. HCatalog doesn't support Sequence File by default yet. "
operator|+
literal|"You may specify it through INPUT/OUTPUT storage drivers."
argument_list|)
throw|;
case|case
name|HiveParser
operator|.
name|TOK_TBLTEXTFILE
case|:
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Operation not supported. HCatalog doesn't support Text File by default yet. "
operator|+
literal|"You may specify it through INPUT/OUTPUT storage drivers."
argument_list|)
throw|;
case|case
name|HiveParser
operator|.
name|TOK_TBLRCFILE
case|:
name|inputFormat
operator|=
name|RCFileInputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
expr_stmt|;
name|outputFormat
operator|=
name|RCFileOutputFormat
operator|.
name|class
operator|.
name|getName
argument_list|()
expr_stmt|;
name|inDriver
operator|=
name|RCFileInputDriver
operator|.
name|class
operator|.
name|getName
argument_list|()
expr_stmt|;
name|outDriver
operator|=
name|RCFileOutputDriver
operator|.
name|class
operator|.
name|getName
argument_list|()
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|inputFormat
operator|==
literal|null
operator|||
name|outputFormat
operator|==
literal|null
operator|||
name|inDriver
operator|==
literal|null
operator|||
name|outDriver
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"File format specification in command Alter Table file format is incorrect."
argument_list|)
throw|;
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
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
operator|(
operator|(
name|DDLWork
operator|)
name|rootTasks
operator|.
name|get
argument_list|(
name|rootTasks
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|getWork
argument_list|()
operator|)
operator|.
name|getAlterTblDesc
argument_list|()
operator|.
name|getPartSpec
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|howlProps
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|howlProps
operator|.
name|put
argument_list|(
name|HCatConstants
operator|.
name|HCAT_ISD_CLASS
argument_list|,
name|inDriver
argument_list|)
expr_stmt|;
name|howlProps
operator|.
name|put
argument_list|(
name|HCatConstants
operator|.
name|HCAT_OSD_CLASS
argument_list|,
name|outDriver
argument_list|)
expr_stmt|;
try|try
block|{
name|Hive
name|db
init|=
name|context
operator|.
name|getHive
argument_list|()
decl_stmt|;
name|Table
name|tbl
init|=
name|db
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|partSpec
operator|==
literal|null
condition|)
block|{
comment|// File format is for table; not for partition.
name|tbl
operator|.
name|getTTable
argument_list|()
operator|.
name|getParameters
argument_list|()
operator|.
name|putAll
argument_list|(
name|howlProps
argument_list|)
expr_stmt|;
name|db
operator|.
name|alterTable
argument_list|(
name|tableName
argument_list|,
name|tbl
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Partition
name|part
init|=
name|db
operator|.
name|getPartition
argument_list|(
name|tbl
argument_list|,
name|partSpec
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partParams
init|=
name|part
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|partParams
operator|==
literal|null
condition|)
block|{
name|partParams
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|partParams
operator|.
name|putAll
argument_list|(
name|howlProps
argument_list|)
expr_stmt|;
name|part
operator|.
name|getTPartition
argument_list|()
operator|.
name|setParameters
argument_list|(
name|partParams
argument_list|)
expr_stmt|;
name|db
operator|.
name|alterPartition
argument_list|(
name|tableName
argument_list|,
name|part
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|he
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|he
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InvalidOperationException
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
block|}
block|}
end_class

end_unit

