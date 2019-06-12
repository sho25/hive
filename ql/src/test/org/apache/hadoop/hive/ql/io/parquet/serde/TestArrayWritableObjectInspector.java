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
name|io
operator|.
name|parquet
operator|.
name|serde
package|;
end_package

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
name|StructTypeInfo
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
name|junit
operator|.
name|Assert
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

begin_comment
comment|/**  * Tests for ArrayWritableObjectInspector. At the moment only behavior related to HIVE-21796 covered.  */
end_comment

begin_class
specifier|public
class|class
name|TestArrayWritableObjectInspector
extends|extends
name|TestCase
block|{
specifier|private
name|StructTypeInfo
name|nestOnce
parameter_list|(
name|TypeInfo
name|nestedType
parameter_list|)
block|{
return|return
operator|(
name|StructTypeInfo
operator|)
name|TypeInfoFactory
operator|.
name|getStructTypeInfo
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"value"
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|nestedType
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|StructTypeInfo
name|createNestedStruct
parameter_list|(
name|TypeInfo
name|nestedType
parameter_list|,
name|int
name|depth
parameter_list|)
block|{
name|StructTypeInfo
name|result
init|=
name|nestOnce
argument_list|(
name|nestedType
argument_list|)
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
name|depth
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|=
name|nestOnce
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/** Regression tests for HIVE-21796: equals and hash takes forever if HIVE-21796 is reverted / reintroduced. */
specifier|public
name|void
name|testIdenticalInspectorsEquals
parameter_list|()
block|{
name|StructTypeInfo
name|nestedStruct
init|=
name|createNestedStruct
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"int"
argument_list|)
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|ArrayWritableObjectInspector
name|inspectorX
init|=
operator|new
name|ArrayWritableObjectInspector
argument_list|(
literal|true
argument_list|,
name|nestedStruct
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ArrayWritableObjectInspector
name|inspectorY
init|=
operator|new
name|ArrayWritableObjectInspector
argument_list|(
literal|true
argument_list|,
name|nestedStruct
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|inspectorX
argument_list|,
name|inspectorY
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|inspectorX
operator|.
name|hashCode
argument_list|()
argument_list|,
name|inspectorY
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** Regression tests for HIVE-21796: equals and hash takes forever if HIVE-21796 is reverted / reintroduced. */
specifier|public
name|void
name|testEqualInspectorsEquals
parameter_list|()
block|{
name|StructTypeInfo
name|nestedStructX
init|=
name|createNestedStruct
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"int"
argument_list|)
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|StructTypeInfo
name|nestedStructY
init|=
name|createNestedStruct
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"int"
argument_list|)
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|ArrayWritableObjectInspector
name|inspectorX
init|=
operator|new
name|ArrayWritableObjectInspector
argument_list|(
literal|true
argument_list|,
name|nestedStructX
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ArrayWritableObjectInspector
name|inspectorY
init|=
operator|new
name|ArrayWritableObjectInspector
argument_list|(
literal|true
argument_list|,
name|nestedStructY
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|inspectorX
argument_list|,
name|inspectorY
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|inspectorX
operator|.
name|hashCode
argument_list|()
argument_list|,
name|inspectorY
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** Regression tests for HIVE-21796: equals and hash takes forever if HIVE-21796 is reverted / reintroduced. */
specifier|public
name|void
name|testDifferentInspectorsEquals
parameter_list|()
block|{
name|StructTypeInfo
name|nestedStructX
init|=
name|createNestedStruct
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"int"
argument_list|)
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|StructTypeInfo
name|nestedStructY
init|=
name|createNestedStruct
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
literal|"bigint"
argument_list|)
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|ArrayWritableObjectInspector
name|inspectorX
init|=
operator|new
name|ArrayWritableObjectInspector
argument_list|(
literal|true
argument_list|,
name|nestedStructX
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ArrayWritableObjectInspector
name|inspectorY
init|=
operator|new
name|ArrayWritableObjectInspector
argument_list|(
literal|true
argument_list|,
name|nestedStructY
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotEquals
argument_list|(
name|inspectorX
argument_list|,
name|inspectorY
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotEquals
argument_list|(
name|inspectorX
operator|.
name|hashCode
argument_list|()
argument_list|,
name|inspectorY
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

