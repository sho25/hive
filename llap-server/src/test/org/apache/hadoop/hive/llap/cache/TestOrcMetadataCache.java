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
name|llap
operator|.
name|cache
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|llap
operator|.
name|io
operator|.
name|api
operator|.
name|cache
operator|.
name|LowLevelCache
operator|.
name|Priority
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
name|llap
operator|.
name|io
operator|.
name|metadata
operator|.
name|OrcFileMetadata
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
name|llap
operator|.
name|io
operator|.
name|metadata
operator|.
name|OrcMetadataCache
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
name|llap
operator|.
name|io
operator|.
name|metadata
operator|.
name|OrcStripeMetadata
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
name|TestOrcMetadataCache
block|{
specifier|private
specifier|static
class|class
name|DummyCachePolicy
implements|implements
name|LowLevelCachePolicy
block|{
specifier|public
name|DummyCachePolicy
parameter_list|()
block|{     }
specifier|public
name|void
name|cache
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|,
name|Priority
name|pri
parameter_list|)
block|{     }
specifier|public
name|void
name|notifyLock
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|)
block|{     }
specifier|public
name|void
name|notifyUnlock
parameter_list|(
name|LlapCacheableBuffer
name|buffer
parameter_list|)
block|{     }
specifier|public
name|long
name|evictSomeBlocks
parameter_list|(
name|long
name|memoryToReserve
parameter_list|)
block|{
return|return
name|memoryToReserve
return|;
block|}
specifier|public
name|void
name|setEvictionListener
parameter_list|(
name|EvictionListener
name|listener
parameter_list|)
block|{     }
specifier|public
name|String
name|debugDumpForOom
parameter_list|()
block|{
return|return
literal|""
return|;
block|}
specifier|public
name|void
name|setParentDebugDumper
parameter_list|(
name|LlapOomDebugDump
name|dumper
parameter_list|)
block|{     }
block|}
specifier|private
specifier|static
class|class
name|DummyMemoryManager
implements|implements
name|MemoryManager
block|{
name|int
name|allocs
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|reserveMemory
parameter_list|(
name|long
name|memoryToReserve
parameter_list|,
name|boolean
name|waitForEviction
parameter_list|)
block|{
operator|++
name|allocs
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|releaseMemory
parameter_list|(
name|long
name|memUsage
parameter_list|)
block|{
operator|--
name|allocs
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|debugDumpForOom
parameter_list|()
block|{
return|return
literal|""
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetPut
parameter_list|()
throws|throws
name|Exception
block|{
name|DummyMemoryManager
name|mm
init|=
operator|new
name|DummyMemoryManager
argument_list|()
decl_stmt|;
name|DummyCachePolicy
name|cp
init|=
operator|new
name|DummyCachePolicy
argument_list|()
decl_stmt|;
name|OrcMetadataCache
name|cache
init|=
operator|new
name|OrcMetadataCache
argument_list|(
name|mm
argument_list|,
name|cp
argument_list|)
decl_stmt|;
name|OrcFileMetadata
name|ofm1
init|=
name|OrcFileMetadata
operator|.
name|createDummy
argument_list|(
literal|1
argument_list|)
decl_stmt|,
name|ofm2
init|=
name|OrcFileMetadata
operator|.
name|createDummy
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|ofm1
argument_list|,
name|cache
operator|.
name|putFileMetadata
argument_list|(
name|ofm1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|mm
operator|.
name|allocs
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|ofm2
argument_list|,
name|cache
operator|.
name|putFileMetadata
argument_list|(
name|ofm2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|mm
operator|.
name|allocs
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|ofm1
argument_list|,
name|cache
operator|.
name|getFileMetadata
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|ofm2
argument_list|,
name|cache
operator|.
name|getFileMetadata
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|OrcFileMetadata
name|ofm3
init|=
name|OrcFileMetadata
operator|.
name|createDummy
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|ofm1
argument_list|,
name|cache
operator|.
name|putFileMetadata
argument_list|(
name|ofm3
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|mm
operator|.
name|allocs
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|ofm1
argument_list|,
name|cache
operator|.
name|getFileMetadata
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|OrcStripeMetadata
name|osm1
init|=
name|OrcStripeMetadata
operator|.
name|createDummy
argument_list|(
literal|1
argument_list|)
decl_stmt|,
name|osm2
init|=
name|OrcStripeMetadata
operator|.
name|createDummy
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|osm1
argument_list|,
name|cache
operator|.
name|putStripeMetadata
argument_list|(
name|osm1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|mm
operator|.
name|allocs
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|osm2
argument_list|,
name|cache
operator|.
name|putStripeMetadata
argument_list|(
name|osm2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|mm
operator|.
name|allocs
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|osm1
argument_list|,
name|cache
operator|.
name|getStripeMetadata
argument_list|(
name|osm1
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|osm2
argument_list|,
name|cache
operator|.
name|getStripeMetadata
argument_list|(
name|osm2
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|OrcStripeMetadata
name|osm3
init|=
name|OrcStripeMetadata
operator|.
name|createDummy
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|osm1
argument_list|,
name|cache
operator|.
name|putStripeMetadata
argument_list|(
name|osm3
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|mm
operator|.
name|allocs
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|osm1
argument_list|,
name|cache
operator|.
name|getStripeMetadata
argument_list|(
name|osm3
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

