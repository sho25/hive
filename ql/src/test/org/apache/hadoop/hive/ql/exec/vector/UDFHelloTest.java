begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|exec
operator|.
name|vector
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
name|MapredContext
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
name|ql
operator|.
name|udf
operator|.
name|generic
operator|.
name|GenericUDF
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

begin_comment
comment|/**  * UDF to obfuscate input data appending "Hello "  */
end_comment

begin_class
specifier|public
class|class
name|UDFHelloTest
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|UDFHelloTest
operator|.
name|class
argument_list|)
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
specifier|static
name|String
name|greeting
init|=
literal|""
decl_stmt|;
specifier|private
name|ObjectInspectorConverters
operator|.
name|Converter
index|[]
name|converters
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Object
name|evaluate
parameter_list|(
name|DeferredObject
index|[]
name|arg0
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|arg0
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"UDFHelloTest expects exactly 1 argument"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"UDFHelloTest expects exactly 1 argument"
argument_list|)
throw|;
block|}
if|if
condition|(
name|arg0
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Empty input"
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|Text
name|data
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
name|arg0
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|dataString
init|=
name|data
operator|.
name|toString
argument_list|()
decl_stmt|;
name|result
operator|.
name|set
argument_list|(
name|greeting
operator|+
name|dataString
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
name|arg0
parameter_list|)
block|{
return|return
literal|"Hello..."
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|configure
parameter_list|(
name|MapredContext
name|context
parameter_list|)
block|{
name|greeting
operator|=
literal|"Hello "
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
name|arg0
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|converters
operator|=
operator|new
name|ObjectInspectorConverters
operator|.
name|Converter
index|[
name|arg0
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
name|arg0
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
name|arg0
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
comment|// evaluate will return a Text object
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
return|;
block|}
block|}
end_class

end_unit

