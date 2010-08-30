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
name|MapJoinObjectKey
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
name|RowContainer
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
name|MapJoinObjectKey
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
comment|/**    * MapJoinObjectCtx.    *    */
specifier|public
specifier|static
class|class
name|MapJoinObjectCtx
block|{
name|ObjectInspector
name|standardOI
decl_stmt|;
name|SerDe
name|serde
decl_stmt|;
name|TableDesc
name|tblDesc
decl_stmt|;
name|Configuration
name|conf
decl_stmt|;
comment|/**      * @param standardOI      * @param serde      */
specifier|public
name|MapJoinObjectCtx
parameter_list|(
name|ObjectInspector
name|standardOI
parameter_list|,
name|SerDe
name|serde
parameter_list|,
name|TableDesc
name|tblDesc
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|standardOI
operator|=
name|standardOI
expr_stmt|;
name|this
operator|.
name|serde
operator|=
name|serde
expr_stmt|;
name|this
operator|.
name|tblDesc
operator|=
name|tblDesc
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
comment|/**      * @return the standardOI      */
specifier|public
name|ObjectInspector
name|getStandardOI
parameter_list|()
block|{
return|return
name|standardOI
return|;
block|}
comment|/**      * @return the serde      */
specifier|public
name|SerDe
name|getSerDe
parameter_list|()
block|{
return|return
name|serde
return|;
block|}
specifier|public
name|TableDesc
name|getTblDesc
parameter_list|()
block|{
return|return
name|tblDesc
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
block|}
specifier|static
specifier|transient
name|Map
argument_list|<
name|Integer
argument_list|,
name|MapJoinObjectCtx
argument_list|>
name|mapMetadata
init|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|MapJoinObjectCtx
argument_list|>
argument_list|()
decl_stmt|;
specifier|static
specifier|transient
name|int
name|nextVal
init|=
literal|0
decl_stmt|;
specifier|public
specifier|static
name|Map
argument_list|<
name|Integer
argument_list|,
name|MapJoinObjectCtx
argument_list|>
name|getMapMetadata
parameter_list|()
block|{
return|return
name|mapMetadata
return|;
block|}
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
name|mapJoinTables
operator|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|HashMapWrapper
argument_list|<
name|MapJoinObjectKey
argument_list|,
name|MapJoinObjectValue
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
name|int
name|cacheSize
init|=
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
name|HIVEMAPJOINCACHEROWS
argument_list|)
decl_stmt|;
name|HashMapWrapper
argument_list|<
name|MapJoinObjectKey
argument_list|,
name|MapJoinObjectValue
argument_list|>
name|hashTable
init|=
operator|new
name|HashMapWrapper
argument_list|<
name|MapJoinObjectKey
argument_list|,
name|MapJoinObjectValue
argument_list|>
argument_list|(
name|cacheSize
argument_list|)
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
block|}
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
if|if
condition|(
name|tag
operator|==
name|posBigTable
condition|)
block|{
name|this
operator|.
name|getExecContext
argument_list|()
operator|.
name|processInputFileChangeForLocalWork
argument_list|()
expr_stmt|;
block|}
try|try
block|{
comment|// get alias
name|alias
operator|=
operator|(
name|byte
operator|)
name|tag
expr_stmt|;
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
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|key
init|=
name|computeValues
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
argument_list|)
decl_stmt|;
comment|// does this source need to be stored in the hash map
if|if
condition|(
name|tag
operator|!=
name|posBigTable
condition|)
block|{
if|if
condition|(
name|firstRow
condition|)
block|{
name|metadataKeyTag
operator|=
name|nextVal
operator|++
expr_stmt|;
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
name|mapMetadata
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
name|MapJoinObjectCtx
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
name|firstRow
operator|=
literal|false
expr_stmt|;
block|}
name|reportProgress
argument_list|()
expr_stmt|;
name|numMapRowsRead
operator|++
expr_stmt|;
if|if
condition|(
operator|(
name|numMapRowsRead
operator|>
name|maxMapJoinSize
operator|)
operator|&&
operator|(
name|reporter
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|counterNameToEnum
operator|!=
literal|null
operator|)
condition|)
block|{
comment|// update counter
name|LOG
operator|.
name|warn
argument_list|(
literal|"Too many rows in map join tables. Fatal error counter will be incremented!!"
argument_list|)
expr_stmt|;
name|incrCounter
argument_list|(
name|fatalErrorCntr
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|fatalError
operator|=
literal|true
expr_stmt|;
return|return;
block|}
name|HashMapWrapper
argument_list|<
name|MapJoinObjectKey
argument_list|,
name|MapJoinObjectValue
argument_list|>
name|hashTable
init|=
name|mapJoinTables
operator|.
name|get
argument_list|(
name|alias
argument_list|)
decl_stmt|;
name|MapJoinObjectKey
name|keyMap
init|=
operator|new
name|MapJoinObjectKey
argument_list|(
name|metadataKeyTag
argument_list|,
name|key
argument_list|)
decl_stmt|;
name|MapJoinObjectValue
name|o
init|=
name|hashTable
operator|.
name|get
argument_list|(
name|keyMap
argument_list|)
decl_stmt|;
name|RowContainer
name|res
init|=
literal|null
decl_stmt|;
name|boolean
name|needNewKey
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|o
operator|==
literal|null
condition|)
block|{
name|int
name|bucketSize
init|=
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
name|HIVEMAPJOINBUCKETCACHESIZE
argument_list|)
decl_stmt|;
name|res
operator|=
name|getRowContainer
argument_list|(
name|hconf
argument_list|,
operator|(
name|byte
operator|)
name|tag
argument_list|,
name|order
index|[
name|tag
index|]
argument_list|,
name|bucketSize
argument_list|)
expr_stmt|;
name|res
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|res
operator|=
name|o
operator|.
name|getObj
argument_list|()
expr_stmt|;
name|res
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
comment|// If key already exists, HashMapWrapper.get() guarantees it is
comment|// already in main memory HashMap
comment|// cache. So just replacing the object value should update the
comment|// HashMapWrapper. This will save
comment|// the cost of constructing the new key/object and deleting old one
comment|// and inserting the new one.
if|if
condition|(
name|hashTable
operator|.
name|cacheSize
argument_list|()
operator|>
literal|0
condition|)
block|{
name|o
operator|.
name|setObj
argument_list|(
name|res
argument_list|)
expr_stmt|;
name|needNewKey
operator|=
literal|false
expr_stmt|;
block|}
block|}
if|if
condition|(
name|metadataValueTag
index|[
name|tag
index|]
operator|==
operator|-
literal|1
condition|)
block|{
name|metadataValueTag
index|[
name|tag
index|]
operator|=
name|nextVal
operator|++
expr_stmt|;
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
name|mapMetadata
operator|.
name|put
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|metadataValueTag
index|[
name|tag
index|]
argument_list|)
argument_list|,
operator|new
name|MapJoinObjectCtx
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
comment|// Construct externalizable objects for key and value
if|if
condition|(
name|needNewKey
condition|)
block|{
name|MapJoinObjectKey
name|keyObj
init|=
operator|new
name|MapJoinObjectKey
argument_list|(
name|metadataKeyTag
argument_list|,
name|key
argument_list|)
decl_stmt|;
name|MapJoinObjectValue
name|valueObj
init|=
operator|new
name|MapJoinObjectValue
argument_list|(
name|metadataValueTag
index|[
name|tag
index|]
argument_list|,
name|res
argument_list|)
decl_stmt|;
name|valueObj
operator|.
name|setConf
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|valueObj
operator|.
name|setConf
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
comment|// This may potentially increase the size of the hashmap on the mapper
if|if
condition|(
name|res
operator|.
name|size
argument_list|()
operator|>
name|mapJoinRowsKey
condition|)
block|{
if|if
condition|(
name|res
operator|.
name|size
argument_list|()
operator|%
literal|100
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Number of values for a given key "
operator|+
name|keyObj
operator|+
literal|" are "
operator|+
name|res
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"used memory "
operator|+
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|totalMemory
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|hashTable
operator|.
name|put
argument_list|(
name|keyObj
argument_list|,
name|valueObj
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
comment|// Add the value to the ArrayList
name|storage
operator|.
name|get
argument_list|(
name|alias
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
name|MapJoinObjectKey
name|keyMap
init|=
operator|new
name|MapJoinObjectKey
argument_list|(
name|metadataKeyTag
argument_list|,
name|key
argument_list|)
decl_stmt|;
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
name|keyMap
argument_list|)
decl_stmt|;
comment|// there is no join-value or join-key has all null elements
if|if
condition|(
name|o
operator|==
literal|null
operator|||
operator|(
name|hasAnyNulls
argument_list|(
name|key
argument_list|)
operator|)
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
name|storage
operator|.
name|put
argument_list|(
name|pos
argument_list|,
name|o
operator|.
name|getObj
argument_list|()
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
name|alias
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
name|int
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

