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
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|parse
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
name|fs
operator|.
name|FSDataInputStream
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
name|fs
operator|.
name|PathFilter
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
name|metastore
operator|.
name|ReplChangeManager
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
name|api
operator|.
name|Database
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
name|api
operator|.
name|FieldSchema
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
name|Context
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
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|Task
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
name|exec
operator|.
name|repl
operator|.
name|util
operator|.
name|ReplUtils
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
name|hooks
operator|.
name|ReadEntity
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
name|hooks
operator|.
name|WriteEntity
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
name|metadata
operator|.
name|Hive
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
name|metadata
operator|.
name|Partition
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
name|metadata
operator|.
name|Table
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
name|parse
operator|.
name|repl
operator|.
name|DumpType
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
name|parse
operator|.
name|repl
operator|.
name|dump
operator|.
name|Utils
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
name|parse
operator|.
name|repl
operator|.
name|dump
operator|.
name|io
operator|.
name|DBSerializer
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
name|parse
operator|.
name|repl
operator|.
name|dump
operator|.
name|io
operator|.
name|JsonWriter
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
name|parse
operator|.
name|repl
operator|.
name|dump
operator|.
name|io
operator|.
name|ReplicationSpecSerializer
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
name|parse
operator|.
name|repl
operator|.
name|dump
operator|.
name|io
operator|.
name|TableSerializer
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|MetaData
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|MetadataJson
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|json
operator|.
name|JSONException
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
name|io
operator|.
name|Serializable
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
name|net
operator|.
name|URISyntaxException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|LinkedHashMap
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|StringTokenizer
import|;
end_import

begin_comment
comment|/**  *  * EximUtil. Utility methods for the export/import semantic  * analyzers.  *  */
end_comment

