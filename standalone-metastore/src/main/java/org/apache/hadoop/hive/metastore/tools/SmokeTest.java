begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|tools
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
name|ColumnType
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
name|HiveMetaStoreClient
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
name|builder
operator|.
name|DatabaseBuilder
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
name|conf
operator|.
name|MetastoreConf
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
name|File
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
comment|/**  * This class runs a few simple operations against the server to make sure it runs reasonably.  * Even though it is a test its in the main tree because it needs to be deployed with the server  * to allow smoke testing once the server is installed.  */
end_comment

begin_class
specifier|public
class|class
name|SmokeTest
block|{
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
name|SmokeTest
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|dbName
init|=
literal|"internal_smoke_test"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|tableName
init|=
literal|"internal_smoke_test_table"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|partValue
init|=
literal|"internal_smoke_test_val1"
decl_stmt|;
specifier|private
name|SmokeTest
parameter_list|()
block|{    }
specifier|private
name|void
name|runTest
parameter_list|(
name|IMetaStoreClient
name|client
parameter_list|)
throws|throws
name|TException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting smoke test"
argument_list|)
expr_stmt|;
name|File
name|dbDir
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
argument_list|,
literal|"internal_smoke_test"
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|dbDir
operator|.
name|mkdir
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unable to create direcotory "
operator|+
name|dbDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
throw|;
block|}
name|dbDir
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Going to create database "
operator|+
name|dbName
argument_list|)
expr_stmt|;
name|Database
name|db
init|=
operator|new
name|DatabaseBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|dbName
argument_list|)
operator|.
name|setLocation
argument_list|(
name|dbDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|client
operator|.
name|createDatabase
argument_list|(
name|db
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Going to create table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
operator|new
name|TableBuilder
argument_list|()
operator|.
name|setDbName
argument_list|(
name|db
argument_list|)
operator|.
name|setTableName
argument_list|(
name|tableName
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"col1"
argument_list|,
name|ColumnType
operator|.
name|INT_TYPE_NAME
argument_list|)
operator|.
name|addCol
argument_list|(
literal|"col2"
argument_list|,
name|ColumnType
operator|.
name|TIMESTAMP_TYPE_NAME
argument_list|)
operator|.
name|addPartCol
argument_list|(
literal|"pcol1"
argument_list|,
name|ColumnType
operator|.
name|STRING_TYPE_NAME
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|client
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Going to create partition with value "
operator|+
name|partValue
argument_list|)
expr_stmt|;
name|Partition
name|part
init|=
operator|new
name|PartitionBuilder
argument_list|()
operator|.
name|fromTable
argument_list|(
name|table
argument_list|)
operator|.
name|addValue
argument_list|(
literal|"val1"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|client
operator|.
name|add_partition
argument_list|(
name|part
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Going to list the partitions"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|parts
init|=
name|client
operator|.
name|listPartitions
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
operator|(
name|short
operator|)
operator|-
literal|1
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Fetched: { "
operator|+
name|parts
operator|.
name|toString
argument_list|()
operator|+
literal|"}"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Going to drop database"
argument_list|)
expr_stmt|;
name|client
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Completed smoke test"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|SmokeTest
name|test
init|=
operator|new
name|SmokeTest
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|MetastoreConf
operator|.
name|newMetastoreConf
argument_list|()
decl_stmt|;
name|IMetaStoreClient
name|client
init|=
operator|new
name|HiveMetaStoreClient
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|test
operator|.
name|runTest
argument_list|(
name|client
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

