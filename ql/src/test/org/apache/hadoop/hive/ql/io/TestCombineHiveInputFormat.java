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
name|io
package|;
end_package

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
name|ql
operator|.
name|exec
operator|.
name|Utilities
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
name|MapredWork
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
name|TableDesc
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
name|FileInputFormat
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
name|InputSplit
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
name|JobConf
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
name|RecordReader
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
name|Reporter
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
name|util
operator|.
name|ReflectionUtils
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
name|LinkedHashMap
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

begin_comment
comment|/**  * Unittest for CombineHiveInputFormat.  */
end_comment

begin_class
specifier|public
class|class
name|TestCombineHiveInputFormat
extends|extends
name|TestCase
block|{
specifier|public
name|void
name|testAvoidSplitCombination
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|JobConf
name|job
init|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|TableDesc
name|tblDesc
init|=
name|Utilities
operator|.
name|defaultTd
decl_stmt|;
name|tblDesc
operator|.
name|setInputFileFormatClass
argument_list|(
name|TestSkipCombineInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|PartitionDesc
name|partDesc
init|=
operator|new
name|PartitionDesc
argument_list|(
name|tblDesc
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|LinkedHashMap
argument_list|<
name|Path
argument_list|,
name|PartitionDesc
argument_list|>
name|pt
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|pt
operator|.
name|put
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/tmp/testfolder1"
argument_list|)
argument_list|,
name|partDesc
argument_list|)
expr_stmt|;
name|pt
operator|.
name|put
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/tmp/testfolder2"
argument_list|)
argument_list|,
name|partDesc
argument_list|)
expr_stmt|;
name|MapredWork
name|mrwork
init|=
operator|new
name|MapredWork
argument_list|()
decl_stmt|;
name|mrwork
operator|.
name|getMapWork
argument_list|()
operator|.
name|setPathToPartitionInfo
argument_list|(
name|pt
argument_list|)
expr_stmt|;
name|Path
name|mapWorkPath
init|=
operator|new
name|Path
argument_list|(
literal|"/tmp/"
operator|+
name|System
operator|.
name|getProperty
argument_list|(
literal|"user.name"
argument_list|)
argument_list|,
literal|"hive"
argument_list|)
decl_stmt|;
name|Utilities
operator|.
name|setMapRedWork
argument_list|(
name|conf
argument_list|,
name|mrwork
argument_list|,
name|mapWorkPath
argument_list|)
expr_stmt|;
try|try
block|{
name|Path
index|[]
name|paths
init|=
operator|new
name|Path
index|[
literal|2
index|]
decl_stmt|;
name|paths
index|[
literal|0
index|]
operator|=
operator|new
name|Path
argument_list|(
literal|"/tmp/testfolder1"
argument_list|)
expr_stmt|;
name|paths
index|[
literal|1
index|]
operator|=
operator|new
name|Path
argument_list|(
literal|"/tmp/testfolder2"
argument_list|)
expr_stmt|;
name|CombineHiveInputFormat
name|combineInputFormat
init|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|CombineHiveInputFormat
operator|.
name|class
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|combineInputFormat
operator|.
name|pathToPartitionInfo
operator|=
name|Utilities
operator|.
name|getMapWork
argument_list|(
name|conf
argument_list|)
operator|.
name|getPathToPartitionInfo
argument_list|()
expr_stmt|;
name|Set
name|results
init|=
name|combineInputFormat
operator|.
name|getNonCombinablePathIndices
argument_list|(
name|job
argument_list|,
name|paths
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Should have both path indices in the results set"
argument_list|,
literal|2
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// Cleanup the mapwork path
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
operator|.
name|delete
argument_list|(
name|mapWorkPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|TestSkipCombineInputFormat
extends|extends
name|FileInputFormat
implements|implements
name|CombineHiveInputFormat
operator|.
name|AvoidSplitCombination
block|{
annotation|@
name|Override
specifier|public
name|RecordReader
name|getRecordReader
parameter_list|(
name|InputSplit
name|inputSplit
parameter_list|,
name|JobConf
name|jobConf
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|shouldSkipCombine
parameter_list|(
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Skip combine for all paths
return|return
literal|true
return|;
block|}
block|}
block|}
end_class

end_unit

