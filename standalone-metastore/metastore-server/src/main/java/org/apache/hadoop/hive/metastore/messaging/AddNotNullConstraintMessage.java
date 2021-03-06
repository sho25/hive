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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|metastore
operator|.
name|api
operator|.
name|SQLNotNullConstraint
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|AddNotNullConstraintMessage
extends|extends
name|EventMessage
block|{
specifier|protected
name|AddNotNullConstraintMessage
parameter_list|()
block|{
name|super
argument_list|(
name|EventType
operator|.
name|ADD_NOTNULLCONSTRAINT
argument_list|)
expr_stmt|;
block|}
comment|/**    * Getter for list of not null constraints.    * @return List of SQLNotNullConstraint    */
specifier|public
specifier|abstract
name|List
argument_list|<
name|SQLNotNullConstraint
argument_list|>
name|getNotNullConstraints
parameter_list|()
throws|throws
name|Exception
function_decl|;
block|}
end_class

end_unit

