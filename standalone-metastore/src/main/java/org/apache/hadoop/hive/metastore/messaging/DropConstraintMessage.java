begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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

begin_class
specifier|public
specifier|abstract
class|class
name|DropConstraintMessage
extends|extends
name|EventMessage
block|{
specifier|protected
name|DropConstraintMessage
parameter_list|()
block|{
name|super
argument_list|(
name|EventType
operator|.
name|DROP_CONSTRAINT
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
name|String
name|getConstraint
parameter_list|()
function_decl|;
block|}
end_class

end_unit

