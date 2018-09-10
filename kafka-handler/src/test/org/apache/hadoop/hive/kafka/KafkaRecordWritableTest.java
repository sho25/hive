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
name|kafka
operator|.
name|clients
operator|.
name|consumer
operator|.
name|ConsumerRecord
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
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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

begin_comment
comment|/**  * Test class for kafka Writable.  */
end_comment

begin_class
specifier|public
class|class
name|KafkaRecordWritableTest
block|{
specifier|public
name|KafkaRecordWritableTest
parameter_list|()
block|{   }
annotation|@
name|Test
specifier|public
name|void
name|testWriteReadFields
parameter_list|()
throws|throws
name|IOException
block|{
name|ConsumerRecord
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|record
init|=
operator|new
name|ConsumerRecord
argument_list|(
literal|"topic"
argument_list|,
literal|0
argument_list|,
literal|3L
argument_list|,
literal|"key"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"value"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|KafkaRecordWritable
name|kafkaRecordWritable
init|=
operator|new
name|KafkaRecordWritable
argument_list|(
name|record
operator|.
name|partition
argument_list|()
argument_list|,
name|record
operator|.
name|offset
argument_list|()
argument_list|,
name|record
operator|.
name|timestamp
argument_list|()
argument_list|,
name|record
operator|.
name|value
argument_list|()
argument_list|,
literal|0L
argument_list|,
literal|100L
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|w
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|kafkaRecordWritable
operator|.
name|write
argument_list|(
name|w
argument_list|)
expr_stmt|;
name|w
operator|.
name|flush
argument_list|()
expr_stmt|;
name|ByteArrayInputStream
name|input
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|DataInputStream
name|inputStream
init|=
operator|new
name|DataInputStream
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|KafkaRecordWritable
name|actualKafkaRecordWritable
init|=
operator|new
name|KafkaRecordWritable
argument_list|()
decl_stmt|;
name|actualKafkaRecordWritable
operator|.
name|readFields
argument_list|(
name|inputStream
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|kafkaRecordWritable
argument_list|,
name|actualKafkaRecordWritable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWriteReadFields2
parameter_list|()
throws|throws
name|IOException
block|{
name|ConsumerRecord
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|record
init|=
operator|new
name|ConsumerRecord
argument_list|(
literal|"topic"
argument_list|,
literal|0
argument_list|,
literal|3L
argument_list|,
literal|"key"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"value"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|KafkaRecordWritable
name|kafkaRecordWritable
init|=
operator|new
name|KafkaRecordWritable
argument_list|(
name|record
operator|.
name|partition
argument_list|()
argument_list|,
name|record
operator|.
name|offset
argument_list|()
argument_list|,
name|record
operator|.
name|timestamp
argument_list|()
argument_list|,
name|record
operator|.
name|value
argument_list|()
argument_list|,
literal|0L
argument_list|,
literal|100L
argument_list|,
literal|"thiskey"
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|w
init|=
operator|new
name|DataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|kafkaRecordWritable
operator|.
name|write
argument_list|(
name|w
argument_list|)
expr_stmt|;
name|w
operator|.
name|flush
argument_list|()
expr_stmt|;
name|ByteArrayInputStream
name|input
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|DataInputStream
name|inputStream
init|=
operator|new
name|DataInputStream
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|KafkaRecordWritable
name|actualKafkaRecordWritable
init|=
operator|new
name|KafkaRecordWritable
argument_list|()
decl_stmt|;
name|actualKafkaRecordWritable
operator|.
name|readFields
argument_list|(
name|inputStream
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|kafkaRecordWritable
argument_list|,
name|actualKafkaRecordWritable
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

