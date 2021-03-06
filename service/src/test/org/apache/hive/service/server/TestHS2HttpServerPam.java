begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|server
package|;
end_package

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
name|hive
operator|.
name|metastore
operator|.
name|MetaStoreTestUtils
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
name|security
operator|.
name|PamAuthenticator
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
name|security
operator|.
name|PamUserIdentity
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
name|CloseableHttpResponse
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
name|HttpGet
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
name|CloseableHttpClient
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
name|HttpClients
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|http
operator|.
name|HttpHeader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|server
operator|.
name|UserIdentity
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|util
operator|.
name|B64Code
import|;
end_import

begin_import
import|import
name|org
operator|.
name|eclipse
operator|.
name|jetty
operator|.
name|util
operator|.
name|StringUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|javax
operator|.
name|security
operator|.
name|sasl
operator|.
name|AuthenticationException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
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
name|java
operator|.
name|net
operator|.
name|HttpURLConnection
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
name|security
operator|.
name|KeyStore
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|KeyStoreException
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
name|java
operator|.
name|security
operator|.
name|cert
operator|.
name|Certificate
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|cert
operator|.
name|CertificateException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|cert
operator|.
name|CertificateFactory
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
name|Map
import|;
end_import

begin_comment
comment|/**  * TestHS2HttpServerPam -- executes tests of HiveServer2 HTTP Server for Pam authentication  */
end_comment

