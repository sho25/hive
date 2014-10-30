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
operator|.
name|context
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|ptest
operator|.
name|execution
operator|.
name|conf
operator|.
name|Host
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
name|ImmutableSet
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
name|Sets
import|;
end_import

begin_class
specifier|public
class|class
name|ExecutionContext
block|{
specifier|private
specifier|final
name|Set
argument_list|<
name|Host
argument_list|>
name|mHosts
decl_stmt|;
specifier|private
specifier|final
name|String
name|mLocalWorkingDirectory
decl_stmt|;
specifier|private
specifier|final
name|String
name|mPrivateKey
decl_stmt|;
specifier|private
specifier|final
name|ExecutionContextProvider
name|mExecutionContextProvider
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|Host
argument_list|>
name|mBadHosts
decl_stmt|;
specifier|public
name|ExecutionContext
parameter_list|(
name|ExecutionContextProvider
name|executionContextProvider
parameter_list|,
name|Set
argument_list|<
name|Host
argument_list|>
name|hosts
parameter_list|,
name|String
name|localWorkingDirectory
parameter_list|,
name|String
name|privateKey
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|mExecutionContextProvider
operator|=
name|executionContextProvider
expr_stmt|;
name|mHosts
operator|=
name|hosts
expr_stmt|;
name|mLocalWorkingDirectory
operator|=
name|localWorkingDirectory
expr_stmt|;
name|mPrivateKey
operator|=
name|privateKey
expr_stmt|;
name|mBadHosts
operator|=
name|Sets
operator|.
name|newHashSet
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|addBadHost
parameter_list|(
name|Host
name|host
parameter_list|)
block|{
name|mBadHosts
operator|.
name|add
argument_list|(
name|host
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|clearBadHosts
parameter_list|()
block|{
name|mBadHosts
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|addHost
parameter_list|(
name|Host
name|host
parameter_list|)
block|{
name|mHosts
operator|.
name|add
argument_list|(
name|host
argument_list|)
expr_stmt|;
block|}
name|boolean
name|removeHost
parameter_list|(
name|Host
name|host
parameter_list|)
block|{
return|return
name|mHosts
operator|.
name|remove
argument_list|(
name|host
argument_list|)
return|;
block|}
specifier|public
name|ImmutableSet
argument_list|<
name|Host
argument_list|>
name|getBadHosts
parameter_list|()
block|{
return|return
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
name|mBadHosts
argument_list|)
return|;
block|}
specifier|public
name|ImmutableSet
argument_list|<
name|Host
argument_list|>
name|getHosts
parameter_list|()
block|{
return|return
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
name|mHosts
argument_list|)
return|;
block|}
specifier|public
name|String
name|getLocalWorkingDirectory
parameter_list|()
block|{
return|return
name|mLocalWorkingDirectory
return|;
block|}
specifier|public
name|String
name|getPrivateKey
parameter_list|()
block|{
return|return
name|mPrivateKey
return|;
block|}
specifier|public
name|void
name|replaceBadHosts
parameter_list|()
throws|throws
name|CreateHostsFailedException
block|{
name|mExecutionContextProvider
operator|.
name|replaceBadHosts
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|terminate
parameter_list|()
block|{
name|mExecutionContextProvider
operator|.
name|terminate
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

