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

begin_class
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"Group By Operator"
argument_list|)
specifier|public
class|class
name|groupByDesc
implements|implements
name|java
operator|.
name|io
operator|.
name|Serializable
block|{
comment|/** Group-by Mode:    *  COMPLETE: complete 1-phase aggregation: aggregate, evaluate    *  PARTIAL1: partial aggregation - first phase:  aggregate, evaluatePartial    *  PARTIAL2: partial aggregation - second phase: aggregatePartial, evaluatePartial    *  FINAL: partial aggregation - final phase: aggregatePartial, evaluate    *  HASH: the same as PARTIAL1 but use hash-table-based aggregation      */
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
specifier|static
enum|enum
name|Mode
block|{
name|COMPLETE
block|,
name|PARTIAL1
block|,
name|PARTIAL2
block|,
name|FINAL
block|,
name|HASH
block|}
empty_stmt|;
specifier|private
name|Mode
name|mode
decl_stmt|;
specifier|private
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|keys
decl_stmt|;
specifier|private
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
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
name|aggregationDesc
argument_list|>
name|aggregators
decl_stmt|;
specifier|public
name|groupByDesc
parameter_list|()
block|{ }
specifier|public
name|groupByDesc
parameter_list|(
specifier|final
name|Mode
name|mode
parameter_list|,
specifier|final
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|keys
parameter_list|,
specifier|final
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
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
name|aggregationDesc
argument_list|>
name|aggregators
parameter_list|)
block|{
name|this
operator|.
name|mode
operator|=
name|mode
expr_stmt|;
name|this
operator|.
name|keys
operator|=
name|keys
expr_stmt|;
name|this
operator|.
name|aggregators
operator|=
name|aggregators
expr_stmt|;
block|}
specifier|public
name|Mode
name|getMode
parameter_list|()
block|{
return|return
name|this
operator|.
name|mode
return|;
block|}
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"mode"
argument_list|)
specifier|public
name|String
name|getModeString
parameter_list|()
block|{
switch|switch
condition|(
name|mode
condition|)
block|{
case|case
name|COMPLETE
case|:
return|return
literal|"complete"
return|;
case|case
name|PARTIAL1
case|:
return|return
literal|"partial1"
return|;
case|case
name|PARTIAL2
case|:
return|return
literal|"partial2"
return|;
case|case
name|HASH
case|:
return|return
literal|"hash"
return|;
block|}
return|return
literal|"unknown"
return|;
block|}
specifier|public
name|void
name|setMode
parameter_list|(
specifier|final
name|Mode
name|mode
parameter_list|)
block|{
name|this
operator|.
name|mode
operator|=
name|mode
expr_stmt|;
block|}
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"keys"
argument_list|)
specifier|public
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|getKeys
parameter_list|()
block|{
return|return
name|this
operator|.
name|keys
return|;
block|}
specifier|public
name|void
name|setKeys
parameter_list|(
specifier|final
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
name|exprNodeDesc
argument_list|>
name|keys
parameter_list|)
block|{
name|this
operator|.
name|keys
operator|=
name|keys
expr_stmt|;
block|}
annotation|@
name|explain
argument_list|(
name|displayName
operator|=
literal|"aggregations"
argument_list|)
specifier|public
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
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
name|aggregationDesc
argument_list|>
name|getAggregators
parameter_list|()
block|{
return|return
name|this
operator|.
name|aggregators
return|;
block|}
specifier|public
name|void
name|setAggregators
parameter_list|(
specifier|final
name|java
operator|.
name|util
operator|.
name|ArrayList
argument_list|<
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
name|aggregationDesc
argument_list|>
name|aggregators
parameter_list|)
block|{
name|this
operator|.
name|aggregators
operator|=
name|aggregators
expr_stmt|;
block|}
block|}
end_class

end_unit

