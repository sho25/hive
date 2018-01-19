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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
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
name|DriverManager
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|ResultSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|ResultSetMetaData
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|io
operator|.
name|FileUtils
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
name|io
operator|.
name|IOUtils
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
name|hive
operator|.
name|jdbc
operator|.
name|HiveConnection
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
name|service
operator|.
name|auth
operator|.
name|HiveAuthConstants
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
name|service
operator|.
name|cli
operator|.
name|session
operator|.
name|SessionUtils
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
name|beeline
operator|.
name|BeeLine
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
name|hive
operator|.
name|shims
operator|.
name|Utils
import|;
end_import

begin_comment
comment|/**  * Simple client application to test various direct and proxy connection to HiveServer2  * Note that it's not an automated test at this point. It requires a manually configured  * secure HivServer2. It also requires a super user and a normal user principal.  * Steps to run the test -  *   kinit<super-user>  *   hive --service jar beeline/target/hive-beeline-0.13.0-SNAPSHOT-tests.jar \  *      org.apache.hive.beeline.ProxyAuthTest \  *<HS2host><HS2Port><HS2-Server-principal><client-principal>  */
end_comment

begin_class
specifier|public
class|class
name|ProxyAuthTest
block|{
specifier|private
specifier|static
specifier|final
name|String
name|driverName
init|=
literal|"org.apache.hive.jdbc.HiveDriver"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|BEELINE_EXIT
init|=
literal|"beeline.system.exit"
decl_stmt|;
specifier|private
specifier|static
name|Connection
name|con
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|noClose
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|String
name|tabName
init|=
literal|"jdbc_test"
decl_stmt|;
specifier|private
specifier|static
name|String
name|tabDataFileName
decl_stmt|;
specifier|private
specifier|static
name|String
name|scriptFileName
decl_stmt|;
specifier|private
specifier|static
name|String
index|[]
name|dmlStmts
decl_stmt|;
specifier|private
specifier|static
name|String
index|[]
name|dfsStmts
decl_stmt|;
specifier|private
specifier|static
name|String
index|[]
name|selectStmts
decl_stmt|;
specifier|private
specifier|static
name|String
index|[]
name|cleanUpStmts
decl_stmt|;
specifier|private
specifier|static
name|InputStream
name|inpStream
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|int
name|tabCount
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
name|File
name|resultFile
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|4
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Usage ProxyAuthTest<host><port><server_principal><proxy_user> [testTab]"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
name|File
name|currentResultFile
init|=
literal|null
decl_stmt|;
name|String
index|[]
name|beeLineArgs
init|=
block|{}
decl_stmt|;
name|Class
operator|.
name|forName
argument_list|(
name|driverName
argument_list|)
expr_stmt|;
name|String
name|host
init|=
name|args
index|[
literal|0
index|]
decl_stmt|;
name|String
name|port
init|=
name|args
index|[
literal|1
index|]
decl_stmt|;
name|String
name|serverPrincipal
init|=
name|args
index|[
literal|2
index|]
decl_stmt|;
name|String
name|proxyUser
init|=
name|args
index|[
literal|3
index|]
decl_stmt|;
name|String
name|url
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|>
literal|4
condition|)
block|{
name|tabName
operator|=
name|args
index|[
literal|4
index|]
expr_stmt|;
block|}
name|generateData
argument_list|()
expr_stmt|;
name|generateSQL
argument_list|(
literal|null
argument_list|)
expr_stmt|;
try|try
block|{
comment|/*      * Connect via kerberos and get delegation token      */
name|url
operator|=
literal|"jdbc:hive2://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
operator|+
literal|"/default;principal="
operator|+
name|serverPrincipal
expr_stmt|;
name|con
operator|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|url
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Connected successfully to "
operator|+
name|url
argument_list|)
expr_stmt|;
comment|// get delegation token for the given proxy user
name|String
name|token
init|=
operator|(
operator|(
name|HiveConnection
operator|)
name|con
operator|)
operator|.
name|getDelegationToken
argument_list|(
name|proxyUser
argument_list|,
name|serverPrincipal
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"true"
operator|.
name|equals
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"proxyAuth.debug"
argument_list|,
literal|"false"
argument_list|)
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Got token: "
operator|+
name|token
argument_list|)
expr_stmt|;
block|}
name|con
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// so that beeline won't kill the JVM
name|System
operator|.
name|setProperty
argument_list|(
name|BEELINE_EXIT
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
comment|// connect using principal via Beeline with inputStream
name|url
operator|=
literal|"jdbc:hive2://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
operator|+
literal|"/default;principal="
operator|+
name|serverPrincipal
expr_stmt|;
name|currentResultFile
operator|=
name|generateSQL
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|beeLineArgs
operator|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
name|url
block|,
literal|"-n"
block|,
literal|"foo"
block|,
literal|"-p"
block|,
literal|"bar"
block|}
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Connection with kerberos, user/password via args, using input rediction"
argument_list|)
expr_stmt|;
name|BeeLine
operator|.
name|mainWithInputRedirection
argument_list|(
name|beeLineArgs
argument_list|,
name|inpStream
argument_list|)
expr_stmt|;
name|compareResults
argument_list|(
name|currentResultFile
argument_list|)
expr_stmt|;
comment|// connect using principal via Beeline with inputStream
name|url
operator|=
literal|"jdbc:hive2://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
operator|+
literal|"/default;principal="
operator|+
name|serverPrincipal
expr_stmt|;
name|currentResultFile
operator|=
name|generateSQL
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|beeLineArgs
operator|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
name|url
block|,
literal|"-n"
block|,
literal|"foo"
block|,
literal|"-p"
block|,
literal|"bar"
block|,
literal|"-f"
block|,
name|scriptFileName
block|}
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Connection with kerberos, user/password via args, using input script"
argument_list|)
expr_stmt|;
name|BeeLine
operator|.
name|main
argument_list|(
name|beeLineArgs
argument_list|)
expr_stmt|;
name|compareResults
argument_list|(
name|currentResultFile
argument_list|)
expr_stmt|;
comment|// connect using principal via Beeline with inputStream
name|url
operator|=
literal|"jdbc:hive2://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
operator|+
literal|"/default;principal="
operator|+
name|serverPrincipal
expr_stmt|;
name|currentResultFile
operator|=
name|generateSQL
argument_list|(
name|url
operator|+
literal|" foo bar "
argument_list|)
expr_stmt|;
name|beeLineArgs
operator|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
name|url
block|,
literal|"-f"
block|,
name|scriptFileName
block|}
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Connection with kerberos, user/password via connect, using input script"
argument_list|)
expr_stmt|;
name|BeeLine
operator|.
name|main
argument_list|(
name|beeLineArgs
argument_list|)
expr_stmt|;
name|compareResults
argument_list|(
name|currentResultFile
argument_list|)
expr_stmt|;
comment|// connect using principal via Beeline with inputStream
name|url
operator|=
literal|"jdbc:hive2://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
operator|+
literal|"/default;principal="
operator|+
name|serverPrincipal
expr_stmt|;
name|currentResultFile
operator|=
name|generateSQL
argument_list|(
name|url
operator|+
literal|" foo bar "
argument_list|)
expr_stmt|;
name|beeLineArgs
operator|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
name|url
block|,
literal|"-f"
block|,
name|scriptFileName
block|}
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Connection with kerberos, user/password via connect, using input redirect"
argument_list|)
expr_stmt|;
name|BeeLine
operator|.
name|mainWithInputRedirection
argument_list|(
name|beeLineArgs
argument_list|,
name|inpStream
argument_list|)
expr_stmt|;
name|compareResults
argument_list|(
name|currentResultFile
argument_list|)
expr_stmt|;
comment|/*      * Connect using the delegation token passed via configuration object      */
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Store token into ugi and try"
argument_list|)
expr_stmt|;
name|storeTokenInJobConf
argument_list|(
name|token
argument_list|)
expr_stmt|;
name|url
operator|=
literal|"jdbc:hive2://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
operator|+
literal|"/default;auth=delegationToken"
expr_stmt|;
name|con
operator|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|url
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Connecting to "
operator|+
name|url
argument_list|)
expr_stmt|;
name|runTest
argument_list|()
expr_stmt|;
name|con
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// connect using token via Beeline with inputStream
name|url
operator|=
literal|"jdbc:hive2://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
operator|+
literal|"/default"
expr_stmt|;
name|currentResultFile
operator|=
name|generateSQL
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|beeLineArgs
operator|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
name|url
block|,
literal|"-n"
block|,
literal|"foo"
block|,
literal|"-p"
block|,
literal|"bar"
block|,
literal|"-a"
block|,
literal|"delegationToken"
block|}
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Connection with token, user/password via args, using input redirection"
argument_list|)
expr_stmt|;
name|BeeLine
operator|.
name|mainWithInputRedirection
argument_list|(
name|beeLineArgs
argument_list|,
name|inpStream
argument_list|)
expr_stmt|;
name|compareResults
argument_list|(
name|currentResultFile
argument_list|)
expr_stmt|;
comment|// connect using token via Beeline using script
name|url
operator|=
literal|"jdbc:hive2://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
operator|+
literal|"/default"
expr_stmt|;
name|currentResultFile
operator|=
name|generateSQL
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|beeLineArgs
operator|=
operator|new
name|String
index|[]
block|{
literal|"-u"
block|,
name|url
block|,
literal|"-n"
block|,
literal|"foo"
block|,
literal|"-p"
block|,
literal|"bar"
block|,
literal|"-a"
block|,
literal|"delegationToken"
block|,
literal|"-f"
block|,
name|scriptFileName
block|}
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Connection with token, user/password via args, using input script"
argument_list|)
expr_stmt|;
name|BeeLine
operator|.
name|main
argument_list|(
name|beeLineArgs
argument_list|)
expr_stmt|;
name|compareResults
argument_list|(
name|currentResultFile
argument_list|)
expr_stmt|;
comment|// connect using token via Beeline using script
name|url
operator|=
literal|"jdbc:hive2://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
operator|+
literal|"/default"
expr_stmt|;
name|currentResultFile
operator|=
name|generateSQL
argument_list|(
name|url
operator|+
literal|" foo bar "
argument_list|)
expr_stmt|;
name|beeLineArgs
operator|=
operator|new
name|String
index|[]
block|{
literal|"-a"
block|,
literal|"delegationToken"
block|,
literal|"-f"
block|,
name|scriptFileName
block|}
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Connection with token, user/password via connect, using input script"
argument_list|)
expr_stmt|;
name|BeeLine
operator|.
name|main
argument_list|(
name|beeLineArgs
argument_list|)
expr_stmt|;
name|compareResults
argument_list|(
name|currentResultFile
argument_list|)
expr_stmt|;
comment|// connect using token via Beeline using script
name|url
operator|=
literal|"jdbc:hive2://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
operator|+
literal|"/default"
expr_stmt|;
name|currentResultFile
operator|=
name|generateSQL
argument_list|(
name|url
operator|+
literal|" foo bar "
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Connection with token, user/password via connect, using input script"
argument_list|)
expr_stmt|;
name|beeLineArgs
operator|=
operator|new
name|String
index|[]
block|{
literal|"-f"
block|,
name|scriptFileName
block|,
literal|"-a"
block|,
literal|"delegationToken"
block|}
expr_stmt|;
name|BeeLine
operator|.
name|main
argument_list|(
name|beeLineArgs
argument_list|)
expr_stmt|;
name|compareResults
argument_list|(
name|currentResultFile
argument_list|)
expr_stmt|;
comment|/*      * Connect via kerberos with trusted proxy user      */
name|url
operator|=
literal|"jdbc:hive2://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
operator|+
literal|"/default;principal="
operator|+
name|serverPrincipal
operator|+
literal|";hive.server2.proxy.user="
operator|+
name|proxyUser
expr_stmt|;
name|con
operator|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|url
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Connected successfully to "
operator|+
name|url
argument_list|)
expr_stmt|;
name|runTest
argument_list|()
expr_stmt|;
operator|(
operator|(
name|HiveConnection
operator|)
name|con
operator|)
operator|.
name|cancelDelegationToken
argument_list|(
name|token
argument_list|)
expr_stmt|;
name|con
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"*** SQLException: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|" : "
operator|+
name|e
operator|.
name|getSQLState
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
comment|/* verify the connection fails after canceling the token */
try|try
block|{
name|url
operator|=
literal|"jdbc:hive2://"
operator|+
name|host
operator|+
literal|":"
operator|+
name|port
operator|+
literal|"/default;auth=delegationToken"
expr_stmt|;
name|con
operator|=
name|DriverManager
operator|.
name|getConnection
argument_list|(
name|url
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|Exception
argument_list|(
literal|"connection should have failed after token cancellation"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
comment|// Expected to fail due to canceled token
block|}
block|}
specifier|private
specifier|static
name|void
name|storeTokenInJobConf
parameter_list|(
name|String
name|tokenStr
parameter_list|)
throws|throws
name|Exception
block|{
name|SessionUtils
operator|.
name|setTokenStr
argument_list|(
name|Utils
operator|.
name|getUGI
argument_list|()
argument_list|,
name|tokenStr
argument_list|,
name|HiveAuthConstants
operator|.
name|HS2_CLIENT_TOKEN
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Stored token "
operator|+
name|tokenStr
argument_list|)
expr_stmt|;
block|}
comment|// run sql operations
specifier|private
specifier|static
name|void
name|runTest
parameter_list|()
throws|throws
name|Exception
block|{
comment|// craete table and check dir ownership
name|runDMLs
argument_list|()
expr_stmt|;
comment|// run queries
for|for
control|(
name|String
name|stmt
range|:
name|dfsStmts
control|)
block|{
name|runQuery
argument_list|(
name|stmt
argument_list|)
expr_stmt|;
block|}
comment|// run queries
for|for
control|(
name|String
name|stmt
range|:
name|selectStmts
control|)
block|{
name|runQuery
argument_list|(
name|stmt
argument_list|)
expr_stmt|;
block|}
comment|// delete all the objects created
name|cleanUp
argument_list|()
expr_stmt|;
block|}
comment|// create tables and load data
specifier|private
specifier|static
name|void
name|runDMLs
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|String
name|stmt
range|:
name|dmlStmts
control|)
block|{
name|exStatement
argument_list|(
name|stmt
argument_list|)
expr_stmt|;
block|}
block|}
comment|// drop tables
specifier|private
specifier|static
name|void
name|cleanUp
parameter_list|()
throws|throws
name|Exception
block|{
for|for
control|(
name|String
name|stmt
range|:
name|cleanUpStmts
control|)
block|{
name|exStatement
argument_list|(
name|stmt
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|runQuery
parameter_list|(
name|String
name|sqlStmt
parameter_list|)
throws|throws
name|Exception
block|{
name|Statement
name|stmt
init|=
name|con
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|ResultSet
name|res
init|=
name|stmt
operator|.
name|executeQuery
argument_list|(
name|sqlStmt
argument_list|)
decl_stmt|;
name|ResultSetMetaData
name|meta
init|=
name|res
operator|.
name|getMetaData
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Resultset has "
operator|+
name|meta
operator|.
name|getColumnCount
argument_list|()
operator|+
literal|" columns"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|meta
operator|.
name|getColumnCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Column #"
operator|+
name|i
operator|+
literal|" Name: "
operator|+
name|meta
operator|.
name|getColumnName
argument_list|(
name|i
argument_list|)
operator|+
literal|" Type: "
operator|+
name|meta
operator|.
name|getColumnType
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
while|while
condition|(
name|res
operator|.
name|next
argument_list|()
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|meta
operator|.
name|getColumnCount
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Column #"
operator|+
name|i
operator|+
literal|": "
operator|+
name|res
operator|.
name|getString
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|res
operator|.
name|close
argument_list|()
expr_stmt|;
name|stmt
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// Execute the given sql statement
specifier|private
specifier|static
name|void
name|exStatement
parameter_list|(
name|String
name|query
parameter_list|)
throws|throws
name|Exception
block|{
name|Statement
name|stmt
init|=
name|con
operator|.
name|createStatement
argument_list|()
decl_stmt|;
name|stmt
operator|.
name|execute
argument_list|(
name|query
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|noClose
condition|)
block|{
name|stmt
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|// generate SQL stmts to execute
specifier|private
specifier|static
name|File
name|generateSQL
parameter_list|(
name|String
name|url
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|current
init|=
operator|new
name|java
operator|.
name|io
operator|.
name|File
argument_list|(
literal|"."
argument_list|)
operator|.
name|getCanonicalPath
argument_list|()
decl_stmt|;
name|String
name|currentDir
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.dir"
argument_list|)
decl_stmt|;
name|String
name|queryTab
init|=
name|tabName
operator|+
literal|"_"
operator|+
operator|(
name|tabCount
operator|++
operator|)
decl_stmt|;
name|dmlStmts
operator|=
operator|new
name|String
index|[]
block|{
literal|"USE default"
block|,
literal|"drop table if exists  "
operator|+
name|queryTab
block|,
literal|"create table "
operator|+
name|queryTab
operator|+
literal|"(id int, name string) "
operator|+
literal|"ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'"
block|,
literal|"load data local inpath '"
operator|+
name|tabDataFileName
operator|+
literal|"' into table "
operator|+
name|queryTab
block|}
expr_stmt|;
name|selectStmts
operator|=
operator|new
name|String
index|[]
block|{
literal|"select * from "
operator|+
name|queryTab
operator|+
literal|" limit 5"
block|,
literal|"select name, id from "
operator|+
name|queryTab
operator|+
literal|" where id< 3"
block|,     }
expr_stmt|;
name|dfsStmts
operator|=
operator|new
name|String
index|[]
block|{
comment|//      "set " + SESSION_USER_NAME,
comment|//      "dfs -ls -d ${hiveconf:hive.metastore.warehouse.dir}/" + queryTab
block|}
expr_stmt|;
name|cleanUpStmts
operator|=
operator|new
name|String
index|[]
block|{
literal|"drop table if exists  "
operator|+
name|queryTab
block|}
expr_stmt|;
comment|// write sql statements to file
return|return
name|writeArrayToByteStream
argument_list|(
name|url
argument_list|)
return|;
block|}
comment|// generate data file for test
specifier|private
specifier|static
name|void
name|generateData
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|fileData
index|[]
init|=
block|{
literal|"1|aaa"
block|,
literal|"2|bbb"
block|,
literal|"3|ccc"
block|,
literal|"4|ddd"
block|,
literal|"5|eee"
block|,     }
decl_stmt|;
name|File
name|tmpFile
init|=
name|File
operator|.
name|createTempFile
argument_list|(
name|tabName
argument_list|,
literal|".data"
argument_list|)
decl_stmt|;
name|tmpFile
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|tabDataFileName
operator|=
name|tmpFile
operator|.
name|getPath
argument_list|()
expr_stmt|;
name|FileWriter
name|fstream
init|=
operator|new
name|FileWriter
argument_list|(
name|tabDataFileName
argument_list|)
decl_stmt|;
name|BufferedWriter
name|out
init|=
operator|new
name|BufferedWriter
argument_list|(
name|fstream
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|line
range|:
name|fileData
control|)
block|{
name|out
operator|.
name|write
argument_list|(
name|line
argument_list|)
expr_stmt|;
name|out
operator|.
name|newLine
argument_list|()
expr_stmt|;
block|}
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|tmpFile
operator|.
name|setWritable
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Create a input stream of given name.ext  and write sql statements to to it
comment|// Returns the result File object which will contain the query results
specifier|private
specifier|static
name|File
name|writeArrayToByteStream
parameter_list|(
name|String
name|url
parameter_list|)
throws|throws
name|Exception
block|{
name|ByteArrayOutputStream
name|out
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
if|if
condition|(
name|url
operator|!=
literal|null
condition|)
block|{
name|writeCmdLine
argument_list|(
literal|"!connect "
operator|+
name|url
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
name|writeCmdLine
argument_list|(
literal|"!brief"
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|writeCmdLine
argument_list|(
literal|"!set silent true"
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|resultFile
operator|=
name|File
operator|.
name|createTempFile
argument_list|(
name|tabName
argument_list|,
literal|".out"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
literal|"true"
operator|.
name|equals
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"proxyAuth.debug"
argument_list|,
literal|"false"
argument_list|)
argument_list|)
condition|)
block|{
name|resultFile
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
block|}
name|writeCmdLine
argument_list|(
literal|"!record "
operator|+
name|resultFile
operator|.
name|getPath
argument_list|()
argument_list|,
name|out
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|stmt
range|:
name|dmlStmts
control|)
block|{
name|writeSqlLine
argument_list|(
name|stmt
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|stmt
range|:
name|selectStmts
control|)
block|{
name|writeSqlLine
argument_list|(
name|stmt
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|stmt
range|:
name|cleanUpStmts
control|)
block|{
name|writeSqlLine
argument_list|(
name|stmt
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
name|writeCmdLine
argument_list|(
literal|"!record"
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|writeCmdLine
argument_list|(
literal|"!quit"
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|File
name|tmpFile
init|=
name|File
operator|.
name|createTempFile
argument_list|(
name|tabName
argument_list|,
literal|".q"
argument_list|)
decl_stmt|;
name|tmpFile
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|scriptFileName
operator|=
name|tmpFile
operator|.
name|getPath
argument_list|()
expr_stmt|;
name|FileOutputStream
name|fstream
init|=
operator|new
name|FileOutputStream
argument_list|(
name|scriptFileName
argument_list|)
decl_stmt|;
name|out
operator|.
name|writeTo
argument_list|(
name|fstream
argument_list|)
expr_stmt|;
name|inpStream
operator|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|out
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|resultFile
return|;
block|}
comment|// write stmt + ";" + System.getProperty("line.separator")
specifier|private
specifier|static
name|void
name|writeSqlLine
parameter_list|(
name|String
name|stmt
parameter_list|,
name|OutputStream
name|out
parameter_list|)
throws|throws
name|Exception
block|{
name|out
operator|.
name|write
argument_list|(
name|stmt
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
literal|";"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"line.separator"
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|writeCmdLine
parameter_list|(
name|String
name|cmdLine
parameter_list|,
name|OutputStream
name|out
parameter_list|)
throws|throws
name|Exception
block|{
name|out
operator|.
name|write
argument_list|(
name|cmdLine
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"line.separator"
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|compareResults
parameter_list|(
name|File
name|file2
parameter_list|)
throws|throws
name|IOException
block|{
comment|// load the expected results
name|File
name|baseResultFile
init|=
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"proxyAuth.res.file"
argument_list|)
argument_list|,
literal|"data/files/ProxyAuth.res"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|FileUtils
operator|.
name|contentEquals
argument_list|(
name|baseResultFile
argument_list|,
name|file2
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"File compare failed: "
operator|+
name|file2
operator|.
name|getPath
argument_list|()
operator|+
literal|" differs"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

