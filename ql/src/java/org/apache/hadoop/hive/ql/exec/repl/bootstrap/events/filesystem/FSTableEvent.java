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
name|events
operator|.
name|filesystem
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
name|lang
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
name|load
operator|.
name|MetaData
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
name|AddPartitionDesc
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
name|PlanUtils
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
name|HiveStrictManagedMigration
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
name|util
operator|.
name|HiveStrictManagedMigration
operator|.
name|getHiveUpdater
import|;
end_import

begin_class
specifier|public
class|class
name|FSTableEvent
implements|implements
name|TableEvent
block|{
specifier|private
specifier|final
name|Path
name|fromPath
decl_stmt|;
specifier|private
specifier|final
name|MetaData
name|metadata
decl_stmt|;
specifier|private
specifier|final
name|HiveConf
name|hiveConf
decl_stmt|;
name|FSTableEvent
parameter_list|(
name|HiveConf
name|hiveConf
parameter_list|,
name|String
name|metadataDir
parameter_list|)
block|{
try|try
block|{
name|URI
name|fromURI
init|=
name|EximUtil
operator|.
name|getValidatedURI
argument_list|(
name|hiveConf
argument_list|,
name|PlanUtils
operator|.
name|stripQuotes
argument_list|(
name|metadataDir
argument_list|)
argument_list|)
decl_stmt|;
name|fromPath
operator|=
operator|new
name|Path
argument_list|(
name|fromURI
operator|.
name|getScheme
argument_list|()
argument_list|,
name|fromURI
operator|.
name|getAuthority
argument_list|()
argument_list|,
name|fromURI
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|fromURI
argument_list|,
name|hiveConf
argument_list|)
decl_stmt|;
name|metadata
operator|=
name|EximUtil
operator|.
name|readMetaData
argument_list|(
name|fs
argument_list|,
operator|new
name|Path
argument_list|(
name|fromPath
argument_list|,
name|EximUtil
operator|.
name|METADATA_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|hiveConf
operator|=
name|hiveConf
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|boolean
name|shouldNotReplicate
parameter_list|()
block|{
name|ReplicationSpec
name|spec
init|=
name|replicationSpec
argument_list|()
decl_stmt|;
return|return
name|spec
operator|.
name|isNoop
argument_list|()
operator|||
operator|!
name|spec
operator|.
name|isInReplicationScope
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|metadataPath
parameter_list|()
block|{
return|return
name|fromPath
return|;
block|}
annotation|@
name|Override
specifier|public
name|ImportTableDesc
name|tableDesc
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
name|Table
name|table
init|=
operator|new
name|Table
argument_list|(
name|metadata
operator|.
name|getTable
argument_list|()
argument_list|)
decl_stmt|;
comment|// The table can be non acid in case of replication from 2.6 cluster.
if|if
condition|(
operator|!
name|AcidUtils
operator|.
name|isTransactionalTable
argument_list|(
name|table
argument_list|)
operator|&&
name|hiveConf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_STRICT_MANAGED_TABLES
argument_list|)
operator|&&
operator|(
name|table
operator|.
name|getTableType
argument_list|()
operator|==
name|TableType
operator|.
name|MANAGED_TABLE
operator|)
condition|)
block|{
name|Hive
name|hiveDb
init|=
name|Hive
operator|.
name|get
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
comment|//TODO : dump metadata should be read to make sure that migration is required.
name|HiveStrictManagedMigration
operator|.
name|TableMigrationOption
name|migrationOption
init|=
name|HiveStrictManagedMigration
operator|.
name|determineMigrationTypeAutomatically
argument_list|(
name|table
operator|.
name|getTTable
argument_list|()
argument_list|,
name|table
operator|.
name|getTableType
argument_list|()
argument_list|,
literal|null
argument_list|,
operator|(
name|Configuration
operator|)
name|hiveConf
argument_list|,
name|hiveDb
operator|.
name|getMSC
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|HiveStrictManagedMigration
operator|.
name|migrateTable
argument_list|(
name|table
operator|.
name|getTTable
argument_list|()
argument_list|,
name|table
operator|.
name|getTableType
argument_list|()
argument_list|,
name|migrationOption
argument_list|,
literal|false
argument_list|,
name|getHiveUpdater
argument_list|(
name|hiveConf
argument_list|)
argument_list|,
name|hiveDb
operator|.
name|getMSC
argument_list|()
argument_list|,
operator|(
name|Configuration
operator|)
name|hiveConf
argument_list|)
expr_stmt|;
comment|// If the conversion is from non transactional to transactional table
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
name|replicationSpec
argument_list|()
operator|.
name|setMigratingToTxnTable
argument_list|()
expr_stmt|;
block|}
block|}
name|ImportTableDesc
name|tableDesc
init|=
operator|new
name|ImportTableDesc
argument_list|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|dbName
argument_list|)
condition|?
name|table
operator|.
name|getDbName
argument_list|()
else|:
name|dbName
argument_list|,
name|table
argument_list|)
decl_stmt|;
name|tableDesc
operator|.
name|setReplicationSpec
argument_list|(
name|replicationSpec
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|table
operator|.
name|getTableType
argument_list|()
operator|==
name|TableType
operator|.
name|EXTERNAL_TABLE
condition|)
block|{
name|tableDesc
operator|.
name|setExternal
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
return|return
name|tableDesc
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
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|AddPartitionDesc
argument_list|>
name|partitionDescriptions
parameter_list|(
name|ImportTableDesc
name|tblDesc
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|AddPartitionDesc
argument_list|>
name|descs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|//TODO: if partitions are loaded lazily via the iterator then we will have to avoid conversion of everything here as it defeats the purpose.
for|for
control|(
name|Partition
name|partition
range|:
name|metadata
operator|.
name|getPartitions
argument_list|()
control|)
block|{
comment|// TODO: this should ideally not create AddPartitionDesc per partition
name|AddPartitionDesc
name|partsDesc
init|=
name|partitionDesc
argument_list|(
name|fromPath
argument_list|,
name|tblDesc
argument_list|,
name|partition
argument_list|)
decl_stmt|;
name|descs
operator|.
name|add
argument_list|(
name|partsDesc
argument_list|)
expr_stmt|;
block|}
return|return
name|descs
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|partitions
parameter_list|(
name|ImportTableDesc
name|tblDesc
parameter_list|)
throws|throws
name|SemanticException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|partitions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|Partition
name|partition
range|:
name|metadata
operator|.
name|getPartitions
argument_list|()
control|)
block|{
name|String
name|partName
init|=
name|Warehouse
operator|.
name|makePartName
argument_list|(
name|tblDesc
operator|.
name|getPartCols
argument_list|()
argument_list|,
name|partition
operator|.
name|getValues
argument_list|()
argument_list|)
decl_stmt|;
name|partitions
operator|.
name|add
argument_list|(
name|partName
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|MetaException
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
return|return
name|partitions
return|;
block|}
specifier|private
name|AddPartitionDesc
name|partitionDesc
parameter_list|(
name|Path
name|fromPath
parameter_list|,
name|ImportTableDesc
name|tblDesc
parameter_list|,
name|Partition
name|partition
parameter_list|)
throws|throws
name|SemanticException
block|{
try|try
block|{
name|AddPartitionDesc
name|partsDesc
init|=
operator|new
name|AddPartitionDesc
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
name|EximUtil
operator|.
name|makePartSpec
argument_list|(
name|tblDesc
operator|.
name|getPartCols
argument_list|()
argument_list|,
name|partition
operator|.
name|getValues
argument_list|()
argument_list|)
argument_list|,
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|,
name|partition
operator|.
name|getParameters
argument_list|()
argument_list|)
decl_stmt|;
name|AddPartitionDesc
operator|.
name|OnePartitionDesc
name|partDesc
init|=
name|partsDesc
operator|.
name|getPartition
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|partDesc
operator|.
name|setInputFormat
argument_list|(
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|getInputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|partDesc
operator|.
name|setOutputFormat
argument_list|(
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|getOutputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|partDesc
operator|.
name|setNumBuckets
argument_list|(
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|getNumBuckets
argument_list|()
argument_list|)
expr_stmt|;
name|partDesc
operator|.
name|setCols
argument_list|(
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|getCols
argument_list|()
argument_list|)
expr_stmt|;
name|partDesc
operator|.
name|setSerializationLib
argument_list|(
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getSerializationLib
argument_list|()
argument_list|)
expr_stmt|;
name|partDesc
operator|.
name|setSerdeParams
argument_list|(
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|getSerdeInfo
argument_list|()
operator|.
name|getParameters
argument_list|()
argument_list|)
expr_stmt|;
name|partDesc
operator|.
name|setBucketCols
argument_list|(
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|getBucketCols
argument_list|()
argument_list|)
expr_stmt|;
name|partDesc
operator|.
name|setSortCols
argument_list|(
name|partition
operator|.
name|getSd
argument_list|()
operator|.
name|getSortCols
argument_list|()
argument_list|)
expr_stmt|;
name|partDesc
operator|.
name|setLocation
argument_list|(
operator|new
name|Path
argument_list|(
name|fromPath
argument_list|,
name|Warehouse
operator|.
name|makePartName
argument_list|(
name|tblDesc
operator|.
name|getPartCols
argument_list|()
argument_list|,
name|partition
operator|.
name|getValues
argument_list|()
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|partsDesc
operator|.
name|setReplicationSpec
argument_list|(
name|replicationSpec
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|partsDesc
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
annotation|@
name|Override
specifier|public
name|ReplicationSpec
name|replicationSpec
parameter_list|()
block|{
return|return
name|metadata
operator|.
name|getReplicationSpec
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|EventType
name|eventType
parameter_list|()
block|{
return|return
name|EventType
operator|.
name|Table
return|;
block|}
block|}
end_class

end_unit

