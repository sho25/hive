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
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
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
name|IntWritable
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

begin_class
specifier|public
specifier|abstract
class|class
name|GenericUDFBasePad
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|Converter
name|converter1
decl_stmt|;
specifier|private
specifier|transient
name|Converter
name|converter2
decl_stmt|;
specifier|private
specifier|transient
name|Converter
name|converter3
decl_stmt|;
specifier|private
name|Text
name|result
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
specifier|private
name|String
name|udfName
decl_stmt|;
specifier|private
name|StringBuilder
name|builder
decl_stmt|;
specifier|public
name|GenericUDFBasePad
parameter_list|(
name|String
name|_udfName
parameter_list|)
block|{
name|this
operator|.
name|udfName
operator|=
name|_udfName
expr_stmt|;
name|this
operator|.
name|builder
operator|=
operator|new
name|StringBuilder
argument_list|()
expr_stmt|;
block|}
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
literal|3
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
name|udfName
operator|+
literal|" requires three arguments. Found :"
operator|+
name|arguments
operator|.
name|length
argument_list|)
throw|;
block|}
name|converter1
operator|=
name|checkTextArguments
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|converter2
operator|=
name|checkIntArguments
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|converter3
operator|=
name|checkTextArguments
argument_list|(
name|arguments
argument_list|,
literal|2
argument_list|)
expr_stmt|;
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
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
name|Object
name|valObject1
init|=
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
name|Object
name|valObject2
init|=
name|arguments
index|[
literal|1
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
name|Object
name|valObject3
init|=
name|arguments
index|[
literal|2
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|valObject1
operator|==
literal|null
operator|||
name|valObject2
operator|==
literal|null
operator|||
name|valObject3
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|Text
name|str
init|=
operator|(
name|Text
operator|)
name|converter1
operator|.
name|convert
argument_list|(
name|valObject1
argument_list|)
decl_stmt|;
name|IntWritable
name|lenW
init|=
operator|(
name|IntWritable
operator|)
name|converter2
operator|.
name|convert
argument_list|(
name|valObject2
argument_list|)
decl_stmt|;
name|Text
name|pad
init|=
operator|(
name|Text
operator|)
name|converter3
operator|.
name|convert
argument_list|(
name|valObject3
argument_list|)
decl_stmt|;
if|if
condition|(
name|str
operator|==
literal|null
operator|||
name|pad
operator|==
literal|null
operator|||
name|lenW
operator|==
literal|null
operator|||
name|pad
operator|.
name|toString
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|len
init|=
name|lenW
operator|.
name|get
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|performOp
argument_list|(
name|builder
argument_list|,
name|len
argument_list|,
name|str
operator|.
name|toString
argument_list|()
argument_list|,
name|pad
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|set
argument_list|(
name|builder
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
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
return|return
name|getStandardDisplayString
argument_list|(
name|udfName
argument_list|,
name|children
argument_list|)
return|;
block|}
specifier|protected
specifier|abstract
name|void
name|performOp
parameter_list|(
name|StringBuilder
name|builder
parameter_list|,
name|int
name|len
parameter_list|,
name|String
name|str
parameter_list|,
name|String
name|pad
parameter_list|)
function_decl|;
comment|// Convert input arguments to Text, if necessary.
specifier|private
name|Converter
name|checkTextArguments
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|,
name|int
name|i
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
if|if
condition|(
name|arguments
index|[
name|i
index|]
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|i
argument_list|,
literal|"Only primitive type arguments are accepted but "
operator|+
name|arguments
index|[
name|i
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" is passed."
argument_list|)
throw|;
block|}
name|Converter
name|converter
init|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
name|i
index|]
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
argument_list|)
decl_stmt|;
return|return
name|converter
return|;
block|}
specifier|private
name|Converter
name|checkIntArguments
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|,
name|int
name|i
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
if|if
condition|(
name|arguments
index|[
name|i
index|]
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|i
argument_list|,
literal|"Only primitive type arguments are accepted but "
operator|+
name|arguments
index|[
name|i
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" is passed."
argument_list|)
throw|;
block|}
name|PrimitiveCategory
name|inputType
init|=
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
name|i
index|]
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
name|Converter
name|converter
decl_stmt|;
switch|switch
condition|(
name|inputType
condition|)
block|{
case|case
name|INT
case|:
case|case
name|SHORT
case|:
case|case
name|BYTE
case|:
name|converter
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
name|i
index|]
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|i
operator|+
literal|1
argument_list|,
name|udfName
operator|+
literal|" only takes INT/SHORT/BYTE types as "
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
operator|+
literal|"-ths argument, got "
operator|+
name|inputType
argument_list|)
throw|;
block|}
return|return
name|converter
return|;
block|}
block|}
end_class

end_unit

