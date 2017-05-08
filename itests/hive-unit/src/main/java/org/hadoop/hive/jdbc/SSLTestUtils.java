begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|jdbc
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
name|URLEncoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Connection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Statement
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|fs
operator|.
name|Path
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

begin_class
specifier|public
class|class
name|SSLTestUtils
block|{
specifier|private
specifier|static
specifier|final
name|String
name|LOCALHOST_KEY_STORE_NAME
init|=
literal|"keystore.jks"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TRUST_STORE_NAME
init|=
literal|"truststore.jks"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|KEY_STORE_TRUST_STORE_PASSWORD
init|=
literal|"HiveJdbc"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HS2_BINARY_MODE
init|=
literal|"binary"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HS2_HTTP_MODE
init|=
literal|"http"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HS2_HTTP_ENDPOINT
init|=
literal|"cliservice"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HS2_BINARY_AUTH_MODE
init|=
literal|"NONE"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|dataFileDir
init|=
operator|!
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.data.files"
argument_list|,
literal|""
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|?
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.data.files"
argument_list|)
else|:
name|conf
operator|.
name|get
argument_list|(
literal|"test.data.files"
argument_list|)
operator|.
name|replace
argument_list|(
literal|'\\'
argument_list|,
literal|'/'
argument_list|)
operator|.
name|replace
argument_list|(
literal|"c:"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SSL_CONN_PARAMS
init|=
literal|"ssl=true;sslTrustStore="
operator|+
name|URLEncoder
operator|.
name|encode
argument_list|(
name|dataFileDir
operator|+
name|File
operator|.
name|separator
operator|+
name|TRUST_STORE_NAME
argument_list|)
operator|+
literal|";trustStorePassword="
operator|+
name|KEY_STORE_TRUST_STORE_PASSWORD
decl_stmt|;
specifier|public
specifier|static
name|void
name|setSslConfOverlay
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
parameter_list|)
block|{
name|confOverlay
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
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
name|HiveConf
operator|.
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
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_SSL_KEYSTORE_PASSWORD
operator|.
name|varname
argument_list|,
name|KEY_STORE_TRUST_STORE_PASSWORD
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|setMetastoreSslConf
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METASTORE_USE_SSL
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METASTORE_SSL_KEYSTORE_PATH
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
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METASTORE_SSL_KEYSTORE_PASSWORD
argument_list|,
name|KEY_STORE_TRUST_STORE_PASSWORD
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METASTORE_SSL_TRUSTSTORE_PATH
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
name|conf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_METASTORE_SSL_TRUSTSTORE_PASSWORD
argument_list|,
name|KEY_STORE_TRUST_STORE_PASSWORD
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|clearSslConfOverlay
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
parameter_list|)
block|{
name|confOverlay
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_USE_SSL
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|setHttpConfOverlay
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
parameter_list|)
block|{
name|confOverlay
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
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
name|HiveConf
operator|.
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
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
operator|.
name|varname
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|setBinaryConfOverlay
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|confOverlay
parameter_list|)
block|{
name|confOverlay
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_TRANSPORT_MODE
operator|.
name|varname
argument_list|,
name|HS2_BINARY_MODE
argument_list|)
expr_stmt|;
name|confOverlay
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_AUTHENTICATION
operator|.
name|varname
argument_list|,
name|HS2_BINARY_AUTH_MODE
argument_list|)
expr_stmt|;
name|confOverlay
operator|.
name|put
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SERVER2_ENABLE_DOAS
operator|.
name|varname
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|setupTestTableWithData
parameter_list|(
name|String
name|tableName
parameter_list|,
name|Path
name|dataFilePath
parameter_list|,
name|Connection
name|hs2Conn
parameter_list|)
throws|throws
name|Exception
block|{
name|Statement
name|stmt
init|=
name|hs2Conn
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"set hive.support.concurrency = false"
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"drop table if exists "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
literal|"create table "
operator|+
name|tableName
operator|+
literal|" (under_col int comment 'the under column', value string)"
argument_list|)
expr_stmt|;
comment|// load data
name|stmt
operator|.
name|execute
argument_list|(
literal|"load data local inpath '"
operator|+
name|dataFilePath
operator|.
name|toString
argument_list|()
operator|+
literal|"' into table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|stmt
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|String
name|getDataFileDir
parameter_list|()
block|{
return|return
name|dataFileDir
return|;
block|}
block|}
end_class

end_unit

