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
name|conf
operator|.
name|Configuration
import|;
end_import

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
name|ConsumerConfig
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
name|util
operator|.
name|Properties
import|;
end_import

begin_comment
comment|/**  * Test for Utility class.  */
end_comment

begin_class
specifier|public
class|class
name|KafkaStreamingUtilsTest
block|{
specifier|public
name|KafkaStreamingUtilsTest
parameter_list|()
block|{   }
annotation|@
name|Test
specifier|public
name|void
name|testConsumerProperties
parameter_list|()
block|{
name|Configuration
name|configuration
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|configuration
operator|.
name|set
argument_list|(
literal|"kafka.bootstrap.servers"
argument_list|,
literal|"localhost:9090"
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|set
argument_list|(
literal|"kafka.consumer.fetch.max.wait.ms"
argument_list|,
literal|"40"
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|set
argument_list|(
literal|"kafka.consumer.my.new.wait.ms"
argument_list|,
literal|"400"
argument_list|)
expr_stmt|;
name|Properties
name|properties
init|=
name|KafkaStreamingUtils
operator|.
name|consumerProperties
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"localhost:9090"
argument_list|,
name|properties
operator|.
name|getProperty
argument_list|(
literal|"bootstrap.servers"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"40"
argument_list|,
name|properties
operator|.
name|getProperty
argument_list|(
literal|"fetch.max.wait.ms"
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"400"
argument_list|,
name|properties
operator|.
name|getProperty
argument_list|(
literal|"my.new.wait.ms"
argument_list|)
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
name|canNotSetForbiddenProp
parameter_list|()
block|{
name|Configuration
name|configuration
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|configuration
operator|.
name|set
argument_list|(
literal|"kafka.bootstrap.servers"
argument_list|,
literal|"localhost:9090"
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|set
argument_list|(
literal|"kafka.consumer."
operator|+
name|ConsumerConfig
operator|.
name|ENABLE_AUTO_COMMIT_CONFIG
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|KafkaStreamingUtils
operator|.
name|consumerProperties
argument_list|(
name|configuration
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
name|canNotSetForbiddenProp2
parameter_list|()
block|{
name|Configuration
name|configuration
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|configuration
operator|.
name|set
argument_list|(
literal|"kafka.bootstrap.servers"
argument_list|,
literal|"localhost:9090"
argument_list|)
expr_stmt|;
name|configuration
operator|.
name|set
argument_list|(
literal|"kafka.consumer."
operator|+
name|ConsumerConfig
operator|.
name|AUTO_OFFSET_RESET_CONFIG
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|KafkaStreamingUtils
operator|.
name|consumerProperties
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

