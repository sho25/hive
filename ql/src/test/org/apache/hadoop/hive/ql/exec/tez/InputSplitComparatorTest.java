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
name|exec
operator|.
name|tez
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
name|mapred
operator|.
name|FileSplit
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
name|exec
operator|.
name|tez
operator|.
name|HiveSplitGenerator
operator|.
name|InputSplitComparator
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

begin_class
specifier|public
class|class
name|InputSplitComparatorTest
block|{
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|EMPTY
init|=
operator|new
name|String
index|[]
block|{}
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testCompare1
parameter_list|()
throws|throws
name|Exception
block|{
name|FileSplit
name|split1
init|=
operator|new
name|FileSplit
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/abc/def"
argument_list|)
argument_list|,
literal|2000L
argument_list|,
literal|500L
argument_list|,
name|EMPTY
argument_list|)
decl_stmt|;
name|FileSplit
name|split2
init|=
operator|new
name|FileSplit
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/abc/def"
argument_list|)
argument_list|,
literal|1000L
argument_list|,
literal|500L
argument_list|,
name|EMPTY
argument_list|)
decl_stmt|;
name|InputSplitComparator
name|comparator
init|=
operator|new
name|InputSplitComparator
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|comparator
operator|.
name|compare
argument_list|(
name|split1
argument_list|,
name|split2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

