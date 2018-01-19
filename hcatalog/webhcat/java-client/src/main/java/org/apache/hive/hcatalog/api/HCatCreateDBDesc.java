begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|api
package|;
end_package

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
name|common
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|common
operator|.
name|classification
operator|.
name|InterfaceStability
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
name|Database
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
name|hcatalog
operator|.
name|common
operator|.
name|HCatException
import|;
end_import

begin_comment
comment|/**  * The Class HCatCreateDBDesc for defining database attributes.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|HCatCreateDBDesc
block|{
specifier|private
name|String
name|dbName
decl_stmt|;
specifier|private
name|String
name|locationUri
decl_stmt|;
specifier|private
name|String
name|comment
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|dbProperties
decl_stmt|;
specifier|private
name|boolean
name|ifNotExits
init|=
literal|false
decl_stmt|;
comment|/**    * Gets the database properties.    *    * @return the database properties    */
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
name|this
operator|.
name|dbProperties
return|;
block|}
comment|/**    * Gets the if not exists.    *    * @return the if not exists    */
specifier|public
name|boolean
name|getIfNotExists
parameter_list|()
block|{
return|return
name|this
operator|.
name|ifNotExits
return|;
block|}
comment|/**    * Gets the comments.    *    * @return the comments    */
specifier|public
name|String
name|getComments
parameter_list|()
block|{
return|return
name|this
operator|.
name|comment
return|;
block|}
comment|/**    * Gets the location.    *    * @return the location    */
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|this
operator|.
name|locationUri
return|;
block|}
comment|/**    * Gets the database name.    *    * @return the database name    */
specifier|public
name|String
name|getDatabaseName
parameter_list|()
block|{
return|return
name|this
operator|.
name|dbName
return|;
block|}
specifier|private
name|HCatCreateDBDesc
parameter_list|(
name|String
name|dbName
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"HCatCreateDBDesc ["
operator|+
operator|(
name|dbName
operator|!=
literal|null
condition|?
literal|"dbName="
operator|+
name|dbName
operator|+
literal|", "
else|:
literal|"dbName=null"
operator|)
operator|+
operator|(
name|locationUri
operator|!=
literal|null
condition|?
literal|"location="
operator|+
name|locationUri
operator|+
literal|", "
else|:
literal|"location=null"
operator|)
operator|+
operator|(
name|comment
operator|!=
literal|null
condition|?
literal|"comment="
operator|+
name|comment
operator|+
literal|", "
else|:
literal|"comment=null"
operator|)
operator|+
operator|(
name|dbProperties
operator|!=
literal|null
condition|?
literal|"dbProperties="
operator|+
name|dbProperties
operator|+
literal|", "
else|:
literal|"dbProperties=null"
operator|)
operator|+
literal|"ifNotExits="
operator|+
name|ifNotExits
operator|+
literal|"]"
return|;
block|}
comment|/**    * Creates the builder for defining attributes.    *    * @param dbName the db name    * @return the builder    */
specifier|public
specifier|static
name|Builder
name|create
parameter_list|(
name|String
name|dbName
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|(
name|dbName
argument_list|)
return|;
block|}
name|Database
name|toHiveDb
parameter_list|()
block|{
name|Database
name|hiveDB
init|=
operator|new
name|Database
argument_list|()
decl_stmt|;
name|hiveDB
operator|.
name|setDescription
argument_list|(
name|this
operator|.
name|comment
argument_list|)
expr_stmt|;
name|hiveDB
operator|.
name|setLocationUri
argument_list|(
name|this
operator|.
name|locationUri
argument_list|)
expr_stmt|;
name|hiveDB
operator|.
name|setName
argument_list|(
name|this
operator|.
name|dbName
argument_list|)
expr_stmt|;
name|hiveDB
operator|.
name|setParameters
argument_list|(
name|this
operator|.
name|dbProperties
argument_list|)
expr_stmt|;
return|return
name|hiveDB
return|;
block|}
specifier|public
specifier|static
class|class
name|Builder
block|{
specifier|private
name|String
name|innerLoc
decl_stmt|;
specifier|private
name|String
name|innerComment
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|innerDBProps
decl_stmt|;
specifier|private
name|String
name|dbName
decl_stmt|;
specifier|private
name|boolean
name|ifNotExists
init|=
literal|false
decl_stmt|;
specifier|private
name|Builder
parameter_list|(
name|String
name|dbName
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|dbName
expr_stmt|;
block|}
comment|/**      * Location.      *      * @param value the location of the database.      * @return the builder      */
specifier|public
name|Builder
name|location
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|this
operator|.
name|innerLoc
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Comment.      *      * @param value comments.      * @return the builder      */
specifier|public
name|Builder
name|comment
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|this
operator|.
name|innerComment
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * If not exists.      * @param ifNotExists If set to true, hive will not throw exception, if a      * database with the same name already exists.      * @return the builder      */
specifier|public
name|Builder
name|ifNotExists
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
return|return
name|this
return|;
block|}
comment|/**      * Database properties.      *      * @param dbProps the database properties      * @return the builder      */
specifier|public
name|Builder
name|databaseProperties
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
name|innerDBProps
operator|=
name|dbProps
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Builds the create database descriptor.      *      * @return An instance of HCatCreateDBDesc      * @throws HCatException      */
specifier|public
name|HCatCreateDBDesc
name|build
parameter_list|()
throws|throws
name|HCatException
block|{
if|if
condition|(
name|this
operator|.
name|dbName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HCatException
argument_list|(
literal|"Database name cannot be null."
argument_list|)
throw|;
block|}
name|HCatCreateDBDesc
name|desc
init|=
operator|new
name|HCatCreateDBDesc
argument_list|(
name|this
operator|.
name|dbName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|comment
operator|=
name|this
operator|.
name|innerComment
expr_stmt|;
name|desc
operator|.
name|locationUri
operator|=
name|this
operator|.
name|innerLoc
expr_stmt|;
name|desc
operator|.
name|dbProperties
operator|=
name|this
operator|.
name|innerDBProps
expr_stmt|;
name|desc
operator|.
name|ifNotExits
operator|=
name|this
operator|.
name|ifNotExists
expr_stmt|;
return|return
name|desc
return|;
block|}
block|}
block|}
end_class

end_unit

