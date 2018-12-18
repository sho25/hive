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
name|hadoop
operator|.
name|hive
operator|.
name|metastore
operator|.
name|messaging
operator|.
name|json
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
name|messaging
operator|.
name|AbortTxnMessage
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
name|messaging
operator|.
name|AddForeignKeyMessage
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
name|messaging
operator|.
name|AddNotNullConstraintMessage
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
name|messaging
operator|.
name|AddPartitionMessage
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
name|messaging
operator|.
name|AddPrimaryKeyMessage
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
name|messaging
operator|.
name|AddUniqueConstraintMessage
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
name|messaging
operator|.
name|AllocWriteIdMessage
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
name|messaging
operator|.
name|AlterDatabaseMessage
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
name|messaging
operator|.
name|AlterPartitionMessage
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
name|messaging
operator|.
name|AlterTableMessage
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
name|messaging
operator|.
name|CommitTxnMessage
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
name|messaging
operator|.
name|CreateDatabaseMessage
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
name|messaging
operator|.
name|CreateFunctionMessage
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
name|messaging
operator|.
name|CreateTableMessage
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
name|messaging
operator|.
name|DropConstraintMessage
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
name|messaging
operator|.
name|DropDatabaseMessage
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
name|messaging
operator|.
name|DropFunctionMessage
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
name|messaging
operator|.
name|DropPartitionMessage
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
name|messaging
operator|.
name|DropTableMessage
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
name|messaging
operator|.
name|InsertMessage
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
name|messaging
operator|.
name|MessageDeserializer
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
name|messaging
operator|.
name|OpenTxnMessage
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
name|messaging
operator|.
name|AcidWriteMessage
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
name|messaging
operator|.
name|UpdateTableColumnStatMessage
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
name|messaging
operator|.
name|DeleteTableColumnStatMessage
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
name|messaging
operator|.
name|UpdatePartitionColumnStatMessage
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
name|messaging
operator|.
name|DeletePartitionColumnStatMessage
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|DeserializationFeature
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|MapperFeature
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|ObjectMapper
import|;
end_import

begin_comment
comment|/**  * MessageDeserializer implementation, for deserializing from JSON strings.  */
end_comment

begin_class
specifier|public
class|class
name|JSONMessageDeserializer
extends|extends
name|MessageDeserializer
block|{
specifier|static
name|ObjectMapper
name|mapper
init|=
operator|new
name|ObjectMapper
argument_list|()
decl_stmt|;
comment|// Thread-safe.
static|static
block|{
name|mapper
operator|.
name|configure
argument_list|(
name|DeserializationFeature
operator|.
name|FAIL_ON_UNKNOWN_PROPERTIES
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|mapper
operator|.
name|configure
argument_list|(
name|MapperFeature
operator|.
name|AUTO_DETECT_GETTERS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|mapper
operator|.
name|configure
argument_list|(
name|MapperFeature
operator|.
name|AUTO_DETECT_IS_GETTERS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|mapper
operator|.
name|configure
argument_list|(
name|MapperFeature
operator|.
name|AUTO_DETECT_FIELDS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|CreateDatabaseMessage
name|getCreateDatabaseMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONCreateDatabaseMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct JSONCreateDatabaseMessage."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AlterDatabaseMessage
name|getAlterDatabaseMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONAlterDatabaseMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct JSONAlterDatabaseMessage."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|DropDatabaseMessage
name|getDropDatabaseMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONDropDatabaseMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct JSONDropDatabaseMessage."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|CreateTableMessage
name|getCreateTableMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONCreateTableMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct JSONCreateTableMessage."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AlterTableMessage
name|getAlterTableMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONAlterTableMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct appropriate alter table type."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|DropTableMessage
name|getDropTableMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONDropTableMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct JSONDropTableMessage."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AddPartitionMessage
name|getAddPartitionMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONAddPartitionMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct AddPartitionMessage."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AlterPartitionMessage
name|getAlterPartitionMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONAlterPartitionMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct AlterPartitionMessage."
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|DropPartitionMessage
name|getDropPartitionMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONDropPartitionMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct DropPartitionMessage."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|CreateFunctionMessage
name|getCreateFunctionMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONCreateFunctionMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct JSONCreateFunctionMessage."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|DropFunctionMessage
name|getDropFunctionMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONDropFunctionMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct JSONDropDatabaseMessage."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|InsertMessage
name|getInsertMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONInsertMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct InsertMessage"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AddPrimaryKeyMessage
name|getAddPrimaryKeyMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONAddPrimaryKeyMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct AddPrimaryKeyMessage"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AddForeignKeyMessage
name|getAddForeignKeyMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONAddForeignKeyMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct AddForeignKeyMessage"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AddUniqueConstraintMessage
name|getAddUniqueConstraintMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONAddUniqueConstraintMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct AddUniqueConstraintMessage"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AddNotNullConstraintMessage
name|getAddNotNullConstraintMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONAddNotNullConstraintMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct AddNotNullConstraintMessage"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|DropConstraintMessage
name|getDropConstraintMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONDropConstraintMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct DropConstraintMessage"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|OpenTxnMessage
name|getOpenTxnMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONOpenTxnMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct OpenTxnMessage"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|CommitTxnMessage
name|getCommitTxnMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONCommitTxnMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct CommitTxnMessage"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AbortTxnMessage
name|getAbortTxnMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONAbortTxnMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct AbortTxnMessage"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AllocWriteIdMessage
name|getAllocWriteIdMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONAllocWriteIdMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct AllocWriteIdMessage"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|AcidWriteMessage
name|getAcidWriteMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONAcidWriteMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct AcidWriteMessage"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|UpdateTableColumnStatMessage
name|getUpdateTableColumnStatMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONUpdateTableColumnStatMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct UpdateTableColumnStatMessage"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|DeleteTableColumnStatMessage
name|getDeleteTableColumnStatMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONDeleteTableColumnStatMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct UpdateTableColumnStatMessage"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|UpdatePartitionColumnStatMessage
name|getUpdatePartitionColumnStatMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONUpdatePartitionColumnStatMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct UpdatePartitionColumnStatMessage"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|DeletePartitionColumnStatMessage
name|getDeletePartitionColumnStatMessage
parameter_list|(
name|String
name|messageBody
parameter_list|)
block|{
try|try
block|{
return|return
name|mapper
operator|.
name|readValue
argument_list|(
name|messageBody
argument_list|,
name|JSONDeletePartitionColumnStatMessage
operator|.
name|class
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Could not construct UpdatePartitionColumnStatMessage"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

