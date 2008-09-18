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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|jexl
operator|.
name|Expression
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|jexl
operator|.
name|ExpressionFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|jexl
operator|.
name|JexlContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|jexl
operator|.
name|JexlHelper
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
name|plan
operator|.
name|exprNodeConstantDesc
import|;
end_import

begin_class
specifier|public
class|class
name|TestJEXL
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testJEXL
parameter_list|()
throws|throws
name|Exception
block|{
name|Integer
name|a
init|=
name|Integer
operator|.
name|valueOf
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|Integer
name|b
init|=
name|Integer
operator|.
name|valueOf
argument_list|(
literal|8
argument_list|)
decl_stmt|;
name|JexlContext
name|jc
init|=
name|JexlHelper
operator|.
name|createContext
argument_list|()
decl_stmt|;
name|jc
operator|.
name|getVars
argument_list|()
operator|.
name|put
argument_list|(
literal|"a"
argument_list|,
name|a
argument_list|)
expr_stmt|;
name|jc
operator|.
name|getVars
argument_list|()
operator|.
name|put
argument_list|(
literal|"b"
argument_list|,
name|b
argument_list|)
expr_stmt|;
try|try
block|{
name|Expression
name|e
init|=
name|ExpressionFactory
operator|.
name|createExpression
argument_list|(
literal|"a+b"
argument_list|)
decl_stmt|;
name|Object
name|o
init|=
name|e
operator|.
name|evaluate
argument_list|(
name|jc
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|o
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
literal|13
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"JEXL library test ok"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
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
name|Expression
name|eval
parameter_list|,
name|JexlContext
name|input
parameter_list|,
name|Object
name|output
parameter_list|)
throws|throws
name|Exception
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
comment|// evaluate on row
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
name|Object
name|ret
init|=
name|eval
operator|.
name|evaluate
argument_list|(
name|input
argument_list|)
decl_stmt|;
comment|// System.out.println("" + ret.getClass() + " " + ret);
name|assertEquals
argument_list|(
name|ret
argument_list|,
name|output
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
name|testJEXLSpeed
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|int
name|basetimes
init|=
literal|100000
decl_stmt|;
name|JexlContext
name|jc
init|=
name|JexlHelper
operator|.
name|createContext
argument_list|()
decl_stmt|;
name|jc
operator|.
name|getVars
argument_list|()
operator|.
name|put
argument_list|(
literal|"__udf__concat"
argument_list|,
name|FunctionRegistry
operator|.
name|getUDFClass
argument_list|(
literal|"concat"
argument_list|)
operator|.
name|newInstance
argument_list|()
argument_list|)
expr_stmt|;
name|measureSpeed
argument_list|(
literal|"1 + 2"
argument_list|,
name|basetimes
operator|*
literal|100
argument_list|,
name|ExpressionFactory
operator|.
name|createExpression
argument_list|(
literal|"1 + 2"
argument_list|)
argument_list|,
name|jc
argument_list|,
name|Long
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
literal|"__udf__concat.evaluate(\"1\", \"2\")"
argument_list|,
name|basetimes
operator|*
literal|10
argument_list|,
name|ExpressionFactory
operator|.
name|createExpression
argument_list|(
literal|"__udf__concat.evaluate(\"1\", \"2\")"
argument_list|)
argument_list|,
name|jc
argument_list|,
literal|"12"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
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
name|testExprNodeFuncEvaluator
parameter_list|()
throws|throws
name|Exception
block|{   }
block|}
end_class

end_unit

