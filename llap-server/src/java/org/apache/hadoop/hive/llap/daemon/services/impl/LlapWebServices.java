begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|llap
operator|.
name|daemon
operator|.
name|services
operator|.
name|impl
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|UserGroupInformation
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
name|service
operator|.
name|AbstractService
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

begin_class
specifier|public
class|class
name|LlapWebServices
extends|extends
name|AbstractService
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
name|LlapWebServices
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|int
name|port
decl_stmt|;
specifier|private
name|HttpServer
name|http
decl_stmt|;
specifier|public
name|LlapWebServices
parameter_list|()
block|{
name|super
argument_list|(
literal|"LlapWebServices"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serviceInit
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|port
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_DAEMON_WEB_PORT
argument_list|)
expr_stmt|;
name|String
name|bindAddress
init|=
literal|"0.0.0.0"
decl_stmt|;
name|HttpServer
operator|.
name|Builder
name|builder
init|=
operator|new
name|HttpServer
operator|.
name|Builder
argument_list|()
operator|.
name|setName
argument_list|(
literal|"llap"
argument_list|)
operator|.
name|setPort
argument_list|(
name|this
operator|.
name|port
argument_list|)
operator|.
name|setHost
argument_list|(
name|bindAddress
argument_list|)
decl_stmt|;
name|builder
operator|.
name|setConf
argument_list|(
operator|new
name|HiveConf
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|UserGroupInformation
operator|.
name|isSecurityEnabled
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setUseSSL
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_WEB_AUTO_AUTH
argument_list|)
condition|)
block|{
name|builder
operator|.
name|setUseSPNEGO
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// this setups auth filtering in build()
name|builder
operator|.
name|setSPNEGOPrincipal
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_KERBEROS_PRINCIPAL
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setSPNEGOKeytab
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|LLAP_KERBEROS_KEYTAB_FILE
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
try|try
block|{
name|this
operator|.
name|http
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"LLAP web service failed to come up"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|serviceStart
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|this
operator|.
name|http
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|http
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|serviceStop
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|this
operator|.
name|http
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|http
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

