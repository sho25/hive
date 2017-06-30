begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*   Licensed to the Apache Software Foundation (ASF) under one   or more contributor license agreements.  See the NOTICE file   distributed with this work for additional information   regarding copyright ownership.  The ASF licenses this file   to you under the Apache License, Version 2.0 (the   "License"); you may not use this file except in compliance   with the License.  You may obtain a copy of the License at        http://www.apache.org/licenses/LICENSE-2.0    Unless required by applicable law or agreed to in writing, software   distributed under the License is distributed on an "AS IS" BASIS,   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   See the License for the specific language governing permissions and   limitations under the License.  */
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
name|replicationSpec
operator|=
operator|new
name|ReplicationSpec
argument_list|()
expr_stmt|;
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
operator|(
name|replicationSpec
operator|.
name|isInReplicationScope
argument_list|()
operator|)
operator|&&
operator|(
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
comment|// All parsing is done, we're now good to start the export process.
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
name|ast
argument_list|,
name|tmpPath
argument_list|,
name|conf
argument_list|)
decl_stmt|;
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
name|conf
argument_list|,
name|LOG
argument_list|)
operator|.
name|run
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
block|}
block|}
end_class

end_unit

