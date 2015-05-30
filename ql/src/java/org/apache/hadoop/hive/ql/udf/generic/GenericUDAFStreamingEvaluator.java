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
name|ObjectInspector
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"deprecation"
block|,
literal|"unchecked"
block|}
argument_list|)
specifier|public
specifier|abstract
class|class
name|GenericUDAFStreamingEvaluator
parameter_list|<
name|T1
parameter_list|>
extends|extends
name|GenericUDAFEvaluator
implements|implements
name|ISupportStreamingModeForWindowing
block|{
specifier|protected
specifier|final
name|GenericUDAFEvaluator
name|wrappedEval
decl_stmt|;
specifier|protected
specifier|final
name|WindowFrameDef
name|wFrameDef
decl_stmt|;
specifier|public
name|GenericUDAFStreamingEvaluator
parameter_list|(
name|GenericUDAFEvaluator
name|wrappedEval
parameter_list|,
name|WindowFrameDef
name|wFrameDef
parameter_list|)
block|{
name|this
operator|.
name|wrappedEval
operator|=
name|wrappedEval
expr_stmt|;
name|this
operator|.
name|wFrameDef
operator|=
name|wFrameDef
expr_stmt|;
name|this
operator|.
name|mode
operator|=
name|wrappedEval
operator|.
name|mode
expr_stmt|;
block|}
class|class
name|StreamingState
extends|extends
name|AbstractAggregationBuffer
block|{
specifier|final
name|AggregationBuffer
name|wrappedBuf
decl_stmt|;
specifier|final
name|List
argument_list|<
name|T1
argument_list|>
name|results
decl_stmt|;
comment|// Hold the aggregation results for each row in the partition
name|int
name|numRows
decl_stmt|;
comment|// Number of rows processed in the partition.
name|StreamingState
parameter_list|(
name|AggregationBuffer
name|buf
parameter_list|)
block|{
name|this
operator|.
name|wrappedBuf
operator|=
name|buf
expr_stmt|;
name|results
operator|=
operator|new
name|ArrayList
argument_list|<
name|T1
argument_list|>
argument_list|()
expr_stmt|;
name|numRows
operator|=
literal|0
expr_stmt|;
block|}
specifier|protected
name|void
name|reset
parameter_list|()
block|{
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
name|numRows
operator|=
literal|0
expr_stmt|;
block|}
block|}
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
return|return
name|wrappedEval
operator|.
name|init
argument_list|(
name|m
argument_list|,
name|parameters
argument_list|)
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
name|StreamingState
name|ss
init|=
operator|(
name|StreamingState
operator|)
name|agg
decl_stmt|;
name|wrappedEval
operator|.
name|reset
argument_list|(
name|ss
operator|.
name|wrappedBuf
argument_list|)
expr_stmt|;
name|ss
operator|.
name|reset
argument_list|()
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
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|": terminatePartial not supported"
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
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|": merge not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|getNextResult
parameter_list|(
name|AggregationBuffer
name|agg
parameter_list|)
throws|throws
name|HiveException
block|{
name|StreamingState
name|ss
init|=
operator|(
name|StreamingState
operator|)
name|agg
decl_stmt|;
if|if
condition|(
operator|!
name|ss
operator|.
name|results
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|T1
name|res
init|=
name|ss
operator|.
name|results
operator|.
name|remove
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|==
literal|null
condition|)
block|{
return|return
name|ISupportStreamingModeForWindowing
operator|.
name|NULL_RESULT
return|;
block|}
return|return
name|res
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
specifier|static
specifier|abstract
class|class
name|SumAvgEnhancer
parameter_list|<
name|T1
parameter_list|,
name|T2
parameter_list|>
extends|extends
name|GenericUDAFStreamingEvaluator
argument_list|<
name|T1
argument_list|>
block|{
specifier|public
name|SumAvgEnhancer
parameter_list|(
name|GenericUDAFEvaluator
name|wrappedEval
parameter_list|,
name|WindowFrameDef
name|wFrameDef
parameter_list|)
block|{
name|super
argument_list|(
name|wrappedEval
argument_list|,
name|wFrameDef
argument_list|)
expr_stmt|;
block|}
class|class
name|SumAvgStreamingState
extends|extends
name|StreamingState
block|{
specifier|final
name|List
argument_list|<
name|T2
argument_list|>
name|intermediateVals
decl_stmt|;
comment|// Keep track of S[0..x]
name|SumAvgStreamingState
parameter_list|(
name|AggregationBuffer
name|buf
parameter_list|)
block|{
name|super
argument_list|(
name|buf
argument_list|)
expr_stmt|;
name|intermediateVals
operator|=
operator|new
name|ArrayList
argument_list|<
name|T2
argument_list|>
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|estimate
parameter_list|()
block|{
if|if
condition|(
operator|!
operator|(
name|wrappedBuf
operator|instanceof
name|AbstractAggregationBuffer
operator|)
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|int
name|underlying
init|=
operator|(
operator|(
name|AbstractAggregationBuffer
operator|)
name|wrappedBuf
operator|)
operator|.
name|estimate
argument_list|()
decl_stmt|;
if|if
condition|(
name|underlying
operator|==
operator|-
literal|1
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|wFrameDef
operator|.
name|isStartUnbounded
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
comment|/*          * sz Estimate = sz needed by underlying AggBuffer + sz for results + sz          * for intermediates + 3 * JavaDataModel.PRIMITIVES1 sz of results = sz          * of underlying * wdwSz sz of intermediates = sz of underlying * wdwSz          */
name|int
name|wdwSz
init|=
name|wFrameDef
operator|.
name|getWindowSize
argument_list|()
decl_stmt|;
return|return
name|underlying
operator|+
operator|(
name|underlying
operator|*
name|wdwSz
operator|)
operator|+
operator|(
name|underlying
operator|*
name|wdwSz
operator|)
operator|+
operator|(
literal|3
operator|*
name|JavaDataModel
operator|.
name|PRIMITIVES1
operator|)
return|;
block|}
specifier|protected
name|void
name|reset
parameter_list|()
block|{
name|intermediateVals
operator|.
name|clear
argument_list|()
expr_stmt|;
name|super
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
comment|/**        * For the cases "X preceding and Y preceding" or the number of processed rows        * is more than the size of FOLLOWING window, we are able to generate a PTF result        * for a previous row.        * @return        */
specifier|public
name|boolean
name|hasResultReady
parameter_list|()
block|{
return|return
name|this
operator|.
name|numRows
operator|>=
name|wFrameDef
operator|.
name|getEnd
argument_list|()
operator|.
name|getRelativeOffset
argument_list|()
return|;
block|}
comment|/**        * Retrieve the next stored intermediate result, i.e.,        * Get S[x-1] in the computation of S[x..y] = S[y] - S[x-1].        */
specifier|public
name|T2
name|retrieveNextIntermediateValue
parameter_list|()
block|{
if|if
condition|(
operator|!
name|wFrameDef
operator|.
name|getStart
argument_list|()
operator|.
name|isUnbounded
argument_list|()
operator|&&
operator|!
name|this
operator|.
name|intermediateVals
operator|.
name|isEmpty
argument_list|()
operator|&&
name|this
operator|.
name|numRows
operator|>=
name|wFrameDef
operator|.
name|getWindowSize
argument_list|()
condition|)
block|{
return|return
name|this
operator|.
name|intermediateVals
operator|.
name|remove
argument_list|(
literal|0
argument_list|)
return|;
block|}
return|return
literal|null
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
name|AggregationBuffer
name|underlying
init|=
name|wrappedEval
operator|.
name|getNewAggregationBuffer
argument_list|()
decl_stmt|;
return|return
operator|new
name|SumAvgStreamingState
argument_list|(
name|underlying
argument_list|)
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
name|SumAvgStreamingState
name|ss
init|=
operator|(
name|SumAvgStreamingState
operator|)
name|agg
decl_stmt|;
name|wrappedEval
operator|.
name|iterate
argument_list|(
name|ss
operator|.
name|wrappedBuf
argument_list|,
name|parameters
argument_list|)
expr_stmt|;
comment|// We need to insert 'null' before processing first row for the case: X preceding and y preceding
if|if
condition|(
name|ss
operator|.
name|numRows
operator|==
literal|0
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
name|wFrameDef
operator|.
name|getEnd
argument_list|()
operator|.
name|getRelativeOffset
argument_list|()
init|;
name|i
operator|<
literal|0
condition|;
name|i
operator|++
control|)
block|{
name|ss
operator|.
name|results
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Generate the result for the windowing ending at the current row
if|if
condition|(
name|ss
operator|.
name|hasResultReady
argument_list|()
condition|)
block|{
name|ss
operator|.
name|results
operator|.
name|add
argument_list|(
name|getNextResult
argument_list|(
name|ss
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|wFrameDef
operator|.
name|isStartUnbounded
argument_list|()
operator|&&
name|ss
operator|.
name|numRows
operator|+
literal|1
operator|>=
name|wFrameDef
operator|.
name|getStart
argument_list|()
operator|.
name|getRelativeOffset
argument_list|()
condition|)
block|{
name|ss
operator|.
name|intermediateVals
operator|.
name|add
argument_list|(
name|getCurrentIntermediateResult
argument_list|(
name|ss
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ss
operator|.
name|numRows
operator|++
expr_stmt|;
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
name|SumAvgStreamingState
name|ss
init|=
operator|(
name|SumAvgStreamingState
operator|)
name|agg
decl_stmt|;
name|Object
name|o
init|=
name|wrappedEval
operator|.
name|terminate
argument_list|(
name|ss
operator|.
name|wrappedBuf
argument_list|)
decl_stmt|;
comment|// After all the rows are processed, continue to generate results for the rows that results haven't generated.
comment|// For the case: X following and Y following, process first Y-X results and then insert X nulls.
comment|// For the case X preceding and Y following, process Y results.
for|for
control|(
name|int
name|i
init|=
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
name|wFrameDef
operator|.
name|getStart
argument_list|()
operator|.
name|getRelativeOffset
argument_list|()
argument_list|)
init|;
name|i
operator|<
name|wFrameDef
operator|.
name|getEnd
argument_list|()
operator|.
name|getRelativeOffset
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ss
operator|.
name|results
operator|.
name|add
argument_list|(
name|getNextResult
argument_list|(
name|ss
argument_list|)
argument_list|)
expr_stmt|;
name|ss
operator|.
name|numRows
operator|++
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|wFrameDef
operator|.
name|getStart
argument_list|()
operator|.
name|getRelativeOffset
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ss
operator|.
name|results
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|ss
operator|.
name|numRows
operator|++
expr_stmt|;
block|}
return|return
name|o
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getRowsRemainingAfterTerminate
parameter_list|()
throws|throws
name|HiveException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|protected
specifier|abstract
name|T1
name|getNextResult
parameter_list|(
name|SumAvgStreamingState
name|ss
parameter_list|)
throws|throws
name|HiveException
function_decl|;
specifier|protected
specifier|abstract
name|T2
name|getCurrentIntermediateResult
parameter_list|(
name|SumAvgStreamingState
name|ss
parameter_list|)
throws|throws
name|HiveException
function_decl|;
block|}
block|}
end_class

end_unit

