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
name|ql
operator|.
name|udf
operator|.
name|UDFLike
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
name|BooleanWritable
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
comment|/**  * GenericUDFLikeAll is return true if a text(column value) matches to all patterns  *  * Example usage: SELECT key FROM src WHERE key like all ('%ab%', 'a%','b%','abc');  *  * LIKE ALL returns true if test matches all patterns patternN.  * Returns NULL if the expression on the left hand side is NULL or if one of the patterns in the list is NULL.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"like all"
argument_list|,
name|value
operator|=
literal|"test _FUNC_(pattern1, pattern2...) - returns true if test matches all patterns patternN. "
operator|+
literal|" Returns NULL if the expression on the left hand side is NULL or if one of the patterns in the list is NULL."
argument_list|)
specifier|public
class|class
name|GenericUDFLikeAll
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|PrimitiveCategory
index|[]
name|inputTypes
decl_stmt|;
specifier|private
specifier|transient
name|Converter
index|[]
name|converters
decl_stmt|;
specifier|private
specifier|transient
name|boolean
name|isConstantNullPatternContain
decl_stmt|;
specifier|private
name|boolean
name|isAllPatternsConstant
init|=
literal|true
decl_stmt|;
specifier|private
specifier|final
name|BooleanWritable
name|bw
init|=
operator|new
name|BooleanWritable
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
operator|<
literal|2
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"The like all operator requires at least one pattern for matching, got "
operator|+
operator|(
name|arguments
operator|.
name|length
operator|-
literal|1
operator|)
argument_list|)
throw|;
block|}
name|inputTypes
operator|=
operator|new
name|PrimitiveCategory
index|[
name|arguments
operator|.
name|length
index|]
expr_stmt|;
name|converters
operator|=
operator|new
name|Converter
index|[
name|arguments
operator|.
name|length
index|]
expr_stmt|;
comment|/**expects string or null arguments */
for|for
control|(
name|int
name|idx
init|=
literal|0
init|;
name|idx
operator|<
name|arguments
operator|.
name|length
condition|;
name|idx
operator|++
control|)
block|{
name|checkArgPrimitive
argument_list|(
name|arguments
argument_list|,
name|idx
argument_list|)
expr_stmt|;
name|checkArgGroups
argument_list|(
name|arguments
argument_list|,
name|idx
argument_list|,
name|inputTypes
argument_list|,
name|PrimitiveGrouping
operator|.
name|STRING_GROUP
argument_list|,
name|PrimitiveGrouping
operator|.
name|VOID_GROUP
argument_list|)
expr_stmt|;
name|PrimitiveCategory
name|inputType
init|=
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
decl_stmt|;
if|if
condition|(
name|arguments
index|[
name|idx
index|]
operator|instanceof
name|ConstantObjectInspector
operator|&&
name|idx
operator|!=
literal|0
condition|)
block|{
name|Object
name|constValue
init|=
operator|(
operator|(
name|ConstantObjectInspector
operator|)
name|arguments
index|[
name|idx
index|]
operator|)
operator|.
name|getWritableConstantValue
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|isConstantNullPatternContain
operator|&&
name|constValue
operator|==
literal|null
condition|)
block|{
name|isConstantNullPatternContain
operator|=
literal|true
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|idx
operator|!=
literal|0
operator|&&
name|isAllPatternsConstant
condition|)
block|{
name|isAllPatternsConstant
operator|=
literal|false
expr_stmt|;
block|}
name|converters
index|[
name|idx
index|]
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|arguments
index|[
name|idx
index|]
argument_list|,
name|getOutputOI
argument_list|(
name|inputType
argument_list|)
argument_list|)
expr_stmt|;
name|inputTypes
index|[
name|idx
index|]
operator|=
name|inputType
expr_stmt|;
block|}
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
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
name|bw
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|/**If field value or any constant string pattern value is null then return null*/
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
name|isConstantNullPatternContain
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|/**If all patterns are constant string and no pattern have null value the do short circuit boolean check      * Else evaluate all patterns if any pattern contains null value then return null otherwise at last return matching result      * */
name|Text
name|columnValue
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
name|pattern
init|=
operator|new
name|Text
argument_list|()
decl_stmt|;
name|UDFLike
name|likeUdf
init|=
operator|new
name|UDFLike
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|idx
init|=
literal|1
init|;
name|idx
operator|<
name|arguments
operator|.
name|length
condition|;
name|idx
operator|++
control|)
block|{
if|if
condition|(
name|arguments
index|[
name|idx
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
name|pattern
operator|.
name|set
argument_list|(
operator|(
name|Text
operator|)
name|converters
index|[
name|idx
index|]
operator|.
name|convert
argument_list|(
name|arguments
index|[
name|idx
index|]
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|likeUdf
operator|.
name|evaluate
argument_list|(
name|columnValue
argument_list|,
name|pattern
argument_list|)
operator|.
name|get
argument_list|()
operator|&&
name|bw
operator|.
name|get
argument_list|()
condition|)
block|{
name|bw
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|isAllPatternsConstant
condition|)
block|{
return|return
name|bw
return|;
block|}
block|}
block|}
return|return
name|bw
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
literal|"likeall"
argument_list|,
name|children
argument_list|)
return|;
block|}
specifier|private
name|ObjectInspector
name|getOutputOI
parameter_list|(
name|PrimitiveCategory
name|inputType
parameter_list|)
block|{
switch|switch
condition|(
name|inputType
condition|)
block|{
case|case
name|CHAR
case|:
case|case
name|STRING
case|:
case|case
name|VARCHAR
case|:
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
return|;
case|case
name|VOID
case|:
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableVoidObjectInspector
return|;
default|default:
break|break;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

