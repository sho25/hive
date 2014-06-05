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
name|util
operator|.
name|ArrayList
import|;
end_import

begin_comment
comment|/**  * RowSchema Implementation.  */
end_comment

begin_class
specifier|public
class|class
name|RowSchema
implements|implements
name|Serializable
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
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|signature
init|=
operator|new
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|RowSchema
parameter_list|()
block|{   }
specifier|public
name|RowSchema
parameter_list|(
name|RowSchema
name|that
parameter_list|)
block|{
name|this
operator|.
name|signature
operator|=
operator|(
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
operator|)
name|that
operator|.
name|signature
operator|.
name|clone
argument_list|()
expr_stmt|;
block|}
specifier|public
name|RowSchema
parameter_list|(
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|signature
parameter_list|)
block|{
name|this
operator|.
name|signature
operator|=
name|signature
expr_stmt|;
block|}
specifier|public
name|void
name|setSignature
parameter_list|(
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|signature
parameter_list|)
block|{
name|this
operator|.
name|signature
operator|=
name|signature
expr_stmt|;
block|}
specifier|public
name|ArrayList
argument_list|<
name|ColumnInfo
argument_list|>
name|getSignature
parameter_list|()
block|{
return|return
name|signature
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
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'('
argument_list|)
expr_stmt|;
for|for
control|(
name|ColumnInfo
name|col
range|:
name|signature
control|)
block|{
if|if
condition|(
name|sb
operator|.
name|length
argument_list|()
operator|>
literal|1
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
name|col
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
expr_stmt|;
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

