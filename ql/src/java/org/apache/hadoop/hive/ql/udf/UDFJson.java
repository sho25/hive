begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ql
operator|.
name|udf
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
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterators
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
name|ql
operator|.
name|exec
operator|.
name|Description
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
name|ql
operator|.
name|exec
operator|.
name|UDF
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
name|codehaus
operator|.
name|jackson
operator|.
name|JsonFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|JsonParser
operator|.
name|Feature
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|ObjectMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|type
operator|.
name|TypeFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|type
operator|.
name|JavaType
import|;
end_import

begin_comment
comment|/**  * UDFJson.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"get_json_object"
argument_list|,
name|value
operator|=
literal|"_FUNC_(json_txt, path) - Extract a json object from path "
argument_list|,
name|extended
operator|=
literal|"Extract json object from a json string based on json path "
operator|+
literal|"specified, and return json string of the extracted json object. It "
operator|+
literal|"will return null if the input json string is invalid.\n"
operator|+
literal|"A limited version of JSONPath supported:\n"
operator|+
literal|"  $   : Root object\n"
operator|+
literal|"  .   : Child operator\n"
operator|+
literal|"  []  : Subscript operator for array\n"
operator|+
literal|"  *   : Wildcard for []\n"
operator|+
literal|"Syntax not supported that's worth noticing:\n"
operator|+
literal|"  ''  : Zero length string as key\n"
operator|+
literal|"  ..  : Recursive descent\n"
operator|+
literal|"&amp;#064;   : Current object/element\n"
operator|+
literal|"  ()  : Script expression\n"
operator|+
literal|"  ?() : Filter (script) expression.\n"
operator|+
literal|"  [,] : Union operator\n"
operator|+
literal|"  [start:end:step] : array slice operator\n"
argument_list|)
specifier|public
class|class
name|UDFJson
extends|extends
name|UDF
block|{
specifier|private
specifier|static
specifier|final
name|Pattern
name|patternKey
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"^([a-zA-Z0-9_\\-\\:\\s]+).*"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Pattern
name|patternIndex
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"\\[([0-9]+|\\*)\\]"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|JavaType
name|MAP_TYPE
init|=
name|TypeFactory
operator|.
name|fromClass
argument_list|(
name|Map
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|JavaType
name|LIST_TYPE
init|=
name|TypeFactory
operator|.
name|fromClass
argument_list|(
name|List
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|JsonFactory
name|jsonFactory
init|=
operator|new
name|JsonFactory
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ObjectMapper
name|objectMapper
init|=
operator|new
name|ObjectMapper
argument_list|(
name|jsonFactory
argument_list|)
decl_stmt|;
comment|// An LRU cache using a linked hash map
specifier|static
class|class
name|HashCache
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
extends|extends
name|LinkedHashMap
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|int
name|CACHE_SIZE
init|=
literal|16
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|INIT_SIZE
init|=
literal|32
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|float
name|LOAD_FACTOR
init|=
literal|0.6f
decl_stmt|;
name|HashCache
parameter_list|()
block|{
name|super
argument_list|(
name|INIT_SIZE
argument_list|,
name|LOAD_FACTOR
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|boolean
name|removeEldestEntry
parameter_list|(
name|Map
operator|.
name|Entry
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|eldest
parameter_list|)
block|{
return|return
name|size
argument_list|()
operator|>
name|CACHE_SIZE
return|;
block|}
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|extractObjectCache
init|=
operator|new
name|HashCache
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
name|pathExprCache
init|=
operator|new
name|HashCache
argument_list|<
name|String
argument_list|,
name|String
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
name|indexListCache
init|=
operator|new
name|HashCache
argument_list|<
name|String
argument_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mKeyGroup1Cache
init|=
operator|new
name|HashCache
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
name|mKeyMatchesCache
init|=
operator|new
name|HashCache
argument_list|<
name|String
argument_list|,
name|Boolean
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|UDFJson
parameter_list|()
block|{
comment|// Allows for unescaped ASCII control characters in JSON values
name|jsonFactory
operator|.
name|enable
argument_list|(
name|Feature
operator|.
name|ALLOW_UNQUOTED_CONTROL_CHARS
argument_list|)
expr_stmt|;
comment|// Enabled to accept quoting of all character backslash qooting mechanism
name|jsonFactory
operator|.
name|enable
argument_list|(
name|Feature
operator|.
name|ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER
argument_list|)
expr_stmt|;
block|}
comment|/**    * Extract json object from a json string based on json path specified, and    * return json string of the extracted json object. It will return null if the    * input json string is invalid.    *    * A limited version of JSONPath supported: $ : Root object . : Child operator    * [] : Subscript operator for array * : Wildcard for []    *    * Syntax not supported that's worth noticing: '' : Zero length string as key    * .. : Recursive descent&amp;#064; : Current object/element () : Script    * expression ?() : Filter (script) expression. [,] : Union operator    * [start:end:step] : array slice operator    *    * @param jsonString    *          the json string.    * @param pathString    *          the json path expression.    * @return json string or null when an error happens.    */
specifier|public
name|Text
name|evaluate
parameter_list|(
name|String
name|jsonString
parameter_list|,
name|String
name|pathString
parameter_list|)
block|{
if|if
condition|(
name|jsonString
operator|==
literal|null
operator|||
name|jsonString
operator|.
name|isEmpty
argument_list|()
operator|||
name|pathString
operator|==
literal|null
operator|||
name|pathString
operator|.
name|isEmpty
argument_list|()
operator|||
name|pathString
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|!=
literal|'$'
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|pathExprStart
init|=
literal|1
decl_stmt|;
name|boolean
name|unknownType
init|=
name|pathString
operator|.
name|equals
argument_list|(
literal|"$"
argument_list|)
decl_stmt|;
name|boolean
name|isRootArray
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|pathString
operator|.
name|length
argument_list|()
operator|>
literal|1
condition|)
block|{
if|if
condition|(
name|pathString
operator|.
name|charAt
argument_list|(
literal|1
argument_list|)
operator|==
literal|'['
condition|)
block|{
name|pathExprStart
operator|=
literal|0
expr_stmt|;
name|isRootArray
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|pathString
operator|.
name|charAt
argument_list|(
literal|1
argument_list|)
operator|==
literal|'.'
condition|)
block|{
name|isRootArray
operator|=
name|pathString
operator|.
name|length
argument_list|()
operator|>
literal|2
operator|&&
name|pathString
operator|.
name|charAt
argument_list|(
literal|2
argument_list|)
operator|==
literal|'['
expr_stmt|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
comment|// Cache pathExpr
name|String
index|[]
name|pathExpr
init|=
name|pathExprCache
operator|.
name|get
argument_list|(
name|pathString
argument_list|)
decl_stmt|;
if|if
condition|(
name|pathExpr
operator|==
literal|null
condition|)
block|{
name|pathExpr
operator|=
name|pathString
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|pathExprCache
operator|.
name|put
argument_list|(
name|pathString
argument_list|,
name|pathExpr
argument_list|)
expr_stmt|;
block|}
comment|// Cache extractObject
name|Object
name|extractObject
init|=
name|extractObjectCache
operator|.
name|get
argument_list|(
name|jsonString
argument_list|)
decl_stmt|;
if|if
condition|(
name|extractObject
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|unknownType
condition|)
block|{
try|try
block|{
name|extractObject
operator|=
name|objectMapper
operator|.
name|readValue
argument_list|(
name|jsonString
argument_list|,
name|LIST_TYPE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// Ignore exception
block|}
if|if
condition|(
name|extractObject
operator|==
literal|null
condition|)
block|{
try|try
block|{
name|extractObject
operator|=
name|objectMapper
operator|.
name|readValue
argument_list|(
name|jsonString
argument_list|,
name|MAP_TYPE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
else|else
block|{
name|JavaType
name|javaType
init|=
name|isRootArray
condition|?
name|LIST_TYPE
else|:
name|MAP_TYPE
decl_stmt|;
try|try
block|{
name|extractObject
operator|=
name|objectMapper
operator|.
name|readValue
argument_list|(
name|jsonString
argument_list|,
name|javaType
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
name|extractObjectCache
operator|.
name|put
argument_list|(
name|jsonString
argument_list|,
name|extractObject
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
name|pathExprStart
init|;
name|i
operator|<
name|pathExpr
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|extractObject
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|extractObject
operator|=
name|extract
argument_list|(
name|extractObject
argument_list|,
name|pathExpr
index|[
name|i
index|]
argument_list|,
name|i
operator|==
name|pathExprStart
operator|&&
name|isRootArray
argument_list|)
expr_stmt|;
block|}
name|Text
name|result
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
if|if
condition|(
name|extractObject
operator|instanceof
name|Map
operator|||
name|extractObject
operator|instanceof
name|List
condition|)
block|{
try|try
block|{
name|result
operator|.
name|set
argument_list|(
name|objectMapper
operator|.
name|writeValueAsString
argument_list|(
name|extractObject
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|extractObject
operator|!=
literal|null
condition|)
block|{
name|result
operator|.
name|set
argument_list|(
name|extractObject
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|Object
name|extract
parameter_list|(
name|Object
name|json
parameter_list|,
name|String
name|path
parameter_list|,
name|boolean
name|skipMapProc
parameter_list|)
block|{
comment|// skip MAP processing for the first path element if root is array
if|if
condition|(
operator|!
name|skipMapProc
condition|)
block|{
comment|// Cache patternkey.matcher(path).matches()
name|Matcher
name|mKey
init|=
literal|null
decl_stmt|;
name|Boolean
name|mKeyMatches
init|=
name|mKeyMatchesCache
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|mKeyMatches
operator|==
literal|null
condition|)
block|{
name|mKey
operator|=
name|patternKey
operator|.
name|matcher
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|mKeyMatches
operator|=
name|mKey
operator|.
name|matches
argument_list|()
condition|?
name|Boolean
operator|.
name|TRUE
else|:
name|Boolean
operator|.
name|FALSE
expr_stmt|;
name|mKeyMatchesCache
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|mKeyMatches
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|mKeyMatches
operator|.
name|booleanValue
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// Cache mkey.group(1)
name|String
name|mKeyGroup1
init|=
name|mKeyGroup1Cache
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|mKeyGroup1
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|mKey
operator|==
literal|null
condition|)
block|{
name|mKey
operator|=
name|patternKey
operator|.
name|matcher
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|mKeyMatches
operator|=
name|mKey
operator|.
name|matches
argument_list|()
condition|?
name|Boolean
operator|.
name|TRUE
else|:
name|Boolean
operator|.
name|FALSE
expr_stmt|;
name|mKeyMatchesCache
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|mKeyMatches
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|mKeyMatches
operator|.
name|booleanValue
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
name|mKeyGroup1
operator|=
name|mKey
operator|.
name|group
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|mKeyGroup1Cache
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|mKeyGroup1
argument_list|)
expr_stmt|;
block|}
name|json
operator|=
name|extract_json_withkey
argument_list|(
name|json
argument_list|,
name|mKeyGroup1
argument_list|)
expr_stmt|;
block|}
comment|// Cache indexList
name|ArrayList
argument_list|<
name|String
argument_list|>
name|indexList
init|=
name|indexListCache
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexList
operator|==
literal|null
condition|)
block|{
name|Matcher
name|mIndex
init|=
name|patternIndex
operator|.
name|matcher
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|indexList
operator|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
while|while
condition|(
name|mIndex
operator|.
name|find
argument_list|()
condition|)
block|{
name|indexList
operator|.
name|add
argument_list|(
name|mIndex
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexListCache
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|indexList
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indexList
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|json
operator|=
name|extract_json_withindex
argument_list|(
name|json
argument_list|,
name|indexList
argument_list|)
expr_stmt|;
block|}
return|return
name|json
return|;
block|}
specifier|private
specifier|transient
name|AddingList
name|jsonList
init|=
operator|new
name|AddingList
argument_list|()
decl_stmt|;
specifier|private
specifier|static
class|class
name|AddingList
extends|extends
name|ArrayList
argument_list|<
name|Object
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Object
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|Iterators
operator|.
name|forArray
argument_list|(
name|toArray
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|removeRange
parameter_list|(
name|int
name|fromIndex
parameter_list|,
name|int
name|toIndex
parameter_list|)
block|{
name|super
operator|.
name|removeRange
argument_list|(
name|fromIndex
argument_list|,
name|toIndex
argument_list|)
expr_stmt|;
block|}
block|}
empty_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|Object
name|extract_json_withindex
parameter_list|(
name|Object
name|json
parameter_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
name|indexList
parameter_list|)
block|{
name|jsonList
operator|.
name|clear
argument_list|()
expr_stmt|;
name|jsonList
operator|.
name|add
argument_list|(
name|json
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|index
range|:
name|indexList
control|)
block|{
name|int
name|targets
init|=
name|jsonList
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|index
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"*"
argument_list|)
condition|)
block|{
for|for
control|(
name|Object
name|array
range|:
name|jsonList
control|)
block|{
if|if
condition|(
name|array
operator|instanceof
name|List
condition|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
operator|(
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|array
operator|)
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|jsonList
operator|.
name|add
argument_list|(
operator|(
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|array
operator|)
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
else|else
block|{
for|for
control|(
name|Object
name|array
range|:
name|jsonList
control|)
block|{
name|int
name|indexValue
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|array
operator|instanceof
name|List
operator|)
condition|)
block|{
continue|continue;
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
name|array
decl_stmt|;
if|if
condition|(
name|indexValue
operator|>=
name|list
operator|.
name|size
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|jsonList
operator|.
name|add
argument_list|(
name|list
operator|.
name|get
argument_list|(
name|indexValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|jsonList
operator|.
name|size
argument_list|()
operator|==
name|targets
condition|)
block|{
return|return
literal|null
return|;
block|}
name|jsonList
operator|.
name|removeRange
argument_list|(
literal|0
argument_list|,
name|targets
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|jsonList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|(
name|jsonList
operator|.
name|size
argument_list|()
operator|>
literal|1
operator|)
condition|?
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|jsonList
argument_list|)
else|:
name|jsonList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
name|Object
name|extract_json_withkey
parameter_list|(
name|Object
name|json
parameter_list|,
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|json
operator|instanceof
name|List
condition|)
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|jsonArray
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
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
operator|(
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|json
operator|)
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|json_elem
init|=
operator|(
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|json
operator|)
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Object
name|json_obj
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|json_elem
operator|instanceof
name|Map
condition|)
block|{
name|json_obj
operator|=
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|json_elem
operator|)
operator|.
name|get
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
else|else
block|{
continue|continue;
block|}
if|if
condition|(
name|json_obj
operator|instanceof
name|List
condition|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
operator|(
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|json_obj
operator|)
operator|.
name|size
argument_list|()
condition|;
name|j
operator|++
control|)
block|{
name|jsonArray
operator|.
name|add
argument_list|(
operator|(
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|json_obj
operator|)
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|json_obj
operator|!=
literal|null
condition|)
block|{
name|jsonArray
operator|.
name|add
argument_list|(
name|json_obj
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|(
name|jsonArray
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|)
condition|?
literal|null
else|:
name|jsonArray
return|;
block|}
elseif|else
if|if
condition|(
name|json
operator|instanceof
name|Map
condition|)
block|{
return|return
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|json
operator|)
operator|.
name|get
argument_list|(
name|path
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

