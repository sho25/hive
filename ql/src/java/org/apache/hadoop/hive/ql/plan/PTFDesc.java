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
name|PTFUtils
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
name|WindowTableFunctionDef
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

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"PTF Operator"
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
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
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
static|static
block|{
name|PTFUtils
operator|.
name|makeTransient
argument_list|(
name|PTFDesc
operator|.
name|class
argument_list|,
literal|"llInfo"
argument_list|)
expr_stmt|;
name|PTFUtils
operator|.
name|makeTransient
argument_list|(
name|PTFDesc
operator|.
name|class
argument_list|,
literal|"cfg"
argument_list|)
expr_stmt|;
block|}
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
block|}
end_class

end_unit

