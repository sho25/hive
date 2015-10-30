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
package|;
end_package

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
name|Timestamp
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
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|HiveChar
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
name|HiveDecimal
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
name|HiveVarchar
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
name|StructObjectInspector
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
name|primitive
operator|.
name|AbstractPrimitiveJavaObjectInspector
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
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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
name|CharTypeInfo
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
name|TypeInfo
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
name|TypeInfoUtils
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
name|VarcharTypeInfo
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
name|Writable
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Splitter
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * RegexSerDe uses regular expression (regex) to deserialize data. It doesn't  * support data serialization.  *  * It can deserialize the data using regex and extracts groups as columns.  *  * In deserialization stage, if a row does not match the regex, then all columns  * in the row will be NULL. If a row matches the regex but has less than  * expected groups, the missing groups will be NULL. If a row matches the regex  * but has more than expected groups, the additional groups are just ignored.  *  * NOTE: Regex SerDe supports primitive column types such as TINYINT, SMALLINT,  * INT, BIGINT, FLOAT, DOUBLE, STRING, BOOLEAN and DECIMAL  *  *  * NOTE: This implementation uses javaStringObjectInspector for STRING. A  * more efficient implementation should use UTF-8 encoded Text and  * writableStringObjectInspector. We should switch to that when we have a UTF-8  * based Regex library.  */
end_comment

