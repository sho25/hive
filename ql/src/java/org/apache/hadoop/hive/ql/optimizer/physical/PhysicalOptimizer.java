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
name|physical
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
name|SemanticException
import|;
end_import

begin_comment
comment|/**  * A hierarchy physical optimizer, which contains a list of  * PhysicalPlanResolver. Each resolver has its own set of optimization rule.  */
end_comment

begin_class
specifier|public
class|class
name|PhysicalOptimizer
block|{
specifier|private
name|PhysicalContext
name|pctx
decl_stmt|;
specifier|private
name|List
argument_list|<
name|PhysicalPlanResolver
argument_list|>
name|resolvers
decl_stmt|;
specifier|public
name|PhysicalOptimizer
parameter_list|(
name|PhysicalContext
name|pctx
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|pctx
operator|=
name|pctx
expr_stmt|;
name|initialize
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
block|}
comment|/**    * create the list of physical plan resolvers.    *    * @param hiveConf    */
specifier|private
name|void
name|initialize
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|resolvers
operator|=
operator|new
name|ArrayList
argument_list|<
name|PhysicalPlanResolver
argument_list|>
argument_list|()
expr_stmt|;
if|if
condition|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESKEWJOIN
argument_list|)
condition|)
block|{
name|resolvers
operator|.
name|add
argument_list|(
operator|new
name|SkewJoinResolver
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVECONVERTJOIN
argument_list|)
condition|)
block|{
name|resolvers
operator|.
name|add
argument_list|(
operator|new
name|CommonJoinResolver
argument_list|()
argument_list|)
expr_stmt|;
comment|// The joins have been automatically converted to map-joins.
comment|// However, if the joins were converted to sort-merge joins automatically,
comment|// they should also be tried as map-joins.
if|if
condition|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTO_SORTMERGE_JOIN_TOMAPJOIN
argument_list|)
condition|)
block|{
name|resolvers
operator|.
name|add
argument_list|(
operator|new
name|SortMergeJoinResolver
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|resolvers
operator|.
name|add
argument_list|(
operator|new
name|MapJoinResolver
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMETADATAONLYQUERIES
argument_list|)
condition|)
block|{
name|resolvers
operator|.
name|add
argument_list|(
operator|new
name|MetadataOnlyOptimizer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVENULLSCANOPTIMIZE
argument_list|)
condition|)
block|{
name|resolvers
operator|.
name|add
argument_list|(
operator|new
name|NullScanOptimizer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESAMPLINGFORORDERBY
argument_list|)
condition|)
block|{
name|resolvers
operator|.
name|add
argument_list|(
operator|new
name|SamplingOptimizer
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Physical optimizers which follow this need to be careful not to invalidate the inferences
comment|// made by this optimizer. Only optimizers which depend on the results of this one should
comment|// follow it.
if|if
condition|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_INFER_BUCKET_SORT
argument_list|)
condition|)
block|{
name|resolvers
operator|.
name|add
argument_list|(
operator|new
name|BucketingSortingInferenceOptimizer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_CHECK_CROSS_PRODUCT
argument_list|)
condition|)
block|{
name|resolvers
operator|.
name|add
argument_list|(
operator|new
name|CrossProductHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Vectorization should be the last optimization, because it doesn't modify the plan
comment|// or any operators. It makes a very low level transformation to the expressions to
comment|// run in the vectorized mode.
if|if
condition|(
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_VECTORIZATION_ENABLED
argument_list|)
condition|)
block|{
name|resolvers
operator|.
name|add
argument_list|(
operator|new
name|Vectorizer
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
literal|"none"
operator|.
name|equalsIgnoreCase
argument_list|(
name|hiveConf
operator|.
name|getVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESTAGEIDREARRANGE
argument_list|)
argument_list|)
condition|)
block|{
name|resolvers
operator|.
name|add
argument_list|(
operator|new
name|StageIDsRearranger
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|pctx
operator|.
name|getContext
argument_list|()
operator|.
name|getExplainAnalyze
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|resolvers
operator|.
name|add
argument_list|(
operator|new
name|AnnotateRunTimeStatsOptimizer
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * invoke all the resolvers one-by-one, and alter the physical plan.    *    * @return PhysicalContext    * @throws HiveException    */
specifier|public
name|PhysicalContext
name|optimize
parameter_list|()
throws|throws
name|SemanticException
block|{
for|for
control|(
name|PhysicalPlanResolver
name|r
range|:
name|resolvers
control|)
block|{
name|pctx
operator|=
name|r
operator|.
name|resolve
argument_list|(
name|pctx
argument_list|)
expr_stmt|;
block|}
return|return
name|pctx
return|;
block|}
block|}
end_class

end_unit

