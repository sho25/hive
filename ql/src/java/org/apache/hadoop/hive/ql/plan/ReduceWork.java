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
name|plan
package|;
end_package

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
name|Arrays
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
name|exec
operator|.
name|FileSinkOperator
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
name|JoinOperator
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
name|OperatorUtils
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
name|VectorizerReason
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
name|BaseWork
operator|.
name|BaseExplainVectorization
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
name|Explain
operator|.
name|Level
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
name|Explain
operator|.
name|Vectorization
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
name|serde2
operator|.
name|Deserializer
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
name|serde2
operator|.
name|SerDeUtils
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|mapred
operator|.
name|JobConf
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
name|ReflectionUtil
import|;
end_import

begin_comment
comment|/**  * ReduceWork represents all the information used to run a reduce task on the cluster.  * It is first used when the query planner breaks the logical plan into tasks and  * used throughout physical optimization to track reduce-side operator plans, schema  * info about key/value pairs, etc  *  * ExecDriver will serialize the contents of this class and make sure it is  * distributed on the cluster. The ExecReducer will ultimately deserialize this  * class on the data nodes and setup it's operator pipeline accordingly.  *  * This class is also used in the explain command any property with the  * appropriate annotation will be displayed in the explain output.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"serial"
block|,
literal|"deprecation"
block|}
argument_list|)
specifier|public
class|class
name|ReduceWork
extends|extends
name|BaseWork
block|{
specifier|public
name|ReduceWork
parameter_list|()
block|{}
specifier|public
name|ReduceWork
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
comment|// schema of the map-reduce 'key' object - this is homogeneous
specifier|private
name|TableDesc
name|keyDesc
decl_stmt|;
comment|// schema of the map-reduce 'value' object - this is heterogeneous
specifier|private
name|List
argument_list|<
name|TableDesc
argument_list|>
name|tagToValueDesc
init|=
operator|new
name|ArrayList
argument_list|<
name|TableDesc
argument_list|>
argument_list|()
decl_stmt|;
comment|// first operator of the reduce task. (not the reducesinkoperator, but the
comment|// operator that handles the output of these, e.g.: JoinOperator).
specifier|private
name|Operator
argument_list|<
name|?
argument_list|>
name|reducer
decl_stmt|;
comment|// desired parallelism of the reduce task.
specifier|private
name|Integer
name|numReduceTasks
decl_stmt|;
comment|// boolean to signal whether tagging will be used (e.g.: join) or
comment|// not (e.g.: group by)
specifier|private
name|boolean
name|needsTagging
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|tagToInput
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// boolean that says whether tez auto reduce parallelism should be used
specifier|private
name|boolean
name|isAutoReduceParallelism
decl_stmt|;
comment|// boolean that says whether the data distribution is uniform hash (not java HashCode)
specifier|private
specifier|transient
name|boolean
name|isUniformDistribution
init|=
literal|false
decl_stmt|;
comment|// boolean that says whether to slow start or not
specifier|private
name|boolean
name|isSlowStart
init|=
literal|true
decl_stmt|;
comment|// for auto reduce parallelism - minimum reducers requested
specifier|private
name|int
name|minReduceTasks
decl_stmt|;
comment|// for auto reduce parallelism - max reducers requested
specifier|private
name|int
name|maxReduceTasks
decl_stmt|;
specifier|private
name|ObjectInspector
name|keyObjectInspector
init|=
literal|null
decl_stmt|;
specifier|private
name|ObjectInspector
name|valueObjectInspector
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|reduceVectorizationEnabled
decl_stmt|;
specifier|private
name|String
name|vectorReduceEngine
decl_stmt|;
specifier|private
name|String
name|vectorReduceColumnSortOrder
decl_stmt|;
specifier|private
name|String
name|vectorReduceColumnNullOrder
decl_stmt|;
specifier|private
specifier|transient
name|TezEdgeProperty
name|edgeProp
decl_stmt|;
comment|/**    * If the plan has a reducer and correspondingly a reduce-sink, then store the TableDesc pointing    * to keySerializeInfo of the ReduceSink    *    * @param keyDesc    */
specifier|public
name|void
name|setKeyDesc
parameter_list|(
specifier|final
name|TableDesc
name|keyDesc
parameter_list|)
block|{
name|this
operator|.
name|keyDesc
operator|=
name|keyDesc
expr_stmt|;
block|}
specifier|public
name|TableDesc
name|getKeyDesc
parameter_list|()
block|{
return|return
name|keyDesc
return|;
block|}
specifier|public
name|List
argument_list|<
name|TableDesc
argument_list|>
name|getTagToValueDesc
parameter_list|()
block|{
return|return
name|tagToValueDesc
return|;
block|}
specifier|public
name|void
name|setTagToValueDesc
parameter_list|(
specifier|final
name|List
argument_list|<
name|TableDesc
argument_list|>
name|tagToValueDesc
parameter_list|)
block|{
name|this
operator|.
name|tagToValueDesc
operator|=
name|tagToValueDesc
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Execution mode"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|,
name|vectorization
operator|=
name|Vectorization
operator|.
name|SUMMARY_PATH
argument_list|)
specifier|public
name|String
name|getExecutionMode
parameter_list|()
block|{
if|if
condition|(
name|vectorMode
condition|)
block|{
if|if
condition|(
name|llapMode
condition|)
block|{
if|if
condition|(
name|uberMode
condition|)
block|{
return|return
literal|"vectorized, uber"
return|;
block|}
else|else
block|{
return|return
literal|"vectorized, llap"
return|;
block|}
block|}
else|else
block|{
return|return
literal|"vectorized"
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|llapMode
condition|)
block|{
return|return
name|uberMode
condition|?
literal|"uber"
else|:
literal|"llap"
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Reduce Operator Tree"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|,
name|vectorization
operator|=
name|Vectorization
operator|.
name|OPERATOR_PATH
argument_list|)
specifier|public
name|Operator
argument_list|<
name|?
argument_list|>
name|getReducer
parameter_list|()
block|{
return|return
name|reducer
return|;
block|}
specifier|public
name|void
name|setReducer
parameter_list|(
specifier|final
name|Operator
argument_list|<
name|?
argument_list|>
name|reducer
parameter_list|)
block|{
name|this
operator|.
name|reducer
operator|=
name|reducer
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Needs Tagging"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|boolean
name|getNeedsTagging
parameter_list|()
block|{
return|return
name|needsTagging
return|;
block|}
specifier|public
name|void
name|setNeedsTagging
parameter_list|(
name|boolean
name|needsTagging
parameter_list|)
block|{
name|this
operator|.
name|needsTagging
operator|=
name|needsTagging
expr_stmt|;
block|}
specifier|public
name|void
name|setTagToInput
parameter_list|(
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|tagToInput
parameter_list|)
block|{
name|this
operator|.
name|tagToInput
operator|=
name|tagToInput
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"tagToInput"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|}
argument_list|)
specifier|public
name|Map
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
name|getTagToInput
parameter_list|()
block|{
return|return
name|tagToInput
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|replaceRoots
parameter_list|(
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|,
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|replacementMap
parameter_list|)
block|{
name|setReducer
argument_list|(
name|replacementMap
operator|.
name|get
argument_list|(
name|getReducer
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|getAllRootOperators
parameter_list|()
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|opSet
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|opSet
operator|.
name|add
argument_list|(
name|getReducer
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|opSet
return|;
block|}
annotation|@
name|Override
specifier|public
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|getAnyRootOperator
parameter_list|()
block|{
return|return
name|getReducer
argument_list|()
return|;
block|}
comment|/**    * If the number of reducers is -1, the runtime will automatically figure it    * out by input data size.    *    * The number of reducers will be a positive number only in case the target    * table is bucketed into N buckets (through CREATE TABLE). This feature is    * not supported yet, so the number of reducers will always be -1 for now.    */
specifier|public
name|Integer
name|getNumReduceTasks
parameter_list|()
block|{
return|return
name|numReduceTasks
return|;
block|}
specifier|public
name|void
name|setNumReduceTasks
parameter_list|(
specifier|final
name|Integer
name|numReduceTasks
parameter_list|)
block|{
name|this
operator|.
name|numReduceTasks
operator|=
name|numReduceTasks
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|configureJobConf
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
if|if
condition|(
name|reducer
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|FileSinkOperator
name|fs
range|:
name|OperatorUtils
operator|.
name|findOperators
argument_list|(
name|reducer
argument_list|,
name|FileSinkOperator
operator|.
name|class
argument_list|)
control|)
block|{
name|PlanUtils
operator|.
name|configureJobConf
argument_list|(
name|fs
operator|.
name|getConf
argument_list|()
operator|.
name|getTableInfo
argument_list|()
argument_list|,
name|job
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|setAutoReduceParallelism
parameter_list|(
name|boolean
name|isAutoReduceParallelism
parameter_list|)
block|{
name|this
operator|.
name|isAutoReduceParallelism
operator|=
name|isAutoReduceParallelism
expr_stmt|;
block|}
specifier|public
name|boolean
name|isAutoReduceParallelism
parameter_list|()
block|{
return|return
name|isAutoReduceParallelism
return|;
block|}
specifier|public
name|boolean
name|isSlowStart
parameter_list|()
block|{
return|return
name|isSlowStart
return|;
block|}
specifier|public
name|void
name|setSlowStart
parameter_list|(
name|boolean
name|isSlowStart
parameter_list|)
block|{
name|this
operator|.
name|isSlowStart
operator|=
name|isSlowStart
expr_stmt|;
block|}
comment|// ReducerTraits.UNIFORM
specifier|public
name|void
name|setUniformDistribution
parameter_list|(
name|boolean
name|isUniformDistribution
parameter_list|)
block|{
name|this
operator|.
name|isUniformDistribution
operator|=
name|isUniformDistribution
expr_stmt|;
block|}
specifier|public
name|boolean
name|isUniformDistribution
parameter_list|()
block|{
return|return
name|this
operator|.
name|isUniformDistribution
return|;
block|}
specifier|public
name|void
name|setMinReduceTasks
parameter_list|(
name|int
name|minReduceTasks
parameter_list|)
block|{
name|this
operator|.
name|minReduceTasks
operator|=
name|minReduceTasks
expr_stmt|;
block|}
specifier|public
name|int
name|getMinReduceTasks
parameter_list|()
block|{
return|return
name|minReduceTasks
return|;
block|}
specifier|public
name|int
name|getMaxReduceTasks
parameter_list|()
block|{
return|return
name|maxReduceTasks
return|;
block|}
specifier|public
name|void
name|setMaxReduceTasks
parameter_list|(
name|int
name|maxReduceTasks
parameter_list|)
block|{
name|this
operator|.
name|maxReduceTasks
operator|=
name|maxReduceTasks
expr_stmt|;
block|}
specifier|public
name|void
name|setReduceVectorizationEnabled
parameter_list|(
name|boolean
name|reduceVectorizationEnabled
parameter_list|)
block|{
name|this
operator|.
name|reduceVectorizationEnabled
operator|=
name|reduceVectorizationEnabled
expr_stmt|;
block|}
specifier|public
name|boolean
name|getReduceVectorizationEnabled
parameter_list|()
block|{
return|return
name|reduceVectorizationEnabled
return|;
block|}
specifier|public
name|void
name|setVectorReduceEngine
parameter_list|(
name|String
name|vectorReduceEngine
parameter_list|)
block|{
name|this
operator|.
name|vectorReduceEngine
operator|=
name|vectorReduceEngine
expr_stmt|;
block|}
specifier|public
name|String
name|getVectorReduceEngine
parameter_list|()
block|{
return|return
name|vectorReduceEngine
return|;
block|}
specifier|public
name|void
name|setVectorReduceColumnSortOrder
parameter_list|(
name|String
name|vectorReduceColumnSortOrder
parameter_list|)
block|{
name|this
operator|.
name|vectorReduceColumnSortOrder
operator|=
name|vectorReduceColumnSortOrder
expr_stmt|;
block|}
specifier|public
name|String
name|getVectorReduceColumnSortOrder
parameter_list|()
block|{
return|return
name|vectorReduceColumnSortOrder
return|;
block|}
specifier|public
name|void
name|setVectorReduceColumnNullOrder
parameter_list|(
name|String
name|vectorReduceColumnNullOrder
parameter_list|)
block|{
name|this
operator|.
name|vectorReduceColumnNullOrder
operator|=
name|vectorReduceColumnNullOrder
expr_stmt|;
block|}
specifier|public
name|String
name|getVectorReduceColumnNullOrder
parameter_list|()
block|{
return|return
name|vectorReduceColumnNullOrder
return|;
block|}
comment|// Use LinkedHashSet to give predictable display order.
specifier|private
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|reduceVectorizableEngines
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"tez"
argument_list|,
literal|"spark"
argument_list|)
argument_list|)
decl_stmt|;
specifier|public
class|class
name|ReduceExplainVectorization
extends|extends
name|BaseExplainVectorization
block|{
specifier|private
specifier|final
name|ReduceWork
name|reduceWork
decl_stmt|;
specifier|private
name|VectorizationCondition
index|[]
name|reduceVectorizationConditions
decl_stmt|;
specifier|public
name|ReduceExplainVectorization
parameter_list|(
name|ReduceWork
name|reduceWork
parameter_list|)
block|{
name|super
argument_list|(
name|reduceWork
argument_list|)
expr_stmt|;
name|this
operator|.
name|reduceWork
operator|=
name|reduceWork
expr_stmt|;
block|}
specifier|private
name|VectorizationCondition
index|[]
name|createReduceExplainVectorizationConditions
parameter_list|()
block|{
name|boolean
name|enabled
init|=
name|reduceWork
operator|.
name|getReduceVectorizationEnabled
argument_list|()
decl_stmt|;
name|String
name|engine
init|=
name|reduceWork
operator|.
name|getVectorReduceEngine
argument_list|()
decl_stmt|;
name|String
name|engineInSupportedCondName
init|=
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXECUTION_ENGINE
operator|.
name|varname
operator|+
literal|" "
operator|+
name|engine
operator|+
literal|" IN "
operator|+
name|reduceVectorizableEngines
decl_stmt|;
name|boolean
name|engineInSupported
init|=
name|reduceVectorizableEngines
operator|.
name|contains
argument_list|(
name|engine
argument_list|)
decl_stmt|;
name|VectorizationCondition
index|[]
name|conditions
init|=
operator|new
name|VectorizationCondition
index|[]
block|{
operator|new
name|VectorizationCondition
argument_list|(
name|enabled
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_VECTORIZATION_REDUCE_ENABLED
operator|.
name|varname
argument_list|)
block|,
operator|new
name|VectorizationCondition
argument_list|(
name|engineInSupported
argument_list|,
name|engineInSupportedCondName
argument_list|)
block|}
decl_stmt|;
return|return
name|conditions
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|SUMMARY
argument_list|,
name|displayName
operator|=
literal|"enableConditionsMet"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getEnableConditionsMet
parameter_list|()
block|{
if|if
condition|(
name|reduceVectorizationConditions
operator|==
literal|null
condition|)
block|{
name|reduceVectorizationConditions
operator|=
name|createReduceExplainVectorizationConditions
argument_list|()
expr_stmt|;
block|}
return|return
name|VectorizationCondition
operator|.
name|getConditionsMet
argument_list|(
name|reduceVectorizationConditions
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|SUMMARY
argument_list|,
name|displayName
operator|=
literal|"enableConditionsNotMet"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getEnableConditionsNotMet
parameter_list|()
block|{
if|if
condition|(
name|reduceVectorizationConditions
operator|==
literal|null
condition|)
block|{
name|reduceVectorizationConditions
operator|=
name|createReduceExplainVectorizationConditions
argument_list|()
expr_stmt|;
block|}
return|return
name|VectorizationCondition
operator|.
name|getConditionsNotMet
argument_list|(
name|reduceVectorizationConditions
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|DETAIL
argument_list|,
name|displayName
operator|=
literal|"reduceColumnSortOrder"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getReduceColumnSortOrder
parameter_list|()
block|{
if|if
condition|(
operator|!
name|getVectorizationExamined
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|reduceWork
operator|.
name|getVectorReduceColumnSortOrder
argument_list|()
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|DETAIL
argument_list|,
name|displayName
operator|=
literal|"reduceColumnNullOrder"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getReduceColumnNullOrder
parameter_list|()
block|{
if|if
condition|(
operator|!
name|getVectorizationExamined
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|reduceWork
operator|.
name|getVectorReduceColumnNullOrder
argument_list|()
return|;
block|}
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|SUMMARY
argument_list|,
name|displayName
operator|=
literal|"Reduce Vectorization"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|ReduceExplainVectorization
name|getReduceExplainVectorization
parameter_list|()
block|{
if|if
condition|(
operator|!
name|getVectorizationExamined
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|ReduceExplainVectorization
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|public
name|void
name|setEdgePropRef
parameter_list|(
name|TezEdgeProperty
name|edgeProp
parameter_list|)
block|{
name|this
operator|.
name|edgeProp
operator|=
name|edgeProp
expr_stmt|;
block|}
specifier|public
name|TezEdgeProperty
name|getEdgePropRef
parameter_list|()
block|{
return|return
name|edgeProp
return|;
block|}
block|}
end_class

end_unit

