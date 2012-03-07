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
name|HiveException
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
name|plan
operator|.
name|CopyWork
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
name|LoadTableDesc
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
name|MoveWork
import|;
end_import

begin_comment
comment|/**  * LoadSemanticAnalyzer.  *  */
end_comment

begin_class
specifier|public
class|class
name|LoadSemanticAnalyzer
extends|extends
name|BaseSemanticAnalyzer
block|{
specifier|private
name|boolean
name|isLocal
decl_stmt|;
specifier|private
name|boolean
name|isOverWrite
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
throws|,
name|URISyntaxException
block|{
name|URI
name|fromURI
init|=
operator|new
name|Path
argument_list|(
name|fromPath
argument_list|)
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
name|String
name|fromAuthority
init|=
name|fromURI
operator|.
name|getAuthority
argument_list|()
decl_stmt|;
name|String
name|path
init|=
name|fromURI
operator|.
name|getPath
argument_list|()
decl_stmt|;
comment|// generate absolute path relative to current directory or hdfs home
comment|// directory
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
name|isLocal
condition|)
block|{
name|path
operator|=
operator|new
name|Path
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.dir"
argument_list|)
argument_list|,
name|path
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|path
operator|=
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
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
block|}
comment|// set correct scheme and authority
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
comment|// file for local
name|fromScheme
operator|=
literal|"file"
expr_stmt|;
block|}
else|else
block|{
comment|// use default values from fs.default.name
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
name|fromScheme
operator|=
name|defaultURI
operator|.
name|getScheme
argument_list|()
expr_stmt|;
name|fromAuthority
operator|=
name|defaultURI
operator|.
name|getAuthority
argument_list|()
expr_stmt|;
block|}
block|}
comment|// if scheme is specified but not authority then use the default authority
if|if
condition|(
operator|(
operator|!
name|fromScheme
operator|.
name|equals
argument_list|(
literal|"file"
argument_list|)
operator|)
operator|&&
name|StringUtils
operator|.
name|isEmpty
argument_list|(
name|fromAuthority
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
name|fromAuthority
operator|=
name|defaultURI
operator|.
name|getAuthority
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
name|fromScheme
operator|+
literal|"@"
operator|+
name|fromAuthority
operator|+
literal|"@"
operator|+
name|path
argument_list|)
expr_stmt|;
return|return
operator|new
name|URI
argument_list|(
name|fromScheme
argument_list|,
name|fromAuthority
argument_list|,
name|path
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
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
argument_list|,
literal|"Source file system should be \"file\" if \"local\" is specified"
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
name|FileSystem
operator|.
name|get
argument_list|(
name|fromURI
argument_list|,
name|conf
argument_list|)
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
literal|"No files matching path "
operator|+
name|fromURI
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
comment|// Has to use full name to make sure it does not conflict with
comment|// org.apache.commons.lang.StringUtils
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
argument_list|,
name|e
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
name|String
name|reason
init|=
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
literal|" is not valid. "
operator|+
literal|"Please check that values for params \"default.fs.name\" and "
operator|+
literal|"\"hive.metastore.warehouse.dir\" do not conflict."
decl_stmt|;
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
name|reason
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
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
name|isLocal
operator|=
literal|false
expr_stmt|;
name|isOverWrite
operator|=
literal|false
expr_stmt|;
name|Tree
name|fromTree
init|=
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Tree
name|tableTree
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
name|isLocal
operator|=
literal|true
expr_stmt|;
name|isOverWrite
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
name|fromTree
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
name|fromTree
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
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
argument_list|(
name|fromTree
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
argument_list|,
name|e
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
name|conf
argument_list|,
operator|(
name|ASTNode
operator|)
name|tableTree
argument_list|)
decl_stmt|;
if|if
condition|(
name|ts
operator|.
name|tableHandle
operator|.
name|isOffline
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|OFFLINE_TABLE_OR_PARTITION
operator|.
name|getMsg
argument_list|(
literal|":Table "
operator|+
name|ts
operator|.
name|tableName
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|ts
operator|.
name|tableHandle
operator|.
name|isView
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|DML_AGAINST_VIEW
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|ts
operator|.
name|tableHandle
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
name|LOAD_INTO_NON_NATIVE
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
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
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|parts
init|=
name|ts
operator|.
name|tableHandle
operator|.
name|getPartitionKeys
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|parts
operator|!=
literal|null
operator|&&
name|parts
operator|.
name|size
argument_list|()
operator|>
literal|0
operator|)
operator|&&
operator|(
name|ts
operator|.
name|partSpec
operator|==
literal|null
operator|||
name|ts
operator|.
name|partSpec
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|NEED_PARTITION_ERROR
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
comment|// make sure the arguments make sense
name|applyConstraints
argument_list|(
name|fromURI
argument_list|,
name|toURI
argument_list|,
name|fromTree
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
comment|// if the local keyword is specified - we will always make a copy. this
comment|// might seem redundant in the case
comment|// that the hive warehouse is also located in the local file system - but
comment|// that's just a test case.
name|String
name|copyURIStr
init|=
name|ctx
operator|.
name|getExternalTmpFileURI
argument_list|(
name|toURI
argument_list|)
decl_stmt|;
name|URI
name|copyURI
init|=
name|URI
operator|.
name|create
argument_list|(
name|copyURIStr
argument_list|)
decl_stmt|;
name|rTask
operator|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|CopyWork
argument_list|(
name|fromURI
operator|.
name|toString
argument_list|()
argument_list|,
name|copyURIStr
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|fromURI
operator|=
name|copyURI
expr_stmt|;
block|}
comment|// create final load/move work
name|String
name|loadTmpPath
init|=
name|ctx
operator|.
name|getExternalTmpFileURI
argument_list|(
name|toURI
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
init|=
name|ts
operator|.
name|getPartSpec
argument_list|()
decl_stmt|;
if|if
condition|(
name|partSpec
operator|==
literal|null
condition|)
block|{
name|partSpec
operator|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|outputs
operator|.
name|add
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|ts
operator|.
name|tableHandle
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|Partition
name|part
init|=
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getPartition
argument_list|(
name|ts
operator|.
name|tableHandle
argument_list|,
name|partSpec
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|part
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|part
operator|.
name|isOffline
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|OFFLINE_TABLE_OR_PARTITION
operator|.
name|getMsg
argument_list|(
name|ts
operator|.
name|tableName
operator|+
literal|":"
operator|+
name|part
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
name|outputs
operator|.
name|add
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|part
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outputs
operator|.
name|add
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|ts
operator|.
name|tableHandle
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
name|LoadTableDesc
name|loadTableWork
init|=
operator|new
name|LoadTableDesc
argument_list|(
name|fromURI
operator|.
name|toString
argument_list|()
argument_list|,
name|loadTmpPath
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
name|partSpec
argument_list|,
name|isOverWrite
argument_list|)
decl_stmt|;
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
name|MoveWork
argument_list|(
name|getInputs
argument_list|()
argument_list|,
name|getOutputs
argument_list|()
argument_list|,
name|loadTableWork
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
argument_list|,
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
name|MoveWork
argument_list|(
name|getInputs
argument_list|()
argument_list|,
name|getOutputs
argument_list|()
argument_list|,
name|loadTableWork
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
argument_list|,
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
name|HIVEINDEXAUTOUPDATE
argument_list|)
condition|)
block|{
name|IndexUpdater
name|indexUpdater
init|=
operator|new
name|IndexUpdater
argument_list|(
name|loadTableWork
argument_list|,
name|getInputs
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|indexUpdateTasks
init|=
name|indexUpdater
operator|.
name|generateUpdateTasks
argument_list|()
decl_stmt|;
for|for
control|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|updateTask
range|:
name|indexUpdateTasks
control|)
block|{
comment|//LOAD DATA will either have a copy& move or just a move, we always want the update to be dependent on the move
if|if
condition|(
name|rTask
operator|.
name|getChildren
argument_list|()
operator|==
literal|null
operator|||
name|rTask
operator|.
name|getChildren
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|rTask
operator|.
name|addDependentTask
argument_list|(
name|updateTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
operator|(
operator|(
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
operator|)
name|rTask
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|addDependentTask
argument_list|(
name|updateTask
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|console
operator|.
name|printInfo
argument_list|(
literal|"WARNING: could not auto-update stale indexes, indexes are not out of sync"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

