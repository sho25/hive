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
name|metastore
package|;
end_package

begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

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
name|MetaException
import|;
end_import

begin_class
specifier|public
class|class
name|TestPartitions
extends|extends
name|MetaStoreTestBase
block|{
specifier|public
name|TestPartitions
parameter_list|()
throws|throws
name|Exception
block|{   }
specifier|public
name|void
name|testPartitions
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|DB
name|db
init|=
name|DB
operator|.
name|createDB
argument_list|(
literal|"foo1"
argument_list|,
name|conf_
argument_list|)
decl_stmt|;
name|Table
name|bar1
init|=
name|Table
operator|.
name|create
argument_list|(
name|db
argument_list|,
literal|"bar1"
argument_list|,
name|createSchema
argument_list|(
literal|"foo1"
argument_list|,
literal|"bar1"
argument_list|)
argument_list|,
name|conf_
argument_list|)
decl_stmt|;
block|{
name|List
argument_list|<
name|String
argument_list|>
name|partitions
init|=
name|bar1
operator|.
name|getPartitions
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|partitions
operator|.
name|size
argument_list|()
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
name|Path
name|part1
init|=
operator|new
name|Path
argument_list|(
name|bar1
operator|.
name|getPath
argument_list|()
argument_list|,
literal|"ds=2008-01-01"
argument_list|)
decl_stmt|;
name|Path
name|part2
init|=
operator|new
name|Path
argument_list|(
name|bar1
operator|.
name|getPath
argument_list|()
argument_list|,
literal|"ds=2008-01-02"
argument_list|)
decl_stmt|;
name|fileSys_
operator|.
name|mkdirs
argument_list|(
name|part1
argument_list|)
expr_stmt|;
name|fileSys_
operator|.
name|mkdirs
argument_list|(
name|part2
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|partitions
init|=
name|bar1
operator|.
name|getPartitions
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|partitions
operator|.
name|size
argument_list|()
operator|==
literal|2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|partitions
operator|.
name|contains
argument_list|(
literal|"ds=2008-01-01"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|partitions
operator|.
name|contains
argument_list|(
literal|"ds=2008-01-02"
argument_list|)
argument_list|)
expr_stmt|;
name|cleanup
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MetaException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
block|}
end_class

end_unit

