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
name|optimizer
operator|.
name|physical
operator|.
name|index
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|Stack
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|Index
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
name|cache
operator|.
name|CacheUtils
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
name|Operator
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
name|TableScanOperator
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
name|index
operator|.
name|bitmap
operator|.
name|BitmapIndexHandler
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
name|index
operator|.
name|compact
operator|.
name|CompactIndexHandler
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
name|DefaultGraphWalker
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
name|DefaultRuleDispatcher
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
name|Dispatcher
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
name|GraphWalker
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
name|lib
operator|.
name|NodeProcessor
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
name|NodeProcessorCtx
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
name|Rule
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
name|RuleRegExp
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
name|optimizer
operator|.
name|IndexUtils
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
name|physical
operator|.
name|PhysicalContext
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
name|ParseContext
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
name|MapredWork
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
name|OperatorDesc
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
name|common
operator|.
name|util
operator|.
name|HiveStringUtils
import|;
end_import

begin_comment
comment|/**  *  * IndexWhereTaskDispatcher.  Walks a Task tree, and for the right kind of Task,  * walks the operator tree to create an index subquery.  Then attaches the  * subquery task to the task tree.  *  */
end_comment

begin_class
specifier|public
class|class
name|IndexWhereTaskDispatcher
implements|implements
name|Dispatcher
block|{
specifier|private
specifier|final
name|PhysicalContext
name|physicalContext
decl_stmt|;
comment|// To store table to index mapping
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Index
argument_list|>
argument_list|>
name|indexMap
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|supportedIndexes
decl_stmt|;
specifier|public
name|IndexWhereTaskDispatcher
parameter_list|(
name|PhysicalContext
name|context
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|physicalContext
operator|=
name|context
expr_stmt|;
name|indexMap
operator|=
name|Maps
operator|.
name|newHashMap
argument_list|()
expr_stmt|;
name|supportedIndexes
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|supportedIndexes
operator|.
name|add
argument_list|(
name|CompactIndexHandler
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|supportedIndexes
operator|.
name|add
argument_list|(
name|BitmapIndexHandler
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|dispatch
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
init|=
operator|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|nd
decl_stmt|;
name|ParseContext
name|pctx
init|=
name|physicalContext
operator|.
name|getParseContext
argument_list|()
decl_stmt|;
comment|// create the regex's so the walker can recognize our WHERE queries
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|operatorRules
init|=
name|createOperatorRules
argument_list|(
name|pctx
argument_list|)
decl_stmt|;
comment|// check for no indexes on any table
if|if
condition|(
name|operatorRules
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// create context so the walker can carry the current task with it.
name|IndexWhereProcCtx
name|indexWhereOptimizeCtx
init|=
operator|new
name|IndexWhereProcCtx
argument_list|(
name|task
argument_list|,
name|pctx
argument_list|)
decl_stmt|;
comment|// create the dispatcher, which fires the processor according to the rule that
comment|// best matches
name|Dispatcher
name|dispatcher
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|getDefaultProcessor
argument_list|()
argument_list|,
name|operatorRules
argument_list|,
name|indexWhereOptimizeCtx
argument_list|)
decl_stmt|;
comment|// walk the mapper operator(not task) tree for each specific task
name|GraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|dispatcher
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Node
argument_list|>
name|topNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|task
operator|.
name|getWork
argument_list|()
operator|instanceof
name|MapredWork
condition|)
block|{
name|topNodes
operator|.
name|addAll
argument_list|(
operator|(
operator|(
name|MapredWork
operator|)
name|task
operator|.
name|getWork
argument_list|()
operator|)
operator|.
name|getMapWork
argument_list|()
operator|.
name|getAliasToWork
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|private
name|List
argument_list|<
name|Index
argument_list|>
name|getIndex
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
name|indexCacheKey
init|=
name|CacheUtils
operator|.
name|buildKey
argument_list|(
name|HiveStringUtils
operator|.
name|normalizeIdentifier
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|)
argument_list|,
name|HiveStringUtils
operator|.
name|normalizeIdentifier
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Index
argument_list|>
name|indexList
init|=
name|indexMap
operator|.
name|get
argument_list|(
name|indexCacheKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexList
operator|==
literal|null
condition|)
block|{
name|indexList
operator|=
name|IndexUtils
operator|.
name|getIndexes
argument_list|(
name|table
argument_list|,
name|supportedIndexes
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexList
operator|==
literal|null
condition|)
block|{
name|indexList
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
block|}
name|indexMap
operator|.
name|put
argument_list|(
name|indexCacheKey
argument_list|,
name|indexList
argument_list|)
expr_stmt|;
block|}
return|return
name|indexList
return|;
block|}
comment|/**    * Create a set of rules that only matches WHERE predicates on columns we have    * an index on.    * @return    */
specifier|private
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|createOperatorRules
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|operatorRules
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
argument_list|()
decl_stmt|;
comment|// query the metastore to know what columns we have indexed
name|Map
argument_list|<
name|TableScanOperator
argument_list|,
name|List
argument_list|<
name|Index
argument_list|>
argument_list|>
name|indexes
init|=
operator|new
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|List
argument_list|<
name|Index
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
range|:
name|pctx
operator|.
name|getTopOps
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|op
operator|instanceof
name|TableScanOperator
condition|)
block|{
name|List
argument_list|<
name|Index
argument_list|>
name|tblIndexes
init|=
name|getIndex
argument_list|(
operator|(
operator|(
name|TableScanOperator
operator|)
name|op
operator|)
operator|.
name|getConf
argument_list|()
operator|.
name|getTableMetadata
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|tblIndexes
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|indexes
operator|.
name|put
argument_list|(
operator|(
name|TableScanOperator
operator|)
name|op
argument_list|,
name|tblIndexes
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// quit if our tables don't have any indexes
if|if
condition|(
name|indexes
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// We set the pushed predicate from the WHERE clause as the filter expr on
comment|// all table scan operators, so we look for table scan operators(TS%)
name|operatorRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"RULEWhere"
argument_list|,
name|TableScanOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|IndexWhereProcessor
argument_list|(
name|indexes
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|operatorRules
return|;
block|}
specifier|private
name|NodeProcessor
name|getDefaultProcessor
parameter_list|()
block|{
return|return
operator|new
name|NodeProcessor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
literal|null
return|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

