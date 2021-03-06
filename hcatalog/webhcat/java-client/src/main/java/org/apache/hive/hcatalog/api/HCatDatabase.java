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

begin_comment
comment|/**  * HCatDatabase is wrapper class around org.apache.hadoop.hive.metastore.api.Database.  */
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
name|HCatDatabase
block|{
specifier|private
name|String
name|dbName
decl_stmt|;
specifier|private
name|String
name|dbLocation
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
name|props
decl_stmt|;
name|HCatDatabase
parameter_list|(
name|Database
name|db
parameter_list|)
block|{
name|this
operator|.
name|dbName
operator|=
name|db
operator|.
name|getName
argument_list|()
expr_stmt|;
name|this
operator|.
name|props
operator|=
name|db
operator|.
name|getParameters
argument_list|()
expr_stmt|;
name|this
operator|.
name|dbLocation
operator|=
name|db
operator|.
name|getLocationUri
argument_list|()
expr_stmt|;
name|this
operator|.
name|comment
operator|=
name|db
operator|.
name|getDescription
argument_list|()
expr_stmt|;
block|}
comment|/**    * Gets the database name.    *    * @return the database name    */
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|dbName
return|;
block|}
comment|/**    * Gets the dB location.    *    * @return the dB location    */
specifier|public
name|String
name|getLocation
parameter_list|()
block|{
return|return
name|dbLocation
return|;
block|}
comment|/**    * Gets the comment.    *    * @return the comment    */
specifier|public
name|String
name|getComment
parameter_list|()
block|{
return|return
name|comment
return|;
block|}
comment|/**    * Gets the dB properties.    *    * @return the dB properties    */
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getProperties
parameter_list|()
block|{
return|return
name|props
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"HCatDatabase ["
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
name|dbLocation
operator|!=
literal|null
condition|?
literal|"dbLocation="
operator|+
name|dbLocation
operator|+
literal|", "
else|:
literal|"dbLocation=null"
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
name|props
operator|!=
literal|null
condition|?
literal|"props="
operator|+
name|props
else|:
literal|"props=null"
operator|)
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

