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
name|sql
operator|.
name|DatabaseMetaData
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
name|TTypeId
import|;
end_import

begin_comment
comment|/**  * Type.  *  */
end_comment

begin_enum
specifier|public
enum|enum
name|Type
block|{
name|BOOLEAN_TYPE
argument_list|(
literal|"BOOLEAN"
argument_list|,
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|BOOLEAN
argument_list|,
name|TTypeId
operator|.
name|BOOLEAN_TYPE
argument_list|)
block|,
name|TINYINT_TYPE
argument_list|(
literal|"TINYINT"
argument_list|,
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|TINYINT
argument_list|,
name|TTypeId
operator|.
name|TINYINT_TYPE
argument_list|)
block|,
name|SMALLINT_TYPE
argument_list|(
literal|"SMALLINT"
argument_list|,
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|SMALLINT
argument_list|,
name|TTypeId
operator|.
name|SMALLINT_TYPE
argument_list|)
block|,
name|INT_TYPE
argument_list|(
literal|"INT"
argument_list|,
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|INTEGER
argument_list|,
name|TTypeId
operator|.
name|INT_TYPE
argument_list|)
block|,
name|BIGINT_TYPE
argument_list|(
literal|"BIGINT"
argument_list|,
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|BIGINT
argument_list|,
name|TTypeId
operator|.
name|BIGINT_TYPE
argument_list|)
block|,
name|FLOAT_TYPE
argument_list|(
literal|"FLOAT"
argument_list|,
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|FLOAT
argument_list|,
name|TTypeId
operator|.
name|FLOAT_TYPE
argument_list|)
block|,
name|DOUBLE_TYPE
argument_list|(
literal|"DOUBLE"
argument_list|,
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|DOUBLE
argument_list|,
name|TTypeId
operator|.
name|DOUBLE_TYPE
argument_list|)
block|,
name|STRING_TYPE
argument_list|(
literal|"STRING"
argument_list|,
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|VARCHAR
argument_list|,
name|TTypeId
operator|.
name|STRING_TYPE
argument_list|)
block|,
name|TIMESTAMP_TYPE
argument_list|(
literal|"TIMESTAMP"
argument_list|,
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|TIMESTAMP
argument_list|,
name|TTypeId
operator|.
name|TIMESTAMP_TYPE
argument_list|)
block|,
name|BINARY_TYPE
argument_list|(
literal|"BINARY"
argument_list|,
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|BINARY
argument_list|,
name|TTypeId
operator|.
name|BINARY_TYPE
argument_list|)
block|,
name|DECIMAL_TYPE
argument_list|(
literal|"DECIMAL"
argument_list|,
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|DECIMAL
argument_list|,
name|TTypeId
operator|.
name|DECIMAL_TYPE
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
block|,
name|ARRAY_TYPE
argument_list|(
literal|"ARRAY"
argument_list|,
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|VARCHAR
argument_list|,
name|TTypeId
operator|.
name|STRING_TYPE
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
block|,
name|MAP_TYPE
argument_list|(
literal|"MAP"
argument_list|,
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|VARCHAR
argument_list|,
name|TTypeId
operator|.
name|STRING_TYPE
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
block|,
name|STRUCT_TYPE
argument_list|(
literal|"STRUCT"
argument_list|,
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|VARCHAR
argument_list|,
name|TTypeId
operator|.
name|STRING_TYPE
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
block|,
name|UNION_TYPE
argument_list|(
literal|"UNIONTYPE"
argument_list|,
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|VARCHAR
argument_list|,
name|TTypeId
operator|.
name|STRING_TYPE
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
block|,
name|USER_DEFINED_TYPE
argument_list|(
literal|null
argument_list|,
name|java
operator|.
name|sql
operator|.
name|Types
operator|.
name|VARCHAR
argument_list|,
name|TTypeId
operator|.
name|STRING_TYPE
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
block|;
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
specifier|private
specifier|final
name|TTypeId
name|tType
decl_stmt|;
specifier|private
specifier|final
name|int
name|javaSQLType
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isComplex
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isCollection
decl_stmt|;
name|Type
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|javaSQLType
parameter_list|,
name|TTypeId
name|tType
parameter_list|,
name|boolean
name|isComplex
parameter_list|,
name|boolean
name|isCollection
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|javaSQLType
operator|=
name|javaSQLType
expr_stmt|;
name|this
operator|.
name|tType
operator|=
name|tType
expr_stmt|;
name|this
operator|.
name|isComplex
operator|=
name|isComplex
expr_stmt|;
name|this
operator|.
name|isCollection
operator|=
name|isCollection
expr_stmt|;
block|}
name|Type
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|javaSqlType
parameter_list|,
name|TTypeId
name|tType
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|javaSqlType
argument_list|,
name|tType
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isPrimitiveType
parameter_list|()
block|{
return|return
operator|!
name|isComplex
return|;
block|}
specifier|public
name|boolean
name|isComplexType
parameter_list|()
block|{
return|return
name|isComplex
return|;
block|}
specifier|public
name|boolean
name|isCollectionType
parameter_list|()
block|{
return|return
name|isCollection
return|;
block|}
specifier|public
specifier|static
name|Type
name|getType
parameter_list|(
name|TTypeId
name|tType
parameter_list|)
block|{
for|for
control|(
name|Type
name|type
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|tType
operator|.
name|equals
argument_list|(
name|type
operator|.
name|tType
argument_list|)
condition|)
block|{
return|return
name|type
return|;
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unregonized Thrift TTypeId value: "
operator|+
name|tType
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|Type
name|getType
parameter_list|(
name|String
name|name
parameter_list|)
block|{
for|for
control|(
name|Type
name|type
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|name
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
operator|.
name|name
argument_list|)
condition|)
block|{
return|return
name|type
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|isComplexType
argument_list|()
condition|)
block|{
if|if
condition|(
name|name
operator|.
name|toUpperCase
argument_list|()
operator|.
name|startsWith
argument_list|(
name|type
operator|.
name|name
argument_list|)
condition|)
block|{
return|return
name|type
return|;
block|}
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unrecognized type name: "
operator|+
name|name
argument_list|)
throw|;
block|}
comment|/**    * Radix for this type (typically either 2 or 10)    * Null is returned for data types where this is not applicable.    */
specifier|public
name|Integer
name|getNumPrecRadix
parameter_list|()
block|{
switch|switch
condition|(
name|this
condition|)
block|{
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
literal|10
return|;
case|case
name|FLOAT_TYPE
case|:
case|case
name|DOUBLE_TYPE
case|:
return|return
literal|2
return|;
default|default:
comment|// everything else (including boolean and string) is null
return|return
literal|null
return|;
block|}
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
switch|switch
condition|(
name|this
condition|)
block|{
case|case
name|TINYINT_TYPE
case|:
return|return
literal|3
return|;
case|case
name|SMALLINT_TYPE
case|:
return|return
literal|5
return|;
case|case
name|INT_TYPE
case|:
return|return
literal|10
return|;
case|case
name|BIGINT_TYPE
case|:
return|return
literal|19
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
default|default:
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Scale for this type.    */
specifier|public
name|Integer
name|getScale
parameter_list|()
block|{
switch|switch
condition|(
name|this
condition|)
block|{
case|case
name|BOOLEAN_TYPE
case|:
case|case
name|STRING_TYPE
case|:
case|case
name|TIMESTAMP_TYPE
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
name|Integer
operator|.
name|MAX_VALUE
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
comment|/**    * The column size for this type.    * For numeric data this is the maximum precision.    * For character data this is the length in characters.    * For datetime types this is the length in characters of the String representation    * (assuming the maximum allowed precision of the fractional seconds component).    * For binary data this is the length in bytes.    * Null is returned for for data types where the column size is not applicable.    */
specifier|public
name|Integer
name|getColumnSize
parameter_list|()
block|{
if|if
condition|(
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
name|this
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
name|TIMESTAMP_TYPE
case|:
return|return
literal|30
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
specifier|public
name|boolean
name|isNumericType
parameter_list|()
block|{
switch|switch
condition|(
name|this
condition|)
block|{
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
case|case
name|FLOAT_TYPE
case|:
case|case
name|DOUBLE_TYPE
case|:
case|case
name|DECIMAL_TYPE
case|:
return|return
literal|true
return|;
default|default:
return|return
literal|false
return|;
block|}
block|}
comment|/**    * Prefix used to quote a literal of this type (may be null)    */
specifier|public
name|String
name|getLiteralPrefix
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
comment|/**    * Suffix used to quote a literal of this type (may be null)    * @return    */
specifier|public
name|String
name|getLiteralSuffix
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
comment|/**    * Can you use NULL for this type?    * @return    * DatabaseMetaData.typeNoNulls - does not allow NULL values    * DatabaseMetaData.typeNullable - allows NULL values    * DatabaseMetaData.typeNullableUnknown - nullability unknown    */
specifier|public
name|Short
name|getNullable
parameter_list|()
block|{
comment|// All Hive types are nullable
return|return
name|DatabaseMetaData
operator|.
name|typeNullable
return|;
block|}
comment|/**    * Is the type case sensitive?    * @return    */
specifier|public
name|Boolean
name|isCaseSensitive
parameter_list|()
block|{
switch|switch
condition|(
name|this
condition|)
block|{
case|case
name|STRING_TYPE
case|:
return|return
literal|true
return|;
default|default:
return|return
literal|false
return|;
block|}
block|}
comment|/**    * Parameters used in creating the type (may be null)    * @return    */
specifier|public
name|String
name|getCreateParams
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
comment|/**    * Can you use WHERE based on this type?    * @return    * DatabaseMetaData.typePredNone - No support    * DatabaseMetaData.typePredChar - Only support with WHERE .. LIKE    * DatabaseMetaData.typePredBasic - Supported except for WHERE .. LIKE    * DatabaseMetaData.typeSearchable - Supported for all WHERE ..    */
specifier|public
name|Short
name|getSearchable
parameter_list|()
block|{
if|if
condition|(
name|isPrimitiveType
argument_list|()
condition|)
block|{
return|return
name|DatabaseMetaData
operator|.
name|typeSearchable
return|;
block|}
return|return
name|DatabaseMetaData
operator|.
name|typePredNone
return|;
block|}
comment|/**    * Is this type unsigned?    * @return    */
specifier|public
name|Boolean
name|isUnsignedAttribute
parameter_list|()
block|{
if|if
condition|(
name|isNumericType
argument_list|()
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
comment|/**    * Can this type represent money?    * @return    */
specifier|public
name|Boolean
name|isFixedPrecScale
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    * Can this type be used for an auto-increment value?    * @return    */
specifier|public
name|Boolean
name|isAutoIncrement
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    * Localized version of type name (may be null).    * @return    */
specifier|public
name|String
name|getLocalizedName
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
comment|/**    * Minimum scale supported for this type    * @return    */
specifier|public
name|Short
name|getMinimumScale
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
comment|/**    * Maximum scale supported for this type    * @return    */
specifier|public
name|Short
name|getMaximumScale
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
specifier|public
name|TTypeId
name|toTType
parameter_list|()
block|{
return|return
name|tType
return|;
block|}
specifier|public
name|int
name|toJavaSQLType
parameter_list|()
block|{
return|return
name|javaSQLType
return|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
block|}
end_enum

end_unit

