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
name|spark
package|;
end_package

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
name|SemanticNodeProcessor
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
name|optimizer
operator|.
name|BucketJoinProcCtx
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
name|BucketMapjoinProc
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
name|SortBucketJoinProcCtx
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
name|parse
operator|.
name|spark
operator|.
name|OptimizeSparkProcContext
import|;
end_import

begin_comment
comment|/**  * This processes joins in which user specified a hint to identify the small-table.  * Currently it takes a mapjoin already converted from hints, and converts it further  * to BucketMapJoin or SMBMapJoin using same small-table identification.  *  * The idea is eventually to process even hinted Mapjoin hints here,  * but due to code complexity in refactoring, that is still in Optimizer.  */
end_comment

begin_class
specifier|public
class|class
name|SparkJoinHintOptimizer
implements|implements
name|SemanticNodeProcessor
block|{
specifier|private
name|BucketMapjoinProc
name|bucketMapJoinOptimizer
decl_stmt|;
specifier|private
name|SparkSMBJoinHintOptimizer
name|smbMapJoinOptimizer
decl_stmt|;
specifier|public
name|SparkJoinHintOptimizer
parameter_list|(
name|ParseContext
name|parseCtx
parameter_list|)
block|{
name|bucketMapJoinOptimizer
operator|=
operator|new
name|BucketMapjoinProc
argument_list|(
name|parseCtx
argument_list|)
expr_stmt|;
name|smbMapJoinOptimizer
operator|=
operator|new
name|SparkSMBJoinHintOptimizer
argument_list|(
name|parseCtx
argument_list|)
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
name|OptimizeSparkProcContext
name|context
init|=
operator|(
name|OptimizeSparkProcContext
operator|)
name|procCtx
decl_stmt|;
name|HiveConf
name|hiveConf
init|=
name|context
operator|.
name|getParseContext
argument_list|()
operator|.
name|getConf
argument_list|()
decl_stmt|;
comment|// Convert from mapjoin to bucket map join if enabled.
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
name|HIVEOPTBUCKETMAPJOIN
argument_list|)
operator|||
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEOPTSORTMERGEBUCKETMAPJOIN
argument_list|)
condition|)
block|{
name|BucketJoinProcCtx
name|bjProcCtx
init|=
operator|new
name|BucketJoinProcCtx
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|bucketMapJoinOptimizer
operator|.
name|process
argument_list|(
name|nd
argument_list|,
name|stack
argument_list|,
name|bjProcCtx
argument_list|,
name|nodeOutputs
argument_list|)
expr_stmt|;
block|}
comment|// Convert from bucket map join to sort merge bucket map join if enabled.
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
name|HIVEOPTSORTMERGEBUCKETMAPJOIN
argument_list|)
condition|)
block|{
name|SortBucketJoinProcCtx
name|smbJoinCtx
init|=
operator|new
name|SortBucketJoinProcCtx
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|smbMapJoinOptimizer
operator|.
name|process
argument_list|(
name|nd
argument_list|,
name|stack
argument_list|,
name|smbJoinCtx
argument_list|,
name|nodeOutputs
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

