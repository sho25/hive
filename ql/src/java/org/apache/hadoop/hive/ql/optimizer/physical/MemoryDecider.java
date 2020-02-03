begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Comparator
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
name|Iterator
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
name|LinkedHashSet
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
name|SortedSet
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
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|MapJoinOperator
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
name|tez
operator|.
name|TezTask
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
name|SemanticDispatcher
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
name|SemanticGraphWalker
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
name|SemanticNodeProcessor
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
name|SemanticRule
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
name|lib
operator|.
name|TaskGraphWalker
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
name|BaseWork
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
name|MergeJoinWork
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
name|ReduceWork
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
name|TezWork
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
comment|/**  * MemoryDecider is a simple physical optimizer that adjusts the memory layout of tez tasks.  * Currently it only cares about hash table sizes for the graceful hash join.  * It tried to keep hashtables that are small and early in the operator pipeline completely  * in memory.  */
end_comment

begin_class
specifier|public
class|class
name|MemoryDecider
implements|implements
name|PhysicalPlanResolver
block|{
specifier|protected
specifier|static
specifier|transient
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|MemoryDecider
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
class|class
name|MemoryCalculator
implements|implements
name|SemanticDispatcher
block|{
specifier|private
specifier|final
name|long
name|totalAvailableMemory
decl_stmt|;
comment|// how much to we have
specifier|private
specifier|final
name|long
name|minimumHashTableSize
decl_stmt|;
comment|// minimum size of ht completely in memory
specifier|private
specifier|final
name|double
name|inflationFactor
decl_stmt|;
comment|// blowout factor datasize -> memory size
specifier|private
specifier|final
name|PhysicalContext
name|pctx
decl_stmt|;
specifier|public
name|MemoryCalculator
parameter_list|(
name|PhysicalContext
name|pctx
parameter_list|)
block|{
name|this
operator|.
name|pctx
operator|=
name|pctx
expr_stmt|;
name|this
operator|.
name|totalAvailableMemory
operator|=
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|pctx
operator|.
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVECONVERTJOINNOCONDITIONALTASKTHRESHOLD
argument_list|)
expr_stmt|;
name|this
operator|.
name|minimumHashTableSize
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|pctx
operator|.
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHYBRIDGRACEHASHJOINMINNUMPARTITIONS
argument_list|)
operator|*
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|pctx
operator|.
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHYBRIDGRACEHASHJOINMINWBSIZE
argument_list|)
expr_stmt|;
name|this
operator|.
name|inflationFactor
operator|=
name|HiveConf
operator|.
name|getFloatVar
argument_list|(
name|pctx
operator|.
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_HASH_TABLE_INFLATION_FACTOR
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
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
argument_list|>
name|currTask
init|=
operator|(
name|Task
argument_list|<
name|?
argument_list|>
operator|)
name|nd
decl_stmt|;
if|if
condition|(
name|currTask
operator|instanceof
name|StatsTask
condition|)
block|{
name|currTask
operator|=
operator|(
operator|(
name|StatsTask
operator|)
name|currTask
operator|)
operator|.
name|getWork
argument_list|()
operator|.
name|getSourceTask
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|currTask
operator|instanceof
name|TezTask
condition|)
block|{
name|TezWork
name|work
init|=
operator|(
operator|(
name|TezTask
operator|)
name|currTask
operator|)
operator|.
name|getWork
argument_list|()
decl_stmt|;
for|for
control|(
name|BaseWork
name|w
range|:
name|work
operator|.
name|getAllWork
argument_list|()
control|)
block|{
name|evaluateWork
argument_list|(
name|w
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|evaluateWork
parameter_list|(
name|BaseWork
name|w
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|w
operator|instanceof
name|MapWork
condition|)
block|{
name|evaluateMapWork
argument_list|(
operator|(
name|MapWork
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|w
operator|instanceof
name|ReduceWork
condition|)
block|{
name|evaluateReduceWork
argument_list|(
operator|(
name|ReduceWork
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|w
operator|instanceof
name|MergeJoinWork
condition|)
block|{
name|evaluateMergeWork
argument_list|(
operator|(
name|MergeJoinWork
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"We are not going to evaluate this work type: "
operator|+
name|w
operator|.
name|getClass
argument_list|()
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|evaluateMergeWork
parameter_list|(
name|MergeJoinWork
name|w
parameter_list|)
throws|throws
name|SemanticException
block|{
for|for
control|(
name|BaseWork
name|baseWork
range|:
name|w
operator|.
name|getBaseWorkList
argument_list|()
control|)
block|{
name|evaluateOperators
argument_list|(
name|baseWork
argument_list|,
name|pctx
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|evaluateReduceWork
parameter_list|(
name|ReduceWork
name|w
parameter_list|)
throws|throws
name|SemanticException
block|{
name|evaluateOperators
argument_list|(
name|w
argument_list|,
name|pctx
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|evaluateMapWork
parameter_list|(
name|MapWork
name|w
parameter_list|)
throws|throws
name|SemanticException
block|{
name|evaluateOperators
argument_list|(
name|w
argument_list|,
name|pctx
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|evaluateOperators
parameter_list|(
name|BaseWork
name|w
parameter_list|,
name|PhysicalContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// lets take a look at the operator memory requirements.
name|SemanticDispatcher
name|disp
init|=
literal|null
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|MapJoinOperator
argument_list|>
name|mapJoins
init|=
operator|new
name|LinkedHashSet
argument_list|<
name|MapJoinOperator
argument_list|>
argument_list|()
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|SemanticRule
argument_list|,
name|SemanticNodeProcessor
argument_list|>
name|rules
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|SemanticRule
argument_list|,
name|SemanticNodeProcessor
argument_list|>
argument_list|()
decl_stmt|;
name|rules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"Map join memory estimator"
argument_list|,
name|MapJoinOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
argument_list|)
argument_list|,
operator|new
name|SemanticNodeProcessor
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
block|{
name|mapJoins
operator|.
name|add
argument_list|(
operator|(
name|MapJoinOperator
operator|)
name|nd
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|disp
operator|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
literal|null
argument_list|,
name|rules
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|SemanticGraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
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
name|topNodes
operator|.
name|addAll
argument_list|(
name|w
operator|.
name|getAllRootOperators
argument_list|()
argument_list|)
expr_stmt|;
name|LinkedHashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
name|nodeOutput
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Node
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
name|nodeOutput
argument_list|)
expr_stmt|;
if|if
condition|(
name|mapJoins
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return;
block|}
try|try
block|{
name|long
name|total
init|=
literal|0
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|MapJoinOperator
argument_list|,
name|Long
argument_list|>
name|sizes
init|=
operator|new
name|HashMap
argument_list|<
name|MapJoinOperator
argument_list|,
name|Long
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|MapJoinOperator
argument_list|,
name|Integer
argument_list|>
name|positions
init|=
operator|new
name|HashMap
argument_list|<
name|MapJoinOperator
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|MapJoinOperator
name|mj
range|:
name|mapJoins
control|)
block|{
name|long
name|size
init|=
name|computeSizeToFitInMem
argument_list|(
name|mj
argument_list|)
decl_stmt|;
name|sizes
operator|.
name|put
argument_list|(
name|mj
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|positions
operator|.
name|put
argument_list|(
name|mj
argument_list|,
name|i
operator|++
argument_list|)
expr_stmt|;
name|total
operator|+=
name|size
expr_stmt|;
block|}
name|Comparator
argument_list|<
name|MapJoinOperator
argument_list|>
name|comp
init|=
operator|new
name|Comparator
argument_list|<
name|MapJoinOperator
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|MapJoinOperator
name|mj1
parameter_list|,
name|MapJoinOperator
name|mj2
parameter_list|)
block|{
if|if
condition|(
name|mj1
operator|==
literal|null
operator|||
name|mj2
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|()
throw|;
block|}
name|int
name|res
init|=
name|Long
operator|.
name|compare
argument_list|(
name|sizes
operator|.
name|get
argument_list|(
name|mj1
argument_list|)
argument_list|,
name|sizes
operator|.
name|get
argument_list|(
name|mj2
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|==
literal|0
condition|)
block|{
name|res
operator|=
name|Integer
operator|.
name|compare
argument_list|(
name|positions
operator|.
name|get
argument_list|(
name|mj1
argument_list|)
argument_list|,
name|positions
operator|.
name|get
argument_list|(
name|mj2
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|res
return|;
block|}
block|}
decl_stmt|;
name|SortedSet
argument_list|<
name|MapJoinOperator
argument_list|>
name|sortedMapJoins
init|=
operator|new
name|TreeSet
argument_list|<
name|MapJoinOperator
argument_list|>
argument_list|(
name|comp
argument_list|)
decl_stmt|;
name|sortedMapJoins
operator|.
name|addAll
argument_list|(
name|mapJoins
argument_list|)
expr_stmt|;
name|long
name|remainingSize
init|=
name|totalAvailableMemory
operator|/
literal|2
decl_stmt|;
name|Iterator
argument_list|<
name|MapJoinOperator
argument_list|>
name|it
init|=
name|sortedMapJoins
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|long
name|totalLargeJoins
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|MapJoinOperator
name|mj
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|long
name|size
init|=
name|sizes
operator|.
name|get
argument_list|(
name|mj
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"MapJoin: "
operator|+
name|mj
operator|+
literal|", size: "
operator|+
name|size
operator|+
literal|", remaining: "
operator|+
name|remainingSize
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|size
operator|<
name|remainingSize
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting "
operator|+
name|size
operator|+
literal|" bytes needed for "
operator|+
name|mj
operator|+
literal|" (in-mem)"
argument_list|)
expr_stmt|;
block|}
name|mj
operator|.
name|getConf
argument_list|()
operator|.
name|setMemoryNeeded
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|remainingSize
operator|-=
name|size
expr_stmt|;
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|totalLargeJoins
operator|+=
name|sizes
operator|.
name|get
argument_list|(
name|mj
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|sortedMapJoins
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// all of the joins fit into half the memory. Let's be safe and scale them out.
name|sortedMapJoins
operator|.
name|addAll
argument_list|(
name|mapJoins
argument_list|)
expr_stmt|;
name|totalLargeJoins
operator|=
name|total
expr_stmt|;
if|if
condition|(
name|totalLargeJoins
operator|>
name|totalAvailableMemory
condition|)
block|{
comment|// this shouldn't happen
throw|throw
operator|new
name|HiveException
argument_list|()
throw|;
block|}
name|remainingSize
operator|=
name|totalAvailableMemory
operator|/
literal|2
expr_stmt|;
block|}
comment|// we used half the mem for small joins, now let's scale the rest
name|double
name|weight
init|=
operator|(
name|remainingSize
operator|+
name|totalAvailableMemory
operator|/
literal|2
operator|)
operator|/
operator|(
name|double
operator|)
name|totalLargeJoins
decl_stmt|;
for|for
control|(
name|MapJoinOperator
name|mj
range|:
name|sortedMapJoins
control|)
block|{
name|long
name|size
init|=
call|(
name|long
call|)
argument_list|(
name|weight
operator|*
name|sizes
operator|.
name|get
argument_list|(
name|mj
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting "
operator|+
name|size
operator|+
literal|" bytes needed for "
operator|+
name|mj
operator|+
literal|" (spills)"
argument_list|)
expr_stmt|;
block|}
name|mj
operator|.
name|getConf
argument_list|()
operator|.
name|setMemoryNeeded
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
comment|// if we have issues with stats, just scale linearily
name|long
name|size
init|=
name|totalAvailableMemory
operator|/
name|mapJoins
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Scaling mapjoin memory w/o stats"
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|MapJoinOperator
name|mj
range|:
name|mapJoins
control|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting "
operator|+
name|size
operator|+
literal|" bytes needed for "
operator|+
name|mj
operator|+
literal|" (fallback)"
argument_list|)
expr_stmt|;
block|}
name|mj
operator|.
name|getConf
argument_list|()
operator|.
name|setMemoryNeeded
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|long
name|computeSizeToFitInMem
parameter_list|(
name|MapJoinOperator
name|mj
parameter_list|)
throws|throws
name|HiveException
block|{
return|return
call|(
name|long
call|)
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|this
operator|.
name|minimumHashTableSize
argument_list|,
name|computeInputSize
argument_list|(
name|mj
argument_list|)
argument_list|)
operator|*
name|this
operator|.
name|inflationFactor
argument_list|)
return|;
block|}
specifier|private
name|long
name|computeInputSize
parameter_list|(
name|MapJoinOperator
name|mj
parameter_list|)
throws|throws
name|HiveException
block|{
name|long
name|size
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|mj
operator|.
name|getConf
argument_list|()
operator|!=
literal|null
operator|&&
name|mj
operator|.
name|getConf
argument_list|()
operator|.
name|getParentDataSizes
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|long
name|l
range|:
name|mj
operator|.
name|getConf
argument_list|()
operator|.
name|getParentDataSizes
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|size
operator|+=
name|l
expr_stmt|;
block|}
block|}
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"No data sizes"
argument_list|)
throw|;
block|}
return|return
name|size
return|;
block|}
specifier|public
class|class
name|DefaultRule
implements|implements
name|SemanticNodeProcessor
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
block|}
annotation|@
name|Override
specifier|public
name|PhysicalContext
name|resolve
parameter_list|(
name|PhysicalContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|pctx
operator|.
name|getConf
argument_list|()
expr_stmt|;
comment|// create dispatcher and graph walker
name|SemanticDispatcher
name|disp
init|=
operator|new
name|MemoryCalculator
argument_list|(
name|pctx
argument_list|)
decl_stmt|;
name|TaskGraphWalker
name|ogw
init|=
operator|new
name|TaskGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
comment|// get all the tasks nodes from root task
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
name|topNodes
operator|.
name|addAll
argument_list|(
name|pctx
operator|.
name|getRootTasks
argument_list|()
argument_list|)
expr_stmt|;
comment|// begin to walk through the task tree.
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
name|pctx
return|;
block|}
block|}
end_class

end_unit

