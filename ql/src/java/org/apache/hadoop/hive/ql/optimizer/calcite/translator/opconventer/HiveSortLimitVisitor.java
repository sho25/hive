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
operator|.
name|calcite
operator|.
name|translator
operator|.
name|opconventer
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
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|RelCollations
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|RelFieldCollation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rex
operator|.
name|RexLiteral
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rex
operator|.
name|RexNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|util
operator|.
name|ImmutableBitSet
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
operator|.
name|StrictChecks
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
name|io
operator|.
name|AcidUtils
operator|.
name|Operation
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
name|calcite
operator|.
name|reloperators
operator|.
name|HiveSortLimit
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
name|calcite
operator|.
name|translator
operator|.
name|opconventer
operator|.
name|HiveOpConverter
operator|.
name|OpAttr
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
name|LimitDesc
import|;
end_import

begin_class
class|class
name|HiveSortLimitVisitor
extends|extends
name|HiveRelNodeVisitor
argument_list|<
name|HiveSortLimit
argument_list|>
block|{
name|HiveSortLimitVisitor
parameter_list|(
name|HiveOpConverter
name|hiveOpConverter
parameter_list|)
block|{
name|super
argument_list|(
name|hiveOpConverter
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|OpAttr
name|visit
parameter_list|(
name|HiveSortLimit
name|sortRel
parameter_list|)
throws|throws
name|SemanticException
block|{
name|OpAttr
name|inputOpAf
init|=
name|hiveOpConverter
operator|.
name|dispatch
argument_list|(
name|sortRel
operator|.
name|getInput
argument_list|()
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
literal|"Translating operator rel#"
operator|+
name|sortRel
operator|.
name|getId
argument_list|()
operator|+
literal|":"
operator|+
name|sortRel
operator|.
name|getRelTypeName
argument_list|()
operator|+
literal|" with row type: ["
operator|+
name|sortRel
operator|.
name|getRowType
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
if|if
condition|(
name|sortRel
operator|.
name|getCollation
argument_list|()
operator|==
name|RelCollations
operator|.
name|EMPTY
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Operator rel#"
operator|+
name|sortRel
operator|.
name|getId
argument_list|()
operator|+
literal|":"
operator|+
name|sortRel
operator|.
name|getRelTypeName
argument_list|()
operator|+
literal|" consists of limit"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sortRel
operator|.
name|fetch
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Operator rel#"
operator|+
name|sortRel
operator|.
name|getId
argument_list|()
operator|+
literal|":"
operator|+
name|sortRel
operator|.
name|getRelTypeName
argument_list|()
operator|+
literal|" consists of sort"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Operator rel#"
operator|+
name|sortRel
operator|.
name|getId
argument_list|()
operator|+
literal|":"
operator|+
name|sortRel
operator|.
name|getRelTypeName
argument_list|()
operator|+
literal|" consists of sort+limit"
argument_list|)
expr_stmt|;
block|}
block|}
name|Operator
argument_list|<
name|?
argument_list|>
name|inputOp
init|=
name|inputOpAf
operator|.
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|?
argument_list|>
name|resultOp
init|=
name|inputOpAf
operator|.
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// 1. If we need to sort tuples based on the value of some
comment|// of their columns
if|if
condition|(
name|sortRel
operator|.
name|getCollation
argument_list|()
operator|!=
name|RelCollations
operator|.
name|EMPTY
condition|)
block|{
comment|// In strict mode, in the presence of order by, limit must be specified.
if|if
condition|(
name|sortRel
operator|.
name|fetch
operator|==
literal|null
condition|)
block|{
name|String
name|error
init|=
name|StrictChecks
operator|.
name|checkNoLimit
argument_list|(
name|hiveOpConverter
operator|.
name|getHiveConf
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
throw|throw
operator|new
name|SemanticException
argument_list|(
name|error
argument_list|)
throw|;
block|}
comment|// 1.a. Extract order for each column from collation
comment|// Generate sortCols and order
name|ImmutableBitSet
operator|.
name|Builder
name|sortColsPosBuilder
init|=
name|ImmutableBitSet
operator|.
name|builder
argument_list|()
decl_stmt|;
name|ImmutableBitSet
operator|.
name|Builder
name|sortOutputColsPosBuilder
init|=
name|ImmutableBitSet
operator|.
name|builder
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Integer
argument_list|,
name|RexNode
argument_list|>
name|obRefToCallMap
init|=
name|sortRel
operator|.
name|getInputRefToCallMap
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|sortCols
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|StringBuilder
name|order
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|StringBuilder
name|nullOrder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|RelFieldCollation
name|sortInfo
range|:
name|sortRel
operator|.
name|getCollation
argument_list|()
operator|.
name|getFieldCollations
argument_list|()
control|)
block|{
name|int
name|sortColumnPos
init|=
name|sortInfo
operator|.
name|getFieldIndex
argument_list|()
decl_stmt|;
name|ColumnInfo
name|columnInfo
init|=
operator|new
name|ColumnInfo
argument_list|(
name|inputOp
operator|.
name|getSchema
argument_list|()
operator|.
name|getSignature
argument_list|()
operator|.
name|get
argument_list|(
name|sortColumnPos
argument_list|)
argument_list|)
decl_stmt|;
name|ExprNodeColumnDesc
name|sortColumn
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|columnInfo
operator|.
name|getType
argument_list|()
argument_list|,
name|columnInfo
operator|.
name|getInternalName
argument_list|()
argument_list|,
name|columnInfo
operator|.
name|getTabAlias
argument_list|()
argument_list|,
name|columnInfo
operator|.
name|getIsVirtualCol
argument_list|()
argument_list|)
decl_stmt|;
name|sortCols
operator|.
name|add
argument_list|(
name|sortColumn
argument_list|)
expr_stmt|;
if|if
condition|(
name|sortInfo
operator|.
name|getDirection
argument_list|()
operator|==
name|RelFieldCollation
operator|.
name|Direction
operator|.
name|DESCENDING
condition|)
block|{
name|order
operator|.
name|append
argument_list|(
literal|"-"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|order
operator|.
name|append
argument_list|(
literal|"+"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sortInfo
operator|.
name|nullDirection
operator|==
name|RelFieldCollation
operator|.
name|NullDirection
operator|.
name|FIRST
condition|)
block|{
name|nullOrder
operator|.
name|append
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sortInfo
operator|.
name|nullDirection
operator|==
name|RelFieldCollation
operator|.
name|NullDirection
operator|.
name|LAST
condition|)
block|{
name|nullOrder
operator|.
name|append
argument_list|(
literal|"z"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Default
name|nullOrder
operator|.
name|append
argument_list|(
name|sortInfo
operator|.
name|getDirection
argument_list|()
operator|==
name|RelFieldCollation
operator|.
name|Direction
operator|.
name|DESCENDING
condition|?
literal|"z"
else|:
literal|"a"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|obRefToCallMap
operator|!=
literal|null
condition|)
block|{
name|RexNode
name|obExpr
init|=
name|obRefToCallMap
operator|.
name|get
argument_list|(
name|sortColumnPos
argument_list|)
decl_stmt|;
name|sortColsPosBuilder
operator|.
name|set
argument_list|(
name|sortColumnPos
argument_list|)
expr_stmt|;
if|if
condition|(
name|obExpr
operator|==
literal|null
condition|)
block|{
name|sortOutputColsPosBuilder
operator|.
name|set
argument_list|(
name|sortColumnPos
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Use only 1 reducer for order by
name|int
name|numReducers
init|=
literal|1
decl_stmt|;
comment|// We keep the columns only the columns that are part of the final output
name|List
argument_list|<
name|String
argument_list|>
name|keepColumns
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|ImmutableBitSet
name|sortColsPos
init|=
name|sortColsPosBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|ImmutableBitSet
name|sortOutputColsPos
init|=
name|sortOutputColsPosBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|ColumnInfo
argument_list|>
name|inputSchema
init|=
name|inputOp
operator|.
name|getSchema
argument_list|()
operator|.
name|getSignature
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|inputSchema
operator|.
name|size
argument_list|()
condition|;
name|pos
operator|++
control|)
block|{
if|if
condition|(
operator|(
name|sortColsPos
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|&&
name|sortOutputColsPos
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|)
operator|||
operator|(
operator|!
name|sortColsPos
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|&&
operator|!
name|sortOutputColsPos
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|)
condition|)
block|{
name|keepColumns
operator|.
name|add
argument_list|(
name|inputSchema
operator|.
name|get
argument_list|(
name|pos
argument_list|)
operator|.
name|getInternalName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// 1.b. Generate reduce sink and project operator
name|resultOp
operator|=
name|HiveOpConverterUtils
operator|.
name|genReduceSinkAndBacktrackSelect
argument_list|(
name|resultOp
argument_list|,
name|sortCols
operator|.
name|toArray
argument_list|(
operator|new
name|ExprNodeDesc
index|[
name|sortCols
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
literal|0
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
argument_list|,
name|order
operator|.
name|toString
argument_list|()
argument_list|,
name|nullOrder
operator|.
name|toString
argument_list|()
argument_list|,
name|numReducers
argument_list|,
name|Operation
operator|.
name|NOT_ACID
argument_list|,
name|hiveOpConverter
operator|.
name|getHiveConf
argument_list|()
argument_list|,
name|keepColumns
argument_list|)
expr_stmt|;
block|}
comment|// 2. If we need to generate limit
if|if
condition|(
name|sortRel
operator|.
name|fetch
operator|!=
literal|null
condition|)
block|{
name|int
name|limit
init|=
name|RexLiteral
operator|.
name|intValue
argument_list|(
name|sortRel
operator|.
name|fetch
argument_list|)
decl_stmt|;
name|int
name|offset
init|=
name|sortRel
operator|.
name|offset
operator|==
literal|null
condition|?
literal|0
else|:
name|RexLiteral
operator|.
name|intValue
argument_list|(
name|sortRel
operator|.
name|offset
argument_list|)
decl_stmt|;
name|LimitDesc
name|limitDesc
init|=
operator|new
name|LimitDesc
argument_list|(
name|offset
argument_list|,
name|limit
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|cinfoLst
init|=
name|HiveOpConverterUtils
operator|.
name|createColInfos
argument_list|(
name|resultOp
argument_list|)
decl_stmt|;
name|resultOp
operator|=
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
name|limitDesc
argument_list|,
operator|new
name|RowSchema
argument_list|(
name|cinfoLst
argument_list|)
argument_list|,
name|resultOp
argument_list|)
expr_stmt|;
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
literal|"Generated "
operator|+
name|resultOp
operator|+
literal|" with row schema: ["
operator|+
name|resultOp
operator|.
name|getSchema
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
comment|// 3. Return result
return|return
name|inputOpAf
operator|.
name|clone
argument_list|(
name|resultOp
argument_list|)
return|;
block|}
block|}
end_class

end_unit

