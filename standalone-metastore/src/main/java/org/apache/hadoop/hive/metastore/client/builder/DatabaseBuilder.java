begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metastore
operator|.
name|client
operator|.
name|builder
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
name|metastore
operator|.
name|api
operator|.
name|Database
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
name|metastore
operator|.
name|api
operator|.
name|MetaException
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
name|metastore
operator|.
name|api
operator|.
name|PrincipalType
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
name|metastore
operator|.
name|utils
operator|.
name|MetaStoreUtils
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
name|metastore
operator|.
name|utils
operator|.
name|SecurityUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

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
name|HashMap
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

begin_comment
comment|/**  * A builder for {@link Database}.  The name of the new database is required.  Everything else  * selects reasonable defaults.  */
end_comment

begin_class
specifier|public
class|class
name|DatabaseBuilder
block|{
specifier|private
name|String
name|name
decl_stmt|,
name|description
decl_stmt|,
name|location
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|String
name|ownerName
decl_stmt|;
specifier|private
name|PrincipalType
name|ownerType
decl_stmt|;
specifier|public
name|DatabaseBuilder
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|DatabaseBuilder
name|setDescription
parameter_list|(
name|String
name|description
parameter_list|)
block|{
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|DatabaseBuilder
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
return|return
name|this
return|;
block|}
specifier|public
name|DatabaseBuilder
name|setParams
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
name|this
operator|.
name|params
operator|=
name|params
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|DatabaseBuilder
name|addParam
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|params
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|DatabaseBuilder
name|setOwnerName
parameter_list|(
name|String
name|ownerName
parameter_list|)
block|{
name|this
operator|.
name|ownerName
operator|=
name|ownerName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|DatabaseBuilder
name|setOwnerType
parameter_list|(
name|PrincipalType
name|ownerType
parameter_list|)
block|{
name|this
operator|.
name|ownerType
operator|=
name|ownerType
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|Database
name|build
parameter_list|()
throws|throws
name|TException
block|{
if|if
condition|(
name|name
operator|==
literal|null
condition|)
throw|throw
operator|new
name|MetaException
argument_list|(
literal|"You must name the database"
argument_list|)
throw|;
name|Database
name|db
init|=
operator|new
name|Database
argument_list|(
name|name
argument_list|,
name|description
argument_list|,
name|location
argument_list|,
name|params
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|ownerName
operator|!=
literal|null
condition|)
name|ownerName
operator|=
name|SecurityUtils
operator|.
name|getUser
argument_list|()
expr_stmt|;
name|db
operator|.
name|setOwnerName
argument_list|(
name|ownerName
argument_list|)
expr_stmt|;
if|if
condition|(
name|ownerType
operator|==
literal|null
condition|)
name|ownerType
operator|=
name|PrincipalType
operator|.
name|USER
expr_stmt|;
name|db
operator|.
name|setOwnerType
argument_list|(
name|ownerType
argument_list|)
expr_stmt|;
return|return
name|db
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|MetaStoreUtils
operator|.
name|newMetaException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

