begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|common
operator|.
name|ValidWriteIds
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
name|metastore
operator|.
name|MetaStoreUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|org
operator|.
name|antlr
operator|.
name|runtime
operator|.
name|tree
operator|.
name|Tree
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
name|fs
operator|.
name|FileStatus
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
name|fs
operator|.
name|FileSystem
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
name|FileUtils
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
name|QueryState
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
name|hooks
operator|.
name|ReadEntity
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
name|InvalidTableException
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
name|Partition
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
name|PartitionIterable
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
name|plan
operator|.
name|CopyWork
import|;
end_import

begin_comment
comment|/**  * ExportSemanticAnalyzer.  *  */
end_comment

begin_class
specifier|public
class|class
name|ExportSemanticAnalyzer
extends|extends
name|BaseSemanticAnalyzer
block|{
specifier|private
name|ReplicationSpec
name|replicationSpec
decl_stmt|;
specifier|public
name|ExportSemanticAnalyzer
parameter_list|(
name|QueryState
name|queryState
parameter_list|)
throws|throws
name|SemanticException
block|{
name|super
argument_list|(
name|queryState
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|analyzeInternal
parameter_list|(
name|ASTNode
name|ast
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Tree
name|tableTree
init|=
name|ast
operator|.
name|getChild
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Tree
name|toTree
init|=
name|ast
operator|.
name|getChild
argument_list|(
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|ast
operator|.
name|getChildCount
argument_list|()
operator|>
literal|2
condition|)
block|{
name|replicationSpec
operator|=
operator|new
name|ReplicationSpec
argument_list|(
operator|(
name|ASTNode
operator|)
name|ast
operator|.
name|getChild
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|replicationSpec
operator|=
operator|new
name|ReplicationSpec
argument_list|()
expr_stmt|;
block|}
comment|// initialize export path
name|String
name|tmpPath
init|=
name|stripQuotes
argument_list|(
name|toTree
operator|.
name|getText
argument_list|()
argument_list|)
decl_stmt|;
name|URI
name|toURI
init|=
name|EximUtil
operator|.
name|getValidatedURI
argument_list|(
name|conf
argument_list|,
name|tmpPath
argument_list|)
decl_stmt|;
comment|// initialize source table/partition
name|TableSpec
name|ts
decl_stmt|;
try|try
block|{
name|ts
operator|=
operator|new
name|TableSpec
argument_list|(
name|db
argument_list|,
name|conf
argument_list|,
operator|(
name|ASTNode
operator|)
name|tableTree
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SemanticException
name|sme
parameter_list|)
block|{
if|if
condition|(
operator|(
name|replicationSpec
operator|.
name|isInReplicationScope
argument_list|()
operator|)
operator|&&
operator|(
operator|(
name|sme
operator|.
name|getCause
argument_list|()
operator|instanceof
name|InvalidTableException
operator|)
operator|||
operator|(
name|sme
operator|instanceof
name|Table
operator|.
name|ValidationFailureSemanticException
operator|)
operator|)
condition|)
block|{
comment|// If we're in replication scope, it's possible that we're running the export long after
comment|// the table was dropped, so the table not existing currently or being a different kind of
comment|// table is not an error - it simply means we should no-op, and let a future export
comment|// capture the appropriate state
name|ts
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|sme
throw|;
block|}
block|}
if|if
condition|(
name|ts
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|EximUtil
operator|.
name|validateTable
argument_list|(
name|ts
operator|.
name|tableHandle
argument_list|)
expr_stmt|;
if|if
condition|(
name|replicationSpec
operator|.
name|isInReplicationScope
argument_list|()
operator|&&
name|ts
operator|.
name|tableHandle
operator|.
name|isTemporary
argument_list|()
condition|)
block|{
comment|// No replication for temporary tables either
name|ts
operator|=
literal|null
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|SemanticException
name|e
parameter_list|)
block|{
comment|// table was a view, a non-native table or an offline table.
comment|// ignore for replication, error if not.
if|if
condition|(
name|replicationSpec
operator|.
name|isInReplicationScope
argument_list|()
condition|)
block|{
name|ts
operator|=
literal|null
expr_stmt|;
comment|// null out ts so we can't use it.
block|}
else|else
block|{
throw|throw
name|e
throw|;
block|}
block|}
block|}
try|try
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|toURI
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|Path
name|toPath
init|=
operator|new
name|Path
argument_list|(
name|toURI
operator|.
name|getScheme
argument_list|()
argument_list|,
name|toURI
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|toURI
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|FileStatus
name|tgt
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|toPath
argument_list|)
decl_stmt|;
comment|// target exists
if|if
condition|(
operator|!
name|tgt
operator|.
name|isDir
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_PATH
operator|.
name|getMsg
argument_list|(
name|ast
argument_list|,
literal|"Target is not a directory : "
operator|+
name|toURI
argument_list|)
argument_list|)
throw|;
block|}
else|else
block|{
name|FileStatus
index|[]
name|files
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|toPath
argument_list|,
name|FileUtils
operator|.
name|HIDDEN_FILES_PATH_FILTER
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|!=
literal|null
operator|&&
name|files
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_PATH
operator|.
name|getMsg
argument_list|(
name|ast
argument_list|,
literal|"Target is not an empty directory : "
operator|+
name|toURI
argument_list|)
argument_list|)
throw|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{       }
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|INVALID_PATH
operator|.
name|getMsg
argument_list|(
name|ast
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|PartitionIterable
name|partitions
init|=
literal|null
decl_stmt|;
try|try
block|{
name|replicationSpec
operator|.
name|setCurrentReplicationState
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|db
operator|.
name|getMSC
argument_list|()
operator|.
name|getCurrentNotificationEventId
argument_list|()
operator|.
name|getEventId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|(
name|ts
operator|!=
literal|null
operator|)
operator|&&
operator|(
name|ts
operator|.
name|tableHandle
operator|.
name|isPartitioned
argument_list|()
operator|)
condition|)
block|{
if|if
condition|(
name|ts
operator|.
name|specType
operator|==
name|TableSpec
operator|.
name|SpecType
operator|.
name|TABLE_ONLY
condition|)
block|{
comment|// TABLE-ONLY, fetch partitions if regular export, don't if metadata-only
if|if
condition|(
name|replicationSpec
operator|.
name|isMetadataOnly
argument_list|()
condition|)
block|{
name|partitions
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|partitions
operator|=
operator|new
name|PartitionIterable
argument_list|(
name|db
argument_list|,
name|ts
operator|.
name|tableHandle
argument_list|,
literal|null
argument_list|,
name|conf
operator|.
name|getIntVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_BATCH_RETRIEVE_MAX
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// PARTITIONS specified - partitions inside tableSpec
name|partitions
operator|=
operator|new
name|PartitionIterable
argument_list|(
name|ts
operator|.
name|partitions
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Either tableHandle isn't partitioned => null, or repl-export after ts becomes null => null.
comment|// or this is a noop-replication export, so we can skip looking at ptns.
name|partitions
operator|=
literal|null
expr_stmt|;
block|}
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|ctx
operator|.
name|getLocalTmpPath
argument_list|()
argument_list|,
literal|"_metadata"
argument_list|)
decl_stmt|;
name|EximUtil
operator|.
name|createExportDump
argument_list|(
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
argument_list|,
name|path
argument_list|,
operator|(
name|ts
operator|!=
literal|null
condition|?
name|ts
operator|.
name|tableHandle
else|:
literal|null
operator|)
argument_list|,
name|partitions
argument_list|,
name|replicationSpec
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
extends|extends
name|Serializable
argument_list|>
name|rTask
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
operator|new
name|CopyWork
argument_list|(
name|path
argument_list|,
operator|new
name|Path
argument_list|(
name|toURI
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|rTask
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"_metadata file written into "
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|" and then copied to "
operator|+
name|toURI
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
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
name|ErrorMsg
operator|.
name|IO_ERROR
operator|.
name|getMsg
argument_list|(
literal|"Exception while writing out the local file"
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|replicationSpec
operator|.
name|isMetadataOnly
argument_list|()
operator|||
operator|(
name|ts
operator|==
literal|null
operator|)
condition|)
return|return;
try|try
block|{
name|Path
name|parentPath
init|=
operator|new
name|Path
argument_list|(
name|toURI
argument_list|)
decl_stmt|;
name|boolean
name|isMmTable
init|=
name|MetaStoreUtils
operator|.
name|isInsertOnlyTable
argument_list|(
name|ts
operator|.
name|tableHandle
operator|.
name|getParameters
argument_list|()
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|LOG14535
operator|.
name|info
argument_list|(
literal|"Exporting table "
operator|+
name|ts
operator|.
name|tableName
operator|+
literal|" / "
operator|+
name|ts
operator|.
name|tableHandle
operator|.
name|getTableName
argument_list|()
operator|+
literal|": "
operator|+
name|isMmTable
argument_list|)
expr_stmt|;
name|int
name|lbLevels
init|=
name|isMmTable
operator|&&
name|ts
operator|.
name|tableHandle
operator|.
name|isStoredAsSubDirectories
argument_list|()
condition|?
name|ts
operator|.
name|tableHandle
operator|.
name|getSkewedColNames
argument_list|()
operator|.
name|size
argument_list|()
else|:
literal|0
decl_stmt|;
name|ValidWriteIds
name|ids
init|=
name|isMmTable
condition|?
name|db
operator|.
name|getValidWriteIdsForTable
argument_list|(
name|ts
operator|.
name|tableHandle
operator|.
name|getDbName
argument_list|()
argument_list|,
name|ts
operator|.
name|tableHandle
operator|.
name|getTableName
argument_list|()
argument_list|)
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|ts
operator|.
name|tableHandle
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
for|for
control|(
name|Partition
name|partition
range|:
name|partitions
control|)
block|{
name|Path
name|fromPath
init|=
name|partition
operator|.
name|getDataLocation
argument_list|()
decl_stmt|;
name|Path
name|toPartPath
init|=
operator|new
name|Path
argument_list|(
name|parentPath
argument_list|,
name|partition
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|CopyWork
name|cw
init|=
name|createCopyWork
argument_list|(
name|isMmTable
argument_list|,
name|lbLevels
argument_list|,
name|ids
argument_list|,
name|fromPath
argument_list|,
name|toPartPath
argument_list|)
decl_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|TaskFactory
operator|.
name|get
argument_list|(
name|cw
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|inputs
operator|.
name|add
argument_list|(
operator|new
name|ReadEntity
argument_list|(
name|partition
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|Path
name|fromPath
init|=
name|ts
operator|.
name|tableHandle
operator|.
name|getDataLocation
argument_list|()
decl_stmt|;
name|Path
name|toDataPath
init|=
operator|new
name|Path
argument_list|(
name|parentPath
argument_list|,
literal|"data"
argument_list|)
decl_stmt|;
name|CopyWork
name|cw
init|=
name|createCopyWork
argument_list|(
name|isMmTable
argument_list|,
name|lbLevels
argument_list|,
name|ids
argument_list|,
name|fromPath
argument_list|,
name|toDataPath
argument_list|)
decl_stmt|;
name|rootTasks
operator|.
name|add
argument_list|(
name|TaskFactory
operator|.
name|get
argument_list|(
name|cw
argument_list|,
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|inputs
operator|.
name|add
argument_list|(
operator|new
name|ReadEntity
argument_list|(
name|ts
operator|.
name|tableHandle
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|outputs
operator|.
name|add
argument_list|(
name|toWriteEntity
argument_list|(
name|parentPath
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
decl||
name|IOException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
specifier|private
name|CopyWork
name|createCopyWork
parameter_list|(
name|boolean
name|isMmTable
parameter_list|,
name|int
name|lbLevels
parameter_list|,
name|ValidWriteIds
name|ids
parameter_list|,
name|Path
name|fromPath
parameter_list|,
name|Path
name|toDataPath
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|validPaths
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|isMmTable
condition|)
block|{
name|fromPath
operator|=
name|fromPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|makeQualified
argument_list|(
name|fromPath
argument_list|)
expr_stmt|;
name|validPaths
operator|=
name|Utilities
operator|.
name|getValidMmDirectoriesFromTableOrPart
argument_list|(
name|fromPath
argument_list|,
name|conf
argument_list|,
name|ids
argument_list|,
name|lbLevels
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|validPaths
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|CopyWork
argument_list|(
name|fromPath
argument_list|,
name|toDataPath
argument_list|,
literal|false
argument_list|)
return|;
comment|// Not MM, or no need to skip anything.
block|}
else|else
block|{
return|return
name|createCopyWorkForValidPaths
argument_list|(
name|fromPath
argument_list|,
name|toDataPath
argument_list|,
name|validPaths
argument_list|)
return|;
block|}
block|}
specifier|private
name|CopyWork
name|createCopyWorkForValidPaths
parameter_list|(
name|Path
name|fromPath
parameter_list|,
name|Path
name|toPartPath
parameter_list|,
name|List
argument_list|<
name|Path
argument_list|>
name|validPaths
parameter_list|)
block|{
name|Path
index|[]
name|from
init|=
operator|new
name|Path
index|[
name|validPaths
operator|.
name|size
argument_list|()
index|]
decl_stmt|,
name|to
init|=
operator|new
name|Path
index|[
name|validPaths
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
name|String
name|fromPathStr
init|=
name|fromPath
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|fromPathStr
operator|.
name|endsWith
argument_list|(
name|Path
operator|.
name|SEPARATOR
argument_list|)
condition|)
block|{
name|fromPathStr
operator|+=
literal|"/"
expr_stmt|;
block|}
for|for
control|(
name|Path
name|validPath
range|:
name|validPaths
control|)
block|{
name|from
index|[
name|i
index|]
operator|=
name|validPath
expr_stmt|;
comment|// TODO: assumes the results are already qualified.
name|to
index|[
name|i
index|]
operator|=
operator|new
name|Path
argument_list|(
name|toPartPath
argument_list|,
name|validPath
operator|.
name|toString
argument_list|()
operator|.
name|substring
argument_list|(
name|fromPathStr
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Utilities
operator|.
name|LOG14535
operator|.
name|info
argument_list|(
literal|"Will copy "
operator|+
name|from
index|[
name|i
index|]
operator|+
literal|" to "
operator|+
name|to
index|[
name|i
index|]
operator|+
literal|" based on dest "
operator|+
name|toPartPath
operator|+
literal|", from "
operator|+
name|fromPathStr
operator|+
literal|", subpath "
operator|+
name|validPath
argument_list|)
expr_stmt|;
operator|++
name|i
expr_stmt|;
block|}
return|return
operator|new
name|CopyWork
argument_list|(
name|from
argument_list|,
name|to
argument_list|)
return|;
block|}
block|}
end_class

end_unit

