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
name|lazy
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
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|Properties
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|SerDeException
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
name|lazy
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|LazyObjectInspectorParameters
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
name|TypeInfoFactory
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
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|HiveStringUtils
import|;
end_import

begin_comment
comment|/**  * SerDeParameters.  *  */
end_comment

begin_class
specifier|public
class|class
name|LazySerDeParameters
implements|implements
name|LazyObjectInspectorParameters
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
name|LazySerDeParameters
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
name|byte
index|[]
name|DefaultSeparators
init|=
block|{
operator|(
name|byte
operator|)
literal|1
block|,
operator|(
name|byte
operator|)
literal|2
block|,
operator|(
name|byte
operator|)
literal|3
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SERIALIZATION_EXTEND_NESTING_LEVELS
init|=
literal|"hive.serialization.extend.nesting.levels"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SERIALIZATION_EXTEND_ADDITIONAL_NESTING_LEVELS
init|=
literal|"hive.serialization.extend.additional.nesting.levels"
decl_stmt|;
specifier|private
name|Properties
name|tableProperties
decl_stmt|;
specifier|private
name|String
name|serdeName
decl_stmt|;
comment|// The list of bytes used for the separators in the column (a nested struct
comment|// such as Array<Array<int>> will use multiple separators).
comment|// The list of separators + escapeChar are the bytes required to be escaped.
specifier|private
name|byte
index|[]
name|separators
decl_stmt|;
specifier|private
name|Text
name|nullSequence
decl_stmt|;
specifier|private
name|TypeInfo
name|rowTypeInfo
decl_stmt|;
specifier|private
name|boolean
name|lastColumnTakesRest
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
decl_stmt|;
specifier|private
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|columnTypes
decl_stmt|;
specifier|private
name|boolean
name|escaped
decl_stmt|;
specifier|private
name|byte
name|escapeChar
decl_stmt|;
specifier|private
name|boolean
index|[]
name|needsEscape
init|=
operator|new
name|boolean
index|[
literal|256
index|]
decl_stmt|;
comment|// A flag for each byte to indicate if escape is needed.
specifier|private
name|boolean
name|extendedBooleanLiteral
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|timestampFormats
decl_stmt|;
specifier|public
name|LazySerDeParameters
parameter_list|(
name|Configuration
name|job
parameter_list|,
name|Properties
name|tbl
parameter_list|,
name|String
name|serdeName
parameter_list|)
throws|throws
name|SerDeException
block|{
name|this
operator|.
name|tableProperties
operator|=
name|tbl
expr_stmt|;
name|this
operator|.
name|serdeName
operator|=
name|serdeName
expr_stmt|;
name|String
name|nullString
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_NULL_FORMAT
argument_list|,
literal|"\\N"
argument_list|)
decl_stmt|;
name|nullSequence
operator|=
operator|new
name|Text
argument_list|(
name|nullString
argument_list|)
expr_stmt|;
name|String
name|lastColumnTakesRestString
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_LAST_COLUMN_TAKES_REST
argument_list|)
decl_stmt|;
name|lastColumnTakesRest
operator|=
operator|(
name|lastColumnTakesRestString
operator|!=
literal|null
operator|&&
name|lastColumnTakesRestString
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"true"
argument_list|)
operator|)
expr_stmt|;
name|extractColumnInfo
argument_list|()
expr_stmt|;
comment|// Create the LazyObject for storing the rows
name|rowTypeInfo
operator|=
name|TypeInfoFactory
operator|.
name|getStructTypeInfo
argument_list|(
name|columnNames
argument_list|,
name|columnTypes
argument_list|)
expr_stmt|;
name|collectSeparators
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
comment|// Get the escape information
name|String
name|escapeProperty
init|=
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|ESCAPE_CHAR
argument_list|)
decl_stmt|;
name|escaped
operator|=
operator|(
name|escapeProperty
operator|!=
literal|null
operator|)
expr_stmt|;
if|if
condition|(
name|escaped
condition|)
block|{
name|escapeChar
operator|=
name|LazyUtils
operator|.
name|getByte
argument_list|(
name|escapeProperty
argument_list|,
operator|(
name|byte
operator|)
literal|'\\'
argument_list|)
expr_stmt|;
name|needsEscape
index|[
name|escapeChar
operator|&
literal|0xFF
index|]
operator|=
literal|true
expr_stmt|;
comment|// Converts the negative byte into positive index
for|for
control|(
name|byte
name|b
range|:
name|separators
control|)
block|{
name|needsEscape
index|[
name|b
operator|&
literal|0xFF
index|]
operator|=
literal|true
expr_stmt|;
comment|// Converts the negative byte into positive index
block|}
comment|// '\r' and '\n' are reserved and can't be used for escape chars and separators
if|if
condition|(
name|needsEscape
index|[
literal|'\r'
index|]
operator|||
name|needsEscape
index|[
literal|'\n'
index|]
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"\\r and \\n cannot be used as escaping characters or separators"
argument_list|)
throw|;
block|}
name|boolean
name|isEscapeCRLF
init|=
name|Boolean
operator|.
name|valueOf
argument_list|(
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_ESCAPE_CRLF
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|isEscapeCRLF
condition|)
block|{
name|needsEscape
index|[
literal|'\r'
index|]
operator|=
literal|true
expr_stmt|;
name|needsEscape
index|[
literal|'\n'
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|extendedBooleanLiteral
operator|=
operator|(
name|job
operator|==
literal|null
condition|?
literal|false
else|:
name|job
operator|.
name|getBoolean
argument_list|(
name|ConfVars
operator|.
name|HIVE_LAZYSIMPLE_EXTENDED_BOOLEAN_LITERAL
operator|.
name|varname
argument_list|,
literal|false
argument_list|)
operator|)
expr_stmt|;
name|String
index|[]
name|timestampFormatsArray
init|=
name|HiveStringUtils
operator|.
name|splitAndUnEscape
argument_list|(
name|tbl
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|TIMESTAMP_FORMATS
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|timestampFormatsArray
operator|!=
literal|null
condition|)
block|{
name|timestampFormats
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|timestampFormatsArray
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
name|serdeName
operator|+
literal|" initialized with: columnNames="
operator|+
name|columnNames
operator|+
literal|" columnTypes="
operator|+
name|columnTypes
operator|+
literal|" separator="
operator|+
name|Arrays
operator|.
name|asList
argument_list|(
name|separators
argument_list|)
operator|+
literal|" nullstring="
operator|+
name|nullString
operator|+
literal|" lastColumnTakesRest="
operator|+
name|lastColumnTakesRest
operator|+
literal|" timestampFormats="
operator|+
name|timestampFormats
argument_list|)
expr_stmt|;
block|}
comment|/**    * Extracts and set column names and column types from the table properties    * @throws SerDeException    */
specifier|public
name|void
name|extractColumnInfo
parameter_list|()
throws|throws
name|SerDeException
block|{
comment|// Read the configuration parameters
name|String
name|columnNameProperty
init|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMNS
argument_list|)
decl_stmt|;
comment|// NOTE: if "columns.types" is missing, all columns will be of String type
name|String
name|columnTypeProperty
init|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|LIST_COLUMN_TYPES
argument_list|)
decl_stmt|;
comment|// Parse the configuration parameters
if|if
condition|(
name|columnNameProperty
operator|!=
literal|null
operator|&&
name|columnNameProperty
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|columnNames
operator|=
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
expr_stmt|;
block|}
else|else
block|{
name|columnNames
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|columnTypeProperty
operator|==
literal|null
condition|)
block|{
comment|// Default type: all string
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
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
name|columnNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|)
expr_stmt|;
block|}
name|columnTypeProperty
operator|=
name|sb
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|columnTypes
operator|=
name|TypeInfoUtils
operator|.
name|getTypeInfosFromTypeString
argument_list|(
name|columnTypeProperty
argument_list|)
expr_stmt|;
if|if
condition|(
name|columnNames
operator|.
name|size
argument_list|()
operator|!=
name|columnTypes
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|serdeName
operator|+
literal|": columns has "
operator|+
name|columnNames
operator|.
name|size
argument_list|()
operator|+
literal|" elements while columns.types has "
operator|+
name|columnTypes
operator|.
name|size
argument_list|()
operator|+
literal|" elements!"
argument_list|)
throw|;
block|}
block|}
specifier|public
name|List
argument_list|<
name|TypeInfo
argument_list|>
name|getColumnTypes
parameter_list|()
block|{
return|return
name|columnTypes
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getColumnNames
parameter_list|()
block|{
return|return
name|columnNames
return|;
block|}
specifier|public
name|byte
index|[]
name|getSeparators
parameter_list|()
block|{
return|return
name|separators
return|;
block|}
specifier|public
name|Text
name|getNullSequence
parameter_list|()
block|{
return|return
name|nullSequence
return|;
block|}
specifier|public
name|TypeInfo
name|getRowTypeInfo
parameter_list|()
block|{
return|return
name|rowTypeInfo
return|;
block|}
specifier|public
name|boolean
name|isLastColumnTakesRest
parameter_list|()
block|{
return|return
name|lastColumnTakesRest
return|;
block|}
specifier|public
name|boolean
name|isEscaped
parameter_list|()
block|{
return|return
name|escaped
return|;
block|}
specifier|public
name|byte
name|getEscapeChar
parameter_list|()
block|{
return|return
name|escapeChar
return|;
block|}
specifier|public
name|boolean
index|[]
name|getNeedsEscape
parameter_list|()
block|{
return|return
name|needsEscape
return|;
block|}
specifier|public
name|boolean
name|isExtendedBooleanLiteral
parameter_list|()
block|{
return|return
name|extendedBooleanLiteral
return|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getTimestampFormats
parameter_list|()
block|{
return|return
name|timestampFormats
return|;
block|}
specifier|public
name|void
name|setSeparator
parameter_list|(
name|int
name|index
parameter_list|,
name|byte
name|separator
parameter_list|)
throws|throws
name|SerDeException
block|{
if|if
condition|(
name|index
operator|<
literal|0
operator|||
name|index
operator|>=
name|separators
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Invalid separator array index value: "
operator|+
name|index
argument_list|)
throw|;
block|}
name|separators
index|[
name|index
index|]
operator|=
name|separator
expr_stmt|;
block|}
comment|/**    * To be backward-compatible, initialize the first 3 separators to     * be the given values from the table properties. The default number of separators is 8; if only    * hive.serialization.extend.nesting.levels is set, the number of    * separators is extended to 24; if hive.serialization.extend.additional.nesting.levels    * is set, the number of separators is extended to 154.    * @param tableProperties table properties to extract the user provided separators    */
specifier|private
name|void
name|collectSeparators
parameter_list|(
name|Properties
name|tableProperties
parameter_list|)
block|{
name|List
argument_list|<
name|Byte
argument_list|>
name|separatorCandidates
init|=
operator|new
name|ArrayList
argument_list|<
name|Byte
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|extendNestingValue
init|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|SERIALIZATION_EXTEND_NESTING_LEVELS
argument_list|)
decl_stmt|;
name|String
name|extendAdditionalNestingValue
init|=
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|SERIALIZATION_EXTEND_ADDITIONAL_NESTING_LEVELS
argument_list|)
decl_stmt|;
name|boolean
name|extendedNesting
init|=
name|extendNestingValue
operator|!=
literal|null
operator|&&
name|extendNestingValue
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"true"
argument_list|)
decl_stmt|;
name|boolean
name|extendedAdditionalNesting
init|=
name|extendAdditionalNestingValue
operator|!=
literal|null
operator|&&
name|extendAdditionalNestingValue
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"true"
argument_list|)
decl_stmt|;
name|separatorCandidates
operator|.
name|add
argument_list|(
name|LazyUtils
operator|.
name|getByte
argument_list|(
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|FIELD_DELIM
argument_list|,
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|SERIALIZATION_FORMAT
argument_list|)
argument_list|)
argument_list|,
name|DefaultSeparators
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|separatorCandidates
operator|.
name|add
argument_list|(
name|LazyUtils
operator|.
name|getByte
argument_list|(
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|COLLECTION_DELIM
argument_list|)
argument_list|,
name|DefaultSeparators
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|separatorCandidates
operator|.
name|add
argument_list|(
name|LazyUtils
operator|.
name|getByte
argument_list|(
name|tableProperties
operator|.
name|getProperty
argument_list|(
name|serdeConstants
operator|.
name|MAPKEY_DELIM
argument_list|)
argument_list|,
name|DefaultSeparators
index|[
literal|2
index|]
argument_list|)
argument_list|)
expr_stmt|;
comment|//use only control chars that are very unlikely to be part of the string
comment|// the following might/likely to be used in text files for strings
comment|// 9 (horizontal tab, HT, \t, ^I)
comment|// 10 (line feed, LF, \n, ^J),
comment|// 12 (form feed, FF, \f, ^L),
comment|// 13 (carriage return, CR, \r, ^M),
comment|// 27 (escape, ESC, \e [GCC only], ^[).
for|for
control|(
name|byte
name|b
init|=
literal|4
init|;
name|b
operator|<=
literal|8
condition|;
name|b
operator|++
control|)
block|{
name|separatorCandidates
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
name|separatorCandidates
operator|.
name|add
argument_list|(
operator|(
name|byte
operator|)
literal|11
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
name|b
init|=
literal|14
init|;
name|b
operator|<=
literal|26
condition|;
name|b
operator|++
control|)
block|{
name|separatorCandidates
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|byte
name|b
init|=
literal|28
init|;
name|b
operator|<=
literal|31
condition|;
name|b
operator|++
control|)
block|{
name|separatorCandidates
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|byte
name|b
init|=
operator|-
literal|128
init|;
name|b
operator|<=
operator|-
literal|1
condition|;
name|b
operator|++
control|)
block|{
name|separatorCandidates
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
name|int
name|numSeparators
init|=
literal|8
decl_stmt|;
if|if
condition|(
name|extendedAdditionalNesting
condition|)
block|{
name|numSeparators
operator|=
name|separatorCandidates
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|extendedNesting
condition|)
block|{
name|numSeparators
operator|=
literal|24
expr_stmt|;
block|}
name|separators
operator|=
operator|new
name|byte
index|[
name|numSeparators
index|]
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
name|numSeparators
condition|;
name|i
operator|++
control|)
block|{
name|separators
index|[
name|i
index|]
operator|=
name|separatorCandidates
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

