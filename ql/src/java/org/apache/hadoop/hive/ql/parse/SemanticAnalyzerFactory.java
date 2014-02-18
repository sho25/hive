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
name|parse
package|;
end_package

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
name|plan
operator|.
name|HiveOperation
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
name|session
operator|.
name|SessionState
import|;
end_import

begin_comment
comment|/**  * SemanticAnalyzerFactory.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|SemanticAnalyzerFactory
block|{
specifier|static
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|HiveOperation
argument_list|>
name|commandType
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|HiveOperation
argument_list|>
argument_list|()
decl_stmt|;
specifier|static
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|HiveOperation
index|[]
argument_list|>
name|tablePartitionCommandType
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|HiveOperation
index|[]
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_EXPLAIN
argument_list|,
name|HiveOperation
operator|.
name|EXPLAIN
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_LOAD
argument_list|,
name|HiveOperation
operator|.
name|LOAD
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_EXPORT
argument_list|,
name|HiveOperation
operator|.
name|EXPORT
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_IMPORT
argument_list|,
name|HiveOperation
operator|.
name|IMPORT
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_CREATEDATABASE
argument_list|,
name|HiveOperation
operator|.
name|CREATEDATABASE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DROPDATABASE
argument_list|,
name|HiveOperation
operator|.
name|DROPDATABASE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SWITCHDATABASE
argument_list|,
name|HiveOperation
operator|.
name|SWITCHDATABASE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_CREATETABLE
argument_list|,
name|HiveOperation
operator|.
name|CREATETABLE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_TRUNCATETABLE
argument_list|,
name|HiveOperation
operator|.
name|TRUNCATETABLE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DROPTABLE
argument_list|,
name|HiveOperation
operator|.
name|DROPTABLE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DESCTABLE
argument_list|,
name|HiveOperation
operator|.
name|DESCTABLE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DESCFUNCTION
argument_list|,
name|HiveOperation
operator|.
name|DESCFUNCTION
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_MSCK
argument_list|,
name|HiveOperation
operator|.
name|MSCK
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDCOLS
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_ADDCOLS
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_REPLACECOLS
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_REPLACECOLS
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_RENAMECOL
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_RENAMECOL
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_RENAME
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_RENAME
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_DROPPARTS
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_DROPPARTS
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDPARTS
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_ADDPARTS
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_TOUCH
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_TOUCH
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ARCHIVE
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_ARCHIVE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_UNARCHIVE
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_UNARCHIVE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_PROPERTIES
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_PROPERTIES
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DROPTABLE_PROPERTIES
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_PROPERTIES
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOWDATABASES
argument_list|,
name|HiveOperation
operator|.
name|SHOWDATABASES
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOWTABLES
argument_list|,
name|HiveOperation
operator|.
name|SHOWTABLES
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOWCOLUMNS
argument_list|,
name|HiveOperation
operator|.
name|SHOWCOLUMNS
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOW_TABLESTATUS
argument_list|,
name|HiveOperation
operator|.
name|SHOW_TABLESTATUS
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOW_TBLPROPERTIES
argument_list|,
name|HiveOperation
operator|.
name|SHOW_TBLPROPERTIES
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOW_CREATETABLE
argument_list|,
name|HiveOperation
operator|.
name|SHOW_CREATETABLE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOWFUNCTIONS
argument_list|,
name|HiveOperation
operator|.
name|SHOWFUNCTIONS
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOWINDEXES
argument_list|,
name|HiveOperation
operator|.
name|SHOWINDEXES
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOWPARTITIONS
argument_list|,
name|HiveOperation
operator|.
name|SHOWPARTITIONS
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOWLOCKS
argument_list|,
name|HiveOperation
operator|.
name|SHOWLOCKS
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOWDBLOCKS
argument_list|,
name|HiveOperation
operator|.
name|SHOWLOCKS
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOWCONF
argument_list|,
name|HiveOperation
operator|.
name|SHOWCONF
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_CREATEFUNCTION
argument_list|,
name|HiveOperation
operator|.
name|CREATEFUNCTION
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DROPFUNCTION
argument_list|,
name|HiveOperation
operator|.
name|DROPFUNCTION
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_CREATEMACRO
argument_list|,
name|HiveOperation
operator|.
name|CREATEMACRO
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DROPMACRO
argument_list|,
name|HiveOperation
operator|.
name|DROPMACRO
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_CREATEVIEW
argument_list|,
name|HiveOperation
operator|.
name|CREATEVIEW
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DROPVIEW
argument_list|,
name|HiveOperation
operator|.
name|DROPVIEW
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_CREATEINDEX
argument_list|,
name|HiveOperation
operator|.
name|CREATEINDEX
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DROPINDEX
argument_list|,
name|HiveOperation
operator|.
name|DROPINDEX
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERINDEX_REBUILD
argument_list|,
name|HiveOperation
operator|.
name|ALTERINDEX_REBUILD
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERINDEX_PROPERTIES
argument_list|,
name|HiveOperation
operator|.
name|ALTERINDEX_PROPS
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERVIEW_PROPERTIES
argument_list|,
name|HiveOperation
operator|.
name|ALTERVIEW_PROPERTIES
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DROPVIEW_PROPERTIES
argument_list|,
name|HiveOperation
operator|.
name|ALTERVIEW_PROPERTIES
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERVIEW_ADDPARTS
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_ADDPARTS
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERVIEW_DROPPARTS
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_DROPPARTS
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_QUERY
argument_list|,
name|HiveOperation
operator|.
name|QUERY
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_LOCKTABLE
argument_list|,
name|HiveOperation
operator|.
name|LOCKTABLE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_UNLOCKTABLE
argument_list|,
name|HiveOperation
operator|.
name|UNLOCKTABLE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_LOCKDB
argument_list|,
name|HiveOperation
operator|.
name|LOCKDB
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_UNLOCKDB
argument_list|,
name|HiveOperation
operator|.
name|UNLOCKDB
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_CREATEROLE
argument_list|,
name|HiveOperation
operator|.
name|CREATEROLE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DROPROLE
argument_list|,
name|HiveOperation
operator|.
name|DROPROLE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_GRANT
argument_list|,
name|HiveOperation
operator|.
name|GRANT_PRIVILEGE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_REVOKE
argument_list|,
name|HiveOperation
operator|.
name|REVOKE_PRIVILEGE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOW_GRANT
argument_list|,
name|HiveOperation
operator|.
name|SHOW_GRANT
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_GRANT_ROLE
argument_list|,
name|HiveOperation
operator|.
name|GRANT_ROLE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_REVOKE_ROLE
argument_list|,
name|HiveOperation
operator|.
name|REVOKE_ROLE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOW_ROLES
argument_list|,
name|HiveOperation
operator|.
name|SHOW_ROLES
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOW_SET_ROLE
argument_list|,
name|HiveOperation
operator|.
name|SHOW_ROLES
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_SHOW_ROLE_GRANT
argument_list|,
name|HiveOperation
operator|.
name|SHOW_ROLE_GRANT
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERDATABASE_PROPERTIES
argument_list|,
name|HiveOperation
operator|.
name|ALTERDATABASE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_DESCDATABASE
argument_list|,
name|HiveOperation
operator|.
name|DESCDATABASE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_SKEWED
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_SKEWED
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ANALYZE
argument_list|,
name|HiveOperation
operator|.
name|ANALYZE_TABLE
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERVIEW_RENAME
argument_list|,
name|HiveOperation
operator|.
name|ALTERVIEW_RENAME
argument_list|)
expr_stmt|;
name|commandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_PARTCOLTYPE
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_PARTCOLTYPE
argument_list|)
expr_stmt|;
block|}
static|static
block|{
name|tablePartitionCommandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_PROTECTMODE
argument_list|,
operator|new
name|HiveOperation
index|[]
block|{
name|HiveOperation
operator|.
name|ALTERTABLE_PROTECTMODE
block|,
name|HiveOperation
operator|.
name|ALTERPARTITION_PROTECTMODE
block|}
argument_list|)
expr_stmt|;
name|tablePartitionCommandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_FILEFORMAT
argument_list|,
operator|new
name|HiveOperation
index|[]
block|{
name|HiveOperation
operator|.
name|ALTERTABLE_FILEFORMAT
block|,
name|HiveOperation
operator|.
name|ALTERPARTITION_FILEFORMAT
block|}
argument_list|)
expr_stmt|;
name|tablePartitionCommandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_LOCATION
argument_list|,
operator|new
name|HiveOperation
index|[]
block|{
name|HiveOperation
operator|.
name|ALTERTABLE_LOCATION
block|,
name|HiveOperation
operator|.
name|ALTERPARTITION_LOCATION
block|}
argument_list|)
expr_stmt|;
name|tablePartitionCommandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_MERGEFILES
argument_list|,
operator|new
name|HiveOperation
index|[]
block|{
name|HiveOperation
operator|.
name|ALTERTABLE_MERGEFILES
block|,
name|HiveOperation
operator|.
name|ALTERPARTITION_MERGEFILES
block|}
argument_list|)
expr_stmt|;
name|tablePartitionCommandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_SERIALIZER
argument_list|,
operator|new
name|HiveOperation
index|[]
block|{
name|HiveOperation
operator|.
name|ALTERTABLE_SERIALIZER
block|,
name|HiveOperation
operator|.
name|ALTERPARTITION_SERIALIZER
block|}
argument_list|)
expr_stmt|;
name|tablePartitionCommandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_SERDEPROPERTIES
argument_list|,
operator|new
name|HiveOperation
index|[]
block|{
name|HiveOperation
operator|.
name|ALTERTABLE_SERDEPROPERTIES
block|,
name|HiveOperation
operator|.
name|ALTERPARTITION_SERDEPROPERTIES
block|}
argument_list|)
expr_stmt|;
name|tablePartitionCommandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_RENAMEPART
argument_list|,
operator|new
name|HiveOperation
index|[]
block|{
literal|null
block|,
name|HiveOperation
operator|.
name|ALTERTABLE_RENAMEPART
block|}
argument_list|)
expr_stmt|;
name|tablePartitionCommandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTBLPART_SKEWED_LOCATION
argument_list|,
operator|new
name|HiveOperation
index|[]
block|{
name|HiveOperation
operator|.
name|ALTERTBLPART_SKEWED_LOCATION
block|,
name|HiveOperation
operator|.
name|ALTERTBLPART_SKEWED_LOCATION
block|}
argument_list|)
expr_stmt|;
name|tablePartitionCommandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_TABLEBUCKETS
argument_list|,
operator|new
name|HiveOperation
index|[]
block|{
name|HiveOperation
operator|.
name|ALTERTABLE_BUCKETNUM
block|,
name|HiveOperation
operator|.
name|ALTERPARTITION_BUCKETNUM
block|}
argument_list|)
expr_stmt|;
name|tablePartitionCommandType
operator|.
name|put
argument_list|(
name|HiveParser
operator|.
name|TOK_ALTERTABLE_CLUSTER_SORT
argument_list|,
operator|new
name|HiveOperation
index|[]
block|{
name|HiveOperation
operator|.
name|ALTERTABLE_CLUSTER_SORT
block|,
name|HiveOperation
operator|.
name|ALTERTABLE_CLUSTER_SORT
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|BaseSemanticAnalyzer
name|get
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|ASTNode
name|tree
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|tree
operator|.
name|getToken
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Empty Syntax Tree"
argument_list|)
throw|;
block|}
else|else
block|{
name|setSessionCommandType
argument_list|(
name|commandType
operator|.
name|get
argument_list|(
name|tree
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|tree
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
name|TOK_EXPLAIN
case|:
return|return
operator|new
name|ExplainSemanticAnalyzer
argument_list|(
name|conf
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_LOAD
case|:
return|return
operator|new
name|LoadSemanticAnalyzer
argument_list|(
name|conf
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_EXPORT
case|:
return|return
operator|new
name|ExportSemanticAnalyzer
argument_list|(
name|conf
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_IMPORT
case|:
return|return
operator|new
name|ImportSemanticAnalyzer
argument_list|(
name|conf
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_CREATEDATABASE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPDATABASE
case|:
case|case
name|HiveParser
operator|.
name|TOK_SWITCHDATABASE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPVIEW
case|:
case|case
name|HiveParser
operator|.
name|TOK_DESCDATABASE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DESCTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DESCFUNCTION
case|:
case|case
name|HiveParser
operator|.
name|TOK_MSCK
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDCOLS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_RENAMECOL
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_REPLACECOLS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_RENAME
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_DROPPARTS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ADDPARTS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_PROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPTABLE_PROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_SERIALIZER
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_SERDEPROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_PARTCOLTYPE
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERINDEX_REBUILD
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERINDEX_PROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERVIEW_PROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPVIEW_PROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERVIEW_ADDPARTS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERVIEW_DROPPARTS
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERVIEW_RENAME
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWDATABASES
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWTABLES
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWCOLUMNS
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOW_TABLESTATUS
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOW_TBLPROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOW_CREATETABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWFUNCTIONS
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWPARTITIONS
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWINDEXES
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWLOCKS
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWDBLOCKS
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWCONF
case|:
case|case
name|HiveParser
operator|.
name|TOK_CREATEINDEX
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPINDEX
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_CLUSTER_SORT
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_TOUCH
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_ARCHIVE
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_UNARCHIVE
case|:
case|case
name|HiveParser
operator|.
name|TOK_LOCKTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_UNLOCKTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_LOCKDB
case|:
case|case
name|HiveParser
operator|.
name|TOK_UNLOCKDB
case|:
case|case
name|HiveParser
operator|.
name|TOK_CREATEROLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPROLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_GRANT
case|:
case|case
name|HiveParser
operator|.
name|TOK_REVOKE
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOW_GRANT
case|:
case|case
name|HiveParser
operator|.
name|TOK_GRANT_ROLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_REVOKE_ROLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOW_ROLE_GRANT
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOW_ROLES
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERDATABASE_PROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_SKEWED
case|:
case|case
name|HiveParser
operator|.
name|TOK_TRUNCATETABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_EXCHANGEPARTITION
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOW_SET_ROLE
case|:
return|return
operator|new
name|DDLSemanticAnalyzer
argument_list|(
name|conf
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE_PARTITION
case|:
name|HiveOperation
name|commandType
init|=
literal|null
decl_stmt|;
name|Integer
name|type
init|=
operator|(
operator|(
name|ASTNode
operator|)
name|tree
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
name|getType
argument_list|()
decl_stmt|;
if|if
condition|(
name|tree
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getChildCount
argument_list|()
operator|>
literal|1
condition|)
block|{
name|commandType
operator|=
name|tablePartitionCommandType
operator|.
name|get
argument_list|(
name|type
argument_list|)
index|[
literal|1
index|]
expr_stmt|;
block|}
else|else
block|{
name|commandType
operator|=
name|tablePartitionCommandType
operator|.
name|get
argument_list|(
name|type
argument_list|)
index|[
literal|0
index|]
expr_stmt|;
block|}
name|setSessionCommandType
argument_list|(
name|commandType
argument_list|)
expr_stmt|;
return|return
operator|new
name|DDLSemanticAnalyzer
argument_list|(
name|conf
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_CREATEFUNCTION
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPFUNCTION
case|:
return|return
operator|new
name|FunctionSemanticAnalyzer
argument_list|(
name|conf
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_ANALYZE
case|:
return|return
operator|new
name|ColumnStatsSemanticAnalyzer
argument_list|(
name|conf
argument_list|,
name|tree
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_CREATEMACRO
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROPMACRO
case|:
return|return
operator|new
name|MacroSemanticAnalyzer
argument_list|(
name|conf
argument_list|)
return|;
default|default:
return|return
operator|new
name|SemanticAnalyzer
argument_list|(
name|conf
argument_list|)
return|;
block|}
block|}
block|}
specifier|private
specifier|static
name|void
name|setSessionCommandType
parameter_list|(
name|HiveOperation
name|commandType
parameter_list|)
block|{
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|setCommandType
argument_list|(
name|commandType
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|SemanticAnalyzerFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

