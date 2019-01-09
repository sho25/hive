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
name|ArrayList
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
name|UUID
import|;
end_import

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
name|fs
operator|.
name|Path
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
name|Warehouse
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|hive_metastoreConstants
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
name|DriverContext
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
name|exec
operator|.
name|DDLTask
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
name|StatsTask
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
name|plan
operator|.
name|AlterTableDesc
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
name|CreateTableLikeDesc
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
name|plan
operator|.
name|DropTableDesc
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
name|ExportWork
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
comment|/**  * A subclass of the {@link org.apache.hadoop.hive.ql.parse.SemanticAnalyzer} that just handles  * acid export statements. It works by rewriting the acid export into insert statements into a temporary table,  * and then export it from there.  */
end_comment

begin_class
specifier|public
class|class
name|AcidExportSemanticAnalyzer
extends|extends
name|RewriteSemanticAnalyzer
block|{
name|AcidExportSemanticAnalyzer
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
specifier|protected
name|void
name|analyze
parameter_list|(
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
operator|.
name|getType
argument_list|()
operator|!=
name|HiveParser
operator|.
name|TOK_EXPORT
condition|)
block|{
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
literal|"AcidExportSemanticAnalyzer"
argument_list|)
throw|;
block|}
name|analyzeAcidExport
argument_list|(
name|tree
argument_list|)
expr_stmt|;
block|}
comment|/**    * Exporting an Acid table is more complicated than a flat table.  It may contains delete events,    * which can only be interpreted properly withing the context of the table/metastore where they    * were generated.  It may also contain insert events that belong to transactions that aborted    * where the same constraints apply.    * In order to make the export artifact free of these constraints, the export does a    * insert into tmpTable select * from<export table> to filter/apply the events in current    * context and then export the tmpTable.  This export artifact can now be imported into any    * table on any cluster (subject to schema checks etc).    * See {@link #analyzeAcidExport(ASTNode)}    * @param tree Export statement    * @return true if exporting an Acid table.    */
specifier|public
specifier|static
name|boolean
name|isAcidExport
parameter_list|(
name|ASTNode
name|tree
parameter_list|)
throws|throws
name|SemanticException
block|{
assert|assert
name|tree
operator|!=
literal|null
operator|&&
name|tree
operator|.
name|getToken
argument_list|()
operator|!=
literal|null
operator|&&
name|tree
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_EXPORT
assert|;
name|Tree
name|tokTab
init|=
name|tree
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
assert|assert
name|tokTab
operator|!=
literal|null
operator|&&
name|tokTab
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_TAB
assert|;
name|Table
name|tableHandle
init|=
literal|null
decl_stmt|;
try|try
block|{
name|tableHandle
operator|=
name|getTable
argument_list|(
operator|(
name|ASTNode
operator|)
name|tokTab
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
argument_list|,
name|Hive
operator|.
name|get
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
comment|//tableHandle can be null if table doesn't exist
return|return
name|tableHandle
operator|!=
literal|null
operator|&&
name|AcidUtils
operator|.
name|isFullAcidTable
argument_list|(
name|tableHandle
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|getTmptTableNameForExport
parameter_list|(
name|Table
name|exportTable
parameter_list|)
block|{
name|String
name|tmpTableDb
init|=
name|exportTable
operator|.
name|getDbName
argument_list|()
decl_stmt|;
name|String
name|tmpTableName
init|=
name|exportTable
operator|.
name|getTableName
argument_list|()
operator|+
literal|"_"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|replace
argument_list|(
literal|'-'
argument_list|,
literal|'_'
argument_list|)
decl_stmt|;
return|return
name|Warehouse
operator|.
name|getQualifiedName
argument_list|(
name|tmpTableDb
argument_list|,
name|tmpTableName
argument_list|)
return|;
block|}
comment|/**    * See {@link #isAcidExport(ASTNode)}    * 1. create the temp table T    * 2. compile 'insert into T select * from acidTable'    * 3. compile 'export acidTable'  (acidTable will be replaced with T during execution)    * 4. create task to drop T    *    * Using a true temp (session level) table means it should not affect replication and the table    * is not visible outside the Session that created for security    */
specifier|private
name|void
name|analyzeAcidExport
parameter_list|(
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
assert|assert
name|ast
operator|!=
literal|null
operator|&&
name|ast
operator|.
name|getToken
argument_list|()
operator|!=
literal|null
operator|&&
name|ast
operator|.
name|getToken
argument_list|()
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_EXPORT
assert|;
name|ASTNode
name|tableTree
init|=
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
assert|assert
name|tableTree
operator|!=
literal|null
operator|&&
name|tableTree
operator|.
name|getType
argument_list|()
operator|==
name|HiveParser
operator|.
name|TOK_TAB
assert|;
name|ASTNode
name|tokRefOrNameExportTable
init|=
operator|(
name|ASTNode
operator|)
name|tableTree
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Table
name|exportTable
init|=
name|getTargetTable
argument_list|(
name|tokRefOrNameExportTable
argument_list|)
decl_stmt|;
assert|assert
name|AcidUtils
operator|.
name|isFullAcidTable
argument_list|(
name|exportTable
argument_list|)
assert|;
comment|//need to create the table "manually" rather than creating a task since it has to exist to
comment|// compile the insert into T...
name|String
name|newTableName
init|=
name|getTmptTableNameForExport
argument_list|(
name|exportTable
argument_list|)
decl_stmt|;
comment|//this is db.table
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tblProps
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|tblProps
operator|.
name|put
argument_list|(
name|hive_metastoreConstants
operator|.
name|TABLE_IS_TRANSACTIONAL
argument_list|,
name|Boolean
operator|.
name|FALSE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|location
decl_stmt|;
comment|// for temporary tables we set the location to something in the session's scratch dir
comment|// it has the same life cycle as the tmp table
try|try
block|{
comment|// Generate a unique ID for temp table path.
comment|// This path will be fixed for the life of the temp table.
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|SessionState
operator|.
name|getTempTableSpace
argument_list|(
name|conf
argument_list|)
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|path
operator|=
name|Warehouse
operator|.
name|getDnsPath
argument_list|(
name|path
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|location
operator|=
name|path
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|err
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Error while generating temp table path:"
argument_list|,
name|err
argument_list|)
throw|;
block|}
name|CreateTableLikeDesc
name|ctlt
init|=
operator|new
name|CreateTableLikeDesc
argument_list|(
name|newTableName
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|location
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|tblProps
argument_list|,
literal|true
argument_list|,
comment|//important so we get an exception on name collision
name|Warehouse
operator|.
name|getQualifiedName
argument_list|(
name|exportTable
operator|.
name|getTTable
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Table
name|newTable
decl_stmt|;
try|try
block|{
name|ReadEntity
name|dbForTmpTable
init|=
operator|new
name|ReadEntity
argument_list|(
name|db
operator|.
name|getDatabase
argument_list|(
name|exportTable
operator|.
name|getDbName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|inputs
operator|.
name|add
argument_list|(
name|dbForTmpTable
argument_list|)
expr_stmt|;
comment|//so the plan knows we are 'reading' this db - locks, security...
name|DDLTask
name|createTableTask
init|=
operator|(
name|DDLTask
operator|)
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DDLWork
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
name|ctlt
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|createTableTask
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|//above get() doesn't set it
name|createTableTask
operator|.
name|execute
argument_list|(
operator|new
name|DriverContext
argument_list|(
operator|new
name|Context
argument_list|(
name|conf
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|newTable
operator|=
name|db
operator|.
name|getTable
argument_list|(
name|newTableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|HiveException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
comment|//now generate insert statement
comment|//insert into newTableName select * from ts<where partition spec>
name|StringBuilder
name|rewrittenQueryStr
init|=
name|generateExportQuery
argument_list|(
name|newTable
operator|.
name|getPartCols
argument_list|()
argument_list|,
name|tokRefOrNameExportTable
argument_list|,
name|tableTree
argument_list|,
name|newTableName
argument_list|)
decl_stmt|;
name|ReparseResult
name|rr
init|=
name|parseRewrittenQuery
argument_list|(
name|rewrittenQueryStr
argument_list|,
name|ctx
operator|.
name|getCmd
argument_list|()
argument_list|)
decl_stmt|;
name|Context
name|rewrittenCtx
init|=
name|rr
operator|.
name|rewrittenCtx
decl_stmt|;
name|rewrittenCtx
operator|.
name|setIsUpdateDeleteMerge
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|//it's set in parseRewrittenQuery()
name|ASTNode
name|rewrittenTree
init|=
name|rr
operator|.
name|rewrittenTree
decl_stmt|;
try|try
block|{
name|useSuper
operator|=
literal|true
expr_stmt|;
comment|//newTable has to exist at this point to compile
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
comment|//now we have the rootTasks set up for Insert ... Select
name|removeStatsTasks
argument_list|(
name|rootTasks
argument_list|)
expr_stmt|;
comment|//now make an ExportTask from temp table
comment|/*analyzeExport() creates TableSpec which in turn tries to build      "public List<Partition> partitions" by looking in the metastore to find Partitions matching      the partition spec in the Export command.  These of course don't exist yet since we've not      ran the insert stmt yet!!!!!!!       */
name|Task
argument_list|<
name|ExportWork
argument_list|>
name|exportTask
init|=
name|ExportSemanticAnalyzer
operator|.
name|analyzeExport
argument_list|(
name|ast
argument_list|,
name|newTableName
argument_list|,
name|db
argument_list|,
name|conf
argument_list|,
name|inputs
argument_list|,
name|outputs
argument_list|)
decl_stmt|;
comment|// Add an alter table task to set transactional props
comment|// do it after populating temp table so that it's written as non-transactional table but
comment|// update props before export so that export archive metadata has these props.  This way when
comment|// IMPORT is done for this archive and target table doesn't exist, it will be created as Acid.
name|AlterTableDesc
name|alterTblDesc
init|=
operator|new
name|AlterTableDesc
argument_list|(
name|AlterTableDesc
operator|.
name|AlterTableTypes
operator|.
name|ADDPROPS
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapProps
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|mapProps
operator|.
name|put
argument_list|(
name|hive_metastoreConstants
operator|.
name|TABLE_IS_TRANSACTIONAL
argument_list|,
name|Boolean
operator|.
name|TRUE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|alterTblDesc
operator|.
name|setProps
argument_list|(
name|mapProps
argument_list|)
expr_stmt|;
name|alterTblDesc
operator|.
name|setOldName
argument_list|(
name|newTableName
argument_list|)
expr_stmt|;
name|addExportTask
argument_list|(
name|rootTasks
argument_list|,
name|exportTask
argument_list|,
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
name|alterTblDesc
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now make a task to drop temp table
comment|// {@link DDLSemanticAnalyzer#analyzeDropTable(ASTNode ast, TableType expectedType)
name|ReplicationSpec
name|replicationSpec
init|=
operator|new
name|ReplicationSpec
argument_list|()
decl_stmt|;
name|DropTableDesc
name|dropTblDesc
init|=
operator|new
name|DropTableDesc
argument_list|(
name|newTableName
argument_list|,
name|TableType
operator|.
name|MANAGED_TABLE
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
name|replicationSpec
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|DDLWork
argument_list|>
name|dropTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DDLWork
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
name|dropTblDesc
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|exportTask
operator|.
name|addDependentTask
argument_list|(
name|dropTask
argument_list|)
expr_stmt|;
name|markReadEntityForUpdate
argument_list|()
expr_stmt|;
if|if
condition|(
name|ctx
operator|.
name|isExplainPlan
argument_list|()
condition|)
block|{
try|try
block|{
comment|//so that "explain" doesn't "leak" tmp tables
comment|// TODO: catalog
name|db
operator|.
name|dropTable
argument_list|(
name|newTable
operator|.
name|getDbName
argument_list|()
argument_list|,
name|newTable
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to drop "
operator|+
name|newTableName
operator|+
literal|" due to: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Generate    * insert into newTableName select * from ts<where partition spec>    * for EXPORT command.    */
specifier|private
name|StringBuilder
name|generateExportQuery
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
parameter_list|,
name|ASTNode
name|tokRefOrNameExportTable
parameter_list|,
name|ASTNode
name|tableTree
parameter_list|,
name|String
name|newTableName
parameter_list|)
throws|throws
name|SemanticException
block|{
name|StringBuilder
name|rewrittenQueryStr
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"insert into "
argument_list|)
operator|.
name|append
argument_list|(
name|newTableName
argument_list|)
decl_stmt|;
name|addPartitionColsToInsert
argument_list|(
name|partCols
argument_list|,
name|rewrittenQueryStr
argument_list|)
expr_stmt|;
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
literal|" select * from "
argument_list|)
operator|.
name|append
argument_list|(
name|getFullTableNameForSQL
argument_list|(
name|tokRefOrNameExportTable
argument_list|)
argument_list|)
expr_stmt|;
comment|//builds partition spec so we can build suitable WHERE clause
name|TableSpec
name|exportTableSpec
init|=
operator|new
name|TableSpec
argument_list|(
name|db
argument_list|,
name|conf
argument_list|,
name|tableTree
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|exportTableSpec
operator|.
name|getPartSpec
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|StringBuilder
name|whereClause
init|=
literal|null
decl_stmt|;
name|int
name|partColsIdx
init|=
operator|-
literal|1
decl_stmt|;
comment|//keep track of corresponding col in partCols
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ent
range|:
name|exportTableSpec
operator|.
name|getPartSpec
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|partColsIdx
operator|++
expr_stmt|;
if|if
condition|(
name|ent
operator|.
name|getValue
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
comment|//partial spec
block|}
if|if
condition|(
name|whereClause
operator|==
literal|null
condition|)
block|{
name|whereClause
operator|=
operator|new
name|StringBuilder
argument_list|(
literal|" WHERE "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|whereClause
operator|.
name|length
argument_list|()
operator|>
literal|" WHERE "
operator|.
name|length
argument_list|()
condition|)
block|{
name|whereClause
operator|.
name|append
argument_list|(
literal|" AND "
argument_list|)
expr_stmt|;
block|}
name|whereClause
operator|.
name|append
argument_list|(
name|HiveUtils
operator|.
name|unparseIdentifier
argument_list|(
name|ent
operator|.
name|getKey
argument_list|()
argument_list|,
name|conf
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|" = "
argument_list|)
operator|.
name|append
argument_list|(
name|genPartValueString
argument_list|(
name|partCols
operator|.
name|get
argument_list|(
name|partColsIdx
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|ent
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|whereClause
operator|!=
literal|null
condition|)
block|{
name|rewrittenQueryStr
operator|.
name|append
argument_list|(
name|whereClause
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|rewrittenQueryStr
return|;
block|}
comment|/**    * Makes the exportTask run after all other tasks of the "insert into T ..." are done.    */
specifier|private
name|void
name|addExportTask
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|rootTasks
parameter_list|,
name|Task
argument_list|<
name|ExportWork
argument_list|>
name|exportTask
parameter_list|,
name|Task
argument_list|<
name|DDLWork
argument_list|>
name|alterTable
parameter_list|)
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|t
range|:
name|rootTasks
control|)
block|{
if|if
condition|(
name|t
operator|.
name|getNumChild
argument_list|()
operator|<=
literal|0
condition|)
block|{
comment|//todo: ConditionalTask#addDependentTask(Task) doesn't do the right thing: HIVE-18978
name|t
operator|.
name|addDependentTask
argument_list|(
name|alterTable
argument_list|)
expr_stmt|;
comment|//this is a leaf so add exportTask to follow it
name|alterTable
operator|.
name|addDependentTask
argument_list|(
name|exportTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|addExportTask
argument_list|(
name|t
operator|.
name|getDependentTasks
argument_list|()
argument_list|,
name|exportTask
argument_list|,
name|alterTable
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|removeStatsTasks
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|rootTasks
parameter_list|)
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|statsTasks
init|=
name|findStatsTasks
argument_list|(
name|rootTasks
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|statsTasks
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|Task
argument_list|<
name|?
argument_list|>
name|statsTask
range|:
name|statsTasks
control|)
block|{
if|if
condition|(
name|statsTask
operator|.
name|getParentTasks
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
comment|//should never happen
block|}
for|for
control|(
name|Task
argument_list|<
name|?
argument_list|>
name|t
range|:
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|statsTask
operator|.
name|getParentTasks
argument_list|()
argument_list|)
control|)
block|{
name|t
operator|.
name|removeDependentTask
argument_list|(
name|statsTask
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|findStatsTasks
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|rootTasks
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|statsTasks
parameter_list|)
block|{
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|t
range|:
name|rootTasks
control|)
block|{
if|if
condition|(
name|t
operator|instanceof
name|StatsTask
condition|)
block|{
if|if
condition|(
name|statsTasks
operator|==
literal|null
condition|)
block|{
name|statsTasks
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|statsTasks
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|t
operator|.
name|getDependentTasks
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|statsTasks
operator|=
name|findStatsTasks
argument_list|(
name|t
operator|.
name|getDependentTasks
argument_list|()
argument_list|,
name|statsTasks
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|statsTasks
return|;
block|}
block|}
end_class

end_unit

