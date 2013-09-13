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
name|cli
operator|.
name|SemanticAnalysis
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|exec
operator|.
name|Task
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
name|InvalidTableException
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
name|parse
operator|.
name|AbstractSemanticAnalyzerHook
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
name|parse
operator|.
name|HiveSemanticAnalyzerHookContext
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
name|parse
operator|.
name|SemanticException
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
name|DDLWork
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
comment|/**  * Base class for HCatSemanticAnalyzer hooks.  * @deprecated Use/modify {@link org.apache.hive.hcatalog.cli.SemanticAnalysis.HCatSemanticAnalyzerBase} instead  */
end_comment

begin_class
specifier|public
class|class
name|HCatSemanticAnalyzerBase
extends|extends
name|AbstractSemanticAnalyzerHook
block|{
specifier|private
name|HiveAuthorizationProvider
name|authProvider
decl_stmt|;
specifier|public
name|HiveAuthorizationProvider
name|getAuthProvider
parameter_list|()
block|{
if|if
condition|(
name|authProvider
operator|==
literal|null
condition|)
block|{
name|authProvider
operator|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getAuthorizer
argument_list|()
expr_stmt|;
block|}
return|return
name|authProvider
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postAnalyze
parameter_list|(
name|HiveSemanticAnalyzerHookContext
name|context
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
operator|.
name|postAnalyze
argument_list|(
name|context
argument_list|,
name|rootTasks
argument_list|)
expr_stmt|;
comment|//Authorize the operation.
name|authorizeDDL
argument_list|(
name|context
argument_list|,
name|rootTasks
argument_list|)
expr_stmt|;
block|}
comment|/**    * Checks for the given rootTasks, and calls authorizeDDLWork() for each DDLWork to    * be authorized. The hooks should override this, or authorizeDDLWork to perform the    * actual authorization.    */
comment|/*   * Impl note: Hive provides authorization with it's own model, and calls the defined   * HiveAuthorizationProvider from Driver.doAuthorization(). However, HCat has to   * do additional calls to the auth provider to implement expected behavior for   * StorageDelegationAuthorizationProvider. This means, that the defined auth provider   * is called by both Hive and HCat. The following are missing from Hive's implementation,   * and when they are fixed in Hive, we can remove the HCat-specific auth checks.   * 1. CREATE DATABASE/TABLE, ADD PARTITION statements does not call   * HiveAuthorizationProvider.authorize() with the candidate objects, which means that   * we cannot do checks against defined LOCATION.   * 2. HiveOperation does not define sufficient Privileges for most of the operations,   * especially database operations.   * 3. For some of the operations, Hive SemanticAnalyzer does not add the changed   * object as a WriteEntity or ReadEntity.   *   * @see https://issues.apache.org/jira/browse/HCATALOG-244   * @see https://issues.apache.org/jira/browse/HCATALOG-245   */
specifier|protected
name|void
name|authorizeDDL
parameter_list|(
name|HiveSemanticAnalyzerHookContext
name|context
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|rootTasks
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
operator|!
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_ENABLED
argument_list|)
condition|)
block|{
return|return;
block|}
name|Hive
name|hive
decl_stmt|;
try|try
block|{
name|hive
operator|=
name|context
operator|.
name|getHive
argument_list|()
expr_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|task
range|:
name|rootTasks
control|)
block|{
if|if
condition|(
name|task
operator|.
name|getWork
argument_list|()
operator|instanceof
name|DDLWork
condition|)
block|{
name|DDLWork
name|work
init|=
operator|(
name|DDLWork
operator|)
name|task
operator|.
name|getWork
argument_list|()
decl_stmt|;
if|if
condition|(
name|work
operator|!=
literal|null
condition|)
block|{
name|authorizeDDLWork
argument_list|(
name|context
argument_list|,
name|hive
argument_list|,
name|work
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|SemanticException
name|ex
parameter_list|)
block|{
throw|throw
name|ex
throw|;
block|}
catch|catch
parameter_list|(
name|AuthorizationException
name|ex
parameter_list|)
block|{
throw|throw
name|ex
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
comment|/**    * Authorized the given DDLWork. Does nothing by default. Override this    * and delegate to the relevant method in HiveAuthorizationProvider obtained by    * getAuthProvider().    */
specifier|protected
name|void
name|authorizeDDLWork
parameter_list|(
name|HiveSemanticAnalyzerHookContext
name|context
parameter_list|,
name|Hive
name|hive
parameter_list|,
name|DDLWork
name|work
parameter_list|)
throws|throws
name|HiveException
block|{   }
specifier|protected
name|void
name|authorize
parameter_list|(
name|Privilege
index|[]
name|inputPrivs
parameter_list|,
name|Privilege
index|[]
name|outputPrivs
parameter_list|)
throws|throws
name|AuthorizationException
throws|,
name|SemanticException
block|{
try|try
block|{
name|getAuthProvider
argument_list|()
operator|.
name|authorize
argument_list|(
name|inputPrivs
argument_list|,
name|outputPrivs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|void
name|authorize
parameter_list|(
name|Database
name|db
parameter_list|,
name|Privilege
name|priv
parameter_list|)
throws|throws
name|AuthorizationException
throws|,
name|SemanticException
block|{
try|try
block|{
name|getAuthProvider
argument_list|()
operator|.
name|authorize
argument_list|(
name|db
argument_list|,
literal|null
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|priv
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|void
name|authorizeTable
parameter_list|(
name|Hive
name|hive
parameter_list|,
name|String
name|tableName
parameter_list|,
name|Privilege
name|priv
parameter_list|)
throws|throws
name|AuthorizationException
throws|,
name|HiveException
block|{
name|Table
name|table
decl_stmt|;
try|try
block|{
name|table
operator|=
name|hive
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidTableException
name|ite
parameter_list|)
block|{
comment|// Table itself doesn't exist in metastore, nothing to validate.
return|return;
block|}
name|authorize
argument_list|(
name|table
argument_list|,
name|priv
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|authorize
parameter_list|(
name|Table
name|table
parameter_list|,
name|Privilege
name|priv
parameter_list|)
throws|throws
name|AuthorizationException
throws|,
name|SemanticException
block|{
try|try
block|{
name|getAuthProvider
argument_list|()
operator|.
name|authorize
argument_list|(
name|table
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|priv
block|}
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|void
name|authorize
parameter_list|(
name|Partition
name|part
parameter_list|,
name|Privilege
name|priv
parameter_list|)
throws|throws
name|AuthorizationException
throws|,
name|SemanticException
block|{
try|try
block|{
name|getAuthProvider
argument_list|()
operator|.
name|authorize
argument_list|(
name|part
argument_list|,
operator|new
name|Privilege
index|[]
block|{
name|priv
block|}
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