begin_class
specifier|public
class|class
name|EximUtil
block|{
specifier|public
specifier|static
specifier|final
name|String
name|METADATA_NAME
init|=
literal|"_metadata"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FILES_NAME
init|=
literal|"_files"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DATA_PATH_NAME
init|=
literal|"data"
decl_stmt|;
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
name|EximUtil
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Wrapper class for common BaseSemanticAnalyzer non-static members    * into static generic methods without having the fn signatures    * becoming overwhelming, with passing each of these into every function.    *    * Note, however, that since this is constructed with args passed in,    * parts of the context, such as the tasks or inputs, might have been    * overridden with temporary context values, rather than being exactly    * 1:1 equivalent to BaseSemanticAnalyzer.getRootTasks() or BSA.getInputs().    */
specifier|public
specifier|static
class|class
name|SemanticAnalyzerWrapperContext
block|{
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|Hive
name|db
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|tasks
decl_stmt|;
specifier|private
name|Logger
name|LOG
decl_stmt|;
specifier|private
name|Context
name|ctx
decl_stmt|;
specifier|private
name|DumpType
name|eventType
init|=
name|DumpType
operator|.
name|EVENT_UNKNOWN
decl_stmt|;
specifier|private
name|Task
argument_list|<
name|?
argument_list|>
name|openTxnTask
init|=
literal|null
decl_stmt|;
specifier|public
name|HiveConf
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|public
name|Hive
name|getHive
parameter_list|()
block|{
return|return
name|db
return|;
block|}
specifier|public
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|getInputs
parameter_list|()
block|{
return|return
name|inputs
return|;
block|}
specifier|public
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|getOutputs
parameter_list|()
block|{
return|return
name|outputs
return|;
block|}
specifier|public
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|getTasks
parameter_list|()
block|{
return|return
name|tasks
return|;
block|}
specifier|public
name|Logger
name|getLOG
parameter_list|()
block|{
return|return
name|LOG
return|;
block|}
specifier|public
name|Context
name|getCtx
parameter_list|()
block|{
return|return
name|ctx
return|;
block|}
specifier|public
name|void
name|setEventType
parameter_list|(
name|DumpType
name|eventType
parameter_list|)
block|{
name|this
operator|.
name|eventType
operator|=
name|eventType
expr_stmt|;
block|}
specifier|public
name|DumpType
name|getEventType
parameter_list|()
block|{
return|return
name|eventType
return|;
block|}
specifier|public
name|SemanticAnalyzerWrapperContext
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|Hive
name|db
parameter_list|,
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
parameter_list|,
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|tasks
parameter_list|,
name|Logger
name|LOG
parameter_list|,
name|Context
name|ctx
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
name|db
operator|=
name|db
expr_stmt|;
name|this
operator|.
name|inputs
operator|=
name|inputs
expr_stmt|;
name|this
operator|.
name|outputs
operator|=
name|outputs
expr_stmt|;
name|this
operator|.
name|tasks
operator|=
name|tasks
expr_stmt|;
name|this
operator|.
name|LOG
operator|=
name|LOG
expr_stmt|;
name|this
operator|.
name|ctx
operator|=
name|ctx
expr_stmt|;
block|}
specifier|public
name|Task
argument_list|<
name|?
argument_list|>
name|getOpenTxnTask
parameter_list|()
block|{
return|return
name|openTxnTask
return|;
block|}
specifier|public
name|void
name|setOpenTxnTask
parameter_list|(
name|Task
argument_list|<
name|?
argument_list|>
name|openTxnTask
parameter_list|)
block|{
name|this
operator|.
name|openTxnTask
operator|=
name|openTxnTask
expr_stmt|;
block|}
block|}
comment|/**    * Wrapper class for mapping replication source and target path for copying data.    */
specifier|public
specifier|static
class|class
name|ReplPathMapping
block|{
specifier|private
name|Path
name|srcPath
decl_stmt|;
specifier|private
name|Path
name|tgtPath
decl_stmt|;
specifier|public
name|ReplPathMapping
parameter_list|(
name|Path
name|srcPath
parameter_list|,
name|Path
name|tgtPath
parameter_list|)
block|{
if|if
condition|(
name|srcPath
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Source Path can not be null."
argument_list|)
throw|;
block|}
name|this
operator|.
name|srcPath
operator|=
name|srcPath
expr_stmt|;
if|if
condition|(
name|tgtPath
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Target Path can not be null."
argument_list|)
throw|;
block|}
name|this
operator|.
name|tgtPath
operator|=
name|tgtPath
expr_stmt|;
block|}
specifier|public
name|Path
name|getSrcPath
parameter_list|()
block|{
return|return
name|srcPath
return|;
block|}
specifier|public
name|void
name|setSrcPath
parameter_list|(
name|Path
name|srcPath
parameter_list|)
block|{
name|this
operator|.
name|srcPath
operator|=
name|srcPath
expr_stmt|;
block|}
specifier|public
name|Path
name|getTargetPath
parameter_list|()
block|{
return|return
name|tgtPath
return|;
block|}
specifier|public
name|void
name|setTargetPath
parameter_list|(
name|Path
name|targetPath
parameter_list|)
block|{
name|this
operator|.
name|tgtPath
operator|=
name|targetPath
expr_stmt|;
block|}
block|}
specifier|private
name|EximUtil
parameter_list|()
block|{   }
comment|/**    * Initialize the URI where the exported data collection is    * to created for export, or is present for import    */
specifier|public
specifier|static
name|URI
name|getValidatedURI
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|dcPath
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
name|boolean
name|testMode
init|=
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVETESTMODE
argument_list|)
operator|||
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEEXIMTESTMODE
argument_list|)
decl_stmt|;
name|URI
name|uri
init|=
operator|new
name|Path
argument_list|(
name|dcPath
argument_list|)
operator|.
name|toUri
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|uri
argument_list|,
name|conf
argument_list|)
decl_stmt|;
comment|// Get scheme from FileSystem
name|String
name|scheme
init|=
name|fs
operator|.
name|getScheme
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
name|String
name|path
init|=
name|uri
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Path before norm :"
operator|+
name|path
argument_list|)
expr_stmt|;
comment|// generate absolute path relative to home directory
if|if
condition|(
operator|!
name|path
operator|.
name|startsWith
argument_list|(
literal|"/"
argument_list|)
condition|)
block|{
if|if
condition|(
name|testMode
condition|)
block|{
name|path
operator|=
operator|(
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|)
argument_list|,
name|path
argument_list|)
operator|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|path
operator|=
operator|(
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/user/"
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|)
argument_list|,
name|path
argument_list|)
operator|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
expr_stmt|;
block|}
block|}
comment|// if scheme is specified but not authority then use the default authority
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|authority
argument_list|)
condition|)
block|{
name|URI
name|defaultURI
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
operator|.
name|getUri
argument_list|()
decl_stmt|;
name|authority
operator|=
name|defaultURI
operator|.
name|getAuthority
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Scheme:"
operator|+
name|scheme
operator|+
literal|", authority:"
operator|+
name|authority
operator|+
literal|", path:"
operator|+
name|path
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|eximSchemes
init|=
name|conf
operator|.
name|getStringCollection
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXIM_URI_SCHEME_WL
operator|.
name|varname
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|eximSchemes
operator|.
name|contains
argument_list|(
name|scheme
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_PATH
operator|.
name|getMsg
argument_list|(
literal|"only the following file systems accepted for export/import : "
operator|+
name|conf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_EXIM_URI_SCHEME_WL
operator|.
name|varname
argument_list|)
argument_list|)
argument_list|)
throw|;
block|}
try|try
block|{
return|return
operator|new
name|URI
argument_list|(
name|scheme
argument_list|,
name|authority
argument_list|,
name|path
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_PATH
operator|.
name|getMsg
argument_list|()
argument_list|,
name|e
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
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|IO_ERROR
operator|.
name|getMsg
argument_list|()
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|static
name|void
name|validateTable
parameter_list|(
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
name|metadata
operator|.
name|Table
name|table
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|table
operator|.
name|isNonNative
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|EXIM_FOR_NON_NATIVE
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|String
name|relativeToAbsolutePath
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
name|boolean
name|testMode
init|=
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVETESTMODE
argument_list|)
operator|||
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEEXIMTESTMODE
argument_list|)
decl_stmt|;
empty_stmt|;
if|if
condition|(
name|testMode
condition|)
block|{
name|URI
name|uri
init|=
operator|new
name|Path
argument_list|(
name|location
argument_list|)
operator|.
name|toUri
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|uri
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|String
name|scheme
init|=
name|fs
operator|.
name|getScheme
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
name|String
name|path
init|=
name|uri
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|path
operator|.
name|startsWith
argument_list|(
literal|"/"
argument_list|)
condition|)
block|{
name|path
operator|=
operator|(
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.tmp.dir"
argument_list|)
argument_list|,
name|path
argument_list|)
operator|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|scheme
argument_list|)
condition|)
block|{
name|scheme
operator|=
literal|"pfile"
expr_stmt|;
block|}
try|try
block|{
name|uri
operator|=
operator|new
name|URI
argument_list|(
name|scheme
argument_list|,
name|authority
argument_list|,
name|path
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_PATH
operator|.
name|getMsg
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|uri
operator|.
name|toString
argument_list|()
return|;
block|}
else|else
block|{
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|location
argument_list|)
decl_stmt|;
if|if
condition|(
name|path
operator|.
name|isAbsolute
argument_list|()
condition|)
block|{
return|return
name|location
return|;
block|}
return|return
name|path
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|makeQualified
argument_list|(
name|path
argument_list|)
operator|.
name|toString
argument_list|()
return|;
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
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|IO_ERROR
operator|.
name|getMsg
argument_list|()
operator|+
literal|": "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/* major version number should match for backward compatibility */
specifier|public
specifier|static
specifier|final
name|String
name|METADATA_FORMAT_VERSION
init|=
literal|"0.2"
decl_stmt|;
comment|/* If null, then the major version number should match */
specifier|public
specifier|static
specifier|final
name|String
name|METADATA_FORMAT_FORWARD_COMPATIBLE_VERSION
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
name|void
name|createDbExportDump
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|metadataPath
parameter_list|,
name|Database
name|dbObj
parameter_list|,
name|ReplicationSpec
name|replicationSpec
parameter_list|)
throws|throws
name|IOException
throws|,
name|SemanticException
block|{
comment|// WARNING NOTE : at this point, createDbExportDump lives only in a world where ReplicationSpec is in replication scope
comment|// If we later make this work for non-repl cases, analysis of this logic might become necessary. Also, this is using
comment|// Replv2 semantics, i.e. with listFiles laziness (no copy at export time)
comment|// Remove all the entries from the parameters which are added by repl tasks internally.
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
init|=
name|dbObj
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|parameters
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tmpParameters
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|parameters
argument_list|)
decl_stmt|;
name|tmpParameters
operator|.
name|entrySet
argument_list|()
operator|.
name|removeIf
argument_list|(
name|e
lambda|->
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|startsWith
argument_list|(
name|Utils
operator|.
name|BOOTSTRAP_DUMP_STATE_KEY_PREFIX
argument_list|)
operator|||
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
name|ReplUtils
operator|.
name|REPL_CHECKPOINT_KEY
argument_list|)
operator|||
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
name|ReplChangeManager
operator|.
name|SOURCE_OF_REPLICATION
argument_list|)
operator|||
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
name|ReplUtils
operator|.
name|REPL_FIRST_INC_PENDING_FLAG
argument_list|)
argument_list|)
expr_stmt|;
name|dbObj
operator|.
name|setParameters
argument_list|(
name|tmpParameters
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|JsonWriter
name|jsonWriter
init|=
operator|new
name|JsonWriter
argument_list|(
name|fs
argument_list|,
name|metadataPath
argument_list|)
init|)
block|{
operator|new
name|DBSerializer
argument_list|(
name|dbObj
argument_list|)
operator|.
name|writeTo
argument_list|(
name|jsonWriter
argument_list|,
name|replicationSpec
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|parameters
operator|!=
literal|null
condition|)
block|{
name|dbObj
operator|.
name|setParameters
argument_list|(
name|parameters
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|createExportDump
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|metadataPath
parameter_list|,
name|Table
name|tableHandle
parameter_list|,
name|Iterable
argument_list|<
name|Partition
argument_list|>
name|partitions
parameter_list|,
name|ReplicationSpec
name|replicationSpec
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|SemanticException
throws|,
name|IOException
block|{
if|if
condition|(
name|replicationSpec
operator|==
literal|null
condition|)
block|{
name|replicationSpec
operator|=
operator|new
name|ReplicationSpec
argument_list|()
expr_stmt|;
comment|// instantiate default values if not specified
block|}
if|if
condition|(
name|tableHandle
operator|==
literal|null
condition|)
block|{
name|replicationSpec
operator|.
name|setNoop
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|JsonWriter
name|writer
init|=
operator|new
name|JsonWriter
argument_list|(
name|fs
argument_list|,
name|metadataPath
argument_list|)
init|)
block|{
if|if
condition|(
name|replicationSpec
operator|.
name|isInReplicationScope
argument_list|()
condition|)
block|{
operator|new
name|ReplicationSpecSerializer
argument_list|()
operator|.
name|writeTo
argument_list|(
name|writer
argument_list|,
name|replicationSpec
argument_list|)
expr_stmt|;
block|}
operator|new
name|TableSerializer
argument_list|(
name|tableHandle
argument_list|,
name|partitions
argument_list|,
name|hiveConf
argument_list|)
operator|.
name|writeTo
argument_list|(
name|writer
argument_list|,
name|replicationSpec
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|MetaData
name|readMetaData
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|metadataPath
parameter_list|)
throws|throws
name|IOException
throws|,
name|SemanticException
block|{
name|String
name|message
init|=
name|readAsString
argument_list|(
name|fs
argument_list|,
name|metadataPath
argument_list|)
decl_stmt|;
try|try
block|{
return|return
operator|new
name|MetadataJson
argument_list|(
name|message
argument_list|)
operator|.
name|getMetaData
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|TException
decl||
name|JSONException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|ERROR_SERIALIZE_METADATA
operator|.
name|getMsg
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|String
name|readAsString
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|fromMetadataPath
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|FSDataInputStream
name|stream
init|=
name|fs
operator|.
name|open
argument_list|(
name|fromMetadataPath
argument_list|)
init|)
block|{
return|return
name|IOUtils
operator|.
name|toString
argument_list|(
name|stream
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
return|;
block|}
block|}
comment|/* check the forward and backward compatibility */
specifier|public
specifier|static
name|void
name|doCheckCompatibility
parameter_list|(
name|String
name|currVersion
parameter_list|,
name|String
name|version
parameter_list|,
name|String
name|fcVersion
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|version
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_METADATA
operator|.
name|getMsg
argument_list|(
literal|"Version number missing"
argument_list|)
argument_list|)
throw|;
block|}
name|StringTokenizer
name|st
init|=
operator|new
name|StringTokenizer
argument_list|(
name|version
argument_list|,
literal|"."
argument_list|)
decl_stmt|;
name|int
name|data_major
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|st
operator|.
name|nextToken
argument_list|()
argument_list|)
decl_stmt|;
name|StringTokenizer
name|st2
init|=
operator|new
name|StringTokenizer
argument_list|(
name|currVersion
argument_list|,
literal|"."
argument_list|)
decl_stmt|;
name|int
name|code_major
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|st2
operator|.
name|nextToken
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|code_minor
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|st2
operator|.
name|nextToken
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|code_major
operator|>
name|data_major
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_METADATA
operator|.
name|getMsg
argument_list|(
literal|"Not backward compatible."
operator|+
literal|" Producer version "
operator|+
name|version
operator|+
literal|", Consumer version "
operator|+
name|currVersion
argument_list|)
argument_list|)
throw|;
block|}
else|else
block|{
if|if
condition|(
operator|(
name|fcVersion
operator|==
literal|null
operator|)
operator|||
name|fcVersion
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|code_major
operator|<
name|data_major
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_METADATA
operator|.
name|getMsg
argument_list|(
literal|"Not forward compatible."
operator|+
literal|"Producer version "
operator|+
name|version
operator|+
literal|", Consumer version "
operator|+
name|currVersion
argument_list|)
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|StringTokenizer
name|st3
init|=
operator|new
name|StringTokenizer
argument_list|(
name|fcVersion
argument_list|,
literal|"."
argument_list|)
decl_stmt|;
name|int
name|fc_major
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|st3
operator|.
name|nextToken
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|fc_minor
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|st3
operator|.
name|nextToken
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|fc_major
operator|>
name|code_major
operator|)
operator|||
operator|(
operator|(
name|fc_major
operator|==
name|code_major
operator|)
operator|&&
operator|(
name|fc_minor
operator|>
name|code_minor
operator|)
operator|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_METADATA
operator|.
name|getMsg
argument_list|(
literal|"Not forward compatible."
operator|+
literal|"Minimum version "
operator|+
name|fcVersion
operator|+
literal|", Consumer version "
operator|+
name|currVersion
argument_list|)
argument_list|)
throw|;
block|}
block|}
block|}
block|}
comment|/**    * Return the partition specification from the specified keys and values    *    * @param partCols    *          the names of the partition keys    * @param partVals    *          the values of the partition keys    *    * @return the partition specification as a map    */
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|makePartSpec
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partVals
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|partCols
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|partSpec
operator|.
name|put
argument_list|(
name|partCols
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|,
name|partVals
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|partSpec
return|;
block|}
comment|/**    * Compares the schemas - names, types and order, but ignoring comments    *    * @param newSchema    *          the new schema    * @param oldSchema    *          the old schema    * @return a boolean indicating match    */
specifier|public
specifier|static
name|boolean
name|schemaCompare
parameter_list|(
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|newSchema
parameter_list|,
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|oldSchema
parameter_list|)
block|{
name|Iterator
argument_list|<
name|FieldSchema
argument_list|>
name|newColIter
init|=
name|newSchema
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|FieldSchema
name|oldCol
range|:
name|oldSchema
control|)
block|{
name|FieldSchema
name|newCol
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|newColIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|newCol
operator|=
name|newColIter
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
comment|// not using FieldSchema.equals as comments can be different
if|if
condition|(
operator|!
name|oldCol
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|newCol
operator|.
name|getName
argument_list|()
argument_list|)
operator|||
operator|!
name|oldCol
operator|.
name|getType
argument_list|()
operator|.
name|equals
argument_list|(
name|newCol
operator|.
name|getType
argument_list|()
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
if|if
condition|(
name|newColIter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
specifier|static
name|PathFilter
name|getDirectoryFilter
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|)
block|{
comment|// TODO : isn't there a prior impl of an isDirectory utility PathFilter so users don't have to write their own?
return|return
operator|new
name|PathFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
try|try
block|{
return|return
name|fs
operator|.
name|isDirectory
argument_list|(
name|p
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
block|}
return|;
block|}
block|}
end_class

end_unit

