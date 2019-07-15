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
name|errors
operator|.
name|DataConstraintViolationError
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
name|BooleanWritable
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
name|fail
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
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * Test class for {@link GenericUDFEnforceConstraint}.  */
end_comment

begin_class
specifier|public
class|class
name|TestGenericUDFEnforceConstraint
block|{
annotation|@
name|Test
specifier|public
name|void
name|testNull
parameter_list|()
throws|throws
name|HiveException
block|{
try|try
block|{
name|GenericUDFEnforceConstraint
name|udf
init|=
operator|new
name|GenericUDFEnforceConstraint
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
decl_stmt|;
name|ObjectInspector
index|[]
name|arguments
init|=
block|{
name|valueOI
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
name|BooleanWritable
name|input
init|=
operator|new
name|BooleanWritable
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|GenericUDF
operator|.
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|GenericUDF
operator|.
name|DeferredJavaObject
argument_list|(
name|input
argument_list|)
block|}
decl_stmt|;
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Unreachable line"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|DataConstraintViolationError
name|e
parameter_list|)
block|{
comment|//DataConstraintViolationError is expected
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"NOT NULL constraint violated!"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInvalidArgumentsLength
parameter_list|()
throws|throws
name|HiveException
block|{
try|try
block|{
name|GenericUDFEnforceConstraint
name|udf
init|=
operator|new
name|GenericUDFEnforceConstraint
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI1
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
decl_stmt|;
name|ObjectInspector
name|valueOI2
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
decl_stmt|;
name|ObjectInspector
index|[]
name|arguments
init|=
block|{
name|valueOI1
block|,
name|valueOI2
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Unreachable line"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
comment|//HiveException is expected
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Invalid number of arguments"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCorrect
parameter_list|()
throws|throws
name|HiveException
block|{
name|GenericUDFEnforceConstraint
name|udf
init|=
operator|new
name|GenericUDFEnforceConstraint
argument_list|()
decl_stmt|;
name|ObjectInspector
name|valueOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
decl_stmt|;
name|ObjectInspector
index|[]
name|arguments
init|=
block|{
name|valueOI
block|}
decl_stmt|;
name|udf
operator|.
name|initialize
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
name|BooleanWritable
name|input
init|=
operator|new
name|BooleanWritable
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|GenericUDF
operator|.
name|DeferredObject
index|[]
name|args
init|=
block|{
operator|new
name|GenericUDF
operator|.
name|DeferredJavaObject
argument_list|(
name|input
argument_list|)
block|}
decl_stmt|;
name|BooleanWritable
name|writable
init|=
operator|(
name|BooleanWritable
operator|)
name|udf
operator|.
name|evaluate
argument_list|(
name|args
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Not expected result: expected [true] actual  [ "
operator|+
name|writable
operator|.
name|get
argument_list|()
operator|+
literal|" ]"
argument_list|,
name|writable
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

