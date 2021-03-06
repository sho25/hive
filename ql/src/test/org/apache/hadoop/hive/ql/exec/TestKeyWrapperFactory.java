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
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|KeyWrapperFactory
operator|.
name|ListKeyWrapper
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
name|KeyWrapperFactory
operator|.
name|TextKeyWrapper
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|TestKeyWrapperFactory
block|{
specifier|private
name|KeyWrapperFactory
name|factory
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
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
name|ArrayList
argument_list|<
name|Text
argument_list|>
name|col1
init|=
operator|new
name|ArrayList
argument_list|<
name|Text
argument_list|>
argument_list|()
decl_stmt|;
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
name|TypeInfo
name|col1Type
init|=
name|TypeInfoFactory
operator|.
name|getListTypeInfo
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Text
argument_list|>
name|cola
init|=
operator|new
name|ArrayList
argument_list|<
name|Text
argument_list|>
argument_list|()
decl_stmt|;
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
name|TypeInfo
name|colaType
init|=
name|TypeInfoFactory
operator|.
name|getListTypeInfo
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
decl_stmt|;
try|try
block|{
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|data
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
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
argument_list|()
decl_stmt|;
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
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
name|typeInfos
init|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
decl_stmt|;
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
name|TypeInfo
name|dataType
init|=
name|TypeInfoFactory
operator|.
name|getStructTypeInfo
argument_list|(
name|names
argument_list|,
name|typeInfos
argument_list|)
decl_stmt|;
name|InspectableObject
name|r
init|=
operator|new
name|InspectableObject
argument_list|()
decl_stmt|;
name|ObjectInspector
index|[]
name|oi
init|=
operator|new
name|ObjectInspector
index|[
literal|1
index|]
decl_stmt|;
name|r
operator|.
name|o
operator|=
name|data
expr_stmt|;
name|oi
index|[
literal|0
index|]
operator|=
name|TypeInfoUtils
operator|.
name|getStandardWritableObjectInspectorFromTypeInfo
argument_list|(
name|dataType
argument_list|)
expr_stmt|;
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
name|ExprNodeEvaluator
index|[]
name|evals
init|=
operator|new
name|ExprNodeEvaluator
index|[
literal|1
index|]
decl_stmt|;
name|evals
index|[
literal|0
index|]
operator|=
name|eval
expr_stmt|;
name|ObjectInspector
name|resultOI
init|=
name|eval
operator|.
name|initialize
argument_list|(
name|oi
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|ObjectInspector
index|[]
name|resultOIs
init|=
operator|new
name|ObjectInspector
index|[
literal|1
index|]
decl_stmt|;
name|resultOIs
index|[
literal|0
index|]
operator|=
name|resultOI
expr_stmt|;
name|factory
operator|=
operator|new
name|KeyWrapperFactory
argument_list|(
name|evals
argument_list|,
name|oi
argument_list|,
name|resultOIs
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
name|Test
specifier|public
name|void
name|testKeyWrapperEqualsCopy
parameter_list|()
throws|throws
name|Exception
block|{
name|KeyWrapper
name|w1
init|=
name|factory
operator|.
name|getKeyWrapper
argument_list|()
decl_stmt|;
name|KeyWrapper
name|w2
init|=
name|w1
operator|.
name|copyKey
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|w1
operator|.
name|equals
argument_list|(
name|w2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDifferentWrapperTypesUnequal
parameter_list|()
block|{
name|TextKeyWrapper
name|w3
init|=
name|factory
operator|.
expr|new
name|TextKeyWrapper
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|ListKeyWrapper
name|w4
init|=
name|factory
operator|.
expr|new
name|ListKeyWrapper
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|w3
operator|.
name|equals
argument_list|(
name|w4
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|w4
operator|.
name|equals
argument_list|(
name|w3
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUnsetHashCode
parameter_list|()
block|{
name|KeyWrapper
name|w1
init|=
name|factory
operator|.
name|getKeyWrapper
argument_list|()
decl_stmt|;
name|KeyWrapper
name|w2
init|=
name|w1
operator|.
name|copyKey
argument_list|()
decl_stmt|;
name|w1
operator|.
name|setHashKey
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|w1
operator|.
name|equals
argument_list|(
name|w2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

