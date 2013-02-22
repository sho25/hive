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
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Modifier
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
name|*
import|;
end_import

begin_comment
comment|/**  * common class for reflective UDFs  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractGenericUDFReflect
extends|extends
name|GenericUDF
block|{
specifier|private
name|PrimitiveObjectInspector
index|[]
name|parameterOIs
decl_stmt|;
specifier|private
name|PrimitiveTypeEntry
index|[]
name|parameterTypes
decl_stmt|;
specifier|private
name|Class
index|[]
name|parameterClasses
decl_stmt|;
specifier|private
name|Object
index|[]
name|parameterJavaValues
decl_stmt|;
name|void
name|setupParameterOIs
parameter_list|(
name|ObjectInspector
index|[]
name|arguments
parameter_list|,
name|int
name|start
parameter_list|)
throws|throws
name|UDFArgumentTypeException
block|{
name|int
name|length
init|=
name|arguments
operator|.
name|length
operator|-
name|start
decl_stmt|;
name|parameterOIs
operator|=
operator|new
name|PrimitiveObjectInspector
index|[
name|length
index|]
expr_stmt|;
name|parameterTypes
operator|=
operator|new
name|PrimitiveTypeEntry
index|[
name|length
index|]
expr_stmt|;
name|parameterClasses
operator|=
operator|new
name|Class
index|[
name|length
index|]
expr_stmt|;
name|parameterJavaValues
operator|=
operator|new
name|Object
index|[
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
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|arguments
index|[
name|i
operator|+
name|start
index|]
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|i
argument_list|,
literal|"The parameters of GenericUDFReflect(class,method[,arg1[,arg2]...])"
operator|+
literal|" must be primitive (int, double, string, etc)."
argument_list|)
throw|;
block|}
name|parameterOIs
index|[
name|i
index|]
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|arguments
index|[
name|i
operator|+
name|start
index|]
expr_stmt|;
name|parameterTypes
index|[
name|i
index|]
operator|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getTypeEntryFromPrimitiveCategory
argument_list|(
name|parameterOIs
index|[
name|i
index|]
operator|.
name|getPrimitiveCategory
argument_list|()
argument_list|)
expr_stmt|;
name|parameterClasses
index|[
name|i
index|]
operator|=
name|parameterTypes
index|[
name|i
index|]
operator|.
name|primitiveJavaType
operator|==
literal|null
condition|?
name|parameterTypes
index|[
name|i
index|]
operator|.
name|primitiveJavaClass
else|:
name|parameterTypes
index|[
name|i
index|]
operator|.
name|primitiveJavaType
expr_stmt|;
block|}
block|}
name|Object
index|[]
name|setupParameters
parameter_list|(
name|DeferredObject
index|[]
name|arguments
parameter_list|,
name|int
name|start
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Get the parameter values
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|parameterOIs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|argument
init|=
name|arguments
index|[
name|i
operator|+
name|start
index|]
operator|.
name|get
argument_list|()
decl_stmt|;
name|parameterJavaValues
index|[
name|i
index|]
operator|=
name|parameterOIs
index|[
name|i
index|]
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|argument
argument_list|)
expr_stmt|;
block|}
return|return
name|parameterJavaValues
return|;
block|}
comment|// a(string,int,int) can be matched with methods like
comment|// a(string,int,int), a(string,int,Integer), a(string,Integer,int) and a(string,Integer,Integer)
comment|// and accepts the first one clazz.getMethods() returns
name|Method
name|findMethod
parameter_list|(
name|Class
name|clazz
parameter_list|,
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|retType
parameter_list|,
name|boolean
name|memberOnly
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|Method
name|method
range|:
name|clazz
operator|.
name|getMethods
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|method
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|name
argument_list|)
operator|||
operator|(
name|retType
operator|!=
literal|null
operator|&&
operator|!
name|retType
operator|.
name|isAssignableFrom
argument_list|(
name|method
operator|.
name|getReturnType
argument_list|()
argument_list|)
operator|)
operator|||
operator|(
name|memberOnly
operator|&&
name|Modifier
operator|.
name|isStatic
argument_list|(
name|method
operator|.
name|getReturnType
argument_list|()
operator|.
name|getModifiers
argument_list|()
argument_list|)
operator|)
operator|||
name|method
operator|.
name|getParameterTypes
argument_list|()
operator|.
name|length
operator|!=
name|parameterTypes
operator|.
name|length
condition|)
block|{
continue|continue;
block|}
comment|// returns first one matches all of the params
name|boolean
name|match
init|=
literal|true
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|types
init|=
name|method
operator|.
name|getParameterTypes
argument_list|()
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
name|parameterTypes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|types
index|[
name|i
index|]
operator|!=
name|parameterTypes
index|[
name|i
index|]
operator|.
name|primitiveJavaType
operator|&&
name|types
index|[
name|i
index|]
operator|!=
name|parameterTypes
index|[
name|i
index|]
operator|.
name|primitiveJavaClass
operator|&&
operator|!
name|types
index|[
name|i
index|]
operator|.
name|isAssignableFrom
argument_list|(
name|parameterTypes
index|[
name|i
index|]
operator|.
name|primitiveJavaClass
argument_list|)
condition|)
block|{
name|match
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|match
condition|)
block|{
return|return
name|method
return|;
block|}
block|}
comment|// tried all, back to original code (for error message)
return|return
name|clazz
operator|.
name|getMethod
argument_list|(
name|name
argument_list|,
name|parameterClasses
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
name|functionName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|'('
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
name|children
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|children
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|protected
specifier|abstract
name|String
name|functionName
parameter_list|()
function_decl|;
block|}
end_class

end_unit

