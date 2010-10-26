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
name|JDBMSinkDesc
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
name|util
operator|.
name|JoinUtil
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
name|JDBMSinkOperator
extends|extends
name|TerminalOperator
argument_list|<
name|JDBMSinkDesc
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
name|JDBMSinkOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|//from abstract map join operator
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
specifier|private
name|boolean
name|smallTablesOnly
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
name|MapJoinObjectKey
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
specifier|public
specifier|static
class|class
name|JDBMSinkObjectCtx
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
name|JDBMSinkObjectCtx
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
literal|"Mapside join size exceeds hive.mapjoin.maxsize. "
operator|+
literal|"Please increase that or remove the mapjoin hint."
block|}
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
specifier|public
name|JDBMSinkOperator
parameter_list|()
block|{
comment|//super();
block|}
specifier|public
name|JDBMSinkOperator
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
name|JDBMSinkDesc
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
name|numMapRowsRead
operator|=
literal|0
expr_stmt|;
name|firstRow
operator|=
literal|true
expr_stmt|;
comment|//for small tables only; so get the big table position first
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
comment|//initialize some variables, which used to be initialized in CommonJoinOperator
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
comment|//process join keys
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
comment|//process join values
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
comment|//process join filters
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
name|joinValues
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
comment|/*    * This operator only process small tables    * Read the key/value pairs    * Load them into hashtable    */
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
comment|//let the mapJoinOp process these small tables
try|try
block|{
name|alias
operator|=
name|order
index|[
name|tag
index|]
expr_stmt|;
comment|//alias = (byte)tag;
comment|// compute keys and values as StandardObjects
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|key
init|=
name|JoinUtil
operator|.
name|computeKeys
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
if|if
condition|(
name|firstRow
condition|)
block|{
name|metadataKeyTag
operator|=
operator|-
literal|1
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
name|JDBMSinkObjectCtx
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
operator|(
name|byte
operator|)
name|tag
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
name|JoinUtil
operator|.
name|getRowContainer
argument_list|(
name|hconf
argument_list|,
name|rowContainerStandardObjectInspectors
operator|.
name|get
argument_list|(
operator|(
name|byte
operator|)
name|tag
argument_list|)
argument_list|,
name|order
index|[
name|tag
index|]
argument_list|,
name|bucketSize
argument_list|,
name|spillTableDesc
argument_list|,
name|conf
argument_list|,
name|noOuterJoin
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
name|order
index|[
name|tag
index|]
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
name|JDBMSinkObjectCtx
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
comment|//valueObj.setConf(hconf);
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
comment|/*    * Flush the hashtable into jdbm file    * Load this jdbm file into HDFS only    */
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
comment|//get tmp file URI
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
name|MapJoinObjectKey
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
comment|//get the key and value
name|Byte
name|tag
init|=
name|hashTables
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|HashMapWrapper
name|hashTable
init|=
name|hashTables
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|//get the jdbm file and path
name|String
name|jdbmFile
init|=
name|hashTable
operator|.
name|flushMemoryCacheToPersistent
argument_list|()
decl_stmt|;
name|Path
name|localPath
init|=
operator|new
name|Path
argument_list|(
name|jdbmFile
argument_list|)
decl_stmt|;
comment|//get current input file name
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
comment|//get the tmp URI path; it will be a hdfs path if not local mode
name|Path
name|tmpURIPath
init|=
operator|new
name|Path
argument_list|(
name|tmpURI
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
literal|"-"
operator|+
name|tag
operator|+
literal|"-"
operator|+
name|bigBucketFileName
operator|+
literal|".jdbm"
argument_list|)
decl_stmt|;
comment|//upload jdbm file to this HDFS
name|FileSystem
name|fs
init|=
name|tmpURIPath
operator|.
name|getFileSystem
argument_list|(
name|this
operator|.
name|getExecContext
argument_list|()
operator|.
name|getJc
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|.
name|copyFromLocalFile
argument_list|(
name|localPath
argument_list|,
name|tmpURIPath
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Upload 1 JDBM File to: "
operator|+
name|tmpURIPath
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Upload 1 JDBM File to: "
operator|+
name|tmpURIPath
argument_list|)
expr_stmt|;
comment|//remove the original jdbm tmp file
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
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Copy local file to HDFS error"
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
literal|"JDBMSINK"
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
name|JDBMSINK
return|;
block|}
specifier|private
name|void
name|getPersistentFilePath
parameter_list|(
name|Map
argument_list|<
name|Byte
argument_list|,
name|Path
argument_list|>
name|paths
parameter_list|)
throws|throws
name|HiveException
block|{
name|Map
argument_list|<
name|Byte
argument_list|,
name|Path
argument_list|>
name|jdbmFilePaths
init|=
name|paths
decl_stmt|;
try|try
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
name|Map
operator|.
name|Entry
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
name|hashTables
range|:
name|mapJoinTables
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|//hashTable.close();
name|Byte
name|key
init|=
name|hashTables
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|HashMapWrapper
name|hashTable
init|=
name|hashTables
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|//get the jdbm file and path
name|String
name|jdbmFile
init|=
name|hashTable
operator|.
name|flushMemoryCacheToPersistent
argument_list|()
decl_stmt|;
name|Path
name|localPath
init|=
operator|new
name|Path
argument_list|(
name|jdbmFile
argument_list|)
decl_stmt|;
comment|//insert into map
name|jdbmFilePaths
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|localPath
argument_list|)
expr_stmt|;
block|}
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
name|fatal
argument_list|(
literal|"Get local JDBM file error"
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

