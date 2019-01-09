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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

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
name|LinkedHashMap
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|TableType
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
name|Context
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
name|hooks
operator|.
name|Entity
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
name|lib
operator|.
name|Node
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
name|HiveUtils
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
name|metadata
operator|.
name|VirtualColumn
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
comment|/**  * A subclass of the {@link org.apache.hadoop.hive.ql.parse.SemanticAnalyzer} that just handles  * update, delete and merge statements.  It works by rewriting the updates and deletes into insert  * statements (since they are actually inserts) and then doing some patch up to make them work as  * updates and deletes instead.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|RewriteSemanticAnalyzer
extends|extends
name|SemanticAnalyzer
block|{
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|RewriteSemanticAnalyzer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|boolean
name|useSuper
init|=
literal|false
decl_stmt|;
name|RewriteSemanticAnalyzer
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
name|tree
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|useSuper
condition|)
block|{
name|super
operator|.
name|analyzeInternal
argument_list|(
name|tree
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|getTxnMgr
argument_list|()
operator|.
name|supportsAcid
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|ACID_OP_ON_NONACID_TXNMGR
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
name|analyze
argument_list|(
name|tree
argument_list|)
expr_stmt|;
name|cleanUpMetaColumnAccessControl
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
specifier|abstract
name|void
name|analyze
parameter_list|(
name|ASTNode
name|tree
parameter_list|)
throws|throws
name|SemanticException
function_decl|;
comment|/**    * Append list of partition columns to Insert statement, i.e. the 2nd set of partCol1,partCol2    * INSERT INTO T PARTITION(partCol1,partCol2...) SELECT col1, ... partCol1,partCol2...    * @param target target table    */
specifier|protected
name|void
name|addPartitionColsToSelect
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
parameter_list|,
name|StringBuilder
name|rewrittenQueryStr
parameter_list|,
name|ASTNode
name|target
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
name|targetName
init|=
name|target
operator|!=
literal|null
condition|?
name|getSimpleTableName
argument_list|(
name|target
argument_list|)
else|:
literal|null
decl_stmt|;
comment|// If the table is partitioned, we need to select the partition columns as well.
if|if
condition|(
name|partCols
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|FieldSchema
name|fschema
range|:
name|partCols
control|)
block|{
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
comment|//would be nice if there was a way to determine if quotes are needed
if|if
condition|(
name|targetName
operator|!=
literal|null
condition|)
block|{
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
name|targetName
argument_list|)
operator|.
name|append
argument_list|(
literal|'.'
argument_list|)
expr_stmt|;
block|}
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|fschema
operator|.
name|getName
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Assert that we are not asked to update a bucketing column or partition column.    * @param colName it's the A in "SET A = B"    */
specifier|protected
name|void
name|checkValidSetClauseTarget
parameter_list|(
name|ASTNode
name|colName
parameter_list|,
name|Table
name|targetTable
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
name|columnName
init|=
name|normalizeColName
argument_list|(
name|colName
operator|.
name|getText
argument_list|()
argument_list|)
decl_stmt|;
comment|// Make sure this isn't one of the partitioning columns, that's not supported.
for|for
control|(
name|FieldSchema
name|fschema
range|:
name|targetTable
operator|.
name|getPartCols
argument_list|()
control|)
block|{
if|if
condition|(
name|fschema
operator|.
name|getName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|columnName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|UPDATE_CANNOT_UPDATE_PART_VALUE
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|//updating bucket column should move row from one file to another - not supported
if|if
condition|(
name|targetTable
operator|.
name|getBucketCols
argument_list|()
operator|!=
literal|null
operator|&&
name|targetTable
operator|.
name|getBucketCols
argument_list|()
operator|.
name|contains
argument_list|(
name|columnName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|UPDATE_CANNOT_UPDATE_BUCKET_VALUE
argument_list|,
name|columnName
argument_list|)
throw|;
block|}
name|boolean
name|foundColumnInTargetTable
init|=
literal|false
decl_stmt|;
for|for
control|(
name|FieldSchema
name|col
range|:
name|targetTable
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
name|col
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|foundColumnInTargetTable
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|foundColumnInTargetTable
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_TARGET_COLUMN_IN_SET_CLAUSE
argument_list|,
name|colName
operator|.
name|getText
argument_list|()
argument_list|,
name|targetTable
operator|.
name|getFullyQualifiedName
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|ASTNode
name|findLHSofAssignment
parameter_list|(
name|ASTNode
name|assignment
parameter_list|)
block|{
assert|assert
name|assignment
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|EQUAL
operator|:
literal|"Expected set assignments to use equals operator but found "
operator|+
name|assignment
operator|.
name|getName
argument_list|()
assert|;
name|ASTNode
name|tableOrColTok
init|=
operator|(
name|ASTNode
operator|)
name|assignment
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
assert|assert
name|tableOrColTok
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_TABLE_OR_COL
operator|:
literal|"Expected left side of assignment to be table or column"
assert|;
name|ASTNode
name|colName
init|=
operator|(
name|ASTNode
operator|)
name|tableOrColTok
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
assert|assert
name|colName
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|Identifier
operator|:
literal|"Expected column name"
assert|;
return|return
name|colName
return|;
block|}
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|collectSetColumnsAndExpressions
parameter_list|(
name|ASTNode
name|setClause
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|setRCols
parameter_list|,
name|Table
name|targetTable
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// An update needs to select all of the columns, as we rewrite the entire row.  Also,
comment|// we need to figure out which columns we are going to replace.
assert|assert
name|setClause
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_SET_COLUMNS_CLAUSE
operator|:
literal|"Expected second child of update token to be set token"
assert|;
comment|// Get the children of the set clause, each of which should be a column assignment
name|List
argument_list|<
name|?
extends|extends
name|Node
argument_list|>
name|assignments
init|=
name|setClause
operator|.
name|getChildren
argument_list|()
decl_stmt|;
comment|// Must be deterministic order map for consistent q-test output across Java versions
name|Map
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|setCols
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
argument_list|(
name|assignments
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Node
name|a
range|:
name|assignments
control|)
block|{
name|ASTNode
name|assignment
init|=
operator|(
name|ASTNode
operator|)
name|a
decl_stmt|;
name|ASTNode
name|colName
init|=
name|findLHSofAssignment
argument_list|(
name|assignment
argument_list|)
decl_stmt|;
if|if
condition|(
name|setRCols
operator|!=
literal|null
condition|)
block|{
name|addSetRCols
argument_list|(
operator|(
name|ASTNode
operator|)
name|assignment
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|setRCols
argument_list|)
expr_stmt|;
block|}
name|checkValidSetClauseTarget
argument_list|(
name|colName
argument_list|,
name|targetTable
argument_list|)
expr_stmt|;
name|String
name|columnName
init|=
name|normalizeColName
argument_list|(
name|colName
operator|.
name|getText
argument_list|()
argument_list|)
decl_stmt|;
comment|// This means that in UPDATE T SET x = _something_
comment|// _something_ can be whatever is supported in SELECT _something_
name|setCols
operator|.
name|put
argument_list|(
name|columnName
argument_list|,
operator|(
name|ASTNode
operator|)
name|assignment
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|setCols
return|;
block|}
comment|/**    * @return the Metastore representation of the target table    */
specifier|protected
name|Table
name|getTargetTable
parameter_list|(
name|ASTNode
name|tabRef
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
name|getTable
argument_list|(
name|tabRef
argument_list|,
name|db
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * @param throwException if false, return null if table doesn't exist, else throw    */
specifier|protected
specifier|static
name|Table
name|getTable
parameter_list|(
name|ASTNode
name|tabRef
parameter_list|,
name|Hive
name|db
parameter_list|,
name|boolean
name|throwException
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
index|[]
name|tableName
decl_stmt|;
switch|switch
condition|(
name|tabRef
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|HiveParser
operator|.
name|TOK_TABREF
case|:
name|tableName
operator|=
name|getQualifiedTableName
argument_list|(
operator|(
name|ASTNode
operator|)
name|tabRef
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|HiveParser
operator|.
name|TOK_TABNAME
case|:
name|tableName
operator|=
name|getQualifiedTableName
argument_list|(
name|tabRef
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
name|raiseWrongType
argument_list|(
literal|"TOK_TABREF|TOK_TABNAME"
argument_list|,
name|tabRef
argument_list|)
throw|;
block|}
name|Table
name|mTable
decl_stmt|;
try|try
block|{
name|mTable
operator|=
name|db
operator|.
name|getTable
argument_list|(
name|tableName
index|[
literal|0
index|]
argument_list|,
name|tableName
index|[
literal|1
index|]
argument_list|,
name|throwException
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidTableException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to find table "
operator|+
name|getDotName
argument_list|(
name|tableName
argument_list|)
operator|+
literal|" got exception "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
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
name|getDotName
argument_list|(
name|tableName
argument_list|)
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to find table "
operator|+
name|getDotName
argument_list|(
name|tableName
argument_list|)
operator|+
literal|" got exception "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
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
return|return
name|mTable
return|;
block|}
comment|/**    *  Walk through all our inputs and set them to note that this read is part of an update or a delete.    */
specifier|protected
name|void
name|markReadEntityForUpdate
parameter_list|()
block|{
for|for
control|(
name|ReadEntity
name|input
range|:
name|inputs
control|)
block|{
if|if
condition|(
name|isWritten
argument_list|(
name|input
argument_list|)
condition|)
block|{
comment|//TODO: this is actually not adding anything since LockComponent uses a Trie to "promote" a lock
comment|//except by accident - when we have a partitioned target table we have a ReadEntity and WriteEntity
comment|//for the table, so we mark ReadEntity and then delete WriteEntity (replace with Partition entries)
comment|//so DbTxnManager skips Read lock on the ReadEntity....
name|input
operator|.
name|setUpdateOrDelete
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|//input.noLockNeeded()?
block|}
block|}
block|}
comment|/**    *  For updates, we need to set the column access info so that it contains information on    *  the columns we are updating.    *  (But not all the columns of the target table even though the rewritten query writes    *  all columns of target table since that is an implmentation detail).    */
specifier|protected
name|void
name|setUpAccessControlInfoForUpdate
parameter_list|(
name|Table
name|mTable
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|setCols
parameter_list|)
block|{
name|ColumnAccessInfo
name|cai
init|=
operator|new
name|ColumnAccessInfo
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|colName
range|:
name|setCols
operator|.
name|keySet
argument_list|()
control|)
block|{
name|cai
operator|.
name|add
argument_list|(
name|Table
operator|.
name|getCompleteName
argument_list|(
name|mTable
operator|.
name|getDbName
argument_list|()
argument_list|,
name|mTable
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|,
name|colName
argument_list|)
expr_stmt|;
block|}
name|setUpdateColumnAccessInfo
argument_list|(
name|cai
argument_list|)
expr_stmt|;
block|}
comment|/**    * We need to weed ROW__ID out of the input column info, as it doesn't make any sense to    * require the user to have authorization on that column.    */
specifier|private
name|void
name|cleanUpMetaColumnAccessControl
parameter_list|()
block|{
comment|//we do this for Update/Delete (incl Merge) because we introduce this column into the query
comment|//as part of rewrite
if|if
condition|(
name|columnAccessInfo
operator|!=
literal|null
condition|)
block|{
name|columnAccessInfo
operator|.
name|stripVirtualColumn
argument_list|(
name|VirtualColumn
operator|.
name|ROWID
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Parse the newly generated SQL statement to get a new AST.    */
specifier|protected
name|ReparseResult
name|parseRewrittenQuery
parameter_list|(
name|StringBuilder
name|rewrittenQueryStr
parameter_list|,
name|String
name|originalQuery
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// Set dynamic partitioning to nonstrict so that queries do not need any partition
comment|// references.
comment|// TODO: this may be a perf issue as it prevents the optimizer.. or not
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|DYNAMICPARTITIONINGMODE
argument_list|,
literal|"nonstrict"
argument_list|)
expr_stmt|;
comment|// Disable LLAP IO wrapper; doesn't propagate extra ACID columns correctly.
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_IO_ROW_WRAPPER_ENABLED
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Parse the rewritten query string
name|Context
name|rewrittenCtx
decl_stmt|;
try|try
block|{
name|rewrittenCtx
operator|=
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|rewrittenCtx
operator|.
name|setHDFSCleanup
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// We keep track of all the contexts that are created by this query
comment|// so we can clear them when we finish execution
name|ctx
operator|.
name|addRewrittenStatementContext
argument_list|(
name|rewrittenCtx
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|UPDATEDELETE_IO_ERROR
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
name|rewrittenCtx
operator|.
name|setExplainConfig
argument_list|(
name|ctx
operator|.
name|getExplainConfig
argument_list|()
argument_list|)
expr_stmt|;
name|rewrittenCtx
operator|.
name|setExplainPlan
argument_list|(
name|ctx
operator|.
name|isExplainPlan
argument_list|()
argument_list|)
expr_stmt|;
name|rewrittenCtx
operator|.
name|setStatsSource
argument_list|(
name|ctx
operator|.
name|getStatsSource
argument_list|()
argument_list|)
expr_stmt|;
name|rewrittenCtx
operator|.
name|setPlanMapper
argument_list|(
name|ctx
operator|.
name|getPlanMapper
argument_list|()
argument_list|)
expr_stmt|;
name|rewrittenCtx
operator|.
name|setIsUpdateDeleteMerge
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|rewrittenCtx
operator|.
name|setCmd
argument_list|(
name|rewrittenQueryStr
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ASTNode
name|rewrittenTree
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Going to reparse<"
operator|+
name|originalQuery
operator|+
literal|"> as \n<"
operator|+
name|rewrittenQueryStr
operator|.
name|toString
argument_list|()
operator|+
literal|">"
argument_list|)
expr_stmt|;
name|rewrittenTree
operator|=
name|ParseUtils
operator|.
name|parse
argument_list|(
name|rewrittenQueryStr
operator|.
name|toString
argument_list|()
argument_list|,
name|rewrittenCtx
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|UPDATEDELETE_PARSE_ERROR
operator|.
name|getMsg
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
operator|new
name|ReparseResult
argument_list|(
name|rewrittenTree
argument_list|,
name|rewrittenCtx
argument_list|)
return|;
block|}
comment|/**    * Assert it supports Acid write.    */
specifier|protected
name|void
name|validateTargetTable
parameter_list|(
name|Table
name|mTable
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|mTable
operator|.
name|getTableType
argument_list|()
operator|==
name|TableType
operator|.
name|VIRTUAL_VIEW
operator|||
name|mTable
operator|.
name|getTableType
argument_list|()
operator|==
name|TableType
operator|.
name|MATERIALIZED_VIEW
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Table "
operator|+
name|mTable
operator|.
name|getFullyQualifiedName
argument_list|()
operator|+
literal|" is a view or materialized view"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|UPDATE_DELETE_VIEW
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**    * Check that {@code readEntity} is also being written.    */
specifier|private
name|boolean
name|isWritten
parameter_list|(
name|Entity
name|readEntity
parameter_list|)
block|{
for|for
control|(
name|Entity
name|writeEntity
range|:
name|outputs
control|)
block|{
comment|//make sure to compare them as Entity, i.e. that it's the same table or partition, etc
if|if
condition|(
name|writeEntity
operator|.
name|toString
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|readEntity
operator|.
name|toString
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|// This method finds any columns on the right side of a set statement (thus rcols) and puts them
comment|// in a set so we can add them to the list of input cols to check.
specifier|private
name|void
name|addSetRCols
parameter_list|(
name|ASTNode
name|node
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|setRCols
parameter_list|)
block|{
comment|// See if this node is a TOK_TABLE_OR_COL.  If so, find the value and put it in the list.  If
comment|// not, recurse on any children
if|if
condition|(
name|node
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_TABLE_OR_COL
condition|)
block|{
name|ASTNode
name|colName
init|=
operator|(
name|ASTNode
operator|)
name|node
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
assert|assert
name|colName
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|Identifier
operator|:
literal|"Expected column name"
assert|;
name|setRCols
operator|.
name|add
argument_list|(
name|normalizeColName
argument_list|(
name|colName
operator|.
name|getText
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|node
operator|.
name|getChildren
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Node
name|n
range|:
name|node
operator|.
name|getChildren
argument_list|()
control|)
block|{
name|addSetRCols
argument_list|(
operator|(
name|ASTNode
operator|)
name|n
argument_list|,
name|setRCols
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Column names are stored in metastore in lower case, regardless of the CREATE TABLE statement.    * Unfortunately there is no single place that normalizes the input query.    * @param colName not null    */
specifier|private
specifier|static
name|String
name|normalizeColName
parameter_list|(
name|String
name|colName
parameter_list|)
block|{
return|return
name|colName
operator|.
name|toLowerCase
argument_list|()
return|;
block|}
comment|/**    * SemanticAnalyzer will generate a WriteEntity for the target table since it doesn't know/check    * if the read and write are of the same table in "insert ... select ....".  Since DbTxnManager    * uses Read/WriteEntity objects to decide which locks to acquire, we get more concurrency if we    * have change the table WriteEntity to a set of partition WriteEntity objects based on    * ReadEntity objects computed for this table.    */
specifier|protected
name|void
name|updateOutputs
parameter_list|(
name|Table
name|targetTable
parameter_list|)
block|{
name|markReadEntityForUpdate
argument_list|()
expr_stmt|;
if|if
condition|(
name|targetTable
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|ReadEntity
argument_list|>
name|partitionsRead
init|=
name|getRestrictedPartitionSet
argument_list|(
name|targetTable
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|partitionsRead
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// if there is WriteEntity with WriteType=UPDATE/DELETE for target table, replace it with
comment|// WriteEntity for each partition
name|List
argument_list|<
name|WriteEntity
argument_list|>
name|toRemove
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|WriteEntity
name|we
range|:
name|outputs
control|)
block|{
name|WriteEntity
operator|.
name|WriteType
name|wt
init|=
name|we
operator|.
name|getWriteType
argument_list|()
decl_stmt|;
if|if
condition|(
name|isTargetTable
argument_list|(
name|we
argument_list|,
name|targetTable
argument_list|)
operator|&&
operator|(
name|wt
operator|==
name|WriteEntity
operator|.
name|WriteType
operator|.
name|UPDATE
operator|||
name|wt
operator|==
name|WriteEntity
operator|.
name|WriteType
operator|.
name|DELETE
operator|)
condition|)
block|{
comment|// The assumption here is that SemanticAnalyzer will will generate ReadEntity for each
comment|// partition that exists and is matched by the WHERE clause (which may be all of them).
comment|// Since we don't allow updating the value of a partition column, we know that we always
comment|// write the same (or fewer) partitions than we read.  Still, the write is a Dynamic
comment|// Partition write - see HIVE-15032.
name|toRemove
operator|.
name|add
argument_list|(
name|we
argument_list|)
expr_stmt|;
block|}
block|}
name|outputs
operator|.
name|removeAll
argument_list|(
name|toRemove
argument_list|)
expr_stmt|;
comment|// TODO: why is this like that?
for|for
control|(
name|ReadEntity
name|re
range|:
name|partitionsRead
control|)
block|{
for|for
control|(
name|WriteEntity
name|original
range|:
name|toRemove
control|)
block|{
comment|//since we may have both Update and Delete branches, Auth needs to know
name|WriteEntity
name|we
init|=
operator|new
name|WriteEntity
argument_list|(
name|re
operator|.
name|getPartition
argument_list|()
argument_list|,
name|original
operator|.
name|getWriteType
argument_list|()
argument_list|)
decl_stmt|;
name|we
operator|.
name|setDynamicPartitionWrite
argument_list|(
name|original
operator|.
name|isDynamicPartitionWrite
argument_list|()
argument_list|)
expr_stmt|;
name|outputs
operator|.
name|add
argument_list|(
name|we
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|/**    * If the optimizer has determined that it only has to read some of the partitions of the    * target table to satisfy the query, then we know that the write side of update/delete    * (and update/delete parts of merge)    * can only write (at most) that set of partitions (since we currently don't allow updating    * partition (or bucket) columns).  So we want to replace the table level    * WriteEntity in the outputs with WriteEntity for each of these partitions    * ToDo: see if this should be moved to SemanticAnalyzer itself since it applies to any    * insert which does a select against the same table.  Then SemanticAnalyzer would also    * be able to not use DP for the Insert...    *    * Note that the Insert of Merge may be creating new partitions and writing to partitions    * which were not read  (WHEN NOT MATCHED...).  WriteEntity for that should be created    * in MoveTask (or some other task after the query is complete).    */
specifier|private
name|List
argument_list|<
name|ReadEntity
argument_list|>
name|getRestrictedPartitionSet
parameter_list|(
name|Table
name|targetTable
parameter_list|)
block|{
name|List
argument_list|<
name|ReadEntity
argument_list|>
name|partitionsRead
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ReadEntity
name|re
range|:
name|inputs
control|)
block|{
if|if
condition|(
name|re
operator|.
name|isFromTopLevelQuery
operator|&&
name|re
operator|.
name|getType
argument_list|()
operator|==
name|Entity
operator|.
name|Type
operator|.
name|PARTITION
operator|&&
name|isTargetTable
argument_list|(
name|re
argument_list|,
name|targetTable
argument_list|)
condition|)
block|{
name|partitionsRead
operator|.
name|add
argument_list|(
name|re
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|partitionsRead
return|;
block|}
comment|/**    * Does this Entity belong to target table (partition).    */
specifier|private
name|boolean
name|isTargetTable
parameter_list|(
name|Entity
name|entity
parameter_list|,
name|Table
name|targetTable
parameter_list|)
block|{
comment|//todo: https://issues.apache.org/jira/browse/HIVE-15048
comment|/**      * is this the right way to compare?  Should it just compare paths?      * equals() impl looks heavy weight      */
return|return
name|targetTable
operator|.
name|equals
argument_list|(
name|entity
operator|.
name|getTable
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Returns the table name to use in the generated query preserving original quotes/escapes if any.    * @see #getFullTableNameForSQL(ASTNode)    */
specifier|protected
name|String
name|getSimpleTableName
parameter_list|(
name|ASTNode
name|n
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|getSimpleTableNameBase
argument_list|(
name|n
argument_list|)
argument_list|,
name|this
operator|.
name|conf
argument_list|)
return|;
block|}
specifier|protected
name|String
name|getSimpleTableNameBase
parameter_list|(
name|ASTNode
name|n
parameter_list|)
throws|throws
name|SemanticException
block|{
switch|switch
condition|(
name|n
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|HiveParser
operator|.
name|TOK_TABREF
case|:
name|int
name|aliasIndex
init|=
name|findTabRefIdxs
argument_list|(
name|n
argument_list|)
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|aliasIndex
operator|!=
literal|0
condition|)
block|{
return|return
name|n
operator|.
name|getChild
argument_list|(
name|aliasIndex
argument_list|)
operator|.
name|getText
argument_list|()
return|;
comment|//the alias
block|}
return|return
name|getSimpleTableNameBase
argument_list|(
operator|(
name|ASTNode
operator|)
name|n
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|)
return|;
case|case
name|HiveParser
operator|.
name|TOK_TABNAME
case|:
if|if
condition|(
name|n
operator|.
name|getChildCount
argument_list|()
operator|==
literal|2
condition|)
block|{
comment|//db.table -> return table
return|return
name|n
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getText
argument_list|()
return|;
block|}
return|return
name|n
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
operator|.
name|getText
argument_list|()
return|;
case|case
name|HiveParser
operator|.
name|TOK_SUBQUERY
case|:
return|return
name|n
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
operator|.
name|getText
argument_list|()
return|;
comment|//the alias
default|default:
throw|throw
name|raiseWrongType
argument_list|(
literal|"TOK_TABREF|TOK_TABNAME|TOK_SUBQUERY"
argument_list|,
name|n
argument_list|)
throw|;
block|}
block|}
specifier|protected
specifier|static
specifier|final
class|class
name|ReparseResult
block|{
specifier|final
name|ASTNode
name|rewrittenTree
decl_stmt|;
specifier|final
name|Context
name|rewrittenCtx
decl_stmt|;
name|ReparseResult
parameter_list|(
name|ASTNode
name|n
parameter_list|,
name|Context
name|c
parameter_list|)
block|{
name|rewrittenTree
operator|=
name|n
expr_stmt|;
name|rewrittenCtx
operator|=
name|c
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

