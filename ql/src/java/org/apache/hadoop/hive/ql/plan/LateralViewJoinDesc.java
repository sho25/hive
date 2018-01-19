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
name|util
operator|.
name|ArrayList
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

begin_comment
comment|/**  * LateralViewJoinDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Lateral View Join Operator"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|,
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
class|class
name|LateralViewJoinDesc
extends|extends
name|AbstractOperatorDesc
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
name|numSelColumns
decl_stmt|;
specifier|private
name|ArrayList
argument_list|<
name|String
argument_list|>
name|outputInternalColNames
decl_stmt|;
specifier|public
name|LateralViewJoinDesc
parameter_list|()
block|{   }
specifier|public
name|LateralViewJoinDesc
parameter_list|(
name|int
name|numSelColumns
parameter_list|,
name|ArrayList
argument_list|<
name|String
argument_list|>
name|outputInternalColNames
parameter_list|)
block|{
name|this
operator|.
name|numSelColumns
operator|=
name|numSelColumns
expr_stmt|;
name|this
operator|.
name|outputInternalColNames
operator|=
name|outputInternalColNames
expr_stmt|;
block|}
specifier|public
name|void
name|setOutputInternalColNames
parameter_list|(
name|ArrayList
argument_list|<
name|String
argument_list|>
name|outputInternalColNames
parameter_list|)
block|{
name|this
operator|.
name|outputInternalColNames
operator|=
name|outputInternalColNames
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"outputColumnNames"
argument_list|)
specifier|public
name|ArrayList
argument_list|<
name|String
argument_list|>
name|getOutputInternalColNames
parameter_list|()
block|{
return|return
name|outputInternalColNames
return|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Output"
argument_list|,
name|explainLevels
operator|=
block|{
name|Level
operator|.
name|USER
block|}
argument_list|)
specifier|public
name|ArrayList
argument_list|<
name|String
argument_list|>
name|getUserLevelExplainOutputInternalColNames
parameter_list|()
block|{
return|return
name|outputInternalColNames
return|;
block|}
specifier|public
name|int
name|getNumSelColumns
parameter_list|()
block|{
return|return
name|numSelColumns
return|;
block|}
specifier|public
name|void
name|setNumSelColumns
parameter_list|(
name|int
name|numSelColumns
parameter_list|)
block|{
name|this
operator|.
name|numSelColumns
operator|=
name|numSelColumns
expr_stmt|;
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
name|LateralViewJoinDesc
name|otherDesc
init|=
operator|(
name|LateralViewJoinDesc
operator|)
name|other
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|getOutputInternalColNames
argument_list|()
argument_list|,
name|otherDesc
operator|.
name|getOutputInternalColNames
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

