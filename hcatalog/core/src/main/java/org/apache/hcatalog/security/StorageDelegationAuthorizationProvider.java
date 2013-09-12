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
name|hcatalog
operator|.
name|security
package|;
end_package

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
name|List
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
name|Hive
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
name|HiveStorageHandler
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
name|security
operator|.
name|HiveAuthenticationProvider
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
name|security
operator|.
name|authorization
operator|.
name|HiveAuthorizationProvider
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
name|security
operator|.
name|authorization
operator|.
name|HiveAuthorizationProviderBase
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
name|security
operator|.
name|authorization
operator|.
name|Privilege
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
name|ReflectionUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|mapreduce
operator|.
name|HCatStorageHandler
import|;
end_import

begin_comment
comment|/**  * A HiveAuthorizationProvider which delegates the authorization requests to   * the underlying AuthorizationProviders obtained from the StorageHandler.  * @deprecated Use/modify {@link org.apache.hive.hcatalog.security.StorageDelegationAuthorizationProvider} instead  */
end_comment

begin_class
specifier|public
class|class
name|StorageDelegationAuthorizationProvider
extends|extends
name|HiveAuthorizationProviderBase
block|{
specifier|protected
name|HiveAuthorizationProvider
name|hdfsAuthorizer
init|=
operator|new
name|HdfsAuthorizationProvider
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|authProviders
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
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|hdfsAuthorizer
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
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
block|{
name|hive_db
operator|=
operator|new
name|HiveProxy
argument_list|(
name|Hive
operator|.
name|get
argument_list|(
operator|new
name|HiveConf
argument_list|(
name|conf
argument_list|,
name|HiveAuthorizationProvider
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setAuthenticator
parameter_list|(
name|HiveAuthenticationProvider
name|authenticator
parameter_list|)
block|{
name|super
operator|.
name|setAuthenticator
argument_list|(
name|authenticator
argument_list|)
expr_stmt|;
name|hdfsAuthorizer
operator|.
name|setAuthenticator
argument_list|(
name|authenticator
argument_list|)
expr_stmt|;
block|}
static|static
block|{
name|registerAuthProvider
argument_list|(
literal|"org.apache.hadoop.hive.hbase.HBaseStorageHandler"
argument_list|,
literal|"org.apache.hcatalog.hbase.HBaseAuthorizationProvider"
argument_list|)
expr_stmt|;
name|registerAuthProvider
argument_list|(
literal|"org.apache.hcatalog.hbase.HBaseHCatStorageHandler"
argument_list|,
literal|"org.apache.hcatalog.hbase.HBaseAuthorizationProvider"
argument_list|)
expr_stmt|;
block|}
comment|//workaround until Hive adds StorageHandler.getAuthorizationProvider(). Remove these parts afterwards
specifier|public
specifier|static
name|void
name|registerAuthProvider
parameter_list|(
name|String
name|storageHandlerClass
parameter_list|,
name|String
name|authProviderClass
parameter_list|)
block|{
name|authProviders
operator|.
name|put
argument_list|(
name|storageHandlerClass
argument_list|,
name|authProviderClass
argument_list|)
expr_stmt|;
block|}
comment|/** Returns the StorageHandler of the Table obtained from the HCatStorageHandler */
specifier|protected
name|HiveAuthorizationProvider
name|getDelegate
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|HiveException
block|{
name|HiveStorageHandler
name|handler
init|=
name|table
operator|.
name|getStorageHandler
argument_list|()
decl_stmt|;
if|if
condition|(
name|handler
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|handler
operator|instanceof
name|HCatStorageHandler
condition|)
block|{
return|return
operator|(
operator|(
name|HCatStorageHandler
operator|)
name|handler
operator|)
operator|.
name|getAuthorizationProvider
argument_list|()
return|;
block|}
else|else
block|{
name|String
name|authProviderClass
init|=
name|authProviders
operator|.
name|get
argument_list|(
name|handler
operator|.
name|getClass
argument_list|()
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|authProviderClass
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|getConf
argument_list|()
operator|.
name|getClassByName
argument_list|(
name|authProviderClass
argument_list|)
argument_list|,
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Cannot instantiate delegation AuthotizationProvider"
argument_list|)
throw|;
block|}
block|}
comment|//else we do not have anything to delegate to
throw|throw
operator|new
name|HiveException
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"Storage Handler for table:%s is not an instance "
operator|+
literal|"of HCatStorageHandler"
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
block|}
else|else
block|{
comment|//return an authorizer for HDFS
return|return
name|hdfsAuthorizer
return|;
block|}
block|}
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
comment|//global authorizations against warehouse hdfs directory
name|hdfsAuthorizer
operator|.
name|authorize
argument_list|(
name|readRequiredPriv
argument_list|,
name|writeRequiredPriv
argument_list|)
expr_stmt|;
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
comment|//db's are tied to a hdfs location
name|hdfsAuthorizer
operator|.
name|authorize
argument_list|(
name|db
argument_list|,
name|readRequiredPriv
argument_list|,
name|writeRequiredPriv
argument_list|)
expr_stmt|;
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
name|getDelegate
argument_list|(
name|table
argument_list|)
operator|.
name|authorize
argument_list|(
name|table
argument_list|,
name|readRequiredPriv
argument_list|,
name|writeRequiredPriv
argument_list|)
expr_stmt|;
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
name|getDelegate
argument_list|(
name|part
operator|.
name|getTable
argument_list|()
argument_list|)
operator|.
name|authorize
argument_list|(
name|part
argument_list|,
name|readRequiredPriv
argument_list|,
name|writeRequiredPriv
argument_list|)
expr_stmt|;
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
name|getDelegate
argument_list|(
name|table
argument_list|)
operator|.
name|authorize
argument_list|(
name|table
argument_list|,
name|part
argument_list|,
name|columns
argument_list|,
name|readRequiredPriv
argument_list|,
name|writeRequiredPriv
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

