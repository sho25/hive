begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|templeton
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|httpclient
operator|.
name|HttpClient
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
name|httpclient
operator|.
name|HttpMethod
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
name|httpclient
operator|.
name|NameValuePair
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
name|httpclient
operator|.
name|methods
operator|.
name|DeleteMethod
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
name|httpclient
operator|.
name|methods
operator|.
name|GetMethod
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
name|httpclient
operator|.
name|methods
operator|.
name|PutMethod
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
name|httpclient
operator|.
name|methods
operator|.
name|StringRequestEntity
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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|ErrorMsg
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|ObjectMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|type
operator|.
name|TypeReference
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
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Ignore
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
name|HttpStatus
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
name|HashMap
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
name|TreeMap
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
import|;
end_import

begin_comment
comment|/**  * A set of tests exercising e2e WebHCat DDL APIs.  These tests are somewhat  * between WebHCat e2e (hcatalog/src/tests/e2e/templeton) tests and simple58  *  * unit tests.  This will start a WebHCat server and make REST calls to it.  * It doesn't need Hadoop or (standalone) metastore to be running.  * Running this is much simpler than e2e tests.  *  * Most of these tests check that HTTP Status code is what is expected and  * Hive Error code {@link org.apache.hadoop.hive.ql.ErrorMsg} is what is  * expected.  *  * It may be possible to extend this to more than just DDL later.  */
end_comment

