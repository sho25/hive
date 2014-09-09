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

begin_class
specifier|public
class|class
name|CastBooleanToVarCharViaLongToVarChar
extends|extends
name|CastBooleanToStringViaLongToString
implements|implements
name|TruncStringOutput
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
name|maxLength
decl_stmt|;
comment|// Must be manually set with setMaxLength.
specifier|public
name|CastBooleanToVarCharViaLongToVarChar
parameter_list|(
name|int
name|inputColumn
parameter_list|,
name|int
name|outputColumn
parameter_list|)
block|{
name|super
argument_list|(
name|inputColumn
argument_list|,
name|outputColumn
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CastBooleanToVarCharViaLongToVarChar
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|assign
parameter_list|(
name|BytesColumnVector
name|outV
parameter_list|,
name|int
name|i
parameter_list|,
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|StringExpr
operator|.
name|truncate
argument_list|(
name|outV
argument_list|,
name|i
argument_list|,
name|bytes
argument_list|,
literal|0
argument_list|,
name|length
argument_list|,
name|maxLength
argument_list|)
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
literal|"Char"
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getMaxLength
parameter_list|()
block|{
return|return
name|maxLength
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setMaxLength
parameter_list|(
name|int
name|maxLength
parameter_list|)
block|{
name|this
operator|.
name|maxLength
operator|=
name|maxLength
expr_stmt|;
block|}
block|}
end_class

end_unit

