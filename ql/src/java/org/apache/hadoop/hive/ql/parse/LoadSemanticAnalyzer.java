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
name|parse
package|;
end_package

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
name|org
operator|.
name|antlr
operator|.
name|runtime
operator|.
name|tree
operator|.
name|CommonTree
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
name|tree
operator|.
name|Tree
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
name|lang
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
name|TaskFactory
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
name|Utilities
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
name|plan
operator|.
name|copyWork
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
name|plan
operator|.
name|loadFileDesc
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
name|plan
operator|.
name|loadTableDesc
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
name|plan
operator|.
name|moveWork
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

begin_class
specifier|public
class|class
name|LoadSemanticAnalyzer
extends|extends
name|BaseSemanticAnalyzer
block|{
name|boolean
name|isLocal
decl_stmt|;
name|boolean
name|isOverWrite
decl_stmt|;
name|FileSystem
name|fs
decl_stmt|;
specifier|public
name|LoadSemanticAnalyzer
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|FileStatus
index|[]
name|matchFilesOrDir
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
index|[]
name|srcs
init|=
name|fs
operator|.
name|globStatus
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|srcs
operator|!=
literal|null
operator|)
operator|&&
name|srcs
operator|.
name|length
operator|==
literal|1
condition|)
block|{
if|if
condition|(
name|srcs
index|[
literal|0
index|]
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|srcs
operator|=
name|fs
operator|.
name|listStatus
argument_list|(
name|srcs
index|[
literal|0
index|]
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|(
name|srcs
operator|)
return|;
block|}
specifier|private
name|URI
name|initializeFromURI
parameter_list|(
name|String
name|fromPath
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO: support hdfs relative path names by defaulting to /user/<user.name>
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|fromPath
argument_list|)
decl_stmt|;
name|URI
name|fromURI
init|=
name|p
operator|.
name|toUri
argument_list|()
decl_stmt|;
name|String
name|fromScheme
init|=
name|fromURI
operator|.
name|getScheme
argument_list|()
decl_stmt|;
comment|// initialize scheme for 'local' mode
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|fromScheme
argument_list|)
condition|)
block|{
if|if
condition|(
name|isLocal
condition|)
block|{
if|if
condition|(
operator|!
name|fromPath
operator|.
name|startsWith
argument_list|(
literal|"/"
argument_list|)
condition|)
block|{
comment|// generate absolute path relative to current directory
name|p
operator|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
literal|"file://"
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.dir"
argument_list|)
argument_list|)
argument_list|,
name|fromPath
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|p
operator|=
operator|new
name|Path
argument_list|(
literal|"file://"
operator|+
name|fromPath
argument_list|)
expr_stmt|;
block|}
name|fromURI
operator|=
name|p
operator|.
name|toUri
argument_list|()
expr_stmt|;
name|fromScheme
operator|=
literal|"file"
expr_stmt|;
block|}
block|}
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|fromURI
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|String
name|fromAuthority
init|=
literal|null
decl_stmt|;
comment|// fall back to configuration based scheme if necessary
if|if
condition|(
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|fromScheme
argument_list|)
condition|)
block|{
name|fromScheme
operator|=
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|getScheme
argument_list|()
expr_stmt|;
name|fromAuthority
operator|=
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|getAuthority
argument_list|()
expr_stmt|;
block|}
comment|// if using hdfs - authority must be specified. fall back using configuration if none specified.
if|if
condition|(
name|fromScheme
operator|.
name|equals
argument_list|(
literal|"hdfs"
argument_list|)
condition|)
block|{
name|fromAuthority
operator|=
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|fromURI
operator|.
name|getAuthority
argument_list|()
argument_list|)
condition|?
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|getAuthority
argument_list|()
else|:
name|fromURI
operator|.
name|getAuthority
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|fromURI
operator|=
operator|new
name|URI
argument_list|(
name|fromScheme
argument_list|,
name|fromAuthority
argument_list|,
name|fromURI
operator|.
name|getPath
argument_list|()
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|fromURI
return|;
block|}
specifier|private
name|void
name|applyConstraints
parameter_list|(
name|URI
name|fromURI
parameter_list|,
name|URI
name|toURI
parameter_list|,
name|Tree
name|ast
parameter_list|,
name|boolean
name|isLocal
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
operator|!
name|fromURI
operator|.
name|getScheme
argument_list|()
operator|.
name|equals
argument_list|(
literal|"file"
argument_list|)
operator|&&
operator|!
name|fromURI
operator|.
name|getScheme
argument_list|()
operator|.
name|equals
argument_list|(
literal|"hdfs"
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
name|ast
argument_list|,
literal|"only \"file\" or \"hdfs\" file systems accepted"
argument_list|)
argument_list|)
throw|;
block|}
comment|// local mode implies that scheme should be "file"
comment|// we can change this going forward
if|if
condition|(
name|isLocal
operator|&&
operator|!
name|fromURI
operator|.
name|getScheme
argument_list|()
operator|.
name|equals
argument_list|(
literal|"file"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|ILLEGAL_PATH
operator|.
name|getMsg
argument_list|(
name|ast
argument_list|)
argument_list|)
throw|;
block|}
try|try
block|{
name|FileStatus
index|[]
name|srcs
init|=
name|matchFilesOrDir
argument_list|(
name|fs
argument_list|,
operator|new
name|Path
argument_list|(
name|fromURI
operator|.
name|getScheme
argument_list|()
argument_list|,
name|fromURI
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|fromURI
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|srcs
operator|==
literal|null
operator|||
name|srcs
operator|.
name|length
operator|==
literal|0
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
name|ast
argument_list|,
literal|"No files matching path"
argument_list|)
argument_list|)
throw|;
block|}
for|for
control|(
name|FileStatus
name|oneSrc
range|:
name|srcs
control|)
block|{
if|if
condition|(
name|oneSrc
operator|.
name|isDir
argument_list|()
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
name|ast
argument_list|,
literal|"source contains directory: "
operator|+
name|oneSrc
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// Has to use full name to make sure it does not conflict with org.apache.commons.lang.StringUtils
name|LOG
operator|.
name|error
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
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
name|ast
argument_list|)
argument_list|)
throw|;
block|}
comment|// only in 'local' mode do we copy stuff from one place to another.
comment|// reject different scheme/authority in other cases.
if|if
condition|(
operator|!
name|isLocal
operator|&&
operator|(
operator|!
name|StringUtils
operator|.
name|equals
argument_list|(
name|fromURI
operator|.
name|getScheme
argument_list|()
argument_list|,
name|toURI
operator|.
name|getScheme
argument_list|()
argument_list|)
operator|||
operator|!
name|StringUtils
operator|.
name|equals
argument_list|(
name|fromURI
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|toURI
operator|.
name|getAuthority
argument_list|()
argument_list|)
operator|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Move from: "
operator|+
name|fromURI
operator|.
name|toString
argument_list|()
operator|+
literal|" to: "
operator|+
name|toURI
operator|.
name|toString
argument_list|()
operator|+
literal|" is not valid"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|ILLEGAL_PATH
operator|.
name|getMsg
argument_list|(
name|ast
argument_list|,
literal|"Cannot load data across filesystems, use load data local"
argument_list|)
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|analyzeInternal
parameter_list|(
name|CommonTree
name|ast
parameter_list|,
name|Context
name|ctx
parameter_list|)
throws|throws
name|SemanticException
block|{
name|isLocal
operator|=
name|isOverWrite
operator|=
literal|false
expr_stmt|;
name|Tree
name|from_t
init|=
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Tree
name|table_t
init|=
name|ast
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|ast
operator|.
name|getChildCount
argument_list|()
operator|==
literal|4
condition|)
block|{
name|isOverWrite
operator|=
name|isLocal
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|ast
operator|.
name|getChildCount
argument_list|()
operator|==
literal|3
condition|)
block|{
if|if
condition|(
name|ast
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
operator|.
name|getText
argument_list|()
operator|.
name|toLowerCase
argument_list|()
operator|.
name|equals
argument_list|(
literal|"local"
argument_list|)
condition|)
block|{
name|isLocal
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|isOverWrite
operator|=
literal|true
expr_stmt|;
block|}
block|}
comment|// initialize load path
name|URI
name|fromURI
decl_stmt|;
try|try
block|{
name|String
name|fromPath
init|=
name|stripQuotes
argument_list|(
name|from_t
operator|.
name|getText
argument_list|()
argument_list|)
decl_stmt|;
name|fromURI
operator|=
name|initializeFromURI
argument_list|(
name|fromPath
argument_list|)
expr_stmt|;
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
name|INVALID_PATH
operator|.
name|getMsg
argument_list|(
name|from_t
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
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
argument_list|(
name|from_t
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
comment|// initialize destination table/partition
name|tableSpec
name|ts
init|=
operator|new
name|tableSpec
argument_list|(
name|db
argument_list|,
operator|(
name|CommonTree
operator|)
name|table_t
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|URI
name|toURI
init|=
operator|(
name|ts
operator|.
name|partHandle
operator|!=
literal|null
operator|)
condition|?
name|ts
operator|.
name|partHandle
operator|.
name|getDataLocation
argument_list|()
else|:
name|ts
operator|.
name|tableHandle
operator|.
name|getDataLocation
argument_list|()
decl_stmt|;
comment|// make sure the arguments make sense
name|applyConstraints
argument_list|(
name|fromURI
argument_list|,
name|toURI
argument_list|,
name|from_t
argument_list|,
name|isLocal
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|rTask
init|=
literal|null
decl_stmt|;
comment|// create copy work
if|if
condition|(
name|isLocal
condition|)
block|{
comment|// if the local keyword is specified - we will always make a copy. this might seem redundant in the case
comment|// that the hive warehouse is also located in the local file system - but that's just a test case.
name|URI
name|copyURI
decl_stmt|;
try|try
block|{
name|copyURI
operator|=
operator|new
name|URI
argument_list|(
name|toURI
operator|.
name|getScheme
argument_list|()
argument_list|,
name|toURI
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|conf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SCRATCHDIR
argument_list|)
operator|+
literal|"/"
operator|+
name|Utilities
operator|.
name|randGen
operator|.
name|nextInt
argument_list|()
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
comment|// Has to use full name to make sure it does not conflict with org.apache.commons.lang.StringUtils
name|LOG
operator|.
name|error
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"Invalid URI. Check value of variable: "
operator|+
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SCRATCHDIR
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Cannot initialize temporary destination URI"
argument_list|)
throw|;
block|}
name|rTask
operator|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|copyWork
argument_list|(
name|fromURI
operator|.
name|toString
argument_list|()
argument_list|,
name|copyURI
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
name|fromURI
operator|=
name|copyURI
expr_stmt|;
block|}
comment|// create final load/move work
name|List
argument_list|<
name|loadTableDesc
argument_list|>
name|loadTableWork
init|=
operator|new
name|ArrayList
argument_list|<
name|loadTableDesc
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|loadFileDesc
argument_list|>
name|loadFileWork
init|=
operator|new
name|ArrayList
argument_list|<
name|loadFileDesc
argument_list|>
argument_list|()
decl_stmt|;
name|loadTableWork
operator|.
name|add
argument_list|(
operator|new
name|loadTableDesc
argument_list|(
name|fromURI
operator|.
name|toString
argument_list|()
argument_list|,
name|Utilities
operator|.
name|getTableDesc
argument_list|(
name|ts
operator|.
name|tableHandle
argument_list|)
argument_list|,
operator|(
name|ts
operator|.
name|partSpec
operator|!=
literal|null
operator|)
condition|?
name|ts
operator|.
name|partSpec
else|:
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
argument_list|,
name|isOverWrite
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|rTask
operator|!=
literal|null
condition|)
block|{
name|rTask
operator|.
name|addDependentTask
argument_list|(
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|moveWork
argument_list|(
name|loadTableWork
argument_list|,
name|loadFileWork
argument_list|)
argument_list|,
name|this
operator|.
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rTask
operator|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|moveWork
argument_list|(
name|loadTableWork
argument_list|,
name|loadFileWork
argument_list|)
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
block|}
name|rootTasks
operator|.
name|add
argument_list|(
name|rTask
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