begin_class
specifier|public
class|class
name|TestWebHCatE2e
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
name|TestWebHCatE2e
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|String
name|templetonBaseUrl
init|=
literal|"http://localhost:50111/templeton/v1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|username
init|=
literal|"johndoe"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ERROR_CODE
init|=
literal|"errorCode"
decl_stmt|;
specifier|private
specifier|static
name|Main
name|templetonServer
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|charSet
init|=
literal|"UTF-8"
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|startHebHcatInMem
parameter_list|()
throws|throws
name|Exception
block|{
name|Exception
name|webhcatException
init|=
literal|null
decl_stmt|;
name|int
name|webhcatPort
init|=
literal|0
decl_stmt|;
name|boolean
name|webhcatStarted
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|tryCount
init|=
literal|0
init|;
name|tryCount
operator|<
name|MetaStoreTestUtils
operator|.
name|RETRY_COUNT
condition|;
name|tryCount
operator|++
control|)
block|{
try|try
block|{
if|if
condition|(
name|tryCount
operator|==
name|MetaStoreTestUtils
operator|.
name|RETRY_COUNT
operator|-
literal|1
condition|)
block|{
comment|/* Last try to get a port.  Just use default 50111.  */
name|webhcatPort
operator|=
literal|50111
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to find free port; using default: "
operator|+
name|webhcatPort
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|webhcatPort
operator|=
name|MetaStoreTestUtils
operator|.
name|findFreePort
argument_list|()
expr_stmt|;
block|}
name|templetonBaseUrl
operator|=
name|templetonBaseUrl
operator|.
name|replace
argument_list|(
literal|"50111"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|webhcatPort
argument_list|)
argument_list|)
expr_stmt|;
name|templetonServer
operator|=
operator|new
name|Main
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"-D"
operator|+
name|AppConfig
operator|.
name|UNIT_TEST_MODE
operator|+
literal|"=true"
block|,
literal|"-D"
operator|+
name|AppConfig
operator|.
name|PORT
operator|+
literal|"="
operator|+
name|webhcatPort
block|}
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting Main; WebHCat using port: "
operator|+
name|webhcatPort
argument_list|)
expr_stmt|;
name|templetonServer
operator|.
name|run
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Main started"
argument_list|)
expr_stmt|;
name|webhcatStarted
operator|=
literal|true
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|Exception
name|ce
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Attempt to Start WebHCat using port: "
operator|+
name|webhcatPort
operator|+
literal|" failed"
argument_list|)
expr_stmt|;
name|webhcatException
operator|=
name|ce
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|webhcatStarted
condition|)
block|{
throw|throw
name|webhcatException
throw|;
block|}
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|stopWebHcatInMem
parameter_list|()
block|{
if|if
condition|(
name|templetonServer
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping Main"
argument_list|)
expr_stmt|;
name|templetonServer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Main stopped"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|jsonStringToSortedMap
parameter_list|(
name|String
name|jsonStr
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|sortedMap
decl_stmt|;
try|try
block|{
name|sortedMap
operator|=
operator|(
operator|new
name|ObjectMapper
argument_list|()
operator|)
operator|.
name|readValue
argument_list|(
name|jsonStr
argument_list|,
operator|new
name|TypeReference
argument_list|<
name|TreeMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
block|{}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Exception converting json string to sorted map "
operator|+
name|ex
argument_list|,
name|ex
argument_list|)
throw|;
block|}
return|return
name|sortedMap
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|getStatus
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"+getStatus()"
argument_list|)
expr_stmt|;
name|MethodCallRetVal
name|p
init|=
name|doHttpCall
argument_list|(
name|templetonBaseUrl
operator|+
literal|"/status"
argument_list|,
name|HTTP_METHOD_TYPE
operator|.
name|GET
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|p
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
name|HttpStatus
operator|.
name|OK_200
argument_list|,
name|p
operator|.
name|httpStatusCode
argument_list|)
expr_stmt|;
comment|// Must be deterministic order map for comparison across Java versions
name|Assert
operator|.
name|assertTrue
argument_list|(
name|p
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
name|jsonStringToSortedMap
argument_list|(
literal|"{\"status\":\"ok\",\"version\":\"v1\"}"
argument_list|)
operator|.
name|equals
argument_list|(
name|jsonStringToSortedMap
argument_list|(
name|p
operator|.
name|responseBody
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"-getStatus()"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
argument_list|(
literal|"not ready due to HIVE-4824"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|listDataBases
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"+listDataBases()"
argument_list|)
expr_stmt|;
name|MethodCallRetVal
name|p
init|=
name|doHttpCall
argument_list|(
name|templetonBaseUrl
operator|+
literal|"/ddl/database"
argument_list|,
name|HTTP_METHOD_TYPE
operator|.
name|GET
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|p
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
name|HttpStatus
operator|.
name|OK_200
argument_list|,
name|p
operator|.
name|httpStatusCode
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|p
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
literal|"{\"databases\":[\"default\"]}"
argument_list|,
name|p
operator|.
name|responseBody
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"-listDataBases()"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check that we return correct status code when the URL doesn't map to any method    * in {@link Server}    */
annotation|@
name|Test
specifier|public
name|void
name|invalidPath
parameter_list|()
throws|throws
name|IOException
block|{
name|MethodCallRetVal
name|p
init|=
name|doHttpCall
argument_list|(
name|templetonBaseUrl
operator|+
literal|"/no_such_mapping/database"
argument_list|,
name|HTTP_METHOD_TYPE
operator|.
name|GET
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|p
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
name|HttpStatus
operator|.
name|NOT_FOUND_404
argument_list|,
name|p
operator|.
name|httpStatusCode
argument_list|)
expr_stmt|;
block|}
comment|/**    * tries to drop table in a DB that doesn't exist    */
annotation|@
name|Ignore
argument_list|(
literal|"not ready due to HIVE-4824"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|dropTableNoSuchDB
parameter_list|()
throws|throws
name|IOException
block|{
name|MethodCallRetVal
name|p
init|=
name|doHttpCall
argument_list|(
name|templetonBaseUrl
operator|+
literal|"/ddl/database/no_such_db/table/t1"
argument_list|,
name|HTTP_METHOD_TYPE
operator|.
name|DELETE
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|p
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
name|HttpStatus
operator|.
name|NOT_FOUND_404
argument_list|,
name|p
operator|.
name|httpStatusCode
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|p
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
name|ErrorMsg
operator|.
name|DATABASE_NOT_EXISTS
operator|.
name|getErrorCode
argument_list|()
argument_list|,
name|getErrorCode
argument_list|(
name|p
operator|.
name|responseBody
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * tries to drop table in a DB that doesn't exist    */
annotation|@
name|Ignore
argument_list|(
literal|"not ready due to HIVE-4824"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|dropTableNoSuchDbIfExists
parameter_list|()
throws|throws
name|IOException
block|{
name|MethodCallRetVal
name|p
init|=
name|doHttpCall
argument_list|(
name|templetonBaseUrl
operator|+
literal|"/ddl/database/no_such_db/table/t1"
argument_list|,
name|HTTP_METHOD_TYPE
operator|.
name|DELETE
argument_list|,
literal|null
argument_list|,
operator|new
name|NameValuePair
index|[]
block|{
operator|new
name|NameValuePair
argument_list|(
literal|"ifExists"
argument_list|,
literal|"true"
argument_list|)
block|}
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|p
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
name|HttpStatus
operator|.
name|NOT_FOUND_404
argument_list|,
name|p
operator|.
name|httpStatusCode
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|p
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
name|ErrorMsg
operator|.
name|DATABASE_NOT_EXISTS
operator|.
name|getErrorCode
argument_list|()
argument_list|,
name|getErrorCode
argument_list|(
name|p
operator|.
name|responseBody
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * tries to drop table that doesn't exist (with ifExists=true)   */
annotation|@
name|Ignore
argument_list|(
literal|"not ready due to HIVE-4824"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|dropTableIfExists
parameter_list|()
throws|throws
name|IOException
block|{
name|MethodCallRetVal
name|p
init|=
name|doHttpCall
argument_list|(
name|templetonBaseUrl
operator|+
literal|"/ddl/database/default/table/no_such_table"
argument_list|,
name|HTTP_METHOD_TYPE
operator|.
name|DELETE
argument_list|,
literal|null
argument_list|,
operator|new
name|NameValuePair
index|[]
block|{
operator|new
name|NameValuePair
argument_list|(
literal|"ifExists"
argument_list|,
literal|"true"
argument_list|)
block|}
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|p
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
name|HttpStatus
operator|.
name|OK_200
argument_list|,
name|p
operator|.
name|httpStatusCode
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
argument_list|(
literal|"not ready due to HIVE-4824"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|createDataBase
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|props
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|props
operator|.
name|put
argument_list|(
literal|"comment"
argument_list|,
literal|"Hello, there"
argument_list|)
expr_stmt|;
name|props
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.warehouse.dir"
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props2
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
name|props2
operator|.
name|put
argument_list|(
literal|"prop"
argument_list|,
literal|"val"
argument_list|)
expr_stmt|;
name|props
operator|.
name|put
argument_list|(
literal|"properties"
argument_list|,
name|props2
argument_list|)
expr_stmt|;
comment|//{ "comment":"Hello there", "location":"file:///tmp/warehouse", "properties":{"a":"b"}}
name|MethodCallRetVal
name|p
init|=
name|doHttpCall
argument_list|(
name|templetonBaseUrl
operator|+
literal|"/ddl/database/newdb"
argument_list|,
name|HTTP_METHOD_TYPE
operator|.
name|PUT
argument_list|,
name|props
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|p
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
name|HttpStatus
operator|.
name|OK_200
argument_list|,
name|p
operator|.
name|httpStatusCode
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
argument_list|(
literal|"not ready due to HIVE-4824"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|createTable
parameter_list|()
throws|throws
name|IOException
block|{
comment|//{ "comment":"test", "columns": [ { "name": "col1", "type": "string" } ], "format": { "storedAs": "rcfile" } }
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|props
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|props
operator|.
name|put
argument_list|(
literal|"comment"
argument_list|,
literal|"Table in default db"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|col
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|col
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"col1"
argument_list|)
expr_stmt|;
name|col
operator|.
name|put
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|colList
init|=
operator|new
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|colList
operator|.
name|add
argument_list|(
name|col
argument_list|)
expr_stmt|;
name|props
operator|.
name|put
argument_list|(
literal|"columns"
argument_list|,
name|colList
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|format
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|format
operator|.
name|put
argument_list|(
literal|"storedAs"
argument_list|,
literal|"rcfile"
argument_list|)
expr_stmt|;
name|props
operator|.
name|put
argument_list|(
literal|"format"
argument_list|,
name|format
argument_list|)
expr_stmt|;
name|MethodCallRetVal
name|createTbl
init|=
name|doHttpCall
argument_list|(
name|templetonBaseUrl
operator|+
literal|"/ddl/database/default/table/test_table"
argument_list|,
name|HTTP_METHOD_TYPE
operator|.
name|PUT
argument_list|,
name|props
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|createTbl
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
name|HttpStatus
operator|.
name|OK_200
argument_list|,
name|createTbl
operator|.
name|httpStatusCode
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"createTable() resp: "
operator|+
name|createTbl
operator|.
name|responseBody
argument_list|)
expr_stmt|;
name|MethodCallRetVal
name|descTbl
init|=
name|doHttpCall
argument_list|(
name|templetonBaseUrl
operator|+
literal|"/ddl/database/default/table/test_table"
argument_list|,
name|HTTP_METHOD_TYPE
operator|.
name|GET
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|descTbl
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
name|HttpStatus
operator|.
name|OK_200
argument_list|,
name|descTbl
operator|.
name|httpStatusCode
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Ignore
argument_list|(
literal|"not ready due to HIVE-4824"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|describeNoSuchTable
parameter_list|()
throws|throws
name|IOException
block|{
name|MethodCallRetVal
name|p
init|=
name|doHttpCall
argument_list|(
name|templetonBaseUrl
operator|+
literal|"/ddl/database/default/table/no_such_table"
argument_list|,
name|HTTP_METHOD_TYPE
operator|.
name|GET
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|p
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
name|HttpStatus
operator|.
name|NOT_FOUND_404
argument_list|,
name|p
operator|.
name|httpStatusCode
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|p
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
name|ErrorMsg
operator|.
name|INVALID_TABLE
operator|.
name|getErrorCode
argument_list|()
argument_list|,
name|getErrorCode
argument_list|(
name|p
operator|.
name|responseBody
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|getHadoopVersion
parameter_list|()
throws|throws
name|Exception
block|{
name|MethodCallRetVal
name|p
init|=
name|doHttpCall
argument_list|(
name|templetonBaseUrl
operator|+
literal|"/version/hadoop"
argument_list|,
name|HTTP_METHOD_TYPE
operator|.
name|GET
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HttpStatus
operator|.
name|OK_200
argument_list|,
name|p
operator|.
name|httpStatusCode
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|props
init|=
name|JsonBuilder
operator|.
name|jsonToMap
argument_list|(
name|p
operator|.
name|responseBody
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"hadoop"
argument_list|,
name|props
operator|.
name|get
argument_list|(
literal|"module"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|p
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
operator|(
operator|(
name|String
operator|)
name|props
operator|.
name|get
argument_list|(
literal|"version"
argument_list|)
operator|)
operator|.
name|matches
argument_list|(
literal|"[1-3].[0-9]+.[0-9]+.*"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|getHiveVersion
parameter_list|()
throws|throws
name|Exception
block|{
name|MethodCallRetVal
name|p
init|=
name|doHttpCall
argument_list|(
name|templetonBaseUrl
operator|+
literal|"/version/hive"
argument_list|,
name|HTTP_METHOD_TYPE
operator|.
name|GET
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HttpStatus
operator|.
name|OK_200
argument_list|,
name|p
operator|.
name|httpStatusCode
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|props
init|=
name|JsonBuilder
operator|.
name|jsonToMap
argument_list|(
name|p
operator|.
name|responseBody
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"hive"
argument_list|,
name|props
operator|.
name|get
argument_list|(
literal|"module"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|p
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
operator|(
operator|(
name|String
operator|)
name|props
operator|.
name|get
argument_list|(
literal|"version"
argument_list|)
operator|)
operator|.
name|matches
argument_list|(
literal|"[0-9]+.[0-9]+.[0-9]+.*"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|getPigVersion
parameter_list|()
throws|throws
name|Exception
block|{
name|MethodCallRetVal
name|p
init|=
name|doHttpCall
argument_list|(
name|templetonBaseUrl
operator|+
literal|"/version/pig"
argument_list|,
name|HTTP_METHOD_TYPE
operator|.
name|GET
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|HttpStatus
operator|.
name|NOT_IMPLEMENTED_501
argument_list|,
name|p
operator|.
name|httpStatusCode
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|props
init|=
name|JsonBuilder
operator|.
name|jsonToMap
argument_list|(
name|p
operator|.
name|responseBody
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|p
operator|.
name|getAssertMsg
argument_list|()
argument_list|,
literal|"Pig version request not yet "
operator|+
literal|"implemented"
argument_list|,
operator|(
name|String
operator|)
name|props
operator|.
name|get
argument_list|(
literal|"error"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * It's expected that Templeton returns a properly formatted JSON object when it    * encounters an error.  It should have {@code ERROR_CODE} element in it which    * should be the Hive canonical error msg code.    * @return the code or -1 if it cannot be found    */
specifier|private
specifier|static
name|int
name|getErrorCode
parameter_list|(
name|String
name|jsonErrorObject
parameter_list|)
throws|throws
name|IOException
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|//JSON key is always a String
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|retProps
init|=
name|JsonBuilder
operator|.
name|jsonToMap
argument_list|(
name|jsonErrorObject
operator|+
literal|"blah blah"
argument_list|)
decl_stmt|;
name|int
name|hiveRetCode
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|retProps
operator|.
name|get
argument_list|(
name|ERROR_CODE
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|hiveRetCode
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|retProps
operator|.
name|get
argument_list|(
name|ERROR_CODE
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|hiveRetCode
return|;
block|}
comment|/**    * Encapsulates information from HTTP method call    */
specifier|private
specifier|static
class|class
name|MethodCallRetVal
block|{
specifier|private
specifier|final
name|int
name|httpStatusCode
decl_stmt|;
specifier|private
specifier|final
name|String
name|responseBody
decl_stmt|;
specifier|private
specifier|final
name|String
name|submittedURL
decl_stmt|;
specifier|private
specifier|final
name|String
name|methodName
decl_stmt|;
specifier|private
name|MethodCallRetVal
parameter_list|(
name|int
name|httpStatusCode
parameter_list|,
name|String
name|responseBody
parameter_list|,
name|String
name|submittedURL
parameter_list|,
name|String
name|methodName
parameter_list|)
block|{
name|this
operator|.
name|httpStatusCode
operator|=
name|httpStatusCode
expr_stmt|;
name|this
operator|.
name|responseBody
operator|=
name|responseBody
expr_stmt|;
name|this
operator|.
name|submittedURL
operator|=
name|submittedURL
expr_stmt|;
name|this
operator|.
name|methodName
operator|=
name|methodName
expr_stmt|;
block|}
name|String
name|getAssertMsg
parameter_list|()
block|{
return|return
name|methodName
operator|+
literal|" "
operator|+
name|submittedURL
operator|+
literal|" "
operator|+
name|responseBody
return|;
block|}
block|}
specifier|private
specifier|static
enum|enum
name|HTTP_METHOD_TYPE
block|{
name|GET
block|,
name|POST
block|,
name|DELETE
block|,
name|PUT
block|}
specifier|private
specifier|static
name|MethodCallRetVal
name|doHttpCall
parameter_list|(
name|String
name|uri
parameter_list|,
name|HTTP_METHOD_TYPE
name|type
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|doHttpCall
argument_list|(
name|uri
argument_list|,
name|type
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Does a basic HTTP GET and returns Http Status code + response body    * Will add the dummy user query string    */
specifier|private
specifier|static
name|MethodCallRetVal
name|doHttpCall
parameter_list|(
name|String
name|uri
parameter_list|,
name|HTTP_METHOD_TYPE
name|type
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|data
parameter_list|,
name|NameValuePair
index|[]
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|HttpClient
name|client
init|=
operator|new
name|HttpClient
argument_list|()
decl_stmt|;
name|HttpMethod
name|method
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|GET
case|:
name|method
operator|=
operator|new
name|GetMethod
argument_list|(
name|uri
argument_list|)
expr_stmt|;
break|break;
case|case
name|DELETE
case|:
name|method
operator|=
operator|new
name|DeleteMethod
argument_list|(
name|uri
argument_list|)
expr_stmt|;
break|break;
case|case
name|PUT
case|:
name|method
operator|=
operator|new
name|PutMethod
argument_list|(
name|uri
argument_list|)
expr_stmt|;
if|if
condition|(
name|data
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|String
name|msgBody
init|=
name|JsonBuilder
operator|.
name|mapToJson
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Msg Body: "
operator|+
name|msgBody
argument_list|)
expr_stmt|;
name|StringRequestEntity
name|sre
init|=
operator|new
name|StringRequestEntity
argument_list|(
name|msgBody
argument_list|,
literal|"application/json"
argument_list|,
name|charSet
argument_list|)
decl_stmt|;
operator|(
operator|(
name|PutMethod
operator|)
name|method
operator|)
operator|.
name|setRequestEntity
argument_list|(
name|sre
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unsupported method type: "
operator|+
name|type
argument_list|)
throw|;
block|}
if|if
condition|(
name|params
operator|==
literal|null
condition|)
block|{
name|method
operator|.
name|setQueryString
argument_list|(
operator|new
name|NameValuePair
index|[]
block|{
operator|new
name|NameValuePair
argument_list|(
literal|"user.name"
argument_list|,
name|username
argument_list|)
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|NameValuePair
index|[]
name|newParams
init|=
operator|new
name|NameValuePair
index|[
name|params
operator|.
name|length
operator|+
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|params
argument_list|,
literal|0
argument_list|,
name|newParams
argument_list|,
literal|1
argument_list|,
name|params
operator|.
name|length
argument_list|)
expr_stmt|;
name|newParams
index|[
literal|0
index|]
operator|=
operator|new
name|NameValuePair
argument_list|(
literal|"user.name"
argument_list|,
name|username
argument_list|)
expr_stmt|;
name|method
operator|.
name|setQueryString
argument_list|(
name|newParams
argument_list|)
expr_stmt|;
block|}
name|String
name|actualUri
init|=
literal|"no URI"
decl_stmt|;
try|try
block|{
name|actualUri
operator|=
name|method
operator|.
name|getURI
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
comment|//should this be escaped string?
name|LOG
operator|.
name|debug
argument_list|(
name|type
operator|+
literal|": "
operator|+
name|method
operator|.
name|getURI
argument_list|()
operator|.
name|getEscapedURI
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|httpStatus
init|=
name|client
operator|.
name|executeMethod
argument_list|(
name|method
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Http Status Code="
operator|+
name|httpStatus
argument_list|)
expr_stmt|;
name|String
name|resp
init|=
name|method
operator|.
name|getResponseBodyAsString
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"response: "
operator|+
name|resp
argument_list|)
expr_stmt|;
return|return
operator|new
name|MethodCallRetVal
argument_list|(
name|httpStatus
argument_list|,
name|resp
argument_list|,
name|actualUri
argument_list|,
name|method
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"doHttpCall() failed"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|method
operator|.
name|releaseConnection
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|MethodCallRetVal
argument_list|(
operator|-
literal|1
argument_list|,
literal|"Http "
operator|+
name|type
operator|+
literal|" failed; see log file for details"
argument_list|,
name|actualUri
argument_list|,
name|method
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

