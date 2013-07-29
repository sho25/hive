begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|execution
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
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
specifier|public
class|class
name|MockLocalCommandFactory
extends|extends
name|LocalCommandFactory
block|{
specifier|protected
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|commands
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
specifier|private
name|LocalCommand
name|instance
decl_stmt|;
specifier|public
name|MockLocalCommandFactory
parameter_list|(
name|Logger
name|logger
parameter_list|)
block|{
name|super
argument_list|(
name|logger
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setInstance
parameter_list|(
name|LocalCommand
name|instance
parameter_list|)
block|{
name|this
operator|.
name|instance
operator|=
name|instance
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getCommands
parameter_list|()
block|{
return|return
name|commands
return|;
block|}
annotation|@
name|Override
specifier|public
name|LocalCommand
name|create
parameter_list|(
name|LocalCommand
operator|.
name|CollectPolicy
name|policy
parameter_list|,
name|String
name|command
parameter_list|)
throws|throws
name|IOException
block|{
name|commands
operator|.
name|add
argument_list|(
name|command
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
name|instance
return|;
block|}
block|}
end_class

end_unit

