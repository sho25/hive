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
name|exec
package|;
end_package

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
name|util
operator|.
name|ArrayList
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
name|filecache
operator|.
name|DistributedCache
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
name|ql
operator|.
name|exec
operator|.
name|HashTableSinkOperator
operator|.
name|HashTableSinkObjectCtx
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
name|persistence
operator|.
name|AbstractMapJoinKey
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
name|persistence
operator|.
name|HashMapWrapper
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
name|persistence
operator|.
name|MapJoinObjectValue
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
name|persistence
operator|.
name|MapJoinRowContainer
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
name|plan
operator|.
name|MapJoinDesc
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
name|TableDesc
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
name|api
operator|.
name|OperatorType
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
name|serde2
operator|.
name|SerDe
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
name|serde2
operator|.
name|SerDeException
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspector
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
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
name|serde2
operator|.
name|objectinspector
operator|.
name|ObjectInspectorUtils
operator|.
name|ObjectInspectorCopyOption
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
name|ShimLoader
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
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * Map side Join operator implementation.  */
end_comment

begin_class
specifier|public
class|class
name|MapJoinOperator
extends|extends
name|AbstractMapJoinOperator
argument_list|<
name|MapJoinDesc
argument_list|>
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
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
name|MapJoinOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
specifier|transient
name|HashMapWrapper
argument_list|<
name|AbstractMapJoinKey
argument_list|,
name|MapJoinObjectValue
argument_list|>
index|[]
name|mapJoinTables
decl_stmt|;
specifier|protected
specifier|static
name|MapJoinMetaData
name|metadata
init|=
operator|new
name|MapJoinMetaData
argument_list|()
decl_stmt|;
specifier|public
specifier|static
name|MapJoinMetaData
name|getMetadata
parameter_list|()
block|{
return|return
name|metadata
return|;
block|}
specifier|private
specifier|static
specifier|final
specifier|transient
name|String
index|[]
name|FATAL_ERR_MSG
init|=
block|{
literal|null
block|,
comment|// counter value 0 means no error
literal|"Mapside join exceeds available memory. "
operator|+
literal|"Please try removing the mapjoin hint."
block|}
decl_stmt|;
specifier|protected
specifier|transient
name|MapJoinRowContainer
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
index|[]
name|rowContainerMap
decl_stmt|;
specifier|transient
name|int
name|metadataKeyTag
decl_stmt|;
specifier|transient
name|int
index|[]
name|metadataValueTag
decl_stmt|;
specifier|transient
name|boolean
name|hashTblInitedOnce
decl_stmt|;
specifier|public
name|MapJoinOperator
parameter_list|()
block|{   }
specifier|public
name|MapJoinOperator
parameter_list|(
name|AbstractMapJoinOperator
argument_list|<
name|?
extends|extends
name|MapJoinDesc
argument_list|>
name|mjop
parameter_list|)
block|{
name|super
argument_list|(
name|mjop
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|protected
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|metadataValueTag
operator|=
operator|new
name|int
index|[
name|numAliases
index|]
expr_stmt|;
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|numAliases
condition|;
name|pos
operator|++
control|)
block|{
name|metadataValueTag
index|[
name|pos
index|]
operator|=
operator|-
literal|1
expr_stmt|;
block|}
name|metadataKeyTag
operator|=
operator|-
literal|1
expr_stmt|;
name|int
name|tagLen
init|=
name|conf
operator|.
name|getTagLength
argument_list|()
decl_stmt|;
name|mapJoinTables
operator|=
operator|new
name|HashMapWrapper
index|[
name|tagLen
index|]
expr_stmt|;
name|rowContainerMap
operator|=
operator|new
name|MapJoinRowContainer
index|[
name|tagLen
index|]
expr_stmt|;
comment|// initialize the hash tables for other tables
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|numAliases
condition|;
name|pos
operator|++
control|)
block|{
if|if
condition|(
name|pos
operator|==
name|posBigTable
condition|)
block|{
continue|continue;
block|}
name|HashMapWrapper
argument_list|<
name|AbstractMapJoinKey
argument_list|,
name|MapJoinObjectValue
argument_list|>
name|hashTable
init|=
operator|new
name|HashMapWrapper
argument_list|<
name|AbstractMapJoinKey
argument_list|,
name|MapJoinObjectValue
argument_list|>
argument_list|()
decl_stmt|;
name|mapJoinTables
index|[
name|pos
index|]
operator|=
name|hashTable
expr_stmt|;
name|MapJoinRowContainer
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
name|rowContainer
init|=
operator|new
name|MapJoinRowContainer
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|rowContainerMap
index|[
name|pos
index|]
operator|=
name|rowContainer
expr_stmt|;
block|}
name|hashTblInitedOnce
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|fatalErrorMessage
parameter_list|(
name|StringBuilder
name|errMsg
parameter_list|,
name|long
name|counterCode
parameter_list|)
block|{
name|errMsg
operator|.
name|append
argument_list|(
literal|"Operator "
operator|+
name|getOperatorId
argument_list|()
operator|+
literal|" (id="
operator|+
name|id
operator|+
literal|"): "
operator|+
name|FATAL_ERR_MSG
index|[
operator|(
name|int
operator|)
name|counterCode
index|]
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|generateMapMetaData
parameter_list|()
throws|throws
name|HiveException
throws|,
name|SerDeException
block|{
comment|// generate the meta data for key
comment|// index for key is -1
name|TableDesc
name|keyTableDesc
init|=
name|conf
operator|.
name|getKeyTblDesc
argument_list|()
decl_stmt|;
name|SerDe
name|keySerializer
init|=
operator|(
name|SerDe
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|keyTableDesc
operator|.
name|getDeserializerClass
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|keySerializer
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
name|keyTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
name|metadata
operator|.
name|put
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|metadataKeyTag
argument_list|)
argument_list|,
operator|new
name|HashTableSinkObjectCtx
argument_list|(
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|keySerializer
operator|.
name|getObjectInspector
argument_list|()
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
argument_list|,
name|keySerializer
argument_list|,
name|keyTableDesc
argument_list|,
literal|false
argument_list|,
name|hconf
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|order
operator|.
name|length
condition|;
name|pos
operator|++
control|)
block|{
if|if
condition|(
name|pos
operator|==
name|posBigTable
condition|)
block|{
continue|continue;
block|}
name|TableDesc
name|valueTableDesc
decl_stmt|;
if|if
condition|(
name|conf
operator|.
name|getNoOuterJoin
argument_list|()
condition|)
block|{
name|valueTableDesc
operator|=
name|conf
operator|.
name|getValueTblDescs
argument_list|()
operator|.
name|get
argument_list|(
name|pos
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|valueTableDesc
operator|=
name|conf
operator|.
name|getValueFilteredTblDescs
argument_list|()
operator|.
name|get
argument_list|(
name|pos
argument_list|)
expr_stmt|;
block|}
name|SerDe
name|valueSerDe
init|=
operator|(
name|SerDe
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|valueTableDesc
operator|.
name|getDeserializerClass
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|valueSerDe
operator|.
name|initialize
argument_list|(
literal|null
argument_list|,
name|valueTableDesc
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
name|ObjectInspector
name|inspector
init|=
name|valueSerDe
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
name|metadata
operator|.
name|put
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|pos
argument_list|)
argument_list|,
operator|new
name|HashTableSinkObjectCtx
argument_list|(
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|inspector
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
argument_list|,
name|valueSerDe
argument_list|,
name|valueTableDesc
argument_list|,
name|hasFilter
argument_list|(
name|pos
argument_list|)
argument_list|,
name|hconf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|loadHashTable
parameter_list|()
throws|throws
name|HiveException
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|getExecContext
argument_list|()
operator|.
name|getLocalWork
argument_list|()
operator|.
name|getInputFileChangeSensitive
argument_list|()
condition|)
block|{
if|if
condition|(
name|hashTblInitedOnce
condition|)
block|{
return|return;
block|}
else|else
block|{
name|hashTblInitedOnce
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|String
name|baseDir
init|=
literal|null
decl_stmt|;
name|String
name|currentInputFile
init|=
name|getExecContext
argument_list|()
operator|.
name|getCurrentInputFile
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"******* Load from HashTable File: input : "
operator|+
name|currentInputFile
argument_list|)
expr_stmt|;
name|String
name|fileName
init|=
name|getExecContext
argument_list|()
operator|.
name|getLocalWork
argument_list|()
operator|.
name|getBucketFileName
argument_list|(
name|currentInputFile
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|ShimLoader
operator|.
name|getHadoopShims
argument_list|()
operator|.
name|isLocalMode
argument_list|(
name|hconf
argument_list|)
condition|)
block|{
name|baseDir
operator|=
name|this
operator|.
name|getExecContext
argument_list|()
operator|.
name|getLocalWork
argument_list|()
operator|.
name|getTmpFileURI
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|Path
index|[]
name|localArchives
decl_stmt|;
name|String
name|stageID
init|=
name|this
operator|.
name|getExecContext
argument_list|()
operator|.
name|getLocalWork
argument_list|()
operator|.
name|getStageID
argument_list|()
decl_stmt|;
name|String
name|suffix
init|=
name|Utilities
operator|.
name|generateTarFileName
argument_list|(
name|stageID
argument_list|)
decl_stmt|;
name|FileSystem
name|localFs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
name|localArchives
operator|=
name|DistributedCache
operator|.
name|getLocalCacheArchives
argument_list|(
name|this
operator|.
name|hconf
argument_list|)
expr_stmt|;
name|Path
name|archive
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|localArchives
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|archive
operator|=
name|localArchives
index|[
name|j
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|archive
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
name|suffix
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|Path
name|archiveLocalLink
init|=
name|archive
operator|.
name|makeQualified
argument_list|(
name|localFs
argument_list|)
decl_stmt|;
name|baseDir
operator|=
name|archiveLocalLink
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
expr_stmt|;
block|}
block|}
for|for
control|(
name|byte
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|mapJoinTables
operator|.
name|length
condition|;
name|pos
operator|++
control|)
block|{
name|HashMapWrapper
argument_list|<
name|AbstractMapJoinKey
argument_list|,
name|MapJoinObjectValue
argument_list|>
name|hashtable
init|=
name|mapJoinTables
index|[
name|pos
index|]
decl_stmt|;
if|if
condition|(
name|hashtable
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|String
name|filePath
init|=
name|Utilities
operator|.
name|generatePath
argument_list|(
name|baseDir
argument_list|,
name|conf
operator|.
name|getDumpFilePrefix
argument_list|()
argument_list|,
name|pos
argument_list|,
name|fileName
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|filePath
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"\tLoad back 1 hashtable file from tmp file uri:"
operator|+
name|path
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|hashtable
operator|.
name|initilizePersistentHash
argument_list|(
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Load Distributed Cache Error"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|// Load the hash table
annotation|@
name|Override
specifier|public
name|void
name|cleanUpInputFileChangedOp
parameter_list|()
throws|throws
name|HiveException
block|{
try|try
block|{
if|if
condition|(
name|firstRow
condition|)
block|{
comment|// generate the map metadata
name|generateMapMetaData
argument_list|()
expr_stmt|;
name|firstRow
operator|=
literal|false
expr_stmt|;
block|}
name|loadHashTable
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|processOp
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
if|if
condition|(
name|firstRow
condition|)
block|{
comment|// generate the map metadata
name|generateMapMetaData
argument_list|()
expr_stmt|;
name|firstRow
operator|=
literal|false
expr_stmt|;
block|}
name|alias
operator|=
operator|(
name|byte
operator|)
name|tag
expr_stmt|;
comment|// compute keys and values as StandardObjects
name|AbstractMapJoinKey
name|key
init|=
name|JoinUtil
operator|.
name|computeMapJoinKeys
argument_list|(
name|row
argument_list|,
name|joinKeys
index|[
name|alias
index|]
argument_list|,
name|joinKeysObjectInspectors
index|[
name|alias
index|]
argument_list|)
decl_stmt|;
name|boolean
name|joinNeeded
init|=
literal|false
decl_stmt|;
for|for
control|(
name|byte
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|order
operator|.
name|length
condition|;
name|pos
operator|++
control|)
block|{
if|if
condition|(
name|pos
operator|!=
name|alias
condition|)
block|{
name|MapJoinObjectValue
name|o
init|=
name|mapJoinTables
index|[
name|pos
index|]
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|MapJoinRowContainer
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
name|rowContainer
init|=
name|rowContainerMap
index|[
name|pos
index|]
decl_stmt|;
comment|// there is no join-value or join-key has all null elements
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|key
operator|.
name|hasAnyNulls
argument_list|(
name|nullsafes
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|noOuterJoin
condition|)
block|{
name|joinNeeded
operator|=
literal|true
expr_stmt|;
name|storage
index|[
name|pos
index|]
operator|=
name|dummyObjVectors
index|[
name|pos
index|]
expr_stmt|;
block|}
else|else
block|{
name|storage
index|[
name|pos
index|]
operator|=
name|emptyList
expr_stmt|;
block|}
block|}
else|else
block|{
name|joinNeeded
operator|=
literal|true
expr_stmt|;
name|rowContainer
operator|.
name|reset
argument_list|(
name|o
operator|.
name|getObj
argument_list|()
argument_list|)
expr_stmt|;
name|storage
index|[
name|pos
index|]
operator|=
name|rowContainer
expr_stmt|;
name|aliasFilterTags
index|[
name|pos
index|]
operator|=
name|o
operator|.
name|getAliasFilter
argument_list|()
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|joinNeeded
condition|)
block|{
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|value
init|=
name|getFilteredValue
argument_list|(
name|alias
argument_list|,
name|row
argument_list|)
decl_stmt|;
comment|// Add the value to the ArrayList
name|storage
index|[
name|alias
index|]
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
comment|// generate the output records
name|checkAndGenObject
argument_list|()
expr_stmt|;
block|}
comment|// done with the row
name|storage
index|[
name|tag
index|]
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|byte
name|pos
init|=
literal|0
init|;
name|pos
operator|<
name|order
operator|.
name|length
condition|;
name|pos
operator|++
control|)
block|{
if|if
condition|(
name|pos
operator|!=
name|tag
condition|)
block|{
name|storage
index|[
name|pos
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|SerDeException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|closeOp
parameter_list|(
name|boolean
name|abort
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|mapJoinTables
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|HashMapWrapper
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|hashTable
range|:
name|mapJoinTables
control|)
block|{
if|if
condition|(
name|hashTable
operator|!=
literal|null
condition|)
block|{
name|hashTable
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|super
operator|.
name|closeOp
argument_list|(
name|abort
argument_list|)
expr_stmt|;
block|}
comment|/**    * Implements the getName function for the Node Interface.    *    * @return the name of the operator    */
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|getOperatorName
argument_list|()
return|;
block|}
specifier|static
specifier|public
name|String
name|getOperatorName
parameter_list|()
block|{
return|return
literal|"MAPJOIN"
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperatorType
name|getType
parameter_list|()
block|{
return|return
name|OperatorType
operator|.
name|MAPJOIN
return|;
block|}
block|}
end_class

end_unit

