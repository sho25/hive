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
name|messaging
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

begin_comment
comment|/**  * Interface for converting HCat events from String-form back to HCatEventMessage instances.  */
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
specifier|abstract
class|class
name|MessageDeserializer
block|{
comment|/**    * Method to construct HCatEventMessage from string.    */
specifier|public
name|HCatEventMessage
name|getHCatEventMessage
parameter_list|(
name|String
name|eventTypeString
parameter_list|,
name|String
name|messageBody
parameter_list|)
block|{
switch|switch
condition|(
name|HCatEventMessage
operator|.
name|EventType
operator|.
name|valueOf
argument_list|(
name|eventTypeString
argument_list|)
condition|)
block|{
case|case
name|CREATE_DATABASE
case|:
return|return
name|getCreateDatabaseMessage
argument_list|(
name|messageBody
argument_list|)
return|;
case|case
name|DROP_DATABASE
case|:
return|return
name|getDropDatabaseMessage
argument_list|(
name|messageBody
argument_list|)
return|;
case|case
name|CREATE_TABLE
case|:
return|return
name|getCreateTableMessage
argument_list|(
name|messageBody
argument_list|)
return|;
case|case
name|ALTER_TABLE
case|:
return|return
name|getAlterTableMessage
argument_list|(
name|messageBody
argument_list|)
return|;
case|case
name|DROP_TABLE
case|:
return|return
name|getDropTableMessage
argument_list|(
name|messageBody
argument_list|)
return|;
case|case
name|ADD_PARTITION
case|:
return|return
name|getAddPartitionMessage
argument_list|(
name|messageBody
argument_list|)
return|;
case|case
name|ALTER_PARTITION
case|:
return|return
name|getAlterPartitionMessage
argument_list|(
name|messageBody
argument_list|)
return|;
case|case
name|DROP_PARTITION
case|:
return|return
name|getDropPartitionMessage
argument_list|(
name|messageBody
argument_list|)
return|;
case|case
name|CREATE_FUNCTION
case|:
return|return
name|getCreateFunctionMessage
argument_list|(
name|messageBody
argument_list|)
return|;
case|case
name|DROP_FUNCTION
case|:
return|return
name|getDropFunctionMessage
argument_list|(
name|messageBody
argument_list|)
return|;
case|case
name|INSERT
case|:
return|return
name|getInsertMessage
argument_list|(
name|messageBody
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unsupported event-type: "
operator|+
name|eventTypeString
argument_list|)
throw|;
block|}
block|}
comment|/**    * Method to de-serialize CreateDatabaseMessage instance.    */
specifier|public
specifier|abstract
name|CreateDatabaseMessage
name|getCreateDatabaseMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
function_decl|;
comment|/**    * Method to de-serialize DropDatabaseMessage instance.    */
specifier|public
specifier|abstract
name|DropDatabaseMessage
name|getDropDatabaseMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
function_decl|;
comment|/**    * Method to de-serialize CreateTableMessage instance.    */
specifier|public
specifier|abstract
name|CreateTableMessage
name|getCreateTableMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
function_decl|;
comment|/**    * Method to de-serialize AlterTableMessge    * @param messageBody string message    * @return object message    */
specifier|public
specifier|abstract
name|AlterTableMessage
name|getAlterTableMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
function_decl|;
comment|/**    * Method to de-serialize DropTableMessage instance.    */
specifier|public
specifier|abstract
name|DropTableMessage
name|getDropTableMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
function_decl|;
comment|/**    * Method to de-serialize AddPartitionMessage instance.    */
specifier|public
specifier|abstract
name|AddPartitionMessage
name|getAddPartitionMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
function_decl|;
comment|/**    * Method to deserialize AlterPartitionMessage    * @param messageBody the message in serialized form    * @return message in object form    */
specifier|public
specifier|abstract
name|AlterPartitionMessage
name|getAlterPartitionMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
function_decl|;
comment|/**    * Method to de-serialize DropPartitionMessage instance.    */
specifier|public
specifier|abstract
name|DropPartitionMessage
name|getDropPartitionMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
function_decl|;
comment|/**    * Method to de-serialize CreateFunctionMessage instance.    */
specifier|public
specifier|abstract
name|CreateFunctionMessage
name|getCreateFunctionMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
function_decl|;
comment|/**    * Method to de-serialize DropFunctionMessage instance.    */
specifier|public
specifier|abstract
name|DropFunctionMessage
name|getDropFunctionMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
function_decl|;
comment|/**    * Method to deserialize InsertMessage    * @param messageBody the message in serialized form    * @return message in object form    */
specifier|public
specifier|abstract
name|InsertMessage
name|getInsertMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
function_decl|;
comment|// Protection against construction.
specifier|protected
name|MessageDeserializer
parameter_list|()
block|{}
block|}
end_class

end_unit

