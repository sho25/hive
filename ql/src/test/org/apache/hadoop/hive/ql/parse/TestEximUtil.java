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
name|parse
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

begin_comment
comment|/**  * TestEximUtil.  *  */
end_comment

begin_class
specifier|public
class|class
name|TestEximUtil
extends|extends
name|TestCase
block|{
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
block|{   }
specifier|public
name|void
name|testCheckCompatibility
parameter_list|()
throws|throws
name|SemanticException
block|{
comment|// backward/forward compatible
name|EximUtil
operator|.
name|doCheckCompatibility
argument_list|(
literal|"10.3"
argument_list|,
comment|// current code version
literal|"10.4"
argument_list|,
comment|// data's version
literal|null
comment|// data's FC version
argument_list|)
expr_stmt|;
comment|// No exceptions expected
name|EximUtil
operator|.
name|doCheckCompatibility
argument_list|(
literal|"10.4"
argument_list|,
comment|// current code version
literal|"10.4"
argument_list|,
comment|// data's version
literal|null
comment|// data's FC version
argument_list|)
expr_stmt|;
comment|// No exceptions expected
name|EximUtil
operator|.
name|doCheckCompatibility
argument_list|(
literal|"10.5"
argument_list|,
comment|// current code version
literal|"10.4"
argument_list|,
comment|// data's version
literal|null
comment|// data's FC version
argument_list|)
expr_stmt|;
comment|// No exceptions expected
comment|// not backward compatible
try|try
block|{
name|EximUtil
operator|.
name|doCheckCompatibility
argument_list|(
literal|"11.0"
argument_list|,
comment|// current code version
literal|"10.4"
argument_list|,
comment|// data's version
literal|null
comment|// data's FC version
argument_list|)
expr_stmt|;
comment|// No exceptions expected
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SemanticException
name|e
parameter_list|)
block|{     }
comment|// not forward compatible
try|try
block|{
name|EximUtil
operator|.
name|doCheckCompatibility
argument_list|(
literal|"9.9"
argument_list|,
comment|// current code version
literal|"10.4"
argument_list|,
comment|// data's version
literal|null
comment|// data's FC version
argument_list|)
expr_stmt|;
comment|// No exceptions expected
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SemanticException
name|e
parameter_list|)
block|{     }
comment|// forward compatible
name|EximUtil
operator|.
name|doCheckCompatibility
argument_list|(
literal|"9.9"
argument_list|,
comment|// current code version
literal|"10.4"
argument_list|,
comment|// data's version
literal|"9.9"
comment|// data's FC version
argument_list|)
expr_stmt|;
comment|// No exceptions expected
name|EximUtil
operator|.
name|doCheckCompatibility
argument_list|(
literal|"9.9"
argument_list|,
comment|// current code version
literal|"10.4"
argument_list|,
comment|// data's version
literal|"9.8"
comment|// data's FC version
argument_list|)
expr_stmt|;
comment|// No exceptions expected
name|EximUtil
operator|.
name|doCheckCompatibility
argument_list|(
literal|"9.9"
argument_list|,
comment|// current code version
literal|"10.4"
argument_list|,
comment|// data's version
literal|"8.8"
comment|// data's FC version
argument_list|)
expr_stmt|;
comment|// No exceptions expected
name|EximUtil
operator|.
name|doCheckCompatibility
argument_list|(
literal|"10.3"
argument_list|,
comment|// current code version
literal|"10.4"
argument_list|,
comment|// data's version
literal|"10.3"
comment|// data's FC version
argument_list|)
expr_stmt|;
comment|// No exceptions expected
comment|// not forward compatible
try|try
block|{
name|EximUtil
operator|.
name|doCheckCompatibility
argument_list|(
literal|"10.2"
argument_list|,
comment|// current code version
literal|"10.4"
argument_list|,
comment|// data's version
literal|"10.3"
comment|// data's FC version
argument_list|)
expr_stmt|;
comment|// No exceptions expected
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SemanticException
name|e
parameter_list|)
block|{     }
block|}
block|}
end_class

end_unit

