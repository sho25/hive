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
name|util
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
name|PathFilter
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
name|TableName
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
name|repl
operator|.
name|ReplConst
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
name|repl
operator|.
name|ReplScope
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
name|ColumnStatistics
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
name|ddl
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
name|ddl
operator|.
name|table
operator|.
name|misc
operator|.
name|AlterTableSetPropertiesDesc
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
name|partition
operator|.
name|PartitionUtils
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
name|ColumnStatsUpdateWork
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
name|ExprNodeColumnDesc
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
name|ExprNodeConstantDesc
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
name|ExprNodeGenericFuncDesc
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
name|util
operator|.
name|HiveStrictManagedMigration
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
name|serde2
operator|.
name|typeinfo
operator|.
name|PrimitiveTypeInfo
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
name|serde2
operator|.
name|typeinfo
operator|.
name|TypeInfoFactory
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
name|UpdatedMetaDataTracker
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
name|HashMap
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
name|TableMigrationOption
operator|.
name|MANAGED
import|;
end_import

begin_class
specifier|public
class|class
name|ReplUtils
block|{
specifier|public
specifier|static
specifier|final
name|String
name|LAST_REPL_ID_KEY
init|=
literal|"hive.repl.last.repl.id"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REPL_CHECKPOINT_KEY
init|=
name|ReplConst
operator|.
name|REPL_TARGET_DB_PROPERTY
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REPL_FIRST_INC_PENDING_FLAG
init|=
literal|"hive.repl.first.inc.pending"
decl_stmt|;
comment|// write id allocated in the current execution context which will be passed through config to be used by different
comment|// tasks.
specifier|public
specifier|static
specifier|final
name|String
name|REPL_CURRENT_TBL_WRITE_ID
init|=
literal|"hive.repl.current.table.write.id"
decl_stmt|;
comment|// Configuration to be received via WITH clause of REPL LOAD to clean tables from any previously failed
comment|// bootstrap load.
specifier|public
specifier|static
specifier|final
name|String
name|REPL_CLEAN_TABLES_FROM_BOOTSTRAP_CONFIG
init|=
literal|"hive.repl.clean.tables.from.bootstrap"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|FUNCTIONS_ROOT_DIR_NAME
init|=
literal|"_functions"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CONSTRAINTS_ROOT_DIR_NAME
init|=
literal|"_constraints"
decl_stmt|;
comment|// Root directory for dumping bootstrapped tables along with incremental events dump.
specifier|public
specifier|static
specifier|final
name|String
name|INC_BOOTSTRAP_ROOT_DIR_NAME
init|=
literal|"_bootstrap"
decl_stmt|;
comment|// Name of the directory which stores the list of tables included in the policy in case of table level replication.
comment|// One file per database, named after the db name. The directory is not created for db level replication.
specifier|public
specifier|static
specifier|final
name|String
name|REPL_TABLE_LIST_DIR_NAME
init|=
literal|"_tables"
decl_stmt|;
comment|// Migrating to transactional tables in bootstrap load phase.
comment|// It is enough to copy all the original files under base_1 dir and so write-id is hardcoded to 1.
specifier|public
specifier|static
specifier|final
name|Long
name|REPL_BOOTSTRAP_MIGRATION_BASE_WRITE_ID
init|=
literal|1L
decl_stmt|;
comment|// we keep the statement id as 0 so that the base directory is created with 0 and is easy to find out during
comment|// duplicate check. Note : Stmt id is not used for base directory now, but to avoid misuse later, its maintained.
specifier|public
specifier|static
specifier|final
name|int
name|REPL_BOOTSTRAP_MIGRATION_BASE_STMT_ID
init|=
literal|0
decl_stmt|;
comment|// Configuration to enable/disable dumping ACID tables. Used only for testing and shouldn't be
comment|// seen in production or in case of tests other than the ones where it's required.
specifier|public
specifier|static
specifier|final
name|String
name|REPL_DUMP_INCLUDE_ACID_TABLES
init|=
literal|"hive.repl.dump.include.acid.tables"
decl_stmt|;
comment|/**    * Bootstrap REPL LOAD operation type on the examined object based on ckpt state.    */
specifier|public
enum|enum
name|ReplLoadOpType
block|{
name|LOAD_NEW
block|,
name|LOAD_SKIP
block|,
name|LOAD_REPLACE
block|}
specifier|public
specifier|static
name|Map
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|ExprNodeGenericFuncDesc
argument_list|>
argument_list|>
name|genPartSpecs
parameter_list|(
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|partitions
parameter_list|)
throws|throws
name|SemanticException
block|{
name|Map
argument_list|<
name|Integer
argument_list|,
name|List
argument_list|<
name|ExprNodeGenericFuncDesc
argument_list|>
argument_list|>
name|partSpecs
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|partPrefixLength
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|partitions
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|partPrefixLength
operator|=
name|partitions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|size
argument_list|()
expr_stmt|;
comment|// pick the length of the first ptn, we expect all ptns listed to have the same number of
comment|// key-vals.
block|}
name|List
argument_list|<
name|ExprNodeGenericFuncDesc
argument_list|>
name|partitionDesc
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|ptn
range|:
name|partitions
control|)
block|{
comment|// convert each key-value-map to appropriate expression.
name|ExprNodeGenericFuncDesc
name|expr
init|=
literal|null
decl_stmt|;
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
name|kvp
range|:
name|ptn
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|key
init|=
name|kvp
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Object
name|val
init|=
name|kvp
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|String
name|type
init|=
name|table
operator|.
name|getPartColByName
argument_list|(
name|key
argument_list|)
operator|.
name|getType
argument_list|()
decl_stmt|;
name|PrimitiveTypeInfo
name|pti
init|=
name|TypeInfoFactory
operator|.
name|getPrimitiveTypeInfo
argument_list|(
name|type
argument_list|)
decl_stmt|;
name|ExprNodeColumnDesc
name|column
init|=
operator|new
name|ExprNodeColumnDesc
argument_list|(
name|pti
argument_list|,
name|key
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|ExprNodeGenericFuncDesc
name|op
init|=
name|PartitionUtils
operator|.
name|makeBinaryPredicate
argument_list|(
literal|"="
argument_list|,
name|column
argument_list|,
operator|new
name|ExprNodeConstantDesc
argument_list|(
name|TypeInfoFactory
operator|.
name|stringTypeInfo
argument_list|,
name|val
argument_list|)
argument_list|)
decl_stmt|;
name|expr
operator|=
operator|(
name|expr
operator|==
literal|null
operator|)
condition|?
name|op
else|:
name|PartitionUtils
operator|.
name|makeBinaryPredicate
argument_list|(
literal|"and"
argument_list|,
name|expr
argument_list|,
name|op
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|expr
operator|!=
literal|null
condition|)
block|{
name|partitionDesc
operator|.
name|add
argument_list|(
name|expr
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|partitionDesc
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|partSpecs
operator|.
name|put
argument_list|(
name|partPrefixLength
argument_list|,
name|partitionDesc
argument_list|)
expr_stmt|;
block|}
return|return
name|partSpecs
return|;
block|}
specifier|public
specifier|static
name|Task
argument_list|<
name|?
argument_list|>
name|getTableReplLogTask
parameter_list|(
name|ImportTableDesc
name|tableDesc
parameter_list|,
name|ReplLogger
name|replLogger
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|SemanticException
block|{
name|TableType
name|tableType
init|=
name|tableDesc
operator|.
name|isExternal
argument_list|()
condition|?
name|TableType
operator|.
name|EXTERNAL_TABLE
else|:
name|tableDesc
operator|.
name|tableType
argument_list|()
decl_stmt|;
name|ReplStateLogWork
name|replLogWork
init|=
operator|new
name|ReplStateLogWork
argument_list|(
name|replLogger
argument_list|,
name|tableDesc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|tableType
argument_list|)
decl_stmt|;
return|return
name|TaskFactory
operator|.
name|get
argument_list|(
name|replLogWork
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|Task
argument_list|<
name|?
argument_list|>
name|getTableCheckpointTask
parameter_list|(
name|ImportTableDesc
name|tableDesc
parameter_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
parameter_list|,
name|String
name|dumpRoot
parameter_list|,
name|HiveConf
name|conf
parameter_list|)
throws|throws
name|SemanticException
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mapProp
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|mapProp
operator|.
name|put
argument_list|(
name|REPL_CHECKPOINT_KEY
argument_list|,
name|dumpRoot
argument_list|)
expr_stmt|;
specifier|final
name|TableName
name|tName
init|=
name|TableName
operator|.
name|fromString
argument_list|(
name|tableDesc
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
name|tableDesc
operator|.
name|getDatabaseName
argument_list|()
argument_list|)
decl_stmt|;
name|AlterTableSetPropertiesDesc
name|alterTblDesc
init|=
operator|new
name|AlterTableSetPropertiesDesc
argument_list|(
name|tName
argument_list|,
name|partSpec
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
name|mapProp
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|null
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
name|alterTblDesc
argument_list|)
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|replCkptStatus
parameter_list|(
name|String
name|dbName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|props
parameter_list|,
name|String
name|dumpRoot
parameter_list|)
throws|throws
name|InvalidOperationException
block|{
comment|// If ckpt property not set or empty means, bootstrap is not run on this object.
if|if
condition|(
operator|(
name|props
operator|!=
literal|null
operator|)
operator|&&
name|props
operator|.
name|containsKey
argument_list|(
name|REPL_CHECKPOINT_KEY
argument_list|)
operator|&&
operator|!
name|props
operator|.
name|get
argument_list|(
name|REPL_CHECKPOINT_KEY
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|props
operator|.
name|get
argument_list|(
name|REPL_CHECKPOINT_KEY
argument_list|)
operator|.
name|equals
argument_list|(
name|dumpRoot
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
throw|throw
operator|new
name|InvalidOperationException
argument_list|(
name|ErrorMsg
operator|.
name|REPL_BOOTSTRAP_LOAD_PATH_NOT_VALID
operator|.
name|format
argument_list|(
name|dumpRoot
argument_list|,
name|props
operator|.
name|get
argument_list|(
name|REPL_CHECKPOINT_KEY
argument_list|)
argument_list|)
argument_list|)
throw|;
block|}
return|return
literal|false
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isTableMigratingToTransactional
parameter_list|(
name|HiveConf
name|conf
parameter_list|,
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
name|tableObj
parameter_list|)
throws|throws
name|TException
throws|,
name|IOException
block|{
if|if
condition|(
name|conf
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
operator|!
name|AcidUtils
operator|.
name|isTransactionalTable
argument_list|(
name|tableObj
argument_list|)
operator|&&
name|TableType
operator|.
name|valueOf
argument_list|(
name|tableObj
operator|.
name|getTableType
argument_list|()
argument_list|)
operator|==
name|TableType
operator|.
name|MANAGED_TABLE
condition|)
block|{
comment|//TODO : isPathOwnByHive is hard coded to true, need to get it from repl dump metadata.
name|HiveStrictManagedMigration
operator|.
name|TableMigrationOption
name|migrationOption
init|=
name|HiveStrictManagedMigration
operator|.
name|determineMigrationTypeAutomatically
argument_list|(
name|tableObj
argument_list|,
name|TableType
operator|.
name|MANAGED_TABLE
argument_list|,
literal|null
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|)
decl_stmt|;
return|return
name|migrationOption
operator|==
name|MANAGED
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|private
specifier|static
name|void
name|addOpenTxnTaskForMigration
parameter_list|(
name|String
name|actualDbName
parameter_list|,
name|String
name|actualTblName
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|UpdatedMetaDataTracker
name|updatedMetaDataTracker
parameter_list|,
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|taskList
parameter_list|,
name|Task
argument_list|<
name|?
argument_list|>
name|childTask
parameter_list|)
block|{
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
operator|new
name|ReplTxnWork
argument_list|(
name|actualDbName
argument_list|,
name|actualTblName
argument_list|,
name|ReplTxnWork
operator|.
name|OperationType
operator|.
name|REPL_MIGRATION_OPEN_TXN
argument_list|)
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|replTxnTask
operator|.
name|addDependentTask
argument_list|(
name|childTask
argument_list|)
expr_stmt|;
name|updatedMetaDataTracker
operator|.
name|setNeedCommitTxn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|taskList
operator|.
name|add
argument_list|(
name|replTxnTask
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|addOpenTxnTaskForMigration
parameter_list|(
name|String
name|actualDbName
parameter_list|,
name|String
name|actualTblName
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|UpdatedMetaDataTracker
name|updatedMetaDataTracker
parameter_list|,
name|Task
argument_list|<
name|?
argument_list|>
name|childTask
parameter_list|,
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
name|tableObj
parameter_list|)
throws|throws
name|IOException
throws|,
name|TException
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|taskList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|taskList
operator|.
name|add
argument_list|(
name|childTask
argument_list|)
expr_stmt|;
if|if
condition|(
name|isTableMigratingToTransactional
argument_list|(
name|conf
argument_list|,
name|tableObj
argument_list|)
operator|&&
name|updatedMetaDataTracker
operator|!=
literal|null
condition|)
block|{
name|addOpenTxnTaskForMigration
argument_list|(
name|actualDbName
argument_list|,
name|actualTblName
argument_list|,
name|conf
argument_list|,
name|updatedMetaDataTracker
argument_list|,
name|taskList
argument_list|,
name|childTask
argument_list|)
expr_stmt|;
block|}
return|return
name|taskList
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|addTasksForLoadingColStats
parameter_list|(
name|ColumnStatistics
name|colStats
parameter_list|,
name|HiveConf
name|conf
parameter_list|,
name|UpdatedMetaDataTracker
name|updatedMetadata
parameter_list|,
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
name|tableObj
parameter_list|,
name|long
name|writeId
parameter_list|)
throws|throws
name|IOException
throws|,
name|TException
block|{
name|List
argument_list|<
name|Task
argument_list|<
name|?
argument_list|>
argument_list|>
name|taskList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|boolean
name|isMigratingToTxn
init|=
name|ReplUtils
operator|.
name|isTableMigratingToTransactional
argument_list|(
name|conf
argument_list|,
name|tableObj
argument_list|)
decl_stmt|;
name|ColumnStatsUpdateWork
name|work
init|=
operator|new
name|ColumnStatsUpdateWork
argument_list|(
name|colStats
argument_list|,
name|isMigratingToTxn
argument_list|)
decl_stmt|;
name|work
operator|.
name|setWriteId
argument_list|(
name|writeId
argument_list|)
expr_stmt|;
name|Task
argument_list|<
name|?
argument_list|>
name|task
init|=
name|TaskFactory
operator|.
name|get
argument_list|(
name|work
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|taskList
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
comment|// If the table is going to be migrated to a transactional table we will need to open
comment|// and commit a transaction to associate a valid writeId with the statistics.
if|if
condition|(
name|isMigratingToTxn
condition|)
block|{
name|ReplUtils
operator|.
name|addOpenTxnTaskForMigration
argument_list|(
name|colStats
operator|.
name|getStatsDesc
argument_list|()
operator|.
name|getDbName
argument_list|()
argument_list|,
name|colStats
operator|.
name|getStatsDesc
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|,
name|conf
argument_list|,
name|updatedMetadata
argument_list|,
name|taskList
argument_list|,
name|task
argument_list|)
expr_stmt|;
block|}
return|return
name|taskList
return|;
block|}
comment|// Path filters to filter only events (directories) excluding "_bootstrap"
specifier|public
specifier|static
name|PathFilter
name|getEventsDirectoryFilter
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|)
block|{
return|return
name|p
lambda|->
block|{
try|try
block|{
return|return
name|fs
operator|.
name|isDirectory
argument_list|(
name|p
argument_list|)
operator|&&
operator|!
name|p
operator|.
name|getName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|ReplUtils
operator|.
name|INC_BOOTSTRAP_ROOT_DIR_NAME
argument_list|)
operator|&&
operator|!
name|p
operator|.
name|getName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|ReplUtils
operator|.
name|REPL_TABLE_LIST_DIR_NAME
argument_list|)
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|;
block|}
specifier|public
specifier|static
name|PathFilter
name|getBootstrapDirectoryFilter
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|)
block|{
return|return
name|p
lambda|->
block|{
try|try
block|{
return|return
name|fs
operator|.
name|isDirectory
argument_list|(
name|p
argument_list|)
operator|&&
operator|!
name|p
operator|.
name|getName
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
name|ReplUtils
operator|.
name|REPL_TABLE_LIST_DIR_NAME
argument_list|)
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isFirstIncPending
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|parameters
parameter_list|)
block|{
if|if
condition|(
name|parameters
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|String
name|firstIncPendFlag
init|=
name|parameters
operator|.
name|get
argument_list|(
name|ReplUtils
operator|.
name|REPL_FIRST_INC_PENDING_FLAG
argument_list|)
decl_stmt|;
comment|// If flag is not set, then we assume first incremental load is done as the database/table may be created by user
comment|// and not through replication.
return|return
name|firstIncPendFlag
operator|!=
literal|null
operator|&&
operator|!
name|firstIncPendFlag
operator|.
name|isEmpty
argument_list|()
operator|&&
literal|"true"
operator|.
name|equalsIgnoreCase
argument_list|(
name|firstIncPendFlag
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|EnvironmentContext
name|setReplDataLocationChangedFlag
parameter_list|(
name|EnvironmentContext
name|envContext
parameter_list|)
block|{
if|if
condition|(
name|envContext
operator|==
literal|null
condition|)
block|{
name|envContext
operator|=
operator|new
name|EnvironmentContext
argument_list|()
expr_stmt|;
block|}
name|envContext
operator|.
name|putToProperties
argument_list|(
name|ReplConst
operator|.
name|REPL_DATA_LOCATION_CHANGED
argument_list|,
name|ReplConst
operator|.
name|TRUE
argument_list|)
expr_stmt|;
return|return
name|envContext
return|;
block|}
specifier|public
specifier|static
name|Long
name|getMigrationCurrentTblWriteId
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
name|String
name|writeIdString
init|=
name|conf
operator|.
name|get
argument_list|(
name|ReplUtils
operator|.
name|REPL_CURRENT_TBL_WRITE_ID
argument_list|)
decl_stmt|;
if|if
condition|(
name|writeIdString
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|writeIdString
argument_list|)
return|;
block|}
comment|// Only for testing, we do not include ACID tables in the dump (and replicate) if config says so.
specifier|public
specifier|static
name|boolean
name|includeAcidTableInDump
parameter_list|(
name|HiveConf
name|conf
parameter_list|)
block|{
if|if
condition|(
name|conf
operator|.
name|getBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_IN_TEST_REPL
argument_list|)
condition|)
block|{
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|REPL_DUMP_INCLUDE_ACID_TABLES
argument_list|,
literal|true
argument_list|)
return|;
block|}
return|return
literal|true
return|;
block|}
specifier|public
specifier|static
name|boolean
name|tableIncludedInReplScope
parameter_list|(
name|ReplScope
name|replScope
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
return|return
operator|(
operator|(
name|replScope
operator|==
literal|null
operator|)
operator|||
name|replScope
operator|.
name|tableIncludedInReplScope
argument_list|(
name|tableName
argument_list|)
operator|)
return|;
block|}
block|}
end_class

end_unit

