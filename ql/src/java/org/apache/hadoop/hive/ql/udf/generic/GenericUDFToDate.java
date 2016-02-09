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
name|CastLongToDate
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
name|CastStringToDate
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
name|CastTimestampToDate
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
name|PrimitiveObjectInspectorConverter
operator|.
name|DateConverter
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

begin_comment
comment|/**  * GenericUDFToDate  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"date"
argument_list|,
name|value
operator|=
literal|"CAST(<Date string> as DATE) - Returns the date represented by the date string."
argument_list|,
name|extended
operator|=
literal|"date_string is a string in the format 'yyyy-MM-dd.'"
operator|+
literal|"Example:\n "
operator|+
literal|"> SELECT CAST('2009-01-01' AS DATE) FROM src LIMIT 1;\n"
operator|+
literal|"  '2009-01-01'"
argument_list|)
annotation|@
name|VectorizedExpressions
argument_list|(
block|{
name|CastStringToDate
operator|.
name|class
block|,
name|CastLongToDate
operator|.
name|class
block|,
name|CastTimestampToDate
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|GenericUDFToDate
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|PrimitiveObjectInspector
name|argumentOI
decl_stmt|;
specifier|private
specifier|transient
name|DateConverter
name|dc
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
literal|1
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"The function CAST as DATE requires at least one argument, got "
operator|+
name|arguments
operator|.
name|length
argument_list|)
throw|;
block|}
try|try
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
name|PrimitiveCategory
name|pc
init|=
name|argumentOI
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
name|PrimitiveGrouping
name|pg
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getPrimitiveGrouping
argument_list|(
name|pc
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|pg
condition|)
block|{
case|case
name|DATE_GROUP
case|:
case|case
name|STRING_GROUP
case|:
case|case
name|VOID_GROUP
case|:
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"CAST as DATE only allows date,string, or timestamp types"
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"The function CAST as DATE takes only primitive types"
argument_list|)
throw|;
block|}
name|dc
operator|=
operator|new
name|DateConverter
argument_list|(
name|argumentOI
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDateObjectInspector
argument_list|)
expr_stmt|;
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDateObjectInspector
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
name|o0
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
name|o0
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|dc
operator|.
name|convert
argument_list|(
name|o0
argument_list|)
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
literal|1
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
literal|"CAST( "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|children
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" AS DATE)"
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

