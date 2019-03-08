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
operator|.
name|table
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
name|fs
operator|.
name|Path
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
name|ValidReaderWriteIdList
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
name|ValidWriteIdList
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
name|TableType
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
name|Warehouse
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
name|ql
operator|.
name|ErrorMsg
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
name|ReplCopyTask
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
name|Utilities
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
name|ReplExternalTables
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
name|TableEvent
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
name|bootstrap
operator|.
name|load
operator|.
name|util
operator|.
name|PathUtils
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
name|io
operator|.
name|AcidUtils
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
name|metadata
operator|.
name|Table
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
name|EximUtil
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
name|ImportSemanticAnalyzer
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
name|ReplLogger
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
name|DropTableDesc
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
name|ImportTableDesc
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
name|LoadMultiFilesDesc
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
name|LoadTableDesc
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
name|LoadTableDesc
operator|.
name|LoadFileType
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
name|MoveWork
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
name|ReplTxnWork
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|BitSet
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
name|TreeMap
import|;
end_import

begin_import
import|import static
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
operator|.
name|ConfVars
operator|.
name|REPL_ENABLE_MOVE_OPTIMIZATION
import|;
end_import

begin_import
import|import static
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
name|ImportSemanticAnalyzer
operator|.
name|isPartitioned
import|;
end_import

