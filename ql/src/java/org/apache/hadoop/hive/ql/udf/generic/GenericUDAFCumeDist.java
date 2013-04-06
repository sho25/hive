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
name|serde2
operator|.
name|io
operator|.
name|DoubleWritable
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
literal|"cume_dist"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x) - The CUME_DIST function (defined as the inverse of percentile in some "
operator|+
literal|"statistical books) computes the position of a specified value relative to a set of values. "
operator|+
literal|"To compute the CUME_DIST of a value x in a set S of size N, you use the formula: "
operator|+
literal|"CUME_DIST(x) =  number of values in S coming before "
operator|+
literal|"   and including x in the specified order/ N"
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
name|GenericUDAFCumeDist
extends|extends
name|GenericUDAFRank
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
name|GenericUDAFCumeDist
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|GenericUDAFRankEvaluator
name|createEvaluator
parameter_list|()
block|{
return|return
operator|new
name|GenericUDAFCumeDistEvaluator
argument_list|()
return|;
block|}
specifier|public
specifier|static
class|class
name|GenericUDAFCumeDistEvaluator
extends|extends
name|GenericUDAFRankEvaluator
block|{
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
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
argument_list|)
return|;
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
name|List
argument_list|<
name|IntWritable
argument_list|>
name|ranks
init|=
operator|(
operator|(
name|RankBuffer
operator|)
name|agg
operator|)
operator|.
name|rowNums
decl_stmt|;
name|int
name|ranksSize
init|=
name|ranks
operator|.
name|size
argument_list|()
decl_stmt|;
name|double
name|ranksSizeDouble
init|=
name|ranksSize
decl_stmt|;
name|List
argument_list|<
name|DoubleWritable
argument_list|>
name|distances
init|=
operator|new
name|ArrayList
argument_list|<
name|DoubleWritable
argument_list|>
argument_list|(
name|ranksSize
argument_list|)
decl_stmt|;
name|int
name|last
init|=
operator|-
literal|1
decl_stmt|;
name|int
name|current
init|=
operator|-
literal|1
decl_stmt|;
comment|// tracks the number of elements with the same rank at the current time
name|int
name|elementsAtRank
init|=
literal|1
decl_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|ranksSize
condition|;
name|index
operator|++
control|)
block|{
name|current
operator|=
name|ranks
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
name|index
operator|==
literal|0
condition|)
block|{
name|last
operator|=
name|current
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|last
operator|==
name|current
condition|)
block|{
name|elementsAtRank
operator|++
expr_stmt|;
block|}
else|else
block|{
name|last
operator|=
name|current
expr_stmt|;
name|double
name|distance
init|=
operator|(
operator|(
name|double
operator|)
name|index
operator|)
operator|/
name|ranksSizeDouble
decl_stmt|;
while|while
condition|(
name|elementsAtRank
operator|--
operator|>
literal|0
condition|)
block|{
name|distances
operator|.
name|add
argument_list|(
operator|new
name|DoubleWritable
argument_list|(
name|distance
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|elementsAtRank
operator|=
literal|1
expr_stmt|;
block|}
block|}
if|if
condition|(
name|ranksSize
operator|>
literal|0
operator|&&
name|last
operator|==
name|current
condition|)
block|{
name|double
name|distance
init|=
operator|(
operator|(
name|double
operator|)
name|ranksSize
operator|)
operator|/
name|ranksSizeDouble
decl_stmt|;
while|while
condition|(
name|elementsAtRank
operator|--
operator|>
literal|0
condition|)
block|{
name|distances
operator|.
name|add
argument_list|(
operator|new
name|DoubleWritable
argument_list|(
name|distance
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|distances
return|;
block|}
block|}
block|}
end_class

end_unit

