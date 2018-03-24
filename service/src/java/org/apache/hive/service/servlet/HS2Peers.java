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
name|servlet
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
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|ServletContext
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServlet
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletRequest
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletResponse
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
name|security
operator|.
name|authentication
operator|.
name|client
operator|.
name|KerberosAuthenticator
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
name|http
operator|.
name|HttpServer
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
name|server
operator|.
name|HS2ActivePassiveHARegistry
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
name|server
operator|.
name|HS2ActivePassiveHARegistryClient
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
name|server
operator|.
name|HiveServer2Instance
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
name|annotate
operator|.
name|JsonIgnore
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
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|SerializationConfig
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

begin_comment
comment|/**  * Returns all HS2 instances in Active-Passive standy modes.  */
end_comment

begin_class
specifier|public
class|class
name|HS2Peers
extends|extends
name|HttpServlet
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HS2Peers
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
class|class
name|HS2Instances
block|{
specifier|private
name|Collection
argument_list|<
name|HiveServer2Instance
argument_list|>
name|hiveServer2Instances
decl_stmt|;
comment|// empty c'tor to make jackson happy
specifier|public
name|HS2Instances
parameter_list|()
block|{     }
specifier|public
name|HS2Instances
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|HiveServer2Instance
argument_list|>
name|hiveServer2Instances
parameter_list|)
block|{
name|this
operator|.
name|hiveServer2Instances
operator|=
name|hiveServer2Instances
expr_stmt|;
block|}
specifier|public
name|Collection
argument_list|<
name|HiveServer2Instance
argument_list|>
name|getHiveServer2Instances
parameter_list|()
block|{
return|return
name|hiveServer2Instances
return|;
block|}
specifier|public
name|void
name|setHiveServer2Instances
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|HiveServer2Instance
argument_list|>
name|hiveServer2Instances
parameter_list|)
block|{
name|this
operator|.
name|hiveServer2Instances
operator|=
name|hiveServer2Instances
expr_stmt|;
block|}
annotation|@
name|JsonIgnore
specifier|public
name|String
name|toJson
parameter_list|()
throws|throws
name|IOException
block|{
name|ObjectMapper
name|mapper
init|=
operator|new
name|ObjectMapper
argument_list|()
decl_stmt|;
name|mapper
operator|.
name|configure
argument_list|(
name|SerializationConfig
operator|.
name|Feature
operator|.
name|FAIL_ON_EMPTY_BEANS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
return|return
name|mapper
operator|.
name|writerWithDefaultPrettyPrinter
argument_list|()
operator|.
name|writeValueAsString
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|doGet
parameter_list|(
name|HttpServletRequest
name|request
parameter_list|,
name|HttpServletResponse
name|response
parameter_list|)
throws|throws
name|IOException
block|{
comment|// admin check -
comment|// allows when hadoop.security.instrumentation.requires.admin is set to false
comment|// when hadoop.security.instrumentation.requires.admin is set to true, checks if hadoop.security.authorization
comment|// is true and if the logged in user (via PAM or SPNEGO + kerberos) is in hive.users.in.admin.role list
specifier|final
name|ServletContext
name|context
init|=
name|getServletContext
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|HttpServer
operator|.
name|isInstrumentationAccessAllowed
argument_list|(
name|context
argument_list|,
name|request
argument_list|,
name|response
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unauthorized to perform GET action. remoteUser: {}"
argument_list|,
name|request
operator|.
name|getRemoteUser
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|ServletContext
name|ctx
init|=
name|getServletContext
argument_list|()
decl_stmt|;
name|HiveConf
name|hiveConf
init|=
operator|(
name|HiveConf
operator|)
name|ctx
operator|.
name|getAttribute
argument_list|(
literal|"hiveconf"
argument_list|)
decl_stmt|;
name|HS2ActivePassiveHARegistry
name|hs2Registry
init|=
name|HS2ActivePassiveHARegistryClient
operator|.
name|getClient
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|HS2Instances
name|instances
init|=
operator|new
name|HS2Instances
argument_list|(
name|hs2Registry
operator|.
name|getAll
argument_list|()
argument_list|)
decl_stmt|;
name|response
operator|.
name|getWriter
argument_list|()
operator|.
name|write
argument_list|(
name|instances
operator|.
name|toJson
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|.
name|setStatus
argument_list|(
name|HttpServletResponse
operator|.
name|SC_OK
argument_list|)
expr_stmt|;
name|response
operator|.
name|flushBuffer
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

