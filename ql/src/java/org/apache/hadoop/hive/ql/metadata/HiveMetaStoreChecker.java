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
name|metadata
package|;
end_package

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
name|Map
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
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|metadata
operator|.
name|CheckResult
operator|.
name|PartitionResult
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_comment
comment|/**  * Verify that the information in the metastore matches what is on the  * filesystem. Return a CheckResult object containing lists of missing and any  * unexpected tables and partitions.  */
end_comment

begin_class
specifier|public
class|class
name|HiveMetaStoreChecker
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HiveMetaStoreChecker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Hive
name|hive
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|conf
decl_stmt|;
specifier|public
name|HiveMetaStoreChecker
parameter_list|(
name|Hive
name|hive
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|hive
operator|=
name|hive
expr_stmt|;
name|conf
operator|=
name|hive
operator|.
name|getConf
argument_list|()
expr_stmt|;
block|}
comment|/**    * Check the metastore for inconsistencies, data missing in either the    * metastore or on the dfs.    *    * @param dbName    *          name of the database, if not specified the default will be used.    * @param tableName    *          Table we want to run the check for. If null we'll check all the    *          tables in the database.    * @param partitions    *          List of partition name value pairs, if null or empty check all    *          partitions    * @param result    *          Fill this with the results of the check    * @throws HiveException    *           Failed to get required information from the metastore.    * @throws IOException    *           Most likely filesystem related    */
specifier|public
name|void
name|checkMetastore
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|partitions
parameter_list|,
name|CheckResult
name|result
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
if|if
condition|(
name|dbName
operator|==
literal|null
operator|||
literal|""
operator|.
name|equalsIgnoreCase
argument_list|(
name|dbName
argument_list|)
condition|)
block|{
name|dbName
operator|=
name|MetaStoreUtils
operator|.
name|DEFAULT_DATABASE_NAME
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|tableName
operator|==
literal|null
operator|||
literal|""
operator|.
name|equals
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
comment|// no table specified, check all tables and all partitions.
name|List
argument_list|<
name|String
argument_list|>
name|tables
init|=
name|hive
operator|.
name|getTablesForDb
argument_list|(
name|dbName
argument_list|,
literal|".*"
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|currentTableName
range|:
name|tables
control|)
block|{
name|checkTable
argument_list|(
name|dbName
argument_list|,
name|currentTableName
argument_list|,
literal|null
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
name|findUnknownTables
argument_list|(
name|dbName
argument_list|,
name|tables
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|partitions
operator|==
literal|null
operator|||
name|partitions
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// only one table, let's check all partitions
name|checkTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// check the specified partitions
name|checkTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partitions
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|result
operator|.
name|getPartitionsNotInMs
argument_list|()
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|result
operator|.
name|getPartitionsNotOnFs
argument_list|()
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|result
operator|.
name|getTablesNotInMs
argument_list|()
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|result
operator|.
name|getTablesNotOnFs
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|TException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Check for table directories that aren't in the metastore.    *    * @param dbName    *          Name of the database    * @param tables    *          List of table names    * @param result    *          Add any found tables to this    * @throws HiveException    *           Failed to get required information from the metastore.    * @throws IOException    *           Most likely filesystem related    * @throws MetaException    *           Failed to get required information from the metastore.    * @throws NoSuchObjectException    *           Failed to get required information from the metastore.    * @throws TException    *           Thrift communication error.    */
name|void
name|findUnknownTables
parameter_list|(
name|String
name|dbName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|tables
parameter_list|,
name|CheckResult
name|result
parameter_list|)
throws|throws
name|IOException
throws|,
name|MetaException
throws|,
name|TException
throws|,
name|HiveException
block|{
name|Set
argument_list|<
name|Path
argument_list|>
name|dbPaths
init|=
operator|new
name|HashSet
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|tableNames
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|tables
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|tableName
range|:
name|tables
control|)
block|{
name|Table
name|table
init|=
name|hive
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
comment|// hack, instead figure out a way to get the db paths
name|String
name|isExternal
init|=
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
literal|"EXTERNAL"
argument_list|)
decl_stmt|;
if|if
condition|(
name|isExternal
operator|==
literal|null
operator|||
operator|!
literal|"TRUE"
operator|.
name|equalsIgnoreCase
argument_list|(
name|isExternal
argument_list|)
condition|)
block|{
name|dbPaths
operator|.
name|add
argument_list|(
name|table
operator|.
name|getPath
argument_list|()
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|Path
name|dbPath
range|:
name|dbPaths
control|)
block|{
name|FileSystem
name|fs
init|=
name|dbPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|statuses
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|dbPath
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|status
range|:
name|statuses
control|)
block|{
if|if
condition|(
name|status
operator|.
name|isDir
argument_list|()
operator|&&
operator|!
name|tableNames
operator|.
name|contains
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|result
operator|.
name|getTablesNotInMs
argument_list|()
operator|.
name|add
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Check the metastore for inconsistencies, data missing in either the    * metastore or on the dfs.    *    * @param dbName    *          Name of the database    * @param tableName    *          Name of the table    * @param partitions    *          Partitions to check, if null or empty get all the partitions.    * @param result    *          Result object    * @throws HiveException    *           Failed to get required information from the metastore.    * @throws IOException    *           Most likely filesystem related    * @throws MetaException    *           Failed to get required information from the metastore.    */
name|void
name|checkTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|partitions
parameter_list|,
name|CheckResult
name|result
parameter_list|)
throws|throws
name|MetaException
throws|,
name|IOException
throws|,
name|HiveException
block|{
name|Table
name|table
init|=
literal|null
decl_stmt|;
try|try
block|{
name|table
operator|=
name|hive
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|result
operator|.
name|getTablesNotInMs
argument_list|()
operator|.
name|add
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
return|return;
block|}
name|List
argument_list|<
name|Partition
argument_list|>
name|parts
init|=
operator|new
name|ArrayList
argument_list|<
name|Partition
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|findUnknownPartitions
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|table
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
operator|||
name|partitions
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// no partitions specified, let's get all
name|parts
operator|=
name|hive
operator|.
name|getPartitions
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// we're interested in specific partitions,
comment|// don't check for any others
name|findUnknownPartitions
operator|=
literal|false
expr_stmt|;
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
range|:
name|partitions
control|)
block|{
name|Partition
name|part
init|=
name|hive
operator|.
name|getPartition
argument_list|(
name|table
argument_list|,
name|map
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|part
operator|==
literal|null
condition|)
block|{
name|PartitionResult
name|pr
init|=
operator|new
name|PartitionResult
argument_list|()
decl_stmt|;
name|pr
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|pr
operator|.
name|setPartitionName
argument_list|(
name|Warehouse
operator|.
name|makePartPath
argument_list|(
name|map
argument_list|)
argument_list|)
expr_stmt|;
name|result
operator|.
name|getPartitionsNotInMs
argument_list|()
operator|.
name|add
argument_list|(
name|pr
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|parts
operator|.
name|add
argument_list|(
name|part
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|checkTable
argument_list|(
name|table
argument_list|,
name|parts
argument_list|,
name|findUnknownPartitions
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check the metastore for inconsistencies, data missing in either the    * metastore or on the dfs.    *    * @param table    *          Table to check    * @param parts    *          Partitions to check    * @param result    *          Result object    * @param findUnknownPartitions    *          Should we try to find unknown partitions?    * @throws IOException    *           Could not get information from filesystem    * @throws HiveException    *           Could not create Partition object    */
name|void
name|checkTable
parameter_list|(
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|parts
parameter_list|,
name|boolean
name|findUnknownPartitions
parameter_list|,
name|CheckResult
name|result
parameter_list|)
throws|throws
name|IOException
throws|,
name|HiveException
block|{
name|Path
name|tablePath
init|=
name|table
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|tablePath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|tablePath
argument_list|)
condition|)
block|{
name|result
operator|.
name|getTablesNotOnFs
argument_list|()
operator|.
name|add
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|Set
argument_list|<
name|Path
argument_list|>
name|partPaths
init|=
operator|new
name|HashSet
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
comment|// check that the partition folders exist on disk
for|for
control|(
name|Partition
name|partition
range|:
name|parts
control|)
block|{
if|if
condition|(
name|partition
operator|==
literal|null
condition|)
block|{
comment|// most likely the user specified an invalid partition
continue|continue;
block|}
name|Path
name|partPath
init|=
name|partition
operator|.
name|getDataLocation
argument_list|()
decl_stmt|;
name|fs
operator|=
name|partPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|partPath
argument_list|)
condition|)
block|{
name|PartitionResult
name|pr
init|=
operator|new
name|PartitionResult
argument_list|()
decl_stmt|;
name|pr
operator|.
name|setPartitionName
argument_list|(
name|partition
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|pr
operator|.
name|setTableName
argument_list|(
name|partition
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|getPartitionsNotOnFs
argument_list|()
operator|.
name|add
argument_list|(
name|pr
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|partition
operator|.
name|getSpec
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|partPaths
operator|.
name|add
argument_list|(
name|partPath
operator|.
name|makeQualified
argument_list|(
name|fs
argument_list|)
argument_list|)
expr_stmt|;
name|partPath
operator|=
name|partPath
operator|.
name|getParent
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|findUnknownPartitions
condition|)
block|{
name|findUnknownPartitions
argument_list|(
name|table
argument_list|,
name|partPaths
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Find partitions on the fs that are unknown to the metastore.    *    * @param table    *          Table where the partitions would be located    * @param partPaths    *          Paths of the partitions the ms knows about    * @param result    *          Result object    * @throws IOException    *           Thrown if we fail at fetching listings from the fs.    */
name|void
name|findUnknownPartitions
parameter_list|(
name|Table
name|table
parameter_list|,
name|Set
argument_list|<
name|Path
argument_list|>
name|partPaths
parameter_list|,
name|CheckResult
name|result
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|tablePath
init|=
name|table
operator|.
name|getPath
argument_list|()
decl_stmt|;
comment|// now check the table folder and see if we find anything
comment|// that isn't in the metastore
name|Set
argument_list|<
name|Path
argument_list|>
name|allPartDirs
init|=
operator|new
name|HashSet
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|getAllLeafDirs
argument_list|(
name|tablePath
argument_list|,
name|allPartDirs
argument_list|)
expr_stmt|;
comment|// don't want the table dir
name|allPartDirs
operator|.
name|remove
argument_list|(
name|tablePath
argument_list|)
expr_stmt|;
comment|// remove the partition paths we know about
name|allPartDirs
operator|.
name|removeAll
argument_list|(
name|partPaths
argument_list|)
expr_stmt|;
comment|// we should now only have the unexpected folders left
for|for
control|(
name|Path
name|partPath
range|:
name|allPartDirs
control|)
block|{
name|FileSystem
name|fs
init|=
name|partPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|String
name|partitionName
init|=
name|getPartitionName
argument_list|(
name|fs
operator|.
name|makeQualified
argument_list|(
name|tablePath
argument_list|)
argument_list|,
name|partPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|partitionName
operator|!=
literal|null
condition|)
block|{
name|PartitionResult
name|pr
init|=
operator|new
name|PartitionResult
argument_list|()
decl_stmt|;
name|pr
operator|.
name|setPartitionName
argument_list|(
name|partitionName
argument_list|)
expr_stmt|;
name|pr
operator|.
name|setTableName
argument_list|(
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|getPartitionsNotInMs
argument_list|()
operator|.
name|add
argument_list|(
name|pr
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Get the partition name from the path.    *    * @param tablePath    *          Path of the table.    * @param partitionPath    *          Path of the partition.    * @return Partition name, for example partitiondate=2008-01-01    */
specifier|private
name|String
name|getPartitionName
parameter_list|(
name|Path
name|tablePath
parameter_list|,
name|Path
name|partitionPath
parameter_list|)
block|{
name|String
name|result
init|=
literal|null
decl_stmt|;
name|Path
name|currPath
init|=
name|partitionPath
decl_stmt|;
while|while
condition|(
name|currPath
operator|!=
literal|null
operator|&&
operator|!
name|tablePath
operator|.
name|equals
argument_list|(
name|currPath
argument_list|)
condition|)
block|{
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|result
operator|=
name|currPath
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|result
operator|=
name|currPath
operator|.
name|getName
argument_list|()
operator|+
name|Path
operator|.
name|SEPARATOR
operator|+
name|result
expr_stmt|;
block|}
name|currPath
operator|=
name|currPath
operator|.
name|getParent
argument_list|()
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Recursive method to get the leaf directories of a base path. Example:    * base/dir1/dir2 base/dir3    *    * This will return dir2 and dir3 but not dir1.    *    * @param basePath    *          Start directory    * @param allDirs    *          This set will contain the leaf paths at the end.    * @throws IOException    *           Thrown if we can't get lists from the fs.    */
specifier|private
name|void
name|getAllLeafDirs
parameter_list|(
name|Path
name|basePath
parameter_list|,
name|Set
argument_list|<
name|Path
argument_list|>
name|allDirs
parameter_list|)
throws|throws
name|IOException
block|{
name|getAllLeafDirs
argument_list|(
name|basePath
argument_list|,
name|allDirs
argument_list|,
name|basePath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|getAllLeafDirs
parameter_list|(
name|Path
name|basePath
parameter_list|,
name|Set
argument_list|<
name|Path
argument_list|>
name|allDirs
parameter_list|,
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
index|[]
name|statuses
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|basePath
argument_list|)
decl_stmt|;
name|boolean
name|directoryFound
init|=
literal|false
decl_stmt|;
for|for
control|(
name|FileStatus
name|status
range|:
name|statuses
control|)
block|{
if|if
condition|(
name|status
operator|.
name|isDir
argument_list|()
condition|)
block|{
name|directoryFound
operator|=
literal|true
expr_stmt|;
name|getAllLeafDirs
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
argument_list|,
name|allDirs
argument_list|,
name|fs
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|directoryFound
condition|)
block|{
name|allDirs
operator|.
name|add
argument_list|(
name|basePath
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

