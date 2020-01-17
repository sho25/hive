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
name|metadata
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
name|IMetaStoreClient
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
name|TestMetastoreExpr
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
name|annotation
operator|.
name|MetastoreCheckinTest
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
name|PrivilegeGrantInfo
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
name|client
operator|.
name|CustomIgnoreRule
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
name|client
operator|.
name|TestListPartitions
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
name|client
operator|.
name|builder
operator|.
name|PartitionBuilder
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
name|client
operator|.
name|builder
operator|.
name|TableBuilder
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
name|minihms
operator|.
name|AbstractMetaStoreService
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
name|SerializationUtilities
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
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
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
name|junit
operator|.
name|framework
operator|.
name|TestCase
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
import|;
end_import

begin_comment
comment|/**  * Test class for list partitions related methods on temporary tables.  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
name|MetastoreCheckinTest
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestSessionHiveMetastoreClientListPartitionsTempTable
extends|extends
name|TestListPartitions
block|{
specifier|private
name|HiveConf
name|conf
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PART_PRIV
init|=
literal|"PARTITION_LEVEL_PRIVILEGE"
decl_stmt|;
specifier|public
name|TestSessionHiveMetastoreClientListPartitionsTempTable
parameter_list|(
name|String
name|name
parameter_list|,
name|AbstractMetaStoreService
name|metaStore
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|metaStore
argument_list|)
expr_stmt|;
name|ignoreRule
operator|=
operator|new
name|CustomIgnoreRule
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|initHiveConf
argument_list|()
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|setClient
argument_list|(
name|Hive
operator|.
name|get
argument_list|(
name|conf
argument_list|)
operator|.
name|getMSC
argument_list|()
argument_list|)
expr_stmt|;
name|getClient
argument_list|()
operator|.
name|dropDatabase
argument_list|(
name|DB_NAME
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|getMetaStore
argument_list|()
operator|.
name|cleanWarehouseDirs
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|initHiveConf
parameter_list|()
throws|throws
name|HiveException
block|{
name|conf
operator|=
name|Hive
operator|.
name|get
argument_list|()
operator|.
name|getConf
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setBoolVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTORE_FASTPATH
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Table
name|createTestTable
parameter_list|(
name|IMetaStoreClient
name|client
parameter_list|,
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
name|partCols
parameter_list|,
name|boolean
name|setPartitionLevelPrivileges
parameter_list|)
throws|throws
name|TException
block|{
name|TableBuilder
name|builder
init|=
operator|new
name|TableBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"id"
argument_list|,
literal|"int"
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"name"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|setTemporary
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|partCols
operator|.
name|forEach
argument_list|(
name|col
lambda|->
name|builder
operator|.
name|addPartCol
argument_list|(
name|col
argument_list|,
literal|"string"
argument_list|)
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|builder
operator|.
name|build
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|setPartitionLevelPrivileges
condition|)
block|{
name|table
operator|.
name|putToParameters
argument_list|(
name|PART_PRIV
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
name|client
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
return|return
name|table
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addPartition
parameter_list|(
name|IMetaStoreClient
name|client
parameter_list|,
name|Table
name|table
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|values
parameter_list|)
throws|throws
name|TException
block|{
name|PartitionBuilder
name|builder
init|=
operator|new
name|PartitionBuilder
argument_list|()
operator|.
name|inTable
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|values
operator|.
name|forEach
argument_list|(
name|builder
operator|::
name|addValue
argument_list|)
expr_stmt|;
name|Partition
name|partition
init|=
name|builder
operator|.
name|build
argument_list|(
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|containsKey
argument_list|(
name|PART_PRIV
argument_list|)
operator|&&
name|table
operator|.
name|getParameters
argument_list|()
operator|.
name|get
argument_list|(
name|PART_PRIV
argument_list|)
operator|.
name|equals
argument_list|(
literal|"true"
argument_list|)
condition|)
block|{
name|PrincipalPrivilegeSet
name|privileges
init|=
operator|new
name|PrincipalPrivilegeSet
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|PrivilegeGrantInfo
argument_list|>
argument_list|>
name|userPrivileges
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|userPrivileges
operator|.
name|put
argument_list|(
name|USER_NAME
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
name|privileges
operator|.
name|setUserPrivileges
argument_list|(
name|userPrivileges
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|PrivilegeGrantInfo
argument_list|>
argument_list|>
name|groupPrivileges
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|groupPrivileges
operator|.
name|put
argument_list|(
name|GROUP
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
name|privileges
operator|.
name|setGroupPrivileges
argument_list|(
name|groupPrivileges
argument_list|)
expr_stmt|;
name|partition
operator|.
name|setPrivileges
argument_list|(
name|privileges
argument_list|)
expr_stmt|;
block|}
name|client
operator|.
name|add_partition
argument_list|(
name|partition
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|assertAuthInfoReturned
parameter_list|(
name|String
name|userName
parameter_list|,
name|String
name|group
parameter_list|,
name|Partition
name|partition
parameter_list|)
block|{
name|PrincipalPrivilegeSet
name|privileges
init|=
name|partition
operator|.
name|getPrivileges
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|privileges
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|privileges
operator|.
name|getUserPrivileges
argument_list|()
operator|.
name|containsKey
argument_list|(
name|userName
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|privileges
operator|.
name|getGroupPrivileges
argument_list|()
operator|.
name|containsKey
argument_list|(
name|group
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|testListPartitionsAllNullTblName
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testListPartitionsAllNullTblName
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|testListPartitionsAllNullDbName
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testListPartitionsAllNullDbName
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|testListPartitionSpecsNullTblName
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testListPartitionSpecsNullTblName
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|testListPartitionsWithAuthByValuesNullDbName
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testListPartitionsWithAuthByValuesNullDbName
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|testListPartitionsWithAuthByValuesNullTblName
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testListPartitionsWithAuthByValuesNullTblName
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|testListPartitionNamesNullDbName
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testListPartitionNamesNullDbName
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|testListPartitionNamesNullTblName
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testListPartitionNamesNullTblName
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|testListPartitionNamesByValuesNullDbName
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testListPartitionNamesByValuesNullDbName
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|testListPartitionNamesByValuesNullTblName
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testListPartitionNamesByValuesNullTblName
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|testListPartitionsByFilterNullTblName
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testListPartitionsByFilterNullTblName
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|testListPartitionsByFilterNullDbName
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testListPartitionsByFilterNullDbName
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|testListPartitionValuesNullDbName
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testListPartitionValuesNullDbName
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|testListPartitionValuesNullTblName
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testListPartitionValuesNullTblName
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|NoSuchObjectException
operator|.
name|class
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|testListPartitionNamesNoDb
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testListPartitionNamesNoDb
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testListPartitionsAllNoTable
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|testListPartitionsAllNoTable
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListPartitionsByExpr
parameter_list|()
throws|throws
name|Exception
block|{
name|createTable4PartColsParts
argument_list|(
name|getClient
argument_list|()
argument_list|)
expr_stmt|;
name|TestMetastoreExpr
operator|.
name|ExprBuilder
name|e
init|=
operator|new
name|TestMetastoreExpr
operator|.
name|ExprBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|checkExpr
argument_list|(
literal|2
argument_list|,
name|e
operator|.
name|strCol
argument_list|(
literal|"yyyy"
argument_list|)
operator|.
name|val
argument_list|(
literal|"2017"
argument_list|)
operator|.
name|pred
argument_list|(
literal|"="
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|checkExpr
argument_list|(
literal|3
argument_list|,
name|e
operator|.
name|strCol
argument_list|(
literal|"mm"
argument_list|)
operator|.
name|val
argument_list|(
literal|"11"
argument_list|)
operator|.
name|pred
argument_list|(
literal|">"
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|checkExpr
argument_list|(
literal|4
argument_list|,
name|e
operator|.
name|strCol
argument_list|(
literal|"dd"
argument_list|)
operator|.
name|val
argument_list|(
literal|"29"
argument_list|)
operator|.
name|pred
argument_list|(
literal|">="
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|checkExpr
argument_list|(
literal|2
argument_list|,
name|e
operator|.
name|strCol
argument_list|(
literal|"yyyy"
argument_list|)
operator|.
name|val
argument_list|(
literal|"2017"
argument_list|)
operator|.
name|pred
argument_list|(
literal|"!="
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|checkExpr
argument_list|(
literal|1
argument_list|,
name|e
operator|.
name|strCol
argument_list|(
literal|"yyyy"
argument_list|)
operator|.
name|val
argument_list|(
literal|"2017"
argument_list|)
operator|.
name|pred
argument_list|(
literal|"="
argument_list|,
literal|2
argument_list|)
operator|.
name|strCol
argument_list|(
literal|"mm"
argument_list|)
operator|.
name|val
argument_list|(
literal|"10"
argument_list|)
operator|.
name|pred
argument_list|(
literal|">="
argument_list|,
literal|2
argument_list|)
operator|.
name|pred
argument_list|(
literal|"and"
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|checkExpr
argument_list|(
literal|3
argument_list|,
name|e
operator|.
name|strCol
argument_list|(
literal|"dd"
argument_list|)
operator|.
name|val
argument_list|(
literal|"10"
argument_list|)
operator|.
name|pred
argument_list|(
literal|"<"
argument_list|,
literal|2
argument_list|)
operator|.
name|strCol
argument_list|(
literal|"yyyy"
argument_list|)
operator|.
name|val
argument_list|(
literal|"2009"
argument_list|)
operator|.
name|pred
argument_list|(
literal|"!="
argument_list|,
literal|2
argument_list|)
operator|.
name|pred
argument_list|(
literal|"or"
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|checkExpr
argument_list|(
literal|0
argument_list|,
name|e
operator|.
name|strCol
argument_list|(
literal|"yyyy"
argument_list|)
operator|.
name|val
argument_list|(
literal|"2019"
argument_list|)
operator|.
name|pred
argument_list|(
literal|"="
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|AssertionError
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testListPartitionsByExprNullResult
parameter_list|()
throws|throws
name|Exception
block|{
name|createTable4PartColsParts
argument_list|(
name|getClient
argument_list|()
argument_list|)
expr_stmt|;
name|TestMetastoreExpr
operator|.
name|ExprBuilder
name|e
init|=
operator|new
name|TestMetastoreExpr
operator|.
name|ExprBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|getClient
argument_list|()
operator|.
name|listPartitionsByExpr
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|,
name|SerializationUtilities
operator|.
name|serializeExpressionToKryo
argument_list|(
name|e
operator|.
name|strCol
argument_list|(
literal|"yyyy"
argument_list|)
operator|.
name|val
argument_list|(
literal|"2017"
argument_list|)
operator|.
name|pred
argument_list|(
literal|"="
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListPartitionsByExprDefMaxParts
parameter_list|()
throws|throws
name|Exception
block|{
name|createTable4PartColsParts
argument_list|(
name|getClient
argument_list|()
argument_list|)
expr_stmt|;
name|TestMetastoreExpr
operator|.
name|ExprBuilder
name|e
init|=
operator|new
name|TestMetastoreExpr
operator|.
name|ExprBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|getClient
argument_list|()
operator|.
name|listPartitionsByExpr
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|,
name|SerializationUtilities
operator|.
name|serializeExpressionToKryo
argument_list|(
name|e
operator|.
name|strCol
argument_list|(
literal|"yyyy"
argument_list|)
operator|.
name|val
argument_list|(
literal|"2017"
argument_list|)
operator|.
name|pred
argument_list|(
literal|">="
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|,
operator|(
name|short
operator|)
literal|3
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testListPartitionsByExprHighMaxParts
parameter_list|()
throws|throws
name|Exception
block|{
name|createTable4PartColsParts
argument_list|(
name|getClient
argument_list|()
argument_list|)
expr_stmt|;
name|TestMetastoreExpr
operator|.
name|ExprBuilder
name|e
init|=
operator|new
name|TestMetastoreExpr
operator|.
name|ExprBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|getClient
argument_list|()
operator|.
name|listPartitionsByExpr
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|,
name|SerializationUtilities
operator|.
name|serializeExpressionToKryo
argument_list|(
name|e
operator|.
name|strCol
argument_list|(
literal|"yyyy"
argument_list|)
operator|.
name|val
argument_list|(
literal|"2017"
argument_list|)
operator|.
name|pred
argument_list|(
literal|">="
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|,
operator|(
name|short
operator|)
literal|100
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|result
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|NoSuchObjectException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testListPartitionsByExprNoDb
parameter_list|()
throws|throws
name|Exception
block|{
name|getClient
argument_list|()
operator|.
name|dropDatabase
argument_list|(
name|DB_NAME
argument_list|)
expr_stmt|;
name|getClient
argument_list|()
operator|.
name|listPartitionsByExpr
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|'f'
block|,
literal|'o'
block|,
literal|'o'
block|}
argument_list|,
literal|null
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testListPartitionsByExprNoTbl
parameter_list|()
throws|throws
name|Exception
block|{
name|getClient
argument_list|()
operator|.
name|listPartitionsByExpr
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|'f'
block|,
literal|'o'
block|,
literal|'o'
block|}
argument_list|,
literal|null
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testListPartitionsByExprEmptyDbName
parameter_list|()
throws|throws
name|Exception
block|{
name|getClient
argument_list|()
operator|.
name|listPartitionsByExpr
argument_list|(
literal|""
argument_list|,
name|TABLE_NAME
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|'f'
block|,
literal|'o'
block|,
literal|'o'
block|}
argument_list|,
literal|null
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testListPartitionsByExprEmptyTblName
parameter_list|()
throws|throws
name|Exception
block|{
name|createTable3PartCols1Part
argument_list|(
name|getClient
argument_list|()
argument_list|)
expr_stmt|;
name|getClient
argument_list|()
operator|.
name|listPartitionsByExpr
argument_list|(
name|DB_NAME
argument_list|,
literal|""
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|'f'
block|,
literal|'o'
block|,
literal|'o'
block|}
argument_list|,
literal|null
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testListPartitionsByExprNullDbName
parameter_list|()
throws|throws
name|Exception
block|{
name|getClient
argument_list|()
operator|.
name|listPartitionsByExpr
argument_list|(
literal|null
argument_list|,
name|TABLE_NAME
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|'f'
block|,
literal|'o'
block|,
literal|'o'
block|}
argument_list|,
literal|null
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|MetaException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testListPartitionsByExprNullTblName
parameter_list|()
throws|throws
name|Exception
block|{
name|getClient
argument_list|()
operator|.
name|listPartitionsByExpr
argument_list|(
name|DB_NAME
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|'f'
block|,
literal|'o'
block|,
literal|'o'
block|}
argument_list|,
literal|null
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkExpr
parameter_list|(
name|int
name|numParts
parameter_list|,
name|ExprNodeGenericFuncDesc
name|expr
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Partition
argument_list|>
name|parts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|getClient
argument_list|()
operator|.
name|listPartitionsByExpr
argument_list|(
name|DB_NAME
argument_list|,
name|TABLE_NAME
argument_list|,
name|SerializationUtilities
operator|.
name|serializeExpressionToKryo
argument_list|(
name|expr
argument_list|)
argument_list|,
literal|null
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|,
name|parts
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Partition check failed: "
operator|+
name|expr
operator|.
name|getExprString
argument_list|()
argument_list|,
name|numParts
argument_list|,
name|parts
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

