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
name|listbucketingpruner
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
name|common
operator|.
name|FileUtils
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
name|PrunerUtils
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
name|Transform
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
name|PrunedPartitionList
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

begin_comment
comment|/**  * The transformation step that does list bucketing pruning.  *  */
end_comment

begin_class
specifier|public
class|class
name|ListBucketingPruner
implements|implements
name|Transform
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ListBucketingPruner
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/*    * (non-Javadoc)    *    * @see org.apache.hadoop.hive.ql.optimizer.Transform#transform(org.apache.hadoop.hive.ql.parse.    * ParseContext)    */
annotation|@
name|Override
specifier|public
name|ParseContext
name|transform
parameter_list|(
name|ParseContext
name|pctx
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// create a the context for walking operators
name|NodeProcessorCtx
name|opPartWalkerCtx
init|=
operator|new
name|LBOpPartitionWalkerCtx
argument_list|(
name|pctx
argument_list|)
decl_stmt|;
comment|// Retrieve all partitions generated from partition pruner and partition column pruner
name|PrunerUtils
operator|.
name|walkOperatorTree
argument_list|(
name|pctx
argument_list|,
name|opPartWalkerCtx
argument_list|,
name|LBPartitionProcFactory
operator|.
name|getFilterProc
argument_list|()
argument_list|,
name|LBPartitionProcFactory
operator|.
name|getDefaultProc
argument_list|()
argument_list|)
expr_stmt|;
name|PrunedPartitionList
name|partsList
init|=
operator|(
operator|(
name|LBOpPartitionWalkerCtx
operator|)
name|opPartWalkerCtx
operator|)
operator|.
name|getPartitions
argument_list|()
decl_stmt|;
if|if
condition|(
name|partsList
operator|!=
literal|null
condition|)
block|{
name|Set
argument_list|<
name|Partition
argument_list|>
name|parts
init|=
literal|null
decl_stmt|;
name|parts
operator|=
name|partsList
operator|.
name|getConfirmedPartns
argument_list|()
expr_stmt|;
name|parts
operator|.
name|addAll
argument_list|(
name|partsList
operator|.
name|getUnknownPartns
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|parts
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|parts
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|)
condition|)
block|{
for|for
control|(
name|Partition
name|part
range|:
name|parts
control|)
block|{
comment|// only process partition which is skewed and list bucketed
if|if
condition|(
name|ListBucketingPrunerUtils
operator|.
name|isListBucketingPart
argument_list|(
name|part
argument_list|)
condition|)
block|{
comment|// create a the context for walking operators
name|NodeProcessorCtx
name|opWalkerCtx
init|=
operator|new
name|LBOpWalkerCtx
argument_list|(
name|pctx
operator|.
name|getOpToPartToSkewedPruner
argument_list|()
argument_list|,
name|part
argument_list|)
decl_stmt|;
comment|// walk operator tree to create expression tree for list bucketing
name|PrunerUtils
operator|.
name|walkOperatorTree
argument_list|(
name|pctx
argument_list|,
name|opWalkerCtx
argument_list|,
name|LBProcFactory
operator|.
name|getFilterProc
argument_list|()
argument_list|,
name|LBProcFactory
operator|.
name|getDefaultProc
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|pctx
return|;
block|}
comment|/**    * Prunes to the directories which match the skewed keys in where clause.    *    *    * Algorithm    *    * =========    *    * For each possible skewed element combination:    * 1. walk through ExprNode tree    * 2. decide Boolean (True/False/unknown(null))    *    * Go through each skewed element combination again:    * 1. if it is skewed value, skip the directory only if it is false, otherwise keep it    * 2. skip the default directory only if all skewed elements,non-skewed value, are false.    *    * Example    * =======    * For example:    * 1. skewed column (list): C1, C2    * 2. skewed value (list of list): (1,a), (2,b), (1,c)    *    * Unique skewed elements for each skewed column (list of list):    * (1,2,other), (a,b,c,other)    *    * Index: (0,1,2) (0,1,2,3)    * Output matches order of skewed column. Output can be read as:    *    * C1 has unique element list (1,2,other)    * C2 has unique element list (a,b,c,other)    *    * C1\C2 | a | b | c |Other    * 1 | (1,a) | X | (1,c) |X    * 2 | X |(2,b) | X |X    * other | X | X | X |X    *    * Complete dynamic-multi-dimension collection    *    * (0,0) (1,a) * -> T    * (0,1) (1,b) -> T    * (0,2) (1,c) *-> F    * (0,3) (1,other)-> F    * (1,0) (2,a)-> F    * (1,1) (2,b) * -> T    * (1,2) (2,c)-> F    * (1,3) (2,other)-> F    * (2,0) (other,a) -> T    * (2,1) (other,b) -> T    * (2,2) (other,c) -> T    * (2,3) (other,other) -> T    * * is skewed value entry    *    * Expression Tree : ((c1=1) and (c2=a)) or ( (c1=3) or (c2=b))    *    * or    * / \    * and or    * / \ / \    * c1=1 c2=a c1=3 c2=b    *    *    * For each entry in dynamic-multi-dimension container    *    * 1. walk through the tree to decide value (please see map's value above)    * 2. if it is skewed value    * 2.1 remove the entry from the map    * 2.2 add directory to path unless value is false    * 3. otherwise, add value to map    *    * Once it is done, go through the rest entries in map to decide default directory    * 1. we know all is not skewed value    * 2. we skip default directory only if all value is false    *    * What we choose at the end?    *    * 1. directory for (1,a) because it 's skewed value and match returns true    * 2. directory for (2,b) because it 's skewed value and match returns true    * 3. default directory because not all non-skewed value returns false    *    * we skip directory for (1,c) since match returns false    *    * Note: unknown is marked in {@link #transform(ParseContext)}<blockquote>    *<pre>    * newcd = new ExprNodeConstantDesc(cd.getTypeInfo(), null)    *</pre>    *    *</blockquote> can be checked via<blockquote>    *    *<pre>    *     child_nd instanceof ExprNodeConstantDesc    *&& ((ExprNodeConstantDesc) child_nd).getValue() == null)    *</pre>    *    *</blockquote>    *    * @param ctx    *          parse context    * @param part    *          partition    * @param pruner    *          expression node tree    * @return    */
specifier|public
specifier|static
name|Path
index|[]
name|prune
parameter_list|(
name|ParseContext
name|ctx
parameter_list|,
name|Partition
name|part
parameter_list|,
name|ExprNodeDesc
name|pruner
parameter_list|)
block|{
name|Path
index|[]
name|finalPaths
init|=
literal|null
decl_stmt|;
try|try
block|{
name|finalPaths
operator|=
name|execute
argument_list|(
name|ctx
argument_list|,
name|part
argument_list|,
name|pruner
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SemanticException
name|e
parameter_list|)
block|{
comment|// Use full partition path for error case.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Using full partition scan :"
operator|+
name|part
operator|.
name|getPath
argument_list|()
operator|+
literal|"."
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|finalPaths
operator|=
name|part
operator|.
name|getPath
argument_list|()
expr_stmt|;
block|}
return|return
name|finalPaths
return|;
block|}
comment|/**    * Main skeleton for list bucketing pruning.    *    * @param ctx    * @param part    * @param pruner    * @return    * @throws SemanticException    */
specifier|private
specifier|static
name|Path
index|[]
name|execute
parameter_list|(
name|ParseContext
name|ctx
parameter_list|,
name|Partition
name|part
parameter_list|,
name|ExprNodeDesc
name|pruner
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Path
index|[]
name|finalPaths
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|selectedPaths
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|ListBucketingPrunerUtils
operator|.
name|isUnknownState
argument_list|(
name|pruner
argument_list|)
condition|)
block|{
comment|// Use full partition path for error case.
name|LOG
operator|.
name|warn
argument_list|(
literal|"List bucketing pruner is either null or in unknown state "
operator|+
literal|" so that it uses full partition scan :"
operator|+
name|part
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|finalPaths
operator|=
name|part
operator|.
name|getPath
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// Retrieve skewed columns.
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|sVals
init|=
name|part
operator|.
name|getSkewedColValues
argument_list|()
decl_stmt|;
assert|assert
operator|(
operator|(
name|sVals
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|sVals
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|)
operator|)
operator|:
name|part
operator|.
name|getName
argument_list|()
operator|+
literal|" skewed metadata is corrupted. No skewed value information."
assert|;
comment|// Calculate collection.
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|indexCollection
init|=
name|DynamicMultiDimensionalCollection
operator|.
name|generateCollection
argument_list|(
name|sVals
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|indexCollection
operator|!=
literal|null
operator|)
operator|:
literal|"Collection is null."
assert|;
comment|// Calculate unique skewed elements for each skewed column.
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|uniqSkewValues
init|=
name|DynamicMultiDimensionalCollection
operator|.
name|uniqueSkewedValueList
argument_list|(
name|sVals
argument_list|)
decl_stmt|;
comment|// Decide skewed value directory selection.
name|List
argument_list|<
name|Boolean
argument_list|>
name|nonSkewedValueMatchResult
init|=
name|decideSkewedValueDirSelection
argument_list|(
name|part
argument_list|,
name|pruner
argument_list|,
name|selectedPaths
argument_list|,
name|indexCollection
argument_list|,
name|uniqSkewValues
argument_list|)
decl_stmt|;
comment|// Decide default directory selection.
name|decideDefaultDirSelection
argument_list|(
name|part
argument_list|,
name|selectedPaths
argument_list|,
name|nonSkewedValueMatchResult
argument_list|)
expr_stmt|;
comment|// Finalize paths.
name|finalPaths
operator|=
name|generateFinalPath
argument_list|(
name|part
argument_list|,
name|selectedPaths
argument_list|)
expr_stmt|;
block|}
return|return
name|finalPaths
return|;
block|}
comment|/**    * Walk through every entry in complete collection    * 1. calculate if it matches expression tree    * 2. decide if select skewed value directory    * 3. store match result for non-skewed value for later handle on default directory    * C1\C2 | a | b | c |Other    * 1 | (1,a) | X | (1,c) |X    * 2 | X |(2,b) | X |X    * other | X | X | X |X    * Final result    * Complete dynamic-multi-dimension collection    * (0,0) (1,a) * -> T    * (0,1) (1,b) -> T    * (0,2) (1,c) *-> F    * (0,3) (1,other)-> F    * (1,0) (2,a)-> F    * (1,1) (2,b) * -> T    * (1,2) (2,c)-> F    * (1,3) (2,other)-> F    * (2,0) (other,a) -> T    * (2,1) (other,b) -> T    * (2,2) (other,c) -> T    * (2,3) (other,other) -> T    *    * * is skewed value entry    *    * 1. directory for (1,a) is chosen because it 's skewed value and match returns true    * 2. directory for (2,b) is chosen because it 's skewed value and match returns true    *    * @param part    * @param pruner    * @param selectedPaths    * @param collections    * @param uniqSkewedValues    * @return    * @throws SemanticException    */
specifier|private
specifier|static
name|List
argument_list|<
name|Boolean
argument_list|>
name|decideSkewedValueDirSelection
parameter_list|(
name|Partition
name|part
parameter_list|,
name|ExprNodeDesc
name|pruner
parameter_list|,
name|List
argument_list|<
name|Path
argument_list|>
name|selectedPaths
parameter_list|,
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|collections
parameter_list|,
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|uniqSkewedValues
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// For each entry in dynamic-multi-dimension collection.
name|List
argument_list|<
name|String
argument_list|>
name|skewedCols
init|=
name|part
operator|.
name|getSkewedColNames
argument_list|()
decl_stmt|;
comment|// Retrieve skewed column.
name|Map
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|,
name|String
argument_list|>
name|mappings
init|=
name|part
operator|.
name|getSkewedColValueLocationMaps
argument_list|()
decl_stmt|;
comment|// Retrieve skewed
comment|// map.
assert|assert
name|ListBucketingPrunerUtils
operator|.
name|isListBucketingPart
argument_list|(
name|part
argument_list|)
operator|:
name|part
operator|.
name|getName
argument_list|()
operator|+
literal|" skewed metadata is corrupted. No skewed column and/or location mappings information."
assert|;
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|skewedValues
init|=
name|part
operator|.
name|getSkewedColValues
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Boolean
argument_list|>
name|nonSkewedValueMatchResult
init|=
operator|new
name|ArrayList
argument_list|<
name|Boolean
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|String
argument_list|>
name|cell
range|:
name|collections
control|)
block|{
comment|// Walk through the tree to decide value.
comment|// Example: skewed column: C1, C2 ;
comment|// index: (1,a) ;
comment|// expression tree: ((c1=1) and (c2=a)) or ((c1=3) or (c2=b))
name|Boolean
name|matchResult
init|=
name|ListBucketingPrunerUtils
operator|.
name|evaluateExprOnCell
argument_list|(
name|skewedCols
argument_list|,
name|cell
argument_list|,
name|pruner
argument_list|,
name|uniqSkewedValues
argument_list|)
decl_stmt|;
comment|// Handle skewed value.
if|if
condition|(
name|skewedValues
operator|.
name|contains
argument_list|(
name|cell
argument_list|)
condition|)
block|{
comment|// if it is skewed value
if|if
condition|(
operator|(
name|matchResult
operator|==
literal|null
operator|)
operator|||
name|matchResult
condition|)
block|{
comment|// add directory to path unless value is false
comment|/* It's valid case if a partition: */
comment|/* 1. is defined with skewed columns and skewed values in metadata */
comment|/* 2. doesn't have all skewed values within its data */
if|if
condition|(
name|mappings
operator|.
name|get
argument_list|(
name|cell
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|selectedPaths
operator|.
name|add
argument_list|(
operator|new
name|Path
argument_list|(
name|mappings
operator|.
name|get
argument_list|(
name|cell
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// Non-skewed value, add it to list for later handle on default directory.
name|nonSkewedValueMatchResult
operator|.
name|add
argument_list|(
name|matchResult
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|nonSkewedValueMatchResult
return|;
block|}
comment|/**    * Decide whether should select the default directory.    *    * @param part    * @param selectedPaths    * @param nonSkewedValueMatchResult    */
specifier|private
specifier|static
name|void
name|decideDefaultDirSelection
parameter_list|(
name|Partition
name|part
parameter_list|,
name|List
argument_list|<
name|Path
argument_list|>
name|selectedPaths
parameter_list|,
name|List
argument_list|<
name|Boolean
argument_list|>
name|nonSkewedValueMatchResult
parameter_list|)
block|{
name|boolean
name|skipDefDir
init|=
literal|true
decl_stmt|;
for|for
control|(
name|Boolean
name|v
range|:
name|nonSkewedValueMatchResult
control|)
block|{
if|if
condition|(
operator|(
name|v
operator|==
literal|null
operator|)
operator|||
name|v
condition|)
block|{
name|skipDefDir
operator|=
literal|false
expr_stmt|;
comment|// we skip default directory only if all value is false
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|skipDefDir
condition|)
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|part
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
operator|(
name|FileUtils
operator|.
name|makeDefaultListBucketingDirName
argument_list|(
name|part
operator|.
name|getSkewedColNames
argument_list|()
argument_list|,
name|ListBucketingPrunerUtils
operator|.
name|HIVE_LIST_BUCKETING_DEFAULT_DIR_NAME
argument_list|)
operator|)
argument_list|)
expr_stmt|;
name|selectedPaths
operator|.
name|add
argument_list|(
operator|new
name|Path
argument_list|(
name|builder
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Decide the final path.    *    * @param part    * @param selectedPaths    * @return    */
specifier|private
specifier|static
name|Path
index|[]
name|generateFinalPath
parameter_list|(
name|Partition
name|part
parameter_list|,
name|List
argument_list|<
name|Path
argument_list|>
name|selectedPaths
parameter_list|)
block|{
name|Path
index|[]
name|finalPaths
decl_stmt|;
if|if
condition|(
name|selectedPaths
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Using full partition scan :"
operator|+
name|part
operator|.
name|getPath
argument_list|()
operator|+
literal|"."
argument_list|)
expr_stmt|;
name|finalPaths
operator|=
name|part
operator|.
name|getPath
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|finalPaths
operator|=
name|selectedPaths
operator|.
name|toArray
argument_list|(
operator|new
name|Path
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|finalPaths
return|;
block|}
comment|/**    * Note: this class is not designed to be used in general but for list bucketing pruner only.    * The structure addresses the following requirements:    * 1. multiple dimension collection    * 2. length of each dimension is dynamic. It's decided at runtime.    * The first user is list bucketing pruner and used in pruning phase:    * 1. Each skewed column has a batch of skewed elements.    * 2. One skewed column represents one dimension.    * 3. Length of dimension is size of skewed elements.    * 4. no. of skewed columns and length of dimension are dynamic and configured by user.    * use case:    * ========    * Use case #1:    * Multiple dimension collection represents if to select a directory representing by the cell.    * skewed column: C1, C2, C3    * skewed value: (1,a,x), (2,b,x), (1,c,x), (2,a,y)    * Other: represent value for the column which is not part of skewed value.    * C3 = x    * C1\C2 | a | b | c |Other    * 1 | Boolean(1,a,x) | X | Boolean(1,c,x) |X    * 2 | X |Boolean(2,b,x) | X |X    * other | X | X | X |X    * C3 = y    * C1\C2 | a | b | c |Other    * 1 | X | X | X |X    * 2 | Boolean(2,a,y) | X | X |X    * other | X | X | X |X    * Boolean is cell type which can be False/True/Null(Unknown).    * (1,a,x) is just for information purpose to explain which skewed value it represents.    * 1. value of Boolean(1,a,x) represents if we select the directory for list bucketing    * 2. value of Boolean(2,b,x) represents if we select the directory for list bucketing    * ...    * 3. All the rest, marked as "X", will decide if to pickup the default directory.    * 4. Not only "other" columns/rows but also the rest as long as it doesn't represent skewed    * value.    * For cell representing skewed value:    * 1. False, skip the directory    * 2. True/Unknown, select the directory    * For cells representing default directory:    * 1. only if all cells are false, skip the directory    * 2. all other cases, select the directory    * Use case #2:    * Multiple dimension collection represents skewed elements so that walk through tree one by one.    * Cell is a List<String> representing the value mapping from index path and skewed value.    * skewed column: C1, C2, C3    * skewed value: (1,a,x), (2,b,x), (1,c,x), (2,a,y)    * Other: represent value for the column which is not part of skewed value.    * C3 = x    * C1\C2 | a | b | c |Other    * 1 | (1,a,x) | X | (1,c,x) |X    * 2 | X |(2,b,x) | X |X    * other | X | X | X |X    * C3 = y    * C1\C2 | a | b | c |Other    * 1 | X | X | X |X    * 2 | (2,a,y) | X | X |X    * other | X | X | X |X    * Implementation:    * ==============    * please see another example in {@link ListBucketingPruner#prune}    * We will use a HasMap to represent the Dynamic-Multiple-Dimension collection:    * 1. Key is List<Integer> representing the index path to the cell    * 2. value represents the cell (Boolean for use case #1, List<String> for case #2)    * For example:    * 1. skewed column (list): C1, C2, C3    * 2. skewed value (list of list): (1,a,x), (2,b,x), (1,c,x), (2,a,y)    * From skewed value, we calculate the unique skewed element for each skewed column:    * C1: (1,2)    * C2: (a,b,c)    * C3: (x,y)    * We store them in list of list. We don't need to store skewed column name since we use order to    * match:    * 1. Skewed column (list): C1, C2, C3    * 2. Unique skewed elements for each skewed column (list of list):    * (1,2,other), (a,b,c,other), (x,y,other)    * 3. index (0,1,2) (0,1,2,3) (0,1,2)    *    * We use the index,starting at 0. to construct hashmap representing dynamic-multi-dimension    * collection:    * key (what skewed value key represents) -> value (Boolean for use case #1, List<String> for case    * #2).    * (0,0,0) (1,a,x)    * (0,0,1) (1,a,y)    * (0,1,0) (1,b,x)    * (0,1,1) (1,b,y)    * (0,2,0) (1,c,x)    * (0,2,1) (1,c,y)    * (1,0,0) (2,a,x)    * (1,0,1) (2,a,y)    * (1,1,0) (2,b,x)    * (1,1,1) (2,b,y)    * (1,2,0) (2,c,x)    * (1,2,1) (2,c,y)    * ...    */
specifier|public
specifier|static
class|class
name|DynamicMultiDimensionalCollection
block|{
comment|/**      * Find out complete skewed-element collection      * For example:      * 1. skewed column (list): C1, C2      * 2. skewed value (list of list): (1,a), (2,b), (1,c)      * It returns the complete collection      * (1,a) , (1,b) , (1,c) , (1,other), (2,a), (2,b) , (2,c), (2,other), (other,a), (other,b),      * (other,c), (other,other)      * @throws SemanticException      */
specifier|public
specifier|static
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|generateCollection
parameter_list|(
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|values
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// Calculate unique skewed elements for each skewed column.
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|uniqSkewedElements
init|=
name|DynamicMultiDimensionalCollection
operator|.
name|uniqueElementsList
argument_list|(
name|values
argument_list|,
name|ListBucketingPrunerUtils
operator|.
name|HIVE_LIST_BUCKETING_DEFAULT_KEY
argument_list|)
decl_stmt|;
comment|// Calculate complete dynamic-multi-dimension collection.
return|return
name|DynamicMultiDimensionalCollection
operator|.
name|flat
argument_list|(
name|uniqSkewedElements
argument_list|)
return|;
block|}
comment|/**      * Convert value to unique element list. This is specific for skew value use case:      * For example:      * 1. skewed column (list): C1, C2, C3      * 2. skewed value (list of list): (1,a,x), (2,b,x), (1,c,x), (2,a,y)      * Input: skewed value (list of list): (1,a,x), (2,b,x), (1,c,x), (2,a,y)      * Output: Unique skewed elements for each skewed column (list of list):      * (1,2,other), (a,b,c,other), (x,y,other)      * Output matches order of skewed column. Output can be read as:      * C1 has unique element list (1,2,other)      * C2 has unique element list (a,b,c,other)      * C3 has unique element list (x,y,other)      * Other represents any value which is not part skewed-value combination.      * @param values      *          skewed value list      * @return a list of unique element lists      */
specifier|public
specifier|static
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|uniqueElementsList
parameter_list|(
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|values
parameter_list|,
name|String
name|defaultDirName
parameter_list|)
block|{
comment|// Get unique skewed value list.
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|result
init|=
name|uniqueSkewedValueList
argument_list|(
name|values
argument_list|)
decl_stmt|;
comment|// Add default dir at the end of each list
for|for
control|(
name|List
argument_list|<
name|String
argument_list|>
name|list
range|:
name|result
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|defaultDirName
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**      * Convert value to unique skewed value list. It is used in      * {@link ListBucketingPrunerUtils#evaluateExprOnCell}      *      * For example:      *      * 1. skewed column (list): C1, C2, C3      * 2. skewed value (list of list): (1,a,x), (2,b,x), (1,c,x), (2,a,y)      *      * Input: skewed value (list of list): (1,a,x), (2,b,x), (1,c,x), (2,a,y)      * Output: Unique skewed value for each skewed column (list of list):      * (1,2), (a,b,c), (x,y)      *      * Output matches order of skewed column. Output can be read as:      * C1 has unique skewed value list (1,2,)      * C2 has unique skewed value list (a,b,c)      * C3 has unique skewed value list (x,y)      *      * @param values      *          skewed value list      * @return a list of unique skewed value lists      */
specifier|public
specifier|static
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|uniqueSkewedValueList
parameter_list|(
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|values
parameter_list|)
block|{
if|if
condition|(
operator|(
name|values
operator|==
literal|null
operator|)
operator|||
operator|(
name|values
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|)
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// skewed value has the same length.
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
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
name|values
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// add unique element to list per occurrence order in skewed value.
comment|// occurrence order in skewed value doesn't matter.
comment|// as long as we add them to a list, order is preserved from now on.
for|for
control|(
name|List
argument_list|<
name|String
argument_list|>
name|value
range|:
name|values
control|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|value
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|result
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|contains
argument_list|(
name|value
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
name|result
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|add
argument_list|(
name|value
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
comment|/**      * Flat a dynamic-multi-dimension collection.      *      * For example:      * 1. skewed column (list): C1, C2, C3      * 2. skewed value (list of list): (1,a,x), (2,b,x), (1,c,x), (2,a,y)      *      * Unique skewed elements for each skewed column (list of list):      * (1,2,other), (a,b,c,other)      * Index: (0,1,2) (0,1,2,3)      *      * Complete dynamic-multi-dimension collection      * (0,0) (1,a) * -> T      * (0,1) (1,b) -> T      * (0,2) (1,c) *-> F      * (0,3) (1,other)-> F      * (1,0) (2,a)-> F      * (1,1) (2,b) * -> T      * (1,2) (2,c)-> F      * (1,3) (2,other)-> F      * (2,0) (other,a) -> T      * (2,1) (other,b) -> T      * (2,2) (other,c) -> T      * (2,3) (other,other) -> T      * * is skewed value entry      *      * @param uniqSkewedElements      *      * @return      */
specifier|public
specifier|static
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|flat
parameter_list|(
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|uniqSkewedElements
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|uniqSkewedElements
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|collection
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|walker
argument_list|(
name|collection
argument_list|,
name|uniqSkewedElements
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
return|return
name|collection
return|;
block|}
comment|/**      * Flat the collection recursively.      *      * @param finalResult      * @param input      * @param listSoFar      * @param level      * @throws SemanticException      */
specifier|private
specifier|static
name|void
name|walker
parameter_list|(
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|finalResult
parameter_list|,
specifier|final
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|input
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|listSoFar
parameter_list|,
specifier|final
name|int
name|level
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// Base case.
if|if
condition|(
name|level
operator|==
operator|(
name|input
operator|.
name|size
argument_list|()
operator|-
literal|1
operator|)
condition|)
block|{
assert|assert
operator|(
name|input
operator|.
name|get
argument_list|(
name|level
argument_list|)
operator|!=
literal|null
operator|)
operator|:
literal|"Unique skewed element list has null list in "
operator|+
name|level
operator|+
literal|"th position."
assert|;
for|for
control|(
name|String
name|v
range|:
name|input
operator|.
name|get
argument_list|(
name|level
argument_list|)
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|oneCompleteIndex
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|listSoFar
argument_list|)
decl_stmt|;
name|oneCompleteIndex
operator|.
name|add
argument_list|(
name|v
argument_list|)
expr_stmt|;
name|finalResult
operator|.
name|add
argument_list|(
name|oneCompleteIndex
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
comment|// Recursive.
for|for
control|(
name|String
name|v
range|:
name|input
operator|.
name|get
argument_list|(
name|level
argument_list|)
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|clonedListSoFar
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|listSoFar
argument_list|)
decl_stmt|;
name|clonedListSoFar
operator|.
name|add
argument_list|(
name|v
argument_list|)
expr_stmt|;
name|int
name|nextLevel
init|=
name|level
operator|+
literal|1
decl_stmt|;
name|walker
argument_list|(
name|finalResult
argument_list|,
name|input
argument_list|,
name|clonedListSoFar
argument_list|,
name|nextLevel
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

