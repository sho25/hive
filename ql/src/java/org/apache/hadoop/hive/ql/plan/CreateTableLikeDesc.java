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

begin_class
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"Create Table"
argument_list|)
specifier|public
class|class
name|CreateTableLikeDesc
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
name|tableName
decl_stmt|;
name|boolean
name|isExternal
decl_stmt|;
name|String
name|location
decl_stmt|;
name|boolean
name|ifNotExists
decl_stmt|;
name|String
name|likeTableName
decl_stmt|;
specifier|public
name|CreateTableLikeDesc
parameter_list|()
block|{   }
specifier|public
name|CreateTableLikeDesc
parameter_list|(
name|String
name|tableName
parameter_list|,
name|boolean
name|isExternal
parameter_list|,
name|String
name|location
parameter_list|,
name|boolean
name|ifNotExists
parameter_list|,
name|String
name|likeTableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|isExternal
operator|=
name|isExternal
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
name|this
operator|.
name|ifNotExists
operator|=
name|ifNotExists
expr_stmt|;
name|this
operator|.
name|likeTableName
operator|=
name|likeTableName
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"if not exists"
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
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"name"
argument_list|)
specifier|public
name|String
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
specifier|public
name|void
name|setTableName
parameter_list|(
name|String
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"location"
argument_list|)
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|location
return|;
block|}
specifier|public
name|void
name|setLocation
parameter_list|(
name|String
name|location
parameter_list|)
block|{
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"isExternal"
argument_list|)
specifier|public
name|boolean
name|isExternal
parameter_list|()
block|{
return|return
name|isExternal
return|;
block|}
specifier|public
name|void
name|setExternal
parameter_list|(
name|boolean
name|isExternal
parameter_list|)
block|{
name|this
operator|.
name|isExternal
operator|=
name|isExternal
expr_stmt|;
block|}
annotation|@
name|Explain
argument_list|(
name|displayName
operator|=
literal|"like"
argument_list|)
specifier|public
name|String
name|getLikeTableName
parameter_list|()
block|{
return|return
name|likeTableName
return|;
block|}
specifier|public
name|void
name|setLikeTableName
parameter_list|(
name|String
name|likeTableName
parameter_list|)
block|{
name|this
operator|.
name|likeTableName
operator|=
name|likeTableName
expr_stmt|;
block|}
block|}
end_class

end_unit

