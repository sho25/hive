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
name|ql
operator|.
name|metadata
package|;
end_package

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
name|TestCase
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
name|metastore
operator|.
name|api
operator|.
name|AlreadyExistsException
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
name|ql
operator|.
name|io
operator|.
name|HiveIgnoreKeyTextOutputFormat
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
name|serde
operator|.
name|serdeConstants
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
name|mapred
operator|.
name|TextInputFormat
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

begin_comment
comment|/**  * TestHiveMetaStoreChecker.  *  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveMetaStoreChecker
extends|extends
name|TestCase
block|{
specifier|private
name|Hive
name|hive
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|HiveMetaStoreChecker
name|checker
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|String
name|dbName
init|=
literal|"dbname"
decl_stmt|;
specifier|private
specifier|final
name|String
name|tableName
init|=
literal|"tablename"
decl_stmt|;
specifier|private
specifier|final
name|String
name|partDateName
init|=
literal|"partdate"
decl_stmt|;
specifier|private
specifier|final
name|String
name|partCityName
init|=
literal|"partcity"
decl_stmt|;
specifier|private
name|List
argument_list|<
name|FieldSchema
argument_list|>
name|partCols
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|parts
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|hive
operator|=
name|Hive
operator|.
name|get
argument_list|()
expr_stmt|;
name|checker
operator|=
operator|new
name|HiveMetaStoreChecker
argument_list|(
name|hive
argument_list|)
expr_stmt|;
name|partCols
operator|=
operator|new
name|ArrayList
argument_list|<
name|FieldSchema
argument_list|>
argument_list|()
expr_stmt|;
name|partCols
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
name|partDateName
argument_list|,
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|partCols
operator|.
name|add
argument_list|(
operator|new
name|FieldSchema
argument_list|(
name|partCityName
argument_list|,
name|serdeConstants
operator|.
name|STRING_TYPE_NAME
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|parts
operator|=
operator|new
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|part1
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|part1
operator|.
name|put
argument_list|(
name|partDateName
argument_list|,
literal|"2008-01-01"
argument_list|)
expr_stmt|;
name|part1
operator|.
name|put
argument_list|(
name|partCityName
argument_list|,
literal|"london"
argument_list|)
expr_stmt|;
name|parts
operator|.
name|add
argument_list|(
name|part1
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|part2
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|part2
operator|.
name|put
argument_list|(
name|partDateName
argument_list|,
literal|"2008-01-02"
argument_list|)
expr_stmt|;
name|part2
operator|.
name|put
argument_list|(
name|partCityName
argument_list|,
literal|"stockholm"
argument_list|)
expr_stmt|;
name|parts
operator|.
name|add
argument_list|(
name|part2
argument_list|)
expr_stmt|;
comment|// cleanup
name|hive
operator|.
name|dropTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
try|try
block|{
name|hive
operator|.
name|dropDatabase
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchObjectException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|Hive
operator|.
name|closeCurrent
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testTableCheck
parameter_list|()
throws|throws
name|HiveException
throws|,
name|MetaException
throws|,
name|IOException
throws|,
name|TException
throws|,
name|AlreadyExistsException
block|{
name|CheckResult
name|result
init|=
operator|new
name|CheckResult
argument_list|()
decl_stmt|;
name|checker
operator|.
name|checkMetastore
argument_list|(
name|dbName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|// we haven't added anything so should return an all ok
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// check table only, should not exist in ms
name|result
operator|=
operator|new
name|CheckResult
argument_list|()
expr_stmt|;
name|checker
operator|.
name|checkMetastore
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|getTablesNotInMs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableName
argument_list|,
name|result
operator|.
name|getTablesNotInMs
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|Database
name|db
init|=
operator|new
name|Database
argument_list|()
decl_stmt|;
name|db
operator|.
name|setName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|hive
operator|.
name|createDatabase
argument_list|(
name|db
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
operator|new
name|Table
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|table
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|table
operator|.
name|setInputFormatClass
argument_list|(
name|TextInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|table
operator|.
name|setOutputFormatClass
argument_list|(
name|HiveIgnoreKeyTextOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|hive
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|// now we've got a table, check that it works
comment|// first check all (1) tables
name|result
operator|=
operator|new
name|CheckResult
argument_list|()
expr_stmt|;
name|checker
operator|.
name|checkMetastore
argument_list|(
name|dbName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// then let's check the one we know about
name|result
operator|=
operator|new
name|CheckResult
argument_list|()
expr_stmt|;
name|checker
operator|.
name|checkMetastore
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// remove the table folder
name|fs
operator|=
name|table
operator|.
name|getPath
argument_list|()
operator|.
name|getFileSystem
argument_list|(
name|hive
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|table
operator|.
name|getPath
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// now this shouldn't find the path on the fs
name|result
operator|=
operator|new
name|CheckResult
argument_list|()
expr_stmt|;
name|checker
operator|.
name|checkMetastore
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|getTablesNotOnFs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableName
argument_list|,
name|result
operator|.
name|getTablesNotOnFs
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// put it back and one additional table
name|fs
operator|.
name|mkdirs
argument_list|(
name|table
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|fakeTable
init|=
name|table
operator|.
name|getPath
argument_list|()
operator|.
name|getParent
argument_list|()
operator|.
name|suffix
argument_list|(
name|Path
operator|.
name|SEPARATOR
operator|+
literal|"faketable"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|fakeTable
argument_list|)
expr_stmt|;
comment|// find the extra table
name|result
operator|=
operator|new
name|CheckResult
argument_list|()
expr_stmt|;
name|checker
operator|.
name|checkMetastore
argument_list|(
name|dbName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|getTablesNotInMs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|fakeTable
operator|.
name|getName
argument_list|()
argument_list|,
name|result
operator|.
name|getTablesNotInMs
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// create a new external table
name|hive
operator|.
name|dropTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|table
operator|.
name|setProperty
argument_list|(
literal|"EXTERNAL"
argument_list|,
literal|"TRUE"
argument_list|)
expr_stmt|;
name|hive
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|// should return all ok
name|result
operator|=
operator|new
name|CheckResult
argument_list|()
expr_stmt|;
name|checker
operator|.
name|checkMetastore
argument_list|(
name|dbName
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testPartitionsCheck
parameter_list|()
throws|throws
name|HiveException
throws|,
name|MetaException
throws|,
name|IOException
throws|,
name|TException
throws|,
name|AlreadyExistsException
block|{
name|Database
name|db
init|=
operator|new
name|Database
argument_list|()
decl_stmt|;
name|db
operator|.
name|setName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|hive
operator|.
name|createDatabase
argument_list|(
name|db
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
operator|new
name|Table
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|table
operator|.
name|setDbName
argument_list|(
name|dbName
argument_list|)
expr_stmt|;
name|table
operator|.
name|setInputFormatClass
argument_list|(
name|TextInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|table
operator|.
name|setOutputFormatClass
argument_list|(
name|HiveIgnoreKeyTextOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|table
operator|.
name|setPartCols
argument_list|(
name|partCols
argument_list|)
expr_stmt|;
name|hive
operator|.
name|createTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|table
operator|=
name|hive
operator|.
name|getTable
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|partSpec
range|:
name|parts
control|)
block|{
name|hive
operator|.
name|createPartition
argument_list|(
name|table
argument_list|,
name|partSpec
argument_list|)
expr_stmt|;
block|}
name|CheckResult
name|result
init|=
operator|new
name|CheckResult
argument_list|()
decl_stmt|;
name|checker
operator|.
name|checkMetastore
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|// all is well
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Partition
argument_list|>
name|partitions
init|=
name|hive
operator|.
name|getPartitions
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|partitions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Partition
name|partToRemove
init|=
name|partitions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|Path
name|partToRemovePath
init|=
operator|new
name|Path
argument_list|(
name|partToRemove
operator|.
name|getDataLocation
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|=
name|partToRemovePath
operator|.
name|getFileSystem
argument_list|(
name|hive
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|partToRemovePath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|result
operator|=
operator|new
name|CheckResult
argument_list|()
expr_stmt|;
name|checker
operator|.
name|checkMetastore
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|// missing one partition on fs
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|getPartitionsNotOnFs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|partToRemove
operator|.
name|getName
argument_list|()
argument_list|,
name|result
operator|.
name|getPartitionsNotOnFs
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getPartitionName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|partToRemove
operator|.
name|getTable
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|,
name|result
operator|.
name|getPartitionsNotOnFs
argument_list|()
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
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|partsCopy
init|=
operator|new
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|partsCopy
operator|.
name|add
argument_list|(
name|partitions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getSpec
argument_list|()
argument_list|)
expr_stmt|;
comment|// check only the partition that exists, all should be well
name|result
operator|=
operator|new
name|CheckResult
argument_list|()
expr_stmt|;
name|checker
operator|.
name|checkMetastore
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
name|partsCopy
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
comment|// put the other one back
name|fs
operator|.
name|mkdirs
argument_list|(
name|partToRemovePath
argument_list|)
expr_stmt|;
comment|// add a partition dir on fs
name|Path
name|fakePart
init|=
operator|new
name|Path
argument_list|(
name|table
operator|.
name|getDataLocation
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|"fakepartition=fakevalue"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|fakePart
argument_list|)
expr_stmt|;
name|checker
operator|.
name|checkMetastore
argument_list|(
name|dbName
argument_list|,
name|tableName
argument_list|,
literal|null
argument_list|,
name|result
argument_list|)
expr_stmt|;
comment|// one extra partition
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotInMs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getTablesNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getPartitionsNotOnFs
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|getPartitionsNotInMs
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|fakePart
operator|.
name|getName
argument_list|()
argument_list|,
name|result
operator|.
name|getPartitionsNotInMs
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getPartitionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

