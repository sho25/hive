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
name|LinkedList
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
name|HashTableDummyOperator
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
name|vector
operator|.
name|VectorizedRowBatchCtx
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfo
import|;
end_import

begin_comment
comment|/**  * BaseWork. Base class for any "work" that's being done on the cluster. Items like stats  * gathering that are commonly used regardless of the type of work live here.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"serial"
block|}
argument_list|)
specifier|public
specifier|abstract
class|class
name|BaseWork
extends|extends
name|AbstractOperatorDesc
block|{
specifier|protected
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|BaseWork
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// dummyOps is a reference to all the HashTableDummy operators in the
comment|// plan. These have to be separately initialized when we setup a task.
comment|// Their function is mainly as root ops to give the mapjoin the correct
comment|// schema info.
name|List
argument_list|<
name|HashTableDummyOperator
argument_list|>
name|dummyOps
decl_stmt|;
name|int
name|tag
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|sortColNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|MapredLocalWork
name|mrLocalWork
decl_stmt|;
specifier|public
name|BaseWork
parameter_list|()
block|{}
specifier|public
name|BaseWork
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|gatheringStats
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
comment|// Vectorization.
specifier|protected
name|VectorizedRowBatchCtx
name|vectorizedRowBatchCtx
decl_stmt|;
specifier|protected
name|boolean
name|useVectorizedInputFileFormat
decl_stmt|;
specifier|protected
name|boolean
name|llapMode
init|=
literal|false
decl_stmt|;
specifier|protected
name|boolean
name|uberMode
init|=
literal|false
decl_stmt|;
specifier|public
name|void
name|setGatheringStats
parameter_list|(
name|boolean
name|gatherStats
parameter_list|)
block|{
name|this
operator|.
name|gatheringStats
operator|=
name|gatherStats
expr_stmt|;
block|}
specifier|public
name|boolean
name|isGatheringStats
parameter_list|()
block|{
return|return
name|this
operator|.
name|gatheringStats
return|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
specifier|public
name|void
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|HashTableDummyOperator
argument_list|>
name|getDummyOps
parameter_list|()
block|{
return|return
name|dummyOps
return|;
block|}
specifier|public
name|void
name|setDummyOps
parameter_list|(
name|List
argument_list|<
name|HashTableDummyOperator
argument_list|>
name|dummyOps
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|dummyOps
operator|!=
literal|null
operator|&&
operator|!
name|this
operator|.
name|dummyOps
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|(
name|dummyOps
operator|==
literal|null
operator|||
name|dummyOps
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Removing dummy operators from "
operator|+
name|name
operator|+
literal|" "
operator|+
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|dummyOps
operator|=
name|dummyOps
expr_stmt|;
block|}
specifier|public
name|void
name|addDummyOp
parameter_list|(
name|HashTableDummyOperator
name|dummyOp
parameter_list|)
block|{
if|if
condition|(
name|dummyOps
operator|==
literal|null
condition|)
block|{
name|dummyOps
operator|=
operator|new
name|LinkedList
argument_list|<
name|HashTableDummyOperator
argument_list|>
argument_list|()
expr_stmt|;
block|}
name|dummyOps
operator|.
name|add
argument_list|(
name|dummyOp
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|abstract
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
function_decl|;
specifier|public
specifier|abstract
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|getAllRootOperators
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|getAnyRootOperator
parameter_list|()
function_decl|;
specifier|public
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|getAllOperators
parameter_list|()
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|returnSet
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
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|opSet
init|=
name|getAllRootOperators
argument_list|()
decl_stmt|;
name|Stack
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|opStack
init|=
operator|new
name|Stack
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// add all children
name|opStack
operator|.
name|addAll
argument_list|(
name|opSet
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|opStack
operator|.
name|empty
argument_list|()
condition|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|op
init|=
name|opStack
operator|.
name|pop
argument_list|()
decl_stmt|;
name|returnSet
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
if|if
condition|(
name|op
operator|.
name|getChildOperators
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|opStack
operator|.
name|addAll
argument_list|(
name|op
operator|.
name|getChildOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|returnSet
return|;
block|}
comment|/**    * Returns a set containing all leaf operators from the operator tree in this work.    * @return a set containing all leaf operators in this operator tree.    */
specifier|public
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|getAllLeafOperators
parameter_list|()
block|{
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|returnSet
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
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|opSet
init|=
name|getAllRootOperators
argument_list|()
decl_stmt|;
name|Stack
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
name|opStack
init|=
operator|new
name|Stack
argument_list|<
name|Operator
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// add all children
name|opStack
operator|.
name|addAll
argument_list|(
name|opSet
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|opStack
operator|.
name|empty
argument_list|()
condition|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|op
init|=
name|opStack
operator|.
name|pop
argument_list|()
decl_stmt|;
if|if
condition|(
name|op
operator|.
name|getNumChild
argument_list|()
operator|==
literal|0
condition|)
block|{
name|returnSet
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|op
operator|.
name|getChildOperators
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|opStack
operator|.
name|addAll
argument_list|(
name|op
operator|.
name|getChildOperators
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|returnSet
return|;
block|}
comment|// -----------------------------------------------------------------------------------------------
comment|/*    * The vectorization context for creating the VectorizedRowBatch for the node.    */
specifier|public
name|VectorizedRowBatchCtx
name|getVectorizedRowBatchCtx
parameter_list|()
block|{
return|return
name|vectorizedRowBatchCtx
return|;
block|}
specifier|public
name|void
name|setVectorizedRowBatchCtx
parameter_list|(
name|VectorizedRowBatchCtx
name|vectorizedRowBatchCtx
parameter_list|)
block|{
name|this
operator|.
name|vectorizedRowBatchCtx
operator|=
name|vectorizedRowBatchCtx
expr_stmt|;
block|}
comment|/*    * Whether the HiveConf.ConfVars.HIVE_VECTORIZATION_USE_VECTORIZED_INPUT_FILE_FORMAT variable    * (hive.vectorized.use.vectorized.input.format) was true when the Vectorizer class evaluated    * vectorizing this node.    *    * When Vectorized Input File Format looks at this flag, it can determine whether it should    * operate vectorized or not.  In some modes, the node can be vectorized but use row    * serialization.    */
specifier|public
name|void
name|setUseVectorizedInputFileFormat
parameter_list|(
name|boolean
name|useVectorizedInputFileFormat
parameter_list|)
block|{
name|this
operator|.
name|useVectorizedInputFileFormat
operator|=
name|useVectorizedInputFileFormat
expr_stmt|;
block|}
specifier|public
name|boolean
name|getUseVectorizedInputFileFormat
parameter_list|()
block|{
return|return
name|useVectorizedInputFileFormat
return|;
block|}
comment|// -----------------------------------------------------------------------------------------------
comment|/**    * @return the mapredLocalWork    */
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Local Work"
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
argument_list|)
specifier|public
name|MapredLocalWork
name|getMapRedLocalWork
parameter_list|()
block|{
return|return
name|mrLocalWork
return|;
block|}
comment|/**    * @param mapLocalWork    *          the mapredLocalWork to set    */
specifier|public
name|void
name|setMapRedLocalWork
parameter_list|(
specifier|final
name|MapredLocalWork
name|mapLocalWork
parameter_list|)
block|{
name|this
operator|.
name|mrLocalWork
operator|=
name|mapLocalWork
expr_stmt|;
block|}
specifier|public
name|void
name|setUberMode
parameter_list|(
name|boolean
name|uberMode
parameter_list|)
block|{
name|this
operator|.
name|uberMode
operator|=
name|uberMode
expr_stmt|;
block|}
specifier|public
name|boolean
name|getUberMode
parameter_list|()
block|{
return|return
name|uberMode
return|;
block|}
specifier|public
name|void
name|setLlapMode
parameter_list|(
name|boolean
name|llapMode
parameter_list|)
block|{
name|this
operator|.
name|llapMode
operator|=
name|llapMode
expr_stmt|;
block|}
specifier|public
name|boolean
name|getLlapMode
parameter_list|()
block|{
return|return
name|llapMode
return|;
block|}
specifier|public
specifier|abstract
name|void
name|configureJobConf
parameter_list|(
name|JobConf
name|job
parameter_list|)
function_decl|;
specifier|public
name|void
name|setTag
parameter_list|(
name|int
name|tag
parameter_list|)
block|{
name|this
operator|.
name|tag
operator|=
name|tag
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"tag"
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
name|int
name|getTag
parameter_list|()
block|{
return|return
name|tag
return|;
block|}
specifier|public
name|void
name|addSortCols
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|sortCols
parameter_list|)
block|{
name|this
operator|.
name|sortColNames
operator|.
name|addAll
argument_list|(
name|sortCols
argument_list|)
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getSortCols
parameter_list|()
block|{
return|return
name|sortColNames
return|;
block|}
block|}
end_class

end_unit

