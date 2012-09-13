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
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
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
comment|// TODO: we use MetadataTypedColumnsetSerDe for now, till DynamicSerDe is
comment|// ready
specifier|transient
name|Serializer
name|keySerializer
decl_stmt|;
specifier|transient
name|boolean
name|keyIsText
decl_stmt|;
specifier|transient
name|Serializer
name|valueSerializer
decl_stmt|;
specifier|transient
name|int
name|tag
decl_stmt|;
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
name|keyEval
operator|=
operator|new
name|ExprNodeEvaluator
index|[
name|conf
operator|.
name|getKeyCols
argument_list|()
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
name|conf
operator|.
name|getKeyCols
argument_list|()
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
name|partitionEval
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
specifier|transient
name|HiveKey
name|keyWritable
init|=
operator|new
name|HiveKey
argument_list|()
decl_stmt|;
specifier|transient
name|Writable
name|value
decl_stmt|;
specifier|transient
name|StructObjectInspector
name|keyObjectInspector
decl_stmt|;
specifier|transient
name|StructObjectInspector
name|valueObjectInspector
decl_stmt|;
specifier|transient
name|ObjectInspector
index|[]
name|partitionObjectInspectors
decl_stmt|;
specifier|transient
name|Object
index|[]
index|[]
name|cachedKeys
decl_stmt|;
specifier|transient
name|Object
index|[]
name|cachedValues
decl_stmt|;
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
name|boolean
name|firstRow
decl_stmt|;
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
name|evals
operator|.
name|length
operator|>
name|length
condition|)
block|{
comment|// union keys
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
comment|// If no partition cols, just distribute the data uniformly to provide
comment|// better
comment|// load balance. If the requirement is to have a single reducer, we
comment|// should set
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
name|value
operator|=
name|valueSerializer
operator|.
name|serialize
argument_list|(
name|cachedValues
argument_list|,
name|valueObjectInspector
argument_list|)
expr_stmt|;
comment|// Evaluate the keys
name|Object
index|[]
name|distributionKeys
init|=
operator|new
name|Object
index|[
name|numDistributionKeys
index|]
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
name|numDistributionKeys
condition|;
name|i
operator|++
control|)
block|{
name|distributionKeys
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
name|numDistinctExprs
operator|>
literal|0
condition|)
block|{
comment|// with distinct key(s)
for|for
control|(
name|int
name|i
init|=
literal|0
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
name|distributionKeys
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
name|Object
index|[]
name|distinctParameters
init|=
operator|new
name|Object
index|[
name|distinctColIndices
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|size
argument_list|()
index|]
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
name|distinctParameters
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|distinctParameters
index|[
name|j
index|]
operator|=
name|keyEval
index|[
name|distinctColIndices
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|get
argument_list|(
name|j
argument_list|)
index|]
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
name|cachedKeys
index|[
name|i
index|]
index|[
name|numDistributionKeys
index|]
operator|=
operator|new
name|StandardUnion
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|,
name|distinctParameters
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// no distinct key
name|System
operator|.
name|arraycopy
argument_list|(
name|distributionKeys
argument_list|,
literal|0
argument_list|,
name|cachedKeys
index|[
literal|0
index|]
argument_list|,
literal|0
argument_list|,
name|numDistributionKeys
argument_list|)
expr_stmt|;
block|}
comment|// Serialize the keys and append the tag
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|cachedKeys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|keyIsText
condition|)
block|{
name|Text
name|key
init|=
operator|(
name|Text
operator|)
name|keySerializer
operator|.
name|serialize
argument_list|(
name|cachedKeys
index|[
name|i
index|]
argument_list|,
name|keyObjectInspector
argument_list|)
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
name|key
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|keyLength
init|=
name|key
operator|.
name|getLength
argument_list|()
decl_stmt|;
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
block|}
else|else
block|{
comment|// Must be BytesWritable
name|BytesWritable
name|key
init|=
operator|(
name|BytesWritable
operator|)
name|keySerializer
operator|.
name|serialize
argument_list|(
name|cachedKeys
index|[
name|i
index|]
argument_list|,
name|keyObjectInspector
argument_list|)
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
name|key
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|keyLength
init|=
name|key
operator|.
name|getLength
argument_list|()
decl_stmt|;
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
block|}
name|keyWritable
operator|.
name|setHashCode
argument_list|(
name|keyHashCode
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|collect
argument_list|(
name|keyWritable
argument_list|,
name|value
argument_list|)
expr_stmt|;
comment|// Since this is a terminal operator, update counters explicitly -
comment|// forward is not called
if|if
condition|(
name|counterNameToEnum
operator|!=
literal|null
condition|)
block|{
operator|++
name|outputRows
expr_stmt|;
if|if
condition|(
name|outputRows
operator|%
literal|1000
operator|==
literal|0
condition|)
block|{
name|incrCounter
argument_list|(
name|numOutputRowsCntr
argument_list|,
name|outputRows
argument_list|)
expr_stmt|;
name|outputRows
operator|=
literal|0
expr_stmt|;
block|}
block|}
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
name|e
argument_list|)
throw|;
block|}
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
block|}
end_class

end_unit

