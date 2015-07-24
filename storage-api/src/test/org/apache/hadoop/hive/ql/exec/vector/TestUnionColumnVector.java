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
name|vector
package|;
end_package

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

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
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

begin_comment
comment|/**  * Test for StructColumnVector  */
end_comment

begin_class
specifier|public
class|class
name|TestUnionColumnVector
block|{
annotation|@
name|Test
specifier|public
name|void
name|testFlatten
parameter_list|()
throws|throws
name|Exception
block|{
name|LongColumnVector
name|col1
init|=
operator|new
name|LongColumnVector
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|LongColumnVector
name|col2
init|=
operator|new
name|LongColumnVector
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|UnionColumnVector
name|vector
init|=
operator|new
name|UnionColumnVector
argument_list|(
literal|10
argument_list|,
name|col1
argument_list|,
name|col2
argument_list|)
decl_stmt|;
name|vector
operator|.
name|init
argument_list|()
expr_stmt|;
name|col1
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
operator|++
name|i
control|)
block|{
name|vector
operator|.
name|tags
index|[
name|i
index|]
operator|=
name|i
operator|%
literal|2
expr_stmt|;
name|col1
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
name|col2
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|2
operator|*
name|i
expr_stmt|;
block|}
name|vector
operator|.
name|flatten
argument_list|(
literal|false
argument_list|,
literal|null
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|col1
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
name|i
operator|%
literal|2
argument_list|,
name|vector
operator|.
name|tags
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"col1 at "
operator|+
name|i
argument_list|,
literal|0
argument_list|,
name|col1
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"col2 at "
operator|+
name|i
argument_list|,
literal|2
operator|*
name|i
argument_list|,
name|col2
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|vector
operator|.
name|unFlatten
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|col1
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
operator|++
name|i
control|)
block|{
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|vector
operator|.
name|stringifyValue
argument_list|(
name|buf
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"{\"tag\": "
operator|+
operator|(
name|i
operator|%
literal|2
operator|)
operator|+
literal|", \"value\": "
operator|+
operator|(
name|i
operator|%
literal|2
operator|==
literal|0
condition|?
literal|0
else|:
literal|2
operator|*
name|i
operator|)
operator|+
literal|"}"
argument_list|,
name|buf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|vector
operator|.
name|reset
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|col1
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSet
parameter_list|()
throws|throws
name|Exception
block|{
name|LongColumnVector
name|input1
init|=
operator|new
name|LongColumnVector
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|LongColumnVector
name|input2
init|=
operator|new
name|LongColumnVector
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|UnionColumnVector
name|input
init|=
operator|new
name|UnionColumnVector
argument_list|(
literal|10
argument_list|,
name|input1
argument_list|,
name|input2
argument_list|)
decl_stmt|;
name|input
operator|.
name|init
argument_list|()
expr_stmt|;
name|LongColumnVector
name|output1
init|=
operator|new
name|LongColumnVector
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|LongColumnVector
name|output2
init|=
operator|new
name|LongColumnVector
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|UnionColumnVector
name|output
init|=
operator|new
name|UnionColumnVector
argument_list|(
literal|10
argument_list|,
name|output1
argument_list|,
name|output2
argument_list|)
decl_stmt|;
name|output
operator|.
name|init
argument_list|()
expr_stmt|;
name|input1
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
operator|++
name|i
control|)
block|{
name|input
operator|.
name|tags
index|[
name|i
index|]
operator|=
name|i
operator|%
literal|2
expr_stmt|;
name|input1
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|i
operator|+
literal|1
expr_stmt|;
name|input2
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|i
operator|+
literal|2
expr_stmt|;
block|}
name|output
operator|.
name|setElement
argument_list|(
literal|3
argument_list|,
literal|4
argument_list|,
name|input
argument_list|)
expr_stmt|;
name|StringBuilder
name|buf
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|output
operator|.
name|stringifyValue
argument_list|(
name|buf
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"{\"tag\": 0, \"value\": 1}"
argument_list|,
name|buf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|input
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|input
operator|.
name|isNull
index|[
literal|5
index|]
operator|=
literal|true
expr_stmt|;
name|output
operator|.
name|setElement
argument_list|(
literal|3
argument_list|,
literal|5
argument_list|,
name|input
argument_list|)
expr_stmt|;
name|buf
operator|=
operator|new
name|StringBuilder
argument_list|()
expr_stmt|;
name|output
operator|.
name|stringifyValue
argument_list|(
name|buf
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"null"
argument_list|,
name|buf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|input
operator|.
name|reset
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|input1
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|input
operator|.
name|noNulls
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

