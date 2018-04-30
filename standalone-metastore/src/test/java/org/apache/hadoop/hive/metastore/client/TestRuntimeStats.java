begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
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
name|ObjectStore
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
name|annotation
operator|.
name|MetastoreUnitTest
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
name|RuntimeStat
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
name|minihms
operator|.
name|AbstractMetaStoreService
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
name|experimental
operator|.
name|categories
operator|.
name|Category
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
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|assertArrayEquals
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
name|Assert
operator|.
name|assertNotEquals
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
name|assertNotNull
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
annotation|@
name|Category
argument_list|(
name|MetastoreUnitTest
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestRuntimeStats
extends|extends
name|MetaStoreClientTest
block|{
specifier|private
specifier|final
name|AbstractMetaStoreService
name|metaStore
decl_stmt|;
specifier|private
name|IMetaStoreClient
name|client
decl_stmt|;
specifier|private
name|String
name|metastoreName
decl_stmt|;
specifier|public
name|TestRuntimeStats
parameter_list|(
name|String
name|name
parameter_list|,
name|AbstractMetaStoreService
name|metaStore
parameter_list|)
throws|throws
name|Exception
block|{
name|this
operator|.
name|metastoreName
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|metaStore
operator|=
name|metaStore
expr_stmt|;
name|this
operator|.
name|metaStore
operator|.
name|start
argument_list|()
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
name|client
operator|=
name|metaStore
operator|.
name|getClient
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
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
name|client
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRuntimeStatHandling
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|RuntimeStat
argument_list|>
name|rs0
init|=
name|getRuntimeStats
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|rs0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|rs0
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|RuntimeStat
name|stat
init|=
name|createStat
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|client
operator|.
name|addRuntimeStat
argument_list|(
name|stat
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RuntimeStat
argument_list|>
name|rs1
init|=
name|getRuntimeStats
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|rs1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rs1
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|stat
operator|.
name|getPayload
argument_list|()
argument_list|,
name|rs1
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getPayload
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|stat
operator|.
name|getWeight
argument_list|()
argument_list|,
name|rs1
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getWeight
argument_list|()
argument_list|)
expr_stmt|;
comment|// server sets createtime
name|assertNotEquals
argument_list|(
name|stat
operator|.
name|getCreateTime
argument_list|()
argument_list|,
name|rs1
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getCreateTime
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|.
name|addRuntimeStat
argument_list|(
name|createStat
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|addRuntimeStat
argument_list|(
name|createStat
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|addRuntimeStat
argument_list|(
name|createStat
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RuntimeStat
argument_list|>
name|rs2
init|=
name|getRuntimeStats
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|rs2
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
name|testCleanup
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectStore
name|objStore
init|=
operator|new
name|ObjectStore
argument_list|()
decl_stmt|;
name|objStore
operator|.
name|setConf
argument_list|(
name|metaStore
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|objStore
operator|.
name|deleteRuntimeStats
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|objStore
operator|.
name|addRuntimeStat
argument_list|(
name|createStat
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|objStore
operator|.
name|addRuntimeStat
argument_list|(
name|createStat
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|deleted
init|=
name|objStore
operator|.
name|deleteRuntimeStats
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|deleted
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RuntimeStat
argument_list|>
name|all
init|=
name|getRuntimeStats
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|all
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|all
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getWeight
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReading
parameter_list|()
throws|throws
name|Exception
block|{
name|ObjectStore
name|objStore
init|=
operator|new
name|ObjectStore
argument_list|()
decl_stmt|;
name|objStore
operator|.
name|setConf
argument_list|(
name|metaStore
operator|.
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
name|objStore
operator|.
name|deleteRuntimeStats
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|objStore
operator|.
name|addRuntimeStat
argument_list|(
name|createStat
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|objStore
operator|.
name|addRuntimeStat
argument_list|(
name|createStat
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|objStore
operator|.
name|addRuntimeStat
argument_list|(
name|createStat
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RuntimeStat
argument_list|>
name|g0
init|=
name|client
operator|.
name|getRuntimeStats
argument_list|(
literal|3
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|g0
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|g0
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getWeight
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|ct
init|=
name|g0
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getCreateTime
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RuntimeStat
argument_list|>
name|g1
init|=
name|client
operator|.
name|getRuntimeStats
argument_list|(
literal|3
argument_list|,
name|ct
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|g1
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|g1
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getWeight
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|g1
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getWeight
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|ct1
init|=
name|g1
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getCreateTime
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RuntimeStat
argument_list|>
name|g2
init|=
name|client
operator|.
name|getRuntimeStats
argument_list|(
literal|3
argument_list|,
name|ct1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|g2
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|List
argument_list|<
name|RuntimeStat
argument_list|>
name|getRuntimeStats
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|client
operator|.
name|getRuntimeStats
argument_list|(
operator|-
literal|1
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
specifier|private
name|RuntimeStat
name|createStat
parameter_list|(
name|int
name|w
parameter_list|)
block|{
name|byte
index|[]
name|payload
init|=
operator|new
name|byte
index|[
name|w
index|]
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
name|payload
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|payload
index|[
name|i
index|]
operator|=
literal|'x'
expr_stmt|;
block|}
name|RuntimeStat
name|stat
init|=
operator|new
name|RuntimeStat
argument_list|()
decl_stmt|;
name|stat
operator|.
name|setWeight
argument_list|(
name|w
argument_list|)
expr_stmt|;
name|stat
operator|.
name|setPayload
argument_list|(
name|payload
argument_list|)
expr_stmt|;
return|return
name|stat
return|;
block|}
block|}
end_class

end_unit

