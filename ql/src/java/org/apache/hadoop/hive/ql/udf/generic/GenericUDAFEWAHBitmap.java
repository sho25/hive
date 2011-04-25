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
name|io
operator|.
name|IOException
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
name|javaewah
operator|.
name|EWAHCompressedBitmap
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
name|index
operator|.
name|bitmap
operator|.
name|BitmapObjectInput
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
name|index
operator|.
name|bitmap
operator|.
name|BitmapObjectOutput
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
name|StandardListObjectInspector
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
name|LongWritable
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
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * GenericUDAFEWAHBitmap.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"ewah_bitmap"
argument_list|,
name|value
operator|=
literal|"_FUNC_(expr) - Returns an EWAH-compressed bitmap representation of a column."
argument_list|)
specifier|public
class|class
name|GenericUDAFEWAHBitmap
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
name|GenericUDAFEWAHBitmap
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
if|if
condition|(
operator|!
name|ObjectInspectorUtils
operator|.
name|compareSupported
argument_list|(
name|oi
argument_list|)
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
literal|"Cannot support comparison of map<> type or complex type containing map<>."
argument_list|)
throw|;
block|}
return|return
operator|new
name|GenericUDAFEWAHBitmapEvaluator
argument_list|()
return|;
block|}
comment|//The UDAF evaluator assumes that all rows it's evaluating have
comment|//the same (desired) value.
specifier|public
specifier|static
class|class
name|GenericUDAFEWAHBitmapEvaluator
extends|extends
name|GenericUDAFEvaluator
block|{
comment|// For PARTIAL1 and COMPLETE: ObjectInspectors for original data
specifier|private
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
specifier|private
name|LongObjectInspector
name|bitmapLongOI
decl_stmt|;
comment|// For PARTIAL2 and FINAL: ObjectInspectors for partial aggregations
comment|// (lists of bitmaps)
specifier|private
name|StandardListObjectInspector
name|loi
decl_stmt|;
specifier|private
name|StandardListObjectInspector
name|internalMergeOI
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
comment|// init output object inspectors
comment|// The output of a partial aggregation is a list
if|if
condition|(
name|m
operator|==
name|Mode
operator|.
name|PARTIAL1
condition|)
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
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableLongObjectInspector
argument_list|)
return|;
block|}
else|else
block|{
comment|//no map aggregation
name|internalMergeOI
operator|=
operator|(
name|StandardListObjectInspector
operator|)
name|parameters
index|[
literal|0
index|]
expr_stmt|;
name|bitmapLongOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableLongObjectInspector
expr_stmt|;
name|inputOI
operator|=
name|PrimitiveObjectInspectorFactory
operator|.
name|writableByteObjectInspector
expr_stmt|;
name|loi
operator|=
operator|(
name|StandardListObjectInspector
operator|)
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableLongObjectInspector
argument_list|)
expr_stmt|;
return|return
name|loi
return|;
block|}
block|}
comment|/** class for storing the current partial result aggregation */
specifier|static
class|class
name|BitmapAgg
implements|implements
name|AggregationBuffer
block|{
name|EWAHCompressedBitmap
name|bitmap
decl_stmt|;
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
name|BitmapAgg
operator|)
name|agg
operator|)
operator|.
name|bitmap
operator|=
operator|new
name|EWAHCompressedBitmap
argument_list|()
expr_stmt|;
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
name|BitmapAgg
name|result
init|=
operator|new
name|BitmapAgg
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
name|Object
name|p
init|=
name|parameters
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|p
operator|!=
literal|null
condition|)
block|{
name|BitmapAgg
name|myagg
init|=
operator|(
name|BitmapAgg
operator|)
name|agg
decl_stmt|;
try|try
block|{
name|int
name|row
init|=
name|PrimitiveObjectInspectorUtils
operator|.
name|getInt
argument_list|(
name|p
argument_list|,
name|inputOI
argument_list|)
decl_stmt|;
name|addBitmap
argument_list|(
name|row
argument_list|,
name|myagg
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|BitmapAgg
name|myagg
init|=
operator|(
name|BitmapAgg
operator|)
name|agg
decl_stmt|;
name|BitmapObjectOutput
name|bitmapObjOut
init|=
operator|new
name|BitmapObjectOutput
argument_list|()
decl_stmt|;
try|try
block|{
name|myagg
operator|.
name|bitmap
operator|.
name|writeExternal
argument_list|(
name|bitmapObjOut
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|bitmapObjOut
operator|.
name|list
argument_list|()
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
name|BitmapAgg
name|myagg
init|=
operator|(
name|BitmapAgg
operator|)
name|agg
decl_stmt|;
name|ArrayList
argument_list|<
name|LongWritable
argument_list|>
name|partialResult
init|=
operator|(
name|ArrayList
argument_list|<
name|LongWritable
argument_list|>
operator|)
name|internalMergeOI
operator|.
name|getList
argument_list|(
name|partial
argument_list|)
decl_stmt|;
name|BitmapObjectInput
name|bitmapObjIn
init|=
operator|new
name|BitmapObjectInput
argument_list|(
name|partialResult
argument_list|)
decl_stmt|;
name|EWAHCompressedBitmap
name|partialBitmap
init|=
operator|new
name|EWAHCompressedBitmap
argument_list|()
decl_stmt|;
try|try
block|{
name|partialBitmap
operator|.
name|readExternal
argument_list|(
name|bitmapObjIn
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|myagg
operator|.
name|bitmap
operator|=
name|myagg
operator|.
name|bitmap
operator|.
name|or
argument_list|(
name|partialBitmap
argument_list|)
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
name|BitmapAgg
name|myagg
init|=
operator|(
name|BitmapAgg
operator|)
name|agg
decl_stmt|;
name|BitmapObjectOutput
name|bitmapObjOut
init|=
operator|new
name|BitmapObjectOutput
argument_list|()
decl_stmt|;
try|try
block|{
name|myagg
operator|.
name|bitmap
operator|.
name|writeExternal
argument_list|(
name|bitmapObjOut
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|bitmapObjOut
operator|.
name|list
argument_list|()
return|;
block|}
specifier|private
name|void
name|addBitmap
parameter_list|(
name|int
name|newRow
parameter_list|,
name|BitmapAgg
name|myagg
parameter_list|)
block|{
if|if
condition|(
operator|!
name|myagg
operator|.
name|bitmap
operator|.
name|set
argument_list|(
name|newRow
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Can't set bits out of order with EWAHCompressedBitmap"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

