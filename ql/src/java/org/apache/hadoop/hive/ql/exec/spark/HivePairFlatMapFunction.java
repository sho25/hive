begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  Licensed to the Apache Software Foundation (ASF) under one  *  or more contributor license agreements.  See the NOTICE file  *  distributed with this work for additional information  *  regarding copyright ownership.  The ASF licenses this file  *  to you under the Apache License, Version 2.0 (the  *  "License"); you may not use this file except in compliance  *  with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|exec
operator|.
name|spark
package|;
end_package

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|NumberFormat
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
name|mapred
operator|.
name|JobConf
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|TaskContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|function
operator|.
name|PairFlatMapFunction
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|HivePairFlatMapFunction
parameter_list|<
name|T
parameter_list|,
name|K
parameter_list|,
name|V
parameter_list|>
implements|implements
name|PairFlatMapFunction
argument_list|<
name|T
argument_list|,
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|private
specifier|final
name|NumberFormat
name|taskIdFormat
init|=
name|NumberFormat
operator|.
name|getInstance
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|NumberFormat
name|stageIdFormat
init|=
name|NumberFormat
operator|.
name|getInstance
argument_list|()
decl_stmt|;
block|{
name|taskIdFormat
operator|.
name|setGroupingUsed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|taskIdFormat
operator|.
name|setMinimumIntegerDigits
argument_list|(
literal|6
argument_list|)
expr_stmt|;
name|stageIdFormat
operator|.
name|setGroupingUsed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|stageIdFormat
operator|.
name|setMinimumIntegerDigits
argument_list|(
literal|4
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|transient
name|JobConf
name|jobConf
decl_stmt|;
specifier|protected
name|SparkReporter
name|sparkReporter
decl_stmt|;
specifier|private
name|byte
index|[]
name|buffer
decl_stmt|;
specifier|public
name|HivePairFlatMapFunction
parameter_list|(
name|byte
index|[]
name|buffer
parameter_list|,
name|SparkReporter
name|sparkReporter
parameter_list|)
block|{
name|this
operator|.
name|buffer
operator|=
name|buffer
expr_stmt|;
name|this
operator|.
name|sparkReporter
operator|=
name|sparkReporter
expr_stmt|;
block|}
specifier|protected
name|void
name|initJobConf
parameter_list|()
block|{
if|if
condition|(
name|jobConf
operator|==
literal|null
condition|)
block|{
name|jobConf
operator|=
name|KryoSerializer
operator|.
name|deserializeJobConf
argument_list|(
name|this
operator|.
name|buffer
argument_list|)
expr_stmt|;
name|SmallTableCache
operator|.
name|initialize
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
name|setupMRLegacyConfigs
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
specifier|abstract
name|boolean
name|isMap
parameter_list|()
function_decl|;
comment|// Some Hive features depends on several MR configuration legacy, build and add
comment|// these configuration to JobConf here.
specifier|private
name|void
name|setupMRLegacyConfigs
parameter_list|()
block|{
name|StringBuilder
name|taskAttemptIdBuilder
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"attempt_"
argument_list|)
decl_stmt|;
name|taskAttemptIdBuilder
operator|.
name|append
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"_"
argument_list|)
operator|.
name|append
argument_list|(
name|stageIdFormat
operator|.
name|format
argument_list|(
name|TaskContext
operator|.
name|get
argument_list|()
operator|.
name|stageId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"_"
argument_list|)
expr_stmt|;
if|if
condition|(
name|isMap
argument_list|()
condition|)
block|{
name|taskAttemptIdBuilder
operator|.
name|append
argument_list|(
literal|"m_"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|taskAttemptIdBuilder
operator|.
name|append
argument_list|(
literal|"r_"
argument_list|)
expr_stmt|;
block|}
comment|// Spark task attempt id is increased by Spark context instead of task, which may introduce
comment|// unstable qtest output, since non Hive features depends on this, we always set it to 0 here.
name|taskAttemptIdBuilder
operator|.
name|append
argument_list|(
name|taskIdFormat
operator|.
name|format
argument_list|(
name|TaskContext
operator|.
name|get
argument_list|()
operator|.
name|partitionId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"_0"
argument_list|)
expr_stmt|;
name|String
name|taskAttemptIdStr
init|=
name|taskAttemptIdBuilder
operator|.
name|toString
argument_list|()
decl_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
literal|"mapred.task.id"
argument_list|,
name|taskAttemptIdStr
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|set
argument_list|(
literal|"mapreduce.task.attempt.id"
argument_list|,
name|taskAttemptIdStr
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|setInt
argument_list|(
literal|"mapred.task.partition"
argument_list|,
name|TaskContext
operator|.
name|get
argument_list|()
operator|.
name|partitionId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

