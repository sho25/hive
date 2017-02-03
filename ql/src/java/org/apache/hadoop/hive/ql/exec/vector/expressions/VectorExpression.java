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
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|collect
operator|.
name|ImmutableMap
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

begin_comment
comment|/**  * Base class for expressions.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|VectorExpression
implements|implements
name|Serializable
block|{
specifier|public
enum|enum
name|Type
block|{
name|STRING
block|,
name|CHAR
block|,
name|VARCHAR
block|,
name|TIMESTAMP
block|,
name|DATE
block|,
name|LONG
block|,
name|DOUBLE
block|,
name|DECIMAL
block|,
name|INTERVAL_YEAR_MONTH
block|,
name|INTERVAL_DAY_TIME
block|,
name|BINARY
block|,
name|OTHER
block|;
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Type
argument_list|>
name|types
init|=
name|ImmutableMap
operator|.
expr|<
name|String
block|,
name|Type
decl|>
name|builder
argument_list|()
decl|.
name|put
argument_list|(
literal|"string"
argument_list|,
name|STRING
argument_list|)
decl|.
name|put
argument_list|(
literal|"char"
argument_list|,
name|CHAR
argument_list|)
decl|.
name|put
argument_list|(
literal|"varchar"
argument_list|,
name|VARCHAR
argument_list|)
decl|.
name|put
argument_list|(
literal|"timestamp"
argument_list|,
name|TIMESTAMP
argument_list|)
decl|.
name|put
argument_list|(
literal|"date"
argument_list|,
name|DATE
argument_list|)
decl|.
name|put
argument_list|(
literal|"long"
argument_list|,
name|LONG
argument_list|)
decl|.
name|put
argument_list|(
literal|"double"
argument_list|,
name|DOUBLE
argument_list|)
decl|.
name|put
argument_list|(
literal|"decimal"
argument_list|,
name|DECIMAL
argument_list|)
decl|.
name|put
argument_list|(
literal|"interval_year_month"
argument_list|,
name|INTERVAL_YEAR_MONTH
argument_list|)
decl|.
name|put
argument_list|(
literal|"interval_day_time"
argument_list|,
name|INTERVAL_DAY_TIME
argument_list|)
decl|.
name|put
argument_list|(
literal|"binary"
argument_list|,
name|BINARY
argument_list|)
decl|.
name|build
argument_list|()
block|;
specifier|public
specifier|static
name|Type
name|getValue
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|String
name|nameLower
init|=
name|name
operator|.
name|toLowerCase
argument_list|()
decl_stmt|;
if|if
condition|(
name|types
operator|.
name|containsKey
argument_list|(
name|nameLower
argument_list|)
condition|)
block|{
return|return
name|types
operator|.
name|get
argument_list|(
name|nameLower
argument_list|)
return|;
block|}
return|return
name|OTHER
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|/**    * Child expressions are evaluated post order.    */
specifier|protected
name|VectorExpression
index|[]
name|childExpressions
init|=
literal|null
decl_stmt|;
comment|/**    * More detailed input types, such as date and timestamp.    */
specifier|protected
name|Type
index|[]
name|inputTypes
decl_stmt|;
comment|/**    * Output type of the expression.    */
specifier|protected
name|String
name|outputType
decl_stmt|;
comment|/**    * This is the primary method to implement expression logic.    * @param batch    */
specifier|public
specifier|abstract
name|void
name|evaluate
parameter_list|(
name|VectorizedRowBatch
name|batch
parameter_list|)
function_decl|;
specifier|public
name|void
name|init
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|childExpressions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|VectorExpression
name|child
range|:
name|childExpressions
control|)
block|{
name|child
operator|.
name|init
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Returns the index of the output column in the array    * of column vectors. If not applicable, -1 is returned.    * @return Index of the output column    */
specifier|public
specifier|abstract
name|int
name|getOutputColumn
parameter_list|()
function_decl|;
comment|/**    * Returns type of the output column.    */
specifier|public
name|String
name|getOutputType
parameter_list|()
block|{
return|return
name|outputType
return|;
block|}
comment|/**    * Set type of the output column.    */
specifier|public
name|void
name|setOutputType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|outputType
operator|=
name|type
expr_stmt|;
block|}
comment|/**    * Initialize the child expressions.    */
specifier|public
name|void
name|setChildExpressions
parameter_list|(
name|VectorExpression
index|[]
name|ve
parameter_list|)
block|{
name|childExpressions
operator|=
name|ve
expr_stmt|;
block|}
specifier|public
name|VectorExpression
index|[]
name|getChildExpressions
parameter_list|()
block|{
return|return
name|childExpressions
return|;
block|}
specifier|public
specifier|abstract
name|VectorExpressionDescriptor
operator|.
name|Descriptor
name|getDescriptor
parameter_list|()
function_decl|;
comment|/**    * Evaluate the child expressions on the given input batch.    * @param vrg {@link VectorizedRowBatch}    */
specifier|final
specifier|protected
name|void
name|evaluateChildren
parameter_list|(
name|VectorizedRowBatch
name|vrg
parameter_list|)
block|{
if|if
condition|(
name|childExpressions
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|VectorExpression
name|ve
range|:
name|childExpressions
control|)
block|{
name|ve
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Set more detailed types to distinguish certain types that is represented in same    * {@link org.apache.hadoop.hive.ql.exec.vector.VectorExpressionDescriptor.ArgumentType}s. For example, date and    * timestamp will be in {@link org.apache.hadoop.hive.ql.exec.vector.LongColumnVector} but they need to be    * distinguished.    * @param inputTypes    */
specifier|public
name|void
name|setInputTypes
parameter_list|(
name|Type
modifier|...
name|inputTypes
parameter_list|)
block|{
name|this
operator|.
name|inputTypes
operator|=
name|inputTypes
expr_stmt|;
block|}
specifier|public
name|Type
index|[]
name|getInputTypes
parameter_list|()
block|{
return|return
name|inputTypes
return|;
block|}
specifier|public
name|String
name|vectorExpressionParameters
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|b
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|instanceof
name|IdentityExpression
condition|)
block|{
name|b
operator|.
name|append
argument_list|(
name|vectorExpressionParameters
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|b
operator|.
name|append
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|vectorExpressionParameters
init|=
name|vectorExpressionParameters
argument_list|()
decl_stmt|;
if|if
condition|(
name|vectorExpressionParameters
operator|!=
literal|null
condition|)
block|{
name|b
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
expr_stmt|;
name|b
operator|.
name|append
argument_list|(
name|vectorExpressionParameters
argument_list|)
expr_stmt|;
name|b
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|childExpressions
operator|!=
literal|null
condition|)
block|{
name|b
operator|.
name|append
argument_list|(
literal|"(children: "
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
operator|<
name|childExpressions
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|b
operator|.
name|append
argument_list|(
name|childExpressions
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|<
name|childExpressions
operator|.
name|length
operator|-
literal|1
condition|)
block|{
name|b
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
block|}
name|b
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
name|b
operator|.
name|append
argument_list|(
literal|" -> "
argument_list|)
expr_stmt|;
name|int
name|outputColumn
init|=
name|getOutputColumn
argument_list|()
decl_stmt|;
if|if
condition|(
name|outputColumn
operator|!=
operator|-
literal|1
condition|)
block|{
name|b
operator|.
name|append
argument_list|(
name|outputColumn
argument_list|)
expr_stmt|;
name|b
operator|.
name|append
argument_list|(
literal|":"
argument_list|)
expr_stmt|;
block|}
name|b
operator|.
name|append
argument_list|(
name|getOutputType
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|b
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|String
name|displayUtf8Bytes
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
block|{
if|if
condition|(
name|bytes
operator|==
literal|null
condition|)
block|{
return|return
literal|"NULL"
return|;
block|}
else|else
block|{
return|return
operator|new
name|String
argument_list|(
name|bytes
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
name|String
name|displayArrayOfUtf8ByteArrays
parameter_list|(
name|byte
index|[]
index|[]
name|arrayOfByteArrays
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|boolean
name|isFirst
init|=
literal|true
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|bytes
range|:
name|arrayOfByteArrays
control|)
block|{
if|if
condition|(
name|isFirst
condition|)
block|{
name|isFirst
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|displayUtf8Bytes
argument_list|(
name|bytes
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

