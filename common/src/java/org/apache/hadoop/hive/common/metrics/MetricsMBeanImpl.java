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
name|common
operator|.
name|metrics
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|Map
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|Attribute
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|AttributeList
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|AttributeNotFoundException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|InvalidAttributeValueException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanAttributeInfo
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanConstructorInfo
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanInfo
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanNotificationInfo
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|MBeanOperationInfo
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|management
operator|.
name|ReflectionException
import|;
end_import

begin_class
specifier|public
class|class
name|MetricsMBeanImpl
implements|implements
name|MetricsMBean
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metricsMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|MBeanAttributeInfo
index|[]
name|attributeInfos
decl_stmt|;
specifier|private
name|boolean
name|dirtyAttributeInfoCache
init|=
literal|true
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|MBeanConstructorInfo
index|[]
name|ctors
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|MBeanOperationInfo
index|[]
name|ops
init|=
block|{
operator|new
name|MBeanOperationInfo
argument_list|(
literal|"reset"
argument_list|,
literal|"Sets the values of all Attributes to 0"
argument_list|,
literal|null
argument_list|,
literal|"void"
argument_list|,
name|MBeanOperationInfo
operator|.
name|ACTION
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|MBeanNotificationInfo
index|[]
name|notifs
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Object
name|getAttribute
parameter_list|(
name|String
name|arg0
parameter_list|)
throws|throws
name|AttributeNotFoundException
throws|,
name|MBeanException
throws|,
name|ReflectionException
block|{
synchronized|synchronized
init|(
name|metricsMap
init|)
block|{
if|if
condition|(
name|metricsMap
operator|.
name|containsKey
argument_list|(
name|arg0
argument_list|)
condition|)
block|{
return|return
name|metricsMap
operator|.
name|get
argument_list|(
name|arg0
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|AttributeNotFoundException
argument_list|(
literal|"Key ["
operator|+
name|arg0
operator|+
literal|"] not found/tracked"
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|AttributeList
name|getAttributes
parameter_list|(
name|String
index|[]
name|arg0
parameter_list|)
block|{
name|AttributeList
name|results
init|=
operator|new
name|AttributeList
argument_list|()
decl_stmt|;
synchronized|synchronized
init|(
name|metricsMap
init|)
block|{
for|for
control|(
name|String
name|key
range|:
name|arg0
control|)
block|{
name|results
operator|.
name|add
argument_list|(
operator|new
name|Attribute
argument_list|(
name|key
argument_list|,
name|metricsMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|results
return|;
block|}
annotation|@
name|Override
specifier|public
name|MBeanInfo
name|getMBeanInfo
parameter_list|()
block|{
if|if
condition|(
name|dirtyAttributeInfoCache
condition|)
block|{
synchronized|synchronized
init|(
name|metricsMap
init|)
block|{
name|attributeInfos
operator|=
operator|new
name|MBeanAttributeInfo
index|[
name|metricsMap
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|metricsMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|attributeInfos
index|[
name|i
index|]
operator|=
operator|new
name|MBeanAttributeInfo
argument_list|(
name|key
argument_list|,
name|metricsMap
operator|.
name|get
argument_list|(
name|key
argument_list|)
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|key
argument_list|,
literal|true
argument_list|,
literal|true
comment|/*writable*/
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
name|dirtyAttributeInfoCache
operator|=
literal|false
expr_stmt|;
block|}
block|}
return|return
operator|new
name|MBeanInfo
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
literal|"metrics information"
argument_list|,
name|attributeInfos
argument_list|,
name|ctors
argument_list|,
name|ops
argument_list|,
name|notifs
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|invoke
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
index|[]
name|args
parameter_list|,
name|String
index|[]
name|signature
parameter_list|)
throws|throws
name|MBeanException
throws|,
name|ReflectionException
block|{
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"reset"
argument_list|)
condition|)
block|{
name|reset
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
throw|throw
operator|new
name|ReflectionException
argument_list|(
operator|new
name|NoSuchMethodException
argument_list|(
name|name
argument_list|)
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setAttribute
parameter_list|(
name|Attribute
name|attr
parameter_list|)
throws|throws
name|AttributeNotFoundException
throws|,
name|InvalidAttributeValueException
throws|,
name|MBeanException
throws|,
name|ReflectionException
block|{
try|try
block|{
name|put
argument_list|(
name|attr
operator|.
name|getName
argument_list|()
argument_list|,
name|attr
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MBeanException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AttributeList
name|setAttributes
parameter_list|(
name|AttributeList
name|arg0
parameter_list|)
block|{
name|AttributeList
name|attributesSet
init|=
operator|new
name|AttributeList
argument_list|()
decl_stmt|;
for|for
control|(
name|Attribute
name|attr
range|:
name|arg0
operator|.
name|asList
argument_list|()
control|)
block|{
try|try
block|{
name|setAttribute
argument_list|(
name|attr
argument_list|)
expr_stmt|;
name|attributesSet
operator|.
name|add
argument_list|(
name|attr
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AttributeNotFoundException
name|e
parameter_list|)
block|{
comment|// ignore exception - we simply don't add this attribute
comment|// back in to the resultant set.
block|}
catch|catch
parameter_list|(
name|InvalidAttributeValueException
name|e
parameter_list|)
block|{
comment|// ditto
block|}
catch|catch
parameter_list|(
name|MBeanException
name|e
parameter_list|)
block|{
comment|// likewise
block|}
catch|catch
parameter_list|(
name|ReflectionException
name|e
parameter_list|)
block|{
comment|// and again, one last time.
block|}
block|}
return|return
name|attributesSet
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasKey
parameter_list|(
name|String
name|name
parameter_list|)
block|{
synchronized|synchronized
init|(
name|metricsMap
init|)
block|{
return|return
name|metricsMap
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|put
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
throws|throws
name|IOException
block|{
synchronized|synchronized
init|(
name|metricsMap
init|)
block|{
if|if
condition|(
operator|!
name|metricsMap
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|dirtyAttributeInfoCache
operator|=
literal|true
expr_stmt|;
block|}
name|metricsMap
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|get
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|getAttribute
argument_list|(
name|name
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|AttributeNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|MBeanException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|ReflectionException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
synchronized|synchronized
init|(
name|metricsMap
init|)
block|{
for|for
control|(
name|String
name|key
range|:
name|metricsMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|metricsMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
synchronized|synchronized
init|(
name|metricsMap
init|)
block|{
name|attributeInfos
operator|=
literal|null
expr_stmt|;
name|dirtyAttributeInfoCache
operator|=
literal|true
expr_stmt|;
name|metricsMap
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

