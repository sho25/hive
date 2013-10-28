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
name|serde2
operator|.
name|objectinspector
operator|.
name|primitive
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
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
import|;
end_import

begin_class
specifier|public
class|class
name|TestPrimitiveObjectInspectorFactory
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testGetPrimitiveWritableObjectInspector
parameter_list|()
block|{
comment|// even without type params, return a default OI for varchar
name|PrimitiveObjectInspector
name|poi
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|VARCHAR
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|poi
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableHiveVarcharObjectInspector
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testGetPrimitiveJavaObjectInspector
parameter_list|()
block|{
comment|// even without type params, return a default OI for varchar
name|PrimitiveObjectInspector
name|poi
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveCategory
operator|.
name|VARCHAR
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|poi
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaHiveVarcharObjectInspector
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

