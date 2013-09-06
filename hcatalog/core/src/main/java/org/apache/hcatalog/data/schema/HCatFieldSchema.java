begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|schema
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|builder
operator|.
name|ToStringBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_class
specifier|public
class|class
name|HCatFieldSchema
implements|implements
name|Serializable
block|{
specifier|public
enum|enum
name|Type
block|{
name|INT
block|,
name|TINYINT
block|,
name|SMALLINT
block|,
name|BIGINT
block|,
name|BOOLEAN
block|,
name|FLOAT
block|,
name|DOUBLE
block|,
name|STRING
block|,
name|ARRAY
block|,
name|MAP
block|,
name|STRUCT
block|,
name|BINARY
block|,     }
specifier|public
enum|enum
name|Category
block|{
name|PRIMITIVE
block|,
name|ARRAY
block|,
name|MAP
block|,
name|STRUCT
block|;
specifier|public
specifier|static
name|Category
name|fromType
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
if|if
condition|(
name|Type
operator|.
name|ARRAY
operator|==
name|type
condition|)
block|{
return|return
name|ARRAY
return|;
block|}
elseif|else
if|if
condition|(
name|Type
operator|.
name|STRUCT
operator|==
name|type
condition|)
block|{
return|return
name|STRUCT
return|;
block|}
elseif|else
if|if
condition|(
name|Type
operator|.
name|MAP
operator|==
name|type
condition|)
block|{
return|return
name|MAP
return|;
block|}
else|else
block|{
return|return
name|PRIMITIVE
return|;
block|}
block|}
block|}
empty_stmt|;
specifier|public
name|boolean
name|isComplex
parameter_list|()
block|{
return|return
operator|(
name|category
operator|==
name|Category
operator|.
name|PRIMITIVE
operator|)
condition|?
literal|false
else|:
literal|true
return|;
block|}
comment|/**      *      */
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
name|String
name|comment
init|=
literal|null
decl_stmt|;
name|Type
name|type
init|=
literal|null
decl_stmt|;
name|Category
name|category
init|=
literal|null
decl_stmt|;
comment|// Populated if column is struct, array or map types.
comment|// If struct type, contains schema of the struct.
comment|// If array type, contains schema of one of the elements.
comment|// If map type, contains schema of the value element.
name|HCatSchema
name|subSchema
init|=
literal|null
decl_stmt|;
comment|// populated if column is Map type
name|Type
name|mapKeyType
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|typeString
init|=
literal|null
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
name|HCatFieldSchema
parameter_list|()
block|{
comment|// preventing empty ctor from being callable
block|}
comment|/**      * Returns type of the field      * @return type of the field      */
specifier|public
name|Type
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/**      * Returns category of the field      * @return category of the field      */
specifier|public
name|Category
name|getCategory
parameter_list|()
block|{
return|return
name|category
return|;
block|}
comment|/**      * Returns name of the field      * @return name of the field      */
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|fieldName
return|;
block|}
specifier|public
name|String
name|getComment
parameter_list|()
block|{
return|return
name|comment
return|;
block|}
comment|/**      * Constructor constructing a primitive datatype HCatFieldSchema      * @param fieldName Name of the primitive field      * @param type Type of the primitive field      * @throws HCatException if call made on non-primitive types      */
specifier|public
name|HCatFieldSchema
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Type
name|type
parameter_list|,
name|String
name|comment
parameter_list|)
throws|throws
name|HCatException
block|{
name|assertTypeInCategory
argument_list|(
name|type
argument_list|,
name|Category
operator|.
name|PRIMITIVE
argument_list|,
name|fieldName
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|category
operator|=
name|Category
operator|.
name|PRIMITIVE
expr_stmt|;
name|this
operator|.
name|comment
operator|=
name|comment
expr_stmt|;
block|}
comment|/**      * Constructor for constructing a ARRAY type or STRUCT type HCatFieldSchema, passing type and subschema      * @param fieldName Name of the array or struct field      * @param type Type of the field - either Type.ARRAY or Type.STRUCT      * @param subSchema - subschema of the struct, or element schema of the elements in the array      * @throws HCatException if call made on Primitive or Map types      */
specifier|public
name|HCatFieldSchema
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Type
name|type
parameter_list|,
name|HCatSchema
name|subSchema
parameter_list|,
name|String
name|comment
parameter_list|)
throws|throws
name|HCatException
block|{
name|assertTypeNotInCategory
argument_list|(
name|type
argument_list|,
name|Category
operator|.
name|PRIMITIVE
argument_list|)
expr_stmt|;
name|assertTypeNotInCategory
argument_list|(
name|type
argument_list|,
name|Category
operator|.
name|MAP
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|category
operator|=
name|Category
operator|.
name|fromType
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|this
operator|.
name|subSchema
operator|=
name|subSchema
expr_stmt|;
if|if
condition|(
name|type
operator|==
name|Type
operator|.
name|ARRAY
condition|)
block|{
name|this
operator|.
name|subSchema
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|setName
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|comment
operator|=
name|comment
expr_stmt|;
block|}
specifier|private
name|void
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|fieldName
operator|=
name|name
expr_stmt|;
block|}
comment|/**      * Constructor for constructing a MAP type HCatFieldSchema, passing type of key and value      * @param fieldName Name of the array or struct field      * @param type Type of the field - must be Type.MAP      * @param mapKeyType - key type of the Map      * @param mapValueSchema - subschema of the value of the Map      * @throws HCatException if call made on non-Map types      */
specifier|public
name|HCatFieldSchema
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Type
name|type
parameter_list|,
name|Type
name|mapKeyType
parameter_list|,
name|HCatSchema
name|mapValueSchema
parameter_list|,
name|String
name|comment
parameter_list|)
throws|throws
name|HCatException
block|{
name|assertTypeInCategory
argument_list|(
name|type
argument_list|,
name|Category
operator|.
name|MAP
argument_list|,
name|fieldName
argument_list|)
expr_stmt|;
name|assertTypeInCategory
argument_list|(
name|mapKeyType
argument_list|,
name|Category
operator|.
name|PRIMITIVE
argument_list|,
name|fieldName
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|MAP
expr_stmt|;
name|this
operator|.
name|category
operator|=
name|Category
operator|.
name|MAP
expr_stmt|;
name|this
operator|.
name|mapKeyType
operator|=
name|mapKeyType
expr_stmt|;
name|this
operator|.
name|subSchema
operator|=
name|mapValueSchema
expr_stmt|;
name|this
operator|.
name|subSchema
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|setName
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|comment
operator|=
name|comment
expr_stmt|;
block|}
specifier|public
name|HCatSchema
name|getStructSubSchema
parameter_list|()
throws|throws
name|HCatException
block|{
name|assertTypeInCategory
argument_list|(
name|this
operator|.
name|type
argument_list|,
name|Category
operator|.
name|STRUCT
argument_list|,
name|this
operator|.
name|fieldName
argument_list|)
expr_stmt|;
return|return
name|subSchema
return|;
block|}
specifier|public
name|HCatSchema
name|getArrayElementSchema
parameter_list|()
throws|throws
name|HCatException
block|{
name|assertTypeInCategory
argument_list|(
name|this
operator|.
name|type
argument_list|,
name|Category
operator|.
name|ARRAY
argument_list|,
name|this
operator|.
name|fieldName
argument_list|)
expr_stmt|;
return|return
name|subSchema
return|;
block|}
specifier|public
name|Type
name|getMapKeyType
parameter_list|()
throws|throws
name|HCatException
block|{
name|assertTypeInCategory
argument_list|(
name|this
operator|.
name|type
argument_list|,
name|Category
operator|.
name|MAP
argument_list|,
name|this
operator|.
name|fieldName
argument_list|)
expr_stmt|;
return|return
name|mapKeyType
return|;
block|}
specifier|public
name|HCatSchema
name|getMapValueSchema
parameter_list|()
throws|throws
name|HCatException
block|{
name|assertTypeInCategory
argument_list|(
name|this
operator|.
name|type
argument_list|,
name|Category
operator|.
name|MAP
argument_list|,
name|this
operator|.
name|fieldName
argument_list|)
expr_stmt|;
return|return
name|subSchema
return|;
block|}
specifier|private
specifier|static
name|void
name|assertTypeInCategory
parameter_list|(
name|Type
name|type
parameter_list|,
name|Category
name|category
parameter_list|,
name|String
name|fieldName
parameter_list|)
throws|throws
name|HCatException
block|{
name|Category
name|typeCategory
init|=
name|Category
operator|.
name|fromType
argument_list|(
name|type
argument_list|)
decl_stmt|;
if|if
condition|(
name|typeCategory
operator|!=
name|category
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Type category mismatch. Expected "
operator|+
name|category
operator|+
literal|" but type "
operator|+
name|type
operator|+
literal|" in category "
operator|+
name|typeCategory
operator|+
literal|" (field "
operator|+
name|fieldName
operator|+
literal|")"
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|assertTypeNotInCategory
parameter_list|(
name|Type
name|type
parameter_list|,
name|Category
name|category
parameter_list|)
throws|throws
name|HCatException
block|{
name|Category
name|typeCategory
init|=
name|Category
operator|.
name|fromType
argument_list|(
name|type
argument_list|)
decl_stmt|;
if|if
condition|(
name|typeCategory
operator|==
name|category
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Type category mismatch. Expected type "
operator|+
name|type
operator|+
literal|" not in category "
operator|+
name|category
operator|+
literal|" but was so."
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
operator|new
name|ToStringBuilder
argument_list|(
name|this
argument_list|)
operator|.
name|append
argument_list|(
literal|"fieldName"
argument_list|,
name|fieldName
argument_list|)
operator|.
name|append
argument_list|(
literal|"comment"
argument_list|,
name|comment
argument_list|)
operator|.
name|append
argument_list|(
literal|"type"
argument_list|,
name|getTypeString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"category"
argument_list|,
name|category
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|String
name|getTypeString
parameter_list|()
block|{
if|if
condition|(
name|typeString
operator|!=
literal|null
condition|)
block|{
return|return
name|typeString
return|;
block|}
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|Category
operator|.
name|PRIMITIVE
operator|==
name|category
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Category
operator|.
name|STRUCT
operator|==
name|category
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"struct<"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|subSchema
operator|.
name|getSchemaAsTypeString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|">"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Category
operator|.
name|ARRAY
operator|==
name|category
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"array<"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|subSchema
operator|.
name|getSchemaAsTypeString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|">"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Category
operator|.
name|MAP
operator|==
name|category
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"map<"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|mapKeyType
argument_list|)
expr_stmt|;
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
name|subSchema
operator|.
name|getSchemaAsTypeString
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|">"
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|typeString
operator|=
name|sb
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|HCatFieldSchema
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|HCatFieldSchema
name|other
init|=
operator|(
name|HCatFieldSchema
operator|)
name|obj
decl_stmt|;
if|if
condition|(
name|category
operator|!=
name|other
operator|.
name|category
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|fieldName
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|other
operator|.
name|fieldName
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|fieldName
operator|.
name|equals
argument_list|(
name|other
operator|.
name|fieldName
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|this
operator|.
name|getTypeString
argument_list|()
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|other
operator|.
name|getTypeString
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
elseif|else
if|if
condition|(
operator|!
name|this
operator|.
name|getTypeString
argument_list|()
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getTypeString
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

