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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashSet
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
name|java
operator|.
name|util
operator|.
name|Stack
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
name|ContentSummary
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
name|exec
operator|.
name|mr
operator|.
name|MapRedTask
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
name|index
operator|.
name|HiveIndexHandler
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
name|HiveIndexQueryContext
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
name|ExprNodeDesc
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
name|MapWork
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
name|TableDesc
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
name|TableScanDesc
import|;
end_import

begin_comment
comment|/** * * IndexWhereProcessor. * Processes Operator Nodes to look for WHERE queries with a predicate column * on which we have an index.  Creates an index subquery Task for these * WHERE queries to use the index automatically. */
end_comment

begin_class
specifier|public
class|class
name|IndexWhereProcessor
implements|implements
name|NodeProcessor
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
name|IndexWhereProcessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|TableScanOperator
argument_list|,
name|List
argument_list|<
name|Index
argument_list|>
argument_list|>
name|tsToIndices
decl_stmt|;
specifier|public
name|IndexWhereProcessor
parameter_list|(
name|Map
argument_list|<
name|TableScanOperator
argument_list|,
name|List
argument_list|<
name|Index
argument_list|>
argument_list|>
name|tsToIndices
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|tsToIndices
operator|=
name|tsToIndices
expr_stmt|;
block|}
annotation|@
name|Override
comment|/**    * Process a node of the operator tree. This matches on the rule in IndexWhereTaskDispatcher    */
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
name|TableScanOperator
name|operator
init|=
operator|(
name|TableScanOperator
operator|)
name|nd
decl_stmt|;
name|List
argument_list|<
name|Node
argument_list|>
name|opChildren
init|=
name|operator
operator|.
name|getChildren
argument_list|()
decl_stmt|;
name|TableScanDesc
name|operatorDesc
init|=
name|operator
operator|.
name|getConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|operatorDesc
operator|==
literal|null
operator|||
operator|!
name|tsToIndices
operator|.
name|containsKey
argument_list|(
name|operator
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|Index
argument_list|>
name|indexes
init|=
name|tsToIndices
operator|.
name|get
argument_list|(
name|operator
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|predicate
init|=
name|operatorDesc
operator|.
name|getFilterExpr
argument_list|()
decl_stmt|;
name|IndexWhereProcCtx
name|context
init|=
operator|(
name|IndexWhereProcCtx
operator|)
name|procCtx
decl_stmt|;
name|ParseContext
name|pctx
init|=
name|context
operator|.
name|getParseContext
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Processing predicate for index optimization"
argument_list|)
expr_stmt|;
if|if
condition|(
name|predicate
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"null predicate pushed down"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|LOG
operator|.
name|info
argument_list|(
name|predicate
operator|.
name|getExprString
argument_list|()
argument_list|)
expr_stmt|;
comment|// check if we have tsToIndices on all partitions in this table scan
name|Set
argument_list|<
name|Partition
argument_list|>
name|queryPartitions
decl_stmt|;
try|try
block|{
name|queryPartitions
operator|=
name|IndexUtils
operator|.
name|checkPartitionsCoveredByIndex
argument_list|(
name|operator
argument_list|,
name|pctx
argument_list|,
name|indexes
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryPartitions
operator|==
literal|null
condition|)
block|{
comment|// partitions not covered
return|return
literal|null
return|;
block|}
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
literal|"Fatal Error: problem accessing metastore"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
comment|// we can only process MapReduce tasks to check input size
if|if
condition|(
operator|!
name|context
operator|.
name|getCurrentTask
argument_list|()
operator|.
name|isMapRedTask
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|MapRedTask
name|currentTask
init|=
operator|(
name|MapRedTask
operator|)
name|context
operator|.
name|getCurrentTask
argument_list|()
decl_stmt|;
comment|// get potential reentrant index queries from each index
name|Map
argument_list|<
name|Index
argument_list|,
name|HiveIndexQueryContext
argument_list|>
name|queryContexts
init|=
operator|new
name|HashMap
argument_list|<
name|Index
argument_list|,
name|HiveIndexQueryContext
argument_list|>
argument_list|()
decl_stmt|;
comment|// make sure we have an index on the table being scanned
name|TableDesc
name|tblDesc
init|=
name|operator
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Index
argument_list|>
argument_list|>
name|indexesByType
init|=
operator|new
name|HashMap
argument_list|<
name|String
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
name|Index
name|indexOnTable
range|:
name|indexes
control|)
block|{
if|if
condition|(
name|indexesByType
operator|.
name|get
argument_list|(
name|indexOnTable
operator|.
name|getIndexHandlerClass
argument_list|()
argument_list|)
operator|==
literal|null
condition|)
block|{
name|List
argument_list|<
name|Index
argument_list|>
name|newType
init|=
operator|new
name|ArrayList
argument_list|<
name|Index
argument_list|>
argument_list|()
decl_stmt|;
name|newType
operator|.
name|add
argument_list|(
name|indexOnTable
argument_list|)
expr_stmt|;
name|indexesByType
operator|.
name|put
argument_list|(
name|indexOnTable
operator|.
name|getIndexHandlerClass
argument_list|()
argument_list|,
name|newType
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|indexesByType
operator|.
name|get
argument_list|(
name|indexOnTable
operator|.
name|getIndexHandlerClass
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|indexOnTable
argument_list|)
expr_stmt|;
block|}
block|}
comment|// choose index type with most tsToIndices of the same type on the table
comment|// TODO HIVE-2130 This would be a good place for some sort of cost based choice?
name|List
argument_list|<
name|Index
argument_list|>
name|bestIndexes
init|=
name|indexesByType
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|Index
argument_list|>
name|indexTypes
range|:
name|indexesByType
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|bestIndexes
operator|.
name|size
argument_list|()
operator|<
name|indexTypes
operator|.
name|size
argument_list|()
condition|)
block|{
name|bestIndexes
operator|=
name|indexTypes
expr_stmt|;
block|}
block|}
comment|// rewrite index queries for the chosen index type
name|HiveIndexQueryContext
name|tmpQueryContext
init|=
operator|new
name|HiveIndexQueryContext
argument_list|()
decl_stmt|;
name|tmpQueryContext
operator|.
name|setQueryPartitions
argument_list|(
name|queryPartitions
argument_list|)
expr_stmt|;
name|rewriteForIndexes
argument_list|(
name|predicate
argument_list|,
name|bestIndexes
argument_list|,
name|pctx
argument_list|,
name|currentTask
argument_list|,
name|tmpQueryContext
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|indexTasks
init|=
name|tmpQueryContext
operator|.
name|getQueryTasks
argument_list|()
decl_stmt|;
if|if
condition|(
name|indexTasks
operator|!=
literal|null
operator|&&
name|indexTasks
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|queryContexts
operator|.
name|put
argument_list|(
name|bestIndexes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|tmpQueryContext
argument_list|)
expr_stmt|;
block|}
comment|// choose an index rewrite to use
if|if
condition|(
name|queryContexts
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// TODO HIVE-2130 This would be a good place for some sort of cost based choice?
name|Index
name|chosenIndex
init|=
name|queryContexts
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
comment|// modify the parse context to use indexing
comment|// we need to delay this until we choose one index so that we don't attempt to modify pctx multiple times
name|HiveIndexQueryContext
name|queryContext
init|=
name|queryContexts
operator|.
name|get
argument_list|(
name|chosenIndex
argument_list|)
decl_stmt|;
comment|// prepare the map reduce job to use indexing
name|MapWork
name|work
init|=
name|currentTask
operator|.
name|getWork
argument_list|()
operator|.
name|getMapWork
argument_list|()
decl_stmt|;
name|work
operator|.
name|setInputformat
argument_list|(
name|queryContext
operator|.
name|getIndexInputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|work
operator|.
name|addIndexIntermediateFile
argument_list|(
name|queryContext
operator|.
name|getIndexIntermediateFile
argument_list|()
argument_list|)
expr_stmt|;
comment|// modify inputs based on index query
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
init|=
name|pctx
operator|.
name|getSemanticInputs
argument_list|()
decl_stmt|;
name|inputs
operator|.
name|addAll
argument_list|(
name|queryContext
operator|.
name|getAdditionalSemanticInputs
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|chosenRewrite
init|=
name|queryContext
operator|.
name|getQueryTasks
argument_list|()
decl_stmt|;
comment|// add dependencies so index query runs first
name|insertIndexQuery
argument_list|(
name|pctx
argument_list|,
name|context
argument_list|,
name|chosenRewrite
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Get a list of Tasks to activate use of tsToIndices.    * Generate the tasks for the index query (where we store results of    * querying the index in a tmp file) inside the IndexHandler    * @param predicate Predicate of query to rewrite    * @param index Index to use for rewrite    * @param pctx    * @param task original task before rewrite    * @param queryContext stores return values    */
specifier|private
name|void
name|rewriteForIndexes
parameter_list|(
name|ExprNodeDesc
name|predicate
parameter_list|,
name|List
argument_list|<
name|Index
argument_list|>
name|indexes
parameter_list|,
name|ParseContext
name|pctx
parameter_list|,
name|Task
argument_list|<
name|MapredWork
argument_list|>
name|task
parameter_list|,
name|HiveIndexQueryContext
name|queryContext
parameter_list|)
throws|throws
name|SemanticException
block|{
name|HiveIndexHandler
name|indexHandler
decl_stmt|;
comment|// All tsToIndices in the list are of the same type, and therefore can use the
comment|// same handler to generate the index query tasks
name|Index
name|index
init|=
name|indexes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
try|try
block|{
name|indexHandler
operator|=
name|HiveUtils
operator|.
name|getIndexHandler
argument_list|(
name|pctx
operator|.
name|getConf
argument_list|()
argument_list|,
name|index
operator|.
name|getIndexHandlerClass
argument_list|()
argument_list|)
expr_stmt|;
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
literal|"Exception while loading IndexHandler: "
operator|+
name|index
operator|.
name|getIndexHandlerClass
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Failed to load indexHandler: "
operator|+
name|index
operator|.
name|getIndexHandlerClass
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// check the size
try|try
block|{
name|ContentSummary
name|inputSummary
init|=
name|Utilities
operator|.
name|getInputSummary
argument_list|(
name|pctx
operator|.
name|getContext
argument_list|()
argument_list|,
name|task
operator|.
name|getWork
argument_list|()
operator|.
name|getMapWork
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|long
name|inputSize
init|=
name|inputSummary
operator|.
name|getLength
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|indexHandler
operator|.
name|checkQuerySize
argument_list|(
name|inputSize
argument_list|,
name|pctx
operator|.
name|getConf
argument_list|()
argument_list|)
condition|)
block|{
name|queryContext
operator|.
name|setQueryTasks
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return;
block|}
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
literal|"Failed to get task size"
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// use the IndexHandler to generate the index query
name|indexHandler
operator|.
name|generateIndexQuery
argument_list|(
name|indexes
argument_list|,
name|predicate
argument_list|,
name|pctx
argument_list|,
name|queryContext
argument_list|)
expr_stmt|;
comment|// TODO HIVE-2115 use queryContext.residualPredicate to process residual predicate
return|return;
block|}
comment|/**    * Insert the rewrite tasks at the head of the pctx task tree    * @param pctx    * @param context    * @param chosenRewrite    */
specifier|private
name|void
name|insertIndexQuery
parameter_list|(
name|ParseContext
name|pctx
parameter_list|,
name|IndexWhereProcCtx
name|context
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|chosenRewrite
parameter_list|)
block|{
name|Task
argument_list|<
name|?
argument_list|>
name|wholeTableScan
init|=
name|context
operator|.
name|getCurrentTask
argument_list|()
decl_stmt|;
name|LinkedHashSet
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|rewriteLeaves
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|findLeaves
argument_list|(
name|chosenRewrite
argument_list|,
name|rewriteLeaves
argument_list|)
expr_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
argument_list|>
name|leaf
range|:
name|rewriteLeaves
control|)
block|{
name|leaf
operator|.
name|addDependentTask
argument_list|(
name|wholeTableScan
argument_list|)
expr_stmt|;
comment|// add full scan task as child for every index query task
block|}
comment|// replace the original with the index sub-query as a root task
name|pctx
operator|.
name|replaceRootTask
argument_list|(
name|wholeTableScan
argument_list|,
name|chosenRewrite
argument_list|)
expr_stmt|;
block|}
comment|/**    * Find the leaves of the task tree    */
specifier|private
name|void
name|findLeaves
parameter_list|(
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|tasks
parameter_list|,
name|Set
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|leaves
parameter_list|)
block|{
for|for
control|(
name|Task
argument_list|<
name|?
argument_list|>
name|t
range|:
name|tasks
control|)
block|{
if|if
condition|(
name|t
operator|.
name|getDependentTasks
argument_list|()
operator|==
literal|null
condition|)
block|{
name|leaves
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|findLeaves
argument_list|(
name|t
operator|.
name|getDependentTasks
argument_list|()
argument_list|,
name|leaves
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

