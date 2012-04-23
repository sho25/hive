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
name|sql
operator|.
name|Timestamp
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TimeZone
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|io
operator|.
name|TimestampWritable
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
name|PrimitiveObjectInspectorConverter
operator|.
name|TimestampConverter
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
class|class
name|GenericUDFFromUtcTimestamp
extends|extends
name|GenericUDF
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|GenericUDFFromUtcTimestamp
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|PrimitiveObjectInspector
index|[]
name|argumentOIs
decl_stmt|;
specifier|private
name|TimestampConverter
name|timestampConverter
decl_stmt|;
specifier|private
name|TextConverter
name|textConverter
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
literal|"The function "
operator|+
name|getName
argument_list|()
operator|+
literal|" requires at least two "
operator|+
literal|"argument, got "
operator|+
name|arguments
operator|.
name|length
argument_list|)
throw|;
block|}
try|try
block|{
name|argumentOIs
operator|=
operator|new
name|PrimitiveObjectInspector
index|[
literal|2
index|]
expr_stmt|;
name|argumentOIs
index|[
literal|0
index|]
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
expr_stmt|;
if|if
condition|(
name|arguments
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|argumentOIs
index|[
literal|1
index|]
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
literal|1
index|]
expr_stmt|;
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
literal|"The function "
operator|+
name|getName
argument_list|()
operator|+
literal|" takes only primitive types"
argument_list|)
throw|;
block|}
name|timestampConverter
operator|=
operator|new
name|TimestampConverter
argument_list|(
name|argumentOIs
index|[
literal|0
index|]
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableTimestampObjectInspector
argument_list|)
expr_stmt|;
name|textConverter
operator|=
operator|new
name|TextConverter
argument_list|(
name|argumentOIs
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|javaTimestampObjectInspector
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
name|TimeZone
name|timezone
init|=
literal|null
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
if|if
condition|(
name|arguments
operator|.
name|length
operator|>
literal|1
operator|&&
name|arguments
index|[
literal|1
index|]
operator|!=
literal|null
condition|)
block|{
name|Text
name|text
init|=
name|textConverter
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
if|if
condition|(
name|text
operator|!=
literal|null
condition|)
block|{
name|timezone
operator|=
name|TimeZone
operator|.
name|getTimeZone
argument_list|(
name|text
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
name|Timestamp
name|timestamp
init|=
operator|(
operator|(
name|TimestampWritable
operator|)
name|timestampConverter
operator|.
name|convert
argument_list|(
name|o0
argument_list|)
operator|)
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
name|int
name|offset
init|=
name|timezone
operator|.
name|getOffset
argument_list|(
name|timestamp
operator|.
name|getTime
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|invert
argument_list|()
condition|)
block|{
name|offset
operator|=
operator|-
name|offset
expr_stmt|;
block|}
return|return
name|applyOffset
argument_list|(
name|offset
argument_list|,
name|timestamp
argument_list|)
return|;
block|}
specifier|protected
name|Timestamp
name|applyOffset
parameter_list|(
name|long
name|offset
parameter_list|,
name|Timestamp
name|t
parameter_list|)
block|{
name|long
name|newTime
init|=
name|t
operator|.
name|getTime
argument_list|()
operator|+
name|offset
decl_stmt|;
name|Timestamp
name|t2
init|=
operator|new
name|Timestamp
argument_list|(
name|newTime
argument_list|)
decl_stmt|;
name|t2
operator|.
name|setNanos
argument_list|(
name|t
operator|.
name|getNanos
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|t2
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
literal|"Converting field "
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
literal|" from UTC to timezone: "
argument_list|)
expr_stmt|;
if|if
condition|(
name|children
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|children
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"from_utc_timestamp"
return|;
block|}
specifier|protected
name|boolean
name|invert
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

