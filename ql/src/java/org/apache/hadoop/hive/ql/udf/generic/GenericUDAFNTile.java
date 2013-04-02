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
name|udf
operator|.
name|generic
package|;
end_package

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
name|hive
operator|.
name|ql
operator|.
name|exec
operator|.
name|Description
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
name|UDFArgumentTypeException
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
name|WindowFunctionDescription
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
name|parse
operator|.
name|SemanticException
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
name|udf
operator|.
name|generic
operator|.
name|GenericUDAFEvaluator
operator|.
name|AggregationBuffer
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
name|PrimitiveObjectInspector
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
name|objectinspector
operator|.
name|primitive
operator|.
name|PrimitiveObjectInspectorUtils
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
name|TypeInfo
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
name|TypeInfoUtils
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

begin_class
annotation|@
name|WindowFunctionDescription
argument_list|(
name|description
operator|=
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"rank"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x) NTILE allows easy calculation of tertiles, quartiles, deciles and other "
operator|+
literal|"common summary statistics. This function divides an ordered partition into a specified "
operator|+
literal|"number of groups called buckets and assigns a bucket number to each row in the partition."
argument_list|)
argument_list|,
name|supportsWindow
operator|=
literal|false
argument_list|,
name|pivotResult
operator|=
literal|true
argument_list|)
specifier|public
class|class
name|GenericUDAFNTile
extends|extends
name|AbstractGenericUDAFResolver
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|GenericUDAFNTile
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|GenericUDAFEvaluator
name|getEvaluator
parameter_list|(
name|TypeInfo
index|[]
name|parameters
parameter_list|)
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|parameters
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|parameters
operator|.
name|length
operator|-
literal|1
argument_list|,
literal|"Exactly one argument is expected."
argument_list|)
throw|;
block|}
name|ObjectInspector
name|oi
init|=
name|TypeInfoUtils
operator|.
name|getStandardJavaObjectInspectorFromTypeInfo
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|boolean
name|c
init|=
name|ObjectInspectorUtils
operator|.
name|compareTypes
argument_list|(
name|oi
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|c
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Number of tiles must be an int expression"
argument_list|)
throw|;
block|}
return|return
operator|new
name|GenericUDAFNTileEvaluator
argument_list|()
return|;
block|}
specifier|static
class|class
name|NTileBuffer
implements|implements
name|AggregationBuffer
block|{
name|Integer
name|numBuckets
decl_stmt|;
name|int
name|numRows
decl_stmt|;
name|void
name|init
parameter_list|()
block|{
name|numBuckets
operator|=
literal|null
expr_stmt|;
name|numRows
operator|=
literal|0
expr_stmt|;
block|}
name|NTileBuffer
parameter_list|()
block|{
name|init
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|GenericUDAFNTileEvaluator
extends|extends
name|GenericUDAFEvaluator
block|{
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ObjectInspector
name|init
parameter_list|(
name|Mode
name|m
parameter_list|,
name|ObjectInspector
index|[]
name|parameters
parameter_list|)
throws|throws
name|HiveException
block|{
assert|assert
operator|(
name|parameters
operator|.
name|length
operator|==
literal|1
operator|)
assert|;
name|super
operator|.
name|init
argument_list|(
name|m
argument_list|,
name|parameters
argument_list|)
expr_stmt|;
if|if
condition|(
name|m
operator|!=
name|Mode
operator|.
name|COMPLETE
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"Only COMPLETE mode supported for NTile function"
argument_list|)
throw|;
block|}
name|inputOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|parameters
index|[
literal|0
index|]
expr_stmt|;
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableIntObjectInspector
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AggregationBuffer
name|getNewAggregationBuffer
parameter_list|()
throws|throws
name|HiveException
block|{
return|return
operator|new
name|NTileBuffer
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
operator|(
operator|(
name|NTileBuffer
operator|)
name|agg
operator|)
operator|.
name|init
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|iterate
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|,
name|Object
index|[]
name|parameters
parameter_list|)
throws|throws
name|HiveException
block|{
name|NTileBuffer
name|rb
init|=
operator|(
name|NTileBuffer
operator|)
name|agg
decl_stmt|;
if|if
condition|(
name|rb
operator|.
name|numBuckets
operator|==
literal|null
condition|)
block|{
name|rb
operator|.
name|numBuckets
operator|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getInt
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|,
name|inputOI
argument_list|)
expr_stmt|;
block|}
name|rb
operator|.
name|numRows
operator|++
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|terminatePartial
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"terminatePartial not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|merge
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|,
name|Object
name|partial
parameter_list|)
throws|throws
name|HiveException
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"merge not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|terminate
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
name|NTileBuffer
name|rb
init|=
operator|(
name|NTileBuffer
operator|)
name|agg
decl_stmt|;
name|ArrayList
argument_list|<
name|IntWritable
argument_list|>
name|res
init|=
operator|new
name|ArrayList
argument_list|<
name|IntWritable
argument_list|>
argument_list|(
name|rb
operator|.
name|numRows
argument_list|)
decl_stmt|;
comment|/* 			 * if there is a remainder from numRows/numBuckets; then distribute increase the size of the first 'rem' buckets by 1. 			 */
name|int
name|bucketsz
init|=
name|rb
operator|.
name|numRows
operator|/
name|rb
operator|.
name|numBuckets
decl_stmt|;
name|int
name|rem
init|=
name|rb
operator|.
name|numRows
operator|%
name|rb
operator|.
name|numBuckets
decl_stmt|;
name|int
name|start
init|=
literal|0
decl_stmt|;
name|int
name|bucket
init|=
literal|1
decl_stmt|;
while|while
condition|(
name|start
operator|<
name|rb
operator|.
name|numRows
condition|)
block|{
name|int
name|end
init|=
name|start
operator|+
name|bucketsz
decl_stmt|;
if|if
condition|(
name|rem
operator|>
literal|0
condition|)
block|{
name|end
operator|++
expr_stmt|;
name|rem
operator|--
expr_stmt|;
block|}
name|end
operator|=
name|Math
operator|.
name|min
argument_list|(
name|rb
operator|.
name|numRows
argument_list|,
name|end
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|<
name|end
condition|;
name|i
operator|++
control|)
block|{
name|res
operator|.
name|add
argument_list|(
operator|new
name|IntWritable
argument_list|(
name|bucket
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|start
operator|=
name|end
expr_stmt|;
name|bucket
operator|++
expr_stmt|;
block|}
return|return
name|res
return|;
block|}
block|}
block|}
end_class

end_unit

