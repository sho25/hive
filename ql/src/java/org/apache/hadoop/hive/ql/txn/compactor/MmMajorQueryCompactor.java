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
name|txn
operator|.
name|compactor
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
name|conf
operator|.
name|Configuration
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
name|StatsSetupConst
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
name|ValidTxnList
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
name|AlreadyExistsException
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
name|FieldSchema
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
name|Order
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
name|metastore
operator|.
name|api
operator|.
name|SerDeInfo
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
name|SkewedInfo
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
name|StorageDescriptor
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
name|metastore
operator|.
name|api
operator|.
name|hive_metastoreConstants
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
name|txn
operator|.
name|CompactionInfo
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
name|DriverUtils
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
name|table
operator|.
name|create
operator|.
name|show
operator|.
name|ShowCreateTableOperation
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
name|AcidOutputFormat
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
name|session
operator|.
name|SessionState
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
name|util
operator|.
name|DirectionUtils
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
name|security
operator|.
name|UserGroupInformation
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|HiveStringUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|common
operator|.
name|util
operator|.
name|Ref
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
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Modifier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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

begin_comment
comment|/**  * Class responsible to run query based major compaction on insert only tables.  */
end_comment

begin_class
class|class
name|MmMajorQueryCompactor
extends|extends
name|QueryCompactor
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
name|MmMajorQueryCompactor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
name|void
name|runCompaction
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|Table
name|table
parameter_list|,
name|Partition
name|partition
parameter_list|,
name|StorageDescriptor
name|storageDescriptor
parameter_list|,
name|ValidWriteIdList
name|writeIds
parameter_list|,
name|CompactionInfo
name|compactionInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Going to delete directories for aborted transactions for MM table "
operator|+
name|table
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|AcidUtils
operator|.
name|Directory
name|dir
init|=
name|AcidUtils
operator|.
name|getAcidState
argument_list|(
literal|null
argument_list|,
operator|new
name|Path
argument_list|(
name|storageDescriptor
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|,
name|hiveConf
argument_list|,
name|writeIds
argument_list|,
name|Ref
operator|.
name|from
argument_list|(
literal|false
argument_list|)
argument_list|,
literal|false
argument_list|,
name|table
operator|.
name|getParameters
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|removeFilesForMmTable
argument_list|(
name|hiveConf
argument_list|,
name|dir
argument_list|)
expr_stmt|;
comment|// Then, actually do the compaction.
if|if
condition|(
operator|!
name|compactionInfo
operator|.
name|isMajorCompaction
argument_list|()
condition|)
block|{
comment|// Not supported for MM tables right now.
name|LOG
operator|.
name|info
argument_list|(
literal|"Not compacting "
operator|+
name|storageDescriptor
operator|.
name|getLocation
argument_list|()
operator|+
literal|"; not a major compaction"
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
name|String
name|tmpLocation
init|=
name|Util
operator|.
name|generateTmpPath
argument_list|(
name|storageDescriptor
argument_list|)
decl_stmt|;
name|Path
name|baseLocation
init|=
operator|new
name|Path
argument_list|(
name|tmpLocation
argument_list|,
literal|"_base"
argument_list|)
decl_stmt|;
comment|// Set up the session for driver.
name|HiveConf
name|driverConf
init|=
operator|new
name|HiveConf
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|driverConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_QUOTEDID_SUPPORT
operator|.
name|varname
argument_list|,
literal|"column"
argument_list|)
expr_stmt|;
name|driverConf
operator|.
name|unset
argument_list|(
name|ValidTxnList
operator|.
name|VALID_TXNS_KEY
argument_list|)
expr_stmt|;
comment|//so Driver doesn't get confused
comment|//thinking it already has a txn opened
name|String
name|user
init|=
name|UserGroupInformation
operator|.
name|getCurrentUser
argument_list|()
operator|.
name|getShortUserName
argument_list|()
decl_stmt|;
name|SessionState
name|sessionState
init|=
name|DriverUtils
operator|.
name|setUpSessionState
argument_list|(
name|driverConf
argument_list|,
name|user
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// Note: we could skip creating the table and just add table type stuff directly to the
comment|//       "insert overwrite directory" command if there were no bucketing or list bucketing.
name|String
name|tmpPrefix
init|=
name|table
operator|.
name|getDbName
argument_list|()
operator|+
literal|".tmp_compactor_"
operator|+
name|table
operator|.
name|getTableName
argument_list|()
operator|+
literal|"_"
decl_stmt|;
name|String
name|tmpTableName
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|tmpTableName
operator|=
name|tmpPrefix
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|String
name|query
init|=
name|buildMmCompactionCtQuery
argument_list|(
name|tmpTableName
argument_list|,
name|table
argument_list|,
name|partition
operator|==
literal|null
condition|?
name|table
operator|.
name|getSd
argument_list|()
else|:
name|partition
operator|.
name|getSd
argument_list|()
argument_list|,
name|baseLocation
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Compacting a MM table into "
operator|+
name|query
argument_list|)
expr_stmt|;
try|try
block|{
name|DriverUtils
operator|.
name|runOnDriver
argument_list|(
name|driverConf
argument_list|,
name|user
argument_list|,
name|sessionState
argument_list|,
name|query
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|Throwable
name|cause
init|=
name|ex
decl_stmt|;
while|while
condition|(
name|cause
operator|!=
literal|null
operator|&&
operator|!
operator|(
name|cause
operator|instanceof
name|AlreadyExistsException
operator|)
condition|)
block|{
name|cause
operator|=
name|cause
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|cause
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
block|}
name|String
name|query
init|=
name|buildMmCompactionQuery
argument_list|(
name|table
argument_list|,
name|partition
argument_list|,
name|tmpTableName
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Compacting a MM table via "
operator|+
name|query
argument_list|)
expr_stmt|;
name|long
name|compactorTxnId
init|=
name|CompactorMR
operator|.
name|CompactorMap
operator|.
name|getCompactorTxnId
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|DriverUtils
operator|.
name|runOnDriver
argument_list|(
name|driverConf
argument_list|,
name|user
argument_list|,
name|sessionState
argument_list|,
name|query
argument_list|,
name|writeIds
argument_list|,
name|compactorTxnId
argument_list|)
expr_stmt|;
name|commitMmCompaction
argument_list|(
name|tmpLocation
argument_list|,
name|storageDescriptor
operator|.
name|getLocation
argument_list|()
argument_list|,
name|hiveConf
argument_list|,
name|writeIds
argument_list|,
name|compactorTxnId
argument_list|)
expr_stmt|;
name|DriverUtils
operator|.
name|runOnDriver
argument_list|(
name|driverConf
argument_list|,
name|user
argument_list|,
name|sessionState
argument_list|,
literal|"drop table if exists "
operator|+
name|tmpTableName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HiveException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error compacting a MM table"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|// Remove the directories for aborted transactions only
specifier|private
name|void
name|removeFilesForMmTable
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
name|AcidUtils
operator|.
name|Directory
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
comment|// For MM table, we only want to delete delta dirs for aborted txns.
name|List
argument_list|<
name|Path
argument_list|>
name|filesToDelete
init|=
name|dir
operator|.
name|getAbortedDirectories
argument_list|()
decl_stmt|;
if|if
condition|(
name|filesToDelete
operator|.
name|size
argument_list|()
operator|<
literal|1
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"About to remove "
operator|+
name|filesToDelete
operator|.
name|size
argument_list|()
operator|+
literal|" aborted directories from "
operator|+
name|dir
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|filesToDelete
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
for|for
control|(
name|Path
name|dead
range|:
name|filesToDelete
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Going to delete path "
operator|+
name|dead
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|dead
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|buildMmCompactionCtQuery
parameter_list|(
name|String
name|fullName
parameter_list|,
name|Table
name|t
parameter_list|,
name|StorageDescriptor
name|sd
parameter_list|,
name|String
name|location
parameter_list|)
block|{
name|StringBuilder
name|query
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"create temporary table "
argument_list|)
operator|.
name|append
argument_list|(
name|fullName
argument_list|)
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
init|=
name|t
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
decl_stmt|;
name|boolean
name|isFirst
init|=
literal|true
decl_stmt|;
for|for
control|(
name|FieldSchema
name|col
range|:
name|cols
control|)
block|{
if|if
condition|(
operator|!
name|isFirst
condition|)
block|{
name|query
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|isFirst
operator|=
literal|false
expr_stmt|;
name|query
operator|.
name|append
argument_list|(
literal|"`"
argument_list|)
operator|.
name|append
argument_list|(
name|col
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"` "
argument_list|)
operator|.
name|append
argument_list|(
name|col
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|append
argument_list|(
literal|") "
argument_list|)
expr_stmt|;
comment|// Bucketing.
name|List
argument_list|<
name|String
argument_list|>
name|buckCols
init|=
name|t
operator|.
name|getSd
argument_list|()
operator|.
name|getBucketCols
argument_list|()
decl_stmt|;
if|if
condition|(
name|buckCols
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|query
operator|.
name|append
argument_list|(
literal|"CLUSTERED BY ("
argument_list|)
operator|.
name|append
argument_list|(
name|StringUtils
operator|.
name|join
argument_list|(
literal|","
argument_list|,
name|buckCols
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|") "
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Order
argument_list|>
name|sortCols
init|=
name|t
operator|.
name|getSd
argument_list|()
operator|.
name|getSortCols
argument_list|()
decl_stmt|;
if|if
condition|(
name|sortCols
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|query
operator|.
name|append
argument_list|(
literal|"SORTED BY ("
argument_list|)
expr_stmt|;
name|isFirst
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|Order
name|sortCol
range|:
name|sortCols
control|)
block|{
if|if
condition|(
operator|!
name|isFirst
condition|)
block|{
name|query
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|isFirst
operator|=
literal|false
expr_stmt|;
name|query
operator|.
name|append
argument_list|(
name|sortCol
operator|.
name|getCol
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
operator|.
name|append
argument_list|(
name|DirectionUtils
operator|.
name|codeToText
argument_list|(
name|sortCol
operator|.
name|getOrder
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|append
argument_list|(
literal|") "
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|append
argument_list|(
literal|"INTO "
argument_list|)
operator|.
name|append
argument_list|(
name|t
operator|.
name|getSd
argument_list|()
operator|.
name|getNumBuckets
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|" BUCKETS"
argument_list|)
expr_stmt|;
block|}
comment|// Stored as directories. We don't care about the skew otherwise.
if|if
condition|(
name|t
operator|.
name|getSd
argument_list|()
operator|.
name|isStoredAsSubDirectories
argument_list|()
condition|)
block|{
name|SkewedInfo
name|skewedInfo
init|=
name|t
operator|.
name|getSd
argument_list|()
operator|.
name|getSkewedInfo
argument_list|()
decl_stmt|;
if|if
condition|(
name|skewedInfo
operator|!=
literal|null
operator|&&
operator|!
name|skewedInfo
operator|.
name|getSkewedColNames
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|query
operator|.
name|append
argument_list|(
literal|" SKEWED BY ("
argument_list|)
operator|.
name|append
argument_list|(
name|StringUtils
operator|.
name|join
argument_list|(
literal|", "
argument_list|,
name|skewedInfo
operator|.
name|getSkewedColNames
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|") ON "
argument_list|)
expr_stmt|;
name|isFirst
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|List
argument_list|<
name|String
argument_list|>
name|colValues
range|:
name|skewedInfo
operator|.
name|getSkewedColValues
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|isFirst
condition|)
block|{
name|query
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|isFirst
operator|=
literal|false
expr_stmt|;
name|query
operator|.
name|append
argument_list|(
literal|"('"
argument_list|)
operator|.
name|append
argument_list|(
name|StringUtils
operator|.
name|join
argument_list|(
literal|"','"
argument_list|,
name|colValues
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"')"
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|append
argument_list|(
literal|") STORED AS DIRECTORIES"
argument_list|)
expr_stmt|;
block|}
block|}
name|SerDeInfo
name|serdeInfo
init|=
name|sd
operator|.
name|getSerdeInfo
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|serdeParams
init|=
name|serdeInfo
operator|.
name|getParameters
argument_list|()
decl_stmt|;
name|query
operator|.
name|append
argument_list|(
literal|" ROW FORMAT SERDE '"
argument_list|)
operator|.
name|append
argument_list|(
name|HiveStringUtils
operator|.
name|escapeHiveCommand
argument_list|(
name|serdeInfo
operator|.
name|getSerializationLib
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
expr_stmt|;
name|String
name|sh
init|=
name|t
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|hive_metastoreConstants
operator|.
name|META_TABLE_STORAGE
argument_list|)
decl_stmt|;
assert|assert
name|sh
operator|==
literal|null
assert|;
comment|// Not supposed to be a compactable table.
if|if
condition|(
operator|!
name|serdeParams
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ShowCreateTableOperation
operator|.
name|appendSerdeParams
argument_list|(
name|query
argument_list|,
name|serdeParams
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|append
argument_list|(
literal|"STORED AS INPUTFORMAT '"
argument_list|)
operator|.
name|append
argument_list|(
name|HiveStringUtils
operator|.
name|escapeHiveCommand
argument_list|(
name|sd
operator|.
name|getInputFormat
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"' OUTPUTFORMAT '"
argument_list|)
operator|.
name|append
argument_list|(
name|HiveStringUtils
operator|.
name|escapeHiveCommand
argument_list|(
name|sd
operator|.
name|getOutputFormat
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"' LOCATION '"
argument_list|)
operator|.
name|append
argument_list|(
name|HiveStringUtils
operator|.
name|escapeHiveCommand
argument_list|(
name|location
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"' TBLPROPERTIES ("
argument_list|)
expr_stmt|;
comment|// Exclude all standard table properties.
name|Set
argument_list|<
name|String
argument_list|>
name|excludes
init|=
name|getHiveMetastoreConstants
argument_list|()
decl_stmt|;
name|excludes
operator|.
name|addAll
argument_list|(
name|StatsSetupConst
operator|.
name|TABLE_PARAMS_STATS_KEYS
argument_list|)
expr_stmt|;
name|isFirst
operator|=
literal|true
expr_stmt|;
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
name|e
range|:
name|t
operator|.
name|getParameters
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|e
operator|.
name|getValue
argument_list|()
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|excludes
operator|.
name|contains
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
operator|!
name|isFirst
condition|)
block|{
name|query
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|isFirst
operator|=
literal|false
expr_stmt|;
name|query
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
operator|.
name|append
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"'='"
argument_list|)
operator|.
name|append
argument_list|(
name|HiveStringUtils
operator|.
name|escapeHiveCommand
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|isFirst
condition|)
block|{
name|query
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|append
argument_list|(
literal|"'transactional'='false')"
argument_list|)
expr_stmt|;
return|return
name|query
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|String
name|buildMmCompactionQuery
parameter_list|(
name|Table
name|t
parameter_list|,
name|Partition
name|p
parameter_list|,
name|String
name|tmpName
parameter_list|)
block|{
name|String
name|fullName
init|=
name|t
operator|.
name|getDbName
argument_list|()
operator|+
literal|"."
operator|+
name|t
operator|.
name|getTableName
argument_list|()
decl_stmt|;
comment|// ideally we should make a special form of insert overwrite so that we:
comment|// 1) Could use fast merge path for ORC and RC.
comment|// 2) Didn't have to create a table.
name|StringBuilder
name|query
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"insert overwrite table "
operator|+
name|tmpName
operator|+
literal|" "
argument_list|)
decl_stmt|;
name|StringBuilder
name|filter
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|p
operator|!=
literal|null
condition|)
block|{
name|filter
operator|=
operator|new
name|StringBuilder
argument_list|(
literal|" where "
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|vals
init|=
name|p
operator|.
name|getValues
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|keys
init|=
name|t
operator|.
name|getPartitionKeys
argument_list|()
decl_stmt|;
assert|assert
name|keys
operator|.
name|size
argument_list|()
operator|==
name|vals
operator|.
name|size
argument_list|()
assert|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|keys
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|filter
operator|.
name|append
argument_list|(
name|i
operator|==
literal|0
condition|?
literal|"`"
else|:
literal|" and `"
argument_list|)
operator|.
name|append
argument_list|(
name|keys
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"`='"
argument_list|)
operator|.
name|append
argument_list|(
name|vals
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"'"
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|append
argument_list|(
literal|" select "
argument_list|)
expr_stmt|;
comment|// Use table descriptor for columns.
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
init|=
name|t
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|cols
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|query
operator|.
name|append
argument_list|(
name|i
operator|==
literal|0
condition|?
literal|"`"
else|:
literal|", `"
argument_list|)
operator|.
name|append
argument_list|(
name|cols
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"`"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|query
operator|.
name|append
argument_list|(
literal|"select *"
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|append
argument_list|(
literal|" from "
argument_list|)
operator|.
name|append
argument_list|(
name|fullName
argument_list|)
operator|.
name|append
argument_list|(
name|filter
argument_list|)
expr_stmt|;
return|return
name|query
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Note: similar logic to the main committer; however, no ORC versions and stuff like that.    * @param from The temp directory used for compactor output. Not the actual base/delta.    * @param to The final directory; basically a SD directory. Not the actual base/delta.    * @param compactorTxnId txn that the compactor started    */
specifier|private
name|void
name|commitMmCompaction
parameter_list|(
name|String
name|from
parameter_list|,
name|String
name|to
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|ValidWriteIdList
name|actualWriteIds
parameter_list|,
name|long
name|compactorTxnId
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|fromPath
init|=
operator|new
name|Path
argument_list|(
name|from
argument_list|)
decl_stmt|,
name|toPath
init|=
operator|new
name|Path
argument_list|(
name|to
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|fromPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// Assume the high watermark can be used as maximum transaction ID.
comment|//todo: is that true?  can it be aborted? does it matter for compaction? probably OK since
comment|//getAcidState() doesn't check if X is valid in base_X_vY for compacted base dirs.
name|long
name|maxTxn
init|=
name|actualWriteIds
operator|.
name|getHighWatermark
argument_list|()
decl_stmt|;
name|AcidOutputFormat
operator|.
name|Options
name|options
init|=
operator|new
name|AcidOutputFormat
operator|.
name|Options
argument_list|(
name|conf
argument_list|)
operator|.
name|writingBase
argument_list|(
literal|true
argument_list|)
operator|.
name|isCompressed
argument_list|(
literal|false
argument_list|)
operator|.
name|maximumWriteId
argument_list|(
name|maxTxn
argument_list|)
operator|.
name|bucket
argument_list|(
literal|0
argument_list|)
operator|.
name|statementId
argument_list|(
operator|-
literal|1
argument_list|)
operator|.
name|visibilityTxnId
argument_list|(
name|compactorTxnId
argument_list|)
decl_stmt|;
name|Path
name|newBaseDir
init|=
name|AcidUtils
operator|.
name|createFilename
argument_list|(
name|toPath
argument_list|,
name|options
argument_list|)
operator|.
name|getParent
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|fromPath
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|from
operator|+
literal|" not found.  Assuming 0 splits. Creating "
operator|+
name|newBaseDir
argument_list|)
expr_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|newBaseDir
argument_list|)
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Moving contents of "
operator|+
name|from
operator|+
literal|" to "
operator|+
name|to
argument_list|)
expr_stmt|;
name|FileStatus
index|[]
name|children
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|fromPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|children
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unexpected files in the source: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|children
argument_list|)
argument_list|)
throw|;
block|}
name|FileStatus
name|dirPath
init|=
name|children
index|[
literal|0
index|]
decl_stmt|;
name|fs
operator|.
name|rename
argument_list|(
name|dirPath
operator|.
name|getPath
argument_list|()
argument_list|,
name|newBaseDir
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|fromPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|getHiveMetastoreConstants
parameter_list|()
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|result
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Field
name|f
range|:
name|hive_metastoreConstants
operator|.
name|class
operator|.
name|getDeclaredFields
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|Modifier
operator|.
name|isStatic
argument_list|(
name|f
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
operator|!
name|Modifier
operator|.
name|isFinal
argument_list|(
name|f
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
operator|!
name|String
operator|.
name|class
operator|.
name|equals
argument_list|(
name|f
operator|.
name|getType
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|f
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
try|try
block|{
name|result
operator|.
name|add
argument_list|(
operator|(
name|String
operator|)
name|f
operator|.
name|get
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

