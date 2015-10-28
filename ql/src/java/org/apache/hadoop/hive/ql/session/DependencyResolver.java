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
name|ql
operator|.
name|session
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
name|net
operator|.
name|URISyntaxException
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
name|LinkedList
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
name|IOException
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
name|session
operator|.
name|SessionState
operator|.
name|LogHelper
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
name|groovy
operator|.
name|grape
operator|.
name|Grape
import|;
end_import

begin_import
import|import
name|groovy
operator|.
name|lang
operator|.
name|GroovyClassLoader
import|;
end_import

begin_class
specifier|public
class|class
name|DependencyResolver
block|{
specifier|private
specifier|static
specifier|final
name|String
name|HIVE_HOME
init|=
literal|"HIVE_HOME"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HIVE_CONF_DIR
init|=
literal|"HIVE_CONF_DIR"
decl_stmt|;
specifier|private
name|String
name|ivysettingsPath
decl_stmt|;
specifier|private
specifier|static
name|LogHelper
name|_console
init|=
operator|new
name|LogHelper
argument_list|(
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"DependencyResolver"
argument_list|)
argument_list|)
decl_stmt|;
specifier|public
name|DependencyResolver
parameter_list|()
block|{
comment|// Check if HIVE_CONF_DIR is defined
if|if
condition|(
name|System
operator|.
name|getenv
argument_list|()
operator|.
name|containsKey
argument_list|(
name|HIVE_CONF_DIR
argument_list|)
condition|)
block|{
name|ivysettingsPath
operator|=
name|System
operator|.
name|getenv
argument_list|()
operator|.
name|get
argument_list|(
name|HIVE_CONF_DIR
argument_list|)
operator|+
literal|"/ivysettings.xml"
expr_stmt|;
block|}
comment|// If HIVE_CONF_DIR is not defined or file is not found in HIVE_CONF_DIR then check HIVE_HOME/conf
if|if
condition|(
name|ivysettingsPath
operator|==
literal|null
operator|||
operator|!
operator|(
operator|new
name|File
argument_list|(
name|ivysettingsPath
argument_list|)
operator|.
name|exists
argument_list|()
operator|)
condition|)
block|{
if|if
condition|(
name|System
operator|.
name|getenv
argument_list|()
operator|.
name|containsKey
argument_list|(
name|HIVE_HOME
argument_list|)
condition|)
block|{
name|ivysettingsPath
operator|=
name|System
operator|.
name|getenv
argument_list|()
operator|.
name|get
argument_list|(
name|HIVE_HOME
argument_list|)
operator|+
literal|"/conf/ivysettings.xml"
expr_stmt|;
block|}
block|}
comment|// If HIVE_HOME is not defined or file is not found in HIVE_HOME/conf then load default ivysettings.xml from class loader
if|if
condition|(
name|ivysettingsPath
operator|==
literal|null
operator|||
operator|!
operator|(
operator|new
name|File
argument_list|(
name|ivysettingsPath
argument_list|)
operator|.
name|exists
argument_list|()
operator|)
condition|)
block|{
name|URL
name|ivysetttingsResource
init|=
name|ClassLoader
operator|.
name|getSystemResource
argument_list|(
literal|"ivysettings.xml"
argument_list|)
decl_stmt|;
if|if
condition|(
name|ivysetttingsResource
operator|!=
literal|null
condition|)
block|{
name|ivysettingsPath
operator|=
name|ivysetttingsResource
operator|.
name|getFile
argument_list|()
expr_stmt|;
name|_console
operator|.
name|printInfo
argument_list|(
literal|"ivysettings.xml file not found in HIVE_HOME or HIVE_CONF_DIR,"
operator|+
name|ivysettingsPath
operator|+
literal|" will be used"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    *    * @param uri    * @return List of URIs of downloaded jars    * @throws URISyntaxException    * @throws IOException    */
specifier|public
name|List
argument_list|<
name|URI
argument_list|>
name|downloadDependencies
parameter_list|(
name|URI
name|uri
parameter_list|)
throws|throws
name|URISyntaxException
throws|,
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|dependencyMap
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
name|String
name|authority
init|=
name|uri
operator|.
name|getAuthority
argument_list|()
decl_stmt|;
if|if
condition|(
name|authority
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|URISyntaxException
argument_list|(
name|authority
argument_list|,
literal|"Invalid url: Expected 'org:module:version', found null"
argument_list|)
throw|;
block|}
name|String
index|[]
name|authorityTokens
init|=
name|authority
operator|.
name|toLowerCase
argument_list|()
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|authorityTokens
operator|.
name|length
operator|!=
literal|3
condition|)
block|{
throw|throw
operator|new
name|URISyntaxException
argument_list|(
name|authority
argument_list|,
literal|"Invalid url: Expected 'org:module:version', found "
operator|+
name|authority
argument_list|)
throw|;
block|}
name|dependencyMap
operator|.
name|put
argument_list|(
literal|"org"
argument_list|,
name|authorityTokens
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|dependencyMap
operator|.
name|put
argument_list|(
literal|"module"
argument_list|,
name|authorityTokens
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|dependencyMap
operator|.
name|put
argument_list|(
literal|"version"
argument_list|,
name|authorityTokens
index|[
literal|2
index|]
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|queryMap
init|=
name|parseQueryString
argument_list|(
name|uri
operator|.
name|getQuery
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|queryMap
operator|!=
literal|null
condition|)
block|{
name|dependencyMap
operator|.
name|putAll
argument_list|(
name|queryMap
argument_list|)
expr_stmt|;
block|}
return|return
name|grab
argument_list|(
name|dependencyMap
argument_list|)
return|;
block|}
comment|/**    * @param queryString    * @return queryMap Map which contains grape parameters such as transitive, exclude, ext and classifier.    * Example: Input:  ext=jar&exclude=org.mortbay.jetty:jetty&transitive=true    *          Output:  {[ext]:[jar], [exclude]:{[group]:[org.mortbay.jetty], [module]:[jetty]}, [transitive]:[true]}    * @throws URISyntaxException    */
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|parseQueryString
parameter_list|(
name|String
name|queryString
parameter_list|)
throws|throws
name|URISyntaxException
block|{
if|if
condition|(
name|queryString
operator|==
literal|null
operator|||
name|queryString
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|excludeList
init|=
operator|new
name|LinkedList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|queryMap
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
name|String
index|[]
name|mapTokens
init|=
name|queryString
operator|.
name|split
argument_list|(
literal|"&"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|tokens
range|:
name|mapTokens
control|)
block|{
name|String
index|[]
name|mapPair
init|=
name|tokens
operator|.
name|split
argument_list|(
literal|"="
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapPair
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid query string: "
operator|+
name|queryString
argument_list|)
throw|;
block|}
if|if
condition|(
name|mapPair
index|[
literal|0
index|]
operator|.
name|equals
argument_list|(
literal|"exclude"
argument_list|)
condition|)
block|{
name|excludeList
operator|.
name|addAll
argument_list|(
name|computeExcludeList
argument_list|(
name|mapPair
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|mapPair
index|[
literal|0
index|]
operator|.
name|equals
argument_list|(
literal|"transitive"
argument_list|)
condition|)
block|{
if|if
condition|(
name|mapPair
index|[
literal|1
index|]
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
literal|"true"
argument_list|)
condition|)
block|{
name|queryMap
operator|.
name|put
argument_list|(
name|mapPair
index|[
literal|0
index|]
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|queryMap
operator|.
name|put
argument_list|(
name|mapPair
index|[
literal|0
index|]
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|queryMap
operator|.
name|put
argument_list|(
name|mapPair
index|[
literal|0
index|]
argument_list|,
name|mapPair
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|excludeList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|queryMap
operator|.
name|put
argument_list|(
literal|"exclude"
argument_list|,
name|excludeList
argument_list|)
expr_stmt|;
block|}
return|return
name|queryMap
return|;
block|}
specifier|private
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|computeExcludeList
parameter_list|(
name|String
name|excludeString
parameter_list|)
throws|throws
name|URISyntaxException
block|{
name|String
name|excludes
index|[]
init|=
name|excludeString
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|excludeList
init|=
operator|new
name|LinkedList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|exclude
range|:
name|excludes
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tempMap
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
name|String
name|args
index|[]
init|=
name|exclude
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|URISyntaxException
argument_list|(
name|excludeString
argument_list|,
literal|"Invalid exclude string: expected 'org:module,org:module,..', found "
operator|+
name|excludeString
argument_list|)
throw|;
block|}
name|tempMap
operator|.
name|put
argument_list|(
literal|"group"
argument_list|,
name|args
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|tempMap
operator|.
name|put
argument_list|(
literal|"module"
argument_list|,
name|args
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|excludeList
operator|.
name|add
argument_list|(
name|tempMap
argument_list|)
expr_stmt|;
block|}
return|return
name|excludeList
return|;
block|}
comment|/**    *    * @param dependencies    * @return List of URIs of downloaded jars    * @throws IOException    */
specifier|private
name|List
argument_list|<
name|URI
argument_list|>
name|grab
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|dependencies
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|args
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
name|URI
index|[]
name|localUrls
decl_stmt|;
comment|//grape expects excludes key in args map
if|if
condition|(
name|dependencies
operator|.
name|containsKey
argument_list|(
literal|"exclude"
argument_list|)
condition|)
block|{
name|args
operator|.
name|put
argument_list|(
literal|"excludes"
argument_list|,
name|dependencies
operator|.
name|get
argument_list|(
literal|"exclude"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//Set transitive to true by default
if|if
condition|(
operator|!
name|dependencies
operator|.
name|containsKey
argument_list|(
literal|"transitive"
argument_list|)
condition|)
block|{
name|dependencies
operator|.
name|put
argument_list|(
literal|"transitive"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|args
operator|.
name|put
argument_list|(
literal|"classLoader"
argument_list|,
operator|new
name|GroovyClassLoader
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"grape.config"
argument_list|,
name|ivysettingsPath
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"groovy.grape.report.downloads"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|localUrls
operator|=
name|Grape
operator|.
name|resolve
argument_list|(
name|args
argument_list|,
name|dependencies
argument_list|)
expr_stmt|;
if|if
condition|(
name|localUrls
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Not able to download all the dependencies.."
argument_list|)
throw|;
block|}
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|localUrls
argument_list|)
return|;
block|}
block|}
end_class

end_unit

