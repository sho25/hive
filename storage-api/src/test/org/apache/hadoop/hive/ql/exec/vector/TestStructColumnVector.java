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
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_import
import|import
name|java
operator|.
name|sql
operator|.
name|Timestamp
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
name|TestStructColumnVector
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
name|StructColumnVector
name|vector
init|=
operator|new
name|StructColumnVector
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
literal|"[0, "
operator|+
operator|(
literal|2
operator|*
name|i
operator|)
operator|+
literal|"]"
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
name|StructColumnVector
name|input
init|=
operator|new
name|StructColumnVector
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
name|StructColumnVector
name|output
init|=
operator|new
name|StructColumnVector
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
name|input2
operator|.
name|noNulls
operator|=
literal|false
expr_stmt|;
name|input2
operator|.
name|isNull
index|[
literal|5
index|]
operator|=
literal|true
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
literal|6
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
literal|"null"
argument_list|,
name|buf
operator|.
name|toString
argument_list|()
argument_list|)
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
literal|"[1, null]"
argument_list|,
name|buf
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
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
literal|"[1, 6]"
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
annotation|@
name|Test
specifier|public
name|void
name|testStringify
parameter_list|()
throws|throws
name|IOException
block|{
name|VectorizedRowBatch
name|batch
init|=
operator|new
name|VectorizedRowBatch
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|LongColumnVector
name|x1
init|=
operator|new
name|LongColumnVector
argument_list|()
decl_stmt|;
name|TimestampColumnVector
name|x2
init|=
operator|new
name|TimestampColumnVector
argument_list|()
decl_stmt|;
name|StructColumnVector
name|x
init|=
operator|new
name|StructColumnVector
argument_list|(
literal|1024
argument_list|,
name|x1
argument_list|,
name|x2
argument_list|)
decl_stmt|;
name|BytesColumnVector
name|y
init|=
operator|new
name|BytesColumnVector
argument_list|()
decl_stmt|;
name|batch
operator|.
name|cols
index|[
literal|0
index|]
operator|=
name|x
expr_stmt|;
name|batch
operator|.
name|cols
index|[
literal|1
index|]
operator|=
name|y
expr_stmt|;
name|batch
operator|.
name|reset
argument_list|()
expr_stmt|;
name|Timestamp
name|ts
init|=
name|Timestamp
operator|.
name|valueOf
argument_list|(
literal|"2000-01-01 00:00:00"
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
literal|10
condition|;
operator|++
name|r
control|)
block|{
name|batch
operator|.
name|size
operator|+=
literal|1
expr_stmt|;
name|x1
operator|.
name|vector
index|[
name|r
index|]
operator|=
literal|3
operator|*
name|r
expr_stmt|;
name|ts
operator|.
name|setTime
argument_list|(
name|ts
operator|.
name|getTime
argument_list|()
operator|+
literal|1000
argument_list|)
expr_stmt|;
name|x2
operator|.
name|set
argument_list|(
name|r
argument_list|,
name|ts
argument_list|)
expr_stmt|;
name|byte
index|[]
name|buffer
init|=
operator|(
literal|"value "
operator|+
name|r
operator|)
operator|.
name|getBytes
argument_list|(
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
decl_stmt|;
name|y
operator|.
name|setRef
argument_list|(
name|r
argument_list|,
name|buffer
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
specifier|final
name|String
name|EXPECTED
init|=
operator|(
literal|"Column vector types: 0:STRUCT, 1:BYTES\n"
operator|+
literal|"[[0, 2000-01-01 00:00:01.0], \"value 0\"]\n"
operator|+
literal|"[[3, 2000-01-01 00:00:02.0], \"value 1\"]\n"
operator|+
literal|"[[6, 2000-01-01 00:00:03.0], \"value 2\"]\n"
operator|+
literal|"[[9, 2000-01-01 00:00:04.0], \"value 3\"]\n"
operator|+
literal|"[[12, 2000-01-01 00:00:05.0], \"value 4\"]\n"
operator|+
literal|"[[15, 2000-01-01 00:00:06.0], \"value 5\"]\n"
operator|+
literal|"[[18, 2000-01-01 00:00:07.0], \"value 6\"]\n"
operator|+
literal|"[[21, 2000-01-01 00:00:08.0], \"value 7\"]\n"
operator|+
literal|"[[24, 2000-01-01 00:00:09.0], \"value 8\"]\n"
operator|+
literal|"[[27, 2000-01-01 00:00:10.0], \"value 9\"]"
operator|)
decl_stmt|;
name|assertEquals
argument_list|(
name|EXPECTED
argument_list|,
name|batch
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

