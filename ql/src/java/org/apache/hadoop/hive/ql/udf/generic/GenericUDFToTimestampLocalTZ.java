begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|SettableUDF
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
name|SettableTimestampLocalTZObjectInspector
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
name|TimestampLocalTZConverter
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
name|TimestampLocalTZTypeInfo
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
name|TypeInfo
import|;
end_import

begin_comment
comment|/**  * Convert from string to TIMESTAMP WITH LOCAL TIME ZONE.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"timestamp with local time zone"
argument_list|,
name|value
operator|=
literal|"CAST(STRING as TIMESTAMP WITH LOCAL TIME ZONE) - returns the"
operator|+
literal|"timestamp with local time zone represented by string."
argument_list|,
name|extended
operator|=
literal|"The string should be of format 'yyyy-MM-dd HH:mm:ss[.SSS...] ZoneId/ZoneOffset'. "
operator|+
literal|"Examples of ZoneId and ZoneOffset are Asia/Shanghai and GMT+08:00. "
operator|+
literal|"The time and zone parts are optional. If time is absent, '00:00:00.0' will be used. "
operator|+
literal|"If zone is absent, the system time zone will be used."
argument_list|)
specifier|public
class|class
name|GenericUDFToTimestampLocalTZ
extends|extends
name|GenericUDF
implements|implements
name|SettableUDF
block|{
specifier|private
specifier|transient
name|PrimitiveObjectInspector
name|argumentOI
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveObjectInspectorConverter
operator|.
name|TimestampLocalTZConverter
name|converter
decl_stmt|;
specifier|private
name|TimestampLocalTZTypeInfo
name|typeInfo
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
literal|"The function CAST as TIMESTAMP WITH LOCAL TIME ZONE requires at least one argument, got "
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
switch|switch
condition|(
name|argumentOI
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|CHAR
case|:
case|case
name|VARCHAR
case|:
case|case
name|STRING
case|:
case|case
name|DATE
case|:
case|case
name|TIMESTAMP
case|:
case|case
name|TIMESTAMPLOCALTZ
case|:
break|break;
default|default:
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"CAST as TIMESTAMP WITH LOCAL TIME ZONE only allows"
operator|+
literal|"string/date/timestamp/timestamp with time zone types"
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
literal|"The function CAST as TIMESTAMP WITH LOCAL TIME ZONE takes only primitive types"
argument_list|)
throw|;
block|}
name|SettableTimestampLocalTZObjectInspector
name|outputOI
init|=
operator|(
name|SettableTimestampLocalTZObjectInspector
operator|)
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|typeInfo
argument_list|)
decl_stmt|;
name|converter
operator|=
operator|new
name|TimestampLocalTZConverter
argument_list|(
name|argumentOI
argument_list|,
name|outputOI
argument_list|)
expr_stmt|;
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
name|converter
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
literal|" AS "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|typeInfo
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
name|sb
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
annotation|@
name|Override
specifier|public
name|TypeInfo
name|getTypeInfo
parameter_list|()
block|{
return|return
name|typeInfo
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setTypeInfo
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|this
operator|.
name|typeInfo
operator|=
operator|(
name|TimestampLocalTZTypeInfo
operator|)
name|typeInfo
expr_stmt|;
block|}
block|}
end_class

end_unit

