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

begin_class
annotation|@
name|Description
argument_list|(
name|name
operator|=
literal|"lead"
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
name|GenericUDAFLead
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
specifier|protected
name|String
name|functionName
parameter_list|()
block|{
return|return
literal|"Lead"
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
name|GenericUDAFLeadEvaluator
argument_list|()
return|;
block|}
specifier|public
specifier|static
class|class
name|GenericUDAFLeadEvaluator
extends|extends
name|GenericUDAFLeadLagEvaluator
block|{
specifier|public
name|GenericUDAFLeadEvaluator
parameter_list|()
block|{     }
comment|/*      * used to initialize Streaming Evaluator.      */
specifier|protected
name|GenericUDAFLeadEvaluator
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
name|LeadBuffer
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
name|GenericUDAFLeadEvaluatorStreaming
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
specifier|static
class|class
name|LeadBuffer
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
name|leadAmt
decl_stmt|;
name|Object
index|[]
name|leadWindow
decl_stmt|;
name|int
name|nextPosInWindow
decl_stmt|;
name|int
name|lastRowIdx
decl_stmt|;
specifier|public
name|void
name|initialize
parameter_list|(
name|int
name|leadAmt
parameter_list|)
block|{
name|this
operator|.
name|leadAmt
operator|=
name|leadAmt
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
name|leadWindow
operator|=
operator|new
name|Object
index|[
name|leadAmt
index|]
expr_stmt|;
name|nextPosInWindow
operator|=
literal|0
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
name|leadExprValue
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
name|int
name|leadRow
init|=
name|row
operator|-
name|leadAmt
decl_stmt|;
if|if
condition|(
name|leadRow
operator|>=
literal|0
condition|)
block|{
name|values
operator|.
name|add
argument_list|(
name|leadExprValue
argument_list|)
expr_stmt|;
block|}
name|leadWindow
index|[
name|nextPosInWindow
index|]
operator|=
name|defaultValue
expr_stmt|;
name|nextPosInWindow
operator|=
operator|(
name|nextPosInWindow
operator|+
literal|1
operator|)
operator|%
name|leadAmt
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
comment|/*        * if there are fewer than leadAmt values in leadWindow; start reading from the first position.        * Otherwise the window starts from nextPosInWindow.        */
if|if
condition|(
name|lastRowIdx
operator|<
name|leadAmt
condition|)
block|{
name|nextPosInWindow
operator|=
literal|0
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
name|leadAmt
condition|;
name|i
operator|++
control|)
block|{
name|values
operator|.
name|add
argument_list|(
name|leadWindow
index|[
name|nextPosInWindow
index|]
argument_list|)
expr_stmt|;
name|nextPosInWindow
operator|=
operator|(
name|nextPosInWindow
operator|+
literal|1
operator|)
operator|%
name|leadAmt
expr_stmt|;
block|}
return|return
name|values
return|;
block|}
block|}
comment|/*    * StreamingEval: wrap regular eval. on getNext remove first row from values    * and return it.    */
specifier|static
class|class
name|GenericUDAFLeadEvaluatorStreaming
extends|extends
name|GenericUDAFLeadEvaluator
implements|implements
name|ISupportStreamingModeForWindowing
block|{
specifier|protected
name|GenericUDAFLeadEvaluatorStreaming
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
name|LeadBuffer
name|lb
init|=
operator|(
name|LeadBuffer
operator|)
name|agg
decl_stmt|;
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

