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
name|exec
operator|.
name|vector
operator|.
name|ptf
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
name|LongColumnVector
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
name|VectorizedRowBatch
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
name|plan
operator|.
name|ptf
operator|.
name|WindowFrameDef
import|;
end_import

begin_comment
comment|/**  * This class evaluates rank() for a PTF group.  *  * Rank starts at 1; the same rank is streamed to the output column as repeated; after the last  * group row, the rank is increased by the number of group rows.  */
end_comment

begin_class
specifier|public
class|class
name|VectorPTFEvaluatorRank
extends|extends
name|VectorPTFEvaluatorBase
block|{
specifier|private
name|int
name|rank
decl_stmt|;
specifier|private
name|int
name|groupCount
decl_stmt|;
specifier|public
name|VectorPTFEvaluatorRank
parameter_list|(
name|WindowFrameDef
name|windowFrameDef
parameter_list|,
name|int
name|outputColumnNum
parameter_list|)
block|{
name|super
argument_list|(
name|windowFrameDef
argument_list|,
name|outputColumnNum
argument_list|)
expr_stmt|;
name|resetEvaluator
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|evaluateGroupBatch
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
throws|throws
name|HiveException
block|{
comment|// We don't evaluate input columns...
comment|/*      * Do careful maintenance of the outputColVector.noNulls flag.      */
name|LongColumnVector
name|longColVector
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|outputColumnNum
index|]
decl_stmt|;
name|longColVector
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|longColVector
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|false
expr_stmt|;
name|longColVector
operator|.
name|vector
index|[
literal|0
index|]
operator|=
name|rank
expr_stmt|;
name|groupCount
operator|+=
name|batch
operator|.
name|size
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|doLastBatchWork
parameter_list|()
block|{
name|rank
operator|+=
name|groupCount
expr_stmt|;
name|groupCount
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|streamsResult
parameter_list|()
block|{
comment|// No group value.
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|Type
name|getResultColumnVectorType
parameter_list|()
block|{
return|return
name|Type
operator|.
name|LONG
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|resetEvaluator
parameter_list|()
block|{
name|rank
operator|=
literal|1
expr_stmt|;
name|groupCount
operator|=
literal|0
expr_stmt|;
block|}
block|}
end_class

end_unit

