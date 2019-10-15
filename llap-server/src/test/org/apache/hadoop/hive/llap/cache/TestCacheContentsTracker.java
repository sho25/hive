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
name|llap
operator|.
name|cache
package|;
end_package

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
name|LinkedList
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
name|common
operator|.
name|io
operator|.
name|CacheTag
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

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
operator|.
name|toCollection
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
name|assertNull
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
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doAnswer
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_comment
comment|/**  * Unit tests for TestCacheContentsTracker functions.  */
end_comment

begin_class
specifier|public
class|class
name|TestCacheContentsTracker
block|{
specifier|private
specifier|static
name|CacheContentsTracker
name|tracker
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
block|{
name|LowLevelCachePolicy
name|lowLevelCachePolicyMock
init|=
name|mock
argument_list|(
name|LowLevelCachePolicy
operator|.
name|class
argument_list|)
decl_stmt|;
name|EvictionListener
name|evictionListenerMock
init|=
name|mock
argument_list|(
name|EvictionListener
operator|.
name|class
argument_list|)
decl_stmt|;
name|tracker
operator|=
operator|new
name|CacheContentsTracker
argument_list|(
name|lowLevelCachePolicyMock
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|setEvictionListener
argument_list|(
name|evictionListenerMock
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests parent CacheTag generation by checking each step when traversing from 3rd level    * partition to DB level.    */
annotation|@
name|Test
specifier|public
name|void
name|testParentCacheTagGeneration
parameter_list|()
block|{
name|CacheTag
name|db
init|=
name|cacheTagBuilder
argument_list|(
literal|"dbname"
argument_list|)
decl_stmt|;
name|CacheTag
name|table
init|=
name|cacheTagBuilder
argument_list|(
literal|"dbname.tablename"
argument_list|)
decl_stmt|;
name|CacheTag
name|p
init|=
name|cacheTagBuilder
argument_list|(
literal|"dbname.tablename"
argument_list|,
literal|"p=v1"
argument_list|)
decl_stmt|;
name|CacheTag
name|pp
init|=
name|cacheTagBuilder
argument_list|(
literal|"dbname.tablename"
argument_list|,
literal|"p=v1"
argument_list|,
literal|"pp=vv1"
argument_list|)
decl_stmt|;
name|CacheTag
name|ppp
init|=
name|cacheTagBuilder
argument_list|(
literal|"dbname.tablename"
argument_list|,
literal|"p=v1"
argument_list|,
literal|"pp=vv1"
argument_list|,
literal|"ppp=vvv1"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|pp
operator|.
name|compareTo
argument_list|(
name|CacheTag
operator|.
name|createParentCacheTag
argument_list|(
name|ppp
argument_list|)
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|p
operator|.
name|compareTo
argument_list|(
name|CacheTag
operator|.
name|createParentCacheTag
argument_list|(
name|pp
argument_list|)
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|table
operator|.
name|compareTo
argument_list|(
name|CacheTag
operator|.
name|createParentCacheTag
argument_list|(
name|p
argument_list|)
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|db
operator|.
name|compareTo
argument_list|(
name|CacheTag
operator|.
name|createParentCacheTag
argument_list|(
name|table
argument_list|)
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|CacheTag
operator|.
name|createParentCacheTag
argument_list|(
name|db
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Caches some mock buffers and checks summary produced by CacheContentsTracker. Later this is    * done again after some mock buffers were evicted.    */
annotation|@
name|Test
specifier|public
name|void
name|testAggregatedStatsGeneration
parameter_list|()
block|{
name|cacheTestBuffers
argument_list|()
expr_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|tracker
operator|.
name|debugDumpShort
argument_list|(
name|sb
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_CACHE_STATE_WHEN_FULL
argument_list|,
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|evictSomeTestBuffers
argument_list|()
expr_stmt|;
name|sb
operator|=
operator|new
name|StringBuilder
argument_list|()
expr_stmt|;
name|tracker
operator|.
name|debugDumpShort
argument_list|(
name|sb
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED_CACHE_STATE_AFTER_EVICTION
argument_list|,
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|LlapCacheableBuffer
name|createMockBuffer
parameter_list|(
name|long
name|size
parameter_list|,
name|CacheTag
name|cacheTag
parameter_list|)
block|{
name|LlapCacheableBuffer
name|llapCacheableBufferMock
init|=
name|mock
argument_list|(
name|LlapCacheableBuffer
operator|.
name|class
argument_list|)
decl_stmt|;
name|doAnswer
argument_list|(
name|invocationOnMock
lambda|->
block|{
return|return
name|size
return|;
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|llapCacheableBufferMock
argument_list|)
operator|.
name|getMemoryUsage
argument_list|()
expr_stmt|;
name|doAnswer
argument_list|(
name|invocationOnMock
lambda|->
block|{
return|return
name|cacheTag
return|;
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|llapCacheableBufferMock
argument_list|)
operator|.
name|getTag
argument_list|()
expr_stmt|;
return|return
name|llapCacheableBufferMock
return|;
block|}
specifier|private
specifier|static
name|CacheTag
name|cacheTagBuilder
parameter_list|(
name|String
name|dbAndTable
parameter_list|,
name|String
modifier|...
name|partitions
parameter_list|)
block|{
if|if
condition|(
name|partitions
operator|!=
literal|null
operator|&&
name|partitions
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|LinkedList
argument_list|<
name|String
argument_list|>
name|parts
init|=
name|Arrays
operator|.
name|stream
argument_list|(
name|partitions
argument_list|)
operator|.
name|collect
argument_list|(
name|toCollection
argument_list|(
name|LinkedList
operator|::
operator|new
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|CacheTag
operator|.
name|build
argument_list|(
name|dbAndTable
argument_list|,
name|parts
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|CacheTag
operator|.
name|build
argument_list|(
name|dbAndTable
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
name|void
name|cacheTestBuffers
parameter_list|()
block|{
name|tracker
operator|.
name|cache
argument_list|(
name|createMockBuffer
argument_list|(
literal|4
operator|*
literal|1024L
argument_list|,
name|cacheTagBuilder
argument_list|(
literal|"default.testtable"
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|cache
argument_list|(
name|createMockBuffer
argument_list|(
literal|2
operator|*
literal|1024L
argument_list|,
name|cacheTagBuilder
argument_list|(
literal|"otherdb.testtable"
argument_list|,
literal|"p=v1"
argument_list|,
literal|"pp=vv1"
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|cache
argument_list|(
name|createMockBuffer
argument_list|(
literal|32
operator|*
literal|1024L
argument_list|,
name|cacheTagBuilder
argument_list|(
literal|"otherdb.testtable"
argument_list|,
literal|"p=v1"
argument_list|,
literal|"pp=vv1"
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|cache
argument_list|(
name|createMockBuffer
argument_list|(
literal|64
operator|*
literal|1024L
argument_list|,
name|cacheTagBuilder
argument_list|(
literal|"otherdb.testtable"
argument_list|,
literal|"p=v1"
argument_list|,
literal|"pp=vv2"
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|cache
argument_list|(
name|createMockBuffer
argument_list|(
literal|128
operator|*
literal|1024L
argument_list|,
name|cacheTagBuilder
argument_list|(
literal|"otherdb.testtable"
argument_list|,
literal|"p=v2"
argument_list|,
literal|"pp=vv1"
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|cache
argument_list|(
name|createMockBuffer
argument_list|(
literal|256
operator|*
literal|1024L
argument_list|,
name|cacheTagBuilder
argument_list|(
literal|"otherdb.testtable2"
argument_list|,
literal|"p=v3"
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|cache
argument_list|(
name|createMockBuffer
argument_list|(
literal|512
operator|*
literal|1024
operator|*
literal|1024L
argument_list|,
name|cacheTagBuilder
argument_list|(
literal|"otherdb.testtable2"
argument_list|,
literal|"p=v3"
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|cache
argument_list|(
name|createMockBuffer
argument_list|(
literal|1024
operator|*
literal|1024
operator|*
literal|1024L
argument_list|,
name|cacheTagBuilder
argument_list|(
literal|"otherdb.testtable3"
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|cache
argument_list|(
name|createMockBuffer
argument_list|(
literal|2
operator|*
literal|1024
operator|*
literal|1024L
argument_list|,
name|cacheTagBuilder
argument_list|(
literal|"default.testtable"
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|evictSomeTestBuffers
parameter_list|()
block|{
name|tracker
operator|.
name|notifyEvicted
argument_list|(
name|createMockBuffer
argument_list|(
literal|32
operator|*
literal|1024L
argument_list|,
name|cacheTagBuilder
argument_list|(
literal|"otherdb.testtable"
argument_list|,
literal|"p=v1"
argument_list|,
literal|"pp=vv1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|notifyEvicted
argument_list|(
name|createMockBuffer
argument_list|(
literal|512
operator|*
literal|1024
operator|*
literal|1024L
argument_list|,
name|cacheTagBuilder
argument_list|(
literal|"otherdb.testtable2"
argument_list|,
literal|"p=v3"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|notifyEvicted
argument_list|(
name|createMockBuffer
argument_list|(
literal|2
operator|*
literal|1024
operator|*
literal|1024L
argument_list|,
name|cacheTagBuilder
argument_list|(
literal|"default.testtable"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|notifyEvicted
argument_list|(
name|createMockBuffer
argument_list|(
literal|4
operator|*
literal|1024L
argument_list|,
name|cacheTagBuilder
argument_list|(
literal|"default.testtable"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|String
name|EXPECTED_CACHE_STATE_WHEN_FULL
init|=
literal|"\n"
operator|+
literal|"Cache state: \n"
operator|+
literal|"default : 2/2, 2101248/2101248\n"
operator|+
literal|"default.testtable : 2/2, 2101248/2101248\n"
operator|+
literal|"otherdb : 7/7, 1611106304/1611106304\n"
operator|+
literal|"otherdb.testtable : 4/4, 231424/231424\n"
operator|+
literal|"otherdb.testtable/p=v1 : 3/3, 100352/100352\n"
operator|+
literal|"otherdb.testtable/p=v1/pp=vv1 : 2/2, 34816/34816\n"
operator|+
literal|"otherdb.testtable/p=v1/pp=vv2 : 1/1, 65536/65536\n"
operator|+
literal|"otherdb.testtable/p=v2 : 1/1, 131072/131072\n"
operator|+
literal|"otherdb.testtable/p=v2/pp=vv1 : 1/1, 131072/131072\n"
operator|+
literal|"otherdb.testtable2 : 2/2, 537133056/537133056\n"
operator|+
literal|"otherdb.testtable2/p=v3 : 2/2, 537133056/537133056\n"
operator|+
literal|"otherdb.testtable3 : 1/1, 1073741824/1073741824"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|EXPECTED_CACHE_STATE_AFTER_EVICTION
init|=
literal|"\n"
operator|+
literal|"Cache state: \n"
operator|+
literal|"default : 0/2, 0/2101248\n"
operator|+
literal|"default.testtable : 0/2, 0/2101248\n"
operator|+
literal|"otherdb : 5/7, 1074202624/1611106304\n"
operator|+
literal|"otherdb.testtable : 3/4, 198656/231424\n"
operator|+
literal|"otherdb.testtable/p=v1 : 2/3, 67584/100352\n"
operator|+
literal|"otherdb.testtable/p=v1/pp=vv1 : 1/2, 2048/34816\n"
operator|+
literal|"otherdb.testtable/p=v1/pp=vv2 : 1/1, 65536/65536\n"
operator|+
literal|"otherdb.testtable/p=v2 : 1/1, 131072/131072\n"
operator|+
literal|"otherdb.testtable/p=v2/pp=vv1 : 1/1, 131072/131072\n"
operator|+
literal|"otherdb.testtable2 : 1/2, 262144/537133056\n"
operator|+
literal|"otherdb.testtable2/p=v3 : 1/2, 262144/537133056\n"
operator|+
literal|"otherdb.testtable3 : 1/1, 1073741824/1073741824"
decl_stmt|;
block|}
end_class

end_unit