begin_class
specifier|public
class|class
name|TestHS2HttpServerPam
block|{
specifier|private
specifier|static
name|HiveServer2
name|hiveServer2
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|HiveConf
name|hiveConf
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|String
name|metastorePasswd
init|=
literal|"693efe9fa425ad21886d73a0fa3fbc70"
decl_stmt|;
comment|//random md5
specifier|private
specifier|static
name|Integer
name|webUIPort
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|String
name|host
init|=
literal|"localhost"
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeTests
parameter_list|()
throws|throws
name|Exception
block|{
name|webUIPort
operator|=
name|MetaStoreTestUtils
operator|.
name|findFreePortExcepting
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_PORT
operator|.
name|getDefaultValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|hiveConf
operator|=
operator|new
name|HiveConf
argument_list|()
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_IN_TEST
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|METASTOREPWD
operator|.
name|varname
argument_list|,
name|metastorePasswd
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_PORT
operator|.
name|varname
argument_list|,
name|webUIPort
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_PAM_SERVICES
argument_list|,
literal|"sshd"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_WEBUI_USE_PAM
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setBoolVar
argument_list|(
name|ConfVars
operator|.
name|HIVE_IN_TEST
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|hiveServer2
operator|=
operator|new
name|HiveServer2
argument_list|(
operator|new
name|TestPamAuthenticator
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|hiveServer2
operator|.
name|init
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|hiveServer2
operator|.
name|start
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUnauthorizedConnection
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|baseURL
init|=
literal|"http://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|webUIPort
operator|+
literal|"/stacks"
decl_stmt|;
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|baseURL
argument_list|)
decl_stmt|;
name|HttpURLConnection
name|conn
init|=
operator|(
name|HttpURLConnection
operator|)
name|url
operator|.
name|openConnection
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HttpURLConnection
operator|.
name|HTTP_UNAUTHORIZED
argument_list|,
name|conn
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAuthorizedConnection
parameter_list|()
throws|throws
name|Exception
block|{
name|CloseableHttpClient
name|httpclient
init|=
literal|null
decl_stmt|;
try|try
block|{
name|String
name|username
init|=
literal|"user1"
decl_stmt|;
name|String
name|password
init|=
literal|"1"
decl_stmt|;
name|httpclient
operator|=
name|HttpClients
operator|.
name|createDefault
argument_list|()
expr_stmt|;
name|HttpGet
name|httpGet
init|=
operator|new
name|HttpGet
argument_list|(
literal|"http://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|webUIPort
argument_list|)
decl_stmt|;
name|String
name|authB64Code
init|=
name|B64Code
operator|.
name|encode
argument_list|(
name|username
operator|+
literal|":"
operator|+
name|password
argument_list|,
name|StringUtil
operator|.
name|__ISO_8859_1
argument_list|)
decl_stmt|;
name|httpGet
operator|.
name|setHeader
argument_list|(
name|HttpHeader
operator|.
name|AUTHORIZATION
operator|.
name|asString
argument_list|()
argument_list|,
literal|"Basic "
operator|+
name|authB64Code
argument_list|)
expr_stmt|;
name|CloseableHttpResponse
name|response
init|=
name|httpclient
operator|.
name|execute
argument_list|(
name|httpGet
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|response
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|HttpURLConnection
operator|.
name|HTTP_OK
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|httpclient
operator|!=
literal|null
condition|)
block|{
name|httpclient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIncorrectUser
parameter_list|()
throws|throws
name|Exception
block|{
name|CloseableHttpClient
name|httpclient
init|=
literal|null
decl_stmt|;
try|try
block|{
name|String
name|username
init|=
literal|"nouser"
decl_stmt|;
name|String
name|password
init|=
literal|"aaaa"
decl_stmt|;
name|httpclient
operator|=
name|HttpClients
operator|.
name|createDefault
argument_list|()
expr_stmt|;
name|HttpGet
name|httpGet
init|=
operator|new
name|HttpGet
argument_list|(
literal|"http://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|webUIPort
argument_list|)
decl_stmt|;
name|String
name|authB64Code
init|=
name|B64Code
operator|.
name|encode
argument_list|(
name|username
operator|+
literal|":"
operator|+
name|password
argument_list|,
name|StringUtil
operator|.
name|__ISO_8859_1
argument_list|)
decl_stmt|;
name|httpGet
operator|.
name|setHeader
argument_list|(
name|HttpHeader
operator|.
name|AUTHORIZATION
operator|.
name|asString
argument_list|()
argument_list|,
literal|"Basic "
operator|+
name|authB64Code
argument_list|)
expr_stmt|;
name|CloseableHttpResponse
name|response
init|=
name|httpclient
operator|.
name|execute
argument_list|(
name|httpGet
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|response
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|HttpURLConnection
operator|.
name|HTTP_UNAUTHORIZED
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|httpclient
operator|!=
literal|null
condition|)
block|{
name|httpclient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIncorrectPassword
parameter_list|()
throws|throws
name|Exception
block|{
name|CloseableHttpClient
name|httpclient
init|=
literal|null
decl_stmt|;
try|try
block|{
name|String
name|username
init|=
literal|"user1"
decl_stmt|;
name|String
name|password
init|=
literal|"aaaa"
decl_stmt|;
name|httpclient
operator|=
name|HttpClients
operator|.
name|createDefault
argument_list|()
expr_stmt|;
name|HttpGet
name|httpGet
init|=
operator|new
name|HttpGet
argument_list|(
literal|"http://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|webUIPort
argument_list|)
decl_stmt|;
name|String
name|authB64Code
init|=
name|B64Code
operator|.
name|encode
argument_list|(
name|username
operator|+
literal|":"
operator|+
name|password
argument_list|,
name|StringUtil
operator|.
name|__ISO_8859_1
argument_list|)
decl_stmt|;
name|httpGet
operator|.
name|setHeader
argument_list|(
name|HttpHeader
operator|.
name|AUTHORIZATION
operator|.
name|asString
argument_list|()
argument_list|,
literal|"Basic "
operator|+
name|authB64Code
argument_list|)
expr_stmt|;
name|CloseableHttpResponse
name|response
init|=
name|httpclient
operator|.
name|execute
argument_list|(
name|httpGet
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|response
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|HttpURLConnection
operator|.
name|HTTP_UNAUTHORIZED
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|httpclient
operator|!=
literal|null
condition|)
block|{
name|httpclient
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|TestPamAuthenticator
extends|extends
name|PamAuthenticator
block|{
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|users
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|TestPamAuthenticator
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|AuthenticationException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
static|static
block|{
name|users
operator|.
name|put
argument_list|(
literal|"user1"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|users
operator|.
name|put
argument_list|(
literal|"user2"
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
name|users
operator|.
name|put
argument_list|(
literal|"user3"
argument_list|,
literal|"3"
argument_list|)
expr_stmt|;
name|users
operator|.
name|put
argument_list|(
literal|"user4"
argument_list|,
literal|"4"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|UserIdentity
name|login
parameter_list|(
name|String
name|username
parameter_list|,
name|String
name|password
parameter_list|)
block|{
if|if
condition|(
name|users
operator|.
name|containsKey
argument_list|(
name|username
argument_list|)
condition|)
block|{
if|if
condition|(
name|users
operator|.
name|get
argument_list|(
name|username
argument_list|)
operator|.
name|equals
argument_list|(
name|password
argument_list|)
condition|)
block|{
return|return
operator|new
name|PamUserIdentity
argument_list|(
name|username
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterTests
parameter_list|()
block|{
name|hiveServer2
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

