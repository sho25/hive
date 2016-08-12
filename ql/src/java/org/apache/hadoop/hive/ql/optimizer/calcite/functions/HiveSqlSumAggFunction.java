begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|functions
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
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|core
operator|.
name|AggregateCall
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
name|type
operator|.
name|RelDataType
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
name|type
operator|.
name|RelDataTypeFactory
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
name|type
operator|.
name|RelDataTypeField
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
name|RexBuilder
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
name|sql
operator|.
name|SqlAggFunction
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
name|sql
operator|.
name|SqlFunctionCategory
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
name|sql
operator|.
name|SqlKind
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
name|sql
operator|.
name|SqlSplittableAggFunction
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
name|sql
operator|.
name|SqlSplittableAggFunction
operator|.
name|SumSplitter
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
name|sql
operator|.
name|fun
operator|.
name|SqlStdOperatorTable
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
name|sql
operator|.
name|type
operator|.
name|ReturnTypes
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
name|sql
operator|.
name|type
operator|.
name|SqlOperandTypeChecker
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
name|sql
operator|.
name|type
operator|.
name|SqlOperandTypeInference
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
name|sql
operator|.
name|type
operator|.
name|SqlReturnTypeInference
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
name|sql
operator|.
name|type
operator|.
name|SqlTypeName
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
name|ImmutableIntList
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
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_comment
comment|/**  *<code>Sum</code> is an aggregator which returns the sum of the values which  * go into it. It has precisely one argument of numeric type (<code>int</code>,  *<code>long</code>,<code>float</code>,<code>double</code>), and the result  * is the same type.  */
end_comment

