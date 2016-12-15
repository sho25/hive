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
name|optimizer
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|lang
operator|.
name|StringUtils
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
name|Constants
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
name|metastore
operator|.
name|api
operator|.
name|FieldSchema
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
name|ColumnInfo
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
name|FileSinkOperator
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
name|Operator
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
name|OperatorFactory
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
name|ReduceSinkOperator
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
name|RowSchema
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
name|SelectOperator
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
name|UDF
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
name|Utilities
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
name|Utilities
operator|.
name|ReduceField
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
name|AcidUtils
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
name|lib
operator|.
name|DefaultGraphWalker
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
name|lib
operator|.
name|DefaultRuleDispatcher
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
name|lib
operator|.
name|Dispatcher
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
name|lib
operator|.
name|GraphWalker
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
name|lib
operator|.
name|Node
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
name|lib
operator|.
name|NodeProcessor
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
name|lib
operator|.
name|NodeProcessorCtx
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
name|lib
operator|.
name|Rule
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
name|lib
operator|.
name|RuleRegExp
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
name|ParseContext
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
name|ExprNodeColumnDesc
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
name|ExprNodeDesc
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
name|ExprNodeGenericFuncDesc
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
name|FileSinkDesc
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
name|OperatorDesc
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
name|PlanUtils
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
name|ReduceSinkDesc
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
name|SelectDesc
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
name|TableDesc
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
name|UDFDateFloorDay
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
name|UDFDateFloorHour
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
name|UDFDateFloorMinute
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
name|UDFDateFloorMonth
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
name|UDFDateFloorSecond
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
name|UDFDateFloorWeek
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
name|UDFDateFloorYear
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
name|GenericUDFBridge
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
operator|.
name|PrimitiveCategory
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
name|TypeInfoFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|parquet
operator|.
name|Strings
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Stack
import|;
end_import

begin_comment
comment|/**  * Introduces a RS before FS to partition data by configuration specified  * time granularity.  */
end_comment

