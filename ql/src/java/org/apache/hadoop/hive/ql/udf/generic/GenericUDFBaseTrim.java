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
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|PrimitiveObjectInspectorConverter
operator|.
name|TextConverter
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

begin_class
specifier|public
specifier|abstract
class|class
name|GenericUDFBaseTrim
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|TextConverter
name|converter
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
specifier|public
name|GenericUDFBaseTrim
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
literal|1
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
name|udfName
operator|+
literal|" requires one value argument. Found :"
operator|+
name|arguments
operator|.
name|length
argument_list|)
throw|;
block|}
name|PrimitiveObjectInspector
name|argumentOI
decl_stmt|;
if|if
condition|(
name|arguments
index|[
literal|0
index|]
operator|instanceof
name|PrimitiveObjectInspector
condition|)
block|{
name|argumentOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
name|udfName
operator|+
literal|" takes only primitive types. found "
operator|+
name|arguments
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
argument_list|)
throw|;
block|}
switch|switch
condition|(
name|argumentOI
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|STRING
case|:
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
name|udfName
operator|+
literal|" takes only STRING/CHAR/VARCHAR types. Found "
operator|+
name|argumentOI
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
throw|;
block|}
name|converter
operator|=
operator|new
name|TextConverter
argument_list|(
name|argumentOI
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
name|valObject
init|=
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|valObject
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
name|val
init|=
operator|(
operator|(
name|Text
operator|)
name|converter
operator|.
name|convert
argument_list|(
name|valObject
argument_list|)
operator|)
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|val
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|result
operator|.
name|set
argument_list|(
name|performOp
argument_list|(
name|val
operator|.
name|toString
argument_list|()
argument_list|)
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
name|String
name|performOp
parameter_list|(
name|String
name|val
parameter_list|)
function_decl|;
block|}
end_class

end_unit

