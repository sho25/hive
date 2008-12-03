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
name|exprNodeDesc
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
name|reduceSinkDesc
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
name|tableDesc
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
comment|/**  * Reduce Sink Operator sends output to the reduce stage  **/
end_comment

begin_class
specifier|public
class|class
name|ReduceSinkOperator
extends|extends
name|TerminalOperator
argument_list|<
name|reduceSinkDesc
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
comment|/**    * The evaluators for the key columns.    * Key columns decide the sort order on the reducer side.    * Key columns are passed to the reducer in the "key".    */
specifier|transient
specifier|protected
name|ExprNodeEvaluator
index|[]
name|keyEval
decl_stmt|;
comment|/**    * The evaluators for the value columns.    * Value columns are passed to reducer in the "value".     */
specifier|transient
specifier|protected
name|ExprNodeEvaluator
index|[]
name|valueEval
decl_stmt|;
comment|/**    * The evaluators for the partition columns (CLUSTER BY or DISTRIBUTE BY in Hive language).    * Partition columns decide the reducer that the current row goes to.    * Partition columns are not passed to reducer.    */
specifier|transient
specifier|protected
name|ExprNodeEvaluator
index|[]
name|partitionEval
decl_stmt|;
comment|// TODO: we use MetadataTypedColumnsetSerDe for now, till DynamicSerDe is ready
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
specifier|public
name|void
name|initialize
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initialize
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
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
name|exprNodeDesc
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
name|exprNodeDesc
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
name|exprNodeDesc
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
name|tableDesc
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
name|tableDesc
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
name|ObjectInspector
name|keyObjectInspector
decl_stmt|;
specifier|transient
name|ObjectInspector
name|valueObjectInspector
decl_stmt|;
specifier|transient
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|keyFieldsObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
specifier|transient
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|valueFieldsObjectInspectors
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|ObjectInspector
name|rowInspector
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
comment|// Evaluate the keys
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|keyEval
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|ExprNodeEvaluator
name|e
range|:
name|keyEval
control|)
block|{
name|e
operator|.
name|evaluate
argument_list|(
name|row
argument_list|,
name|rowInspector
argument_list|,
name|tempInspectableObject
argument_list|)
expr_stmt|;
name|keys
operator|.
name|add
argument_list|(
name|tempInspectableObject
operator|.
name|o
argument_list|)
expr_stmt|;
comment|// Construct the keyObjectInspector from the first row
if|if
condition|(
name|keyObjectInspector
operator|==
literal|null
condition|)
block|{
name|keyFieldsObjectInspectors
operator|.
name|add
argument_list|(
name|tempInspectableObject
operator|.
name|oi
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Construct the keyObjectInspector from the first row
if|if
condition|(
name|keyObjectInspector
operator|==
literal|null
condition|)
block|{
name|keyObjectInspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|ObjectInspectorUtils
operator|.
name|getIntegerArray
argument_list|(
name|keyFieldsObjectInspectors
operator|.
name|size
argument_list|()
argument_list|)
argument_list|,
name|keyFieldsObjectInspectors
argument_list|)
expr_stmt|;
block|}
comment|// Serialize the keys and append the tag
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
name|keys
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
name|keys
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
name|get
argument_list|()
argument_list|,
literal|0
argument_list|,
name|key
operator|.
name|getSize
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
name|getSize
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
name|get
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
comment|// Set the HashCode
name|int
name|keyHashCode
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ExprNodeEvaluator
name|e
range|:
name|partitionEval
control|)
block|{
name|e
operator|.
name|evaluate
argument_list|(
name|row
argument_list|,
name|rowInspector
argument_list|,
name|tempInspectableObject
argument_list|)
expr_stmt|;
name|keyHashCode
operator|=
name|keyHashCode
operator|*
literal|31
operator|+
operator|(
name|tempInspectableObject
operator|.
name|o
operator|==
literal|null
condition|?
literal|0
else|:
name|tempInspectableObject
operator|.
name|o
operator|.
name|hashCode
argument_list|()
operator|)
expr_stmt|;
block|}
name|keyWritable
operator|.
name|setHashCode
argument_list|(
name|keyHashCode
argument_list|)
expr_stmt|;
comment|// Evaluate the value
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|valueEval
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|ExprNodeEvaluator
name|e
range|:
name|valueEval
control|)
block|{
name|e
operator|.
name|evaluate
argument_list|(
name|row
argument_list|,
name|rowInspector
argument_list|,
name|tempInspectableObject
argument_list|)
expr_stmt|;
name|values
operator|.
name|add
argument_list|(
name|tempInspectableObject
operator|.
name|o
argument_list|)
expr_stmt|;
comment|// Construct the valueObjectInspector from the first row
if|if
condition|(
name|valueObjectInspector
operator|==
literal|null
condition|)
block|{
name|valueFieldsObjectInspectors
operator|.
name|add
argument_list|(
name|tempInspectableObject
operator|.
name|oi
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Construct the valueObjectInspector from the first row
if|if
condition|(
name|valueObjectInspector
operator|==
literal|null
condition|)
block|{
name|valueObjectInspector
operator|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|ObjectInspectorUtils
operator|.
name|getIntegerArray
argument_list|(
name|valueFieldsObjectInspectors
operator|.
name|size
argument_list|()
argument_list|)
argument_list|,
name|valueFieldsObjectInspectors
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
name|values
argument_list|,
name|valueObjectInspector
argument_list|)
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
try|try
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
specifier|public
name|String
name|getOperatorName
parameter_list|()
block|{
return|return
operator|new
name|String
argument_list|(
literal|"RS"
argument_list|)
return|;
block|}
block|}
end_class

end_unit

