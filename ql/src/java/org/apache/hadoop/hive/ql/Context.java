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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
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
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|SimpleDateFormat
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
name|Date
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
name|Iterator
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
name|Random
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
name|antlr
operator|.
name|runtime
operator|.
name|TokenRewriteStream
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|ContentSummary
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
name|FileStatus
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|common
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
name|ql
operator|.
name|lockmgr
operator|.
name|HiveLock
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
name|lockmgr
operator|.
name|HiveLockManager
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * Context for Semantic Analyzers. Usage: not reusable - construct a new one for  * each query should call clear() at end of use to remove temporary folders  */
end_comment

begin_class
specifier|public
class|class
name|Context
block|{
specifier|private
name|Path
name|resFile
decl_stmt|;
specifier|private
name|Path
name|resDir
decl_stmt|;
specifier|private
name|FileSystem
name|resFs
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
literal|"hive.ql.Context"
argument_list|)
decl_stmt|;
specifier|private
name|Path
index|[]
name|resDirPaths
decl_stmt|;
specifier|private
name|int
name|resDirFilesNum
decl_stmt|;
name|boolean
name|initialized
decl_stmt|;
name|String
name|originalTracker
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|ContentSummary
argument_list|>
name|pathToCS
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|String
argument_list|,
name|ContentSummary
argument_list|>
argument_list|()
decl_stmt|;
comment|// scratch path to use for all non-local (ie. hdfs) file system tmp folders
specifier|private
specifier|final
name|Path
name|nonLocalScratchPath
decl_stmt|;
comment|// scratch directory to use for local file system tmp folders
specifier|private
specifier|final
name|String
name|localScratchDir
decl_stmt|;
comment|// Keeps track of scratch directories created for different scheme/authority
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|fsScratchDirs
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
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|protected
name|int
name|pathid
init|=
literal|10000
decl_stmt|;
specifier|protected
name|boolean
name|explain
init|=
literal|false
decl_stmt|;
specifier|protected
name|String
name|cmd
init|=
literal|""
decl_stmt|;
comment|// number of previous attempts
specifier|protected
name|int
name|tryCount
init|=
literal|0
decl_stmt|;
specifier|private
name|TokenRewriteStream
name|tokenRewriteStream
decl_stmt|;
name|String
name|executionId
decl_stmt|;
comment|// List of Locks for this query
specifier|protected
name|List
argument_list|<
name|HiveLock
argument_list|>
name|hiveLocks
decl_stmt|;
specifier|protected
name|HiveLockManager
name|hiveLockMgr
decl_stmt|;
specifier|private
name|boolean
name|needLockMgr
decl_stmt|;
specifier|public
name|Context
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|generateExecutionId
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a Context with a given executionId.  ExecutionId, together with    * user name and conf, will determine the temporary directory locations.    */
specifier|public
name|Context
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|executionId
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|executionId
operator|=
name|executionId
expr_stmt|;
comment|// non-local tmp location is configurable. however it is the same across
comment|// all external file systems
name|nonLocalScratchPath
operator|=
operator|new
name|Path
argument_list|(
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SCRATCHDIR
argument_list|)
argument_list|,
name|executionId
argument_list|)
expr_stmt|;
comment|// local tmp location is not configurable for now
name|localScratchDir
operator|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|executionId
expr_stmt|;
block|}
comment|/**    * Set the context on whether the current query is an explain query.    * @param value true if the query is an explain query, false if not    */
specifier|public
name|void
name|setExplain
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|explain
operator|=
name|value
expr_stmt|;
block|}
comment|/**    * Find whether the current query is an explain query    * @return true if the query is an explain query, false if not    */
specifier|public
name|boolean
name|getExplain
parameter_list|()
block|{
return|return
name|explain
return|;
block|}
comment|/**    * Set the original query command.    * @param cmd the original query command string    */
specifier|public
name|void
name|setCmd
parameter_list|(
name|String
name|cmd
parameter_list|)
block|{
name|this
operator|.
name|cmd
operator|=
name|cmd
expr_stmt|;
block|}
comment|/**    * Find the original query command.    * @return the original query command string    */
specifier|public
name|String
name|getCmd
parameter_list|()
block|{
return|return
name|cmd
return|;
block|}
comment|/**    * Get a tmp directory on specified URI    *    * @param scheme Scheme of the target FS    * @param authority Authority of the target FS    * @param mkdir create the directory if true    * @param scratchdir path of tmp directory    */
specifier|private
name|String
name|getScratchDir
parameter_list|(
name|String
name|scheme
parameter_list|,
name|String
name|authority
parameter_list|,
name|boolean
name|mkdir
parameter_list|,
name|String
name|scratchDir
parameter_list|)
block|{
name|String
name|fileSystem
init|=
name|scheme
operator|+
literal|":"
operator|+
name|authority
decl_stmt|;
name|String
name|dir
init|=
name|fsScratchDirs
operator|.
name|get
argument_list|(
name|fileSystem
argument_list|)
decl_stmt|;
if|if
condition|(
name|dir
operator|==
literal|null
condition|)
block|{
name|Path
name|dirPath
init|=
operator|new
name|Path
argument_list|(
name|scheme
argument_list|,
name|authority
argument_list|,
name|scratchDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|mkdir
condition|)
block|{
try|try
block|{
name|FileSystem
name|fs
init|=
name|dirPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|dirPath
operator|=
operator|new
name|Path
argument_list|(
name|fs
operator|.
name|makeQualified
argument_list|(
name|dirPath
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|dirPath
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Cannot make directory: "
operator|+
name|dirPath
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
name|dir
operator|=
name|dirPath
operator|.
name|toString
argument_list|()
expr_stmt|;
name|fsScratchDirs
operator|.
name|put
argument_list|(
name|fileSystem
argument_list|,
name|dir
argument_list|)
expr_stmt|;
block|}
return|return
name|dir
return|;
block|}
comment|/**    * Create a local scratch directory on demand and return it.    */
specifier|public
name|String
name|getLocalScratchDir
parameter_list|(
name|boolean
name|mkdir
parameter_list|)
block|{
try|try
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|URI
name|uri
init|=
name|fs
operator|.
name|getUri
argument_list|()
decl_stmt|;
return|return
name|getScratchDir
argument_list|(
name|uri
operator|.
name|getScheme
argument_list|()
argument_list|,
name|uri
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|mkdir
argument_list|,
name|localScratchDir
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Create a map-reduce scratch directory on demand and return it.    *    */
specifier|public
name|String
name|getMRScratchDir
parameter_list|()
block|{
comment|// if we are executing entirely on the client side - then
comment|// just (re)use the local scratch directory
if|if
condition|(
name|isLocalOnlyExecutionMode
argument_list|()
condition|)
block|{
return|return
name|getLocalScratchDir
argument_list|(
operator|!
name|explain
argument_list|)
return|;
block|}
try|try
block|{
name|Path
name|dir
init|=
name|FileUtils
operator|.
name|makeQualified
argument_list|(
name|nonLocalScratchPath
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|URI
name|uri
init|=
name|dir
operator|.
name|toUri
argument_list|()
decl_stmt|;
return|return
name|getScratchDir
argument_list|(
name|uri
operator|.
name|getScheme
argument_list|()
argument_list|,
name|uri
operator|.
name|getAuthority
argument_list|()
argument_list|,
operator|!
name|explain
argument_list|,
name|uri
operator|.
name|getPath
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Error while making MR scratch "
operator|+
literal|"directory - check filesystem config ("
operator|+
name|e
operator|.
name|getCause
argument_list|()
operator|+
literal|")"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|String
name|getExternalScratchDir
parameter_list|(
name|URI
name|extURI
parameter_list|)
block|{
return|return
name|getScratchDir
argument_list|(
name|extURI
operator|.
name|getScheme
argument_list|()
argument_list|,
name|extURI
operator|.
name|getAuthority
argument_list|()
argument_list|,
operator|!
name|explain
argument_list|,
name|nonLocalScratchPath
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Remove any created scratch directories.    */
specifier|private
name|void
name|removeScratchDir
parameter_list|()
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|fsScratchDirs
operator|.
name|entrySet
argument_list|()
control|)
block|{
try|try
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|p
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|delete
argument_list|(
name|p
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error Removing Scratch: "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|fsScratchDirs
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|private
name|String
name|nextPathId
parameter_list|()
block|{
return|return
name|Integer
operator|.
name|toString
argument_list|(
name|pathid
operator|++
argument_list|)
return|;
block|}
specifier|private
specifier|static
specifier|final
name|String
name|MR_PREFIX
init|=
literal|"-mr-"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|EXT_PREFIX
init|=
literal|"-ext-"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LOCAL_PREFIX
init|=
literal|"-local-"
decl_stmt|;
comment|/**    * Check if path is for intermediate data    * @return true if a uri is a temporary uri for map-reduce intermediate data,    *         false otherwise    */
specifier|public
name|boolean
name|isMRTmpFileURI
parameter_list|(
name|String
name|uriStr
parameter_list|)
block|{
return|return
operator|(
name|uriStr
operator|.
name|indexOf
argument_list|(
name|executionId
argument_list|)
operator|!=
operator|-
literal|1
operator|)
operator|&&
operator|(
name|uriStr
operator|.
name|indexOf
argument_list|(
name|MR_PREFIX
argument_list|)
operator|!=
operator|-
literal|1
operator|)
return|;
block|}
comment|/**    * Get a path to store map-reduce intermediate data in.    *    * @return next available path for map-red intermediate data    */
specifier|public
name|String
name|getMRTmpFileURI
parameter_list|()
block|{
return|return
name|getMRScratchDir
argument_list|()
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|MR_PREFIX
operator|+
name|nextPathId
argument_list|()
return|;
block|}
comment|/**    * Given a URI for mapreduce intermediate output, swizzle the    * it to point to the local file system. This can be called in    * case the caller decides to run in local mode (in which case    * all intermediate data can be stored locally)    *    * @param originalURI uri to localize    * @return localized path for map-red intermediate data    */
specifier|public
name|String
name|localizeMRTmpFileURI
parameter_list|(
name|String
name|originalURI
parameter_list|)
block|{
name|Path
name|o
init|=
operator|new
name|Path
argument_list|(
name|originalURI
argument_list|)
decl_stmt|;
name|Path
name|mrbase
init|=
operator|new
name|Path
argument_list|(
name|getMRScratchDir
argument_list|()
argument_list|)
decl_stmt|;
name|URI
name|relURI
init|=
name|mrbase
operator|.
name|toUri
argument_list|()
operator|.
name|relativize
argument_list|(
name|o
operator|.
name|toUri
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|relURI
operator|.
name|equals
argument_list|(
name|o
operator|.
name|toUri
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid URI: "
operator|+
name|originalURI
operator|+
literal|", cannot relativize against"
operator|+
name|mrbase
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|getLocalScratchDir
argument_list|(
operator|!
name|explain
argument_list|)
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|relURI
operator|.
name|getPath
argument_list|()
return|;
block|}
comment|/**    * Get a tmp path on local host to store intermediate data.    *    * @return next available tmp path on local fs    */
specifier|public
name|String
name|getLocalTmpFileURI
parameter_list|()
block|{
return|return
name|getLocalScratchDir
argument_list|(
literal|true
argument_list|)
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|LOCAL_PREFIX
operator|+
name|nextPathId
argument_list|()
return|;
block|}
comment|/**    * Get a path to store tmp data destined for external URI.    *    * @param extURI    *          external URI to which the tmp data has to be eventually moved    * @return next available tmp path on the file system corresponding extURI    */
specifier|public
name|String
name|getExternalTmpFileURI
parameter_list|(
name|URI
name|extURI
parameter_list|)
block|{
return|return
name|getExternalScratchDir
argument_list|(
name|extURI
argument_list|)
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|EXT_PREFIX
operator|+
name|nextPathId
argument_list|()
return|;
block|}
comment|/**    * @return the resFile    */
specifier|public
name|Path
name|getResFile
parameter_list|()
block|{
return|return
name|resFile
return|;
block|}
comment|/**    * @param resFile    *          the resFile to set    */
specifier|public
name|void
name|setResFile
parameter_list|(
name|Path
name|resFile
parameter_list|)
block|{
name|this
operator|.
name|resFile
operator|=
name|resFile
expr_stmt|;
name|resDir
operator|=
literal|null
expr_stmt|;
name|resDirPaths
operator|=
literal|null
expr_stmt|;
name|resDirFilesNum
operator|=
literal|0
expr_stmt|;
block|}
comment|/**    * @return the resDir    */
specifier|public
name|Path
name|getResDir
parameter_list|()
block|{
return|return
name|resDir
return|;
block|}
comment|/**    * @param resDir    *          the resDir to set    */
specifier|public
name|void
name|setResDir
parameter_list|(
name|Path
name|resDir
parameter_list|)
block|{
name|this
operator|.
name|resDir
operator|=
name|resDir
expr_stmt|;
name|resFile
operator|=
literal|null
expr_stmt|;
name|resDirFilesNum
operator|=
literal|0
expr_stmt|;
name|resDirPaths
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|resDir
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|FileSystem
name|fs
init|=
name|resDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|resDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Context clear error: "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|resFile
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|FileSystem
name|fs
init|=
name|resFile
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|resFile
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Context clear error: "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|removeScratchDir
argument_list|()
expr_stmt|;
name|originalTracker
operator|=
literal|null
expr_stmt|;
name|setNeedLockMgr
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|DataInput
name|getStream
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
operator|!
name|initialized
condition|)
block|{
name|initialized
operator|=
literal|true
expr_stmt|;
if|if
condition|(
operator|(
name|resFile
operator|==
literal|null
operator|)
operator|&&
operator|(
name|resDir
operator|==
literal|null
operator|)
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|resFile
operator|!=
literal|null
condition|)
block|{
return|return
name|resFile
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|open
argument_list|(
name|resFile
argument_list|)
return|;
block|}
name|resFs
operator|=
name|resDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|FileStatus
name|status
init|=
name|resFs
operator|.
name|getFileStatus
argument_list|(
name|resDir
argument_list|)
decl_stmt|;
assert|assert
name|status
operator|.
name|isDir
argument_list|()
assert|;
name|FileStatus
index|[]
name|resDirFS
init|=
name|resFs
operator|.
name|globStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|resDir
operator|+
literal|"/*"
argument_list|)
argument_list|)
decl_stmt|;
name|resDirPaths
operator|=
operator|new
name|Path
index|[
name|resDirFS
operator|.
name|length
index|]
expr_stmt|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
for|for
control|(
name|FileStatus
name|resFS
range|:
name|resDirFS
control|)
block|{
if|if
condition|(
operator|!
name|resFS
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|resDirPaths
index|[
name|pos
operator|++
index|]
operator|=
name|resFS
operator|.
name|getPath
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|pos
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|resFs
operator|.
name|open
argument_list|(
name|resDirPaths
index|[
name|resDirFilesNum
operator|++
index|]
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|getNextStream
argument_list|()
return|;
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"getStream error: "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"getStream error: "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|DataInput
name|getNextStream
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|resDir
operator|!=
literal|null
operator|&&
name|resDirFilesNum
operator|<
name|resDirPaths
operator|.
name|length
operator|&&
operator|(
name|resDirPaths
index|[
name|resDirFilesNum
index|]
operator|!=
literal|null
operator|)
condition|)
block|{
return|return
name|resFs
operator|.
name|open
argument_list|(
name|resDirPaths
index|[
name|resDirFilesNum
operator|++
index|]
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"getNextStream error: "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"getNextStream error: "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Little abbreviation for StringUtils.    */
specifier|private
specifier|static
name|boolean
name|strEquals
parameter_list|(
name|String
name|str1
parameter_list|,
name|String
name|str2
parameter_list|)
block|{
return|return
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|StringUtils
operator|.
name|equals
argument_list|(
name|str1
argument_list|,
name|str2
argument_list|)
return|;
block|}
comment|/**    * Set the token rewrite stream being used to parse the current top-level SQL    * statement. Note that this should<b>not</b> be used for other parsing    * activities; for example, when we encounter a reference to a view, we switch    * to a new stream for parsing the stored view definition from the catalog,    * but we don't clobber the top-level stream in the context.    *    * @param tokenRewriteStream    *          the stream being used    */
specifier|public
name|void
name|setTokenRewriteStream
parameter_list|(
name|TokenRewriteStream
name|tokenRewriteStream
parameter_list|)
block|{
assert|assert
operator|(
name|this
operator|.
name|tokenRewriteStream
operator|==
literal|null
operator|)
assert|;
name|this
operator|.
name|tokenRewriteStream
operator|=
name|tokenRewriteStream
expr_stmt|;
block|}
comment|/**    * @return the token rewrite stream being used to parse the current top-level    *         SQL statement, or null if it isn't available (e.g. for parser    *         tests)    */
specifier|public
name|TokenRewriteStream
name|getTokenRewriteStream
parameter_list|()
block|{
return|return
name|tokenRewriteStream
return|;
block|}
comment|/**    * Generate a unique executionId.  An executionId, together with user name and    * the configuration, will determine the temporary locations of all intermediate    * files.    *    * In the future, users can use the executionId to resume a query.    */
specifier|public
specifier|static
name|String
name|generateExecutionId
parameter_list|()
block|{
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|SimpleDateFormat
name|format
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd_HH-mm-ss_SSS"
argument_list|)
decl_stmt|;
name|String
name|executionId
init|=
literal|"hive_"
operator|+
name|format
operator|.
name|format
argument_list|(
operator|new
name|Date
argument_list|()
argument_list|)
operator|+
literal|"_"
operator|+
name|Math
operator|.
name|abs
argument_list|(
name|rand
operator|.
name|nextLong
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|executionId
return|;
block|}
comment|/**    * Does Hive wants to run tasks entirely on the local machine    * (where the query is being compiled)?    *    * Today this translates into running hadoop jobs locally    */
specifier|public
name|boolean
name|isLocalOnlyExecutionMode
parameter_list|()
block|{
return|return
name|HiveConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPJT
argument_list|)
operator|.
name|equals
argument_list|(
literal|"local"
argument_list|)
return|;
block|}
specifier|public
name|List
argument_list|<
name|HiveLock
argument_list|>
name|getHiveLocks
parameter_list|()
block|{
return|return
name|hiveLocks
return|;
block|}
specifier|public
name|void
name|setHiveLocks
parameter_list|(
name|List
argument_list|<
name|HiveLock
argument_list|>
name|hiveLocks
parameter_list|)
block|{
name|this
operator|.
name|hiveLocks
operator|=
name|hiveLocks
expr_stmt|;
block|}
specifier|public
name|HiveLockManager
name|getHiveLockMgr
parameter_list|()
block|{
return|return
name|hiveLockMgr
return|;
block|}
specifier|public
name|void
name|setHiveLockMgr
parameter_list|(
name|HiveLockManager
name|hiveLockMgr
parameter_list|)
block|{
name|this
operator|.
name|hiveLockMgr
operator|=
name|hiveLockMgr
expr_stmt|;
block|}
specifier|public
name|void
name|setOriginalTracker
parameter_list|(
name|String
name|originalTracker
parameter_list|)
block|{
name|this
operator|.
name|originalTracker
operator|=
name|originalTracker
expr_stmt|;
block|}
specifier|public
name|void
name|restoreOriginalTracker
parameter_list|()
block|{
if|if
condition|(
name|originalTracker
operator|!=
literal|null
condition|)
block|{
name|HiveConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPJT
argument_list|,
name|originalTracker
argument_list|)
expr_stmt|;
name|originalTracker
operator|=
literal|null
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|addCS
parameter_list|(
name|String
name|path
parameter_list|,
name|ContentSummary
name|cs
parameter_list|)
block|{
name|pathToCS
operator|.
name|put
argument_list|(
name|path
argument_list|,
name|cs
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ContentSummary
name|getCS
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|pathToCS
operator|.
name|get
argument_list|(
name|path
argument_list|)
return|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|ContentSummary
argument_list|>
name|getPathToCS
parameter_list|()
block|{
return|return
name|pathToCS
return|;
block|}
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
comment|/**    * Given a mapping from paths to objects, localize any MR tmp paths    * @param map mapping from paths to objects    */
specifier|public
name|void
name|localizeKeys
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|path
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|isMRTmpFileURI
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|Object
name|val
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|map
operator|.
name|remove
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|localizeMRTmpFileURI
argument_list|(
name|path
argument_list|)
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Given a list of paths, localize any MR tmp paths contained therein    * @param paths list of paths to be localized    */
specifier|public
name|void
name|localizePaths
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|paths
parameter_list|)
block|{
name|Iterator
argument_list|<
name|String
argument_list|>
name|iter
init|=
name|paths
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|toAdd
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|String
name|path
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|isMRTmpFileURI
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|iter
operator|.
name|remove
argument_list|()
expr_stmt|;
name|toAdd
operator|.
name|add
argument_list|(
name|localizeMRTmpFileURI
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|paths
operator|.
name|addAll
argument_list|(
name|toAdd
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isNeedLockMgr
parameter_list|()
block|{
return|return
name|needLockMgr
return|;
block|}
specifier|public
name|void
name|setNeedLockMgr
parameter_list|(
name|boolean
name|needLockMgr
parameter_list|)
block|{
name|this
operator|.
name|needLockMgr
operator|=
name|needLockMgr
expr_stmt|;
block|}
specifier|public
name|int
name|getTryCount
parameter_list|()
block|{
return|return
name|tryCount
return|;
block|}
specifier|public
name|void
name|setTryCount
parameter_list|(
name|int
name|tryCount
parameter_list|)
block|{
name|this
operator|.
name|tryCount
operator|=
name|tryCount
expr_stmt|;
block|}
block|}
end_class

end_unit

