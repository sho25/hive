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
name|cli
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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
name|ql
operator|.
name|session
operator|.
name|SessionState
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
name|service
operator|.
name|HiveClient
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
name|TException
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
name|protocol
operator|.
name|TProtocol
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
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransport
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
name|TTransportException
import|;
end_import

begin_comment
comment|/**  * CliSessionState.  *  */
end_comment

begin_class
specifier|public
class|class
name|CliSessionState
extends|extends
name|SessionState
block|{
comment|/**    * -e option if any that the session has been invoked with.    */
specifier|public
name|String
name|execString
decl_stmt|;
comment|/**    * -f option if any that the session has been invoked with.    */
specifier|public
name|String
name|fileName
decl_stmt|;
comment|/**    * properties set from -hiveconf via cmdline.    */
specifier|public
name|Properties
name|cmdProperties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
comment|/**    * -i option if any that the session has been invoked with.    */
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|initFiles
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * host name and port number of remote Hive server    */
specifier|protected
name|String
name|host
decl_stmt|;
specifier|protected
name|int
name|port
decl_stmt|;
specifier|private
name|boolean
name|remoteMode
decl_stmt|;
specifier|private
name|TTransport
name|transport
decl_stmt|;
specifier|private
name|HiveClient
name|client
decl_stmt|;
specifier|public
name|CliSessionState
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|remoteMode
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|CliSessionState
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|remoteMode
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * Connect to Hive Server    */
specifier|public
name|void
name|connect
parameter_list|()
throws|throws
name|TTransportException
block|{
name|transport
operator|=
operator|new
name|TSocket
argument_list|(
name|host
argument_list|,
name|port
argument_list|)
expr_stmt|;
name|TProtocol
name|protocol
init|=
operator|new
name|TBinaryProtocol
argument_list|(
name|transport
argument_list|)
decl_stmt|;
name|client
operator|=
operator|new
name|HiveClient
argument_list|(
name|protocol
argument_list|)
expr_stmt|;
name|transport
operator|.
name|open
argument_list|()
expr_stmt|;
name|remoteMode
operator|=
literal|true
expr_stmt|;
block|}
specifier|public
name|void
name|setHost
parameter_list|(
name|String
name|host
parameter_list|)
block|{
name|this
operator|.
name|host
operator|=
name|host
expr_stmt|;
block|}
specifier|public
name|String
name|getHost
parameter_list|()
block|{
return|return
name|host
return|;
block|}
specifier|public
name|int
name|getPort
parameter_list|()
block|{
return|return
name|port
return|;
block|}
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
name|client
operator|.
name|clean
argument_list|()
expr_stmt|;
name|client
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|transport
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|boolean
name|isRemoteMode
parameter_list|()
block|{
return|return
name|remoteMode
return|;
block|}
specifier|public
name|HiveClient
name|getClient
parameter_list|()
block|{
return|return
name|client
return|;
block|}
block|}
end_class

end_unit

