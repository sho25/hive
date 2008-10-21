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
name|ql
operator|.
name|typeinfo
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|ObjectInspectorUtils
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
name|StructField
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
name|StructObjectInspector
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

begin_comment
comment|/** StructTypeInfo represents the TypeInfo of a struct.  *  A struct contains one or more fields each of which has a unique name  *  and its own TypeInfo.  Different fields can have the same or different  *  TypeInfo.   *    *  Always use the TypeInfoFactory to create new TypeInfo objects, instead  *  of directly creating an instance of this class.   */
end_comment

begin_class
specifier|public
class|class
name|StructTypeInfo
extends|extends
name|TypeInfo
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|allStructFieldNames
decl_stmt|;
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
name|allStructFieldTypeInfos
decl_stmt|;
comment|/** For java serialization use only.    */
specifier|public
name|StructTypeInfo
parameter_list|()
block|{}
specifier|public
name|String
name|getTypeName
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"struct{"
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
name|allStructFieldNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
name|sb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|allStructFieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|allStructFieldTypeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/** For java serialization use only.    */
specifier|public
name|void
name|setAllStructFieldNames
parameter_list|(
name|ArrayList
argument_list|<
name|String
argument_list|>
name|allStructFieldNames
parameter_list|)
block|{
name|this
operator|.
name|allStructFieldNames
operator|=
name|allStructFieldNames
expr_stmt|;
block|}
comment|/** For java serialization use only.    */
specifier|public
name|void
name|setAllStructFieldTypeInfos
parameter_list|(
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
name|allStructFieldTypeInfos
parameter_list|)
block|{
name|this
operator|.
name|allStructFieldTypeInfos
operator|=
name|allStructFieldTypeInfos
expr_stmt|;
block|}
comment|/** For TypeInfoFactory use only.    */
name|StructTypeInfo
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|names
parameter_list|,
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|typeInfos
parameter_list|)
block|{
assert|assert
operator|(
name|allStructFieldNames
operator|.
name|size
argument_list|()
operator|==
name|typeInfos
operator|.
name|size
argument_list|()
operator|)
assert|;
name|allStructFieldNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|allStructFieldNames
operator|.
name|addAll
argument_list|(
name|names
argument_list|)
expr_stmt|;
name|allStructFieldTypeInfos
operator|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|()
expr_stmt|;
name|allStructFieldTypeInfos
operator|.
name|addAll
argument_list|(
name|typeInfos
argument_list|)
expr_stmt|;
block|}
specifier|public
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
name|List
argument_list|<
name|String
argument_list|>
name|getAllStructFieldNames
parameter_list|()
block|{
return|return
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|allStructFieldNames
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|getAllStructFieldTypeInfos
parameter_list|()
block|{
return|return
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|allStructFieldTypeInfos
argument_list|)
return|;
block|}
specifier|public
name|TypeInfo
name|getStructFieldTypeInfo
parameter_list|(
name|String
name|field
parameter_list|)
block|{
name|String
name|fieldLowerCase
init|=
name|field
operator|.
name|toLowerCase
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
name|allStructFieldNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|fieldLowerCase
operator|.
name|equals
argument_list|(
name|allStructFieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
condition|)
block|{
return|return
name|allStructFieldTypeInfos
operator|.
name|get
argument_list|(
name|i
argument_list|)
return|;
block|}
block|}
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"cannot find field "
operator|+
name|field
operator|+
literal|"(lowercase form: "
operator|+
name|fieldLowerCase
operator|+
literal|") in "
operator|+
name|allStructFieldNames
argument_list|)
throw|;
comment|// return null;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|other
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|other
operator|instanceof
name|TypeInfo
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|TypeInfo
name|o
init|=
operator|(
name|TypeInfo
operator|)
name|other
decl_stmt|;
return|return
name|o
operator|.
name|getCategory
argument_list|()
operator|.
name|equals
argument_list|(
name|getCategory
argument_list|()
argument_list|)
operator|&&
name|o
operator|.
name|getAllStructFieldNames
argument_list|()
operator|.
name|equals
argument_list|(
name|getAllStructFieldNames
argument_list|()
argument_list|)
operator|&&
name|o
operator|.
name|getAllStructFieldTypeInfos
argument_list|()
operator|.
name|equals
argument_list|(
name|getAllStructFieldTypeInfos
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|allStructFieldNames
operator|.
name|hashCode
argument_list|()
operator|^
name|allStructFieldTypeInfos
operator|.
name|hashCode
argument_list|()
return|;
block|}
block|}
end_class

end_unit

