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
name|parse
operator|.
name|repl
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
name|ql
operator|.
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|AddNotNullConstraintHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|AddForeignKeyHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|AddPrimaryKeyHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|AddUniqueConstraintHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|AlterDatabaseHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|CreateDatabaseHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|CreateFunctionHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|DefaultHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|DropConstraintHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|DropDatabaseHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|DropFunctionHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|DropPartitionHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|DropTableHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|InsertHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|MessageHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|RenamePartitionHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|RenameTableHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|TableHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|TruncatePartitionHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|TruncateTableHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|OpenTxnHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|CommitTxnHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|AbortTxnHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|AllocWriteIdHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|UpdateTableColStatHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|DeleteTableColStatHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|UpdatePartColStatHandler
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
name|parse
operator|.
name|repl
operator|.
name|load
operator|.
name|message
operator|.
name|DeletePartColStatHandler
import|;
end_import

begin_enum
specifier|public
enum|enum
name|DumpType
block|{
name|EVENT_CREATE_TABLE
argument_list|(
literal|"EVENT_CREATE_TABLE"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|TableHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_ADD_PARTITION
argument_list|(
literal|"EVENT_ADD_PARTITION"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|TableHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_DROP_TABLE
argument_list|(
literal|"EVENT_DROP_TABLE"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|DropTableHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_DROP_FUNCTION
argument_list|(
literal|"EVENT_DROP_FUNCTION"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|DropFunctionHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_DROP_PARTITION
argument_list|(
literal|"EVENT_DROP_PARTITION"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|DropPartitionHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_ALTER_DATABASE
argument_list|(
literal|"EVENT_ALTER_DATABASE"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|AlterDatabaseHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_ALTER_TABLE
argument_list|(
literal|"EVENT_ALTER_TABLE"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|TableHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_RENAME_TABLE
argument_list|(
literal|"EVENT_RENAME_TABLE"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|RenameTableHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_RENAME_DROP_TABLE
argument_list|(
literal|"EVENT_RENAME_DROP_TABLE"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|DropTableHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_TRUNCATE_TABLE
argument_list|(
literal|"EVENT_TRUNCATE_TABLE"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|TruncateTableHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_ALTER_PARTITION
argument_list|(
literal|"EVENT_ALTER_PARTITION"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|TableHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_RENAME_PARTITION
argument_list|(
literal|"EVENT_RENAME_PARTITION"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|RenamePartitionHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_TRUNCATE_PARTITION
argument_list|(
literal|"EVENT_TRUNCATE_PARTITION"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|TruncatePartitionHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_INSERT
argument_list|(
literal|"EVENT_INSERT"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|InsertHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_ADD_PRIMARYKEY
argument_list|(
literal|"EVENT_ADD_PRIMARYKEY"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|AddPrimaryKeyHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_ADD_FOREIGNKEY
argument_list|(
literal|"EVENT_ADD_FOREIGNKEY"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|AddForeignKeyHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_ADD_UNIQUECONSTRAINT
argument_list|(
literal|"EVENT_ADD_UNIQUECONSTRAINT"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|AddUniqueConstraintHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_ADD_NOTNULLCONSTRAINT
argument_list|(
literal|"EVENT_ADD_NOTNULLCONSTRAINT"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|AddNotNullConstraintHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_DROP_CONSTRAINT
argument_list|(
literal|"EVENT_DROP_CONSTRAINT"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|DropConstraintHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_CREATE_FUNCTION
argument_list|(
literal|"EVENT_CREATE_FUNCTION"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|CreateFunctionHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_UNKNOWN
argument_list|(
literal|"EVENT_UNKNOWN"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|DefaultHandler
argument_list|()
return|;
block|}
block|}
block|,
name|BOOTSTRAP
argument_list|(
literal|"BOOTSTRAP"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|DefaultHandler
argument_list|()
return|;
block|}
block|}
block|,
name|INCREMENTAL
argument_list|(
literal|"INCREMENTAL"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|DefaultHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_CREATE_DATABASE
argument_list|(
literal|"EVENT_CREATE_DATABASE"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|CreateDatabaseHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_DROP_DATABASE
argument_list|(
literal|"EVENT_DROP_DATABASE"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|DropDatabaseHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_OPEN_TXN
argument_list|(
literal|"EVENT_OPEN_TXN"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|OpenTxnHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_COMMIT_TXN
argument_list|(
literal|"EVENT_COMMIT_TXN"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|CommitTxnHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_ABORT_TXN
argument_list|(
literal|"EVENT_ABORT_TXN"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|AbortTxnHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_ALLOC_WRITE_ID
argument_list|(
literal|"EVENT_ALLOC_WRITE_ID"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|AllocWriteIdHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_UPDATE_TABLE_COL_STAT
argument_list|(
literal|"EVENT_UPDATE_TABLE_COL_STAT"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|UpdateTableColStatHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_DELETE_TABLE_COL_STAT
argument_list|(
literal|"EVENT_DELETE_TABLE_COL_STAT"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|DeleteTableColStatHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_UPDATE_PART_COL_STAT
argument_list|(
literal|"EVENT_UPDATE_PART_COL_STAT"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|UpdatePartColStatHandler
argument_list|()
return|;
block|}
block|}
block|,
name|EVENT_DELETE_PART_COL_STAT
argument_list|(
literal|"EVENT_DELETE_PART_COL_STAT"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|MessageHandler
name|handler
parameter_list|()
block|{
return|return
operator|new
name|DeletePartColStatHandler
argument_list|()
return|;
block|}
block|}
block|;
name|String
name|type
init|=
literal|null
decl_stmt|;
name|DumpType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
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
name|type
return|;
block|}
specifier|public
specifier|abstract
name|MessageHandler
name|handler
parameter_list|()
function_decl|;
block|}
end_enum

end_unit

