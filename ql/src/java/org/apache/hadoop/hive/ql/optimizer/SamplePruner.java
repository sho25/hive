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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|FileStatus
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
name|FileSystem
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
name|ql
operator|.
name|exec
operator|.
name|FilterOperator
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
name|FilterDesc
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
name|FilterDesc
operator|.
name|SampleDesc
import|;
end_import

begin_comment
comment|/**  * The transformation step that does sample pruning.  *  */
end_comment

begin_class
specifier|public
class|class
name|SamplePruner
extends|extends
name|Transform
block|{
comment|/**    * SamplePrunerCtx.    *    */
specifier|public
specifier|static
class|class
name|SamplePrunerCtx
implements|implements
name|NodeProcessorCtx
block|{
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|SampleDesc
argument_list|>
name|opToSamplePruner
decl_stmt|;
specifier|public
name|SamplePrunerCtx
parameter_list|(
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|SampleDesc
argument_list|>
name|opToSamplePruner
parameter_list|)
block|{
name|this
operator|.
name|opToSamplePruner
operator|=
name|opToSamplePruner
expr_stmt|;
block|}
comment|/**      * @return the opToSamplePruner      */
specifier|public
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|SampleDesc
argument_list|>
name|getOpToSamplePruner
parameter_list|()
block|{
return|return
name|opToSamplePruner
return|;
block|}
comment|/**      * @param opToSamplePruner      *          the opToSamplePruner to set      */
specifier|public
name|void
name|setOpToSamplePruner
parameter_list|(
name|HashMap
argument_list|<
name|TableScanOperator
argument_list|,
name|SampleDesc
argument_list|>
name|opToSamplePruner
parameter_list|)
block|{
name|this
operator|.
name|opToSamplePruner
operator|=
name|opToSamplePruner
expr_stmt|;
block|}
block|}
comment|// The log
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
literal|"hive.ql.optimizer.SamplePruner"
argument_list|)
decl_stmt|;
comment|/*    * (non-Javadoc)    *    * @see    * org.apache.hadoop.hive.ql.optimizer.Transform#transform(org.apache.hadoop    * .hive.ql.parse.ParseContext)    */
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
name|SamplePrunerCtx
name|samplePrunerCtx
init|=
operator|new
name|SamplePrunerCtx
argument_list|(
name|pctx
operator|.
name|getOpToSamplePruner
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|opRules
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
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"R1"
argument_list|,
literal|"("
operator|+
name|TableScanOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
operator|+
name|FilterOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
operator|+
name|FilterOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%|"
operator|+
name|TableScanOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
operator|+
name|FilterOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%)"
argument_list|)
argument_list|,
name|getFilterProc
argument_list|()
argument_list|)
expr_stmt|;
comment|// The dispatcher fires the processor corresponding to the closest matching
comment|// rule and passes the context along
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
name|getDefaultProc
argument_list|()
argument_list|,
name|opRules
argument_list|,
name|samplePrunerCtx
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
comment|// Create a list of topop nodes
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
name|getTopOps
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
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
comment|/**    * FilterPPR filter processor.    *    */
specifier|public
specifier|static
class|class
name|FilterPPR
implements|implements
name|NodeProcessor
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
name|FilterOperator
name|filOp
init|=
operator|(
name|FilterOperator
operator|)
name|nd
decl_stmt|;
name|FilterDesc
name|filOpDesc
init|=
name|filOp
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|SampleDesc
name|sampleDescr
init|=
name|filOpDesc
operator|.
name|getSampleDescr
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|sampleDescr
operator|==
literal|null
operator|)
operator|||
operator|!
name|sampleDescr
operator|.
name|getInputPruning
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
assert|assert
operator|(
name|stack
operator|.
name|size
argument_list|()
operator|==
literal|3
operator|&&
name|stack
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|instanceof
name|FilterOperator
operator|)
operator|||
name|stack
operator|.
name|size
argument_list|()
operator|==
literal|2
assert|;
name|TableScanOperator
name|tsOp
init|=
operator|(
name|TableScanOperator
operator|)
name|stack
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
operator|(
operator|(
name|SamplePrunerCtx
operator|)
name|procCtx
operator|)
operator|.
name|getOpToSamplePruner
argument_list|()
operator|.
name|put
argument_list|(
name|tsOp
argument_list|,
name|sampleDescr
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getFilterProc
parameter_list|()
block|{
return|return
operator|new
name|FilterPPR
argument_list|()
return|;
block|}
comment|/**    * DefaultPPR default processor which does nothing.    *    */
specifier|public
specifier|static
class|class
name|DefaultPPR
implements|implements
name|NodeProcessor
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
comment|// Nothing needs to be done.
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getDefaultProc
parameter_list|()
block|{
return|return
operator|new
name|DefaultPPR
argument_list|()
return|;
block|}
comment|/**    * Prunes to get all the files in the partition that satisfy the TABLESAMPLE    * clause.    *    * @param part    *          The partition to prune    * @return Path[]    * @throws SemanticException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"nls"
argument_list|)
specifier|public
specifier|static
name|Path
index|[]
name|prune
parameter_list|(
name|Partition
name|part
parameter_list|,
name|SampleDesc
name|sampleDescr
parameter_list|)
throws|throws
name|SemanticException
block|{
name|int
name|num
init|=
name|sampleDescr
operator|.
name|getNumerator
argument_list|()
decl_stmt|;
name|int
name|den
init|=
name|sampleDescr
operator|.
name|getDenominator
argument_list|()
decl_stmt|;
name|int
name|bucketCount
init|=
name|part
operator|.
name|getBucketCount
argument_list|()
decl_stmt|;
name|String
name|fullScanMsg
init|=
literal|""
decl_stmt|;
comment|// check if input pruning is possible
comment|// TODO: this code is buggy - it relies on having one file per bucket; no MM support (by design).
name|boolean
name|isMmTable
init|=
name|AcidUtils
operator|.
name|isInsertOnlyTable
argument_list|(
name|part
operator|.
name|getTable
argument_list|()
operator|.
name|getParameters
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|sampleDescr
operator|.
name|getInputPruning
argument_list|()
operator|&&
operator|!
name|isMmTable
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"numerator = "
operator|+
name|num
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"denominator = "
operator|+
name|den
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"bucket count = "
operator|+
name|bucketCount
argument_list|)
expr_stmt|;
if|if
condition|(
name|bucketCount
operator|==
name|den
condition|)
block|{
name|Path
index|[]
name|ret
init|=
operator|new
name|Path
index|[
literal|1
index|]
decl_stmt|;
name|ret
index|[
literal|0
index|]
operator|=
name|part
operator|.
name|getBucketPath
argument_list|(
name|num
operator|-
literal|1
argument_list|)
expr_stmt|;
return|return
operator|(
name|ret
operator|)
return|;
block|}
elseif|else
if|if
condition|(
name|bucketCount
operator|>
name|den
operator|&&
name|bucketCount
operator|%
name|den
operator|==
literal|0
condition|)
block|{
name|int
name|numPathsInSample
init|=
name|bucketCount
operator|/
name|den
decl_stmt|;
name|Path
index|[]
name|ret
init|=
operator|new
name|Path
index|[
name|numPathsInSample
index|]
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
name|numPathsInSample
condition|;
name|i
operator|++
control|)
block|{
name|ret
index|[
name|i
index|]
operator|=
name|part
operator|.
name|getBucketPath
argument_list|(
name|i
operator|*
name|den
operator|+
name|num
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
elseif|else
if|if
condition|(
name|bucketCount
operator|<
name|den
operator|&&
name|den
operator|%
name|bucketCount
operator|==
literal|0
condition|)
block|{
name|Path
index|[]
name|ret
init|=
operator|new
name|Path
index|[
literal|1
index|]
decl_stmt|;
name|ret
index|[
literal|0
index|]
operator|=
name|part
operator|.
name|getBucketPath
argument_list|(
operator|(
name|num
operator|-
literal|1
operator|)
operator|%
name|bucketCount
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
else|else
block|{
comment|// need to do full scan
name|fullScanMsg
operator|=
literal|"Tablesample denominator "
operator|+
name|den
operator|+
literal|" is not multiple/divisor of bucket count "
operator|+
name|bucketCount
operator|+
literal|" of table "
operator|+
name|part
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// need to do full scan
name|fullScanMsg
operator|=
name|isMmTable
condition|?
literal|"MM table"
else|:
literal|"Tablesample not on clustered columns"
expr_stmt|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
name|fullScanMsg
operator|+
literal|", using full table scan"
argument_list|)
expr_stmt|;
name|Path
index|[]
name|ret
init|=
name|part
operator|.
name|getPath
argument_list|()
decl_stmt|;
return|return
name|ret
return|;
block|}
comment|/**    * Class used for return value of addPath()    *    */
specifier|public
specifier|static
class|class
name|AddPathReturnStatus
block|{
specifier|public
name|AddPathReturnStatus
parameter_list|(
name|boolean
name|hasFile
parameter_list|,
name|boolean
name|allFile
parameter_list|,
name|long
name|sizeLeft
parameter_list|)
block|{
name|this
operator|.
name|hasFile
operator|=
name|hasFile
expr_stmt|;
name|this
operator|.
name|allFile
operator|=
name|allFile
expr_stmt|;
name|this
operator|.
name|sizeLeft
operator|=
name|sizeLeft
expr_stmt|;
block|}
comment|// whether the sub-directory has any file
specifier|public
name|boolean
name|hasFile
decl_stmt|;
comment|// whether all files are not sufficient to reach sizeLeft
specifier|public
name|boolean
name|allFile
decl_stmt|;
comment|// remaining size needed after putting files in the return path list
specifier|public
name|long
name|sizeLeft
decl_stmt|;
block|}
comment|/**    * Try to recursively add files in sub-directories into retPathList until    * reaching the sizeLeft.    * @param fs    * @param pathPattern    * @param sizeLeft    * @param fileLimit    * @param retPathList    * @return status of the recursive call    * @throws IOException    */
specifier|public
specifier|static
name|AddPathReturnStatus
name|addPath
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|String
name|pathPattern
parameter_list|,
name|long
name|sizeLeft
parameter_list|,
name|int
name|fileLimit
parameter_list|,
name|Collection
argument_list|<
name|Path
argument_list|>
name|retPathList
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Path pattern = "
operator|+
name|pathPattern
argument_list|)
expr_stmt|;
name|FileStatus
name|srcs
index|[]
init|=
name|fs
operator|.
name|globStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|pathPattern
argument_list|)
argument_list|)
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|srcs
argument_list|)
expr_stmt|;
name|boolean
name|hasFile
init|=
literal|false
decl_stmt|,
name|allFile
init|=
literal|true
decl_stmt|;
for|for
control|(
name|FileStatus
name|src
range|:
name|srcs
control|)
block|{
if|if
condition|(
name|sizeLeft
operator|<=
literal|0
condition|)
block|{
name|allFile
operator|=
literal|false
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|src
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got directory: "
operator|+
name|src
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|AddPathReturnStatus
name|ret
init|=
name|addPath
argument_list|(
name|fs
argument_list|,
name|src
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|"/*"
argument_list|,
name|sizeLeft
argument_list|,
name|fileLimit
argument_list|,
name|retPathList
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|==
literal|null
condition|)
block|{
comment|// not qualify this optimization
return|return
literal|null
return|;
block|}
name|sizeLeft
operator|=
name|ret
operator|.
name|sizeLeft
expr_stmt|;
name|hasFile
operator||=
name|ret
operator|.
name|hasFile
expr_stmt|;
name|allFile
operator|&=
name|ret
operator|.
name|allFile
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got file: "
operator|+
name|src
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|hasFile
operator|=
literal|true
expr_stmt|;
name|retPathList
operator|.
name|add
argument_list|(
name|src
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|sizeLeft
operator|-=
name|src
operator|.
name|getLen
argument_list|()
expr_stmt|;
if|if
condition|(
name|retPathList
operator|.
name|size
argument_list|()
operator|>=
name|fileLimit
operator|&&
name|sizeLeft
operator|>
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
return|return
operator|new
name|AddPathReturnStatus
argument_list|(
name|hasFile
argument_list|,
name|allFile
argument_list|,
name|sizeLeft
argument_list|)
return|;
block|}
specifier|public
enum|enum
name|LimitPruneRetStatus
block|{
comment|// no files in the partition
name|NoFile
block|,
comment|// sum size of all files in the partition is smaller than size required
name|NeedAllFiles
block|,
comment|// a susbset of files for the partition are sufficient for the optimization
name|NeedSomeFiles
block|,
comment|// the partition doesn't qualify the global limit optimization for some reason
name|NotQualify
block|}
comment|/**    * Try to generate a list of subset of files in the partition to reach a size    * limit with number of files less than fileLimit    * @param part    * @param sizeLimit    * @param fileLimit    * @param retPathList list of Paths returned    * @return the result of the attempt    * @throws SemanticException    */
specifier|public
specifier|static
name|LimitPruneRetStatus
name|limitPrune
parameter_list|(
name|Partition
name|part
parameter_list|,
name|long
name|sizeLimit
parameter_list|,
name|int
name|fileLimit
parameter_list|,
name|Collection
argument_list|<
name|Path
argument_list|>
name|retPathList
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
name|FileSystem
name|fs
init|=
name|part
operator|.
name|getDataLocation
argument_list|()
operator|.
name|getFileSystem
argument_list|(
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|pathPattern
init|=
name|part
operator|.
name|getDataLocation
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|"/*"
decl_stmt|;
name|AddPathReturnStatus
name|ret
init|=
name|addPath
argument_list|(
name|fs
argument_list|,
name|pathPattern
argument_list|,
name|sizeLimit
argument_list|,
name|fileLimit
argument_list|,
name|retPathList
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|==
literal|null
condition|)
block|{
return|return
name|LimitPruneRetStatus
operator|.
name|NotQualify
return|;
block|}
elseif|else
if|if
condition|(
operator|!
name|ret
operator|.
name|hasFile
condition|)
block|{
return|return
name|LimitPruneRetStatus
operator|.
name|NoFile
return|;
block|}
elseif|else
if|if
condition|(
name|ret
operator|.
name|sizeLeft
operator|>
literal|0
condition|)
block|{
return|return
name|LimitPruneRetStatus
operator|.
name|NotQualify
return|;
block|}
elseif|else
if|if
condition|(
name|ret
operator|.
name|allFile
condition|)
block|{
return|return
name|LimitPruneRetStatus
operator|.
name|NeedAllFiles
return|;
block|}
else|else
block|{
return|return
name|LimitPruneRetStatus
operator|.
name|NeedSomeFiles
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot get path"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

