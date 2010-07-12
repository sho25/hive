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
name|UDFArgumentException
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
name|primitive
operator|.
name|LongObjectInspector
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
name|io
operator|.
name|LongWritable
import|;
end_import

begin_comment
comment|/**  * This class implements the COUNT aggregation function as in SQL.  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"count"
argument_list|,
name|value
operator|=
literal|"_FUNC_(*) - Returns the total number of retrieved rows, including "
operator|+
literal|"rows containing NULL values.\n"
operator|+
literal|"_FUNC_(expr) - Returns the number of rows for which the supplied "
operator|+
literal|"expression is non-NULL.\n"
operator|+
literal|"_FUNC_(DISTINCT expr[, expr...]) - Returns the number of rows for "
operator|+
literal|"which the supplied expression(s) are unique and non-NULL."
argument_list|)
specifier|public
class|class
name|GenericUDAFCount
implements|implements
name|GenericUDAFResolver2
block|{
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
comment|// This method implementation is preserved for backward compatibility.
return|return
operator|new
name|GenericUDAFCountEvaluator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|GenericUDAFEvaluator
name|getEvaluator
parameter_list|(
name|GenericUDAFParameterInfo
name|paramInfo
parameter_list|)
throws|throws
name|SemanticException
block|{
name|TypeInfo
index|[]
name|parameters
init|=
name|paramInfo
operator|.
name|getParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|parameters
operator|.
name|length
operator|==
literal|0
condition|)
block|{
if|if
condition|(
operator|!
name|paramInfo
operator|.
name|isAllColumns
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"Argument expected"
argument_list|)
throw|;
block|}
assert|assert
operator|!
name|paramInfo
operator|.
name|isDistinct
argument_list|()
operator|:
literal|"DISTINCT not supported with *"
assert|;
block|}
else|else
block|{
if|if
condition|(
name|parameters
operator|.
name|length
operator|>
literal|1
operator|&&
operator|!
name|paramInfo
operator|.
name|isDistinct
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentException
argument_list|(
literal|"DISTINCT keyword must be specified"
argument_list|)
throw|;
block|}
assert|assert
operator|!
name|paramInfo
operator|.
name|isAllColumns
argument_list|()
operator|:
literal|"* not supported in expression list"
assert|;
block|}
return|return
operator|new
name|GenericUDAFCountEvaluator
argument_list|()
operator|.
name|setCountAllColumns
argument_list|(
name|paramInfo
operator|.
name|isAllColumns
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * GenericUDAFCountEvaluator.    *    */
specifier|public
specifier|static
class|class
name|GenericUDAFCountEvaluator
extends|extends
name|GenericUDAFEvaluator
block|{
specifier|private
name|boolean
name|countAllColumns
init|=
literal|false
decl_stmt|;
specifier|private
name|LongObjectInspector
name|partialCountAggOI
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
name|super
operator|.
name|init
argument_list|(
name|m
argument_list|,
name|parameters
argument_list|)
expr_stmt|;
name|partialCountAggOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableLongObjectInspector
expr_stmt|;
name|result
operator|=
operator|new
name|LongWritable
argument_list|(
literal|0
argument_list|)
expr_stmt|;
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableLongObjectInspector
return|;
block|}
specifier|private
name|GenericUDAFCountEvaluator
name|setCountAllColumns
parameter_list|(
name|boolean
name|countAllCols
parameter_list|)
block|{
name|countAllColumns
operator|=
name|countAllCols
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** class for storing count value. */
specifier|static
class|class
name|CountAgg
implements|implements
name|AggregationBuffer
block|{
name|long
name|value
decl_stmt|;
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
name|CountAgg
name|buffer
init|=
operator|new
name|CountAgg
argument_list|()
decl_stmt|;
name|reset
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
return|return
name|buffer
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
name|CountAgg
operator|)
name|agg
operator|)
operator|.
name|value
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
if|if
condition|(
name|countAllColumns
condition|)
block|{
assert|assert
name|parameters
operator|.
name|length
operator|==
literal|0
assert|;
operator|(
operator|(
name|CountAgg
operator|)
name|agg
operator|)
operator|.
name|value
operator|++
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|parameters
operator|.
name|length
operator|>
literal|0
assert|;
name|boolean
name|countThisRow
init|=
literal|true
decl_stmt|;
for|for
control|(
name|Object
name|nextParam
range|:
name|parameters
control|)
block|{
if|if
condition|(
name|nextParam
operator|==
literal|null
condition|)
block|{
name|countThisRow
operator|=
literal|false
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|countThisRow
condition|)
block|{
operator|(
operator|(
name|CountAgg
operator|)
name|agg
operator|)
operator|.
name|value
operator|++
expr_stmt|;
block|}
block|}
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
name|long
name|p
init|=
name|partialCountAggOI
operator|.
name|get
argument_list|(
name|partial
argument_list|)
decl_stmt|;
operator|(
operator|(
name|CountAgg
operator|)
name|agg
operator|)
operator|.
name|value
operator|+=
name|p
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
name|result
operator|.
name|set
argument_list|(
operator|(
operator|(
name|CountAgg
operator|)
name|agg
operator|)
operator|.
name|value
argument_list|)
expr_stmt|;
return|return
name|result
return|;
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
block|}
block|}
end_class

end_unit

