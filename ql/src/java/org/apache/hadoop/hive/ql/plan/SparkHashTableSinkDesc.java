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

begin_comment
comment|/**  * Map Join operator Descriptor implementation.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Spark HashTable Sink Operator"
argument_list|)
specifier|public
class|class
name|SparkHashTableSinkDesc
extends|extends
name|HashTableSinkDesc
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|// The position of this table
specifier|private
name|byte
name|tag
decl_stmt|;
specifier|public
name|SparkHashTableSinkDesc
parameter_list|()
block|{   }
specifier|public
name|SparkHashTableSinkDesc
parameter_list|(
name|MapJoinDesc
name|clone
parameter_list|)
block|{
name|super
argument_list|(
name|clone
argument_list|)
expr_stmt|;
block|}
specifier|public
name|byte
name|getTag
parameter_list|()
block|{
return|return
name|tag
return|;
block|}
specifier|public
name|void
name|setTag
parameter_list|(
name|byte
name|tag
parameter_list|)
block|{
name|this
operator|.
name|tag
operator|=
name|tag
expr_stmt|;
block|}
specifier|public
class|class
name|SparkHashTableSinkOperatorExplainVectorization
extends|extends
name|OperatorExplainVectorization
block|{
specifier|private
specifier|final
name|HashTableSinkDesc
name|filterDesc
decl_stmt|;
specifier|private
specifier|final
name|VectorSparkHashTableSinkDesc
name|vectorHashTableSinkDesc
decl_stmt|;
specifier|public
name|SparkHashTableSinkOperatorExplainVectorization
parameter_list|(
name|HashTableSinkDesc
name|filterDesc
parameter_list|,
name|VectorSparkHashTableSinkDesc
name|vectorSparkHashTableSinkDesc
parameter_list|)
block|{
comment|// Native vectorization supported.
name|super
argument_list|(
name|vectorSparkHashTableSinkDesc
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|filterDesc
operator|=
name|filterDesc
expr_stmt|;
name|this
operator|.
name|vectorHashTableSinkDesc
operator|=
name|vectorSparkHashTableSinkDesc
expr_stmt|;
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
literal|"Spark Hash Table Sink Vectorization"
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
name|SparkHashTableSinkOperatorExplainVectorization
name|getHashTableSinkVectorization
parameter_list|()
block|{
name|VectorSparkHashTableSinkDesc
name|vectorHashTableSinkDesc
init|=
operator|(
name|VectorSparkHashTableSinkDesc
operator|)
name|getVectorDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|vectorHashTableSinkDesc
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
name|SparkHashTableSinkOperatorExplainVectorization
argument_list|(
name|this
argument_list|,
name|vectorHashTableSinkDesc
argument_list|)
return|;
block|}
block|}
end_class

end_unit

