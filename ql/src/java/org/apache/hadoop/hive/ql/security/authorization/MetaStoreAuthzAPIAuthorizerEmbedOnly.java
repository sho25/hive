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
name|security
operator|.
name|authorization
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|IHMSHandler
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
name|hive
operator|.
name|ql
operator|.
name|metadata
operator|.
name|AuthorizationException
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
name|metadata
operator|.
name|HiveException
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
name|metadata
operator|.
name|Partition
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
name|metadata
operator|.
name|Table
import|;
end_import

begin_comment
comment|/**  * If this authorizer is used, it allows authorization api to be invoked only in embedded  * metastore mode.  */
end_comment

begin_class
specifier|public
class|class
name|MetaStoreAuthzAPIAuthorizerEmbedOnly
extends|extends
name|HiveAuthorizationProviderBase
implements|implements
name|HiveMetastoreAuthorizationProvider
block|{
specifier|public
specifier|static
specifier|final
name|String
name|errMsg
init|=
literal|"Metastore Authorization api invocation for "
operator|+
literal|"remote metastore is disabled in this configuration. Run commands via jdbc/odbc clients "
operator|+
literal|"via HiveServer2 that is using embedded metastore."
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|authorize
parameter_list|(
name|Privilege
index|[]
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{
comment|// not authorized by this implementation, ie operation is allowed by it
block|}
annotation|@
name|Override
specifier|public
name|void
name|authorize
parameter_list|(
name|Database
name|db
parameter_list|,
name|Privilege
index|[]
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{
comment|// not authorized by this implementation, ie operation is allowed by it
block|}
annotation|@
name|Override
specifier|public
name|void
name|authorize
parameter_list|(
name|Table
name|table
parameter_list|,
name|Privilege
index|[]
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{
comment|// not authorized by this implementation, ie operation is allowed by it
block|}
annotation|@
name|Override
specifier|public
name|void
name|authorize
parameter_list|(
name|Partition
name|part
parameter_list|,
name|Privilege
index|[]
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{
comment|// not authorized by this implementation, ie operation is allowed by it
block|}
annotation|@
name|Override
specifier|public
name|void
name|authorize
parameter_list|(
name|Table
name|table
parameter_list|,
name|Partition
name|part
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|columns
parameter_list|,
name|Privilege
index|[]
name|readRequiredPriv
parameter_list|,
name|Privilege
index|[]
name|writeRequiredPriv
parameter_list|)
throws|throws
name|HiveException
throws|,
name|AuthorizationException
block|{
comment|// not authorized by this implementation, ie operation is allowed by it
block|}
annotation|@
name|Override
specifier|public
name|void
name|setMetaStoreHandler
parameter_list|(
name|IHMSHandler
name|handler
parameter_list|)
block|{
comment|// no-op - HMSHander not needed by this impl
block|}
annotation|@
name|Override
specifier|public
name|void
name|authorizeAuthorizationApiInvocation
parameter_list|()
throws|throws
name|AuthorizationException
block|{
if|if
condition|(
name|HiveMetaStore
operator|.
name|isMetaStoreRemote
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|AuthorizationException
argument_list|(
name|errMsg
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

