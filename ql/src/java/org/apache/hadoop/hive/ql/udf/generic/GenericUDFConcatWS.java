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
name|ql
operator|.
name|udf
operator|.
name|generic
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
name|exec
operator|.
name|UDFArgumentLengthException
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
name|UDFArgumentTypeException
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
name|serde
operator|.
name|Constants
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * Generic UDF for string function  *<code>CONCAT_WS(sep,str1,str2,str3,...)</code>. This mimics the function from  * MySQL http://dev.mysql.com/doc/refman/5.0/en/string-functions.html#  * function_concat-ws  *   * @see org.apache.hadoop.hive.ql.udf.generic.GenericUDF  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"concat_ws"
argument_list|,
name|value
operator|=
literal|"_FUNC_(separator, str1, str2, ...) - "
operator|+
literal|"returns the concatenation of the strings separated by the separator."
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_('ce', 'fa', 'book') FROM src LIMIT 1;\n"
operator|+
literal|"  'facebook'"
argument_list|)
specifier|public
class|class
name|GenericUDFConcatWS
extends|extends
name|GenericUDF
block|{
specifier|private
name|ObjectInspector
index|[]
name|argumentOIs
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
if|if
condition|(
name|arguments
operator|.
name|length
operator|<
literal|2
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"The function CONCAT_WS(separator,str1,str2,str3,...) needs at least two arguments."
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|arguments
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|arguments
index|[
name|i
index|]
operator|.
name|getTypeName
argument_list|()
operator|!=
name|Constants
operator|.
name|STRING_TYPE_NAME
operator|&&
name|arguments
index|[
name|i
index|]
operator|.
name|getTypeName
argument_list|()
operator|!=
name|Constants
operator|.
name|VOID_TYPE_NAME
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|i
argument_list|,
literal|"Argument "
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
operator|+
literal|" of function CONCAT_WS must be \""
operator|+
name|Constants
operator|.
name|STRING_TYPE_NAME
operator|+
literal|"\", but \""
operator|+
name|arguments
index|[
name|i
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\" was found."
argument_list|)
throw|;
block|}
block|}
name|argumentOIs
operator|=
name|arguments
expr_stmt|;
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
return|;
block|}
specifier|private
specifier|final
name|Text
name|resultText
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
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
if|if
condition|(
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
name|separator
init|=
operator|(
operator|(
name|StringObjectInspector
operator|)
name|argumentOIs
index|[
literal|0
index|]
operator|)
operator|.
name|getPrimitiveJavaObject
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
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|arguments
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|arguments
index|[
name|i
index|]
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
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
name|separator
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
operator|(
operator|(
name|StringObjectInspector
operator|)
name|argumentOIs
index|[
name|i
index|]
operator|)
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|arguments
index|[
name|i
index|]
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|resultText
operator|.
name|set
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|resultText
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
operator|>=
literal|2
operator|)
assert|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"concat_ws("
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
name|children
operator|.
name|length
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|children
index|[
name|i
index|]
argument_list|)
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|children
index|[
name|children
operator|.
name|length
operator|-
literal|1
index|]
argument_list|)
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

