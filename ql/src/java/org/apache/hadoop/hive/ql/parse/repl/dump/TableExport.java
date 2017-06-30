begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*   Licensed to the Apache Software Foundation (ASF) under one   or more contributor license agreements.  See the NOTICE file   distributed with this work for additional information   regarding copyright ownership.  The ASF licenses this file   to you under the Apache License, Version 2.0 (the   "License"); you may not use this file except in compliance   with the License.  You may obtain a copy of the License at        http://www.apache.org/licenses/LICENSE-2.0    Unless required by applicable law or agreed to in writing, software   distributed under the License is distributed on an "AS IS" BASIS,   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   See the License for the specific language governing permissions and   limitations under the License.  */
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
name|dump
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
name|hooks
operator|.
name|WriteEntity
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
name|Hive
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
name|parse
operator|.
name|ASTNode
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
name|BaseSemanticAnalyzer
operator|.
name|TableSpec
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
name|io
operator|.
name|FileOperations
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
name|net
operator|.
name|URI
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
name|Set
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
name|BaseSemanticAnalyzer
operator|.
name|toWriteEntity
import|;
end_import

begin_class
specifier|public
class|class
name|TableExport
block|{
specifier|private
name|TableSpec
name|tableSpec
decl_stmt|;
specifier|private
specifier|final
name|ReplicationSpec
name|replicationSpec
decl_stmt|;
specifier|private
specifier|final
name|Hive
name|db
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|final
name|Logger
name|logger
decl_stmt|;
specifier|private
specifier|final
name|Paths
name|paths
decl_stmt|;
specifier|private
specifier|final
name|AuthEntities
name|authEntities
init|=
operator|new
name|AuthEntities
argument_list|()
decl_stmt|;
specifier|public
name|TableExport
parameter_list|(
name|Paths
name|paths
parameter_list|,
name|TableSpec
name|tableSpec
parameter_list|,
name|ReplicationSpec
name|replicationSpec
parameter_list|,
name|Hive
name|db
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|Logger
name|logger
parameter_list|)
throws|throws
name|SemanticException
block|{
name|this
operator|.
name|tableSpec
operator|=
operator|(
name|tableSpec
operator|!=
literal|null
operator|&&
name|tableSpec
operator|.
name|tableHandle
operator|.
name|isTemporary
argument_list|()
operator|&&
operator|!
name|replicationSpec
operator|.
name|isInReplicationScope
argument_list|()
operator|)
condition|?
literal|null
else|:
name|tableSpec
expr_stmt|;
name|this
operator|.
name|replicationSpec
operator|=
name|replicationSpec
expr_stmt|;
name|this
operator|.
name|db
operator|=
name|db
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
name|this
operator|.
name|paths
operator|=
name|paths
expr_stmt|;
block|}
specifier|public
name|AuthEntities
name|run
parameter_list|()
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|tableSpec
operator|==
literal|null
condition|)
block|{
name|writeMetaData
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|shouldExport
argument_list|()
condition|)
block|{
if|if
condition|(
name|tableSpec
operator|.
name|tableHandle
operator|.
name|isView
argument_list|()
condition|)
block|{
name|replicationSpec
operator|.
name|setIsMetadataOnly
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|PartitionIterable
name|withPartitions
init|=
name|partitions
argument_list|()
decl_stmt|;
name|writeMetaData
argument_list|(
name|withPartitions
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|replicationSpec
operator|.
name|isMetadataOnly
argument_list|()
condition|)
block|{
name|writeData
argument_list|(
name|withPartitions
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|authEntities
return|;
block|}
specifier|private
name|PartitionIterable
name|partitions
parameter_list|()
throws|throws
name|SemanticException
block|{
try|try
block|{
name|long
name|currentEventId
init|=
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
decl_stmt|;
name|replicationSpec
operator|.
name|setCurrentReplicationState
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|currentEventId
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|tableSpec
operator|.
name|tableHandle
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
if|if
condition|(
name|tableSpec
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
return|return
literal|null
return|;
block|}
else|else
block|{
return|return
operator|new
name|PartitionIterable
argument_list|(
name|db
argument_list|,
name|tableSpec
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
return|;
block|}
block|}
else|else
block|{
comment|// PARTITIONS specified - partitions inside tableSpec
return|return
operator|new
name|PartitionIterable
argument_list|(
name|tableSpec
operator|.
name|partitions
argument_list|)
return|;
block|}
block|}
else|else
block|{
comment|// Either tableHandle isn't partitioned => null, or repl-export after ts becomes null => null.
comment|// or this is a noop-replication export, so we can skip looking at ptns.
return|return
literal|null
return|;
block|}
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
literal|"Error when identifying partitions"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|writeMetaData
parameter_list|(
name|PartitionIterable
name|partitions
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
name|EximUtil
operator|.
name|createExportDump
argument_list|(
name|paths
operator|.
name|exportFileSystem
argument_list|,
name|paths
operator|.
name|metaDataExportFile
argument_list|()
argument_list|,
name|tableSpec
operator|==
literal|null
condition|?
literal|null
else|:
name|tableSpec
operator|.
name|tableHandle
argument_list|,
name|partitions
argument_list|,
name|replicationSpec
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"_metadata file written into "
operator|+
name|paths
operator|.
name|metaDataExportFile
argument_list|()
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
comment|// the path used above should not be used on a second try as each dump request is written to a unique location.
comment|// however if we want to keep the dump location clean we might want to delete the paths
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
block|}
specifier|private
name|void
name|writeData
parameter_list|(
name|PartitionIterable
name|partitions
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
if|if
condition|(
name|tableSpec
operator|.
name|tableHandle
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
if|if
condition|(
name|partitions
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"partitions cannot be null for partitionTable :"
operator|+
name|tableSpec
operator|.
name|tableName
argument_list|)
throw|;
block|}
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
comment|// this the data copy
name|Path
name|rootDataDumpDir
init|=
name|paths
operator|.
name|partitionExportDir
argument_list|(
name|partition
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
operator|new
name|FileOperations
argument_list|(
name|fromPath
argument_list|,
name|rootDataDumpDir
argument_list|,
name|conf
argument_list|)
operator|.
name|export
argument_list|(
name|replicationSpec
argument_list|)
expr_stmt|;
name|authEntities
operator|.
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
name|tableSpec
operator|.
name|tableHandle
operator|.
name|getDataLocation
argument_list|()
decl_stmt|;
comment|//this is the data copy
operator|new
name|FileOperations
argument_list|(
name|fromPath
argument_list|,
name|paths
operator|.
name|dataExportDir
argument_list|()
argument_list|,
name|conf
argument_list|)
operator|.
name|export
argument_list|(
name|replicationSpec
argument_list|)
expr_stmt|;
name|authEntities
operator|.
name|inputs
operator|.
name|add
argument_list|(
operator|new
name|ReadEntity
argument_list|(
name|tableSpec
operator|.
name|tableHandle
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|authEntities
operator|.
name|outputs
operator|.
name|add
argument_list|(
name|toWriteEntity
argument_list|(
name|paths
operator|.
name|exportRootDir
argument_list|,
name|conf
argument_list|)
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
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|boolean
name|shouldExport
parameter_list|()
throws|throws
name|SemanticException
block|{
if|if
condition|(
name|replicationSpec
operator|.
name|isInReplicationScope
argument_list|()
condition|)
block|{
return|return
operator|!
operator|(
name|tableSpec
operator|.
name|tableHandle
operator|.
name|isTemporary
argument_list|()
operator|||
name|tableSpec
operator|.
name|tableHandle
operator|.
name|isNonNative
argument_list|()
operator|)
return|;
block|}
elseif|else
if|if
condition|(
name|tableSpec
operator|.
name|tableHandle
operator|.
name|isNonNative
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SemanticException
argument_list|(
name|ErrorMsg
operator|.
name|EXIM_FOR_NON_NATIVE
operator|.
name|getMsg
argument_list|()
argument_list|)
throw|;
block|}
return|return
literal|true
return|;
block|}
comment|/**    * this class is responsible for giving various paths to be used during export along with root export    * directory creation.    */
specifier|public
specifier|static
class|class
name|Paths
block|{
specifier|private
specifier|final
name|ASTNode
name|ast
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|public
specifier|final
name|Path
name|exportRootDir
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|exportFileSystem
decl_stmt|;
specifier|public
name|Paths
parameter_list|(
name|ASTNode
name|ast
parameter_list|,
name|Path
name|dbRoot
parameter_list|,
name|String
name|tblName
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|SemanticException
block|{
name|this
operator|.
name|ast
operator|=
name|ast
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|Path
name|tableRoot
init|=
operator|new
name|Path
argument_list|(
name|dbRoot
argument_list|,
name|tblName
argument_list|)
decl_stmt|;
name|URI
name|exportRootDir
init|=
name|EximUtil
operator|.
name|getValidatedURI
argument_list|(
name|conf
argument_list|,
name|tableRoot
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|validateTargetDir
argument_list|(
name|exportRootDir
argument_list|)
expr_stmt|;
name|this
operator|.
name|exportRootDir
operator|=
operator|new
name|Path
argument_list|(
name|exportRootDir
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|exportFileSystem
operator|=
name|this
operator|.
name|exportRootDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|exportFileSystem
operator|.
name|exists
argument_list|(
name|this
operator|.
name|exportRootDir
argument_list|)
condition|)
block|{
name|exportFileSystem
operator|.
name|mkdirs
argument_list|(
name|this
operator|.
name|exportRootDir
argument_list|)
expr_stmt|;
block|}
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
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|Paths
parameter_list|(
name|ASTNode
name|ast
parameter_list|,
name|String
name|path
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|SemanticException
block|{
name|this
operator|.
name|ast
operator|=
name|ast
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|exportRootDir
operator|=
operator|new
name|Path
argument_list|(
name|EximUtil
operator|.
name|getValidatedURI
argument_list|(
name|conf
argument_list|,
name|path
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|exportFileSystem
operator|=
name|exportRootDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|exportFileSystem
operator|.
name|exists
argument_list|(
name|this
operator|.
name|exportRootDir
argument_list|)
condition|)
block|{
name|exportFileSystem
operator|.
name|mkdirs
argument_list|(
name|this
operator|.
name|exportRootDir
argument_list|)
expr_stmt|;
block|}
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
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|Path
name|partitionExportDir
parameter_list|(
name|String
name|partitionName
parameter_list|)
throws|throws
name|SemanticException
block|{
return|return
name|exportDir
argument_list|(
operator|new
name|Path
argument_list|(
name|exportRootDir
argument_list|,
name|partitionName
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|Path
name|exportDir
parameter_list|(
name|Path
name|exportDir
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
if|if
condition|(
operator|!
name|exportFileSystem
operator|.
name|exists
argument_list|(
name|exportDir
argument_list|)
condition|)
block|{
name|exportFileSystem
operator|.
name|mkdirs
argument_list|(
name|exportDir
argument_list|)
expr_stmt|;
block|}
return|return
name|exportDir
return|;
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
literal|"error while creating directory for partition at "
operator|+
name|exportDir
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|Path
name|metaDataExportFile
parameter_list|()
block|{
return|return
operator|new
name|Path
argument_list|(
name|exportRootDir
argument_list|,
name|EximUtil
operator|.
name|METADATA_NAME
argument_list|)
return|;
block|}
comment|/**      * This is currently referring to the export path for the data within a non partitioned table.      * Partition's data export directory is created within the export semantics of partition.      */
specifier|private
name|Path
name|dataExportDir
parameter_list|()
throws|throws
name|SemanticException
block|{
return|return
name|exportDir
argument_list|(
operator|new
name|Path
argument_list|(
name|exportRootDir
argument_list|,
name|EximUtil
operator|.
name|DATA_PATH_NAME
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * this level of validation might not be required as the root directory in which we dump will      * be different for each run hence possibility of it having data is not there.      */
specifier|private
name|void
name|validateTargetDir
parameter_list|(
name|URI
name|rootDirExportFile
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|rootDirExportFile
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
name|rootDirExportFile
operator|.
name|getScheme
argument_list|()
argument_list|,
name|rootDirExportFile
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|rootDirExportFile
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
name|isDirectory
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
name|rootDirExportFile
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
name|rootDirExportFile
argument_list|)
argument_list|)
throw|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|ignored
parameter_list|)
block|{         }
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
block|}
block|}
specifier|public
specifier|static
class|class
name|AuthEntities
block|{
specifier|public
specifier|final
name|Set
argument_list|<
name|ReadEntity
argument_list|>
name|inputs
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
specifier|final
name|Set
argument_list|<
name|WriteEntity
argument_list|>
name|outputs
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
block|}
block|}
end_class

end_unit
