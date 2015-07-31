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

begin_comment
comment|/**  * Map Join operator Descriptor implementation.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Spark HashTable Sink Operator"
argument_list|)
specifier|public
class|class
name|SparkHashTableSinkDesc
extends|extends
name|HashTableSinkDesc
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
comment|// The position of this table
specifier|private
name|byte
name|tag
decl_stmt|;
specifier|public
name|SparkHashTableSinkDesc
parameter_list|()
block|{   }
specifier|public
name|SparkHashTableSinkDesc
parameter_list|(
name|MapJoinDesc
name|clone
parameter_list|)
block|{
name|super
argument_list|(
name|clone
argument_list|)
expr_stmt|;
block|}
specifier|public
name|byte
name|getTag
parameter_list|()
block|{
return|return
name|tag
return|;
block|}
specifier|public
name|void
name|setTag
parameter_list|(
name|byte
name|tag
parameter_list|)
block|{
name|this
operator|.
name|tag
operator|=
name|tag
expr_stmt|;
block|}
block|}
end_class

end_unit

