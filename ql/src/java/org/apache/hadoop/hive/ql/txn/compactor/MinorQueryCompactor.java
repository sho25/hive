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
name|commons
operator|.
name|lang3
operator|.
name|ArrayUtils
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
name|lang3
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_comment
comment|/**  * Class responsible for handling query based minor compaction.  */
end_comment

begin_class
specifier|final
class|class
name|MinorQueryCompactor
extends|extends
name|QueryCompactor
block|{
specifier|public
specifier|static
specifier|final
name|String
name|MINOR_COMP_TBL_PROP
init|=
literal|"queryminorcomp"
decl_stmt|;
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
name|MinorQueryCompactor
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
name|info
argument_list|(
literal|"Running query based minor compaction"
argument_list|)
expr_stmt|;
name|AcidUtils
operator|.
name|setAcidOperationalProperties
argument_list|(
name|hiveConf
argument_list|,
literal|true
argument_list|,
name|AcidUtils
operator|.
name|getAcidOperationalProperties
argument_list|(
name|table
operator|.
name|getParameters
argument_list|()
argument_list|)
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
comment|// Set up the session for driver.
name|HiveConf
name|conf
init|=
operator|new
name|HiveConf
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|conf
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
name|conf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|SPLIT_GROUPING_MODE
operator|.
name|varname
argument_list|,
literal|"compactor"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_STATS_FETCH_COLUMN_STATS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_STATS_ESTIMATE_STATS
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|String
name|tmpTableName
init|=
name|table
operator|.
name|getDbName
argument_list|()
operator|+
literal|"_tmp_compactor_"
operator|+
name|table
operator|.
name|getTableName
argument_list|()
operator|+
literal|"_"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|createQueries
init|=
name|getCreateQueries
argument_list|(
name|table
argument_list|,
name|tmpTableName
argument_list|,
name|dir
argument_list|,
name|writeIds
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|compactionQueries
init|=
name|getCompactionQueries
argument_list|(
name|tmpTableName
argument_list|,
name|writeIds
operator|.
name|getInvalidWriteIds
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|dropQueries
init|=
name|getDropQueries
argument_list|(
name|tmpTableName
argument_list|)
decl_stmt|;
name|runCompactionQueries
argument_list|(
name|conf
argument_list|,
name|tmpTableName
argument_list|,
name|storageDescriptor
argument_list|,
name|writeIds
argument_list|,
name|compactionInfo
argument_list|,
name|createQueries
argument_list|,
name|compactionQueries
argument_list|,
name|dropQueries
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|commitCompaction
parameter_list|(
name|String
name|dest
parameter_list|,
name|String
name|tmpTableName
parameter_list|,
name|HiveConf
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
throws|,
name|HiveException
block|{
comment|// get result temp tables;
name|String
name|deltaTableName
init|=
name|AcidUtils
operator|.
name|DELTA_PREFIX
operator|+
name|tmpTableName
operator|+
literal|"_result"
decl_stmt|;
name|commitCompaction
argument_list|(
name|deltaTableName
argument_list|,
name|dest
argument_list|,
literal|false
argument_list|,
name|conf
argument_list|,
name|actualWriteIds
argument_list|,
name|compactorTxnId
argument_list|)
expr_stmt|;
name|String
name|deleteDeltaTableName
init|=
name|AcidUtils
operator|.
name|DELETE_DELTA_PREFIX
operator|+
name|tmpTableName
operator|+
literal|"_result"
decl_stmt|;
name|commitCompaction
argument_list|(
name|deleteDeltaTableName
argument_list|,
name|dest
argument_list|,
literal|true
argument_list|,
name|conf
argument_list|,
name|actualWriteIds
argument_list|,
name|compactorTxnId
argument_list|)
expr_stmt|;
block|}
comment|/**    * Get a list of create/alter table queries. These tables serves as temporary data source for query based    * minor compaction. The following tables are created:    *<ol>    *<li>tmpDelta, tmpDeleteDelta - temporary, external, partitioned table, having the schema of an ORC ACID file.    *   Each partition corresponds to exactly one delta/delete-delta directory</li>    *<li>tmpDeltaResult, tmpDeleteDeltaResult - temporary table which stores the aggregated results of the minor    *   compaction query</li>    *</ol>    * @param table the source table, where the compaction is running on    * @param tempTableBase an unique identifier which is used to create delta/delete-delta temp tables    * @param dir the directory, where the delta directories resides    * @param writeIds list of valid write ids, used to filter out delta directories which are not relevant for compaction    * @return list of create/alter queries, always non-null    */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getCreateQueries
parameter_list|(
name|Table
name|table
parameter_list|,
name|String
name|tempTableBase
parameter_list|,
name|AcidUtils
operator|.
name|Directory
name|dir
parameter_list|,
name|ValidWriteIdList
name|writeIds
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|queries
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// create delta temp table
name|String
name|tmpTableName
init|=
name|AcidUtils
operator|.
name|DELTA_PREFIX
operator|+
name|tempTableBase
decl_stmt|;
name|queries
operator|.
name|add
argument_list|(
name|buildCreateTableQuery
argument_list|(
name|table
argument_list|,
name|tmpTableName
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|buildAlterTableQuery
argument_list|(
name|tmpTableName
argument_list|,
name|dir
argument_list|,
name|writeIds
argument_list|,
literal|false
argument_list|)
operator|.
name|ifPresent
argument_list|(
name|queries
operator|::
name|add
argument_list|)
expr_stmt|;
comment|// create delta result temp table
name|queries
operator|.
name|add
argument_list|(
name|buildCreateTableQuery
argument_list|(
name|table
argument_list|,
name|tmpTableName
operator|+
literal|"_result"
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
comment|// create delete delta temp tables
name|String
name|tmpDeleteTableName
init|=
name|AcidUtils
operator|.
name|DELETE_DELTA_PREFIX
operator|+
name|tempTableBase
decl_stmt|;
name|queries
operator|.
name|add
argument_list|(
name|buildCreateTableQuery
argument_list|(
name|table
argument_list|,
name|tmpDeleteTableName
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|buildAlterTableQuery
argument_list|(
name|tmpDeleteTableName
argument_list|,
name|dir
argument_list|,
name|writeIds
argument_list|,
literal|true
argument_list|)
operator|.
name|ifPresent
argument_list|(
name|queries
operator|::
name|add
argument_list|)
expr_stmt|;
comment|// create delete delta result temp table
name|queries
operator|.
name|add
argument_list|(
name|buildCreateTableQuery
argument_list|(
name|table
argument_list|,
name|tmpDeleteTableName
operator|+
literal|"_result"
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|queries
return|;
block|}
comment|/**    * Helper method, which builds a create table query. The create query is customized based on the input arguments, but    * the schema of the table is the same as an ORC ACID file schema.    * @param table he source table, where the compaction is running on    * @param newTableName name of the table to be created    * @param isExternal true, if new table should be external    * @param isPartitioned true, if new table should be partitioned    * @param isBucketed true, if the new table should be bucketed    * @return a create table statement, always non-null. Example:    *<p>    *   if source table schema is: (a:int, b:int)    *</p>    * the corresponding create statement is:    *<p>    *   CREATE TEMPORARY EXTERNAL TABLE tmp_table (`operation` int, `originalTransaction` bigint, `bucket` int,    *   `rowId` bigint, `currentTransaction` bigint, `row` struct<`a` :int, `b` :int> PARTITIONED BY (`file_name` string)    *   STORED AS ORC TBLPROPERTIES ('transactional'='false','queryminorcomp'='true');    *</p>    */
specifier|private
name|String
name|buildCreateTableQuery
parameter_list|(
name|Table
name|table
parameter_list|,
name|String
name|newTableName
parameter_list|,
name|boolean
name|isExternal
parameter_list|,
name|boolean
name|isPartitioned
parameter_list|,
name|boolean
name|isBucketed
parameter_list|)
block|{
name|StringBuilder
name|query
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"create temporary "
argument_list|)
decl_stmt|;
if|if
condition|(
name|isExternal
condition|)
block|{
name|query
operator|.
name|append
argument_list|(
literal|"external "
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|append
argument_list|(
literal|"table "
argument_list|)
operator|.
name|append
argument_list|(
name|newTableName
argument_list|)
operator|.
name|append
argument_list|(
literal|" ("
argument_list|)
expr_stmt|;
comment|// Acid virtual columns
name|query
operator|.
name|append
argument_list|(
literal|"`operation` int, `originalTransaction` bigint, `bucket` int, `rowId` bigint, `currentTransaction` bigint, "
operator|+
literal|"`row` struct<"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
init|=
name|table
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
comment|// Actual columns
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
literal|":"
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
literal|">)"
argument_list|)
expr_stmt|;
if|if
condition|(
name|isPartitioned
condition|)
block|{
name|query
operator|.
name|append
argument_list|(
literal|" partitioned by (`file_name` string)"
argument_list|)
expr_stmt|;
block|}
name|int
name|bucketingVersion
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|isBucketed
condition|)
block|{
name|int
name|numBuckets
init|=
literal|1
decl_stmt|;
try|try
block|{
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
name|t
init|=
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getTable
argument_list|(
name|table
operator|.
name|getDbName
argument_list|()
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|numBuckets
operator|=
name|Math
operator|.
name|max
argument_list|(
name|t
operator|.
name|getNumBuckets
argument_list|()
argument_list|,
name|numBuckets
argument_list|)
expr_stmt|;
name|bucketingVersion
operator|=
name|t
operator|.
name|getBucketingVersion
argument_list|()
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
name|info
argument_list|(
literal|"Error finding table {}. Minor compaction result will use 0 buckets."
argument_list|,
name|table
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|query
operator|.
name|append
argument_list|(
literal|" clustered by (`bucket`)"
argument_list|)
operator|.
name|append
argument_list|(
literal|" sorted by (`bucket`, `originalTransaction`, `rowId`)"
argument_list|)
operator|.
name|append
argument_list|(
literal|" into "
argument_list|)
operator|.
name|append
argument_list|(
name|numBuckets
argument_list|)
operator|.
name|append
argument_list|(
literal|" buckets"
argument_list|)
expr_stmt|;
block|}
block|}
name|query
operator|.
name|append
argument_list|(
literal|" stored as orc"
argument_list|)
expr_stmt|;
name|query
operator|.
name|append
argument_list|(
literal|" tblproperties ('transactional'='false'"
argument_list|)
expr_stmt|;
name|query
operator|.
name|append
argument_list|(
literal|", '"
argument_list|)
expr_stmt|;
name|query
operator|.
name|append
argument_list|(
name|MINOR_COMP_TBL_PROP
argument_list|)
expr_stmt|;
name|query
operator|.
name|append
argument_list|(
literal|"'='true'"
argument_list|)
expr_stmt|;
if|if
condition|(
name|isBucketed
condition|)
block|{
name|query
operator|.
name|append
argument_list|(
literal|", 'bucketing_version'='"
argument_list|)
operator|.
name|append
argument_list|(
name|bucketingVersion
argument_list|)
operator|.
name|append
argument_list|(
literal|"')"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|query
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
return|return
name|query
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Builds an alter table query, which adds partitions pointing to location of delta directories.    * @param tableName name of the to be altered table    * @param dir the parent directory of delta directories    * @param validWriteIdList list of valid write IDs    * @param isDeleteDelta if true, only the delete delta directories will be mapped as new partitions, otherwise only    *                      the delta directories    * @return alter table statement wrapped in {@link Optional}.    */
specifier|private
name|Optional
argument_list|<
name|String
argument_list|>
name|buildAlterTableQuery
parameter_list|(
name|String
name|tableName
parameter_list|,
name|AcidUtils
operator|.
name|Directory
name|dir
parameter_list|,
name|ValidWriteIdList
name|validWriteIdList
parameter_list|,
name|boolean
name|isDeleteDelta
parameter_list|)
block|{
comment|// add partitions
if|if
condition|(
operator|!
name|dir
operator|.
name|getCurrentDirectories
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|long
name|minWriteID
init|=
name|validWriteIdList
operator|.
name|getMinOpenWriteId
argument_list|()
operator|==
literal|null
condition|?
literal|1
else|:
name|validWriteIdList
operator|.
name|getMinOpenWriteId
argument_list|()
decl_stmt|;
name|long
name|highWatermark
init|=
name|validWriteIdList
operator|.
name|getHighWatermark
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|AcidUtils
operator|.
name|ParsedDelta
argument_list|>
name|deltas
init|=
name|dir
operator|.
name|getCurrentDirectories
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|delta
lambda|->
name|delta
operator|.
name|isDeleteDelta
argument_list|()
operator|==
name|isDeleteDelta
operator|&&
name|delta
operator|.
name|getMaxWriteId
argument_list|()
operator|<=
name|highWatermark
operator|&&
name|delta
operator|.
name|getMinWriteId
argument_list|()
operator|>=
name|minWriteID
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|deltas
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|StringBuilder
name|query
init|=
operator|new
name|StringBuilder
argument_list|()
operator|.
name|append
argument_list|(
literal|"alter table "
argument_list|)
operator|.
name|append
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|query
operator|.
name|append
argument_list|(
literal|" add "
argument_list|)
expr_stmt|;
name|deltas
operator|.
name|forEach
argument_list|(
name|delta
lambda|->
name|query
operator|.
name|append
argument_list|(
literal|"partition (file_name='"
argument_list|)
operator|.
name|append
argument_list|(
name|delta
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"') location '"
argument_list|)
operator|.
name|append
argument_list|(
name|delta
operator|.
name|getPath
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"' "
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|Optional
operator|.
name|of
argument_list|(
name|query
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
block|}
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
comment|/**    * Get a list of compaction queries which fills up the delta/delete-delta temporary result tables.    * @param tmpTableBase an unique identifier, which helps to find all the temporary tables    * @param invalidWriteIds list of invalid write IDs. This list is used to filter out aborted/open transactions    * @return list of compaction queries, always non-null    */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getCompactionQueries
parameter_list|(
name|String
name|tmpTableBase
parameter_list|,
name|long
index|[]
name|invalidWriteIds
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|queries
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|sourceTableName
init|=
name|AcidUtils
operator|.
name|DELTA_PREFIX
operator|+
name|tmpTableBase
decl_stmt|;
name|String
name|resultTableName
init|=
name|sourceTableName
operator|+
literal|"_result"
decl_stmt|;
name|queries
operator|.
name|add
argument_list|(
name|buildCompactionQuery
argument_list|(
name|sourceTableName
argument_list|,
name|resultTableName
argument_list|,
name|invalidWriteIds
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|sourceDeleteTableName
init|=
name|AcidUtils
operator|.
name|DELETE_DELTA_PREFIX
operator|+
name|tmpTableBase
decl_stmt|;
name|String
name|resultDeleteTableName
init|=
name|sourceDeleteTableName
operator|+
literal|"_result"
decl_stmt|;
name|queries
operator|.
name|add
argument_list|(
name|buildCompactionQuery
argument_list|(
name|sourceDeleteTableName
argument_list|,
name|resultDeleteTableName
argument_list|,
name|invalidWriteIds
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|queries
return|;
block|}
comment|/**    * Build a minor compaction query. A compaction query selects the content of the source temporary table and inserts    * it into the result table, filtering out all rows which belong to open/aborted transactions.    * @param sourceTableName the name of the source table    * @param resultTableName the name of the result table    * @param invalidWriteIds list of invalid write IDs    * @return compaction query, always non-null    */
specifier|private
name|String
name|buildCompactionQuery
parameter_list|(
name|String
name|sourceTableName
parameter_list|,
name|String
name|resultTableName
parameter_list|,
name|long
index|[]
name|invalidWriteIds
parameter_list|)
block|{
name|StringBuilder
name|query
init|=
operator|new
name|StringBuilder
argument_list|()
operator|.
name|append
argument_list|(
literal|"insert into table "
argument_list|)
operator|.
name|append
argument_list|(
name|resultTableName
argument_list|)
operator|.
name|append
argument_list|(
literal|" select `operation`, `originalTransaction`, `bucket`, `rowId`, `currentTransaction`, `row` from "
argument_list|)
operator|.
name|append
argument_list|(
name|sourceTableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|invalidWriteIds
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|query
operator|.
name|append
argument_list|(
literal|" where `originalTransaction` not in ("
argument_list|)
operator|.
name|append
argument_list|(
name|StringUtils
operator|.
name|join
argument_list|(
name|ArrayUtils
operator|.
name|toObject
argument_list|(
name|invalidWriteIds
argument_list|)
argument_list|,
literal|","
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
return|return
name|query
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Get list of drop table statements.    * @param tmpTableBase an unique identifier, which helps to find all the tables used in query based minor compaction    * @return list of drop table statements, always non-null    */
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|getDropQueries
parameter_list|(
name|String
name|tmpTableBase
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|queries
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|dropStm
init|=
literal|"drop table if exists "
decl_stmt|;
name|queries
operator|.
name|add
argument_list|(
name|dropStm
operator|+
name|AcidUtils
operator|.
name|DELTA_PREFIX
operator|+
name|tmpTableBase
argument_list|)
expr_stmt|;
name|queries
operator|.
name|add
argument_list|(
name|dropStm
operator|+
name|AcidUtils
operator|.
name|DELETE_DELTA_PREFIX
operator|+
name|tmpTableBase
argument_list|)
expr_stmt|;
name|queries
operator|.
name|add
argument_list|(
name|dropStm
operator|+
name|AcidUtils
operator|.
name|DELTA_PREFIX
operator|+
name|tmpTableBase
operator|+
literal|"_result"
argument_list|)
expr_stmt|;
name|queries
operator|.
name|add
argument_list|(
name|dropStm
operator|+
name|AcidUtils
operator|.
name|DELETE_DELTA_PREFIX
operator|+
name|tmpTableBase
operator|+
literal|"_result"
argument_list|)
expr_stmt|;
return|return
name|queries
return|;
block|}
comment|/**    * Creates the delta directory and moves the result files.    * @param deltaTableName name of the temporary table, where the results are stored    * @param dest destination path, where the result should be moved    * @param isDeleteDelta is the destination a delete delta directory    * @param conf hive configuration    * @param actualWriteIds list of valid write Ids    * @param compactorTxnId transaction Id of the compaction    * @throws HiveException the result files cannot be moved    * @throws IOException the destination delta directory cannot be created    */
specifier|private
name|void
name|commitCompaction
parameter_list|(
name|String
name|deltaTableName
parameter_list|,
name|String
name|dest
parameter_list|,
name|boolean
name|isDeleteDelta
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|ValidWriteIdList
name|actualWriteIds
parameter_list|,
name|long
name|compactorTxnId
parameter_list|)
throws|throws
name|HiveException
throws|,
name|IOException
block|{
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
name|deltaTable
init|=
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getTable
argument_list|(
name|deltaTableName
argument_list|)
decl_stmt|;
name|Util
operator|.
name|moveContents
argument_list|(
operator|new
name|Path
argument_list|(
name|deltaTable
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|dest
argument_list|)
argument_list|,
literal|false
argument_list|,
name|isDeleteDelta
argument_list|,
name|conf
argument_list|,
name|actualWriteIds
argument_list|,
name|compactorTxnId
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

