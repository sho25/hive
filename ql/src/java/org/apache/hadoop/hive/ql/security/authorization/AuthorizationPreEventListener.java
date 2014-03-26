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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|MetaStorePreEventListener
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
name|MetaStoreUtils
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
name|TableType
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
name|metastore
operator|.
name|api
operator|.
name|InvalidOperationException
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
name|MetaException
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
name|NoSuchObjectException
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
name|events
operator|.
name|PreAddPartitionEvent
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
name|events
operator|.
name|PreAlterPartitionEvent
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
name|events
operator|.
name|PreAlterTableEvent
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
name|events
operator|.
name|PreCreateDatabaseEvent
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
name|events
operator|.
name|PreCreateTableEvent
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
name|events
operator|.
name|PreDropDatabaseEvent
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
name|events
operator|.
name|PreDropPartitionEvent
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
name|events
operator|.
name|PreDropTableEvent
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
name|events
operator|.
name|PreEventContext
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
name|HiveUtils
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
name|plan
operator|.
name|HiveOperation
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
name|HiveMetastoreAuthenticationProvider
import|;
end_import

begin_comment
comment|/**  * AuthorizationPreEventListener : A MetaStorePreEventListener that  * performs authorization/authentication checks on the metastore-side.  *  * Note that this can only perform authorization checks on defined  * metastore PreEventContexts, such as the adding/dropping and altering  * of databases, tables and partitions.  */
end_comment