begin_class
specifier|public
class|class
name|LoadTable
block|{
specifier|private
specifier|final
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|LoadTable
operator|.
name|class
argument_list|)
decl_stmt|;
comment|//  private final Helper helper;
specifier|private
specifier|final
name|Context
name|context
decl_stmt|;
specifier|private
specifier|final
name|ReplLogger
name|replLogger
decl_stmt|;
specifier|private
specifier|final
name|TableContext
name|tableContext
decl_stmt|;
specifier|private
specifier|final
name|TaskTracker
name|tracker
decl_stmt|;
specifier|private
specifier|final
name|TableEvent
name|event
decl_stmt|;
specifier|public
name|LoadTable
parameter_list|(
name|TableEvent
name|event
parameter_list|,
name|Context
name|context
parameter_list|,
name|ReplLogger
name|replLogger
parameter_list|,
name|TableContext
name|tableContext
parameter_list|,
name|TaskTracker
name|limiter
parameter_list|)
block|{
name|this
operator|.
name|event
operator|=
name|event
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|replLogger
operator|=
name|replLogger
expr_stmt|;
name|this
operator|.
name|tableContext
operator|=
name|tableContext
expr_stmt|;
name|this
operator|.
name|tracker
operator|=
operator|new
name|TaskTracker
argument_list|(
name|limiter
argument_list|)
expr_stmt|;
block|}
specifier|public
name|TaskTracker
name|tasks
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Path being passed to us is a table dump location. We go ahead and load it in as needed.
comment|// If tblName is null, then we default to the table name specified in _metadata, which is good.
comment|// or are both specified, in which case, that's what we are intended to create the new table as.
if|if
condition|(
name|event
operator|.
name|shouldNotReplicate
argument_list|()
condition|)
block|{
return|return
name|tracker
return|;
block|}
name|String
name|dbName
init|=
name|tableContext
operator|.
name|dbNameToLoadIn
decl_stmt|;
comment|//this can never be null or empty;
comment|// Create table associated with the import
comment|// Executed if relevant, and used to contain all the other details about the table if not.
name|ImportTableDesc
name|tableDesc
init|=
name|tableContext
operator|.
name|overrideProperties
argument_list|(
name|event
operator|.
name|tableDesc
argument_list|(
name|dbName
argument_list|)
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|ImportSemanticAnalyzer
operator|.
name|tableIfExists
argument_list|(
name|tableDesc
argument_list|,
name|context
operator|.
name|hiveDb
argument_list|)
decl_stmt|;
comment|// Normally, on import, trying to create a table or a partition in a db that does not yet exist
comment|// is a error condition. However, in the case of a REPL LOAD, it is possible that we are trying
comment|// to create tasks to create a table inside a db that as-of-now does not exist, but there is
comment|// a precursor Task waiting that will create it before this is encountered. Thus, we instantiate
comment|// defaults and do not error out in that case.
comment|// the above will change now since we are going to split replication load in multiple execution
comment|// tasks and hence we could have created the database earlier in which case the waitOnPrecursor will
comment|// be false and hence if db Not found we should error out.
name|Database
name|parentDb
init|=
name|context
operator|.
name|hiveDb
operator|.
name|getDatabase
argument_list|(
name|tableDesc
operator|.
name|getDatabaseName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentDb
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|tableContext
operator|.
name|waitOnPrecursor
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|DATABASE_NOT_EXISTS
operator|.
name|getMsg
argument_list|(
name|tableDesc
operator|.
name|getDatabaseName
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
block|}
name|Task
argument_list|<
name|?
argument_list|>
name|tblRootTask
init|=
literal|null
decl_stmt|;
name|ReplLoadOpType
name|loadTblType
init|=
name|getLoadTableType
argument_list|(
name|table
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|loadTblType
condition|)
block|{
case|case
name|LOAD_NEW
case|:
break|break;
case|case
name|LOAD_REPLACE
case|:
name|tblRootTask
operator|=
name|dropTableTask
argument_list|(
name|table
argument_list|)
expr_stmt|;
break|break;
case|case
name|LOAD_SKIP
case|:
return|return
name|tracker
return|;
default|default:
break|break;
block|}
name|TableLocationTuple
name|tableLocationTuple
init|=
name|tableLocation
argument_list|(
name|tableDesc
argument_list|,
name|parentDb
argument_list|,
name|tableContext
argument_list|,
name|context
argument_list|)
decl_stmt|;
name|tableDesc
operator|.
name|setLocation
argument_list|(
name|tableLocationTuple
operator|.
name|location
argument_list|)
expr_stmt|;
comment|/* Note: In the following section, Metadata-only import handling logic is        interleaved with regular repl-import logic. The rule of thumb being        followed here is that MD-only imports are essentially ALTERs. They do        not load data, and should not be "creating" any metadata - they should        be replacing instead. The only place it makes sense for a MD-only import        to create is in the case of a table that's been dropped and recreated,        or in the case of an unpartitioned table. In all other cases, it should        behave like a noop or a pure MD alter.     */
name|newTableTasks
argument_list|(
name|tableDesc
argument_list|,
name|tblRootTask
argument_list|,
name|tableLocationTuple
argument_list|)
expr_stmt|;
comment|// Set Checkpoint task as dependant to create table task. So, if same dump is retried for
comment|// bootstrap, we skip current table update.
name|Task
argument_list|<
name|?
argument_list|>
name|ckptTask
init|=
name|ReplUtils
operator|.
name|getTableCheckpointTask
argument_list|(
name|tableDesc
argument_list|,
literal|null
argument_list|,
name|context
operator|.
name|dumpDirectory
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|isPartitioned
argument_list|(
name|tableDesc
argument_list|)
condition|)
block|{
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|replLogTask
init|=
name|ReplUtils
operator|.
name|getTableReplLogTask
argument_list|(
name|tableDesc
argument_list|,
name|replLogger
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|ckptTask
operator|.
name|addDependentTask
argument_list|(
name|replLogTask
argument_list|)
expr_stmt|;
block|}
name|tracker
operator|.
name|addDependentTask
argument_list|(
name|ckptTask
argument_list|)
expr_stmt|;
return|return
name|tracker
return|;
block|}
specifier|private
name|ReplLoadOpType
name|getLoadTableType
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|InvalidOperationException
throws|,
name|HiveException
block|{
if|if
condition|(
name|table
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
name|ReplUtils
operator|.
name|replCkptStatus
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
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
return|return
name|ReplLoadOpType
operator|.
name|LOAD_REPLACE
return|;
block|}
specifier|private
name|void
name|newTableTasks
parameter_list|(
name|ImportTableDesc
name|tblDesc
parameter_list|,
name|Task
argument_list|<
name|?
argument_list|>
name|tblRootTask
parameter_list|,
name|TableLocationTuple
name|tuple
parameter_list|)
throws|throws
name|Exception
block|{
name|Table
name|table
init|=
name|tblDesc
operator|.
name|toTable
argument_list|(
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|ReplicationSpec
name|replicationSpec
init|=
name|event
operator|.
name|replicationSpec
argument_list|()
decl_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|createTableTask
init|=
name|tblDesc
operator|.
name|getCreateTableTask
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
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
if|if
condition|(
name|tblRootTask
operator|==
literal|null
condition|)
block|{
name|tblRootTask
operator|=
name|createTableTask
expr_stmt|;
block|}
else|else
block|{
name|tblRootTask
operator|.
name|addDependentTask
argument_list|(
name|createTableTask
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|replicationSpec
operator|.
name|isMetadataOnly
argument_list|()
condition|)
block|{
name|tracker
operator|.
name|addTask
argument_list|(
name|tblRootTask
argument_list|)
expr_stmt|;
return|return;
block|}
name|Task
argument_list|<
name|?
argument_list|>
name|parentTask
init|=
name|createTableTask
decl_stmt|;
if|if
condition|(
name|replicationSpec
operator|.
name|isTransactionalTableDump
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|partNames
init|=
name|isPartitioned
argument_list|(
name|tblDesc
argument_list|)
condition|?
name|event
operator|.
name|partitions
argument_list|(
name|tblDesc
argument_list|)
else|:
literal|null
decl_stmt|;
name|ReplTxnWork
name|replTxnWork
init|=
operator|new
name|ReplTxnWork
argument_list|(
name|tblDesc
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|tblDesc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|partNames
argument_list|,
name|replicationSpec
operator|.
name|getValidWriteIdList
argument_list|()
argument_list|,
name|ReplTxnWork
operator|.
name|OperationType
operator|.
name|REPL_WRITEID_STATE
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|replTxnTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|replTxnWork
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|parentTask
operator|.
name|addDependentTask
argument_list|(
name|replTxnTask
argument_list|)
expr_stmt|;
name|parentTask
operator|=
name|replTxnTask
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|replicationSpec
operator|.
name|isMigratingToTxnTable
argument_list|()
condition|)
block|{
comment|// Non-transactional table is converted to transactional table.
comment|// The write-id 1 is used to copy data for the given table and also no writes are aborted.
name|ValidWriteIdList
name|validWriteIdList
init|=
operator|new
name|ValidReaderWriteIdList
argument_list|(
name|AcidUtils
operator|.
name|getFullTableName
argument_list|(
name|tblDesc
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|tblDesc
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|,
operator|new
name|long
index|[
literal|0
index|]
argument_list|,
operator|new
name|BitSet
argument_list|()
argument_list|,
name|ReplUtils
operator|.
name|REPL_BOOTSTRAP_MIGRATION_BASE_WRITE_ID
argument_list|)
decl_stmt|;
name|ReplTxnWork
name|replTxnWork
init|=
operator|new
name|ReplTxnWork
argument_list|(
name|tblDesc
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|tblDesc
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
name|validWriteIdList
operator|.
name|writeToString
argument_list|()
argument_list|,
name|ReplTxnWork
operator|.
name|OperationType
operator|.
name|REPL_WRITEID_STATE
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|replTxnTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|replTxnWork
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|parentTask
operator|.
name|addDependentTask
argument_list|(
name|replTxnTask
argument_list|)
expr_stmt|;
name|parentTask
operator|=
name|replTxnTask
expr_stmt|;
block|}
name|boolean
name|shouldCreateLoadTableTask
init|=
operator|(
operator|!
name|isPartitioned
argument_list|(
name|tblDesc
argument_list|)
operator|&&
operator|!
name|TableType
operator|.
name|EXTERNAL_TABLE
operator|.
name|equals
argument_list|(
name|table
operator|.
name|getTableType
argument_list|()
argument_list|)
operator|)
operator|||
name|tuple
operator|.
name|isConvertedFromManagedToExternal
decl_stmt|;
if|if
condition|(
name|shouldCreateLoadTableTask
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"adding dependent ReplTxnTask/CopyWork/MoveWork for table"
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|loadTableTask
init|=
name|loadTableTask
argument_list|(
name|table
argument_list|,
name|replicationSpec
argument_list|,
operator|new
name|Path
argument_list|(
name|tblDesc
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|,
name|event
operator|.
name|metadataPath
argument_list|()
argument_list|)
decl_stmt|;
name|parentTask
operator|.
name|addDependentTask
argument_list|(
name|loadTableTask
argument_list|)
expr_stmt|;
block|}
name|tracker
operator|.
name|addTask
argument_list|(
name|tblRootTask
argument_list|)
expr_stmt|;
block|}
specifier|static
class|class
name|TableLocationTuple
block|{
specifier|final
name|String
name|location
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isConvertedFromManagedToExternal
decl_stmt|;
name|TableLocationTuple
parameter_list|(
name|String
name|location
parameter_list|,
name|boolean
name|isConvertedFromManagedToExternal
parameter_list|)
block|{
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
name|this
operator|.
name|isConvertedFromManagedToExternal
operator|=
name|isConvertedFromManagedToExternal
expr_stmt|;
block|}
block|}
specifier|static
name|TableLocationTuple
name|tableLocation
parameter_list|(
name|ImportTableDesc
name|tblDesc
parameter_list|,
name|Database
name|parentDb
parameter_list|,
name|TableContext
name|tableContext
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|MetaException
throws|,
name|SemanticException
block|{
name|Warehouse
name|wh
init|=
name|context
operator|.
name|warehouse
decl_stmt|;
name|Path
name|defaultTablePath
decl_stmt|;
if|if
condition|(
name|parentDb
operator|==
literal|null
condition|)
block|{
name|defaultTablePath
operator|=
name|wh
operator|.
name|getDefaultTablePath
argument_list|(
name|tblDesc
operator|.
name|getDatabaseName
argument_list|()
argument_list|,
name|tblDesc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|tblDesc
operator|.
name|isExternal
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|defaultTablePath
operator|=
name|wh
operator|.
name|getDefaultTablePath
argument_list|(
name|parentDb
argument_list|,
name|tblDesc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|tblDesc
operator|.
name|isExternal
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// dont use TableType.EXTERNAL_TABLE.equals(tblDesc.tableType()) since this comes in as managed always for tables.
if|if
condition|(
name|tblDesc
operator|.
name|isExternal
argument_list|()
condition|)
block|{
if|if
condition|(
name|tblDesc
operator|.
name|getLocation
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// this is the use case when the table got converted to external table as part of migration
comment|// related rules to be applied to replicated tables across different versions of hive.
return|return
operator|new
name|TableLocationTuple
argument_list|(
name|wh
operator|.
name|getDnsPath
argument_list|(
name|defaultTablePath
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
literal|true
argument_list|)
return|;
block|}
name|String
name|currentLocation
init|=
operator|new
name|Path
argument_list|(
name|tblDesc
operator|.
name|getLocation
argument_list|()
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|String
name|newLocation
init|=
name|ReplExternalTables
operator|.
name|externalTableLocation
argument_list|(
name|context
operator|.
name|hiveConf
argument_list|,
name|currentLocation
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"external table {} data location is: {}"
argument_list|,
name|tblDesc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|newLocation
argument_list|)
expr_stmt|;
return|return
operator|new
name|TableLocationTuple
argument_list|(
name|newLocation
argument_list|,
literal|false
argument_list|)
return|;
block|}
name|Path
name|path
init|=
name|tableContext
operator|.
name|waitOnPrecursor
argument_list|()
condition|?
name|wh
operator|.
name|getDnsPath
argument_list|(
name|defaultTablePath
argument_list|)
else|:
name|wh
operator|.
name|getDefaultTablePath
argument_list|(
name|parentDb
argument_list|,
name|tblDesc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|tblDesc
operator|.
name|isExternal
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|TableLocationTuple
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
literal|false
argument_list|)
return|;
block|}
specifier|private
name|Task
argument_list|<
name|?
argument_list|>
name|loadTableTask
parameter_list|(
name|Table
name|table
parameter_list|,
name|ReplicationSpec
name|replicationSpec
parameter_list|,
name|Path
name|tgtPath
parameter_list|,
name|Path
name|fromURI
parameter_list|)
block|{
name|Path
name|dataPath
init|=
operator|new
name|Path
argument_list|(
name|fromURI
argument_list|,
name|EximUtil
operator|.
name|DATA_PATH_NAME
argument_list|)
decl_stmt|;
name|Path
name|tmpPath
init|=
name|tgtPath
decl_stmt|;
comment|// if move optimization is enabled, copy the files directly to the target path. No need to create the staging dir.
name|LoadFileType
name|loadFileType
decl_stmt|;
if|if
condition|(
name|replicationSpec
operator|.
name|isInReplicationScope
argument_list|()
operator|&&
name|context
operator|.
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|REPL_ENABLE_MOVE_OPTIMIZATION
argument_list|)
condition|)
block|{
name|loadFileType
operator|=
name|LoadFileType
operator|.
name|IGNORE
expr_stmt|;
if|if
condition|(
name|event
operator|.
name|replicationSpec
argument_list|()
operator|.
name|isMigratingToTxnTable
argument_list|()
condition|)
block|{
comment|// Migrating to transactional tables in bootstrap load phase.
comment|// It is enough to copy all the original files under base_1 dir and so write-id is hardcoded to 1.
comment|// ReplTxnTask added earlier in the DAG ensure that the write-id=1 is made valid in HMS metadata.
name|tmpPath
operator|=
operator|new
name|Path
argument_list|(
name|tmpPath
argument_list|,
name|AcidUtils
operator|.
name|baseDir
argument_list|(
name|ReplUtils
operator|.
name|REPL_BOOTSTRAP_MIGRATION_BASE_WRITE_ID
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|loadFileType
operator|=
operator|(
name|replicationSpec
operator|.
name|isReplace
argument_list|()
operator|||
name|replicationSpec
operator|.
name|isMigratingToTxnTable
argument_list|()
operator|)
condition|?
name|LoadFileType
operator|.
name|REPLACE_ALL
else|:
name|LoadFileType
operator|.
name|OVERWRITE_EXISTING
expr_stmt|;
name|tmpPath
operator|=
name|PathUtils
operator|.
name|getExternalTmpPath
argument_list|(
name|tgtPath
argument_list|,
name|context
operator|.
name|pathInfo
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"adding dependent CopyWork/AddPart/MoveWork for table "
operator|+
name|table
operator|.
name|getCompleteName
argument_list|()
operator|+
literal|" with source location: "
operator|+
name|dataPath
operator|.
name|toString
argument_list|()
operator|+
literal|" and target location "
operator|+
name|tgtPath
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|copyTask
init|=
name|ReplCopyTask
operator|.
name|getLoadCopyTask
argument_list|(
name|replicationSpec
argument_list|,
name|dataPath
argument_list|,
name|tmpPath
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|MoveWork
name|moveWork
init|=
operator|new
name|MoveWork
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
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|AcidUtils
operator|.
name|isTransactionalTable
argument_list|(
name|table
argument_list|)
condition|)
block|{
if|if
condition|(
name|replicationSpec
operator|.
name|isMigratingToTxnTable
argument_list|()
condition|)
block|{
comment|// Write-id is hardcoded to 1 so that for migration, we just move all original files under base_1 dir.
comment|// ReplTxnTask added earlier in the DAG ensure that the write-id is made valid in HMS metadata.
name|LoadTableDesc
name|loadTableWork
init|=
operator|new
name|LoadTableDesc
argument_list|(
name|tmpPath
argument_list|,
name|Utilities
operator|.
name|getTableDesc
argument_list|(
name|table
argument_list|)
argument_list|,
operator|new
name|TreeMap
argument_list|<>
argument_list|()
argument_list|,
name|loadFileType
argument_list|,
name|ReplUtils
operator|.
name|REPL_BOOTSTRAP_MIGRATION_BASE_WRITE_ID
argument_list|)
decl_stmt|;
name|loadTableWork
operator|.
name|setStmtId
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// Need to set insertOverwrite so base_1 is created instead of delta_1_1_0.
name|loadTableWork
operator|.
name|setInsertOverwrite
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|moveWork
operator|.
name|setLoadTableWork
argument_list|(
name|loadTableWork
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LoadMultiFilesDesc
name|loadFilesWork
init|=
operator|new
name|LoadMultiFilesDesc
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|tmpPath
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|tgtPath
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|moveWork
operator|.
name|setMultiFilesDesc
argument_list|(
name|loadFilesWork
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|LoadTableDesc
name|loadTableWork
init|=
operator|new
name|LoadTableDesc
argument_list|(
name|tmpPath
argument_list|,
name|Utilities
operator|.
name|getTableDesc
argument_list|(
name|table
argument_list|)
argument_list|,
operator|new
name|TreeMap
argument_list|<>
argument_list|()
argument_list|,
name|loadFileType
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
name|moveWork
operator|.
name|setLoadTableWork
argument_list|(
name|loadTableWork
argument_list|)
expr_stmt|;
block|}
name|moveWork
operator|.
name|setIsInReplicationScope
argument_list|(
name|replicationSpec
operator|.
name|isInReplicationScope
argument_list|()
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|loadTableTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|moveWork
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
decl_stmt|;
name|copyTask
operator|.
name|addDependentTask
argument_list|(
name|loadTableTask
argument_list|)
expr_stmt|;
return|return
name|copyTask
return|;
block|}
specifier|private
name|Task
argument_list|<
name|?
argument_list|>
name|dropTableTask
parameter_list|(
name|Table
name|table
parameter_list|)
block|{
assert|assert
operator|(
name|table
operator|!=
literal|null
operator|)
assert|;
name|DropTableDesc
name|dropTblDesc
init|=
operator|new
name|DropTableDesc
argument_list|(
name|table
operator|.
name|getFullyQualifiedName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableType
argument_list|()
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
name|event
operator|.
name|replicationSpec
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|DDLWork
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
name|dropTblDesc
argument_list|)
argument_list|,
name|context
operator|.
name|hiveConf
argument_list|)
return|;
block|}
block|}
end_class

end_unit

