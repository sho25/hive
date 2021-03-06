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
name|optimizer
operator|.
name|calcite
operator|.
name|reloperators
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|SqlFunction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|SqlFunctionCategory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|SqlKind
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|type
operator|.
name|SqlOperandTypeChecker
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|type
operator|.
name|SqlOperandTypeInference
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|calcite
operator|.
name|sql
operator|.
name|type
operator|.
name|SqlReturnTypeInference
import|;
end_import

begin_class
specifier|public
class|class
name|HiveSqlFunction
extends|extends
name|SqlFunction
block|{
specifier|private
specifier|final
name|boolean
name|deterministic
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|runtimeConstant
decl_stmt|;
specifier|public
name|HiveSqlFunction
parameter_list|(
name|String
name|name
parameter_list|,
name|SqlKind
name|kind
parameter_list|,
name|SqlReturnTypeInference
name|returnTypeInference
parameter_list|,
name|SqlOperandTypeInference
name|operandTypeInference
parameter_list|,
name|SqlOperandTypeChecker
name|operandTypeChecker
parameter_list|,
name|SqlFunctionCategory
name|category
parameter_list|,
name|boolean
name|deterministic
parameter_list|,
name|boolean
name|runtimeConstant
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|kind
argument_list|,
name|returnTypeInference
argument_list|,
name|operandTypeInference
argument_list|,
name|operandTypeChecker
argument_list|,
name|category
argument_list|)
expr_stmt|;
name|this
operator|.
name|deterministic
operator|=
name|deterministic
expr_stmt|;
name|this
operator|.
name|runtimeConstant
operator|=
name|runtimeConstant
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isDeterministic
parameter_list|()
block|{
return|return
name|deterministic
return|;
block|}
comment|/**    * Whether it is safe to cache or materialize plans containing this operator.    * We do not rely on {@link SqlFunction#isDynamicFunction()} because it has    * different implications, e.g., a dynamic function will not be reduced in    * Calcite since plans may be cached in the context of prepared statements.    * In our case, we check whether a plan contains runtime constants before    * constant folding happens, hence we can let Calcite reduce these functions.    *    * @return true iff it is unsafe to cache or materialized query plans    * referencing this operator    */
specifier|public
name|boolean
name|isRuntimeConstant
parameter_list|()
block|{
return|return
name|runtimeConstant
return|;
block|}
block|}
end_class

end_unit

