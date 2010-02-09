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
name|typeinfo
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
name|HashMap
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
name|serde
operator|.
name|Constants
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
name|PrimitiveObjectInspectorUtils
import|;
end_import

begin_comment
comment|/**  * TypeInfoFactory can be used to create the TypeInfo object for any types.  *   * TypeInfo objects are all read-only so we can reuse them easily.  * TypeInfoFactory has internal cache to make sure we don't create 2 TypeInfo  * objects that represents the same type.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|TypeInfoFactory
block|{
specifier|static
name|HashMap
argument_list|<
name|String
argument_list|,
name|TypeInfo
argument_list|>
name|cachedPrimitiveTypeInfo
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|TypeInfo
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|TypeInfoFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
specifier|public
specifier|static
name|TypeInfo
name|getPrimitiveTypeInfo
parameter_list|(
name|String
name|typeName
parameter_list|)
block|{
if|if
condition|(
literal|null
operator|==
name|PrimitiveObjectInspectorUtils
operator|.
name|getTypeEntryFromTypeName
argument_list|(
name|typeName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot getPrimitiveTypeInfo for "
operator|+
name|typeName
argument_list|)
throw|;
block|}
name|TypeInfo
name|result
init|=
name|cachedPrimitiveTypeInfo
operator|.
name|get
argument_list|(
name|typeName
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|result
operator|=
operator|new
name|PrimitiveTypeInfo
argument_list|(
name|typeName
argument_list|)
expr_stmt|;
name|cachedPrimitiveTypeInfo
operator|.
name|put
argument_list|(
name|typeName
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
specifier|final
name|TypeInfo
name|voidTypeInfo
init|=
name|getPrimitiveTypeInfo
argument_list|(
name|Constants
operator|.
name|VOID_TYPE_NAME
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|TypeInfo
name|booleanTypeInfo
init|=
name|getPrimitiveTypeInfo
argument_list|(
name|Constants
operator|.
name|BOOLEAN_TYPE_NAME
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|TypeInfo
name|intTypeInfo
init|=
name|getPrimitiveTypeInfo
argument_list|(
name|Constants
operator|.
name|INT_TYPE_NAME
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|TypeInfo
name|longTypeInfo
init|=
name|getPrimitiveTypeInfo
argument_list|(
name|Constants
operator|.
name|BIGINT_TYPE_NAME
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|TypeInfo
name|stringTypeInfo
init|=
name|getPrimitiveTypeInfo
argument_list|(
name|Constants
operator|.
name|STRING_TYPE_NAME
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|TypeInfo
name|floatTypeInfo
init|=
name|getPrimitiveTypeInfo
argument_list|(
name|Constants
operator|.
name|FLOAT_TYPE_NAME
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|TypeInfo
name|doubleTypeInfo
init|=
name|getPrimitiveTypeInfo
argument_list|(
name|Constants
operator|.
name|DOUBLE_TYPE_NAME
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|TypeInfo
name|byteTypeInfo
init|=
name|getPrimitiveTypeInfo
argument_list|(
name|Constants
operator|.
name|TINYINT_TYPE_NAME
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|TypeInfo
name|shortTypeInfo
init|=
name|getPrimitiveTypeInfo
argument_list|(
name|Constants
operator|.
name|SMALLINT_TYPE_NAME
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|TypeInfo
name|unknownTypeInfo
init|=
name|getPrimitiveTypeInfo
argument_list|(
literal|"unknown"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|TypeInfo
name|getPrimitiveTypeInfoFromPrimitiveWritable
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
block|{
name|String
name|typeName
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getTypeNameFromPrimitiveWritable
argument_list|(
name|clazz
argument_list|)
decl_stmt|;
if|if
condition|(
name|typeName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Internal error: Cannot get typeName for "
operator|+
name|clazz
argument_list|)
throw|;
block|}
return|return
name|getPrimitiveTypeInfo
argument_list|(
name|typeName
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|TypeInfo
name|getPrimitiveTypeInfoFromJavaPrimitive
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
block|{
return|return
name|getPrimitiveTypeInfo
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTypeNameFromPrimitiveJava
argument_list|(
name|clazz
argument_list|)
argument_list|)
return|;
block|}
specifier|static
name|HashMap
argument_list|<
name|ArrayList
argument_list|<
name|List
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|,
name|TypeInfo
argument_list|>
name|cachedStructTypeInfo
init|=
operator|new
name|HashMap
argument_list|<
name|ArrayList
argument_list|<
name|List
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|,
name|TypeInfo
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|TypeInfo
name|getStructTypeInfo
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
name|ArrayList
argument_list|<
name|List
argument_list|<
name|?
argument_list|>
argument_list|>
name|signature
init|=
operator|new
name|ArrayList
argument_list|<
name|List
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|names
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|typeInfos
argument_list|)
expr_stmt|;
name|TypeInfo
name|result
init|=
name|cachedStructTypeInfo
operator|.
name|get
argument_list|(
name|signature
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|result
operator|=
operator|new
name|StructTypeInfo
argument_list|(
name|names
argument_list|,
name|typeInfos
argument_list|)
expr_stmt|;
name|cachedStructTypeInfo
operator|.
name|put
argument_list|(
name|signature
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|static
name|HashMap
argument_list|<
name|TypeInfo
argument_list|,
name|TypeInfo
argument_list|>
name|cachedListTypeInfo
init|=
operator|new
name|HashMap
argument_list|<
name|TypeInfo
argument_list|,
name|TypeInfo
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|TypeInfo
name|getListTypeInfo
parameter_list|(
name|TypeInfo
name|elementTypeInfo
parameter_list|)
block|{
name|TypeInfo
name|result
init|=
name|cachedListTypeInfo
operator|.
name|get
argument_list|(
name|elementTypeInfo
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|result
operator|=
operator|new
name|ListTypeInfo
argument_list|(
name|elementTypeInfo
argument_list|)
expr_stmt|;
name|cachedListTypeInfo
operator|.
name|put
argument_list|(
name|elementTypeInfo
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|static
name|HashMap
argument_list|<
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|,
name|TypeInfo
argument_list|>
name|cachedMapTypeInfo
init|=
operator|new
name|HashMap
argument_list|<
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|,
name|TypeInfo
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|TypeInfo
name|getMapTypeInfo
parameter_list|(
name|TypeInfo
name|keyTypeInfo
parameter_list|,
name|TypeInfo
name|valueTypeInfo
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
name|signature
init|=
operator|new
name|ArrayList
argument_list|<
name|TypeInfo
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|keyTypeInfo
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|valueTypeInfo
argument_list|)
expr_stmt|;
name|TypeInfo
name|result
init|=
name|cachedMapTypeInfo
operator|.
name|get
argument_list|(
name|signature
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|result
operator|=
operator|new
name|MapTypeInfo
argument_list|(
name|keyTypeInfo
argument_list|,
name|valueTypeInfo
argument_list|)
expr_stmt|;
name|cachedMapTypeInfo
operator|.
name|put
argument_list|(
name|signature
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
empty_stmt|;
block|}
end_class

end_unit

