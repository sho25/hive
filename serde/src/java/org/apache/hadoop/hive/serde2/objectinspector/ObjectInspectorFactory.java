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
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|GenericArrayType
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|ParameterizedType
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Type
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
name|java
operator|.
name|util
operator|.
name|Map
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
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorUtils
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
name|Text
import|;
end_import

begin_comment
comment|/**  * ObjectInspectorFactory is the primary way to create new ObjectInspector  * instances.  *   * SerDe classes should call the static functions in this library to create an  * ObjectInspector to return to the caller of SerDe2.getObjectInspector().  *   * The reason of having caches here is that ObjectInspector is because  * ObjectInspectors do not have an internal state - so ObjectInspectors with the  * same construction parameters should result in exactly the same  * ObjectInspector.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|ObjectInspectorFactory
block|{
comment|/**    * ObjectInspectorOptions describes what ObjectInspector to use. JAVA is to    * use pure JAVA reflection. THRIFT is to use JAVA reflection and filter out    * __isset fields. New ObjectInspectorOptions can be added here when    * available.    *     * We choose to use a single HashMap objectInspectorCache to cache all    * situations for efficiency and code simplicity. And we don't expect a case    * that a user need to create 2 or more different types of ObjectInspectors    * for the same Java type.    */
specifier|public
enum|enum
name|ObjectInspectorOptions
block|{
name|JAVA
block|,
name|THRIFT
block|}
empty_stmt|;
specifier|private
specifier|static
name|HashMap
argument_list|<
name|Type
argument_list|,
name|ObjectInspector
argument_list|>
name|objectInspectorCache
init|=
operator|new
name|HashMap
argument_list|<
name|Type
argument_list|,
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|ObjectInspector
name|getReflectionObjectInspector
parameter_list|(
name|Type
name|t
parameter_list|,
name|ObjectInspectorOptions
name|options
parameter_list|)
block|{
name|ObjectInspector
name|oi
init|=
name|objectInspectorCache
operator|.
name|get
argument_list|(
name|t
argument_list|)
decl_stmt|;
if|if
condition|(
name|oi
operator|==
literal|null
condition|)
block|{
name|oi
operator|=
name|getReflectionObjectInspectorNoCache
argument_list|(
name|t
argument_list|,
name|options
argument_list|)
expr_stmt|;
name|objectInspectorCache
operator|.
name|put
argument_list|(
name|t
argument_list|,
name|oi
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|options
operator|.
name|equals
argument_list|(
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
operator|&&
name|oi
operator|.
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|ThriftStructObjectInspector
operator|.
name|class
argument_list|)
operator|)
operator|||
operator|(
name|options
operator|.
name|equals
argument_list|(
name|ObjectInspectorOptions
operator|.
name|THRIFT
argument_list|)
operator|&&
name|oi
operator|.
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|ReflectionStructObjectInspector
operator|.
name|class
argument_list|)
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot call getObjectInspectorByReflection with both JAVA and THRIFT !"
argument_list|)
throw|;
block|}
return|return
name|oi
return|;
block|}
specifier|private
specifier|static
name|ObjectInspector
name|getReflectionObjectInspectorNoCache
parameter_list|(
name|Type
name|t
parameter_list|,
name|ObjectInspectorOptions
name|options
parameter_list|)
block|{
if|if
condition|(
name|t
operator|instanceof
name|GenericArrayType
condition|)
block|{
name|GenericArrayType
name|at
init|=
operator|(
name|GenericArrayType
operator|)
name|t
decl_stmt|;
return|return
name|getStandardListObjectInspector
argument_list|(
name|getReflectionObjectInspector
argument_list|(
name|at
operator|.
name|getGenericComponentType
argument_list|()
argument_list|,
name|options
argument_list|)
argument_list|)
return|;
block|}
if|if
condition|(
name|t
operator|instanceof
name|ParameterizedType
condition|)
block|{
name|ParameterizedType
name|pt
init|=
operator|(
name|ParameterizedType
operator|)
name|t
decl_stmt|;
comment|// List?
if|if
condition|(
name|List
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
operator|(
name|Class
argument_list|<
name|?
argument_list|>
operator|)
name|pt
operator|.
name|getRawType
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|getStandardListObjectInspector
argument_list|(
name|getReflectionObjectInspector
argument_list|(
name|pt
operator|.
name|getActualTypeArguments
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|options
argument_list|)
argument_list|)
return|;
block|}
comment|// Map?
if|if
condition|(
name|Map
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
operator|(
name|Class
argument_list|<
name|?
argument_list|>
operator|)
name|pt
operator|.
name|getRawType
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|getStandardMapObjectInspector
argument_list|(
name|getReflectionObjectInspector
argument_list|(
name|pt
operator|.
name|getActualTypeArguments
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|options
argument_list|)
argument_list|,
name|getReflectionObjectInspector
argument_list|(
name|pt
operator|.
name|getActualTypeArguments
argument_list|()
index|[
literal|1
index|]
argument_list|,
name|options
argument_list|)
argument_list|)
return|;
block|}
comment|// Otherwise convert t to RawType so we will fall into the following if
comment|// block.
name|t
operator|=
name|pt
operator|.
name|getRawType
argument_list|()
expr_stmt|;
block|}
comment|// Must be a class.
if|if
condition|(
operator|!
operator|(
name|t
operator|instanceof
name|Class
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ObjectInspectorFactory
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|" internal error:"
operator|+
name|t
argument_list|)
throw|;
block|}
name|Class
argument_list|<
name|?
argument_list|>
name|c
init|=
operator|(
name|Class
argument_list|<
name|?
argument_list|>
operator|)
name|t
decl_stmt|;
comment|// Java Primitive Type?
if|if
condition|(
name|PrimitiveObjectInspectorUtils
operator|.
name|isPrimitiveJavaType
argument_list|(
name|c
argument_list|)
condition|)
block|{
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTypeEntryFromPrimitiveJavaType
argument_list|(
name|c
argument_list|)
operator|.
name|primitiveCategory
argument_list|)
return|;
block|}
comment|// Java Primitive Class?
if|if
condition|(
name|PrimitiveObjectInspectorUtils
operator|.
name|isPrimitiveJavaClass
argument_list|(
name|c
argument_list|)
condition|)
block|{
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTypeEntryFromPrimitiveJavaClass
argument_list|(
name|c
argument_list|)
operator|.
name|primitiveCategory
argument_list|)
return|;
block|}
comment|// Primitive Writable class?
if|if
condition|(
name|PrimitiveObjectInspectorUtils
operator|.
name|isPrimitiveWritableClass
argument_list|(
name|c
argument_list|)
condition|)
block|{
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getTypeEntryFromPrimitiveWritableClass
argument_list|(
name|c
argument_list|)
operator|.
name|primitiveCategory
argument_list|)
return|;
block|}
comment|// Must be struct because List and Map need to be ParameterizedType
assert|assert
operator|(
operator|!
name|List
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|c
argument_list|)
operator|)
assert|;
assert|assert
operator|(
operator|!
name|Map
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|c
argument_list|)
operator|)
assert|;
comment|// Create StructObjectInspector
name|ReflectionStructObjectInspector
name|oi
decl_stmt|;
switch|switch
condition|(
name|options
condition|)
block|{
case|case
name|JAVA
case|:
name|oi
operator|=
operator|new
name|ReflectionStructObjectInspector
argument_list|()
expr_stmt|;
break|break;
case|case
name|THRIFT
case|:
name|oi
operator|=
operator|new
name|ThriftStructObjectInspector
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ObjectInspectorFactory
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|": internal error."
argument_list|)
throw|;
block|}
comment|// put it into the cache BEFORE it is initialized to make sure we can catch
comment|// recursive types.
name|objectInspectorCache
operator|.
name|put
argument_list|(
name|t
argument_list|,
name|oi
argument_list|)
expr_stmt|;
name|Field
index|[]
name|fields
init|=
name|ObjectInspectorUtils
operator|.
name|getDeclaredNonStaticFields
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|structFieldObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|fields
operator|.
name|length
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
name|fields
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|oi
operator|.
name|shouldIgnoreField
argument_list|(
name|fields
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|structFieldObjectInspectors
operator|.
name|add
argument_list|(
name|getReflectionObjectInspector
argument_list|(
name|fields
index|[
name|i
index|]
operator|.
name|getGenericType
argument_list|()
argument_list|,
name|options
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|oi
operator|.
name|init
argument_list|(
name|c
argument_list|,
name|structFieldObjectInspectors
argument_list|)
expr_stmt|;
return|return
name|oi
return|;
block|}
specifier|static
name|HashMap
argument_list|<
name|ObjectInspector
argument_list|,
name|StandardListObjectInspector
argument_list|>
name|cachedStandardListObjectInspector
init|=
operator|new
name|HashMap
argument_list|<
name|ObjectInspector
argument_list|,
name|StandardListObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|StandardListObjectInspector
name|getStandardListObjectInspector
parameter_list|(
name|ObjectInspector
name|listElementObjectInspector
parameter_list|)
block|{
name|StandardListObjectInspector
name|result
init|=
name|cachedStandardListObjectInspector
operator|.
name|get
argument_list|(
name|listElementObjectInspector
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
name|StandardListObjectInspector
argument_list|(
name|listElementObjectInspector
argument_list|)
expr_stmt|;
name|cachedStandardListObjectInspector
operator|.
name|put
argument_list|(
name|listElementObjectInspector
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
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|,
name|StandardMapObjectInspector
argument_list|>
name|cachedStandardMapObjectInspector
init|=
operator|new
name|HashMap
argument_list|<
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|,
name|StandardMapObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|StandardMapObjectInspector
name|getStandardMapObjectInspector
parameter_list|(
name|ObjectInspector
name|mapKeyObjectInspector
parameter_list|,
name|ObjectInspector
name|mapValueObjectInspector
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|signature
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|mapKeyObjectInspector
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|mapValueObjectInspector
argument_list|)
expr_stmt|;
name|StandardMapObjectInspector
name|result
init|=
name|cachedStandardMapObjectInspector
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
name|StandardMapObjectInspector
argument_list|(
name|mapKeyObjectInspector
argument_list|,
name|mapValueObjectInspector
argument_list|)
expr_stmt|;
name|cachedStandardMapObjectInspector
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
name|ArrayList
argument_list|<
name|List
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|,
name|StandardStructObjectInspector
argument_list|>
name|cachedStandardStructObjectInspector
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
name|StandardStructObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|StandardStructObjectInspector
name|getStandardStructObjectInspector
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|structFieldNames
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|structFieldObjectInspectors
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
argument_list|()
decl_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|structFieldNames
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|structFieldObjectInspectors
argument_list|)
expr_stmt|;
name|StandardStructObjectInspector
name|result
init|=
name|cachedStandardStructObjectInspector
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
name|StandardStructObjectInspector
argument_list|(
name|structFieldNames
argument_list|,
name|structFieldObjectInspectors
argument_list|)
expr_stmt|;
name|cachedStandardStructObjectInspector
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
name|List
argument_list|<
name|StructObjectInspector
argument_list|>
argument_list|,
name|UnionStructObjectInspector
argument_list|>
name|cachedUnionStructObjectInspector
init|=
operator|new
name|HashMap
argument_list|<
name|List
argument_list|<
name|StructObjectInspector
argument_list|>
argument_list|,
name|UnionStructObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|UnionStructObjectInspector
name|getUnionStructObjectInspector
parameter_list|(
name|List
argument_list|<
name|StructObjectInspector
argument_list|>
name|structObjectInspectors
parameter_list|)
block|{
name|UnionStructObjectInspector
name|result
init|=
name|cachedUnionStructObjectInspector
operator|.
name|get
argument_list|(
name|structObjectInspectors
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
name|UnionStructObjectInspector
argument_list|(
name|structObjectInspectors
argument_list|)
expr_stmt|;
name|cachedUnionStructObjectInspector
operator|.
name|put
argument_list|(
name|structObjectInspectors
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
name|Object
argument_list|>
argument_list|,
name|ColumnarStructObjectInspector
argument_list|>
name|cachedColumnarStructObjectInspector
init|=
operator|new
name|HashMap
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|,
name|ColumnarStructObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|ColumnarStructObjectInspector
name|getColumnarStructObjectInspector
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|structFieldNames
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|structFieldObjectInspectors
parameter_list|,
name|Text
name|nullSequence
parameter_list|)
block|{
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|signature
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|structFieldNames
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|structFieldObjectInspectors
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|nullSequence
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|ColumnarStructObjectInspector
name|result
init|=
name|cachedColumnarStructObjectInspector
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
name|ColumnarStructObjectInspector
argument_list|(
name|structFieldNames
argument_list|,
name|structFieldObjectInspectors
argument_list|,
name|nullSequence
argument_list|)
expr_stmt|;
name|cachedColumnarStructObjectInspector
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
specifier|private
name|ObjectInspectorFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

