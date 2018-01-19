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
name|CompilationOpContext
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
name|OperatorDesc
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
name|VectorDesc
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
name|serde2
operator|.
name|objectinspector
operator|.
name|StructObjectInspector
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * This is the *NON-NATIVE* vector map join operator for just LEFT OUTER JOIN and filtered.  *  * It is a row pass-thru so that super MapJoinOperator can do the outer join filtering properly.  *  */
end_comment

begin_class
specifier|public
class|class
name|VectorMapJoinOuterFilteredOperator
extends|extends
name|VectorMapJoinBaseOperator
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|// The above members are initialized by the constructor and must not be
comment|// transient.
comment|//---------------------------------------------------------------------------
specifier|private
specifier|transient
name|boolean
name|firstBatch
decl_stmt|;
specifier|private
specifier|transient
name|VectorExtractRow
name|vectorExtractRow
decl_stmt|;
specifier|protected
specifier|transient
name|Object
index|[]
name|singleRow
decl_stmt|;
specifier|protected
specifier|transient
name|int
name|savePosBigTable
decl_stmt|;
comment|/** Kryo ctor. */
annotation|@
name|VisibleForTesting
specifier|public
name|VectorMapJoinOuterFilteredOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorMapJoinOuterFilteredOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|)
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
specifier|public
name|VectorMapJoinOuterFilteredOperator
parameter_list|(
name|CompilationOpContext
name|ctx
parameter_list|,
name|OperatorDesc
name|conf
parameter_list|,
name|VectorizationContext
name|vContext
parameter_list|,
name|VectorDesc
name|vectorDesc
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
argument_list|(
name|ctx
argument_list|,
name|conf
argument_list|,
name|vContext
argument_list|,
name|vectorDesc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
specifier|final
name|int
name|posBigTable
init|=
name|conf
operator|.
name|getPosBigTable
argument_list|()
decl_stmt|;
name|savePosBigTable
operator|=
name|posBigTable
expr_stmt|;
comment|// We need a input object inspector that is for the row we will extract out of the
comment|// vectorized row batch, not for example, an original inspector for an ORC table, etc.
name|inputObjInspectors
index|[
name|posBigTable
index|]
operator|=
name|VectorizedBatchUtil
operator|.
name|convertToStandardStructObjectInspector
argument_list|(
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
name|posBigTable
index|]
argument_list|)
expr_stmt|;
comment|// Call super VectorMapJoinOuterFilteredOperator, which calls super MapJoinOperator with
comment|// new input inspector.
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|firstBatch
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|data
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
name|VectorizedRowBatch
name|batch
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|data
decl_stmt|;
comment|// Preparation for hybrid grace hash join
name|this
operator|.
name|tag
operator|=
name|tag
expr_stmt|;
if|if
condition|(
name|scratchBatch
operator|==
literal|null
condition|)
block|{
name|scratchBatch
operator|=
name|VectorizedBatchUtil
operator|.
name|makeLike
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|firstBatch
condition|)
block|{
name|vectorExtractRow
operator|=
operator|new
name|VectorExtractRow
argument_list|()
expr_stmt|;
name|vectorExtractRow
operator|.
name|init
argument_list|(
operator|(
name|StructObjectInspector
operator|)
name|inputObjInspectors
index|[
name|savePosBigTable
index|]
argument_list|,
name|vContext
operator|.
name|getProjectedColumns
argument_list|()
argument_list|)
expr_stmt|;
name|singleRow
operator|=
operator|new
name|Object
index|[
name|vectorExtractRow
operator|.
name|getCount
argument_list|()
index|]
expr_stmt|;
name|firstBatch
operator|=
literal|false
expr_stmt|;
block|}
comment|// VectorizedBatchUtil.debugDisplayBatch( batch, "VectorReduceSinkOperator processOp ");
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|int
name|selected
index|[]
init|=
name|batch
operator|.
name|selected
decl_stmt|;
for|for
control|(
name|int
name|logical
init|=
literal|0
init|;
name|logical
operator|<
name|batch
operator|.
name|size
condition|;
name|logical
operator|++
control|)
block|{
name|int
name|batchIndex
init|=
name|selected
index|[
name|logical
index|]
decl_stmt|;
name|vectorExtractRow
operator|.
name|extractRow
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|,
name|singleRow
argument_list|)
expr_stmt|;
name|super
operator|.
name|process
argument_list|(
name|singleRow
argument_list|,
name|tag
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|batchIndex
init|=
literal|0
init|;
name|batchIndex
operator|<
name|batch
operator|.
name|size
condition|;
name|batchIndex
operator|++
control|)
block|{
name|vectorExtractRow
operator|.
name|extractRow
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|,
name|singleRow
argument_list|)
expr_stmt|;
name|super
operator|.
name|process
argument_list|(
name|singleRow
argument_list|,
name|tag
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

