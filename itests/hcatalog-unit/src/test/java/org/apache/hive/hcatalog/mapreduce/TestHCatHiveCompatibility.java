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
name|mapreduce
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
name|Iterator
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
name|hive
operator|.
name|hcatalog
operator|.
name|common
operator|.
name|HCatConstants
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
name|data
operator|.
name|Tuple
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

begin_class
specifier|public
class|class
name|TestHCatHiveCompatibility
extends|extends
name|HCatBaseTest
block|{
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
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|createInputData
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|LOOP_SIZE
init|=
literal|11
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
literal|0
init|;
name|i
operator|<
name|LOOP_SIZE
condition|;
name|i
operator|++
control|)
block|{
name|writer
operator|.
name|write
argument_list|(
name|i
operator|+
literal|"\t1\n"
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testUnpartedReadWrite
parameter_list|()
throws|throws
name|Exception
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"drop table if exists junit_unparted_noisd"
argument_list|)
expr_stmt|;
name|String
name|createTable
init|=
literal|"create table junit_unparted_noisd(a int) stored as RCFILE"
decl_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|createTable
argument_list|)
expr_stmt|;
comment|// assert that the table created has no hcat instrumentation, and that we're still able to read it.
name|Table
name|table
init|=
name|client
operator|.
name|getTable
argument_list|(
literal|"default"
argument_list|,
literal|"junit_unparted_noisd"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getInputFormat
argument_list|()
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HIVE_RCFILE_IF_CLASS
argument_list|)
argument_list|)
expr_stmt|;
name|PigServer
name|server
init|=
name|createPigServer
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|logAndRegister
argument_list|(
name|server
argument_list|,
literal|"A = load '"
operator|+
name|INPUT_FILE_NAME
operator|+
literal|"' as (a:int);"
argument_list|)
expr_stmt|;
name|logAndRegister
argument_list|(
name|server
argument_list|,
literal|"store A into 'default.junit_unparted_noisd' using org.apache.hive.hcatalog.pig.HCatStorer();"
argument_list|)
expr_stmt|;
name|logAndRegister
argument_list|(
name|server
argument_list|,
literal|"B = load 'default.junit_unparted_noisd' using org.apache.hive.hcatalog.pig.HCatLoader();"
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|Tuple
argument_list|>
name|itr
init|=
name|server
operator|.
name|openIterator
argument_list|(
literal|"B"
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|itr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Tuple
name|t
init|=
name|itr
operator|.
name|next
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|t
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
name|Assert
operator|.
name|assertFalse
argument_list|(
name|itr
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|11
argument_list|,
name|i
argument_list|)
expr_stmt|;
comment|// assert that the table created still has no hcat instrumentation
name|Table
name|table2
init|=
name|client
operator|.
name|getTable
argument_list|(
literal|"default"
argument_list|,
literal|"junit_unparted_noisd"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|table2
operator|.
name|getSd
argument_list|()
operator|.
name|getInputFormat
argument_list|()
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HIVE_RCFILE_IF_CLASS
argument_list|)
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop table junit_unparted_noisd"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPartedRead
parameter_list|()
throws|throws
name|Exception
block|{
name|driver
operator|.
name|run
argument_list|(
literal|"drop table if exists junit_parted_noisd"
argument_list|)
expr_stmt|;
name|String
name|createTable
init|=
literal|"create table junit_parted_noisd(a int) partitioned by (b string) stored as RCFILE"
decl_stmt|;
name|driver
operator|.
name|run
argument_list|(
name|createTable
argument_list|)
expr_stmt|;
comment|// assert that the table created has no hcat instrumentation, and that we're still able to read it.
name|Table
name|table
init|=
name|client
operator|.
name|getTable
argument_list|(
literal|"default"
argument_list|,
literal|"junit_parted_noisd"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|table
operator|.
name|getSd
argument_list|()
operator|.
name|getInputFormat
argument_list|()
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HIVE_RCFILE_IF_CLASS
argument_list|)
argument_list|)
expr_stmt|;
name|PigServer
name|server
init|=
name|createPigServer
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|logAndRegister
argument_list|(
name|server
argument_list|,
literal|"A = load '"
operator|+
name|INPUT_FILE_NAME
operator|+
literal|"' as (a:int);"
argument_list|)
expr_stmt|;
name|logAndRegister
argument_list|(
name|server
argument_list|,
literal|"store A into 'default.junit_parted_noisd' using org.apache.hive.hcatalog.pig.HCatStorer('b=42');"
argument_list|)
expr_stmt|;
name|logAndRegister
argument_list|(
name|server
argument_list|,
literal|"B = load 'default.junit_parted_noisd' using org.apache.hive.hcatalog.pig.HCatLoader();"
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|Tuple
argument_list|>
name|itr
init|=
name|server
operator|.
name|openIterator
argument_list|(
literal|"B"
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|itr
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Tuple
name|t
init|=
name|itr
operator|.
name|next
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|t
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Contains explicit field "a" and partition "b".
name|Assert
operator|.
name|assertEquals
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|t
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
literal|"42"
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
name|Assert
operator|.
name|assertFalse
argument_list|(
name|itr
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|11
argument_list|,
name|i
argument_list|)
expr_stmt|;
comment|// assert that the table created still has no hcat instrumentation
name|Table
name|table2
init|=
name|client
operator|.
name|getTable
argument_list|(
literal|"default"
argument_list|,
literal|"junit_parted_noisd"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|table2
operator|.
name|getSd
argument_list|()
operator|.
name|getInputFormat
argument_list|()
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HIVE_RCFILE_IF_CLASS
argument_list|)
argument_list|)
expr_stmt|;
comment|// assert that there is one partition present, and it had hcat instrumentation inserted when it was created.
name|Partition
name|ptn
init|=
name|client
operator|.
name|getPartition
argument_list|(
literal|"default"
argument_list|,
literal|"junit_parted_noisd"
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"42"
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|ptn
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|ptn
operator|.
name|getSd
argument_list|()
operator|.
name|getInputFormat
argument_list|()
operator|.
name|equals
argument_list|(
name|HCatConstants
operator|.
name|HIVE_RCFILE_IF_CLASS
argument_list|)
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop table junit_unparted_noisd"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

