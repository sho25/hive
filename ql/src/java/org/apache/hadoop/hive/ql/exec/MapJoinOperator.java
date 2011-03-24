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
name|Map
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
name|Map
argument_list|<
name|Byte
argument_list|,
name|HashMapWrapper
argument_list|<
name|AbstractMapJoinKey
argument_list|,
name|MapJoinObjectValue
argument_list|>
argument_list|>
name|mapJoinTables
decl_stmt|;
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
literal|"Mapside join size exceeds hive.mapjoin.maxsize. "
operator|+
literal|"Please increase that or remove the mapjoin hint."
block|}
decl_stmt|;
specifier|protected
specifier|transient
name|Map
argument_list|<
name|Byte
argument_list|,
name|MapJoinRowContainer
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
argument_list|>
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
name|int
name|maxMapJoinSize
decl_stmt|;
specifier|transient
name|boolean
name|hashTblInitedOnce
decl_stmt|;
specifier|private
name|int
name|bigTableAlias
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
name|maxMapJoinSize
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMAXMAPJOINSIZE
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
name|bigTableAlias
operator|=
name|order
index|[
name|posBigTable
index|]
expr_stmt|;
name|mapJoinTables
operator|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|HashMapWrapper
argument_list|<
name|AbstractMapJoinKey
argument_list|,
name|MapJoinObjectValue
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|rowContainerMap
operator|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|MapJoinRowContainer
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
argument_list|>
argument_list|()
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
operator|.
name|put
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
name|pos
argument_list|)
argument_list|,
name|hashTable
argument_list|)
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
operator|.
name|put
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
operator|(
name|byte
operator|)
name|pos
argument_list|)
argument_list|,
name|rowContainer
argument_list|)
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
name|MapJoinMetaData
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
name|hconf
argument_list|)
argument_list|)
expr_stmt|;
comment|// index for values is just alias
for|for
control|(
name|int
name|tag
init|=
literal|0
init|;
name|tag
operator|<
name|order
operator|.
name|length
condition|;
name|tag
operator|++
control|)
block|{
name|int
name|alias
init|=
operator|(
name|int
operator|)
name|order
index|[
name|tag
index|]
decl_stmt|;
if|if
condition|(
name|alias
operator|==
name|this
operator|.
name|bigTableAlias
condition|)
block|{
continue|continue;
block|}
name|TableDesc
name|valueTableDesc
init|=
name|conf
operator|.
name|getValueTblDescs
argument_list|()
operator|.
name|get
argument_list|(
name|tag
argument_list|)
decl_stmt|;
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
name|MapJoinMetaData
operator|.
name|put
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|alias
argument_list|)
argument_list|,
operator|new
name|HashTableSinkObjectCtx
argument_list|(
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|valueSerDe
operator|.
name|getObjectInspector
argument_list|()
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
name|boolean
name|localMode
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|hconf
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
decl_stmt|;
name|String
name|baseDir
init|=
literal|null
decl_stmt|;
name|String
name|currentInputFile
init|=
name|HiveConf
operator|.
name|getVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HADOOPMAPFILENAME
argument_list|)
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
name|currentFileName
decl_stmt|;
if|if
condition|(
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
name|currentFileName
operator|=
name|this
operator|.
name|getFileName
argument_list|(
name|currentInputFile
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|currentFileName
operator|=
literal|"-"
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|localMode
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
name|Map
operator|.
name|Entry
argument_list|<
name|Byte
argument_list|,
name|HashMapWrapper
argument_list|<
name|AbstractMapJoinKey
argument_list|,
name|MapJoinObjectValue
argument_list|>
argument_list|>
name|entry
range|:
name|mapJoinTables
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Byte
name|pos
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|HashMapWrapper
argument_list|<
name|AbstractMapJoinKey
argument_list|,
name|MapJoinObjectValue
argument_list|>
name|hashtable
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|String
name|filePath
init|=
name|Utilities
operator|.
name|generatePath
argument_list|(
name|baseDir
argument_list|,
name|pos
argument_list|,
name|currentFileName
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
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
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
comment|// get alias
name|alias
operator|=
name|order
index|[
name|tag
index|]
expr_stmt|;
comment|// alias = (byte)tag;
if|if
condition|(
operator|(
name|lastAlias
operator|==
literal|null
operator|)
operator|||
operator|(
operator|!
name|lastAlias
operator|.
name|equals
argument_list|(
name|alias
argument_list|)
operator|)
condition|)
block|{
name|nextSz
operator|=
name|joinEmitInterval
expr_stmt|;
block|}
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
operator|.
name|get
argument_list|(
name|alias
argument_list|)
argument_list|,
name|joinKeysObjectInspectors
operator|.
name|get
argument_list|(
name|alias
argument_list|)
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|value
init|=
name|JoinUtil
operator|.
name|computeValues
argument_list|(
name|row
argument_list|,
name|joinValues
operator|.
name|get
argument_list|(
name|alias
argument_list|)
argument_list|,
name|joinValuesObjectInspectors
operator|.
name|get
argument_list|(
name|alias
argument_list|)
argument_list|,
name|joinFilters
operator|.
name|get
argument_list|(
name|alias
argument_list|)
argument_list|,
name|joinFilterObjectInspectors
operator|.
name|get
argument_list|(
name|alias
argument_list|)
argument_list|,
name|noOuterJoin
argument_list|)
decl_stmt|;
comment|// Add the value to the ArrayList
name|storage
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|tag
argument_list|)
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
for|for
control|(
name|Byte
name|pos
range|:
name|order
control|)
block|{
if|if
condition|(
name|pos
operator|.
name|intValue
argument_list|()
operator|!=
name|tag
condition|)
block|{
name|MapJoinObjectValue
name|o
init|=
name|mapJoinTables
operator|.
name|get
argument_list|(
name|pos
argument_list|)
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
operator|.
name|get
argument_list|(
name|pos
argument_list|)
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
argument_list|()
condition|)
block|{
if|if
condition|(
name|noOuterJoin
condition|)
block|{
name|storage
operator|.
name|put
argument_list|(
name|pos
argument_list|,
name|emptyList
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|storage
operator|.
name|put
argument_list|(
name|pos
argument_list|,
name|dummyObjVectors
index|[
name|pos
operator|.
name|intValue
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
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
operator|.
name|put
argument_list|(
name|pos
argument_list|,
name|rowContainer
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// generate the output records
name|checkAndGenObject
argument_list|()
expr_stmt|;
comment|// done with the row
name|storage
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|tag
argument_list|)
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|Byte
name|pos
range|:
name|order
control|)
block|{
if|if
condition|(
name|pos
operator|.
name|intValue
argument_list|()
operator|!=
name|tag
condition|)
block|{
name|storage
operator|.
name|put
argument_list|(
name|pos
argument_list|,
literal|null
argument_list|)
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
specifier|private
name|String
name|getFileName
parameter_list|(
name|String
name|path
parameter_list|)
block|{
if|if
condition|(
name|path
operator|==
literal|null
operator|||
name|path
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|last_separator
init|=
name|path
operator|.
name|lastIndexOf
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
operator|+
literal|1
decl_stmt|;
name|String
name|fileName
init|=
name|path
operator|.
name|substring
argument_list|(
name|last_separator
argument_list|)
decl_stmt|;
return|return
name|fileName
return|;
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
operator|.
name|values
argument_list|()
control|)
block|{
name|hashTable
operator|.
name|close
argument_list|()
expr_stmt|;
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