begin_class
specifier|public
class|class
name|SortedDynPartitionTimeGranularityOptimizer
extends|extends
name|Transform
block|{
annotation|@
name|Override
specifier|public
name|ParseContext
name|transform
parameter_list|(
name|ParseContext
name|pCtx
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// create a walker which walks the tree in a DFS manner while maintaining the
comment|// operator stack. The dispatcher generates the plan from the operator tree
name|Map
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
name|opRules
init|=
operator|new
name|LinkedHashMap
argument_list|<
name|Rule
argument_list|,
name|NodeProcessor
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|FS
init|=
name|FileSinkOperator
operator|.
name|getOperatorName
argument_list|()
operator|+
literal|"%"
decl_stmt|;
name|opRules
operator|.
name|put
argument_list|(
operator|new
name|RuleRegExp
argument_list|(
literal|"Sorted Dynamic Partition Time Granularity"
argument_list|,
name|FS
argument_list|)
argument_list|,
name|getSortDynPartProc
argument_list|(
name|pCtx
argument_list|)
argument_list|)
expr_stmt|;
name|Dispatcher
name|disp
init|=
operator|new
name|DefaultRuleDispatcher
argument_list|(
literal|null
argument_list|,
name|opRules
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|GraphWalker
name|ogw
init|=
operator|new
name|DefaultGraphWalker
argument_list|(
name|disp
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Node
argument_list|>
name|topNodes
init|=
operator|new
name|ArrayList
argument_list|<
name|Node
argument_list|>
argument_list|()
decl_stmt|;
name|topNodes
operator|.
name|addAll
argument_list|(
name|pCtx
operator|.
name|getTopOps
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|ogw
operator|.
name|startWalking
argument_list|(
name|topNodes
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|pCtx
return|;
block|}
specifier|private
name|NodeProcessor
name|getSortDynPartProc
parameter_list|(
name|ParseContext
name|pCtx
parameter_list|)
block|{
return|return
operator|new
name|SortedDynamicPartitionProc
argument_list|(
name|pCtx
argument_list|)
return|;
block|}
class|class
name|SortedDynamicPartitionProc
implements|implements
name|NodeProcessor
block|{
specifier|private
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SortedDynPartitionTimeGranularityOptimizer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|ParseContext
name|parseCtx
decl_stmt|;
specifier|public
name|SortedDynamicPartitionProc
parameter_list|(
name|ParseContext
name|pCtx
parameter_list|)
block|{
name|this
operator|.
name|parseCtx
operator|=
name|pCtx
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|process
parameter_list|(
name|Node
name|nd
parameter_list|,
name|Stack
argument_list|<
name|Node
argument_list|>
name|stack
parameter_list|,
name|NodeProcessorCtx
name|procCtx
parameter_list|,
name|Object
modifier|...
name|nodeOutputs
parameter_list|)
throws|throws
name|SemanticException
block|{
comment|// introduce RS and EX before FS
name|FileSinkOperator
name|fsOp
init|=
operator|(
name|FileSinkOperator
operator|)
name|nd
decl_stmt|;
specifier|final
name|String
name|sh
init|=
name|fsOp
operator|.
name|getConf
argument_list|()
operator|.
name|getTableInfo
argument_list|()
operator|.
name|getOutputFileFormatClassName
argument_list|()
decl_stmt|;
if|if
condition|(
name|parseCtx
operator|.
name|getQueryProperties
argument_list|()
operator|.
name|isQuery
argument_list|()
operator|||
name|sh
operator|==
literal|null
operator|||
operator|!
name|sh
operator|.
name|equals
argument_list|(
name|Constants
operator|.
name|DRUID_HIVE_OUTPUT_FORMAT
argument_list|)
condition|)
block|{
comment|// Bail out, nothing to do
return|return
literal|null
return|;
block|}
name|String
name|segmentGranularity
init|=
name|parseCtx
operator|.
name|getCreateTable
argument_list|()
operator|.
name|getTblProps
argument_list|()
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|DRUID_SEGMENT_GRANULARITY
argument_list|)
decl_stmt|;
name|segmentGranularity
operator|=
operator|!
name|Strings
operator|.
name|isNullOrEmpty
argument_list|(
name|segmentGranularity
argument_list|)
condition|?
name|segmentGranularity
else|:
name|HiveConf
operator|.
name|getVar
argument_list|(
name|parseCtx
operator|.
name|getConf
argument_list|()
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_DRUID_INDEXING_GRANULARITY
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Sorted dynamic partitioning on time granularity optimization kicked in..."
argument_list|)
expr_stmt|;
comment|// unlink connection between FS and its parent
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|fsParent
init|=
name|fsOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|fsParent
operator|=
name|fsOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|fsParent
operator|.
name|getChildOperators
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// Create SelectOp with granularity column
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|granularitySelOp
init|=
name|getGranularitySelOp
argument_list|(
name|fsParent
argument_list|,
name|segmentGranularity
argument_list|)
decl_stmt|;
comment|// Create ReduceSinkOp operator
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|parentCols
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|granularitySelOp
operator|.
name|getSchema
argument_list|()
operator|.
name|getSignature
argument_list|()
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|allRSCols
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|ColumnInfo
name|ci
range|:
name|parentCols
control|)
block|{
name|allRSCols
operator|.
name|add
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|ci
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Get the key positions
name|List
argument_list|<
name|Integer
argument_list|>
name|keyPositions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|keyPositions
operator|.
name|add
argument_list|(
name|allRSCols
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|sortOrder
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|sortOrder
operator|.
name|add
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|// asc
name|List
argument_list|<
name|Integer
argument_list|>
name|sortNullOrder
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|sortNullOrder
operator|.
name|add
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// nulls first
name|ReduceSinkOperator
name|rsOp
init|=
name|getReduceSinkOp
argument_list|(
name|keyPositions
argument_list|,
name|sortOrder
argument_list|,
name|sortNullOrder
argument_list|,
name|allRSCols
argument_list|,
name|granularitySelOp
argument_list|,
name|fsOp
operator|.
name|getConf
argument_list|()
operator|.
name|getWriteType
argument_list|()
argument_list|)
decl_stmt|;
comment|// Create backtrack SelectOp
name|List
argument_list|<
name|ExprNodeDesc
argument_list|>
name|descs
init|=
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|(
name|allRSCols
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colNames
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|colName
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
name|allRSCols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ExprNodeDesc
name|col
init|=
name|allRSCols
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|colName
operator|=
name|col
operator|.
name|getExprString
argument_list|()
expr_stmt|;
name|colNames
operator|.
name|add
argument_list|(
name|colName
argument_list|)
expr_stmt|;
if|if
condition|(
name|keyPositions
operator|.
name|contains
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|descs
operator|.
name|add
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|col
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|ReduceField
operator|.
name|KEY
operator|.
name|toString
argument_list|()
operator|+
literal|"."
operator|+
name|colName
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|descs
operator|.
name|add
argument_list|(
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|col
operator|.
name|getTypeInfo
argument_list|()
argument_list|,
name|ReduceField
operator|.
name|VALUE
operator|.
name|toString
argument_list|()
operator|+
literal|"."
operator|+
name|colName
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|RowSchema
name|selRS
init|=
operator|new
name|RowSchema
argument_list|(
name|granularitySelOp
operator|.
name|getSchema
argument_list|()
argument_list|)
decl_stmt|;
name|SelectDesc
name|selConf
init|=
operator|new
name|SelectDesc
argument_list|(
name|descs
argument_list|,
name|colNames
argument_list|)
decl_stmt|;
name|SelectOperator
name|backtrackSelOp
init|=
operator|(
name|SelectOperator
operator|)
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
name|selConf
argument_list|,
name|selRS
argument_list|,
name|rsOp
argument_list|)
decl_stmt|;
comment|// Link backtrack SelectOp to FileSinkOp
name|fsOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|clear
argument_list|()
expr_stmt|;
name|fsOp
operator|.
name|getParentOperators
argument_list|()
operator|.
name|add
argument_list|(
name|backtrackSelOp
argument_list|)
expr_stmt|;
name|backtrackSelOp
operator|.
name|getChildOperators
argument_list|()
operator|.
name|add
argument_list|(
name|fsOp
argument_list|)
expr_stmt|;
comment|// Update file sink descriptor
name|fsOp
operator|.
name|getConf
argument_list|()
operator|.
name|setDpSortState
argument_list|(
name|FileSinkDesc
operator|.
name|DPSortState
operator|.
name|PARTITION_SORTED
argument_list|)
expr_stmt|;
name|fsOp
operator|.
name|getConf
argument_list|()
operator|.
name|setPartitionCols
argument_list|(
name|rsOp
operator|.
name|getConf
argument_list|()
operator|.
name|getPartitionCols
argument_list|()
argument_list|)
expr_stmt|;
name|ColumnInfo
name|ci
init|=
operator|new
name|ColumnInfo
argument_list|(
name|granularitySelOp
operator|.
name|getSchema
argument_list|()
operator|.
name|getSignature
argument_list|()
operator|.
name|get
argument_list|(
name|granularitySelOp
operator|.
name|getSchema
argument_list|()
operator|.
name|getSignature
argument_list|()
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
comment|// granularity column
name|fsOp
operator|.
name|getSchema
argument_list|()
operator|.
name|getSignature
argument_list|()
operator|.
name|add
argument_list|(
name|ci
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Inserted "
operator|+
name|granularitySelOp
operator|.
name|getOperatorId
argument_list|()
operator|+
literal|", "
operator|+
name|rsOp
operator|.
name|getOperatorId
argument_list|()
operator|+
literal|" and "
operator|+
name|backtrackSelOp
operator|.
name|getOperatorId
argument_list|()
operator|+
literal|" as parent of "
operator|+
name|fsOp
operator|.
name|getOperatorId
argument_list|()
operator|+
literal|" and child of "
operator|+
name|fsParent
operator|.
name|getOperatorId
argument_list|()
argument_list|)
expr_stmt|;
name|parseCtx
operator|.
name|setReduceSinkAddedBySortedDynPartition
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|private
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|getGranularitySelOp
parameter_list|(
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|fsParent
parameter_list|,
name|String
name|segmentGranularity
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|parentCols
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|fsParent
operator|.
name|getSchema
argument_list|()
operator|.
name|getSignature
argument_list|()
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|descs
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|colNames
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|int
name|timestampPos
init|=
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
name|parentCols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ColumnInfo
name|ci
init|=
name|parentCols
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ExprNodeColumnDesc
name|columnDesc
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|ci
argument_list|)
decl_stmt|;
name|descs
operator|.
name|add
argument_list|(
name|columnDesc
argument_list|)
expr_stmt|;
name|colNames
operator|.
name|add
argument_list|(
name|columnDesc
operator|.
name|getExprString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|columnDesc
operator|.
name|getTypeInfo
argument_list|()
operator|.
name|getCategory
argument_list|()
operator|==
name|ObjectInspector
operator|.
name|Category
operator|.
name|PRIMITIVE
operator|&&
operator|(
operator|(
name|PrimitiveTypeInfo
operator|)
name|columnDesc
operator|.
name|getTypeInfo
argument_list|()
operator|)
operator|.
name|getPrimitiveCategory
argument_list|()
operator|==
name|PrimitiveCategory
operator|.
name|TIMESTAMP
condition|)
block|{
if|if
condition|(
name|timestampPos
operator|!=
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Multiple columns with timestamp type on query result; "
operator|+
literal|"could not resolve which one is the timestamp column"
argument_list|)
throw|;
block|}
name|timestampPos
operator|=
name|i
expr_stmt|;
block|}
block|}
if|if
condition|(
name|timestampPos
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"No column with timestamp type on query result; "
operator|+
literal|"one column should be of timestamp type"
argument_list|)
throw|;
block|}
name|RowSchema
name|selRS
init|=
operator|new
name|RowSchema
argument_list|(
name|fsParent
operator|.
name|getSchema
argument_list|()
argument_list|)
decl_stmt|;
comment|// Granularity (partition) column
name|String
name|udfName
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|UDF
argument_list|>
name|udfClass
decl_stmt|;
switch|switch
condition|(
name|segmentGranularity
condition|)
block|{
case|case
literal|"YEAR"
case|:
name|udfName
operator|=
literal|"floor_year"
expr_stmt|;
name|udfClass
operator|=
name|UDFDateFloorYear
operator|.
name|class
expr_stmt|;
break|break;
case|case
literal|"MONTH"
case|:
name|udfName
operator|=
literal|"floor_month"
expr_stmt|;
name|udfClass
operator|=
name|UDFDateFloorMonth
operator|.
name|class
expr_stmt|;
break|break;
case|case
literal|"WEEK"
case|:
name|udfName
operator|=
literal|"floor_week"
expr_stmt|;
name|udfClass
operator|=
name|UDFDateFloorWeek
operator|.
name|class
expr_stmt|;
break|break;
case|case
literal|"DAY"
case|:
name|udfName
operator|=
literal|"floor_day"
expr_stmt|;
name|udfClass
operator|=
name|UDFDateFloorDay
operator|.
name|class
expr_stmt|;
break|break;
case|case
literal|"HOUR"
case|:
name|udfName
operator|=
literal|"floor_hour"
expr_stmt|;
name|udfClass
operator|=
name|UDFDateFloorHour
operator|.
name|class
expr_stmt|;
break|break;
case|case
literal|"MINUTE"
case|:
name|udfName
operator|=
literal|"floor_minute"
expr_stmt|;
name|udfClass
operator|=
name|UDFDateFloorMinute
operator|.
name|class
expr_stmt|;
break|break;
case|case
literal|"SECOND"
case|:
name|udfName
operator|=
literal|"floor_second"
expr_stmt|;
name|udfClass
operator|=
name|UDFDateFloorSecond
operator|.
name|class
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Granularity for Druid segment not recognized"
argument_list|)
throw|;
block|}
name|ExprNodeDesc
name|expr
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|parentCols
operator|.
name|get
argument_list|(
name|timestampPos
argument_list|)
argument_list|)
decl_stmt|;
name|descs
operator|.
name|add
argument_list|(
operator|new
name|ExprNodeGenericFuncDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|timestampTypeInfo
argument_list|,
operator|new
name|GenericUDFBridge
argument_list|(
name|udfName
argument_list|,
literal|false
argument_list|,
name|udfClass
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|expr
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|colNames
operator|.
name|add
argument_list|(
name|Constants
operator|.
name|DRUID_TIMESTAMP_GRANULARITY_COL_NAME
argument_list|)
expr_stmt|;
comment|// Add granularity to the row schema
name|ColumnInfo
name|ci
init|=
operator|new
name|ColumnInfo
argument_list|(
name|Constants
operator|.
name|DRUID_TIMESTAMP_GRANULARITY_COL_NAME
argument_list|,
name|TypeInfoFactory
operator|.
name|timestampTypeInfo
argument_list|,
name|selRS
operator|.
name|getSignature
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTabAlias
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|selRS
operator|.
name|getSignature
argument_list|()
operator|.
name|add
argument_list|(
name|ci
argument_list|)
expr_stmt|;
comment|// Create SelectDesc
name|SelectDesc
name|selConf
init|=
operator|new
name|SelectDesc
argument_list|(
name|descs
argument_list|,
name|colNames
argument_list|)
decl_stmt|;
comment|// Create Select Operator
name|SelectOperator
name|selOp
init|=
operator|(
name|SelectOperator
operator|)
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
name|selConf
argument_list|,
name|selRS
argument_list|,
name|fsParent
argument_list|)
decl_stmt|;
return|return
name|selOp
return|;
block|}
specifier|private
name|ReduceSinkOperator
name|getReduceSinkOp
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|keyPositions
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|sortOrder
parameter_list|,
name|List
argument_list|<
name|Integer
argument_list|>
name|sortNullOrder
parameter_list|,
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|allCols
parameter_list|,
name|Operator
argument_list|<
name|?
extends|extends
name|OperatorDesc
argument_list|>
name|parent
parameter_list|,
name|AcidUtils
operator|.
name|Operation
name|writeType
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|keyCols
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
comment|// we will clone here as RS will update bucket column key with its
comment|// corresponding with bucket number and hence their OIs
for|for
control|(
name|Integer
name|idx
range|:
name|keyPositions
control|)
block|{
name|keyCols
operator|.
name|add
argument_list|(
name|allCols
operator|.
name|get
argument_list|(
name|idx
argument_list|)
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|valCols
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
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
name|allCols
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|keyPositions
operator|.
name|contains
argument_list|(
name|i
argument_list|)
condition|)
block|{
name|valCols
operator|.
name|add
argument_list|(
name|allCols
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
name|partCols
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|Integer
name|idx
range|:
name|keyPositions
control|)
block|{
name|partCols
operator|.
name|add
argument_list|(
name|allCols
operator|.
name|get
argument_list|(
name|idx
argument_list|)
operator|.
name|clone
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// map _col0 to KEY._col0, etc
name|Map
argument_list|<
name|String
argument_list|,
name|ExprNodeDesc
argument_list|>
name|colExprMap
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|nameMapping
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|keyColNames
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|keyCol
range|:
name|keyCols
control|)
block|{
name|String
name|keyColName
init|=
name|keyCol
operator|.
name|getExprString
argument_list|()
decl_stmt|;
name|keyColNames
operator|.
name|add
argument_list|(
name|keyColName
argument_list|)
expr_stmt|;
name|colExprMap
operator|.
name|put
argument_list|(
name|Utilities
operator|.
name|ReduceField
operator|.
name|KEY
operator|+
literal|"."
operator|+
name|keyColName
argument_list|,
name|keyCol
argument_list|)
expr_stmt|;
name|nameMapping
operator|.
name|put
argument_list|(
name|keyColName
argument_list|,
name|Utilities
operator|.
name|ReduceField
operator|.
name|KEY
operator|+
literal|"."
operator|+
name|keyColName
argument_list|)
expr_stmt|;
block|}
name|ArrayList
argument_list|<
name|String
argument_list|>
name|valColNames
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|ExprNodeDesc
name|valCol
range|:
name|valCols
control|)
block|{
name|String
name|colName
init|=
name|valCol
operator|.
name|getExprString
argument_list|()
decl_stmt|;
name|valColNames
operator|.
name|add
argument_list|(
name|colName
argument_list|)
expr_stmt|;
name|colExprMap
operator|.
name|put
argument_list|(
name|Utilities
operator|.
name|ReduceField
operator|.
name|VALUE
operator|+
literal|"."
operator|+
name|colName
argument_list|,
name|valCol
argument_list|)
expr_stmt|;
name|nameMapping
operator|.
name|put
argument_list|(
name|colName
argument_list|,
name|Utilities
operator|.
name|ReduceField
operator|.
name|VALUE
operator|+
literal|"."
operator|+
name|colName
argument_list|)
expr_stmt|;
block|}
comment|// order and null order
name|String
name|orderStr
init|=
name|StringUtils
operator|.
name|repeat
argument_list|(
literal|"+"
argument_list|,
name|sortOrder
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|nullOrderStr
init|=
name|StringUtils
operator|.
name|repeat
argument_list|(
literal|"a"
argument_list|,
name|sortNullOrder
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
comment|// Create Key/Value TableDesc. When the operator plan is split into MR tasks,
comment|// the reduce operator will initialize Extract operator with information
comment|// from Key and Value TableDesc
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|fields
init|=
name|PlanUtils
operator|.
name|getFieldSchemasFromColumnList
argument_list|(
name|keyCols
argument_list|,
name|keyColNames
argument_list|,
literal|0
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|TableDesc
name|keyTable
init|=
name|PlanUtils
operator|.
name|getReduceKeyTableDesc
argument_list|(
name|fields
argument_list|,
name|orderStr
argument_list|,
name|nullOrderStr
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|valFields
init|=
name|PlanUtils
operator|.
name|getFieldSchemasFromColumnList
argument_list|(
name|valCols
argument_list|,
name|valColNames
argument_list|,
literal|0
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|TableDesc
name|valueTable
init|=
name|PlanUtils
operator|.
name|getReduceValueTableDesc
argument_list|(
name|valFields
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|List
argument_list|<
name|Integer
argument_list|>
argument_list|>
name|distinctColumnIndices
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
comment|// Number of reducers is set to default (-1)
name|ReduceSinkDesc
name|rsConf
init|=
operator|new
name|ReduceSinkDesc
argument_list|(
name|keyCols
argument_list|,
name|keyCols
operator|.
name|size
argument_list|()
argument_list|,
name|valCols
argument_list|,
name|keyColNames
argument_list|,
name|distinctColumnIndices
argument_list|,
name|valColNames
argument_list|,
operator|-
literal|1
argument_list|,
name|partCols
argument_list|,
operator|-
literal|1
argument_list|,
name|keyTable
argument_list|,
name|valueTable
argument_list|,
name|writeType
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|signature
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|parent
operator|.
name|getSchema
argument_list|()
operator|.
name|getSignature
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
name|ColumnInfo
name|colInfo
init|=
operator|new
name|ColumnInfo
argument_list|(
name|parent
operator|.
name|getSchema
argument_list|()
operator|.
name|getSignature
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
argument_list|)
decl_stmt|;
name|colInfo
operator|.
name|setInternalName
argument_list|(
name|nameMapping
operator|.
name|get
argument_list|(
name|colInfo
operator|.
name|getInternalName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|signature
operator|.
name|add
argument_list|(
name|colInfo
argument_list|)
expr_stmt|;
block|}
name|ReduceSinkOperator
name|op
init|=
operator|(
name|ReduceSinkOperator
operator|)
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
name|rsConf
argument_list|,
operator|new
name|RowSchema
argument_list|(
name|signature
argument_list|)
argument_list|,
name|parent
argument_list|)
decl_stmt|;
name|op
operator|.
name|setColumnExprMap
argument_list|(
name|colExprMap
argument_list|)
expr_stmt|;
return|return
name|op
return|;
block|}
block|}
block|}
end_class

end_unit

