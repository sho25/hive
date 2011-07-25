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
package|;
end_package

begin_comment
comment|/**  * MapRedStats.  *  * A data structure to keep one mapreduce's stats:  * number of mappers, number of reducers, accumulative CPU time and whether it  * succeeds.  *  */
end_comment

begin_class
specifier|public
class|class
name|MapRedStats
block|{
name|int
name|numMap
decl_stmt|;
name|int
name|numReduce
decl_stmt|;
name|long
name|cpuMSec
decl_stmt|;
name|long
name|hdfsRead
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|hdfsWrite
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|mapInputRecords
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|mapOutputRecords
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|reduceInputRecords
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|reduceOutputRecords
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|reduceShuffleBytes
init|=
operator|-
literal|1
decl_stmt|;
name|boolean
name|success
decl_stmt|;
specifier|public
name|MapRedStats
parameter_list|(
name|int
name|numMap
parameter_list|,
name|int
name|numReduce
parameter_list|,
name|long
name|cpuMSec
parameter_list|,
name|boolean
name|ifSuccess
parameter_list|)
block|{
name|this
operator|.
name|numMap
operator|=
name|numMap
expr_stmt|;
name|this
operator|.
name|numReduce
operator|=
name|numReduce
expr_stmt|;
name|this
operator|.
name|cpuMSec
operator|=
name|cpuMSec
expr_stmt|;
name|this
operator|.
name|success
operator|=
name|ifSuccess
expr_stmt|;
block|}
specifier|public
name|boolean
name|isSuccess
parameter_list|()
block|{
return|return
name|success
return|;
block|}
specifier|public
name|long
name|getCpuMSec
parameter_list|()
block|{
return|return
name|cpuMSec
return|;
block|}
specifier|public
name|int
name|getNumMap
parameter_list|()
block|{
return|return
name|numMap
return|;
block|}
specifier|public
name|void
name|setNumMap
parameter_list|(
name|int
name|numMap
parameter_list|)
block|{
name|this
operator|.
name|numMap
operator|=
name|numMap
expr_stmt|;
block|}
specifier|public
name|int
name|getNumReduce
parameter_list|()
block|{
return|return
name|numReduce
return|;
block|}
specifier|public
name|void
name|setNumReduce
parameter_list|(
name|int
name|numReduce
parameter_list|)
block|{
name|this
operator|.
name|numReduce
operator|=
name|numReduce
expr_stmt|;
block|}
specifier|public
name|long
name|getHdfsRead
parameter_list|()
block|{
return|return
name|hdfsRead
return|;
block|}
specifier|public
name|void
name|setHdfsRead
parameter_list|(
name|long
name|hdfsRead
parameter_list|)
block|{
name|this
operator|.
name|hdfsRead
operator|=
name|hdfsRead
expr_stmt|;
block|}
specifier|public
name|long
name|getHdfsWrite
parameter_list|()
block|{
return|return
name|hdfsWrite
return|;
block|}
specifier|public
name|void
name|setHdfsWrite
parameter_list|(
name|long
name|hdfsWrite
parameter_list|)
block|{
name|this
operator|.
name|hdfsWrite
operator|=
name|hdfsWrite
expr_stmt|;
block|}
specifier|public
name|long
name|getMapInputRecords
parameter_list|()
block|{
return|return
name|mapInputRecords
return|;
block|}
specifier|public
name|void
name|setMapInputRecords
parameter_list|(
name|long
name|mapInputRecords
parameter_list|)
block|{
name|this
operator|.
name|mapInputRecords
operator|=
name|mapInputRecords
expr_stmt|;
block|}
specifier|public
name|long
name|getMapOutputRecords
parameter_list|()
block|{
return|return
name|mapOutputRecords
return|;
block|}
specifier|public
name|void
name|setMapOutputRecords
parameter_list|(
name|long
name|mapOutputRecords
parameter_list|)
block|{
name|this
operator|.
name|mapOutputRecords
operator|=
name|mapOutputRecords
expr_stmt|;
block|}
specifier|public
name|long
name|getReduceInputRecords
parameter_list|()
block|{
return|return
name|reduceInputRecords
return|;
block|}
specifier|public
name|void
name|setReduceInputRecords
parameter_list|(
name|long
name|reduceInputRecords
parameter_list|)
block|{
name|this
operator|.
name|reduceInputRecords
operator|=
name|reduceInputRecords
expr_stmt|;
block|}
specifier|public
name|long
name|getReduceOutputRecords
parameter_list|()
block|{
return|return
name|reduceOutputRecords
return|;
block|}
specifier|public
name|void
name|setReduceOutputRecords
parameter_list|(
name|long
name|reduceOutputRecords
parameter_list|)
block|{
name|this
operator|.
name|reduceOutputRecords
operator|=
name|reduceOutputRecords
expr_stmt|;
block|}
specifier|public
name|long
name|getReduceShuffleBytes
parameter_list|()
block|{
return|return
name|reduceShuffleBytes
return|;
block|}
specifier|public
name|void
name|setReduceShuffleBytes
parameter_list|(
name|long
name|reduceShuffleBytes
parameter_list|)
block|{
name|this
operator|.
name|reduceShuffleBytes
operator|=
name|reduceShuffleBytes
expr_stmt|;
block|}
specifier|public
name|void
name|setCpuMSec
parameter_list|(
name|long
name|cpuMSec
parameter_list|)
block|{
name|this
operator|.
name|cpuMSec
operator|=
name|cpuMSec
expr_stmt|;
block|}
specifier|public
name|void
name|setSuccess
parameter_list|(
name|boolean
name|success
parameter_list|)
block|{
name|this
operator|.
name|success
operator|=
name|success
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|numMap
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"Map: "
operator|+
name|numMap
operator|+
literal|"  "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|numReduce
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"Reduce: "
operator|+
name|numReduce
operator|+
literal|"  "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cpuMSec
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" Accumulative CPU: "
operator|+
operator|(
name|cpuMSec
operator|/
literal|1000D
operator|)
operator|+
literal|" sec  "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hdfsRead
operator|>=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" HDFS Read: "
operator|+
name|hdfsRead
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hdfsWrite
operator|>=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" HDFS Write: "
operator|+
name|hdfsWrite
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|" "
operator|+
operator|(
name|success
condition|?
literal|"SUCESS"
else|:
literal|"FAIL"
operator|)
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

