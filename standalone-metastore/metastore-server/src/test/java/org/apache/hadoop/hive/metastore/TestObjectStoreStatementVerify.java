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
name|java
operator|.
name|sql
operator|.
name|DriverManager
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|SQLException
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|annotation
operator|.
name|MetastoreUnitTest
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
name|TableMeta
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
name|conf
operator|.
name|MetastoreConf
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
name|BeforeClass
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

begin_class
annotation|@
name|Category
argument_list|(
name|MetastoreUnitTest
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestObjectStoreStatementVerify
block|{
specifier|private
name|ObjectStore
name|objectStore
init|=
literal|null
decl_stmt|;
specifier|private
name|Configuration
name|conf
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|String
name|DB1
init|=
literal|"db1"
decl_stmt|;
specifier|private
specifier|final
name|String
name|TBL1
init|=
literal|"db1_tbl1"
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|oneTimeSetup
parameter_list|()
throws|throws
name|SQLException
block|{
name|DriverManager
operator|.
name|registerDriver
argument_list|(
operator|new
name|StatementVerifyingDerby
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ObjectStore
name|createObjectStore
parameter_list|()
throws|throws
name|MetaException
throws|,
name|InvalidOperationException
block|{
name|conf
operator|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
expr_stmt|;
name|MetastoreConf
operator|.
name|setBoolVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|HIVE_IN_TEST
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CONNECTION_DRIVER
argument_list|,
name|StatementVerifyingDerby
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|jdbcUrl
init|=
name|MetastoreConf
operator|.
name|getVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CONNECT_URL_KEY
argument_list|)
decl_stmt|;
name|jdbcUrl
operator|=
name|jdbcUrl
operator|.
name|replace
argument_list|(
literal|"derby"
argument_list|,
literal|"sderby"
argument_list|)
expr_stmt|;
name|MetastoreConf
operator|.
name|setVar
argument_list|(
name|conf
argument_list|,
name|MetastoreConf
operator|.
name|ConfVars
operator|.
name|CONNECT_URL_KEY
argument_list|,
name|jdbcUrl
argument_list|)
expr_stmt|;
name|MetaStoreTestUtils
operator|.
name|setConfForStandloneMode
argument_list|(
name|conf
argument_list|)
expr_stmt|;
specifier|final
name|ObjectStore
name|objectStore
init|=
operator|new
name|ObjectStore
argument_list|()
decl_stmt|;
name|objectStore
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|HiveMetaStore
operator|.
name|HMSHandler
operator|.
name|createDefaultCatalog
argument_list|(
name|objectStore
argument_list|,
operator|new
name|Warehouse
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|objectStore
return|;
block|}
comment|// This function is called during the prepare statement step of object retrieval through DN
specifier|static
name|void
name|verifySql
parameter_list|(
specifier|final
name|String
name|sql
parameter_list|)
block|{
if|if
condition|(
name|sql
operator|.
name|contains
argument_list|(
literal|"SELECT DISTINCT 'org.apache.hadoop.hive.metastore.model.MTable' AS"
argument_list|)
operator|||
name|sql
operator|.
name|contains
argument_list|(
literal|"SELECT 'org.apache.hadoop.hive.metastore.model.MTable' AS"
argument_list|)
condition|)
block|{
name|verifyMTableDBFetchGroup
argument_list|(
name|sql
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|verifyMTableDBFetchGroup
parameter_list|(
specifier|final
name|String
name|sql
parameter_list|)
block|{
comment|// We want to validate that when an MTable is fetched, we join the DBS table and fetch
comment|// the database info as well. For example, if we don't use a proper fetch plan, the DN query
comment|// would be something like
comment|//
comment|// SELECT DISTINCT 'org.apache.hadoop.hive.metastore.model.MTable' AS
comment|//   NUCLEUS_TYPE, A0.CREATE_TIME, A0.TBL_ID, A0.LAST_ACCESS_TIME, A0.OWNER, A0.OWNER_TYPE,
comment|//   A0.RETENTION, A0.IS_REWRITE_ENABLED, A0.TBL_NAME, A0.TBL_TYPE, A0.WRITE_ID
comment|//   FROM TBLS A0
comment|//   LEFT OUTER JOIN DBS B0 ON A0.DB_ID = B0.DB_ID WHERE B0.CTLG_NAME = ?
comment|//
comment|// Note in the above query that we never pick anything from the DBS table!
comment|//
comment|// If you have a good fetch plan, your query should be something like
comment|//
comment|// SELECT DISTINCT 'org.apache.hadoop.hive.metastore.model.MTable' AS
comment|//   NUCLEUS_TYPE, A0.CREATE_TIME, C0.CTLG_NAME, C0."DESC", C0.DB_LOCATION_URI, C0."NAME",
comment|//   C0.OWNER_NAME, C0.OWNER_TYPE, C0.DB_ID, A0.TBL_ID, A0.LAST_ACCESS_TIME, A0.OWNER,
comment|//   A0.OWNER_TYPE, A0.RETENTION, A0.IS_REWRITE_ENABLED, A0.TBL_NAME, A0.TBL_TYPE, A0.WRITE_ID
comment|//   FROM TBLS A0 LEFT OUTER JOIN DBS B0 ON A0.DB_ID = B0.DB_ID
comment|//   LEFT OUTER JOIN DBS C0 ON A0.DB_ID = C0.DB_ID WHERE B0.CTLG_NAME = ?
comment|//
comment|// Notice that we pick the DB_ID, OWNER_TYPE, NAME, DESC etc from the DBS table. This is the
comment|// correct behavior.
comment|// Step 1. Find the identifiers for the DBS database by matching on "JOIN DBS (xx) ON"
name|Pattern
name|sqlPatternDb
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"JOIN\\ DBS\\ ([a-zA-Z0-9]+)\\ ON"
argument_list|)
decl_stmt|;
name|Matcher
name|matcher
init|=
name|sqlPatternDb
operator|.
name|matcher
argument_list|(
name|sql
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|dbIdentifiers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|matcher
operator|.
name|find
argument_list|()
condition|)
block|{
name|dbIdentifiers
operator|.
name|add
argument_list|(
name|matcher
operator|.
name|group
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Step 2. Now there should a string with the db identifier which picks the NAME field from
comment|// databases. If we don't find this, then we did not join in the database info.
name|boolean
name|confirmedDbNameRetrieval
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|dbIdenfier
range|:
name|dbIdentifiers
control|)
block|{
if|if
condition|(
name|sql
operator|.
name|contains
argument_list|(
name|dbIdenfier
operator|+
literal|".\"NAME\""
argument_list|)
condition|)
block|{
name|confirmedDbNameRetrieval
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"The Db info should be retrieved as part of MTable fetch"
argument_list|,
name|confirmedDbNameRetrieval
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetTableMetaFetchGroup
parameter_list|()
throws|throws
name|MetaException
throws|,
name|InvalidObjectException
throws|,
name|InvalidOperationException
block|{
name|objectStore
operator|=
name|createObjectStore
argument_list|()
expr_stmt|;
name|Database
name|db
init|=
operator|new
name|Database
argument_list|(
name|DB1
argument_list|,
literal|"description"
argument_list|,
literal|"locurl"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|db
operator|.
name|setCatalogName
argument_list|(
literal|"hive"
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|createDatabase
argument_list|(
name|db
argument_list|)
expr_stmt|;
name|objectStore
operator|.
name|createTable
argument_list|(
name|makeTable
argument_list|(
name|DB1
argument_list|,
name|TBL1
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TableMeta
argument_list|>
name|tableMeta
init|=
name|objectStore
operator|.
name|getTableMeta
argument_list|(
literal|"hive"
argument_list|,
literal|"*"
argument_list|,
literal|"*"
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Number of items for tableMeta is incorrect"
argument_list|,
literal|1
argument_list|,
name|tableMeta
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Table name incorrect"
argument_list|,
name|TBL1
argument_list|,
name|tableMeta
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Db name incorrect"
argument_list|,
name|DB1
argument_list|,
name|tableMeta
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getDbName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Table
name|makeTable
parameter_list|(
name|String
name|dbName
parameter_list|,
name|String
name|tblName
parameter_list|)
throws|throws
name|MetaException
block|{
return|return
operator|new
name|TableBuilder
argument_list|()
operator|.
name|setTableName
argument_list|(
name|tblName
argument_list|)
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
operator|.
name|setOwner
argument_list|(
literal|"owner"
argument_list|)
operator|.
name|setCreateTime
argument_list|(
literal|1
argument_list|)
operator|.
name|setLastAccessTime
argument_list|(
literal|2
argument_list|)
operator|.
name|setRetention
argument_list|(
literal|3
argument_list|)
operator|.
name|addTableParam
argument_list|(
literal|"EXTERNAL"
argument_list|,
literal|"false"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"MANAGED_TABLE"
argument_list|)
operator|.
name|setLocation
argument_list|(
literal|"location"
argument_list|)
operator|.
name|setCompressed
argument_list|(
literal|false
argument_list|)
operator|.
name|setNumBuckets
argument_list|(
literal|0
argument_list|)
operator|.
name|setSerdeName
argument_list|(
literal|"SerDeName"
argument_list|)
operator|.
name|setSerdeLib
argument_list|(
literal|"serializationLib"
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"some_col"
argument_list|,
literal|"double"
argument_list|,
literal|null
argument_list|)
operator|.
name|build
argument_list|(
name|conf
argument_list|)
return|;
block|}
block|}
end_class

end_unit

