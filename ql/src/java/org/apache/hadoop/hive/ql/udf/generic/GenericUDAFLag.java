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
name|udf
operator|.
name|generic
operator|.
name|GenericUDAFLead
operator|.
name|GenericUDAFLeadEvaluator
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
name|GenericUDAFLead
operator|.
name|GenericUDAFLeadEvaluatorStreaming
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
name|GenericUDAFLead
operator|.
name|LeadBuffer
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
name|GenericUDAFLeadLag
operator|.
name|GenericUDAFLeadLagEvaluator
import|;
end_import

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"lag"
argument_list|,
name|value
operator|=
literal|"_FUNC_(expr, amt, default)"
argument_list|)
annotation|@
name|WindowFunctionDescription
argument_list|(
name|supportsWindow
operator|=
literal|false
argument_list|,
name|pivotResult
operator|=
literal|true
argument_list|,
name|impliesOrder
operator|=
literal|true
argument_list|)
specifier|public
class|class
name|GenericUDAFLag
extends|extends
name|GenericUDAFLeadLag
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
name|GenericUDAFLag
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
name|String
name|functionName
parameter_list|()
block|{
return|return
literal|"Lag"
return|;
block|}
annotation|@
name|Override
specifier|protected
name|GenericUDAFLeadLagEvaluator
name|createLLEvaluator
parameter_list|()
block|{
return|return
operator|new
name|GenericUDAFLagEvaluator
argument_list|()
return|;
block|}
specifier|public
specifier|static
class|class
name|GenericUDAFLagEvaluator
extends|extends
name|GenericUDAFLeadLagEvaluator
block|{
specifier|public
name|GenericUDAFLagEvaluator
parameter_list|()
block|{     }
comment|/*      * used to initialize Streaming Evaluator.      */
specifier|protected
name|GenericUDAFLagEvaluator
parameter_list|(
name|GenericUDAFLeadLagEvaluator
name|src
parameter_list|)
block|{
name|super
argument_list|(
name|src
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|LeadLagBuffer
name|getNewLLBuffer
parameter_list|()
throws|throws
name|HiveException
block|{
return|return
operator|new
name|LagBuffer
argument_list|()
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
return|return
operator|new
name|GenericUDAFLagEvaluatorStreaming
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
specifier|static
class|class
name|LagBuffer
implements|implements
name|LeadLagBuffer
block|{
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|values
decl_stmt|;
name|int
name|lagAmt
decl_stmt|;
name|ArrayList
argument_list|<
name|Object
argument_list|>
name|lagValues
decl_stmt|;
name|int
name|lastRowIdx
decl_stmt|;
specifier|public
name|void
name|initialize
parameter_list|(
name|int
name|lagAmt
parameter_list|)
block|{
name|this
operator|.
name|lagAmt
operator|=
name|lagAmt
expr_stmt|;
name|lagValues
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|(
name|lagAmt
argument_list|)
expr_stmt|;
name|values
operator|=
operator|new
name|ArrayList
argument_list|<
name|Object
argument_list|>
argument_list|()
expr_stmt|;
name|lastRowIdx
operator|=
operator|-
literal|1
expr_stmt|;
block|}
specifier|public
name|void
name|addRow
parameter_list|(
name|Object
name|currValue
parameter_list|,
name|Object
name|defaultValue
parameter_list|)
block|{
name|int
name|row
init|=
name|lastRowIdx
operator|+
literal|1
decl_stmt|;
if|if
condition|(
name|row
operator|<
name|lagAmt
condition|)
block|{
name|lagValues
operator|.
name|add
argument_list|(
name|defaultValue
argument_list|)
expr_stmt|;
block|}
name|values
operator|.
name|add
argument_list|(
name|currValue
argument_list|)
expr_stmt|;
name|lastRowIdx
operator|++
expr_stmt|;
block|}
specifier|public
name|Object
name|terminate
parameter_list|()
block|{
comment|/*        * if partition is smaller than the lagAmt;        * the entire partition is in lagValues.        */
if|if
condition|(
name|values
operator|.
name|size
argument_list|()
operator|<
name|lagAmt
condition|)
block|{
name|values
operator|=
name|lagValues
expr_stmt|;
return|return
name|lagValues
return|;
block|}
name|int
name|lastIdx
init|=
name|values
operator|.
name|size
argument_list|()
operator|-
literal|1
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
name|lagAmt
condition|;
name|i
operator|++
control|)
block|{
name|values
operator|.
name|remove
argument_list|(
name|lastIdx
operator|-
name|i
argument_list|)
expr_stmt|;
block|}
name|values
operator|.
name|addAll
argument_list|(
literal|0
argument_list|,
name|lagValues
argument_list|)
expr_stmt|;
return|return
name|values
return|;
block|}
block|}
comment|/*    * StreamingEval: wrap regular eval. on getNext remove first row from values    * and return it.    */
specifier|static
class|class
name|GenericUDAFLagEvaluatorStreaming
extends|extends
name|GenericUDAFLagEvaluator
implements|implements
name|ISupportStreamingModeForWindowing
block|{
specifier|protected
name|GenericUDAFLagEvaluatorStreaming
parameter_list|(
name|GenericUDAFLeadLagEvaluator
name|src
parameter_list|)
block|{
name|super
argument_list|(
name|src
argument_list|)
expr_stmt|;
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
name|LagBuffer
name|lb
init|=
operator|(
name|LagBuffer
operator|)
name|agg
decl_stmt|;
if|if
condition|(
operator|!
name|lb
operator|.
name|lagValues
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Object
name|res
init|=
name|lb
operator|.
name|lagValues
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
elseif|else
if|if
condition|(
operator|!
name|lb
operator|.
name|values
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Object
name|res
init|=
name|lb
operator|.
name|values
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
name|getAmt
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

