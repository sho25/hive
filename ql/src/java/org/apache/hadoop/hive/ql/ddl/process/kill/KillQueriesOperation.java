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
name|ddl
operator|.
name|process
operator|.
name|kill
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
name|ddl
operator|.
name|DDLOperation
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
name|ddl
operator|.
name|DDLOperationContext
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
name|metadata
operator|.
name|HiveException
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

begin_comment
comment|/**  * Operation process of killing queries.  */
end_comment

begin_class
specifier|public
class|class
name|KillQueriesOperation
extends|extends
name|DDLOperation
argument_list|<
name|KillQueriesDesc
argument_list|>
block|{
specifier|public
name|KillQueriesOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|KillQueriesDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|desc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|HiveException
block|{
name|SessionState
name|sessionState
init|=
name|SessionState
operator|.
name|get
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|queryId
range|:
name|desc
operator|.
name|getQueryIds
argument_list|()
control|)
block|{
name|sessionState
operator|.
name|getKillQuery
argument_list|()
operator|.
name|killQuery
argument_list|(
name|queryId
argument_list|,
literal|"User invoked KILL QUERY"
argument_list|,
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"kill query called ({})"
argument_list|,
name|desc
operator|.
name|getQueryIds
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
block|}
end_class

end_unit

