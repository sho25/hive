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
name|io
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
name|HashMap
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
name|ql
operator|.
name|plan
operator|.
name|PartitionDesc
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
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * TestHiveFileFormatUtils.  */
end_comment

begin_class
specifier|public
class|class
name|TestHiveFileFormatUtils
block|{
annotation|@
name|Test
specifier|public
name|void
name|testGetPartitionDescFromPathRecursively
parameter_list|()
throws|throws
name|IOException
block|{
name|PartitionDesc
name|partDesc_3
init|=
operator|new
name|PartitionDesc
argument_list|()
decl_stmt|;
name|PartitionDesc
name|partDesc_4
init|=
operator|new
name|PartitionDesc
argument_list|()
decl_stmt|;
name|PartitionDesc
name|partDesc_5
init|=
operator|new
name|PartitionDesc
argument_list|()
decl_stmt|;
name|PartitionDesc
name|partDesc_6
init|=
operator|new
name|PartitionDesc
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
name|pathToPartitionInfo
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|pathToPartitionInfo
operator|.
name|put
argument_list|(
operator|new
name|Path
argument_list|(
literal|"file:///tbl/par1/part2/part3"
argument_list|)
argument_list|,
name|partDesc_3
argument_list|)
expr_stmt|;
name|pathToPartitionInfo
operator|.
name|put
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/tbl/par1/part2/part4"
argument_list|)
argument_list|,
name|partDesc_4
argument_list|)
expr_stmt|;
name|pathToPartitionInfo
operator|.
name|put
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/tbl/par1/part2/part5/"
argument_list|)
argument_list|,
name|partDesc_5
argument_list|)
expr_stmt|;
name|pathToPartitionInfo
operator|.
name|put
argument_list|(
operator|new
name|Path
argument_list|(
literal|"hdfs:///tbl/par1/part2/part6/"
argument_list|)
argument_list|,
name|partDesc_6
argument_list|)
expr_stmt|;
comment|// first group
name|PartitionDesc
name|ret
init|=
literal|null
decl_stmt|;
name|ret
operator|=
name|HiveFileFormatUtils
operator|.
name|getFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file:///tbl/par1/part2/part3"
argument_list|)
argument_list|,
name|IOPrepareCache
operator|.
name|get
argument_list|()
operator|.
name|allocatePartitionDescMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"file:///tbl/par1/part2/part3 not found."
argument_list|,
name|partDesc_3
argument_list|,
name|ret
argument_list|)
expr_stmt|;
name|ret
operator|=
name|HiveFileFormatUtils
operator|.
name|getFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tbl/par1/part2/part3"
argument_list|)
argument_list|,
name|IOPrepareCache
operator|.
name|get
argument_list|()
operator|.
name|allocatePartitionDescMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"/tbl/par1/part2/part3 not found."
argument_list|,
name|partDesc_3
argument_list|,
name|ret
argument_list|)
expr_stmt|;
name|boolean
name|exception
init|=
literal|false
decl_stmt|;
try|try
block|{
name|ret
operator|=
name|HiveFileFormatUtils
operator|.
name|getFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
operator|new
name|Path
argument_list|(
literal|"hdfs:///tbl/par1/part2/part3"
argument_list|)
argument_list|,
name|IOPrepareCache
operator|.
name|get
argument_list|()
operator|.
name|allocatePartitionDescMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|exception
operator|=
literal|true
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"hdfs:///tbl/par1/part2/part3 should return null"
argument_list|,
literal|true
argument_list|,
name|exception
argument_list|)
expr_stmt|;
name|exception
operator|=
literal|false
expr_stmt|;
comment|// second group
name|ret
operator|=
name|HiveFileFormatUtils
operator|.
name|getFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file:///tbl/par1/part2/part4"
argument_list|)
argument_list|,
name|IOPrepareCache
operator|.
name|get
argument_list|()
operator|.
name|allocatePartitionDescMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"file:///tbl/par1/part2/part4 not found."
argument_list|,
name|partDesc_4
argument_list|,
name|ret
argument_list|)
expr_stmt|;
name|ret
operator|=
name|HiveFileFormatUtils
operator|.
name|getFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tbl/par1/part2/part4"
argument_list|)
argument_list|,
name|IOPrepareCache
operator|.
name|get
argument_list|()
operator|.
name|allocatePartitionDescMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"/tbl/par1/part2/part4 not found."
argument_list|,
name|partDesc_4
argument_list|,
name|ret
argument_list|)
expr_stmt|;
name|ret
operator|=
name|HiveFileFormatUtils
operator|.
name|getFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
operator|new
name|Path
argument_list|(
literal|"hdfs:///tbl/par1/part2/part4"
argument_list|)
argument_list|,
name|IOPrepareCache
operator|.
name|get
argument_list|()
operator|.
name|allocatePartitionDescMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hdfs:///tbl/par1/part2/part4 should  not found"
argument_list|,
name|partDesc_4
argument_list|,
name|ret
argument_list|)
expr_stmt|;
comment|// third group
name|ret
operator|=
name|HiveFileFormatUtils
operator|.
name|getFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file:///tbl/par1/part2/part5"
argument_list|)
argument_list|,
name|IOPrepareCache
operator|.
name|get
argument_list|()
operator|.
name|allocatePartitionDescMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"file:///tbl/par1/part2/part5 not found."
argument_list|,
name|partDesc_5
argument_list|,
name|ret
argument_list|)
expr_stmt|;
name|ret
operator|=
name|HiveFileFormatUtils
operator|.
name|getFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tbl/par1/part2/part5"
argument_list|)
argument_list|,
name|IOPrepareCache
operator|.
name|get
argument_list|()
operator|.
name|allocatePartitionDescMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"/tbl/par1/part2/part5 not found."
argument_list|,
name|partDesc_5
argument_list|,
name|ret
argument_list|)
expr_stmt|;
name|ret
operator|=
name|HiveFileFormatUtils
operator|.
name|getFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
operator|new
name|Path
argument_list|(
literal|"hdfs:///tbl/par1/part2/part5"
argument_list|)
argument_list|,
name|IOPrepareCache
operator|.
name|get
argument_list|()
operator|.
name|allocatePartitionDescMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hdfs:///tbl/par1/part2/part5 not found"
argument_list|,
name|partDesc_5
argument_list|,
name|ret
argument_list|)
expr_stmt|;
comment|// fourth group
try|try
block|{
name|ret
operator|=
name|HiveFileFormatUtils
operator|.
name|getFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
operator|new
name|Path
argument_list|(
literal|"file:///tbl/par1/part2/part6"
argument_list|)
argument_list|,
name|IOPrepareCache
operator|.
name|get
argument_list|()
operator|.
name|allocatePartitionDescMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|exception
operator|=
literal|true
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"file:///tbl/par1/part2/part6 should return null"
argument_list|,
literal|true
argument_list|,
name|exception
argument_list|)
expr_stmt|;
name|exception
operator|=
literal|false
expr_stmt|;
name|ret
operator|=
name|HiveFileFormatUtils
operator|.
name|getFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tbl/par1/part2/part6"
argument_list|)
argument_list|,
name|IOPrepareCache
operator|.
name|get
argument_list|()
operator|.
name|allocatePartitionDescMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"/tbl/par1/part2/part6 not found."
argument_list|,
name|partDesc_6
argument_list|,
name|ret
argument_list|)
expr_stmt|;
name|ret
operator|=
name|HiveFileFormatUtils
operator|.
name|getFromPathRecursively
argument_list|(
name|pathToPartitionInfo
argument_list|,
operator|new
name|Path
argument_list|(
literal|"hdfs:///tbl/par1/part2/part6"
argument_list|)
argument_list|,
name|IOPrepareCache
operator|.
name|get
argument_list|()
operator|.
name|allocatePartitionDescMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"hdfs:///tbl/par1/part2/part6 not found."
argument_list|,
name|partDesc_6
argument_list|,
name|ret
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

