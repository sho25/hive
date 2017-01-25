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
operator|.
name|calcite
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|config
operator|.
name|CalciteConnectionConfig
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
name|plan
operator|.
name|Context
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
name|RelNode
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
name|cost
operator|.
name|HiveAlgorithmsConf
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
name|rules
operator|.
name|HiveRulesRegistry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_class
specifier|public
class|class
name|HivePlannerContext
implements|implements
name|Context
block|{
specifier|private
name|HiveAlgorithmsConf
name|algoConfig
decl_stmt|;
specifier|private
name|HiveRulesRegistry
name|registry
decl_stmt|;
specifier|private
name|CalciteConnectionConfig
name|calciteConfig
decl_stmt|;
specifier|private
name|Set
argument_list|<
name|RelNode
argument_list|>
name|corrScalarRexSQWithAgg
decl_stmt|;
specifier|public
name|HivePlannerContext
parameter_list|(
name|HiveAlgorithmsConf
name|algoConfig
parameter_list|,
name|HiveRulesRegistry
name|registry
parameter_list|,
name|CalciteConnectionConfig
name|calciteConfig
parameter_list|,
name|Set
argument_list|<
name|RelNode
argument_list|>
name|corrScalarRexSQWithAgg
parameter_list|)
block|{
name|this
operator|.
name|algoConfig
operator|=
name|algoConfig
expr_stmt|;
name|this
operator|.
name|registry
operator|=
name|registry
expr_stmt|;
name|this
operator|.
name|calciteConfig
operator|=
name|calciteConfig
expr_stmt|;
comment|// this is to keep track if a subquery is correlated and contains aggregate
comment|// this is computed in CalcitePlanner while planning and is later required by subuery remove rule
comment|// hence this is passed using HivePlannerContext
name|this
operator|.
name|corrScalarRexSQWithAgg
operator|=
name|corrScalarRexSQWithAgg
expr_stmt|;
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|unwrap
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|clazz
parameter_list|)
block|{
if|if
condition|(
name|clazz
operator|.
name|isInstance
argument_list|(
name|algoConfig
argument_list|)
condition|)
block|{
return|return
name|clazz
operator|.
name|cast
argument_list|(
name|algoConfig
argument_list|)
return|;
block|}
if|if
condition|(
name|clazz
operator|.
name|isInstance
argument_list|(
name|registry
argument_list|)
condition|)
block|{
return|return
name|clazz
operator|.
name|cast
argument_list|(
name|registry
argument_list|)
return|;
block|}
if|if
condition|(
name|clazz
operator|.
name|isInstance
argument_list|(
name|calciteConfig
argument_list|)
condition|)
block|{
return|return
name|clazz
operator|.
name|cast
argument_list|(
name|calciteConfig
argument_list|)
return|;
block|}
if|if
condition|(
name|clazz
operator|.
name|isInstance
argument_list|(
name|corrScalarRexSQWithAgg
argument_list|)
condition|)
block|{
return|return
name|clazz
operator|.
name|cast
argument_list|(
name|corrScalarRexSQWithAgg
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

