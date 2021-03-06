begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|accumulo
operator|.
name|columns
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|accumulo
operator|.
name|AccumuloHiveConstants
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
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
name|Maps
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
specifier|public
class|class
name|ColumnMappingFactory
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|log
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ColumnMappingFactory
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Generate the proper instance of a ColumnMapping    *    * @param columnSpec    *          Specification for mapping this column to Accumulo    * @param defaultEncoding    *          The default encoding in which values should be encoded to Accumulo    */
specifier|public
specifier|static
name|ColumnMapping
name|get
parameter_list|(
name|String
name|columnSpec
parameter_list|,
name|ColumnEncoding
name|defaultEncoding
parameter_list|,
name|String
name|columnName
parameter_list|,
name|TypeInfo
name|columnType
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|columnSpec
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|columnName
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|columnType
argument_list|)
expr_stmt|;
name|ColumnEncoding
name|encoding
init|=
name|defaultEncoding
decl_stmt|;
comment|// Check for column encoding specification
if|if
condition|(
name|ColumnEncoding
operator|.
name|hasColumnEncoding
argument_list|(
name|columnSpec
argument_list|)
condition|)
block|{
name|String
name|columnEncodingStr
init|=
name|ColumnEncoding
operator|.
name|getColumnEncoding
argument_list|(
name|columnSpec
argument_list|)
decl_stmt|;
name|columnSpec
operator|=
name|ColumnEncoding
operator|.
name|stripCode
argument_list|(
name|columnSpec
argument_list|)
expr_stmt|;
if|if
condition|(
name|AccumuloHiveConstants
operator|.
name|ROWID
operator|.
name|equalsIgnoreCase
argument_list|(
name|columnSpec
argument_list|)
condition|)
block|{
return|return
operator|new
name|HiveAccumuloRowIdColumnMapping
argument_list|(
name|columnSpec
argument_list|,
name|ColumnEncoding
operator|.
name|get
argument_list|(
name|columnEncodingStr
argument_list|)
argument_list|,
name|columnName
argument_list|,
name|columnType
operator|.
name|getTypeName
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|pair
init|=
name|parseMapping
argument_list|(
name|columnSpec
argument_list|)
decl_stmt|;
if|if
condition|(
name|isPrefix
argument_list|(
name|pair
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
comment|// Sanity check that, for a map, we got 2 encodings
if|if
condition|(
operator|!
name|ColumnEncoding
operator|.
name|isMapEncoding
argument_list|(
name|columnEncodingStr
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected map encoding for a map specification, "
operator|+
name|columnSpec
operator|+
literal|" with encoding "
operator|+
name|columnEncodingStr
argument_list|)
throw|;
block|}
name|Entry
argument_list|<
name|ColumnEncoding
argument_list|,
name|ColumnEncoding
argument_list|>
name|encodings
init|=
name|ColumnEncoding
operator|.
name|getMapEncoding
argument_list|(
name|columnEncodingStr
argument_list|)
decl_stmt|;
return|return
operator|new
name|HiveAccumuloMapColumnMapping
argument_list|(
name|pair
operator|.
name|getKey
argument_list|()
argument_list|,
name|pair
operator|.
name|getValue
argument_list|()
argument_list|,
name|encodings
operator|.
name|getKey
argument_list|()
argument_list|,
name|encodings
operator|.
name|getValue
argument_list|()
argument_list|,
name|columnName
argument_list|,
name|columnType
operator|.
name|getTypeName
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|HiveAccumuloColumnMapping
argument_list|(
name|pair
operator|.
name|getKey
argument_list|()
argument_list|,
name|pair
operator|.
name|getValue
argument_list|()
argument_list|,
name|ColumnEncoding
operator|.
name|getFromMapping
argument_list|(
name|columnEncodingStr
argument_list|)
argument_list|,
name|columnName
argument_list|,
name|columnType
operator|.
name|getTypeName
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|AccumuloHiveConstants
operator|.
name|ROWID
operator|.
name|equalsIgnoreCase
argument_list|(
name|columnSpec
argument_list|)
condition|)
block|{
return|return
operator|new
name|HiveAccumuloRowIdColumnMapping
argument_list|(
name|columnSpec
argument_list|,
name|defaultEncoding
argument_list|,
name|columnName
argument_list|,
name|columnType
operator|.
name|getTypeName
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|pair
init|=
name|parseMapping
argument_list|(
name|columnSpec
argument_list|)
decl_stmt|;
name|boolean
name|isPrefix
init|=
name|isPrefix
argument_list|(
name|pair
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|cq
init|=
name|pair
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|// Replace any \* that appear in the prefix with a regular *
if|if
condition|(
operator|-
literal|1
operator|!=
name|cq
operator|.
name|indexOf
argument_list|(
name|AccumuloHiveConstants
operator|.
name|ESCAPED_ASTERISK
argument_list|)
condition|)
block|{
name|cq
operator|=
name|cq
operator|.
name|replaceAll
argument_list|(
name|AccumuloHiveConstants
operator|.
name|ESCAPED_ASERTISK_REGEX
argument_list|,
name|Character
operator|.
name|toString
argument_list|(
name|AccumuloHiveConstants
operator|.
name|ASTERISK
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|isPrefix
condition|)
block|{
return|return
operator|new
name|HiveAccumuloMapColumnMapping
argument_list|(
name|pair
operator|.
name|getKey
argument_list|()
argument_list|,
name|cq
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|cq
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|,
name|defaultEncoding
argument_list|,
name|defaultEncoding
argument_list|,
name|columnName
argument_list|,
name|columnType
operator|.
name|getTypeName
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|HiveAccumuloColumnMapping
argument_list|(
name|pair
operator|.
name|getKey
argument_list|()
argument_list|,
name|cq
argument_list|,
name|encoding
argument_list|,
name|columnName
argument_list|,
name|columnType
operator|.
name|getTypeName
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
block|}
specifier|public
specifier|static
name|ColumnMapping
name|getMap
parameter_list|(
name|String
name|columnSpec
parameter_list|,
name|ColumnEncoding
name|keyEncoding
parameter_list|,
name|ColumnEncoding
name|valueEncoding
parameter_list|,
name|String
name|columnName
parameter_list|,
name|TypeInfo
name|columnType
parameter_list|)
block|{
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|pair
init|=
name|parseMapping
argument_list|(
name|columnSpec
argument_list|)
decl_stmt|;
return|return
operator|new
name|HiveAccumuloMapColumnMapping
argument_list|(
name|pair
operator|.
name|getKey
argument_list|()
argument_list|,
name|pair
operator|.
name|getValue
argument_list|()
argument_list|,
name|keyEncoding
argument_list|,
name|valueEncoding
argument_list|,
name|columnName
argument_list|,
name|columnType
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isPrefix
parameter_list|(
name|String
name|maybePrefix
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|maybePrefix
argument_list|)
expr_stmt|;
if|if
condition|(
name|AccumuloHiveConstants
operator|.
name|ASTERISK
operator|==
name|maybePrefix
operator|.
name|charAt
argument_list|(
name|maybePrefix
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
condition|)
block|{
if|if
condition|(
name|maybePrefix
operator|.
name|length
argument_list|()
operator|>
literal|1
condition|)
block|{
return|return
name|AccumuloHiveConstants
operator|.
name|ESCAPE
operator|!=
name|maybePrefix
operator|.
name|charAt
argument_list|(
name|maybePrefix
operator|.
name|length
argument_list|()
operator|-
literal|2
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|true
return|;
block|}
block|}
comment|// If we couldn't find an asterisk, it's not a prefix
return|return
literal|false
return|;
block|}
comment|/**    * Consumes the column mapping specification and breaks it into column family and column    * qualifier.    */
specifier|public
specifier|static
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parseMapping
parameter_list|(
name|String
name|columnSpec
parameter_list|)
throws|throws
name|InvalidColumnMappingException
block|{
name|int
name|index
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|index
operator|>=
name|columnSpec
operator|.
name|length
argument_list|()
condition|)
block|{
name|log
operator|.
name|error
argument_list|(
literal|"Cannot parse '"
operator|+
name|columnSpec
operator|+
literal|"' as colon-separated column configuration"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|InvalidColumnMappingException
argument_list|(
literal|"Columns must be provided as colon-separated family and qualifier pairs"
argument_list|)
throw|;
block|}
name|index
operator|=
name|columnSpec
operator|.
name|indexOf
argument_list|(
name|AccumuloHiveConstants
operator|.
name|COLON
argument_list|,
name|index
argument_list|)
expr_stmt|;
if|if
condition|(
operator|-
literal|1
operator|==
name|index
condition|)
block|{
name|log
operator|.
name|error
argument_list|(
literal|"Cannot parse '"
operator|+
name|columnSpec
operator|+
literal|"' as colon-separated column configuration"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|InvalidColumnMappingException
argument_list|(
literal|"Columns must be provided as colon-separated family and qualifier pairs"
argument_list|)
throw|;
block|}
comment|// Check for an escape character before the colon
if|if
condition|(
name|index
operator|-
literal|1
operator|>
literal|0
condition|)
block|{
name|char
name|testChar
init|=
name|columnSpec
operator|.
name|charAt
argument_list|(
name|index
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|AccumuloHiveConstants
operator|.
name|ESCAPE
operator|==
name|testChar
condition|)
block|{
comment|// this colon is escaped, search again after it
name|index
operator|++
expr_stmt|;
continue|continue;
block|}
comment|// If the previous character isn't an escape characters, it's the separator
block|}
comment|// Can't be escaped, it is the separator
break|break;
block|}
name|String
name|cf
init|=
name|columnSpec
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
decl_stmt|,
name|cq
init|=
name|columnSpec
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|1
argument_list|)
decl_stmt|;
comment|// Check for the escaped colon to remove before doing the expensive regex replace
if|if
condition|(
operator|-
literal|1
operator|!=
name|cf
operator|.
name|indexOf
argument_list|(
name|AccumuloHiveConstants
operator|.
name|ESCAPED_COLON
argument_list|)
condition|)
block|{
name|cf
operator|=
name|cf
operator|.
name|replaceAll
argument_list|(
name|AccumuloHiveConstants
operator|.
name|ESCAPED_COLON_REGEX
argument_list|,
name|Character
operator|.
name|toString
argument_list|(
name|AccumuloHiveConstants
operator|.
name|COLON
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Check for the escaped colon to remove before doing the expensive regex replace
if|if
condition|(
operator|-
literal|1
operator|!=
name|cq
operator|.
name|indexOf
argument_list|(
name|AccumuloHiveConstants
operator|.
name|ESCAPED_COLON
argument_list|)
condition|)
block|{
name|cq
operator|=
name|cq
operator|.
name|replaceAll
argument_list|(
name|AccumuloHiveConstants
operator|.
name|ESCAPED_COLON_REGEX
argument_list|,
name|Character
operator|.
name|toString
argument_list|(
name|AccumuloHiveConstants
operator|.
name|COLON
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|Maps
operator|.
name|immutableEntry
argument_list|(
name|cf
argument_list|,
name|cq
argument_list|)
return|;
block|}
block|}
end_class

end_unit

