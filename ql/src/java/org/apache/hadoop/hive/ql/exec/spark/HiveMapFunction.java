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
name|spark
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Utilities
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
name|hive
operator|.
name|ql
operator|.
name|io
operator|.
name|merge
operator|.
name|MergeFileMapper
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
name|Reporter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|TaskContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|function
operator|.
name|PairFlatMapFunction
import|;
end_import

begin_import
import|import
name|scala
operator|.
name|Tuple2
import|;
end_import

begin_class
specifier|public
class|class
name|HiveMapFunction
implements|implements
name|PairFlatMapFunction
argument_list|<
name|Iterator
argument_list|<
name|Tuple2
argument_list|<
name|BytesWritable
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
argument_list|,
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|1L
decl_stmt|;
specifier|private
specifier|transient
name|JobConf
name|jobConf
decl_stmt|;
specifier|private
name|byte
index|[]
name|buffer
decl_stmt|;
specifier|public
name|HiveMapFunction
parameter_list|(
name|byte
index|[]
name|buffer
parameter_list|)
block|{
name|this
operator|.
name|buffer
operator|=
name|buffer
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Iterable
argument_list|<
name|Tuple2
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
name|call
parameter_list|(
name|Iterator
argument_list|<
name|Tuple2
argument_list|<
name|BytesWritable
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
name|it
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|jobConf
operator|==
literal|null
condition|)
block|{
name|jobConf
operator|=
name|KryoSerializer
operator|.
name|deserializeJobConf
argument_list|(
name|this
operator|.
name|buffer
argument_list|)
expr_stmt|;
name|SparkUtilities
operator|.
name|setTaskInfoInJobConf
argument_list|(
name|jobConf
argument_list|,
name|TaskContext
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|SparkRecordHandler
name|mapRecordHandler
decl_stmt|;
comment|// need different record handler for MergeFileWork
if|if
condition|(
name|MergeFileMapper
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|jobConf
operator|.
name|get
argument_list|(
name|Utilities
operator|.
name|MAPRED_MAPPER_CLASS
argument_list|)
argument_list|)
condition|)
block|{
name|mapRecordHandler
operator|=
operator|new
name|SparkMergeFileRecordHandler
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|mapRecordHandler
operator|=
operator|new
name|SparkMapRecordHandler
argument_list|()
expr_stmt|;
block|}
name|HiveMapFunctionResultList
name|result
init|=
operator|new
name|HiveMapFunctionResultList
argument_list|(
name|jobConf
argument_list|,
name|it
argument_list|,
name|mapRecordHandler
argument_list|)
decl_stmt|;
comment|//TODO we need to implement a Spark specified Reporter to collect stats, refer to HIVE-7709.
name|mapRecordHandler
operator|.
name|init
argument_list|(
name|jobConf
argument_list|,
name|result
argument_list|,
name|Reporter
operator|.
name|NULL
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

