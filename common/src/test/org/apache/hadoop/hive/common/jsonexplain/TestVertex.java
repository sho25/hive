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
name|TestVertex
block|{
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
name|testExtractOpTree
parameter_list|()
throws|throws
name|Exception
block|{
name|JSONObject
name|object
init|=
operator|new
name|JSONObject
argument_list|(
literal|"{\"Join:\":[{},{}]}"
argument_list|)
decl_stmt|;
name|Vertex
name|uut
init|=
operator|new
name|Vertex
argument_list|(
literal|"name"
argument_list|,
name|object
argument_list|,
literal|null
argument_list|,
name|tezJsonParser
argument_list|)
decl_stmt|;
name|uut
operator|.
name|extractOpTree
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|uut
operator|.
name|mergeJoinDummyVertexs
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
name|testExtractOpNonJsonChildrenShouldThrow
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|jsonString
init|=
literal|"{\"opName\":{\"children\":\"not-json\"}}"
decl_stmt|;
name|JSONObject
name|operator
init|=
operator|new
name|JSONObject
argument_list|(
name|jsonString
argument_list|)
decl_stmt|;
name|Vertex
name|uut
init|=
operator|new
name|Vertex
argument_list|(
literal|"name"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|tezJsonParser
argument_list|)
decl_stmt|;
try|try
block|{
name|uut
operator|.
name|extractOp
argument_list|(
name|operator
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Unsupported operator name's children operator is neither a jsonobject nor a jsonarray"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExtractOpNoChildrenOperatorId
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|jsonString
init|=
literal|"{\"opName\":{\"OperatorId:\":\"operator-id\"}}"
decl_stmt|;
name|JSONObject
name|operator
init|=
operator|new
name|JSONObject
argument_list|(
name|jsonString
argument_list|)
decl_stmt|;
name|Vertex
name|uut
init|=
operator|new
name|Vertex
argument_list|(
literal|"name"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|tezJsonParser
argument_list|)
decl_stmt|;
name|Op
name|result
init|=
name|uut
operator|.
name|extractOp
argument_list|(
name|operator
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"opName"
argument_list|,
name|result
operator|.
name|name
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"operator-id"
argument_list|,
name|result
operator|.
name|operatorId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|result
operator|.
name|children
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|result
operator|.
name|attrs
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
name|testExtractOpOneChild
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|jsonString
init|=
literal|"{\"opName\":{\"children\":{\"childName\":"
operator|+
literal|"{\"OperatorId:\":\"child-operator-id\"}}}}"
decl_stmt|;
name|JSONObject
name|operator
init|=
operator|new
name|JSONObject
argument_list|(
name|jsonString
argument_list|)
decl_stmt|;
name|Vertex
name|uut
init|=
operator|new
name|Vertex
argument_list|(
literal|"name"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|tezJsonParser
argument_list|)
decl_stmt|;
name|Op
name|result
init|=
name|uut
operator|.
name|extractOp
argument_list|(
name|operator
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"opName"
argument_list|,
name|result
operator|.
name|name
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|children
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"childName"
argument_list|,
name|result
operator|.
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|name
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"child-operator-id"
argument_list|,
name|result
operator|.
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|operatorId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExtractOpMultipleChildren
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|jsonString
init|=
literal|"{\"opName\":{\"children\":["
operator|+
literal|"{\"childName1\":{\"OperatorId:\":\"child-operator-id1\"}},"
operator|+
literal|"{\"childName2\":{\"OperatorId:\":\"child-operator-id2\"}}]}}"
decl_stmt|;
name|JSONObject
name|operator
init|=
operator|new
name|JSONObject
argument_list|(
name|jsonString
argument_list|)
decl_stmt|;
name|Vertex
name|uut
init|=
operator|new
name|Vertex
argument_list|(
literal|"name"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|tezJsonParser
argument_list|)
decl_stmt|;
name|Op
name|result
init|=
name|uut
operator|.
name|extractOp
argument_list|(
name|operator
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"opName"
argument_list|,
name|result
operator|.
name|name
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|result
operator|.
name|children
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"childName1"
argument_list|,
name|result
operator|.
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|name
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"child-operator-id1"
argument_list|,
name|result
operator|.
name|children
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|operatorId
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"childName2"
argument_list|,
name|result
operator|.
name|children
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|name
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"child-operator-id2"
argument_list|,
name|result
operator|.
name|children
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|operatorId
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

