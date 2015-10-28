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
name|Arrays
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * ListStructObjectInspector works on struct data that is stored as a Java List  * or Java Array object. Basically, the fields are stored sequentially in the  * List object.  *  * The names of the struct fields and the internal structure of the struct  * fields are specified in the ctor of the StructObjectInspector.  *  * Always use the ObjectInspectorFactory to create new ObjectInspector objects,  * instead of directly creating an instance of this class.  */
end_comment

begin_class
specifier|public
class|class
name|StandardStructObjectInspector
extends|extends
name|SettableStructObjectInspector
block|{
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|StandardStructObjectInspector
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
class|class
name|MyField
implements|implements
name|StructField
block|{
specifier|protected
name|int
name|fieldID
decl_stmt|;
specifier|protected
name|String
name|fieldName
decl_stmt|;
specifier|protected
name|ObjectInspector
name|fieldObjectInspector
decl_stmt|;
specifier|protected
name|String
name|fieldComment
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
name|int
name|fieldID
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|ObjectInspector
name|fieldObjectInspector
parameter_list|)
block|{
name|this
operator|.
name|fieldID
operator|=
name|fieldID
expr_stmt|;
name|this
operator|.
name|fieldName
operator|=
name|fieldName
operator|.
name|toLowerCase
argument_list|()
expr_stmt|;
name|this
operator|.
name|fieldObjectInspector
operator|=
name|fieldObjectInspector
expr_stmt|;
block|}
specifier|public
name|MyField
parameter_list|(
name|int
name|fieldID
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|ObjectInspector
name|fieldObjectInspector
parameter_list|,
name|String
name|fieldComment
parameter_list|)
block|{
name|this
argument_list|(
name|fieldID
argument_list|,
name|fieldName
argument_list|,
name|fieldObjectInspector
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldComment
operator|=
name|fieldComment
expr_stmt|;
block|}
specifier|public
name|int
name|getFieldID
parameter_list|()
block|{
return|return
name|fieldID
return|;
block|}
specifier|public
name|String
name|getFieldName
parameter_list|()
block|{
return|return
name|fieldName
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
name|fieldComment
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
literal|""
operator|+
name|fieldID
operator|+
literal|":"
operator|+
name|fieldName
return|;
block|}
block|}
specifier|protected
name|List
argument_list|<
name|MyField
argument_list|>
name|fields
decl_stmt|;
specifier|protected
name|StandardStructObjectInspector
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Call ObjectInspectorFactory.getStandardListObjectInspector instead.    */
specifier|protected
name|StandardStructObjectInspector
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
name|init
argument_list|(
name|structFieldNames
argument_list|,
name|structFieldObjectInspectors
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**   * Call ObjectInspectorFactory.getStandardListObjectInspector instead.   */
specifier|protected
name|StandardStructObjectInspector
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
name|init
argument_list|(
name|structFieldNames
argument_list|,
name|structFieldObjectInspectors
argument_list|,
name|structFieldComments
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|init
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
name|fields
operator|=
operator|new
name|ArrayList
argument_list|<
name|MyField
argument_list|>
argument_list|(
name|structFieldNames
operator|.
name|size
argument_list|()
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
name|structFieldNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
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
name|structFieldNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|structFieldObjectInspectors
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|structFieldComments
operator|==
literal|null
condition|?
literal|null
else|:
name|structFieldComments
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|StandardStructObjectInspector
parameter_list|(
name|List
argument_list|<
name|StructField
argument_list|>
name|fields
parameter_list|)
block|{
name|init
argument_list|(
name|fields
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|init
parameter_list|(
name|List
argument_list|<
name|StructField
argument_list|>
name|fields
parameter_list|)
block|{
name|this
operator|.
name|fields
operator|=
operator|new
name|ArrayList
argument_list|<
name|MyField
argument_list|>
argument_list|(
name|fields
operator|.
name|size
argument_list|()
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
name|fields
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|fields
operator|.
name|add
argument_list|(
operator|new
name|MyField
argument_list|(
name|i
argument_list|,
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldName
argument_list|()
argument_list|,
name|fields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|boolean
name|warned
init|=
literal|false
decl_stmt|;
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
comment|// We support both List<Object> and Object[]
comment|// so we have to do differently.
name|boolean
name|isArray
init|=
name|data
operator|.
name|getClass
argument_list|()
operator|.
name|isArray
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|isArray
operator|&&
operator|!
operator|(
name|data
operator|instanceof
name|List
operator|)
condition|)
block|{
if|if
condition|(
operator|!
name|warned
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Invalid type for struct "
operator|+
name|data
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"ignoring similar errors."
argument_list|)
expr_stmt|;
name|warned
operator|=
literal|true
expr_stmt|;
block|}
return|return
name|data
return|;
block|}
name|int
name|listSize
init|=
operator|(
name|isArray
condition|?
operator|(
operator|(
name|Object
index|[]
operator|)
name|data
operator|)
operator|.
name|length
else|:
operator|(
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|data
operator|)
operator|.
name|size
argument_list|()
operator|)
decl_stmt|;
name|MyField
name|f
init|=
operator|(
name|MyField
operator|)
name|fieldRef
decl_stmt|;
if|if
condition|(
name|fields
operator|.
name|size
argument_list|()
operator|!=
name|listSize
operator|&&
operator|!
name|warned
condition|)
block|{
comment|// TODO: remove this
name|warned
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Trying to access "
operator|+
name|fields
operator|.
name|size
argument_list|()
operator|+
literal|" fields inside a list of "
operator|+
name|listSize
operator|+
literal|" elements: "
operator|+
operator|(
name|isArray
condition|?
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
else|:
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|data
operator|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"ignoring similar errors."
argument_list|)
expr_stmt|;
block|}
name|int
name|fieldID
init|=
name|f
operator|.
name|getFieldID
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldID
operator|>=
name|listSize
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|isArray
condition|)
block|{
return|return
operator|(
operator|(
name|Object
index|[]
operator|)
name|data
operator|)
index|[
name|fieldID
index|]
return|;
block|}
else|else
block|{
return|return
operator|(
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|data
operator|)
operator|.
name|get
argument_list|(
name|fieldID
argument_list|)
return|;
block|}
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
operator|!
operator|(
name|data
operator|instanceof
name|List
operator|)
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
return|return
name|list
return|;
block|}
comment|// /////////////////////////////
comment|// SettableStructObjectInspector
annotation|@
name|Override
specifier|public
name|Object
name|create
parameter_list|()
block|{
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|a
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
name|a
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
name|a
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
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|a
init|=
operator|(
name|ArrayList
argument_list|<
name|Object
argument_list|>
operator|)
name|struct
decl_stmt|;
name|MyField
name|myField
init|=
operator|(
name|MyField
operator|)
name|field
decl_stmt|;
name|a
operator|.
name|set
argument_list|(
name|myField
operator|.
name|fieldID
argument_list|,
name|fieldValue
argument_list|)
expr_stmt|;
return|return
name|a
return|;
block|}
block|}
end_class

end_unit

