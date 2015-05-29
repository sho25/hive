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
name|udf
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
name|ql
operator|.
name|metadata
operator|.
name|HiveException
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
name|Text
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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
name|TestUDFJson
block|{
annotation|@
name|Test
specifier|public
name|void
name|testJson
parameter_list|()
throws|throws
name|HiveException
block|{
name|String
name|book0
init|=
literal|"{\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\""
operator|+
literal|",\"category\":\"reference\",\"price\":8.95}"
decl_stmt|;
name|String
name|backet0
init|=
literal|"[1,2,{\"b\":\"y\",\"a\":\"x\"}]"
decl_stmt|;
name|String
name|backet
init|=
literal|"["
operator|+
name|backet0
operator|+
literal|",[3,4],[5,6]]"
decl_stmt|;
name|String
name|backetFlat
init|=
name|backet0
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|backet0
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
operator|+
literal|",3,4,5,6]"
decl_stmt|;
name|String
name|book
init|=
literal|"["
operator|+
name|book0
operator|+
literal|",{\"author\":\"Herman Melville\",\"title\":\"Moby Dick\","
operator|+
literal|"\"category\":\"fiction\",\"price\":8.99"
operator|+
literal|",\"isbn\":\"0-553-21311-3\"},{\"author\":\"J. R. R. Tolkien\""
operator|+
literal|",\"title\":\"The Lord of the Rings\",\"category\":\"fiction\""
operator|+
literal|",\"reader\":[{\"age\":25,\"name\":\"bob\"},{\"age\":26,\"name\":\"jack\"}]"
operator|+
literal|",\"price\":22.99,\"isbn\":\"0-395-19395-8\"}]"
decl_stmt|;
name|String
name|json
init|=
literal|"{\"store\":{\"fruit\":[{\"weight\":8,\"type\":\"apple\"},"
operator|+
literal|"{\"weight\":9,\"type\":\"pear\"}],\"basket\":"
operator|+
name|backet
operator|+
literal|",\"book\":"
operator|+
name|book
operator|+
literal|",\"bicycle\":{\"price\":19.95,\"color\":\"red\"}}"
operator|+
literal|",\"email\":\"amy@only_for_json_udf_test.net\""
operator|+
literal|",\"owner\":\"amy\",\"zip code\":\"94025\",\"fb:testid\":\"1234\"}"
decl_stmt|;
name|UDFJson
name|udf
init|=
operator|new
name|UDFJson
argument_list|()
decl_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.owner"
argument_list|,
literal|"amy"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.bicycle"
argument_list|,
literal|"{\"price\":19.95,\"color\":\"red\"}"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.book"
argument_list|,
name|book
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.book[0]"
argument_list|,
name|book0
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.book[*]"
argument_list|,
name|book
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.book[0].category"
argument_list|,
literal|"reference"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.book[*].category"
argument_list|,
literal|"[\"reference\",\"fiction\",\"fiction\"]"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.book[*].reader[0].age"
argument_list|,
literal|"25"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.book[*].reader[*].age"
argument_list|,
literal|"[25,26]"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.basket[0][1]"
argument_list|,
literal|"2"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.basket[*]"
argument_list|,
name|backet
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.basket[*][0]"
argument_list|,
literal|"[1,3,5]"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.basket[0][*]"
argument_list|,
name|backet0
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.basket[*][*]"
argument_list|,
name|backetFlat
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.basket[0][2].b"
argument_list|,
literal|"y"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.basket[0][*].b"
argument_list|,
literal|"[\"y\"]"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.non_exist_key"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.book[10]"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.book[0].non_exist_key"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.basket[*].non_exist_key"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.basket[0][*].non_exist_key"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.store.basket[*][*].non_exist_key"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.zip code"
argument_list|,
literal|"94025"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
name|json
argument_list|,
literal|"$.fb:testid"
argument_list|,
literal|"1234"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"{\"a\":\"b\nc\"}"
argument_list|,
literal|"$.a"
argument_list|,
literal|"b\nc"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRootArray
parameter_list|()
throws|throws
name|HiveException
block|{
name|UDFJson
name|udf
init|=
operator|new
name|UDFJson
argument_list|()
decl_stmt|;
name|runTest
argument_list|(
literal|"[1,2,3]"
argument_list|,
literal|"$[0]"
argument_list|,
literal|"1"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[1,2,3]"
argument_list|,
literal|"$.[0]"
argument_list|,
literal|"1"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[1,2,3]"
argument_list|,
literal|"$.[1]"
argument_list|,
literal|"2"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[1,2,3]"
argument_list|,
literal|"$[1]"
argument_list|,
literal|"2"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[1,2,3]"
argument_list|,
literal|"$[3]"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[1,2,3]"
argument_list|,
literal|"$.[*]"
argument_list|,
literal|"[1,2,3]"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[1,2,3]"
argument_list|,
literal|"$[*]"
argument_list|,
literal|"[1,2,3]"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[1,2,3]"
argument_list|,
literal|"$"
argument_list|,
literal|"[1,2,3]"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[{\"k1\":\"v1\"},{\"k2\":\"v2\"},{\"k3\":\"v3\"}]"
argument_list|,
literal|"$[2]"
argument_list|,
literal|"{\"k3\":\"v3\"}"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[{\"k1\":\"v1\"},{\"k2\":\"v2\"},{\"k3\":\"v3\"}]"
argument_list|,
literal|"$[2].k3"
argument_list|,
literal|"v3"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[{\"k1\":[{\"k11\":[1,2,3]}]}]"
argument_list|,
literal|"$[0].k1[0].k11[1]"
argument_list|,
literal|"2"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[{\"k1\":[{\"k11\":[1,2,3]}]}]"
argument_list|,
literal|"$[0].k1[0].k11"
argument_list|,
literal|"[1,2,3]"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[{\"k1\":[{\"k11\":[1,2,3]}]}]"
argument_list|,
literal|"$[0].k1[0]"
argument_list|,
literal|"{\"k11\":[1,2,3]}"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[{\"k1\":[{\"k11\":[1,2,3]}]}]"
argument_list|,
literal|"$[0].k1"
argument_list|,
literal|"[{\"k11\":[1,2,3]}]"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[{\"k1\":[{\"k11\":[1,2,3]}]}]"
argument_list|,
literal|"$[0]"
argument_list|,
literal|"{\"k1\":[{\"k11\":[1,2,3]}]}"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[[1,2,3],[4,5,6],[7,8,9]]"
argument_list|,
literal|"$[1]"
argument_list|,
literal|"[4,5,6]"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[[1,2,3],[4,5,6],[7,8,9]]"
argument_list|,
literal|"$[1][0]"
argument_list|,
literal|"4"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[\"a\",\"b\"]"
argument_list|,
literal|"$[1]"
argument_list|,
literal|"b"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[[\"a\",\"b\"]]"
argument_list|,
literal|"$[0][1]"
argument_list|,
literal|"b"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[1,2,3]"
argument_list|,
literal|"[0]"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[1,2,3]"
argument_list|,
literal|"$0"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[1,2,3]"
argument_list|,
literal|"0"
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[1,2,3]"
argument_list|,
literal|"$."
argument_list|,
literal|null
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"[1,2,3]"
argument_list|,
literal|"$"
argument_list|,
literal|"[1,2,3]"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
name|runTest
argument_list|(
literal|"{\"a\":4}"
argument_list|,
literal|"$"
argument_list|,
literal|"{\"a\":4}"
argument_list|,
name|udf
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|runTest
parameter_list|(
name|String
name|json
parameter_list|,
name|String
name|path
parameter_list|,
name|String
name|exp
parameter_list|,
name|UDFJson
name|udf
parameter_list|)
block|{
name|Text
name|res
init|=
name|udf
operator|.
name|evaluate
argument_list|(
name|json
argument_list|,
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|exp
operator|==
literal|null
condition|)
block|{
name|Assert
operator|.
name|assertNull
argument_list|(
name|res
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|res
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"get_json_object test"
argument_list|,
name|exp
argument_list|,
name|res
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

