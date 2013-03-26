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
name|hcatalog
operator|.
name|templeton
operator|.
name|mock
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|Server
import|;
end_import

begin_comment
comment|/*  * Test that the server code exists.  */
end_comment

begin_class
specifier|public
class|class
name|MockServer
extends|extends
name|Server
block|{
specifier|public
name|String
name|user
decl_stmt|;
specifier|public
name|MockServer
parameter_list|()
block|{
name|execService
operator|=
operator|new
name|MockExecService
argument_list|()
expr_stmt|;
name|resetUser
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|resetUser
parameter_list|()
block|{
name|user
operator|=
name|System
operator|.
name|getenv
argument_list|(
literal|"USER"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getUser
parameter_list|()
block|{
return|return
name|user
return|;
block|}
block|}
end_class

end_unit