begin_class
specifier|public
class|class
name|HiveSqlSumAggFunction
extends|extends
name|SqlAggFunction
implements|implements
name|CanAggregateDistinct
block|{
specifier|final
name|boolean
name|isDistinct
decl_stmt|;
specifier|final
name|SqlReturnTypeInference
name|returnTypeInference
decl_stmt|;
specifier|final
name|SqlOperandTypeInference
name|operandTypeInference
decl_stmt|;
specifier|final
name|SqlOperandTypeChecker
name|operandTypeChecker
decl_stmt|;
comment|//~ Constructors -----------------------------------------------------------
specifier|public
name|HiveSqlSumAggFunction
parameter_list|(
name|boolean
name|isDistinct
parameter_list|,
name|SqlReturnTypeInference
name|returnTypeInference
parameter_list|,
name|SqlOperandTypeInference
name|operandTypeInference
parameter_list|,
name|SqlOperandTypeChecker
name|operandTypeChecker
parameter_list|)
block|{
name|super
argument_list|(
literal|"sum"
argument_list|,
name|SqlKind
operator|.
name|SUM
argument_list|,
name|returnTypeInference
argument_list|,
name|operandTypeInference
argument_list|,
name|operandTypeChecker
argument_list|,
name|SqlFunctionCategory
operator|.
name|NUMERIC
argument_list|)
expr_stmt|;
name|this
operator|.
name|returnTypeInference
operator|=
name|returnTypeInference
expr_stmt|;
name|this
operator|.
name|operandTypeChecker
operator|=
name|operandTypeChecker
expr_stmt|;
name|this
operator|.
name|operandTypeInference
operator|=
name|operandTypeInference
expr_stmt|;
name|this
operator|.
name|isDistinct
operator|=
name|isDistinct
expr_stmt|;
block|}
comment|//~ Methods ----------------------------------------------------------------
annotation|@
name|Override
specifier|public
name|boolean
name|isDistinct
parameter_list|()
block|{
return|return
name|isDistinct
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|unwrap
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|)
block|{
if|if
condition|(
name|clazz
operator|==
name|SqlSplittableAggFunction
operator|.
name|class
condition|)
block|{
return|return
name|clazz
operator|.
name|cast
argument_list|(
operator|new
name|HiveSumSplitter
argument_list|()
argument_list|)
return|;
block|}
return|return
name|super
operator|.
name|unwrap
argument_list|(
name|clazz
argument_list|)
return|;
block|}
class|class
name|HiveSumSplitter
extends|extends
name|SumSplitter
block|{
annotation|@
name|Override
specifier|public
name|AggregateCall
name|other
parameter_list|(
name|RelDataTypeFactory
name|typeFactory
parameter_list|,
name|AggregateCall
name|e
parameter_list|)
block|{
name|RelDataType
name|countRetType
init|=
name|typeFactory
operator|.
name|createTypeWithNullability
argument_list|(
name|typeFactory
operator|.
name|createSqlType
argument_list|(
name|SqlTypeName
operator|.
name|BIGINT
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
return|return
name|AggregateCall
operator|.
name|create
argument_list|(
operator|new
name|HiveSqlCountAggFunction
argument_list|(
name|isDistinct
argument_list|,
name|ReturnTypes
operator|.
name|explicit
argument_list|(
name|countRetType
argument_list|)
argument_list|,
name|operandTypeInference
argument_list|,
name|operandTypeChecker
argument_list|)
argument_list|,
literal|false
argument_list|,
name|ImmutableIntList
operator|.
name|of
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|,
name|countRetType
argument_list|,
literal|"count"
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AggregateCall
name|topSplit
parameter_list|(
name|RexBuilder
name|rexBuilder
parameter_list|,
name|Registry
argument_list|<
name|RexNode
argument_list|>
name|extra
parameter_list|,
name|int
name|offset
parameter_list|,
name|RelDataType
name|inputRowType
parameter_list|,
name|AggregateCall
name|aggregateCall
parameter_list|,
name|int
name|leftSubTotal
parameter_list|,
name|int
name|rightSubTotal
parameter_list|)
block|{
specifier|final
name|List
argument_list|<
name|RexNode
argument_list|>
name|merges
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|RelDataTypeField
argument_list|>
name|fieldList
init|=
name|inputRowType
operator|.
name|getFieldList
argument_list|()
decl_stmt|;
if|if
condition|(
name|leftSubTotal
operator|>=
literal|0
condition|)
block|{
specifier|final
name|RelDataType
name|type
init|=
name|fieldList
operator|.
name|get
argument_list|(
name|leftSubTotal
argument_list|)
operator|.
name|getType
argument_list|()
decl_stmt|;
name|merges
operator|.
name|add
argument_list|(
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|type
argument_list|,
name|leftSubTotal
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rightSubTotal
operator|>=
literal|0
condition|)
block|{
specifier|final
name|RelDataType
name|type
init|=
name|fieldList
operator|.
name|get
argument_list|(
name|rightSubTotal
argument_list|)
operator|.
name|getType
argument_list|()
decl_stmt|;
name|merges
operator|.
name|add
argument_list|(
name|rexBuilder
operator|.
name|makeInputRef
argument_list|(
name|type
argument_list|,
name|rightSubTotal
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|RexNode
name|node
decl_stmt|;
switch|switch
condition|(
name|merges
operator|.
name|size
argument_list|()
condition|)
block|{
case|case
literal|1
case|:
name|node
operator|=
name|merges
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|node
operator|=
name|rexBuilder
operator|.
name|makeCall
argument_list|(
name|SqlStdOperatorTable
operator|.
name|MULTIPLY
argument_list|,
name|merges
argument_list|)
expr_stmt|;
name|node
operator|=
name|rexBuilder
operator|.
name|makeAbstractCast
argument_list|(
name|aggregateCall
operator|.
name|type
argument_list|,
name|node
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"unexpected count "
operator|+
name|merges
argument_list|)
throw|;
block|}
name|int
name|ordinal
init|=
name|extra
operator|.
name|register
argument_list|(
name|node
argument_list|)
decl_stmt|;
return|return
name|AggregateCall
operator|.
name|create
argument_list|(
operator|new
name|HiveSqlSumAggFunction
argument_list|(
name|isDistinct
argument_list|,
name|returnTypeInference
argument_list|,
name|operandTypeInference
argument_list|,
name|operandTypeChecker
argument_list|)
argument_list|,
literal|false
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|ordinal
argument_list|)
argument_list|,
operator|-
literal|1
argument_list|,
name|aggregateCall
operator|.
name|type
argument_list|,
name|aggregateCall
operator|.
name|name
argument_list|)
return|;
block|}
block|}
block|}
end_class

begin_comment
comment|// End SqlSumAggFunction.java
end_comment

end_unit

