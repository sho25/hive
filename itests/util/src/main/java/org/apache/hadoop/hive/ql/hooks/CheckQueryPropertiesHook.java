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
name|hooks
package|;
end_package

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
name|QueryProperties
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
name|session
operator|.
name|SessionState
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
name|session
operator|.
name|SessionState
operator|.
name|LogHelper
import|;
end_import

begin_comment
comment|/**  *  * CheckQueryPropertiesHook.  *  * This hook prints the values in the QueryProperties object contained in the QueryPlan  * in the HookContext passed to the hook.  */
end_comment

begin_class
specifier|public
class|class
name|CheckQueryPropertiesHook
implements|implements
name|ExecuteWithHookContext
block|{
specifier|public
name|void
name|run
parameter_list|(
name|HookContext
name|hookContext
parameter_list|)
block|{
name|LogHelper
name|console
init|=
name|SessionState
operator|.
name|getConsole
argument_list|()
decl_stmt|;
if|if
condition|(
name|console
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|QueryProperties
name|queryProps
init|=
name|hookContext
operator|.
name|getQueryPlan
argument_list|()
operator|.
name|getQueryProperties
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryProps
operator|!=
literal|null
condition|)
block|{
name|console
operator|.
name|printError
argument_list|(
literal|"Has Join: "
operator|+
name|queryProps
operator|.
name|hasJoin
argument_list|()
argument_list|)
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
literal|"Has Group By: "
operator|+
name|queryProps
operator|.
name|hasGroupBy
argument_list|()
argument_list|)
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
literal|"Has Sort By: "
operator|+
name|queryProps
operator|.
name|hasSortBy
argument_list|()
argument_list|)
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
literal|"Has Order By: "
operator|+
name|queryProps
operator|.
name|hasOrderBy
argument_list|()
argument_list|)
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
literal|"Has Group By After Join: "
operator|+
name|queryProps
operator|.
name|hasJoinFollowedByGroupBy
argument_list|()
argument_list|)
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
literal|"Uses Script: "
operator|+
name|queryProps
operator|.
name|usesScript
argument_list|()
argument_list|)
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
literal|"Has Distribute By: "
operator|+
name|queryProps
operator|.
name|hasDistributeBy
argument_list|()
argument_list|)
expr_stmt|;
name|console
operator|.
name|printError
argument_list|(
literal|"Has Cluster By: "
operator|+
name|queryProps
operator|.
name|hasClusterBy
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

