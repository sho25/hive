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
name|Arrays
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
name|Random
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
name|io
operator|.
name|HiveKey
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
name|ExprNodeDescUtils
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
name|ReduceSinkDesc
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
name|Serializer
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
name|InspectableObject
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
name|StandardUnionObjectInspector
operator|.
name|StandardUnion
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
name|UnionObjectInspector
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
name|BinaryComparable
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
name|io
operator|.
name|IntWritable
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
name|Text
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
name|Writable
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
name|OutputCollector
import|;
end_import

begin_comment
comment|/**  * Reduce Sink Operator sends output to the reduce stage.  **/
end_comment

begin_class
specifier|public
class|class
name|ReduceSinkOperator
extends|extends
name|TerminalOperator
argument_list|<
name|ReduceSinkDesc
argument_list|>
implements|implements
name|Serializable
implements|,
name|TopNHash
operator|.
name|BinaryCollector
block|{
static|static
block|{
name|PTFUtils
operator|.
name|makeTransient
argument_list|(
name|ReduceSinkOperator
operator|.
name|class
argument_list|,
literal|"inputAliases"
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|protected
specifier|transient
name|OutputCollector
name|out
decl_stmt|;
comment|/**    * The evaluators for the key columns. Key columns decide the sort order on    * the reducer side. Key columns are passed to the reducer in the "key".    */
specifier|protected
specifier|transient
name|ExprNodeEvaluator
index|[]
name|keyEval
decl_stmt|;
comment|/**    * The evaluators for the value columns. Value columns are passed to reducer    * in the "value".    */
specifier|protected
specifier|transient
name|ExprNodeEvaluator
index|[]
name|valueEval
decl_stmt|;
comment|/**    * The evaluators for the partition columns (CLUSTER BY or DISTRIBUTE BY in    * Hive language). Partition columns decide the reducer that the current row    * goes to. Partition columns are not passed to reducer.    */
specifier|protected
specifier|transient
name|ExprNodeEvaluator
index|[]
name|partitionEval
decl_stmt|;
comment|/**    * Evaluators for bucketing columns. This is used to compute bucket number.    */
specifier|protected
specifier|transient
name|ExprNodeEvaluator
index|[]
name|bucketEval
init|=
literal|null
decl_stmt|;
comment|// TODO: we use MetadataTypedColumnsetSerDe for now, till DynamicSerDe is
comment|// ready
specifier|protected
specifier|transient
name|Serializer
name|keySerializer
decl_stmt|;
specifier|protected
specifier|transient
name|boolean
name|keyIsText
decl_stmt|;
specifier|protected
specifier|transient
name|Serializer
name|valueSerializer
decl_stmt|;
specifier|transient
name|int
name|tag
decl_stmt|;
specifier|protected
specifier|transient
name|byte
index|[]
name|tagByte
init|=
operator|new
name|byte
index|[
literal|1
index|]
decl_stmt|;
specifier|transient
specifier|protected
name|int
name|numDistributionKeys
decl_stmt|;
specifier|transient
specifier|protected
name|int
name|numDistinctExprs
decl_stmt|;
specifier|transient
name|String
index|[]
name|inputAliases
decl_stmt|;
comment|// input aliases of this RS for join (used for PPD)
specifier|public
name|void
name|setInputAliases
parameter_list|(
name|String
index|[]
name|inputAliases
parameter_list|)
block|{
name|this
operator|.
name|inputAliases
operator|=
name|inputAliases
expr_stmt|;
block|}
specifier|public
name|String
index|[]
name|getInputAliases
parameter_list|()
block|{
return|return
name|inputAliases
return|;
block|}
specifier|public
name|void
name|setOutputCollector
parameter_list|(
name|OutputCollector
name|_out
parameter_list|)
block|{
name|this
operator|.
name|out
operator|=
name|_out
expr_stmt|;
block|}
comment|// picks topN K:V pairs from input.
specifier|protected
specifier|transient
name|TopNHash
name|reducerHash
init|=
operator|new
name|TopNHash
argument_list|()
decl_stmt|;
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
try|try
block|{
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keys
init|=
name|conf
operator|.
name|getKeyCols
argument_list|()
decl_stmt|;
name|keyEval
operator|=
operator|new
name|ExprNodeEvaluator
index|[
name|keys
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|e
range|:
name|keys
control|)
block|{
name|keyEval
index|[
name|i
operator|++
index|]
operator|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
name|numDistributionKeys
operator|=
name|conf
operator|.
name|getNumDistributionKeys
argument_list|()
expr_stmt|;
name|distinctColIndices
operator|=
name|conf
operator|.
name|getDistinctColumnIndices
argument_list|()
expr_stmt|;
name|numDistinctExprs
operator|=
name|distinctColIndices
operator|.
name|size
argument_list|()
expr_stmt|;
name|valueEval
operator|=
operator|new
name|ExprNodeEvaluator
index|[
name|conf
operator|.
name|getValueCols
argument_list|()
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|i
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|ExprNodeDesc
name|e
range|:
name|conf
operator|.
name|getValueCols
argument_list|()
control|)
block|{
name|valueEval
index|[
name|i
operator|++
index|]
operator|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
name|partitionEval
operator|=
operator|new
name|ExprNodeEvaluator
index|[
name|conf
operator|.
name|getPartitionCols
argument_list|()
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|i
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|ExprNodeDesc
name|e
range|:
name|conf
operator|.
name|getPartitionCols
argument_list|()
control|)
block|{
name|int
name|index
init|=
name|ExprNodeDescUtils
operator|.
name|indexOf
argument_list|(
name|e
argument_list|,
name|keys
argument_list|)
decl_stmt|;
name|partitionEval
index|[
name|i
operator|++
index|]
operator|=
name|index
operator|<
literal|0
condition|?
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|e
argument_list|)
else|:
name|keyEval
index|[
name|index
index|]
expr_stmt|;
block|}
if|if
condition|(
name|conf
operator|.
name|getBucketCols
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|conf
operator|.
name|getBucketCols
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|bucketEval
operator|=
operator|new
name|ExprNodeEvaluator
index|[
name|conf
operator|.
name|getBucketCols
argument_list|()
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|i
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|ExprNodeDesc
name|e
range|:
name|conf
operator|.
name|getBucketCols
argument_list|()
control|)
block|{
name|int
name|index
init|=
name|ExprNodeDescUtils
operator|.
name|indexOf
argument_list|(
name|e
argument_list|,
name|keys
argument_list|)
decl_stmt|;
name|bucketEval
index|[
name|i
operator|++
index|]
operator|=
name|index
operator|<
literal|0
condition|?
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|e
argument_list|)
else|:
name|keyEval
index|[
name|index
index|]
expr_stmt|;
block|}
name|buckColIdxInKey
operator|=
name|conf
operator|.
name|getPartitionCols
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
name|tag
operator|=
name|conf
operator|.
name|getTag
argument_list|()
expr_stmt|;
name|tagByte
index|[
literal|0
index|]
operator|=
operator|(
name|byte
operator|)
name|tag
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Using tag = "
operator|+
name|tag
argument_list|)
expr_stmt|;
name|TableDesc
name|keyTableDesc
init|=
name|conf
operator|.
name|getKeySerializeInfo
argument_list|()
decl_stmt|;
name|keySerializer
operator|=
operator|(
name|Serializer
operator|)
name|keyTableDesc
operator|.
name|getDeserializerClass
argument_list|()
operator|.
name|newInstance
argument_list|()
expr_stmt|;
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
name|keyIsText
operator|=
name|keySerializer
operator|.
name|getSerializedClass
argument_list|()
operator|.
name|equals
argument_list|(
name|Text
operator|.
name|class
argument_list|)
expr_stmt|;
name|TableDesc
name|valueTableDesc
init|=
name|conf
operator|.
name|getValueSerializeInfo
argument_list|()
decl_stmt|;
name|valueSerializer
operator|=
operator|(
name|Serializer
operator|)
name|valueTableDesc
operator|.
name|getDeserializerClass
argument_list|()
operator|.
name|newInstance
argument_list|()
expr_stmt|;
name|valueSerializer
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
name|int
name|limit
init|=
name|conf
operator|.
name|getTopN
argument_list|()
decl_stmt|;
name|float
name|memUsage
init|=
name|conf
operator|.
name|getTopNMemoryUsage
argument_list|()
decl_stmt|;
if|if
condition|(
name|limit
operator|>=
literal|0
operator|&&
name|memUsage
operator|>
literal|0
condition|)
block|{
name|reducerHash
operator|.
name|initialize
argument_list|(
name|limit
argument_list|,
name|memUsage
argument_list|,
name|conf
operator|.
name|isMapGroupBy
argument_list|()
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
name|firstRow
operator|=
literal|true
expr_stmt|;
name|initializeChildren
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|transient
name|InspectableObject
name|tempInspectableObject
init|=
operator|new
name|InspectableObject
argument_list|()
decl_stmt|;
specifier|protected
specifier|transient
name|HiveKey
name|keyWritable
init|=
operator|new
name|HiveKey
argument_list|()
decl_stmt|;
specifier|protected
specifier|transient
name|ObjectInspector
name|keyObjectInspector
decl_stmt|;
specifier|protected
specifier|transient
name|ObjectInspector
name|valueObjectInspector
decl_stmt|;
specifier|transient
name|ObjectInspector
index|[]
name|partitionObjectInspectors
decl_stmt|;
specifier|transient
name|ObjectInspector
index|[]
name|bucketObjectInspectors
init|=
literal|null
decl_stmt|;
specifier|transient
name|int
name|buckColIdxInKey
decl_stmt|;
specifier|protected
specifier|transient
name|Object
index|[]
name|cachedValues
decl_stmt|;
specifier|protected
specifier|transient
name|List
argument_list|<
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|distinctColIndices
decl_stmt|;
comment|/**    * This two dimensional array holds key data and a corresponding Union object    * which contains the tag identifying the aggregate expression for distinct columns.    *    * If there is no distict expression, cachedKeys is simply like this.    * cachedKeys[0] = [col0][col1]    *    * with two distict expression, union(tag:key) is attatched for each distinct expression    * cachedKeys[0] = [col0][col1][0:dist1]    * cachedKeys[1] = [col0][col1][1:dist2]    *    * in this case, child GBY evaluates distict values with expression like KEY.col2:0.dist1    * see {@link ExprNodeColumnEvaluator}    */
comment|// TODO: we only ever use one row of these at a time. Why do we need to cache multiple?
specifier|protected
specifier|transient
name|Object
index|[]
index|[]
name|cachedKeys
decl_stmt|;
name|boolean
name|firstRow
decl_stmt|;
specifier|protected
specifier|transient
name|Random
name|random
decl_stmt|;
comment|/**    * Initializes array of ExprNodeEvaluator. Adds Union field for distinct    * column indices for group by.    * Puts the return values into a StructObjectInspector with output column    * names.    *    * If distinctColIndices is empty, the object inspector is same as    * {@link Operator#initEvaluatorsAndReturnStruct(ExprNodeEvaluator[], List, ObjectInspector)}    */
specifier|protected
specifier|static
name|StructObjectInspector
name|initEvaluatorsAndReturnStruct
parameter_list|(
name|ExprNodeEvaluator
index|[]
name|evals
parameter_list|,
name|List
argument_list|<
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|distinctColIndices
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|outputColNames
parameter_list|,
name|int
name|length
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
block|{
name|int
name|inspectorLen
init|=
name|evals
operator|.
name|length
operator|>
name|length
condition|?
name|length
operator|+
literal|1
else|:
name|evals
operator|.
name|length
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|sois
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|(
name|inspectorLen
argument_list|)
decl_stmt|;
comment|// keys
name|ObjectInspector
index|[]
name|fieldObjectInspectors
init|=
name|initEvaluators
argument_list|(
name|evals
argument_list|,
literal|0
argument_list|,
name|length
argument_list|,
name|rowInspector
argument_list|)
decl_stmt|;
name|sois
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|fieldObjectInspectors
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|outputColNames
operator|.
name|size
argument_list|()
operator|>
name|length
condition|)
block|{
comment|// union keys
assert|assert
name|distinctColIndices
operator|!=
literal|null
assert|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|uois
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|Integer
argument_list|>
name|distinctCols
range|:
name|distinctColIndices
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|names
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ObjectInspector
argument_list|>
name|eois
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|numExprs
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
range|:
name|distinctCols
control|)
block|{
name|names
operator|.
name|add
argument_list|(
name|HiveConf
operator|.
name|getColumnInternalName
argument_list|(
name|numExprs
argument_list|)
argument_list|)
expr_stmt|;
name|eois
operator|.
name|add
argument_list|(
name|evals
index|[
name|i
index|]
operator|.
name|initialize
argument_list|(
name|rowInspector
argument_list|)
argument_list|)
expr_stmt|;
name|numExprs
operator|++
expr_stmt|;
block|}
name|uois
operator|.
name|add
argument_list|(
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|names
argument_list|,
name|eois
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|UnionObjectInspector
name|uoi
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardUnionObjectInspector
argument_list|(
name|uois
argument_list|)
decl_stmt|;
name|sois
operator|.
name|add
argument_list|(
name|uoi
argument_list|)
expr_stmt|;
block|}
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|outputColNames
argument_list|,
name|sois
argument_list|)
return|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
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
name|ObjectInspector
name|rowInspector
init|=
name|inputObjInspectors
index|[
name|tag
index|]
decl_stmt|;
if|if
condition|(
name|firstRow
condition|)
block|{
name|firstRow
operator|=
literal|false
expr_stmt|;
comment|// TODO: this is fishy - we init object inspectors based on first tag. We
comment|//       should either init for each tag, or if rowInspector doesn't really
comment|//       matter, then we can create this in ctor and get rid of firstRow.
name|keyObjectInspector
operator|=
name|initEvaluatorsAndReturnStruct
argument_list|(
name|keyEval
argument_list|,
name|distinctColIndices
argument_list|,
name|conf
operator|.
name|getOutputKeyColumnNames
argument_list|()
argument_list|,
name|numDistributionKeys
argument_list|,
name|rowInspector
argument_list|)
expr_stmt|;
name|valueObjectInspector
operator|=
name|initEvaluatorsAndReturnStruct
argument_list|(
name|valueEval
argument_list|,
name|conf
operator|.
name|getOutputValueColumnNames
argument_list|()
argument_list|,
name|rowInspector
argument_list|)
expr_stmt|;
name|partitionObjectInspectors
operator|=
name|initEvaluators
argument_list|(
name|partitionEval
argument_list|,
name|rowInspector
argument_list|)
expr_stmt|;
if|if
condition|(
name|bucketEval
operator|!=
literal|null
condition|)
block|{
name|bucketObjectInspectors
operator|=
name|initEvaluators
argument_list|(
name|bucketEval
argument_list|,
name|rowInspector
argument_list|)
expr_stmt|;
block|}
name|int
name|numKeys
init|=
name|numDistinctExprs
operator|>
literal|0
condition|?
name|numDistinctExprs
else|:
literal|1
decl_stmt|;
name|int
name|keyLen
init|=
name|numDistinctExprs
operator|>
literal|0
condition|?
name|numDistributionKeys
operator|+
literal|1
else|:
name|numDistributionKeys
decl_stmt|;
name|cachedKeys
operator|=
operator|new
name|Object
index|[
name|numKeys
index|]
index|[
name|keyLen
index|]
expr_stmt|;
name|cachedValues
operator|=
operator|new
name|Object
index|[
name|valueEval
operator|.
name|length
index|]
expr_stmt|;
block|}
comment|// Determine distKeyLength (w/o distincts), and then add the first if present.
name|populateCachedDistributionKeys
argument_list|(
name|row
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// replace bucketing columns with hashcode % numBuckets
name|int
name|buckNum
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|bucketEval
operator|!=
literal|null
condition|)
block|{
name|buckNum
operator|=
name|computeBucketNumber
argument_list|(
name|row
argument_list|,
name|conf
operator|.
name|getNumBuckets
argument_list|()
argument_list|)
expr_stmt|;
name|cachedKeys
index|[
literal|0
index|]
index|[
name|buckColIdxInKey
index|]
operator|=
operator|new
name|IntWritable
argument_list|(
name|buckNum
argument_list|)
expr_stmt|;
block|}
name|HiveKey
name|firstKey
init|=
name|toHiveKey
argument_list|(
name|cachedKeys
index|[
literal|0
index|]
argument_list|,
name|tag
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|int
name|distKeyLength
init|=
name|firstKey
operator|.
name|getDistKeyLength
argument_list|()
decl_stmt|;
if|if
condition|(
name|numDistinctExprs
operator|>
literal|0
condition|)
block|{
name|populateCachedDistinctKeys
argument_list|(
name|row
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|firstKey
operator|=
name|toHiveKey
argument_list|(
name|cachedKeys
index|[
literal|0
index|]
argument_list|,
name|tag
argument_list|,
name|distKeyLength
argument_list|)
expr_stmt|;
block|}
comment|// Try to store the first key. If it's not excluded, we will proceed.
name|int
name|firstIndex
init|=
name|reducerHash
operator|.
name|tryStoreKey
argument_list|(
name|firstKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|firstIndex
operator|==
name|TopNHash
operator|.
name|EXCLUDE
condition|)
return|return;
comment|// Nothing to do.
comment|// Compute value and hashcode - we'd either store or forward them.
name|BytesWritable
name|value
init|=
name|makeValueWritable
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|int
name|hashCode
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|bucketEval
operator|==
literal|null
condition|)
block|{
name|hashCode
operator|=
name|computeHashCode
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hashCode
operator|=
name|computeHashCode
argument_list|(
name|row
argument_list|,
name|buckNum
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|firstIndex
operator|==
name|TopNHash
operator|.
name|FORWARD
condition|)
block|{
name|firstKey
operator|.
name|setHashCode
argument_list|(
name|hashCode
argument_list|)
expr_stmt|;
name|collect
argument_list|(
name|firstKey
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|firstIndex
operator|>=
literal|0
assert|;
name|reducerHash
operator|.
name|storeValue
argument_list|(
name|firstIndex
argument_list|,
name|value
argument_list|,
name|hashCode
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|// All other distinct keys will just be forwarded. This could be optimized...
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|numDistinctExprs
condition|;
name|i
operator|++
control|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|cachedKeys
index|[
literal|0
index|]
argument_list|,
literal|0
argument_list|,
name|cachedKeys
index|[
name|i
index|]
argument_list|,
literal|0
argument_list|,
name|numDistributionKeys
argument_list|)
expr_stmt|;
name|populateCachedDistinctKeys
argument_list|(
name|row
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|HiveKey
name|hiveKey
init|=
name|toHiveKey
argument_list|(
name|cachedKeys
index|[
name|i
index|]
argument_list|,
name|tag
argument_list|,
name|distKeyLength
argument_list|)
decl_stmt|;
name|hiveKey
operator|.
name|setHashCode
argument_list|(
name|hashCode
argument_list|)
expr_stmt|;
name|collect
argument_list|(
name|hiveKey
argument_list|,
name|value
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
name|e
throw|;
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
block|}
specifier|private
name|int
name|computeBucketNumber
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|numBuckets
parameter_list|)
throws|throws
name|HiveException
block|{
name|int
name|buckNum
init|=
literal|0
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
name|bucketEval
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|o
init|=
name|bucketEval
index|[
name|i
index|]
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|buckNum
operator|=
name|buckNum
operator|*
literal|31
operator|+
name|ObjectInspectorUtils
operator|.
name|hashCode
argument_list|(
name|o
argument_list|,
name|bucketObjectInspectors
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|buckNum
operator|<
literal|0
condition|)
block|{
name|buckNum
operator|=
operator|-
literal|1
operator|*
name|buckNum
expr_stmt|;
block|}
return|return
name|buckNum
operator|%
name|numBuckets
return|;
block|}
specifier|private
name|void
name|populateCachedDistributionKeys
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|index
parameter_list|)
throws|throws
name|HiveException
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
name|numDistributionKeys
condition|;
name|i
operator|++
control|)
block|{
name|cachedKeys
index|[
name|index
index|]
index|[
name|i
index|]
operator|=
name|keyEval
index|[
name|i
index|]
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cachedKeys
index|[
literal|0
index|]
operator|.
name|length
operator|>
name|numDistributionKeys
condition|)
block|{
name|cachedKeys
index|[
name|index
index|]
index|[
name|numDistributionKeys
index|]
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**    * Populate distinct keys part of cachedKeys for a particular row.    * @param row the row    * @param index the cachedKeys index to write to    */
specifier|private
name|void
name|populateCachedDistinctKeys
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|index
parameter_list|)
throws|throws
name|HiveException
block|{
name|StandardUnion
name|union
decl_stmt|;
name|cachedKeys
index|[
name|index
index|]
index|[
name|numDistributionKeys
index|]
operator|=
name|union
operator|=
operator|new
name|StandardUnion
argument_list|(
operator|(
name|byte
operator|)
name|index
argument_list|,
operator|new
name|Object
index|[
name|distinctColIndices
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
name|Object
index|[]
name|distinctParameters
init|=
operator|(
name|Object
index|[]
operator|)
name|union
operator|.
name|getObject
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|distinctParamI
init|=
literal|0
init|;
name|distinctParamI
operator|<
name|distinctParameters
operator|.
name|length
condition|;
name|distinctParamI
operator|++
control|)
block|{
name|distinctParameters
index|[
name|distinctParamI
index|]
operator|=
name|keyEval
index|[
name|distinctColIndices
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|get
argument_list|(
name|distinctParamI
argument_list|)
index|]
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
name|union
operator|.
name|setTag
argument_list|(
operator|(
name|byte
operator|)
name|index
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|computeHashCode
parameter_list|(
name|Object
name|row
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Evaluate the HashCode
name|int
name|keyHashCode
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|partitionEval
operator|.
name|length
operator|==
literal|0
condition|)
block|{
comment|// If no partition cols, just distribute the data uniformly to provide better
comment|// load balance. If the requirement is to have a single reducer, we should set
comment|// the number of reducers to 1.
comment|// Use a constant seed to make the code deterministic.
if|if
condition|(
name|random
operator|==
literal|null
condition|)
block|{
name|random
operator|=
operator|new
name|Random
argument_list|(
literal|12345
argument_list|)
expr_stmt|;
block|}
name|keyHashCode
operator|=
name|random
operator|.
name|nextInt
argument_list|()
expr_stmt|;
block|}
else|else
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
name|partitionEval
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|o
init|=
name|partitionEval
index|[
name|i
index|]
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|keyHashCode
operator|=
name|keyHashCode
operator|*
literal|31
operator|+
name|ObjectInspectorUtils
operator|.
name|hashCode
argument_list|(
name|o
argument_list|,
name|partitionObjectInspectors
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|keyHashCode
return|;
block|}
specifier|private
name|int
name|computeHashCode
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|buckNum
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// Evaluate the HashCode
name|int
name|keyHashCode
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|partitionEval
operator|.
name|length
operator|==
literal|0
condition|)
block|{
comment|// If no partition cols, just distribute the data uniformly to provide better
comment|// load balance. If the requirement is to have a single reducer, we should set
comment|// the number of reducers to 1.
comment|// Use a constant seed to make the code deterministic.
if|if
condition|(
name|random
operator|==
literal|null
condition|)
block|{
name|random
operator|=
operator|new
name|Random
argument_list|(
literal|12345
argument_list|)
expr_stmt|;
block|}
name|keyHashCode
operator|=
name|random
operator|.
name|nextInt
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// partitionEval will include all columns from distribution columns i.e;
comment|// partition columns + bucket number columns. Bucket number column is
comment|// initialized with -1. Ignore that and use bucket number instead
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|partitionEval
operator|.
name|length
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|o
init|=
name|partitionEval
index|[
name|i
index|]
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|keyHashCode
operator|=
name|keyHashCode
operator|*
literal|31
operator|+
name|ObjectInspectorUtils
operator|.
name|hashCode
argument_list|(
name|o
argument_list|,
name|partitionObjectInspectors
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|keyHashCode
operator|=
name|keyHashCode
operator|*
literal|31
operator|+
name|buckNum
expr_stmt|;
block|}
return|return
name|keyHashCode
return|;
block|}
comment|// Serialize the keys and append the tag
specifier|protected
name|HiveKey
name|toHiveKey
parameter_list|(
name|Object
name|obj
parameter_list|,
name|int
name|tag
parameter_list|,
name|Integer
name|distLength
parameter_list|)
throws|throws
name|SerDeException
block|{
name|BinaryComparable
name|key
init|=
operator|(
name|BinaryComparable
operator|)
name|keySerializer
operator|.
name|serialize
argument_list|(
name|obj
argument_list|,
name|keyObjectInspector
argument_list|)
decl_stmt|;
name|int
name|keyLength
init|=
name|key
operator|.
name|getLength
argument_list|()
decl_stmt|;
if|if
condition|(
name|tag
operator|==
operator|-
literal|1
condition|)
block|{
name|keyWritable
operator|.
name|set
argument_list|(
name|key
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyLength
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|keyWritable
operator|.
name|setSize
argument_list|(
name|keyLength
operator|+
literal|1
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|key
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyWritable
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|keyLength
argument_list|)
expr_stmt|;
name|keyWritable
operator|.
name|get
argument_list|()
index|[
name|keyLength
index|]
operator|=
name|tagByte
index|[
literal|0
index|]
expr_stmt|;
block|}
name|keyWritable
operator|.
name|setDistKeyLength
argument_list|(
operator|(
name|distLength
operator|==
literal|null
operator|)
condition|?
name|keyLength
else|:
name|distLength
argument_list|)
expr_stmt|;
return|return
name|keyWritable
return|;
block|}
specifier|public
name|void
name|collect
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|int
name|hash
parameter_list|)
throws|throws
name|IOException
block|{
name|HiveKey
name|keyWritable
init|=
operator|new
name|HiveKey
argument_list|(
name|key
argument_list|,
name|hash
argument_list|)
decl_stmt|;
name|BytesWritable
name|valueWritable
init|=
operator|new
name|BytesWritable
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|collect
argument_list|(
name|keyWritable
argument_list|,
name|valueWritable
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|collect
parameter_list|(
name|BytesWritable
name|keyWritable
parameter_list|,
name|Writable
name|valueWritable
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Since this is a terminal operator, update counters explicitly -
comment|// forward is not called
if|if
condition|(
literal|null
operator|!=
name|out
condition|)
block|{
name|out
operator|.
name|collect
argument_list|(
name|keyWritable
argument_list|,
name|valueWritable
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|BytesWritable
name|makeValueWritable
parameter_list|(
name|Object
name|row
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Evaluate the value
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|valueEval
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|cachedValues
index|[
name|i
index|]
operator|=
name|valueEval
index|[
name|i
index|]
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
comment|// Serialize the value
return|return
operator|(
name|BytesWritable
operator|)
name|valueSerializer
operator|.
name|serialize
argument_list|(
name|cachedValues
argument_list|,
name|valueObjectInspector
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
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
operator|!
name|abort
condition|)
block|{
name|reducerHash
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
name|super
operator|.
name|closeOp
argument_list|(
name|abort
argument_list|)
expr_stmt|;
name|out
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * @return the name of the operator    */
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
literal|"RS"
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
name|REDUCESINK
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|opAllowedBeforeMapJoin
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

