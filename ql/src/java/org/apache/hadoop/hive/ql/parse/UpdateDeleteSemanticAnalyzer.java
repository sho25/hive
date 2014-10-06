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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
comment|/**  * A subclass of the {@link org.apache.hadoop.hive.ql.parse.SemanticAnalyzer} that just handles  * update and delete statements.  It works by rewriting the updates and deletes into insert  * statements (since they are actually inserts) and then doing some patch up to make them work as  * updates and deletes instead.  */
end_comment

begin_class
specifier|public
class|class
name|UpdateDeleteSemanticAnalyzer
extends|extends
name|SemanticAnalyzer
block|{
name|boolean
name|useSuper
init|=
literal|false
decl_stmt|;
specifier|public
name|UpdateDeleteSemanticAnalyzer
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|conf
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
name|SessionState
operator|.
name|get
argument_list|()
operator|.
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
name|TOK_DELETE_FROM
case|:
name|analyzeDelete
argument_list|(
name|tree
argument_list|)
expr_stmt|;
return|return;
case|case
name|HiveParser
operator|.
name|TOK_UPDATE_TABLE
case|:
name|analyzeUpdate
argument_list|(
name|tree
argument_list|)
expr_stmt|;
return|return;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Asked to parse token "
operator|+
name|tree
operator|.
name|getName
argument_list|()
operator|+
literal|" in "
operator|+
literal|"UpdateDeleteSemanticAnalyzer"
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|updating
parameter_list|()
block|{
return|return
name|ctx
operator|.
name|getAcidOperation
argument_list|()
operator|==
name|AcidUtils
operator|.
name|Operation
operator|.
name|UPDATE
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|deleting
parameter_list|()
block|{
return|return
name|ctx
operator|.
name|getAcidOperation
argument_list|()
operator|==
name|AcidUtils
operator|.
name|Operation
operator|.
name|DELETE
return|;
block|}
specifier|private
name|void
name|analyzeUpdate
parameter_list|(
name|ASTNode
name|tree
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ctx
operator|.
name|setAcidOperation
argument_list|(
name|AcidUtils
operator|.
name|Operation
operator|.
name|UPDATE
argument_list|)
expr_stmt|;
name|reparseAndSuperAnalyze
argument_list|(
name|tree
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|analyzeDelete
parameter_list|(
name|ASTNode
name|tree
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ctx
operator|.
name|setAcidOperation
argument_list|(
name|AcidUtils
operator|.
name|Operation
operator|.
name|DELETE
argument_list|)
expr_stmt|;
name|reparseAndSuperAnalyze
argument_list|(
name|tree
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|reparseAndSuperAnalyze
parameter_list|(
name|ASTNode
name|tree
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|?
extends|extends
name|Node
argument_list|>
name|children
init|=
name|tree
operator|.
name|getChildren
argument_list|()
decl_stmt|;
comment|// The first child should be the table we are deleting from
name|ASTNode
name|tabName
init|=
operator|(
name|ASTNode
operator|)
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
assert|assert
name|tabName
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_TABNAME
operator|:
literal|"Expected tablename as first child of "
operator|+
name|operation
argument_list|()
operator|+
literal|" but found "
operator|+
name|tabName
operator|.
name|getName
argument_list|()
assert|;
name|String
index|[]
name|tableName
init|=
name|getQualifiedTableName
argument_list|(
name|tabName
argument_list|)
decl_stmt|;
comment|// Rewrite the delete or update into an insert.  Crazy, but it works as deletes and update
comment|// actually are inserts into the delta file in Hive.  A delete
comment|// DELETE FROM _tablename_ [WHERE ...]
comment|// will be rewritten as
comment|// INSERT INTO TABLE _tablename_ [PARTITION (_partcols_)] SELECT ROW__ID[,
comment|// _partcols_] from _tablename_ SORT BY ROW__ID
comment|// An update
comment|// UPDATE _tablename_ SET x = _expr_ [WHERE...]
comment|// will be rewritten as
comment|// INSERT INTO TABLE _tablename_ [PARTITION (_partcols_)] SELECT _all_,
comment|// _partcols_from _tablename_ SORT BY ROW__ID
comment|// where _all_ is all the non-partition columns.  The expressions from the set clause will be
comment|// re-attached later.
comment|// The where clause will also be re-attached later.
comment|// The sort by clause is put in there so that records come out in the right order to enable
comment|// merge on read.
name|StringBuilder
name|rewrittenQueryStr
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
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
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
init|=
name|mTable
operator|.
name|getPartCols
argument_list|()
decl_stmt|;
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
literal|"insert into table "
argument_list|)
expr_stmt|;
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
name|getDotName
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
comment|// If the table is partitioned we have to put the partition() clause in
if|if
condition|(
name|partCols
operator|!=
literal|null
operator|&&
name|partCols
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
literal|" partition ("
argument_list|)
expr_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
for|for
control|(
name|FieldSchema
name|fschema
range|:
name|partCols
control|)
block|{
if|if
condition|(
name|first
condition|)
name|first
operator|=
literal|false
expr_stmt|;
else|else
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
name|fschema
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
literal|" select ROW__ID"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|ASTNode
argument_list|>
name|setColExprs
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|ASTNode
argument_list|>
name|setCols
init|=
literal|null
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|setRCols
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|updating
argument_list|()
condition|)
block|{
comment|// An update needs to select all of the columns, as we rewrite the entire row.  Also,
comment|// we need to figure out which columns we are going to replace.  We won't write the set
comment|// expressions in the rewritten query.  We'll patch that up later.
comment|// The set list from update should be the second child (index 1)
assert|assert
name|children
operator|.
name|size
argument_list|()
operator|>=
literal|2
operator|:
literal|"Expected update token to have at least two children"
assert|;
name|ASTNode
name|setClause
init|=
operator|(
name|ASTNode
operator|)
name|children
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
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
name|setCols
operator|=
operator|new
name|HashMap
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
expr_stmt|;
name|setColExprs
operator|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|ASTNode
argument_list|>
argument_list|(
name|assignments
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
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
name|String
name|columnName
init|=
name|colName
operator|.
name|getText
argument_list|()
decl_stmt|;
comment|// Make sure this isn't one of the partitioning columns, that's not supported.
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
block|}
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
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|nonPartCols
init|=
name|mTable
operator|.
name|getCols
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nonPartCols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
name|String
name|name
init|=
name|nonPartCols
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getName
argument_list|()
decl_stmt|;
name|ASTNode
name|setCol
init|=
name|setCols
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|setCol
operator|!=
literal|null
condition|)
block|{
comment|// This is one of the columns we're setting, record it's position so we can come back
comment|// later and patch it up.
comment|// Add one to the index because the select has the ROW__ID as the first column.
name|setColExprs
operator|.
name|put
argument_list|(
name|i
operator|+
literal|1
argument_list|,
name|setCol
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
name|fschema
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
literal|" from "
argument_list|)
expr_stmt|;
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
name|getDotName
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|ASTNode
name|where
init|=
literal|null
decl_stmt|;
name|int
name|whereIndex
init|=
name|deleting
argument_list|()
condition|?
literal|1
else|:
literal|2
decl_stmt|;
if|if
condition|(
name|children
operator|.
name|size
argument_list|()
operator|>
name|whereIndex
condition|)
block|{
name|where
operator|=
operator|(
name|ASTNode
operator|)
name|children
operator|.
name|get
argument_list|(
name|whereIndex
argument_list|)
expr_stmt|;
assert|assert
name|where
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_WHERE
operator|:
literal|"Expected where clause, but found "
operator|+
name|where
operator|.
name|getName
argument_list|()
assert|;
block|}
comment|// Add a sort by clause so that the row ids come out in the correct order
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
literal|" sort by ROW__ID desc "
argument_list|)
expr_stmt|;
comment|// Parse the rewritten query string
name|Context
name|rewrittenCtx
decl_stmt|;
try|try
block|{
comment|// Set dynamic partitioning to nonstrict so that queries do not need any partition
comment|// references.
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
name|rewrittenCtx
operator|=
operator|new
name|Context
argument_list|(
name|conf
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
name|setCmd
argument_list|(
name|rewrittenQueryStr
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|rewrittenCtx
operator|.
name|setAcidOperation
argument_list|(
name|ctx
operator|.
name|getAcidOperation
argument_list|()
argument_list|)
expr_stmt|;
name|ParseDriver
name|pd
init|=
operator|new
name|ParseDriver
argument_list|()
decl_stmt|;
name|ASTNode
name|rewrittenTree
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Going to reparse "
operator|+
name|operation
argument_list|()
operator|+
literal|" as<"
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
name|pd
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
name|rewrittenTree
operator|=
name|ParseUtils
operator|.
name|findRootNonNullToken
argument_list|(
name|rewrittenTree
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
name|ASTNode
name|rewrittenInsert
init|=
operator|(
name|ASTNode
operator|)
name|rewrittenTree
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
assert|assert
name|rewrittenInsert
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_INSERT
operator|:
literal|"Expected TOK_INSERT as second child of TOK_QUERY but found "
operator|+
name|rewrittenInsert
operator|.
name|getName
argument_list|()
assert|;
if|if
condition|(
name|where
operator|!=
literal|null
condition|)
block|{
comment|// The structure of the AST for the rewritten insert statement is:
comment|// TOK_QUERY -> TOK_FROM
comment|//          \-> TOK_INSERT -> TOK_INSERT_INTO
comment|//                        \-> TOK_SELECT
comment|//                        \-> TOK_SORTBY
comment|// The following adds the TOK_WHERE and its subtree from the original query as a child of
comment|// TOK_INSERT, which is where it would have landed if it had been there originally in the
comment|// string.  We do it this way because it's easy then turning the original AST back into a
comment|// string and reparsing it.  We have to move the SORT_BY over one,
comment|// so grab it and then push it to the second slot, and put the where in the first slot
name|ASTNode
name|sortBy
init|=
operator|(
name|ASTNode
operator|)
name|rewrittenInsert
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
decl_stmt|;
assert|assert
name|sortBy
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_SORTBY
operator|:
literal|"Expected TOK_SORTBY to be first child of TOK_SELECT, but found "
operator|+
name|sortBy
operator|.
name|getName
argument_list|()
assert|;
name|rewrittenInsert
operator|.
name|addChild
argument_list|(
name|sortBy
argument_list|)
expr_stmt|;
name|rewrittenInsert
operator|.
name|setChild
argument_list|(
literal|2
argument_list|,
name|where
argument_list|)
expr_stmt|;
block|}
comment|// Patch up the projection list for updates, putting back the original set expressions.
if|if
condition|(
name|updating
argument_list|()
operator|&&
name|setColExprs
operator|!=
literal|null
condition|)
block|{
comment|// Walk through the projection list and replace the column names with the
comment|// expressions from the original update.  Under the TOK_SELECT (see above) the structure
comment|// looks like:
comment|// TOK_SELECT -> TOK_SELEXPR -> expr
comment|//           \-> TOK_SELEXPR -> expr ...
name|ASTNode
name|rewrittenSelect
init|=
operator|(
name|ASTNode
operator|)
name|rewrittenInsert
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
assert|assert
name|rewrittenSelect
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_SELECT
operator|:
literal|"Expected TOK_SELECT as second child of TOK_INSERT but found "
operator|+
name|rewrittenSelect
operator|.
name|getName
argument_list|()
assert|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Integer
argument_list|,
name|ASTNode
argument_list|>
name|entry
range|:
name|setColExprs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ASTNode
name|selExpr
init|=
operator|(
name|ASTNode
operator|)
name|rewrittenSelect
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
assert|assert
name|selExpr
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_SELEXPR
operator|:
literal|"Expected child of TOK_SELECT to be TOK_SELEXPR but was "
operator|+
name|selExpr
operator|.
name|getName
argument_list|()
assert|;
comment|// Now, change it's child
name|selExpr
operator|.
name|setChild
argument_list|(
literal|0
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
try|try
block|{
name|useSuper
operator|=
literal|true
expr_stmt|;
name|super
operator|.
name|analyze
argument_list|(
name|rewrittenTree
argument_list|,
name|rewrittenCtx
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|useSuper
operator|=
literal|false
expr_stmt|;
block|}
comment|// Walk through all our inputs and set them to note that this read is part of an update or a
comment|// delete.
for|for
control|(
name|ReadEntity
name|input
range|:
name|inputs
control|)
block|{
name|input
operator|.
name|setUpdateOrDelete
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|inputIsPartitioned
argument_list|(
name|inputs
argument_list|)
condition|)
block|{
comment|// In order to avoid locking the entire write table we need to replace the single WriteEntity
comment|// with a WriteEntity for each partition
name|outputs
operator|.
name|clear
argument_list|()
expr_stmt|;
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
name|input
operator|.
name|getTyp
argument_list|()
operator|==
name|Entity
operator|.
name|Type
operator|.
name|PARTITION
condition|)
block|{
name|WriteEntity
operator|.
name|WriteType
name|writeType
init|=
name|deleting
argument_list|()
condition|?
name|WriteEntity
operator|.
name|WriteType
operator|.
name|DELETE
else|:
name|WriteEntity
operator|.
name|WriteType
operator|.
name|UPDATE
decl_stmt|;
name|outputs
operator|.
name|add
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|input
operator|.
name|getPartition
argument_list|()
argument_list|,
name|writeType
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// We still need to patch up the WriteEntities as they will have an insert type.  Change
comment|// them to the appropriate type for our operation.
for|for
control|(
name|WriteEntity
name|output
range|:
name|outputs
control|)
block|{
name|output
operator|.
name|setWriteType
argument_list|(
name|deleting
argument_list|()
condition|?
name|WriteEntity
operator|.
name|WriteType
operator|.
name|DELETE
else|:
name|WriteEntity
operator|.
name|WriteType
operator|.
name|UPDATE
argument_list|)
expr_stmt|;
block|}
block|}
comment|// For updates, we need to set the column access info so that it contains information on
comment|// the columns we are updating.
if|if
condition|(
name|updating
argument_list|()
condition|)
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
comment|// Add the setRCols to the input list
for|for
control|(
name|String
name|colName
range|:
name|setRCols
control|)
block|{
if|if
condition|(
name|columnAccessInfo
operator|!=
literal|null
condition|)
block|{
comment|//assuming this means we are not doing Auth
name|columnAccessInfo
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
block|}
block|}
comment|// We need to weed ROW__ID out of the input column info, as it doesn't make any sense to
comment|// require the user to have authorization on that column.
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
specifier|private
name|String
name|operation
parameter_list|()
block|{
if|if
condition|(
name|updating
argument_list|()
condition|)
return|return
literal|"update"
return|;
elseif|else
if|if
condition|(
name|deleting
argument_list|()
condition|)
return|return
literal|"delete"
return|;
else|else
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"UpdateDeleteSemanticAnalyzer neither updating nor "
operator|+
literal|"deleting, operation not known."
argument_list|)
throw|;
block|}
specifier|private
name|boolean
name|inputIsPartitioned
parameter_list|(
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|)
block|{
comment|// We cannot simply look at the first entry, as in the case where the input is partitioned
comment|// there will be a table entry as well.  So look for at least one partition entry.
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
name|getTyp
argument_list|()
operator|==
name|Entity
operator|.
name|Type
operator|.
name|PARTITION
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
comment|// This method find any columns on the right side of a set statement (thus rcols) and puts them
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
name|colName
operator|.
name|getText
argument_list|()
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
block|}
end_class

end_unit

