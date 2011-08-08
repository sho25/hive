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
name|util
operator|.
name|ArrayList
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
name|ObjectInspectorFactory
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
comment|/**  * GenericUDFMapKeys.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"map_keys"
argument_list|,
name|value
operator|=
literal|"_FUNC_(map) - "
operator|+
literal|"Returns an unordered array containing the keys of the input map."
argument_list|)
specifier|public
class|class
name|GenericUDFMapKeys
extends|extends
name|GenericUDF
block|{
specifier|private
name|MapObjectInspector
name|mapOI
decl_stmt|;
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|retArray
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
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
literal|"The function MAP_KEYS only accepts one argument."
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
operator|!
operator|(
name|arguments
index|[
literal|0
index|]
operator|instanceof
name|MapObjectInspector
operator|)
condition|)
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
literal|"\" is expected at function MAP_KEYS, "
operator|+
literal|"but \""
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
name|ObjectInspector
name|mapKeyOI
init|=
name|mapOI
operator|.
name|getMapKeyObjectInspector
argument_list|()
decl_stmt|;
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|mapKeyOI
argument_list|)
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
name|retArray
operator|.
name|clear
argument_list|()
expr_stmt|;
name|Object
name|mapObj
init|=
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
name|retArray
operator|.
name|addAll
argument_list|(
name|mapOI
operator|.
name|getMap
argument_list|(
name|mapObj
argument_list|)
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|retArray
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
name|children
operator|.
name|length
operator|==
literal|1
assert|;
return|return
literal|"map_keys("
operator|+
name|children
index|[
literal|0
index|]
operator|+
literal|")"
return|;
block|}
block|}
end_class

end_unit

