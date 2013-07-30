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
name|metastore
package|;
end_package

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
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|Index
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
name|InvalidInputException
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
name|InvalidObjectException
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
name|InvalidPartitionException
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
name|PartitionEventType
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
name|PrincipalPrivilegeSet
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
name|PrincipalType
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
name|PrivilegeBag
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
name|Role
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
name|Type
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
name|UnknownDBException
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
name|UnknownPartitionException
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
name|UnknownTableException
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
name|model
operator|.
name|MDBPrivilege
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
name|model
operator|.
name|MGlobalPrivilege
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
name|model
operator|.
name|MPartitionColumnPrivilege
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
name|model
operator|.
name|MPartitionPrivilege
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
name|model
operator|.
name|MRoleMap
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
name|model
operator|.
name|MTableColumnPrivilege
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
name|model
operator|.
name|MTablePrivilege
import|;
end_import

begin_comment
comment|/**  *  * DummyRawStoreForJdoConnection.  *  * An implementation of RawStore that verifies the DummyJdoConnectionUrlHook has already been  * applied when this class's setConf method is called, by checking that the value of the  * METASTORECONNECTURLKEY ConfVar has been updated.  *  * All non-void methods return default values.  */
end_comment

begin_class
specifier|public
class|class
name|DummyRawStoreForJdoConnection
implements|implements
name|RawStore
block|{
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|arg0
parameter_list|)
block|{
name|String
name|expected
init|=
name|DummyJdoConnectionUrlHook
operator|.
name|newUrl
decl_stmt|;
name|String
name|actual
init|=
name|arg0
operator|.
name|get
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORECONNECTURLKEY
operator|.
name|varname
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"The expected URL used by JDO to connect to the metastore: "
operator|+
name|expected
operator|+
literal|" did not match the actual value when the Raw Store was initialized: "
operator|+
name|actual
argument_list|,
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
block|{     }
annotation|@
name|Override
specifier|public
name|boolean
name|openTransaction
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|commitTransaction
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|rollbackTransaction
parameter_list|()
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|createDatabase
parameter_list|(
name|Database
name|db
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{     }
annotation|@
name|Override
specifier|public
name|Database
name|getDatabase
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|NoSuchObjectException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|dropDatabase
parameter_list|(
name|String
name|dbname
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|alterDatabase
parameter_list|(
name|String
name|dbname
parameter_list|,
name|Database
name|db
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getDatabases
parameter_list|(
name|String
name|pattern
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAllDatabases
parameter_list|()
throws|throws
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|createType
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Type
name|getType
parameter_list|(
name|String
name|typeName
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|dropType
parameter_list|(
name|String
name|typeName
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|createTable
parameter_list|(
name|Table
name|tbl
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{     }
annotation|@
name|Override
specifier|public
name|boolean
name|dropTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Table
name|getTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|addPartition
parameter_list|(
name|Partition
name|part
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Partition
name|getPartition
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|part_vals
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|dropPartition
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|part_vals
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Partition
argument_list|>
name|getPartitions
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|int
name|max
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|alterTable
parameter_list|(
name|String
name|dbname
parameter_list|,
name|String
name|name
parameter_list|,
name|Table
name|newTable
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{     }
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getTables
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|pattern
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Table
argument_list|>
name|getTableObjectsByName
parameter_list|(
name|String
name|dbname
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|tableNames
parameter_list|)
throws|throws
name|MetaException
throws|,
name|UnknownDBException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAllTables
parameter_list|(
name|String
name|dbName
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|listTableNamesByFilter
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|filter
parameter_list|,
name|short
name|max_tables
parameter_list|)
throws|throws
name|MetaException
throws|,
name|UnknownDBException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|listPartitionNames
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|short
name|max_parts
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|listPartitionNamesByFilter
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|String
name|filter
parameter_list|,
name|short
name|max_parts
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|alterPartition
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|part_vals
parameter_list|,
name|Partition
name|new_part
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|alterPartitions
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|part_vals_list
parameter_list|,
name|List
argument_list|<
name|Partition
argument_list|>
name|new_parts
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{     }
annotation|@
name|Override
specifier|public
name|boolean
name|addIndex
parameter_list|(
name|Index
name|index
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Index
name|getIndex
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|origTableName
parameter_list|,
name|String
name|indexName
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|dropIndex
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|origTableName
parameter_list|,
name|String
name|indexName
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Index
argument_list|>
name|getIndexes
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|origTableName
parameter_list|,
name|int
name|max
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|listIndexNames
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|origTableName
parameter_list|,
name|short
name|max
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|alterIndex
parameter_list|(
name|String
name|dbname
parameter_list|,
name|String
name|baseTblName
parameter_list|,
name|String
name|name
parameter_list|,
name|Index
name|newIndex
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{     }
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Partition
argument_list|>
name|getPartitionsByFilter
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|String
name|filter
parameter_list|,
name|short
name|maxParts
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Partition
argument_list|>
name|getPartitionsByNames
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partNames
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Table
name|markPartitionForEvent
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partVals
parameter_list|,
name|PartitionEventType
name|evtType
parameter_list|)
throws|throws
name|MetaException
throws|,
name|UnknownTableException
throws|,
name|InvalidPartitionException
throws|,
name|UnknownPartitionException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isPartitionMarkedForEvent
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partName
parameter_list|,
name|PartitionEventType
name|evtType
parameter_list|)
throws|throws
name|MetaException
throws|,
name|UnknownTableException
throws|,
name|InvalidPartitionException
throws|,
name|UnknownPartitionException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|addRole
parameter_list|(
name|String
name|rowName
parameter_list|,
name|String
name|ownerName
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
throws|,
name|NoSuchObjectException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|removeRole
parameter_list|(
name|String
name|roleName
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|grantRole
parameter_list|(
name|Role
name|role
parameter_list|,
name|String
name|userName
parameter_list|,
name|PrincipalType
name|principalType
parameter_list|,
name|String
name|grantor
parameter_list|,
name|PrincipalType
name|grantorType
parameter_list|,
name|boolean
name|grantOption
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
throws|,
name|InvalidObjectException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|revokeRole
parameter_list|(
name|Role
name|role
parameter_list|,
name|String
name|userName
parameter_list|,
name|PrincipalType
name|principalType
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|PrincipalPrivilegeSet
name|getUserPrivilegeSet
parameter_list|(
name|String
name|userName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|groupNames
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|PrincipalPrivilegeSet
name|getDBPrivilegeSet
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|userName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|groupNames
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|PrincipalPrivilegeSet
name|getTablePrivilegeSet
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|userName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|groupNames
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|PrincipalPrivilegeSet
name|getPartitionPrivilegeSet
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|partition
parameter_list|,
name|String
name|userName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|groupNames
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|PrincipalPrivilegeSet
name|getColumnPrivilegeSet
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|partitionName
parameter_list|,
name|String
name|columnName
parameter_list|,
name|String
name|userName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|groupNames
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|MGlobalPrivilege
argument_list|>
name|listPrincipalGlobalGrants
parameter_list|(
name|String
name|principalName
parameter_list|,
name|PrincipalType
name|principalType
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|MDBPrivilege
argument_list|>
name|listPrincipalDBGrants
parameter_list|(
name|String
name|principalName
parameter_list|,
name|PrincipalType
name|principalType
parameter_list|,
name|String
name|dbName
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|MTablePrivilege
argument_list|>
name|listAllTableGrants
parameter_list|(
name|String
name|principalName
parameter_list|,
name|PrincipalType
name|principalType
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|MPartitionPrivilege
argument_list|>
name|listPrincipalPartitionGrants
parameter_list|(
name|String
name|principalName
parameter_list|,
name|PrincipalType
name|principalType
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|partName
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|MTableColumnPrivilege
argument_list|>
name|listPrincipalTableColumnGrants
parameter_list|(
name|String
name|principalName
parameter_list|,
name|PrincipalType
name|principalType
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|columnName
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|MPartitionColumnPrivilege
argument_list|>
name|listPrincipalPartitionColumnGrants
parameter_list|(
name|String
name|principalName
parameter_list|,
name|PrincipalType
name|principalType
parameter_list|,
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|partName
parameter_list|,
name|String
name|columnName
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|grantPrivileges
parameter_list|(
name|PrivilegeBag
name|privileges
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
throws|,
name|NoSuchObjectException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|revokePrivileges
parameter_list|(
name|PrivilegeBag
name|privileges
parameter_list|)
throws|throws
name|InvalidObjectException
throws|,
name|MetaException
throws|,
name|NoSuchObjectException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Role
name|getRole
parameter_list|(
name|String
name|roleName
parameter_list|)
throws|throws
name|NoSuchObjectException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|listRoleNames
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|MRoleMap
argument_list|>
name|listRoles
parameter_list|(
name|String
name|principalName
parameter_list|,
name|PrincipalType
name|principalType
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Partition
name|getPartitionWithAuth
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partVals
parameter_list|,
name|String
name|user_name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|group_names
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
throws|,
name|InvalidObjectException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Partition
argument_list|>
name|getPartitionsWithAuth
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|,
name|short
name|maxParts
parameter_list|,
name|String
name|userName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|groupNames
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
throws|,
name|InvalidObjectException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|listPartitionNamesPs
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|part_vals
parameter_list|,
name|short
name|max_parts
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Partition
argument_list|>
name|listPartitionsPsWithAuth
parameter_list|(
name|String
name|db_name
parameter_list|,
name|String
name|tbl_name
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|part_vals
parameter_list|,
name|short
name|max_parts
parameter_list|,
name|String
name|userName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|groupNames
parameter_list|)
throws|throws
name|MetaException
throws|,
name|InvalidObjectException
throws|,
name|NoSuchObjectException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|cleanupEvents
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|addToken
parameter_list|(
name|String
name|tokenIdentifier
parameter_list|,
name|String
name|delegationToken
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|removeToken
parameter_list|(
name|String
name|tokenIdentifier
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getToken
parameter_list|(
name|String
name|tokenIdentifier
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getAllTokenIdentifiers
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|addMasterKey
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateMasterKey
parameter_list|(
name|Integer
name|seqNo
parameter_list|,
name|String
name|key
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|boolean
name|removeMasterKey
parameter_list|(
name|Integer
name|keySeq
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getMasterKeys
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ColumnStatistics
name|getTableColumnStatistics
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|colName
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|deleteTableColumnStatistics
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|colName
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
throws|,
name|InvalidObjectException
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|deletePartitionColumnStatistics
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|partName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partVals
parameter_list|,
name|String
name|colName
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
throws|,
name|InvalidObjectException
throws|,
name|InvalidInputException
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|ColumnStatistics
name|getPartitionColumnStatistics
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|String
name|partName
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partVal
parameter_list|,
name|String
name|colName
parameter_list|)
throws|throws
name|MetaException
throws|,
name|NoSuchObjectException
throws|,
name|InvalidInputException
throws|,
name|InvalidObjectException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|updateTableColumnStatistics
parameter_list|(
name|ColumnStatistics
name|statsObj
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
throws|,
name|InvalidObjectException
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|updatePartitionColumnStatistics
parameter_list|(
name|ColumnStatistics
name|statsObj
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|partVals
parameter_list|)
throws|throws
name|NoSuchObjectException
throws|,
name|MetaException
throws|,
name|InvalidObjectException
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

