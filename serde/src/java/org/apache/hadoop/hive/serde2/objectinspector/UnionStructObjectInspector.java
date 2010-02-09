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
name|List
import|;
end_import

begin_comment
comment|/**  * UnionStructObjectInspector unions several struct data into a single struct.  * Basically, the fields of these structs are put together sequentially into a  * single struct.  *   * The object that can be acceptable by this ObjectInspector is a List of  * objects, each of which can be inspected by the ObjectInspector provided in  * the ctor of UnionStructObjectInspector.  *   * Always use the ObjectInspectorFactory to create new ObjectInspector objects,  * instead of directly creating an instance of this class.  */
end_comment

begin_class
specifier|public
class|class
name|UnionStructObjectInspector
extends|extends
name|StructObjectInspector
block|{
comment|/**    * MyField.    *    */
specifier|public
specifier|static
class|class
name|MyField
implements|implements
name|StructField
block|{
specifier|public
name|int
name|structID
decl_stmt|;
name|StructField
name|structField
decl_stmt|;
specifier|public
name|MyField
parameter_list|(
name|int
name|structID
parameter_list|,
name|StructField
name|structField
parameter_list|)
block|{
name|this
operator|.
name|structID
operator|=
name|structID
expr_stmt|;
name|this
operator|.
name|structField
operator|=
name|structField
expr_stmt|;
block|}
specifier|public
name|String
name|getFieldName
parameter_list|()
block|{
return|return
name|structField
operator|.
name|getFieldName
argument_list|()
return|;
block|}
specifier|public
name|ObjectInspector
name|getFieldObjectInspector
parameter_list|()
block|{
return|return
name|structField
operator|.
name|getFieldObjectInspector
argument_list|()
return|;
block|}
block|}
name|List
argument_list|<
name|StructObjectInspector
argument_list|>
name|unionObjectInspectors
decl_stmt|;
name|List
argument_list|<
name|MyField
argument_list|>
name|fields
decl_stmt|;
specifier|protected
name|UnionStructObjectInspector
parameter_list|(
name|List
argument_list|<
name|StructObjectInspector
argument_list|>
name|unionObjectInspectors
parameter_list|)
block|{
name|init
argument_list|(
name|unionObjectInspectors
argument_list|)
expr_stmt|;
block|}
name|void
name|init
parameter_list|(
name|List
argument_list|<
name|StructObjectInspector
argument_list|>
name|unionObjectInspectors
parameter_list|)
block|{
name|this
operator|.
name|unionObjectInspectors
operator|=
name|unionObjectInspectors
expr_stmt|;
name|int
name|totalSize
init|=
literal|0
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
name|unionObjectInspectors
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|totalSize
operator|+=
name|unionObjectInspectors
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getAllStructFieldRefs
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
name|fields
operator|=
operator|new
name|ArrayList
argument_list|<
name|MyField
argument_list|>
argument_list|(
name|totalSize
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|unionObjectInspectors
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|StructObjectInspector
name|oi
init|=
name|unionObjectInspectors
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
for|for
control|(
name|StructField
name|sf
range|:
name|oi
operator|.
name|getAllStructFieldRefs
argument_list|()
control|)
block|{
name|fields
operator|.
name|add
argument_list|(
operator|new
name|MyField
argument_list|(
name|i
argument_list|,
name|sf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|final
name|Category
name|getCategory
parameter_list|()
block|{
return|return
name|Category
operator|.
name|STRUCT
return|;
block|}
specifier|public
name|String
name|getTypeName
parameter_list|()
block|{
return|return
name|ObjectInspectorUtils
operator|.
name|getStandardStructTypeName
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|// Without Data
annotation|@
name|Override
specifier|public
name|StructField
name|getStructFieldRef
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
return|return
name|ObjectInspectorUtils
operator|.
name|getStandardStructFieldRef
argument_list|(
name|fieldName
argument_list|,
name|fields
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|getAllStructFieldRefs
parameter_list|()
block|{
return|return
name|fields
return|;
block|}
comment|// With Data
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|Object
name|getStructFieldData
parameter_list|(
name|Object
name|data
parameter_list|,
name|StructField
name|fieldRef
parameter_list|)
block|{
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|MyField
name|f
init|=
operator|(
name|MyField
operator|)
name|fieldRef
decl_stmt|;
name|Object
name|fieldData
decl_stmt|;
comment|// We support both List<Object> and Object[]
comment|// so we have to do differently.
if|if
condition|(
name|data
operator|.
name|getClass
argument_list|()
operator|.
name|isArray
argument_list|()
condition|)
block|{
name|Object
index|[]
name|list
init|=
operator|(
name|Object
index|[]
operator|)
name|data
decl_stmt|;
assert|assert
operator|(
name|list
operator|.
name|length
operator|==
name|unionObjectInspectors
operator|.
name|size
argument_list|()
operator|)
assert|;
name|fieldData
operator|=
name|list
index|[
name|f
operator|.
name|structID
index|]
expr_stmt|;
block|}
else|else
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|list
init|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|data
decl_stmt|;
assert|assert
operator|(
name|list
operator|.
name|size
argument_list|()
operator|==
name|unionObjectInspectors
operator|.
name|size
argument_list|()
operator|)
assert|;
name|fieldData
operator|=
name|list
operator|.
name|get
argument_list|(
name|f
operator|.
name|structID
argument_list|)
expr_stmt|;
block|}
return|return
name|unionObjectInspectors
operator|.
name|get
argument_list|(
name|f
operator|.
name|structID
argument_list|)
operator|.
name|getStructFieldData
argument_list|(
name|fieldData
argument_list|,
name|f
operator|.
name|structField
argument_list|)
return|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|getStructFieldsDataAsList
parameter_list|(
name|Object
name|data
parameter_list|)
block|{
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// We support both List<Object> and Object[]
comment|// so we have to do differently.
if|if
condition|(
name|data
operator|.
name|getClass
argument_list|()
operator|.
name|isArray
argument_list|()
condition|)
block|{
name|data
operator|=
name|java
operator|.
name|util
operator|.
name|Arrays
operator|.
name|asList
argument_list|(
operator|(
name|Object
index|[]
operator|)
name|data
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Object
argument_list|>
name|list
init|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|data
decl_stmt|;
assert|assert
operator|(
name|list
operator|.
name|size
argument_list|()
operator|==
name|unionObjectInspectors
operator|.
name|size
argument_list|()
operator|)
assert|;
comment|// Explode
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|fields
operator|.
name|size
argument_list|()
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
name|unionObjectInspectors
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|.
name|addAll
argument_list|(
name|unionObjectInspectors
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getStructFieldsDataAsList
argument_list|(
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

