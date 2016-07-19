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
name|hive
operator|.
name|hcatalog
operator|.
name|pig
package|;
end_package

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
name|io
operator|.
name|FileWriter
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
name|Collection
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|FileUtil
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
name|cli
operator|.
name|CliSessionState
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
name|CommandNeedRetryException
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
name|Driver
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
name|IOConstants
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
name|StorageFormats
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
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hive
operator|.
name|hcatalog
operator|.
name|data
operator|.
name|Pair
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|ExecType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|pig
operator|.
name|PigServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|Assume
operator|.
name|assumeTrue
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestHCatStorerMulti
block|{
specifier|public
specifier|static
specifier|final
name|String
name|TEST_DATA_DIR
init|=
name|HCatUtil
operator|.
name|makePathASafeFileName
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.dir"
argument_list|)
operator|+
literal|"/build/test/data/"
operator|+
name|TestHCatStorerMulti
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
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_WAREHOUSE_DIR
init|=
name|TEST_DATA_DIR
operator|+
literal|"/warehouse"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|INPUT_FILE_NAME
init|=
name|TEST_DATA_DIR
operator|+
literal|"/input.data"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|BASIC_TABLE
init|=
literal|"junit_unparted_basic"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PARTITIONED_TABLE
init|=
literal|"junit_parted_basic"
decl_stmt|;
specifier|private
specifier|static
name|Driver
name|driver
decl_stmt|;
specifier|private
specifier|static
name|Map
argument_list|<
name|Integer
argument_list|,
name|Pair
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
argument_list|>
name|basicInputData
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
name|DISABLED_STORAGE_FORMATS
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Set
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
block|{
block|{
name|put
argument_list|(
name|IOConstants
operator|.
name|PARQUETFILE
argument_list|,
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
block|{
name|add
argument_list|(
literal|"testStoreBasicTable"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testStorePartitionedTable"
argument_list|)
expr_stmt|;
name|add
argument_list|(
literal|"testStoreTableMulti"
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
specifier|private
specifier|final
name|String
name|storageFormat
decl_stmt|;
annotation|@
name|Parameterized
operator|.
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|generateParameters
parameter_list|()
block|{
return|return
name|StorageFormats
operator|.
name|names
argument_list|()
return|;
block|}
specifier|public
name|TestHCatStorerMulti
parameter_list|(
name|String
name|storageFormat
parameter_list|)
block|{
name|this
operator|.
name|storageFormat
operator|=
name|storageFormat
expr_stmt|;
block|}
specifier|private
name|void
name|dropTable
parameter_list|(
name|String
name|tablename
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"drop table "
operator|+
name|tablename
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|createTable
parameter_list|(
name|String
name|tablename
parameter_list|,
name|String
name|schema
parameter_list|,
name|String
name|partitionedBy
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|String
name|createTable
decl_stmt|;
name|createTable
operator|=
literal|"create table "
operator|+
name|tablename
operator|+
literal|"("
operator|+
name|schema
operator|+
literal|") "
expr_stmt|;
if|if
condition|(
operator|(
name|partitionedBy
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|partitionedBy
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|createTable
operator|=
name|createTable
operator|+
literal|"partitioned by ("
operator|+
name|partitionedBy
operator|+
literal|") "
expr_stmt|;
block|}
name|createTable
operator|=
name|createTable
operator|+
literal|"stored as "
operator|+
name|storageFormat
expr_stmt|;
name|int
name|retCode
init|=
name|driver
operator|.
name|run
argument_list|(
name|createTable
argument_list|)
operator|.
name|getResponseCode
argument_list|()
decl_stmt|;
if|if
condition|(
name|retCode
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to create table. ["
operator|+
name|createTable
operator|+
literal|"], return code from hive driver : ["
operator|+
name|retCode
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
specifier|private
name|void
name|createTable
parameter_list|(
name|String
name|tablename
parameter_list|,
name|String
name|schema
parameter_list|)
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|createTable
argument_list|(
name|tablename
argument_list|,
name|schema
argument_list|,
literal|null
argument_list|)
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
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|driver
operator|==
literal|null
condition|)
block|{
name|HiveConf
name|hiveConf
init|=
operator|new
name|HiveConf
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|PREEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|POSTEXECHOOKS
operator|.
name|varname
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|set
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_SUPPORT_CONCURRENCY
operator|.
name|varname
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|hiveConf
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
name|TEST_WAREHOUSE_DIR
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVEMAPREDMODE
argument_list|,
literal|"nonstrict"
argument_list|)
expr_stmt|;
name|hiveConf
operator|.
name|setVar
argument_list|(
name|HiveConf
operator|.
name|ConfVars
operator|.
name|HIVE_AUTHORIZATION_MANAGER
argument_list|,
literal|"org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory"
argument_list|)
expr_stmt|;
name|driver
operator|=
operator|new
name|Driver
argument_list|(
name|hiveConf
argument_list|)
expr_stmt|;
name|SessionState
operator|.
name|start
argument_list|(
operator|new
name|CliSessionState
argument_list|(
name|hiveConf
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|cleanup
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|cleanup
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStoreBasicTable
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|createTable
argument_list|(
name|BASIC_TABLE
argument_list|,
literal|"a int, b string"
argument_list|)
expr_stmt|;
name|populateBasicFile
argument_list|()
expr_stmt|;
name|PigServer
name|server
init|=
operator|new
name|PigServer
argument_list|(
name|ExecType
operator|.
name|LOCAL
argument_list|)
decl_stmt|;
name|server
operator|.
name|setBatchOn
argument_list|()
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"A = load '"
operator|+
name|INPUT_FILE_NAME
operator|+
literal|"' as (a:int, b:chararray);"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"store A into '"
operator|+
name|BASIC_TABLE
operator|+
literal|"' using org.apache.hive.hcatalog.pig.HCatStorer();"
argument_list|)
expr_stmt|;
name|server
operator|.
name|executeBatch
argument_list|()
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"select * from "
operator|+
name|BASIC_TABLE
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|unpartitionedTableValuesReadFromHiveDriver
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|driver
operator|.
name|getResults
argument_list|(
name|unpartitionedTableValuesReadFromHiveDriver
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|basicInputData
operator|.
name|size
argument_list|()
argument_list|,
name|unpartitionedTableValuesReadFromHiveDriver
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
name|testStorePartitionedTable
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|createTable
argument_list|(
name|PARTITIONED_TABLE
argument_list|,
literal|"a int, b string"
argument_list|,
literal|"bkt string"
argument_list|)
expr_stmt|;
name|populateBasicFile
argument_list|()
expr_stmt|;
name|PigServer
name|server
init|=
operator|new
name|PigServer
argument_list|(
name|ExecType
operator|.
name|LOCAL
argument_list|)
decl_stmt|;
name|server
operator|.
name|setBatchOn
argument_list|()
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"A = load '"
operator|+
name|INPUT_FILE_NAME
operator|+
literal|"' as (a:int, b:chararray);"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"B2 = filter A by a< 2;"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"store B2 into '"
operator|+
name|PARTITIONED_TABLE
operator|+
literal|"' using org.apache.hive.hcatalog.pig.HCatStorer('bkt=0');"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"C2 = filter A by a>= 2;"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"store C2 into '"
operator|+
name|PARTITIONED_TABLE
operator|+
literal|"' using org.apache.hive.hcatalog.pig.HCatStorer('bkt=1');"
argument_list|)
expr_stmt|;
name|server
operator|.
name|executeBatch
argument_list|()
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"select * from "
operator|+
name|PARTITIONED_TABLE
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|partitionedTableValuesReadFromHiveDriver
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|driver
operator|.
name|getResults
argument_list|(
name|partitionedTableValuesReadFromHiveDriver
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|basicInputData
operator|.
name|size
argument_list|()
argument_list|,
name|partitionedTableValuesReadFromHiveDriver
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
name|testStoreTableMulti
parameter_list|()
throws|throws
name|Exception
block|{
name|assumeTrue
argument_list|(
operator|!
name|TestUtil
operator|.
name|shouldSkip
argument_list|(
name|storageFormat
argument_list|,
name|DISABLED_STORAGE_FORMATS
argument_list|)
argument_list|)
expr_stmt|;
name|createTable
argument_list|(
name|BASIC_TABLE
argument_list|,
literal|"a int, b string"
argument_list|)
expr_stmt|;
name|createTable
argument_list|(
name|PARTITIONED_TABLE
argument_list|,
literal|"a int, b string"
argument_list|,
literal|"bkt string"
argument_list|)
expr_stmt|;
name|populateBasicFile
argument_list|()
expr_stmt|;
name|PigServer
name|server
init|=
operator|new
name|PigServer
argument_list|(
name|ExecType
operator|.
name|LOCAL
argument_list|)
decl_stmt|;
name|server
operator|.
name|setBatchOn
argument_list|()
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"A = load '"
operator|+
name|INPUT_FILE_NAME
operator|+
literal|"' as (a:int, b:chararray);"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"store A into '"
operator|+
name|BASIC_TABLE
operator|+
literal|"' using org.apache.hive.hcatalog.pig.HCatStorer();"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"B2 = filter A by a< 2;"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"store B2 into '"
operator|+
name|PARTITIONED_TABLE
operator|+
literal|"' using org.apache.hive.hcatalog.pig.HCatStorer('bkt=0');"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"C2 = filter A by a>= 2;"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"store C2 into '"
operator|+
name|PARTITIONED_TABLE
operator|+
literal|"' using org.apache.hive.hcatalog.pig.HCatStorer('bkt=1');"
argument_list|)
expr_stmt|;
name|server
operator|.
name|executeBatch
argument_list|()
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"select * from "
operator|+
name|BASIC_TABLE
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|unpartitionedTableValuesReadFromHiveDriver
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|driver
operator|.
name|getResults
argument_list|(
name|unpartitionedTableValuesReadFromHiveDriver
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"select * from "
operator|+
name|PARTITIONED_TABLE
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|partitionedTableValuesReadFromHiveDriver
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|driver
operator|.
name|getResults
argument_list|(
name|partitionedTableValuesReadFromHiveDriver
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|basicInputData
operator|.
name|size
argument_list|()
argument_list|,
name|unpartitionedTableValuesReadFromHiveDriver
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|basicInputData
operator|.
name|size
argument_list|()
argument_list|,
name|partitionedTableValuesReadFromHiveDriver
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|populateBasicFile
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|LOOP_SIZE
init|=
literal|3
decl_stmt|;
name|String
index|[]
name|input
init|=
operator|new
name|String
index|[
name|LOOP_SIZE
operator|*
name|LOOP_SIZE
index|]
decl_stmt|;
name|basicInputData
operator|=
operator|new
name|HashMap
argument_list|<
name|Integer
argument_list|,
name|Pair
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|int
name|k
init|=
literal|0
decl_stmt|;
name|File
name|file
init|=
operator|new
name|File
argument_list|(
name|INPUT_FILE_NAME
argument_list|)
decl_stmt|;
name|file
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|FileWriter
name|writer
init|=
operator|new
name|FileWriter
argument_list|(
name|file
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|LOOP_SIZE
condition|;
name|i
operator|++
control|)
block|{
name|String
name|si
init|=
name|i
operator|+
literal|""
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|1
init|;
name|j
operator|<=
name|LOOP_SIZE
condition|;
name|j
operator|++
control|)
block|{
name|String
name|sj
init|=
literal|"S"
operator|+
name|j
operator|+
literal|"S"
decl_stmt|;
name|input
index|[
name|k
index|]
operator|=
name|si
operator|+
literal|"\t"
operator|+
name|sj
expr_stmt|;
name|basicInputData
operator|.
name|put
argument_list|(
name|k
argument_list|,
operator|new
name|Pair
argument_list|<
name|Integer
argument_list|,
name|String
argument_list|>
argument_list|(
name|i
argument_list|,
name|sj
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|write
argument_list|(
name|input
index|[
name|k
index|]
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|k
operator|++
expr_stmt|;
block|}
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|cleanup
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommandNeedRetryException
block|{
name|File
name|f
init|=
operator|new
name|File
argument_list|(
name|TEST_WAREHOUSE_DIR
argument_list|)
decl_stmt|;
if|if
condition|(
name|f
operator|.
name|exists
argument_list|()
condition|)
block|{
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
name|f
argument_list|)
expr_stmt|;
block|}
operator|new
name|File
argument_list|(
name|TEST_WAREHOUSE_DIR
argument_list|)
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|dropTable
argument_list|(
name|BASIC_TABLE
argument_list|)
expr_stmt|;
name|dropTable
argument_list|(
name|PARTITIONED_TABLE
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

