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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|thrift
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
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
name|service
operator|.
name|rpc
operator|.
name|thrift
operator|.
name|TOpenSessionReq
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * Test security in classes generated by Thrift.  */
end_comment

begin_class
specifier|public
class|class
name|TestThriftCLIServiceSecurity
block|{
comment|/**    * Ensures password isn't printed to logs from TOpenSessionReq.toString().    * See maven-replacer-plugin code in service-rpc/pom.xml.    *    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testPasswordNotInLogs
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|PASSWORD
init|=
literal|"testpassword"
decl_stmt|;
name|TOpenSessionReq
name|tOpenSessionReq
init|=
operator|new
name|TOpenSessionReq
argument_list|()
decl_stmt|;
name|tOpenSessionReq
operator|.
name|setPassword
argument_list|(
name|PASSWORD
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|tOpenSessionReq
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
name|PASSWORD
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

