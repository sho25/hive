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
operator|.
name|auth
package|;
end_package

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessControlContext
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessController
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedExceptionAction
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
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|StringTokenizer
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|Subject
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
name|shims
operator|.
name|ShimLoader
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
name|HttpContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|GSSContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|GSSManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|GSSName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|ietf
operator|.
name|jgss
operator|.
name|Oid
import|;
end_import

begin_comment
comment|/**  * Utility functions for HTTP mode authentication.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|HttpAuthUtils
block|{
specifier|public
specifier|static
specifier|final
name|String
name|WWW_AUTHENTICATE
init|=
literal|"WWW-Authenticate"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|AUTHORIZATION
init|=
literal|"Authorization"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|BASIC
init|=
literal|"Basic"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|NEGOTIATE
init|=
literal|"Negotiate"
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
name|HttpAuthUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COOKIE_ATTR_SEPARATOR
init|=
literal|"&"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COOKIE_CLIENT_USER_NAME
init|=
literal|"cu"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COOKIE_CLIENT_RAND_NUMBER
init|=
literal|"rn"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COOKIE_KEY_VALUE_SEPARATOR
init|=
literal|"="
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|COOKIE_ATTRIBUTES
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|COOKIE_CLIENT_USER_NAME
argument_list|,
name|COOKIE_CLIENT_RAND_NUMBER
argument_list|)
argument_list|)
decl_stmt|;
comment|/**    * @return Stringified Base64 encoded kerberosAuthHeader on success    * @throws Exception    */
specifier|public
specifier|static
name|String
name|getKerberosServiceTicket
parameter_list|(
name|String
name|principal
parameter_list|,
name|String
name|host
parameter_list|,
name|String
name|serverHttpUrl
parameter_list|,
name|boolean
name|assumeSubject
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|serverPrincipal
init|=
name|ShimLoader
operator|.
name|getHadoopThriftAuthBridge
argument_list|()
operator|.
name|getServerPrincipal
argument_list|(
name|principal
argument_list|,
name|host
argument_list|)
decl_stmt|;
if|if
condition|(
name|assumeSubject
condition|)
block|{
comment|// With this option, we're assuming that the external application,
comment|// using the JDBC driver has done a JAAS kerberos login already
name|AccessControlContext
name|context
init|=
name|AccessController
operator|.
name|getContext
argument_list|()
decl_stmt|;
name|Subject
name|subject
init|=
name|Subject
operator|.
name|getSubject
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|subject
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"The Subject is not set"
argument_list|)
throw|;
block|}
return|return
name|Subject
operator|.
name|doAs
argument_list|(
name|subject
argument_list|,
operator|new
name|HttpKerberosClientAction
argument_list|(
name|serverPrincipal
argument_list|,
name|serverHttpUrl
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
comment|// JAAS login from ticket cache to setup the client UserGroupInformation
name|UserGroupInformation
name|clientUGI
init|=
name|ShimLoader
operator|.
name|getHadoopThriftAuthBridge
argument_list|()
operator|.
name|getCurrentUGIWithConf
argument_list|(
literal|"kerberos"
argument_list|)
decl_stmt|;
return|return
name|clientUGI
operator|.
name|doAs
argument_list|(
operator|new
name|HttpKerberosClientAction
argument_list|(
name|serverPrincipal
argument_list|,
name|serverHttpUrl
argument_list|)
argument_list|)
return|;
block|}
block|}
comment|/**    * Creates and returns a HS2 cookie token.    * @param clientUserName Client User name.    * @return An unsigned cookie token generated from input parameters.    * The final cookie generated is of the following format :    * cu=<username>&rn=<randomNumber>&s=<cookieSignature>    */
specifier|public
specifier|static
name|String
name|createCookieToken
parameter_list|(
name|String
name|clientUserName
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COOKIE_CLIENT_USER_NAME
argument_list|)
operator|.
name|append
argument_list|(
name|COOKIE_KEY_VALUE_SEPARATOR
argument_list|)
operator|.
name|append
argument_list|(
name|clientUserName
argument_list|)
operator|.
name|append
argument_list|(
name|COOKIE_ATTR_SEPARATOR
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|COOKIE_CLIENT_RAND_NUMBER
argument_list|)
operator|.
name|append
argument_list|(
name|COOKIE_KEY_VALUE_SEPARATOR
argument_list|)
operator|.
name|append
argument_list|(
operator|(
operator|new
name|Random
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|)
operator|.
name|nextLong
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Parses a cookie token to retrieve client user name.    * @param tokenStr Token String.    * @return A valid user name if input is of valid format, else returns null.    */
specifier|public
specifier|static
name|String
name|getUserNameFromCookieToken
parameter_list|(
name|String
name|tokenStr
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
name|splitCookieToken
argument_list|(
name|tokenStr
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|map
operator|.
name|keySet
argument_list|()
operator|.
name|equals
argument_list|(
name|COOKIE_ATTRIBUTES
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Invalid token with missing attributes "
operator|+
name|tokenStr
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|map
operator|.
name|get
argument_list|(
name|COOKIE_CLIENT_USER_NAME
argument_list|)
return|;
block|}
comment|/**    * Splits the cookie token into attributes pairs.    * @param str input token.    * @return a map with the attribute pairs of the token if the input is valid.    * Else, returns null.    */
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|splitCookieToken
parameter_list|(
name|String
name|tokenStr
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|StringTokenizer
name|st
init|=
operator|new
name|StringTokenizer
argument_list|(
name|tokenStr
argument_list|,
name|COOKIE_ATTR_SEPARATOR
argument_list|)
decl_stmt|;
while|while
condition|(
name|st
operator|.
name|hasMoreTokens
argument_list|()
condition|)
block|{
name|String
name|part
init|=
name|st
operator|.
name|nextToken
argument_list|()
decl_stmt|;
name|int
name|separator
init|=
name|part
operator|.
name|indexOf
argument_list|(
name|COOKIE_KEY_VALUE_SEPARATOR
argument_list|)
decl_stmt|;
if|if
condition|(
name|separator
operator|==
operator|-
literal|1
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Invalid token string "
operator|+
name|tokenStr
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|String
name|key
init|=
name|part
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|separator
argument_list|)
decl_stmt|;
name|String
name|value
init|=
name|part
operator|.
name|substring
argument_list|(
name|separator
operator|+
literal|1
argument_list|)
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|map
return|;
block|}
specifier|private
name|HttpAuthUtils
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Can't initialize class"
argument_list|)
throw|;
block|}
comment|/**    * We'll create an instance of this class within a doAs block so that the client's TGT credentials    * can be read from the Subject    */
specifier|public
specifier|static
class|class
name|HttpKerberosClientAction
implements|implements
name|PrivilegedExceptionAction
argument_list|<
name|String
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|String
name|HTTP_RESPONSE
init|=
literal|"HTTP_RESPONSE"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SERVER_HTTP_URL
init|=
literal|"SERVER_HTTP_URL"
decl_stmt|;
specifier|private
specifier|final
name|String
name|serverPrincipal
decl_stmt|;
specifier|private
specifier|final
name|String
name|serverHttpUrl
decl_stmt|;
specifier|private
specifier|final
name|Base64
name|base64codec
decl_stmt|;
specifier|private
specifier|final
name|HttpContext
name|httpContext
decl_stmt|;
specifier|public
name|HttpKerberosClientAction
parameter_list|(
name|String
name|serverPrincipal
parameter_list|,
name|String
name|serverHttpUrl
parameter_list|)
block|{
name|this
operator|.
name|serverPrincipal
operator|=
name|serverPrincipal
expr_stmt|;
name|this
operator|.
name|serverHttpUrl
operator|=
name|serverHttpUrl
expr_stmt|;
name|base64codec
operator|=
operator|new
name|Base64
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|httpContext
operator|=
operator|new
name|BasicHttpContext
argument_list|()
expr_stmt|;
name|httpContext
operator|.
name|setAttribute
argument_list|(
name|SERVER_HTTP_URL
argument_list|,
name|serverHttpUrl
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|run
parameter_list|()
throws|throws
name|Exception
block|{
comment|// This Oid for Kerberos GSS-API mechanism.
name|Oid
name|mechOid
init|=
operator|new
name|Oid
argument_list|(
literal|"1.2.840.113554.1.2.2"
argument_list|)
decl_stmt|;
comment|// Oid for kerberos principal name
name|Oid
name|krb5PrincipalOid
init|=
operator|new
name|Oid
argument_list|(
literal|"1.2.840.113554.1.2.2.1"
argument_list|)
decl_stmt|;
name|GSSManager
name|manager
init|=
name|GSSManager
operator|.
name|getInstance
argument_list|()
decl_stmt|;
comment|// GSS name for server
name|GSSName
name|serverName
init|=
name|manager
operator|.
name|createName
argument_list|(
name|serverPrincipal
argument_list|,
name|krb5PrincipalOid
argument_list|)
decl_stmt|;
comment|// Create a GSSContext for authentication with the service.
comment|// We're passing client credentials as null since we want them to be read from the Subject.
name|GSSContext
name|gssContext
init|=
name|manager
operator|.
name|createContext
argument_list|(
name|serverName
argument_list|,
name|mechOid
argument_list|,
literal|null
argument_list|,
name|GSSContext
operator|.
name|DEFAULT_LIFETIME
argument_list|)
decl_stmt|;
name|gssContext
operator|.
name|requestMutualAuth
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Establish context
name|byte
index|[]
name|inToken
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|outToken
init|=
name|gssContext
operator|.
name|initSecContext
argument_list|(
name|inToken
argument_list|,
literal|0
argument_list|,
name|inToken
operator|.
name|length
argument_list|)
decl_stmt|;
name|gssContext
operator|.
name|dispose
argument_list|()
expr_stmt|;
comment|// Base64 encoded and stringified token for server
return|return
operator|new
name|String
argument_list|(
name|base64codec
operator|.
name|encode
argument_list|(
name|outToken
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

