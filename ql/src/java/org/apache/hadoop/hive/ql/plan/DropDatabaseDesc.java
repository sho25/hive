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
comment|/**  * DropDatabaseDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Drop Database"
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
name|DropDatabaseDesc
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
name|String
name|databaseName
decl_stmt|;
name|boolean
name|ifExists
decl_stmt|;
name|boolean
name|cascade
decl_stmt|;
specifier|public
name|DropDatabaseDesc
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|boolean
name|ifExists
parameter_list|)
block|{
name|this
argument_list|(
name|databaseName
argument_list|,
name|ifExists
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|DropDatabaseDesc
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|boolean
name|ifExists
parameter_list|,
name|boolean
name|cascade
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|databaseName
operator|=
name|databaseName
expr_stmt|;
name|this
operator|.
name|ifExists
operator|=
name|ifExists
expr_stmt|;
name|this
operator|.
name|cascade
operator|=
name|cascade
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
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"if exists"
argument_list|)
specifier|public
name|boolean
name|getIfExists
parameter_list|()
block|{
return|return
name|ifExists
return|;
block|}
specifier|public
name|void
name|setIfExists
parameter_list|(
name|boolean
name|ifExists
parameter_list|)
block|{
name|this
operator|.
name|ifExists
operator|=
name|ifExists
expr_stmt|;
block|}
specifier|public
name|boolean
name|isCasdade
parameter_list|()
block|{
return|return
name|cascade
return|;
block|}
specifier|public
name|void
name|setIsCascade
parameter_list|(
name|boolean
name|cascade
parameter_list|)
block|{
name|this
operator|.
name|cascade
operator|=
name|cascade
expr_stmt|;
block|}
block|}
end_class

end_unit

