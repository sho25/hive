begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HiveKey
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
name|BytesWritable
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
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|Partitioner
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
name|mapred
operator|.
name|lib
operator|.
name|TotalOrderPartitioner
import|;
end_import

begin_class
specifier|public
class|class
name|HiveTotalOrderPartitioner
implements|implements
name|Partitioner
argument_list|<
name|HiveKey
argument_list|,
name|Object
argument_list|>
block|{
specifier|private
name|Partitioner
argument_list|<
name|BytesWritable
argument_list|,
name|Object
argument_list|>
name|partitioner
init|=
operator|new
name|TotalOrderPartitioner
argument_list|<
name|BytesWritable
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|void
name|configure
parameter_list|(
name|JobConf
name|job
parameter_list|)
block|{
name|JobConf
name|newconf
init|=
operator|new
name|JobConf
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|newconf
operator|.
name|setMapOutputKeyClass
argument_list|(
name|BytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|partitioner
operator|.
name|configure
argument_list|(
name|newconf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|getPartition
parameter_list|(
name|HiveKey
name|key
parameter_list|,
name|Object
name|value
parameter_list|,
name|int
name|numPartitions
parameter_list|)
block|{
return|return
name|partitioner
operator|.
name|getPartition
argument_list|(
name|key
argument_list|,
name|value
argument_list|,
name|numPartitions
argument_list|)
return|;
block|}
block|}
end_class

end_unit

