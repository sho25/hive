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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|session
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FilenameFilter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

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
name|hadoop
operator|.
name|hive
operator|.
name|conf
operator|.
name|HiveConf
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|cli
operator|.
name|SessionHandle
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
name|cli
operator|.
name|thrift
operator|.
name|EmbeddedThriftBinaryCLIService
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
name|cli
operator|.
name|thrift
operator|.
name|ThriftCLIServiceClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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

begin_class
specifier|public
class|class
name|TestSessionCleanup
extends|extends
name|TestCase
block|{
annotation|@
name|Test
comment|// This is to test session temporary files are cleaned up after HIVE-11768
specifier|public
name|void
name|testTempSessionFileCleanup
parameter_list|()
throws|throws
name|Exception
block|{
name|EmbeddedThriftBinaryCLIService
name|service
init|=
operator|new
name|EmbeddedThriftBinaryCLIService
argument_list|()
decl_stmt|;
name|service
operator|.
name|init
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|ThriftCLIServiceClient
name|client
init|=
operator|new
name|ThriftCLIServiceClient
argument_list|(
name|service
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|existingPipeoutFiles
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|getPipeoutFiles
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|SessionHandle
name|sessionHandle
init|=
name|client
operator|.
name|openSession
argument_list|(
literal|"user1"
argument_list|,
literal|"foobar"
argument_list|,
name|Collections
operator|.
expr|<
name|String
argument_list|,
name|String
operator|>
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
literal|"set a=b"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|File
name|operationLogRootDir
init|=
operator|new
name|File
argument_list|(
operator|new
name|HiveConf
argument_list|()
operator|.
name|getVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_LOGGING_OPERATION_LOG_LOCATION
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotEquals
argument_list|(
name|operationLogRootDir
operator|.
name|list
argument_list|()
operator|.
name|length
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|client
operator|.
name|closeSession
argument_list|(
name|sessionHandle
argument_list|)
expr_stmt|;
comment|// Check if session files are removed
name|Assert
operator|.
name|assertEquals
argument_list|(
name|operationLogRootDir
operator|.
name|list
argument_list|()
operator|.
name|length
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// Check if the pipeout files are removed
name|Set
argument_list|<
name|String
argument_list|>
name|finalPipeoutFiles
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|getPipeoutFiles
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|finalPipeoutFiles
operator|.
name|removeAll
argument_list|(
name|existingPipeoutFiles
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|finalPipeoutFiles
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
index|[]
name|getPipeoutFiles
parameter_list|()
block|{
name|File
name|localScratchDir
init|=
operator|new
name|File
argument_list|(
operator|new
name|HiveConf
argument_list|()
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|LOCALSCRATCHDIR
argument_list|)
argument_list|)
decl_stmt|;
name|String
index|[]
name|pipeoutFiles
init|=
name|localScratchDir
operator|.
name|list
argument_list|(
operator|new
name|FilenameFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|File
name|dir
parameter_list|,
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|name
operator|.
name|endsWith
argument_list|(
literal|"pipeout"
argument_list|)
condition|)
return|return
literal|true
return|;
return|return
literal|false
return|;
block|}
block|}
argument_list|)
decl_stmt|;
return|return
name|pipeoutFiles
return|;
block|}
block|}
end_class

end_unit

