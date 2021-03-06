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
package|;
end_package

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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|BitSet
import|;
end_import

begin_comment
comment|/**  * Tests for {@link ValidCompactorWriteIdList}.  */
end_comment

begin_class
specifier|public
class|class
name|TestValidCompactorWriteIdList
block|{
specifier|private
specifier|final
name|String
name|tableName
init|=
literal|"t1"
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|minTxnHigh
parameter_list|()
block|{
name|BitSet
name|bitSet
init|=
operator|new
name|BitSet
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|bitSet
operator|.
name|set
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|ValidWriteIdList
name|writeIds
init|=
operator|new
name|ValidCompactorWriteIdList
argument_list|(
name|tableName
argument_list|,
operator|new
name|long
index|[]
block|{
literal|3
block|,
literal|4
block|}
argument_list|,
name|bitSet
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|ValidWriteIdList
operator|.
name|RangeResponse
name|rsp
init|=
name|writeIds
operator|.
name|isWriteIdRangeValid
argument_list|(
literal|7
argument_list|,
literal|9
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidWriteIdList
operator|.
name|RangeResponse
operator|.
name|NONE
argument_list|,
name|rsp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|maxTxnLow
parameter_list|()
block|{
name|BitSet
name|bitSet
init|=
operator|new
name|BitSet
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|bitSet
operator|.
name|set
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|ValidWriteIdList
name|writeIds
init|=
operator|new
name|ValidCompactorWriteIdList
argument_list|(
name|tableName
argument_list|,
operator|new
name|long
index|[]
block|{
literal|13
block|,
literal|14
block|}
argument_list|,
name|bitSet
argument_list|,
literal|12
argument_list|)
decl_stmt|;
name|ValidWriteIdList
operator|.
name|RangeResponse
name|rsp
init|=
name|writeIds
operator|.
name|isWriteIdRangeValid
argument_list|(
literal|7
argument_list|,
literal|9
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidWriteIdList
operator|.
name|RangeResponse
operator|.
name|ALL
argument_list|,
name|rsp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|minTxnHighNoExceptions
parameter_list|()
block|{
name|ValidWriteIdList
name|writeIds
init|=
operator|new
name|ValidCompactorWriteIdList
argument_list|(
name|tableName
argument_list|,
operator|new
name|long
index|[
literal|0
index|]
argument_list|,
operator|new
name|BitSet
argument_list|()
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|ValidWriteIdList
operator|.
name|RangeResponse
name|rsp
init|=
name|writeIds
operator|.
name|isWriteIdRangeValid
argument_list|(
literal|7
argument_list|,
literal|9
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidWriteIdList
operator|.
name|RangeResponse
operator|.
name|NONE
argument_list|,
name|rsp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|maxTxnLowNoExceptions
parameter_list|()
block|{
name|ValidWriteIdList
name|writeIds
init|=
operator|new
name|ValidCompactorWriteIdList
argument_list|(
name|tableName
argument_list|,
operator|new
name|long
index|[
literal|0
index|]
argument_list|,
operator|new
name|BitSet
argument_list|()
argument_list|,
literal|15
argument_list|)
decl_stmt|;
name|ValidWriteIdList
operator|.
name|RangeResponse
name|rsp
init|=
name|writeIds
operator|.
name|isWriteIdRangeValid
argument_list|(
literal|7
argument_list|,
literal|9
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidWriteIdList
operator|.
name|RangeResponse
operator|.
name|ALL
argument_list|,
name|rsp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|exceptionsAllBelow
parameter_list|()
block|{
name|BitSet
name|bitSet
init|=
operator|new
name|BitSet
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|bitSet
operator|.
name|set
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|ValidWriteIdList
name|writeIds
init|=
operator|new
name|ValidCompactorWriteIdList
argument_list|(
name|tableName
argument_list|,
operator|new
name|long
index|[]
block|{
literal|3
block|,
literal|6
block|}
argument_list|,
name|bitSet
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|ValidWriteIdList
operator|.
name|RangeResponse
name|rsp
init|=
name|writeIds
operator|.
name|isWriteIdRangeValid
argument_list|(
literal|7
argument_list|,
literal|9
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidWriteIdList
operator|.
name|RangeResponse
operator|.
name|NONE
argument_list|,
name|rsp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|exceptionsInMidst
parameter_list|()
block|{
name|BitSet
name|bitSet
init|=
operator|new
name|BitSet
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|bitSet
operator|.
name|set
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|ValidWriteIdList
name|writeIds
init|=
operator|new
name|ValidCompactorWriteIdList
argument_list|(
name|tableName
argument_list|,
operator|new
name|long
index|[]
block|{
literal|8
block|}
argument_list|,
name|bitSet
argument_list|,
literal|7
argument_list|)
decl_stmt|;
name|ValidWriteIdList
operator|.
name|RangeResponse
name|rsp
init|=
name|writeIds
operator|.
name|isWriteIdRangeValid
argument_list|(
literal|7
argument_list|,
literal|9
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidWriteIdList
operator|.
name|RangeResponse
operator|.
name|NONE
argument_list|,
name|rsp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|exceptionsAbveHighWaterMark
parameter_list|()
block|{
name|BitSet
name|bitSet
init|=
operator|new
name|BitSet
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|bitSet
operator|.
name|set
argument_list|(
literal|0
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|ValidWriteIdList
name|writeIds
init|=
operator|new
name|ValidCompactorWriteIdList
argument_list|(
name|tableName
argument_list|,
operator|new
name|long
index|[]
block|{
literal|8
block|,
literal|11
block|,
literal|17
block|,
literal|29
block|}
argument_list|,
name|bitSet
argument_list|,
literal|15
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
literal|""
argument_list|,
operator|new
name|long
index|[]
block|{
literal|8
block|,
literal|11
block|}
argument_list|,
name|writeIds
operator|.
name|getInvalidWriteIds
argument_list|()
argument_list|)
expr_stmt|;
name|ValidWriteIdList
operator|.
name|RangeResponse
name|rsp
init|=
name|writeIds
operator|.
name|isWriteIdRangeValid
argument_list|(
literal|7
argument_list|,
literal|9
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidWriteIdList
operator|.
name|RangeResponse
operator|.
name|ALL
argument_list|,
name|rsp
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|writeIds
operator|.
name|isWriteIdRangeValid
argument_list|(
literal|12
argument_list|,
literal|16
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidWriteIdList
operator|.
name|RangeResponse
operator|.
name|NONE
argument_list|,
name|rsp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|writeToString
parameter_list|()
block|{
name|BitSet
name|bitSet
init|=
operator|new
name|BitSet
argument_list|(
literal|4
argument_list|)
decl_stmt|;
name|bitSet
operator|.
name|set
argument_list|(
literal|0
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|ValidWriteIdList
name|writeIds
init|=
operator|new
name|ValidCompactorWriteIdList
argument_list|(
name|tableName
argument_list|,
operator|new
name|long
index|[]
block|{
literal|7
block|,
literal|9
block|,
literal|10
block|,
name|Long
operator|.
name|MAX_VALUE
block|}
argument_list|,
name|bitSet
argument_list|,
literal|8
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|tableName
operator|+
literal|":8:"
operator|+
name|Long
operator|.
name|MAX_VALUE
operator|+
literal|"::7"
argument_list|,
name|writeIds
operator|.
name|writeToString
argument_list|()
argument_list|)
expr_stmt|;
name|writeIds
operator|=
operator|new
name|ValidCompactorWriteIdList
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"null:"
operator|+
name|Long
operator|.
name|toString
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
operator|+
literal|":"
operator|+
name|Long
operator|.
name|MAX_VALUE
operator|+
literal|"::"
argument_list|,
name|writeIds
operator|.
name|writeToString
argument_list|()
argument_list|)
expr_stmt|;
name|writeIds
operator|=
operator|new
name|ValidCompactorWriteIdList
argument_list|(
name|tableName
argument_list|,
operator|new
name|long
index|[
literal|0
index|]
argument_list|,
operator|new
name|BitSet
argument_list|()
argument_list|,
literal|23
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|tableName
operator|+
literal|":23:"
operator|+
name|Long
operator|.
name|MAX_VALUE
operator|+
literal|"::"
argument_list|,
name|writeIds
operator|.
name|writeToString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|readFromString
parameter_list|()
block|{
name|ValidCompactorWriteIdList
name|writeIds
init|=
operator|new
name|ValidCompactorWriteIdList
argument_list|(
name|tableName
operator|+
literal|":37:"
operator|+
name|Long
operator|.
name|MAX_VALUE
operator|+
literal|"::7,9,10"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|tableName
argument_list|,
name|writeIds
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|37L
argument_list|,
name|writeIds
operator|.
name|getHighWatermark
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|writeIds
operator|.
name|getMinOpenWriteId
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
operator|new
name|long
index|[]
block|{
literal|7L
block|,
literal|9L
block|,
literal|10L
block|}
argument_list|,
name|writeIds
operator|.
name|getInvalidWriteIds
argument_list|()
argument_list|)
expr_stmt|;
name|writeIds
operator|=
operator|new
name|ValidCompactorWriteIdList
argument_list|(
name|tableName
operator|+
literal|":21:"
operator|+
name|Long
operator|.
name|MAX_VALUE
operator|+
literal|":"
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|21L
argument_list|,
name|writeIds
operator|.
name|getHighWatermark
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|writeIds
operator|.
name|getMinOpenWriteId
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|writeIds
operator|.
name|getInvalidWriteIds
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAbortedTxn
parameter_list|()
throws|throws
name|Exception
block|{
name|ValidCompactorWriteIdList
name|writeIdList
init|=
operator|new
name|ValidCompactorWriteIdList
argument_list|(
name|tableName
operator|+
literal|":5:4::1,2,3"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|5L
argument_list|,
name|writeIdList
operator|.
name|getHighWatermark
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|writeIdList
operator|.
name|getMinOpenWriteId
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
operator|new
name|long
index|[]
block|{
literal|1L
block|,
literal|2L
block|,
literal|3L
block|}
argument_list|,
name|writeIdList
operator|.
name|getInvalidWriteIds
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAbortedRange
parameter_list|()
throws|throws
name|Exception
block|{
name|ValidCompactorWriteIdList
name|writeIdList
init|=
operator|new
name|ValidCompactorWriteIdList
argument_list|(
name|tableName
operator|+
literal|":11:4::5,6,7,8"
argument_list|)
decl_stmt|;
name|ValidWriteIdList
operator|.
name|RangeResponse
name|rsp
init|=
name|writeIdList
operator|.
name|isWriteIdRangeAborted
argument_list|(
literal|1L
argument_list|,
literal|3L
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidWriteIdList
operator|.
name|RangeResponse
operator|.
name|NONE
argument_list|,
name|rsp
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|writeIdList
operator|.
name|isWriteIdRangeAborted
argument_list|(
literal|9L
argument_list|,
literal|10L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidWriteIdList
operator|.
name|RangeResponse
operator|.
name|NONE
argument_list|,
name|rsp
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|writeIdList
operator|.
name|isWriteIdRangeAborted
argument_list|(
literal|6L
argument_list|,
literal|7L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidWriteIdList
operator|.
name|RangeResponse
operator|.
name|ALL
argument_list|,
name|rsp
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|writeIdList
operator|.
name|isWriteIdRangeAborted
argument_list|(
literal|4L
argument_list|,
literal|6L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidWriteIdList
operator|.
name|RangeResponse
operator|.
name|SOME
argument_list|,
name|rsp
argument_list|)
expr_stmt|;
name|rsp
operator|=
name|writeIdList
operator|.
name|isWriteIdRangeAborted
argument_list|(
literal|6L
argument_list|,
literal|13L
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|ValidWriteIdList
operator|.
name|RangeResponse
operator|.
name|SOME
argument_list|,
name|rsp
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

