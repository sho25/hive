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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
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
operator|.
name|LlapSigner
operator|.
name|Signable
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
name|llap
operator|.
name|security
operator|.
name|LlapSigner
operator|.
name|SignedMessage
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
name|io
operator|.
name|DataInputBuffer
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
name|io
operator|.
name|DataOutputBuffer
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
name|AbstractDelegationTokenIdentifier
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
name|AbstractDelegationTokenSecretManager
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
name|junit
operator|.
name|Test
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
name|TestLlapSignerImpl
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
name|TestLlapSignerImpl
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|10000
argument_list|)
specifier|public
name|void
name|testSigning
parameter_list|()
throws|throws
name|Exception
block|{
name|FakeSecretManager
name|fsm
init|=
operator|new
name|FakeSecretManager
argument_list|()
decl_stmt|;
name|fsm
operator|.
name|startThreads
argument_list|()
expr_stmt|;
comment|// Make sure the signature works.
name|LlapSignerImpl
name|signer
init|=
operator|new
name|LlapSignerImpl
argument_list|(
name|fsm
argument_list|)
decl_stmt|;
name|byte
name|theByte
init|=
literal|1
decl_stmt|;
name|TestSignable
name|in
init|=
operator|new
name|TestSignable
argument_list|(
name|theByte
argument_list|)
decl_stmt|;
name|TestSignable
name|in2
init|=
operator|new
name|TestSignable
argument_list|(
operator|++
name|theByte
argument_list|)
decl_stmt|;
name|SignedMessage
name|sm
init|=
name|signer
operator|.
name|serializeAndSign
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|SignedMessage
name|sm2
init|=
name|signer
operator|.
name|serializeAndSign
argument_list|(
name|in2
argument_list|)
decl_stmt|;
name|TestSignable
name|out
init|=
name|TestSignable
operator|.
name|deserialize
argument_list|(
name|sm
operator|.
name|message
argument_list|)
decl_stmt|;
name|TestSignable
name|out2
init|=
name|TestSignable
operator|.
name|deserialize
argument_list|(
name|sm2
operator|.
name|message
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|in
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|in2
argument_list|,
name|out2
argument_list|)
expr_stmt|;
name|signer
operator|.
name|checkSignature
argument_list|(
name|sm
operator|.
name|message
argument_list|,
name|sm
operator|.
name|signature
argument_list|,
name|out
operator|.
name|masterKeyId
argument_list|)
expr_stmt|;
name|signer
operator|.
name|checkSignature
argument_list|(
name|sm2
operator|.
name|message
argument_list|,
name|sm2
operator|.
name|signature
argument_list|,
name|out2
operator|.
name|masterKeyId
argument_list|)
expr_stmt|;
comment|// Make sure the broken signature doesn't work.
try|try
block|{
name|signer
operator|.
name|checkSignature
argument_list|(
name|sm
operator|.
name|message
argument_list|,
name|sm2
operator|.
name|signature
argument_list|,
name|out
operator|.
name|masterKeyId
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Didn't throw"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|ex
parameter_list|)
block|{
comment|// Expected.
block|}
name|int
name|index
init|=
name|sm
operator|.
name|signature
operator|.
name|length
operator|/
literal|2
decl_stmt|;
name|sm
operator|.
name|signature
index|[
name|index
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|sm
operator|.
name|signature
index|[
name|index
index|]
operator|+
literal|1
argument_list|)
expr_stmt|;
try|try
block|{
name|signer
operator|.
name|checkSignature
argument_list|(
name|sm
operator|.
name|message
argument_list|,
name|sm
operator|.
name|signature
argument_list|,
name|out
operator|.
name|masterKeyId
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Didn't throw"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|ex
parameter_list|)
block|{
comment|// Expected.
block|}
name|sm
operator|.
name|signature
index|[
name|index
index|]
operator|=
call|(
name|byte
call|)
argument_list|(
name|sm
operator|.
name|signature
index|[
name|index
index|]
operator|-
literal|1
argument_list|)
expr_stmt|;
name|fsm
operator|=
name|rollKey
argument_list|(
name|fsm
argument_list|,
name|out
operator|.
name|masterKeyId
argument_list|)
expr_stmt|;
name|signer
operator|=
operator|new
name|LlapSignerImpl
argument_list|(
name|fsm
argument_list|)
expr_stmt|;
comment|// Sign in2 with a different key.
name|sm2
operator|=
name|signer
operator|.
name|serializeAndSign
argument_list|(
name|in2
argument_list|)
expr_stmt|;
name|out2
operator|=
name|TestSignable
operator|.
name|deserialize
argument_list|(
name|sm2
operator|.
name|message
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
name|out
operator|.
name|masterKeyId
argument_list|,
name|out2
operator|.
name|masterKeyId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|in2
argument_list|,
name|out2
argument_list|)
expr_stmt|;
name|signer
operator|.
name|checkSignature
argument_list|(
name|sm2
operator|.
name|message
argument_list|,
name|sm2
operator|.
name|signature
argument_list|,
name|out2
operator|.
name|masterKeyId
argument_list|)
expr_stmt|;
name|signer
operator|.
name|checkSignature
argument_list|(
name|sm
operator|.
name|message
argument_list|,
name|sm
operator|.
name|signature
argument_list|,
name|out
operator|.
name|masterKeyId
argument_list|)
expr_stmt|;
comment|// Make sure the key ID mismatch causes error.
try|try
block|{
name|signer
operator|.
name|checkSignature
argument_list|(
name|sm2
operator|.
name|message
argument_list|,
name|sm2
operator|.
name|signature
argument_list|,
name|out
operator|.
name|masterKeyId
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Didn't throw"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|ex
parameter_list|)
block|{
comment|// Expected.
block|}
comment|// The same for rolling the key; re-create the fsm with only the key #2.
name|fsm
operator|=
name|rollKey
argument_list|(
name|fsm
argument_list|,
name|out2
operator|.
name|masterKeyId
argument_list|)
expr_stmt|;
name|signer
operator|=
operator|new
name|LlapSignerImpl
argument_list|(
name|fsm
argument_list|)
expr_stmt|;
name|signer
operator|.
name|checkSignature
argument_list|(
name|sm2
operator|.
name|message
argument_list|,
name|sm2
operator|.
name|signature
argument_list|,
name|out2
operator|.
name|masterKeyId
argument_list|)
expr_stmt|;
comment|// The key is missing - shouldn't be able to verify.
try|try
block|{
name|signer
operator|.
name|checkSignature
argument_list|(
name|sm
operator|.
name|message
argument_list|,
name|sm
operator|.
name|signature
argument_list|,
name|out
operator|.
name|masterKeyId
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Didn't throw"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|ex
parameter_list|)
block|{
comment|// Expected.
block|}
name|fsm
operator|.
name|stopThreads
argument_list|()
expr_stmt|;
block|}
specifier|private
name|FakeSecretManager
name|rollKey
parameter_list|(
name|FakeSecretManager
name|fsm
parameter_list|,
name|int
name|idToPreserve
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Adding keys is PITA - there's no way to plug into timed rolling; just create a new fsm.
name|DelegationKey
name|dk
init|=
name|fsm
operator|.
name|getDelegationKey
argument_list|(
name|idToPreserve
argument_list|)
decl_stmt|,
name|curDk
init|=
name|fsm
operator|.
name|getCurrentKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|curDk
operator|.
name|getKeyId
argument_list|()
operator|!=
name|idToPreserve
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"The current key is not the one we expect; key rolled in background? Signed with "
operator|+
name|idToPreserve
operator|+
literal|" but got "
operator|+
name|curDk
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Regardless of the above, we should have the key we've signed with.
name|assertNotNull
argument_list|(
name|dk
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|idToPreserve
argument_list|,
name|dk
operator|.
name|getKeyId
argument_list|()
argument_list|)
expr_stmt|;
name|fsm
operator|.
name|stopThreads
argument_list|()
expr_stmt|;
name|fsm
operator|=
operator|new
name|FakeSecretManager
argument_list|()
expr_stmt|;
name|fsm
operator|.
name|addKey
argument_list|(
name|dk
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"Couldn't add key"
argument_list|,
name|fsm
operator|.
name|getDelegationKey
argument_list|(
name|dk
operator|.
name|getKeyId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|fsm
operator|.
name|startThreads
argument_list|()
expr_stmt|;
return|return
name|fsm
return|;
block|}
specifier|private
specifier|static
class|class
name|TestSignable
implements|implements
name|Signable
block|{
specifier|public
name|int
name|masterKeyId
decl_stmt|;
specifier|public
name|byte
name|index
decl_stmt|;
specifier|public
name|TestSignable
parameter_list|(
name|byte
name|i
parameter_list|)
block|{
name|index
operator|=
name|i
expr_stmt|;
block|}
specifier|public
name|TestSignable
parameter_list|(
name|int
name|keyId
parameter_list|,
name|byte
name|b
parameter_list|)
block|{
name|masterKeyId
operator|=
name|keyId
expr_stmt|;
name|index
operator|=
name|b
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setSignInfo
parameter_list|(
name|int
name|masterKeyId
parameter_list|)
block|{
name|this
operator|.
name|masterKeyId
operator|=
name|masterKeyId
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|serialize
parameter_list|()
throws|throws
name|IOException
block|{
name|DataOutputBuffer
name|dob
init|=
operator|new
name|DataOutputBuffer
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|dob
operator|.
name|writeInt
argument_list|(
name|masterKeyId
argument_list|)
expr_stmt|;
name|dob
operator|.
name|write
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|byte
index|[]
name|b
init|=
name|dob
operator|.
name|getData
argument_list|()
decl_stmt|;
name|dob
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|b
return|;
block|}
specifier|public
specifier|static
name|TestSignable
name|deserialize
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|IOException
block|{
name|DataInputBuffer
name|db
init|=
operator|new
name|DataInputBuffer
argument_list|()
decl_stmt|;
name|db
operator|.
name|reset
argument_list|(
name|bytes
argument_list|,
name|bytes
operator|.
name|length
argument_list|)
expr_stmt|;
name|int
name|keyId
init|=
name|db
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|byte
name|b
init|=
name|db
operator|.
name|readByte
argument_list|()
decl_stmt|;
name|db
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
operator|new
name|TestSignable
argument_list|(
name|keyId
argument_list|,
name|b
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
literal|31
operator|*
name|index
operator|+
name|masterKeyId
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|obj
operator|instanceof
name|TestSignable
operator|)
condition|)
return|return
literal|false
return|;
name|TestSignable
name|other
init|=
operator|(
name|TestSignable
operator|)
name|obj
decl_stmt|;
return|return
operator|(
name|index
operator|==
name|other
operator|.
name|index
operator|)
operator|&&
operator|(
name|masterKeyId
operator|==
name|other
operator|.
name|masterKeyId
operator|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|FakeSecretManager
extends|extends
name|AbstractDelegationTokenSecretManager
argument_list|<
name|AbstractDelegationTokenIdentifier
argument_list|>
implements|implements
name|SigningSecretManager
block|{
specifier|public
name|FakeSecretManager
parameter_list|()
block|{
name|super
argument_list|(
literal|10000000
argument_list|,
literal|10000000
argument_list|,
literal|10000000
argument_list|,
literal|10000000
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|DelegationKey
name|getCurrentKey
parameter_list|()
block|{
return|return
name|getDelegationKey
argument_list|(
name|getCurrentKeyId
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|DelegationKey
name|getDelegationKey
parameter_list|(
name|int
name|keyId
parameter_list|)
block|{
return|return
name|super
operator|.
name|getDelegationKey
argument_list|(
name|keyId
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|signWithKey
parameter_list|(
name|byte
index|[]
name|message
parameter_list|,
name|DelegationKey
name|key
parameter_list|)
block|{
return|return
name|createPassword
argument_list|(
name|message
argument_list|,
name|key
operator|.
name|getKey
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|signWithKey
parameter_list|(
name|byte
index|[]
name|message
parameter_list|,
name|int
name|keyId
parameter_list|)
throws|throws
name|SecurityException
block|{
name|DelegationKey
name|key
init|=
name|getDelegationKey
argument_list|(
name|keyId
argument_list|)
decl_stmt|;
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SecurityException
argument_list|(
literal|"The key ID "
operator|+
name|keyId
operator|+
literal|" was not found"
argument_list|)
throw|;
block|}
return|return
name|createPassword
argument_list|(
name|message
argument_list|,
name|key
operator|.
name|getKey
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AbstractDelegationTokenIdentifier
name|createIdentifier
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|stopThreads
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

