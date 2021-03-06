begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|optimizer
operator|.
name|calcite
operator|.
name|rules
operator|.
name|views
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|RelNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|RelVisitor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|core
operator|.
name|Aggregate
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|core
operator|.
name|Filter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|core
operator|.
name|Join
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|core
operator|.
name|Project
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|core
operator|.
name|TableScan
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|core
operator|.
name|Union
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|util
operator|.
name|ControlFlowException
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
name|optimizer
operator|.
name|calcite
operator|.
name|RelOptHiveTable
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
comment|/**  * This class is a helper to check whether a materialized view rebuild  * can be transformed from INSERT OVERWRITE to INSERT INTO.  *  * We are verifying that:  *   1) the rewriting is rooted by legal operators (Filter and Project)  *   before reaching a Union operator,  *   2) the left branch uses the MV that we are trying to rebuild and  *   legal operators (Filter and Project), and  *   3) the right branch only uses legal operators (i.e., Filter, Project,  *   Join, and TableScan)  */
end_comment

begin_class
specifier|public
class|class
name|MaterializedViewRewritingRelVisitor
extends|extends
name|RelVisitor
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
name|MaterializedViewRewritingRelVisitor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|containsAggregate
decl_stmt|;
specifier|private
name|boolean
name|rewritingAllowed
decl_stmt|;
specifier|public
name|MaterializedViewRewritingRelVisitor
parameter_list|()
block|{
name|this
operator|.
name|containsAggregate
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|rewritingAllowed
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|visit
parameter_list|(
name|RelNode
name|node
parameter_list|,
name|int
name|ordinal
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Aggregate
condition|)
block|{
name|this
operator|.
name|containsAggregate
operator|=
literal|true
expr_stmt|;
comment|// Aggregate mode - it should be followed by union
comment|// that we need to analyze
name|RelNode
name|input
init|=
name|node
operator|.
name|getInput
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|input
operator|instanceof
name|Union
condition|)
block|{
name|check
argument_list|(
operator|(
name|Union
operator|)
name|input
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|node
operator|instanceof
name|Union
condition|)
block|{
comment|// Non aggregate mode - analyze union operator
name|check
argument_list|(
operator|(
name|Union
operator|)
name|node
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|node
operator|instanceof
name|Project
condition|)
block|{
comment|// Project operator, we can continue
name|super
operator|.
name|visit
argument_list|(
name|node
argument_list|,
name|ordinal
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|ReturnedValue
argument_list|(
literal|false
argument_list|)
throw|;
block|}
specifier|private
name|void
name|check
parameter_list|(
name|Union
name|union
parameter_list|)
block|{
comment|// We found the Union
if|if
condition|(
name|union
operator|.
name|getInputs
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|2
condition|)
block|{
comment|// Bail out
throw|throw
operator|new
name|ReturnedValue
argument_list|(
literal|false
argument_list|)
throw|;
block|}
comment|// First branch should have the query (with write ID filter conditions)
operator|new
name|RelVisitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|visit
parameter_list|(
name|RelNode
name|node
parameter_list|,
name|int
name|ordinal
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|TableScan
operator|||
name|node
operator|instanceof
name|Filter
operator|||
name|node
operator|instanceof
name|Project
operator|||
name|node
operator|instanceof
name|Join
condition|)
block|{
comment|// We can continue
name|super
operator|.
name|visit
argument_list|(
name|node
argument_list|,
name|ordinal
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|node
operator|instanceof
name|Aggregate
operator|&&
name|containsAggregate
condition|)
block|{
comment|// We can continue
name|super
operator|.
name|visit
argument_list|(
name|node
argument_list|,
name|ordinal
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ReturnedValue
argument_list|(
literal|false
argument_list|)
throw|;
block|}
block|}
block|}
operator|.
name|go
argument_list|(
name|union
operator|.
name|getInput
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// Second branch should only have the MV
operator|new
name|RelVisitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|visit
parameter_list|(
name|RelNode
name|node
parameter_list|,
name|int
name|ordinal
parameter_list|,
name|RelNode
name|parent
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|TableScan
condition|)
block|{
comment|// We can continue
comment|// TODO: Need to check that this is the same MV that we are rebuilding
name|RelOptHiveTable
name|hiveTable
init|=
operator|(
name|RelOptHiveTable
operator|)
name|node
operator|.
name|getTable
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|hiveTable
operator|.
name|getHiveTableMD
argument_list|()
operator|.
name|isMaterializedView
argument_list|()
condition|)
block|{
comment|// If it is not a materialized view, we do not rewrite it
throw|throw
operator|new
name|ReturnedValue
argument_list|(
literal|false
argument_list|)
throw|;
block|}
if|if
condition|(
name|containsAggregate
operator|&&
operator|!
name|AcidUtils
operator|.
name|isFullAcidTable
argument_list|(
name|hiveTable
operator|.
name|getHiveTableMD
argument_list|()
argument_list|)
condition|)
block|{
comment|// If it contains an aggregate and it is not a full acid table,
comment|// we do not rewrite it (we need MERGE support)
throw|throw
operator|new
name|ReturnedValue
argument_list|(
literal|false
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|node
operator|instanceof
name|Project
condition|)
block|{
comment|// We can continue
name|super
operator|.
name|visit
argument_list|(
name|node
argument_list|,
name|ordinal
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ReturnedValue
argument_list|(
literal|false
argument_list|)
throw|;
block|}
block|}
block|}
operator|.
name|go
argument_list|(
name|union
operator|.
name|getInput
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// We pass all the checks, we can rewrite
throw|throw
operator|new
name|ReturnedValue
argument_list|(
literal|true
argument_list|)
throw|;
block|}
comment|/**    * Starts an iteration.    */
specifier|public
name|RelNode
name|go
parameter_list|(
name|RelNode
name|p
parameter_list|)
block|{
try|try
block|{
name|visit
argument_list|(
name|p
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReturnedValue
name|e
parameter_list|)
block|{
comment|// Rewriting cannot be performed
name|rewritingAllowed
operator|=
name|e
operator|.
name|value
expr_stmt|;
block|}
return|return
name|p
return|;
block|}
specifier|public
name|boolean
name|isContainsAggregate
parameter_list|()
block|{
return|return
name|containsAggregate
return|;
block|}
specifier|public
name|boolean
name|isRewritingAllowed
parameter_list|()
block|{
return|return
name|rewritingAllowed
return|;
block|}
comment|/**    * Exception used to interrupt a visitor walk.    */
specifier|private
specifier|static
class|class
name|ReturnedValue
extends|extends
name|ControlFlowException
block|{
specifier|private
specifier|final
name|boolean
name|value
decl_stmt|;
specifier|public
name|ReturnedValue
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

