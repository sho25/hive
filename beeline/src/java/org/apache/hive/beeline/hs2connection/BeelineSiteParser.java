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
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * This class implements HS2ConnectionFileParser for the named url configuration file named  * beeline-site.xml. The class looks for this file in ${user.home}/.beeline, ${HIVE_CONF_DIR} or  * /etc/conf/hive in that order and uses the first file found in the above locations.  */
end_comment

begin_class
specifier|public
class|class
name|BeelineSiteParser
implements|implements
name|HS2ConnectionFileParser
block|{
comment|/**    * Prefix string used for named jdbc uri configs    */
specifier|public
specifier|static
specifier|final
name|String
name|BEELINE_CONNECTION_NAMED_JDBC_URL_PREFIX
init|=
literal|"beeline.hs2.jdbc.url."
decl_stmt|;
comment|/**    * Property key used to provide the default named jdbc uri in the config file    */
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_NAMED_JDBC_URL_PROPERTY_KEY
init|=
literal|"default"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_BEELINE_SITE_FILE_NAME
init|=
literal|"beeline-site.xml"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_BEELINE_SITE_LOCATION
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.home"
argument_list|)
operator|+
name|File
operator|.
name|separator
operator|+
operator|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"os.name"
argument_list|)
operator|.
name|toLowerCase
argument_list|()
operator|.
name|indexOf
argument_list|(
literal|"windows"
argument_list|)
operator|!=
operator|-
literal|1
condition|?
literal|""
else|:
literal|"."
operator|)
operator|+
literal|"beeline"
operator|+
name|File
operator|.
name|separator
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ETC_HIVE_CONF_LOCATION
init|=
name|File
operator|.
name|separator
operator|+
literal|"etc"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"hive"
operator|+
name|File
operator|.
name|separator
operator|+
literal|"conf"
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|locations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|log
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|BeelineSiteParser
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|BeelineSiteParser
parameter_list|()
block|{
comment|// file locations to be searched in the correct order
name|locations
operator|.
name|add
argument_list|(
name|DEFAULT_BEELINE_SITE_LOCATION
operator|+
name|DEFAULT_BEELINE_SITE_FILE_NAME
argument_list|)
expr_stmt|;
if|if
condition|(
name|System
operator|.
name|getenv
argument_list|(
literal|"HIVE_CONF_DIR"
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|locations
operator|.
name|add
argument_list|(
name|System
operator|.
name|getenv
argument_list|(
literal|"HIVE_CONF_DIR"
argument_list|)
operator|+
name|File
operator|.
name|separator
operator|+
name|DEFAULT_BEELINE_SITE_FILE_NAME
argument_list|)
expr_stmt|;
block|}
name|locations
operator|.
name|add
argument_list|(
name|ETC_HIVE_CONF_LOCATION
operator|+
name|DEFAULT_BEELINE_SITE_FILE_NAME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|BeelineSiteParser
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|testLocations
parameter_list|)
block|{
if|if
condition|(
name|testLocations
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|locations
operator|.
name|addAll
argument_list|(
name|testLocations
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Properties
name|getConnectionProperties
parameter_list|()
throws|throws
name|BeelineSiteParseException
block|{
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|String
name|fileLocation
init|=
name|getFileLocation
argument_list|()
decl_stmt|;
if|if
condition|(
name|fileLocation
operator|==
literal|null
condition|)
block|{
name|log
operator|.
name|debug
argument_list|(
literal|"Could not find Beeline configuration file: {}"
argument_list|,
name|DEFAULT_BEELINE_SITE_FILE_NAME
argument_list|)
expr_stmt|;
return|return
name|props
return|;
block|}
name|log
operator|.
name|info
argument_list|(
literal|"Beeline configuration file at: {}"
argument_list|,
name|fileLocation
argument_list|)
expr_stmt|;
comment|// load the properties from config file
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|conf
operator|.
name|addResource
argument_list|(
operator|new
name|Path
argument_list|(
operator|new
name|File
argument_list|(
name|fileLocation
argument_list|)
operator|.
name|toURI
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|kv
range|:
name|conf
control|)
block|{
name|String
name|key
init|=
name|kv
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|key
operator|.
name|startsWith
argument_list|(
name|BEELINE_CONNECTION_NAMED_JDBC_URL_PREFIX
argument_list|)
condition|)
block|{
name|props
operator|.
name|setProperty
argument_list|(
name|key
operator|.
name|substring
argument_list|(
name|BEELINE_CONNECTION_NAMED_JDBC_URL_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
argument_list|,
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BeelineSiteParseException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|props
return|;
block|}
specifier|public
name|Properties
name|getConnectionProperties
parameter_list|(
name|String
name|propertyValue
parameter_list|)
throws|throws
name|BeelineSiteParseException
block|{
name|Properties
name|props
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|String
name|fileLocation
init|=
name|getFileLocation
argument_list|()
decl_stmt|;
if|if
condition|(
name|fileLocation
operator|==
literal|null
condition|)
block|{
name|log
operator|.
name|debug
argument_list|(
literal|"Could not find Beeline configuration file: {}"
argument_list|,
name|DEFAULT_BEELINE_SITE_FILE_NAME
argument_list|)
expr_stmt|;
return|return
name|props
return|;
block|}
name|log
operator|.
name|info
argument_list|(
literal|"Beeline configuration file at: {}"
argument_list|,
name|fileLocation
argument_list|)
expr_stmt|;
comment|// load the properties from config file
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|conf
operator|.
name|addResource
argument_list|(
operator|new
name|Path
argument_list|(
operator|new
name|File
argument_list|(
name|fileLocation
argument_list|)
operator|.
name|toURI
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|kv
range|:
name|conf
control|)
block|{
name|String
name|key
init|=
name|kv
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|key
operator|.
name|startsWith
argument_list|(
name|BEELINE_CONNECTION_NAMED_JDBC_URL_PREFIX
argument_list|)
operator|&&
operator|(
name|propertyValue
operator|.
name|equalsIgnoreCase
argument_list|(
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
operator|)
condition|)
block|{
name|props
operator|.
name|setProperty
argument_list|(
name|key
operator|.
name|substring
argument_list|(
name|BEELINE_CONNECTION_NAMED_JDBC_URL_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
argument_list|,
name|kv
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BeelineSiteParseException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|props
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|configExists
parameter_list|()
block|{
return|return
operator|(
name|getFileLocation
argument_list|()
operator|!=
literal|null
operator|)
return|;
block|}
comment|/*    * This method looks in locations specified above and returns the first location where the file    * exists. If the file does not exist in any one of the locations it returns null    */
name|String
name|getFileLocation
parameter_list|()
block|{
for|for
control|(
name|String
name|location
range|:
name|locations
control|)
block|{
if|if
condition|(
operator|new
name|File
argument_list|(
name|location
argument_list|)
operator|.
name|exists
argument_list|()
condition|)
block|{
return|return
name|location
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

