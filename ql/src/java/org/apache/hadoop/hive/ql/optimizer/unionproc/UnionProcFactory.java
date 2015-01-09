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
name|unionproc
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
name|HashMap
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
name|OperatorFactory
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
name|RowSchema
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
name|unionproc
operator|.
name|UnionProcContext
operator|.
name|UnionParseContext
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
name|FileSinkDesc
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

begin_comment
comment|/**  * Operator factory for union processing.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|UnionProcFactory
block|{
specifier|private
name|UnionProcFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
specifier|public
specifier|static
name|int
name|getPositionParent
parameter_list|(
name|UnionOperator
name|union
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|)
block|{
name|int
name|pos
init|=
literal|0
decl_stmt|;
name|int
name|size
init|=
name|stack
operator|.
name|size
argument_list|()
decl_stmt|;
assert|assert
name|size
operator|>=
literal|2
operator|&&
name|stack
operator|.
name|get
argument_list|(
name|size
operator|-
literal|1
argument_list|)
operator|==
name|union
assert|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parent
init|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|)
name|stack
operator|.
name|get
argument_list|(
name|size
operator|-
literal|2
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|parUnion
init|=
name|union
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
name|pos
operator|=
name|parUnion
operator|.
name|indexOf
argument_list|(
name|parent
argument_list|)
expr_stmt|;
assert|assert
name|pos
operator|<
name|parUnion
operator|.
name|size
argument_list|()
assert|;
return|return
name|pos
return|;
block|}
comment|/**    * MapRed subquery followed by Union.    */
specifier|public
specifier|static
class|class
name|MapRedUnion
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
name|UnionOperator
name|union
init|=
operator|(
name|UnionOperator
operator|)
name|nd
decl_stmt|;
name|UnionProcContext
name|ctx
init|=
operator|(
name|UnionProcContext
operator|)
name|procCtx
decl_stmt|;
comment|// find the branch on which this processor was invoked
name|int
name|pos
init|=
name|getPositionParent
argument_list|(
name|union
argument_list|,
name|stack
argument_list|)
decl_stmt|;
name|UnionParseContext
name|uCtx
init|=
name|ctx
operator|.
name|getUnionParseContext
argument_list|(
name|union
argument_list|)
decl_stmt|;
if|if
condition|(
name|uCtx
operator|==
literal|null
condition|)
block|{
name|uCtx
operator|=
operator|new
name|UnionParseContext
argument_list|(
name|union
operator|.
name|getConf
argument_list|()
operator|.
name|getNumInputs
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|setMapOnlySubq
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|uCtx
operator|.
name|setMapOnlySubq
argument_list|(
name|pos
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|uCtx
operator|.
name|setRootTask
argument_list|(
name|pos
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setUnionParseContext
argument_list|(
name|union
argument_list|,
name|uCtx
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Map-only subquery followed by Union.    */
specifier|public
specifier|static
class|class
name|MapUnion
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
name|UnionOperator
name|union
init|=
operator|(
name|UnionOperator
operator|)
name|nd
decl_stmt|;
name|UnionProcContext
name|ctx
init|=
operator|(
name|UnionProcContext
operator|)
name|procCtx
decl_stmt|;
comment|// find the branch on which this processor was invoked
name|int
name|pos
init|=
name|getPositionParent
argument_list|(
name|union
argument_list|,
name|stack
argument_list|)
decl_stmt|;
name|UnionParseContext
name|uCtx
init|=
name|ctx
operator|.
name|getUnionParseContext
argument_list|(
name|union
argument_list|)
decl_stmt|;
if|if
condition|(
name|uCtx
operator|==
literal|null
condition|)
block|{
name|uCtx
operator|=
operator|new
name|UnionParseContext
argument_list|(
name|union
operator|.
name|getConf
argument_list|()
operator|.
name|getNumInputs
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|uCtx
operator|.
name|setMapOnlySubq
argument_list|(
name|pos
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|uCtx
operator|.
name|setRootTask
argument_list|(
name|pos
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setUnionParseContext
argument_list|(
name|union
argument_list|,
name|uCtx
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Union subquery followed by Union.    */
specifier|public
specifier|static
class|class
name|UnknownUnion
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
name|UnionOperator
name|union
init|=
operator|(
name|UnionOperator
operator|)
name|nd
decl_stmt|;
name|UnionProcContext
name|ctx
init|=
operator|(
name|UnionProcContext
operator|)
name|procCtx
decl_stmt|;
comment|// find the branch on which this processor was invoked
name|int
name|pos
init|=
name|getPositionParent
argument_list|(
name|union
argument_list|,
name|stack
argument_list|)
decl_stmt|;
name|UnionParseContext
name|uCtx
init|=
name|ctx
operator|.
name|getUnionParseContext
argument_list|(
name|union
argument_list|)
decl_stmt|;
if|if
condition|(
name|uCtx
operator|==
literal|null
condition|)
block|{
name|uCtx
operator|=
operator|new
name|UnionParseContext
argument_list|(
name|union
operator|.
name|getConf
argument_list|()
operator|.
name|getNumInputs
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|int
name|start
init|=
name|stack
operator|.
name|size
argument_list|()
operator|-
literal|2
decl_stmt|;
name|UnionOperator
name|parentUnionOperator
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|start
operator|>=
literal|0
condition|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parent
init|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|)
name|stack
operator|.
name|get
argument_list|(
name|start
argument_list|)
decl_stmt|;
if|if
condition|(
name|parent
operator|instanceof
name|UnionOperator
condition|)
block|{
name|parentUnionOperator
operator|=
operator|(
name|UnionOperator
operator|)
name|parent
expr_stmt|;
break|break;
block|}
name|start
operator|--
expr_stmt|;
block|}
assert|assert
name|parentUnionOperator
operator|!=
literal|null
assert|;
comment|// default to false
name|boolean
name|mapOnly
init|=
literal|false
decl_stmt|;
name|boolean
name|rootTask
init|=
literal|false
decl_stmt|;
name|UnionParseContext
name|parentUCtx
init|=
name|ctx
operator|.
name|getUnionParseContext
argument_list|(
name|parentUnionOperator
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentUCtx
operator|!=
literal|null
operator|&&
name|parentUCtx
operator|.
name|allMapOnlySubQSet
argument_list|()
condition|)
block|{
name|mapOnly
operator|=
name|parentUCtx
operator|.
name|allMapOnlySubQ
argument_list|()
expr_stmt|;
name|rootTask
operator|=
name|parentUCtx
operator|.
name|allRootTasks
argument_list|()
expr_stmt|;
block|}
name|uCtx
operator|.
name|setMapOnlySubq
argument_list|(
name|pos
argument_list|,
name|mapOnly
argument_list|)
expr_stmt|;
name|uCtx
operator|.
name|setRootTask
argument_list|(
name|pos
argument_list|,
name|rootTask
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|setUnionParseContext
argument_list|(
name|union
argument_list|,
name|uCtx
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Union followed by no processing.    * This is to optimize queries of the type:    * select * from (subq1 union all subq2 ...)x;    * where at least one of the queries involve a map-reduce job.    * There is no need for a union in this scenario - it involves an extra    * write and read for the final output without this optimization.    * Queries of the form:    *   select x.c1 from (subq1 union all subq2 ...)x where filter(x.c2);    * can be transformed to:    *   select * from (subq1 where filter union all subq2 where filter ...)x;    * and then optimized.    */
specifier|public
specifier|static
class|class
name|UnionNoProcessFile
implements|implements
name|NodeProcessor
block|{
specifier|private
name|void
name|pushOperatorsAboveUnion
parameter_list|(
name|UnionOperator
name|union
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|int
name|pos
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// Clone all the operators between union and filescan, and push them above
comment|// the union. Remove the union (the tree below union gets delinked after that)
try|try
block|{
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|parents
init|=
name|union
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
name|int
name|numParents
init|=
name|parents
operator|.
name|size
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
name|parent
range|:
name|parents
control|)
block|{
name|parent
operator|.
name|setChildOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
for|for
control|(
init|;
name|pos
operator|<
name|stack
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|;
name|pos
operator|++
control|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|originalOp
init|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|)
name|stack
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|p
init|=
literal|0
init|;
name|p
operator|<
name|numParents
condition|;
name|p
operator|++
control|)
block|{
name|OperatorDesc
name|cloneDesc
init|=
operator|(
name|OperatorDesc
operator|)
name|originalOp
operator|.
name|getConf
argument_list|()
operator|.
name|clone
argument_list|()
decl_stmt|;
name|RowSchema
name|origSchema
init|=
name|originalOp
operator|.
name|getSchema
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|origColExprMap
init|=
name|originalOp
operator|.
name|getColumnExprMap
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|cloneOp
init|=
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
name|cloneDesc
argument_list|,
name|origSchema
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|RowSchema
argument_list|(
name|origSchema
argument_list|)
argument_list|,
name|origColExprMap
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|HashMap
argument_list|(
name|origColExprMap
argument_list|)
argument_list|,
name|parents
operator|.
name|get
argument_list|(
name|p
argument_list|)
argument_list|)
decl_stmt|;
name|parents
operator|.
name|set
argument_list|(
name|p
argument_list|,
name|cloneOp
argument_list|)
expr_stmt|;
block|}
block|}
comment|// FileSink cannot be simply cloned - it requires some special processing.
comment|// Sub-queries for the union will be processed as independent map-reduce jobs
comment|// possibly running in parallel. Those sub-queries cannot write to the same
comment|// directory. Clone the filesink, but create a sub-directory in the final path
comment|// for each sub-query. Also, these different filesinks need to be linked to each other
name|FileSinkOperator
name|fileSinkOp
init|=
operator|(
name|FileSinkOperator
operator|)
name|stack
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
comment|// For file sink operator, change the directory name
name|Path
name|parentDirName
init|=
name|fileSinkOp
operator|.
name|getConf
argument_list|()
operator|.
name|getDirName
argument_list|()
decl_stmt|;
comment|// Clone the fileSinkDesc of the final fileSink and create similar fileSinks at
comment|// each parent
name|List
argument_list|<
name|FileSinkDesc
argument_list|>
name|fileDescLists
init|=
operator|new
name|ArrayList
argument_list|<
name|FileSinkDesc
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
name|parent
range|:
name|parents
control|)
block|{
name|FileSinkDesc
name|fileSinkDesc
init|=
operator|(
name|FileSinkDesc
operator|)
name|fileSinkOp
operator|.
name|getConf
argument_list|()
operator|.
name|clone
argument_list|()
decl_stmt|;
name|fileSinkDesc
operator|.
name|setDirName
argument_list|(
operator|new
name|Path
argument_list|(
name|parentDirName
argument_list|,
name|parent
operator|.
name|getIdentifier
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|fileSinkDesc
operator|.
name|setLinkedFileSink
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|fileSinkDesc
operator|.
name|setParentDir
argument_list|(
name|parentDirName
argument_list|)
expr_stmt|;
name|parent
operator|.
name|setChildOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|tmpFileSinkOp
init|=
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
name|fileSinkDesc
argument_list|,
name|parent
operator|.
name|getSchema
argument_list|()
argument_list|,
name|parent
argument_list|)
decl_stmt|;
name|tmpFileSinkOp
operator|.
name|setChildOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fileDescLists
operator|.
name|add
argument_list|(
name|fileSinkDesc
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|FileSinkDesc
name|fileDesc
range|:
name|fileDescLists
control|)
block|{
name|fileDesc
operator|.
name|setLinkedFileSinkDesc
argument_list|(
name|fileDescLists
argument_list|)
expr_stmt|;
block|}
comment|// delink union
name|union
operator|.
name|setChildOperators
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|union
operator|.
name|setParentOperators
argument_list|(
literal|null
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
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
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
name|FileSinkOperator
name|fileSinkOp
init|=
operator|(
name|FileSinkOperator
operator|)
name|nd
decl_stmt|;
comment|// Has this filesink already been processed
if|if
condition|(
name|fileSinkOp
operator|.
name|getConf
argument_list|()
operator|.
name|isLinkedFileSink
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|size
init|=
name|stack
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|pos
init|=
name|size
operator|-
literal|2
decl_stmt|;
name|UnionOperator
name|union
init|=
literal|null
decl_stmt|;
comment|// Walk the tree. As long as the operators between the union and the filesink
comment|// do not involve a reducer, and they can be pushed above the union, it makes
comment|// sense to push them above the union, and remove the union. An interface
comment|// has been added to the operator 'supportUnionRemoveOptimization' to denote whether
comment|// this operator can be removed.
while|while
condition|(
name|pos
operator|>=
literal|0
condition|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|operator
init|=
operator|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
operator|)
name|stack
operator|.
name|get
argument_list|(
name|pos
argument_list|)
decl_stmt|;
comment|// (1) Because we have operator.supportUnionRemoveOptimization() for
comment|// true only in SEL and FIL operators,
comment|// this rule will actually only match UNION%(SEL%|FIL%)*FS%
comment|// (2) The assumption here is that, if
comment|// operator.getChildOperators().size()> 1, we are going to have
comment|// multiple FS operators, i.e., multiple inserts.
comment|// Current implementation does not support this. More details, please
comment|// see HIVE-9217.
if|if
condition|(
name|operator
operator|.
name|getChildOperators
argument_list|()
operator|!=
literal|null
operator|&&
name|operator
operator|.
name|getChildOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Break if it encountered a union
if|if
condition|(
name|operator
operator|instanceof
name|UnionOperator
condition|)
block|{
name|union
operator|=
operator|(
name|UnionOperator
operator|)
name|operator
expr_stmt|;
break|break;
block|}
if|if
condition|(
operator|!
name|operator
operator|.
name|supportUnionRemoveOptimization
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|pos
operator|--
expr_stmt|;
block|}
name|UnionProcContext
name|ctx
init|=
operator|(
name|UnionProcContext
operator|)
name|procCtx
decl_stmt|;
name|UnionParseContext
name|uCtx
init|=
name|ctx
operator|.
name|getUnionParseContext
argument_list|(
name|union
argument_list|)
decl_stmt|;
comment|// No need for this if all sub-queries are map-only queries
comment|// If all the queries are map-only, anyway the query is most optimized
if|if
condition|(
operator|(
name|uCtx
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|uCtx
operator|.
name|allMapOnlySubQ
argument_list|()
operator|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|pos
operator|++
expr_stmt|;
name|pushOperatorsAboveUnion
argument_list|(
name|union
argument_list|,
name|stack
argument_list|,
name|pos
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Default processor.    */
specifier|public
specifier|static
class|class
name|NoUnion
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
return|return
literal|null
return|;
block|}
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getMapRedUnion
parameter_list|()
block|{
return|return
operator|new
name|MapRedUnion
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getMapUnion
parameter_list|()
block|{
return|return
operator|new
name|MapUnion
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getUnknownUnion
parameter_list|()
block|{
return|return
operator|new
name|UnknownUnion
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getNoUnion
parameter_list|()
block|{
return|return
operator|new
name|NoUnion
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|NodeProcessor
name|getUnionNoProcessFile
parameter_list|()
block|{
return|return
operator|new
name|UnionNoProcessFile
argument_list|()
return|;
block|}
block|}
end_class

end_unit

