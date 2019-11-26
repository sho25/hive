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
name|FilterOperator
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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
operator|.
name|HiveFilter
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
name|ExprNodeConverter
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

begin_class
class|class
name|HiveFilterVisitor
extends|extends
name|HiveRelNodeVisitor
argument_list|<
name|HiveFilter
argument_list|>
block|{
name|HiveFilterVisitor
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
comment|/**    * TODO: 1) isSamplingPred 2) sampleDesc 3) isSortedFilter.    */
annotation|@
name|Override
name|OpAttr
name|visit
parameter_list|(
name|HiveFilter
name|filterRel
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
name|filterRel
operator|.
name|getInput
argument_list|()
argument_list|)
decl_stmt|;
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
name|filterRel
operator|.
name|getId
argument_list|()
operator|+
literal|":"
operator|+
name|filterRel
operator|.
name|getRelTypeName
argument_list|()
operator|+
literal|" with row type: ["
operator|+
name|filterRel
operator|.
name|getRowType
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|ExprNodeDesc
name|filCondExpr
init|=
name|filterRel
operator|.
name|getCondition
argument_list|()
operator|.
name|accept
argument_list|(
operator|new
name|ExprNodeConverter
argument_list|(
name|inputOpAf
operator|.
name|tabAlias
argument_list|,
name|filterRel
operator|.
name|getInput
argument_list|()
operator|.
name|getRowType
argument_list|()
argument_list|,
name|inputOpAf
operator|.
name|vcolsInCalcite
argument_list|,
name|filterRel
operator|.
name|getCluster
argument_list|()
operator|.
name|getTypeFactory
argument_list|()
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|FilterDesc
name|filDesc
init|=
operator|new
name|FilterDesc
argument_list|(
name|filCondExpr
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|cinfoLst
init|=
name|HiveOpConverterUtils
operator|.
name|createColInfos
argument_list|(
name|inputOpAf
operator|.
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|FilterOperator
name|filOp
init|=
operator|(
name|FilterOperator
operator|)
name|OperatorFactory
operator|.
name|getAndMakeChild
argument_list|(
name|filDesc
argument_list|,
operator|new
name|RowSchema
argument_list|(
name|cinfoLst
argument_list|)
argument_list|,
name|inputOpAf
operator|.
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
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
literal|"Generated "
operator|+
name|filOp
operator|+
literal|" with row schema: ["
operator|+
name|filOp
operator|.
name|getSchema
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
return|return
name|inputOpAf
operator|.
name|clone
argument_list|(
name|filOp
argument_list|)
return|;
block|}
block|}
end_class

end_unit

