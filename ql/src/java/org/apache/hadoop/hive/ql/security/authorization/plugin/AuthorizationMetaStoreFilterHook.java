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
operator|.
name|plugin
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
name|hive
operator|.
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
operator|.
name|Private
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
name|DefaultMetaStoreFilterHookImpl
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
name|ql
operator|.
name|security
operator|.
name|authorization
operator|.
name|plugin
operator|.
name|HivePrivilegeObject
operator|.
name|HivePrivilegeObjectType
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

begin_comment
comment|/**  * Metastore filter hook for filtering out the list of objects that the current authorization  * implementation does not allow user to see  */
end_comment

begin_class
annotation|@
name|Private
specifier|public
class|class
name|AuthorizationMetaStoreFilterHook
extends|extends
name|DefaultMetaStoreFilterHookImpl
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
name|AuthorizationMetaStoreFilterHook
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|AuthorizationMetaStoreFilterHook
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
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|filterTableNames
parameter_list|(
name|String
name|dbName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|tableList
parameter_list|)
throws|throws
name|MetaException
block|{
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|listObjs
init|=
name|getHivePrivObjects
argument_list|(
name|dbName
argument_list|,
name|tableList
argument_list|)
decl_stmt|;
return|return
name|getTableNames
argument_list|(
name|getFilteredObjects
argument_list|(
name|listObjs
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|filterDatabases
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|dbList
parameter_list|)
throws|throws
name|MetaException
block|{
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|listObjs
init|=
name|getHivePrivObjects
argument_list|(
name|dbList
argument_list|)
decl_stmt|;
return|return
name|getDbNames
argument_list|(
name|getFilteredObjects
argument_list|(
name|listObjs
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|getHivePrivObjects
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|dbList
parameter_list|)
block|{
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|objs
init|=
operator|new
name|ArrayList
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|dbname
range|:
name|dbList
control|)
block|{
name|objs
operator|.
name|add
argument_list|(
operator|new
name|HivePrivilegeObject
argument_list|(
name|HivePrivilegeObjectType
operator|.
name|DATABASE
argument_list|,
name|dbname
argument_list|,
name|dbname
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|objs
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getDbNames
parameter_list|(
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|filteredObjects
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|tnames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HivePrivilegeObject
name|obj
range|:
name|filteredObjects
control|)
block|{
name|tnames
operator|.
name|add
argument_list|(
name|obj
operator|.
name|getDbname
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|tnames
return|;
block|}
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getTableNames
parameter_list|(
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|filteredObjects
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|tnames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|HivePrivilegeObject
name|obj
range|:
name|filteredObjects
control|)
block|{
name|tnames
operator|.
name|add
argument_list|(
name|obj
operator|.
name|getObjectName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|tnames
return|;
block|}
specifier|private
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|getFilteredObjects
parameter_list|(
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|listObjs
parameter_list|)
throws|throws
name|MetaException
block|{
name|SessionState
name|ss
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
name|HiveAuthzContext
operator|.
name|Builder
name|authzContextBuilder
init|=
operator|new
name|HiveAuthzContext
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|authzContextBuilder
operator|.
name|setUserIpAddress
argument_list|(
name|ss
operator|.
name|getUserIpAddress
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
return|return
name|ss
operator|.
name|getAuthorizerV2
argument_list|()
operator|.
name|filterListCmdObjects
argument_list|(
name|listObjs
argument_list|,
name|authzContextBuilder
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|HiveAuthzPluginException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|MetaException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|HiveAccessControlException
name|e
parameter_list|)
block|{
comment|// authorization error is not really expected in a filter call
comment|// the impl should have just filtered out everything. A checkPrivileges call
comment|// would have already been made to authorize this action
name|LOG
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|MetaException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|private
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|getHivePrivObjects
parameter_list|(
name|String
name|dbName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|tableList
parameter_list|)
block|{
name|List
argument_list|<
name|HivePrivilegeObject
argument_list|>
name|objs
init|=
operator|new
name|ArrayList
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|tname
range|:
name|tableList
control|)
block|{
name|objs
operator|.
name|add
argument_list|(
operator|new
name|HivePrivilegeObject
argument_list|(
name|HivePrivilegeObjectType
operator|.
name|TABLE_OR_VIEW
argument_list|,
name|dbName
argument_list|,
name|tname
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|objs
return|;
block|}
block|}
end_class

end_unit

