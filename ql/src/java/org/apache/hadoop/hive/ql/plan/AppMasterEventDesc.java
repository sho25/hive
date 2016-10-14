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
name|IOException
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
name|DataOutputBuffer
import|;
end_import

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Application Master Event Operator"
argument_list|)
specifier|public
class|class
name|AppMasterEventDesc
extends|extends
name|AbstractOperatorDesc
block|{
specifier|private
name|TableDesc
name|table
decl_stmt|;
specifier|private
name|String
name|vertexName
decl_stmt|;
specifier|private
name|String
name|inputName
decl_stmt|;
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Target Vertex"
argument_list|)
specifier|public
name|String
name|getVertexName
parameter_list|()
block|{
return|return
name|vertexName
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Target Input"
argument_list|)
specifier|public
name|String
name|getInputName
parameter_list|()
block|{
return|return
name|inputName
return|;
block|}
specifier|public
name|void
name|setInputName
parameter_list|(
name|String
name|inputName
parameter_list|)
block|{
name|this
operator|.
name|inputName
operator|=
name|inputName
expr_stmt|;
block|}
specifier|public
name|void
name|setVertexName
parameter_list|(
name|String
name|vertexName
parameter_list|)
block|{
name|this
operator|.
name|vertexName
operator|=
name|vertexName
expr_stmt|;
block|}
specifier|public
name|TableDesc
name|getTable
parameter_list|()
block|{
return|return
name|table
return|;
block|}
specifier|public
name|void
name|setTable
parameter_list|(
name|TableDesc
name|table
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
block|}
specifier|public
name|void
name|writeEventHeader
parameter_list|(
name|DataOutputBuffer
name|buffer
parameter_list|)
throws|throws
name|IOException
block|{
comment|// nothing to add
block|}
block|}
end_class

end_unit

