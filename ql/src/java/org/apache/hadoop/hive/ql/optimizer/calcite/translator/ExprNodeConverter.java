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
name|calcite
operator|.
name|translator
package|;
end_package

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
name|Calendar
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
name|common
operator|.
name|type
operator|.
name|HiveChar
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
name|type
operator|.
name|HiveVarchar
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
name|ExprNodeConstantDesc
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
name|ExprNodeGenericFuncDesc
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDF
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
name|TypeInfoFactory
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
name|RexCall
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
name|RexInputRef
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
name|rex
operator|.
name|RexVisitorImpl
import|;
end_import

begin_comment
comment|/*  * convert a RexNode to an ExprNodeDesc  */
end_comment

begin_class
specifier|public
class|class
name|ExprNodeConverter
extends|extends
name|RexVisitorImpl
argument_list|<
name|ExprNodeDesc
argument_list|>
block|{
name|RelDataType
name|rType
decl_stmt|;
name|String
name|tabAlias
decl_stmt|;
name|boolean
name|partitioningExpr
decl_stmt|;
specifier|public
name|ExprNodeConverter
parameter_list|(
name|String
name|tabAlias
parameter_list|,
name|RelDataType
name|rType
parameter_list|,
name|boolean
name|partitioningExpr
parameter_list|)
block|{
name|super
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|tabAlias
operator|=
name|tabAlias
expr_stmt|;
name|this
operator|.
name|rType
operator|=
name|rType
expr_stmt|;
name|this
operator|.
name|partitioningExpr
operator|=
name|partitioningExpr
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ExprNodeDesc
name|visitInputRef
parameter_list|(
name|RexInputRef
name|inputRef
parameter_list|)
block|{
name|RelDataTypeField
name|f
init|=
name|rType
operator|.
name|getFieldList
argument_list|()
operator|.
name|get
argument_list|(
name|inputRef
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|TypeConverter
operator|.
name|convert
argument_list|(
name|f
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|,
name|f
operator|.
name|getName
argument_list|()
argument_list|,
name|tabAlias
argument_list|,
name|partitioningExpr
argument_list|)
return|;
block|}
comment|/**    * TODO: Handle 1) cast 2) Field Access 3) Windowing Over() 4, Windowing Agg Call    */
annotation|@
name|Override
specifier|public
name|ExprNodeDesc
name|visitCall
parameter_list|(
name|RexCall
name|call
parameter_list|)
block|{
name|ExprNodeGenericFuncDesc
name|gfDesc
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|deep
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|args
init|=
operator|new
name|LinkedList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|RexNode
name|operand
range|:
name|call
operator|.
name|operands
control|)
block|{
name|args
operator|.
name|add
argument_list|(
name|operand
operator|.
name|accept
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// If Expr is flat (and[p,q,r,s] or[p,q,r,s]) then recursively build the
comment|// exprnode
if|if
condition|(
name|ASTConverter
operator|.
name|isFlat
argument_list|(
name|call
argument_list|)
condition|)
block|{
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|tmpExprArgs
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|tmpExprArgs
operator|.
name|addAll
argument_list|(
name|args
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|gfDesc
operator|=
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|TypeConverter
operator|.
name|convert
argument_list|(
name|call
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|,
name|SqlFunctionConverter
operator|.
name|getHiveUDF
argument_list|(
name|call
operator|.
name|getOperator
argument_list|()
argument_list|,
name|call
operator|.
name|getType
argument_list|()
argument_list|,
literal|2
argument_list|)
argument_list|,
name|tmpExprArgs
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|2
init|;
name|i
operator|<
name|call
operator|.
name|operands
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|tmpExprArgs
operator|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
expr_stmt|;
name|tmpExprArgs
operator|.
name|add
argument_list|(
name|gfDesc
argument_list|)
expr_stmt|;
name|tmpExprArgs
operator|.
name|add
argument_list|(
name|args
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|gfDesc
operator|=
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|TypeConverter
operator|.
name|convert
argument_list|(
name|call
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|,
name|SqlFunctionConverter
operator|.
name|getHiveUDF
argument_list|(
name|call
operator|.
name|getOperator
argument_list|()
argument_list|,
name|call
operator|.
name|getType
argument_list|()
argument_list|,
literal|2
argument_list|)
argument_list|,
name|tmpExprArgs
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|GenericUDF
name|hiveUdf
init|=
name|SqlFunctionConverter
operator|.
name|getHiveUDF
argument_list|(
name|call
operator|.
name|getOperator
argument_list|()
argument_list|,
name|call
operator|.
name|getType
argument_list|()
argument_list|,
name|args
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|hiveUdf
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot find UDF for "
operator|+
name|call
operator|.
name|getType
argument_list|()
operator|+
literal|" "
operator|+
name|call
operator|.
name|getOperator
argument_list|()
operator|+
literal|"["
operator|+
name|call
operator|.
name|getOperator
argument_list|()
operator|.
name|getKind
argument_list|()
operator|+
literal|"]/"
operator|+
name|args
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
name|gfDesc
operator|=
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|TypeConverter
operator|.
name|convert
argument_list|(
name|call
operator|.
name|getType
argument_list|()
argument_list|)
argument_list|,
name|hiveUdf
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
return|return
name|gfDesc
return|;
block|}
comment|/**    * TODO: 1. Handle NULL    */
annotation|@
name|Override
specifier|public
name|ExprNodeDesc
name|visitLiteral
parameter_list|(
name|RexLiteral
name|literal
parameter_list|)
block|{
name|RelDataType
name|lType
init|=
name|literal
operator|.
name|getType
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|literal
operator|.
name|getType
argument_list|()
operator|.
name|getSqlTypeName
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
return|return
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|,
name|Boolean
operator|.
name|valueOf
argument_list|(
name|RexLiteral
operator|.
name|booleanValue
argument_list|(
name|literal
argument_list|)
argument_list|)
argument_list|)
return|;
case|case
name|TINYINT
case|:
return|return
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|byteTypeInfo
argument_list|,
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|literal
operator|.
name|getValue3
argument_list|()
operator|)
operator|.
name|byteValue
argument_list|()
argument_list|)
argument_list|)
return|;
case|case
name|SMALLINT
case|:
return|return
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|shortTypeInfo
argument_list|,
name|Short
operator|.
name|valueOf
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|literal
operator|.
name|getValue3
argument_list|()
operator|)
operator|.
name|shortValue
argument_list|()
argument_list|)
argument_list|)
return|;
case|case
name|INTEGER
case|:
return|return
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|literal
operator|.
name|getValue3
argument_list|()
operator|)
operator|.
name|intValue
argument_list|()
argument_list|)
argument_list|)
return|;
case|case
name|BIGINT
case|:
return|return
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|literal
operator|.
name|getValue3
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
argument_list|)
argument_list|)
return|;
case|case
name|FLOAT
case|:
return|return
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|floatTypeInfo
argument_list|,
name|Float
operator|.
name|valueOf
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|literal
operator|.
name|getValue3
argument_list|()
operator|)
operator|.
name|floatValue
argument_list|()
argument_list|)
argument_list|)
return|;
case|case
name|DOUBLE
case|:
return|return
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
argument_list|,
name|Double
operator|.
name|valueOf
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|literal
operator|.
name|getValue3
argument_list|()
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
argument_list|)
return|;
case|case
name|DATE
case|:
return|return
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|dateTypeInfo
argument_list|,
operator|new
name|Date
argument_list|(
operator|(
operator|(
name|Calendar
operator|)
name|literal
operator|.
name|getValue
argument_list|()
operator|)
operator|.
name|getTimeInMillis
argument_list|()
argument_list|)
argument_list|)
return|;
case|case
name|TIMESTAMP
case|:
block|{
name|Object
name|value
init|=
name|literal
operator|.
name|getValue3
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|Long
condition|)
block|{
name|value
operator|=
operator|new
name|Timestamp
argument_list|(
operator|(
name|Long
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|timestampTypeInfo
argument_list|,
name|value
argument_list|)
return|;
block|}
case|case
name|BINARY
case|:
return|return
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|binaryTypeInfo
argument_list|,
name|literal
operator|.
name|getValue3
argument_list|()
argument_list|)
return|;
case|case
name|DECIMAL
case|:
return|return
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
name|lType
operator|.
name|getPrecision
argument_list|()
argument_list|,
name|lType
operator|.
name|getScale
argument_list|()
argument_list|)
argument_list|,
name|literal
operator|.
name|getValue3
argument_list|()
argument_list|)
return|;
case|case
name|VARCHAR
case|:
return|return
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|getVarcharTypeInfo
argument_list|(
name|lType
operator|.
name|getPrecision
argument_list|()
argument_list|)
argument_list|,
operator|new
name|HiveVarchar
argument_list|(
operator|(
name|String
operator|)
name|literal
operator|.
name|getValue3
argument_list|()
argument_list|,
name|lType
operator|.
name|getPrecision
argument_list|()
argument_list|)
argument_list|)
return|;
case|case
name|CHAR
case|:
return|return
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|getCharTypeInfo
argument_list|(
name|lType
operator|.
name|getPrecision
argument_list|()
argument_list|)
argument_list|,
operator|new
name|HiveChar
argument_list|(
operator|(
name|String
operator|)
name|literal
operator|.
name|getValue3
argument_list|()
argument_list|,
name|lType
operator|.
name|getPrecision
argument_list|()
argument_list|)
argument_list|)
return|;
case|case
name|OTHER
case|:
default|default:
return|return
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|voidTypeInfo
argument_list|,
name|literal
operator|.
name|getValue3
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

