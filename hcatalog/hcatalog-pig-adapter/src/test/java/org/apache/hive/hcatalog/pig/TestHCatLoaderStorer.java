begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|hive
operator|.
name|hcatalog
operator|.
name|HcatTestUtils
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
name|mapreduce
operator|.
name|HCatBaseTest
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
name|apache
operator|.
name|pig
operator|.
name|backend
operator|.
name|executionengine
operator|.
name|ExecJob
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
name|data
operator|.
name|DataType
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
name|data
operator|.
name|Tuple
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
name|impl
operator|.
name|logicalLayer
operator|.
name|schema
operator|.
name|Schema
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
comment|/**  * Test that require both HCatLoader and HCatStorer. For read or write only functionality,  * please consider @{link TestHCatLoader} or @{link TestHCatStorer}.  */
end_comment

begin_class
specifier|public
class|class
name|TestHCatLoaderStorer
extends|extends
name|HCatBaseTest
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
name|TestHCatLoaderStorer
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Test round trip of smallint/tinyint: Hive->Pig->Hive.  This is a more general use case in HCatalog:    * 'read some data from Hive, process it in Pig, write result back to a Hive table'    */
annotation|@
name|Test
specifier|public
name|void
name|testReadWrite
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|tblName
init|=
literal|"small_ints_table"
decl_stmt|;
specifier|final
name|String
name|tblName2
init|=
literal|"pig_hcatalog_1"
decl_stmt|;
name|File
name|dataDir
init|=
operator|new
name|File
argument_list|(
name|TEST_DATA_DIR
operator|+
name|File
operator|.
name|separator
operator|+
literal|"testReadWrite"
argument_list|)
decl_stmt|;
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
name|dataDir
argument_list|)
expr_stmt|;
comment|// Might not exist
name|Assert
operator|.
name|assertTrue
argument_list|(
name|dataDir
operator|.
name|mkdir
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|String
name|INPUT_FILE_NAME
init|=
name|dataDir
operator|+
literal|"/inputtrw.data"
decl_stmt|;
name|AbstractHCatLoaderTest
operator|.
name|dropTable
argument_list|(
name|tblName
argument_list|,
name|driver
argument_list|)
expr_stmt|;
name|HcatTestUtils
operator|.
name|createTestDataFile
argument_list|(
name|INPUT_FILE_NAME
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"40\t1"
block|}
argument_list|)
expr_stmt|;
name|AbstractHCatLoaderTest
operator|.
name|executeStatementOnDriver
argument_list|(
literal|"create external table "
operator|+
name|tblName
operator|+
literal|" (my_small_int smallint, my_tiny_int tinyint)"
operator|+
literal|" row format delimited fields terminated by '\t' stored as textfile location '"
operator|+
name|dataDir
operator|.
name|toURI
argument_list|()
operator|.
name|getPath
argument_list|()
operator|+
literal|"'"
argument_list|,
name|driver
argument_list|)
expr_stmt|;
name|AbstractHCatLoaderTest
operator|.
name|dropTable
argument_list|(
name|tblName2
argument_list|,
name|driver
argument_list|)
expr_stmt|;
name|AbstractHCatLoaderTest
operator|.
name|createTableDefaultDB
argument_list|(
name|tblName2
argument_list|,
literal|"my_small_int smallint, "
operator|+
literal|"my_tiny_int "
operator|+
literal|"tinyint"
argument_list|,
literal|null
argument_list|,
name|driver
argument_list|,
literal|"textfile"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"File="
operator|+
name|INPUT_FILE_NAME
argument_list|)
expr_stmt|;
name|TestHCatStorer
operator|.
name|dumpFile
argument_list|(
name|INPUT_FILE_NAME
argument_list|)
expr_stmt|;
name|PigServer
name|server
init|=
name|createPigServer
argument_list|(
literal|true
argument_list|)
decl_stmt|;
try|try
block|{
name|int
name|queryNumber
init|=
literal|1
decl_stmt|;
name|logAndRegister
argument_list|(
name|server
argument_list|,
literal|"A = load '"
operator|+
name|tblName
operator|+
literal|"' using org.apache.hive.hcatalog.pig.HCatLoader() as (my_small_int:int, my_tiny_int:int);"
argument_list|,
name|queryNumber
operator|++
argument_list|)
expr_stmt|;
name|logAndRegister
argument_list|(
name|server
argument_list|,
literal|"b = foreach A generate my_small_int + my_tiny_int as my_small_int, my_tiny_int;"
argument_list|,
name|queryNumber
operator|++
argument_list|)
expr_stmt|;
name|logAndRegister
argument_list|(
name|server
argument_list|,
literal|"store b into '"
operator|+
name|tblName2
operator|+
literal|"' using org.apache.hive.hcatalog.pig.HCatStorer();"
argument_list|,
name|queryNumber
argument_list|)
expr_stmt|;
comment|//perform simple checksum here; make sure nothing got turned to NULL
name|AbstractHCatLoaderTest
operator|.
name|executeStatementOnDriver
argument_list|(
literal|"select my_small_int from "
operator|+
name|tblName2
argument_list|,
name|driver
argument_list|)
expr_stmt|;
name|ArrayList
name|l
init|=
operator|new
name|ArrayList
argument_list|()
decl_stmt|;
name|driver
operator|.
name|getResults
argument_list|(
name|l
argument_list|)
expr_stmt|;
for|for
control|(
name|Object
name|t
range|:
name|l
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"t="
operator|+
name|t
argument_list|)
expr_stmt|;
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Expected '1' rows; got '"
operator|+
name|l
operator|.
name|size
argument_list|()
operator|+
literal|"'"
argument_list|,
literal|1
argument_list|,
name|l
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|result
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
operator|(
name|String
operator|)
name|l
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Expected value '41'; got '"
operator|+
name|result
operator|+
literal|"'"
argument_list|,
literal|41
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|server
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Ensure Pig can read/write tinyint/smallint columns.    */
annotation|@
name|Test
specifier|public
name|void
name|testSmallTinyInt
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|readTblName
init|=
literal|"test_small_tiny_int"
decl_stmt|;
name|File
name|dataDir
init|=
operator|new
name|File
argument_list|(
name|TEST_DATA_DIR
operator|+
literal|"/testSmallTinyIntData"
argument_list|)
decl_stmt|;
name|File
name|dataFile
init|=
operator|new
name|File
argument_list|(
name|dataDir
argument_list|,
literal|"testSmallTinyInt.tsv"
argument_list|)
decl_stmt|;
name|String
name|writeTblName
init|=
literal|"test_small_tiny_int_write"
decl_stmt|;
name|File
name|writeDataFile
init|=
operator|new
name|File
argument_list|(
name|TEST_DATA_DIR
argument_list|,
name|writeTblName
operator|+
literal|".tsv"
argument_list|)
decl_stmt|;
name|FileUtil
operator|.
name|fullyDelete
argument_list|(
name|dataDir
argument_list|)
expr_stmt|;
comment|// Might not exist
name|Assert
operator|.
name|assertTrue
argument_list|(
name|dataDir
operator|.
name|mkdir
argument_list|()
argument_list|)
expr_stmt|;
name|HcatTestUtils
operator|.
name|createTestDataFile
argument_list|(
name|dataFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
name|String
operator|.
name|format
argument_list|(
literal|"%d\t%d"
argument_list|,
name|Short
operator|.
name|MIN_VALUE
argument_list|,
name|Byte
operator|.
name|MIN_VALUE
argument_list|)
block|,
name|String
operator|.
name|format
argument_list|(
literal|"%d\t%d"
argument_list|,
name|Short
operator|.
name|MAX_VALUE
argument_list|,
name|Byte
operator|.
name|MAX_VALUE
argument_list|)
block|}
argument_list|)
expr_stmt|;
comment|// Create a table with smallint/tinyint columns, load data, and query from Hive.
name|driver
operator|.
name|run
argument_list|(
literal|"drop table if exists "
operator|+
name|readTblName
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create external table "
operator|+
name|readTblName
operator|+
literal|" (my_small_int smallint, my_tiny_int tinyint)"
operator|+
literal|" row format delimited fields terminated by '\t' stored as textfile"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"load data local inpath '"
operator|+
name|dataDir
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
operator|+
literal|"' into table "
operator|+
name|readTblName
argument_list|)
expr_stmt|;
name|PigServer
name|server
init|=
name|HCatBaseTest
operator|.
name|createPigServer
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"data = load '"
operator|+
name|readTblName
operator|+
literal|"' using org.apache.hive.hcatalog.pig.HCatLoader();"
argument_list|)
expr_stmt|;
comment|// Ensure Pig schema is correct.
name|Schema
name|schema
init|=
name|server
operator|.
name|dumpSchema
argument_list|(
literal|"data"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|schema
operator|.
name|getFields
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"my_small_int"
argument_list|,
name|schema
operator|.
name|getField
argument_list|(
literal|0
argument_list|)
operator|.
name|alias
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DataType
operator|.
name|INTEGER
argument_list|,
name|schema
operator|.
name|getField
argument_list|(
literal|0
argument_list|)
operator|.
name|type
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"my_tiny_int"
argument_list|,
name|schema
operator|.
name|getField
argument_list|(
literal|1
argument_list|)
operator|.
name|alias
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|DataType
operator|.
name|INTEGER
argument_list|,
name|schema
operator|.
name|getField
argument_list|(
literal|1
argument_list|)
operator|.
name|type
argument_list|)
expr_stmt|;
comment|// Ensure Pig can read data correctly.
name|Iterator
argument_list|<
name|Tuple
argument_list|>
name|it
init|=
name|server
operator|.
name|openIterator
argument_list|(
literal|"data"
argument_list|)
decl_stmt|;
name|Tuple
name|t
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|Short
operator|.
name|MIN_VALUE
argument_list|)
argument_list|,
name|t
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|Byte
operator|.
name|MIN_VALUE
argument_list|)
argument_list|,
name|t
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|=
name|it
operator|.
name|next
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|Short
operator|.
name|MAX_VALUE
argument_list|)
argument_list|,
name|t
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|Byte
operator|.
name|MAX_VALUE
argument_list|)
argument_list|,
name|t
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|it
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
comment|// Ensure Pig can write correctly to smallint/tinyint columns. This means values within the
comment|// bounds of the column type are written, and values outside throw an exception.
name|driver
operator|.
name|run
argument_list|(
literal|"drop table if exists "
operator|+
name|writeTblName
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create table "
operator|+
name|writeTblName
operator|+
literal|" (my_small_int smallint, my_tiny_int tinyint) stored as rcfile"
argument_list|)
expr_stmt|;
comment|// Values within the column type bounds.
name|HcatTestUtils
operator|.
name|createTestDataFile
argument_list|(
name|writeDataFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{
name|String
operator|.
name|format
argument_list|(
literal|"%d\t%d"
argument_list|,
name|Short
operator|.
name|MIN_VALUE
argument_list|,
name|Byte
operator|.
name|MIN_VALUE
argument_list|)
block|,
name|String
operator|.
name|format
argument_list|(
literal|"%d\t%d"
argument_list|,
name|Short
operator|.
name|MAX_VALUE
argument_list|,
name|Byte
operator|.
name|MAX_VALUE
argument_list|)
block|}
argument_list|)
expr_stmt|;
name|smallTinyIntBoundsCheckHelper
argument_list|(
name|writeDataFile
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
argument_list|,
name|ExecJob
operator|.
name|JOB_STATUS
operator|.
name|COMPLETED
argument_list|)
expr_stmt|;
comment|// Values outside the column type bounds will fail at runtime.
name|HcatTestUtils
operator|.
name|createTestDataFile
argument_list|(
name|TEST_DATA_DIR
operator|+
literal|"/shortTooSmall.tsv"
argument_list|,
operator|new
name|String
index|[]
block|{
name|String
operator|.
name|format
argument_list|(
literal|"%d\t%d"
argument_list|,
name|Short
operator|.
name|MIN_VALUE
operator|-
literal|1
argument_list|,
literal|0
argument_list|)
block|}
argument_list|)
expr_stmt|;
name|smallTinyIntBoundsCheckHelper
argument_list|(
name|TEST_DATA_DIR
operator|+
literal|"/shortTooSmall.tsv"
argument_list|,
name|ExecJob
operator|.
name|JOB_STATUS
operator|.
name|FAILED
argument_list|)
expr_stmt|;
name|HcatTestUtils
operator|.
name|createTestDataFile
argument_list|(
name|TEST_DATA_DIR
operator|+
literal|"/shortTooBig.tsv"
argument_list|,
operator|new
name|String
index|[]
block|{
name|String
operator|.
name|format
argument_list|(
literal|"%d\t%d"
argument_list|,
name|Short
operator|.
name|MAX_VALUE
operator|+
literal|1
argument_list|,
literal|0
argument_list|)
block|}
argument_list|)
expr_stmt|;
name|smallTinyIntBoundsCheckHelper
argument_list|(
name|TEST_DATA_DIR
operator|+
literal|"/shortTooBig.tsv"
argument_list|,
name|ExecJob
operator|.
name|JOB_STATUS
operator|.
name|FAILED
argument_list|)
expr_stmt|;
name|HcatTestUtils
operator|.
name|createTestDataFile
argument_list|(
name|TEST_DATA_DIR
operator|+
literal|"/byteTooSmall.tsv"
argument_list|,
operator|new
name|String
index|[]
block|{
name|String
operator|.
name|format
argument_list|(
literal|"%d\t%d"
argument_list|,
literal|0
argument_list|,
name|Byte
operator|.
name|MIN_VALUE
operator|-
literal|1
argument_list|)
block|}
argument_list|)
expr_stmt|;
name|smallTinyIntBoundsCheckHelper
argument_list|(
name|TEST_DATA_DIR
operator|+
literal|"/byteTooSmall.tsv"
argument_list|,
name|ExecJob
operator|.
name|JOB_STATUS
operator|.
name|FAILED
argument_list|)
expr_stmt|;
name|HcatTestUtils
operator|.
name|createTestDataFile
argument_list|(
name|TEST_DATA_DIR
operator|+
literal|"/byteTooBig.tsv"
argument_list|,
operator|new
name|String
index|[]
block|{
name|String
operator|.
name|format
argument_list|(
literal|"%d\t%d"
argument_list|,
literal|0
argument_list|,
name|Byte
operator|.
name|MAX_VALUE
operator|+
literal|1
argument_list|)
block|}
argument_list|)
expr_stmt|;
name|smallTinyIntBoundsCheckHelper
argument_list|(
name|TEST_DATA_DIR
operator|+
literal|"/byteTooBig.tsv"
argument_list|,
name|ExecJob
operator|.
name|JOB_STATUS
operator|.
name|FAILED
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|smallTinyIntBoundsCheckHelper
parameter_list|(
name|String
name|data
parameter_list|,
name|ExecJob
operator|.
name|JOB_STATUS
name|expectedStatus
parameter_list|)
throws|throws
name|Exception
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"drop table if exists test_tbl"
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"create table test_tbl (my_small_int smallint, my_tiny_int tinyint) stored as rcfile"
argument_list|)
expr_stmt|;
name|PigServer
name|server
init|=
name|HCatBaseTest
operator|.
name|createPigServer
argument_list|(
literal|false
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
literal|"data = load '"
operator|+
name|data
operator|+
literal|"' using PigStorage('\t') as (my_small_int:int, my_tiny_int:int);"
argument_list|)
expr_stmt|;
name|server
operator|.
name|registerQuery
argument_list|(
literal|"store data into 'test_tbl' using org.apache.hive.hcatalog.pig.HCatStorer('','','-onOutOfRangeValue Throw');"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ExecJob
argument_list|>
name|jobs
init|=
name|server
operator|.
name|executeBatch
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedStatus
argument_list|,
name|jobs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

