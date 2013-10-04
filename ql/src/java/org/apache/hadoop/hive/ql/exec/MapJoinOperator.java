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
name|hive
operator|.
name|ql
operator|.
name|HashTableLoaderFactory
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
name|MapJoinKey
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
name|MapJoinObjectSerDeContext
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
name|exec
operator|.
name|persistence
operator|.
name|MapJoinTableContainer
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
name|MapJoinTableContainerSerDe
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
specifier|private
specifier|transient
name|String
name|tableKey
decl_stmt|;
specifier|private
specifier|transient
name|String
name|serdeKey
decl_stmt|;
specifier|private
specifier|transient
name|ObjectCache
name|cache
decl_stmt|;
specifier|private
name|HashTableLoader
name|loader
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
literal|"Mapside join exceeds available memory. "
operator|+
literal|"Please try removing the mapjoin hint."
block|}
decl_stmt|;
specifier|private
specifier|transient
name|MapJoinTableContainer
index|[]
name|mapJoinTables
decl_stmt|;
specifier|private
specifier|transient
name|MapJoinTableContainerSerDe
index|[]
name|mapJoinTableSerdes
decl_stmt|;
specifier|private
specifier|transient
name|boolean
name|hashTblInitedOnce
decl_stmt|;
specifier|private
specifier|transient
name|MapJoinKey
name|key
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
name|int
name|tagLen
init|=
name|conf
operator|.
name|getTagLength
argument_list|()
decl_stmt|;
name|tableKey
operator|=
literal|"__HASH_MAP_"
operator|+
name|this
operator|.
name|getOperatorId
argument_list|()
operator|+
literal|"_container"
expr_stmt|;
name|serdeKey
operator|=
literal|"__HASH_MAP_"
operator|+
name|this
operator|.
name|getOperatorId
argument_list|()
operator|+
literal|"_serde"
expr_stmt|;
name|cache
operator|=
name|ObjectCacheFactory
operator|.
name|getCache
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|loader
operator|=
name|HashTableLoaderFactory
operator|.
name|getLoader
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|mapJoinTables
operator|=
operator|(
name|MapJoinTableContainer
index|[]
operator|)
name|cache
operator|.
name|retrieve
argument_list|(
name|tableKey
argument_list|)
expr_stmt|;
name|mapJoinTableSerdes
operator|=
operator|(
name|MapJoinTableContainerSerDe
index|[]
operator|)
name|cache
operator|.
name|retrieve
argument_list|(
name|serdeKey
argument_list|)
expr_stmt|;
name|hashTblInitedOnce
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|mapJoinTables
operator|==
literal|null
operator|||
name|mapJoinTableSerdes
operator|==
literal|null
condition|)
block|{
name|mapJoinTables
operator|=
operator|new
name|MapJoinTableContainer
index|[
name|tagLen
index|]
expr_stmt|;
name|mapJoinTableSerdes
operator|=
operator|new
name|MapJoinTableContainerSerDe
index|[
name|tagLen
index|]
expr_stmt|;
name|hashTblInitedOnce
operator|=
literal|false
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
name|MapJoinObjectSerDeContext
name|keyContext
init|=
operator|new
name|MapJoinObjectSerDeContext
argument_list|(
name|keySerializer
argument_list|,
literal|false
argument_list|)
decl_stmt|;
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
name|MapJoinObjectSerDeContext
name|valueContext
init|=
operator|new
name|MapJoinObjectSerDeContext
argument_list|(
name|valueSerDe
argument_list|,
name|hasFilter
argument_list|(
name|pos
argument_list|)
argument_list|)
decl_stmt|;
name|mapJoinTableSerdes
index|[
name|pos
index|]
operator|=
operator|new
name|MapJoinTableContainerSerDe
argument_list|(
name|keyContext
argument_list|,
name|valueContext
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
name|loader
operator|.
name|load
argument_list|(
name|this
operator|.
name|getExecContext
argument_list|()
argument_list|,
name|hconf
argument_list|,
name|this
operator|.
name|getConf
argument_list|()
argument_list|,
name|posBigTable
argument_list|,
name|mapJoinTables
argument_list|,
name|mapJoinTableSerdes
argument_list|)
expr_stmt|;
name|cache
operator|.
name|cache
argument_list|(
name|tableKey
argument_list|,
name|mapJoinTables
argument_list|)
expr_stmt|;
name|cache
operator|.
name|cache
argument_list|(
name|serdeKey
argument_list|,
name|mapJoinTableSerdes
argument_list|)
expr_stmt|;
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
name|key
operator|=
name|JoinUtil
operator|.
name|computeMapJoinKeys
argument_list|(
name|key
argument_list|,
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
expr_stmt|;
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
name|MapJoinRowContainer
name|rowContainer
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
comment|// there is no join-value or join-key has all null elements
if|if
condition|(
name|rowContainer
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
name|storage
index|[
name|pos
index|]
operator|=
name|rowContainer
operator|.
name|copy
argument_list|()
expr_stmt|;
name|aliasFilterTags
index|[
name|pos
index|]
operator|=
name|rowContainer
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
name|MapJoinTableContainer
name|tableContainer
range|:
name|mapJoinTables
control|)
block|{
if|if
condition|(
name|tableContainer
operator|!=
literal|null
condition|)
block|{
name|tableContainer
operator|.
name|clear
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

