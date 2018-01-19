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

begin_comment
comment|/**  * VectorSMBMapJoinDesc.  *  * Extra parameters beyond SMBMapJoinDesc just for the VectorSMBMapJoinOperator.  *  * We don't extend SMBMapJoinDesc because the base OperatorDesc doesn't support  * clone and adding it is a lot work for little gain.  */
end_comment

begin_class
specifier|public
class|class
name|VectorSMBJoinDesc
extends|extends
name|AbstractVectorDesc
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|public
name|VectorSMBJoinDesc
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|public
name|VectorSMBJoinDesc
name|clone
parameter_list|()
block|{
name|VectorSMBJoinDesc
name|clone
init|=
operator|new
name|VectorSMBJoinDesc
argument_list|()
decl_stmt|;
return|return
name|clone
return|;
block|}
block|}
end_class

end_unit

