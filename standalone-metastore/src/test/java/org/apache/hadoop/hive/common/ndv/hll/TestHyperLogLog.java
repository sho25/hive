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
name|ndv
operator|.
name|hll
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
name|common
operator|.
name|ndv
operator|.
name|hll
operator|.
name|HyperLogLog
operator|.
name|EncodingType
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
name|metastore
operator|.
name|annotation
operator|.
name|MetastoreUnitTest
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
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|MetastoreUnitTest
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestHyperLogLog
block|{
comment|// 5% tolerance for estimated count
specifier|private
name|float
name|longRangeTolerance
init|=
literal|5.0f
decl_stmt|;
specifier|private
name|float
name|shortRangeTolerance
init|=
literal|2.0f
decl_stmt|;
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testHLLDenseMerge
parameter_list|()
block|{
name|HyperLogLog
name|hll
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll2
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll3
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll4
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setNumRegisterIndexBits
argument_list|(
literal|16
argument_list|)
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll5
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setNumRegisterIndexBits
argument_list|(
literal|12
argument_list|)
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|int
name|size
init|=
literal|1000
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|hll
operator|.
name|addLong
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|hll2
operator|.
name|addLong
argument_list|(
name|size
operator|+
name|i
argument_list|)
expr_stmt|;
name|hll3
operator|.
name|addLong
argument_list|(
literal|2
operator|*
name|size
operator|+
name|i
argument_list|)
expr_stmt|;
name|hll4
operator|.
name|addLong
argument_list|(
literal|3
operator|*
name|size
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|double
name|threshold
init|=
name|size
operator|>
literal|40000
condition|?
name|longRangeTolerance
else|:
name|shortRangeTolerance
decl_stmt|;
name|double
name|delta
init|=
name|threshold
operator|*
name|size
operator|/
literal|100
decl_stmt|;
name|double
name|delta4
init|=
name|threshold
operator|*
operator|(
literal|4
operator|*
name|size
operator|)
operator|/
literal|100
decl_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll2
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
comment|// merge
name|hll
operator|.
name|merge
argument_list|(
name|hll2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|2
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// merge should update registers and hence the count
name|hll
operator|.
name|merge
argument_list|(
name|hll2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|2
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// new merge
name|hll
operator|.
name|merge
argument_list|(
name|hll3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|3
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// valid merge -- register set size gets bigger (also 4k items
name|hll
operator|.
name|merge
argument_list|(
name|hll4
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|4
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta4
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// invalid merge -- smaller register merge to bigger
name|hll
operator|.
name|merge
argument_list|(
name|hll5
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testHLLSparseMerge
parameter_list|()
block|{
name|HyperLogLog
name|hll
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll2
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll3
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll4
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setNumRegisterIndexBits
argument_list|(
literal|16
argument_list|)
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll5
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setNumRegisterIndexBits
argument_list|(
literal|12
argument_list|)
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|int
name|size
init|=
literal|500
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|hll
operator|.
name|addLong
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|hll2
operator|.
name|addLong
argument_list|(
name|size
operator|+
name|i
argument_list|)
expr_stmt|;
name|hll3
operator|.
name|addLong
argument_list|(
literal|2
operator|*
name|size
operator|+
name|i
argument_list|)
expr_stmt|;
name|hll4
operator|.
name|addLong
argument_list|(
literal|3
operator|*
name|size
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|double
name|threshold
init|=
name|size
operator|>
literal|40000
condition|?
name|longRangeTolerance
else|:
name|shortRangeTolerance
decl_stmt|;
name|double
name|delta
init|=
name|threshold
operator|*
name|size
operator|/
literal|100
decl_stmt|;
name|double
name|delta4
init|=
name|threshold
operator|*
operator|(
literal|4
operator|*
name|size
operator|)
operator|/
literal|100
decl_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll2
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
comment|// merge
name|hll
operator|.
name|merge
argument_list|(
name|hll2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|2
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// merge should update registers and hence the count
name|hll
operator|.
name|merge
argument_list|(
name|hll2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|2
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// new merge
name|hll
operator|.
name|merge
argument_list|(
name|hll3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|3
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// valid merge -- register set size gets bigger& dense automatically
name|hll
operator|.
name|merge
argument_list|(
name|hll4
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|4
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta4
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// invalid merge -- smaller register merge to bigger
name|hll
operator|.
name|merge
argument_list|(
name|hll5
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testHLLSparseDenseMerge
parameter_list|()
block|{
name|HyperLogLog
name|hll
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll2
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll3
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll4
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setNumRegisterIndexBits
argument_list|(
literal|16
argument_list|)
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll5
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setNumRegisterIndexBits
argument_list|(
literal|12
argument_list|)
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|int
name|size
init|=
literal|1000
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|hll
operator|.
name|addLong
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|hll2
operator|.
name|addLong
argument_list|(
name|size
operator|+
name|i
argument_list|)
expr_stmt|;
name|hll3
operator|.
name|addLong
argument_list|(
literal|2
operator|*
name|size
operator|+
name|i
argument_list|)
expr_stmt|;
name|hll4
operator|.
name|addLong
argument_list|(
literal|3
operator|*
name|size
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|double
name|threshold
init|=
name|size
operator|>
literal|40000
condition|?
name|longRangeTolerance
else|:
name|shortRangeTolerance
decl_stmt|;
name|double
name|delta
init|=
name|threshold
operator|*
name|size
operator|/
literal|100
decl_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll2
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
comment|// sparse-sparse merge
name|hll
operator|.
name|merge
argument_list|(
name|hll2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|2
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// merge should update registers and hence the count
name|hll
operator|.
name|merge
argument_list|(
name|hll2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|2
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// sparse-dense merge
name|hll
operator|.
name|merge
argument_list|(
name|hll3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|3
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// merge should convert hll2 to DENSE
name|hll2
operator|.
name|merge
argument_list|(
name|hll4
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|2
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll2
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|,
name|hll2
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// invalid merge -- smaller register merge to bigger
name|hll
operator|.
name|merge
argument_list|(
name|hll5
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testHLLDenseSparseMerge
parameter_list|()
block|{
name|HyperLogLog
name|hll
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll2
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll3
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll4
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setNumRegisterIndexBits
argument_list|(
literal|16
argument_list|)
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll5
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setNumRegisterIndexBits
argument_list|(
literal|12
argument_list|)
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|int
name|size
init|=
literal|1000
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|hll
operator|.
name|addLong
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|hll2
operator|.
name|addLong
argument_list|(
name|size
operator|+
name|i
argument_list|)
expr_stmt|;
name|hll3
operator|.
name|addLong
argument_list|(
literal|2
operator|*
name|size
operator|+
name|i
argument_list|)
expr_stmt|;
name|hll4
operator|.
name|addLong
argument_list|(
literal|3
operator|*
name|size
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|double
name|threshold
init|=
name|size
operator|>
literal|40000
condition|?
name|longRangeTolerance
else|:
name|shortRangeTolerance
decl_stmt|;
name|double
name|delta
init|=
name|threshold
operator|*
name|size
operator|/
literal|100
decl_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll2
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
comment|// sparse-sparse merge
name|hll
operator|.
name|merge
argument_list|(
name|hll2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|2
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// merge should update registers and hence the count
name|hll
operator|.
name|merge
argument_list|(
name|hll2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|2
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// sparse-dense merge
name|hll
operator|.
name|merge
argument_list|(
name|hll3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|3
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// merge should convert hll3 to DENSE
name|hll3
operator|.
name|merge
argument_list|(
name|hll4
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|2
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll3
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|,
name|hll3
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// invalid merge -- smaller register merge to bigger
name|hll
operator|.
name|merge
argument_list|(
name|hll5
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalArgumentException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testHLLSparseOverflowMerge
parameter_list|()
block|{
name|HyperLogLog
name|hll
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll2
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll3
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll4
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setNumRegisterIndexBits
argument_list|(
literal|16
argument_list|)
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|hll5
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setNumRegisterIndexBits
argument_list|(
literal|12
argument_list|)
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|int
name|size
init|=
literal|1000
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|hll
operator|.
name|addLong
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|hll2
operator|.
name|addLong
argument_list|(
name|size
operator|+
name|i
argument_list|)
expr_stmt|;
name|hll3
operator|.
name|addLong
argument_list|(
literal|2
operator|*
name|size
operator|+
name|i
argument_list|)
expr_stmt|;
name|hll4
operator|.
name|addLong
argument_list|(
literal|3
operator|*
name|size
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|double
name|threshold
init|=
name|size
operator|>
literal|40000
condition|?
name|longRangeTolerance
else|:
name|shortRangeTolerance
decl_stmt|;
name|double
name|delta
init|=
name|threshold
operator|*
name|size
operator|/
literal|100
decl_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll2
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
comment|// sparse-sparse merge
name|hll
operator|.
name|merge
argument_list|(
name|hll2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|2
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// merge should update registers and hence the count
name|hll
operator|.
name|merge
argument_list|(
name|hll2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|2
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// sparse-sparse overload to dense
name|hll
operator|.
name|merge
argument_list|(
name|hll3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|3
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|,
name|hll
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// merge should convert hll2 to DENSE
name|hll2
operator|.
name|merge
argument_list|(
name|hll4
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
literal|2
operator|*
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll2
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|,
name|hll2
operator|.
name|getEncoding
argument_list|()
argument_list|)
expr_stmt|;
comment|// invalid merge -- smaller register merge to bigger
name|hll
operator|.
name|merge
argument_list|(
name|hll5
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHLLSparseMoreRegisterBits
parameter_list|()
block|{
name|HyperLogLog
name|hll
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|setNumRegisterIndexBits
argument_list|(
literal|16
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|int
name|size
init|=
literal|1000
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|hll
operator|.
name|addLong
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|double
name|threshold
init|=
name|size
operator|>
literal|40000
condition|?
name|longRangeTolerance
else|:
name|shortRangeTolerance
decl_stmt|;
name|double
name|delta
init|=
name|threshold
operator|*
name|size
operator|/
literal|100
decl_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
name|size
argument_list|,
operator|(
name|double
operator|)
name|hll
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHLLSquash
parameter_list|()
block|{
name|int
index|[]
name|sizes
init|=
operator|new
name|int
index|[]
block|{
literal|500
block|,
literal|1000
block|,
literal|2300
block|,
literal|4096
block|}
decl_stmt|;
name|int
name|minBits
init|=
literal|9
decl_stmt|;
for|for
control|(
specifier|final
name|int
name|size
range|:
name|sizes
control|)
block|{
name|HyperLogLog
name|hlls
index|[]
init|=
operator|new
name|HyperLogLog
index|[
literal|16
index|]
decl_stmt|;
for|for
control|(
name|int
name|k
init|=
name|minBits
init|;
name|k
operator|<
name|hlls
operator|.
name|length
condition|;
name|k
operator|++
control|)
block|{
specifier|final
name|HyperLogLog
name|hll
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|)
operator|.
name|setNumRegisterIndexBits
argument_list|(
name|k
argument_list|)
operator|.
name|build
argument_list|()
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|hll
operator|.
name|addLong
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|hlls
index|[
name|k
index|]
operator|=
name|hll
expr_stmt|;
block|}
for|for
control|(
name|int
name|k
init|=
name|minBits
init|;
name|k
operator|<
name|hlls
operator|.
name|length
condition|;
name|k
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
name|k
operator|+
literal|1
init|;
name|j
operator|<
name|hlls
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
specifier|final
name|HyperLogLog
name|large
init|=
name|hlls
index|[
name|j
index|]
decl_stmt|;
specifier|final
name|HyperLogLog
name|small
init|=
name|hlls
index|[
name|k
index|]
decl_stmt|;
specifier|final
name|HyperLogLog
name|mush
init|=
name|large
operator|.
name|squash
argument_list|(
name|small
operator|.
name|getNumRegisterIndexBits
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|small
operator|.
name|count
argument_list|()
argument_list|,
name|mush
operator|.
name|count
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|double
name|delta
init|=
name|Math
operator|.
name|ceil
argument_list|(
name|small
operator|.
name|getStandardError
argument_list|()
operator|*
name|size
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
name|size
argument_list|,
operator|(
name|double
operator|)
name|mush
operator|.
name|count
argument_list|()
argument_list|,
name|delta
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHLLDenseDenseSquash
parameter_list|()
block|{
name|HyperLogLog
name|p14HLL
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|)
operator|.
name|setNumRegisterIndexBits
argument_list|(
literal|14
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|p10HLL
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|)
operator|.
name|setNumRegisterIndexBits
argument_list|(
literal|10
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|int
name|size
init|=
literal|1_000_000
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|p14HLL
operator|.
name|addLong
argument_list|(
name|i
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
literal|10_000
condition|;
name|i
operator|++
control|)
block|{
name|p10HLL
operator|.
name|addLong
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|p14HLL
operator|.
name|squash
argument_list|(
name|p10HLL
operator|.
name|getNumRegisterIndexBits
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
name|size
argument_list|,
name|p14HLL
operator|.
name|count
argument_list|()
argument_list|,
name|longRangeTolerance
operator|*
name|size
operator|/
literal|100.0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHLLSparseDenseSquash
parameter_list|()
block|{
name|HyperLogLog
name|p14HLL
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|SPARSE
argument_list|)
operator|.
name|setNumRegisterIndexBits
argument_list|(
literal|14
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HyperLogLog
name|p10HLL
init|=
name|HyperLogLog
operator|.
name|builder
argument_list|()
operator|.
name|setEncoding
argument_list|(
name|EncodingType
operator|.
name|DENSE
argument_list|)
operator|.
name|setNumRegisterIndexBits
argument_list|(
literal|10
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|int
name|size
init|=
literal|2000
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|p14HLL
operator|.
name|addLong
argument_list|(
name|i
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
literal|10_000
condition|;
name|i
operator|++
control|)
block|{
name|p10HLL
operator|.
name|addLong
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|p14HLL
operator|.
name|squash
argument_list|(
name|p10HLL
operator|.
name|getNumRegisterIndexBits
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|double
operator|)
name|size
argument_list|,
name|p14HLL
operator|.
name|count
argument_list|()
argument_list|,
name|longRangeTolerance
operator|*
name|size
operator|/
literal|100.0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