begin_class
annotation|@
name|SerDeSpec
argument_list|(
name|schemaProps
operator|=
block|{
name|serdeConstants
operator|.
name|LIST_COLUMNS
block|,
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
block|,
name|RegexSerDe
operator|.
name|INPUT_REGEX
block|,
name|RegexSerDe
operator|.
name|INPUT_REGEX_CASE_SENSITIVE
block|}
argument_list|)
specifier|public
class|class
name|RegexSerDe
extends|extends
name|AbstractSerDe
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
name|RegexSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INPUT_REGEX
init|=
literal|"input.regex"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|INPUT_REGEX_CASE_SENSITIVE
init|=
literal|"input.regex.case.insensitive"
decl_stmt|;
name|int
name|numColumns
decl_stmt|;
name|String
name|inputRegex
decl_stmt|;
name|Pattern
name|inputPattern
decl_stmt|;
name|StructObjectInspector
name|rowOI
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|row
decl_stmt|;
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
decl_stmt|;
name|Object
index|[]
name|outputFields
decl_stmt|;
name|Text
name|outputRowText
decl_stmt|;
name|boolean
name|alreadyLoggedNoMatch
init|=
literal|false
decl_stmt|;
name|boolean
name|alreadyLoggedPartialMatch
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Properties
name|tbl
parameter_list|)
throws|throws
name|SerDeException
block|{
comment|// We can get the table definition from tbl.
comment|// Read the configuration parameters
name|inputRegex
operator|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|INPUT_REGEX
argument_list|)
expr_stmt|;
name|String
name|columnNameProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|)
decl_stmt|;
name|String
name|columnTypeProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|)
decl_stmt|;
name|boolean
name|inputRegexIgnoreCase
init|=
literal|"true"
operator|.
name|equalsIgnoreCase
argument_list|(
name|tbl
operator|.
name|getProperty
argument_list|(
name|INPUT_REGEX_CASE_SENSITIVE
argument_list|)
argument_list|)
decl_stmt|;
comment|// output format string is not supported anymore, warn user of deprecation
if|if
condition|(
literal|null
operator|!=
name|tbl
operator|.
name|getProperty
argument_list|(
literal|"output.format.string"
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"output.format.string has been deprecated"
argument_list|)
expr_stmt|;
block|}
comment|// Parse the configuration parameters
if|if
condition|(
name|inputRegex
operator|!=
literal|null
condition|)
block|{
name|inputPattern
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|inputRegex
argument_list|,
name|Pattern
operator|.
name|DOTALL
operator|+
operator|(
name|inputRegexIgnoreCase
condition|?
name|Pattern
operator|.
name|CASE_INSENSITIVE
else|:
literal|0
operator|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|inputPattern
operator|=
literal|null
expr_stmt|;
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"This table does not have serde property \"input.regex\"!"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|columnNameProperty
operator|.
name|split
argument_list|(
literal|","
argument_list|)
argument_list|)
decl_stmt|;
name|columnTypes
operator|=
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
name|columnTypeProperty
argument_list|)
expr_stmt|;
assert|assert
name|columnNames
operator|.
name|size
argument_list|()
operator|==
name|columnTypes
operator|.
name|size
argument_list|()
assert|;
name|numColumns
operator|=
name|columnNames
operator|.
name|size
argument_list|()
expr_stmt|;
comment|/* Constructing the row ObjectInspector:      * The row consists of some set of primitive columns, each column will      * be a java object of primitive type.      */
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|columnOIs
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|columnNames
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|numColumns
condition|;
name|c
operator|++
control|)
block|{
name|TypeInfo
name|typeInfo
init|=
name|columnTypes
operator|.
name|get
argument_list|(
name|c
argument_list|)
decl_stmt|;
if|if
condition|(
name|typeInfo
operator|instanceof
name|PrimitiveTypeInfo
condition|)
block|{
name|PrimitiveTypeInfo
name|pti
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|columnTypes
operator|.
name|get
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|AbstractPrimitiveJavaObjectInspector
name|oi
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveJavaObjectInspector
argument_list|(
name|pti
argument_list|)
decl_stmt|;
name|columnOIs
operator|.
name|add
argument_list|(
name|oi
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" doesn't allow column ["
operator|+
name|c
operator|+
literal|"] named "
operator|+
name|columnNames
operator|.
name|get
argument_list|(
name|c
argument_list|)
operator|+
literal|" with type "
operator|+
name|columnTypes
operator|.
name|get
argument_list|(
name|c
argument_list|)
argument_list|)
throw|;
block|}
block|}
comment|// StandardStruct uses ArrayList to store the row.
name|rowOI
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|columnNames
argument_list|,
name|columnOIs
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|Splitter
operator|.
name|on
argument_list|(
literal|'\0'
argument_list|)
operator|.
name|split
argument_list|(
name|tbl
operator|.
name|getProperty
argument_list|(
literal|"columns.comments"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|row
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|numColumns
argument_list|)
expr_stmt|;
comment|// Constructing the row object, etc, which will be reused for all rows.
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|numColumns
condition|;
name|c
operator|++
control|)
block|{
name|row
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
name|outputFields
operator|=
operator|new
name|Object
index|[
name|numColumns
index|]
expr_stmt|;
name|outputRowText
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|getObjectInspector
parameter_list|()
throws|throws
name|SerDeException
block|{
return|return
name|rowOI
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Writable
argument_list|>
name|getSerializedClass
parameter_list|()
block|{
return|return
name|Text
operator|.
name|class
return|;
block|}
comment|// Number of rows not matching the regex
name|long
name|unmatchedRowsCount
init|=
literal|0
decl_stmt|;
comment|// Number of rows that match the regex but have missing groups.
name|long
name|partialMatchedRowsCount
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Object
name|deserialize
parameter_list|(
name|Writable
name|blob
parameter_list|)
throws|throws
name|SerDeException
block|{
name|Text
name|rowText
init|=
operator|(
name|Text
operator|)
name|blob
decl_stmt|;
name|Matcher
name|m
init|=
name|inputPattern
operator|.
name|matcher
argument_list|(
name|rowText
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|.
name|groupCount
argument_list|()
operator|!=
name|numColumns
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Number of matching groups doesn't match the number of columns"
argument_list|)
throw|;
block|}
comment|// If do not match, ignore the line, return a row with all nulls.
if|if
condition|(
operator|!
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
name|unmatchedRowsCount
operator|++
expr_stmt|;
if|if
condition|(
operator|!
name|alreadyLoggedNoMatch
condition|)
block|{
comment|// Report the row if its the first time
name|LOG
operator|.
name|warn
argument_list|(
literal|""
operator|+
name|unmatchedRowsCount
operator|+
literal|" unmatched rows are found: "
operator|+
name|rowText
argument_list|)
expr_stmt|;
name|alreadyLoggedNoMatch
operator|=
literal|true
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
comment|// Otherwise, return the row.
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|numColumns
condition|;
name|c
operator|++
control|)
block|{
try|try
block|{
name|String
name|t
init|=
name|m
operator|.
name|group
argument_list|(
name|c
operator|+
literal|1
argument_list|)
decl_stmt|;
name|TypeInfo
name|typeInfo
init|=
name|columnTypes
operator|.
name|get
argument_list|(
name|c
argument_list|)
decl_stmt|;
comment|// Convert the column to the correct type when needed and set in row obj
name|PrimitiveTypeInfo
name|pti
init|=
operator|(
name|PrimitiveTypeInfo
operator|)
name|typeInfo
decl_stmt|;
switch|switch
condition|(
name|pti
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|STRING
case|:
name|row
operator|.
name|set
argument_list|(
name|c
argument_list|,
name|t
argument_list|)
expr_stmt|;
break|break;
case|case
name|BYTE
case|:
name|Byte
name|b
decl_stmt|;
name|b
operator|=
name|Byte
operator|.
name|valueOf
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|row
operator|.
name|set
argument_list|(
name|c
argument_list|,
name|b
argument_list|)
expr_stmt|;
break|break;
case|case
name|SHORT
case|:
name|Short
name|s
decl_stmt|;
name|s
operator|=
name|Short
operator|.
name|valueOf
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|row
operator|.
name|set
argument_list|(
name|c
argument_list|,
name|s
argument_list|)
expr_stmt|;
break|break;
case|case
name|INT
case|:
name|Integer
name|i
decl_stmt|;
name|i
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|row
operator|.
name|set
argument_list|(
name|c
argument_list|,
name|i
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|Long
name|l
decl_stmt|;
name|l
operator|=
name|Long
operator|.
name|valueOf
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|row
operator|.
name|set
argument_list|(
name|c
argument_list|,
name|l
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|Float
name|f
decl_stmt|;
name|f
operator|=
name|Float
operator|.
name|valueOf
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|row
operator|.
name|set
argument_list|(
name|c
argument_list|,
name|f
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|Double
name|d
decl_stmt|;
name|d
operator|=
name|Double
operator|.
name|valueOf
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|row
operator|.
name|set
argument_list|(
name|c
argument_list|,
name|d
argument_list|)
expr_stmt|;
break|break;
case|case
name|BOOLEAN
case|:
name|Boolean
name|bool
decl_stmt|;
name|bool
operator|=
name|Boolean
operator|.
name|valueOf
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|row
operator|.
name|set
argument_list|(
name|c
argument_list|,
name|bool
argument_list|)
expr_stmt|;
break|break;
case|case
name|TIMESTAMP
case|:
name|Timestamp
name|ts
decl_stmt|;
name|ts
operator|=
name|Timestamp
operator|.
name|valueOf
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|row
operator|.
name|set
argument_list|(
name|c
argument_list|,
name|ts
argument_list|)
expr_stmt|;
break|break;
case|case
name|DATE
case|:
name|Date
name|date
decl_stmt|;
name|date
operator|=
name|Date
operator|.
name|valueOf
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|row
operator|.
name|set
argument_list|(
name|c
argument_list|,
name|date
argument_list|)
expr_stmt|;
break|break;
case|case
name|DECIMAL
case|:
name|HiveDecimal
name|bd
init|=
name|HiveDecimal
operator|.
name|create
argument_list|(
name|t
argument_list|)
decl_stmt|;
name|row
operator|.
name|set
argument_list|(
name|c
argument_list|,
name|bd
argument_list|)
expr_stmt|;
break|break;
case|case
name|CHAR
case|:
name|HiveChar
name|hc
init|=
operator|new
name|HiveChar
argument_list|(
name|t
argument_list|,
operator|(
operator|(
name|CharTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getLength
argument_list|()
argument_list|)
decl_stmt|;
name|row
operator|.
name|set
argument_list|(
name|c
argument_list|,
name|hc
argument_list|)
expr_stmt|;
break|break;
case|case
name|VARCHAR
case|:
name|HiveVarchar
name|hv
init|=
operator|new
name|HiveVarchar
argument_list|(
name|t
argument_list|,
operator|(
operator|(
name|VarcharTypeInfo
operator|)
name|typeInfo
operator|)
operator|.
name|getLength
argument_list|()
argument_list|)
decl_stmt|;
name|row
operator|.
name|set
argument_list|(
name|c
argument_list|,
name|hv
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Unsupported type "
operator|+
name|typeInfo
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|partialMatchedRowsCount
operator|++
expr_stmt|;
if|if
condition|(
operator|!
name|alreadyLoggedPartialMatch
condition|)
block|{
comment|// Report the row if its the first row
name|LOG
operator|.
name|warn
argument_list|(
literal|""
operator|+
name|partialMatchedRowsCount
operator|+
literal|" partially unmatched rows are found, "
operator|+
literal|" cannot find group "
operator|+
name|c
operator|+
literal|": "
operator|+
name|rowText
argument_list|)
expr_stmt|;
name|alreadyLoggedPartialMatch
operator|=
literal|true
expr_stmt|;
block|}
name|row
operator|.
name|set
argument_list|(
name|c
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|row
return|;
block|}
annotation|@
name|Override
specifier|public
name|Writable
name|serialize
parameter_list|(
name|Object
name|obj
parameter_list|,
name|ObjectInspector
name|objInspector
parameter_list|)
throws|throws
name|SerDeException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Regex SerDe doesn't support the serialize() method"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|SerDeStats
name|getSerDeStats
parameter_list|()
block|{
comment|// no support for statistics
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

