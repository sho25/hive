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
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
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
name|mapred
operator|.
name|Reporter
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
name|conf
operator|.
name|Configuration
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
name|Writable
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
name|SemanticAnalyzer
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
name|*
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
name|ObjectInspectorFactory
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
name|StructField
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
name|StructObjectInspector
import|;
end_import

begin_class
specifier|public
class|class
name|TestOperators
extends|extends
name|TestCase
block|{
comment|// this is our row to test expressions on
specifier|protected
name|InspectableObject
index|[]
name|r
decl_stmt|;
specifier|protected
name|void
name|setUp
parameter_list|()
block|{
name|r
operator|=
operator|new
name|InspectableObject
index|[
literal|5
index|]
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|names
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|names
operator|.
name|add
argument_list|(
literal|"col0"
argument_list|)
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
literal|"col2"
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|objectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|objectInspectors
operator|.
name|add
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getStandardPrimitiveObjectInspector
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|objectInspectors
operator|.
name|add
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getStandardPrimitiveObjectInspector
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|objectInspectors
operator|.
name|add
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getStandardPrimitiveObjectInspector
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|data
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|data
operator|.
name|add
argument_list|(
literal|""
operator|+
name|i
argument_list|)
expr_stmt|;
name|data
operator|.
name|add
argument_list|(
literal|""
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|)
expr_stmt|;
name|data
operator|.
name|add
argument_list|(
literal|""
operator|+
operator|(
name|i
operator|+
literal|2
operator|)
argument_list|)
expr_stmt|;
try|try
block|{
name|r
index|[
name|i
index|]
operator|=
operator|new
name|InspectableObject
argument_list|()
expr_stmt|;
name|r
index|[
name|i
index|]
operator|.
name|o
operator|=
name|data
expr_stmt|;
name|r
index|[
name|i
index|]
operator|.
name|oi
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|names
argument_list|,
name|objectInspectors
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
name|void
name|testBaseFilterOperator
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Testing Filter Operator"
argument_list|)
expr_stmt|;
name|exprNodeDesc
name|col0
init|=
operator|new
name|exprNodeColumnDesc
argument_list|(
name|String
operator|.
name|class
argument_list|,
literal|"col0"
argument_list|)
decl_stmt|;
name|exprNodeDesc
name|col1
init|=
operator|new
name|exprNodeColumnDesc
argument_list|(
name|String
operator|.
name|class
argument_list|,
literal|"col1"
argument_list|)
decl_stmt|;
name|exprNodeDesc
name|col2
init|=
operator|new
name|exprNodeColumnDesc
argument_list|(
name|String
operator|.
name|class
argument_list|,
literal|"col2"
argument_list|)
decl_stmt|;
name|exprNodeDesc
name|zero
init|=
operator|new
name|exprNodeConstantDesc
argument_list|(
name|String
operator|.
name|class
argument_list|,
literal|"0"
argument_list|)
decl_stmt|;
name|exprNodeDesc
name|func1
init|=
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|">"
argument_list|,
name|col2
argument_list|,
name|col1
argument_list|)
decl_stmt|;
name|exprNodeDesc
name|func2
init|=
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"=="
argument_list|,
name|col0
argument_list|,
name|zero
argument_list|)
decl_stmt|;
name|exprNodeDesc
name|func3
init|=
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"&&"
argument_list|,
name|func1
argument_list|,
name|func2
argument_list|)
decl_stmt|;
assert|assert
operator|(
name|func3
operator|!=
literal|null
operator|)
assert|;
name|filterDesc
name|filterCtx
init|=
operator|new
name|filterDesc
argument_list|(
name|func3
argument_list|)
decl_stmt|;
comment|// Configuration
name|Operator
argument_list|<
name|filterDesc
argument_list|>
name|op
init|=
name|OperatorFactory
operator|.
name|get
argument_list|(
name|filterDesc
operator|.
name|class
argument_list|)
decl_stmt|;
name|op
operator|.
name|setConf
argument_list|(
name|filterCtx
argument_list|)
expr_stmt|;
comment|// runtime initialization
name|op
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
for|for
control|(
name|InspectableObject
name|oner
range|:
name|r
control|)
block|{
name|op
operator|.
name|process
argument_list|(
name|oner
operator|.
name|o
argument_list|,
name|oner
operator|.
name|oi
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|Enum
argument_list|<
name|?
argument_list|>
argument_list|,
name|Long
argument_list|>
name|results
init|=
name|op
operator|.
name|getStats
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"filtered = "
operator|+
name|results
operator|.
name|get
argument_list|(
name|FilterOperator
operator|.
name|Counter
operator|.
name|FILTERED
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|FilterOperator
operator|.
name|Counter
operator|.
name|FILTERED
argument_list|)
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"passed = "
operator|+
name|results
operator|.
name|get
argument_list|(
name|FilterOperator
operator|.
name|Counter
operator|.
name|PASSED
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|FilterOperator
operator|.
name|Counter
operator|.
name|PASSED
argument_list|)
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|/*       for(Enum e: results.keySet()) {         System.out.println(e.toString() + ":" + results.get(e));       }       */
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Filter Operator ok"
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
name|testFileSinkOperator
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Testing FileSink Operator"
argument_list|)
expr_stmt|;
comment|// col1
name|exprNodeDesc
name|exprDesc1
init|=
operator|new
name|exprNodeColumnDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|,
literal|"col1"
argument_list|)
decl_stmt|;
comment|// col2
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|exprDesc2children
init|=
operator|new
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|exprNodeDesc
name|expr1
init|=
operator|new
name|exprNodeColumnDesc
argument_list|(
name|String
operator|.
name|class
argument_list|,
literal|"col0"
argument_list|)
decl_stmt|;
name|exprNodeDesc
name|expr2
init|=
operator|new
name|exprNodeConstantDesc
argument_list|(
literal|"1"
argument_list|)
decl_stmt|;
name|exprNodeDesc
name|exprDesc2
init|=
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"concat"
argument_list|,
name|expr1
argument_list|,
name|expr2
argument_list|)
decl_stmt|;
comment|// select operator to project these two columns
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|earr
init|=
operator|new
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|earr
operator|.
name|add
argument_list|(
name|exprDesc1
argument_list|)
expr_stmt|;
name|earr
operator|.
name|add
argument_list|(
name|exprDesc2
argument_list|)
expr_stmt|;
name|selectDesc
name|selectCtx
init|=
operator|new
name|selectDesc
argument_list|(
name|earr
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|selectDesc
argument_list|>
name|op
init|=
name|OperatorFactory
operator|.
name|get
argument_list|(
name|selectDesc
operator|.
name|class
argument_list|)
decl_stmt|;
name|op
operator|.
name|setConf
argument_list|(
name|selectCtx
argument_list|)
expr_stmt|;
comment|// fileSinkOperator to dump the output of the select
name|fileSinkDesc
name|fsd
init|=
operator|new
name|fileSinkDesc
argument_list|(
literal|"file:///tmp"
operator|+
name|File
operator|.
name|separator
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
operator|+
name|File
operator|.
name|separator
operator|+
literal|"TestFileSinkOperator"
argument_list|,
name|Utilities
operator|.
name|defaultTd
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|fileSinkDesc
argument_list|>
name|flop
init|=
name|OperatorFactory
operator|.
name|get
argument_list|(
name|fileSinkDesc
operator|.
name|class
argument_list|)
decl_stmt|;
name|flop
operator|.
name|setConf
argument_list|(
name|fsd
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|nextOp
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|nextOp
operator|.
name|add
argument_list|(
name|flop
argument_list|)
expr_stmt|;
name|op
operator|.
name|setChildOperators
argument_list|(
name|nextOp
argument_list|)
expr_stmt|;
name|op
operator|.
name|initialize
argument_list|(
operator|new
name|JobConf
argument_list|(
name|TestOperators
operator|.
name|class
argument_list|)
argument_list|,
name|Reporter
operator|.
name|NULL
argument_list|)
expr_stmt|;
comment|// evaluate on row
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|op
operator|.
name|process
argument_list|(
name|r
index|[
name|i
index|]
operator|.
name|o
argument_list|,
name|r
index|[
name|i
index|]
operator|.
name|oi
argument_list|)
expr_stmt|;
block|}
name|op
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"FileSink Operator ok"
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
name|testScriptOperator
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Testing Script Operator"
argument_list|)
expr_stmt|;
comment|// col1
name|exprNodeDesc
name|exprDesc1
init|=
operator|new
name|exprNodeColumnDesc
argument_list|(
name|String
operator|.
name|class
argument_list|,
literal|"col1"
argument_list|)
decl_stmt|;
comment|// col2
name|exprNodeDesc
name|expr1
init|=
operator|new
name|exprNodeColumnDesc
argument_list|(
name|String
operator|.
name|class
argument_list|,
literal|"col0"
argument_list|)
decl_stmt|;
name|exprNodeDesc
name|expr2
init|=
operator|new
name|exprNodeConstantDesc
argument_list|(
literal|"1"
argument_list|)
decl_stmt|;
name|exprNodeDesc
name|exprDesc2
init|=
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"concat"
argument_list|,
name|expr1
argument_list|,
name|expr2
argument_list|)
decl_stmt|;
comment|// select operator to project these two columns
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|earr
init|=
operator|new
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
argument_list|()
decl_stmt|;
name|earr
operator|.
name|add
argument_list|(
name|exprDesc1
argument_list|)
expr_stmt|;
name|earr
operator|.
name|add
argument_list|(
name|exprDesc2
argument_list|)
expr_stmt|;
name|selectDesc
name|selectCtx
init|=
operator|new
name|selectDesc
argument_list|(
name|earr
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|selectDesc
argument_list|>
name|op
init|=
name|OperatorFactory
operator|.
name|get
argument_list|(
name|selectDesc
operator|.
name|class
argument_list|)
decl_stmt|;
name|op
operator|.
name|setConf
argument_list|(
name|selectCtx
argument_list|)
expr_stmt|;
comment|// scriptOperator to echo the output of the select
name|tableDesc
name|scriptOutput
init|=
name|PlanUtils
operator|.
name|getDefaultTableDesc
argument_list|(
literal|""
operator|+
name|Utilities
operator|.
name|tabCode
argument_list|,
literal|"a,b"
argument_list|)
decl_stmt|;
name|tableDesc
name|scriptInput
init|=
name|PlanUtils
operator|.
name|getDefaultTableDesc
argument_list|(
literal|""
operator|+
name|Utilities
operator|.
name|tabCode
argument_list|,
literal|"a,b"
argument_list|)
decl_stmt|;
name|scriptDesc
name|sd
init|=
operator|new
name|scriptDesc
argument_list|(
literal|"cat"
argument_list|,
name|scriptOutput
argument_list|,
name|scriptInput
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|scriptDesc
argument_list|>
name|sop
init|=
name|OperatorFactory
operator|.
name|get
argument_list|(
name|scriptDesc
operator|.
name|class
argument_list|)
decl_stmt|;
name|sop
operator|.
name|setConf
argument_list|(
name|sd
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|nextScriptOp
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|nextScriptOp
operator|.
name|add
argument_list|(
name|sop
argument_list|)
expr_stmt|;
comment|// Collect operator to observe the output of the script
name|collectDesc
name|cd
init|=
operator|new
name|collectDesc
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|10
argument_list|)
argument_list|)
decl_stmt|;
name|CollectOperator
name|cdop
init|=
operator|(
name|CollectOperator
operator|)
name|OperatorFactory
operator|.
name|get
argument_list|(
name|collectDesc
operator|.
name|class
argument_list|)
decl_stmt|;
name|cdop
operator|.
name|setConf
argument_list|(
name|cd
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|nextCollectOp
init|=
operator|new
name|ArrayList
argument_list|<
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|nextCollectOp
operator|.
name|add
argument_list|(
name|cdop
argument_list|)
expr_stmt|;
comment|// chain the scriptOperator to the select operator
name|op
operator|.
name|setChildOperators
argument_list|(
name|nextScriptOp
argument_list|)
expr_stmt|;
comment|// chain the collect operator to the script operator
name|sop
operator|.
name|setChildOperators
argument_list|(
name|nextCollectOp
argument_list|)
expr_stmt|;
name|op
operator|.
name|initialize
argument_list|(
operator|new
name|JobConf
argument_list|(
name|TestOperators
operator|.
name|class
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// evaluate on row
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|op
operator|.
name|process
argument_list|(
name|r
index|[
name|i
index|]
operator|.
name|o
argument_list|,
name|r
index|[
name|i
index|]
operator|.
name|oi
argument_list|)
expr_stmt|;
block|}
name|op
operator|.
name|close
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|InspectableObject
name|io
init|=
operator|new
name|InspectableObject
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|cdop
operator|.
name|retrieve
argument_list|(
name|io
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"["
operator|+
name|i
operator|+
literal|"] io.o="
operator|+
name|io
operator|.
name|o
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"["
operator|+
name|i
operator|+
literal|"] io.oi="
operator|+
name|io
operator|.
name|oi
argument_list|)
expr_stmt|;
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|io
operator|.
name|oi
decl_stmt|;
assert|assert
operator|(
name|soi
operator|!=
literal|null
operator|)
assert|;
name|StructField
name|a
init|=
name|soi
operator|.
name|getStructFieldRef
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
name|StructField
name|b
init|=
name|soi
operator|.
name|getStructFieldRef
argument_list|(
literal|"b"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|""
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|,
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|io
operator|.
name|o
argument_list|,
name|a
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|i
operator|)
operator|+
literal|"1"
argument_list|,
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|io
operator|.
name|o
argument_list|,
name|b
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Script Operator ok"
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
name|testMapOperator
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Testing Map Operator"
argument_list|)
expr_stmt|;
comment|// initialize configuration
name|Configuration
name|hconf
init|=
operator|new
name|JobConf
argument_list|(
name|TestOperators
operator|.
name|class
argument_list|)
decl_stmt|;
name|HiveConf
operator|.
name|setVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPMAPFILENAME
argument_list|,
literal|"hdfs:///testDir/testFile"
argument_list|)
expr_stmt|;
comment|// initialize pathToAliases
name|ArrayList
argument_list|<
name|String
argument_list|>
name|aliases
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|aliases
operator|.
name|add
argument_list|(
literal|"a"
argument_list|)
expr_stmt|;
name|aliases
operator|.
name|add
argument_list|(
literal|"b"
argument_list|)
expr_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|pathToAliases
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|pathToAliases
operator|.
name|put
argument_list|(
literal|"/testDir"
argument_list|,
name|aliases
argument_list|)
expr_stmt|;
comment|// initialize pathToTableInfo
comment|// Default: treat the table as a single column "col"
name|tableDesc
name|td
init|=
name|Utilities
operator|.
name|defaultTd
decl_stmt|;
name|partitionDesc
name|pd
init|=
operator|new
name|partitionDesc
argument_list|(
name|td
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
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
name|partitionDesc
argument_list|>
name|pathToPartitionInfo
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
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
name|partitionDesc
argument_list|>
argument_list|()
decl_stmt|;
name|pathToPartitionInfo
operator|.
name|put
argument_list|(
literal|"/testDir"
argument_list|,
name|pd
argument_list|)
expr_stmt|;
comment|// initialize aliasToWork
name|collectDesc
name|cd
init|=
operator|new
name|collectDesc
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|CollectOperator
name|cdop1
init|=
operator|(
name|CollectOperator
operator|)
name|OperatorFactory
operator|.
name|get
argument_list|(
name|collectDesc
operator|.
name|class
argument_list|)
decl_stmt|;
name|cdop1
operator|.
name|setConf
argument_list|(
name|cd
argument_list|)
expr_stmt|;
name|CollectOperator
name|cdop2
init|=
operator|(
name|CollectOperator
operator|)
name|OperatorFactory
operator|.
name|get
argument_list|(
name|collectDesc
operator|.
name|class
argument_list|)
decl_stmt|;
name|cdop2
operator|.
name|setConf
argument_list|(
name|cd
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|aliasToWork
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|aliasToWork
operator|.
name|put
argument_list|(
literal|"a"
argument_list|,
name|cdop1
argument_list|)
expr_stmt|;
name|aliasToWork
operator|.
name|put
argument_list|(
literal|"b"
argument_list|,
name|cdop2
argument_list|)
expr_stmt|;
comment|// initialize mapredWork
name|mapredWork
name|mrwork
init|=
operator|new
name|mapredWork
argument_list|()
decl_stmt|;
name|mrwork
operator|.
name|setPathToAliases
argument_list|(
name|pathToAliases
argument_list|)
expr_stmt|;
name|mrwork
operator|.
name|setPathToPartitionInfo
argument_list|(
name|pathToPartitionInfo
argument_list|)
expr_stmt|;
name|mrwork
operator|.
name|setAliasToWork
argument_list|(
name|aliasToWork
argument_list|)
expr_stmt|;
comment|// get map operator and initialize it
name|MapOperator
name|mo
init|=
operator|new
name|MapOperator
argument_list|()
decl_stmt|;
name|mo
operator|.
name|setConf
argument_list|(
name|mrwork
argument_list|)
expr_stmt|;
name|mo
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Text
name|tw
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|InspectableObject
name|io1
init|=
operator|new
name|InspectableObject
argument_list|()
decl_stmt|;
name|InspectableObject
name|io2
init|=
operator|new
name|InspectableObject
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|String
name|answer
init|=
literal|"[["
operator|+
name|i
operator|+
literal|", "
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
operator|+
literal|", "
operator|+
operator|(
name|i
operator|+
literal|2
operator|)
operator|+
literal|"]]"
decl_stmt|;
name|tw
operator|.
name|set
argument_list|(
literal|""
operator|+
name|i
operator|+
literal|"\u0001"
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
operator|+
literal|"\u0001"
operator|+
operator|(
name|i
operator|+
literal|2
operator|)
argument_list|)
expr_stmt|;
name|mo
operator|.
name|process
argument_list|(
operator|(
name|Writable
operator|)
name|tw
argument_list|)
expr_stmt|;
name|cdop1
operator|.
name|retrieve
argument_list|(
name|io1
argument_list|)
expr_stmt|;
name|cdop2
operator|.
name|retrieve
argument_list|(
name|io2
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"io1.o.toString() = "
operator|+
name|io1
operator|.
name|o
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"io2.o.toString() = "
operator|+
name|io2
operator|.
name|o
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"answer.toString() = "
operator|+
name|answer
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|answer
operator|.
name|toString
argument_list|()
argument_list|,
name|io1
operator|.
name|o
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|answer
operator|.
name|toString
argument_list|()
argument_list|,
name|io2
operator|.
name|o
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Map Operator ok"
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
operator|(
name|e
operator|)
throw|;
block|}
block|}
block|}
end_class

end_unit

