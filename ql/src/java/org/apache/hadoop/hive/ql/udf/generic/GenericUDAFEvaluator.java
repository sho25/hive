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
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

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
name|lang
operator|.
name|annotation
operator|.
name|Retention
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|RetentionPolicy
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hive
operator|.
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|common
operator|.
name|classification
operator|.
name|InterfaceStability
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
name|MapredContext
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
name|PTFPartition
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
name|ptf
operator|.
name|PTFExpressionDef
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
name|ptf
operator|.
name|WindowFrameDef
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
name|UDFType
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
name|ptf
operator|.
name|BasePartitionEvaluator
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
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|AnnotationUtils
import|;
end_import

begin_comment
comment|/**  * A Generic User-defined aggregation function (GenericUDAF) for the use with  * Hive.  *  * New GenericUDAF classes need to inherit from this GenericUDAF class.  *  * The GenericUDAF are superior to normal UDAFs in the following ways: 1. It can  * accept arguments of complex types, and return complex types. 2. It can accept  * variable length of arguments. 3. It can accept an infinite number of function  * signature - for example, it's easy to write a GenericUDAF that accepts  * array&lt;int&gt;, array&lt;array&lt;int&gt;&gt; and so on (arbitrary levels of nesting).  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
annotation|@
name|UDFType
argument_list|(
name|deterministic
operator|=
literal|true
argument_list|)
specifier|public
specifier|abstract
class|class
name|GenericUDAFEvaluator
implements|implements
name|Closeable
block|{
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
annotation|@
name|Retention
argument_list|(
name|RetentionPolicy
operator|.
name|RUNTIME
argument_list|)
specifier|public
specifier|static
annotation_defn|@interface
name|AggregationType
block|{
name|boolean
name|estimable
parameter_list|()
default|default
literal|false
function_decl|;
block|}
specifier|public
specifier|static
name|boolean
name|isEstimable
parameter_list|(
name|AggregationBuffer
name|buffer
parameter_list|)
block|{
if|if
condition|(
name|buffer
operator|instanceof
name|AbstractAggregationBuffer
condition|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|AggregationBuffer
argument_list|>
name|clazz
init|=
name|buffer
operator|.
name|getClass
argument_list|()
decl_stmt|;
name|AggregationType
name|annotation
init|=
name|AnnotationUtils
operator|.
name|getAnnotation
argument_list|(
name|clazz
argument_list|,
name|AggregationType
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|annotation
operator|!=
literal|null
operator|&&
name|annotation
operator|.
name|estimable
argument_list|()
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Although similar to AbstractAggregationBuffer::estimate(), it differs from it in 2 aspects    * 1) This avoids creation of AggregationBuffer which may result in large memory allocation    * 2) This is used only while compiling query as oppose to AbstractAggregationBuffer version    * which may be used in both runtime as well as compile time.    * @return    */
specifier|public
name|int
name|estimate
parameter_list|()
block|{
return|return
operator|-
literal|1
return|;
block|}
comment|/**    * Mode.    *    */
specifier|public
specifier|static
enum|enum
name|Mode
block|{
comment|/**      * PARTIAL1: from original data to partial aggregation data: iterate() and      * terminatePartial() will be called.      */
name|PARTIAL1
block|,
comment|/**      * PARTIAL2: from partial aggregation data to partial aggregation data:      * merge() and terminatePartial() will be called.      */
name|PARTIAL2
block|,
comment|/**      * FINAL: from partial aggregation to full aggregation: merge() and      * terminate() will be called.      */
name|FINAL
block|,
comment|/**      * COMPLETE: from original data directly to full aggregation: iterate() and      * terminate() will be called.      */
name|COMPLETE
block|}
empty_stmt|;
name|Mode
name|mode
decl_stmt|;
comment|/**    * The constructor.    */
specifier|public
name|GenericUDAFEvaluator
parameter_list|()
block|{   }
comment|/**    * Additionally setup GenericUDAFEvaluator with MapredContext before initializing.    * This is only called in runtime of MapRedTask.    *    * @param mapredContext context    */
specifier|public
name|void
name|configure
parameter_list|(
name|MapredContext
name|mapredContext
parameter_list|)
block|{   }
comment|/**    * Initialize the evaluator.    *    * @param m    *          The mode of aggregation.    * @param parameters    *          The ObjectInspector for the parameters: In PARTIAL1 and COMPLETE    *          mode, the parameters are original data; In PARTIAL2 and FINAL    *          mode, the parameters are just partial aggregations (in that case,    *          the array will always have a single element).    * @return The ObjectInspector for the return value. In PARTIAL1 and PARTIAL2    *         mode, the ObjectInspector for the return value of    *         terminatePartial() call; In FINAL and COMPLETE mode, the    *         ObjectInspector for the return value of terminate() call.    *    *         NOTE: We need ObjectInspector[] (in addition to the TypeInfo[] in    *         GenericUDAFResolver) for 2 reasons: 1. ObjectInspector contains    *         more information than TypeInfo; and GenericUDAFEvaluator.init at    *         execution time. 2. We call GenericUDAFResolver.getEvaluator at    *         compilation time,    */
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
comment|// This function should be overriden in every sub class
comment|// And the sub class should call super.init(m, parameters) to get mode set.
name|mode
operator|=
name|m
expr_stmt|;
name|partitionEvaluator
operator|=
literal|null
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|/**    * The interface for a class that is used to store the aggregation result    * during the process of aggregation.    *    * We split this piece of data out because there can be millions of instances    * of this Aggregation in hash-based aggregation process, and it's very    * important to conserve memory.    *    * In the future, we may completely hide this class inside the Evaluator and    * use integer numbers to identify which aggregation we are looking at.    *    * @deprecated use {@link AbstractAggregationBuffer} instead    */
annotation|@
name|Deprecated
specifier|public
specifier|static
interface|interface
name|AggregationBuffer
block|{   }
empty_stmt|;
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
specifier|static
specifier|abstract
class|class
name|AbstractAggregationBuffer
implements|implements
name|AggregationBuffer
block|{
comment|/**      * Estimate the size of memory which is occupied by aggregation buffer.      * Currently, hive assumes that primitives types occupies 16 byte and java object has      * 64 byte overhead for each. For map, each entry also has 64 byte overhead.      */
specifier|public
name|int
name|estimate
parameter_list|()
block|{
return|return
operator|-
literal|1
return|;
block|}
block|}
comment|/**    * Get a new aggregation object.    */
specifier|public
specifier|abstract
name|AggregationBuffer
name|getNewAggregationBuffer
parameter_list|()
throws|throws
name|HiveException
function_decl|;
comment|/**    * Reset the aggregation. This is useful if we want to reuse the same    * aggregation.    */
specifier|public
specifier|abstract
name|void
name|reset
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Close GenericUDFEvaluator.    * This is only called in runtime of MapRedTask.    */
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{   }
comment|/**    * This function will be called by GroupByOperator when it sees a new input    * row.    *    * @param agg    *          The object to store the aggregation result.    * @param parameters    *          The row, can be inspected by the OIs passed in init().    */
specifier|public
name|void
name|aggregate
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
name|mode
operator|==
name|Mode
operator|.
name|PARTIAL1
operator|||
name|mode
operator|==
name|Mode
operator|.
name|COMPLETE
condition|)
block|{
name|iterate
argument_list|(
name|agg
argument_list|,
name|parameters
argument_list|)
expr_stmt|;
block|}
else|else
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
name|merge
argument_list|(
name|agg
argument_list|,
name|parameters
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This function will be called by GroupByOperator when it sees a new input    * row.    *    * @param agg    *          The object to store the aggregation result.    */
specifier|public
name|Object
name|evaluate
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|mode
operator|==
name|Mode
operator|.
name|PARTIAL1
operator|||
name|mode
operator|==
name|Mode
operator|.
name|PARTIAL2
condition|)
block|{
return|return
name|terminatePartial
argument_list|(
name|agg
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|terminate
argument_list|(
name|agg
argument_list|)
return|;
block|}
block|}
comment|/**    * Iterate through original data.    *    * @param parameters    *          The objects of parameters.    */
specifier|public
specifier|abstract
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
function_decl|;
comment|/**    * Get partial aggregation result.    *    * @return partial aggregation result.    */
specifier|public
specifier|abstract
name|Object
name|terminatePartial
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * Merge with partial aggregation result. NOTE: null might be passed in case    * there is no input data.    *    * @param partial    *          The partial aggregation result.    */
specifier|public
specifier|abstract
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
function_decl|;
comment|/**    * Get final aggregation result.    *    * @return final aggregation result.    */
specifier|public
specifier|abstract
name|Object
name|terminate
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
function_decl|;
comment|/**    * When evaluating an aggregates over a fixed Window, the naive way to compute    * results is to compute the aggregate for each row. But often there is a way    * to compute results in a more efficient manner. This method enables the    * basic evaluator to provide a function object that does the job in a more    * efficient manner.    *<p>    * This method is called after this Evaluator is initialized. The returned    * Function must be initialized. It is passed the 'window' of aggregation for    * each row.    *    * @param wFrmDef    *          the Window definition in play for this evaluation.    * @return null implies that this fn cannot be processed in Streaming mode. So    *         each row is evaluated independently.    */
specifier|public
name|GenericUDAFEvaluator
name|getWindowingEvaluator
parameter_list|(
name|WindowFrameDef
name|wFrmDef
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
comment|/**    * Optional information to add to expression string. Subclasses can override.    */
specifier|public
name|String
name|getExprString
parameter_list|()
block|{
return|return
literal|""
return|;
block|}
specifier|protected
name|BasePartitionEvaluator
name|partitionEvaluator
decl_stmt|;
comment|/**    * When evaluating an aggregates over a fixed Window, streaming is not possible    * especially for RANGE Window type. For such case, the whole partition data needs    * to be collected and then to evaluate the aggregates. The naive approach is to    * calculate a row range for each row and to perform the aggregates. For some    * functions, a better implementation can be used to reduce the calculation.    * Note: since the evaluator is reused across different partitions, AggregationBuffer    * needs reset before aggregating for the new partition in the implementation.    * @param winFrame    the Window definition in play for this evaluation.    * @param partition   the partition data    * @param parameters  the list of the expressions in the function    * @param outputOI    the output object inspector    * @param nullsLast   the nulls last configuration    * @return            the evaluator, default to BasePartitionEvaluator which    *                    implements the naive approach    */
specifier|public
specifier|final
name|BasePartitionEvaluator
name|getPartitionWindowingEvaluator
parameter_list|(
name|WindowFrameDef
name|winFrame
parameter_list|,
name|PTFPartition
name|partition
parameter_list|,
name|List
argument_list|<
name|PTFExpressionDef
argument_list|>
name|parameters
parameter_list|,
name|ObjectInspector
name|outputOI
parameter_list|,
name|boolean
name|nullsLast
parameter_list|)
block|{
if|if
condition|(
name|partitionEvaluator
operator|==
literal|null
condition|)
block|{
name|partitionEvaluator
operator|=
name|createPartitionEvaluator
argument_list|(
name|winFrame
argument_list|,
name|partition
argument_list|,
name|parameters
argument_list|,
name|outputOI
argument_list|,
name|nullsLast
argument_list|)
expr_stmt|;
block|}
return|return
name|partitionEvaluator
return|;
block|}
comment|/**    *  This class needs to be overridden by the child class to implement function    *  specific evaluator.    */
specifier|protected
name|BasePartitionEvaluator
name|createPartitionEvaluator
parameter_list|(
name|WindowFrameDef
name|winFrame
parameter_list|,
name|PTFPartition
name|partition
parameter_list|,
name|List
argument_list|<
name|PTFExpressionDef
argument_list|>
name|parameters
parameter_list|,
name|ObjectInspector
name|outputOI
parameter_list|,
name|boolean
name|nullsLast
parameter_list|)
block|{
return|return
operator|new
name|BasePartitionEvaluator
argument_list|(
name|this
argument_list|,
name|winFrame
argument_list|,
name|partition
argument_list|,
name|parameters
argument_list|,
name|outputOI
argument_list|,
name|nullsLast
argument_list|)
return|;
block|}
block|}
end_class

end_unit

