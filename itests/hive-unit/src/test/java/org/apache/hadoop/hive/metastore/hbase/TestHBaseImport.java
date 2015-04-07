begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
operator|.
name|hbase
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
name|hive
operator|.
name|metastore
operator|.
name|ObjectStore
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
name|RawStore
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
name|Function
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
name|FunctionType
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
name|ResourceType
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
name|ResourceUri
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
name|junit
operator|.
name|AfterClass
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
name|Before
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
name|Arrays
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

begin_comment
comment|/**  * Test that import from an RDBMS based metastore works  */
end_comment

begin_class
specifier|public
class|class
name|TestHBaseImport
extends|extends
name|HBaseIntegrationTests
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestHBaseStoreIntegration
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|startup
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseIntegrationTests
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|shutdown
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseIntegrationTests
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
name|setupConnection
argument_list|()
expr_stmt|;
name|setupHBaseStore
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|doImport
parameter_list|()
throws|throws
name|Exception
block|{
name|RawStore
name|rdbms
init|=
operator|new
name|ObjectStore
argument_list|()
decl_stmt|;
name|rdbms
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|String
index|[]
name|dbNames
init|=
operator|new
name|String
index|[]
block|{
literal|"importdb1"
block|,
literal|"importdb2"
block|}
decl_stmt|;
name|String
index|[]
name|tableNames
init|=
operator|new
name|String
index|[]
block|{
literal|"nonparttable"
block|,
literal|"parttable"
block|}
decl_stmt|;
name|String
index|[]
name|partVals
init|=
operator|new
name|String
index|[]
block|{
literal|"na"
block|,
literal|"emea"
block|,
literal|"latam"
block|,
literal|"apac"
block|}
decl_stmt|;
name|String
index|[]
name|funcNames
init|=
operator|new
name|String
index|[]
block|{
literal|"func1"
block|,
literal|"func2"
block|}
decl_stmt|;
name|String
index|[]
name|roles
init|=
operator|new
name|String
index|[]
block|{
literal|"role1"
block|,
literal|"role2"
block|}
decl_stmt|;
name|int
name|now
init|=
operator|(
name|int
operator|)
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|/
literal|1000
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
name|roles
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|rdbms
operator|.
name|addRole
argument_list|(
name|roles
index|[
name|i
index|]
argument_list|,
literal|"me"
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
name|dbNames
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|rdbms
operator|.
name|createDatabase
argument_list|(
operator|new
name|Database
argument_list|(
name|dbNames
index|[
name|i
index|]
argument_list|,
literal|"no description"
argument_list|,
literal|"file:/tmp"
argument_list|,
name|emptyParameters
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|cols
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
decl_stmt|;
name|cols
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"col1"
argument_list|,
literal|"int"
argument_list|,
literal|"nocomment"
argument_list|)
argument_list|)
expr_stmt|;
name|SerDeInfo
name|serde
init|=
operator|new
name|SerDeInfo
argument_list|(
literal|"serde"
argument_list|,
literal|"seriallib"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|StorageDescriptor
name|sd
init|=
operator|new
name|StorageDescriptor
argument_list|(
name|cols
argument_list|,
literal|"file:/tmp"
argument_list|,
literal|"input"
argument_list|,
literal|"output"
argument_list|,
literal|false
argument_list|,
literal|0
argument_list|,
name|serde
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|emptyParameters
argument_list|)
decl_stmt|;
name|rdbms
operator|.
name|createTable
argument_list|(
operator|new
name|Table
argument_list|(
name|tableNames
index|[
literal|0
index|]
argument_list|,
name|dbNames
index|[
name|i
index|]
argument_list|,
literal|"me"
argument_list|,
name|now
argument_list|,
name|now
argument_list|,
literal|0
argument_list|,
name|sd
argument_list|,
literal|null
argument_list|,
name|emptyParameters
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
decl_stmt|;
name|partCols
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
literal|"region"
argument_list|,
literal|"string"
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|rdbms
operator|.
name|createTable
argument_list|(
operator|new
name|Table
argument_list|(
name|tableNames
index|[
literal|1
index|]
argument_list|,
name|dbNames
index|[
name|i
index|]
argument_list|,
literal|"me"
argument_list|,
name|now
argument_list|,
name|now
argument_list|,
literal|0
argument_list|,
name|sd
argument_list|,
name|partCols
argument_list|,
name|emptyParameters
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|partVals
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|StorageDescriptor
name|psd
init|=
operator|new
name|StorageDescriptor
argument_list|(
name|sd
argument_list|)
decl_stmt|;
name|psd
operator|.
name|setLocation
argument_list|(
literal|"file:/tmp/region="
operator|+
name|partVals
index|[
name|j
index|]
argument_list|)
expr_stmt|;
name|Partition
name|part
init|=
operator|new
name|Partition
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|partVals
index|[
name|j
index|]
argument_list|)
argument_list|,
name|dbNames
index|[
name|i
index|]
argument_list|,
name|tableNames
index|[
literal|1
index|]
argument_list|,
name|now
argument_list|,
name|now
argument_list|,
name|psd
argument_list|,
name|emptyParameters
argument_list|)
decl_stmt|;
name|store
operator|.
name|addPartition
argument_list|(
name|part
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|funcName
range|:
name|funcNames
control|)
block|{
name|store
operator|.
name|createFunction
argument_list|(
operator|new
name|Function
argument_list|(
name|funcName
argument_list|,
name|dbNames
index|[
name|i
index|]
argument_list|,
literal|"classname"
argument_list|,
literal|"ownername"
argument_list|,
name|PrincipalType
operator|.
name|USER
argument_list|,
operator|(
name|int
operator|)
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|/
literal|1000
argument_list|,
name|FunctionType
operator|.
name|JAVA
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|ResourceUri
argument_list|(
name|ResourceType
operator|.
name|JAR
argument_list|,
literal|"uri"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|HBaseImport
name|importer
init|=
operator|new
name|HBaseImport
argument_list|()
decl_stmt|;
name|importer
operator|.
name|setConnections
argument_list|(
name|rdbms
argument_list|,
name|store
argument_list|)
expr_stmt|;
name|importer
operator|.
name|run
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|roles
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Role
name|role
init|=
name|store
operator|.
name|getRole
argument_list|(
name|roles
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|role
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|roles
index|[
name|i
index|]
argument_list|,
name|role
operator|.
name|getRoleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Make sure there aren't any extra roles
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|store
operator|.
name|listRoleNames
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|dbNames
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Database
name|db
init|=
name|store
operator|.
name|getDatabase
argument_list|(
name|dbNames
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|db
argument_list|)
expr_stmt|;
comment|// check one random value in the db rather than every value
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"file:/tmp"
argument_list|,
name|db
operator|.
name|getLocationUri
argument_list|()
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
name|store
operator|.
name|getTable
argument_list|(
name|db
operator|.
name|getName
argument_list|()
argument_list|,
name|tableNames
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|now
argument_list|,
name|table
operator|.
name|getLastAccessTime
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"input"
argument_list|,
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getInputFormat
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|=
name|store
operator|.
name|getTable
argument_list|(
name|db
operator|.
name|getName
argument_list|()
argument_list|,
name|tableNames
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|table
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|partVals
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|Partition
name|part
init|=
name|store
operator|.
name|getPartition
argument_list|(
name|dbNames
index|[
name|i
index|]
argument_list|,
name|tableNames
index|[
literal|1
index|]
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|partVals
index|[
name|j
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|part
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"file:/tmp/region="
operator|+
name|partVals
index|[
name|j
index|]
argument_list|,
name|part
operator|.
name|getSd
argument_list|()
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|store
operator|.
name|getPartitions
argument_list|(
name|dbNames
index|[
name|i
index|]
argument_list|,
name|tableNames
index|[
literal|1
index|]
argument_list|,
operator|-
literal|1
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|store
operator|.
name|getAllTables
argument_list|(
name|dbNames
index|[
name|i
index|]
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|store
operator|.
name|getFunctions
argument_list|(
name|dbNames
index|[
name|i
index|]
argument_list|,
literal|"*"
argument_list|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|funcNames
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|store
operator|.
name|getFunction
argument_list|(
name|dbNames
index|[
name|i
index|]
argument_list|,
name|funcNames
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|store
operator|.
name|getAllDatabases
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

