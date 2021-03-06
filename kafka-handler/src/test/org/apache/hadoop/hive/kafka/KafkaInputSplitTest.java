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
name|kafka
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
name|fs
operator|.
name|Path
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
name|DataInputBuffer
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
name|DataOutputBuffer
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
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * Kafka Hadoop InputSplit Test.  */
end_comment

begin_class
specifier|public
class|class
name|KafkaInputSplitTest
block|{
specifier|private
specifier|final
name|KafkaInputSplit
name|expectedInputSplit
decl_stmt|;
specifier|public
name|KafkaInputSplitTest
parameter_list|()
block|{
name|String
name|topic
init|=
literal|"my_topic"
decl_stmt|;
name|this
operator|.
name|expectedInputSplit
operator|=
operator|new
name|KafkaInputSplit
argument_list|(
name|topic
argument_list|,
literal|1
argument_list|,
literal|50L
argument_list|,
literal|56L
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWriteRead
parameter_list|()
throws|throws
name|IOException
block|{
name|DataOutputBuffer
name|output
init|=
operator|new
name|DataOutputBuffer
argument_list|()
decl_stmt|;
name|this
operator|.
name|expectedInputSplit
operator|.
name|write
argument_list|(
name|output
argument_list|)
expr_stmt|;
name|KafkaInputSplit
name|kafkaInputSplit
init|=
operator|new
name|KafkaInputSplit
argument_list|()
decl_stmt|;
name|DataInputBuffer
name|input
init|=
operator|new
name|DataInputBuffer
argument_list|()
decl_stmt|;
name|input
operator|.
name|reset
argument_list|(
name|output
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|output
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
name|kafkaInputSplit
operator|.
name|readFields
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|this
operator|.
name|expectedInputSplit
argument_list|,
name|kafkaInputSplit
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|andRangeOverLapping
parameter_list|()
block|{
name|KafkaInputSplit
name|kafkaInputSplit
init|=
operator|new
name|KafkaInputSplit
argument_list|(
literal|"test-topic"
argument_list|,
literal|2
argument_list|,
literal|10
argument_list|,
literal|400
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|KafkaInputSplit
name|kafkaInputSplit2
init|=
operator|new
name|KafkaInputSplit
argument_list|(
literal|"test-topic"
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|200
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|new
name|KafkaInputSplit
argument_list|(
literal|"test-topic"
argument_list|,
literal|2
argument_list|,
literal|10
argument_list|,
literal|200
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|)
argument_list|)
argument_list|,
name|KafkaInputSplit
operator|.
name|intersectRange
argument_list|(
name|kafkaInputSplit
argument_list|,
name|kafkaInputSplit2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|andRangeNonOverLapping
parameter_list|()
block|{
name|KafkaInputSplit
name|kafkaInputSplit
init|=
operator|new
name|KafkaInputSplit
argument_list|(
literal|"test-topic"
argument_list|,
literal|2
argument_list|,
literal|10
argument_list|,
literal|400
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|KafkaInputSplit
name|kafkaInputSplit2
init|=
operator|new
name|KafkaInputSplit
argument_list|(
literal|"test-topic"
argument_list|,
literal|2
argument_list|,
literal|550
argument_list|,
literal|700
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertNull
argument_list|(
name|KafkaInputSplit
operator|.
name|intersectRange
argument_list|(
name|kafkaInputSplit
argument_list|,
name|kafkaInputSplit2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|orRange
parameter_list|()
block|{
name|KafkaInputSplit
name|kafkaInputSplit
init|=
operator|new
name|KafkaInputSplit
argument_list|(
literal|"test-topic"
argument_list|,
literal|2
argument_list|,
literal|300
argument_list|,
literal|400
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|KafkaInputSplit
name|kafkaInputSplit2
init|=
operator|new
name|KafkaInputSplit
argument_list|(
literal|"test-topic"
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|,
literal|600
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|kafkaInputSplit2
argument_list|,
name|KafkaInputSplit
operator|.
name|unionRange
argument_list|(
name|kafkaInputSplit
argument_list|,
name|kafkaInputSplit2
argument_list|)
argument_list|)
expr_stmt|;
name|KafkaInputSplit
name|kafkaInputSplit3
init|=
operator|new
name|KafkaInputSplit
argument_list|(
literal|"test-topic"
argument_list|,
literal|2
argument_list|,
literal|700
argument_list|,
literal|6000
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
operator|new
name|KafkaInputSplit
argument_list|(
literal|"test-topic"
argument_list|,
literal|2
argument_list|,
literal|300
argument_list|,
literal|6000
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|)
argument_list|)
argument_list|,
name|KafkaInputSplit
operator|.
name|unionRange
argument_list|(
name|kafkaInputSplit
argument_list|,
name|kafkaInputSplit3
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|copyOf
parameter_list|()
block|{
name|KafkaInputSplit
name|kafkaInputSplit
init|=
operator|new
name|KafkaInputSplit
argument_list|(
literal|"test-topic"
argument_list|,
literal|2
argument_list|,
literal|300
argument_list|,
literal|400
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|KafkaInputSplit
name|copyOf
init|=
name|KafkaInputSplit
operator|.
name|copyOf
argument_list|(
name|kafkaInputSplit
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|kafkaInputSplit
argument_list|,
name|copyOf
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotSame
argument_list|(
name|kafkaInputSplit
argument_list|,
name|copyOf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testClone
parameter_list|()
block|{
name|KafkaInputSplit
name|kafkaInputSplit
init|=
operator|new
name|KafkaInputSplit
argument_list|(
literal|"test-topic"
argument_list|,
literal|2
argument_list|,
literal|300
argument_list|,
literal|400
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|KafkaInputSplit
name|clone
init|=
name|KafkaInputSplit
operator|.
name|copyOf
argument_list|(
name|kafkaInputSplit
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|kafkaInputSplit
argument_list|,
name|clone
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotSame
argument_list|(
name|clone
argument_list|,
name|kafkaInputSplit
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSlice
parameter_list|()
block|{
name|KafkaInputSplit
name|kafkaInputSplit
init|=
operator|new
name|KafkaInputSplit
argument_list|(
literal|"test-topic"
argument_list|,
literal|2
argument_list|,
literal|300
argument_list|,
literal|400
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/tmp"
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KafkaInputSplit
argument_list|>
name|kafkaInputSplitList
init|=
name|KafkaInputSplit
operator|.
name|slice
argument_list|(
literal|14
argument_list|,
name|kafkaInputSplit
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|kafkaInputSplitList
operator|.
name|stream
argument_list|()
operator|.
name|mapToLong
argument_list|(
name|kafkaPullerInputSplit1
lambda|->
name|kafkaPullerInputSplit1
operator|.
name|getEndOffset
argument_list|()
operator|-
name|kafkaPullerInputSplit1
operator|.
name|getStartOffset
argument_list|()
argument_list|)
operator|.
name|sum
argument_list|()
argument_list|,
name|kafkaInputSplit
operator|.
name|getEndOffset
argument_list|()
operator|-
name|kafkaInputSplit
operator|.
name|getStartOffset
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|kafkaInputSplitList
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|kafkaPullerInputSplit1
lambda|->
name|kafkaInputSplit
operator|.
name|getStartOffset
argument_list|()
operator|==
name|kafkaPullerInputSplit1
operator|.
name|getStartOffset
argument_list|()
argument_list|)
operator|.
name|count
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|kafkaInputSplitList
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|kafkaPullerInputSplit1
lambda|->
name|kafkaInputSplit
operator|.
name|getEndOffset
argument_list|()
operator|==
name|kafkaPullerInputSplit1
operator|.
name|getEndOffset
argument_list|()
argument_list|)
operator|.
name|count
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

