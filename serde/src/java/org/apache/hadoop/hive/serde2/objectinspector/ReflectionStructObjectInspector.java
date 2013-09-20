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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * ReflectionStructObjectInspector works on struct data that is stored as a  * native Java object. It will drill down into the Java class to get the fields  * and construct ObjectInspectors for the fields, if they are not specified.  *   * Always use the ObjectInspectorFactory to create new ObjectInspector objects,  * instead of directly creating an instance of this class.  *   */
end_comment

begin_class
specifier|public
class|class
name|ReflectionStructObjectInspector
extends|extends
name|SettableStructObjectInspector
block|{
comment|/**    * MyField.    *    */
specifier|public
specifier|static
class|class
name|MyField
implements|implements
name|StructField
block|{
specifier|protected
name|Field
name|field
decl_stmt|;
specifier|protected
name|ObjectInspector
name|fieldObjectInspector
decl_stmt|;
specifier|protected
name|MyField
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|MyField
parameter_list|(
name|Field
name|field
parameter_list|,
name|ObjectInspector
name|fieldObjectInspector
parameter_list|)
block|{
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
name|this
operator|.
name|fieldObjectInspector
operator|=
name|fieldObjectInspector
expr_stmt|;
block|}
specifier|public
name|String
name|getFieldName
parameter_list|()
block|{
return|return
name|field
operator|.
name|getName
argument_list|()
operator|.
name|toLowerCase
argument_list|()
return|;
block|}
specifier|public
name|ObjectInspector
name|getFieldObjectInspector
parameter_list|()
block|{
return|return
name|fieldObjectInspector
return|;
block|}
specifier|public
name|String
name|getFieldComment
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|field
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
name|Class
argument_list|<
name|?
argument_list|>
name|objectClass
decl_stmt|;
name|List
argument_list|<
name|MyField
argument_list|>
name|fields
decl_stmt|;
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
name|String
name|getTypeName
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"struct<"
argument_list|)
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
for|for
control|(
name|StructField
name|structField
range|:
name|getAllStructFieldRefs
argument_list|()
control|)
block|{
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|structField
operator|.
name|getFieldName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
operator|.
name|append
argument_list|(
name|structField
operator|.
name|getFieldObjectInspector
argument_list|()
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
literal|">"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * This method is only intended to be used by the Utilities class in this    * package. This creates an uninitialized ObjectInspector so the Utilities    * class can put it into a cache before it initializes when it might look up    * the cache for member fields that might be of the same type (e.g. recursive    * type like linked list and trees).    */
name|ReflectionStructObjectInspector
parameter_list|()
block|{   }
comment|/**    * This method is only intended to be used by Utilities class in this package.    * The reason that this method is not recursive by itself is because we want    * to allow recursive types.    */
name|void
name|init
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|objectClass
parameter_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|structFieldObjectInspectors
parameter_list|)
block|{
assert|assert
operator|(
operator|!
name|List
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|objectClass
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
name|objectClass
argument_list|)
operator|)
assert|;
name|this
operator|.
name|objectClass
operator|=
name|objectClass
expr_stmt|;
name|Field
index|[]
name|reflectionFields
init|=
name|ObjectInspectorUtils
operator|.
name|getDeclaredNonStaticFields
argument_list|(
name|objectClass
argument_list|)
decl_stmt|;
name|fields
operator|=
operator|new
name|ArrayList
argument_list|<
name|MyField
argument_list|>
argument_list|(
name|structFieldObjectInspectors
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|used
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
name|reflectionFields
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
name|shouldIgnoreField
argument_list|(
name|reflectionFields
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|reflectionFields
index|[
name|i
index|]
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|fields
operator|.
name|add
argument_list|(
operator|new
name|MyField
argument_list|(
name|reflectionFields
index|[
name|i
index|]
argument_list|,
name|structFieldObjectInspectors
operator|.
name|get
argument_list|(
name|used
operator|++
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
assert|assert
operator|(
name|fields
operator|.
name|size
argument_list|()
operator|==
name|structFieldObjectInspectors
operator|.
name|size
argument_list|()
operator|)
assert|;
block|}
comment|// ThriftStructObjectInspector will override and ignore __isset fields.
specifier|public
name|boolean
name|shouldIgnoreField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
literal|false
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
if|if
condition|(
operator|!
operator|(
name|fieldRef
operator|instanceof
name|MyField
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"fieldRef has to be of MyField"
argument_list|)
throw|;
block|}
name|MyField
name|f
init|=
operator|(
name|MyField
operator|)
name|fieldRef
decl_stmt|;
try|try
block|{
name|Object
name|r
init|=
name|f
operator|.
name|field
operator|.
name|get
argument_list|(
name|data
argument_list|)
decl_stmt|;
return|return
name|r
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
name|RuntimeException
argument_list|(
literal|"cannot get field "
operator|+
name|f
operator|.
name|field
operator|+
literal|" from "
operator|+
name|data
operator|.
name|getClass
argument_list|()
operator|+
literal|" "
operator|+
name|data
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
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
try|try
block|{
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
name|fields
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
name|add
argument_list|(
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|field
operator|.
name|get
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Object
name|create
parameter_list|()
block|{
return|return
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|objectClass
argument_list|,
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|setStructFieldData
parameter_list|(
name|Object
name|struct
parameter_list|,
name|StructField
name|field
parameter_list|,
name|Object
name|fieldValue
parameter_list|)
block|{
name|MyField
name|myField
init|=
operator|(
name|MyField
operator|)
name|field
decl_stmt|;
try|try
block|{
name|myField
operator|.
name|field
operator|.
name|set
argument_list|(
name|struct
argument_list|,
name|fieldValue
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
name|RuntimeException
argument_list|(
literal|"cannot set field "
operator|+
name|myField
operator|.
name|field
operator|+
literal|" of "
operator|+
name|struct
operator|.
name|getClass
argument_list|()
operator|+
literal|" "
operator|+
name|struct
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|struct
return|;
block|}
block|}
end_class

end_unit

