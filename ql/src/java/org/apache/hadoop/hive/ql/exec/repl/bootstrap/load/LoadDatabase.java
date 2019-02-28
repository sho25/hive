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
name|exec
operator|.
name|repl
operator|.
name|bootstrap
operator|.
name|load
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
name|conf
operator|.
name|HiveConf
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
name|api
operator|.
name|InvalidOperationException
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
name|DDLWork2
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
name|database
operator|.
name|CreateDatabaseDesc
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
name|bootstrap
operator|.
name|events
operator|.
name|DatabaseEvent
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
name|bootstrap
operator|.
name|load
operator|.
name|util
operator|.
name|Context
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
name|TaskTracker
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
name|metadata
operator|.
name|HiveException
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
name|plan
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
name|exec
operator|.
name|repl
operator|.
name|util
operator|.
name|ReplUtils
operator|.
name|ReplLoadOpType
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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

begin_class
specifier|public
class|class
name|LoadDatabase
block|{
specifier|final
name|Context
name|context
decl_stmt|;
specifier|final
name|TaskTracker
name|tracker
decl_stmt|;
specifier|private
specifier|final
name|DatabaseEvent
name|event
decl_stmt|;
specifier|private
specifier|final
name|String
name|dbNameToLoadIn
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isTableLevelLoad
decl_stmt|;
specifier|public
name|LoadDatabase
parameter_list|(
name|Context
name|context
parameter_list|,
name|DatabaseEvent
name|event
parameter_list|,
name|String
name|dbNameToLoadIn
parameter_list|,
name|String
name|tblNameToLoadIn
parameter_list|,
name|TaskTracker
name|loadTaskTracker
parameter_list|)
block|{
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|event
operator|=
name|event
expr_stmt|;
name|this
operator|.
name|dbNameToLoadIn
operator|=
name|dbNameToLoadIn
expr_stmt|;
name|this
operator|.
name|tracker
operator|=
operator|new
name|TaskTracker
argument_list|(
name|loadTaskTracker
argument_list|)
expr_stmt|;
comment|//TODO : Load database should not be called for table level load.
name|isTableLevelLoad
operator|=
name|tblNameToLoadIn
operator|!=
literal|null
operator|&&
operator|!
name|tblNameToLoadIn
operator|.
name|isEmpty
argument_list|()
expr_stmt|;
block|}
specifier|public
name|TaskTracker
name|tasks
parameter_list|()
throws|throws
name|SemanticException
block|{
try|try
block|{
name|Database
name|dbInMetadata
init|=
name|readDbMetadata
argument_list|()
decl_stmt|;
name|String
name|dbName
init|=
name|dbInMetadata
operator|.
name|getName
argument_list|()
decl_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|dbRootTask
init|=
literal|null
decl_stmt|;
name|ReplLoadOpType
name|loadDbType
init|=
name|getLoadDbType
argument_list|(
name|dbName
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|loadDbType
condition|)
block|{
case|case
name|LOAD_NEW
case|:
name|dbRootTask
operator|=
name|createDbTask
argument_list|(
name|dbInMetadata
argument_list|)
expr_stmt|;
break|break;
case|case
name|LOAD_REPLACE
case|:
name|dbRootTask
operator|=
name|alterDbTask
argument_list|(
name|dbInMetadata
argument_list|)
expr_stmt|;
break|break;
default|default:
break|break;
block|}
if|if
condition|(
name|dbRootTask
operator|!=
literal|null
condition|)
block|{
name|dbRootTask
operator|.
name|addDependentTask
argument_list|(
name|setOwnerInfoTask
argument_list|(
name|dbInMetadata
argument_list|)
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|addTask
argument_list|(
name|dbRootTask
argument_list|)
expr_stmt|;
block|}
return|return
name|tracker
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
name|SemanticException
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
name|Database
name|readDbMetadata
parameter_list|()
throws|throws
name|SemanticException
block|{
return|return
name|event
operator|.
name|dbInMetadata
argument_list|(
name|dbNameToLoadIn
argument_list|)
return|;
block|}
specifier|private
name|ReplLoadOpType
name|getLoadDbType
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|HiveException
block|{
name|Database
name|db
init|=
name|context
operator|.
name|hiveDb
operator|.
name|getDatabase
argument_list|(
name|dbName
argument_list|)
decl_stmt|;
if|if
condition|(
name|db
operator|==
literal|null
condition|)
block|{
return|return
name|ReplLoadOpType
operator|.
name|LOAD_NEW
return|;
block|}
if|if
condition|(
name|isDbAlreadyBootstrapped
argument_list|(
name|db
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"Bootstrap REPL LOAD is not allowed on Database: "
operator|+
name|dbName
operator|+
literal|" as it was already done."
argument_list|)
throw|;
block|}
if|if
condition|(
name|ReplUtils
operator|.
name|replCkptStatus
argument_list|(
name|dbName
argument_list|,
name|db
operator|.
name|getParameters
argument_list|()
argument_list|,
name|context
operator|.
name|dumpDirectory
argument_list|)
condition|)
block|{
return|return
name|ReplLoadOpType
operator|.
name|LOAD_SKIP
return|;
block|}
if|if
condition|(
name|isDbEmpty
argument_list|(
name|dbName
argument_list|)
condition|)
block|{
return|return
name|ReplLoadOpType
operator|.
name|LOAD_REPLACE
return|;
block|}
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
literal|"Bootstrap REPL LOAD is not allowed on Database: "
operator|+
name|dbName
operator|+
literal|" as it is not empty. One or more tables/functions exist."
argument_list|)
throw|;
block|}
specifier|private
name|boolean
name|isDbAlreadyBootstrapped
parameter_list|(
name|Database
name|db
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
init|=
name|db
operator|.
name|getParameters
argument_list|()
decl_stmt|;
return|return
operator|(
operator|(
name|props
operator|!=
literal|null
operator|)
operator|&&
name|props
operator|.
name|containsKey
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
operator|&&
operator|!
name|props
operator|.
name|get
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
operator|.
name|isEmpty
argument_list|()
operator|)
return|;
block|}
specifier|private
name|boolean
name|isDbEmpty
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|HiveException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|allTables
init|=
name|context
operator|.
name|hiveDb
operator|.
name|getAllTables
argument_list|(
name|dbName
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|allFunctions
init|=
name|context
operator|.
name|hiveDb
operator|.
name|getFunctions
argument_list|(
name|dbName
argument_list|,
literal|"*"
argument_list|)
decl_stmt|;
return|return
name|allTables
operator|.
name|isEmpty
argument_list|()
operator|&&
name|allFunctions
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|private
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|createDbTask
parameter_list|(
name|Database
name|dbObj
parameter_list|)
block|{
comment|// note that we do not set location - for repl load, we want that auto-created.
name|CreateDatabaseDesc
name|createDbDesc
init|=
operator|new
name|CreateDatabaseDesc
argument_list|(
name|dbObj
operator|.
name|getName
argument_list|()
argument_list|,
name|dbObj
operator|.
name|getDescription
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
name|updateDbProps
argument_list|(
name|dbObj
argument_list|,
name|context
operator|.
name|dumpDirectory
argument_list|,
operator|!
name|isTableLevelLoad
argument_list|)
argument_list|)
decl_stmt|;
comment|// If it exists, we want this to be an error condition. Repl Load is not intended to replace a
comment|// db.
comment|// TODO: we might revisit this in create-drop-recreate cases, needs some thinking on.
name|DDLWork2
name|work
init|=
operator|new
name|DDLWork2
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
name|createDbDesc
argument_list|)
decl_stmt|;
return|return
name|TaskFactory
operator|.
name|get
argument_list|(
name|work
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
return|;
block|}
specifier|private
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|alterDbTask
parameter_list|(
name|Database
name|dbObj
parameter_list|)
block|{
return|return
name|alterDbTask
argument_list|(
name|dbObj
operator|.
name|getName
argument_list|()
argument_list|,
name|updateDbProps
argument_list|(
name|dbObj
argument_list|,
name|context
operator|.
name|dumpDirectory
argument_list|,
operator|!
name|isTableLevelLoad
argument_list|)
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
return|;
block|}
specifier|private
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|setOwnerInfoTask
parameter_list|(
name|Database
name|dbObj
parameter_list|)
block|{
name|AlterDatabaseDesc
name|alterDbDesc
init|=
operator|new
name|AlterDatabaseDesc
argument_list|(
name|dbObj
operator|.
name|getName
argument_list|()
argument_list|,
operator|new
name|PrincipalDesc
argument_list|(
name|dbObj
operator|.
name|getOwnerName
argument_list|()
argument_list|,
name|dbObj
operator|.
name|getOwnerType
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|DDLWork2
name|work
init|=
operator|new
name|DDLWork2
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
name|alterDbDesc
argument_list|)
decl_stmt|;
return|return
name|TaskFactory
operator|.
name|get
argument_list|(
name|work
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|updateDbProps
parameter_list|(
name|Database
name|dbObj
parameter_list|,
name|String
name|dumpDirectory
parameter_list|,
name|boolean
name|needSetIncFlag
parameter_list|)
block|{
comment|/*     explicitly remove the setting of last.repl.id from the db object parameters as loadTask is going     to run multiple times and explicit logic is in place which prevents updates to tables when db level     last repl id is set and we create a AlterDatabaseTask at the end of processing a database.      */
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|dbObj
operator|.
name|getParameters
argument_list|()
argument_list|)
decl_stmt|;
name|parameters
operator|.
name|remove
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
expr_stmt|;
comment|// Add the checkpoint key to the Database binding it to current dump directory.
comment|// So, if retry using same dump, we shall skip Database object update.
name|parameters
operator|.
name|put
argument_list|(
name|ReplUtils
operator|.
name|REPL_CHECKPOINT_KEY
argument_list|,
name|dumpDirectory
argument_list|)
expr_stmt|;
if|if
condition|(
name|needSetIncFlag
condition|)
block|{
comment|// This flag will be set to false after first incremental load is done. This flag is used by repl copy task to
comment|// check if duplicate file check is required or not. This flag is used by compaction to check if compaction can be
comment|// done for this database or not. If compaction is done before first incremental then duplicate check will fail as
comment|// compaction may change the directory structure.
name|parameters
operator|.
name|put
argument_list|(
name|ReplUtils
operator|.
name|REPL_FIRST_INC_PENDING_FLAG
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
return|return
name|parameters
return|;
block|}
specifier|private
specifier|static
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|alterDbTask
parameter_list|(
name|String
name|dbName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
block|{
name|AlterDatabaseDesc
name|alterDbDesc
init|=
operator|new
name|AlterDatabaseDesc
argument_list|(
name|dbName
argument_list|,
name|props
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|DDLWork2
name|work
init|=
operator|new
name|DDLWork2
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|()
argument_list|,
name|alterDbDesc
argument_list|)
decl_stmt|;
return|return
name|TaskFactory
operator|.
name|get
argument_list|(
name|work
argument_list|,
name|hiveConf
argument_list|)
return|;
block|}
specifier|public
specifier|static
class|class
name|AlterDatabase
extends|extends
name|LoadDatabase
block|{
specifier|public
name|AlterDatabase
parameter_list|(
name|Context
name|context
parameter_list|,
name|DatabaseEvent
name|event
parameter_list|,
name|String
name|dbNameToLoadIn
parameter_list|,
name|TaskTracker
name|loadTaskTracker
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|event
argument_list|,
name|dbNameToLoadIn
argument_list|,
literal|null
argument_list|,
name|loadTaskTracker
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TaskTracker
name|tasks
parameter_list|()
throws|throws
name|SemanticException
block|{
name|Database
name|dbObj
init|=
name|readDbMetadata
argument_list|()
decl_stmt|;
name|tracker
operator|.
name|addTask
argument_list|(
name|alterDbTask
argument_list|(
name|dbObj
operator|.
name|getName
argument_list|()
argument_list|,
name|dbObj
operator|.
name|getParameters
argument_list|()
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|tracker
return|;
block|}
block|}
block|}
end_class

end_unit

