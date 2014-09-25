begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  Licensed to the Apache Software Foundation (ASF) under one  *  or more contributor license agreements.  See the NOTICE file  *  distributed with this work for additional information  *  regarding copyright ownership.  The ASF licenses this file  *  to you under the Apache License, Version 2.0 (the  *  "License"); you may not use this file except in compliance  *  with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
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
name|spark
operator|.
name|api
operator|.
name|java
operator|.
name|JavaPairRDD
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

begin_class
specifier|public
class|class
name|SparkPlan
block|{
name|List
argument_list|<
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
name|rdds
decl_stmt|;
specifier|public
name|SparkPlan
parameter_list|(
name|List
argument_list|<
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
argument_list|>
name|rs
parameter_list|)
block|{
name|rdds
operator|=
name|rs
expr_stmt|;
block|}
specifier|public
name|void
name|execute
parameter_list|()
throws|throws
name|Exception
block|{
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|finalRDD
init|=
literal|null
decl_stmt|;
for|for
control|(
name|JavaPairRDD
argument_list|<
name|HiveKey
argument_list|,
name|BytesWritable
argument_list|>
name|rdd
range|:
name|rdds
control|)
block|{
if|if
condition|(
name|finalRDD
operator|==
literal|null
condition|)
block|{
name|finalRDD
operator|=
name|rdd
expr_stmt|;
block|}
else|else
block|{
name|finalRDD
operator|=
name|finalRDD
operator|.
name|union
argument_list|(
name|rdd
argument_list|)
expr_stmt|;
block|}
name|finalRDD
operator|.
name|foreach
argument_list|(
name|HiveVoidFunction
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

