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
name|exec
operator|.
name|vector
operator|.
name|expressions
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
name|DataTypePhysicalVariation
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
name|HiveDecimal
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
name|ExprNodeEvaluator
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
name|ExprNodeEvaluatorFactory
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
name|exec
operator|.
name|vector
operator|.
name|VectorExtractRow
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
name|VectorRandomBatchSource
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
name|VectorRandomRowSource
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
name|VectorizationContext
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
name|VectorizedRowBatch
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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|VectorRandomRowSource
operator|.
name|GenerationSpec
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
name|expressions
operator|.
name|VectorExpression
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
name|udf
operator|.
name|VectorUDFAdaptor
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
name|HiveException
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
name|session
operator|.
name|SessionState
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|objectinspector
operator|.
name|ObjectInspectorUtils
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
name|objectinspector
operator|.
name|ObjectInspectorUtils
operator|.
name|ObjectInspectorCopyOption
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
name|objectinspector
operator|.
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
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
name|DecimalTypeInfo
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
name|PrimitiveTypeInfo
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
name|TypeInfoUtils
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestVectorNegative
block|{
specifier|public
name|TestVectorNegative
parameter_list|()
block|{
comment|// Arithmetic operations rely on getting conf from SessionState, need to initialize here.
name|SessionState
name|ss
init|=
operator|new
name|SessionState
argument_list|(
operator|new
name|HiveConf
argument_list|()
argument_list|)
decl_stmt|;
name|ss
operator|.
name|getConf
argument_list|()
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_COMPAT
argument_list|,
literal|"latest"
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|setCurrentSessionState
argument_list|(
name|ss
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInteger
parameter_list|()
throws|throws
name|Exception
block|{
name|Random
name|random
init|=
operator|new
name|Random
argument_list|(
literal|7743
argument_list|)
decl_stmt|;
name|doIntegerTests
argument_list|(
name|random
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFloating
parameter_list|()
throws|throws
name|Exception
block|{
name|Random
name|random
init|=
operator|new
name|Random
argument_list|(
literal|7743
argument_list|)
decl_stmt|;
name|doFloatingTests
argument_list|(
name|random
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDecimal
parameter_list|()
throws|throws
name|Exception
block|{
name|Random
name|random
init|=
operator|new
name|Random
argument_list|(
literal|7743
argument_list|)
decl_stmt|;
name|doDecimalTests
argument_list|(
name|random
argument_list|)
expr_stmt|;
block|}
specifier|public
enum|enum
name|NegativeTestMode
block|{
name|ROW_MODE
block|,
name|ADAPTOR
block|,
name|VECTOR_EXPRESSION
block|;
specifier|static
specifier|final
name|int
name|count
init|=
name|values
argument_list|()
operator|.
name|length
decl_stmt|;
block|}
specifier|private
specifier|static
name|TypeInfo
index|[]
name|integerTypeInfos
init|=
operator|new
name|TypeInfo
index|[]
block|{
name|TypeInfoFactory
operator|.
name|byteTypeInfo
block|,
name|TypeInfoFactory
operator|.
name|shortTypeInfo
block|,
name|TypeInfoFactory
operator|.
name|intTypeInfo
block|,
name|TypeInfoFactory
operator|.
name|longTypeInfo
block|}
decl_stmt|;
specifier|private
specifier|static
name|TypeInfo
index|[]
name|floatingTypeInfos
init|=
operator|new
name|TypeInfo
index|[]
block|{
name|TypeInfoFactory
operator|.
name|floatTypeInfo
block|,
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
block|}
decl_stmt|;
specifier|private
name|void
name|doIntegerTests
parameter_list|(
name|Random
name|random
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|TypeInfo
name|typeInfo
range|:
name|integerTypeInfos
control|)
block|{
name|doTests
argument_list|(
name|random
argument_list|,
name|typeInfo
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|doIntegerFloatingTests
parameter_list|(
name|Random
name|random
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|TypeInfo
name|typeInfo
range|:
name|integerTypeInfos
control|)
block|{
name|doTests
argument_list|(
name|random
argument_list|,
name|typeInfo
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|doFloatingTests
parameter_list|(
name|Random
name|random
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|TypeInfo
name|typeInfo
range|:
name|floatingTypeInfos
control|)
block|{
name|doTests
argument_list|(
name|random
argument_list|,
name|typeInfo
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|TypeInfo
index|[]
name|decimalTypeInfos
init|=
operator|new
name|TypeInfo
index|[]
block|{
operator|new
name|DecimalTypeInfo
argument_list|(
literal|38
argument_list|,
literal|18
argument_list|)
block|,
operator|new
name|DecimalTypeInfo
argument_list|(
literal|25
argument_list|,
literal|2
argument_list|)
block|,
operator|new
name|DecimalTypeInfo
argument_list|(
literal|19
argument_list|,
literal|4
argument_list|)
block|,
operator|new
name|DecimalTypeInfo
argument_list|(
literal|18
argument_list|,
literal|10
argument_list|)
block|,
operator|new
name|DecimalTypeInfo
argument_list|(
literal|17
argument_list|,
literal|3
argument_list|)
block|,
operator|new
name|DecimalTypeInfo
argument_list|(
literal|12
argument_list|,
literal|2
argument_list|)
block|,
operator|new
name|DecimalTypeInfo
argument_list|(
literal|7
argument_list|,
literal|1
argument_list|)
block|}
decl_stmt|;
specifier|private
name|void
name|doDecimalTests
parameter_list|(
name|Random
name|random
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|TypeInfo
name|typeInfo
range|:
name|decimalTypeInfos
control|)
block|{
name|doTests
argument_list|(
name|random
argument_list|,
name|typeInfo
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|TypeInfo
name|getOutputTypeInfo
parameter_list|(
name|GenericUDF
name|genericUdfClone
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|objectInspectorList
parameter_list|)
throws|throws
name|HiveException
block|{
name|ObjectInspector
index|[]
name|array
init|=
name|objectInspectorList
operator|.
name|toArray
argument_list|(
operator|new
name|ObjectInspector
index|[
name|objectInspectorList
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|ObjectInspector
name|outputObjectInspector
init|=
name|genericUdfClone
operator|.
name|initialize
argument_list|(
name|array
argument_list|)
decl_stmt|;
return|return
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|outputObjectInspector
argument_list|)
return|;
block|}
specifier|private
name|void
name|doTests
parameter_list|(
name|Random
name|random
parameter_list|,
name|TypeInfo
name|typeInfo
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|typeName
init|=
name|typeInfo
operator|.
name|getTypeName
argument_list|()
decl_stmt|;
name|PrimitiveCategory
name|primitiveCategory1
init|=
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|GenerationSpec
argument_list|>
name|generationSpecList
init|=
operator|new
name|ArrayList
argument_list|<
name|GenerationSpec
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|DataTypePhysicalVariation
argument_list|>
name|explicitDataTypePhysicalVariationList
init|=
operator|new
name|ArrayList
argument_list|<
name|DataTypePhysicalVariation
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columns
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|columnNum
init|=
literal|1
decl_stmt|;
name|generationSpecList
operator|.
name|add
argument_list|(
name|GenerationSpec
operator|.
name|createSameType
argument_list|(
name|typeInfo
argument_list|)
argument_list|)
expr_stmt|;
name|explicitDataTypePhysicalVariationList
operator|.
name|add
argument_list|(
name|DataTypePhysicalVariation
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|ExprNodeDesc
name|col1Expr
decl_stmt|;
name|String
name|columnName
init|=
literal|"col"
operator|+
operator|(
name|columnNum
operator|++
operator|)
decl_stmt|;
name|col1Expr
operator|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|typeInfo
argument_list|,
name|columnName
argument_list|,
literal|"table"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|columns
operator|.
name|add
argument_list|(
name|columnName
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|objectInspectorList
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|objectInspectorList
operator|.
name|add
argument_list|(
name|VectorRandomRowSource
operator|.
name|getObjectInspector
argument_list|(
name|typeInfo
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|children
operator|.
name|add
argument_list|(
name|col1Expr
argument_list|)
expr_stmt|;
comment|//----------------------------------------------------------------------------------------------
name|String
index|[]
name|columnNames
init|=
name|columns
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|VectorRandomRowSource
name|rowSource
init|=
operator|new
name|VectorRandomRowSource
argument_list|()
decl_stmt|;
name|rowSource
operator|.
name|initGenerationSpecSchema
argument_list|(
name|random
argument_list|,
name|generationSpecList
argument_list|,
comment|/* maxComplexDepth */
literal|0
argument_list|,
comment|/* allowNull */
literal|true
argument_list|,
comment|/* isUnicodeOk */
literal|true
argument_list|,
name|explicitDataTypePhysicalVariationList
argument_list|)
expr_stmt|;
name|Object
index|[]
index|[]
name|randomRows
init|=
name|rowSource
operator|.
name|randomRows
argument_list|(
literal|100000
argument_list|)
decl_stmt|;
name|VectorRandomBatchSource
name|batchSource
init|=
name|VectorRandomBatchSource
operator|.
name|createInterestingBatches
argument_list|(
name|random
argument_list|,
name|rowSource
argument_list|,
name|randomRows
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|GenericUDF
name|genericUdf
init|=
operator|new
name|GenericUDFOPNegative
argument_list|()
decl_stmt|;
name|ObjectInspector
index|[]
name|objectInspectors
init|=
name|objectInspectorList
operator|.
name|toArray
argument_list|(
operator|new
name|ObjectInspector
index|[
name|objectInspectorList
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|ObjectInspector
name|outputObjectInspector
init|=
literal|null
decl_stmt|;
try|try
block|{
name|outputObjectInspector
operator|=
name|genericUdf
operator|.
name|initialize
argument_list|(
name|objectInspectors
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|TypeInfo
name|outputTypeInfo
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfoFromObjectInspector
argument_list|(
name|outputObjectInspector
argument_list|)
decl_stmt|;
name|ExprNodeGenericFuncDesc
name|exprDesc
init|=
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|outputTypeInfo
argument_list|,
name|genericUdf
argument_list|,
name|children
argument_list|)
decl_stmt|;
specifier|final
name|int
name|rowCount
init|=
name|randomRows
operator|.
name|length
decl_stmt|;
name|Object
index|[]
index|[]
name|resultObjectsArray
init|=
operator|new
name|Object
index|[
name|NegativeTestMode
operator|.
name|count
index|]
index|[]
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
name|NegativeTestMode
operator|.
name|count
condition|;
name|i
operator|++
control|)
block|{
name|Object
index|[]
name|resultObjects
init|=
operator|new
name|Object
index|[
name|rowCount
index|]
decl_stmt|;
name|resultObjectsArray
index|[
name|i
index|]
operator|=
name|resultObjects
expr_stmt|;
name|NegativeTestMode
name|negativeTestMode
init|=
name|NegativeTestMode
operator|.
name|values
argument_list|()
index|[
name|i
index|]
decl_stmt|;
switch|switch
condition|(
name|negativeTestMode
condition|)
block|{
case|case
name|ROW_MODE
case|:
name|doRowArithmeticTest
argument_list|(
name|typeInfo
argument_list|,
name|columns
argument_list|,
name|children
argument_list|,
name|exprDesc
argument_list|,
name|randomRows
argument_list|,
name|rowSource
operator|.
name|rowStructObjectInspector
argument_list|()
argument_list|,
name|outputTypeInfo
argument_list|,
name|resultObjects
argument_list|)
expr_stmt|;
break|break;
case|case
name|ADAPTOR
case|:
case|case
name|VECTOR_EXPRESSION
case|:
name|doVectorArithmeticTest
argument_list|(
name|typeInfo
argument_list|,
name|columns
argument_list|,
name|columnNames
argument_list|,
name|rowSource
operator|.
name|typeInfos
argument_list|()
argument_list|,
name|rowSource
operator|.
name|dataTypePhysicalVariations
argument_list|()
argument_list|,
name|children
argument_list|,
name|exprDesc
argument_list|,
name|negativeTestMode
argument_list|,
name|batchSource
argument_list|,
name|exprDesc
operator|.
name|getWritableObjectInspector
argument_list|()
argument_list|,
name|outputTypeInfo
argument_list|,
name|resultObjects
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected Negative operator test mode "
operator|+
name|negativeTestMode
argument_list|)
throw|;
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rowCount
condition|;
name|i
operator|++
control|)
block|{
comment|// Row-mode is the expected value.
name|Object
name|expectedResult
init|=
name|resultObjectsArray
index|[
literal|0
index|]
index|[
name|i
index|]
decl_stmt|;
for|for
control|(
name|int
name|v
init|=
literal|1
init|;
name|v
operator|<
name|NegativeTestMode
operator|.
name|count
condition|;
name|v
operator|++
control|)
block|{
name|Object
name|vectorResult
init|=
name|resultObjectsArray
index|[
name|v
index|]
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|expectedResult
operator|==
literal|null
operator|||
name|vectorResult
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|expectedResult
operator|!=
literal|null
operator|||
name|vectorResult
operator|!=
literal|null
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Row "
operator|+
name|i
operator|+
literal|" typeName "
operator|+
name|typeName
operator|+
literal|" outputTypeName "
operator|+
name|outputTypeInfo
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" "
operator|+
name|NegativeTestMode
operator|.
name|values
argument_list|()
index|[
name|v
index|]
operator|+
literal|" result is NULL "
operator|+
operator|(
name|vectorResult
operator|==
literal|null
operator|)
operator|+
literal|" does not match row-mode expected result is NULL "
operator|+
operator|(
name|expectedResult
operator|==
literal|null
operator|)
operator|+
literal|" row values "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|randomRows
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|expectedResult
operator|.
name|equals
argument_list|(
name|vectorResult
argument_list|)
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Row "
operator|+
name|i
operator|+
literal|" typeName "
operator|+
name|typeName
operator|+
literal|" outputTypeName "
operator|+
name|outputTypeInfo
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" "
operator|+
name|NegativeTestMode
operator|.
name|values
argument_list|()
index|[
name|v
index|]
operator|+
literal|" result "
operator|+
name|vectorResult
operator|.
name|toString
argument_list|()
operator|+
literal|" ("
operator|+
name|vectorResult
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|")"
operator|+
literal|" does not match row-mode expected result "
operator|+
name|expectedResult
operator|.
name|toString
argument_list|()
operator|+
literal|" ("
operator|+
name|expectedResult
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|")"
operator|+
literal|" row values "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|randomRows
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
specifier|private
name|void
name|doRowArithmeticTest
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
parameter_list|,
name|ExprNodeGenericFuncDesc
name|exprDesc
parameter_list|,
name|Object
index|[]
index|[]
name|randomRows
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|,
name|Object
index|[]
name|resultObjects
parameter_list|)
throws|throws
name|Exception
block|{
comment|/*     System.out.println(         "*DEBUG* typeInfo " + typeInfo.toString() +         " negativeTestMode ROW_MODE" +         " exprDesc " + exprDesc.toString());     */
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|ExprNodeEvaluator
name|evaluator
init|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|exprDesc
argument_list|,
name|hiveConf
argument_list|)
decl_stmt|;
name|evaluator
operator|.
name|initialize
argument_list|(
name|rowInspector
argument_list|)
expr_stmt|;
name|ObjectInspector
name|objectInspector
init|=
name|TypeInfoUtils
operator|.
name|getStandardWritableObjectInspectorFromTypeInfo
argument_list|(
name|outputTypeInfo
argument_list|)
decl_stmt|;
specifier|final
name|int
name|rowCount
init|=
name|randomRows
operator|.
name|length
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
name|rowCount
condition|;
name|i
operator|++
control|)
block|{
name|Object
index|[]
name|row
init|=
name|randomRows
index|[
name|i
index|]
decl_stmt|;
name|Object
name|result
init|=
name|evaluator
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Object
name|copyResult
init|=
literal|null
decl_stmt|;
try|try
block|{
name|copyResult
operator|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|result
argument_list|,
name|objectInspector
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"here"
argument_list|)
expr_stmt|;
block|}
name|resultObjects
index|[
name|i
index|]
operator|=
name|copyResult
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|extractResultObjects
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|,
name|int
name|rowIndex
parameter_list|,
name|VectorExtractRow
name|resultVectorExtractRow
parameter_list|,
name|Object
index|[]
name|scrqtchRow
parameter_list|,
name|ObjectInspector
name|objectInspector
parameter_list|,
name|Object
index|[]
name|resultObjects
parameter_list|)
block|{
name|boolean
name|selectedInUse
init|=
name|batch
operator|.
name|selectedInUse
decl_stmt|;
name|int
index|[]
name|selected
init|=
name|batch
operator|.
name|selected
decl_stmt|;
for|for
control|(
name|int
name|logicalIndex
init|=
literal|0
init|;
name|logicalIndex
operator|<
name|batch
operator|.
name|size
condition|;
name|logicalIndex
operator|++
control|)
block|{
specifier|final
name|int
name|batchIndex
init|=
operator|(
name|selectedInUse
condition|?
name|selected
index|[
name|logicalIndex
index|]
else|:
name|logicalIndex
operator|)
decl_stmt|;
name|resultVectorExtractRow
operator|.
name|extractRow
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|,
name|scrqtchRow
argument_list|)
expr_stmt|;
name|Object
name|copyResult
init|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|scrqtchRow
index|[
literal|0
index|]
argument_list|,
name|objectInspector
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
decl_stmt|;
name|resultObjects
index|[
name|rowIndex
operator|++
index|]
operator|=
name|copyResult
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|doVectorArithmeticTest
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|,
name|String
index|[]
name|columnNames
parameter_list|,
name|TypeInfo
index|[]
name|typeInfos
parameter_list|,
name|DataTypePhysicalVariation
index|[]
name|dataTypePhysicalVariations
parameter_list|,
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|children
parameter_list|,
name|ExprNodeGenericFuncDesc
name|exprDesc
parameter_list|,
name|NegativeTestMode
name|negativeTestMode
parameter_list|,
name|VectorRandomBatchSource
name|batchSource
parameter_list|,
name|ObjectInspector
name|objectInspector
parameter_list|,
name|TypeInfo
name|outputTypeInfo
parameter_list|,
name|Object
index|[]
name|resultObjects
parameter_list|)
throws|throws
name|Exception
block|{
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
if|if
condition|(
name|negativeTestMode
operator|==
name|NegativeTestMode
operator|.
name|ADAPTOR
condition|)
block|{
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_TEST_VECTOR_ADAPTOR_OVERRIDE
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|VectorizationContext
name|vectorizationContext
init|=
operator|new
name|VectorizationContext
argument_list|(
literal|"name"
argument_list|,
name|columns
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|typeInfos
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|dataTypePhysicalVariations
argument_list|)
argument_list|,
name|hiveConf
argument_list|)
decl_stmt|;
name|VectorExpression
name|vectorExpression
init|=
name|vectorizationContext
operator|.
name|getVectorExpression
argument_list|(
name|exprDesc
argument_list|)
decl_stmt|;
name|vectorExpression
operator|.
name|transientInit
argument_list|()
expr_stmt|;
if|if
condition|(
name|negativeTestMode
operator|==
name|NegativeTestMode
operator|.
name|VECTOR_EXPRESSION
operator|&&
name|vectorExpression
operator|instanceof
name|VectorUDFAdaptor
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"*NO NATIVE VECTOR EXPRESSION* typeInfo "
operator|+
name|typeInfo
operator|.
name|toString
argument_list|()
operator|+
literal|" negativeTestMode "
operator|+
name|negativeTestMode
operator|+
literal|" vectorExpression "
operator|+
name|vectorExpression
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|outputScratchTypeNames
init|=
name|vectorizationContext
operator|.
name|getScratchColumnTypeNames
argument_list|()
decl_stmt|;
name|VectorizedRowBatchCtx
name|batchContext
init|=
operator|new
name|VectorizedRowBatchCtx
argument_list|(
name|columnNames
argument_list|,
name|typeInfos
argument_list|,
name|dataTypePhysicalVariations
argument_list|,
comment|/* dataColumnNums */
literal|null
argument_list|,
comment|/* partitionColumnCount */
literal|0
argument_list|,
comment|/* virtualColumnCount */
literal|0
argument_list|,
comment|/* neededVirtualColumns */
literal|null
argument_list|,
name|outputScratchTypeNames
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|VectorizedRowBatch
name|batch
init|=
name|batchContext
operator|.
name|createVectorizedRowBatch
argument_list|()
decl_stmt|;
name|VectorExtractRow
name|resultVectorExtractRow
init|=
operator|new
name|VectorExtractRow
argument_list|()
decl_stmt|;
name|resultVectorExtractRow
operator|.
name|init
argument_list|(
operator|new
name|TypeInfo
index|[]
block|{
name|outputTypeInfo
block|}
argument_list|,
operator|new
name|int
index|[]
block|{
name|vectorExpression
operator|.
name|getOutputColumnNum
argument_list|()
block|}
argument_list|)
expr_stmt|;
name|Object
index|[]
name|scrqtchRow
init|=
operator|new
name|Object
index|[
literal|1
index|]
decl_stmt|;
comment|// System.out.println("*VECTOR EXPRESSION* " + vectorExpression.getClass().getSimpleName());
comment|/*     System.out.println(         "*DEBUG* typeInfo " + typeInfo.toString() +         " negativeTestMode " + negativeTestMode +         " vectorExpression " + vectorExpression.toString());     */
name|batchSource
operator|.
name|resetBatchIteration
argument_list|()
expr_stmt|;
name|int
name|rowIndex
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
operator|!
name|batchSource
operator|.
name|fillNextBatch
argument_list|(
name|batch
argument_list|)
condition|)
block|{
break|break;
block|}
name|vectorExpression
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
name|extractResultObjects
argument_list|(
name|batch
argument_list|,
name|rowIndex
argument_list|,
name|resultVectorExtractRow
argument_list|,
name|scrqtchRow
argument_list|,
name|objectInspector
argument_list|,
name|resultObjects
argument_list|)
expr_stmt|;
name|rowIndex
operator|+=
name|batch
operator|.
name|size
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

