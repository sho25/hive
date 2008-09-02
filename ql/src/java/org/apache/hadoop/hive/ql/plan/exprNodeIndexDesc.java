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
name|io
operator|.
name|Serializable
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
name|parse
operator|.
name|TypeInfo
import|;
end_import

begin_class
specifier|public
class|class
name|exprNodeIndexDesc
extends|extends
name|exprNodeDesc
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
name|exprNodeDesc
name|desc
decl_stmt|;
name|exprNodeDesc
name|index
decl_stmt|;
specifier|public
name|exprNodeIndexDesc
parameter_list|()
block|{}
specifier|public
name|exprNodeIndexDesc
parameter_list|(
name|TypeInfo
name|typeInfo
parameter_list|,
name|exprNodeDesc
name|desc
parameter_list|,
name|exprNodeDesc
name|index
parameter_list|)
block|{
name|super
argument_list|(
name|typeInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|desc
operator|=
name|desc
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
block|}
specifier|public
name|exprNodeDesc
name|getDesc
parameter_list|()
block|{
return|return
name|this
operator|.
name|desc
return|;
block|}
specifier|public
name|void
name|setDesc
parameter_list|(
name|exprNodeDesc
name|desc
parameter_list|)
block|{
name|this
operator|.
name|desc
operator|=
name|desc
expr_stmt|;
block|}
specifier|public
name|exprNodeDesc
name|getIndex
parameter_list|()
block|{
return|return
name|this
operator|.
name|index
return|;
block|}
specifier|public
name|void
name|setIndex
parameter_list|(
name|exprNodeDesc
name|index
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|this
operator|.
name|desc
operator|.
name|toString
argument_list|()
operator|+
literal|"["
operator|+
name|this
operator|.
name|index
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

