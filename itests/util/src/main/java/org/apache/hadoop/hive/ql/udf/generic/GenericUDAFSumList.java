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
name|udf
operator|.
name|generic
package|;
end_package

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
name|util
operator|.
name|JavaDataModel
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
name|ListObjectInspector
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
name|io
operator|.
name|LongWritable
import|;
end_import

begin_comment
comment|/**  * GenericUDAFSum.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"sum_list"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x) - Returns the sum of a set of numbers"
argument_list|)
specifier|public
class|class
name|GenericUDAFSumList
extends|extends
name|AbstractGenericUDAFResolver
block|{
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|GenericUDAFSumList
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
name|GenericUDAFParameterInfo
name|info
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ObjectInspector
index|[]
name|inspectors
init|=
name|info
operator|.
name|getParameterObjectInspectors
argument_list|()
decl_stmt|;
if|if
condition|(
name|inspectors
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
name|inspectors
operator|.
name|length
operator|-
literal|1
argument_list|,
literal|"Exactly one argument is expected."
argument_list|)
throw|;
block|}
if|if
condition|(
name|inspectors
index|[
literal|0
index|]
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|LIST
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Argument should be a list type"
argument_list|)
throw|;
block|}
name|ListObjectInspector
name|listOI
init|=
operator|(
name|ListObjectInspector
operator|)
name|inspectors
index|[
literal|0
index|]
decl_stmt|;
name|ObjectInspector
name|elementOI
init|=
name|listOI
operator|.
name|getListElementObjectInspector
argument_list|()
decl_stmt|;
if|if
condition|(
name|elementOI
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Only primitive type arguments are accepted but "
operator|+
name|elementOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" is passed."
argument_list|)
throw|;
block|}
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
name|pcat
init|=
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|elementOI
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
decl_stmt|;
return|return
operator|new
name|GenericUDAFSumLong
argument_list|()
return|;
block|}
comment|/**    * GenericUDAFSumLong.    *    */
specifier|public
specifier|static
class|class
name|GenericUDAFSumLong
extends|extends
name|GenericUDAFEvaluator
block|{
specifier|private
name|ListObjectInspector
name|listOI
decl_stmt|;
specifier|private
name|PrimitiveObjectInspector
name|elementOI
decl_stmt|;
specifier|private
name|ObjectInspectorConverters
operator|.
name|Converter
name|toLong
decl_stmt|;
specifier|private
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
specifier|private
name|LongWritable
name|result
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
name|result
operator|=
operator|new
name|LongWritable
argument_list|(
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|m
operator|==
name|Mode
operator|.
name|PARTIAL1
operator|||
name|m
operator|==
name|Mode
operator|.
name|COMPLETE
condition|)
block|{
name|listOI
operator|=
operator|(
name|ListObjectInspector
operator|)
name|parameters
index|[
literal|0
index|]
expr_stmt|;
name|elementOI
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|listOI
operator|.
name|getListElementObjectInspector
argument_list|()
expr_stmt|;
name|toLong
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|elementOI
argument_list|,
name|PrimitiveObjectInspectorFactory
operator|.
name|javaLongObjectInspector
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
block|}
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableLongObjectInspector
return|;
block|}
comment|/** class for storing double sum value. */
annotation|@
name|AggregationType
argument_list|(
name|estimable
operator|=
literal|true
argument_list|)
specifier|static
class|class
name|SumLongAgg
extends|extends
name|AbstractAggregationBuffer
block|{
name|boolean
name|empty
decl_stmt|;
name|long
name|sum
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|estimate
parameter_list|()
block|{
return|return
name|JavaDataModel
operator|.
name|PRIMITIVES1
operator|+
name|JavaDataModel
operator|.
name|PRIMITIVES2
return|;
block|}
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
name|SumLongAgg
name|result
init|=
operator|new
name|SumLongAgg
argument_list|()
decl_stmt|;
name|reset
argument_list|(
name|result
argument_list|)
expr_stmt|;
return|return
name|result
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
name|SumLongAgg
name|myagg
init|=
operator|(
name|SumLongAgg
operator|)
name|agg
decl_stmt|;
name|myagg
operator|.
name|empty
operator|=
literal|true
expr_stmt|;
name|myagg
operator|.
name|sum
operator|=
literal|0
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
assert|assert
operator|(
name|parameters
operator|.
name|length
operator|==
literal|1
operator|)
assert|;
name|SumLongAgg
name|myagg
init|=
operator|(
name|SumLongAgg
operator|)
name|agg
decl_stmt|;
name|int
name|length
init|=
name|listOI
operator|.
name|getListLength
argument_list|(
name|parameters
index|[
literal|0
index|]
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
name|Object
name|element
init|=
name|listOI
operator|.
name|getListElement
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|,
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|element
operator|!=
literal|null
condition|)
block|{
name|myagg
operator|.
name|sum
operator|+=
operator|(
name|Long
operator|)
name|toLong
operator|.
name|convert
argument_list|(
name|element
argument_list|)
expr_stmt|;
name|myagg
operator|.
name|empty
operator|=
literal|false
expr_stmt|;
block|}
block|}
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
return|return
name|terminate
argument_list|(
name|agg
argument_list|)
return|;
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
if|if
condition|(
name|partial
operator|!=
literal|null
condition|)
block|{
name|SumLongAgg
name|myagg
init|=
operator|(
name|SumLongAgg
operator|)
name|agg
decl_stmt|;
name|myagg
operator|.
name|sum
operator|+=
name|PrimitiveObjectInspectorUtils
operator|.
name|getLong
argument_list|(
name|partial
argument_list|,
name|inputOI
argument_list|)
expr_stmt|;
name|myagg
operator|.
name|empty
operator|=
literal|false
expr_stmt|;
block|}
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
name|SumLongAgg
name|myagg
init|=
operator|(
name|SumLongAgg
operator|)
name|agg
decl_stmt|;
if|if
condition|(
name|myagg
operator|.
name|empty
condition|)
block|{
return|return
literal|null
return|;
block|}
name|result
operator|.
name|set
argument_list|(
name|myagg
operator|.
name|sum
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
block|}
end_class

end_unit

