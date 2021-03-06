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
name|optimizer
operator|.
name|calcite
operator|.
name|translator
operator|.
name|opconventer
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
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|RelDistribution
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|rel
operator|.
name|RelDistribution
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
name|io
operator|.
name|AcidUtils
operator|.
name|Operation
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveSortExchange
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
name|optimizer
operator|.
name|calcite
operator|.
name|translator
operator|.
name|opconventer
operator|.
name|HiveOpConverter
operator|.
name|OpAttr
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
name|ExprNodeDesc
import|;
end_import

begin_class
class|class
name|HiveSortExchangeVisitor
extends|extends
name|HiveRelNodeVisitor
argument_list|<
name|HiveSortExchange
argument_list|>
block|{
name|HiveSortExchangeVisitor
parameter_list|(
name|HiveOpConverter
name|hiveOpConverter
parameter_list|)
block|{
name|super
argument_list|(
name|hiveOpConverter
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|OpAttr
name|visit
parameter_list|(
name|HiveSortExchange
name|exchangeRel
parameter_list|)
throws|throws
name|SemanticException
block|{
name|OpAttr
name|inputOpAf
init|=
name|hiveOpConverter
operator|.
name|dispatch
argument_list|(
name|exchangeRel
operator|.
name|getInput
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|tabAlias
init|=
name|inputOpAf
operator|.
name|tabAlias
decl_stmt|;
if|if
condition|(
name|tabAlias
operator|==
literal|null
operator|||
name|tabAlias
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
name|tabAlias
operator|=
name|hiveOpConverter
operator|.
name|getHiveDerivedTableAlias
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Translating operator rel#"
operator|+
name|exchangeRel
operator|.
name|getId
argument_list|()
operator|+
literal|":"
operator|+
name|exchangeRel
operator|.
name|getRelTypeName
argument_list|()
operator|+
literal|" with row type: ["
operator|+
name|exchangeRel
operator|.
name|getRowType
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|RelDistribution
name|distribution
init|=
name|exchangeRel
operator|.
name|getDistribution
argument_list|()
decl_stmt|;
if|if
condition|(
name|distribution
operator|.
name|getType
argument_list|()
operator|!=
name|Type
operator|.
name|HASH_DISTRIBUTED
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Only hash distribution supported for LogicalExchange"
argument_list|)
throw|;
block|}
name|ExprNodeDesc
index|[]
name|expressions
init|=
operator|new
name|ExprNodeDesc
index|[
name|exchangeRel
operator|.
name|getJoinKeys
argument_list|()
operator|.
name|size
argument_list|()
index|]
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
name|exchangeRel
operator|.
name|getJoinKeys
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|index
operator|++
control|)
block|{
name|expressions
index|[
name|index
index|]
operator|=
name|HiveOpConverterUtils
operator|.
name|convertToExprNode
argument_list|(
name|exchangeRel
operator|.
name|getJoinKeys
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
argument_list|,
name|exchangeRel
operator|.
name|getInput
argument_list|()
argument_list|,
name|inputOpAf
operator|.
name|tabAlias
argument_list|,
name|inputOpAf
operator|.
name|vcolsInCalcite
argument_list|)
expr_stmt|;
block|}
name|exchangeRel
operator|.
name|setJoinExpressions
argument_list|(
name|expressions
argument_list|)
expr_stmt|;
name|ReduceSinkOperator
name|rsOp
init|=
name|genReduceSink
argument_list|(
name|inputOpAf
operator|.
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|tabAlias
argument_list|,
name|expressions
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|,
name|Operation
operator|.
name|NOT_ACID
argument_list|,
name|hiveOpConverter
operator|.
name|getHiveConf
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|OpAttr
argument_list|(
name|tabAlias
argument_list|,
name|inputOpAf
operator|.
name|vcolsInCalcite
argument_list|,
name|rsOp
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|ReduceSinkOperator
name|genReduceSink
parameter_list|(
name|Operator
argument_list|<
name|?
argument_list|>
name|input
parameter_list|,
name|String
name|tableAlias
parameter_list|,
name|ExprNodeDesc
index|[]
name|keys
parameter_list|,
name|int
name|tag
parameter_list|,
name|int
name|numReducers
parameter_list|,
name|Operation
name|acidOperation
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
name|HiveOpConverterUtils
operator|.
name|genReduceSink
argument_list|(
name|input
argument_list|,
name|tableAlias
argument_list|,
name|keys
argument_list|,
name|tag
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|ExprNodeDesc
argument_list|>
argument_list|()
argument_list|,
literal|""
argument_list|,
literal|""
argument_list|,
name|numReducers
argument_list|,
name|acidOperation
argument_list|,
name|hiveConf
argument_list|)
return|;
block|}
block|}
end_class

end_unit

