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
name|metastore
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableSet
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
name|metastore
operator|.
name|api
operator|.
name|BasicTxnInfo
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
name|CreationMetadata
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
name|Materialization
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
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|FixMethodOrder
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
name|runners
operator|.
name|MethodSorters
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
name|metastore
operator|.
name|Warehouse
operator|.
name|DEFAULT_CATALOG_NAME
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

begin_comment
comment|/**  * Unit tests for {@link org.apache.hadoop.hive.metastore.MaterializationsInvalidationCache}.  * The tests focus on arrival of notifications (possibly out of order) and the logic  * to clean up the materializations cache. Tests need to be executed in a certain order  * to avoid interactions among them, as the invalidation cache is a singleton.  */
end_comment

begin_class
annotation|@
name|FixMethodOrder
argument_list|(
name|MethodSorters
operator|.
name|NAME_ASCENDING
argument_list|)
specifier|public
class|class
name|TestMetaStoreMaterializationsCacheCleaner
block|{
specifier|private
specifier|static
specifier|final
name|String
name|DB_NAME
init|=
literal|"hive3252"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TBL_NAME_1
init|=
literal|"tmptbl1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TBL_NAME_2
init|=
literal|"tmptbl2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TBL_NAME_3
init|=
literal|"tmptbl3"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MV_NAME_1
init|=
literal|"mv1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MV_NAME_2
init|=
literal|"mv2"
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testCleanerScenario1
parameter_list|()
throws|throws
name|Exception
block|{
comment|// create mock raw store
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"metastore.materializations.invalidation.impl"
argument_list|,
literal|"DISABLE"
argument_list|)
expr_stmt|;
comment|// create mock handler
specifier|final
name|IHMSHandler
name|handler
init|=
name|mock
argument_list|(
name|IHMSHandler
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// initialize invalidation cache (set conf to disable)
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|init
argument_list|(
name|conf
argument_list|,
name|handler
argument_list|)
expr_stmt|;
comment|// This is a dummy test, invalidation cache is not supposed to
comment|// record any information.
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|int
name|id
init|=
literal|2
decl_stmt|;
name|BasicTxnInfo
name|txn2
init|=
name|createTxnInfo
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
name|id
argument_list|)
decl_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
name|id
argument_list|,
name|id
argument_list|)
expr_stmt|;
comment|// Create tbl2 (nothing to do)
name|id
operator|=
literal|3
expr_stmt|;
name|BasicTxnInfo
name|txn3
init|=
name|createTxnInfo
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
name|id
argument_list|)
decl_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_2
argument_list|,
name|id
argument_list|,
name|id
argument_list|)
expr_stmt|;
comment|// Cleanup (current = 4, duration = 4) -> Does nothing
name|long
name|removed
init|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|cleanup
argument_list|(
literal|0L
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|removed
argument_list|)
expr_stmt|;
comment|// Create mv1
name|Table
name|mv1
init|=
name|mock
argument_list|(
name|Table
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mv1
operator|.
name|getDbName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|DB_NAME
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mv1
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|MV_NAME_1
argument_list|)
expr_stmt|;
name|CreationMetadata
name|mockCM1
init|=
operator|new
name|CreationMetadata
argument_list|(
name|DEFAULT_CATALOG_NAME
argument_list|,
name|DB_NAME
argument_list|,
name|MV_NAME_1
argument_list|,
name|ImmutableSet
operator|.
name|of
argument_list|(
name|DB_NAME
operator|+
literal|"."
operator|+
name|TBL_NAME_1
argument_list|,
name|DB_NAME
operator|+
literal|"."
operator|+
name|TBL_NAME_2
argument_list|)
argument_list|)
decl_stmt|;
comment|// Create txn list (highWatermark=4;minOpenTxn=Long.MAX_VALUE)
name|mockCM1
operator|.
name|setValidTxnList
argument_list|(
literal|"3:"
operator|+
name|Long
operator|.
name|MAX_VALUE
operator|+
literal|"::"
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mv1
operator|.
name|getCreationMetadata
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockCM1
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|createMaterializedView
argument_list|(
name|mockCM1
operator|.
name|getDbName
argument_list|()
argument_list|,
name|mockCM1
operator|.
name|getTblName
argument_list|()
argument_list|,
name|mockCM1
operator|.
name|getTablesUsed
argument_list|()
argument_list|,
name|mockCM1
operator|.
name|getValidTxnList
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Materialization
argument_list|>
name|invalidationInfos
init|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|getMaterializationInvalidationInfo
argument_list|(
name|DB_NAME
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|MV_NAME_1
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|invalidationInfos
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|id
operator|=
literal|10
expr_stmt|;
name|BasicTxnInfo
name|txn10
init|=
name|createTxnInfo
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_2
argument_list|,
name|id
argument_list|)
decl_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_2
argument_list|,
name|id
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|id
operator|=
literal|9
expr_stmt|;
name|BasicTxnInfo
name|txn9
init|=
name|createTxnInfo
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
name|id
argument_list|)
decl_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
name|id
argument_list|,
name|id
argument_list|)
expr_stmt|;
comment|// Cleanup (current = 12, duration = 4) -> Removes txn1, txn2, txn3
name|removed
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|cleanup
argument_list|(
literal|8L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|removed
argument_list|)
expr_stmt|;
name|invalidationInfos
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|getMaterializationInvalidationInfo
argument_list|(
name|DB_NAME
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|MV_NAME_1
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|invalidationInfos
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// Create mv2
name|Table
name|mv2
init|=
name|mock
argument_list|(
name|Table
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mv2
operator|.
name|getDbName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|DB_NAME
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mv2
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|MV_NAME_2
argument_list|)
expr_stmt|;
name|CreationMetadata
name|mockCM2
init|=
operator|new
name|CreationMetadata
argument_list|(
name|DEFAULT_CATALOG_NAME
argument_list|,
name|DB_NAME
argument_list|,
name|MV_NAME_2
argument_list|,
name|ImmutableSet
operator|.
name|of
argument_list|(
name|DB_NAME
operator|+
literal|"."
operator|+
name|TBL_NAME_1
argument_list|,
name|DB_NAME
operator|+
literal|"."
operator|+
name|TBL_NAME_2
argument_list|)
argument_list|)
decl_stmt|;
comment|// Create txn list (highWatermark=10;minOpenTxn=Long.MAX_VALUE)
name|mockCM2
operator|.
name|setValidTxnList
argument_list|(
literal|"10:"
operator|+
name|Long
operator|.
name|MAX_VALUE
operator|+
literal|"::"
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mv2
operator|.
name|getCreationMetadata
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockCM2
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|createMaterializedView
argument_list|(
name|mockCM2
operator|.
name|getDbName
argument_list|()
argument_list|,
name|mockCM2
operator|.
name|getTblName
argument_list|()
argument_list|,
name|mockCM2
operator|.
name|getTablesUsed
argument_list|()
argument_list|,
name|mockCM2
operator|.
name|getValidTxnList
argument_list|()
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mv2
operator|.
name|getCreationMetadata
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockCM2
argument_list|)
expr_stmt|;
name|invalidationInfos
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|getMaterializationInvalidationInfo
argument_list|(
name|DB_NAME
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|MV_NAME_1
argument_list|,
name|MV_NAME_2
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|invalidationInfos
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// Create tbl3 (nothing to do)
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_3
argument_list|,
literal|11
argument_list|,
literal|11
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_3
argument_list|,
literal|18
argument_list|,
literal|18
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
literal|14
argument_list|,
literal|14
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
literal|17
argument_list|,
literal|17
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_2
argument_list|,
literal|16
argument_list|,
literal|16
argument_list|)
expr_stmt|;
comment|// Cleanup (current = 20, duration = 4) -> Removes txn10, txn11
name|removed
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|cleanup
argument_list|(
literal|16L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|removed
argument_list|)
expr_stmt|;
name|invalidationInfos
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|getMaterializationInvalidationInfo
argument_list|(
name|DB_NAME
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|MV_NAME_1
argument_list|,
name|MV_NAME_2
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|invalidationInfos
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_2
argument_list|,
literal|15
argument_list|,
literal|15
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_2
argument_list|,
literal|7
argument_list|,
literal|7
argument_list|)
expr_stmt|;
name|invalidationInfos
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|getMaterializationInvalidationInfo
argument_list|(
name|DB_NAME
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|MV_NAME_1
argument_list|,
name|MV_NAME_2
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|invalidationInfos
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// Cleanup (current = 24, duration = 4) -> Removes txn9, txn14, txn15, txn16, txn17, txn18
name|removed
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|cleanup
argument_list|(
literal|20L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|removed
argument_list|)
expr_stmt|;
name|invalidationInfos
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|getMaterializationInvalidationInfo
argument_list|(
name|DB_NAME
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|MV_NAME_1
argument_list|,
name|MV_NAME_2
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|invalidationInfos
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// Cleanup (current = 28, duration = 4) -> Removes txn9
name|removed
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|cleanup
argument_list|(
literal|24L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|removed
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCleanerScenario2
parameter_list|()
throws|throws
name|Exception
block|{
comment|// create mock raw store
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"metastore.materializations.invalidation.impl"
argument_list|,
literal|"DEFAULT"
argument_list|)
expr_stmt|;
comment|// create mock handler
specifier|final
name|IHMSHandler
name|handler
init|=
name|mock
argument_list|(
name|IHMSHandler
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// initialize invalidation cache (set conf to default)
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|init
argument_list|(
name|conf
argument_list|,
name|handler
argument_list|)
expr_stmt|;
comment|// Scenario consists of the following steps:
comment|// Create tbl1
comment|// (t = 1) Insert row in tbl1
comment|// (t = 2) Insert row in tbl1
comment|// Create tbl2
comment|// (t = 3) Insert row in tbl2
comment|// Cleanup (current = 4, duration = 4) -> Does nothing
comment|// Create mv1
comment|// (t = 10) Insert row in tbl2
comment|// (t = 9) Insert row in tbl1 (out of order)
comment|// Cleanup (current = 12, duration = 4) -> Removes txn1, txn2, txn3
comment|// Create mv2
comment|// Create tbl3
comment|// (t = 11) Insert row in tbl3
comment|// (t = 18) Insert row in tbl3
comment|// (t = 14) Insert row in tbl1
comment|// (t = 17) Insert row in tbl1
comment|// (t = 16) Insert row in tbl2
comment|// Cleanup (current = 20, duration = 4) -> Removes txn10, txn11
comment|// (t = 12) Insert row in tbl1
comment|// (t = 15) Insert row in tbl2
comment|// (t = 7) Insert row in tbl2
comment|// Cleanup (current = 24, duration = 4) -> Removes txn9, txn14, txn15, txn16, txn17, txn18
comment|// Create tbl1 (nothing to do)
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|int
name|id
init|=
literal|2
decl_stmt|;
name|BasicTxnInfo
name|txn2
init|=
name|createTxnInfo
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
name|id
argument_list|)
decl_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
name|id
argument_list|,
name|id
argument_list|)
expr_stmt|;
comment|// Create tbl2 (nothing to do)
name|id
operator|=
literal|3
expr_stmt|;
name|BasicTxnInfo
name|txn3
init|=
name|createTxnInfo
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
name|id
argument_list|)
decl_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_2
argument_list|,
name|id
argument_list|,
name|id
argument_list|)
expr_stmt|;
comment|// Cleanup (current = 4, duration = 4) -> Does nothing
name|long
name|removed
init|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|cleanup
argument_list|(
literal|0L
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|removed
argument_list|)
expr_stmt|;
comment|// Create mv1
name|Table
name|mv1
init|=
name|mock
argument_list|(
name|Table
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mv1
operator|.
name|getDbName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|DB_NAME
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mv1
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|MV_NAME_1
argument_list|)
expr_stmt|;
name|CreationMetadata
name|mockCM1
init|=
operator|new
name|CreationMetadata
argument_list|(
name|DEFAULT_CATALOG_NAME
argument_list|,
name|DB_NAME
argument_list|,
name|MV_NAME_1
argument_list|,
name|ImmutableSet
operator|.
name|of
argument_list|(
name|DB_NAME
operator|+
literal|"."
operator|+
name|TBL_NAME_1
argument_list|,
name|DB_NAME
operator|+
literal|"."
operator|+
name|TBL_NAME_2
argument_list|)
argument_list|)
decl_stmt|;
comment|// Create txn list (highWatermark=4;minOpenTxn=Long.MAX_VALUE)
name|mockCM1
operator|.
name|setValidTxnList
argument_list|(
literal|"3:"
operator|+
name|Long
operator|.
name|MAX_VALUE
operator|+
literal|"::"
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mv1
operator|.
name|getCreationMetadata
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockCM1
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|createMaterializedView
argument_list|(
name|mockCM1
operator|.
name|getDbName
argument_list|()
argument_list|,
name|mockCM1
operator|.
name|getTblName
argument_list|()
argument_list|,
name|mockCM1
operator|.
name|getTablesUsed
argument_list|()
argument_list|,
name|mockCM1
operator|.
name|getValidTxnList
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Materialization
argument_list|>
name|invalidationInfos
init|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|getMaterializationInvalidationInfo
argument_list|(
name|DB_NAME
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|MV_NAME_1
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|invalidationInfos
operator|.
name|get
argument_list|(
name|MV_NAME_1
argument_list|)
operator|.
name|getInvalidationTime
argument_list|()
argument_list|)
expr_stmt|;
name|id
operator|=
literal|10
expr_stmt|;
name|BasicTxnInfo
name|txn10
init|=
name|createTxnInfo
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_2
argument_list|,
name|id
argument_list|)
decl_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_2
argument_list|,
name|id
argument_list|,
name|id
argument_list|)
expr_stmt|;
name|id
operator|=
literal|9
expr_stmt|;
name|BasicTxnInfo
name|txn9
init|=
name|createTxnInfo
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
name|id
argument_list|)
decl_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
name|id
argument_list|,
name|id
argument_list|)
expr_stmt|;
comment|// Cleanup (current = 12, duration = 4) -> Removes txn1, txn2, txn3
name|removed
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|cleanup
argument_list|(
literal|8L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3L
argument_list|,
name|removed
argument_list|)
expr_stmt|;
name|invalidationInfos
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|getMaterializationInvalidationInfo
argument_list|(
name|DB_NAME
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|MV_NAME_1
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|9L
argument_list|,
name|invalidationInfos
operator|.
name|get
argument_list|(
name|MV_NAME_1
argument_list|)
operator|.
name|getInvalidationTime
argument_list|()
argument_list|)
expr_stmt|;
comment|// Create mv2
name|Table
name|mv2
init|=
name|mock
argument_list|(
name|Table
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mv2
operator|.
name|getDbName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|DB_NAME
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mv2
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|MV_NAME_2
argument_list|)
expr_stmt|;
name|CreationMetadata
name|mockCM2
init|=
operator|new
name|CreationMetadata
argument_list|(
name|DEFAULT_CATALOG_NAME
argument_list|,
name|DB_NAME
argument_list|,
name|MV_NAME_2
argument_list|,
name|ImmutableSet
operator|.
name|of
argument_list|(
name|DB_NAME
operator|+
literal|"."
operator|+
name|TBL_NAME_1
argument_list|,
name|DB_NAME
operator|+
literal|"."
operator|+
name|TBL_NAME_2
argument_list|)
argument_list|)
decl_stmt|;
comment|// Create txn list (highWatermark=10;minOpenTxn=Long.MAX_VALUE)
name|mockCM2
operator|.
name|setValidTxnList
argument_list|(
literal|"10:"
operator|+
name|Long
operator|.
name|MAX_VALUE
operator|+
literal|"::"
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mv2
operator|.
name|getCreationMetadata
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockCM2
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|createMaterializedView
argument_list|(
name|mockCM2
operator|.
name|getDbName
argument_list|()
argument_list|,
name|mockCM2
operator|.
name|getTblName
argument_list|()
argument_list|,
name|mockCM2
operator|.
name|getTablesUsed
argument_list|()
argument_list|,
name|mockCM2
operator|.
name|getValidTxnList
argument_list|()
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|mv2
operator|.
name|getCreationMetadata
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockCM2
argument_list|)
expr_stmt|;
name|invalidationInfos
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|getMaterializationInvalidationInfo
argument_list|(
name|DB_NAME
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|MV_NAME_1
argument_list|,
name|MV_NAME_2
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|9L
argument_list|,
name|invalidationInfos
operator|.
name|get
argument_list|(
name|MV_NAME_1
argument_list|)
operator|.
name|getInvalidationTime
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|invalidationInfos
operator|.
name|get
argument_list|(
name|MV_NAME_2
argument_list|)
operator|.
name|getInvalidationTime
argument_list|()
argument_list|)
expr_stmt|;
comment|// Create tbl3 (nothing to do)
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_3
argument_list|,
literal|11
argument_list|,
literal|11
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_3
argument_list|,
literal|18
argument_list|,
literal|18
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
literal|14
argument_list|,
literal|14
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
literal|17
argument_list|,
literal|17
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_2
argument_list|,
literal|16
argument_list|,
literal|16
argument_list|)
expr_stmt|;
comment|// Cleanup (current = 20, duration = 4) -> Removes txn10, txn11
name|removed
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|cleanup
argument_list|(
literal|16L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2L
argument_list|,
name|removed
argument_list|)
expr_stmt|;
name|invalidationInfos
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|getMaterializationInvalidationInfo
argument_list|(
name|DB_NAME
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|MV_NAME_1
argument_list|,
name|MV_NAME_2
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|9L
argument_list|,
name|invalidationInfos
operator|.
name|get
argument_list|(
name|MV_NAME_1
argument_list|)
operator|.
name|getInvalidationTime
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|14L
argument_list|,
name|invalidationInfos
operator|.
name|get
argument_list|(
name|MV_NAME_2
argument_list|)
operator|.
name|getInvalidationTime
argument_list|()
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_1
argument_list|,
literal|12
argument_list|,
literal|12
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_2
argument_list|,
literal|15
argument_list|,
literal|15
argument_list|)
expr_stmt|;
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|notifyTableModification
argument_list|(
name|DB_NAME
argument_list|,
name|TBL_NAME_2
argument_list|,
literal|7
argument_list|,
literal|7
argument_list|)
expr_stmt|;
name|invalidationInfos
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|getMaterializationInvalidationInfo
argument_list|(
name|DB_NAME
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|MV_NAME_1
argument_list|,
name|MV_NAME_2
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|7L
argument_list|,
name|invalidationInfos
operator|.
name|get
argument_list|(
name|MV_NAME_1
argument_list|)
operator|.
name|getInvalidationTime
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|12L
argument_list|,
name|invalidationInfos
operator|.
name|get
argument_list|(
name|MV_NAME_2
argument_list|)
operator|.
name|getInvalidationTime
argument_list|()
argument_list|)
expr_stmt|;
comment|// Cleanup (current = 24, duration = 4) -> Removes txn9, txn14, txn15, txn16, txn17, txn18
name|removed
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|cleanup
argument_list|(
literal|20L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|6L
argument_list|,
name|removed
argument_list|)
expr_stmt|;
name|invalidationInfos
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|getMaterializationInvalidationInfo
argument_list|(
name|DB_NAME
argument_list|,
name|ImmutableList
operator|.
name|of
argument_list|(
name|MV_NAME_1
argument_list|,
name|MV_NAME_2
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|7L
argument_list|,
name|invalidationInfos
operator|.
name|get
argument_list|(
name|MV_NAME_1
argument_list|)
operator|.
name|getInvalidationTime
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|12L
argument_list|,
name|invalidationInfos
operator|.
name|get
argument_list|(
name|MV_NAME_2
argument_list|)
operator|.
name|getInvalidationTime
argument_list|()
argument_list|)
expr_stmt|;
comment|// Cleanup (current = 28, duration = 4) -> Removes txn9
name|removed
operator|=
name|MaterializationsInvalidationCache
operator|.
name|get
argument_list|()
operator|.
name|cleanup
argument_list|(
literal|24L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|removed
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|BasicTxnInfo
name|createTxnInfo
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tableName
parameter_list|,
name|int
name|i
parameter_list|)
block|{
name|BasicTxnInfo
name|r
init|=
operator|new
name|BasicTxnInfo
argument_list|()
decl_stmt|;
name|r
operator|.
name|setDbname
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|r
operator|.
name|setTablename
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|r
operator|.
name|setTxnid
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|r
operator|.
name|setTime
argument_list|(
name|i
argument_list|)
expr_stmt|;
return|return
name|r
return|;
block|}
block|}
end_class

end_unit

