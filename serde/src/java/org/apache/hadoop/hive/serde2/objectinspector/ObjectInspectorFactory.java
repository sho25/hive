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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|common
operator|.
name|StringInternUtils
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
name|thrift
operator|.
name|TUnion
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|Cache
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|cache
operator|.
name|CacheBuilder
import|;
end_import

begin_comment
comment|/**  * ObjectInspectorFactory is the primary way to create new ObjectInspector  * instances.  *  * SerDe classes should call the static functions in this library to create an  * ObjectInspector to return to the caller of SerDe2.getObjectInspector().  *  * The reason of having caches here is that ObjectInspector is because  * ObjectInspectors do not have an internal state - so ObjectInspectors with the  * same construction parameters should result in exactly the same  * ObjectInspector.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|ObjectInspectorFactory
block|{
comment|/**    * ObjectInspectorOptions describes what ObjectInspector to use. JAVA is to    * use pure JAVA reflection. THRIFT is to use JAVA reflection and filter out    * __isset fields, PROTOCOL_BUFFERS filters out has*.    * New ObjectInspectorOptions can be added here when available.    *    * We choose to use a single HashMap objectInspectorCache to cache all    * situations for efficiency and code simplicity. And we don't expect a case    * that a user need to create 2 or more different types of ObjectInspectors    * for the same Java type.    */
specifier|public
enum|enum
name|ObjectInspectorOptions
block|{
name|JAVA
block|,
name|THRIFT
block|,
name|PROTOCOL_BUFFERS
block|,
name|AVRO
block|}
comment|// guava cache builder does not support generics, so reuse builder
specifier|private
specifier|static
name|CacheBuilder
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|boundedBuilder
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|initialCapacity
argument_list|(
literal|1024
argument_list|)
operator|.
name|maximumSize
argument_list|(
literal|10240
argument_list|)
comment|// 10x initial capacity
operator|.
name|concurrencyLevel
argument_list|(
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
argument_list|)
operator|.
name|expireAfterAccess
argument_list|(
literal|5
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
operator|.
name|softValues
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|CacheBuilder
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|unboundedBuilder
init|=
name|CacheBuilder
operator|.
name|newBuilder
argument_list|()
operator|.
name|initialCapacity
argument_list|(
literal|1024
argument_list|)
operator|.
name|concurrencyLevel
argument_list|(
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
argument_list|)
operator|.
name|expireAfterAccess
argument_list|(
literal|5
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
operator|.
name|softValues
argument_list|()
decl_stmt|;
comment|// if this is made bounded (with eviction), type == type may not be oi == oi
specifier|static
name|Cache
argument_list|<
name|Type
argument_list|,
name|ObjectInspector
argument_list|>
name|objectInspectorCache
init|=
name|unboundedBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|static
name|Cache
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
name|boundedBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|static
name|Cache
argument_list|<
name|ObjectInspector
argument_list|,
name|StandardListObjectInspector
argument_list|>
name|cachedStandardListObjectInspector
init|=
name|boundedBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|static
name|Cache
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
name|boundedBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|static
name|Cache
argument_list|<
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|,
name|StandardUnionObjectInspector
argument_list|>
name|cachedStandardUnionObjectInspector
init|=
name|boundedBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|static
name|Cache
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
name|boundedBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|static
name|Cache
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
name|boundedBuilder
operator|.
name|build
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
return|return
name|getReflectionObjectInspector
argument_list|(
name|t
argument_list|,
name|options
argument_list|,
literal|true
argument_list|)
return|;
block|}
specifier|static
name|ObjectInspector
name|getReflectionObjectInspector
parameter_list|(
name|Type
name|t
parameter_list|,
name|ObjectInspectorOptions
name|options
parameter_list|,
name|boolean
name|ensureInited
parameter_list|)
block|{
name|ObjectInspector
name|oi
init|=
name|objectInspectorCache
operator|.
name|asMap
argument_list|()
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
argument_list|,
name|ensureInited
argument_list|)
expr_stmt|;
name|ObjectInspector
name|prev
init|=
name|objectInspectorCache
operator|.
name|asMap
argument_list|()
operator|.
name|putIfAbsent
argument_list|(
name|t
argument_list|,
name|oi
argument_list|)
decl_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
name|oi
operator|=
name|prev
expr_stmt|;
block|}
block|}
if|if
condition|(
name|ensureInited
operator|&&
name|oi
operator|instanceof
name|ReflectionStructObjectInspector
condition|)
block|{
name|ReflectionStructObjectInspector
name|soi
init|=
operator|(
name|ReflectionStructObjectInspector
operator|)
name|oi
decl_stmt|;
synchronized|synchronized
init|(
name|soi
init|)
block|{
name|HashSet
argument_list|<
name|Type
argument_list|>
name|checkedTypes
init|=
operator|new
name|HashSet
argument_list|<
name|Type
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|soi
operator|.
name|isFullyInited
argument_list|(
name|checkedTypes
argument_list|)
condition|)
block|{
try|try
block|{
comment|// Wait for up to 3 seconds before checking if any init error.
comment|// Init should be fast if no error, no need to make this configurable.
name|soi
operator|.
name|wait
argument_list|(
literal|3000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Interrupted while waiting for "
operator|+
name|soi
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" to initialize"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
name|verifyObjectInspector
argument_list|(
name|options
argument_list|,
name|oi
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|,
operator|new
name|Class
index|[]
block|{
name|ThriftStructObjectInspector
operator|.
name|class
block|,
name|ProtocolBuffersStructObjectInspector
operator|.
name|class
block|}
argument_list|)
expr_stmt|;
name|verifyObjectInspector
argument_list|(
name|options
argument_list|,
name|oi
argument_list|,
name|ObjectInspectorOptions
operator|.
name|THRIFT
argument_list|,
operator|new
name|Class
index|[]
block|{
name|ReflectionStructObjectInspector
operator|.
name|class
block|,
name|ProtocolBuffersStructObjectInspector
operator|.
name|class
block|}
argument_list|)
expr_stmt|;
name|verifyObjectInspector
argument_list|(
name|options
argument_list|,
name|oi
argument_list|,
name|ObjectInspectorOptions
operator|.
name|PROTOCOL_BUFFERS
argument_list|,
operator|new
name|Class
index|[]
block|{
name|ThriftStructObjectInspector
operator|.
name|class
block|,
name|ReflectionStructObjectInspector
operator|.
name|class
block|}
argument_list|)
expr_stmt|;
return|return
name|oi
return|;
block|}
comment|/**    * Verify that we don't have an unexpected type of object inspector.    * @param option The option to verify    * @param oi The ObjectInspector to verify    * @param checkOption We're only interested in this option type    * @param classes ObjectInspector should not be of these types    */
specifier|private
specifier|static
name|void
name|verifyObjectInspector
parameter_list|(
name|ObjectInspectorOptions
name|option
parameter_list|,
name|ObjectInspector
name|oi
parameter_list|,
name|ObjectInspectorOptions
name|checkOption
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|classes
parameter_list|)
block|{
if|if
condition|(
name|option
operator|.
name|equals
argument_list|(
name|checkOption
argument_list|)
condition|)
block|{
for|for
control|(
name|Class
argument_list|<
name|?
argument_list|>
name|checkClass
range|:
name|classes
control|)
block|{
if|if
condition|(
name|oi
operator|.
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|checkClass
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot call getObjectInspectorByReflection with more then one of "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|ObjectInspectorOptions
operator|.
name|values
argument_list|()
argument_list|)
operator|+
literal|"!"
argument_list|)
throw|;
block|}
block|}
block|}
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
parameter_list|,
name|boolean
name|ensureInited
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
argument_list|,
name|ensureInited
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
operator|||
name|Set
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
argument_list|,
name|ensureInited
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
argument_list|,
name|ensureInited
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
argument_list|,
name|ensureInited
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
comment|// Enum class?
if|if
condition|(
name|Enum
operator|.
name|class
operator|.
name|isAssignableFrom
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
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|STRING
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
name|TUnion
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|c
argument_list|)
condition|?
operator|new
name|ThriftUnionObjectInspector
argument_list|()
else|:
operator|new
name|ThriftStructObjectInspector
argument_list|()
expr_stmt|;
break|break;
case|case
name|PROTOCOL_BUFFERS
case|:
name|oi
operator|=
operator|new
name|ProtocolBuffersStructObjectInspector
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
name|ReflectionStructObjectInspector
name|prev
init|=
operator|(
name|ReflectionStructObjectInspector
operator|)
name|objectInspectorCache
operator|.
name|asMap
argument_list|()
operator|.
name|putIfAbsent
argument_list|(
name|t
argument_list|,
name|oi
argument_list|)
decl_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
name|oi
operator|=
name|prev
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|oi
operator|.
name|init
argument_list|(
name|t
argument_list|,
name|c
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|oi
operator|.
name|inited
condition|)
block|{
comment|// Failed to init, remove it from cache
name|objectInspectorCache
operator|.
name|asMap
argument_list|()
operator|.
name|remove
argument_list|(
name|t
argument_list|,
name|oi
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|oi
return|;
block|}
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
name|asMap
argument_list|()
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
name|StandardListObjectInspector
name|prev
init|=
name|cachedStandardListObjectInspector
operator|.
name|asMap
argument_list|()
operator|.
name|putIfAbsent
argument_list|(
name|listElementObjectInspector
argument_list|,
name|result
argument_list|)
decl_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
name|prev
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|StandardConstantListObjectInspector
name|getStandardConstantListObjectInspector
parameter_list|(
name|ObjectInspector
name|listElementObjectInspector
parameter_list|,
name|List
argument_list|<
name|?
argument_list|>
name|constantValue
parameter_list|)
block|{
return|return
operator|new
name|StandardConstantListObjectInspector
argument_list|(
name|listElementObjectInspector
argument_list|,
name|constantValue
argument_list|)
return|;
block|}
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
name|asMap
argument_list|()
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
name|StandardMapObjectInspector
name|prev
init|=
name|cachedStandardMapObjectInspector
operator|.
name|asMap
argument_list|()
operator|.
name|putIfAbsent
argument_list|(
name|signature
argument_list|,
name|result
argument_list|)
decl_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
name|prev
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|StandardConstantMapObjectInspector
name|getStandardConstantMapObjectInspector
parameter_list|(
name|ObjectInspector
name|mapKeyObjectInspector
parameter_list|,
name|ObjectInspector
name|mapValueObjectInspector
parameter_list|,
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|constantValue
parameter_list|)
block|{
return|return
operator|new
name|StandardConstantMapObjectInspector
argument_list|(
name|mapKeyObjectInspector
argument_list|,
name|mapValueObjectInspector
argument_list|,
name|constantValue
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|StandardUnionObjectInspector
name|getStandardUnionObjectInspector
parameter_list|(
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|unionObjectInspectors
parameter_list|)
block|{
name|StandardUnionObjectInspector
name|result
init|=
name|cachedStandardUnionObjectInspector
operator|.
name|asMap
argument_list|()
operator|.
name|get
argument_list|(
name|unionObjectInspectors
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
name|StandardUnionObjectInspector
argument_list|(
name|unionObjectInspectors
argument_list|)
expr_stmt|;
name|StandardUnionObjectInspector
name|prev
init|=
name|cachedStandardUnionObjectInspector
operator|.
name|asMap
argument_list|()
operator|.
name|putIfAbsent
argument_list|(
name|unionObjectInspectors
argument_list|,
name|result
argument_list|)
decl_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
name|prev
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
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
return|return
name|getStandardStructObjectInspector
argument_list|(
name|structFieldNames
argument_list|,
name|structFieldObjectInspectors
argument_list|,
literal|null
argument_list|)
return|;
block|}
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
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|structComments
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
literal|3
argument_list|)
decl_stmt|;
name|StringInternUtils
operator|.
name|internStringsInList
argument_list|(
name|structFieldNames
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|structComments
operator|!=
literal|null
condition|)
block|{
name|StringInternUtils
operator|.
name|internStringsInList
argument_list|(
name|structComments
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|structComments
argument_list|)
expr_stmt|;
block|}
name|StandardStructObjectInspector
name|result
init|=
name|cachedStandardStructObjectInspector
operator|.
name|asMap
argument_list|()
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
argument_list|,
name|structComments
argument_list|)
expr_stmt|;
name|StandardStructObjectInspector
name|prev
init|=
name|cachedStandardStructObjectInspector
operator|.
name|asMap
argument_list|()
operator|.
name|putIfAbsent
argument_list|(
name|signature
argument_list|,
name|result
argument_list|)
decl_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
name|prev
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
specifier|public
specifier|static
name|StandardConstantStructObjectInspector
name|getStandardConstantStructObjectInspector
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
name|List
argument_list|<
name|?
argument_list|>
name|value
parameter_list|)
block|{
return|return
operator|new
name|StandardConstantStructObjectInspector
argument_list|(
name|structFieldNames
argument_list|,
name|structFieldObjectInspectors
argument_list|,
name|value
argument_list|)
return|;
block|}
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
name|getIfPresent
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
parameter_list|)
block|{
return|return
name|getColumnarStructObjectInspector
argument_list|(
name|structFieldNames
argument_list|,
name|structFieldObjectInspectors
argument_list|,
literal|null
argument_list|)
return|;
block|}
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
name|List
argument_list|<
name|String
argument_list|>
name|structFieldComments
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
argument_list|(
literal|3
argument_list|)
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
if|if
condition|(
name|structFieldComments
operator|!=
literal|null
condition|)
block|{
name|signature
operator|.
name|add
argument_list|(
name|structFieldComments
argument_list|)
expr_stmt|;
block|}
name|ColumnarStructObjectInspector
name|result
init|=
name|cachedColumnarStructObjectInspector
operator|.
name|asMap
argument_list|()
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
name|structFieldComments
argument_list|)
expr_stmt|;
name|ColumnarStructObjectInspector
name|prev
init|=
name|cachedColumnarStructObjectInspector
operator|.
name|asMap
argument_list|()
operator|.
name|putIfAbsent
argument_list|(
name|signature
argument_list|,
name|result
argument_list|)
decl_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
name|result
operator|=
name|prev
expr_stmt|;
block|}
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

