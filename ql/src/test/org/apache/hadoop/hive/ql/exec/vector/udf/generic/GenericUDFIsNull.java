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
name|exec
operator|.
name|vector
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
name|ql
operator|.
name|udf
operator|.
name|generic
operator|.
name|GenericUDFUtils
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

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"myisnull"
argument_list|,
name|value
operator|=
literal|"_FUNC_(value,default_value) - Returns default value if value is null else returns value"
argument_list|,
name|extended
operator|=
literal|"Example:\n"
operator|+
literal|"> SELECT _FUNC_(null,'bla') FROM src LIMIT 1;\n"
operator|+
literal|"  bla"
argument_list|)
comment|/*  * This is a copy of GenericUDFNvl, which is built-in. We'll make it a generic  * custom UDF for test purposes.  */
specifier|public
class|class
name|GenericUDFIsNull
extends|extends
name|GenericUDF
block|{
specifier|private
specifier|transient
name|GenericUDFUtils
operator|.
name|ReturnObjectInspectorResolver
name|returnOIResolver
decl_stmt|;
specifier|private
specifier|transient
name|ObjectInspector
index|[]
name|argumentOIs
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
name|argumentOIs
operator|=
name|arguments
expr_stmt|;
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
literal|"The operator 'MYISNULL'  accepts 2 arguments."
argument_list|)
throw|;
block|}
name|returnOIResolver
operator|=
operator|new
name|GenericUDFUtils
operator|.
name|ReturnObjectInspectorResolver
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
operator|(
name|returnOIResolver
operator|.
name|update
argument_list|(
name|arguments
index|[
literal|0
index|]
argument_list|)
operator|&&
name|returnOIResolver
operator|.
name|update
argument_list|(
name|arguments
index|[
literal|1
index|]
argument_list|)
operator|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|2
argument_list|,
literal|"The first and seconds arguments of function MYISNULL should have the same type, "
operator|+
literal|"but they are different: \""
operator|+
name|arguments
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\" and \""
operator|+
name|arguments
index|[
literal|1
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|"\""
argument_list|)
throw|;
block|}
return|return
name|returnOIResolver
operator|.
name|get
argument_list|()
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
name|retVal
init|=
name|returnOIResolver
operator|.
name|convertIfNecessary
argument_list|(
name|arguments
index|[
literal|0
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|argumentOIs
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|retVal
operator|==
literal|null
condition|)
block|{
name|retVal
operator|=
name|returnOIResolver
operator|.
name|convertIfNecessary
argument_list|(
name|arguments
index|[
literal|1
index|]
operator|.
name|get
argument_list|()
argument_list|,
name|argumentOIs
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|retVal
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
literal|"if "
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
literal|" is null "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"returns"
argument_list|)
expr_stmt|;
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

