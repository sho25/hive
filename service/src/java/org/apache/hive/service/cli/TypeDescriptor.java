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
name|hive
operator|.
name|service
operator|.
name|cli
package|;
end_package

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
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|typeinfo
operator|.
name|TypeInfoFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TPrimitiveTypeEntry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeDesc
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
operator|.
name|TTypeEntry
import|;
end_import

begin_comment
comment|/**  * TypeDescriptor.  *  */
end_comment

begin_class
specifier|public
class|class
name|TypeDescriptor
block|{
specifier|private
specifier|final
name|Type
name|type
decl_stmt|;
specifier|private
name|String
name|typeName
init|=
literal|null
decl_stmt|;
specifier|private
name|TypeQualifiers
name|typeQualifiers
init|=
literal|null
decl_stmt|;
specifier|public
name|TypeDescriptor
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
specifier|public
name|TypeDescriptor
parameter_list|(
name|TTypeDesc
name|tTypeDesc
parameter_list|)
block|{
name|List
argument_list|<
name|TTypeEntry
argument_list|>
name|tTypeEntries
init|=
name|tTypeDesc
operator|.
name|getTypes
argument_list|()
decl_stmt|;
name|TPrimitiveTypeEntry
name|top
init|=
name|tTypeEntries
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getPrimitiveEntry
argument_list|()
decl_stmt|;
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|getType
argument_list|(
name|top
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|top
operator|.
name|isSetTypeQualifiers
argument_list|()
condition|)
block|{
name|setTypeQualifiers
argument_list|(
name|TypeQualifiers
operator|.
name|fromTTypeQualifiers
argument_list|(
name|top
operator|.
name|getTypeQualifiers
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|TypeDescriptor
parameter_list|(
name|String
name|typeName
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|Type
operator|.
name|getType
argument_list|(
name|typeName
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|type
operator|.
name|isComplexType
argument_list|()
condition|)
block|{
name|this
operator|.
name|typeName
operator|=
name|typeName
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|type
operator|.
name|isQualifiedType
argument_list|()
condition|)
block|{
name|PrimitiveTypeInfo
name|pti
init|=
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|typeName
argument_list|)
decl_stmt|;
name|setTypeQualifiers
argument_list|(
name|TypeQualifiers
operator|.
name|fromTypeInfo
argument_list|(
name|pti
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|Type
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
specifier|public
name|TTypeDesc
name|toTTypeDesc
parameter_list|()
block|{
name|TPrimitiveTypeEntry
name|primitiveEntry
init|=
operator|new
name|TPrimitiveTypeEntry
argument_list|(
name|type
operator|.
name|toTType
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|getTypeQualifiers
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|primitiveEntry
operator|.
name|setTypeQualifiers
argument_list|(
name|getTypeQualifiers
argument_list|()
operator|.
name|toTTypeQualifiers
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|TTypeEntry
name|entry
init|=
name|TTypeEntry
operator|.
name|primitiveEntry
argument_list|(
name|primitiveEntry
argument_list|)
decl_stmt|;
name|TTypeDesc
name|desc
init|=
operator|new
name|TTypeDesc
argument_list|()
decl_stmt|;
name|desc
operator|.
name|addToTypes
argument_list|(
name|entry
argument_list|)
expr_stmt|;
return|return
name|desc
return|;
block|}
specifier|public
name|String
name|getTypeName
parameter_list|()
block|{
if|if
condition|(
name|typeName
operator|!=
literal|null
condition|)
block|{
return|return
name|typeName
return|;
block|}
else|else
block|{
return|return
name|type
operator|.
name|getName
argument_list|()
return|;
block|}
block|}
specifier|public
name|TypeQualifiers
name|getTypeQualifiers
parameter_list|()
block|{
return|return
name|typeQualifiers
return|;
block|}
specifier|public
name|void
name|setTypeQualifiers
parameter_list|(
name|TypeQualifiers
name|typeQualifiers
parameter_list|)
block|{
name|this
operator|.
name|typeQualifiers
operator|=
name|typeQualifiers
expr_stmt|;
block|}
comment|/**    * The column size for this type.    * For numeric data this is the maximum precision.    * For character data this is the length in characters.    * For datetime types this is the length in characters of the String representation    * (assuming the maximum allowed precision of the fractional seconds component).    * For binary data this is the length in bytes.    * Null is returned for for data types where the column size is not applicable.    */
specifier|public
name|Integer
name|getColumnSize
parameter_list|()
block|{
if|if
condition|(
name|type
operator|.
name|isNumericType
argument_list|()
condition|)
block|{
return|return
name|getPrecision
argument_list|()
return|;
block|}
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|STRING_TYPE
case|:
case|case
name|BINARY_TYPE
case|:
return|return
name|Integer
operator|.
name|MAX_VALUE
return|;
case|case
name|CHAR_TYPE
case|:
case|case
name|VARCHAR_TYPE
case|:
return|return
name|typeQualifiers
operator|.
name|getCharacterMaximumLength
argument_list|()
return|;
case|case
name|DATE_TYPE
case|:
return|return
literal|10
return|;
case|case
name|TIMESTAMP_TYPE
case|:
return|return
literal|29
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Maximum precision for numeric types.    * Returns null for non-numeric types.    * @return    */
specifier|public
name|Integer
name|getPrecision
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|type
operator|==
name|Type
operator|.
name|DECIMAL_TYPE
condition|)
block|{
return|return
name|typeQualifiers
operator|.
name|getPrecision
argument_list|()
return|;
block|}
return|return
name|this
operator|.
name|type
operator|.
name|getMaxPrecision
argument_list|()
return|;
block|}
comment|/**    * The number of fractional digits for this type.    * Null is returned for data types where this is not applicable.    */
specifier|public
name|Integer
name|getDecimalDigits
parameter_list|()
block|{
switch|switch
condition|(
name|this
operator|.
name|type
condition|)
block|{
case|case
name|BOOLEAN_TYPE
case|:
case|case
name|TINYINT_TYPE
case|:
case|case
name|SMALLINT_TYPE
case|:
case|case
name|INT_TYPE
case|:
case|case
name|BIGINT_TYPE
case|:
return|return
literal|0
return|;
case|case
name|FLOAT_TYPE
case|:
return|return
literal|7
return|;
case|case
name|DOUBLE_TYPE
case|:
return|return
literal|15
return|;
case|case
name|DECIMAL_TYPE
case|:
return|return
name|typeQualifiers
operator|.
name|getScale
argument_list|()
return|;
case|case
name|TIMESTAMP_TYPE
case|:
return|return
literal|9
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

