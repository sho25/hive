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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
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
name|Explain
operator|.
name|Level
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
name|Explain
operator|.
name|Vectorization
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
specifier|public
class|class
name|AppMasterEventOperatorExplainVectorization
extends|extends
name|OperatorExplainVectorization
block|{
specifier|private
specifier|final
name|AppMasterEventDesc
name|appMasterEventDesc
decl_stmt|;
specifier|private
specifier|final
name|VectorAppMasterEventDesc
name|vectorAppMasterEventDesc
decl_stmt|;
specifier|public
name|AppMasterEventOperatorExplainVectorization
parameter_list|(
name|AppMasterEventDesc
name|appMasterEventDesc
parameter_list|,
name|VectorAppMasterEventDesc
name|vectorAppMasterEventDesc
parameter_list|)
block|{
comment|// Native vectorization supported.
name|super
argument_list|(
name|vectorAppMasterEventDesc
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|appMasterEventDesc
operator|=
name|appMasterEventDesc
expr_stmt|;
name|this
operator|.
name|vectorAppMasterEventDesc
operator|=
name|vectorAppMasterEventDesc
expr_stmt|;
block|}
block|}
annotation|@
name|Explain
argument_list|(
name|vectorization
operator|=
name|Vectorization
operator|.
name|OPERATOR
argument_list|,
name|displayName
operator|=
literal|"App Master Event Vectorization"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|DEFAULT
block|,
name|Level
operator|.
name|EXTENDED
block|}
argument_list|)
specifier|public
name|AppMasterEventOperatorExplainVectorization
name|getAppMasterEventVectorization
parameter_list|()
block|{
name|VectorAppMasterEventDesc
name|vectorAppMasterEventDesc
init|=
operator|(
name|VectorAppMasterEventDesc
operator|)
name|getVectorDesc
argument_list|()
decl_stmt|;
if|if
condition|(
name|vectorAppMasterEventDesc
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|AppMasterEventOperatorExplainVectorization
argument_list|(
name|this
argument_list|,
name|vectorAppMasterEventDesc
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSame
parameter_list|(
name|OperatorDesc
name|other
parameter_list|)
block|{
if|if
condition|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|other
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|AppMasterEventDesc
name|otherDesc
init|=
operator|(
name|AppMasterEventDesc
operator|)
name|other
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|getInputName
argument_list|()
argument_list|,
name|otherDesc
operator|.
name|getInputName
argument_list|()
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|getVertexName
argument_list|()
argument_list|,
name|otherDesc
operator|.
name|getVertexName
argument_list|()
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|getTable
argument_list|()
argument_list|,
name|otherDesc
operator|.
name|getTable
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

