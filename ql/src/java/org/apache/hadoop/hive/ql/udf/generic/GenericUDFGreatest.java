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
name|ObjectInspector
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * GenericUDF Class for SQL construct "greatest(v1, v2, .. vn)".  *  * NOTES: 1. v1, v2 and vn should have the same TypeInfo, or an exception will  * be thrown.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"greatest"
argument_list|,
name|value
operator|=
literal|"_FUNC_(v1, v2, ...) - Returns the greatest value in a list of values"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_(2, 3, 1) FROM src LIMIT 1;\n"
operator|+
literal|"  3"
argument_list|)
specifier|public
class|class
name|GenericUDFGreatest
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|ObjectInspector
index|[]
name|argumentOIs
decl_stmt|;
specifier|private
specifier|transient
name|GenericUDFUtils
operator|.
name|ReturnObjectInspectorResolver
name|returnOIResolver
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
name|getFuncName
argument_list|()
operator|+
literal|" requires at least 2 arguments, got "
operator|+
name|arguments
operator|.
name|length
argument_list|)
throw|;
block|}
if|if
condition|(
name|arguments
index|[
literal|0
index|]
operator|.
name|getCategory
argument_list|()
operator|!=
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
name|getFuncName
argument_list|()
operator|+
literal|" only takes primitive types, got "
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
name|argumentOIs
operator|=
name|arguments
expr_stmt|;
name|returnOIResolver
operator|=
operator|new
name|GenericUDFUtils
operator|.
name|ReturnObjectInspectorResolver
argument_list|(
literal|false
argument_list|)
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
name|arguments
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|returnOIResolver
operator|.
name|update
argument_list|(
name|arguments
index|[
name|i
index|]
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|i
argument_list|,
literal|"The expressions after "
operator|+
name|getFuncName
argument_list|()
operator|+
literal|" should all have the same type: \""
operator|+
name|returnOIResolver
operator|.
name|get
argument_list|()
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\" is expected but \""
operator|+
name|arguments
index|[
name|i
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\" is found"
argument_list|)
throw|;
block|}
block|}
return|return
name|returnOIResolver
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"rawtypes"
block|,
literal|"unchecked"
block|}
argument_list|)
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
name|Comparable
name|maxV
init|=
literal|null
decl_stmt|;
name|int
name|maxIndex
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|arguments
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|ai
init|=
name|arguments
index|[
name|i
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|ai
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
comment|// all PRIMITIVEs are Comparable
name|Comparable
name|v
init|=
operator|(
name|Comparable
operator|)
name|ai
decl_stmt|;
if|if
condition|(
name|maxV
operator|==
literal|null
condition|)
block|{
name|maxV
operator|=
name|v
expr_stmt|;
name|maxIndex
operator|=
name|i
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
operator|(
name|isGreatest
argument_list|()
condition|?
literal|1
else|:
operator|-
literal|1
operator|)
operator|*
name|v
operator|.
name|compareTo
argument_list|(
name|maxV
argument_list|)
operator|>
literal|0
condition|)
block|{
name|maxV
operator|=
name|v
expr_stmt|;
name|maxIndex
operator|=
name|i
expr_stmt|;
block|}
block|}
if|if
condition|(
name|maxV
operator|!=
literal|null
condition|)
block|{
return|return
name|returnOIResolver
operator|.
name|convertIfNecessary
argument_list|(
name|maxV
argument_list|,
name|argumentOIs
index|[
name|maxIndex
index|]
argument_list|)
return|;
block|}
return|return
literal|null
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
argument_list|,
literal|","
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
literal|"greatest"
return|;
block|}
specifier|protected
name|boolean
name|isGreatest
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

