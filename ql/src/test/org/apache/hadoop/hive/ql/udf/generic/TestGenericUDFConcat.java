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
name|udf
operator|.
name|generic
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|UDFArgumentException
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
name|testutil
operator|.
name|BaseScalarUdfTest
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
name|testutil
operator|.
name|DataBuilder
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
name|testutil
operator|.
name|OperatorTestUtils
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
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
import|;
end_import

begin_class
specifier|public
class|class
name|TestGenericUDFConcat
extends|extends
name|BaseScalarUdfTest
block|{
annotation|@
name|Override
specifier|public
name|InspectableObject
index|[]
name|getBaseTable
parameter_list|()
block|{
name|DataBuilder
name|db
init|=
operator|new
name|DataBuilder
argument_list|()
decl_stmt|;
name|db
operator|.
name|setColumnNames
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"c"
argument_list|)
expr_stmt|;
name|db
operator|.
name|setColumnTypes
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|)
expr_stmt|;
name|db
operator|.
name|addRow
argument_list|(
literal|"one"
argument_list|,
literal|"two"
argument_list|,
literal|"three"
argument_list|)
expr_stmt|;
name|db
operator|.
name|addRow
argument_list|(
literal|"four"
argument_list|,
literal|"two"
argument_list|,
literal|"three"
argument_list|)
expr_stmt|;
name|db
operator|.
name|addRow
argument_list|(
literal|null
argument_list|,
literal|"two"
argument_list|,
literal|"three"
argument_list|)
expr_stmt|;
return|return
name|db
operator|.
name|createRows
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|InspectableObject
index|[]
name|getExpectedResult
parameter_list|()
block|{
name|DataBuilder
name|db
init|=
operator|new
name|DataBuilder
argument_list|()
decl_stmt|;
name|db
operator|.
name|setColumnNames
argument_list|(
literal|"_col1"
argument_list|,
literal|"_col2"
argument_list|)
expr_stmt|;
name|db
operator|.
name|setColumnTypes
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|)
expr_stmt|;
name|db
operator|.
name|addRow
argument_list|(
literal|"one"
argument_list|,
literal|"onetwo"
argument_list|)
expr_stmt|;
name|db
operator|.
name|addRow
argument_list|(
literal|"four"
argument_list|,
literal|"fourtwo"
argument_list|)
expr_stmt|;
name|db
operator|.
name|addRow
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|db
operator|.
name|createRows
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|getExpressionList
parameter_list|()
throws|throws
name|UDFArgumentException
block|{
name|ExprNodeDesc
name|expr1
init|=
name|OperatorTestUtils
operator|.
name|getStringColumn
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
name|ExprNodeDesc
name|expr2
init|=
name|OperatorTestUtils
operator|.
name|getStringColumn
argument_list|(
literal|"b"
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
name|List
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
name|expr1
argument_list|)
expr_stmt|;
name|earr
operator|.
name|add
argument_list|(
name|exprDesc2
argument_list|)
expr_stmt|;
return|return
name|earr
return|;
block|}
block|}
end_class

end_unit

