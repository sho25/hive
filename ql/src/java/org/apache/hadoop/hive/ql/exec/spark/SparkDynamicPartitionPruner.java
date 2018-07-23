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
name|exec
operator|.
name|spark
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
name|ObjectInputStream
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
name|Collections
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
name|HashSet
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
name|java
operator|.
name|util
operator|.
name|Set
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
name|base
operator|.
name|Preconditions
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
name|log
operator|.
name|PerfLogger
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
name|optimizer
operator|.
name|spark
operator|.
name|SparkPartitionPruningSinkDesc
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
name|ql
operator|.
name|exec
operator|.
name|ExprNodeEvaluator
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
name|ExprNodeEvaluatorFactory
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
name|ExprNodeDesc
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
name|MapWork
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
name|PartitionDesc
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
name|serde2
operator|.
name|Deserializer
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
name|ObjectInspectorConverters
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
name|StructField
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
name|StructObjectInspector
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
name|hive
operator|.
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
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
name|io
operator|.
name|BytesWritable
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
name|JobConf
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
comment|/**  * The spark version of DynamicPartitionPruner.  */
end_comment

begin_class
specifier|public
class|class
name|SparkDynamicPartitionPruner
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
name|SparkDynamicPartitionPruner
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|SparkDynamicPartitionPruner
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|PerfLogger
name|perfLogger
init|=
name|SessionState
operator|.
name|getPerfLogger
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|SourceInfo
argument_list|>
argument_list|>
name|sourceInfoMap
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|SourceInfo
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|BytesWritable
name|writable
init|=
operator|new
name|BytesWritable
argument_list|()
decl_stmt|;
specifier|public
name|void
name|prune
parameter_list|(
name|MapWork
name|work
parameter_list|,
name|JobConf
name|jobConf
parameter_list|)
throws|throws
name|HiveException
throws|,
name|SerDeException
block|{
name|sourceInfoMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|initialize
argument_list|(
name|work
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
if|if
condition|(
name|sourceInfoMap
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// Nothing to prune for this MapWork
return|return;
block|}
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_DYNAMICALLY_PRUNE_PARTITIONS
operator|+
name|work
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|processFiles
argument_list|(
name|work
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
name|prunePartitions
argument_list|(
name|work
argument_list|)
expr_stmt|;
name|perfLogger
operator|.
name|PerfLogBegin
argument_list|(
name|CLASS_NAME
argument_list|,
name|PerfLogger
operator|.
name|SPARK_DYNAMICALLY_PRUNE_PARTITIONS
operator|+
name|work
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|initialize
parameter_list|(
name|MapWork
name|work
parameter_list|,
name|JobConf
name|jobConf
parameter_list|)
throws|throws
name|SerDeException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|SourceInfo
argument_list|>
name|columnMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|SourceInfo
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|sourceWorkIds
init|=
name|work
operator|.
name|getEventSourceTableDescMap
argument_list|()
operator|.
name|keySet
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|id
range|:
name|sourceWorkIds
control|)
block|{
name|List
argument_list|<
name|TableDesc
argument_list|>
name|tables
init|=
name|work
operator|.
name|getEventSourceTableDescMap
argument_list|()
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
comment|// Real column name - on which the operation is being performed
name|List
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
name|work
operator|.
name|getEventSourceColumnNameMap
argument_list|()
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
comment|// Column type
name|List
argument_list|<
name|String
argument_list|>
name|columnTypes
init|=
name|work
operator|.
name|getEventSourceColumnTypeMap
argument_list|()
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|partKeyExprs
init|=
name|work
operator|.
name|getEventSourcePartKeyExprMap
argument_list|()
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|String
argument_list|>
name|cit
init|=
name|columnNames
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|String
argument_list|>
name|typit
init|=
name|columnTypes
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|ExprNodeDesc
argument_list|>
name|pit
init|=
name|partKeyExprs
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|TableDesc
name|t
range|:
name|tables
control|)
block|{
name|String
name|columnName
init|=
name|cit
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|columnType
init|=
name|typit
operator|.
name|next
argument_list|()
decl_stmt|;
name|ExprNodeDesc
name|partKeyExpr
init|=
name|pit
operator|.
name|next
argument_list|()
decl_stmt|;
name|SourceInfo
name|si
init|=
operator|new
name|SourceInfo
argument_list|(
name|t
argument_list|,
name|partKeyExpr
argument_list|,
name|columnName
argument_list|,
name|columnType
argument_list|,
name|jobConf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|sourceInfoMap
operator|.
name|containsKey
argument_list|(
name|id
argument_list|)
condition|)
block|{
name|sourceInfoMap
operator|.
name|put
argument_list|(
name|id
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|SourceInfo
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sourceInfoMap
operator|.
name|get
argument_list|(
name|id
argument_list|)
operator|.
name|add
argument_list|(
name|si
argument_list|)
expr_stmt|;
comment|// We could have multiple sources restrict the same column, need to take
comment|// the union of the values in that case.
if|if
condition|(
name|columnMap
operator|.
name|containsKey
argument_list|(
name|columnName
argument_list|)
condition|)
block|{
name|si
operator|.
name|values
operator|=
name|columnMap
operator|.
name|get
argument_list|(
name|columnName
argument_list|)
operator|.
name|values
expr_stmt|;
block|}
name|columnMap
operator|.
name|put
argument_list|(
name|columnName
argument_list|,
name|si
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|processFiles
parameter_list|(
name|MapWork
name|work
parameter_list|,
name|JobConf
name|jobConf
parameter_list|)
throws|throws
name|HiveException
block|{
name|ObjectInputStream
name|in
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Path
name|baseDir
init|=
name|work
operator|.
name|getTmpPathForPartitionPruning
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|baseDir
operator|.
name|toUri
argument_list|()
argument_list|,
name|jobConf
argument_list|)
decl_stmt|;
comment|// Find the SourceInfo to put values in.
for|for
control|(
name|String
name|name
range|:
name|sourceInfoMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|Path
name|sourceDir
init|=
operator|new
name|Path
argument_list|(
name|baseDir
argument_list|,
name|name
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|fstatus
range|:
name|fs
operator|.
name|listStatus
argument_list|(
name|sourceDir
argument_list|)
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Start processing pruning file: "
operator|+
name|fstatus
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|in
operator|=
operator|new
name|ObjectInputStream
argument_list|(
name|fs
operator|.
name|open
argument_list|(
name|fstatus
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numName
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|columnNames
init|=
operator|new
name|HashSet
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
name|numName
condition|;
name|i
operator|++
control|)
block|{
name|columnNames
operator|.
name|add
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// make sure the dpp sink has output for all the corresponding part columns
for|for
control|(
name|SourceInfo
name|si
range|:
name|sourceInfoMap
operator|.
name|get
argument_list|(
name|name
argument_list|)
control|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|columnNames
operator|.
name|contains
argument_list|(
name|si
operator|.
name|columnName
argument_list|)
argument_list|,
literal|"AssertionError: no output for column "
operator|+
name|si
operator|.
name|columnName
argument_list|)
expr_stmt|;
block|}
comment|// Read dpp outputs
while|while
condition|(
name|in
operator|.
name|available
argument_list|()
operator|>
literal|0
condition|)
block|{
name|writable
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
for|for
control|(
name|SourceInfo
name|info
range|:
name|sourceInfoMap
operator|.
name|get
argument_list|(
name|name
argument_list|)
control|)
block|{
name|Object
name|row
init|=
name|info
operator|.
name|deserializer
operator|.
name|deserialize
argument_list|(
name|writable
argument_list|)
decl_stmt|;
name|Object
name|value
init|=
name|info
operator|.
name|soi
operator|.
name|getStructFieldData
argument_list|(
name|row
argument_list|,
name|info
operator|.
name|field
argument_list|)
decl_stmt|;
name|value
operator|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|value
argument_list|,
name|info
operator|.
name|fieldInspector
argument_list|)
expr_stmt|;
name|info
operator|.
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
try|try
block|{
if|if
condition|(
name|in
operator|!=
literal|null
condition|)
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"error while trying to close input stream"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
specifier|private
name|void
name|prunePartitions
parameter_list|(
name|MapWork
name|work
parameter_list|)
throws|throws
name|HiveException
block|{
for|for
control|(
name|String
name|source
range|:
name|sourceInfoMap
operator|.
name|keySet
argument_list|()
control|)
block|{
for|for
control|(
name|SourceInfo
name|info
range|:
name|sourceInfoMap
operator|.
name|get
argument_list|(
name|source
argument_list|)
control|)
block|{
name|prunePartitionSingleSource
argument_list|(
name|info
argument_list|,
name|work
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|prunePartitionSingleSource
parameter_list|(
name|SourceInfo
name|info
parameter_list|,
name|MapWork
name|work
parameter_list|)
throws|throws
name|HiveException
block|{
name|Set
argument_list|<
name|Object
argument_list|>
name|values
init|=
name|info
operator|.
name|values
decl_stmt|;
comment|// strip the column name of the targetId
name|String
name|columnName
init|=
name|SparkPartitionPruningSinkDesc
operator|.
name|stripOffTargetId
argument_list|(
name|info
operator|.
name|columnName
argument_list|)
decl_stmt|;
name|ObjectInspector
name|oi
init|=
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|info
operator|.
name|columnType
argument_list|)
argument_list|)
decl_stmt|;
name|ObjectInspectorConverters
operator|.
name|Converter
name|converter
init|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|javaStringObjectInspector
argument_list|,
name|oi
argument_list|)
decl_stmt|;
name|StructObjectInspector
name|soi
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|columnName
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|oi
argument_list|)
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|ExprNodeEvaluator
name|eval
init|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|info
operator|.
name|partKey
argument_list|)
decl_stmt|;
name|eval
operator|.
name|initialize
argument_list|(
name|soi
argument_list|)
expr_stmt|;
name|applyFilterToPartitions
argument_list|(
name|work
argument_list|,
name|converter
argument_list|,
name|eval
argument_list|,
name|columnName
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|applyFilterToPartitions
parameter_list|(
name|MapWork
name|work
parameter_list|,
name|ObjectInspectorConverters
operator|.
name|Converter
name|converter
parameter_list|,
name|ExprNodeEvaluator
name|eval
parameter_list|,
name|String
name|columnName
parameter_list|,
name|Set
argument_list|<
name|Object
argument_list|>
name|values
parameter_list|)
throws|throws
name|HiveException
block|{
name|Object
index|[]
name|row
init|=
operator|new
name|Object
index|[
literal|1
index|]
decl_stmt|;
name|Iterator
argument_list|<
name|Path
argument_list|>
name|it
init|=
name|work
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Path
name|p
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|PartitionDesc
name|desc
init|=
name|work
operator|.
name|getPathToPartitionInfo
argument_list|()
operator|.
name|get
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|spec
init|=
name|desc
operator|.
name|getPartSpec
argument_list|()
decl_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|spec
argument_list|,
literal|"No partition spec found in dynamic pruning"
argument_list|)
expr_stmt|;
name|String
name|partValueString
init|=
name|spec
operator|.
name|get
argument_list|(
name|columnName
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|partValueString
argument_list|,
literal|"Could not find partition value for column: "
operator|+
name|columnName
argument_list|)
expr_stmt|;
name|Object
name|partValue
init|=
name|converter
operator|.
name|convert
argument_list|(
name|partValueString
argument_list|)
decl_stmt|;
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
literal|"Converted partition value: "
operator|+
name|partValue
operator|+
literal|" original ("
operator|+
name|partValueString
operator|+
literal|")"
argument_list|)
expr_stmt|;
block|}
name|row
index|[
literal|0
index|]
operator|=
name|partValue
expr_stmt|;
name|partValue
operator|=
name|eval
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
expr_stmt|;
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
literal|"part key expr applied: "
operator|+
name|partValue
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|values
operator|.
name|contains
argument_list|(
name|partValue
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Pruning path: "
operator|+
name|p
argument_list|)
expr_stmt|;
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
name|work
operator|.
name|removePathToAlias
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|work
operator|.
name|removePathToPartitionInfo
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
specifier|private
specifier|static
class|class
name|SourceInfo
block|{
specifier|final
name|ExprNodeDesc
name|partKey
decl_stmt|;
specifier|final
name|Deserializer
name|deserializer
decl_stmt|;
specifier|final
name|StructObjectInspector
name|soi
decl_stmt|;
specifier|final
name|StructField
name|field
decl_stmt|;
specifier|final
name|ObjectInspector
name|fieldInspector
decl_stmt|;
name|Set
argument_list|<
name|Object
argument_list|>
name|values
init|=
operator|new
name|HashSet
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
specifier|final
name|String
name|columnName
decl_stmt|;
specifier|final
name|String
name|columnType
decl_stmt|;
name|SourceInfo
parameter_list|(
name|TableDesc
name|table
parameter_list|,
name|ExprNodeDesc
name|partKey
parameter_list|,
name|String
name|columnName
parameter_list|,
name|String
name|columnType
parameter_list|,
name|JobConf
name|jobConf
parameter_list|)
throws|throws
name|SerDeException
block|{
name|this
operator|.
name|partKey
operator|=
name|partKey
expr_stmt|;
name|this
operator|.
name|columnName
operator|=
name|columnName
expr_stmt|;
name|this
operator|.
name|columnType
operator|=
name|columnType
expr_stmt|;
name|deserializer
operator|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|table
operator|.
name|getDeserializerClass
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|deserializer
operator|.
name|initialize
argument_list|(
name|jobConf
argument_list|,
name|table
operator|.
name|getProperties
argument_list|()
argument_list|)
expr_stmt|;
name|ObjectInspector
name|inspector
init|=
name|deserializer
operator|.
name|getObjectInspector
argument_list|()
decl_stmt|;
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
literal|"Type of obj insp: "
operator|+
name|inspector
operator|.
name|getTypeName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|soi
operator|=
operator|(
name|StructObjectInspector
operator|)
name|inspector
expr_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|StructField
argument_list|>
name|fields
init|=
name|soi
operator|.
name|getAllStructFieldRefs
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|fields
operator|.
name|size
argument_list|()
operator|>
literal|1
operator|)
operator|:
literal|"expecting single field in input"
assert|;
name|field
operator|=
name|fields
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|fieldInspector
operator|=
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|field
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

