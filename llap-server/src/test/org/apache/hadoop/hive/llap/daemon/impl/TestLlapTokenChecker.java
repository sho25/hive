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
name|daemon
operator|.
name|impl
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Text
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
name|lang3
operator|.
name|tuple
operator|.
name|Pair
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
name|LlapTokenIdentifier
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
name|List
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

begin_class
specifier|public
class|class
name|TestLlapTokenChecker
block|{
annotation|@
name|Test
specifier|public
name|void
name|testGetToken
parameter_list|()
block|{
name|check
argument_list|(
name|LlapTokenChecker
operator|.
name|getTokenInfoInternal
argument_list|(
literal|"u"
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|"u"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|check
argument_list|(
name|LlapTokenChecker
operator|.
name|getTokenInfoInternal
argument_list|(
literal|null
argument_list|,
name|createTokens
argument_list|(
literal|"u"
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|,
literal|"u"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|check
argument_list|(
name|LlapTokenChecker
operator|.
name|getTokenInfoInternal
argument_list|(
literal|null
argument_list|,
name|createTokens
argument_list|(
literal|"u"
argument_list|,
literal|"a"
argument_list|)
argument_list|)
argument_list|,
literal|"u"
argument_list|,
literal|"a"
argument_list|)
expr_stmt|;
name|check
argument_list|(
name|LlapTokenChecker
operator|.
name|getTokenInfoInternal
argument_list|(
literal|"u"
argument_list|,
name|createTokens
argument_list|(
literal|"u"
argument_list|,
literal|"a"
argument_list|)
argument_list|)
argument_list|,
literal|"u"
argument_list|,
literal|"a"
argument_list|)
expr_stmt|;
name|check
argument_list|(
name|LlapTokenChecker
operator|.
name|getTokenInfoInternal
argument_list|(
literal|"u"
argument_list|,
name|createTokens
argument_list|(
literal|"u"
argument_list|,
literal|"a"
argument_list|,
literal|"u"
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|,
literal|"u"
argument_list|,
literal|"a"
argument_list|)
expr_stmt|;
comment|// Note - some of these scenarios could be handled, but they are not supported right now.
comment|// The reason is that we bind a query to app/user using the signed token information, and
comment|// we don't want to bother figuring out which one to use in case of ambiguity w/o a use case.
name|checkGetThrows
argument_list|(
literal|"u"
argument_list|,
name|createTokens
argument_list|(
literal|"u2"
argument_list|,
literal|"a"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Ambiguous user.
name|checkGetThrows
argument_list|(
literal|"u2"
argument_list|,
name|createTokens
argument_list|(
literal|"u2"
argument_list|,
literal|"a"
argument_list|,
literal|"u3"
argument_list|,
literal|"a"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Ambiguous user.
name|checkGetThrows
argument_list|(
literal|null
argument_list|,
name|createTokens
argument_list|(
literal|"u2"
argument_list|,
literal|"a"
argument_list|,
literal|"u3"
argument_list|,
literal|"a"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Ambiguous user.
name|checkGetThrows
argument_list|(
literal|null
argument_list|,
name|createTokens
argument_list|(
literal|"u2"
argument_list|,
literal|"a"
argument_list|,
literal|"u2"
argument_list|,
literal|"a1"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Ambiguous app.
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCheckPermissions
parameter_list|()
block|{
name|LlapTokenChecker
operator|.
name|checkPermissionsInternal
argument_list|(
literal|"u"
argument_list|,
literal|null
argument_list|,
literal|"u"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|LlapTokenChecker
operator|.
name|checkPermissionsInternal
argument_list|(
literal|null
argument_list|,
name|createTokens
argument_list|(
literal|"u"
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|"u"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|LlapTokenChecker
operator|.
name|checkPermissionsInternal
argument_list|(
literal|"u"
argument_list|,
name|createTokens
argument_list|(
literal|"u"
argument_list|,
literal|"a"
argument_list|)
argument_list|,
literal|"u"
argument_list|,
literal|"a"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// No access.
name|checkPrmThrows
argument_list|(
literal|"u2"
argument_list|,
literal|null
argument_list|,
literal|"u"
argument_list|,
literal|"a"
argument_list|)
expr_stmt|;
name|checkPrmThrows
argument_list|(
literal|"u"
argument_list|,
literal|null
argument_list|,
literal|"u"
argument_list|,
literal|"a"
argument_list|)
expr_stmt|;
comment|// Note - Kerberos user w/o appId doesn't have access.
name|checkPrmThrows
argument_list|(
literal|null
argument_list|,
name|createTokens
argument_list|(
literal|"u2"
argument_list|,
literal|"a"
argument_list|)
argument_list|,
literal|"u"
argument_list|,
literal|"a"
argument_list|)
expr_stmt|;
name|checkPrmThrows
argument_list|(
literal|null
argument_list|,
name|createTokens
argument_list|(
literal|"u"
argument_list|,
literal|"a2"
argument_list|)
argument_list|,
literal|"u"
argument_list|,
literal|"a"
argument_list|)
expr_stmt|;
name|checkPrmThrows
argument_list|(
literal|null
argument_list|,
name|createTokens
argument_list|(
literal|"u"
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|"u"
argument_list|,
literal|"a"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|createTokens
parameter_list|(
name|String
modifier|...
name|args
parameter_list|)
block|{
name|List
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|tokens
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|args
operator|.
name|length
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|tokens
operator|.
name|add
argument_list|(
operator|new
name|LlapTokenIdentifier
argument_list|(
operator|new
name|Text
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|"c"
argument_list|,
name|args
index|[
name|i
operator|+
literal|1
index|]
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|tokens
return|;
block|}
specifier|private
name|void
name|checkGetThrows
parameter_list|(
name|String
name|kerberosName
parameter_list|,
name|List
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|tokens
parameter_list|)
block|{
try|try
block|{
name|LlapTokenChecker
operator|.
name|getTokenInfoInternal
argument_list|(
name|kerberosName
argument_list|,
name|tokens
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
block|}
specifier|private
name|void
name|checkPrmThrows
parameter_list|(
name|String
name|kerberosName
parameter_list|,
name|List
argument_list|<
name|LlapTokenIdentifier
argument_list|>
name|tokens
parameter_list|,
name|String
name|userName
parameter_list|,
name|String
name|appId
parameter_list|)
block|{
try|try
block|{
name|LlapTokenChecker
operator|.
name|checkPermissionsInternal
argument_list|(
name|kerberosName
argument_list|,
name|tokens
argument_list|,
name|userName
argument_list|,
name|appId
argument_list|,
literal|null
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
block|}
specifier|private
name|void
name|check
parameter_list|(
name|LlapTokenChecker
operator|.
name|LlapTokenInfo
name|p
parameter_list|,
name|String
name|user
parameter_list|,
name|String
name|appId
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|user
argument_list|,
name|p
operator|.
name|userName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|appId
argument_list|,
name|p
operator|.
name|appId
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

