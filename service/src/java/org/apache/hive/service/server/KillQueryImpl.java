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
name|hive
operator|.
name|service
operator|.
name|server
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
name|lang
operator|.
name|StringUtils
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
name|ql
operator|.
name|QueryState
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
name|authorization
operator|.
name|plugin
operator|.
name|HiveAuthzContext
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
name|HiveOperationType
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
name|KillQuery
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
name|yarn
operator|.
name|api
operator|.
name|ApplicationClientProtocol
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
name|yarn
operator|.
name|api
operator|.
name|protocolrecords
operator|.
name|ApplicationsRequestScope
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
name|yarn
operator|.
name|api
operator|.
name|protocolrecords
operator|.
name|GetApplicationsRequest
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
name|yarn
operator|.
name|api
operator|.
name|protocolrecords
operator|.
name|GetApplicationsResponse
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|ApplicationId
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
name|yarn
operator|.
name|api
operator|.
name|records
operator|.
name|ApplicationReport
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
name|yarn
operator|.
name|client
operator|.
name|ClientRMProxy
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
name|yarn
operator|.
name|client
operator|.
name|api
operator|.
name|YarnClient
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
name|yarn
operator|.
name|exceptions
operator|.
name|YarnException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|HiveSQLException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|OperationHandle
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|operation
operator|.
name|Operation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|service
operator|.
name|cli
operator|.
name|operation
operator|.
name|OperationManager
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
import|;
end_import

