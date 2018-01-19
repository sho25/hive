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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|metastore
operator|.
name|api
operator|.
name|HiveObjectRef
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
name|HiveObjectType
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
name|api
operator|.
name|PrincipalPrivilegeSet
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
name|thrift
operator|.
name|TException
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|HiveAuthorizationProviderBase
implements|implements
name|HiveAuthorizationProvider
block|{
specifier|protected
class|class
name|HiveProxy
block|{
specifier|private
specifier|final
name|boolean
name|hasHiveClient
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|IHMSHandler
name|handler
decl_stmt|;
specifier|public
name|HiveProxy
parameter_list|(
name|Hive
name|hive
parameter_list|)
block|{
name|this
operator|.
name|hasHiveClient
operator|=
name|hive
operator|!=
literal|null
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|hive
operator|.
name|getConf
argument_list|()
expr_stmt|;
name|this
operator|.
name|handler
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|HiveProxy
parameter_list|()
block|{
name|this
operator|.
name|hasHiveClient
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|conf
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|handler
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|void
name|setHandler
parameter_list|(
name|IHMSHandler
name|handler
parameter_list|)
block|{
name|this
operator|.
name|handler
operator|=
name|handler
expr_stmt|;
block|}
specifier|public
name|boolean
name|isRunFromMetaStore
parameter_list|()
block|{
return|return
operator|!
name|hasHiveClient
return|;
block|}
specifier|public
name|PrincipalPrivilegeSet
name|get_privilege_set
parameter_list|(
name|HiveObjectType
name|column
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partValues
parameter_list|,
name|String
name|col
parameter_list|,
name|String
name|userName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|groupNames
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|!
name|isRunFromMetaStore
argument_list|()
condition|)
block|{
return|return
name|Hive
operator|.
name|getWithFastCheck
argument_list|(
name|conf
argument_list|)
operator|.
name|get_privilege_set
argument_list|(
name|column
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partValues
argument_list|,
name|col
argument_list|,
name|userName
argument_list|,
name|groupNames
argument_list|)
return|;
block|}
else|else
block|{
name|HiveObjectRef
name|hiveObj
init|=
operator|new
name|HiveObjectRef
argument_list|(
name|column
argument_list|,
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partValues
argument_list|,
name|col
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|handler
operator|.
name|get_privilege_set
argument_list|(
name|hiveObj
argument_list|,
name|userName
argument_list|,
name|groupNames
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|public
name|Database
name|getDatabase
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|!
name|isRunFromMetaStore
argument_list|()
condition|)
block|{
return|return
name|Hive
operator|.
name|getWithFastCheck
argument_list|(
name|conf
argument_list|)
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
return|;
block|}
else|else
block|{
try|try
block|{
return|return
name|handler
operator|.
name|get_database_core
argument_list|(
name|dbName
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
specifier|protected
name|HiveProxy
name|hive_db
decl_stmt|;
specifier|protected
name|HiveAuthenticationProvider
name|authenticator
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HiveAuthorizationProvider
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
try|try
block|{
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|this
operator|.
name|conf
return|;
block|}
specifier|public
name|HiveAuthenticationProvider
name|getAuthenticator
parameter_list|()
block|{
return|return
name|authenticator
return|;
block|}
specifier|public
name|void
name|setAuthenticator
parameter_list|(
name|HiveAuthenticationProvider
name|authenticator
parameter_list|)
block|{
name|this
operator|.
name|authenticator
operator|=
name|authenticator
expr_stmt|;
block|}
block|}
end_class

end_unit

