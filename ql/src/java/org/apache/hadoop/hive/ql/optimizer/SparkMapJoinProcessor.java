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
package|;
end_package

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
name|ErrorMsg
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
name|JoinCondDesc
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_class
specifier|public
class|class
name|SparkMapJoinProcessor
extends|extends
name|MapJoinProcessor
block|{
comment|/**    * convert a regular join to a a map-side join.    *    * @param conf    * @param op join operator    * @param bigTablePos position of the source to be read as part of    *                   map-reduce framework. All other sources are cached in memory    * @param noCheckOuterJoin    * @param validateMapJoinTree    */
annotation|@
name|Override
specifier|public
name|MapJoinOperator
name|convertMapJoin
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|JoinOperator
name|op
parameter_list|,
name|boolean
name|leftSrc
parameter_list|,
name|String
index|[]
name|baseSrc
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|mapAliases
parameter_list|,
name|int
name|bigTablePos
parameter_list|,
name|boolean
name|noCheckOuterJoin
parameter_list|,
name|boolean
name|validateMapJoinTree
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// outer join cannot be performed on a table which is being cached
name|JoinCondDesc
index|[]
name|condns
init|=
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getConds
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|noCheckOuterJoin
condition|)
block|{
if|if
condition|(
name|checkMapJoin
argument_list|(
name|bigTablePos
argument_list|,
name|condns
argument_list|)
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|NO_OUTER_MAPJOIN
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|// create the map-join operator
name|MapJoinOperator
name|mapJoinOp
init|=
name|convertJoinOpMapJoinOp
argument_list|(
name|conf
argument_list|,
name|op
argument_list|,
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|isLeftInputJoin
argument_list|()
argument_list|,
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getBaseSrc
argument_list|()
argument_list|,
name|op
operator|.
name|getConf
argument_list|()
operator|.
name|getMapAliases
argument_list|()
argument_list|,
name|bigTablePos
argument_list|,
name|noCheckOuterJoin
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapJoinOp
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// 1. remove RS as parent for the big table branch
comment|// 2. remove old join op from child set of all the RSs
name|List
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|parentOps
init|=
name|mapJoinOp
operator|.
name|getParentOperators
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
name|parentOps
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parentOp
init|=
name|parentOps
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|parentOp
operator|.
name|getChildOperators
argument_list|()
operator|.
name|remove
argument_list|(
name|op
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|==
name|bigTablePos
condition|)
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
name|grandParentOps
init|=
name|parentOp
operator|.
name|getParentOperators
argument_list|()
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|grandParentOps
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|,
literal|"AssertionError: expect number of parents to be 1, but was "
operator|+
name|grandParentOps
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|grandParentOp
init|=
name|grandParentOps
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|grandParentOp
operator|.
name|replaceChild
argument_list|(
name|parentOp
argument_list|,
name|mapJoinOp
argument_list|)
expr_stmt|;
name|mapJoinOp
operator|.
name|replaceParent
argument_list|(
name|parentOp
argument_list|,
name|grandParentOp
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|mapJoinOp
return|;
block|}
block|}
end_class

end_unit

