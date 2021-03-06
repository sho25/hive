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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|Socket
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
name|HiveMetaStore
operator|.
name|HMSHandler
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
name|ThriftHiveMetastore
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
name|ThriftHiveMetastore
operator|.
name|Iface
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

begin_comment
comment|/**  * TSetIpAddressProcessor passes the IP address of the Thrift client to the HMSHandler.  */
end_comment

begin_class
specifier|public
class|class
name|TSetIpAddressProcessor
parameter_list|<
name|I
extends|extends
name|Iface
parameter_list|>
extends|extends
name|ThriftHiveMetastore
operator|.
name|Processor
argument_list|<
name|Iface
argument_list|>
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|TSetIpAddressProcessor
parameter_list|(
name|I
name|iface
parameter_list|)
throws|throws
name|SecurityException
throws|,
name|NoSuchFieldException
throws|,
name|IllegalArgumentException
throws|,
name|IllegalAccessException
throws|,
name|NoSuchMethodException
throws|,
name|InvocationTargetException
block|{
name|super
argument_list|(
name|iface
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|process
parameter_list|(
specifier|final
name|TProtocol
name|in
parameter_list|,
specifier|final
name|TProtocol
name|out
parameter_list|)
throws|throws
name|TException
block|{
name|setIpAddress
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|process
argument_list|(
name|in
argument_list|,
name|out
argument_list|)
return|;
block|}
specifier|protected
name|void
name|setIpAddress
parameter_list|(
specifier|final
name|TProtocol
name|in
parameter_list|)
block|{
name|TTransport
name|transport
init|=
name|in
operator|.
name|getTransport
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|transport
operator|instanceof
name|TSocket
operator|)
condition|)
block|{
return|return;
block|}
name|setIpAddress
argument_list|(
operator|(
operator|(
name|TSocket
operator|)
name|transport
operator|)
operator|.
name|getSocket
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|setIpAddress
parameter_list|(
specifier|final
name|Socket
name|inSocket
parameter_list|)
block|{
name|HMSHandler
operator|.
name|setThreadLocalIpAddress
argument_list|(
name|inSocket
operator|.
name|getInetAddress
argument_list|()
operator|.
name|getHostAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

