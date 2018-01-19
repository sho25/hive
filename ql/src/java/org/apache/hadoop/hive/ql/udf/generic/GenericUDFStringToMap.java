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
operator|.
name|generic
package|;
end_package

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
name|UDFArgumentException
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
name|metadata
operator|.
name|HiveException
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
name|ObjectInspectorConverters
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
name|ObjectInspector
operator|.
name|Category
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
name|ObjectInspectorConverters
operator|.
name|Converter
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
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorUtils
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
name|PrimitiveObjectInspectorUtils
operator|.
name|PrimitiveGrouping
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

begin_comment
comment|/**  * GenericUDFStringToMap.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"str_to_map"
argument_list|,
name|value
operator|=
literal|"_FUNC_(text, delimiter1, delimiter2) - "
operator|+
literal|"Creates a map by parsing text "
argument_list|,
name|extended
operator|=
literal|"Split text into key-value pairs"
operator|+
literal|" using two delimiters. The first delimiter separates pairs, and the"
operator|+
literal|" second delimiter sperates key and value. If only one parameter is given, default"
operator|+
literal|" delimiters are used: ',' as delimiter1 and ':' as delimiter2."
argument_list|)
specifier|public
class|class
name|GenericUDFStringToMap
extends|extends
name|GenericUDF
block|{
comment|// Must be deterministic order map for consistent q-test output across Java versions - see HIVE-9161
specifier|private
specifier|final
name|LinkedHashMap
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|ret
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
name|Converter
name|soi_text
decl_stmt|,
name|soi_de1
init|=
literal|null
decl_stmt|,
name|soi_de2
init|=
literal|null
decl_stmt|;
specifier|final
specifier|static
name|String
name|default_de1
init|=
literal|","
decl_stmt|;
specifier|final
specifier|static
name|String
name|default_de2
init|=
literal|":"
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|initialize
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
for|for
control|(
name|int
name|idx
init|=
literal|0
init|;
name|idx
operator|<
name|Math
operator|.
name|min
argument_list|(
name|arguments
operator|.
name|length
argument_list|,
literal|3
argument_list|)
condition|;
operator|++
name|idx
control|)
block|{
if|if
condition|(
name|arguments
index|[
name|idx
index|]
operator|.
name|getCategory
argument_list|()
operator|!=
name|Category
operator|.
name|PRIMITIVE
operator|||
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
name|idx
index|]
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
operator|!=
name|PrimitiveGrouping
operator|.
name|STRING_GROUP
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"All argument should be string/character type"
argument_list|)
throw|;
block|}
block|}
name|soi_text
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|arguments
index|[
literal|0
index|]
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|)
expr_stmt|;
if|if
condition|(
name|arguments
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|soi_de1
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|arguments
index|[
literal|1
index|]
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|arguments
operator|.
name|length
operator|>
literal|2
condition|)
block|{
name|soi_de2
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|arguments
index|[
literal|2
index|]
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|)
expr_stmt|;
block|}
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardMapObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|)
throws|throws
name|HiveException
block|{
name|ret
operator|.
name|clear
argument_list|()
expr_stmt|;
name|String
name|text
init|=
operator|(
name|String
operator|)
name|soi_text
operator|.
name|convert
argument_list|(
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|text
operator|==
literal|null
condition|)
block|{
return|return
name|ret
return|;
block|}
name|String
name|delimiter1
init|=
operator|(
name|soi_de1
operator|==
literal|null
operator|)
condition|?
name|default_de1
else|:
operator|(
name|String
operator|)
name|soi_de1
operator|.
name|convert
argument_list|(
name|arguments
index|[
literal|1
index|]
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|delimiter2
init|=
operator|(
name|soi_de2
operator|==
literal|null
operator|)
condition|?
name|default_de2
else|:
operator|(
name|String
operator|)
name|soi_de2
operator|.
name|convert
argument_list|(
name|arguments
index|[
literal|2
index|]
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|delimiter1
operator|==
literal|null
condition|)
block|{
name|delimiter1
operator|=
name|default_de1
expr_stmt|;
block|}
if|if
condition|(
name|delimiter2
operator|==
literal|null
condition|)
block|{
name|delimiter2
operator|=
name|default_de2
expr_stmt|;
block|}
name|String
index|[]
name|keyValuePairs
init|=
name|text
operator|.
name|split
argument_list|(
name|delimiter1
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|keyValuePair
range|:
name|keyValuePairs
control|)
block|{
name|String
index|[]
name|keyValue
init|=
name|keyValuePair
operator|.
name|split
argument_list|(
name|delimiter2
argument_list|,
literal|2
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyValue
operator|.
name|length
operator|<
literal|2
condition|)
block|{
name|ret
operator|.
name|put
argument_list|(
name|keyValuePair
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ret
operator|.
name|put
argument_list|(
name|keyValue
index|[
literal|0
index|]
argument_list|,
name|keyValue
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDisplayString
parameter_list|(
name|String
index|[]
name|children
parameter_list|)
block|{
assert|assert
operator|(
name|children
operator|.
name|length
operator|<=
literal|3
operator|)
assert|;
return|return
name|getStandardDisplayString
argument_list|(
literal|"str_to_map"
argument_list|,
name|children
argument_list|,
literal|","
argument_list|)
return|;
block|}
block|}
end_class

end_unit

