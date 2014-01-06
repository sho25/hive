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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|fs
operator|.
name|Path
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
name|io
operator|.
name|IOContext
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
name|CollectDesc
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
name|FilterDesc
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
name|MapredWork
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
name|PartitionDesc
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
name|PlanUtils
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
name|ScriptDesc
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
name|SelectDesc
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
name|TableDesc
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
name|PrimitiveObjectInspector
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
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_comment
comment|/**  * TestOperators.  *  */
end_comment

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
annotation|@
name|Override
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
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|)
expr_stmt|;
name|objectInspectors
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|)
expr_stmt|;
name|objectInspectors
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
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
name|ExprNodeDesc
name|col0
init|=
name|TestExecDriver
operator|.
name|getStringColumn
argument_list|(
literal|"col0"
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|col1
init|=
name|TestExecDriver
operator|.
name|getStringColumn
argument_list|(
literal|"col1"
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|col2
init|=
name|TestExecDriver
operator|.
name|getStringColumn
argument_list|(
literal|"col2"
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|zero
init|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|"0"
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
literal|">"
argument_list|,
name|col2
argument_list|,
name|col1
argument_list|)
decl_stmt|;
name|ExprNodeDesc
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
name|ExprNodeDesc
name|func3
init|=
name|TypeCheckProcFactory
operator|.
name|DefaultExprProcessor
operator|.
name|getFuncExprNodeDesc
argument_list|(
literal|"and"
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
name|FilterDesc
name|filterCtx
init|=
operator|new
name|FilterDesc
argument_list|(
name|func3
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// Configuration
name|Operator
argument_list|<
name|FilterDesc
argument_list|>
name|op
init|=
name|OperatorFactory
operator|.
name|get
argument_list|(
name|FilterDesc
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
operator|new
name|JobConf
argument_list|(
name|TestOperators
operator|.
name|class
argument_list|)
argument_list|,
operator|new
name|ObjectInspector
index|[]
block|{
name|r
index|[
literal|0
index|]
operator|.
name|oi
block|}
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
name|processOp
argument_list|(
name|oner
operator|.
name|o
argument_list|,
literal|0
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
name|Long
operator|.
name|valueOf
argument_list|(
literal|4
argument_list|)
argument_list|,
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
name|Long
operator|.
name|valueOf
argument_list|(
literal|1
argument_list|)
argument_list|,
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
comment|/*        * for(Enum e: results.keySet()) { System.out.println(e.toString() + ":" +        * results.get(e)); }        */
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
specifier|private
name|void
name|testTaskIds
parameter_list|(
name|String
index|[]
name|taskIds
parameter_list|,
name|String
name|expectedAttemptId
parameter_list|,
name|String
name|expectedTaskId
parameter_list|)
block|{
name|Configuration
name|conf
init|=
operator|new
name|JobConf
argument_list|(
name|TestOperators
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|one
range|:
name|taskIds
control|)
block|{
name|conf
operator|.
name|set
argument_list|(
literal|"mapred.task.id"
argument_list|,
name|one
argument_list|)
expr_stmt|;
name|String
name|attemptId
init|=
name|Utilities
operator|.
name|getTaskId
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedAttemptId
argument_list|,
name|attemptId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Utilities
operator|.
name|getTaskIdFromFilename
argument_list|(
name|attemptId
argument_list|)
argument_list|,
name|expectedTaskId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Utilities
operator|.
name|getTaskIdFromFilename
argument_list|(
name|attemptId
operator|+
literal|".gz"
argument_list|)
argument_list|,
name|expectedTaskId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Utilities
operator|.
name|getTaskIdFromFilename
argument_list|(
name|Utilities
operator|.
name|toTempPath
argument_list|(
operator|new
name|Path
argument_list|(
name|attemptId
operator|+
literal|".gz"
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|expectedTaskId
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * More stuff needs to be added here. Currently it only checks some basic    * file naming libraries    * The old test was deactivated as part of hive-405    */
specifier|public
name|void
name|testFileSinkOperator
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|testTaskIds
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"attempt_200707121733_0003_m_000005_0"
block|,
literal|"attempt_local_0001_m_000005_0"
block|,
literal|"task_200709221812_0001_m_000005_0"
block|,
literal|"task_local_0001_m_000005_0"
block|}
argument_list|,
literal|"000005_0"
argument_list|,
literal|"000005"
argument_list|)
expr_stmt|;
name|testTaskIds
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"job_local_0001_map_000005"
block|,
literal|"job_local_0001_reduce_000005"
block|,         }
argument_list|,
literal|"000005"
argument_list|,
literal|"000005"
argument_list|)
expr_stmt|;
name|testTaskIds
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"1234567"
block|}
argument_list|,
literal|"1234567"
argument_list|,
literal|"1234567"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Utilities
operator|.
name|getTaskIdFromFilename
argument_list|(
literal|"/mnt/dev005/task_local_0001_m_000005_0"
argument_list|)
argument_list|,
literal|"000005"
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
comment|/**    *  When ScriptOperator runs external script, it passes job configuration as environment    *  variables. But environment variables have some system limitations and we have to check    *  job configuration properties firstly. This test checks that staff.    */
specifier|public
name|void
name|testScriptOperatorEnvVarsProcessing
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|ScriptOperator
name|scriptOperator
init|=
operator|new
name|ScriptOperator
argument_list|()
decl_stmt|;
comment|//Environment Variables name
name|assertEquals
argument_list|(
literal|"a_b_c"
argument_list|,
name|scriptOperator
operator|.
name|safeEnvVarName
argument_list|(
literal|"a.b.c"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"a_b_c"
argument_list|,
name|scriptOperator
operator|.
name|safeEnvVarName
argument_list|(
literal|"a-b-c"
argument_list|)
argument_list|)
expr_stmt|;
comment|//Environment Variables short values
name|assertEquals
argument_list|(
literal|"value"
argument_list|,
name|scriptOperator
operator|.
name|safeEnvVarValue
argument_list|(
literal|"value"
argument_list|,
literal|"name"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"value"
argument_list|,
name|scriptOperator
operator|.
name|safeEnvVarValue
argument_list|(
literal|"value"
argument_list|,
literal|"name"
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
comment|//Environment Variables long values
name|char
index|[]
name|array
init|=
operator|new
name|char
index|[
literal|20
operator|*
literal|1024
operator|+
literal|1
index|]
decl_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|array
argument_list|,
literal|'a'
argument_list|)
expr_stmt|;
name|String
name|hugeEnvVar
init|=
operator|new
name|String
argument_list|(
name|array
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|20
operator|*
literal|1024
operator|+
literal|1
argument_list|,
name|hugeEnvVar
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20
operator|*
literal|1024
operator|+
literal|1
argument_list|,
name|scriptOperator
operator|.
name|safeEnvVarValue
argument_list|(
name|hugeEnvVar
argument_list|,
literal|"name"
argument_list|,
literal|false
argument_list|)
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20
operator|*
literal|1024
argument_list|,
name|scriptOperator
operator|.
name|safeEnvVarValue
argument_list|(
name|hugeEnvVar
argument_list|,
literal|"name"
argument_list|,
literal|true
argument_list|)
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
comment|//Full test
name|Configuration
name|hconf
init|=
operator|new
name|JobConf
argument_list|(
name|ScriptOperator
operator|.
name|class
argument_list|)
decl_stmt|;
name|hconf
operator|.
name|set
argument_list|(
literal|"name"
argument_list|,
name|hugeEnvVar
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|env
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESCRIPTTRUNCATEENV
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|scriptOperator
operator|.
name|addJobConfToEnvironment
argument_list|(
name|hconf
argument_list|,
name|env
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20
operator|*
literal|1024
operator|+
literal|1
argument_list|,
name|env
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|HiveConf
operator|.
name|setBoolVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESCRIPTTRUNCATEENV
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|scriptOperator
operator|.
name|addJobConfToEnvironment
argument_list|(
name|hconf
argument_list|,
name|env
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20
operator|*
literal|1024
argument_list|,
name|env
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Script Operator Environment Variables processing ok"
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
name|ExprNodeDesc
name|exprDesc1
init|=
name|TestExecDriver
operator|.
name|getStringColumn
argument_list|(
literal|"col1"
argument_list|)
decl_stmt|;
comment|// col2
name|ExprNodeDesc
name|expr1
init|=
name|TestExecDriver
operator|.
name|getStringColumn
argument_list|(
literal|"col0"
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|expr2
init|=
operator|new
name|ExprNodeConstantDesc
argument_list|(
literal|"1"
argument_list|)
decl_stmt|;
name|ExprNodeDesc
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
name|ExprNodeDesc
argument_list|>
name|earr
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
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
name|ArrayList
argument_list|<
name|String
argument_list|>
name|outputCols
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
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
name|earr
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|outputCols
operator|.
name|add
argument_list|(
literal|"_col"
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|SelectDesc
name|selectCtx
init|=
operator|new
name|SelectDesc
argument_list|(
name|earr
argument_list|,
name|outputCols
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|SelectDesc
argument_list|>
name|op
init|=
name|OperatorFactory
operator|.
name|get
argument_list|(
name|SelectDesc
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
name|TableDesc
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
name|TableDesc
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
name|ScriptDesc
name|sd
init|=
operator|new
name|ScriptDesc
argument_list|(
literal|"cat"
argument_list|,
name|scriptOutput
argument_list|,
name|TextRecordWriter
operator|.
name|class
argument_list|,
name|scriptInput
argument_list|,
name|TextRecordReader
operator|.
name|class
argument_list|,
name|TextRecordReader
operator|.
name|class
argument_list|,
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
literal|"key"
argument_list|)
argument_list|)
decl_stmt|;
name|Operator
argument_list|<
name|ScriptDesc
argument_list|>
name|sop
init|=
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
name|sd
argument_list|,
name|op
argument_list|)
decl_stmt|;
comment|// Collect operator to observe the output of the script
name|CollectDesc
name|cd
init|=
operator|new
name|CollectDesc
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
name|getAndMakeChild
argument_list|(
name|cd
argument_list|,
name|sop
argument_list|)
decl_stmt|;
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
operator|new
name|ObjectInspector
index|[]
block|{
name|r
index|[
literal|0
index|]
operator|.
name|oi
block|}
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
name|processOp
argument_list|(
name|r
index|[
name|i
index|]
operator|.
name|o
argument_list|,
literal|0
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
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|a
operator|.
name|getFieldObjectInspector
argument_list|()
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
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
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|b
operator|.
name|getFieldObjectInspector
argument_list|()
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
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
name|IOContext
operator|.
name|get
argument_list|()
operator|.
name|setInputPath
argument_list|(
operator|new
name|Path
argument_list|(
literal|"hdfs:///testDir/testFile"
argument_list|)
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
literal|"hdfs:///testDir"
argument_list|,
name|aliases
argument_list|)
expr_stmt|;
comment|// initialize pathToTableInfo
comment|// Default: treat the table as a single column "col"
name|TableDesc
name|td
init|=
name|Utilities
operator|.
name|defaultTd
decl_stmt|;
name|PartitionDesc
name|pd
init|=
operator|new
name|PartitionDesc
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
name|PartitionDesc
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
name|PartitionDesc
argument_list|>
argument_list|()
decl_stmt|;
name|pathToPartitionInfo
operator|.
name|put
argument_list|(
literal|"hdfs:///testDir"
argument_list|,
name|pd
argument_list|)
expr_stmt|;
comment|// initialize aliasToWork
name|CollectDesc
name|cd
init|=
operator|new
name|CollectDesc
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
name|CollectDesc
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
name|CollectDesc
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
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
argument_list|>
name|aliasToWork
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
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
name|MapredWork
name|mrwork
init|=
operator|new
name|MapredWork
argument_list|()
decl_stmt|;
name|mrwork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setPathToAliases
argument_list|(
name|pathToAliases
argument_list|)
expr_stmt|;
name|mrwork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setPathToPartitionInfo
argument_list|(
name|pathToPartitionInfo
argument_list|)
expr_stmt|;
name|mrwork
operator|.
name|getMapWork
argument_list|()
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
name|initializeAsRoot
argument_list|(
name|hconf
argument_list|,
name|mrwork
operator|.
name|getMapWork
argument_list|()
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

