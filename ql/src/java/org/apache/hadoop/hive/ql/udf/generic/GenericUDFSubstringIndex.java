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
import|import static
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
operator|.
name|NUMERIC_GROUP
import|;
end_import

begin_import
import|import static
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
operator|.
name|STRING_GROUP
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
name|ConstantObjectInspector
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
name|Text
import|;
end_import

begin_comment
comment|/**  * GenericUDFSubstringIndex.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"substring_index"
argument_list|,
name|value
operator|=
literal|"_FUNC_(str, delim, count) - Returns the substring from string str before count occurrences "
operator|+
literal|"of the delimiter delim."
argument_list|,
name|extended
operator|=
literal|"If count is positive, everything to the left of the final delimiter (counting from the left) "
operator|+
literal|"is returned. If count is negative, everything to the right of the final delimiter "
operator|+
literal|"(counting from the right) is returned. Substring_index performs a case-sensitive match when searching "
operator|+
literal|"for delim.\n"
operator|+
literal|"Example:\n> SELECT _FUNC_('www.apache.org', '.', 2);\n 'www.apache'"
argument_list|)
specifier|public
class|class
name|GenericUDFSubstringIndex
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|Converter
index|[]
name|converters
init|=
operator|new
name|Converter
index|[
literal|3
index|]
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveCategory
index|[]
name|inputTypes
init|=
operator|new
name|PrimitiveCategory
index|[
literal|3
index|]
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
specifier|private
specifier|transient
name|String
name|delimConst
decl_stmt|;
specifier|private
specifier|transient
name|boolean
name|isDelimConst
decl_stmt|;
specifier|private
specifier|transient
name|Integer
name|countConst
decl_stmt|;
specifier|private
specifier|transient
name|boolean
name|isCountConst
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
name|checkArgsSize
argument_list|(
name|arguments
argument_list|,
literal|3
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|checkArgPrimitive
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|checkArgPrimitive
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|checkArgPrimitive
argument_list|(
name|arguments
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|checkArgGroups
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|,
name|inputTypes
argument_list|,
name|STRING_GROUP
argument_list|)
expr_stmt|;
name|checkArgGroups
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|,
name|inputTypes
argument_list|,
name|STRING_GROUP
argument_list|)
expr_stmt|;
name|checkArgGroups
argument_list|(
name|arguments
argument_list|,
literal|2
argument_list|,
name|inputTypes
argument_list|,
name|NUMERIC_GROUP
argument_list|)
expr_stmt|;
name|obtainStringConverter
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|,
name|inputTypes
argument_list|,
name|converters
argument_list|)
expr_stmt|;
name|obtainStringConverter
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|,
name|inputTypes
argument_list|,
name|converters
argument_list|)
expr_stmt|;
name|obtainIntConverter
argument_list|(
name|arguments
argument_list|,
literal|2
argument_list|,
name|inputTypes
argument_list|,
name|converters
argument_list|)
expr_stmt|;
if|if
condition|(
name|arguments
index|[
literal|1
index|]
operator|instanceof
name|ConstantObjectInspector
condition|)
block|{
name|delimConst
operator|=
name|getConstantStringValue
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|isDelimConst
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|arguments
index|[
literal|2
index|]
operator|instanceof
name|ConstantObjectInspector
condition|)
block|{
name|countConst
operator|=
name|getConstantIntValue
argument_list|(
name|arguments
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|isCountConst
operator|=
literal|true
expr_stmt|;
block|}
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
comment|// str
name|String
name|str
init|=
name|getStringValue
argument_list|(
name|arguments
argument_list|,
literal|0
argument_list|,
name|converters
argument_list|)
decl_stmt|;
if|if
condition|(
name|str
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|str
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|output
operator|.
name|set
argument_list|(
literal|""
argument_list|)
expr_stmt|;
return|return
name|output
return|;
block|}
comment|// delim
name|String
name|delim
decl_stmt|;
if|if
condition|(
name|isDelimConst
condition|)
block|{
name|delim
operator|=
name|delimConst
expr_stmt|;
block|}
else|else
block|{
name|delim
operator|=
name|getStringValue
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|,
name|converters
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|delim
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|delim
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|output
operator|.
name|set
argument_list|(
literal|""
argument_list|)
expr_stmt|;
return|return
name|output
return|;
block|}
comment|// count
name|Integer
name|countV
decl_stmt|;
if|if
condition|(
name|isCountConst
condition|)
block|{
name|countV
operator|=
name|countConst
expr_stmt|;
block|}
else|else
block|{
name|countV
operator|=
name|getIntValue
argument_list|(
name|arguments
argument_list|,
literal|2
argument_list|,
name|converters
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|countV
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|count
init|=
name|countV
operator|.
name|intValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|count
operator|==
literal|0
condition|)
block|{
name|output
operator|.
name|set
argument_list|(
literal|""
argument_list|)
expr_stmt|;
return|return
name|output
return|;
block|}
comment|// get substring
name|String
name|res
decl_stmt|;
if|if
condition|(
name|count
operator|>
literal|0
condition|)
block|{
name|int
name|idx
init|=
name|StringUtils
operator|.
name|ordinalIndexOf
argument_list|(
name|str
argument_list|,
name|delim
argument_list|,
name|count
argument_list|)
decl_stmt|;
if|if
condition|(
name|idx
operator|!=
operator|-
literal|1
condition|)
block|{
name|res
operator|=
name|str
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|idx
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|res
operator|=
name|str
expr_stmt|;
block|}
block|}
else|else
block|{
name|int
name|idx
init|=
name|StringUtils
operator|.
name|lastOrdinalIndexOf
argument_list|(
name|str
argument_list|,
name|delim
argument_list|,
operator|-
name|count
argument_list|)
decl_stmt|;
if|if
condition|(
name|idx
operator|!=
operator|-
literal|1
condition|)
block|{
name|res
operator|=
name|str
operator|.
name|substring
argument_list|(
name|idx
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|res
operator|=
name|str
expr_stmt|;
block|}
block|}
name|output
operator|.
name|set
argument_list|(
name|res
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
annotation|@
name|Override
specifier|protected
name|String
name|getFuncName
parameter_list|()
block|{
return|return
literal|"substring_index"
return|;
block|}
block|}
end_class

end_unit

