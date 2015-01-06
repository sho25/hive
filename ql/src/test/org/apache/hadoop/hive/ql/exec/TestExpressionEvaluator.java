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
name|exec
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
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|TypeCheckProcFactory
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
name|serde
operator|.
name|serdeConstants
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
name|InspectableObject
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
name|typeinfo
operator|.
name|ListTypeInfo
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * TestExpressionEvaluator.  *  */
end_comment

begin_class
specifier|public
class|class
name|TestExpressionEvaluator
extends|extends
name|TestCase
block|{
comment|// this is our row to test expressions on
specifier|protected
name|InspectableObject
name|r
decl_stmt|;
name|ArrayList
argument_list|<
name|Text
argument_list|>
name|col1
decl_stmt|;
name|TypeInfo
name|col1Type
decl_stmt|;
name|ArrayList
argument_list|<
name|Text
argument_list|>
name|cola
decl_stmt|;
name|TypeInfo
name|colaType
decl_stmt|;
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|data
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|names
decl_stmt|;
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
name|typeInfos
decl_stmt|;
name|TypeInfo
name|dataType
decl_stmt|;
specifier|public
name|TestExpressionEvaluator
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
name|SessionState
operator|.
name|setCurrentSessionState
argument_list|(
name|ss
argument_list|)
expr_stmt|;
name|col1
operator|=
operator|new
name|ArrayList
argument_list|<
name|Text
argument_list|>
argument_list|()
expr_stmt|;
name|col1
operator|.
name|add
argument_list|(
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
argument_list|)
expr_stmt|;
name|col1
operator|.
name|add
argument_list|(
operator|new
name|Text
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|col1
operator|.
name|add
argument_list|(
operator|new
name|Text
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|col1
operator|.
name|add
argument_list|(
operator|new
name|Text
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|col1Type
operator|=
name|TypeInfoFactory
operator|.
name|getListTypeInfo
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
expr_stmt|;
name|cola
operator|=
operator|new
name|ArrayList
argument_list|<
name|Text
argument_list|>
argument_list|()
expr_stmt|;
name|cola
operator|.
name|add
argument_list|(
operator|new
name|Text
argument_list|(
literal|"a"
argument_list|)
argument_list|)
expr_stmt|;
name|cola
operator|.
name|add
argument_list|(
operator|new
name|Text
argument_list|(
literal|"b"
argument_list|)
argument_list|)
expr_stmt|;
name|cola
operator|.
name|add
argument_list|(
operator|new
name|Text
argument_list|(
literal|"c"
argument_list|)
argument_list|)
expr_stmt|;
name|colaType
operator|=
name|TypeInfoFactory
operator|.
name|getListTypeInfo
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
expr_stmt|;
try|try
block|{
name|data
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
expr_stmt|;
name|data
operator|.
name|add
argument_list|(
name|col1
argument_list|)
expr_stmt|;
name|data
operator|.
name|add
argument_list|(
name|cola
argument_list|)
expr_stmt|;
name|names
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|names
operator|.
name|add
argument_list|(
literal|"col1"
argument_list|)
expr_stmt|;
name|names
operator|.
name|add
argument_list|(
literal|"cola"
argument_list|)
expr_stmt|;
name|typeInfos
operator|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
expr_stmt|;
name|typeInfos
operator|.
name|add
argument_list|(
name|col1Type
argument_list|)
expr_stmt|;
name|typeInfos
operator|.
name|add
argument_list|(
name|colaType
argument_list|)
expr_stmt|;
name|dataType
operator|=
name|TypeInfoFactory
operator|.
name|getStructTypeInfo
argument_list|(
name|names
argument_list|,
name|typeInfos
argument_list|)
expr_stmt|;
name|r
operator|=
operator|new
name|InspectableObject
argument_list|()
expr_stmt|;
name|r
operator|.
name|o
operator|=
name|data
expr_stmt|;
name|r
operator|.
name|oi
operator|=
name|TypeInfoUtils
operator|.
name|getStandardWritableObjectInspectorFromTypeInfo
argument_list|(
name|dataType
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
block|{   }
specifier|public
name|void
name|testExprNodeColumnEvaluator
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
comment|// get a evaluator for a simple field expression
name|ExprNodeDesc
name|exprDesc
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|colaType
argument_list|,
literal|"cola"
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|ExprNodeEvaluator
name|eval
init|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|exprDesc
argument_list|)
decl_stmt|;
comment|// evaluate on row
name|ObjectInspector
name|resultOI
init|=
name|eval
operator|.
name|initialize
argument_list|(
name|r
operator|.
name|oi
argument_list|)
decl_stmt|;
name|Object
name|resultO
init|=
name|eval
operator|.
name|evaluate
argument_list|(
name|r
operator|.
name|o
argument_list|)
decl_stmt|;
name|Object
name|standardResult
init|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|resultO
argument_list|,
name|resultOI
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|cola
argument_list|,
name|standardResult
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"ExprNodeColumnEvaluator ok"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|private
specifier|static
name|ExprNodeDesc
name|getListIndexNode
parameter_list|(
name|ExprNodeDesc
name|node
parameter_list|,
name|int
name|index
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|getListIndexNode
argument_list|(
name|node
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|index
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|ExprNodeDesc
name|getListIndexNode
parameter_list|(
name|ExprNodeDesc
name|node
parameter_list|,
name|ExprNodeDesc
name|index
parameter_list|)
throws|throws
name|Exception
block|{
name|ArrayList
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
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|children
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|children
operator|.
name|add
argument_list|(
name|index
argument_list|)
expr_stmt|;
return|return
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
operator|(
operator|(
name|ListTypeInfo
operator|)
name|node
operator|.
name|getTypeInfo
argument_list|()
operator|)
operator|.
name|getListElementTypeInfo
argument_list|()
argument_list|,
name|FunctionRegistry
operator|.
name|getGenericUDFForIndex
argument_list|()
argument_list|,
name|children
argument_list|)
return|;
block|}
specifier|public
name|void
name|testExprNodeFuncEvaluator
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
comment|// get a evaluator for a string concatenation expression
name|ExprNodeDesc
name|col1desc
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|col1Type
argument_list|,
literal|"col1"
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|coladesc
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|colaType
argument_list|,
literal|"cola"
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|col11desc
init|=
name|getListIndexNode
argument_list|(
name|col1desc
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|cola0desc
init|=
name|getListIndexNode
argument_list|(
name|coladesc
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|func1
init|=
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"concat"
argument_list|,
name|col11desc
argument_list|,
name|cola0desc
argument_list|)
decl_stmt|;
name|ExprNodeEvaluator
name|eval
init|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|func1
argument_list|)
decl_stmt|;
comment|// evaluate on row
name|ObjectInspector
name|resultOI
init|=
name|eval
operator|.
name|initialize
argument_list|(
name|r
operator|.
name|oi
argument_list|)
decl_stmt|;
name|Object
name|resultO
init|=
name|eval
operator|.
name|evaluate
argument_list|(
name|r
operator|.
name|o
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1a"
argument_list|,
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|resultO
argument_list|,
name|resultOI
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|JAVA
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"ExprNodeFuncEvaluator ok"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|public
name|void
name|testExprNodeConversionEvaluator
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
comment|// get a evaluator for a string concatenation expression
name|ExprNodeDesc
name|col1desc
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|col1Type
argument_list|,
literal|"col1"
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|col11desc
init|=
name|getListIndexNode
argument_list|(
name|col1desc
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|func1
init|=
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
name|serdeConstants
operator|.
name|DOUBLE_TYPE_NAME
argument_list|,
name|col11desc
argument_list|)
decl_stmt|;
name|ExprNodeEvaluator
name|eval
init|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|func1
argument_list|)
decl_stmt|;
comment|// evaluate on row
name|ObjectInspector
name|resultOI
init|=
name|eval
operator|.
name|initialize
argument_list|(
name|r
operator|.
name|oi
argument_list|)
decl_stmt|;
name|Object
name|resultO
init|=
name|eval
operator|.
name|evaluate
argument_list|(
name|r
operator|.
name|o
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Double
operator|.
name|valueOf
argument_list|(
literal|"1"
argument_list|)
argument_list|,
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|resultO
argument_list|,
name|resultOI
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|JAVA
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"testExprNodeConversionEvaluator ok"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|measureSpeed
parameter_list|(
name|String
name|expr
parameter_list|,
name|int
name|times
parameter_list|,
name|ExprNodeEvaluator
name|eval
parameter_list|,
name|InspectableObject
name|input
parameter_list|,
name|Object
name|standardJavaOutput
parameter_list|)
throws|throws
name|HiveException
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Evaluating "
operator|+
name|expr
operator|+
literal|" for "
operator|+
name|times
operator|+
literal|" times"
argument_list|)
expr_stmt|;
operator|new
name|InspectableObject
argument_list|()
expr_stmt|;
name|ObjectInspector
name|resultOI
init|=
name|eval
operator|.
name|initialize
argument_list|(
name|input
operator|.
name|oi
argument_list|)
decl_stmt|;
name|Object
name|resultO
init|=
literal|null
decl_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
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
name|times
condition|;
name|i
operator|++
control|)
block|{
name|resultO
operator|=
name|eval
operator|.
name|evaluate
argument_list|(
name|input
operator|.
name|o
argument_list|)
expr_stmt|;
block|}
name|long
name|end
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|standardJavaOutput
argument_list|,
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|resultO
argument_list|,
name|resultOI
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|JAVA
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Evaluation finished: "
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%2.3f"
argument_list|,
operator|(
name|end
operator|-
name|start
operator|)
operator|*
literal|0.001
argument_list|)
operator|+
literal|" seconds, "
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%2.3f"
argument_list|,
operator|(
name|end
operator|-
name|start
operator|)
operator|*
literal|1000.0
operator|/
name|times
argument_list|)
operator|+
literal|" seconds/million call."
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testExprNodeSpeed
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|int
name|basetimes
init|=
literal|100000
decl_stmt|;
name|measureSpeed
argument_list|(
literal|"1 + 2"
argument_list|,
name|basetimes
operator|*
literal|100
argument_list|,
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"+"
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|1
argument_list|)
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|r
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
literal|1
operator|+
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|measureSpeed
argument_list|(
literal|"1 + 2 - 3"
argument_list|,
name|basetimes
operator|*
literal|100
argument_list|,
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"-"
argument_list|,
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"+"
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|1
argument_list|)
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|r
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
literal|1
operator|+
literal|2
operator|-
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|measureSpeed
argument_list|(
literal|"1 + 2 - 3 + 4"
argument_list|,
name|basetimes
operator|*
literal|100
argument_list|,
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"+"
argument_list|,
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"-"
argument_list|,
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"+"
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|1
argument_list|)
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|4
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|r
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
literal|1
operator|+
literal|2
operator|-
literal|3
operator|+
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|measureSpeed
argument_list|(
literal|"concat(\"1\", \"2\")"
argument_list|,
name|basetimes
operator|*
literal|100
argument_list|,
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"concat"
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|"1"
argument_list|)
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|"2"
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|r
argument_list|,
literal|"12"
argument_list|)
expr_stmt|;
name|measureSpeed
argument_list|(
literal|"concat(concat(\"1\", \"2\"), \"3\")"
argument_list|,
name|basetimes
operator|*
literal|100
argument_list|,
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"concat"
argument_list|,
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"concat"
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|"1"
argument_list|)
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|"2"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|"3"
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|r
argument_list|,
literal|"123"
argument_list|)
expr_stmt|;
name|measureSpeed
argument_list|(
literal|"concat(concat(concat(\"1\", \"2\"), \"3\"), \"4\")"
argument_list|,
name|basetimes
operator|*
literal|100
argument_list|,
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"concat"
argument_list|,
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"concat"
argument_list|,
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"concat"
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|"1"
argument_list|)
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|"2"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|"3"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|"4"
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|r
argument_list|,
literal|"1234"
argument_list|)
expr_stmt|;
name|ExprNodeDesc
name|constant1
init|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|constant2
init|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|measureSpeed
argument_list|(
literal|"concat(col1[1], cola[1])"
argument_list|,
name|basetimes
operator|*
literal|10
argument_list|,
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"concat"
argument_list|,
name|getListIndexNode
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|col1Type
argument_list|,
literal|"col1"
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|)
argument_list|,
name|constant1
argument_list|)
argument_list|,
name|getListIndexNode
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|colaType
argument_list|,
literal|"cola"
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|)
argument_list|,
name|constant1
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|r
argument_list|,
literal|"1b"
argument_list|)
expr_stmt|;
name|measureSpeed
argument_list|(
literal|"concat(concat(col1[1], cola[1]), col1[2])"
argument_list|,
name|basetimes
operator|*
literal|10
argument_list|,
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"concat"
argument_list|,
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"concat"
argument_list|,
name|getListIndexNode
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|col1Type
argument_list|,
literal|"col1"
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|)
argument_list|,
name|constant1
argument_list|)
argument_list|,
name|getListIndexNode
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|colaType
argument_list|,
literal|"cola"
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|)
argument_list|,
name|constant1
argument_list|)
argument_list|)
argument_list|,
name|getListIndexNode
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|col1Type
argument_list|,
literal|"col1"
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|)
argument_list|,
name|constant2
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|r
argument_list|,
literal|"1b2"
argument_list|)
expr_stmt|;
name|measureSpeed
argument_list|(
literal|"concat(concat(concat(col1[1], cola[1]), col1[2]), cola[2])"
argument_list|,
name|basetimes
operator|*
literal|10
argument_list|,
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"concat"
argument_list|,
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"concat"
argument_list|,
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"concat"
argument_list|,
name|getListIndexNode
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|col1Type
argument_list|,
literal|"col1"
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|)
argument_list|,
name|constant1
argument_list|)
argument_list|,
name|getListIndexNode
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|colaType
argument_list|,
literal|"cola"
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|)
argument_list|,
name|constant1
argument_list|)
argument_list|)
argument_list|,
name|getListIndexNode
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|col1Type
argument_list|,
literal|"col1"
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|)
argument_list|,
name|constant2
argument_list|)
argument_list|)
argument_list|,
name|getListIndexNode
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|colaType
argument_list|,
literal|"cola"
argument_list|,
literal|""
argument_list|,
literal|false
argument_list|)
argument_list|,
name|constant2
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|r
argument_list|,
literal|"1b2c"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
block|}
end_class

end_unit

