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
name|llap
operator|.
name|security
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
name|security
operator|.
name|token
operator|.
name|Token
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
name|LlapTokenLocalClientImpl
implements|implements
name|LlapTokenLocalClient
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
name|LlapTokenLocalClientImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|SecretManager
name|secretManager
decl_stmt|;
specifier|public
name|LlapTokenLocalClientImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|clusterId
parameter_list|)
block|{
comment|// TODO: create this centrally in HS2 case
name|secretManager
operator|=
name|SecretManager
operator|.
name|createSecretManager
argument_list|(
name|conf
argument_list|,
name|clusterId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Token
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|createToken
parameter_list|(
name|String
name|appId
parameter_list|,
name|String
name|user
parameter_list|,
name|boolean
name|isSignatureRequired
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|Token
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|token
init|=
name|secretManager
operator|.
name|createLlapToken
argument_list|(
name|appId
argument_list|,
name|user
argument_list|,
name|isSignatureRequired
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Created a LLAP delegation token locally: "
operator|+
name|token
argument_list|)
expr_stmt|;
block|}
return|return
name|token
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|(
name|ex
operator|instanceof
name|IOException
operator|)
condition|?
operator|(
name|IOException
operator|)
name|ex
else|:
operator|new
name|IOException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
name|secretManager
operator|.
name|stopThreads
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
comment|// Ignore.
block|}
block|}
block|}
end_class

end_unit

