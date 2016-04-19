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
name|exec
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
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
name|conf
operator|.
name|Configuration
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
name|conf
operator|.
name|HiveConf
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
name|CompilationOpContext
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
name|io
operator|.
name|IOContext
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
name|io
operator|.
name|IOContextMap
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
name|FilterDesc
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
name|api
operator|.
name|OperatorType
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
name|PrimitiveObjectInspector
import|;
end_import

begin_comment
comment|/**  * Filter operator implementation.  **/
end_comment

begin_class
specifier|public
class|class
name|FilterOperator
extends|extends
name|Operator
argument_list|<
name|FilterDesc
argument_list|>
implements|implements
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|transient
name|ExprNodeEvaluator
name|conditionEvaluator
decl_stmt|;
specifier|private
specifier|transient
name|PrimitiveObjectInspector
name|conditionInspector
decl_stmt|;
specifier|private
specifier|transient
name|int
name|consecutiveSearches
decl_stmt|;
specifier|private
specifier|transient
name|IOContext
name|ioContext
decl_stmt|;
specifier|protected
specifier|transient
name|int
name|heartbeatInterval
decl_stmt|;
comment|/** Kryo ctor. */
specifier|protected
name|FilterOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|FilterOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|consecutiveSearches
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
try|try
block|{
name|heartbeatInterval
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESENDHEARTBEAT
argument_list|)
expr_stmt|;
name|conditionEvaluator
operator|=
name|ExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|conf
operator|.
name|getPredicate
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|HiveConf
operator|.
name|getBoolVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEEXPREVALUATIONCACHE
argument_list|)
condition|)
block|{
name|conditionEvaluator
operator|=
name|ExprNodeEvaluatorFactory
operator|.
name|toCachedEval
argument_list|(
name|conditionEvaluator
argument_list|)
expr_stmt|;
block|}
name|conditionInspector
operator|=
literal|null
expr_stmt|;
name|ioContext
operator|=
name|IOContextMap
operator|.
name|get
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
name|ObjectInspector
name|rowInspector
init|=
name|inputObjInspectors
index|[
name|tag
index|]
decl_stmt|;
if|if
condition|(
name|conditionInspector
operator|==
literal|null
condition|)
block|{
name|conditionInspector
operator|=
operator|(
name|PrimitiveObjectInspector
operator|)
name|conditionEvaluator
operator|.
name|initialize
argument_list|(
name|rowInspector
argument_list|)
expr_stmt|;
block|}
comment|// If the input is sorted, and we are executing a search based on the arguments to this filter,
comment|// set the comparison in the IOContext and the type of the UDF
if|if
condition|(
name|conf
operator|.
name|isSortedFilter
argument_list|()
operator|&&
name|ioContext
operator|.
name|useSorted
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|conditionEvaluator
operator|instanceof
name|ExprNodeGenericFuncEvaluator
operator|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Attempted to use the fact data is sorted when the conditionEvaluator is not "
operator|+
literal|"of type ExprNodeGenericFuncEvaluator"
argument_list|)
expr_stmt|;
name|ioContext
operator|.
name|setUseSorted
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return;
block|}
else|else
block|{
name|ioContext
operator|.
name|setComparison
argument_list|(
operator|(
operator|(
name|ExprNodeGenericFuncEvaluator
operator|)
name|conditionEvaluator
operator|)
operator|.
name|compare
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ioContext
operator|.
name|getGenericUDFClassName
argument_list|()
operator|==
literal|null
condition|)
block|{
name|ioContext
operator|.
name|setGenericUDFClassName
argument_list|(
operator|(
operator|(
name|ExprNodeGenericFuncEvaluator
operator|)
name|conditionEvaluator
operator|)
operator|.
name|genericUDF
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// If we are currently searching the data for a place to begin, do not return data yet
if|if
condition|(
name|ioContext
operator|.
name|isBinarySearching
argument_list|()
condition|)
block|{
name|consecutiveSearches
operator|++
expr_stmt|;
comment|// In case we're searching through an especially large set of data, send a heartbeat in
comment|// order to avoid timeout
if|if
condition|(
operator|(
operator|(
name|consecutiveSearches
operator|%
name|heartbeatInterval
operator|)
operator|==
literal|0
operator|)
operator|&&
operator|(
name|reporter
operator|!=
literal|null
operator|)
condition|)
block|{
name|reporter
operator|.
name|progress
argument_list|()
expr_stmt|;
block|}
return|return;
block|}
block|}
name|Object
name|condition
init|=
name|conditionEvaluator
operator|.
name|evaluate
argument_list|(
name|row
argument_list|)
decl_stmt|;
comment|// If we are currently performing a binary search on the input, don't forward the results
comment|// Currently this value is set when a query is optimized using a compact index.  The map reduce
comment|// job responsible for scanning and filtering the index sets this value.  It remains set
comment|// throughout the binary search executed by the HiveBinarySearchRecordResder until a starting
comment|// point for a linear scan has been identified, at which point this value is unset.
if|if
condition|(
name|ioContext
operator|.
name|isBinarySearching
argument_list|()
condition|)
block|{
return|return;
block|}
name|Boolean
name|ret
init|=
operator|(
name|Boolean
operator|)
name|conditionInspector
operator|.
name|getPrimitiveJavaObject
argument_list|(
name|condition
argument_list|)
decl_stmt|;
if|if
condition|(
name|Boolean
operator|.
name|TRUE
operator|.
name|equals
argument_list|(
name|ret
argument_list|)
condition|)
block|{
name|forward
argument_list|(
name|row
argument_list|,
name|rowInspector
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @return the name of the operator    */
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|FilterOperator
operator|.
name|getOperatorName
argument_list|()
return|;
block|}
specifier|static
specifier|public
name|String
name|getOperatorName
parameter_list|()
block|{
return|return
literal|"FIL"
return|;
block|}
annotation|@
name|Override
specifier|public
name|OperatorType
name|getType
parameter_list|()
block|{
return|return
name|OperatorType
operator|.
name|FILTER
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|supportSkewJoinOptimization
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|columnNamesRowResolvedCanBeObtained
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|supportAutomaticSortMergeJoin
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|supportUnionRemoveOptimization
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

