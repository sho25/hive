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
name|java
operator|.
name|util
operator|.
name|Map
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
comment|/**  * CreateDatabaseDesc.  *  */
end_comment

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Create Database"
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
name|CreateDatabaseDesc
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
name|String
name|locationUri
decl_stmt|;
name|String
name|comment
decl_stmt|;
name|boolean
name|ifNotExists
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|dbProperties
decl_stmt|;
comment|/**    * For serialization only.    */
specifier|public
name|CreateDatabaseDesc
parameter_list|()
block|{   }
specifier|public
name|CreateDatabaseDesc
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|String
name|comment
parameter_list|,
name|String
name|locationUri
parameter_list|,
name|boolean
name|ifNotExists
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
name|comment
operator|=
name|comment
expr_stmt|;
name|this
operator|.
name|locationUri
operator|=
name|locationUri
expr_stmt|;
name|this
operator|.
name|ifNotExists
operator|=
name|ifNotExists
expr_stmt|;
name|this
operator|.
name|dbProperties
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|CreateDatabaseDesc
parameter_list|(
name|String
name|databaseName
parameter_list|,
name|boolean
name|ifNotExists
parameter_list|)
block|{
name|this
argument_list|(
name|databaseName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|ifNotExists
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"if not exists"
argument_list|,
name|displayOnlyOnTrue
operator|=
literal|true
argument_list|)
specifier|public
name|boolean
name|getIfNotExists
parameter_list|()
block|{
return|return
name|ifNotExists
return|;
block|}
specifier|public
name|void
name|setIfNotExists
parameter_list|(
name|boolean
name|ifNotExists
parameter_list|)
block|{
name|this
operator|.
name|ifNotExists
operator|=
name|ifNotExists
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getDatabaseProperties
parameter_list|()
block|{
return|return
name|dbProperties
return|;
block|}
specifier|public
name|void
name|setDatabaseProperties
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|dbProps
parameter_list|)
block|{
name|this
operator|.
name|dbProperties
operator|=
name|dbProps
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"name"
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
name|getName
parameter_list|()
block|{
return|return
name|databaseName
return|;
block|}
specifier|public
name|void
name|setName
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
literal|"comment"
argument_list|)
specifier|public
name|String
name|getComment
parameter_list|()
block|{
return|return
name|comment
return|;
block|}
specifier|public
name|void
name|setComment
parameter_list|(
name|String
name|comment
parameter_list|)
block|{
name|this
operator|.
name|comment
operator|=
name|comment
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"locationUri"
argument_list|)
specifier|public
name|String
name|getLocationUri
parameter_list|()
block|{
return|return
name|locationUri
return|;
block|}
specifier|public
name|void
name|setLocationUri
parameter_list|(
name|String
name|locationUri
parameter_list|)
block|{
name|this
operator|.
name|locationUri
operator|=
name|locationUri
expr_stmt|;
block|}
block|}
end_class

end_unit

