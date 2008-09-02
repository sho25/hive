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
name|parse
package|;
end_package

begin_comment
comment|/**  * Join conditions Descriptor implementation.  *   */
end_comment

begin_class
specifier|public
class|class
name|joinCond
block|{
specifier|private
name|int
name|left
decl_stmt|;
specifier|private
name|int
name|right
decl_stmt|;
specifier|private
name|joinType
name|joinType
decl_stmt|;
specifier|public
name|joinCond
parameter_list|()
block|{  }
specifier|public
name|joinCond
parameter_list|(
name|int
name|left
parameter_list|,
name|int
name|right
parameter_list|,
name|joinType
name|joinType
parameter_list|)
block|{
name|this
operator|.
name|left
operator|=
name|left
expr_stmt|;
name|this
operator|.
name|right
operator|=
name|right
expr_stmt|;
name|this
operator|.
name|joinType
operator|=
name|joinType
expr_stmt|;
block|}
specifier|public
name|int
name|getLeft
parameter_list|()
block|{
return|return
name|left
return|;
block|}
specifier|public
name|void
name|setLeft
parameter_list|(
specifier|final
name|int
name|left
parameter_list|)
block|{
name|this
operator|.
name|left
operator|=
name|left
expr_stmt|;
block|}
specifier|public
name|int
name|getRight
parameter_list|()
block|{
return|return
name|right
return|;
block|}
specifier|public
name|void
name|setRight
parameter_list|(
specifier|final
name|int
name|right
parameter_list|)
block|{
name|this
operator|.
name|right
operator|=
name|right
expr_stmt|;
block|}
specifier|public
name|joinType
name|getJoinType
parameter_list|()
block|{
return|return
name|joinType
return|;
block|}
specifier|public
name|void
name|setJoinType
parameter_list|(
specifier|final
name|joinType
name|joinType
parameter_list|)
block|{
name|this
operator|.
name|joinType
operator|=
name|joinType
expr_stmt|;
block|}
block|}
end_class

end_unit

