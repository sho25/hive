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
name|util
package|;
end_package

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
name|TxnCommandsBaseForTests
operator|.
name|Table
operator|.
name|ACIDTBL
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
name|TxnCommandsBaseForTests
operator|.
name|Table
operator|.
name|ACIDTBLPART
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
name|TxnCommandsBaseForTests
operator|.
name|Table
operator|.
name|NONACIDNONBUCKET
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
name|TxnCommandsBaseForTests
operator|.
name|Table
operator|.
name|NONACIDORCTBL
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
name|TxnCommandsBaseForTests
operator|.
name|Table
operator|.
name|NONACIDORCTBL2
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|ql
operator|.
name|TxnCommandsBaseForTests
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
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestHiveStrictManagedMigration
extends|extends
name|TxnCommandsBaseForTests
block|{
specifier|private
specifier|static
specifier|final
name|String
name|TEST_DATA_DIR
init|=
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
operator|+
name|File
operator|.
name|separator
operator|+
name|TestHiveStrictManagedMigration
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
operator|+
literal|"-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|.
name|getPath
argument_list|()
operator|.
name|replaceAll
argument_list|(
literal|"\\\\"
argument_list|,
literal|"/"
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testUpgrade
parameter_list|()
throws|throws
name|Exception
block|{
name|int
index|[]
index|[]
name|data
init|=
block|{
block|{
literal|1
block|,
literal|2
block|}
block|,
block|{
literal|3
block|,
literal|4
block|}
block|,
block|{
literal|5
block|,
literal|6
block|}
block|}
decl_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"DROP TABLE IF EXISTS test.TAcid"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"DROP DATABASE IF EXISTS test"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"CREATE DATABASE test"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"CREATE TABLE test.TAcid (a int, b int) CLUSTERED BY (b) INTO 2 BUCKETS STORED AS orc TBLPROPERTIES"
operator|+
literal|" ('transactional'='true')"
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"INSERT INTO test.TAcid"
operator|+
name|makeValuesClause
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|runStatementOnDriver
argument_list|(
literal|"CREATE EXTERNAL TABLE texternal (a int, b int)"
argument_list|)
expr_stmt|;
name|String
name|oldWarehouse
init|=
name|getWarehouseDir
argument_list|()
decl_stmt|;
name|String
index|[]
name|args
init|=
block|{
literal|"--hiveconf"
block|,
literal|"hive.strict.managed.tables=true"
block|,
literal|"-m"
block|,
literal|"automatic"
block|,
literal|"--modifyManagedTables"
block|,
literal|"--oldWarehouseRoot"
block|,
name|oldWarehouse
block|}
decl_stmt|;
name|HiveConf
name|newConf
init|=
operator|new
name|HiveConf
argument_list|(
name|hiveConf
argument_list|)
decl_stmt|;
name|File
name|newWarehouseDir
init|=
operator|new
name|File
argument_list|(
name|getTestDataDir
argument_list|()
argument_list|,
literal|"newWarehouse"
argument_list|)
decl_stmt|;
name|newConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|METASTOREWAREHOUSE
operator|.
name|varname
argument_list|,
name|newWarehouseDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|newConf
operator|.
name|set
argument_list|(
literal|"strict.managed.tables.migration.owner"
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|)
expr_stmt|;
name|HiveStrictManagedMigration
operator|.
name|hiveConf
operator|=
name|newConf
expr_stmt|;
name|HiveStrictManagedMigration
operator|.
name|scheme
operator|=
literal|"file"
expr_stmt|;
name|HiveStrictManagedMigration
operator|.
name|main
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|newWarehouseDir
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|new
name|File
argument_list|(
name|newWarehouseDir
argument_list|,
name|ACIDTBL
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|new
name|File
argument_list|(
name|newWarehouseDir
argument_list|,
name|ACIDTBLPART
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|new
name|File
argument_list|(
name|newWarehouseDir
argument_list|,
name|NONACIDNONBUCKET
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|new
name|File
argument_list|(
name|newWarehouseDir
argument_list|,
name|NONACIDORCTBL
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|new
name|File
argument_list|(
name|newWarehouseDir
argument_list|,
name|NONACIDORCTBL2
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|new
name|File
argument_list|(
operator|new
name|File
argument_list|(
name|newWarehouseDir
argument_list|,
literal|"test.db"
argument_list|)
argument_list|,
literal|"tacid"
argument_list|)
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|new
name|File
argument_list|(
name|oldWarehouse
argument_list|,
literal|"texternal"
argument_list|)
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|String
name|getTestDataDir
parameter_list|()
block|{
return|return
name|TEST_DATA_DIR
return|;
block|}
block|}
end_class

end_unit

