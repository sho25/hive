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
name|io
operator|.
name|orc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|nio
operator|.
name|ByteBuffer
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
name|Map
operator|.
name|Entry
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
name|codec
operator|.
name|binary
operator|.
name|Hex
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
name|conf
operator|.
name|HiveConf
operator|.
name|ConfVars
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
name|MetadataPpdResult
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
name|SerializationUtilities
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
name|HdfsUtils
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
name|orc
operator|.
name|OrcInputFormat
operator|.
name|FooterCache
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
name|sarg
operator|.
name|ConvertAstToSearchArg
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
name|sarg
operator|.
name|PredicateLeaf
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
name|sarg
operator|.
name|SearchArgument
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
name|sarg
operator|.
name|SearchArgumentFactory
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
name|shims
operator|.
name|HadoopShims
operator|.
name|HdfsFileStatusWithId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|orc
operator|.
name|impl
operator|.
name|OrcTail
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
name|esotericsoftware
operator|.
name|kryo
operator|.
name|Kryo
import|;
end_import

begin_import
import|import
name|com
operator|.
name|esotericsoftware
operator|.
name|kryo
operator|.
name|io
operator|.
name|Output
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
comment|/** Metastore-based footer cache storing serialized footers. Also has a local cache. */
end_comment

begin_class
specifier|public
class|class
name|ExternalCache
implements|implements
name|FooterCache
block|{
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
name|ExternalCache
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|LocalCache
name|localCache
decl_stmt|;
specifier|private
specifier|final
name|ExternalFooterCachesByConf
name|externalCacheSrc
decl_stmt|;
specifier|private
name|boolean
name|isWarnLogged
init|=
literal|false
decl_stmt|;
comment|// Configuration and things set from it.
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
name|boolean
name|isInTest
decl_stmt|;
specifier|private
name|SearchArgument
name|sarg
decl_stmt|;
specifier|private
name|ByteBuffer
name|sargIsOriginal
decl_stmt|,
name|sargNotIsOriginal
decl_stmt|;
specifier|private
name|boolean
name|isPpdEnabled
decl_stmt|;
specifier|public
name|ExternalCache
parameter_list|(
name|LocalCache
name|lc
parameter_list|,
name|ExternalFooterCachesByConf
name|efcf
parameter_list|)
block|{
name|localCache
operator|=
name|lc
expr_stmt|;
name|externalCacheSrc
operator|=
name|efcf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|put
parameter_list|(
name|OrcInputFormat
operator|.
name|FooterCacheKey
name|key
parameter_list|,
name|OrcTail
name|orcTail
parameter_list|)
throws|throws
name|IOException
block|{
name|localCache
operator|.
name|put
argument_list|(
name|key
operator|.
name|getPath
argument_list|()
argument_list|,
name|orcTail
argument_list|)
expr_stmt|;
if|if
condition|(
name|key
operator|.
name|getFileId
argument_list|()
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|externalCacheSrc
operator|.
name|getCache
argument_list|(
name|conf
argument_list|)
operator|.
name|putFileMetadata
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|key
operator|.
name|getFileId
argument_list|()
argument_list|)
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|orcTail
operator|.
name|getSerializedTail
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isBlocking
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasPpd
parameter_list|()
block|{
return|return
name|isPpdEnabled
return|;
block|}
specifier|public
name|void
name|configure
parameter_list|(
name|HiveConf
name|queryConfig
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|queryConfig
expr_stmt|;
name|this
operator|.
name|sarg
operator|=
name|ConvertAstToSearchArg
operator|.
name|createFromConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|isPpdEnabled
operator|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVEOPTINDEXFILTER
argument_list|)
operator|&&
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_ORC_MS_FOOTER_CACHE_PPD
argument_list|)
expr_stmt|;
name|this
operator|.
name|isInTest
operator|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|conf
argument_list|,
name|ConfVars
operator|.
name|HIVE_IN_TEST
argument_list|)
expr_stmt|;
name|this
operator|.
name|sargIsOriginal
operator|=
name|this
operator|.
name|sargNotIsOriginal
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|getAndValidate
parameter_list|(
name|List
argument_list|<
name|HdfsFileStatusWithId
argument_list|>
name|files
parameter_list|,
name|boolean
name|isOriginal
parameter_list|,
name|OrcTail
index|[]
name|result
parameter_list|,
name|ByteBuffer
index|[]
name|ppdResult
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveException
block|{
assert|assert
name|result
operator|.
name|length
operator|==
name|files
operator|.
name|size
argument_list|()
assert|;
assert|assert
name|ppdResult
operator|==
literal|null
operator|||
name|ppdResult
operator|.
name|length
operator|==
name|files
operator|.
name|size
argument_list|()
assert|;
comment|// First, check the local cache.
name|localCache
operator|.
name|getAndValidate
argument_list|(
name|files
argument_list|,
name|isOriginal
argument_list|,
name|result
argument_list|,
name|ppdResult
argument_list|)
expr_stmt|;
comment|// posMap is an unfortunate consequence of batching/iterating thru MS results.
name|HashMap
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
name|posMap
init|=
operator|new
name|HashMap
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
comment|// We won't do metastore-side PPD for the things we have locally.
name|List
argument_list|<
name|Long
argument_list|>
name|fileIds
init|=
name|determineFileIdsToQuery
argument_list|(
name|files
argument_list|,
name|result
argument_list|,
name|posMap
argument_list|)
decl_stmt|;
comment|// Need to get a new one, see the comment wrt threadlocals.
name|ExternalFooterCachesByConf
operator|.
name|Cache
name|cache
init|=
name|externalCacheSrc
operator|.
name|getCache
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ByteBuffer
name|serializedSarg
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|isPpdEnabled
condition|)
block|{
name|serializedSarg
operator|=
name|getSerializedSargForMetastore
argument_list|(
name|isOriginal
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|serializedSarg
operator|!=
literal|null
condition|)
block|{
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|Long
argument_list|,
name|MetadataPpdResult
argument_list|>
argument_list|>
name|iter
init|=
name|cache
operator|.
name|getFileMetadataByExpr
argument_list|(
name|fileIds
argument_list|,
name|serializedSarg
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// don't fetch the footer, PPD happens in MS.
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Entry
argument_list|<
name|Long
argument_list|,
name|MetadataPpdResult
argument_list|>
name|e
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|int
name|ix
init|=
name|getAndVerifyIndex
argument_list|(
name|posMap
argument_list|,
name|files
argument_list|,
name|result
argument_list|,
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|processPpdResult
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|,
name|files
operator|.
name|get
argument_list|(
name|ix
argument_list|)
argument_list|,
name|ix
argument_list|,
name|result
argument_list|,
name|ppdResult
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Only populate corrupt IDs for the things we couldn't deserialize if we are not using
comment|// ppd. We assume that PPD makes sure the cached values are correct (or fails otherwise);
comment|// also, we don't use the footers in PPD case.
name|List
argument_list|<
name|Long
argument_list|>
name|corruptIds
init|=
literal|null
decl_stmt|;
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|Long
argument_list|,
name|ByteBuffer
argument_list|>
argument_list|>
name|iter
init|=
name|cache
operator|.
name|getFileMetadata
argument_list|(
name|fileIds
argument_list|)
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Entry
argument_list|<
name|Long
argument_list|,
name|ByteBuffer
argument_list|>
name|e
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|int
name|ix
init|=
name|getAndVerifyIndex
argument_list|(
name|posMap
argument_list|,
name|files
argument_list|,
name|result
argument_list|,
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|processBbResult
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|,
name|ix
argument_list|,
name|files
operator|.
name|get
argument_list|(
name|ix
argument_list|)
argument_list|,
name|result
argument_list|)
condition|)
block|{
if|if
condition|(
name|corruptIds
operator|==
literal|null
condition|)
block|{
name|corruptIds
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|corruptIds
operator|.
name|add
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|corruptIds
operator|!=
literal|null
condition|)
block|{
name|cache
operator|.
name|clearFileMetadata
argument_list|(
name|corruptIds
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|int
name|getAndVerifyIndex
parameter_list|(
name|HashMap
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
name|posMap
parameter_list|,
name|List
argument_list|<
name|HdfsFileStatusWithId
argument_list|>
name|files
parameter_list|,
name|OrcTail
index|[]
name|result
parameter_list|,
name|Long
name|fileId
parameter_list|)
block|{
name|int
name|ix
init|=
name|posMap
operator|.
name|get
argument_list|(
name|fileId
argument_list|)
decl_stmt|;
assert|assert
name|result
index|[
name|ix
index|]
operator|==
literal|null
assert|;
assert|assert
name|fileId
operator|!=
literal|null
operator|&&
name|fileId
operator|.
name|equals
argument_list|(
name|files
operator|.
name|get
argument_list|(
name|ix
argument_list|)
operator|.
name|getFileId
argument_list|()
argument_list|)
assert|;
return|return
name|ix
return|;
block|}
specifier|private
name|boolean
name|processBbResult
parameter_list|(
name|ByteBuffer
name|bb
parameter_list|,
name|int
name|ix
parameter_list|,
name|HdfsFileStatusWithId
name|file
parameter_list|,
name|OrcTail
index|[]
name|result
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|bb
operator|==
literal|null
condition|)
return|return
literal|true
return|;
name|result
index|[
name|ix
index|]
operator|=
name|createOrcTailFromMs
argument_list|(
name|file
argument_list|,
name|bb
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
index|[
name|ix
index|]
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|localCache
operator|.
name|put
argument_list|(
name|file
operator|.
name|getFileStatus
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|,
name|result
index|[
name|ix
index|]
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|private
name|void
name|processPpdResult
parameter_list|(
name|MetadataPpdResult
name|mpr
parameter_list|,
name|HdfsFileStatusWithId
name|file
parameter_list|,
name|int
name|ix
parameter_list|,
name|OrcTail
index|[]
name|result
parameter_list|,
name|ByteBuffer
index|[]
name|ppdResult
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|mpr
operator|==
literal|null
condition|)
return|return;
comment|// This file is unknown to metastore.
name|ppdResult
index|[
name|ix
index|]
operator|=
name|mpr
operator|.
name|isSetIncludeBitset
argument_list|()
condition|?
name|mpr
operator|.
name|bufferForIncludeBitset
argument_list|()
else|:
name|NO_SPLIT_AFTER_PPD
expr_stmt|;
if|if
condition|(
name|mpr
operator|.
name|isSetMetadata
argument_list|()
condition|)
block|{
name|result
index|[
name|ix
index|]
operator|=
name|createOrcTailFromMs
argument_list|(
name|file
argument_list|,
name|mpr
operator|.
name|bufferForMetadata
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
index|[
name|ix
index|]
operator|!=
literal|null
condition|)
block|{
name|localCache
operator|.
name|put
argument_list|(
name|file
operator|.
name|getFileStatus
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|,
name|result
index|[
name|ix
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|List
argument_list|<
name|Long
argument_list|>
name|determineFileIdsToQuery
parameter_list|(
name|List
argument_list|<
name|HdfsFileStatusWithId
argument_list|>
name|files
parameter_list|,
name|OrcTail
index|[]
name|result
parameter_list|,
name|HashMap
argument_list|<
name|Long
argument_list|,
name|Integer
argument_list|>
name|posMap
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|result
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|result
index|[
name|i
index|]
operator|!=
literal|null
condition|)
continue|continue;
name|HdfsFileStatusWithId
name|file
init|=
name|files
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
specifier|final
name|FileStatus
name|fs
init|=
name|file
operator|.
name|getFileStatus
argument_list|()
decl_stmt|;
name|Long
name|fileId
init|=
name|file
operator|.
name|getFileId
argument_list|()
decl_stmt|;
if|if
condition|(
name|fileId
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|isInTest
condition|)
block|{
if|if
condition|(
operator|!
name|isWarnLogged
operator|||
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Not using metastore cache because fileId is missing: "
operator|+
name|fs
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|isWarnLogged
operator|=
literal|true
expr_stmt|;
block|}
continue|continue;
block|}
name|fileId
operator|=
name|generateTestFileId
argument_list|(
name|fs
argument_list|,
name|files
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Generated file ID "
operator|+
name|fileId
operator|+
literal|" at "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|posMap
operator|.
name|put
argument_list|(
name|fileId
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
return|return
name|Lists
operator|.
name|newArrayList
argument_list|(
name|posMap
operator|.
name|keySet
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|Long
name|generateTestFileId
parameter_list|(
specifier|final
name|FileStatus
name|fs
parameter_list|,
name|List
argument_list|<
name|HdfsFileStatusWithId
argument_list|>
name|files
parameter_list|,
name|int
name|i
parameter_list|)
block|{
specifier|final
name|Long
name|fileId
init|=
name|HdfsUtils
operator|.
name|createTestFileId
argument_list|(
name|fs
operator|.
name|getPath
argument_list|()
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|,
name|fs
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|files
operator|.
name|set
argument_list|(
name|i
argument_list|,
operator|new
name|HdfsFileStatusWithId
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|FileStatus
name|getFileStatus
parameter_list|()
block|{
return|return
name|fs
return|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|getFileId
parameter_list|()
block|{
return|return
name|fileId
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|fileId
return|;
block|}
specifier|private
name|ByteBuffer
name|getSerializedSargForMetastore
parameter_list|(
name|boolean
name|isOriginal
parameter_list|)
block|{
if|if
condition|(
name|sarg
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|ByteBuffer
name|serializedSarg
init|=
name|isOriginal
condition|?
name|sargIsOriginal
else|:
name|sargNotIsOriginal
decl_stmt|;
if|if
condition|(
name|serializedSarg
operator|!=
literal|null
condition|)
return|return
name|serializedSarg
return|;
name|SearchArgument
name|sarg2
init|=
name|sarg
decl_stmt|;
name|Kryo
name|kryo
init|=
name|SerializationUtilities
operator|.
name|borrowKryo
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|(
name|isOriginal
condition|?
name|sargNotIsOriginal
else|:
name|sargIsOriginal
operator|)
operator|==
literal|null
condition|)
block|{
name|sarg2
operator|=
name|kryo
operator|.
name|copy
argument_list|(
name|sarg2
argument_list|)
expr_stmt|;
comment|// In case we need it for the other case.
block|}
name|translateSargToTableColIndexes
argument_list|(
name|sarg2
argument_list|,
name|conf
argument_list|,
name|OrcInputFormat
operator|.
name|getRootColumn
argument_list|(
name|isOriginal
argument_list|)
argument_list|)
expr_stmt|;
name|ExternalCache
operator|.
name|Baos
name|baos
init|=
operator|new
name|Baos
argument_list|()
decl_stmt|;
name|Output
name|output
init|=
operator|new
name|Output
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|kryo
operator|.
name|writeObject
argument_list|(
name|output
argument_list|,
name|sarg2
argument_list|)
expr_stmt|;
name|output
operator|.
name|flush
argument_list|()
expr_stmt|;
name|serializedSarg
operator|=
name|baos
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
name|isOriginal
condition|)
block|{
name|sargIsOriginal
operator|=
name|serializedSarg
expr_stmt|;
block|}
else|else
block|{
name|sargNotIsOriginal
operator|=
name|serializedSarg
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|SerializationUtilities
operator|.
name|releaseKryo
argument_list|(
name|kryo
argument_list|)
expr_stmt|;
block|}
return|return
name|serializedSarg
return|;
block|}
comment|/**    * Modifies the SARG, replacing column names with column indexes in target table schema. This    * basically does the same thing as all the shennannigans with included columns, except for the    * last step where ORC gets direct subtypes of root column and uses the ordered match to map    * table columns to file columns. The numbers put into predicate leaf should allow to go into    * said subtypes directly by index to get the proper index in the file.    * This won't work with schema evolution, although it's probably much easier to reason about    * if schema evolution was to be supported, because this is a clear boundary between table    * schema columns and all things ORC. None of the ORC stuff is used here and none of the    * table schema stuff is used after that - ORC doesn't need a bunch of extra crap to apply    * the SARG thus modified.    */
specifier|public
specifier|static
name|void
name|translateSargToTableColIndexes
parameter_list|(
name|SearchArgument
name|sarg
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|int
name|rootColumn
parameter_list|)
block|{
name|String
name|nameStr
init|=
name|OrcInputFormat
operator|.
name|getNeededColumnNamesString
argument_list|(
name|conf
argument_list|)
decl_stmt|,
name|idStr
init|=
name|OrcInputFormat
operator|.
name|getSargColumnIDsString
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|String
index|[]
name|knownNames
init|=
name|nameStr
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|String
index|[]
name|idStrs
init|=
operator|(
name|idStr
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|idStr
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
assert|assert
name|idStrs
operator|==
literal|null
operator|||
name|knownNames
operator|.
name|length
operator|==
name|idStrs
operator|.
name|length
assert|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|nameIdMap
init|=
operator|new
name|HashMap
argument_list|<>
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
name|knownNames
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|Integer
name|newId
init|=
operator|(
name|idStrs
operator|!=
literal|null
operator|)
condition|?
name|Integer
operator|.
name|parseInt
argument_list|(
name|idStrs
index|[
name|i
index|]
argument_list|)
else|:
name|i
decl_stmt|;
name|Integer
name|oldId
init|=
name|nameIdMap
operator|.
name|put
argument_list|(
name|knownNames
index|[
name|i
index|]
argument_list|,
name|newId
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldId
operator|!=
literal|null
operator|&&
name|oldId
operator|.
name|intValue
argument_list|()
operator|!=
name|newId
operator|.
name|intValue
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Multiple IDs for "
operator|+
name|knownNames
index|[
name|i
index|]
operator|+
literal|" in column strings: ["
operator|+
name|idStr
operator|+
literal|"], ["
operator|+
name|nameStr
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
name|List
argument_list|<
name|PredicateLeaf
argument_list|>
name|leaves
init|=
name|sarg
operator|.
name|getLeaves
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
name|leaves
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|PredicateLeaf
name|pl
init|=
name|leaves
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Integer
name|colId
init|=
name|nameIdMap
operator|.
name|get
argument_list|(
name|pl
operator|.
name|getColumnName
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|newColName
init|=
name|RecordReaderImpl
operator|.
name|encodeTranslatedSargColumn
argument_list|(
name|rootColumn
argument_list|,
name|colId
argument_list|)
decl_stmt|;
name|SearchArgumentFactory
operator|.
name|setPredicateLeafColumn
argument_list|(
name|pl
argument_list|,
name|newColName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"SARG translated into "
operator|+
name|sarg
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|OrcTail
name|createOrcTailFromMs
parameter_list|(
name|HdfsFileStatusWithId
name|file
parameter_list|,
name|ByteBuffer
name|bb
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|bb
operator|==
literal|null
condition|)
return|return
literal|null
return|;
name|FileStatus
name|fs
init|=
name|file
operator|.
name|getFileStatus
argument_list|()
decl_stmt|;
name|ByteBuffer
name|copy
init|=
name|bb
operator|.
name|duplicate
argument_list|()
decl_stmt|;
try|try
block|{
name|OrcTail
name|orcTail
init|=
name|ReaderImpl
operator|.
name|extractFileTail
argument_list|(
name|copy
argument_list|,
name|fs
operator|.
name|getLen
argument_list|()
argument_list|,
name|fs
operator|.
name|getModificationTime
argument_list|()
argument_list|)
decl_stmt|;
comment|// trigger lazy read of metadata to make sure serialized data is not corrupted and readable
name|orcTail
operator|.
name|getStripeStatistics
argument_list|()
expr_stmt|;
return|return
name|orcTail
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|byte
index|[]
name|data
init|=
operator|new
name|byte
index|[
name|bb
operator|.
name|remaining
argument_list|()
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|bb
operator|.
name|array
argument_list|()
argument_list|,
name|bb
operator|.
name|arrayOffset
argument_list|()
operator|+
name|bb
operator|.
name|position
argument_list|()
argument_list|,
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
name|String
name|msg
init|=
literal|"Failed to parse the footer stored in cache for file ID "
operator|+
name|file
operator|.
name|getFileId
argument_list|()
operator|+
literal|" "
operator|+
name|bb
operator|+
literal|" [ "
operator|+
name|Hex
operator|.
name|encodeHexString
argument_list|(
name|data
argument_list|)
operator|+
literal|" ]"
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|,
name|ex
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|Baos
extends|extends
name|ByteArrayOutputStream
block|{
specifier|public
name|ByteBuffer
name|get
parameter_list|()
block|{
return|return
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|count
argument_list|)
return|;
block|}
block|}
comment|/** An abstraction for testing ExternalCache in OrcInputFormat. */
specifier|public
interface|interface
name|ExternalFooterCachesByConf
block|{
specifier|public
interface|interface
name|Cache
block|{
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|MetadataPpdResult
argument_list|>
argument_list|>
name|getFileMetadataByExpr
parameter_list|(
name|List
argument_list|<
name|Long
argument_list|>
name|fileIds
parameter_list|,
name|ByteBuffer
name|serializedSarg
parameter_list|,
name|boolean
name|doGetFooters
parameter_list|)
throws|throws
name|HiveException
function_decl|;
name|void
name|clearFileMetadata
parameter_list|(
name|List
argument_list|<
name|Long
argument_list|>
name|fileIds
parameter_list|)
throws|throws
name|HiveException
function_decl|;
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|ByteBuffer
argument_list|>
argument_list|>
name|getFileMetadata
parameter_list|(
name|List
argument_list|<
name|Long
argument_list|>
name|fileIds
parameter_list|)
throws|throws
name|HiveException
function_decl|;
name|void
name|putFileMetadata
parameter_list|(
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|keys
parameter_list|,
name|ArrayList
argument_list|<
name|ByteBuffer
argument_list|>
name|values
parameter_list|)
throws|throws
name|HiveException
function_decl|;
block|}
specifier|public
name|Cache
name|getCache
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
block|}
end_class

end_unit

