begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|execution
package|;
end_package

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
name|URL
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
name|java
operator|.
name|util
operator|.
name|SortedSet
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
name|ptest
operator|.
name|api
operator|.
name|server
operator|.
name|TestLogger
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
name|ptest
operator|.
name|execution
operator|.
name|conf
operator|.
name|Context
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
name|ptest
operator|.
name|execution
operator|.
name|conf
operator|.
name|TestConfiguration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpHost
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpRequestInterceptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|StatusLine
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|auth
operator|.
name|AuthScheme
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|auth
operator|.
name|AuthScope
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|auth
operator|.
name|AuthState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|auth
operator|.
name|Credentials
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|auth
operator|.
name|UsernamePasswordCredentials
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|client
operator|.
name|CredentialsProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|client
operator|.
name|methods
operator|.
name|HttpPost
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|client
operator|.
name|protocol
operator|.
name|ClientContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|entity
operator|.
name|StringEntity
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|impl
operator|.
name|auth
operator|.
name|BasicScheme
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|impl
operator|.
name|client
operator|.
name|DefaultHttpClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|protocol
operator|.
name|BasicHttpContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|protocol
operator|.
name|ExecutionContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|protocol
operator|.
name|HttpContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|ObjectMapper
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_class
class|class
name|JIRAService
block|{
specifier|private
specifier|final
name|Logger
name|mLogger
decl_stmt|;
specifier|private
specifier|final
name|String
name|mName
decl_stmt|;
specifier|private
specifier|final
name|String
name|mBuildTag
decl_stmt|;
specifier|private
specifier|final
name|String
name|mPatch
decl_stmt|;
specifier|private
specifier|final
name|String
name|mUrl
decl_stmt|;
specifier|private
specifier|final
name|String
name|mUser
decl_stmt|;
specifier|private
specifier|final
name|String
name|mPassword
decl_stmt|;
specifier|private
specifier|final
name|String
name|mJenkinsURL
decl_stmt|;
specifier|public
name|JIRAService
parameter_list|(
name|Logger
name|logger
parameter_list|,
name|TestConfiguration
name|configuration
parameter_list|,
name|String
name|buildTag
parameter_list|)
block|{
name|mLogger
operator|=
name|logger
expr_stmt|;
name|mName
operator|=
name|configuration
operator|.
name|getJiraName
argument_list|()
expr_stmt|;
name|mBuildTag
operator|=
name|buildTag
expr_stmt|;
name|mPatch
operator|=
name|configuration
operator|.
name|getPatch
argument_list|()
expr_stmt|;
name|mUrl
operator|=
name|configuration
operator|.
name|getJiraUrl
argument_list|()
expr_stmt|;
name|mUser
operator|=
name|configuration
operator|.
name|getJiraUser
argument_list|()
expr_stmt|;
name|mPassword
operator|=
name|configuration
operator|.
name|getJiraPassword
argument_list|()
expr_stmt|;
name|mJenkinsURL
operator|=
name|configuration
operator|.
name|getJenkinsURL
argument_list|()
expr_stmt|;
block|}
name|void
name|postComment
parameter_list|(
name|boolean
name|error
parameter_list|,
name|int
name|numExecutesTests
parameter_list|,
name|SortedSet
argument_list|<
name|String
argument_list|>
name|failedTests
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|messages
parameter_list|)
block|{
name|DefaultHttpClient
name|httpClient
init|=
operator|new
name|DefaultHttpClient
argument_list|()
decl_stmt|;
try|try
block|{
name|String
name|buildTag
init|=
name|formatBuildTag
argument_list|(
name|mBuildTag
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|comments
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|comments
operator|.
name|add
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|comments
operator|.
name|add
argument_list|(
literal|""
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|failedTests
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|comments
operator|.
name|add
argument_list|(
literal|"{color:red}Overall{color}: -1 at least one tests failed"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|numExecutesTests
operator|==
literal|0
condition|)
block|{
name|comments
operator|.
name|add
argument_list|(
literal|"{color:red}Overall{color}: -1 no tests executed"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|error
condition|)
block|{
name|comments
operator|.
name|add
argument_list|(
literal|"{color:red}Overall{color}: -1 build exited with an error"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|comments
operator|.
name|add
argument_list|(
literal|"{color:green}Overall{color}: +1 all checks pass"
argument_list|)
expr_stmt|;
block|}
name|comments
operator|.
name|add
argument_list|(
literal|""
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|mPatch
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|comments
operator|.
name|add
argument_list|(
literal|"Here are the results of testing the latest attachment:"
argument_list|)
expr_stmt|;
name|comments
operator|.
name|add
argument_list|(
name|mPatch
argument_list|)
expr_stmt|;
block|}
name|comments
operator|.
name|add
argument_list|(
literal|""
argument_list|)
expr_stmt|;
if|if
condition|(
name|numExecutesTests
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|failedTests
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|comments
operator|.
name|add
argument_list|(
name|formatSuccess
argument_list|(
literal|"+1 "
operator|+
name|numExecutesTests
operator|+
literal|" tests passed"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|comments
operator|.
name|add
argument_list|(
name|formatError
argument_list|(
literal|"-1 due to "
operator|+
name|failedTests
operator|.
name|size
argument_list|()
operator|+
literal|" failed/errored test(s), "
operator|+
name|numExecutesTests
operator|+
literal|" tests executed"
argument_list|)
argument_list|)
expr_stmt|;
name|comments
operator|.
name|add
argument_list|(
literal|"*Failed tests:*"
argument_list|)
expr_stmt|;
name|comments
operator|.
name|add
argument_list|(
literal|"{noformat}"
argument_list|)
expr_stmt|;
name|comments
operator|.
name|addAll
argument_list|(
name|failedTests
argument_list|)
expr_stmt|;
name|comments
operator|.
name|add
argument_list|(
literal|"{noformat}"
argument_list|)
expr_stmt|;
block|}
name|comments
operator|.
name|add
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
name|comments
operator|.
name|add
argument_list|(
literal|"Test results: "
operator|+
name|mJenkinsURL
operator|+
literal|"/"
operator|+
name|buildTag
operator|+
literal|"/testReport"
argument_list|)
expr_stmt|;
name|comments
operator|.
name|add
argument_list|(
literal|"Console output: "
operator|+
name|mJenkinsURL
operator|+
literal|"/"
operator|+
name|buildTag
operator|+
literal|"/console"
argument_list|)
expr_stmt|;
name|comments
operator|.
name|add
argument_list|(
literal|""
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|messages
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|comments
operator|.
name|add
argument_list|(
literal|"Messages:"
argument_list|)
expr_stmt|;
name|comments
operator|.
name|add
argument_list|(
literal|"{noformat}"
argument_list|)
expr_stmt|;
name|comments
operator|.
name|addAll
argument_list|(
name|messages
argument_list|)
expr_stmt|;
name|comments
operator|.
name|add
argument_list|(
literal|"{noformat}"
argument_list|)
expr_stmt|;
name|comments
operator|.
name|add
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
name|comments
operator|.
name|add
argument_list|(
literal|"This message is automatically generated."
argument_list|)
expr_stmt|;
name|mLogger
operator|.
name|info
argument_list|(
literal|"Comment: "
operator|+
name|Joiner
operator|.
name|on
argument_list|(
literal|"\n"
argument_list|)
operator|.
name|join
argument_list|(
name|comments
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|body
init|=
name|Joiner
operator|.
name|on
argument_list|(
literal|"\n"
argument_list|)
operator|.
name|join
argument_list|(
name|comments
argument_list|)
decl_stmt|;
name|String
name|url
init|=
name|String
operator|.
name|format
argument_list|(
literal|"%s/rest/api/2/issue/%s/comment"
argument_list|,
name|mUrl
argument_list|,
name|mName
argument_list|)
decl_stmt|;
name|URL
name|apiURL
init|=
operator|new
name|URL
argument_list|(
name|mUrl
argument_list|)
decl_stmt|;
name|httpClient
operator|.
name|getCredentialsProvider
argument_list|()
operator|.
name|setCredentials
argument_list|(
operator|new
name|AuthScope
argument_list|(
name|apiURL
operator|.
name|getHost
argument_list|()
argument_list|,
name|apiURL
operator|.
name|getPort
argument_list|()
argument_list|,
name|AuthScope
operator|.
name|ANY_REALM
argument_list|)
argument_list|,
operator|new
name|UsernamePasswordCredentials
argument_list|(
name|mUser
argument_list|,
name|mPassword
argument_list|)
argument_list|)
expr_stmt|;
name|BasicHttpContext
name|localcontext
init|=
operator|new
name|BasicHttpContext
argument_list|()
decl_stmt|;
name|localcontext
operator|.
name|setAttribute
argument_list|(
literal|"preemptive-auth"
argument_list|,
operator|new
name|BasicScheme
argument_list|()
argument_list|)
expr_stmt|;
name|httpClient
operator|.
name|addRequestInterceptor
argument_list|(
operator|new
name|PreemptiveAuth
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|HttpPost
name|request
init|=
operator|new
name|HttpPost
argument_list|(
name|url
argument_list|)
decl_stmt|;
name|ObjectMapper
name|mapper
init|=
operator|new
name|ObjectMapper
argument_list|()
decl_stmt|;
name|StringEntity
name|params
init|=
operator|new
name|StringEntity
argument_list|(
name|mapper
operator|.
name|writeValueAsString
argument_list|(
operator|new
name|Body
argument_list|(
name|body
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|request
operator|.
name|addHeader
argument_list|(
literal|"Content-Type"
argument_list|,
literal|"application/json"
argument_list|)
expr_stmt|;
name|request
operator|.
name|setEntity
argument_list|(
name|params
argument_list|)
expr_stmt|;
name|HttpResponse
name|httpResponse
init|=
name|httpClient
operator|.
name|execute
argument_list|(
name|request
argument_list|,
name|localcontext
argument_list|)
decl_stmt|;
name|StatusLine
name|statusLine
init|=
name|httpResponse
operator|.
name|getStatusLine
argument_list|()
decl_stmt|;
if|if
condition|(
name|statusLine
operator|.
name|getStatusCode
argument_list|()
operator|!=
literal|201
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|statusLine
operator|.
name|getStatusCode
argument_list|()
operator|+
literal|" "
operator|+
name|statusLine
operator|.
name|getReasonPhrase
argument_list|()
argument_list|)
throw|;
block|}
name|mLogger
operator|.
name|info
argument_list|(
literal|"JIRA Response Metadata: "
operator|+
name|httpResponse
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|mLogger
operator|.
name|error
argument_list|(
literal|"Encountered error attempting to post comment to "
operator|+
name|mName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|httpClient
operator|.
name|getConnectionManager
argument_list|()
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
specifier|static
class|class
name|Body
block|{
specifier|private
name|String
name|body
decl_stmt|;
specifier|public
name|Body
parameter_list|()
block|{      }
specifier|public
name|Body
parameter_list|(
name|String
name|body
parameter_list|)
block|{
name|this
operator|.
name|body
operator|=
name|body
expr_stmt|;
block|}
specifier|public
name|String
name|getBody
parameter_list|()
block|{
return|return
name|body
return|;
block|}
specifier|public
name|void
name|setBody
parameter_list|(
name|String
name|body
parameter_list|)
block|{
name|this
operator|.
name|body
operator|=
name|body
expr_stmt|;
block|}
block|}
comment|/**    * Hive-Build-123 to Hive-Build/123    */
annotation|@
name|VisibleForTesting
specifier|static
name|String
name|formatBuildTag
parameter_list|(
name|String
name|buildTag
parameter_list|)
block|{
if|if
condition|(
name|buildTag
operator|.
name|contains
argument_list|(
literal|"-"
argument_list|)
condition|)
block|{
name|int
name|lastDashIndex
init|=
name|buildTag
operator|.
name|lastIndexOf
argument_list|(
literal|"-"
argument_list|)
decl_stmt|;
name|String
name|buildName
init|=
name|buildTag
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|lastDashIndex
argument_list|)
decl_stmt|;
name|String
name|buildId
init|=
name|buildTag
operator|.
name|substring
argument_list|(
name|lastDashIndex
operator|+
literal|1
argument_list|)
decl_stmt|;
return|return
name|buildName
operator|+
literal|"/"
operator|+
name|buildId
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Build tag '"
operator|+
name|buildTag
operator|+
literal|"' must contain a -"
argument_list|)
throw|;
block|}
specifier|private
specifier|static
name|String
name|formatError
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"{color:red}ERROR:{color} %s"
argument_list|,
name|msg
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|String
name|formatSuccess
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"{color:green}SUCCESS:{color} %s"
argument_list|,
name|msg
argument_list|)
return|;
block|}
specifier|static
class|class
name|PreemptiveAuth
implements|implements
name|HttpRequestInterceptor
block|{
specifier|public
name|void
name|process
parameter_list|(
specifier|final
name|HttpRequest
name|request
parameter_list|,
specifier|final
name|HttpContext
name|context
parameter_list|)
throws|throws
name|HttpException
throws|,
name|IOException
block|{
name|AuthState
name|authState
init|=
operator|(
name|AuthState
operator|)
name|context
operator|.
name|getAttribute
argument_list|(
name|ClientContext
operator|.
name|TARGET_AUTH_STATE
argument_list|)
decl_stmt|;
if|if
condition|(
name|authState
operator|.
name|getAuthScheme
argument_list|()
operator|==
literal|null
condition|)
block|{
name|AuthScheme
name|authScheme
init|=
operator|(
name|AuthScheme
operator|)
name|context
operator|.
name|getAttribute
argument_list|(
literal|"preemptive-auth"
argument_list|)
decl_stmt|;
name|CredentialsProvider
name|credsProvider
init|=
operator|(
name|CredentialsProvider
operator|)
name|context
operator|.
name|getAttribute
argument_list|(
name|ClientContext
operator|.
name|CREDS_PROVIDER
argument_list|)
decl_stmt|;
name|HttpHost
name|targetHost
init|=
operator|(
name|HttpHost
operator|)
name|context
operator|.
name|getAttribute
argument_list|(
name|ExecutionContext
operator|.
name|HTTP_TARGET_HOST
argument_list|)
decl_stmt|;
if|if
condition|(
name|authScheme
operator|!=
literal|null
condition|)
block|{
name|Credentials
name|creds
init|=
name|credsProvider
operator|.
name|getCredentials
argument_list|(
operator|new
name|AuthScope
argument_list|(
name|targetHost
operator|.
name|getHostName
argument_list|()
argument_list|,
name|targetHost
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|creds
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HttpException
argument_list|(
literal|"No credentials for preemptive authentication"
argument_list|)
throw|;
block|}
name|authState
operator|.
name|update
argument_list|(
name|authScheme
argument_list|,
name|creds
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|TestLogger
name|logger
init|=
operator|new
name|TestLogger
argument_list|(
name|System
operator|.
name|err
argument_list|,
name|TestLogger
operator|.
name|LEVEL
operator|.
name|TRACE
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|context
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|context
operator|.
name|put
argument_list|(
literal|"jiraUrl"
argument_list|,
literal|"https://issues.apache.org/jira"
argument_list|)
expr_stmt|;
name|context
operator|.
name|put
argument_list|(
literal|"jiraUser"
argument_list|,
literal|"hiveqa"
argument_list|)
expr_stmt|;
name|context
operator|.
name|put
argument_list|(
literal|"jiraPassword"
argument_list|,
literal|"password goes here"
argument_list|)
expr_stmt|;
name|context
operator|.
name|put
argument_list|(
literal|"branch"
argument_list|,
literal|"trunk"
argument_list|)
expr_stmt|;
name|context
operator|.
name|put
argument_list|(
literal|"repository"
argument_list|,
literal|"repo"
argument_list|)
expr_stmt|;
name|context
operator|.
name|put
argument_list|(
literal|"repositoryName"
argument_list|,
literal|"repoName"
argument_list|)
expr_stmt|;
name|context
operator|.
name|put
argument_list|(
literal|"antArgs"
argument_list|,
literal|"-Dsome=thing"
argument_list|)
expr_stmt|;
name|TestConfiguration
name|configuration
init|=
operator|new
name|TestConfiguration
argument_list|(
operator|new
name|Context
argument_list|(
name|context
argument_list|)
argument_list|,
name|logger
argument_list|)
decl_stmt|;
name|configuration
operator|.
name|setJiraName
argument_list|(
literal|"HIVE-4892"
argument_list|)
expr_stmt|;
name|JIRAService
name|service
init|=
operator|new
name|JIRAService
argument_list|(
name|logger
argument_list|,
name|configuration
argument_list|,
literal|"test-123"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|messages
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"msg1"
argument_list|,
literal|"msg2"
argument_list|)
decl_stmt|;
name|SortedSet
argument_list|<
name|String
argument_list|>
name|failedTests
init|=
name|Sets
operator|.
name|newTreeSet
argument_list|(
name|Collections
operator|.
name|singleton
argument_list|(
literal|"failed"
argument_list|)
argument_list|)
decl_stmt|;
name|service
operator|.
name|postComment
argument_list|(
literal|false
argument_list|,
literal|5
argument_list|,
name|failedTests
argument_list|,
name|messages
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

