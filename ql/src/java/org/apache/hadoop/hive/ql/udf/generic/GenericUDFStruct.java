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
name|ql
operator|.
name|exec
operator|.
name|Description
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
name|ConstantObjectInspector
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
name|ObjectInspectorFactory
import|;
end_import

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"struct"
argument_list|,
name|value
operator|=
literal|"_FUNC_(col1, col2, col3, ...) - Creates a struct with the given field values"
argument_list|)
specifier|public
class|class
name|GenericUDFStruct
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|Object
index|[]
name|ret
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|int
name|numFields
init|=
name|arguments
operator|.
name|length
decl_stmt|;
name|ret
operator|=
operator|new
name|Object
index|[
name|numFields
index|]
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|fname
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|numFields
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|f
init|=
literal|1
init|;
name|f
operator|<=
name|numFields
condition|;
name|f
operator|++
control|)
block|{
name|fname
operator|.
name|add
argument_list|(
literal|"col"
operator|+
name|f
argument_list|)
expr_stmt|;
block|}
name|boolean
name|constantStruct
init|=
literal|true
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
name|arguments
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ObjectInspector
name|oi
init|=
name|arguments
index|[
name|i
index|]
decl_stmt|;
name|constantStruct
operator|&=
operator|(
name|oi
operator|.
name|getCategory
argument_list|()
operator|==
name|Category
operator|.
name|PRIMITIVE
operator|)
operator|&&
operator|(
name|oi
operator|instanceof
name|ConstantObjectInspector
operator|)
expr_stmt|;
if|if
condition|(
name|constantStruct
condition|)
block|{
comment|// nested complex types trigger Kryo issue #216 in plan deserialization
name|ret
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|ConstantObjectInspector
operator|)
name|oi
operator|)
operator|.
name|getWritableConstantValue
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|constantStruct
condition|)
block|{
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardConstantStructObjectInspector
argument_list|(
name|fname
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|arguments
argument_list|)
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|ret
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|fname
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|arguments
argument_list|)
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|)
throws|throws
name|HiveException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|arguments
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ret
index|[
name|i
index|]
operator|=
name|arguments
index|[
name|i
index|]
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDisplayString
parameter_list|(
name|String
index|[]
name|children
parameter_list|)
block|{
return|return
name|getStandardDisplayString
argument_list|(
literal|"struct"
argument_list|,
name|children
argument_list|,
literal|","
argument_list|)
return|;
block|}
block|}
end_class

end_unit

