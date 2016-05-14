begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements. See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License. You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Iterator
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
name|Map
operator|.
name|Entry
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
name|ql
operator|.
name|exec
operator|.
name|ColumnInfo
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
name|LimitOperator
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
name|plan
operator|.
name|ExprNodeColumnDesc
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
comment|/**  * This class implements the processor context for Constant Propagate.  *  * ConstantPropagateProcCtx keeps track of propagated constants in a column->const map for each  * operator, enabling constants to be revolved across operators.  */
end_comment

begin_class
specifier|public
class|class
name|ConstantPropagateProcCtx
implements|implements
name|NodeProcessorCtx
block|{
specifier|public
enum|enum
name|ConstantPropagateOption
block|{
name|FULL
block|,
comment|// Do full constant propagation
name|SHORTCUT
block|,
comment|// Only perform expression short-cutting - remove unnecessary AND/OR operators
comment|// if one of the child conditions is true/false.
block|}
empty_stmt|;
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
name|ConstantPropagateProcCtx
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|Map
argument_list|<
name|ColumnInfo
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|>
name|opToConstantExprs
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|opToDelete
decl_stmt|;
specifier|private
name|ConstantPropagateOption
name|constantPropagateOption
init|=
name|ConstantPropagateOption
operator|.
name|FULL
decl_stmt|;
specifier|public
name|ConstantPropagateProcCtx
parameter_list|()
block|{
name|this
argument_list|(
name|ConstantPropagateOption
operator|.
name|FULL
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ConstantPropagateProcCtx
parameter_list|(
name|ConstantPropagateOption
name|option
parameter_list|)
block|{
name|opToConstantExprs
operator|=
operator|new
name|HashMap
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|Map
argument_list|<
name|ColumnInfo
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|opToDelete
operator|=
operator|new
name|HashSet
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|constantPropagateOption
operator|=
name|option
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|,
name|Map
argument_list|<
name|ColumnInfo
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|>
name|getOpToConstantExprs
parameter_list|()
block|{
return|return
name|opToConstantExprs
return|;
block|}
comment|/**    * Get propagated constant map from parents.    *    * Traverse all parents of current operator, if there is propagated constant (determined by    * assignment expression like column=constant value), resolve the column using RowResolver and add    * it to current constant map.    *    * @param op    *        operator getting the propagated constants.    * @return map of ColumnInfo to ExprNodeDesc. The values of that map must be either    *         ExprNodeConstantDesc or ExprNodeNullDesc.    */
specifier|public
name|Map
argument_list|<
name|ColumnInfo
argument_list|,
name|ExprNodeDesc
argument_list|>
name|getPropagatedConstants
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
parameter_list|)
block|{
comment|// this map should map columnInfo to ExprConstantNodeDesc
name|Map
argument_list|<
name|ColumnInfo
argument_list|,
name|ExprNodeDesc
argument_list|>
name|constants
init|=
operator|new
name|HashMap
argument_list|<
name|ColumnInfo
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|op
operator|.
name|getSchema
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
name|constants
return|;
block|}
name|RowSchema
name|rs
init|=
name|op
operator|.
name|getSchema
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Getting constants of op:"
operator|+
name|op
operator|+
literal|" with rs:"
operator|+
name|rs
argument_list|)
expr_stmt|;
if|if
condition|(
name|op
operator|.
name|getParentOperators
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
name|constants
return|;
block|}
comment|// A previous solution is based on tableAlias and colAlias, which is
comment|// unsafe, esp. when CBO generates derived table names. see HIVE-13602.
comment|// For correctness purpose, we only trust colExpMap.
comment|// We assume that CBO can do the constantPropagation before this function is
comment|// called to help improve the performance.
comment|// UnionOperator, LimitOperator and FilterOperator are special, they should already be
comment|// column-position aligned.
name|List
argument_list|<
name|Map
argument_list|<
name|Integer
argument_list|,
name|ExprNodeDesc
argument_list|>
argument_list|>
name|parentsToConstant
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|boolean
name|areAllParentsContainConstant
init|=
literal|true
decl_stmt|;
name|boolean
name|noParentsContainConstant
init|=
literal|true
decl_stmt|;
for|for
control|(
name|Operator
argument_list|<
name|?
argument_list|>
name|parent
range|:
name|op
operator|.
name|getParentOperators
argument_list|()
control|)
block|{
name|Map
argument_list|<
name|ColumnInfo
argument_list|,
name|ExprNodeDesc
argument_list|>
name|constMap
init|=
name|opToConstantExprs
operator|.
name|get
argument_list|(
name|parent
argument_list|)
decl_stmt|;
if|if
condition|(
name|constMap
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Constant of Op "
operator|+
name|parent
operator|.
name|getOperatorId
argument_list|()
operator|+
literal|" is not found"
argument_list|)
expr_stmt|;
name|areAllParentsContainConstant
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|noParentsContainConstant
operator|=
literal|false
expr_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|ExprNodeDesc
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|ColumnInfo
argument_list|,
name|ExprNodeDesc
argument_list|>
name|entry
range|:
name|constMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|map
operator|.
name|put
argument_list|(
name|parent
operator|.
name|getSchema
argument_list|()
operator|.
name|getPosition
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getInternalName
argument_list|()
argument_list|)
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|parentsToConstant
operator|.
name|add
argument_list|(
name|map
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Constant of Op "
operator|+
name|parent
operator|.
name|getOperatorId
argument_list|()
operator|+
literal|" "
operator|+
name|constMap
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|noParentsContainConstant
condition|)
block|{
return|return
name|constants
return|;
block|}
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|signature
init|=
name|op
operator|.
name|getSchema
argument_list|()
operator|.
name|getSignature
argument_list|()
decl_stmt|;
if|if
condition|(
name|op
operator|instanceof
name|LimitOperator
operator|||
name|op
operator|instanceof
name|FilterOperator
condition|)
block|{
comment|// there should be only one parent.
if|if
condition|(
name|op
operator|.
name|getParentOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|Map
argument_list|<
name|Integer
argument_list|,
name|ExprNodeDesc
argument_list|>
name|parentToConstant
init|=
name|parentsToConstant
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|signature
operator|.
name|size
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
if|if
condition|(
name|parentToConstant
operator|.
name|containsKey
argument_list|(
name|index
argument_list|)
condition|)
block|{
name|constants
operator|.
name|put
argument_list|(
name|signature
operator|.
name|get
argument_list|(
name|index
argument_list|)
argument_list|,
name|parentToConstant
operator|.
name|get
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|op
operator|instanceof
name|UnionOperator
operator|&&
name|areAllParentsContainConstant
condition|)
block|{
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|signature
operator|.
name|size
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
name|ExprNodeDesc
name|constant
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Map
argument_list|<
name|Integer
argument_list|,
name|ExprNodeDesc
argument_list|>
name|parentToConstant
range|:
name|parentsToConstant
control|)
block|{
if|if
condition|(
operator|!
name|parentToConstant
operator|.
name|containsKey
argument_list|(
name|index
argument_list|)
condition|)
block|{
comment|// if this parent does not contain a constant at this position, we
comment|// continue to look at other positions.
name|constant
operator|=
literal|null
expr_stmt|;
break|break;
block|}
else|else
block|{
if|if
condition|(
name|constant
operator|==
literal|null
condition|)
block|{
name|constant
operator|=
name|parentToConstant
operator|.
name|get
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// compare if they are the same constant.
name|ExprNodeDesc
name|nextConstant
init|=
name|parentToConstant
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|nextConstant
operator|.
name|isSame
argument_list|(
name|constant
argument_list|)
condition|)
block|{
comment|// they are not the same constant. for example, union all of 1
comment|// and 2.
name|constant
operator|=
literal|null
expr_stmt|;
break|break;
block|}
block|}
block|}
block|}
comment|// we have checked all the parents for the "index" position.
if|if
condition|(
name|constant
operator|!=
literal|null
condition|)
block|{
name|constants
operator|.
name|put
argument_list|(
name|signature
operator|.
name|get
argument_list|(
name|index
argument_list|)
argument_list|,
name|constant
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|op
operator|instanceof
name|JoinOperator
condition|)
block|{
name|JoinOperator
name|joinOp
init|=
operator|(
name|JoinOperator
operator|)
name|op
decl_stmt|;
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
argument_list|>
name|itr
init|=
name|joinOp
operator|.
name|getConf
argument_list|()
operator|.
name|getExprs
argument_list|()
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|itr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Entry
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|>
name|e
init|=
name|itr
operator|.
name|next
argument_list|()
decl_stmt|;
name|int
name|tag
init|=
name|e
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Operator
argument_list|<
name|?
argument_list|>
name|parent
init|=
name|op
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|exprs
init|=
name|e
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|exprs
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|ExprNodeDesc
name|expr
range|:
name|exprs
control|)
block|{
comment|// we are only interested in ExprNodeColumnDesc
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
name|String
name|parentColName
init|=
operator|(
operator|(
name|ExprNodeColumnDesc
operator|)
name|expr
operator|)
operator|.
name|getColumn
argument_list|()
decl_stmt|;
comment|// find this parentColName in its parent's rs
name|int
name|parentPos
init|=
name|parent
operator|.
name|getSchema
argument_list|()
operator|.
name|getPosition
argument_list|(
name|parentColName
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentsToConstant
operator|.
name|get
argument_list|(
name|tag
argument_list|)
operator|.
name|containsKey
argument_list|(
name|parentPos
argument_list|)
condition|)
block|{
comment|// this position in parent is a constant
comment|// reverse look up colExprMap to find the childColName
if|if
condition|(
name|op
operator|.
name|getColumnExprMap
argument_list|()
operator|!=
literal|null
operator|&&
name|op
operator|.
name|getColumnExprMap
argument_list|()
operator|.
name|entrySet
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|entry
range|:
name|op
operator|.
name|getColumnExprMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|isSame
argument_list|(
name|expr
argument_list|)
condition|)
block|{
comment|// now propagate the constant from the parent to the child
name|constants
operator|.
name|put
argument_list|(
name|signature
operator|.
name|get
argument_list|(
name|op
operator|.
name|getSchema
argument_list|()
operator|.
name|getPosition
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|parentsToConstant
operator|.
name|get
argument_list|(
name|tag
argument_list|)
operator|.
name|get
argument_list|(
name|parentPos
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
block|}
block|}
else|else
block|{
comment|// there should be only one parent.
if|if
condition|(
name|op
operator|.
name|getParentOperators
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|Operator
argument_list|<
name|?
argument_list|>
name|parent
init|=
name|op
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|op
operator|.
name|getColumnExprMap
argument_list|()
operator|!=
literal|null
operator|&&
name|op
operator|.
name|getColumnExprMap
argument_list|()
operator|.
name|entrySet
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|entry
range|:
name|op
operator|.
name|getColumnExprMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|op
operator|.
name|getSchema
argument_list|()
operator|.
name|getPosition
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
comment|// Not present
continue|continue;
block|}
name|ExprNodeDesc
name|expr
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|expr
operator|instanceof
name|ExprNodeColumnDesc
condition|)
block|{
name|String
name|parentColName
init|=
operator|(
operator|(
name|ExprNodeColumnDesc
operator|)
name|expr
operator|)
operator|.
name|getColumn
argument_list|()
decl_stmt|;
comment|// find this parentColName in its parent's rs
name|int
name|parentPos
init|=
name|parent
operator|.
name|getSchema
argument_list|()
operator|.
name|getPosition
argument_list|(
name|parentColName
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentsToConstant
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|containsKey
argument_list|(
name|parentPos
argument_list|)
condition|)
block|{
comment|// this position in parent is a constant
comment|// now propagate the constant from the parent to the child
name|constants
operator|.
name|put
argument_list|(
name|signature
operator|.
name|get
argument_list|(
name|op
operator|.
name|getSchema
argument_list|()
operator|.
name|getPosition
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|parentsToConstant
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|(
name|parentPos
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Offerring constants "
operator|+
name|constants
operator|.
name|keySet
argument_list|()
operator|+
literal|" to operator "
operator|+
name|op
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|constants
return|;
block|}
specifier|public
name|void
name|addOpToDelete
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|op
parameter_list|)
block|{
name|opToDelete
operator|.
name|add
argument_list|(
name|op
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Set
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|getOpToDelete
parameter_list|()
block|{
return|return
name|opToDelete
return|;
block|}
specifier|public
name|ConstantPropagateOption
name|getConstantPropagateOption
parameter_list|()
block|{
return|return
name|constantPropagateOption
return|;
block|}
specifier|public
name|void
name|setConstantPropagateOption
parameter_list|(
name|ConstantPropagateOption
name|constantPropagateOption
parameter_list|)
block|{
name|this
operator|.
name|constantPropagateOption
operator|=
name|constantPropagateOption
expr_stmt|;
block|}
block|}
end_class

end_unit

