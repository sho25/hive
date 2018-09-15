begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metastore
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|metastore
operator|.
name|utils
operator|.
name|StringUtils
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
name|HashSet
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

begin_comment
comment|/**  * Constants and utility functions for column types.  This is explicitly done as constants in the  * class rather than an enum in order to interoperate with Hive's old serdeConstants.  All type  * names in this class match the type names in Hive's serdeConstants class.  They must continue  * to do so.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|ColumnType
block|{
specifier|public
specifier|static
specifier|final
name|String
name|VOID_TYPE_NAME
init|=
literal|"void"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BOOLEAN_TYPE_NAME
init|=
literal|"boolean"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TINYINT_TYPE_NAME
init|=
literal|"tinyint"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SMALLINT_TYPE_NAME
init|=
literal|"smallint"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INT_TYPE_NAME
init|=
literal|"int"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BIGINT_TYPE_NAME
init|=
literal|"bigint"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FLOAT_TYPE_NAME
init|=
literal|"float"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DOUBLE_TYPE_NAME
init|=
literal|"double"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STRING_TYPE_NAME
init|=
literal|"string"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CHAR_TYPE_NAME
init|=
literal|"char"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|VARCHAR_TYPE_NAME
init|=
literal|"varchar"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DATE_TYPE_NAME
init|=
literal|"date"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DATETIME_TYPE_NAME
init|=
literal|"datetime"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TIMESTAMP_TYPE_NAME
init|=
literal|"timestamp"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DECIMAL_TYPE_NAME
init|=
literal|"decimal"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BINARY_TYPE_NAME
init|=
literal|"binary"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INTERVAL_YEAR_MONTH_TYPE_NAME
init|=
literal|"interval_year_month"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INTERVAL_DAY_TIME_TYPE_NAME
init|=
literal|"interval_day_time"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TIMESTAMPTZ_TYPE_NAME
init|=
literal|"timestamp with time zone"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LIST_TYPE_NAME
init|=
literal|"array"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MAP_TYPE_NAME
init|=
literal|"map"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|STRUCT_TYPE_NAME
init|=
literal|"struct"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|UNION_TYPE_NAME
init|=
literal|"uniontype"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LIST_COLUMNS
init|=
literal|"columns"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LIST_COLUMN_TYPES
init|=
literal|"columns.types"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|COLUMN_NAME_DELIMITER
init|=
literal|"column.name.delimiter"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|PrimitiveTypes
init|=
name|StringUtils
operator|.
name|asSet
argument_list|(
name|VOID_TYPE_NAME
argument_list|,
name|BOOLEAN_TYPE_NAME
argument_list|,
name|TINYINT_TYPE_NAME
argument_list|,
name|SMALLINT_TYPE_NAME
argument_list|,
name|INT_TYPE_NAME
argument_list|,
name|BIGINT_TYPE_NAME
argument_list|,
name|FLOAT_TYPE_NAME
argument_list|,
name|DOUBLE_TYPE_NAME
argument_list|,
name|STRING_TYPE_NAME
argument_list|,
name|VARCHAR_TYPE_NAME
argument_list|,
name|CHAR_TYPE_NAME
argument_list|,
name|DATE_TYPE_NAME
argument_list|,
name|DATETIME_TYPE_NAME
argument_list|,
name|TIMESTAMP_TYPE_NAME
argument_list|,
name|INTERVAL_YEAR_MONTH_TYPE_NAME
argument_list|,
name|INTERVAL_DAY_TIME_TYPE_NAME
argument_list|,
name|DECIMAL_TYPE_NAME
argument_list|,
name|BINARY_TYPE_NAME
argument_list|,
name|TIMESTAMPTZ_TYPE_NAME
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|StringTypes
init|=
name|StringUtils
operator|.
name|asSet
argument_list|(
name|STRING_TYPE_NAME
argument_list|,
name|VARCHAR_TYPE_NAME
argument_list|,
name|CHAR_TYPE_NAME
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|NumericTypes
init|=
name|StringUtils
operator|.
name|asSet
argument_list|(
name|TINYINT_TYPE_NAME
argument_list|,
name|SMALLINT_TYPE_NAME
argument_list|,
name|INT_TYPE_NAME
argument_list|,
name|BIGINT_TYPE_NAME
argument_list|,
name|FLOAT_TYPE_NAME
argument_list|,
name|DOUBLE_TYPE_NAME
argument_list|,
name|DECIMAL_TYPE_NAME
argument_list|)
decl_stmt|;
comment|// This intentionally does not include interval types.
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|DateTimeTypes
init|=
name|StringUtils
operator|.
name|asSet
argument_list|(
name|DATE_TYPE_NAME
argument_list|,
name|DATETIME_TYPE_NAME
argument_list|,
name|TIMESTAMP_TYPE_NAME
argument_list|,
name|TIMESTAMPTZ_TYPE_NAME
argument_list|)
decl_stmt|;
comment|// This map defines the progression of up casts in numeric types.
specifier|public
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|NumericCastOrder
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
static|static
block|{
name|NumericCastOrder
operator|.
name|put
argument_list|(
name|TINYINT_TYPE_NAME
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|NumericCastOrder
operator|.
name|put
argument_list|(
name|SMALLINT_TYPE_NAME
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|NumericCastOrder
operator|.
name|put
argument_list|(
name|INT_TYPE_NAME
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|NumericCastOrder
operator|.
name|put
argument_list|(
name|BIGINT_TYPE_NAME
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|NumericCastOrder
operator|.
name|put
argument_list|(
name|DECIMAL_TYPE_NAME
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|NumericCastOrder
operator|.
name|put
argument_list|(
name|FLOAT_TYPE_NAME
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|NumericCastOrder
operator|.
name|put
argument_list|(
name|DOUBLE_TYPE_NAME
argument_list|,
literal|7
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|decoratedTypeNames
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
static|static
block|{
name|decoratedTypeNames
operator|.
name|add
argument_list|(
literal|"char"
argument_list|)
expr_stmt|;
name|decoratedTypeNames
operator|.
name|add
argument_list|(
literal|"decimal"
argument_list|)
expr_stmt|;
name|decoratedTypeNames
operator|.
name|add
argument_list|(
literal|"varchar"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|alternateTypeNames
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
static|static
block|{
name|alternateTypeNames
operator|.
name|put
argument_list|(
literal|"integer"
argument_list|,
name|INT_TYPE_NAME
argument_list|)
expr_stmt|;
name|alternateTypeNames
operator|.
name|put
argument_list|(
literal|"numeric"
argument_list|,
name|DECIMAL_TYPE_NAME
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|CollectionTypes
init|=
name|StringUtils
operator|.
name|asSet
argument_list|(
name|LIST_TYPE_NAME
argument_list|,
name|MAP_TYPE_NAME
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|IntegralTypes
init|=
name|StringUtils
operator|.
name|asSet
argument_list|(
name|TINYINT_TYPE_NAME
argument_list|,
name|SMALLINT_TYPE_NAME
argument_list|,
name|INT_TYPE_NAME
argument_list|,
name|BIGINT_TYPE_NAME
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|AllTypes
init|=
name|StringUtils
operator|.
name|asSet
argument_list|(
name|VOID_TYPE_NAME
argument_list|,
name|BOOLEAN_TYPE_NAME
argument_list|,
name|TINYINT_TYPE_NAME
argument_list|,
name|SMALLINT_TYPE_NAME
argument_list|,
name|INT_TYPE_NAME
argument_list|,
name|BIGINT_TYPE_NAME
argument_list|,
name|FLOAT_TYPE_NAME
argument_list|,
name|DOUBLE_TYPE_NAME
argument_list|,
name|STRING_TYPE_NAME
argument_list|,
name|CHAR_TYPE_NAME
argument_list|,
name|VARCHAR_TYPE_NAME
argument_list|,
name|DATE_TYPE_NAME
argument_list|,
name|DATETIME_TYPE_NAME
argument_list|,
name|TIMESTAMP_TYPE_NAME
argument_list|,
name|DECIMAL_TYPE_NAME
argument_list|,
name|BINARY_TYPE_NAME
argument_list|,
name|INTERVAL_YEAR_MONTH_TYPE_NAME
argument_list|,
name|INTERVAL_DAY_TIME_TYPE_NAME
argument_list|,
name|TIMESTAMPTZ_TYPE_NAME
argument_list|,
name|LIST_TYPE_NAME
argument_list|,
name|MAP_TYPE_NAME
argument_list|,
name|STRUCT_TYPE_NAME
argument_list|,
name|UNION_TYPE_NAME
argument_list|,
name|LIST_COLUMNS
argument_list|,
name|LIST_COLUMN_TYPES
argument_list|,
name|COLUMN_NAME_DELIMITER
argument_list|)
decl_stmt|;
comment|/**    * Given a type string return the type name.  For example, passing in the type string    *<tt>varchar(256)</tt> will return<tt>varchar</tt>.    * @param typeString Type string    * @return type name, guaranteed to be in lower case    */
specifier|public
specifier|static
name|String
name|getTypeName
parameter_list|(
name|String
name|typeString
parameter_list|)
block|{
if|if
condition|(
name|typeString
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|String
name|protoType
init|=
name|typeString
operator|.
name|toLowerCase
argument_list|()
operator|.
name|split
argument_list|(
literal|"\\W"
argument_list|)
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|decoratedTypeNames
operator|.
name|contains
argument_list|(
name|protoType
argument_list|)
condition|)
block|{
return|return
name|protoType
return|;
block|}
name|String
name|realType
init|=
name|alternateTypeNames
operator|.
name|get
argument_list|(
name|protoType
argument_list|)
decl_stmt|;
return|return
name|realType
operator|==
literal|null
condition|?
name|protoType
else|:
name|realType
return|;
block|}
specifier|public
specifier|static
name|boolean
name|areColTypesCompatible
parameter_list|(
name|String
name|from
parameter_list|,
name|String
name|to
parameter_list|)
block|{
if|if
condition|(
name|from
operator|.
name|equals
argument_list|(
name|to
argument_list|)
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|PrimitiveTypes
operator|.
name|contains
argument_list|(
name|from
argument_list|)
operator|&&
name|PrimitiveTypes
operator|.
name|contains
argument_list|(
name|to
argument_list|)
condition|)
block|{
comment|// They aren't the same, but we may be able to do a cast
comment|// If they are both types of strings, that should be fine
if|if
condition|(
name|StringTypes
operator|.
name|contains
argument_list|(
name|from
argument_list|)
operator|&&
name|StringTypes
operator|.
name|contains
argument_list|(
name|to
argument_list|)
condition|)
return|return
literal|true
return|;
comment|// If both are numeric, make sure the new type is larger than the old.
if|if
condition|(
name|NumericTypes
operator|.
name|contains
argument_list|(
name|from
argument_list|)
operator|&&
name|NumericTypes
operator|.
name|contains
argument_list|(
name|to
argument_list|)
condition|)
block|{
return|return
name|NumericCastOrder
operator|.
name|get
argument_list|(
name|from
argument_list|)
operator|<
name|NumericCastOrder
operator|.
name|get
argument_list|(
name|to
argument_list|)
return|;
block|}
comment|// Allow string to double/decimal conversion
if|if
condition|(
name|StringTypes
operator|.
name|contains
argument_list|(
name|from
argument_list|)
operator|&&
operator|(
name|to
operator|.
name|equals
argument_list|(
name|DOUBLE_TYPE_NAME
argument_list|)
operator|||
name|to
operator|.
name|equals
argument_list|(
name|DECIMAL_TYPE_NAME
argument_list|)
operator|)
condition|)
return|return
literal|true
return|;
comment|// Void can go to anything
if|if
condition|(
name|from
operator|.
name|equals
argument_list|(
name|VOID_TYPE_NAME
argument_list|)
condition|)
return|return
literal|true
return|;
comment|// Allow date to string casts.  NOTE: I suspect this is the reverse of what we actually
comment|// want, but it matches the code in o.a.h.h.serde2.typeinfo.TypeInfoUtils.  I can't see how
comment|// users would be altering date columns into string columns.  The other I easily see since
comment|// Hive did not originally support datetime types.  Also, the comment in the Hive code
comment|// says string to date, even though the code does the opposite.  But for now I'm keeping
comment|// this as is so the functionality matches.
if|if
condition|(
name|DateTimeTypes
operator|.
name|contains
argument_list|(
name|from
argument_list|)
operator|&&
name|StringTypes
operator|.
name|contains
argument_list|(
name|to
argument_list|)
condition|)
return|return
literal|true
return|;
comment|// Allow numeric to string
if|if
condition|(
name|NumericTypes
operator|.
name|contains
argument_list|(
name|from
argument_list|)
operator|&&
name|StringTypes
operator|.
name|contains
argument_list|(
name|to
argument_list|)
condition|)
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|// These aren't column types, they are info for how things are stored in thrift.
comment|// It didn't seem useful to create another Constants class just for these though.
specifier|public
specifier|static
specifier|final
name|String
name|SERIALIZATION_FORMAT
init|=
literal|"serialization.format"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SERIALIZATION_LIB
init|=
literal|"serialization.lib"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SERIALIZATION_DDL
init|=
literal|"serialization.ddl"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|char
name|COLUMN_COMMENTS_DELIMITER
init|=
literal|'\0'
decl_stmt|;
specifier|private
specifier|static
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|typeToThriftTypeMap
decl_stmt|;
static|static
block|{
name|typeToThriftTypeMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|BOOLEAN_TYPE_NAME
argument_list|,
literal|"bool"
argument_list|)
expr_stmt|;
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|TINYINT_TYPE_NAME
argument_list|,
literal|"byte"
argument_list|)
expr_stmt|;
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|SMALLINT_TYPE_NAME
argument_list|,
literal|"i16"
argument_list|)
expr_stmt|;
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|INT_TYPE_NAME
argument_list|,
literal|"i32"
argument_list|)
expr_stmt|;
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|BIGINT_TYPE_NAME
argument_list|,
literal|"i64"
argument_list|)
expr_stmt|;
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|DOUBLE_TYPE_NAME
argument_list|,
literal|"double"
argument_list|)
expr_stmt|;
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|FLOAT_TYPE_NAME
argument_list|,
literal|"float"
argument_list|)
expr_stmt|;
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|LIST_TYPE_NAME
argument_list|,
literal|"list"
argument_list|)
expr_stmt|;
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|MAP_TYPE_NAME
argument_list|,
literal|"map"
argument_list|)
expr_stmt|;
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|STRING_TYPE_NAME
argument_list|,
literal|"string"
argument_list|)
expr_stmt|;
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|BINARY_TYPE_NAME
argument_list|,
literal|"binary"
argument_list|)
expr_stmt|;
comment|// These 4 types are not supported yet.
comment|// We should define a complex type date in thrift that contains a single int
comment|// member, and DynamicSerDe
comment|// should convert it to date type at runtime.
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|DATE_TYPE_NAME
argument_list|,
literal|"date"
argument_list|)
expr_stmt|;
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|DATETIME_TYPE_NAME
argument_list|,
literal|"datetime"
argument_list|)
expr_stmt|;
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|TIMESTAMP_TYPE_NAME
argument_list|,
literal|"timestamp"
argument_list|)
expr_stmt|;
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|DECIMAL_TYPE_NAME
argument_list|,
literal|"decimal"
argument_list|)
expr_stmt|;
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|INTERVAL_YEAR_MONTH_TYPE_NAME
argument_list|,
name|INTERVAL_YEAR_MONTH_TYPE_NAME
argument_list|)
expr_stmt|;
name|typeToThriftTypeMap
operator|.
name|put
argument_list|(
name|INTERVAL_DAY_TIME_TYPE_NAME
argument_list|,
name|INTERVAL_DAY_TIME_TYPE_NAME
argument_list|)
expr_stmt|;
block|}
comment|/**    * Convert type to ThriftType. We do that by tokenizing the type and convert    * each token.    */
specifier|public
specifier|static
name|String
name|typeToThriftType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|StringBuilder
name|thriftType
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|last
init|=
literal|0
decl_stmt|;
name|boolean
name|lastAlphaDigit
init|=
name|Character
operator|.
name|isLetterOrDigit
argument_list|(
name|type
operator|.
name|charAt
argument_list|(
name|last
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|type
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
name|type
operator|.
name|length
argument_list|()
operator|||
name|Character
operator|.
name|isLetterOrDigit
argument_list|(
name|type
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
argument_list|)
operator|!=
name|lastAlphaDigit
condition|)
block|{
name|String
name|token
init|=
name|type
operator|.
name|substring
argument_list|(
name|last
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|last
operator|=
name|i
expr_stmt|;
name|String
name|thriftToken
init|=
name|typeToThriftTypeMap
operator|.
name|get
argument_list|(
name|token
argument_list|)
decl_stmt|;
name|thriftType
operator|.
name|append
argument_list|(
name|thriftToken
operator|==
literal|null
condition|?
name|token
else|:
name|thriftToken
argument_list|)
expr_stmt|;
name|lastAlphaDigit
operator|=
operator|!
name|lastAlphaDigit
expr_stmt|;
block|}
block|}
return|return
name|thriftType
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|String
name|getListType
parameter_list|(
name|String
name|t
parameter_list|)
block|{
return|return
literal|"array<"
operator|+
name|t
operator|+
literal|">"
return|;
block|}
block|}
end_class

end_unit

