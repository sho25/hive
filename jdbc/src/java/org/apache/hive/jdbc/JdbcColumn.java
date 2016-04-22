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
name|jdbc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Date
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Types
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
name|type
operator|.
name|HiveIntervalDayTime
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
name|type
operator|.
name|HiveIntervalYearMonth
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
name|serde
operator|.
name|serdeConstants
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
name|thrift
operator|.
name|Type
import|;
end_import

begin_comment
comment|/**  * Column metadata.  */
end_comment

begin_class
specifier|public
class|class
name|JdbcColumn
block|{
specifier|private
specifier|final
name|String
name|columnName
decl_stmt|;
specifier|private
specifier|final
name|String
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|String
name|tableCatalog
decl_stmt|;
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
specifier|private
specifier|final
name|String
name|comment
decl_stmt|;
specifier|private
specifier|final
name|int
name|ordinalPos
decl_stmt|;
name|JdbcColumn
parameter_list|(
name|String
name|columnName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|tableCatalog
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|comment
parameter_list|,
name|int
name|ordinalPos
parameter_list|)
block|{
name|this
operator|.
name|columnName
operator|=
name|columnName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|tableCatalog
operator|=
name|tableCatalog
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|comment
operator|=
name|comment
expr_stmt|;
name|this
operator|.
name|ordinalPos
operator|=
name|ordinalPos
expr_stmt|;
block|}
specifier|public
name|String
name|getColumnName
parameter_list|()
block|{
return|return
name|columnName
return|;
block|}
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
specifier|public
name|String
name|getTableCatalog
parameter_list|()
block|{
return|return
name|tableCatalog
return|;
block|}
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
specifier|static
name|String
name|columnClassName
parameter_list|(
name|Type
name|hiveType
parameter_list|,
name|JdbcColumnAttributes
name|columnAttributes
parameter_list|)
throws|throws
name|SQLException
block|{
name|int
name|columnType
init|=
name|hiveTypeToSqlType
argument_list|(
name|hiveType
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|columnType
condition|)
block|{
case|case
name|Types
operator|.
name|NULL
case|:
return|return
literal|"null"
return|;
case|case
name|Types
operator|.
name|BOOLEAN
case|:
return|return
name|Boolean
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
case|case
name|Types
operator|.
name|CHAR
case|:
case|case
name|Types
operator|.
name|VARCHAR
case|:
return|return
name|String
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
case|case
name|Types
operator|.
name|TINYINT
case|:
return|return
name|Byte
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
case|case
name|Types
operator|.
name|SMALLINT
case|:
return|return
name|Short
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
case|case
name|Types
operator|.
name|INTEGER
case|:
return|return
name|Integer
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
case|case
name|Types
operator|.
name|BIGINT
case|:
return|return
name|Long
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
case|case
name|Types
operator|.
name|DATE
case|:
return|return
name|Date
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
case|case
name|Types
operator|.
name|FLOAT
case|:
return|return
name|Float
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
case|case
name|Types
operator|.
name|DOUBLE
case|:
return|return
name|Double
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
case|case
name|Types
operator|.
name|TIMESTAMP
case|:
return|return
name|Timestamp
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
case|case
name|Types
operator|.
name|DECIMAL
case|:
return|return
name|BigInteger
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
case|case
name|Types
operator|.
name|BINARY
case|:
return|return
name|byte
index|[]
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
case|case
name|Types
operator|.
name|OTHER
case|:
case|case
name|Types
operator|.
name|JAVA_OBJECT
case|:
block|{
switch|switch
condition|(
name|hiveType
condition|)
block|{
case|case
name|INTERVAL_YEAR_MONTH_TYPE
case|:
return|return
name|HiveIntervalYearMonth
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
case|case
name|INTERVAL_DAY_TIME_TYPE
case|:
return|return
name|HiveIntervalDayTime
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
default|default:
return|return
name|String
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
block|}
block|}
case|case
name|Types
operator|.
name|ARRAY
case|:
case|case
name|Types
operator|.
name|STRUCT
case|:
return|return
name|String
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
default|default:
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Invalid column type: "
operator|+
name|columnType
argument_list|)
throw|;
block|}
block|}
specifier|static
name|Type
name|typeStringToHiveType
parameter_list|(
name|String
name|type
parameter_list|)
throws|throws
name|SQLException
block|{
if|if
condition|(
literal|"string"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|STRING_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"varchar"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|VARCHAR_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"char"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|CHAR_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"float"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|FLOAT_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"double"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|DOUBLE_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"boolean"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|BOOLEAN_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"tinyint"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|TINYINT_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"smallint"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|SMALLINT_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"int"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|INT_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"bigint"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|BIGINT_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"date"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|DATE_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"timestamp"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|TIMESTAMP_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"interval_year_month"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|INTERVAL_YEAR_MONTH_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"interval_day_time"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|INTERVAL_DAY_TIME_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"decimal"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|DECIMAL_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"binary"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|BINARY_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"map"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|MAP_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"array"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|ARRAY_TYPE
return|;
block|}
elseif|else
if|if
condition|(
literal|"struct"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Type
operator|.
name|STRUCT_TYPE
return|;
block|}
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Unrecognized column type: "
operator|+
name|type
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|int
name|hiveTypeToSqlType
parameter_list|(
name|Type
name|hiveType
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
name|hiveType
operator|.
name|toJavaSQLType
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|int
name|hiveTypeToSqlType
parameter_list|(
name|String
name|type
parameter_list|)
throws|throws
name|SQLException
block|{
if|if
condition|(
literal|"void"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
operator|||
literal|"null"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|Types
operator|.
name|NULL
return|;
block|}
else|else
block|{
return|return
name|hiveTypeToSqlType
argument_list|(
name|typeStringToHiveType
argument_list|(
name|type
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|static
name|String
name|getColumnTypeName
parameter_list|(
name|String
name|type
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// we need to convert the Hive type to the SQL type name
comment|// TODO: this would be better handled in an enum
if|if
condition|(
literal|"string"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
literal|"varchar"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|VARCHAR_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
literal|"char"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|CHAR_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
literal|"float"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|FLOAT_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
literal|"double"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|DOUBLE_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
literal|"boolean"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|BOOLEAN_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
literal|"tinyint"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|TINYINT_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
literal|"smallint"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|SMALLINT_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
literal|"int"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|INT_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
literal|"bigint"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|BIGINT_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
literal|"timestamp"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|TIMESTAMP_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
literal|"date"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|DATE_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
literal|"interval_year_month"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|INTERVAL_YEAR_MONTH_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
literal|"interval_day_time"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|INTERVAL_DAY_TIME_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
literal|"decimal"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|DECIMAL_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
literal|"binary"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|BINARY_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
literal|"void"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
operator|||
literal|"null"
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|VOID_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"map"
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|MAP_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"array"
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|LIST_TYPE_NAME
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"struct"
argument_list|)
condition|)
block|{
return|return
name|serdeConstants
operator|.
name|STRUCT_TYPE_NAME
return|;
block|}
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Unrecognized column type: "
operator|+
name|type
argument_list|)
throw|;
block|}
specifier|static
name|int
name|columnDisplaySize
parameter_list|(
name|Type
name|hiveType
parameter_list|,
name|JdbcColumnAttributes
name|columnAttributes
parameter_list|)
throws|throws
name|SQLException
block|{
comment|// according to hiveTypeToSqlType possible options are:
name|int
name|columnType
init|=
name|hiveTypeToSqlType
argument_list|(
name|hiveType
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|columnType
condition|)
block|{
case|case
name|Types
operator|.
name|BOOLEAN
case|:
return|return
name|columnPrecision
argument_list|(
name|hiveType
argument_list|,
name|columnAttributes
argument_list|)
return|;
case|case
name|Types
operator|.
name|CHAR
case|:
case|case
name|Types
operator|.
name|VARCHAR
case|:
return|return
name|columnPrecision
argument_list|(
name|hiveType
argument_list|,
name|columnAttributes
argument_list|)
return|;
case|case
name|Types
operator|.
name|BINARY
case|:
return|return
name|Integer
operator|.
name|MAX_VALUE
return|;
comment|// hive has no max limit for binary
case|case
name|Types
operator|.
name|TINYINT
case|:
case|case
name|Types
operator|.
name|SMALLINT
case|:
case|case
name|Types
operator|.
name|INTEGER
case|:
case|case
name|Types
operator|.
name|BIGINT
case|:
return|return
name|columnPrecision
argument_list|(
name|hiveType
argument_list|,
name|columnAttributes
argument_list|)
operator|+
literal|1
return|;
comment|// allow +/-
case|case
name|Types
operator|.
name|DATE
case|:
return|return
literal|10
return|;
case|case
name|Types
operator|.
name|TIMESTAMP
case|:
return|return
name|columnPrecision
argument_list|(
name|hiveType
argument_list|,
name|columnAttributes
argument_list|)
return|;
comment|// see http://download.oracle.com/javase/6/docs/api/constant-values.html#java.lang.Float.MAX_EXPONENT
case|case
name|Types
operator|.
name|FLOAT
case|:
return|return
literal|24
return|;
comment|// e.g. -(17#).e-###
comment|// see http://download.oracle.com/javase/6/docs/api/constant-values.html#java.lang.Double.MAX_EXPONENT
case|case
name|Types
operator|.
name|DOUBLE
case|:
return|return
literal|25
return|;
comment|// e.g. -(17#).e-####
case|case
name|Types
operator|.
name|DECIMAL
case|:
return|return
name|columnPrecision
argument_list|(
name|hiveType
argument_list|,
name|columnAttributes
argument_list|)
operator|+
literal|2
return|;
comment|// '-' sign and '.'
case|case
name|Types
operator|.
name|OTHER
case|:
case|case
name|Types
operator|.
name|JAVA_OBJECT
case|:
return|return
name|columnPrecision
argument_list|(
name|hiveType
argument_list|,
name|columnAttributes
argument_list|)
return|;
case|case
name|Types
operator|.
name|ARRAY
case|:
case|case
name|Types
operator|.
name|STRUCT
case|:
return|return
name|Integer
operator|.
name|MAX_VALUE
return|;
default|default:
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Invalid column type: "
operator|+
name|columnType
argument_list|)
throw|;
block|}
block|}
specifier|static
name|int
name|columnPrecision
parameter_list|(
name|Type
name|hiveType
parameter_list|,
name|JdbcColumnAttributes
name|columnAttributes
parameter_list|)
throws|throws
name|SQLException
block|{
name|int
name|columnType
init|=
name|hiveTypeToSqlType
argument_list|(
name|hiveType
argument_list|)
decl_stmt|;
comment|// according to hiveTypeToSqlType possible options are:
switch|switch
condition|(
name|columnType
condition|)
block|{
case|case
name|Types
operator|.
name|BOOLEAN
case|:
return|return
literal|1
return|;
case|case
name|Types
operator|.
name|CHAR
case|:
case|case
name|Types
operator|.
name|VARCHAR
case|:
if|if
condition|(
name|columnAttributes
operator|!=
literal|null
condition|)
block|{
return|return
name|columnAttributes
operator|.
name|precision
return|;
block|}
return|return
name|Integer
operator|.
name|MAX_VALUE
return|;
comment|// hive has no max limit for strings
case|case
name|Types
operator|.
name|BINARY
case|:
return|return
name|Integer
operator|.
name|MAX_VALUE
return|;
comment|// hive has no max limit for binary
case|case
name|Types
operator|.
name|TINYINT
case|:
return|return
literal|3
return|;
case|case
name|Types
operator|.
name|SMALLINT
case|:
return|return
literal|5
return|;
case|case
name|Types
operator|.
name|INTEGER
case|:
return|return
literal|10
return|;
case|case
name|Types
operator|.
name|BIGINT
case|:
return|return
literal|19
return|;
case|case
name|Types
operator|.
name|FLOAT
case|:
return|return
literal|7
return|;
case|case
name|Types
operator|.
name|DOUBLE
case|:
return|return
literal|15
return|;
case|case
name|Types
operator|.
name|DATE
case|:
return|return
literal|10
return|;
case|case
name|Types
operator|.
name|TIMESTAMP
case|:
return|return
literal|29
return|;
case|case
name|Types
operator|.
name|DECIMAL
case|:
return|return
name|columnAttributes
operator|.
name|precision
return|;
case|case
name|Types
operator|.
name|OTHER
case|:
case|case
name|Types
operator|.
name|JAVA_OBJECT
case|:
block|{
switch|switch
condition|(
name|hiveType
condition|)
block|{
case|case
name|INTERVAL_YEAR_MONTH_TYPE
case|:
comment|// -yyyyyyy-mm  : should be more than enough
return|return
literal|11
return|;
case|case
name|INTERVAL_DAY_TIME_TYPE
case|:
comment|// -ddddddddd hh:mm:ss.nnnnnnnnn
return|return
literal|29
return|;
default|default:
return|return
name|Integer
operator|.
name|MAX_VALUE
return|;
block|}
block|}
case|case
name|Types
operator|.
name|ARRAY
case|:
case|case
name|Types
operator|.
name|STRUCT
case|:
return|return
name|Integer
operator|.
name|MAX_VALUE
return|;
default|default:
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Invalid column type: "
operator|+
name|columnType
argument_list|)
throw|;
block|}
block|}
specifier|static
name|int
name|columnScale
parameter_list|(
name|Type
name|hiveType
parameter_list|,
name|JdbcColumnAttributes
name|columnAttributes
parameter_list|)
throws|throws
name|SQLException
block|{
name|int
name|columnType
init|=
name|hiveTypeToSqlType
argument_list|(
name|hiveType
argument_list|)
decl_stmt|;
comment|// according to hiveTypeToSqlType possible options are:
switch|switch
condition|(
name|columnType
condition|)
block|{
case|case
name|Types
operator|.
name|BOOLEAN
case|:
case|case
name|Types
operator|.
name|CHAR
case|:
case|case
name|Types
operator|.
name|VARCHAR
case|:
case|case
name|Types
operator|.
name|TINYINT
case|:
case|case
name|Types
operator|.
name|SMALLINT
case|:
case|case
name|Types
operator|.
name|INTEGER
case|:
case|case
name|Types
operator|.
name|BIGINT
case|:
case|case
name|Types
operator|.
name|DATE
case|:
case|case
name|Types
operator|.
name|BINARY
case|:
return|return
literal|0
return|;
case|case
name|Types
operator|.
name|FLOAT
case|:
return|return
literal|7
return|;
case|case
name|Types
operator|.
name|DOUBLE
case|:
return|return
literal|15
return|;
case|case
name|Types
operator|.
name|TIMESTAMP
case|:
return|return
literal|9
return|;
case|case
name|Types
operator|.
name|DECIMAL
case|:
return|return
name|columnAttributes
operator|.
name|scale
return|;
case|case
name|Types
operator|.
name|OTHER
case|:
case|case
name|Types
operator|.
name|JAVA_OBJECT
case|:
case|case
name|Types
operator|.
name|ARRAY
case|:
case|case
name|Types
operator|.
name|STRUCT
case|:
return|return
literal|0
return|;
default|default:
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Invalid column type: "
operator|+
name|columnType
argument_list|)
throw|;
block|}
block|}
specifier|public
name|Integer
name|getNumPrecRadix
parameter_list|()
block|{
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"tinyint"
argument_list|)
condition|)
block|{
return|return
literal|10
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"smallint"
argument_list|)
condition|)
block|{
return|return
literal|10
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"int"
argument_list|)
condition|)
block|{
return|return
literal|10
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"bigint"
argument_list|)
condition|)
block|{
return|return
literal|10
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"float"
argument_list|)
condition|)
block|{
return|return
literal|10
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"double"
argument_list|)
condition|)
block|{
return|return
literal|10
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"decimal"
argument_list|)
condition|)
block|{
return|return
literal|10
return|;
block|}
else|else
block|{
comment|// anything else including boolean and string is null
return|return
literal|null
return|;
block|}
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
specifier|public
name|int
name|getOrdinalPos
parameter_list|()
block|{
return|return
name|ordinalPos
return|;
block|}
block|}
end_class

end_unit

