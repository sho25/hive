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
name|java
operator|.
name|util
operator|.
name|Properties
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
name|hive
operator|.
name|serde
operator|.
name|serdeConstants
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

begin_class
specifier|public
class|class
name|CustomSerDe5
extends|extends
name|CustomSerDe4
block|{
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// Read the configuration parameters
name|String
name|columnNameProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|)
decl_stmt|;
name|String
name|columnTypeProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|)
decl_stmt|;
comment|// The input column can either be a string or a list of integer values.
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|columnNameProperty
operator|.
name|split
argument_list|(
literal|","
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
init|=
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
name|columnTypeProperty
argument_list|)
decl_stmt|;
assert|assert
name|columnNames
operator|.
name|size
argument_list|()
operator|==
name|columnTypes
operator|.
name|size
argument_list|()
assert|;
name|numColumns
operator|=
name|columnNames
operator|.
name|size
argument_list|()
expr_stmt|;
comment|// No exception for type checking for simplicity
comment|// Constructing the row ObjectInspector:
comment|// The row consists of string columns, double columns, some union<int, double> columns only.
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|columnOIs
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|columnNames
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|numColumns
condition|;
name|c
operator|++
control|)
block|{
if|if
condition|(
name|columnTypes
operator|.
name|get
argument_list|(
name|c
argument_list|)
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|)
condition|)
block|{
name|columnOIs
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|columnTypes
operator|.
name|get
argument_list|(
name|c
argument_list|)
operator|.
name|equals
argument_list|(
name|TypeInfoFactory
operator|.
name|doubleTypeInfo
argument_list|)
condition|)
block|{
name|columnOIs
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaDoubleObjectInspector
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Blindly add this as a union type containing int and double!
comment|// Should be sufficient for the test case.
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|unionOI
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|unionOI
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaIntObjectInspector
argument_list|)
expr_stmt|;
name|unionOI
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaDoubleObjectInspector
argument_list|)
expr_stmt|;
name|columnOIs
operator|.
name|add
argument_list|(
operator|new
name|CustomNonSettableUnionObjectInspector1
argument_list|(
name|unionOI
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// StandardList uses ArrayList to store the row.
name|rowOI
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|columnNames
argument_list|,
name|columnOIs
argument_list|)
expr_stmt|;
comment|// Constructing the row object, etc, which will be reused for all rows.
name|row
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|numColumns
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|numColumns
condition|;
name|c
operator|++
control|)
block|{
name|row
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

