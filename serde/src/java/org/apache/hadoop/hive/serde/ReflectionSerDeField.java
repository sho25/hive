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
name|serde
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
name|*
import|;
end_import

begin_comment
comment|/**  * The default implementation of Hive Field based on Java Reflection.  */
end_comment

begin_class
specifier|public
class|class
name|ReflectionSerDeField
implements|implements
name|SerDeField
block|{
specifier|protected
name|Class
name|_parentClass
decl_stmt|;
specifier|protected
name|Class
name|_class
decl_stmt|;
specifier|protected
name|Field
name|_field
decl_stmt|;
specifier|protected
name|boolean
name|_isList
decl_stmt|;
specifier|protected
name|boolean
name|_isMap
decl_stmt|;
specifier|protected
name|boolean
name|_isClassPrimitive
decl_stmt|;
specifier|protected
name|Class
name|_valueClass
decl_stmt|;
specifier|protected
name|Class
name|_keyClass
decl_stmt|;
specifier|public
specifier|static
name|boolean
name|isClassPrimitive
parameter_list|(
name|Class
name|c
parameter_list|)
block|{
return|return
operator|(
operator|(
name|c
operator|==
name|String
operator|.
name|class
operator|)
operator|||
operator|(
name|c
operator|==
name|Boolean
operator|.
name|class
operator|)
operator|||
operator|(
name|c
operator|==
name|Character
operator|.
name|class
operator|)
operator|||
name|java
operator|.
name|lang
operator|.
name|Number
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|c
argument_list|)
operator|||
name|c
operator|.
name|isPrimitive
argument_list|()
operator|)
return|;
block|}
specifier|public
name|ReflectionSerDeField
parameter_list|(
name|String
name|className
parameter_list|,
name|String
name|fieldName
parameter_list|)
throws|throws
name|SerDeException
block|{
try|try
block|{
name|_parentClass
operator|=
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
expr_stmt|;
name|_field
operator|=
name|_parentClass
operator|.
name|getDeclaredField
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|_isList
operator|=
name|java
operator|.
name|util
operator|.
name|List
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|_field
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|_isMap
operator|=
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|_field
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|_class
operator|=
name|_field
operator|.
name|getType
argument_list|()
expr_stmt|;
if|if
condition|(
name|_isList
operator|||
name|_isMap
condition|)
block|{
name|ParameterizedType
name|ptype
init|=
operator|(
name|ParameterizedType
operator|)
name|_field
operator|.
name|getGenericType
argument_list|()
decl_stmt|;
name|Type
index|[]
name|targs
init|=
name|ptype
operator|.
name|getActualTypeArguments
argument_list|()
decl_stmt|;
if|if
condition|(
name|_isList
condition|)
block|{
name|_valueClass
operator|=
operator|(
operator|(
name|Class
operator|)
name|targs
index|[
literal|0
index|]
operator|)
expr_stmt|;
block|}
else|else
block|{
name|_keyClass
operator|=
operator|(
operator|(
name|Class
operator|)
name|targs
index|[
literal|0
index|]
operator|)
expr_stmt|;
name|_valueClass
operator|=
operator|(
operator|(
name|Class
operator|)
name|targs
index|[
literal|1
index|]
operator|)
expr_stmt|;
block|}
name|_isClassPrimitive
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|_isClassPrimitive
operator|=
name|isClassPrimitive
argument_list|(
name|_class
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Illegal class or member:"
operator|+
name|className
operator|+
literal|"."
operator|+
name|fieldName
operator|+
literal|":"
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|Object
name|get
parameter_list|(
name|Object
name|obj
parameter_list|)
throws|throws
name|SerDeException
block|{
try|try
block|{
return|return
operator|(
name|_field
operator|.
name|get
argument_list|(
name|obj
argument_list|)
operator|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Illegal object or access error"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|boolean
name|isList
parameter_list|()
block|{
return|return
name|_isList
return|;
block|}
specifier|public
name|boolean
name|isMap
parameter_list|()
block|{
return|return
name|_isMap
return|;
block|}
specifier|public
name|boolean
name|isPrimitive
parameter_list|()
block|{
if|if
condition|(
name|_isList
operator|||
name|_isMap
condition|)
return|return
literal|false
return|;
return|return
name|_isClassPrimitive
return|;
block|}
specifier|public
name|Class
name|getType
parameter_list|()
block|{
return|return
name|_class
return|;
block|}
specifier|public
name|Class
name|getListElementType
parameter_list|()
block|{
if|if
condition|(
name|_isList
condition|)
block|{
return|return
name|_valueClass
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not a list field "
argument_list|)
throw|;
block|}
block|}
specifier|public
name|Class
name|getMapKeyType
parameter_list|()
block|{
if|if
condition|(
name|_isMap
condition|)
block|{
return|return
name|_keyClass
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not a map field "
argument_list|)
throw|;
block|}
block|}
specifier|public
name|Class
name|getMapValueType
parameter_list|()
block|{
if|if
condition|(
name|_isMap
condition|)
block|{
return|return
name|_valueClass
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Not a map field "
argument_list|)
throw|;
block|}
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|_field
operator|.
name|getName
argument_list|()
return|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|fieldToString
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|fieldToString
parameter_list|(
name|SerDeField
name|hf
parameter_list|)
block|{
return|return
operator|(
literal|"Field="
operator|+
name|hf
operator|.
name|getName
argument_list|()
operator|+
literal|", isPrimitive="
operator|+
name|hf
operator|.
name|isPrimitive
argument_list|()
operator|+
literal|", isList="
operator|+
name|hf
operator|.
name|isList
argument_list|()
operator|+
operator|(
name|hf
operator|.
name|isList
argument_list|()
condition|?
literal|" of "
operator|+
name|hf
operator|.
name|getListElementType
argument_list|()
operator|.
name|getName
argument_list|()
else|:
literal|""
operator|)
operator|+
literal|", isMap="
operator|+
name|hf
operator|.
name|isMap
argument_list|()
operator|+
operator|(
name|hf
operator|.
name|isMap
argument_list|()
condition|?
literal|" of<"
operator|+
name|hf
operator|.
name|getMapKeyType
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|","
operator|+
name|hf
operator|.
name|getMapValueType
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|">"
else|:
literal|""
operator|)
operator|+
literal|", type="
operator|+
name|hf
operator|.
name|getType
argument_list|()
operator|.
name|getName
argument_list|()
operator|)
return|;
block|}
block|}
end_class

end_unit

