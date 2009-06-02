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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
operator|.
name|Category
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
name|hive
operator|.
name|serde2
operator|.
name|thrift
operator|.
name|test
operator|.
name|Complex
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
name|thrift
operator|.
name|test
operator|.
name|IntString
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

begin_class
specifier|public
class|class
name|TestObjectInspectorUtils
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testObjectInspectorUtils
parameter_list|()
throws|throws
name|Throwable
block|{
try|try
block|{
name|ObjectInspector
name|oi1
init|=
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|Complex
operator|.
name|class
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|THRIFT
argument_list|)
decl_stmt|;
comment|// metadata
name|assertEquals
argument_list|(
name|Category
operator|.
name|STRUCT
argument_list|,
name|oi1
operator|.
name|getCategory
argument_list|()
argument_list|)
expr_stmt|;
comment|// standard ObjectInspector
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|oi1
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fields
init|=
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|fields
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|fields
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|soi
operator|.
name|getStructFieldRef
argument_list|(
literal|"aint"
argument_list|)
argument_list|)
expr_stmt|;
comment|// null
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertNull
argument_list|(
name|soi
operator|.
name|getStructFieldData
argument_list|(
literal|null
argument_list|,
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// real object
name|Complex
name|cc
init|=
operator|new
name|Complex
argument_list|()
decl_stmt|;
name|cc
operator|.
name|aint
operator|=
literal|1
expr_stmt|;
name|cc
operator|.
name|aString
operator|=
literal|"test"
expr_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|c2
init|=
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Integer
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|}
argument_list|)
decl_stmt|;
name|cc
operator|.
name|lint
operator|=
name|c2
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|c3
init|=
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"one"
block|,
literal|"two"
block|}
argument_list|)
decl_stmt|;
name|cc
operator|.
name|lString
operator|=
name|c3
expr_stmt|;
name|List
argument_list|<
name|IntString
argument_list|>
name|c4
init|=
operator|new
name|ArrayList
argument_list|<
name|IntString
argument_list|>
argument_list|()
decl_stmt|;
name|cc
operator|.
name|lintString
operator|=
name|c4
expr_stmt|;
name|cc
operator|.
name|mStringString
operator|=
literal|null
expr_stmt|;
comment|// standard object
name|Object
name|c
init|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|cc
argument_list|,
name|oi1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|c
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"test"
argument_list|,
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|c
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|c2
argument_list|,
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|c
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|c3
argument_list|,
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|c
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|c4
argument_list|,
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|c
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|4
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|c
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|cfields
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
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
literal|6
condition|;
name|i
operator|++
control|)
block|{
name|cfields
operator|.
name|add
argument_list|(
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|c
argument_list|,
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|cfields
argument_list|,
name|soi
operator|.
name|getStructFieldsDataAsList
argument_list|(
name|c
argument_list|)
argument_list|)
expr_stmt|;
comment|// sub fields
name|assertEquals
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaIntObjectInspector
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaIntObjectInspector
argument_list|)
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|)
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getReflectionObjectInspector
argument_list|(
name|IntString
operator|.
name|class
argument_list|,
name|ObjectInspectorFactory
operator|.
name|ObjectInspectorOptions
operator|.
name|THRIFT
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|4
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getStandardMapObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|)
argument_list|,
name|fields
operator|.
name|get
argument_list|(
literal|5
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
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

