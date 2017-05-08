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
operator|.
name|load
operator|.
name|message
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
name|ql
operator|.
name|exec
operator|.
name|Task
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
name|exec
operator|.
name|TaskFactory
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
name|SemanticException
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
name|AlterTableDesc
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
name|DDLWork
import|;
end_import

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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_class
class|class
name|RenameTableHandler
extends|extends
name|AbstractMessageHandler
block|{
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|handle
parameter_list|(
name|Context
name|context
parameter_list|)
throws|throws
name|SemanticException
block|{
name|AlterTableMessage
name|msg
init|=
name|deserializer
operator|.
name|getAlterTableMessage
argument_list|(
name|context
operator|.
name|dmd
operator|.
name|getPayload
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|context
operator|.
name|isTableNameEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"RENAMES of tables are not supported for table-level replication"
argument_list|)
throw|;
block|}
try|try
block|{
name|String
name|oldDbName
init|=
name|msg
operator|.
name|getTableObjBefore
argument_list|()
operator|.
name|getDbName
argument_list|()
decl_stmt|;
name|String
name|newDbName
init|=
name|msg
operator|.
name|getTableObjAfter
argument_list|()
operator|.
name|getDbName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|context
operator|.
name|isDbNameEmpty
argument_list|()
condition|)
block|{
comment|// If we're loading into a db, instead of into the warehouse, then the oldDbName and
comment|// newDbName must be the same
if|if
condition|(
operator|!
name|oldDbName
operator|.
name|equalsIgnoreCase
argument_list|(
name|newDbName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
literal|"Cannot replicate an event renaming a table across"
operator|+
literal|" databases into a db level load "
operator|+
name|oldDbName
operator|+
literal|"->"
operator|+
name|newDbName
argument_list|)
throw|;
block|}
else|else
block|{
comment|// both were the same, and can be replaced by the new db we're loading into.
name|oldDbName
operator|=
name|context
operator|.
name|dbName
expr_stmt|;
name|newDbName
operator|=
name|context
operator|.
name|dbName
expr_stmt|;
block|}
block|}
name|String
name|oldName
init|=
name|oldDbName
operator|+
literal|"."
operator|+
name|msg
operator|.
name|getTableObjBefore
argument_list|()
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|String
name|newName
init|=
name|newDbName
operator|+
literal|"."
operator|+
name|msg
operator|.
name|getTableObjAfter
argument_list|()
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|AlterTableDesc
name|renameTableDesc
init|=
operator|new
name|AlterTableDesc
argument_list|(
name|oldName
argument_list|,
name|newName
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|DDLWork
argument_list|>
name|renameTableTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DDLWork
argument_list|(
name|readEntitySet
argument_list|,
name|writeEntitySet
argument_list|,
name|renameTableDesc
argument_list|)
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|context
operator|.
name|log
operator|.
name|debug
argument_list|(
literal|"Added rename table task : {}:{}->{}"
argument_list|,
name|renameTableTask
operator|.
name|getId
argument_list|()
argument_list|,
name|oldName
argument_list|,
name|newName
argument_list|)
expr_stmt|;
comment|// oldDbName and newDbName *will* be the same if we're here
name|databasesUpdated
operator|.
name|put
argument_list|(
name|newDbName
argument_list|,
name|context
operator|.
name|dmd
operator|.
name|getEventTo
argument_list|()
argument_list|)
expr_stmt|;
name|tablesUpdated
operator|.
name|remove
argument_list|(
name|oldName
argument_list|)
expr_stmt|;
name|tablesUpdated
operator|.
name|put
argument_list|(
name|newName
argument_list|,
name|context
operator|.
name|dmd
operator|.
name|getEventTo
argument_list|()
argument_list|)
expr_stmt|;
comment|// Note : edge-case here in interaction with table-level REPL LOAD, where that nukes out tablesUpdated
comment|// However, we explicitly don't support repl of that sort, and error out above if so. If that should
comment|// ever change, this will need reworking.
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|renameTableTask
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
operator|(
name|e
operator|instanceof
name|SemanticException
operator|)
condition|?
operator|(
name|SemanticException
operator|)
name|e
else|:
operator|new
name|SemanticException
argument_list|(
literal|"Error reading message members"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

