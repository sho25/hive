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
name|File
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
name|ObjectInspectorFactory
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
name|StandardStructObjectInspector
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
name|StandardStructObjectInspector
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
comment|// from abstract map join operator
comment|/**    * The expressions for join inputs's join keys.    */
specifier|protected
specifier|transient
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
argument_list|>
name|joinKeys
decl_stmt|;
comment|/**    * The ObjectInspectors for the join inputs's join keys.    */
specifier|protected
specifier|transient
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|>
name|joinKeysObjectInspectors
decl_stmt|;
comment|/**    * The standard ObjectInspectors for the join inputs's join keys.    */
specifier|protected
specifier|transient
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|>
name|joinKeysStandardObjectInspectors
decl_stmt|;
specifier|protected
specifier|transient
name|int
name|posBigTableTag
init|=
operator|-
literal|1
decl_stmt|;
comment|// one of the tables that is not in memory
specifier|protected
specifier|transient
name|int
name|posBigTableAlias
init|=
operator|-
literal|1
decl_stmt|;
comment|// one of the tables that is not in memory
specifier|transient
name|int
name|mapJoinRowsKey
decl_stmt|;
comment|// rows for a given key
specifier|protected
specifier|transient
name|RowContainer
argument_list|<
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|>
name|emptyList
init|=
literal|null
decl_stmt|;
specifier|transient
name|int
name|numMapRowsRead
decl_stmt|;
specifier|protected
specifier|transient
name|int
name|totalSz
decl_stmt|;
comment|// total size of the composite object
specifier|transient
name|boolean
name|firstRow
decl_stmt|;
comment|/**    * The filters for join    */
specifier|protected
specifier|transient
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
argument_list|>
name|joinFilters
decl_stmt|;
specifier|protected
specifier|transient
name|int
name|numAliases
decl_stmt|;
comment|// number of aliases
comment|/**    * The expressions for join outputs.    */
specifier|protected
specifier|transient
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
argument_list|>
name|joinValues
decl_stmt|;
comment|/**    * The ObjectInspectors for the join inputs.    */
specifier|protected
specifier|transient
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|>
name|joinValuesObjectInspectors
decl_stmt|;
comment|/**    * The ObjectInspectors for join filters.    */
specifier|protected
specifier|transient
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|>
name|joinFilterObjectInspectors
decl_stmt|;
comment|/**    * The standard ObjectInspectors for the join inputs.    */
specifier|protected
specifier|transient
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|>
name|joinValuesStandardObjectInspectors
decl_stmt|;
specifier|protected
specifier|transient
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|>
name|rowContainerStandardObjectInspectors
decl_stmt|;
specifier|protected
specifier|transient
name|Byte
index|[]
name|order
decl_stmt|;
comment|// order in which the results should
name|Configuration
name|hconf
decl_stmt|;
specifier|protected
specifier|transient
name|Byte
name|alias
decl_stmt|;
specifier|protected
specifier|transient
name|Map
argument_list|<
name|Byte
argument_list|,
name|TableDesc
argument_list|>
name|spillTableDesc
decl_stmt|;
comment|// spill tables are
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
specifier|protected
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
specifier|protected
specifier|transient
name|LogHelper
name|console
decl_stmt|;
specifier|private
name|long
name|hashTableScale
decl_stmt|;
specifier|private
name|boolean
name|isAbort
init|=
literal|false
decl_stmt|;
specifier|public
specifier|static
class|class
name|HashTableSinkObjectCtx
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
name|HashTableSinkObjectCtx
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
specifier|final
name|int
name|metadataKeyTag
init|=
operator|-
literal|1
decl_stmt|;
specifier|transient
name|int
index|[]
name|metadataValueTag
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
name|numMapRowsRead
operator|=
literal|0
expr_stmt|;
name|firstRow
operator|=
literal|true
expr_stmt|;
comment|// for small tables only; so get the big table position first
name|posBigTableTag
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
name|posBigTableAlias
operator|=
name|order
index|[
name|posBigTableTag
index|]
expr_stmt|;
comment|// initialize some variables, which used to be initialized in CommonJoinOperator
name|numAliases
operator|=
name|conf
operator|.
name|getExprs
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
name|this
operator|.
name|hconf
operator|=
name|hconf
expr_stmt|;
name|totalSz
operator|=
literal|0
expr_stmt|;
name|noOuterJoin
operator|=
name|conf
operator|.
name|isNoOuterJoin
argument_list|()
expr_stmt|;
comment|// process join keys
name|joinKeys
operator|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
argument_list|>
argument_list|()
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
name|order
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
argument_list|)
expr_stmt|;
name|joinKeysStandardObjectInspectors
operator|=
name|JoinUtil
operator|.
name|getStandardObjectInspectors
argument_list|(
name|joinKeysObjectInspectors
argument_list|,
name|posBigTableAlias
argument_list|)
expr_stmt|;
comment|// process join values
name|joinValues
operator|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
argument_list|>
argument_list|()
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
name|order
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
argument_list|)
expr_stmt|;
name|joinValuesStandardObjectInspectors
operator|=
name|JoinUtil
operator|.
name|getStandardObjectInspectors
argument_list|(
name|joinValuesObjectInspectors
argument_list|,
name|posBigTableAlias
argument_list|)
expr_stmt|;
comment|// process join filters
name|joinFilters
operator|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ExprNodeEvaluator
argument_list|>
argument_list|>
argument_list|()
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
name|order
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
argument_list|)
expr_stmt|;
if|if
condition|(
name|noOuterJoin
condition|)
block|{
name|rowContainerStandardObjectInspectors
operator|=
name|joinValuesStandardObjectInspectors
expr_stmt|;
block|}
else|else
block|{
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|>
name|rowContainerObjectInspectors
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|>
argument_list|()
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
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|rcOIs
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|rcOIs
operator|.
name|addAll
argument_list|(
name|joinValuesObjectInspectors
operator|.
name|get
argument_list|(
name|alias
argument_list|)
argument_list|)
expr_stmt|;
comment|// for each alias, add object inspector for boolean as the last element
name|rcOIs
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableBooleanObjectInspector
argument_list|)
expr_stmt|;
name|rowContainerObjectInspectors
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|rcOIs
argument_list|)
expr_stmt|;
block|}
name|rowContainerStandardObjectInspectors
operator|=
name|getStandardObjectInspectors
argument_list|(
name|rowContainerObjectInspectors
argument_list|)
expr_stmt|;
block|}
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
name|AbstractMapJoinKey
argument_list|,
name|MapJoinObjectValue
argument_list|>
argument_list|>
argument_list|()
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
name|float
name|hashTableMaxMemoryUsage
init|=
name|this
operator|.
name|getConf
argument_list|()
operator|.
name|getHashtableMemoryUsage
argument_list|()
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
comment|// initialize the hash tables for other tables
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
name|posBigTableTag
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
argument_list|(
name|hashTableThreshold
argument_list|,
name|hashTableLoadFactor
argument_list|,
name|hashTableMaxMemoryUsage
argument_list|)
decl_stmt|;
name|mapJoinTables
operator|.
name|put
argument_list|(
name|pos
argument_list|,
name|hashTable
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
specifier|static
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|>
name|getStandardObjectInspectors
parameter_list|(
name|Map
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|>
name|aliasToObjectInspectors
parameter_list|)
block|{
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|>
name|result
init|=
operator|new
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|Byte
argument_list|,
name|List
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|>
name|oiEntry
range|:
name|aliasToObjectInspectors
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Byte
name|alias
init|=
name|oiEntry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|oiList
init|=
name|oiEntry
operator|.
name|getValue
argument_list|()
decl_stmt|;
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
operator|.
name|put
argument_list|(
name|alias
argument_list|,
name|fieldOIList
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|void
name|setKeyMetaData
parameter_list|()
throws|throws
name|SerDeException
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
name|clear
argument_list|()
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
comment|// let the mapJoinOp process these small tables
try|try
block|{
if|if
condition|(
name|firstRow
condition|)
block|{
comment|// generate the map metadata
name|setKeyMetaData
argument_list|()
expr_stmt|;
name|firstRow
operator|=
literal|false
expr_stmt|;
block|}
name|alias
operator|=
name|order
index|[
name|tag
index|]
expr_stmt|;
comment|// alias = (byte)tag;
comment|// compute keys and values as StandardObjects
name|AbstractMapJoinKey
name|keyMap
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
name|Object
index|[]
name|value
init|=
name|JoinUtil
operator|.
name|computeMapJoinValues
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
name|HashMapWrapper
argument_list|<
name|AbstractMapJoinKey
argument_list|,
name|MapJoinObjectValue
argument_list|>
name|hashTable
init|=
name|mapJoinTables
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|tag
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
name|MapJoinRowContainer
argument_list|<
name|Object
index|[]
argument_list|>
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
name|res
operator|=
operator|new
name|MapJoinRowContainer
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|()
expr_stmt|;
name|res
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
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
name|order
index|[
name|tag
index|]
expr_stmt|;
name|setValueMetaData
argument_list|(
name|tag
argument_list|)
expr_stmt|;
block|}
comment|// Construct externalizable objects for key and value
if|if
condition|(
name|needNewKey
condition|)
block|{
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
name|isAbort
operator|=
name|hashTable
operator|.
name|isAbort
argument_list|(
name|rowNumber
argument_list|,
name|console
argument_list|)
expr_stmt|;
if|if
condition|(
name|isAbort
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"RunOutOfMeomoryUsage"
argument_list|)
throw|;
block|}
block|}
name|hashTable
operator|.
name|put
argument_list|(
name|keyMap
argument_list|,
name|valueObj
argument_list|)
expr_stmt|;
block|}
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
name|void
name|setValueMetaData
parameter_list|(
name|int
name|tag
parameter_list|)
throws|throws
name|SerDeException
block|{
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
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|newFields
init|=
name|rowContainerStandardObjectInspectors
operator|.
name|get
argument_list|(
operator|(
name|Byte
operator|)
name|alias
argument_list|)
decl_stmt|;
name|int
name|length
init|=
name|newFields
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|newNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|length
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
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|tmp
init|=
operator|new
name|String
argument_list|(
literal|"tmp_"
operator|+
name|i
argument_list|)
decl_stmt|;
name|newNames
operator|.
name|add
argument_list|(
name|tmp
argument_list|)
expr_stmt|;
block|}
name|StandardStructObjectInspector
name|standardOI
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|newNames
argument_list|,
name|newFields
argument_list|)
decl_stmt|;
name|MapJoinMetaData
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
name|HashTableSinkObjectCtx
argument_list|(
name|standardOI
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
comment|// get tmp file URI
name|String
name|tmpURI
init|=
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
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Get TMP URI: "
operator|+
name|tmpURI
argument_list|)
expr_stmt|;
name|long
name|fileLength
decl_stmt|;
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
name|hashTables
range|:
name|mapJoinTables
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// get the key and value
name|Byte
name|tag
init|=
name|hashTables
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
name|hashTable
init|=
name|hashTables
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|// get current input file name
name|String
name|bigBucketFileName
init|=
name|this
operator|.
name|getExecContext
argument_list|()
operator|.
name|getCurrentBigBucketFile
argument_list|()
decl_stmt|;
if|if
condition|(
name|bigBucketFileName
operator|==
literal|null
operator|||
name|bigBucketFileName
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|bigBucketFileName
operator|=
literal|"-"
expr_stmt|;
block|}
comment|// get the tmp URI path; it will be a hdfs path if not local mode
name|String
name|tmpURIPath
init|=
name|Utilities
operator|.
name|generatePath
argument_list|(
name|tmpURI
argument_list|,
name|conf
operator|.
name|getDumpFilePrefix
argument_list|()
argument_list|,
name|tag
argument_list|,
name|bigBucketFileName
argument_list|)
decl_stmt|;
name|hashTable
operator|.
name|isAbort
argument_list|(
name|rowNumber
argument_list|,
name|console
argument_list|)
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
literal|"\tDump the hashtable into file: "
operator|+
name|tmpURIPath
argument_list|)
expr_stmt|;
comment|// get the hashtable file and path
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|tmpURIPath
argument_list|)
decl_stmt|;
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
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|.
name|create
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|fileLength
operator|=
name|hashTable
operator|.
name|flushMemoryCacheToPersistent
argument_list|(
name|file
argument_list|)
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
name|tmpURIPath
operator|+
literal|" File size: "
operator|+
name|fileLength
argument_list|)
expr_stmt|;
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
literal|"Generate Hashtable error"
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
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

