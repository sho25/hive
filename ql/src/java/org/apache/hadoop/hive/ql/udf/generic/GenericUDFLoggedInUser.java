begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|session
operator|.
name|SessionState
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
name|UDFType
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

begin_comment
comment|// This function is not a deterministic function, but a runtime constant.
end_comment

begin_comment
comment|// The return value is constant within a query but can be different between queries.
end_comment

begin_class
annotation|@
name|UDFType
argument_list|(
name|deterministic
operator|=
literal|false
argument_list|,
name|runtimeConstant
operator|=
literal|true
argument_list|)
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"logged_in_user"
argument_list|,
name|value
operator|=
literal|"_FUNC_() - Returns logged in user name"
argument_list|,
name|extended
operator|=
literal|"SessionState GetUserName - the username provided at session initialization"
argument_list|)
annotation|@
name|NDV
argument_list|(
name|maxNdv
operator|=
literal|1
argument_list|)
specifier|public
class|class
name|GenericUDFLoggedInUser
extends|extends
name|GenericUDF
block|{
specifier|protected
name|Text
name|loggedInUser
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
literal|0
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentLengthException
argument_list|(
literal|"The function LOGGED_IN_USER does not take any arguments, but found "
operator|+
name|arguments
operator|.
name|length
argument_list|)
throw|;
block|}
if|if
condition|(
name|loggedInUser
operator|==
literal|null
condition|)
block|{
comment|// TODO: getUserFromAuthenticator?
name|String
name|loggedInUserName
init|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getUserName
argument_list|()
decl_stmt|;
if|if
condition|(
name|loggedInUserName
operator|!=
literal|null
condition|)
block|{
name|loggedInUser
operator|=
operator|new
name|Text
argument_list|(
name|loggedInUserName
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableStringObjectInspector
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
return|return
name|loggedInUser
return|;
block|}
specifier|public
name|Text
name|getLoggedInUser
parameter_list|()
block|{
return|return
name|loggedInUser
return|;
block|}
specifier|public
name|void
name|setLoggedInUser
parameter_list|(
name|Text
name|loggedInUser
parameter_list|)
block|{
name|this
operator|.
name|loggedInUser
operator|=
name|loggedInUser
expr_stmt|;
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
literal|"LOGGED_IN_USER()"
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|copyToNewInstance
parameter_list|(
name|Object
name|newInstance
parameter_list|)
throws|throws
name|UDFArgumentException
block|{
name|super
operator|.
name|copyToNewInstance
argument_list|(
name|newInstance
argument_list|)
expr_stmt|;
comment|// Need to preserve loggedInUser
name|GenericUDFLoggedInUser
name|other
init|=
operator|(
name|GenericUDFLoggedInUser
operator|)
name|newInstance
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|loggedInUser
operator|!=
literal|null
condition|)
block|{
name|other
operator|.
name|loggedInUser
operator|=
operator|new
name|Text
argument_list|(
name|this
operator|.
name|loggedInUser
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

