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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|ql
operator|.
name|exec
operator|.
name|vector
operator|.
name|VectorizationContext
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
name|vector
operator|.
name|ColumnVector
operator|.
name|Type
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
name|vector
operator|.
name|expressions
operator|.
name|IdentityExpression
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
name|vector
operator|.
name|expressions
operator|.
name|VectorExpression
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
name|vector
operator|.
name|ptf
operator|.
name|VectorPTFEvaluatorBase
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
name|plan
operator|.
name|Explain
operator|.
name|Level
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
name|Explain
operator|.
name|Vectorization
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
name|VectorPTFDesc
operator|.
name|SupportedFunctionType
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
name|ptf
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
name|plan
operator|.
name|ptf
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
name|ptf
operator|.
name|Noop
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
name|TypeInfo
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

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"PTF Operator"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
class|class
name|PTFDesc
extends|extends
name|AbstractOperatorDesc
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|PTFDesc
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|PartitionedTableFunctionDef
name|funcDef
decl_stmt|;
specifier|transient
name|LeadLagInfo
name|llInfo
decl_stmt|;
comment|/*    * is this PTFDesc for a Map-Side PTF Operation?    */
name|boolean
name|isMapSide
init|=
literal|false
decl_stmt|;
specifier|transient
name|Configuration
name|cfg
decl_stmt|;
specifier|public
name|PartitionedTableFunctionDef
name|getFuncDef
parameter_list|()
block|{
return|return
name|funcDef
return|;
block|}
specifier|public
name|void
name|setFuncDef
parameter_list|(
name|PartitionedTableFunctionDef
name|funcDef
parameter_list|)
block|{
name|this
operator|.
name|funcDef
operator|=
name|funcDef
expr_stmt|;
block|}
specifier|public
name|PartitionedTableFunctionDef
name|getStartOfChain
parameter_list|()
block|{
return|return
name|funcDef
operator|==
literal|null
condition|?
literal|null
else|:
name|funcDef
operator|.
name|getStartOfChain
argument_list|()
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Function definitions"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|List
argument_list|<
name|PTFInputDef
argument_list|>
name|getFuncDefExplain
parameter_list|()
block|{
if|if
condition|(
name|funcDef
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|PTFInputDef
argument_list|>
name|inputs
init|=
operator|new
name|ArrayList
argument_list|<
name|PTFInputDef
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|PTFInputDef
name|current
init|=
name|funcDef
init|;
name|current
operator|!=
literal|null
condition|;
name|current
operator|=
name|current
operator|.
name|getInput
argument_list|()
control|)
block|{
name|inputs
operator|.
name|add
argument_list|(
name|current
argument_list|)
expr_stmt|;
block|}
name|Collections
operator|.
name|reverse
argument_list|(
name|inputs
argument_list|)
expr_stmt|;
return|return
name|inputs
return|;
block|}
specifier|public
name|LeadLagInfo
name|getLlInfo
parameter_list|()
block|{
return|return
name|llInfo
return|;
block|}
specifier|public
name|void
name|setLlInfo
parameter_list|(
name|LeadLagInfo
name|llInfo
parameter_list|)
block|{
name|this
operator|.
name|llInfo
operator|=
name|llInfo
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Lead/Lag information"
argument_list|)
specifier|public
name|String
name|getLlInfoExplain
parameter_list|()
block|{
if|if
condition|(
name|llInfo
operator|!=
literal|null
operator|&&
name|llInfo
operator|.
name|getLeadLagExprs
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|PlanUtils
operator|.
name|getExprListString
argument_list|(
name|llInfo
operator|.
name|getLeadLagExprs
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|boolean
name|forWindowing
parameter_list|()
block|{
return|return
name|funcDef
operator|instanceof
name|WindowTableFunctionDef
return|;
block|}
specifier|public
name|boolean
name|forNoop
parameter_list|()
block|{
return|return
name|funcDef
operator|.
name|getTFunction
argument_list|()
operator|instanceof
name|Noop
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Map-side function"
argument_list|,
name|displayOnlyOnTrue
operator|=
literal|true
argument_list|)
specifier|public
name|boolean
name|isMapSide
parameter_list|()
block|{
return|return
name|isMapSide
return|;
block|}
specifier|public
name|void
name|setMapSide
parameter_list|(
name|boolean
name|isMapSide
parameter_list|)
block|{
name|this
operator|.
name|isMapSide
operator|=
name|isMapSide
expr_stmt|;
block|}
specifier|public
name|Configuration
name|getCfg
parameter_list|()
block|{
return|return
name|cfg
return|;
block|}
specifier|public
name|void
name|setCfg
parameter_list|(
name|Configuration
name|cfg
parameter_list|)
block|{
name|this
operator|.
name|cfg
operator|=
name|cfg
expr_stmt|;
block|}
comment|// Since we don't have a non-native or pass-thru version of VectorPTFOperator, we do not
comment|// have enableConditionsMet / enableConditionsNotMet like we have for VectorReduceSinkOperator,
comment|// etc.
specifier|public
class|class
name|PTFOperatorExplainVectorization
extends|extends
name|OperatorExplainVectorization
block|{
specifier|private
specifier|final
name|PTFDesc
name|PTFDesc
decl_stmt|;
specifier|private
specifier|final
name|VectorPTFDesc
name|vectorPTFDesc
decl_stmt|;
specifier|private
specifier|final
name|VectorPTFInfo
name|vectorPTFInfo
decl_stmt|;
specifier|private
name|VectorizationCondition
index|[]
name|nativeConditions
decl_stmt|;
specifier|public
name|PTFOperatorExplainVectorization
parameter_list|(
name|PTFDesc
name|PTFDesc
parameter_list|,
name|VectorDesc
name|vectorDesc
parameter_list|)
block|{
comment|// VectorPTFOperator is native vectorized.
name|super
argument_list|(
name|vectorDesc
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|PTFDesc
operator|=
name|PTFDesc
expr_stmt|;
name|vectorPTFDesc
operator|=
operator|(
name|VectorPTFDesc
operator|)
name|vectorDesc
expr_stmt|;
name|vectorPTFInfo
operator|=
name|vectorPTFDesc
operator|.
name|getVectorPTFInfo
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|EXPRESSION
argument_list|,
name|displayName
operator|=
literal|"functionNames"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getFunctionNames
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|toString
argument_list|(
name|vectorPTFDesc
operator|.
name|getEvaluatorFunctionNames
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|EXPRESSION
argument_list|,
name|displayName
operator|=
literal|"functionInputExpressions"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getFunctionInputExpressions
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|toString
argument_list|(
name|vectorPTFInfo
operator|.
name|getEvaluatorInputExpressions
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|EXPRESSION
argument_list|,
name|displayName
operator|=
literal|"partitionExpressions"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getPartitionExpressions
parameter_list|()
block|{
name|VectorExpression
index|[]
name|partitionExpressions
init|=
name|vectorPTFInfo
operator|.
name|getPartitionExpressions
argument_list|()
decl_stmt|;
if|if
condition|(
name|partitionExpressions
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|Arrays
operator|.
name|toString
argument_list|(
name|partitionExpressions
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|EXPRESSION
argument_list|,
name|displayName
operator|=
literal|"orderExpressions"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getOrderExpressions
parameter_list|()
block|{
name|VectorExpression
index|[]
name|orderExpressions
init|=
name|vectorPTFInfo
operator|.
name|getOrderExpressions
argument_list|()
decl_stmt|;
if|if
condition|(
name|orderExpressions
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|Arrays
operator|.
name|toString
argument_list|(
name|orderExpressions
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|EXPRESSION
argument_list|,
name|displayName
operator|=
literal|"evaluatorClasses"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getEvaluatorClasses
parameter_list|()
block|{
name|VectorPTFEvaluatorBase
index|[]
name|evaluators
init|=
name|VectorPTFDesc
operator|.
name|getEvaluators
argument_list|(
name|vectorPTFDesc
argument_list|,
name|vectorPTFInfo
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|evaluators
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|VectorPTFEvaluatorBase
name|evaluator
range|:
name|evaluators
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|evaluator
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|result
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|DETAIL
argument_list|,
name|displayName
operator|=
literal|"outputColumns"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getOutputColumns
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|toString
argument_list|(
name|vectorPTFInfo
operator|.
name|getOutputColumnMap
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|DETAIL
argument_list|,
name|displayName
operator|=
literal|"outputTypes"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getOutputTypes
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|toString
argument_list|(
name|vectorPTFDesc
operator|.
name|getOutputTypeInfos
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|DETAIL
argument_list|,
name|displayName
operator|=
literal|"keyInputColumns"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getKeyInputColumns
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|toString
argument_list|(
name|vectorPTFInfo
operator|.
name|getKeyInputColumnMap
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|DETAIL
argument_list|,
name|displayName
operator|=
literal|"nonKeyInputColumns"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getNonKeyInputColumns
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|toString
argument_list|(
name|vectorPTFInfo
operator|.
name|getNonKeyInputColumnMap
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|DETAIL
argument_list|,
name|displayName
operator|=
literal|"streamingColumns"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|String
name|getStreamingColumns
parameter_list|()
block|{
name|VectorPTFEvaluatorBase
index|[]
name|evaluators
init|=
name|VectorPTFDesc
operator|.
name|getEvaluators
argument_list|(
name|vectorPTFDesc
argument_list|,
name|vectorPTFInfo
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|Integer
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|VectorPTFEvaluatorBase
name|evaluator
range|:
name|evaluators
control|)
block|{
if|if
condition|(
name|evaluator
operator|.
name|streamsResult
argument_list|()
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|evaluator
operator|.
name|getOutputColumnNum
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|OPERATOR
argument_list|,
name|displayName
operator|=
literal|"PTF Vectorization"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|PTFOperatorExplainVectorization
name|getPTFVectorization
parameter_list|()
block|{
if|if
condition|(
name|vectorDesc
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|PTFOperatorExplainVectorization
argument_list|(
name|this
argument_list|,
name|vectorDesc
argument_list|)
return|;
block|}
block|}
end_class

end_unit

