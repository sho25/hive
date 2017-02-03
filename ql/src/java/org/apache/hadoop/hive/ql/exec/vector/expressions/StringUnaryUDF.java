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
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * Expression for vectorized evaluation of unary UDFs on strings.  * An object of {@link IUDFUnaryString} is applied to every element of  * the vector.  */
end_comment

begin_class
specifier|public
class|class
name|StringUnaryUDF
extends|extends
name|VectorExpression
block|{
specifier|public
interface|interface
name|IUDFUnaryString
block|{
name|Text
name|evaluate
parameter_list|(
name|Text
name|s
parameter_list|)
function_decl|;
block|}
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
name|colNum
decl_stmt|;
specifier|private
name|int
name|outputColumn
decl_stmt|;
specifier|private
name|IUDFUnaryString
name|func
decl_stmt|;
specifier|private
specifier|transient
specifier|final
name|Text
name|s
decl_stmt|;
name|StringUnaryUDF
parameter_list|(
name|int
name|colNum
parameter_list|,
name|int
name|outputColumn
parameter_list|,
name|IUDFUnaryString
name|func
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
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
name|func
operator|=
name|func
expr_stmt|;
block|}
specifier|public
name|StringUnaryUDF
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|s
operator|=
operator|new
name|Text
argument_list|()
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
name|inputColVector
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|colNum
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
name|byte
index|[]
index|[]
name|vector
init|=
name|inputColVector
operator|.
name|vector
decl_stmt|;
name|int
index|[]
name|start
init|=
name|inputColVector
operator|.
name|start
decl_stmt|;
name|int
index|[]
name|length
init|=
name|inputColVector
operator|.
name|length
decl_stmt|;
name|BytesColumnVector
name|outV
init|=
operator|(
name|BytesColumnVector
operator|)
name|batch
operator|.
name|cols
index|[
name|outputColumn
index|]
decl_stmt|;
name|outV
operator|.
name|initBuffer
argument_list|()
expr_stmt|;
name|Text
name|t
decl_stmt|;
if|if
condition|(
name|n
operator|==
literal|0
condition|)
block|{
comment|//Nothing to do
return|return;
block|}
comment|// Design Note: In the future, if this function can be implemented
comment|// directly to translate input to output without creating new
comment|// objects, performance can probably be improved significantly.
comment|// It's implemented in the simplest way now, just calling the
comment|// existing built-in function.
if|if
condition|(
name|inputColVector
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
name|inputColVector
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
name|s
operator|.
name|set
argument_list|(
name|vector
index|[
literal|0
index|]
argument_list|,
name|start
index|[
literal|0
index|]
argument_list|,
name|length
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|t
operator|=
name|func
operator|.
name|evaluate
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|setString
argument_list|(
name|outV
argument_list|,
literal|0
argument_list|,
name|t
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
comment|/* Fill output isNull with false for selected elements since there is a chance we'll            * convert to noNulls == false in setString();            */
name|outV
operator|.
name|isNull
index|[
name|i
index|]
operator|=
literal|false
expr_stmt|;
name|s
operator|.
name|set
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|,
name|start
index|[
name|i
index|]
argument_list|,
name|length
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|t
operator|=
name|func
operator|.
name|evaluate
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|setString
argument_list|(
name|outV
argument_list|,
name|i
argument_list|,
name|t
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
comment|// Set all elements to not null. The setString call can override this.
name|Arrays
operator|.
name|fill
argument_list|(
name|outV
operator|.
name|isNull
argument_list|,
literal|0
argument_list|,
name|n
argument_list|,
literal|false
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
name|s
operator|.
name|set
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|,
name|start
index|[
name|i
index|]
argument_list|,
name|length
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|t
operator|=
name|func
operator|.
name|evaluate
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|setString
argument_list|(
name|outV
argument_list|,
name|i
argument_list|,
name|t
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
name|inputColVector
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
name|inputColVector
operator|.
name|isNull
index|[
literal|0
index|]
expr_stmt|;
comment|// setString can override this
if|if
condition|(
operator|!
name|inputColVector
operator|.
name|isNull
index|[
literal|0
index|]
condition|)
block|{
name|s
operator|.
name|set
argument_list|(
name|vector
index|[
literal|0
index|]
argument_list|,
name|start
index|[
literal|0
index|]
argument_list|,
name|length
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|t
operator|=
name|func
operator|.
name|evaluate
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|setString
argument_list|(
name|outV
argument_list|,
literal|0
argument_list|,
name|t
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
name|inputColVector
operator|.
name|isNull
index|[
name|i
index|]
expr_stmt|;
comment|// setString can override this
if|if
condition|(
operator|!
name|inputColVector
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|s
operator|.
name|set
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|,
name|start
index|[
name|i
index|]
argument_list|,
name|length
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|t
operator|=
name|func
operator|.
name|evaluate
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|setString
argument_list|(
name|outV
argument_list|,
name|i
argument_list|,
name|t
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
comment|// setString can override this null propagation
name|System
operator|.
name|arraycopy
argument_list|(
name|inputColVector
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
name|inputColVector
operator|.
name|isNull
index|[
name|i
index|]
condition|)
block|{
name|s
operator|.
name|set
argument_list|(
name|vector
index|[
name|i
index|]
argument_list|,
name|start
index|[
name|i
index|]
argument_list|,
name|length
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|t
operator|=
name|func
operator|.
name|evaluate
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|setString
argument_list|(
name|outV
argument_list|,
name|i
argument_list|,
name|t
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
comment|/* Set the output string entry i to the contents of Text object t.    * If t is a null object reference, record that the value is a SQL NULL.    */
specifier|private
specifier|static
name|void
name|setString
parameter_list|(
name|BytesColumnVector
name|outV
parameter_list|,
name|int
name|i
parameter_list|,
name|Text
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|==
literal|null
condition|)
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
return|return;
block|}
name|outV
operator|.
name|setVal
argument_list|(
name|i
argument_list|,
name|t
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|t
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
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
annotation|@
name|Override
specifier|public
name|String
name|getOutputType
parameter_list|()
block|{
return|return
literal|"String"
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
name|IUDFUnaryString
name|getFunc
parameter_list|()
block|{
return|return
name|func
return|;
block|}
specifier|public
name|void
name|setFunc
parameter_list|(
name|IUDFUnaryString
name|func
parameter_list|)
block|{
name|this
operator|.
name|func
operator|=
name|func
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
name|String
name|vectorExpressionParameters
parameter_list|()
block|{
return|return
literal|"col "
operator|+
name|colNum
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

