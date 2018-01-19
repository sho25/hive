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
operator|.
name|expressions
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
name|assertEquals
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
name|exec
operator|.
name|vector
operator|.
name|LongColumnVector
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
name|exec
operator|.
name|vector
operator|.
name|VectorizedRowBatch
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
name|exec
operator|.
name|vector
operator|.
name|expressions
operator|.
name|gen
operator|.
name|LongColUnaryMinus
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
name|exec
operator|.
name|vector
operator|.
name|util
operator|.
name|VectorizedRowGroupGenUtil
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

begin_comment
comment|/**  * Unit tests for unary minus.  */
end_comment

begin_class
specifier|public
class|class
name|TestUnaryMinus
block|{
annotation|@
name|Test
specifier|public
name|void
name|testUnaryMinus
parameter_list|()
block|{
name|VectorizedRowBatch
name|vrg
init|=
name|VectorizedRowGroupGenUtil
operator|.
name|getVectorizedRowBatch
argument_list|(
literal|1024
argument_list|,
literal|2
argument_list|,
literal|23
argument_list|)
decl_stmt|;
name|LongColUnaryMinus
name|expr
init|=
operator|new
name|LongColUnaryMinus
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|expr
operator|.
name|evaluate
argument_list|(
name|vrg
argument_list|)
expr_stmt|;
comment|//verify
name|long
index|[]
name|inVector
init|=
operator|(
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
literal|0
index|]
operator|)
operator|.
name|vector
decl_stmt|;
name|long
index|[]
name|outVector
init|=
operator|(
operator|(
name|LongColumnVector
operator|)
name|vrg
operator|.
name|cols
index|[
literal|1
index|]
operator|)
operator|.
name|vector
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
name|outVector
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|inVector
index|[
name|i
index|]
operator|+
name|outVector
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

