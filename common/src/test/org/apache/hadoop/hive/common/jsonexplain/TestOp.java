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
name|common
operator|.
name|jsonexplain
package|;
end_package

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|JsonNode
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|databind
operator|.
name|ObjectMapper
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
name|jsonexplain
operator|.
name|tez
operator|.
name|TezJsonParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|json
operator|.
name|JSONObject
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
name|java
operator|.
name|util
operator|.
name|*
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
name|TestOp
block|{
specifier|private
name|ObjectMapper
name|objectMapper
init|=
operator|new
name|ObjectMapper
argument_list|()
decl_stmt|;
specifier|private
name|TezJsonParser
name|tezJsonParser
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|tezJsonParser
operator|=
operator|new
name|TezJsonParser
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInlineJoinOpJsonHandling
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|jsonString
init|=
literal|"{"
operator|+
literal|"\"input vertices:\":{\"a\":\"AVERTEX\"},"
operator|+
literal|"\"condition map:\": ["
operator|+
literal|"{\"c1\": \"{\\\"type\\\": \\\"type\\\", \\\"left\\\": \\\"left\\\", "
operator|+
literal|"\\\"right\\\": \\\"right\\\"}\"}],"
operator|+
literal|"\"keys:\":{\"left\":\"AKEY\", \"right\":\"BKEY\"}}"
decl_stmt|;
name|JSONObject
name|mapJoin
init|=
operator|new
name|JSONObject
argument_list|(
name|jsonString
argument_list|)
decl_stmt|;
name|Vertex
name|vertexB
init|=
operator|new
name|Vertex
argument_list|(
literal|"vertex-b"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|tezJsonParser
argument_list|)
decl_stmt|;
name|Op
name|dummyOp
init|=
operator|new
name|Op
argument_list|(
literal|"Dummy Op"
argument_list|,
literal|"dummy-id"
argument_list|,
literal|"output-vertex-name"
argument_list|,
literal|null
argument_list|,
name|Collections
operator|.
name|EMPTY_LIST
argument_list|,
literal|null
argument_list|,
name|mapJoin
argument_list|,
literal|null
argument_list|,
name|tezJsonParser
argument_list|)
decl_stmt|;
name|vertexB
operator|.
name|outputOps
operator|.
name|add
argument_list|(
name|dummyOp
argument_list|)
expr_stmt|;
name|Vertex
name|vertexC
init|=
operator|new
name|Vertex
argument_list|(
literal|"vertex-c"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|tezJsonParser
argument_list|)
decl_stmt|;
name|vertexC
operator|.
name|outputOps
operator|.
name|add
argument_list|(
name|dummyOp
argument_list|)
expr_stmt|;
name|Vertex
name|vertexA
init|=
operator|new
name|Vertex
argument_list|(
literal|"vertex-a"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|tezJsonParser
argument_list|)
decl_stmt|;
name|vertexA
operator|.
name|tagToInput
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|vertexA
operator|.
name|tagToInput
operator|.
name|put
argument_list|(
literal|"left"
argument_list|,
literal|"vertex-b"
argument_list|)
expr_stmt|;
name|vertexA
operator|.
name|tagToInput
operator|.
name|put
argument_list|(
literal|"right"
argument_list|,
literal|"vertex-c"
argument_list|)
expr_stmt|;
name|vertexA
operator|.
name|parentConnections
operator|.
name|add
argument_list|(
operator|new
name|Connection
argument_list|(
literal|"left"
argument_list|,
name|vertexB
argument_list|)
argument_list|)
expr_stmt|;
name|vertexA
operator|.
name|parentConnections
operator|.
name|add
argument_list|(
operator|new
name|Connection
argument_list|(
literal|"right"
argument_list|,
name|vertexC
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attrs
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Op
name|uut
init|=
operator|new
name|Op
argument_list|(
literal|"Map Join Operator"
argument_list|,
literal|"op-id"
argument_list|,
literal|"output-vertex-name"
argument_list|,
literal|null
argument_list|,
name|Collections
operator|.
name|EMPTY_LIST
argument_list|,
name|attrs
argument_list|,
name|mapJoin
argument_list|,
name|vertexA
argument_list|,
name|tezJsonParser
argument_list|)
decl_stmt|;
name|uut
operator|.
name|inlineJoinOp
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|attrs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|result
init|=
name|attrs
operator|.
name|get
argument_list|(
literal|"Conds:"
argument_list|)
decl_stmt|;
name|String
name|expected
init|=
literal|"dummy-id.AKEY=dummy-id.BKEY(type)"
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|result
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

