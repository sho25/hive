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
name|reducesink
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
name|exec
operator|.
name|vector
operator|.
name|VectorizationContext
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
name|exec
operator|.
name|vector
operator|.
name|expressions
operator|.
name|VectorExpression
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * This class is the UniformHash empty key operator class for native vectorized reduce sink.  *  * Since there is no key, we initialize the keyWritable once with an empty value.  */
end_comment

begin_class
specifier|public
class|class
name|VectorReduceSinkEmptyKeyOperator
extends|extends
name|VectorReduceSinkCommonOperator
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CLASS_NAME
init|=
name|VectorReduceSinkEmptyKeyOperator
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|CLASS_NAME
argument_list|)
decl_stmt|;
comment|// The above members are initialized by the constructor and must not be
comment|// transient.
comment|//---------------------------------------------------------------------------
specifier|private
specifier|transient
name|boolean
name|isKeyInitialized
decl_stmt|;
comment|/** Kryo ctor. */
specifier|protected
name|VectorReduceSinkEmptyKeyOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorReduceSinkEmptyKeyOperator
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
name|VectorReduceSinkEmptyKeyOperator
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
name|LOG
operator|.
name|info
argument_list|(
literal|"VectorReduceSinkEmptyKeyOperator constructor vectorReduceSinkInfo "
operator|+
name|vectorReduceSinkInfo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|initializeOp
parameter_list|(
name|Configuration
name|hconf
parameter_list|)
throws|throws
name|HiveException
block|{
name|super
operator|.
name|initializeOp
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
name|isKeyInitialized
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|(
name|Object
name|row
parameter_list|,
name|int
name|tag
parameter_list|)
throws|throws
name|HiveException
block|{
try|try
block|{
name|VectorizedRowBatch
name|batch
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|row
decl_stmt|;
name|batchCounter
operator|++
expr_stmt|;
if|if
condition|(
name|batch
operator|.
name|size
operator|==
literal|0
condition|)
block|{
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
name|CLASS_NAME
operator|+
literal|" batch #"
operator|+
name|batchCounter
operator|+
literal|" empty"
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
if|if
condition|(
operator|!
name|isKeyInitialized
condition|)
block|{
name|isKeyInitialized
operator|=
literal|true
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|isEmptyKey
argument_list|)
expr_stmt|;
name|initializeEmptyKey
argument_list|(
name|tag
argument_list|)
expr_stmt|;
block|}
comment|// Perform any value expressions.  Results will go into scratch columns.
if|if
condition|(
name|reduceSinkValueExpressions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|VectorExpression
name|ve
range|:
name|reduceSinkValueExpressions
control|)
block|{
name|ve
operator|.
name|evaluate
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
block|}
specifier|final
name|int
name|size
init|=
name|batch
operator|.
name|size
decl_stmt|;
if|if
condition|(
operator|!
name|isEmptyValue
condition|)
block|{
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
name|int
index|[]
name|selected
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
name|size
condition|;
name|logical
operator|++
control|)
block|{
specifier|final
name|int
name|batchIndex
init|=
name|selected
index|[
name|logical
index|]
decl_stmt|;
name|valueLazyBinarySerializeWrite
operator|.
name|reset
argument_list|()
expr_stmt|;
name|valueVectorSerializeRow
operator|.
name|serializeWrite
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|)
expr_stmt|;
name|valueBytesWritable
operator|.
name|set
argument_list|(
name|valueOutput
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|valueOutput
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|collect
argument_list|(
name|keyWritable
argument_list|,
name|valueBytesWritable
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
name|size
condition|;
name|batchIndex
operator|++
control|)
block|{
name|valueLazyBinarySerializeWrite
operator|.
name|reset
argument_list|()
expr_stmt|;
name|valueVectorSerializeRow
operator|.
name|serializeWrite
argument_list|(
name|batch
argument_list|,
name|batchIndex
argument_list|)
expr_stmt|;
name|valueBytesWritable
operator|.
name|set
argument_list|(
name|valueOutput
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|valueOutput
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|collect
argument_list|(
name|keyWritable
argument_list|,
name|valueBytesWritable
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
comment|// Empty value, too.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|collect
argument_list|(
name|keyWritable
argument_list|,
name|valueBytesWritable
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

