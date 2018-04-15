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
name|metastore
operator|.
name|utils
operator|.
name|MetaStoreUtils
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
name|ReplStateLogWork
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
name|AddDependencyToLeaves
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
name|bootstrap
operator|.
name|load
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
name|exec
operator|.
name|util
operator|.
name|DAGTraversal
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
name|lockmgr
operator|.
name|HiveTxnManager
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
name|session
operator|.
name|SessionState
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
name|IOException
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
name|ArrayList
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
specifier|private
specifier|final
name|HiveTxnManager
name|txnMgr
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
parameter_list|,
name|HiveTxnManager
name|txnMgr
parameter_list|)
throws|throws
name|SemanticException
throws|,
name|IOException
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
name|this
operator|.
name|txnMgr
operator|=
name|txnMgr
expr_stmt|;
block|}
specifier|private
name|void
name|createTableReplLogTask
parameter_list|(
name|String
name|tableName
parameter_list|,
name|TableType
name|tableType
parameter_list|)
throws|throws
name|SemanticException
block|{
name|ReplStateLogWork
name|replLogWork
init|=
operator|new
name|ReplStateLogWork
argument_list|(
name|replLogger
argument_list|,
name|tableName
argument_list|,
name|tableType
argument_list|)
decl_stmt|;
name|Task
argument_list|<
name|ReplStateLogWork
argument_list|>
name|replLogTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|replLogWork
argument_list|)
decl_stmt|;
name|DAGTraversal
operator|.
name|traverse
argument_list|(
name|tracker
operator|.
name|tasks
argument_list|()
argument_list|,
operator|new
name|AddDependencyToLeaves
argument_list|(
name|replLogTask
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|tracker
operator|.
name|tasks
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|tracker
operator|.
name|addTask
argument_list|(
name|replLogTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|DAGTraversal
operator|.
name|traverse
argument_list|(
name|tracker
operator|.
name|tasks
argument_list|()
argument_list|,
operator|new
name|AddDependencyToLeaves
argument_list|(
name|replLogTask
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
argument_list|>
name|visited
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|tracker
operator|.
name|updateTaskCount
argument_list|(
name|replLogTask
argument_list|,
name|visited
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|TaskTracker
name|tasks
parameter_list|()
throws|throws
name|SemanticException
block|{
comment|// Path being passed to us is a table dump location. We go ahead and load it in as needed.
comment|// If tblName is null, then we default to the table name specified in _metadata, which is good.
comment|// or are both specified, in which case, that's what we are intended to create the new table as.
try|try
block|{
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
name|ReplicationSpec
name|replicationSpec
init|=
name|event
operator|.
name|replicationSpec
argument_list|()
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
if|if
condition|(
name|table
operator|==
literal|null
condition|)
block|{
comment|// If table doesn't exist, allow creating a new one only if the database state is older than the update.
if|if
condition|(
operator|(
name|parentDb
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|replicationSpec
operator|.
name|allowReplacementInto
argument_list|(
name|parentDb
operator|.
name|getParameters
argument_list|()
argument_list|)
operator|)
condition|)
block|{
comment|// If the target table exists and is newer or same as current update based on repl.last.id, then just noop it.
return|return
name|tracker
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|replicationSpec
operator|.
name|allowReplacementInto
argument_list|(
name|table
operator|.
name|getParameters
argument_list|()
argument_list|)
condition|)
block|{
comment|// If the target table exists and is newer or same as current update based on repl.last.id, then just noop it.
return|return
name|tracker
return|;
block|}
block|}
if|if
condition|(
name|tableDesc
operator|.
name|getLocation
argument_list|()
operator|==
literal|null
condition|)
block|{
name|tableDesc
operator|.
name|setLocation
argument_list|(
name|location
argument_list|(
name|tableDesc
argument_list|,
name|parentDb
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/* Note: In the following section, Metadata-only import handling logic is      interleaved with regular repl-import logic. The rule of thumb being      followed here is that MD-only imports are essentially ALTERs. They do      not load data, and should not be "creating" any metadata - they should      be replacing instead. The only place it makes sense for a MD-only import      to create is in the case of a table that's been dropped and recreated,      or in the case of an unpartitioned table. In all other cases, it should      behave like a noop or a pure MD alter.   */
if|if
condition|(
name|table
operator|==
literal|null
condition|)
block|{
name|newTableTasks
argument_list|(
name|tableDesc
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|existingTableTasks
argument_list|(
name|tableDesc
argument_list|,
name|table
argument_list|,
name|replicationSpec
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|isPartitioned
argument_list|(
name|tableDesc
argument_list|)
condition|)
block|{
name|createTableReplLogTask
argument_list|(
name|tableDesc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|tableDesc
operator|.
name|tableType
argument_list|()
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
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|existingTableTasks
parameter_list|(
name|ImportTableDesc
name|tblDesc
parameter_list|,
name|Table
name|table
parameter_list|,
name|ReplicationSpec
name|replicationSpec
parameter_list|)
block|{
if|if
condition|(
operator|!
name|table
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"table non-partitioned"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|replicationSpec
operator|.
name|allowReplacementInto
argument_list|(
name|table
operator|.
name|getParameters
argument_list|()
argument_list|)
condition|)
block|{
return|return;
comment|// silently return, table is newer than our replacement.
block|}
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|alterTableTask
init|=
name|alterTableTask
argument_list|(
name|tblDesc
argument_list|,
name|replicationSpec
argument_list|)
decl_stmt|;
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
name|alterTableTask
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
name|event
operator|.
name|metadataPath
argument_list|()
argument_list|,
name|event
operator|.
name|metadataPath
argument_list|()
argument_list|)
decl_stmt|;
name|alterTableTask
operator|.
name|addDependentTask
argument_list|(
name|loadTableTask
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|addTask
argument_list|(
name|alterTableTask
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|newTableTasks
parameter_list|(
name|ImportTableDesc
name|tblDesc
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Table
name|table
decl_stmt|;
name|table
operator|=
operator|new
name|Table
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
expr_stmt|;
comment|// Either we're dropping and re-creating, or the table didn't exist, and we're creating.
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
name|event
operator|.
name|replicationSpec
argument_list|()
operator|.
name|isMetadataOnly
argument_list|()
condition|)
block|{
name|tracker
operator|.
name|addTask
argument_list|(
name|createTableTask
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
operator|!
name|isPartitioned
argument_list|(
name|tblDesc
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"adding dependent CopyWork/MoveWork for table"
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
name|event
operator|.
name|replicationSpec
argument_list|()
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
name|createTableTask
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
name|createTableTask
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|location
parameter_list|(
name|ImportTableDesc
name|tblDesc
parameter_list|,
name|Database
name|parentDb
parameter_list|)
throws|throws
name|MetaException
throws|,
name|SemanticException
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
return|return
name|context
operator|.
name|warehouse
operator|.
name|getDefaultTablePath
argument_list|(
name|parentDb
argument_list|,
name|tblDesc
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
else|else
block|{
name|Path
name|tablePath
init|=
operator|new
name|Path
argument_list|(
name|context
operator|.
name|warehouse
operator|.
name|getDefaultDatabasePath
argument_list|(
name|tblDesc
operator|.
name|getDatabaseName
argument_list|()
argument_list|)
argument_list|,
name|MetaStoreUtils
operator|.
name|encodeTableName
argument_list|(
name|tblDesc
operator|.
name|getTableName
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|context
operator|.
name|warehouse
operator|.
name|getDnsPath
argument_list|(
name|tablePath
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
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
decl_stmt|;
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
name|replicationSpec
operator|.
name|isReplace
argument_list|()
condition|?
name|LoadFileType
operator|.
name|REPLACE_ALL
else|:
name|LoadFileType
operator|.
name|OVERWRITE_EXISTING
argument_list|,
comment|//todo: what is the point of this?  If this is for replication, who would have opened a txn?
name|txnMgr
operator|.
name|getCurrentTxnId
argument_list|()
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
name|loadTableWork
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
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
extends|extends
name|Serializable
argument_list|>
name|alterTableTask
parameter_list|(
name|ImportTableDesc
name|tableDesc
parameter_list|,
name|ReplicationSpec
name|replicationSpec
parameter_list|)
block|{
name|tableDesc
operator|.
name|setReplaceMode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|replicationSpec
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|replicationSpec
operator|.
name|isInReplicationScope
argument_list|()
operator|)
condition|)
block|{
name|tableDesc
operator|.
name|setReplicationSpec
argument_list|(
name|replicationSpec
argument_list|)
expr_stmt|;
block|}
return|return
name|tableDesc
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
return|;
block|}
block|}
end_class

end_unit

