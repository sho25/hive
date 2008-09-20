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

begin_comment
comment|/** A Map Type has homogeneous keys and homogeneous values.  *  All keys of the Map have the same TypeInfo, which is returned by  *  getMapKeyTypeInfo(); and all values of the Map has the same TypeInfo,  *  which is returned by getMapValueTypeInfo().  *    *  Always use the TypeInfoFactory to create new TypeInfo objects, instead  *  of directly creating an instance of this class.   */
end_comment

begin_class
specifier|public
class|class
name|MapTypeInfo
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
name|TypeInfo
name|mapKeyTypeInfo
decl_stmt|;
name|TypeInfo
name|mapValueTypeInfo
decl_stmt|;
comment|/** For java serialization use only.    */
specifier|public
name|MapTypeInfo
parameter_list|()
block|{}
specifier|public
name|String
name|getTypeName
parameter_list|()
block|{
return|return
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
name|Constants
operator|.
name|MAP_TYPE_NAME
operator|+
literal|"<"
operator|+
name|mapKeyTypeInfo
operator|.
name|getTypeName
argument_list|()
operator|+
literal|","
operator|+
name|mapValueTypeInfo
operator|.
name|getTypeName
argument_list|()
operator|+
literal|">"
return|;
block|}
comment|/** For java serialization use only.    */
specifier|public
name|void
name|setMapKeyTypeInfo
parameter_list|(
name|TypeInfo
name|mapKeyTypeInfo
parameter_list|)
block|{
name|this
operator|.
name|mapKeyTypeInfo
operator|=
name|mapKeyTypeInfo
expr_stmt|;
block|}
comment|/** For java serialization use only.    */
specifier|public
name|void
name|setMapValueTypeInfo
parameter_list|(
name|TypeInfo
name|mapValueTypeInfo
parameter_list|)
block|{
name|this
operator|.
name|mapValueTypeInfo
operator|=
name|mapValueTypeInfo
expr_stmt|;
block|}
comment|// For TypeInfoFactory use only
name|MapTypeInfo
parameter_list|(
name|TypeInfo
name|keyTypeInfo
parameter_list|,
name|TypeInfo
name|valueTypeInfo
parameter_list|)
block|{
name|this
operator|.
name|mapKeyTypeInfo
operator|=
name|keyTypeInfo
expr_stmt|;
name|this
operator|.
name|mapValueTypeInfo
operator|=
name|valueTypeInfo
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
name|MAP
return|;
block|}
specifier|public
name|TypeInfo
name|getMapKeyTypeInfo
parameter_list|()
block|{
return|return
name|mapKeyTypeInfo
return|;
block|}
specifier|public
name|TypeInfo
name|getMapValueTypeInfo
parameter_list|()
block|{
return|return
name|mapValueTypeInfo
return|;
block|}
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
name|getMapKeyTypeInfo
argument_list|()
operator|.
name|equals
argument_list|(
name|getMapKeyTypeInfo
argument_list|()
argument_list|)
operator|&&
name|o
operator|.
name|getMapValueTypeInfo
argument_list|()
operator|.
name|equals
argument_list|(
name|getMapValueTypeInfo
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
name|mapKeyTypeInfo
operator|.
name|hashCode
argument_list|()
operator|^
name|mapValueTypeInfo
operator|.
name|hashCode
argument_list|()
return|;
block|}
block|}
end_class

end_unit

