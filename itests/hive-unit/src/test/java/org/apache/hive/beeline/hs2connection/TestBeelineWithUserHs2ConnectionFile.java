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
name|beeline
operator|.
name|hs2connection
package|;
end_package

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
name|net
operator|.
name|URI
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
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestBeelineWithUserHs2ConnectionFile
extends|extends
name|BeelineWithHS2ConnectionFileTestBase
block|{
annotation|@
name|Test
specifier|public
name|void
name|testBeelineConnectionHttp
parameter_list|()
throws|throws
name|Exception
block|{
name|setupHttpHs2
argument_list|()
expr_stmt|;
name|String
name|path
init|=
name|createHttpHs2ConnectionFile
argument_list|()
decl_stmt|;
name|assertBeelineOutputContains
argument_list|(
name|path
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-e"
block|,
literal|"show tables;"
block|}
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setupHttpHs2
parameter_list|()
throws|throws
name|Exception
block|{
name|confOverlay
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_TRANSPORT_MODE
operator|.
name|varname
argument_list|,
name|HS2_HTTP_MODE
argument_list|)
expr_stmt|;
name|confOverlay
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_THRIFT_HTTP_PATH
operator|.
name|varname
argument_list|,
name|HS2_HTTP_ENDPOINT
argument_list|)
expr_stmt|;
name|confOverlay
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
operator|.
name|varname
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|miniHS2
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
name|createTable
argument_list|()
expr_stmt|;
block|}
specifier|private
name|String
name|createHttpHs2ConnectionFile
parameter_list|()
throws|throws
name|Exception
block|{
name|Hs2ConnectionXmlConfigFileWriter
name|writer
init|=
operator|new
name|Hs2ConnectionXmlConfigFileWriter
argument_list|()
decl_stmt|;
name|String
name|baseJdbcURL
init|=
name|miniHS2
operator|.
name|getBaseJdbcURL
argument_list|()
decl_stmt|;
name|URI
name|uri
init|=
operator|new
name|URI
argument_list|(
name|baseJdbcURL
operator|.
name|substring
argument_list|(
literal|5
argument_list|)
argument_list|)
decl_stmt|;
name|writer
operator|.
name|writeProperty
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|BEELINE_CONNECTION_PROPERTY_PREFIX
operator|+
literal|"hosts"
argument_list|,
name|uri
operator|.
name|getHost
argument_list|()
operator|+
literal|":"
operator|+
name|uri
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|writeProperty
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|BEELINE_CONNECTION_PROPERTY_PREFIX
operator|+
literal|"user"
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|writeProperty
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|BEELINE_CONNECTION_PROPERTY_PREFIX
operator|+
literal|"password"
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|writeProperty
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|BEELINE_CONNECTION_PROPERTY_PREFIX
operator|+
literal|"transportMode"
argument_list|,
name|HS2_HTTP_MODE
argument_list|)
expr_stmt|;
name|writer
operator|.
name|writeProperty
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|BEELINE_CONNECTION_PROPERTY_PREFIX
operator|+
literal|"httpPath"
argument_list|,
name|HS2_HTTP_ENDPOINT
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|writer
operator|.
name|path
argument_list|()
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBeelineConnectionNoAuth
parameter_list|()
throws|throws
name|Exception
block|{
name|setupNoAuthConfHS2
argument_list|()
expr_stmt|;
name|String
name|path
init|=
name|createNoAuthHs2ConnectionFile
argument_list|()
decl_stmt|;
name|assertBeelineOutputContains
argument_list|(
name|path
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-e"
block|,
literal|"show tables;"
block|}
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|setupNoAuthConfHS2
parameter_list|()
throws|throws
name|Exception
block|{
comment|// use default configuration for no-auth mode
name|miniHS2
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
name|createTable
argument_list|()
expr_stmt|;
block|}
specifier|private
name|String
name|createNoAuthHs2ConnectionFile
parameter_list|()
throws|throws
name|Exception
block|{
name|Hs2ConnectionXmlConfigFileWriter
name|writer
init|=
operator|new
name|Hs2ConnectionXmlConfigFileWriter
argument_list|()
decl_stmt|;
name|String
name|baseJdbcURL
init|=
name|miniHS2
operator|.
name|getBaseJdbcURL
argument_list|()
decl_stmt|;
name|URI
name|uri
init|=
operator|new
name|URI
argument_list|(
name|baseJdbcURL
operator|.
name|substring
argument_list|(
literal|5
argument_list|)
argument_list|)
decl_stmt|;
name|writer
operator|.
name|writeProperty
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|BEELINE_CONNECTION_PROPERTY_PREFIX
operator|+
literal|"hosts"
argument_list|,
name|uri
operator|.
name|getHost
argument_list|()
operator|+
literal|":"
operator|+
name|uri
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|writeProperty
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|BEELINE_CONNECTION_PROPERTY_PREFIX
operator|+
literal|"user"
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|writeProperty
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|BEELINE_CONNECTION_PROPERTY_PREFIX
operator|+
literal|"password"
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|writer
operator|.
name|path
argument_list|()
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBeelineConnectionSSL
parameter_list|()
throws|throws
name|Exception
block|{
name|setupSslHs2
argument_list|()
expr_stmt|;
name|String
name|path
init|=
name|createSSLHs2ConnectionFile
argument_list|()
decl_stmt|;
name|assertBeelineOutputContains
argument_list|(
name|path
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-e"
block|,
literal|"show tables;"
block|}
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|createSSLHs2ConnectionFile
parameter_list|()
throws|throws
name|Exception
block|{
name|Hs2ConnectionXmlConfigFileWriter
name|writer
init|=
operator|new
name|Hs2ConnectionXmlConfigFileWriter
argument_list|()
decl_stmt|;
name|String
name|baseJdbcURL
init|=
name|miniHS2
operator|.
name|getBaseJdbcURL
argument_list|()
decl_stmt|;
name|URI
name|uri
init|=
operator|new
name|URI
argument_list|(
name|baseJdbcURL
operator|.
name|substring
argument_list|(
literal|5
argument_list|)
argument_list|)
decl_stmt|;
name|writer
operator|.
name|writeProperty
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|BEELINE_CONNECTION_PROPERTY_PREFIX
operator|+
literal|"hosts"
argument_list|,
name|uri
operator|.
name|getHost
argument_list|()
operator|+
literal|":"
operator|+
name|uri
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|writeProperty
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|BEELINE_CONNECTION_PROPERTY_PREFIX
operator|+
literal|"user"
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|writeProperty
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|BEELINE_CONNECTION_PROPERTY_PREFIX
operator|+
literal|"password"
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|writeProperty
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|BEELINE_CONNECTION_PROPERTY_PREFIX
operator|+
literal|"ssl"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|writeProperty
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|BEELINE_CONNECTION_PROPERTY_PREFIX
operator|+
literal|"trustStorePassword"
argument_list|,
name|KEY_STORE_TRUST_STORE_PASSWORD
argument_list|)
expr_stmt|;
name|writer
operator|.
name|writeProperty
argument_list|(
name|HS2ConnectionFileParser
operator|.
name|BEELINE_CONNECTION_PROPERTY_PREFIX
operator|+
literal|"sslTrustStore"
argument_list|,
name|dataFileDir
operator|+
name|File
operator|.
name|separator
operator|+
name|TRUST_STORE_NAME
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|writer
operator|.
name|path
argument_list|()
return|;
block|}
specifier|private
name|void
name|setupSslHs2
parameter_list|()
throws|throws
name|Exception
block|{
name|confOverlay
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_USE_SSL
operator|.
name|varname
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|confOverlay
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_SSL_KEYSTORE_PATH
operator|.
name|varname
argument_list|,
name|dataFileDir
operator|+
name|File
operator|.
name|separator
operator|+
name|LOCALHOST_KEY_STORE_NAME
argument_list|)
expr_stmt|;
name|confOverlay
operator|.
name|put
argument_list|(
name|ConfVars
operator|.
name|HIVE_SERVER2_SSL_KEYSTORE_PASSWORD
operator|.
name|varname
argument_list|,
name|KEY_STORE_TRUST_STORE_PASSWORD
argument_list|)
expr_stmt|;
name|miniHS2
operator|.
name|start
argument_list|(
name|confOverlay
argument_list|)
expr_stmt|;
name|createTable
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

