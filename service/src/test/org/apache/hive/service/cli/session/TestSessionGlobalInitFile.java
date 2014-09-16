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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|commons
operator|.
name|io
operator|.
name|FileUtils
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
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|CLIService
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
name|ICLIService
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
name|OperationHandle
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
name|RowSet
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
name|ThriftBinaryCLIService
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
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|TestSessionGlobalInitFile
extends|extends
name|TestCase
block|{
specifier|private
name|FakeEmbeddedThriftBinaryCLIService
name|service
decl_stmt|;
specifier|private
name|ThriftCLIServiceClient
name|client
decl_stmt|;
specifier|private
name|File
name|initFile
decl_stmt|;
specifier|private
name|String
name|tmpDir
decl_stmt|;
comment|/**    * This class is almost the same as EmbeddedThriftBinaryCLIService,    * except its constructor having a HiveConf param for test usage.    */
specifier|private
class|class
name|FakeEmbeddedThriftBinaryCLIService
extends|extends
name|ThriftBinaryCLIService
block|{
specifier|public
name|FakeEmbeddedThriftBinaryCLIService
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|CLIService
argument_list|()
argument_list|)
expr_stmt|;
name|isEmbedded
operator|=
literal|true
expr_stmt|;
name|cliService
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|cliService
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
specifier|public
name|ICLIService
name|getService
parameter_list|()
block|{
return|return
name|cliService
return|;
block|}
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
comment|// create and put .hiverc sample file to default directory
name|initFile
operator|=
name|File
operator|.
name|createTempFile
argument_list|(
literal|"test"
argument_list|,
literal|"hive"
argument_list|)
expr_stmt|;
name|tmpDir
operator|=
name|initFile
operator|.
name|getParentFile
argument_list|()
operator|.
name|getAbsoluteFile
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
literal|"TestSessionGlobalInitFile"
expr_stmt|;
name|initFile
operator|.
name|delete
argument_list|()
expr_stmt|;
name|FileUtils
operator|.
name|deleteDirectory
argument_list|(
operator|new
name|File
argument_list|(
name|tmpDir
argument_list|)
argument_list|)
expr_stmt|;
name|initFile
operator|=
operator|new
name|File
argument_list|(
name|tmpDir
operator|+
name|File
operator|.
name|separator
operator|+
name|SessionManager
operator|.
name|HIVERCFILE
argument_list|)
expr_stmt|;
name|initFile
operator|.
name|getParentFile
argument_list|()
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|initFile
operator|.
name|createNewFile
argument_list|()
expr_stmt|;
name|String
index|[]
name|fileContent
init|=
operator|new
name|String
index|[]
block|{
literal|"-- global init hive file for test"
block|,
literal|"set a=1;"
block|,
literal|"set hiveconf:b=1;"
block|,
literal|"set hivevar:c=1;"
block|,
literal|"set d\\"
block|,
literal|"      =1;"
block|,
literal|"add jar "
operator|+
name|initFile
operator|.
name|getAbsolutePath
argument_list|()
block|}
decl_stmt|;
name|FileUtils
operator|.
name|writeLines
argument_list|(
name|initFile
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|fileContent
argument_list|)
argument_list|)
expr_stmt|;
comment|// set up service and client
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_GLOBAL_INIT_FILE_LOCATION
argument_list|,
name|initFile
operator|.
name|getParentFile
argument_list|()
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|service
operator|=
operator|new
name|FakeEmbeddedThriftBinaryCLIService
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|service
operator|.
name|init
argument_list|(
operator|new
name|HiveConf
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|=
operator|new
name|ThriftCLIServiceClient
argument_list|(
name|service
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
comment|// restore
name|FileUtils
operator|.
name|deleteDirectory
argument_list|(
operator|new
name|File
argument_list|(
name|tmpDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSessionGlobalInitFile
parameter_list|()
throws|throws
name|Exception
block|{
comment|/**      * create session, and fetch the property set in global init file. Test if      * the global init file .hiverc is loaded correctly by checking the expected      * setting property.      */
name|SessionHandle
name|sessionHandle
init|=
name|client
operator|.
name|openSession
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|verifyInitProperty
argument_list|(
literal|"a"
argument_list|,
literal|"1"
argument_list|,
name|sessionHandle
argument_list|)
expr_stmt|;
name|verifyInitProperty
argument_list|(
literal|"b"
argument_list|,
literal|"1"
argument_list|,
name|sessionHandle
argument_list|)
expr_stmt|;
name|verifyInitProperty
argument_list|(
literal|"c"
argument_list|,
literal|"1"
argument_list|,
name|sessionHandle
argument_list|)
expr_stmt|;
name|verifyInitProperty
argument_list|(
literal|"hivevar:c"
argument_list|,
literal|"1"
argument_list|,
name|sessionHandle
argument_list|)
expr_stmt|;
name|verifyInitProperty
argument_list|(
literal|"d"
argument_list|,
literal|"1"
argument_list|,
name|sessionHandle
argument_list|)
expr_stmt|;
comment|/**      * TODO: client.executeStatement do not support listing resources command      * (beeline> list jar)      */
comment|// Assert.assertEquals("expected uri", api.getAddedResource("jar"));
name|client
operator|.
name|closeSession
argument_list|(
name|sessionHandle
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSessionGlobalInitFileWithUser
parameter_list|()
throws|throws
name|Exception
block|{
comment|//Test when the session is opened by a user. (HiveSessionImplwithUGI)
name|SessionHandle
name|sessionHandle
init|=
name|client
operator|.
name|openSession
argument_list|(
literal|"hive"
argument_list|,
literal|"password"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|verifyInitProperty
argument_list|(
literal|"a"
argument_list|,
literal|"1"
argument_list|,
name|sessionHandle
argument_list|)
expr_stmt|;
name|client
operator|.
name|closeSession
argument_list|(
name|sessionHandle
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSessionGlobalInitFileAndConfOverlay
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Test if the user session specific conf overlaying global init conf.
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|confOverlay
operator|.
name|put
argument_list|(
literal|"a"
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
name|confOverlay
operator|.
name|put
argument_list|(
literal|"set:hiveconf:b"
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
name|confOverlay
operator|.
name|put
argument_list|(
literal|"set:hivevar:c"
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
name|SessionHandle
name|sessionHandle
init|=
name|client
operator|.
name|openSession
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|confOverlay
argument_list|)
decl_stmt|;
name|verifyInitProperty
argument_list|(
literal|"a"
argument_list|,
literal|"2"
argument_list|,
name|sessionHandle
argument_list|)
expr_stmt|;
name|verifyInitProperty
argument_list|(
literal|"b"
argument_list|,
literal|"2"
argument_list|,
name|sessionHandle
argument_list|)
expr_stmt|;
name|verifyInitProperty
argument_list|(
literal|"c"
argument_list|,
literal|"2"
argument_list|,
name|sessionHandle
argument_list|)
expr_stmt|;
name|client
operator|.
name|closeSession
argument_list|(
name|sessionHandle
argument_list|)
expr_stmt|;
name|sessionHandle
operator|=
name|client
operator|.
name|openSession
argument_list|(
literal|"hive"
argument_list|,
literal|"password"
argument_list|,
name|confOverlay
argument_list|)
expr_stmt|;
name|verifyInitProperty
argument_list|(
literal|"a"
argument_list|,
literal|"2"
argument_list|,
name|sessionHandle
argument_list|)
expr_stmt|;
name|client
operator|.
name|closeSession
argument_list|(
name|sessionHandle
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|verifyInitProperty
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|,
name|SessionHandle
name|sessionHandle
parameter_list|)
throws|throws
name|Exception
block|{
name|OperationHandle
name|operationHandle
init|=
name|client
operator|.
name|executeStatement
argument_list|(
name|sessionHandle
argument_list|,
literal|"set "
operator|+
name|key
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|RowSet
name|rowSet
init|=
name|client
operator|.
name|fetchResults
argument_list|(
name|operationHandle
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rowSet
operator|.
name|numRows
argument_list|()
argument_list|)
expr_stmt|;
comment|// we know rowSet has only one element
name|Assert
operator|.
name|assertEquals
argument_list|(
name|key
operator|+
literal|"="
operator|+
name|value
argument_list|,
name|rowSet
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

