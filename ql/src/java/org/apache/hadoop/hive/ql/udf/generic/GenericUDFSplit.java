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
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * GenericUDFSplit.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"split"
argument_list|,
name|value
operator|=
literal|"_FUNC_(str, regex) - Splits str around occurances that match "
operator|+
literal|"regex"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_('oneAtwoBthreeC', '[ABC]') FROM src LIMIT 1;\n"
operator|+
literal|"  [\"one\", \"two\", \"three\"]"
argument_list|)
specifier|public
class|class
name|GenericUDFSplit
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|ObjectInspectorConverters
operator|.
name|Converter
index|[]
name|converters
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
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"The function SPLIT(s, regexp) takes exactly 2 arguments."
argument_list|)
throw|;
block|}
name|converters
operator|=
operator|new
name|ObjectInspectorConverters
operator|.
name|Converter
index|[
name|arguments
operator|.
name|length
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
name|arguments
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|converters
index|[
name|i
index|]
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|arguments
index|[
name|i
index|]
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
argument_list|)
expr_stmt|;
block|}
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
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
assert|assert
operator|(
name|arguments
operator|.
name|length
operator|==
literal|2
operator|)
assert|;
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
operator|||
name|arguments
index|[
literal|1
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
name|Text
name|s
init|=
operator|(
name|Text
operator|)
name|converters
index|[
literal|0
index|]
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
name|Text
name|regex
init|=
operator|(
name|Text
operator|)
name|converters
index|[
literal|1
index|]
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
name|ArrayList
argument_list|<
name|Text
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|Text
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|str
range|:
name|s
operator|.
name|toString
argument_list|()
operator|.
name|split
argument_list|(
name|regex
operator|.
name|toString
argument_list|()
argument_list|)
control|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|new
name|Text
argument_list|(
name|str
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|result
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
operator|==
literal|2
operator|)
assert|;
return|return
literal|"split("
operator|+
name|children
index|[
literal|0
index|]
operator|+
literal|", "
operator|+
name|children
index|[
literal|1
index|]
operator|+
literal|")"
return|;
block|}
block|}
end_class

end_unit

