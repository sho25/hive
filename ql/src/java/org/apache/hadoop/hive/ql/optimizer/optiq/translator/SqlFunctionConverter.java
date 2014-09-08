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
name|optiq
operator|.
name|translator
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Annotation
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|Description
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
name|FunctionInfo
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
name|FunctionRegistry
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
name|ASTNode
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
name|HiveParser
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
name|ParseDriver
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
name|ql
operator|.
name|udf
operator|.
name|generic
operator|.
name|GenericUDFBridge
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
name|GenericUDFOPNegative
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
name|GenericUDFOPPositive
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
name|eigenbase
operator|.
name|reltype
operator|.
name|RelDataType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|reltype
operator|.
name|RelDataTypeFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
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
name|eigenbase
operator|.
name|sql
operator|.
name|SqlFunction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
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
name|eigenbase
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
name|eigenbase
operator|.
name|sql
operator|.
name|SqlOperator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
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
name|eigenbase
operator|.
name|sql
operator|.
name|type
operator|.
name|InferTypes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|sql
operator|.
name|type
operator|.
name|OperandTypes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
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
name|eigenbase
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
name|eigenbase
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
name|eigenbase
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
name|eigenbase
operator|.
name|sql
operator|.
name|type
operator|.
name|SqlTypeFamily
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eigenbase
operator|.
name|util
operator|.
name|Util
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
name|ImmutableMap
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
name|Maps
import|;
end_import

