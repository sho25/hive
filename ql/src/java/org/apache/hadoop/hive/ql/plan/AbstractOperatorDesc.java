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
name|plan
package|;
end_package

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
name|PTFUtils
import|;
end_import

begin_class
specifier|public
class|class
name|AbstractOperatorDesc
implements|implements
name|OperatorDesc
block|{
specifier|protected
name|boolean
name|vectorMode
init|=
literal|false
decl_stmt|;
specifier|protected
specifier|transient
name|Statistics
name|statistics
decl_stmt|;
specifier|protected
specifier|transient
name|OpTraits
name|opTraits
decl_stmt|;
specifier|protected
specifier|transient
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|opProps
decl_stmt|;
static|static
block|{
name|PTFUtils
operator|.
name|makeTransient
argument_list|(
name|AbstractOperatorDesc
operator|.
name|class
argument_list|,
literal|"opProps"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Explain
argument_list|(
name|skipHeader
operator|=
literal|true
argument_list|,
name|displayName
operator|=
literal|"Statistics"
argument_list|)
specifier|public
name|Statistics
name|getStatistics
parameter_list|()
block|{
return|return
name|statistics
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setStatistics
parameter_list|(
name|Statistics
name|statistics
parameter_list|)
block|{
name|this
operator|.
name|statistics
operator|=
name|statistics
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|clone
parameter_list|()
throws|throws
name|CloneNotSupportedException
block|{
throw|throw
operator|new
name|CloneNotSupportedException
argument_list|(
literal|"clone not supported"
argument_list|)
throw|;
block|}
specifier|public
name|void
name|setVectorMode
parameter_list|(
name|boolean
name|vm
parameter_list|)
block|{
name|this
operator|.
name|vectorMode
operator|=
name|vm
expr_stmt|;
block|}
specifier|public
name|OpTraits
name|getOpTraits
parameter_list|()
block|{
return|return
name|opTraits
return|;
block|}
specifier|public
name|void
name|setOpTraits
parameter_list|(
name|OpTraits
name|opTraits
parameter_list|)
block|{
name|this
operator|.
name|opTraits
operator|=
name|opTraits
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getOpProps
parameter_list|()
block|{
return|return
name|opProps
return|;
block|}
specifier|public
name|void
name|setOpProps
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
parameter_list|)
block|{
name|this
operator|.
name|opProps
operator|=
name|props
expr_stmt|;
block|}
block|}
end_class

end_unit

