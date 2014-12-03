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
name|spark
package|;
end_package

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
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|common
operator|.
name|ObjectPair
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
name|AppMasterEventOperator
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
name|GroupByOperator
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
name|MuxOperator
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
name|ReduceSinkOperator
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
name|UnionOperator
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
name|optimizer
operator|.
name|MapJoinProcessor
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
name|parse
operator|.
name|spark
operator|.
name|OptimizeSparkProcContext
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
name|DynamicPruningEventDesc
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
name|OpTraits
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|plan
operator|.
name|Statistics
import|;
end_import

begin_comment
comment|/**  * SparkMapJoinOptimizer cloned from ConvertJoinMapJoin is an optimization that replaces a common join  * (aka shuffle join) with a map join (aka broadcast or fragment replicate  * join when possible. Map joins have restrictions on which joins can be  * converted (e.g.: full outer joins cannot be handled as map joins) as well  * as memory restrictions (one side of the join has to fit into memory).  */
end_comment

begin_class
specifier|public
class|class
name|SparkMapJoinOptimizer
implements|implements
name|NodeProcessor
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SparkMapJoinOptimizer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
comment|/*    * (non-Javadoc) we should ideally not modify the tree we traverse. However,    * since we need to walk the tree at any time when we modify the operator, we    * might as well do it here.    */
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
name|OptimizeSparkProcContext
name|context
init|=
operator|(
name|OptimizeSparkProcContext
operator|)
name|procCtx
decl_stmt|;
name|HiveConf
name|conf
init|=
name|context
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|ParseContext
name|parseContext
init|=
name|context
operator|.
name|getParseContext
argument_list|()
decl_stmt|;
name|JoinOperator
name|joinOp
init|=
operator|(
name|JoinOperator
operator|)
name|nd
decl_stmt|;
if|if
condition|(
operator|!
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVECONVERTJOIN
argument_list|)
condition|)
block|{
comment|//&& !(conf.getBoolVar(HiveConf.ConfVars.HIVE_AUTO_SORTMERGE_JOIN))) {
comment|// we are just converting to a common merge join operator. The shuffle
comment|// join in map-reduce case.
comment|// int pos = 0; // it doesn't matter which position we use in this case.
comment|// convertJoinSMBJoin(joinOp, context, pos, 0, false, false);
return|return
literal|null
return|;
block|}
comment|// if we have traits, and table info is present in the traits, we know the
comment|// exact number of buckets. Else choose the largest number of estimated
comment|// reducers from the parent operators.
comment|//TODO  enable later. disabling this check for now
name|int
name|numBuckets
init|=
literal|1
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Estimated number of buckets "
operator|+
name|numBuckets
argument_list|)
expr_stmt|;
comment|/* TODO: handle this later     if (mapJoinConversionPos< 0) {       // we cannot convert to bucket map join, we cannot convert to       // map join either based on the size. Check if we can convert to SMB join.       if (conf.getBoolVar(HiveConf.ConfVars.HIVE_AUTO_SORTMERGE_JOIN) == false) {         convertJoinSMBJoin(joinOp, context, 0, 0, false, false);         return null;       }       Class<? extends BigTableSelectorForAutoSMJ> bigTableMatcherClass = null;       try {         bigTableMatcherClass =             (Class<? extends BigTableSelectorForAutoSMJ>) (Class.forName(HiveConf.getVar(                 parseContext.getConf(),                 HiveConf.ConfVars.HIVE_AUTO_SORTMERGE_JOIN_BIGTABLE_SELECTOR)));       } catch (ClassNotFoundException e) {         throw new SemanticException(e.getMessage());       }        BigTableSelectorForAutoSMJ bigTableMatcher =           ReflectionUtils.newInstance(bigTableMatcherClass, null);       JoinDesc joinDesc = joinOp.getConf();       JoinCondDesc[] joinCondns = joinDesc.getConds();       Set<Integer> joinCandidates = MapJoinProcessor.getBigTableCandidates(joinCondns);       if (joinCandidates.isEmpty()) {         // This is a full outer join. This can never be a map-join         // of any type. So return false.         return false;       }       mapJoinConversionPos =           bigTableMatcher.getBigTablePosition(parseContext, joinOp, joinCandidates);       if (mapJoinConversionPos< 0) {         // contains aliases from sub-query         // we are just converting to a common merge join operator. The shuffle         // join in map-reduce case.         int pos = 0; // it doesn't matter which position we use in this case.         convertJoinSMBJoin(joinOp, context, pos, 0, false, false);         return null;       }        if (checkConvertJoinSMBJoin(joinOp, context, mapJoinConversionPos, tezBucketJoinProcCtx)) {         convertJoinSMBJoin(joinOp, context, mapJoinConversionPos,             tezBucketJoinProcCtx.getNumBuckets(), tezBucketJoinProcCtx.isSubQuery(), true);       } else {         // we are just converting to a common merge join operator. The shuffle         // join in map-reduce case.         int pos = 0; // it doesn't matter which position we use in this case.         convertJoinSMBJoin(joinOp, context, pos, 0, false, false);       }       return null;     }      if (numBuckets> 1) {       if (conf.getBoolVar(HiveConf.ConfVars.HIVE_CONVERT_JOIN_BUCKET_MAPJOIN_TEZ)) {         if (convertJoinBucketMapJoin(joinOp, context, mapJoinConversionPos, tezBucketJoinProcCtx)) {           return null;         }       }     }*/
name|LOG
operator|.
name|info
argument_list|(
literal|"Convert to non-bucketed map join"
argument_list|)
expr_stmt|;
comment|// check if we can convert to map join no bucket scaling.
name|ObjectPair
argument_list|<
name|Integer
argument_list|,
name|Long
argument_list|>
name|mapJoinInfo
init|=
name|getMapJoinConversionInfo
argument_list|(
name|joinOp
argument_list|,
name|context
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|int
name|mapJoinConversionPos
init|=
name|mapJoinInfo
operator|.
name|getFirst
argument_list|()
decl_stmt|;
if|if
condition|(
name|mapJoinConversionPos
operator|<
literal|0
condition|)
block|{
comment|// we are just converting to a common merge join operator. The shuffle
comment|// join in map-reduce case.
comment|/*       int pos = 0; // it doesn't matter which position we use in this case.       convertJoinSMBJoin(joinOp, context, pos, 0, false, false);       */
return|return
literal|null
return|;
block|}
name|MapJoinOperator
name|mapJoinOp
init|=
name|convertJoinMapJoin
argument_list|(
name|joinOp
argument_list|,
name|context
argument_list|,
name|mapJoinConversionPos
argument_list|)
decl_stmt|;
comment|// map join operator by default has no bucket cols
name|mapJoinOp
operator|.
name|setOpTraits
argument_list|(
operator|new
name|OpTraits
argument_list|(
literal|null
argument_list|,
operator|-
literal|1
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|mapJoinOp
operator|.
name|setStatistics
argument_list|(
name|joinOp
operator|.
name|getStatistics
argument_list|()
argument_list|)
expr_stmt|;
comment|// propagate this change till the next RS
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|childOp
range|:
name|mapJoinOp
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
name|setAllChildrenTraitsToNull
argument_list|(
name|childOp
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|getMjOpSizes
argument_list|()
operator|.
name|put
argument_list|(
name|mapJoinOp
argument_list|,
name|mapJoinInfo
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|// replaces the join operator with a new CommonJoinOperator, removes the
comment|// parent reduce sinks
comment|/*   private void convertJoinSMBJoin(JoinOperator joinOp, OptimizeSparkProcContext context,       int mapJoinConversionPos, int numBuckets, boolean isSubQuery, boolean adjustParentsChildren)       throws SemanticException {     ParseContext parseContext = context.parseContext;     MapJoinDesc mapJoinDesc = null;     if (adjustParentsChildren) {         mapJoinDesc = MapJoinProcessor.getMapJoinDesc(context.conf, parseContext.getOpParseCtx(),             joinOp, parseContext.getJoinContext().get(joinOp), mapJoinConversionPos, true);     } else {       JoinDesc joinDesc = joinOp.getConf();       // retain the original join desc in the map join.       mapJoinDesc =           new MapJoinDesc(null, null, joinDesc.getExprs(), null, null,               joinDesc.getOutputColumnNames(), mapJoinConversionPos, joinDesc.getConds(),               joinDesc.getFilters(), joinDesc.getNoOuterJoin(), null);     }      @SuppressWarnings("unchecked")     CommonMergeJoinOperator mergeJoinOp =         (CommonMergeJoinOperator) OperatorFactory.get(new CommonMergeJoinDesc(numBuckets,             isSubQuery, mapJoinConversionPos, mapJoinDesc));     OpTraits opTraits =         new OpTraits(joinOp.getOpTraits().getBucketColNames(), numBuckets, joinOp.getOpTraits()             .getSortCols());     mergeJoinOp.setOpTraits(opTraits);     mergeJoinOp.setStatistics(joinOp.getStatistics());      for (Operator<? extends OperatorDesc> parentOp : joinOp.getParentOperators()) {       int pos = parentOp.getChildOperators().indexOf(joinOp);       parentOp.getChildOperators().remove(pos);       parentOp.getChildOperators().add(pos, mergeJoinOp);     }      for (Operator<? extends OperatorDesc> childOp : joinOp.getChildOperators()) {       int pos = childOp.getParentOperators().indexOf(joinOp);       childOp.getParentOperators().remove(pos);       childOp.getParentOperators().add(pos, mergeJoinOp);     }      List<Operator<? extends OperatorDesc>> childOperators = mergeJoinOp.getChildOperators();     if (childOperators == null) {       childOperators = new ArrayList<Operator<? extends OperatorDesc>>();       mergeJoinOp.setChildOperators(childOperators);     }      List<Operator<? extends OperatorDesc>> parentOperators = mergeJoinOp.getParentOperators();     if (parentOperators == null) {       parentOperators = new ArrayList<Operator<? extends OperatorDesc>>();       mergeJoinOp.setParentOperators(parentOperators);     }      childOperators.clear();     parentOperators.clear();     childOperators.addAll(joinOp.getChildOperators());     parentOperators.addAll(joinOp.getParentOperators());     mergeJoinOp.getConf().setGenJoinKeys(false);      if (adjustParentsChildren) {       mergeJoinOp.getConf().setGenJoinKeys(true);       List<Operator<? extends OperatorDesc>> newParentOpList =           new ArrayList<Operator<? extends OperatorDesc>>();       for (Operator<? extends OperatorDesc> parentOp : mergeJoinOp.getParentOperators()) {         for (Operator<? extends OperatorDesc> grandParentOp : parentOp.getParentOperators()) {           grandParentOp.getChildOperators().remove(parentOp);           grandParentOp.getChildOperators().add(mergeJoinOp);           newParentOpList.add(grandParentOp);         }       }       mergeJoinOp.getParentOperators().clear();       mergeJoinOp.getParentOperators().addAll(newParentOpList);       List<Operator<? extends OperatorDesc>> parentOps =           new ArrayList<Operator<? extends OperatorDesc>>(mergeJoinOp.getParentOperators());       for (Operator<? extends OperatorDesc> parentOp : parentOps) {         int parentIndex = mergeJoinOp.getParentOperators().indexOf(parentOp);         if (parentIndex == mapJoinConversionPos) {           continue;         }          // insert the dummy store operator here         DummyStoreOperator dummyStoreOp = new TezDummyStoreOperator();         dummyStoreOp.setParentOperators(new ArrayList<Operator<? extends OperatorDesc>>());         dummyStoreOp.setChildOperators(new ArrayList<Operator<? extends OperatorDesc>>());         dummyStoreOp.getChildOperators().add(mergeJoinOp);         int index = parentOp.getChildOperators().indexOf(mergeJoinOp);         parentOp.getChildOperators().remove(index);         parentOp.getChildOperators().add(index, dummyStoreOp);         dummyStoreOp.getParentOperators().add(parentOp);         mergeJoinOp.getParentOperators().remove(parentIndex);         mergeJoinOp.getParentOperators().add(parentIndex, dummyStoreOp);       }     }     mergeJoinOp.cloneOriginalParentsList(mergeJoinOp.getParentOperators());   }   */
specifier|private
name|void
name|setAllChildrenTraitsToNull
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|currentOp
parameter_list|)
block|{
if|if
condition|(
name|currentOp
operator|instanceof
name|ReduceSinkOperator
condition|)
block|{
return|return;
block|}
name|currentOp
operator|.
name|setOpTraits
argument_list|(
operator|new
name|OpTraits
argument_list|(
literal|null
argument_list|,
operator|-
literal|1
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|childOp
range|:
name|currentOp
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
if|if
condition|(
operator|(
name|childOp
operator|instanceof
name|ReduceSinkOperator
operator|)
operator|||
operator|(
name|childOp
operator|instanceof
name|GroupByOperator
operator|)
condition|)
block|{
break|break;
block|}
name|setAllChildrenTraitsToNull
argument_list|(
name|childOp
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|setNumberOfBucketsOnChildren
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|currentOp
parameter_list|)
block|{
name|int
name|numBuckets
init|=
name|currentOp
operator|.
name|getOpTraits
argument_list|()
operator|.
name|getNumBuckets
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
name|currentOp
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|op
operator|instanceof
name|ReduceSinkOperator
operator|)
operator|&&
operator|!
operator|(
name|op
operator|instanceof
name|GroupByOperator
operator|)
condition|)
block|{
name|op
operator|.
name|getOpTraits
argument_list|()
operator|.
name|setNumBuckets
argument_list|(
name|numBuckets
argument_list|)
expr_stmt|;
name|setNumberOfBucketsOnChildren
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    *   This method returns the big table position in a map-join. If the given join    *   cannot be converted to a map-join (This could happen for several reasons - one    *   of them being presence of 2 or more big tables that cannot fit in-memory), it returns -1.    *    *   Otherwise, it returns an int value that is the index of the big table in the set    *   MapJoinProcessor.bigTableCandidateSet    *    * @param joinOp    * @param context    * @param buckets    * @return pair, first value is the position, second value is the in-memory size of this mapjoin.    */
specifier|private
name|ObjectPair
argument_list|<
name|Integer
argument_list|,
name|Long
argument_list|>
name|getMapJoinConversionInfo
parameter_list|(
name|JoinOperator
name|joinOp
parameter_list|,
name|OptimizeSparkProcContext
name|context
parameter_list|,
name|int
name|buckets
parameter_list|)
block|{
name|Set
argument_list|<
name|Integer
argument_list|>
name|bigTableCandidateSet
init|=
name|MapJoinProcessor
operator|.
name|getBigTableCandidates
argument_list|(
name|joinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getConds
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|maxSize
init|=
name|context
operator|.
name|getConf
argument_list|()
operator|.
name|getLongVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVECONVERTJOINNOCONDITIONALTASKTHRESHOLD
argument_list|)
decl_stmt|;
name|int
name|bigTablePosition
init|=
operator|-
literal|1
decl_stmt|;
name|Statistics
name|bigInputStat
init|=
literal|null
decl_stmt|;
name|long
name|totalSize
init|=
literal|0
decl_stmt|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
comment|// bigTableFound means we've encountered a table that's bigger than the
comment|// max. This table is either the the big table or we cannot convert.
name|boolean
name|bigTableFound
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentOp
range|:
name|joinOp
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
name|Statistics
name|currInputStat
init|=
name|parentOp
operator|.
name|getStatistics
argument_list|()
decl_stmt|;
if|if
condition|(
name|currInputStat
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Couldn't get statistics from: "
operator|+
name|parentOp
argument_list|)
expr_stmt|;
return|return
operator|new
name|ObjectPair
argument_list|(
operator|-
literal|1
argument_list|,
literal|0
argument_list|)
return|;
block|}
comment|// Union is hard to handle. For instance, the following case:
comment|//  TS    TS
comment|//  |      |
comment|//  FIL   FIL
comment|//  |      |
comment|//  SEL   SEL
comment|//    \   /
comment|//    UNION
comment|//      |
comment|//      RS
comment|//      |
comment|//     JOIN
comment|// If we treat this as a MJ case, then after the RS is removed, we would
comment|// create two MapWorks, for each of the TS. Each of these MapWork will contain
comment|// a MJ operator, which is wrong.
comment|// Otherwise, we could try to break the op tree at the UNION, and create two MapWorks
comment|// for the branches above. Then, MJ will be in the following ReduceWork.
comment|// But, this is tricky to implement, and we'll leave it as a future work for now.
comment|// TODO: handle this as a MJ case
if|if
condition|(
name|containUnionWithoutRS
argument_list|(
name|parentOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
condition|)
block|{
return|return
operator|new
name|ObjectPair
argument_list|(
operator|-
literal|1
argument_list|,
literal|0
argument_list|)
return|;
block|}
name|long
name|inputSize
init|=
name|currInputStat
operator|.
name|getDataSize
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|bigInputStat
operator|==
literal|null
operator|)
operator|||
operator|(
operator|(
name|bigInputStat
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|inputSize
operator|>
name|bigInputStat
operator|.
name|getDataSize
argument_list|()
operator|)
operator|)
condition|)
block|{
if|if
condition|(
name|bigTableFound
condition|)
block|{
comment|// cannot convert to map join; we've already chosen a big table
comment|// on size and there's another one that's bigger.
return|return
operator|new
name|ObjectPair
argument_list|(
operator|-
literal|1
argument_list|,
literal|0
argument_list|)
return|;
block|}
if|if
condition|(
name|inputSize
operator|/
name|buckets
operator|>
name|maxSize
condition|)
block|{
if|if
condition|(
operator|!
name|bigTableCandidateSet
operator|.
name|contains
argument_list|(
name|pos
argument_list|)
condition|)
block|{
comment|// can't use the current table as the big table, but it's too
comment|// big for the map side.
return|return
operator|new
name|ObjectPair
argument_list|(
operator|-
literal|1
argument_list|,
literal|0
argument_list|)
return|;
block|}
name|bigTableFound
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|bigInputStat
operator|!=
literal|null
condition|)
block|{
comment|// we're replacing the current big table with a new one. Need
comment|// to count the current one as a map table then.
name|totalSize
operator|+=
name|bigInputStat
operator|.
name|getDataSize
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|totalSize
operator|/
name|buckets
operator|>
name|maxSize
condition|)
block|{
comment|// sum of small tables size in this join exceeds configured limit
comment|// hence cannot convert.
return|return
operator|new
name|ObjectPair
argument_list|(
operator|-
literal|1
argument_list|,
literal|0
argument_list|)
return|;
block|}
if|if
condition|(
name|bigTableCandidateSet
operator|.
name|contains
argument_list|(
name|pos
argument_list|)
condition|)
block|{
name|bigTablePosition
operator|=
name|pos
expr_stmt|;
name|bigInputStat
operator|=
name|currInputStat
expr_stmt|;
block|}
block|}
else|else
block|{
name|totalSize
operator|+=
name|currInputStat
operator|.
name|getDataSize
argument_list|()
expr_stmt|;
if|if
condition|(
name|totalSize
operator|/
name|buckets
operator|>
name|maxSize
condition|)
block|{
comment|// cannot hold all map tables in memory. Cannot convert.
return|return
operator|new
name|ObjectPair
argument_list|(
operator|-
literal|1
argument_list|,
literal|0
argument_list|)
return|;
block|}
block|}
name|pos
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|bigTablePosition
operator|==
operator|-
literal|1
condition|)
block|{
comment|//No big table candidates.
return|return
operator|new
name|ObjectPair
argument_list|(
operator|-
literal|1
argument_list|,
literal|0
argument_list|)
return|;
block|}
comment|//Final check, find size of already-calculated Mapjoin Operators in same work (spark-stage).  We need to factor
comment|//this in to prevent overwhelming Spark executor-memory.
name|long
name|connectedMapJoinSize
init|=
name|getConnectedMapJoinSize
argument_list|(
name|joinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|bigTablePosition
argument_list|)
argument_list|,
name|joinOp
argument_list|,
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|connectedMapJoinSize
operator|+
operator|(
name|totalSize
operator|/
name|buckets
operator|)
operator|)
operator|>
name|maxSize
condition|)
block|{
return|return
operator|new
name|ObjectPair
argument_list|(
operator|-
literal|1
argument_list|,
literal|0
argument_list|)
return|;
block|}
return|return
operator|new
name|ObjectPair
argument_list|(
name|bigTablePosition
argument_list|,
name|connectedMapJoinSize
operator|+
operator|(
name|totalSize
operator|/
name|buckets
operator|)
argument_list|)
return|;
block|}
comment|/**    * Examines this operator and all the connected operators, for mapjoins that will be in the same work.    * @param parentOp potential big-table parent operator, explore up from this.    * @param joinOp potential mapjoin operator, explore down from this.    * @param ctx context to pass information.    * @return total size of parent mapjoins in same work as this operator.    */
specifier|private
name|long
name|getConnectedMapJoinSize
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentOp
parameter_list|,
name|Operator
name|joinOp
parameter_list|,
name|OptimizeSparkProcContext
name|ctx
parameter_list|)
block|{
name|long
name|result
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|grandParentOp
range|:
name|parentOp
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
name|result
operator|+=
name|getConnectedParentMapJoinSize
argument_list|(
name|grandParentOp
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
name|result
operator|+=
name|getConnectedChildMapJoinSize
argument_list|(
name|joinOp
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * Examines this operator and all the parents, for mapjoins that will be in the same work.    * @param op given operator    * @param ctx context to pass information.    * @return total size of parent mapjoins in same work as this operator.    */
specifier|private
name|long
name|getConnectedParentMapJoinSize
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|,
name|OptimizeSparkProcContext
name|ctx
parameter_list|)
block|{
if|if
condition|(
operator|(
name|op
operator|instanceof
name|UnionOperator
operator|)
operator|||
operator|(
name|op
operator|instanceof
name|ReduceSinkOperator
operator|)
condition|)
block|{
comment|//Work Boundary, stop exploring.
return|return
literal|0
return|;
block|}
if|if
condition|(
name|op
operator|instanceof
name|MapJoinOperator
condition|)
block|{
comment|//found parent mapjoin operator.  Its size should already reflect any other mapjoins connected to it.
name|long
name|mjSize
init|=
name|ctx
operator|.
name|getMjOpSizes
argument_list|()
operator|.
name|get
argument_list|(
name|op
argument_list|)
decl_stmt|;
return|return
name|mjSize
return|;
block|}
name|long
name|result
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentOp
range|:
name|op
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
comment|//Else, recurse up the parents.
name|result
operator|+=
name|getConnectedParentMapJoinSize
argument_list|(
name|parentOp
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Examines this operator and all the children, for mapjoins that will be in the same work.    * @param op given operator    * @param ctx context to pass information.    * @return total size of child mapjoins in same work as this operator.    */
specifier|private
name|long
name|getConnectedChildMapJoinSize
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|,
name|OptimizeSparkProcContext
name|ctx
parameter_list|)
block|{
if|if
condition|(
operator|(
name|op
operator|instanceof
name|UnionOperator
operator|)
operator|||
operator|(
name|op
operator|instanceof
name|ReduceSinkOperator
operator|)
condition|)
block|{
comment|//Work Boundary, stop exploring.
return|return
literal|0
return|;
block|}
if|if
condition|(
name|op
operator|instanceof
name|MapJoinOperator
condition|)
block|{
comment|//found child mapjoin operator.  Its size should already reflect any mapjoins connected to it, so stop processing.
name|long
name|mjSize
init|=
name|ctx
operator|.
name|getMjOpSizes
argument_list|()
operator|.
name|get
argument_list|(
name|op
argument_list|)
decl_stmt|;
return|return
name|mjSize
return|;
block|}
name|long
name|result
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|childOp
range|:
name|op
operator|.
name|getChildOperators
argument_list|()
control|)
block|{
comment|//Else, recurse to the children.
name|result
operator|+=
name|getConnectedChildMapJoinSize
argument_list|(
name|childOp
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/*    * Once we have decided on the map join, the tree would transform from    *    *        |                   |    *       Join               MapJoin    *       / \                /   \    *     RS   RS   --->     RS    TS (big table)    *    /      \           /    *   TS       TS        TS (small table)    *    * for spark.    */
specifier|public
name|MapJoinOperator
name|convertJoinMapJoin
parameter_list|(
name|JoinOperator
name|joinOp
parameter_list|,
name|OptimizeSparkProcContext
name|context
parameter_list|,
name|int
name|bigTablePosition
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// bail on mux operator because currently the mux operator masks the emit keys
comment|// of the constituent reduce sinks.
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentOp
range|:
name|joinOp
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
if|if
condition|(
name|parentOp
operator|instanceof
name|MuxOperator
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
comment|//can safely convert the join to a map join.
name|ParseContext
name|parseContext
init|=
name|context
operator|.
name|getParseContext
argument_list|()
decl_stmt|;
name|MapJoinOperator
name|mapJoinOp
init|=
name|MapJoinProcessor
operator|.
name|convertJoinOpMapJoinOp
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|parseContext
operator|.
name|getOpParseCtx
argument_list|()
argument_list|,
name|joinOp
argument_list|,
name|parseContext
operator|.
name|getJoinContext
argument_list|()
operator|.
name|get
argument_list|(
name|joinOp
argument_list|)
argument_list|,
name|bigTablePosition
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentBigTableOp
init|=
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|bigTablePosition
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentBigTableOp
operator|instanceof
name|ReduceSinkOperator
condition|)
block|{
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|remove
argument_list|(
name|bigTablePosition
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
operator|(
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|contains
argument_list|(
name|parentBigTableOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|)
condition|)
block|{
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|add
argument_list|(
name|bigTablePosition
argument_list|,
name|parentBigTableOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|parentBigTableOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|removeChild
argument_list|(
name|parentBigTableOp
argument_list|)
expr_stmt|;
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
name|mapJoinOp
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|contains
argument_list|(
name|mapJoinOp
argument_list|)
operator|)
condition|)
block|{
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|add
argument_list|(
name|mapJoinOp
argument_list|)
expr_stmt|;
block|}
name|op
operator|.
name|getChildOperators
argument_list|()
operator|.
name|remove
argument_list|(
name|joinOp
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|mapJoinOp
return|;
block|}
specifier|private
name|boolean
name|containUnionWithoutRS
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|op
parameter_list|)
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|op
operator|instanceof
name|UnionOperator
condition|)
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|pop
range|:
name|op
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|pop
operator|instanceof
name|ReduceSinkOperator
operator|)
condition|)
block|{
name|result
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|op
operator|instanceof
name|ReduceSinkOperator
condition|)
block|{
name|result
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|pop
range|:
name|op
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
if|if
condition|(
name|containUnionWithoutRS
argument_list|(
name|pop
argument_list|)
condition|)
block|{
name|result
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

