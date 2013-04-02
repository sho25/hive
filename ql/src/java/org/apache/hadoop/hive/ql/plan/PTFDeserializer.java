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
name|plan
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
name|Properties
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
name|exec
operator|.
name|ExprNodeEvaluator
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
name|PTFTranslator
operator|.
name|LeadLagInfo
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
name|WindowingExprNodeEvaluatorFactory
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
name|PTFDesc
operator|.
name|BoundaryDef
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
name|PTFDesc
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
name|PTFDesc
operator|.
name|PTFInputDef
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
name|PTFDesc
operator|.
name|PTFQueryInputDef
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
name|PTFDesc
operator|.
name|PartitionedTableFunctionDef
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
name|PTFDesc
operator|.
name|ShapeDetails
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
name|PTFDesc
operator|.
name|ValueBoundaryDef
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
name|PTFDesc
operator|.
name|WindowExpressionDef
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
name|PTFDesc
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
name|plan
operator|.
name|PTFDesc
operator|.
name|WindowFunctionDef
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
name|PTFDesc
operator|.
name|WindowTableFunctionDef
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
name|GenericUDFLeadLag
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
name|TableFunctionEvaluator
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
name|TableFunctionResolver
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
name|WindowingTableFunction
operator|.
name|WindowingTableFunctionResolver
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
name|SerDe
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
name|SerDeException
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
name|SerDeUtils
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
name|ListObjectInspector
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
name|StructField
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
name|StructObjectInspector
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
name|ReflectionUtils
import|;
end_import