begin_class
specifier|public
class|class
name|AuthorizationPreEventListener
extends|extends
name|MetaStorePreEventListener
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|AuthorizationPreEventListener
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ThreadLocal
argument_list|<
name|Configuration
argument_list|>
name|tConfig
init|=
operator|new
name|ThreadLocal
argument_list|<
name|Configuration
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|Configuration
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|HiveConf
argument_list|(
name|AuthorizationPreEventListener
operator|.
name|class
argument_list|)
return|;
block|}
block|}
decl_stmt|;
specifier|private
specifier|final
name|ThreadLocal
argument_list|<
name|HiveMetastoreAuthenticationProvider
argument_list|>
name|tAuthenticator
init|=
operator|new
name|ThreadLocal
argument_list|<
name|HiveMetastoreAuthenticationProvider
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|HiveMetastoreAuthenticationProvider
name|initialValue
parameter_list|()
block|{
try|try
block|{
return|return
operator|(
name|HiveMetastoreAuthenticationProvider
operator|)
name|HiveUtils
operator|.
name|getAuthenticator
argument_list|(
name|tConfig
operator|.
name|get
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METASTORE_AUTHENTICATOR_MANAGER
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|he
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Authentication provider instantiation failure"
argument_list|,
name|he
argument_list|)
throw|;
block|}
block|}
block|}
decl_stmt|;
specifier|private
specifier|final
name|ThreadLocal
argument_list|<
name|HiveMetastoreAuthorizationProvider
argument_list|>
name|tAuthorizer
init|=
operator|new
name|ThreadLocal
argument_list|<
name|HiveMetastoreAuthorizationProvider
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|HiveMetastoreAuthorizationProvider
name|initialValue
parameter_list|()
block|{
try|try
block|{
return|return
operator|(
name|HiveMetastoreAuthorizationProvider
operator|)
name|HiveUtils
operator|.
name|getAuthorizeProviderManager
argument_list|(
name|tConfig
operator|.
name|get
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METASTORE_AUTHORIZATION_MANAGER
argument_list|,
name|tAuthenticator
operator|.
name|get
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|he
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Authorization provider instantiation failure"
argument_list|,
name|he
argument_list|)
throw|;
block|}
block|}
block|}
decl_stmt|;
specifier|private
specifier|final
name|ThreadLocal
argument_list|<
name|Boolean
argument_list|>
name|tConfigSetOnAuths
init|=
operator|new
name|ThreadLocal
argument_list|<
name|Boolean
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|Boolean
name|initialValue
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
decl_stmt|;
specifier|public
name|AuthorizationPreEventListener
parameter_list|(
name|Configuration
name|config
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
argument_list|(
name|config
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onEvent
parameter_list|(
name|PreEventContext
name|context
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
throws|,
name|InvalidOperationException
block|{
if|if
condition|(
operator|!
name|tConfigSetOnAuths
operator|.
name|get
argument_list|()
condition|)
block|{
comment|// The reason we do this guard is because when we do not have a good way of initializing
comment|// the config to the handler's thread local config until this call, so we do it then.
comment|// Once done, though, we need not repeat this linking, we simply call setMetaStoreHandler
comment|// and let the AuthorizationProvider and AuthenticationProvider do what they want.
name|tConfig
operator|.
name|set
argument_list|(
name|context
operator|.
name|getHandler
argument_list|()
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
comment|// Warning note : HMSHandler.getHiveConf() is not thread-unique, .getConf() is.
name|tAuthenticator
operator|.
name|get
argument_list|()
operator|.
name|setConf
argument_list|(
name|tConfig
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|tAuthorizer
operator|.
name|get
argument_list|()
operator|.
name|setConf
argument_list|(
name|tConfig
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|tConfigSetOnAuths
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// set so we don't repeat this initialization
block|}
name|tAuthenticator
operator|.
name|get
argument_list|()
operator|.
name|setMetaStoreHandler
argument_list|(
name|context
operator|.
name|getHandler
argument_list|()
argument_list|)
expr_stmt|;
name|tAuthorizer
operator|.
name|get
argument_list|()
operator|.
name|setMetaStoreHandler
argument_list|(
name|context
operator|.
name|getHandler
argument_list|()
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|context
operator|.
name|getEventType
argument_list|()
condition|)
block|{
case|case
name|CREATE_TABLE
case|:
name|authorizeCreateTable
argument_list|(
operator|(
name|PreCreateTableEvent
operator|)
name|context
argument_list|)
expr_stmt|;
break|break;
case|case
name|DROP_TABLE
case|:
name|authorizeDropTable
argument_list|(
operator|(
name|PreDropTableEvent
operator|)
name|context
argument_list|)
expr_stmt|;
break|break;
case|case
name|ALTER_TABLE
case|:
name|authorizeAlterTable
argument_list|(
operator|(
name|PreAlterTableEvent
operator|)
name|context
argument_list|)
expr_stmt|;
break|break;
case|case
name|ADD_PARTITION
case|:
name|authorizeAddPartition
argument_list|(
operator|(
name|PreAddPartitionEvent
operator|)
name|context
argument_list|)
expr_stmt|;
break|break;
case|case
name|DROP_PARTITION
case|:
name|authorizeDropPartition
argument_list|(
operator|(
name|PreDropPartitionEvent
operator|)
name|context
argument_list|)
expr_stmt|;
break|break;
case|case
name|ALTER_PARTITION
case|:
name|authorizeAlterPartition
argument_list|(
operator|(
name|PreAlterPartitionEvent
operator|)
name|context
argument_list|)
expr_stmt|;
break|break;
case|case
name|CREATE_DATABASE
case|:
name|authorizeCreateDatabase
argument_list|(
operator|(
name|PreCreateDatabaseEvent
operator|)
name|context
argument_list|)
expr_stmt|;
break|break;
case|case
name|DROP_DATABASE
case|:
name|authorizeDropDatabase
argument_list|(
operator|(
name|PreDropDatabaseEvent
operator|)
name|context
argument_list|)
expr_stmt|;
break|break;
case|case
name|LOAD_PARTITION_DONE
case|:
comment|// noop for now
break|break;
default|default:
break|break;
block|}
block|}
specifier|private
name|void
name|authorizeCreateDatabase
parameter_list|(
name|PreCreateDatabaseEvent
name|context
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|MetaException
block|{
try|try
block|{
name|tAuthorizer
operator|.
name|get
argument_list|()
operator|.
name|authorize
argument_list|(
operator|new
name|Database
argument_list|(
name|context
operator|.
name|getDatabase
argument_list|()
argument_list|)
argument_list|,
name|HiveOperation
operator|.
name|CREATEDATABASE
operator|.
name|getInputRequiredPrivileges
argument_list|()
argument_list|,
name|HiveOperation
operator|.
name|CREATEDATABASE
operator|.
name|getOutputRequiredPrivileges
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AuthorizationException
name|e
parameter_list|)
block|{
throw|throw
name|invalidOperationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
name|metaException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|authorizeDropDatabase
parameter_list|(
name|PreDropDatabaseEvent
name|context
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|MetaException
block|{
try|try
block|{
name|tAuthorizer
operator|.
name|get
argument_list|()
operator|.
name|authorize
argument_list|(
operator|new
name|Database
argument_list|(
name|context
operator|.
name|getDatabase
argument_list|()
argument_list|)
argument_list|,
name|HiveOperation
operator|.
name|DROPDATABASE
operator|.
name|getInputRequiredPrivileges
argument_list|()
argument_list|,
name|HiveOperation
operator|.
name|DROPDATABASE
operator|.
name|getOutputRequiredPrivileges
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AuthorizationException
name|e
parameter_list|)
block|{
throw|throw
name|invalidOperationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
name|metaException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|authorizeCreateTable
parameter_list|(
name|PreCreateTableEvent
name|context
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|MetaException
block|{
try|try
block|{
name|tAuthorizer
operator|.
name|get
argument_list|()
operator|.
name|authorize
argument_list|(
operator|new
name|TableWrapper
argument_list|(
name|context
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|,
name|HiveOperation
operator|.
name|CREATETABLE
operator|.
name|getInputRequiredPrivileges
argument_list|()
argument_list|,
name|HiveOperation
operator|.
name|CREATETABLE
operator|.
name|getOutputRequiredPrivileges
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AuthorizationException
name|e
parameter_list|)
block|{
throw|throw
name|invalidOperationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
name|metaException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|authorizeDropTable
parameter_list|(
name|PreDropTableEvent
name|context
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|MetaException
block|{
try|try
block|{
name|tAuthorizer
operator|.
name|get
argument_list|()
operator|.
name|authorize
argument_list|(
operator|new
name|TableWrapper
argument_list|(
name|context
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|,
name|HiveOperation
operator|.
name|DROPTABLE
operator|.
name|getInputRequiredPrivileges
argument_list|()
argument_list|,
name|HiveOperation
operator|.
name|DROPTABLE
operator|.
name|getOutputRequiredPrivileges
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AuthorizationException
name|e
parameter_list|)
block|{
throw|throw
name|invalidOperationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
name|metaException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|authorizeAlterTable
parameter_list|(
name|PreAlterTableEvent
name|context
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|MetaException
block|{
try|try
block|{
name|tAuthorizer
operator|.
name|get
argument_list|()
operator|.
name|authorize
argument_list|(
operator|new
name|TableWrapper
argument_list|(
name|context
operator|.
name|getOldTable
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AuthorizationException
name|e
parameter_list|)
block|{
throw|throw
name|invalidOperationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
name|metaException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|authorizeAddPartition
parameter_list|(
name|PreAddPartitionEvent
name|context
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|MetaException
block|{
try|try
block|{
for|for
control|(
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
name|Partition
name|mapiPart
range|:
name|context
operator|.
name|getPartitions
argument_list|()
control|)
block|{
name|tAuthorizer
operator|.
name|get
argument_list|()
operator|.
name|authorize
argument_list|(
operator|new
name|PartitionWrapper
argument_list|(
name|mapiPart
argument_list|,
name|context
argument_list|)
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_ADDPARTS
operator|.
name|getInputRequiredPrivileges
argument_list|()
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_ADDPARTS
operator|.
name|getOutputRequiredPrivileges
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|AuthorizationException
name|e
parameter_list|)
block|{
throw|throw
name|invalidOperationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
throw|throw
name|invalidOperationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
name|metaException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|authorizeDropPartition
parameter_list|(
name|PreDropPartitionEvent
name|context
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|MetaException
block|{
try|try
block|{
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
name|Partition
name|mapiPart
init|=
name|context
operator|.
name|getPartition
argument_list|()
decl_stmt|;
name|tAuthorizer
operator|.
name|get
argument_list|()
operator|.
name|authorize
argument_list|(
operator|new
name|PartitionWrapper
argument_list|(
name|mapiPart
argument_list|,
name|context
argument_list|)
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_DROPPARTS
operator|.
name|getInputRequiredPrivileges
argument_list|()
argument_list|,
name|HiveOperation
operator|.
name|ALTERTABLE_DROPPARTS
operator|.
name|getOutputRequiredPrivileges
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AuthorizationException
name|e
parameter_list|)
block|{
throw|throw
name|invalidOperationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
throw|throw
name|invalidOperationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
name|metaException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|authorizeAlterPartition
parameter_list|(
name|PreAlterPartitionEvent
name|context
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|MetaException
block|{
try|try
block|{
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
name|Partition
name|mapiPart
init|=
name|context
operator|.
name|getNewPartition
argument_list|()
decl_stmt|;
name|tAuthorizer
operator|.
name|get
argument_list|()
operator|.
name|authorize
argument_list|(
operator|new
name|PartitionWrapper
argument_list|(
name|mapiPart
argument_list|,
name|context
argument_list|)
argument_list|,
literal|null
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|Privilege
operator|.
name|ALTER_METADATA
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AuthorizationException
name|e
parameter_list|)
block|{
throw|throw
name|invalidOperationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
throw|throw
name|invalidOperationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
name|metaException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|InvalidOperationException
name|invalidOperationException
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|InvalidOperationException
name|ex
init|=
operator|new
name|InvalidOperationException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|ex
operator|.
name|initCause
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|ex
return|;
block|}
specifier|private
name|MetaException
name|metaException
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|MetaException
name|ex
init|=
operator|new
name|MetaException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|ex
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return
name|ex
return|;
block|}
comment|// Wrapper extends ql.metadata.Table for easy construction syntax
specifier|public
specifier|static
class|class
name|TableWrapper
extends|extends
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
block|{
specifier|public
name|TableWrapper
parameter_list|(
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
name|Table
name|apiTable
parameter_list|)
block|{
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
name|Table
name|wrapperApiTable
init|=
name|apiTable
operator|.
name|deepCopy
argument_list|()
decl_stmt|;
if|if
condition|(
name|wrapperApiTable
operator|.
name|getTableType
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// TableType specified was null, we need to figure out what type it was.
if|if
condition|(
name|MetaStoreUtils
operator|.
name|isExternalTable
argument_list|(
name|wrapperApiTable
argument_list|)
condition|)
block|{
name|wrapperApiTable
operator|.
name|setTableType
argument_list|(
name|TableType
operator|.
name|EXTERNAL_TABLE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|MetaStoreUtils
operator|.
name|isIndexTable
argument_list|(
name|wrapperApiTable
argument_list|)
condition|)
block|{
name|wrapperApiTable
operator|.
name|setTableType
argument_list|(
name|TableType
operator|.
name|INDEX_TABLE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|(
name|wrapperApiTable
operator|.
name|getSd
argument_list|()
operator|==
literal|null
operator|)
operator|||
operator|(
name|wrapperApiTable
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
operator|==
literal|null
operator|)
condition|)
block|{
name|wrapperApiTable
operator|.
name|setTableType
argument_list|(
name|TableType
operator|.
name|VIRTUAL_VIEW
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|wrapperApiTable
operator|.
name|setTableType
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|initialize
argument_list|(
name|wrapperApiTable
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Wrapper extends ql.metadata.Partition for easy construction syntax
specifier|public
specifier|static
class|class
name|PartitionWrapper
extends|extends
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
block|{
specifier|public
name|PartitionWrapper
parameter_list|(
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
name|table
parameter_list|,
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
name|Partition
name|mapiPart
parameter_list|)
throws|throws
name|HiveException
block|{
name|initialize
argument_list|(
name|table
argument_list|,
name|mapiPart
argument_list|)
expr_stmt|;
block|}
specifier|public
name|PartitionWrapper
parameter_list|(
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
name|Partition
name|mapiPart
parameter_list|,
name|PreEventContext
name|context
parameter_list|)
throws|throws
name|HiveException
throws|,
name|NoSuchObjectException
throws|,
name|MetaException
block|{
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
name|Partition
name|wrapperApiPart
init|=
name|mapiPart
operator|.
name|deepCopy
argument_list|()
decl_stmt|;
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
name|Table
name|t
init|=
name|context
operator|.
name|getHandler
argument_list|()
operator|.
name|get_table
argument_list|(
name|mapiPart
operator|.
name|getDbName
argument_list|()
argument_list|,
name|mapiPart
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|wrapperApiPart
operator|.
name|getSd
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// In the cases of create partition, by the time this event fires, the partition
comment|// object has not yet come into existence, and thus will not yet have a
comment|// location or an SD, but these are needed to create a ql.metadata.Partition,
comment|// so we use the table's SD. The only place this is used is by the
comment|// authorization hooks, so we will not affect code flow in the metastore itself.
name|wrapperApiPart
operator|.
name|setSd
argument_list|(
name|t
operator|.
name|getSd
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|initialize
argument_list|(
operator|new
name|TableWrapper
argument_list|(
name|t
argument_list|)
argument_list|,
name|wrapperApiPart
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

