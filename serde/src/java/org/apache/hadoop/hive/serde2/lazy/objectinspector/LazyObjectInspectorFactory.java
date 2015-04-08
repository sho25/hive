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
name|lazy
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|avro
operator|.
name|AvroLazyObjectInspector
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyObjectInspectorParameters
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyObjectInspectorParametersImpl
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
operator|.
name|ObjectInspectorOptions
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
comment|/**  * ObjectInspectorFactory is the primary way to create new ObjectInspector  * instances.  *  * SerDe classes should call the static functions in this library to create an  * ObjectInspector to return to the caller of SerDe2.getObjectInspector().  *  * The reason of having caches here is that ObjectInspectors do not have an  * internal state - so ObjectInspectors with the same construction parameters  * should result in exactly the same ObjectInspector.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|LazyObjectInspectorFactory
block|{
specifier|static
name|ConcurrentHashMap
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|,
name|LazySimpleStructObjectInspector
argument_list|>
name|cachedLazySimpleStructObjectInspector
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|,
name|LazySimpleStructObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Deprecated
specifier|public
specifier|static
name|LazySimpleStructObjectInspector
name|getLazySimpleStructObjectInspector
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
name|byte
name|separator
parameter_list|,
name|Text
name|nullSequence
parameter_list|,
name|boolean
name|lastColumnTakesRest
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|)
block|{
return|return
name|getLazySimpleStructObjectInspector
argument_list|(
name|structFieldNames
argument_list|,
name|structFieldObjectInspectors
argument_list|,
literal|null
argument_list|,
name|separator
argument_list|,
name|nullSequence
argument_list|,
name|lastColumnTakesRest
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
return|;
block|}
annotation|@
name|Deprecated
specifier|public
specifier|static
name|LazySimpleStructObjectInspector
name|getLazySimpleStructObjectInspector
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
name|byte
name|separator
parameter_list|,
name|Text
name|nullSequence
parameter_list|,
name|boolean
name|lastColumnTakesRest
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|,
name|ObjectInspectorOptions
name|option
parameter_list|)
block|{
return|return
name|getLazySimpleStructObjectInspector
argument_list|(
name|structFieldNames
argument_list|,
name|structFieldObjectInspectors
argument_list|,
literal|null
argument_list|,
name|separator
argument_list|,
name|nullSequence
argument_list|,
name|lastColumnTakesRest
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|option
argument_list|)
return|;
block|}
annotation|@
name|Deprecated
specifier|public
specifier|static
name|LazySimpleStructObjectInspector
name|getLazySimpleStructObjectInspector
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
parameter_list|,
name|byte
name|separator
parameter_list|,
name|Text
name|nullSequence
parameter_list|,
name|boolean
name|lastColumnTakesRest
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|)
block|{
return|return
name|getLazySimpleStructObjectInspector
argument_list|(
name|structFieldNames
argument_list|,
name|structFieldObjectInspectors
argument_list|,
name|structFieldComments
argument_list|,
name|separator
argument_list|,
name|nullSequence
argument_list|,
name|lastColumnTakesRest
argument_list|,
name|escaped
argument_list|,
name|escapeChar
argument_list|,
name|ObjectInspectorOptions
operator|.
name|JAVA
argument_list|)
return|;
block|}
annotation|@
name|Deprecated
specifier|public
specifier|static
name|LazySimpleStructObjectInspector
name|getLazySimpleStructObjectInspector
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
parameter_list|,
name|byte
name|separator
parameter_list|,
name|Text
name|nullSequence
parameter_list|,
name|boolean
name|lastColumnTakesRest
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|,
name|ObjectInspectorOptions
name|option
parameter_list|)
block|{
return|return
name|getLazySimpleStructObjectInspector
argument_list|(
name|structFieldNames
argument_list|,
name|structFieldObjectInspectors
argument_list|,
name|structFieldComments
argument_list|,
name|separator
argument_list|,
operator|new
name|LazyObjectInspectorParametersImpl
argument_list|(
name|escaped
argument_list|,
name|escapeChar
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|nullSequence
argument_list|,
name|lastColumnTakesRest
argument_list|)
argument_list|,
name|option
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|LazySimpleStructObjectInspector
name|getLazySimpleStructObjectInspector
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
parameter_list|,
name|byte
name|separator
parameter_list|,
name|LazyObjectInspectorParameters
name|lazyParams
parameter_list|,
name|ObjectInspectorOptions
name|option
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
name|Byte
operator|.
name|valueOf
argument_list|(
name|separator
argument_list|)
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|lazyParams
operator|.
name|getNullSequence
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|Boolean
operator|.
name|valueOf
argument_list|(
name|lazyParams
operator|.
name|isLastColumnTakesRest
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|LazyObjectInspectorFactory
operator|.
name|addCommonLazyParamsToSignature
argument_list|(
name|lazyParams
argument_list|,
name|signature
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|option
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
name|LazySimpleStructObjectInspector
name|result
init|=
name|cachedLazySimpleStructObjectInspector
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
switch|switch
condition|(
name|option
condition|)
block|{
case|case
name|JAVA
case|:
name|result
operator|=
operator|new
name|LazySimpleStructObjectInspector
argument_list|(
name|structFieldNames
argument_list|,
name|structFieldObjectInspectors
argument_list|,
name|structFieldComments
argument_list|,
name|separator
argument_list|,
name|lazyParams
argument_list|)
expr_stmt|;
break|break;
case|case
name|AVRO
case|:
name|result
operator|=
operator|new
name|AvroLazyObjectInspector
argument_list|(
name|structFieldNames
argument_list|,
name|structFieldObjectInspectors
argument_list|,
name|structFieldComments
argument_list|,
name|separator
argument_list|,
name|lazyParams
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Illegal ObjectInspector type ["
operator|+
name|option
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|cachedLazySimpleStructObjectInspector
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
name|ConcurrentHashMap
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|,
name|LazyListObjectInspector
argument_list|>
name|cachedLazySimpleListObjectInspector
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|,
name|LazyListObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Deprecated
specifier|public
specifier|static
name|LazyListObjectInspector
name|getLazySimpleListObjectInspector
parameter_list|(
name|ObjectInspector
name|listElementObjectInspector
parameter_list|,
name|byte
name|separator
parameter_list|,
name|Text
name|nullSequence
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|)
block|{
return|return
name|getLazySimpleListObjectInspector
argument_list|(
name|listElementObjectInspector
argument_list|,
name|separator
argument_list|,
operator|new
name|LazyObjectInspectorParametersImpl
argument_list|(
name|escaped
argument_list|,
name|escapeChar
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|nullSequence
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|LazyListObjectInspector
name|getLazySimpleListObjectInspector
parameter_list|(
name|ObjectInspector
name|listElementObjectInspector
parameter_list|,
name|byte
name|separator
parameter_list|,
name|LazyObjectInspectorParameters
name|lazyParams
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
name|listElementObjectInspector
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
name|separator
argument_list|)
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|lazyParams
operator|.
name|getNullSequence
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|LazyObjectInspectorFactory
operator|.
name|addCommonLazyParamsToSignature
argument_list|(
name|lazyParams
argument_list|,
name|signature
argument_list|)
expr_stmt|;
name|LazyListObjectInspector
name|result
init|=
name|cachedLazySimpleListObjectInspector
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
name|LazyListObjectInspector
argument_list|(
name|listElementObjectInspector
argument_list|,
name|separator
argument_list|,
name|lazyParams
argument_list|)
expr_stmt|;
name|cachedLazySimpleListObjectInspector
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
name|ConcurrentHashMap
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|,
name|LazyMapObjectInspector
argument_list|>
name|cachedLazySimpleMapObjectInspector
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|,
name|LazyMapObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Deprecated
specifier|public
specifier|static
name|LazyMapObjectInspector
name|getLazySimpleMapObjectInspector
parameter_list|(
name|ObjectInspector
name|mapKeyObjectInspector
parameter_list|,
name|ObjectInspector
name|mapValueObjectInspector
parameter_list|,
name|byte
name|itemSeparator
parameter_list|,
name|byte
name|keyValueSeparator
parameter_list|,
name|Text
name|nullSequence
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|)
block|{
return|return
name|getLazySimpleMapObjectInspector
argument_list|(
name|mapKeyObjectInspector
argument_list|,
name|mapValueObjectInspector
argument_list|,
name|itemSeparator
argument_list|,
name|keyValueSeparator
argument_list|,
operator|new
name|LazyObjectInspectorParametersImpl
argument_list|(
name|escaped
argument_list|,
name|escapeChar
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|nullSequence
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|LazyMapObjectInspector
name|getLazySimpleMapObjectInspector
parameter_list|(
name|ObjectInspector
name|mapKeyObjectInspector
parameter_list|,
name|ObjectInspector
name|mapValueObjectInspector
parameter_list|,
name|byte
name|itemSeparator
parameter_list|,
name|byte
name|keyValueSeparator
parameter_list|,
name|LazyObjectInspectorParameters
name|lazyParams
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
name|signature
operator|.
name|add
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
name|itemSeparator
argument_list|)
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
name|keyValueSeparator
argument_list|)
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|lazyParams
operator|.
name|getNullSequence
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|LazyObjectInspectorFactory
operator|.
name|addCommonLazyParamsToSignature
argument_list|(
name|lazyParams
argument_list|,
name|signature
argument_list|)
expr_stmt|;
name|LazyMapObjectInspector
name|result
init|=
name|cachedLazySimpleMapObjectInspector
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
name|LazyMapObjectInspector
argument_list|(
name|mapKeyObjectInspector
argument_list|,
name|mapValueObjectInspector
argument_list|,
name|itemSeparator
argument_list|,
name|keyValueSeparator
argument_list|,
name|lazyParams
argument_list|)
expr_stmt|;
name|cachedLazySimpleMapObjectInspector
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
name|ConcurrentHashMap
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|,
name|LazyUnionObjectInspector
argument_list|>
name|cachedLazyUnionObjectInspector
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|List
argument_list|<
name|Object
argument_list|>
argument_list|,
name|LazyUnionObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Deprecated
specifier|public
specifier|static
name|LazyUnionObjectInspector
name|getLazyUnionObjectInspector
parameter_list|(
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|ois
parameter_list|,
name|byte
name|separator
parameter_list|,
name|Text
name|nullSequence
parameter_list|,
name|boolean
name|escaped
parameter_list|,
name|byte
name|escapeChar
parameter_list|)
block|{
return|return
name|getLazyUnionObjectInspector
argument_list|(
name|ois
argument_list|,
name|separator
argument_list|,
operator|new
name|LazyObjectInspectorParametersImpl
argument_list|(
name|escaped
argument_list|,
name|escapeChar
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|nullSequence
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|LazyUnionObjectInspector
name|getLazyUnionObjectInspector
parameter_list|(
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|ois
parameter_list|,
name|byte
name|separator
parameter_list|,
name|LazyObjectInspectorParameters
name|lazyParams
parameter_list|)
block|{
name|List
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
name|ois
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
name|separator
argument_list|)
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|lazyParams
operator|.
name|getNullSequence
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|LazyObjectInspectorFactory
operator|.
name|addCommonLazyParamsToSignature
argument_list|(
name|lazyParams
argument_list|,
name|signature
argument_list|)
expr_stmt|;
name|LazyUnionObjectInspector
name|result
init|=
name|cachedLazyUnionObjectInspector
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
name|LazyUnionObjectInspector
argument_list|(
name|ois
argument_list|,
name|separator
argument_list|,
name|lazyParams
argument_list|)
expr_stmt|;
name|cachedLazyUnionObjectInspector
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
name|LazyObjectInspectorFactory
parameter_list|()
block|{
comment|// prevent instantiation
block|}
specifier|private
specifier|static
name|void
name|addCommonLazyParamsToSignature
parameter_list|(
name|LazyObjectInspectorParameters
name|lazyParams
parameter_list|,
name|List
argument_list|<
name|Object
argument_list|>
name|signature
parameter_list|)
block|{
name|signature
operator|.
name|add
argument_list|(
name|lazyParams
operator|.
name|isEscaped
argument_list|()
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|lazyParams
operator|.
name|getEscapeChar
argument_list|()
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|lazyParams
operator|.
name|isExtendedBooleanLiteral
argument_list|()
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|lazyParams
operator|.
name|getTimestampFormats
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

