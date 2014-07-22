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
name|serde2
operator|.
name|objectinspector
operator|.
name|ConstantObjectInspector
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
name|ObjectInspectorConverters
operator|.
name|Converter
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
name|io
operator|.
name|IntWritable
import|;
end_import

begin_comment
comment|/**  * abstract class for Lead& lag UDAFs GenericUDAFLeadLag.  *  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|GenericUDAFLeadLag
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
name|GenericUDAFLead
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
name|parameters
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ObjectInspector
index|[]
name|paramOIs
init|=
name|parameters
operator|.
name|getParameterObjectInspectors
argument_list|()
decl_stmt|;
name|String
name|fNm
init|=
name|functionName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|paramOIs
operator|.
name|length
operator|>=
literal|1
operator|&&
name|paramOIs
operator|.
name|length
operator|<=
literal|3
operator|)
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
name|paramOIs
operator|.
name|length
operator|-
literal|1
argument_list|,
literal|"Incorrect invocation of "
operator|+
name|fNm
operator|+
literal|": _FUNC_(expr, amt, default)"
argument_list|)
throw|;
block|}
name|int
name|amt
init|=
literal|1
decl_stmt|;
if|if
condition|(
name|paramOIs
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|ObjectInspector
name|amtOI
init|=
name|paramOIs
index|[
literal|1
index|]
decl_stmt|;
if|if
condition|(
operator|!
name|ObjectInspectorUtils
operator|.
name|isConstantObjectInspector
argument_list|(
name|amtOI
argument_list|)
operator|||
operator|(
name|amtOI
operator|.
name|getCategory
argument_list|()
operator|!=
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
operator|)
operator|||
operator|(
operator|(
name|PrimitiveObjectInspector
operator|)
name|amtOI
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
operator|!=
name|PrimitiveObjectInspector
operator|.
name|PrimitiveCategory
operator|.
name|INT
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
name|fNm
operator|+
literal|" amount must be a integer value "
operator|+
name|amtOI
operator|.
name|getTypeName
argument_list|()
operator|+
literal|" was passed as parameter 1."
argument_list|)
throw|;
block|}
name|Object
name|o
init|=
operator|(
operator|(
name|ConstantObjectInspector
operator|)
name|amtOI
operator|)
operator|.
name|getWritableConstantValue
argument_list|()
decl_stmt|;
name|amt
operator|=
operator|(
operator|(
name|IntWritable
operator|)
name|o
operator|)
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
name|amt
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|UDFArgumentTypeException
argument_list|(
literal|1
argument_list|,
name|fNm
operator|+
literal|" amount can not be nagative. Specified: "
operator|+
name|amt
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|paramOIs
operator|.
name|length
operator|==
literal|3
condition|)
block|{
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|paramOIs
index|[
literal|2
index|]
argument_list|,
name|paramOIs
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
name|GenericUDAFLeadLagEvaluator
name|eval
init|=
name|createLLEvaluator
argument_list|()
decl_stmt|;
name|eval
operator|.
name|setAmt
argument_list|(
name|amt
argument_list|)
expr_stmt|;
return|return
name|eval
return|;
block|}
specifier|protected
specifier|abstract
name|String
name|functionName
parameter_list|()
function_decl|;
specifier|protected
specifier|abstract
name|GenericUDAFLeadLagEvaluator
name|createLLEvaluator
parameter_list|()
function_decl|;
specifier|public
specifier|static
specifier|abstract
class|class
name|GenericUDAFLeadLagEvaluator
extends|extends
name|GenericUDAFEvaluator
block|{
specifier|private
specifier|transient
name|ObjectInspector
index|[]
name|inputOI
decl_stmt|;
specifier|private
name|int
name|amt
decl_stmt|;
name|String
name|fnName
decl_stmt|;
specifier|private
specifier|transient
name|Converter
name|defaultValueConverter
decl_stmt|;
specifier|public
name|GenericUDAFLeadLagEvaluator
parameter_list|()
block|{     }
comment|/*      * used to initialize Streaming Evaluator.      */
specifier|protected
name|GenericUDAFLeadLagEvaluator
parameter_list|(
name|GenericUDAFLeadLagEvaluator
name|src
parameter_list|)
block|{
name|this
operator|.
name|inputOI
operator|=
name|src
operator|.
name|inputOI
expr_stmt|;
name|this
operator|.
name|amt
operator|=
name|src
operator|.
name|amt
expr_stmt|;
name|this
operator|.
name|fnName
operator|=
name|src
operator|.
name|fnName
expr_stmt|;
name|this
operator|.
name|defaultValueConverter
operator|=
name|src
operator|.
name|defaultValueConverter
expr_stmt|;
name|this
operator|.
name|mode
operator|=
name|src
operator|.
name|mode
expr_stmt|;
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
literal|"Only COMPLETE mode supported for "
operator|+
name|fnName
operator|+
literal|" function"
argument_list|)
throw|;
block|}
name|inputOI
operator|=
name|parameters
expr_stmt|;
if|if
condition|(
name|parameters
operator|.
name|length
operator|==
literal|3
condition|)
block|{
name|defaultValueConverter
operator|=
name|ObjectInspectorConverters
operator|.
name|getConverter
argument_list|(
name|parameters
index|[
literal|2
index|]
argument_list|,
name|parameters
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|ObjectInspectorFactory
operator|.
name|getStandardListObjectInspector
argument_list|(
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|)
argument_list|)
return|;
block|}
specifier|public
name|int
name|getAmt
parameter_list|()
block|{
return|return
name|amt
return|;
block|}
specifier|public
name|void
name|setAmt
parameter_list|(
name|int
name|amt
parameter_list|)
block|{
name|this
operator|.
name|amt
operator|=
name|amt
expr_stmt|;
block|}
specifier|public
name|String
name|getFnName
parameter_list|()
block|{
return|return
name|fnName
return|;
block|}
specifier|public
name|void
name|setFnName
parameter_list|(
name|String
name|fnName
parameter_list|)
block|{
name|this
operator|.
name|fnName
operator|=
name|fnName
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|LeadLagBuffer
name|getNewLLBuffer
parameter_list|()
throws|throws
name|HiveException
function_decl|;
annotation|@
name|Override
specifier|public
name|AggregationBuffer
name|getNewAggregationBuffer
parameter_list|()
throws|throws
name|HiveException
block|{
name|LeadLagBuffer
name|lb
init|=
name|getNewLLBuffer
argument_list|()
decl_stmt|;
name|lb
operator|.
name|initialize
argument_list|(
name|amt
argument_list|)
expr_stmt|;
return|return
name|lb
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
name|LeadLagBuffer
operator|)
name|agg
operator|)
operator|.
name|initialize
argument_list|(
name|amt
argument_list|)
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
name|Object
name|rowExprVal
init|=
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|parameters
index|[
literal|0
index|]
argument_list|,
name|inputOI
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|Object
name|defaultVal
init|=
name|parameters
operator|.
name|length
operator|>
literal|2
condition|?
name|ObjectInspectorUtils
operator|.
name|copyToStandardObject
argument_list|(
name|defaultValueConverter
operator|.
name|convert
argument_list|(
name|parameters
index|[
literal|2
index|]
argument_list|)
argument_list|,
name|inputOI
index|[
literal|0
index|]
argument_list|)
else|:
literal|null
decl_stmt|;
operator|(
operator|(
name|LeadLagBuffer
operator|)
name|agg
operator|)
operator|.
name|addRow
argument_list|(
name|rowExprVal
argument_list|,
name|defaultVal
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
name|LeadLagBuffer
operator|)
name|agg
operator|)
operator|.
name|terminate
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

