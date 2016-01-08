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
name|hive
operator|.
name|service
package|;
end_package

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|MessageDigest
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|NoSuchAlgorithmException
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
name|codec
operator|.
name|binary
operator|.
name|Base64
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
comment|/**  * The cookie signer generates a signature based on SHA digest  * and appends it to the cookie value generated at the  * server side. It uses SHA digest algorithm to sign and verify signatures.  */
end_comment

begin_class
specifier|public
class|class
name|CookieSigner
block|{
specifier|private
specifier|static
specifier|final
name|String
name|SIGNATURE
init|=
literal|"&s="
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SHA_STRING
init|=
literal|"SHA"
decl_stmt|;
specifier|private
name|byte
index|[]
name|secretBytes
decl_stmt|;
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
name|CookieSigner
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Constructor    * @param secret Secret Bytes    */
specifier|public
name|CookieSigner
parameter_list|(
name|byte
index|[]
name|secret
parameter_list|)
block|{
if|if
condition|(
name|secret
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|" NULL Secret Bytes"
argument_list|)
throw|;
block|}
name|this
operator|.
name|secretBytes
operator|=
name|secret
operator|.
name|clone
argument_list|()
expr_stmt|;
block|}
comment|/**    * Sign the cookie given the string token as input.    * @param str Input token    * @return Signed token that can be used to create a cookie    */
specifier|public
name|String
name|signCookie
parameter_list|(
name|String
name|str
parameter_list|)
block|{
if|if
condition|(
name|str
operator|==
literal|null
operator|||
name|str
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"NULL or empty string to sign"
argument_list|)
throw|;
block|}
name|String
name|signature
init|=
name|getSignature
argument_list|(
name|str
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Signature generated for "
operator|+
name|str
operator|+
literal|" is "
operator|+
name|signature
argument_list|)
expr_stmt|;
block|}
return|return
name|str
operator|+
name|SIGNATURE
operator|+
name|signature
return|;
block|}
comment|/**    * Verify a signed string and extracts the original string.    * @param signedStr The already signed string    * @return Raw Value of the string without the signature    */
specifier|public
name|String
name|verifyAndExtract
parameter_list|(
name|String
name|signedStr
parameter_list|)
block|{
name|int
name|index
init|=
name|signedStr
operator|.
name|lastIndexOf
argument_list|(
name|SIGNATURE
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid input sign: "
operator|+
name|signedStr
argument_list|)
throw|;
block|}
name|String
name|originalSignature
init|=
name|signedStr
operator|.
name|substring
argument_list|(
name|index
operator|+
name|SIGNATURE
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|rawValue
init|=
name|signedStr
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
decl_stmt|;
name|String
name|currentSignature
init|=
name|getSignature
argument_list|(
name|rawValue
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Signature generated for "
operator|+
name|rawValue
operator|+
literal|" inside verify is "
operator|+
name|currentSignature
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|originalSignature
operator|.
name|equals
argument_list|(
name|currentSignature
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid sign, original = "
operator|+
name|originalSignature
operator|+
literal|" current = "
operator|+
name|currentSignature
argument_list|)
throw|;
block|}
return|return
name|rawValue
return|;
block|}
comment|/**    * Get the signature of the input string based on SHA digest algorithm.    * @param str Input token    * @return Signed String    */
specifier|private
name|String
name|getSignature
parameter_list|(
name|String
name|str
parameter_list|)
block|{
try|try
block|{
name|MessageDigest
name|md
init|=
name|MessageDigest
operator|.
name|getInstance
argument_list|(
name|SHA_STRING
argument_list|)
decl_stmt|;
name|md
operator|.
name|update
argument_list|(
name|str
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|md
operator|.
name|update
argument_list|(
name|secretBytes
argument_list|)
expr_stmt|;
name|byte
index|[]
name|digest
init|=
name|md
operator|.
name|digest
argument_list|()
decl_stmt|;
return|return
operator|new
name|Base64
argument_list|(
literal|0
argument_list|)
operator|.
name|encodeToString
argument_list|(
name|digest
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid SHA digest String: "
operator|+
name|SHA_STRING
operator|+
literal|" "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

