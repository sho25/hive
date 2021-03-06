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
name|io
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
name|io
operator|.
name|orc
operator|.
name|OrcRawRecordMerger
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
name|concurrent
operator|.
name|ThreadLocalRandom
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
name|assertNotEquals
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

begin_class
specifier|public
class|class
name|TestRecordIdentifier
block|{
annotation|@
name|Test
specifier|public
name|void
name|TestOrdering
parameter_list|()
throws|throws
name|Exception
block|{
name|RecordIdentifier
name|left
init|=
operator|new
name|RecordIdentifier
argument_list|(
literal|100
argument_list|,
literal|200
argument_list|,
literal|1200
argument_list|)
decl_stmt|;
name|RecordIdentifier
name|right
init|=
operator|new
name|RecordIdentifier
argument_list|()
decl_stmt|;
name|right
operator|.
name|setValues
argument_list|(
literal|100L
argument_list|,
literal|200
argument_list|,
literal|1000L
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|right
operator|.
name|compareTo
argument_list|(
name|left
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|left
operator|.
name|compareTo
argument_list|(
name|right
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|left
operator|.
name|set
argument_list|(
name|right
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|right
operator|.
name|compareTo
argument_list|(
name|left
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|right
operator|.
name|setRowId
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|right
operator|.
name|compareTo
argument_list|(
name|left
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|left
operator|.
name|setValues
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|right
operator|.
name|setValues
argument_list|(
literal|100
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|left
operator|.
name|compareTo
argument_list|(
name|right
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|right
operator|.
name|compareTo
argument_list|(
name|left
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|left
operator|.
name|setValues
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|right
operator|.
name|setValues
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|left
operator|.
name|compareTo
argument_list|(
name|right
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|right
operator|.
name|compareTo
argument_list|(
name|left
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHashEquals
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|origTxn
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|(
literal|1
argument_list|,
literal|10000000000L
argument_list|)
decl_stmt|;
name|int
name|bucketId
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextInt
argument_list|(
literal|1
argument_list|,
literal|512
argument_list|)
decl_stmt|;
name|long
name|rowId
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|(
literal|1
argument_list|,
literal|10000000000L
argument_list|)
decl_stmt|;
name|long
name|currTxn
init|=
name|origTxn
operator|+
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|(
literal|0
argument_list|,
literal|10000000000L
argument_list|)
decl_stmt|;
name|RecordIdentifier
name|left
init|=
operator|new
name|RecordIdentifier
argument_list|(
name|origTxn
argument_list|,
name|bucketId
argument_list|,
name|rowId
argument_list|)
decl_stmt|;
name|RecordIdentifier
name|right
init|=
operator|new
name|RecordIdentifier
argument_list|(
name|origTxn
argument_list|,
name|bucketId
argument_list|,
name|rowId
argument_list|)
decl_stmt|;
name|OrcRawRecordMerger
operator|.
name|ReaderKey
name|rkLeft
init|=
operator|new
name|OrcRawRecordMerger
operator|.
name|ReaderKey
argument_list|(
name|origTxn
argument_list|,
name|bucketId
argument_list|,
name|rowId
argument_list|,
name|currTxn
argument_list|)
decl_stmt|;
name|OrcRawRecordMerger
operator|.
name|ReaderKey
name|rkRight
init|=
operator|new
name|OrcRawRecordMerger
operator|.
name|ReaderKey
argument_list|(
name|origTxn
argument_list|,
name|bucketId
argument_list|,
name|rowId
argument_list|,
name|currTxn
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"RecordIdentifier.equals"
argument_list|,
name|left
argument_list|,
name|right
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"RecordIdentifier.hashCode"
argument_list|,
name|left
operator|.
name|hashCode
argument_list|()
argument_list|,
name|right
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ReaderKey"
argument_list|,
name|rkLeft
argument_list|,
name|rkLeft
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ReaderKey.hashCode"
argument_list|,
name|rkLeft
operator|.
name|hashCode
argument_list|()
argument_list|,
name|rkRight
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
comment|//debatable if this is correct, but that's how it's implemented
name|assertNotEquals
argument_list|(
literal|"RecordIdentifier<> ReaderKey"
argument_list|,
name|left
argument_list|,
name|rkRight
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

