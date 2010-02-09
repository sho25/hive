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
name|Driver
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|DriverPropertyInfo
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
name|util
operator|.
name|Properties
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

begin_comment
comment|/**  * HiveDriver.  *  */
end_comment

begin_class
specifier|public
class|class
name|HiveDriver
implements|implements
name|Driver
block|{
static|static
block|{
try|try
block|{
name|java
operator|.
name|sql
operator|.
name|DriverManager
operator|.
name|registerDriver
argument_list|(
operator|new
name|HiveDriver
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SQLException
name|e
parameter_list|)
block|{
comment|// TODO Auto-generated catch block
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Major version number of this driver.    */
specifier|private
specifier|static
specifier|final
name|int
name|MAJOR_VERSION
init|=
literal|0
decl_stmt|;
comment|/**    * Minor version number of this driver.    */
specifier|private
specifier|static
specifier|final
name|int
name|MINOR_VERSION
init|=
literal|0
decl_stmt|;
comment|/**    * Is this driver JDBC compliant?    */
specifier|private
specifier|static
specifier|final
name|boolean
name|JDBC_COMPLIANT
init|=
literal|false
decl_stmt|;
comment|/**    * The required prefix for the connection URL.    */
specifier|private
specifier|static
specifier|final
name|String
name|URL_PREFIX
init|=
literal|"jdbc:hive://"
decl_stmt|;
comment|/**    * If host is provided, without a port.    */
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_PORT
init|=
literal|"10000"
decl_stmt|;
comment|/**    * Property key for the database name.    */
specifier|private
specifier|static
specifier|final
name|String
name|DBNAME_PROPERTY_KEY
init|=
literal|"DBNAME"
decl_stmt|;
comment|/**    * Property key for the Hive Server host.    */
specifier|private
specifier|static
specifier|final
name|String
name|HOST_PROPERTY_KEY
init|=
literal|"HOST"
decl_stmt|;
comment|/**    * Property key for the Hive Server port.    */
specifier|private
specifier|static
specifier|final
name|String
name|PORT_PROPERTY_KEY
init|=
literal|"PORT"
decl_stmt|;
comment|/**    *    */
specifier|public
name|HiveDriver
parameter_list|()
block|{
comment|// TODO Auto-generated constructor stub
name|SecurityManager
name|security
init|=
name|System
operator|.
name|getSecurityManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|security
operator|!=
literal|null
condition|)
block|{
name|security
operator|.
name|checkWrite
argument_list|(
literal|"foobah"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Checks whether a given url is in a valid format.    *     * The current uri format is: jdbc:hive://[host[:port]]    *     * jdbc:hive:// - run in embedded mode jdbc:hive://localhost - connect to    * localhost default port (10000) jdbc:hive://localhost:5050 - connect to    * localhost port 5050    *     * TODO: - write a better regex. - decide on uri format    */
specifier|public
name|boolean
name|acceptsURL
parameter_list|(
name|String
name|url
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
name|Pattern
operator|.
name|matches
argument_list|(
name|URL_PREFIX
operator|+
literal|".*"
argument_list|,
name|url
argument_list|)
return|;
block|}
specifier|public
name|Connection
name|connect
parameter_list|(
name|String
name|url
parameter_list|,
name|Properties
name|info
parameter_list|)
throws|throws
name|SQLException
block|{
return|return
operator|new
name|HiveConnection
argument_list|(
name|url
argument_list|,
name|info
argument_list|)
return|;
block|}
comment|/**    * Returns the major version of this driver.    */
specifier|public
name|int
name|getMajorVersion
parameter_list|()
block|{
return|return
name|MAJOR_VERSION
return|;
block|}
comment|/**    * Returns the minor version of this driver.    */
specifier|public
name|int
name|getMinorVersion
parameter_list|()
block|{
return|return
name|MINOR_VERSION
return|;
block|}
specifier|public
name|DriverPropertyInfo
index|[]
name|getPropertyInfo
parameter_list|(
name|String
name|url
parameter_list|,
name|Properties
name|info
parameter_list|)
throws|throws
name|SQLException
block|{
if|if
condition|(
name|info
operator|==
literal|null
condition|)
block|{
name|info
operator|=
operator|new
name|Properties
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|url
operator|!=
literal|null
operator|)
operator|&&
name|url
operator|.
name|startsWith
argument_list|(
name|URL_PREFIX
argument_list|)
condition|)
block|{
name|info
operator|=
name|parseURL
argument_list|(
name|url
argument_list|,
name|info
argument_list|)
expr_stmt|;
block|}
name|DriverPropertyInfo
name|hostProp
init|=
operator|new
name|DriverPropertyInfo
argument_list|(
name|HOST_PROPERTY_KEY
argument_list|,
name|info
operator|.
name|getProperty
argument_list|(
name|HOST_PROPERTY_KEY
argument_list|,
literal|""
argument_list|)
argument_list|)
decl_stmt|;
name|hostProp
operator|.
name|required
operator|=
literal|false
expr_stmt|;
name|hostProp
operator|.
name|description
operator|=
literal|"Hostname of Hive Server"
expr_stmt|;
name|DriverPropertyInfo
name|portProp
init|=
operator|new
name|DriverPropertyInfo
argument_list|(
name|PORT_PROPERTY_KEY
argument_list|,
name|info
operator|.
name|getProperty
argument_list|(
name|PORT_PROPERTY_KEY
argument_list|,
literal|""
argument_list|)
argument_list|)
decl_stmt|;
name|portProp
operator|.
name|required
operator|=
literal|false
expr_stmt|;
name|portProp
operator|.
name|description
operator|=
literal|"Port number of Hive Server"
expr_stmt|;
name|DriverPropertyInfo
name|dbProp
init|=
operator|new
name|DriverPropertyInfo
argument_list|(
name|DBNAME_PROPERTY_KEY
argument_list|,
name|info
operator|.
name|getProperty
argument_list|(
name|DBNAME_PROPERTY_KEY
argument_list|,
literal|"default"
argument_list|)
argument_list|)
decl_stmt|;
name|dbProp
operator|.
name|required
operator|=
literal|false
expr_stmt|;
name|dbProp
operator|.
name|description
operator|=
literal|"Database name"
expr_stmt|;
name|DriverPropertyInfo
index|[]
name|dpi
init|=
operator|new
name|DriverPropertyInfo
index|[
literal|3
index|]
decl_stmt|;
name|dpi
index|[
literal|0
index|]
operator|=
name|hostProp
expr_stmt|;
name|dpi
index|[
literal|1
index|]
operator|=
name|portProp
expr_stmt|;
name|dpi
index|[
literal|2
index|]
operator|=
name|dbProp
expr_stmt|;
return|return
name|dpi
return|;
block|}
comment|/**    * Returns whether the driver is JDBC compliant.    */
specifier|public
name|boolean
name|jdbcCompliant
parameter_list|()
block|{
return|return
name|JDBC_COMPLIANT
return|;
block|}
comment|/**    * Takes a url in the form of jdbc:hive://[hostname]:[port]/[db_name] and    * parses it. Everything after jdbc:hive// is optional.    *     * @param url    * @param defaults    * @return    * @throws java.sql.SQLException    */
specifier|private
name|Properties
name|parseURL
parameter_list|(
name|String
name|url
parameter_list|,
name|Properties
name|defaults
parameter_list|)
throws|throws
name|SQLException
block|{
name|Properties
name|urlProps
init|=
operator|(
name|defaults
operator|!=
literal|null
operator|)
condition|?
operator|new
name|Properties
argument_list|(
name|defaults
argument_list|)
else|:
operator|new
name|Properties
argument_list|()
decl_stmt|;
if|if
condition|(
name|url
operator|==
literal|null
operator|||
operator|!
name|url
operator|.
name|startsWith
argument_list|(
name|URL_PREFIX
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SQLException
argument_list|(
literal|"Invalid connection url: "
operator|+
name|url
argument_list|)
throw|;
block|}
if|if
condition|(
name|url
operator|.
name|length
argument_list|()
operator|<=
name|URL_PREFIX
operator|.
name|length
argument_list|()
condition|)
block|{
return|return
name|urlProps
return|;
block|}
comment|// [hostname]:[port]/[db_name]
name|String
name|connectionInfo
init|=
name|url
operator|.
name|substring
argument_list|(
name|URL_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
comment|// [hostname]:[port] [db_name]
name|String
index|[]
name|hostPortAndDatabase
init|=
name|connectionInfo
operator|.
name|split
argument_list|(
literal|"/"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
comment|// [hostname]:[port]
if|if
condition|(
name|hostPortAndDatabase
index|[
literal|0
index|]
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|String
index|[]
name|hostAndPort
init|=
name|hostPortAndDatabase
index|[
literal|0
index|]
operator|.
name|split
argument_list|(
literal|":"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|urlProps
operator|.
name|put
argument_list|(
name|HOST_PROPERTY_KEY
argument_list|,
name|hostAndPort
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|hostAndPort
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|urlProps
operator|.
name|put
argument_list|(
name|PORT_PROPERTY_KEY
argument_list|,
name|hostAndPort
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|urlProps
operator|.
name|put
argument_list|(
name|PORT_PROPERTY_KEY
argument_list|,
name|DEFAULT_PORT
argument_list|)
expr_stmt|;
block|}
block|}
comment|// [db_name]
if|if
condition|(
name|hostPortAndDatabase
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|urlProps
operator|.
name|put
argument_list|(
name|DBNAME_PROPERTY_KEY
argument_list|,
name|hostPortAndDatabase
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|urlProps
return|;
block|}
block|}
end_class

end_unit

