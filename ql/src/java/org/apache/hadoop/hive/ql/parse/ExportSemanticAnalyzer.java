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
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
name|exec
operator|.
name|Utilities
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
name|parse
operator|.
name|repl
operator|.
name|dump
operator|.
name|TableExport
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
name|plan
operator|.
name|ExportWork
operator|.
name|MmContext
import|;
end_import

begin_comment
comment|/**  * ExportSemanticAnalyzer.  *  */
end_comment

begin_class
specifier|public
class|class
name|ExportSemanticAnalyzer
extends|extends
name|BaseSemanticAnalyzer
block|{
specifier|private
name|boolean
name|isMmExport
init|=
literal|false
decl_stmt|;
name|ExportSemanticAnalyzer
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
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Task
argument_list|<
name|ExportWork
argument_list|>
name|task
init|=
name|analyzeExport
argument_list|(
name|ast
argument_list|,
literal|null
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
name|isMmExport
operator|=
name|task
operator|.
name|getWork
argument_list|()
operator|.
name|getMmContext
argument_list|()
operator|!=
literal|null
expr_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param acidTableName - table name in db.table format; not NULL if exporting Acid table    */
specifier|static
name|Task
argument_list|<
name|ExportWork
argument_list|>
name|analyzeExport
parameter_list|(
name|ASTNode
name|ast
parameter_list|,
annotation|@
name|Nullable
name|String
name|acidTableName
parameter_list|,
name|Hive
name|db
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Tree
name|tableTree
init|=
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Tree
name|toTree
init|=
name|ast
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ReplicationSpec
name|replicationSpec
decl_stmt|;
if|if
condition|(
name|ast
operator|.
name|getChildCount
argument_list|()
operator|>
literal|2
condition|)
block|{
comment|// Replication case: export table<tbl> to<location> for replication
name|replicationSpec
operator|=
operator|new
name|ReplicationSpec
argument_list|(
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Export case
name|replicationSpec
operator|=
operator|new
name|ReplicationSpec
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|replicationSpec
operator|.
name|getCurrentReplicationState
argument_list|()
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|long
name|currentEventId
init|=
name|db
operator|.
name|getMSC
argument_list|()
operator|.
name|getCurrentNotificationEventId
argument_list|()
operator|.
name|getEventId
argument_list|()
decl_stmt|;
name|replicationSpec
operator|.
name|setCurrentReplicationState
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|currentEventId
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Error when getting current notification event ID"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|// initialize source table/partition
name|TableSpec
name|ts
decl_stmt|;
try|try
block|{
name|ts
operator|=
operator|new
name|TableSpec
argument_list|(
name|db
argument_list|,
name|conf
argument_list|,
operator|(
name|ASTNode
operator|)
name|tableTree
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SemanticException
name|sme
parameter_list|)
block|{
if|if
condition|(
operator|!
name|replicationSpec
operator|.
name|isInReplicationScope
argument_list|()
condition|)
throw|throw
name|sme
throw|;
if|if
condition|(
operator|(
name|sme
operator|.
name|getCause
argument_list|()
operator|instanceof
name|InvalidTableException
operator|)
operator|||
operator|(
name|sme
operator|instanceof
name|Table
operator|.
name|ValidationFailureSemanticException
operator|)
condition|)
block|{
comment|// If we're in replication scope, it's possible that we're running the export long after
comment|// the table was dropped, so the table not existing currently or being a different kind of
comment|// table is not an error - it simply means we should no-op, and let a future export
comment|// capture the appropriate state
name|ts
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|sme
throw|;
block|}
block|}
comment|// initialize export path
name|String
name|tmpPath
init|=
name|stripQuotes
argument_list|(
name|toTree
operator|.
name|getText
argument_list|()
argument_list|)
decl_stmt|;
comment|// All parsing is done, we're now good to start the export process
name|TableExport
operator|.
name|Paths
name|exportPaths
init|=
operator|new
name|TableExport
operator|.
name|Paths
argument_list|(
name|ASTErrorUtils
operator|.
name|getMsg
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_PATH
operator|.
name|getMsg
argument_list|()
argument_list|,
name|ast
argument_list|)
argument_list|,
name|tmpPath
argument_list|,
name|conf
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// Note: this tableExport is actually never used other than for auth, and another one is
comment|//       created when the task is executed. So, we don't care about the correct MM state here.
name|TableExport
operator|.
name|AuthEntities
name|authEntities
init|=
operator|new
name|TableExport
argument_list|(
name|exportPaths
argument_list|,
name|ts
argument_list|,
name|replicationSpec
argument_list|,
name|db
argument_list|,
literal|null
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|)
operator|.
name|getAuthEntities
argument_list|()
decl_stmt|;
name|inputs
operator|.
name|addAll
argument_list|(
name|authEntities
operator|.
name|inputs
argument_list|)
expr_stmt|;
name|outputs
operator|.
name|addAll
argument_list|(
name|authEntities
operator|.
name|outputs
argument_list|)
expr_stmt|;
name|String
name|exportRootDirName
init|=
name|tmpPath
decl_stmt|;
name|MmContext
name|mmCtx
init|=
name|MmContext
operator|.
name|createIfNeeded
argument_list|(
name|ts
operator|==
literal|null
condition|?
literal|null
else|:
name|ts
operator|.
name|tableHandle
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|FILE_OP_LOGGER
operator|.
name|debug
argument_list|(
literal|"Exporting table {}: MM context {}"
argument_list|,
name|ts
operator|==
literal|null
condition|?
literal|null
else|:
name|ts
operator|.
name|getTableName
argument_list|()
argument_list|,
name|mmCtx
argument_list|)
expr_stmt|;
comment|// Configure export work
name|ExportWork
name|exportWork
init|=
operator|new
name|ExportWork
argument_list|(
name|exportRootDirName
argument_list|,
name|ts
argument_list|,
name|replicationSpec
argument_list|,
name|ASTErrorUtils
operator|.
name|getMsg
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_PATH
operator|.
name|getMsg
argument_list|()
argument_list|,
name|ast
argument_list|)
argument_list|,
name|acidTableName
argument_list|,
name|mmCtx
argument_list|)
decl_stmt|;
comment|// Create an export task and add it as a root task
return|return
name|TaskFactory
operator|.
name|get
argument_list|(
name|exportWork
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasTransactionalInQuery
parameter_list|()
block|{
return|return
name|isMmExport
return|;
comment|// Full ACID export goes thru UpdateDelete analyzer.
block|}
block|}
end_class

end_unit

