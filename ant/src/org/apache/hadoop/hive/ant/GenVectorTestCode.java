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
name|ant
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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

begin_comment
comment|/**  *  * GenVectorTestCode.  * This class is mutable and maintains a hashmap of TestSuiteClassName to test cases.  * The tests cases are added over the course of vectorized expressions class generation,  * with test classes being outputted at the end. For each column vector (inputs and/or outputs)  * a matrix of pairwise covering Booleans is used to generate test cases across nulls and  * repeating dimensions. Based on the input column vector(s) nulls and repeating states  * the states of the output column vector (if there is one) is validated, along with the null  * vector. For filter operations the selection vector is validated against the generated  * data. Each template corresponds to a class representing a test suite.  */
end_comment

begin_class
specifier|public
class|class
name|GenVectorTestCode
block|{
specifier|public
enum|enum
name|TestSuiteClassName
block|{
name|TestColumnScalarOperationVectorExpressionEvaluation
block|,
name|TestColumnScalarFilterVectorExpressionEvaluation
block|,
name|TestColumnColumnOperationVectorExpressionEvaluation
block|,
name|TestColumnColumnFilterVectorExpressionEvaluation
block|,   }
specifier|private
specifier|final
name|String
name|testOutputDir
decl_stmt|;
specifier|private
specifier|final
name|String
name|testTemplateDirectory
decl_stmt|;
specifier|private
specifier|final
name|HashMap
argument_list|<
name|TestSuiteClassName
argument_list|,
name|StringBuilder
argument_list|>
name|testsuites
decl_stmt|;
specifier|public
name|GenVectorTestCode
parameter_list|(
name|String
name|testOutputDir
parameter_list|,
name|String
name|testTemplateDirectory
parameter_list|)
block|{
name|this
operator|.
name|testOutputDir
operator|=
name|testOutputDir
expr_stmt|;
name|this
operator|.
name|testTemplateDirectory
operator|=
name|testTemplateDirectory
expr_stmt|;
name|testsuites
operator|=
operator|new
name|HashMap
argument_list|<
name|TestSuiteClassName
argument_list|,
name|StringBuilder
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|TestSuiteClassName
name|className
range|:
name|TestSuiteClassName
operator|.
name|values
argument_list|()
control|)
block|{
name|testsuites
operator|.
name|put
argument_list|(
name|className
argument_list|,
operator|new
name|StringBuilder
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|addColumnScalarOperationTestCases
parameter_list|(
name|boolean
name|op1IsCol
parameter_list|,
name|String
name|vectorExpClassName
parameter_list|,
name|String
name|inputColumnVectorType
parameter_list|,
name|String
name|outputColumnVectorType
parameter_list|,
name|String
name|scalarType
parameter_list|)
throws|throws
name|IOException
block|{
name|TestSuiteClassName
name|template
init|=
name|TestSuiteClassName
operator|.
name|TestColumnScalarOperationVectorExpressionEvaluation
decl_stmt|;
comment|//Read the template into a string;
name|String
name|templateFile
init|=
name|GenVectorCode
operator|.
name|joinPath
argument_list|(
name|this
operator|.
name|testTemplateDirectory
argument_list|,
name|template
operator|.
name|toString
argument_list|()
operator|+
literal|".txt"
argument_list|)
decl_stmt|;
name|String
name|templateString
init|=
name|removeTemplateComments
argument_list|(
name|GenVectorCode
operator|.
name|readFile
argument_list|(
name|templateFile
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Boolean
index|[]
name|testMatrix
range|:
operator|new
name|Boolean
index|[]
index|[]
block|{
comment|// Pairwise: InitOuputColHasNulls, InitOuputColIsRepeating, ColumnHasNulls, ColumnIsRepeating
block|{
literal|false
block|,
literal|true
block|,
literal|true
block|,
literal|true
block|}
block|,
block|{
literal|false
block|,
literal|false
block|,
literal|false
block|,
literal|false
block|}
block|,
block|{
literal|true
block|,
literal|false
block|,
literal|true
block|,
literal|false
block|}
block|,
block|{
literal|true
block|,
literal|true
block|,
literal|false
block|,
literal|false
block|}
block|,
block|{
literal|true
block|,
literal|false
block|,
literal|false
block|,
literal|true
block|}
block|}
control|)
block|{
name|String
name|testCase
init|=
name|templateString
decl_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<TestName>"
argument_list|,
literal|"test"
operator|+
name|vectorExpClassName
operator|+
name|createNullRepeatingNameFragment
argument_list|(
literal|"Out"
argument_list|,
name|testMatrix
index|[
literal|0
index|]
argument_list|,
name|testMatrix
index|[
literal|1
index|]
argument_list|)
operator|+
name|createNullRepeatingNameFragment
argument_list|(
literal|"Col"
argument_list|,
name|testMatrix
index|[
literal|2
index|]
argument_list|,
name|testMatrix
index|[
literal|3
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<VectorExpClassName>"
argument_list|,
name|vectorExpClassName
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<InputColumnVectorType>"
argument_list|,
name|inputColumnVectorType
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<OutputColumnVectorType>"
argument_list|,
name|outputColumnVectorType
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<ScalarType>"
argument_list|,
name|scalarType
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<CamelCaseScalarType>"
argument_list|,
name|GenVectorCode
operator|.
name|getCamelCaseType
argument_list|(
name|scalarType
argument_list|)
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<InitOuputColHasNulls>"
argument_list|,
name|testMatrix
index|[
literal|0
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<InitOuputColIsRepeating>"
argument_list|,
name|testMatrix
index|[
literal|1
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<ColumnHasNulls>"
argument_list|,
name|testMatrix
index|[
literal|2
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<ColumnIsRepeating>"
argument_list|,
name|testMatrix
index|[
literal|3
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|op1IsCol
condition|)
block|{
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<ConstructorParams>"
argument_list|,
literal|"0, scalarValue"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<ConstructorParams>"
argument_list|,
literal|"scalarValue, 0"
argument_list|)
expr_stmt|;
block|}
name|testsuites
operator|.
name|get
argument_list|(
name|template
argument_list|)
operator|.
name|append
argument_list|(
name|testCase
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|addColumnScalarFilterTestCases
parameter_list|(
name|boolean
name|op1IsCol
parameter_list|,
name|String
name|vectorExpClassName
parameter_list|,
name|String
name|inputColumnVectorType
parameter_list|,
name|String
name|scalarType
parameter_list|,
name|String
name|operatorSymbol
parameter_list|)
throws|throws
name|IOException
block|{
name|TestSuiteClassName
name|template
init|=
name|TestSuiteClassName
operator|.
name|TestColumnScalarFilterVectorExpressionEvaluation
decl_stmt|;
comment|//Read the template into a string;
name|String
name|templateFile
init|=
name|GenVectorCode
operator|.
name|joinPath
argument_list|(
name|this
operator|.
name|testTemplateDirectory
argument_list|,
name|template
operator|.
name|toString
argument_list|()
operator|+
literal|".txt"
argument_list|)
decl_stmt|;
name|String
name|templateString
init|=
name|removeTemplateComments
argument_list|(
name|GenVectorCode
operator|.
name|readFile
argument_list|(
name|templateFile
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Boolean
index|[]
name|testMatrix
range|:
operator|new
name|Boolean
index|[]
index|[]
block|{
comment|// Pairwise: ColumnHasNulls, ColumnIsRepeating
block|{
literal|true
block|,
literal|true
block|}
block|,
block|{
literal|true
block|,
literal|false
block|}
block|,
block|{
literal|false
block|,
literal|false
block|}
block|,
block|{
literal|false
block|,
literal|true
block|}
block|}
control|)
block|{
name|String
name|testCase
init|=
name|templateString
decl_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<TestName>"
argument_list|,
literal|"test"
operator|+
name|vectorExpClassName
operator|+
name|createNullRepeatingNameFragment
argument_list|(
literal|"Col"
argument_list|,
name|testMatrix
index|[
literal|0
index|]
argument_list|,
name|testMatrix
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<VectorExpClassName>"
argument_list|,
name|vectorExpClassName
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<InputColumnVectorType>"
argument_list|,
name|inputColumnVectorType
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<ScalarType>"
argument_list|,
name|scalarType
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<CamelCaseScalarType>"
argument_list|,
name|GenVectorCode
operator|.
name|getCamelCaseType
argument_list|(
name|scalarType
argument_list|)
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<ColumnHasNulls>"
argument_list|,
name|testMatrix
index|[
literal|0
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<ColumnIsRepeating>"
argument_list|,
name|testMatrix
index|[
literal|1
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<Operator>"
argument_list|,
name|operatorSymbol
argument_list|)
expr_stmt|;
if|if
condition|(
name|op1IsCol
condition|)
block|{
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<Operand1>"
argument_list|,
literal|"inputColumnVector.vector[i]"
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<Operand2>"
argument_list|,
literal|"scalarValue"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<Operand1>"
argument_list|,
literal|"scalarValue"
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<Operand2>"
argument_list|,
literal|"inputColumnVector.vector[i]"
argument_list|)
expr_stmt|;
block|}
name|testsuites
operator|.
name|get
argument_list|(
name|template
argument_list|)
operator|.
name|append
argument_list|(
name|testCase
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|addColumnColumnOperationTestCases
parameter_list|(
name|String
name|vectorExpClassName
parameter_list|,
name|String
name|inputColumnVectorType1
parameter_list|,
name|String
name|inputColumnVectorType2
parameter_list|,
name|String
name|outputColumnVectorType
parameter_list|)
throws|throws
name|IOException
block|{
name|TestSuiteClassName
name|template
init|=
name|TestSuiteClassName
operator|.
name|TestColumnColumnOperationVectorExpressionEvaluation
decl_stmt|;
comment|//Read the template into a string;
name|String
name|templateFile
init|=
name|GenVectorCode
operator|.
name|joinPath
argument_list|(
name|this
operator|.
name|testTemplateDirectory
argument_list|,
name|template
operator|.
name|toString
argument_list|()
operator|+
literal|".txt"
argument_list|)
decl_stmt|;
name|String
name|templateString
init|=
name|removeTemplateComments
argument_list|(
name|GenVectorCode
operator|.
name|readFile
argument_list|(
name|templateFile
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Boolean
index|[]
name|testMatrix
range|:
operator|new
name|Boolean
index|[]
index|[]
block|{
comment|// Pairwise: InitOuputColHasNulls, InitOuputColIsRepeating, Column1HasNulls,
comment|// Column1IsRepeating, Column2HasNulls, Column2IsRepeating
block|{
literal|true
block|,
literal|true
block|,
literal|false
block|,
literal|true
block|,
literal|true
block|,
literal|true
block|}
block|,
block|{
literal|false
block|,
literal|false
block|,
literal|true
block|,
literal|false
block|,
literal|false
block|,
literal|false
block|}
block|,
block|{
literal|true
block|,
literal|false
block|,
literal|true
block|,
literal|false
block|,
literal|true
block|,
literal|true
block|}
block|,
block|{
literal|true
block|,
literal|true
block|,
literal|true
block|,
literal|true
block|,
literal|false
block|,
literal|false
block|}
block|,
block|{
literal|false
block|,
literal|false
block|,
literal|false
block|,
literal|true
block|,
literal|true
block|,
literal|false
block|}
block|,
block|{
literal|false
block|,
literal|true
block|,
literal|false
block|,
literal|false
block|,
literal|false
block|,
literal|true
block|}
block|}
control|)
block|{
name|String
name|testCase
init|=
name|templateString
decl_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<TestName>"
argument_list|,
literal|"test"
operator|+
name|vectorExpClassName
operator|+
name|createNullRepeatingNameFragment
argument_list|(
literal|"Out"
argument_list|,
name|testMatrix
index|[
literal|0
index|]
argument_list|,
name|testMatrix
index|[
literal|1
index|]
argument_list|)
operator|+
name|createNullRepeatingNameFragment
argument_list|(
literal|"C1"
argument_list|,
name|testMatrix
index|[
literal|2
index|]
argument_list|,
name|testMatrix
index|[
literal|3
index|]
argument_list|)
operator|+
name|createNullRepeatingNameFragment
argument_list|(
literal|"C2"
argument_list|,
name|testMatrix
index|[
literal|4
index|]
argument_list|,
name|testMatrix
index|[
literal|5
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<VectorExpClassName>"
argument_list|,
name|vectorExpClassName
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<InputColumnVectorType1>"
argument_list|,
name|inputColumnVectorType1
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<InputColumnVectorType2>"
argument_list|,
name|inputColumnVectorType2
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<OutputColumnVectorType>"
argument_list|,
name|outputColumnVectorType
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<InitOuputColHasNulls>"
argument_list|,
name|testMatrix
index|[
literal|0
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<InitOuputColIsRepeating>"
argument_list|,
name|testMatrix
index|[
literal|1
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<Column1HasNulls>"
argument_list|,
name|testMatrix
index|[
literal|2
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<Column1IsRepeating>"
argument_list|,
name|testMatrix
index|[
literal|3
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<Column2HasNulls>"
argument_list|,
name|testMatrix
index|[
literal|4
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<Column2IsRepeating>"
argument_list|,
name|testMatrix
index|[
literal|5
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testsuites
operator|.
name|get
argument_list|(
name|template
argument_list|)
operator|.
name|append
argument_list|(
name|testCase
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|addColumnColumnFilterTestCases
parameter_list|(
name|String
name|vectorExpClassName
parameter_list|,
name|String
name|inputColumnVectorType1
parameter_list|,
name|String
name|inputColumnVectorType2
parameter_list|,
name|String
name|operatorSymbol
parameter_list|)
throws|throws
name|IOException
block|{
name|TestSuiteClassName
name|template
init|=
name|TestSuiteClassName
operator|.
name|TestColumnColumnFilterVectorExpressionEvaluation
decl_stmt|;
comment|//Read the template into a string;
name|String
name|templateFile
init|=
name|GenVectorCode
operator|.
name|joinPath
argument_list|(
name|this
operator|.
name|testTemplateDirectory
argument_list|,
name|template
operator|.
name|toString
argument_list|()
operator|+
literal|".txt"
argument_list|)
decl_stmt|;
name|String
name|templateString
init|=
name|removeTemplateComments
argument_list|(
name|GenVectorCode
operator|.
name|readFile
argument_list|(
name|templateFile
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Boolean
index|[]
name|testMatrix
range|:
operator|new
name|Boolean
index|[]
index|[]
block|{
comment|// Pairwise: Column1HasNulls, Column1IsRepeating, Column2HasNulls, Column2IsRepeating
block|{
literal|false
block|,
literal|true
block|,
literal|true
block|,
literal|true
block|}
block|,
block|{
literal|false
block|,
literal|false
block|,
literal|false
block|,
literal|false
block|}
block|,
block|{
literal|true
block|,
literal|false
block|,
literal|true
block|,
literal|false
block|}
block|,
block|{
literal|true
block|,
literal|true
block|,
literal|false
block|,
literal|false
block|}
block|,
block|{
literal|true
block|,
literal|false
block|,
literal|false
block|,
literal|true
block|}
block|}
control|)
block|{
name|String
name|testCase
init|=
name|templateString
decl_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<TestName>"
argument_list|,
literal|"test"
operator|+
name|vectorExpClassName
operator|+
name|createNullRepeatingNameFragment
argument_list|(
literal|"C1"
argument_list|,
name|testMatrix
index|[
literal|0
index|]
argument_list|,
name|testMatrix
index|[
literal|1
index|]
argument_list|)
operator|+
name|createNullRepeatingNameFragment
argument_list|(
literal|"C2"
argument_list|,
name|testMatrix
index|[
literal|2
index|]
argument_list|,
name|testMatrix
index|[
literal|3
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<VectorExpClassName>"
argument_list|,
name|vectorExpClassName
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<InputColumnVectorType1>"
argument_list|,
name|inputColumnVectorType1
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<InputColumnVectorType2>"
argument_list|,
name|inputColumnVectorType2
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<Column1HasNulls>"
argument_list|,
name|testMatrix
index|[
literal|0
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<Column1IsRepeating>"
argument_list|,
name|testMatrix
index|[
literal|1
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<Column2HasNulls>"
argument_list|,
name|testMatrix
index|[
literal|2
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<Column2IsRepeating>"
argument_list|,
name|testMatrix
index|[
literal|3
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|testCase
operator|=
name|testCase
operator|.
name|replaceAll
argument_list|(
literal|"<Operator>"
argument_list|,
name|operatorSymbol
argument_list|)
expr_stmt|;
name|testsuites
operator|.
name|get
argument_list|(
name|template
argument_list|)
operator|.
name|append
argument_list|(
name|testCase
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|generateTestSuites
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|templateFile
init|=
name|GenVectorCode
operator|.
name|joinPath
argument_list|(
name|this
operator|.
name|testTemplateDirectory
argument_list|,
literal|"TestClass.txt"
argument_list|)
decl_stmt|;
for|for
control|(
name|TestSuiteClassName
name|testClass
range|:
name|testsuites
operator|.
name|keySet
argument_list|()
control|)
block|{
name|String
name|templateString
init|=
name|GenVectorCode
operator|.
name|readFile
argument_list|(
name|templateFile
argument_list|)
decl_stmt|;
name|templateString
operator|=
name|templateString
operator|.
name|replaceAll
argument_list|(
literal|"<ClassName>"
argument_list|,
name|testClass
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|templateString
operator|=
name|templateString
operator|.
name|replaceAll
argument_list|(
literal|"<TestCases>"
argument_list|,
name|testsuites
operator|.
name|get
argument_list|(
name|testClass
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|outputFile
init|=
name|GenVectorCode
operator|.
name|joinPath
argument_list|(
name|this
operator|.
name|testOutputDir
argument_list|,
name|testClass
operator|+
literal|".java"
argument_list|)
decl_stmt|;
name|GenVectorCode
operator|.
name|writeFile
argument_list|(
name|outputFile
argument_list|,
name|templateString
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|String
name|createNullRepeatingNameFragment
parameter_list|(
name|String
name|idenitfier
parameter_list|,
name|boolean
name|nulls
parameter_list|,
name|boolean
name|repeating
parameter_list|)
block|{
if|if
condition|(
name|nulls
operator|||
name|repeating
condition|)
block|{
if|if
condition|(
name|nulls
condition|)
block|{
name|idenitfier
operator|+=
literal|"Nulls"
expr_stmt|;
block|}
if|if
condition|(
name|repeating
condition|)
block|{
name|idenitfier
operator|+=
literal|"Repeats"
expr_stmt|;
block|}
return|return
name|idenitfier
return|;
block|}
return|return
literal|""
return|;
block|}
specifier|private
specifier|static
name|String
name|removeTemplateComments
parameter_list|(
name|String
name|templateString
parameter_list|)
block|{
return|return
name|templateString
operator|.
name|replaceAll
argument_list|(
literal|"(?s)<!--(.*)-->"
argument_list|,
literal|""
argument_list|)
return|;
block|}
block|}
end_class

end_unit

