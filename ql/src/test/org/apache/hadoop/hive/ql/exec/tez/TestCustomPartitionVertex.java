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
name|exec
operator|.
name|tez
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|HashMultimap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Multimap
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
name|TezWork
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
name|io
operator|.
name|DataOutputBuffer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|UserPayload
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tez
operator|.
name|dag
operator|.
name|api
operator|.
name|VertexManagerPluginContext
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
name|nio
operator|.
name|ByteBuffer
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
name|mockito
operator|.
name|Mockito
operator|.
name|mock
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
name|when
import|;
end_import

begin_class
specifier|public
class|class
name|TestCustomPartitionVertex
block|{
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|5000
argument_list|)
specifier|public
name|void
name|testGetBytePayload
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|numBuckets
init|=
literal|10
decl_stmt|;
name|VertexManagerPluginContext
name|context
init|=
name|mock
argument_list|(
name|VertexManagerPluginContext
operator|.
name|class
argument_list|)
decl_stmt|;
name|CustomVertexConfiguration
name|vertexConf
init|=
operator|new
name|CustomVertexConfiguration
argument_list|(
name|numBuckets
argument_list|,
name|TezWork
operator|.
name|VertexType
operator|.
name|INITIALIZED_EDGES
argument_list|)
decl_stmt|;
name|DataOutputBuffer
name|dob
init|=
operator|new
name|DataOutputBuffer
argument_list|()
decl_stmt|;
name|vertexConf
operator|.
name|write
argument_list|(
name|dob
argument_list|)
expr_stmt|;
name|UserPayload
name|payload
init|=
name|UserPayload
operator|.
name|create
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|dob
operator|.
name|getData
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|context
operator|.
name|getUserPayload
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|payload
argument_list|)
expr_stmt|;
name|CustomPartitionVertex
name|vm
init|=
operator|new
name|CustomPartitionVertex
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|vm
operator|.
name|initialize
argument_list|()
expr_stmt|;
comment|// prepare empty routing table
name|Multimap
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|routingTable
init|=
name|HashMultimap
operator|.
expr|<
name|Integer
decl_stmt|,
name|Integer
decl|>
name|create
argument_list|()
decl_stmt|;
name|payload
operator|=
name|vm
operator|.
name|getBytePayload
argument_list|(
name|routingTable
argument_list|)
expr_stmt|;
comment|// get conf from user payload
name|CustomEdgeConfiguration
name|edgeConf
init|=
operator|new
name|CustomEdgeConfiguration
argument_list|()
decl_stmt|;
name|DataInputByteBuffer
name|dibb
init|=
operator|new
name|DataInputByteBuffer
argument_list|()
decl_stmt|;
name|dibb
operator|.
name|reset
argument_list|(
name|payload
operator|.
name|getPayload
argument_list|()
argument_list|)
expr_stmt|;
name|edgeConf
operator|.
name|readFields
argument_list|(
name|dibb
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|numBuckets
argument_list|,
name|edgeConf
operator|.
name|getNumBuckets
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

