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
name|service
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
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TSocket
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|ServerSocket
import|;
end_import

begin_comment
comment|/**  * For testing HiveServer in server mode  *  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveServerSessions
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
specifier|final
name|int
name|clientNum
init|=
literal|2
decl_stmt|;
specifier|private
name|int
name|port
decl_stmt|;
specifier|private
name|Thread
name|server
decl_stmt|;
specifier|private
name|TSocket
index|[]
name|transports
init|=
operator|new
name|TSocket
index|[
name|clientNum
index|]
decl_stmt|;
specifier|private
name|HiveClient
index|[]
name|clients
init|=
operator|new
name|HiveClient
index|[
name|clientNum
index|]
decl_stmt|;
specifier|public
name|TestHiveServerSessions
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
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
name|port
operator|=
name|findFreePort
argument_list|()
expr_stmt|;
name|server
operator|=
operator|new
name|Thread
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
name|HiveServer
operator|.
name|main
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-p"
block|,
name|String
operator|.
name|valueOf
argument_list|(
name|port
argument_list|)
block|}
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|server
operator|.
name|start
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|transports
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|TSocket
name|transport
init|=
operator|new
name|TSocket
argument_list|(
literal|"localhost"
argument_list|,
name|port
argument_list|)
decl_stmt|;
name|transport
operator|.
name|open
argument_list|()
expr_stmt|;
name|transports
index|[
name|i
index|]
operator|=
name|transport
expr_stmt|;
name|clients
index|[
name|i
index|]
operator|=
operator|new
name|HiveClient
argument_list|(
operator|new
name|TBinaryProtocol
argument_list|(
name|transport
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
for|for
control|(
name|TSocket
name|socket
range|:
name|transports
control|)
block|{
if|if
condition|(
name|socket
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|socket
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignroe
block|}
block|}
block|}
if|if
condition|(
name|server
operator|!=
literal|null
condition|)
block|{
name|server
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|int
name|findFreePort
parameter_list|()
throws|throws
name|IOException
block|{
name|ServerSocket
name|socket
init|=
operator|new
name|ServerSocket
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|int
name|port
init|=
name|socket
operator|.
name|getLocalPort
argument_list|()
decl_stmt|;
name|socket
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|port
return|;
block|}
specifier|public
name|void
name|testSessionVars
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|clients
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|clients
index|[
name|i
index|]
operator|.
name|execute
argument_list|(
literal|"set hiveconf:var=value"
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|clients
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|clients
index|[
name|i
index|]
operator|.
name|execute
argument_list|(
literal|"set hiveconf:var"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hiveconf:var=value"
operator|+
name|i
argument_list|,
name|clients
index|[
name|i
index|]
operator|.
name|fetchOne
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

