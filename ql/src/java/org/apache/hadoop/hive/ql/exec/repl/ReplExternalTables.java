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
name|NoSuchObjectException
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
name|PathBuilder
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
name|BufferedReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Base64
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
name|Set
import|;
end_import

begin_comment
comment|/**  * Format of the file used to dump information about external tables:  *<p>  * table_name1,[base64Encoded(table_dir_location)]\n  *  * The file generated here is explicitly used for data copy of external tables and hence handling of  * writing this file is different than regular event handling for replication based on the conditions  * specified in {@link org.apache.hadoop.hive.ql.parse.repl.dump.Utils#shouldReplicate}  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|ReplExternalTables
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ReplExternalTables
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FIELD_SEPARATOR
init|=
literal|","
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FILE_NAME
init|=
literal|"_external_tables_info"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_RETRIES
init|=
literal|5
decl_stmt|;
specifier|private
name|ReplExternalTables
parameter_list|()
block|{}
specifier|public
specifier|static
name|String
name|externalTableLocation
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|String
name|location
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
name|baseDir
init|=
name|hiveConf
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPL_EXTERNAL_TABLE_BASE_DIR
operator|.
name|varname
argument_list|)
decl_stmt|;
name|Path
name|basePath
init|=
operator|new
name|Path
argument_list|(
name|baseDir
argument_list|)
decl_stmt|;
name|Path
name|currentPath
init|=
operator|new
name|Path
argument_list|(
name|location
argument_list|)
decl_stmt|;
name|Path
name|dataLocation
init|=
name|externalTableDataPath
argument_list|(
name|hiveConf
argument_list|,
name|basePath
argument_list|,
name|currentPath
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Incoming external table location: {} , new location: {}"
argument_list|,
name|location
argument_list|,
name|dataLocation
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|dataLocation
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|Path
name|externalTableDataPath
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|Path
name|basePath
parameter_list|,
name|Path
name|sourcePath
parameter_list|)
throws|throws
name|SemanticException
block|{
name|String
name|baseUriPath
init|=
name|basePath
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|String
name|sourceUriPath
init|=
name|sourcePath
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
comment|// "/" is input for base directory, then we should use exact same path as source or else append
comment|// source path under the base directory.
name|String
name|targetPathWithoutSchemeAndAuth
init|=
literal|"/"
operator|.
name|equalsIgnoreCase
argument_list|(
name|baseUriPath
argument_list|)
condition|?
name|sourceUriPath
else|:
operator|(
name|baseUriPath
operator|+
name|sourceUriPath
operator|)
decl_stmt|;
name|Path
name|dataPath
decl_stmt|;
try|try
block|{
name|dataPath
operator|=
name|PathBuilder
operator|.
name|fullyQualifiedHDFSUri
argument_list|(
operator|new
name|Path
argument_list|(
name|targetPathWithoutSchemeAndAuth
argument_list|)
argument_list|,
name|basePath
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
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
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|dataPath
return|;
block|}
specifier|public
specifier|static
class|class
name|Writer
implements|implements
name|Closeable
block|{
specifier|private
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|Writer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|final
name|Path
name|writePath
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|includeExternalTables
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|dumpMetadataOnly
decl_stmt|;
specifier|private
name|OutputStream
name|writer
decl_stmt|;
name|Writer
parameter_list|(
name|Path
name|dbRoot
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
expr_stmt|;
name|writePath
operator|=
operator|new
name|Path
argument_list|(
name|dbRoot
argument_list|,
name|FILE_NAME
argument_list|)
expr_stmt|;
name|includeExternalTables
operator|=
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPL_INCLUDE_EXTERNAL_TABLES
argument_list|)
expr_stmt|;
name|dumpMetadataOnly
operator|=
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|REPL_DUMP_METADATA_ONLY
argument_list|)
expr_stmt|;
if|if
condition|(
name|shouldWrite
argument_list|()
condition|)
block|{
name|this
operator|.
name|writer
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|hiveConf
argument_list|)
operator|.
name|create
argument_list|(
name|writePath
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|shouldWrite
parameter_list|()
block|{
return|return
operator|!
name|dumpMetadataOnly
operator|&&
name|includeExternalTables
return|;
block|}
comment|/**      * this will dump a single line per external table. it can include additional lines for the same      * table if the table is partitioned and the partition location is outside the table.      */
name|void
name|dataLocationDump
parameter_list|(
name|Table
name|table
parameter_list|)
throws|throws
name|InterruptedException
throws|,
name|IOException
throws|,
name|HiveException
block|{
if|if
condition|(
operator|!
name|shouldWrite
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
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
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"only External tables can be writen via this writer, provided table is "
operator|+
name|table
operator|.
name|getTableType
argument_list|()
argument_list|)
throw|;
block|}
name|Path
name|fullyQualifiedDataLocation
init|=
name|PathBuilder
operator|.
name|fullyQualifiedHDFSUri
argument_list|(
name|table
operator|.
name|getDataLocation
argument_list|()
argument_list|,
name|FileSystem
operator|.
name|get
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
decl_stmt|;
name|write
argument_list|(
name|lineFor
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|fullyQualifiedDataLocation
argument_list|,
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|table
operator|.
name|isPartitioned
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
decl_stmt|;
try|try
block|{
name|partitions
operator|=
name|Hive
operator|.
name|get
argument_list|(
name|hiveConf
argument_list|)
operator|.
name|getPartitions
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|NoSuchObjectException
condition|)
block|{
comment|// If table is dropped when dump in progress, just skip partitions data location dump
name|LOG
operator|.
name|debug
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
throw|throw
name|e
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
name|boolean
name|partitionLocOutsideTableLoc
init|=
operator|!
name|FileUtils
operator|.
name|isPathWithinSubtree
argument_list|(
name|partition
operator|.
name|getDataLocation
argument_list|()
argument_list|,
name|table
operator|.
name|getDataLocation
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|partitionLocOutsideTableLoc
condition|)
block|{
name|fullyQualifiedDataLocation
operator|=
name|PathBuilder
operator|.
name|fullyQualifiedHDFSUri
argument_list|(
name|partition
operator|.
name|getDataLocation
argument_list|()
argument_list|,
name|FileSystem
operator|.
name|get
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
name|write
argument_list|(
name|lineFor
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|,
name|fullyQualifiedDataLocation
argument_list|,
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
specifier|static
name|String
name|lineFor
parameter_list|(
name|String
name|tableName
parameter_list|,
name|Path
name|dataLoc
parameter_list|,
name|HiveConf
name|hiveConf
parameter_list|)
throws|throws
name|IOException
throws|,
name|SemanticException
block|{
name|StringWriter
name|lineToWrite
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|lineToWrite
operator|.
name|append
argument_list|(
name|tableName
argument_list|)
operator|.
name|append
argument_list|(
name|FIELD_SEPARATOR
argument_list|)
expr_stmt|;
name|Path
name|dataLocation
init|=
name|PathBuilder
operator|.
name|fullyQualifiedHDFSUri
argument_list|(
name|dataLoc
argument_list|,
name|dataLoc
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|encodedBytes
init|=
name|Base64
operator|.
name|getEncoder
argument_list|()
operator|.
name|encode
argument_list|(
name|dataLocation
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|encodedPath
init|=
operator|new
name|String
argument_list|(
name|encodedBytes
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
name|lineToWrite
operator|.
name|append
argument_list|(
name|encodedPath
argument_list|)
operator|.
name|append
argument_list|(
literal|"\n"
argument_list|)
expr_stmt|;
return|return
name|lineToWrite
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|void
name|write
parameter_list|(
name|String
name|line
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|int
name|currentRetry
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|currentRetry
operator|<
name|MAX_RETRIES
condition|)
block|{
try|try
block|{
name|writer
operator|.
name|write
argument_list|(
name|line
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|currentRetry
operator|++
expr_stmt|;
if|if
condition|(
name|currentRetry
operator|<
name|MAX_RETRIES
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"failed to write data with maxRetries {} due to"
argument_list|,
name|currentRetry
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"failed to write data with maxRetries {} due to"
argument_list|,
name|currentRetry
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"failed to write data"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
operator|*
name|currentRetry
operator|*
name|currentRetry
argument_list|)
expr_stmt|;
name|writer
operator|=
name|openWriterAppendMode
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|OutputStream
name|openWriterAppendMode
parameter_list|()
block|{
try|try
block|{
comment|// not sure if the exception was due to a incorrect state within the writer hence closing it
name|close
argument_list|()
expr_stmt|;
return|return
name|FileSystem
operator|.
name|get
argument_list|(
name|hiveConf
argument_list|)
operator|.
name|append
argument_list|(
name|writePath
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|String
name|message
init|=
literal|"there was an error to open the file {} in append mode"
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|message
argument_list|,
name|writePath
operator|.
name|toString
argument_list|()
argument_list|,
name|e1
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|message
argument_list|,
name|e1
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|writer
operator|!=
literal|null
condition|)
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
class|class
name|Reader
block|{
specifier|private
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|Reader
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
specifier|private
specifier|final
name|Path
name|rootReplLoadPath
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isIncrementalPhase
decl_stmt|;
specifier|public
name|Reader
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|Path
name|rootReplLoadPath
parameter_list|,
name|boolean
name|isIncrementalPhase
parameter_list|)
block|{
name|this
operator|.
name|hiveConf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|rootReplLoadPath
operator|=
name|rootReplLoadPath
expr_stmt|;
name|this
operator|.
name|isIncrementalPhase
operator|=
name|isIncrementalPhase
expr_stmt|;
block|}
comment|/**      * currently we only support dump/load of single db and the db Dump location cannot be inferred from      * the incoming dbNameOfPattern value since the load db name can be different from the target db Name      * hence traverse 1 level down from rootReplLoadPath to look for the file providing the hdfs locations.      */
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|sourceLocationsToCopy
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|isIncrementalPhase
condition|)
block|{
return|return
name|sourceLocationsToCopy
argument_list|(
operator|new
name|Path
argument_list|(
name|rootReplLoadPath
argument_list|,
name|FILE_NAME
argument_list|)
argument_list|)
return|;
block|}
comment|// this is bootstrap load path
name|Set
argument_list|<
name|String
argument_list|>
name|locationsToCopy
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|FileSystem
name|fileSystem
init|=
name|rootReplLoadPath
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|fileStatuses
init|=
name|fileSystem
operator|.
name|listStatus
argument_list|(
name|rootReplLoadPath
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|next
range|:
name|fileStatuses
control|)
block|{
if|if
condition|(
name|next
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
name|Path
name|externalTableInfoPath
init|=
operator|new
name|Path
argument_list|(
name|next
operator|.
name|getPath
argument_list|()
argument_list|,
name|FILE_NAME
argument_list|)
decl_stmt|;
name|locationsToCopy
operator|.
name|addAll
argument_list|(
name|sourceLocationsToCopy
argument_list|(
name|externalTableInfoPath
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|locationsToCopy
return|;
block|}
specifier|private
name|BufferedReader
name|reader
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|externalTableInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|InputStreamReader
name|in
init|=
operator|new
name|InputStreamReader
argument_list|(
name|fs
operator|.
name|open
argument_list|(
name|externalTableInfo
argument_list|)
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
return|return
operator|new
name|BufferedReader
argument_list|(
name|in
argument_list|)
return|;
block|}
comment|/**      * The SET of source locations should never be created based on the table Name in      * {@link #FILE_NAME} since there can be multiple entries for the same table in case the table is      * partitioned and the partitions are added by providing a separate Location for that partition,      * different than the table location.      */
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|sourceLocationsToCopy
parameter_list|(
name|Path
name|externalTableInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|locationsToCopy
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|FileSystem
name|fileSystem
init|=
name|externalTableInfo
operator|.
name|getFileSystem
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fileSystem
operator|.
name|exists
argument_list|(
name|externalTableInfo
argument_list|)
condition|)
block|{
return|return
name|locationsToCopy
return|;
block|}
name|int
name|currentRetry
init|=
literal|0
decl_stmt|;
name|BufferedReader
name|reader
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|currentRetry
operator|<
name|MAX_RETRIES
condition|)
block|{
try|try
block|{
name|reader
operator|=
name|reader
argument_list|(
name|fileSystem
argument_list|,
name|externalTableInfo
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|line
init|=
name|reader
operator|.
name|readLine
argument_list|()
init|;
name|line
operator|!=
literal|null
condition|;
name|line
operator|=
name|reader
operator|.
name|readLine
argument_list|()
control|)
block|{
name|String
index|[]
name|splits
init|=
name|line
operator|.
name|split
argument_list|(
name|FIELD_SEPARATOR
argument_list|)
decl_stmt|;
name|locationsToCopy
operator|.
name|add
argument_list|(
operator|new
name|String
argument_list|(
name|Base64
operator|.
name|getDecoder
argument_list|()
operator|.
name|decode
argument_list|(
name|splits
index|[
literal|1
index|]
argument_list|)
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|locationsToCopy
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|currentRetry
operator|++
expr_stmt|;
if|if
condition|(
name|currentRetry
operator|<
name|MAX_RETRIES
condition|)
block|{
name|closeQuietly
argument_list|(
name|reader
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"failed to read {}"
argument_list|,
name|externalTableInfo
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"failed to read {}"
argument_list|,
name|externalTableInfo
operator|.
name|toString
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
finally|finally
block|{
name|closeQuietly
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
block|}
comment|// we should never reach here
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"we should never reach this condition"
argument_list|)
throw|;
block|}
specifier|private
specifier|static
name|void
name|closeQuietly
parameter_list|(
name|BufferedReader
name|reader
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|reader
operator|!=
literal|null
condition|)
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"error while closing reader "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

