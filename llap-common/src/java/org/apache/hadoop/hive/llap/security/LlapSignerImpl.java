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
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|security
operator|.
name|token
operator|.
name|delegation
operator|.
name|DelegationKey
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

begin_class
specifier|public
class|class
name|LlapSignerImpl
implements|implements
name|LlapSigner
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
name|LlapSignerImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|SigningSecretManager
name|secretManager
decl_stmt|;
specifier|public
name|LlapSignerImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|clusterId
parameter_list|)
block|{
comment|// TODO: create this centrally in HS2 case
assert|assert
name|UserGroupInformation
operator|.
name|isSecurityEnabled
argument_list|()
assert|;
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
name|VisibleForTesting
specifier|public
name|LlapSignerImpl
parameter_list|(
name|SigningSecretManager
name|sm
parameter_list|)
block|{
name|secretManager
operator|=
name|sm
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|SignedMessage
name|serializeAndSign
parameter_list|(
name|Signable
name|message
parameter_list|)
throws|throws
name|IOException
block|{
name|SignedMessage
name|result
init|=
operator|new
name|SignedMessage
argument_list|()
decl_stmt|;
name|DelegationKey
name|key
init|=
name|secretManager
operator|.
name|getCurrentKey
argument_list|()
decl_stmt|;
name|message
operator|.
name|setSignInfo
argument_list|(
name|key
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|message
operator|=
name|message
operator|.
name|serialize
argument_list|()
expr_stmt|;
name|result
operator|.
name|signature
operator|=
name|secretManager
operator|.
name|signWithKey
argument_list|(
name|result
operator|.
name|message
argument_list|,
name|key
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkSignature
parameter_list|(
name|byte
index|[]
name|message
parameter_list|,
name|byte
index|[]
name|signature
parameter_list|,
name|int
name|keyId
parameter_list|)
throws|throws
name|SecurityException
block|{
name|byte
index|[]
name|expectedSignature
init|=
name|secretManager
operator|.
name|signWithKey
argument_list|(
name|message
argument_list|,
name|keyId
argument_list|)
decl_stmt|;
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|signature
argument_list|,
name|expectedSignature
argument_list|)
condition|)
return|return;
throw|throw
operator|new
name|SecurityException
argument_list|(
literal|"Message signature does not match"
argument_list|)
throw|;
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
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error closing the signer"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

