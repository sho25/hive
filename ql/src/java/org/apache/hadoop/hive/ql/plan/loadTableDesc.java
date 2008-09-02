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
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_class
specifier|public
class|class
name|loadTableDesc
extends|extends
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
name|loadDesc
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
name|boolean
name|replace
decl_stmt|;
comment|// TODO: the below seems like they should just be combined into partitionDesc
specifier|private
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
name|tableDesc
name|table
decl_stmt|;
specifier|private
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
decl_stmt|;
specifier|public
name|loadTableDesc
parameter_list|()
block|{ }
specifier|public
name|loadTableDesc
parameter_list|(
specifier|final
name|String
name|sourceDir
parameter_list|,
specifier|final
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
name|tableDesc
name|table
parameter_list|,
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|,
specifier|final
name|boolean
name|replace
parameter_list|)
block|{
name|super
argument_list|(
name|sourceDir
argument_list|)
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|partitionSpec
operator|=
name|partitionSpec
expr_stmt|;
name|this
operator|.
name|replace
operator|=
name|replace
expr_stmt|;
block|}
specifier|public
name|loadTableDesc
parameter_list|(
specifier|final
name|String
name|sourceDir
parameter_list|,
specifier|final
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
name|tableDesc
name|table
parameter_list|,
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|)
block|{
name|this
argument_list|(
name|sourceDir
argument_list|,
name|table
argument_list|,
name|partitionSpec
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|tableDesc
name|getTable
parameter_list|()
block|{
return|return
name|this
operator|.
name|table
return|;
block|}
specifier|public
name|void
name|setTable
parameter_list|(
specifier|final
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
name|tableDesc
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
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getPartitionSpec
parameter_list|()
block|{
return|return
name|this
operator|.
name|partitionSpec
return|;
block|}
specifier|public
name|void
name|setPartitionSpec
parameter_list|(
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partitionSpec
parameter_list|)
block|{
name|this
operator|.
name|partitionSpec
operator|=
name|partitionSpec
expr_stmt|;
block|}
specifier|public
name|boolean
name|getReplace
parameter_list|()
block|{
return|return
name|replace
return|;
block|}
specifier|public
name|void
name|setReplace
parameter_list|(
name|boolean
name|replace
parameter_list|)
block|{
name|this
operator|.
name|replace
operator|=
name|replace
expr_stmt|;
block|}
block|}
end_class

end_unit

