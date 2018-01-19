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
name|STRING_GROUP
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
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|vector
operator|.
name|VectorizedExpressions
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
name|vector
operator|.
name|expressions
operator|.
name|FilterStringColRegExpStringScalar
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
name|BooleanWritable
import|;
end_import

begin_comment
comment|/**  * UDF to extract a specific group identified by a java regex. Note that if a  * regexp has a backslash ('\'), then need to specify '\\' For example,  * regexp_extract('100-200', '(\\d+)-(\\d+)', 1) will return '100'  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"rlike,regexp"
argument_list|,
name|value
operator|=
literal|"str _FUNC_ regexp - Returns true if str matches regexp and "
operator|+
literal|"false otherwise"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT 'fb' _FUNC_ '.*' FROM src LIMIT 1;\n"
operator|+
literal|"  true"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|FilterStringColRegExpStringScalar
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|GenericUDFRegExp
extends|extends
name|GenericUDF
block|{
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|GenericUDFRegExp
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
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
literal|2
index|]
decl_stmt|;
specifier|private
specifier|transient
name|Converter
index|[]
name|converters
init|=
operator|new
name|Converter
index|[
literal|2
index|]
decl_stmt|;
specifier|private
specifier|final
name|BooleanWritable
name|output
init|=
operator|new
name|BooleanWritable
argument_list|()
decl_stmt|;
specifier|private
specifier|transient
name|boolean
name|isRegexConst
decl_stmt|;
specifier|private
specifier|transient
name|String
name|regexConst
decl_stmt|;
specifier|private
specifier|transient
name|Pattern
name|patternConst
decl_stmt|;
specifier|private
specifier|transient
name|boolean
name|warned
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
literal|2
argument_list|,
literal|2
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
name|regexConst
operator|=
name|getConstantStringValue
argument_list|(
name|arguments
argument_list|,
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|regexConst
operator|!=
literal|null
condition|)
block|{
name|patternConst
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|regexConst
argument_list|)
expr_stmt|;
block|}
name|isRegexConst
operator|=
literal|true
expr_stmt|;
block|}
name|ObjectInspector
name|outputOI
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
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
name|String
name|s
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
name|s
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
name|regex
decl_stmt|;
if|if
condition|(
name|isRegexConst
condition|)
block|{
name|regex
operator|=
name|regexConst
expr_stmt|;
block|}
else|else
block|{
name|regex
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
name|regex
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
name|regex
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
if|if
condition|(
operator|!
name|warned
condition|)
block|{
name|warned
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" regex is empty. Additional "
operator|+
literal|"warnings for an empty regex will be suppressed."
argument_list|)
expr_stmt|;
block|}
name|output
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return
name|output
return|;
block|}
name|Pattern
name|p
decl_stmt|;
if|if
condition|(
name|isRegexConst
condition|)
block|{
name|p
operator|=
name|patternConst
expr_stmt|;
block|}
else|else
block|{
name|p
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|regex
argument_list|)
expr_stmt|;
block|}
name|Matcher
name|m
init|=
name|p
operator|.
name|matcher
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|output
operator|.
name|set
argument_list|(
name|m
operator|.
name|find
argument_list|(
literal|0
argument_list|)
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
name|children
index|[
literal|0
index|]
operator|+
literal|" regexp "
operator|+
name|children
index|[
literal|1
index|]
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
literal|"regexp"
return|;
block|}
block|}
end_class

end_unit

