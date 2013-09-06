begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|messaging
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
import|;
end_import

begin_comment
comment|/**  * Class representing messages emitted when Metastore operations are done.  * (E.g. Creation and deletion of databases, tables and partitions.)  * @deprecated Use/modify {@link org.apache.hive.hcatalog.messaging.HCatEventMessage} instead  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|HCatEventMessage
block|{
comment|/**      * Enumeration of all supported types of Metastore operations.      */
specifier|public
specifier|static
enum|enum
name|EventType
block|{
name|CREATE_DATABASE
parameter_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_DATABASE_EVENT
parameter_list|)
operator|,
constructor|DROP_DATABASE(HCatConstants.HCAT_DROP_DATABASE_EVENT
block|)
enum|,
name|CREATE_TABLE
parameter_list|(
name|HCatConstants
operator|.
name|HCAT_CREATE_TABLE_EVENT
parameter_list|)
operator|,
constructor|DROP_TABLE(HCatConstants.HCAT_DROP_TABLE_EVENT
block|)
operator|,
name|ADD_PARTITION
argument_list|(
name|HCatConstants
operator|.
name|HCAT_ADD_PARTITION_EVENT
argument_list|)
operator|,
name|DROP_PARTITION
argument_list|(
name|HCatConstants
operator|.
name|HCAT_DROP_PARTITION_EVENT
argument_list|)
expr_stmt|;
end_class

begin_decl_stmt
specifier|private
name|String
name|typeString
decl_stmt|;
end_decl_stmt

begin_expr_stmt
name|EventType
argument_list|(
name|String
name|typeString
argument_list|)
block|{
name|this
operator|.
name|typeString
operator|=
name|typeString
block|;         }
expr|@
name|Override
specifier|public
name|String
name|toString
argument_list|()
block|{
return|return
name|typeString
return|;
block|}
end_expr_stmt

begin_decl_stmt
unit|}      protected
name|EventType
name|eventType
decl_stmt|;
end_decl_stmt

begin_constructor
specifier|protected
name|HCatEventMessage
parameter_list|(
name|EventType
name|eventType
parameter_list|)
block|{
name|this
operator|.
name|eventType
operator|=
name|eventType
expr_stmt|;
block|}
end_constructor

begin_function
specifier|public
name|EventType
name|getEventType
parameter_list|()
block|{
return|return
name|eventType
return|;
block|}
end_function

begin_comment
comment|/**      * Getter for HCatalog Server's URL.      * (This is where the event originates from.)      * @return HCatalog Server's URL (String).      */
end_comment

begin_function_decl
specifier|public
specifier|abstract
name|String
name|getServer
parameter_list|()
function_decl|;
end_function_decl

begin_comment
comment|/**      * Getter for the Kerberos principal of the HCatalog service.      * @return HCatalog Service Principal (String).      */
end_comment

begin_function_decl
specifier|public
specifier|abstract
name|String
name|getServicePrincipal
parameter_list|()
function_decl|;
end_function_decl

begin_comment
comment|/**      * Getter for the name of the Database on which the Metastore operation is done.      * @return Database-name (String).      */
end_comment

begin_function_decl
specifier|public
specifier|abstract
name|String
name|getDB
parameter_list|()
function_decl|;
end_function_decl

begin_comment
comment|/**      * Getter for the timestamp associated with the operation.      * @return Timestamp (Long - seconds since epoch).      */
end_comment

begin_function_decl
specifier|public
specifier|abstract
name|Long
name|getTimestamp
parameter_list|()
function_decl|;
end_function_decl

begin_comment
comment|/**      * Class invariant. Checked after construction or deserialization.      */
end_comment

begin_function
specifier|public
name|HCatEventMessage
name|checkValid
parameter_list|()
block|{
if|if
condition|(
name|getServer
argument_list|()
operator|==
literal|null
operator|||
name|getServicePrincipal
argument_list|()
operator|==
literal|null
condition|)
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Server-URL/Service-Principal shouldn't be null."
argument_list|)
throw|;
if|if
condition|(
name|getEventType
argument_list|()
operator|==
literal|null
condition|)
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Event-type unset."
argument_list|)
throw|;
if|if
condition|(
name|getDB
argument_list|()
operator|==
literal|null
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"DB-name unset."
argument_list|)
throw|;
return|return
name|this
return|;
block|}
end_function

unit|}
end_unit

