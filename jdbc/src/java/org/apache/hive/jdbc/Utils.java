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
name|jdbc
package|;
end_package

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
name|Types
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|thrift
operator|.
name|TStatus
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
name|thrift
operator|.
name|TStatusCode
import|;
end_import

begin_class
specifier|public
class|class
name|Utils
block|{
comment|/**     * The required prefix for the connection URL.     */
specifier|public
specifier|static
specifier|final
name|String
name|URL_PREFIX
init|=
literal|"jdbc:hive2://"
decl_stmt|;
comment|/**     * If host is provided, without a port.     */
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_PORT
init|=
literal|"10000"
decl_stmt|;
comment|/**    * Hive's default database name    */
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_DATABASE
init|=
literal|"default"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|URI_JDBC_PREFIX
init|=
literal|"jdbc:"
decl_stmt|;
specifier|public
specifier|static
class|class
name|JdbcConnectionParams
block|{
specifier|private
name|String
name|host
init|=
literal|null
decl_stmt|;
specifier|private
name|int
name|port
decl_stmt|;
specifier|private
name|String
name|dbName
init|=
name|DEFAULT_DATABASE
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hiveConfs
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
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hiveVars
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
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|sessionVars
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
specifier|private
name|boolean
name|isEmbeddedMode
init|=
literal|false
decl_stmt|;
specifier|public
name|JdbcConnectionParams
parameter_list|()
block|{     }
specifier|public
name|String
name|getHost
parameter_list|()
block|{
return|return
name|host
return|;
block|}
specifier|public
name|int
name|getPort
parameter_list|()
block|{
return|return
name|port
return|;
block|}
specifier|public
name|String
name|getDbName
parameter_list|()
block|{
return|return
name|dbName
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getHiveConfs
parameter_list|()
block|{
return|return
name|hiveConfs
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getHiveVars
parameter_list|()
block|{
return|return
name|hiveVars
return|;
block|}
specifier|public
name|boolean
name|isEmbeddedMode
parameter_list|()
block|{
return|return
name|isEmbeddedMode
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getSessionVars
parameter_list|()
block|{
return|return
name|sessionVars
return|;
block|}
specifier|public
name|void
name|setHost
parameter_list|(
name|String
name|host
parameter_list|)
block|{
name|this
operator|.
name|host
operator|=
name|host
expr_stmt|;
block|}
specifier|public
name|void
name|setPort
parameter_list|(
name|int
name|port
parameter_list|)
block|{
name|this
operator|.
name|port
operator|=
name|port
expr_stmt|;
block|}
specifier|public
name|void
name|setDbName
parameter_list|(
name|String
name|dbName
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
block|}
specifier|public
name|void
name|setHiveConfs
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hiveConfs
parameter_list|)
block|{
name|this
operator|.
name|hiveConfs
operator|=
name|hiveConfs
expr_stmt|;
block|}
specifier|public
name|void
name|setHiveVars
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|hiveVars
parameter_list|)
block|{
name|this
operator|.
name|hiveVars
operator|=
name|hiveVars
expr_stmt|;
block|}
specifier|public
name|void
name|setEmbeddedMode
parameter_list|(
name|boolean
name|embeddedMode
parameter_list|)
block|{
name|this
operator|.
name|isEmbeddedMode
operator|=
name|embeddedMode
expr_stmt|;
block|}
specifier|public
name|void
name|setSessionVars
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|sessionVars
parameter_list|)
block|{
name|this
operator|.
name|sessionVars
operator|=
name|sessionVars
expr_stmt|;
block|}
block|}
comment|// Verify success or success_with_info status, else throw SQLException
specifier|public
specifier|static
name|void
name|verifySuccessWithInfo
parameter_list|(
name|TStatus
name|status
parameter_list|)
throws|throws
name|SQLException
block|{
name|verifySuccess
argument_list|(
name|status
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Verify success status, else throw SQLException
specifier|public
specifier|static
name|void
name|verifySuccess
parameter_list|(
name|TStatus
name|status
parameter_list|)
throws|throws
name|SQLException
block|{
name|verifySuccess
argument_list|(
name|status
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|// Verify success and optionally with_info status, else throw SQLException
specifier|public
specifier|static
name|void
name|verifySuccess
parameter_list|(
name|TStatus
name|status
parameter_list|,
name|boolean
name|withInfo
parameter_list|)
throws|throws
name|SQLException
block|{
if|if
condition|(
operator|(
name|status
operator|.
name|getStatusCode
argument_list|()
operator|!=
name|TStatusCode
operator|.
name|SUCCESS_STATUS
operator|)
operator|&&
operator|(
name|withInfo
operator|&&
operator|(
name|status
operator|.
name|getStatusCode
argument_list|()
operator|!=
name|TStatusCode
operator|.
name|SUCCESS_WITH_INFO_STATUS
operator|)
operator|)
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
name|status
operator|.
name|getErrorMessage
argument_list|()
argument_list|,
name|status
operator|.
name|getSqlState
argument_list|()
argument_list|,
name|status
operator|.
name|getErrorCode
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**    * Parse JDBC connection URL    * The new format of the URL is jdbc:hive2://<host>:<port>/dbName;sess_var_list?hive_conf_list#hive_var_list    * where the optional sess, conf and var lists are semicolon separated<key>=<val> pairs. As before, if the    * host/port is not specified, it the driver runs an embedded hive.    * examples -    *  jdbc:hive2://ubuntu:11000/db2?hive.cli.conf.printheader=true;hive.exec.mode.local.auto.inputbytes.max=9999#stab=salesTable;icol=customerID    *  jdbc:hive2://?hive.cli.conf.printheader=true;hive.exec.mode.local.auto.inputbytes.max=9999#stab=salesTable;icol=customerID    *  jdbc:hive2://ubuntu:11000/db2;user=foo;password=bar    *    *  Connect to http://server:10001/hs2, with specified basicAuth credentials and initial database:    *     jdbc:hive2://server:10001/db;user=foo;password=bar?hive.server2.transport.mode=http;hive.server2.thrift.http.path=hs2    *    * Note that currently the session properties are not used.    *    * @param uri    * @return    */
specifier|public
specifier|static
name|JdbcConnectionParams
name|parseURL
parameter_list|(
name|String
name|uri
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|JdbcConnectionParams
name|connParams
init|=
operator|new
name|JdbcConnectionParams
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|uri
operator|.
name|startsWith
argument_list|(
name|URL_PREFIX
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Bad URL format"
argument_list|)
throw|;
block|}
comment|// For URLs with no other configuration
comment|// Don't parse them, but set embedded mode as true
if|if
condition|(
name|uri
operator|.
name|equalsIgnoreCase
argument_list|(
name|URL_PREFIX
argument_list|)
condition|)
block|{
name|connParams
operator|.
name|setEmbeddedMode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
name|connParams
return|;
block|}
name|URI
name|jdbcURI
init|=
name|URI
operator|.
name|create
argument_list|(
name|uri
operator|.
name|substring
argument_list|(
name|URI_JDBC_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
comment|// Check to prevent unintentional use of embedded mode. A missing "/"
comment|// to separate the 'path' portion of URI can result in this.
comment|// The missing "/" common typo while using secure mode, eg of such url -
comment|// jdbc:hive2://localhost:10000;principal=hive/HiveServer2Host@YOUR-REALM.COM
if|if
condition|(
operator|(
name|jdbcURI
operator|.
name|getAuthority
argument_list|()
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|jdbcURI
operator|.
name|getHost
argument_list|()
operator|==
literal|null
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Bad URL format. Hostname not found "
operator|+
literal|" in authority part of the url: "
operator|+
name|jdbcURI
operator|.
name|getAuthority
argument_list|()
operator|+
literal|". Are you missing a '/' after the hostname ?"
argument_list|)
throw|;
block|}
name|connParams
operator|.
name|setHost
argument_list|(
name|jdbcURI
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|connParams
operator|.
name|getHost
argument_list|()
operator|==
literal|null
condition|)
block|{
name|connParams
operator|.
name|setEmbeddedMode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|port
init|=
name|jdbcURI
operator|.
name|getPort
argument_list|()
decl_stmt|;
if|if
condition|(
name|port
operator|==
operator|-
literal|1
condition|)
block|{
name|port
operator|=
name|Integer
operator|.
name|valueOf
argument_list|(
name|DEFAULT_PORT
argument_list|)
expr_stmt|;
block|}
name|connParams
operator|.
name|setPort
argument_list|(
name|port
argument_list|)
expr_stmt|;
block|}
comment|// key=value pattern
name|Pattern
name|pattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"([^;]*)=([^;]*)[;]?"
argument_list|)
decl_stmt|;
comment|// dbname and session settings
name|String
name|sessVars
init|=
name|jdbcURI
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|sessVars
operator|!=
literal|null
operator|)
operator|&&
operator|!
name|sessVars
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|String
name|dbName
init|=
literal|""
decl_stmt|;
comment|// removing leading '/' returned by getPath()
name|sessVars
operator|=
name|sessVars
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|sessVars
operator|.
name|contains
argument_list|(
literal|";"
argument_list|)
condition|)
block|{
comment|// only dbname is provided
name|dbName
operator|=
name|sessVars
expr_stmt|;
block|}
else|else
block|{
comment|// we have dbname followed by session parameters
name|dbName
operator|=
name|sessVars
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sessVars
operator|.
name|indexOf
argument_list|(
literal|';'
argument_list|)
argument_list|)
expr_stmt|;
name|sessVars
operator|=
name|sessVars
operator|.
name|substring
argument_list|(
name|sessVars
operator|.
name|indexOf
argument_list|(
literal|';'
argument_list|)
operator|+
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|sessVars
operator|!=
literal|null
condition|)
block|{
name|Matcher
name|sessMatcher
init|=
name|pattern
operator|.
name|matcher
argument_list|(
name|sessVars
argument_list|)
decl_stmt|;
while|while
condition|(
name|sessMatcher
operator|.
name|find
argument_list|()
condition|)
block|{
name|connParams
operator|.
name|getSessionVars
argument_list|()
operator|.
name|put
argument_list|(
name|sessMatcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|,
name|sessMatcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|dbName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|connParams
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
block|}
block|}
comment|// parse hive conf settings
name|String
name|confStr
init|=
name|jdbcURI
operator|.
name|getQuery
argument_list|()
decl_stmt|;
if|if
condition|(
name|confStr
operator|!=
literal|null
condition|)
block|{
name|Matcher
name|confMatcher
init|=
name|pattern
operator|.
name|matcher
argument_list|(
name|confStr
argument_list|)
decl_stmt|;
while|while
condition|(
name|confMatcher
operator|.
name|find
argument_list|()
condition|)
block|{
name|connParams
operator|.
name|getHiveConfs
argument_list|()
operator|.
name|put
argument_list|(
name|confMatcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|,
name|confMatcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// parse hive var settings
name|String
name|varStr
init|=
name|jdbcURI
operator|.
name|getFragment
argument_list|()
decl_stmt|;
if|if
condition|(
name|varStr
operator|!=
literal|null
condition|)
block|{
name|Matcher
name|varMatcher
init|=
name|pattern
operator|.
name|matcher
argument_list|(
name|varStr
argument_list|)
decl_stmt|;
while|while
condition|(
name|varMatcher
operator|.
name|find
argument_list|()
condition|)
block|{
name|connParams
operator|.
name|getHiveVars
argument_list|()
operator|.
name|put
argument_list|(
name|varMatcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|,
name|varMatcher
operator|.
name|group
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|connParams
return|;
block|}
comment|/**    * Takes a version string delmited by '.' and '-' characters    * and returns a partial version.    *    * @param fullVersion    *          version string.    * @param tokenPosition    *          position of version string to get starting at 0. eg, for a X.x.xxx    *          string, 0 will return the major version, 1 will return minor    *          version.    * @return version part, or -1 if version string was malformed.    */
specifier|static
name|int
name|getVersionPart
parameter_list|(
name|String
name|fullVersion
parameter_list|,
name|int
name|position
parameter_list|)
block|{
name|int
name|version
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
name|String
index|[]
name|tokens
init|=
name|fullVersion
operator|.
name|split
argument_list|(
literal|"[\\.-]"
argument_list|)
decl_stmt|;
comment|//$NON-NLS-1$
if|if
condition|(
name|tokens
operator|!=
literal|null
operator|&&
name|tokens
operator|.
name|length
operator|>
literal|1
operator|&&
name|tokens
index|[
name|position
index|]
operator|!=
literal|null
condition|)
block|{
name|version
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|tokens
index|[
name|position
index|]
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|version
operator|=
operator|-
literal|1
expr_stmt|;
block|}
return|return
name|version
return|;
block|}
block|}
end_class

end_unit

