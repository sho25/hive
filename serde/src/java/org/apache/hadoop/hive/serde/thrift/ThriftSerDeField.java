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
name|serde
operator|.
name|thrift
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
name|serde
operator|.
name|*
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
name|*
import|;
end_import

begin_comment
comment|/**  * Thrift implementation of SerDeField  * Uses Reflection for the most part. Uses __isset interface exposed by Thrift  * to determine whether a field is set  *  */
end_comment

begin_class
specifier|public
class|class
name|ThriftSerDeField
extends|extends
name|ReflectionSerDeField
block|{
specifier|private
name|Class
name|issetClass
decl_stmt|;
specifier|private
name|Field
name|issetField
decl_stmt|;
specifier|private
name|Field
name|fieldIssetField
decl_stmt|;
specifier|public
name|ThriftSerDeField
parameter_list|(
name|String
name|className
parameter_list|,
name|String
name|fieldName
parameter_list|)
throws|throws
name|SerDeException
block|{
name|super
argument_list|(
name|className
argument_list|,
name|fieldName
argument_list|)
expr_stmt|;
try|try
block|{
name|issetClass
operator|=
name|Class
operator|.
name|forName
argument_list|(
name|className
operator|+
literal|"$Isset"
argument_list|)
expr_stmt|;
name|fieldIssetField
operator|=
name|issetClass
operator|.
name|getDeclaredField
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|issetField
operator|=
name|_parentClass
operator|.
name|getDeclaredField
argument_list|(
literal|"__isset"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|(
operator|new
name|SerDeException
argument_list|(
literal|"Not a Thrift Class?"
argument_list|,
name|e
argument_list|)
operator|)
throw|;
block|}
block|}
specifier|public
name|Object
name|get
parameter_list|(
name|Object
name|obj
parameter_list|)
throws|throws
name|SerDeException
block|{
try|try
block|{
if|if
condition|(
name|fieldIssetField
operator|.
name|getBoolean
argument_list|(
name|issetField
operator|.
name|get
argument_list|(
name|obj
argument_list|)
argument_list|)
condition|)
block|{
return|return
name|_field
operator|.
name|get
argument_list|(
name|obj
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SerDeException
argument_list|(
literal|"Illegal object or access error"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"ThriftSerDeField::ReflectionSerDeField["
operator|+
name|super
operator|.
name|toString
argument_list|()
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

