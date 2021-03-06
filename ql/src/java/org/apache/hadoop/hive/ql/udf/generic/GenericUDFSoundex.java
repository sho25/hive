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
name|codec
operator|.
name|language
operator|.
name|Soundex
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * GenericUDFSoundex.  *  * Soundex is an encoding used to relate similar names, but can also be used as  * a general purpose scheme to find word with similar phonemes.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"soundex"
argument_list|,
name|value
operator|=
literal|"_FUNC_(string) - Returns soundex code of the string."
argument_list|,
name|extended
operator|=
literal|"The soundex code consist of the first letter of the name followed by three digits.\n"
operator|+
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_('Miller');\n M460"
argument_list|)
specifier|public
class|class
name|GenericUDFSoundex
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|final
specifier|transient
name|Converter
index|[]
name|textConverters
init|=
operator|new
name|Converter
index|[
literal|1
index|]
decl_stmt|;
specifier|private
specifier|final
specifier|transient
name|PrimitiveCategory
index|[]
name|inputTypes
init|=
operator|new
name|PrimitiveCategory
index|[
literal|1
index|]
decl_stmt|;
specifier|private
specifier|final
specifier|transient
name|Soundex
name|soundex
init|=
operator|new
name|Soundex
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Text
name|output
init|=
operator|new
name|Text
argument_list|()
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
literal|1
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
name|getFuncName
argument_list|()
operator|+
literal|" requires 1 argument, got "
operator|+
name|arguments
operator|.
name|length
argument_list|)
throw|;
block|}
name|checkIfPrimitive
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|,
literal|"1st"
argument_list|)
expr_stmt|;
name|checkIfStringGroup
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|,
literal|"1st"
argument_list|)
expr_stmt|;
name|getStringConverter
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|,
literal|"1st"
argument_list|)
expr_stmt|;
name|ObjectInspector
name|outputOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
decl_stmt|;
return|return
name|outputOI
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
name|obj0
decl_stmt|;
if|if
condition|(
operator|(
name|obj0
operator|=
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
operator|)
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
name|str0
init|=
name|textConverters
index|[
literal|0
index|]
operator|.
name|convert
argument_list|(
name|obj0
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|soundexCode
decl_stmt|;
try|try
block|{
name|soundexCode
operator|=
name|soundex
operator|.
name|soundex
argument_list|(
name|str0
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
name|output
operator|.
name|set
argument_list|(
name|soundexCode
argument_list|)
expr_stmt|;
return|return
name|output
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
name|getFuncName
argument_list|()
argument_list|,
name|children
argument_list|)
return|;
block|}
specifier|protected
name|void
name|checkIfPrimitive
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|,
name|int
name|i
parameter_list|,
name|String
name|argOrder
parameter_list|)
throws|throws
name|UDFArgumentTypeException
block|{
name|ObjectInspector
operator|.
name|Category
name|oiCat
init|=
name|arguments
index|[
name|i
index|]
operator|.
name|getCategory
argument_list|()
decl_stmt|;
if|if
condition|(
name|oiCat
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
name|getFuncName
argument_list|()
operator|+
literal|" only takes primitive types as "
operator|+
name|argOrder
operator|+
literal|" argument, got "
operator|+
name|oiCat
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|void
name|checkIfStringGroup
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|,
name|int
name|i
parameter_list|,
name|String
name|argOrder
parameter_list|)
throws|throws
name|UDFArgumentTypeException
block|{
name|inputTypes
index|[
name|i
index|]
operator|=
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
expr_stmt|;
if|if
condition|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|inputTypes
index|[
name|i
index|]
argument_list|)
operator|!=
name|PrimitiveGrouping
operator|.
name|STRING_GROUP
operator|&&
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|inputTypes
index|[
name|i
index|]
argument_list|)
operator|!=
name|PrimitiveGrouping
operator|.
name|VOID_GROUP
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|i
argument_list|,
name|getFuncName
argument_list|()
operator|+
literal|" only takes STRING_GROUP types as "
operator|+
name|argOrder
operator|+
literal|" argument, got "
operator|+
name|inputTypes
index|[
name|i
index|]
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|void
name|getStringConverter
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|,
name|int
name|i
parameter_list|,
name|String
name|argOrder
parameter_list|)
throws|throws
name|UDFArgumentTypeException
block|{
name|textConverters
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
annotation|@
name|Override
specifier|protected
name|String
name|getFuncName
parameter_list|()
block|{
return|return
literal|"soundex"
return|;
block|}
block|}
end_class

end_unit

