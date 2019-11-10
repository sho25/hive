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
name|parse
package|;
end_package

begin_import
import|import
name|org
operator|.
name|antlr
operator|.
name|runtime
operator|.
name|tree
operator|.
name|Tree
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SemanticAnalyzerFactory
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|SemanticAnalyzerFactory
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"SemanticAnalyzerFactory should not be instantiated"
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|BaseSemanticAnalyzer
name|get
parameter_list|(
name|QueryState
name|queryState
parameter_list|,
name|ASTNode
name|tree
parameter_list|)
throws|throws
name|SemanticException
block|{
name|BaseSemanticAnalyzer
name|sem
init|=
name|getInternal
argument_list|(
name|queryState
argument_list|,
name|tree
argument_list|)
decl_stmt|;
if|if
condition|(
name|queryState
operator|.
name|getHiveOperation
argument_list|()
operator|==
literal|null
condition|)
block|{
name|String
name|query
init|=
name|queryState
operator|.
name|getQueryString
argument_list|()
decl_stmt|;
if|if
condition|(
name|query
operator|!=
literal|null
operator|&&
name|query
operator|.
name|length
argument_list|()
operator|>
literal|30
condition|)
block|{
name|query
operator|=
name|query
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|30
argument_list|)
expr_stmt|;
block|}
name|String
name|msg
init|=
literal|"Unknown HiveOperation for query='"
operator|+
name|query
operator|+
literal|"' queryId="
operator|+
name|queryState
operator|.
name|getQueryId
argument_list|()
decl_stmt|;
comment|//throw new IllegalStateException(msg);
name|LOG
operator|.
name|debug
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
return|return
name|sem
return|;
block|}
specifier|private
specifier|static
name|BaseSemanticAnalyzer
name|getInternal
parameter_list|(
name|QueryState
name|queryState
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
name|HiveOperation
name|opType
init|=
name|HiveOperation
operator|.
name|operationForToken
argument_list|(
name|tree
operator|.
name|getType
argument_list|()
argument_list|)
decl_stmt|;
name|queryState
operator|.
name|setCommandType
argument_list|(
name|opType
argument_list|)
expr_stmt|;
if|if
condition|(
name|DDLSemanticAnalyzerFactory
operator|.
name|handles
argument_list|(
name|tree
operator|.
name|getType
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|DDLSemanticAnalyzerFactory
operator|.
name|getAnalyzer
argument_list|(
name|tree
argument_list|,
name|queryState
argument_list|)
return|;
block|}
switch|switch
condition|(
name|tree
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
name|queryState
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_EXPLAIN_SQ_REWRITE
case|:
return|return
operator|new
name|ExplainSQRewriteSemanticAnalyzer
argument_list|(
name|queryState
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
name|queryState
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_EXPORT
case|:
if|if
condition|(
name|AcidExportSemanticAnalyzer
operator|.
name|isAcidExport
argument_list|(
name|tree
argument_list|)
condition|)
block|{
return|return
operator|new
name|AcidExportSemanticAnalyzer
argument_list|(
name|queryState
argument_list|)
return|;
block|}
return|return
operator|new
name|ExportSemanticAnalyzer
argument_list|(
name|queryState
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
name|queryState
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_REPL_DUMP
case|:
return|return
operator|new
name|ReplicationSemanticAnalyzer
argument_list|(
name|queryState
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_REPL_LOAD
case|:
return|return
operator|new
name|ReplicationSemanticAnalyzer
argument_list|(
name|queryState
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_REPL_STATUS
case|:
return|return
operator|new
name|ReplicationSemanticAnalyzer
argument_list|(
name|queryState
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_ALTERTABLE
case|:
block|{
name|Tree
name|child
init|=
name|tree
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|queryState
operator|.
name|setCommandType
argument_list|(
name|HiveOperation
operator|.
name|operationForToken
argument_list|(
name|child
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|DDLSemanticAnalyzer
argument_list|(
name|queryState
argument_list|)
return|;
block|}
case|case
name|HiveParser
operator|.
name|TOK_ALTERVIEW
case|:
block|{
name|Tree
name|child
init|=
name|tree
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|child
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|HiveParser
operator|.
name|TOK_ALTERVIEW_PROPERTIES
case|:
case|case
name|HiveParser
operator|.
name|TOK_ALTERVIEW_DROPPROPERTIES
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
name|opType
operator|=
name|HiveOperation
operator|.
name|operationForToken
argument_list|(
name|child
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|queryState
operator|.
name|setCommandType
argument_list|(
name|opType
argument_list|)
expr_stmt|;
return|return
operator|new
name|DDLSemanticAnalyzer
argument_list|(
name|queryState
argument_list|)
return|;
block|}
comment|// TOK_ALTERVIEW_AS
assert|assert
name|child
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_QUERY
assert|;
name|queryState
operator|.
name|setCommandType
argument_list|(
name|HiveOperation
operator|.
name|ALTERVIEW_AS
argument_list|)
expr_stmt|;
return|return
operator|new
name|SemanticAnalyzer
argument_list|(
name|queryState
argument_list|)
return|;
block|}
case|case
name|HiveParser
operator|.
name|TOK_DROPTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DESCTABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_MSCK
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
name|TOK_SHOWPARTITIONS
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
name|TOK_SHOWVIEWS
case|:
case|case
name|HiveParser
operator|.
name|TOK_SHOWMATERIALIZEDVIEWS
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
name|TOK_TRUNCATETABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_CACHE_METADATA
case|:
return|return
operator|new
name|DDLSemanticAnalyzer
argument_list|(
name|queryState
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
name|queryState
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_UPDATE_TABLE
case|:
case|case
name|HiveParser
operator|.
name|TOK_DELETE_FROM
case|:
return|return
operator|new
name|UpdateDeleteSemanticAnalyzer
argument_list|(
name|queryState
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_MERGE
case|:
return|return
operator|new
name|MergeSemanticAnalyzer
argument_list|(
name|queryState
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_ALTER_SCHEDULED_QUERY
case|:
case|case
name|HiveParser
operator|.
name|TOK_CREATE_SCHEDULED_QUERY
case|:
case|case
name|HiveParser
operator|.
name|TOK_DROP_SCHEDULED_QUERY
case|:
return|return
operator|new
name|ScheduledQueryAnalyzer
argument_list|(
name|queryState
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_START_TRANSACTION
case|:
case|case
name|HiveParser
operator|.
name|TOK_COMMIT
case|:
case|case
name|HiveParser
operator|.
name|TOK_ROLLBACK
case|:
case|case
name|HiveParser
operator|.
name|TOK_SET_AUTOCOMMIT
case|:
default|default:
name|SemanticAnalyzer
name|semAnalyzer
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|queryState
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_CBO_ENABLED
argument_list|)
condition|?
operator|new
name|CalcitePlanner
argument_list|(
name|queryState
argument_list|)
else|:
operator|new
name|SemanticAnalyzer
argument_list|(
name|queryState
argument_list|)
decl_stmt|;
return|return
name|semAnalyzer
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

