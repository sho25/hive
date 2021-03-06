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
name|FilterOperator
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
name|ConstantVectorExpression
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
name|ExprNodeDesc
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
name|FilterDesc
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
name|ql
operator|.
name|plan
operator|.
name|VectorFilterDesc
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
comment|/**  * Filter operator implementation.  **/
end_comment

begin_class
specifier|public
class|class
name|VectorFilterOperator
extends|extends
name|FilterOperator
implements|implements
name|VectorizationOperator
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
name|VectorizationContext
name|vContext
decl_stmt|;
specifier|private
name|VectorFilterDesc
name|vectorDesc
decl_stmt|;
specifier|private
name|VectorExpression
name|predicateExpression
init|=
literal|null
decl_stmt|;
comment|// Temporary selected vector
specifier|private
specifier|transient
name|int
index|[]
name|temporarySelected
decl_stmt|;
comment|// filterMode is 1 if condition is always true, -1 if always false
comment|// and 0 if condition needs to be computed.
specifier|transient
specifier|private
name|int
name|filterMode
init|=
literal|0
decl_stmt|;
specifier|public
name|VectorFilterOperator
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
name|this
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
operator|(
name|FilterDesc
operator|)
name|conf
expr_stmt|;
name|this
operator|.
name|vContext
operator|=
name|vContext
expr_stmt|;
name|this
operator|.
name|vectorDesc
operator|=
operator|(
name|VectorFilterDesc
operator|)
name|vectorDesc
expr_stmt|;
name|predicateExpression
operator|=
name|this
operator|.
name|vectorDesc
operator|.
name|getPredicateExpression
argument_list|()
expr_stmt|;
block|}
comment|/** Kryo ctor. */
annotation|@
name|VisibleForTesting
specifier|public
name|VectorFilterOperator
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|VectorFilterOperator
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
annotation|@
name|Override
specifier|public
name|VectorizationContext
name|getInputVectorizationContext
parameter_list|()
block|{
return|return
name|vContext
return|;
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
name|VectorExpression
operator|.
name|doTransientInit
argument_list|(
name|predicateExpression
argument_list|,
name|hconf
argument_list|)
expr_stmt|;
try|try
block|{
name|heartbeatInterval
operator|=
name|HiveConf
operator|.
name|getIntVar
argument_list|(
name|hconf
argument_list|,
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVESENDHEARTBEAT
argument_list|)
expr_stmt|;
name|predicateExpression
operator|.
name|init
argument_list|(
name|hconf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
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
if|if
condition|(
name|predicateExpression
operator|instanceof
name|ConstantVectorExpression
condition|)
block|{
name|ConstantVectorExpression
name|cve
init|=
operator|(
name|ConstantVectorExpression
operator|)
name|this
operator|.
name|predicateExpression
decl_stmt|;
if|if
condition|(
name|cve
operator|.
name|getLongValue
argument_list|()
operator|==
literal|1
condition|)
block|{
name|filterMode
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
name|filterMode
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
name|temporarySelected
operator|=
operator|new
name|int
index|[
name|VectorizedRowBatch
operator|.
name|DEFAULT_SIZE
index|]
expr_stmt|;
block|}
specifier|public
name|void
name|setFilterCondition
parameter_list|(
name|VectorExpression
name|expr
parameter_list|)
block|{
name|this
operator|.
name|predicateExpression
operator|=
name|expr
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
name|VectorizedRowBatch
name|vrg
init|=
operator|(
name|VectorizedRowBatch
operator|)
name|row
decl_stmt|;
comment|//The selected vector represents selected rows.
comment|//Clone the selected vector
name|System
operator|.
name|arraycopy
argument_list|(
name|vrg
operator|.
name|selected
argument_list|,
literal|0
argument_list|,
name|temporarySelected
argument_list|,
literal|0
argument_list|,
name|vrg
operator|.
name|size
argument_list|)
expr_stmt|;
name|int
index|[]
name|selectedBackup
init|=
name|vrg
operator|.
name|selected
decl_stmt|;
name|vrg
operator|.
name|selected
operator|=
name|temporarySelected
expr_stmt|;
name|int
name|sizeBackup
init|=
name|vrg
operator|.
name|size
decl_stmt|;
name|boolean
name|selectedInUseBackup
init|=
name|vrg
operator|.
name|selectedInUse
decl_stmt|;
comment|//Evaluate the predicate expression
switch|switch
condition|(
name|filterMode
condition|)
block|{
case|case
literal|0
case|:
name|predicateExpression
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
break|break;
case|case
operator|-
literal|1
case|:
comment|// All will be filtered out
name|vrg
operator|.
name|size
operator|=
literal|0
expr_stmt|;
break|break;
case|case
literal|1
case|:
default|default:
comment|// All are selected, do nothing
block|}
if|if
condition|(
name|vrg
operator|.
name|size
operator|>
literal|0
condition|)
block|{
name|vectorForward
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
block|}
comment|// Restore the original selected vector
name|vrg
operator|.
name|selected
operator|=
name|selectedBackup
expr_stmt|;
name|vrg
operator|.
name|size
operator|=
name|sizeBackup
expr_stmt|;
name|vrg
operator|.
name|selectedInUse
operator|=
name|selectedInUseBackup
expr_stmt|;
block|}
specifier|static
specifier|public
name|String
name|getOperatorName
parameter_list|()
block|{
return|return
literal|"FIL"
return|;
block|}
specifier|public
name|VectorExpression
name|getPredicateExpression
parameter_list|()
block|{
return|return
name|predicateExpression
return|;
block|}
annotation|@
name|Override
specifier|public
name|VectorDesc
name|getVectorDesc
parameter_list|()
block|{
return|return
name|vectorDesc
return|;
block|}
block|}
end_class

end_unit

