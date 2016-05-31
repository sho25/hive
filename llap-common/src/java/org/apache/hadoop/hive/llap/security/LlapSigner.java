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

begin_interface
specifier|public
interface|interface
name|LlapSigner
block|{
comment|/** An object signable by a signer. */
specifier|public
interface|interface
name|Signable
block|{
comment|/** Called by the signer to record key information as part of the message to be signed. */
name|void
name|setSignInfo
parameter_list|(
name|int
name|masterKeyId
parameter_list|,
name|String
name|user
parameter_list|)
function_decl|;
comment|/** Called by the signer to get the serialized representation of the message to be signed. */
name|byte
index|[]
name|serialize
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
comment|/** Message with the signature. */
specifier|public
specifier|static
specifier|final
class|class
name|SignedMessage
block|{
specifier|public
name|byte
index|[]
name|message
decl_stmt|,
name|signature
decl_stmt|;
block|}
comment|/** Serializes and signs the message. */
name|SignedMessage
name|serializeAndSign
parameter_list|(
name|Signable
name|message
parameter_list|)
throws|throws
name|IOException
function_decl|;
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
function_decl|;
block|}
end_interface

end_unit

