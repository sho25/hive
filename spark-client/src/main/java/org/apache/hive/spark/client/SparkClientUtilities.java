begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|spark
operator|.
name|client
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
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
name|collect
operator|.
name|ImmutableList
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
name|collect
operator|.
name|Lists
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
name|FileNotFoundException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|MalformedURLException
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
name|net
operator|.
name|URLClassLoader
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
name|Collections
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
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|lang3
operator|.
name|StringUtils
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|SparkContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|util
operator|.
name|MutableURLClassLoader
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
name|FileSystem
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
name|scala
operator|.
name|Option
import|;
end_import

begin_class
specifier|public
class|class
name|SparkClientUtilities
block|{
specifier|protected
specifier|static
specifier|final
specifier|transient
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SparkClientUtilities
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|downloadedFiles
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HIVE_KRYO_REG_NAME
init|=
literal|"org.apache.hive.spark.HiveKryoRegistrator"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|HIVE_KRYO_REG_JAR_NAME
init|=
literal|"hive-kryo-registrator"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ImmutableList
argument_list|<
name|String
argument_list|>
name|ERROR_KEYWORDS
init|=
name|ImmutableList
operator|.
name|of
argument_list|(
literal|"error"
argument_list|,
literal|"exception"
argument_list|)
decl_stmt|;
comment|/**    * Add new elements to the classpath.    * Returns currently known class paths as best effort. For system class loader, this may return empty.    * In such cases we will anyway create new child class loader in {@link #addToClassPath(Map, Configuration, File)},    * so all new class paths will be added and next time we will have a URLClassLoader to work with.    */
specifier|private
specifier|static
name|List
argument_list|<
name|URL
argument_list|>
name|getCurrentClassPaths
parameter_list|(
name|ClassLoader
name|parentLoader
parameter_list|)
block|{
if|if
condition|(
name|parentLoader
operator|instanceof
name|URLClassLoader
condition|)
block|{
return|return
name|Lists
operator|.
name|newArrayList
argument_list|(
operator|(
operator|(
name|URLClassLoader
operator|)
name|parentLoader
operator|)
operator|.
name|getURLs
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
block|}
comment|/**    * Add new elements to the classpath by creating a child ClassLoader containing both old and new paths.    * This method supports downloading HDFS files to local FS if missing from cache or later timestamp.    * However, this method has no tricks working around HIVE-11878, like UDFClassLoader....    *    * @param newPaths Map of classpath elements and corresponding timestamp    * @return locally accessible files corresponding to the newPaths    */
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|addToClassPath
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|newPaths
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|File
name|localTmpDir
parameter_list|)
throws|throws
name|Exception
block|{
name|ClassLoader
name|parentLoader
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|URL
argument_list|>
name|curPath
init|=
name|getCurrentClassPaths
argument_list|(
name|parentLoader
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|localNewPaths
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|boolean
name|newPathAdded
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|newPaths
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|URL
name|newUrl
init|=
name|urlFromPathString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|conf
argument_list|,
name|localTmpDir
argument_list|)
decl_stmt|;
name|localNewPaths
operator|.
name|add
argument_list|(
name|newUrl
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|newUrl
operator|!=
literal|null
operator|&&
operator|!
name|curPath
operator|.
name|contains
argument_list|(
name|newUrl
argument_list|)
condition|)
block|{
name|curPath
operator|.
name|add
argument_list|(
name|newUrl
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Added jar["
operator|+
name|newUrl
operator|+
literal|"] to classpath."
argument_list|)
expr_stmt|;
name|newPathAdded
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
name|newPathAdded
condition|)
block|{
name|URLClassLoader
name|newLoader
init|=
operator|new
name|URLClassLoader
argument_list|(
name|curPath
operator|.
name|toArray
argument_list|(
operator|new
name|URL
index|[
name|curPath
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|parentLoader
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|setContextClassLoader
argument_list|(
name|newLoader
argument_list|)
expr_stmt|;
block|}
return|return
name|localNewPaths
return|;
block|}
comment|/**    * Create a URL from a string representing a path to a local file.    * The path string can be just a path, or can start with file:/, file:///    *    * @param path path string    * @return    */
specifier|private
specifier|static
name|URL
name|urlFromPathString
parameter_list|(
name|String
name|path
parameter_list|,
name|Long
name|timeStamp
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|File
name|localTmpDir
parameter_list|)
block|{
name|URL
name|url
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|StringUtils
operator|.
name|indexOf
argument_list|(
name|path
argument_list|,
literal|"file:/"
argument_list|)
operator|==
literal|0
condition|)
block|{
name|url
operator|=
operator|new
name|URL
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|StringUtils
operator|.
name|indexOf
argument_list|(
name|path
argument_list|,
literal|"hdfs:/"
argument_list|)
operator|==
literal|0
operator|||
name|StringUtils
operator|.
name|indexOf
argument_list|(
name|path
argument_list|,
literal|"viewfs:/"
argument_list|)
operator|==
literal|0
condition|)
block|{
name|Path
name|remoteFile
init|=
operator|new
name|Path
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|Path
name|localFile
init|=
operator|new
name|Path
argument_list|(
name|localTmpDir
operator|.
name|getAbsolutePath
argument_list|()
operator|+
name|File
operator|.
name|separator
operator|+
name|remoteFile
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Long
name|currentTS
init|=
name|downloadedFiles
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentTS
operator|==
literal|null
condition|)
block|{
name|currentTS
operator|=
operator|-
literal|1L
expr_stmt|;
block|}
if|if
condition|(
operator|!
operator|new
name|File
argument_list|(
name|localFile
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|exists
argument_list|()
operator|||
name|currentTS
operator|<
name|timeStamp
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Copying "
operator|+
name|remoteFile
operator|+
literal|" to "
operator|+
name|localFile
argument_list|)
expr_stmt|;
name|FileSystem
name|remoteFS
init|=
name|remoteFile
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|remoteFS
operator|.
name|copyToLocalFile
argument_list|(
name|remoteFile
argument_list|,
name|localFile
argument_list|)
expr_stmt|;
name|downloadedFiles
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|timeStamp
argument_list|)
expr_stmt|;
block|}
return|return
name|urlFromPathString
argument_list|(
name|localFile
operator|.
name|toString
argument_list|()
argument_list|,
name|timeStamp
argument_list|,
name|conf
argument_list|,
name|localTmpDir
argument_list|)
return|;
block|}
else|else
block|{
name|url
operator|=
operator|new
name|File
argument_list|(
name|path
argument_list|)
operator|.
name|toURL
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|err
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Bad URL "
operator|+
name|path
operator|+
literal|", ignoring path"
argument_list|,
name|err
argument_list|)
expr_stmt|;
block|}
return|return
name|url
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isYarnClusterMode
parameter_list|(
name|String
name|master
parameter_list|,
name|String
name|deployMode
parameter_list|)
block|{
return|return
literal|"yarn-cluster"
operator|.
name|equals
argument_list|(
name|master
argument_list|)
operator|||
operator|(
literal|"yarn"
operator|.
name|equals
argument_list|(
name|master
argument_list|)
operator|&&
literal|"cluster"
operator|.
name|equals
argument_list|(
name|deployMode
argument_list|)
operator|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isYarnClientMode
parameter_list|(
name|String
name|master
parameter_list|,
name|String
name|deployMode
parameter_list|)
block|{
return|return
literal|"yarn-client"
operator|.
name|equals
argument_list|(
name|master
argument_list|)
operator|||
operator|(
literal|"yarn"
operator|.
name|equals
argument_list|(
name|master
argument_list|)
operator|&&
literal|"client"
operator|.
name|equals
argument_list|(
name|deployMode
argument_list|)
operator|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isYarnMaster
parameter_list|(
name|String
name|master
parameter_list|)
block|{
return|return
name|master
operator|!=
literal|null
operator|&&
name|master
operator|.
name|startsWith
argument_list|(
literal|"yarn"
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isLocalMaster
parameter_list|(
name|String
name|master
parameter_list|)
block|{
return|return
name|master
operator|!=
literal|null
operator|&&
name|master
operator|.
name|startsWith
argument_list|(
literal|"local"
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|getDeployModeFromMaster
parameter_list|(
name|String
name|master
parameter_list|)
block|{
if|if
condition|(
name|master
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|master
operator|.
name|equals
argument_list|(
literal|"yarn-client"
argument_list|)
condition|)
block|{
return|return
literal|"client"
return|;
block|}
elseif|else
if|if
condition|(
name|master
operator|.
name|equals
argument_list|(
literal|"yarn-cluster"
argument_list|)
condition|)
block|{
return|return
literal|"cluster"
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
specifier|public
specifier|static
name|String
name|findKryoRegistratorJar
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|FileNotFoundException
block|{
comment|// find the jar in local maven repo for testing
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_IN_TEST
argument_list|)
condition|)
block|{
name|String
name|repo
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"maven.local.repository"
argument_list|)
decl_stmt|;
name|String
name|version
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"hive.version"
argument_list|)
decl_stmt|;
name|String
name|jarName
init|=
name|HIVE_KRYO_REG_JAR_NAME
operator|+
literal|"-"
operator|+
name|version
operator|+
literal|".jar"
decl_stmt|;
name|String
index|[]
name|parts
init|=
operator|new
name|String
index|[]
block|{
name|repo
block|,
literal|"org"
block|,
literal|"apache"
block|,
literal|"hive"
block|,
name|HIVE_KRYO_REG_JAR_NAME
block|,
name|version
block|,
name|jarName
block|}
decl_stmt|;
name|String
name|jar
init|=
name|Joiner
operator|.
name|on
argument_list|(
name|File
operator|.
name|separator
argument_list|)
operator|.
name|join
argument_list|(
name|parts
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|new
name|File
argument_list|(
name|jar
argument_list|)
operator|.
name|exists
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
name|jar
operator|+
literal|" doesn't exist."
argument_list|)
throw|;
block|}
return|return
name|jar
return|;
block|}
name|Option
argument_list|<
name|String
argument_list|>
name|option
init|=
name|SparkContext
operator|.
name|jarOfClass
argument_list|(
name|SparkClientUtilities
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|option
operator|.
name|isDefined
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
literal|"Cannot find the path to hive-exec.jar"
argument_list|)
throw|;
block|}
name|File
name|path
init|=
operator|new
name|File
argument_list|(
name|option
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
name|File
index|[]
name|jars
init|=
name|path
operator|.
name|getParentFile
argument_list|()
operator|.
name|listFiles
argument_list|(
parameter_list|(
name|dir
parameter_list|,
name|name
parameter_list|)
lambda|->
name|name
operator|.
name|startsWith
argument_list|(
name|HIVE_KRYO_REG_JAR_NAME
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|jars
operator|!=
literal|null
operator|&&
name|jars
operator|.
name|length
operator|>
literal|0
condition|)
block|{
return|return
name|jars
index|[
literal|0
index|]
operator|.
name|getAbsolutePath
argument_list|()
return|;
block|}
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
literal|"Cannot find the "
operator|+
name|HIVE_KRYO_REG_JAR_NAME
operator|+
literal|" jar under "
operator|+
name|path
operator|.
name|getParent
argument_list|()
argument_list|)
throw|;
block|}
specifier|public
specifier|static
name|void
name|addJarToContextLoader
parameter_list|(
name|File
name|jar
parameter_list|)
throws|throws
name|MalformedURLException
block|{
name|ClassLoader
name|loader
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getContextClassLoader
argument_list|()
decl_stmt|;
if|if
condition|(
name|loader
operator|instanceof
name|MutableURLClassLoader
condition|)
block|{
operator|(
operator|(
name|MutableURLClassLoader
operator|)
name|loader
operator|)
operator|.
name|addURL
argument_list|(
name|jar
operator|.
name|toURI
argument_list|()
operator|.
name|toURL
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|URLClassLoader
name|newLoader
init|=
operator|new
name|URLClassLoader
argument_list|(
operator|new
name|URL
index|[]
block|{
name|jar
operator|.
name|toURI
argument_list|()
operator|.
name|toURL
argument_list|()
block|}
argument_list|,
name|loader
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|setContextClassLoader
argument_list|(
name|newLoader
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|boolean
name|containsErrorKeyword
parameter_list|(
name|String
name|line
parameter_list|)
block|{
return|return
name|ERROR_KEYWORDS
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|x
lambda|->
name|StringUtils
operator|.
name|containsIgnoreCase
argument_list|(
name|line
argument_list|,
name|x
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

