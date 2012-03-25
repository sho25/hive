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
name|metastore
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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|api
operator|.
name|Database
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  *  * TestRemoteHiveMetaStoreIpAddress.  *  * Test which checks that the remote Hive metastore stores the proper IP address using  * IpAddressListener  */
end_comment

begin_class
specifier|public
class|class
name|TestRemoteHiveMetaStoreIpAddress
extends|extends
name|TestCase
block|{
specifier|protected
specifier|static
specifier|final
name|String
name|METASTORE_PORT
init|=
literal|"39083"
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|isServerStarted
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|static
name|HiveMetaStoreClient
name|msc
decl_stmt|;
specifier|private
specifier|static
class|class
name|RunMS
implements|implements
name|Runnable
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|System
operator|.
name|setProperty
argument_list|(
name|ConfVars
operator|.
name|METASTORE_EVENT_LISTENERS
operator|.
name|varname
argument_list|,
name|IpAddressListener
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|HiveMetaStore
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
name|METASTORE_PORT
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|(
name|System
operator|.
name|err
argument_list|)
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
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
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|isServerStarted
condition|)
block|{
name|assertNotNull
argument_list|(
literal|"Unable to connect to the MetaStore server"
argument_list|,
name|msc
argument_list|)
expr_stmt|;
return|return;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Starting MetaStore Server on port "
operator|+
name|METASTORE_PORT
argument_list|)
expr_stmt|;
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
operator|new
name|RunMS
argument_list|()
argument_list|)
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|isServerStarted
operator|=
literal|true
expr_stmt|;
comment|// Wait a little bit for the metastore to start. Should probably have
comment|// a better way of detecting if the metastore has started?
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
comment|// This is default case with setugi off for both client and server
name|createClient
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testIpAddress
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|Database
name|db
init|=
operator|new
name|Database
argument_list|()
decl_stmt|;
name|db
operator|.
name|setName
argument_list|(
literal|"testIpAddressIp"
argument_list|)
expr_stmt|;
name|msc
operator|.
name|createDatabase
argument_list|(
name|db
argument_list|)
expr_stmt|;
name|msc
operator|.
name|dropDatabase
argument_list|(
name|db
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"testIpAddress() failed."
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|protected
name|void
name|createClient
parameter_list|()
throws|throws
name|Exception
block|{
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|METASTORE_MODE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREURIS
argument_list|,
literal|"thrift://localhost:"
operator|+
name|METASTORE_PORT
argument_list|)
expr_stmt|;
name|msc
operator|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

