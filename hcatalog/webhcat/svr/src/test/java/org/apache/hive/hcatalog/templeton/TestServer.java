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
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
package|;
end_package

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
operator|.
name|mock
operator|.
name|MockServer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/*  * Test that the server code exists, and responds to basic requests.  */
end_comment

begin_class
specifier|public
class|class
name|TestServer
extends|extends
name|TestCase
block|{
name|MockServer
name|server
decl_stmt|;
specifier|public
name|void
name|setUp
parameter_list|()
block|{
operator|new
name|Main
argument_list|(
literal|null
argument_list|)
expr_stmt|;
comment|// Initialize the config
name|server
operator|=
operator|new
name|MockServer
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testServer
parameter_list|()
block|{
name|assertNotNull
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testStatus
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|server
operator|.
name|status
argument_list|()
operator|.
name|get
argument_list|(
literal|"status"
argument_list|)
argument_list|,
literal|"ok"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testVersions
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|server
operator|.
name|version
argument_list|()
operator|.
name|get
argument_list|(
literal|"version"
argument_list|)
argument_list|,
literal|"v1"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testFormats
parameter_list|()
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|server
operator|.
name|requestFormats
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|List
operator|)
name|server
operator|.
name|requestFormats
argument_list|()
operator|.
name|get
argument_list|(
literal|"responseTypes"
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|"application/json"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

