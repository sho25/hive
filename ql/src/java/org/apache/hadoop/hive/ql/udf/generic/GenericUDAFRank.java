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
name|ObjectInspectorUtils
operator|.
name|ObjectInspectorCopyOption
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
literal|"_FUNC_(x)"
argument_list|)
argument_list|,
name|supportsWindow
operator|=
literal|false
argument_list|,
name|pivotResult
operator|=
literal|true
argument_list|,
name|rankingFunction
operator|=
literal|true
argument_list|,
name|impliesOrder
operator|=
literal|true
argument_list|)
specifier|public
class|class
name|GenericUDAFRank
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
name|GenericUDAFRank
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
operator|<
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
literal|"One or more arguments are expected."
argument_list|)
throw|;
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
name|parameters
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ObjectInspector
name|oi
init|=
name|TypeInfoUtils
operator|.
name|getStandardJavaObjectInspectorFromTypeInfo
argument_list|(
name|parameters
index|[
name|i
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
name|i
argument_list|,
literal|"Cannot support comparison of map<> type or complex type containing map<>."
argument_list|)
throw|;
block|}
block|}
return|return
name|createEvaluator
argument_list|()
return|;
block|}
specifier|protected
name|GenericUDAFAbstractRankEvaluator
name|createEvaluator
parameter_list|()
block|{
return|return
operator|new
name|GenericUDAFRankEvaluator
argument_list|()
return|;
block|}
specifier|static
class|class
name|RankBuffer
implements|implements
name|AggregationBuffer
block|{
name|ArrayList
argument_list|<
name|IntWritable
argument_list|>
name|rowNums
decl_stmt|;
name|int
name|currentRowNum
decl_stmt|;
name|Object
index|[]
name|currVal
decl_stmt|;
name|int
name|currentRank
decl_stmt|;
name|int
name|numParams
decl_stmt|;
name|boolean
name|supportsStreaming
decl_stmt|;
name|RankBuffer
parameter_list|(
name|int
name|numParams
parameter_list|,
name|boolean
name|supportsStreaming
parameter_list|)
block|{
name|this
operator|.
name|numParams
operator|=
name|numParams
expr_stmt|;
name|this
operator|.
name|supportsStreaming
operator|=
name|supportsStreaming
expr_stmt|;
name|init
argument_list|()
expr_stmt|;
block|}
name|void
name|init
parameter_list|()
block|{
name|rowNums
operator|=
operator|new
name|ArrayList
argument_list|<
name|IntWritable
argument_list|>
argument_list|()
expr_stmt|;
name|currentRowNum
operator|=
literal|0
expr_stmt|;
name|currentRank
operator|=
literal|0
expr_stmt|;
name|currVal
operator|=
operator|new
name|Object
index|[
name|numParams
index|]
expr_stmt|;
if|if
condition|(
name|supportsStreaming
condition|)
block|{
comment|/* initialize rowNums to have 1 row */
name|rowNums
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|incrRowNum
parameter_list|()
block|{
name|currentRowNum
operator|++
expr_stmt|;
block|}
name|void
name|addRank
parameter_list|()
block|{
if|if
condition|(
name|supportsStreaming
condition|)
block|{
name|rowNums
operator|.
name|set
argument_list|(
literal|0
argument_list|,
operator|new
name|IntWritable
argument_list|(
name|currentRank
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rowNums
operator|.
name|add
argument_list|(
operator|new
name|IntWritable
argument_list|(
name|currentRank
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
specifier|abstract
class|class
name|GenericUDAFAbstractRankEvaluator
extends|extends
name|GenericUDAFEvaluator
block|{
name|ObjectInspector
index|[]
name|inputOI
decl_stmt|;
name|ObjectInspector
index|[]
name|outputOI
decl_stmt|;
name|boolean
name|isStreamingMode
init|=
literal|false
decl_stmt|;
specifier|protected
name|boolean
name|isStreaming
parameter_list|()
block|{
return|return
name|isStreamingMode
return|;
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
literal|"Only COMPLETE mode supported for Rank function"
argument_list|)
throw|;
block|}
name|inputOI
operator|=
name|parameters
expr_stmt|;
name|outputOI
operator|=
operator|new
name|ObjectInspector
index|[
name|inputOI
operator|.
name|length
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|inputOI
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|outputOI
index|[
name|i
index|]
operator|=
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|inputOI
index|[
name|i
index|]
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|JAVA
argument_list|)
expr_stmt|;
block|}
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
name|RankBuffer
argument_list|(
name|inputOI
operator|.
name|length
argument_list|,
name|isStreamingMode
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
operator|(
operator|(
name|RankBuffer
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
name|RankBuffer
name|rb
init|=
operator|(
name|RankBuffer
operator|)
name|agg
decl_stmt|;
name|int
name|c
init|=
name|GenericUDAFRank
operator|.
name|compare
argument_list|(
name|rb
operator|.
name|currVal
argument_list|,
name|outputOI
argument_list|,
name|parameters
argument_list|,
name|inputOI
argument_list|)
decl_stmt|;
name|rb
operator|.
name|incrRowNum
argument_list|()
expr_stmt|;
if|if
condition|(
name|rb
operator|.
name|currentRowNum
operator|==
literal|1
operator|||
name|c
operator|!=
literal|0
condition|)
block|{
name|nextRank
argument_list|(
name|rb
argument_list|)
expr_stmt|;
name|rb
operator|.
name|currVal
operator|=
name|GenericUDAFRank
operator|.
name|copyToStandardObject
argument_list|(
name|parameters
argument_list|,
name|inputOI
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|JAVA
argument_list|)
expr_stmt|;
block|}
name|rb
operator|.
name|addRank
argument_list|()
expr_stmt|;
block|}
comment|/*      * Called when the value in the partition has changed. Update the currentRank      */
specifier|protected
name|void
name|nextRank
parameter_list|(
name|RankBuffer
name|rb
parameter_list|)
block|{
name|rb
operator|.
name|currentRank
operator|=
name|rb
operator|.
name|currentRowNum
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
return|return
operator|(
operator|(
name|RankBuffer
operator|)
name|agg
operator|)
operator|.
name|rowNums
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|GenericUDAFRankEvaluator
extends|extends
name|GenericUDAFAbstractRankEvaluator
implements|implements
name|ISupportStreamingModeForWindowing
block|{
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
return|return
operator|(
operator|(
name|RankBuffer
operator|)
name|agg
operator|)
operator|.
name|rowNums
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|GenericUDAFEvaluator
name|getWindowingEvaluator
parameter_list|(
name|WindowFrameDef
name|wFrmDef
parameter_list|)
block|{
name|isStreamingMode
operator|=
literal|true
expr_stmt|;
return|return
name|this
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
return|return
literal|0
return|;
block|}
block|}
specifier|public
specifier|static
name|int
name|compare
parameter_list|(
name|Object
index|[]
name|o1
parameter_list|,
name|ObjectInspector
index|[]
name|oi1
parameter_list|,
name|Object
index|[]
name|o2
parameter_list|,
name|ObjectInspector
index|[]
name|oi2
parameter_list|)
block|{
name|int
name|c
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
name|oi1
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|c
operator|=
name|ObjectInspectorUtils
operator|.
name|compare
argument_list|(
name|o1
index|[
name|i
index|]
argument_list|,
name|oi1
index|[
name|i
index|]
argument_list|,
name|o2
index|[
name|i
index|]
argument_list|,
name|oi2
index|[
name|i
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
block|{
return|return
name|c
return|;
block|}
block|}
return|return
name|c
return|;
block|}
specifier|public
specifier|static
name|Object
index|[]
name|copyToStandardObject
parameter_list|(
name|Object
index|[]
name|o
parameter_list|,
name|ObjectInspector
index|[]
name|oi
parameter_list|,
name|ObjectInspectorCopyOption
name|objectInspectorOption
parameter_list|)
block|{
name|Object
index|[]
name|out
init|=
operator|new
name|Object
index|[
name|o
operator|.
name|length
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
name|oi
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|out
index|[
name|i
index|]
operator|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|o
index|[
name|i
index|]
argument_list|,
name|oi
index|[
name|i
index|]
argument_list|,
name|objectInspectorOption
argument_list|)
expr_stmt|;
block|}
return|return
name|out
return|;
block|}
block|}
end_class

end_unit

