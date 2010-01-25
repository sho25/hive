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
name|ListObjectInspector
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
name|MapObjectInspector
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
name|ObjectInspector
operator|.
name|Category
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
name|io
operator|.
name|IntWritable
import|;
end_import

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"index"
argument_list|,
name|value
operator|=
literal|"_FUNC_(a, n) - Returns the n-th element of a "
argument_list|)
specifier|public
class|class
name|GenericUDFIndex
extends|extends
name|GenericUDF
block|{
specifier|private
name|MapObjectInspector
name|mapOI
decl_stmt|;
specifier|private
name|boolean
name|mapKeyPreferWritable
decl_stmt|;
specifier|private
name|ListObjectInspector
name|listOI
decl_stmt|;
specifier|private
name|PrimitiveObjectInspector
name|indexOI
decl_stmt|;
specifier|private
name|ObjectInspector
name|returnOI
decl_stmt|;
specifier|private
specifier|final
name|IntWritable
name|result
init|=
operator|new
name|IntWritable
argument_list|(
operator|-
literal|1
argument_list|)
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
literal|2
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"The function INDEX accepts exactly 2 arguments."
argument_list|)
throw|;
block|}
if|if
condition|(
name|arguments
index|[
literal|0
index|]
operator|instanceof
name|MapObjectInspector
condition|)
block|{
comment|// index into a map
name|mapOI
operator|=
operator|(
name|MapObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
expr_stmt|;
name|listOI
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|arguments
index|[
literal|0
index|]
operator|instanceof
name|ListObjectInspector
condition|)
block|{
comment|// index into a list
name|listOI
operator|=
operator|(
name|ListObjectInspector
operator|)
name|arguments
index|[
literal|0
index|]
expr_stmt|;
name|mapOI
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"\""
operator|+
name|Category
operator|.
name|MAP
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"\" or \""
operator|+
name|Category
operator|.
name|LIST
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|+
literal|"\" is expected at function INDEX, but \""
operator|+
name|arguments
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\" is found"
argument_list|)
throw|;
block|}
comment|// index has to be a primitive
if|if
condition|(
name|arguments
index|[
literal|1
index|]
operator|instanceof
name|PrimitiveObjectInspector
condition|)
block|{
name|indexOI
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
else|else
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
literal|"Primitive Type is expected but "
operator|+
name|arguments
index|[
literal|1
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\" is found"
argument_list|)
throw|;
block|}
if|if
condition|(
name|mapOI
operator|!=
literal|null
condition|)
block|{
name|returnOI
operator|=
name|mapOI
operator|.
name|getMapValueObjectInspector
argument_list|()
expr_stmt|;
name|ObjectInspector
name|keyOI
init|=
name|mapOI
operator|.
name|getMapKeyObjectInspector
argument_list|()
decl_stmt|;
name|mapKeyPreferWritable
operator|=
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|keyOI
operator|)
operator|.
name|preferWritable
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|returnOI
operator|=
name|listOI
operator|.
name|getListElementObjectInspector
argument_list|()
expr_stmt|;
block|}
return|return
name|returnOI
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
assert|assert
operator|(
name|arguments
operator|.
name|length
operator|==
literal|2
operator|)
assert|;
name|Object
name|main
init|=
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
name|Object
name|index
init|=
name|arguments
index|[
literal|1
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|mapOI
operator|!=
literal|null
condition|)
block|{
name|Object
name|indexObject
decl_stmt|;
if|if
condition|(
name|mapKeyPreferWritable
condition|)
block|{
name|indexObject
operator|=
name|indexOI
operator|.
name|getPrimitiveWritableObject
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|indexObject
operator|=
name|indexOI
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
return|return
name|mapOI
operator|.
name|getMapValueElement
argument_list|(
name|main
argument_list|,
name|indexObject
argument_list|)
return|;
block|}
else|else
block|{
assert|assert
operator|(
name|listOI
operator|!=
literal|null
operator|)
assert|;
name|int
name|intIndex
init|=
literal|0
decl_stmt|;
try|try
block|{
name|intIndex
operator|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getInt
argument_list|(
name|index
argument_list|,
name|indexOI
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
comment|// If index is null, we should return null.
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
comment|// If index is not a number, we should return null.
return|return
literal|null
return|;
block|}
return|return
name|listOI
operator|.
name|getListElement
argument_list|(
name|main
argument_list|,
name|intIndex
argument_list|)
return|;
block|}
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
literal|2
operator|)
assert|;
return|return
name|children
index|[
literal|0
index|]
operator|+
literal|"["
operator|+
name|children
index|[
literal|1
index|]
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

