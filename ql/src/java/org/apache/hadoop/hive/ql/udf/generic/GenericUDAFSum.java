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
name|common
operator|.
name|type
operator|.
name|HiveDecimal
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
name|io
operator|.
name|HiveDecimalWritable
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
name|DecimalTypeInfo
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
name|PrimitiveTypeInfo
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
name|TypeInfoFactory
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
comment|/**  * GenericUDAFSum.  *  */
end_comment

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"sum"
argument_list|,
name|value
operator|=
literal|"_FUNC_(x) - Returns the sum of a set of numbers"
argument_list|)
specifier|public
class|class
name|GenericUDAFSum
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
name|GenericUDAFSum
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
if|if
condition|(
name|parameters
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
name|parameters
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" is passed."
argument_list|)
throw|;
block|}
switch|switch
condition|(
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|parameters
index|[
literal|0
index|]
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
return|return
operator|new
name|GenericUDAFSumLong
argument_list|()
return|;
case|case
name|TIMESTAMP
case|:
case|case
name|FLOAT
case|:
case|case
name|DOUBLE
case|:
case|case
name|STRING
case|:
case|case
name|VARCHAR
case|:
case|case
name|CHAR
case|:
return|return
operator|new
name|GenericUDAFSumDouble
argument_list|()
return|;
case|case
name|DECIMAL
case|:
return|return
operator|new
name|GenericUDAFSumHiveDecimal
argument_list|()
return|;
case|case
name|BOOLEAN
case|:
case|case
name|DATE
case|:
default|default:
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|0
argument_list|,
literal|"Only numeric or string type arguments are accepted but "
operator|+
name|parameters
index|[
literal|0
index|]
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" is passed."
argument_list|)
throw|;
block|}
block|}
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
name|TypeInfo
index|[]
name|parameters
init|=
name|info
operator|.
name|getParameters
argument_list|()
decl_stmt|;
name|GenericUDAFSumEvaluator
name|eval
init|=
operator|(
name|GenericUDAFSumEvaluator
operator|)
name|getEvaluator
argument_list|(
name|parameters
argument_list|)
decl_stmt|;
name|eval
operator|.
name|setSumDistinct
argument_list|(
name|info
operator|.
name|isDistinct
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|eval
return|;
block|}
specifier|public
specifier|static
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
name|getReturnType
parameter_list|(
name|TypeInfo
name|type
parameter_list|)
block|{
if|if
condition|(
name|type
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
return|return
literal|null
return|;
block|}
switch|switch
condition|(
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|type
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
condition|)
block|{
case|case
name|BYTE
case|:
case|case
name|SHORT
case|:
case|case
name|INT
case|:
case|case
name|LONG
case|:
return|return
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|LONG
return|;
case|case
name|TIMESTAMP
case|:
case|case
name|FLOAT
case|:
case|case
name|DOUBLE
case|:
case|case
name|STRING
case|:
case|case
name|VARCHAR
case|:
case|case
name|CHAR
case|:
return|return
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|DOUBLE
return|;
case|case
name|DECIMAL
case|:
return|return
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|DECIMAL
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * The base type for sum operator evaluator    *    */
specifier|public
specifier|static
specifier|abstract
class|class
name|GenericUDAFSumEvaluator
parameter_list|<
name|ResultType
parameter_list|>
extends|extends
name|GenericUDAFEvaluator
block|{
specifier|static
specifier|abstract
class|class
name|SumAgg
parameter_list|<
name|T
parameter_list|>
extends|extends
name|AbstractAggregationBuffer
block|{
name|boolean
name|empty
decl_stmt|;
name|T
name|sum
decl_stmt|;
name|Object
name|previousValue
init|=
literal|null
decl_stmt|;
block|}
specifier|protected
name|PrimitiveObjectInspector
name|inputOI
decl_stmt|;
specifier|protected
name|ObjectInspector
name|outputOI
decl_stmt|;
specifier|protected
name|ResultType
name|result
decl_stmt|;
specifier|protected
name|boolean
name|sumDistinct
decl_stmt|;
specifier|public
name|boolean
name|sumDistinct
parameter_list|()
block|{
return|return
name|sumDistinct
return|;
block|}
specifier|public
name|void
name|setSumDistinct
parameter_list|(
name|boolean
name|sumDistinct
parameter_list|)
block|{
name|this
operator|.
name|sumDistinct
operator|=
name|sumDistinct
expr_stmt|;
block|}
comment|/**      * Check if the input object is the same as the previous one for the case of      * SUM(DISTINCT).      * @param input the input object      * @return True if sumDistinct is false or the input is different from the previous object      */
specifier|protected
name|boolean
name|checkDistinct
parameter_list|(
name|SumAgg
name|agg
parameter_list|,
name|Object
name|input
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|sumDistinct
operator|&&
name|ObjectInspectorUtils
operator|.
name|compare
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|,
name|agg
operator|.
name|previousValue
argument_list|,
name|outputOI
argument_list|)
operator|==
literal|0
condition|)
block|{
return|return
literal|false
return|;
block|}
name|agg
operator|.
name|previousValue
operator|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|input
argument_list|,
name|inputOI
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|JAVA
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
comment|/**    * GenericUDAFSumHiveDecimal.    *    */
specifier|public
specifier|static
class|class
name|GenericUDAFSumHiveDecimal
extends|extends
name|GenericUDAFSumEvaluator
argument_list|<
name|HiveDecimalWritable
argument_list|>
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
name|HiveDecimalWritable
argument_list|(
name|HiveDecimal
operator|.
name|ZERO
argument_list|)
expr_stmt|;
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
name|outputOI
operator|=
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|inputOI
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|JAVA
argument_list|)
expr_stmt|;
comment|// The output precision is 10 greater than the input which should cover at least
comment|// 10b rows. The scale is the same as the input.
name|DecimalTypeInfo
name|outputTypeInfo
init|=
literal|null
decl_stmt|;
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
name|int
name|precision
init|=
name|Math
operator|.
name|min
argument_list|(
name|HiveDecimal
operator|.
name|MAX_PRECISION
argument_list|,
name|inputOI
operator|.
name|precision
argument_list|()
operator|+
literal|10
argument_list|)
decl_stmt|;
name|outputTypeInfo
operator|=
name|TypeInfoFactory
operator|.
name|getDecimalTypeInfo
argument_list|(
name|precision
argument_list|,
name|inputOI
operator|.
name|scale
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|outputTypeInfo
operator|=
operator|(
name|DecimalTypeInfo
operator|)
name|inputOI
operator|.
name|getTypeInfo
argument_list|()
expr_stmt|;
block|}
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|getPrimitiveWritableObjectInspector
argument_list|(
name|outputTypeInfo
argument_list|)
return|;
block|}
comment|/** class for storing decimal sum value. */
annotation|@
name|AggregationType
argument_list|(
name|estimable
operator|=
literal|false
argument_list|)
comment|// hard to know exactly for decimals
specifier|static
class|class
name|SumHiveDecimalAgg
extends|extends
name|SumAgg
argument_list|<
name|HiveDecimal
argument_list|>
block|{     }
annotation|@
name|Override
specifier|public
name|AggregationBuffer
name|getNewAggregationBuffer
parameter_list|()
throws|throws
name|HiveException
block|{
name|SumHiveDecimalAgg
name|agg
init|=
operator|new
name|SumHiveDecimalAgg
argument_list|()
decl_stmt|;
name|reset
argument_list|(
name|agg
argument_list|)
expr_stmt|;
return|return
name|agg
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
name|SumAgg
argument_list|<
name|HiveDecimal
argument_list|>
name|bdAgg
init|=
operator|(
name|SumAgg
argument_list|<
name|HiveDecimal
argument_list|>
operator|)
name|agg
decl_stmt|;
name|bdAgg
operator|.
name|empty
operator|=
literal|true
expr_stmt|;
name|bdAgg
operator|.
name|sum
operator|=
name|HiveDecimal
operator|.
name|ZERO
expr_stmt|;
block|}
name|boolean
name|warned
init|=
literal|false
decl_stmt|;
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
try|try
block|{
if|if
condition|(
name|checkDistinct
argument_list|(
operator|(
name|SumAgg
operator|)
name|agg
argument_list|,
name|parameters
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
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
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|warned
condition|)
block|{
name|warned
operator|=
literal|true
expr_stmt|;
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
literal|" ignoring similar exceptions."
argument_list|)
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
name|SumHiveDecimalAgg
name|myagg
init|=
operator|(
name|SumHiveDecimalAgg
operator|)
name|agg
decl_stmt|;
if|if
condition|(
name|myagg
operator|.
name|sum
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|myagg
operator|.
name|empty
operator|=
literal|false
expr_stmt|;
name|myagg
operator|.
name|sum
operator|=
name|myagg
operator|.
name|sum
operator|.
name|add
argument_list|(
name|PrimitiveObjectInspectorUtils
operator|.
name|getHiveDecimal
argument_list|(
name|partial
argument_list|,
name|inputOI
argument_list|)
argument_list|)
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
name|SumHiveDecimalAgg
name|myagg
init|=
operator|(
name|SumHiveDecimalAgg
operator|)
name|agg
decl_stmt|;
if|if
condition|(
name|myagg
operator|.
name|empty
operator|||
name|myagg
operator|.
name|sum
operator|==
literal|null
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
annotation|@
name|Override
specifier|public
name|GenericUDAFEvaluator
name|getWindowingEvaluator
parameter_list|(
name|WindowFrameDef
name|wFrameDef
parameter_list|)
block|{
return|return
operator|new
name|GenericUDAFStreamingEvaluator
operator|.
name|SumAvgEnhancer
argument_list|<
name|HiveDecimalWritable
argument_list|,
name|HiveDecimal
argument_list|>
argument_list|(
name|this
argument_list|,
name|wFrameDef
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|HiveDecimalWritable
name|getNextResult
parameter_list|(
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
name|GenericUDAFStreamingEvaluator
operator|.
name|SumAvgEnhancer
argument_list|<
name|HiveDecimalWritable
argument_list|,
name|HiveDecimal
argument_list|>
operator|.
name|SumAvgStreamingState
name|ss
parameter_list|)
throws|throws
name|HiveException
block|{
name|SumHiveDecimalAgg
name|myagg
init|=
operator|(
name|SumHiveDecimalAgg
operator|)
name|ss
operator|.
name|wrappedBuf
decl_stmt|;
name|HiveDecimal
name|r
init|=
name|myagg
operator|.
name|empty
condition|?
literal|null
else|:
name|myagg
operator|.
name|sum
decl_stmt|;
name|HiveDecimal
name|d
init|=
name|ss
operator|.
name|retrieveNextIntermediateValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|d
operator|!=
literal|null
condition|)
block|{
name|r
operator|=
name|r
operator|==
literal|null
condition|?
literal|null
else|:
name|r
operator|.
name|subtract
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
return|return
name|r
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|HiveDecimalWritable
argument_list|(
name|r
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|HiveDecimal
name|getCurrentIntermediateResult
parameter_list|(
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
name|GenericUDAFStreamingEvaluator
operator|.
name|SumAvgEnhancer
argument_list|<
name|HiveDecimalWritable
argument_list|,
name|HiveDecimal
argument_list|>
operator|.
name|SumAvgStreamingState
name|ss
parameter_list|)
throws|throws
name|HiveException
block|{
name|SumHiveDecimalAgg
name|myagg
init|=
operator|(
name|SumHiveDecimalAgg
operator|)
name|ss
operator|.
name|wrappedBuf
decl_stmt|;
return|return
name|myagg
operator|.
name|empty
condition|?
literal|null
else|:
name|myagg
operator|.
name|sum
return|;
block|}
block|}
return|;
block|}
block|}
comment|/**    * GenericUDAFSumDouble.    *    */
specifier|public
specifier|static
class|class
name|GenericUDAFSumDouble
extends|extends
name|GenericUDAFSumEvaluator
argument_list|<
name|DoubleWritable
argument_list|>
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
name|DoubleWritable
argument_list|(
literal|0
argument_list|)
expr_stmt|;
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
name|outputOI
operator|=
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|inputOI
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|JAVA
argument_list|)
expr_stmt|;
return|return
name|PrimitiveObjectInspectorFactory
operator|.
name|writableDoubleObjectInspector
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
name|SumDoubleAgg
extends|extends
name|SumAgg
argument_list|<
name|Double
argument_list|>
block|{
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
name|SumDoubleAgg
name|result
init|=
operator|new
name|SumDoubleAgg
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
name|SumDoubleAgg
name|myagg
init|=
operator|(
name|SumDoubleAgg
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
literal|0.0
expr_stmt|;
block|}
name|boolean
name|warned
init|=
literal|false
decl_stmt|;
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
try|try
block|{
if|if
condition|(
name|checkDistinct
argument_list|(
operator|(
name|SumAgg
operator|)
name|agg
argument_list|,
name|parameters
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
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
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|warned
condition|)
block|{
name|warned
operator|=
literal|true
expr_stmt|;
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
literal|" ignoring similar exceptions."
argument_list|)
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
name|SumDoubleAgg
name|myagg
init|=
operator|(
name|SumDoubleAgg
operator|)
name|agg
decl_stmt|;
name|myagg
operator|.
name|empty
operator|=
literal|false
expr_stmt|;
name|myagg
operator|.
name|sum
operator|+=
name|PrimitiveObjectInspectorUtils
operator|.
name|getDouble
argument_list|(
name|partial
argument_list|,
name|inputOI
argument_list|)
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
name|SumDoubleAgg
name|myagg
init|=
operator|(
name|SumDoubleAgg
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
annotation|@
name|Override
specifier|public
name|GenericUDAFEvaluator
name|getWindowingEvaluator
parameter_list|(
name|WindowFrameDef
name|wFrameDef
parameter_list|)
block|{
return|return
operator|new
name|GenericUDAFStreamingEvaluator
operator|.
name|SumAvgEnhancer
argument_list|<
name|DoubleWritable
argument_list|,
name|Double
argument_list|>
argument_list|(
name|this
argument_list|,
name|wFrameDef
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|DoubleWritable
name|getNextResult
parameter_list|(
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
name|GenericUDAFStreamingEvaluator
operator|.
name|SumAvgEnhancer
argument_list|<
name|DoubleWritable
argument_list|,
name|Double
argument_list|>
operator|.
name|SumAvgStreamingState
name|ss
parameter_list|)
throws|throws
name|HiveException
block|{
name|SumDoubleAgg
name|myagg
init|=
operator|(
name|SumDoubleAgg
operator|)
name|ss
operator|.
name|wrappedBuf
decl_stmt|;
name|Double
name|r
init|=
name|myagg
operator|.
name|empty
condition|?
literal|null
else|:
name|myagg
operator|.
name|sum
decl_stmt|;
name|Double
name|d
init|=
name|ss
operator|.
name|retrieveNextIntermediateValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|d
operator|!=
literal|null
condition|)
block|{
name|r
operator|=
name|r
operator|==
literal|null
condition|?
literal|null
else|:
name|r
operator|-
name|d
expr_stmt|;
block|}
return|return
name|r
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|DoubleWritable
argument_list|(
name|r
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Double
name|getCurrentIntermediateResult
parameter_list|(
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
name|GenericUDAFStreamingEvaluator
operator|.
name|SumAvgEnhancer
argument_list|<
name|DoubleWritable
argument_list|,
name|Double
argument_list|>
operator|.
name|SumAvgStreamingState
name|ss
parameter_list|)
throws|throws
name|HiveException
block|{
name|SumDoubleAgg
name|myagg
init|=
operator|(
name|SumDoubleAgg
operator|)
name|ss
operator|.
name|wrappedBuf
decl_stmt|;
return|return
name|myagg
operator|.
name|empty
condition|?
literal|null
else|:
operator|new
name|Double
argument_list|(
name|myagg
operator|.
name|sum
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
comment|/**    * GenericUDAFSumLong.    *    */
specifier|public
specifier|static
class|class
name|GenericUDAFSumLong
extends|extends
name|GenericUDAFSumEvaluator
argument_list|<
name|LongWritable
argument_list|>
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
name|outputOI
operator|=
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|inputOI
argument_list|,
name|ObjectInspectorCopyOption
operator|.
name|JAVA
argument_list|)
expr_stmt|;
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
name|SumAgg
argument_list|<
name|Long
argument_list|>
block|{
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
literal|0L
expr_stmt|;
block|}
specifier|private
name|boolean
name|warned
init|=
literal|false
decl_stmt|;
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
try|try
block|{
if|if
condition|(
name|checkDistinct
argument_list|(
operator|(
name|SumAgg
operator|)
name|agg
argument_list|,
name|parameters
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
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
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|warned
condition|)
block|{
name|warned
operator|=
literal|true
expr_stmt|;
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
annotation|@
name|Override
specifier|public
name|GenericUDAFEvaluator
name|getWindowingEvaluator
parameter_list|(
name|WindowFrameDef
name|wFrameDef
parameter_list|)
block|{
return|return
operator|new
name|GenericUDAFStreamingEvaluator
operator|.
name|SumAvgEnhancer
argument_list|<
name|LongWritable
argument_list|,
name|Long
argument_list|>
argument_list|(
name|this
argument_list|,
name|wFrameDef
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|LongWritable
name|getNextResult
parameter_list|(
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
name|GenericUDAFStreamingEvaluator
operator|.
name|SumAvgEnhancer
argument_list|<
name|LongWritable
argument_list|,
name|Long
argument_list|>
operator|.
name|SumAvgStreamingState
name|ss
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
name|ss
operator|.
name|wrappedBuf
decl_stmt|;
name|Long
name|r
init|=
name|myagg
operator|.
name|empty
condition|?
literal|null
else|:
name|myagg
operator|.
name|sum
decl_stmt|;
name|Long
name|d
init|=
name|ss
operator|.
name|retrieveNextIntermediateValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|d
operator|!=
literal|null
condition|)
block|{
name|r
operator|=
name|r
operator|==
literal|null
condition|?
literal|null
else|:
name|r
operator|-
name|d
expr_stmt|;
block|}
return|return
name|r
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|LongWritable
argument_list|(
name|r
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|Long
name|getCurrentIntermediateResult
parameter_list|(
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
name|GenericUDAFStreamingEvaluator
operator|.
name|SumAvgEnhancer
argument_list|<
name|LongWritable
argument_list|,
name|Long
argument_list|>
operator|.
name|SumAvgStreamingState
name|ss
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
name|ss
operator|.
name|wrappedBuf
decl_stmt|;
return|return
name|myagg
operator|.
name|empty
condition|?
literal|null
else|:
operator|new
name|Long
argument_list|(
name|myagg
operator|.
name|sum
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
block|}
end_class

end_unit

