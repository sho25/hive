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
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
comment|/**  * Test for MapColumnVector  */
end_comment

begin_class
specifier|public
class|class
name|TestMapColumnVector
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
name|DoubleColumnVector
name|col2
init|=
operator|new
name|DoubleColumnVector
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|MapColumnVector
name|vector
init|=
operator|new
name|MapColumnVector
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
comment|// TEST - repeating NULL& no selection
name|col1
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|vector
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|vector
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|vector
operator|.
name|isNull
index|[
literal|0
index|]
operator|=
literal|true
expr_stmt|;
name|vector
operator|.
name|childCount
operator|=
literal|0
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
name|col1
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|i
operator|+
literal|3
expr_stmt|;
name|col2
operator|.
name|vector
index|[
name|i
index|]
operator|=
name|i
operator|*
literal|10
expr_stmt|;
name|vector
operator|.
name|offsets
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
name|vector
operator|.
name|lengths
index|[
name|i
index|]
operator|=
literal|10
operator|+
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
comment|// make sure the vector was flattened
name|assertFalse
argument_list|(
name|vector
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|vector
operator|.
name|noNulls
argument_list|)
expr_stmt|;
comment|// child isn't flattened, because parent is repeating null
name|assertTrue
argument_list|(
name|col1
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|col1
operator|.
name|noNulls
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
name|assertTrue
argument_list|(
literal|"isNull at "
operator|+
name|i
argument_list|,
name|vector
operator|.
name|isNull
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
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
literal|"null"
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
name|assertTrue
argument_list|(
name|vector
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
comment|// TEST - repeating NULL& selection
name|Arrays
operator|.
name|fill
argument_list|(
name|vector
operator|.
name|isNull
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|int
index|[]
name|sel
init|=
operator|new
name|int
index|[]
block|{
literal|3
block|,
literal|5
block|,
literal|7
block|}
decl_stmt|;
name|vector
operator|.
name|flatten
argument_list|(
literal|true
argument_list|,
name|sel
argument_list|,
literal|3
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
literal|"failure at "
operator|+
name|i
argument_list|,
name|i
operator|==
literal|3
operator|||
name|i
operator|==
literal|5
operator|||
name|i
operator|==
literal|7
argument_list|,
name|vector
operator|.
name|isNull
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
comment|// TEST - repeating non-NULL& no-selection
name|vector
operator|.
name|noNulls
operator|=
literal|true
expr_stmt|;
name|vector
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|vector
operator|.
name|offsets
index|[
literal|0
index|]
operator|=
literal|0
expr_stmt|;
name|vector
operator|.
name|lengths
index|[
literal|0
index|]
operator|=
literal|3
expr_stmt|;
name|vector
operator|.
name|childCount
operator|=
literal|3
expr_stmt|;
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
comment|// make sure the vector was flattened
name|assertFalse
argument_list|(
name|vector
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|vector
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|col1
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|col1
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|col2
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|col2
operator|.
name|noNulls
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
literal|"offset at "
operator|+
name|i
argument_list|,
literal|0
argument_list|,
name|vector
operator|.
name|offsets
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"length at "
operator|+
name|i
argument_list|,
literal|3
argument_list|,
name|vector
operator|.
name|lengths
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
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
literal|"[{\"key\": 3, \"value\": 0.0},"
operator|+
literal|" {\"key\": 3, \"value\": 10.0},"
operator|+
literal|" {\"key\": 3, \"value\": 20.0}]"
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
name|assertTrue
argument_list|(
name|col1
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|vector
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|col2
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|col2
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|vector
operator|.
name|noNulls
argument_list|)
expr_stmt|;
comment|// TEST - repeating non-NULL& selection
name|Arrays
operator|.
name|fill
argument_list|(
name|vector
operator|.
name|offsets
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|vector
operator|.
name|lengths
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|col1
operator|.
name|vector
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|vector
operator|.
name|flatten
argument_list|(
literal|true
argument_list|,
name|sel
argument_list|,
literal|3
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|==
literal|3
operator|||
name|i
operator|==
literal|5
operator|||
name|i
operator|==
literal|7
condition|)
block|{
name|assertEquals
argument_list|(
literal|"failure at "
operator|+
name|i
argument_list|,
literal|0
argument_list|,
name|vector
operator|.
name|offsets
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"failure at "
operator|+
name|i
argument_list|,
literal|3
argument_list|,
name|vector
operator|.
name|lengths
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|"failure at "
operator|+
name|i
argument_list|,
operator|-
literal|1
argument_list|,
name|vector
operator|.
name|offsets
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"failure at "
operator|+
name|i
argument_list|,
operator|-
literal|1
argument_list|,
name|vector
operator|.
name|lengths
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|3
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
literal|"failure at "
operator|+
name|i
argument_list|,
literal|3
argument_list|,
name|col1
operator|.
name|vector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|3
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
literal|"failure at "
operator|+
name|i
argument_list|,
operator|-
literal|1
argument_list|,
name|col1
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
comment|// TEST - reset
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
name|assertTrue
argument_list|(
name|col1
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|col2
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|col2
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|vector
operator|.
name|isRepeating
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|vector
operator|.
name|noNulls
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|vector
operator|.
name|childCount
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
name|DoubleColumnVector
name|input2
init|=
operator|new
name|DoubleColumnVector
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|MapColumnVector
name|input
init|=
operator|new
name|MapColumnVector
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
literal|30
argument_list|)
decl_stmt|;
name|DoubleColumnVector
name|output2
init|=
operator|new
name|DoubleColumnVector
argument_list|(
literal|30
argument_list|)
decl_stmt|;
name|MapColumnVector
name|output
init|=
operator|new
name|MapColumnVector
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
literal|6
index|]
operator|=
literal|true
expr_stmt|;
name|input
operator|.
name|childCount
operator|=
literal|11
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|output1
operator|.
name|vector
argument_list|,
operator|-
literal|1
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
name|input1
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|10
operator|*
name|i
expr_stmt|;
name|input2
operator|.
name|vector
index|[
name|i
index|]
operator|=
literal|100
operator|*
name|i
expr_stmt|;
name|input
operator|.
name|offsets
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
name|input
operator|.
name|lengths
index|[
name|i
index|]
operator|=
literal|2
expr_stmt|;
name|output
operator|.
name|offsets
index|[
name|i
index|]
operator|=
name|i
operator|+
literal|2
expr_stmt|;
name|output
operator|.
name|lengths
index|[
name|i
index|]
operator|=
literal|3
expr_stmt|;
block|}
name|output
operator|.
name|childCount
operator|=
literal|30
expr_stmt|;
comment|// copy a null
name|output
operator|.
name|setElement
argument_list|(
literal|3
argument_list|,
literal|6
argument_list|,
name|input
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|30
argument_list|,
name|output
operator|.
name|childCount
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
literal|"null"
argument_list|,
name|buf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// copy a value
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
name|assertEquals
argument_list|(
literal|30
argument_list|,
name|output
operator|.
name|offsets
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|output
operator|.
name|lengths
index|[
literal|3
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|32
argument_list|,
name|output
operator|.
name|childCount
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
literal|"[{\"key\": 50, \"value\": 500.0},"
operator|+
literal|" {\"key\": 60, \"value\": 600.0}]"
argument_list|,
name|buf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// overwrite a value
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
name|assertEquals
argument_list|(
literal|34
argument_list|,
name|output
operator|.
name|childCount
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|34
argument_list|,
name|output1
operator|.
name|vector
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|50
argument_list|,
name|output1
operator|.
name|vector
index|[
literal|30
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|60
argument_list|,
name|output1
operator|.
name|vector
index|[
literal|31
index|]
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
literal|"[{\"key\": 40, \"value\": 400.0},"
operator|+
literal|" {\"key\": 50, \"value\": 500.0}]"
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
name|output
operator|.
name|reset
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|output
operator|.
name|childCount
argument_list|)
expr_stmt|;
name|input
operator|.
name|isRepeating
operator|=
literal|true
expr_stmt|;
name|input
operator|.
name|offsets
index|[
literal|0
index|]
operator|=
literal|0
expr_stmt|;
name|input
operator|.
name|lengths
index|[
literal|0
index|]
operator|=
literal|10
expr_stmt|;
name|output
operator|.
name|setElement
argument_list|(
literal|2
argument_list|,
literal|7
argument_list|,
name|input
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|output
operator|.
name|childCount
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
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"[{\"key\": 0, \"value\": 0.0},"
operator|+
literal|" {\"key\": 10, \"value\": 100.0},"
operator|+
literal|" {\"key\": 20, \"value\": 200.0},"
operator|+
literal|" {\"key\": 30, \"value\": 300.0},"
operator|+
literal|" {\"key\": 40, \"value\": 400.0},"
operator|+
literal|" {\"key\": 50, \"value\": 500.0},"
operator|+
literal|" {\"key\": 60, \"value\": 600.0},"
operator|+
literal|" {\"key\": 70, \"value\": 700.0},"
operator|+
literal|" {\"key\": 80, \"value\": 800.0},"
operator|+
literal|" {\"key\": 90, \"value\": 900.0}]"
argument_list|,
name|buf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

