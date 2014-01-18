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
name|BufferedOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ObjectOutputStream
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
name|List
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
name|mapjoin
operator|.
name|MapJoinMemoryExhaustionHandler
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
name|HashTableSinkDesc
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
name|ql
operator|.
name|session
operator|.
name|SessionState
operator|.
name|LogHelper
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
name|serde2
operator|.
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorFactory
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

begin_class
specifier|public
class|class
name|HashTableSinkOperator
extends|extends
name|TerminalOperator
argument_list|<
name|HashTableSinkDesc
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
name|HashTableSinkOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * The expressions for join inputs's join keys.    */
specifier|private
specifier|transient
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
index|[]
name|joinKeys
decl_stmt|;
comment|/**    * The ObjectInspectors for the join inputs's join keys.    */
specifier|private
specifier|transient
name|List
argument_list|<
name|ObjectInspector
argument_list|>
index|[]
name|joinKeysObjectInspectors
decl_stmt|;
specifier|private
specifier|transient
name|int
name|posBigTableAlias
init|=
operator|-
literal|1
decl_stmt|;
comment|// one of the tables that is not in memory
comment|/**    * The filters for join    */
specifier|private
specifier|transient
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
index|[]
name|joinFilters
decl_stmt|;
specifier|private
specifier|transient
name|int
index|[]
index|[]
name|filterMaps
decl_stmt|;
comment|/**    * The expressions for join outputs.    */
specifier|private
specifier|transient
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
index|[]
name|joinValues
decl_stmt|;
comment|/**    * The ObjectInspectors for the join inputs.    */
specifier|private
specifier|transient
name|List
argument_list|<
name|ObjectInspector
argument_list|>
index|[]
name|joinValuesObjectInspectors
decl_stmt|;
comment|/**    * The ObjectInspectors for join filters.    */
specifier|private
specifier|transient
name|List
argument_list|<
name|ObjectInspector
argument_list|>
index|[]
name|joinFilterObjectInspectors
decl_stmt|;
specifier|private
specifier|transient
name|Byte
index|[]
name|order
decl_stmt|;
comment|// order in which the results should
specifier|private
name|Configuration
name|hconf
decl_stmt|;
specifier|private
specifier|transient
name|Byte
name|alias
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
specifier|static
specifier|final
name|Object
index|[]
name|EMPTY_OBJECT_ARRAY
init|=
operator|new
name|Object
index|[
literal|0
index|]
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|MapJoinRowContainer
name|EMPTY_ROW_CONTAINER
init|=
operator|new
name|MapJoinRowContainer
argument_list|()
decl_stmt|;
static|static
block|{
name|EMPTY_ROW_CONTAINER
operator|.
name|add
argument_list|(
name|EMPTY_OBJECT_ARRAY
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|transient
name|boolean
name|noOuterJoin
decl_stmt|;
specifier|private
name|long
name|rowNumber
init|=
literal|0
decl_stmt|;
specifier|private
specifier|transient
name|LogHelper
name|console
decl_stmt|;
specifier|private
name|long
name|hashTableScale
decl_stmt|;
specifier|private
name|MapJoinMemoryExhaustionHandler
name|memoryExhaustionHandler
decl_stmt|;
specifier|public
name|HashTableSinkOperator
parameter_list|()
block|{   }
specifier|public
name|HashTableSinkOperator
parameter_list|(
name|MapJoinOperator
name|mjop
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
operator|new
name|HashTableSinkDesc
argument_list|(
name|mjop
operator|.
name|getConf
argument_list|()
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
name|boolean
name|isSilent
init|=
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESESSIONSILENT
argument_list|)
decl_stmt|;
name|console
operator|=
operator|new
name|LogHelper
argument_list|(
name|LOG
argument_list|,
name|isSilent
argument_list|)
expr_stmt|;
name|memoryExhaustionHandler
operator|=
operator|new
name|MapJoinMemoryExhaustionHandler
argument_list|(
name|console
argument_list|,
name|conf
operator|.
name|getHashtableMemoryUsage
argument_list|()
argument_list|)
expr_stmt|;
comment|// for small tables only; so get the big table position first
name|posBigTableAlias
operator|=
name|conf
operator|.
name|getPosBigTable
argument_list|()
expr_stmt|;
name|order
operator|=
name|conf
operator|.
name|getTagOrder
argument_list|()
expr_stmt|;
comment|// initialize some variables, which used to be initialized in CommonJoinOperator
name|this
operator|.
name|hconf
operator|=
name|hconf
expr_stmt|;
name|noOuterJoin
operator|=
name|conf
operator|.
name|isNoOuterJoin
argument_list|()
expr_stmt|;
name|filterMaps
operator|=
name|conf
operator|.
name|getFilterMap
argument_list|()
expr_stmt|;
name|int
name|tagLen
init|=
name|conf
operator|.
name|getTagLength
argument_list|()
decl_stmt|;
comment|// process join keys
name|joinKeys
operator|=
operator|new
name|List
index|[
name|tagLen
index|]
expr_stmt|;
name|JoinUtil
operator|.
name|populateJoinKeyValue
argument_list|(
name|joinKeys
argument_list|,
name|conf
operator|.
name|getKeys
argument_list|()
argument_list|,
name|posBigTableAlias
argument_list|)
expr_stmt|;
name|joinKeysObjectInspectors
operator|=
name|JoinUtil
operator|.
name|getObjectInspectorsFromEvaluators
argument_list|(
name|joinKeys
argument_list|,
name|inputObjInspectors
argument_list|,
name|posBigTableAlias
argument_list|,
name|tagLen
argument_list|)
expr_stmt|;
comment|// process join values
name|joinValues
operator|=
operator|new
name|List
index|[
name|tagLen
index|]
expr_stmt|;
name|JoinUtil
operator|.
name|populateJoinKeyValue
argument_list|(
name|joinValues
argument_list|,
name|conf
operator|.
name|getExprs
argument_list|()
argument_list|,
name|posBigTableAlias
argument_list|)
expr_stmt|;
name|joinValuesObjectInspectors
operator|=
name|JoinUtil
operator|.
name|getObjectInspectorsFromEvaluators
argument_list|(
name|joinValues
argument_list|,
name|inputObjInspectors
argument_list|,
name|posBigTableAlias
argument_list|,
name|tagLen
argument_list|)
expr_stmt|;
comment|// process join filters
name|joinFilters
operator|=
operator|new
name|List
index|[
name|tagLen
index|]
expr_stmt|;
name|JoinUtil
operator|.
name|populateJoinKeyValue
argument_list|(
name|joinFilters
argument_list|,
name|conf
operator|.
name|getFilters
argument_list|()
argument_list|,
name|posBigTableAlias
argument_list|)
expr_stmt|;
name|joinFilterObjectInspectors
operator|=
name|JoinUtil
operator|.
name|getObjectInspectorsFromEvaluators
argument_list|(
name|joinFilters
argument_list|,
name|inputObjInspectors
argument_list|,
name|posBigTableAlias
argument_list|,
name|tagLen
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|noOuterJoin
condition|)
block|{
name|List
argument_list|<
name|ObjectInspector
argument_list|>
index|[]
name|rowContainerObjectInspectors
init|=
operator|new
name|List
index|[
name|tagLen
index|]
decl_stmt|;
for|for
control|(
name|Byte
name|alias
range|:
name|order
control|)
block|{
if|if
condition|(
name|alias
operator|==
name|posBigTableAlias
condition|)
block|{
continue|continue;
block|}
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|rcOIs
init|=
name|joinValuesObjectInspectors
index|[
name|alias
index|]
decl_stmt|;
if|if
condition|(
name|filterMaps
operator|!=
literal|null
operator|&&
name|filterMaps
index|[
name|alias
index|]
operator|!=
literal|null
condition|)
block|{
comment|// for each alias, add object inspector for filter tag as the last element
name|rcOIs
operator|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|rcOIs
argument_list|)
expr_stmt|;
name|rcOIs
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableShortObjectInspector
argument_list|)
expr_stmt|;
block|}
name|rowContainerObjectInspectors
index|[
name|alias
index|]
operator|=
name|rcOIs
expr_stmt|;
block|}
block|}
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
name|int
name|hashTableThreshold
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
name|HIVEHASHTABLETHRESHOLD
argument_list|)
decl_stmt|;
name|float
name|hashTableLoadFactor
init|=
name|HiveConf
operator|.
name|getFloatVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHASHTABLELOADFACTOR
argument_list|)
decl_stmt|;
name|hashTableScale
operator|=
name|HiveConf
operator|.
name|getLongVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEHASHTABLESCALE
argument_list|)
expr_stmt|;
if|if
condition|(
name|hashTableScale
operator|<=
literal|0
condition|)
block|{
name|hashTableScale
operator|=
literal|1
expr_stmt|;
block|}
try|try
block|{
name|TableDesc
name|keyTableDesc
init|=
name|conf
operator|.
name|getKeyTblDesc
argument_list|()
decl_stmt|;
name|SerDe
name|keySerde
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
name|keySerde
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
name|keySerde
argument_list|,
literal|false
argument_list|)
decl_stmt|;
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
operator|==
name|posBigTableAlias
condition|)
block|{
continue|continue;
block|}
name|mapJoinTables
index|[
name|pos
index|]
operator|=
operator|new
name|HashMapWrapper
argument_list|(
name|hashTableThreshold
argument_list|,
name|hashTableLoadFactor
argument_list|)
expr_stmt|;
name|TableDesc
name|valueTableDesc
init|=
name|conf
operator|.
name|getValueTblFilteredDescs
argument_list|()
operator|.
name|get
argument_list|(
name|pos
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
argument_list|)
expr_stmt|;
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
specifier|private
specifier|static
name|List
argument_list|<
name|ObjectInspector
argument_list|>
index|[]
name|getStandardObjectInspectors
parameter_list|(
name|List
argument_list|<
name|ObjectInspector
argument_list|>
index|[]
name|aliasToObjectInspectors
parameter_list|,
name|int
name|maxTag
parameter_list|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|List
argument_list|<
name|ObjectInspector
argument_list|>
index|[]
name|result
init|=
operator|new
name|List
index|[
name|maxTag
index|]
decl_stmt|;
for|for
control|(
name|byte
name|alias
init|=
literal|0
init|;
name|alias
operator|<
name|aliasToObjectInspectors
operator|.
name|length
condition|;
name|alias
operator|++
control|)
block|{
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|oiList
init|=
name|aliasToObjectInspectors
index|[
name|alias
index|]
decl_stmt|;
if|if
condition|(
name|oiList
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldOIList
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|oiList
operator|.
name|size
argument_list|()
argument_list|)
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
name|oiList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|fieldOIList
operator|.
name|add
argument_list|(
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|oiList
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|WRITABLE
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
index|[
name|alias
index|]
operator|=
name|fieldOIList
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/*    * This operator only process small tables Read the key/value pairs Load them into hashtable    */
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
name|alias
operator|=
operator|(
name|byte
operator|)
name|tag
expr_stmt|;
comment|// compute keys and values as StandardObjects
name|MapJoinKey
name|key
init|=
name|JoinUtil
operator|.
name|computeMapJoinKeys
argument_list|(
literal|null
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
decl_stmt|;
name|Object
index|[]
name|value
init|=
name|EMPTY_OBJECT_ARRAY
decl_stmt|;
if|if
condition|(
operator|(
name|hasFilter
argument_list|(
name|alias
argument_list|)
operator|&&
name|filterMaps
index|[
name|alias
index|]
operator|.
name|length
operator|>
literal|0
operator|)
operator|||
name|joinValues
index|[
name|alias
index|]
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|value
operator|=
name|JoinUtil
operator|.
name|computeMapJoinValues
argument_list|(
name|row
argument_list|,
name|joinValues
index|[
name|alias
index|]
argument_list|,
name|joinValuesObjectInspectors
index|[
name|alias
index|]
argument_list|,
name|joinFilters
index|[
name|alias
index|]
argument_list|,
name|joinFilterObjectInspectors
index|[
name|alias
index|]
argument_list|,
name|filterMaps
operator|==
literal|null
condition|?
literal|null
else|:
name|filterMaps
index|[
name|alias
index|]
argument_list|)
expr_stmt|;
block|}
name|MapJoinTableContainer
name|tableContainer
init|=
name|mapJoinTables
index|[
name|alias
index|]
decl_stmt|;
name|MapJoinRowContainer
name|rowContainer
init|=
name|tableContainer
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|rowContainer
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|value
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
name|rowContainer
operator|=
operator|new
name|MapJoinRowContainer
argument_list|()
expr_stmt|;
name|rowContainer
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rowContainer
operator|=
name|EMPTY_ROW_CONTAINER
expr_stmt|;
block|}
name|rowNumber
operator|++
expr_stmt|;
if|if
condition|(
name|rowNumber
operator|>
name|hashTableScale
operator|&&
name|rowNumber
operator|%
name|hashTableScale
operator|==
literal|0
condition|)
block|{
name|memoryExhaustionHandler
operator|.
name|checkMemoryStatus
argument_list|(
name|tableContainer
operator|.
name|size
argument_list|()
argument_list|,
name|rowNumber
argument_list|)
expr_stmt|;
block|}
name|tableContainer
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|rowContainer
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|rowContainer
operator|==
name|EMPTY_ROW_CONTAINER
condition|)
block|{
name|rowContainer
operator|=
name|rowContainer
operator|.
name|copy
argument_list|()
expr_stmt|;
name|rowContainer
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|tableContainer
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|rowContainer
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rowContainer
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|hasFilter
parameter_list|(
name|int
name|alias
parameter_list|)
block|{
return|return
name|filterMaps
operator|!=
literal|null
operator|&&
name|filterMaps
index|[
name|alias
index|]
operator|!=
literal|null
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
try|try
block|{
if|if
condition|(
name|mapJoinTables
operator|!=
literal|null
condition|)
block|{
comment|// get tmp path
name|Path
name|tmpPath
init|=
name|this
operator|.
name|getExecContext
argument_list|()
operator|.
name|getLocalWork
argument_list|()
operator|.
name|getTmpPath
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Temp URI for side table: "
operator|+
name|tmpPath
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
name|tag
init|=
literal|0
init|;
name|tag
operator|<
name|mapJoinTables
operator|.
name|length
condition|;
name|tag
operator|++
control|)
block|{
comment|// get the key and value
name|MapJoinTableContainer
name|tableContainer
init|=
name|mapJoinTables
index|[
name|tag
index|]
decl_stmt|;
if|if
condition|(
name|tableContainer
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
comment|// get current input file name
name|String
name|bigBucketFileName
init|=
name|getExecContext
argument_list|()
operator|.
name|getCurrentBigBucketFile
argument_list|()
decl_stmt|;
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
name|bigBucketFileName
argument_list|)
decl_stmt|;
comment|// get the tmp URI path; it will be a hdfs path if not local mode
name|String
name|dumpFilePrefix
init|=
name|conf
operator|.
name|getDumpFilePrefix
argument_list|()
decl_stmt|;
name|Path
name|path
init|=
name|Utilities
operator|.
name|generatePath
argument_list|(
name|tmpPath
argument_list|,
name|dumpFilePrefix
argument_list|,
name|tag
argument_list|,
name|fileName
argument_list|)
decl_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
name|Utilities
operator|.
name|now
argument_list|()
operator|+
literal|"\tDump the side-table into file: "
operator|+
name|path
argument_list|)
expr_stmt|;
comment|// get the hashtable file and path
name|FileSystem
name|fs
init|=
name|path
operator|.
name|getFileSystem
argument_list|(
name|hconf
argument_list|)
decl_stmt|;
name|ObjectOutputStream
name|out
init|=
operator|new
name|ObjectOutputStream
argument_list|(
operator|new
name|BufferedOutputStream
argument_list|(
name|fs
operator|.
name|create
argument_list|(
name|path
argument_list|)
argument_list|,
literal|4096
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|mapJoinTableSerdes
index|[
name|tag
index|]
operator|.
name|persist
argument_list|(
name|out
argument_list|,
name|tableContainer
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|tableContainer
operator|.
name|clear
argument_list|()
expr_stmt|;
name|console
operator|.
name|printInfo
argument_list|(
name|Utilities
operator|.
name|now
argument_list|()
operator|+
literal|"\tUpload 1 File to: "
operator|+
name|path
argument_list|)
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
literal|"Error generating side-table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
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
literal|"HASHTABLESINK"
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
name|HASHTABLESINK
return|;
block|}
block|}
end_class

end_unit

