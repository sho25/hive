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
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
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

begin_comment
comment|/**  * This test checks the {@link HCatConstants#HCAT_PIG_STORER_EXTERNAL_LOCATION} that we can set in the  * UDFContext of {@link HCatStorer} so that it writes to the specified external location.  *  * Since {@link HCatStorer} does not allow extra parameters in the constructor, we use {@link HCatStorerWrapper}  * that always treats the last parameter as the external path.  */
end_comment

begin_class
specifier|public
class|class
name|TestHCatStorerWrapper
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
name|Test
specifier|public
name|void
name|testStoreExternalTableWithExternalDir
parameter_list|()
throws|throws
name|Exception
block|{
name|File
name|tmpExternalDir
init|=
operator|new
name|File
argument_list|(
name|TEST_DATA_DIR
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|tmpExternalDir
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
name|String
name|part_val
init|=
literal|"100"
decl_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop table junit_external"
argument_list|)
expr_stmt|;
name|String
name|createTable
init|=
literal|"create external table junit_external(a int, b string) partitioned by (c string) stored as RCFILE"
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|driver
operator|.
name|run
argument_list|(
name|createTable
argument_list|)
operator|.
name|getResponseCode
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|LOOP_SIZE
init|=
literal|3
decl_stmt|;
name|String
index|[]
name|inputData
init|=
operator|new
name|String
index|[
name|LOOP_SIZE
operator|*
name|LOOP_SIZE
index|]
decl_stmt|;
name|int
name|k
init|=
literal|0
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
name|inputData
index|[
name|k
operator|++
index|]
operator|=
name|si
operator|+
literal|"\t"
operator|+
name|j
expr_stmt|;
block|}
block|}
name|HcatTestUtils
operator|.
name|createTestDataFile
argument_list|(
name|INPUT_FILE_NAME
argument_list|,
name|inputData
argument_list|)
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
name|logAndRegister
argument_list|(
name|server
argument_list|,
literal|"A = load '"
operator|+
name|INPUT_FILE_NAME
operator|+
literal|"' as (a:int, b:chararray);"
argument_list|)
expr_stmt|;
name|logAndRegister
argument_list|(
name|server
argument_list|,
literal|"store A into 'default.junit_external' using "
operator|+
name|HCatStorerWrapper
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"('c="
operator|+
name|part_val
operator|+
literal|"','"
operator|+
name|tmpExternalDir
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
literal|"');"
argument_list|)
expr_stmt|;
name|server
operator|.
name|executeBatch
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|tmpExternalDir
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
name|boolean
name|found
init|=
literal|false
decl_stmt|;
name|File
index|[]
name|f
init|=
name|tmpExternalDir
operator|.
name|listFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|f
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|File
name|fin
range|:
name|f
control|)
block|{
if|if
condition|(
name|fin
operator|.
name|getPath
argument_list|()
operator|.
name|contains
argument_list|(
literal|"part-m-00000"
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
name|Assert
operator|.
name|assertTrue
argument_list|(
name|found
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"select * from junit_external"
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|res
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
name|res
argument_list|)
expr_stmt|;
name|driver
operator|.
name|run
argument_list|(
literal|"drop table junit_external"
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|String
argument_list|>
name|itr
init|=
name|res
operator|.
name|iterator
argument_list|()
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
name|Assert
operator|.
name|assertEquals
argument_list|(
name|si
operator|+
literal|"\t"
operator|+
name|j
operator|+
literal|"\t"
operator|+
name|part_val
argument_list|,
name|itr
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
block|}
block|}
end_class

end_unit