begin_class
specifier|public
class|class
name|KillQueryImpl
implements|implements
name|KillQuery
block|{
specifier|private
specifier|final
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|KillQueryImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|OperationManager
name|operationManager
decl_stmt|;
specifier|private
enum|enum
name|TagOrId
block|{
name|TAG
block|,
name|ID
block|,
name|UNKNOWN
block|}
empty_stmt|;
specifier|public
name|KillQueryImpl
parameter_list|(
name|OperationManager
name|operationManager
parameter_list|)
block|{
name|this
operator|.
name|operationManager
operator|=
name|operationManager
expr_stmt|;
block|}
specifier|public
specifier|static
name|Set
argument_list|<
name|ApplicationId
argument_list|>
name|getChildYarnJobs
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|tag
parameter_list|)
throws|throws
name|IOException
throws|,
name|YarnException
block|{
name|Set
argument_list|<
name|ApplicationId
argument_list|>
name|childYarnJobs
init|=
operator|new
name|HashSet
argument_list|<
name|ApplicationId
argument_list|>
argument_list|()
decl_stmt|;
name|GetApplicationsRequest
name|gar
init|=
name|GetApplicationsRequest
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|gar
operator|.
name|setScope
argument_list|(
name|ApplicationsRequestScope
operator|.
name|OWN
argument_list|)
expr_stmt|;
name|gar
operator|.
name|setApplicationTags
argument_list|(
name|Collections
operator|.
name|singleton
argument_list|(
name|tag
argument_list|)
argument_list|)
expr_stmt|;
name|ApplicationClientProtocol
name|proxy
init|=
name|ClientRMProxy
operator|.
name|createRMProxy
argument_list|(
name|conf
argument_list|,
name|ApplicationClientProtocol
operator|.
name|class
argument_list|)
decl_stmt|;
name|GetApplicationsResponse
name|apps
init|=
name|proxy
operator|.
name|getApplications
argument_list|(
name|gar
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ApplicationReport
argument_list|>
name|appsList
init|=
name|apps
operator|.
name|getApplicationList
argument_list|()
decl_stmt|;
for|for
control|(
name|ApplicationReport
name|appReport
range|:
name|appsList
control|)
block|{
if|if
condition|(
name|isAdmin
argument_list|()
operator|||
name|appReport
operator|.
name|getApplicationTags
argument_list|()
operator|.
name|contains
argument_list|(
name|QueryState
operator|.
name|USERID_TAG
operator|+
literal|"="
operator|+
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getUserName
argument_list|()
argument_list|)
condition|)
block|{
name|childYarnJobs
operator|.
name|add
argument_list|(
name|appReport
operator|.
name|getApplicationId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|childYarnJobs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No child applications found"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Found child YARN applications: "
operator|+
name|StringUtils
operator|.
name|join
argument_list|(
name|childYarnJobs
argument_list|,
literal|","
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|childYarnJobs
return|;
block|}
specifier|public
specifier|static
name|void
name|killChildYarnJobs
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|tag
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|tag
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Killing yarn jobs using query tag:"
operator|+
name|tag
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|ApplicationId
argument_list|>
name|childYarnJobs
init|=
name|getChildYarnJobs
argument_list|(
name|conf
argument_list|,
name|tag
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|childYarnJobs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|YarnClient
name|yarnClient
init|=
name|YarnClient
operator|.
name|createYarnClient
argument_list|()
decl_stmt|;
name|yarnClient
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|yarnClient
operator|.
name|start
argument_list|()
expr_stmt|;
for|for
control|(
name|ApplicationId
name|app
range|:
name|childYarnJobs
control|)
block|{
name|yarnClient
operator|.
name|killApplication
argument_list|(
name|app
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
decl||
name|YarnException
name|ye
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception occurred while killing child job({})"
argument_list|,
name|ye
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|boolean
name|isAdmin
parameter_list|()
block|{
name|boolean
name|isAdmin
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getAuthorizerV2
argument_list|()
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getAuthorizerV2
argument_list|()
operator|.
name|checkPrivileges
argument_list|(
name|HiveOperationType
operator|.
name|KILL_QUERY
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|HivePrivilegeObject
argument_list|>
argument_list|()
argument_list|,
operator|new
name|HiveAuthzContext
operator|.
name|Builder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|isAdmin
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{       }
block|}
return|return
name|isAdmin
return|;
block|}
specifier|private
name|boolean
name|cancelOperation
parameter_list|(
name|Operation
name|operation
parameter_list|,
name|boolean
name|isAdmin
parameter_list|,
name|String
name|errMsg
parameter_list|)
throws|throws
name|HiveSQLException
block|{
if|if
condition|(
name|isAdmin
operator|||
name|operation
operator|.
name|getParentSession
argument_list|()
operator|.
name|getUserName
argument_list|()
operator|.
name|equals
argument_list|(
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getAuthenticator
argument_list|()
operator|.
name|getUserName
argument_list|()
argument_list|)
condition|)
block|{
name|OperationHandle
name|handle
init|=
name|operation
operator|.
name|getHandle
argument_list|()
decl_stmt|;
name|operationManager
operator|.
name|cancelOperation
argument_list|(
name|handle
argument_list|,
name|errMsg
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|killQuery
parameter_list|(
name|String
name|queryIdOrTag
parameter_list|,
name|String
name|errMsg
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|TagOrId
name|tagOrId
init|=
name|TagOrId
operator|.
name|UNKNOWN
decl_stmt|;
name|Set
argument_list|<
name|Operation
argument_list|>
name|operationsToKill
init|=
operator|new
name|HashSet
argument_list|<
name|Operation
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|operationManager
operator|.
name|getOperationByQueryId
argument_list|(
name|queryIdOrTag
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|operationsToKill
operator|.
name|add
argument_list|(
name|operationManager
operator|.
name|getOperationByQueryId
argument_list|(
name|queryIdOrTag
argument_list|)
argument_list|)
expr_stmt|;
name|tagOrId
operator|=
name|TagOrId
operator|.
name|ID
expr_stmt|;
block|}
else|else
block|{
name|operationsToKill
operator|.
name|addAll
argument_list|(
name|operationManager
operator|.
name|getOperationsByQueryTag
argument_list|(
name|queryIdOrTag
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|operationsToKill
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tagOrId
operator|=
name|TagOrId
operator|.
name|TAG
expr_stmt|;
block|}
block|}
if|if
condition|(
name|operationsToKill
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Query not found: "
operator|+
name|queryIdOrTag
argument_list|)
expr_stmt|;
block|}
name|boolean
name|admin
init|=
name|isAdmin
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|tagOrId
condition|)
block|{
case|case
name|ID
case|:
name|Operation
name|operation
init|=
name|operationsToKill
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|boolean
name|canceled
init|=
name|cancelOperation
argument_list|(
name|operation
argument_list|,
name|admin
argument_list|,
name|errMsg
argument_list|)
decl_stmt|;
if|if
condition|(
name|canceled
condition|)
block|{
name|String
name|queryTag
init|=
name|operation
operator|.
name|getQueryTag
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryTag
operator|==
literal|null
condition|)
block|{
name|queryTag
operator|=
name|queryIdOrTag
expr_stmt|;
block|}
name|killChildYarnJobs
argument_list|(
name|conf
argument_list|,
name|queryTag
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// no privilege to cancel
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"No privilege"
argument_list|)
throw|;
block|}
break|break;
case|case
name|TAG
case|:
name|int
name|numCanceled
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Operation
name|operationToKill
range|:
name|operationsToKill
control|)
block|{
if|if
condition|(
name|cancelOperation
argument_list|(
name|operationToKill
argument_list|,
name|admin
argument_list|,
name|errMsg
argument_list|)
condition|)
block|{
name|numCanceled
operator|++
expr_stmt|;
block|}
block|}
name|killChildYarnJobs
argument_list|(
name|conf
argument_list|,
name|queryIdOrTag
argument_list|)
expr_stmt|;
if|if
condition|(
name|numCanceled
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|HiveSQLException
argument_list|(
literal|"No privilege"
argument_list|)
throw|;
block|}
break|break;
case|case
name|UNKNOWN
case|:
name|killChildYarnJobs
argument_list|(
name|conf
argument_list|,
name|queryIdOrTag
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
catch|catch
parameter_list|(
name|HiveSQLException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Kill query failed for query "
operator|+
name|queryIdOrTag
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