begin_class
specifier|public
class|class
name|SqlFunctionConverter
block|{
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|SqlOperator
argument_list|>
name|hiveToOptiq
decl_stmt|;
specifier|static
specifier|final
name|Map
argument_list|<
name|SqlOperator
argument_list|,
name|HiveToken
argument_list|>
name|optiqToHiveToken
decl_stmt|;
specifier|static
specifier|final
name|Map
argument_list|<
name|SqlOperator
argument_list|,
name|String
argument_list|>
name|reverseOperatorMap
decl_stmt|;
static|static
block|{
name|Builder
name|builder
init|=
operator|new
name|Builder
argument_list|()
decl_stmt|;
name|hiveToOptiq
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|builder
operator|.
name|hiveToOptiq
argument_list|)
expr_stmt|;
name|optiqToHiveToken
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|builder
operator|.
name|optiqToHiveToken
argument_list|)
expr_stmt|;
name|reverseOperatorMap
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|builder
operator|.
name|reverseOperatorMap
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|SqlOperator
name|getOptiqOperator
parameter_list|(
name|GenericUDF
name|hiveUDF
parameter_list|,
name|ImmutableList
argument_list|<
name|RelDataType
argument_list|>
name|optiqArgTypes
parameter_list|,
name|RelDataType
name|retType
parameter_list|)
block|{
comment|// handle overloaded methods first
if|if
condition|(
name|hiveUDF
operator|instanceof
name|GenericUDFOPNegative
condition|)
block|{
return|return
name|SqlStdOperatorTable
operator|.
name|UNARY_MINUS
return|;
block|}
elseif|else
if|if
condition|(
name|hiveUDF
operator|instanceof
name|GenericUDFOPPositive
condition|)
block|{
return|return
name|SqlStdOperatorTable
operator|.
name|UNARY_PLUS
return|;
block|}
comment|// do genric lookup
return|return
name|getOptiqFn
argument_list|(
name|getName
argument_list|(
name|hiveUDF
argument_list|)
argument_list|,
name|optiqArgTypes
argument_list|,
name|retType
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|GenericUDF
name|getHiveUDF
parameter_list|(
name|SqlOperator
name|op
parameter_list|,
name|RelDataType
name|dt
parameter_list|)
block|{
name|String
name|name
init|=
name|reverseOperatorMap
operator|.
name|get
argument_list|(
name|op
argument_list|)
decl_stmt|;
if|if
condition|(
name|name
operator|==
literal|null
condition|)
name|name
operator|=
name|op
operator|.
name|getName
argument_list|()
expr_stmt|;
name|FunctionInfo
name|hFn
init|=
name|name
operator|!=
literal|null
condition|?
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
name|name
argument_list|)
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|hFn
operator|==
literal|null
condition|)
name|hFn
operator|=
name|handleExplicitCast
argument_list|(
name|op
argument_list|,
name|dt
argument_list|)
expr_stmt|;
return|return
name|hFn
operator|==
literal|null
condition|?
literal|null
else|:
name|hFn
operator|.
name|getGenericUDF
argument_list|()
return|;
block|}
specifier|private
specifier|static
name|FunctionInfo
name|handleExplicitCast
parameter_list|(
name|SqlOperator
name|op
parameter_list|,
name|RelDataType
name|dt
parameter_list|)
block|{
name|FunctionInfo
name|castUDF
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|op
operator|.
name|kind
operator|==
name|SqlKind
operator|.
name|CAST
condition|)
block|{
name|TypeInfo
name|castType
init|=
name|TypeConverter
operator|.
name|convert
argument_list|(
name|dt
argument_list|)
decl_stmt|;
if|if
condition|(
name|castType
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|byteTypeInfo
argument_list|)
condition|)
block|{
name|castUDF
operator|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
literal|"tinyint"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|castType
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|charTypeInfo
argument_list|)
condition|)
block|{
name|castUDF
operator|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
literal|"char"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|castType
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|varcharTypeInfo
argument_list|)
condition|)
block|{
name|castUDF
operator|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
literal|"varchar"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|castType
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
condition|)
block|{
name|castUDF
operator|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
literal|"string"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|castType
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|booleanTypeInfo
argument_list|)
condition|)
block|{
name|castUDF
operator|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
literal|"boolean"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|castType
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|shortTypeInfo
argument_list|)
condition|)
block|{
name|castUDF
operator|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
literal|"smallint"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|castType
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|intTypeInfo
argument_list|)
condition|)
block|{
name|castUDF
operator|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
literal|"int"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|castType
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|longTypeInfo
argument_list|)
condition|)
block|{
name|castUDF
operator|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
literal|"bigint"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|castType
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|floatTypeInfo
argument_list|)
condition|)
block|{
name|castUDF
operator|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
literal|"float"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|castType
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
argument_list|)
condition|)
block|{
name|castUDF
operator|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
literal|"double"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|castType
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|timestampTypeInfo
argument_list|)
condition|)
block|{
name|castUDF
operator|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
literal|"timestamp"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|castType
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|dateTypeInfo
argument_list|)
condition|)
block|{
name|castUDF
operator|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
literal|"datetime"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|castType
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|decimalTypeInfo
argument_list|)
condition|)
block|{
name|castUDF
operator|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
literal|"decimal"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|castType
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|binaryTypeInfo
argument_list|)
condition|)
block|{
name|castUDF
operator|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
literal|"binary"
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|castUDF
return|;
block|}
comment|// TODO: 1) handle Agg Func Name translation 2) is it correct to add func args
comment|// as child of func?
specifier|public
specifier|static
name|ASTNode
name|buildAST
parameter_list|(
name|SqlOperator
name|op
parameter_list|,
name|List
argument_list|<
name|ASTNode
argument_list|>
name|children
parameter_list|)
block|{
name|HiveToken
name|hToken
init|=
name|optiqToHiveToken
operator|.
name|get
argument_list|(
name|op
argument_list|)
decl_stmt|;
name|ASTNode
name|node
decl_stmt|;
if|if
condition|(
name|hToken
operator|!=
literal|null
condition|)
block|{
name|node
operator|=
operator|(
name|ASTNode
operator|)
name|ParseDriver
operator|.
name|adaptor
operator|.
name|create
argument_list|(
name|hToken
operator|.
name|type
argument_list|,
name|hToken
operator|.
name|text
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|node
operator|=
operator|(
name|ASTNode
operator|)
name|ParseDriver
operator|.
name|adaptor
operator|.
name|create
argument_list|(
name|HiveParser
operator|.
name|TOK_FUNCTION
argument_list|,
literal|"TOK_FUNCTION"
argument_list|)
expr_stmt|;
if|if
condition|(
name|op
operator|.
name|kind
operator|!=
name|SqlKind
operator|.
name|CAST
condition|)
block|{
if|if
condition|(
name|op
operator|.
name|kind
operator|==
name|SqlKind
operator|.
name|MINUS_PREFIX
condition|)
block|{
name|node
operator|=
operator|(
name|ASTNode
operator|)
name|ParseDriver
operator|.
name|adaptor
operator|.
name|create
argument_list|(
name|HiveParser
operator|.
name|MINUS
argument_list|,
literal|"MINUS"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|op
operator|.
name|kind
operator|==
name|SqlKind
operator|.
name|PLUS_PREFIX
condition|)
block|{
name|node
operator|=
operator|(
name|ASTNode
operator|)
name|ParseDriver
operator|.
name|adaptor
operator|.
name|create
argument_list|(
name|HiveParser
operator|.
name|PLUS
argument_list|,
literal|"PLUS"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|node
operator|.
name|addChild
argument_list|(
operator|(
name|ASTNode
operator|)
name|ParseDriver
operator|.
name|adaptor
operator|.
name|create
argument_list|(
name|HiveParser
operator|.
name|Identifier
argument_list|,
name|op
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|ASTNode
name|c
range|:
name|children
control|)
block|{
name|ParseDriver
operator|.
name|adaptor
operator|.
name|addChild
argument_list|(
name|node
argument_list|,
name|c
argument_list|)
expr_stmt|;
block|}
return|return
name|node
return|;
block|}
comment|/**    * Build AST for flattened Associative expressions ('and', 'or'). Flattened    * expressions is of the form or[x,y,z] which is originally represented as    * "or[x, or[y, z]]".    */
specifier|public
specifier|static
name|ASTNode
name|buildAST
parameter_list|(
name|SqlOperator
name|op
parameter_list|,
name|List
argument_list|<
name|ASTNode
argument_list|>
name|children
parameter_list|,
name|int
name|i
parameter_list|)
block|{
if|if
condition|(
name|i
operator|+
literal|1
operator|<
name|children
operator|.
name|size
argument_list|()
condition|)
block|{
name|HiveToken
name|hToken
init|=
name|optiqToHiveToken
operator|.
name|get
argument_list|(
name|op
argument_list|)
decl_stmt|;
name|ASTNode
name|curNode
init|=
operator|(
operator|(
name|ASTNode
operator|)
name|ParseDriver
operator|.
name|adaptor
operator|.
name|create
argument_list|(
name|hToken
operator|.
name|type
argument_list|,
name|hToken
operator|.
name|text
argument_list|)
operator|)
decl_stmt|;
name|ParseDriver
operator|.
name|adaptor
operator|.
name|addChild
argument_list|(
name|curNode
argument_list|,
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|ParseDriver
operator|.
name|adaptor
operator|.
name|addChild
argument_list|(
name|curNode
argument_list|,
name|buildAST
argument_list|(
name|op
argument_list|,
name|children
argument_list|,
name|i
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|curNode
return|;
block|}
else|else
block|{
return|return
name|children
operator|.
name|get
argument_list|(
name|i
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
name|String
name|getName
parameter_list|(
name|GenericUDF
name|hiveUDF
parameter_list|)
block|{
name|String
name|udfName
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|hiveUDF
operator|instanceof
name|GenericUDFBridge
condition|)
block|{
name|udfName
operator|=
operator|(
operator|(
name|GenericUDFBridge
operator|)
name|hiveUDF
operator|)
operator|.
name|getUdfName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|Class
argument_list|<
name|?
extends|extends
name|GenericUDF
argument_list|>
name|udfClass
init|=
name|hiveUDF
operator|.
name|getClass
argument_list|()
decl_stmt|;
name|Annotation
name|udfAnnotation
init|=
name|udfClass
operator|.
name|getAnnotation
argument_list|(
name|Description
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|udfAnnotation
operator|!=
literal|null
operator|&&
name|udfAnnotation
operator|instanceof
name|Description
condition|)
block|{
name|Description
name|udfDescription
init|=
operator|(
name|Description
operator|)
name|udfAnnotation
decl_stmt|;
name|udfName
operator|=
name|udfDescription
operator|.
name|name
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|udfName
operator|==
literal|null
operator|||
name|udfName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|udfName
operator|=
name|hiveUDF
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
expr_stmt|;
name|int
name|indx
init|=
name|udfName
operator|.
name|lastIndexOf
argument_list|(
literal|"."
argument_list|)
decl_stmt|;
if|if
condition|(
name|indx
operator|>=
literal|0
condition|)
block|{
name|indx
operator|+=
literal|1
expr_stmt|;
name|udfName
operator|=
name|udfName
operator|.
name|substring
argument_list|(
name|indx
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|udfName
return|;
block|}
specifier|private
specifier|static
class|class
name|Builder
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|SqlOperator
argument_list|>
name|hiveToOptiq
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|SqlOperator
argument_list|,
name|HiveToken
argument_list|>
name|optiqToHiveToken
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|SqlOperator
argument_list|,
name|String
argument_list|>
name|reverseOperatorMap
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|Builder
parameter_list|()
block|{
name|registerFunction
argument_list|(
literal|"+"
argument_list|,
name|SqlStdOperatorTable
operator|.
name|PLUS
argument_list|,
name|hToken
argument_list|(
name|HiveParser
operator|.
name|PLUS
argument_list|,
literal|"+"
argument_list|)
argument_list|)
expr_stmt|;
name|registerFunction
argument_list|(
literal|"-"
argument_list|,
name|SqlStdOperatorTable
operator|.
name|MINUS
argument_list|,
name|hToken
argument_list|(
name|HiveParser
operator|.
name|MINUS
argument_list|,
literal|"-"
argument_list|)
argument_list|)
expr_stmt|;
name|registerFunction
argument_list|(
literal|"*"
argument_list|,
name|SqlStdOperatorTable
operator|.
name|MULTIPLY
argument_list|,
name|hToken
argument_list|(
name|HiveParser
operator|.
name|STAR
argument_list|,
literal|"*"
argument_list|)
argument_list|)
expr_stmt|;
name|registerFunction
argument_list|(
literal|"/"
argument_list|,
name|SqlStdOperatorTable
operator|.
name|DIVIDE
argument_list|,
name|hToken
argument_list|(
name|HiveParser
operator|.
name|STAR
argument_list|,
literal|"/"
argument_list|)
argument_list|)
expr_stmt|;
name|registerFunction
argument_list|(
literal|"%"
argument_list|,
name|SqlStdOperatorTable
operator|.
name|MOD
argument_list|,
name|hToken
argument_list|(
name|HiveParser
operator|.
name|STAR
argument_list|,
literal|"%"
argument_list|)
argument_list|)
expr_stmt|;
name|registerFunction
argument_list|(
literal|"and"
argument_list|,
name|SqlStdOperatorTable
operator|.
name|AND
argument_list|,
name|hToken
argument_list|(
name|HiveParser
operator|.
name|KW_AND
argument_list|,
literal|"and"
argument_list|)
argument_list|)
expr_stmt|;
name|registerFunction
argument_list|(
literal|"or"
argument_list|,
name|SqlStdOperatorTable
operator|.
name|OR
argument_list|,
name|hToken
argument_list|(
name|HiveParser
operator|.
name|KW_OR
argument_list|,
literal|"or"
argument_list|)
argument_list|)
expr_stmt|;
name|registerFunction
argument_list|(
literal|"="
argument_list|,
name|SqlStdOperatorTable
operator|.
name|EQUALS
argument_list|,
name|hToken
argument_list|(
name|HiveParser
operator|.
name|EQUAL
argument_list|,
literal|"="
argument_list|)
argument_list|)
expr_stmt|;
name|registerFunction
argument_list|(
literal|"<"
argument_list|,
name|SqlStdOperatorTable
operator|.
name|LESS_THAN
argument_list|,
name|hToken
argument_list|(
name|HiveParser
operator|.
name|LESSTHAN
argument_list|,
literal|"<"
argument_list|)
argument_list|)
expr_stmt|;
name|registerFunction
argument_list|(
literal|"<="
argument_list|,
name|SqlStdOperatorTable
operator|.
name|LESS_THAN_OR_EQUAL
argument_list|,
name|hToken
argument_list|(
name|HiveParser
operator|.
name|LESSTHANOREQUALTO
argument_list|,
literal|"<="
argument_list|)
argument_list|)
expr_stmt|;
name|registerFunction
argument_list|(
literal|">"
argument_list|,
name|SqlStdOperatorTable
operator|.
name|GREATER_THAN
argument_list|,
name|hToken
argument_list|(
name|HiveParser
operator|.
name|GREATERTHAN
argument_list|,
literal|">"
argument_list|)
argument_list|)
expr_stmt|;
name|registerFunction
argument_list|(
literal|">="
argument_list|,
name|SqlStdOperatorTable
operator|.
name|GREATER_THAN_OR_EQUAL
argument_list|,
name|hToken
argument_list|(
name|HiveParser
operator|.
name|GREATERTHANOREQUALTO
argument_list|,
literal|">="
argument_list|)
argument_list|)
expr_stmt|;
name|registerFunction
argument_list|(
literal|"!"
argument_list|,
name|SqlStdOperatorTable
operator|.
name|NOT
argument_list|,
name|hToken
argument_list|(
name|HiveParser
operator|.
name|KW_NOT
argument_list|,
literal|"not"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|registerFunction
parameter_list|(
name|String
name|name
parameter_list|,
name|SqlOperator
name|optiqFn
parameter_list|,
name|HiveToken
name|hiveToken
parameter_list|)
block|{
name|reverseOperatorMap
operator|.
name|put
argument_list|(
name|optiqFn
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|FunctionInfo
name|hFn
init|=
name|FunctionRegistry
operator|.
name|getFunctionInfo
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|hFn
operator|!=
literal|null
condition|)
block|{
name|String
name|hFnName
init|=
name|getName
argument_list|(
name|hFn
operator|.
name|getGenericUDF
argument_list|()
argument_list|)
decl_stmt|;
name|hiveToOptiq
operator|.
name|put
argument_list|(
name|hFnName
argument_list|,
name|optiqFn
argument_list|)
expr_stmt|;
if|if
condition|(
name|hiveToken
operator|!=
literal|null
condition|)
block|{
name|optiqToHiveToken
operator|.
name|put
argument_list|(
name|optiqFn
argument_list|,
name|hiveToken
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
specifier|static
name|HiveToken
name|hToken
parameter_list|(
name|int
name|type
parameter_list|,
name|String
name|text
parameter_list|)
block|{
return|return
operator|new
name|HiveToken
argument_list|(
name|type
argument_list|,
name|text
argument_list|)
return|;
block|}
specifier|public
specifier|static
class|class
name|OptiqUDAF
extends|extends
name|SqlAggFunction
block|{
specifier|final
name|ImmutableList
argument_list|<
name|RelDataType
argument_list|>
name|m_argTypes
decl_stmt|;
specifier|final
name|RelDataType
name|m_retType
decl_stmt|;
specifier|public
name|OptiqUDAF
parameter_list|(
name|String
name|opName
parameter_list|,
name|SqlReturnTypeInference
name|returnTypeInference
parameter_list|,
name|SqlOperandTypeInference
name|operandTypeInference
parameter_list|,
name|SqlOperandTypeChecker
name|operandTypeChecker
parameter_list|,
name|ImmutableList
argument_list|<
name|RelDataType
argument_list|>
name|argTypes
parameter_list|,
name|RelDataType
name|retType
parameter_list|)
block|{
name|super
argument_list|(
name|opName
argument_list|,
name|SqlKind
operator|.
name|OTHER_FUNCTION
argument_list|,
name|returnTypeInference
argument_list|,
name|operandTypeInference
argument_list|,
name|operandTypeChecker
argument_list|,
name|SqlFunctionCategory
operator|.
name|USER_DEFINED_FUNCTION
argument_list|)
expr_stmt|;
name|m_argTypes
operator|=
name|argTypes
expr_stmt|;
name|m_retType
operator|=
name|retType
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|RelDataType
argument_list|>
name|getParameterTypes
parameter_list|(
specifier|final
name|RelDataTypeFactory
name|typeFactory
parameter_list|)
block|{
return|return
name|m_argTypes
return|;
block|}
annotation|@
name|Override
specifier|public
name|RelDataType
name|getReturnType
parameter_list|(
specifier|final
name|RelDataTypeFactory
name|typeFactory
parameter_list|)
block|{
return|return
name|m_retType
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|OptiqUDFInfo
block|{
specifier|private
name|String
name|m_udfName
decl_stmt|;
specifier|private
name|SqlReturnTypeInference
name|m_returnTypeInference
decl_stmt|;
specifier|private
name|SqlOperandTypeInference
name|m_operandTypeInference
decl_stmt|;
specifier|private
name|SqlOperandTypeChecker
name|m_operandTypeChecker
decl_stmt|;
specifier|private
name|ImmutableList
argument_list|<
name|RelDataType
argument_list|>
name|m_argTypes
decl_stmt|;
specifier|private
name|RelDataType
name|m_retType
decl_stmt|;
block|}
specifier|private
specifier|static
name|OptiqUDFInfo
name|getUDFInfo
parameter_list|(
name|String
name|hiveUdfName
parameter_list|,
name|ImmutableList
argument_list|<
name|RelDataType
argument_list|>
name|optiqArgTypes
parameter_list|,
name|RelDataType
name|optiqRetType
parameter_list|)
block|{
name|OptiqUDFInfo
name|udfInfo
init|=
operator|new
name|OptiqUDFInfo
argument_list|()
decl_stmt|;
name|udfInfo
operator|.
name|m_udfName
operator|=
name|hiveUdfName
expr_stmt|;
name|udfInfo
operator|.
name|m_returnTypeInference
operator|=
name|ReturnTypes
operator|.
name|explicit
argument_list|(
name|optiqRetType
argument_list|)
expr_stmt|;
name|udfInfo
operator|.
name|m_operandTypeInference
operator|=
name|InferTypes
operator|.
name|explicit
argument_list|(
name|optiqArgTypes
argument_list|)
expr_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|SqlTypeFamily
argument_list|>
name|typeFamilyBuilder
init|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|SqlTypeFamily
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|RelDataType
name|at
range|:
name|optiqArgTypes
control|)
block|{
name|typeFamilyBuilder
operator|.
name|add
argument_list|(
name|Util
operator|.
name|first
argument_list|(
name|at
operator|.
name|getSqlTypeName
argument_list|()
operator|.
name|getFamily
argument_list|()
argument_list|,
name|SqlTypeFamily
operator|.
name|ANY
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|udfInfo
operator|.
name|m_operandTypeChecker
operator|=
name|OperandTypes
operator|.
name|family
argument_list|(
name|typeFamilyBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|udfInfo
operator|.
name|m_argTypes
operator|=
name|ImmutableList
operator|.
expr|<
name|RelDataType
operator|>
name|copyOf
argument_list|(
name|optiqArgTypes
argument_list|)
expr_stmt|;
name|udfInfo
operator|.
name|m_retType
operator|=
name|optiqRetType
expr_stmt|;
return|return
name|udfInfo
return|;
block|}
specifier|public
specifier|static
name|SqlOperator
name|getOptiqFn
parameter_list|(
name|String
name|hiveUdfName
parameter_list|,
name|ImmutableList
argument_list|<
name|RelDataType
argument_list|>
name|optiqArgTypes
parameter_list|,
name|RelDataType
name|optiqRetType
parameter_list|)
block|{
name|SqlOperator
name|optiqOp
init|=
name|hiveToOptiq
operator|.
name|get
argument_list|(
name|hiveUdfName
argument_list|)
decl_stmt|;
if|if
condition|(
name|optiqOp
operator|==
literal|null
condition|)
block|{
name|OptiqUDFInfo
name|uInf
init|=
name|getUDFInfo
argument_list|(
name|hiveUdfName
argument_list|,
name|optiqArgTypes
argument_list|,
name|optiqRetType
argument_list|)
decl_stmt|;
name|optiqOp
operator|=
operator|new
name|SqlFunction
argument_list|(
name|uInf
operator|.
name|m_udfName
argument_list|,
name|SqlKind
operator|.
name|OTHER_FUNCTION
argument_list|,
name|uInf
operator|.
name|m_returnTypeInference
argument_list|,
name|uInf
operator|.
name|m_operandTypeInference
argument_list|,
name|uInf
operator|.
name|m_operandTypeChecker
argument_list|,
name|SqlFunctionCategory
operator|.
name|USER_DEFINED_FUNCTION
argument_list|)
expr_stmt|;
block|}
return|return
name|optiqOp
return|;
block|}
specifier|public
specifier|static
name|SqlAggFunction
name|getOptiqAggFn
parameter_list|(
name|String
name|hiveUdfName
parameter_list|,
name|ImmutableList
argument_list|<
name|RelDataType
argument_list|>
name|optiqArgTypes
parameter_list|,
name|RelDataType
name|optiqRetType
parameter_list|)
block|{
name|SqlAggFunction
name|optiqAggFn
init|=
operator|(
name|SqlAggFunction
operator|)
name|hiveToOptiq
operator|.
name|get
argument_list|(
name|hiveUdfName
argument_list|)
decl_stmt|;
if|if
condition|(
name|optiqAggFn
operator|==
literal|null
condition|)
block|{
name|OptiqUDFInfo
name|uInf
init|=
name|getUDFInfo
argument_list|(
name|hiveUdfName
argument_list|,
name|optiqArgTypes
argument_list|,
name|optiqRetType
argument_list|)
decl_stmt|;
name|optiqAggFn
operator|=
operator|new
name|OptiqUDAF
argument_list|(
name|uInf
operator|.
name|m_udfName
argument_list|,
name|uInf
operator|.
name|m_returnTypeInference
argument_list|,
name|uInf
operator|.
name|m_operandTypeInference
argument_list|,
name|uInf
operator|.
name|m_operandTypeChecker
argument_list|,
name|uInf
operator|.
name|m_argTypes
argument_list|,
name|uInf
operator|.
name|m_retType
argument_list|)
expr_stmt|;
block|}
return|return
name|optiqAggFn
return|;
block|}
specifier|static
class|class
name|HiveToken
block|{
name|int
name|type
decl_stmt|;
name|String
name|text
decl_stmt|;
name|String
index|[]
name|args
decl_stmt|;
name|HiveToken
parameter_list|(
name|int
name|type
parameter_list|,
name|String
name|text
parameter_list|,
name|String
modifier|...
name|args
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|text
operator|=
name|text
expr_stmt|;
name|this
operator|.
name|args
operator|=
name|args
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

