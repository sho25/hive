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
name|Serializable
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
comment|/**  * LockDatabaseDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Lock Database"
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
name|LockDatabaseDesc
extends|extends
name|DDLDesc
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
name|String
name|databaseName
decl_stmt|;
specifier|private
name|String
name|mode
decl_stmt|;
specifier|private
name|String
name|queryId
decl_stmt|;
specifier|private
name|String
name|queryStr
decl_stmt|;
specifier|public
name|LockDatabaseDesc
parameter_list|()
block|{   }
specifier|public
name|LockDatabaseDesc
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|String
name|mode
parameter_list|,
name|String
name|queryId
parameter_list|)
block|{
name|this
operator|.
name|databaseName
operator|=
name|databaseName
expr_stmt|;
name|this
operator|.
name|mode
operator|=
name|mode
expr_stmt|;
name|this
operator|.
name|queryId
operator|=
name|queryId
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"database"
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
name|String
name|getDatabaseName
parameter_list|()
block|{
return|return
name|databaseName
return|;
block|}
specifier|public
name|void
name|setDatabaseName
parameter_list|(
name|String
name|databaseName
parameter_list|)
block|{
name|this
operator|.
name|databaseName
operator|=
name|databaseName
expr_stmt|;
block|}
specifier|public
name|void
name|setMode
parameter_list|(
name|String
name|mode
parameter_list|)
block|{
name|this
operator|.
name|mode
operator|=
name|mode
expr_stmt|;
block|}
specifier|public
name|String
name|getMode
parameter_list|()
block|{
return|return
name|mode
return|;
block|}
specifier|public
name|String
name|getQueryId
parameter_list|()
block|{
return|return
name|queryId
return|;
block|}
specifier|public
name|void
name|setQueryId
parameter_list|(
name|String
name|queryId
parameter_list|)
block|{
name|this
operator|.
name|queryId
operator|=
name|queryId
expr_stmt|;
block|}
specifier|public
name|String
name|getQueryStr
parameter_list|()
block|{
return|return
name|queryStr
return|;
block|}
specifier|public
name|void
name|setQueryStr
parameter_list|(
name|String
name|queryStr
parameter_list|)
block|{
name|this
operator|.
name|queryStr
operator|=
name|queryStr
expr_stmt|;
block|}
block|}
end_class

end_unit

