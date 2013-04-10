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
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|JavaUtils
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
name|ListObjectInspector
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
name|MapObjectInspector
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
name|PrimitiveObjectInspector
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
name|StandardStructObjectInspector
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
name|StructField
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
name|UnionObjectInspector
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
name|HiveDecimalObjectInspector
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
name|BinaryObjectInspector
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
name|BooleanObjectInspector
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
name|ByteObjectInspector
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
name|DoubleObjectInspector
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
name|FloatObjectInspector
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
name|IntObjectInspector
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
name|LongObjectInspector
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
name|ShortObjectInspector
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
name|StringObjectInspector
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
name|TimestampObjectInspector
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
name|BytesWritable
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

begin_comment
comment|/**  * SerDeUtils.  *  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|SerDeUtils
block|{
specifier|public
specifier|static
specifier|final
name|char
name|QUOTE
init|=
literal|'"'
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|char
name|COLON
init|=
literal|':'
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|char
name|COMMA
init|=
literal|','
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LBRACKET
init|=
literal|"["
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RBRACKET
init|=
literal|"]"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|LBRACE
init|=
literal|"{"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RBRACE
init|=
literal|"}"
decl_stmt|;
comment|// lower case null is used within json objects
specifier|private
specifier|static
specifier|final
name|String
name|JSON_NULL
init|=
literal|"null"
decl_stmt|;
specifier|private
specifier|static
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|serdes
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SerDeUtils
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|void
name|registerSerDe
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|serde
parameter_list|)
block|{
if|if
condition|(
name|serdes
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"double registering serde "
operator|+
name|name
argument_list|)
expr_stmt|;
return|return;
block|}
name|serdes
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|serde
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|Deserializer
name|lookupDeserializer
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|SerDeException
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|c
decl_stmt|;
if|if
condition|(
name|serdes
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|c
operator|=
name|serdes
operator|.
name|get
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|c
operator|=
name|Class
operator|.
name|forName
argument_list|(
name|name
argument_list|,
literal|true
argument_list|,
name|JavaUtils
operator|.
name|getClassLoader
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"SerDe "
operator|+
name|name
operator|+
literal|" does not exist"
argument_list|)
throw|;
block|}
block|}
try|try
block|{
return|return
operator|(
name|Deserializer
operator|)
name|c
operator|.
name|newInstance
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|nativeSerDeNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
static|static
block|{
name|nativeSerDeNames
operator|.
name|add
argument_list|(
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
name|dynamic_type
operator|.
name|DynamicSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|nativeSerDeNames
operator|.
name|add
argument_list|(
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
name|MetadataTypedColumnsetSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// For backward compatibility
name|nativeSerDeNames
operator|.
name|add
argument_list|(
literal|"org.apache.hadoop.hive.serde.thrift.columnsetSerDe"
argument_list|)
expr_stmt|;
name|nativeSerDeNames
operator|.
name|add
argument_list|(
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
name|LazySimpleSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|nativeSerDeNames
operator|.
name|add
argument_list|(
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
name|columnar
operator|.
name|ColumnarSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|boolean
name|shouldGetColsFromSerDe
parameter_list|(
name|String
name|serde
parameter_list|)
block|{
return|return
operator|(
name|serde
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|nativeSerDeNames
operator|.
name|contains
argument_list|(
name|serde
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|boolean
name|initCoreSerDes
init|=
name|registerCoreSerDes
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
name|boolean
name|registerCoreSerDes
parameter_list|()
block|{
comment|// Eagerly load SerDes so they will register their symbolic names even on
comment|// Lazy Loading JVMs
try|try
block|{
comment|// loading these classes will automatically register the short names
name|Class
operator|.
name|forName
argument_list|(
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
name|MetadataTypedColumnsetSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Class
operator|.
name|forName
argument_list|(
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
name|LazySimpleSerDe
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Class
operator|.
name|forName
argument_list|(
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
name|ThriftDeserializer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"IMPOSSIBLE Exception: Unable to initialize core serdes"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
literal|true
return|;
block|}
comment|/**    * Escape a String in JSON format.    */
specifier|public
specifier|static
name|String
name|escapeString
parameter_list|(
name|String
name|str
parameter_list|)
block|{
name|int
name|length
init|=
name|str
operator|.
name|length
argument_list|()
decl_stmt|;
name|StringBuilder
name|escape
init|=
operator|new
name|StringBuilder
argument_list|(
name|length
operator|+
literal|16
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
name|length
condition|;
operator|++
name|i
control|)
block|{
name|char
name|c
init|=
name|str
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|c
condition|)
block|{
case|case
literal|'"'
case|:
case|case
literal|'\\'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\b'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'b'
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\f'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'f'
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\n'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'n'
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\r'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'r'
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\t'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'t'
argument_list|)
expr_stmt|;
break|break;
default|default:
comment|// Control characeters! According to JSON RFC u0020
if|if
condition|(
name|c
operator|<
literal|' '
condition|)
block|{
name|String
name|hex
init|=
name|Integer
operator|.
name|toHexString
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'u'
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|4
init|;
name|j
operator|>
name|hex
operator|.
name|length
argument_list|()
condition|;
operator|--
name|j
control|)
block|{
name|escape
operator|.
name|append
argument_list|(
literal|'0'
argument_list|)
expr_stmt|;
block|}
name|escape
operator|.
name|append
argument_list|(
name|hex
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|escape
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
block|}
return|return
operator|(
name|escape
operator|.
name|toString
argument_list|()
operator|)
return|;
block|}
specifier|public
specifier|static
name|String
name|lightEscapeString
parameter_list|(
name|String
name|str
parameter_list|)
block|{
name|int
name|length
init|=
name|str
operator|.
name|length
argument_list|()
decl_stmt|;
name|StringBuilder
name|escape
init|=
operator|new
name|StringBuilder
argument_list|(
name|length
operator|+
literal|16
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
name|length
condition|;
operator|++
name|i
control|)
block|{
name|char
name|c
init|=
name|str
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|c
condition|)
block|{
case|case
literal|'\n'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'n'
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\r'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'r'
argument_list|)
expr_stmt|;
break|break;
case|case
literal|'\t'
case|:
name|escape
operator|.
name|append
argument_list|(
literal|'\\'
argument_list|)
expr_stmt|;
name|escape
operator|.
name|append
argument_list|(
literal|'t'
argument_list|)
expr_stmt|;
break|break;
default|default:
name|escape
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
return|return
operator|(
name|escape
operator|.
name|toString
argument_list|()
operator|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getJSONString
parameter_list|(
name|Object
name|o
parameter_list|,
name|ObjectInspector
name|oi
parameter_list|)
block|{
return|return
name|getJSONString
argument_list|(
name|o
argument_list|,
name|oi
argument_list|,
name|JSON_NULL
argument_list|)
return|;
block|}
comment|/**    * Use this if you need to have custom representation of top level null .    * (ie something other than 'null')    * eg, for hive output, we want to to print NULL for a null map object.    * @param o Object    * @param oi ObjectInspector    * @param nullStr The custom string used to represent null value    * @return    */
specifier|public
specifier|static
name|String
name|getJSONString
parameter_list|(
name|Object
name|o
parameter_list|,
name|ObjectInspector
name|oi
parameter_list|,
name|String
name|nullStr
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|buildJSONString
argument_list|(
name|sb
argument_list|,
name|o
argument_list|,
name|oi
argument_list|,
name|nullStr
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|static
name|void
name|buildJSONString
parameter_list|(
name|StringBuilder
name|sb
parameter_list|,
name|Object
name|o
parameter_list|,
name|ObjectInspector
name|oi
parameter_list|,
name|String
name|nullStr
parameter_list|)
block|{
switch|switch
condition|(
name|oi
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
block|{
name|PrimitiveObjectInspector
name|poi
init|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|oi
decl_stmt|;
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|nullStr
argument_list|)
expr_stmt|;
block|}
else|else
block|{
switch|switch
condition|(
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
block|{
name|boolean
name|b
init|=
operator|(
operator|(
name|BooleanObjectInspector
operator|)
name|poi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|b
condition|?
literal|"true"
else|:
literal|"false"
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|BYTE
case|:
block|{
name|sb
operator|.
name|append
argument_list|(
operator|(
operator|(
name|ByteObjectInspector
operator|)
name|poi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|SHORT
case|:
block|{
name|sb
operator|.
name|append
argument_list|(
operator|(
operator|(
name|ShortObjectInspector
operator|)
name|poi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|INT
case|:
block|{
name|sb
operator|.
name|append
argument_list|(
operator|(
operator|(
name|IntObjectInspector
operator|)
name|poi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|LONG
case|:
block|{
name|sb
operator|.
name|append
argument_list|(
operator|(
operator|(
name|LongObjectInspector
operator|)
name|poi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|FLOAT
case|:
block|{
name|sb
operator|.
name|append
argument_list|(
operator|(
operator|(
name|FloatObjectInspector
operator|)
name|poi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|DOUBLE
case|:
block|{
name|sb
operator|.
name|append
argument_list|(
operator|(
operator|(
name|DoubleObjectInspector
operator|)
name|poi
operator|)
operator|.
name|get
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|STRING
case|:
block|{
name|sb
operator|.
name|append
argument_list|(
literal|'"'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|escapeString
argument_list|(
operator|(
operator|(
name|StringObjectInspector
operator|)
name|poi
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'"'
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|TIMESTAMP
case|:
block|{
name|sb
operator|.
name|append
argument_list|(
literal|'"'
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
operator|(
operator|(
name|TimestampObjectInspector
operator|)
name|poi
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'"'
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|BINARY
case|:
block|{
name|BytesWritable
name|bw
init|=
operator|(
operator|(
name|BinaryObjectInspector
operator|)
name|oi
operator|)
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|o
argument_list|)
decl_stmt|;
name|Text
name|txt
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|txt
operator|.
name|set
argument_list|(
name|bw
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|bw
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|txt
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
case|case
name|DECIMAL
case|:
block|{
name|sb
operator|.
name|append
argument_list|(
operator|(
operator|(
name|HiveDecimalObjectInspector
operator|)
name|oi
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown primitive type: "
operator|+
name|poi
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
throw|;
block|}
block|}
break|break;
block|}
case|case
name|LIST
case|:
block|{
name|ListObjectInspector
name|loi
init|=
operator|(
name|ListObjectInspector
operator|)
name|oi
decl_stmt|;
name|ObjectInspector
name|listElementObjectInspector
init|=
name|loi
operator|.
name|getListElementObjectInspector
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|?
argument_list|>
name|olist
init|=
name|loi
operator|.
name|getList
argument_list|(
name|o
argument_list|)
decl_stmt|;
if|if
condition|(
name|olist
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|nullStr
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|LBRACKET
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
name|olist
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
name|COMMA
argument_list|)
expr_stmt|;
block|}
name|buildJSONString
argument_list|(
name|sb
argument_list|,
name|olist
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|listElementObjectInspector
argument_list|,
name|JSON_NULL
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|RBRACKET
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
case|case
name|MAP
case|:
block|{
name|MapObjectInspector
name|moi
init|=
operator|(
name|MapObjectInspector
operator|)
name|oi
decl_stmt|;
name|ObjectInspector
name|mapKeyObjectInspector
init|=
name|moi
operator|.
name|getMapKeyObjectInspector
argument_list|()
decl_stmt|;
name|ObjectInspector
name|mapValueObjectInspector
init|=
name|moi
operator|.
name|getMapValueObjectInspector
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|omap
init|=
name|moi
operator|.
name|getMap
argument_list|(
name|o
argument_list|)
decl_stmt|;
if|if
condition|(
name|omap
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|nullStr
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|LBRACE
argument_list|)
expr_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
for|for
control|(
name|Object
name|entry
range|:
name|omap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|COMMA
argument_list|)
expr_stmt|;
block|}
name|Map
operator|.
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|e
init|=
operator|(
name|Map
operator|.
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|entry
decl_stmt|;
name|buildJSONString
argument_list|(
name|sb
argument_list|,
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|mapKeyObjectInspector
argument_list|,
name|JSON_NULL
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COLON
argument_list|)
expr_stmt|;
name|buildJSONString
argument_list|(
name|sb
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|,
name|mapValueObjectInspector
argument_list|,
name|JSON_NULL
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|RBRACE
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
case|case
name|STRUCT
case|:
block|{
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|oi
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|structFields
init|=
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|nullStr
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|LBRACE
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
name|structFields
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
name|COMMA
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|QUOTE
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|structFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|QUOTE
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COLON
argument_list|)
expr_stmt|;
name|buildJSONString
argument_list|(
name|sb
argument_list|,
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|o
argument_list|,
name|structFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|,
name|structFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|,
name|JSON_NULL
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|RBRACE
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
case|case
name|UNION
case|:
block|{
name|UnionObjectInspector
name|uoi
init|=
operator|(
name|UnionObjectInspector
operator|)
name|oi
decl_stmt|;
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|nullStr
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|LBRACE
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|uoi
operator|.
name|getTag
argument_list|(
name|o
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COLON
argument_list|)
expr_stmt|;
name|buildJSONString
argument_list|(
name|sb
argument_list|,
name|uoi
operator|.
name|getField
argument_list|(
name|o
argument_list|)
argument_list|,
name|uoi
operator|.
name|getObjectInspectors
argument_list|()
operator|.
name|get
argument_list|(
name|uoi
operator|.
name|getTag
argument_list|(
name|o
argument_list|)
argument_list|)
argument_list|,
name|JSON_NULL
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|RBRACE
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown type in ObjectInspector!"
argument_list|)
throw|;
block|}
block|}
comment|/**    * return false though element is null if nullsafe flag is true for that    */
specifier|public
specifier|static
name|boolean
name|hasAnyNullObject
parameter_list|(
name|List
name|o
parameter_list|,
name|StandardStructObjectInspector
name|loi
parameter_list|,
name|boolean
index|[]
name|nullSafes
parameter_list|)
block|{
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fields
init|=
name|loi
operator|.
name|getAllStructFieldRefs
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
name|o
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
operator|(
name|nullSafes
operator|==
literal|null
operator|||
operator|!
name|nullSafes
index|[
name|i
index|]
operator|)
operator|&&
name|hasAnyNullObject
argument_list|(
name|o
operator|.
name|get
argument_list|(
name|i
argument_list|)
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
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * True if Object passed is representing null object.    *    * @param o The object    * @param oi The ObjectInspector    *    * @return true if the object passed is representing NULL object    *         false otherwise    */
specifier|public
specifier|static
name|boolean
name|hasAnyNullObject
parameter_list|(
name|Object
name|o
parameter_list|,
name|ObjectInspector
name|oi
parameter_list|)
block|{
switch|switch
condition|(
name|oi
operator|.
name|getCategory
argument_list|()
condition|)
block|{
case|case
name|PRIMITIVE
case|:
block|{
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
case|case
name|LIST
case|:
block|{
name|ListObjectInspector
name|loi
init|=
operator|(
name|ListObjectInspector
operator|)
name|oi
decl_stmt|;
name|ObjectInspector
name|listElementObjectInspector
init|=
name|loi
operator|.
name|getListElementObjectInspector
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|?
argument_list|>
name|olist
init|=
name|loi
operator|.
name|getList
argument_list|(
name|o
argument_list|)
decl_stmt|;
if|if
condition|(
name|olist
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
comment|// there are no elements in the list
if|if
condition|(
name|olist
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// if all the elements are representing null, then return true
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|olist
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
name|hasAnyNullObject
argument_list|(
name|olist
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|listElementObjectInspector
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
case|case
name|MAP
case|:
block|{
name|MapObjectInspector
name|moi
init|=
operator|(
name|MapObjectInspector
operator|)
name|oi
decl_stmt|;
name|ObjectInspector
name|mapKeyObjectInspector
init|=
name|moi
operator|.
name|getMapKeyObjectInspector
argument_list|()
decl_stmt|;
name|ObjectInspector
name|mapValueObjectInspector
init|=
name|moi
operator|.
name|getMapValueObjectInspector
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|omap
init|=
name|moi
operator|.
name|getMap
argument_list|(
name|o
argument_list|)
decl_stmt|;
if|if
condition|(
name|omap
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
comment|// there are no elements in the map
if|if
condition|(
name|omap
operator|.
name|entrySet
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// if all the entries of map are representing null, then return true
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|entry
range|:
name|omap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|hasAnyNullObject
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|mapKeyObjectInspector
argument_list|)
operator|||
name|hasAnyNullObject
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|mapValueObjectInspector
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
case|case
name|STRUCT
case|:
block|{
name|StructObjectInspector
name|soi
init|=
operator|(
name|StructObjectInspector
operator|)
name|oi
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|structFields
init|=
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
comment|// there are no fields in the struct
if|if
condition|(
name|structFields
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// if any the fields of struct are representing null, then return true
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|structFields
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
name|hasAnyNullObject
argument_list|(
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|o
argument_list|,
name|structFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|,
name|structFields
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
case|case
name|UNION
case|:
block|{
name|UnionObjectInspector
name|uoi
init|=
operator|(
name|UnionObjectInspector
operator|)
name|oi
decl_stmt|;
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
comment|// there are no elements in the union
if|if
condition|(
name|uoi
operator|.
name|getObjectInspectors
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|hasAnyNullObject
argument_list|(
name|uoi
operator|.
name|getField
argument_list|(
name|o
argument_list|)
argument_list|,
name|uoi
operator|.
name|getObjectInspectors
argument_list|()
operator|.
name|get
argument_list|(
name|uoi
operator|.
name|getTag
argument_list|(
name|o
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown type in ObjectInspector!"
argument_list|)
throw|;
block|}
block|}
specifier|private
name|SerDeUtils
parameter_list|()
block|{
comment|// prevent instantiation
block|}
block|}
end_class

end_unit

