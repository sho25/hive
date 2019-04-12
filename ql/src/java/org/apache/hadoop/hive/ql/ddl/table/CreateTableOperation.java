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
name|ddl
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
name|commons
operator|.
name|collections
operator|.
name|CollectionUtils
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
name|conf
operator|.
name|Constants
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
name|EnvironmentContext
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
name|ql
operator|.
name|ddl
operator|.
name|DDLOperation
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
name|DDLOperationContext
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
name|DDLUtils
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
name|hooks
operator|.
name|LineageInfo
operator|.
name|DataContainer
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
name|ReplicationSpec
import|;
end_import

begin_comment
comment|/**  * Operation process of creating a table.  */
end_comment

begin_class
specifier|public
class|class
name|CreateTableOperation
extends|extends
name|DDLOperation
block|{
specifier|private
specifier|final
name|CreateTableDesc
name|desc
decl_stmt|;
specifier|public
name|CreateTableOperation
parameter_list|(
name|DDLOperationContext
name|context
parameter_list|,
name|CreateTableDesc
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|desc
operator|=
name|desc
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|execute
parameter_list|()
throws|throws
name|HiveException
block|{
comment|// create the table
name|Table
name|tbl
init|=
name|desc
operator|.
name|toTable
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"creating table {} on {}"
argument_list|,
name|tbl
operator|.
name|getFullyQualifiedName
argument_list|()
argument_list|,
name|tbl
operator|.
name|getDataLocation
argument_list|()
argument_list|)
expr_stmt|;
name|boolean
name|replDataLocationChanged
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|desc
operator|.
name|getReplicationSpec
argument_list|()
operator|.
name|isInReplicationScope
argument_list|()
condition|)
block|{
comment|// If in replication scope, we should check if the object we're looking at exists, and if so,
comment|// trigger replace-mode semantics.
name|Table
name|existingTable
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getTable
argument_list|(
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|,
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|existingTable
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|desc
operator|.
name|getReplicationSpec
argument_list|()
operator|.
name|allowEventReplacementInto
argument_list|(
name|existingTable
operator|.
name|getParameters
argument_list|()
argument_list|)
condition|)
block|{
name|desc
operator|.
name|setReplaceMode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// we replace existing table.
name|ReplicationSpec
operator|.
name|copyLastReplId
argument_list|(
name|existingTable
operator|.
name|getParameters
argument_list|()
argument_list|,
name|tbl
operator|.
name|getParameters
argument_list|()
argument_list|)
expr_stmt|;
comment|// If location of an existing managed table is changed, then need to delete the old location if exists.
comment|// This scenario occurs when a managed table is converted into external table at source. In this case,
comment|// at target, the table data would be moved to different location under base directory for external tables.
if|if
condition|(
name|existingTable
operator|.
name|getTableType
argument_list|()
operator|.
name|equals
argument_list|(
name|TableType
operator|.
name|MANAGED_TABLE
argument_list|)
operator|&&
name|tbl
operator|.
name|getTableType
argument_list|()
operator|.
name|equals
argument_list|(
name|TableType
operator|.
name|EXTERNAL_TABLE
argument_list|)
operator|&&
operator|(
operator|!
name|existingTable
operator|.
name|getDataLocation
argument_list|()
operator|.
name|equals
argument_list|(
name|tbl
operator|.
name|getDataLocation
argument_list|()
argument_list|)
operator|)
condition|)
block|{
name|replDataLocationChanged
operator|=
literal|true
expr_stmt|;
block|}
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"DDLTask: Create Table is skipped as table {} is newer than update"
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
comment|// no replacement, the existing table state is newer than our update.
block|}
block|}
block|}
comment|// create the table
if|if
condition|(
name|desc
operator|.
name|getReplaceMode
argument_list|()
condition|)
block|{
name|createTableReplaceMode
argument_list|(
name|tbl
argument_list|,
name|replDataLocationChanged
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|createTableNonReplaceMode
argument_list|(
name|tbl
argument_list|)
expr_stmt|;
block|}
name|DDLUtils
operator|.
name|addIfAbsentByName
argument_list|(
operator|new
name|WriteEntity
argument_list|(
name|tbl
argument_list|,
name|WriteEntity
operator|.
name|WriteType
operator|.
name|DDL_NO_LOCK
argument_list|)
argument_list|,
name|context
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
specifier|private
name|void
name|createTableReplaceMode
parameter_list|(
name|Table
name|tbl
parameter_list|,
name|boolean
name|replDataLocationChanged
parameter_list|)
throws|throws
name|HiveException
block|{
name|ReplicationSpec
name|replicationSpec
init|=
name|desc
operator|.
name|getReplicationSpec
argument_list|()
decl_stmt|;
name|Long
name|writeId
init|=
literal|0L
decl_stmt|;
name|EnvironmentContext
name|environmentContext
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|replicationSpec
operator|!=
literal|null
operator|&&
name|replicationSpec
operator|.
name|isInReplicationScope
argument_list|()
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
comment|// for migration we start the transaction and allocate write id in repl txn task for migration.
name|writeId
operator|=
name|ReplUtils
operator|.
name|getMigrationCurrentTblWriteId
argument_list|(
name|context
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|writeId
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|HiveException
argument_list|(
literal|"DDLTask : Write id is not set in the config by open txn task for migration"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|writeId
operator|=
name|desc
operator|.
name|getReplWriteId
argument_list|()
expr_stmt|;
block|}
comment|// In case of replication statistics is obtained from the source, so do not update those
comment|// on replica.
name|environmentContext
operator|=
operator|new
name|EnvironmentContext
argument_list|()
expr_stmt|;
name|environmentContext
operator|.
name|putToProperties
argument_list|(
name|StatsSetupConst
operator|.
name|DO_NOT_UPDATE_STATS
argument_list|,
name|StatsSetupConst
operator|.
name|TRUE
argument_list|)
expr_stmt|;
block|}
comment|// In replication flow, if table's data location is changed, then set the corresponding flag in
comment|// environment context to notify Metastore to update location of all partitions and delete old directory.
if|if
condition|(
name|replDataLocationChanged
condition|)
block|{
name|environmentContext
operator|=
name|ReplUtils
operator|.
name|setReplDataLocationChangedFlag
argument_list|(
name|environmentContext
argument_list|)
expr_stmt|;
block|}
comment|// replace-mode creates are really alters using CreateTableDesc.
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|alterTable
argument_list|(
name|tbl
operator|.
name|getCatName
argument_list|()
argument_list|,
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|,
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|,
name|tbl
argument_list|,
literal|false
argument_list|,
name|environmentContext
argument_list|,
literal|true
argument_list|,
name|writeId
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|createTableNonReplaceMode
parameter_list|(
name|Table
name|tbl
parameter_list|)
throws|throws
name|HiveException
block|{
if|if
condition|(
name|CollectionUtils
operator|.
name|isNotEmpty
argument_list|(
name|desc
operator|.
name|getPrimaryKeys
argument_list|()
argument_list|)
operator|||
name|CollectionUtils
operator|.
name|isNotEmpty
argument_list|(
name|desc
operator|.
name|getForeignKeys
argument_list|()
argument_list|)
operator|||
name|CollectionUtils
operator|.
name|isNotEmpty
argument_list|(
name|desc
operator|.
name|getUniqueConstraints
argument_list|()
argument_list|)
operator|||
name|CollectionUtils
operator|.
name|isNotEmpty
argument_list|(
name|desc
operator|.
name|getNotNullConstraints
argument_list|()
argument_list|)
operator|||
name|CollectionUtils
operator|.
name|isNotEmpty
argument_list|(
name|desc
operator|.
name|getDefaultConstraints
argument_list|()
argument_list|)
operator|||
name|CollectionUtils
operator|.
name|isNotEmpty
argument_list|(
name|desc
operator|.
name|getCheckConstraints
argument_list|()
argument_list|)
condition|)
block|{
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|createTable
argument_list|(
name|tbl
argument_list|,
name|desc
operator|.
name|getIfNotExists
argument_list|()
argument_list|,
name|desc
operator|.
name|getPrimaryKeys
argument_list|()
argument_list|,
name|desc
operator|.
name|getForeignKeys
argument_list|()
argument_list|,
name|desc
operator|.
name|getUniqueConstraints
argument_list|()
argument_list|,
name|desc
operator|.
name|getNotNullConstraints
argument_list|()
argument_list|,
name|desc
operator|.
name|getDefaultConstraints
argument_list|()
argument_list|,
name|desc
operator|.
name|getCheckConstraints
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|createTable
argument_list|(
name|tbl
argument_list|,
name|desc
operator|.
name|getIfNotExists
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|desc
operator|.
name|isCTAS
argument_list|()
condition|)
block|{
name|Table
name|createdTable
init|=
name|context
operator|.
name|getDb
argument_list|()
operator|.
name|getTable
argument_list|(
name|tbl
operator|.
name|getDbName
argument_list|()
argument_list|,
name|tbl
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|DataContainer
name|dc
init|=
operator|new
name|DataContainer
argument_list|(
name|createdTable
operator|.
name|getTTable
argument_list|()
argument_list|)
decl_stmt|;
name|context
operator|.
name|getQueryState
argument_list|()
operator|.
name|getLineageState
argument_list|()
operator|.
name|setLineage
argument_list|(
name|createdTable
operator|.
name|getPath
argument_list|()
argument_list|,
name|dc
argument_list|,
name|createdTable
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|boolean
name|doesTableNeedLocation
parameter_list|(
name|Table
name|tbl
parameter_list|)
block|{
comment|// TODO: If we are ok with breaking compatibility of existing 3rd party StorageHandlers,
comment|// this method could be moved to the HiveStorageHandler interface.
name|boolean
name|retval
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|tbl
operator|.
name|getStorageHandler
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// TODO: why doesn't this check class name rather than toString?
name|String
name|sh
init|=
name|tbl
operator|.
name|getStorageHandler
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|retval
operator|=
operator|!
literal|"org.apache.hadoop.hive.hbase.HBaseStorageHandler"
operator|.
name|equals
argument_list|(
name|sh
argument_list|)
operator|&&
operator|!
name|Constants
operator|.
name|DRUID_HIVE_STORAGE_HANDLER_ID
operator|.
name|equals
argument_list|(
name|sh
argument_list|)
operator|&&
operator|!
name|Constants
operator|.
name|JDBC_HIVE_STORAGE_HANDLER_ID
operator|.
name|equals
argument_list|(
name|sh
argument_list|)
operator|&&
operator|!
literal|"org.apache.hadoop.hive.accumulo.AccumuloStorageHandler"
operator|.
name|equals
argument_list|(
name|sh
argument_list|)
expr_stmt|;
block|}
return|return
name|retval
return|;
block|}
specifier|public
specifier|static
name|void
name|makeLocationQualified
parameter_list|(
name|Table
name|table
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|HiveException
block|{
name|StorageDescriptor
name|sd
init|=
name|table
operator|.
name|getTTable
argument_list|()
operator|.
name|getSd
argument_list|()
decl_stmt|;
comment|// If the table's location is currently unset, it is left unset, allowing the metastore to
comment|// fill in the table's location.
comment|// Note that the previous logic for some reason would make a special case if the DB was the
comment|// default database, and actually attempt to generate a  location.
comment|// This seems incorrect and uncessary, since the metastore is just as able to fill in the
comment|// default table location in the case of the default DB, as it is for non-default DBs.
name|Path
name|path
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|sd
operator|.
name|isSetLocation
argument_list|()
condition|)
block|{
name|path
operator|=
operator|new
name|Path
argument_list|(
name|sd
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|path
operator|!=
literal|null
condition|)
block|{
name|sd
operator|.
name|setLocation
argument_list|(
name|Utilities
operator|.
name|getQualifiedPath
argument_list|(
name|conf
argument_list|,
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

