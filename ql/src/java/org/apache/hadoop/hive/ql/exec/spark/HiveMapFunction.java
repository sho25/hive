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
name|fs
operator|.
name|FSDataInputStream
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
name|exec
operator|.
name|mr
operator|.
name|ExecMapper
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
name|plan
operator|.
name|MapWork
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
name|BytesWritable
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
name|ExecMapper
name|mapper
decl_stmt|;
specifier|private
specifier|transient
name|SparkCollector
name|collector
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
name|BytesWritable
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
name|mapper
operator|=
operator|new
name|ExecMapper
argument_list|()
expr_stmt|;
name|mapper
operator|.
name|configure
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
name|collector
operator|=
operator|new
name|SparkCollector
argument_list|()
expr_stmt|;
block|}
name|collector
operator|.
name|clear
argument_list|()
expr_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
operator|&&
operator|!
name|ExecMapper
operator|.
name|getDone
argument_list|()
condition|)
block|{
name|Tuple2
argument_list|<
name|BytesWritable
argument_list|,
name|BytesWritable
argument_list|>
name|input
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
name|mapper
operator|.
name|map
argument_list|(
name|input
operator|.
name|_1
argument_list|()
argument_list|,
name|input
operator|.
name|_2
argument_list|()
argument_list|,
name|collector
argument_list|,
name|Reporter
operator|.
name|NULL
argument_list|)
expr_stmt|;
block|}
name|mapper
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|collector
operator|.
name|getResult
argument_list|()
return|;
block|}
block|}
end_class

end_unit

