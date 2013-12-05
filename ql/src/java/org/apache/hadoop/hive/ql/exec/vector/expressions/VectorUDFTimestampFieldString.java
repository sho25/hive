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
name|java
operator|.
name|text
operator|.
name|ParseException
import|;
end_import

begin_comment
comment|/**  * Abstract class to return various fields from a String.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VectorUDFTimestampFieldString
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
specifier|protected
name|int
name|colNum
decl_stmt|;
specifier|protected
name|int
name|outputColumn
decl_stmt|;
specifier|protected
specifier|final
name|int
name|fieldStart
decl_stmt|;
specifier|protected
specifier|final
name|int
name|fieldLength
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|patternMin
init|=
literal|"0000-00-00 00:00:00.000000000"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|patternMax
init|=
literal|"9999-19-99 29:59:59.999999999"
decl_stmt|;
specifier|public
name|VectorUDFTimestampFieldString
parameter_list|(
name|int
name|colNum
parameter_list|,
name|int
name|outputColumn
parameter_list|,
name|int
name|fieldStart
parameter_list|,
name|int
name|fieldLength
parameter_list|)
block|{
name|this
operator|.
name|colNum
operator|=
name|colNum
expr_stmt|;
name|this
operator|.
name|outputColumn
operator|=
name|outputColumn
expr_stmt|;
name|this
operator|.
name|fieldStart
operator|=
name|fieldStart
expr_stmt|;
name|this
operator|.
name|fieldLength
operator|=
name|fieldLength
expr_stmt|;
block|}
specifier|public
name|VectorUDFTimestampFieldString
parameter_list|()
block|{
name|fieldStart
operator|=
operator|-
literal|1
expr_stmt|;
name|fieldLength
operator|=
operator|-
literal|1
expr_stmt|;
block|}
specifier|private
name|long
name|getField
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|ParseException
block|{
comment|// Validate
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length
condition|;
name|i
operator|++
control|)
block|{
name|char
name|ch
init|=
operator|(
name|char
operator|)
name|bytes
index|[
name|start
operator|+
name|i
index|]
decl_stmt|;
if|if
condition|(
name|ch
argument_list|<
name|patternMin
operator|.
name|charAt
operator|(
name|i
operator|)
operator|||
name|ch
argument_list|>
name|patternMax
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"A timestamp string should match 'yyyy-MM-dd HH:mm:ss.fffffffff' pattern."
argument_list|,
name|i
argument_list|)
throw|;
block|}
block|}
return|return
name|doGetField
argument_list|(
name|bytes
argument_list|,
name|start
argument_list|,
name|length
argument_list|)
return|;
block|}
specifier|protected
name|long
name|doGetField
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|ParseException
block|{
name|int
name|field
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|length
operator|<
name|fieldLength
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"A timestamp string should be longer."
argument_list|,
literal|0
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
name|fieldStart
init|;
name|i
operator|<
name|fieldStart
operator|+
name|fieldLength
condition|;
name|i
operator|++
control|)
block|{
name|byte
name|ch
init|=
name|bytes
index|[
name|start
operator|+
name|i
index|]
decl_stmt|;
name|field
operator|=
literal|10
operator|*
name|field
operator|+
operator|(
name|ch
operator|-
literal|'0'
operator|)
expr_stmt|;
block|}
return|return
name|field
return|;
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
name|LongColumnVector
name|outV
init|=
operator|(
name|LongColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|outputColumn
index|]
decl_stmt|;
name|BytesColumnVector
name|inputCol
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|this
operator|.
name|colNum
index|]
decl_stmt|;
specifier|final
name|int
name|n
init|=
name|inputCol
operator|.
name|isRepeating
condition|?
literal|1
else|:
name|batch
operator|.
name|size
decl_stmt|;
name|int
index|[]
name|sel
init|=
name|batch
operator|.
name|selected
decl_stmt|;
if|if
condition|(
name|batch
operator|.
name|size
operator|==
literal|0
condition|)
block|{
comment|// n != batch.size when isRepeating
return|return;
block|}
comment|// true for all algebraic UDFs with no state
name|outV
operator|.
name|isRepeating
operator|=
name|inputCol
operator|.
name|isRepeating
expr_stmt|;
if|if
condition|(
name|inputCol
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
operator|<
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
try|try
block|{
name|outV
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|getField
argument_list|(
name|inputCol
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|inputCol
operator|.
name|start
index|[
name|i
index|]
argument_list|,
name|inputCol
operator|.
name|length
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|outV
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
name|outV
operator|.
name|noNulls
operator|=
literal|false
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
block|}
block|}
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
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|outV
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|getField
argument_list|(
name|inputCol
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|inputCol
operator|.
name|start
index|[
name|i
index|]
argument_list|,
name|inputCol
operator|.
name|length
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|outV
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
name|outV
operator|.
name|noNulls
operator|=
literal|false
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
block|}
block|}
block|}
block|}
else|else
block|{
comment|// Handle case with nulls. Don't do function if the value is null, to save time,
comment|// because calling the function can be expensive.
name|outV
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
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
operator|<
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
name|inputCol
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|inputCol
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
try|try
block|{
name|outV
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|getField
argument_list|(
name|inputCol
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|inputCol
operator|.
name|start
index|[
name|i
index|]
argument_list|,
name|inputCol
operator|.
name|length
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
name|outV
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
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
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|outV
operator|.
name|isNull
index|[
name|i
index|]
operator|=
name|inputCol
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
if|if
condition|(
operator|!
name|inputCol
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
try|try
block|{
name|outV
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|getField
argument_list|(
name|inputCol
operator|.
name|vector
index|[
name|i
index|]
argument_list|,
name|inputCol
operator|.
name|start
index|[
name|i
index|]
argument_list|,
name|inputCol
operator|.
name|length
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
name|outV
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
block|}
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
name|this
operator|.
name|outputColumn
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getOutputType
parameter_list|()
block|{
return|return
literal|"long"
return|;
block|}
specifier|public
name|int
name|getColNum
parameter_list|()
block|{
return|return
name|colNum
return|;
block|}
specifier|public
name|void
name|setColNum
parameter_list|(
name|int
name|colNum
parameter_list|)
block|{
name|this
operator|.
name|colNum
operator|=
name|colNum
expr_stmt|;
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
name|STRING
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