begin_class
specifier|public
class|class
name|PTFDeserializer
block|{
name|PTFDesc
name|ptfDesc
decl_stmt|;
name|StructObjectInspector
name|inputOI
decl_stmt|;
name|HiveConf
name|hConf
decl_stmt|;
name|LeadLagInfo
name|llInfo
decl_stmt|;
specifier|public
name|PTFDeserializer
parameter_list|(
name|PTFDesc
name|ptfDesc
parameter_list|,
name|StructObjectInspector
name|inputOI
parameter_list|,
name|HiveConf
name|hConf
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|ptfDesc
operator|=
name|ptfDesc
expr_stmt|;
name|this
operator|.
name|inputOI
operator|=
name|inputOI
expr_stmt|;
name|this
operator|.
name|hConf
operator|=
name|hConf
expr_stmt|;
name|llInfo
operator|=
operator|new
name|LeadLagInfo
argument_list|()
expr_stmt|;
name|ptfDesc
operator|.
name|setLlInfo
argument_list|(
name|llInfo
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|initializePTFChain
parameter_list|(
name|PartitionedTableFunctionDef
name|tblFnDef
parameter_list|)
throws|throws
name|HiveException
block|{
name|Stack
argument_list|<
name|PTFInputDef
argument_list|>
name|ptfChain
init|=
operator|new
name|Stack
argument_list|<
name|PTFInputDef
argument_list|>
argument_list|()
decl_stmt|;
name|PTFInputDef
name|currentDef
init|=
name|tblFnDef
decl_stmt|;
while|while
condition|(
name|currentDef
operator|!=
literal|null
condition|)
block|{
name|ptfChain
operator|.
name|push
argument_list|(
name|currentDef
argument_list|)
expr_stmt|;
name|currentDef
operator|=
name|currentDef
operator|.
name|getInput
argument_list|()
expr_stmt|;
block|}
while|while
condition|(
operator|!
name|ptfChain
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|currentDef
operator|=
name|ptfChain
operator|.
name|pop
argument_list|()
expr_stmt|;
if|if
condition|(
name|currentDef
operator|instanceof
name|PTFQueryInputDef
condition|)
block|{
name|initialize
argument_list|(
operator|(
name|PTFQueryInputDef
operator|)
name|currentDef
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentDef
operator|instanceof
name|WindowTableFunctionDef
condition|)
block|{
name|initializeWindowing
argument_list|(
operator|(
name|WindowTableFunctionDef
operator|)
name|currentDef
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|initialize
argument_list|(
operator|(
name|PartitionedTableFunctionDef
operator|)
name|currentDef
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|initializeWindowing
parameter_list|(
name|WindowTableFunctionDef
name|def
parameter_list|)
throws|throws
name|HiveException
block|{
name|ShapeDetails
name|inpShape
init|=
name|def
operator|.
name|getInput
argument_list|()
operator|.
name|getOutputShape
argument_list|()
decl_stmt|;
comment|/*      * 1. setup resolve, make connections      */
name|TableFunctionEvaluator
name|tEval
init|=
name|def
operator|.
name|getTFunction
argument_list|()
decl_stmt|;
comment|/*WindowingTableFunctionResolver tResolver = (WindowingTableFunctionResolver)         FunctionRegistry.getTableFunctionResolver(def.getName());*/
name|WindowingTableFunctionResolver
name|tResolver
init|=
operator|(
name|WindowingTableFunctionResolver
operator|)
name|constructResolver
argument_list|(
name|def
operator|.
name|getResolverClassName
argument_list|()
argument_list|)
decl_stmt|;
name|tResolver
operator|.
name|initialize
argument_list|(
name|ptfDesc
argument_list|,
name|def
argument_list|,
name|tEval
argument_list|)
expr_stmt|;
comment|/*      * 2. initialize WFns.      */
if|if
condition|(
name|def
operator|.
name|getWindowFunctions
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|WindowFunctionDef
name|wFnDef
range|:
name|def
operator|.
name|getWindowFunctions
argument_list|()
control|)
block|{
if|if
condition|(
name|wFnDef
operator|.
name|getArgs
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|PTFExpressionDef
name|arg
range|:
name|wFnDef
operator|.
name|getArgs
argument_list|()
control|)
block|{
name|initialize
argument_list|(
name|arg
argument_list|,
name|inpShape
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|wFnDef
operator|.
name|getWindowFrame
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|WindowFrameDef
name|wFrmDef
init|=
name|wFnDef
operator|.
name|getWindowFrame
argument_list|()
decl_stmt|;
name|initialize
argument_list|(
name|wFrmDef
operator|.
name|getStart
argument_list|()
argument_list|,
name|inpShape
argument_list|)
expr_stmt|;
name|initialize
argument_list|(
name|wFrmDef
operator|.
name|getEnd
argument_list|()
argument_list|,
name|inpShape
argument_list|)
expr_stmt|;
block|}
name|setupWdwFnEvaluator
argument_list|(
name|wFnDef
argument_list|)
expr_stmt|;
block|}
name|ArrayList
argument_list|<
name|String
argument_list|>
name|aliases
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldOIs
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|WindowFunctionDef
name|wFnDef
range|:
name|def
operator|.
name|getWindowFunctions
argument_list|()
control|)
block|{
name|aliases
operator|.
name|add
argument_list|(
name|wFnDef
operator|.
name|getAlias
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|wFnDef
operator|.
name|isPivotResult
argument_list|()
condition|)
block|{
name|fieldOIs
operator|.
name|add
argument_list|(
operator|(
operator|(
name|ListObjectInspector
operator|)
name|wFnDef
operator|.
name|getOI
argument_list|()
operator|)
operator|.
name|getListElementObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fieldOIs
operator|.
name|add
argument_list|(
name|wFnDef
operator|.
name|getOI
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|PTFDeserializer
operator|.
name|addInputColumnsToList
argument_list|(
name|inpShape
argument_list|,
name|aliases
argument_list|,
name|fieldOIs
argument_list|)
expr_stmt|;
name|StructObjectInspector
name|wdwOutOI
init|=
name|ObjectInspectorFactory
operator|.
name|getStandardStructObjectInspector
argument_list|(
name|aliases
argument_list|,
name|fieldOIs
argument_list|)
decl_stmt|;
name|tResolver
operator|.
name|setWdwProcessingOutputOI
argument_list|(
name|wdwOutOI
argument_list|)
expr_stmt|;
name|initialize
argument_list|(
name|def
operator|.
name|getOutputFromWdwFnProcessing
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|def
operator|.
name|setOutputFromWdwFnProcessing
argument_list|(
name|inpShape
argument_list|)
expr_stmt|;
block|}
name|inpShape
operator|=
name|def
operator|.
name|getOutputFromWdwFnProcessing
argument_list|()
expr_stmt|;
comment|/*      * 3. initialize WExprs. + having clause      */
if|if
condition|(
name|def
operator|.
name|getWindowExpressions
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|WindowExpressionDef
name|wEDef
range|:
name|def
operator|.
name|getWindowExpressions
argument_list|()
control|)
block|{
name|initialize
argument_list|(
name|wEDef
argument_list|,
name|inpShape
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*      * 4. give Evaluator chance to setup for Output execution; setup Output shape.      */
name|initialize
argument_list|(
name|def
operator|.
name|getOutputShape
argument_list|()
argument_list|)
expr_stmt|;
name|tResolver
operator|.
name|initializeOutputOI
argument_list|()
expr_stmt|;
comment|/*      * If we have windowExpressions then we convert to Std. Object to process;      * we just stream these rows; no need to put in an output Partition.      */
if|if
condition|(
name|def
operator|.
name|getWindowExpressions
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|StructObjectInspector
name|oi
init|=
operator|(
name|StructObjectInspector
operator|)
name|ObjectInspectorUtils
operator|.
name|getStandardObjectInspector
argument_list|(
name|def
operator|.
name|getOutputShape
argument_list|()
operator|.
name|getOI
argument_list|()
argument_list|)
decl_stmt|;
name|def
operator|.
name|getOutputShape
argument_list|()
operator|.
name|setOI
argument_list|(
name|oi
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|initialize
parameter_list|(
name|PTFQueryInputDef
name|def
parameter_list|)
throws|throws
name|HiveException
block|{
name|ShapeDetails
name|outShape
init|=
name|def
operator|.
name|getOutputShape
argument_list|()
decl_stmt|;
name|initialize
argument_list|(
name|outShape
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|initialize
parameter_list|(
name|PartitionedTableFunctionDef
name|def
parameter_list|)
throws|throws
name|HiveException
block|{
name|ShapeDetails
name|inpShape
init|=
name|def
operator|.
name|getInput
argument_list|()
operator|.
name|getOutputShape
argument_list|()
decl_stmt|;
comment|/*      * 1. initialize args      */
if|if
condition|(
name|def
operator|.
name|getArgs
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|PTFExpressionDef
name|arg
range|:
name|def
operator|.
name|getArgs
argument_list|()
control|)
block|{
name|initialize
argument_list|(
name|arg
argument_list|,
name|inpShape
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*      * 2. setup resolve, make connections      */
name|TableFunctionEvaluator
name|tEval
init|=
name|def
operator|.
name|getTFunction
argument_list|()
decl_stmt|;
comment|//TableFunctionResolver tResolver = FunctionRegistry.getTableFunctionResolver(def.getName());
name|TableFunctionResolver
name|tResolver
init|=
name|constructResolver
argument_list|(
name|def
operator|.
name|getResolverClassName
argument_list|()
argument_list|)
decl_stmt|;
name|tResolver
operator|.
name|initialize
argument_list|(
name|ptfDesc
argument_list|,
name|def
argument_list|,
name|tEval
argument_list|)
expr_stmt|;
comment|/*      * 3. give Evaluator chance to setup for RawInput execution; setup RawInput shape      */
if|if
condition|(
name|tEval
operator|.
name|isTransformsRawInput
argument_list|()
condition|)
block|{
name|tResolver
operator|.
name|initializeRawInputOI
argument_list|()
expr_stmt|;
name|initialize
argument_list|(
name|def
operator|.
name|getRawInputShape
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|def
operator|.
name|setRawInputShape
argument_list|(
name|inpShape
argument_list|)
expr_stmt|;
block|}
name|inpShape
operator|=
name|def
operator|.
name|getRawInputShape
argument_list|()
expr_stmt|;
comment|/*      * 4. give Evaluator chance to setup for Output execution; setup Output shape.      */
name|tResolver
operator|.
name|initializeOutputOI
argument_list|()
expr_stmt|;
name|initialize
argument_list|(
name|def
operator|.
name|getOutputShape
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|static
name|void
name|setupWdwFnEvaluator
parameter_list|(
name|WindowFunctionDef
name|def
parameter_list|)
throws|throws
name|HiveException
block|{
name|ArrayList
argument_list|<
name|PTFExpressionDef
argument_list|>
name|args
init|=
name|def
operator|.
name|getArgs
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|argOIs
init|=
operator|new
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
argument_list|()
decl_stmt|;
name|ObjectInspector
index|[]
name|funcArgOIs
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|args
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|PTFExpressionDef
name|arg
range|:
name|args
control|)
block|{
name|argOIs
operator|.
name|add
argument_list|(
name|arg
operator|.
name|getOI
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|funcArgOIs
operator|=
operator|new
name|ObjectInspector
index|[
name|args
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|funcArgOIs
operator|=
name|argOIs
operator|.
name|toArray
argument_list|(
name|funcArgOIs
argument_list|)
expr_stmt|;
block|}
name|GenericUDAFEvaluator
name|wFnEval
init|=
name|def
operator|.
name|getWFnEval
argument_list|()
decl_stmt|;
name|ObjectInspector
name|OI
init|=
name|wFnEval
operator|.
name|init
argument_list|(
name|GenericUDAFEvaluator
operator|.
name|Mode
operator|.
name|COMPLETE
argument_list|,
name|funcArgOIs
argument_list|)
decl_stmt|;
name|def
operator|.
name|setWFnEval
argument_list|(
name|wFnEval
argument_list|)
expr_stmt|;
name|def
operator|.
name|setOI
argument_list|(
name|OI
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|initialize
parameter_list|(
name|BoundaryDef
name|def
parameter_list|,
name|ShapeDetails
name|inpShape
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|def
operator|instanceof
name|ValueBoundaryDef
condition|)
block|{
name|ValueBoundaryDef
name|vDef
init|=
operator|(
name|ValueBoundaryDef
operator|)
name|def
decl_stmt|;
name|initialize
argument_list|(
name|vDef
operator|.
name|getExpressionDef
argument_list|()
argument_list|,
name|inpShape
argument_list|)
expr_stmt|;
block|}
block|}
specifier|protected
name|void
name|initialize
parameter_list|(
name|PTFExpressionDef
name|eDef
parameter_list|,
name|ShapeDetails
name|inpShape
parameter_list|)
throws|throws
name|HiveException
block|{
name|ExprNodeDesc
name|exprNode
init|=
name|eDef
operator|.
name|getExprNode
argument_list|()
decl_stmt|;
name|ExprNodeEvaluator
name|exprEval
init|=
name|WindowingExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|llInfo
argument_list|,
name|exprNode
argument_list|)
decl_stmt|;
name|ObjectInspector
name|oi
init|=
name|initExprNodeEvaluator
argument_list|(
name|exprEval
argument_list|,
name|exprNode
argument_list|,
name|inpShape
argument_list|)
decl_stmt|;
name|eDef
operator|.
name|setExprEvaluator
argument_list|(
name|exprEval
argument_list|)
expr_stmt|;
name|eDef
operator|.
name|setOI
argument_list|(
name|oi
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ObjectInspector
name|initExprNodeEvaluator
parameter_list|(
name|ExprNodeEvaluator
name|exprEval
parameter_list|,
name|ExprNodeDesc
name|exprNode
parameter_list|,
name|ShapeDetails
name|inpShape
parameter_list|)
throws|throws
name|HiveException
block|{
name|ObjectInspector
name|outOI
decl_stmt|;
name|outOI
operator|=
name|exprEval
operator|.
name|initialize
argument_list|(
name|inpShape
operator|.
name|getOI
argument_list|()
argument_list|)
expr_stmt|;
comment|/*      * if there are any LeadLag functions in this Expression Tree: - setup a      * duplicate Evaluator for the 1st arg of the LLFuncDesc - initialize it      * using the InputInfo provided for this Expr tree - set the duplicate      * evaluator on the LLUDF instance.      */
name|List
argument_list|<
name|ExprNodeGenericFuncDesc
argument_list|>
name|llFuncExprs
init|=
name|llInfo
operator|.
name|getLLFuncExprsInTopExpr
argument_list|(
name|exprNode
argument_list|)
decl_stmt|;
if|if
condition|(
name|llFuncExprs
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ExprNodeGenericFuncDesc
name|llFuncExpr
range|:
name|llFuncExprs
control|)
block|{
name|ExprNodeDesc
name|firstArg
init|=
name|llFuncExpr
operator|.
name|getChildren
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ExprNodeEvaluator
name|dupExprEval
init|=
name|WindowingExprNodeEvaluatorFactory
operator|.
name|get
argument_list|(
name|llInfo
argument_list|,
name|firstArg
argument_list|)
decl_stmt|;
name|dupExprEval
operator|.
name|initialize
argument_list|(
name|inpShape
operator|.
name|getOI
argument_list|()
argument_list|)
expr_stmt|;
name|GenericUDFLeadLag
name|llFn
init|=
operator|(
name|GenericUDFLeadLag
operator|)
name|llFuncExpr
operator|.
name|getGenericUDF
argument_list|()
decl_stmt|;
name|llFn
operator|.
name|setExprEvaluator
argument_list|(
name|dupExprEval
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|outOI
return|;
block|}
specifier|protected
name|void
name|initialize
parameter_list|(
name|ShapeDetails
name|shp
parameter_list|)
throws|throws
name|HiveException
block|{
name|String
name|serdeClassName
init|=
name|shp
operator|.
name|getSerdeClassName
argument_list|()
decl_stmt|;
name|Properties
name|serDeProps
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|serdePropsMap
init|=
name|shp
operator|.
name|getSerdeProps
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|serdeName
range|:
name|serdePropsMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|serDeProps
operator|.
name|setProperty
argument_list|(
name|serdeName
argument_list|,
name|serdePropsMap
operator|.
name|get
argument_list|(
name|serdeName
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|SerDe
name|serDe
init|=
operator|(
name|SerDe
operator|)
name|SerDeUtils
operator|.
name|lookupDeserializer
argument_list|(
name|serdeClassName
argument_list|)
decl_stmt|;
name|serDe
operator|.
name|initialize
argument_list|(
name|hConf
argument_list|,
name|serDeProps
argument_list|)
expr_stmt|;
name|shp
operator|.
name|setSerde
argument_list|(
name|serDe
argument_list|)
expr_stmt|;
name|shp
operator|.
name|setOI
argument_list|(
operator|(
name|StructObjectInspector
operator|)
name|serDe
operator|.
name|getObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SerDeException
name|se
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|se
argument_list|)
throw|;
block|}
block|}
specifier|private
specifier|static
name|void
name|addInputColumnsToList
parameter_list|(
name|ShapeDetails
name|shape
parameter_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
name|fieldNames
parameter_list|,
name|ArrayList
argument_list|<
name|ObjectInspector
argument_list|>
name|fieldOIs
parameter_list|)
block|{
name|StructObjectInspector
name|OI
init|=
name|shape
operator|.
name|getOI
argument_list|()
decl_stmt|;
for|for
control|(
name|StructField
name|f
range|:
name|OI
operator|.
name|getAllStructFieldRefs
argument_list|()
control|)
block|{
name|fieldNames
operator|.
name|add
argument_list|(
name|f
operator|.
name|getFieldName
argument_list|()
argument_list|)
expr_stmt|;
name|fieldOIs
operator|.
name|add
argument_list|(
name|f
operator|.
name|getFieldObjectInspector
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|TableFunctionResolver
name|constructResolver
parameter_list|(
name|String
name|className
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Class
argument_list|<
name|?
extends|extends
name|TableFunctionResolver
argument_list|>
name|rCls
init|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|TableFunctionResolver
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
decl_stmt|;
return|return
operator|(
name|TableFunctionResolver
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|rCls
argument_list|,
literal|null
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
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
block|}
end_class

end_unit

