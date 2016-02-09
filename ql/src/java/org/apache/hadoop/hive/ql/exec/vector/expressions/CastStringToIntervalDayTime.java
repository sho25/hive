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
name|exec
operator|.
name|vector
operator|.
name|expressions
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
name|common
operator|.
name|type
operator|.
name|HiveIntervalDayTime
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
name|BytesColumnVector
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
name|TimestampColumnVector
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
name|VectorExpressionDescriptor
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
name|serde
operator|.
name|serdeConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|DateUtils
import|;
end_import

begin_comment
comment|/**  * Casts a string vector to a interval day-time vector.  */
end_comment

begin_class
specifier|public
class|class
name|CastStringToIntervalDayTime
extends|extends
name|VectorExpression
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
name|int
name|inputColumn
decl_stmt|;
specifier|private
name|int
name|outputColumn
decl_stmt|;
specifier|public
name|CastStringToIntervalDayTime
parameter_list|()
block|{    }
specifier|public
name|CastStringToIntervalDayTime
parameter_list|(
name|int
name|inputColumn
parameter_list|,
name|int
name|outputColumn
parameter_list|)
block|{
name|this
operator|.
name|inputColumn
operator|=
name|inputColumn
expr_stmt|;
name|this
operator|.
name|outputColumn
operator|=
name|outputColumn
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|evaluate
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
block|{
if|if
condition|(
name|childExpressions
operator|!=
literal|null
condition|)
block|{
name|super
operator|.
name|evaluateChildren
argument_list|(
name|batch
argument_list|)
expr_stmt|;
block|}
name|BytesColumnVector
name|inV
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|inputColumn
index|]
decl_stmt|;
name|int
index|[]
name|sel
init|=
name|batch
operator|.
name|selected
decl_stmt|;
name|int
name|n
init|=
name|batch
operator|.
name|size
decl_stmt|;
name|TimestampColumnVector
name|outV
init|=
operator|(
name|TimestampColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|outputColumn
index|]
decl_stmt|;
if|if
condition|(
name|n
operator|==
literal|0
condition|)
block|{
comment|// Nothing to do
return|return;
block|}
if|if
condition|(
name|inV
operator|.
name|noNulls
condition|)
block|{
name|outV
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|inV
operator|.
name|isRepeating
condition|)
block|{
name|outV
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|evaluate
argument_list|(
name|outV
argument_list|,
name|inV
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|!=
name|n
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
name|evaluate
argument_list|(
name|outV
argument_list|,
name|inV
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|outV
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
name|evaluate
argument_list|(
name|outV
argument_list|,
name|inV
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
name|outV
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Handle case with nulls. Don't do function if the value is null,
comment|// because the data may be undefined for a null value.
name|outV
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|inV
operator|.
name|isRepeating
condition|)
block|{
name|outV
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|outV
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
name|inV
operator|.
name|isNull
index|[
literal|0
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|inV
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|evaluate
argument_list|(
name|outV
argument_list|,
name|inV
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|batch
operator|.
name|selectedInUse
condition|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|!=
name|n
condition|;
name|j
operator|++
control|)
block|{
name|int
name|i
init|=
name|sel
index|[
name|j
index|]
decl_stmt|;
name|outV
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inV
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|inV
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|evaluate
argument_list|(
name|outV
argument_list|,
name|inV
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
name|outV
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|inV
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|outV
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|n
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|!=
name|n
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|inV
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|evaluate
argument_list|(
name|outV
argument_list|,
name|inV
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
name|outV
operator|.
name|isRepeating
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|evaluate
parameter_list|(
name|TimestampColumnVector
name|outV
parameter_list|,
name|BytesColumnVector
name|inV
parameter_list|,
name|int
name|i
parameter_list|)
block|{
try|try
block|{
name|HiveIntervalDayTime
name|interval
init|=
name|HiveIntervalDayTime
operator|.
name|valueOf
argument_list|(
operator|new
name|String
argument_list|(
name|inV
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|inV
operator|.
name|start
index|[
name|i
index|]
argument_list|,
name|inV
operator|.
name|length
index|[
name|i
index|]
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
decl_stmt|;
name|outV
operator|.
name|setEpochSecondsAndSignedNanos
argument_list|(
name|i
argument_list|,
name|interval
operator|.
name|getTotalSeconds
argument_list|()
argument_list|,
name|interval
operator|.
name|getNanos
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|outV
operator|.
name|setNullValue
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|outV
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
name|outV
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getOutputColumn
parameter_list|()
block|{
return|return
name|outputColumn
return|;
block|}
specifier|public
name|void
name|setOutputColumn
parameter_list|(
name|int
name|outputColumn
parameter_list|)
block|{
name|this
operator|.
name|outputColumn
operator|=
name|outputColumn
expr_stmt|;
block|}
specifier|public
name|int
name|getInputColumn
parameter_list|()
block|{
return|return
name|inputColumn
return|;
block|}
specifier|public
name|void
name|setInputColumn
parameter_list|(
name|int
name|inputColumn
parameter_list|)
block|{
name|this
operator|.
name|inputColumn
operator|=
name|inputColumn
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getOutputType
parameter_list|()
block|{
return|return
name|serdeConstants
operator|.
name|INTERVAL_DAY_TIME_TYPE_NAME
return|;
block|}
annotation|@
name|Override
specifier|public
name|VectorExpressionDescriptor
operator|.
name|Descriptor
name|getDescriptor
parameter_list|()
block|{
name|VectorExpressionDescriptor
operator|.
name|Builder
name|b
init|=
operator|new
name|VectorExpressionDescriptor
operator|.
name|Builder
argument_list|()
decl_stmt|;
name|b
operator|.
name|setMode
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|Mode
operator|.
name|PROJECTION
argument_list|)
operator|.
name|setNumArguments
argument_list|(
literal|1
argument_list|)
operator|.
name|setArgumentTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|ArgumentType
operator|.
name|STRING_FAMILY
argument_list|)
operator|.
name|setInputExpressionTypes
argument_list|(
name|VectorExpressionDescriptor
operator|.
name|InputExpressionType
operator|.
name|COLUMN
argument_list|)
expr_stmt|;
return|return
name|b
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

