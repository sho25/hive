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
name|hadoop
operator|.
name|hive
operator|.
name|conf
operator|.
name|HiveConf
operator|.
name|StrictChecks
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
name|httpclient
operator|.
name|util
operator|.
name|URIUtil
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
name|TableType
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
name|QueryState
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
name|io
operator|.
name|AcidUtils
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
name|io
operator|.
name|HiveFileFormatUtils
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
name|LockException
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
name|StatsWork
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
name|LoadTableDesc
operator|.
name|LoadFileType
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
name|BasicStatsWork
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
name|mapred
operator|.
name|InputFormat
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
specifier|public
name|LoadSemanticAnalyzer
parameter_list|(
name|QueryState
name|queryState
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|queryState
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
argument_list|,
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
name|String
name|name
init|=
name|p
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
name|name
operator|.
name|equals
argument_list|(
name|EximUtil
operator|.
name|METADATA_NAME
argument_list|)
condition|?
literal|true
else|:
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
operator|&&
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"."
argument_list|)
return|;
block|}
block|}
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
argument_list|,
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
name|String
name|name
init|=
name|p
operator|.
name|getName
argument_list|()
decl_stmt|;
return|return
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
operator|&&
operator|!
name|name
operator|.
name|startsWith
argument_list|(
literal|"."
argument_list|)
return|;
block|}
block|}
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
parameter_list|,
name|boolean
name|isLocal
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
name|URIUtil
operator|.
name|decode
argument_list|(
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
name|fromPath
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
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
name|List
argument_list|<
name|FileStatus
argument_list|>
name|applyConstraintsAndGetFiles
parameter_list|(
name|URI
name|fromURI
parameter_list|,
name|Tree
name|ast
parameter_list|,
name|boolean
name|isLocal
parameter_list|,
name|Table
name|table
parameter_list|)
throws|throws
name|SemanticException
block|{
name|FileStatus
index|[]
name|srcs
init|=
literal|null
decl_stmt|;
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
name|srcs
operator|=
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
argument_list|)
argument_list|)
expr_stmt|;
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
comment|// Do another loop if table is bucketed
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
init|=
name|table
operator|.
name|getBucketCols
argument_list|()
decl_stmt|;
if|if
condition|(
name|bucketCols
operator|!=
literal|null
operator|&&
operator|!
name|bucketCols
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// Hive assumes that user names the files as per the corresponding
comment|// bucket. For e.g, file names should follow the format 000000_0, 000000_1 etc.
comment|// Here the 1st file will belong to bucket 0 and 2nd to bucket 1 and so on.
name|boolean
index|[]
name|bucketArray
init|=
operator|new
name|boolean
index|[
name|table
operator|.
name|getNumBuckets
argument_list|()
index|]
decl_stmt|;
comment|// initialize the array
name|Arrays
operator|.
name|fill
argument_list|(
name|bucketArray
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|int
name|numBuckets
init|=
name|table
operator|.
name|getNumBuckets
argument_list|()
decl_stmt|;
for|for
control|(
name|FileStatus
name|oneSrc
range|:
name|srcs
control|)
block|{
name|String
name|bucketName
init|=
name|oneSrc
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|//get the bucket id
name|String
name|bucketIdStr
init|=
name|Utilities
operator|.
name|getBucketFileNameFromPathSubString
argument_list|(
name|bucketName
argument_list|)
decl_stmt|;
name|int
name|bucketId
init|=
name|Utilities
operator|.
name|getBucketIdFromFile
argument_list|(
name|bucketIdStr
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"bucket ID for file "
operator|+
name|oneSrc
operator|.
name|getPath
argument_list|()
operator|+
literal|" = "
operator|+
name|bucketId
operator|+
literal|" for table "
operator|+
name|table
operator|.
name|getFullyQualifiedName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|bucketId
operator|==
operator|-
literal|1
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
literal|"The file name is invalid : "
operator|+
name|oneSrc
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
literal|" for table "
operator|+
name|table
operator|.
name|getFullyQualifiedName
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|bucketId
operator|>=
name|numBuckets
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
literal|"The file name corresponds to invalid bucketId : "
operator|+
name|oneSrc
operator|.
name|getPath
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|+
literal|". Maximum number of buckets can be "
operator|+
name|numBuckets
operator|+
literal|" for table "
operator|+
name|table
operator|.
name|getFullyQualifiedName
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|bucketArray
index|[
name|bucketId
index|]
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
literal|"Multiple files for same bucket : "
operator|+
name|bucketId
operator|+
literal|". Only 1 file per bucket allowed in single load command. To load multiple files for same bucket, use multiple statements for table "
operator|+
name|table
operator|.
name|getFullyQualifiedName
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
name|bucketArray
index|[
name|bucketId
index|]
operator|=
literal|true
expr_stmt|;
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
return|return
name|Lists
operator|.
name|newArrayList
argument_list|(
name|srcs
argument_list|)
return|;
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
name|boolean
name|isLocal
init|=
literal|false
decl_stmt|;
name|boolean
name|isOverWrite
init|=
literal|false
decl_stmt|;
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
argument_list|,
name|isLocal
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
name|TableSpec
name|ts
init|=
operator|new
name|TableSpec
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
name|isView
argument_list|()
operator|||
name|ts
operator|.
name|tableHandle
operator|.
name|isMaterializedView
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
if|if
condition|(
name|ts
operator|.
name|tableHandle
operator|.
name|isStoredAsSubDirectories
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|LOAD_INTO_STORED_AS_DIR
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
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
name|List
argument_list|<
name|String
argument_list|>
name|bucketCols
init|=
name|ts
operator|.
name|tableHandle
operator|.
name|getBucketCols
argument_list|()
decl_stmt|;
if|if
condition|(
name|bucketCols
operator|!=
literal|null
operator|&&
operator|!
name|bucketCols
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|String
name|error
init|=
name|StrictChecks
operator|.
name|checkBucketing
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Please load into an intermediate table"
operator|+
literal|" and use 'insert... select' to allow Hive to enforce bucketing. "
operator|+
name|error
argument_list|)
throw|;
block|}
block|}
comment|// make sure the arguments make sense
name|List
argument_list|<
name|FileStatus
argument_list|>
name|files
init|=
name|applyConstraintsAndGetFiles
argument_list|(
name|fromURI
argument_list|,
name|fromTree
argument_list|,
name|isLocal
argument_list|,
name|ts
operator|.
name|tableHandle
argument_list|)
decl_stmt|;
comment|// for managed tables, make sure the file formats match
if|if
condition|(
name|TableType
operator|.
name|MANAGED_TABLE
operator|.
name|equals
argument_list|(
name|ts
operator|.
name|tableHandle
operator|.
name|getTableType
argument_list|()
argument_list|)
operator|&&
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVECHECKFILEFORMAT
argument_list|)
condition|)
block|{
name|ensureFileFormatsMatch
argument_list|(
name|ts
argument_list|,
name|files
argument_list|,
name|fromURI
argument_list|)
expr_stmt|;
block|}
name|inputs
operator|.
name|add
argument_list|(
name|toReadEntity
argument_list|(
operator|new
name|Path
argument_list|(
name|fromURI
argument_list|)
argument_list|)
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
comment|// create final load/move work
name|boolean
name|preservePartitionSpecs
init|=
literal|false
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
argument_list|,
operator|(
name|isOverWrite
condition|?
name|WriteEntity
operator|.
name|WriteType
operator|.
name|INSERT_OVERWRITE
else|:
name|WriteEntity
operator|.
name|WriteType
operator|.
name|INSERT
operator|)
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
name|isOverWrite
condition|)
block|{
name|outputs
operator|.
name|add
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|part
argument_list|,
name|WriteEntity
operator|.
name|WriteType
operator|.
name|INSERT_OVERWRITE
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
name|part
argument_list|,
name|WriteEntity
operator|.
name|WriteType
operator|.
name|INSERT
argument_list|)
argument_list|)
expr_stmt|;
comment|// If partition already exists and we aren't overwriting it, then respect
comment|// its current location info rather than picking it from the parent TableDesc
name|preservePartitionSpecs
operator|=
literal|true
expr_stmt|;
block|}
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
argument_list|,
operator|(
name|isOverWrite
condition|?
name|WriteEntity
operator|.
name|WriteType
operator|.
name|INSERT_OVERWRITE
else|:
name|WriteEntity
operator|.
name|WriteType
operator|.
name|INSERT
operator|)
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
name|Long
name|writeId
init|=
literal|null
decl_stmt|;
name|int
name|stmtId
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|AcidUtils
operator|.
name|isTransactionalTable
argument_list|(
name|ts
operator|.
name|tableHandle
argument_list|)
condition|)
block|{
try|try
block|{
name|writeId
operator|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getTxnMgr
argument_list|()
operator|.
name|getTableWriteId
argument_list|(
name|ts
operator|.
name|tableHandle
operator|.
name|getDbName
argument_list|()
argument_list|,
name|ts
operator|.
name|tableHandle
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LockException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Failed to allocate the write id"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
name|stmtId
operator|=
name|SessionState
operator|.
name|get
argument_list|()
operator|.
name|getTxnMgr
argument_list|()
operator|.
name|getStmtIdAndIncrement
argument_list|()
expr_stmt|;
block|}
comment|// Note: this sets LoadFileType incorrectly for ACID; is that relevant for load?
comment|//       See setLoadFileType and setIsAcidIow calls elsewhere for an example.
name|LoadTableDesc
name|loadTableWork
init|=
operator|new
name|LoadTableDesc
argument_list|(
operator|new
name|Path
argument_list|(
name|fromURI
argument_list|)
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
condition|?
name|LoadFileType
operator|.
name|REPLACE_ALL
else|:
name|LoadFileType
operator|.
name|KEEP_EXISTING
argument_list|,
name|writeId
argument_list|)
decl_stmt|;
name|loadTableWork
operator|.
name|setStmtId
argument_list|(
name|stmtId
argument_list|)
expr_stmt|;
if|if
condition|(
name|preservePartitionSpecs
condition|)
block|{
comment|// Note : preservePartitionSpecs=true implies inheritTableSpecs=false but
comment|// but preservePartitionSpecs=false(default) here is not sufficient enough
comment|// info to set inheritTableSpecs=true
name|loadTableWork
operator|.
name|setInheritTableSpecs
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|childTask
init|=
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
argument_list|,
name|isLocal
argument_list|)
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
name|childTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rTask
operator|=
name|childTask
expr_stmt|;
block|}
name|rootTasks
operator|.
name|add
argument_list|(
name|rTask
argument_list|)
expr_stmt|;
comment|// The user asked for stats to be collected.
comment|// Some stats like number of rows require a scan of the data
comment|// However, some other stats, like number of files, do not require a complete scan
comment|// Update the stats which do not require a complete scan.
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|statTask
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESTATSAUTOGATHER
argument_list|)
condition|)
block|{
name|BasicStatsWork
name|basicStatsWork
init|=
operator|new
name|BasicStatsWork
argument_list|(
name|loadTableWork
argument_list|)
decl_stmt|;
name|basicStatsWork
operator|.
name|setNoStatsAggregator
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|basicStatsWork
operator|.
name|setClearAggregatorStats
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|StatsWork
name|columnStatsWork
init|=
operator|new
name|StatsWork
argument_list|(
name|ts
operator|.
name|tableHandle
argument_list|,
name|basicStatsWork
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|statTask
operator|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|columnStatsWork
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|statTask
operator|!=
literal|null
condition|)
block|{
name|childTask
operator|.
name|addDependentTask
argument_list|(
name|statTask
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|ensureFileFormatsMatch
parameter_list|(
name|TableSpec
name|ts
parameter_list|,
name|List
argument_list|<
name|FileStatus
argument_list|>
name|fileStatuses
parameter_list|,
specifier|final
name|URI
name|fromURI
parameter_list|)
throws|throws
name|SemanticException
block|{
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|InputFormat
argument_list|>
name|destInputFormat
decl_stmt|;
try|try
block|{
if|if
condition|(
name|ts
operator|.
name|getPartSpec
argument_list|()
operator|==
literal|null
operator|||
name|ts
operator|.
name|getPartSpec
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|destInputFormat
operator|=
name|ts
operator|.
name|tableHandle
operator|.
name|getInputFormatClass
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|destInputFormat
operator|=
name|ts
operator|.
name|partHandle
operator|.
name|getInputFormatClass
argument_list|()
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
try|try
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|fromURI
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|boolean
name|validFormat
init|=
name|HiveFileFormatUtils
operator|.
name|checkInputFormat
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|destInputFormat
argument_list|,
name|fileStatuses
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|validFormat
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_FILE_FORMAT_IN_LOAD
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
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
name|SemanticException
argument_list|(
literal|"Unable to load data to destination table."
operator|+
literal|" Error: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

