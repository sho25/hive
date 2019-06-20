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
name|ReplChangeManager
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
name|ql
operator|.
name|ddl
operator|.
name|DDLWork
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
name|ddl
operator|.
name|database
operator|.
name|AlterDatabaseDesc
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
name|ddl
operator|.
name|privilege
operator|.
name|PrincipalDesc
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
name|exec
operator|.
name|repl
operator|.
name|util
operator|.
name|ReplUtils
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
name|ReplicationSpec
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
name|parse
operator|.
name|repl
operator|.
name|dump
operator|.
name|Utils
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
name|HashMap
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
comment|/**  * AlterDatabaseHandler.  * Handler at target warehouse for the EVENT_ALTER_DATABASE type of messages  */
end_comment

begin_class
specifier|public
class|class
name|AlterDatabaseHandler
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
name|AlterDatabaseMessage
name|msg
init|=
name|deserializer
operator|.
name|getAlterDatabaseMessage
argument_list|(
name|context
operator|.
name|dmd
operator|.
name|getPayload
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|actualDbName
init|=
name|context
operator|.
name|isDbNameEmpty
argument_list|()
condition|?
name|msg
operator|.
name|getDB
argument_list|()
else|:
name|context
operator|.
name|dbName
decl_stmt|;
try|try
block|{
name|Database
name|oldDb
init|=
name|msg
operator|.
name|getDbObjBefore
argument_list|()
decl_stmt|;
name|Database
name|newDb
init|=
name|msg
operator|.
name|getDbObjAfter
argument_list|()
decl_stmt|;
name|AlterDatabaseDesc
name|alterDbDesc
decl_stmt|;
if|if
condition|(
operator|(
name|oldDb
operator|.
name|getOwnerType
argument_list|()
operator|==
name|newDb
operator|.
name|getOwnerType
argument_list|()
operator|)
operator|&&
name|oldDb
operator|.
name|getOwnerName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|newDb
operator|.
name|getOwnerName
argument_list|()
argument_list|)
condition|)
block|{
comment|// If owner information is unchanged, then DB properties would've changed
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|newDbProps
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|dbProps
init|=
name|newDb
operator|.
name|getParameters
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|dbProps
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
comment|// Ignore the keys which are local to source warehouse
if|if
condition|(
name|key
operator|.
name|startsWith
argument_list|(
name|Utils
operator|.
name|BOOTSTRAP_DUMP_STATE_KEY_PREFIX
argument_list|)
operator|||
name|key
operator|.
name|equals
argument_list|(
name|ReplicationSpec
operator|.
name|KEY
operator|.
name|CURR_STATE_ID
operator|.
name|toString
argument_list|()
argument_list|)
operator|||
name|key
operator|.
name|equals
argument_list|(
name|ReplUtils
operator|.
name|REPL_CHECKPOINT_KEY
argument_list|)
operator|||
name|key
operator|.
name|equals
argument_list|(
name|ReplChangeManager
operator|.
name|SOURCE_OF_REPLICATION
argument_list|)
operator|||
name|key
operator|.
name|equals
argument_list|(
name|ReplUtils
operator|.
name|REPL_FIRST_INC_PENDING_FLAG
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|newDbProps
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|alterDbDesc
operator|=
operator|new
name|AlterDatabaseDesc
argument_list|(
name|actualDbName
argument_list|,
name|newDbProps
argument_list|,
name|context
operator|.
name|eventOnlyReplicationSpec
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|alterDbDesc
operator|=
operator|new
name|AlterDatabaseDesc
argument_list|(
name|actualDbName
argument_list|,
operator|new
name|PrincipalDesc
argument_list|(
name|newDb
operator|.
name|getOwnerName
argument_list|()
argument_list|,
name|newDb
operator|.
name|getOwnerType
argument_list|()
argument_list|)
argument_list|,
name|context
operator|.
name|eventOnlyReplicationSpec
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Task
argument_list|<
name|DDLWork
argument_list|>
name|alterDbTask
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
name|alterDbDesc
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
literal|"Added alter database task : {}:{}"
argument_list|,
name|alterDbTask
operator|.
name|getId
argument_list|()
argument_list|,
name|actualDbName
argument_list|)
expr_stmt|;
comment|// Only database object is updated
name|updatedMetadata
operator|.
name|set
argument_list|(
name|context
operator|.
name|dmd
operator|.
name|getEventTo
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|actualDbName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
name|alterDbTask
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

