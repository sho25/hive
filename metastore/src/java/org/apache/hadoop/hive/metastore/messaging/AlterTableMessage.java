begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|metastore
operator|.
name|messaging
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
name|metastore
operator|.
name|api
operator|.
name|Table
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|AlterTableMessage
extends|extends
name|EventMessage
block|{
specifier|protected
name|AlterTableMessage
parameter_list|()
block|{
name|super
argument_list|(
name|EventType
operator|.
name|ALTER_TABLE
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|abstract
name|String
name|getTable
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|boolean
name|getIsTruncateOp
parameter_list|()
function_decl|;
specifier|public
specifier|abstract
name|Table
name|getTableObjBefore
parameter_list|()
throws|throws
name|Exception
function_decl|;
specifier|public
specifier|abstract
name|Table
name|getTableObjAfter
parameter_list|()
throws|throws
name|Exception
function_decl|;
annotation|@
name|Override
specifier|public
name|EventMessage
name|checkValid
parameter_list|()
block|{
if|if
condition|(
name|getTable
argument_list|()
operator|==
literal|null
condition|)
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Table name unset."
argument_list|)
throw|;
try|try
block|{
if|if
condition|(
name|getTableObjAfter
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Table object(after) not set."
argument_list|)
throw|;
block|}
if|if
condition|(
name|getTableObjBefore
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Table object(before) not set."
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|e
operator|instanceof
name|IllegalStateException
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Event not set up correctly"
argument_list|,
name|e
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|(
name|IllegalStateException
operator|)
name|e
throw|;
block|}
block|}
return|return
name|super
operator|.
name|checkValid
argument_list|()
return|;
block|}
block|}
end_class

end_unit

